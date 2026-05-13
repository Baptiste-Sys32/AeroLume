package com.badeparday.aerolume.ui.screen.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.badeparday.aerolume.data.AppContainer
import com.badeparday.aerolume.domain.model.AppSettings
import com.badeparday.aerolume.domain.model.Flight
import com.badeparday.aerolume.domain.repository.AppSettingsRepository
import com.badeparday.aerolume.domain.repository.FlightDataRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

data class SearchUiState(
    val query: String = "",
    val recentQueries: List<String> = emptyList(),
    val results: List<Flight> = emptyList(),
    val settings: AppSettings = AppSettings(),
)

class SearchViewModel(
    private val flightDataRepository: FlightDataRepository,
    settingsRepository: AppSettingsRepository,
) : ViewModel() {
    private val query = MutableStateFlow("")
    private val recentQueries = MutableStateFlow(listOf("AF011", "CDG", "TK1823", "JFK"))

    val uiState: StateFlow<SearchUiState> =
        combine(query, recentQueries, flightDataRepository.flights, settingsRepository.settings) { currentQuery, recents, flights, settings ->
            val needle = currentQuery.trim().lowercase()
            val filtered =
                if (needle.isBlank()) {
                    flights.sortedBy { it.flightNumber }.take(6)
                } else {
                    flights.filter { flight ->
                        listOf(
                            flight.flightNumber,
                            flight.airline,
                            flight.origin.code,
                            flight.destination.code,
                            flight.origin.city,
                            flight.destination.city,
                            "${flight.origin.code} ${flight.destination.code}",
                        ).any { candidate ->
                            candidate.lowercase().contains(needle)
                        }
                    }.sortedBy { it.flightNumber }
                }
            SearchUiState(
                query = currentQuery,
                recentQueries = recents,
                results = filtered,
                settings = settings,
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = SearchUiState(),
        )

    fun onQueryChanged(value: String) {
        query.value = value
    }

    fun useRecentQuery(value: String) {
        query.value = value
        rememberQuery(value)
    }

    fun rememberSelection(flightId: String) {
        val flight = flightDataRepository.flights.value.firstOrNull { it.id == flightId } ?: return
        rememberQuery(flight.flightNumber)
    }

    private fun rememberQuery(value: String) {
        recentQueries.update { current ->
            buildList {
                add(value)
                addAll(current.filterNot { it.equals(value, ignoreCase = true) })
            }.take(5)
        }
    }

    companion object {
        fun factory(container: AppContainer) =
            viewModelFactory {
                initializer {
                    SearchViewModel(
                        flightDataRepository = container.flightDataRepository,
                        settingsRepository = container.settingsRepository,
                    )
                }
            }
    }
}
