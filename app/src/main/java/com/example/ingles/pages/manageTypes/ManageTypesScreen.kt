package com.example.ingles.pages.manageTypes

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.ingles.util.Constants
import com.example.tool.common.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageTypesScreen(viewModel: MainViewModel) {
    var showAddDialog by remember { mutableStateOf(false) }
    var newType by remember { mutableStateOf("") }
    val tipos = remember { Constants.TIPOS_PALABRAS }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Gestionar Tipos") },
                navigationIcon = {
                    IconButton(onClick = { viewModel.selectedScreen = "Home" }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            // Botón de agregar tipo en la parte superior
            Button(
                onClick = { showAddDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(
                    Icons.Filled.Add,
                    contentDescription = "Agregar tipo",
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text("Agregar nuevo tipo")
            }

            LazyColumn {
                items(tipos.filter { it != "Todos" }) { tipo ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(tipo)
                            IconButton(
                                onClick = { Constants.eliminarTipo(tipo) }
                            ) {
                                Icon(
                                    Icons.Filled.Delete,
                                    contentDescription = "Eliminar tipo",
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Agregar nuevo tipo") },
            text = {
                OutlinedTextField(
                    value = newType,
                    onValueChange = { newType = it },
                    label = { Text("Nombre del tipo") },
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (newType.isNotBlank()) {
                            Constants.agregarTipo(newType)
                            newType = ""
                            showAddDialog = false
                        }
                    }
                ) {
                    Text("Agregar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
} 