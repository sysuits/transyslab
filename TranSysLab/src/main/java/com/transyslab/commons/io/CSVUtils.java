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

package com.transyslab.commons.io;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;


public class CSVUtils {

	/**读取csv文件
	 * @param filePath 文件路径
	 * @param header csv列头
	 * @return CSVRecord 列表
	 * @throws IOException **/
	public static CSVParser getCSVParser(String filePath,String[] header, boolean firstRowIsHeader) throws IOException{
		//创建CSVFormat
		CSVFormat formator;
		if(firstRowIsHeader){
			formator = CSVFormat.DEFAULT.withFirstRecordAsHeader();
		}
		else{
			if(header==null)
				//忽略表头
				formator = CSVFormat.DEFAULT.withIgnoreHeaderCase();
			else
				formator = CSVFormat.DEFAULT.withHeader(header);
		}
		InputStreamReader fileReader=new InputStreamReader(new FileInputStream(filePath),StandardCharsets.UTF_8);
		//创建并返回CSVParser对象
		return new CSVParser(fileReader,formator);
	}
	public static CSVParser getCSVParser(String filePath, boolean firstRowIsHeader)throws IOException{
		return getCSVParser(filePath,null,firstRowIsHeader);
	}
	public static CSVPrinter getCSVWriter(String filePath, String[] header, boolean append)throws IOException{
		FileUtils.createFile(filePath);
		//创建CSVFormat
		CSVFormat formator;
		if(header==null)
			//忽略表头
			formator = CSVFormat.DEFAULT.withIgnoreHeaderCase().withRecordSeparator("\n");
		else
			formator = CSVFormat.DEFAULT.withHeader(header).withRecordSeparator("\n");
		OutputStreamWriter fileWriter = new OutputStreamWriter(new FileOutputStream(filePath,append),StandardCharsets.UTF_8);
		return new CSVPrinter(fileWriter,formator);
	}
	public static List<CSVRecord> readCSV(String filePath,String[] header,boolean firstRowIsHeader) throws IOException{
		CSVParser parser = getCSVParser(filePath,header,firstRowIsHeader);

		List<CSVRecord> records=parser.getRecords();

		parser.close();

		return records;
	}

	public static List<CSVRecord> readCSV(String filePath,String[] header) throws IOException{
		return readCSV(filePath,header,false);
	}

	public static double[][] readCSVData(String filePath,String[] headers) throws IOException{
		List<CSVRecord> records = readCSV(filePath,headers);
		double[][] ans = new double[records.get(0).size()][records.size()];
		for (int i = 0; i < records.size(); i++) {
			for (int j = 0; j < records.get(0).size(); j++) {
				ans[j][i] = Double.parseDouble(records.get(i).get(j));
			}
		}
		return ans;
	}

	public static void writeCSV(String filePath,String[] header, Object[][] data) throws IOException{
		writeCSV(filePath,header,false,data);
	}
	public static void writeCSV(String filePath,String[] header,boolean append, Object[][] data) throws IOException{
		CSVPrinter printer = getCSVWriter(filePath,header,append);
		List<Object> dataRecord = new ArrayList<>();
		for(int i=0;i<data.length;i++){
			for(int j=0;j<data[i].length;j++){
				dataRecord.add(data[i][j]);
			}
			printer.printRecord(dataRecord);
			dataRecord.clear();
		}
		printer.flush();
		printer.close();
	}

}
