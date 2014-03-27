package jp.dip.jimanglaurant;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

public class EMF{
	private static EntityManagerFactory emfInstance = Persistence.createEntityManagerFactory("transactions-optional");
	private EMF() {}
	public static EntityManagerFactory get() {
		return emfInstance;
	}
}
