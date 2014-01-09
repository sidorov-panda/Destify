#import "Destify.h"

Destify *_destify;

%hook SpringBoard
-(void)applicationDidFinishLaunching:(id)application {
    %orig;
    
    _destify = [[Destify alloc] init];
    [_destify reload];
}
%end

%hook SBBulletinBannerController
-(void)observer:(id)arg1 addBulletin:(id)arg2 forFeed:(unsigned int)arg3 {
    %orig;
    
    if(_destify && arg2)
        [_destify parseAndSend:arg2];
}	
%end

static void loadSettings(CFNotificationCenterRef center, void *observer, CFStringRef name, const void *object, CFDictionaryRef info) {
    [_destify reload];
}

%ctor {
    NSAutoreleasePool *pool = [[NSAutoreleasePool alloc] init];
    CFNotificationCenterAddObserver(CFNotificationCenterGetDarwinNotifyCenter(), NULL, &loadSettings, CFSTR("org.iLendSoft.Destify.settingsChanged"), NULL, 0);
    [pool release];
}