/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fungsi;

import java.awt.Color;
import java.awt.Component;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import fungsi.koneksiDB;

/**
 *
 * @author Owner
 */
public class WarnaTableKasirRalan extends DefaultTableCellRenderer {

    private Connection koneksi = koneksiDB.condb();

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        Component component = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        boolean warnaKhusus = false;
        if (row % 2 == 1) {
            component.setBackground(new Color(255, 244, 244));
            component.setForeground(new Color(50, 50, 50));
        } else {
            component.setBackground(new Color(255, 255, 255));
            component.setForeground(new Color(50, 50, 50));
        }
//      
//        if (table.getValueAt(row, 20).toString().equals("Sudah SOAP")) {
//            component.setBackground(new Color(46, 139, 87));
//            component.setForeground(new Color(255, 230, 230));
//        } else if (table.getValueAt(row, 10).toString().equals("Batal")) {
//            component.setBackground(new Color(255, 51, 51));
//            component.setForeground(new Color(255, 255, 255));
//        } else if (table.getValueAt(row, 10).toString().equals("Dirujuk") || table.getValueAt(row, 10).toString().equals("Meninggal") || table.getValueAt(row, 10).toString().equals("Pulang Paksa")) {
//            component.setBackground(new Color(152, 152, 156));
//            component.setForeground(new Color(245, 245, 255));
//        } else if (table.getValueAt(row, 10).toString().equals("Dirawat")) {
//            component.setBackground(new Color(119, 221, 119));
//            component.setForeground(new Color(245, 255, 245));
//        }
//        if (table.getValueAt(row, 15).toString().equals("Sudah Bayar")) {
//            component.setBackground(new Color(50, 50, 50));
//            component.setForeground(new Color(255, 255, 255));
//        }
//        return component;
        try {
            // --- Atur warna sesuai kondisi khusus ---
            if (table.getValueAt(row, 20).toString().equals("Sudah SOAP")) {
                component.setBackground(new Color(46, 139, 87));
                component.setForeground(new Color(255, 230, 230));
                  warnaKhusus = true;
            } else if (table.getValueAt(row, 10).toString().equals("Batal")) {
                component.setBackground(new Color(255, 51, 51));
                component.setForeground(new Color(255, 255, 255));
                  warnaKhusus = true;
            } else if (table.getValueAt(row, 10).toString().equals("Dirujuk")
                    || table.getValueAt(row, 10).toString().equals("Meninggal")
                    || table.getValueAt(row, 10).toString().equals("Pulang Paksa")) {
                component.setBackground(new Color(152, 152, 156));
                component.setForeground(new Color(245, 245, 255));
                  warnaKhusus = true;
            } else if (table.getValueAt(row, 10).toString().equals("Dirawat")) {
                component.setBackground(new Color(119, 221, 119));
                component.setForeground(new Color(245, 255, 245));
                  warnaKhusus = true;
            }

            if (table.getValueAt(row, 15).toString().equals("Sudah Bayar")) {
                component.setBackground(new Color(50, 50, 50));
                component.setForeground(new Color(255, 255, 255));
                  warnaKhusus = true;
            }

            // --- Cek catatan pasien (SELALU PRIORITAS) ---
            String noRM = table.getValueAt(row, 2).toString();

            String sql = "SELECT COUNT(*) "
                    + "FROM reg_periksa r "
                    + "INNER JOIN catatan_pasien c ON r.no_rkm_medis = c.no_rkm_medis "
                    + "WHERE r.no_rkm_medis = ?";
            PreparedStatement ps = koneksi.prepareStatement(sql);
            ps.setString(1, noRM);
            ResultSet rs = ps.executeQuery();

            if (rs.next() && rs.getInt(1) > 0) {
                // Jika ada catatan → override jadi ungu
                component.setBackground(new Color(128, 0, 128));
                component.setForeground(new Color(255, 255, 255));
                  warnaKhusus = true;
            }

            rs.close();
            ps.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
        
         if (isSelected && !warnaKhusus) {
        component.setForeground(Color.RED);
    }

        return component;
    }

}
