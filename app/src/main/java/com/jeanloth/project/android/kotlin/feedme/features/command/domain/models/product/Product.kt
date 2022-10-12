package com.jeanloth.project.android.kotlin.feedme.features.command.domain.models.product

import com.jeanloth.project.android.kotlin.feedme.R
import com.jeanloth.project.android.kotlin.feedme.features.command.domain.models.ProductCategory
import com.jeanloth.project.android.kotlin.feedme.features.command.domain.models.WrapperItem

data class Product(
    val id: Long = 0,
    val label:String,
    val image: String? = null,
    var imageId: Int = R.drawable.delicious_banana,
    val category: ProductCategory = ProductCategory.FRUIT,
    override val unitPrice: Float = 0f
) : WrapperItem {

    override fun toString(): String {
        return "Product : [id: $id, label: $label, price : $unitPrice]"
    }
}