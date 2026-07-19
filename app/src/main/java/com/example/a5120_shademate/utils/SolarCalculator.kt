package com.example.a5120_shademate.utils

import java.util.*
import kotlin.math.*

/**
 * Simplified Solar Position Calculator based on NOAA algorithms.
 */
object SolarCalculator {

    data class SunPosition(val azimuth: Double, val altitude: Double)

    fun getSunPosition(lat: Double, lon: Double, date: Date): SunPosition {
        val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
        calendar.time = date

        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH) + 1
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)
        val second = calendar.get(Calendar.SECOND)

        // Calculate Julian Day
        val jd = getJulianDay(year, month, day, hour, minute, second)
        val t = (jd - 2451545.0) / 36525.0

        // Geometric Mean Long Sun
        var l0 = 280.46646 + t * (36000.76983 + t * 0.0003032)
        while (l0 > 360) l0 -= 360
        while (l0 < 0) l0 += 360

        // Geometric Mean Anom Sun
        val m = 357.52911 + t * (35999.05029 - 0.0001537 * t)
        val e = 0.016708634 - t * (0.000042037 + 0.0000001267 * t)

        // Sun Eq of Ctr
        val sunEqCtr = sin(toRadians(m)) * (1.914602 - t * (0.004817 + 0.000014 * t)) +
                sin(toRadians(2 * m)) * (0.019993 - 0.000101 * t) +
                sin(toRadians(3 * m)) * 0.000289

        val sunTrueLong = l0 + sunEqCtr
        val sunAppLong = sunTrueLong - 0.00569 - 0.00478 * sin(toRadians(125.04 - 1934.136 * t))

        val meanObliqEcliptic = 23 + (26 + (21.448 - t * (46.815 + t * (0.00059 - t * 0.001813))) / 60) / 60
        val obliqCorr = meanObliqEcliptic + 0.00256 * cos(toRadians(125.04 - 1934.136 * t))

        val sunDeclin = toDegrees(asin(sin(toRadians(obliqCorr)) * sin(toRadians(sunAppLong))))

        val varY = tan(toRadians(obliqCorr / 2.0)) * tan(toRadians(obliqCorr / 2.0))
        val eqTime = 4.0 * toDegrees(varY * sin(2.0 * toRadians(l0)) - 2.0 * e * sin(toRadians(m)) +
                4.0 * e * varY * sin(toRadians(m)) * cos(2.0 * toRadians(l0)) -
                0.5 * varY * varY * sin(4.0 * toRadians(l0)) - 1.25 * e * e * sin(2.0 * toRadians(m)))

        val solarNoonUtc = (720 - 4.0 * lon - eqTime) / 1440.0
        val solarTimeFix = eqTime + 4.0 * lon

        var trueSolarTime = (hour * 60.0 + minute + second / 60.0 + solarTimeFix) % 1440.0
        if (trueSolarTime < 0) trueSolarTime += 1440

        var hourAngle = trueSolarTime / 4.0 - 180.0
        if (hourAngle < -180) hourAngle += 360

        val cosZenith = sin(toRadians(lat)) * sin(toRadians(sunDeclin)) +
                cos(toRadians(lat)) * cos(toRadians(sunDeclin)) * cos(toRadians(hourAngle))

        val zenith = toDegrees(acos(max(-1.0, min(1.0, cosZenith))))
        val altitude = 90.0 - zenith

        val azRaw = toDegrees(acos(((sin(toRadians(lat)) * cosZenith) - sin(toRadians(sunDeclin))) /
                (cos(toRadians(lat)) * sin(toRadians(zenith)))))

        val azimuth = if (hourAngle > 0) (azRaw + 180) % 360 else (540 - azRaw) % 360

        return SunPosition(azimuth, altitude)
    }

    private fun getJulianDay(year: Int, month: Int, day: Int, h: Int, m: Int, s: Int): Double {
        var y = year
        var mm = month
        if (mm <= 2) {
            y -= 1
            mm += 12
        }
        val a = floor(y / 100.0)
        val b = 2 - a + floor(a / 4.0)
        return floor(365.25 * (y + 4716)) + floor(30.6001 * (mm + 1)) + day + b - 1524.5 + (h + m / 60.0 + s / 3600.0) / 24.0
    }

    private fun toRadians(deg: Double) = deg * PI / 180.0
    private fun toDegrees(rad: Double) = rad * 180.0 / PI
}
