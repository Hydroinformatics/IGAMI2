/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package igami2.DataBase;

import java.util.HashMap;
import org.hibernate.cfg.AnnotationConfiguration;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

/**
 * Hibernate Utility class with a convenient method to get Session Factory
 * object.
 *
 * @author VIDYA
 */
public class HibernateUtil {

    private static HashMap<Integer,SessionFactory> sessionFactoryMap; //a new session Factory for every watersheds
    private static String[] DBConfigLocation;
    private static String watershedConfigFile = "../SWAT/igami2_config/hibernate";
    
    public HibernateUtil()
    {
        sessionFactoryMap = new HashMap();
    }

    public static SessionFactory createSessionFactory(int watershedId) //session factory for every watershed
    {
        SessionFactory fact = null;
        try {
            watershedConfigFile = watershedConfigFile + watershedId + ".cfg.xml";
            Configuration cfg = new AnnotationConfiguration();
            cfg.configure(watershedConfigFile);
            fact = cfg.buildSessionFactory();
            sessionFactoryMap.put(watershedId, fact);
        } catch (Exception ex) {
            // Log the exception. 
            System.err.println("Initial SessionFactory creation failed." + ex);
            throw new ExceptionInInitializerError(ex);
        }
        return fact;
    }
   
    
    public static SessionFactory getSessionFactory(int watershedId) {
        SessionFactory fact = null;                
        fact = sessionFactoryMap.get(watershedId);
        if(fact==null)//no session factory initialized yet so create a new one
        {
            fact = createSessionFactory(watershedId);
        }       
        return fact;
    }
}
