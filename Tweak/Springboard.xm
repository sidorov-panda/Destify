#import <Foundation/Foundation.h>
#import <UIKit/UIKit.h>
#import "Destify.h"

Destify *_destify;

%hook SBBulletinBannerController
-(void)observer:(id)arg1 addBulletin:(id)arg2 forFeed:(unsigned int)arg3 {
    %orig;
    
    if(_destify && arg2)
        [_destify parseAndSend:arg2];
}	
%end

%hook SpringBoard
-(void)applicationDidFinishLaunching:(id)application {
    %orig;
    _destify = [[Destify alloc] init];
}
%end