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

    private Session session;

    public void loadSession() {
        session = HibernateUtil.getSessionFactory().openSession();
        session.beginTransaction();
    }
    
    public void closeSession()
    {
        session.close();
    }

    public List executeHQLQuery(String hql) {
        List resultList = null;
        try {
            loadSession();

            Query q = session.createQuery(hql);
            resultList = q.list();

            commitTranssaction();
            closeSession();

        } catch (HibernateException he) {
            he.printStackTrace();
            //add reattempt code
            closeSession();
            //commitTranssaction(); //release the connection
            while (true) {
                try {
                    Thread.sleep(1000);
                    System.out.println("Re attempting the query " + hql);
                    loadSession();

                    Query q = session.createQuery(hql);
                    resultList = q.list();

                    commitTranssaction();
                    closeSession();
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
        loadSession();
        session.save(o);
        commitTranssaction();
        closeSession();
    }
    //save a set of objects

    public void saveMultipleObject(List<Object> o) {
        loadSession();
        Iterator itr = o.iterator();
        while (itr.hasNext()) {
            session.save(itr.next());
        }

        commitTranssaction();
        closeSession();
    }

    //save only one object
    public void updateOneObject(Object o) {
        loadSession();
        session.update(o);
        commitTranssaction();
        closeSession();

    }
    //save a set of objects

    public void updateMultipleObject(List<Object> o) {
        loadSession();
        Iterator itr = o.iterator();
        while (itr.hasNext()) {
            session.update(itr.next());
        }
        commitTranssaction();
        closeSession();
    }

    //save only one object
    public void deleteOneObject(Object o) {
        loadSession();
        session.delete(o);
        commitTranssaction();
        closeSession();

    }
    //save a set of objects

    public void deleteMultipleObject(List<Object> objs) {
        loadSession();
        Iterator itr = objs.iterator();
        while (itr.hasNext()) {
            session.delete(itr.next());
        }
        commitTranssaction();
        closeSession();
    }

    private void displayResult(List resultList) {
        for (Object o : resultList) {
            //NewuserParamters userParam = (NewuserParamters)o;
        }
    }
}