import java.io.RandomAccessFile;

import Ice.Current;
import SmartHome.*;

public class Sensor extends Ice.Application {

    private boolean shutdown = false, suspend = false;
    private int oldTemperature;
    HomeManagerPrx hm;
    
    @SuppressWarnings("serial")
    class SensorI extends _SensorDisp {
        public void shutdown(Current c) {
            System.out.println("Shutting down...");
            shutdown = true;
            c.adapter.getCommunicator().shutdown();
        }
    }

    // the sensor reading loop, keep on reading a sensor/tracker data file (if EOF go back to BOF) and publish its
    // content via Ice, one line per second
    // until receiving a shutdown request from HomeManager
    private void sensorRead(String type, RandomAccessFile file) throws Exception {

        int runningTime, totalSeconds, itemperature;
        String[] fields;

        while (!shutdown) {
            runningTime = 0;
            // read a line from the sensor file and separate it by commas
            fields = getData(file).split(",");
            // assuming the line contains two fields
            assert (fields.length == 2);
            fields[0] = fields[0].trim();
            fields[1] = fields[1].trim();
            // line[0] = sensor value, line[1] = sensor reading time
            totalSeconds = Integer.parseInt(fields[1]);
            while (runningTime++ < totalSeconds) {
                if (type.compareTo("temperature") == 0) {
                    if (suspend) {
                        //Home Manager wants the temperature sensor to stop sending the readings
                        //until the temperature has changed and it is outside the range of 15 to 28 degrees
                        itemperature = Integer.parseInt(fields[0]);
                        if (itemperature != oldTemperature && (itemperature < 15 || itemperature > 28)) {
                            suspend = false;
                            oldTemperature = itemperature;
                            //sendNotification("temperature", fields[0]);
                            hm.setTemperature(itemperature);
                        }
                    } else {
                        //no suspension? just send it...
                        //sendNotification("temperature", fields[0]);
                    }
                    System.out.println("=> temperature = " + fields[0] + (suspend ? " (not published)" : "") + "; old = " + Integer.toString(oldTemperature));
                } else if (type.compareTo("energy") == 0) {
                    System.out.println("=> energy usage = " + fields[0]);
                    //sendNotification("energy", fields[0]);
                } else if (type.endsWith("Location")) {
                    System.out.println("=> user = " + type.substring(0, type.length() - 8) + "; location = " + fields[0]);
                    //sendNotification("location", type.substring(0, type.length() - 8), fields[0]);
                }
                makeSleep(1000); // produce a reading every one second
            }
        }
    }

    private void sendNotification() {
        //Ice.ObjectPrx obj = communicator().stringToProxy("homemanager:tcp -h 127.0.0.1 -p " + args[0]);
        //HomeManagerPrx hm = HomeManagerPrxHelper.uncheckedCast(obj);
    }

    // read a line from an random access file (text file)
    private String getData(RandomAccessFile file) throws Exception {
        String line;
        // return the current line if it's not EOF yet and move the pointer to
        // the next line
        if ((line = file.readLine()) != null) {
            return line;
        }
        // if it's EOF, return to BOF and return the first line
        file.seek(0);
        return file.readLine();
    }

    // pause for a couple of milliseconds
    private void makeSleep(int milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void main(String[] args) {
        Sensor app = new Sensor();
        int status = app.main("Sensor", args);
        System.exit(status);
    }

    @Override
    public int run(String[] args) {
        if (args.length != 3) {
            System.err.println("java Sensor [type][predefined-data-file][port]");
            return 1;
        }
        Ice.ObjectPrx obj = communicator().stringToProxy("homemanager:tcp -h 127.0.0.1 -p " + args[2]);
        HomeManagerPrx hm = HomeManagerPrxHelper.uncheckedCast(obj);
        try {
            RandomAccessFile file = new RandomAccessFile(args[1], "r");
            //publishing sensor/tracker reading
            if (args[0].compareTo("temperature") == 0) {
                System.out.println("Temperature sensor started...");
                sensorRead(args[0], file);
            } else if (args[0].compareTo("energy") == 0) {
                System.out.println("Energy sensor started...");
                sensorRead(args[0], file);
            } else if (args[0].compareTo("location") == 0 && args[1].endsWith("Location.txt")) {
                System.out.println("Location tracker started...");
                sensorRead(args[1].substring(0, args[1].length() - 4), file);
            }
            file.close(); // close the sensor file
        } catch (java.io.IOException ex) {
            ex.printStackTrace();
        } catch (Ice.LocalException ex) {
            ex.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

}
