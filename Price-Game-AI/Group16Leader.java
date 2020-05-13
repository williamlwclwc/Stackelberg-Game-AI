import comp34120.ex2.PlayerImpl;
import comp34120.ex2.PlayerType;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

final class Group16Leader
	extends PlayerImpl
{
	/* The randomizer used to generate random price */
	private final Random m_randomizer = new Random(System.currentTimeMillis());

	private float[] historicalDataLeader = new float[131];
	private float[] historicalDataFollower = new float[131];
	private float aStar;
	private float bStar;
	private int windowSize = 100;
	private int currentDay = 100;
	
	@Override
	public void startSimulation(int p_steps)
		throws RemoteException
	{
		// collect historical data
		for (int day = 1; day <= 100; day++)
		{
			historicalDataLeader[day] = m_platformStub.query(m_type, day).m_leaderPrice;
			historicalDataFollower[day] = m_platformStub.query(m_type, day).m_followerPrice;
		}
		currentDay = 100;		
	}

	@Override
	public void endSimulation()
		throws RemoteException
	{
		// calculate accumulated profit
		m_platformStub.log(m_type, "Accumulated Profit: " + String.valueOf(calculateProfit()));	
	}

	public void calculateReactionFunction(int lastDay)
	{
		float xSum = 0;
		float ySum = 0;
		float xySum = 0;
		float xSquaredSum = 0;
		float x = 0;
		float y = 0;
		
		int newWindowSize;

		// check to see if there are enough data for this window size
		if (windowSize > lastDay)
			newWindowSize = lastDay;
		else
			newWindowSize = windowSize;

		// calculate reaction function
		for (int day = 1; day <= newWindowSize; day++)
		{
			x = historicalDataLeader[lastDay - newWindowSize + day];
			y = historicalDataFollower[lastDay - newWindowSize + day];
			xSum = xSum + x;
			ySum = ySum + y;
			xSquaredSum = xSquaredSum + x*x;
			xySum = xySum + x*y;
		}
		aStar = (xSquaredSum*ySum - xSum*xySum) / (newWindowSize*xSquaredSum - xSum*xSum);
		bStar = (newWindowSize*xySum - xSum*ySum) / (newWindowSize*xSquaredSum - xSum*xSum);
	}

	public float calculateBestStrategy()
	{
		// calculate best strategy
		return (float)((-3 - 0.3*aStar + 0.3*bStar)/(-2 + 0.6*bStar));
	}

	public float calculateProfit() throws RemoteException
	{
		float totalProfit = 0;
		float x = 0;
		float y = 0;
		
		// calculate accumulated profit
		for (int i = 101; i <= currentDay; i++)
		{
			x = m_platformStub.query(m_type, i).m_leaderPrice;
			y = m_platformStub.query(m_type, i).m_followerPrice;
			
			totalProfit = totalProfit + (float)((x - 1)*(2 - x + 0.3*y));
		}
		
		return totalProfit;
	}

	private Group16Leader()
		throws RemoteException, NotBoundException
	{
		super(PlayerType.LEADER, "Group16 Leader");
	}

	@Override
	public void goodbye()
		throws RemoteException
	{
		ExitTask.exit(500);
	}

	/**
	 * To inform this instance to proceed to a new simulation day
	 * @param p_date The date of the new day
	 * @throws RemoteException
	 */
	@Override
	public void proceedNewDay(int p_date)
		throws RemoteException
	{
		// update historical data with the previous day data
		historicalDataLeader[p_date - 1] = m_platformStub.query(m_type, p_date - 1).m_leaderPrice;
		historicalDataFollower[p_date - 1] = m_platformStub.query(m_type, p_date - 1).m_followerPrice;

		calculateReactionFunction(p_date - 1);
		float bestStrategy = calculateBestStrategy();
		m_platformStub.publishPrice(m_type, (float)bestStrategy);
		currentDay = p_date;
	}

	/**
	 * Generate a random price based Gaussian distribution. The mean is p_mean,
	 * and the diversity is p_diversity
	 * @param p_mean The mean of the Gaussian distribution
	 * @param p_diversity The diversity of the Gaussian distribution
	 * @return The generated price
	 */
	private float genPrice(final float p_mean, final float p_diversity)
	{
		return (float) (p_mean + m_randomizer.nextGaussian() * p_diversity);
	}

	public static void main(final String[] p_args)
		throws RemoteException, NotBoundException
	{
		new Group16Leader();
	}

	/**
	 * The task used to automatically exit the leader process
	 * @author Xin
	 */
	private static class ExitTask
		extends TimerTask
	{
		static void exit(final long p_delay)
		{
			(new Timer()).schedule(new ExitTask(), p_delay);
		}
		
		@Override
		public void run()
		{
			System.exit(0);
		}
	}
}
