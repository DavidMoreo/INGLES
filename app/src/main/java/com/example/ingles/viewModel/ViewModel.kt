package com.example.tool.common

import EnglishModel
import com.google.gson.Gson
import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf

import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.google.firebase.crashlytics.buildtools.reloc.com.google.common.reflect.TypeToken

import org.json.JSONArray
import readJsonFromFile


class MainViewModel : ViewModel() {

    var fileNameEnglish by mutableStateOf("BD_Store_IN")
    var englishList = mutableStateListOf<EnglishModel>()
    var filter by mutableStateOf("Todos")
    var showDialog by mutableStateOf(false)
    var selectedScreen by mutableStateOf("Home")
    var englishItem by mutableStateOf(EnglishModel(id = "", ES = "",EN="",PRO="", COUNT = 0))

    val filteredData: List<EnglishModel>
        get() = englishList
            .filter { 
                (filter == "Todos" || it.PRO == filter) &&
                (it.ES.contains(filter, ignoreCase = true) || it.EN.contains(filter, ignoreCase = true))
            }
            .sortedByDescending { it.ES }



    fun LoadData(context: Context, viewModel: MainViewModel)
    {
        englishList=  mutableStateListOf<EnglishModel>();
        val readJson = readJsonFromFile(context, viewModel.fileNameEnglish, );
        if(readJson!="" && readJson !="[]") {
            try {
                if (!readJson.isNullOrBlank()) {
                    val gson = Gson()
                    val listType = object : TypeToken<List<EnglishModel>>() {}.type
                    val list: List<EnglishModel> = gson.fromJson(readJson, listType)
                    viewModel.englishList.clear()

                    list.forEach { product ->
                        viewModel.englishList.add(product)
                    }
                }
            }
            catch (e: Exception) {
                if(readJson=="") {
                }
                englishList=  mutableStateListOf<EnglishModel>();
                e.printStackTrace() // Log the error for debugging
            }
        }
    }




}
