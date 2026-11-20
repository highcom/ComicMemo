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
import com.highcom.comicmemo.databinding.FragmentNotificationListBinding
import com.highcom.comicmemo.ui.SimpleCallbackHelper
import com.highcom.comicmemo.ui.edit.ComicMemoActivity
import com.highcom.comicmemo.viewmodel.NotificationListViewModel
import dagger.hilt.android.AndroidEntryPoint

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

        adapter = NotificationListAdapter()
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
