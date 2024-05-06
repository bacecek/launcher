package dev.bacecek.launcher

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.slack.circuit.backstack.rememberSaveableBackStack
import com.slack.circuit.foundation.Circuit
import com.slack.circuit.foundation.CircuitCompositionLocals
import com.slack.circuit.foundation.NavigableCircuitContent
import com.slack.circuit.foundation.rememberCircuitNavigator
import com.slack.circuit.overlay.ContentWithOverlays
import com.slack.circuitx.android.rememberAndroidScreenAwareNavigator
import dev.bacecek.launcher.apps.AppInfo
import dev.bacecek.launcher.apps.AppsScreen
import dev.bacecek.launcher.navigation.AndroidScreenStarterImpl
import dev.bacecek.launcher.ui.theme.ApplicationTheme
import org.koin.android.ext.android.inject

class MainActivity : ComponentActivity() {
    private val circuit: Circuit by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.isNavigationBarContrastEnforced = false
        }
        setContent {
            BackHandler {}
            val backstack = rememberSaveableBackStack(root = AppsScreen)
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
