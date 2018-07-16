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

import de.epiceric.shopchest.ShopChest;
import de.epiceric.shopchest.event.ShopInitializedEvent;
import de.epiceric.shopchest.language.LanguageUtils;
import de.epiceric.shopchest.shop.Shop;
import de.epiceric.shopchest.utils.ItemUtils;
import de.epiceric.shopchest.utils.Utils;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

public class DynmapShopchestPlugin extends JavaPlugin implements Listener
{
    private ShopChest shopChest;
    private Set< Location > locations = new HashSet<>();
    private Collection< ShopRegion > shopRegions = new LinkedList<>();

    public void onEnable()
    {
        this.shopChest = (ShopChest) this.getServer().getPluginManager().getPlugin( "ShopChest" );
        this.getServer().getPluginManager().registerEvents( this, this );
    }

    public void onDisable()
    {
    }

    @EventHandler
    public void onShopInitializedEvent( ShopInitializedEvent event )
    {
        for ( Shop shop : this.shopChest.getShopUtils().getShops() )
        {
            if ( !this.locations.contains( shop.getLocation() ))
            {
                this.locations.add( shop.getLocation() );

                String enchantments =
                        LanguageUtils.getEnchantmentString( ItemUtils.getEnchantments( shop.getProduct()) );
                int inventory = Utils.getAmount( shop.getInventoryHolder().getInventory(), shop.getProduct() );
                int freeSpace = Utils.getFreeSpaceForItem( shop.getInventoryHolder().getInventory(), shop.getProduct() );
                this.getLogger().info(
                        "Shop " +
                        "- world: "        + shop.getLocation().getWorld().getName() +
                        ", x: "            + shop.getLocation().getBlockX() +
                        ", y: "            + shop.getLocation().getBlockY() +
                        ", z: "            + shop.getLocation().getBlockZ() +
                        ", type: "         + shop.getShopType() +
                        ", vendor: "       + shop.getVendor().getName() +
                        ", item: "         + LanguageUtils.getItemName( shop.getProduct() ) +
                        ", enchantments: " + enchantments +
                        ", amount: "       + shop.getProduct().getAmount() +
                        ", buy price: "    + shop.getBuyPrice() +
                        ", sell price: "   + shop.getSellPrice() +
                        ", inventory: "    + inventory +
                        ", free space: "   + freeSpace );

                ShopRegion shopRegion = this.getShopRegion( shop );
                shopRegion.addShop( shop );
                this.checkOverlappingShopRegions( shopRegion );
            }
        }

        this.getLogger().info( "Regions:" );

        for ( ShopRegion shopRegion : this.shopRegions )
        {
            this.getLogger().info(
                    "Shop Region " +
                    "- world: "    + shopRegion.world +
                    ", xLeft: "    + shopRegion.xLeft +
                    ", zTop: "     + shopRegion.zTop +
                    ", xRight: "   + shopRegion.xRight +
                    ", zBottom: "  + shopRegion.zBottom +
                    ", count: "    + shopRegion.shops.size() );
        }
    }

    private ShopRegion getShopRegion( Shop shop )
    {
        for ( ShopRegion shopRegion : this.shopRegions )
        {
            if (( shopRegion.world.equals( shop.getLocation().getWorld().getName() )) &&
                ( shopRegion.xLeft   <= shop.getLocation().getBlockX() ) &&
                ( shopRegion.xRight  >= shop.getLocation().getBlockX() ) &&
                ( shopRegion.zTop    <= shop.getLocation().getBlockZ() ) &&
                ( shopRegion.zBottom >= shop.getLocation().getBlockZ() ))
            {
                return shopRegion;
            }
        }

        ShopRegion shopRegion = new ShopRegion();
        this.shopRegions.add( shopRegion );
        return shopRegion;
    }

    private void checkOverlappingShopRegions( ShopRegion shopRegion )
    {
        Collection< ShopRegion > overlappingShopRegions = new LinkedList<>();

        for ( ShopRegion otherShopRegion : this.shopRegions )
        {
            // don't check for an overlap with the same region
            if ( otherShopRegion == shopRegion ) continue;

            boolean xOverlap =
                    // left X of other region is inside this region
                    ((( shopRegion.xLeft  <= otherShopRegion.xLeft  ) &&
                      ( shopRegion.xRight >= otherShopRegion.xLeft  )) ||

                     // right X of other region is inside this region
                     (( shopRegion.xLeft  <= otherShopRegion.xRight ) &&
                      ( shopRegion.xRight >= otherShopRegion.xRight )) ||

                     // both left and right X of this region are inside other region
                     (( otherShopRegion.xLeft  <= shopRegion.xLeft  ) &&
                      ( otherShopRegion.xRight >= shopRegion.xLeft  ) &&
                      ( otherShopRegion.xLeft  <= shopRegion.xRight ) &&
                      ( otherShopRegion.xRight >= shopRegion.xRight )));

            boolean zOverlap =
                    // top Z of other region is inside this region
                    ((( shopRegion.zTop    <= otherShopRegion.zTop    ) &&
                      ( shopRegion.zBottom >= otherShopRegion.zTop    )) ||

                     // bottom Z of other region is inside this region
                     (( shopRegion.zTop    <= otherShopRegion.zBottom ) &&
                      ( shopRegion.zBottom >= otherShopRegion.zBottom )) ||

                     // both top and bottom Z of this region are inside other region
                     (( otherShopRegion.zTop    <= shopRegion.zTop    ) &&
                      ( otherShopRegion.zBottom >= shopRegion.zTop    ) &&
                      ( otherShopRegion.zTop    <= shopRegion.zBottom ) &&
                      ( otherShopRegion.zBottom >= shopRegion.zBottom )));

            // two regions overlap only if they overlap in the both the X and the Z dimension, and are in the same world
            if (( xOverlap ) && ( zOverlap ) && ( shopRegion.world.equals( otherShopRegion.world )))
            {
                overlappingShopRegions.add( otherShopRegion );
            }
        }

        // merge and remove overlapping regions
        for ( ShopRegion overlappingShopRegion : overlappingShopRegions )
        {
            this.shopRegions.remove( overlappingShopRegion );

            for ( Shop shop : overlappingShopRegion.shops )
            {
                shopRegion.addShop( shop );
            }
        }
    }
}
