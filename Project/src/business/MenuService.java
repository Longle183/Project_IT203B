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

    public void viewPendingOrders() throws Exception {
        String sql = "SELECT od.id, m.name, od.quantity, od.status, " +
                "t.name AS table_name, u.username " +
                "FROM order_details od " +
                "JOIN menu_items m ON od.menu_item_id = m.id " +
                "JOIN orders o ON od.order_id = o.id " +
                "JOIN restaurant_tables t ON o.table_id = t.id " +
                "JOIN users u ON o.user_id = u.id " +
                "WHERE od.status != 'SERVED'";   // ✅ CHỖ QUAN TRỌNG

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            System.out.println("\n===== DANH SÁCH MÓN (CHƯA SERVED) =====");

            while (rs.next()) {
                System.out.println("ID: " + rs.getInt("id")
                        + " | Món: " + rs.getString("name")
                        + " | SL: " + rs.getInt("quantity")
                        + " | Trạng thái: " + rs.getString("status")
                        + " | Bàn: " + rs.getString("table_name")
                        + " | Khách: " + rs.getString("username"));
            }
        }
    }

    public void updateOrderStatus(int orderDetailId, String status) {
        String sql = "UPDATE order_details SET status = ? WHERE id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, status);
            ps.setInt(2, orderDetailId);

            int rows = ps.executeUpdate();

            if (rows > 0) {
                System.out.println("Cập nhật trạng thái thành công!");
            } else {
                System.out.println("Không tìm thấy ID!");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean isValidTransition(String current, String next) {
        return (current.equals("PENDING") && next.equals("COOKING")) ||
                (current.equals("COOKING") && next.equals("READY")) ||
                (current.equals("READY") && next.equals("SERVED"));
    }

    public void viewMyOrders(int userId) {
        String sql = """
        SELECT rt.name AS table_name,
               m.name AS item_name,
               od.quantity,
               od.status
        FROM order_details od
        JOIN orders o ON od.order_id = o.id
        JOIN menu_items m ON od.menu_item_id = m.id
        JOIN restaurant_tables rt ON o.table_id = rt.id
        WHERE o.user_id = ?
        ORDER BY od.status
    """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();

            System.out.println("\n====== MÓN BẠN ĐÃ GỌI ======");
            System.out.printf("| %-10s | %-20s | %-5s | %-10s |\n",
                    "Bàn", "Tên món", "SL", "Trạng thái");
            System.out.println("------------------------------------------------------");

            boolean found = false;

            while (rs.next()) {
                found = true;

                String status = formatStatus(rs.getString("status"));

                System.out.printf("| %-10s | %-20s | %-5d | %-10s |\n",
                        rs.getString("table_name"),
                        rs.getString("item_name"),
                        rs.getInt("quantity"),
                        status);
            }

            if (!found) {
                System.out.println("Bạn chưa gọi món nào!");
            } else {
                System.out.println("======================================================");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String formatStatus(String status) {
        switch (status) {
            case "PENDING": return " PENDING";
            case "COOKING": return " COOKING";
            case "READY": return " READY";
            case "SERVED": return " SERVED";
            default: return status;
        }
    }
}
