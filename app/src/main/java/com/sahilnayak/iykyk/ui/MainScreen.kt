package com.sahilnayak.iykyk.ui

import android.graphics.Bitmap
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.outlined.Face
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sahilnayak.iykyk.MainViewModel
import com.sahilnayak.iykyk.model.PersonResult
import com.sahilnayak.iykyk.model.ProcessingState
import kotlin.math.roundToInt

private val Forest = Color(0xFF12382E)
private val ForestSoft = Color(0xFF244D41)
private val Cream = Color(0xFFFFF9ED)
private val Pistachio = Color(0xFFEFF8C9)
private val Mint = Color(0xFFC9EFDA)
private val Lilac = Color(0xFFE8D4F5)
private val Peach = Color(0xFFF5C7A9)
private val Quiet = Color(0xFF617069)
private val Line = Color(0xFFD4DDD2)

private enum class ScreenKey { Home, Processing, Result, Error }
private enum class AppIcon { Video, Lock, Save, Share, Refresh, Arrow, People }

private fun AppIcon.vector(): ImageVector = when (this) {
    AppIcon.Video -> Icons.Outlined.Face
    AppIcon.Lock -> Icons.Outlined.Lock
    AppIcon.Save -> Icons.Outlined.Face
    AppIcon.Share -> Icons.Outlined.Share
    AppIcon.Refresh -> Icons.Outlined.Refresh
    AppIcon.Arrow -> Icons.AutoMirrored.Outlined.ArrowForward
    AppIcon.People -> Icons.Outlined.Face
}

@Composable
fun MainScreen(
    viewModel: MainViewModel,
    onPickVideo: () -> Unit,
    onSave: (Bitmap) -> Unit,
    onShare: (Bitmap) -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val screen = when (state) {
        ProcessingState.Empty -> ScreenKey.Home
        is ProcessingState.Working -> ScreenKey.Processing
        is ProcessingState.Complete -> ScreenKey.Result
        is ProcessingState.Failed -> ScreenKey.Error
    }

    Surface(color = Cream, modifier = Modifier.fillMaxSize()) {
        AnimatedContent(
            targetState = screen,
            transitionSpec = { fadeIn(tween(320)) togetherWith fadeOut(tween(180)) },
            label = "screen transition"
        ) { target ->
            when (target) {
                ScreenKey.Home -> HomeContent(onPickVideo)
                ScreenKey.Processing -> {
                    val working = state as? ProcessingState.Working
                    ProcessingContent(working?.progress ?: 0f, working?.message.orEmpty())
                }
                ScreenKey.Result -> {
                    val complete = state as? ProcessingState.Complete
                    if (complete != null) ResultContent(complete, onPickVideo, onSave, onShare)
                }
                ScreenKey.Error -> {
                    val failed = state as? ProcessingState.Failed
                    ErrorContent(failed?.message.orEmpty(), onPickVideo)
                }
            }
        }
    }
}

