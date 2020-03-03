package business;

public class AutoInvest implements Runnable{

	@Override
	public void run() {
		// TODO Auto-generated method stub
		Invest.automaticInvest();
	}

}
