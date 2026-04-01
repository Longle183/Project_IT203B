package presentation;

import business.*;
import entity.User;

import java.util.*;

public class Main {
    static Scanner sc = new Scanner(System.in);
    static MenuService menuService = new MenuService();
    static TableService tableService = new TableService();
    static AuthService authService = new AuthService();

    public static void main(String[] args) throws Exception {
        while (true) {
            System.out.println("===== SYSTEM =====");
            System.out.println("1. Đăng ký");
            System.out.println("2. Đăng nhập");
            System.out.println("0. Thoát");

            int choice = Integer.parseInt(sc.nextLine());

            switch (choice) {
                case 1:
                    authService.register();
                    break;
                case 2:
                    User user = authService.login();
                    if (user != null) mainMenu(user);
                    break;
                case 0:
                    System.exit(0);
                    break;
                default:
                    System.out.println("Lựa chọn không hợp lệ!");
            }
        }
    }

    // ================= MAIN MENU =================
    static void mainMenu(User user) throws Exception {
        while (true) {
            System.out.println("\n===== MENU CHÍNH =====");
            System.out.println("User: " + user.getUsername() + " | Role: " + user.getRole());

            if (RoleService.isManager(user)) {
                System.out.println("1. Quản lý món ăn");
                System.out.println("2. Quản lý bàn");
                System.out.println("0. Đăng xuất");

                int choice = Integer.parseInt(sc.nextLine());

                switch (choice) {
                    case 1:
                        menuMenu();
                        break;
                    case 2:
                        tableMenu();
                        break;
                    case 0:
                        return;
                    default:
                        System.out.println("Sai lựa chọn!");
                }
            }

            else if (RoleService.isChef(user)) {
                System.out.println("1. Xem menu");
                System.out.println("0. Đăng xuất");

                int choice = Integer.parseInt(sc.nextLine());

                switch (choice) {
                    case 1:
                        menuService.viewMenu();
                        break;
                    case 0:
                        return;
                    default:
                        System.out.println("Sai lựa chọn!");
                }
            }

            else if (RoleService.isCustomer(user)) {
                System.out.println("1. Xem menu");
                System.out.println("2. Đặt bàn");
                System.out.println("3. Xem bàn đã đặt");
                System.out.println("4. Gọi món");
                System.out.println("5. Xem món đã gọi");
                System.out.println("0. Đăng xuất");

                int choice = Integer.parseInt(sc.nextLine());

                switch (choice) {
                    case 1:
                        menuService.viewMenu();
                        break;
                    case 2:
                        tableService.bookTable(user.getId());
                        break;
                    case 3:
                        tableService.viewMyTables(user.getId());
                        break;
                    case 4:
                        menuService.orderFood(user.getId());
                        break;
                    case 5:
                        menuService.viewOrder(user.getId());
                        break;
                    case 0:
                        return;
                    default:
                        System.out.println("Sai lựa chọn!");
                }
            }
        }
    }

    // ================= MENU =================
    static void menuMenu() throws Exception {
        while (true) {
            System.out.println("=== MENU ===");
            System.out.println("1. Thêm món");
            System.out.println("2. Xem menu");
            System.out.println("3. Xoá món");
            System.out.println("4. Tìm kiếm món");
            System.out.println("0. Quay lại");

            int c = Integer.parseInt(sc.nextLine());

            switch (c) {
                case 1:
                    menuService.addMenu();
                    break;
                case 2:
                    menuService.viewMenu();
                    break;
                case 3:
                    menuService.deleteMenu();
                    break;
                case 4:
                    menuService.searchMenuByName();
                    break;
                case 0:
                    return;
                default:
                    System.out.println("Sai lựa chọn!");
            }
        }
    }

    // ================= TABLE =================
    static void tableMenu() throws Exception {
        while (true) {
            System.out.println("=== TABLE ===");
            System.out.println("1. Thêm bàn");
            System.out.println("2. Xem bàn");
            System.out.println("3. Xoá bàn");
            System.out.println("4. Cập nhật trạng thái bàn");
            System.out.println("0. Quay lại");

            int c = Integer.parseInt(sc.nextLine());

            switch (c) {
                case 1:
                    tableService.addTable();
                    break;
                case 2:
                    tableService.viewTables();
                    break;
                case 3:
                    tableService.deleteTable();
                    break;
                case 4:
                    tableService.updateTableStatus();
                    break;
                case 0:
                    return;
                default:
                    System.out.println("Sai lựa chọn!");
            }
        }
    }
}