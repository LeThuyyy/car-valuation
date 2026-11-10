package com.carvaluation.vin;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Nhà cung cấp giải mã VIN dành riêng cho thị trường Việt Nam
 * Hỗ trợ các dòng xe sản xuất nội địa (VinFast, Thaco Kia/Mazda, TC Motor Hyundai, Toyota/Honda VN)
 * và xe nhập khẩu nguyên chiếc phổ biến (Thái Lan, Indonesia, Nhật Bản, Hàn Quốc).
 */
@Component
public class VietnamMarketVinDecoderProvider implements VinDecoderProvider {

    // Bảng tra cứu năm sản xuất ký tự thứ 10 theo ISO 3779
    private static final Map<Character, Integer> YEAR_MAP = new HashMap<>();
    static {
        YEAR_MAP.put('A', 2010); YEAR_MAP.put('B', 2011); YEAR_MAP.put('C', 2012);
        YEAR_MAP.put('D', 2013); YEAR_MAP.put('E', 2014); YEAR_MAP.put('F', 2015);
        YEAR_MAP.put('G', 2016); YEAR_MAP.put('H', 2017); YEAR_MAP.put('J', 2018);
        YEAR_MAP.put('K', 2019); YEAR_MAP.put('L', 2020); YEAR_MAP.put('M', 2021);
        YEAR_MAP.put('N', 2022); YEAR_MAP.put('P', 2023); YEAR_MAP.put('R', 2024);
        YEAR_MAP.put('S', 2025); YEAR_MAP.put('T', 2026); YEAR_MAP.put('V', 2027);
        YEAR_MAP.put('W', 2028); YEAR_MAP.put('X', 2029); YEAR_MAP.put('Y', 2030);
    }

    @Override
    public String getProviderName() {
        return "VIETNAM_MARKET_PROVIDER";
    }

    @Override
    public boolean supports(String vin) {
        return vin != null && vin.length() == 17;
    }

