package com.example.data.repository

import com.example.data.database.AppDao
import com.example.data.model.ContentItem
import com.example.data.model.Doubt
import com.example.data.model.StudentProfile
import com.example.data.model.TestAttempt
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull

class AppRepository(private val appDao: AppDao) {

    val studentProfile: Flow<StudentProfile?> = appDao.getStudentProfile()
    val allDoubts: Flow<List<Doubt>> = appDao.getAllDoubts()
    val allTestAttempts: Flow<List<TestAttempt>> = appDao.getAllTestAttempts()
    val allContentItems: Flow<List<ContentItem>> = appDao.getAllContentItems()

    suspend fun saveProfile(profile: StudentProfile) {
        appDao.insertStudentProfile(profile)
    }

    suspend fun deleteProfile() {
        appDao.clearStudentProfile()
    }

    suspend fun askDoubt(doubt: Doubt) {
        appDao.insertDoubt(doubt)
    }

    suspend fun recordTestAttempt(attempt: TestAttempt) {
        appDao.insertTestAttempt(attempt)
    }

    suspend fun postContentItem(item: ContentItem) {
        appDao.insertContentItem(item)
    }

    suspend fun updateContentItem(item: ContentItem) {
        appDao.updateContentItem(item)
    }

    suspend fun deleteContentItemById(id: Int) {
        appDao.deleteContentItemById(id)
    }

