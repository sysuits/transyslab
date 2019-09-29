package com.transyslab.simcore.mlp;

import com.transyslab.commons.tools.SimulationClock;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class EtyGPS {
    public String hphm; // ���ƺ���
    public int hpzl;// ��������
    public int cllx;// ��������1������˽�ҳ���2��С��˽�ҳ���3����������4�����⳵��5��������
    // ���Ӧ��hpzl��01,02,02,02,01
    public int localness;// �Ƿ�Ϊ���س���1���أ�0���
    public double longitude;// ����
    public double latitude;// γ��
    public int angle;// ��ͷ�Ƕȣ�������Ϊ0�ȣ�˳ʱ�뷽����תΪ������
    public String speedcolor;// ������ɫ��������ֵ�ߣ��̣��У��ƣ��ͣ��죩��
    public String moment;
    public String ftnode;
    public double speed;

    public EtyGPS(String hphm, String hpzl, double longitude, double latitude, double angle,
                  String speedColor, LocalDateTime moment,String ftnode, double speed) {
        this.hphm = hphm;
        this.hpzl = Integer.valueOf(hpzl);
        // ����6λС��
        BigDecimal bdx = new BigDecimal(longitude);
        BigDecimal bdy = new BigDecimal(latitude);
        this.longitude = bdx.setScale(6, BigDecimal.ROUND_HALF_UP).doubleValue();
        this.latitude = bdy.setScale(6, BigDecimal.ROUND_HALF_UP).doubleValue();
        // �Ƕ�ȡ��
        this.angle = (int) Math.round(angle);
        this.speedcolor = speedColor;
        this.moment = moment.format(SimulationClock.DATETIME_FORMAT);
        this.ftnode = ftnode;
        this.speed = speed;
    }
}
