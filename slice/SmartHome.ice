module SmartHome
{
    interface HomeManager
    {
        int currentTemperature();
        void shutdown();
        void setTemperature(int temperature);
    };
    
    interface Sensor
    {
        void shutdown();
    };
    
};