package dev.bacecek.launcher.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.androidx.compose.koinViewModel

@Composable
fun SettingsDialog(
    onDismissRequest: () -> Unit,
) {
    val viewModel = koinViewModel<SettingsViewModel>()
    val gridSize by viewModel.gridSize.collectAsStateWithLifecycle()
    val availableGridSizes = viewModel.availableGridSizes
    val isRecentsEnabled by viewModel.isRecentsEnabled.collectAsStateWithLifecycle()
    AlertDialog(
        containerColor = MaterialTheme.colorScheme.surface,
        onDismissRequest = onDismissRequest,
        title = {
            Text(
                text = "Settings",
                style = MaterialTheme.typography.titleLarge,
            )
        },
        confirmButton = {
            Text(
                text = "Ok",
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.clickable(onClick = onDismissRequest),
            )
        },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState())) {
                HorizontalDivider()
                SettingsDialogSectionTitle("Grid Size")
                Column(Modifier.selectableGroup()) {
                    availableGridSizes.forEach { size ->
                        SettingsDialogThemeChooserRow(
                            text = size.toString(),
                            selected = gridSize == size,
                            onClick = { viewModel.setGridSize(size) },
                        )
                    }
                }
                SettingsDialogSectionTitle("Show recent used apps")
                Column(Modifier.selectableGroup()) {
                    SettingsDialogThemeChooserRow(
                        text = "Enabled",
                        selected = isRecentsEnabled,
                        onClick = { viewModel.setRecentsEnabled(true) },
                    )
                    SettingsDialogThemeChooserRow(
                        text = "Disabled",
                        selected = !isRecentsEnabled,
                        onClick = { viewModel.setRecentsEnabled(false) },
                    )
                }
            }
        },
    )
}

@Composable
private fun SettingsDialogSectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        modifier = Modifier.padding(top = 16.dp, bottom = 8.dp),
    )
}

@Composable
private fun SettingsDialogThemeChooserRow(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Row(
        Modifier
            .fillMaxWidth()
            .selectable(
                selected = selected,
                role = Role.RadioButton,
                onClick = onClick,
            )
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RadioButton(
            selected = selected,
            onClick = null,
        )
        Spacer(Modifier.width(8.dp))
        Text(text)
    }
}
