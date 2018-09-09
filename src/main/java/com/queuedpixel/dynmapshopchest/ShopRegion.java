/*

dynmap-shopchest : Display ShopChest Shops on Dynmap

Copyright (c) 2018 Queued Pixel <git@queuedpixel.com>

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.

*/

package com.queuedpixel.dynmapshopchest;

import de.epiceric.shopchest.shop.Shop;
import org.bukkit.Location;

import java.util.LinkedList;
import java.util.List;

public class ShopRegion
{
    static int regionPadding = 0; // padding (in blocks) between region border and shops

    String world;
    int xLeft;
    int zTop;
    int xRight;
    int zBottom;
    List< Shop > shops = new LinkedList<>();

    void addShop( Shop shop )
    {
        this.shops.add( shop );
    }

    void resize()
    {
        // don't resize if there are no shops
        if ( this.shops.size() < 1 ) return;

        // get the smallest and largest x and z coordinates from all shops
        String world = null;
        int xMin = Integer.MAX_VALUE;
        int zMin = Integer.MAX_VALUE;
        int xMax = Integer.MIN_VALUE;
        int zMax = Integer.MIN_VALUE;
        for ( Shop shop : this.shops )
        {
            Location location = shop.getLocation();

            // verify that all shops are in the same world
            if ( world == null ) world = location.getWorld().getName();
            else if ( !world.equals( location.getWorld().getName() ))
            {
                throw new IllegalStateException( "Not all shops in same world." );
            }

            // update our min and max values
            if ( location.getBlockX() < xMin ) xMin = location.getBlockX();
            if ( location.getBlockZ() < zMin ) zMin = location.getBlockZ();
            if ( location.getBlockX() > xMax ) xMax = location.getBlockX();
            if ( location.getBlockZ() > zMax ) zMax = location.getBlockZ();
        }

        // update region size
        this.world   = world;
        this.xLeft   = xMin - ShopRegion.regionPadding;
        this.zTop    = zMin - ShopRegion.regionPadding;
        this.xRight  = xMax + ShopRegion.regionPadding;
        this.zBottom = zMax + ShopRegion.regionPadding;
    }
}
