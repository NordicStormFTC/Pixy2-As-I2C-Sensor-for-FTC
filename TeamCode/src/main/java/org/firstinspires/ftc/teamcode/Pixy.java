package org.firstinspires.ftc.teamcode;

import androidx.annotation.NonNull;

import com.qualcomm.hardware.lynx.LynxI2cDeviceSynch;
import com.qualcomm.robotcore.hardware.I2cAddr;
import com.qualcomm.robotcore.hardware.I2cDeviceSynch;
import com.qualcomm.robotcore.hardware.I2cDeviceSynchDevice;
import com.qualcomm.robotcore.hardware.I2cDeviceSynchSimple;
import com.qualcomm.robotcore.hardware.I2cWaitControl;
import com.qualcomm.robotcore.hardware.configuration.annotations.DeviceProperties;
import com.qualcomm.robotcore.hardware.configuration.annotations.I2cDeviceType;

/**
 * Hello! This file was written by Alexander W Bur of team 5962 Nordic Storm on jan 20th 2025.
 * Please reach out! contact us at FTC-5962@saintpeterrobotics.org
 */
@I2cDeviceType
@DeviceProperties(name = "Pixy2", description = "Pixy2 Smart Camera", xmlTag = "MCP9808")
public class Pixy extends I2cDeviceSynchDevice<I2cDeviceSynchSimple> {

    /**
     this is the window of I2C data registers that we are going to read from.
     REV supports 26 read and 26 write registers.
     */
    private final I2cDeviceSynch.ReadWindow readWindow;

    /**
     if you wanted to track multiple blocks, you
     would make a list of blocks, and instead of "updateBlock()"
     you would have updateList(). This object is exposed through
     "getBlock()" which updates the block before handing it to the user.
     this means that "updateBlock()" does not need to be called
     in any loop, and "getBlock()" will never throw a null pointer error.
     */
    private PixyBlock pixyBlock;

    /**
     * if there are no valid results detected by pixy, we return
     * an empty block object to hold the space of a pixy block.
     * all of its fields are set to 0, and returns false
     * for "block.isValid()"
     */
    private final EmptyBlock emptyBlock;

    /**
     * this is how we determine if Pixy is sending us valid data
     */
    private final int syncWord = 0xc1af;

    /**
     * You should never directly call this constructor!
     * To instantiate Pixy, do it like you would any other sensor or motor...
     *  Pixy pixy = hardwareMap.get(Pixy.class, "device name here");
     */
    public Pixy(final I2cDeviceSynchSimple i2cDeviceSynchSimple, final boolean deviceClientIsOwned) {
        super(i2cDeviceSynchSimple, deviceClientIsOwned);

        final int DEFAULT_ADDRESS = 0x54;
        this.deviceClient.setI2cAddress(I2cAddr.create7bit(DEFAULT_ADDRESS));

        super.registerArmingStateCallback(false);

        readWindow = new I2cDeviceSynch.ReadWindow(0, 26, I2cDeviceSynch.ReadMode.REPEAT);

        this.engage();

        emptyBlock = new EmptyBlock();
    }


    /**
     * use me! If pixy has data of interest, we return a freshly updated PixyBlock object
     * detailing what the camera is tracking. If nothing is found, we return an "EmptyBlock" object
     * with all of its fields set to 0. This method will never throw a null pointer.
     *
     * @return a freshly updated pixyBlock
     */
    public PixyBlock getBlock() {
        if (isValidResult()) {
            return pixyBlock;
        } else {
            return emptyBlock;
        }
    }

    /**
     * Here we don't need to read anything back from the device,
     * just send it a request to turn the lamps on or off.
     */
    public void turnOnLamps() {
        final byte[] lampRequest = {(byte) 174, (byte) 193, (byte) 22, (byte) 2, (byte) 1, (byte) 1};

        deviceClient.write(0, lampRequest);
    }

    public void turnOffLamps() {
        final byte[] lampRequest = {(byte) 174, (byte) 193, (byte) 22, (byte) 2, (byte) 0, (byte) 0};

        deviceClient.write(0, lampRequest);
    }

