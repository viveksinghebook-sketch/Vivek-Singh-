package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.ContentItem
import com.example.data.model.Doubt
import com.example.data.model.StudentProfile
import com.example.data.model.TestAttempt
import com.example.ui.theme.*
import com.example.ui.viewmodel.AppViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RrCoachingApp(viewModel: AppViewModel) {
    val context = LocalContext.current
    val student by viewModel.studentProfile.collectAsStateWithLifecycle()
    val doubts by viewModel.allDoubts.collectAsStateWithLifecycle()
    val testAttempts by viewModel.allTestAttempts.collectAsStateWithLifecycle()
    val allContents by viewModel.allContentItems.collectAsStateWithLifecycle()

    var activeTab by remember { mutableStateOf("HOME") } // HOME, LIVE, RECORDED, PDF_NOTES, BOOKS, TESTS, DOUBTS, PROFILE, ROLES, SETTINGS

    // Force role sync dynamically
    val currentRole = viewModel.appRoleMode

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            color = AcadOrangeAccent,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = "RR",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                            }
                        }
                        Column {
                            Text(
                                text = "RR COACHING CENTRE",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    color = AcadBluePrimary,
                                    letterSpacing = 0.5.sp
                                )
                            )
                            Text(
                                text = "BY VINAY SIR • CLASSES 9th - 12th",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = AcadOrangeAccent
                                )
                            )
                        }
                    }
                },
                actions = {
                    // Quick Role Switcher for teachers and admins
                    IconButton(
                        onClick = { activeTab = "ROLES" },
                        modifier = Modifier.testTag("app_role_quick_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.SwitchAccount,
                            contentDescription = "Switch Roles",
                            tint = AcadBluePrimary
                        )
                    }

                    if (student != null) {
                        IconButton(
                            onClick = {
                                viewModel.logout()
                                activeTab = "HOME"
                                Toast.makeText(context, "Logged Out successfully", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.testTag("logout_toolbar_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Logout,
                                contentDescription = "Log out",
                                tint = LiveRed
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = AcadBluePrimary
                )
            )
        },
        bottomBar = {
            if (student != null && activeTab != "QUIZ_ACTIVE" && activeTab != "LIVE_ROOM") {
                NavigationBar(
                    containerColor = Color.White,
                    tonalElevation = 8.dp,
                    modifier = Modifier.height(72.dp)
                ) {
                    val items = listOf(
                        Triple("HOME", "Home", Icons.Filled.Home),
                        Triple("LIVE", "Live Class", Icons.Filled.LiveTv),
                        Triple("RECORDED", "Recorded", Icons.Filled.PlayCircle),
                        Triple("TESTS", "Tests", Icons.Filled.Quiz),
                        Triple("DOUBTS", "Doubts AI", Icons.Filled.Comment),
                        Triple("PROFILE", "Profile", Icons.Filled.AccountCircle)
                    )
                    items.forEach { (tabId, label, icon) ->
                        val isSelected = activeTab == tabId
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = { activeTab = tabId },
                            icon = {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = label,
                                    tint = if (isSelected) AcadBluePrimary else Color.Gray,
                                    modifier = Modifier.size(24.dp)
                                )
                            },
                            label = {
                                Text(
                                    text = label,
                                    fontSize = 9.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) AcadBluePrimary else Color.Gray,
                                    maxLines = 1
                                )
                            },
                            alwaysShowLabel = true,
                            modifier = Modifier.testTag("nav_tab_$tabId")
                        )
                    }
                }
            }
        },
        containerColor = AppLightBackground
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (student == null) {
                AuthScreenView(viewModel)
            } else {
                when (currentRole) {
                    "ADMIN" -> AdminDashboardScreenView(viewModel, allContents)
                    "TEACHER" -> TeacherDashboardScreenView(viewModel, allContents)
                    else -> {
                        // Standard Student Tabs
                        when (activeTab) {
                            "HOME" -> DashboardScreenView(
                                viewModel = viewModel,
                                student = student!!,
                                allContents = allContents,
                                onNavigateToLive = { item: ContentItem ->
                                    viewModel.selectLiveClass(item)
                                    activeTab = "LIVE_ROOM"
                                },
                                onNavigateToClassroomType = { tab: String ->
                                    activeTab = tab
                                }
                            )
                            "LIVE" -> LiveLecturesListView(
                                viewModel = viewModel,
                                allContents = allContents,
                                onSelectLive = { item: ContentItem ->
                                    viewModel.selectLiveClass(item)
                                    activeTab = "LIVE_ROOM"
                                }
                            )
                            "LIVE_ROOM" -> LiveClassRoomView(viewModel) {
                                activeTab = "LIVE"
                            }
                            "RECORDED" -> RecordedVideosScreenView(viewModel, allContents)
                            "PDF_NOTES" -> PdfNotesScreenView(viewModel, allContents)
                            "BOOKS" -> BooksLibraryScreenView(viewModel)
                            "TESTS" -> TestSeriesScreenView(viewModel, testAttempts) { score, title ->
                                activeTab = "TESTS"
                            }
                            "DOUBTS" -> DoubtAIScreenView(viewModel, doubts)
                            "PROFILE" -> StudentProfileScreenView(viewModel, student!!, testAttempts)
                            "ROLES" -> AppRoleSwitcherScreen(viewModel) {
                                activeTab = "HOME"
                            }
                            else -> DashboardScreenView(
                                viewModel = viewModel,
                                student = student!!,
                                allContents = allContents,
                                onNavigateToLive = { item: ContentItem ->
                                    viewModel.selectLiveClass(item)
                                    activeTab = "LIVE_ROOM"
                                },
                                onNavigateToClassroomType = { tab: String ->
                                    activeTab = tab
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

// ================= AUTHENTICATION VIEW SCREEN =================
@Composable
fun AuthScreenView(viewModel: AppViewModel) {
    var checkTerms by remember { mutableStateOf(true) }
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFFEFF6FF), Color.White)
                )
            )
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Spacer(modifier = Modifier.height(20.dp))

        // Large Institute Crest branding
        Surface(
            color = AcadBluePrimary,
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier
                .size(90.dp)
                .shadow(8.dp, RoundedCornerShape(24.dp))
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = "RR",
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize = 40.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Slogan in hindi & english
        Text(
            text = "RR COACHING CENTRE",
            fontWeight = FontWeight.Black,
            color = AcadBluePrimary,
            fontSize = 22.sp,
            textAlign = TextAlign.Center
        )
        Text(
            text = "BY VINAY SIR • CLASESS 9th - 12th\n\"Safalta Ki Shuruat, Vinay Sir Ke Saath\"",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = AcadOrangeAccent,
            textAlign = TextAlign.Center,
            lineHeight = 16.sp
        )

        Spacer(modifier = Modifier.height(32.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(4.dp, RoundedCornerShape(20.dp)),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "STUDENT PORTAL LOGIN",
                    fontWeight = FontWeight.ExtraBold,
                    color = AcadBluePrimary,
                    fontSize = 14.sp,
                    letterSpacing = 1.sp
                )

                if (viewModel.authErrorMsg != null) {
                    Text(
                        text = viewModel.authErrorMsg ?: "",
                        color = Color.Red,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // 1. Mobile Number
                OutlinedTextField(
                    value = viewModel.mobileInput,
                    onValueChange = { if (it.length <= 10) viewModel.mobileInput = it },
                    label = { Text("Mobile Number (10 Digits)") },
                    placeholder = { Text("E.g: 9876543210") },
                    leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("login_mobile_field"),
                    shape = RoundedCornerShape(12.dp)
                )

                // 2. Date of Birth Verification
                OutlinedTextField(
                    value = viewModel.dobInput,
                    onValueChange = { viewModel.dobInput = it },
                    label = { Text("Date of Birth (DD-MM-YYYY)") },
                    placeholder = { Text("E.g: 15-08-2010") },
                    leadingIcon = { Icon(Icons.Default.DateRange, contentDescription = null) },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("login_dob_field"),
                    shape = RoundedCornerShape(12.dp)
                )

                // 3. Class choice
                Column {
                    Text(
                        text = "Select Student Class:",
                        fontWeight = FontWeight.Bold,
                        color = Color.DarkGray,
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val classes = listOf("Class 9", "Class 10", "Class 11", "Class 12")
                        classes.forEach { mClass ->
                            val isSel = viewModel.selectedClassInput == mClass
                            Surface(
                                color = if (isSel) AcadBluePrimary else Color(0xFFF1F5F9),
                                contentColor = if (isSel) Color.White else Color.DarkGray,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { viewModel.selectedClassInput = mClass }
                            ) {
                                Box(
                                    modifier = Modifier.padding(vertical = 10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = mClass,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }

                // Privacy reminder
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Checkbox(
                        checked = checkTerms,
                        onCheckedChange = { checkTerms = it }
                    )
                    Text(
                        text = "Remember Login & agree with Student Terms.",
                        fontSize = 11.sp,
                        color = Color.Gray
                    )
                }

                if (!viewModel.otpSent) {
                    Button(
                        onClick = { viewModel.sendOtp() },
                        colors = ButtonDefaults.buttonColors(containerColor = AcadOrangeAccent),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("login_request_button"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Get OTP Verification Code", fontWeight = FontWeight.Bold)
                    }
                } else {
                    // OTP Authentication Field
                    Divider()
                    Text(
                        text = "Enter 4-Digit OTP sent to your cell. (Try: 1234)",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp,
                        color = AcadOrangeAccent
                    )

                    OutlinedTextField(
                        value = viewModel.otpInput,
                        onValueChange = { viewModel.otpInput = it },
                        label = { Text("Enter OTP Code") },
                        placeholder = { Text("Click verify with 1234") },
                        leadingIcon = { Icon(Icons.Default.LockClock, contentDescription = null) },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("login_otp_field"),
                        shape = RoundedCornerShape(12.dp)
                    )

                    if (viewModel.showOtpError) {
                        Text(
                            text = "Invalid OTP! Enter '1234' or '123456' to proceed.",
                            color = Color.Red,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Button(
                        onClick = { viewModel.verifyOtpAndLogin() },
                        colors = ButtonDefaults.buttonColors(containerColor = AcadBluePrimary),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("login_verify_button"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Verify & Secure Login", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "🔒 Secured App. Encoded databases with student privacy protection.",
            fontSize = 11.sp,
            color = Color.Gray
        )
    }
}

// ================= STUDENT PORTAL DASHBOARD SCREEN =================
@Composable
fun DashboardScreenView(
    viewModel: AppViewModel,
    student: StudentProfile,
    allContents: List<ContentItem>,
    onNavigateToLive: (ContentItem) -> Unit,
    onNavigateToClassroomType: (String) -> Unit
) {
    val liveClasses = allContents.filter { it.category == "LIVE" }
    val recentNotes = allContents.filter { it.category == "PDF" }.take(3)
    val announcements = allContents.filter { it.category == "ANNOUNCEMENT" }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("dashboard_school_scroller"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Core Header Aesthetic (Editorial Theme Style)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, Color(0xFFF1F5F9))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "STUDENT DASHBOARD",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = AcadOrangeAccent,
                        letterSpacing = 1.5.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Aapka Swagat Hai! 👋",
                                fontSize = 13.sp,
                                color = Color.Gray,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = student.name,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = AcadBluePrimary
                            )
                        }

                        // Avatar layout
                        Box(contentAlignment = Alignment.BottomEnd) {
                            Surface(
                                modifier = Modifier.size(54.dp),
                                shape = CircleShape,
                                color = Color(0xFFEFF6FF),
                                border = BorderStroke(2.dp, AcadBluePrimary)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    val initials = student.name.split(" ").filter { it.isNotEmpty() }
                                    val letters = if (initials.size >= 2) {
                                        "${initials[0].first()}${initials[1].first()}".uppercase()
                                    } else if (initials.isNotEmpty()) {
                                        initials[0].take(2).uppercase()
                                    } else "ST"
                                    Text(
                                        text = letters,
                                        fontWeight = FontWeight.Bold,
                                        color = AcadBluePrimary,
                                        fontSize = 18.sp
                                    )
                                }
                            }
                            // Active green dot online status
                            Box(
                                modifier = Modifier
                                    .size(13.dp)
                                    .background(Color(0xFF22C55E), CircleShape)
                                    .border(2.dp, Color.White, CircleShape)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            color = Color(0xFFEFF6FF),
                            shape = RoundedCornerShape(100.dp),
                            border = BorderStroke(1.dp, Color(0xFFDBEAFE))
                        ) {
                            Text(
                                text = "Hello, ${student.name}",
                                fontStyle = FontStyle.Italic,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = AcadBluePrimary,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                        Surface(
                            color = Color(0xFFF1F5F9),
                            shape = RoundedCornerShape(100.dp),
                            border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                        ) {
                            Text(
                                text = "${student.studentClass} • PCM Group",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color.DarkGray,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }

        // Live now active banner section values
        item {
            if (liveClasses.isNotEmpty()) {
                val liveClass = liveClasses.first()
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = AcadBluePrimary),
                    shape = RoundedCornerShape(32.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        // Live Pulse badge
                        Surface(
                            color = AcadOrangeAccent,
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.align(Alignment.Start)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .background(Color.White, CircleShape)
                                )
                                Text(
                                    text = "LIVE NOW",
                                    color = Color.White,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = liveClass.title,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "${liveClass.subject} • ${liveClass.postedBy} • Started 15m ago",
                            fontSize = 12.sp,
                            color = Color(0xFFDBEAFE),
                            fontWeight = FontWeight.Medium
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = { onNavigateToLive(liveClass) },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("join_live_btn"),
                            shape = RoundedCornerShape(16.dp),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                        ) {
                            Text(
                                "Join Live Classroom",
                                color = AcadBluePrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            } else {
                // Default placeholder
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                    shape = RoundedCornerShape(24.dp),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "No Live Classes Running Right Now",
                            fontWeight = FontWeight.Bold,
                            color = Color.Gray,
                            fontSize = 13.sp
                        )
                        Text(
                            text = "Regular classes schedule: 4:00 PM to 8:00 PM Daily",
                            color = Color.LightGray,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }

        // Module Shortcuts Grid (Editorial design with 4 columns/row)
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "STUDY MODULES INDEX",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Gray,
                    letterSpacing = 1.sp
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    listOf(
                        Triple("RECORDED", "Recorded", "🎥"),
                        Triple("PDF_NOTES", "Notes PDF", "📄"),
                        Triple("BOOKS", "e-Books", "📚"),
                        Triple("TESTS", "Exam Hub", "🎯")
                    ).forEach { (destination, label, icon) ->
                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { onNavigateToClassroomType(destination) },
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 12.dp, horizontal = 4.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(text = icon, fontSize = 24.sp)
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = label,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF475569),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }
        }

        // Attendance / progress tracker
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.size(54.dp)
                    ) {
                        CircularProgressIndicator(
                            progress = student.attendancePercent / 100f,
                            color = AcadOrangeAccent,
                            strokeWidth = 6.dp,
                            modifier = Modifier.fillMaxSize()
                        )
                        Text(
                            text = "${student.attendancePercent}%",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = AcadBluePrimary
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Daily Attendance Progress",
                            fontWeight = FontWeight.Bold,
                            color = Color.DarkGray,
                            fontSize = 13.sp
                        )
                        Text(
                            text = "Good job! You have satisfied board requirements of 75% attendance.",
                            fontSize = 11.sp,
                            color = Color.Gray
                        )
                    }
                }
            }
        }

        // Recent Study Materials Card
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "RECENT REVISION NOTES",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Gray,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "View All",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = AcadBluePrimary,
                        modifier = Modifier.clickable { onNavigateToClassroomType("PDF_NOTES") }
                    )
                }

                recentNotes.forEach { pdf ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onNavigateToClassroomType("PDF_NOTES") },
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, Color(0xFFF1F5F9))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .background(Color(0xFFFFF7ED), RoundedCornerShape(12.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("📄", fontSize = 20.sp)
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = pdf.title,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.DarkGray
                                )
                                Text(
                                    text = "${pdf.subject} • ${pdf.durationOrSize}",
                                    fontSize = 11.sp,
                                    color = Color.Gray
                                )
                            }

                            Icon(
                                imageVector = Icons.Default.ArrowForward,
                                contentDescription = null,
                                tint = Color.LightGray,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }

        // Announcement feeds
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "RECENT ANNOUNCEMENTS",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Gray,
                    letterSpacing = 1.sp
                )

                announcements.reversed().take(3).forEach { ann ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, Color(0xFFF1F5F9))
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Surface(
                                    color = Color(0xFFFFF1F2),
                                    shape = RoundedCornerShape(100.dp)
                                ) {
                                    Text(
                                        text = ann.subject,
                                        fontSize = 10.sp,
                                        color = Color(0xFFE11D48),
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                    )
                                }
                                Text(text = "Announced Today", fontSize = 10.sp, color = Color.LightGray)
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(text = ann.title, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = ann.details, fontSize = 12.sp, color = Color.Gray)
                        }
                    }
                }
            }
        }
    }
}

// ================= LIVE CLASS EXPERIENCE SCREEN =================
@Composable
fun LiveClassRoomView(viewModel: AppViewModel, onBack: () -> Unit) {
    val liveClassItem = viewModel.currentLiveClassItem
    var currentTxtMessage by remember { mutableStateOf("") }

    if (liveClassItem == null) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("No Live Classroom Active")
            Button(onClick = onBack) { Text("Back") }
        }
        return
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Mock Video Display screen
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1.77f)
                .background(Color.Black),
            contentAlignment = Alignment.BottomCenter
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "🎥 SIMULATED LIVE VIDEO STREAM",
                    color = Color.LightGray,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
                Text(
                    text = "Quality: ${viewModel.videoQualityState} • Audio: ${if (viewModel.isVideoMuted) "MUTED" else "ACTIVE"}",
                    color = AcadOrangeAccent,
                    fontSize = 11.sp
                )
            }

            // Player controllers layer
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Black.copy(alpha = 0.6f))
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = { viewModel.isVideoMuted = !viewModel.isVideoMuted }) {
                    Icon(
                        imageVector = if (viewModel.isVideoMuted) Icons.Default.VolumeMute else Icons.Default.VolumeUp,
                        contentDescription = "Mute button",
                        tint = Color.White
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    val qualities = listOf("360p", "480p", "720p", "1080p")
                    qualities.forEach { q ->
                        val isPicked = viewModel.videoQualityState == q
                        Text(
                            text = q,
                            color = if (isPicked) AcadOrangeAccent else Color.White,
                            modifier = Modifier
                                .clickable { viewModel.videoQualityState = q }
                                .padding(horizontal = 6.dp),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Live Chat Header section
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = liveClassItem.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = AcadBluePrimary
                )
                Text(text = "Instructor: ${liveClassItem.postedBy} • 142 classmates online", fontSize = 11.sp, color = Color.Gray)
            }

            IconButton(
                onClick = {
                    viewModel.isRaisedHand = !viewModel.isRaisedHand
                }
            ) {
                Icon(
                    imageVector = Icons.Default.BackHand,
                    contentDescription = "Raise Hand",
                    tint = if (viewModel.isRaisedHand) AcadOrangeAccent else Color.Gray
                )
            }
        }

        Divider()

        // Chat messages scroller feed
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(Color(0xFFF8FAFC)),
            contentPadding = PaddingValues(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(viewModel.liveChatMessages) { (sender, content) ->
                val isTeacher = sender.contains("Sir")
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isTeacher) Color(0xFFEFF6FF) else Color.White
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            text = sender,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 11.sp,
                            color = if (isTeacher) AcadOrangeAccent else AcadBluePrimary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(text = content, fontSize = 12.sp, color = Color.DarkGray)
                    }
                }
            }
        }

        // Chat input text bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = currentTxtMessage,
                onValueChange = { currentTxtMessage = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("Ask Vinay Sir your live question...") },
                singleLine = true,
                shape = RoundedCornerShape(100.dp)
            )

            Spacer(modifier = Modifier.width(8.dp))

            IconButton(
                onClick = {
                    if (currentTxtMessage.trim().isNotEmpty()) {
                        viewModel.sendLiveMessage("Rohan Sharma", currentTxtMessage)
                        currentTxtMessage = ""
                    }
                }
            ) {
                Icon(imageVector = Icons.Default.Send, contentDescription = "Send Message", tint = AcadBluePrimary)
            }
        }
    }
}

