package m.rmk.avp

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.viewpager2.widget.ViewPager2
import org.jetbrains.annotations.NotNull
import java.lang.ref.WeakReference

class AdvancedViewPager @JvmOverloads constructor(
    context: Context,
    private val attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val viewPager2: ViewPager2 = ViewPager2(context, attrs, defStyleAttr)
    private val dragAnimator: ValueAnimator
    private val actionHandler: ActionHandler

    @RollingMode
    private var rollingMode: Int = RollingMode.NONE
    private var rollingInterval: Long = DEFAULT_INTERVAL.toLong()
    private var rollingSpeed: Long = DEFAULT_SPEED.toLong()
    private var useInfiniteScroll: Boolean = false

    private var rollingStatusChangeListener: RollingStatusChangeListener? = null

    init {
        initAttributes(attrs)

        dragAnimator = getDragAnimator()
        actionHandler = ActionHandler(viewPager2, dragAnimator)

        viewPager2.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                startRolling()
            }
        })

        addView(
            viewPager2,
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
    }

    private fun initAttributes(attrs: AttributeSet?) {
        attrs?.let {
            val a = context.obtainStyledAttributes(it, R.styleable.AdvancedViewPager)

            useInfiniteScroll =
                a.getBoolean(R.styleable.AdvancedViewPager_avp_useInfiniteScroll, false)
            rollingMode =
                a.getInt(R.styleable.AdvancedViewPager_avp_rollingMode, RollingMode.NONE)
            rollingInterval =
                a.getInt(R.styleable.AdvancedViewPager_avp_rollingInterval, DEFAULT_INTERVAL)
                    .toLong()
            rollingSpeed =
                a.getInt(R.styleable.AdvancedViewPager_avp_rollingInterval, DEFAULT_SPEED)
                    .toLong()
            a.recycle()
        }
    }

    private fun getDragAnimator(): ValueAnimator {
        return ValueAnimator.ofFloat(
            0f,
            if (viewPager2.orientation == ViewPager2.ORIENTATION_HORIZONTAL) viewPager2.width.toFloat() else viewPager2.height.toFloat()
        ).apply {
            addUpdateListener(object : ValueAnimator.AnimatorUpdateListener {
                var prevValue = 0f

                override fun onAnimationUpdate(p0: ValueAnimator?) {
                    val fraction = p0?.animatedFraction as Float

                    val currentItem = if (useInfiniteScroll) {
                        viewPager2.currentItem % AMPLIFIER
                    } else {
                        viewPager2.currentItem
                    }

                    if (fraction == 0f) {
                        prevValue =
                            if (viewPager2.orientation == ViewPager2.ORIENTATION_HORIZONTAL) {
                                (viewPager2.width * currentItem).toFloat()
                            } else {
                                (viewPager2.height * currentItem).toFloat()
                            }
                    }

                    if (fraction in 0.0..1.0) {
                        val delta =
                            if (viewPager2.orientation == ViewPager2.ORIENTATION_HORIZONTAL) {
                                (viewPager2.width * fraction) + (viewPager2.width * currentItem)
                            } else {
                                (viewPager2.height * fraction) + (viewPager2.height * currentItem)
                            }

                        val offset = delta - prevValue

                        if (rollingMode == RollingMode.START_TO_END) {
                            viewPager2.fakeDragBy(-offset)
                        } else {
                            viewPager2.fakeDragBy(offset)
                        }

                        prevValue = delta
                    }
                }
            })

            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationStart(p0: Animator?) {
                    viewPager2.beginFakeDrag()
                }

                override fun onAnimationEnd(p0: Animator?) {
                    viewPager2.endFakeDrag()
                }

                override fun onAnimationCancel(p0: Animator?) {
                    viewPager2.endFakeDrag()
                }
            })

            this.duration = rollingSpeed
        }
    }

    fun <T : AdvancedViewPagerAdapter> setAdapter(adapter: T?) {
        if (adapter != null) {
            (adapter as AdvancedViewPagerAdapter).setUseInfiniteScroll(useInfiniteScroll)
            viewPager2.adapter = adapter
            if (useInfiniteScroll) {
                viewPager2.setCurrentItem(AMPLIFIER / 2, false)
            } else {
                viewPager2.setCurrentItem(0, false)
            }
        } else {
            viewPager2.adapter = null
        }
    }

    fun onStartRolling() {
        attrs?.let {
            val a = context.obtainStyledAttributes(it, R.styleable.AdvancedViewPager)
            rollingMode =
                a.getInt(R.styleable.AdvancedViewPager_avp_rollingMode, RollingMode.NONE)
            a.recycle()
        } ?: apply {
            rollingMode = RollingMode.NONE
        }
        startRolling()
        rollingStatusChangeListener?.onStartRolling()
    }

    fun onStopRolling() {
        rollingMode = RollingMode.NONE
        stopRolling()
        rollingStatusChangeListener?.onStopRolling()
    }

    fun onClickPrev() {
        if (rollingMode != RollingMode.END_TO_START) {
            if (viewPager2.currentItem > 0) {
                stopRolling()
                viewPager2.currentItem = viewPager2.currentItem - 1
            }
        } else {
            viewPager2.adapter?.let { adapter ->
                if (viewPager2.currentItem < adapter.itemCount - 1) {
                    stopRolling()
                    viewPager2.currentItem = viewPager2.currentItem + 1
                }
            }
        }
    }

    fun onClickNext() {
        if (rollingMode != RollingMode.END_TO_START) {
            viewPager2.adapter?.let { adapter ->
                if (viewPager2.currentItem < adapter.itemCount - 1) {
                    stopRolling()
                    viewPager2.currentItem = viewPager2.currentItem + 1
                }
            }
        } else {
            if (viewPager2.currentItem > 0) {
                stopRolling()
                viewPager2.currentItem = viewPager2.currentItem - 1
            }
        }
    }

    fun setRollingStatusChangeListener(listener: RollingStatusChangeListener) {
        rollingStatusChangeListener = listener
    }

    fun registerOnPageChangeCallback(@NotNull callback: ViewPager2.OnPageChangeCallback) {
        viewPager2.registerOnPageChangeCallback(callback)
    }

    private fun startRolling(): Boolean {
        return if (rollingMode != RollingMode.NONE && isRollingPossible()) {
            stopRolling()
            actionHandler.sendEmptyMessageDelayed(MSG_AUTO_SCROLL, rollingInterval)
            true
        } else {
            false
        }
    }

    private fun stopRolling() {
        dragAnimator.cancel()
        actionHandler.removeMessages(MSG_AUTO_SCROLL)
    }

    private fun isRollingPossible(): Boolean {
        return viewPager2.adapter?.let { adapter ->
            return if (rollingMode == RollingMode.START_TO_END) {
                viewPager2.currentItem < adapter.itemCount - 1
            } else {
                viewPager2.currentItem > 0
            }
        } ?: false
    }

    override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {
        when (ev?.actionMasked) {
            MotionEvent.ACTION_DOWN -> stopRolling()
            MotionEvent.ACTION_UP -> startRolling()
        }

        return super.onInterceptTouchEvent(ev)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        startRolling()
    }

    override fun onDetachedFromWindow() {
        stopRolling()
        super.onDetachedFromWindow()
    }

    private class ActionHandler(viewPager2: ViewPager2, val anim: ValueAnimator) :
        Handler(Looper.getMainLooper()) {
        val weakVp = WeakReference<ViewPager2>(viewPager2)

        override fun handleMessage(msg: Message) {
            weakVp.get()?.post { anim.start() }
        }
    }

    interface RollingStatusChangeListener {
        fun onStartRolling()

        fun onStopRolling()
    }
}