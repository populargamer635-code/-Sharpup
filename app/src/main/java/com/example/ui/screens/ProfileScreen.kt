package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import com.example.R
import com.example.model.AvatarOption
import com.example.model.ChildProfile
import com.example.model.DifficultyTier
import com.example.ui.auth.signOutUser
import com.example.ui.components.MascotAvatarBadge
import com.example.ui.theme.SparkCobalt
import com.example.ui.theme.SparkMint

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ProfileScreen(
    profile: ChildProfile,
    saveMessage: String?,
    onSaveProfile: (String, AvatarOption, DifficultyTier) -> Unit,
    onDismissMessage: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val credentialManager = remember(context) { CredentialManager.create(context) }

    var nickname by remember(profile.childName) { mutableStateOf(profile.childName) }
    var selectedAvatar by remember(profile.avatarId) { mutableStateOf(profile.avatarEnum) }
    var selectedTier by remember(profile.difficultyTier) { mutableStateOf(profile.tierEnum) }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 640.dp),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            item {
                Text(
                    text = "Kid Profile",
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            item {
                Card(
                    shape = RoundedCornerShape(26.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            MascotAvatarBadge(avatar = selectedAvatar, size = 64.dp)
                            Spacer(modifier = Modifier.width(14.dp))
                            Column {
                                Text(
                                    text = nickname.ifBlank { "Math Explorer" },
                                    style = MaterialTheme.typography.headlineSmall,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "${selectedAvatar.label} • ${selectedTier.label}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        OutlinedTextField(
                            value = nickname,
                            onValueChange = { input ->
                                if (input.length <= 30) {
                                    nickname = input
                                    onDismissMessage()
                                }
                            },
                            label = { Text("Child's Nickname") },
                            singleLine = true,
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("nickname_input")
                        )

                        Text(
                            text = "Pick Your Mascot Buddy",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            AvatarOption.entries.forEach { option ->
                                val isSelected = option == selectedAvatar
                                Surface(
                                    shape = RoundedCornerShape(18.dp),
                                    color = if (isSelected) MaterialTheme.colorScheme.primaryContainer
                                    else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
                                    border = if (isSelected) BorderStroke(2.dp, SparkCobalt) else null,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(18.dp))
                                        .clickable {
                                            selectedAvatar = option
                                            onDismissMessage()
                                        }
                                        .testTag("avatar_option_${option.id}")
                                ) {
                                    Column(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        MascotAvatarBadge(avatar = option, size = 42.dp)
                                        Spacer(modifier = Modifier.height(5.dp))
                                        Text(
                                            text = option.label,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }
                        }

                        Text(
                            text = "Default Brain Level",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            DifficultyTier.entries.forEach { tier ->
                                val isSelected = tier == selectedTier
                                Surface(
                                    shape = RoundedCornerShape(16.dp),
                                    color = if (isSelected) MaterialTheme.colorScheme.primaryContainer
                                    else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                                    border = if (isSelected) BorderStroke(2.dp, SparkCobalt) else null,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(16.dp))
                                        .clickable {
                                            selectedTier = tier
                                            onDismissMessage()
                                        }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(14.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = "${tier.label} (${tier.ageHint})",
                                                style = MaterialTheme.typography.titleSmall,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Text(
                                                text = tier.description,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        if (isSelected) {
                                            Icon(
                                                imageVector = Icons.Rounded.Check,
                                                contentDescription = "Selected",
                                                tint = SparkCobalt
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        if (saveMessage != null) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = SparkMint.copy(alpha = 0.16f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = saveMessage,
                                    style = MaterialTheme.typography.labelLarge,
                                    color = SparkMint,
                                    modifier = Modifier.padding(12.dp)
                                )
                            }
                        }

                        Button(
                            onClick = {
                                onSaveProfile(
                                    nickname.trim().ifBlank { "Math Explorer" },
                                    selectedAvatar,
                                    selectedTier
                                )
                            },
                            shape = RoundedCornerShape(18.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = SparkCobalt),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(54.dp)
                                .testTag("save_profile_button")
                        ) {
                            Text(
                                text = "Save Profile",
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                    }
                }
            }

            item {
                OutlinedButton(
                    onClick = { signOutUser(credentialManager, scope) },
                    shape = RoundedCornerShape(18.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("sign_out_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.Logout,
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.sign_out),
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}
