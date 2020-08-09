package hajun.animaldiag;

import android.app.Application;
import android.content.Context;
import android.database.SQLException;

import java.io.File;
import java.util.StringTokenizer;
import java.util.Vector;

public class Share extends Application {
	public int				m_nDisease;
	public int				m_nSymptom;
	public int				m_nBodyPart;
	public BeanDisease[]	m_disease; // 질병 클래스 객체
	public BeanSymptom[]	m_symptom; // 증상 클래스 객체
	public BeanBodyPart[] 	m_bodypart; // 1,2,3,4 몸체 객체
	public String[][] 		m_strSymptomBodyPart;
	
	public Vector<Vector<BeanSymptom>> m_vecSymptomPart;

	public double[]			m_dSelectedSymptom;
	public boolean			m_bSaveHistory;
	
	int						m_nSex;
	BeanDisease				m_DiseaseDetail;
	BeanSymptom				m_RemedyDetail;
	Vector<BeanSymptom> 	m_vecSelected = new Vector<BeanSymptom>(); //List 개념과 동일
	
	// FCM
	public HanBang		m_hanbang = new HanBang();
	
	// ART

	
	
	String m_strDataPath;// = "HFCM.DAT";

	public void LoadHanBangDatabase(Context context)
	{
		SqlActivity myDbHelper = new SqlActivity(context);
		try {
			myDbHelper.openDataBase();//

			m_disease = myDbHelper.LoadDisease(); // 질병 객체 할당 50 디비에서 가져오기때문에 동적으로 ㄱㄱ
			m_nDisease = m_disease.length;

			m_symptom = myDbHelper.LoadSymptom(); // 증상 객체 할당 92
			m_nSymptom = m_symptom.length;

			m_bodypart = myDbHelper.LoadBodyPart(); // 바디 객체 할당 4
			m_nBodyPart = m_bodypart.length;

			myDbHelper.close();
		}
		catch(SQLException sqle) {
			throw sqle;
		} //연동된 DB 데이터를 각 객체에 할당해주는 작업
		
		m_vecSymptomPart = new Vector<Vector<BeanSymptom>>();
		for(int i = 0; i < m_nBodyPart; i++)
		{
			Vector<BeanSymptom> beans = new Vector<BeanSymptom>();
			m_vecSymptomPart.add(beans);
		}
		
		for(int i = 0; i < m_nSymptom; i++)
		{
			if(m_symptom[i].nBodyPart == 0)
				continue;
			
			m_vecSymptomPart.get(m_symptom[i].nBodyPart-1).add(m_symptom[i]);
		} // 선택된 증상 갯수 만큼 Vector(C#으로 따지면 리스트배열)를 이용해서 동적으로 배열을 생성해주는 것인듯
		//엥저거뻥임
	}
	

	public boolean IsLearned() // 학습되었는가를 묻는 함수
	{
		/*
		File fp = getFileStreamPath(m_strDataPath); // 이 파일의 경로가 뭘까...
		return fp.exists();
		*/
		return m_hanbang.IsLearned();
	}
	
	public void HanBangLearning()
	{
		double dTrain[][] = GetTrainingData();
		
		m_hanbang.Learning(2.0, 0.00001, m_nDisease, m_nSymptom, m_nDisease, dTrain);

	}
	
	public void SaveHanBangData()
	{
		File fp = getFileStreamPath(m_strDataPath);

		m_hanbang.SaveData(fp);
	}
	
	public void LoadHanBangData()
	{
		double dTrain[][] = GetTrainingData();
		File fp = getFileStreamPath(m_strDataPath);

		m_hanbang.LoadData(fp, m_nDisease, m_nSymptom, m_nDisease, dTrain);
	}
	
	private double[][] GetTrainingData() //학습시킬 데이터
	{
		double[][] dTrainData = new double[m_nDisease][m_nSymptom]; // 질병 50  증상 92
		
		for(int i = 0; i < m_nDisease; i++)
		{
			String strSym = m_disease[i].strSymptom; //각 질병에 해당하는 증상부분을 String 타입으로 저장

			StringTokenizer tokens = new StringTokenizer(strSym.trim(), " "); // 띄워쓰기 구분자로 토큰 분리 질병에 여러가지 증상이 존재하는데 그걸 구분자로 분리하는 것

			while(tokens.hasMoreTokens()) // 다음 토큰이 없을 때 까지
			{
				int nSnum = Integer.parseInt(tokens.nextToken()) - 1; // String 타입으로 분리된 토큰을 int형으로 형변환(질병의 해당 증상을 체크하는 것인듯 ), -1을하는 이유는 배열의 인덱스로 사용하기 위함인듯
				dTrainData[i][nSnum] = 1;	// ex ) 4번 질병(지루증)에 증상 1, 51번의 2가지 증상이 있음; ex ) dTrainData[3][0] = 1, dTrainData[3][50] = 1;

			}
		}
		
		return dTrainData;
	}
	

	
	public void SetSelectedSymptom(double dSymptom[], boolean bSaveHistory)
	{
		m_dSelectedSymptom = dSymptom;
		m_bSaveHistory = bSaveHistory;
	}
	
	public double[] GetSelectedSymptom()
	{		
		return m_dSelectedSymptom;
	}

}
