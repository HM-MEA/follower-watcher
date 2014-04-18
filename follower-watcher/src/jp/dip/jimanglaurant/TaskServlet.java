package jp.dip.jimanglaurant;

import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Logger;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import twitter4j.IDs;
import twitter4j.Relationship;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;

public class TaskServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final Logger log = Logger.getLogger(TaskServlet.class.getName());

    public TaskServlet() {
        super();
    }

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		long user_id = Long.parseLong(request.getParameter("user_id"));
		EntityManager em = EMF.get().createEntityManager();
        Query query = em.createNamedQuery("getUserAccountByUserId");
        query.setParameter("uid", user_id);
        UserAccount useraccount = (UserAccount)query.getResultList().get(0);
        
        log.info(useraccount.getScreen_name());
                
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
			
			int count_403 = 0;
			int count_404 = 0;
						
			for(int i = 0;i < intersection.size();i++){
				try{
					Relationship rs = twitter.showFriendship(intersection.get(i).longValue(), twitter.getId());
					if(rs.isSourceBlockingTarget()){
						String str = "@" + twitter.showUser(intersection.get(i).longValue()).getScreenName() + " さんにブロックされました";
						twitter.sendDirectMessage(twitter.getId(),str);
						log.info(str);
					}else if(!rs.isSourceFollowingTarget()){
						String str = "@" + twitter.showUser(intersection.get(i).longValue()).getScreenName() + " さんにリムーブされました";
						twitter.sendDirectMessage(twitter.getId(),str);
						log.info(str);
					}
				} catch(TwitterException e) {
					if(e.getErrorCode() == 403){
						count_403 += 1;
					}else if(e.getErrorCode() == 404){
						count_404 += 1;
					}else{
						log.warning(e.getErrorMessage());
					}
				}
			}
			
			String str = "null";
			if(count_403 != 0){
				if(count_404 != 0){
					str = "フォロワーのうち、アカウントの凍結が" + count_403 + "件、アカウントの削除が" + count_404 + "件ありました";
				}else{
					str = "フォロワーのうち、アカウントの凍結が" + count_403 + "件ありました";
				}
			}else{
				if(count_404 != 0){
					str = "フォロワーのうち、アカウントの削除が" + count_404 + "件ありました";
				}
			}
			if(!str.equals("null")){
				twitter.sendDirectMessage(twitter.getId(),str);
				log.info(str);
			}
							
			useraccount.setFollower_list(newfollowerlist);
			
		} catch (TwitterException e) {
			
			throw new ServletException(e);
			
		} finally {
			em.close();
		}
	}
}
