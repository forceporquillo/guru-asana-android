/*
 * Copyright 2020 Google LLC. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev.forcecodes.guruasana.poseprocessor;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.Task;
import com.google.android.odml.image.MlImage;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.pose.Pose;
import com.google.mlkit.vision.pose.PoseDetection;
import com.google.mlkit.vision.pose.PoseDetector;
import com.google.mlkit.vision.pose.PoseDetectorOptionsBase;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import dev.forcecodes.guruasana.poseprocessor.camera.GraphicOverlay;
import dev.forcecodes.guruasana.poseprocessor.classification.PoseClassifierProcessor;

/** A processor to run pose detector. */
public class PoseDetectorProcessor
    extends VisionProcessorBase<PoseDetectorProcessor.PoseWithClassification> {

  private static final String TAG = "PoseDetectorProcessor";

  private final PoseDetector detector;
  private final PoseClassifierProcessor.PoseDifficultyLevel difficultyLevel;

  private ConfidencePoseGraphicCallback confidencePoseGraphicCallback;

  private final boolean showInFrameLikelihood;
  private final boolean visualize3D;
  private final boolean rescale3DVisualization;
  private final boolean runClassification;
  private final boolean isStreamMode;
  private final Context context;
  private final Executor classificationExecutor;

  private PoseClassifierProcessor poseClassifierProcessor;
  /** Internal class to hold Pose and classification results. */
  protected static class PoseWithClassification {
    private final Pose pose;
    private final List<PoseClassifierProcessor.PoseResult> classificationResult;

    public PoseWithClassification(Pose pose, List<PoseClassifierProcessor.PoseResult> classificationResult) {
      this.pose = pose;
      this.classificationResult = classificationResult;
    }

    public Pose getPose() {
      return pose;
    }

    public List<PoseClassifierProcessor.PoseResult> getClassificationResult() {
      return classificationResult;
    }
  }

  public PoseDetectorProcessor(
          Context context,
          PoseDetectorOptionsBase options,
          PoseClassifierProcessor.PoseDifficultyLevel difficultyLevel,
          boolean isStreamMode
  ) {
    this(context, options, difficultyLevel,
            true, true,
            true,
            true,
            isStreamMode
    );
  }

  public PoseDetectorProcessor(
          Context context,
          PoseDetectorOptionsBase options,
          PoseClassifierProcessor.PoseDifficultyLevel difficultyLevel,
          boolean isStreamMode,
          boolean runClassification,
          InferenceInfoGraphicCallback inferenceInfoGraphicCallback,
          ConfidencePoseGraphicCallback confidencePoseGraphicCallback
  ) {
    this(context, options, difficultyLevel,
            true, true,
            true,
            runClassification,
            isStreamMode,
            inferenceInfoGraphicCallback,
            confidencePoseGraphicCallback
    );
  }

  public PoseDetectorProcessor(
          Context context,
          PoseDetectorOptionsBase options,
          PoseClassifierProcessor.PoseDifficultyLevel difficultyLevel,
          boolean showInFrameLikelihood,
          boolean visualizeZ,
          boolean rescale3DVisualization,
          boolean runClassification,
          boolean isStreamMode
  ) {
    this(context, options, difficultyLevel, showInFrameLikelihood, visualizeZ,
            rescale3DVisualization, runClassification, isStreamMode,
            null, null);
  }

  public PoseDetectorProcessor(
      Context context,
      PoseDetectorOptionsBase options,
      PoseClassifierProcessor.PoseDifficultyLevel difficultyLevel,
      boolean showInFrameLikelihood,
      boolean visualize3D,
      boolean rescaleZForVisualization,
      boolean runClassification,
      boolean isStreamMode,
      InferenceInfoGraphicCallback inferenceInfoGraphicCallback,
      ConfidencePoseGraphicCallback confidencePoseGraphicCallback
  ) {
    super(context, inferenceInfoGraphicCallback);
    this.showInFrameLikelihood = showInFrameLikelihood;
    this.difficultyLevel = difficultyLevel;
    this.visualize3D = visualize3D;
    this.rescale3DVisualization = rescaleZForVisualization;
    this.detector = PoseDetection.getClient(options);
    this.runClassification = runClassification;
    this.isStreamMode = isStreamMode;
    this.context = context;

    // for process Tasks execution
    classificationExecutor = Executors.newSingleThreadExecutor();

    if (context instanceof ConfidencePoseGraphicCallback && confidencePoseGraphicCallback == null) {
      this.confidencePoseGraphicCallback = (ConfidencePoseGraphicCallback) context;
    }
    if (confidencePoseGraphicCallback != null) {
      this.confidencePoseGraphicCallback = confidencePoseGraphicCallback;
    }

    Log.d(TAG, "Encoder           : " + options.encode());
    Log.d(TAG, "Run Configuration : " + options.getRunConfigName());
    Log.d(TAG, "Hardware Configs  : " + Arrays.toString(options.getPreferredHardwareConfigs()));
    Log.d(TAG, "Executor          : " + options.getExecutor());

  }

  @Override
  public void stop() {
    super.stop();
    detector.close();
  }

  @Override
  protected Task<PoseWithClassification> detectInImage(InputImage image) {
    return detector
        .process(image)
        .continueWith(
            classificationExecutor,
            task -> {
              Pose pose = task.getResult();
              List<PoseClassifierProcessor.PoseResult> classificationResult = new ArrayList<>();
              if (runClassification) {
                if (poseClassifierProcessor == null) {
                  poseClassifierProcessor = new PoseClassifierProcessor(context, isStreamMode, difficultyLevel);
                }
                classificationResult = poseClassifierProcessor.getPoseResult(pose);
              }
              return new PoseWithClassification(pose, classificationResult);
            });
  }

  @Override
  protected Task<PoseWithClassification> detectInImage(MlImage image) {
    return detector
        .process(image)
        .continueWith(
            classificationExecutor,
            task -> {
              Pose pose = task.getResult();
              List<PoseClassifierProcessor.PoseResult> classificationResult = new ArrayList<>();
              if (runClassification) {
                if (poseClassifierProcessor == null) {
                  poseClassifierProcessor = new PoseClassifierProcessor(context, isStreamMode, difficultyLevel);
                }
                classificationResult = poseClassifierProcessor.getPoseResult(pose);
              }
              return new PoseWithClassification(pose, classificationResult);
            });
  }

  @Override
  protected void onSuccess(
      @NonNull PoseWithClassification poseWithClassification,
      @NonNull GraphicOverlay graphicOverlay) {
    confidencePoseGraphicCallback.onGetPoseClassificationChangeListener(poseWithClassification.getClassificationResult());
    if (runClassification) {
      graphicOverlay.add(
              new PoseGraphic(
                      graphicOverlay,
                      poseWithClassification.pose,
                      showInFrameLikelihood,
                      visualize3D,
                      rescale3DVisualization));
    }
  }

  @Override
  protected void onFailure(@NonNull Exception e) {
    Log.e(TAG, "Pose detection failed!", e);
  }

  @Override
  protected boolean isMlImageEnabled(Context context) {
    // Use MlImage in Pose Detection by default, change it to OFF to switch to InputImage.
    return true;
  }
}
