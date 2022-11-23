import { BeeCore, Identity } from "../core.types";
import { Chat, Contact, ID, Message } from "./beeCore.type";
import { NativeEventEmitter, NativeModules } from 'react-native';
const { CoreModule } = NativeModules;
import 'fastestsmallesttextencoderdecoder';
import { Buffer } from 'buffer';

class RNBeeCore implements BeeCore {
    ready = false
    eventListener = null

    constructor() {

    }

    async bindService(): Promise<boolean> {
        return new Promise((res, rej)=>{
            try{
                CoreModule.startBind((val)=>{
                    this.ready = val
                    res(val)
                    const eventEmitter = new NativeEventEmitter(NativeModules.ToastExample);
                    this.eventListener = eventEmitter.addListener('MESSAGING', (event) => {
                       console.log(event) // "someValue"
                    });
                });
            }catch(e){
                rej(e)
            }

        })
    }

    async getIdentity(): Promise<Identity> {
        try {
            console.log("get identity called")
            const res = await CoreModule.getIdentity()
            const id: Identity = base64ToObject(res)
            console.log("get identity result",id)
            return id
        } catch (error) {
            throw error
        }
    }
    hasIdentity(): Promise<boolean> {
        return CoreModule.hasIdentity()
    }

    async newIdentity(name: any): Promise<Identity> {
        try {
            const res = await CoreModule.newIdentity(name)
            const id: Identity = base64ToObject(res)
            return id
        } catch (error) {
            throw error
        }
    }

    async newPMChat(contact: Contact): Promise<Chat> {
        const id = await this.getIdentity()
        const res = await CoreModule.newPMChat(contact._id)
        const newChat:Chat = base64ToObject(res)
        return newChat
    }
    async getPMChat(contact: Contact): Promise<Chat> {
        const res = await CoreModule.getPMChat(contact._id)
        const newChat:Chat = base64ToObject(res)
        return newChat
    }
    async getChat(chatID: ID) {
        console.log("getPMChat called with param: ",chatID)
        try {
            const res = await CoreModule.getChat(chatID.toString())
            const chat:Chat = base64ToObject(res)
            console.log("getPMChat chat is:", chat)
            return chat
        } catch (error) {
            throw error
        }

    }
    async getChatList(): Promise<Chat[]> {
        try {
            const res = await CoreModule.getChats()
            console.log("string response from getChats",res)
            const chat:Chat[] = base64ToObject(res)
            console.log(chat)
            console.log("parsed response from getChats",chat)
            return chat
        } catch (error) {
            throw error
        } 
    }
    async getChatMessages(chatId: string): Promise<Message[]> {
        try {
            const res = await CoreModule.getMessages(chatId)
            const msgs:Message[] = base64ToObject(res)
            return msgs.map((m)=> ({...m,createdAt:convertUnixTimeToLocalDate(m.createdAt)}))
        } catch (error) {
            throw error
        } 
    }
    async sendChatMessage(chatId: string, msg: Message): Promise<Message> {
        try {
            console.log("call send chat")
            const res = await CoreModule.sendMessage(chatId, msg.text)
            const ms:Message = base64ToObject(res)
            return {...ms,createdAt:convertUnixTimeToLocalDate(ms.createdAt)}
        } catch (error) {
            throw error
        } 
    }
    submitIncomingMessages(handler: (chatId: string, msg: Message) => void): void {

    }
    async getContactList(): Promise<Contact[]> {
        try {
            const res = await CoreModule.getContacts()
            const con:Contact[] = base64ToObject(res)
            return con
        } catch (error) {
            throw error
        } 
    }
    async newContact(contact: Contact): Promise<void> {
        try {
            const res = await CoreModule.addContact(contact._id, contact.name)
            return
        } catch (error) {
            throw error
        } 
    }

}

export const base64ToObject = <T>(msg: string): T => {
    return JSON.parse(Buffer.from(msg, 'base64').toString());
};

export const objectToBase64String = (obj: object): string => {
    const json = JSON.stringify(obj);
    return Buffer.from(json).toString('base64');
};


export const create = () => {
    return new RNBeeCore()
}

function convertUnixTimeToLocalDate(unixDate) {
    let date = new Date(unixDate*1000)
    // let newDate = new Date(date.getTime()+date.getTimezoneOffset()*60*1000);

    // let offset = date.getTimezoneOffset() / 60;
    // let hours = date.getHours();

    // newDate.setHours(hours - offset);

    return date;   
}