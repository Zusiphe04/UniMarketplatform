package com.example.unimarket.factory;

import com.example.unimarket.domain.ProductImage;
import com.example.unimarket.util.Helper;

import java.net.URI;
import java.util.UUID;

public final class ProductImageFactory {
    private ProductImageFactory() { }

    public static ProductImage create(UUID productId, String imageUrl, String altText,
                                      int displayOrder, boolean primary) {
        String url = Helper.cleanText(imageUrl);
        String alt = Helper.cleanText(altText);
        if (productId == null || !validUrl(url) || url.length() > 1000
                || alt != null && alt.length() > 200 || displayOrder < 0 || displayOrder > 7) return null;
        return new ProductImage.Builder().setId(Helper.generateId()).setProductId(productId)
                .setImageUrl(url).setAltText(alt).setDisplayOrder(displayOrder)
                .setPrimaryImage(primary).build();
    }

    private static boolean validUrl(String value) {
        if (value == null) return false;
        try {
            URI uri = URI.create(value);
            return uri.getHost() != null && ("https".equalsIgnoreCase(uri.getScheme())
                    || "http".equalsIgnoreCase(uri.getScheme()));
        } catch (IllegalArgumentException exception) {
            return false;
        }
    }
}
