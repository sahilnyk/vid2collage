package com.sahilnayak.iykyk.ui

import android.graphics.Bitmap
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sahilnayak.iykyk.MainViewModel
import com.sahilnayak.iykyk.model.PersonResult
import com.sahilnayak.iykyk.model.ProcessingState
import kotlin.math.roundToInt

private val Ink = Color(0xFF101516)
private val Paper = Color(0xFFF5F0E7)
private val Coral = Color(0xFFFF7657)
private val Mint = Color(0xFF92D8C7)
private val Panel = Color(0xFF1A2122)
private val Hairline = Color(0xFF354041)
private val Muted = Color(0xFFA8B1AE)

@Composable
fun MainScreen(
    viewModel: MainViewModel,
    onPickVideo: () -> Unit,
    onSave: (Bitmap) -> Unit,
    onShare: (Bitmap) -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    Surface(color = Ink, modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Ink)
                .safeDrawingPadding()
        ) {
            EditorialBackdrop()
            when (val current = state) {
                ProcessingState.Empty -> EmptyContent(onPickVideo)
                is ProcessingState.Working -> ProcessingContent(current.progress, current.message)
                is ProcessingState.Complete -> ResultContent(current, onPickVideo, onSave, onShare)
                is ProcessingState.Failed -> ErrorContent(current.message, onPickVideo)
            }
        }
    }
}

@Composable
private fun EditorialBackdrop() {
    Canvas(Modifier.fillMaxSize()) {
        drawCircle(Coral.copy(alpha = 0.10f), radius = size.width * 0.48f, center = Offset.Zero)
        drawCircle(Mint.copy(alpha = 0.07f), radius = size.width * 0.58f, center = Offset(size.width, size.height))
    }
}

@Composable
private fun EmptyContent(onPickVideo: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp, vertical = 18.dp)) {
        BrandHeader()
        Spacer(Modifier.weight(0.75f))
        Text("VIDEO → PEOPLE", color = Mint, fontSize = 13.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
        Spacer(Modifier.height(14.dp))
        Text("A cast list\nfor your video.", style = MaterialTheme.typography.displayLarge, color = Paper)
        Spacer(Modifier.height(16.dp))
        Box(Modifier.size(width = 64.dp, height = 5.dp).background(Coral, CircleShape))
        Spacer(Modifier.height(20.dp))
        Text(
            "Pick a portrait clip. IYKYK finds every person, counts each return, and keeps their best frame.",
            color = Muted,
            style = MaterialTheme.typography.bodyLarge
        )
        Spacer(Modifier.height(28.dp))
        ProcessCard()
        Spacer(Modifier.weight(1f))
        PrimaryButton("Choose a video", onPickVideo)
        Spacer(Modifier.height(14.dp))
        Text(
            "PRIVATE BY DESIGN  ·  NOTHING LEAVES YOUR PHONE",
            modifier = Modifier.fillMaxWidth(),
            color = Muted,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.1.sp,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun ProcessCard() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Panel)
            .border(1.dp, Hairline, RoundedCornerShape(20.dp))
            .padding(horizontal = 18.dp, vertical = 17.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        ProcessStep("01", "Detect", Modifier.weight(1f))
        ProcessStep("02", "Match", Modifier.weight(1f))
        ProcessStep("03", "Pick", Modifier.weight(1f))
    }
}

@Composable
private fun ProcessStep(number: String, label: String, modifier: Modifier = Modifier) {
    Column(modifier) {
        Text(number, color = Coral, fontSize = 11.sp, fontWeight = FontWeight.Black)
        Spacer(Modifier.height(5.dp))
        Text(label, color = Paper, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun ProcessingContent(progress: Float, message: String) {
    val safeProgress = progress.coerceIn(0f, 1f)
    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp, vertical = 18.dp)) {
        BrandHeader(status = "WORKING ON-DEVICE")
        Spacer(Modifier.weight(0.8f))
        Text(
            "${(safeProgress * 100).roundToInt()}%",
            color = Coral,
            fontSize = 72.sp,
            lineHeight = 76.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = (-3).sp
        )
        Text(message, color = Paper, style = MaterialTheme.typography.headlineLarge)
        Spacer(Modifier.height(28.dp))
        LinearProgressIndicator(
            progress = { safeProgress },
            modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape),
            color = Coral,
            trackColor = Hairline
        )
        Spacer(Modifier.height(16.dp))
        ProgressStages(safeProgress)
        Spacer(Modifier.weight(1.2f))
        Surface(color = Panel, shape = RoundedCornerShape(18.dp), modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(18.dp)) {
                Text("NO CLOUD. NO UPLOAD.", color = Mint, fontSize = 11.sp, fontWeight = FontWeight.Black, letterSpacing = 1.3.sp)
                Spacer(Modifier.height(6.dp))
                Text("Frames are analysed here, then released when you leave.", color = Muted, fontSize = 14.sp)
            }
        }
    }
}

