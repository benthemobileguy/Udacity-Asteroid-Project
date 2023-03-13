package com.udacity.asteroidradar.repository

import android.os.Build
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import com.udacity.asteroidradar.Constants
import com.udacity.asteroidradar.PictureOfDay
import com.udacity.asteroidradar.api.NasaApi
import com.udacity.asteroidradar.api.asDatabaseModel
import com.udacity.asteroidradar.api.parseAsteroidsJsonResult
import com.udacity.asteroidradar.database.AsteroidDatabase
import com.udacity.asteroidradar.database.asDomainModel
import com.udacity.asteroidradar.domain.Asteroid
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.*


fun getFormattedDate(days: Int=0): String{
    val calendar = Calendar.getInstance()
    if(days > 0){
        calendar.add(Calendar.DAY_OF_YEAR, days)
    }
    val currentTime = calendar.time
    val dateFormat = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        SimpleDateFormat(Constants.API_QUERY_DATE_FORMAT, Locale.getDefault())
    } else {
        TODO("VERSION.SDK_INT < N")
    }
    return dateFormat.format(currentTime)
}


class AsteroidRepository(private val database: AsteroidDatabase) {

    val todayAsteroids: LiveData<List<Asteroid>> =
        Transformations.map(database.asteroidDao.getTodayAsteroids()){ it.asDomainModel()}
    val weeklyAsteroids: LiveData<List<Asteroid>> =
        Transformations.map(database.asteroidDao.getWeeklyAsteroids()){ it.asDomainModel()}
    val asteroids: LiveData<List<Asteroid>> =
            Transformations.map(database.asteroidDao.getAsteroids()){ it.asDomainModel()}

    val pictureOfDay: LiveData<PictureOfDay> = Transformations.map(
            database.asteroidDao.getPictureOfDay()){ it?.asDomainModel() }

    suspend fun refreshPictureOfDay(){
        withContext(Dispatchers.IO){
            try {

                Timber.d("Request new pictureOfDay...")
                val pictureOfDay = NasaApi.retrofitMoshiService.getPictureOfDay(
                        apiKey = Constants.API_KEY)
                // convert them to array of DatabaseAsteroids and insert all
                database.asteroidDao.insertPictureOfDay(pictureOfDay.asDatabaseModel())
            } catch (e: Exception) {
                Timber.e("Encountered error while refreshing picture of day: $e")
            }
        }
    }

    suspend fun refreshAsteroids() {
        withContext(Dispatchers.IO) {
            try {
                val today = getFormattedDate()
                Timber.d("Request new asteroids w/ startDate $today")
                val stringResponse = NasaApi.retrofitScalarService.getAsteroids(
                        apiKey = Constants.API_KEY, startDate = today , endDate = null)
                Timber.d("The results from start $today are $stringResponse")
                val networkAsteroids = parseAsteroidsJsonResult(JSONObject(stringResponse))
                database.asteroidDao.insertAll(*networkAsteroids.asDatabaseModel())
            } catch (e: Exception) {

                Timber.e("Error while refreshing asteroids: $e")
            }
        }
    }
}



