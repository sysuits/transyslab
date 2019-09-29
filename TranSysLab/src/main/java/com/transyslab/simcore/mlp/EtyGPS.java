package com.transyslab.simcore.mlp;

import com.transyslab.commons.tools.SimulationClock;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class EtyGPS {
    public String hphm; // 号牌号码
    public int hpzl;// 号牌种类
    public int cllx;// 车辆类型1：大型私家车；2：小型私家车；3：公交车；4：出租车；5：货车；
    // 其对应的hpzl：01,02,02,02,01
    public int localness;// 是否为本地车，1本地，0外地
    public double longitude;// 经度
    public double latitude;// 纬度
    public int angle;// 车头角度，正北向为0度，顺时针方向旋转为正方向
    public String speedcolor;// 车身颜色，代表车速值高（绿）中（黄）低（红），
    public String moment;
    public String ftnode;
    public double speed;

    public EtyGPS(String hphm, String hpzl, double longitude, double latitude, double angle,
                  String speedColor, LocalDateTime moment,String ftnode, double speed) {
        this.hphm = hphm;
        this.hpzl = Integer.valueOf(hpzl);
        // 保留6位小数
        BigDecimal bdx = new BigDecimal(longitude);
        BigDecimal bdy = new BigDecimal(latitude);
        this.longitude = bdx.setScale(6, BigDecimal.ROUND_HALF_UP).doubleValue();
        this.latitude = bdy.setScale(6, BigDecimal.ROUND_HALF_UP).doubleValue();
        // 角度取整
        this.angle = (int) Math.round(angle);
        this.speedcolor = speedColor;
        this.moment = moment.format(SimulationClock.DATETIME_FORMAT);
        this.ftnode = ftnode;
        this.speed = speed;
    }
}
