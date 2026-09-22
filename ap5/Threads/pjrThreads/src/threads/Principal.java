package threads;

public class Principal {

	public static void main(String[] args) {
		TE t1 = new TE(1000, "Tic");
		TE t2 = new TE(1000, "Tac");
		t1.start();
		t2.start();
		TI t3 = new TI(2000,"Ping");
		TI t4 = new TI(2000,"Pong");
		
		
		
		Thread t3t = new Thread(t3);
		Thread t4t = new Thread(t4);
		t3t.start();
		t4t.start();
		
	}

}
