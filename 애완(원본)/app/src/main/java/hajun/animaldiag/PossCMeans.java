package hajun.animaldiag;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Random;
import java.util.Scanner;

public class PossCMeans
{
	private double m_dEpsilon;
	private double m_dFuzziness;
	private double m_dP;			// 2 / (m_dFuzziness - 1);

	private int m_nDataNum;
	private int m_nClusterNum;
	private int m_nDimensionNum;

	private double m_dX[][];		// N, D 
	private double m_dU[][];		// N, C
	private double m_dV[][];		// C, D
	private double m_dDistance[][];	// N, C

	private double m_dAlpha[][];
	private double m_dWinDistance[];

	public int m_nRepeatCnt;

	public PossCMeans()
	{
	}

	public int GetDataNum()
	{
		return m_nDataNum;
	}

	public int GetDimensionNum()
	{
		return m_nDimensionNum;
	}

	public int GetClusterNum()
	{
		return m_nClusterNum;
	}

	public void SetTrainData(int nDataNum, int nDimensionNum, int nClusterNum, double[][] dTrain)
	{
		m_nDataNum = nDataNum;
		m_nDimensionNum = nDimensionNum;
		m_nClusterNum = nClusterNum;

		InitialAlloc();

		m_dX = dTrain;
	}

	public void SavePCMData(File fp)
	{
		try {
			FileOutputStream fos = new FileOutputStream(fp);
			BufferedOutputStream bos = new BufferedOutputStream(fos);
			DataOutputStream dos = new DataOutputStream(bos);

			dos.writeInt(m_nClusterNum);
			dos.writeInt(m_nDimensionNum);

			for(int i = 0; i < m_nClusterNum; i++)
				for(int j = 0; j < m_nDimensionNum; j++)
					dos.writeDouble(m_dV[i][j]);

			dos.close();
			bos.close();
			fos.close();
		}
		catch (IOException e) {
			System.out.println("Save Data Failed.");
			e.printStackTrace();
		}
	}

	public void LoadPCMData(File fp)
	{
		try
		{
			FileInputStream fis = new FileInputStream(fp);
			BufferedInputStream bis = new BufferedInputStream(fis);
			DataInputStream dis = new DataInputStream(bis);

			m_nClusterNum = dis.readInt();
			m_nDimensionNum = dis.readInt();

			m_dV = new double[m_nClusterNum][m_nDimensionNum];

			for(int j = 0; j < m_nClusterNum; j++)
				for(int k = 0; k < m_nDimensionNum; k++)
					m_dV[j][k] = dis.readDouble();

			dis.close();
			bis.close();
			fis.close();
		}
		catch (IOException e) {
			System.out.println("Load Data Failed.");
			e.printStackTrace();
		}
	}

	public void ReadDataFile(String path)
	{
		try
		{
			Scanner sc = new Scanner(new File(path));

			m_nDataNum = sc.nextInt();
			m_nDimensionNum = sc.nextInt();
			m_nClusterNum = sc.nextInt();

			m_dX = new double[m_nDataNum][m_nDimensionNum];

			for(int i = 0; i < m_nDataNum; i++)
				for(int k = 0; k < m_nDimensionNum; k++)
					m_dX[i][k] = sc.nextDouble();

			sc.close();
		}
		catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		InitialAlloc();
	}

	private void InitialAlloc()
	{
		m_dV 		= new double[m_nClusterNum][m_nDimensionNum];
		m_dU 		= new double[m_nDataNum][m_nClusterNum];
		m_dDistance = new double[m_nDataNum][m_nClusterNum];
		m_dWinDistance = new double[m_nClusterNum];
		m_dAlpha	= new double[m_nDataNum][m_nDimensionNum];

		System.out.println("증상 수 : " + m_nDimensionNum);
		System.out.println("질병 수 : " + m_nDataNum);
		System.out.println("클러스터 수 : " + m_nClusterNum + "\n");
	}

