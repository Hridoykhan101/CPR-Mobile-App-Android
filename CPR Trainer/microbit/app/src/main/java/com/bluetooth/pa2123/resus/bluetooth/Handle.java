package com.bluetooth.pa2123.resus.bluetooth;

import java.util.UUID;

public class Handle {
    // Android API does not give you access to handles to use as unique ID of GATT attribute so composite of UUID and instance ID being used instead

    private UUID uuid;
    private int instance_id;

    public Handle(UUID uuid, int instance_id) {
        this.uuid = uuid;
        this.instance_id = instance_id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Handle handle = (Handle) o;

        if (instance_id != handle.instance_id) return false;
        return !(uuid != null ? !uuid.equals(handle.uuid) : handle.uuid != null);
    }

    @Override
    public int hashCode() {
        int result = uuid != null ? uuid.hashCode() : 0;
        result = 31 * result + instance_id;
        return result;
    }

    @Override
    public String toString() {
        return "Handle{" +
                "uuid=" + uuid +
                ", instance_id=" + instance_id +
                '}';
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public int getInstance_id() {
        return instance_id;
    }

    public void setInstance_id(int instance_id) {
        this.instance_id = instance_id;
    }
}
