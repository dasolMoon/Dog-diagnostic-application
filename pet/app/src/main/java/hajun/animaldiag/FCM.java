package hajun.animaldiag;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Random;
import java.util.Vector;

public class FCM {

    private double m_dEpsilon;
    private double m_dFuzziness;
    private double m_dP;			// 2 / (m_dFuzziness - 1); // 퍼지화?
    private double a = 1,b = 1;

    private int m_nDataNum;  // 데이터 개수
    private int m_nClusterNum; // 클러스터 개수
    private int m_nDimensionNum; // 차원 갯수

    private double m_dX[][];		// Data, Dimension // [데이터][거리]
    private double m_dU[][];		// N, C 소속도
    private double m_dOU[][];       // 이전 소속도
    private double m_dV[];		// C
    private double m_dOV[];		// C
    private double m_dDistance[][];	// N, C //거리

    private double m_Percent[]; // 퍼센트
    private double m_dWinDistance[]; // 이긴 거리?(이긴 질병)
    int m_nRepeatCnt;
    boolean m_LearnState = false;


    public FCM() {
    }

    public void SetTrainData(int nDataNum, int nDimensionNum, int nClusterNum, double[][] dTrain)
    {
        m_nDataNum = nDataNum; // 데이터 갯수 초기와
        m_nDimensionNum = nDimensionNum; // 차원 초기화
        m_nClusterNum = nClusterNum; // 클러스터 개수

        InitialAlloc();

        m_dX = dTrain;
    }

    public void SaveFCMData(File fp) //FCM Data를 저장(클러스터 갯수, 차원 갯수, 중심값)
    {
        try {
            FileOutputStream fos = new FileOutputStream(fp);
            BufferedOutputStream bos = new BufferedOutputStream(fos);
            DataOutputStream dos = new DataOutputStream(bos);

            dos.writeInt(m_nClusterNum);
            dos.writeInt(m_nDimensionNum);


            for(int i = 0; i < m_nClusterNum; i++)
                    dos.writeDouble(m_dV[i]);

            dos.close();
            bos.close();
            fos.close();
        }
        catch (IOException e) {
            System.out.println("Save Data Failed.");
            e.printStackTrace();
        }
    }

    private void InitialAlloc() //데이터 초기화
    {
        m_dV 		= new double[m_nClusterNum]; //중심 값
        m_dOV 		= new double[m_nClusterNum]; //이전 중심 값
        m_dU 		= new double[m_nClusterNum][m_nDimensionNum]; // 소속도 값
        m_dOU 		= new double[m_nClusterNum][m_nDimensionNum]; // 이전 소속도 값
        m_dDistance = new double[m_nClusterNum][m_nDimensionNum]; // 거리 값
        m_dWinDistance = new double[m_nClusterNum]; //승자 거리값?

        m_Percent = new double[m_nClusterNum]; // 확률?

    }

    double Diff = 0.0; // 이전 중심과의 차이


    public void FCMLearning(double dFuzziness, double dEpsilon) // FCM 학습 진행
    {
        m_dFuzziness = dFuzziness; //퍼지 상수값
        m_dEpsilon = dEpsilon; // 임계값

        m_dP = 2 / (m_dFuzziness - 1); //목적 함수 퍼지 상수값

        Step1_Initialize(); // 소속도 초기화(랜덤)
        m_nRepeatCnt = 0;
        do
        {
            Step2_CalcClusterCenter();//중심값
            Step3_UpdateMembershipFunc();//새로운 소속도
            Diff = Step4_Termination();//새로운 중심값 과 이전 중심값 비교
        }
        while(Diff > m_dEpsilon);

        m_LearnState = true;

        System.out.println("FCM Learning Compelete....");
    }

    // 초기화(랜덤 소속도)
    private void Step1_Initialize()
    {
        Random rand = new Random();

        for(int i = 0; i < m_nClusterNum ; i++)
        {
            m_dOV[i] = 0; //이전 중심값
            m_dV[i] = 0; // 현재 중심값

            for(int j = 0; j < m_nDimensionNum; j++)
            {
                m_dU[i][j] = rand.nextDouble(); // 램덤 소속도
            }
        }

    }


