package com.martynaroj.traveljournal.view.others.classes;

import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;

import androidx.core.content.FileProvider;

import com.martynaroj.traveljournal.BuildConfig;
import com.martynaroj.traveljournal.R;
import com.martynaroj.traveljournal.view.others.interfaces.Constants;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class PDFCreator {

    private Context context;

    private File file;
    private PdfDocument document;
    private PdfDocument.PageInfo pageInfo;
    private PdfDocument.Page page;
    private Canvas canvas;
    private int remainHeight;

    private static int MARGIN = 24;
    private static int CANVAS_W = Constants.PAGE_A4_WIDTH - (2 * MARGIN);
    private static int CANVAS_H = Constants.PAGE_A4_HEIGHT - (2 * MARGIN);

    private static int COLOR_BLACK = Color.BLACK;
    private static int COLOR_DARKGRAY = Color.DKGRAY;
    private static int COLOR_GRAY = Color.GRAY;

    private static int TSIZE_BIG = 16;
    private static int TSIZE_MEDIUM = 12;
    private static int TSIZE_NORMAL = 8;
    private static int TSIZE_SMALL = 4;

    private static Layout.Alignment A_CENTER = Layout.Alignment.ALIGN_CENTER;
    private static Layout.Alignment A_LEFT = Layout.Alignment.ALIGN_NORMAL;
    private static Layout.Alignment A_RIGHT = Layout.Alignment.ALIGN_OPPOSITE;


    public PDFCreator(Context context) {
        this.context = context;
    }


    //MAIN==========--------------------------------------------------------------------------------


    public void init(String filename) {
        file = new File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), filename + Constants.PDF_EXT);
        document = new PdfDocument();
        pageInfo = new PdfDocument.PageInfo.Builder(
                Constants.PAGE_A4_WIDTH, Constants.PAGE_A4_HEIGHT, 1).create();
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
        drawText("testtest", TSIZE_NORMAL, COLOR_BLACK);
        document.finishPage(page);
    }


    private void drawText(String text, int size, int color) {
        TextPaint textPaint = getTextPaint(size, color);
        StaticLayout textLayout = getTextLayout(text, textPaint, Layout.Alignment.ALIGN_NORMAL);
        if (textLayout.getHeight() > remainHeight) {
            int index = measureIndexToSubstring(textLayout.getHeight(), textLayout);
            textLayout = getTextLayout(text.substring(0, index), textPaint, Layout.Alignment.ALIGN_NORMAL);
            textLayout.draw(canvas);
            document.finishPage(page);
            addNewPage();
            textLayout = getTextLayout(text.substring(index), textPaint, Layout.Alignment.ALIGN_NORMAL);
        }
        textLayout.draw(canvas);
        moveCanvas(textLayout.getHeight());
    }


    private void drawNewLines(int amount) {
        StringBuilder text = new StringBuilder(" ");
        for (int i = 0; i < amount - 1; i++)
            text.append("\n");
        StaticLayout textLayout = getTextLayout(text.toString(), getTextPaint(TSIZE_NORMAL, COLOR_BLACK),
                Layout.Alignment.ALIGN_NORMAL);
        moveCanvas(textLayout.getHeight());
    }


    //DRAWING OPTIONS-------------------------------------------------------------------------------


    private StaticLayout getTextLayout(String text, TextPaint textPaint, Layout.Alignment alignment) {
        return new StaticLayout(text, textPaint, CANVAS_W, alignment,
                1, 0, false);
    }


    private TextPaint getTextPaint(int size, int color) {
        TextPaint textPaint = new TextPaint();
        textPaint.setAntiAlias(true);
        textPaint.setTextSize(size * context.getResources().getDisplayMetrics().density);
        textPaint.setColor(color);
        return textPaint;
    }


    //OTHERS----------------------------------------------------------------------------------------


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
