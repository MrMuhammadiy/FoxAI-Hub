package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.R
import com.example.data.database.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.DailyHubViewModel
import com.example.ui.viewmodel.Screen
import com.example.ui.viewmodel.TimerMode
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// --- Main App Entrance ---
@Composable
fun DailyHubApp(viewModel: DailyHubViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsState()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Crossfade(targetState = currentScreen, label = "ScreenTransition") { screen ->
            when (screen) {
                Screen.Login -> LoginScreen(viewModel)
                Screen.Signup -> SignupScreen(viewModel)
                Screen.Dashboard -> DashboardScreen(viewModel)
                Screen.Study -> StudyScreen(viewModel)
                Screen.Work -> WorkScreen(viewModel)
                Screen.Lifestyle -> LifestyleScreen(viewModel)
                Screen.Facts -> FactsScreen(viewModel)
                Screen.FoxAi -> FoxAiScreen(viewModel)
                Screen.Settings -> SettingsScreen(viewModel)
            }
        }
    }
}

// --- Common UI Components ---

@Composable
fun AppBackground(content: @Composable BoxScope.() -> Unit) {
    val isDark = isSystemInDarkTheme()
    val gradientColors = if (isDark) {
        listOf(
            MaterialTheme.colorScheme.background,
            Color(0xFF141218),
            Color(0xFF1D192B)
        )
    } else {
        listOf(
            MaterialTheme.colorScheme.background,
            Color(0xFFF3EDF7),
            Color(0xFFEADDFF)
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(gradientColors))
    ) {
        // Draw colorful glowing blurred blobs in the background for a premium glassmorphism/frosted effect
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            if (!isDark) {
                // Purple glowing sphere top right
                drawCircle(
                    color = Color(0x3D6750A4),
                    radius = w * 0.45f,
                    center = Offset(w * 0.9f, h * 0.15f)
                )
                // Light lavender sphere bottom left
                drawCircle(
                    color = Color(0x2BCCC2DC),
                    radius = w * 0.5f,
                    center = Offset(w * 0.1f, h * 0.85f)
                )
            } else {
                // Soft deep purple glow top right
                drawCircle(
                    color = Color(0x2B8C73C7),
                    radius = w * 0.45f,
                    center = Offset(w * 0.9f, h * 0.15f)
                )
                // Rust glow bottom left
                drawCircle(
                    color = Color(0x11EFB8C8),
                    radius = w * 0.5f,
                    center = Offset(w * 0.1f, h * 0.85f)
                )
            }
        }
        content()
    }
}

@Composable
fun ModuleHeader(
    title: String,
    subtitle: String,
    icon: ImageVector,
    accentColor: Color,
    onBack: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (onBack != null) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .padding(end = 12.dp)
                    .background(
                        MaterialTheme.colorScheme.surface.copy(alpha = 0.6f),
                        CircleShape
                    )
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        Box(
            modifier = Modifier
                .size(44.dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(accentColor.copy(alpha = 0.4f), Color.Transparent)
                    ),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = accentColor,
                modifier = Modifier.size(24.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1.0f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                ),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
    }
}

@Composable
fun CustomBottomNavigation(
    selectedScreen: Screen,
    onNavigate: (Screen) -> Unit
) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.75f),
        tonalElevation = 0.dp,
        modifier = Modifier
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(32.dp))
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(32.dp))
    ) {
        NavigationBarItem(
            selected = selectedScreen == Screen.Dashboard,
            onClick = { onNavigate(Screen.Dashboard) },
            icon = { Icon(Icons.Default.Dashboard, contentDescription = "Dashboard") },
            label = { Text("Hub", style = MaterialTheme.typography.labelSmall) }
        )
        NavigationBarItem(
            selected = selectedScreen == Screen.Study,
            onClick = { onNavigate(Screen.Study) },
            icon = { Icon(Icons.Default.School, contentDescription = "Study") },
            label = { Text("Study", style = MaterialTheme.typography.labelSmall) }
        )
        NavigationBarItem(
            selected = selectedScreen == Screen.Work,
            onClick = { onNavigate(Screen.Work) },
            icon = { Icon(Icons.Default.Work, contentDescription = "Work") },
            label = { Text("Work", style = MaterialTheme.typography.labelSmall) }
        )
        NavigationBarItem(
            selected = selectedScreen == Screen.Lifestyle,
            onClick = { onNavigate(Screen.Lifestyle) },
            icon = { Icon(Icons.Default.Favorite, contentDescription = "Lifestyle") },
            label = { Text("Life", style = MaterialTheme.typography.labelSmall) }
        )
        NavigationBarItem(
            selected = selectedScreen == Screen.FoxAi,
            onClick = { onNavigate(Screen.FoxAi) },
            icon = { Icon(Icons.Default.Chat, contentDescription = "FoxAI") },
            label = { Text("FoxAI", style = MaterialTheme.typography.labelSmall) }
        )
    }
}

