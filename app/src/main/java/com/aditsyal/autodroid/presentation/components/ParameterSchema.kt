package com.aditsyal.autodroid.presentation.components

sealed class ParameterType {
    data object TEXT : ParameterType()
    data object NUMBER : ParameterType()
    data object TIME : ParameterType()
    data object TOGGLE : ParameterType()
    data class DROPDOWN(val options: List<String>) : ParameterType()
}

data class ParameterSchema(
    val key: String,
    val label: String,
    val type: ParameterType,
    val defaultValue: Any? = null
)
