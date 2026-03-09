package com.example.contactos_app.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.contactos_app.components.Input
import com.example.contactos_app.data.Contact
import com.example.contactos_app.validation.AuthValidator
import com.example.contactos_app.validation.ValidationResult
import com.example.contactos_app.viewmodel.ContactViewModel
import androidx.compose.ui.input.nestedscroll.nestedScroll

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormScreen(
    navController: NavController,
    viewModel: ContactViewModel,
    id: Int?
) {

    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()

    val contacts by viewModel.contactList.collectAsState()
    val contactToEdit = contacts.find { it.id == id }

    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var photoUri by remember { mutableStateOf<Uri?>(null) }

    LaunchedEffect(contactToEdit) {
        if (contactToEdit != null) {
            name = contactToEdit.name
            phone = contactToEdit.phone
            email = contactToEdit.email
            if (contactToEdit.photo.isNotEmpty()) {
                photoUri = Uri.parse(contactToEdit.photo)
            }
        }
    }

    var nameError by remember { mutableStateOf<String?>(null) }
    var phoneError by remember { mutableStateOf<String?>(null) }
    var emailError by remember { mutableStateOf<String?>(null) }

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        photoUri = uri
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),

        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (id == null) "Nuevo Contacto" else "Editar Contacto",
                        color = Color.White
                    )
                },

                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),

                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Regresar",
                            tint = Color.White
                        )
                    }
                },
                scrollBehavior = scrollBehavior
            )
        }

    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.primary)
                    .padding(vertical = 40.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (photoUri != null) {
                        Image(
                            painter = rememberAsyncImagePainter(photoUri),
                            contentDescription = null,
                            modifier = Modifier
                                .size(120.dp)
                                .clip(CircleShape)
                                .clickable { imagePicker.launch("image/*") }
                        )
                    } else {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(120.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.25f))
                                .clickable { imagePicker.launch("image/*") }
                        ) {
                            if (name.isNotEmpty()) {
                                Text(
                                    text = name.firstOrNull()?.uppercase() ?: "",
                                    color = Color.White,
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = "FP por default",
                                    modifier = Modifier.size(80.dp),
                                    tint = Color.White
                                )
                            }
                        }
                    }

                    if (name.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = name,
                            color = Color.White,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Input(
                    value = name,
                    onValueChange = {
                        name = it
                        nameError = null
                    },
                    label = "Nombre",
                    isError = nameError != null,
                    errorMessage = nameError,
                    leadingIcon = {
                        Icon(Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    }
                )
                Input(
                    value = phone,
                    onValueChange = {
                        phone = it
                        phoneError = null
                    },
                    label = "Móvil",
                    keyboardType = KeyboardType.Number,
                    isError = phoneError != null,
                    errorMessage = phoneError,
                    leadingIcon = {
                        Icon(Icons.Default.Call, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    }
                )
                Input(
                    value = email,
                    onValueChange = {
                        email = it
                        emailError = null
                    },
                    label = "Correo",
                    keyboardType = KeyboardType.Email,
                    isError = emailError != null,
                    errorMessage = emailError,
                    leadingIcon = {
                        Icon(Icons.Default.Email, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    }
                )

                Button(
                    onClick = {

                        when (val result =
                            AuthValidator.validateContact(name, phone, email)
                        ) {

                            is ValidationResult.Success -> {

                                val contact = Contact(
                                    id = id ?: 0,
                                    name = name,
                                    phone = phone,
                                    email = email,
                                    photo = photoUri?.toString() ?: ""
                                )

                                if (id == null) {
                                    viewModel.addContact(contact)
                                } else {
                                    viewModel.updateContact(contact)
                                }

                                navController.popBackStack()
                            }

                            is ValidationResult.Errors -> {

                                nameError = result.fieldErrors["name"]
                                phoneError = result.fieldErrors["phone"]
                                emailError = result.fieldErrors["email"]
                            }
                        }
                    },

                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (id == null) "Guardar" else "Actualizar")
                }

            }

        }
    }
}