    suspend fun checkAndSeedDatabase() {
        val items = appDao.getAllContentItems().first()
        if (items.isEmpty()) {
            val defaultSeed = listOf(
                // 1. LIVE CLASSES
                ContentItem(
                    category = "LIVE",
                    subject = "Mathematics",
                    title = "Quadratic Equations (द्विघात समीकरण) - L1",
                    details = "Vinay Sir taking Class 10 masterclass on standard equations, splitting midterms Hinglish tips & tricks.",
                    durationOrSize = "Live - 12:30 PM",
                    resourceLink = "yFh9FmBLe-M", // mock video identifier
                    isPremium = false
                ),
                ContentItem(
                    category = "LIVE",
                    subject = "Physics",
                    title = "Kinematics & Projectile Motion L-3 (Class 11)",
                    details = "Full conceptual derivations of Range, Max Height, and Trajectory. Free PDF formula booklet in class notes.",
                    durationOrSize = "Live - 02:00 PM",
                    resourceLink = "6hR7G8e9wPQ",
                    isPremium = false
                ),
                ContentItem(
                    category = "LIVE",
                    subject = "Chemistry",
                    title = "Chemical Kinetics and Rate Laws L-1 (Class 12)",
                    details = "Interactive molecular collisions discussion, determination of reaction orders, half-life formulas.",
                    durationOrSize = "Live - 04:00 PM",
                    resourceLink = "8A9vIe6yQwZ",
                    isPremium = true
                ),

                // 2. RECORDED VIDEOS
                ContentItem(
                    category = "RECORDED",
                    subject = "Mathematics",
                    title = "Trigonometry Basics & Formulas Class 10",
                    details = "Full lecture covering Sin, Cos, Tan ratios, identities, and height-distance shortcuts in Hinglish.",
                    durationOrSize = "45 mins",
                    resourceLink = "6eH6J8_trig",
                    isPremium = false
                ),
                ContentItem(
                    category = "RECORDED",
                    subject = "Physics",
                    title = "Newton's Laws of Motion Class 9",
                    details = "Inertia, Force definition, Action-Reaction vectors with live real-world animations and Sir's premium notes.",
                    durationOrSize = "35 mins",
                    resourceLink = "9iK9L1_nlm",
                    isPremium = false
                ),
                ContentItem(
                    category = "RECORDED",
                    subject = "Chemistry",
                    title = "Organic Chemistry - Nomenclature Class 11",
                    details = "Master IUPAC naming of alkanes, alkenes, alkynes easily. No rote memorization required!",
                    durationOrSize = "52 mins",
                    resourceLink = "4gT7H5_org",
                    isPremium = true
                ),

                // 3. PDF NOTES
                ContentItem(
                    category = "PDF",
                    subject = "Physics",
                    title = "Electrostatics Master Formula Booklet (Class 12)",
                    details = "Contains quick-reference guides for Coulomb's Law, Gauss Theorem, Capacitance, and potential gradients.",
                    durationOrSize = "4.2 MB (18 Pages)",
                    resourceLink = "https://example.com/notes/electrostatics.pdf",
                    isPremium = false
                ),
                ContentItem(
                    category = "PDF",
                    subject = "Mathematics",
                    title = "Real Numbers Concepts & Short Theorems (Class 10)",
                    details = "Handwritten PDF notes by Vinay Sir covering Euclid's Lemma and Rational/Irrational proofs.",
                    durationOrSize = "1.8 MB (8 Pages)",
                    resourceLink = "https://example.com/notes/real_numbers.pdf",
                    isPremium = false
                ),
                ContentItem(
                    category = "PDF",
                    subject = "Biology",
                    title = "Human Digestion & Alimentary Canal Diagrams (Class 11)",
                    details = "High-definition labeled schematic cards. Crucial diagrams highly repeated in Board Exams.",
                    durationOrSize = "3.5 MB (12 Pages)",
                    resourceLink = "https://example.com/notes/digestion.pdf",
                    isPremium = true
                ),

                // 4. BOOKS LIBRARY
                ContentItem(
                    category = "BOOK",
                    subject = "NCERT Books",
                    title = "NCERT Solutions Class 10 - Mathematics",
                    details = "Complete step-by-step textbook exercises solved with bilingual, easy-to-understand student guides.",
                    durationOrSize = "11.2 MB",
                    resourceLink = "https://example.com/books/ncert_math10.pdf"
                ),
                ContentItem(
                    category = "BOOK",
                    subject = "Previous Year Papers",
                    title = "CBSE Class 12 Physics Solved Paper (2020-2024)",
                    details = "Original exam series compilations grouped chapter-by-chapter. Invaluable prep helper.",
                    durationOrSize = "15.6 MB",
                    resourceLink = "https://example.com/books/pyq_phys12.pdf"
                ),
                ContentItem(
                    category = "BOOK",
                    subject = "Question Banks",
                    title = "RR Coaching Board Exam Question Bank (Class 10 Science)",
                    details = "150+ high-yield multiple choice and descriptive problems designed personally by Vinay Sir.",
                    durationOrSize = "8.4 MB",
                    resourceLink = "https://example.com/books/qb_sci10.pdf",
                    isPremium = true
                ),

                // 5. ASSIGNMENTS
                ContentItem(
                    category = "ASSIGNMENT",
                    subject = "Physics",
                    title = "Vector Mathematics and Resolving Components (Class 11)",
                    details = "Solve 10 problems on vector dot product, cross product, and resolution of forces. Show diagram steps.",
                    durationOrSize = "Due in 2 Days",
                    resourceLink = "hw_vector_pcm",
                    solvedStatus = "NOT_SUBMITTED"
                ),
                ContentItem(
                    category = "ASSIGNMENT",
                    subject = "Chemistry",
                    title = "Nomenclature of Alcohols & Ethers Practice Set (Class 12)",
                    details = "Complete homework sheet. Draw structures for 8 specified IUPAC formulas and submit scans.",
                    durationOrSize = "Graded",
                    resourceLink = "hw_org_chem",
                    solvedStatus = "GRADED",
                    teacherGrade = "A+",
                    teacherFeedback = "Shabash! Excellent layout of structural bonds. Keep it up."
                ),

                // 6. ANNOUNCEMENTS
                ContentItem(
                    category = "ANNOUNCEMENT",
                    subject = "Exams",
                    title = "CBSE 2026 Revised Board Exam Pattern Released!",
                    details = "Vinay Sir explains CBSE's latest guidelines: 50% Competency-based multiple choice, and structure-based questions. Video session coming on Sunday at 10 AM.",
                    durationOrSize = "Important",
                    resourceLink = "https://cbse.gov.in"
                ),
                ContentItem(
                    category = "ANNOUNCEMENT",
                    subject = "Holiday",
                    title = "RR Coaching Offline Centre Closed on Ram Navami",
                    details = "All online live classes will continue as scheduled. Offline students can access live streaming lectures on the app.",
                    durationOrSize = "Holiday Notice",
                    resourceLink = "holiday"
                )
            )
            appDao.insertContentItems(defaultSeed)
        }
    }
}
