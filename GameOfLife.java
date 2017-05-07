import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.io.*;
import javax.imageio.*;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class GameOfLife
{
    private static final int WIDTH = 960;
    private static final int HEIGHT = 960;

    public static void main( String[] args ) {
        SwingUtilities.invokeLater( new Runnable() {
            public void run() {
                createAndShowGUI();
            }
        } );
    }

    private static void createAndShowGUI()
    {
        JFrame frame = new ImageFrame( WIDTH, HEIGHT);
        frame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
        frame.setVisible( true );
    }
}

class ImageFrame extends JFrame
{
    private BufferedImage image = null;
    private Graphics2D g2d = null;
    private Timer timer;
    private int[][] cellGrid;
    private boolean isRunning = false;

    static private final int BLACK = 0xFF000000;
    static private final int RED   = 0xFFFF0000;
    static private final int GREEN = 0xFF00FF00;
    static private final int BLUE  = 0xFF0000FF;

    ImageIcon startIcon, pauseIcon, clearIcon;

    public ImageFrame( int width, int height)
    {
        this.setTitle( "Conway's Game of Life" );
        this.setSize( width, height );
        addComponents();

        timer = new Timer(150, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                timer.stop();
                incrementGeneration();
                drawCellGrid();
                timer.restart();
            }
        });     // start & stop timer using button components

        cellGrid = new int[image.getWidth() / 8][image.getHeight() / 8];
        empty_World();
    }

    // map indices to a valid location
    int[] mapSteps(int x_loc, int y_loc, int x_step, int y_step) {

        x_loc += x_step;		// x coordinate before potentially mapping to valid location
        y_loc += y_step; 		// y coordinate before potentially mapping to valid location

        // out of bounds, wrap around
        if( x_loc >= cellGrid.length || y_loc >= cellGrid.length || x_loc < 0 || y_loc < 0 ) {
            if(x_loc >= cellGrid.length)            x_loc = 0;
            if(x_loc < 0)      	                    x_loc = cellGrid.length - 1;
            if(y_loc >= cellGrid.length)            y_loc = 0;
            if(y_loc < 0)         	                y_loc = cellGrid.length - 1;
        }

        int[] mappedStep = { x_loc, y_loc };	 // return the mapped location
        return mappedStep;
    }

    private int getNumNeighbors(int x, int y) {     // count adjacent particles
        int numNeigbors = 0;
        int adj_x, adj_y;                           // coordinates of the adjacent particle
        for(int n = -1; n <= 1; n++) {
            for(int m = -1; m <= 1; m++) {
                int x_step = n;             // steps that will land us on adjacent pixels
                int y_step = m;
                int[] mappedSteps = mapSteps( x, y, x_step, y_step );
                adj_x = mappedSteps[0];	// x coordinate of adjacent pixel
                adj_y = mappedSteps[1];	// y coordinate of adjacent pixel

                if( !(x == adj_x && y == adj_y) && (cellGrid[adj_y][adj_x] == 1 || cellGrid[adj_y][adj_x] == 2) )
                    numNeigbors++;  // increment number of neighbors
            }
        }
        return numNeigbors;
    }

    private void clearCellGrid() {

        for(int i = 0; i < cellGrid.length; i++)
            for(int j = 0; j < cellGrid[i].length; j++)
                    cellGrid[i][j] = 0;

    }

    private void updateGridCell() { // update values after user changes anything with clicks

        for(int i = 0; i < cellGrid.length; i++) {
            for(int j = 0; j < cellGrid[i].length; j++) {
                if(image.getRGB(j*8, i*8) == GREEN)
                    cellGrid[i][j] = 1;
                else if (image.getRGB(j*8, i*8) == RED)
                    cellGrid[i][j] = -1;
                else if (image.getRGB(j*8, i*8) == BLACK)
                    cellGrid[i][j] = 0;
            }
        }

    }

    private void incrementGeneration() {

        updateGridCell();
        int[][] temp = new int[cellGrid.length][cellGrid.length];
        int numNeighbors = 0;
        for(int i = 0; i < cellGrid.length; i++) {
            for(int j = 0; j < cellGrid[i].length; j++) {
                numNeighbors = getNumNeighbors(j, i);
                // cell dies
                if( !( cellGrid[i][j] == 0 || cellGrid[i][j] == -1 ) && (numNeighbors < 2 || numNeighbors > 3) ) {
                    temp[i][j] = -1;
                }
                // cell lives
                else if((cellGrid[i][j] == 1 || cellGrid[i][j] == 2 ) && (numNeighbors == 2 || numNeighbors == 3)) {
                    if(cellGrid[i][j] == 1) {
                        temp[i][j] = 2;
                    }
                    else if(cellGrid[i][j] == 2) {
                        temp[i][j] = 2;
                    }
                }
                // dead cell comes to life
                else if( (cellGrid[i][j] == -1 || cellGrid[i][j] == 0 ) && (numNeighbors == 3)) {
                    temp[i][j] = 1;
                }
            }
        }

        for(int i = 0; i < cellGrid.length; i++)
            for(int j = 0; j < cellGrid[i].length; j++)
                cellGrid[i][j] = temp[i][j];

    }

    private void empty_World() {
        clearCellGrid();
        drawCellGrid();
    }

    private void drawCellGrid() {
        for(int i = 0; i < cellGrid.length; i++) {
            for(int j = 0; j < cellGrid[i].length; j++) {

                if( cellGrid[i][j] ==  0 )
                    g2d.setColor( Color.BLACK );
                else if ( cellGrid[i][j] == -1 )
                    g2d.setColor( new Color(RED) );
                else if ( cellGrid[i][j] == 1 )
                    g2d.setColor( Color.GREEN );
                else if ( cellGrid[i][j] == 2 )
                    g2d.setColor( new Color(0xff0093D1) );

                g2d.fillRect( j * 8 , i * 8, 8, 8 );
            }
        }
        repaint();
    }

    private void addComponents() {
        image = new BufferedImage(960, 960, BufferedImage.TYPE_INT_ARGB);
        g2d = (Graphics2D) image.createGraphics();
        g2d.setRenderingHint( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON );

        try {
            startIcon = new ImageIcon(  ImageIO.read(GameOfLife.class.getClass().getResourceAsStream("/images/play.png")) );
            pauseIcon = new ImageIcon(  ImageIO.read(GameOfLife.class.getClass().getResourceAsStream("/images/pause.png")) );
            clearIcon = new ImageIcon(  ImageIO.read(GameOfLife.class.getClass().getResourceAsStream("/images/clear.png")) );
        } catch (IOException e) {
            e.printStackTrace();
        }

        JButton clearButton = new JButton();
        clearButton.setIcon(clearIcon);
        clearButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                empty_World();
            }
        });

        JButton playButton = new JButton();
        playButton.setIcon(startIcon);
        playButton.addActionListener( new ActionListener()
        {
            public void actionPerformed( ActionEvent event )
            {
                if(!isRunning) {      // if not isRunning, start isRunning
                    isRunning ^= true;
                    playButton.setIcon(pauseIcon);
                    timer.start();
                }
                else {
                    isRunning ^= true; // if isRunning, stop isRunning
                    playButton.setIcon(startIcon);
                    timer.stop();
                }
                repaint();
            }
        } );

        JSlider slider = new JSlider(1, 100, 50);
        slider.setMinorTickSpacing(5);
        slider.setSnapToTicks(true);
        slider.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {

                timer.stop();

                int newSpeed = (slider.getMaximum() - slider.getValue()) * 3;

                timer = new Timer( newSpeed, new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        timer.stop();
                        incrementGeneration();
                        drawCellGrid();
                        timer.restart();
                    }
                }  );
                if(isRunning)
                    timer.start();
            }
        });

        this.getContentPane().add( new MousePanel( image, playButton, clearButton, slider ), BorderLayout.CENTER );    // add panel to frame
    }
}

