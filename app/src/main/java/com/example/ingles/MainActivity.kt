package com.example.ingles

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import java.util.Locale

class MainActivity : ComponentActivity(), TextToSpeech.OnInitListener {

    private lateinit var tts: TextToSpeech
    private lateinit var speechRecognizer: SpeechRecognizer
    private var webView: WebView? = null
    private var isListening = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        tts = TextToSpeech(this, this)
        
        // Initialize SpeechRecognizer
        if (SpeechRecognizer.isRecognitionAvailable(this)) {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)
            speechRecognizer.setRecognitionListener(recognitionListener)
        } else {
            Toast.makeText(this, "Speech Recognition not available", Toast.LENGTH_SHORT).show()
        }

        val permissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (!isGranted) {
                Toast.makeText(this, "Permission Denied: Audio recording needed for voice chat", Toast.LENGTH_LONG).show()
            }
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }

        setContent {
            WebViewScreen()
        }
    }

    @Composable
    fun WebViewScreen() {
        AndroidView(
            factory = { context ->
                WebView(context).apply {
                    layoutParams = android.view.ViewGroup.LayoutParams(
                        android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                        android.view.ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true
                    settings.mediaPlaybackRequiresUserGesture = false
                    settings.allowFileAccess = true
                    
                    addJavascriptInterface(WebAppInterface(this@MainActivity), "Android")
                    
                    webViewClient = object : WebViewClient() {
                        override fun onPageFinished(view: WebView?, url: String?) {
                            Log.d("WebView", "Page finished loading: $url")
                            super.onPageFinished(view, url)
                        }

                        override fun onReceivedError(
                            view: WebView?, 
                            errorCode: Int, 
                            description: String?, 
                            failingUrl: String?
                        ) {
                            Log.e("WebView", "Error: $description, code: $errorCode, url: $failingUrl")
                            super.onReceivedError(view, errorCode, description, failingUrl)
                        }
                    }
                    
                    loadUrl("file:///android_asset/chat.html")
                    
                    WebView.setWebContentsDebuggingEnabled(true)
                    webView = this
                }
            },
            modifier = Modifier.fillMaxSize()
        )
    }

    // --- Speech Recognition Logic ---

    fun startRecognition(lang: String) {
        if (!::speechRecognizer.isInitialized) return
        
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, if (lang == "es") "es-ES" else "en-US")
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
        }
        
        runOnUiThread {
            try {
                speechRecognizer.startListening(intent)
                callJs("onAndroidSpeechStarted()")
            } catch (e: Exception) {
                Log.e("MainActivity", "Error starting speech: ${e.message}")
                callJs("onAndroidSpeechError('${e.message}')")
            }
        }
    }

    fun stopRecognition() {
        if (!::speechRecognizer.isInitialized) return
        runOnUiThread {
            speechRecognizer.stopListening()
        }
    }

    private val recognitionListener = object : RecognitionListener {
        override fun onReadyForSpeech(params: Bundle?) { Log.d("SR", "Ready") }
        override fun onBeginningOfSpeech() { Log.d("SR", "Beginning") }
        override fun onRmsChanged(rmsdB: Float) {}
        override fun onBufferReceived(buffer: ByteArray?) {}
        override fun onEndOfSpeech() { 
            Log.d("SR", "End of speech") 
            // Don't change UI state yet, wait for results or error
        }
        
        override fun onError(error: Int) {
            val errorMessage = getErrorText(error)
            Log.e("SR", "Error: $errorMessage")
            runOnUiThread {
                callJs("onAndroidSpeechError('$errorMessage')")
            }
        }

        override fun onResults(results: Bundle?) {
            val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            val text = matches?.get(0) ?: ""
            Log.d("SR", "Results: $text")
            runOnUiThread {
                callJs("onAndroidSpeechResult('${escapeJs(text)}')")
            }
        }

        override fun onPartialResults(partialResults: Bundle?) {
             // Optional: Update UI live?
        }

        override fun onEvent(eventType: Int, params: Bundle?) {}
    }

    private fun getErrorText(errorCode: Int): String {
        return when (errorCode) {
            SpeechRecognizer.ERROR_AUDIO -> "Audio recording error"
            SpeechRecognizer.ERROR_CLIENT -> "Client side error"
            SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Insufficient permissions"
            SpeechRecognizer.ERROR_NETWORK -> "Network error"
            SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network timeout"
            SpeechRecognizer.ERROR_NO_MATCH -> "No match"
            SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Recognizer busy"
            SpeechRecognizer.ERROR_SERVER -> "Server error"
            SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No speech input"
            else -> "Unknown error"
        }
    }

    // --- TTS Logic ---

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            tts.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {
                    runOnUiThread { callJs("onAndroidTtsStart()") }
                }

                override fun onDone(utteranceId: String?) {
                    runOnUiThread { callJs("onAndroidTtsEnd()") }
                }

                override fun onError(utteranceId: String?) {
                    runOnUiThread { callJs("onAndroidTtsEnd()") }
                }
            })
        } else {
             Log.e("TTS", "Initialization failed")
        }
    }

    fun speak(text: String, lang: String, rate: Float = 1.0f, pitch: Float = 1.0f) {
        val locale = if (lang == "es") Locale("es", "ES") else Locale.US
        val result = tts.setLanguage(locale)
        if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
            Log.e("TTS", "Language $lang not supported")
            Toast.makeText(this, "Language $lang not supported by system TTS", Toast.LENGTH_SHORT).show()
            return
        }
        
        if (::tts.isInitialized) {
            tts.setSpeechRate(rate)
            tts.setPitch(pitch)
        }
        
        val params = Bundle()
        params.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "TTS_ID")
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, params, "TTS_ID")
    }

    fun setSpeechRate(rate: Float) {
        if (::tts.isInitialized) {
            tts.setSpeechRate(rate)
        }
    }

    // --- Helper ---

    fun callJs(script: String) {
        webView?.evaluateJavascript(script, null)
    }

    private fun escapeJs(text: String): String {
        return text.replace("'", "\\'").replace("\n", " ")
    }
    
    override fun onDestroy() {
        if (::tts.isInitialized) {
            tts.stop()
            tts.shutdown()
        }
        if (::speechRecognizer.isInitialized) {
            speechRecognizer.destroy()
        }
        super.onDestroy()
    }
    
    // --- Interface Class ---

    inner class WebAppInterface(private val activity: MainActivity) {
        @JavascriptInterface
        fun speak(text: String, lang: String, rate: Float, pitch: Float) {
            activity.runOnUiThread {
                activity.speak(text, lang, rate, pitch)
            }
        }

        @JavascriptInterface
        fun startListening(lang: String) {
            activity.startRecognition(lang)
        }

        @JavascriptInterface
        fun stopListening() {
            activity.stopRecognition()
        }

        @JavascriptInterface
        fun setSpeechRate(rate: Float) {
            activity.runOnUiThread {
                activity.setSpeechRate(rate)
            }
        }

        @JavascriptInterface
        fun getVoiceName(lang: String): String {
            if (!::tts.isInitialized) return "TTS Not Initialized"
            try {
                val locale = if (lang == "es") Locale("es", "ES") else Locale.US
                activity.runOnUiThread { 
                    // Optional: we might not want to switch UI state here, but simple getters are fine on background
                    // Actually, TTS methods are thread safe. modifying language changes state.
                }
                // We set language to query what voice is used for that language
                val result = activity.tts.setLanguage(locale)
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    return "Language Not Supported"
                }
                val voice = activity.tts.voice
                return if (voice != null) "${voice.name} | ${voice.locale}" else "Default System Voice"
            } catch (e: Exception) {
                return "Error: ${e.message}"
            }
        }

        @JavascriptInterface
        fun openTtsSettings() {
            try {
                val intent = Intent("com.android.settings.TTS_SETTINGS")
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                activity.startActivity(intent)
            } catch (e: Exception) {
                Log.e("MainActivity", "Error opening TTS settings: ${e.message}")
            }
        }

        @JavascriptInterface
        fun openBrowser(url: String) {
            try {
                val intent = Intent(Intent.ACTION_VIEW, android.net.Uri.parse(url))
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                activity.startActivity(intent)
            } catch (e: Exception) {
                Log.e("MainActivity", "Error opening browser: ${e.message}")
            }
        }
    }
}


