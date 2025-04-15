import android.content.Context
import android.os.Environment
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import org.json.JSONObject
import java.io.File
import java.io.FileWriter

fun saveJsonToFile(context: Context, fileName: String, jsonData: String,storageApp: Boolean=false) {
    if(!storageApp){
        createFolderInExternalStorage(context);
        createTextFileInExternalStorage(context,fileName, jsonData );
    }else{
        createFolderInAppStorage(context);
        createTextFileInAppStorage(context,fileName, jsonData );

    }
}

// Leer un JSON desde un archivo (Read)
fun readJsonFromFile(context: Context, fileName: String,storageApp: Boolean=false): String {
    if(!storageApp) {
        val jsonString = readTextFileFromExternalStorage(context, fileName);
        return jsonString
    }else{
        val jsonString = readTextFileFromAppStorage(context, fileName);
        return jsonString
    }
    return  "";
}

// Actualizar un JSON en un archivo (Update)
fun updateJsonInFile(context: Context, fileName: String, jsonData: JSONObject) {
    val file = File(context.filesDir, fileName)
    if (file.exists()) {
        val fileWriter = FileWriter(file)
        fileWriter.write(jsonData.toString())
        fileWriter.close()
    }
}

// Eliminar un archivo JSON (Delete)
fun deleteJsonFile(context: Context, fileName: String) {
    val file = File(context.filesDir, fileName)
    if (file.exists()) {
        file.delete()
    }
}


@Composable
fun RequestStoragePermission(context:Context) {

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            // Permiso concedido, puedes crear la carpeta
            createFolderInExternalStorage(context)
        } else {
            // Permiso denegado, maneja el caso
            Toast.makeText(context, "Permiso denegado", Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(Unit) {
        // launcher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    }
}

fun createFolderInExternalStorage(context: Context) {
    val folderName = "ES_IN_BD"
    val storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
    val miCarpeta = File(storageDir, folderName)

    if (!miCarpeta.exists()) {
        if (miCarpeta.mkdirs()) {
            Toast.makeText(context, "Carpeta creada: ${miCarpeta.path}", Toast.LENGTH_SHORT).show()
        } else {
            //  Toast.makeText(context, "Error al crear la carpeta", Toast.LENGTH_SHORT).show()
        }
    } else {
        //  Toast.makeText(context, "La carpeta ya existe", Toast.LENGTH_SHORT).show()
    }
}

fun createTextFileInExternalStorage(context: Context, fileName: String, content: String) {
    val folderName = "ES_IN_BD"
    val storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
    val miCarpeta = File(storageDir, folderName)

    // Asegurar que la carpeta existe
    if (!miCarpeta.exists()) {
        miCarpeta.mkdirs()
    }

    val archivo = File(miCarpeta, "$fileName.txt")

    try {
        archivo.writeText(content)
        //  Toast.makeText(context, "Archivo creado: ${archivo.path}", Toast.LENGTH_SHORT).show()
    } catch (e: Exception) {
        //  Toast.makeText(context, "Error al crear archivo: ${e.message}", Toast.LENGTH_SHORT).show()
    }
}

fun readTextFileFromExternalStorage(context: Context, fileName: String): String {
    val folderName = "ES_IN_BD"
    val storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
    val miCarpeta = File(storageDir, folderName) // Crear referencia a la carpeta
    val archivo = File(miCarpeta, "$fileName.txt") // Archivo dentro de la carpeta

    return if (archivo.exists()) {
        try {
            archivo.readText()
        } catch (e: Exception) {
            //  Toast.makeText(context, "Error al leer el archivo: ${e.message}", Toast.LENGTH_SHORT).show()
            ""
        }
    } else {
        // Toast.makeText(context, "El archivo no existe", Toast.LENGTH_SHORT).show()
        ""
    }
}



fun createTextFileInAppStorage(context: Context, fileName: String, content: String) {
    val folderName = "ES_IN_BD"
    val appStorageDir = File(context.filesDir, folderName)

    // Asegurar que la carpeta existe
    if (!appStorageDir.exists()) {
        appStorageDir.mkdirs()
    }

    val archivo = File(appStorageDir, "$fileName.txt")

    try {
        archivo.writeText(content)
        // Toast.makeText(context, "Archivo creado: ${archivo.path}", Toast.LENGTH_SHORT).show()
    } catch (e: Exception) {
        // Toast.makeText(context, "Error al crear archivo: ${e.message}", Toast.LENGTH_SHORT).show()
    }
}

fun createFolderInAppStorage(context: Context) {
    val folderName = "ES_IN_BD"
    val appStorageDir = File(context.filesDir, folderName) // Carpeta dentro del almacenamiento interno de la app

    if (!appStorageDir.exists()) {
        if (appStorageDir.mkdirs()) {
            Toast.makeText(context, "Carpeta creada: ${appStorageDir.path}", Toast.LENGTH_SHORT).show()
        } else {
            //Toast.makeText(context, "Error al crear la carpeta", Toast.LENGTH_SHORT).show()
        }
    } else {
        // Toast.makeText(context, "La carpeta ya existe", Toast.LENGTH_SHORT).show()
    }
}

fun readTextFileFromAppStorage(context: Context, fileName: String): String {
    val folderName = "ES_IN_BD"
    val appStorageDir = File(context.filesDir, folderName) // Carpeta dentro del almacenamiento de la app
    val archivo = File(appStorageDir, "$fileName.txt") // Archivo dentro de la carpeta

    return if (archivo.exists()) {
        try {
            archivo.readText()
        } catch (e: Exception) {
            ""
        }
    } else {
        // Toast.makeText(context, "El archivo no existe", Toast.LENGTH_SHORT).show()
        ""
    }
}

