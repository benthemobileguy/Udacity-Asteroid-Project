package com.udacity.asteroidradar.main

import android.app.Application
import androidx.lifecycle.*
import com.udacity.asteroidradar.api.AsteroidApiFilter
import com.udacity.asteroidradar.database.getDatabase
import com.udacity.asteroidradar.domain.Asteroid
import com.udacity.asteroidradar.repository.AsteroidRepository
import kotlinx.coroutines.launch
import timber.log.Timber

import java.lang.Exception

enum class AsteroidApiStatus { LOADING, ERROR, DONE } //set enums for state


class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val database = getDatabase(application)
    private val asteroidsRepository = AsteroidRepository(database)

    private val _status = MutableLiveData<AsteroidApiStatus>()
    val status: LiveData<AsteroidApiStatus> get() = _status

    init {
        viewModelScope.launch {
            try {
                _status.value = AsteroidApiStatus.LOADING
                asteroidsRepository.refreshAsteroids()
            }catch(e: Exception){
                Timber.e("Error loading asteroids $e")
                _status.value = AsteroidApiStatus.ERROR
            }finally {
                _status.value = AsteroidApiStatus.DONE
            }
        }
        viewModelScope.launch {
            asteroidsRepository.refreshPictureOfDay()
        }

    }

    private val _filterSelected = MutableLiveData<AsteroidApiFilter>(AsteroidApiFilter.SHOW_SAVED)
    val filterSelected: LiveData<AsteroidApiFilter>
        get() = _filterSelected


    val asteroids = Transformations.switchMap(_filterSelected) {
        when (it!!) {
            AsteroidApiFilter.SHOW_TODAY -> asteroidsRepository.todayAsteroids
            AsteroidApiFilter.SHOW_WEEK -> asteroidsRepository.weeklyAsteroids
            else -> asteroidsRepository.asteroids
        }
    }

    val pictureOfDay = asteroidsRepository.pictureOfDay

    private val _navigateToDetail = MutableLiveData<Asteroid>()

    val navigateToDetail: LiveData<Asteroid>
        get() = _navigateToDetail

    fun navigationFinished() {
        _navigateToDetail.value = null
    }

    fun onAsteroidClicked(asteroid: Asteroid) {
        _navigateToDetail.value = asteroid
    }

    fun updateFilter(filter: AsteroidApiFilter) {
        _filterSelected.value = filter
    }

    class Factory(val app: Application) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
                return MainViewModel(app) as T
            }
            throw IllegalArgumentException("Unable to build viewmodel")
        }
    }

}
