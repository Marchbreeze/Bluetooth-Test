package march.breeze.locationbluetooth.presentation.connect

import androidx.recyclerview.widget.RecyclerView
import march.breeze.locationbluetooth.databinding.ItemSearchListBinding
import march.breeze.locationbluetooth.model.Device

class SearchListViewHolder(private val binding: ItemSearchListBinding) :
    RecyclerView.ViewHolder(binding.root) {

    fun onBind(item: Device) {
        binding.tvSearchItem.text = item.name
    }
}