    @Override
    public Optional<VinScanResult> decode(String vin) {
        String wmi = vin.substring(0, 3).toUpperCase();
        char yearChar = vin.charAt(9);
        Integer year = YEAR_MAP.get(yearChar);

        VinScanResult result = new VinScanResult();
        result.setSuccess(true);
        result.setValid(true);
        result.setVin(vin);
        result.setYear(year);
        result.setSource(getProviderName());
        result.setNeedsConfirmation(true);

        // VinFast Việt Nam (RL4, RL1, VNK...)
        if (wmi.startsWith("RL") || wmi.equals("VNK")) {
            result.setMake("VinFast");
            result.setCountry("Việt Nam");
            result.setOrigin("Lắp ráp trong nước (CKD Hải Phòng)");
            decodeVinFastModel(vin, result);
            return Optional.of(result);
        }

        // Honda Việt Nam / Thái Lan
        if (wmi.equals("MRH") || wmi.equals("RLH")) {
            result.setMake("Honda");
            result.setCountry(wmi.equals("MRH") ? "Thái Lan / Việt Nam" : "Việt Nam");
            result.setOrigin("CKD / Nhập Thái");
            decodeHondaModel(vin, result);
            return Optional.of(result);
        }

        // Toyota (MR0, MRO, AHT, MHR)
        if (wmi.equals("MR0") || wmi.equals("MRO") || wmi.equals("MHR") || wmi.equals("JTE") || wmi.equals("JT1")) {
            result.setMake("Toyota");
            if (wmi.startsWith("J")) {
                result.setCountry("Nhật Bản");
                result.setOrigin("Nhập khẩu nguyên chiếc (CBU Nhật)");
            } else if (wmi.equals("MHR")) {
                result.setCountry("Indonesia");
                result.setOrigin("Nhập khẩu (Indonesia)");
            } else {
                result.setCountry("Việt Nam / Thái Lan");
                result.setOrigin("CKD Vĩnh Phúc / Nhập Thái");
            }
            decodeToyotaModel(vin, result);
            return Optional.of(result);
        }

        // Hyundai (KMH, TMA, MAL...)
        if (wmi.equals("KMH") || wmi.equals("MAL") || wmi.equals("RLH")) {
            result.setMake("Hyundai");
            result.setCountry(wmi.equals("KMH") ? "Hàn Quốc" : "Việt Nam");
            result.setOrigin("Lắp ráp TC Motor Ninh Bình");
            decodeHyundaiModel(vin, result);
            return Optional.of(result);
        }

        // Kia (KNA, KNE) hoặc THACO lắp ráp
        if (wmi.equals("KNA") || wmi.equals("KNE") || wmi.equals("RLK")) {
            result.setMake("Kia");
            result.setCountry("Hàn Quốc / Việt Nam");
            result.setOrigin("Lắp ráp THACO Chu Lai");
            decodeKiaModel(vin, result);
            return Optional.of(result);
        }

        // Mazda (JMZ, MM8, RLM)
        if (wmi.equals("JMZ") || wmi.equals("MM8") || wmi.equals("RLM")) {
            result.setMake("Mazda");
            result.setCountry(wmi.equals("JMZ") ? "Nhật Bản" : (wmi.equals("MM8") ? "Thái Lan" : "Việt Nam"));
            result.setOrigin("Lắp ráp THACO Chu Lai");
            decodeMazdaModel(vin, result);
            return Optional.of(result);
        }

        // Ford (MN1, MNA, 1FA, 3FA)
        if (wmi.equals("MN1") || wmi.equals("MNA") || wmi.startsWith("1F") || wmi.startsWith("3F")) {
            result.setMake("Ford");
            result.setCountry(wmi.startsWith("MN") ? "Thái Lan / Việt Nam" : "Mỹ");
            result.setOrigin("Lắp ráp Hải Dương / Nhập Thái");
            decodeFordModel(vin, result);
            return Optional.of(result);
        }

        // Nhận diện cơ bản năm và quốc gia nếu không khớp các hãng trên
        if (year != null) {
            result.setYear(year);
            result.setMessage("Nhận diện được năm sản xuất " + year + " từ VIN. Vui lòng xác nhận hãng và dòng xe.");
            return Optional.of(result);
        }

        return Optional.empty();
    }

    private void decodeVinFastModel(String vin, VinScanResult res) {
        char c4 = vin.charAt(3);
        char c5 = vin.charAt(4);
        String sub = ("" + c4 + c5).toUpperCase();

        if (sub.contains("3") || sub.contains("F3")) {
            res.setModel("VF 3");
            res.setBodyType("Mini SUV");
            res.setFuelType("Điện");
            res.setEngine("Động cơ điện 32kW");
        } else if (sub.contains("5") || sub.contains("F5")) {
            res.setModel("VF 5 Plus");
            res.setBodyType("A-SUV");
            res.setFuelType("Điện");
            res.setEngine("Động cơ điện 100kW");
        } else if (sub.contains("6") || sub.contains("F6")) {
            res.setModel("VF 6");
            res.setBodyType("B-SUV");
            res.setFuelType("Điện");
        } else if (sub.contains("7") || sub.contains("F7")) {
            res.setModel("VF 7");
            res.setBodyType("C-SUV");
            res.setFuelType("Điện");
        } else if (sub.contains("8") || sub.contains("F8")) {
            res.setModel("VF 8");
            res.setBodyType("D-SUV");
            res.setFuelType("Điện");
            res.setEngine("Dual Motor AWD 300kW");
        } else if (sub.contains("9") || sub.contains("F9")) {
            res.setModel("VF 9");
            res.setBodyType("E-SUV (7 chỗ)");
            res.setFuelType("Điện");
        } else if (sub.contains("A") || sub.contains("LA")) {
            res.setModel("Lux A2.0");
            res.setBodyType("Sedan hạng E");
            res.setFuelType("Xăng");
            res.setEngine("2.0L Turbo BMW");
            res.setTransmission("Tự động 8 cấp ZF");
        } else if (sub.contains("S") || sub.contains("SA")) {
            res.setModel("Lux SA2.0");
            res.setBodyType("SUV 7 chỗ");
            res.setFuelType("Xăng");
            res.setEngine("2.0L Turbo BMW");
        } else if (sub.contains("D") || sub.contains("FD")) {
            res.setModel("Fadil");
            res.setBodyType("Hatchback hạng A");
            res.setFuelType("Xăng");
            res.setEngine("1.4L");
            res.setTransmission("CVT");
        } else {
            res.setModel("VinFast (Cần xác nhận phiên bản)");
            res.setFuelType("Điện / Xăng");
        }
        res.setMessage("Giải mã thành công xe VinFast thị trường Việt Nam");
    }

