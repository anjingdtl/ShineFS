package com.shinefs.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.height
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shinefs.app.ui.theme.ShineColors

/**
 * Cycle 00 基线壳：仅承载构建/安装/运行验证与首页信息架构预告。
 * 四大入口为静态占位（标注建设周期），不提供任何模拟交互——
 * 罗盘与卦象功能在规则引擎（YijingCore）就绪前不得伪造。
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BaselineHome()
        }
    }
}

private data class HomeEntry(val title: String, val subtitle: String, val cycle: String)

private val homeEntries = listOf(
    HomeEntry("风水罗盘", "动态罗盘 · 定盘 · 坐向", "Cycle 02-03"),
    HomeEntry("起卦解易", "方位起卦 · 时间起卦 · 数字起卦", "Cycle 04-05"),
    HomeEntry("宅居测局", "大门 · 客厅 · 主卧 · 灶位 等场景", "Cycle 06"),
    HomeEntry("卦例记录", "历史 · 收藏 · 规则版本", "Cycle 07"),
)

@Composable
fun BaselineHome() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ShineColors.BackgroundDeep)
            .padding(horizontal = 28.dp, vertical = 48.dp)
            .semantics { contentDescription = "shinefs_home_root" },
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "观方辨位 · 依易起卦",
            color = ShineColors.GoldPrimary,
            fontSize = 20.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.semantics { contentDescription = "shinefs_home_tagline" },
        )
        Text(
            text = "ShineFS 周易风水罗盘",
            color = ShineColors.TextSecondary,
            fontSize = 13.sp,
            modifier = Modifier.padding(top = 10.dp),
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 48.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            homeEntries.forEach { entry ->
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = entry.title,
                        color = ShineColors.TextPrimary,
                        fontSize = 17.sp,
                    )
                    Text(
                        text = "${entry.subtitle}　·　建设中（${entry.cycle}）",
                        color = ShineColors.TextSecondary,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 4.dp),
                    )
                    HorizontalDivider(
                        color = ShineColors.Divider,
                        modifier = Modifier
                            .padding(top = 14.dp)
                            .height(1.dp),
                    )
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 8.dp),
            verticalArrangement = Arrangement.Bottom,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "Cycle 00 项目基线 · V1.0",
                color = ShineColors.GoldMuted,
                fontSize = 11.sp,
                modifier = Modifier.semantics { contentDescription = "shinefs_home_baseline" },
            )
        }
    }
}
