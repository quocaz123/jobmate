package com.quokka.jobmate_connect.configuration;

import com.quokka.jobmate_connect.constant.VerificationStatus;
import com.quokka.jobmate_connect.entity.Role;
import com.quokka.jobmate_connect.entity.User;
import com.quokka.jobmate_connect.repository.RoleRepository;
import com.quokka.jobmate_connect.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.HashSet;
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

    @Bean
    CommandLineRunner initRoles() {
        return args -> {
            Map<String, String> defaultRoles = Map.of(
                    "USER", "Người dùng hệ thống",
                    "ADMIN", "Quản trị viên",
                    "EMPLOYER", "Nhà tuyển dụng"
            );

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
                                                .build()
                                );
                                log.info("Tạo mới role '{}'", roleName);
                            }
                    );
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
                                    .build()
                    ));

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
            log.warn("ĐÃ TẠO tài khoản ADMIN mặc định: email='{}', password='{}'. Hãy đổi mật khẩu ngay sau khi đăng nhập!",
                    adminEmail, adminPassword);
        };
    }
}