// --- Login Screen ---
@Composable
fun LoginScreen(viewModel: DailyHubViewModel) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }

    val authError by viewModel.authError.collectAsState()

    AppBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            // Logo & Mascot Frame
            Box(
                modifier = Modifier
                    .size(130.dp)
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primaryContainer,
                                MaterialTheme.colorScheme.secondaryContainer
                            )
                        ),
                        shape = CircleShape
                    )
                    .padding(4.dp),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_fox_mascot),
                    contentDescription = "Fox Mascot Logo",
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "DailyHub",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Serif,
                    letterSpacing = 1.sp
                ),
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = "Study. Work. Lifestyle. FoxAI Assistant.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(24.dp)),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Sign In to Your Dashboard",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = username,
                        onValueChange = { username = it },
                        label = { Text("Username") },
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("username_input"),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Password") },
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                        trailingIcon = {
                            IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                                Icon(
                                    imageVector = if (isPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = "Toggle password visibility"
                                )
                            }
                        },
                        visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("password_input"),
                        shape = RoundedCornerShape(12.dp),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = { viewModel.login(username, password) })
                    )

                    if (authError != null) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = authError ?: "",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = { viewModel.login(username, password) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("login_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text("Log In", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    TextButton(
                        onClick = {
                            viewModel.clearAuthStates()
                            viewModel.navigateTo(Screen.Signup)
                        }
                    ) {
                        Text(
                            "New to DailyHub? Create an Account",
                            color = MaterialTheme.colorScheme.secondary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

// --- Signup Screen ---
@Composable
fun SignupScreen(viewModel: DailyHubViewModel) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }

    val authError by viewModel.authError.collectAsState()

    AppBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Join DailyHub",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Serif
                ),
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = "Set up your secure, private local profile today",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(24.dp)),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Create Account",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = username,
                        onValueChange = { username = it },
                        label = { Text("Username") },
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("signup_username_input"),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Password") },
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                        trailingIcon = {
                            IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                                Icon(
                                    imageVector = if (isPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = "Toggle password visibility"
                                )
                            }
                        },
                        visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("signup_password_input"),
                        shape = RoundedCornerShape(12.dp)
                    )

                    if (authError != null) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = authError ?: "",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = { viewModel.signup(username, password) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("signup_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                    ) {
                        Text("Sign Up", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    TextButton(
                        onClick = {
                            viewModel.clearAuthStates()
                            viewModel.navigateTo(Screen.Login)
                        }
                    ) {
                        Text(
                            "Already have an account? Sign In",
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

// --- Dashboard Screen ---
@Composable
fun DashboardScreen(viewModel: DailyHubViewModel) {
    val user by viewModel.currentUser.collectAsState()
    val tasks by viewModel.tasks.collectAsState()
    val habits by viewModel.habits.collectAsState()
    val moodLogs by viewModel.moodLogs.collectAsState()
    val currentFact by viewModel.currentFact.collectAsState()
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current
    val isDark = isSystemInDarkTheme()

    AppBackground {
        Scaffold(
            bottomBar = { CustomBottomNavigation(Screen.Dashboard) { viewModel.navigateTo(it) } },
            containerColor = Color.Transparent
        ) { innerPadding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header Space
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "Hello, ${user?.username ?: "User"}!",
                                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Your centralized dashboard is ready.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                        IconButton(
                            onClick = { viewModel.navigateTo(Screen.Settings) },
                            modifier = Modifier
                                .background(MaterialTheme.colorScheme.surface, CircleShape)
                                .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Settings",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                // Fact Card Section (Frosted Glass with outline border)
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(24.dp)),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.Lightbulb,
                                        contentDescription = null,
                                        tint = FactsColor,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "DID YOU KNOW? • ${currentFact.category.uppercase()}",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                Row {
                                    IconButton(
                                        onClick = { viewModel.rotateFact() },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Refresh,
                                            contentDescription = "New Fact",
                                            modifier = Modifier.size(16.dp),
                                            tint = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    IconButton(
                                        onClick = {
                                            clipboardManager.setText(AnnotatedString("[DailyHub Fact] ${currentFact.text}"))
                                            Toast.makeText(context, "Fact copied to clipboard!", Toast.LENGTH_SHORT).show()
                                        },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Share,
                                            contentDescription = "Share",
                                            modifier = Modifier.size(16.dp),
                                            tint = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = currentFact.text,
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                // Mascot FoxAI Smart Widget (Custom gradient + frosted avatar outline)
                item {
                    val gradient = Brush.linearGradient(
                        colors = if (isDark) {
                            listOf(Color(0xFF4F378B), Color(0xFF381E72))
                        } else {
                            listOf(Color(0xFF6750A4), Color(0xFF4F378B))
                        }
                    )
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.navigateTo(Screen.FoxAi) }
                            .border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(24.dp)),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                    ) {
                        Box(
                            modifier = Modifier
                                .background(gradient)
                                .padding(18.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(60.dp)
                                        .background(Color(0x33FFFFFF), RoundedCornerShape(16.dp))
                                        .border(1.dp, Color(0x4DFFFFFF), RoundedCornerShape(16.dp))
                                        .padding(4.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Image(
                                        painter = painterResource(id = R.drawable.img_fox_mascot),
                                        contentDescription = "Fox Mascot",
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .clip(RoundedCornerShape(12.dp)),
                                        contentScale = ContentScale.Crop
                                    )
                                }
                                Spacer(modifier = Modifier.width(16.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "FoxAI Assistant",
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = Color.White
                                    )
                                    Text(
                                        text = "I have scanned your tasks and habits. Let's make today awesome!",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.White.copy(alpha = 0.8f)
                                    )
                                }
                                Icon(
                                    Icons.Default.Chat,
                                    contentDescription = null,
                                    tint = Color.White
                                )
                            }
                        }
                    }
                }

                // Metrics / Status Quick Glance (Frosted Cards)
                item {
                    val activeTasksCount = tasks.filter { !it.isCompleted }.size
                    val totalHabits = habits.size
                    val completedHabitsToday = habits.filter { it.lastLoggedDate == viewModel.getTodayDateString() }.size

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Task Summary
                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { viewModel.navigateTo(Screen.Work) }
                                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(24.dp)),
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            )
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Icon(Icons.Default.List, contentDescription = null, tint = WorkColor)
                                Spacer(modifier = Modifier.height(12.dp))
                                Text("Pending Tasks", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                                Text(
                                    "$activeTasksCount tasks",
                                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }

                        // Habit Summary
                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { viewModel.navigateTo(Screen.Lifestyle) }
                                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(24.dp)),
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            )
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = StudyColor)
                                Spacer(modifier = Modifier.height(12.dp))
                                Text("Habits Done", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                                Text(
                                    "$completedHabitsToday / $totalHabits",
                                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }

                // Quick Mood Glance (Frosted glass row card)
                item {
                    val moodToday = moodLogs.firstOrNull()
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.navigateTo(Screen.Lifestyle) }
                            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(24.dp)),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(16.dp)
                                .fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Mood, contentDescription = null, tint = LifestyleColor)
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text("Today's Mood State", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                                    Text(
                                        text = if (moodToday != null) "${moodToday.emoji} ${moodToday.note}" else "Not logged yet today.",
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1
                                    )
                                }
                            }
                            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                        }
                    }
                }

                // Navigate Cards Section Header
                item {
                    Text(
                        "MODULES",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }

                // Study Module Card
                item {
                    NavigationCard(
                        title = "Study Hub",
                        desc = "Pomodoro, flashcards, and learning logs",
                        color = StudyColor,
                        icon = Icons.Default.School
                    ) {
                        viewModel.navigateTo(Screen.Study)
                    }
                }

                // Work Module Card
                item {
                    NavigationCard(
                        title = "Work & Productivity",
                        desc = "Prioritized checklist and daily 3 wins",
                        color = WorkColor,
                        icon = Icons.Default.Work
                    ) {
                        viewModel.navigateTo(Screen.Work)
                    }
                }

                // Lifestyle Card
                item {
                    NavigationCard(
                        title = "Lifestyle & Reflection",
                        desc = "Habit streaks, mood checking, reflections",
                        color = LifestyleColor,
                        icon = Icons.Default.Favorite
                    ) {
                        viewModel.navigateTo(Screen.Lifestyle)
                    }
                }

                // Facts Card
                item {
                    NavigationCard(
                        title = "Did You Know?",
                        desc = "Explore over 100 interesting, curated facts",
                        color = FactsColor,
                        icon = Icons.Default.Lightbulb
                    ) {
                        viewModel.navigateTo(Screen.Facts)
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(20.dp))
                }
            }
        }
    }
}

