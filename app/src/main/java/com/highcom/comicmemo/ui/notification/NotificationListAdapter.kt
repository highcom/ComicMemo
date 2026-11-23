package com.highcom.comicmemo.ui.notification

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.net.toUri
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.highcom.comicmemo.R
import com.highcom.comicmemo.databinding.NotificationItemBinding
import com.highcom.comicmemo.datamodel.BookNotification
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * お知らせ通知一覧表示用のアダプター
 *
 * @property onItemLongClickListener アイテム長押し時イベントリスナー
 */
class NotificationListAdapter(
    private val onItemLongClickListener: ((BookNotification) -> Unit)? = null
) : ListAdapter<BookNotification, NotificationListAdapter.NotificationViewHolder>(
    DiffCallback
) {
    companion object DiffCallback : DiffUtil.ItemCallback<BookNotification>() {
        override fun areItemsTheSame(oldItem: BookNotification, newItem: BookNotification): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: BookNotification, newItem: BookNotification): Boolean {
            return oldItem == newItem
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationViewHolder {
        val binding = NotificationItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return NotificationViewHolder(binding)
    }

    override fun onBindViewHolder(holder: NotificationViewHolder, position: Int) {
        holder.bind(getItem(position), onItemLongClickListener)
    }

    class NotificationViewHolder(val binding: NotificationItemBinding) : RecyclerView.ViewHolder(binding.root) {
        var notificationItem: BookNotification? = null

        fun bind(item: BookNotification, onItemLongClickListener: ((BookNotification) -> Unit)? = null) {
            notificationItem = item
            binding.notificationTitleView.text = item.title

            binding.notificationContentLayout.setOnLongClickListener {
                onItemLongClickListener?.invoke(item)
                true
            }

            val authorPublisher = when {
                item.author.isNotEmpty() && item.publisher.isNotEmpty() -> "${item.author} / ${item.publisher}"
                item.author.isNotEmpty() -> item.author
                item.publisher.isNotEmpty() -> item.publisher
                else -> ""
            }
            binding.notificationAuthorView.text = authorPublisher

            val sdf = SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.JAPAN)
            val dateTimeStr = sdf.format(Date(item.notifyDateTime))
            val context = binding.root.context
            binding.notificationDateTimeView.text = context.getString(R.string.notification_time_format, dateTimeStr)

            if (item.imageUrl.isNotEmpty()) {
                val imgUri = item.imageUrl.toUri().buildUpon().scheme("https").build()
                Glide.with(binding.notificationImageView.context)
                    .load(imgUri)
                    .apply(
                        RequestOptions()
                            .placeholder(R.drawable.loading_animation)
                            .error(R.drawable.ic_baseline_broken_image_24)
                    )
                    .into(binding.notificationImageView)
            } else {
                binding.notificationImageView.setImageResource(R.drawable.ic_baseline_broken_image_24)
            }
        }
    }
}
