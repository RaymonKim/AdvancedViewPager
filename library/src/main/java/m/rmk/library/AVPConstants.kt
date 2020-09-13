package m.rmk.library

import androidx.annotation.IntDef

const val MSG_AUTO_SCROLL = 0

const val DEFAULT_INTERVAL = 3000
const val DEFAULT_SPEED = 500

const val AMPLIFIER = 100000

@Target(
    AnnotationTarget.FIELD,
    AnnotationTarget.TYPE_PARAMETER,
    AnnotationTarget.VALUE_PARAMETER
)
@Retention(AnnotationRetention.SOURCE)
@IntDef
internal annotation class RollingMode {
    companion object {
        const val NONE = 0
        const val START_TO_END = 1
        const val END_TO_START = 2
    }
}