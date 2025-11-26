package com.kilma.raspberrypi.ui.minecraft

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.kilma.raspberrypi.databinding.FragmentMinecraftBinding

class MinecraftFragment : Fragment() {

    private var _binding: FragmentMinecraftBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: MinecraftViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewModel = ViewModelProvider(this).get(MinecraftViewModel::class.java)

        _binding = FragmentMinecraftBinding.inflate(inflater, container, false)
        val root: View = binding.root

        setupObservers()
        setupClickListeners()

        return root
    }

    private fun setupObservers() {
        viewModel.text.observe(viewLifecycleOwner) {
            binding.textMinecraft.text = it
        }

        viewModel.statusText.observe(viewLifecycleOwner) {
            binding.tvStatus.text = it
        }
    }

    private fun setupClickListeners() {
        binding.btnConnect.setOnClickListener {
            val ip = binding.etServerIp.text.toString()
            val port = binding.etRconPort.text.toString()
            val password = binding.etRconPassword.text.toString()
            viewModel.connect(ip, port, password)
        }

        binding.btnSetCreative.setOnClickListener {
            val playerName = binding.etPlayerName.text.toString()
            viewModel.setPlayerGameMode(playerName, "creative")
        }

        binding.btnSetSurvival.setOnClickListener {
            val playerName = binding.etPlayerName.text.toString()
            viewModel.setPlayerGameMode(playerName, "survival")
        }

        binding.btnGiveItem.setOnClickListener {
            val playerName = binding.etPlayerName.text.toString()
            val itemName = binding.etItemName.text.toString()
            val quantity = binding.etItemQuantity.text.toString()
            viewModel.giveItem(playerName, itemName, quantity)
        }

        // Quick item buttons
        binding.btnItemDiamondSword.setOnClickListener {
            giveQuickItem("diamond_sword", "1")
        }

        binding.btnItemIronPickaxe.setOnClickListener {
            giveQuickItem("iron_pickaxe", "1")
        }

        binding.btnItemGoldenApple.setOnClickListener {
            giveQuickItem("golden_apple", "16")
        }

        binding.btnItemEnderPearl.setOnClickListener {
            giveQuickItem("ender_pearl", "16")
        }

        binding.btnItemDiamond.setOnClickListener {
            giveQuickItem("diamond", "64")
        }

        binding.btnItemEmerald.setOnClickListener {
            giveQuickItem("emerald", "64")
        }

        binding.btnItemTnt.setOnClickListener {
            giveQuickItem("tnt", "64")
        }

        binding.btnItemElytra.setOnClickListener {
            giveQuickItem("elytra", "1")
        }

        binding.btnItemEnchantedBook.setOnClickListener {
            giveQuickItem("enchanted_book", "1")
        }

        binding.btnItemTotem.setOnClickListener {
            giveQuickItem("totem_of_undying", "1")
        }

        binding.btnItemIronIngot.setOnClickListener {
            giveQuickItem("iron_ingot", "64")
        }

        binding.btnItemObsidian.setOnClickListener {
            giveQuickItem("obsidian", "64")
        }
    }

    private fun giveQuickItem(itemName: String, quantity: String) {
        val playerName = binding.etPlayerName.text.toString()
        viewModel.giveItem(playerName, itemName, quantity)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
