package march.breeze.locationbluetooth.presentation.connect

import androidx.recyclerview.widget.RecyclerView
import march.breeze.locationbluetooth.databinding.ItemPairedListBinding
import march.breeze.locationbluetooth.model.Device

class PairedListViewHolder(private val binding: ItemPairedListBinding) :
    RecyclerView.ViewHolder(binding.root) {

    fun onBind(item: Device) {
        binding.tvPairedItem.text = item.name
    }
}