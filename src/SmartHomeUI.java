import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Scanner;
import SmartHome.*;

public class SmartHomeUI {

    private Scanner input = new Scanner(System.in);
    private static String userName = "";
    private Ice.Communicator communicator;
    private HomeManagerPrx hm;

    public SmartHomeUI(String port) throws Exception {
        String menuInput;

        if (checkUserCount() <= 2) {
            //ask user name
            while (userName.length() == 0) {
                System.out.print("\nPlease enter your user name: ");
                userName = input.nextLine().trim();
            }
        }

        communicator = Ice.Util.initialize();
        hm = HomeManagerPrxHelper.checkedCast(communicator.stringToProxy("homemanager:tcp -p " + port + " -h localhost"));

        while (true) {
            //main menu
            System.out.println("\nWelcome to the Smart Home Monitoring System");
            System.out.println("Please select an option:");
            System.out.println("1. View temperature");
            System.out.println("E. Exit");
            menuInput = input.nextLine();

            switch (menuInput.trim()) {
            case "1":
                viewTemperature(port);
                break;
            case "E":
                exitMenu();
            default:
                invalidCommand();
                break;
            }
        }
    }

    private void viewTemperature(String port) {
        System.out.println("Current temperature: " + hm.currentTemperature());
    }

    private void exitMenu() throws IOException {
        exitUserCount();
        //if this is the last instance of SmartHomeUI running...
        //shutdown everything
        hm.shutdown();
        communicator.destroy();
        System.exit(0);
    }

    private void invalidCommand() {
        System.out.println("\nInvalid command. Press Enter to continue.");
        input.nextLine();
    }

    private int checkUserCount() throws IOException {
        RandomAccessFile file;
        String line;
        int numUsers = 0;

        //write into user count log file; add one more user
        file = new RandomAccessFile("usercount.log", "rw");
        if ((line = file.readLine()) != null) {
            file.seek(0);
            numUsers = Integer.parseInt(line.trim()) + 1;
            if (numUsers <= 2) {
                file.writeChars(Integer.toString(numUsers));
            } else {
                System.out.println("Usage exceeds maximum number of simultaneous connection allowed.");
            }
        } else {
            file.writeChars("1");
        }
        file.close();
        return numUsers;
    }

    private void exitUserCount() throws IOException {
        RandomAccessFile file;
        String line;

        //check if there is any instance of SmartHomeUI running...
        file = new RandomAccessFile("usercount.log", "rw");
        if ((line = file.readLine()) != null) {
            file.seek(0);
            file.writeChars(Integer.toString(Integer.parseInt(line.trim()) - 1));
        } else {
            file.writeChars("0");
        }
        file.close();
    }

    //command: java SmartHomeUI
    public static void main(String[] args) {
        if (args.length != 1) {
            System.err.println("java SmartHomeUI [port]");
            System.exit(1);
        }
        try {
            new SmartHomeUI(args[0]);
        } catch (Exception ex) {
            ex.printStackTrace();
            System.exit(1);
        }
    }

}
