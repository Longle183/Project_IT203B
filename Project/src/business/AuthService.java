package business;

import utils.DBConnection;
import entity.User;

import java.sql.*;
import java.util.Scanner;

public class AuthService {
    private Scanner sc = new Scanner(System.in);

    // ================= ĐĂNG KÝ =================
    public void register() {
        try (Connection conn = DBConnection.getConnection()) {

            String username;
            do {
                System.out.print("Username: ");
                username = sc.nextLine().trim();

                if (username.isEmpty()) {
                    System.out.println("Username không được để trống!");
                }
            } while (username.isEmpty());

            String password;
            do {
                System.out.print("Password: ");
                password = sc.nextLine().trim();

                if (password.isEmpty()) {
                    System.out.println("Password không được để trống!");
                }
            } while (password.isEmpty());

            String role = "CUSTOMER";

            String sql = "INSERT INTO users(username,password,role) VALUES (?,?,?)";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, username);
            ps.setString(2, password);
            ps.setString(3, role);

            ps.executeUpdate();
            System.out.println("Đăng ký thành công! (Role: CUSTOMER)");

        } catch (SQLIntegrityConstraintViolationException e) {
            System.out.println("Username đã tồn tại!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ================= ĐĂNG NHẬP =================
    public User login() {
        try (Connection conn = DBConnection.getConnection()) {

            String username;
            do {
                System.out.print("Username: ");
                username = sc.nextLine().trim();

                if (username.isEmpty()) {
                    System.out.println("Username không được để trống!");
                }
            } while (username.isEmpty());

            String password;
            do {
                System.out.print("Password: ");
                password = sc.nextLine().trim();

                if (password.isEmpty()) {
                    System.out.println("Password không được để trống!");
                }
            } while (password.isEmpty());

            String sql = "SELECT * FROM users WHERE username=? AND password=?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, username);
            ps.setString(2, password);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                User user = new User();
                user.setId(rs.getInt("id"));
                user.setUsername(rs.getString("username"));
                user.setRole(rs.getString("role"));

                System.out.println("Đăng nhập thành công! Role: " + user.getRole());
                return user;
            } else {
                System.out.println("Sai tài khoản hoặc mật khẩu!");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}