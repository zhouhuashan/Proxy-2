package com.shareyourproxy.api.rx.command;

import android.app.Service;
import android.os.Parcel;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.shareyourproxy.api.domain.model.Channel;
import com.shareyourproxy.api.domain.model.User;
import com.shareyourproxy.api.rx.RxUserChannelSync;
import com.shareyourproxy.api.rx.command.eventcallback.EventCallback;

import java.util.List;

/**
 * Created by Evan on 6/8/15.
 */
public class AddUserChannelCommand extends BaseCommand {
    public static final Creator<AddUserChannelCommand> CREATOR = new Creator<AddUserChannelCommand>
        () {
        @Override
        public AddUserChannelCommand createFromParcel(Parcel in) {
            return new AddUserChannelCommand(in);
        }

        @Override
        public AddUserChannelCommand[] newArray(int size) {
            return new AddUserChannelCommand[size];
        }
    };
    private final static java.lang.ClassLoader CL = AddUserChannelCommand.class.getClassLoader();
    public final User user;
    public final Channel newChannel;
    public final Channel oldChannel;

    public AddUserChannelCommand(
        @NonNull User user, @NonNull Channel newChannel, @Nullable Channel oldChannel) {
        super(AddUserChannelCommand.class.getPackage().getName(),
            AddUserChannelCommand.class.getName());
        this.user = user;
        this.newChannel = newChannel;
        this.oldChannel = oldChannel;
    }

    private AddUserChannelCommand(Parcel in) {
        this((User) in.readValue(CL), (Channel) in.readValue(CL), (Channel) in.readValue(CL));
    }

    @Override
    public List<EventCallback> execute(Service service) {
        return RxUserChannelSync.addChannel(service, user, newChannel, oldChannel);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeValue(user);
        dest.writeValue(newChannel);
        dest.writeValue(oldChannel);
    }
}