    // 클러스터 중심 값 계산
    private void Step2_CalcClusterCenter()
    {
        for(int i = 0; i <m_nClusterNum ; i++) // 이전 중심값에 현재 중심값을 저장
            {
                m_dOV[i] = m_dV[i];
            }

        for(int j = 0; j < m_nClusterNum ; j++)
        {
            double sum = 0; // 분자합
            double sum2 = 0; // 분모합
            double of; // 목적 함수(Object Function)
            for(int k = 0; k < m_nDimensionNum; k++)
            {
                of = (a * Math.pow(m_dU[j][k], m_dFuzziness));
                sum += of * m_dX[j][k];
                sum2 += of;

            }
            m_dV[j] = sum/sum2;
            if(Double.isNaN(m_dV[j]))
                m_dV[j] = 0.0;
        }
    }

    // 멤버십 업데이트 및 최대 변화량
    private void Step3_UpdateMembershipFunc()
    {
        double sum = 0;
        double sum2 = 0;
        for(int j = 0; j < m_nClusterNum; j++) // 거리 계산
        {
            for(int i = 0; i < m_nDimensionNum; i++)
            {
                m_dDistance[j][i] = Math.pow(m_dX[j][i] - m_dV[j], 2);
            }

        }

        for (int i = 0 ; i <m_nClusterNum  ; i++) // 현재 소속도와 전형성을 저장(=이전 소속도, 전형성)
            for (int j = 0 ; j < m_nDimensionNum; j++)
            {
                m_dOU[i][j] = m_dU[i][j];
            }


        for (int i = 0 ; i <m_nClusterNum ; i++) // 새로운 소속도와 전형성을 갱신
            for (int j = 0 ; j < m_nDimensionNum; j++)
            {
                m_dU[i][j] = CalculateMembership(i,j);
            }
    }

    private double CalculateMembership(int clusterindex, int dataindex) // FCM 새로운 소속도
    {
        double sum = 0;
        for (int i = 0 ; i < m_nClusterNum; i++)
            sum += Math.pow((m_dDistance[clusterindex][dataindex] / m_dDistance[i][dataindex]), m_dFuzziness);

        return 1.0 / sum;
    }

    private double Step4_Termination() // 중심값 오차률
    {
        double Diff = 0.0;

        for (int i = 0; i < m_nClusterNum; i++)
                Diff += Math.abs(m_dV[i] - m_dOV[i]);//센터 값 차이
        return Diff;
    }

    double[] GetWinnerDistance()
    {
        return m_dWinDistance;
    }


    public int[] Classification(Vector<BeanSymptom> vecSelected, int nNum) // 질병 순위를 매기기위한 코드
    {
        for(int i = 0; i < m_nClusterNum; i++)
        {
            int nCount = 0;
            int tCount = 0;

            for (int j = 0; j < m_nDimensionNum; j++)
            {
                if (m_dX[i][j] == 1)
                {
                    tCount++;
                }
            }

            for (int j = 0; j < vecSelected.size(); j++)
            {
                if ((1.0 - m_dU[i][vecSelected.get(j).nNum - 1]) > 0.1) // 질병에 선택한 증상들이 몇개나 포함되었는지 카운트해주기 위한 for문과 if문
                {
                    nCount++;
                }
            }

            m_Percent[i] = ((double)nCount / vecSelected.size())*((double) nCount / tCount) * 100;
        }




        //크기순으로 순위를 매길건데 중요한거는 인덱스 값을 저장해야돼! 어떻게 해야 할까??
        double tempArr[] = m_Percent;
        int nTopRank[] = new int [nNum]; // 질병 순위를 매기기위한 배열


        for (int i = 0 ; i < nNum; i++)
        {
            int nIdx=0;
            double dMin = -1.0;

            for(int j = 0 ; j < m_nClusterNum; j++)
            {
                if(tempArr[j] > dMin)
                {
                    nIdx = j;
                    dMin = tempArr[j];
                }
            }

            tempArr[nIdx] = -1.0;
            nTopRank[i] = nIdx;
            m_dWinDistance[i] = dMin;
        }

        return nTopRank;
    }
}
