package jp.dip.jimanglaurant;

import java.io.IOException;
import java.util.ArrayList;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import twitter4j.IDs;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;

public class TaskServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

    public TaskServlet() {
        super();
    }

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		long user_id = Long.parseLong(request.getParameter("user_id"));
		EntityManager em = EMF.get().createEntityManager();
        Query query = em.createNamedQuery("getUserAccountByUserId");
        query.setParameter("uid", user_id);
        UserAccount useraccount = (UserAccount)query.getResultList().get(0);
                
        Twitter twitter = new TwitterFactory().getInstance();
        twitter.setOAuthAccessToken(new AccessToken(useraccount.getAccess_token(),useraccount.getAccess_token_secret()));
        
        try {
			ArrayList<Long> newfollowerlist = new ArrayList<Long>();
        	IDs ids = twitter.getFollowersIDs(IDs.START);
			long[] id = ids.getIDs();
			for(int i = 0;i < id.length;i++){
				newfollowerlist.add(id[i]);
			}
			while(ids.hasNext()){
				ids = twitter.getFollowersIDs(ids.getNextCursor());
				id = ids.getIDs();
				for(int i = 0;i < id.length;i++){
					newfollowerlist.add(id[i]);
				}
			}
						
			ArrayList<Long> oldfollowerlist = new ArrayList<Long>(useraccount.getFollower_list());
			
			ArrayList<Long> intersection = new ArrayList<Long>(oldfollowerlist);
			
			intersection.removeAll(newfollowerlist);
						
			for(int i = 0;i < intersection.size();i++){
				try{
					twitter.sendDirectMessage(twitter.getId(),"@" + twitter.showUser(intersection.get(i).longValue()).getScreenName() + " さんにリムーブされました");
				} catch(TwitterException e) {
				}
			}
						
			useraccount.setFollower_list(newfollowerlist);
			
		} catch (TwitterException e) {
			
			throw new ServletException(e);
			
		} finally {
			em.close();
		}
	}
}
