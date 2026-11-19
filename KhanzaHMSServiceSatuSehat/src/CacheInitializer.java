/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

/**
 *
 * @author USER
 */
import java.io.File;
import java.io.IOException;

public class CacheInitializer {
    public static void main(String[] args) {
        // Folder cache
        File cacheDir = new File("cache");

        // Buat folder jika belum ada
        if (!cacheDir.exists()) {
            boolean created = cacheDir.mkdir();
            if (created) {
                System.out.println("Folder 'cache' berhasil dibuat!");
            } else {
                System.out.println("Gagal membuat folder 'cache'.");
            }
        }

        // Daftar file yang dibutuhkan
        String[] files = { "propinsi.iyem", "kabupaten.iyem", "kecamatan.iyem", "kelurahan.iyem" };

        for (String fileName : files) {
            File file = new File(cacheDir, fileName);
            if (!file.exists()) {
                try {
                    boolean createdFile = file.createNewFile();
                    if (createdFile) {
                        System.out.println(fileName + " berhasil dibuat!");
                    } else {
                        System.out.println("Gagal membuat " + fileName);
                    }
                } catch (IOException e) {
                    System.err.println("Error membuat " + fileName + ": " + e.getMessage());
                }
            }
        }
    }
}

