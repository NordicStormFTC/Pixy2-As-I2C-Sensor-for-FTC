package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;

public class OpMode extends LinearOpMode {

    private Pixy pixy;

    @Override
    public void runOpMode() throws InterruptedException {

        /** instantiate pixy like any other motor or sensor
         **/
        pixy = hardwareMap.get(Pixy.class, "deviceName");

        /*
        outside of the loop, declare a pixy block
         */
        PixyBlock detectedBlock;

        /*
        you can turn the lamps on or off really at any time.
         */
        pixy.turnOffLamps();

        waitForStart();
        while (opModeIsActive()) {

            /*
            returns a non null block with updated info from pixy
             */
            detectedBlock = pixy.getBlock();

            telemetry.addLine(detectedBlock.toString());

            if (detectedBlock.isValid()) {
                // pixy detected something... now do something
            }
            telemetry.update();
        }
    }
}
