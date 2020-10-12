package hajun.animaldiag;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import hajun.animaldiag.R;

public class DialogDiagnosis extends Dialog
{
	Share	m_share;
	Context m_context;
	Handler m_handler;

	public DialogDiagnosis(Context context, Handler handle) {
		super(context);

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.dialog_diag);

		m_context = context;
		m_handler = handle;

		m_share = (Share) context.getApplicationContext();

		Message msg = Message.obtain(m_handler);
		msg.what = 0;
		setDismissMessage(msg);
	}

	public void addSymptom(final int nBodyPart, int nSex)
	{
		LinearLayout view = (LinearLayout) findViewById(R.id.dialog_diag_layout);

		for(int i = 0; i < m_share.m_vecSymptomPart.get(nBodyPart).size(); i++)
		{
			final BeanSymptom bean = m_share.m_vecSymptomPart.get(nBodyPart).get(i);

			if(bean.nSex == 3 || bean.nSex == nSex)
			{
				final Button btn = new Button(m_context);
				btn.setTextColor(Color.rgb(0,0,40));
				btn.setBackgroundColor(Color.argb(32, 80, 60, 60));
				btn.setText(bean.strName);
				btn.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {

						// 중복 증상이면 삭제
						for(int i = 0; i < m_share.m_vecSelected.size(); i++)
						{
							if(m_share.m_vecSelected.get(i).strName.equals(bean.strName))
							{
								final int nIdx = i;

								AlertDialog.Builder builder = new AlertDialog.Builder(m_context);
								builder.setMessage(bean.strName + " 증상을 제거하시겠습니까?")
										.setCancelable(true)
										.setPositiveButton("삭제", new DialogInterface.OnClickListener() {
											public void onClick(DialogInterface dialog, int id) {
												m_share.m_vecSelected.remove(nIdx);
												Toast.makeText(m_context, bean.strName + " 증상이 삭제되었습니다.", Toast.LENGTH_SHORT).show();
											}
										})
										.setNegativeButton("취소", new DialogInterface.OnClickListener() {
											public void onClick(DialogInterface dialog, int id) {
											}
										});
								AlertDialog alert = builder.create();
								alert.show();

								return ;
							}
						}

						// 증상 추가
						AlertDialog.Builder builder = new AlertDialog.Builder(m_context);
						builder.setMessage(bean.strName + " 증상을 추가하시겠습니까?")
								.setCancelable(true)
								.setPositiveButton("추가", new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog, int id) {
										m_share.m_vecSelected.add(bean);
										Toast.makeText(m_context, bean.strName + " 증상이 추가되었습니다.", Toast.LENGTH_SHORT).show();
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

				view.addView(btn);
			}
		}
	}
}