	public void PCMLearning(double dFuzziness, double dEpsilon)
	{
		m_dFuzziness = dFuzziness;
		m_dP = 2 / (m_dFuzziness - 1);
		m_dEpsilon = dEpsilon;

		System.out.println(String.format("PCM Learning Start.\n(Fuzziness : %f, Epsilon : %f)\n", m_dFuzziness, m_dEpsilon));

		// PCM 학습
		System.out.print("STEP 1. Complete Initialization ... ");
		Step1_Initialize();
		System.out.println("Done.\n");

		m_nRepeatCnt = 0;
		double dMaxDiff;
		do
		{
			System.out.println(String.format("[%dth Iteration]", ++m_nRepeatCnt));

			System.out.print("STEP 2. Calculate Cluster Center ... ");
			Step2_CalcClusterCenter();
			System.out.println("Done.");

			System.out.print("STEP 3. Update Membership ... ");
			dMaxDiff = Step3_4_CalcMembershipFunc_CompareDegree();
			System.out.println("Done.");

			System.out.println(String.format("Step 4. Membership diffrence : %f \n", dMaxDiff));
		}
		while(m_nRepeatCnt < 10);
		//while(dMaxDiff > m_dEpsilon);

		System.out.println("PCM Learning Compelete.");
	}

	public void EnhancedPCM()
	{
		SymmetricMeasure();

		for(int i = 0; i < m_nClusterNum; i++)
		{
			for(int j = 0; j < m_nDimensionNum; j++)
			{
				double dblUp = 0.0;
				double dblDown = 0.0;
				for(int k = 0; k < m_nDataNum; k++)
				{
					dblUp += m_dU[i][k] * (double)m_dX[k][j];
					dblDown += m_dU[i][k];
				}
				m_dV[i][j] = dblUp / dblDown;		//클러스터의 중심좌표 설정
			}
		}
	}
	private double CalculateDistance(double p, double c) // calculate distance between data and center of cluster
	{
		return Math.sqrt(Math.pow(p - c, 2.0));
	}

	// 초기화
	private void Step1_Initialize()
	{

		double power = 0.0;
		for (int c = 0; c < m_nDataNum; c++)
		{
			for (int h = 0; h < m_nClusterNum; h++)
			{
				double bottom = 0.0;
				power = 1/(m_dFuzziness-1);
				bottom = 1+ (Math.pow(m_dX[c][h]/m_dFuzziness, power));

				m_dU[h][c] = 1 / bottom;
			}
		}

//		for(int i = 0; i < m_nClusterNum; i++)
//			m_dU[i][i] = 1;

		/*
		Random rand = new Random();

		for(int i = 0; i < m_nDataNum; i++)
		{
			double s = 0.0;
			for(int j = 1; j < m_nClusterNum; j++)
			{
				m_dU[i][j] = rand.nextDouble() / m_nClusterNum;
				s += m_dU[i][j];
			}

			m_dU[i][0] = 1.0 - s;
		}
		*/
	}

	// 클러스터 중심 값 계산
	private void Step2_CalcClusterCenter()
	{
		double m_dPowU[][] = new double[m_nDataNum][m_nClusterNum];
		for(int i = 0; i < m_nDataNum; i++)
			for(int j = 0; j < m_nClusterNum; j++)
				m_dPowU[i][j] = Math.pow(m_dU[i][j], m_dFuzziness);


		for(int j = 0; j < m_nDataNum; j++)
		{
			double den = 0.0;
			double totalDen = 0;
			double totalColor = 0;
			double data = .0;

			for(int k = 0; k < m_nClusterNum; k++)
			{
				den = Math.pow(m_dU[j][k], m_dFuzziness);
				totalColor += den * m_dX[j][k];
				totalDen += den;


				m_dV[k][j] = totalColor/totalDen;
			}
		}

/*
		for(int j = 0; j < m_nDataNum; j++)
		{
			double den = 0.0;
			double totalDen = 0;
			double totalColor = 0;
			double data = .0;

			for(int k = 0; k < m_nClusterNum; k++)
			{
				double numerator = 0, denominator = 0;

				for(int i = 0; i < m_nDataNum; i++)
				{
					numerator += m_dPowU[i][j] * m_dX[i][k];
					denominator += m_dPowU[i][j];
				}

				m_dV[j][k] = numerator / denominator;
			}

			*/
	}

