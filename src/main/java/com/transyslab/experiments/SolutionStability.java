/*
package com.transyslab.experiments;

import com.transyslab.commons.tools.ADFullerTest;
import com.transyslab.commons.tools.FitnessFunction;
import com.transyslab.commons.tools.VariablePerturbation;
import com.transyslab.simcore.mlp.MLPEngine;
import com.transyslab.simcore.mlp.MLPLink;
import com.transyslab.simcore.mlp.MLPParameter;
import com.transyslab.simcore.mlp.MacroCharacter;

import java.util.Arrays;
import java.util.List;

*/
/*

public class SolutionStability {
	public static void main(String[] args) {
		MLPEngine mlpEngine = new MLPEngine("src/main/resources/demo_neihuan/scenario2/optwym.properties");
		mlpEngine.loadFiles();
		double[] fullParas = new double[]{0.4633, 21.7950, 0.1765, 120/3.6, 170.19,1.4367,4.1219,3.6699};
		double[] subParams = Arrays.copyOfRange(fullParas, 4, 8);
		mlpEngine.seedFixed = true;//强制
		mlpEngine.runningSeed = 1718400000;
		mlpEngine.getSimParameter().setLCBuffTime(3.5627);
		mlpEngine.getSimParameter().setLCDStepSize(0.0);
		// 参数取值范围
		double kjam = 0.1765, qmax = 0.4633, vfree = 21.7950,deltat = 0.2;
		double xcLower = MLPParameter.xcLower(kjam, qmax,deltat);
		double rupper = MLPParameter.rUpper(10,vfree,kjam,qmax);
		double[] plower = new double[]{xcLower+0.00001,1e-5,0.0,0.0};
		double[] pupper = new double[]{200.0, rupper-1e-5, 10.0, 10.0};
		for (int i = 0; i < 10; i++) {
			double[] pertubeParams = VariablePerturbation.pertubate(plower,pupper,0.01,subParams);
			System.arraycopy(pertubeParams,0,fullParas,4,pertubeParams.length);
			mlpEngine.runWithPara(fullParas);
			//统计发车
			int vehHoldCount = 0;
			for (int k = 0; k<mlpEngine.getNetwork().nLinks(); k++) {
				vehHoldCount += ((MLPLink) mlpEngine.getNetwork().getLink(k)).countHoldingInflow();
			}
			System.out.println("未发车辆数：" + (vehHoldCount-208) + "辆");
			List<MacroCharacter> records = mlpEngine.getNetwork().getSecStatRecords("det2");
			double[] kmSpd = records.stream().mapToDouble(MacroCharacter::getKmSpeed).toArray();
			double[] minFlow = records.stream().mapToDouble(r -> r.getHourFlow()/12.0).toArray();
			double rmse = FitnessFunction.evaRNSE(kmSpd, mlpEngine.getEmpData());
			double ksdis = FitnessFunction.evaKSDistance(ADFullerTest.seriesDiff(kmSpd,1),
					ADFullerTest.seriesDiff(mlpEngine.getEmpData(),1));
			System.out.println("RMSE: "  + rmse );
			System.out.println("KSFitness: " + ksdis);
			System.out.println(Arrays.toString(minFlow));
		}
		mlpEngine.close();
	}
}
*/
