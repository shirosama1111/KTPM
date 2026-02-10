package com.example.demo4.dao;

import com.example.demo4.Database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;

public class StatisticsDao {

    public static int countHouseholds() throws Exception {
        String sql = "SELECT COUNT(*) FROM households";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            rs.next();
            return rs.getInt(1);
        }
    }

    public static int countCitizens() throws Exception {
        String sql = "SELECT COUNT(*) FROM citizens";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            rs.next();
            return rs.getInt(1);
        }
    }

    public static Map<String, Integer> citizenByGender() throws Exception {
        Map<String, Integer> map = new LinkedHashMap<>();
        String sql = """
            SELECT 
                CASE 
                    WHEN gender IS NULL OR gender = '' THEN N'Chưa rõ'
                    ELSE gender 
                END as gender_group,
                COUNT(*) as total
            FROM citizens
            GROUP BY gender
        """;

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next())
                map.put(rs.getString("gender_group"), rs.getInt("total"));
        }
        return map;
    }

    /**
     * ✅ Thống kê theo nhóm tuổi CHI TIẾT như yêu cầu case study:
     * - Mầm non (0-2 tuổi)
     * - Mẫu giáo (3-5 tuổi)
     * - Cấp 1 (6-10 tuổi)
     * - Cấp 2 (11-14 tuổi)
     * - Cấp 3 (15-17 tuổi)
     * - Độ tuổi lao động (18-60 tuổi)
     * - Nghỉ hưu (>60 tuổi)
     */
    public static Map<String, Integer> citizenByDetailedAgeGroup() throws Exception {
        Map<String, Integer> map = new LinkedHashMap<>();
        String sql = """
            SELECT
              CASE
                WHEN DATEDIFF(YEAR, dob, GETDATE()) <= 2 THEN N'Mầm non (0-2)'
                WHEN DATEDIFF(YEAR, dob, GETDATE()) BETWEEN 3 AND 5 THEN N'Mẫu giáo (3-5)'
                WHEN DATEDIFF(YEAR, dob, GETDATE()) BETWEEN 6 AND 10 THEN N'Cấp 1 (6-10)'
                WHEN DATEDIFF(YEAR, dob, GETDATE()) BETWEEN 11 AND 14 THEN N'Cấp 2 (11-14)'
                WHEN DATEDIFF(YEAR, dob, GETDATE()) BETWEEN 15 AND 17 THEN N'Cấp 3 (15-17)'
                WHEN DATEDIFF(YEAR, dob, GETDATE()) BETWEEN 18 AND 60 THEN N'Lao động (18-60)'
                WHEN DATEDIFF(YEAR, dob, GETDATE()) > 60 THEN N'Nghỉ hưu (>60)'
                ELSE N'Chưa rõ'
              END AS age_group,
              COUNT(*) total
            FROM citizens
            WHERE dob IS NOT NULL
            GROUP BY
              CASE
                WHEN DATEDIFF(YEAR, dob, GETDATE()) <= 2 THEN N'Mầm non (0-2)'
                WHEN DATEDIFF(YEAR, dob, GETDATE()) BETWEEN 3 AND 5 THEN N'Mẫu giáo (3-5)'
                WHEN DATEDIFF(YEAR, dob, GETDATE()) BETWEEN 6 AND 10 THEN N'Cấp 1 (6-10)'
                WHEN DATEDIFF(YEAR, dob, GETDATE()) BETWEEN 11 AND 14 THEN N'Cấp 2 (11-14)'
                WHEN DATEDIFF(YEAR, dob, GETDATE()) BETWEEN 15 AND 17 THEN N'Cấp 3 (15-17)'
                WHEN DATEDIFF(YEAR, dob, GETDATE()) BETWEEN 18 AND 60 THEN N'Lao động (18-60)'
                WHEN DATEDIFF(YEAR, dob, GETDATE()) > 60 THEN N'Nghỉ hưu (>60)'
                ELSE N'Chưa rõ'
              END
            ORDER BY
              CASE age_group
                WHEN N'Mầm non (0-2)' THEN 1
                WHEN N'Mẫu giáo (3-5)' THEN 2
                WHEN N'Cấp 1 (6-10)' THEN 3
                WHEN N'Cấp 2 (11-14)' THEN 4
                WHEN N'Cấp 3 (15-17)' THEN 5
                WHEN N'Lao động (18-60)' THEN 6
                WHEN N'Nghỉ hưu (>60)' THEN 7
                ELSE 8
              END
        """;

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next())
                map.put(rs.getString("age_group"), rs.getInt("total"));
        }
        return map;
    }

    /**
     * ✅ GIỮ NGUYÊN method cũ cho tương thích ngược
     */
    public static Map<String, Integer> citizenByAgeGroup() throws Exception {
        Map<String, Integer> map = new LinkedHashMap<>();
        String sql = """
            SELECT
              CASE
                WHEN DATEDIFF(YEAR, dob, GETDATE()) < 18 THEN N'Dưới 18'
                WHEN DATEDIFF(YEAR, dob, GETDATE()) BETWEEN 18 AND 60 THEN N'18–60'
                ELSE N'Trên 60'
              END AS age_group,
              COUNT(*) total
            FROM citizens
            WHERE dob IS NOT NULL
            GROUP BY
              CASE
                WHEN DATEDIFF(YEAR, dob, GETDATE()) < 18 THEN N'Dưới 18'
                WHEN DATEDIFF(YEAR, dob, GETDATE()) BETWEEN 18 AND 60 THEN N'18–60'
                ELSE N'Trên 60'
              END
        """;

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next())
                map.put(rs.getString("age_group"), rs.getInt("total"));
        }
        return map;
    }

    /**
     * ✅ Thống kê thay đổi nhân khẩu theo khoảng thời gian
     */
    public static Map<String, Integer> citizenChangesByPeriod(LocalDate from, LocalDate to) throws Exception {
        Map<String, Integer> map = new LinkedHashMap<>();
        String sql = """
            SELECT
              change_type,
              COUNT(*) as total
            FROM citizen_changes
            WHERE change_date BETWEEN ? AND ?
            GROUP BY change_type
        """;

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setDate(1, java.sql.Date.valueOf(from));
            ps.setDate(2, java.sql.Date.valueOf(to));

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String type = rs.getString("change_type");
                    String displayType = switch(type) {
                        case "MOVE_OUT" -> "Chuyển đi";
                        case "DEAD" -> "Qua đời";
                        case "NEW_BORN" -> "Sinh mới";
                        default -> type;
                    };
                    map.put(displayType, rs.getInt("total"));
                }
            }
        }
        return map;
    }

    /**
     * ✅ Thống kê tạm vắng/tạm trú hiện tại
     */
    public static Map<String, Integer> temporaryRecordStats() throws Exception {
        Map<String, Integer> map = new LinkedHashMap<>();
        String sql = """
            SELECT
              CASE type
                WHEN 'TAM_VANG' THEN N'Tạm vắng'
                WHEN 'TAM_TRU' THEN N'Tạm trú'
                ELSE type
              END as record_type,
              COUNT(*) as total
            FROM temporary_records
            WHERE end_date IS NULL OR end_date >= GETDATE()
            GROUP BY type
        """;

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next())
                map.put(rs.getString("record_type"), rs.getInt("total"));
        }
        return map;
    }

    /**
     * ✅ Thống kê tạm vắng/tạm trú theo khoảng thời gian
     */
    public static Map<String, Integer> temporaryRecordsByPeriod(LocalDate from, LocalDate to) throws Exception {
        Map<String, Integer> map = new LinkedHashMap<>();
        String sql = """
            SELECT
              CASE type
                WHEN 'TAM_VANG' THEN N'Tạm vắng'
                WHEN 'TAM_TRU' THEN N'Tạm trú'
                ELSE type
              END as record_type,
              COUNT(*) as total
            FROM temporary_records
            WHERE start_date BETWEEN ? AND ?
            GROUP BY type
        """;

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setDate(1, java.sql.Date.valueOf(from));
            ps.setDate(2, java.sql.Date.valueOf(to));

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next())
                    map.put(rs.getString("record_type"), rs.getInt("total"));
            }
        }
        return map;
    }

    /**
     * ✅ Thống kê dân tộc
     */
    public static Map<String, Integer> citizenByEthnicity() throws Exception {
        Map<String, Integer> map = new LinkedHashMap<>();
        String sql = """
            SELECT 
                CASE 
                    WHEN ethnicity IS NULL OR ethnicity = '' THEN N'Chưa rõ'
                    ELSE ethnicity 
                END as ethnicity_group,
                COUNT(*) as total
            FROM citizens
            GROUP BY ethnicity
            ORDER BY total DESC
        """;

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next())
                map.put(rs.getString("ethnicity_group"), rs.getInt("total"));
        }
        return map;
    }

    /**
     * ✅ Thống kê nghề nghiệp (top 15)
     */
    public static Map<String, Integer> citizenByJob() throws Exception {
        Map<String, Integer> map = new LinkedHashMap<>();
        String sql = """
            SELECT TOP 15
                CASE 
                    WHEN job IS NULL OR job = '' THEN N'Chưa rõ'
                    ELSE job 
                END as job_group,
                COUNT(*) as total
            FROM citizens
            GROUP BY job
            ORDER BY total DESC
        """;

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next())
                map.put(rs.getString("job_group"), rs.getInt("total"));
        }
        return map;
    }
}