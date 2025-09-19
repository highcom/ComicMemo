package com.highcom.comicmemo.ui.search

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.util.Log
import androidx.fragment.app.Fragment
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
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import com.highcom.comicmemo.R
import com.highcom.comicmemo.databinding.FragmentBarcodeSearchBinding
import com.highcom.comicmemo.viewmodel.RakutenApiStatus
import com.highcom.comicmemo.viewmodel.RakutenBookViewModel

/**
 * カメラプレビューを表示し、ML Kit を利用してリアルタイムにバーコードを読み取る Fragment。
 *
 * - カメラ権限を実行時に要求します。
 * - 権限が許可されると CameraX を起動し、Preview と ImageAnalysis をライフサイクルにバインドします。
 * - バーコードを検出したら Navigation を使って結果画面へ遷移します。
 */
class BarcodeSearchFragment : Fragment() {
    private lateinit var binding: FragmentBarcodeSearchBinding
    /** ライブカメラ映像を表示する PreviewView。 */
    private lateinit var previewView: PreviewView
    /** Activityで生成されたViewModelを利用する */
    private val viewModel: RakutenBookViewModel by activityViewModels()
    @Suppress("DEPRECATION")
    private val handler = Handler()
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
        if (granted) startCamera() else
            Toast.makeText(requireContext(), "カメラ権限が必要です", Toast.LENGTH_LONG).show()
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            startCamera()
        } else {
            requestPermission.launch(Manifest.permission.CAMERA)
        }

        // ViewModelの初期設定
        viewModel.initialize(getString(R.string.rakuten_app_id), RakutenBookViewModel.GENRE_ID_COMIC)

        // 楽天APIの呼び出し状況に応じてプログレスサークルの表示
        viewModel.status.observe(viewLifecycleOwner) { apiStatus ->
            when (apiStatus) {
                RakutenApiStatus.LOADING -> handler.post { binding.progressBar.visibility = View.VISIBLE }
                else -> handler.post { binding.progressBar.visibility = View.INVISIBLE }
            }
        }

        // 楽天書籍データを監視
        viewModel.bookList.observe(viewLifecycleOwner) {
            if (it.isNullOrEmpty()) return@observe
            findNavController().navigate(R.id.action_barcode_search_fragment_to_book_detail_fragment, bundleOf("BUNDLE_ITEM_DATA" to it.first()))
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
                        val value = barcodes.first().rawValue ?: ""
                        // TODO:ここら辺のログの処理は不要
                        Log.d("BarcodeSearchFragment", "barcode: $value")
                        // 読み取ったら遷移。重複防止で一度だけ
                        // TODO:何度も呼び出してしまうので呼び出しをステータスで管理したほうが良さげ
                        viewModel.searchIsbn(value)
                    }
                }
                .addOnFailureListener { Log.e("BarcodeSearchFragment", "scan failed", it) }
                .addOnCompleteListener { imageProxy.close() }
        } else imageProxy.close()
    }
}