    public int getFPS() {
        final byte[] fpsRequest = {(byte) 174, (byte) 193, (byte) 24, (byte) 0};

        deviceClient.write(0, fpsRequest);
        deviceClient.waitForWriteCompletions(I2cWaitControl.WRITTEN);

        /*
        the raw feedback we get from pixy
         */
        byte[] rawBytes = deviceClient.read(readWindow.getRegisterFirst(), readWindow.getRegisterCount());

        /*
        the FPS packet is returned at a 32-bit word, meaning we combine 4 bytes to get a readable value.
         */
        int lower = combineBytes(rawBytes[6], rawBytes[7]);
        int upper = combineBytes(rawBytes[8], rawBytes[9]);

        return combineBytes((byte) lower, (byte) upper);
    }

    /*
    in our case, the only results were interested in are those with a signature of 1 or 2
     */
    private boolean isValidResult() {
        updateBlock();
        return pixyBlock.signature == 1 || pixyBlock.signature == 2;
    }

    /**
     * this updates the pixy block object and is called before returning the object in "getBlock()"
     * as such users do not need to call it in their loop.
     */
    private void updateBlock() {
        /*
        this is the data request we send to pixy to get back the block/color object
         */
        byte[] blockRequest = {(byte) 174, (byte) 193, (byte) 32, (byte) 2, (byte) 3, (byte) 1};
        // note that here, 3 indicates we wish to see blocks from signatures 1 and 2 (1 + 2)
        // and 1 indicates that we are only interested in seeing 1 block detected by pixy

        /*
        writes our request to the I2C slave i.e. pixy
         */
        deviceClient.write(0, blockRequest);
        deviceClient.waitForWriteCompletions(I2cWaitControl.WRITTEN);

        /*
        the raw feedback we get from pixy
         */
        byte[] rawBytes = deviceClient.read(readWindow.getRegisterFirst(), readWindow.getRegisterCount());

        /*
        now we convert the data to update the pixyBlock
         */
        if (combineBytes(rawBytes[0], rawBytes[1]) == syncWord) {
            pixyBlock = bytesToBlock(rawBytes);
        }
    }

    /**
     * @return a pixyBlock parsed from the data *bytes* given back by pixy
     * @Param the bytes of data to be processed into a "PixyBlock" object
     */
    private PixyBlock bytesToBlock(@NonNull byte[] rawBytes) {
        PixyBlock detectedBlock = new PixyBlock();

        detectedBlock.signature = combineBytes(rawBytes[6], rawBytes[7]);
        detectedBlock.centerX = combineBytes(rawBytes[8], rawBytes[9]);
        detectedBlock.centerY = combineBytes(rawBytes[10], rawBytes[11]);
        detectedBlock.width = combineBytes(rawBytes[12], rawBytes[13]);
        detectedBlock.height = combineBytes(rawBytes[14], rawBytes[15]);
        detectedBlock.angle = combineBytes(rawBytes[16], rawBytes[17]);
        detectedBlock.trackingIndex = combineBytes(rawBytes[18], (byte) 0);
        detectedBlock.age = combineBytes(rawBytes[19], (byte) 0);

        return detectedBlock;
    }

    /*
    essentially combines two bytes into one. i.e generates a 16-bit word.
    some data relayed by pixy doesn't fit in 1 byte, so we use this to combine
    two relevant bytes into one interpretable int.
     */
    private int combineBytes(byte lower, byte upper) {
        return (((int) upper & 0xff) << 8) | ((int) lower & 0xff);
    }


    //-------------- baggage from the REV I2C API
    @Override
    protected boolean doInitialize() {
        ((LynxI2cDeviceSynch) (deviceClient)).setBusSpeed(LynxI2cDeviceSynch.BusSpeed.FAST_400K);
        return true;
    }

    @Override
    public Manufacturer getManufacturer() {
        return Manufacturer.Other;
    }

    @Override
    public String getDeviceName() {
        return "pixy";
    }
}

