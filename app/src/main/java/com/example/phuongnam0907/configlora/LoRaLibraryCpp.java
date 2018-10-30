package com.example.phuongnam0907.configlora;

import android.util.Log;

import com.google.android.things.pio.Gpio;
import com.google.android.things.pio.PeripheralManager;
import com.google.android.things.pio.SpiDevice;

import java.io.IOException;

public class LoRaLibraryCpp {

    private static final String TAG = "LoRa: ";

    public static final String GPIO_RESET   = "BCM17";  //Pin 11
    public static final String GPIO_CSS     = "BCM25";  //Pin 22
    public static final String GPIO_DIO0    = "BCM4";   //Pin 7

    private SpiDevice spiDevice;
    private Gpio pinReset;  //BCM17 - pin 11
    private Gpio pinD0;     //BCM4  - pin 7
    private Gpio pinCSS;    //BCM25 - pin 22

    // registers
    public static final byte REG_FIFO                   = (byte) 0x00;
    public static final byte REG_OP_MODE                = (byte) 0x01;
    public static final byte REG_FRF_MSB                = (byte) 0x06;
    public static final byte REG_FRF_MID                = (byte) 0x07;
    public static final byte REG_FRF_LSB                = (byte) 0x08;
    public static final byte REG_PA_CONFIG              = (byte) 0x09;
    public static final byte REG_OCP                    = (byte) 0x0b;
    public static final byte REG_LNA                    = (byte) 0x0c;
    public static final byte REG_FIFO_ADDR_PTR          = (byte) 0x0d;
    public static final byte REG_FIFO_TX_BASE_ADDR      = (byte) 0x0e;
    public static final byte REG_FIFO_RX_BASE_ADDR      = (byte) 0x0f;
    public static final byte REG_FIFO_RX_CURRENT_ADDR   = (byte) 0x10;
    public static final byte REG_IRQ_FLAGS              = (byte) 0x12;
    public static final byte REG_RX_NB_BYTES            = (byte) 0x13;
    public static final byte REG_PKT_SNR_VALUE          = (byte) 0x19;
    public static final byte REG_PKT_RSSI_VALUE         = (byte) 0x1a;
    public static final byte REG_MODEM_CONFIG_1         = (byte) 0x1d;
    public static final byte REG_MODEM_CONFIG_2         = (byte) 0x1e;
    public static final byte REG_PREAMBLE_MSB           = (byte) 0x20;
    public static final byte REG_PREAMBLE_LSB           = (byte) 0x21;
    public static final byte REG_PAYLOAD_LENGTH         = (byte) 0x22;
    public static final byte REG_MODEM_CONFIG_3         = (byte) 0x26;
    public static final byte REG_FREQ_ERROR_MSB         = (byte) 0x28;
    public static final byte REG_FREQ_ERROR_MID         = (byte) 0x29;
    public static final byte REG_FREQ_ERROR_LSB         = (byte) 0x2a;
    public static final byte REG_RSSI_WIDEBAND          = (byte) 0x2c;
    public static final byte REG_DETECTION_OPTIMIZE     = (byte) 0x31;
    public static final byte REG_INVERTIQ               = (byte) 0x33;
    public static final byte REG_DETECTION_THRESHOLD    = (byte) 0x37;
    public static final byte REG_SYNC_WORD              = (byte) 0x39;
    public static final byte REG_INVERTIQ2              = (byte) 0x3b;
    public static final byte REG_DIO_MAPPING_1          = (byte) 0x40;
    public static final byte REG_VERSION                = (byte) 0x42;
    public static final byte REG_PA_DAC                 = (byte) 0x4d;

    // modes
    public static final byte MODE_LONG_RANGE_MODE       = (byte) 0x80;
    public static final byte MODE_SLEEP                 = (byte) 0x00;
    public static final byte MODE_STDBY                 = (byte) 0x01;
    public static final byte MODE_TX                    = (byte) 0x03;
    public static final byte MODE_RX_CONTINUOUS         = (byte) 0x05;
    public static final byte MODE_RX_SINGLE             = (byte) 0x06;

    // PA config
    public static final byte PA_BOOST                   = (byte) 0x80;

    // IRQ masks
    public static final byte IRQ_TX_DONE_MASK           = (byte) 0x08;
    public static final byte IRQ_PAYLOAD_CRC_ERROR_MASK = (byte) 0x20;
    public static final byte IRQ_RX_DONE_MASK           = (byte) 0x40;

