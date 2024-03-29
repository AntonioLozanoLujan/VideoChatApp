package application.services;

import application.services.audio.player.AudioPlayerService;
import application.services.audio.receiver.AudioStreamingReceiverService;
import application.services.audio.recorder.AudioRecordingService;
import application.services.audio.sender.AudioStreamingSenderService;
import application.services.video.player.VideoPlayerService;
import application.services.video.receiver.VideoStreamingReceiverService;
import application.services.video.recorder.VideoRecordingService;
import application.services.video.sender.VideoStreamingSenderService;
import javafx.scene.image.ImageView;

public class VideoChatServiceManager {

	private static AudioStreamingReceiverService audioReceiver;

	private static AudioStreamingSenderService audioSender;

	private static VideoStreamingReceiverService videoReceiver;

	private static VideoStreamingSenderService videoSender;

	private static AudioPlayerService audioPlayer;

	private static AudioRecordingService audioRecorder;

	private static VideoPlayerService videoPlayer;

	private static VideoRecordingService videoRecorder;

	private static boolean activeMic = true;

	private static boolean activeSpeaker = true;

	private static boolean pausedCall = false;

	static {
		audioReceiver = AudioStreamingReceiverService.getInstance();
		audioSender = AudioStreamingSenderService.getInstance();

		videoReceiver = VideoStreamingReceiverService.getInstance();
		videoSender = VideoStreamingSenderService.getInstance();

		audioPlayer = AudioPlayerService.getInstance();
		audioRecorder = AudioRecordingService.getInstance();

		videoPlayer = VideoPlayerService.getInstance();
		videoRecorder = VideoRecordingService.getInstance();

		//===========================================================
		
		audioReceiver.addServiceTask(() -> {
			audioPlayer.setData(audioReceiver.getData());
		});

		videoReceiver.addServiceTask(() -> {
			videoPlayer.setImage(videoReceiver.getData());
		});

		audioRecorder.addServiceTask(() -> {
			audioSender.setAudioData(audioRecorder.getAudioData());
		});

		videoRecorder.addServiceTask(() -> {
			videoSender.setImage(videoRecorder.getLastFrame());
		});	
		
		//===========================================================
		
		audioReceiver.addConnectionListener(() -> {
			audioPlayer.startPlayer();
		});
		
		audioSender.addConnectionListener(() -> {
			audioRecorder.startRecorder();
		});
		
		videoReceiver.addConnectionListener(() -> {
			videoPlayer.startPlayer();
		});
		
		videoSender.addConnectionListener(() -> {
			videoRecorder.startRecorder();
		});
		
		//===========================================================
	}

	public static void setupPlayer(ImageView view) {
		videoPlayer.setImageView(view);
	}

	public static void initCall() {
		audioSender.startSender();
		videoSender.startSender();
		
		videoPlayer.setBlack();
	}

	public static void acceptCall() {
		audioReceiver.startReceiver();
		videoReceiver.startReceiver();
	}

	public static void pauseCall() {
		pausedCall = !pausedCall;
		if (pausedCall) {
			audioPlayer.pausePlayer();
			videoPlayer.pausePlayer();
			audioRecorder.pauseRecorder();
			videoRecorder.pauseRecorder();
		} else {
			if(activeSpeaker)
				audioPlayer.resumePlayer();
			
			videoPlayer.resumePlayer();
			if(activeMic)
				audioRecorder.resumeRecorder();
			
			videoRecorder.resumeRecorder();
		}
	}

	public static void stopCall() {
		audioPlayer.stopService();
		videoPlayer.stopService();
		
		audioRecorder.stopService();
		videoRecorder.stopService();
		
		audioSender.stopService();
		videoSender.stopService();
		
		audioReceiver.stopService();
		videoReceiver.stopService();
	}

	public static void toggleMic() {
		if (activeMic) {
			audioRecorder.pauseRecorder();
		} else {
			audioRecorder.resumeRecorder();
		}
		activeMic = !activeMic;
	}

	public static void toggleSpeaker() {
		if (activeSpeaker) {
			audioPlayer.pausePlayer();
		} else {
			audioPlayer.resumePlayer();
		}
		activeSpeaker = !activeSpeaker;
	}
}
