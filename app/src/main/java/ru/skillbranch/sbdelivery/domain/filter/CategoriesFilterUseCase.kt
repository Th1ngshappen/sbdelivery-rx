package ru.skillbranch.sbdelivery.domain.filter

import io.reactivex.rxjava3.core.Single
import ru.skillbranch.sbdelivery.domain.entity.DishEntity
import ru.skillbranch.sbdelivery.repository.DishesRepositoryContract
import ru.skillbranch.sbdelivery.repository.error.EmptyDishesError

class CategoriesFilterUseCase(private val repository: DishesRepositoryContract) : CategoriesFilter {
    override fun categoryFilterDishes(categoryId: String): Single<List<DishEntity>> {
        return if (categoryId.isEmpty()) repository.getCachedDishes()
        else repository.getCachedDishes()
            .map { dishes ->
                dishes.filter {
                    it.categoryId == categoryId
                }
            }
            .flatMap { if (it.isEmpty()) Single.error(EmptyDishesError("Список пуст")) else Single.just(it)}
    }
}