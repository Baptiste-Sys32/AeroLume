package com.badeparday.aerolume.ui.screen.saved

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

data class SavedFlightsUiState(
    val flights: List<Flight> = emptyList(),
    val settings: AppSettings = AppSettings(),
)

class SavedFlightsViewModel(
    private val flightDataRepository: FlightDataRepository,
    settingsRepository: AppSettingsRepository,
) : ViewModel() {

    val uiState: StateFlow<SavedFlightsUiState> =
        combine(flightDataRepository.observeSavedFlights(), settingsRepository.settings) { flights, settings ->
            SavedFlightsUiState(
                flights = flights,
                settings = settings,
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = SavedFlightsUiState(),
        )

    fun toggleSaved(flightId: String) {
        viewModelScope.launch {
            flightDataRepository.toggleSaved(flightId)
        }
    }

    companion object {
        fun factory(container: AppContainer) =
            viewModelFactory {
                initializer {
                    SavedFlightsViewModel(
                        flightDataRepository = container.flightDataRepository,
                        settingsRepository = container.settingsRepository,
                    )
                }
            }
    }
}
