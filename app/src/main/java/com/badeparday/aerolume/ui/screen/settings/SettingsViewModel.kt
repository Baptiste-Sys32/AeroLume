package com.badeparday.aerolume.ui.screen.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.badeparday.aerolume.data.AppContainer
import com.badeparday.aerolume.domain.model.AppSettings
import com.badeparday.aerolume.domain.model.MapAppearance
import com.badeparday.aerolume.domain.model.MotionIntensity
import com.badeparday.aerolume.domain.model.UnitSystem
import com.badeparday.aerolume.domain.repository.AppSettingsRepository
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class SettingsUiState(
    val settings: AppSettings = AppSettings(),
)

class SettingsViewModel(
    private val settingsRepository: AppSettingsRepository,
) : ViewModel() {
    val uiState: StateFlow<SettingsUiState> =
        settingsRepository.settings
            .map(::SettingsUiState)
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = SettingsUiState(AppSettings()),
            )

    fun setDemoMode(enabled: Boolean) {
        viewModelScope.launch { settingsRepository.setDemoMode(enabled) }
    }

    fun setMotionIntensity(intensity: MotionIntensity) {
        viewModelScope.launch { settingsRepository.setMotionIntensity(intensity) }
    }

    fun setUnitSystem(unitSystem: UnitSystem) {
        viewModelScope.launch { settingsRepository.setUnitSystem(unitSystem) }
    }

    fun setMapAppearance(mapAppearance: MapAppearance) {
        viewModelScope.launch { settingsRepository.setMapAppearance(mapAppearance) }
    }

    companion object {
        fun factory(container: AppContainer) =
            viewModelFactory {
                initializer {
                    SettingsViewModel(settingsRepository = container.settingsRepository)
                }
            }
    }
}