	// 멤버십 업데이트 및 최대 변화량
	private double Step3_4_CalcMembershipFunc_CompareDegree()
	{
		for(int j = 0; j < m_nClusterNum; j++)
			for(int i = 0; i < m_nDataNum; i++)
				m_dDistance[i][j] = GetDistance(i, j);

		double max_diff = 0.0;
		for(int j = 0; j < m_nClusterNum; j++)
		{
			double max = 0.0;
			double min = 0.0;
			double sum = 0.0;
			double newmax = .0;

			for (int i = 0; i < m_nDataNum; i++)
			{
				if (max <
						m_dU[i][i]) max = m_dU[i][j];
				if (min > m_dU[i][j]) min = m_dU[i][j];
			}
			for (int i = 0; i < m_nDataNum; i++)
			{
				m_dU[i][ j] = (m_dU[i][ j] - min) / (max - min);
				sum += m_dU[i][ j];
			}
			for (int i = 0; i < m_nDataNum; i++)
			{
				m_dU[i][j] = m_dU[i][ j] / sum;
				if (Double.isNaN(m_dU[i][j]))
				{
					m_dU[i][j] = 0.0;
				}
				if (m_dU[i][j] > newmax) newmax = m_dU[i][j];
				double diff = Math.abs(newmax - m_dU[i][j]);

				if (diff > max_diff)
					max_diff = diff;

				m_dU[i][j] = newmax;
			}
		}
		return max_diff;
	}

	private double GetDistance(int i, int j)
	{
		double sum = 0.0;
		for(int k = 0; k < m_nDimensionNum; k++)
			sum += Math.pow(m_dX[i][k] - m_dV[j][k], 2);

		return Math.sqrt(sum);
	}

	private double GetDistance(double dPattern[], int j)
	{
		double sum = 0.0;
		for(int k = 0; k < m_nDimensionNum; k++)
			sum += Math.pow(dPattern[k] - m_dV[j][k], 2);

		return Math.sqrt(sum);
	}

	private double GetWeightDistance(double dPattern[], int j)
	{
		double sum = 0.0;
		for(int k = 0; k < m_nDimensionNum; k++)
			sum += m_dAlpha[j][k] * Math.pow(dPattern[k] - m_dV[j][k], 2);

		return Math.sqrt(sum);
	}


	private double GetNewMembership(int i, int j)
	{
		double sum = 0.0;
		for(int k = 0; k < m_nClusterNum; k++)
		{
			//System.out.println(i + " " + j + " " + m_dDistance[i][j] + "/" + m_dDistance[i][k]);
			// PCM EDIT recalc membership values
			if(m_dDistance[i][j] == m_dDistance[i][k])	// 0/0 예외처리
			{
				sum += 1;
				continue;
			}

			double dTemp = Math.pow(m_dDistance[i][j] / m_dDistance[i][k], m_dP);
			if(Double.isNaN(dTemp))
				System.out.print("NaN");

			sum += dTemp;
		}

		//System.out.println(i + " " + j + " 1/" + sum + " = " + 1.0/sum);

		return 1.0 / sum;
	}

