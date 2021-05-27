package com.cerdenia.android.fullcup.ui.viewmodel

import android.graphics.Color
import androidx.lifecycle.*
import app.futured.donut.DonutSection
import com.cerdenia.android.fullcup.data.FullCupRepository
import com.cerdenia.android.fullcup.data.local.FullCupPreferences
import com.cerdenia.android.fullcup.data.model.ActivityLog
import com.cerdenia.android.fullcup.data.model.ColoredCategory
import com.cerdenia.android.fullcup.data.model.DailyLog
import java.text.SimpleDateFormat
import java.util.*

class HomeViewModel : ViewModel() {
    private val repo = FullCupRepository.getInstance()
    private val dateLive = MutableLiveData<String>()

    val categories get() = FullCupPreferences.categories

    // Temporary colors
    private val colors = listOf(Color.RED, Color.YELLOW, Color.BLUE,
        Color.GREEN, Color.CYAN, Color.MAGENTA)

    val coloredCategories = categories.mapIndexed { i, category ->
        ColoredCategory(category, colors[i])
    }

    private val dbDailyLogLive: LiveData<DailyLog?> = Transformations
        .switchMap(dateLive) { date -> repo.getLogByDate(date) }

    val dailyLogLive = MediatorLiveData<DailyLog?>()
    val donutDataLive = MediatorLiveData<List<DonutSection>>()

    init {
        // Verify data before exposing to view. We want stored
        // user-selected categories to match existing logs stored in DB.
        dailyLogLive.addSource(dbDailyLogLive) { source ->
            lateinit var activitiesToDelete: List<ActivityLog>
            var loggedCategories = source?.activities?.map { it.category } ?: emptyList()
            if (loggedCategories !== categories) {
                // First, get rid of logs for categories that a user has deselected.
                activitiesToDelete = source?.activities
                    ?.filter { !categories.contains(it.category) }
                    ?: emptyList()
                source?.activities?.removeAll(activitiesToDelete)
                loggedCategories = source?.activities?.map { it.category } ?: emptyList()
                // Then create new logs for categories that have been newly selected.
                categories.forEach { category ->
                    if (!loggedCategories.contains(category)) {
                        source?.activities?.add(ActivityLog(category = category))
                    }
                }
            }

            dailyLogLive.value = source
            repo.deleteActivityLog(*activitiesToDelete.toTypedArray())
        }

        donutDataLive.addSource(dailyLogLive) { source ->
            val activitiesMarkedDone = source?.activities
                ?.filter { it.isDone }
                ?: emptyList()
            donutDataLive.value = activitiesMarkedDone.mapIndexed { i, activity ->
                DonutSection(name = activity.category,
                    amount = 1f,
                    color = colors[i]
                )
            }
        }
    }

    fun saveDailyLog(log: DailyLog) {
        repo.addOrUpdateDailyLog(log)
    }

    fun getDailyLog() {
        val date = SimpleDateFormat("yyyy-MM-dd").format(Date())
        dateLive.value = date
    }
}