// ================= RECORDED VIDEOS LIST AND VIEW =================
@Composable
fun RecordedVideosScreenView(viewModel: AppViewModel, allContents: List<ContentItem>) {
    var searchQuery by remember { mutableStateOf("") }
    val recordedItems = allContents.filter {
        it.category == "RECORDED" && (searchQuery.isEmpty() || it.title.contains(searchQuery, ignoreCase = true))
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "RECORDED CLASESS ARCHIVE",
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = AcadOrangeAccent,
            letterSpacing = 1.sp
        )
        Text(text = "High definition board exam lectures and theory sessions.", fontSize = 11.sp, color = Color.Gray)

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search recorded chapters (e.g. Kinetics, Vector...)") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(14.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(recordedItems) { item ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, Color(0xFFF1F5F9))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .background(Color(0xFFEFF6FF), RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null, tint = AcadBluePrimary)
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = item.title, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray)
                            Text(text = "${item.subject} • ${item.durationOrSize} • By ${item.postedBy}", fontSize = 11.sp, color = Color.Gray)
                        }

                        Button(
                            onClick = {
                                viewModel.selectLiveClass(item)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = AcadBluePrimary),
                            shape = RoundedCornerShape(100.dp)
                        ) {
                            Text("Play", fontSize = 11.sp)
                        }
                    }
                }
            }
        }
    }
}

