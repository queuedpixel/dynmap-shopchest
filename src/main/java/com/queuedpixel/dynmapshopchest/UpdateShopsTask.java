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
import de.epiceric.shopchest.language.LanguageUtils;
import de.epiceric.shopchest.shop.Shop;
import de.epiceric.shopchest.utils.ItemUtils;
import de.epiceric.shopchest.utils.Utils;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.scheduler.BukkitRunnable;
import org.dynmap.DynmapCommonAPI;
import org.dynmap.markers.AreaMarker;
import org.dynmap.markers.MarkerAPI;
import org.dynmap.markers.MarkerSet;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class UpdateShopsTask extends BukkitRunnable
{
    private DynmapShopchestPlugin plugin;
    private ShopChest shopChest;
    private MarkerAPI markerApi;
    private String formatString;
    private ShopComparator shopComparator = new ShopComparator();
    private Collection< ShopRegion > shopRegions = new LinkedList<>();
    private MarkerSet markerSet = null;
    private Collection< AreaMarker > areaMarkers = new LinkedList<>();

    UpdateShopsTask( DynmapShopchestPlugin plugin )
    {
        this.plugin = plugin;
        this.shopChest = (ShopChest) Bukkit.getServer().getPluginManager().getPlugin( "ShopChest" );
        DynmapCommonAPI dynmapApi = (DynmapCommonAPI) Bukkit.getServer().getPluginManager().getPlugin( "dynmap" );
        this.markerApi = dynmapApi.getMarkerAPI();
        RegisteredServiceProvider< Economy > rsp =
                        Bukkit.getServer().getServicesManager().getRegistration( Economy.class );
        Economy economy = rsp.getProvider();
        this.formatString = "%,." + economy.fractionalDigits() + "f";
    }

    public void run()
    {
        String whiteBackgroundStyle = "background-color: #FFFFFF;";
        String borderStyle          = "border-right: 1px solid black;";
        String textAlignLeftStyle   = "text-align: left;";
        String textAlignCenterStyle = "text-align: center;";
        String textAlignRightStyle  = "text-align: right;";
        String paddingLeftStyle     = "padding-left: 3px;";
        String paddingRightStyle    = "padding-right: 3px;";
        String paddingBothStyle     = paddingLeftStyle + " " + paddingRightStyle;

        String divStyle        = "overflow-y: auto; max-height: 75vh;";
        String tableStyle      = "border-collapse: collapse;";
        String paddingRowStyle = whiteBackgroundStyle + " height: 1em;";
        String headerRowStyle  = whiteBackgroundStyle + " " + textAlignCenterStyle + ";";
        String itemHeaderStyle = borderStyle + " " + textAlignLeftStyle + ";";
        String leftItemStyle   = borderStyle + " " + textAlignRightStyle + " " + paddingRightStyle;
        String middleItemStyle = borderStyle + " " + textAlignRightStyle + " " + paddingBothStyle;
        String rightItemStyle  = textAlignRightStyle + " " + paddingLeftStyle;

        if ( this.markerSet != null )
        {
            for ( AreaMarker areaMarker : this.areaMarkers )
            {
                areaMarker.deleteMarker();
            }

            this.shopRegions.clear();
            this.areaMarkers.clear();
        }
        else
        {
            this.markerSet = markerApi.createMarkerSet(
                    "DynmapShopchest", this.plugin.getConfig().getString( "layerName" ), null, false );
            this.markerSet.setLayerPriority( this.plugin.getConfig().getInt( "layerPriority" ));
        }

        Set< Location > locations = new HashSet<>();

        for ( Shop shop : this.shopChest.getShopUtils().getShops() )
        {
            if ( !locations.contains( shop.getLocation() ))
            {
                locations.add( shop.getLocation() );
                ShopRegion shopRegion = this.getShopRegion( shop );
                shopRegion.addShop( shop );
                shopRegion.resize();
                this.checkOverlappingShopRegions( shopRegion );
            }
        }

        for ( ShopRegion shopRegion : this.shopRegions )
        {
            shopRegion.shops.sort( shopComparator );
            String lastVendor = null;

            StringBuilder builder = new StringBuilder();
            builder.append( "<div style=\"" ).append( divStyle ).append( "\">" );
            builder.append( "<table style=\"" ).append( tableStyle ).append( "\">" );

            boolean alternate = true;
            for ( Shop shop : shopRegion.shops )
            {
                String currentVendor = shop.getShopType().equals( Shop.ShopType.ADMIN ) ?
                        "<em>Admin Shop</em>" : shop.getVendor().getName();

                if ( !currentVendor.equals( lastVendor ))
                {
                    if ( lastVendor != null )
                    {
                        builder.append( "<tr style=\"" ).append( paddingRowStyle ).append( "\">" );
                        builder.append( "<td colspan=\"6\">" );
                        builder.append( "</td>" );
                        builder.append( "</tr>" );
                    }

                    builder.append( "<tr style=\"" ).append( whiteBackgroundStyle ).append( "\">" );
                    builder.append( "<td colspan=\"6\">" );
                    builder.append( "<strong>Vendor:</strong> " );
                    builder.append( currentVendor );
                    builder.append( "</td>" );
                    builder.append( "</tr>" );
                    builder.append( "<tr style=\"" ).append( headerRowStyle ).append( "\">" );
                    builder.append( "<th style=\"" ).append( itemHeaderStyle ).append( "\">" );
                    builder.append( "Item" );
                    builder.append( "</th>" );
                    builder.append( "<th style=\"" ).append( borderStyle ).append( "\">" );
                    builder.append( "Amt." );
                    builder.append( "</th>" );
                    builder.append( "<th style=\"" ).append( borderStyle ).append( "\">" );
                    builder.append( "Buy" );
                    builder.append( "</th>" );
                    builder.append( "<th style=\"" ).append( borderStyle ).append( "\">" );
                    builder.append( "#" );
                    builder.append( "</th>" );
                    builder.append( "<th style=\"" ).append( borderStyle ).append( "\">" );
                    builder.append( "Sell" );
                    builder.append( "</th>" );
                    builder.append( "<th>" );
                    builder.append( "#" );
                    builder.append( "</th>" );
                    builder.append( "</tr>" );

                    alternate = true;
                    lastVendor = currentVendor;
                }

                alternate = !alternate;
                String backgroundStyle = "background-color: " + ( alternate ? "#AAAAAA" : "#DDDDDD" ) + ";";

                Inventory inventory = shop.getInventoryHolder().getInventory();
                String buyPrice =
                        shop.getBuyPrice() == 0 ? "" : String.format( this.formatString, shop.getBuyPrice() );
                String sellPrice =
                        shop.getSellPrice() == 0 ? "" : String.format( this.formatString, shop.getSellPrice() );
                String inventoryCount = shop.getBuyPrice() == 0 ? "" :
                        shop.getShopType().equals( Shop.ShopType.ADMIN ) ? "∞" :
                                String.format( "%,d", Utils.getAmount( inventory, shop.getProduct() ));
                String freeSpaceCount = shop.getSellPrice() == 0 ? "" :
                        shop.getShopType().equals( Shop.ShopType.ADMIN ) ? "∞" :
                                String.format( "%,d", Utils.getFreeSpaceForItem( inventory, shop.getProduct() ));

                builder.append( "<tr style=\"" ).append( backgroundStyle ).append( "\">" );
                builder.append( "<td style=\"" ).append( leftItemStyle ).append( "\">" );
                builder.append( "<strong>" );
                builder.append( LanguageUtils.getItemName( shop.getProduct() ));
                builder.append( "</strong>" );

                Map< Enchantment, Integer > enchantments = ItemUtils.getEnchantments( shop.getProduct() );
                for ( Enchantment enchantment : enchantments.keySet() )
                {
                    builder.append( "<br>" );
                    builder.append( "<em>" );
                    builder.append( LanguageUtils.getEnchantmentName( enchantment, enchantments.get( enchantment )));
                    builder.append( "</em>" );
                }

                builder.append( "</td>" );
                builder.append( "<td style=\"" ).append( middleItemStyle ).append( "\">" );
                builder.append( shop.getProduct().getAmount() );
                builder.append( "</td>" );
                builder.append( "<td style=\"" ).append( middleItemStyle ).append( "\">" );
                builder.append( buyPrice );
                builder.append( "</td>" );
                builder.append( "<td style=\"" ).append( middleItemStyle ).append( "\">" );
                builder.append( inventoryCount );
                builder.append( "</td>" );
                builder.append( "<td style=\"" ).append( middleItemStyle ).append( "\">" );
                builder.append( sellPrice );
                builder.append( "</td>" );
                builder.append( "<td style=\"" ).append( rightItemStyle ).append( "\">" );
                builder.append( freeSpaceCount );
                builder.append( "</td>" );
                builder.append( "</tr>" );
            }

            builder.append( "</table>" );
            builder.append( "</div>" );

            AreaMarker areaMarker = this.markerSet.createAreaMarker(
                    UUID.randomUUID().toString(), builder.toString(), true, shopRegion.world,
                    new double[] { shopRegion.xLeft, shopRegion.xLeft, shopRegion.xRight, shopRegion.xRight },
                    new double[] { shopRegion.zTop, shopRegion.zBottom, shopRegion.zBottom, shopRegion.zTop }, false );
            areaMarker.setLineStyle(
                    this.plugin.getConfig().getInt(    "lineWidth"   ),
                    this.plugin.getConfig().getDouble( "lineOpacity" ),
                    this.plugin.getConfig().getInt(    "lineColor"   ));
            areaMarker.setFillStyle(
                    this.plugin.getConfig().getDouble( "fillOpacity" ),
                    this.plugin.getConfig().getInt(    "fillColor"   ));
            this.areaMarkers.add( areaMarker );
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
                          ( otherShopRegion.xLeft  <= shopRegion.xRight )));

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
                          ( otherShopRegion.zTop    <= shopRegion.zBottom )));

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
