package march.breeze.locationbluetooth.presentation.connect

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import march.breeze.locationbluetooth.databinding.ItemPairedListBinding
import march.breeze.locationbluetooth.model.Device
import march.breeze.locationbluetooth.util.extension.ItemDiffCallback

class PairedListAdapter : ListAdapter<Device, PairedListViewHolder>(diffUtil) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PairedListViewHolder {
        val inflater by lazy { LayoutInflater.from(parent.context) }
        val binding: ItemPairedListBinding = ItemPairedListBinding.inflate(inflater, parent, false)
        return PairedListViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PairedListViewHolder, position: Int) {
        holder.onBind(getItem(position))
    }

    fun addList(newItems: List<Device>) {
        val currentItems = currentList.toMutableList()
        currentItems.addAll(newItems)
        submitList(currentItems)
    }

    companion object {
        private val diffUtil = ItemDiffCallback<Device>(
            onItemsTheSame = { old, new -> old.address == new.address },
            onContentsTheSame = { old, new -> old == new },
        )
    }
}