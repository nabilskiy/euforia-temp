package digital.euforia.app.service.notifications

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalTime

class LocalNotificationDeliveryModeTest {

    @Test
    fun `quiet hours start at 23 and end before 8`() {
        assertFalse(LocalNotificationDeliveryMode.isQuietHours(LocalTime.of(22, 59)))
        assertTrue(LocalNotificationDeliveryMode.isQuietHours(LocalTime.of(23, 0)))
        assertTrue(LocalNotificationDeliveryMode.isQuietHours(LocalTime.of(7, 59)))
        assertFalse(LocalNotificationDeliveryMode.isQuietHours(LocalTime.of(8, 0)))
    }
}
