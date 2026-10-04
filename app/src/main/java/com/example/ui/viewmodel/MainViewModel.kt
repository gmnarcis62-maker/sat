package com.example.ui.viewmodel

import android.app.Application
import android.graphics.Bitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.util.*
import com.example.data.hardware.ConnectionStatus
import com.example.data.hardware.DiscoveredDevice
import com.example.data.hardware.SignalData
import com.example.data.local.entity.*
import com.example.data.repository.AppRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class CityPreset(val name: String, val lat: Double, val lng: Double)

data class DiagnosticStep(
    val id: Int,
    val question: String,
    val symptomDescription: String,
    val toolRequired: String,
    val expectedMeasurement: String,
    val yesAction: String,
    val noAction: String,
    val safetyWarning: String = "هنگام کار با بخش اولیه پاور به ولتاژ ۴۰۰ ولت دست نزنید."
)

data class ChatMessage(
    val text: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis(),
    val image: Bitmap? = null
)

class MainViewModel(application: Application) : AndroidViewModel(application) {

    val repository = AppRepository(application, viewModelScope)
    val sensorTracker = SensorTracker(application)
    val audioSynthesizer = AudioFeedbackSynthesizer()

    // Persian Date
    val currentDatePersian = PersianDateUtil.gregorianToPersian()

    // City Presets in Iran
    val cities = listOf(
        CityPreset("تهران (پایتخت)", 35.6892, 51.3890),
        CityPreset("اصفهان", 32.6546, 51.6680),
        CityPreset("مشهد", 36.2972, 59.6067),
        CityPreset("شیراز", 29.5918, 52.5837),
        CityPreset("تبریز", 38.0800, 46.2919),
        CityPreset("اهواز", 31.3183, 48.6706),
        CityPreset("کرمانشاه", 34.3142, 47.0650),
        CityPreset("رشت", 37.2808, 49.5832),
        CityPreset("کرمان", 30.2839, 57.0834),
        CityPreset("ساری", 36.5659, 53.0586)
    )

    private val _selectedCity = MutableStateFlow(cities[0])
    val selectedCity: StateFlow<CityPreset> = _selectedCity.asStateFlow()

    private val _customLat = MutableStateFlow(35.6892)
    val customLat: StateFlow<Double> = _customLat.asStateFlow()

    private val _customLng = MutableStateFlow(51.3890)
    val customLng: StateFlow<Double> = _customLng.asStateFlow()

    // Satellites
    val satellites: StateFlow<List<SatelliteEntity>> = repository.allSatellites
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedSatellite = MutableStateFlow<SatelliteEntity?>(null)
    val selectedSatellite: StateFlow<SatelliteEntity?> = _selectedSatellite.asStateFlow()

    // Live Calculation
    val alignmentResult: StateFlow<SatelliteCalculator.AlignmentResult> = combine(
        _customLat, _customLng, _selectedSatellite
    ) { lat, lng, sat ->
        val satPos = sat?.orbitalPositionDeg ?: 52.5
        SatelliteCalculator.calculateAlignment(lat, lng, satPos)
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        SatelliteCalculator.calculateAlignment(35.6892, 51.3890, 52.5)
    )

    // Sensor Orientation
    val azimuth: StateFlow<Float> = sensorTracker.azimuth
    val pitch: StateFlow<Float> = sensorTracker.pitch
    val roll: StateFlow<Float> = sensorTracker.roll
    val hasSensors: StateFlow<Boolean> = sensorTracker.hasSensors

    // Angle difference between current phone bearing and target satellite
    val azimuthDifference: StateFlow<Float> = combine(azimuth, alignmentResult) { currentAz, targetResult ->
        var diff = targetResult.magneticAzimuthDeg.toFloat() - currentAz
        while (diff < -180f) diff += 360f
        while (diff > 180f) diff -= 360f
        diff
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0f)

    // Hardware Signal Meter Adapter
    val connectionStatus: StateFlow<ConnectionStatus> = repository.signalMeterAdapter.connectionStatus
    val liveSignal: StateFlow<SignalData> = repository.signalMeterAdapter.liveSignal
    val connectedDeviceName: StateFlow<String?> = repository.signalMeterAdapter.connectedDeviceName
    val discoveredDevices: StateFlow<List<DiscoveredDevice>> = repository.signalMeterAdapter.discoveredDevices

    private val _isAudioMuted = MutableStateFlow(false)
    val isAudioMuted: StateFlow<Boolean> = _isAudioMuted.asStateFlow()

    // Business Data
    val customers: StateFlow<List<CustomerEntity>> = repository.allCustomers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val missions: StateFlow<List<MissionEntity>> = repository.allMissions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val invoices: StateFlow<List<InvoiceEntity>> = repository.allInvoices
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val inventory: StateFlow<List<InventoryItemEntity>> = repository.allInventory
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val repairs: StateFlow<List<RepairCaseEntity>> = repository.allRepairs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val receivers: StateFlow<List<ReceiverModelEntity>> = repository.allReceivers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // AI Chat
    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(
        listOf(
            ChatMessage(
                text = "سلام و درود همکار گرامی! من دستیار تخصصی ماهواره‌یار هستم. سوالات فنی در زمینه تنظیم دیش، قیچی، عیب‌یابی برد رسیور، خطاهای نرم‌افزاری و محاسبات را مطرح فرمایید.",
                isUser = false
            )
        )
    )
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

