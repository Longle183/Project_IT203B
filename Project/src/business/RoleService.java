package business;

import entity.User;

public class RoleService {

    public static boolean isManager(User user) {
        return user.getRole().equalsIgnoreCase("MANAGER");
    }

    public static boolean isChef(User user) {
        return user.getRole().equalsIgnoreCase("CHEF");
    }

    public static boolean isCustomer(User user) {
        return user.getRole().equalsIgnoreCase("CUSTOMER");
    }
}