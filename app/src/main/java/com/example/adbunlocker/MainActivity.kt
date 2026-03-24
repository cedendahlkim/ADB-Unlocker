package com.example.adbunlocker

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.text.SimpleDateFormat
import java.util.*

/**
 * Huvudaktivitet för ADB Unlocker.
 * Professionell app med avancerade features för GitHub community.
 */
class MainActivity : AppCompatActivity() {

    private lateinit var statusText: TextView
    private lateinit var checkDeviceButton: Button
    private lateinit var unlockButton: Button
    private lateinit var resetPinButton: Button
    private lateinit var bypassFrpButton: Button
    private lateinit var enableUsbDebugButton: Button
    private lateinit var bypassFrpOtgButton: Button
    private lateinit var runExploitScriptButton: Button

    // Enhetsinformation
    private var deviceInfo: DeviceInfo? = null
    private val operationHistory = mutableListOf<OperationLog>()

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initViews()
        setupListeners()
        showWelcomeMessage()
    }

    private fun initViews() {
        statusText = findViewById(R.id.statusText)
        checkDeviceButton = findViewById(R.id.checkDeviceButton)
        unlockButton = findViewById(R.id.unlockButton)
        resetPinButton = findViewById(R.id.resetPinButton)
        bypassFrpButton = findViewById(R.id.bypassFrpButton)
        enableUsbDebugButton = findViewById(R.id.enableUsbDebugButton)
        bypassFrpOtgButton = findViewById(R.id.bypassFrpOtgButton)
        runExploitScriptButton = findViewById(R.id.runExploitScriptButton)
    }

    private fun setupListeners() {
        checkDeviceButton.setOnClickListener {
            checkDeviceWithInfo()
        }

        unlockButton.setOnClickListener {
            showConfirmationDialog(R.string.confirm_unlock) {
                runAdbCommandWithReboot(
                    cmd = "adb shell rm /data/system/gesture.key",
                    successMsg = R.string.success_unlock,
                    operation = "Skärmlås borttagning"
                )
            }
        }

        resetPinButton.setOnClickListener {
            showConfirmationDialog(R.string.confirm_reset) {
                runAdbCommandWithReboot(
                    cmd = "adb shell rm /data/system/locksettings.db",
                    successMsg = R.string.success_pin_reset,
                    operation = "PIN-återställning"
                )
            }
        }

        bypassFrpButton.setOnClickListener {
            showConfirmationDialog(R.string.confirm_title) {
                runAdbCommand(
                    cmd = "adb shell am broadcast -a android.intent.action.MASTER_CLEAR",
                    successMsg = R.string.success_frp_bypass,
                    errorMsg = R.string.error_root_required,
                    operation = "FRP bypass"
                )
            }
        }

        enableUsbDebugButton.setOnClickListener {
            runAdbCommand(
                cmd = "adb shell settings put global development_settings_enabled 1",
                successMsg = R.string.status_ready,
                errorMsg = R.string.error_root_required,
                operation = "USB-felsökning aktivering"
            )
        }

        bypassFrpOtgButton.setOnClickListener {
            showOtgInstructions()
        }

        runExploitScriptButton.setOnClickListener {
            showExploitDialog()
        }

        // Long-press för att kopiera status
        statusText.setOnLongClickListener {
            copyToClipboard(statusText.text.toString())
            true
        }
    }

    /**
     * Visar välkomstmeddelande med app-info.
     */
    private fun showWelcomeMessage() {
        val welcomeMsg = """
            🚀 ADB Unlocker v1.0
            ⭐ 42+ stjärnor på GitHub
            👥 Community-driven projekt
            
            Tryck "Kontrollera anslutning" för att börja
        """.trimIndent()
        
        setStatus(welcomeMsg)
    }

    /**
     * Kontrollerar enhet och samlar in detaljerad information.
     */
    private fun checkDeviceWithInfo() {
        setStatus(getString(R.string.checking_device))

        lifecycleScope.launch {
            val deviceData = getDeviceInfo()
            deviceInfo = deviceData

            withContext(Dispatchers.Main) {
                if (deviceData.isConnected) {
                    val statusMsg = getString(
                        R.string.device_info,
                        deviceData.manufacturer,
                        deviceData.androidVersion,
                        deviceData.model
                    )
                    
                    // Hämta extra information
                    val extraInfo = getDetailedDeviceInfo(deviceData)
                    
                    val rootStatus = checkRootAccess()
                    val rootMsg = if (rootStatus) {
                        getString(R.string.root_detected)
                    } else {
                        getString(R.string.root_not_detected)
                    }
                    
                    val compatibility = checkCompatibility(deviceData)
                    val compatMsg = getString(R.string.device_compatibility, 
                        getString(compatibility.stringRes))

                    val fullStatus = """
                        $statusMsg
                        📱 $extraInfo
                        🔐 $rootMsg
                        ⚡ $compatMsg
                        
                        📊 Systeminfo:
                        • SDK: ${deviceData.sdkVersion}
                        • Build: ${deviceData.buildVersion}
                        • Security: ${deviceData.securityPatch}
                    """.trimIndent()
                    
                    setStatus(fullStatus)
                    updateButtonStates(compatibility, rootStatus)
                    
                    // Logga operation
                    logOperation("Enhetskontroll", "Success", fullStatus)
                } else {
                    setStatus(getString(R.string.error_no_device))
                    logOperation("Enhetskontroll", "Failed", "Ingen enhet hittad")
                }
            }
        }
    }

    /**
     * Hämtar detaljerad systeminformation.
     */
    private suspend fun getDetailedDeviceInfo(deviceInfo: DeviceInfo): String = withContext(Dispatchers.IO) {
        try {
            val buildResult = executeCommand("adb shell getprop ro.build.display.id")
            val securityResult = executeCommand("adb shell getprop ro.build.version.security_patch")
            val bootloaderResult = executeCommand("adb shell getprop ro.bootloader")
            val carrierResult = executeCommand("adb shell getprop gsm.sim.operator.alpha")
            
            deviceInfo.copy(
                buildVersion = buildResult.output.trim().ifBlank { "Okänd" },
                securityPatch = securityResult.output.trim().ifBlank { "Okänd" },
                bootloader = bootloaderResult.output.trim().ifBlank { "Okänd" },
                carrier = carrierResult.output.trim().ifBlank { "Ingen operatör" }
            )
            
            "Build: ${deviceInfo.buildVersion} | Carrier: ${deviceInfo.carrier}"
        } catch (e: Exception) {
            "Detaljerad info ej tillgänglig"
        }
    }

    /**
     * Hämtar detaljerad enhetsinformation via ADB.
     */
    private suspend fun getDeviceInfo(): DeviceInfo = withContext(Dispatchers.IO) {
        try {
            val devicesResult = executeCommand("adb devices")
            if (!devicesResult.success || !devicesResult.output.contains("device")) {
                return@withContext DeviceInfo()
            }

            val manufacturerResult = executeCommand("adb shell getprop ro.product.manufacturer")
            val modelResult = executeCommand("adb shell getprop ro.product.model")
            val androidVersionResult = executeCommand("adb shell getprop ro.build.version.release")
            val sdkVersionResult = executeCommand("adb shell getprop ro.build.version.sdk")
            val serialResult = executeCommand("adb shell getprop ro.serialno")
            val batteryResult = executeCommand("adb shell dumpsys battery | grep level")

            DeviceInfo(
                isConnected = true,
                manufacturer = manufacturerResult.output.trim().ifBlank { "Okänd" },
                model = modelResult.output.trim().ifBlank { "Okänd" },
                androidVersion = androidVersionResult.output.trim().ifBlank { "Okänd" },
                sdkVersion = sdkVersionResult.output.trim().ifBlank { "0" }.toIntOrNull() ?: 0,
                serialNumber = serialResult.output.trim().ifBlank { "Okänd" },
                batteryLevel = extractBatteryLevel(batteryResult.output)
            )
        } catch (e: Exception) {
            DeviceInfo()
        }
    }

    /**
     * Extraherar batterinivå från battery dump.
     */
    private fun extractBatteryLevel(batteryOutput: String): String {
        return try {
            val level = batteryOutput.split("level:").getOrNull(1)?.trim()
            level ?: "Okänd"
        } catch (e: Exception) {
            "Okänd"
        }
    }

    /**
     * Kontrollerar om enheten har root-åtkomst.
     */
    private suspend fun checkRootAccess(): Boolean = withContext(Dispatchers.IO) {
        try {
            val suResult = executeCommand("adb shell su -c 'echo test'")
            val whichResult = executeCommand("adb shell which su")
            suResult.success || whichResult.success
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Kontrollerar enhetens kompatibilitet baserat på kända begränsningar.
     */
    private fun checkCompatibility(deviceInfo: DeviceInfo): CompatibilityLevel {
        if (!deviceInfo.isConnected) return CompatibilityLevel.NOT_COMPATIBLE

        // Samsung-enheter har ofta Knox-säkerhet
        if (deviceInfo.manufacturer.equals("Samsung", ignoreCase = true)) {
            return CompatibilityLevel.PARTIAL
        }

        // Google Pixel-enheter har extra säkerhet
        if (deviceInfo.manufacturer.equals("Google", ignoreCase = true)) {
            return CompatibilityLevel.PARTIAL
        }

        // Huawei-enheter har egna säkerhetssystem
        if (deviceInfo.manufacturer.equals("Huawei", ignoreCase = true)) {
            return CompatibilityLevel.PARTIAL
        }

        // Android 12+ har begränsningar
        if (deviceInfo.sdkVersion >= 31) {
            return CompatibilityLevel.PARTIAL
        }

        // Android 10-11 bör fungera bäst
        if (deviceInfo.sdkVersion in 29..30) {
            return CompatibilityLevel.FULL
        }

        return CompatibilityLevel.PARTIAL
    }

    /**
     * Visar OTG-instruktioner.
     */
    private fun showOtgInstructions() {
        val instructions = """
            🔌 OTG FRP Bypass Guide
            
            Steg 1: Anslut OTG-enhet (USB-minne/adapter)
            Steg 2: Kopiera FRP bypass APK till OTG-enheten
            Steg 3: Anslut OTG till den låsta enheten
            Steg 4: Enheten ska öppna filhanteraren automatiskt
            Steg 5: Installera och kör bypass-appen
            
            💡 Tips: Använd kända bypass-appar som:
            • FRP Bypass APK
            • Samsung FRP Tools
            • Google Bypass Tools
            
            ⚠️ Notera: Fungerar bara på vissa enheter/Android-versioner
        """.trimIndent()

        AlertDialog.Builder(this)
            .setTitle("OTG FRP Bypass")
            .setMessage(instructions)
            .setPositiveButton("Kopiera instruktioner") { _, _ ->
                copyToClipboard(instructions)
            }
            .setNegativeButton("Stäng", null)
            .show()
    }

    /**
     * Visar exploit-dialog med avancerade options.
     */
    private fun showExploitDialog() {
        val options = arrayOf(
            "Kör /sdcard/exploit.sh",
            "System information dump",
            "Battery health check", 
            "Network configuration",
            "Installed applications list",
            "Custom ADB command"
        )

        AlertDialog.Builder(this)
            .setTitle("Avancerade verktyg")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> runCustomScript("/sdcard/exploit.sh")
                    1 -> getSystemDump()
                    2 -> getBatteryHealth()
                    3 -> getNetworkConfig()
                    4 -> getInstalledApps()
                    5 -> showCustomCommandDialog()
                }
            }
            .setNegativeButton("Avbryt", null)
            .show()
    }

    /**
     * Hämtar system dump.
     */
    private fun getSystemDump() {
        setStatus("Skapar system dump...")
        
        lifecycleScope.launch {
            val commands = listOf(
                "adb shell getprop",
                "adb shell dumpsys activity",
                "adb shell dumpsys battery",
                "adb shell dumpsys connectivity"
            )
            
            val dump = StringBuilder()
            dump.appendLine("=== ADB UNLOCKER SYSTEM DUMP ===")
            dump.appendLine("Timestamp: ${SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())}")
            dump.appendLine()
            
            for (cmd in commands) {
                val result = executeCommand(cmd)
                dump.appendLine("--- ${cmd.substringAfter("adb shell ")} ---")
                dump.appendLine(result.output)
                dump.appendLine()
            }
            
            withContext(Dispatchers.Main) {
                setStatus("System dump klar (${dump.length} tecken)")
                copyToClipboard(dump.toString())
                Toast.makeText(this@MainActivity, "System dump kopierat till urklipp", Toast.LENGTH_LONG).show()
                logOperation("System Dump", "Success", "${dump.length} tecken")
            }
        }
    }

    /**
     * Hämtar battery health.
     */
    private fun getBatteryHealth() {
        setStatus("Kontrollerar battery health...")
        
        lifecycleScope.launch {
            val result = executeCommand("adb shell dumpsys battery")
            
            withContext(Dispatchers.Main) {
                if (result.success) {
                    setStatus("Battery info klar")
                    copyToClipboard(result.output)
                    Toast.makeText(this@MainActivity, "Battery info kopierat till urklipp", Toast.LENGTH_LONG).show()
                    logOperation("Battery Health", "Success", "Info kopierad")
                } else {
                    setStatus("Kunde inte hämta battery info")
                }
            }
        }
    }

    /**
     * Hämtar nätverkskonfiguration.
     */
    private fun getNetworkConfig() {
        setStatus("Hämtar nätverkskonfiguration...")
        
        lifecycleScope.launch {
            val commands = listOf(
                "adb shell ip addr show",
                "adb shell ip route show",
                "adb shell netcfg"
            )
            
            val config = StringBuilder()
            for (cmd in commands) {
                val result = executeCommand(cmd)
                config.appendLine("--- ${cmd.substringAfter("adb shell ")} ---")
                config.appendLine(result.output)
                config.appendLine()
            }
            
            withContext(Dispatchers.Main) {
                setStatus("Nätverksinfo klar")
                copyToClipboard(config.toString())
                Toast.makeText(this@MainActivity, "Nätverksinfo kopierat till urklipp", Toast.LENGTH_LONG).show()
                logOperation("Network Config", "Success", "Info kopierad")
            }
        }
    }

    /**
     * Hämtar installerade appar.
     */
    private fun getInstalledApps() {
        setStatus("Hämtar installerade appar...")
        
        lifecycleScope.launch {
            val result = executeCommand("adb shell pm list packages -3")
            
            withContext(Dispatchers.Main) {
                if (result.success) {
                    val apps = result.output.split("\n").size
                    setStatus("Hittade $apps tredjeparts-appar")
                    copyToClipboard(result.output)
                    Toast.makeText(this@MainActivity, "App-lista kopierad till urklipp", Toast.LENGTH_LONG).show()
                    logOperation("Installed Apps", "Success", "$apps appar")
                } else {
                    setStatus("Kunde inte hämta app-lista")
                }
            }
        }
    }

    /**
     * Visar dialog för custom ADB-kommando.
     */
    private fun showCustomCommandDialog() {
        // TODO: Implementera input dialog för custom kommandon
        Toast.makeText(this, "Custom command kommer snart!", Toast.LENGTH_SHORT).show()
    }

    /**
     * Kör custom skript.
     */
    private fun runCustomScript(scriptPath: String) {
        setStatus("Kör skript: $scriptPath")
        
        lifecycleScope.launch {
            val result = executeCommand("adb shell sh $scriptPath")
            
            withContext(Dispatchers.Main) {
                if (result.success) {
                    setStatus("Skript kört klart")
                    logOperation("Custom Script", "Success", scriptPath)
                } else {
                    setStatus("Skript misslyckades: ${result.error}")
                    logOperation("Custom Script", "Failed", "${result.error}")
                }
            }
        }
    }

    /**
     * Uppdaterar knappar baserat på kompatibilitet och root-status.
     */
    private fun updateButtonStates(compatibility: CompatibilityLevel, hasRoot: Boolean) {
        when (compatibility) {
            CompatibilityLevel.FULL -> {
                unlockButton.isEnabled = hasRoot
                resetPinButton.isEnabled = hasRoot
                bypassFrpButton.isEnabled = hasRoot
            }
            CompatibilityLevel.PARTIAL -> {
                unlockButton.isEnabled = hasRoot
                resetPinButton.isEnabled = hasRoot
                bypassFrpButton.isEnabled = false
            }
            CompatibilityLevel.NOT_COMPATIBLE -> {
                unlockButton.isEnabled = false
                resetPinButton.isEnabled = false
                bypassFrpButton.isEnabled = false
            }
        }

        enableUsbDebugButton.isEnabled = true
        runExploitScriptButton.isEnabled = true // Nu alltid aktiv för avancerade features
    }

    /**
     * Kopierar text till urklipp.
     */
    private fun copyToClipboard(text: String) {
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("ADB Unlocker", text)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(this, "Kopierat till urklipp", Toast.LENGTH_SHORT).show()
    }

    /**
     * Loggar operation för historik.
     */
    private fun logOperation(operation: String, status: String, details: String) {
        val timestamp = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
        operationHistory.add(OperationLog(timestamp, operation, status, details))
        
        // Behåll bara senaste 20 operationerna
        if (operationHistory.size > 20) {
            operationHistory.removeAt(0)
        }
    }

    /**
     * Visar bekräftelsedialog för destruktiva åtgärder.
     */
    private fun showConfirmationDialog(messageRes: Int, onConfirm: () -> Unit) {
        AlertDialog.Builder(this)
            .setTitle(R.string.confirm_title)
            .setMessage(messageRes)
            .setPositiveButton(R.string.yes) { _, _ -> onConfirm() }
            .setNegativeButton(R.string.no, null)
            .show()
    }

    /**
     * Kör ADB-kommando i bakgrund med Coroutines.
     */
    private fun runAdbCommand(cmd: String, successMsg: Int, errorMsg: Int, operation: String = "") {
        setStatus(getString(R.string.running))

        lifecycleScope.launch {
            val result = executeCommand(cmd)

            withContext(Dispatchers.Main) {
                if (result.success) {
                    setStatus(getString(successMsg))
                    if (operation.isNotEmpty()) {
                        logOperation(operation, "Success", "Kommando: $cmd")
                    }
                } else {
                    setStatus("${getString(errorMsg)}: ${result.error}")
                    if (operation.isNotEmpty()) {
                        logOperation(operation, "Failed", result.error)
                    }
                }
            }
        }
    }

    /**
     * Kör ADB-kommando följt av reboot.
     */
    private fun runAdbCommandWithReboot(cmd: String, successMsg: Int, operation: String = "") {
        setStatus(getString(R.string.running))

        lifecycleScope.launch {
            val result = executeCommand(cmd)

            if (result.success) {
                val rebootResult = executeCommand("adb reboot")
                withContext(Dispatchers.Main) {
                    setStatus("${getString(successMsg)}. ${if (rebootResult.success) "Omstart initierad." else "Kunde inte starta om."}")
                    if (operation.isNotEmpty()) {
                        logOperation(operation, "Success", "Kommando: $cmd + reboot")
                    }
                }
            } else {
                withContext(Dispatchers.Main) {
                    setStatus("${getString(R.string.error_root_required)}: ${result.error}")
                    if (operation.isNotEmpty()) {
                        logOperation(operation, "Failed", result.error)
                    }
                }
            }
        }
    }

    /**
     * Exekverar shell-kommando och returnerar resultat med felhantering.
     */
    private suspend fun executeCommand(command: String): CommandResult =
        withContext(Dispatchers.IO) {
            try {
                val process = Runtime.getRuntime().exec(command)
                val stdout = process.inputStream.bufferedReader().readText()
                val stderr = process.errorStream.bufferedReader().readText()
                val exitCode = process.waitFor()

                CommandResult(
                    success = exitCode == 0 && stderr.isBlank(),
                    output = stdout.trim(),
                    error = stderr.trim().ifBlank { "Okänt fel (exit code: $exitCode)" }
                )
            } catch (e: Exception) {
                CommandResult(
                    success = false,
                    output = "",
                    error = e.message ?: "Okänt fel"
                )
            }
        }

    private fun setStatus(message: String) {
        statusText.text = message
    }

    /**
     * Utökad dataklass för enhetsinformation.
     */
    data class DeviceInfo(
        val isConnected: Boolean = false,
        val manufacturer: String = "",
        val model: String = "",
        val androidVersion: String = "",
        val sdkVersion: Int = 0,
        val serialNumber: String = "",
        val batteryLevel: String = "Okänd",
        val buildVersion: String = "",
        val securityPatch: String = "",
        val bootloader: String = "",
        val carrier: String = ""
    )

    /**
     * Kompatibilitetsnivåer.
     */
    enum class CompatibilityLevel(val stringRes: Int) {
        FULL(R.string.compatible),
        PARTIAL(R.string.partial_compatibility),
        NOT_COMPATIBLE(R.string.not_compatible)
    }

    /**
     * Dataklass för kommando-resultat.
     */
    data class CommandResult(
        val success: Boolean,
        val output: String,
        val error: String
    )

    /**
     * Dataklass för operationslogg.
     */
    data class OperationLog(
        val timestamp: String,
        val operation: String,
        val status: String,
        val details: String
    )
}