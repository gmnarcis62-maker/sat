package com.example

import com.example.core.util.SatelliteCalculator
import org.junit.Assert.*
import org.junit.Test

/**
 * Rigorous Unit Tests for SatelliteCalculator.
 *
 * Test Count: 18 distinct test cases.
 * Validates:
 * 1. Tehran observer to Yahsat 52.5°E, Hotbird 13.0°E, Badr 26.0°E with exact analytical references.
 * 2. Zenith singularity (Sub-Satellite Point, SSP) where Azimuth is indeterminate and Elevation = 90°.
 * 3. Equatorial East/West symmetry and skew boundary behavior.
 * 4. Southern hemisphere orientation (pointing North towards Equator).
 * 5. Satellite visibility threshold (above vs below horizon).
 * 6. High Arctic polar latitude horizon limits.
 * 7. Multi-LNB paraxial offset with custom BDF, dish depth, and physical LNB collision warning.
 * 8. Coaxial cable nominal loss across RG6/RG11/RG59 at 2150 MHz and 950 MHz.
 * 9. Input validation rejecting NaN, Infinity, negative values, and out-of-domain coordinates.
 */
class SatelliteCalculatorTest {

    // Tehran Coordinates: 35.6892° N, 51.3890° E
    private val tehranLat = 35.6892
    private val tehranLng = 51.3890

    /**
     * Test 1: Tehran (35.6892° N, 51.3890° E) to Yahsat 1A (52.5° E)
     * Analytical derivation:
     * deltaLng = 52.5 - 51.3890 = +1.1110°
     * cosBeta = cos(35.6892°) * cos(1.1110°) = 0.812061
     * slantRange = sqrt(Re^2 + R0^2 - 2*Re*R0*cosBeta) = 37,171.7 km
     * sinElevation = (R0 * cosBeta - Re) / slantRange = 0.74950 -> Elevation = 48.55°
     * rhoEast = sin(1.1110°) = 0.019389, rhoNorth = -sin(35.6892°) * cos(1.1110°) = -0.583272
     * True Azimuth = atan2(0.019389, -0.583272) = 178.10° (just East of South 180°)
     * Skew = atan2(cos(35.6892°)*sin(1.1110°), sin(35.6892°)) = +1.55° (Clockwise)
     */
    @Test
    fun testTehranToYahsat52_5E() {
        val result = SatelliteCalculator.calculateAlignment(
            observerLat = tehranLat,
            observerLng = tehranLng,
            satLongitude = 52.5,
            magneticDeclination = 4.8
        )

        assertEquals("True Azimuth must be 178.1°", 178.1, result.trueAzimuthDeg, 0.15)
        assertEquals("Elevation must be 48.5°", 48.5, result.elevationDeg, 0.15)
        assertEquals("Skew must be +1.5°", 1.5, result.lnbSkewDeg, 0.15)
        assertEquals("Slant range must be ~37172 km", 37171.7, result.slantRangeKm, 2.0)
        assertTrue("Satellite is above horizon", result.isVisible)
        assertFalse("Not zenith", result.isZenith)
        assertTrue("Skew is defined", result.isSkewDefined)
        assertEquals("Magnetic Azimuth = True - Declination", 173.3, result.magneticAzimuthDeg, 0.15)
        assertFalse("Explicit declination was passed", result.isDeclinationEstimated)
    }

    /**
     * Test 2: Tehran to Hotbird 13.0° E (West of Tehran)
     * Analytical derivation:
     * deltaLng = 13.0 - 51.3890 = -38.3890°
     * cosBeta = cos(35.6892°) * cos(-38.3890°) = 0.636603
     * slantRange = sqrt(Re^2 + R0^2 - 2*Re*R0*cosBeta) = 38,420.0 km
     * Elevation = 32.18°
     * True Azimuth = atan2(sin(-38.389°), -sin(35.6892°)*cos(-38.389°)) = 233.63°
     * Skew = -40.85° (Counter-Clockwise)
     */
    @Test
    fun testTehranToHotbird13E() {
        val result = SatelliteCalculator.calculateAlignment(
            observerLat = tehranLat,
            observerLng = tehranLng,
            satLongitude = 13.0
        )

        assertEquals("True Azimuth must be 233.6°", 233.6, result.trueAzimuthDeg, 0.15)
        assertEquals("Elevation must be 32.2°", 32.2, result.elevationDeg, 0.15)
        assertEquals("Skew must be -40.8°", -40.8, result.lnbSkewDeg, 0.15)
        assertEquals("Slant range must be ~38420 km", 38420.0, result.slantRangeKm, 2.0)
        assertTrue("Satellite is visible", result.isVisible)
        assertEquals("جنوب‌غربی (SW)", result.compassDirectionText)
        assertTrue("Declination was estimated regionally", result.isDeclinationEstimated)
    }

