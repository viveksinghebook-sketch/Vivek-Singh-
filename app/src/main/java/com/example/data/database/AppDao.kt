package com.example.data.database

import androidx.room.*
import com.example.data.model.ContentItem
import com.example.data.model.Doubt
import com.example.data.model.StudentProfile
import com.example.data.model.TestAttempt
import kotlinx.coroutines.flow.Flow

@Dao
interface AppDao {

    // Student Profile Queries
    @Query("SELECT * FROM student_profile LIMIT 1")
    fun getStudentProfile(): Flow<StudentProfile?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudentProfile(profile: StudentProfile)

    @Query("DELETE FROM student_profile")
    suspend fun clearStudentProfile()


    // Doubts Queries
    @Query("SELECT * FROM student_doubts ORDER BY timestamp DESC")
    fun getAllDoubts(): Flow<List<Doubt>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDoubt(doubt: Doubt)


    // Test Attempts Queries
    @Query("SELECT * FROM test_attempts ORDER BY id DESC")
    fun getAllTestAttempts(): Flow<List<TestAttempt>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTestAttempt(attempt: TestAttempt)


    // General Content Queries (Live lectures, Recorded videos, PDF Books, Announcements, Assignments)
    @Query("SELECT * FROM content_items ORDER BY timestamp DESC")
    fun getAllContentItems(): Flow<List<ContentItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContentItem(item: ContentItem)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContentItems(items: List<ContentItem>)

    @Update
    suspend fun updateContentItem(item: ContentItem)

    @Query("DELETE FROM content_items WHERE id = :id")
    suspend fun deleteContentItemById(id: Int)
}
