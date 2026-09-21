package com.shoppinggenius.app.feature.carapp

import com.google.common.truth.Truth.assertThat
import com.shoppinggenius.app.model.Grocery
import com.shoppinggenius.app.model.Product
import org.junit.jupiter.api.Test

class ProductPickerScreenTest {

    @Test
    fun `normalizeGroceryName ignores casing and duplicate whitespace`() {
        val normalizedName = normalizeGroceryName("  Hafer   Milch  ")

        assertThat(normalizedName).isEqualTo("hafer milch")
    }

    @Test
    fun `isDuplicateGroceryName detects existing entry by normalized name`() {
        val groceries = listOf(
            Grocery(productId = "1", name = "Hafer Milch", purchased = false)
        )

        val isDuplicate = isDuplicateGroceryName("  hafer   milch ", groceries)

        assertThat(isDuplicate).isTrue()
    }

    @Test
    fun `availableProductsForCarApp filters products already represented in the list`() {
        val groceries = listOf(
            Grocery(productId = "1", name = "Hafer Milch", purchased = false),
            Grocery(productId = "3", name = "Brot", purchased = false)
        )
        val products = listOf(
            Product(id = "1", name = "Hafer Milch"),
            Product(id = "2", name = "  hafer   milch "),
            Product(id = "3", name = "Brot"),
            Product(id = "4", name = "Äpfel")
        )

        val availableProducts = availableProductsForCarApp(products, groceries)

        assertThat(availableProducts.map(Product::id)).containsExactly("4")
    }

    @Test
    fun `availableProductsForCarApp removes duplicate catalog entries with same name`() {
        val products = listOf(
            Product(id = "1", name = "Tomaten"),
            Product(id = "2", name = " tomaten "),
            Product(id = "3", name = "Gurken")
        )

        val availableProducts = availableProductsForCarApp(products, emptyList())

        assertThat(availableProducts.map(Product::id)).containsExactly("1", "3").inOrder()
    }
}

