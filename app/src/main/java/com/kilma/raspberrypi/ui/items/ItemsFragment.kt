package com.kilma.raspberrypi.ui.items

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.TextInputEditText
import com.kilma.raspberrypi.R
import com.kilma.raspberrypi.data.ItemsRepository
import com.kilma.raspberrypi.data.MinecraftItem

class ItemsFragment : Fragment() {

    private lateinit var viewModel: ItemsViewModel
    private lateinit var itemsAdapter: ItemsAdapter
    private lateinit var editPlayerName: TextInputEditText

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewModel = ViewModelProvider(this).get(ItemsViewModel::class.java)
        val view = inflater.inflate(R.layout.fragment_items, container, false)
        
        editPlayerName = view.findViewById(R.id.edit_player_name)
        val recyclerView: RecyclerView = view.findViewById(R.id.recycler_items)
        
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        itemsAdapter = ItemsAdapter(ItemsRepository.getPopularItems()) { item ->
            giveItemToPlayer(item)
        }
        recyclerView.adapter = itemsAdapter
        
        return view
    }
    
    private fun giveItemToPlayer(item: MinecraftItem) {
        val playerName = editPlayerName.text.toString().trim()
        if (playerName.isEmpty()) {
            Toast.makeText(requireContext(), "Please enter player name", Toast.LENGTH_SHORT).show()
            return
        }
        
        // TODO: Send RCON command: /give <player> <item> 1
        val command = "give $playerName ${item.command} 1"
        Toast.makeText(requireContext(), "Giving ${item.name} to $playerName", Toast.LENGTH_SHORT).show()
        // viewModel.sendRconCommand(command)
    }
}

class ItemsAdapter(
    private val items: List<MinecraftItem>,
    private val onGiveClick: (MinecraftItem) -> Unit
) : RecyclerView.Adapter<ItemsAdapter.ItemViewHolder>() {

    class ItemViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val icon: ImageView = view.findViewById(R.id.item_icon)
        val name: TextView = view.findViewById(R.id.item_name)
        val category: TextView = view.findViewById(R.id.item_category)
        val btnGive: Button = view.findViewById(R.id.btn_give_item)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_minecraft_item, parent, false)
        return ItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val item = items[position]
        holder.icon.setImageResource(item.iconRes)
        holder.name.text = item.name
        holder.category.text = item.category
        holder.btnGive.setOnClickListener {
            onGiveClick(item)
        }
    }

    override fun getItemCount() = items.size
}
