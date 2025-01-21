
public class Pixy3 extends I2cDeviceSynchDeviceWithParameters<I2cDeviceSynch, Pixy3.PixyCam2Params> {

    static final I2cAddr DEFAULT_ADDRESS = I2cAddr.create7bit(0x54);

    private PixyBlock detectedBlock;

    private final int synchWord = 0xc1af;

    private final int sigmap = 255;

    private final int numBlocksToRead = 1;

    /*
     * data request: color block       Values:
     * 0-1 synch word                  0xAE, 0xC1
     * 2 type of packet                32... = 0x20
     * 3 length of payload             2... = 0x2
     * 4 sigmap                        255
     * 5 maximum blocks to return      1
     */
    private final byte[] colorBlockRequest = {(byte) 0xAE, (byte) 0xC1, 0x20, 0x2, (byte) sigmap, (byte) numBlocksToRead};

    private final byte[] fpsRequest = {};

    private final byte[] setLampsRequest = {};

    private byte[] rawBytes;

    I2cDeviceSynch.ReadWindow readWindow;

    public static class PixyCam2Params implements Cloneable {

        I2cAddr i2cAddr = DEFAULT_ADDRESS;

        public PixyCam2Params clone() {
            try {
                return (PixyCam2Params) super.clone();
            } catch (CloneNotSupportedException e) {
                throw new RuntimeException("Internal error: parameters not cloneable");
            }
        }
    }

    public Pixy3(I2cDeviceSynch deviceSynch) {
        super(deviceSynch, true, new PixyCam2Params());

        this.deviceClient.setI2cAddress(DEFAULT_ADDRESS);
        super.registerArmingStateCallback(false);
        this.deviceClient.setLogging(true);
        this.deviceClient.engage();

        /*
         * The REV I2C API supports 26 read and 26 write registers
         */
        rawBytes = new byte[26];

        readWindow = new I2cDeviceSynch.ReadWindow(0, rawBytes.length, I2cDeviceSynch.ReadMode.BALANCED);
        this.deviceClient.setReadWindow(readWindow);
    }

    public void requestSync(Telemetry telemetry) {
        telemetry.addData("Writing", deviceClient.isWriteCoalescingEnabled());
        byte[] request = { (byte) 0xAE, (byte) 0xC1, 0x20, 0x2, (byte)  255, (byte) numBlocksToRead };

        this.deviceClient.write(1, request); // reads 43433
        //this.deviceClient.write8((byte) 0xAE);
    }

    public void readSync(@NonNull Telemetry telemetry) {
        rawBytes = this.deviceClient.read(readWindow.getRegisterFirst(), readWindow.getRegisterFirst() + 19);
        //telemetry.addData("Result", combineBytes(rawBytes[0], rawBytes[1]));
        //telemetry.addData("Byte", rawBytes[0]);

        for (int i = 0; i < rawBytes.length - 1; i++) {
            int b1 = rawBytes[i];
//
            if (b1 < 0) b1 += 256;
//
            int b2 = rawBytes[i + 1];
//
            if (b2 < 0) b2 += 256;

            if (combineBytes((byte) b1, (byte) b2) == 0xc1af) telemetry.addLine("found it");
            telemetry.addData("one", rawBytes[i] + i);
            telemetry.addData("two", rawBytes[i + 1] + (i + 1));
            telemetry.addData("combined", combineBytes(rawBytes[i], rawBytes[i + 1]) + (i + 1));
//            //telemetry.addData("writing coalexcing", this.deviceClient.isWriteCoalescingEnabled());
//        }
        }
    }


    public PixyBlock getBlock(Telemetry telemetry) {
        updateBlock(telemetry);
        return detectedBlock;
    }

    /**
     * @param upper the second byte the pixy sends
     * @param lower the first byte the pixy sends
     * @return the combination of the two bytes, for example
     * the bytes that return the blocks signature are 6-7, so we must
     * combine these bytes into one.
     */
    public int combineBytes(byte upper, byte lower) {
        return (((int) upper & 0xff) << 8) | ((int) lower & 0xff);
    }

    /**
     * @param rawBytes the bytes sent from pixy
     * @return a usable pixy block object, with human interpretable details
     */
    public PixyBlock bytesToBlock(@NonNull byte[] rawBytes) {
        PixyBlock detectedBlock = new PixyBlock();

        detectedBlock.signature = combineBytes(rawBytes[0], rawBytes[1]);
        detectedBlock.centerX = combineBytes(rawBytes[2], rawBytes[3]);
        detectedBlock.centerY = combineBytes(rawBytes[4], rawBytes[5]);
        detectedBlock.width = combineBytes(rawBytes[6], rawBytes[7]);
        detectedBlock.height = combineBytes(rawBytes[8], rawBytes[9]);
        detectedBlock.angle = combineBytes(rawBytes[10], rawBytes[11]);
        detectedBlock.trackingIndex = combineBytes(rawBytes[12], (byte) 0);
        detectedBlock.age = combineBytes(rawBytes[13], (byte) 0);

        return detectedBlock;
    }

    public PixyBlock bytesToBlockIndex(@NonNull byte[] rawBytes, int i) {
        PixyBlock detectedBlock = new PixyBlock();

        detectedBlock.signature = combineBytes(rawBytes[i + 0], rawBytes[i + 1]);
        detectedBlock.centerX = combineBytes(rawBytes[i + 2], rawBytes[i + 3]);
        detectedBlock.centerY = combineBytes(rawBytes[i + 4], rawBytes[i + 5]);
        detectedBlock.width = combineBytes(rawBytes[i + 6], rawBytes[i + 7]);
        detectedBlock.height = combineBytes(rawBytes[i + 8], rawBytes[i + 9]);
        detectedBlock.angle = combineBytes(rawBytes[i + 10], rawBytes[i + 11]);
        detectedBlock.trackingIndex = combineBytes(rawBytes[i + 12], (byte) (i + 0));
        detectedBlock.age = combineBytes(rawBytes[i + 13], (byte) (i + 0));

        return detectedBlock;
    }


    public void updateBlock(Telemetry telemetry) {
        /*
         * this sends our requested data to pixy
         */
        this.deviceClient.write(0, colorBlockRequest);

        /*
         * these are the raw bytes sent back by pixy
         */
        rawBytes = this.deviceClient.read(readWindow.getRegisterFirst(), readWindow.getRegisterCount());

        /*
         * this parses through the first two bytes to test for the sync word "0xc1af".
         * if the synch word is found, the class variable "detected block" is updated with new data
         */
        if (combineBytes(rawBytes[0], rawBytes[1]) == synchWord) {
            telemetry.addLine("found it");
        } else {
            telemetry.addLine("didnt found it");
        }

    }

    public int getFPS() {
        return 1;
    }

    public void setLamps() {

    }

    @Override
    protected boolean internalInitialize(@NonNull PixyCam2Params pixyCam2Params) {
        this.parameters = pixyCam2Params.clone();
        deviceClient.setI2cAddress(parameters.i2cAddr);
        return true;
    }

    @Override
    public Manufacturer getManufacturer() {
        return Manufacturer.Other;
    }

    @Override
    public String getDeviceName() {
        return "pixy3";
    }

}

