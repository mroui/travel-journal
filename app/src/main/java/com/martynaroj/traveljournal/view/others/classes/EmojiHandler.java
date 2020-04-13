package com.martynaroj.traveljournal.view.others.classes;

import android.view.View;
import android.widget.ImageView;

import com.martynaroj.traveljournal.R;
import com.martynaroj.traveljournal.databinding.DialogAddNoteBinding;
import com.martynaroj.traveljournal.databinding.DialogRateDayBinding;
import com.martynaroj.traveljournal.view.others.enums.Emoji;

public class EmojiHandler implements View.OnClickListener {

    private ImageView emojiHappy, emojiNormal, emojiSad, emojiLucky, emojiShocked, emojiBored;
    private Emoji selectedEmoji;


    public Emoji getSelectedEmoji() {
        return selectedEmoji;
    }


    public EmojiHandler(DialogAddNoteBinding binding, int defaultEmoji) {
        selectedEmoji = Emoji.values()[defaultEmoji];
        initEmoji(binding, defaultEmoji);
        setDefaultEmoji(defaultEmoji);
        setEmojiListener();
    }


    public EmojiHandler(DialogRateDayBinding binding, int defaultEmoji) {
        selectedEmoji = Emoji.values()[defaultEmoji];
        initEmoji(binding, defaultEmoji);
        setDefaultEmoji(defaultEmoji);
        setEmojiListener();
    }


    private void initEmoji(DialogAddNoteBinding binding, int defaultEmoji) {
        emojiHappy = binding.dialogAddNotePlaceEmojiHappy;
        emojiNormal = binding.dialogAddNotePlaceEmojiNormal;
        emojiSad = binding.dialogAddNotePlaceEmojiSad;
        emojiLucky = binding.dialogAddNotePlaceEmojiLucky;
        emojiShocked = binding.dialogAddNotePlaceEmojiShocked;
        emojiBored = binding.dialogAddNotePlaceEmojiBored;
    }


    private void initEmoji(DialogRateDayBinding binding, int defaultEmoji) {
        emojiHappy = binding.dialogRateDayEmojiHappy;
        emojiNormal = binding.dialogRateDayEmojiNormal;
        emojiSad = binding.dialogRateDayEmojiSad;
        emojiLucky = binding.dialogRateDayEmojiLucky;
        emojiShocked = binding.dialogRateDayEmojiShocked;
        emojiBored = binding.dialogRateDayEmojiBored;
    }


    private void setDefaultEmoji(int emoji) {
        switch (Emoji.values()[emoji]) {
            case HAPPY:
                emojiOnClick(Emoji.HAPPY, emojiHappy, R.drawable.ic_emoji_happy_color);
                break;
            case NORMAL:
                emojiOnClick(Emoji.NORMAL, emojiNormal, R.drawable.ic_emoji_normal_color);
                break;
            case SAD:
                emojiOnClick(Emoji.SAD, emojiSad, R.drawable.ic_emoji_sad_color);
                break;
            case LUCKY:
                emojiOnClick(Emoji.LUCKY, emojiLucky, R.drawable.ic_emoji_lucky_color);
                break;
            case SHOCKED:
                emojiOnClick(Emoji.SHOCKED, emojiShocked, R.drawable.ic_emoji_shocked_color);
                break;
            case BORED:
                emojiOnClick(Emoji.BORED, emojiBored, R.drawable.ic_emoji_bored_color);
                break;
        }
    }


    private void setEmojiListener() {
        emojiHappy.setOnClickListener(this);
        emojiNormal.setOnClickListener(this);
        emojiSad.setOnClickListener(this);
        emojiLucky.setOnClickListener(this);
        emojiShocked.setOnClickListener(this);
        emojiBored.setOnClickListener(this);
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.dialog_rate_day_emoji_happy:
            case R.id.dialog_add_note_place_emoji_happy:
                emojiOnClick(Emoji.HAPPY, view, R.drawable.ic_emoji_happy_color);
                break;
            case R.id.dialog_rate_day_emoji_normal:
            case R.id.dialog_add_note_place_emoji_normal:
                emojiOnClick(Emoji.NORMAL, view, R.drawable.ic_emoji_normal_color);
                break;
            case R.id.dialog_rate_day_emoji_sad:
            case R.id.dialog_add_note_place_emoji_sad:
                emojiOnClick(Emoji.SAD, view, R.drawable.ic_emoji_sad_color);
                break;
            case R.id.dialog_rate_day_emoji_lucky:
            case R.id.dialog_add_note_place_emoji_lucky:
                emojiOnClick(Emoji.LUCKY, view, R.drawable.ic_emoji_lucky_color);
                break;
            case R.id.dialog_rate_day_emoji_shocked:
            case R.id.dialog_add_note_place_emoji_shocked:
                emojiOnClick(Emoji.SHOCKED, view, R.drawable.ic_emoji_shocked_color);
                break;
            case R.id.dialog_rate_day_emoji_bored:
            case R.id.dialog_add_note_place_emoji_bored:
                emojiOnClick(Emoji.BORED, view, R.drawable.ic_emoji_bored_color);
                break;
        }
    }


    private void emojiOnClick(Emoji emoji, View view, int resource) {
        this.selectedEmoji = emoji;
        clearEmoji();
        ((ImageView) view).setImageResource(resource);
    }


    private void clearEmoji() {
        emojiHappy.setImageResource(R.drawable.ic_emoji_happy);
        emojiNormal.setImageResource(R.drawable.ic_emoji_normal);
        emojiSad.setImageResource(R.drawable.ic_emoji_sad);
        emojiLucky.setImageResource(R.drawable.ic_emoji_lucky);
        emojiShocked.setImageResource(R.drawable.ic_emoji_shocked);
        emojiBored.setImageResource(R.drawable.ic_emoji_bored);
    }

}
