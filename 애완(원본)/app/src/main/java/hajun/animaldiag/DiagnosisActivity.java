package hajun.animaldiag;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import java.util.Vector;

import hajun.animaldiag.R;

public class DiagnosisActivity extends ListActivity {

	Share	m_share;
	ItemAdapter m_adapter = null;
	boolean m_bFront = true;

	@SuppressLint("HandlerLeak")
	Handler m_handler = new Handler(){
		@Override
		public void handleMessage(Message msg){
			SetSelectedList();
			for(int i = 0; i < m_share.m_vecSelected.size(); i++)
				System.out.println(m_share.m_vecSelected.get(i).strName);
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		m_share = (Share) this.getApplicationContext();
		m_share.m_vecSelected.clear();

		setContentView(R.layout.diagnosis_layout);

		m_adapter = new ItemAdapter(this, R.layout.diagnosis_item, m_share.m_vecSelected);
		setListAdapter(m_adapter);

		SetButtons();
	}

	public void SetSelectedList()
	{
		// 중복 제거
		for(int i = 0; i < m_share.m_vecSelected.size(); i++)
		{
			for(int j = 0; j < m_share.m_vecSelected.size(); j++)
			{
				if(i == j)
					continue;

				if(m_share.m_vecSelected.get(i).nNum == m_share.m_vecSelected.get(j).nNum)
					m_share.m_vecSelected.remove(j);
			}
		}

		m_adapter.notifyDataSetChanged();
	}

	class ItemAdapter extends ArrayAdapter<BeanSymptom> {
		private Vector<BeanSymptom> items;

		public ItemAdapter(Context context, int textViewResourceId, Vector<BeanSymptom> items){
			super(context, textViewResourceId, items);
			this.items = items;
		}

		public View getView(final int position, View convertView, ViewGroup parent) {
			View v = convertView;
			if (v == null) {
				LayoutInflater vi = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				v = vi.inflate(R.layout.diagnosis_item, null);
			}

			final BeanSymptom p = items.get(position);

			if (p != null)
			{
				TextView txtIdx = (TextView) v.findViewById(R.id.diagnosis_symname);
				txtIdx.setText(String.valueOf(p.strName));
				txtIdx.setTextColor(Color.BLACK);
			}

			v.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					AlertDialog.Builder builder = new AlertDialog.Builder(DiagnosisActivity.this);
					builder.setMessage(p.strName + " 증상을 삭제하시겠습니까?")
							.setCancelable(true)
							.setPositiveButton("삭제", new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog, int id) {
									items.remove(position);
									notifyDataSetChanged();
								}
							})
							.setNegativeButton("취소", new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog, int id) {
								}
							});
					AlertDialog alert = builder.create();
					alert.show();
				}
			});

			return v;
		}
	}

	public void SetButtons()
	{
		final ViewFlipper flipper = (ViewFlipper) findViewById(R.id.flipper);
		flipper.setDisplayedChild(0);

		Button btnFlipper = (Button) findViewById(R.id.btnFlipper);
		btnFlipper.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				flipper.showNext();
			}
		});

		Button btnDiagnosis = (Button) findViewById(R.id.btnDiagonsis_diag);
		btnDiagnosis.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {

				if(m_share.m_vecSelected.size() == 0)
				{
					Toast.makeText(DiagnosisActivity.this, "선택된 증상이 없습니다.\n먼저 불편하신 부위를 선택하시고, 세부 증상을 선택해주시기 바랍니다.", Toast.LENGTH_LONG).show();
					return ;
				}

				double dPattern[] = new double[m_share.m_nSymptom];
				for(int i = 0; i < m_share.m_nSymptom; i++)
					dPattern[i] = 0;

				for(int i = 0; i < m_share.m_vecSelected.size(); i++)
					dPattern[m_share.m_vecSelected.get(i).nNum-1] = 1;

				m_share.SetSelectedSymptom(dPattern, true);

				Intent intent = new Intent(DiagnosisActivity.this, ResultActivity.class);
				startActivity(intent);
				overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
			}
		});
		Button btnDiagBody = (Button) findViewById(R.id.btn_diag_inner);
		btnDiagBody.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				DialogDiagnosis dialog = new DialogDiagnosis(DiagnosisActivity.this, m_handler);
				dialog.addSymptom(0, m_share.m_nSex);
				dialog.show();
			}
		});

		Button btnDiagMental = (Button) findViewById(R.id.btn_diag_body);
		btnDiagMental.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				DialogDiagnosis dialog = new DialogDiagnosis(DiagnosisActivity.this, m_handler);
				dialog.addSymptom(1, m_share.m_nSex);
				dialog.show();
			}
		});
		Button btnDiagJoint = (Button) findViewById(R.id.btn_diag_face);
		btnDiagJoint.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				DialogDiagnosis dialog = new DialogDiagnosis(DiagnosisActivity.this, m_handler);
				dialog.addSymptom(2, m_share.m_nSex);
				dialog.show();
			}
		});

		Button btnDiagSkin = (Button) findViewById(R.id.btn_diag_leg);
		btnDiagSkin.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				DialogDiagnosis dialog = new DialogDiagnosis(DiagnosisActivity.this, m_handler);
				dialog.addSymptom(3, m_share.m_nSex);
				dialog.show();
			}
		});
	}
}