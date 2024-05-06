package dev.bacecek.launcher.settings

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.slack.circuit.backstack.rememberSaveableBackStack
import com.slack.circuit.foundation.Circuit
import com.slack.circuit.foundation.CircuitCompositionLocals
import com.slack.circuit.foundation.NavigableCircuitContent
import com.slack.circuit.foundation.rememberCircuitNavigator
import com.slack.circuit.overlay.ContentWithOverlays
import com.slack.circuitx.android.IntentScreen
import com.slack.circuitx.android.rememberAndroidScreenAwareNavigator
import dev.bacecek.launcher.navigation.AndroidScreenStarterImpl
import dev.bacecek.launcher.ui.theme.ApplicationTheme
import org.koin.android.ext.android.inject

@Suppress("FunctionName")
fun SettingsIntentScreen(context: Context) = IntentScreen(
    Intent(context, SettingsActivity::class.java)
)

class SettingsActivity : ComponentActivity() {
    private val circuit: Circuit by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val backstack = rememberSaveableBackStack(root = SettingsScreen)
            val navigator = rememberAndroidScreenAwareNavigator(
                rememberCircuitNavigator(backstack),
                AndroidScreenStarterImpl(this)
            )
            ApplicationTheme {
                CircuitCompositionLocals(circuit) {
                    ContentWithOverlays {
                        NavigableCircuitContent(navigator, backstack)
                    }
                }
            }
        }
    }
}
