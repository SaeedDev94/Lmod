package io.github.saeeddev94.lmod

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.edit
import com.topjohnwu.superuser.Shell
import io.github.saeeddev94.lmod.ui.LmodTheme
import io.github.saeeddev94.lmod.ui.component.SelectDialog
import java.util.TimeZone

class MainActivity : ComponentActivity() {

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            LmodTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                        TopAppBar(title = { Text("Lmod") })
                    }
                ) { innerPadding ->
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(12.dp, 0.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        Text(
                            "Restart",
                            Modifier
                                .fillMaxWidth()
                                .padding(0.dp, innerPadding.calculateTopPadding(), 0.dp, 0.dp)
                        )
                        val rowModifier = Modifier.fillMaxWidth().padding(0.dp, 12.dp, 0.dp, 0.dp)
                        Row(modifier = rowModifier) {
                            Button(
                                { Shell.cmd("killall com.android.systemui").exec() },
                                Modifier.padding(0.dp, 0.dp, 8.dp, 0.dp),
                            ) {
                                Text("SystemUI")
                            }
                            Button(
                                { Shell.cmd("killall com.android.launcher3").exec() },
                                Modifier.padding(0.dp, 0.dp, 8.dp, 0.dp),
                            ) {
                                Text("Launcher")
                            }
                            Button(
                                { Shell.cmd("killall com.android.deskclock").exec() },
                                Modifier,
                            ) {
                                Text("Clock")
                            }
                        }
                        Row(modifier = rowModifier) {
                            BatteryIconScale(Modifier)
                        }
                        Row(modifier = rowModifier) {
                            TimeZone(Modifier)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BatteryIconScale(modifier: Modifier) {
    val context = LocalContext.current
    val label = "Battery icon scale"
    val suffix = "%"
    val prefContext = remember { context.createDeviceProtectedStorageContext() }
    val pref = remember { prefContext.getSharedPreferences(SharedPref.NAME, Context.MODE_PRIVATE) }
    val options = remember { (0..100 step 5).toList().map { "$it" } }
    val selected = remember {
        val batteryScale =
            pref.getFloat(SharedPref.BATTERY_ICON_SCALE_KEY, SharedPref.BATTERY_ICON_SCALE_DEFAULT)
        val option = SharedPref.batteryScaleToOption(batteryScale)
        val index = options.indexOf(option)
        mutableIntStateOf(index)
    }
    val onOptionSelect = { index: Int ->
        selected.intValue = index
        val option = options[index]
        val scale = SharedPref.optionToBatteryScale(option)
        pref.edit { putFloat(SharedPref.BATTERY_ICON_SCALE_KEY, scale) }
    }

    Column(
        modifier = modifier.fillMaxWidth(),
    ) {
        SelectDialog(
            label,
            options,
            selected.intValue,
            onOptionSelect,
            suffix = suffix,
        )
    }
}

@Composable
fun TimeZone(modifier: Modifier) {
    val context = LocalContext.current
    val label = "Time zone"
    val prefContext = remember { context.createDeviceProtectedStorageContext() }
    val pref = remember { prefContext.getSharedPreferences(SharedPref.NAME, Context.MODE_PRIVATE) }
    val options = remember { TimeZone.getAvailableIDs().toList() }
    val selected = remember {
        val option =
            pref.getString(SharedPref.TIME_ZONE_KEY, SharedPref.TIME_ZONE_DEFAULT)!!
        val index = options.indexOf(option)
        mutableIntStateOf(index)
    }
    val onOptionSelect = { index: Int ->
        selected.intValue = index
        val option = options[index]
        pref.edit { putString(SharedPref.TIME_ZONE_KEY, option) }
    }

    Column(
        modifier = modifier.fillMaxWidth(),
    ) {
        SelectDialog(
            label,
            options,
            selected.intValue,
            onOptionSelect,
        )
    }
}
