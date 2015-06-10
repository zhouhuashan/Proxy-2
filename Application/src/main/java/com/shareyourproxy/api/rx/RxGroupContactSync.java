package com.shareyourproxy.api.rx;

import android.content.Context;

import com.shareyourproxy.api.domain.factory.GroupFactory;
import com.shareyourproxy.api.domain.factory.UserFactory;
import com.shareyourproxy.api.domain.model.Contact;
import com.shareyourproxy.api.domain.model.Group;
import com.shareyourproxy.api.domain.model.GroupEditContact;
import com.shareyourproxy.api.domain.model.User;
import com.shareyourproxy.api.rx.command.event.CommandEvent;
import com.shareyourproxy.api.rx.command.event.GroupContactAddedEvent;
import com.shareyourproxy.api.rx.command.event.GroupContactDeletedEvent;
import com.shareyourproxy.api.rx.command.event.GroupContactsUpdatedEvent;

import java.util.ArrayList;
import java.util.List;

import io.realm.Realm;
import rx.Observable;
import rx.functions.Func1;
import rx.functions.Func2;
import rx.observables.ConnectableObservable;
import timber.log.Timber;

import static com.shareyourproxy.api.RestClient.getGroupContactService;
import static com.shareyourproxy.api.domain.factory.RealmUserFactory.createRealmUser;
import static com.shareyourproxy.api.rx.RxHelper.filterNullContact;

/**
 * Created by Evan on 6/4/15.
 */
public class RxGroupContactSync {

    /**
     * Private constructor.
     */
    private RxGroupContactSync() {
    }

    public static List<CommandEvent> updateGroupContacts(
        final Context context, final User user,
        final ArrayList<GroupEditContact> editGroups, final Contact contact) {
        return Observable.merge(
            Observable.from(editGroups).map(contactInGroup())
                .filter(filterNullContact()).toList().map(createGroupContactEvent(contact)),
            Observable.from(editGroups).flatMap(
                groupContactCommand(context, user, contact)))
            .toList().toBlocking().single();
    }

    private static Func1<GroupEditContact, Group> contactInGroup() {
        return new Func1<GroupEditContact, Group>() {
            @Override
            public Group call(GroupEditContact groupEditContact) {
                if (groupEditContact.hasContact()) {
                    return groupEditContact.getGroup();
                } else {
                    return null;
                }
            }
        };
    }

    private static Func1<GroupEditContact, Observable<CommandEvent>> groupContactCommand(
        final Context context, final User user, final Contact contact) {
        return new Func1<GroupEditContact, Observable<CommandEvent>>() {
            @Override
            public Observable<CommandEvent> call(GroupEditContact groupEditContact) {
                if (groupEditContact.hasContact()) {
                    return addGroupContact(context, user, groupEditContact.getGroup(), contact);
                } else {
                    return deleteGroupContact(context, user, groupEditContact.getGroup(), contact);
                }
            }
        };
    }

    private static Func1<List<Group>, CommandEvent> createGroupContactEvent(
        final Contact contact) {
        return new Func1<List<Group>, CommandEvent>() {
            @Override
            public GroupContactsUpdatedEvent call(List<Group> groups) {
                return new GroupContactsUpdatedEvent(contact, new ArrayList<>(groups));
            }
        };
    }

    public static Observable<CommandEvent> addGroupContact(
        Context context, User user, Group editGroup, Contact contact) {
        return rx.Observable.zip(
            saveRealmGroupContact(context, user, editGroup, contact),
            saveFirebaseGroupContact(context, user.id().value(), editGroup.id().value(), contact),
            zipAddGroupContact());
    }

    public static Observable<CommandEvent> deleteGroupContact(
        Context context, User user, Group editGroup, Contact contact) {
        return rx.Observable.zip(
            deleteRealmGroupContact(context, user, editGroup, contact),
            deleteFirebaseGroupContact(
                context, user.id().value(), editGroup.id().value(), contact),
            zipDeleteGroupContact());
    }

    private static Func2<Group, Contact, CommandEvent> zipAddGroupContact() {
        return new Func2<Group, Contact, CommandEvent>() {
            @Override
            public GroupContactAddedEvent call(Group group, Contact contact) {
                return new GroupContactAddedEvent(group, contact);
            }
        };
    }

    private static Func2<Group, Contact, CommandEvent> zipDeleteGroupContact() {
        return new Func2<Group, Contact, CommandEvent>() {
            @Override
            public GroupContactDeletedEvent call(Group group, Contact contact) {
                return new GroupContactDeletedEvent(group, contact);
            }
        };
    }

    private static rx.Observable<Group> saveRealmGroupContact(
        Context context, User user, Group editGroup, Contact contact) {
        return Observable.just(editGroup).map(addRealmGroupContact(context, user, contact));
    }

    private static Func1<Group, Group> addRealmGroupContact(
        final Context context, final User user, final Contact contact) {
        return new Func1<Group, Group>() {
            @Override
            public Group call(Group oldGroup) {
                Group newGroup = GroupFactory.addGroupContact(oldGroup, contact);
                User newUser = UserFactory.addUserGroup(user, newGroup);
                Realm realm = Realm.getInstance(context);
                realm.beginTransaction();
                realm.copyToRealmOrUpdate(createRealmUser(newUser));
                realm.commitTransaction();
                realm.close();
                return newGroup;
            }
        };
    }

    private static rx.Observable<Group> deleteRealmGroupContact(
        Context context, User user, Group group, Contact contact) {
        return Observable.just(group)
            .map(deleteRealmGroupContact(context, user, contact));
    }

    private static Func1<Group, Group> deleteRealmGroupContact(
        final Context context, final User user, final Contact contact) {
        return new Func1<Group, Group>() {
            @Override
            public Group call(Group oldGroup) {
                Group newGroup = GroupFactory.deleteGroupContact(oldGroup, contact);
                User newUser = UserFactory.addUserGroup(user, newGroup);
                Realm realm = Realm.getInstance(context);
                realm.beginTransaction();
                realm.copyToRealmOrUpdate(createRealmUser(newUser));
                realm.commitTransaction();
                realm.close();
                return newGroup;
            }
        };
    }

    private static rx.Observable<Contact> saveFirebaseGroupContact(
        Context context, String userId, String groupId, Contact contact) {
        return getGroupContactService(context)
            .addGroupContact(userId, groupId, contact.id().value(), contact);
    }

    private static rx.Observable<Contact> deleteFirebaseGroupContact(
        Context context, String userId, String groupId, Contact contact) {
        Observable<Contact> deleteObserver = getGroupContactService(context)
            .deleteGroupContact(userId, groupId, contact.id().value());
        deleteObserver.subscribe(new JustObserver<Contact>() {
            @Override
            public void onError() {
                Timber.e("error deleting group contact");
            }

            @Override
            public void onNext(Contact event) {
                Timber.i("delete group contact successful");
            }
        });
        ConnectableObservable<Contact> connectableObservable = deleteObserver.publish();
        return rx.Observable.merge(
            Observable.just(contact), connectableObservable)
            .filter(filterNullContact());
    }

}