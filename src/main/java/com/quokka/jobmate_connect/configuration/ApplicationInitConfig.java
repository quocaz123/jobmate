package com.quokka.jobmate_connect.configuration;

import com.quokka.jobmate_connect.entity.Role;
import com.quokka.jobmate_connect.repository.RoleRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class ApplicationInitConfig {

    RoleRepository roleRepository;

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
                if(exists) {
                    log.info("Role '{}' đã tồn tại, bỏ qua", roleName);
                    return;
                }
                else {
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
}
