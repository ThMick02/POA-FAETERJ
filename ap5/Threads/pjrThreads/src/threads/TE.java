package threads;

public class TE extends Thread {
	private int tempo;
	private String msg;

	public TE(int tempo, String msg) {
		super();
		this.tempo = tempo;
		this.msg = msg;
	}

	
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
