package base.workbench;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

import javax.swing.JComponent;
import javax.swing.JDesktopPane;

/**
 * See http://stackoverflow.com/a/12389479/909085
 */
public class ModuleNetworkGlasspane extends JComponent {

	private static final long serialVersionUID = -1423113285724582925L;
	private ConcurrentHashMap<ModuleInputPortButton, ModuleOutputPortButton> linked; // InputPortButton - OutputPortButton
	private JDesktopPane desktopPane;
	private AbstractModulePortButton activeLinkingPortButton = null;
	private Color linkColor = new Color(0,255,0);
	private Color linkEndColor = new Color(0,155,0);

    public ModuleNetworkGlasspane (JDesktopPane desktopPane)
    {
        super ();
        this.linked = new ConcurrentHashMap<ModuleInputPortButton, ModuleOutputPortButton> ();
        this.desktopPane = desktopPane;
    }

    public void link ( ModuleInputPortButton inputButton, ModuleOutputPortButton outputButton )
    {
    	this.linked.put ( inputButton, outputButton );
        repaint ();
        this.desktopPane.repaint();
    }

    public void unlink ( ModuleInputPortButton inputButton )
    {
    	this.linked.remove(inputButton);
        repaint ();
        this.desktopPane.repaint();
    }

    public void unlink ( final ModuleOutputPortButton outputButton )
    {
    	// Remove all links targeting the specified output button
    	this.linked.values().removeIf(new Predicate<ModuleOutputPortButton> () {

			@Override
			public boolean test(ModuleOutputPortButton t) {
				return t.equals(outputButton);
			}});
    	
    	// Repaint visuals
        repaint ();
        this.desktopPane.repaint();
    }

    protected void paintComponent ( Graphics g )
    {
        Graphics2D g2d = ( Graphics2D ) g;
        g2d.setStroke(new BasicStroke(3));
        g2d.setRenderingHint ( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON );

        
        for ( JComponent c1 : linked.keySet () )
        {
        	Rectangle r1 = getBoundsInWindow ( c1 );
        	Rectangle r2 = getBoundsInWindow ( this.linked.get ( c1 ) );
            Point p1 = getRectCenter ( r1 );
            Point p2 = getRectCenter ( r2 );
            int xoffset = 10;
            int x1 = r1.x + xoffset;
            int x2 = r2.x + r2.width-xoffset;
            if (ModuleOutputPortButton.class.isAssignableFrom(c1.getClass())){
                x2 = r2.x + xoffset;
                x1 = r1.x + r1.width-xoffset;
            }
            g2d.setPaint ( this.linkColor );
            g2d.drawLine ( x1, p1.y, x2, p2.y );
            g2d.setPaint ( this.linkEndColor );
            g2d.drawOval(x1-3, p1.y-3, 6, 6);
            g2d.drawOval(x2-3, p2.y-3, 6, 6);
        }
        
        // Draw link from selected button to mouse cursor during linking activity
        /*if (this.activeLinkingPortButton != null){
        	g2d.setPaint ( Color.RED );
        	Point p1 = getRectCenter ( getBoundsInWindow ( this.activeLinkingPortButton ) );
        	g2d.drawLine ( p1.x, p1.y, MouseInfo.getPointerInfo().getLocation().x, MouseInfo.getPointerInfo().getLocation().y );
        }*/
    }

    private Point getRectCenter ( Rectangle rect )
    {
        return new Point ( rect.x + rect.width / 2, rect.y + rect.height / 2 );
    }

    private Rectangle getBoundsInWindow ( Component component )
    {
        return getRelativeBounds ( component, this.desktopPane.getRootPane() );
    }

    private Rectangle getRelativeBounds ( Component component, Component relativeTo )
    {
        return new Rectangle ( getRelativeLocation ( component, relativeTo ),
                component.getSize () );
    }

    private Point getRelativeLocation ( Component component, Component relativeTo )
    {
        Point los = component.getLocationOnScreen ();
        Point rt = relativeTo.getLocationOnScreen ();
        return new Point ( los.x - rt.x, los.y - rt.y );
    }

    public boolean contains ( int x, int y )
    {
        return false;
    }

	/**
	 * @return the activeLinkingPortButton
	 */
	public AbstractModulePortButton getActiveLinkingPortButton() {
		return activeLinkingPortButton;
	}

	/**
	 * @param activeLinkingPortButton the activeLinkingPortButton to set
	 */
	public void setActiveLinkingPortButton(AbstractModulePortButton activeLinkingPortButton) {
		this.activeLinkingPortButton = activeLinkingPortButton;
	}
}
