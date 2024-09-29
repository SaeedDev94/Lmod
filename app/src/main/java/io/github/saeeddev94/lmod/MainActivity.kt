package io.github.saeeddev94.lmod

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import io.github.saeeddev94.lmod.ui.theme.LmodTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LmodTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Features(
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun Features(modifier: Modifier = Modifier) {
    Text(
        text = "Trebuchet Launcher Double Tap to Sleep",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun FeaturesPreview() {
    LmodTheme {
        Features()
    }
}
