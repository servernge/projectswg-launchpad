/*
 * 
 * This file is part of ProjectSWG Launchpad.
 *
 * ProjectSWG Launchpad is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * ProjectSWG Launchpad is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with ProjectSWG Launchpad.  If not, see <http://www.gnu.org/licenses/>.      
 *
 */

package com.projectswg.launchpad.controller;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.ArrayList;

import com.projectswg.launchpad.ProjectSWG;
import com.projectswg.launchpad.extras.TREFix;
import com.projectswg.launchpad.service.Manager;
import com.projectswg.launchpad.model.Resource;

import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.ParallelTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.util.Pair;

public class MainController implements FxmlController
{
	@FXML
	private Button launcherSettingsButton, extrasButton, gameSettingsButton;
	
	@FXML
	private Button updateButton, setupButton, cancelButton, playButton, scanButton;
	
	@FXML
	private ProgressIndicator progressIndicator;
	
	@FXML
	private ProgressBar progressBar;
	
	@FXML
	private StackPane root;
	
	@FXML
	private VBox mainRoot;
	
	@FXML
	private Pane mainDisplayPane, gameProcessPane;
	
	@FXML
	private ImageView mainGraphic;
	
	public static final double SLIDE_DURATION = 300;
	public static final double FADE_DURATION = 250;
	
	public static final int ANIMATION_NONE = 0;
	public static final int ANIMATION_LOW = 1;
	public static final int ANIMATION_HIGH = 2;
	
	public static final String CHECKMARK = "\u2713";
	public static final String XMARK = "\u2717";
	public static final String PENCIL_ICON = "\u270e";
	public static final String MAGNIFYING_GLASS = "\ud83d\udd0d";
	public static final String UP_ARROW = "\u21e1";
	public static final String DOWN_ARROW = "\u21e3";
	public static final String WHITE_CIRCLE = "\u25cb";
	public static final String BLACK_CIRCLE = "\u25cf";
	public static final String SPEAKER = "\ud83d\udd0a";
	
	// observables
	private SimpleIntegerProperty animationLevel;
	private ObservableList<GameController> games;
	
	// animation
	private GaussianBlur blur;
	
	private ProjectSWG pswg;
	private Stage stage;
	private Manager manager;
	private ModalController modalController;
	private SettingsController settingsComponent;
	private ExtrasController extrasComponent;
	private SetupController setupComponent;
	private NodeDisplay mainDisplay;
	private GameDisplay gameDisplay;
	private Timeline showDownloadLow, hideDownloadLow;
	private ParallelTransition showDownloadHigh, hideDownloadHigh;
	
	
	public MainController()
	{
		animationLevel = new SimpleIntegerProperty();
		blur = new GaussianBlur();
	}
	
	@Override
	public void initialize(URL arg0, ResourceBundle arg1)
	{
		
	}
	
