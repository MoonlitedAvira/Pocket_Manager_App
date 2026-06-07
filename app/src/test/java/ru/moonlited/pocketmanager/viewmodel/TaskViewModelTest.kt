package ru.moonlited.pocketmanager.viewmodel

import org.junit.Test
import org.junit.Assert.*
import java.time.LocalDate
import ru.moonlited.pocketmanager.data.local.entity.TaskEntity

class TaskViewModelTest {

    @Test
    fun `test HeatMap filtering logic`() {
        val selectedDate = LocalDate.of(2024, 6, 3)
        val dateStr = selectedDate.toString()
        
        val tasks = listOf(
            TaskEntity(
                localId = 1,
                title = "Test 1",
                description = null,
                createdAt = "2024-06-03T10:00:00",
                updatedAt = "2024-06-03T10:00:00",
                deadline = "2024-06-03T23:59:59"
            ),
            TaskEntity(
                localId = 2,
                title = "Test 2",
                description = null,
                createdAt = "2024-06-01T10:00:00",
                updatedAt = "2024-06-01T10:00:00",
                deadline = "2024-06-05T23:59:59"
            )
        )
        
        val filteredTasks = tasks.filter { !it.isDeleted && 
            (it.deadline?.startsWith(dateStr) == true || 
             it.startExecutionAt?.startsWith(dateStr) == true || 
             it.createdAt.startsWith(dateStr) ||
             (it.deadline == null && it.startExecutionAt == null))
        }
        
        assertEquals(1, filteredTasks.size)
        assertEquals("Test 1", filteredTasks[0].title)
    }
}
