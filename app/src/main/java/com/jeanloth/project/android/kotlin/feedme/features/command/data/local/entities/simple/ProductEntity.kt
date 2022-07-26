package com.jeanloth.project.android.kotlin.feedme.features.command.data.local.entities.simple

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.jeanloth.project.android.kotlin.feedme.core.database.ProductCategoryConverter
import com.jeanloth.project.android.kotlin.feedme.features.command.domain.models.ProductCategory
import com.jeanloth.project.android.kotlin.feedme.features.command.domain.models.WrapperItemEntity
import com.jeanloth.project.android.kotlin.feedme.features.command.domain.models.product.Product

/**
 * Product entity to describe all products in baskets
 */
@Entity(tableName = "product")
@TypeConverters(ProductCategoryConverter::class)
class ProductEntity(
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0,
    var label: String = "",

    var imagePath : String? = null, // Path to saved images

    var image : String? = null, // Name of referential images in json file

    val unitPrice: Float = 0f,
    val category: ProductCategory = ProductCategory.FRUIT
) : WrapperItemEntity

// Command > (Basket > List<ProductWrapper> > Product) + List<ProductWrapper>

fun ProductEntity.asPojo() = Product(
    productId = id,
    label = label,
    imagePath = imagePath,
    unitPrice = unitPrice,
    category = category
)
