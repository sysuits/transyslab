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

package com.transyslab.commons.renderer;

import java.util.Arrays;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import com.transyslab.roadnetwork.Constants;

//˫������У�һ���ɶ��������ߣ���һ����д�������ߣ���
//��Ⱦ�߳̽��ж������������߳̽���д����
//һ����ԣ������д���ٶ�>��Ⱦ�Ķ�ȡ�ٶ�
//Ϊ�����ȱ�֤��Ⱦ�����������ɶ�����Ϊ�����������д���н��н�����
public class FrameQueue {
	
	//����֡����
    private final static int capacity_ = Constants.FRAME_QUEUE_BUFFER;
    //����������֤д�����Ͷ��н����������̰߳�ȫ
    private ReentrantLock  writeLock_;  
    //�������������ڻ��ѷ���д�����
    private Condition notFull_;  
  
    //�ɶ�����д����  
    private AnimationFrame[] writeArray_, readArray_;
    //���в�����  
    private volatile int writeCount_, readCount_;
    //����ͷβ����  
    private int writeArrayHP_, writeArrayTP_, readArrayHP_, readArrayTP_;  
    
    //֡��
    private int frameCount;
    
    //����ģʽ
    private static FrameQueue theFrameQueue;
    public static FrameQueue getInstance(){
    	if(theFrameQueue == null)
    		 theFrameQueue = new FrameQueue(capacity_) ;
    	return theFrameQueue;
    }
    //main�̵߳��ã���ʼ��������Ԫ��
    public void initFrameQueue(){

    	/*
    	for(int i=0;i<capacity_;i++){
    		writeArray_[i] = new AnimationFrame();
    		readArray_[i] = new AnimationFrame();
    	}*/
    }
    public int getFrameCount(){
    	return frameCount;
    }
    public FrameQueue(int capacity){

        if(capacity<=0)  
        {  
            throw new IllegalArgumentException("Queue initial capacity can't less than 0!");  
        }
        frameCount = 0;
        readArray_ = new AnimationFrame[capacity];  
        writeArray_ = new AnimationFrame[capacity];  
        
        writeLock_ = new ReentrantLock();
        notFull_ = writeLock_.newCondition(); 
    }  
    //������󣬷��̰߳�ȫ���ɼ�������offer���ñ�֤�̰߳�ȫ
    private void insert(AnimationFrame e)  
    {  
    	
        writeArray_[writeArrayTP_] = e;  
        ++writeArrayTP_;
        //0-capacity
        ++writeCount_;  
    }  
    //ȡ������ �����̰߳�ȫ���ɼ�������poll���ñ�֤�̰߳�ȫ 
    private AnimationFrame extract(boolean isPause)  
    {  
    	AnimationFrame e = readArray_[readArrayHP_];
    	if(!isPause){
            ++readArrayHP_;
            //capacity-0
            --readCount_;  
    	}
        return e;  
    }  
  
      

//��������:  
//�ɶ�����Ϊ��  && ��д�������� 
    private boolean queueSwitch() 
    {
    	//��Ⱦ�߳�ռ��д������������߳��޸�����
        writeLock_.lock();  
        try  
        {
        	//��д����δ��
            if (writeCount_ < writeArray_.length)  
            {   
            	return false;
            }  
            else  
            {
                //ͳһ����vehicledata
                Arrays.stream(readArray_).forEach(f->{
                    if (f!=null)
                        f.clean();
                });
            	//�������ý���
            	AnimationFrame[] tmpArray = readArray_;  
                readArray_ = writeArray_;  
                writeArray_ = tmpArray;
                
                //���в��������������û򽻻�
                readCount_ = writeCount_;  
                readArrayHP_ = 0;  
                readArrayTP_ = writeArrayTP_;  
  
                writeCount_ = 0;  
                writeArrayHP_ = readArrayHP_;  
                writeArrayTP_ = 0;  
                //�����ѱ��������������ķ����߳�
                //ע�⣺һ����д���������������̻߳ᱻ������������await()����
                notFull_.signal();   
                return true;  
            }  
        }  
        finally  
        {  
        	//��Ⱦ�߳��ͷ�д��
            writeLock_.unlock();  
        }  
    }  

    public void offer(AnimationFrame e) throws InterruptedException 
    {  
        if(e != null) {
            writeLock_.lock();
            try {
                //���������������������߳�
                if(writeCount_ < writeArray_.length) {
                    insert(e);
                }
                else
                    //�����߳������ڴ˴��ȴ���Ⱦ�߳�signal
                    notFull_.await();
            }
            finally {
                writeLock_.unlock();
            }
        }
    }


    public AnimationFrame poll(boolean isPause){
    	//���жϿɶ������Ƿ�Ϊ��
        if(readCount_<=0)  
        {
        	//��������ʧ���򷵻ؿ�֡
        	if(queueSwitch()) 
        		return extract(isPause);
        	else if (readCount_==0)
        	    return readArray_[readArray_.length-1];
        	else
        		return null;  
        }
        //�ɶ����в�Ϊ�����ȡ����
        if(readCount_>0) 
        	return extract(isPause);
        return null;
    }
    public void clear(){
        frameCount = 0;
        writeArray_ = new AnimationFrame[capacity_];
        readArray_ = new AnimationFrame[capacity_];
    }
}
