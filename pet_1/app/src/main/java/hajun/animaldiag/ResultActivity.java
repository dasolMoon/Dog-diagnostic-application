package hajun.animaldiag;

import android.app.Activity;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import java.util.StringTokenizer;

import hajun.animaldiag.R;

public class ResultActivity extends Activity {

	Share	m_share;
	PossFCM m_pfcm;

	int		m_nResultNum = 10;

	BeanDisease[] m_disease = new BeanDisease[m_nResultNum];
	double[] m_dDist = new double[m_nResultNum];

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.result_layout);

		m_share = (Share) this.getApplicationContext();

		double[] dPattern = m_share.GetSelectedSymptom(); //증상 배열을 불러와서 저장

		m_share.m_hanbang.Diagnosis(dPattern, m_share.m_nDisease, m_share.m_vecSelected);//증상 배열과 질병 수를 파라미터 값으로 보내줌 왜지?? -> 밑의 함수들의 데이터 셋을 맞추기 위해서 연산을 수행하네
		int[] nIdx = m_share.m_hanbang.GetResult(); //인덱스가 저장된 배열
		m_dDist = m_share.m_hanbang.GetResultDist();

		for(int i = 0; i < m_nResultNum; i++)
			m_disease[i] = m_share.m_disease[nIdx[i]];

		DisplayResult();
	}

	public void DisplayResult()
	{
		final ZoomTextView txtExplanation = (ZoomTextView) findViewById(R.id.txt_result_explanation);

		Button btnKCD[] = new Button[m_nResultNum];
		TextView txtKCDPercent[] = new TextView[m_nResultNum];

		btnKCD[0] = (Button) findViewById(R.id.btn_result_KCD1);
		btnKCD[1] = (Button) findViewById(R.id.btn_result_KCD2);
		btnKCD[2] = (Button) findViewById(R.id.btn_result_KCD3);
		btnKCD[3] = (Button) findViewById(R.id.btn_result_KCD4);
		btnKCD[4] = (Button) findViewById(R.id.btn_result_KCD5);
		btnKCD[5] = (Button) findViewById(R.id.btn_result_KCD6);
		btnKCD[6] = (Button) findViewById(R.id.btn_result_KCD7);
		btnKCD[7] = (Button) findViewById(R.id.btn_result_KCD8);
		btnKCD[8] = (Button) findViewById(R.id.btn_result_KCD9);
		btnKCD[9] = (Button) findViewById(R.id.btn_result_KCD10);

		txtKCDPercent[0] = (TextView) findViewById(R.id.txt_result_KCD1_percent);
		txtKCDPercent[1] = (TextView) findViewById(R.id.txt_result_KCD2_percent);
		txtKCDPercent[2] = (TextView) findViewById(R.id.txt_result_KCD3_percent);
		txtKCDPercent[3] = (TextView) findViewById(R.id.txt_result_KCD4_percent);
		txtKCDPercent[4] = (TextView) findViewById(R.id.txt_result_KCD5_percent);
		txtKCDPercent[5] = (TextView) findViewById(R.id.txt_result_KCD6_percent);
		txtKCDPercent[6] = (TextView) findViewById(R.id.txt_result_KCD7_percent);
		txtKCDPercent[7] = (TextView) findViewById(R.id.txt_result_KCD8_percent);
		txtKCDPercent[8] = (TextView) findViewById(R.id.txt_result_KCD9_percent);
		txtKCDPercent[9] = (TextView) findViewById(R.id.txt_result_KCD10_percent);

		for(int i = 0; i < m_nResultNum; i++)
		{
			final int nIdx = i;
			//String strPer = String.format("%.2f%%", 100-(m_dDist[i]/m_dDist[m_share.m_nDisease-1]*100)); //이 부분이 질병들의 확률 값을 출력해주는 부분인듯
			String strPer = String.format("%.2f%%", m_dDist[i]); //이 부분이 질병들의 확률 값을 출력해주는 부분인듯
			txtKCDPercent[nIdx].setText(strPer);


			String str = String.format("<font color=\"#000000\"><b>[진단명]</b></font><br>%s<br><br><font color=\"#000000\"><b>[증상]</b></font><br>", m_disease[nIdx].strName);

			StringTokenizer tokens = new StringTokenizer(m_disease[nIdx].strSymptom, " ");
			while(tokens.hasMoreTokens())
			{
				int nSymIdx = Integer.parseInt(tokens.nextToken());

				boolean bSameSym = false;
				for(int k = 0; k < m_share.m_vecSelected.size(); k++)
				{
					if(m_share.m_vecSelected.get(k).nNum == nSymIdx)
					{
						bSameSym = true;
						break;
					}
				}
				nSymIdx--;

				if(bSameSym)
					str += "<font color=\"#000000\"><u>" + m_share.m_symptom[nSymIdx].strName + "</u></font>, ";
				else
					str += m_share.m_symptom[nSymIdx].strName + ", ";
			}
			str = str.substring(0, str.length()-2);

			str += String.format("<br><br><font color=\"#000000\"><b>[원인]</b></font><br>%s", m_disease[nIdx].strFactor);
			str += String.format("<br><br><font color=\"#000000\"><b>[%s이란]</b></font><br>%s", m_disease[nIdx].strName, m_disease[nIdx].strExplanation);

			final String strResult = str;

			btnKCD[nIdx].setText(String.format("%d. ", i+1) + m_disease[nIdx].strName);
			btnKCD[nIdx].setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {txtExplanation.setText(Html.fromHtml(strResult));}
			});

			// 확률 변경해야함.....................................

			if(i == 0)
				txtExplanation.setText(Html.fromHtml(strResult));

		}
	}
}
