package org.cengiz1.sutineki;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

import java.util.Random;
import java.util.Objects;

public class SutInekAlmaPlugin extends JavaPlugin implements CommandExecutor, Listener {

    @Override
    public void onEnable() {
        Objects.requireNonNull(getCommand("sütinekial")).setExecutor(this);
        getServer().getPluginManager().registerEvents(this, this);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (command.getName().equalsIgnoreCase("sütinekial")) {
            if (args.length == 1) {
                Player targetPlayer = Bukkit.getPlayer(args[0]);
                if (targetPlayer != null) {
                    giveCowEgg(targetPlayer);
                    sender.sendMessage("Süt İneği Yumurtası " + targetPlayer.getName() + " adlı oyuncuya verildi.");
                } else {
                    sender.sendMessage("Oyuncu bulunamadı.");
                }
            } else {
                sender.sendMessage("Kullanım: /sütinekial <oyuncu ismi>");
            }
            return true;
        }
        return false;
    }

    private void giveCowEgg(Player player) {
        ItemStack cowEgg = new ItemStack(Material.COW_SPAWN_EGG);
        ItemMeta eggMeta = cowEgg.getItemMeta();
        eggMeta.setDisplayName("\uE1EC Süt İneği \uE1EC");
        cowEgg.setItemMeta(eggMeta);

        player.getInventory().addItem(cowEgg);
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();
        ItemMeta itemMeta = item.getItemMeta();

        if (event.getAction().toString().contains("RIGHT_CLICK") && item.getType().equals(Material.APPLE)) {
            if (itemMeta != null && itemMeta.hasCustomModelData() && itemMeta.getCustomModelData() == 10012) {
                event.setCancelled(true);

                if (event.getClickedBlock() != null &&
                        event.getClickedBlock().getType() == Material.GRASS_BLOCK &&
                        event.getClickedBlock().getWorld().getNearbyEntities(event.getClickedBlock().getLocation(), 1, 1, 1).stream()
                                .anyMatch(entity -> entity.getType() == EntityType.COW && entity.getCustomName() != null && entity.getCustomName().equals("\uE1EC Süt İneği \uE1EC"))) {

                    // Menüyü aç
                    openFeedMenu(player);
                }
            }
        }
    }
    @EventHandler
    public void onPlayerConsumeItem(PlayerItemConsumeEvent event) {
        ItemStack consumedItem = event.getItem();
        Player player = event.getPlayer();

        if (consumedItem.getType() == Material.WHEAT) {
            // Süt İneği isimli inekleri tespit et
            if (player.getType() == EntityType.COW && player.getCustomName() != null && player.getCustomName().equals("\uE1EC Süt İneği \uE1EC")) {
                event.setCancelled(true);
                player.sendMessage("Süt İneği buğday yiyemez.");
            }
        }
    }

    private void openFeedMenu(Player player) {
        Inventory feedMenu = getServer().createInventory(player, 9, "Süt İneğini Besle");

        // Menüyü özelleştir
        ItemStack feedButton = new ItemStack(Material.APPLE);
        ItemMeta feedButtonMeta = feedButton.getItemMeta();
        feedButtonMeta.setDisplayName("İneği Besle");
        feedButton.setItemMeta(feedButtonMeta);

        feedMenu.setItem(4, feedButton);

        player.openInventory(feedMenu);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getView().getTitle().equals("Süt İneğini Besle")) {
            event.setCancelled(true);

            if (event.getRawSlot() == 4) {
                Player player = (Player) event.getWhoClicked();
                Inventory inventory = player.getInventory();

                ItemStack appleToRemove = null;
                for (ItemStack item : inventory.getContents()) {
                    if (item != null && item.getType() == Material.APPLE) {
                        ItemMeta itemMeta = item.getItemMeta();
                        if (itemMeta != null && itemMeta.hasCustomModelData() && itemMeta.getCustomModelData() == 10012) {
                            appleToRemove = item;
                            break;
                        }
                    }
                }

                if (appleToRemove != null) {
                    if (new Random().nextBoolean()) {
                        // Başarılı olunca
                        inventory.removeItem(appleToRemove);

                        ItemStack milkBucket = new ItemStack(Material.MILK_BUCKET);
                        ItemMeta milkBucketMeta = milkBucket.getItemMeta();
                        milkBucketMeta.setDisplayName("\uE1EC Süt İneği Kovası \uE1EC");
                        milkBucket.setItemMeta(milkBucketMeta);

                        inventory.addItem(milkBucket);
                        player.sendMessage("Tebrikler! Süt İneği kovası verildi.");
                    } else {
                        // Başarısız olunca
                        player.sendMessage("Üzgünüz, süt inek kovası düşmedi.");
                        inventory.removeItem(appleToRemove);
                    }
                } else {
                    player.sendMessage("Üzgünüz, Envanterinde Lahana yok.");
                }

                player.closeInventory();
            }
        }
    }


    @EventHandler
    public void onPlayerDrinkMilk(PlayerItemConsumeEvent event) {
        Player player = event.getPlayer();
        ItemStack consumedItem = event.getItem();

        if (consumedItem.getType() == Material.MILK_BUCKET && consumedItem.getItemMeta().getDisplayName().equals("\uE1EC Süt İneği Kovası \uE1EC")) {
            PotionEffect resistanceEffect = new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 60 * 20, 1); // 60 saniye
            PotionEffect strengthEffect = new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 60 * 20, 1); // 60 saniye

            player.addPotionEffect(resistanceEffect, true); // true parametresi eklenmiştir
            player.addPotionEffect(strengthEffect, true); // true parametresi eklenmiştir

            // Sadece bir kova kullanıldığında kovayı kaldır

            player.sendMessage("Süt İneği kovası içildi. Direnç ve Kuvvet efektleri verildi.");
        }
    }


}
