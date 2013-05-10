import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Scanner;

import SmartHome.*;

public class SmartHomeUI extends Ice.Application {

    private Scanner input = new Scanner(System.in);
    private static String userName = "";
    private Ice.Communicator communicator;
    private HomeManagerPrx hm;

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

    private static void menu() {
        System.out.println("\nWelcome to the Smart Home Monitoring System");
        System.out.println("Please select an option:");
        System.out.println("1. View temperature");
        System.out.println("x. Exit");
    }

    @Override
    public int run(String[] args) {
        if (args.length != 1) {
            System.err.println("java SmartHomeUI [port]");
            return 1;
        }
        Ice.ObjectPrx obj = communicator().stringToProxy("homemanager:tcp -h 127.0.0.1 -p " + args[0]);
        HomeManagerPrx hm = HomeManagerPrxHelper.uncheckedCast(obj);
        menu();

        java.io.BufferedReader in = new java.io.BufferedReader(new java.io.InputStreamReader(System.in));

        String line = null;
        do {
            try {
                System.out.print("==> ");
                System.out.flush();
                line = in.readLine();
                if (line == null) {
                    break;
                } else if (line.equals("1")) {
                    System.out.println("Current temperature is " + hm.currentTemperature());
                } else if (line.equals("s")) {
                    hm.shutdown();
                } else if (line.equals("x")) {
                    // Nothing to do
                } else if (line.equals("?")) {
                    menu();
                } else {
                    System.out.println("unknown command `" + line + "'");
                    menu();
                }
            } catch (java.io.IOException ex) {
                ex.printStackTrace();
            } catch (Ice.LocalException ex) {
                ex.printStackTrace();
            }
        } while (!line.equals("x"));

        return 0;

    }

    //command: java SmartHomeUI
    public static void main(String[] args) {
        SmartHomeUI app = new SmartHomeUI();
        int status = app.main("SmartHomeUI", args);
        System.exit(status);
    }

}
