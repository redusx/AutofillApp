package com.example.autofillapp.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.autofillapp.data.ProfileRepository
import com.example.autofillapp.data.UserProfile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/** UI state for the profile screen. */
data class ProfileUiState(
        val profile: UserProfile = UserProfile(),
        val isLoading: Boolean = true,
        val isSaved: Boolean = false,
        val hasExistingProfile: Boolean = false
)

/** ViewModel managing profile data for the UI layer. */
class ProfileViewModel(private val repository: ProfileRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.getProfile().collect { profile ->
                _uiState.value =
                        _uiState.value.copy(
                                profile = profile ?: UserProfile(),
                                isLoading = false,
                                hasExistingProfile = profile != null
                        )
            }
        }
    }

    fun updateFullName(value: String) {
        _uiState.value =
                _uiState.value.copy(
                        profile = _uiState.value.profile.copy(fullName = value),
                        isSaved = false
                )
    }

    fun updateFirstName(value: String) {
        _uiState.value =
                _uiState.value.copy(
                        profile = _uiState.value.profile.copy(firstName = value),
                        isSaved = false
                )
    }

    fun updateLastName(value: String) {
        _uiState.value =
                _uiState.value.copy(
                        profile = _uiState.value.profile.copy(lastName = value),
                        isSaved = false
                )
    }

    fun updateEmail(value: String) {
        _uiState.value =
                _uiState.value.copy(
                        profile = _uiState.value.profile.copy(email = value),
                        isSaved = false
                )
    }

    fun updatePhone(value: String) {
        _uiState.value =
                _uiState.value.copy(
                        profile = _uiState.value.profile.copy(phone = value),
                        isSaved = false
                )
    }

    fun updateAddress(value: String) {
        _uiState.value =
                _uiState.value.copy(
                        profile = _uiState.value.profile.copy(address = value),
                        isSaved = false
                )
    }

    fun updateCity(value: String) {
        _uiState.value =
                _uiState.value.copy(
                        profile = _uiState.value.profile.copy(city = value),
                        isSaved = false
                )
    }

    fun updatePostalCode(value: String) {
        _uiState.value =
                _uiState.value.copy(
                        profile = _uiState.value.profile.copy(postalCode = value),
                        isSaved = false
                )
    }

    fun updateCountry(value: String) {
        _uiState.value =
                _uiState.value.copy(
                        profile = _uiState.value.profile.copy(country = value),
                        isSaved = false
                )
    }

    fun saveProfile() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            repository.saveProfile(_uiState.value.profile)
            _uiState.value =
                    _uiState.value.copy(
                            isLoading = false,
                            isSaved = true,
                            hasExistingProfile = true
                    )
        }
    }

    fun deleteProfile() {
        viewModelScope.launch {
            repository.deleteProfile()
            _uiState.value = ProfileUiState(isLoading = false)
        }
    }

    /** Factory to create ProfileViewModel with repository dependency. */
    class Factory(private val repository: ProfileRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ProfileViewModel(repository) as T
        }
    }
}
