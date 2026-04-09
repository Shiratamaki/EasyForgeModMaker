package com.easyforge.model;

public enum FluidType {
    WATER("水", 1000, 1000, 300, false),
    LAVA("岩浆", 3000, 3000, 1300, true),
    MILK("奶", 1000, 1000, 300, false),
    HONEY("蜂蜜", 1200, 2000, 310, false),
    OIL("油", 800, 1500, 320, false),
    FUEL("燃料", 900, 1200, 350, false),
    CHEMICAL("化学品", 1100, 800, 290, false),
    GAS("气体", 100, 50, 400, true);

    private final String displayName;
    private final int density;
    private final int viscosity;
    private final int temperature;
    private final boolean gaseous;

    FluidType(String displayName, int density, int viscosity, int temperature, boolean gaseous) {
        this.displayName = displayName;
        this.density = density;
        this.viscosity = viscosity;
        this.temperature = temperature;
        this.gaseous = gaseous;
    }

    public String getDisplayName() { return displayName; }
    public int getDensity() { return density; }
    public int getViscosity() { return viscosity; }
    public int getTemperature() { return temperature; }
    public boolean isGaseous() { return gaseous; }
}