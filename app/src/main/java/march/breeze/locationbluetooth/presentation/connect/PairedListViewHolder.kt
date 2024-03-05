package march.breeze.locationbluetooth.presentation.connect

import androidx.recyclerview.widget.RecyclerView
import march.breeze.locationbluetooth.databinding.ItemPairedListBinding
import march.breeze.locationbluetooth.model.Device
import march.breeze.locationbluetooth.util.extension.setOnSingleClickListener

class PairedListViewHolder(
    private val binding: ItemPairedListBinding,
    private val onClicked: (String) -> (Unit)
) :
    RecyclerView.ViewHolder(binding.root) {

    fun onBind(item: Device) {
        binding.tvPairedItem.text = item.name
        binding.root.setOnSingleClickListener {
            onClicked(item.address)
        }
    }
}