package com.example.ingles

import EnglishModel
import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import com.example.ingles.pages.addNewItem.AddNewItemScreen
import com.example.ingles.pages.manageTypes.ManageTypesScreen
import com.example.ingles.ui.theme.INGLESTheme
import com.example.ingles.util.Constants
import com.example.tool.common.MainViewModel
import com.google.gson.Gson
import saveJsonToFile
import java.util.*

class MainActivity : ComponentActivity(), TextToSpeech.OnInitListener {
    private lateinit var tts: TextToSpeech
    private var speechRate by mutableStateOf(0.1f)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        tts = TextToSpeech(this, this)
        val context: Context = this
        val viewModel: MainViewModel by viewModels()
        viewModel.LoadData(context, viewModel)
        
        setContent {
            INGLESTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = { BottomNavigationBar(viewModel) { viewModel.selectedScreen = it } }
                ) { innerPadding ->
                    Box(modifier = Modifier.padding(10.dp)) {
                        when (viewModel.selectedScreen) {
                            "Add" -> AddNewItemScreen(context, viewModel)
                            "Types" -> ManageTypesScreen(viewModel)
                            "Home" -> PhraseListApp(
                                tts = tts,
                                speechRate = speechRate,
                                onSpeechRateChange = { newRate -> speechRate = newRate },
                                onSpeak = { text -> tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, null) },
                                modifier = Modifier.padding(innerPadding),
                                context = context,
                                viewModel = viewModel
                            )
                        }
                    }
                }
            }
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts.setLanguage(Locale.US)
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Toast.makeText(this, "Language not supported", Toast.LENGTH_SHORT).show()
            } else {
                tts.setSpeechRate(speechRate)
            }
        } else {
            Toast.makeText(this, "TTS initialization failed", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroy() {
        if (::tts.isInitialized) {
            tts.stop()
            tts.shutdown()
        }
        super.onDestroy()
    }
}

@Composable
fun PhraseListApp(
    tts: TextToSpeech,
    speechRate: Float,
    onSpeechRateChange: (Float) -> Unit,
    onSpeak: (String) -> Unit,
    modifier: Modifier = Modifier,
    context: Context,
    viewModel: MainViewModel
) {
    var showSpeechRecognition by remember { mutableStateOf(false) }
    val context = LocalContext.current
    var selectedTabIndex by remember { mutableStateOf(0) }

    // Lanzador para el reconocimiento de voz
    val speechLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val spokenText = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.get(0)
            Toast.makeText(context, "You said: $spokenText", Toast.LENGTH_SHORT).show()
        }
    }

    Column(modifier = modifier) {
        // TabRow con scroll horizontal
        ScrollableTabRow(
            selectedTabIndex = selectedTabIndex,
            edgePadding = 0.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Constants.TIPOS_PALABRAS.forEachIndexed { index, tipo ->
                Tab(
                    text = { Text(tipo) },
                    selected = selectedTabIndex == index,
                    onClick = { selectedTabIndex = index }
                )
            }
        }

        // Control deslizante para la velocidad
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 16.dp)
        ) {
            Text("Speed: ${speechRate}x")
            Slider(
                value = speechRate,
                onValueChange = { newRate ->
                    onSpeechRateChange(newRate)
                    tts.setSpeechRate(newRate)
                },
                valueRange = 0.1f..2.0f,
                steps = 100
            )
        }

        // Lista filtrada por tipo
        LazyColumn(modifier = Modifier.padding(horizontal = 16.dp)) {
            val filteredList = if (selectedTabIndex == 0) {
                viewModel.englishList
            } else {
                viewModel.englishList.filter { it.PRO == Constants.TIPOS_PALABRAS[selectedTabIndex] }
            }

            items(filteredList) { item ->
                PhraseItem(
                    english = item.EN,
                    spanish = item.ES,
                    onClick = { onSpeak(item.EN) },
                    onSpeakClick = {
                        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.US.toString())
                            putExtra(RecognizerIntent.EXTRA_PROMPT, "Say the phrase in English")
                        }
                        speechLauncher.launch(intent)
                    },
                    onWordClick = { e -> onSpeak(e) },
                    context = context,
                    viewModel = viewModel,
                    item = item
                )
            }
        }
    }
}

@Composable
fun PhraseItem1(
    english: String,
    spanish: String,
    onClick: () -> Unit,
    onSpeakClick: () -> Unit,
    context: Context,
    viewModel: MainViewModel,
    item:EnglishModel
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = english, style = MaterialTheme.typography.headlineSmall)
            Text(text = spanish, style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = {Remove(context=context,viewModel=viewModel,item )}) {
                Text("Eliminar")
            }

            Button(onClick = onSpeakClick) {
                Text("Speak and Check")
            }
        }
    }
}


