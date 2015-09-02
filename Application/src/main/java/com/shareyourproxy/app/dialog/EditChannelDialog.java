package com.shareyourproxy.app.dialog;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatDialog;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.shareyourproxy.R;
import com.shareyourproxy.api.domain.model.Channel;
import com.shareyourproxy.api.domain.model.ChannelType;
import com.shareyourproxy.api.rx.command.AddUserChannelCommand;
import com.shareyourproxy.api.rx.command.DeleteUserChannelCommand;
import com.shareyourproxy.api.rx.event.DialogCanceledEvent;
import com.shareyourproxy.util.ObjectUtils;

import butterknife.Bind;
import butterknife.BindColor;
import butterknife.BindString;
import butterknife.ButterKnife;

import static com.shareyourproxy.api.domain.factory.ChannelFactory.createModelInstance;
import static com.shareyourproxy.util.ViewUtils.hideSoftwareKeyboard;

/**
 * Dialog that handles editing a selected channel.
 */
public class EditChannelDialog extends BaseDialogFragment {
    // Final
    private static final String ARG_CHANNEL = "EditChannelDialog.Channel";
    private static final String TAG = ObjectUtils.getSimpleName(AddChannelDialog.class);
    // View
    @Bind(R.id.dialog_channel_action_address_edittext)
    protected EditText editTextActionAddress;
    private final OnClickListener _negativeClicked =
        new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                hideSoftwareKeyboard(editTextActionAddress);
                getRxBus().post(new DialogCanceledEvent());
            }
        };
    @Bind(R.id.dialog_channel_label_edittext)
    protected EditText editTextLabel;
    @Bind(R.id.dialog_channel_label_floatlabel)
    protected TextInputLayout floatLabelChannelLabel;
    @Bind(R.id.dialog_channel_action_address_floatlabel)
    protected TextInputLayout floatLabelAddress;
    // Color
    @BindColor(R.color.common_text)
    protected int _textColor;
    @BindColor(R.color.common_blue)
    protected int _blue;
    @BindString(R.string.required)
    protected String _required;
    // Transient
    private Channel _channel;
    /**
     * EditorActionListener that detects when the software keyboard's done or enter button is
     * pressed.
     */
    private final OnEditorActionListener _onEditorActionListener =
        new OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == KeyEvent.KEYCODE_ENTER
                    || actionId == KeyEvent.KEYCODE_ENDCALL) {
                    saveChannelAndExit();
                    return true;
                }
                return false;
            }
        };
    private final View.OnClickListener _positiveClicked =
        new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveChannelAndExit();
            }
        };
    private final OnClickListener _deleteClicked =
        new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                getRxBus().post(
                    new DeleteUserChannelCommand(getLoggedInUser(), _channel));
                dismiss();
            }
        };
    private String _dialogTitle;
    private String _channelAddressHint;
    private String _channelLabelHint;

    /**
     * Constructor.
     */
    public EditChannelDialog() {
    }

    /**
     * Create a new instance of a {@link EditChannelDialog}.
     *
     * @return A {@link EditChannelDialog}
     */
    public static EditChannelDialog newInstance(
        Channel channel) {
        //Bundle arguments
        Bundle bundle = new Bundle();
        bundle.putParcelable(ARG_CHANNEL, channel);
        //copy dialog instance
        EditChannelDialog dialog = new EditChannelDialog();
        dialog.setArguments(bundle);
        return dialog;
    }

    /**
     * Dispatch a Channel Added Event
     */
    private void addUserChannel() {
        String actionContent = editTextActionAddress.getText().toString().trim();
        String labelContent = editTextLabel.getText().toString().trim();
        if (!TextUtils.isEmpty(actionContent.trim())) {
            Channel channel;
            if (_channel.channelType().equals(ChannelType.Facebook)) {
                channel = createModelInstance(_channel.id().value(), _channel.label(),
                    _channel.channelType(), actionContent);
                getRxBus().post(new AddUserChannelCommand(getLoggedInUser(), channel, _channel));
            } else {
                channel =
                    createModelInstance(_channel.id().value(), labelContent, _channel.channelType(),
                        actionContent);
                getRxBus().post(new AddUserChannelCommand(getLoggedInUser(), channel, _channel));
            }
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        _channel = getArguments().getParcelable(ARG_CHANNEL);
    }

    @NonNull
    @Override
    @SuppressLint("InflateParams")
    public AppCompatDialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);
        View view = getActivity().getLayoutInflater()
            .inflate(R.layout.dialog_add_channel, null, false);
        ButterKnife.bind(this, view);
        initializeDisplayValues();

        AlertDialog dialog = new AlertDialog.Builder(getActivity(),
            R.style.Base_Theme_AppCompat_Light_Dialog)
            .setTitle(_dialogTitle)
            .setView(view)
            .setPositiveButton(R.string.save, null)
            .setNegativeButton(android.R.string.cancel, _negativeClicked)
            .setNeutralButton(R.string.delete, _deleteClicked)
            .create();
        //Override the dialog wrapping content and cancel dismiss on click outside
        // of the dialog window
        dialog.getWindow().getAttributes().width = WindowManager.LayoutParams.MATCH_PARENT;
        dialog.setCanceledOnTouchOutside(false);
        // Show the SW Keyboard on dialog start. Always.
        dialog.getWindow().setSoftInputMode(
            WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        initializeEditText();
        return dialog;
    }

    @Override
    public void onStart() {
        super.onStart();
        AlertDialog dialog = (AlertDialog) getDialog();
        setButtonTint(dialog.getButton(Dialog.BUTTON_POSITIVE), _blue);
        setButtonTint(dialog.getButton(Dialog.BUTTON_NEGATIVE), _textColor);
        setButtonTint(dialog.getButton(Dialog.BUTTON_NEUTRAL), _textColor);
        //Alert Dialogs dismiss by default because of an internal handler... this bypasses that.
        dialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(_positiveClicked);
    }

    public void saveChannelAndExit() {
        boolean addressHasText = editTextActionAddress.getText().toString().trim().length() > 0;
        if (!addressHasText) {
            floatLabelAddress.setError(_required);
        } else {
            floatLabelAddress.setErrorEnabled(false);
            addUserChannel();
            dismiss();
        }
    }

    private void initializeDisplayValues() {
        String label = _channel.channelType().getLabel();
        switch (_channel.channelType()) {
            case Custom:
                _dialogTitle = getString(R.string.dialog_editchannel_title_custom);
                _channelAddressHint = getString(R.string.dialog_editchannel_hint_address_custom);
                _channelLabelHint = getString(R.string.dialog_editchannel_hint_label_custom);
                break;
            case Phone:
                _dialogTitle = getString(R.string.dialog_editchannel_title_blank, label);
                _channelAddressHint = getString(R.string.dialog_editchannel_hint_address_phone);
                _channelLabelHint = getString(R.string.dialog_editchannel_hint_label_phone);
                break;
            case SMS:
                _dialogTitle = getString(R.string.dialog_editchannel_title_blank, label);
                _channelAddressHint = getString(R.string.dialog_editchannel_hint_address_sms);
                _channelLabelHint = getString(R.string.dialog_editchannel_hint_label_sms);
                break;
            case Email:
                _dialogTitle = getString(R.string.dialog_editchannel_title_blank, label);
                _channelAddressHint = getString(R.string.dialog_editchannel_hint_address_email);
                _channelLabelHint = getString(R.string.dialog_editchannel_hint_label_email);
                break;
            case Web:
                _dialogTitle = getString(R.string.dialog_editchannel_title_blank, label);
                _channelAddressHint = getString(R.string.dialog_editchannel_hint_address_web);
                _channelLabelHint = getString(R.string.dialog_editchannel_hint_label_web);
                break;
            case Facebook:
                _dialogTitle = getString(R.string.dialog_editchannel_title_blank, label);
                _channelAddressHint = getString(R.string.dialog_editchannel_hint_address_facebook);
                _channelLabelHint = "";
                break;
            default:
                _dialogTitle = getString(R.string.dialog_editchannel_title_blank, label);
                _channelAddressHint = getString(R.string.dialog_editchannel_hint_address_default);
                _channelLabelHint = getString(R.string.dialog_editchannel_hint_label_default);
                break;
        }
    }

    /**
     * Initialize values for EditText to switch color.
     */
    private void initializeEditText() {
        editTextActionAddress.setOnEditorActionListener(_onEditorActionListener);
        editTextActionAddress.setText(_channel.actionAddress());
        floatLabelAddress.setHint(_channelAddressHint);

        if (_channel.channelType().equals(ChannelType.Facebook)) {
            editTextLabel.setVisibility(View.GONE);
            floatLabelChannelLabel.setVisibility(View.GONE);
        } else {
            editTextLabel.setText(_channel.label());
            floatLabelChannelLabel.setHint(_channelLabelHint);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }

    /**
     * Use the private string TAG from this class as an identifier.
     *
     * @param fragmentManager manager of fragments
     * @return this dialog
     */
    public EditChannelDialog show(FragmentManager fragmentManager) {
        show(fragmentManager, TAG);
        return this;
    }
}