// ================= PDF NOTES AND BOOKMARKS =================
@Composable
fun PdfNotesScreenView(viewModel: AppViewModel, allContents: List<ContentItem>) {
    val pdfs = allContents.filter { it.category == "PDF" }
    val activePdf = viewModel.activePdfItem

    if (activePdf != null) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { viewModel.activePdfItem = null }) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
                Text(text = activePdf.title, fontWeight = FontWeight.Bold, fontSize = 13.sp, modifier = Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(12.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, Color(0xFFF1F5F9))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = "📖 SIMULATED DOCUMENT PAGE VIEW", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = AcadBluePrimary)
                    Text(text = "Topic: ${activePdf.details}", color = Color.Gray, fontSize = 12.sp, textAlign = TextAlign.Center)
                    Spacer(modifier = Modifier.height(20.dp))
                    Text(text = "Page ${viewModel.activePdfCurrentPage} of 18", fontWeight = FontWeight.Bold, color = AcadOrangeAccent)

                    Spacer(modifier = Modifier.height(20.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        Button(
                            onClick = { if (viewModel.activePdfCurrentPage > 1) viewModel.activePdfCurrentPage-- },
                            enabled = viewModel.activePdfCurrentPage > 1
                        ) {
                            Text("Prev")
                        }
                        Button(
                            onClick = { if (viewModel.activePdfCurrentPage < 18) viewModel.activePdfCurrentPage++ },
                            enabled = viewModel.activePdfCurrentPage < 18
                        ) {
                            Text("Next")
                        }
                    }
                }
            }
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text(
                text = "STUDY NOTES & CLASSROOM PDFs",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = AcadOrangeAccent,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(10.dp))

            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(pdfs) { pdf ->
                    val isBookmarked = viewModel.bookmarkedNotesIds.contains(pdf.id)
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, Color(0xFFF1F5F9))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .background(Color(0xFFFFF7ED), RoundedCornerShape(10.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("📄", fontSize = 20.sp)
                                }

                                Column {
                                    Text(text = pdf.title, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray)
                                    Text(text = "${pdf.subject} • ${pdf.durationOrSize}", fontSize = 11.sp, color = Color.Gray)
                                    Text(text = pdf.details, fontSize = 11.sp, fontStyle = FontStyle.Italic, color = Color.LightGray)
                                }
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(onClick = { viewModel.toggleBookmarkNote(pdf.id) }) {
                                    Icon(
                                        imageVector = if (isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                        contentDescription = "Bookmark notes",
                                        tint = if (isBookmarked) AcadOrangeAccent else Color.LightGray
                                    )
                                }

                                Button(
                                    onClick = {
                                        viewModel.activePdfItem = pdf
                                        viewModel.activePdfCurrentPage = 1
                                    }
                                ) {
                                    Text("Open", fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ================= BOOKS STUDY LIBRARY =================
@Composable
fun BooksLibraryScreenView(viewModel: AppViewModel) {
    val books = listOf(
        Pair("Class 12 Physics NCERT Exemplar", "Complete answers & numericals solver."),
        Pair("Class 12 Chemistry Quick Formula Sheet", "High yield core chemistry tables."),
        Pair("Mathematics Coordinate Geometry Handouts", "Formulas by Vinay sir."),
        Pair("Class 10 CBSE Board Past Year Solved", "Solved board exam worksheets.")
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "FREE REFERENCE MATERIAL LIBRARY",
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = AcadOrangeAccent,
            letterSpacing = 1.sp
        )
        Spacer(modifier = Modifier.height(10.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(books) { book ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, Color(0xFFF1F5F9))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Text(text = "📚", fontSize = 32.sp)

                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = book.first, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray)
                            Text(text = book.second, fontSize = 11.sp, color = Color.Gray)
                        }

                        Button(
                            onClick = { /* Simulated download */ }
                        ) {
                            Text("Get PDF", fontSize = 11.sp)
                        }
                    }
                }
            }
        }
    }
}

