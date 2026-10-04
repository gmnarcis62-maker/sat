package com.example.core.util

import kotlin.math.*

/**
 * High-precision Geostationary Satellite and Antenna Alignment Calculator.
 *
 * Implements rigorous geodesy and spherical trigonometry for:
 * 1. Topocentric Horizon True Azimuth (using four-quadrant atan2 with singular Zenith handling).
 * 2. Elevation angle and direct line-of-sight slant range distance.
 * 3. Polarization Skew angle of the LNB feedhorn (with equatorial and meridian boundary handling).
 * 4. Multi-LNB (قیچی) bracket physical offset estimation with configurable BDF and dish depth.
 * 5. Coaxial cable nominal attenuation (loss in dB) with frequency scaling.
 */
object SatelliteCalculator {

    // WGS84 / IUGG Physical Geodesy Reference Constants
    const val EARTH_RADIUS_KM = 6378.137          // Earth equatorial mean radius (Re)
    const val GEO_ORBIT_RADIUS_KM = 42164.17      // Geostationary orbit radius from Earth center (R0)
    const val DEFAULT_BDF = 0.88                  // Typical Beam Deviation Factor for prime-offset parabolic dishes

    data class AlignmentResult(
        val trueAzimuthDeg: Double,
        val magneticAzimuthDeg: Double,
        val elevationDeg: Double,
        val lnbSkewDeg: Double,
        val slantRangeKm: Double,
        val isVisible: Boolean,
        val isZenith: Boolean,
        val isSkewDefined: Boolean,
        val compassDirectionText: String,
        val magneticDeclinationDeg: Double,
        val isDeclinationEstimated: Boolean,
        val distanceKm: Double = slantRangeKm
    )

    data class MultiLnbResult(
        val orbitalDifferenceDeg: Double,
        val distanceCm: Double,
        val sidePosition: String,
        val elevationGuidance: String,
        val focalLengthCm: Double,
        val bdfUsed: Double,
        val minimumLnbSpacingWarning: String? = null
    )

