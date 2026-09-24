package com.example.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.locale.AppLanguage
import com.example.ui.locale.AppStrings
import com.example.ui.theme.BrandPrimary
import com.google.firebase.auth.FirebaseUser

@Composable
fun StoreTopBar(
    currentLanguage: AppLanguage,
    strings: AppStrings,
    cartItemCount: Int,
    currentUser: FirebaseUser? = null,
    onToggleLanguage: () -> Unit,
    onOpenCart: () -> Unit,
    onOpenAuth: () -> Unit = {},
    onOpenOwnerLogin: () -> Unit,
    modifier: Modifier = Modifier
) {
    var logoTapCount by remember { mutableIntStateOf(0) }

    Surface(
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
        shadowElevation = 1.dp,
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Logo & Brand (Tapping 5 times or clicking unlocks Owner PIN!)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable {
                        logoTapCount++
                        if (logoTapCount >= 4) {
                            logoTapCount = 0
                            onOpenOwnerLogin()
                        }
                    }
                    .testTag("app_brand_header")
            ) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = BrandPrimary,
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Build,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Text(
                        text = strings.appTitle,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = if (currentLanguage == AppLanguage.GU) "ઇલેક્ટ્રોનિક્સ અને સર્વિસ" else "Spares & Repair Hub",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Right Actions: Language Switcher + Cart + Discreet Owner Key
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Language Switcher Button [ EN | ગુજ ]
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                    modifier = Modifier
                        .clip(RoundedCornerShape(18.dp))
                        .clickable { onToggleLanguage() }
                        .testTag("language_toggle_btn")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Language,
                            contentDescription = "Switch Language",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (currentLanguage == AppLanguage.EN) "ગુજરાતી" else "English",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                // Cart Button with Badge
                BadgedBox(
                    badge = {
                        if (cartItemCount > 0) {
                            Badge(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = Color.White
                            ) {
                                Text(
                                    text = "$cartItemCount",
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.testTag("top_bar_cart_badge")
                                )
                            }
                        }
                    }
                ) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                        modifier = Modifier
                            .size(38.dp)
                            .clickable { onOpenCart() }
                            .testTag("top_bar_cart_button")
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.ShoppingCart,
                                contentDescription = "View Cart",
                                tint = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                // Firebase Customer/Admin Account Icon
                Surface(
                    shape = CircleShape,
                    color = if (currentUser != null) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .clickable { onOpenAuth() }
                        .testTag("top_bar_account_button")
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.AccountCircle,
                            contentDescription = "Account & Authentication",
                            tint = if (currentUser != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                // Discreet Store Owner Key (Hidden in plain sight, looks like a security badge)
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .clickable { onOpenOwnerLogin() }
                        .testTag("owner_portal_shortcut")
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = strings.ownerLogin,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            modifier = Modifier.size(15.dp)
                        )
                    }
                }
            }
        }
    }
}