@Composable
fun NavigationCard(
    title: String,
    desc: String,
    color: Color,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(24.dp)),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(color.copy(alpha = 0.15f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = color)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = desc,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f))
        }
    }
}

// --- Study Screen ---
@Composable
fun StudyScreen(viewModel: DailyHubViewModel) {
    val timerMode by viewModel.timerMode.collectAsState()
    val secondsRemaining by viewModel.durationSecondsRemaining.collectAsState()
    val isTimerRunning by viewModel.isTimerRunning.collectAsState()
    val user by viewModel.currentUser.collectAsState()

    val decks by viewModel.decks.collectAsState()
    val currentDeck by viewModel.currentDeck.collectAsState()
    val deckCards by viewModel.deckCards.collectAsState()
    val currentCardIndex by viewModel.currentCardIndex.collectAsState()
    val isCardFlipped by viewModel.isCardFlipped.collectAsState()

    val learningLogs by viewModel.learningLogs.collectAsState()

    var showCreateDeckDialog by remember { mutableStateOf(false) }
    var newDeckName by remember { mutableStateOf("") }

    var showAddCardDialog by remember { mutableStateOf(false) }
    var newCardFront by remember { mutableStateOf("") }
    var newCardBack by remember { mutableStateOf("") }

    var newLearningLogText by remember { mutableStateOf("") }

    val minutes = secondsRemaining / 60
    val seconds = secondsRemaining % 60
    val timeFormatted = String.format("%02d:%02d", minutes, seconds)

    AppBackground {
        Scaffold(
            bottomBar = { CustomBottomNavigation(Screen.Study) { viewModel.navigateTo(it) } },
            containerColor = Color.Transparent
        ) { innerPadding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    ModuleHeader(
                        title = "Study Hub",
                        subtitle = "Train your focus and build knowledge.",
                        icon = Icons.Default.School,
                        accentColor = StudyColor,
                        onBack = { viewModel.navigateTo(Screen.Dashboard) }
                    )
                }

                // 1. Pomodoro Timer
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(24.dp)),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(
                            modifier = Modifier.padding(18.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                "POMODORO TIMER",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                                color = StudyColor
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            // Timer Mode Selector
                            Row(
                                modifier = Modifier
                                    .background(
                                        MaterialTheme.colorScheme.background,
                                        RoundedCornerShape(8.dp)
                                    )
                                    .padding(4.dp),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                TimerModeButton(
                                    label = "Focus",
                                    selected = timerMode == TimerMode.Focus,
                                    color = StudyColor
                                ) {
                                    viewModel.changeTimerMode(TimerMode.Focus)
                                }
                                TimerModeButton(
                                    label = "Short Break",
                                    selected = timerMode == TimerMode.ShortBreak,
                                    color = StudyColor
                                ) {
                                    viewModel.changeTimerMode(TimerMode.ShortBreak)
                                }
                                TimerModeButton(
                                    label = "Long Break",
                                    selected = timerMode == TimerMode.LongBreak,
                                    color = StudyColor
                                ) {
                                    viewModel.changeTimerMode(TimerMode.LongBreak)
                                }
                            }

                            Spacer(modifier = Modifier.height(24.dp))

                            // Timer Digital Clock Display
                            Text(
                                text = timeFormatted,
                                style = MaterialTheme.typography.displayLarge.copy(
                                    fontWeight = FontWeight.Black,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 64.sp
                                ),
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            // Progress Bar
                            val totalTime = when (timerMode) {
                                TimerMode.Focus -> (user?.focusDurationMinutes ?: 25) * 60
                                TimerMode.ShortBreak -> (user?.shortBreakMinutes ?: 5) * 60
                                TimerMode.LongBreak -> (user?.longBreakMinutes ?: 15) * 60
                            }
                            val progress = if (totalTime > 0) secondsRemaining.toFloat() / totalTime.toFloat() else 0f
                            LinearProgressIndicator(
                                progress = { progress },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(CircleShape),
                                color = StudyColor,
                                trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                            )

                            Spacer(modifier = Modifier.height(24.dp))

                            // Controls Row
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedButton(
                                    onClick = { viewModel.resetTimer() },
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(Icons.Default.Refresh, contentDescription = "Reset")
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Reset")
                                }

                                Button(
                                    onClick = {
                                        if (isTimerRunning) viewModel.pauseTimer() else viewModel.startTimer()
                                    },
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = StudyColor)
                                ) {
                                    Icon(
                                        imageVector = if (isTimerRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                                        contentDescription = if (isTimerRunning) "Pause" else "Start"
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(if (isTimerRunning) "Pause" else "Start")
                                }
                            }
                        }
                    }
                }

                // 2. Flashcards Section
                item {
                    Text(
                        "FLASHCARDS SYSTEM",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }

                if (currentDeck == null) {
                    // List available decks
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(modifier = Modifier.padding(18.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        "Your Decks",
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                    )
                                    Button(
                                        onClick = { showCreateDeckDialog = true },
                                        shape = RoundedCornerShape(10.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = StudyColor),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                    ) {
                                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("New Deck", style = MaterialTheme.typography.labelSmall)
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                if (decks.isEmpty()) {
                                    Text(
                                        "No card decks created yet. Add one to start studying!",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                        modifier = Modifier.padding(vertical = 12.dp)
                                    )
                                } else {
                                    decks.forEach { deck ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable { viewModel.selectDeckForStudy(deck) }
                                                .padding(vertical = 10.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(Icons.Default.Book, contentDescription = null, tint = StudyColor)
                                                Spacer(modifier = Modifier.width(12.dp))
                                                Text(
                                                    deck.name,
                                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                                                )
                                            }
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                IconButton(onClick = { viewModel.deleteDeck(deck) }) {
                                                    Icon(
                                                        Icons.Default.Delete,
                                                        contentDescription = "Delete deck",
                                                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f),
                                                        modifier = Modifier.size(20.dp)
                                                    )
                                                }
                                                Icon(Icons.Default.ChevronRight, contentDescription = null)
                                            }
                                        }
                                        HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
                                    }
                                }
                            }
                        }
                    }
                } else {
                    // Study Mode View
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(modifier = Modifier.padding(18.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            "Deck: ${currentDeck?.name}",
                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                        )
                                        Text(
                                            "Card ${if (deckCards.isNotEmpty()) currentCardIndex + 1 else 0} of ${deckCards.size}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                        )
                                    }
                                    Row {
                                        IconButton(onClick = { showAddCardDialog = true }) {
                                            Icon(Icons.Default.Add, contentDescription = "Add card", tint = StudyColor)
                                        }
                                        IconButton(onClick = { viewModel.selectDeckForStudy(currentDeck!!) /* resets */ }) {
                                            Icon(Icons.Default.Close, contentDescription = "Exit deck")
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                if (deckCards.isEmpty()) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 32.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text("This deck is empty.")
                                        Spacer(modifier = Modifier.height(12.dp))
                                        Button(
                                            onClick = { showAddCardDialog = true },
                                            colors = ButtonDefaults.buttonColors(containerColor = StudyColor)
                                        ) {
                                            Text("Add First Card")
                                        }
                                    }
                                } else {
                                    val card = deckCards[currentCardIndex]

                                    // Interactive Flashcard Box with Flip
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(180.dp)
                                            .background(
                                                Brush.linearGradient(
                                                    colors = if (isCardFlipped) {
                                                        listOf(Color(0xFF2E3E34), Color(0xFF1E2F25))
                                                    } else {
                                                        listOf(
                                                            MaterialTheme.colorScheme.surfaceVariant,
                                                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                                        )
                                                    }
                                                ),
                                                RoundedCornerShape(16.dp)
                                            )
                                            .border(
                                                1.dp,
                                                if (isCardFlipped) StudyColor else Color.Transparent,
                                                RoundedCornerShape(16.dp)
                                            )
                                            .clickable { viewModel.flipCard() }
                                            .padding(20.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text(
                                                text = if (isCardFlipped) "BACK (REVEALED)" else "FRONT (QUESTION)",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = if (isCardFlipped) StudyColor else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                            )
                                            Spacer(modifier = Modifier.height(12.dp))
                                            Text(
                                                text = if (isCardFlipped) card.back else card.front,
                                                style = MaterialTheme.typography.titleLarge.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    textAlign = TextAlign.Center
                                                ),
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Spacer(modifier = Modifier.height(12.dp))
                                            Text(
                                                text = "Tap to Flip",
                                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(16.dp))

                                    // Evaluation controls
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        IconButton(onClick = { viewModel.prevCard() }) {
                                            Icon(Icons.Default.ArrowBack, contentDescription = "Prev card")
                                        }

                                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            Button(
                                                onClick = { viewModel.markCardKnown(card, false) },
                                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                                                shape = RoundedCornerShape(8.dp)
                                            ) {
                                                Text("Review Again")
                                            }

                                            Button(
                                                onClick = { viewModel.markCardKnown(card, true) },
                                                colors = ButtonDefaults.buttonColors(containerColor = StudyColor),
                                                shape = RoundedCornerShape(8.dp)
                                            ) {
                                                Text("Got It!")
                                            }
                                        }

                                        IconButton(onClick = { viewModel.nextCard() }) {
                                            Icon(Icons.Default.ArrowForward, contentDescription = "Next card")
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))
                                    if (card.isKnown) {
                                        Text(
                                            "✓ Marked as known!",
                                            color = StudyColor,
                                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                            modifier = Modifier.fillMaxWidth(),
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // 3. Learning Logs
                item {
                    Text(
                        "LEARNING LOGS",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }

                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Text(
                                "What did you learn today?",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = newLearningLogText,
                                onValueChange = { newLearningLogText = it },
                                placeholder = { Text("Write your daily learning discoveries...") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = {
                                    if (newLearningLogText.isNotBlank()) {
                                        viewModel.addLearningLog(newLearningLogText)
                                        newLearningLogText = ""
                                    }
                                },
                                modifier = Modifier.align(Alignment.End),
                                colors = ButtonDefaults.buttonColors(containerColor = StudyColor),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("Save Discovery")
                            }

                            if (learningLogs.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    "Discovery Archive",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                learningLogs.forEach { log ->
                                    val dateStr = SimpleDateFormat("MMM dd, yyyy • HH:mm", Locale.getDefault()).format(Date(log.timestamp))
                                    Column(modifier = Modifier.padding(vertical = 8.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.Top
                                        ) {
                                            Text(
                                                text = log.content,
                                                style = MaterialTheme.typography.bodyMedium,
                                                modifier = Modifier.weight(1f)
                                            )
                                            IconButton(
                                                onClick = { viewModel.deleteLearningLog(log) },
                                                modifier = Modifier.size(24.dp)
                                            ) {
                                                Icon(
                                                    Icons.Default.Delete,
                                                    contentDescription = "Delete",
                                                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.6f),
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }
                                        Text(
                                            text = dateStr,
                                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                                        )
                                    }
                                    HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
                                }
                            }
                        }
                    }
                }

                item { Spacer(modifier = Modifier.height(16.dp)) }
            }
        }

        // Dialogs for Deck Creation
        if (showCreateDeckDialog) {
            Dialog(onDismissRequest = { showCreateDeckDialog = false }) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Create Knowledge Deck", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(
                            value = newDeckName,
                            onValueChange = { newDeckName = it },
                            label = { Text("Deck Name") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(onClick = { showCreateDeckDialog = false }) { Text("Cancel") }
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = {
                                    if (newDeckName.isNotBlank()) {
                                        viewModel.createDeck(newDeckName)
                                        newDeckName = ""
                                        showCreateDeckDialog = false
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = StudyColor)
                            ) { Text("Create") }
                        }
                    }
                }
            }
        }

        // Dialog for adding flashcard
        if (showAddCardDialog) {
            Dialog(onDismissRequest = { showAddCardDialog = false }) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Add New Flashcard", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(
                            value = newCardFront,
                            onValueChange = { newCardFront = it },
                            label = { Text("Front (Question/Concept)") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = newCardBack,
                            onValueChange = { newCardBack = it },
                            label = { Text("Back (Answer/Definition)") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(onClick = { showAddCardDialog = false }) { Text("Cancel") }
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = {
                                    if (newCardFront.isNotBlank() && newCardBack.isNotBlank()) {
                                        viewModel.addFlashcard(newCardFront, newCardBack)
                                        newCardFront = ""
                                        newCardBack = ""
                                        showAddCardDialog = false
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = StudyColor)
                            ) { Text("Add Card") }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TimerModeButton(
    label: String,
    selected: Boolean,
    color: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .background(
                if (selected) color else Color.Transparent,
                RoundedCornerShape(6.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
            color = if (selected) Color.White else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
    }
}

// --- Work & Productivity Screen ---
@Composable
fun WorkScreen(viewModel: DailyHubViewModel) {
    val tasks by viewModel.tasks.collectAsState()
    val threeWins by viewModel.threeWins.collectAsState()

    var showAddTaskDialog by remember { mutableStateOf(false) }
    var taskTitle by remember { mutableStateOf("") }
    var taskPriority by remember { mutableStateOf("Medium") }
    var taskDueDate by remember { mutableStateOf("Today") }

    // State for local 3 wins edits
    var win1 by remember { mutableStateOf("") }
    var win2 by remember { mutableStateOf("") }
    var win3 by remember { mutableStateOf("") }

    // Load initial 3 wins
    LaunchedEffect(threeWins) {
        threeWins?.let {
            win1 = it.win1
            win2 = it.win2
            win3 = it.win3
        }
    }

    AppBackground {
        Scaffold(
            bottomBar = { CustomBottomNavigation(Screen.Work) { viewModel.navigateTo(it) } },
            containerColor = Color.Transparent
        ) { innerPadding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    ModuleHeader(
                        title = "Work & Output",
                        subtitle = "Track daily goals and checklist accomplishments.",
                        icon = Icons.Default.Work,
                        accentColor = WorkColor,
                        onBack = { viewModel.navigateTo(Screen.Dashboard) }
                    )
                }

                // 1. Weekly progress Card
                item {
                    val completed = tasks.filter { it.isCompleted }.size
                    val total = tasks.size
                    val completionPercent = if (total > 0) (completed.toFloat() / total.toFloat() * 100).toInt() else 0

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(24.dp)),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    "WEEKLY PROGRESS",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    "$completed task(s) completed out of $total total.",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .size(54.dp)
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "$completionPercent%",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }

                // 2. 3 Wins Today
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(24.dp)),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = FactsColor)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    "DAILY 3 WINS",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                                    color = FactsColor
                                )
                            }

                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "Lock in your 3 essential victories for today:",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            OutlinedTextField(
                                value = win1,
                                onValueChange = { win1 = it },
                                label = { Text("Win #1") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = win2,
                                onValueChange = { win2 = it },
                                label = { Text("Win #2") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = win3,
                                onValueChange = { win3 = it },
                                label = { Text("Win #3") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp)
                            )

                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = { viewModel.saveThreeWins(win1, win2, win3) },
                                modifier = Modifier.align(Alignment.End),
                                colors = ButtonDefaults.buttonColors(containerColor = FactsColor),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("Save Victories", color = Color.Black, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // 3. Task List checklist
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "TASKS & GOALS",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                        Button(
                            onClick = { showAddTaskDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = WorkColor),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Add Task", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }

                if (tasks.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "No tasks logged yet. Create some key goals!",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                } else {
                    items(tasks) { task ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (task.isCompleted) {
                                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                                } else {
                                    MaterialTheme.colorScheme.surface
                                }
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = task.isCompleted,
                                    onCheckedChange = { viewModel.toggleTaskCompletion(task) }
                                )

                                Spacer(modifier = Modifier.width(8.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = task.title,
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            textDecoration = if (task.isCompleted) androidx.compose.ui.text.style.TextDecoration.LineThrough else null
                                        ),
                                        color = if (task.isCompleted) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f) else MaterialTheme.colorScheme.onSurface
                                    )
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        // Priority Tag
                                        PriorityTag(priority = task.priority)

                                        // Due Date
                                        Text(
                                            "Due: ${task.dueDate}",
                                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                        )
                                    }
                                }

                                IconButton(onClick = { viewModel.deleteTask(task) }) {
                                    Icon(
                                        Icons.Default.Delete,
                                        contentDescription = "Delete",
                                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f)
                                    )
                                }
                            }
                        }
                    }
                }

                item { Spacer(modifier = Modifier.height(16.dp)) }
            }
        }

        // Dialog for adding task
        if (showAddTaskDialog) {
            Dialog(onDismissRequest = { showAddTaskDialog = false }) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Add New Task", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(
                            value = taskTitle,
                            onValueChange = { taskTitle = it },
                            label = { Text("Task Title") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Priority Level", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf("High", "Medium", "Low").forEach { p ->
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .background(
                                            if (taskPriority == p) WorkColor else MaterialTheme.colorScheme.surfaceVariant,
                                            RoundedCornerShape(8.dp)
                                        )
                                        .clickable { taskPriority = p }
                                        .padding(vertical = 10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        p,
                                        color = if (taskPriority == p) Color.White else MaterialTheme.colorScheme.onSurface,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(
                            value = taskDueDate,
                            onValueChange = { taskDueDate = it },
                            label = { Text("Due Date / Time") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(onClick = { showAddTaskDialog = false }) { Text("Cancel") }
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = {
                                    if (taskTitle.isNotBlank()) {
                                        viewModel.addTask(taskTitle, taskPriority, taskDueDate)
                                        taskTitle = ""
                                        showAddTaskDialog = false
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = WorkColor)
                            ) { Text("Create") }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PriorityTag(priority: String) {
    val color = when (priority) {
        "High" -> Color(0xFFD90429)
        "Medium" -> Color(0xFFF77F00)
        else -> Color(0xFF023E8A)
    }

    Box(
        modifier = Modifier
            .background(color.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(
            text = priority,
            style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
            color = color
        )
    }
}

// --- Lifestyle & Habits Screen ---
@Composable
fun LifestyleScreen(viewModel: DailyHubViewModel) {
    val habits by viewModel.habits.collectAsState()
    val moodLogs by viewModel.moodLogs.collectAsState()
    val reflection by viewModel.dailyReflection.collectAsState()

    var showAddHabitDialog by remember { mutableStateOf(false) }
    var habitName by remember { mutableStateOf("") }

    var moodNote by remember { mutableStateOf("") }
    var selectedEmoji by remember { mutableStateOf("😊") }

    var reflectionAnswer by remember { mutableStateOf("") }
    val reflectionPrompt = "What made you smile today and what are you grateful for?"

    LaunchedEffect(reflection) {
        reflection?.let {
            reflectionAnswer = it.answer
        }
    }

    AppBackground {
        Scaffold(
            bottomBar = { CustomBottomNavigation(Screen.Lifestyle) { viewModel.navigateTo(it) } },
            containerColor = Color.Transparent
        ) { innerPadding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    ModuleHeader(
                        title = "Lifestyle & Health",
                        subtitle = "Track daily healthy habit streaks and emotional check-ins.",
                        icon = Icons.Default.Favorite,
                        accentColor = LifestyleColor,
                        onBack = { viewModel.navigateTo(Screen.Dashboard) }
                    )
                }

                // 1. Emoji Mood Tracker Check-In
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(24.dp)),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Text(
                                "DAILY MOOD CHECK-IN",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                                color = LifestyleColor
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            // Emojis row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                listOf("😭", "😔", "😐", "😊", "🤩").forEach { emoji ->
                                    Box(
                                        modifier = Modifier
                                            .size(46.dp)
                                            .background(
                                                if (selectedEmoji == emoji) LifestyleColor.copy(alpha = 0.2f) else Color.Transparent,
                                                CircleShape
                                            )
                                            .border(
                                                1.dp,
                                                if (selectedEmoji == emoji) LifestyleColor else Color.Transparent,
                                                CircleShape
                                            )
                                            .clickable { selectedEmoji = emoji }
                                            .padding(6.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(emoji, fontSize = 24.sp)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            OutlinedTextField(
                                value = moodNote,
                                onValueChange = { moodNote = it },
                                placeholder = { Text("What's on your mind?") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp)
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            Button(
                                onClick = {
                                    viewModel.logMood(selectedEmoji, moodNote)
                                    moodNote = ""
                                },
                                modifier = Modifier.align(Alignment.End),
                                colors = ButtonDefaults.buttonColors(containerColor = LifestyleColor),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("Check In", fontWeight = FontWeight.Bold)
                            }

                            if (moodLogs.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    "Mood History",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                moodLogs.take(5).forEach { log ->
                                    val dateStr = SimpleDateFormat("MMM dd • HH:mm", Locale.getDefault()).format(Date(log.timestamp))
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(log.emoji, fontSize = 20.sp)
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Column {
                                                Text(log.note, style = MaterialTheme.typography.bodyMedium)
                                                Text(dateStr, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f))
                                            }
                                        }
                                        IconButton(onClick = { viewModel.deleteMoodLog(log) }) {
                                            Icon(
                                                Icons.Default.Delete,
                                                contentDescription = "Delete",
                                                tint = MaterialTheme.colorScheme.error.copy(alpha = 0.6f),
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // 2. Habits Tracker List
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "HABITS & STREAKS",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                        Button(
                            onClick = { showAddHabitDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = LifestyleColor),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("New Habit", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }

                if (habits.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "No habits tracked yet. Set up something healthy!",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                } else {
                    items(habits) { habit ->
                        val today = viewModel.getTodayDateString()
                        val loggedToday = habit.lastLoggedDate == today

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .background(
                                                if (loggedToday) StudyColor.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant,
                                                CircleShape
                                            )
                                            .clickable(enabled = !loggedToday) { viewModel.logHabitToday(habit) },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = if (loggedToday) Icons.Default.Check else Icons.Default.Whatshot,
                                            contentDescription = null,
                                            tint = if (loggedToday) StudyColor else LifestyleColor
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Column {
                                        Text(
                                            habit.name,
                                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                                        )
                                        Text(
                                            "🔥 Streak: ${habit.streak} days",
                                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                            color = LifestyleColor
                                        )
                                    }
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (!loggedToday) {
                                        TextButton(onClick = { viewModel.logHabitToday(habit) }) {
                                            Text("LOG", color = LifestyleColor, fontWeight = FontWeight.Bold)
                                        }
                                    } else {
                                        Text(
                                            "DONE",
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                            color = StudyColor,
                                            modifier = Modifier.padding(horizontal = 8.dp)
                                        )
                                    }

                                    IconButton(onClick = { viewModel.deleteHabit(habit) }) {
                                        Icon(
                                            Icons.Default.Delete,
                                            contentDescription = "Delete",
                                            tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // 3. Reflective Prompt Card
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Text(
                                "DAILY REFLECTION",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                                color = StudyColor
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                reflectionPrompt,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            OutlinedTextField(
                                value = reflectionAnswer,
                                onValueChange = { reflectionAnswer = it },
                                placeholder = { Text("Take a brief moment to reflect...") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Button(
                                onClick = { viewModel.saveReflection(reflectionPrompt, reflectionAnswer) },
                                modifier = Modifier.align(Alignment.End),
                                colors = ButtonDefaults.buttonColors(containerColor = StudyColor),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("Save Reflection", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                item { Spacer(modifier = Modifier.height(16.dp)) }
            }
        }

        // Dialog for adding habit
        if (showAddHabitDialog) {
            Dialog(onDismissRequest = { showAddHabitDialog = false }) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Add New Health Habit", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(
                            value = habitName,
                            onValueChange = { habitName = it },
                            label = { Text("Habit Name (e.g. Drink 3L Water)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(onClick = { showAddHabitDialog = false }) { Text("Cancel") }
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = {
                                    if (habitName.isNotBlank()) {
                                        viewModel.addHabit(habitName)
                                        habitName = ""
                                        showAddHabitDialog = false
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = LifestyleColor)
                            ) { Text("Create") }
                        }
                    }
                }
            }
        }
    }
}

// --- Facts Screen (Detailed) ---
@Composable
fun FactsScreen(viewModel: DailyHubViewModel) {
    val currentFact by viewModel.currentFact.collectAsState()
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current

    AppBackground {
        Scaffold(
            bottomBar = { CustomBottomNavigation(Screen.Dashboard) { viewModel.navigateTo(it) } },
            containerColor = Color.Transparent
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top
            ) {
                ModuleHeader(
                    title = "Did You Know?",
                    subtitle = "Explore our local seed of 100+ highly curated educational facts.",
                    icon = Icons.Default.Lightbulb,
                    accentColor = FactsColor,
                    onBack = { viewModel.navigateTo(Screen.Dashboard) }
                )

                Spacer(modifier = Modifier.height(48.dp))

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .background(FactsColor.copy(alpha = 0.15f), CircleShape)
                                .padding(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Text(
                                currentFact.category.uppercase(),
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                                color = FactsColor
                            )
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        Text(
                            text = currentFact.text,
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                                lineHeight = 32.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(modifier = Modifier.height(32.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            OutlinedButton(
                                onClick = { viewModel.rotateFact() },
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, FactsColor)
                            ) {
                                Icon(Icons.Default.Refresh, contentDescription = null, tint = FactsColor)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("New Fact", color = FactsColor)
                            }

                            Button(
                                onClick = {
                                    val formatted = "[DailyHub Did You Know?]\n" +
                                            "Category: ${currentFact.category}\n\n" +
                                            "\"${currentFact.text}\"\n\n" +
                                            "Shared via DailyHub Companion App"
                                    clipboardManager.setText(AnnotatedString(formatted))
                                    Toast.makeText(context, "Beautiful Fact Card copied!", Toast.LENGTH_SHORT).show()
                                },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = FactsColor)
                            ) {
                                Icon(Icons.Default.Share, contentDescription = null, tint = Color.Black)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Share Snapshot", color = Color.Black, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

// --- FoxAI Chat Assistant Screen ---
@Composable
fun FoxAiScreen(viewModel: DailyHubViewModel) {
    val messages by viewModel.foxAiMessages.collectAsState()
    val isThinking by viewModel.isFoxAiThinking.collectAsState()

    var chatInputText by remember { mutableStateOf("") }

    AppBackground {
        Scaffold(
            bottomBar = { CustomBottomNavigation(Screen.FoxAi) { viewModel.navigateTo(it) } },
            containerColor = Color.Transparent
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                // Custom Mascot Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = { viewModel.navigateTo(Screen.Dashboard) },
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }

                        Image(
                            painter = painterResource(id = R.drawable.img_fox_mascot),
                            contentDescription = "Fox Mascot",
                            modifier = Modifier
                                .size(46.dp)
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Text(
                                "FoxAI Assistant",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                "Mascot • Free Gemini Flash v1.5",
                                style = MaterialTheme.typography.bodySmall,
                                color = StudyColor
                            )
                        }
                    }

                    // Trash button to clear logs
                    IconButton(onClick = { viewModel.clearFoxAiHistory() }) {
                        Icon(Icons.Default.Delete, contentDescription = "Clear chat history", tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f))
                    }
                }

                // Chat Messages Space
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    if (messages.isEmpty()) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState())
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.img_fox_mascot),
                                contentDescription = "Fox Mascot Icon",
                                modifier = Modifier
                                    .size(90.dp)
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                "Hi! I'm FoxAI!",
                                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Black)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "I am a witty, encouraging fox. I have full knowledge of your Study stats, Work task list, and mood history! Ask me anything, like:",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(16.dp))

                            // Suggestions Chips
                            SuggestionChipItem("How is my day looking?") {
                                chatInputText = "How is my day looking?"
                            }
                            SuggestionChipItem("Am I doing good on streaks?") {
                                chatInputText = "Am I doing good on streaks?"
                            }
                            SuggestionChipItem("Give me a study motivation quote!") {
                                chatInputText = "Give me a study motivation quote?"
                            }
                        }
                    } else {
                        val scrollState = rememberScrollState()
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(scrollState)
                                .padding(horizontal = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Spacer(modifier = Modifier.height(8.dp))
                            messages.forEach { msg ->
                                ChatBubbleItem(message = msg)
                            }
                            if (isThinking) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(16.dp),
                                        strokeWidth = 2.dp,
                                        color = FoxAiColor
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        "FoxAI is typing a personalized message...",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))

                            // Keep scroll synced
                            LaunchedEffect(messages.size, isThinking) {
                                scrollState.animateScrollTo(scrollState.maxValue)
                            }
                        }
                    }
                }

                // Input Bar (Respect insets)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = chatInputText,
                        onValueChange = { chatInputText = it },
                        placeholder = { Text("Ask your fox companion...") },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("chat_input"),
                        shape = RoundedCornerShape(20.dp),
                        maxLines = 3,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                        keyboardActions = KeyboardActions(onSend = {
                            if (chatInputText.isNotBlank()) {
                                viewModel.sendFoxAiMessage(chatInputText)
                                chatInputText = ""
                            }
                        })
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = {
                            if (chatInputText.isNotBlank()) {
                                viewModel.sendFoxAiMessage(chatInputText)
                                chatInputText = ""
                            }
                        },
                        modifier = Modifier
                            .background(FoxAiColor, CircleShape)
                            .size(44.dp)
                            .testTag("send_button")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send", tint = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
fun SuggestionChipItem(text: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .padding(vertical = 4.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Text(
            text,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.secondary
        )
    }
}

@Composable
fun ChatBubbleItem(message: FoxAiMessage) {
    val alignment = if (message.isUser) Alignment.End else Alignment.Start
    val containerColor = if (message.isUser) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }
    val contentColor = if (message.isUser) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = alignment
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = 280.dp)
                .background(
                    containerColor,
                    shape = RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp,
                        bottomStart = if (message.isUser) 16.dp else 4.dp,
                        bottomEnd = if (message.isUser) 4.dp else 16.dp
                    )
                )
                .padding(12.dp)
        ) {
            Text(
                text = message.text,
                style = MaterialTheme.typography.bodyMedium,
                color = contentColor
            )
        }
        Text(
            text = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(message.timestamp)),
            style = MaterialTheme.typography.bodySmall.copy(fontSize = 9.sp),
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
        )
    }
}

// --- Settings Screen ---
@Composable
fun SettingsScreen(viewModel: DailyHubViewModel) {
    val user by viewModel.currentUser.collectAsState()
    val authError by viewModel.authError.collectAsState()

    var showPasswordDialog by remember { mutableStateOf(false) }
    var newPasswordInput by remember { mutableStateOf("") }

    var showTimerSettingsDialog by remember { mutableStateOf(false) }
    var focusDurationMin by remember { mutableIntStateOf(user?.focusDurationMinutes ?: 25) }
    var shortBreakMin by remember { mutableIntStateOf(user?.shortBreakMinutes ?: 5) }
    var longBreakMin by remember { mutableIntStateOf(user?.longBreakMinutes ?: 15) }

    AppBackground {
        Scaffold(
            bottomBar = { CustomBottomNavigation(Screen.Settings) { viewModel.navigateTo(it) } },
            containerColor = Color.Transparent
        ) { innerPadding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    ModuleHeader(
                        title = "Account Settings",
                        subtitle = "Manage secure local password encryption and configuration parameters.",
                        icon = Icons.Default.Settings,
                        accentColor = StudyColor,
                        onBack = { viewModel.navigateTo(Screen.Dashboard) }
                    )
                }

                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Text(
                                "PROFILE DETAILS",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                                color = StudyColor
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(46.dp)
                                        .background(StudyColor.copy(alpha = 0.2f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Person, contentDescription = null, tint = StudyColor)
                                }
                                Spacer(modifier = Modifier.width(16.dp))
                                Column {
                                    Text(
                                        user?.username ?: "Guest User",
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                    )
                                    Text(
                                        "Secure local SQLite Room DB account",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                    )
                                }
                            }
                        }
                    }
                }

                // Timer durations
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Text(
                                "POMODORO CONFIGURATION",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                                color = StudyColor
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("Focus Duration: ${user?.focusDurationMinutes} minutes", style = MaterialTheme.typography.bodyMedium)
                            Text("Short Break: ${user?.shortBreakMinutes} minutes", style = MaterialTheme.typography.bodyMedium)
                            Text("Long Break: ${user?.longBreakMinutes} minutes", style = MaterialTheme.typography.bodyMedium)
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = { showTimerSettingsDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = StudyColor),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("Edit Durations")
                            }
                        }
                    }
                }

                // Security actions
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Text(
                                "SECURITY & ACTIONS",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                                color = MaterialTheme.colorScheme.error
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            if (authError != null) {
                                Text(
                                    authError ?: "",
                                    color = StudyColor,
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                    modifier = Modifier.padding(bottom = 12.dp)
                                )
                            }

                            Button(
                                onClick = { showPasswordDialog = true },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                            ) {
                                Text("Change Secure Password")
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Button(
                                onClick = { viewModel.logout() },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                Text("Sign Out", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }

                            Spacer(modifier = Modifier.height(16.dp))
                            HorizontalDivider(color = MaterialTheme.colorScheme.error.copy(alpha = 0.1f))
                            Spacer(modifier = Modifier.height(12.dp))

                            Button(
                                onClick = { viewModel.deleteAccount() },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                            ) {
                                Text("Permanently Delete Account & Data", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // Privacy/Usage limits statement
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.2f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                "TECHNICAL & PRIVACY NOTICE",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.secondary
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "• Security Notice: This local profile hashes and salts passwords securely in our SQLite Room DB.\n" +
                                "• FoxAI Assistant: Powered by Google Gemini v1.5 Flash API keys. Quota limits are shared per-project. Your chatbot context contains elements of your dashboard metrics for hyper-personalized witty response rendering.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                    }
                }

                item { Spacer(modifier = Modifier.height(20.dp)) }
            }
        }

        // Dialog for Change Password
        if (showPasswordDialog) {
            Dialog(onDismissRequest = { showPasswordDialog = false }) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Change Secure Password", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(
                            value = newPasswordInput,
                            onValueChange = { newPasswordInput = it },
                            label = { Text("New Password") },
                            singleLine = true,
                            visualTransformation = PasswordVisualTransformation(),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(onClick = { showPasswordDialog = false }) { Text("Cancel") }
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = {
                                    if (newPasswordInput.isNotBlank()) {
                                        viewModel.updatePassword(newPasswordInput)
                                        newPasswordInput = ""
                                        showPasswordDialog = false
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = StudyColor)
                            ) { Text("Update") }
                        }
                    }
                }
            }
        }

        // Dialog for Timer Durations edit
        if (showTimerSettingsDialog) {
            Dialog(onDismissRequest = { showTimerSettingsDialog = false }) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Edit Pomodoro Durations", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                        Spacer(modifier = Modifier.height(12.dp))

                        Text("Focus Minutes: $focusDurationMin", style = MaterialTheme.typography.labelSmall)
                        Slider(
                            value = focusDurationMin.toFloat(),
                            onValueChange = { focusDurationMin = it.toInt() },
                            valueRange = 1f..60f,
                            steps = 59
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text("Short Break Minutes: $shortBreakMin", style = MaterialTheme.typography.labelSmall)
                        Slider(
                            value = shortBreakMin.toFloat(),
                            onValueChange = { shortBreakMin = it.toInt() },
                            valueRange = 1f..30f,
                            steps = 29
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text("Long Break Minutes: $longBreakMin", style = MaterialTheme.typography.labelSmall)
                        Slider(
                            value = longBreakMin.toFloat(),
                            onValueChange = { longBreakMin = it.toInt() },
                            valueRange = 1f..45f,
                            steps = 44
                        )

                        Spacer(modifier = Modifier.height(16.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(onClick = { showTimerSettingsDialog = false }) { Text("Cancel") }
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = {
                                    viewModel.saveTimerSettings(focusDurationMin, shortBreakMin, longBreakMin)
                                    showTimerSettingsDialog = false
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = StudyColor)
                            ) { Text("Save Settings") }
                        }
                    }
                }
            }
        }
    }
}
