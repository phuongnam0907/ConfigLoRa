package com.example.phuongnam0907.configlora;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

import com.google.android.things.pio.Gpio;
import com.google.android.things.pio.PeripheralManager;
import com.google.android.things.pio.SpiDevice;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Skeleton of an Android Things activity.
 * <p>
 * Android Things peripheral APIs are accessible through the class
 * PeripheralManagerService. For example, the snippet below will open a GPIO pin and
 * set it to HIGH:
 *
 * <pre>{@code
 * PeripheralManagerService service = new PeripheralManagerService();
 * mLedGpio = service.openGpio("BCM6");
 * mLedGpio.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW);
 * mLedGpio.setValue(true);
 * }</pre>
 * <p>
 * For more complex peripherals, look for an existing user-space driver, or implement one if none
 * is available.
 *
 * @see <a href="https://github.com/androidthings/contrib-drivers#readme">https://github.com/androidthings/contrib-drivers#readme</a>
 */
public class MainActivity extends Activity {

    private SpiDevice mDevice;
    private static final String TAG = MainActivity.class.getSimpleName();
    private Gpio pinReset;  //BCM17 - pin 11
    private Gpio pinD0;     //BCM4  - pin 7
    private Gpio pinCSS;    //BCM25 - pin 22

    public static final byte REG_VERSION                = (byte) 0x42;
    public static final byte REG_FIFO_TX_BASE_ADDR      = (byte) 0x0E;
    public static final byte REG_FIFO_RX_BASE_ADDR      = (byte) 0x0F;
    public static final byte REG_LNA                    = (byte) 0x0C;
    public static final byte REG_MODEM_CONFIG_3         = (byte) 0x26;
    public static final byte REG_OP_MODE                = (byte) 0x01;
    public static final byte REG_FRF_MSB                = (byte) 0x06;
    public static final byte REG_FRF_MID                = (byte) 0x07;
    public static final byte REG_FRF_LSB                = (byte) 0x08;
    public static final byte REG_PA_CONFIG              = (byte) 0x09;
    public static final byte REG_PA_DAC                 = (byte) 0x4D;
    public static final byte REG_OCP                    = (byte) 0x0B;

    public static final byte MODE_LONG_RANGE_MODE       = (byte) 0x80;
    public static final byte MODE_SLEEP                 = (byte) 0x00;
    public static final byte MODE_STDBY                 = (byte) 0x01;
    public static final byte MODE_TX                    = (byte) 0x03;
    public static final byte MODE_RX_CONTINUOUS         = (byte) 0x05;
    public static final byte MODE_RX_SINGLE             = (byte) 0x06;
    public static final byte PA_BOOST                   = (byte) 0x80;

    public static final int PA_OUTPUT_RFO_PIN          = 0;
    public static final int PA_OUTPUT_PA_BOOST_PIN     = 1;

    private static final boolean LOW = false;
    private static final boolean HIGH = true;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        PeripheralManager manager = PeripheralManager.getInstance();
        Log.d(TAG,"List of Devices support SPI : "+ manager.getSpiBusList());
        try {
            pinReset = manager.openGpio("BCM17");
            pinD0 = manager.openGpio("BCM4");
            pinCSS = manager.openGpio("BCM25");
            Log.d(TAG,"Name: " + pinReset.getName());
            Log.d(TAG,"Name: " + pinD0.getName());
            Log.d(TAG,"Name: " + pinCSS.getName());

            pinReset.setDirection(Gpio.DIRECTION_OUT_INITIALLY_HIGH);
            pinD0.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW);
            pinCSS.setDirection(Gpio.DIRECTION_OUT_INITIALLY_HIGH);

            mDevice = manager.openSpiDevice("SPI0.1");
            Log.d(TAG,"Name: " + mDevice.getName());
            //configSPIDevice(mDevice);

            start();
            delay(1000);
            printRegisters();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

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

