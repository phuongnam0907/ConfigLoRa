package com.example.phuongnam0907.configlora;

import android.os.Handler;
import android.util.Log;

import com.google.android.things.pio.Gpio;
import com.google.android.things.pio.PeripheralManager;

import java.io.IOException;

public class LoRaLibraryCpp {

    private static final String TAG = "LoRa Library";

    // REGISTER
    public static final byte REG_FIFO                    = (byte) 0x00;
    public static final byte REG_OPMODE                  = (byte) 0x01;
    public static final byte REG_FREQ23_16				 = (byte) 0x06;
    public static final byte REG_FREQ15_8				 = (byte) 0x07;
    public static final byte REG_FREQ7_0			     = (byte) 0x08;
    public static final byte REG_PA_CONFIG               = (byte) 0x09;	// POWER AMPLIFIER CONFIG
    public static final byte REG_PA_RAMP				 = (byte) 0x0A;
    public static final byte REG_OCP					 = (byte) 0x0B;	//Over load current protection
    public static final byte REG_FIFO_ADDR_PTR           = (byte) 0x0D;
    public static final byte REG_FIFO_TX_BASE_ADDR       = (byte) 0x0E;
    public static final byte REG_FIFO_RX_BASE_ADDR       = (byte) 0x0F;
    public static final byte REG_FIFO_RX_CURRENT_ADDR    = (byte) 0x10;
    public static final byte REG_IRQ_FLAGS_MASK          = (byte) 0x11;
    public static final byte REG_IRQ_FLAGS               = (byte) 0x12;
    public static final byte REG_RX_NB_BYTES             = (byte) 0x13;
    public static final byte REG_MODEM_STAT              = (byte) 0x18;
    public static final byte REG_MODEM_CONFIG1           = (byte) 0x1D;	//Bandwidth &Coding rate
    public static final byte REG_MODEM_CONFIG2           = (byte) 0x1E;	//SpreadingFactor
    public static final byte REG_RX_TIME_OUT			 = (byte) 0x1F;
    public static final byte REG_PREAMBLE_LENGTH_H		 = (byte) 0x20;
    public static final byte REG_PREAMBLE_LENGTH_L		 = (byte) 0x21;
    public static final byte REG_PAYLOAD_LENGTH          = (byte) 0x22;
    public static final byte REG_HOP_PERIOD              = (byte) 0x24;
    public static final byte REG_DIO_MAPPING_1           = (byte) 0x40;
    public static final byte REG_DIO_MAPPING_2           = (byte) 0x41;
    public static final byte REG_LR_PADAC				 = (byte) 0x4D;
    // MODES
    public static final byte LORA_SLEEP             	 = (byte) 0x08;
    public static final byte LORA_STANDBY            	 = (byte) 0x09;
    public static final byte LORA_TX				     = (byte) 0x8B;
    public static final byte LORA_RX_CONTINUOS      	 = (byte) 0x8D;
    // LOW NOISE AMPLIFIER
    public static final byte REG_LNA                     = (byte) 0x0C;
    public static final byte LNA_MAX_GAIN                = (byte) 0x23; // 0010 0011
    public static final byte LNA_OFF_GAIN                = (byte) 0x00;
    public static final byte CR							 = (byte) 1;	//1=>4/5,2=>4/6,3=>4/7,4=>4/8
    public static final byte Lora_Rate_Sel				 = (byte) 6;
    public static final byte BandWidth_Sel				 = (byte) 7;
    // INT
    //public static final byte u8 unsigned byte

    public byte[] Freq433Table 			    = new byte[] {(byte)0x85,(byte)0x3b,(byte)0x13};	//433Mhz
    public byte[] sx1278PowerTable		    = new byte[] {(byte)0xF0,(byte)0xF1,(byte)0xF2,(byte)0xF3,(byte)0xF4,(byte)0xF5,(byte)0xF6,(byte)0xF7,(byte)0xF8,(byte)0xF9,(byte)0xFA,(byte)0xFB,(byte)0xFC,(byte)0xFD,(byte)0xFE,(byte)0xFF};    //FF=20dbm,FC=17dbm,F9=14dbm,F6=11dbm
    public byte[] sx1278SpreadFactorTable   = new byte[] {(byte)6,(byte)7,(byte)8,(byte)9,(byte)10,(byte)11,(byte)12};	//64/128/256/512/1024/2048/4096 chips/symble
    public byte[] sx1278LoRaBwTable 		= new byte[] {(byte)0,(byte)1,(byte)2,(byte)3,(byte)4,(byte)5,(byte)6,(byte)7,(byte)8,(byte)9};	//7.8KHz,10.4KHz,15.6KHz,20.8KHz,31.2KHz,41.7KHz,62.5KHz,125KHz,250KHz,500KHz

