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
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.dynmap.DynmapCommonAPI;
import org.dynmap.markers.AreaMarker;
import org.dynmap.markers.MarkerAPI;
import org.dynmap.markers.MarkerSet;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
import java.util.UUID;

public class DynmapShopchestPlugin extends JavaPlugin implements Listener
{
    private String formatString;
    private MarkerSet markerSet;
    private ShopChest shopChest;
    private Set< Location > locations = new HashSet<>();
    private Collection< ShopRegion > shopRegions = new LinkedList<>();

    public void onEnable()
    {
        RegisteredServiceProvider< Economy > rsp =
                        Bukkit.getServer().getServicesManager().getRegistration( Economy.class );
        Economy economy = rsp.getProvider();
        this.formatString = "%." + economy.fractionalDigits() + "f";

        this.shopChest = (ShopChest) this.getServer().getPluginManager().getPlugin( "ShopChest" );
        this.getServer().getPluginManager().registerEvents( this, this );
        DynmapCommonAPI dynmapApi = (DynmapCommonAPI) this.getServer().getPluginManager().getPlugin( "dynmap" );
        MarkerAPI markerApi = dynmapApi.getMarkerAPI();
        this.markerSet = markerApi.createMarkerSet( "ShopChest", "ShopChest", null, false );
        this.markerSet.setLayerPriority( 10 );
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
                shopRegion.resize();
                this.checkOverlappingShopRegions( shopRegion );
            }
        }

        this.getLogger().info( "Regions:" );

        for ( ShopRegion shopRegion : this.shopRegions )
        {
            StringBuilder builder = new StringBuilder();
            builder.append( "<table>" );
            builder.append( "<tr>" );
            builder.append( "<th>" );
            builder.append( "Item" );
            builder.append( "</th>" );
            builder.append( "<th>" );
            builder.append( "Amount" );
            builder.append( "</th>" );
            builder.append( "<th>" );
            builder.append( "Buy Price" );
            builder.append( "</th>" );
            builder.append( "<th>" );
            builder.append( "Inventory" );
            builder.append( "</th>" );
            builder.append( "<th>" );
            builder.append( "Sell Price" );
            builder.append( "</th>" );
            builder.append( "<th>" );
            builder.append( "Free Space" );
            builder.append( "</th>" );
            builder.append( "</tr>" );

            for ( Shop shop : shopRegion.shops )
            {
                String buyPrice =
                        shop.getBuyPrice() == 0 ? "N/A" : String.format( this.formatString, shop.getBuyPrice() );
                String sellPrice =
                        shop.getSellPrice() == 0 ? "N/A" : String.format( this.formatString, shop.getSellPrice() );
                String inventory = shop.getBuyPrice() == 0 ? "N/A" :
                        Integer.toString(
                                Utils.getAmount( shop.getInventoryHolder().getInventory(), shop.getProduct() ));
                String freeSpace = shop.getSellPrice() == 0 ? "N/A" :
                        Integer.toString(
                                Utils.getFreeSpaceForItem(
                                        shop.getInventoryHolder().getInventory(), shop.getProduct() ));

                builder.append( "<tr>" );
                builder.append( "<td>" );
                builder.append( LanguageUtils.getItemName( shop.getProduct() ));
                builder.append( "</td>" );
                builder.append( "<td>" );
                builder.append( shop.getProduct().getAmount() );
                builder.append( "</td>" );
                builder.append( "<td>" );
                builder.append( buyPrice );
                builder.append( "</td>" );
                builder.append( "<td>" );
                builder.append( inventory );
                builder.append( "</td>" );
                builder.append( "<td>" );
                builder.append( sellPrice );
                builder.append( "</td>" );
                builder.append( "<td>" );
                builder.append( freeSpace );
                builder.append( "</td>" );
                builder.append( "</tr>" );
            }

            builder.append( "</table>" );

            this.getLogger().info(
                    "Shop Region " +
                    "- world: "    + shopRegion.world +
                    ", xLeft: "    + shopRegion.xLeft +
                    ", zTop: "     + shopRegion.zTop +
                    ", xRight: "   + shopRegion.xRight +
                    ", zBottom: "  + shopRegion.zBottom +
                    ", count: "    + shopRegion.shops.size() );

            AreaMarker area = this.markerSet.createAreaMarker(
                    UUID.randomUUID().toString(), builder.toString(), true, shopRegion.world,
                    new double[] { shopRegion.xLeft, shopRegion.xLeft, shopRegion.xRight, shopRegion.xRight },
                    new double[] { shopRegion.zTop, shopRegion.zBottom, shopRegion.zBottom, shopRegion.zTop }, false );
            area.setLineStyle( 3, 0.75, 0x00FFFF );
            area.setFillStyle( 0.25, 0x00FFFF );
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

        do
        {
            overlappingShopRegions.clear();

            // check for overlapping regions
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

                // two regions overlap if they overlap in the both the X and the Z dimension, and are in the same world
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

            // resize the region after shops from all overlapping regions have been added
            shopRegion.resize();
        }
        // check again for overlapping regions if one or more regions were merged
        while ( !overlappingShopRegions.isEmpty() );
    }
}