    /**
     * Test 3: Tehran to Badr 26.0° E (West of Tehran)
     * Analytical derivation:
     * deltaLng = 26.0 - 51.3890 = -25.3890°
     * cosBeta = cos(35.6892°) * cos(-25.3890°) = 0.733560
     * slantRange = 37,733.9 km
     * Elevation = 40.61°
     * True Azimuth = 219.13°
     * Skew = -30.83°
     */
    @Test
    fun testTehranToBadr26E() {
        val result = SatelliteCalculator.calculateAlignment(
            observerLat = tehranLat,
            observerLng = tehranLng,
            satLongitude = 26.0
        )

        assertEquals("True Azimuth must be 219.1°", 219.1, result.trueAzimuthDeg, 0.15)
        assertEquals("Elevation must be 40.6°", 40.6, result.elevationDeg, 0.15)
        assertEquals("Skew must be -30.8°", -30.8, result.lnbSkewDeg, 0.15)
        assertEquals("Slant range must be ~37734 km", 37733.9, result.slantRangeKm, 2.0)
        assertTrue("Satellite is visible", result.isVisible)
    }

    /**
     * Test 4: Sub-Satellite Point (SSP) Zenith Singularity
     * Observer at Equator (lat = 0.0°) directly below satellite (lng = 52.5°, sat = 52.5°).
     * Exact geometry:
     * deltaLng = 0.0° -> cosBeta = 1.0.
     * slantRange = R0 - Re = 42164.17 - 6378.137 = 35,786.033 km.
     * Elevation = 90.0° (Zenith).
     * Azimuth is geometrically singular / undefined!
     */
    @Test
    fun testZenithSingularityDirectlyUnderSatellite() {
        val result = SatelliteCalculator.calculateAlignment(
            observerLat = 0.0,
            observerLng = 52.5,
            satLongitude = 52.5
        )

        assertTrue("Must detect zenith condition", result.isZenith)
        assertFalse("Skew is undefined at zenith", result.isSkewDefined)
        assertEquals("Elevation must be exactly 90.0°", 90.0, result.elevationDeg, 0.05)
        assertEquals("Slant range must equal R0 - Re = 35786.0 km", 35786.0, result.slantRangeKm, 0.1)
        assertTrue("Direction text must state Zenith", result.compassDirectionText.contains("Zenith"))
    }

    /**
     * Test 5: Observer on Equator looking East along the equator
     * Lat = 0.0°, Lng = 0.0°, Sat = 50.0° E.
     * deltaLng = +50.0° -> cosBeta = cos(50°) = 0.642788.
     * slantRange = 38,349.5 km -> Elevation = 32.69°
     * rhoEast = sin(50°) > 0, rhoNorth = -sin(0)*cos(50°) = 0.
     * True Azimuth = atan2(+, 0) = 90.0° (Due East).
     * Skew = +90.0°.
     */
    @Test
    fun testEquatorLookingEast() {
        val result = SatelliteCalculator.calculateAlignment(
            observerLat = 0.0,
            observerLng = 0.0,
            satLongitude = 50.0
        )

        assertEquals("On equator looking East, Azimuth must be 90.0°", 90.0, result.trueAzimuthDeg, 0.05)
        assertEquals("On equator looking East, Skew must be +90.0°", 90.0, result.lnbSkewDeg, 0.05)
        assertEquals("Elevation must be 32.7°", 32.7, result.elevationDeg, 0.1)
        assertEquals("شرق (E)", result.compassDirectionText)
    }

    /**
     * Test 6: Observer on Equator looking West along the equator
     * Lat = 0.0°, Lng = 0.0°, Sat = -50.0° (50° W).
     * rhoEast = sin(-50°) < 0, rhoNorth = 0.
     * True Azimuth = atan2(-, 0) = 270.0° (Due West).
     * Skew = -90.0°.
     */
    @Test
    fun testEquatorLookingWest() {
        val result = SatelliteCalculator.calculateAlignment(
            observerLat = 0.0,
            observerLng = 0.0,
            satLongitude = -50.0
        )

        assertEquals("On equator looking West, Azimuth must be 270.0°", 270.0, result.trueAzimuthDeg, 0.05)
        assertEquals("On equator looking West, Skew must be -90.0°", -90.0, result.lnbSkewDeg, 0.05)
        assertEquals("Elevation must be 32.7°", 32.7, result.elevationDeg, 0.1)
        assertEquals("غرب (W)", result.compassDirectionText)
    }