    private void decodeHondaModel(String vin, VinScanResult res) {
        char c4 = vin.charAt(3);
        if (c4 == 'G' || c4 == 'C') {
            res.setModel("City");
            res.setBodyType("Sedan hạng B");
            res.setEngine("1.5L i-VTEC");
            res.setTransmission("CVT");
            res.setFuelType("Xăng");
        } else if (c4 == 'R' || c4 == 'W') {
            res.setModel("CR-V");
            res.setBodyType("Crossover 5+2");
            res.setEngine("1.5L VTEC Turbo");
            res.setFuelType("Xăng / e:HEV Hybrid");
        } else if (c4 == 'F') {
            res.setModel("Civic");
            res.setBodyType("Sedan hạng C");
            res.setEngine("1.5L Turbo");
            res.setFuelType("Xăng");
        } else if (c4 == 'H') {
            res.setModel("HR-V");
            res.setBodyType("B-SUV");
            res.setFuelType("Xăng");
        } else {
            res.setModel("Honda (Cần xác nhận dòng xe)");
        }
        res.setMessage("Giải mã xe Honda thị trường Việt Nam");
    }

    private void decodeToyotaModel(String vin, VinScanResult res) {
        char c4 = vin.charAt(3);
        if (c4 == 'B' || c4 == 'P') {
            res.setModel("Vios");
            res.setBodyType("Sedan hạng B");
            res.setEngine("1.5L 2NR-FE Dual VVT-i");
            res.setTransmission("CVT / Số sàn");
            res.setFuelType("Xăng");
        } else if (c4 == 'T' || c4 == 'K') {
            res.setModel("Corolla Cross");
            res.setBodyType("C-SUV");
            res.setFuelType("Xăng / Hybrid");
            res.setEngine("1.8L");
        } else if (c4 == 'N') {
            res.setModel("Innova");
            res.setBodyType("MPV 7 chỗ");
            res.setEngine("2.0L");
            res.setFuelType("Xăng");
        } else if (c4 == 'F') {
            res.setModel("Fortuner");
            res.setBodyType("SUV 7 chỗ");
            res.setFuelType("Dầu (Diesel) / Xăng");
        } else if (c4 == 'W') {
            res.setModel("Veloz Cross");
            res.setBodyType("MPV 7 chỗ");
            res.setEngine("1.5L");
            res.setTransmission("CVT");
        } else {
            res.setModel("Toyota (Cần xác nhận dòng xe)");
        }
        res.setMessage("Giải mã xe Toyota thị trường Việt Nam");
    }

    private void decodeHyundaiModel(String vin, VinScanResult res) {
        char c4 = vin.charAt(3);
        if (c4 == 'C' || c4 == 'A') {
            res.setModel("Accent");
            res.setBodyType("Sedan hạng B");
            res.setEngine("1.4L / 1.5L Kappa");
            res.setFuelType("Xăng");
        } else if (c4 == 'T') {
            res.setModel("Tucson");
            res.setBodyType("C-SUV");
            res.setFuelType("Xăng / Dầu");
        } else if (c4 == 'S') {
            res.setModel("Santa Fe");
            res.setBodyType("SUV 7 chỗ");
            res.setFuelType("Dầu 2.2L / Xăng 2.5L");
        } else if (c4 == 'K') {
            res.setModel("Creta");
            res.setBodyType("B-SUV");
            res.setEngine("1.5L SmartStream");
            res.setFuelType("Xăng");
        } else if (c4 == 'I') {
            res.setModel("Grand i10");
            res.setBodyType("Hatchback / Sedan hạng A");
            res.setEngine("1.2L");
            res.setFuelType("Xăng");
        } else {
            res.setModel("Hyundai (Cần xác nhận dòng xe)");
        }
        res.setMessage("Giải mã xe Hyundai TC Motor");
    }

