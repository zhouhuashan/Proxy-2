package com.shareyourproxy.api.domain.factory;

import com.shareyourproxy.api.domain.model.Group;
import com.shareyourproxy.api.domain.model.Id;
import com.shareyourproxy.api.domain.realm.RealmGroup;
import com.shareyourproxy.api.domain.realm.RealmString;

import java.util.HashMap;
import java.util.Map;

import io.realm.RealmList;

/**
 * Factory for creating {@link RealmGroup}s.
 */
public class RealmGroupFactory {

    /**
     * Return a RealmList of Contacts from a user
     *
     * @param groupHashMap to get contacts from
     * @return RealmList of Contacts
     */
    public static RealmList<RealmGroup> getRealmGroups(HashMap<String, Group> groupHashMap) {
        if (groupHashMap == null) {
            return null;
        }
        RealmList<RealmGroup> realmGroupArray = new RealmList<>();
        for (Map.Entry<String, Group> entryGroup : groupHashMap.entrySet()) {
            Group group = entryGroup.getValue();
            RealmGroup realmGroup = new RealmGroup();
            realmGroup.setId(group.id().value());
            realmGroup.setLabel(group.label());
            realmGroup.setChannels(createRealmStringList(group.channels()));
            realmGroup.setContacts(createRealmStringList(group.contacts()));
            realmGroupArray.add(realmGroup);
        }
        return realmGroupArray;
    }

    private static RealmList<RealmString> createRealmStringList(HashMap<String, Id> values) {
        if (values == null) {
            return null;
        }
        RealmList<RealmString> list = new RealmList<>();
        for (Map.Entry<String, Id> valueEntry : values.entrySet()) {
            RealmString newChannel = new RealmString();
            newChannel.setValue(valueEntry.getKey());
            list.add(newChannel);
        }
        return list;
    }
}
