/*
 * Copyright 2013 Brandon Borkholder
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jogamp.glg2d;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.LayoutManager2;
import java.io.Serializable;
import java.util.logging.Logger;

import org.lwjgl.opengl.GLContext;
import javax.swing.JComponent;
import javax.swing.JPopupMenu;
import javax.swing.JViewport;
import javax.swing.RepaintManager;

/**
 * This canvas redirects all paints to an OpenGL canvas. The drawable component
 * can be any JComponent. This is a simple implementation to allow manual
 * painting of a JComponent scene to OpenGL. A {@code G2DGLCanvas} is more
 * appropriate when rendering a complex scene using
 * {@link JComponent#paintComponent(Graphics)} and the {@code Graphics2D}
 * object.
 * 
 * <p>
 * GL drawing can be enabled or disabled using the {@code setGLDrawing(boolean)}
 * method. If GL drawing is enabled, all full paint requests are intercepted and
 * the drawable component is drawn to the OpenGL canvas.
 * </p>
 * 
 * <p>
 * Override {@link #createGLComponent(GLCapabilitiesImmutable, GLContext)} to
 * create the OpenGL canvas. The returned canvas may be a {@code GLJPanel} or a
 * {@code GLCanvas}. {@link #createG2DListener(JComponent)} is used to create
 * the {@code GLEventListener} that will draw to the OpenGL canvas. Use
 * {@link #getGLDrawable()} if you want to attach an {@code Animator}.
 * Otherwise, paints will only happen when requested (either with
 * {@code repaint()} or from AWT).
 * </p>
 */
public class GLG2DCanvas extends JComponent {
	private static final long serialVersionUID = -471481443599019888L;

	GLGraphics2D graphics;

	private JComponent drawableComponent;
	private boolean drawGL;

	/**
	 * Creates a new, blank {@code G2DGLCanvas}.
	 */
	public GLG2DCanvas() {
		graphics = new GLGraphics2D();
		setGLDrawing(true);
		setLayout(new GLOverlayLayout());
		RepaintManager.setCurrentManager(GLAwareRepaintManager.INSTANCE);
	}

	/**
	 * Creates a new {@code G2DGLCanvas} where {@code drawableComponent} fills the
	 * canvas.
	 */
	public GLG2DCanvas(JComponent drawableComponent) {
		this();
		setDrawableComponent(drawableComponent);
	}

	/**
	 * Returns {@code true} if the {@code drawableComonent} is drawn using OpenGL
	 * libraries. If {@code false}, it is using normal Java2D drawing routines.
	 */
	public boolean isGLDrawing() {
		return drawGL;
	}

	/**
	 * Sets the drawing path, {@code true} for OpenGL, {@code false} for normal
	 * Java2D.
	 * 
	 * @see #isGLDrawing()
	 */
	public void setGLDrawing(boolean drawGL) {
		if (this.drawGL != drawGL) {
			this.drawGL = drawGL;
			setOpaque(drawGL);

			firePropertyChange("gldrawing", !drawGL, drawGL);

			repaint();
		}
	}

	/**
	 * Gets the {@code JComponent} to be drawn to the OpenGL canvas.
	 */
	public JComponent getDrawableComponent() {
		return drawableComponent;
	}

	/**
	 * Sets the {@code JComponent} that will be drawn to the OpenGL canvas.
	 */
	public void setDrawableComponent(JComponent component) {
		if (component == drawableComponent) {
			return;
		}

		if (drawableComponent != null) {
			remove(drawableComponent);
		}

		drawableComponent = component;
		if (drawableComponent != null) {
			verifyHierarchy(drawableComponent);
			add(drawableComponent);
			drawableComponent.setLocation(0,0);
			drawableComponent.setSize(getWidth(),getHeight());
		}
		validate();
	}