    /**
     * Test 7: Southern Hemisphere Observer
     * Sydney, Australia: 33.8688° S (-33.8688°), 151.2093° E
     * Optus D2 satellite at 152.0° E (deltaLng = +0.7907°)
     * In the Southern hemisphere, the equator is to the NORTH!
     * rhoNorth = -sin(-33.8688°) * cos(0.7907°) = +0.55722 (> 0, pointing North).
     * True Azimuth = atan2(sin(0.7907°), +0.55722) = 1.42° (Clockwise from True North).
     * Elevation = 50.60°.
     */
    @Test
    fun testSouthernHemisphereObserverPointsNorth() {
        val result = SatelliteCalculator.calculateAlignment(
            observerLat = -33.8688,
            observerLng = 151.2093,
            satLongitude = 152.0,
            magneticDeclination = 12.0
        )

        assertEquals("Sydney to Optus D2 Azimuth must point North (~1.4°)", 1.4, result.trueAzimuthDeg, 0.15)
        assertEquals("Elevation must be 50.6°", 50.6, result.elevationDeg, 0.15)
        assertTrue("Visible", result.isVisible)
        assertFalse("Not zenith", result.isZenith)
    }

    /**
     * Test 8: Satellite Below Horizon
     * Tehran looking at Amazonas at 61.0° W (deltaLng = -112.4°).
     * cosBeta = cos(35.6892°) * cos(-112.389°) = -0.30907 < Re/R0 (~0.151269).
     * Elevation is negative (-23.5°), satellite is behind Earth's curvature.
     */
    @Test
    fun testSatelliteBelowHorizonIsNotVisible() {
        val result = SatelliteCalculator.calculateAlignment(
            observerLat = tehranLat,
            observerLng = tehranLng,
            satLongitude = -61.0
        )

        assertFalse("Must NOT be visible", result.isVisible)
        assertTrue("Elevation must be negative", result.elevationDeg < 0.0)
    }

    /**
     * Test 9: High Arctic Latitude Horizon Limit
     * At 85.0° N latitude, the geostationary arc is below the horizon for all longitudes
     * (Geometric horizon limit is arccos(Re/R0) = 81.3°).
     */
    @Test
    fun testHighArcticLatitudeHorizonLimit() {
        val result = SatelliteCalculator.calculateAlignment(
            observerLat = 85.0,
            observerLng = 0.0,
            satLongitude = 0.0
        )

        assertFalse("Geostationary satellites are below horizon at 85° N", result.isVisible)
        assertTrue("Elevation is negative", result.elevationDeg < 0.0)
    }

    /**
     * Test 10: Multi-LNB Offset with default BDF (0.88)
     * Prime: Hotbird 13.0° E, Target: Yahsat 52.5° E (deltaSat = 39.5°).
     * Dish diameter = 90 cm, f/D = 0.6 -> focalLength F = 54.0 cm.
     * Offset angle = 39.5° * 0.88 = 34.76°.
     * Linear offset = F * tan(34.76°) = 54.0 * 0.69396 = 37.47 cm.
     */
    @Test
    fun testMultiLnbOffsetStandard90cmDish() {
        val result = SatelliteCalculator.calculateMultiLnbOffset(
            primeSatLng = 13.0,
            targetSatLng = 52.5,
            dishDiameterCm = 90.0,
            fOverD = 0.6
        )

        assertEquals("Orbital difference = 39.5°", 39.5, result.orbitalDifferenceDeg, 0.05)
        assertEquals("Focal length = 54.0 cm", 54.0, result.focalLengthCm, 0.05)
        assertEquals("Distance = 37.5 cm", 37.5, result.distanceCm, 0.1)
        assertTrue("Must be on Left side (West)", result.sidePosition.contains("سمت چپ"))
        assertEquals(0.88, result.bdfUsed, 0.001)
        assertNotNull("Delta > 25° triggers coma aberration warning", result.minimumLnbSpacingWarning)
        assertTrue(result.minimumLnbSpacingWarning!!.contains("اختلاف مداری بیش از ۲۵ درجه"))
    }

