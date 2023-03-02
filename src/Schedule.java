package JadeProject;
import java.util.Random;
import java.util.ArrayList;
import java.util.Collections;
import java.util.*;


public class Schedule {

    private final int DAYS = 5;
    private final int HOURS = 8;
    private ArrayList<otherSchedule> preference = new ArrayList<otherSchedule>();
    private final int TOTAL = DAYS*HOURS;


    // constructor
    Schedule(){
        initialize();
        Collections.sort(preference, new Comparator<otherSchedule>() {
            @Override
            public int compare(otherSchedule p1, otherSchedule p2) {
                // Aqui esta el truco, ahora comparamos p2 con p1 y no al reves como antes
                return new Double(p2.getPreference()).compareTo(new Double(p1.getPreference()));
            }
        });


    }
    @Override
    public String toString (){
        String s="";
        for(int i = 0; i< preference.size();i++){
            s+= preference.get(i).getDay() +" ";
            s+= preference.get(i).getHour() +" ";
            s+= preference.get(i).getPreference() +"\n";
        }
        return s;
    }
    void initialize(){
        double rangeMin =  0.0f;
        double rangeMax = 1.0f;
        Random r = new Random();
        int days_aux=1;
        int hour_aux=1;
        for(int i = 0; i< TOTAL;i++){
            double value = rangeMin + (rangeMax - rangeMin) * r.nextDouble();
            preference.add(new otherSchedule(days_aux,hour_aux,value));
            if(hour_aux >= HOURS){
                days_aux++;
                hour_aux=1;
            }
            hour_aux++;

        }
    }
    public ArrayList<otherSchedule> getArrayList(){return preference;}

    public void timeSelected(){
        otherSchedule aux = preference.get(0);
        preference.remove(0);
        aux.setPreference(0);
        preference.add(aux);
    }
    public void timeSelectedCanceled(int m_day, int m_hour,double m_pref){

        preference.remove(preference.size()-1);

        preference.add(0,new otherSchedule(m_day,m_hour,m_pref));
    }

    public int find(int day, int hour){
        int aux=0;
        for(int i=0; i<preference.size(); i++){
            if(preference.get(i).getDay() == day && preference.get(i).getHour() == hour )
                aux=i;
        }
        return aux;
    }


    public void cancelReceiving(otherSchedule aux){
        int a = find(aux.getDay(),aux.getHour());
        preference.remove(a);
        aux.setPreference(0);
        preference.add(aux);
    }


}

