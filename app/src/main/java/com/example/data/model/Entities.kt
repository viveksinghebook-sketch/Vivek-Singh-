package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "student_profile")
data class StudentProfile(
    @PrimaryKey val id: String = "student_active",
    val name: String,
    val mobile: String,
    val dob: String,
    val studentClass: String, // Class 9, 10, 11, or 12
    val parentName: String,
    val parentMobile: String,
    val profilePhotoPlaceholder: Int = 1, // avatar selection
    val attendancePercent: Int = 92,
    val coursePurchased: String = "All-In-One Batch (PCM + Hinglish)",
    val registrationDate: String = "12 June 2026",
    val feesPaid: Boolean = true
)

@Entity(tableName = "student_doubts")
data class Doubt(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val subject: String,
    val questionText: String,
    val imagePath: String? = null, // mock path
    val voicePath: String? = null, // mock path
    val answerText: String?, // Reply from Vinay Sir's AI Study Assistant
    val timestamp: Long = System.currentTimeMillis(),
    val status: String = "ANSWERED" // PENDING or ANSWERED
)

@Entity(tableName = "test_attempts")
data class TestAttempt(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val testTitle: String,
    val subject: String,
    val score: Int,
    val totalQuestions: Int,
    val correctAnswers: Int,
    val timeSpentSeconds: Int,
    val dateCompleted: String = "Today"
)

@Entity(tableName = "content_items")
data class ContentItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val category: String, // LIVE, RECORDED, PDF, BOOK, ASSIGNMENT, ANNOUNCEMENT
    val subject: String,  // Mathematics, Physics, Chemistry, Biology, SST, English, etc.
    val title: String,
    val details: String,  // Description, subtasks, etc.
    val durationOrSize: String, // e.g. "45 mins", "4.2 MB", "24 Pages"
    val resourceLink: String, // mock youtube url, document link, etc.
    val postedBy: String = "Vinay Sir",
    val timestamp: Long = System.currentTimeMillis(),
    val isPremium: Boolean = false,
    val solvedStatus: String = "NOT_SUBMITTED", // For assignment submissions tracking: "NOT_SUBMITTED", "SUBMITTED", "GRADED"
    val teacherGrade: String? = null,
    val teacherFeedback: String? = null
)
