package com.nkwabyte.medilert.ui.screens.profile

import androidx.compose.foundation.Image
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nkwabyte.medilert.data.platform.rememberImagePicker
import com.nkwabyte.medilert.ui.theme.*
import com.nkwabyte.medilert.viewmodel.AppViewModel
import com.nkwabyte.medilert.viewmodel.NavViewModel

@Composable
fun ProfilePhotoViewScreen(
    navViewModel: NavViewModel = viewModel { NavViewModel() },
    appViewModel: AppViewModel = viewModel { AppViewModel() }
) {
    val currentUser      by appViewModel.currentUser.collectAsState()
    val isUploading      by appViewModel.isUploadingPhoto.collectAsState()
    val storedPhotoBytes by appViewModel.profilePhotoBytes.collectAsState()
    var showDeleteDialog by remember { mutableStateOf(false) }

    // Bytes of the image just picked; null = fall back to storedPhotoBytes or placeholder
    var localBytes by remember { mutableStateOf<ByteArray?>(null) }

    var errorMessage by remember { mutableStateOf<String?>(null) }

    val picker = rememberImagePicker { bytes ->
        val maxSize = 20 * 1024 * 1024 // 20 MB limit
        if (bytes.size > maxSize) {
            errorMessage = "Image is too large. Please select an image under 20 MB."
            return@rememberImagePicker
        }

        // Validate magic bytes for JPEG or PNG
        val isJpeg = bytes.size > 2 && bytes[0] == 0xFF.toByte() && bytes[1] == 0xD8.toByte()
        val isPng = bytes.size > 8 && bytes[0] == 0x89.toByte() && bytes[1] == 0x50.toByte() && bytes[2] == 0x4E.toByte() && bytes[3] == 0x47.toByte()

        if (!isJpeg && !isPng) {
            errorMessage = "Only JPEG and PNG images are allowed."
            return@rememberImagePicker
        }

        errorMessage = null
        localBytes = bytes
        appViewModel.uploadProfilePhoto(bytes)
    }

    val displayBytes = localBytes ?: storedPhotoBytes
    val hasPhoto = displayBytes != null || currentUser.photoUrl.isNotBlank()

    Box(modifier = Modifier.fillMaxSize().background(TextPrimary)) {

        // ── Back button ──────────────────────────────────────────────────────
        Box(
            modifier = Modifier
                .padding(start = 24.dp, top = 56.dp)
                .size(40.dp)
                .background(Color.White.copy(alpha = 0.2f), CircleShape)
                .clickable { navViewModel.popBack() },
            contentAlignment = Alignment.Center
        ) { Icon(Icons.Default.ChevronLeft, contentDescription = "Back", tint = Color.White) }

        // ── Photo area ───────────────────────────────────────────────────────
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            if (displayBytes != null) {
                coil3.compose.AsyncImage(
                    model = displayBytes,
                    contentDescription = "Profile photo",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(200.dp)
                        .clip(CircleShape)
                        .border(3.dp, PrimaryGreen.copy(alpha = 0.6f), CircleShape)
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(200.dp)
                        .background(Color.White.copy(alpha = 0.1f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.5f),
                        modifier = Modifier.size(100.dp)
                    )
                }
            }

            if (isUploading) {
                CircularProgressIndicator(
                    color = PrimaryGreen,
                    strokeWidth = 3.dp,
                    modifier = Modifier.size(220.dp)
                )
            }
        }

        // ── Upload status label ───────────────────────────────────────────────
        if (isUploading) {
            Text(
                "Uploading…",
                fontFamily = Poppins,
                fontWeight = FontWeight.Medium,
                fontSize = 13.sp,
                color = Color.White.copy(0.7f),
                modifier = Modifier
                    .align(Alignment.Center)
                    .offset(y = 130.dp)
            )
        }

        // ── Bottom action buttons ────────────────────────────────────────────
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(
                    brush = Brush.verticalGradient(
                        listOf(Color.Transparent, Color.Black.copy(alpha = 0.8f))
                    )
                )
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Button(
                    onClick = { picker.pickFromCamera() },
                    enabled = !isUploading,
                    modifier = Modifier.weight(1f).height(52.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)
                ) {
                    Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Camera", fontFamily = Poppins, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                }
                Button(
                    onClick = { picker.pickFromGallery() },
                    enabled = !isUploading,
                    modifier = Modifier.weight(1f).height(52.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.15f))
                ) {
                    Icon(Icons.Default.Image, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Gallery", fontFamily = Poppins, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                }
            }

            if (hasPhoto) {
                OutlinedButton(
                    onClick = { showDeleteDialog = true },
                    enabled = !isUploading,
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
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = {
                Text("Remove Photo", fontFamily = Poppins, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            },
            text = {
                Text(
                    "Are you sure you want to remove your profile photo?",
                    fontFamily = Poppins, fontWeight = FontWeight.Medium, fontSize = 14.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteDialog = false
                        localBytes = null
                        appViewModel.removeProfilePhoto()
                        navViewModel.popBack()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GhanaRed)
                ) {
                    Text("Remove", fontFamily = Poppins, fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel", fontFamily = Poppins, fontWeight = FontWeight.SemiBold)
                }
            },
            shape = RoundedCornerShape(24.dp)
        )
    }

    if (errorMessage != null) {
        AlertDialog(
            onDismissRequest = { errorMessage = null },
            title = {
                Text("Invalid Image", fontFamily = Poppins, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = GhanaRed)
            },
            text = {
                Text(
                    errorMessage!!,
                    fontFamily = Poppins, fontWeight = FontWeight.Medium, fontSize = 14.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = { errorMessage = null },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)
                ) {
                    Text("OK", fontFamily = Poppins, fontWeight = FontWeight.SemiBold)
                }
            },
            shape = RoundedCornerShape(24.dp)
        )
    }
}