@Composable
private fun HomeContent(onPickVideo: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Cream)
            .safeDrawingPadding()
            .padding(horizontal = 18.dp, vertical = 14.dp)
    ) {
        AppHeader("on-device")
        Spacer(Modifier.height(16.dp))
        Surface(
            color = Pistachio,
            shape = RoundedCornerShape(28.dp, 28.dp, 9.dp, 28.dp),
            modifier = Modifier.fillMaxWidth().weight(1f)
        ) {
            Column(Modifier.padding(horizontal = 22.dp, vertical = 20.dp)) {
                Text("portrait stories, sorted", color = ForestSoft, style = MaterialTheme.typography.labelLarge)
                Spacer(Modifier.height(8.dp))
                Text("turn a video\ninto a cast.", color = Forest, style = MaterialTheme.typography.displayLarge)
                Spacer(Modifier.height(6.dp))
                Text(
                    "every person, every return, one frame worth keeping.",
                    color = Quiet,
                    style = MaterialTheme.typography.bodyLarge
                )
                Spacer(Modifier.height(16.dp))
                PortraitDeck(Modifier.fillMaxWidth().weight(1f))
                Spacer(Modifier.height(14.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    ProcessStep("01", "find faces")
                    ProcessStep("02", "match people")
                    ProcessStep("03", "pick the frame")
                }
            }
        }
        Spacer(Modifier.height(12.dp))
        ActionButton("choose a video", AppIcon.Video, onPickVideo)
        Spacer(Modifier.height(10.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            LineIcon(AppIcon.Lock, Quiet, Modifier.size(15.dp))
            Spacer(Modifier.size(6.dp))
            Text("your frames never leave this phone", color = Quiet, style = MaterialTheme.typography.labelMedium)
        }
    }
}

@Composable
private fun PortraitDeck(modifier: Modifier = Modifier) {
    Box(modifier, contentAlignment = Alignment.Center) {
        PortraitPlaceholder(
            color = Peach,
            modifier = Modifier.fillMaxHeight(0.82f).aspectRatio(0.64f).offset(x = (-76).dp, y = 8.dp).rotate(-7f)
        )
        PortraitPlaceholder(
            color = Lilac,
            modifier = Modifier.fillMaxHeight(0.92f).aspectRatio(0.64f).offset(x = 72.dp, y = 8.dp).rotate(6f)
        )
        PortraitPlaceholder(color = Mint, featured = true, modifier = Modifier.fillMaxHeight().aspectRatio(0.64f))
    }
}

@Composable
private fun PortraitPlaceholder(color: Color, featured: Boolean = false, modifier: Modifier = Modifier) {
    Surface(
        color = color,
        shape = RoundedCornerShape(48.dp, 18.dp, 48.dp, 18.dp),
        border = BorderStroke(2.dp, Forest),
        shadowElevation = if (featured) 8.dp else 1.dp,
        modifier = modifier
    ) {
        Box(Modifier.fillMaxSize().padding(12.dp)) {
            Canvas(Modifier.fillMaxSize()) {
                val stroke = Stroke(width = 2.2.dp.toPx(), cap = StrokeCap.Round)
                drawCircle(Forest, radius = size.minDimension * 0.15f, center = Offset(size.width / 2, size.height * 0.36f), style = stroke)
                drawArc(
                    color = Forest,
                    startAngle = 205f,
                    sweepAngle = 130f,
                    useCenter = false,
                    topLeft = Offset(size.width * 0.23f, size.height * 0.50f),
                    size = Size(size.width * 0.54f, size.height * 0.36f),
                    style = stroke
                )
                drawLine(Forest.copy(alpha = 0.35f), Offset(0f, size.height * 0.83f), Offset(size.width, size.height * 0.83f), strokeWidth = 1.dp.toPx())
            }
            Text(
                if (featured) "best frame" else "face found",
                color = Forest,
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.align(Alignment.BottomStart)
            )
        }
    }
}

@Composable
private fun ProcessStep(number: String, label: String) {
    Column {
        Text(number, color = Forest.copy(alpha = 0.48f), style = MaterialTheme.typography.labelSmall)
        Text(label, color = Forest, fontSize = 11.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun ProcessingContent(progress: Float, message: String) {
    val safeProgress = progress.coerceIn(0f, 1f)
    val animatedProgress by animateFloatAsState(safeProgress, tween(350), label = "processing progress")
    Column(
        modifier = Modifier.fillMaxSize().background(Cream).safeDrawingPadding().padding(horizontal = 18.dp, vertical = 14.dp)
    ) {
        AppHeader("working locally")
        Spacer(Modifier.height(16.dp))
        Surface(
            color = Forest,
            shape = RoundedCornerShape(28.dp, 28.dp, 28.dp, 9.dp),
            modifier = Modifier.fillMaxWidth().weight(1f)
        ) {
            Column(Modifier.padding(22.dp)) {
                Text("finding the people", color = Mint, style = MaterialTheme.typography.labelLarge)
                Spacer(Modifier.height(8.dp))
                Text(
                    "${(animatedProgress * 100).roundToInt()}%",
                    color = Cream,
                    fontSize = 68.sp,
                    lineHeight = 68.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = (-3).sp
                )
                Text(message.lowercase(), color = Cream, style = MaterialTheme.typography.headlineMedium)
                Spacer(Modifier.height(24.dp))
                ScanningFrames(Modifier.fillMaxWidth().weight(1f))
                Spacer(Modifier.height(22.dp))
                LinearProgressIndicator(
                    progress = { animatedProgress },
                    modifier = Modifier.fillMaxWidth().height(9.dp).clip(CircleShape),
                    color = Peach,
                    trackColor = ForestSoft
                )
                Spacer(Modifier.height(12.dp))
                ProgressStages(animatedProgress)
            }
        }
        Spacer(Modifier.height(12.dp))
        Surface(color = Lilac, shape = RoundedCornerShape(18.dp), modifier = Modifier.fillMaxWidth()) {
            Row(Modifier.padding(15.dp), verticalAlignment = Alignment.CenterVertically) {
                LineIcon(AppIcon.Lock, Forest, Modifier.size(20.dp))
                Spacer(Modifier.size(10.dp))
                Text("no cloud, no upload, everything stays here", color = Forest, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@Composable
private fun ScanningFrames(modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "scanner")
    val scanPosition by transition.animateFloat(
        initialValue = 0.08f,
        targetValue = 0.92f,
        animationSpec = infiniteRepeatable(tween(1500, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "scan position"
    )
    Box(modifier, contentAlignment = Alignment.Center) {
        Row(
            Modifier.fillMaxWidth().fillMaxHeight(0.74f),
            horizontalArrangement = Arrangement.spacedBy(9.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            listOf(Mint, Pistachio, Lilac).forEachIndexed { index, color ->
                Surface(
                    color = color,
                    shape = RoundedCornerShape(
                        topStart = if (index == 0) 36.dp else 14.dp,
                        topEnd = 14.dp,
                        bottomStart = 14.dp,
                        bottomEnd = if (index == 2) 36.dp else 14.dp
                    ),
                    modifier = Modifier.weight(1f).fillMaxHeight(if (index == 1) 1f else 0.88f)
                ) {
                    LineIcon(AppIcon.People, Forest.copy(alpha = 0.66f), Modifier.padding(18.dp))
                }
            }
        }
        Canvas(Modifier.fillMaxSize()) {
            val y = size.height * scanPosition
            drawLine(Peach, Offset(0f, y), Offset(size.width, y), strokeWidth = 3.dp.toPx(), cap = StrokeCap.Round)
        }
    }
}

@Composable
private fun ProgressStages(progress: Float) {
    val stages = listOf("detect" to 0f, "match" to 0.25f, "select" to 0.70f)
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        stages.forEach { (label, start) ->
            Text(
                label,
                color = if (progress >= start) Cream else Cream.copy(alpha = 0.35f),
                style = MaterialTheme.typography.labelMedium
            )
        }
    }
}

@Composable
private fun ResultContent(
    state: ProcessingState.Complete,
    onPickVideo: () -> Unit,
    onSave: (Bitmap) -> Unit,
    onShare: (Bitmap) -> Unit
) {
    val people = state.result.people
    val appearances = people.sumOf(PersonResult::appearanceCount)
    Column(
        modifier = Modifier.fillMaxSize().background(Cream).safeDrawingPadding().verticalScroll(rememberScrollState())
            .padding(horizontal = 18.dp, vertical = 14.dp)
    ) {
        AppHeader("collage ready")
        Spacer(Modifier.height(20.dp))
        Text("your people.", color = Forest, style = MaterialTheme.typography.displayMedium)
        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            StatCard(people.size.toString(), if (people.size == 1) "person" else "people", Mint, Modifier.weight(1f))
            StatCard(appearances.toString(), "appearances", Lilac, Modifier.weight(1f))
        }
        Spacer(Modifier.height(14.dp))
        Surface(
            color = Forest,
            shape = RoundedCornerShape(26.dp, 8.dp, 26.dp, 26.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(Modifier.padding(8.dp)) {
                Row(
                    Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 7.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("final cut", color = Cream, style = MaterialTheme.typography.labelLarge)
                    Text("portrait story  ·  9:16", color = Mint, style = MaterialTheme.typography.labelMedium)
                }
                // Matching the export ratio here makes the phone recording an honest preview.
                Image(
                    bitmap = state.result.collage.asImageBitmap(),
                    contentDescription = "collage with ${people.size} people and $appearances appearances",
                    modifier = Modifier.fillMaxWidth().aspectRatio(9f / 16f).clip(RoundedCornerShape(20.dp)),
                    contentScale = ContentScale.FillBounds
                )
            }
        }
        Spacer(Modifier.height(22.dp))
        Text("appearance notes", color = Forest, style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(10.dp))
        AppearanceSummary(people)
        Spacer(Modifier.height(18.dp))
        ActionButton("save to gallery", AppIcon.Save) { onSave(state.result.collage) }
        Spacer(Modifier.height(10.dp))
        SecondaryButton("share collage", AppIcon.Share) { onShare(state.result.collage) }
        Spacer(Modifier.height(10.dp))
        SecondaryButton("choose another video", AppIcon.Refresh, onPickVideo)
        Spacer(Modifier.height(12.dp))
    }
}

@Composable
private fun StatCard(value: String, label: String, color: Color, modifier: Modifier = Modifier) {
    Surface(color = color, shape = RoundedCornerShape(22.dp, 22.dp, 7.dp, 22.dp), modifier = modifier) {
        Column(Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {
            Text(value, color = Forest, fontSize = 36.sp, lineHeight = 36.sp, fontWeight = FontWeight.Black)
            Text(label, color = ForestSoft, style = MaterialTheme.typography.labelLarge)
        }
    }
}

@Composable
private fun AppearanceSummary(people: List<PersonResult>) {
    Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
        people.forEachIndexed { index, person ->
            val tint = if (index % 2 == 0) Pistachio else Lilac
            Surface(
                color = tint,
                shape = RoundedCornerShape(18.dp, 18.dp, 6.dp, 18.dp),
                border = BorderStroke(1.dp, Line),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Image(
                        bitmap = person.portrait.asImageBitmap(),
                        contentDescription = null,
                        modifier = Modifier.size(width = 52.dp, height = 58.dp).clip(RoundedCornerShape(16.dp, 7.dp, 16.dp, 7.dp)),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(Modifier.size(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text("person ${person.id.toString().padStart(2, '0')}", color = Forest, fontWeight = FontWeight.Bold)
                        Text("clear appearances", color = Quiet, style = MaterialTheme.typography.labelMedium)
                    }
                    Text("${person.appearanceCount}×", color = Forest, fontSize = 22.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(end = 10.dp))
                }
            }
        }
    }
}

@Composable
private fun ErrorContent(message: String, onPickVideo: () -> Unit) {
    Column(Modifier.fillMaxSize().background(Cream).safeDrawingPadding().padding(18.dp)) {
        AppHeader("try another clip")
        Spacer(Modifier.weight(1f))
        Surface(color = Lilac, shape = RoundedCornerShape(30.dp, 30.dp, 30.dp, 8.dp)) {
            Column(Modifier.padding(24.dp)) {
                Text("that video needs another take.", color = Forest, style = MaterialTheme.typography.displayMedium)
                Spacer(Modifier.height(10.dp))
                Text(message.lowercase(), color = Quiet, style = MaterialTheme.typography.bodyLarge)
            }
        }
        Spacer(Modifier.height(16.dp))
        ActionButton("choose another video", AppIcon.Refresh, onPickVideo)
        Spacer(Modifier.weight(1f))
    }
}

@Composable
private fun AppHeader(status: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("vid2collage", color = Forest, fontSize = 20.sp, fontWeight = FontWeight.Black, letterSpacing = (-0.5).sp)
        Surface(color = Color.Transparent, shape = CircleShape, border = BorderStroke(1.dp, Forest.copy(alpha = 0.34f))) {
            Row(Modifier.padding(horizontal = 11.dp, vertical = 7.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(6.dp).background(Mint, CircleShape).border(1.dp, Forest, CircleShape))
                Spacer(Modifier.size(7.dp))
                Text(status, color = Forest, style = MaterialTheme.typography.labelMedium)
            }
        }
    }
}

@Composable
private fun ActionButton(label: String, icon: AppIcon, onClick: () -> Unit) {
    PressButton(label, icon, Forest, Cream, onClick)
}

@Composable
private fun SecondaryButton(label: String, icon: AppIcon, onClick: () -> Unit) {
    PressButton(label, icon, Cream, Forest, onClick, border = true)
}

@Composable
private fun PressButton(
    label: String,
    icon: AppIcon,
    background: Color,
    foreground: Color,
    onClick: () -> Unit,
    border: Boolean = false
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(if (pressed) 0.975f else 1f, spring(stiffness = 520f), label = "button press")
    Button(
        onClick = onClick,
        interactionSource = interaction,
        modifier = Modifier.fillMaxWidth().height(62.dp).graphicsLayer { scaleX = scale; scaleY = scale },
        shape = RoundedCornerShape(18.dp, 18.dp, 6.dp, 18.dp),
        border = if (border) BorderStroke(1.5.dp, Forest) else null,
        colors = ButtonDefaults.buttonColors(containerColor = background, contentColor = foreground),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp, pressedElevation = 0.dp),
        contentPadding = PaddingValues(horizontal = 18.dp)
    ) {
        LineIcon(icon, foreground, Modifier.size(21.dp))
        Spacer(Modifier.size(10.dp))
        Text(label, fontWeight = FontWeight.Bold, fontSize = 16.sp, modifier = Modifier.weight(1f), textAlign = TextAlign.Start)
        LineIcon(AppIcon.Arrow, foreground, Modifier.size(20.dp))
    }
}

@Composable
private fun LineIcon(icon: AppIcon, color: Color, modifier: Modifier = Modifier) {
    if (icon == AppIcon.Video || icon == AppIcon.Save) {
        Canvas(modifier) {
            val width = 1.8.dp.toPx()
            val stroke = Stroke(width = width, cap = StrokeCap.Round)
            if (icon == AppIcon.Video) {
                drawRoundRect(color, Offset(size.width * .08f, size.height * .24f), Size(size.width * .62f, size.height * .58f), CornerRadius(size.minDimension * .12f), style = stroke)
                val path = Path().apply {
                    moveTo(size.width * .72f, size.height * .40f)
                    lineTo(size.width * .94f, size.height * .28f)
                    lineTo(size.width * .94f, size.height * .76f)
                    lineTo(size.width * .72f, size.height * .64f)
                }
                drawPath(path, color, style = stroke)
            } else {
                drawLine(color, Offset(size.width * .50f, size.height * .10f), Offset(size.width * .50f, size.height * .66f), width, StrokeCap.Round)
                drawLine(color, Offset(size.width * .28f, size.height * .46f), Offset(size.width * .50f, size.height * .68f), width, StrokeCap.Round)
                drawLine(color, Offset(size.width * .72f, size.height * .46f), Offset(size.width * .50f, size.height * .68f), width, StrokeCap.Round)
                drawLine(color, Offset(size.width * .18f, size.height * .88f), Offset(size.width * .82f, size.height * .88f), width, StrokeCap.Round)
            }
        }
    } else {
        Icon(imageVector = icon.vector(), contentDescription = null, tint = color, modifier = modifier)
    }
}