    /**
     * Calculates geostationary antenna alignment angles using topocentric horizon vector geometry.
     *
     * @param observerLat Observer latitude in degrees [-90.0, +90.0] (North positive, South negative)
     * @param observerLng Observer longitude in degrees [-180.0, +180.0] (East positive, West negative)
     * @param satLongitude Satellite orbital longitude in degrees [-180.0, +180.0] (East positive, West negative)
     * @param magneticDeclination Known magnetic declination in degrees, or null to use regional empirical estimate
     * @throws IllegalArgumentException on NaN, Infinity, or values exceeding physical coordinate domains
     */
    fun calculateAlignment(
        observerLat: Double,
        observerLng: Double,
        satLongitude: Double,
        magneticDeclination: Double? = null
    ): AlignmentResult {
        validateCoordinates(observerLat, observerLng)
        validateLongitude(satLongitude, "satLongitude")
        if (magneticDeclination != null) {
            require(!magneticDeclination.isNaN() && !magneticDeclination.isInfinite()) {
                "انحراف مغناطیسی نمی‌تواند NaN یا بی‌نهایت باشد."
            }
        }

        val latRad = Math.toRadians(observerLat)
        var deltaLngDeg = satLongitude - observerLng

        // Normalize relative longitude to [-180.0, +180.0]
        while (deltaLngDeg > 180.0) deltaLngDeg -= 360.0
        while (deltaLngDeg < -180.0) deltaLngDeg += 360.0
        val deltaLngRad = Math.toRadians(deltaLngDeg)

        // 1. Central Angle (beta) between observer and sub-satellite point (cos(beta) = cos(lat) * cos(deltaLng))
        val cosBeta = (cos(latRad) * cos(deltaLngRad)).coerceIn(-1.0, 1.0)

        // 2. Direct Slant Range (Line-of-Sight distance)
        // d = sqrt(Re^2 + R0^2 - 2 * Re * R0 * cos(beta))
        val slantRangeKm = sqrt(
            EARTH_RADIUS_KM.pow(2) + GEO_ORBIT_RADIUS_KM.pow(2) -
                    2.0 * EARTH_RADIUS_KM * GEO_ORBIT_RADIUS_KM * cosBeta
        )

        // 3. Elevation Angle (El)
        // sin(El) = (R0 * cos(beta) - Re) / slantRange
        val sinElevation = ((GEO_ORBIT_RADIUS_KM * cosBeta - EARTH_RADIUS_KM) / slantRangeKm).coerceIn(-1.0, 1.0)
        val elevationDeg = Math.toDegrees(asin(sinElevation))
        val isVisible = elevationDeg > 0.0

        // 4. True Azimuth (Az) via Topocentric Horizon Vector (East, North)
        // In local horizon coordinates:
        // rho_East = R0 * sin(deltaLng)
        // rho_North = -R0 * sin(lat) * cos(deltaLng)
        val rhoEast = sin(deltaLngRad)
        val rhoNorth = -sin(latRad) * cos(deltaLngRad)

        // Detect Zenith Singularity: when observer is at equator directly under satellite (Sub-Satellite Point, SSP)
        // At Zenith, Elevation = 90° and Azimuth is mathematically indeterminate.
        val isZenith = abs(observerLat) < 1e-4 && abs(deltaLngDeg) < 1e-4

        var trueAzimuthDeg: Double
        val compassDirectionText: String

        if (isZenith) {
            trueAzimuthDeg = 0.0
            compassDirectionText = "سرسو (Zenith - بدون جهت آزیموت)"
        } else {
            trueAzimuthDeg = Math.toDegrees(atan2(rhoEast, rhoNorth))
            // Normalize Azimuth to [0.0, 360.0)
            while (trueAzimuthDeg < 0.0) trueAzimuthDeg += 360.0
            while (trueAzimuthDeg >= 360.0) trueAzimuthDeg -= 360.0
            compassDirectionText = getCompassDirectionText(trueAzimuthDeg)
        }

        // 5. Magnetic Declination
        val isDeclinationEstimated = (magneticDeclination == null)
        val declination = magneticDeclination ?: estimateApproximateMagneticDeclination(observerLat, observerLng)

        var magneticAzimuthDeg = if (isZenith) 0.0 else trueAzimuthDeg - declination
        while (magneticAzimuthDeg < 0.0) magneticAzimuthDeg += 360.0
        while (magneticAzimuthDeg >= 360.0) magneticAzimuthDeg -= 360.0

        // 6. LNB Polarization Skew Angle
        // Analytical spherical relation: Skew = atan2(cos(lat) * sin(deltaLng), sin(lat))
        // Sign convention (standard European / Middle Eastern practice):
        // Facing the front of the dish (looking towards the LNB feedhorn):
        // Positive (+) skew means rotate LNB CLOCKWISE.
        // Negative (-) skew means rotate LNB COUNTER-CLOCKWISE.
        val isSkewDefined = !isZenith
        val lnbSkewDeg: Double = if (isZenith) {
            0.0 // Arbitrary at zenith
        } else if (abs(observerLat) < 1e-6) {
            // Directly on equator: vertical polarization remains vertical or flips 90° for East/West
            if (deltaLngDeg >= 0) 90.0 else -90.0
        } else {
            val skewRad = atan2(cos(latRad) * sin(deltaLngRad), sin(latRad))
            Math.toDegrees(skewRad)
        }

        return AlignmentResult(
            trueAzimuthDeg = roundToOneDecimal(trueAzimuthDeg),
            magneticAzimuthDeg = roundToOneDecimal(magneticAzimuthDeg),
            elevationDeg = roundToOneDecimal(elevationDeg),
            lnbSkewDeg = roundToOneDecimal(lnbSkewDeg),
            slantRangeKm = roundToOneDecimal(slantRangeKm),
            isVisible = isVisible,
            isZenith = isZenith,
            isSkewDefined = isSkewDefined,
            compassDirectionText = compassDirectionText,
            magneticDeclinationDeg = roundToOneDecimal(declination),
            isDeclinationEstimated = isDeclinationEstimated
        )
    }

