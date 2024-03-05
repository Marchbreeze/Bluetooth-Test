package march.breeze.locationbluetooth.presentation.connect

import androidx.recyclerview.widget.RecyclerView
import march.breeze.locationbluetooth.databinding.ItemSearchListBinding
import march.breeze.locationbluetooth.model.Device
import march.breeze.locationbluetooth.util.extension.setOnSingleClickListener

class SearchListViewHolder(
    private val binding: ItemSearchListBinding,
    private val onClicked: (String) -> (Unit)
) :
    RecyclerView.ViewHolder(binding.root) {

    fun onBind(item: Device) {
        binding.tvSearchItem.text = item.name
        binding.root.setOnSingleClickListener {
            onClicked(item.address)
        }
    }
}