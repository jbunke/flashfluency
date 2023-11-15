package com.redsquare.flashfluency.readler;

import com.jordanbunke.delta_time.contexts.ProgramContext;
import com.jordanbunke.delta_time.debug.GameDebugger;
import com.jordanbunke.delta_time.game.Game;
import com.jordanbunke.delta_time.game.GameManager;
import com.jordanbunke.delta_time.image.GameImage;
import com.jordanbunke.delta_time.io.InputEventLogger;
import com.jordanbunke.delta_time.window.GameWindow;

public class Readler implements ProgramContext {

    private static final Readler INSTANCE;

    public final Game program;

    static {
        INSTANCE = new Readler();
    }

    public Readler() {
        final GameWindow window = new GameWindow(ReadlerConstants.TITLE,
                ReadlerConstants.WINDOWED_W, ReadlerConstants.WINDOWED_H,
                GameImage.dummy(), true, false, false);
        final GameManager manager = new GameManager(0, this);

        program = new Game(window, manager, ReadlerConstants.HZ, ReadlerConstants.FPS);
        program.setCanvasSize(ReadlerConstants.CANVAS_W, ReadlerConstants.CANVAS_H);
    }

    public static Readler get() {
        return INSTANCE;
    }

    public static void main(String[] args) {

    }

    @Override
    public void process(final InputEventLogger eventLogger) {

    }

    @Override
    public void update(final double deltaTime) {

    }

    @Override
    public void render(final GameImage canvas) {

    }

    @Override
    public void debugRender(final GameImage canvas, final GameDebugger debugger) {

    }
}
