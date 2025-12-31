package com.aditsyal.autodroid.domain.usecase.executors

interface ActionExecutor {
    suspend fun execute(config: Map<String, Any>): Result<Unit>
}