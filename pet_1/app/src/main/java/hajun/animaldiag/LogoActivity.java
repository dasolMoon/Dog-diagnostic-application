package hajun.animaldiag;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;

import hajun.animaldiag.R;

public class LogoActivity extends Activity { //처음에 로고화면 띄워주고 디비구성하고,  PFCM 학습하고 결과값 저장하는 과정까지 하는 클래스

	Handler m_handler = new Handler();
	Thread m_thread = null;

	@Override
	protected void onDestroy()
	{
		if(m_thread != null && m_thread.isAlive())
			m_thread.interrupt();

		super.onDestroy();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.logo_layout);


		//Toast.makeText(getApplicationContext(), "컴퓨터 공학과 안하준", Toast.LENGTH_SHORT).show(); //앱 시작 로딩시 뜨는 글귀

		final SQLiteHanBang myDbHelper = new SQLiteHanBang(this);
		final Share share = (Share) this.getApplicationContext();
		final Context context = this;

		final TextView txtProgress = (TextView) findViewById(R.id.text_progress);
		final ProgressBar progbar = (ProgressBar) findViewById(R.id.progressbar);
		progbar.setMax(1000);

		m_thread = new Thread(new Runnable(){
			@Override
			public void run() {

				// 데이터베이스 이전
				m_handler.post(new Runnable(){
					@Override
					public void run() {
						txtProgress.setText("애견 자가 진단 데이터베이스를 구성하고 있습니다.");
					}
				});

				try {
					myDbHelper.createDataBase();
				} catch (IOException e) {
					e.printStackTrace();
				}

				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

				// 데이터베이스 로드
				m_handler.post(new Runnable(){
					@Override
					public void run() {
						progbar.setProgress(150);
						txtProgress.setText("데이터를 불러오고 있습니다.");
					}
				});

				share.LoadHanBangDatabase(context);

				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

				// 한방 학습
				if(share.IsLearned())
					share.LoadHanBangData();//여기까지만 디버깅에서 출력값이 나오고... 뒤에 값은 안나오는데;;
				else //else라서 자체가 안들어가네...... 슈발 그렇다면 위에 if값이 항상 참이라는 것인데...
				{
					m_handler.post(new Runnable(){
						@Override
						public void run() {
							progbar.setProgress(300);
							txtProgress.setText("최초 1회, 애견 자가 진단을 위한 학습을 합니다.\n학습에는 약 3분의 시간이 소요됩니다.\n(기기에 따라 차이가 있을 수 있습니다.)");
						}
					});

					// 프로그레스바 증가를 위한 Thread
					Thread threadProg = new Thread(new Runnable(){
						@Override
						public void run() {
							for(int i = 0; i < 700; i++)
							{
								try {
									Thread.sleep(300);
								} catch (InterruptedException e) {
									e.printStackTrace();
									break;
								}
								final int nCount = 300 + i;
								m_handler.post(new Runnable(){
									@Override
									public void run() {
										progbar.setProgress(nCount);
									}
								});
							}
						}
					});
					threadProg.start();
					//여기까지 로딩 끝
					System.out.println("한방러닝");
					share.HanBangLearning(); // pcm, pfcm 학습
					//share.SaveHanBangData(); // 학습 데이터 저장

/*
					if(threadProg.isAlive())
						threadProg.interrupt();//여기서 왜 스레드 인터럽??*/
				}

				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

				m_handler.post(new Runnable(){
					@Override
					public void run() {
						progbar.setProgress(1000);
						txtProgress.setText("애견 자가 진단 서비스를 시작합니다.");
					}
				});

				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

				Intent intent = new Intent(context, DiagnosisActivity.class);
				startActivity(intent);

				overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);

				finish();
			}
		});
		m_thread.start();
	}
}
