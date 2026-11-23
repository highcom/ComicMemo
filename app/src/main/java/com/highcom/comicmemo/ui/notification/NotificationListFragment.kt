package com.highcom.comicmemo.ui.notification

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.recyclerview.widget.RecyclerView
import com.highcom.comicmemo.R
import com.highcom.comicmemo.databinding.DialogNotificationSettingBinding
import com.highcom.comicmemo.databinding.FragmentNotificationListBinding
import com.highcom.comicmemo.datamodel.BookNotification
import com.highcom.comicmemo.ui.SimpleCallbackHelper
import com.highcom.comicmemo.ui.edit.ComicMemoActivity
import com.highcom.comicmemo.viewmodel.NotificationListViewModel
import dagger.hilt.android.AndroidEntryPoint
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.widget.Toast
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

/**
 * お知らせ通知一覧画面Fragment
 */
@AndroidEntryPoint
class NotificationListFragment : Fragment() {

    private var _binding: FragmentNotificationListBinding? = null
    private val binding get() = _binding!!

    private val viewModel: NotificationListViewModel by viewModels()
    private lateinit var adapter: NotificationListAdapter
    private var simpleCallbackHelper: SimpleCallbackHelper? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNotificationListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireActivity().title = getString(R.string.notification_list)
        (requireActivity() as? ComicMemoActivity)?.setDisplayHomeAsUpEnabled(true)

        // MenuProvider の登録 (ActionBarの戻るボタン対応)
        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: android.view.Menu, menuInflater: android.view.MenuInflater) {
                // オプションメニューのクリア
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                if (menuItem.itemId == android.R.id.home) {
                    requireActivity().onBackPressedDispatcher.onBackPressed()
                    return true
                }
                return false
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)

        adapter = NotificationListAdapter { item ->
            showEditNotificationDialog(item)
        }
        binding.notificationRecyclerView.adapter = adapter

        // スワイプによる削除ボタンの表示・削除操作の設定 (SimpleCallbackHelper)
        val scale = resources.displayMetrics.density
        simpleCallbackHelper = object : SimpleCallbackHelper(
            context,
            binding.notificationRecyclerView,
            scale,
            object : SimpleCallbackListener {
                override fun onSimpleCallbackMove(
                    viewHolder: RecyclerView.ViewHolder,
                    target: RecyclerView.ViewHolder
                ): Boolean = false

                override fun clearSimpleCallbackView(
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder
                ) {}
            }
        ) {
            override fun instantiateUnderlayButton(
                viewHolder: RecyclerView.ViewHolder,
                underlayButtons: MutableList<UnderlayButton>
            ) {
                underlayButtons.add(
                    UnderlayButton(
                        getString(R.string.swipe_delete),
                        0,
                        Color.parseColor("#FF3C30"),
                        viewHolder
                    ) { holder, _ ->
                        clearButtonBuffer()
                        val vh = holder as? NotificationListAdapter.NotificationViewHolder
                        vh?.notificationItem?.let { item ->
                            viewModel.deleteNotification(item.id, requireContext())
                        }
                    }
                )
            }
        }

        // LiveDataの観察
        viewModel.notifications.observe(viewLifecycleOwner) { list ->
            adapter.submitList(list)
            if (list.isNullOrEmpty()) {
                binding.emptyTextView.visibility = View.VISIBLE
                binding.notificationRecyclerView.visibility = View.GONE
            } else {
                binding.emptyTextView.visibility = View.GONE
                binding.notificationRecyclerView.visibility = View.VISIBLE
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.cleanExpiredNotifications()
    }

    /**
     * お知らせ通知設定変更ダイアログの表示処理
     *
     * @param item 対象のお知らせ通知データ
     */
    private fun showEditNotificationDialog(item: BookNotification) {
        val context = requireContext()
        val dialogBinding = DialogNotificationSettingBinding.inflate(layoutInflater)

        dialogBinding.dialogHeaderTitle.text = getString(R.string.notification_edit_setting_title)
        dialogBinding.dialogRegisterButton.text = getString(R.string.dialog_update)

        dialogBinding.dialogBookTitle.text = item.title
        val authorPublisher = when {
            item.author.isNotEmpty() && item.publisher.isNotEmpty() -> "${item.author} / ${item.publisher}"
            item.author.isNotEmpty() -> item.author
            item.publisher.isNotEmpty() -> item.publisher
            else -> ""
        }
        dialogBinding.dialogBookAuthor.text = authorPublisher

        val calendar = Calendar.getInstance()
        calendar.timeInMillis = item.notifyDateTime

        fun updateDateTimeDisplay() {
            val dateFormat = SimpleDateFormat("yyyy/MM/dd (E)", Locale.JAPAN)
            val timeFormat = SimpleDateFormat("HH:mm", Locale.JAPAN)
            dialogBinding.dialogDateView.text = dateFormat.format(calendar.time)
            dialogBinding.dialogTimeView.text = timeFormat.format(calendar.time)
        }

        updateDateTimeDisplay()

        dialogBinding.dialogDateView.setOnClickListener {
            val dateSetListener = DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
                calendar.set(Calendar.YEAR, year)
                calendar.set(Calendar.MONTH, month)
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                updateDateTimeDisplay()
            }
            DatePickerDialog(
                context,
                dateSetListener,
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        dialogBinding.dialogTimeView.setOnClickListener {
            val timeSetListener = TimePickerDialog.OnTimeSetListener { _, hourOfDay, minute ->
                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                calendar.set(Calendar.MINUTE, minute)
                updateDateTimeDisplay()
            }
            TimePickerDialog(
                context,
                timeSetListener,
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                true
            ).show()
        }

        val alertDialog = AlertDialog.Builder(context)
            .setView(dialogBinding.root)
            .create()

        dialogBinding.dialogCancelButton.setOnClickListener {
            alertDialog.dismiss()
        }

        dialogBinding.dialogRegisterButton.setOnClickListener {
            val updated = item.copy(notifyDateTime = calendar.timeInMillis)
            viewModel.updateNotification(updated, context)
            Toast.makeText(context, getString(R.string.notification_updated_toast), Toast.LENGTH_SHORT).show()
            alertDialog.dismiss()
        }

        alertDialog.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
