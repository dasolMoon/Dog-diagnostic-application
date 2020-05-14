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
	public BeanDisease[]	m_disease;
	public BeanSymptom[]	m_symptom;
	public BeanBodyPart[] 	m_bodypart;
	public String[][] 		m_strSymptomBodyPart;
	
	public Vector<Vector<BeanSymptom>> m_vecSymptomPart;

	public double[]			m_dSelectedSymptom;
	public boolean			m_bSaveHistory;
	
	int						m_nSex;
	BeanDisease				m_DiseaseDetail;
	BeanSymptom				m_RemedyDetail;
	Vector<BeanSymptom> 	m_vecSelected = new Vector<BeanSymptom>();
	
	// FCM
	public HanBang		m_hanbang = new HanBang();
	
	// ART

	
	
	String m_strDataPath = "HFCM.DAT";

	public void LoadHanBangDatabase(Context context)
	{
		SQLiteHanBang myDbHelper = new SQLiteHanBang(context);
		try {
			myDbHelper.openDataBase();

			m_disease = myDbHelper.LoadDisease();
			m_nDisease = m_disease.length;

			m_symptom = myDbHelper.LoadSymptom();
			m_nSymptom = m_symptom.length;

			m_bodypart = myDbHelper.LoadBodyPart();
			m_nBodyPart = m_bodypart.length;
			
			myDbHelper.close();
		}
		catch(SQLException sqle) {
			throw sqle;
		}
		
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
		}
	}
	
	// �н� ���� ���� ���� ����
	public boolean IsLearned()
	{
		File fp = getFileStreamPath(m_strDataPath);
		return fp.exists();
	}
	
	public void HanBangLearning()
	{
		// Data ����
		double dTrain[][] = GetTrainingData();
		
		// �н�
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
	
	private double[][] GetTrainingData()
	{
		double[][] dTrainData = new double[m_nDisease][m_nSymptom];
		
		for(int i = 0; i < m_nDisease; i++)
		{
			String strSym = m_disease[i].strSymptom;
			
			StringTokenizer tokens = new StringTokenizer(strSym.trim(), " ");
			
			while(tokens.hasMoreTokens())
			{
				int nSnum = Integer.parseInt(tokens.nextToken()) - 1;
				dTrainData[i][nSnum] = 1;	// ��°� �ʱ�ȭ
			}
		}
		
		return dTrainData;		
	}
	
	public void SetSelectedSymptom(String strSymptom, boolean bSaveHistory)
	{
		m_dSelectedSymptom = new double[m_nSymptom];
		
		StringTokenizer tokens = new StringTokenizer(strSymptom, ",");
		while(tokens.hasMoreTokens())
		{
			int nSymptom = Integer.parseInt(tokens.nextToken().trim()) - 1;
			m_dSelectedSymptom[nSymptom] = 1;
		}
		m_bSaveHistory = bSaveHistory;
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
	
	public String GetSymptomNameFromNumber(String strNum)
	{
		String strName = "";
		
		StringTokenizer tokens = new StringTokenizer(strNum, " ");
		while(tokens.hasMoreTokens())
		{
			int nSymptom = Integer.parseInt(tokens.nextToken().trim()) - 1;
			
			strName += m_symptom[nSymptom].strName + ", ";
		}
		strName = strName.substring(0, strName.length()-2);	
		
		return strName;
	}
}
