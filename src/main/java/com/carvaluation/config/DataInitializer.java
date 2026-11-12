package com.carvaluation.config;

import com.carvaluation.auth.Role;
import com.carvaluation.auth.User;
import com.carvaluation.auth.UserRepository;
import com.carvaluation.pricing.ReferenceCarPrice;
import com.carvaluation.pricing.ReferenceCarPriceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Profile("!prod")
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final UserRepository userRepository;
    private final ReferenceCarPriceRepository pricingRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository,
                           ReferenceCarPriceRepository pricingRepository,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.pricingRepository = pricingRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        initDefaultUsers();
        initReferencePricingData();
    }

    private void initDefaultUsers() {
        if (userRepository.count() == 0) {
            log.info("Khởi tạo tài khoản mẫu mặc định cho hệ thống...");

            // 1. Quản trị viên
            User admin = new User("admin", "admin@carvaluation.vn",
                    passwordEncoder.encode("admin123"),
                    "Quản Trị Viên Hệ Thống", "0901234567", Role.ADMIN);
            userRepository.save(admin);

            // 2. Chuyên gia thẩm định
            User expert = new User("expert", "expert@carvaluation.vn",
                    passwordEncoder.encode("expert123"),
                    "Kỹ Sư Thẩm Định Xe", "0912345678", Role.EXPERT);
            userRepository.save(expert);

            // 3. Khách hàng thông thường
            User user = new User("user", "user@carvaluation.vn",
                    passwordEncoder.encode("user123"),
                    "Nguyễn Văn Khách", "0987654321", Role.USER);
            userRepository.save(user);

            log.info("Tạo xong 3 tài khoản: admin/admin123, expert/expert123, user/user123");
        }
    }

    private void initReferencePricingData() {
        if (pricingRepository.count() == 0) {
            log.info("Khởi tạo dữ liệu giá xe tham khảo thị trường Việt Nam...");

            List<ReferenceCarPrice> samplePrices = List.of(
                // Toyota Vios
                new ReferenceCarPrice("Toyota", "Vios", 2023, "1.5G CVT", 592_000_000L, 510_000_000L),
                new ReferenceCarPrice("Toyota", "Vios", 2022, "1.5G CVT", 581_000_000L, 465_000_000L),
                new ReferenceCarPrice("Toyota", "Vios", 2021, "1.5E CVT", 531_000_000L, 415_000_000L),
                new ReferenceCarPrice("Toyota", "Vios", 2020, "1.5E MT", 470_000_000L, 350_000_000L),

                // Honda City
                new ReferenceCarPrice("Honda", "City", 2023, "1.5 RS", 609_000_000L, 535_000_000L),
                new ReferenceCarPrice("Honda", "City", 2022, "1.5 L", 569_000_000L, 475_000_000L),
                new ReferenceCarPrice("Honda", "City", 2021, "1.5 RS", 599_000_000L, 460_000_000L),

                // Hyundai Accent
                new ReferenceCarPrice("Hyundai", "Accent", 2023, "1.4 AT Đặc biệt", 542_000_000L, 480_000_000L),
                new ReferenceCarPrice("Hyundai", "Accent", 2022, "1.4 AT Tiêu chuẩn", 501_000_000L, 420_000_000L),
                new ReferenceCarPrice("Hyundai", "Accent", 2021, "1.4 MT", 472_000_000L, 360_000_000L),

                // Mazda CX-5
                new ReferenceCarPrice("Mazda", "CX-5", 2023, "2.0 Premium Active", 829_000_000L, 750_000_000L),
                new ReferenceCarPrice("Mazda", "CX-5", 2022, "2.0 Luxury", 879_000_000L, 710_000_000L),
                new ReferenceCarPrice("Mazda", "CX-5", 2021, "2.0 Deluxe", 839_000_000L, 640_000_000L),

                // VinFast VF 5 Plus & VF 8
                new ReferenceCarPrice("VinFast", "VF 5 Plus", 2023, "Plus (Kèm pin)", 538_000_000L, 455_000_000L),
                new ReferenceCarPrice("VinFast", "VF 5 Plus", 2024, "Plus (Kèm pin)", 548_000_000L, 485_000_000L),
                new ReferenceCarPrice("VinFast", "VF 8", 2023, "Plus Dual Motor", 1_270_000_000L, 860_000_000L),
                new ReferenceCarPrice("VinFast", "VF 3", 2024, "Tiêu chuẩn (Kèm pin)", 322_000_000L, 290_000_000L),

                // Ford Ranger
                new ReferenceCarPrice("Ford", "Ranger", 2023, "XLS 4x2 AT", 688_000_000L, 610_000_000L),
                new ReferenceCarPrice("Ford", "Ranger", 2022, "Wildtrak 2.0 Bi-Turbo", 965_000_000L, 780_000_000L),

                // Kia Seltos
                new ReferenceCarPrice("Kia", "Seltos", 2023, "1.4 Turbo Premium", 719_000_000L, 620_000_000L),
                new ReferenceCarPrice("Kia", "Seltos", 2022, "1.4 Turbo Luxury", 699_000_000L, 570_000_000L)
            );

            pricingRepository.saveAll(samplePrices);
            log.info("Đã lưu {} bản ghi giá xe tham khảo phổ biến tại Việt Nam", samplePrices.size());
        }
    }
}
