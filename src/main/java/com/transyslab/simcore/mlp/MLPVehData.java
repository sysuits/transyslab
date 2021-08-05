package com.transyslab.simcore.mlp;

import com.jogamp.opengl.GL2;
import com.transyslab.commons.renderer.JOGLCanvas;
import com.transyslab.commons.renderer.ShapeUtil;
import com.transyslab.roadnetwork.Constants;
import com.transyslab.roadnetwork.VehicleData;

public class MLPVehData extends VehicleData {
    @Override
    public void render(GL2 gl) {
        if ((getSpecialFlag() & Constants.FOLLOWING) == 0){
            ShapeUtil.drawPolygon(gl, getVhcShape().getKerbList(), Constants.COLOR_RED, isSelected(), JOGLCanvas.LAYER_VEHICLE);
        }
        else {
            if((getSpecialFlag() & Constants.VIRTUAL_VEHICLE) != 0)//ÐéÄâ³µ
                ShapeUtil.drawPolygon(gl, getVhcShape().getKerbList(), Constants.COLOR_LITEBLUE, isSelected(),JOGLCanvas.LAYER_VEHICLE);
            else //·ÇÐéÄâ³µ
                ShapeUtil.drawPolygon(gl, getVhcShape().getKerbList(), Constants.COLOR_BLUE, isSelected(),JOGLCanvas.LAYER_VEHICLE);
        }
    }
}
