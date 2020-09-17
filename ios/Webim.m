#import "Webim.h"
#import "Webim-Swift.h"

@implementation Webim

WebimSession *webimSession;
MessageStream *stream;
MessageTracker *tracker;

RCT_EXPORT_MODULE()

- (NSArray<NSString *> *)supportedEvents {
    return @[@"newMessage", @"removeMessage", @"changedMessage", @"allMessagesRemoved", @"tokenUpdated", @"error"];
}


RCT_EXPORT_METHOD(resume:(NSDictionary*) builderData reject:(RCTResponseSenderBlock) reject resolve:(RCTResponseSenderBlock) resolve) {
    NSError *error = nil;
    if (webimSession == nil) {
        SessionBuilder *sessionBuilder = [Webim newSessionBuilder];
        NSString* accountName = [builderData valueForKey:@"accountName"];
        NSString* location = [builderData valueForKey:@"location"];

        // optional
        NSString* accountJSON = [builderData valueForKey:@"accountJSON"];
        NSString* providedAuthorizationToken = [builderData valueForKey: @"providedAuthorizationToken"];
        NSString* appVersion = [builderData valueForKey:@"appVersion"];
        NSNumber* clearVisitorData = [builderData valueForKey:@"clearVisitorData"];
        NSNumber* storeHistoryLocally = [builderData valueForKey:@"storeHistoryLocally"];
        NSString* title = [builderData valueForKey:@"title"];
        NSString* pushToken = [builderData valueForKey:@"pushToken"];
        sessionBuilder = [sessionBuilder setAccountName:accountName];
        sessionBuilder = [sessionBuilder setLocation:location];
        sessionBuilder = [sessionBuilder setFatalErrorHandler:(id<FatalErrorHandler>)self];

        if (accountJSON != nil) {
            sessionBuilder = [sessionBuilder setVisitorFieldsJSONString:accountJSON];
        }
        if (providedAuthorizationToken != nil) {
            sessionBuilder = [sessionBuilder setProvidedAuthorizationTokenStateListener:(id<ProvidedAuthorizationTokenStateListener>)self providedAuthorizationToken:providedAuthorizationToken];
        }
        if (appVersion != nil) {
            sessionBuilder = [sessionBuilder setAppVersion:appVersion];
        }
        if (clearVisitorData != nil) {
            sessionBuilder = [sessionBuilder setIsVisitorDataClearingEnabled:[clearVisitorData boolValue]];
        }
        if (storeHistoryLocally != nil) {
            sessionBuilder = [sessionBuilder setIsLocalHistoryStoragingEnabled:[storeHistoryLocally boolValue]];
        }
        if (title != nil) {
            sessionBuilder = [sessionBuilder setPageTitle:title];
        }
        if (pushToken != nil) {
            sessionBuilder = [sessionBuilder setDeviceToken:pushToken];
        }
        sessionBuilder = [sessionBuilder setIsVisitorDataClearingEnabled:true];
        sessionBuilder = [sessionBuilder setIsLocalHistoryStoragingEnabled:false];
        webimSession = [sessionBuilder build:&error];
        if (error) {
            reject(@[@{ @"message": [error localizedDescription]}]);
            return;
        }
    }
    [webimSession resume:&error];
    if (error) {
        reject(@[@{ @"message": [error localizedDescription]}]);
        return;
    }
    stream = [webimSession getStream];
    [stream setChatRead:&error];
    if (error) {
        reject(@[@{ @"message": [error localizedDescription]}]);
        return;
    }
    tracker = [stream newMessageTrackerWithMessageListener:(id<MessageListener>)self error:&error];
    if (error) {
        reject(@[@{ @"message": [error localizedDescription]}]);
        return;
    }
    [stream startChat:&error];
    if (error) {
        reject(@[@{ @"message": [error localizedDescription]}]);
        return;
    }
    resolve(@[@{}]);
}

RCT_EXPORT_METHOD(sampleMethod:(NSString *)stringArgument numberParameter:(nonnull NSNumber *)numberArgument callback:(RCTResponseSenderBlock)callback)
{
    // TODO: Implement some actually useful functionality
    callback(@[[NSString stringWithFormat: @"numberArgument: %@ stringArgument: %@", numberArgument, stringArgument]]);
}

@end
