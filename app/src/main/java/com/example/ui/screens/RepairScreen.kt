package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.RepairRequest
import com.example.ui.components.CategoryIconHelper
import com.example.ui.components.StatusTimeline
import com.example.ui.locale.AppStrings
import com.example.ui.theme.BrandTertiary
import com.example.ui.theme.SuccessGreen
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun RepairScreen(
    strings: AppStrings,
    repairs: List<RepairRequest>,
    onSubmitRepair: (
        customerName: String,
        customerPhone: String,
        itemType: String,
        problemTitle: String,
        problemDescription: String,
        preferredDate: String,
        photoUri: String?,
        onSuccess: (String) -> Unit
    ) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) }

    // Form state
    var selectedDeviceType by remember { mutableStateOf(RepairRequest.ITEM_TYPES[0]) }
    var problemTitle by remember { mutableStateOf("") }
    var problemDescription by remember { mutableStateOf("") }
    var customerName by remember { mutableStateOf("") }
    var customerPhone by remember { mutableStateOf("") }
    var preferredDate by remember { mutableStateOf("Tomorrow, 2:00 PM") }
    var selectedPhotoUri by remember { mutableStateOf<Uri?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedPhotoUri = uri
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Screen Header
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
            Text(
                text = strings.repairStationTitle,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = strings.repairStationSubtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Sub Navigation Tabs
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.primary,
            modifier = Modifier.fillMaxWidth()
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text(strings.tabBookRepair, fontWeight = FontWeight.Bold) },
                modifier = Modifier.testTag("repair_tab_book")
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(strings.tabMyRepairs, fontWeight = FontWeight.Bold)
                        if (repairs.isNotEmpty()) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = CircleShape,
                                color = BrandTertiary,
                                modifier = Modifier.size(18.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = "${repairs.size}",
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                },
                modifier = Modifier.testTag("repair_tab_history")
            )
        }

        if (selectedTab == 0) {
            // Repair Inquiry Booking Form
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                Text(
                    text = strings.selectDeviceCategory,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Device Type Pills
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(bottom = 8.dp)
                ) {
                    items(RepairRequest.ITEM_TYPES) { type ->
                        val isSelected = type == selectedDeviceType
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            contentColor = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { selectedDeviceType = type }
                                .testTag("device_type_$type")
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                            ) {
                                Icon(
                                    imageVector = CategoryIconHelper.getIconForCategory(type),
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = strings.translateItemType(type),
                                    fontSize = 13.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Problem Title
                OutlinedTextField(
                    value = problemTitle,
                    onValueChange = { problemTitle = it; errorMessage = null },
                    label = { Text(strings.whatIsProblem) },
                    placeholder = { Text(if (strings.isGujarati) "દા.ત. ડિસ્પ્લે તૂટી ગયું, બેટરી ઉતરી જાય છે" else "e.g. Cracked screen, battery draining, won't charge") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("repair_problem_input"),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Problem Description
                OutlinedTextField(
                    value = problemDescription,
                    onValueChange = { problemDescription = it; errorMessage = null },
                    label = { Text(strings.describeIssue) },
                    placeholder = { Text(if (strings.isGujarati) "ડિવાઇસ પડી ગયું હતું કે પાણીમાં પડ્યું હતું તેની વિગત લખો..." else "Mention if device was dropped, exposed to water, or any recent symptoms...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("repair_desc_input"),
                    shape = RoundedCornerShape(12.dp),
                    minLines = 3,
                    maxLines = 5
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Upload Photo Section
                Text(
                    text = strings.uploadPhotoOptional,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))

                if (selectedPhotoUri != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp))
                    ) {
                        AsyncImage(
                            model = selectedPhotoUri,
                            contentDescription = "Uploaded device photo",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                        IconButton(
                            onClick = { selectedPhotoUri = null },
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(8.dp)
                                .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                        ) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Remove photo", tint = Color.White)
                        }
                    }
                } else {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            }
                            .testTag("repair_upload_photo_btn"),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AddAPhoto,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = strings.attachPhotoText,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 13.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Customer Name
                OutlinedTextField(
                    value = customerName,
                    onValueChange = { customerName = it; errorMessage = null },
                    label = { Text(strings.yourName) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("repair_name_input"),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Customer Phone
                OutlinedTextField(
                    value = customerPhone,
                    onValueChange = { customerPhone = it; errorMessage = null },
                    label = { Text(strings.phoneForUpdates) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("repair_phone_input"),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Preferred Date/Time
                OutlinedTextField(
                    value = preferredDate,
                    onValueChange = { preferredDate = it },
                    label = { Text(strings.preferredAppointmentTime) },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.CalendarMonth, contentDescription = null)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("repair_date_input"),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = errorMessage ?: "",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = {
                        if (problemTitle.isBlank() || problemDescription.isBlank() || customerName.isBlank() || customerPhone.isBlank()) {
                            errorMessage = if (strings.isGujarati) "કૃપા કરીને બધી જરૂરી માહિતી ભરો (*)" else "Please fill in all mandatory fields with an asterisk (*)."
                        } else {
                            onSubmitRepair(
                                customerName.trim(),
                                customerPhone.trim(),
                                selectedDeviceType,
                                problemTitle.trim(),
                                problemDescription.trim(),
                                preferredDate.trim(),
                                selectedPhotoUri?.toString()
                            ) {
                                problemTitle = ""
                                problemDescription = ""
                                selectedPhotoUri = null
                                selectedTab = 1
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("submit_repair_request_button"),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(imageVector = Icons.Default.Build, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = strings.submitRepairRequestBtn,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }

                Spacer(modifier = Modifier.height(80.dp))
            }
        } else {
            // My Repairs Tracking List
            if (repairs.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Build,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.outlineVariant
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = strings.noRepairsYet,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (strings.isGujarati) "તમારા ડિવાઇસનું રીપેરિંગ શરૂ કરવા માટે વિનંતી ફોર્મ ભરો." else "Fill in the repair inquiry form to get a diagnosis and technician update.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { selectedTab = 0 }) {
                            Text(strings.tabBookRepair)
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 80.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(repairs, key = { it.id }) { repair ->
                        CustomerRepairTicketCard(strings = strings, repair = repair)
                    }
                }
            }
        }
    }
}

@Composable
private fun CustomerRepairTicketCard(strings: AppStrings, repair: RepairRequest) {
    val dateFormat = remember { SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()) }

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("repair_ticket_${repair.id}")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header: ID & Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = BrandTertiary.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = repair.id,
                            fontWeight = FontWeight.ExtraBold,
                            color = BrandTertiary,
                            fontSize = 14.sp,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = dateFormat.format(Date(repair.createdAt)),
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                val isCompleted = repair.status == RepairRequest.STATUS_COMPLETED
                Surface(
                    color = if (isCompleted) SuccessGreen.copy(alpha = 0.15f) else MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = strings.translateRepairStatus(repair.status),
                        color = if (isCompleted) SuccessGreen else MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Device & Problem
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = CategoryIconHelper.getIconForCategory(repair.itemType),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "${strings.translateItemType(repair.itemType)}: ${repair.problemTitle}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = repair.problemDescription,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (repair.estimatedPrice != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    color = SuccessGreen.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "${strings.estimatedCost} ₹${repair.estimatedPrice.toInt()}",
                        color = SuccessGreen,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(12.dp))

            // 5-Stage Live Status Timeline
            Text(
                text = strings.repairProgress,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))

            val localizedStages = RepairRequest.ALL_STATUSES.map { strings.translateRepairStatus(it) }
            StatusTimeline(
                stages = localizedStages,
                currentStageIndex = repair.stageIndex
            )

            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${repair.customerName} (${repair.customerPhone})",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = repair.preferredDate,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
