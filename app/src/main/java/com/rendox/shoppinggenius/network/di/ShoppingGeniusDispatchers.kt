package com.rendox.shoppinggenius.network.di

import javax.inject.Qualifier

@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class Dispatcher(val dispatcher: ShoppingGeniusDispatchers)

enum class ShoppingGeniusDispatchers {
    Default,
    IO
}
