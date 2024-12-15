package com.strateknia.talkie;

import javafx.beans.property.*;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

public class TalkieController {
    private static final Logger logger = LoggerFactory.getLogger(TalkieController.class);

    @FXML
    private ListView<String> lvUsers;

    @FXML
    private TextField tfUser;

    @FXML
    private Button btLogin;

    @FXML
    private Button btLogout;

    @FXML
    private Label lbToUser;

    @FXML
    private TextArea taResponses;

    @FXML
    private TextField tfMessage;

    @FXML
    private Button btSend;

    private final Property<String> connectedUser = new SimpleStringProperty();
    private final Property<TalkieMessage> response = new SimpleObjectProperty<>();
    private final AtomicReference<Consumer<String>> onUserAdded = new AtomicReference<>(x -> {});
    private final AtomicReference<Consumer<TalkieMessage>> onSendMessageHandler = new AtomicReference<>(x -> {});

    private final AtomicReference<String> toUser = new AtomicReference<>();

    public void initialize() {
        logger.info("Initializing UI");

        lvUsers.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);

        btLogin.setOnMouseClicked(event -> {
            if(isEmpty(tfUser.getText())) {
                return;
            }

            onUserAdded.get().accept(tfUser.getText());

            tfUser.setDisable(true);
            btLogin.setDisable(true);
            btLogout.setDisable(false);

            tfMessage.setDisable(false);
            btSend.setDisable(false);
        });

        btLogout.setOnMouseClicked(event -> {
            tfUser.setText(null);
            tfUser.setDisable(false);
            btLogin.setDisable(false);
            btLogout.setDisable(true);

            tfMessage.setDisable(true);
            btSend.setDisable(true);
        });

        btSend.setOnMouseClicked(event -> {
            if(isEmpty(tfMessage.getText())) {
                return;
            }

            TalkieMessage talkieMessage = TalkieMessage.builder()
                    .timestamp(Instant.now().toEpochMilli())
                    .text(tfMessage.getText())
                    .fromUser(tfUser.getText())
                    .toUser(toUser.get())
                    .topic(CommonConstants.TOPIC_MESSAGES)
                    .build();
            onSendMessageHandler.get().accept(talkieMessage);

            tfMessage.setText(null);
        });

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        response.addListener((observable, oldValue, newValue) -> {
            if(null == newValue) {
                return;
            }

            logger.info("{}",newValue);
            if(newValue.getFromUser().equals(toUser.get())) {
                Instant instant = Instant.ofEpochMilli(newValue.getTimestamp());
                LocalDateTime localDateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
                String response = String.format("%s - %s\n\t%s\n",
                        formatter.format(localDateTime),
                        newValue.getFromUser(),
                        newValue.getText()
                );

                taResponses.appendText(response);
            }
        });



        connectedUser.addListener((observable, oldValue, newValue) -> {
            if(null == newValue) {
                return;
            }
            if(!lvUsers.getItems().contains(newValue)) {
                lvUsers.getItems().add(newValue);
            }
        });


        lvUsers.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                String selectedUser = lvUsers.getSelectionModel().getSelectedItem();
                lbToUser.setText(selectedUser);
                toUser.set(selectedUser);
            } else {

            }
        });
    }

    public boolean isEmpty(String text) {
        return null == text || text.isEmpty() || text.isBlank();
    }

    public Property<TalkieMessage> responseProperty() {
        return response;
    }

    public Property<String> connectedUserProperty() {
        return connectedUser;
    }

    public void setOnUserAdded(Consumer<String> handler) {
        onUserAdded.set(handler);
    }

    public void setOnSendMessageHandler(Consumer<TalkieMessage> handler) {
        onSendMessageHandler.set(handler);
    }
}