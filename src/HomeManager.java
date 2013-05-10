import Ice.Current;
import SmartHome.*;

public class HomeManager extends Ice.Application {

    private int temperature, energy;

    @SuppressWarnings("serial")
    class HomeManagerI extends _HomeManagerDisp {
        public int currentTemperature(Ice.Current c) {
            return 28;
        }

        public void setTemperature(int _temperature, Ice.Current c) {
            temperature = _temperature;
        }

        public void shutdown(Current c) {
            System.out.println("Shutting down...");
            c.adapter.getCommunicator().shutdown();
        }

    }

    @Override
    public int run(String[] args) {
        if (args.length != 1) {
            System.err.println("java HomeManager [port]");
            return 1;
        }
        Ice.ObjectAdapter adapter = communicator().createObjectAdapterWithEndpoints("HomeManager", "tcp -h 127.0.0.1 -p "+args[0]);
        adapter.add(new HomeManagerI(), communicator().stringToIdentity("homemanager"));
        adapter.activate();
        communicator().waitForShutdown();
        communicator().destroy();
        
        // TODO Auto-generated method stub
        return 0;
    }

    static public void main(String[] args) {
        HomeManager s = new HomeManager();
        int status = s.main("HomeManager", args);
        System.exit(status);
    }

}
