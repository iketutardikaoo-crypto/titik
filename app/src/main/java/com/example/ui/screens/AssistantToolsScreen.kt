package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Spellcheck
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.DotCoral
import com.example.ui.theme.DotCyan
import com.example.ui.theme.DotGold
import com.example.ui.theme.DotMint
import com.example.ui.theme.DotPurple
import com.example.ui.theme.SlateBorder
import com.example.ui.theme.SlateCard
import com.example.ui.theme.SlateDark
import com.example.ui.theme.SlateSurface
import com.example.viewmodel.AssistantViewModel

data class AssistantToolItem(
    val id: String,
    val title: String,
    val description: String,
    val icon: ImageVector,
    val accentColor: Color,
    val inputPlaceholder: String
)

@Composable
fun AssistantToolsScreen(
    viewModel: AssistantViewModel,
    modifier: Modifier = Modifier
) {
    var activeTool by remember { mutableStateOf<AssistantToolItem?>(null) }

    val tools = listOf(
        AssistantToolItem(
            id = "SUMMARIZE",
            title = "Perangkum Cerdas",
            description = "Ringkas artikel, paragraf panjang, atau laporan menjadi poin-poin utama yang mudah dipahami.",
            icon = Icons.Default.EditNote,
            accentColor = DotCyan,
            inputPlaceholder = "Tempel teks atau artikel yang ingin dirangkum di sini..."
        ),
        AssistantToolItem(
            id = "DRAFT_EMAIL",
            title = "Penulis Pesan & Email",
            description = "Buatkan draf pesan WhatsApp, email bisnis, atau surat lamaran yang santun dan profesional.",
            icon = Icons.Default.FormatQuote,
            accentColor = DotPurple,
            inputPlaceholder = "Contoh: Mau kirim email ke dosen izin tidak masuk kuliah karena sakit..."
        ),
        AssistantToolItem(
            id = "BRAINSTORM",
            title = "Generator Ide & Solusi",
            description = "Dapatkan daftar ide segar, konsep konten kreatif, atau solusi permasalahan rumit.",
            icon = Icons.Default.Lightbulb,
            accentColor = DotGold,
            inputPlaceholder = "Contoh: Ide konten edukasi seputar teknologi untuk pemula..."
        ),
        AssistantToolItem(
            id = "TRANSLATE",
            title = "Penerjemah Bahasa & Konteks",
            description = "Terjemahkan bahasa Inggris/Indonesia dengan penyesuaian gaya bahasa alami dan penjelasan arti.",
            icon = Icons.Default.Translate,
            accentColor = DotMint,
            inputPlaceholder = "Masukkan kalimat atau frasa yang ingin diterjemahkan..."
        ),
        AssistantToolItem(
            id = "FIX_GRAMMAR",
            title = "Koreksi Tata Bahasa & Ejaan",
            description = "Perbaiki tata bahasa, tanda baca, dan kata baku agar tulisan Anda terlihat sempurna.",
            icon = Icons.Default.Spellcheck,
            accentColor = DotCoral,
            inputPlaceholder = "Masukkan teks yang ingin dicek ejaan dan tata bahasanya..."
        )
    )

    if (activeTool != null) {
        ToolInputDialog(
            tool = activeTool!!,
            onDismiss = { activeTool = null },
            onSubmit = { input ->
                viewModel.executeToolPrompt(activeTool!!.id, input)
                activeTool = null
            }
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(SlateDark)
            .statusBarsPadding()
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Column {
                    Text(
                        text = "Alat Cerdas Dots",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "Pilih alat produktivitas cepat berbantu kecerdasan buatan",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF94A3B8)
                    )
                }
            }

            items(tools) { tool ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .clickable { activeTool = tool }
                        .testTag("tool_${tool.id.lowercase()}"),
                    colors = CardDefaults.cardColors(containerColor = SlateCard),
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(tool.accentColor.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = tool.icon,
                                contentDescription = null,
                                tint = tool.accentColor,
                                modifier = Modifier.size(26.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = tool.title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = tool.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF94A3B8),
                                lineHeight = 18.sp
                            )
                        }

                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "Gunakan",
                            tint = tool.accentColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ToolInputDialog(
    tool: AssistantToolItem,
    onDismiss: () -> Unit,
    onSubmit: (String) -> Unit
) {
    var input by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(22.dp),
            color = SlateCard,
            tonalElevation = 8.dp,
            modifier = Modifier.padding(8.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(tool.accentColor.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = tool.icon,
                            contentDescription = null,
                            tint = tool.accentColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = tool.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = input,
                    onValueChange = { input = it },
                    placeholder = { Text(tool.inputPlaceholder, color = Color(0xFF64748B), fontSize = 13.sp) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = tool.accentColor,
                        unfocusedBorderColor = SlateBorder,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )

                Spacer(modifier = Modifier.height(18.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Batal", color = Color(0xFF94A3B8))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (input.isNotBlank()) {
                                onSubmit(input)
                            }
                        },
                        enabled = input.isNotBlank(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Proses dengan Dots", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