// ================= TEST SERIES & QUIZ RUNNING HUB =================
@Composable
fun TestSeriesScreenView(
    viewModel: AppViewModel,
    testAttempts: List<TestAttempt>,
    onComplete: (score: Int, title: String) -> Unit
) {
    if (viewModel.isQuizRunning) {
        // ACTIVE QUIZ RUNNING SCREEN
        val currentQ = viewModel.activeQuizQuestions.getOrNull(viewModel.currentQuestionIndex)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Timer: ${viewModel.quizTimerSeconds} sec left",
                    color = Color.Red,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Q: ${viewModel.currentQuestionIndex + 1} of ${viewModel.activeQuizQuestions.size}",
                    color = AcadBluePrimary,
                    fontWeight = FontWeight.ExtraBold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (currentQ != null) {
                Text(
                    text = currentQ.question,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.DarkGray
                )

                Spacer(modifier = Modifier.height(16.dp))

                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    currentQ.options.forEachIndexed { idx, opt ->
                        val isSelected = viewModel.quizSelectedAnswerIndex == idx
                        Surface(
                            color = if (isSelected) Color(0xFFDBEAFE) else Color.White,
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, if (isSelected) AcadBluePrimary else Color.LightGray),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewModel.quizSelectedAnswerIndex = idx }
                        ) {
                            Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                                RadioButton(
                                    selected = isSelected,
                                    onClick = { viewModel.quizSelectedAnswerIndex = idx }
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(text = opt, color = Color.DarkGray, fontSize = 13.sp)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = { viewModel.submitAnswer() },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = AcadOrangeAccent),
                    enabled = viewModel.quizSelectedAnswerIndex != null
                ) {
                    Text("Confirm & Next Question", fontWeight = FontWeight.Bold)
                }
            }
        }
    } else if (viewModel.showQuizReportCard) {
        // QUIZ REPORT CARD
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "📊 EXAMINATION RESULTS", fontWeight = FontWeight.Black, color = AcadBluePrimary, fontSize = 18.sp)
            Text(text = viewModel.activeQuizTitle ?: "Exam Results", color = AcadOrangeAccent, fontSize = 14.sp)

            Spacer(modifier = Modifier.height(24.dp))

            Surface(
                color = Color(0xFFEFF6FF),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(2.dp, AcadBluePrimary),
                modifier = Modifier.size(100.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    val scorePercent = if (viewModel.activeQuizQuestions.isNotEmpty()) {
                        (viewModel.quizCorrectCounter * 100) / viewModel.activeQuizQuestions.size
                    } else 0
                    Text(
                        text = "$scorePercent%",
                        fontWeight = FontWeight.Bold,
                        color = AcadBluePrimary,
                        fontSize = 24.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "Correct Solutions: ${viewModel.quizCorrectCounter} / ${viewModel.activeQuizQuestions.size}",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { viewModel.showQuizReportCard = false }
            ) {
                Text("Dismiss & Go Back")
            }
        }
    } else {
        // Standard tests directory listing
        val demoQuizzes = listOf(
            Triple("All India Physics Mock Paper", "Physics", "3 Questions • 180s Limit"),
            Triple("High Yield Chemistry Organic Chapters", "Chemistry", "2 Questions • 120s Limit"),
            Triple("JEE/CBSE Maths Formulas Mock", "Mathematics", "3 Questions • 180s Limit")
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text(
                text = "TEST SERIES ENGINE",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = AcadOrangeAccent,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(10.dp))

            demoQuizzes.forEach { (title, subject, duration) ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 10.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, Color(0xFFF1F5F9))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(text = title, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray)
                            Text(text = "$subject • $duration", fontSize = 11.sp, color = Color.Gray)
                        }

                        Button(
                            onClick = { viewModel.startQuiz(title, subject) },
                            colors = ButtonDefaults.buttonColors(containerColor = AcadBluePrimary)
                        ) {
                            Text("Solve", fontSize = 11.sp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "PAST ATTEMPTS TRANSCRIPT RECORD",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Gray,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(6.dp))

            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(testAttempts.reversed()) { attempt ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White, RoundedCornerShape(10.dp))
                            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(10.dp))
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = attempt.testTitle, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray)
                            Text(text = "${attempt.subject} • Completed - ${attempt.dateCompleted}", fontSize = 10.sp, color = Color.Gray)
                        }
                        Text(text = "${attempt.score}%", color = AcadOrangeAccent, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// ================= GEMINI-POWERED DOUBTS SOLVER SCREEN =================
@Composable
fun DoubtAIScreenView(viewModel: AppViewModel, doubts: List<Doubt>) {
    val subjects = listOf("Mathematics", "Physics", "Chemistry", "Biology", "SST")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "VINAY SIR'S DOUBT AI ASSISTANT",
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = AcadOrangeAccent,
            letterSpacing = 1.sp
        )
        Text(text = "Instant step-by-step solutions powered by Gemini AI Client.", fontSize = 11.sp, color = Color.Gray)

        Spacer(modifier = Modifier.height(12.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, Color(0xFFF1F5F9))
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                // Dropdown simulated
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    subjects.forEach { sub ->
                        val isPicked = viewModel.doubtSubjectInput == sub
                        Surface(
                            color = if (isPicked) AcadBluePrimary else Color(0xFFF1F5F9),
                            contentColor = if (isPicked) Color.White else Color.DarkGray,
                            shape = RoundedCornerShape(100.dp),
                            modifier = Modifier
                                .clickable { viewModel.doubtSubjectInput = sub }
                                .padding(vertical = 4.dp)
                        ) {
                            Text(
                                text = sub,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = viewModel.doubtTextInput,
                    onValueChange = { viewModel.doubtTextInput = it },
                    placeholder = { Text("Describe or paste your question here... (e.g. Solve limit x->0 sinx/x)") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .testTag("ask_doubt_input_box"),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = { viewModel.submitDoubt() },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = AcadOrangeAccent),
                    enabled = viewModel.doubtTextInput.trim().isNotEmpty() && !viewModel.isSolvingDoubt
                ) {
                    if (viewModel.isSolvingDoubt) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(16.dp))
                    } else {
                        Text("Solve with Gemini AI Bot", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "DOUBT HISTORY LIST",
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Gray,
            letterSpacing = 1.sp
        )

        Spacer(modifier = Modifier.height(6.dp))

        // History logs
        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(doubts.reversed()) { d ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, Color(0xFFF1F5F9))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Surface(
                                color = Color(0xFFEFF6FF),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = d.subject,
                                    fontSize = 10.sp,
                                    color = AcadBluePrimary,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }

                            Text(
                                text = d.status,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (d.status == "PENDING") Color.Gray else Color(0xFF22C55E)
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(text = "Q: ${d.questionText}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray)

                        if (d.answerText != null) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Divider()
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(text = "Vinay Sir's Assistant Reply:", minLines = 1, fontWeight = FontWeight.ExtraBold, fontSize = 11.sp, color = AcadOrangeAccent)
                            Text(text = d.answerText, fontSize = 12.sp, color = Color.Gray)
                        }
                    }
                }
            }
        }
    }
}

