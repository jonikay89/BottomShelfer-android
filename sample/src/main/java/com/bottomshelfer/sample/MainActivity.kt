package com.bottomshelfer.sample

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bottomshelfer.sample.databinding.ActivityMainBinding
import com.bottomshelfer.sample.databinding.ItemDemoBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        title = "BottomShelfer"

        val demos = Demos.create(this)

        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = DemosAdapter(demos)
    }
}

internal class DemosAdapter(private val demos: List<Demo>) :
    RecyclerView.Adapter<DemosAdapter.VH>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val inflater = parent.context.getSystemService(android.content.Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val binding = ItemDemoBinding.inflate(inflater, parent, false)
        return VH(binding)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val d = demos[position]
        holder.binding.titleText.text = d.title
        holder.binding.subtitleText.text = d.subtitle
        holder.itemView.setOnClickListener { d.action() }
    }

    override fun getItemCount() = demos.size

    class VH(val binding: ItemDemoBinding) : RecyclerView.ViewHolder(binding.root)
}
