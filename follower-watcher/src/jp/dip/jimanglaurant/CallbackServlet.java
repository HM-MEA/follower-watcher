package jp.dip.jimanglaurant;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.auth.RequestToken;

public class CallbackServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final Logger log = Logger.getLogger(CallbackServlet.class.getName());
       
    public CallbackServlet() {
        super();
    }

	@SuppressWarnings("unchecked")
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		Twitter twitter = (Twitter) request.getSession().getAttribute("twitter");
        RequestToken requestToken = (RequestToken) request.getSession().getAttribute("requestToken");
        String verifier = request.getParameter("oauth_verifier");
        try {
            twitter.getOAuthAccessToken(requestToken, verifier);
            request.getSession().removeAttribute("requestToken");
        } catch (TwitterException e) {
            throw new ServletException(e);
        }
        
        EntityManager em = EMF.get().createEntityManager();
        Query query = em.createNamedQuery("getUserAccountByUserId");
        
        try {
            query.setParameter("uid", twitter.getId());
            List<UserAccount> list = (List<UserAccount>) query.getResultList();
            UserAccount useraccount;
            if(list.size() == 0){
            	useraccount = new UserAccount(twitter.getId(),twitter.getScreenName(),twitter.getOAuthAccessToken().getToken(),twitter.getOAuthAccessToken().getTokenSecret(),new ArrayList<Long>());
            	em.persist(useraccount);
            }else{
            	useraccount = list.get(0);
            }
            
        	request.getSession().setAttribute("useraccount", useraccount);       	
        	log.info(useraccount.getScreen_name()+"が登録されました");
        	
        } catch (IllegalStateException | TwitterException e) {
			e.printStackTrace();
		} finally {
			em.close();
		}
        
        response.sendRedirect(request.getContextPath() + "/index.jsp");
	}
}
