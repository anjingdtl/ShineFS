Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

Add-Type -AssemblyName System.Drawing

$projectRoot = Split-Path -Parent $PSScriptRoot
$background = [System.Drawing.ColorTranslator]::FromHtml("#0C0C0F")
$outline = [System.Drawing.ColorTranslator]::FromHtml("#B8955A")
$gold = [System.Drawing.ColorTranslator]::FromHtml("#D9BC7A")
$muted = [System.Drawing.ColorTranslator]::FromHtml("#7E6A44")
$ink = [System.Drawing.ColorTranslator]::FromHtml("#15161A")
$ivory = [System.Drawing.ColorTranslator]::FromHtml("#E8E2D4")

# Each row is stored bottom-to-top, matching Trigram.lines in core:yijing.
# Brand orientation: from the top, clockwise 乾、兑、离、震、坤、巽、坎、艮.
$brandLines = @(
    @(1, 1, 1), # 乾 / top
    @(1, 1, 0), # 兑 / upper right
    @(1, 0, 1), # 离 / right
    @(1, 0, 0), # 震 / lower right
    @(0, 0, 0), # 坤 / bottom
    @(0, 1, 1), # 巽 / lower left
    @(0, 1, 0), # 坎 / left
    @(0, 0, 1)  # 艮 / upper left
)

function Draw-Yao {
    param(
        [System.Drawing.Graphics]$Graphics,
        [System.Drawing.Pen]$Pen,
        [int]$Yang,
        [float]$Y
    )

    if ($Yang -eq 1) {
        $Graphics.DrawLine($Pen, 48, $Y, 60, $Y)
        return
    }

    $Graphics.DrawLine($Pen, 48, $Y, 52.3, $Y)
    $Graphics.DrawLine($Pen, 55.7, $Y, 60, $Y)
}

function Render-LauncherIcon {
    param(
        [int]$Size,
        [string]$OutputPath
    )

    $bitmap = [System.Drawing.Bitmap]::new(
        $Size,
        $Size,
        [System.Drawing.Imaging.PixelFormat]::Format32bppArgb
    )
    $graphics = [System.Drawing.Graphics]::FromImage($bitmap)
    $outerPen = [System.Drawing.Pen]::new($outline, 2.1)
    $innerPen = [System.Drawing.Pen]::new($outline, 1.7)
    $mutedPen = [System.Drawing.Pen]::new($muted, 0.8)
    $linePen = [System.Drawing.Pen]::new($gold, 2.2)
    $outlinePen = [System.Drawing.Pen]::new($outline, 1.4)
    $inkBrush = [System.Drawing.SolidBrush]::new($ink)
    $ivoryBrush = [System.Drawing.SolidBrush]::new($ivory)

    $linePen.StartCap = [System.Drawing.Drawing2D.LineCap]::Flat
    $linePen.EndCap = [System.Drawing.Drawing2D.LineCap]::Flat

    try {
        $graphics.SmoothingMode = [System.Drawing.Drawing2D.SmoothingMode]::HighQuality
        $graphics.PixelOffsetMode = [System.Drawing.Drawing2D.PixelOffsetMode]::HighQuality
        $graphics.CompositingQuality = [System.Drawing.Drawing2D.CompositingQuality]::HighQuality
        $graphics.Clear($background)
        $graphics.ScaleTransform($Size / 108.0, $Size / 108.0)

        $graphics.DrawEllipse($outerPen, 19, 19, 70, 70)
        $graphics.DrawEllipse($innerPen, 22.5, 22.5, 63, 63)
        $graphics.DrawEllipse($mutedPen, 24, 24, 60, 60)

        for ($index = 0; $index -lt $brandLines.Count; $index++) {
            $state = $graphics.Save()
            try {
                $graphics.TranslateTransform(54, 54)
                $graphics.RotateTransform($index * 45)
                $graphics.TranslateTransform(-54, -54)
                $rows = $brandLines[$index]
                Draw-Yao -Graphics $graphics -Pen $linePen -Yang $rows[2] -Y 25.6
                Draw-Yao -Graphics $graphics -Pen $linePen -Yang $rows[1] -Y 29.9
                Draw-Yao -Graphics $graphics -Pen $linePen -Yang $rows[0] -Y 34.2
            } finally {
                $graphics.Restore($state)
            }
        }

        $graphics.FillEllipse($inkBrush, 38, 38, 32, 32)
        $graphics.DrawEllipse($outlinePen, 38, 38, 32, 32)
        $graphics.FillEllipse($ivoryBrush, 39, 39, 30, 30)
        $graphics.FillPie($inkBrush, 39, 39, 30, 30, -90, 180)
        $graphics.FillEllipse($inkBrush, 46.5, 39, 15, 15)
        $graphics.FillEllipse($ivoryBrush, 46.5, 54, 15, 15)
        $graphics.FillEllipse($ivoryBrush, 51.8, 44.3, 4.4, 4.4)
        $graphics.FillEllipse($inkBrush, 51.8, 59.3, 4.4, 4.4)

        $directory = Split-Path -Parent $OutputPath
        [System.IO.Directory]::CreateDirectory($directory) | Out-Null
        $bitmap.Save($OutputPath, [System.Drawing.Imaging.ImageFormat]::Png)
    } finally {
        $outlinePen.Dispose()
        $innerPen.Dispose()
        $mutedPen.Dispose()
        $linePen.Dispose()
        $outerPen.Dispose()
        $inkBrush.Dispose()
        $ivoryBrush.Dispose()
        $graphics.Dispose()
        $bitmap.Dispose()
    }
}

$densitySizes = [ordered]@{
    "mipmap-mdpi" = 48
    "mipmap-hdpi" = 72
    "mipmap-xhdpi" = 96
    "mipmap-xxhdpi" = 144
    "mipmap-xxxhdpi" = 192
}

foreach ($density in $densitySizes.Keys) {
    $size = $densitySizes[$density]
    $resourceDirectory = Join-Path $projectRoot "app/src/main/res/$density"
    Render-LauncherIcon -Size $size -OutputPath (Join-Path $resourceDirectory "ic_launcher.png")
    Render-LauncherIcon -Size $size -OutputPath (Join-Path $resourceDirectory "ic_launcher_round.png")
}

Write-Output "Rendered canonical bagua launcher fallbacks at mdpi/hdpi/xhdpi/xxhdpi/xxxhdpi."
