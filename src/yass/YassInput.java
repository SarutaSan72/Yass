/*
 * Yass - Karaoke Editor
 * Copyright (C) 2009 Saruta
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package yass;

import net.java.games.input.Component;
import net.java.games.input.Controller;
import net.java.games.input.ControllerEnvironment;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Enumeration;
import java.util.Vector;

/**
 * Description of the Class
 *
 * @author Saruta
 */
public class YassInput {
    static java.awt.Canvas dummy = new java.awt.Canvas();
    Vector<Controller> controllers = new Vector<>();
    Vector<Component> components = new Vector<>();
    Vector<KeyEvent> events = new Vector<>();
    Vector<float[]> values = new Vector<>();
    Vector<long[]> delays = new Vector<>();
    Vector<KeyListener> listeners = new Vector<>();
    PollThread p = null;


    /**
     * Description of the Method
     */
    public static void init() {
        try {
            // JInput bug: list is never refreshed
            ControllerEnvironment ce = ControllerEnvironment.getDefaultEnvironment();
            ce.getControllers();
        } catch (java.lang.UnsatisfiedLinkError e) {
            System.out.println("No jinput-raw in java.library.path");
        }
    }


    /**
     * The main program for the YassInput class
     *
     * @param argv The command line arguments
     */
    public static void main(String[] argv) {
        final YassInput input = new YassInput();

        Controller c = input.getController(4);
        Component comp = input.getComponent(6, c);
        Component comp2 = input.getComponent(7, c);
        Component comp3 = input.getComponent(8, c);

        input.mapTo(dummy, c, comp, 0, 0, KeyEvent.VK_ENTER, KeyEvent.KEY_LOCATION_UNKNOWN);
        input.mapTo(dummy, c, comp2, 0, 0, KeyEvent.VK_ESCAPE, KeyEvent.KEY_LOCATION_UNKNOWN);
        input.mapTo(dummy, c, comp3, 0, 0, KeyEvent.VK_ENTER, KeyEvent.KEY_LOCATION_NUMPAD);
        input.addKeyListener(
                new KeyAdapter() {
                    public void keyPressed(KeyEvent e) {
                        int code = e.getKeyCode();
                        if (code == KeyEvent.VK_ESCAPE) {
                            System.out.println("ESCAPE pressed");
                            input.stopPoll();
                        }
                        if (code == KeyEvent.VK_ENTER) {
                            int location = e.getKeyLocation();
                            if (location == KeyEvent.KEY_LOCATION_NUMPAD) {
                                System.out.println("NUMPAD ENTER pressed");
                            } else {
                                System.out.println("ENTER pressed");
                            }
                        }
                        e.consume();
                    }


                    public void keyReleased(KeyEvent e) {
                        int code = e.getKeyCode();
                        if (code == KeyEvent.VK_ENTER) {
                            System.out.println("ENTER released");
                        }
                        e.consume();
                    }
                });
        input.startPoll();
    }

    /**
     * Description of the Method
     *
     * @param index Description of the Parameter
     * @return Description of the Return Value
     */
    public static int getButtonController(int index) {
        ControllerEnvironment ce = ControllerEnvironment.getDefaultEnvironment();
        Controller[] cs = ce.getControllers();

        int controllerCount = 0;
        for (int d = 0; d < cs.length; d++) {
            Controller.Type type = cs[d].getType();
            if ((type == Controller.Type.GAMEPAD) || (type == Controller.Type.STICK) || cs[d].getName().toLowerCase().contains("wiimote") || cs[d].getName().toLowerCase().contains("shuttle")) {
                if (controllerCount == index) {
                    return d;
                }
                controllerCount++;
            }
        }
        return -1;
    }

    /**
     * Gets the button attribute of the YassInput class
     *
     * @param d Description of the Parameter
     * @param i Description of the Parameter
     * @return The button value
     */
    public static int getButton(int d, int i) {
        ControllerEnvironment ce = ControllerEnvironment.getDefaultEnvironment();
        Controller[] cs = ce.getControllers();

        int buttonCount = 0;
        Component[] comps = cs[d].getComponents();
        for (int c = 0; c < comps.length; c++) {
            if (!(comps[c].getIdentifier() instanceof Component.Identifier.Axis)) {
                if (buttonCount == i) {
                    return c;
                }
                buttonCount++;
            }
        }
        return -1;
    }

    /**
     * Description of the Method
     */
    public void stopPoll() {
        if (p != null) {
            p.interrupt = true;
        }
    }

    /**
     * Description of the Method
     */
    public void startPoll() {
        if (p != null && !p.interrupt) {
            return;
        }
        p = new PollThread();
        p.start();
    }

    /**
     * Adds a feature to the KeyListener attribute of the YassInput object
     *
     * @param l The feature to be added to the KeyListener attribute
     */
    public void addKeyListener(KeyListener l) {
        listeners.addElement(l);
    }

