package threads;

public class TI implements Runnable {
	private int tempo;
	private String msg;

	public TI(int tempo, String msg) {
		super();
		this.tempo = tempo;
		this.msg = msg;
	}

	@Override
	public void run() {
		for (int i = 0; i < 100; i++) {
			try {
				Thread.sleep(tempo);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			System.out.println(msg);
		}

	}
}
