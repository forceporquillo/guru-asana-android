package dev.forcecodes.guruasana.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class PoseJournals(

	@Json(name="beginner")
	val beginner: List<PoseContent>? = null,

	@Json(name="advanced")
	val advanced: List<PoseContent>? = null,

	@Json(name="intermediate")
	val intermediate: List<PoseContent>? = null
)

@JsonClass(generateAdapter = true)
data class PoseContent(

	@Json(name="youtube_link")
	val youtubeLink: String? = null,

	@Json(name="pose")
	val pose: String? = null,

	@Json(name="description")
	val description: String? = null,

	@Json(name="instructions")
	val instructions: String? = null,

	@Json(name="accuracy_rate")
    val accuracyRate: String? = null
)