    public static final int MAX_PKT_LENGTH              = 255;
    public static final int PA_OUTPUT_RFO_PIN           = 0;
    public static final int PA_OUTPUT_PA_BOOST_PIN      = 1;

    public static final boolean LOW                     = false;
    public static final boolean HIGH                    = true;

    /***************************************************************
     *
     *                           SET/GET DEVICE
     *
     ***************************************************************/

    public static String getTAG() {
        return TAG;
    }

    public static String getGpioReset() {
        return GPIO_RESET;
    }

    public static String getGpioCss() {
        return GPIO_CSS;
    }

    public static String getGpioDio0() {
        return GPIO_DIO0;
    }

    public SpiDevice getSpiDevice() {
        return spiDevice;
    }

    public void setSpiDevice(SpiDevice spiDevice) {
        this.spiDevice = spiDevice;
    }

    public Gpio getPinReset() {
        return pinReset;
    }

    public void setPinReset(Gpio pinReset) {
        this.pinReset = pinReset;
    }

    public Gpio getPinD0() {
        return pinD0;
    }

    public void setPinD0(Gpio pinD0) {
        this.pinD0 = pinD0;
    }

    public Gpio getPinCSS() {
        return pinCSS;
    }

    public void setPinCSS(Gpio pinCSS) {
        this.pinCSS = pinCSS;
    }

    /***************************************************************
     *
     *                           CREATE DEVICE
     *
     ***************************************************************/

    private void  init(){
        initSPI();
        initGPIO();
    }

