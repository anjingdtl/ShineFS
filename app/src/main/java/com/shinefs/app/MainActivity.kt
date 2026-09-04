package com.shinefs.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import com.shinefs.app.data.Scenes
import com.shinefs.app.ui.compass.CompassScreen
import com.shinefs.app.ui.divination.HexagramRevealScreen
import com.shinefs.app.ui.divination.SceneSelectScreen
import com.shinefs.app.ui.divination.TimeCastScreen
import com.shinefs.app.ui.history.HistoryScreen
import com.shinefs.app.ui.home.HomeScreen
import com.shinefs.app.ui.nav.Dest
import com.shinefs.app.ui.nav.Router
import com.shinefs.app.ui.rules.CorpusDetailScreen
import com.shinefs.app.ui.rules.CorpusListScreen
import com.shinefs.app.ui.rules.RulesScreen
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppGraph.init(this)
        setContent {
            ShineApp()
        }
    }
}

@Composable
fun ShineApp() {
    val router = remember { Router() }
    val scope = androidx.compose.runtime.rememberCoroutineScope()
    router.HandleBack()
    when (val dest = router.currentAsState()) {
        Dest.Home -> HomeScreen(
            onOpenSpaceTimeCast = { router.push(Dest.Compass()) },
            onOpenCompass = { router.push(Dest.Compass()) },
            onOpenTimeCast = { router.push(Dest.TimeCast) },
            onOpenHouseAudit = { router.push(Dest.HouseAudit) },
            onOpenHistory = { router.push(Dest.History) },
            onOpenRules = { router.push(Dest.Rules) },
        )
        Dest.HouseAudit -> com.shinefs.app.ui.house.HouseAuditScreen(
            casesProvider = {
                withContext(Dispatchers.IO) { AppGraph.caseRepository.all() }
            },
            onBack = { router.pop() },
            onMeasureScene = { auditId, sceneId ->
                router.push(Dest.Compass(houseAuditId = auditId, sceneId = sceneId))
            },
            onOpenCase = { router.push(Dest.Interpretation(it)) },
        )
        Dest.History -> HistoryScreen(
            casesProvider = {
                withContext(Dispatchers.IO) { AppGraph.caseRepository.all() }
            },
            onBack = { router.pop() },
            onOpenCase = { router.push(Dest.Interpretation(it)) },
        )
        Dest.TimeCast -> TimeCastScreen(
            onBack = { router.pop() },
            onCasted = { caseId ->
                router.replace(Dest.Reveal(caseId))
            },
        )
        Dest.Rules -> RulesScreen(
            onBack = { router.pop() },
            onOpenCorpus = { router.push(Dest.CorpusList) },
        )
        Dest.CorpusList -> CorpusListScreen(
            onBack = { router.pop() },
            onOpenDetail = { router.push(Dest.CorpusDetail(it)) },
        )
        is Dest.CorpusDetail -> CorpusDetailScreen(
            kingWenOrder = dest.kingWenOrder,
            onBack = { router.pop() },
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
        is Dest.SceneSelect -> {
            val preselected = dest.preselectedSceneId
            if (preselected != null) {
                LaunchedEffect(dest) {
                    val case = withContext(Dispatchers.IO) {
                        AppGraph.divinationService.castTimeSpace(
                            reading = dest.reading,
                            scene = Scenes.byId(preselected),
                            houseAuditId = dest.houseAuditId,
                        )
                    }
                    router.replace(Dest.Reveal(case.id))
                }
            } else {
                SceneSelectScreen(
                    reading = dest.reading,
                    onBack = { router.pop() },
                    onSelect = { sceneId ->
                        scope.launch(Dispatchers.IO) {
                            val case = AppGraph.divinationService.castTimeSpace(
                                reading = dest.reading,
                                scene = Scenes.byId(sceneId),
                                houseAuditId = dest.houseAuditId,
                            )
                            router.push(Dest.Reveal(case.id))
                        }
                    },
                )
            }
        }
        is Dest.Reveal -> HexagramRevealScreen(
            caseId = dest.caseId,
            caseLoader = { AppGraph.caseRepository.byId(it) },
            onBackToHome = { router.popToRoot() },
            onOpenInterpretation = { router.push(Dest.Interpretation(it)) },
        )
        is Dest.Interpretation -> com.shinefs.app.ui.interpretation.InterpretationScreen(
            caseId = dest.caseId,
            caseLoader = { AppGraph.caseRepository.byId(it) },
            recompute = { AppGraph.divinationService.recomputeTrace(it) },
            onBack = { router.pop() },
            onUpdateCase = { updated ->
                scope.launch(Dispatchers.IO) { AppGraph.caseRepository.update(updated) }
            },
            onDeleteCase = { id ->
                withContext(Dispatchers.IO) { AppGraph.caseRepository.delete(id) }
            },
        )
    }
}
