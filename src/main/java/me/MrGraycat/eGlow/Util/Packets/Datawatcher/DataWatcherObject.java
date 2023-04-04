package me.mrgraycat.eglow.util.packets.datawatcher;

public class DataWatcherObject {
    //position in datawatcher
    public int position;

    // value class type used since 1.9
    public Object classType;

    /**
     * Constructs a new instance of this class with given parameters
     *
     * @param position  - position in datawatcher
     * @param classType - value class type
     */
    public DataWatcherObject(int position, Object classType) {
        this.position = position;
        this.classType = classType;
    }
}