    private void initSPI(){
        PeripheralManager manager = PeripheralManager.getInstance();
        try {
            spiDevice = manager.openSpiDevice("SPI0.1");
            Log.d(TAG,"Name: " + spiDevice.getName());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void initGPIO(){
        PeripheralManager manager = PeripheralManager.getInstance();
        try {
            pinReset = manager.openGpio(GPIO_RESET);
            pinD0 = manager.openGpio(GPIO_DIO0);
            pinCSS = manager.openGpio(GPIO_CSS);
            Log.d(TAG,"Name: " + pinReset.getName());
            Log.d(TAG,"Name: " + pinD0.getName());
            Log.d(TAG,"Name: " + pinCSS.getName());

            pinReset.setDirection(Gpio.DIRECTION_OUT_INITIALLY_HIGH);
            pinD0.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW);
            pinCSS.setDirection(Gpio.DIRECTION_OUT_INITIALLY_HIGH);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /***************************************************************
     *
     *                          CLOSE DEVICE
     *
     ***************************************************************/
    private void close(){
        closeGPIO();
        closeSPI();
    }

    private void closeGPIO(){
        if (pinReset != null) {
            try {
                pinReset.close();
                pinReset = null;
            } catch (IOException e) {
                Log.w(TAG, "Unable to close GPIO", e);
            }
        }

        if (pinD0 != null) {
            try {
                pinD0.close();
                pinD0 = null;
            } catch (IOException e) {
                Log.w(TAG, "Unable to close GPIO", e);
            }
        }

        if (pinCSS != null) {
            try {
                pinCSS.close();
                pinCSS = null;
            } catch (IOException e) {
                Log.w(TAG, "Unable to close GPIO", e);
            }
        }
    }

    private void closeSPI(){
        if (spiDevice != null) {
            try {
                spiDevice.close();
                spiDevice = null;
            } catch (IOException e) {
                Log.w(TAG, "Unable to close SPI device", e);
            }
        }
    }

    /***************************************************************
     *
     *                          START + CONFIG
     *
     ***************************************************************/

    private int start(long frequency) throws IOException {

        init();

        // perform reset
        digitalWrite(pinReset, LOW);
        delay(10);
        digitalWrite(pinReset, HIGH);
        delay(10);


        // start SPI
        //_spi->begin();
        configSPIDevice(spiDevice);

        // check version
        /*byte version = readRegister(REG_VERSION);
        if (version != 0x12) {
            return 0;
        }*/

        // put in sleep mode
        sleep();

        // set frequency
        setFrequency(frequency);

        // set base addresses
        writeRegister(REG_FIFO_TX_BASE_ADDR, (byte) 0);
        writeRegister(REG_FIFO_RX_BASE_ADDR, (byte) 0);

        // set LNA boost
        writeRegister(REG_LNA, (byte) (readRegister(REG_LNA) | 0x03));

        // set auto AGC
        writeRegister(REG_MODEM_CONFIG_3, (byte) 0x04);

        // set output power to 17 dBm
        setTxPower(17, PA_OUTPUT_PA_BOOST_PIN);

        // put in standby mode
        idle();

        delay(1000);

        return 1;
    }

    private void configSPIDevice(SpiDevice device) throws IOException {
        device.setMode(SpiDevice.MODE1);
        device.setFrequency(32000000); // 32MHz
        device.setBitJustification(SpiDevice.BIT_JUSTIFICATION_MSB_FIRST);
        device.setBitsPerWord(8);
        Log.d(TAG,"SPI OK now ....");
    }

    /***************************************************************
     *
     *                  BASIC FUNCTION LIKE ARDUINO
     *
     ***************************************************************/

    private void digitalWrite(Gpio gpio, boolean value){
        try {
            gpio.setValue(value);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void delay(long micro){
        try {
            Thread.sleep(micro);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /***************************************************************
     *
     *               FUNCTION PROCESS REGISTER OF LORA
     *
     ***************************************************************/

    private void writeRegister(byte address, byte value) {
        singleTransfer((byte) (address | 0x80), value);
    }

    private byte readRegister(byte address) {
        return singleTransfer((byte) (address & 0x7f), (byte) 0x00);
    }

    private byte singleTransfer(byte address, byte value) {
        byte[] respone = new byte[2];
        byte[] data = new byte[2];
        data[0] = address;
        data[1] = value;
        try {
            pinCSS.setValue(false);
            spiDevice.transfer(data,respone,data.length);
            pinCSS.setValue(true);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return respone[1];
    }

    private void setFrequency(long frequency) {
        long frf = (frequency << 19) / 32000000;

        writeRegister(REG_FRF_MSB, (byte) (frf >> 16));
        writeRegister(REG_FRF_MID, (byte) (frf >> 8));
        writeRegister(REG_FRF_LSB, (byte) (frf >> 0));
    }

    private void idle(){
        writeRegister(REG_OP_MODE, (byte) (MODE_LONG_RANGE_MODE | MODE_STDBY));
    }

    private void sleep(){
        writeRegister(REG_OP_MODE, (byte) (MODE_LONG_RANGE_MODE | MODE_SLEEP));
    }

    private void setOCP(byte mA)
    {
        byte ocpTrim = 27;
        if (mA <= 120) {
            ocpTrim = (byte) ((mA - 45) / 5);
        } else if (mA <=240) {
            ocpTrim = (byte) ((mA + 30) / 10);
        }
        writeRegister(REG_OCP, (byte) (0x20 | (0x1F & ocpTrim)));
    }
    /***************************************************************
     *
     *                  PUBLIC FUNCTION FOR USER
     *
     ***************************************************************/
    public void begin(){
        try {
            start(434000000);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void end(){
        close();
    }

    public void printRegisters()
    {
        for (int i = 0; i < 128; i++) {
            Log.d(TAG,"0x"+ Integer.toHexString(i) +": 0x" + Integer.toHexString(readRegister((byte) i)& 0x000000FF));
        }
    }

    public void setTxPower(int level, int outputPin) {
        if (PA_OUTPUT_RFO_PIN == outputPin) {
            // RFO
            if (level < 0) {
                level = 0;
            } else if (level > 14) {
                level = 14;
            }

            writeRegister(REG_PA_CONFIG, (byte) (0x70 | level));
        } else {
            // PA BOOST
            if (level > 17) {
                if (level > 20) {
                    level = 20;
                }

                // subtract 3 from level, so 18 - 20 maps to 15 - 17
                level -= 3;

                // High Power +20 dBm Operation (Semtech SX1276/77/78/79 5.4.3.)
                writeRegister(REG_PA_DAC, (byte) 0x87);
                setOCP((byte) 140);
            } else {
                if (level < 2) {
                    level = 2;
                }
                //Default value PA_HF/LF or +17dBm
                writeRegister(REG_PA_DAC, (byte) 0x84);
                setOCP((byte) 100);
            }

            writeRegister(REG_PA_CONFIG, (byte) (PA_BOOST | (level - 2)));
        }
    }
}