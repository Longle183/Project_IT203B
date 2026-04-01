package business;

import utils.DBConnection;

import java.sql.*;
import java.util.*;

public class TableService {

    public void addTable() {
        Scanner sc = new Scanner(System.in);

        try (Connection c = DBConnection.getConnection()) {

            String name;
            int capacity;

            while (true) {
                System.out.print("Nhập tên bàn: ");
                name = sc.nextLine().trim();

                if (name.isEmpty()) {
                    System.out.println("Tên bàn không được để trống!");
                    continue;
                }

                String checkSql = "SELECT * FROM restaurant_tables WHERE name = ?";
                PreparedStatement psCheck = c.prepareStatement(checkSql);
                psCheck.setString(1, name);

                ResultSet rs = psCheck.executeQuery();

                if (rs.next()) {
                    System.out.println("Tên bàn đã tồn tại! Nhập lại!");
                } else {
                    break;
                }
            }

            while (true) {
                try {
                    System.out.print("Nhập số chỗ ngồi: ");
                    capacity = Integer.parseInt(sc.nextLine());

                    if (capacity <= 0) {
                        System.out.println("Số chỗ phải > 0!");
                    } else break;

                } catch (NumberFormatException e) {
                    System.out.println("Vui lòng nhập số hợp lệ!");
                }
            }

            // ===== insert =====
            String sql = "INSERT INTO restaurant_tables(name, capacity, status) VALUES (?, ?, 'AVAILABLE')";
            PreparedStatement ps = c.prepareStatement(sql);

            ps.setString(1, name);
            ps.setInt(2, capacity);

            ps.executeUpdate();

            System.out.println("Thêm bàn thành công!");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void viewTables() throws Exception {
        String sql = "SELECT * FROM restaurant_tables";

        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ResultSet rs = ps.executeQuery();

            System.out.println("\n========== DANH SÁCH BÀN ==========");
            System.out.printf("| %-4s | %-15s | %-8s | %-10s |\n", "ID", "Tên bàn", "Số chỗ", "Trạng thái");
            System.out.println("-----------------------------------------------------");

            while (rs.next()) {
                System.out.printf("| %-4d | %-15s | %-8d | %-10s |\n",
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getInt("capacity"),
                        rs.getString("status"));
            }

            System.out.println("====================================\n");
        }
    }

    public void bookTable(int userId) {
        try (Connection conn = DBConnection.getConnection()) {

            String sql = "SELECT * FROM restaurant_tables WHERE status='AVAILABLE'";
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();

            boolean hasTable = false;

            System.out.println("\n====== DANH SÁCH BÀN TRỐNG ======");
            System.out.printf("| %-4s | %-15s | %-10s |\n", "ID", "Tên bàn", "Sức chứa");
            System.out.println("--------------------------------------");

            while (rs.next()) {
                hasTable = true;
                System.out.printf("| %-4d | %-15s | %-10d |\n",
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getInt("capacity"));
            }

            System.out.println("==================================\n");

            if (!hasTable) {
                System.out.println("Không có bàn trống!");
                return;
            }

            System.out.print("Chọn ID bàn: ");
            int tableId = Integer.parseInt(new Scanner(System.in).nextLine());

            String updateTable = "UPDATE restaurant_tables SET status='OCCUPIED' WHERE id=?";
            PreparedStatement psUpdate = conn.prepareStatement(updateTable);
            psUpdate.setInt(1, tableId);
            psUpdate.executeUpdate();

            String insertOrder = "INSERT INTO orders(table_id, user_id, total_amount) VALUES (?,?,0)";
            PreparedStatement psOrder = conn.prepareStatement(insertOrder);
            psOrder.setInt(1, tableId);
            psOrder.setInt(2, userId);
            psOrder.executeUpdate();

            System.out.println("Đặt bàn thành công!");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void deleteTable() {
        Scanner sc = new Scanner(System.in);

        try (Connection conn = DBConnection.getConnection()) {

            String sql = "SELECT * FROM restaurant_tables";
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();

            System.out.println("\n========== DANH SÁCH BÀN ==========");
            System.out.printf("| %-4s | %-15s | %-8s | %-10s |\n", "ID", "Tên bàn", "Số chỗ", "Trạng thái");
            System.out.println("-----------------------------------------------------");

            while (rs.next()) {
                System.out.printf("| %-4d | %-15s | %-8d | %-10s |\n",
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getInt("capacity"),
                        rs.getString("status"));
            }
            System.out.println("====================================\n");

            System.out.print("Nhập ID bàn cần xoá: ");
            int id = Integer.parseInt(sc.nextLine());

            String checkSql = "SELECT * FROM restaurant_tables WHERE id=?";
            PreparedStatement psCheck = conn.prepareStatement(checkSql);
            psCheck.setInt(1, id);
            ResultSet rsCheck = psCheck.executeQuery();

            if (!rsCheck.next()) {
                System.out.println("Bàn không tồn tại!");
                return;
            }

            String status = rsCheck.getString("status");

            if (status.equals("OCCUPIED")) {
                System.out.println("Không thể xoá bàn đang được sử dụng!");
                return;
            }

            String deleteOrders = "DELETE FROM orders WHERE table_id=?";
            PreparedStatement psDelOrder = conn.prepareStatement(deleteOrders);
            psDelOrder.setInt(1, id);
            psDelOrder.executeUpdate();

            String deleteSql = "DELETE FROM restaurant_tables WHERE id=?";
            PreparedStatement psDelete = conn.prepareStatement(deleteSql);
            psDelete.setInt(1, id);
            psDelete.executeUpdate();

            System.out.println("Xoá bàn thành công!");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void updateTableStatus() {
        Scanner sc = new Scanner(System.in);

        try (Connection conn = DBConnection.getConnection()) {

            String sql = "SELECT * FROM restaurant_tables";
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();

            System.out.println("=== DANH SÁCH BÀN ===");
            while (rs.next()) {
                System.out.println(
                        rs.getInt("id") + " | " +
                                rs.getString("name") + " | " +
                                rs.getString("status")
                );
            }

            System.out.print("Nhập ID bàn: ");
            int id = Integer.parseInt(sc.nextLine());

            String checkSql = "SELECT * FROM restaurant_tables WHERE id=?";
            PreparedStatement psCheck = conn.prepareStatement(checkSql);
            psCheck.setInt(1, id);
            ResultSet rsCheck = psCheck.executeQuery();

            if (!rsCheck.next()) {
                System.out.println("Bàn không tồn tại!");
                return;
            }

            String newStatus;
            do {
                System.out.print("Nhập trạng thái mới (AVAILABLE/OCCUPIED): ");
                newStatus = sc.nextLine().toUpperCase();

                if (!newStatus.equals("AVAILABLE") && !newStatus.equals("OCCUPIED")) {
                    System.out.println("Chỉ được nhập AVAILABLE hoặc OCCUPIED!");
                }

            } while (!newStatus.equals("AVAILABLE") && !newStatus.equals("OCCUPIED"));

            String updateSql = "UPDATE restaurant_tables SET status=? WHERE id=?";
            PreparedStatement psUpdate = conn.prepareStatement(updateSql);
            psUpdate.setString(1, newStatus);
            psUpdate.setInt(2, id);

            psUpdate.executeUpdate();

            System.out.println("Cập nhật trạng thái thành công!");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void viewMyTables(int userId) {
        try (Connection conn = DBConnection.getConnection()) {

            String sql = """
            SELECT o.id AS order_id, t.name, t.capacity, t.status
            FROM orders o
            JOIN restaurant_tables t ON o.table_id = t.id
            WHERE o.user_id = ?
        """;

            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, userId);

            ResultSet rs = ps.executeQuery();

            boolean hasData = false;

            System.out.println("\n====== BÀN ĐÃ ĐẶT ======");
            System.out.printf("| %-5s | %-15s | %-10s | %-10s |\n",
                    "Order", "Tên bàn", "Sức chứa", "Trạng thái");
            System.out.println("--------------------------------------------------");

            while (rs.next()) {
                hasData = true;
                System.out.printf("| %-5d | %-15s | %-10d | %-10s |\n",
                        rs.getInt("order_id"),
                        rs.getString("name"),
                        rs.getInt("capacity"),
                        rs.getString("status"));
            }

            if (!hasData) {
                System.out.println("Bạn chưa đặt bàn nào!");
            }

            System.out.println("====================================\n");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