	public void init(ProjectSWG pswg)
	{
		this.pswg = pswg;
		
		manager = pswg.getManager();
		
		// move to css
		progressIndicator.setMaxSize(36, 36);
		
		// Game process display
		gameDisplay = new GameDisplay(this);
		
		// main text display
		mainDisplay = new NodeDisplay(mainDisplayPane);

		modalController = (ModalController)pswg.getControllers().get("modal");
		settingsComponent = (SettingsController)pswg.getControllers().get("settings");
		setupComponent = (SetupController)pswg.getControllers().get("setup");
		extrasComponent = (ExtrasController)pswg.getControllers().get("extras");
		
		modalController.getRoot().opacityProperty().addListener((observable, oldValue, newValue) -> {
			blur.setRadius(newValue.doubleValue() * 10);
		});
		
		modalController.init(this);
		modalController.addComponent(settingsComponent);
		modalController.addComponent(setupComponent);
		modalController.addComponent(extrasComponent);
		
		root.getChildren().add(modalController.getRoot());

		settingsComponent.init(this);
		setupComponent.init(this);
		extrasComponent.init(this);
		
		animationLevel.set(ProjectSWG.PREFS.getInt("animation", 2));
		
		// add listeners
		
		animationLevel.addListener((observable, oldValue, newValue) -> {
			ProjectSWG.PREFS.putInt("animation", newValue.intValue());
		});
		
		addButtonListeners();
		
		manager.getMainOut().addListener((observable, oldValue, newValue) -> {
			mainDisplay.queueString(newValue);
		});
		
		manager.getSwgReady().addListener((observable, oldValue, newValue) -> {
			
			ProjectSWG.log(String.format("swgReady changed: %s -> %s", oldValue, newValue));
			if (manager.getPswgReady().getValue()) {
				playButton.setVisible(true);
				updateButton.setVisible(false);
				setupButton.setVisible(false);
				
				gameSettingsButton.setDisable(false);
				extrasButton.setDisable(false);
			}
		});
		
		manager.getSwgScanService().runningProperty().addListener((observable, oldValue, newValue) -> {
			if (newValue) {
				ProjectSWG.log("SWG scan started");
				
				launcherSettingsButton.setDisable(true);
				gameSettingsButton.setDisable(true);
				extrasButton.setDisable(true);
		
				setupButton.setVisible(false);
				progressIndicator.setVisible(true);
				
			} else {
				launcherSettingsButton.setDisable(false);
				gameSettingsButton.setDisable(false);
				extrasButton.setDisable(false);
				
				boolean result = manager.getSwgScanService().getValue();
				cancelButton.setVisible(false);
				progressIndicator.setVisible(false);
				
				if (result) {
				
				} else {
					setupButton.setVisible(true);
					playButton.setDisable(true);
				}
			}
		});
		
		manager.getPswgScanService().runningProperty().addListener((observable, oldValue, newValue) -> {
			if (newValue) {
				ProjectSWG.log("PSWG scan started");
				
				launcherSettingsButton.setDisable(true);
				gameSettingsButton.setDisable(true);
				extrasButton.setDisable(true);
				
				playButton.setDisable(true);
				scanButton.setVisible(false);
				setupButton.setVisible(false);
				cancelButton.setVisible(true);
				progressIndicator.setVisible(true);
				
			} else {
				launcherSettingsButton.setDisable(false);
				cancelButton.setVisible(false);
				progressIndicator.setVisible(false);
				
				switch (manager.getPswgScanService().getState()) {
				case CANCELLED:
					ProjectSWG.log("PSWG scan cancelled");
					scanButton.setVisible(true);
					break;
					
				case SUCCEEDED:
					ProjectSWG.log("PSWG scan succeeded");
					Pair<Double, ArrayList<Resource>> result = manager.getPswgScanService().getValue();
					if (result == null) {
						scanButton.setVisible(true);
						return;
					}
					
					final double dlTotal = manager.getPswgScanService().getValue().getKey();
					if (dlTotal > 0) {
						updateButton.setVisible(true);
					} else {
						gameSettingsButton.setDisable(false);
						extrasButton.setDisable(false);

						playButton.setDisable(false);
						scanButton.setVisible(true);
						mainDisplay.queueString("Ready");
					}
					break;
				default:
				}
			}
		});
		
		manager.getUpdateService().runningProperty().addListener((observable, oldValue, newValue) -> {
			if (newValue) {
				launcherSettingsButton.setDisable(true);
				updateButton.setVisible(false);
				cancelButton.setVisible(true);
				progressIndicator.setVisible(true);
			} else {
				launcherSettingsButton.setDisable(false);
				switch (manager.getUpdateService().getState()) {
				case FAILED:
					cancelButton.setVisible(false);
					progressIndicator.setVisible(false);
					scanButton.setVisible(true);
					break;
				
				case CANCELLED:
					cancelButton.setVisible(false);
					progressIndicator.setVisible(false);
					scanButton.setVisible(true);
					break;
					
				case SUCCEEDED:
					gameSettingsButton.setDisable(false);
					extrasButton.setDisable(false);
					cancelButton.setVisible(false);
					progressIndicator.setVisible(false);
					scanButton.setVisible(true);
					if (manager.getUpdateService().getValue()) {
						playButton.setDisable(false);
						playButton.setVisible(true);
					}
					break;
				default:
				}
			}
		});
		
		manager.getUpdateService().progressProperty().addListener((observable, oldValue, newValue) -> {
			if (oldValue.intValue() == -1)
				Platform.runLater(() -> {
					showProgressBar();
				});
			progressBar.setProgress(newValue.doubleValue());
			if (newValue.intValue() == -1)
				Platform.runLater(() -> {
					hideProgressBar();
				});
		});
		
		// initial setup
		if (manager.getSwgReady().getValue() && manager.getPswgReady().getValue()) {
			playButton.setVisible(true);
			playButton.setDisable(false);
			setupButton.setVisible(false);
			scanButton.setVisible(true);
		} else {
			mainDisplay.queueString("Setup required");
			setupButton.setVisible(true);
			gameSettingsButton.setDisable(true);
			extrasButton.setDisable(true);
		}

		// animation
		
		// show low anim
		// fade
		showDownloadLow = new Timeline();
		final KeyValue showDownloadLowKV = new KeyValue(progressBar.opacityProperty(), 1, Interpolator.EASE_BOTH);
		final KeyFrame showDownloadLowKF = new KeyFrame(Duration.millis(FADE_DURATION), showDownloadLowKV);
		showDownloadLow.getKeyFrames().add(showDownloadLowKF);
		
		// show full anim
		// scale
		final ScaleTransition showDownloadHighScale = new ScaleTransition(Duration.millis(SLIDE_DURATION), progressBar);
		showDownloadHighScale.setFromX(0);
		showDownloadHighScale.setFromY(0);
		showDownloadHighScale.setToX(1);
		showDownloadHighScale.setToY(1);
		// fade
		final Timeline showDownloadHighFade = new Timeline();
		final KeyValue showDownloadHighFadeKV = new KeyValue(progressBar.opacityProperty(), 1, Interpolator.EASE_BOTH);
		final KeyFrame showDownloadHighFadeKF = new KeyFrame(Duration.millis(FADE_DURATION), showDownloadHighFadeKV);
		showDownloadHighFade.getKeyFrames().add(showDownloadHighFadeKF);
		// combine and play
		showDownloadHigh = new ParallelTransition();
		showDownloadHigh.getChildren().addAll(showDownloadHighScale, showDownloadHighFade);
		
		// hide low anim
		// long fade
		hideDownloadLow = new Timeline();
		final KeyValue hideDownloadLowKV = new KeyValue(progressBar.opacityProperty(), 0, Interpolator.EASE_BOTH);
		final KeyFrame hideDownloadLowKF = new KeyFrame(Duration.millis(SLIDE_DURATION), hideDownloadLowKV);
		hideDownloadLow.getKeyFrames().add(hideDownloadLowKF);
		hideDownloadLow.setOnFinished((event) -> {
			progressBar.setOpacity(1);
			progressBar.setVisible(false);;
		});
		
		// hide full anim
		// scale
		final ScaleTransition hideDownloadHighScale = new ScaleTransition(Duration.millis(SLIDE_DURATION), progressBar);
		hideDownloadHighScale.setFromX(1);
		hideDownloadHighScale.setFromY(1);
		hideDownloadHighScale.setToX(0);
		hideDownloadHighScale.setToY(0);
		// short fade
		final Timeline highDownloadHighFade = new Timeline();
		final KeyValue highDownloadHighFadeKV = new KeyValue(progressBar.opacityProperty(), 0, Interpolator.EASE_BOTH);
		final KeyFrame highDownloadHighFadeKF = new KeyFrame(Duration.millis(FADE_DURATION), highDownloadHighFadeKV);
		highDownloadHighFade.getKeyFrames().add(highDownloadHighFadeKF);
		// combine and play
		hideDownloadHigh = new ParallelTransition();
		hideDownloadHigh.getChildren().addAll(hideDownloadHighScale, highDownloadHighFade);
		hideDownloadHigh.setOnFinished((event) -> {
			progressBar.setOpacity(1);
			progressBar.setVisible(false);;
		});
		
		/*
		 * Extras
		 */
		TREFix trefix = new TREFix(this);
		extrasComponent.addExtra(trefix);
	}
	
