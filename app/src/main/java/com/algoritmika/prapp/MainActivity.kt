package com.algoritmika.prapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyScreen()
        }
    }
}

@Composable
fun MyScreen(viewModel: MyViewModel = MyViewModel()) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text(text = viewModel.message.value)
        Button(
            onClick = { viewModel.changeMessage("Текст изменён!") },
            modifier = Modifier.padding(top = 8.dp)
        ) {
            Text("Изменить текст")
        }
    }
}
