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

import de.epiceric.shopchest.event.ShopInitializedEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

public class DynmapShopchestPlugin extends JavaPlugin implements Listener
{
    public void onEnable()
    {
        this.getServer().getPluginManager().registerEvents( this, this );
    }

    public void onDisable()
    {
    }

    @EventHandler
    public void onShopInitializedEvent( ShopInitializedEvent event )
    {
        int updateInterval = 3600 * 20; // 1 hour times 20 ticks per second
        UpdateShopsTask updateShopsTask = new UpdateShopsTask( this );
        updateShopsTask.runTaskTimer( this, 0, updateInterval );
    }
}
