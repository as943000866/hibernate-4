package com.lmg.hibernate.dao;

import org.hibernate.Session;
import org.hibernate.SessionFactory;

import com.lmg.hibernate.entities.Department;
import com.lmg.hibernate.hibernate.HibernateUtils;

public class DepartmentDao {
	
	private SessionFactory sessionFactory;
	
	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}
	
	public void save(Department dept){
		//内部获取 Session 对象
		//获取和当前线程绑定的 Session 对象
		//1. 不需要从外部传入 Session 对象
		//2. 多个 DAO 方法也可以使用一个事务!
		Session session = HibernateUtils.getInstance().getSession();
		System.out.println(session.hashCode());
		
//		session.save(dept);
	}
	
	/**
	 * 若需要传入一个 Session 对象, 则意味着上一层(Service) 需要获取到 Session 对象.
	 * 这说明上一层需要和 Hibernate 的 API 紧密耦合. 所以不推荐使用此种方式.
	 */
	public void save(Session session, Department department){
		session.save(department);
	}
}