class MousePanel extends JLayeredPane
{
    static private final int BLACK = 0xFF000000;
    static private final int GREEN = 0xFF00FF00;
    static private final int RED = 0xffff0000;
    static private final int BLUE = 0xFF0000FF;

    private BufferedImage image;
    private Graphics2D g2d;
    private final int WIDTH;
    private final int HEIGHT;

    public MousePanel( BufferedImage image, JButton playButton, JButton clearButton, JSlider slider)
    {
        this.image = image;
        WIDTH = image.getWidth();
        HEIGHT = image.getHeight();
        setBounds(0, 0, WIDTH, HEIGHT);

        g2d = image.createGraphics();

        this.add(slider, new Integer(3));
        slider.setOpaque(true);
        slider.setFocusable(false);
        slider.setBounds(960/2 - 270, 660, 250, 30  );
        slider.setBackground( new Color(0xff0093D1));
        
        this.add(clearButton, new Integer(3));
        clearButton.setOpaque(false);
        clearButton.setContentAreaFilled(false);
        clearButton.setBorderPainted(false);
        clearButton.setBounds(960/2 + 55, 610, 128 + 10, 128  );

        this.add(playButton, new Integer(3));
        playButton.setOpaque(false);
        playButton.setContentAreaFilled(false);
        playButton.setBorderPainted(false);
        playButton.setBounds(960/2 - 128/2 + 30, 610, 128 + 10, 128  );

        addMouseListener( new MouseAdapter()
        {
            public void mousePressed( MouseEvent event )
            {
                Point point = event.getPoint();
                if (  isARGBColor( point, BLACK ) || isARGBColor( point, RED ) ) {
                    drawSquare( point, Color.GREEN );
                }

                else if (  isARGBColor( point, GREEN ) || isARGBColor( point, BLUE ) ) {
                    drawSquare( point, Color.BLACK );
                }
            }
        } );

        addMouseMotionListener( new MouseMotionListener()
        {
            public void mouseDragged(MouseEvent event) {
                Point point = event.getPoint();
                if (  isARGBColor( point, BLACK ) || isARGBColor( point, RED ) )
                    drawSquare( point, Color.GREEN );
            }

            // set the mouse cursor to cross hairs if it is inside a black rectangle
            public void mouseMoved(MouseEvent event) {
                Point point = event.getPoint();
                if ( isARGBColor( point, BLACK ) )
                    setCursor( Cursor.getDefaultCursor() );
                else
                    setCursor( Cursor.getPredefinedCursor (Cursor.CROSSHAIR_CURSOR) );
            }
        });
    }

    private boolean isARGBColor( Point p, int argb )
    {
        return (image.getRGB( p.x, p.y ) == argb );
    }

    private void drawSquare( Point point, Color color )
    {
        int x = ((point.x >> 3) << 3);
        int y = ((point.y >> 3) << 3);
        g2d.setColor( color );
        g2d.fillRect( x, y, 8, 8 );
        repaint();
    }

    public void paintComponent( Graphics g )
    {
        super.paintComponent( g );
        g.drawImage( image, 0, 0, null );
    }
}

