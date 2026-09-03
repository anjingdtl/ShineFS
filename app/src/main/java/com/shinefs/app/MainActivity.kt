package com.shinefs.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.shinefs.app.ui.compass.CompassScreen
import com.shinefs.app.ui.divination.CastModesScreen
import com.shinefs.app.ui.divination.HexagramRevealScreen
import com.shinefs.app.ui.divination.SceneSelectScreen
import com.shinefs.app.ui.home.HomeScreen
import com.shinefs.app.ui.nav.Dest
import com.shinefs.app.ui.nav.Router

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ShineApp()
        }
    }
}

@Composable
fun ShineApp() {
    val router = remember { Router() }
    router.HandleBack()
    when (val dest = router.currentAsState()) {
        Dest.Home -> HomeScreen(
            onOpenCompass = { router.push(Dest.Compass) },
            onOpenCastModes = { router.push(Dest.CastModes) },
        )
        Dest.Compass -> CompassScreen(
            onBack = { router.pop() },
            onCast = { router.push(Dest.SceneSelect(it)) },
        )
        Dest.CastModes -> CastModesScreen(
            onBack = { router.pop() },
            onOpenCompass = {
                router.pop()
                router.push(Dest.Compass)
            },
        )
        is Dest.SceneSelect -> SceneSelectScreen(
            reading = dest.reading,
            onBack = { router.pop() },
            onSelect = { sceneId ->
                val case = AppGraph.divinationService.castWithDirection(
                    reading = dest.reading,
                    scene = com.shinefs.app.data.Scenes.byId(sceneId),
                    houseAuditId = dest.houseAuditId,
                )
                router.push(Dest.Reveal(case.id))
            },
        )
        is Dest.Reveal -> HexagramRevealScreen(
            caseId = dest.caseId,
            caseLoader = { AppGraph.caseRepository.byId(it) },
            ruleExplain = AppGraph.divinationService.ruleExplain(),
            onBackToHome = { router.popToRoot() },
            onOpenInterpretation = { router.push(Dest.Interpretation(it)) },
        )
        is Dest.Interpretation -> InterpretationPlaceholder(caseId = dest.caseId, onBack = { router.pop() })
    }
}

/** Cycle 05 前的解读页占位（结构性八段在下一周期落地）。 */
@Composable
private fun InterpretationPlaceholder(caseId: String, onBack: () -> Unit) {
    com.shinefs.app.ui.compass.ScreenHeader(title = "解卦 · 建设中", onBack = onBack)
}
