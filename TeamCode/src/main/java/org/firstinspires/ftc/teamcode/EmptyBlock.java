package org.firstinspires.ftc.teamcode;

import androidx.annotation.NonNull;

public class EmptyBlock extends PixyBlock {
    public final int sync = 0;
    public final int checksum = 0;
    public final int signature = 0;
    public final int centerX = 0;
    public final int centerY = 0;
    public final int width = 0;
    public final int height = 0;
    public final int angle = 0;
    public final int trackingIndex = 0;
    public final int age = 0;

    @Override
    public boolean isValid(){
        return false;
    }

    @NonNull
    public String toString(){
        return "";
    }
}