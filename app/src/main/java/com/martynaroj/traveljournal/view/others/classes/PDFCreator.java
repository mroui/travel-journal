package com.martynaroj.traveljournal.view.others.classes;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.pdf.PdfDocument;
import android.os.AsyncTask;
import android.os.Build;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;

import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat;

import com.martynaroj.traveljournal.R;
import com.martynaroj.traveljournal.services.models.Address;
import com.martynaroj.traveljournal.services.models.Day;
import com.martynaroj.traveljournal.services.models.Note;
import com.martynaroj.traveljournal.services.models.Photo;
import com.martynaroj.traveljournal.services.models.Place;
import com.martynaroj.traveljournal.services.models.Travel;
import com.martynaroj.traveljournal.services.models.User;
import com.martynaroj.traveljournal.view.interfaces.PdfCreatorListener;
import com.martynaroj.traveljournal.view.others.enums.Emoji;
import com.martynaroj.traveljournal.view.others.interfaces.Constants;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Calendar;
import java.util.List;

public class PDFCreator extends AsyncTask<Void, Void, Void> {

    @SuppressLint("StaticFieldLeak")
    private Context context;
    private PdfCreatorListener listener;

    private File file;
    private PdfDocument document;
    private PdfDocument.PageInfo pageInfo;
    private PdfDocument.Page page;
    private Canvas canvas;
    private int remainHeight;

    private User user;
    private Travel travel;
    private Address destination;
    private List<Day> days;

    private static int MARGIN = 48;
    private static int CANVAS_W = Constants.PAGE_A4_WIDTH - (2 * MARGIN);
    private static int CANVAS_H = Constants.PAGE_A4_HEIGHT - (2 * MARGIN);

    private static int COLOR_BLACK = Color.BLACK;
    private static int COLOR_DKGRAY = Color.DKGRAY;
    private static int COLOR_GRAY = Color.GRAY;
    private static int COLOR_LTGRAY = Color.LTGRAY;

    private static int TSIZE_BIG = 12;
    private static int TSIZE_MEDIUM = 10;
    private static int TSIZE_NORMAL = 8;
    private static int TSIZE_SMALL = 6;
    private static int TSIZE_TINY = 4;

    private static Typeface FONT_NORMAL;
    private static Typeface FONT_BOLD;

    private static Layout.Alignment ALIGN_CENTER = Layout.Alignment.ALIGN_CENTER;
    private static Layout.Alignment ALIGN_LEFT = Layout.Alignment.ALIGN_NORMAL;
    private static Layout.Alignment ALIGN_RIGHT = Layout.Alignment.ALIGN_OPPOSITE;

    private static int IMAGE_H = 200, IMAGE_W = 300;
    private static int ICON_H = 36, ICON_W = 36;


    public PDFCreator(Context context, File file, User user, Travel travel, Address destination, List<Day> days,
                      PdfCreatorListener listener) {
        this.context = context;
        this.file = file;
        this.user = user;
        this.travel = travel;
        this.destination = destination;
        this.days = days;
        this.listener = listener;
        FONT_NORMAL = ResourcesCompat.getFont(context.getApplicationContext(), R.font.raleway_regular);
        FONT_BOLD = ResourcesCompat.getFont(context.getApplicationContext(), R.font.raleway_bold);
    }


    //MAIN------------------------------------------------------------------------------------------


    public void init() {
        document = new PdfDocument();
        pageInfo = new PdfDocument.PageInfo.Builder(Constants.PAGE_A4_WIDTH, Constants.PAGE_A4_HEIGHT, 1).create();
        drawTitlePage();
        createContent();
    }


    private void createContent() {
        if (days != null && !days.isEmpty()) {
            for (Day day : days) {
                createDayContent(day);
            }
        }
    }


    private void createDayContent(Day day) {
        long whatDay = Travel.whatDay(travel.getDatetimeFrom(), day.getDate());
        List<Note> allNotes = day.getAllSortedNotes();
        if (!allNotes.isEmpty()) {
            addNewPage();
            drawText("DAY " + whatDay, TSIZE_BIG, COLOR_BLACK, FONT_BOLD, ALIGN_CENTER);
            drawText(day.getDateString(), TSIZE_MEDIUM, COLOR_GRAY, FONT_BOLD, ALIGN_CENTER);
            drawNewLines(1, TSIZE_TINY);
            drawEmojiRate(Emoji.values()[day.getRate()], (CANVAS_W - ICON_W) / 2);
            drawNewLines(2, TSIZE_BIG);
            for (Note note : allNotes) {
                createNoteContent(note);
            }
            document.finishPage(page);
        }
    }