    private void decodeKiaModel(String vin, VinScanResult res) {
        char c4 = vin.charAt(3);
        if (c4 == 'S') {
            res.setModel("Seltos");
            res.setBodyType("B-SUV");
            res.setEngine("1.4L Turbo / 1.6L");
            res.setFuelType("Xăng");
        } else if (c4 == 'K' || c4 == 'B') {
            res.setModel("K3 / Cerato");
            res.setBodyType("Sedan hạng C");
            res.setEngine("1.6L / 2.0L");
            res.setFuelType("Xăng");
        } else if (c4 == 'M') {
            res.setModel("Morning");
            res.setBodyType("Hatchback hạng A");
            res.setEngine("1.25L");
            res.setFuelType("Xăng");
        } else if (c4 == 'C') {
            res.setModel("Carnival");
            res.setBodyType("MPV cao cấp");
            res.setEngine("Dầu 2.2L Smartstream");
            res.setFuelType("Dầu (Diesel)");
        } else if (c4 == 'O') {
            res.setModel("Sonet");
            res.setBodyType("A-SUV");
            res.setEngine("1.5L");
            res.setFuelType("Xăng");
        } else {
            res.setModel("Kia (Cần xác nhận dòng xe)");
        }
        res.setMessage("Giải mã xe Kia THACO");
    }

    private void decodeMazdaModel(String vin, VinScanResult res) {
        char c4 = vin.charAt(3);
        if (c4 == '5' || c4 == 'C') {
            res.setModel("CX-5");
            res.setBodyType("Crossover 5 chỗ");
            res.setEngine("2.0L / 2.5L SkyActiv-G");
            res.setTransmission("Tự động 6 cấp");
            res.setFuelType("Xăng");
        } else if (c4 == '3') {
            res.setModel("Mazda 3");
            res.setBodyType("Sedan / Hatchback hạng C");
            res.setEngine("1.5L SkyActiv");
            res.setFuelType("Xăng");
        } else if (c4 == '8') {
            res.setModel("CX-8");
            res.setBodyType("SUV 7 chỗ");
            res.setEngine("2.5L SkyActiv");
            res.setFuelType("Xăng");
        } else if (c4 == '2') {
            res.setModel("Mazda 2");
            res.setBodyType("Sedan / Hatchback hạng B");
            res.setEngine("1.5L");
            res.setFuelType("Xăng");
        } else {
            res.setModel("Mazda (Cần xác nhận dòng xe)");
        }
        res.setMessage("Giải mã xe Mazda THACO");
    }

    private void decodeFordModel(String vin, VinScanResult res) {
        char c4 = vin.charAt(3);
        if (c4 == 'R' || c4 == 'T') {
            res.setModel("Ranger");
            res.setBodyType("Bán tải (Pickup)");
            res.setEngine("2.0L Single Turbo / Bi-Turbo");
            res.setFuelType("Dầu (Diesel)");
            res.setTransmission("Số tự động 6AT / 10AT");
        } else if (c4 == 'E') {
            res.setModel("Everest");
            res.setBodyType("SUV 7 chỗ");
            res.setEngine("2.0L Bi-Turbo Diesel");
            res.setFuelType("Dầu (Diesel)");
        } else if (c4 == 'X') {
            res.setModel("Territory");
            res.setBodyType("C-SUV");
            res.setEngine("1.5L EcoBoost");
            res.setFuelType("Xăng");
        } else {
            res.setModel("Ford (Cần xác nhận dòng xe)");
        }
        res.setMessage("Giải mã xe Ford thị trường Việt Nam");
    }

    @Override
    public int getOrder() {
        return 5; // Ưu tiên chạy trước NHTSA vì tối ưu hơn cho xe tại Việt Nam
    }
}
