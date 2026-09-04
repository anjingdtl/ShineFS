package com.shinefs.app.ui.assets

import com.shinefs.core.yijing.model.Trigram
import org.junit.Assert.assertEquals
import org.junit.Test

class BaguaAssetTest {

    @Test
    fun brandAssetUsesFirstHeavenOrderFromTopClockwise() {
        assertEquals(
            listOf(
                Trigram.QIAN,
                Trigram.DUI,
                Trigram.LI,
                Trigram.ZHEN,
                Trigram.KUN,
                Trigram.XUN,
                Trigram.KAN,
                Trigram.GEN,
            ),
            BaguaAsset.xiantianBrandOrder,
        )
    }

    @Test
    fun compassRingKeepsPostnatalOrderFromNorthClockwise() {
        assertEquals(
            listOf(
                Trigram.KAN,
                Trigram.GEN,
                Trigram.ZHEN,
                Trigram.XUN,
                Trigram.LI,
                Trigram.KUN,
                Trigram.DUI,
                Trigram.QIAN,
            ),
            BaguaAsset.postnatalOrder,
        )
    }
}
