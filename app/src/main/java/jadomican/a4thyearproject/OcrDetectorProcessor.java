package jadomican.a4thyearproject;

/**
 * Jason Domican
 * Final Year Project
 * Institute of Technology Tallaght
 *
 * This class is a simple Processor which gets detected TextBlocks and adds them to the overlay
 * as OcrGraphics. Adapted from Google Mobile Vision tutorials available at:
 * https://codelabs.developers.google.com/codelabs/mobile-vision-ocr/
 */

import android.util.Log;
import android.util.SparseArray;

import jadomican.a4thyearproject.camera.GraphicOverlay;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.text.TextBlock;

public class OcrDetectorProcessor implements Detector.Processor<TextBlock> {

    private GraphicOverlay<OcrGraphic> mGraphicOverlay;

    OcrDetectorProcessor(GraphicOverlay<OcrGraphic> ocrGraphicOverlay) {
        mGraphicOverlay = ocrGraphicOverlay;
    }

    /**
     * Called by the detector to process detection results and add them to the graphic overlay.
     */
    @Override
    public void receiveDetections(Detector.Detections<TextBlock> detections) {
        mGraphicOverlay.clear();
        SparseArray<TextBlock> items = detections.getDetectedItems();
        for (int i = 0; i < items.size(); ++i) {
            TextBlock item = items.valueAt(i);
            if (item != null && item.getValue() != null) {
                Log.d("OcrDetectorProcessor", "Text detected! " + item.getValue());
            }
            OcrGraphic graphic = new OcrGraphic(mGraphicOverlay, item);
            mGraphicOverlay.add(graphic);
        }
    }

    /**
     * Frees the resources associated with this detection processor.
     */
    @Override
    public void release() {
        mGraphicOverlay.clear();

    }
}