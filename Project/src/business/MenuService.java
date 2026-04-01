package business;

import utils.DBConnection;

import java.sql.*;
import java.util.*;

public class MenuService {

    public void addMenu() throws Exception {
        Scanner sc = new Scanner(System.in);

        System.out.print("Tên món: ");
        String name = sc.nextLine();

        System.out.print("Giá: ");
        double price = Double.parseDouble(sc.nextLine());

        String sql = "INSERT INTO menu_items(name, price) VALUES(?, ?)";

        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, name);
            ps.setDouble(2, price);
            ps.executeUpdate();
        }

        System.out.println("Thêm món thành công");
    }

    public void viewMenu() throws Exception {
        String sql = "SELECT * FROM menu_items";

        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ResultSet rs = ps.executeQuery();

            System.out.println("\n========== DANH SÁCH MÓN ==========");
            System.out.printf("| %-4s | %-20s | %-10s |\n", "ID", "Tên món", "Giá");
            System.out.println("------------------------------------------");

            while (rs.next()) {
                System.out.printf("| %-4d | %-20s | %-10.0f |\n",
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getDouble("price"));
            }

            System.out.println("====================================\n");
        }
    }

    public void deleteMenu() {
        Scanner sc = new Scanner(System.in);

        try (Connection conn = DBConnection.getConnection()) {

            String sql = "SELECT * FROM menu_items";
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();

            System.out.println("\n========== DANH SÁCH MÓN ==========");
            System.out.printf("| %-4s | %-20s | %-10s |\n", "ID", "Tên món", "Giá");
            System.out.println("------------------------------------------");

            while (rs.next()) {
                System.out.printf("| %-4d | %-20s | %-10.0f |\n",
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getDouble("price"));
            }

            System.out.println("====================================\n");

            System.out.print("Nhập ID món cần xoá: ");
            int id = Integer.parseInt(sc.nextLine());

            String checkSql = "SELECT * FROM menu_items WHERE id=?";
            PreparedStatement psCheck = conn.prepareStatement(checkSql);
            psCheck.setInt(1, id);
            ResultSet rsCheck = psCheck.executeQuery();

            if (!rsCheck.next()) {
                System.out.println("Món không tồn tại!");
                return;
            }

            String checkOrder = "SELECT * FROM order_details WHERE menu_item_id=?";
            PreparedStatement psOrder = conn.prepareStatement(checkOrder);
            psOrder.setInt(1, id);
            ResultSet rsOrder = psOrder.executeQuery();

            if (rsOrder.next()) {
                System.out.println("Không thể xoá món vì đã có trong đơn hàng!");
                return;
            }

            String deleteSql = "DELETE FROM menu_items WHERE id=?";
            PreparedStatement psDelete = conn.prepareStatement(deleteSql);
            psDelete.setInt(1, id);
            psDelete.executeUpdate();

            System.out.println("Xoá món thành công!");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void searchMenuByName() {
        Scanner sc = new Scanner(System.in);

        try (Connection conn = DBConnection.getConnection()) {

            String keyword;

            // nhập tên cần tìm (không được rỗng)
            do {
                System.out.print("Nhập tên món cần tìm: ");
                keyword = sc.nextLine().trim();

                if (keyword.isEmpty()) {
                    System.out.println("Không được để trống!");
                }
            } while (keyword.isEmpty());

            String sql = "SELECT * FROM menu_items WHERE name LIKE ?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, "%" + keyword + "%");

            ResultSet rs = ps.executeQuery();

            boolean found = false;

            System.out.println("\n========== KẾT QUẢ TÌM KIẾM ==========");
            System.out.printf("| %-4s | %-20s | %-10s |\n", "ID", "Tên món", "Giá");
            System.out.println("------------------------------------------");

            while (rs.next()) {
                found = true;
                System.out.printf("| %-4d | %-20s | %-10.0f |\n",
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getDouble("price"));
            }

            if (!found) {
                System.out.println("Không tìm thấy món nào!");
            } else {
                System.out.println("======================================");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void orderFood(int userId) {
        Scanner sc = new Scanner(System.in);

        try (Connection conn = DBConnection.getConnection()) {

            // ===== Lấy bàn khách đang dùng =====
            String findOrder = "SELECT * FROM orders WHERE user_id=? ORDER BY id DESC LIMIT 1";
            PreparedStatement psFind = conn.prepareStatement(findOrder);
            psFind.setInt(1, userId);
            ResultSet rsOrder = psFind.executeQuery();

            if (!rsOrder.next()) {
                System.out.println("Bạn chưa đặt bàn!");
                return;
            }

            int orderId = rsOrder.getInt("id");

            // ===== Hiển thị menu =====
            viewMenu();

            // ===== Nhập món =====
            System.out.print("Nhập ID món: ");
            int menuId = Integer.parseInt(sc.nextLine());

            System.out.print("Số lượng: ");
            int quantity = Integer.parseInt(sc.nextLine());

            // ===== Lấy giá =====
            String getPrice = "SELECT price FROM menu_items WHERE id=?";
            PreparedStatement psPrice = conn.prepareStatement(getPrice);
            psPrice.setInt(1, menuId);
            ResultSet rsPrice = psPrice.executeQuery();

            if (!rsPrice.next()) {
                System.out.println("Món không tồn tại!");
                return;
            }

            double price = rsPrice.getDouble("price");

            // ===== Thêm vào order_details =====
            String insertDetail = "INSERT INTO order_details(order_id, menu_item_id, quantity) VALUES (?, ?, ?)";
            PreparedStatement psInsert = conn.prepareStatement(insertDetail);
            psInsert.setInt(1, orderId);
            psInsert.setInt(2, menuId);
            psInsert.setInt(3, quantity);
            psInsert.executeUpdate();

            // ===== Cập nhật tổng tiền =====
            double total = price * quantity;

            String updateTotal = "UPDATE orders SET total_amount = total_amount + ? WHERE id=?";
            PreparedStatement psUpdate = conn.prepareStatement(updateTotal);
            psUpdate.setDouble(1, total);
            psUpdate.setInt(2, orderId);
            psUpdate.executeUpdate();

            System.out.println("Gọi món thành công!");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void viewOrder(int userId) {
        try (Connection conn = DBConnection.getConnection()) {

            String sql = """
            SELECT t.name AS table_name,
                   m.name AS food_name,
                   od.quantity,
                   m.price,
                   (od.quantity * m.price) AS total
            FROM orders o
            JOIN restaurant_tables t ON o.table_id = t.id
            JOIN order_details od ON o.id = od.order_id
            JOIN menu_items m ON od.menu_item_id = m.id
            WHERE o.user_id = ?
        """;

            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, userId);

            ResultSet rs = ps.executeQuery();

            boolean found = false;
            String tableName = "";

            System.out.println("\n====== MÓN ĐÃ GỌI ======");

            double grandTotal = 0;

            while (rs.next()) {
                if (!found) {
                    tableName = rs.getString("table_name");
                    System.out.println("Bàn: " + tableName);
                    System.out.printf("| %-20s | %-8s | %-10s | %-10s |\n", "Tên món", "SL", "Giá", "Tổng");
                    System.out.println("----------------------------------------------------------");
                    found = true;
                }

                double total = rs.getDouble("total");
                grandTotal += total;

                System.out.printf("| %-20s | %-8d | %-10.0f | %-10.0f |\n",
                        rs.getString("food_name"),
                        rs.getInt("quantity"),
                        rs.getDouble("price"),
                        total);
            }

            if (!found) {
                System.out.println("Bạn chưa gọi món nào!");
            } else {
                System.out.println("----------------------------------------------------------");
                System.out.println("TỔNG TIỀN: " + grandTotal);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
