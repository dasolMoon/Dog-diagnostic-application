package hajun.animaldiag;
import android.util.Log;

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
import java.util.Vector;

public class PossFCM { //pfcm 액티비티

    private double m_dEpsilon;
    private double m_dFuzziness;
    private double m_dP;			// 2 / (m_dFuzziness - 1);
    private double a = 1,b = 1;

    private int m_nDataNum;
    private int m_nClusterNum;
    private int m_nDimensionNum;

    private double m_dX[][];		// Data, Dimension
    private double m_dU[][];		// N, C 소속도
    public double m_dT[][];        // N, C 전형성
    private double m_dOU[][];       // 이전 소속도
    private double m_dOT[][];       // 이전 전형성
    private double m_dV[];		// C
    private double m_dOV[];		// C
    private double m_dVolume[];    // PCM 부피
    private double m_dDistance[][];	// N, C

    private double m_Percent[];
    private double m_dWinDistance[];
    int m_nRepeatCnt;
    boolean m_LearnState = false;


    public PossFCM() //원희오빠 코드 넣을것
    {
    }

    public void SetTrainData(int nDataNum, int nDimensionNum, int nClusterNum, double[][] dTrain)
    {
        m_nDataNum = nDataNum; //데이터갯수
        m_nDimensionNum = nDimensionNum;
        m_nClusterNum = nClusterNum;

        InitialAlloc();

        m_dX = dTrain;
    }

    public void SavePCMData(File fp) //PCM Data를 저장?
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
        m_dT        = new double[m_nClusterNum][m_nDimensionNum];// 전형성 값
        m_dOT       = new double[m_nClusterNum][m_nDimensionNum];// 이전 전형성 값
        m_dVolume   = new double[m_nClusterNum];// PCM 부피
        m_dDistance = new double[m_nClusterNum][m_nDimensionNum]; // 거리 값
        m_dWinDistance = new double[m_nClusterNum]; //승자 거리값?

        m_Percent = new double[m_nClusterNum];

    }

    double Diff = 0.0;


    public void PFCMLearning(double dFuzziness, double dEpsilon) //  실제 pfcm 과정 - 메소드 호출로 진행
    {
        m_dFuzziness = dFuzziness; //퍼지 상수값
        m_dP = 2 / (m_dFuzziness - 1);
        m_dEpsilon = dEpsilon; // 엡실론

        Step1_Initialize();
        m_nRepeatCnt = 0;
        do
        {
            Step2_CalcClusterCenter();
            Step3_UpdateMembershipFunc();
            Diff = Step4_Termination();
        }
        while(Diff > m_dEpsilon);
        //while(m_nRepeatCnt < 10);
        m_LearnState = true;
        for (int i = 0 ; i < m_nClusterNum; i++)
            for (int j = 0 ; j < m_nDimensionNum; j++)
            {
                System.out.println("m_dT["+i+"]["+j+"] : " + m_dT[i][j]);
            }
        System.out.println("PFCM Learning Compelete....");
    }

    // 초기화
    private void Step1_Initialize()
    {
        Random rand = new Random();

        for(int i = 0; i < m_nClusterNum ; i++)
        {
            m_dOV[i] = 0;
            m_dV[i] = 0;
            for(int j = 0; j < m_nDimensionNum; j++)
            {
                m_dU[i][j] = rand.nextDouble();
                m_dT[i][j] = rand.nextDouble();
            }
        }

    }


    // 클러스터 중심 값 계산                                                                                                           ** 수정 요망
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
                of = (a * Math.pow(m_dU[j][k], m_dFuzziness)) + (b * Math.pow(m_dT[j][k], m_dFuzziness));// + (m_dVolume[j][k] * (1-m_dT[j][k])));
                sum += of * m_dX[j][k];
                sum2 += of;

            }
            m_dV[j] = sum/sum2;
            if(Double.isNaN(m_dV[j]))
                m_dV[j] = 0.0;
        }
    }

    // 멤버십 업데이트 및 최대 변화량  임게값에 비교할 값 계산부분
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
                m_dOT[i][j] = m_dT[i][j];
            }

       for (int i = 0 ; i <m_nClusterNum ; i++) // PCM 부피식 계산
       {
           for (int j = 0 ; j < m_nDimensionNum ; j++)
           {
               sum += Math.pow(m_dT[i][j], m_dFuzziness) * m_dDistance[i][j];
               sum2 += Math.pow(m_dT[i][j], m_dFuzziness);
           }
           m_dVolume[i] = sum / sum2;
       }


        for (int i = 0 ; i <m_nClusterNum ; i++) // 새로운 소속도와 전형성을 갱신                                                            ** 수정 요망
            for (int j = 0 ; j < m_nDimensionNum; j++)
            {
                m_dU[i][j] = CalculateMembership(i,j);
                m_dT[i][j] = 1.0 / (1 + (b * m_dDistance[i][j] / m_dVolume[i]));
            }
    }

    private double CalculateMembership(int clusterindex, int dataindex)//                                                           ** 수정 요망
    {
        double sum = 0;
        for (int i = 0 ; i < m_nClusterNum; i++)
            sum += Math.pow((m_dDistance[clusterindex][dataindex] / m_dDistance[i][dataindex]), m_dFuzziness);

        return 1.0 / sum;
    }

    private double Step4_Termination()
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


    public int[] Classification(Vector<BeanSymptom> vecSelected, int nNum)
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
                if ((1.0 - m_dT[i][vecSelected.get(j).nNum - 1]) > 0.1) // 질병에 선택한 증상들이 몇개나 포함되었는지 카운트해주기 위한 for문과 if문
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
