package com.carvaluation.valuation;

/** Only browser-facing photo fields; never serialize JPA relationships or user credentials. */
public record PhotoView(Long id, PhotoType photoType, String photoUrl, String damageReportJson) {

    public static PhotoView from(CarPhoto photo) {
        return new PhotoView(photo.getId(), photo.getPhotoType(),
                "/valuation/photos/" + photo.getId() + "/image", photo.getAiAnalysisJson());
    }
}
