package com.example.ca3_roomdatabase

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.coroutines.launch

@Entity(tableName = "diary_entries")
data class DiaryEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val title: String,
    val content: String
)

@Dao
interface DiaryDao {

    @Insert
    suspend fun insert(entry: DiaryEntry)

    @Query("SELECT * FROM diary_entries")
    suspend fun getAll(): List<DiaryEntry>

    @Query("SELECT COUNT(*) FROM diary_entries")
    suspend fun getCount(): Int
}

@Database(entities = [DiaryEntry::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun diaryDao(): DiaryDao
}

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val db = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "diary_db"
        ).build()

        setContent {
            DiaryScreen(db)
        }
    }
}

@Composable
fun DiaryScreen(db: AppDatabase) {

    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    var entries by remember { mutableStateOf(listOf<DiaryEntry>()) }

    val dao = db.diaryDao()
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        entries = dao.getAll()
    }

    Column {

        Text("Personal Diary")

        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Title") }
        )

        OutlinedTextField(
            value = content,
            onValueChange = { content = it },
            label = { Text("Content") }
        )

        Button(onClick = {
            scope.launch {
                if (dao.getCount() < 3) {

                    dao.insert(
                        DiaryEntry(
                            title = title,
                            content = content
                        )
                    )

                    entries = dao.getAll()
                    title = ""
                    content = ""
                    message = "Entry Saved"

                } else {
                    message = "Limit Reached! Only 3 Entries Allowed"
                }
            }
        }) {
            Text("Save Entry")
        }

        Text(message)

        Text("Saved Entries: ${entries.size}")

        LazyColumn {
            items(entries) {
                Text("Title: ${it.title}")
                Text("Content: ${it.content}")
            }
        }
    }
}
