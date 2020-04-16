package com.martynaroj.traveljournal.view.others.classes;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Typeface;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;

import androidx.core.content.FileProvider;
import androidx.core.content.res.ResourcesCompat;

import com.martynaroj.traveljournal.BuildConfig;
import com.martynaroj.traveljournal.R;
import com.martynaroj.traveljournal.services.models.Address;
import com.martynaroj.traveljournal.services.models.Day;
import com.martynaroj.traveljournal.services.models.Travel;
import com.martynaroj.traveljournal.services.models.User;
import com.martynaroj.traveljournal.view.others.BitmapLoadAsyncTask;
import com.martynaroj.traveljournal.view.others.interfaces.Constants;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class PDFCreator {

    private Context context;

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

    private static int MARGIN = 64;
    private static int CANVAS_W = Constants.PAGE_A4_WIDTH - (2 * MARGIN);
    private static int CANVAS_H = Constants.PAGE_A4_HEIGHT - (2 * MARGIN);

    private static int COLOR_BLACK = Color.BLACK;
    private static int COLOR_DKGRAY = Color.DKGRAY;
    private static int COLOR_GRAY = Color.GRAY;

    private static int TSIZE_BIG = 12;
    private static int TSIZE_NORMAL = 10;
    private static int TSIZE_SMALL = 8;
    private static int TSIZE_TINY = 4;

    private static Typeface FONT_NORMAL;
    private static Typeface FONT_BOLD;

    private static Layout.Alignment ALIGN_CENTER = Layout.Alignment.ALIGN_CENTER;
    private static Layout.Alignment ALIGN_LEFT = Layout.Alignment.ALIGN_NORMAL;
    private static Layout.Alignment ALIGN_RIGHT = Layout.Alignment.ALIGN_OPPOSITE;

    private static int IMAGE_H = 200, IMAGE_W = 300;


    public PDFCreator(Context context, User user, Travel travel, Address destination, List<Day> days) {
        this.context = context;
        this.user = user;
        this.travel = travel;
        this.destination = destination;
        this.days = days;
        FONT_NORMAL = ResourcesCompat.getFont(context.getApplicationContext(), R.font.raleway_regular);
        FONT_BOLD = ResourcesCompat.getFont(context.getApplicationContext(), R.font.raleway_bold);
    }


    //MAIN==========--------------------------------------------------------------------------------


    public void init() {
        file = new File(context.getExternalFilesDir(
                Environment.DIRECTORY_DOWNLOADS),
                getFilenameFormat(travel.getName()) + Constants.PDF_EXT
        );
        document = new PdfDocument();
        pageInfo = new PdfDocument.PageInfo.Builder(Constants.PAGE_A4_WIDTH, Constants.PAGE_A4_HEIGHT, 1).create();
        createTitlePage();
    }


    public String tryToSave() {
        try {
            document.writeTo(new FileOutputStream(file));
            document.close();
            return context.getResources().getString(R.string.messages_file_saved_path) + " " + file.getPath();
        } catch (IOException e) {
            document.close();
            return context.getResources().getString(R.string.messages_error_failed_save_file);
        }
    }


    public void openFile() {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(getFileUri(), "application/pdf");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        if (intent.resolveActivity(context.getPackageManager()) != null)
            context.startActivity(intent);
    }


    //DRAWING---------------------------------------------------------------------------------------


    private void createTitlePage() {
        addNewPage();
        drawText( travel.getDateRangeString(), TSIZE_SMALL, COLOR_GRAY, FONT_NORMAL, ALIGN_RIGHT);
        drawNewLines(1);
        drawText("Author: " + user.getUsername(), TSIZE_SMALL, COLOR_GRAY, FONT_NORMAL, ALIGN_LEFT);
        drawNewLines(1);
        drawText(travel.getName(), TSIZE_BIG, COLOR_BLACK, FONT_BOLD, ALIGN_CENTER);
        drawNewLines(1);
        drawBitmap(travel.getImage());
        drawNewLines(1);
        drawText(destination.getName(), TSIZE_NORMAL, COLOR_BLACK, FONT_NORMAL, ALIGN_CENTER);
        drawText(destination.getAddress(), TSIZE_NORMAL, COLOR_BLACK, FONT_NORMAL, ALIGN_CENTER);
        drawNewLines(1);
        drawText(travel.getDescription(), TSIZE_NORMAL, COLOR_DKGRAY, FONT_NORMAL, ALIGN_CENTER);
        drawSignature();
        document.finishPage(page);
    }


    private void drawText(String text, int size, int color, Typeface typeface, Layout.Alignment alignment) {
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


    private void drawBitmap(String url) {
        if (url != null && !url.trim().isEmpty()) {
            try {
                Bitmap bitmap = new BitmapLoadAsyncTask(url).execute().get();
                Bitmap resizedBitmap = resizeBitmap(bitmap);
                canvas.drawBitmap(resizedBitmap, (CANVAS_W - resizedBitmap.getWidth()) / 2f, 0, null);
                moveCanvas(resizedBitmap.getHeight());
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        }
    }


    private void drawSignature() {
        Calendar date = Calendar.getInstance();
        StaticLayout textLayout = getTextLayout("Created with Travel Journal, " + date.get(Calendar.YEAR),
                getTextPaint(TSIZE_TINY, COLOR_GRAY, FONT_NORMAL), ALIGN_CENTER);
        canvas.translate(0, remainHeight + MARGIN/2f);
        textLayout.draw(canvas);
    }


    private void drawNewLines(int amount) {
        StringBuilder text = new StringBuilder(" ");
        for (int i = 0; i < amount - 1; i++)
            text.append("\n");
        StaticLayout textLayout = getTextLayout(text.toString(),
                getTextPaint(TSIZE_NORMAL, COLOR_BLACK, FONT_NORMAL), ALIGN_LEFT);
        moveCanvas(textLayout.getHeight());
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


    //OTHERS----------------------------------------------------------------------------------------


    private Bitmap resizeBitmap(Bitmap bitmap) {
        Matrix matrix = new Matrix();
        matrix.setScale((float) IMAGE_W / bitmap.getWidth(), (float) IMAGE_H / bitmap.getHeight());
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, false);
    }


    private String getFilenameFormat(String text) {
        return text.replaceAll("\\s+", "_").replaceAll("[{}$&+,:;=\\\\?@#|/'<>.^*()%!-]", "");
    }


    private void addNewPage() {
        page = document.startPage(pageInfo);
        canvas = page.getCanvas();
        canvas.translate(MARGIN, MARGIN);
        remainHeight = CANVAS_H;
    }


    private void moveCanvas(int height) {
        remainHeight -= height;
        canvas.translate(0, height);
    }


    private int measureIndexToSubstring(double textHeight, StaticLayout textLayout) {
        double lineHeight = textHeight / textLayout.getLineCount();
        int howMuchLines = (int) Math.floor(remainHeight / lineHeight);
        return textLayout.getLineStart(howMuchLines + 1);
    }


    private Uri getFileUri() {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M)
            return FileProvider.getUriForFile(context, BuildConfig.APPLICATION_ID + ".provider", file);
        else
            return Uri.fromFile(file);
    }

}
