package com.jeanloth.project.android.kotlin.feedme.features.command.domain.models.product

import com.jeanloth.project.android.kotlin.feedme.features.command.domain.models.ProductCategory
import com.jeanloth.project.android.kotlin.feedme.features.command.domain.models.WrapperItem

data class Product(
    val productId: Long = 0,
    val label:String,
    val image: String? = null,
    val imagePath: String? = null,
    var imageId: Int? = null,
    val category: ProductCategory = ProductCategory.FRUIT,
    override val unitPrice: Float = 0f
) : WrapperItem {

    override val id: Long
        get() = productId

    override fun toString(): String {
        return "Product : $label]" //"Product : [id: $productId, label: $label, price : $unitPrice, image : $image, imageId: $imageId]"
    }
}