    /**
     * Description of the Method
     *
     * @param e Description of the Parameter
     */
    public void fireKeyPressed(KeyEvent e) {
        for (Enumeration<KeyListener> en = listeners.elements(); en.hasMoreElements(); ) {
            KeyListener l = en.nextElement();
            l.keyPressed(new KeyEvent((java.awt.Component) e.getSource(), KeyEvent.KEY_PRESSED, System.currentTimeMillis(), e.getModifiers(), e.getKeyCode(), e.getKeyChar(), e.getKeyLocation()));
        }
    }

    /**
     * Description of the Method
     *
     * @param e Description of the Parameter
     */
    public void fireKeyReleased(KeyEvent e) {
        for (Enumeration<KeyListener> en = listeners.elements(); en.hasMoreElements(); ) {
            KeyListener l = en.nextElement();
            l.keyReleased(new KeyEvent((java.awt.Component) e.getSource(), KeyEvent.KEY_RELEASED, System.currentTimeMillis(), e.getModifiers(), e.getKeyCode(), e.getKeyChar(), e.getKeyLocation()));
        }
    }

    /**
     * see http://java.sun.com/j2se/1.4.2/docs/api/java/awt/event/KeyEvent.html
     *
     * @param c         Description of the Parameter
     * @param keycode   Description of the Parameter
     * @param modifiers Description of the Parameter
     * @param location  Description of the Parameter
     * @param d         Description of the Parameter
     * @param id        Description of the Parameter
     * @param source    Description of the Parameter
     */
    public void mapTo(java.awt.Component source, Controller d, Component c, int id, int modifiers, int keycode, int location) {
        controllers.addElement(d);
        components.addElement(c);
        events.addElement(new KeyEvent(source, id, 0, modifiers, keycode, java.awt.AWTKeyStroke.getAWTKeyStroke(keycode, modifiers).getKeyChar(), location));
        values.addElement(new float[]{0});
        delays.addElement(new long[]{-1});
    }

    /**
     * Description of the Method
     *
     * @param modifiers Description of the Parameter
     * @param keycode   Description of the Parameter
     * @param location  Description of the Parameter
     * @param ds        Description of the Parameter
     * @param cs        Description of the Parameter
     * @param id        Description of the Parameter
     * @param source    Description of the Parameter
     */
    public void mapTo(java.awt.Component source, String ds, String cs, int id, int modifiers, int keycode, int location) {
        Controller d = getController(ds);
        if (d == null) {
            return;
        }
        Component c = getComponent(d, cs);
        if (c == null) {
            return;
        }

        mapTo(source, d, c, id, modifiers, keycode, location);
    }

    /**
     * Description of the Method
     */
    public void removeMaps() {
        controllers.removeAllElements();
        components.removeAllElements();
        events.removeAllElements();
    }

    /**
     * Gets the controllerCount attribute of the YassInput object
     *
     * @return The controllerCount value
     */
    public int getControllerCount() {
        ControllerEnvironment ce = ControllerEnvironment.getDefaultEnvironment();
        Controller[] cs = ce.getControllers();
        return cs.length;
    }


    /**
     * Gets the controller attribute of the YassInput class
     *
     * @param i Description of the Parameter
     * @return The controller value
     */
    private Controller getController(int i) {
        ControllerEnvironment ce = ControllerEnvironment.getDefaultEnvironment();
        Controller[] cs = ce.getControllers();
        return cs[i];
    }


    /**
     * Gets the componentCount attribute of the YassInput object
     *
     * @param i Description of the Parameter
     * @return The componentCount value
     */
    public int getComponentCount(int i) {
        Component[] comps = getController(i).getComponents();
        return comps.length;
    }


    private Component getComponent(int i, Controller c) {
        Component[] comps = c.getComponents();
        return comps[i];
    }


    /**
     * Gets the componentName attribute of the YassInput object
     *
     * @param d Description of the Parameter
     * @param c Description of the Parameter
     * @return The componentName value
     */
    public String getComponentName(int d, int c) {
        Component[] comps = getController(d).getComponents();
        return comps[c].getName();
    }


    /**
     * Gets the controllerName attribute of the YassInput object
     *
     * @param d Description of the Parameter
     * @return The controllerName value
     */
    public String getControllerName(int d) {
        return getController(d).getName();
    }


    /**
     * Gets the controllerName attribute of the YassInput object
     *
     * @param d Description of the Parameter
     * @return The controllerName value
     */
    public String getControllerType(int d) {
        return getController(d).getType().toString();
    }


    /**
     * Gets the controllerIndex attribute of the YassInput object
     *
     * @param s Description of the Parameter
     * @return The controllerIndex value
     */
    public int getControllerIndex(String s) {
        ControllerEnvironment ce = ControllerEnvironment.getDefaultEnvironment();
        Controller[] cs = ce.getControllers();
        for (int d = 0; d < cs.length; d++) {
            if (cs[d].getName().equals(s)) {
                return d;
            }
        }
        return -1;
    }


