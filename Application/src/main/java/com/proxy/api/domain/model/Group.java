package com.proxy.api.domain.model;

import android.os.Parcelable;
import android.support.annotation.Nullable;

import com.proxy.api.gson.AutoGson;

import java.util.ArrayList;
import java.util.UUID;

import auto.parcel.AutoParcel;

/**
 * Groups only have names for now.
 */
@AutoParcel
@AutoGson(autoValueClass = AutoParcel_Group.class)
public abstract class Group implements Parcelable {
    /**
     * create a new Group.
     *
     * @param label    name of the group
     * @param channels channel permissions for the group
     * @param contacts contacts in the group
     * @return Immutable group
     */
    @SuppressWarnings("unused")
    public static Group create(
        String label, ArrayList<Channel> channels, ArrayList<Contact> contacts) {
        String groupId = UUID.randomUUID().toString();
        return builder().groupId(groupId).label(label).channels(channels)
            .contacts(contacts).build();
    }

    /**
     * Group builder.
     *
     * @return this Group.
     */
    public static Builder builder() {
        return new AutoParcel_Group.Builder();
    }

    /**
     * Get the ID of the {@link Group}
     *
     * @return name
     */
    public abstract String groupId();

    /**
     * Get the name of the {@link Group}.
     *
     * @return name
     */
    public abstract String label();

    /**
     * Get the list of {@link Channel}s tied to this {@link Group}.
     *
     * @return list of {@link Channel}s
     */
    @Nullable
    public abstract ArrayList<Channel> channels();

    /**
     * Get the list of {@link Contact}s in this {@link Group}.
     *
     * @return list of {@link Contact}s
     */
    @Nullable
    public abstract ArrayList<Contact> contacts();

    /**
     * Group Builder.
     */
    @AutoParcel.Builder
    public interface Builder {

        /**
         * Set the groups Id.
         *
         * @param id group unique groupId
         * @return group groupId
         */
        Builder groupId(String id);

        /**
         * Set the groups name.
         *
         * @param label group name
         * @return label
         */
        Builder label(String label);

        /**
         * Set group channels.
         *
         * @param channels group channels
         * @return channels
         */
        @Nullable
        Builder channels(ArrayList<Channel> channels);

        /**
         * Set group contacts.
         *
         * @param contacts this groups contacts
         * @return contacts
         */
        @Nullable
        Builder contacts(ArrayList<Contact> contacts);

        /**
         * BUILD.
         *
         * @return Group
         */
        Group build();
    }


}