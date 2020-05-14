package hajun.animaldiag;

import java.io.File;

public class HanBang{

	public PossCMeans m_PCM;
	protected int m_nResult[];
	protected double m_dResultDist[];

	public HanBang()
	{
	}

	// 진단 결과 리턴
	public int[] GetResult()
	{
		return m_nResult;
	}

	public double[] GetResultDist()
	{
		return m_dResultDist;
	}
	// 학습
	public void Learning(double dFuzziness, double dEpsilon, int nData, int nDimension, int nCluster, double dTrain[][])
	{
		m_PCM = new PossCMeans();
		m_PCM.SetTrainData(nData, nDimension, nCluster, dTrain);
		m_PCM.JagaDataRefinement();
		m_PCM.PCMLearning(dFuzziness, dEpsilon);
	}

	// 학습 결과 저장
	public void SaveData(File fp)
	{
		m_PCM.SavePCMData(fp);
	}

	// 학습 결과 로드
	public void LoadData(File fp, int nData, int nDimension, int nCluster, double dTrain[][])
	{
		m_PCM = new PossCMeans();
		m_PCM.SetTrainData(nData, nDimension, nCluster, dTrain);
		m_PCM.JagaDataRefinement();

		m_PCM.LoadPCMData(fp);
	}

	// 진단 결과
	public void Diagnosis(double dPattern[], int nRankNum)
	{
		m_nResult = m_PCM.TestByRefinedDB(dPattern, nRankNum);

		//m_nResult = m_PCM.Test(dPattern, nRankNum, true);
		m_dResultDist = m_PCM.GetWinnerDistance();
	}
}
