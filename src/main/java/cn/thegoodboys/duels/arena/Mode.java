package cn.thegoodboys.duels.arena;

import lombok.Getter;

public enum Mode {
    SOLO("单人"),
    TWO_TWO("双人");

    @Getter
    private final String disPlayName;

    Mode(String disPlayName) {
        this.disPlayName = disPlayName;
    }
}
