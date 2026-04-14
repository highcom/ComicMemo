package com.highcom.comicmemo.ui.search

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import com.highcom.comicmemo.R
import com.highcom.comicmemo.billing.SubscriptionManager
import com.highcom.comicmemo.databinding.FragmentBarcodeSearchBinding
import com.highcom.comicmemo.viewmodel.RakutenApiStatus
import com.highcom.comicmemo.viewmodel.RakutenBookViewModel
import kotlinx.coroutines.cancel

/**
 * カメラプレビューを表示し、ML Kit を利用してリアルタイムにバーコードを読み取る Fragment。
 *
 * - カメラ権限を実行時に要求します。
 * - 権限が許可されると CameraX を起動し、Preview と ImageAnalysis をライフサイクルにバインドします。
 * - バーコードを検出したら Navigation を使って結果画面へ遷移します。
 * - 同一 Activity 内のバナー広告と描画合成が干渉しないよう、プレビューはレイアウトで COMPATIBLE（TextureView）実装を指定しています。
 */
class BarcodeSearchFragment : Fragment() {
    private lateinit var binding: FragmentBarcodeSearchBinding
    /** ライブカメラ映像を表示する PreviewView。 */
    private lateinit var previewView: PreviewView
    /** Activityで生成されたViewModelを利用する */
    private val viewModel: RakutenBookViewModel by activityViewModels()
    @Suppress("DEPRECATION")
    private val handler = Handler()
    /** 検出したバーコードの値 */
    private var codeValue = ""
    /**
     * ML Kit のバーコードスキャナ。
     * QRコード、Code128、EAN-13 を検出対象として設定しています。
     */
    private val scanner by lazy {
        val opts = BarcodeScannerOptions.Builder()
            .setBarcodeFormats(
                Barcode.FORMAT_QR_CODE,
                Barcode.FORMAT_CODE_128,
                Barcode.FORMAT_EAN_13
            ).build()
        BarcodeScanning.getClient(opts)
    }

