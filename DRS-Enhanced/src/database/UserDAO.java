package database;

import model.User;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserDAO {
    private final Connection connection;
    public UserDAO() { this.connection = DatabaseConnection.getInstance().getConnection(); }

    public User authenticate(String username, String passwordHash) {
        String sql = "SELECT * FROM users WHERE username=? AND password_hash=? AND is_active=TRUE";
        try (PreparedStatement s = connection.prepareStatement(sql)) {
            s.setString(1, username); s.setString(2, passwordHash);
            ResultSet rs = s.executeQuery();
            if (rs.next()) { User u = map(rs); updateLastLogin(u.getUserId()); return u; }
        } catch (SQLException e) { System.err.println("[UserDAO] Auth: " + e.getMessage()); }
        return null;
    }
    public boolean createUser(User user) {
        String sql = "INSERT INTO users (username,password_hash,role,full_name,email,phone,department) VALUES(?,?,?,?,?,?,?)";
        try (PreparedStatement s = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            s.setString(1,user.getUsername()); s.setString(2,user.getPasswordHash());
            s.setString(3,user.getRole().name()); s.setString(4,user.getFullName());
            s.setString(5,user.getEmail()); s.setString(6,user.getPhone()); s.setString(7,user.getDepartment());
            if (s.executeUpdate() > 0) { ResultSet k=s.getGeneratedKeys(); if(k.next()) user.setUserId(k.getInt(1)); return true; }
        } catch (SQLException e) { System.err.println("[UserDAO] Create: " + e.getMessage()); }
        return false;
    }
    public List<User> getAllUsers() {
        List<User> list = new ArrayList<>();
        try (Statement s = connection.createStatement(); ResultSet rs = s.executeQuery("SELECT * FROM users ORDER BY user_id")) {
            while (rs.next()) list.add(map(rs));
        } catch (SQLException e) { System.err.println("[UserDAO] GetAll: " + e.getMessage()); }
        return list;
    }
    public User getUserById(int id) {
        try (PreparedStatement s = connection.prepareStatement("SELECT * FROM users WHERE user_id=?")) {
            s.setInt(1,id); ResultSet rs = s.executeQuery(); if(rs.next()) return map(rs);
        } catch (SQLException e) { System.err.println("[UserDAO] GetById: " + e.getMessage()); }
        return null;
    }
    public boolean updateUser(User user) {
        String sql = "UPDATE users SET full_name=?,email=?,phone=?,department=?,role=?,is_active=? WHERE user_id=?";
        try (PreparedStatement s = connection.prepareStatement(sql)) {
            s.setString(1,user.getFullName()); s.setString(2,user.getEmail()); s.setString(3,user.getPhone());
            s.setString(4,user.getDepartment()); s.setString(5,user.getRole().name());
            s.setBoolean(6,user.isActive()); s.setInt(7,user.getUserId());
            return s.executeUpdate() > 0;
        } catch (SQLException e) { System.err.println("[UserDAO] Update: " + e.getMessage()); }
        return false;
    }
    private void updateLastLogin(int id) {
        try (PreparedStatement s = connection.prepareStatement("UPDATE users SET last_login=NOW() WHERE user_id=?")) {
            s.setInt(1,id); s.executeUpdate();
        } catch (SQLException e) { System.err.println("[UserDAO] LastLogin: " + e.getMessage()); }
    }
    private User map(ResultSet rs) throws SQLException {
        User u = new User();
        u.setUserId(rs.getInt("user_id")); u.setUsername(rs.getString("username"));
        u.setPasswordHash(rs.getString("password_hash")); u.setRole(User.Role.valueOf(rs.getString("role")));
        u.setFullName(rs.getString("full_name")); u.setEmail(rs.getString("email"));
        u.setPhone(rs.getString("phone")); u.setDepartment(rs.getString("department"));
        Timestamp ca=rs.getTimestamp("created_at"); if(ca!=null) u.setCreatedAt(ca.toLocalDateTime());
        Timestamp ll=rs.getTimestamp("last_login"); if(ll!=null) u.setLastLogin(ll.toLocalDateTime());
        u.setActive(rs.getBoolean("is_active")); return u;
    }
}
