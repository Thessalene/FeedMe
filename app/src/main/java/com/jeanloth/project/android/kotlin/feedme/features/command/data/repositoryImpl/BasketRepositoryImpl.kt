package com.jeanloth.project.android.kotlin.feedme.features.command.data.repositoryImpl

import com.jeanloth.project.android.kotlin.feedme.features.command.data.local.dao.BasketDao
import com.jeanloth.project.android.kotlin.feedme.features.command.data.local.entities.simple.BasketEntity
import com.jeanloth.project.android.kotlin.feedme.features.command.domain.models.Basket
import com.jeanloth.project.android.kotlin.feedme.features.command.domain.models.Wrapper
import com.jeanloth.project.android.kotlin.feedme.features.command.domain.models.product.Product
import com.jeanloth.project.android.kotlin.feedme.features.command.domain.repository.BasketRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class BasketRepositoryImpl @Inject constructor(
    private val dao : BasketDao
) : BasketRepository {

    override fun save(basket: Basket) : Long {
        val entity = BasketEntity(
            label = basket.label,
            price = basket.price,
        )
        return dao.insert(entity)
    }

    override fun observeBaskets(commandId: Long): Flow<List<Basket>> {
        return observeBaskets().map { it.filter { it.wrappers.any { it.parentId == commandId } } }
    }

    override fun observeBaskets(): Flow<List<Basket>> {
        return dao.observeBasketsWithWrappers().map { baskets ->
            baskets.map {
                Basket(
                    basketId = it.basketEntity.id,
                    label = it.basketEntity.label,
                    price = it.basketEntity.price,
                    wrappers = it.wrappers.map {
                        Wrapper(
                            id = it.wrapper.id,
                            item = Product(
                                label = it.product.label,
                                unitPrice = it.product.unitPrice
                            ),
                            realQuantity = it.wrapper.realQuantity,
                            quantity = it.wrapper.quantity
                        )
                    }
                )
            }
        }
    }

    override fun remove(basket: Basket) {
        TODO("Not yet implemented")
    }


}