	public void addButtonListeners()
	{
		playButton.setOnAction((e) -> {
			manager.startSWG();
		});
	
		setupButton.setOnAction((e) -> {
			modalController.showWithComponent(setupComponent);
		});
		
		cancelButton.setOnAction((e) -> {
			manager.requestStop();
		});
		
		scanButton.setOnAction((e) -> {
			manager.fullScan();
		});
		
		launcherSettingsButton.setOnAction((e) -> {
			modalController.showWithComponent(settingsComponent);
		});
		
		gameSettingsButton.setOnAction((e) -> {
			manager.launchGameSettings();
		});
		
		extrasButton.setOnAction((e) -> {
			modalController.showWithComponent(extrasComponent);
		});
		
		updateButton.setOnAction((e) -> {
			manager.updatePswg();
		});
	}
	
	public void setStage(Stage stage)
	{
		this.stage = stage;
	}
	
	public Stage getStage()
	{
		return stage;
	}
	
	public void refreshLoginServerStatus()
	{
		manager.pingLoginServer();
	}
	
	public ModalController getModalController()
	{
		return modalController;
	}
	
	public void showProgressBar()
	{
		switch (ProjectSWG.PREFS.getInt("animation", 2)) {
		case ANIMATION_NONE:
			progressBar.setVisible(true);
			break;
		case ANIMATION_LOW:
			progressBar.setOpacity(0);
			progressBar.setVisible(true);
			showDownloadLow.play();
			break;
		case ANIMATION_HIGH:
			progressBar.setOpacity(0);
			progressBar.setVisible(true);
			showDownloadHigh.play();
			break;
		}
	}
	
	public void hideProgressBar()
	{
		switch (ProjectSWG.PREFS.getInt("animation", 2)) {
		case ANIMATION_NONE:
			progressBar.setVisible(false);;
			break;
		case ANIMATION_LOW:
			hideDownloadLow.play();
			break;
		case ANIMATION_HIGH:
			hideDownloadHigh.play();
			break;
		}
	}
	
	@Override
	public Parent getRoot()
	{
		return root;
	}
	
	public ProjectSWG getPswg()
	{
		return pswg;
	}
	
	public Manager getManager()
	{
		return manager;
	}
	
	public SimpleIntegerProperty getAnimationLevel()
	{
		return animationLevel;
	}

	public ObservableList<GameController> getGames()
	{
		return games;
	}
	
	public Pane getGameProcessPane()
	{
		return gameProcessPane;
	}
}
