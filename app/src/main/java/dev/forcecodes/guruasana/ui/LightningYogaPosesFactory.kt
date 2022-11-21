package dev.forcecodes.guruasana.ui

import android.content.Context
import com.squareup.moshi.Moshi
import dev.forcecodes.guruasana.model.YogaPoseWrapper
import dev.forcecodes.guruasana.model.YogaPoses

class LightningYogaPosesFactory private constructor() {

    companion object {
        private val yogaItems = mutableListOf<YogaPoses>()
        private const val FILENAME = "lightning_yoga/yoga_api.json"

        @JvmStatic
        fun singleton(context: Context): List<YogaPoses> {
            if (yogaItems.isEmpty()) {
                val string = context
                    .assets
                    .open(FILENAME)
                    .bufferedReader()
                    .use { it.readText() }

                val items = Moshi.Builder()
                    .build()
                    .adapter(YogaPoseWrapper::class.java)
                    .fromJson(string)

                yogaItems.addAll(items?.items ?: emptyList())
            }

            return yogaItems
        }

    }
}