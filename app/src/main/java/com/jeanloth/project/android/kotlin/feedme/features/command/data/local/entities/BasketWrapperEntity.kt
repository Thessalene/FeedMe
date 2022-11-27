package com.jeanloth.project.android.kotlin.feedme.features.command.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.jeanloth.project.android.kotlin.feedme.core.database.StatusConverter
import com.jeanloth.project.android.kotlin.feedme.features.command.domain.models.Basket
import com.jeanloth.project.android.kotlin.feedme.features.command.domain.models.Status
import com.jeanloth.project.android.kotlin.feedme.features.command.domain.models.Wrapper

@Entity(tableName = "basket_wrapper")
@TypeConverters(StatusConverter::class)
class BasketWrapperEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val basketId: Long = 0L,
    val commandId: Long = 0L,

    var realQuantity : Int,
    var quantity : Int,
    val status : Status = Status.TO_DO
)


fun Wrapper<Basket>.asEntity() : BasketWrapperEntity {
    return BasketWrapperEntity(
        id = this.id,
        basketId = this.item.id,
        commandId = this.parentId,
        realQuantity = this.realQuantity,
        quantity = this.quantity,
        status = this.status
    )
}