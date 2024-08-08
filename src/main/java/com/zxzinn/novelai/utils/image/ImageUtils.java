package com.zxzinn.novelai.utils.image;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import lombok.extern.log4j.Log4j2;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.Base64;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Log4j2
public class ImageUtils {
    public static Image base64ToImage(String base64Data) throws IOException {
        byte[] imageData = Base64.getDecoder().decode(base64Data);
        return byteArrayToImage(imageData);
    }

    public String fileToBase64(File file) throws IOException {
        byte[] fileContent = new byte[(int) file.length()];
        try (FileInputStream inputStream = new FileInputStream(file)) {
            inputStream.read(fileContent);
        }
        return Base64.getEncoder().encodeToString(fileContent);
    }

    public static byte[] extractImageFromZip(byte[] zipData) throws IOException {
        try (ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(zipData))) {
            ZipEntry entry = zis.getNextEntry();
            if (entry == null) {
                throw new IOException("Zip 文件是空的");
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int len;
            while ((len = zis.read(buffer)) > 0) {
                baos.write(buffer, 0, len);
            }
            return baos.toByteArray();
        }
    }

    public static Image byteArrayToImage(byte[] imageData) throws IOException {
        ByteArrayInputStream bis = new ByteArrayInputStream(imageData);
        BufferedImage bufferedImage = ImageIO.read(bis);
        if (bufferedImage == null) {
            throw new IOException("無法從字節數組創建圖像");
        }
        return SwingFXUtils.toFXImage(bufferedImage, null);
    }

    public static void saveImage(Image image, String fileName) throws IOException {
        if (image == null) {
            log.error("無法保存圖像：圖像對象為null");
            return;
        }

        File outputDir = new File("output");
        if (!outputDir.exists() && !outputDir.mkdir()) {
            log.error("無法創建輸出目錄");
            return;
        }

        File outputFile = new File(outputDir, fileName);
        BufferedImage bufferedImage = SwingFXUtils.fromFXImage(image, null);

        if (bufferedImage == null) {
            log.error("無法將JavaFX圖像轉換為BufferedImage");
            return;
        }

        if (!ImageIO.write(bufferedImage, "png", outputFile)) {
            log.error("沒有合適的寫入器來保存PNG圖像");
        }
    }


}