@Composable
private fun ProgressStages(progress: Float) {
    val stages = listOf("DETECT" to 0f, "MATCH" to 0.25f, "SELECT" to 0.70f)
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        stages.forEach { (label, start) ->
            Text(
                label,
                color = if (progress >= start) Paper else Muted.copy(alpha = 0.45f),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.2.sp
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
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 18.dp)
    ) {
        BrandHeader(status = "COLLAGE READY")
        Spacer(Modifier.height(24.dp))
        Text("Your final cut.", color = Paper, style = MaterialTheme.typography.headlineLarge)
        Spacer(Modifier.height(14.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            StatCard(people.size.toString(), if (people.size == 1) "PERSON" else "PEOPLE", Modifier.weight(1f))
            StatCard(appearances.toString(), "APPEARANCES", Modifier.weight(1f))
        }
        Spacer(Modifier.height(18.dp))
        // The preview keeps the Story ratio so the recording matches the saved image.
        Image(
            bitmap = state.result.collage.asImageBitmap(),
            contentDescription = "Generated collage with ${people.size} people and $appearances appearances",
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(9f / 16f)
                .clip(RoundedCornerShape(20.dp))
                .border(1.dp, Hairline, RoundedCornerShape(20.dp)),
            contentScale = ContentScale.FillBounds
        )
        Spacer(Modifier.height(22.dp))
        SectionLabel("APPEARANCE BREAKDOWN")
        Spacer(Modifier.height(10.dp))
        AppearanceSummary(people)
        Spacer(Modifier.height(20.dp))
        PrimaryButton("Save to gallery") { onSave(state.result.collage) }
        Spacer(Modifier.height(10.dp))
        OutlinedButton(
            onClick = { onShare(state.result.collage) },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Paper)
        ) {
            Text("Share collage", fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(10.dp))
        OutlinedButton(
            onClick = onPickVideo,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Muted)
        ) {
            Text("Process another video", fontWeight = FontWeight.SemiBold)
        }
        Spacer(Modifier.height(12.dp))
    }
}

@Composable
private fun StatCard(value: String, label: String, modifier: Modifier = Modifier) {
    Surface(modifier = modifier, color = Panel, shape = RoundedCornerShape(18.dp)) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(value, color = Coral, fontSize = 30.sp, lineHeight = 32.sp, fontWeight = FontWeight.Black)
            Text(label, color = Muted, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.8.sp, modifier = Modifier.padding(bottom = 4.dp))
        }
    }
}

@Composable
private fun AppearanceSummary(people: List<PersonResult>) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        people.forEach { person ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Panel)
                    .border(1.dp, Hairline, RoundedCornerShape(16.dp))
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    bitmap = person.portrait.asImageBitmap(),
                    contentDescription = null,
                    modifier = Modifier.size(width = 48.dp, height = 54.dp).clip(RoundedCornerShape(11.dp)),
                    contentScale = ContentScale.Crop
                )
                Spacer(Modifier.size(12.dp))
                Text("Person ${person.id.toString().padStart(2, '0')}", color = Paper, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                Surface(color = Mint, shape = CircleShape) {
                    Text(
                        "${person.appearanceCount}×",
                        color = Ink,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.padding(horizontal = 13.dp, vertical = 7.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun ErrorContent(message: String, onPickVideo: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp, vertical = 18.dp)) {
        BrandHeader(status = "NEEDS ANOTHER TAKE")
        Spacer(Modifier.weight(1f))
        Text("We couldn't read that one.", color = Paper, style = MaterialTheme.typography.headlineLarge)
        Spacer(Modifier.height(10.dp))
        Text(message, color = Muted, style = MaterialTheme.typography.bodyLarge)
        Spacer(Modifier.height(26.dp))
        PrimaryButton("Choose another video", onPickVideo)
        Spacer(Modifier.weight(1.2f))
    }
}

@Composable
private fun BrandHeader(status: String = "PORTRAIT VIDEO LAB") {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("IYKYK", color = Paper, fontSize = 18.sp, fontWeight = FontWeight.Black, letterSpacing = 3.sp)
        Surface(color = Panel, shape = CircleShape) {
            Text(status, color = Mint, fontSize = 9.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.8.sp, modifier = Modifier.padding(horizontal = 11.dp, vertical = 7.dp))
        }
    }
}

@Composable
private fun SectionLabel(label: String) {
    Text(label, color = Mint, fontSize = 11.sp, fontWeight = FontWeight.Black, letterSpacing = 1.4.sp)
}

@Composable
private fun PrimaryButton(label: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().height(58.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Coral, contentColor = Ink)
    ) {
        Text(label, fontWeight = FontWeight.Black, fontSize = 15.sp)
    }
}
