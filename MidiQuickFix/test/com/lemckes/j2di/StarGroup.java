
package com.lemckes.j2di;

import java.awt.Color;

/**
 *
 */
public class StarGroup
    extends IGroup {

    public StarGroup(ILayer l) {
        super(l);
        IPolygon star1 = new IStar(5, 2, 20, -Math.PI / 2);
        star1.setPaint(new Color(0.9f, 0.6f, 0.2f));
        star1.setPosition(20, 160);
        star1.setFilled(true);
        add(star1);

        IPolygon star2 = new IStar(6, 2, 20, -Math.PI / 2);
        star2.setPaint(new Color(0.6f, 0.9f, 0.2f));
        star2.setPosition(60, 160);
        star2.setFilled(false);
        add(star2);

        IPolygon star3 = new IStar(7, 3, 20, -Math.PI / 2);
        star3.setPaint(new Color(0.6f, 0.9f, 0.2f));
        star3.setPosition(100, 160);
        star3.setFilled(false);
        add(star3);

        IPolygon star4 = new IStar(8, 3, 20, -Math.PI / 2);
        star4.setPaint(new Color(0.6f, 0.9f, 0.2f));
        star4.setPosition(140, 160);
        star4.setFilled(false);
        add(star4);

        IPolygon star5 = new IStar(9, 4, 20, -Math.PI / 2);
        star5.setPaint(new Color(0.6f, 0.9f, 0.2f));
        star5.setPosition(20, 210);
        star5.setFilled(false);
        add(star5);

        IPolygon star6 = new IStar(19, 9, 20, -Math.PI / 2);
        star6.setPaint(new Color(0.6f, 0.9f, 0.2f));
        star6.setPosition(60, 210);
        star6.setFilled(false);
        add(star6);

        IPolygon star7 = new IStar(29, 14, 20, -Math.PI / 2);
        star7.setPaint(new Color(0.6f, 0.9f, 0.2f));
        star7.setPosition(100, 210);
        star7.setFilled(false);
        add(star7);

        IPolygon star8 = new IStar(29, 13, 20, -Math.PI / 2);
        star8.setPaint(new Color(0.6f, 0.9f, 0.2f));
        star8.setPosition(140, 210);
        star8.setFilled(false);
        add(star8);
    }
}
