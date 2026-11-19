/*
  by Mas Elkhanza
 */
package bridging;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import fungsi.WarnaTable;
import fungsi.batasInput;
import fungsi.koneksiDB;
import java.awt.Dimension;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import fungsi.sekuel;
import fungsi.validasi;
import fungsi.akses;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.event.KeyEvent;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.zip.GZIPOutputStream;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.text.Document;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;

/**
 *
 * @author dosen
 */
public final class BpjsKirimEncounter extends javax.swing.JDialog {

    private final DefaultTableModel tabMode;
    private sekuel Sequel = new sekuel();
    private validasi Valid = new validasi();
    private Connection koneksi = koneksiDB.condb();
    private PreparedStatement ps;
    private ResultSet rs;
    private int i = 0;
    private String link = "", json = "", iddokter = "", idpasien = "", idepisode = "";
//    private ApiSatuSehat api=new ApiSatuSehat();
    private ApiBpjsEmr api = new ApiBpjsEmr();
    private HttpHeaders headers;
    private HttpEntity requestEntity;
    private ObjectMapper mapper = new ObjectMapper();
    private JsonNode root;
    private JsonNode response;
    private SatuSehatCekNIK cekViaSatuSehat = new SatuSehatCekNIK();
    private StringBuilder htmlContent;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private volatile boolean ceksukses = false;

