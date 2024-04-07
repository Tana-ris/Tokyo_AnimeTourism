package com.example.tokyo2;

public class MarkerInfo {
    private final String description;
    private final int imageResId; // マーカーごとの画像リソース ID を保持するフィールド
    private final String additionalInfo;

    public MarkerInfo(String description, int imageResId, String additionalInfo) {
        this.description = description;
        this.imageResId = imageResId;
        this.additionalInfo = additionalInfo;
    }

    public String getDescription() {
        return description;
    }

    public int getImageResId() {
        return imageResId;
    }

    public String getAdditionalInfo() {
        return additionalInfo;
    }
}

