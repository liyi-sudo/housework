package com.ecommerce.controller;

import com.ecommerce.entity.Goods;
import com.ecommerce.service.GoodsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/api/goods")
@RequiredArgsConstructor
public class ImageController {

    private final GoodsService goodsService;

    @GetMapping(value = "/{id}/image", produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<byte[]> image(@PathVariable Long id,
                                        @RequestParam(defaultValue = "600") int w,
                                        @RequestParam(defaultValue = "600") int h) {
        String text = "商品 " + id;
        try {
            Goods g = goodsService.detail(id);
            if (g != null && g.getName() != null) {
                text = g.getName();
            }
        } catch (Exception ignored) {
        }
        byte[] bytes = buildImage(text, normalize(w), normalize(h));
        return ResponseEntity.ok()
                .header("Cache-Control", "public, max-age=86400")
                .contentType(MediaType.IMAGE_PNG)
                .body(bytes);
    }

    @GetMapping(value = "/image", produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<byte[]> textImage(@RequestParam String text,
                                            @RequestParam(defaultValue = "600") int w,
                                            @RequestParam(defaultValue = "600") int h) throws Exception {
        String t = URLDecoder.decode(text, StandardCharsets.UTF_8.name());
        byte[] bytes = buildImage(t, normalize(w), normalize(h));
        return ResponseEntity.ok()
                .header("Cache-Control", "public, max-age=86400")
                .contentType(MediaType.IMAGE_PNG)
                .body(bytes);
    }

    private int normalize(int v) {
        if (v < 10) return 10;
        if (v > 2000) return 2000;
        return v;
    }

    private byte[] buildImage(String text, int width, int height) {
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = img.createGraphics();
        try {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2.setColor(new Color(245, 245, 245));
            g2.fillRect(0, 0, width, height);
            g2.setColor(new Color(0x9e, 0x9e, 0x9e));
            g2.drawRect(1, 1, width - 2, height - 2);
            int fontSize = Math.max(20, Math.min(width, height) / 8);
            g2.setFont(new Font(Font.SANS_SERIF, Font.BOLD, fontSize));
            FontMetrics fm = g2.getFontMetrics();
            int textWidth = fm.stringWidth(text);
            int textX = (width - textWidth) / 2;
            int textY = (height - fm.getHeight()) / 2 + fm.getAscent();
            g2.setColor(new Color(0x42, 0x42, 0x42));
            g2.drawString(text, Math.max(textX, 5), Math.max(textY, 5));
        } finally {
            g2.dispose();
        }
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ImageIO.write(img, "png", out);
            return out.toByteArray();
        } catch (Exception e) {
            return new byte[0];
        }
    }
}
