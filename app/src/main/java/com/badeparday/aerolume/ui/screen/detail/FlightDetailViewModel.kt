package com.badeparday.aerolume.ui.screen.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.badeparday.aerolume.data.AppContainer
import com.badeparday.aerolume.domain.model.AppSettings
import com.badeparday.aerolume.domain.model.Flight
import com.badeparday.aerolume.domain.repository.AppSettingsRepository
import com.badeparday.aerolume.domain.repository.FlightDataRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class FlightDetailUiState(
    val flight: Flight? = null,
    val settings: AppSettings = AppSettings(),
)

class FlightDetailViewModel(
    private val flightId: String,
    private val flightDataRepository: FlightDataRepository,
    settingsRepository: AppSettingsRepository,
) : ViewModel() {

    val uiState: StateFlow<FlightDetailUiState> =
        combine(flightDataRepository.observeFlight(flightId), settingsRepository.settings) { flight, settings ->
            FlightDetailUiState(
                flight = flight,
                settings = settings,
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = FlightDetailUiState(),
        )

    fun toggleSaved() {
        viewModelScope.launch {
            flightDataRepository.toggleSaved(flightId)
        }
    }

    companion object {
        fun factory(container: AppContainer, flightId: String) =
            viewModelFactory {
                initializer {
                    FlightDetailViewModel(
                        flightId = flightId,
                        flightDataRepository = container.flightDataRepository,
                        settingsRepository = container.settingsRepository,
                    )
                }
            }
    }
}
