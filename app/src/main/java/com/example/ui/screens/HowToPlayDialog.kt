package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Loop
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.example.ui.theme.SlateCard
import com.example.ui.theme.SlateSurface

@Composable
fun HowToPlayDialog(
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = SlateCard,
            tonalElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .testTag("how_to_play_dialog")
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Cara Bermain Dots",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("close_how_to_play_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Tutup",
                            tint = Color.White.copy(alpha = 0.7f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Rule 1: Connect Dots
                TutorialRuleCard(
                    icon = Icons.Default.TouchApp,
                    iconTint = DotCyan,
                    title = "1. Hubungkan Titik Sama Warna",
                    description = "Tarik garis horizontal atau vertikal menghubungkan 2 titik atau lebih yang memiliki warna identik untuk menghapusnya."
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Rule 2: Form Squares / Loops
                TutorialRuleCard(
                    icon = Icons.Default.Loop,
                    iconTint = DotGold,
                    title = "2. Rahasia Kotak / Loop Tertutup",
                    description = "Jika garis Anda membentuk loop atau mengelilingi kotak dan kembali ke titik yang sama, SEMUA titik warna tersebut di seluruh papan akan lenyap seketika dengan poin bonus besar!"
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Rule 3: Game Modes
                TutorialRuleCard(
                    icon = Icons.Default.Speed,
                    iconTint = DotCoral,
                    title = "3. Tiga Mode Seru",
                    description = "• 60 Detik: Pacu adrenalin mengumpulkan skor tertinggi secepat mungkin!\n• 30 Langkah: Susun strategi membuat loop sebanyak-banyaknya.\n• Zen: Relaksasi santai tanpa batas waktu."
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Rule 4: Dots and Boxes
                TutorialRuleCard(
                    icon = Icons.Default.GridView,
                    iconTint = DotMint,
                    title = "4. Mode Titik & Kotak (Dots & Boxes)",
                    description = "Hubungkan 2 pin titik untuk membuat garis. Siapa pun yang melengkapi garis ke-4 dari sebuah kotak akan mengklaim kotak itu dan mendapat GILIRAN EKSTRA!"
                )

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("understand_button"),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text("Saya Mengerti, Ayo Main!", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun TutorialRuleCard(
    icon: ImageVector,
    iconTint: Color,
    title: String,
    description: String
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SlateSurface.copy(alpha = 0.6f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(iconTint.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFFCBD5E1),
                    lineHeight = 20.sp
                )
            }
        }
    }
}
