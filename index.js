import { NativeModules, NativeEventEmitter } from 'react-native';

const { Webim } = NativeModules;

const eventEmitter = new NativeEventEmitter(Webim);

//** Callback, when new message added */
const onMessageAdded = callback => {
    eventEmitter.addListener('messageAdded', (event) => {
        callback(event)
    });
}

const resume = (params = {}) => {
    return Webim.resume({
        ...params,
        userFields: params.userFields ? JSON.stringify(params.userFields) : null,
    })
}

const getLastMessages = (
    count = 10,
    errorCallback = () => { },
    successCallback = () => { }) => {
    Webim.getLastMessages(count, errorCallback, successCallback)
}

const sendMessage = (text = '') => {
    return Webim.sendMessage(text)
}



export default {
    onMessageAdded,
    resume,
    getLastMessages,
    sendMessage,
};
