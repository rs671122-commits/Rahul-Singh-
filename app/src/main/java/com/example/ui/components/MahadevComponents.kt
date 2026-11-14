package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.ui.theme.BlueContainer
import com.example.ui.theme.CardBorderBlue
import com.example.ui.theme.DeepGold
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.ElevatedSlate
import com.example.ui.theme.GoldBorder
import com.example.ui.theme.GoldContainer
import com.example.ui.theme.JetBrainsMonoFamily
import com.example.ui.theme.MidnightSurface
import com.example.ui.theme.ObsidianBlack
import com.example.ui.theme.RoyalGold
import com.example.ui.theme.SapphireBlue
import com.example.ui.theme.SoftGold
import com.example.ui.theme.StatusActiveGreen
import com.example.ui.theme.StatusDangerRed
import com.example.ui.theme.StatusPendingAmber
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.OtpChallenge

@Composable
fun MahadevBrandHeader(
    subtitle: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(id = R.drawable.img_launcher_emblem),
            contentDescription = "Mahadev Panel Trident Logo",
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .border(1.5.dp, RoyalGold, CircleShape),
            contentScale = ContentScale.Crop
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = "MAHADEV PANEL",
                style = MaterialTheme.typography.titleLarge,
                color = RoyalGold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = ElectricBlue,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun StatusPill(
    label: String,
    statusKey: String,
    modifier: Modifier = Modifier
) {
    val (bg, fg, border) = when (statusKey.lowercase()) {
        "active", "approved", "completed", "resolved", "verified", "credit" ->
            Triple(Color(0xFF0A2A20), StatusActiveGreen, Color(0xFF155E44))
        "pending", "in_progress", "high", "adjustment" ->
            Triple(Color(0xFF2B2108), StatusPendingAmber, Color(0xFF6B4F0E))
        "inactive", "suspended", "rejected", "failed", "urgent", "debit", "restricted" ->
            Triple(Color(0xFF2E0D14), StatusDangerRed, Color(0xFF6E1E2D))
        "admin", "premium" ->
            Triple(GoldContainer, RoyalGold, GoldBorder)
        else ->
            Triple(BlueContainer, ElectricBlue, CardBorderBlue)
    }
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(999.dp),
        color = bg,
        border = BorderStroke(1.dp, border)
    ) {
        Text(
            text = label.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = fg,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun LuxuryCard(
    modifier: Modifier = Modifier,
    goldAccent: Boolean = false,
    content: @Composable () -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MidnightSurface),
        border = BorderStroke(1.dp, if (goldAccent) GoldBorder else CardBorderBlue),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        content()
    }
}

@Composable
fun KpiMetricCard(
    title: String,
    value: String,
    icon: ImageVector,
    accentColor: Color,
    modifier: Modifier = Modifier,
    subtitle: String? = null
) {
    Card(
        modifier = modifier,
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = ElevatedSlate),
        border = BorderStroke(1.dp, CardBorderBlue)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .clip(CircleShape)
                        .background(accentColor.copy(alpha = 0.16f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = accentColor,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall.copy(fontFamily = JetBrainsMonoFamily),
                color = TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (!subtitle.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = accentColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun GoldPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: ImageVector? = null
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.height(48.dp),
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = RoyalGold,
            contentColor = ObsidianBlack,
            disabledContainerColor = GoldContainer,
            disabledContentColor = TextSecondary
        ),
        contentPadding = PaddingValues(horizontal = 18.dp)
    ) {
        if (icon != null) {
            Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
        }
        Text(text = text, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun BlueOutlineButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: ImageVector? = null
) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.height(48.dp),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, ElectricBlue.copy(alpha = 0.65f)),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = BlueContainer.copy(alpha = 0.45f),
            contentColor = ElectricBlue
        ),
        contentPadding = PaddingValues(horizontal = 16.dp)
    ) {
        if (icon != null) {
            Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
        }
        Text(text = text, style = MaterialTheme.typography.labelLarge)
    }
}

