package com.bottomshelfer.sample

import android.content.Context
import android.graphics.Rect
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bottomshelfer.sample.databinding.ActivityMainBinding
import com.bottomshelfer.sample.databinding.ItemDemoBinding
import com.bottomshelfer.sample.databinding.ItemSectionHeaderBinding

data class Section(
    val header: String,
    val items: List<Demo>
)

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        title = "BottomShelfer"

        val demos = Demos.create(this)
        val sections = listOf(
            Section("", demos),
        )

        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = SectionedAdapter(this, sections)
        binding.recyclerView.addItemDecoration(SectionSpacingDecoration())
    }
}

internal class SectionedAdapter(
    private val context: Context,
    private val sections: List<Section>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        const val VIEW_TYPE_HEADER = 0
        const val VIEW_TYPE_ITEM = 1
        const val VIEW_TYPE_ITEM_LAST = 2
        const val VIEW_TYPE_ITEM_SINGLE = 3
    }

    private val flatItems = buildList {
        for (section in sections) {
            if (section.header.isNotEmpty()) {
                add(AdapterItem.Header(section.header))
            }
            val items = section.items
            if (items.size == 1) {
                add(AdapterItem.DemoItem(items[0], AdapterItem.Position.SINGLE))
            } else {
                for (i in items.indices) {
                    val pos = when (i) {
                        0 -> AdapterItem.Position.FIRST
                        items.lastIndex -> AdapterItem.Position.LAST
                        else -> AdapterItem.Position.MIDDLE
                    }
                    add(AdapterItem.DemoItem(items[i], pos))
                }
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (val item = flatItems[position]) {
            is AdapterItem.Header -> VIEW_TYPE_HEADER
            is AdapterItem.DemoItem -> when (item.position) {
                AdapterItem.Position.LAST -> VIEW_TYPE_ITEM_LAST
                AdapterItem.Position.SINGLE -> VIEW_TYPE_ITEM_SINGLE
                else -> VIEW_TYPE_ITEM
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        return when (viewType) {
            VIEW_TYPE_HEADER -> {
                val binding = ItemSectionHeaderBinding.inflate(inflater, parent, false)
                HeaderVH(binding)
            }
            else -> {
                val binding = ItemDemoBinding.inflate(inflater, parent, false)
                DemoVH(binding)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = flatItems[position]) {
            is AdapterItem.Header -> {
                (holder as HeaderVH).bind(item.text)
            }
            is AdapterItem.DemoItem -> {
                (holder as DemoVH).bind(item.demo, item.position)
            }
        }
    }

    override fun getItemCount() = flatItems.size

    class HeaderVH(private val binding: ItemSectionHeaderBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(text: String) {
            binding.headerText.text = text
        }
    }

    class DemoVH(private val binding: ItemDemoBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(demo: Demo, position: AdapterItem.Position) {
            binding.titleText.text = demo.title
            binding.subtitleText.text = demo.subtitle
            itemView.setOnClickListener { demo.action() }

            val bgRes = when (position) {
                AdapterItem.Position.FIRST -> R.drawable.bg_cell_top
                AdapterItem.Position.MIDDLE -> R.drawable.bg_cell_middle
                AdapterItem.Position.LAST -> R.drawable.bg_cell_bottom
                AdapterItem.Position.SINGLE -> R.drawable.bg_cell_single
            }
            itemView.background = itemView.context.getDrawable(bgRes)

            binding.divider.visibility = if (position == AdapterItem.Position.LAST || position == AdapterItem.Position.SINGLE) {
                View.GONE
            } else {
                View.VISIBLE
            }
        }
    }
}

internal class SectionSpacingDecoration : RecyclerView.ItemDecoration() {

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        val adapter = parent.adapter as? SectionedAdapter ?: return
        val position = parent.getChildAdapterPosition(view)
        if (position == RecyclerView.NO_POSITION) return

        val viewType = adapter.getItemViewType(position)

        if (viewType == SectionedAdapter.VIEW_TYPE_ITEM_LAST || viewType == SectionedAdapter.VIEW_TYPE_ITEM_SINGLE) {
            outRect.bottom = 24
        }
    }
}

sealed class AdapterItem {
    data class Header(val text: String) : AdapterItem()
    data class DemoItem(val demo: Demo, val position: Position) : AdapterItem()

    enum class Position { FIRST, MIDDLE, LAST, SINGLE }
}