    private void createNoteContent(Note note) {
        drawText(note.getTimeString(), TSIZE_NORMAL, COLOR_GRAY, FONT_BOLD, ALIGN_LEFT);
        drawNewLines(1, TSIZE_TINY);
        if (note instanceof Photo) {
            drawUrlBitmap(((Photo) note).getSrc(), 0, IMAGE_W, IMAGE_H);
            drawNewLines(1, TSIZE_TINY);
            drawText(note.getDescription(), TSIZE_NORMAL, COLOR_DKGRAY, FONT_NORMAL, ALIGN_LEFT);
        } else if (note instanceof Place) {
            drawText(((Place) note).getAddress().replace("&", ", "), TSIZE_NORMAL,
                    COLOR_BLACK, FONT_BOLD, ALIGN_LEFT);
            drawNewLines(1, TSIZE_TINY);
            drawText(note.getDescription(), TSIZE_NORMAL, COLOR_DKGRAY, FONT_NORMAL, ALIGN_LEFT);
            drawNewLines(1, TSIZE_TINY);
            drawEmojiRate(Emoji.values()[((Place) note).getRate()], 0);
        } else {
            drawText(note.getDescription(), TSIZE_NORMAL, COLOR_DKGRAY, FONT_NORMAL, ALIGN_LEFT);
        }
        drawNewLines(2, TSIZE_BIG);
    }


    private String tryToSave() {
        try {
            FileOutputStream outputStream = new FileOutputStream(file);
            document.writeTo(outputStream);
            outputStream.flush();
            outputStream.close();
            document.close();
            return context.getResources().getString(R.string.messages_file_saved_path) + " " + file.getPath();
        } catch (IOException e) {
            document.close();
            return context.getResources().getString(R.string.messages_error_failed_save_file);
        }
    }


    //DRAWING---------------------------------------------------------------------------------------


    private void drawText(String text, int size, int color, Typeface typeface, Layout.Alignment alignment) {
        if (remainHeight <= 0) {
            document.finishPage(page);
            addNewPage();
        }
        TextPaint textPaint = getTextPaint(size, color, typeface);
        StaticLayout textLayout = getTextLayout(text, textPaint, alignment);
        if (textLayout.getHeight() > remainHeight) {
            int index = measureIndexToSubstring(textLayout.getHeight(), textLayout);
            textLayout = getTextLayout(text.substring(0, index), textPaint, alignment);
            textLayout.draw(canvas);
            document.finishPage(page);
            addNewPage();
            textLayout = getTextLayout(text.substring(index), textPaint, alignment);
        }
        textLayout.draw(canvas);
        moveCanvas(textLayout.getHeight());
    }


    private void drawUrlBitmap(String url, int left, int width, int height) {
        if (url != null && !url.trim().isEmpty()) {
            try {
                Bitmap bitmap = BitmapFactory.decodeStream(new URL(url).openStream());
                Bitmap resizedBitmap = resizeBitmap(bitmap, width, height);
                if (resizedBitmap.getHeight() > remainHeight) {
                    document.finishPage(page);
                    addNewPage();
                }
                canvas.drawBitmap(resizedBitmap, left, 0, null);
                resizedBitmap.recycle();
                moveCanvas(resizedBitmap.getHeight());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    private void drawDrawableVectorBitmap(int id, int left, int width, int height) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            Drawable drawable = ContextCompat.getDrawable(context, id);
            if (drawable != null) {
                drawable = (DrawableCompat.wrap(drawable)).mutate();
                drawable.setBounds(left, 0, width + left, height);
                if (height > remainHeight) {
                    document.finishPage(page);
                    addNewPage();
                }
                drawable.draw(canvas);
                moveCanvas(height);
            }
        } else {
            VectorDrawableCompat vector = VectorDrawableCompat.create(context.getResources(), id, null);
            if (vector != null) {
                vector.setBounds(left, 0, width + left, height);
                if (height > remainHeight) {
                    document.finishPage(page);
                    addNewPage();
                }
                vector.draw(canvas);
                moveCanvas(height);
            }
        }
    }


    private void drawNewLines(int amount, int size) {
        StringBuilder text = new StringBuilder(" ");
        for (int i = 0; i < amount - 1; i++)
            text.append("\n");
        StaticLayout textLayout = getTextLayout(text.toString(),
                getTextPaint(size, COLOR_BLACK, FONT_NORMAL), ALIGN_LEFT);
        moveCanvas(textLayout.getHeight());
    }


    private void drawTitlePage() {
        addNewPage();
        drawText(travel.getDateRangeString(), TSIZE_SMALL, COLOR_GRAY, FONT_BOLD, ALIGN_LEFT);
        drawText("@" + user.getUsername(), TSIZE_SMALL, COLOR_GRAY, FONT_BOLD, ALIGN_LEFT);
        drawNewLines(2, TSIZE_NORMAL);
        drawText(travel.getName(), TSIZE_BIG, COLOR_BLACK, FONT_BOLD, ALIGN_CENTER);
        drawNewLines(1, TSIZE_NORMAL);
        drawUrlBitmap(travel.getImage(), (CANVAS_W - IMAGE_W) / 2, IMAGE_W, IMAGE_H);
        drawNewLines(1, TSIZE_NORMAL);
        drawText(destination.getName(), TSIZE_MEDIUM, COLOR_DKGRAY, FONT_BOLD, ALIGN_CENTER);
        drawText(destination.getAddress(), TSIZE_NORMAL, COLOR_GRAY, FONT_BOLD, ALIGN_CENTER);
        drawText(destination.getLatLonString(), TSIZE_SMALL, COLOR_LTGRAY, FONT_BOLD, ALIGN_CENTER);
        drawNewLines(1, TSIZE_NORMAL);
        drawText(travel.getDescription(), TSIZE_NORMAL, COLOR_DKGRAY, FONT_NORMAL, ALIGN_CENTER);
        document.finishPage(page);
    }


    private void drawBackground() {
        Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.pdf_background);
        Rect src = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        Rect dest = new Rect(0, 0, Constants.PAGE_A4_WIDTH, Constants.PAGE_A4_HEIGHT);
        canvas.drawBitmap(bitmap, src, dest, null);
    }


