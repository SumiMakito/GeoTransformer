package com.example.geots

// some silly helper class found by Google
object Transformers {
    private val pi = 3.1415926535897932384626
    private val x_pi = 3.14159265358979324 * 3000.0 / 180.0
    private val a = 6378245.0
    private val ee = 0.00669342162296594323

    private fun transformLat(x: Double, y: Double): Double {
        var ret = (-100.0 + 2.0 * x + 3.0 * y + 0.2 * y * y + 0.1 * x * y
                + 0.2 * Math.sqrt(Math.abs(x)))
        ret += (20.0 * Math.sin(6.0 * x * pi) + 20.0 * Math.sin(2.0 * x * pi)) * 2.0 / 3.0
        ret += (20.0 * Math.sin(y * pi) + 40.0 * Math.sin(y / 3.0 * pi)) * 2.0 / 3.0
        ret += (160.0 * Math.sin(y / 12.0 * pi) + 320 * Math.sin(y * pi / 30.0)) * 2.0 / 3.0
        return ret
    }

    private fun transformLon(x: Double, y: Double): Double {
        var ret = 300.0 + x + 2.0 * y + 0.1 * x * x + 0.1 * x * y + 0.1 * Math.sqrt(Math.abs(x))
        ret += (20.0 * Math.sin(6.0 * x * pi) + 20.0 * Math.sin(2.0 * x * pi)) * 2.0 / 3.0
        ret += (20.0 * Math.sin(x * pi) + 40.0 * Math.sin(x / 3.0 * pi)) * 2.0 / 3.0
        ret += (150.0 * Math.sin(x / 12.0 * pi) + 300.0 * Math.sin(x / 30.0 * pi)) * 2.0 / 3.0
        return ret
    }

    private fun outOfChina(lat: Double, lon: Double): Boolean {
        if (lon < 72.004 || lon > 137.8347)
            return true
        return if (lat < 0.8293 || lat > 55.8271) true else false
    }

    fun wgs84toGcj02(lat: Double, lon: Double): DoubleArray {
        if (outOfChina(lat, lon)) {
            return doubleArrayOf(lat, lon)
        }
        var dLat = transformLat(lon - 105.0, lat - 35.0)
        var dLon = transformLon(lon - 105.0, lat - 35.0)
        val radLat = lat / 180.0 * pi
        var magic = Math.sin(radLat)
        magic = 1 - ee * magic * magic
        val sqrtMagic = Math.sqrt(magic)
        dLat = dLat * 180.0 / (a * (1 - ee) / (magic * sqrtMagic) * pi)
        dLon = dLon * 180.0 / (a / sqrtMagic * Math.cos(radLat) * pi)
        val mgLat = lat + dLat
        val mgLon = lon + dLon
        return doubleArrayOf(mgLat, mgLon)
    }

    fun gcj02toWgs84(lat: Double, lon: Double): DoubleArray {
        val gps = wgs84toGcj02(lat, lon)
        val longitude = lon * 2 - gps[1]
        val latitude = lat * 2 - gps[0]
        return doubleArrayOf(latitude, longitude)
    }

    fun gcj02toBd09(lat: Double, lon: Double): DoubleArray {
        val z = Math.sqrt(lon * lon + lat * lat) + 0.00002 * Math.sin(lat * x_pi)
        val theta = Math.atan2(lat, lon) + 0.000003 * Math.cos(lon * x_pi)
        val tempLon = z * Math.cos(theta) + 0.0065
        val tempLat = z * Math.sin(theta) + 0.006
        return doubleArrayOf(tempLat, tempLon)
    }

    fun bd09toGcj02(lat: Double, lon: Double): DoubleArray {
        val x = lon - 0.0065
        val y = lat - 0.006
        val z = Math.sqrt(x * x + y * y) - 0.00002 * Math.sin(y * x_pi)
        val theta = Math.atan2(y, x) - 0.000003 * Math.cos(x * x_pi)
        val tempLon = z * Math.cos(theta)
        val tempLat = z * Math.sin(theta)
        return doubleArrayOf(tempLat, tempLon)
    }

    fun wgs84toBd09(lat: Double, lon: Double): DoubleArray {
        val gcj02 = wgs84toGcj02(lat, lon)
        return gcj02toBd09(gcj02[0], gcj02[1])
    }

    fun bd09toWgs84(lat: Double, lon: Double): DoubleArray {
        val gcj02 = bd09toGcj02(lat, lon)
        val wgs84 = gcj02toWgs84(gcj02[0], gcj02[1])
        wgs84[0] = wgs84[0]
        wgs84[1] = wgs84[1]
        return wgs84
    }
}