    /**
     * Estimates Multi-LNB (قیچی) bracket spacing using paraxial focal approximation.
     *
     * Mathematical limitations and assumptions:
     * 1. Uses the Beam Deviation Factor (BDF): theta_beam = BDF * theta_orbital.
     *    BDF typically ranges between 0.82 and 0.92 depending on the dish f/D and rim geometry.
     * 2. Assumes paraxial ray focus. For wide offsets (> 15° to 20°), spherical aberration,
     *    coma, and gain degradation occur, requiring curved brackets and LNB re-focusing.
     * 3. Physical mechanical constraint: standard LNB collars have a diameter of ~40mm - 60mm.
     *    Satellites separated by less than ~3° to 4° orbital difference cannot fit adjacent standard LNBs.
     *
     * @param primeSatLng Orbital position of central prime focus satellite
     * @param targetSatLng Orbital position of secondary offset satellite
     * @param dishDiameterCm Measured rim diameter in cm (must be > 0)
     * @param fOverD Focal length to diameter ratio (default 0.60)
     * @param dishDepthCm Optional physical depth of dish center in cm. If supplied, focal length F = D^2 / (16*depth)
     * @param bdf Beam Deviation Factor (default 0.88)
     */
    fun calculateMultiLnbOffset(
        primeSatLng: Double,
        targetSatLng: Double,
        dishDiameterCm: Double = 90.0,
        fOverD: Double = 0.6,
        dishDepthCm: Double? = null,
        bdf: Double = DEFAULT_BDF
    ): MultiLnbResult {
        validateLongitude(primeSatLng, "primeSatLng")
        validateLongitude(targetSatLng, "targetSatLng")
        validatePositive("dishDiameterCm", dishDiameterCm)
        validatePositive("fOverD", fOverD)
        validatePositive("bdf", bdf)
        require(bdf in 0.5..1.0) { "ضریب BDF باید در بازه منطقی [0.5, 1.0] باشد (دریافت شد: $bdf)." }

        val focalLengthCm = if (dishDepthCm != null) {
            validatePositive("dishDepthCm", dishDepthCm)
            dishDiameterCm.pow(2) / (16.0 * dishDepthCm)
        } else {
            dishDiameterCm * fOverD
        }

        var deltaSatDeg = targetSatLng - primeSatLng
        while (deltaSatDeg > 180.0) deltaSatDeg -= 360.0
        while (deltaSatDeg < -180.0) deltaSatDeg += 360.0
        val absDeltaDeg = abs(deltaSatDeg)

        val angularOffsetRad = Math.toRadians(absDeltaDeg * bdf)
        val linearDistanceCm = focalLengthCm * tan(angularOffsetRad)

        // Physical minimum collision check (standard LNB feedhorn collar is ~4.0 to 5.0 cm)
        val warningMessage = if (linearDistanceCm in 0.01..4.2) {
            "هشدار برخورد فیزیکی: فاصله محاسبه‌شده (${roundToOneDecimal(linearDistanceCm)} cm) کمتر از قطر فیزیکی گلویی LNB معمولی (~4.5 cm) است. استفاده از ال‌ان‌بی باریک (Slim/Bullet LNB) الزامی است."
        } else if (absDeltaDeg > 25.0) {
            "هشدار افت توان: اختلاف مداری بیش از ۲۵ درجه است. در این فاصله افت سیگنال به دلیل خطای کما (Coma Aberration) و خروج از کانون سهموی شدید خواهد بود."
        } else null

        // Inverting side due to parabolic reflector mirror inversion:
        // Standing IN FRONT of dish facing the reflector bowl:
        // Target satellite East of Prime -> LNB is placed to the WEST (LEFT).
        // Target satellite West of Prime -> LNB is placed to the EAST (RIGHT).
        val sideInFrontOfDish = if (deltaSatDeg > 0) "سمت چپ (غرب LNB مرکزی)" else "سمت راست (شرق LNB مرکزی)"
        val elevationShift = if (deltaSatDeg > 0) "کمی پایین‌تر از تراز LNB مرکزی" else "کمی بالاتر از تراز LNB مرکزی"

        return MultiLnbResult(
            orbitalDifferenceDeg = roundToOneDecimal(absDeltaDeg),
            distanceCm = roundToOneDecimal(linearDistanceCm),
            sidePosition = sideInFrontOfDish,
            elevationGuidance = elevationShift,
            focalLengthCm = roundToOneDecimal(focalLengthCm),
            bdfUsed = bdf,
            minimumLnbSpacingWarning = warningMessage
        )
    }

