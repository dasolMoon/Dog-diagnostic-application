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

public class LogoActivity extends Activity { //처음에 로고화면 띄워주고 디비구성하고, 학습하고 결과값 저장하는 과정까지 하는 클래스

	Handler m_handler = new Handler();
	Thread m_thread = null;

	//안드로이드 앱 생명 주기(강제종료)
	@Override
	protected void onDestroy()
	{
		if(m_thread != null && m_thread.isAlive())
			m_thread.interrupt();

		super.onDestroy();
	}


	//안드로이드 앱 생명 주기(앱 시작시 실행)
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.logo_layout); // 레이아웃 전개자

		//Toast.makeText(getApplicationContext(), "컴퓨터 공학과 안하준", Toast.LENGTH_SHORT).show(); //앱 시작 로딩시 뜨는 글귀

		final SQLiteHanBang myDbHelper = new SQLiteHanBang(this); // 데이터 베이스 접속
		final Share share = (Share) this.getApplicationContext(); // 데이터 준비
		final Context context = this; //?

		// 화면 출력
		final TextView txtProgress = (TextView) findViewById(R.id.text_progress);// 텍스트 출력
		final ProgressBar progbar = (ProgressBar) findViewById(R.id.progressbar);// 로딩 바
		progbar.setMax(1000);



		m_thread = new Thread(new Runnable(){
			@Override
			public void run() { // 스레드 1 시작

				// 데이터베이스 이전
				m_handler.post(new Runnable(){
					@Override
					public void run() {
						txtProgress.setText("애견 자가 진단 데이터베이스를 구성하고 있습니다.");
					}
				});
				// 데이터 베이스 호출 및 생성
				try {
					myDbHelper.createDataBase();
				} catch (IOException e) {
					e.printStackTrace();
				}

				try {
					Thread.sleep(500);// 5초간 스레드 정지
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

				// 데이터베이스 로드
				m_handler.post(new Runnable(){
					@Override
					public void run() {
						progbar.setProgress(150);// 로딩 바 150/1000
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
					share.LoadHanBangData();
				else
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
					share.HanBangLearning(); //  데이터 학습 및 저장

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
