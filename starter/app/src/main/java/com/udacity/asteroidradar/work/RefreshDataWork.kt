package com.udacity.asteroidradar.work

import android.content.Context
import com.udacity.asteroidradar.repository.AsteroidRepository
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.udacity.asteroidradar.database.getDatabase
import retrofit2.HttpException

/**
 * WorkManager class
 * To implement this, we override the Application and also update it in the manifest
 */
class RefreshDataWorker(appContext: Context, params: WorkerParameters):
        CoroutineWorker(appContext, params) {

    companion object {
        const val WORK_NAME = "RefreshDataWorker"
    }

    override suspend fun doWork(): Result {
        val database = getDatabase(applicationContext)
        val repository = AsteroidRepository(database)
        return try {
            repository.clearPreviousPicOfDay()
            repository.clearPreviousAsteroids()
            repository.refreshAsteroids()
            repository.refreshPictureOfDay()
            Result.success()
        } catch (e: HttpException) {
            Result.retry()
        }
    }
}