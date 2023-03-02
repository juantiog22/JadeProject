package JadeProject;

public class otherSchedule{

    int day, hour;
    double preference;


    public otherSchedule(int day, int hour, double pref){
        this.day = day;
        this.hour = hour;
        this.preference = pref;
    }

    int getDay(){
        return day;
    }

    int getHour(){
        return hour;
    }

    double getPreference(){
        return preference;
    }
    void setPreference(double a){
        this.preference= a;
    }
    public static String getDayName(int day) {
        String result = "";

        switch (day) {
            case 1:
                result = "Monday";
                break;
            case 2:
                result = "Tuesday";
                break;
            case 3:
                result = "Wednesday";
                break;
            case 4:
                result = "Thursday";
                break;
            case 5:
                result = "Friday";
                break;

        }

        return result;
    }

    public static String getHourName(int hour) {
        String result = "";

        switch (hour) {
            case 1:
                result = "8:00";
                break;
            case 2:
                result = "9:00";
                break;
            case 3:
                result = "10:00";
                break;
            case 4:
                result = "11:00";
                break;
            case 5:
                result = "12:00";
                break;
            case 6:
                result = "13:00";
                break;
            case 7:
                result = "14:00";
                break;
            case 8:
                result = "15:00";
                break;

        }

        return result;
    }

}