// ================= STUDENT PROFILE VIEW SCREEN =================
@Composable
fun StudentProfileScreenView(
    viewModel: AppViewModel,
    student: StudentProfile,
    testAttempts: List<TestAttempt>
) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Surface(
            modifier = Modifier.size(90.dp),
            shape = CircleShape,
            color = Color(0xFFEFF6FF),
            border = BorderStroke(3.dp, AcadBluePrimary)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = student.name.take(2).uppercase(),
                    color = AcadBluePrimary,
                    fontWeight = FontWeight.Black,
                    fontSize = 32.sp
                )
            }
        }

        Text(text = student.name, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color.DarkGray)
        Text(text = "Registration ID: #RR-2026-${student.mobile.takeLast(4)}", fontSize = 11.sp, color = Color.Gray)

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(20.dp),
            border = BorderStroke(1.dp, Color(0xFFF1F5F9))
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(text = "ACADEMICS PROFILE REPORT", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = AcadOrangeAccent)
                Divider()

                listOf(
                    "Class Enrolled" to student.studentClass,
                    "Mobile Contact" to student.mobile,
                    "Parent Name" to student.parentName,
                    "Registered Slogan" to student.coursePurchased,
                    "Date of Birth" to student.dob,
                    "Online Status" to "Active Secured ✅",
                    "Fees Record" to "Fees Paid (Full Session)"
                ).forEach { (lbl, valStr) ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = lbl, fontSize = 12.sp, color = Color.Gray)
                        Text(text = valStr, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray)
                    }
                }
            }
        }

        // Anti Screenshot block settings switch
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, Color(0xFFF1F5F9))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(text = "App Screen ShieldProtection", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Text(text = "Locks app screenshot/video capture to prevent leaks.", fontSize = 11.sp, color = Color.Gray)
                }
                Switch(
                    checked = viewModel.antiScreenshotEnabled,
                    onCheckedChange = {
                        viewModel.antiScreenshotEnabled = it
                        val msg = if (it) "Screen Capture Disabled! Data Leak secured." else "Screen Shield Disabled."
                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                    }
                )
            }
        }
    }
}

