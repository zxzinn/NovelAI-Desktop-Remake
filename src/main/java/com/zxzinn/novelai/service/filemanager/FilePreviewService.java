package com.zxzinn.novelai.service.filemanager;

import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.web.WebView;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.io.FilenameUtils;
import org.apache.tika.Tika;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

@Log4j2
public class FilePreviewService {
    private final Tika tika;
    private final WebView webView;

    public FilePreviewService() {
        this.tika = new Tika();
        this.webView = new WebView();
    }

    public Node getPreview(File file) {
        if (file.isFile()) {
            try {
                String mimeType = tika.detect(file);
                if (mimeType.startsWith("image/")) {
                    return createImagePreview(file);
                } else if (mimeType.startsWith("text/") || mimeType.equals("application/pdf")) {
                    return createTextPreview(file);
                } else {
                    return new Label("不支援的文件格式：" + mimeType);
                }
            } catch (Exception e) {
                log.error("無法載入預覽", e);
                return new Label("無法載入預覽：" + e.getMessage());
            }
        }
        return new Label("請選擇一個文件");
    }

    private ImageView createImagePreview(File file) {
        Image image = new Image(file.toURI().toString());
        ImageView imageView = new ImageView(image);
        imageView.setPreserveRatio(true);
        return imageView;
    }

    private WebView createTextPreview(File file) throws IOException {
        String content = Files.readString(file.toPath());
        String extension = FilenameUtils.getExtension(file.getName());
        String mimeType = tika.detect(file);
        webView.getEngine().loadContent(formatContent(content, extension, mimeType), "text/html");
        return webView;
    }

    private String formatContent(String content, String extension, String mimeType) {
        String highlightLanguage = getHighlightLanguage(extension, mimeType);
        String escapedContent = escapeHtml(content);

        return """
            <!DOCTYPE html>
            <html>
            <head>
                <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/highlight.js/11.8.0/styles/default.min.css">
                <script src="https://cdnjs.cloudflare.com/ajax/libs/highlight.js/11.8.0/highlight.min.js"></script>
                <style>
                    body {
                        font-family: Arial, sans-serif;
                        line-height: 1.6;
                        padding: 20px;
                        background-color: #f0f0f0;
                    }
                    pre {
                        background-color: #ffffff;
                        border: 1px solid #ddd;
                        border-radius: 4px;
                        padding: 16px;
                        font-size: 14px;
                    }
                    code {
                        font-family: 'Courier New', Courier, monospace;
                    }
                </style>
            </head>
            <body>
                <pre><code class="%s">%s</code></pre>
                <script>hljs.highlightAll();</script>
            </body>
            </html>
            """.formatted(highlightLanguage, escapedContent);
    }

    private String getHighlightLanguage(String extension, String mimeType) {
        return switch (extension.toLowerCase()) {
            case "java" -> "java";
            case "py" -> "python";
            case "js" -> "javascript";
            case "html" -> "html";
            case "css" -> "css";
            case "json" -> "json";
            case "xml" -> "xml";
            default -> mimeType.startsWith("text/") ? "plaintext" : "";
        };
    }

    private String escapeHtml(String content) {
        return content.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}