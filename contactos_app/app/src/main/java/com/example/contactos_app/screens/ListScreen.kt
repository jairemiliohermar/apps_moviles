package com.example.contactos_app.screens

import android.content.Intent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.contactos_app.viewmodel.ContactViewModel
import androidx.core.net.toUri

val profileColors = listOf(
    Color(0xFFFFAB91),
    Color(0xFF81D4FA),
    Color(0xFFA5D6A7),
    Color(0xFFCE93D8),
    Color(0xFFFFCC80),
    Color(0xFF80CBC4),
    Color(0xFFF48FB1)
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ListScreen(
    navController: NavController,
    viewModel: ContactViewModel
) {
    val context = LocalContext.current
    val contacts by viewModel.contactList.collectAsState()
    var isSearching by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    val sortedContacts = contacts.sortedBy { it.name }
    val filteredContacts = sortedContacts.filter {
        it.name.contains(searchQuery, ignoreCase = true) ||
                it.phone.contains(searchQuery)
    }
    
    val groupedContacts = filteredContacts.groupBy { it.name.firstOrNull()?.uppercase() ?: "#" }
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    Scaffold(
        modifier = Modifier
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            if (isSearching) {
                TopAppBar(
                    title = {
                        TextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = { Text("Buscar contacto...") },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 48.dp)
                                .padding(vertical = 4.dp),
                            shape = CircleShape,
                            colors = TextFieldDefaults.colors(
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                                disabledIndicatorColor = Color.Transparent,
                                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f),
                                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f)
                            ),
                            trailingIcon = {
                                if (searchQuery.isNotEmpty()) {
                                    IconButton(onClick = { searchQuery = "" }) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Limpiar"
                                        )
                                    }
                                }
                            }
                        )
                    },
                    actions = {
                        IconButton(
                            onClick = {
                                isSearching = false
                                searchQuery = ""
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Cerrar búsqueda"
                            )
                        }
                    }
                )

            } else {
                LargeTopAppBar(
                    navigationIcon = {
                        Spacer(modifier = Modifier.width(48.dp))
                    },
                    title = {
                        Text(
                            text = "Contactos",
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.headlineLarge
                        )
                    },

                    actions = {
                        IconButton(
                            onClick = { isSearching = true }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Buscar"
                            )
                        }
                    },
                    scrollBehavior = scrollBehavior
                )
            }
        },

        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate("form") },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Agregar")
            }
        }
    ) { padding ->
        when {
            contacts.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),

                    contentAlignment = Alignment.Center
                ) {
                    Text("No hay contactos registrados")
                }
            }

            filteredContacts.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No se encontraron resultados")
                }
            }
            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    groupedContacts.forEach { (initial, contactsInGroup) ->
                        stickyHeader {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(MaterialTheme.colorScheme.surface)
                                    .padding(horizontal = 24.dp, vertical = 8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .background(Color(0xFFD1E4FF), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = initial,
                                        style = MaterialTheme.typography.titleSmall,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                        
                        item(key = initial) {
                            Card(
                                modifier = Modifier
                                    .padding(horizontal = 16.dp, vertical = 8.dp)
                                    .fillMaxWidth(),
                                shape = MaterialTheme.shapes.medium,
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surface
                                ),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                Column {
                                    contactsInGroup.forEachIndexed { index, contact ->
                                        val swipeToDismissState = rememberSwipeToDismissBoxState(
                                            confirmValueChange = {
                                                when (it) {
                                                    SwipeToDismissBoxValue.StartToEnd -> {
                                                        val intent = Intent(Intent.ACTION_DIAL).apply {
                                                            data = "tel:${contact.phone}".toUri()
                                                        }
                                                        context.startActivity(intent)
                                                        false
                                                    }
                                                    SwipeToDismissBoxValue.EndToStart -> {
                                                        val intent = Intent(Intent.ACTION_SENDTO).apply {
                                                            data = "smsto:${contact.phone}".toUri()
                                                        }
                                                        context.startActivity(intent)
                                                        false
                                                    }
                                                    else -> false
                                                }
                                            }
                                        )

                                        SwipeToDismissBox(
                                            state = swipeToDismissState,
                                            backgroundContent = {
                                                val direction = swipeToDismissState.dismissDirection
                                                val color = when (direction) {
                                                    SwipeToDismissBoxValue.StartToEnd -> Color(0xFF4CAF50)
                                                    SwipeToDismissBoxValue.EndToStart -> Color(0xFF2196F3)
                                                    else -> Color.Transparent
                                                }
                                                val alignment = when (direction) {
                                                    SwipeToDismissBoxValue.StartToEnd -> Alignment.CenterStart
                                                    SwipeToDismissBoxValue.EndToStart -> Alignment.CenterEnd
                                                    else -> Alignment.Center
                                                }
                                                val icon = when (direction) {
                                                    SwipeToDismissBoxValue.StartToEnd -> Icons.Default.Call
                                                    SwipeToDismissBoxValue.EndToStart -> Icons.Default.Email
                                                    else -> null
                                                }

                                                Box(
                                                    modifier = Modifier
                                                        .fillMaxSize()
                                                        .background(color)
                                                        .padding(horizontal = 20.dp),
                                                    contentAlignment = alignment
                                                ) {
                                                    icon?.let {
                                                        Icon(
                                                            imageVector = it,
                                                            contentDescription = null,
                                                            tint = Color.White
                                                        )
                                                    }
                                                }
                                            }
                                        ) {
                                            val globalIndex = filteredContacts.indexOf(contact)
                                            val profileColor = profileColors[if (globalIndex != -1) globalIndex % profileColors.size else 0]
                                            
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .background(MaterialTheme.colorScheme.surface)
                                                    .clickable {
                                                        navController.navigate("detail/${contact.id}")
                                                    }
                                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                if (contact.photo.isNotEmpty()) {
                                                    Image(
                                                        painter = rememberAsyncImagePainter(contact.photo),
                                                        contentDescription = null,
                                                        modifier = Modifier
                                                            .size(60.dp)
                                                            .clip(CircleShape)
                                                    )
                                                } else {
                                                    Box(
                                                        contentAlignment = Alignment.Center,
                                                        modifier = Modifier
                                                            .size(60.dp)
                                                            .clip(CircleShape)
                                                            .background(profileColor)
                                                    ) {
                                                        Text(
                                                            text = contact.name.firstOrNull()?.uppercase() ?: "",
                                                            color = Color.White
                                                        )
                                                    }
                                                }
                                                Spacer(modifier = Modifier.width(16.dp))
                                                Column {
                                                    Text(
                                                        text = contact.name,
                                                        style = MaterialTheme.typography.titleMedium
                                                    )
                                                    Spacer(modifier = Modifier.height(4.dp))
                                                    Text(
                                                        text = contact.phone,
                                                        style = MaterialTheme.typography.bodyMedium,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                }
                                            }
                                        }
                                        
                                        if (index < contactsInGroup.size - 1) {
                                            HorizontalDivider(
                                                modifier = Modifier.padding(horizontal = 16.dp),
                                                thickness = 0.5.dp,
                                                color = Color(0xFFE0E0E0)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