    /**
     * Gets the controller attribute of the YassInput object
     *
     * @param s Description of the Parameter
     * @return The controller value
     */
    public Controller getController(String s) {
        ControllerEnvironment ce = ControllerEnvironment.getDefaultEnvironment();
        Controller[] cs = ce.getControllers();
        for (Controller c : cs) {
            if (c.getName().equals(s)) {
                return c;
            }
        }
        return null;
    }


    /**
     * Gets the componentIndex attribute of the YassInput object
     *
     * @param d Description of the Parameter
     * @param s Description of the Parameter
     * @return The componentIndex value
     */
    public int getComponentIndex(int d, String s) {
        ControllerEnvironment ce = ControllerEnvironment.getDefaultEnvironment();
        Controller[] cs = ce.getControllers();
        Component[] comps = cs[d].getComponents();
        for (int c = 0; c < comps.length; c++) {
            if (comps[c].getName().equals(s)) {
                return c;
            }
        }
        return -1;
    }


    /**
     * Gets the component attribute of the YassInput object
     *
     * @param d Description of the Parameter
     * @param s Description of the Parameter
     * @return The component value
     */
    public Component getComponent(Controller d, String s) {
        Component[] comps = d.getComponents();
        for (Component comp : comps) {
            if (comp.getName().equals(s)) {
                return comp;
            }
        }
        return null;
    }


    /**
     * Description of the Method
     */
    public void pollAllButtons() {
        ControllerEnvironment ce = ControllerEnvironment.getDefaultEnvironment();
        Controller[] cs = ce.getControllers();
        for (Controller cc : cs) {
            Controller.Type type = cc.getType();
            //if (type == Controller.Type.MOUSE || type == Controller.Type.KEYBOARD) {
                // continue;
            //} else
            if (type == Controller.Type.GAMEPAD || type == Controller.Type.STICK) {
                cc.poll();
            } else if (type == Controller.Type.UNKNOWN) {
                cc.poll();
            } else if (cc.getName().toLowerCase().contains("wiimote")) {
                cc.poll();
            }
        }
    }


    /**
     * Gets the value attribute of the YassInput object
     *
     * @param d Description of the Parameter
     * @param c Description of the Parameter
     * @return The value value
     */
    public int getButtonValue(int d, int c) {
        ControllerEnvironment ce = ControllerEnvironment.getDefaultEnvironment();
        Controller[] cs = ce.getControllers();
        Component[] comps = cs[d].getComponents();
        float val = comps[c].getPollData();
        if (comps[c].getIdentifier() instanceof Component.Identifier.Axis) {
            return 0;
        }
        if (val < comps[c].getDeadZone()) {
            return 0;
        }
        return (int) val;
    }


    /**
     * Description of the Method
     *
     * @param d Description of the Parameter
     * @param c Description of the Parameter
     * @return Description of the Return Value
     */
    public String toString(int d, int c) {
        ControllerEnvironment ce = ControllerEnvironment.getDefaultEnvironment();
        Controller[] cs = ce.getControllers();
        Component[] comps = cs[d].getComponents();
        return d + ":" + cs[d].getType() + " - " + comps[c].getName();
    }


    class PollThread extends Thread {
        public boolean interrupt = false;


        /**
         * Constructor for the PollThread object
         */
        public PollThread() {
        }


        public void run() {
            float cur;

            while (!interrupt) {
                try {
                    Thread.sleep(10);
                } catch (Exception ignored) {
                }

                Enumeration<Controller> dEnum = controllers.elements();
                Enumeration<Component> cEnum = components.elements();
                Enumeration<KeyEvent> eEnum = events.elements();
                Enumeration<float[]> vEnum = values.elements();
                Enumeration<long[]> delaysEnum = delays.elements();

                while (dEnum.hasMoreElements()) {
                    Controller d = dEnum.nextElement();
                    Component c = cEnum.nextElement();
                    KeyEvent e = eEnum.nextElement();
                    float value[] = vEnum.nextElement();
                    long delays[] = delaysEnum.nextElement();

                    boolean ok = d.poll();
                    if (ok) {
                        cur = c.getPollData();
                        if (cur != value[0]) {
                            if (cur > value[0]) {
                                fireKeyPressed(e);
                                delays[0] = -50;
                            }
                            if (cur < value[0]) {
                                fireKeyReleased(e);
                                delays[0] = -50;
                            }
                            value[0] = cur;
                        } else {
                            if (cur > 0.1) {
                                if (++delays[0] > 0) {
                                    long currentMillis = System.currentTimeMillis();
                                    if (currentMillis - delays[0] > 100) {
                                        fireKeyPressed(e);
                                        delays[0] = currentMillis;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

