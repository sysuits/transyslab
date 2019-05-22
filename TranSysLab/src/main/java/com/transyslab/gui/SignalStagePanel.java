/*
 * Copyright 2019 The TranSysLab Authors. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.transyslab.gui;

import com.transyslab.roadnetwork.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.geom.Rectangle2D;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class SignalStagePanel extends JPanel {

    private List<SignalPlan> plans2Paint;
    private Map<Integer,Color> colorMap;
    private List<Color> colors;
    private Map<Rectangle2D.Double, Integer> stageBars;
    private List<Arrow2D> stageArrows;
    private Map<String,int[]>mapDirInt;

    private double fTime;
    private double tTime;
    private static SignalStagePanel ssp;
    private SignalStagePanel() {
        setBackground(Color.white);
        this.mapDirInt = new HashMap<>();
        this.colorMap = new HashMap<>();
        this.stageBars = new HashMap<>();
        this.stageArrows = new ArrayList<>();
        initialize();
    }
    public static SignalStagePanel getInstance(){
        if(ssp == null)
            ssp = new SignalStagePanel();
        return ssp;
    }

    public void initialize(){

        colors = new ArrayList<>();
        colors.add(new Color(237,125,49));
        colors.add(new Color(165,165,165));
        colors.add(new Color(91,155,213));
        colors.add(new Color(253,135,242));
        mapDirInt.put("W",new int[]{1,0});
        mapDirInt.put("E",new int[]{-1,0});
        mapDirInt.put("S",new int[]{0,-1});
        mapDirInt.put("N",new int[]{0,1});

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                SignalStagePanel eventObj = (SignalStagePanel) e.getComponent();
                if(eventObj.plans2Paint!=null){
                    eventObj.calcShape2Paint();
                    eventObj.repaint();
                }
            }
        });
    }
    public void setPlans(List<SignalPlan> plans, double fTime, double tTime){
        this.plans2Paint = plans;
        this.updateTime(fTime,tTime);
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2=(Graphics2D) g;
        g2.setPaint(Color.black);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        for(int i=0;i<stageArrows.size();i++){
            g2.setPaint(colorMap.get(stageArrows.get(i).id));
            Java2DUtil.drawArrow(g2,stageArrows.get(i));
        }

        for(Map.Entry<Rectangle2D.Double, Integer> entry : stageBars.entrySet()){
            g2.setPaint(colorMap.get(entry.getValue()));
            g2.fill(entry.getKey());
        }

    }


    public void updateTime(double fTime, double tTime){
        if(plans2Paint!=null){
            this.stageArrows.clear();
            this.stageBars.clear();
            this.colorMap.clear();
            this.fTime = fTime;
            this.tTime = tTime;
            calcShape2Paint();
            repaint();
        }

    }
    public void clear(){
        this.plans2Paint = null;
        this.stageArrows.clear();
        this.stageBars.clear();
        this.colorMap.clear();
        repaint();
    }

    public void calcShape2Paint(){
        int width = this.getWidth();
        int height =  this. getHeight();
        for(int i=0;i<plans2Paint.size();i++){
            SignalPlan itrPlan = plans2Paint.get(i);
            // 色条起始位置
            int[] sBarPos = new int[]{0,height - 15};
            // 箭头起始位置
            int[] sArrowPos = new int[]{0,height - 20};

            if(i==0) {//第一个方案始终在时间轴起点
                sArrowPos[0] = 32;
                sBarPos[0] = 32;
            }
            else{
                sArrowPos[0] = 32 + (int)Math.round((itrPlan.getFTime()-fTime)/(tTime-fTime) * (width - 2*32));
                sBarPos[0] = 32 + (int)Math.round((itrPlan.getFTime()-fTime)/(tTime-fTime) * (width -2*32));
            }
            // 阶段时间表
            List<double[]> stages = itrPlan.getSignalTable().stream().filter(s->s[2]>=fTime && s[1]<tTime)
                    .collect(Collectors.toList());
            for(int j=0;j<stages.size();j++){
                double sPosX;
                if(stages.get(j)[1]<fTime )
                    sPosX = 32;
                else
                    sPosX = 32 + Math.round((stages.get(j)[1]-fTime)/(tTime-fTime)* (width-2*32));
                double sWidth;
                if(stages.get(j)[2]<=tTime)
                    sWidth = 32 + Math.round((stages.get(j)[2]-fTime)/(tTime-fTime)* (width-2*32)) - sPosX;
                else
                    sWidth = 32 + Math.round(1.0 * (width-2*32)) - sPosX;
                stageBars.put(new Rectangle2D.Double(sPosX,height - 15,sWidth,10),(int)stages.get(j)[0]);
            }

            // stage间隔
            int sOffset = 20;
            // stage图例
            for(int j=0;j<itrPlan.getStages().size();j++ ){
                SignalStage s = itrPlan.getStages().get(j);
                colorMap.put(s.getId(),colors.get(j));
                int aOffset = 10;
                int baseY = 9;
                List<String> turnInfo = s.getDirections();
                for(String turn:turnInfo){
                    String[] spiltInfo = turn.split("_");//0:东南西北；1:左直右
                    int[] dir = mapDirInt.get(spiltInfo[0]);
                    if(dir[0] != 0){//→←
                        if(dir[0]<0) {
                            stageArrows.add(new Arrow2D(s.getId(), spiltInfo[1], new int[]{sArrowPos[0] + (int)Arrow2D.LINELENGTH, baseY}, dir));
                            sArrowPos[0] -= aOffset;
                        }
                        else
                            stageArrows.add(new Arrow2D(s.getId(), spiltInfo[1], new int[]{sArrowPos[0], baseY},dir));
                        if(spiltInfo[1].equals("S"))
                            baseY += 8;
                        else{
                            baseY += 6;
                        }
                    }
                    else {//↑↓
                        if (dir[1] == 1) {
                            stageArrows.add(new Arrow2D(s.getId(), spiltInfo[1], new int[]{sArrowPos[0], 1}, dir));
                        } else
                            stageArrows.add(new Arrow2D(s.getId(), spiltInfo[1], sArrowPos, dir));
                    }

                    sArrowPos[0] += aOffset;
                }
                //TODO 箭头排序?
                sArrowPos[0] += sOffset;
            }
        }
    }
}