	/**
	 * Checks the component and all children to ensure that everything is pure
	 * Swing. We can only draw lightweights.
	 * 
	 * 
	 * We'll also set PopupMenus to heavyweight and fix JViewport blitting.
	 */
	protected void verifyHierarchy(Component comp) {
		JPopupMenu.setDefaultLightWeightPopupEnabled(false);

		if (comp instanceof JComponent) {
			((JComponent) comp).setDoubleBuffered(false);
		}

		if (!(comp instanceof JComponent)) {
			Logger.getLogger(GLG2DCanvas.class.getName()).warning("Drawable component and children should be pure Swing: " +
					comp + " does not inherit JComponent");
		}

		if (comp instanceof JViewport) {
			((JViewport) comp).setScrollMode(JViewport.SIMPLE_SCROLL_MODE);
		}

		if (comp instanceof Container) {
			Container cont = (Container) comp;
			for (int i = 0; i < cont.getComponentCount(); i++) {
				verifyHierarchy(cont.getComponent(i));
			}
		}
	}

	/**
	 * Gets the {@code GLGraphics2D} used for drawing.
	 * 
	 * </p>
	 */
	public GLGraphics2D getGraphics() {
		return graphics;
	}


	@Override
	public void paint(Graphics g) {
		if (isGLDrawing() && drawableComponent != null && graphics != null) {
			((GLGraphics2D)g).prePaint();
			drawableComponent.paint(graphics);
			((GLGraphics2D)g).postPaint();
		} else {
			super.paint(g);
		}
	}

	@Override
	protected void paintChildren(Graphics g) {
		/*
		 * Don't paint the drawableComponent. If we'd use a GLJPanel instead of a
		 * GLCanvas, we'd have to paint it here.
		 */
		if (!isGLDrawing()) {
			super.paintChildren(g);
		}
	}

	@Override
	protected void addImpl(Component comp, Object constraints, int index) {
		if (comp == drawableComponent) {
			super.addImpl(comp, constraints, index);
		} else {
			throw new IllegalArgumentException("Do not add component to this. Add them to the object in getDrawableComponent()");
		}
	}

	/**
	 * Implements a simple layout where all the components are the same size as
	 * the parent.
	 */
	protected static class GLOverlayLayout implements LayoutManager2, Serializable {
		private static final long serialVersionUID = -8248213786715565045L;

		@Override
		public Dimension preferredLayoutSize(Container parent) {
			if (parent.isPreferredSizeSet() || parent.getComponentCount() == 0) {
				return parent.getPreferredSize();
			} else {
				int x = -1, y = -1;
				for (Component child : parent.getComponents()) {
					Dimension dim = child.getPreferredSize();
					x = Math.max(dim.width, x);
					y = Math.max(dim.height, y);
				}

				return new Dimension(x, y);
			}
		}

		@Override
		public Dimension minimumLayoutSize(Container parent) {
			if (parent.getComponentCount() == 0) {
				return new Dimension(0, 0);
			} else {
				int x = Integer.MAX_VALUE, y = Integer.MAX_VALUE;
				for (Component child : parent.getComponents()) {
					Dimension dim = child.getMinimumSize();
					x = Math.min(dim.width, x);
					y = Math.min(dim.height, y);
				}

				return new Dimension(x, y);
			}
		}

		@Override
		public Dimension maximumLayoutSize(Container parent) {
			if (parent.getComponentCount() == 0) {
				return new Dimension(0, 0);
			} else {
				int x = -1, y = -1;
				for (Component child : parent.getComponents()) {
					Dimension dim = child.getMaximumSize();
					x = Math.max(dim.width, x);
					y = Math.max(dim.height, y);
				}

				return new Dimension(x, y);
			}
		}

		@Override
		public void layoutContainer(Container parent) {
			for (Component child : parent.getComponents()) {
				child.setSize(parent.getSize());
			}
		}

		@Override
		public void addLayoutComponent(String name, Component comp) {
			// nop
		}

		@Override
		public void addLayoutComponent(Component comp, Object constraints) {
			// nop
		}

		@Override
		public void removeLayoutComponent(Component comp) {
			// nop
		}

		@Override
		public void invalidateLayout(Container target) {
			// nop
		}

		@Override
		public float getLayoutAlignmentX(Container target) {
			return 0.5f;
		}

		@Override
		public float getLayoutAlignmentY(Container target) {
			return 0.5f;
		}
	}
}
