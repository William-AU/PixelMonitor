package solo.pixelmonitor.ui.pictureInPictureScene.transformations;

public enum ImageTransformationType {
    GRAYSCALE("Grayscale");

    private final String displayName;

    ImageTransformationType(String s) {
        displayName = s;
    }

    @Override
    public String toString() {
        return displayName;
    }

    public static ImageTransformationType fromDisplayName(String displayName) {
        for (ImageTransformationType type : values()) {
            if (type.displayName.equals(displayName)) {
                return type;
            }
        }
        throw new IllegalArgumentException("No enum constant with display name: " + displayName);
    }
}
