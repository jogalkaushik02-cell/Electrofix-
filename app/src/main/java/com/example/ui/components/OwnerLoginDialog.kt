package com.example.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.AdminSettings
import com.example.ui.locale.AppStrings
import com.example.ui.theme.BrandPrimary

@Composable
fun OwnerLoginDialog(
    strings: AppStrings,
    adminSettings: AdminSettings? = null,
    onDismiss: () -> Unit,
    onUnlockSuccess: () -> Unit,
    onSavePassword: ((String) -> Unit)? = null
) {
    val savedPassword = adminSettings?.adminPassword ?: ""

    var passwordInput by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.90f)
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier.padding(22.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header Lock Icon
                Surface(
                    color = BrandPrimary.copy(alpha = 0.12f),
                    shape = CircleShape,
                    modifier = Modifier.size(54.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = BrandPrimary,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = strings.ownerLogin,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = if (strings.isGujarati)
                        "માત્ર દુકાનદાર માટે: કિંમત, સ્ટોક અને ઓર્ડર સંચાલન"
                    else
                        "Restricted merchant portal: enter owner password",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
                )

                // Password Entry Field (Secure, no bypasses)
                OutlinedTextField(
                    value = passwordInput,
                    onValueChange = {
                        passwordInput = it
                        error = false
                    },
                    label = { Text(strings.enterPassword) },
                    visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    trailingIcon = {
                        IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                            Icon(
                                imageVector = if (isPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = if (isPasswordVisible) "Hide password" else "Show password"
                            )
                        }
                    },
                    singleLine = true,
                    isError = error,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("owner_dialog_pin_input")
                )

                if (error) {
                    Text(
                        text = strings.incorrectPassword,
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 6.dp)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Unlock Button
                Button(
                    onClick = {
                        if (passwordInput.isNotBlank() && passwordInput == savedPassword) {
                            onUnlockSuccess()
                        } else {
                            error = true
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("owner_dialog_unlock_btn"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(strings.unlockAdmin, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }

                Spacer(modifier = Modifier.height(10.dp))

                TextButton(onClick = onDismiss) {
                    Text(
                        if (strings.isGujarati) "બંધ કરો" else "Cancel",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
