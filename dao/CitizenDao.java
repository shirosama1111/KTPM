package com.example.demo4.dao;

import com.example.demo4.Database;
import com.example.demo4.models.Citizen;
import com.example.demo4.models.CitizenChange;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class CitizenDao {

    // ================= INSERT =================
    public static int insert(Citizen c) throws Exception {
        String sql = """
            INSERT INTO citizens(
                full_name, alias, gender, dob, place_of_birth, hometown, ethnicity,
                cccd, cccd_issue_date, cccd_issue_place,
                job, workplace,
                previous_address, register_date,
                status, is_householder, relation,
                household_id, user_id
            )
            VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)
        """;

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, c.getFullName());
            ps.setString(2, c.getAlias());
            ps.setString(3, c.getGender());
            setDate(ps, 4, c.getDob());
            ps.setString(5, c.getPlaceOfBirth());
            ps.setString(6, c.getHometown());
            ps.setString(7, c.getEthnicity());

            ps.setString(8, c.getCccd());
            setDate(ps, 9, c.getCccdIssueDate());
            ps.setString(10, c.getCccdIssuePlace());

            ps.setString(11, c.getJob());
            ps.setString(12, c.getWorkplace());

            ps.setString(13, c.getPreviousAddress());
            setDate(ps, 14, c.getRegisterDate());

            ps.setString(15, c.getStatus());
            ps.setBoolean(16, Boolean.TRUE.equals(c.getHouseholder()));
            ps.setString(17, c.getRelation());

            setInt(ps, 18, c.getHouseholdId());
            setInt(ps, 19, c.getUserId());

            ps.executeUpdate();

            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) {
                c.setId(rs.getInt(1));
                return c.getId();
            }
        }
        throw new Exception("Không thể thêm citizen");
    }

    // ================= FIND =================
    public static List<Citizen> findAll() throws SQLException {
        List<Citizen> list = new ArrayList<>();
        String sql = "SELECT * FROM citizens ORDER BY id DESC";

        try (Connection con = Database.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                list.add(map(rs));
            }
        }
        return list;
    }

    public static List<Citizen> findAvailableForAssign() throws Exception {
        List<Citizen> list = new ArrayList<>();
        String sql = "SELECT * FROM citizens WHERE user_id IS NULL";

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(map(rs));
            }
        }
        return list;
    }

    public static List<Citizen> findByHouseholdId(int householdId) throws Exception {
        List<Citizen> list = new ArrayList<>();
        String sql = "SELECT * FROM citizens WHERE household_id = ?";

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, householdId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(map(rs));
                }
            }
        }
        return list;
    }

    public static List<Citizen> searchByIdPrefix(String prefix) throws Exception {
        String sql = "SELECT TOP 10 * FROM citizens WHERE CAST(id AS VARCHAR) LIKE ?";
        List<Citizen> list = new ArrayList<>();

        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, prefix + "%");
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                list.add(map(rs));
            }
        }
        return list;
    }

    public static List<Citizen> searchByCccdPrefix(String prefix) throws Exception {
        String sql = "SELECT TOP 10 * FROM citizens WHERE cccd LIKE ?";
        List<Citizen> list = new ArrayList<>();

        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, prefix + "%");
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                list.add(map(rs));
            }
        }
        return list;
    }

    public static Citizen findByCccd(String cccd) throws Exception {
        String sql = "SELECT * FROM citizens WHERE cccd = ?";
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, cccd);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) return map(rs);
            return null;
        }
    }

    public static List<Citizen> findWithoutHousehold() throws Exception {
        String sql = "SELECT * FROM citizens WHERE household_id IS NULL";
        List<Citizen> list = new ArrayList<>();

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(map(rs));
            }
        }
        return list;
    }

    public static int countByHousehold(int householdId) throws Exception {
        String sql = "SELECT COUNT(*) FROM citizens WHERE household_id = ?";

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, householdId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) return rs.getInt(1);
        }
        return 0;
    }

    public static void moveCitizenToHousehold(int citizenId, int householdId) throws SQLException {
        try (Connection conn = Database.getConnection()) {
            conn.setAutoCommit(false);

            // 1. Kiểm tra công dân tồn tại
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT id, household_id FROM citizens WHERE id = ?"
            )) {
                ps.setInt(1, citizenId);
                ResultSet rs = ps.executeQuery();
                if (!rs.next()) {
                    throw new SQLException("Công dân không tồn tại!");
                }
            }

            // 2. Kiểm tra hộ khẩu đích tồn tại
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT household_id FROM households WHERE household_id = ?"
            )) {
                ps.setInt(1, householdId);
                ResultSet rs = ps.executeQuery();
                if (!rs.next()) {
                    throw new SQLException("Hộ khẩu đích không tồn tại!");
                }
            }

            // 3. Cập nhật công dân sang hộ khẩu mới
            try (PreparedStatement ps = conn.prepareStatement(
                    "UPDATE citizens SET household_id = ? WHERE id = ?"
            )) {
                ps.setInt(1, householdId);
                ps.setInt(2, citizenId);
                ps.executeUpdate();
            }

            conn.commit();
        }
    }

    // Move citizen sang hộ mới với Connection
    public static void moveCitizenToHousehold(Connection conn, int citizenId, int householdId) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "UPDATE citizens SET household_id = ? WHERE id = ?")) {
            ps.setInt(1, householdId);
            ps.setInt(2, citizenId);
            ps.executeUpdate();
        }
    }

    // Update citizen với Connection
    public static void update(Connection conn, Citizen c) throws SQLException {
        String sql = """
        UPDATE citizens SET
            full_name=?, alias=?, gender=?, dob=?, place_of_birth=?, hometown=?, ethnicity=?,
            cccd=?, cccd_issue_date=?, cccd_issue_place=?,
            job=?, workplace=?,
            previous_address=?, register_date=?,
            status=?, is_householder=?, relation=?,
            household_id=?, user_id=?
        WHERE id=?
    """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, c.getFullName());
            ps.setString(2, c.getAlias());
            ps.setString(3, c.getGender());
            setDate(ps, 4, c.getDob());
            ps.setString(5, c.getPlaceOfBirth());
            ps.setString(6, c.getHometown());
            ps.setString(7, c.getEthnicity());

            ps.setString(8, c.getCccd());
            setDate(ps, 9, c.getCccdIssueDate());
            ps.setString(10, c.getCccdIssuePlace());

            ps.setString(11, c.getJob());
            ps.setString(12, c.getWorkplace());

            ps.setString(13, c.getPreviousAddress());
            setDate(ps, 14, c.getRegisterDate());

            ps.setString(15, c.getStatus());
            ps.setBoolean(16, Boolean.TRUE.equals(c.getHouseholder()));
            ps.setString(17, c.getRelation());

            setInt(ps, 18, c.getHouseholdId());
            setInt(ps, 19, c.getUserId());

            ps.setInt(20, c.getId());

            ps.executeUpdate();
        }
    }


    // ================= UPDATE =================
    public static void update(Citizen c) throws Exception {
        String sql = """
            UPDATE citizens SET
                full_name=?, alias=?, gender=?, dob=?, place_of_birth=?, hometown=?, ethnicity=?,
                cccd=?, cccd_issue_date=?, cccd_issue_place=?,
                job=?, workplace=?,
                previous_address=?, register_date=?,
                status=?, is_householder=?, relation=?,
                household_id=?, user_id=?
            WHERE id=?
        """;

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, c.getFullName());
            ps.setString(2, c.getAlias());
            ps.setString(3, c.getGender());
            setDate(ps, 4, c.getDob());
            ps.setString(5, c.getPlaceOfBirth());
            ps.setString(6, c.getHometown());
            ps.setString(7, c.getEthnicity());

            ps.setString(8, c.getCccd());
            setDate(ps, 9, c.getCccdIssueDate());
            ps.setString(10, c.getCccdIssuePlace());

            ps.setString(11, c.getJob());
            ps.setString(12, c.getWorkplace());

            ps.setString(13, c.getPreviousAddress());
            setDate(ps, 14, c.getRegisterDate());

            ps.setString(15, c.getStatus());
            ps.setBoolean(16, Boolean.TRUE.equals(c.getHouseholder()));
            ps.setString(17, c.getRelation());

            setInt(ps, 18, c.getHouseholdId());
            setInt(ps, 19, c.getUserId());

            ps.setInt(20, c.getId());

            ps.executeUpdate();
        }
    }

    public static Citizen findById(int id) throws Exception {
        String sql = "SELECT * FROM citizens WHERE id=?";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return map(rs);
        }
        return null;
    }

    public static Citizen findByUserId(int userId) throws Exception {
        String sql = "SELECT * FROM citizens WHERE user_id = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return map(rs);
            return null;
        }
    }

    public static void assignUser(int citizenId, int userId) throws SQLException {
        String sql = "UPDATE citizens SET user_id = ? WHERE id = ?";

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ps.setInt(2, citizenId);
            ps.executeUpdate();
        }
    }

    public static List<Citizen> findByHousehold(int householdId) throws Exception {
        List<Citizen> list = new ArrayList<>();
        String sql = "SELECT * FROM citizens WHERE household_id=?";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, householdId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(map(rs));
        }
        return list;
    }

    // ================= DELETE =================
    public static void deleteById(int id) throws Exception {
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement("DELETE FROM citizens WHERE id = ?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    // ================= MAP =================
    private static Citizen map(ResultSet rs) throws SQLException {
        Citizen c = new Citizen();
        c.setId(rs.getInt("id"));
        c.setFullName(rs.getString("full_name"));
        c.setAlias(rs.getString("alias"));
        c.setGender(rs.getString("gender"));

        Date dob = rs.getDate("dob");
        c.setDob(dob == null ? null : dob.toLocalDate());

        c.setPlaceOfBirth(rs.getString("place_of_birth"));
        c.setHometown(rs.getString("hometown"));
        c.setEthnicity(rs.getString("ethnicity"));

        c.setCccd(rs.getString("cccd"));
        Date issueDate = rs.getDate("cccd_issue_date");
        c.setCccdIssueDate(issueDate == null ? null : issueDate.toLocalDate());
        c.setCccdIssuePlace(rs.getString("cccd_issue_place"));

        c.setJob(rs.getString("job"));
        c.setWorkplace(rs.getString("workplace"));

        c.setPreviousAddress(rs.getString("previous_address"));
        Date regDate = rs.getDate("register_date");
        c.setRegisterDate(regDate == null ? null : regDate.toLocalDate());

        c.setStatus(rs.getString("status"));
        c.setHouseholder(rs.getBoolean("is_householder"));
        c.setRelation(rs.getString("relation"));

        c.setHouseholdId((Integer) rs.getObject("household_id"));
        c.setUserId((Integer) rs.getObject("user_id"));

        return c;
    }

    // ================= HELPERS =================
    private static void setDate(PreparedStatement ps, int idx, LocalDate d) throws SQLException {
        if (d != null) ps.setDate(idx, Date.valueOf(d));
        else ps.setNull(idx, Types.DATE);
    }

    private static void setInt(PreparedStatement ps, int idx, Integer v) throws SQLException {
        if (v != null) ps.setInt(idx, v);
        else ps.setNull(idx, Types.INTEGER);
    }

    public static void removeCitizenFromHousehold(int citizenId) throws Exception {
        removeCitizenFromHousehold(citizenId, "MOVE_OUT");
    }

    public static void removeCitizenFromHousehold(int citizenId, String reason) throws Exception {
        try (Connection conn = Database.getConnection()) {
            conn.setAutoCommit(false);

            Integer oldHouseholdId = null;

            // 1. Lấy household hiện tại của công dân
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT household_id FROM citizens WHERE id=?"
            )) {
                ps.setInt(1, citizenId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        oldHouseholdId = (Integer) rs.getObject("household_id");
                    } else {
                        throw new SQLException("Công dân không tồn tại!");
                    }
                }
            }

            // 2. Cập nhật công dân: gỡ khỏi hộ khẩu
            try (PreparedStatement ps = conn.prepareStatement(
                    "UPDATE citizens SET household_id=NULL WHERE id=?"
            )) {
                ps.setInt(1, citizenId);
                ps.executeUpdate();
            }

            // 3. Ghi lại thay đổi vào citizen_changes, dùng cùng connection
            CitizenChange cc = new CitizenChange();
            cc.setCitizenId(citizenId);
            cc.setFromHouseholdId(oldHouseholdId); // null cũng ok nếu cột cho phép
            cc.setToHouseholdId(null);
            cc.setChangeType(reason); // DEAD / MOVE_OUT
            cc.setChangeDate(LocalDate.now());
            cc.setDestination(null);
            cc.setNote("Gỡ khỏi hộ khẩu");

            CitizenChangeDao.insert(conn, cc); // dùng cùng transaction

            conn.commit();
        } catch (SQLException e) {
            e.printStackTrace();
            throw e;
        }
    }

    // Lấy household_id lớn nhất hiện tại
    public static int getNextHouseholdId() throws SQLException {
        String sql = "SELECT MAX(household_id) FROM households";

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                int maxId = rs.getInt(1);
                return maxId + 1; // ID tiếp theo
            }
            return 1; // nếu bảng trống, bắt đầu từ 1
        }
    }


}