    /**
     * Calculates nominal coaxial cable signal attenuation (loss in dB).
     *
     * Reference Nominal Specifications at 2150 MHz (Top of Satellite L-Band IF 950 - 2150 MHz) per 100 meters:
     * - RG6 (Standard 75-ohm copper/CCS foam dielectric): ~31.5 dB / 100m
     * - RG11 (Heavy low-loss trunk coaxial cable): ~21.0 dB / 100m
     * - RG59 (Older thin 75-ohm cable, high loss at IF): ~44.0 dB / 100m
     *
     * Theoretical Limitation:
     * This calculation uses the first-order skin-effect model (loss proportional to sqrt(f)).
     * In reality, dielectric losses introduce a small linear term (alpha_d * f) at gigahertz frequencies.
     * Actual cable loss varies by manufacturer, age, temperature, moisture, and connector quality (typically +-10%).
     *
     * @param cableType "RG6", "RG11", or "RG59"
     * @param lengthMeters Length of run in meters (must be >= 0)
     * @param frequencyMHz Operating IF frequency in MHz (must be > 0)
     */
    fun calculateCableLoss(
        cableType: String,
        lengthMeters: Double,
        frequencyMHz: Double = 2150.0
    ): Double {
        require(!lengthMeters.isNaN() && !lengthMeters.isInfinite() && lengthMeters >= 0.0) {
            "طول کابل باید عددی نامنفی و متناهی باشد (دریافت شد: $lengthMeters)."
        }
        validatePositive("frequencyMHz", frequencyMHz)

        val lossPer100mAt2150 = when (cableType.trim().uppercase()) {
            "RG6" -> 31.5
            "RG11" -> 21.0
            "RG59" -> 44.0
            else -> throw IllegalArgumentException(
                "نوع کابل نامعتبر است: '$cableType'. انواع مجاز: RG6, RG11, RG59."
            )
        }

        // First-order skin-effect scaling factor
        val freqFactor = sqrt(frequencyMHz / 2150.0)
        val totalLossDb = (lossPer100mAt2150 * freqFactor) * (lengthMeters / 100.0)
        return roundToOneDecimal(totalLossDb)
    }

    /**
     * Regional empirical approximation for magnetic declination across the Iranian plateau
     * and neighboring Middle Eastern coordinates (~4.0° to 6.0° East).
     *
     * Note: This is an empirical regional estimate, not a full spherical harmonic WMM model.
     */
    fun estimateApproximateMagneticDeclination(lat: Double, lng: Double): Double {
        validateCoordinates(lat, lng)
        val declination = 4.8 + (lng - 50.0) * 0.05 - (lat - 35.0) * 0.03
        return roundToOneDecimal(declination.coerceIn(1.0, 9.0))
    }

    private fun getCompassDirectionText(azimuthDeg: Double): String {
        val normalized = (azimuthDeg % 360.0 + 360.0) % 360.0
        return when {
            normalized >= 337.5 || normalized < 22.5 -> "شمال (N)"
            normalized in 22.5..67.5 -> "شمال‌شرقی (NE)"
            normalized in 67.5..112.5 -> "شرق (E)"
            normalized in 112.5..157.5 -> "جنوب‌شرقی (SE)"
            normalized in 157.5..202.5 -> "جنوب (S)"
            normalized in 202.5..247.5 -> "جنوب‌غربی (SW)"
            normalized in 247.5..292.5 -> "غرب (W)"
            else -> "شمال‌غربی (NW)"
        }
    }

    private fun validateCoordinates(lat: Double, lng: Double) {
        require(!lat.isNaN() && !lat.isInfinite() && lat in -90.0..90.0) {
            "عرض جغرافیایی باید عددی معتبر در بازه [-90, +90] باشد (دریافت شد: $lat)."
        }
        require(!lng.isNaN() && !lng.isInfinite() && lng in -180.0..180.0) {
            "طول جغرافیایی باید عددی معتبر در بازه [-180, +180] باشد (دریافت شد: $lng)."
        }
    }

    private fun validateLongitude(lng: Double, name: String) {
        require(!lng.isNaN() && !lng.isInfinite() && lng in -180.0..180.0) {
            "$name باید عددی معتبر در بازه [-180, +180] باشد (دریافت شد: $lng)."
        }
    }

    private fun validatePositive(name: String, value: Double) {
        require(!value.isNaN() && !value.isInfinite() && value > 0.0) {
            "$name باید عددی متناهی و مثبت باشد (دریافت شد: $value)."
        }
    }

    private fun roundToOneDecimal(value: Double): Double {
        return (value * 10.0).roundToInt() / 10.0
    }
}