// ================= ROLE SWITCHER PANEL =================
@Composable
fun AppRoleSwitcherScreen(viewModel: AppViewModel, onBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(imageVector = Icons.Default.VerifiedUser, contentDescription = null, tint = AcadBluePrimary, modifier = Modifier.size(64.dp))
        Spacer(modifier = Modifier.height(12.dp))
        Text(text = "ROLE ACCESS CENTER", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = AcadBluePrimary)
        Text(text = "Quickly test and experience student, teacher, or administrative views.", fontSize = 12.sp, color = Color.Gray, textAlign = TextAlign.Center)

        Spacer(modifier = Modifier.height(24.dp))

        listOf("STUDENT", "TEACHER", "ADMIN").forEach { role ->
            val isPicked = viewModel.appRoleMode == role
            Button(
                onClick = {
                    viewModel.appRoleMode = role
                    onBack()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isPicked) AcadOrangeAccent else AcadBluePrimary
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(text = "Access as $role Panel", fontWeight = FontWeight.Bold)
            }
        }
    }
}

// ================= ADMIN PORTAL DASHBOARD VIEW =================
@Composable
fun AdminDashboardScreenView(viewModel: AppViewModel, allContents: List<ContentItem>) {
    var titleInput by remember { mutableStateOf("") }
    var detailsInput by remember { mutableStateOf("") }
    var selectedCat by remember { mutableStateOf("ANNOUNCEMENT") } // LIVE, RECORDED, PDF, ANNOUNCEMENT

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(text = "ADMIN COMMAND CENTER", fontWeight = FontWeight.Black, color = AcadBluePrimary, fontSize = 16.sp)
        Text(text = "Manage and broadcast dynamic educational material to student feeds.", fontSize = 12.sp, color = Color.Gray)

        Divider()

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(20.dp),
            border = BorderStroke(1.dp, Color(0xFFF1F5F9))
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(text = "POST NEW CONTENT BATCH", fontWeight = FontWeight.Bold, color = AcadOrangeAccent, fontSize = 13.sp)

                // Category
                Column {
                    Text(text = "Notification Category Filters", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        listOf("LIVE", "RECORDED", "PDF", "ANNOUNCEMENT").forEach { cat ->
                            val isSel = selectedCat == cat
                            Surface(
                                color = if (isSel) AcadOrangeAccent else Color(0xFFF1F5F9),
                                contentColor = if (isSel) Color.White else Color.DarkGray,
                                shape = RoundedCornerShape(100.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { selectedCat = cat }
                            ) {
                                Box(modifier = Modifier.padding(vertical = 8.dp), contentAlignment = Alignment.Center) {
                                    Text(text = cat, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = titleInput,
                    onValueChange = { titleInput = it },
                    label = { Text("Content Title") },
                    placeholder = { Text("E.g: Hydrocarbons part 4 revision notes") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = detailsInput,
                    onValueChange = { detailsInput = it },
                    label = { Text("Broadcasting Details") },
                    placeholder = { Text("E.g: Download and practice CBSE worksheets questions") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Button(
                    onClick = {
                        if (titleInput.trim().isNotEmpty() && detailsInput.trim().isNotEmpty()) {
                            viewModel.adminAddType = selectedCat
                            viewModel.adminAddTitle = titleInput
                            viewModel.adminAddDetails = detailsInput
                            viewModel.adminAddNewContent()
                            titleInput = ""
                            detailsInput = ""
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = AcadBluePrimary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Broadcast & Publish Feed", fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))
        Text(text = "CURRENTLY BROADCASTED FEED", fontWeight = FontWeight.Bold, color = Color.Gray, fontSize = 11.sp)

        allContents.reversed().take(5).forEach { item ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White, RoundedCornerShape(10.dp))
                    .padding(10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = item.title, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Text(text = "Topic: ${item.details}", fontSize = 11.sp, color = Color.Gray)
                }

                Surface(
                    color = Color(0xFFEFF6FF),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = item.category,
                        fontSize = 9.sp,
                        color = AcadBluePrimary,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                    )
                }
            }
        }
    }
}

// ================= TEACHER DASHBOARD SCREEN =================
@Composable
fun TeacherDashboardScreenView(viewModel: AppViewModel, allContents: List<ContentItem>) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(text = "TEACHER INTERFACE BOARD", fontWeight = FontWeight.Black, color = AcadBluePrimary, fontSize = 16.sp)
        Text(text = "Check assignments uploaded by students & reply grading feedback.", fontSize = 12.sp, color = Color.Gray)

        Divider()

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(20.dp),
            border = BorderStroke(1.dp, Color(0xFFF1F5F9))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = "PENDING ASSIGNMENTS SCAN evaluations", fontWeight = FontWeight.Bold, color = AcadOrangeAccent, fontSize = 13.sp)
                Spacer(modifier = Modifier.height(10.dp))

                val submits = listOf(
                    "Anjali Singh" to "Physics: Potential numericals Worksheet 2",
                    "Rohan Sharma" to "Mathematics: Determinants Exam PDF",
                    "Harsh Vardhan" to "Chemistry: Kinetics Board Past solutions"
                )

                submits.forEachIndexed { index, (studentName, assignTitle) ->
                    Column(modifier = Modifier.padding(vertical = 6.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(text = studentName, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                Text(text = assignTitle, fontSize = 11.sp, color = Color.Gray)
                            }

                            Button(
                                onClick = { /* grade action demo */ }
                            ) {
                                Text("Grade A+", fontSize = 10.sp)
                            }
                        }
                        if (index < submits.size - 1) Divider(modifier = Modifier.padding(vertical = 4.dp))
                    }
                }
            }
        }
    }
}

// ================= LIVE LECTURES LIST VIEW =================
@Composable
fun LiveLecturesListView(
    viewModel: AppViewModel,
    allContents: List<ContentItem>,
    onSelectLive: (ContentItem) -> Unit
) {
    val liveItems = allContents.filter { it.category == "LIVE" }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "TODAY'S SCHEDULED LIVE CLASESS",
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = AcadOrangeAccent,
            letterSpacing = 1.sp
        )
        Text(text = "Interact live with teachers and clarify chapter concerns instantly.", fontSize = 11.sp, color = Color.Gray)

        Spacer(modifier = Modifier.height(14.dp))

        if (liveItems.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, Color(0xFFF1F5F9))
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("No Live Sessions Broadcasted Today", fontWeight = FontWeight.Bold, color = Color.DarkGray)
                    Text("Classes resume tomorrow per schedule.", fontSize = 11.sp, color = Color.Gray)
                }
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(liveItems) { item ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, Color(0xFFF1F5F9))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(text = item.title, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray)
                                Text(text = "${item.subject} • By ${item.postedBy}", fontSize = 11.sp, color = Color.Gray)
                            }

                            Button(
                                onClick = { onSelectLive(item) },
                                colors = ButtonDefaults.buttonColors(containerColor = AcadOrangeAccent)
                            ) {
                                Text("Join Now", fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}
