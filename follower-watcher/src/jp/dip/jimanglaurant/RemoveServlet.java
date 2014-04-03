package jp.dip.jimanglaurant;

import java.io.IOException;
import java.util.logging.Logger;

import javax.persistence.EntityManager;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class RemoveServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final Logger log = Logger.getLogger(RemoveServlet.class.getName());

    public RemoveServlet() {
        super(); 
    } 

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		UserAccount useraccount = (UserAccount) request.getSession().getAttribute("useraccount");
        EntityManager em = EMF.get().createEntityManager();
        
        UserAccount ua = em.merge(useraccount);
        em.remove(ua); 
        em.close(); 
        
        log.info(useraccount.getScreen_name()+"は登録を解除しました");
        
        request.getSession().invalidate();
        
        response.sendRedirect(request.getContextPath() + "/remove.jsp");
	}

}
