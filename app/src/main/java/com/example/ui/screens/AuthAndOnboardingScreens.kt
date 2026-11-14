package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EnhancedEncryption
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Login
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import com.example.R
import com.example.ui.auth.attemptAutoSignIn
import com.example.ui.auth.onGoogleSignInClicked
import com.example.ui.components.BlueOutlineButton
import com.example.ui.components.GoldPrimaryButton
import com.example.ui.components.LuxuryCard
import com.example.ui.components.StatusPill
import com.example.ui.components.mahadevFieldColors
import com.example.ui.theme.BlueContainer
import com.example.ui.theme.CardBorderBlue
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.GoldBorder
import com.example.ui.theme.ObsidianBlack
import com.example.ui.theme.RoyalGold
import com.example.ui.theme.SoftGold
import com.example.ui.theme.StatusActiveGreen
import com.example.ui.theme.StatusDangerRed
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun AuthScreen(
    onAuthSuccess: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val credentialManager = remember { CredentialManager.create(context) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        attemptAutoSignIn(
            context = context,
            credentialManager = credentialManager,
            onAuthSuccess = onAuthSuccess,
            onUnauthenticated = {},
            scope = scope
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ObsidianBlack)
    ) {
        Image(
            painter = painterResource(id = R.drawable.img_hero_banner),
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp),
            contentScale = ContentScale.Crop,
            alpha = 0.55f
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, ObsidianBlack)
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier.widthIn(max = 520.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Image(
                        painter = painterResource(id = R.drawable.img_launcher_emblem),
                        contentDescription = "Mahadev Panel Emblem",
                        modifier = Modifier
                            .size(96.dp)
                            .clip(CircleShape)
                            .border(2.5.dp, RoyalGold, CircleShape),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.height(18.dp))
                    Text(
                        text = "MAHADEV PANEL",
                        style = MaterialTheme.typography.displaySmall,
                        color = RoyalGold,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Enterprise User & Admin Management Suite",
                        style = MaterialTheme.typography.bodyLarge,
                        color = ElectricBlue,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(24.dp))

                    LuxuryCard(goldAccent = true) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Text(
                                text = "Secure Access Portal",
                                style = MaterialTheme.typography.titleMedium,
                                color = TextPrimary
                            )
                            SecurityFeatureRow(
                                icon = Icons.Filled.EnhancedEncryption,
                                title = "SHA-256 Encrypted PINs",
                                description = "Salted password & security PIN protection"
                            )
                            SecurityFeatureRow(
                                icon = Icons.Filled.VerifiedUser,
                                title = "6-Digit OTP Verification",
                                description = "Multi-step verification for user & admin safety"
                            )
                            SecurityFeatureRow(
                                icon = Icons.Filled.AdminPanelSettings,
                                title = "Role-Based Access Control",
                                description = "Separate User Portal & Admin Console privileges"
                            )

                            if (!errorMessage.isNullOrBlank()) {
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = Color(0xFF2E0D14),
                                    border = BorderStroke(1.dp, StatusDangerRed.copy(alpha = 0.5f)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = errorMessage!!,
                                        color = StatusDangerRed,
                                        style = MaterialTheme.typography.bodySmall,
                                        modifier = Modifier.padding(10.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))
                            Button(
                                onClick = {
                                    errorMessage = null
                                    isLoading = true
                                    onGoogleSignInClicked(
                                        context = context,
                                        credentialManager = credentialManager,
                                        onAuthSuccess = {
                                            isLoading = false
                                            onAuthSuccess()
                                        },
                                        onAuthError = { msg ->
                                            isLoading = false
                                            errorMessage = msg
                                        },
                                        scope = scope,
                                        onAuthCancelled = { isLoading = false }
                                    )
                                },
                                enabled = !isLoading,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp)
                                    .testTag("google_sign_in_button"),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = RoyalGold,
                                    contentColor = ObsidianBlack
                                )
                            ) {
                                if (isLoading) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(22.dp),
                                        color = ObsidianBlack,
                                        strokeWidth = 2.5.dp
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Filled.Login,
                                        contentDescription = null,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = "Sign in with Google",
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SecurityFeatureRow(
    icon: ImageVector,
    title: String,
    description: String
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(BlueContainer)
                .border(1.dp, CardBorderBlue, RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = ElectricBlue,
                modifier = Modifier.size(18.dp)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(text = title, style = MaterialTheme.typography.titleSmall, color = SoftGold)
            Text(text = description, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
        }
    }
}

@Composable
fun OnboardingSignupScreen(
    initialName: String,
    email: String,
    isAdminAccount: Boolean,
    isBusy: Boolean,
    otpVerified: Boolean,
    onRequestOtp: (String) -> Unit,
    onCompleteSignup: (fullName: String, phone: String, rawPin: String) -> Unit,
    onSignOut: () -> Unit
) {
    var fullName by remember(initialName) { mutableStateOf(initialName.ifBlank { "" }) }
    var phone by remember { mutableStateOf("") }
    var pin by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ObsidianBlack)
            .statusBarsPadding()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(modifier = Modifier.widthIn(max = 560.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "COMPLETE SIGNUP",
                    style = MaterialTheme.typography.headlineMedium,
                    color = RoyalGold
                )
                StatusPill(
                    label = if (isAdminAccount) "Admin" else "Member",
                    statusKey = if (isAdminAccount) "admin" else "standard"
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Signed in as $email. Configure your encrypted security PIN and verify via 6-digit OTP.",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )
            Spacer(modifier = Modifier.height(20.dp))

            LuxuryCard(goldAccent = true) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    OutlinedTextField(
                        value = fullName,
                        onValueChange = { fullName = it },
                        label = { Text("Full Name") },
                        singleLine = true,
                        leadingIcon = { Icon(Icons.Filled.Person, null, tint = RoyalGold) },
                        colors = mahadevFieldColors(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("signup_name_input")
                    )
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { if (it.length <= 15) phone = it },
                        label = { Text("Mobile Number") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        leadingIcon = { Icon(Icons.Filled.Phone, null, tint = ElectricBlue) },
                        colors = mahadevFieldColors(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("signup_phone_input")
                    )
                    OutlinedTextField(
                        value = pin,
                        onValueChange = { if (it.length <= 6 && it.all { c -> c.isDigit() }) pin = it },
                        label = { Text("Create Security PIN (4–6 digits)") },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        leadingIcon = { Icon(Icons.Filled.Lock, null, tint = RoyalGold) },
                        supportingText = {
                            Text(
                                "Stored as a salted SHA-256 cryptographic hash.",
                                color = TextSecondary
                            )
                        },
                        colors = mahadevFieldColors(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("signup_pin_input")
                    )

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (otpVerified) Color(0xFF0A2A20) else BlueContainer.copy(alpha = 0.5f),
                        border = BorderStroke(
                            1.dp,
                            if (otpVerified) StatusActiveGreen else CardBorderBlue
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = if (otpVerified) Icons.Filled.CheckCircle else Icons.Filled.Security,
                                    contentDescription = null,
                                    tint = if (otpVerified) StatusActiveGreen else ElectricBlue
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = if (otpVerified) "OTP Verified" else "OTP Verification",
                                        style = MaterialTheme.typography.titleSmall,
                                        color = TextPrimary
                                    )
                                    Text(
                                        text = if (otpVerified) "Device & account confirmed" else "Verify with a 6-digit code",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextSecondary
                                    )
                                }
                            }
                            if (!otpVerified) {
                                BlueOutlineButton(
                                    text = "Verify",
                                    onClick = { onRequestOtp(phone.ifBlank { email }) },
                                    modifier = Modifier.testTag("signup_verify_otp_button")
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    GoldPrimaryButton(
                        text = if (isBusy) "Creating Account..." else "Activate Panel Account",
                        onClick = { onCompleteSignup(fullName, phone, pin) },
                        enabled = !isBusy && fullName.trim().length >= 2 && pin.length >= 4,
                        icon = Icons.Filled.VerifiedUser,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("complete_signup_button")
                    )

                    BlueOutlineButton(
                        text = "Switch Google Account",
                        onClick = onSignOut,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}
