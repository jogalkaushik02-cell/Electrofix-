package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.db.InitialData
import com.example.data.model.Order
import com.example.data.model.RepairRequest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `read string from context matches app_name`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("ElectroFix", appName)
    }

    @Test
    fun `verify initial catalog products count and data integrity`() {
        val products = InitialData.getInitialProducts()
        assertTrue("Product count should be at least 20 items", products.size >= 20)
        
        products.forEach { product ->
            assertTrue("Product name should not be blank", product.name.isNotBlank())
            assertTrue("Product price should be greater than zero", product.price > 0.0)
            assertTrue("Product stock should be non-negative", product.stock >= 0)
        }
    }

    @Test
    fun `verify order statuses sequence`() {
        assertEquals(5, Order.ALL_STATUSES.size)
        assertEquals(Order.STATUS_PLACED, Order.ALL_STATUSES[0])
        assertEquals(Order.STATUS_CONFIRMED, Order.ALL_STATUSES[1])
        assertEquals(Order.STATUS_PACKED, Order.ALL_STATUSES[2])
        assertEquals(Order.STATUS_SHIPPED, Order.ALL_STATUSES[3])
        assertEquals(Order.STATUS_DELIVERED, Order.ALL_STATUSES[4])
    }

    @Test
    fun `verify repair request statuses sequence`() {
        assertEquals(5, RepairRequest.ALL_STATUSES.size)
        assertEquals(RepairRequest.STATUS_RECEIVED, RepairRequest.ALL_STATUSES[0])
        assertEquals(RepairRequest.STATUS_CONTACTED, RepairRequest.ALL_STATUSES[1])
        assertEquals(RepairRequest.STATUS_REPAIRING, RepairRequest.ALL_STATUSES[2])
        assertEquals(RepairRequest.STATUS_READY, RepairRequest.ALL_STATUSES[3])
        assertEquals(RepairRequest.STATUS_COMPLETED, RepairRequest.ALL_STATUSES[4])
    }

    @Test
    fun `verify AppStrings translation in Gujarati and English`() {
        val enStrings = com.example.ui.locale.AppStrings(com.example.ui.locale.AppLanguage.EN)
        val guStrings = com.example.ui.locale.AppStrings(com.example.ui.locale.AppLanguage.GU)

        assertEquals("ElectroFix Store", enStrings.appTitle)
        assertEquals("ઇલેક્ટ્રોફિક્સ સ્ટોર", guStrings.appTitle)
        assertEquals("Home", enStrings.navHome)
        assertEquals("હોમ", guStrings.navHome)
        assertEquals("Shop", enStrings.navShop)
        assertEquals("દુકાન", guStrings.navShop)
        assertEquals("Repair", enStrings.navRepair)
        assertEquals("રીપેર સેવા", guStrings.navRepair)
        assertEquals("Orders", enStrings.navOrders)
        assertEquals("મારા ઓર્ડર", guStrings.navOrders)

        // Status translations
        assertEquals("ઓર્ડર મુકાયો", guStrings.translateOrderStatus(Order.STATUS_PLACED))
        assertEquals("ડિલિવરી થઈ", guStrings.translateOrderStatus(Order.STATUS_DELIVERED))
        assertEquals("વિનંતી મળી", guStrings.translateRepairStatus(RepairRequest.STATUS_RECEIVED))
        assertEquals("પૂર્ણ થયું", guStrings.translateRepairStatus(RepairRequest.STATUS_COMPLETED))
    }
}
