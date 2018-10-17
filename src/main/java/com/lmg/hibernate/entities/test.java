package com.lmg.hibernate.entities;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.hibernate.jdbc.Work;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.service.ServiceRegistryBuilder;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.lmg.hibernate.dao.DepartmentDao;
import com.lmg.hibernate.hibernate.HibernateUtils;

public class test {
	private SessionFactory sessionFactory;
	private Session session;
	private Transaction transaction;
	
	@Before
	public void init(){
		Configuration configuration = new Configuration().configure();
		ServiceRegistry serviceRegistry = new ServiceRegistryBuilder()
				.applySettings(configuration.getProperties())
				.buildServiceRegistry();
		sessionFactory = configuration.buildSessionFactory(serviceRegistry);
		session = sessionFactory.openSession();
		transaction = session.beginTransaction();
	}
	
	@After
	public void destory(){
		transaction.commit();
		session.close();
		sessionFactory.close();
	}
	
	@Test
	public void testBatch(){
		session.doWork(new Work() {
			
			@Override
			public void execute(Connection arg0) throws SQLException {
				// 通过 JDBC 原生的 API 进行操作, 效率最高, 速度最快!
				
			}
		});
	}
	
	@Test
	public void testManageSession(){
		//获取 Session
		//开启事务
		Session session = HibernateUtils.getInstance().getSession();
		System.out.println("--->" + session.hashCode());
		Transaction transaction = session.beginTransaction();
		
		DepartmentDao departmentDao = new DepartmentDao();
		
		Department dept = new Department();
		dept.setName("ATGUIGU");
		
		departmentDao.save(dept);
		departmentDao.save(dept);
		departmentDao.save(dept);
	
		transaction.commit();
		System.out.println(session.isOpen());
			
	}
	
	@Test
	public void testQueryIterate(){
		Department dept = (Department) session.get(Department.class, 2);
		System.out.println(dept.getName());
		System.out.println(dept.getEmps().size());
		
		Query query = session.createQuery("FROM Employee e WHERE e.dept.id = 2");
//		List<Employee> emps = query.list();
//		System.out.println(emps.size());
		
		Iterator<Employee> empIt = query.iterate();
		while(empIt.hasNext()){
			System.out.println(empIt.next().getName());
		}
	}
	
	@Test
	public void testUpdateTimeStampCache(){
		Query query = session.createQuery("FROM Employee");
		query.setCacheable(true);
		
		List<Employee> emps = query.list();
		System.out.println(emps.size());
		
		Employee employee = (Employee) session.get(Employee.class, 1);
		employee.setSalary(30000);
		
		emps = query.list();
		System.out.println(emps.size());
	}
	
	@Test
	public void testQueryCache(){
		Query query = session.createQuery("FROM Employee");
		query.setCacheable(true);
		
		List<Employee> emps = query.list();
		System.out.println(emps.size());
		
		emps = query.list();
		System.out.println(emps.size());
	}
	
	@Test
	public void testCollectionSeconfLevelCache(){
		Department dept = (Department) session.get(Department.class, 1);
		System.out.println(dept.getName());
		System.out.println(dept.getEmps().size());
		
		transaction.commit();
		session.close();
		
		session = sessionFactory.openSession();
	 	transaction = session.beginTransaction();
		
		Department dept2 = (Department) session.get(Department.class, 1);
		System.out.println(dept2.getName());
		System.out.println(dept2.getEmps().size());
	}
	
	@Test
	public void testGroupBy(){
		String hql = "SELECT min(e.salary), max(e.salary) "
				+ "FROM Employee e "
				+ "GROUP BY e.dept "
				+ "HAVING min(e.salary) > :minSal";
		
		Query query = session.createQuery(hql)
							 .setFloat("minSal", 5000);
		
		List<Object[]> result = query.list();
		for (Object[] objs : result) {
			System.out.println(Arrays.asList(objs));
		}
	}
	
	@Test
	public void testFieldQuery2(){
		String hql = "SELECT new Employee(e.email, e.salary, e.dept)"
				+ " FROM Employee e "
				+ "WHERE e.dept = :dept";
		Query query = session.createQuery(hql);
		
		Department dept = new Department();
		dept.setId(1);
		List<Employee> result = query.setEntity("dept", dept)
									 .list();
		
		for (Employee e : result) {
			System.out.println(e.getId()+ ", "+ e.getEmail()
								+", "+ e.getSalary() + ", " + e.getDept());
		}
	}
	
	@Test
	public void testFieldQuery(){
		String hql = "SELECT e.email, e.salary FROM Employee e WHERE e.dept = :dept";
		Query query = session.createQuery(hql);
		
		Department dept = new Department();
		dept.setId(1);
		List<Object[]> result = query.setEntity("dept", dept)
									 .list();
		
		for (Object[] objs : result) {
			System.out.println(Arrays.asList(objs));
		}
	}
	
	@Test
	public void testNamedQuery(){	
		Query query = session.getNamedQuery("salaryEmps");
		
		List<Employee> emps = query.setFloat("minSal", 5000)
								   .setFloat("maxSal", 10000)
								   .list();
		
		for (Employee employee : emps) {
			System.out.println(employee);
		}
	}
	
	@Test
	public void testPageQuery(){	
		String hql = "FROM Employee";
		Query query = session.createQuery(hql);
		
		int pageNo = 1;
		int pageSize = 5;
		
		List<Employee> emps =
		query.setFirstResult((pageNo - 1) * pageSize)
			 .setMaxResults(pageSize)
			 .list();
		for (Employee employee : emps) {
			System.out.println(employee);
		}
	}
	
	@Test
	public void testHQLNamedParameter(){	
		
		//1. 创建 Query 对象
		//基于命名参数.
		String hql = "FROM Employee e WHERE e.salary > :sal AND e.email LIKE :email AND e.dept = :dept";
		Query query = session.createQuery(hql);
		
		Department dept = new Department();
		dept.setId(1);
		//2. 绑定参数
		query.setFloat("sal", 7000)
			 .setString("email", "%A%")
			 .setEntity("dept", dept);
		
		//3. 执行查询
		List<Employee> emps = query.list();
		System.out.println(emps.size());
	}
	
	@Test
	public void testHQL(){	
		
		//1. 创建 Query 对象
		//基于位置的参数.
		String hql = "FROM Employee e WHERE e.salary > ? AND e.email LIKE ? ORDER BY e.name";
		Query query = session.createQuery(hql);
		
		//2. 绑定参数
		//Query 对象调用 setXxx 方法支持方法链的编程风格.
		query.setFloat(0, 6000)
			 .setString(1, "%A%");
		
		//3. 执行查询
		List<Employee> emps = query.list();
		System.out.println(emps.size());
	}
	
	
	
	
}
