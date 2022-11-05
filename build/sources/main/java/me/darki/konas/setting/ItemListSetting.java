package me.darki.konas.setting;


import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ItemListSetting {
    private List<String> itemsString;

    public ItemListSetting(String... itemNames) {
        itemsString = new ArrayList<>();

        for (String name : itemsString) {
            if (!itemsString.contains(name.toLowerCase(Locale.ENGLISH))) {
                itemsString.add(name.toLowerCase(Locale.ENGLISH));
            }
        }
    }

    public ItemListSetting(ArrayList<String> itemNames) {
        itemsString = new ArrayList<>();

        for (String name : itemNames) {
            if (!itemsString.contains(name.toLowerCase(Locale.ENGLISH))) {
                itemsString.add(name.toLowerCase(Locale.ENGLISH));
            }
        }
    }

    public void addItems(ArrayList<String> itemNames) {
        for (String name : itemNames) {
            if (!itemsString.contains(name.toLowerCase(Locale.ENGLISH))) {
                itemsString.add(name.toLowerCase(Locale.ENGLISH));
            }
        }
    }

    public void addItem(String itemName) {
        if (!itemsString.contains(itemName.toLowerCase(Locale.ENGLISH))) {
            itemsString.add(itemName.toLowerCase(Locale.ENGLISH));
        }
    }

    public void removeItem(String itemName) {
        itemsString.remove(itemName.toLowerCase(Locale.ENGLISH));
    }

    public List<String> getItems() {
        return itemsString;
    }
}
