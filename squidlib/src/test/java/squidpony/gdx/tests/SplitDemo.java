package squidpony.gdx.tests;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Files;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import squidpony.FakeLanguageGen;
import squidpony.squidai.DijkstraMap;
import squidpony.squidgrid.gui.gdx.*;
import squidpony.squidgrid.mapping.DungeonGenerator;
import squidpony.squidgrid.mapping.DungeonUtility;
import squidpony.squidmath.Coord;
import squidpony.squidmath.CoordPacker;
import squidpony.squidmath.RNG;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Trying to test how SplitPane s from libGDX can be used to differentiate parts of the UI. Not currently working.
 * Created by Tommy Ettinger on 12/29/2016.
 */
public class SplitDemo extends ApplicationAdapter {
    private Stage stage;
    private SpriteBatch batch;
    private RNG rng;
    private Skin skin;
    private SquidLayers display;
    private TextPanel<Color> messages;
    private VerticalGroup images;
    private TextureAtlas atlas;
    private Array<TextureAtlas.AtlasRegion> regions;
    private SplitPane innerSplit, outerSplit;
    private DungeonGenerator dungeonGen;
    private char[][] decoDungeon, bareDungeon, lineDungeon;
    private int[][] colorIndices, bgColorIndices;
    /** In number of cells */
    private int gridWidth;
    /** In number of cells */
    private int gridHeight;
    /** The pixel width of a cell */
    private int cellWidth;
    /** The pixel height of a cell */
    private int cellHeight;
    private SquidInput input;
    private Color bgColor;
    private DijkstraMap playerToCursor;
    private Coord cursor, player;
    private ArrayList<Coord> toCursor;
    private ArrayList<Coord> awaitedMoves;
    private float secondsWithoutMoves;
    private String[] lang;
    private int langIndex = 0;
    @Override
    public void create () {
        //These variables, corresponding to the screen's width and height in cells and a cell's width and height in
        //pixels, must match the size you specified in the launcher for input to behave.
        //This is one of the more common places a mistake can happen.
        //In our desktop launcher, we gave these arguments to the configuration:
        //	config.width = 80 * 8;
        //  config.height = 40 * 18;
        //Here, config.height refers to the total number of rows to be displayed on the screen.
        //We're displaying 32 rows of dungeon, then 8 more rows of text generation to show some tricks with language.
        //gridHeight is 32 because that variable will be used for generating the dungeon and handling movement within
        //the upper 32 rows. Anything that refers to the full height, which happens rarely and usually for things like
        //screen resizes, just uses gridHeight + 8. Next to it is gridWidth, which is 80 because we want 80 grid spaces
        //across the whole screen. cellWidth and cellHeight are 8 and 18, and match the multipliers for config.width and
        //config.height, but in this case don't strictly need to because we soon use a "Stretchable" font. While
        //gridWidth and gridHeight are measured in spaces on the grid, cellWidth and cellHeight are the pixel dimensions
        //of an individual cell. The font will look more crisp if the cell dimensions match the config multipliers
        //exactly, and the stretchable fonts (technically, distance field fonts) can resize to non-square sizes and
        //still retain most of that crispness.
        gridWidth = 80;
        gridHeight = 24;
        cellWidth = 14;
        cellHeight = 21;
        // gotta have a random number generator. We can seed an RNG with any long we want, or even a String.
        rng = new RNG("SquidLib!");
        atlas = DefaultResources.getIconAtlas();
        regions = atlas.getRegions();

        //Some classes in SquidLib need access to a batch to render certain things, so it's a good idea to have one.
        batch = new SpriteBatch();
        //Here we make sure our Stage, which holds any text-based grids we make, uses our Batch.
        stage = new Stage(new StretchViewport(Gdx.graphics.getWidth(), Gdx.graphics.getHeight()), batch);
        display = new SquidLayers(gridWidth, gridHeight, cellWidth, cellHeight,
                DefaultResources.getStretchableCodeFont());

        /*
        	default-vertical: { handle: default-splitpane-vertical },
	default-horizontal: { handle: default-splitpane }

         */
        skin = new Skin(atlas);
        SplitPane.SplitPaneStyle splitStyleV = new SplitPane.SplitPaneStyle(skin.getDrawable("default-splitpane-vertical")),
                splitStyleH = new SplitPane.SplitPaneStyle(skin.getDrawable("default-splitpane"));
        skin.add("default-vertical", splitStyleV);
        skin.add("default-horizontal", splitStyleH);
        List.ListStyle listStyle = new List.ListStyle(DefaultResources.getIncludedFont(), Color.PURPLE, Color.WHITE, skin.getDrawable("selection"));
        skin.add("default", listStyle);

        images = new VerticalGroup();
        images.addActor(new Image(regions.get(rng.nextIntHasty(regions.size))));
        images.addActor(new Image(regions.get(rng.nextIntHasty(regions.size))));
        images.addActor(new Image(regions.get(rng.nextIntHasty(regions.size))));
        images.addActor(new Image(regions.get(rng.nextIntHasty(regions.size))));
        images.addActor(new Image(regions.get(rng.nextIntHasty(regions.size))));
        images.addActor(new Image(regions.get(rng.nextIntHasty(regions.size))));
        images.setSize(128, 250);
        messages = new TextPanel<Color>(null, DefaultResources.getStretchablePrintFont());

        // a bit of a hack to increase the text height slightly without changing the size of the cells they're in.
        // this causes a tiny bit of overlap between cells, which gets rid of an annoying gap between vertical lines.
        // if you use '#' for walls instead of box drawing chars, you don't need this.
        display.setTextSize(cellWidth, cellHeight + 1);

        // this makes animations rather slow, which is good for attack animations.
        display.setAnimationDuration(0.1f);

        //This uses the seeded RNG we made earlier to build a procedural dungeon using a method that takes rectangular
        //sections of pre-drawn dungeon and drops them into place in a tiling pattern. It makes good "ruined" dungeons.
        dungeonGen = new DungeonGenerator(gridWidth, gridHeight, rng);
        //uncomment this next line to randomly add water to the dungeon in pools.
        //dungeonGen.addWater(15);
        //decoDungeon is given the dungeon with any decorations we specified. (Here, we didn't, unless you chose to add
        //water to the dungeon. In that case, decoDungeon will have different contents than bareDungeon, next.)
        decoDungeon = dungeonGen.generate();
        //getBareDungeon provides the simplest representation of the generated dungeon -- '#' for walls, '.' for floors.
        bareDungeon = dungeonGen.getBareDungeon();
        //When we draw, we may want to use a nicer representation of walls. DungeonUtility has lots of useful methods
        //for modifying char[][] dungeon grids, and this one takes each '#' and replaces it with a box-drawing character.
        lineDungeon = DungeonUtility.hashesToLines(decoDungeon);
        // it's more efficient to get random floors from a packed set containing only (compressed) floor positions.
        short[] placement = CoordPacker.pack(bareDungeon, '.');
        //Coord is the type we use as a general 2D point, usually in a dungeon.
        //Because we know dungeons won't be incredibly huge, Coord performs best for x and y values less than 256.
        cursor = Coord.get(-1, -1);
        //player is, here, just a Coord that stores his position. In a real game, you would probably have a class for
        //creatures, and possibly a subclass for the player.
        player = dungeonGen.utility.randomCell(placement);
        //This is used to allow clicks or taps to take the player to the desired area.
        toCursor = new ArrayList<>(100);
        awaitedMoves = new ArrayList<>(100);
        //DijkstraMap is the pathfinding swiss-army knife we use here to find a path to the latest cursor position.
        playerToCursor = new DijkstraMap(decoDungeon, DijkstraMap.Measurement.MANHATTAN);
        bgColor = SColor.DARK_SLATE_GRAY;
        colorIndices = DungeonUtility.generatePaletteIndices(decoDungeon);
        bgColorIndices = DungeonUtility.generateBGPaletteIndices(decoDungeon);
        // this creates an array of sentences, where each imitates a different sort of language or mix of languages.
        // this serves to demonstrate the large amount of glyphs SquidLib supports.
        lang = new String[]
                {
                        FakeLanguageGen.ENGLISH.sentence(5, 10, new String[]{",", ",", ",", ";"},
                                new String[]{".", ".", ".", "!", "?", "..."}, 0.17, gridWidth - 4),
                        FakeLanguageGen.GREEK_AUTHENTIC.sentence(5, 11, new String[]{",", ",", ";"},
                                new String[]{".", ".", ".", "!", "?", "..."}, 0.2, gridWidth - 4),
                        FakeLanguageGen.GREEK_ROMANIZED.sentence(5, 11, new String[]{",", ",", ";"},
                                new String[]{".", ".", ".", "!", "?", "..."}, 0.2, gridWidth - 4),
                        FakeLanguageGen.LOVECRAFT.sentence(3, 9, new String[]{",", ",", ";"},
                                new String[]{".", ".", "!", "!", "?", "...", "..."}, 0.15, gridWidth - 4),
                        FakeLanguageGen.FRENCH.sentence(4, 12, new String[]{",", ",", ",", ";", ";"},
                                new String[]{".", ".", ".", "!", "?", "..."}, 0.17, gridWidth - 4),
                        FakeLanguageGen.RUSSIAN_AUTHENTIC.sentence(6, 13, new String[]{",", ",", ",", ",", ";", " -"},
                                new String[]{".", ".", ".", "!", "?", "..."}, 0.25, gridWidth - 4),
                        FakeLanguageGen.RUSSIAN_ROMANIZED.sentence(6, 13, new String[]{",", ",", ",", ",", ";", " -"},
                                new String[]{".", ".", ".", "!", "?", "..."}, 0.25, gridWidth - 4),
                        FakeLanguageGen.JAPANESE_ROMANIZED.sentence(5, 13, new String[]{",", ",", ",", ",", ";"},
                                new String[]{".", ".", ".", "!", "?", "...", "..."}, 0.12, gridWidth - 4),
                        FakeLanguageGen.SWAHILI.sentence(4, 9, new String[]{",", ",", ",", ";", ";"},
                                new String[]{".", ".", ".", "!", "?"}, 0.12, gridWidth - 4),
                        FakeLanguageGen.SOMALI.sentence(4, 9, new String[]{",", ",", ",", ";", ";"},
                                new String[]{".", ".", ".", "!", "?"}, 0.12, gridWidth - 4),
                        FakeLanguageGen.HINDI_ROMANIZED.sentence(4, 9, new String[]{",", ",", ",", ";", ";"},
                                new String[]{".", ".", ".", "!", "?"}, 0.12, gridWidth - 4),
                        FakeLanguageGen.NORSE.sentence(4, 9, new String[]{",", ",", ",", ";", ";"},
                                new String[]{".", ".", ".", "!", "?"}, 0.12, gridWidth - 4),
                        FakeLanguageGen.INUKTITUT.sentence(4, 9, new String[]{",", ",", ",", ";", ";"},
                                new String[]{".", ".", ".", "!", "?"}, 0.12, gridWidth - 4),
                        FakeLanguageGen.NAHUATL.sentence(4, 9, new String[]{",", ",", ",", ";", ";"},
                                new String[]{".", ".", ".", "!", "?"}, 0.12, gridWidth - 4),
                        FakeLanguageGen.FANTASY_NAME.sentence(4, 8, new String[]{",", ",", ",", ";", ";"},
                                new String[]{".", ".", ".", "!", "?", "..."}, 0.22, gridWidth - 4),
                        FakeLanguageGen.FANCY_FANTASY_NAME.sentence(4, 8, new String[]{",", ",", ",", ";", ";"},
                                new String[]{".", ".", ".", "!", "?", "..."}, 0.22, gridWidth - 4),
                        FakeLanguageGen.FRENCH.mix(FakeLanguageGen.JAPANESE_ROMANIZED, 0.65).sentence(5, 9, new String[]{",", ",", ",", ";"},
                                new String[]{".", ".", ".", "!", "?", "?", "..."}, 0.14, gridWidth - 4),
                        FakeLanguageGen.ENGLISH.addAccents(0.5, 0.15).sentence(5, 10, new String[]{",", ",", ",", ";"},
                                new String[]{".", ".", ".", "!", "?", "..."}, 0.17, gridWidth - 4),
                        FakeLanguageGen.SWAHILI.mix(FakeLanguageGen.JAPANESE_ROMANIZED, 0.5).mix(FakeLanguageGen.FRENCH, 0.35)
                                .mix(FakeLanguageGen.RUSSIAN_ROMANIZED, 0.25).mix(FakeLanguageGen.GREEK_ROMANIZED, 0.2).mix(FakeLanguageGen.ENGLISH, 0.15)
                                .mix(FakeLanguageGen.FANCY_FANTASY_NAME, 0.12).mix(FakeLanguageGen.LOVECRAFT, 0.1)
                                .sentence(5, 10, new String[]{",", ",", ",", ";"},
                                new String[]{".", ".", ".", "!", "?", "..."}, 0.2, gridWidth - 4),
                };
        messages.init(1000, 400, Color.WHITE,
                lang[(langIndex) % lang.length],
                lang[(langIndex + 1) % lang.length],
                lang[(langIndex + 2) % lang.length],
                lang[(langIndex + 3) % lang.length],
                lang[(langIndex + 4) % lang.length],
                lang[(langIndex + 5) % lang.length]);

        // this is a big one.
        // SquidInput can be constructed with a KeyHandler (which just processes specific keypresses), a SquidMouse
        // (which is given an InputProcessor implementation and can handle multiple kinds of mouse move), or both.
        // keyHandler is meant to be able to handle complex, modified key input, typically for games that distinguish
        // between, say, 'q' and 'Q' for 'quaff' and 'Quip' or whatever obtuse combination you choose. The
        // implementation here handles hjkl keys (also called vi-keys), numpad, arrow keys, and wasd for 4-way movement.
        // Shifted letter keys produce capitalized chars when passed to KeyHandler.handle(), but we don't care about
        // that so we just use two case statements with the same body, i.e. one for 'A' and one for 'a'.
        // You can also set up a series of future moves by clicking within FOV range, using mouseMoved to determine the
        // path to the mouse position with a DijkstraMap (called playerToCursor), and using touchUp to actually trigger
        // the event when someone clicks.
        input = new SquidInput(new SquidInput.KeyHandler() {
            @Override
            public void handle(char key, boolean alt, boolean ctrl, boolean shift) {
                switch (key)
                {
                    case SquidInput.UP_ARROW:
                    case 'k':
                    case 'w':
                    case 'K':
                    case 'W':
                    {
                        //-1 is up on the screen
                        move(0, -1);
                        break;
                    }
                    case SquidInput.DOWN_ARROW:
                    case 'j':
                    case 's':
                    case 'J':
                    case 'S':
                    {
                        //+1 is down on the screen
                        move(0, 1);
                        break;
                    }
                    case SquidInput.LEFT_ARROW:
                    case 'h':
                    case 'a':
                    case 'H':
                    case 'A':
                    {
                        move(-1, 0);
                        break;
                    }
                    case SquidInput.RIGHT_ARROW:
                    case 'l':
                    case 'd':
                    case 'L':
                    case 'D':
                    {
                        move(1, 0);
                        break;
                    }
                    case 'Q':
                    case 'q':
                    case SquidInput.ESCAPE:
                    {
                        Gdx.app.exit();
                        break;
                    }
                }
            }
        });//,
        /*
                //The second parameter passed to a SquidInput can be a SquidMouse, which takes mouse or touchscreen
                //input and converts it to grid coordinates (here, a cell is 12 wide and 24 tall, so clicking at the
                // pixel position 15,51 will pass screenX as 1 (since if you divide 15 by 12 and round down you get 1),
                // and screenY as 2 (since 51 divided by 24 rounded down is 2)).
                new SquidMouse(cellWidth, cellHeight, gridWidth, gridHeight, 0, 0, new InputAdapter() {

                    // if the user clicks and there are no awaitedMoves queued up, generate toCursor if it
                    // hasn't been generated already by mouseMoved, then copy it over to awaitedMoves.
                    @Override
                    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
                        if(awaitedMoves.isEmpty()) {
                            if (toCursor.isEmpty()) {
                                cursor = Coord.get(screenX, screenY);
                                //This uses DijkstraMap.findPath to get a possibly long path from the current player position
                                //to the position the user clicked on.
                                toCursor = playerToCursor.findPath(100, null, null, player, cursor);
                            }
                            awaitedMoves = new ArrayList<>(toCursor);
                        }
                        return false;
                    }

                    @Override
                    public boolean touchDragged(int screenX, int screenY, int pointer) {
                        return mouseMoved(screenX, screenY);
                    }

                    // causes the path to the mouse position to become highlighted (toCursor contains a list of points that
                    // receive highlighting). Uses DijkstraMap.findPath() to find the path, which is surprisingly fast.
                    @Override
                    public boolean mouseMoved(int screenX, int screenY) {
                        if(!awaitedMoves.isEmpty())
                            return false;
                        if(cursor.x == screenX && cursor.y == screenY)
                        {
                            return false;
                        }
                        cursor = Coord.get(screenX, screenY);
                        toCursor = playerToCursor.findPath(100, null, null, player, cursor);
                        return false;
                    }
                }));
                */

//        innerSplit = new SplitPane(new StretchContainer(display), new StretchContainer(images), false, skin);
//
//        outerSplit = new SplitPane(innerSplit, new StretchContainer(messages.getScrollPane()), true, skin);

        innerSplit = new SplitPane(display, images, false, skin);

        outerSplit = new SplitPane(innerSplit, messages.getScrollPane(), true, skin);

        //Setting the InputProcessor is ABSOLUTELY NEEDED TO HANDLE INPUT
        Gdx.input.setInputProcessor(new InputMultiplexer(stage, input));
        //You might be able to get by with the next line instead of the above line, but the former is preferred.
        //Gdx.input.setInputProcessor(input);
        // and then add display, our one visual component, to the list of things that act in Stage.
        stage.addActor(outerSplit);

    }
    /**
     * Move the player if he isn't bumping into a wall or trying to go off the map somehow.
     * In a fully-fledged game, this would not be organized like this, but this is a one-file demo.
     * @param xmod
     * @param ymod
     */
    private void move(int xmod, int ymod) {
        int newX = player.x + xmod, newY = player.y + ymod;
        if (newX >= 0 && newY >= 0 && newX < gridWidth && newY < gridHeight
                && bareDungeon[newX][newY] != '#')
        {
            player = player.translate(xmod, ymod);
        }
        // loops through the text snippets displayed whenever the player moves
        langIndex = (langIndex + 1) % lang.length;
    }

