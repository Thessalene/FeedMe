package com.jeanloth.project.android.kotlin.feedme.features.command.domain.usecases.basket

import com.jeanloth.project.android.kotlin.feedme.features.command.domain.models.Basket
import com.jeanloth.project.android.kotlin.feedme.features.command.domain.models.WrapperType
import com.jeanloth.project.android.kotlin.feedme.features.command.domain.repository.BasketRepository
import javax.inject.Inject

/**
 * Save basket "type" with products
 */
class SaveBasketUseCase @Inject constructor(
    private val repository: BasketRepository,
    private val saveProductWrapperUseCase: SaveProductWrapperUseCase
) {
    operator fun invoke(basket: Basket) : Boolean {
        val basketId = repository.save(basket)

        val wrappers = basket.wrappers.onEach {
            it.parentId = basketId
            it.wrapperType = WrapperType.BASKET_PRODUCT
        }

        saveProductWrapperUseCase(wrappers)

        return basketId != 0L
    }
}