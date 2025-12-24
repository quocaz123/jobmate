package com.quokka.jobmate_connect.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.quokka.jobmate_connect.constant.VerificationStatus;
import com.quokka.jobmate_connect.dto.DefaultCategoriesConfig;
import com.quokka.jobmate_connect.entity.Role;
import com.quokka.jobmate_connect.entity.User;
import com.quokka.jobmate_connect.repository.RoleRepository;
import com.quokka.jobmate_connect.repository.UserRepository;
import com.quokka.jobmate_connect.service.CategoryService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.io.InputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Configuration
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class ApplicationInitConfig {

    RoleRepository roleRepository;
    UserRepository userRepository;
    PasswordEncoder passwordEncoder;
    CategoryService categoryService;

    @Bean
    CommandLineRunner initRoles() {
        return args -> {
            Map<String, String> defaultRoles = Map.of(
                    "USER", "Người dùng hệ thống",
                    "ADMIN", "Quản trị viên",
                    "EMPLOYER", "Nhà tuyển dụng");

            defaultRoles.forEach((roleName, desc) -> {

                boolean exists = roleRepository.existsByName(roleName);
                if (exists) {
                    log.info("Role '{}' đã tồn tại, bỏ qua", roleName);
                    return;
                } else {
                    roleRepository.findByName(roleName).ifPresentOrElse(
                            existing -> log.info("Role '{}' đã tồn tại", roleName),
                            () -> {
                                roleRepository.save(
                                        Role.builder()
                                                .name(roleName)
                                                .description(desc)
                                                .build());
                                log.info("Tạo mới role '{}'", roleName);
                            });
                }
            });
        };
    }

    /**
     * Tạo tài khoản ADMIN mặc định khi khởi động ứng dụng (nếu chưa tồn tại).
     */
    @Bean
    CommandLineRunner initDefaultAdmin() {
        return args -> {
            String adminEmail = "admin@jobmate.com";
            String adminPassword = "Admin@123"; // nên đổi sau khi deploy

            if (userRepository.existsByEmail(adminEmail)) {
                log.info("Admin '{}' đã tồn tại, bỏ qua tạo mới", adminEmail);
                return;
            }

            Role adminRole = roleRepository.findByName("ADMIN")
                    .orElseGet(() -> roleRepository.save(
                            Role.builder()
                                    .name("ADMIN")
                                    .description("Quản trị viên")
                                    .build()));

            Set<Role> roles = new HashSet<>();
            roles.add(adminRole);

            User admin = User.builder()
                    .email(adminEmail)
                    .password(passwordEncoder.encode(adminPassword))
                    .fullName("System Administrator")
                    .status("ACTIVE")
                    .verificationStatus(VerificationStatus.VERIFIED)
                    .roles(roles)
                    .build();

            userRepository.save(admin);
            log.warn(
                    "ĐÃ TẠO tài khoản ADMIN mặc định: email='{}', password='{}'. Hãy đổi mật khẩu ngay sau khi đăng nhập!",
                    adminEmail, adminPassword);
        };
    }

    /**
     * Khởi tạo các categories mặc định khi khởi động ứng dụng (chỉ tạo những
     * category chưa tồn tại).
     * Categories được đọc từ file default-categories.json trong resources.
     */
    @Bean
    CommandLineRunner initDefaultCategories() {
        return args -> {
            log.info("=== Khởi tạo categories mặc định ===");

            List<String> defaultCategories;
            try {
                ObjectMapper mapper = new ObjectMapper();
                InputStream is = getClass().getResourceAsStream("/default-categories.json");

                if (is == null) {
                    log.error("default-categories.json NOT FOUND! Bỏ qua khởi tạo categories.");
                    return;
                }

                DefaultCategoriesConfig config = mapper.readValue(is, DefaultCategoriesConfig.class);
                defaultCategories = config.getCategories();

                if (defaultCategories == null || defaultCategories.isEmpty()) {
                    log.warn("File default-categories.json rỗng hoặc không hợp lệ. Bỏ qua khởi tạo categories.");
                    return;
                }

                log.info("Đã tải {} categories từ file default-categories.json", defaultCategories.size());
            } catch (Exception e) {
                log.error("Lỗi khi đọc file default-categories.json: {}. Bỏ qua khởi tạo categories.", e.getMessage());
                return;
            }

            int totalCategories = defaultCategories.size();
            int createdCount = categoryService.initDefaultCategories(defaultCategories);
            int existingCount = totalCategories - createdCount;

            if (createdCount > 0) {
                log.info("Đã tạo mới {} categories. {} categories đã tồn tại.", createdCount, existingCount);
            } else {
                log.info("Tất cả {} categories đã tồn tại, không có category mới nào được tạo.", totalCategories);
            }
            log.info("=== Hoàn thành khởi tạo categories ===");
        };
    }
}
