package com.shinefs.app.ui.nav

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.shinefs.app.data.LockedReading
import kotlinx.coroutines.flow.MutableStateFlow

/** 轻量路由（避免引入导航库依赖）：显式返回栈 + 系统返回键接管。 */
sealed interface Dest {
    data object Home : Dest
    data object HouseAudit : Dest

    /** houseAuditId/sceneId 非空时表示从宅居测局进入（场景已预选，定盘后直达起卦）。 */
    data class Compass(val houseAuditId: String? = null, val sceneId: String? = null) : Dest
    data object CastModes : Dest
    data class SceneSelect(
        val reading: LockedReading,
        val houseAuditId: String? = null,
        val preselectedSceneId: String? = null,
    ) : Dest
    data class Reveal(val caseId: String) : Dest
    data class Interpretation(val caseId: String) : Dest
}

class Router(initial: List<Dest> = listOf(Dest.Home)) {

    private val _stack = MutableStateFlow(initial)

    val current: Dest
        get() = _stack.value.last()

    fun push(dest: Dest) {
        _stack.value = _stack.value + dest
    }

    /** 以新目的地替换栈顶（用于"过路目的地"，避免返回时重复触发副作用）。 */
    fun replace(dest: Dest) {
        _stack.value = if (_stack.value.size > 1) {
            _stack.value.dropLast(1) + dest
        } else {
            listOf(dest)
        }
    }

    fun pop() {
        if (_stack.value.size > 1) {
            _stack.value = _stack.value.dropLast(1)
        }
    }

    fun popToRoot() {
        _stack.value = listOf(Dest.Home)
    }

    @Composable
    fun currentAsState(): Dest {
        val stack by _stack.collectAsState()
        return stack.last()
    }

    @Composable
    fun HandleBack() {
        val stack by _stack.collectAsState()
        BackHandler(enabled = stack.size > 1) { pop() }
    }
}
