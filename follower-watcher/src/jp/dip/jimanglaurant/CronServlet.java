package jp.dip.jimanglaurant;

import java.io.IOException;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskOptions;

public class CronServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    public CronServlet() {
        super();
    }

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		EntityManager em = EMF.get().createEntityManager();
        Query query = em.createNamedQuery("getAllUserAccount");
        
        @SuppressWarnings("unchecked")
		List<UserAccount> list = query.getResultList();
		
		Queue queue = QueueFactory.getQueue("task");
		
		for(int i = 0;i < list.size();i++){
	        TaskOptions to = TaskOptions.Builder.withUrl("/task").param("user_id",String.valueOf(list.get(i).getUser_id()));
	        queue.add(to);
		}
	}
}
