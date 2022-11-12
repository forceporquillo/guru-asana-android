package dev.forcecodes.guruasana.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class YogaPoseWrapper(
	@Json(name="items")
	val items: List<YogaPoses>? = null
)

@JsonClass(generateAdapter = true)
data class YogaCategoriesItem(

	@Json(name="updated_at")
	val updatedAt: String? = null,

	@Json(name="name")
	val name: String? = null,

	@Json(name="description")
	val description: String? = null,

	@Json(name="parent_yoga_category_id")
	val parentYogaCategoryId: Any? = null,

	@Json(name="created_at")
	val createdAt: String? = null,

	@Json(name="short_name")
	val shortName: String? = null,

	@Json(name="id")
	val id: Int? = null
)

@JsonClass(generateAdapter = true)
data class YogaPoses(

	@Json(name="updated_at")
	val updatedAt: String? = null,

	@Json(name="img_url")
	val imgUrl: String? = null,

	@Json(name="yoga_categories")
	val yogaCategories: List<YogaCategoriesItem>? = null,

	@Json(name="sanskrit_name")
	val sanskritName: String? = null,

	@Json(name="created_at")
	val createdAt: String? = null,

	@Json(name="id")
	val id: Int? = null,

	@Json(name="english_name")
	val englishName: String? = null
)
