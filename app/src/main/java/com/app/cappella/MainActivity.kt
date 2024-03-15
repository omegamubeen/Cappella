package com.app.cappella

import android.app.DatePickerDialog
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberImagePainter
import coil.transform.CircleCropTransformation
import com.app.cappella.model.BabyProfile
import com.app.cappella.model.BabyProfileState
import com.app.cappella.viewmodel.BabyProfileViewModel
import java.net.URI
import java.util.Calendar

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyApp {
                val babyProfileViewModel: BabyProfileViewModel = viewModel()
                BabyProfileScreen(babyProfileViewModel)
            }
        }
    }
}

@Composable
fun MyApp(content: @Composable () -> Unit) {
    MaterialTheme {
        Surface(color = Color.White) {
            content()
        }
    }
}

@Composable
fun BabyProfileScreen(babyProfileViewModel: BabyProfileViewModel) {
    val babyProfileState by babyProfileViewModel.babyProfile.collectAsState()

    when (babyProfileState) {
        is BabyProfileState.Loading -> {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                CircularProgressIndicator()
            }
        }
        is BabyProfileState.Success -> {
            val babyProfile = (babyProfileState as BabyProfileState.Success).babyProfile
            ProfileUI(babyProfile, babyProfileViewModel)
        }
        is BabyProfileState.Error -> {
            Text((babyProfileState as BabyProfileState.Error).message)
        }
        null -> {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                CircularProgressIndicator()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileUI(babyProfile: BabyProfile, viewModel: BabyProfileViewModel) {
    val context = LocalContext.current
    var name by remember { mutableStateOf(babyProfile.name ?: "") }
    var dob by remember { mutableStateOf(babyProfile.dob ?: "") }
    var gender by remember { mutableStateOf(babyProfile.gender ?: "") }

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("Baby's profile") },
            navigationIcon = {
                Icon(
                    imageVector = Icons.Filled.ArrowBack,
                    contentDescription = null,
                    modifier = Modifier.clickable(onClick = {})
                )
            }
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            ProfilePicture(babyProfile.profile_picture) {
                viewModel.updateBabyProfile(context, babyProfile.id, name, dob, gender, it)
            }
            FormField(label = "Name / Nickname *", value = name, onValueChange = { name = it }, modifier = Modifier.fillMaxWidth())
            DatePickerField(label = "Date of birth", value = dob, onDateSelected = { dob = it }, modifier = Modifier.fillMaxWidth())
            GenderDropdown(selectedGender = gender, onGenderSelect = { gender = it })
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    viewModel.updateBabyProfile(context, babyProfile.id, name, dob, gender, null)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Text("Update Profile")
            }
        }
    }
}

@Composable
fun DatePickerField(label: String, value: String, onDateSelected: (String) -> Unit, modifier: Modifier) {
    val context = LocalContext.current
    var showDialog by remember { mutableStateOf(false) }

    // Listen for showDialog changes to display the DatePickerDialog
    if (showDialog) {
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        val currentMonth = Calendar.getInstance().get(Calendar.MONTH)
        val currentDayOfMonth = Calendar.getInstance().get(Calendar.DAY_OF_MONTH)

        DatePickerDialog(
            context, { _, year, monthOfYear, dayOfMonth ->
                // Format the selected date and pass it to onDateSelected
                onDateSelected("$year-${monthOfYear + 1}-${dayOfMonth}")
                showDialog = false
            }, currentYear, currentMonth, currentDayOfMonth
        ).show()
    }

    OutlinedTextField(
        value = value,
        onValueChange = {}, // No action on value change as it's read-only
        label = { Text(label) },
        readOnly = true, // Make the text field read-only
        modifier = modifier
            .clickable { showDialog = true }, // Show dialog on click
    )
}


@Composable
fun FormField(label: String, value: String, onValueChange: (String) -> Unit, modifier: Modifier = Modifier) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = modifier.padding(top = 20.dp),
        keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next)
    )
}

@Composable
fun ProfilePicture(profileImageUrl: String, onImageSelected: (Uri) -> Unit) {
    var imageUri by remember { mutableStateOf<Uri?>(null) }

    val launcher = rememberLauncherForActivityResult(contract = ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            imageUri = it
            onImageSelected(it)
        }
    }

    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        imageUri?.let {
            Image(
                painter = rememberImagePainter(it),
                contentDescription = null,
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
            )
        } ?: Image(
            painter = rememberImagePainter(data = profileImageUrl, builder = {
                transformations(CircleCropTransformation())
            }),
            contentDescription = null,
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
        )

        Text(
            text = "Change profile picture",
            modifier = Modifier
                .clickable {
                    launcher.launch("image/*")
                }
                .padding(top = 8.dp)
        )
    }
}

@Composable
fun GenderDropdown(selectedGender: String, onGenderSelect: (String) -> Unit) {
    val items = listOf("male", "female")
    var expanded by remember { mutableStateOf(false) }
    var selectedIndex by remember {
        mutableStateOf(items.indexOfFirst { it.equals(selectedGender, ignoreCase = true) }.coerceAtLeast(0))
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = Color.Gray,
                shape = RoundedCornerShape(8.dp)
            )
            .clip(RoundedCornerShape(8.dp)) // Clip to the border shape
            .background(Color.White)
            .clickable(onClick = { expanded = true })
            .padding(16.dp)
    ) {
        Text(
            text = items[selectedIndex].replaceFirstChar { if (it.isLowerCase()) it.titlecase(java.util.Locale.ENGLISH) else it.toString() },
            modifier = Modifier.fillMaxWidth()
        )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.fillMaxWidth().background(Color.White, RoundedCornerShape(8.dp))
        ) {
            items.forEachIndexed { index, gender ->
                DropdownMenuItem(
                    text = {
                        Text(gender.replaceFirstChar { if (it.isLowerCase()) it.titlecase(java.util.Locale.ENGLISH) else it.toString() })
                    },
                    onClick = {
                        selectedIndex = index
                        expanded = false
                        onGenderSelect(gender)
                    },
                    modifier = Modifier.background(Color.White)
                )
            }
        }
    }
}