@Composable
fun mahadevFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = RoyalGold,
    unfocusedBorderColor = CardBorderBlue,
    focusedLabelColor = RoyalGold,
    unfocusedLabelColor = TextSecondary,
    cursorColor = RoyalGold,
    focusedTextColor = TextPrimary,
    unfocusedTextColor = TextPrimary,
    focusedContainerColor = ElevatedSlate,
    unfocusedContainerColor = ElevatedSlate
)

@Composable
fun OtpVerificationDialog(
    challenge: OtpChallenge,
    onVerify: (String) -> Unit,
    onResend: () -> Unit,
    onDismiss: () -> Unit
) {
    var codeInput by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MidnightSurface,
        titleContentColor = RoyalGold,
        textContentColor = TextPrimary,
        icon = {
            Icon(
                imageVector = Icons.Filled.VerifiedUser,
                contentDescription = "OTP Verification",
                tint = RoyalGold,
                modifier = Modifier.size(32.dp)
            )
        },
        title = {
            Text("6-Digit OTP Verification", style = MaterialTheme.typography.titleLarge)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Verify your identity for ${challenge.destinationHint}. Enter the 6-digit security code below:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = BlueContainer,
                    border = BorderStroke(1.dp, ElectricBlue.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "VERIFICATION CODE",
                            style = MaterialTheme.typography.labelSmall,
                            color = ElectricBlue
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = challenge.code,
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontFamily = JetBrainsMonoFamily,
                                letterSpacing = androidx.compose.ui.unit.TextUnit(4f, androidx.compose.ui.unit.TextUnitType.Sp)
                            ),
                            color = SoftGold
                        )
                    }
                }
                OutlinedTextField(
                    value = codeInput,
                    onValueChange = { if (it.length <= 6 && it.all { c -> c.isDigit() }) codeInput = it },
                    label = { Text("Enter 6-digit OTP") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = mahadevFieldColors(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("otp_code_input")
                )
                TextButton(
                    onClick = onResend,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Regenerate OTP", color = ElectricBlue)
                }
            }
        },
        confirmButton = {
            GoldPrimaryButton(
                text = "Verify OTP",
                onClick = { onVerify(codeInput) },
                enabled = codeInput.length == 6,
                icon = Icons.Filled.CheckCircle,
                modifier = Modifier.testTag("confirm_otp_button")
            )
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextSecondary)
            }
        }
    )
}

@Composable
fun AdminPinLockDialog(
    hasExistingPin: Boolean,
    onSubmitPin: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var pin by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MidnightSurface,
        titleContentColor = RoyalGold,
        icon = {
            Icon(
                imageVector = Icons.Filled.Shield,
                contentDescription = "Admin Security Lock",
                tint = RoyalGold,
                modifier = Modifier.size(34.dp)
            )
        },
        title = {
            Text(
                text = if (hasExistingPin) "Secure Admin Login" else "Set Encrypted Admin PIN",
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = if (hasExistingPin) {
                        "Enter your encrypted Admin PIN to unlock user management, request approvals, ledger, and reports."
                    } else {
                        "Create a 4–6 digit Admin PIN. It is encrypted with a salted SHA-256 hash before storing in Firestore."
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
                OutlinedTextField(
                    value = pin,
                    onValueChange = { if (it.length <= 6 && it.all { c -> c.isDigit() }) pin = it },
                    label = { Text("Admin Security PIN") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    leadingIcon = { Icon(Icons.Filled.Lock, contentDescription = null, tint = RoyalGold) },
                    colors = mahadevFieldColors(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("admin_pin_input")
                )
            }
        },
        confirmButton = {
            GoldPrimaryButton(
                text = if (hasExistingPin) "Unlock Console" else "Save & Unlock",
                onClick = { onSubmitPin(pin) },
                enabled = pin.length >= 4,
                modifier = Modifier.testTag("admin_pin_submit_button")
            )
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextSecondary)
            }
        }
    )
}
