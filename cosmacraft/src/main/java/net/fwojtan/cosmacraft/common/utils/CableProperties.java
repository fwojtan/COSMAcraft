package net.fwojtan.cosmacraft.common.utils;

import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;

import java.util.ArrayList;
import java.util.List;

public class CableProperties {
    public Vector3d startPoint;
    public Vector3d endPoint;
    public Vector3f color;

    public CableProperties(double startX, double startY, double startZ, double endX, double endY, double endZ, float colR, float colG, float colB){
        startPoint = new Vector3d(startX, startY, startZ);
        endPoint = new Vector3d(endX, endY, endZ);
        color = new Vector3f(colR, colG, colB);
    }


}