@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PhraseItem(
    english: String,
    spanish: String,
    onClick: () -> Unit,
    onSpeakClick: () -> Unit,
    onWordClick: (String) -> Unit,
    context: Context,
    viewModel: MainViewModel,
    item: EnglishModel
) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            ClickableWordsText(
                text = english,
                style = MaterialTheme.typography.headlineSmall,
                onWordClick = onWordClick
            )

            Text(text = spanish, style = MaterialTheme.typography.bodyMedium)
            Text(text = "Tipo: ${item.PRO}", style = MaterialTheme.typography.bodySmall)
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Button(
                    onClick = { showDeleteDialog = true },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Eliminar")
                }
                Button(
                    onClick = {
                        viewModel.englishItem = item
                        viewModel.selectedScreen = "Add"
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text("Editar")
                }
                IconButton(
                    onClick = onSpeakClick,
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            MaterialTheme.colorScheme.secondary,
                            shape = RoundedCornerShape(8.dp)
                        )
                ) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = "Speak and Check",
                        tint = MaterialTheme.colorScheme.onSecondary
                    )
                }
            }
        }
    }

    ConfirmDeleteDialog(
        showDialog = showDeleteDialog,
        onDismiss = { showDeleteDialog = false },
        onConfirm = { Remove(context = context, viewModel = viewModel, item) }
    )
}

@ExperimentalLayoutApi
@Composable
fun ClickableWordsText(
    text: String,
    style: TextStyle,
    onWordClick: (String) -> Unit
) {
    val words = text.split(" ")

    // Usamos FlowRow en lugar de Row para manejar el desbordamiento
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start,
        verticalArrangement = Arrangement.Center,
        maxItemsInEachRow = Int.MAX_VALUE // Sin límite de elementos por fila
    ) {
        words.forEachIndexed { index, word ->
            Box(
                modifier = Modifier
                    .clickable { onWordClick(word) }
                    .padding(end = 4.dp, bottom = 4.dp).height(40.dp)
                    .background(
                        color = Color.Gray.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(1.dp)
                    )
                    .border(
                        width = 1.dp,
                        color = Color.Gray.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(4.dp)
                    )
                    .padding(horizontal = 4.dp)
            ) {
                Text(
                    text = word,
                    style = style,
                    modifier = Modifier.padding(4.dp))
            }
        }
    }
}


@Composable
fun PreviewPhraseListApp(context: Context,
                         viewModel: MainViewModel) {

    INGLESTheme {
        PhraseListApp(
            tts = TextToSpeech(null, null),
            speechRate = 1.0f,
            onSpeechRateChange = {},
            onSpeak = {},
            context = context,
            viewModel = viewModel
        )
    }
}

@Composable
fun BottomNavigationBar(viewModel: MainViewModel, onScreenSelected: (String) -> Unit) {
    NavigationBar {
        NavigationBarItem(
            icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
            label = { Text("Home") },
            selected = viewModel.selectedScreen == "Home",
            onClick = { onScreenSelected("Home") }
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.Add, contentDescription = "Add") },
            label = { Text("Add") },
            selected = viewModel.selectedScreen == "Add",
            onClick = { onScreenSelected("Add") }
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.Settings, contentDescription = "Types") },
            label = { Text("Types") },
            selected = viewModel.selectedScreen == "Types",
            onClick = { onScreenSelected("Types") }
        )
    }
}

@Composable
fun ConfirmDeleteDialog(
    showDialog: Boolean,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    if (showDialog) {
        AlertDialog(
            onDismissRequest = { onDismiss() },
            title = {
                Text(text = "¿Estás seguro?")
            },
            text = {
                Text("Esta acción eliminará el elemento. ¿Quieres continuar?")
            },
            confirmButton = {
                TextButton(onClick = {
                    onConfirm()
                    onDismiss()
                }) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                TextButton(onClick = { onDismiss() }) {
                    Text("Cancelar")
                }
            }
        )
    }
}



fun Remove(context: Context, viewModel: MainViewModel, value:EnglishModel){
  //  if ( value.id!="") {
        viewModel.englishList.remove(value);
        val gson = Gson()
        val jsonArray2 = gson.toJson(viewModel.englishList)
        saveJsonToFile(context, viewModel.fileNameEnglish, jsonArray2);
    //}
}

