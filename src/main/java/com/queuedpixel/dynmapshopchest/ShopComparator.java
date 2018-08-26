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

import de.epiceric.shopchest.language.LanguageUtils;
import de.epiceric.shopchest.shop.Shop;
import de.epiceric.shopchest.utils.ItemUtils;

import java.util.Comparator;

public class ShopComparator implements Comparator< Shop >
{
    public int compare( Shop shop1, Shop shop2 )
    {
        boolean adminShop1 = shop1.getShopType().equals( Shop.ShopType.ADMIN );
        boolean adminShop2 = shop2.getShopType().equals( Shop.ShopType.ADMIN );
        int adminShopResult = Boolean.compare( adminShop1, adminShop2 );
        if ( adminShopResult != 0 ) return -adminShopResult; // negate the result since false sorts before true

        String vendor1 = shop1.getVendor().getName();
        String vendor2 = shop2.getVendor().getName();
        int vendorResult = vendor1.compareTo( vendor2 );
        if ( vendorResult != 0 ) return vendorResult;

        String item1 = LanguageUtils.getItemName( shop1.getProduct() );
        String item2 = LanguageUtils.getItemName( shop2.getProduct() );
        int itemResult = item1.compareTo( item2 );
        if ( itemResult != 0 ) return itemResult;

        String enchantments1 = LanguageUtils.getEnchantmentString( ItemUtils.getEnchantments( shop1.getProduct()) );
        String enchantments2 = LanguageUtils.getEnchantmentString( ItemUtils.getEnchantments( shop2.getProduct()) );
        return enchantments1.compareTo( enchantments2 );
    }
}
