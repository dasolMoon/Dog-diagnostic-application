package hajun.animaldiag;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;

public class LogoActivity extends Activity { //처음에 로고화면 띄워주고 디비구성하고,  PFCM 학습하고 결과값 저장하는 과정까지 하는 클래스

	Handler m_handler = new Handler();
	Thread m_thread = null;

	@Override
	protected void onDestroy() // 액티비티가 소멸되기 전 실행됨 isFinish()메소드로 사용자가 종료했는지, 시스템이 종료했는지 구분할 수 있다
			//이 액티비티는 로고 xml에서 사용되는 단편적인 액티비티같다. onDestroy메소드를 적용했더니 화면이 뜨자마자 해당 메세지가 나왔다.
	{
		if(m_thread != null && m_thread.isAlive())
			m_thread.interrupt();

		super.onDestroy();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) { //액티비티생성시 첫 1회 만들어짐
		super.onCreate(savedInstanceState);
		setContentView(R.layout.logo_layout);

		//Toast.makeText(getApplicationContext(), "테스트 메세지", Toast.LENGTH_SHORT).show(); // 이 액티비티 시작시 보여짐

		// 초기 변수 등을 정의함. 여기서 final은 지역변수를 전역변수로 만들 때 사용
		final SqlActivity myDbHelper = new SqlActivity(this); // SqlActivity  // 비어있는 데이터베이스를 만들고 자료가 있는 데이터베이스를 복사함
		final Share share = (Share) this.getApplicationContext();// Share 액티비티가 뭔지 이해 해야함 ..
		final Context context = this; //앞으로 쓰일 context는 this로 지정

		final TextView txtProgress = (TextView) findViewById(R.id.text_progress); // 프로그래스바 아래의 텍스트 객체 생성
		final ProgressBar progbar = (ProgressBar) findViewById(R.id.progressbar); // 프로그래스바 객체 생성
		progbar.setMax(1000); // 프로그래스바의 max값을 1000으로 설정

		m_thread = new Thread(new Runnable(){ // 액티비티 전역변수인 m_thread 에 새로 스레드 부여
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
