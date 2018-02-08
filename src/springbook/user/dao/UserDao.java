package springbook.user.dao;

import java.sql.*;

import springbook.user.domain.User;

public abstract class UserDao {
	public void add(User user) throws ClassNotFoundException, SQLException{
		Connection c = getConnection();
		PreparedStatement ps = c.prepareStatement("insert into users(id, name, password) values(?,?,?)");
		ps.setString(1, user.getId());
		ps.setString(2, user.getName());
		ps.setString(3, user.getPassword());
		
		ps.executeUpdate();
		
		ps.close();
		c.close();
	}
	
	public User get(String id) throws ClassNotFoundException, SQLException{
		Connection c = getConnection();
		PreparedStatement ps = c.prepareStatement("select * from users where id = ?");
		ps.setString(1, id);
		
		ResultSet rs = ps.executeQuery();
		rs.next();
		User user = new User();
		user.setId(rs.getString("id"));
		user.setName(rs.getString("name"));
		user.setPassword(rs.getString("password"));
		
		rs.close();
		ps.close();
		c.close();
		
		return user;
	}
	
	public abstract Connection getConnection() throws ClassNotFoundException, SQLException;
}

class NUserDao extends UserDao{
	@Override
	public Connection getConnection() throws ClassNotFoundException, SQLException {
		// N荤 DB connection 积己 内靛
		return null;
	}
}

class DUserDao extends UserDao{
	@Override
	public Connection getConnection() throws ClassNotFoundException, SQLException {
		//D荤 DB connection 积己内靛
		return null;
	}
}