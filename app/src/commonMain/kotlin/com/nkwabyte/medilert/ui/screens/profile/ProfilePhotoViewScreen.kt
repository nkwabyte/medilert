package com.nkwabyte.medilert.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nkwabyte.medilert.ui.theme.*
import com.nkwabyte.medilert.viewmodel.NavViewModel

@Composable
fun ProfilePhotoViewScreen(navViewModel: NavViewModel = viewModel { NavViewModel() }) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize().background(TextPrimary)) {
        Box(
            modifier = Modifier.size(40.dp).align(Alignment.TopStart).padding(start = 24.dp, top = 56.dp)
                .background(Color.White.copy(alpha = 0.2f), CircleShape).clickable { navViewModel.popBack() },
            contentAlignment = Alignment.Center
        ) { Icon(Icons.Default.ChevronLeft, contentDescription = "Back", tint = Color.White) }

        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Box(
                modifier = Modifier.size(200.dp).background(Color.White.copy(alpha = 0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) { Icon(Icons.Default.Person, contentDescription = null, tint = Color.White.copy(alpha = 0.5f), modifier = Modifier.size(100.dp)) }
        }

        Column(
            modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth().background(brush = Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.8f)))).padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Button(
                    onClick = { },
                    modifier = Modifier.weight(1f).height(52.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)
                ) {
                    Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Camera", fontFamily = Poppins, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                }
                Button(
                    onClick = { },
                    modifier = Modifier.weight(1f).height(52.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.15f))
                ) {
                    Icon(Icons.Default.Image, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Gallery", fontFamily = Poppins, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                }
            }

            OutlinedButton(
                onClick = { showDeleteDialog = true },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = GhanaRed),
                border = androidx.compose.foundation.BorderStroke(1.dp, GhanaRed.copy(alpha = 0.5f))
            ) {
                Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Remove Photo", fontFamily = Poppins, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Remove Photo", fontFamily = Poppins, fontWeight = FontWeight.Bold, fontSize = 18.sp) },
            text = { Text("Are you sure you want to remove your profile photo?", fontFamily = Poppins, fontWeight = FontWeight.Medium, fontSize = 14.sp) },
            confirmButton = {
                Button(onClick = { showDeleteDialog = false; navViewModel.popBack() }, colors = ButtonDefaults.buttonColors(containerColor = GhanaRed)) {
                    Text("Remove", fontFamily = Poppins, fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showDeleteDialog = false }) { Text("Cancel", fontFamily = Poppins, fontWeight = FontWeight.SemiBold) }
            },
            shape = RoundedCornerShape(24.dp)
        )
    }
}