    private val requestPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            if (shouldStartCamera()) {
                startCamera()
            }
        } else {
            Toast.makeText(requireContext(), "カメラ権限が必要です", Toast.LENGTH_LONG).show()
        }
    }

    /**
     * カメラ権限をリクエストするためのランチャー。
     * ユーザーが拒否した場合は Toast で警告を表示します。
     */
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentBarcodeSearchBinding.inflate(layoutInflater, container, false)
        previewView = binding.previewView
        return binding.root
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        updateSearchLimitUI()
        // イベントログ出力
        val param = Bundle().apply { putInt("free_search_count", SubscriptionManager.getRemainingFreeSearchCount(requireContext())) }
        Firebase.analytics.logEvent("barcode_search", param)

        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            if (shouldStartCamera()) {
                startCamera()
            }
        } else {
            requestPermission.launch(Manifest.permission.CAMERA)
        }

        // ViewModelの初期設定
        viewModel.initialize(RakutenBookViewModel.GENRE_ID_COMIC)

        // 楽天APIの呼び出し状況に応じてプログレスサークルを表示
        viewModel.status.observe(viewLifecycleOwner) { apiStatus ->
            when (apiStatus) {
                RakutenApiStatus.LOADING -> handler.post { binding.progressBar.visibility = View.VISIBLE }
                else -> handler.post { binding.progressBar.visibility = View.INVISIBLE }
            }
        }

        // 楽天書籍データを監視
        lifecycleScope.launchWhenStarted {
            viewModel.isbnBookList.collect {
                if (it.isNullOrEmpty()) {
                    if (codeValue.isNotEmpty()) {
                        binding.barcodeResult.text = getString(R.string.barcode_result_failure) + codeValue
                    }
                } else {
                    // 検索結果があった場合にデクリメント（無料会員のみ）
                    if (!SubscriptionManager.isPremium(requireContext())) {
                        SubscriptionManager.decrementFreeSearchCount(requireContext())
                    }
                    // 該当データがあれば詳細画面へ遷移して監視を終了
                    if (findNavController().currentDestination?.id == R.id.barcodeSearchFragment) {
                        findNavController().navigate(R.id.action_barcode_search_fragment_to_book_detail_fragment, bundleOf("BUNDLE_ITEM_DATA" to it.first()))
                        cancel()
                    }
                }
            }
        }

        requireActivity().addMenuProvider(object : MenuProvider {
            /**
             * アクションバーのメニュー生成
             *
             * @param menu メニュー
             * @param menuInflater インフレーター
             */
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {}

            /**
             * アクションバーのメニュー選択処理
             *
             * @param menuItem メニューアイテム
             * @return 選択処理を行った場合はtrue
             */
            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                when (menuItem.itemId) {
                    android.R.id.home -> {
                        requireActivity().finish()
                        return true
                    }
                }
                return false
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)

        // バーコードが読み取れない時の手入力番号の検索
        binding.searchButton.setOnClickListener {
            val code = binding.manualInput.text.toString()
            if (code.isNotBlank()) {
                codeValue = code
                viewModel.searchIsbn(codeValue)
            } else {
                Snackbar.make(requireView(), getString(R.string.input_failure), Snackbar.LENGTH_LONG).setAction("Action", null).show()
            }
        }

        // 有料会員プランボタンのクリックリスナー
        binding.goToPremiumButton.setOnClickListener {
            if (findNavController().currentDestination?.id == R.id.barcodeSearchFragment) {
                findNavController().navigate(R.id.action_barcodeSearchFragment_to_settingFragment)
            }
        }
    }

    /**
     * 検索制限UIの更新
     */
    private fun updateSearchLimitUI() {
        val context = requireContext()
        if (SubscriptionManager.isPremium(context)) {
            binding.searchLimitMessage.visibility = View.GONE
            binding.goToPremiumButton.visibility = View.GONE
            binding.searchButton.isEnabled = true
        } else {
            val remainingCount = SubscriptionManager.getRemainingFreeSearchCount(context)
            binding.searchLimitMessage.visibility = View.VISIBLE
            if (remainingCount > 0) {
                binding.searchLimitMessage.text = getString(R.string.free_search_limit_message, remainingCount)
                binding.goToPremiumButton.visibility = View.GONE
                binding.searchButton.isEnabled = true
            } else {
                binding.searchLimitMessage.text = getString(R.string.free_search_limit_reached)
                binding.goToPremiumButton.visibility = View.VISIBLE
                binding.searchButton.isEnabled = false
            }
        }
    }

    /**
     * カメラを開始すべきかどうか
     */
    private fun shouldStartCamera(): Boolean {
        return SubscriptionManager.isPremium(requireContext()) || SubscriptionManager.getRemainingFreeSearchCount(requireContext()) > 0
    }


    /**
     * CameraX を初期化し、Preview と ImageAnalysis の各ユースケースを
     * ライフサイクルにバインドします。
     *
     * カメラ権限が許可された後に呼び出します。
     */
    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
        cameraProviderFuture.addListener({
            val provider = cameraProviderFuture.get()
            val preview = Preview.Builder().build().apply {
                setSurfaceProvider(previewView.surfaceProvider)
            }

            val analysis = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
            analysis.setAnalyzer(
                ContextCompat.getMainExecutor(requireContext())
            ) { imageProxy -> processImage(imageProxy) }

            provider.unbindAll()
            provider.bindToLifecycle(
                viewLifecycleOwner,
                CameraSelector.DEFAULT_BACK_CAMERA,
                preview,
                analysis
            )
        }, ContextCompat.getMainExecutor(requireContext()))
    }

    /**
     * 取得した各フレームを ML Kit で解析し、バーコードを検出します。
     * 検出に成功すると最初のバーコード文字列を取得し、結果画面へ遷移します。
     *
     * @param imageProxy カメラから渡される単一フレーム
     */
    @SuppressLint("UnsafeOptInUsageError")
    private fun processImage(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image
        if (mediaImage != null) {
            val input = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
            scanner.process(input)
                .addOnSuccessListener { barcodes ->
                    if (barcodes.isNotEmpty()) {
                        codeValue = barcodes.first().rawValue ?: ""
                        viewModel.searchIsbn(codeValue)
                    }
                }
                .addOnFailureListener {
                    Snackbar.make(requireView(), getString(R.string.read_failure), Snackbar.LENGTH_LONG).setAction("Action", null).show()
                }
                .addOnCompleteListener { imageProxy.close() }
        } else imageProxy.close()
    }
}
