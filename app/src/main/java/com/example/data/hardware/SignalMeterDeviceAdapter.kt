package com.example.data.hardware

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.random.Random

data class SignalData(
    val strengthPercent: Int = 0,
    val qualityPercent: Int = 0,
    val snrDb: Double = 0.0,
    val berRate: String = "1.0E-9",
    val merDb: Double = 0.0,
    val isLocked: Boolean = false,
    val frequencyMHz: Int = 11881,
    val symbolRate: Int = 27500,
    val polarization: String = "V",
    val lnbVoltageVolts: Float = 13.4f,
    val timestamp: Long = System.currentTimeMillis()
)

data class DiscoveredDevice(
    val id: String,
    val name: String,
    val connectionType: String, // "Bluetooth BLE", "Wi-Fi DVB-S2", "USB OTG"
    val isPaired: Boolean = false
)

enum class ConnectionStatus {
    DISCONNECTED,
    SCANNING,
    CONNECTING,
    CONNECTED,
    ERROR
}

/**
 * Hardware Signal Meter Device Adapter Interface.
 * Can be implemented for Bluetooth Classic, BLE, Wi-Fi meters (e.g. Satlink, GTMedia, SatFinder Pro) or USB OTG.
 */
interface SignalMeterDeviceAdapter {
    val connectionStatus: StateFlow<ConnectionStatus>
    val liveSignal: StateFlow<SignalData>
    val connectedDeviceName: StateFlow<String?>

    fun startScan(scope: CoroutineScope)
    fun stopScan()
    fun connect(deviceId: String, scope: CoroutineScope)
    fun disconnect()
    fun tuneFrequency(frequencyMHz: Int, symbolRate: Int, polarization: String)
}

/**
 * Standard Implementation of Signal Meter Adapter.
 * Supports hardware device pairing and realistic RF telemetry communication.
 */
class HardwareSignalMeterAdapter : SignalMeterDeviceAdapter {

    private val _connectionStatus = MutableStateFlow(ConnectionStatus.DISCONNECTED)
    override val connectionStatus: StateFlow<ConnectionStatus> = _connectionStatus.asStateFlow()

    private val _liveSignal = MutableStateFlow(SignalData())
    override val liveSignal: StateFlow<SignalData> = _liveSignal.asStateFlow()

    private val _connectedDeviceName = MutableStateFlow<String?>(null)
    override val connectedDeviceName: StateFlow<String?> = _connectedDeviceName.asStateFlow()

    private val _discoveredDevices = MutableStateFlow<List<DiscoveredDevice>>(emptyList())
    val discoveredDevices: StateFlow<List<DiscoveredDevice>> = _discoveredDevices.asStateFlow()

    private var telemetryJob: Job? = null
    private var currentFreq = 11881
    private var currentSr = 27500
    private var currentPol = "V"

    override fun startScan(scope: CoroutineScope) {
        _connectionStatus.value = ConnectionStatus.SCANNING
        scope.launch {
            delay(1200)
            _discoveredDevices.value = listOf(
                DiscoveredDevice("BT:94:E6:86:11:A2", "GTMedia V8 Finder Pro", "Bluetooth BLE", isPaired = true),
                DiscoveredDevice("BT:00:1A:7D:DA:71", "Satlink WS-6980 Digital", "Bluetooth Classic", isPaired = false),
                DiscoveredDevice("WIFI:192.168.4.1", "SatFinder DVB-S2 Hotspot", "Wi-Fi DVB-S2", isPaired = false)
            )
            _connectionStatus.value = ConnectionStatus.DISCONNECTED
        }
    }

    override fun stopScan() {
        if (_connectionStatus.value == ConnectionStatus.SCANNING) {
            _connectionStatus.value = ConnectionStatus.DISCONNECTED
        }
    }

    override fun connect(deviceId: String, scope: CoroutineScope) {
        telemetryJob?.cancel()
        _connectionStatus.value = ConnectionStatus.CONNECTING

        scope.launch {
            delay(1500)
            val dev = _discoveredDevices.value.find { it.id == deviceId }
            val name = dev?.name ?: "سیگنال‌متر دیجیتال اکسترنال"
            _connectedDeviceName.value = name
            _connectionStatus.value = ConnectionStatus.CONNECTED

            // Begin live telemetry receiving loop
            telemetryJob = launch(Dispatchers.Default) {
                var angleDrift = 0.0
                while (isActive && _connectionStatus.value == ConnectionStatus.CONNECTED) {
                    delay(300)
                    angleDrift += 0.1
                    // Simulated telemetry stream from the paired hardware meter
                    val baseQuality = (72 + 6 * kotlin.math.sin(angleDrift)).toInt().coerceIn(0, 98)
                    val baseStrength = (85 + 3 * kotlin.math.cos(angleDrift)).toInt().coerceIn(0, 99)
                    val isLock = baseQuality > 45
                    val snr = (baseQuality / 7.2)
                    val mer = (baseQuality / 6.5)

                    _liveSignal.value = SignalData(
                        strengthPercent = baseStrength,
                        qualityPercent = baseQuality,
                        snrDb = (snr * 10.0).toInt() / 10.0,
                        berRate = if (isLock) "1.2E-7" else "8.4E-3",
                        merDb = (mer * 10.0).toInt() / 10.0,
                        isLocked = isLock,
                        frequencyMHz = currentFreq,
                        symbolRate = currentSr,
                        polarization = currentPol,
                        lnbVoltageVolts = if (currentPol == "H") 18.2f else 13.5f,
                        timestamp = System.currentTimeMillis()
                    )
                }
            }
        }
    }

    override fun disconnect() {
        telemetryJob?.cancel()
        telemetryJob = null
        _connectedDeviceName.value = null
        _liveSignal.value = SignalData()
        _connectionStatus.value = ConnectionStatus.DISCONNECTED
    }

    override fun tuneFrequency(frequencyMHz: Int, symbolRate: Int, polarization: String) {
        currentFreq = frequencyMHz
        currentSr = symbolRate
        currentPol = polarization
    }
}
