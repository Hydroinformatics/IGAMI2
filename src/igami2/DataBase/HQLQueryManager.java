/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package igami2.DataBase;

import java.util.Iterator;
import java.util.List;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;

/**
 *
 * @author VIDYA
 */
public class HQLQueryManager {

    private Session session; //local session variable for every user, close session only when user is inactive or stop the search

    public HQLQueryManager(int watershedId) {
        session = HibernateUtil.getSessionFactory(watershedId).openSession(); //create a new session for a new user
    }

    public void beginTransaction() {
        //session = HibernateUtil.getSessionFactory().openSession();
        session.beginTransaction();
    }

    public void closeSession() {
        session.close();
    }

    public List executeHQLQuery(String hql) {

        List resultList = null;
        beginTransaction();
        try {
            Query q = session.createQuery(hql);
            resultList = q.list();
            commitTranssaction();
        } catch (HibernateException he) {
            he.printStackTrace();
            //add reattempt code

            while (true) {
                try {
                    Thread.sleep(1000);
                    System.out.println("Re attempting the query " + hql);
                    Query q = session.createQuery(hql);
                    resultList = q.list();

                    commitTranssaction();

                    //sucess so break
                    System.out.println("Sucess in query " + hql);
                    break;


                } catch (InterruptedException ex) {
                } catch (HibernateException he1) {
                    System.out.println("Failed Again");
                }
            }
        }
        return resultList;
    }

    public void commitTranssaction() {
        session.getTransaction().commit();
    }
    //save only one object

    public void saveOneObject(Object o) {
        beginTransaction();
        session.save(o);
        commitTranssaction();

    }
    //save a set of objects

    public void saveMultipleObject(List<Object> o) {
        beginTransaction();
        Iterator itr = o.iterator();
        while (itr.hasNext()) {
            session.save(itr.next());
        }

        commitTranssaction();

    }

    //save only one object
    public void updateOneObject(Object o) {
        beginTransaction();
        session.update(o);
        commitTranssaction();


    }
    //save a set of objects

    public void updateMultipleObject(List<Object> o) {
        beginTransaction();
        Iterator itr = o.iterator();
        while (itr.hasNext()) {
            session.update(itr.next());
        }
        commitTranssaction();

    }

    //save only one object
    public void deleteOneObject(Object o) {
        beginTransaction();
        session.delete(o);
        commitTranssaction();


    }
    //save a set of objects

    public void deleteMultipleObject(List<Object> objs) {
        beginTransaction();
        Iterator itr = objs.iterator();
        while (itr.hasNext()) {
            session.delete(itr.next());
        }
        commitTranssaction();

    }

    private void displayResult(List resultList) {
        for (Object o : resultList) {
            //NewuserParamters userParam = (NewuserParamters)o;
        }
    }
}