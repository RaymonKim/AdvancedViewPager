package m.rmk.avp

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val vp = findViewById<AdvancedViewPager>(R.id.avp)
        vp.setAdapter(MyAdapter())
        findViewById<Button>(R.id.btn1).setOnClickListener {
            vp.onClickPrev()
        }

        findViewById<Button>(R.id.btn2).setOnClickListener {
            vp.onClickNext()
        }
    }


    class FrameViewHolder(rootView: View) : RecyclerView.ViewHolder(rootView)

    inner class MyAdapter : AdvancedViewPagerAdapter() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            return FrameViewHolder(
                LayoutInflater.from(parent.context).inflate(R.layout.adad, parent, false)
            )
        }

        override fun onBindViewHolders(holder: RecyclerView.ViewHolder, position: Int) {
            (holder.itemView as TextView).text = (position + 1).toString()
            if (position % 2 == 0) {
                (holder.itemView as TextView).setBackgroundColor(Color.CYAN)
            } else {
                (holder.itemView as TextView).setBackgroundColor(Color.YELLOW)
            }
        }

        override fun getItemCounts(): Int {
            return 10
        }
    }
}