    private void drawSignature() {
        Calendar date = Calendar.getInstance();
        StaticLayout textLayout = getTextLayout("Created with Travel Journal, " + date.get(Calendar.YEAR),
                getTextPaint(TSIZE_TINY, COLOR_GRAY, FONT_NORMAL), ALIGN_CENTER);
        canvas.translate(0, CANVAS_H + (MARGIN / 2f));
        textLayout.draw(canvas);
        canvas.translate(0, -(CANVAS_H + (MARGIN / 2f)));
    }


    private void drawEmojiRate(Emoji value, int left) {
        int id = 0;
        switch (value) {
            case HAPPY:
                id = R.drawable.ic_emoji_happy_color;
                break;
            case BORED:
                id = R.drawable.ic_emoji_bored_color;
                break;
            case SAD:
                id = R.drawable.ic_emoji_sad_color;
                break;
            case LUCKY:
                id = R.drawable.ic_emoji_lucky_color;
                break;
            case NORMAL:
                id = R.drawable.ic_emoji_normal_color;
                break;
            case SHOCKED:
                id = R.drawable.ic_emoji_shocked_color;
                break;
        }
        drawDrawableVectorBitmap(id, left, ICON_W, ICON_H);
    }


    //DRAWING OPTIONS-------------------------------------------------------------------------------


    private StaticLayout getTextLayout(String text, TextPaint textPaint, Layout.Alignment alignment) {
        return new StaticLayout(text, textPaint, CANVAS_W, alignment, 1, 0, false);
    }


    private TextPaint getTextPaint(int size, int color, Typeface typeface) {
        TextPaint textPaint = new TextPaint();
        textPaint.setAntiAlias(true);
        textPaint.setTypeface(typeface);
        textPaint.setTextSize(size * context.getResources().getDisplayMetrics().density);
        textPaint.setColor(color);
        return textPaint;
    }


    private Bitmap resizeBitmap(Bitmap bitmap, float width, float height) {
        Matrix matrix = new Matrix();
        matrix.setScale(width / bitmap.getWidth(), height / bitmap.getHeight());
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, false);
    }


    private void addNewPage() {
        page = document.startPage(pageInfo);
        canvas = page.getCanvas();
        remainHeight = CANVAS_H;
        drawBackground();
        canvas.translate(MARGIN, MARGIN);
        drawSignature();
    }


    private void moveCanvas(int height) {
        remainHeight -= height;
        canvas.translate(0, height);
    }


    //OTHERS----------------------------------------------------------------------------------------


    private int measureIndexToSubstring(double textHeight, StaticLayout textLayout) {
        double lineHeight = textHeight / textLayout.getLineCount();
        int howMuchLines = (int) Math.floor(remainHeight / lineHeight);
        if (howMuchLines == 0)
            return 0;
        else
            return textLayout.getLineStart(howMuchLines + 1);
    }


    @Override
    protected Void doInBackground(Void... voids) {
        init();
        return null;
    }


    @Override
    protected void onPostExecute(Void status) {
        listener.onFinish(tryToSave());
    }

}
