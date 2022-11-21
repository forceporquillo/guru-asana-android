package dev.forcecodes.guruasana.utils.extensions

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

fun <T> List<T>.toFlow(): Flow<List<T>> = flowOf(this)