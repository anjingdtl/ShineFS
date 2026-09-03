package com.shinefs.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
            onOpenCompass = { router.push(Dest.Compass()) },
            onOpenCastModes = { router.push(Dest.CastModes) },
            onOpenHouseAudit = { router.push(Dest.HouseAudit) },
        )
        Dest.HouseAudit -> com.shinefs.app.ui.house.HouseAuditScreen(
            casesProvider = { AppGraph.caseRepository.all() },
            onBack = { router.pop() },
            onMeasureScene = { auditId, sceneId ->
                router.push(Dest.Compass(houseAuditId = auditId, sceneId = sceneId))
            },
            onOpenCase = { router.push(Dest.Interpretation(it)) },
        )
        is Dest.Compass -> CompassScreen(
            onBack = { router.pop() },
            onCast = { reading ->
                router.push(
                    Dest.SceneSelect(
                        reading = reading,
                        houseAuditId = dest.houseAuditId,
                        preselectedSceneId = dest.sceneId,
                    ),
                )
            },
        )
        Dest.CastModes -> CastModesScreen(
            onBack = { router.pop() },
            onOpenCompass = {
                router.pop()
                router.push(Dest.Compass())
            },
        )
        is Dest.SceneSelect -> {
            val preselected = dest.preselectedSceneId
            if (preselected != null) {
                LaunchedEffect(dest) {
                    val case = AppGraph.divinationService.castWithDirection(
                        reading = dest.reading,
                        scene = com.shinefs.app.data.Scenes.byId(preselected),
                        houseAuditId = dest.houseAuditId,
                    )
                    router.replace(Dest.Reveal(case.id))
                }
            } else {
                SceneSelectScreen(
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
            }
        }
        is Dest.Reveal -> HexagramRevealScreen(
            caseId = dest.caseId,
            caseLoader = { AppGraph.caseRepository.byId(it) },
            ruleExplain = AppGraph.divinationService.ruleExplain(),
            onBackToHome = { router.popToRoot() },
            onOpenInterpretation = { router.push(Dest.Interpretation(it)) },
        )
        is Dest.Interpretation -> com.shinefs.app.ui.interpretation.InterpretationScreen(
            caseId = dest.caseId,
            caseLoader = { AppGraph.caseRepository.byId(it) },
            classicTexts = AppGraph.classicTexts,
            interpreter = AppGraph.aiInterpreter,
            interpreter2 = AppGraph.ruleInterpreter,
            onBack = { router.pop() },
        )
    }
}
