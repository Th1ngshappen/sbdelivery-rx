package ru.skillbranch.sbdelivery.domain

import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import ru.skillbranch.sbdelivery.domain.entity.DishEntity
import ru.skillbranch.sbdelivery.repository.DishesRepositoryContract
import ru.skillbranch.sbdelivery.repository.error.AnotherError
import java.util.*

class SearchUseCaseImpl(private val repository: DishesRepositoryContract) : SearchUseCase {

    override fun getDishes(): Single<List<DishEntity>> = repository.getCachedDishes()


    override fun findDishesByName(searchText: String): Observable<List<DishEntity>> =
        // поиск через DAO
        // do trimming here since the sqllite trim() function does not remove other whitespace characters by default.
        // https://www.sqlitetutorial.net/sqlite-functions/sqlite-trim/
        repository.findDishesByName(searchText.trim())

//      поиск через фильтр
//        repository.getCachedDishes()
//            .toObservable()
//            .map { dishes ->
//                dishes.filter {
//                    it.title.toLowerCase(Locale.ROOT).contains(searchText.trim().toLowerCase(Locale.ROOT))
//                }
//            }
}