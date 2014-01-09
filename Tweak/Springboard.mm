#line 1 "/Users/ils/Dropbox/Projects/Destify/Tweak/Destify/Springboard.xm"
#import "Destify.h"

Destify *_destify;

#include <logos/logos.h>
#include <substrate.h>
@class SBBulletinBannerController; @class SpringBoard; 
static void (*_logos_orig$_ungrouped$SpringBoard$applicationDidFinishLaunching$)(SpringBoard*, SEL, id); static void _logos_method$_ungrouped$SpringBoard$applicationDidFinishLaunching$(SpringBoard*, SEL, id); static void (*_logos_orig$_ungrouped$SBBulletinBannerController$observer$addBulletin$forFeed$)(SBBulletinBannerController*, SEL, id, id, unsigned int); static void _logos_method$_ungrouped$SBBulletinBannerController$observer$addBulletin$forFeed$(SBBulletinBannerController*, SEL, id, id, unsigned int); 

#line 5 "/Users/ils/Dropbox/Projects/Destify/Tweak/Destify/Springboard.xm"

static void _logos_method$_ungrouped$SpringBoard$applicationDidFinishLaunching$(SpringBoard* self, SEL _cmd, id application) {
    _logos_orig$_ungrouped$SpringBoard$applicationDidFinishLaunching$(self, _cmd, application);
    
    _destify = [[Destify alloc] init];
    [_destify reload];
}



static void _logos_method$_ungrouped$SBBulletinBannerController$observer$addBulletin$forFeed$(SBBulletinBannerController* self, SEL _cmd, id arg1, id arg2, unsigned int arg3) {
    _logos_orig$_ungrouped$SBBulletinBannerController$observer$addBulletin$forFeed$(self, _cmd, arg1, arg2, arg3);
    
    if(_destify && arg2)
        [_destify parseAndSend:arg2];
}	


static void loadSettings(CFNotificationCenterRef center, void *observer, CFStringRef name, const void *object, CFDictionaryRef info) {
    [_destify reload];
}

static __attribute__((constructor)) void _logosLocalCtor_4028b031() {
    NSAutoreleasePool *pool = [[NSAutoreleasePool alloc] init];
    CFNotificationCenterAddObserver(CFNotificationCenterGetDarwinNotifyCenter(), NULL, &loadSettings, CFSTR("org.iLendSoft.Destify.settingsChanged"), NULL, 0);
    [pool release];
}
static __attribute__((constructor)) void _logosLocalInit() {
{Class _logos_class$_ungrouped$SpringBoard = objc_getClass("SpringBoard"); MSHookMessageEx(_logos_class$_ungrouped$SpringBoard, @selector(applicationDidFinishLaunching:), (IMP)&_logos_method$_ungrouped$SpringBoard$applicationDidFinishLaunching$, (IMP*)&_logos_orig$_ungrouped$SpringBoard$applicationDidFinishLaunching$);Class _logos_class$_ungrouped$SBBulletinBannerController = objc_getClass("SBBulletinBannerController"); MSHookMessageEx(_logos_class$_ungrouped$SBBulletinBannerController, @selector(observer:addBulletin:forFeed:), (IMP)&_logos_method$_ungrouped$SBBulletinBannerController$observer$addBulletin$forFeed$, (IMP*)&_logos_orig$_ungrouped$SBBulletinBannerController$observer$addBulletin$forFeed$);} }
#line 32 "/Users/ils/Dropbox/Projects/Destify/Tweak/Destify/Springboard.xm"
