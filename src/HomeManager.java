import Ice.Current;
import SmartHome.*;

public class HomeManager {

    private int temperature, energy;

    @SuppressWarnings("serial")
    class HomeManagerI extends _HomeManagerDisp {
        public int currentTemperature(Ice.Current c) {
            return 28;
        }

        public void shutdown(Current c) {
            System.out.println("Shutting down...");
            c.adapter.getCommunicator().shutdown();
        }
    }

    public HomeManager(String port) {
        Ice.Communicator communicator = Ice.Util.initialize();
        Ice.ObjectAdapter adapter = communicator.createObjectAdapterWithEndpoints("homemanager", "tcp -p " + port);
        adapter.add(new HomeManagerI(), communicator.stringToIdentity("homemanager"));
        adapter.activate();
        communicator.waitForShutdown();
        communicator.destroy();
    }

    public static void main(String[] args) {
        if (args.length != 1) {
            System.err.println("java HomeManager [port]");
            System.exit(1);
        }
        try {
            new HomeManager(args[0]);
        } catch (Exception ex) {
            ex.printStackTrace();
            System.exit(1);
        }
    }

}
