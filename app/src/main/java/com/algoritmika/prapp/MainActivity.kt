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
import androidx.compose.ui.viewinterop.AndroidView
import android.widget.CalendarView
import java.util.Calendar

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val viewModel: MyViewModel = MyViewModel()
        setContent {
            MyScreen(viewModel)
        }
    }
}

@Composable
fun MyScreen(viewModel: MyViewModel) {

    Column(modifier = Modifier.padding(16.dp)) {
        Text(text = viewModel.message.value)
        Button(
            onClick = { viewModel.toggleMessage() },
            modifier = Modifier.padding(top = 8.dp)
        ) {
            Text("Изменить текст")
        }
        AndroidView(
            factory = { context ->
                CalendarView(context).apply {
                    val today = Calendar.getInstance()
                    setDate(today.timeInMillis, false, true)
                }
            },
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}
