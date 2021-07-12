package jp.ac.titech.itpro.sdl.muscletraining;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.SoundPool;

public class SoundPlayer {
    private final SoundPool soundPool;
    private final int soundCount;
    private final int soundCount10;

    SoundPlayer(Context context){
        AudioAttributes attrs = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();
        soundPool = new SoundPool.Builder()
                .setAudioAttributes(attrs)
                .setMaxStreams(1)
                .build();

        // Sound Effects by 効果音ラボ
        // https://soundeffect-lab.info/sound/button/mp3/decision34.mp3
        soundCount = soundPool.load(context, R.raw.count, 1);
        // https://soundeffect-lab.info/sound/button/mp3/decision35.mp3
        soundCount10 = soundPool.load(context, R.raw.count10, 1);
    }

    void playCount(){
        soundPool.play(soundCount, 1.0f, 1.0f, 0, 0, 1);
    }

    void playCount10(){
        soundPool.play(soundCount10, 1.0f, 1.0f, 0, 0, 1);
    }
}
