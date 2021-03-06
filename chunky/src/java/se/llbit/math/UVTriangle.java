/* Copyright (c) 2013 Jesper Öqvist <jesper@llbit.se>
 *
 * This file is part of Chunky.
 *
 * Chunky is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Chunky is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with Chunky.  If not, see <http://www.gnu.org/licenses/>.
 */
package se.llbit.math;

/**
 * UV-mapped triangle
 *
 * @author Jesper Öqvist <jesper@llbit.se>
 */
public class UVTriangle {

  /**
   * Normal vector - normal to triangle vertices
   * in counterclockwise order
   */
  public final Vector3 n;

  private final Vector3 a;
  private final Vector3 b;
  private final Vector3 c;

  private final Vector3 b_a;
  private final Vector3 c_a;
  private final Vector3 c_b;
  private final Vector3 a_c;

  private final Vector2 sa;
  private final Vector2 sb;
  private final Vector2 sc;

  private final double d;
  private final double rn;

  /**
   * Construct a new triangle.
   */
  public UVTriangle(Vector3 v0, Vector3 v1, Vector3 v2, Vector2 s0,
      Vector2 s1, Vector2 s2) {

    a = new Vector3(v0);
    b = new Vector3(v1);
    c = new Vector3(v2);

    this.sa = new Vector2(s0);
    this.sb = new Vector2(s1);
    this.sc = new Vector2(s2);

    b_a = new Vector3();
    c_a = new Vector3();
    c_b = new Vector3();
    a_c = new Vector3();

    b_a.sub(b, a);
    c_a.sub(c, a);
    c_b.sub(c, b);
    a_c.sub(a, c);

    n = new Vector3();

    n.cross(b_a, c_a);
    rn = 1.0 / n.length();
    n.normalize();

    d = -n.dot(a);
  }

  /**
   * Find intersection between the ray and this triangle.
   *
   * @return <code>true</code> if the ray intersects the triangle
   */
  public boolean intersect(Ray ray) {
    double px = ray.o.x - QuickMath.floor(ray.o.x + ray.d.x * Ray.OFFSET);
    double py = ray.o.y - QuickMath.floor(ray.o.y + ray.d.y * Ray.OFFSET);
    double pz = ray.o.z - QuickMath.floor(ray.o.z + ray.d.z * Ray.OFFSET);

    // Test that the ray is heading toward the plane.
    double denom = ray.d.dot(n);
    if (denom < -Ray.EPSILON) {

      // Test for intersection with the plane at origin.
      double t = -(px * n.x + py * n.y + pz * n.z + d) / denom;
      if (t > -Ray.EPSILON && t < ray.t) {

        // Calculate plane intersection point.
        px = px + ray.d.x * t;
        py = py + ray.d.y * t;
        pz = pz + ray.d.z * t;

        // Calculate barycentric coordinates.
        double nax = c_b.y * (pz - b.z) - c_b.z * (py - b.y);
        double nay = c_b.z * (px - b.x) - c_b.x * (pz - b.z);
        double naz = c_b.x * (py - b.y) - c_b.y * (px - b.x);

        double nbx = a_c.y * (pz - c.z) - a_c.z * (py - c.y);
        double nby = a_c.z * (px - c.x) - a_c.x * (pz - c.z);
        double nbz = a_c.x * (py - c.y) - a_c.y * (px - c.x);

        double ncx = b_a.y * (pz - a.z) - b_a.z * (py - a.y);
        double ncy = b_a.z * (px - a.x) - b_a.x * (pz - a.z);
        double ncz = b_a.x * (py - a.y) - b_a.y * (px - a.x);

        // alpha, beta, gamma are the barycentric coordinates
        double alpha = (n.x * nax + n.y * nay + n.z * naz) * rn;
        double beta = (n.x * nbx + n.y * nby + n.z * nbz) * rn;
        double gamma = (n.x * ncx + n.y * ncy + n.z * ncz) * rn;

        if (alpha >= 0 && beta >= 0 && gamma >= 0) {

          ray.tNext = t;
          ray.u = alpha * sa.x + beta * sb.x + gamma * sc.x;
          ray.v = alpha * sa.y + beta * sb.y + gamma * sc.y;
          return true;
        }
      }
    }

    return false;
  }

  /**
   * @return Rotated copy of this triangle
   */
  public UVTriangle getYRotated() {
    Transform t = Transform.NONE.rotateY();
    Vector3 ar = new Vector3(a);
    ar.add(-0.5, -0.5, -0.5);
    t.apply(ar);
    ar.add(0.5, 0.5, 0.5);
    Vector3 br = new Vector3(b);
    br.add(-0.5, -0.5, -0.5);
    t.apply(br);
    br.add(0.5, 0.5, 0.5);
    Vector3 cr = new Vector3(c);
    cr.add(-0.5, -0.5, -0.5);
    t.apply(cr);
    cr.add(0.5, 0.5, 0.5);
    return new UVTriangle(ar, br, cr, sa, sb, sc);
  }

}
