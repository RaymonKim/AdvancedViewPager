package m.rmk.avp

import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.NO_ID

abstract class AdvancedViewPagerAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private var useInfiniteScroll = false

    abstract fun getItemCounts(): Int

    abstract fun onBindViewHolders(holder: RecyclerView.ViewHolder, position: Int)

    final override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        onBindViewHolders(
            holder, if (useInfiniteScroll) {
                position % AMPLIFIER
            } else {
                position
            }
        )
    }

    final override fun getItemCount(): Int {
        return if (useInfiniteScroll) {
            getItemCounts() * AMPLIFIER
        } else {
            getItemCounts()
        }
    }

    final override fun getItemViewType(position: Int): Int {
        return getItemViewTypes(position)
    }

    final override fun getItemId(position: Int): Long {
        return getItemIds(position)
    }

    open fun getItemViewTypes(position: Int): Int {
        return 0
    }

    open fun getItemIds(position: Int): Long {
        return NO_ID
    }

    fun setUseInfiniteScroll(useInfiniteScroll: Boolean) {
        this.useInfiniteScroll = useInfiniteScroll
    }
}