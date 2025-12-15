package com.aditsyal.autodroid.presentation.viewmodels

import androidx.lifecycle.ViewModel
import com.aditsyal.autodroid.domain.usecase.CheckPermissionsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class PermissionHandlerViewModel @Inject constructor(
    val checkPermissionsUseCase: CheckPermissionsUseCase
) : ViewModel()
