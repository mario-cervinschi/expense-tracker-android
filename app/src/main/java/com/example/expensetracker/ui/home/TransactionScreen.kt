package com.example.expensetracker.ui.home

import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.example.expensetracker.R
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun copyUriToInternalStorage(context: android.content.Context, sourceUri: Uri): Uri? {
    return try {
        val inputStream = context.contentResolver.openInputStream(sourceUri) ?: return null
        val fileName = "IMG_${System.currentTimeMillis()}.jpg"
        val file = java.io.File(context.filesDir, fileName)
        val outputStream = java.io.FileOutputStream(file)
        inputStream.use { input -> outputStream.use { output -> input.copyTo(output) } }
        Uri.fromFile(file)
    } catch (e: Exception) {
        Log.e("FileCopy", "Error copying file", e)
        null
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun TransactionScreen(transactionId: String?, onClose: () -> Unit) {
    val transactionViewModel = viewModel<TransactionViewModel>(
        factory = TransactionViewModel.Factory(transactionId)
    )
    val transactionUiState = transactionViewModel.uiState
    val context = LocalContext.current

    var title by rememberSaveable { mutableStateOf("") }
    var dateInMillis by rememberSaveable { mutableStateOf(System.currentTimeMillis()) }
    var sumString by rememberSaveable { mutableStateOf("") }
    var income by rememberSaveable { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }

    var currentImageUri by rememberSaveable { mutableStateOf<Uri?>(null) }
    var showCameraPreview by remember { mutableStateOf(false) }

    val cameraPermissionState = rememberPermissionState(
        android.Manifest.permission.CAMERA
    )

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val savedUri = copyUriToInternalStorage(context, it)
            currentImageUri = savedUri
        }
    }

    val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }

    val backgroundColor by animateColorAsState(
        targetValue = if (income) Color(0xFFE8F5E9) else Color(0xFFFFEBEE),
        animationSpec = tween(durationMillis = 500),
        label = "background_color"
    )

    val accentColor by animateColorAsState(
        targetValue = if (income) Color(0xFF4CAF50) else Color(0xFFF44336),
        animationSpec = tween(durationMillis = 500),
        label = "accent_color"
    )

    LaunchedEffect(transactionUiState.submitResult) {
        if (transactionUiState.submitResult is com.example.expensetracker.data.Result.Success) {
            onClose()
        }
    }

    var fieldsInitialized by remember { mutableStateOf(transactionId == null) }
    LaunchedEffect(transactionId, transactionUiState.loadResult) {
        if (fieldsInitialized) return@LaunchedEffect

        if (transactionUiState.loadResult is com.example.expensetracker.data.Result.Success) {
            val transaction = transactionUiState.transaction
            if (transactionId != null && transaction._id != transactionId) return@LaunchedEffect

            title = transaction.title
            dateInMillis = transaction.date.time
            sumString = transaction.sum.toString()
            income = transaction.income
            fieldsInitialized = true

            if (transaction.imagePath.isNotEmpty() && transaction.imagePath != "null") {
                currentImageUri = Uri.parse(transaction.imagePath)
            } else {
                currentImageUri = null
            }
        }
    }

    if (showCameraPreview) {
        CameraView(
            onImageCaptured = { uri ->
                currentImageUri = uri
                showCameraPreview = false
            },
            onError = { exc -> showCameraPreview = false },
            onClose = { showCameraPreview = false }
        )
    } else {
        if (showDatePicker) {
            val datePickerState = rememberDatePickerState(initialSelectedDateMillis = dateInMillis)
            DatePickerDialog(
                onDismissRequest = { showDatePicker = false },
                confirmButton = { Button(onClick = { datePickerState.selectedDateMillis?.let { dateInMillis = it }; showDatePicker = false }) { Text("OK") } },
                dismissButton = { Button(onClick = { showDatePicker = false }) { Text("Cancel") } }
            ) { DatePicker(state = datePickerState) }
        }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(text = stringResource(id = if (transactionId != null) R.string.transactionEdit else R.string.transactionAdd))
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent,
                    ),
                    navigationIcon = {
                        IconButton(onClick = onClose) {
                            Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Go back")
                        }
                    },
                    actions = {
                        if (transactionId != null) {
                            IconButton(onClick = { transactionViewModel.deleteTransaction() }) {
                                Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red)
                            }
                        }
                    }
                )
            },
            bottomBar = {
                Surface(
                    shadowElevation = 8.dp
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Button(
                            onClick = {
                                val date = Date(dateInMillis)
                                val sum = sumString.toDoubleOrNull() ?: 0.0
                                val imagePath = currentImageUri?.toString() ?: ""
                                transactionViewModel.saveOrUpdateItem(title, date, sum, income, imagePath)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = accentColor)
                        ) {
                            Text("Save", style = MaterialTheme.typography.titleMedium)
                        }
                    }
                }
            }
        ) { innerPadding ->
            val scrollState = rememberScrollState()

            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(16.dp)
            ) {
                if (transactionUiState.loadResult is com.example.expensetracker.data.Result.Loading) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
                    return@Scaffold
                }

                if (transactionUiState.submitResult is com.example.expensetracker.data.Result.Loading) {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth(), color = accentColor)
                }

                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = income,
                            onCheckedChange = { income = it },
                            colors = CheckboxDefaults.colors(checkedColor = accentColor)
                        )
                        Text(
                            text = if (income) "Income" else "Expense",
                            modifier = Modifier.padding(start = 8.dp),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = accentColor
                        )
                    }
                }

                TextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title") },
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    colors = TextFieldDefaults.colors(focusedIndicatorColor = accentColor, focusedLabelColor = accentColor)
                )

                TextField(
                    value = dateFormat.format(Date(dateInMillis)),
                    onValueChange = { },
                    label = { Text("Date") },
                    readOnly = true,
                    enabled = false,
                    colors = TextFieldDefaults.colors(
                        disabledTextColor = MaterialTheme.colorScheme.onSurface,
                        disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        disabledTrailingIconColor = accentColor
                    ),
                    trailingIcon = {
                        IconButton(onClick = { showDatePicker = true }) {
                            Icon(imageVector = Icons.Default.DateRange, contentDescription = "Select date", tint = accentColor)
                        }
                    },
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp).clickable { showDatePicker = true }
                )

                TextField(
                    value = sumString,
                    onValueChange = { sumString = it },
                    label = { Text("Sum") },
                    placeholder = { Text("0.00") },
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    colors = TextFieldDefaults.colors(focusedIndicatorColor = accentColor, focusedLabelColor = accentColor)
                )

                Spacer(modifier = Modifier.height(16.dp))

                AnimatedVisibility(
                    visible = currentImageUri != null,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    Box(modifier = Modifier.fillMaxWidth().height(400.dp).clip(RoundedCornerShape(12.dp))) {
                        Image(
                            painter = rememberAsyncImagePainter(currentImageUri),
                            contentDescription = "Receipt photo",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                        IconButton(
                            onClick = { currentImageUri = null },
                            modifier = Modifier.align(Alignment.TopEnd).padding(8.dp)
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "Remove photo", tint = Color.White)
                        }
                    }
                }

                AnimatedVisibility(
                    visible = currentImageUri == null,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = {
                                if (cameraPermissionState.status.isGranted) showCameraPreview = true
                                else cameraPermissionState.launchPermissionRequest()
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Take")
                        }

                        Button(
                            onClick = { galleryLauncher.launch("image/*") },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.AddCircle, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Choose")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}