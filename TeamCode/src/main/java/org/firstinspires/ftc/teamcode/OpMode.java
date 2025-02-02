package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;

public class OpMode extends LinearOpMode {

    private Pixy pixy;

    @Override
    public void runOpMode() throws InterruptedException {

        pixy = hardwareMap.get(Pixy.class, "deviceName");
        PixyBlock detectedBlock;

        pixy.turnOffLamps();

        waitForStart();
        while(opModeIsActive()){

            detectedBlock = pixy.getBlock();

            telemetry.addLine(detectedBlock.toString());

            if(detectedBlock.isValid()){
                // do something
            }
            telemetry.update();
        }
    }
}
