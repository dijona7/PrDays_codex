package com.algoritmika.prapp

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel

class MyViewModel : ViewModel() {
    // приватное изменяемое состояние
    private val _message = mutableStateOf("Привет из ViewModel!")
    // публичное только для чтения
    val message: State<String> = _message

    // функция для изменения текста
    fun changeMessage(newText: String) {
        _message.value = newText
    }

    fun toggleMessage() {
        _message.value = if (_message.value == "Ура!") {
            "Привет из ViewModel!"
        } else {
            "Ура!"
        }
    }
}
