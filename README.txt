GLG2D is an effort to translate Graphics2D calls directly into OpenGL calls
and accelerate the Java2D drawing functionality.  The existing OpenGL pipeline
in the Oracle JVM is minimal at best and doesn't use higher-level OpenGL
primitives, like GL_POLYGON and GLU tesselation that make drawing in OpenGL so
fast.

Find more information on http://brandonborkholder.github.com/glg2d/ (for the original JOGL based version) and https://github.com/elfprince13/glg2d (for this hacked together LWJGL based version).

Use cases:
 * use as a drop-in replacement for a JPanel and all Swing children will be
    accelerated
 * draw Swing components in GLContext in your existing application
 * Accelerate other existing Java2D based code (for example, the CSSBox HTML renderer).

This library is licensed under the Apache 2.0 license and both LWJGL and JOGL are licensed and
distributed separately.

How to build

Import your code into Eclipse, or an IDE of your choice. Make sure the dependencies are there. Uses LWJGL, and Slick-Util.