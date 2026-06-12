package com.example.ui.viewmodel

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.api.GeminiClient
import com.example.data.database.AppDatabase
import com.example.data.model.ContentItem
import com.example.data.model.Doubt
import com.example.data.model.StudentProfile
import com.example.data.model.TestAttempt
import com.example.data.repository.AppRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class QuizQuestion(
    val id: Int,
    val question: String,
    val options: List<String>,
    val correctAnswerIndex: Int,
    val explanation: String
)

class AppViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: AppRepository

    val studentProfile: StateFlow<StudentProfile?>
    val allDoubts: StateFlow<List<Doubt>>
    val allTestAttempts: StateFlow<List<TestAttempt>>
    val allContentItems: StateFlow<List<ContentItem>>

    // AUTH ACTION STATES
    var mobileInput by mutableStateOf("")
    var dobInput by mutableStateOf("")
    var otpInput by mutableStateOf("")
    var selectedClassInput by mutableStateOf("Class 10")
    var authErrorMsg by mutableStateOf<String?>(null)
    var otpSent by mutableStateOf(false)
    var showOtpError by mutableStateOf(false)

    // ACTIVE APP VIEW STATES
    var selectedCategoryFilter by mutableStateOf("ALL") // ALL, LIVE, RECORDED, PDF, BOOK, ASSIGNMENT, ANNOUNCEMENT
    var selectedSubjectFilter by mutableStateOf("ALL")  // ALL, Mathematics, Physics, Chemistry, Biology, SST, etc.
    var searchBarQuery by mutableStateOf("")

    // LIVE CLASSES STATES
    var currentLiveClassItem by mutableStateOf<ContentItem?>(null)
    val liveChatMessages = mutableStateListOf<Pair<String, String>>()
    var isRaisedHand by mutableStateOf(false)
    var videoQualityState by mutableStateOf("720p") // 360p, 480p, 720p, 1080p
    var isVideoMuted by mutableStateOf(false)

    // ACTIVE QUIZ RUNTIME
    var activeQuizTitle by mutableStateOf<String?>(null)
    var activeQuizSubject by mutableStateOf("")
    var activeQuizQuestions = mutableStateListOf<QuizQuestion>()
    var currentQuestionIndex by mutableStateOf(0)
    var quizSelectedAnswerIndex by mutableStateOf<Int?>(null)
    var quizTimerSeconds by mutableStateOf(180) // 3 mins limit
    var quizCorrectCounter by mutableStateOf(0)
    var showQuizReportCard by mutableStateOf(false)
    var isQuizRunning by mutableStateOf(false)

    // DOUBTS SCREEN STATES
    var doubtSubjectInput by mutableStateOf("Mathematics")
    var doubtTextInput by mutableStateOf("")
    var isSolvingDoubt by mutableStateOf(false)
    var selectedDoubtDetail by mutableStateOf<Doubt?>(null)

    // MOCK PDF VIEWER STATE
    var activePdfItem by mutableStateOf<ContentItem?>(null)
    var activePdfCurrentPage by mutableStateOf(1)
    var bookmarkedNotesIds = mutableStateListOf<Int>()

    // ADMIN/TEACHER EDIT STATES
    var adminAddType by mutableStateOf("RECORDED") // RECORDED, PDF, BOOK, ANNOUNCEMENT
    var adminAddTitle by mutableStateOf("")
    var adminAddSubject by mutableStateOf("Mathematics")
    var adminAddDetails by mutableStateOf("")
    var adminAddSizeOrDuration by mutableStateOf("45 mins")
    var adminAddPremium by mutableStateOf(false)

    // SCREEN SCREENSHOT PROTECTION TOGGLE
    var antiScreenshotEnabled by mutableStateOf(true)

    // LOCAL USER MODE (STUDENT, ADMIN, TEACHER) TO EXPERIENCE ALL SIDES of EDTECH
    var appRoleMode by mutableStateOf("STUDENT") // STUDENT, TEACHER, ADMIN

    init {
        val database = AppDatabase.getDatabase(application)
        repository = AppRepository(database.appDao())

        studentProfile = repository.studentProfile.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

        allDoubts = repository.allDoubts.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        allTestAttempts = repository.allTestAttempts.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        allContentItems = repository.allContentItems.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        // Seed data in background
        viewModelScope.launch {
            repository.checkAndSeedDatabase()
        }
    }

    // --- AUTHENTICATION FLOWS ---
    fun sendOtp() {
        if (mobileInput.length < 10) {
            authErrorMsg = "Please enter a valid 10-digit Mobile Number."
            return
        }
        if (dobInput.isEmpty()) {
            authErrorMsg = "Please select or type your Date of Birth."
            return
        }
        authErrorMsg = null
        otpSent = true
    }

    fun verifyOtpAndLogin() {
        if (otpInput != "1234" && otpInput != "123456") {
            // let 1234 / 123456 pass as mock default
            showOtpError = true
            return
        }
        showOtpError = false
        viewModelScope.launch {
            val studentName = when (selectedClassInput) {
                "Class 9" -> "Rohan Sharma"
                "Class 10" -> "Anjali Singh"
                "Class 11" -> "Harsh Vardhan"
                else -> "Vivek Kumar Kumar"
            }
            val parentName = when (selectedClassInput) {
                "Class 9" -> "Mr. Shrivas Sharma"
                "Class 10" -> "Mr. K.P. Singh"
                else -> "Mr. Rajinder Prasad"
            }
            val mockProfile = StudentProfile(
                name = studentName,
                mobile = mobileInput,
                dob = dobInput,
                studentClass = selectedClassInput,
                parentName = parentName,
                parentMobile = "9876543210",
                profilePhotoPlaceholder = when (selectedClassInput) {
                    "Class 9" -> 1
                    "Class 10" -> 2
                    "Class 11" -> 3
                    else -> 4
                }
            )
            repository.saveProfile(mockProfile)
        }
    }

    fun logout() {
        viewModelScope.launch {
            repository.deleteProfile()
            // Reset state
            mobileInput = ""
            dobInput = ""
            otpInput = ""
            otpSent = false
            authErrorMsg = null
        }
    }

    // --- DOUBT SOLVER INTERACTION ---
    fun submitDoubt() {
        if (doubtTextInput.trim().isEmpty()) return
        val currentQuestionText = doubtTextInput
        val currentSubject = doubtSubjectInput
        doubtTextInput = ""
        isSolvingDoubt = true

        viewModelScope.launch {
            // Save initial pending question
            val pendingDoubt = Doubt(
                subject = currentSubject,
                questionText = currentQuestionText,
                answerText = null,
                status = "PENDING"
            )
            repository.askDoubt(pendingDoubt)

            // Dynamic calling to live AI
            val botAnswer = GeminiClient.solveDoubt(currentSubject, currentQuestionText)

            // Save answered question
            val answeredDoubt = Doubt(
                subject = currentSubject,
                questionText = currentQuestionText,
                answerText = botAnswer,
                status = "ANSWERED"
            )
            repository.askDoubt(answeredDoubt)
            isSolvingDoubt = false
        }
    }

    // --- LAUNCH SIMULATED EXAM / QUIZ ---
    fun startQuiz(testName: String, subject: String) {
        activeQuizTitle = testName
        activeQuizSubject = subject
        activeQuizQuestions.clear()

        // Populate sample high yield interactive quiz questions
        when (subject) {
            "Physics" -> {
                activeQuizQuestions.addAll(
                    listOf(
                        QuizQuestion(
                            id = 1,
                            question = "If vectors A and B are perpendicular, their dot product (A · B) is:",
                            options = listOf("1", "0", "A * B", "-1"),
                            correctAnswerIndex = 1,
                            explanation = "Scalar (dot) product of perpendicular vectors is always zero, since cos(90°) = 0."
                        ),
                        QuizQuestion(
                            id = 2,
                            question = "What is the unit of electric field intensity?",
                            options = listOf("N / C", "J / C", "Volt-meter", "Coulomb / Newton"),
                            correctAnswerIndex = 0,
                            explanation = "Electric Field (E) is Force per unit Charge, hence Newton per Coulomb (N/C)."
                        ),
                        QuizQuestion(
                            id = 3,
                            question = "For a projectile, the angle of launch that yields maximum range is:",
                            options = listOf("30°", "60°", "45°", "90°"),
                            correctAnswerIndex = 2,
                            explanation = "Sine of 2*theta is maximum at theta = 45 degrees, which maximizes Range. Maximum Range = u² / g."
                        )
                    )
                )
            }
            "Chemistry" -> {
                activeQuizQuestions.addAll(
                    listOf(
                        QuizQuestion(
                            id = 1,
                            question = "The unit of rate constant for a first-order chemical reaction is:",
                            options = listOf("mol L⁻¹ s⁻¹", "L mol⁻¹ s⁻¹", "s⁻¹", "mol L s"),
                            correctAnswerIndex = 2,
                            explanation = "For first order reaction, Rate = k[A]. Thus k has unit s⁻¹."
                        ),
                        QuizQuestion(
                            id = 2,
                            question = "Which of the following elements has the highest electronegativity?",
                            options = listOf("Oxygen", "Nitrogen", "Chlorine", "Fluorine"),
                            correctAnswerIndex = 3,
                            explanation = "Fluorine is the most electronegative element on Pauling's Scale (with value 4.0)."
                        )
                    )
                )
            }
            else -> { // Default Mathematics
                activeQuizQuestions.addAll(
                    listOf(
                        QuizQuestion(
                            id = 1,
                            question = "If b² - 4ac > 0 and a perfect square, the roots of the quadratic equation are:",
                            options = listOf("Real, Rational and Unequal", "Real, Irrational and Unequal", "Imaginary", "Real and Equal"),
                            correctAnswerIndex = 0,
                            explanation = "When discriminant is positive and a perfect square, roots are real, rational, and unequal."
                        ),
                        QuizQuestion(
                            id = 2,
                            question = "The sum of roots of equation x² - 5x + 6 = 0 is:",
                            options = listOf("6", "-5", "5", "1"),
                            correctAnswerIndex = 2,
                            explanation = "Sum of roots is equal to -b/a. For x² - 5x + 6 = 0, sum of roots is -(-5)/1 = 5."
                        ),
                        QuizQuestion(
                            id = 3,
                            question = "Value of sin²(30°) + cos²(30°) is equal to:",
                            options = listOf("0", "1", "1/2", "3/2"),
                            correctAnswerIndex = 1,
                            explanation = "According to fundamental trigonometric identity sin²(theta) + cos²(theta) = 1 for any angle."
                        )
                    )
                )
            }
        }

        currentQuestionIndex = 0
        quizSelectedAnswerIndex = null
        quizTimerSeconds = activeQuizQuestions.size * 60
        quizCorrectCounter = 0
        showQuizReportCard = false
        isQuizRunning = true

        startQuizTimer()
    }

    private fun startQuizTimer() {
        viewModelScope.launch {
            while (isQuizRunning && quizTimerSeconds > 0) {
                kotlinx.coroutines.delay(1000)
                quizTimerSeconds--
            }
            if (quizTimerSeconds <= 0 && isQuizRunning) {
                submitQuiz()
            }
        }
    }

    fun submitAnswer() {
        val sel = quizSelectedAnswerIndex ?: return
        val currentQuestion = activeQuizQuestions[currentQuestionIndex]
        if (sel == currentQuestion.correctAnswerIndex) {
            quizCorrectCounter++
        }

        if (currentQuestionIndex + 1 < activeQuizQuestions.size) {
            currentQuestionIndex++
            quizSelectedAnswerIndex = null
        } else {
            submitQuiz()
        }
    }

    private fun submitQuiz() {
        isQuizRunning = false
        showQuizReportCard = true
        val totalQ = activeQuizQuestions.size
        val correct = quizCorrectCounter
        val finalScoreVal = (correct * 100) / totalQ

        viewModelScope.launch {
            repository.recordTestAttempt(
                TestAttempt(
                    testTitle = activeQuizTitle ?: "Quick Quiz",
                    subject = activeQuizSubject,
                    score = finalScoreVal,
                    totalQuestions = totalQ,
                    correctAnswers = correct,
                    timeSpentSeconds = (totalQ * 60) - quizTimerSeconds
                )
            )
        }
    }

    // --- LIVE CLASSES INTERACTIVITY ---
    fun selectLiveClass(item: ContentItem) {
        currentLiveClassItem = item
        liveChatMessages.clear()
        liveChatMessages.addAll(
            listOf(
                "Rohan Sharma" to "Good afternoon Sir! Class start ho gayi.",
                "Anjali Singh" to "Namaste Vinay Sir. Voice and video clear hai.",
                "Deepak Verma" to "Sir quadratic equation splitting term trick fir se samjhayien kripya.",
                "Harsh Vardhan" to "Live chat is working fine.",
                "Teacher (Vinay Sir)" to "Welcome students! Aaj hum board exams ki sabset important topics cover karenge."
            )
        )
        isRaisedHand = false
    }

    fun sendLiveMessage(name: String, msg: String) {
        if (msg.trim().isEmpty()) return
        liveChatMessages.add(name to msg)
    }

    // --- PDF BOOKMARK SYSTEM ---
    fun toggleBookmarkNote(id: Int) {
        if (bookmarkedNotesIds.contains(id)) {
            bookmarkedNotesIds.remove(id)
        } else {
            bookmarkedNotesIds.add(id)
        }
    }

    // --- ADMIN / TEACHER COMMAND PANEL OPERATIONS ---
    fun adminAddNewContent() {
        if (adminAddTitle.trim().isEmpty() || adminAddDetails.trim().isEmpty()) return
        val item = ContentItem(
            category = adminAddType,
            subject = adminAddSubject,
            title = adminAddTitle,
            details = adminAddDetails,
            durationOrSize = adminAddSizeOrDuration,
            resourceLink = if (adminAddType == "RECORDED") "mock_lecture_link" else "https://example.com/notes/added_item.pdf",
            isPremium = adminAddPremium
        )
        viewModelScope.launch {
            repository.postContentItem(item)
            // reset fields
            adminAddTitle = ""
            adminAddDetails = ""
            adminAddPremium = false
        }
    }

    fun teacherGradeAssignment(assignment: ContentItem, grade: String, feedback: String) {
        viewModelScope.launch {
            val updated = assignment.copy(
                solvedStatus = "GRADED",
                teacherGrade = grade,
                teacherFeedback = feedback
            )
            repository.updateContentItem(updated)
        }
    }

    fun simulateAssignmentSubmit(assignment: ContentItem) {
        viewModelScope.launch {
            val updated = assignment.copy(
                solvedStatus = "SUBMITTED"
            )
            repository.updateContentItem(updated)
        }
    }
}
