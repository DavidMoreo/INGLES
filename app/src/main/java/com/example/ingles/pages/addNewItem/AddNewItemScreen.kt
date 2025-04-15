package com.example.ingles.pages.addNewItem

import EnglishModel
import android.content.Context
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.ingles.util.Constants
import com.example.tool.common.MainViewModel
import com.google.gson.Gson
import saveJsonToFile

@Composable
fun AddNewItemScreen(context: Context, viewModel: MainViewModel) {

    AddDialog(
        onDismiss = {
            viewModel.englishItem = EnglishModel(id="", ES = "", EN="",PRO="",COUNT = 0);
            viewModel.selectedScreen ="Home";
            viewModel.showDialog = false },
        onAdd = { kepPassword ->
            if(kepPassword.id=="") {
               Add(context, viewModel, kepPassword)
            }else {
                Update(context, viewModel, kepPassword)
            }
            viewModel.showDialog = false
        },
        viewModel,
        context
    )
}




@Composable
fun AddDialog(onDismiss: () -> Unit, onAdd: (EnglishModel) -> Unit, viewModel: MainViewModel, context: Context) {
    val opciones = Constants.TIPOS_PALABRAS
    val isEditing = viewModel.englishItem.id.isNotEmpty()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (isEditing) "Editar" else "Agregar") },
        text = {
            Column {
                OutlinedTextField(
                    value = viewModel.englishItem.ES,
                    onValueChange = { viewModel.englishItem = viewModel.englishItem.copy(ES = it) },
                    label = { Text("Español") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = viewModel.englishItem.EN,
                    onValueChange = {
                        viewModel.englishItem = viewModel.englishItem.copy(EN = it)
                    },
                    label = { Text("Inglés") },
                    modifier = Modifier.fillMaxWidth()
                )

                SelectDropdown(
                    options = opciones,
                    selectedOption = viewModel.englishItem.PRO,
                    onOptionSelected = { viewModel.englishItem = viewModel.englishItem.copy(PRO = it) }
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                val item = if (!isEditing) {
                    // Si es nuevo, generar un nuevo ID
                    viewModel.englishItem.copy(id = java.util.UUID.randomUUID().toString())
                } else {
                    // Si es edición, mantener el ID existente
                    viewModel.englishItem
                }
                onAdd(item)
                viewModel.englishItem = EnglishModel(id = "", ES = "", EN = "", PRO = "", COUNT = 0)
            }) {
                Text(if (isEditing) "Guardar" else "Agregar")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectDropdown(
    options: List<String>,
    selectedOption: String,
    onOptionSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        TextField(
            value = selectedOption,
            onValueChange = {},
            readOnly = true,
            label = { Text("Tipo") },
            trailingIcon = {

            },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { selectionOption ->
                DropdownMenuItem(
                    text = { Text(selectionOption) },
                    onClick = {
                        onOptionSelected(selectionOption)
                        expanded = false
                    }
                )
            }
        }
    }
}



fun Add(context: Context, viewModel: MainViewModel, value:EnglishModel){
    if ( value.ES!="" && value.EN!="") {
        val gson = Gson()
        value.ES =  value.ES ;
        value.EN =  value.EN ;
        value.PRO =  value.PRO ;
        viewModel.englishList.add(value);
        val jsonArray2 = gson.toJson(viewModel.englishList)

        saveJsonToFile(context, viewModel.fileNameEnglish,jsonArray2 )
        viewModel.selectedScreen ="Home";
    }
}

fun Update(context: Context, viewModel: MainViewModel, value: EnglishModel) {
    if (value.ES != "" && value.EN != "") {
        // Primero, eliminar el elemento existente
        viewModel.englishList.removeIf { it.id == value.id }
        
        // Luego agregar el elemento actualizado
        viewModel.englishList.add(value)
        
        // Guardar la lista actualizada
        val gson = Gson()
        val jsonArray2 = gson.toJson(viewModel.englishList)
        saveJsonToFile(context, viewModel.fileNameEnglish, jsonArray2)
        
        // Volver a la pantalla principal
        viewModel.selectedScreen = "Home"
    }
}