        if (mDevice != null) {
            try {
                mDevice.close();
                mDevice = null;
            } catch (IOException e) {
                Log.w(TAG, "Unable to close SPI device", e);
            }
        }

    }

    private void configSPIDevice(SpiDevice device) throws IOException {
        device.setMode(SpiDevice.MODE1);
        device.setFrequency(32000000); // 32MHz
        device.setBitJustification(SpiDevice.BIT_JUSTIFICATION_MSB_FIRST);
        device.setBitsPerWord(8);
        Log.d(TAG,"SPI OK now ....");
    }
    
    private int start() throws IOException {

        // perform reset
        digitalWrite(pinReset, LOW);
        delay(10);
        digitalWrite(pinReset, HIGH);
        delay(10);


        // start SPI
        //_spi->begin();
        configSPIDevice(mDevice);

        // check version
        /*byte version = readRegister(REG_VERSION);
        if (version != 0x12) {
            return 0;
        }*/

        // put in sleep mode
        sleep();
/*

        //Test Register
        writeRegister(REG_FIFO_TX_BASE_ADDR, (byte) 0xab);
        Log.d(TAG,"Test: " + Integer.toHexString(readRegister(REG_FIFO_TX_BASE_ADDR)& 0x000000FF));
        writeRegister(REG_FIFO_RX_BASE_ADDR, (byte) 0x89);
        Log.d(TAG,"Test: " + Integer.toHexString(readRegister(REG_FIFO_RX_BASE_ADDR)& 0x000000FF));
        writeRegister(REG_FIFO_TX_BASE_ADDR, (byte) 0x69);
        Log.d(TAG,"Test: " + Integer.toHexString(readRegister(REG_FIFO_TX_BASE_ADDR)& 0x000000FF));
*/

        // set frequency
        setFrequency(434000000);

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

        return 1;
    }

    private  void digitalWrite(Gpio gpio, boolean value){
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
            mDevice.transfer(data,respone,data.length);
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

    private void setTxPower(int level, int outputPin) {
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

    private void idle(){
        writeRegister(REG_OP_MODE, (byte) (MODE_LONG_RANGE_MODE | MODE_STDBY));
       // Log.d(TAG,"MODE 0x01: 0x" + Integer.toHexString(readRegister(REG_OP_MODE)));
    }

    private void sleep(){
        writeRegister(REG_OP_MODE, (byte) (MODE_LONG_RANGE_MODE | MODE_SLEEP));
        //Log.d(TAG,"MODE 0x01: 0x" + Integer.toHexString(readRegister(REG_OP_MODE)));
    }

    private void printRegisters()
    {
        for (int i = 0; i < 128; i++) {
            Log.d(TAG,"0x"+ Integer.toHexString(i) +": 0x" + Integer.toHexString(readRegister((byte) i)& 0x000000FF));
        }
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
     *                           NEW CODE
     *
     ***************************************************************/
/*
int LoRaClass::beginPacket(int implicitHeader)
{
  if (isTransmitting()) {
    return 0;
  }

  // put in standby mode
  idle();

  if (implicitHeader) {
    implicitHeaderMode();
  } else {
    explicitHeaderMode();
  }

  // reset FIFO address and paload length
  writeRegister(REG_FIFO_ADDR_PTR, 0);
  writeRegister(REG_PAYLOAD_LENGTH, 0);

  return 1;
}

int LoRaClass::endPacket(bool async)
{
  // put in TX mode
  writeRegister(REG_OP_MODE, MODE_LONG_RANGE_MODE | MODE_TX);

  if (async) {
    // grace time is required for the radio
    delayMicroseconds(150);
  } else {
    // wait for TX done
    while ((readRegister(REG_IRQ_FLAGS) & IRQ_TX_DONE_MASK) == 0) {
      yield();
    }
    // clear IRQ's
    writeRegister(REG_IRQ_FLAGS, IRQ_TX_DONE_MASK);
  }

  return 1;
}

bool LoRaClass::isTransmitting()
{
  if ((readRegister(REG_OP_MODE) & MODE_TX) == MODE_TX) {
    return true;
  }

  if (readRegister(REG_IRQ_FLAGS) & IRQ_TX_DONE_MASK) {
    // clear IRQ's
    writeRegister(REG_IRQ_FLAGS, IRQ_TX_DONE_MASK);
  }

  return false;
}

int LoRaClass::parsePacket(int size)
{
  int packetLength = 0;
  int irqFlags = readRegister(REG_IRQ_FLAGS);

  if (size > 0) {
    implicitHeaderMode();

    writeRegister(REG_PAYLOAD_LENGTH, size & 0xff);
  } else {
    explicitHeaderMode();
  }

  // clear IRQ's
  writeRegister(REG_IRQ_FLAGS, irqFlags);

  if ((irqFlags & IRQ_RX_DONE_MASK) && (irqFlags & IRQ_PAYLOAD_CRC_ERROR_MASK) == 0) {
    // received a packet
    _packetIndex = 0;

    // read packet length
    if (_implicitHeaderMode) {
      packetLength = readRegister(REG_PAYLOAD_LENGTH);
    } else {
      packetLength = readRegister(REG_RX_NB_BYTES);
    }

    // set FIFO address to current RX address
    writeRegister(REG_FIFO_ADDR_PTR, readRegister(REG_FIFO_RX_CURRENT_ADDR));

    // put in standby mode
    idle();
  } else if (readRegister(REG_OP_MODE) != (MODE_LONG_RANGE_MODE | MODE_RX_SINGLE)) {
    // not currently in RX mode

    // reset FIFO address
    writeRegister(REG_FIFO_ADDR_PTR, 0);

    // put in single RX mode
    writeRegister(REG_OP_MODE, MODE_LONG_RANGE_MODE | MODE_RX_SINGLE);
  }

  return packetLength;
}

int LoRaClass::packetRssi()
{
  return (readRegister(REG_PKT_RSSI_VALUE) - (_frequency < 868E6 ? 164 : 157));
}

float LoRaClass::packetSnr()
{
  return ((int8_t)readRegister(REG_PKT_SNR_VALUE)) * 0.25;
}

long LoRaClass::packetFrequencyError()
{
  int32_t freqError = 0;
  freqError = static_cast<int32_t>(readRegister(REG_FREQ_ERROR_MSB) & B111);
  freqError <<= 8L;
  freqError += static_cast<int32_t>(readRegister(REG_FREQ_ERROR_MID));
  freqError <<= 8L;
  freqError += static_cast<int32_t>(readRegister(REG_FREQ_ERROR_LSB));

  if (readRegister(REG_FREQ_ERROR_MSB) & B1000) { // Sign bit is on
     freqError -= 524288; // B1000'0000'0000'0000'0000
  }

  const float fXtal = 32E6; // FXOSC: crystal oscillator (XTAL) frequency (2.5. Chip Specification, p. 14)
  const float fError = ((static_cast<float>(freqError) * (1L << 24)) / fXtal) * (getSignalBandwidth() / 500000.0f); // p. 37

  return static_cast<long>(fError);
}

size_t LoRaClass::write(uint8_t byte)
{
  return write(&byte, sizeof(byte));
}

size_t LoRaClass::write(const uint8_t *buffer, size_t size)
{
  int currentLength = readRegister(REG_PAYLOAD_LENGTH);

  // check size
  if ((currentLength + size) > MAX_PKT_LENGTH) {
    size = MAX_PKT_LENGTH - currentLength;
  }

  // write data
  for (size_t i = 0; i < size; i++) {
    writeRegister(REG_FIFO, buffer[i]);
  }

  // update length
  writeRegister(REG_PAYLOAD_LENGTH, currentLength + size);

  return size;
}

int LoRaClass::available()
{
  return (readRegister(REG_RX_NB_BYTES) - _packetIndex);
}

int LoRaClass::read()
{
  if (!available()) {
    return -1;
  }

  _packetIndex++;

  return readRegister(REG_FIFO);
}

int LoRaClass::peek()
{
  if (!available()) {
    return -1;
  }

  // store current FIFO address
  int currentAddress = readRegister(REG_FIFO_ADDR_PTR);

  // read
  uint8_t b = readRegister(REG_FIFO);

  // restore FIFO address
  writeRegister(REG_FIFO_ADDR_PTR, currentAddress);

  return b;
}

void LoRaClass::flush()
{
}
 */

}
