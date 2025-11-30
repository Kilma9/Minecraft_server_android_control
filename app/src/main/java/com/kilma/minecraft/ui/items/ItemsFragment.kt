package com.kilma.minecraft.ui.items

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.kilma.minecraft.R
import com.kilma.minecraft.data.ItemsRepository
import com.kilma.minecraft.data.MinecraftItem
import com.kilma.minecraft.rcon.RconManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.launch

class ItemsFragment : Fragment() {

    private lateinit var viewModel: ItemsViewModel
    private lateinit var editPlayerName: AutoCompleteTextView
    private lateinit var recyclerCategories: RecyclerView
    private lateinit var categoriesAdapter: CategoriesAdapter
    
    private val activePlayers = mutableListOf<String>()
    private val favoritePlayers = mutableSetOf<String>()
    private var selectedPlayer: String = ""
    private var playerAdapter: PlayerDropdownAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewModel = ViewModelProvider(this).get(ItemsViewModel::class.java)
        val view = inflater.inflate(R.layout.fragment_items, container, false)
        
        editPlayerName = view.findViewById(R.id.edit_player_name)
        recyclerCategories = view.findViewById(R.id.recycler_categories)
        
        loadFavoritePlayers()
        setupPlayerDropdown()
        setupCategoryRecycler()
        
        return view
    }
    
    private fun setupPlayerDropdown() {
        // Fetch active players from server via RCON
        lifecycleScope.launch {
            if (RconManager.isConnected()) {
                val result = RconManager.sendCommand("list")
                if (result.isSuccess) {
                    val response = result.getOrNull() ?: ""
                    // Parse "There are X of Y players online: player1, player2, ..."
                    activePlayers.clear()
                    val playersPart = response.substringAfter(":", "").trim()
                    if (playersPart.isNotEmpty()) {
                        activePlayers.addAll(playersPart.split(",").map { it.trim() })
                    }
                }
            } else {
                // Fallback to mock data if not connected
                activePlayers.clear()
                activePlayers.addAll(listOf("Steve", "Alex", "Notch", "Herobrine"))
            }
            
            updatePlayerAdapter()
        }
        
        editPlayerName.setOnItemClickListener { _, _, position, _ ->
            selectedPlayer = editPlayerName.text.toString()
        }
    }
    
    private fun updatePlayerAdapter() {
        // Sort: favorites first, then alphabetically
        val sortedPlayers = activePlayers.sortedWith(compareBy(
            { !favoritePlayers.contains(it) },
            { it }
        ))
        
        playerAdapter = PlayerDropdownAdapter(requireContext(), sortedPlayers, favoritePlayers) { player ->
            toggleFavoritePlayer(player)
        }
        editPlayerName.setAdapter(playerAdapter)
    }
    
    private fun toggleFavoritePlayer(player: String) {
        if (favoritePlayers.contains(player)) {
            favoritePlayers.remove(player)
            Toast.makeText(requireContext(), "Removed from favorites", Toast.LENGTH_SHORT).show()
        } else {
            favoritePlayers.add(player)
            Toast.makeText(requireContext(), "Added to favorites", Toast.LENGTH_SHORT).show()
        }
        
        saveFavoritePlayers()
        updatePlayerAdapter()
    }
    
    private fun setupCategoryRecycler() {
        recyclerCategories.layoutManager = LinearLayoutManager(requireContext())
        val categories = ItemsRepository.getCategories()
        categoriesAdapter = CategoriesAdapter(categories) { item ->
            giveItemToPlayer(item)
        }
        recyclerCategories.adapter = categoriesAdapter
    }
    
    private fun giveItemToPlayer(item: MinecraftItem) {
        val playerName = editPlayerName.text.toString().trim()
        if (playerName.isEmpty()) {
            Toast.makeText(requireContext(), "Please select a player", Toast.LENGTH_SHORT).show()
            return
        }
        
        if (!RconManager.isConnected()) {
            Toast.makeText(requireContext(), "Not connected to server", Toast.LENGTH_SHORT).show()
            return
        }
        
        lifecycleScope.launch {
            val command = "give $playerName ${item.command} 1"
            val result = RconManager.sendCommand(command)
            
            if (result.isSuccess) {
                val response = result.getOrNull() ?: ""
                Toast.makeText(requireContext(), "✓ ${item.name} given to $playerName", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(requireContext(), "Failed: ${result.exceptionOrNull()?.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun loadFavoritePlayers() {
        val prefs = requireContext().getSharedPreferences("items", Context.MODE_PRIVATE)
        val json = prefs.getString("favorite_players", "[]")
        val type = object : TypeToken<Set<String>>() {}.type
        val favorites: Set<String> = Gson().fromJson(json, type) ?: emptySet()
        favoritePlayers.clear()
        favoritePlayers.addAll(favorites)
    }
    
    private fun saveFavoritePlayers() {
        val prefs = requireContext().getSharedPreferences("items", Context.MODE_PRIVATE)
        val json = Gson().toJson(favoritePlayers)
        prefs.edit().putString("favorite_players", json).apply()
    }
}

class CategoriesAdapter(
    private val categories: List<String>,
    private val onItemClick: (MinecraftItem) -> Unit
) : RecyclerView.Adapter<CategoriesAdapter.CategoryViewHolder>() {

    private val expandedCategories = mutableSetOf<String>()

    class CategoryViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val categoryName: TextView = view.findViewById(R.id.category_name)
        val expandIcon: ImageView = view.findViewById(R.id.expand_icon)
        val itemsRecycler: RecyclerView = view.findViewById(R.id.category_items_recycler)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_category, parent, false)
        return CategoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        val category = categories[position]
        holder.categoryName.text = category
        
        val isExpanded = expandedCategories.contains(category)
        holder.itemsRecycler.visibility = if (isExpanded) View.VISIBLE else View.GONE
        holder.expandIcon.rotation = if (isExpanded) 180f else 0f
        
        if (isExpanded) {
            val items = ItemsRepository.getItemsByCategory(category)
            holder.itemsRecycler.layoutManager = LinearLayoutManager(holder.itemView.context)
            holder.itemsRecycler.adapter = ItemsAdapter(items, onItemClick)
        }
        
        holder.itemView.setOnClickListener {
            if (expandedCategories.contains(category)) {
                expandedCategories.remove(category)
            } else {
                expandedCategories.add(category)
            }
            notifyItemChanged(position)
        }
    }

    override fun getItemCount() = categories.size
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

// Custom adapter for player dropdown with stars
class PlayerDropdownAdapter(
    context: Context,
    private val players: List<String>,
    private val favoritePlayers: Set<String>,
    private val onStarClick: (String) -> Unit
) : ArrayAdapter<String>(context, R.layout.item_player_dropdown, players) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        return createView(position, convertView, parent)
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        return createView(position, convertView, parent)
    }

    private fun createView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context)
            .inflate(R.layout.item_player_dropdown, parent, false)
        
        val player = players[position]
        val playerNameText = view.findViewById<TextView>(R.id.player_name)
        val starIcon = view.findViewById<ImageView>(R.id.star_icon)
        
        playerNameText.text = player
        
        val isFavorite = favoritePlayers.contains(player)
        starIcon.setImageResource(
            if (isFavorite) android.R.drawable.btn_star_big_on
            else android.R.drawable.btn_star_big_off
        )
        
        starIcon.setOnClickListener {
            onStarClick(player)
        }
        
        return view
    }
}