	public void SaveData(String strPath)
	{
		try
		{
			File fp = new File(strPath);

			FileWriter fw = new FileWriter(fp);
			BufferedWriter bw = new BufferedWriter(fw);
			PrintWriter pw = new PrintWriter(bw);

			pw.print(m_nDataNum + "\n");
			pw.print(m_nDimensionNum + "\n");
			pw.print(m_nClusterNum + "\n");

			for(int i = 0; i < m_nDataNum; i++)
			{
				for(int j = 0; j < m_nDimensionNum; j++)
					pw.print(m_dX[i][j] + " ");

				pw.print("\n");
			}

			pw.close();
			bw.close();
			fw.close();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}

	// 상위 nNum개 만큼 출력
	public void Test(int nNum, boolean bWeight)
	{
		if(bWeight)		System.out.println("\n[Recognition Weighted Test]");
		else			System.out.println("\n[Recognition Test]");

		int nCorrectCount = 0;
		for(int i = 0; i < m_nDataNum; i++)
		{
			int nWinners[] = GetWinners(m_dX[i], nNum, bWeight);

			System.out.print(String.format("%3d ==> ", i));
			for(int j = 0; j < nNum; j++)
			{
				if(i == nWinners[j])
					nCorrectCount++;

				System.out.print(String.format("%3d(%f), ", nWinners[j], m_dWinDistance[j]));
			}
			System.out.print("\n");
		}

		System.out.print(String.format("%d / %d\n", nCorrectCount, m_nDataNum));
	}

	public int[] TestByRefinedDB(double dPattern[], int nNum)
	{
		double dDistance[] = new double[m_nClusterNum];

		for(int i = 0; i < m_nClusterNum; i++)
		{
			dDistance[i] = 0;

			for(int j = 0; j < m_nDimensionNum; j++)
				dDistance[i] += m_dAlpha[i][j] * dPattern[j];
		}

		// 상위 nMaxRank 만큼
		int nTopRank[] = new int [nNum];
		for(int i = 0; i < nNum; i++)
		{
			int nIdx = -1;
			double dMin = 100000;
			for(int j = 0; j < m_nClusterNum; j++)
			{
				if(dDistance[j] < dMin)
				{
					nIdx = j;
					dMin = dDistance[j];
				}
			}

			dDistance[nIdx] = 10000;
			nTopRank[i] = nIdx;
			m_dWinDistance[i] = dMin;
		}

		int nResult[] = new int[nNum];

		for(int j = 0; j < nNum; j++)
			nResult[j] = nTopRank[j];

		return nResult;
	}

	public int[] Test(double dPattern[], int nNum, boolean bWeight)
	{
		int nWinners[] = GetWinners(dPattern, m_nClusterNum, bWeight);

		int nResult[] = new int[nNum];
		for(int j = 0; j < nNum; j++)
			nResult[j] = nWinners[j];

		return nResult;
	}

	double[] GetWinnerDistance()
	{
		return m_dWinDistance;
	}

	//Winner Node를 선정한다.
	private int[] GetWinners(double dPattern[], int nNum, boolean bWeight)
	{
		double dDistance[] = new double[m_nClusterNum];

		if(bWeight)
		{
			for(int i = 0; i < m_nClusterNum; i++)
				dDistance[i] = GetWeightDistance(dPattern, i);
		}
		else
		{
			for(int i = 0; i < m_nClusterNum; i++)
				dDistance[i] = GetDistance(dPattern, i);
		}

		// 상위 nMaxRank 만큼
		int nTopRank[] = new int [nNum];
		for(int i = 0; i < nNum; i++)
		{
			int nIdx = -1;
			double dMin = 100000;
			for(int j = 0; j < m_nClusterNum; j++)
			{
				if(dDistance[j] < dMin)
				{
					nIdx = j;
					dMin = dDistance[j];
				}
			}

			dDistance[nIdx] = 10000;
			nTopRank[i] = nIdx;
			m_dWinDistance[i] = dMin;
		}

		return nTopRank;
	}


	//Winner Node를 선정한다.
	private int GetWinner(int nPattern)
	{
		double dDistance = 10000.0;

		int nWin = -1;
		for(int i = 0; i < m_nClusterNum; i++)
		{
			double dMin = GetDistance(nPattern,  i);

			if(dDistance > dMin)
			{
				nWin = i;
				dDistance = dMin;
			}
		}

		return nWin;
	}

	void SymmetricMeasure()
	{
		System.out.println("");

		for(int j = 0; j < m_nClusterNum; j++)
			for(int i = 0; i < m_nDataNum; i++)
				m_dDistance[i][j] = GetDistance(i, j);

		double dAlpha;
		for(int j = 0; j < m_nDataNum; j++)
		{
			for(int k = 0; k < m_nDataNum; k++)
			{
				if(k == j)
					continue;

				int i = GetWinner(j);
				int l = GetWinner(k);

				if(i != l)	dAlpha = Alpha(i,l);
				else		dAlpha = 0;

				m_dU[j][i] = ((1 - dAlpha) * (1 - (GetAngle(i, j, k) / 180.0))) - (dAlpha * Ratio(i, j, k));
			}
			System.out.println(String.format("Symmetric Measure [ %d / %d ]", j+1, m_nDataNum));
		}
	}

	double Alpha(int i_index, int j_index)
	{
		double dDistance = 0.0;
		for(int i = 0; i < m_nDimensionNum; i++)
			dDistance += Math.pow(m_dV[i_index][i] - m_dV[j_index][i], 2);

		dDistance = Math.sqrt(dDistance);

		return dDistance / Math.sqrt(m_dFuzziness);
	}

	double Ratio(int i_nCluster, int i_nPattern, int j_nPattern)
	{
		double dDist1 = m_dDistance[i_nPattern][i_nCluster];
		double dDist2 = m_dDistance[j_nPattern][i_nCluster];

		if(dDist1 > dDist2)
		{
			if(dDist1 == 0)
				dDist1 = 1;
			return dDist2 / dDist1;
		}
		else
		{
			if(dDist2 == 0)
				dDist2 = 1;
			return dDist1 / dDist2;
		}
	}

	double GetAngle(int cluster,int i_pattern,int j_pattern)
	{
		double vector1[][] = new double[m_nClusterNum][m_nDimensionNum];
		double vector2[][] = new double[m_nClusterNum][m_nDimensionNum];

		for(int i = 0; i < m_nDimensionNum; i++)
		{
			vector1[cluster][i] = m_dX[i_pattern][i] - m_dV[cluster][i];
			vector2[cluster][i] = m_dX[j_pattern][i] - m_dV[cluster][i];
		}

		double dTemp = 0.0, dNormI = 0.0, dNormJ = 0.0;
		for(int i = 0; i < m_nDimensionNum; i++)
		{
			dTemp  += vector1[cluster][i] * vector2[cluster][i];
			dNormI += vector1[cluster][i] * vector1[cluster][i];
			dNormJ += vector2[cluster][i] * vector2[cluster][i];
			//dNormI += Math.pow(vector1[cluster][i], 2);
			//dNormJ += Math.pow(vector2[cluster][i], 2);
		}

		if(dTemp < 0)			dTemp = dTemp * -1;
		else if(dTemp == 0)		dTemp = 1;

		dNormI = Math.sqrt(dNormI);
		dNormJ = Math.sqrt(dNormJ);
		if(dNormI == 0)
			dNormI = 1;

		double angle = Math.acos(dTemp / (dNormI * dNormJ));

		if(angle < 0)	return 360.0 - angle;
		else			return angle;
	}

	public void JagaDataRefinement()
	{
		System.out.println("m_nDataNum : " + m_nDataNum);
		System.out.println("m_nDimensionNum : " + m_nDimensionNum);
		for(int j = 0; j < m_nDimensionNum; j++)
		{
			int nCount = 0;
			for(int i = 0; i < m_nDataNum; i++)
				if(m_dX[i][j] == 1)
					nCount++;

			if(nCount == 0)
				nCount = 1;

			for(int i = 0; i < m_nDataNum; i++)
				m_dAlpha[i][j] = m_dX[i][j] / nCount;
		}

		for(int i = 0; i < m_nDataNum; i++)
		{
			double dSum = 0;
			for(int j = 0; j < m_nDimensionNum; j++)
				dSum += m_dAlpha[i][j];

			for(int j = 0; j < m_nDimensionNum; j++)
				m_dAlpha[i][j] = 1.0 - (m_dAlpha[i][j] / dSum);
		}

		//SavaData("RefinedData.txt");
	}
}