// ICoreService.aidl
package fx.android.core;

import fx.android.core.ICoreServiceCallback;

// Declare any non-default types here with import statements


// So everything work with base64 json strings.

interface ICoreService {
    /**
     * add new contact
     */
    boolean addContact(String id, String name);
    /**
    * get chat info
    */
    String getChat(String id);
    /**
    * get list of chat info
    */
    String getChats();
    /**
    * get contact info
    */
    String getContact(String id);
    /**
    * get all contact info
    */
    String getContacts();
    /**
    * get identity
    */
    String getIdentity();
    /**
    * create new identity
    */
    String newIdentity(String name);
    /**
    * get all messages for a chat
    */
    String getMessages(String id);
    /**
    * return true if user is already generate identity
    */
    boolean isLogin();
    /**
    * create a new Private Chat with id of contact and ChatID
    */
    String newPMChat(String contactID);
    /**
    * retrun a Private chat by its contactID
    */
    String getPMChat(String contactID);
    /**
    * Send a text Message
    */
    String sendMessage(String chatID, String text);

        /**
         * Often you want to allow a service to call back to its clients.
         * This shows how to do so, by registering a callback interface with
         * the service.
         */
    oneway void registerCallback(ICoreServiceCallback cb);

        /**
         * Remove a previously registered callback interface.
         */
    oneway void unregisterCallback(ICoreServiceCallback cb);


}