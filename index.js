import { NativeModules, NativeEventEmitter } from 'react-native';

const { Webim } = NativeModules;

const eventEmitter = new NativeEventEmitter(Webim);

Webim.onMessageAdded = callback => { 
    eventEmitter.addListener('messageAdded', (event) => {
        callback(event)
    });
 }



export default Webim;