    /**
     * Test 11: Multi-LNB with custom BDF and measured dish depth
     * Dish diameter = 100 cm, measured depth c = 12.0 cm.
     * Parabolic focal length: F = D^2 / (16 * c) = 10000 / 192 = 52.08 cm.
     * Orbital delta = 6.0° (e.g. Hotbird 13°E to Eutelsat 7°E).
     * BDF = 0.85 -> angle = 5.10°.
     * Linear offset = 52.08 * tan(5.10°) = 4.65 cm.
     */
    @Test
    fun testMultiLnbWithMeasuredDepthAndCustomBdf() {
        val result = SatelliteCalculator.calculateMultiLnbOffset(
            primeSatLng = 13.0,
            targetSatLng = 7.0,
            dishDiameterCm = 100.0,
            fOverD = 0.6,
            dishDepthCm = 12.0,
            bdf = 0.85
        )

        assertEquals(6.0, result.orbitalDifferenceDeg, 0.05)
        assertEquals("Focal length from depth = 52.1 cm", 52.1, result.focalLengthCm, 0.1)
        assertEquals("Distance = 4.6 cm", 4.6, result.distanceCm, 0.1)
        assertTrue("Target is West of Prime, so LNB is on Right side (East)", result.sidePosition.contains("سمت راست"))
    }

    /**
     * Test 12: Multi-LNB Physical Collision Warning for Close Satellites
     * Delta = 2.0° (e.g. Eutelsat 7°E and Eutelsat 9°E).
     * Linear distance ~ 1.5 cm, which is less than physical 40mm LNB feed collar!
     */
    @Test
    fun testMultiLnbPhysicalCollisionWarning() {
        val result = SatelliteCalculator.calculateMultiLnbOffset(
            primeSatLng = 7.0,
            targetSatLng = 9.0,
            dishDiameterCm = 90.0
        )

        assertTrue("Linear distance is small (< 3cm)", result.distanceCm < 3.0)
        assertNotNull("Collision warning must be generated", result.minimumLnbSpacingWarning)
        assertTrue(result.minimumLnbSpacingWarning!!.contains("هشدار برخورد فیزیکی"))
    }

    /**
     * Test 13: Cable Loss Nominal Specifications at 2150 MHz
     * 100m RG6 = 31.5 dB
     * 20m RG6 = 6.3 dB
     * 100m RG11 = 21.0 dB
     * 100m RG59 = 44.0 dB
     */
    @Test
    fun testCableLossNominalSpecifications() {
        val rg6_100m = SatelliteCalculator.calculateCableLoss("RG6", 100.0, 2150.0)
        assertEquals(31.5, rg6_100m, 0.05)

        val rg6_20m = SatelliteCalculator.calculateCableLoss("RG6", 20.0, 2150.0)
        assertEquals(6.3, rg6_20m, 0.05)

        val rg11_100m = SatelliteCalculator.calculateCableLoss("RG11", 100.0, 2150.0)
        assertEquals(21.0, rg11_100m, 0.05)

        val rg59_100m = SatelliteCalculator.calculateCableLoss("RG59", 100.0, 2150.0)
        assertEquals(44.0, rg59_100m, 0.05)
    }

    /**
     * Test 14: Cable Loss Frequency Scaling
     * At 950 MHz (bottom of satellite IF):
     * Loss = 31.5 * sqrt(950 / 2150) = 31.5 * 0.66472 = 20.94 dB / 100m.
     */
    @Test
    fun testCableLossFrequencyScaling() {
        val loss950 = SatelliteCalculator.calculateCableLoss("RG6", 100.0, 950.0)
        assertEquals("At 950 MHz loss should be 20.9 dB", 20.9, loss950, 0.1)

        val lossZeroLength = SatelliteCalculator.calculateCableLoss("RG6", 0.0, 2150.0)
        assertEquals("Zero length has 0 dB loss", 0.0, lossZeroLength, 0.01)
    }

    /**
     * Test 15: Validation of Out-of-Domain Coordinates
     */
    @Test(expected = IllegalArgumentException::class)
    fun testValidationLatitudeOutOfRange() {
        SatelliteCalculator.calculateAlignment(observerLat = 90.5, observerLng = 51.0, satLongitude = 52.5)
    }

    /**
     * Test 16: Validation of NaN and Infinity Coordinates
     */
    @Test(expected = IllegalArgumentException::class)
    fun testValidationRejectNaNCoordinates() {
        SatelliteCalculator.calculateAlignment(observerLat = Double.NaN, observerLng = 51.0, satLongitude = 52.5)
    }

    /**
     * Test 17: Validation of Invalid Cable Inputs
     */
    @Test(expected = IllegalArgumentException::class)
    fun testValidationNegativeCableLength() {
        SatelliteCalculator.calculateCableLoss("RG6", -5.0, 2150.0)
    }

    /**
     * Test 18: Validation of Invalid Dish Parameters
     */
    @Test(expected = IllegalArgumentException::class)
    fun testValidationNegativeDishDiameter() {
        SatelliteCalculator.calculateMultiLnbOffset(
            primeSatLng = 13.0,
            targetSatLng = 52.5,
            dishDiameterCm = -90.0
        )
    }
}