    // For Arduino 1.0 and earlier
    /*
    #if defined(ARDUINO) && ARDUINO >= 100
    #include "Arduino.h"
    #else
    #include "WProgram.h"
    #endif
    */

    private static final boolean LOW = false;
    private static final boolean HIGH = true;
    private static final boolean INPUT = false;
    private static final boolean OUTPUT = true;
    private Gpio  m_SPI_CS_PIN; //15
    private Gpio  m_SPI_MISO_PIN; //12
    private Gpio  m_SPI_MOSI_PIN; //13
    private Gpio  m_SPI_SCK_PIN;  //14
    private Gpio  m_DIO0_PIN;   //16
    private Gpio  m_ANT_EN_PIN;   //4
    private Gpio  m_RESET_PIN;  //5

    byte m_TXLength;   //32
    byte m_RXLength;   //32
    byte[] m_TXData = new byte[128];
    byte[] m_RXData = new byte[128];

    private void digitalWrite(Gpio name, boolean value) {
        try {
            name.setValue(value);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private int digitalRead(Gpio name){
        int state = 0;
        try {
            if (name.getValue()) state = 1;
            else state = 0;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return state;
    }

    PeripheralManager manager = PeripheralManager.getInstance();
    private void regPin(Gpio pin, String namePin){
        if (namePin.indexOf("BCM") < 0) namePin = "BCM" + namePin;
        try {
            pin = manager.openGpio(namePin);
            Log.d(TAG,"Name: " + pin.getName());
        } catch (IOException e) {
            Log.e(TAG,"Cannot open the GPIO: " + namePin,e);
        }
    }

    private void delay(int milliseconds){       //**CHECK AGAIN!!!**
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void pinMode (Gpio port, boolean bool){
        if (bool == INPUT){
            try {
                port.setDirection(Gpio.DIRECTION_IN);
                port.setActiveType(Gpio.ACTIVE_HIGH);//**CHECK AGAIN!!!**
                // Register for all state changes
                port.setEdgeTriggerType(Gpio.EDGE_BOTH);
            } catch (IOException e){
                e.printStackTrace();
            }
        } else try {
            port.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    /**********************************************************
     **Name:     SPICmd8bit
     **Function: SPI Write one byte
     **Input:    WrPara
     **Output:   none
     **note:     use for burst mode
     **********************************************************/
    private void SPICmd8bit(byte WrPara)
    {
        byte bitcnt;
        for(bitcnt=8; bitcnt!=0; bitcnt--)
        {
            digitalWrite(m_SPI_SCK_PIN,LOW);
            if((byte)(WrPara&0x80) == 1)
                digitalWrite(m_SPI_MOSI_PIN,HIGH);
            else
                digitalWrite(m_SPI_MOSI_PIN,LOW);
            digitalWrite(m_SPI_SCK_PIN,HIGH);
            WrPara <<= 1;
        }
        digitalWrite(m_SPI_SCK_PIN,LOW);
    }

    /**********************************************************
     **Name:     SPIRead8bit
     **Function: SPI Read one byte
     **Input:    None
     **Output:   result byte
     **Note:     use for burst mode
     **********************************************************/
    private byte SPIRead8bit()
    {
        byte RdPara = 0;
        byte bitcnt;
        digitalWrite(m_SPI_CS_PIN,LOW);
        digitalWrite(m_SPI_MOSI_PIN,HIGH);                                                 //Read one byte data from FIFO, MOSI hold to High
        for(bitcnt=8; bitcnt!=0; bitcnt--)
        {
            digitalWrite(m_SPI_SCK_PIN,LOW);
            RdPara <<= 1;
            digitalWrite(m_SPI_SCK_PIN,HIGH);
            if(digitalRead(m_SPI_MISO_PIN) == 1)
               RdPara |= 0x01;
            else
               RdPara |= 0x00;
        }
        digitalWrite(m_SPI_SCK_PIN,LOW);
        return (RdPara);
    }

    /**********************************************************
     **Name:     SPIRead
     **Function: SPI Read CMD
     **Input:    adr -> address for read
     **Output:   None
     **********************************************************/
    private byte SPIRead(byte adr)
    {
        byte tmp;
        digitalWrite(m_SPI_SCK_PIN,LOW);
        digitalWrite(m_SPI_CS_PIN,LOW);
        SPICmd8bit((byte) (adr&0x7f));                                         //Send address first
        tmp = SPIRead8bit();
        digitalWrite(m_SPI_SCK_PIN,LOW);
        digitalWrite(m_SPI_CS_PIN,HIGH);
        return tmp;
    }

    /**********************************************************
     **Name:     SPIWrite
     **Function: SPI Write CMD
     **Input:     byte address &  byte data
     **Output:   None
     **********************************************************/
    private void SPIWrite( byte adr,  byte WrPara)
    {
        digitalWrite(m_SPI_SCK_PIN,LOW);
        digitalWrite(m_SPI_CS_PIN,LOW);
        SPICmd8bit((byte) (adr|(byte)0x80));
        SPICmd8bit(WrPara);
        digitalWrite(m_SPI_SCK_PIN,LOW);
        digitalWrite(m_SPI_CS_PIN,HIGH);
    }
    /**********************************************************
     **Name:     SPIBurstRead
     **Function: SPI burst read mode
     **Input:    adr-----address for read
     **          ptr-----data buffer point for read
     **          length--how many bytes for read
     **Output:   None
     **********************************************************/
    private void SPIBurstRead( byte adr, byte[] ptr,  byte length)
    {
        byte i;
        if(length<=1)                                            //length must more than one
            return;
        else
        {
            digitalWrite(m_SPI_SCK_PIN,LOW);
            digitalWrite(m_SPI_CS_PIN,LOW);
            SPICmd8bit(adr);
            for(i=0;i<length;i++)
                ptr[i] = SPIRead8bit();
            digitalWrite(m_SPI_SCK_PIN,LOW);
            digitalWrite(m_SPI_CS_PIN,HIGH);
        }
    }

    /**********************************************************
     **Name:     SPIBurstWrite
     **Function: SPI burst write mode
     **Input:    adr-----address for write
     **          ptr-----data buffer point for write
     **          length--how many bytes for write
     **Output:   none
     **********************************************************/
    private void SPIBurstWrite( byte adr,  byte[] ptr,  byte length)
    {
         byte i;
        if(length<=1)                                            //length must more than one
            return;
        else
        {
            digitalWrite(m_SPI_SCK_PIN,LOW);
            digitalWrite(m_SPI_CS_PIN,LOW);
            SPICmd8bit((byte) (adr|0x80));
            for(i=0;i<length;i++)
                SPICmd8bit(ptr[i]);
            digitalWrite(m_SPI_SCK_PIN,LOW);
            digitalWrite(m_SPI_CS_PIN,HIGH);
        }
    }

    //===========================
    public void Initial(String SPI_CS_PIN, String SPI_MISO_PIN, String SPI_MOSI_PIN, String SPI_SCK_PIN, String ANT_EN_PIN, String RESET_PIN, String DIO0_PIN)
    //===========================
    {
        regPin(m_SPI_CS_PIN,SPI_CS_PIN);
        regPin(m_SPI_MISO_PIN,SPI_MISO_PIN);
        regPin(m_SPI_MOSI_PIN,SPI_MOSI_PIN);
        regPin(m_SPI_SCK_PIN,SPI_SCK_PIN);
        regPin(m_ANT_EN_PIN,ANT_EN_PIN);
        regPin(m_RESET_PIN,RESET_PIN);
        regPin(m_DIO0_PIN,DIO0_PIN);
        // initialize the pins
        pinMode(m_SPI_CS_PIN, OUTPUT);
        pinMode(m_SPI_MOSI_PIN, OUTPUT);
        pinMode(m_SPI_MISO_PIN, INPUT);
        pinMode(m_SPI_SCK_PIN, OUTPUT);
        pinMode(m_ANT_EN_PIN, OUTPUT);
        pinMode(m_RESET_PIN, OUTPUT);
        pinMode(m_DIO0_PIN, INPUT);
        digitalWrite(m_DIO0_PIN, LOW);
        digitalWrite(m_RESET_PIN,LOW);
        delay(100);
        digitalWrite(m_RESET_PIN,HIGH);
        delay(100);
        //Serial.println("Setting Low Frequency LoRa Mode");
        SPIWrite(REG_OPMODE,(byte) 0x08);                    //Sleep//Low Frequency Mode
        delay(100);
        SPIWrite(REG_OPMODE,(byte) 0X88);                    //Set LoRa mode
        SPIWrite(REG_FREQ23_16,Freq433Table[0]);      //Set FR Frequency 433MHz
        SPIWrite(REG_FREQ15_8,Freq433Table[1]);
        SPIWrite(REG_FREQ7_0,Freq433Table[2]);
        SPIWrite(REG_PA_CONFIG,(byte) 0xF0);                 //PA low, 11dbm
        SPIWrite(REG_PA_RAMP,(byte) 0x09);                   //40uS
        SPIWrite(REG_OCP,(byte) 0x2B);                       //Over current protection, 100mA
        SPIWrite(REG_LNA,(byte) 0x20);                       //
        SPIWrite(REG_MODEM_CONFIG1, (byte) ((sx1278LoRaBwTable[BandWidth_Sel]<<4) + (CR<<1) + 0x00));   //
        SPIWrite(REG_MODEM_CONFIG2, (byte) ((sx1278SpreadFactorTable[Lora_Rate_Sel]<<4)+0x07));     //2048
        SPIWrite(REG_RX_TIME_OUT,(byte) 0xFF);
        SPIWrite(REG_PREAMBLE_LENGTH_H,(byte) 00);           //Preamble length 12
        SPIWrite(REG_PREAMBLE_LENGTH_L,(byte) 12);
        SPIWrite(REG_OPMODE,(byte) 0X09);                    //Standby
    }
    //===========================
    public void InitialSend(byte TX_Length)
    //===========================
    {
         byte addr=0;
        m_TXLength  = TX_Length;
        //Serial.print("Send: ");
        //Serial.println(buffer);
        digitalWrite(m_ANT_EN_PIN,HIGH);
        SPIWrite(REG_OPMODE,LORA_STANDBY);
        SPIWrite(REG_DIO_MAPPING_1,(byte) 0x41); 			 // Change the DIO0_PIN mapping to 01 so we can listen for TxDone on the interrupt
        SPIWrite(REG_DIO_MAPPING_2,(byte) 0x00);				 //DIO5=00 (ModeReady), DIO4=01(pllLock)
        SPIWrite(REG_PAYLOAD_LENGTH,m_TXLength);	 //TXData length
        SPIWrite(REG_LR_PADAC,(byte) 0X87);				     //Tx for 20dBm 0x87
        SPIWrite(REG_HOP_PERIOD,(byte) 0X00);			     //Disabled
        SPIWrite(REG_IRQ_FLAGS,(byte) 0xFF);				     //Clear Irq
        SPIWrite(REG_IRQ_FLAGS_MASK,(byte) 0xF7);	     //Open TxDone interrupt
        addr=SPIRead(REG_FIFO_TX_BASE_ADDR);
        SPIWrite(REG_FIFO_ADDR_PTR,addr);
        while(m_TXLength!=SPIRead(REG_PAYLOAD_LENGTH))
        {
        }
    }
    //===========================
    public void Send( byte[] buffer)
    //===========================
    {
         byte i;
        buffer[m_TXLength-1]=0;
        for(i=0;i<(m_TXLength-2);i++)
        {
            buffer[m_TXLength-1]^=buffer[i];
        }
        SPIBurstWrite((byte) 0,buffer,m_TXLength);
        SPIWrite(REG_OPMODE,LORA_TX);
        while(digitalRead(m_DIO0_PIN) == 0)			// once TxDone has flipped, everything has been sent
        {
            delay(100);
        }
        SPIRead(REG_IRQ_FLAGS);
        SPIWrite(REG_IRQ_FLAGS, (byte) 0xff); 		// clear the flags 0x08 is the TxDone flag
        SPIWrite(REG_OPMODE,LORA_STANDBY);
        digitalWrite(m_ANT_EN_PIN,LOW);
    }

    //===========================
    public void InitialReceive(byte RX_Length)
    //===========================
    {
         byte addr;
        //Serial.println("====Initial Receive====");
        m_RXLength  = RX_Length;
        digitalWrite(m_ANT_EN_PIN,LOW);
        SPIWrite(REG_LR_PADAC,(byte) 0X84);                    //Tx for 20dBm 0x84
        SPIWrite(REG_HOP_PERIOD,(byte) 0xFF);                 	//RegHopPeriod NO FHSS
        SPIWrite(REG_DIO_MAPPING_1,(byte) 0x01);            		//DIO0=00, DIO1=00, DIO2=00, DIO3=01 Valid header
        SPIWrite(REG_IRQ_FLAGS_MASK,(byte) 0x3F);             	//Open RxDone interrupt & Timeout
        SPIWrite(REG_IRQ_FLAGS,(byte) 0xFF);
        SPIWrite(REG_PAYLOAD_LENGTH,m_RXLength);    	 //RegPayloadLength  30byte(this register must difine when the data long of one byte in SF is 6)
        addr = SPIRead(REG_FIFO_RX_BASE_ADDR);         	//Read RxBaseAddr
        SPIWrite(REG_FIFO_ADDR_PTR,addr);             	//RxBaseAddr -> FiFoAddrPtr��
        SPIWrite(REG_OPMODE,(byte) 0x8d);                    	//Continuous Rx Mode//Low Frequency Mode
        while((SPIRead(REG_MODEM_STAT)&0x04)!=0x04)
        {
        }
    }
    //===========================
    private byte Receive( long Duration)
    //===========================
    {
        byte i,j=0,addr,packet_size;
        long timer,m_Duration;
        m_Duration  = Duration;
        //Serial.print("Waiting");
        for(timer=0;timer<m_Duration;timer++)
        {
            if(digitalRead(m_DIO0_PIN)==0)
            {
                delay(1);
                //Serial.print(".");
            }
            else
            {
                //Serial.println(" ");
                //Serial.print("Received data = ");
                for(i=0;i<m_RXLength;i++)
                {m_RXData[i] = 0x00;}
                addr = SPIRead(REG_FIFO_RX_CURRENT_ADDR);     //last packet addr
                SPIWrite(REG_FIFO_ADDR_PTR,addr);           //RxBaseAddr -> FiFoAddrPtr
                packet_size = SPIRead(REG_RX_NB_BYTES);     //Number for received bytes
                SPIBurstRead((byte) 0x00, m_RXData, packet_size);
                SPIWrite(REG_IRQ_FLAGS,(byte) 0xFF);
                j=0;
                for(i=0;i<m_RXLength;i++)
                {
                    j^=m_RXData[i];
                    //Serial.print(RXData[i],HEX);
                    //Serial.print(" ");
                }
            }
        }
        if(timer==m_Duration)
        {
            //Serial.print("Time out");
            //Serial.println(" ");
            return 0;
        }
        if(j!=0)
        {
            //Serial.print("CheckSum Error");
            //Serial.println(" ");
            return 0;
        }
    //Serial.println(" ");
        return 1;
    }

    //===========================
    public void DebugLoRa()
    //===========================
    {
        byte i,j;
        for(i=0;i<20;i++)
        {
            j=SPIRead(i);
            Log.d(TAG,Integer.toString(j) + " ");
        }
        for(i=21;i<40;i++)
        {
            j=SPIRead(i);
            Log.d(TAG,Integer.toString(j) + " ");
        }
        Log.d(TAG,"Set LoRa Mode has done");
    }
}