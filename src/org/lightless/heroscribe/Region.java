package org.lightless.heroscribe;

public enum Region {
  EUROPE("Europe"),
  USA("USA");

  private final String label;

  private Region(String label) {
    this.label = label;
  }

  public static Region parse(String label) {
    for (Region region : Region.values()) {
      if (region.label.equals(label)) {
        return region;
      }
    }
    return EUROPE;
  }

  @Override
  public String toString() {
    return label;
  }
}