    /**
     * Draws the map, applies any highlighting for the path to the cursor, and then draws the player.
     */
    public void putMap()
    {
        for (int i = 0; i < gridWidth; i++) {
            for (int j = 0; j < gridHeight; j++) {
                display.put(i, j, lineDungeon[i][j], colorIndices[i][j], bgColorIndices[i][j], 40);
            }
        }
        for (Coord pt : toCursor)
        {
            // use a brighter light to trace the path to the cursor, from 170 max lightness to 0 min.
            display.highlight(pt.x, pt.y, 100);
        }
        //places the player as an '@' at his position in orange (6 is an index into SColor.LIMITED_PALETTE).
        display.put(player.x, player.y, '@', 6);
        //this helps compatibility with the HTML target, which doesn't support String.format()
        char[] spaceArray = new char[gridWidth];
        Arrays.fill(spaceArray, ' ');
        String spaces = String.valueOf(spaceArray);

        messages.init(1000, 400, Color.WHITE,
                lang[(langIndex) % lang.length],
                lang[(langIndex + 1) % lang.length],
                lang[(langIndex + 2) % lang.length],
                lang[(langIndex + 3) % lang.length],
                lang[(langIndex + 4) % lang.length],
                lang[(langIndex + 5) % lang.length]);
    }
    @Override
    public void render () {
        // standard clear the background routine for libGDX
        Gdx.gl.glClearColor(bgColor.r / 255.0f, bgColor.g / 255.0f, bgColor.b / 255.0f, 1.0f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // need to display the map every frame, since we clear the screen to avoid artifacts.
        putMap();
        // if the user clicked, we have a list of moves to perform.
        if(!awaitedMoves.isEmpty())
        {
            // this doesn't check for input, but instead processes and removes Points from awaitedMoves.
            secondsWithoutMoves += Gdx.graphics.getDeltaTime();
            if (secondsWithoutMoves >= 0.1) {
                secondsWithoutMoves = 0;
                Coord m = awaitedMoves.remove(0);
                toCursor.remove(0);
                move(m.x - player.x, m.y - player.y);
            }
        }
        // if we are waiting for the player's input and get input, process it.
        else if(input.hasNext()) {
            input.next();
        }

        // stage has its own batch and must be explicitly told to draw().
        stage.draw();
        stage.act();
    }

    @Override
    public void resize(int width, int height) {
        super.resize(width, height);
        //very important to have the mouse behave correctly if the user fullscreens or resizes the game!
        input.getMouse().reinitialize((float) width / this.gridWidth, (float)height / (this.gridHeight), this.gridWidth, this.gridHeight, 0, 0);
    }

    public static void main (String[] arg) {
        LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
        config.title = "SquidLib Test: Split";
        config.width = 1000;
        config.height = 650;
        config.addIcon("Tentacle-16.png", Files.FileType.Internal);
        config.addIcon("Tentacle-32.png", Files.FileType.Internal);
        config.addIcon("Tentacle-128.png", Files.FileType.Internal);
        new LwjglApplication(new SplitDemo(), config);
    }
}

