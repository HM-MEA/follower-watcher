package jp.dip.jimanglaurant;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.RequestToken;

public class SigninServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    public SigninServlet() {
        super();
    }

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		HttpSession session = request.getSession(true);
		session.invalidate();
		HttpSession newSession = request.getSession(true);
		
		Twitter twitter = new TwitterFactory().getInstance();
        
        newSession.setAttribute("twitter", twitter);
        newSession.setAttribute("user", null);
        
        try {
        	StringBuffer callbackURL = request.getRequestURL();

            int index = callbackURL.lastIndexOf("/");
            callbackURL.replace(index, callbackURL.length(), "").append("/callback");
            
            RequestToken requestToken = twitter.getOAuthRequestToken(callbackURL.toString());
            newSession.setAttribute("requestToken", requestToken);
            response.sendRedirect(requestToken.getAuthenticationURL());
            
        } catch (TwitterException e) {
            throw new ServletException(e);
        }
	}
}