    private val _isAiLoading = MutableStateFlow(false)
    val isAiLoading: StateFlow<Boolean> = _isAiLoading.asStateFlow()

    init {
        sensorTracker.startListening()

        // Auto-select first satellite once loaded
        viewModelScope.launch {
            satellites.collect { list ->
                if (list.isNotEmpty() && _selectedSatellite.value == null) {
                    _selectedSatellite.value = list.first()
                }
            }
        }

        // Sync Audio Beep with Signal
        viewModelScope.launch {
            liveSignal.collect { sig ->
                if (connectionStatus.value == ConnectionStatus.CONNECTED && !_isAudioMuted.value) {
                    audioSynthesizer.updateSignal(sig.qualityPercent, sig.isLocked, viewModelScope)
                } else {
                    audioSynthesizer.stop()
                }
            }
        }
    }

    fun selectCity(city: CityPreset) {
        _selectedCity.value = city
        _customLat.value = city.lat
        _customLng.value = city.lng
    }

    fun setCustomCoordinates(lat: Double, lng: Double) {
        _customLat.value = lat
        _customLng.value = lng
    }

    fun selectSatellite(sat: SatelliteEntity) {
        _selectedSatellite.value = sat
    }

    fun toggleAudioMute() {
        val newMute = !_isAudioMuted.value
        _isAudioMuted.value = newMute
        audioSynthesizer.setMuted(newMute)
    }

    fun startHardwareScan() {
        repository.signalMeterAdapter.startScan(viewModelScope)
    }

    fun connectHardwareDevice(id: String) {
        repository.signalMeterAdapter.connect(id, viewModelScope)
    }

    fun disconnectHardwareDevice() {
        repository.signalMeterAdapter.disconnect()
        audioSynthesizer.stop()
    }

    private val _isAiKeyConfigured = MutableStateFlow(repository.atriaAiService.getResolvedApiKey().isNotBlank())
    val isAiKeyConfigured: StateFlow<Boolean> = _isAiKeyConfigured.asStateFlow()

    fun saveAiApiKey(key: String) {
        repository.atriaAiService.saveLocalApiKey(key)
        _isAiKeyConfigured.value = repository.atriaAiService.getResolvedApiKey().isNotBlank()
    }

    fun getAiApiKey(): String {
        return repository.atriaAiService.getResolvedApiKey()
    }

    fun clearChatMessages() {
        _chatMessages.value = listOf(
            ChatMessage(
                text = "گفتگو بازنشانی شد. سوال فنی جدید خود را درباره دیش، محاسبات، فرکانس‌ها یا عیب‌یابی رسیور بفرمایید.",
                isUser = false
            )
        )
    }

    fun sendAiMessage(prompt: String, bitmap: Bitmap? = null) {
        if (prompt.isBlank() && bitmap == null) return

        val userMsg = ChatMessage(text = prompt, isUser = true, image = bitmap)
        _chatMessages.value = _chatMessages.value + userMsg
        _isAiLoading.value = true

        viewModelScope.launch {
            // Build conversation history from recent chat
            val history = _chatMessages.value
                .filter { it.text.isNotBlank() }
                .map { Pair(it.text, it.isUser) }

            val result = repository.atriaAiService.sendMessage(prompt, history)
            val replyText = when (result) {
                is com.example.data.api.atria.AtriaResult.Success -> {
                    result.reply
                }
                is com.example.data.api.atria.AtriaResult.Error -> {
                    // Combine friendly error notice with offline domain knowledge
                    val offlineKnowledge = repository.geminiService.askAssistant(prompt, bitmap)
                    "⚠️ ${result.userFriendlyMessage}\n\n$offlineKnowledge"
                }
            }

            _chatMessages.value = _chatMessages.value + ChatMessage(text = replyText, isUser = false)
            _isAiLoading.value = false
        }
    }

    // Business Actions
    fun addMission(mission: MissionEntity) {
        viewModelScope.launch { repository.addMission(mission) }
    }

    fun updateMissionStatus(mission: MissionEntity, newStatus: String) {
        viewModelScope.launch { repository.updateMission(mission.copy(status = newStatus)) }
    }

    fun addCustomer(customer: CustomerEntity) {
        viewModelScope.launch { repository.addCustomer(customer) }
    }

    fun addInvoice(invoice: InvoiceEntity) {
        viewModelScope.launch { repository.addInvoice(invoice) }
    }

    fun addInventoryItem(item: InventoryItemEntity) {
        viewModelScope.launch { repository.addInventoryItem(item) }
    }

    fun updateInventoryStock(item: InventoryItemEntity, delta: Int) {
        viewModelScope.launch {
            repository.updateInventoryItem(item.copy(quantity = (item.quantity + delta).coerceAtLeast(0)))
        }
    }

    fun addRepairCase(repair: RepairCaseEntity) {
        viewModelScope.launch { repository.addRepairCase(repair) }
    }

    override fun onCleared() {
        super.onCleared()
        sensorTracker.stopListening()
        audioSynthesizer.release()
    }
}
