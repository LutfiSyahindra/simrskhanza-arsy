package fungsi;

import java.awt.Color;
import java.awt.Component;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 *
 * @author Owner
 */
public class WarnaTableIgd extends DefaultTableCellRenderer {

    private final Connection koneksi = koneksiDB.condb();

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
            boolean isSelected, boolean hasFocus, int row, int column) {

        Component component = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        boolean warnaKhusus = false;
       

        // --- Warna dasar selang-seling ---
        if (row % 2 == 1) {
            component.setBackground(new Color(255, 244, 244));
            component.setForeground(new Color(50, 50, 50));
        } else {
            component.setBackground(new Color(255, 255, 255));
            component.setForeground(new Color(50, 50, 50));
        }

        try {
            // --- Ambil no_rawat dari tabel ---
            String noRawat = table.getValueAt(row, 2).toString();
            String noRM = null;
            
               System.out.println("[DEBUG] Mengecek data untuk no_rawat: " + noRawat);

            // --- Cari no_rkm_medis berdasarkan no_rawat ---
            String sqlCariRM = "SELECT no_rkm_medis FROM reg_periksa WHERE no_rawat = ?";
            try (PreparedStatement psCariRM = koneksi.prepareStatement(sqlCariRM)) {
                psCariRM.setString(1, noRawat);
                try (ResultSet rsCariRM = psCariRM.executeQuery()) {
                    if (rsCariRM.next()) {
                        noRM = rsCariRM.getString("no_rkm_medis");
                    }
                }
            }

            // --- Jika noRM ditemukan, cek apakah ada catatan pasien ---
            if (noRM != null && !noRM.isEmpty()) {
                String sql = "SELECT COUNT(*) AS jumlah "
                        + "FROM catatan_pasien c "
                        + "WHERE c.no_rkm_medis = ?";
                try (PreparedStatement ps = koneksi.prepareStatement(sql)) {
                    ps.setString(1, noRM);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            int jumlahCatatan = rs.getInt("jumlah");

                            // Jika ada catatan → beri warna ungu
                            if (jumlahCatatan > 0) {
                                component.setBackground(new Color(128, 0, 128));
                                component.setForeground(Color.WHITE);
                                 warnaKhusus = true;
                            }
                        }
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        
        if (isSelected && !warnaKhusus) {
        component.setForeground(Color.RED);
    }

        return component;
    }
}
