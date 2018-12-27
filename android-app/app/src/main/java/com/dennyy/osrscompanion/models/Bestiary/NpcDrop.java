package com.dennyy.osrscompanion.models.Bestiary;

import com.dennyy.osrscompanion.enums.DropRarity;

public final class NpcDrop {
    public String name;
    public String quantity;
    public DropRarity rarity;
    public String rarityNotes;

    public NpcDrop(Builder builder) {
        this.name = builder.name;
        this.quantity = builder.quantity;
        this.rarity = builder.rarity;
        this.rarityNotes = builder.rarityNotes;
    }

    public static class Builder {
        private String name;
        private String quantity;
        private DropRarity rarity;
        private String rarityNotes;

        public Builder setName(String name) {
            this.name = name;
            return this;
        }

        public Builder setQuantity(String quantity) {
            this.quantity = quantity;
            return this;
        }

        public Builder setRarity(String rarity) {
            this.rarity = DropRarity.fromString(rarity);
            return this;
        }

        public Builder setRarityNotes(String rarityNotes) {
            this.rarityNotes = rarityNotes;
            return this;
        }

        Builder() {
            this.rarity = DropRarity.VERY_RARE;
        }

        public NpcDrop build() {
            return new NpcDrop(this);
        }
    }
}
