package com.example

import com.example.data.model.AdminSettings
import com.example.data.model.Product
import com.example.ui.locale.AppLanguage
import com.example.ui.locale.AppStrings
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ShopAppUnitTest {

    @Test
    fun testGujaratiLocalizationTranslations() {
        val en = AppStrings(AppLanguage.EN)
        val gu = AppStrings(AppLanguage.GU)

        assertTrue(gu.isGujarati)
        assertFalse(en.isGujarati)

        assertEquals("હોમ", gu.navHome)
        assertEquals("દુકાન", gu.navShop)
        assertEquals("રીપેર સેવા", gu.navRepair)
        assertEquals("મારા ઓર્ડર", gu.navOrders)
        assertEquals("ઉમેરો", gu.addBtn)
        assertEquals("દુકાન માલિક લોગિન", gu.ownerLogin)
        assertEquals("હમણાં પ્રવેશ કરો (પાસવર્ડ પછીથી સેટ કરો)", gu.setPasswordLaterBtn)
    }

    @Test
    fun testAdminSettingsPasswordValidation() {
        // Initially no password set
        val defaultSettings = AdminSettings(id = 1, adminPassword = "", isPasswordSet = false)
        assertFalse(defaultSettings.isPasswordSet)
        assertTrue(defaultSettings.adminPassword.isBlank())

        // Owner sets password
        val configuredSettings = defaultSettings.copy(adminPassword = "MySecureStorePass2026", isPasswordSet = true)
        assertTrue(configuredSettings.isPasswordSet)
        assertEquals("MySecureStorePass2026", configuredSettings.adminPassword)

        // Matching
        val enteredPass = "MySecureStorePass2026"
        assertTrue(enteredPass == configuredSettings.adminPassword)
    }

    @Test
    fun testProductModelDataDriven() {
        val product = Product(
            id = 101,
            name = "Smartphone Display Screen",
            category = "Mobile",
            price = 1499.0,
            mrp = 2499.0,
            stock = 15,
            description = "AMOLED Original Screen",
            iconCategory = "mobile"
        )

        assertEquals(101L, product.id)
        assertEquals(1499.0, product.price, 0.001)
        assertTrue(product.isAvailable)
        assertEquals(15, product.stock)

        // Price update test without changing code
        val updated = product.copy(price = 1299.0, stock = 8)
        assertEquals(1299.0, updated.price, 0.001)
        assertEquals(8, updated.stock)
    }
}
