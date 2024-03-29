package march.breeze.locationbluetooth.presentation.connect

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import march.breeze.locationbluetooth.databinding.ItemSearchListBinding
import march.breeze.locationbluetooth.model.Device
import march.breeze.locationbluetooth.util.extension.ItemDiffCallback

class SearchListAdapter(
    private val onClicked: (String) -> (Unit)
) : ListAdapter<Device, SearchListViewHolder>(diffUtil) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchListViewHolder {
        val inflater by lazy { LayoutInflater.from(parent.context) }
        val binding: ItemSearchListBinding = ItemSearchListBinding.inflate(inflater, parent, false)
        return SearchListViewHolder(binding, onClicked)
    }

    override fun onBindViewHolder(holder: SearchListViewHolder, position: Int) {
        holder.onBind(getItem(position))
    }

    companion object {
        private val diffUtil = ItemDiffCallback<Device>(
            onItemsTheSame = { old, new -> old.address == new.address },
            onContentsTheSame = { old, new -> old == new },
        )
    }
}