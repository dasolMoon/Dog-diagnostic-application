package hajun.animaldiag;

import java.io.File;
import java.io.*;
import java.util.Vector;

import android.util.Log;

public class HanBang{

	public PossCMeans m_PCM;
	public PossFCM m_PFCM = new PossFCM();;
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

/*
	// PCM 학습
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
	}

	// 진단 결과
	public void Diagnosis(double dPattern[], int nRankNum)
	{
		m_nResult = m_PCM.TestByRefinedDB(dPattern, nRankNum);

		//m_nResult = m_PCM.Test(dPattern, nRankNum, true);
		m_dResultDist = m_PCM.GetWinnerDistance();
	}
*/



	public boolean IsLearned()
	{
		return m_PFCM.m_LearnState;
	}

	// PFCM 학습
	public void Learning(double dFuzziness, double dEpsilon, int nData, int nDimension, int nCluster, double dTrain[][])
	{
		m_PFCM.SetTrainData(nData, nDimension, nCluster, dTrain);
		m_PFCM.PFCMLearning(dFuzziness, dEpsilon);
	}

	// 학습 결과 저장
	public void SaveData(File fp)
	{
		m_PFCM.SavePCMData(fp);
	}

	// 학습 결과 로드
	public void LoadData(File fp, int nData, int nDimension, int nCluster, double dTrain[][])
	{
		m_PFCM.SetTrainData(nData, nDimension, nCluster, dTrain);
	}

	// 진단 결과
	public void Diagnosis(double[] dPattern, int nRankNum, Vector<BeanSymptom> vecSelected) { //ReslutActivity 클래스에서 사용됨. 파라미터 값으로는 선택된 증상이 저장된 배열(92)과 질병 수(50)를 받아옴
		m_nResult = m_PFCM.Classification(vecSelected, nRankNum);
		m_dResultDist = m_PFCM.GetWinnerDistance();
	}


}