    /**
     * Creates new form DlgKamar
     *
     * @param parent
     * @param modal
     */
    public BpjsKirimEncounter(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();

        this.setLocation(10, 2);
        setSize(628, 674);

        tabMode = new DefaultTableModel(null, new String[]{
            "P", "Tanggal Registrasi", "No.Rawat", "No.RM", "Nama Pasien", "No.Esp", "Kode Dokter", "Nama Dokter",
            "Tgl Masuk", "Tgl Keluar", "Kode Poli", "Nama Poli/Unit", "Diagnosa", "Stts Rawat", "Stts Lanjut",
            "Mulai Periksa", "ID Encounter"
        }) {
            @Override
            public boolean isCellEditable(int rowIndex, int colIndex) {
                boolean a = false;
                if (colIndex == 0) {
                    a = true;
                }
                return a;
            }
            Class[] types = new Class[]{
                java.lang.Boolean.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class,
                java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class,
                java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class,
                java.lang.String.class, java.lang.String.class
            };

            @Override
            public Class getColumnClass(int columnIndex) {
                return types[columnIndex];
            }
        };
        tbObat.setModel(tabMode);

        //tbKamar.setDefaultRenderer(Object.class, new WarnaTable(panelJudul.getBackground(),tbKamar.getBackground()));
        tbObat.setPreferredScrollableViewportSize(new Dimension(500, 500));
        tbObat.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        for (i = 0; i < 16; i++) {
            TableColumn column = tbObat.getColumnModel().getColumn(i);
            if (i == 0) {
                column.setPreferredWidth(20);
            } else if (i == 1) {
                column.setPreferredWidth(150);
            } else if (i == 2) {
                column.setPreferredWidth(105);
            } else if (i == 3) {
                column.setPreferredWidth(70);
            } else if (i == 4) {
                column.setPreferredWidth(150);
            } else if (i == 5) {
                column.setPreferredWidth(110);
            } else if (i == 6) {
                column.setPreferredWidth(80);
            } else if (i == 7) {
                column.setPreferredWidth(150);
            } else if (i == 8) {
                column.setPreferredWidth(110);
            } else if (i == 9) {
                column.setPreferredWidth(80);
            } else if (i == 10) {
                column.setPreferredWidth(140);
            } else if (i == 11) {
                column.setPreferredWidth(210);
            } else if (i == 12) {
                column.setPreferredWidth(63);
            } else if (i == 13) {
                column.setPreferredWidth(63);
            } else if (i == 14) {
                column.setPreferredWidth(150);
            } else if (i == 15) {
                column.setPreferredWidth(215);
            } else if (i == 16) {
                column.setPreferredWidth(200);
            }
        }
        tbObat.setDefaultRenderer(Object.class, new WarnaTable());

        TCari.setDocument(new batasInput((byte) 100).getKata(TCari));

        try {
            link = koneksiDB.URLFHIRSATUSEHAT();
        } catch (Exception e) {
            System.out.println("Notif : " + e);
        }

        HTMLEditorKit kit = new HTMLEditorKit();
        LoadHTML.setEditable(true);
        LoadHTML.setEditorKit(kit);
        StyleSheet styleSheet = kit.getStyleSheet();
        styleSheet.addRule(
                ".isi td{border-right: 1px solid #e2e7dd;font: 8.5px tahoma;height:12px;border-bottom: 1px solid #e2e7dd;background: #ffffff;color:#323232;}"
                + ".isi2 td{font: 8.5px tahoma;border:none;height:12px;background: #ffffff;color:#323232;}"
                + ".isi3 td{border-right: 1px solid #e2e7dd;font: 8.5px tahoma;height:12px;border-top: 1px solid #e2e7dd;background: #ffffff;color:#323232;}"
                + ".isi4 td{font: 11px tahoma;height:12px;border-top: 1px solid #e2e7dd;background: #ffffff;color:#323232;}"
                + ".isi5 td{font: 8.5px tahoma;border:none;height:12px;background: #ffffff;color:#AA0000;}"
                + ".isi6 td{font: 8.5px tahoma;border:none;height:12px;background: #ffffff;color:#FF0000;}"
                + ".isi7 td{font: 8.5px tahoma;border:none;height:12px;background: #ffffff;color:#C8C800;}"
                + ".isi8 td{font: 8.5px tahoma;border:none;height:12px;background: #ffffff;color:#00AA00;}"
                + ".isi9 td{font: 8.5px tahoma;border:none;height:12px;background: #ffffff;color:#969696;}"
        );
        Document doc = kit.createDefaultDocument();
        LoadHTML.setDocument(doc);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPopupMenu1 = new javax.swing.JPopupMenu();
        ppPilihSemua = new javax.swing.JMenuItem();
        ppPilihBelumTerkirim = new javax.swing.JMenuItem();
        ppBersihkan = new javax.swing.JMenuItem();
        LoadHTML = new widget.editorpane();
        internalFrame1 = new widget.InternalFrame();
        Scroll = new widget.ScrollPane();
        tbObat = new widget.Table();
        jPanel3 = new javax.swing.JPanel();
        panelGlass8 = new widget.panelisi();
        jLabel7 = new widget.Label();
        LCount = new widget.Label();
        BtnAll = new widget.Button();
        BtnKirim = new widget.Button();
        BtnUpdate = new widget.Button();
        BtnPrint = new widget.Button();
        BtnKeluar = new widget.Button();
        panelGlass9 = new widget.panelisi();
        jLabel15 = new widget.Label();
        DTPCari1 = new widget.Tanggal();
        jLabel17 = new widget.Label();
        DTPCari2 = new widget.Tanggal();
        jLabel16 = new widget.Label();
        TCari = new widget.TextBox();
        BtnCari = new widget.Button();

        jPopupMenu1.setName("jPopupMenu1"); // NOI18N

        ppPilihSemua.setBackground(new java.awt.Color(255, 255, 254));
        ppPilihSemua.setFont(new java.awt.Font("Tahoma", 0, 11)); // NOI18N
        ppPilihSemua.setForeground(new java.awt.Color(50, 50, 50));
        ppPilihSemua.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/category.png"))); // NOI18N
        ppPilihSemua.setText("Pilih Semua");
        ppPilihSemua.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        ppPilihSemua.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        ppPilihSemua.setName("ppPilihSemua"); // NOI18N
        ppPilihSemua.setPreferredSize(new java.awt.Dimension(150, 26));
        ppPilihSemua.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ppPilihSemuaActionPerformed(evt);
            }
        });
        jPopupMenu1.add(ppPilihSemua);

        ppPilihBelumTerkirim.setBackground(new java.awt.Color(255, 255, 254));
        ppPilihBelumTerkirim.setFont(new java.awt.Font("Tahoma", 0, 11)); // NOI18N
        ppPilihBelumTerkirim.setForeground(new java.awt.Color(50, 50, 50));
        ppPilihBelumTerkirim.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/category.png"))); // NOI18N
        ppPilihBelumTerkirim.setText("Pilih Belum Terkirim");
        ppPilihBelumTerkirim.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        ppPilihBelumTerkirim.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        ppPilihBelumTerkirim.setName("ppPilihBelumTerkirim"); // NOI18N
        ppPilihBelumTerkirim.setPreferredSize(new java.awt.Dimension(150, 26));
        ppPilihBelumTerkirim.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ppPilihBelumTerkirimActionPerformed(evt);
            }
        });
        jPopupMenu1.add(ppPilihBelumTerkirim);

        ppBersihkan.setBackground(new java.awt.Color(255, 255, 254));
        ppBersihkan.setFont(new java.awt.Font("Tahoma", 0, 11)); // NOI18N
        ppBersihkan.setForeground(new java.awt.Color(50, 50, 50));
        ppBersihkan.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/category.png"))); // NOI18N
        ppBersihkan.setText("Hilangkan Pilihan");
        ppBersihkan.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        ppBersihkan.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        ppBersihkan.setName("ppBersihkan"); // NOI18N
        ppBersihkan.setPreferredSize(new java.awt.Dimension(150, 26));
        ppBersihkan.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ppBersihkanActionPerformed(evt);
            }
        });
        jPopupMenu1.add(ppBersihkan);

        LoadHTML.setBorder(null);
        LoadHTML.setName("LoadHTML"); // NOI18N

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setIconImage(null);
        setIconImages(null);
        setUndecorated(true);
        setResizable(false);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowOpened(java.awt.event.WindowEvent evt) {
                formWindowOpened(evt);
            }
        });

        internalFrame1.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(240, 245, 235)), "::[ Pengiriman Data Encounter BPJS ]::", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 0, 11), new java.awt.Color(50, 50, 50))); // NOI18N
        internalFrame1.setFont(new java.awt.Font("Tahoma", 0, 11)); // NOI18N
        internalFrame1.setName("internalFrame1"); // NOI18N
        internalFrame1.setLayout(new java.awt.BorderLayout(1, 1));

        Scroll.setComponentPopupMenu(jPopupMenu1);
        Scroll.setName("Scroll"); // NOI18N
        Scroll.setOpaque(true);

        tbObat.setComponentPopupMenu(jPopupMenu1);
        tbObat.setName("tbObat"); // NOI18N
        Scroll.setViewportView(tbObat);

        internalFrame1.add(Scroll, java.awt.BorderLayout.CENTER);
        Scroll.getAccessibleContext().setAccessibleName("");

        jPanel3.setName("jPanel3"); // NOI18N
        jPanel3.setOpaque(false);
        jPanel3.setPreferredSize(new java.awt.Dimension(44, 100));
        jPanel3.setLayout(new java.awt.BorderLayout(1, 1));

        panelGlass8.setName("panelGlass8"); // NOI18N
        panelGlass8.setPreferredSize(new java.awt.Dimension(44, 44));
        panelGlass8.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 5, 9));

        jLabel7.setText("Record :");
        jLabel7.setName("jLabel7"); // NOI18N
        jLabel7.setPreferredSize(new java.awt.Dimension(53, 23));
        panelGlass8.add(jLabel7);

        LCount.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        LCount.setText("0");
        LCount.setName("LCount"); // NOI18N
        LCount.setPreferredSize(new java.awt.Dimension(60, 23));
        panelGlass8.add(LCount);

        BtnAll.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/Search-16x16.png"))); // NOI18N
        BtnAll.setMnemonic('M');
        BtnAll.setText("Semua");
        BtnAll.setToolTipText("Alt+M");
        BtnAll.setName("BtnAll"); // NOI18N
        BtnAll.setPreferredSize(new java.awt.Dimension(100, 30));
        BtnAll.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnAllActionPerformed(evt);
            }
        });
        BtnAll.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                BtnAllKeyPressed(evt);
            }
        });
        panelGlass8.add(BtnAll);

        BtnKirim.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/34.png"))); // NOI18N
        BtnKirim.setMnemonic('K');
        BtnKirim.setText("Kirim");
        BtnKirim.setToolTipText("Alt+K");
        BtnKirim.setName("BtnKirim"); // NOI18N
        BtnKirim.setPreferredSize(new java.awt.Dimension(100, 30));
        BtnKirim.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnKirimActionPerformed(evt);
            }
        });
        panelGlass8.add(BtnKirim);

        BtnUpdate.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/edit_f2.png"))); // NOI18N
        BtnUpdate.setMnemonic('U');
        BtnUpdate.setText("Update");
        BtnUpdate.setToolTipText("Alt+U");
        BtnUpdate.setName("BtnUpdate"); // NOI18N
        BtnUpdate.setPreferredSize(new java.awt.Dimension(100, 30));
        BtnUpdate.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnUpdateActionPerformed(evt);
            }
        });
        panelGlass8.add(BtnUpdate);

        BtnPrint.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/b_print.png"))); // NOI18N
        BtnPrint.setMnemonic('T');
        BtnPrint.setText("Cetak");
        BtnPrint.setToolTipText("Alt+T");
        BtnPrint.setName("BtnPrint"); // NOI18N
        BtnPrint.setPreferredSize(new java.awt.Dimension(100, 30));
        BtnPrint.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnPrintActionPerformed(evt);
            }
        });
        panelGlass8.add(BtnPrint);

        BtnKeluar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/exit.png"))); // NOI18N
        BtnKeluar.setMnemonic('K');
        BtnKeluar.setText("Keluar");
        BtnKeluar.setToolTipText("Alt+K");
        BtnKeluar.setName("BtnKeluar"); // NOI18N
        BtnKeluar.setPreferredSize(new java.awt.Dimension(100, 30));
        BtnKeluar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnKeluarActionPerformed(evt);
            }
        });
        BtnKeluar.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                BtnKeluarKeyPressed(evt);
            }
        });
        panelGlass8.add(BtnKeluar);

        jPanel3.add(panelGlass8, java.awt.BorderLayout.CENTER);

        panelGlass9.setName("panelGlass9"); // NOI18N
        panelGlass9.setPreferredSize(new java.awt.Dimension(44, 44));
        panelGlass9.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 5, 9));

        jLabel15.setText("Tgl.Registrasi :");
        jLabel15.setName("jLabel15"); // NOI18N
        jLabel15.setPreferredSize(new java.awt.Dimension(85, 23));
        panelGlass9.add(jLabel15);

        DTPCari1.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "28-04-2026" }));
        DTPCari1.setDisplayFormat("dd-MM-yyyy");
        DTPCari1.setName("DTPCari1"); // NOI18N
        DTPCari1.setOpaque(false);
        DTPCari1.setPreferredSize(new java.awt.Dimension(95, 23));
        panelGlass9.add(DTPCari1);

        jLabel17.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel17.setText("s.d.");
        jLabel17.setName("jLabel17"); // NOI18N
        jLabel17.setPreferredSize(new java.awt.Dimension(24, 23));
        panelGlass9.add(jLabel17);

        DTPCari2.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "28-04-2026" }));
        DTPCari2.setDisplayFormat("dd-MM-yyyy");
        DTPCari2.setName("DTPCari2"); // NOI18N
        DTPCari2.setOpaque(false);
        DTPCari2.setPreferredSize(new java.awt.Dimension(95, 23));
        panelGlass9.add(DTPCari2);

        jLabel16.setText("Key Word :");
        jLabel16.setName("jLabel16"); // NOI18N
        jLabel16.setPreferredSize(new java.awt.Dimension(70, 23));
        panelGlass9.add(jLabel16);

        TCari.setName("TCari"); // NOI18N
        TCari.setPreferredSize(new java.awt.Dimension(210, 23));
        TCari.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                TCariKeyPressed(evt);
            }
        });
        panelGlass9.add(TCari);

        BtnCari.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/accept.png"))); // NOI18N
        BtnCari.setMnemonic('6');
        BtnCari.setToolTipText("Alt+6");
        BtnCari.setName("BtnCari"); // NOI18N
        BtnCari.setPreferredSize(new java.awt.Dimension(28, 23));
        BtnCari.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnCariActionPerformed(evt);
            }
        });
        BtnCari.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                BtnCariKeyPressed(evt);
            }
        });
        panelGlass9.add(BtnCari);

        jPanel3.add(panelGlass9, java.awt.BorderLayout.PAGE_START);

        internalFrame1.add(jPanel3, java.awt.BorderLayout.PAGE_END);

        getContentPane().add(internalFrame1, java.awt.BorderLayout.CENTER);
        internalFrame1.getAccessibleContext().setAccessibleName("::[ Pengiriman Data Encounter BPJS ]::");
        internalFrame1.getAccessibleContext().setAccessibleDescription("");

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void BtnKeluarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnKeluarActionPerformed
        dispose();
    }//GEN-LAST:event_BtnKeluarActionPerformed

    private void BtnKeluarKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnKeluarKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_SPACE) {
            dispose();
        } else {
            Valid.pindah(evt, BtnPrint, BtnKeluar);
        }
    }//GEN-LAST:event_BtnKeluarKeyPressed

    private void BtnPrintActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnPrintActionPerformed
        this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        try {
            htmlContent = new StringBuilder();
            htmlContent.append(
                    "<tr class='isi'>"
                    + "<td valign='middle' bgcolor='#FFFAFA' align='center'><b>Tanggal Registrasi</b></td>"
                    + "<td valign='middle' bgcolor='#FFFAFA' align='center'><b>No.Rawat</b></td>"
                    + "<td valign='middle' bgcolor='#FFFAFA' align='center'><b>No.RM</b></td>"
                    + "<td valign='middle' bgcolor='#FFFAFA' align='center'><b>Nama Pasien</b></td>"
                    + "<td valign='middle' bgcolor='#FFFAFA' align='center'><b>No.KTP Pasien</b></td>"
                    + "<td valign='middle' bgcolor='#FFFAFA' align='center'><b>Kode Dokter</b></td>"
                    + "<td valign='middle' bgcolor='#FFFAFA' align='center'><b>Nama Dokter</b></td>"
                    + "<td valign='middle' bgcolor='#FFFAFA' align='center'><b>No.KTP Dokter</b></td>"
                    + "<td valign='middle' bgcolor='#FFFAFA' align='center'><b>Kode Poli</b></td>"
                    + "<td valign='middle' bgcolor='#FFFAFA' align='center'><b>Nama Poli/Unit</b></td>"
                    + "<td valign='middle' bgcolor='#FFFAFA' align='center'><b>ID Lokasi Unit</b></td>"
                    + "<td valign='middle' bgcolor='#FFFAFA' align='center'><b>Stts Rawat</b></td>"
                    + "<td valign='middle' bgcolor='#FFFAFA' align='center'><b>Stts Lanjut</b></td>"
                    + "<td valign='middle' bgcolor='#FFFAFA' align='center'><b>Tanggal Pulang</b></td>"
                    + "<td valign='middle' bgcolor='#FFFAFA' align='center'><b>ID Encounter</b></td>"
                    + "</tr>"
            );
            for (i = 0; i < tabMode.getRowCount(); i++) {
                htmlContent.append(
                        "<tr class='isi'>"
                        + "<td valign='top'>" + tbObat.getValueAt(i, 1).toString() + "</td>"
                        + "<td valign='top'>" + tbObat.getValueAt(i, 2).toString() + "</td>"
                        + "<td valign='top'>" + tbObat.getValueAt(i, 3).toString() + "</td>"
                        + "<td valign='top'>" + tbObat.getValueAt(i, 4).toString() + "</td>"
                        + "<td valign='top'>" + tbObat.getValueAt(i, 5).toString() + "</td>"
                        + "<td valign='top'>" + tbObat.getValueAt(i, 6).toString() + "</td>"
                        + "<td valign='top'>" + tbObat.getValueAt(i, 7).toString() + "</td>"
                        + "<td valign='top'>" + tbObat.getValueAt(i, 8).toString() + "</td>"
                        + "<td valign='top'>" + tbObat.getValueAt(i, 9).toString() + "</td>"
                        + "<td valign='top'>" + tbObat.getValueAt(i, 10).toString() + "</td>"
                        + "<td valign='top'>" + tbObat.getValueAt(i, 11).toString() + "</td>"
                        + "<td valign='top'>" + tbObat.getValueAt(i, 12).toString() + "</td>"
                        + "<td valign='top'>" + tbObat.getValueAt(i, 13).toString() + "</td>"
                        + "<td valign='top'>" + tbObat.getValueAt(i, 14).toString() + "</td>"
                        + "<td valign='top'>" + tbObat.getValueAt(i, 15).toString() + "</td>"
                        + "</tr>");
            }
            LoadHTML.setText(
                    "<html>"
                    + "<table width='100%' border='0' align='center' cellpadding='1px' cellspacing='0' class='tbl_form'>"
                    + htmlContent.toString()
                    + "</table>"
                    + "</html>"
            );
            htmlContent = null;

            File g = new File("file2.css");
            BufferedWriter bg = new BufferedWriter(new FileWriter(g));
            bg.write(
                    ".isi td{border-right: 1px solid #e2e7dd;font: 8.5px tahoma;height:12px;border-bottom: 1px solid #e2e7dd;background: #ffffff;color:#323232;}"
                    + ".isi2 td{font: 8.5px tahoma;border:none;height:12px;background: #ffffff;color:#323232;}"
                    + ".isi3 td{border-right: 1px solid #e2e7dd;font: 8.5px tahoma;height:12px;border-top: 1px solid #e2e7dd;background: #ffffff;color:#323232;}"
                    + ".isi4 td{font: 11px tahoma;height:12px;border-top: 1px solid #e2e7dd;background: #ffffff;color:#323232;}"
                    + ".isi5 td{font: 8.5px tahoma;border:none;height:12px;background: #ffffff;color:#AA0000;}"
                    + ".isi6 td{font: 8.5px tahoma;border:none;height:12px;background: #ffffff;color:#FF0000;}"
                    + ".isi7 td{font: 8.5px tahoma;border:none;height:12px;background: #ffffff;color:#C8C800;}"
                    + ".isi8 td{font: 8.5px tahoma;border:none;height:12px;background: #ffffff;color:#00AA00;}"
                    + ".isi9 td{font: 8.5px tahoma;border:none;height:12px;background: #ffffff;color:#969696;}"
            );
            bg.close();

            File f = new File("DataSatuSehatEncounter.html");
            BufferedWriter bw = new BufferedWriter(new FileWriter(f));
            bw.write(LoadHTML.getText().replaceAll("<head>", "<head>"
                    + "<link href=\"file2.css\" rel=\"stylesheet\" type=\"text/css\" />"
                    + "<table width='100%' border='0' align='center' cellpadding='3px' cellspacing='0' class='tbl_form'>"
                    + "<tr class='isi2'>"
                    + "<td valign='top' align='center'>"
                    + "<font size='4' face='Tahoma'>" + akses.getnamars() + "</font><br>"
                    + akses.getalamatrs() + ", " + akses.getkabupatenrs() + ", " + akses.getpropinsirs() + "<br>"
                    + akses.getkontakrs() + ", E-mail : " + akses.getemailrs() + "<br><br>"
                    + "<font size='2' face='Tahoma'>DATA PENGIRIMAN SATU SEHAT ENCOUNTER<br><br></font>"
                    + "</td>"
                    + "</tr>"
                    + "</table>")
            );
            bw.close();
            Desktop.getDesktop().browse(f.toURI());
        } catch (Exception e) {
            System.out.println("Notifikasi : " + e);
        }
        this.setCursor(Cursor.getDefaultCursor());
    }//GEN-LAST:event_BtnPrintActionPerformed

    private void TCariKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_TCariKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            BtnCariActionPerformed(null);
        } else if (evt.getKeyCode() == KeyEvent.VK_PAGE_DOWN) {
            BtnCariActionPerformed(null);
        } else if (evt.getKeyCode() == KeyEvent.VK_PAGE_UP) {
            BtnKeluar.requestFocus();
        } else if (evt.getKeyCode() == KeyEvent.VK_UP) {
            tbObat.requestFocus();
        }
    }//GEN-LAST:event_TCariKeyPressed

    private void BtnCariActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnCariActionPerformed
        this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        runBackground(() -> tampil());
        this.setCursor(Cursor.getDefaultCursor());
    }//GEN-LAST:event_BtnCariActionPerformed

    private void BtnCariKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnCariKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_SPACE) {
            BtnCariActionPerformed(null);
        } else {
            Valid.pindah(evt, TCari, BtnPrint);
        }
    }//GEN-LAST:event_BtnCariKeyPressed

    private String safeTime(String datetime) {
        try {
            if (datetime == null || !datetime.contains("T")) {
                return datetime;
            }

            String[] parts = datetime.split("T");
            String date = parts[0];
            String time = parts[1];

            String jam = time.substring(0, 2);
            int j = Integer.parseInt(jam);

            if (j >= 24) {
                j = j % 24;
                time = String.format("%02d", j) + time.substring(2);
            }

            return date + "T" + time;
        } catch (Exception e) {
            return datetime;
        }
    }

    public static byte[] kompresGzip(String data) throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        GZIPOutputStream gzip = new GZIPOutputStream(bos);
        gzip.write(data.getBytes("UTF-8"));
        gzip.close();
        return bos.toByteArray();
    }

    public static String gzipBase64(String data) throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        GZIPOutputStream gzip = new GZIPOutputStream(bos);

        gzip.write(data.getBytes("UTF-8"));
        gzip.close();

        return Base64.getEncoder().encodeToString(bos.toByteArray());
    }

    private boolean validasiNoSEP(String noSEP) {

        try {

            // ================= NO SEP KOSONG =================
            if (noSEP == null || noSEP.trim().isEmpty()) {

                JOptionPane.showMessageDialog(
                        null,
                        "Validasi gagal : Nosep kosong"
                );

                return false;
            }

            // ================= NO SEP > 19 =================
            if (noSEP.length() > 19) {

                JOptionPane.showMessageDialog(
                        null,
                        "Validasi gagal : Nosep lebih dari 19 digit"
                );

                return false;
            }

            // ================= CEK DATA KLAIM =================
            String sqlSEP
                    = "SELECT no_sep "
                    + "FROM bridging_sep "
                    + "WHERE no_sep=? "
                    + "UNION "
                    + "SELECT no_sep "
                    + "FROM bridging_sep_internal "
                    + "WHERE no_sep=? "
                    + "LIMIT 1";

            PreparedStatement psSEP
                    = koneksi.prepareStatement(sqlSEP);

            psSEP.setString(1, noSEP);
            psSEP.setString(2, noSEP);

            ResultSet rsSEP
                    = psSEP.executeQuery();

            boolean ditemukan = rsSEP.next();

            rsSEP.close();
            psSEP.close();

            if (!ditemukan) {

                JOptionPane.showMessageDialog(
                        null,
                        "Validasi gagal : Nosep tidak sesuai data klaim"
                );

                return false;
            }

            return true;

        } catch (Exception e) {

            JOptionPane.showMessageDialog(
                    null,
                    "Error validasi SEP : " + e
            );

            return false;
        }
    }

    private boolean validasiJenisKunjungan(String jnsPelayanan) {

        // ================= NORMALISASI =================
        jnsPelayanan = jnsPelayanan == null
                ? ""
                : jnsPelayanan.trim();

        // ================= VALIDASI KOSONG =================
        if (jnsPelayanan.isEmpty()) {

            JOptionPane.showMessageDialog(
                    null,
                    "Validasi gagal : Jenis Kunjungan kosong"
            );

            return false;
        }

        // ================= VALIDASI HARUS 1 ATAU 2 =================
        if (!jnsPelayanan.equals("1")
                && !jnsPelayanan.equals("2")) {

            JOptionPane.showMessageDialog(
                    null,
                    "Validasi gagal : Jenis Kunjungan harus 1 atau 2"
            );

            return false;
        }

        return true;
    }

    private boolean validasiJenisKunjunganKlaim(
            String noSEP,
            String noRawat,
            String jnsPelayanan
    ) {

        try {

            noSEP = noSEP == null ? "" : noSEP.trim();

            noRawat = noRawat == null
                    ? ""
                    : noRawat.trim();

            jnsPelayanan = jnsPelayanan == null
                    ? ""
                    : jnsPelayanan.trim();

            String sql
                    = "SELECT jnspelayanan "
                    + "FROM bridging_sep "
                    + "WHERE no_sep=? "
                    + "AND no_rawat=? "
                    + "UNION "
                    + "SELECT jnspelayanan "
                    + "FROM bridging_sep_internal "
                    + "WHERE no_sep=? "
                    + "AND no_rawat=? "
                    + "LIMIT 1";

            PreparedStatement ps
                    = koneksi.prepareStatement(sql);

            ps.setString(1, noSEP);
            ps.setString(2, noRawat);

            ps.setString(3, noSEP);
            ps.setString(4, noRawat);

            ResultSet rs = ps.executeQuery();

            if (!rs.next()) {

                rs.close();
                ps.close();

                JOptionPane.showMessageDialog(
                        null,
                        "Validasi gagal : Data klaim tidak ditemukan"
                );

                return false;
            }

            String jnsPelayananKlaim
                    = rs.getString("jnspelayanan") == null
                    ? ""
                    : rs.getString("jnspelayanan").trim();

            rs.close();
            ps.close();

            System.out.println(
                    "JNS APP   : [" + jnsPelayanan + "]"
            );

            System.out.println(
                    "JNS KLAIM : [" + jnsPelayananKlaim + "]"
            );

            if (!jnsPelayanan.equals(jnsPelayananKlaim)) {

                JOptionPane.showMessageDialog(
                        null,
                        "Validasi gagal : Jenis Kunjungan tidak sesuai dengan data klaim"
                        + "\nAPP   : " + jnsPelayanan
                        + "\nKLAIM : " + jnsPelayananKlaim
                );

                return false;
            }

            return true;

        } catch (Exception e) {

            System.out.println(
                    "Error validasi jenis kunjungan : " + e
            );

            return false;
        }
    }

    private boolean validasiBulan(String bulan) {

        // ================= NORMALISASI =================
        bulan = bulan == null
                ? ""
                : bulan.trim();

        // ================= VALIDASI KOSONG =================
        if (bulan.isEmpty()) {

            JOptionPane.showMessageDialog(
                    null,
                    "Validasi gagal : Bulan kosong"
            );

            return false;
        }

        try {

            int bln = Integer.parseInt(bulan);

            // ================= VALIDASI 1-12 =================
            if (bln < 1 || bln > 12) {

                JOptionPane.showMessageDialog(
                        null,
                        "Validasi gagal : Bulan harus 1-12"
                );

                return false;
            }

        } catch (Exception e) {

            JOptionPane.showMessageDialog(
                    null,
                    "Validasi gagal : Format bulan tidak valid"
            );

            return false;
        }

        return true;
    }

    private boolean validasiBulanKlaim(
            String noSEP,
            String bulan
    ) {

        try {

            // ================= NORMALISASI =================
            noSEP = noSEP == null ? "" : noSEP.trim();

            bulan = bulan == null
                    ? ""
                    : bulan.trim();

            // ================= QUERY =================
            String sql
                    = "SELECT tglsep "
                    + "FROM bridging_sep "
                    + "WHERE no_sep=? "
                    + "UNION "
                    + "SELECT tglsep "
                    + "FROM bridging_sep_internal "
                    + "WHERE no_sep=? "
                    + "LIMIT 1";

            PreparedStatement ps
                    = koneksi.prepareStatement(sql);

            ps.setString(1, noSEP);
            ps.setString(2, noSEP);

            ResultSet rs = ps.executeQuery();

            // ================= DATA TIDAK ADA =================
            if (!rs.next()) {

                rs.close();
                ps.close();

                JOptionPane.showMessageDialog(
                        null,
                        "Validasi gagal : Data klaim tidak ditemukan"
                );

                return false;
            }

            // ================= AMBIL BULAN KLAIM =================
            String tglSEP = rs.getString("tglsep");

            rs.close();
            ps.close();

            // contoh : 2026-05-18
            String bulanKlaim = tglSEP.substring(5, 7);

            // ================= VALIDASI =================
            if (!bulan.equals(bulanKlaim)) {

                JOptionPane.showMessageDialog(
                        null,
                        "Validasi gagal : Bulan tidak sesuai dengan data klaim"
                );

                return false;
            }

            return true;

        } catch (Exception e) {

            System.out.println(
                    "Error validasi bulan klaim : " + e
            );

            return false;
        }
    }

    private boolean validasiTahun(
            String noSEP,
            String tahun
    ) {

        try {

            // ================= NORMALISASI =================
            noSEP = noSEP == null
                    ? ""
                    : noSEP.trim();

            tahun = tahun == null
                    ? ""
                    : tahun.trim();

            // ================= TAHUN KOSONG =================
            if (tahun.isEmpty()) {

                JOptionPane.showMessageDialog(
                        null,
                        "Validasi gagal : Tahun kosong"
                );

                return false;
            }

            // ================= TAHUN > 5 DIGIT =================
            if (tahun.length() > 5) {

                JOptionPane.showMessageDialog(
                        null,
                        "Validasi gagal : Tahun lebih dari 5 digit"
                );

                return false;
            }

            // ================= FORMAT ANGKA =================
            if (!tahun.matches("\\d+")) {

                JOptionPane.showMessageDialog(
                        null,
                        "Validasi gagal : Format tahun tidak valid"
                );

                return false;
            }

            // ================= QUERY DATA KLAIM =================
            String sql
                    = "SELECT tglsep "
                    + "FROM bridging_sep "
                    + "WHERE no_sep=? "
                    + "UNION "
                    + "SELECT tglsep "
                    + "FROM bridging_sep_internal "
                    + "WHERE no_sep=? "
                    + "LIMIT 1";

            PreparedStatement ps
                    = koneksi.prepareStatement(sql);

            ps.setString(1, noSEP);
            ps.setString(2, noSEP);

            ResultSet rs = ps.executeQuery();

            // ================= DATA KLAIM TIDAK ADA =================
            if (!rs.next()) {

                rs.close();
                ps.close();

                JOptionPane.showMessageDialog(
                        null,
                        "Validasi gagal : Data klaim tidak ditemukan"
                );

                return false;
            }

            // ================= AMBIL TAHUN KLAIM =================
            String tglSEP = rs.getString("tglsep");

            rs.close();
            ps.close();

            // contoh : 2026-05-18
            String tahunKlaim = tglSEP.substring(0, 4);

            // ================= VALIDASI TAHUN KLAIM =================
            if (!tahun.equals(tahunKlaim)) {

                JOptionPane.showMessageDialog(
                        null,
                        "Validasi gagal : Tahun tidak sesuai dengan data klaim"
                );

                return false;
            }

            return true;

        } catch (Exception e) {

            System.out.println(
                    "Error validasi tahun : " + e
            );

            return false;
        }
    }

    private boolean validasiDataMR(String encrypted) {

        try {

            // ================= NORMALISASI =================
            encrypted = encrypted == null
                    ? ""
                    : encrypted.trim();

            // ================= KOSONG =================
            if (encrypted.isEmpty()) {

                JOptionPane.showMessageDialog(
                        null,
                        "Validasi gagal : Data MR kosong"
                );

                return false;
            }

            // ================= CEK FORMAT ENKRIPSI =================
            // minimal base64 format
            if (!encrypted.matches("^[A-Za-z0-9+/=]+$")) {

                JOptionPane.showMessageDialog(
                        null,
                        "Validasi gagal : Data MR bukan data enkripsi"
                );

                return false;
            }

            // ================= MINIMAL PANJANG =================
            if (encrypted.length() < 20) {

                JOptionPane.showMessageDialog(
                        null,
                        "Validasi gagal : Data MR bukan data enkripsi"
                );

                return false;
            }

            return true;

        } catch (Exception e) {

            System.out.println(
                    "Error validasi data MR : " + e
            );

            return false;
        }
    }

    private void BtnKirimActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnKirimActionPerformed
        ApiBpjsEmr apiBPJS = new ApiBpjsEmr();

        DateTimeFormatter inputFormat
                = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        for (i = 0; i < tbObat.getRowCount(); i++) {

            if (!tbObat.getValueAt(i, 0).toString().equals("true")
                    || tbObat.getValueAt(i, 5).toString().equals("")
                    || tbObat.getValueAt(i, 8).toString().equals("")
                    || !tbObat.getValueAt(i, 16).toString().equals("")) {
                continue;
            }

            try {

                // ================= DATA TABEL =================
                String noRawat = tbObat.getValueAt(i, 2).toString();
                String noRM = tbObat.getValueAt(i, 3).toString();
                String noSEP = tbObat.getValueAt(i, 5).toString();
                String tglMasuk = safeTime(tbObat.getValueAt(i, 8).toString());
                String tglKeluar = safeTime(tbObat.getValueAt(i, 9).toString());
                String sttsLanjut = tbObat.getValueAt(i, 14).toString();
                String safeId = noRawat.replaceAll("[^A-Za-z0-9]", "");
                String kelas
                        = sttsLanjut.equalsIgnoreCase("Ranap")
                        ? "IMP"
                        : "AMB";
                String jnsPelayanan
                        = kelas.equals("IMP") ? "1" : "2";
                System.out.println(
                        "JnsPl : " + jnsPelayanan
                );

                String bulan = tglMasuk.substring(5, 7);

                String tahun = tglMasuk.substring(0, 4);

                // ================= UAT =================
                if (!validasiNoSEP(noSEP)) {
                    continue;
                }
                if (!validasiJenisKunjungan(jnsPelayanan)) {
                    continue;
                }
                if (!validasiJenisKunjunganKlaim(
                        noSEP,
                        noRawat,
                        jnsPelayanan
                )) {
                    continue;
                }
                if (!validasiBulan(bulan)) {
                    continue;
                }
                if (!validasiBulanKlaim(noSEP, bulan)) {
                    continue;
                }
                if (!validasiTahun(noSEP, tahun)) {
                    continue;
                }
                // ================= QUERY PROFILE =================
                String profile
                        = "SELECT "
                        + "pr.nama_instansi, "
                        + "pr.alamat_instansi, "
                        + "pr.kabupaten, "
                        + "pr.propinsi, "
                        + "pr.kontak, "
                        + "pr.email "
                        + "FROM setting pr";
                // ================= AMBIL DATA PROFILE =================
                PreparedStatement psProfile
                        = koneksi.prepareStatement(profile);

                ResultSet rsProfile
                        = psProfile.executeQuery();

                // ================= DEFAULT =================
                String instansi = "";
                String alamatInstansi = "";
                String kabupaten = "";
                String propinsi = "";
                String kontak = "";
                String email = "";

                if (rsProfile.next()) {

                    instansi = rsProfile.getString("nama_instansi");
                    alamatInstansi = rsProfile.getString("alamat_instansi");
                    kabupaten = rsProfile.getString("kabupaten");
                    propinsi = rsProfile.getString("propinsi");
                    kontak = rsProfile.getString("kontak");
                    email = rsProfile.getString("email");
                }

                rsProfile.close();
                psProfile.close();

                String sql
                        = "SELECT "
                        + "rp.no_rkm_medis, "
                        + "p.nm_pasien, p.stts_nikah, p.no_ktp, p.no_peserta, "
                        + "p.jk, p.tgl_lahir, p.alamat, p.no_tlp, "
                        + "d.nm_dokter, d.tgl_lahir AS dr_tgl_lahir, d.jk AS dr_jk, "
                        + "dp.kd_penyakit, py.nm_penyakit, "
                        + "bs.no_rujukan, "
                        + "bsi.no_rujukan AS no_rujukan_internal "
                        + "FROM reg_periksa rp "
                        + "JOIN pasien p ON rp.no_rkm_medis = p.no_rkm_medis "
                        + "LEFT JOIN dokter d ON rp.kd_dokter = d.kd_dokter "
                        + "LEFT JOIN diagnosa_pasien dp "
                        + "ON rp.no_rawat = dp.no_rawat "
                        + "AND dp.prioritas='1' "
                        + "LEFT JOIN penyakit py "
                        + "ON dp.kd_penyakit = py.kd_penyakit "
                        // bridging_sep
                        + "LEFT JOIN bridging_sep bs "
                        + "ON bs.no_rawat = rp.no_rawat "
                        + "AND bs.jnspelayanan = ( "
                        + "    SELECT x.jnspelayanan "
                        + "    FROM bridging_sep x "
                        + "    WHERE x.no_rawat = rp.no_rawat "
                        + "    ORDER BY CASE "
                        + "        WHEN x.jnspelayanan='1' THEN 1 "
                        + "        WHEN x.jnspelayanan='2' THEN 2 "
                        + "        ELSE 3 "
                        + "    END "
                        + "    LIMIT 1 "
                        + ") "
                        // bridging_sep_internal
                        + "LEFT JOIN bridging_sep_internal bsi "
                        + "ON bsi.no_rawat = rp.no_rawat "
                        + "AND bsi.jnspelayanan = ( "
                        + "    SELECT y.jnspelayanan "
                        + "    FROM bridging_sep_internal y "
                        + "    WHERE y.no_rawat = rp.no_rawat "
                        + "    ORDER BY CASE "
                        + "        WHEN y.jnspelayanan='1' THEN 1 "
                        + "        WHEN y.jnspelayanan='2' THEN 2 "
                        + "        ELSE 3 "
                        + "    END "
                        + "    LIMIT 1 "
                        + ") "
                        + "WHERE rp.no_rawat=? "
                        + "LIMIT 1";

                PreparedStatement ps = koneksi.prepareStatement(sql);
                ps.setString(1, noRawat);

                ResultSet rs = ps.executeQuery();

                if (!rs.next()) {
                    rs.close();
                    ps.close();
                    continue;
                }

                // ================= DATA PASIEN =================
                String nama = rs.getString("nm_pasien");
                String nik = rs.getString("no_ktp");
                String bpjs = rs.getString("no_peserta");
                String jk = rs.getString("jk");
                String lahir = rs.getString("tgl_lahir");
                String alamat = rs.getString("alamat");
                String hp = rs.getString("no_tlp");
                String dokter = rs.getString("nm_dokter");
                String tgl_lhr_dokter = rs.getString("dr_tgl_lahir");
                String dokter_jk = rs.getString("dr_jk");
                String icd = rs.getString("kd_penyakit");
                String diagnosa = rs.getString("nm_penyakit");
                String no_rujukan = rs.getString("no_rujukan");
                String no_rujukan_internal = rs.getString("no_rujukan_internal");
                String stts_nikah = rs.getString("stts_nikah");

                rs.close();
                ps.close();

                // ================= VALIDASI =================
                if (nik == null || bpjs == null || icd == null) {
                    continue;
                }

                // ================= RESUME MEDIS =================
                String alasanMasuk = "-";
                String keluhanUtama = "-";

                String diagnosaMasuk = "-";
                String kdDiagnosaMasuk = "-";

                String diagnosa2 = "-";
                String kdDiagnosa2 = "-";

                String diagnosa3 = "-";
                String kdDiagnosa3 = "-";

                String diagnosa4 = "-";
                String kdDiagnosa4 = "-";

                String diagnosa5 = "-";
                String kdDiagnosa5 = "-";

                String prosedurUtama = "-";
                String kdProsedurUtama = "-";

                String prosedur2 = "-";
                String kdProsedur2 = "-";

                String prosedur3 = "-";
                String kdProsedur3 = "-";

                String prosedur4 = "-";
                String kdProsedur4 = "-";

                String obatPulang = "-";
                String alergiPasien = "Tidak ada alergi";
                String instruksiDokter = "Kontrol kembali sesuai jadwal";

                String tableResume
                        = sttsLanjut.equalsIgnoreCase("Ranap")
                        ? "resume_pasien_ranap"
                        : "resume_pasien";

                String sqlResume
                        = "SELECT "
                        + "keluhan_utama, "
                        + "diagnosa_utama, "
                        + "kd_diagnosa_utama, "
                        + "diagnosa_sekunder, "
                        + "kd_diagnosa_sekunder, "
                        + "diagnosa_sekunder2, "
                        + "kd_diagnosa_sekunder2, "
                        + "diagnosa_sekunder3, "
                        + "kd_diagnosa_sekunder3, "
                        + "diagnosa_sekunder4, "
                        + "kd_diagnosa_sekunder4, "
                        + "prosedur_utama, "
                        + "kd_prosedur_utama, "
                        + "prosedur_sekunder, "
                        + "kd_prosedur_sekunder, "
                        + "prosedur_sekunder2, "
                        + "kd_prosedur_sekunder2, "
                        + "prosedur_sekunder3, "
                        + "kd_prosedur_sekunder3, "
                        + "obat_pulang "
                        + "FROM " + tableResume + " "
                        + "WHERE no_rawat=? "
                        + "LIMIT 1";

                PreparedStatement psResume
                        = koneksi.prepareStatement(sqlResume);

                psResume.setString(1, noRawat);

                ResultSet rsResume
                        = psResume.executeQuery();

                if (rsResume.next()) {

                    alasanMasuk
                            = rsResume.getString("keluhan_utama") == null
                            ? "-"
                            : rsResume.getString("keluhan_utama");

                    keluhanUtama
                            = rsResume.getString("keluhan_utama") == null
                            ? "-"
                            : rsResume.getString("keluhan_utama");

                    // ================= DIAGNOSA =================
                    diagnosaMasuk
                            = rsResume.getString("diagnosa_utama") == null
                            ? "-"
                            : rsResume.getString("diagnosa_utama");

                    kdDiagnosaMasuk
                            = rsResume.getString("kd_diagnosa_utama") == null
                            ? "-"
                            : rsResume.getString("kd_diagnosa_utama");

                    diagnosa2
                            = rsResume.getString("diagnosa_sekunder") == null
                            ? "-"
                            : rsResume.getString("diagnosa_sekunder");

                    kdDiagnosa2
                            = rsResume.getString("kd_diagnosa_sekunder") == null
                            ? "-"
                            : rsResume.getString("kd_diagnosa_sekunder");

                    diagnosa3
                            = rsResume.getString("diagnosa_sekunder2") == null
                            ? "-"
                            : rsResume.getString("diagnosa_sekunder2");

                    kdDiagnosa3
                            = rsResume.getString("kd_diagnosa_sekunder2") == null
                            ? "-"
                            : rsResume.getString("kd_diagnosa_sekunder2");

                    diagnosa4
                            = rsResume.getString("diagnosa_sekunder3") == null
                            ? "-"
                            : rsResume.getString("diagnosa_sekunder3");

                    kdDiagnosa4
                            = rsResume.getString("kd_diagnosa_sekunder3") == null
                            ? "-"
                            : rsResume.getString("kd_diagnosa_sekunder3");

                    diagnosa5
                            = rsResume.getString("diagnosa_sekunder4") == null
                            ? "-"
                            : rsResume.getString("diagnosa_sekunder4");

                    kdDiagnosa5
                            = rsResume.getString("kd_diagnosa_sekunder4") == null
                            ? "-"
                            : rsResume.getString("kd_diagnosa_sekunder4");

                    // ================= PROSEDUR =================
                    prosedurUtama
                            = rsResume.getString("prosedur_utama") == null
                            ? "-"
                            : rsResume.getString("prosedur_utama");

                    kdProsedurUtama
                            = rsResume.getString("kd_prosedur_utama") == null
                            ? "-"
                            : rsResume.getString("kd_prosedur_utama");

                    prosedur2
                            = rsResume.getString("prosedur_sekunder") == null
                            ? "-"
                            : rsResume.getString("prosedur_sekunder");

                    kdProsedur2
                            = rsResume.getString("kd_prosedur_sekunder") == null
                            ? "-"
                            : rsResume.getString("kd_prosedur_sekunder");

                    prosedur3
                            = rsResume.getString("prosedur_sekunder2") == null
                            ? "-"
                            : rsResume.getString("prosedur_sekunder2");

                    kdProsedur3
                            = rsResume.getString("kd_prosedur_sekunder2") == null
                            ? "-"
                            : rsResume.getString("kd_prosedur_sekunder2");

                    prosedur4
                            = rsResume.getString("prosedur_sekunder3") == null
                            ? "-"
                            : rsResume.getString("prosedur_sekunder3");

                    kdProsedur4
                            = rsResume.getString("kd_prosedur_sekunder3") == null
                            ? "-"
                            : rsResume.getString("kd_prosedur_sekunder3");

                    // ================= OBAT =================
                    obatPulang
                            = rsResume.getString("obat_pulang") == null
                            ? "-"
                            : rsResume.getString("obat_pulang");
                }

                rsResume.close();
                psResume.close();

                // ================= FORMAT DATE =================
                String tglMasukISO;
                String tglKeluarISO;

                try {

                    if (tglMasuk.contains("T")) {

                        tglMasukISO = tglMasuk;

                    } else {

                        LocalDateTime ldtMasuk
                                = LocalDateTime.parse(
                                        tglMasuk,
                                        inputFormat
                                );

                        tglMasukISO
                                = ldtMasuk
                                        .atZone(ZoneId.of("Asia/Jakarta"))
                                        .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
                    }

                } catch (Exception e) {

                    System.out.println(
                            "ERROR TGL MASUK : " + tglMasuk
                    );

                    continue;
                }

                try {

                    if (tglKeluar.contains("T")) {

                        tglKeluarISO = tglKeluar;

                    } else {

                        LocalDateTime ldtKeluar
                                = LocalDateTime.parse(
                                        tglKeluar,
                                        inputFormat
                                );

                        tglKeluarISO
                                = ldtKeluar
                                        .atZone(ZoneId.of("Asia/Jakarta"))
                                        .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
                    }

                } catch (Exception e) {

                    System.out.println(
                            "ERROR TGL KELUAR : " + tglKeluar
                    );

                    continue;
                }

                // ================= FIX DATA =================
                String gender
                        = jk.equals("L")
                        ? "male"
                        : jk.equals("P")
                        ? "female"
                        : "unknown";

                String genderDr
                        = jk.equals("L")
                        ? "male"
                        : jk.equals("P")
                        ? "female"
                        : "unknown";

                String birth
                        = (lahir != null && lahir.length() >= 10)
                        ? lahir.substring(0, 10)
                        : "2000-01-01";

                String birthDr
                        = (tgl_lhr_dokter != null && tgl_lhr_dokter.length() >= 10)
                        ? tgl_lhr_dokter.substring(0, 10)
                        : "2000-01-01";

                String diagnosaFix
                        = diagnosaMasuk + " (" + kdDiagnosaMasuk + ")";

                // ================= ESCAPE JSON =================
                keluhanUtama
                        = keluhanUtama.replace("\"", "\\\"");

                diagnosaMasuk
                        = diagnosaMasuk.replace("\"", "\\\"");

                alergiPasien
                        = alergiPasien.replace("\"", "\\\"");

                instruksiDokter
                        = instruksiDokter.replace("\"", "\\\"");

                obatPulang
                        = obatPulang.replace("\"", "\\\"");

                boolean adaRadiologi = false;

                PreparedStatement psRad = koneksi.prepareStatement(
                        "SELECT 1 "
                        + "FROM periksa_radiologi pr "
                        + "INNER JOIN jns_perawatan_radiologi jp "
                        + "ON pr.kd_jenis_prw=jp.kd_jenis_prw "
                        + "WHERE pr.no_rawat=? "
                        + "LIMIT 1"
                );

                psRad.setString(1, noRawat);

                ResultSet rsRad = psRad.executeQuery();

                if (rsRad.next()) {
                    adaRadiologi = true;
                }

                rsRad.close();
                psRad.close();

                // ================= DEFAULT ADDRESS =================
                String kota = "-";
                String kecamatan = "-";
                String provinsi = "-";
                String kodePos = "00000";

                // ================= ROOT ID =================
                String rootId
                        = "0204R015-3524044-" + safeId;

                // ================= RESOURCE ID =================
                String compId = rootId + "-comp";
                String patId = rootId + "-pat";
                String encId = rootId + "-enc";
                String condId = rootId + "-cond";
                String pracId = rootId + "-prac";
                String orgId = rootId + "-org";
                String procId = rootId + "-proc";

                // ================= LOG =================
                System.out.println("========================");
                System.out.println("NO RAWAT : " + noRawat);
                System.out.println("NO SEP   : " + noSEP);
                System.out.println("NAMA     : " + nama);
                System.out.println("ICD      : " + icd);
                System.out.println("========================");

                // ==================================================
                // LANJUT JSON FHIR
                // ==================================================
                String bundleId
                        = rootId + "-" + UUID.randomUUID().toString();

                System.out.println("KELUHAN     : " + keluhanUtama);
                System.out.println("DIAGNOSA    : " + diagnosaMasuk);
                System.out.println("ALERGI      : " + alergiPasien);
                System.out.println("ANJURAN     : " + instruksiDokter);
                System.out.println("OBAT        : " + obatPulang);
                System.out.println("STTS LANJUT : " + sttsLanjut);
                System.out.println("NO RAWAT    : " + noRawat);

                String composition
                        = "{"
                        + "\"resourceType\":\"Composition\","
                        + "\"id\":\"" + compId + "\","
                        + "\"status\":\"final\","
                        + "\"type\":{"
                        + "\"coding\":[{"
                        + "\"system\":\"http://loinc.org\","
                        + "\"code\":\"11503-0\","
                        + "\"display\":\"Medical records\""
                        + "}],"
                        + "\"text\":\"Medical records\""
                        + "},"
                        + "\"subject\":{"
                        + "\"reference\":\"Patient/" + patId + "\","
                        + "\"display\":\"" + nama + "\""
                        + "},"
                        + "\"encounter\":{"
                        + "\"reference\":\"Encounter/" + encId + "\""
                        + "},"
                        + "\"date\":\"" + tglKeluar + "\","
                        + "\"author\":[{"
                        + "\"reference\":\"Practitioner/" + pracId + "\","
                        + "\"display\":\"" + dokter + "\""
                        + "}],"
                        + "\"title\":\"Medical records\","
                        + "\"confidentiality\":\"N\","
                        + "\"section\":{"
                        // ================= SECTION 0 =================
                        + "\"0\":{"
                        + "\"title\":\"Reason for admission\","
                        + "\"code\":{"
                        + "\"coding\":[{"
                        + "\"system\":\"http://loinc.org\","
                        + "\"code\":\"29299-5\","
                        + "\"display\":\"Reason for visit Narrative\""
                        + "}]"
                        + "},"
                        + "\"text\":{"
                        + "\"status\":\"additional\","
                        + "\"div\":\"" + keluhanUtama + "\""
                        + "},"
                        + "\"mode\":\"working\","
                        + "\"entry\":[]"
                        + "},"
                        // ================= SECTION 1 =================
                        + "\"1\":{"
                        + "\"title\":\"Chief complaint\","
                        + "\"code\":{"
                        + "\"coding\":[{"
                        + "\"system\":\"http://loinc.org\","
                        + "\"code\":\"10154-3\","
                        + "\"display\":\"Chief complaint Narrative\""
                        + "}]"
                        + "},"
                        + "\"text\":{"
                        + "\"status\":\"additional\","
                        + "\"div\":\"" + keluhanUtama + "\""
                        + "},"
                        + "\"mode\":\"working\","
                        + "\"entry\":[]"
                        + "},"
                        // ================= SECTION 2 =================
                        + "\"2\":{"
                        + "\"title\":\"Admission diagnosis\","
                        + "\"code\":{"
                        + "\"coding\":[{"
                        + "\"system\":\"http://loinc.org\","
                        + "\"code\":\"42347-5\","
                        + "\"display\":\"Admission diagnosis Narrative\""
                        + "}]"
                        + "},"
                        + "\"text\":{"
                        + "\"status\":\"additional\","
                        + "\"div\":\"" + diagnosaMasuk + "\""
                        + "},"
                        + "\"mode\":\"working\","
                        + "\"entry\":[]"
                        + "},"
                        // ================= SECTION 4 =================
                        + "\"4\":{"
                        + "\"title\":\"Medications on Discharge\","
                        + "\"code\":{"
                        + "\"coding\":[{"
                        + "\"system\":\"http://loinc.org\","
                        + "\"code\":\"75311-1\","
                        + "\"display\":\"Hospital discharge medications Narrative\""
                        + "}]"
                        + "},"
                        + "\"text\":{"
                        + "\"status\":\"additional\","
                        + "\"div\":\"" + obatPulang + "\""
                        + "},"
                        + "\"mode\":\"working\","
                        + "\"entry\":[]"
                        + "},"
                        // ================= SECTION 5 =================
                        + "\"5\":{"
                        + "\"title\":\"Plan of care\","
                        + "\"code\":{"
                        + "\"coding\":[{"
                        + "\"system\":\"http://loinc.org\","
                        + "\"code\":\"18776-5\","
                        + "\"display\":\"Plan of care\""
                        + "}]"
                        + "},"
                        + "\"text\":{"
                        + "\"status\":\"additional\","
                        + "\"div\":\"Kontrol kembali bila keluhan berlanjut\""
                        + "},"
                        + "\"mode\":\"working\","
                        + "\"entry\":\"\""
                        + "},"
                        // ================= SECTION 7 =================
                        + "\"7\":{"
                        + "\"title\":\"Known allergies\","
                        + "\"code\":{"
                        + "\"coding\":[{"
                        + "\"system\":\"http://snomed.info/sct\","
                        + "\"code\":\"716186003\","
                        + "\"display\":\"No known allergy\""
                        + "}]"
                        + "},"
                        + "\"text\":{"
                        + "\"status\":\"additional\","
                        + "\"div\":\"Tidak ada riwayat alergi yang diketahui.\""
                        + "},"
                        + "\"mode\":\"working\","
                        + "\"entry\":[]"
                        + "}"
                        + "}"
                        + "}";

                String encounter
                        = "{"
                        + "\"resourceType\":\"Encounter\","
                        + "\"id\":\"" + encId + "\","
                        + "\"identifier\":[{"
                        + "\"use\":\"usual\","
                        + "\"type\":{"
                        + "\"coding\":[],"
                        + "\"text\":\"Nomor SEP BPJS\""
                        + "},"
                        + "\"system\":\"http://api.bpjs-kesehatan.go.id:8080/Vclaim-rest/SEP/\","
                        + "\"value\":\"" + noSEP + "\","
                        + "\"assigner\":{"
                        + "\"display\":\"BPJS Kesehatan\""
                        + "}"
                        + "}],"
                        + "\"subject\":{"
                        + "\"reference\":\"Patient/" + patId + "\","
                        + "\"display\":\"" + nama + "\","
                        + "\"noSep\":\"" + noSEP + "\""
                        + "},"
                        + "\"class\":{"
                        + "\"system\":\"http://terminology.hl7.org/CodeSystem/v3-ActCode\","
                        + "\"code\":\"" + kelas + "\","
                        + "\"display\":\"" + (kelas.equals("IMP") ? "inpatient encounter" : "Ambulatory") + "\""
                        + "},"
                        + "\"incomingReferral\":[{"
                        + "\"identifier\":["
                        + "{"
                        + "\"use\":\"usual\","
                        + "\"type\":{"
                        + "\"coding\":[],"
                        + "\"text\":\"Nomor Rujukan BPJS\""
                        + "},"
                        + "\"system\":\"nomor_rujukan_bpjs\","
                        + "\"value\":\"" + no_rujukan + "\","
                        + "\"assigner\":{"
                        + "\"display\":\"BPJS Kesehatan\""
                        + "}"
                        + "},"
                        + "{"
                        + "\"use\":\"usual\","
                        + "\"type\":{"
                        + "\"coding\":[],"
                        + "\"text\":\"Nomor Rujukan Internal RS\""
                        + "},"
                        + "\"system\":\"nomor_rujukan_internal_rs\","
                        + "\"value\":\"" + no_rujukan_internal + "\","
                        + "\"assigner\":{"
                        + "\"display\":\"Rumah Sakit\""
                        + "}"
                        + "}"
                        + "]"
                        + "}],"
                        + "\"reason\":[{"
                        + "\"coding\":[{"
                        + "\"code\":\"" + icd + "\","
                        + "\"display\":\"" + diagnosa + "\","
                        + "\"system\":\"http://hl7.org/fhir/sid/icd-10\""
                        + "}],"
                        + "\"text\":\"" + diagnosaFix + "\""
                        + "}],"
                        // ================= DIAGNOSIS =================
                        + "\"diagnosis\":["
                        // ================= DIAGNOSA 1 =================
                        + "{"
                        + "\"condition\":{"
                        + "\"reference\":\"Condition/" + condId + "-1\","
                        + "\"role\":{"
                        + "\"coding\":[{"
                        + "\"system\":\"http://hl7.org/fhir/diagnosis-role\","
                        + "\"code\":\"DD\","
                        + "\"display\":\"Discharge Diagnosis\""
                        + "}],"
                        + "\"text\":\"" + diagnosaMasuk + "\""
                        + "},"
                        + "\"rank\":1"
                        + "}"
                        + "},"
                        // ================= DIAGNOSA 2 =================
                        + "{"
                        + "\"condition\":{"
                        + "\"reference\":\"Condition/" + condId + "-2\","
                        + "\"role\":{"
                        + "\"coding\":[{"
                        + "\"system\":\"http://hl7.org/fhir/diagnosis-role\","
                        + "\"code\":\"DD\","
                        + "\"display\":\"Discharge Diagnosis\""
                        + "}],"
                        + "\"text\":\"" + diagnosa2 + "\""
                        + "},"
                        + "\"rank\":2"
                        + "}"
                        + "},"
                        // ================= DIAGNOSA 3 =================
                        + "{"
                        + "\"condition\":{"
                        + "\"reference\":\"Condition/" + condId + "-3\","
                        + "\"role\":{"
                        + "\"coding\":[{"
                        + "\"system\":\"http://hl7.org/fhir/diagnosis-role\","
                        + "\"code\":\"DD\","
                        + "\"display\":\"Discharge Diagnosis\""
                        + "}],"
                        + "\"text\":\"" + diagnosa3 + "\""
                        + "},"
                        + "\"rank\":3"
                        + "}"
                        + "}"
                        + "],"
                        + "\"hospitalization\":{"
                        + "\"dischargeDisposition\":[{"
                        + "\"coding\":[{"
                        + "\"code\":\"home\","
                        + "\"display\":\"Home\","
                        + "\"system\":\"http://terminology.hl7.org/CodeSystem/discharge-disposition\""
                        + "}],"
                        + "\"text\":\"Pulang ke rumah\""
                        + "}]"
                        + "},"
                        + "\"period\":{"
                        + "\"start\":\"" + tglMasukISO + "\","
                        + "\"end\":\"" + tglKeluarISO + "\""
                        + "},"
                        + "\"status\":\"finished\","
                        + "\"text\":{"
                        + "\"div\":\"Pasien " + nama + " dengan diagnosa " + diagnosaFix
                        + " dirawat pada " + tglMasukISO
                        + " sampai " + tglKeluarISO + "\","
                        + "\"status\":\"generated\""
                        + "}"
                        + "}";

                String organization
                        = "{"
                        + "\"resourceType\":\"Organization\","
                        + "\"id\":\"" + orgId + "\","
                        + "\"identifier\":["
                        + "{"
                        + "\"use\":\"official\","
                        + "\"system\":\"urn:oid:bpjs\","
                        + "\"value\":\"0204R015\""
                        + "},"
                        + "{"
                        + "\"use\":\"official\","
                        + "\"system\":\"urn:oid:kemkes\","
                        + "\"value\":\"3524044\""
                        + "}"
                        + "],"
                        + "\"type\":[{"
                        + "\"coding\":[{"
                        + "\"system\":\"http://terminology.hl7.org/CodeSystem/organization-type\","
                        + "\"code\":\"prov\","
                        + "\"display\":\"Healthcare Provider\""
                        + "}],"
                        + "\"text\":\"Healthcare Provider\""
                        + "}],"
                        // ================= NAMA INSTANSI =================
                        + "\"name\":\"" + (instansi == null ? "" : instansi.replace("\"", "\\\\\"")) + "\","
                        // ================= ALIAS =================
                        + "\"alias\":["
                        + "\"" + (instansi == null ? "" : instansi.replace("\"", "\\\\\"")) + "\""
                        + "],"
                        // ================= TELECOM =================
                        + "\"telecom\":[{"
                        + "\"system\":\"phone\","
                        + "\"value\":\"" + (kontak == null ? "" : kontak) + "\","
                        + "\"use\":\"work\""
                        + "}],"
                        // ================= ADDRESS =================
                        + "\"address\":[{"
                        + "\"use\":\"work\","
                        + "\"text\":\""
                        + (alamatInstansi == null ? "" : alamatInstansi.replace("\"", "\\\\\""))
                        + ", "
                        + (kabupaten == null ? "" : kabupaten)
                        + ", "
                        + (propinsi == null ? "" : propinsi)
                        + ", Indonesia\","
                        + "\"line\":["
                        + "\""
                        + (alamatInstansi == null ? "" : alamatInstansi.replace("\"", "\\\\\""))
                        + "\""
                        + "],"
                        + "\"city\":\"" + (kabupaten == null ? "" : kabupaten) + "\","
                        + "\"state\":\"" + (propinsi == null ? "" : propinsi) + "\","
                        + "\"postalCode\":\"\","
                        + "\"country\":\"INDONESIA\""
                        + "}],"
                        // ================= CONTACT =================
                        + "\"contact\":[{"
                        + "\"purpose\":{"
                        + "\"coding\":[{"
                        + "\"system\":\"http://hl7.org/fhir/contactentity-type\","
                        + "\"code\":\"ADMIN\","
                        + "\"display\":\"Administrative\""
                        + "}]"
                        + "},"
                        + "\"telecom\":["
                        + "{"
                        + "\"system\":\"phone\","
                        + "\"value\":\"" + (kontak == null ? "" : kontak) + "\","
                        + "\"use\":\"work\""
                        + "},"
                        + "{"
                        + "\"system\":\"email\","
                        + "\"value\":\"" + (email == null ? "" : email) + "\","
                        + "\"use\":\"work\""
                        + "}"
                        + "]"
                        + "}]"
                        + "}";

                String patient
                        = "{"
                        + "\"resourceType\":\"Patient\","
                        + "\"id\":\"" + patId + "\","
                        + "\"identifier\":["
                        + "{"
                        + "\"use\":\"usual\","
                        + "\"type\":{"
                        + "\"coding\":[{"
                        + "\"system\":\"http://terminology.hl7.org/CodeSystem/v2-0203\","
                        + "\"code\":\"MR\","
                        + "\"display\":\"Medical record number\""
                        + "}],"
                        + "\"text\":\"Nomor Rekam Medis\""
                        + "},"
                        + "\"value\":\"" + noRM + "\","
                        + "\"assigner\":{"
                        + "\"display\":\"RS ABDURRAHMAN SYAMSYURI\""
                        + "}"
                        + "},"
                        + "{"
                        + "\"use\":\"official\","
                        + "\"type\":{"
                        + "\"coding\":[{"
                        + "\"system\":\"http://terminology.hl7.org/CodeSystem/v2-0203\","
                        + "\"code\":\"MB\","
                        + "\"display\":\"Member Number\""
                        + "}],"
                        + "\"text\":\"Nomor Peserta BPJS\""
                        + "},"
                        + "\"value\":\"" + bpjs + "\","
                        + "\"assigner\":{"
                        + "\"display\":\"BPJS KESEHATAN\""
                        + "}"
                        + "},"
                        + "{"
                        + "\"use\":\"official\","
                        + "\"type\":{"
                        + "\"coding\":[{"
                        + "\"system\":\"http://hl7.org/fhir/v2/0203\","
                        + "\"code\":\"NNIDN\","
                        + "\"display\":\"National Person Identifier\""
                        + "}],"
                        + "\"text\":\"Nomor KTP\""
                        + "},"
                        + "\"value\":\"" + nik + "\","
                        + "\"assigner\":{"
                        + "\"display\":\"KEMENDAGRI\""
                        + "}"
                        + "}"
                        + "],"
                        + "\"active\":true,"
                        + "\"name\":[{"
                        + "\"use\":\"official\","
                        + "\"text\":\"" + nama + "\""
                        + "}],"
                        + "\"maritalStatus\":{"
                        + "\"coding\":[{"
                        + "\"system\":\"http://terminology.hl7.org/CodeSystem/v3-MaritalStatus\","
                        + "\"code\":\"U\","
                        + "\"display\":\"" + stts_nikah + "\""
                        + "}],"
                        + "\"text\":\"" + stts_nikah + "\""
                        + "},"
                        + "\"telecom\":["
                        + "{"
                        + "\"system\":\"phone\","
                        + "\"value\":\"" + (hp == null ? "" : hp) + "\","
                        + "\"use\":\"mobile\""
                        + "}"
                        + "],"
                        + "\"gender\":\"" + gender + "\","
                        + "\"birthDate\":\"" + birth + "\","
                        + "\"deceasedBoolean\":false,"
                        + "\"address\":[{"
                        + "\"line\":["
                        + "\"" + (alamat == null ? "" : alamat.replace("\"", "\\\\\"")) + "\""
                        + "],"
                        + "\"city\":\"" + kota + "\","
                        + "\"district\":\"" + kecamatan + "\","
                        + "\"state\":\"" + provinsi + "\","
                        + "\"postalCode\":\"" + kodePos + "\","
                        + "\"text\":\"" + (alamat == null ? "" : alamat.replace("\"", "\\\\\"")) + "\","
                        + "\"use\":\"home\","
                        + "\"type\":\"both\""
                        + "}],"
                        + "\"managingOrganization\":{"
                        + "\"reference\":\"Organization/" + orgId + "\","
                        + "\"display\":\"RS ABDURRAHMAN SYAMSYURI\""
                        + "}"
                        + "}";

                String practitioner
                        = "{"
                        + "\"resourceType\":\"Practitioner\","
                        + "\"id\":\"" + pracId + "\","
                        + "\"identifier\":["
                        + "{"
                        + "\"use\":\"official\","
                        + "\"system\":\"urn:oid:nomor_sip\","
                        + "\"type\":{"
                        + "\"coding\":[{"
                        + "\"system\":\"http://terminology.hl7.org/CodeSystem/v2-0203\","
                        + "\"code\":\"MD\","
                        + "\"display\":\"Medical License number\""
                        + "}]"
                        + "},"
                        + "\"value\":\"503001SIP-DDPMPTSP.D2024\","
                        + "\"assigner\":{"
                        + "\"display\":\"KEMENKES\""
                        + "}"
                        + "},"
                        + "{"
                        + "\"use\":\"official\","
                        + "\"system\":\"NIK\","
                        + "\"type\":{"
                        + "\"coding\":[{"
                        + "\"system\":\"http://terminology.hl7.org/CodeSystem/v2-0203\","
                        + "\"code\":\"NNIDN\","
                        + "\"display\":\"National Person Identifier\""
                        + "}]"
                        + "},"
                        + "\"value\":\"3172055103530001\","
                        + "\"assigner\":{"
                        + "\"display\":\"KEMDAGRI\""
                        + "}"
                        + "},"
                        + "{"
                        + "\"use\":\"usual\","
                        + "\"system\":\"NIP\","
                        + "\"type\":{"
                        + "\"coding\":[{"
                        + "\"system\":\"http://hl7.org/fhir/name-use\","
                        + "\"code\":\"EN\","
                        + "\"display\":\"Employer number\""
                        + "}]"
                        + "},"
                        + "\"value\":\"\","
                        + "\"assigner\":{"
                        + "\"display\":\"RS ABDURRAHMAN SYAMSYURI\""
                        + "}"
                        + "}"
                        + "],"
                        + "\"name\":[{"
                        + "\"use\":\"official\","
                        + "\"text\":\"" + dokter + "\""
                        + "}],"
                        + "\"telecom\":["
                        + "{"
                        + "\"system\":\"phone\","
                        + "\"value\":\"\","
                        + "\"use\":\"work\""
                        + "},"
                        + "{"
                        + "\"system\":\"email\","
                        + "\"value\":\"\","
                        + "\"use\":\"work\""
                        + "},"
                        + "{"
                        + "\"system\":\"fax\","
                        + "\"value\":\"\","
                        + "\"use\":\"work\""
                        + "},"
                        + "{"
                        + "\"system\":\"home\","
                        + "\"value\":\"\","
                        + "\"use\":\"home\""
                        + "}"
                        + "],"
                        + "\"address\":[{"
                        + "\"use\":\"home\","
                        + "\"text\":\"Lamongan\","
                        + "\"line\":["
                        + "\"Lamongan\""
                        + "],"
                        + "\"city\":\"Lamongan\","
                        + "\"postalCode\":\"62211\","
                        + "\"country\":\"INDONESIA\""
                        + "}],"
                        + "\"gender\":\"" + genderDr + "\","
                        + "\"birthDate\":\"" + birthDr + "\""
                        + "}";

                // ================= CONDITION =================
                StringBuilder condition = new StringBuilder();

                // ================= DIAGNOSA ARRAY =================
                String[] kdDiag = {
                    kdDiagnosaMasuk,
                    kdDiagnosa2,
                    kdDiagnosa3,
                    kdDiagnosa4,
                    kdDiagnosa5
                };

                String[] nmDiag = {
                    diagnosaMasuk,
                    diagnosa2,
                    diagnosa3,
                    diagnosa4,
                    diagnosa5
                };

                // ================= LOOP CONDITION =================
                for (int d = 0; d < kdDiag.length; d++) {

                    // skip jika kosong
                    if (kdDiag[d] == null
                            || kdDiag[d].trim().equals("")
                            || kdDiag[d].trim().equals("-")) {
                        continue;
                    }

                    String condIdFix
                            = condId + "-" + (d + 1);

                    String ConditionFix
                            = nmDiag[d] + " (" + kdDiag[d] + ")";

                    condition.append(
                            "{"
                            + "\"resource\":{"
                            + "\"resourceType\":\"Condition\","
                            + "\"id\":\"" + condIdFix + "\","
                            + "\"category\":[{"
                            + "\"coding\":[{"
                            + "\"system\":\"http://hl7.org/fhir/condition-category\","
                            + "\"code\":\"encounter-diagnosis\","
                            + "\"display\":\"Encounter Diagnosis\""
                            + "}]"
                            + "}],"
                            + "\"code\":{"
                            + "\"coding\":[{"
                            + "\"system\":\"http://hl7.org/fhir/sid/icd-10\","
                            + "\"code\":\"" + kdDiag[d] + "\","
                            + "\"display\":\"" + nmDiag[d] + "\""
                            + "}],"
                            + "\"text\":\"" + ConditionFix + "\""
                            + "},"
                            + "\"subject\":{"
                            + "\"reference\":\"Patient/" + patId + "\""
                            + "},"
                            + "\"onsetDateTime\":\"" + tglMasukISO + "\","
                            + "\"clinicalStatus\":\"active\","
                            + "\"verificationStatus\":\"confirmed\""
                            + "}"
                            + "}"
                    );

                    // koma antar condition
                    if (d < kdDiag.length - 1) {
                        condition.append(",");
                    }
                }

                // ================= PROCEDURE =================
                StringBuilder procedure = new StringBuilder();

                procedure.append("[");

                // ================= DATA PROCEDURE =================
                String[] kdProcedure = {
                    kdProsedurUtama,
                    kdProsedur2,
                    kdProsedur3,
                    kdProsedur4,};

                String[] nmProcedure = {
                    prosedurUtama,
                    prosedur2,
                    prosedur3,
                    prosedur4,};

                boolean firstProc = true;

                // ================= LOOP PROCEDURE =================
                for (int i = 0; i < kdProcedure.length; i++) {

                    // skip kosong
                    if (kdProcedure[i] == null
                            || kdProcedure[i].trim().equals("")
                            || kdProcedure[i].trim().equals("-")) {
                        continue;
                    }

                    // separator koma
                    if (!firstProc) {
                        procedure.append(",");
                    }

                    firstProc = false;

                    procedure.append(
                            "{"
                            + "\"resourceType\":\"Procedure\","
                            + "\"id\":\"" + procId + "-" + (i + 1) + "\","
                            + "\"text\":{"
                            + "\"status\":\"generated\","
                            + "\"div\":\"<div xmlns=\\\"http://www.w3.org/1999/xhtml\\\">Generated Narrative with Details</div>\""
                            + "},"
                            + "\"status\":\"completed\","
                            + "\"code\":{"
                            + "\"coding\":[{"
                            + "\"system\":\"http://hl7.org/fhir/sid/icd-9-cm\","
                            + "\"code\":\"" + kdProcedure[i] + "\","
                            + "\"display\":\"" + nmProcedure[i] + "\""
                            + "}]"
                            + "},"
                            + "\"subject\":{"
                            + "\"reference\":\"Patient/" + patId + "\","
                            + "\"display\":\"" + nama + "\""
                            + "},"
                            + "\"performedPeriod\":{"
                            + "\"start\":\"" + tglMasukISO + "\","
                            + "\"end\":\"" + tglKeluarISO + "\""
                            + "},"
                            + "\"performer\":[{"
                            + "\"actor\":{"
                            + "\"reference\":\"Practitioner/" + pracId + "\","
                            + "\"display\":\"" + dokter + "\""
                            + "},"
                            + "\"role\":{"
                            + "\"coding\":[{"
                            + "\"system\":\"http://snomed.info/sct\","
                            + "\"code\":\"310512001\","
                            + "\"display\":\"Medical oncologist\""
                            + "}]"
                            + "}"
                            + "}],"
                            + "\"reasonCode\":[{"
                            + "\"text\":\"DiagnosticReport/\""
                            + "}],"
                            + "\"bodySite\":[{"
                            + "\"coding\":[{"
                            + "\"system\":\"http://hl7.org/fhir/sid/icd-9-cm\","
                            + "\"code\":\"" + kdProcedure[i] + "\","
                            + "\"display\":\"" + nmProcedure[i] + "\""
                            + "}]"
                            + "}],"
                            + "\"focalDevice\":[{"
                            + "\"action\":{"
                            + "\"coding\":[{"
                            + "\"system\":\"\","
                            + "\"code\":\"\","
                            + "\"display\":\"\""
                            + "}]"
                            + "},"
                            + "\"manipulated\":{"
                            + "\"reference\":\"Device/\""
                            + "}"
                            + "}],"
                            + "\"context\":{"
                            + "\"reference\":\"Encounter/" + encId + "\","
                            + "\"display\":\"" + nama + " encounter on IGD\""
                            + "},"
                            + "\"note\":[{"
                            + "\"text\":\"" + nmProcedure[i] + "\""
                            + "}]"
                            + "}"
                    );
                }

                procedure.append("]");

                String diagnosticRAD
                        = "{"
                        + "\"resource\":["
                        + "{"
                        + "\"resourceType\":\"DiagnosticReport\","
                        // ================= ID =================
                        + "\"id\":\"" + rootId + "-rad\","
                        // ================= SUBJECT =================
                        + "\"subject\":{"
                        + "\"reference\":\"Patient/" + patId + "\","
                        + "\"display\":\"" + nama + "\","
                        + "\"noSep\":\"" + noSEP + "\""
                        + "},"
                        // ================= CATEGORY =================
                        + "\"category\":{"
                        + "\"coding\":{"
                        + "\"system\":\"http://hl7.org/fhir/v2/0074\","
                        + "\"code\":\"RAD\","
                        + "\"display\":\"Radiology\""
                        + "}"
                        + "},"
                        // ================= STATUS =================
                        + "\"status\":\"final\","
                        // ================= PERFORMER =================
                        + "\"performer\":[{"
                        + "\"reference\":\"Organization/" + orgId + "\","
                        + "\"display\":\"Radiologi Dan Kedokteran Nuklir\""
                        + "}],"
                        // ================= RESULT =================
                        + "\"result\":["
                        + "{"
                        + "\"resourceType\":\"Observation\","
                        + "\"id\":\"DX" + noRawat.replaceAll("[^0-9]", "") + "\","
                        + "\"status\":\"final\","
                        // ================= TEXT =================
                        + "\"text\":{"
                        + "\"status\":\"generated\","
                        + "\"div\":\"Teknik: Radiografi toraks dalam proyeksi PA. Tidak tampak infiltrat paru.\""
                        + "},"
                        // ================= DATE =================
                        + "\"issued\":\"" + tglKeluarISO + "\","
                        + "\"effectiveDateTime\":\"" + tglKeluarISO + "\","
                        + "\"valueQuantity\":{"
                        + "\"value\":\"1\","
                        + "\"comparator\":\"\","
                        + "\"unit\":\"RAD\","
                        + "\"system\":\"\","
                        + "\"code\":\"RAD\""
                        + "},"
                        + "\"interpretation\":{"
                        + "\"coding\":{"
                        + "\"system\":\"http://hl7.org/fhir/v2/0078\","
                        + "\"code\":\"N\","
                        + "\"display\":\"Normal\""
                        + "},"
                        + "\"text\":\"\""
                        + "},"
                        + "\"referenceRange\":{"
                        + "\"low\":{"
                        + "\"value\":\"0\","
                        + "\"unit\":\"RAD\","
                        + "\"system\":\"http://unitsofmeasure.org\","
                        + "\"code\":\"RAD\""
                        + "},"
                        + "\"high\":{"
                        + "\"value\":\"1\","
                        + "\"unit\":\"RAD\","
                        + "\"system\":\"http://unitsofmeasure.org\","
                        + "\"code\":\"RAD\""
                        + "}"
                        + "},"
                        + "\"code\":{"
                        + "\"coding\":{"
                        + "\"system\":\"http://snomed.info/sct\","
                        + "\"code\":\"PROCx000025499\","
                        + "\"display\":\"THORAX\""
                        + "},"
                        + "\"text\":\"THORAX\""
                        + "},"
                        // ================= DOKTER =================
                        + "\"performer\":{"
                        + "\"reference\":\"Practitioner/" + pracId + "\","
                        + "\"display\":\"" + dokter + "\""
                        + "},"
                        // ================= IMAGE =================
                        + "\"image\":[{"
                        + "\"comment\":\"\","
                        + "\"link\":{"
                        + "\"reference\":\"\","
                        + "\"display\":\"\""
                        + "}"
                        + "}],"
                        // ================= CONCLUSION =================
                        + "\"conclusion\":\"Tak tampak kelainan radiologis pada jantung dan paru.\""
                        + "}"
                        + "]"
                        + "}"
                        + "]"
                        + "}";

                // ================= MEDICATION =================
                String medication = "";
                try {

                    StringBuilder medJson
                            = new StringBuilder();

                    medJson.append("{");
                    medJson.append("\"resource\":[");

                    PreparedStatement psObat
                            = koneksi.prepareStatement(
                                    "SELECT ro.kode_brng, "
                                    + "db.nama_brng, "
                                    + "ro.jml, "
                                    + "kodesatuan.kode_sat, "
                                    + "ifnull(ro.aturan_pakai,'') aturan_pakai "
                                    + "FROM resep_obat ro "
                                    + "INNER JOIN databarang db "
                                    + "ON ro.kode_brng=db.kode_brng "
                                    + "LEFT JOIN kodesatuan "
                                    + "ON db.kode_sat=kodesatuan.kode_sat "
                                    + "WHERE ro.no_rawat=?"
                            );

                    psObat.setString(1, noRawat);

                    ResultSet rsObat
                            = psObat.executeQuery();

                    int noUrut = 1;

                    while (rsObat.next()) {

                        if (noUrut > 1) {
                            medJson.append(",");
                        }

                        String kodeObat
                                = rsObat.getString("kode_brng");

                        String namaObat
                                = rsObat.getString("nama_brng");

                        String jumlah
                                = rsObat.getString("jml");

                        String satuan
                                = rsObat.getString("kode_sat");

                        String aturanPakai
                                = rsObat.getString("aturan_pakai");

                        String medId
                                = rootId
                                + "-med-"
                                + noUrut;

                        medJson.append("{");

                        medJson.append("\"resourceType\":\"MedicationRequest\",");

                        medJson.append("\"text\":{");
                        medJson.append("\"div\":\"" + namaObat + "\"");
                        medJson.append("},");

                        medJson.append("\"identifier\":{");
                        medJson.append("\"system\":\"resep\",");
                        medJson.append("\"value\":\"" + medId + "\"");
                        medJson.append("},");

                        medJson.append("\"subject\":{");
                        medJson.append("\"display\":\"" + nama + "\",");
                        medJson.append("\"reference\":\"Patient/" + patId + "\"");
                        medJson.append("},");

                        medJson.append("\"intent\":\"final\",");

                        medJson.append("\"medicationCodeableConcept\":{");

                        medJson.append("\"coding\":[{");
                        medJson.append("\"code\":\"" + kodeObat + "\",");
                        medJson.append("\"system\":\"SIMRS/drug\"");
                        medJson.append("}],");

                        medJson.append("\"text\":\"" + namaObat + "\"");

                        medJson.append("},");

                        medJson.append("\"dosageInstruction\":[{");

                        medJson.append("\"doseQuantity\":{");
                        medJson.append("\"code\":\"" + satuan + "\",");
                        medJson.append("\"system\":\"http://unitsofmeasure.org\",");
                        medJson.append("\"unit\":\"" + satuan + "\",");
                        medJson.append("\"value\":\"" + jumlah + "\"");
                        medJson.append("},");

                        medJson.append("\"route\":{");
                        medJson.append("\"coding\":[{");
                        medJson.append("\"code\":\"26643006\",");
                        medJson.append("\"display\":\"Oral route\",");
                        medJson.append("\"system\":\"http://snomed.info/sct\"");
                        medJson.append("}]");
                        medJson.append("},");

                        medJson.append("\"timing\":{");
                        medJson.append("\"repeat\":{");
                        medJson.append("\"frequency\":\"1\",");
                        medJson.append("\"period\":1,");
                        medJson.append("\"periodUnit\":\"d\"");
                        medJson.append("}");
                        medJson.append("},");

                        medJson.append("\"additionalInstruction\":[{");
                        medJson.append("\"text\":\"" + aturanPakai + "\"");
                        medJson.append("}]");

                        medJson.append("}],");

                        medJson.append("\"requester\":{");

                        medJson.append("\"agent\":{");
                        medJson.append("\"display\":\"" + dokter + "\",");
                        medJson.append("\"reference\":\"Practitioner/" + pracId + "\"");
                        medJson.append("},");

                        medJson.append("\"onBehalfOf\":{");
                        medJson.append("\"reference\":\"Organization/" + orgId + "\"");
                        medJson.append("}");

                        medJson.append("},");

                        medJson.append("\"meta\":{");
                        medJson.append("\"lastUpdated\":\"" + tglKeluarISO + "\"");
                        medJson.append("}");

                        medJson.append("}");

                        noUrut++;
                    }

                    rsObat.close();
                    psObat.close();

                    medJson.append("]");
                    medJson.append("}");

                    medication = medJson.toString();

                } catch (Exception e) {

                    System.out.println(
                            "Error Medication : " + e
                    );
                }

                String bundle
                        = "{"
                        + "\"resourceType\":\"Bundle\","
                        + "\"id\":\"" + bundleId + "\","
                        + "\"meta\":{"
                        + "\"lastUpdated\":\"" + tglKeluar + "\""
                        + "},"
                        + "\"identifier\":{"
                        + "\"use\":null,"
                        + "\"type\":{"
                        + "\"coding\":[],"
                        + "\"text\":null"
                        + "},"
                        + "\"system\":\"SEP\","
                        + "\"value\":\"" + noSEP + "\","
                        + "\"assigner\":{"
                        + "\"display\":null"
                        + "}"
                        + "},"
                        + "\"type\":\"document\","
                        + "\"entry\":["
                        // ================= COMPOSITION =================
                        + "{"
                        + "\"resource\":" + composition
                        + "},"
                        // ================= ENCOUNTER =================
                        + "{"
                        + "\"resource\":" + encounter
                        + "},"
                        // ================= ORGANIZATION =================
                        + "{"
                        + "\"resource\":" + organization
                        + "},"
                        // ================= PATIENT =================
                        + "{"
                        + "\"resource\":" + patient
                        + "},"
                        // ================= PRACTITIONER =================
                        + "{"
                        + "\"resource\":" + practitioner
                        + "},"
                        + (adaRadiologi ? diagnosticRAD + "," : "")
                        + (!medication.equals("") ? "," + medication : "")
                        // ================= PROCEDURE =================
                        + "{"
                        + "\"resource\":" + procedure.toString()
                        + "},"
                        // ================= CONDITION =================
                        + condition.toString()
                        + "]"
                        + "}";

                // ================= LOG RAW BUNDLE =================
                System.out.println("========== RAW BUNDLE (MINIFIED) ==========");
                System.out.println(bundle);

                // ================= PRETTY PRINT (BIAR KEBACA) =================
                try {
                    org.json.JSONObject json = new org.json.JSONObject(bundle);
                    System.out.println("========== RAW BUNDLE (PRETTY) ==========");
                    System.out.println(json.toString(2));
                } catch (Exception e) {
                    System.out.println("Gagal pretty print JSON: " + e);
                }

                // ================= ENCRYPT =================
                String rawBundle = bundle;

                // STEP 1: GZIP + BASE64
                String compressed = gzipBase64(rawBundle);

                // STEP 2: KEY (WAJIB STRING)
                String key = koneksiDB.CONSIDBPJSEMR()
                        + koneksiDB.SECRETKEYBPJSEMR()
                        + "0204R015";

                // STEP 3: GENERATE AES KEY
                ApiBPJSAesKeySpec aesKey = ApiBPJSEnc.generateKey(key);

                // STEP 4: ENCRYPT
                String encrypted = ApiBPJSEnc.encrypt(compressed, aesKey.getKey(), aesKey.getIv());
//                String encrypted = "TEST";

                // ===== UAT =======                
                if (!validasiDataMR(encrypted)) {
                    continue;
                }

                String request = "{"
                        + "\"request\":{"
                        + "\"noSep\":\"" + noSEP + "\","
                        + "\"jnsPelayanan\":\"" + (kelas.equals("IMP") ? "1" : "2") + "\","
                        + "\"bulan\":\"" + tglMasuk.substring(5, 7) + "\","
                        + "\"tahun\":\"" + tglMasuk.substring(0, 4) + "\","
                        + "\"dataMR\":\"" + encrypted + "\""
                        + "}"
                        + "}";

                String response = apiBPJS.post("/eclaim/rekammedis/insert", request);
                System.out.println(response);

            } catch (Exception e) {
                System.out.println("Error : " + e);
            }
        }
    }//GEN-LAST:event_BtnKirimActionPerformed

    private void ppPilihSemuaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ppPilihSemuaActionPerformed
        for (i = 0; i < tbObat.getRowCount(); i++) {
            tbObat.setValueAt(true, i, 0);
        }
    }//GEN-LAST:event_ppPilihSemuaActionPerformed

    private void ppBersihkanActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ppBersihkanActionPerformed
        for (i = 0; i < tbObat.getRowCount(); i++) {
            tbObat.setValueAt(false, i, 0);
        }
    }//GEN-LAST:event_ppBersihkanActionPerformed

    private void BtnUpdateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnUpdateActionPerformed
        for (i = 0; i < tbObat.getRowCount(); i++) {
            if (tbObat.getValueAt(i, 0).toString().equals("true") && (!tbObat.getValueAt(i, 5).toString().equals("")) && (!tbObat.getValueAt(i, 8).toString().equals("")) && (!tbObat.getValueAt(i, 15).toString().equals(""))) {
                try {
                    iddokter = cekViaSatuSehat.tampilIDParktisi(tbObat.getValueAt(i, 8).toString());
                    idpasien = cekViaSatuSehat.tampilIDPasien(tbObat.getValueAt(i, 5).toString());
                    try {
                        headers = new HttpHeaders();
                        headers.setContentType(MediaType.APPLICATION_JSON);
//                    headers.add("Authorization", "Bearer "+api.TokenSatuSehat());
                        json = "{"
                                + "\"resourceType\": \"Encounter\","
                                + "\"id\": \"" + tbObat.getValueAt(i, 15).toString() + "\","
                                + "\"identifier\": ["
                                + "{"
                                + "\"system\": \"http://sys-ids.kemkes.go.id/encounter/" + koneksiDB.IDSATUSEHAT() + "\","
                                + "\"value\": \"" + tbObat.getValueAt(i, 2).toString() + "\""
                                + "}"
                                + "],"
                                + "\"status\": \"arrived\","
                                + "\"class\": {"
                                + "\"system\": \"http://terminology.hl7.org/CodeSystem/v3-ActCode\","
                                + "\"code\": \"" + (tbObat.getValueAt(i, 11).toString().equals("Ralan") ? "AMB" : "IMP") + "\","
                                + "\"display\": \"" + (tbObat.getValueAt(i, 11).toString().equals("Ralan") ? "ambulatory" : "inpatient encounter") + "\""
                                + "},"
                                + "\"subject\": {"
                                + "\"reference\": \"Patient/" + idpasien + "\","
                                + "\"display\": \"" + tbObat.getValueAt(i, 4).toString() + "\""
                                + "},"
                                + "\"participant\": ["
                                + "{"
                                + "\"type\": ["
                                + "{"
                                + "\"coding\": ["
                                + "{"
                                + "\"system\": \"http://terminology.hl7.org/CodeSystem/v3-ParticipationType\","
                                + "\"code\": \"ATND\","
                                + "\"display\": \"attender\""
                                + "}"
                                + "]"
                                + "}"
                                + "],"
                                + "\"individual\": {"
                                + "\"reference\": \"Practitioner/" + iddokter + "\","
                                + "\"display\": \"" + tbObat.getValueAt(i, 7).toString() + "\""
                                + "}"
                                + "}"
                                + "],"
                                + "\"period\": {"
                                + "\"start\": \"" + tbObat.getValueAt(i, 1).toString() + "\""
                                + "},"
                                + "\"location\": ["
                                + "{"
                                + "\"location\": {"
                                + "\"reference\": \"Location/" + tbObat.getValueAt(i, 11).toString() + "\","
                                + "\"display\": \"" + tbObat.getValueAt(i, 10).toString() + "\""
                                + "}"
                                + "}"
                                + "],"
                                + "\"statusHistory\": ["
                                + "{"
                                + "\"status\": \"arrived\","
                                + "\"period\": {"
                                + "\"start\": \"" + tbObat.getValueAt(i, 1).toString() + "\","
                                + "\"end\": \"" + tbObat.getValueAt(i, 14).toString() + "\""
                                + "}"
                                + "}"
                                + "],"
                                + "\"serviceProvider\": {"
                                + "\"reference\": \"Organization/" + koneksiDB.IDSATUSEHAT() + "\""
                                + "}"
                                + "}";
                        System.out.println("URL : " + link + "/Encounter/" + tbObat.getValueAt(i, 15).toString());
                        System.out.println("Request JSON : " + json);
                        requestEntity = new HttpEntity(json, headers);
                        json = api.getRest().exchange(link + "/Encounter/" + tbObat.getValueAt(i, 15).toString(), HttpMethod.PUT, requestEntity, String.class).getBody();
                        System.out.println("Result JSON : " + json);
                        tbObat.setValueAt(false, i, 0);
                    } catch (Exception e) {
                        System.out.println("Notifikasi Bridging : " + e);
                    }
                } catch (Exception e) {
                    System.out.println("Notifikasi : " + e);
                }
            }
        }
    }//GEN-LAST:event_BtnUpdateActionPerformed

    private void BtnAllActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnAllActionPerformed
        TCari.setText("");
        runBackground(() -> tampil());
    }//GEN-LAST:event_BtnAllActionPerformed

    private void BtnAllKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnAllKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_SPACE) {
            TCari.setText("");
            runBackground(() -> tampil());
        } else {
            Valid.pindah(evt, BtnPrint, BtnKeluar);
        }
    }//GEN-LAST:event_BtnAllKeyPressed

    private void ppPilihBelumTerkirimActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ppPilihBelumTerkirimActionPerformed
        for (int i = 0; i < tbObat.getRowCount(); i++) {
            tbObat.setValueAt(
                    (tbObat.getValueAt(i, 15) == null
                    || tbObat.getValueAt(i, 15).toString().trim().equals("")
                    || tbObat.getValueAt(i, 15).toString().equals("-")),
                    i, 0
            );
        }
    }//GEN-LAST:event_ppPilihBelumTerkirimActionPerformed

    private void formWindowOpened(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowOpened
        if (koneksiDB.CARICEPAT().equals("aktif")) {
            TCari.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
                @Override
                public void insertUpdate(DocumentEvent e) {
                    if (TCari.getText().length() > 2) {
                        runBackground(() -> tampil());
                    }
                }

                @Override
                public void removeUpdate(DocumentEvent e) {
                    if (TCari.getText().length() > 2) {
                        runBackground(() -> tampil());
                    }
                }

                @Override
                public void changedUpdate(DocumentEvent e) {
                    if (TCari.getText().length() > 2) {
                        runBackground(() -> tampil());
                    }
                }
            });
        }
    }//GEN-LAST:event_formWindowOpened

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(() -> {
            SatuSehatKirimEncounter dialog = new SatuSehatKirimEncounter(new javax.swing.JFrame(), true);
            dialog.addWindowListener(new java.awt.event.WindowAdapter() {
                @Override
                public void windowClosing(java.awt.event.WindowEvent e) {
                    System.exit(0);
                }
            });
            dialog.setVisible(true);
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private widget.Button BtnAll;
    private widget.Button BtnCari;
    private widget.Button BtnKeluar;
    private widget.Button BtnKirim;
    private widget.Button BtnPrint;
    private widget.Button BtnUpdate;
    private widget.Tanggal DTPCari1;
    private widget.Tanggal DTPCari2;
    private widget.Label LCount;
    private widget.editorpane LoadHTML;
    private widget.ScrollPane Scroll;
    private widget.TextBox TCari;
    private widget.InternalFrame internalFrame1;
    private widget.Label jLabel15;
    private widget.Label jLabel16;
    private widget.Label jLabel17;
    private widget.Label jLabel7;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPopupMenu jPopupMenu1;
    private widget.panelisi panelGlass8;
    private widget.panelisi panelGlass9;
    private javax.swing.JMenuItem ppBersihkan;
    private javax.swing.JMenuItem ppPilihBelumTerkirim;
    private javax.swing.JMenuItem ppPilihSemua;
    private widget.Table tbObat;
    // End of variables declaration//GEN-END:variables
//    private void tampil() {
//        Valid.tabelKosong(tabMode);
//        try{
//            ps=koneksi.prepareStatement(
//                   "select reg_periksa.tgl_registrasi,reg_periksa.jam_reg,reg_periksa.no_rawat,pasien.nm_pasien,pasien.no_ktp,reg_periksa.no_rkm_medis,reg_periksa.kd_poli,reg_periksa.stts,"+
//                   "pegawai.nama,pegawai.no_ktp as ktpdokter,poliklinik.nm_poli,satu_sehat_mapping_lokasi_ralan.id_lokasi_satusehat,reg_periksa.kd_dokter,reg_periksa.status_lanjut,"+
//                   "concat(reg_periksa.tgl_registrasi,'T',reg_periksa.jam_reg,'+07:00') as pulang,ifnull(satu_sehat_encounter.id_encounter,'') as id_encounter,"+
//                   "case when reg_periksa.status_lanjut='Ralan' then concat(nota_jalan.tanggal,'T',nota_jalan.jam,'+07:00') "+
//                   "when reg_periksa.status_lanjut='Ranap' then concat(nota_inap.tanggal,'T',nota_inap.jam,'+07:00') else null end as billing "+
//                   "from reg_periksa inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis inner join pegawai on pegawai.nik=reg_periksa.kd_dokter "+
//                   "inner join poliklinik on reg_periksa.kd_poli=poliklinik.kd_poli inner join satu_sehat_mapping_lokasi_ralan on satu_sehat_mapping_lokasi_ralan.kd_poli=poliklinik.kd_poli "+
//                   "left join nota_jalan on nota_jalan.no_rawat=reg_periksa.no_rawat "+
//                   "left join nota_inap on nota_inap.no_rawat=reg_periksa.no_rawat "+
//                   "left join satu_sehat_encounter on satu_sehat_encounter.no_rawat=reg_periksa.no_rawat "+
//                   "where reg_periksa.status_bayar='Sudah Bayar' and reg_periksa.tgl_registrasi between ? and ? "+
//                   (TCari.getText().equals("")?"":"and (reg_periksa.no_rawat like ? or reg_periksa.no_rkm_medis like ? or "+
//                   "pasien.nm_pasien like ? or pasien.no_ktp like ? or pegawai.nama like ? or poliklinik.nm_poli like ? or "+
//                   "reg_periksa.stts like ? or reg_periksa.status_lanjut like ?)"));
//            try {
//                ps.setString(1,Valid.SetTgl(DTPCari1.getSelectedItem()+""));
//                ps.setString(2,Valid.SetTgl(DTPCari2.getSelectedItem()+""));
//                if(!TCari.getText().equals("")){
//                    ps.setString(3,"%"+TCari.getText()+"%");
//                    ps.setString(4,"%"+TCari.getText()+"%");
//                    ps.setString(5,"%"+TCari.getText()+"%");
//                    ps.setString(6,"%"+TCari.getText()+"%");
//                    ps.setString(7,"%"+TCari.getText()+"%");
//                    ps.setString(8,"%"+TCari.getText()+"%");
//                    ps.setString(9,"%"+TCari.getText()+"%");
//                    ps.setString(10,"%"+TCari.getText()+"%");
//                }
//                rs=ps.executeQuery();
//                while(rs.next()){
//                    tabMode.addRow(new Object[]{
//                        false,rs.getString("tgl_registrasi")+"T"+rs.getString("jam_reg")+"+07:00",rs.getString("no_rawat"),rs.getString("no_rkm_medis"),rs.getString("nm_pasien"),
//                        rs.getString("no_ktp"),rs.getString("kd_dokter"),rs.getString("nama"),rs.getString("ktpdokter"),rs.getString("kd_poli"),rs.getString("nm_poli"),
//                        rs.getString("id_lokasi_satusehat"),rs.getString("stts"),rs.getString("status_lanjut"),rs.getString("pulang"),rs.getString("id_encounter"),rs.getString("billing")
//                    });
//                }
//            } catch (Exception e) {
//                System.out.println("Notif : "+e);
//            } finally{
//                if(rs!=null){
//                    rs.close();
//                }
//                if(ps!=null){
//                    ps.close();
//                }
//            }
//        }catch(Exception e){
//            System.out.println("Notifikasi : "+e);
//        }
//        LCount.setText(""+tabMode.getRowCount());
//
//    }

    private void tampil() {
        Valid.tabelKosong(tabMode);
        try {
//            ps = koneksi.prepareStatement(
//                    "select "
//                    + "reg_periksa.tgl_registrasi, reg_periksa.jam_reg, reg_periksa.no_rawat, "
//                    + "pasien.no_rkm_medis, pasien.nm_pasien, pasien.no_ktp, "
//                    + "bridging_sep.no_sep, "
//                    + "reg_periksa.kd_dokter, pegawai.nama as nm_dokter, "
//                    + "reg_periksa.kd_poli, poliklinik.nm_poli, "
//                    + "reg_periksa.stts, reg_periksa.status_lanjut, "
//                    + // tgl masuk
//                    "concat(reg_periksa.tgl_registrasi,'T',reg_periksa.jam_reg,'+07:00') as tgl_masuk, "
//                    + // tgl keluar
//                    "case "
//                    + "when reg_periksa.status_lanjut='Ralan' then concat(nota_jalan.tanggal,'T',nota_jalan.jam,'+07:00') "
//                    + "when reg_periksa.status_lanjut='Ranap' then concat(nota_inap.tanggal,'T',nota_inap.jam,'+07:00') "
//                    + "else null end as tgl_keluar, "
//                    + // diagnosa
//                    "diagnosa_pasien.kd_penyakit, penyakit.nm_penyakit, "
//                    + // id encounter BPJS
//                    "ifnull(bpjs_encounter.id_encounter,'') as id_encounter "
//                    + "from reg_periksa "
//                    + // 🔥 WAJIB BPJS
//                    "inner join ( "
//                    + "   select bs.no_rawat, bs.no_sep "
//                    + "   from bridging_sep bs "
//                    + "   where bs.no_sep = ( "
//                    + "       select bs2.no_sep "
//                    + "       from bridging_sep bs2 "
//                    + "       where bs2.no_rawat = bs.no_rawat "
//                    + "       order by "
//                    + "           case when bs2.jnsPelayanan = '1' then 1 else 2 end, "
//                    + "           case when bs2.kdpolitujuan = 'IGD' then 1 else 2 end, "
//                    + "           bs2.tglsep desc "
//                    + "       limit 1 "
//                    + "   ) "
//                    + ") as bridging_sep on bridging_sep.no_rawat = reg_periksa.no_rawat "
//                    + "inner join pasien on reg_periksa.no_rkm_medis = pasien.no_rkm_medis "
//                    + "inner join pegawai on pegawai.nik = reg_periksa.kd_dokter "
//                    + "inner join poliklinik on reg_periksa.kd_poli = poliklinik.kd_poli "
//                    + // ✅ FIX diagnosa (1 saja)
//                    "left join ( "
//                    + "   select no_rawat, kd_penyakit "
//                    + "   from diagnosa_pasien "
//                    + "   where prioritas='1' "
//                    + "   group by no_rawat "
//                    + ") as diagnosa_pasien on diagnosa_pasien.no_rawat = reg_periksa.no_rawat "
//                    + "left join penyakit on penyakit.kd_penyakit = diagnosa_pasien.kd_penyakit "
//                    + // ✅ FIX nota jalan
//                    "left join ( "
//                    + "   select no_rawat, max(tanggal) as tanggal, max(jam) as jam "
//                    + "   from nota_jalan group by no_rawat "
//                    + ") as nota_jalan on nota_jalan.no_rawat = reg_periksa.no_rawat "
//                    + // ✅ FIX nota inap
//                    "left join ( "
//                    + "   select no_rawat, max(tanggal) as tanggal, max(jam) as jam "
//                    + "   from nota_inap group by no_rawat "
//                    + ") as nota_inap on nota_inap.no_rawat = reg_periksa.no_rawat "
//                    + // ✅ FIX encounter (ambil 1 saja)
//                    "left join ( "
//                    + "   select no_rawat, max(id_encounter) as id_encounter "
//                    + "   from bpjs_encounter group by no_rawat "
//                    + ") as bpjs_encounter on bpjs_encounter.no_rawat = reg_periksa.no_rawat "
//                    + "where reg_periksa.status_bayar='Sudah Bayar' "
//                    + "and reg_periksa.tgl_registrasi between ? and ? "
//                    + (TCari.getText().equals("") ? "" : "and (reg_periksa.no_rawat like ? or reg_periksa.no_rkm_medis like ? or "
//                    + "pasien.nm_pasien like ? or pasien.no_ktp like ? or pegawai.nama like ? or poliklinik.nm_poli like ? or "
//                    + "reg_periksa.stts like ? or reg_periksa.status_lanjut like ?)")
//            );

            ps = koneksi.prepareStatement(
                    "select "
                    + "reg_periksa.tgl_registrasi, reg_periksa.jam_reg, reg_periksa.no_rawat, "
                    + "pasien.no_rkm_medis, pasien.nm_pasien, pasien.no_ktp, "
                    // ambil SEP eksternal + internal
                    + "bridging_sep.no_sep, "
                    + "bridging_sep_internal.no_sep as no_sep_internal, "
                    + "reg_periksa.kd_dokter, pegawai.nama as nm_dokter, "
                    + "reg_periksa.kd_poli, poliklinik.nm_poli, "
                    + "reg_periksa.stts, reg_periksa.status_lanjut, "
                    // tgl masuk
                    + "concat(reg_periksa.tgl_registrasi,'T',reg_periksa.jam_reg,'+07:00') as tgl_masuk, "
                    // tgl keluar
                    + "case "
                    + "when reg_periksa.status_lanjut='Ralan' then concat(nota_jalan.tanggal,'T',nota_jalan.jam,'+07:00') "
                    + "when reg_periksa.status_lanjut='Ranap' then concat(nota_inap.tanggal,'T',nota_inap.jam,'+07:00') "
                    + "else null end as tgl_keluar, "
                    // diagnosa
                    + "diagnosa_pasien.kd_penyakit, penyakit.nm_penyakit, "
                    // id encounter BPJS
                    + "ifnull(bpjs_encounter.id_encounter,'') as id_encounter "
                    + "from reg_periksa "
                    // 🔥 bridging_sep
                    + "left join ( "
                    + "   select bs.no_rawat, bs.no_sep "
                    + "   from bridging_sep bs "
                    + "   where bs.no_sep = ( "
                    + "       select bs2.no_sep "
                    + "       from bridging_sep bs2 "
                    + "       where bs2.no_rawat = bs.no_rawat "
                    + "       order by "
                    + "           case when bs2.jnsPelayanan = '1' then 1 else 2 end, "
                    + "           case when bs2.kdpolitujuan = 'IGD' then 1 else 2 end, "
                    + "           bs2.tglsep desc "
                    + "       limit 1 "
                    + "   ) "
                    + ") as bridging_sep on bridging_sep.no_rawat = reg_periksa.no_rawat "
                    // 🔥 bridging_sep_internal
                    + "left join ( "
                    + "   select bsi.no_rawat, bsi.no_sep "
                    + "   from bridging_sep_internal bsi "
                    + "   where bsi.no_sep = ( "
                    + "       select bsi2.no_sep "
                    + "       from bridging_sep_internal bsi2 "
                    + "       where bsi2.no_rawat = bsi.no_rawat "
                    + "       order by "
                    + "           case when bsi2.jnsPelayanan = '1' then 1 else 2 end, "
                    + "           bsi2.tglsep desc "
                    + "       limit 1 "
                    + "   ) "
                    + ") as bridging_sep_internal "
                    + "on bridging_sep_internal.no_rawat = reg_periksa.no_rawat "
                    + "inner join pasien on reg_periksa.no_rkm_medis = pasien.no_rkm_medis "
                    + "inner join pegawai on pegawai.nik = reg_periksa.kd_dokter "
                    + "inner join poliklinik on reg_periksa.kd_poli = poliklinik.kd_poli "
                    // ✅ diagnosa
                    + "left join ( "
                    + "   select no_rawat, kd_penyakit "
                    + "   from diagnosa_pasien "
                    + "   where prioritas='1' "
                    + "   group by no_rawat "
                    + ") as diagnosa_pasien on diagnosa_pasien.no_rawat = reg_periksa.no_rawat "
                    + "left join penyakit on penyakit.kd_penyakit = diagnosa_pasien.kd_penyakit "
                    // ✅ nota jalan
                    + "left join ( "
                    + "   select no_rawat, max(tanggal) as tanggal, max(jam) as jam "
                    + "   from nota_jalan group by no_rawat "
                    + ") as nota_jalan on nota_jalan.no_rawat = reg_periksa.no_rawat "
                    // ✅ nota inap
                    + "left join ( "
                    + "   select no_rawat, max(tanggal) as tanggal, max(jam) as jam "
                    + "   from nota_inap group by no_rawat "
                    + ") as nota_inap on nota_inap.no_rawat = reg_periksa.no_rawat "
                    // ✅ encounter
                    + "left join ( "
                    + "   select no_rawat, max(id_encounter) as id_encounter "
                    + "   from bpjs_encounter group by no_rawat "
                    + ") as bpjs_encounter on bpjs_encounter.no_rawat = reg_periksa.no_rawat "
                    + "where reg_periksa.status_bayar='Sudah Bayar' "
                    + "and reg_periksa.tgl_registrasi between ? and ? "
                    + "and ( "
                    + "    bridging_sep.no_sep is not null "
                    + "    or bridging_sep_internal.no_sep is not null "
                    + ") "
                    + (TCari.getText().equals("") ? "" : "and (reg_periksa.no_rawat like ? or reg_periksa.no_rkm_medis like ? or "
                    + "pasien.nm_pasien like ? or pasien.no_ktp like ? or pegawai.nama like ? or poliklinik.nm_poli like ? or "
                    + "reg_periksa.stts like ? or reg_periksa.status_lanjut like ?)")
            );

            try {
                ps.setString(1, Valid.SetTgl(DTPCari1.getSelectedItem() + ""));
                ps.setString(2, Valid.SetTgl(DTPCari2.getSelectedItem() + ""));

                if (!TCari.getText().equals("")) {
                    ps.setString(3, "%" + TCari.getText() + "%");
                    ps.setString(4, "%" + TCari.getText() + "%");
                    ps.setString(5, "%" + TCari.getText() + "%");
                    ps.setString(6, "%" + TCari.getText() + "%");
                    ps.setString(7, "%" + TCari.getText() + "%");
                    ps.setString(8, "%" + TCari.getText() + "%");
                    ps.setString(9, "%" + TCari.getText() + "%");
                    ps.setString(10, "%" + TCari.getText() + "%");
                }

                rs = ps.executeQuery();
                while (rs.next()) {

                    String noSep = rs.getString("no_sep");

                    if (noSep == null || noSep.trim().isEmpty()) {
                        noSep = rs.getString("no_sep_internal");
                    }

                    if (noSep == null) {
                        noSep = "";
                    }

                    tabMode.addRow(new Object[]{
                        false,
                        rs.getString("tgl_registrasi") + "T" + rs.getString("jam_reg") + "+07:00",
                        rs.getString("no_rawat"),
                        rs.getString("no_rkm_medis"),
                        rs.getString("nm_pasien"),
                        noSep,
                        rs.getString("kd_dokter"),
                        rs.getString("nm_dokter"),
                        rs.getString("tgl_masuk"),
                        rs.getString("tgl_keluar"),
                        rs.getString("kd_poli"),
                        rs.getString("nm_poli"),
                        // diagnosa
                        (rs.getString("kd_penyakit") != null ? rs.getString("kd_penyakit") : "") + " - "
                        + (rs.getString("nm_penyakit") != null ? rs.getString("nm_penyakit") : ""),
                        rs.getString("stts"),
                        rs.getString("status_lanjut"),
                        rs.getString("tgl_masuk"),
                        rs.getString("id_encounter")
                    });
                }

            } catch (Exception e) {
                System.out.println("Notif : " + e);
            } finally {
                if (rs != null) {
                    rs.close();
                }
                if (ps != null) {
                    ps.close();
                }
            }

        } catch (Exception e) {
            System.out.println("Notifikasi : " + e);
        }

        LCount.setText("" + tabMode.getRowCount());
    }

    public void isCek() {
        BtnKirim.setEnabled(akses.getBpjs_kirim_encounter());
        BtnUpdate.setEnabled(akses.getsatu_sehat_kirim_encounter());
        BtnPrint.setEnabled(akses.getsatu_sehat_kirim_encounter());
    }

    public JTable getTable() {
        return tbObat;
    }

    private void runBackground(Runnable task) {
        if (ceksukses) {
            return;
        }
        if (executor.isShutdown() || executor.isTerminated()) {
            return;
        }
        if (!isDisplayable()) {
            return;
        }

        ceksukses = true;
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

        try {
            executor.submit(() -> {
                try {
                    task.run();
                } finally {
                    ceksukses = false;
                    SwingUtilities.invokeLater(() -> {
                        if (isDisplayable()) {
                            setCursor(Cursor.getDefaultCursor());
                        }
                    });
                }
            });
        } catch (RejectedExecutionException ex) {
            ceksukses = false;
        }
    }

    @Override
    public void dispose() {
        executor.shutdownNow();
        super.dispose();
    }
}
