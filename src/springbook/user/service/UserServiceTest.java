package springbook.user.service;

import static org.mockito.Mockito.*;
import static springbook.user.service.UserServiceImpl.MIN_LOGCOUNT_FOR_SILVER;
import static springbook.user.service.UserServiceImpl.MIN_RECCOMEND_FOR_GOLD;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.sql.DataSource;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailException;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.PlatformTransactionManager;

import springbook.user.dao.UserDao;
import springbook.user.domain.Level;
import springbook.user.domain.User;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations="/test-applicationContext.xml")
public class UserServiceTest {
	@Autowired UserService userService;
	@Autowired UserServiceImpl userServiceImpl;
	@Autowired UserDao userDao;
	@Autowired PlatformTransactionManager transactionManager;
	@Autowired MailSender mailSender;
	List<User> users;
	
	@Before
	public void setUp() {
		users = Arrays.asList(
				new User("bumjin", "박범진", "p1", Level.BASIC, MIN_LOGCOUNT_FOR_SILVER-1, 0, "bumjin@naver.com"),
				new User("joytouch", "강명성", "p2", Level.BASIC, MIN_LOGCOUNT_FOR_SILVER, 0, "kms12@gmail.com"),
				new User("erwins", "신승한", "p3", Level.SILVER, 60, MIN_RECCOMEND_FOR_GOLD-1, "ssh33@daum.net"),
				new User("madnite1", "이상호", "p4", Level.SILVER, 60, MIN_RECCOMEND_FOR_GOLD, "lsh55@naver.com"),
				new User("green", "오민규", "p5", Level.GOLD, 100, Integer.MAX_VALUE, "omkmo@naver.com")
				);
	}
	
	@Test
	public void upgradeLevels() throws Exception {
		//고립된 테스트에서는 테스트 대상 오브젝트를 직접 생성
		UserServiceImpl userServiceImp1 = new UserServiceImpl();

		//목 오브젝트로 만든 UserDao를 직접 DI
		MockUserDao mockUserDao = new MockUserDao(this.users);
		userServiceImp1.setUserDao(mockUserDao);

		MockMailSender mockMailSender = new MockMailSender();
		userServiceImp1.setMailSender(mockMailSender);

		userServiceImp1.upgradeLevels();

		//MockUserDao로부터 업데이트 결과를 가져온다
		List<User> updated = mockUserDao.getUpdated();
		assertThat(updated.size(), is(2));
		checkUserAndLevel(updated.get(0), "joytouch", Level.SILVER);
		checkUserAndLevel(updated.get(1), "madnite1", Level.GOLD);

		List<String> request = mockMailSender.getRequests();
		assertThat(request.size(), is(2));
		assertThat(request.get(0), is(users.get(1).getEmail()));
		assertThat(request.get(1), is(users.get(3).getEmail()));
	}

	private void checkUserAndLevel(User updated, String expectedId, Level expectedLevel){
		assertThat(updated.getId(), is(expectedId));
		assertThat(updated.getLevel(), is(expectedLevel));
	}

//	@Test
//	public void mockUpgradeLevels() throws Exception{
//		UserServiceImpl userServiceImpl = new UserServiceImpl();
//
//		UserDao mockUserDao = mock(UserDao.class);
//		when(mockUserDao.getAll()).thenReturn(this.users);
//		userServiceImpl.setUserDao(mockUserDao);
//
//		MailSender mockMailSender = mock(MailSender.class);
//		userServiceImpl.setMailSender(mailSender);
//
//		userServiceImpl.upgradeLevels();
//
//		verify(mockUserDao, times(2)).update(any(User.class));
//		verify(mockUserDao, times(2)).update(any(User.class));
//		verify(mockUserDao).update(users.get(1));
//		assertThat(users.get(1).getLevel(), is(Level.SILVER));
//		verify(mockUserDao).update(users.get(3));
//		assertThat(users.get(3).getLevel(), is(Level.GOLD));
//
//		ArgumentCaptor<SimpleMailMessage> mailMessageArg = ArgumentCaptor.forClass(SimpleMailMessage.class);
//		verify(mockMailSender, times(2)).send(mailMessageArg.capture());
//		List<SimpleMailMessage> mailMessages = mailMessageArg.getAllValues();
//		assertThat(mailMessages.get(0).getTo()[0], is(users.get(1).getEmail()));
//		assertThat(mailMessages.get(1).getTo()[0], is(users.get(3).getEmail()));
//	}

	
	@Test
	public void add() {
		userDao.deleteAll();
		
		User userWithLevel = users.get(4);	// GOLD 레벨
		User userWithoutLevel = users.get(0);
		userWithoutLevel.setLevel(null);
		
		userService.add(userWithLevel);
		userService.add(userWithoutLevel);
		
		User userWithLevelRead = userDao.get(userWithLevel.getId());
		User userWithoutLevelRead = userDao.get(userWithoutLevel.getId());
		
		assertThat(userWithLevelRead.getLevel(), is(userWithLevel.getLevel()));
		assertThat(userWithoutLevelRead.getLevel(), is(Level.BASIC));
	}
	
	static class TestUserService extends UserServiceImpl{
		private String id;
		
		private TestUserService(String id) {
			this.id = id;
		}
		
		protected void upgradeLevel(User user) {
			if(user.getId().equals(this.id)) throw new TestUserServiceException();
			super.upgradeLevel(user);
		}
	}
	
	static class TestUserServiceException extends RuntimeException{}
	
	@Test
	public void upgradeAllOrNothing() throws Exception{
		TestUserService testUserService = new TestUserService(users.get(3).getId());
		testUserService.setUserDao(userDao);
		testUserService.setMailSender(mailSender);
		
		TransactionHandler txHandler = new TransactionHandler();
		txHandler.setTarget(testUserService);
		txHandler.setTransactionManager(transactionManager);
		txHandler.setPattern("upgradeLevels");
		UserService txUserService = (UserService) Proxy.newProxyInstance(
				getClass().getClassLoader(), new Class[]{UserService.class}, txHandler);
		
		userDao.deleteAll();
		for(User user : users) userDao.add(user);
		
		try {
			txUserService.upgradeLevels();
			fail("TestUserServiceException expected");
		} catch(TestUserServiceException e) {
		}
		
		checkLevelUpgraded(users.get(1), false);
	}

	private void checkLevelUpgraded(User user, boolean upgraded) {
		User userUpdate = userDao.get(user.getId());
		if(upgraded) {
			assertThat(userUpdate.getLevel(), is(user.getLevel().nextLevel()));
		}
		else {
			assertThat(userUpdate.getLevel(), is(user.getLevel()));
		}
	}

	static class MockMailSender implements MailSender{
		private List<String> requests =new ArrayList<String>();
		
		public List<String> getRequests(){
			return requests;
		}
		
		public void send(SimpleMailMessage mailMessage) throws MailException{
			requests.add(mailMessage.getTo()[0]);
		}
		
		public void send(SimpleMailMessage[] mailMessage) throws MailException{
		}
	}
	
	static class MockUserDao implements UserDao{
		private List<User> users;
		private List<User> updated = new ArrayList<User>();
		
		private MockUserDao(List<User> users) {
			this.users = users;
		}
		
		public List<User> getUpdated(){
			return this.updated;
		}
		
		public List<User> getAll(){
			return this.users;
		}
		
		public void update(User user) {
			updated.add(user);
		}
		
		public void add(User user) {throw new UnsupportedOperationException();}
		public void deleteAll() {throw new UnsupportedOperationException();}
		public User get(String id) {throw new UnsupportedOperationException();}
		public int getCount() {throw new UnsupportedOperationException();}
	}
}