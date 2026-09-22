package duan.com.example.utils;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

public class ChatRoleUtil {
    private ChatRoleUtil() {}

    public static boolean isStaff(Authentication authentication) {
        if (authentication == null) return false;
        for (GrantedAuthority a : authentication.getAuthorities()) {
            String auth = a.getAuthority();
            if ("Admin".equals(auth) || "Employee".equals(auth)) {
                return true;
            }
        }
        return false;
    }
}