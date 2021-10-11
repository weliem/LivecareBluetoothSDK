package com.example.livecare.bluetoothsdk.initFunctions.bluetooth_connection.peripherals.prizma

import android.bluetooth.BluetoothGattCharacteristic
import android.os.Handler
import android.os.Looper
import com.example.livecare.bluetoothsdk.initFunctions.bluetooth_connection.BluetoothConnection
import com.example.livecare.bluetoothsdk.livecarebluetoothsdk.BleManager
import com.example.livecare.bluetoothsdk.livecarebluetoothsdk.callback.BleNotifyCallback
import com.example.livecare.bluetoothsdk.livecarebluetoothsdk.callback.BleWriteCallback
import com.example.livecare.bluetoothsdk.livecarebluetoothsdk.data.BleDevice
import com.example.livecare.bluetoothsdk.livecarebluetoothsdk.exception.BleException
import com.example.livecare.bluetoothsdk.livecarebluetoothsdk.utils.HexUtil

class PrizmaDevice(private var bluetoothConnection: BluetoothConnection) {
    private lateinit var bleDevice: BleDevice
    private var writeCommand = ""
    private val getDeviceInfoCommand = "AAAA01000000"
    private val getDeviceStatusCommand = "AAAA02000000"
    private val getDeviceInfoResponse = "aaaa0110"
    private val getTemperatureMeasurementStartCommand = "AAAA41020000"
    private val getECGMeasurementStartCommand = "AAAA11020000"
    private val getECGMeasurementStartResponse = "aaaa1112"
    private val getSpO2StartCommand = "AAAA31020000"
    private val getDeviceControlCommand = "AAAA030005000300010004"
    private val getDeviceBITCommand = "aaaa04000000"
    private val getStatusResponse = "aaaa0210"
    private val getTemperatureMeasurementCompletedResponse = "aaaa4412"
    private val getSpO2MeasurementCompletedResponse = "aaaa3412"
    private val getSpO2MeasurementProgressResponse = "aaaa3312"
    private val getECGMeasurementProgressResponse = "aaaa1312"
    private val getECGMeasurementCompletedResponse = "aaaa1412"
    private lateinit var characteristic: BluetoothGattCharacteristic
    private val stringBuilder = StringBuilder()
    private var mcuSn = ""
    private var fwVersion = ""
    private var hwVersion = ""
    private var ecgLsbWeight = ""
    private var batteryLevel = 100


    fun onConnectedSuccess(bleDevice: BleDevice) {
        this.bleDevice = bleDevice
        val gatt = BleManager.getInstance().getBluetoothGatt(bleDevice)
        if(gatt!=null){
            for (service in gatt.services) {
                if (service.uuid.toString() == "49535343-fe7d-4ae5-8fa9-9fafd205e455") {
                    for (characteristic in service.characteristics) {
                        if (characteristic.uuid.toString() == "49535343-1e4d-4bd9-ba61-23c647249616") {
                            writeCommand = getDeviceInfoCommand
                            this.characteristic = characteristic
                            startNotify(characteristic)
                        }
                    }
                }
            }
        }else{
            finishActivity()
        }
    }

    private fun startNotify(characteristic: BluetoothGattCharacteristic) {
        BleManager.getInstance().notify(
                bleDevice,
                characteristic.service.uuid.toString(),
                characteristic.uuid.toString(),
                object : BleNotifyCallback() {
                    override fun onNotifySuccess() {
                        if (writeCommand == getDeviceInfoCommand) {
                            writeCommand = ""
                            Handler(Looper.myLooper()!!).postDelayed({
                                startWrite(characteristic, getDeviceInfoCommand)
                            }, 5000)
                        }
                    }

                    override fun onNotifyFailure(exception: BleException) {}
                    override fun onCharacteristicChanged(data: ByteArray) {
                       /* when {
                            HexUtil.formatHexString(characteristic.value).contains(getDeviceInfoResponse) -> {
                                mcuSn = HexUtil.formatHexString(data).substring(20, 44)
                                fwVersion = HexUtil.formatHexString(data).substring(52, 60)
                                hwVersion = HexUtil.formatHexString(data).substring(68, 76)
                                startWrite(characteristic, getDeviceStatusCommand)
                            }
                            HexUtil.formatHexString(characteristic.value).contains(getECGMeasurementStartResponse) -> {

                                if(HexUtil.formatHexString(data).length > 40){
                                    ecgLsbWeight = HexUtil.formatHexString(data).substring(32, 40)
                                } else {
                                    finishActivity()
                                }
                            }
                            HexUtil.formatHexString(characteristic.value).contains(getTemperatureMeasurementCompletedResponse) -> {
                                sendTempToFirebase(getUsableString(HexUtil.formatHexString(characteristic.value), getTemperatureMeasurementCompletedResponse))
                            }
                            HexUtil.formatHexString(characteristic.value).contains(getSpO2MeasurementCompletedResponse) -> {
                                sendSpO2ToFirebase(getUsableString(HexUtil.formatHexString(characteristic.value), getSpO2MeasurementCompletedResponse))
                            }
                            HexUtil.formatHexString(characteristic.value).contains(getSpO2MeasurementProgressResponse) -> {
                                showHeartRate(getUsableString(HexUtil.formatHexString(characteristic.value), getSpO2MeasurementProgressResponse), TypeBleDevices.SpO2.stringValue)
                            }
                            HexUtil.formatHexString(characteristic.value).contains(getECGMeasurementProgressResponse) -> {
                                stringBuilder.append(HexUtil.formatHexString(characteristic.value))
                                showHeartRate(getUsableString(HexUtil.formatHexString(characteristic.value), getECGMeasurementProgressResponse), TypeBleDevices.ECG.stringValue)
                            }
                            HexUtil.formatHexString(characteristic.value).contains(getECGMeasurementCompletedResponse) -> {
                                sendECGToFirebase(getUsableString(HexUtil.formatHexString(characteristic.value), getECGMeasurementCompletedResponse))
                            }
                            HexUtil.formatHexString(characteristic.value).contains("aaaa001f") -> {
                                Timber.tag(TAG).d("ERROR!!!!!!!")
                            }
                            HexUtil.formatHexString(characteristic.value).contains("aaaaff10") -> {
                                Timber.tag(TAG).d("Unknown command!!!!!!!")
                            }
                        }
                        if (HexUtil.formatHexString(characteristic.value).length > 8 && HexUtil.formatHexString(characteristic.value).substring(0, 8).equals(getStatusResponse, ignoreCase = true)) {
                            batteryLevel = HexUtil.formatHexString(data).substring(40, 42).toInt(16)
                            if (HexUtil.formatHexString(data).substring(40, 42).toInt(16) <= 10){
                                textViewConnectionState.text = java.lang.String.format("Low Battery, Please charge your Prizma Device")
                                updateUI(false)
                            }
                        }*/
                    }
                })
    }

    private fun startWrite(characteristic: BluetoothGattCharacteristic, command: String) {
        BleManager.getInstance().write(
                bleDevice,
                characteristic.service.uuid.toString(),
                characteristic.uuid.toString(),
                HexUtil.hexStringToBytes(command),
                object : BleWriteCallback() {
                    override fun onWriteSuccess(current: Int, total: Int, justWrite: ByteArray) {

                    }

                    override fun onWriteFailure(exception: BleException) {

                    }
                })
    }

    private fun finishActivity() {
        BleManager.getInstance().disconnect(bleDevice)
    }

}