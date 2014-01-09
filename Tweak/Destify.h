#import <Foundation/Foundation.h>
#import <UIKit/UIKit.h>
#import "cutils.h"

#define SETTINGSLOCATION @"/var/mobile/Library/Preferences/org.iLendSoft.Destify.plist"

@interface UIImage (Private)
+(UIImage *)_applicationIconImageForBundleIdentifier:(NSString *)bundleIdentifier format:(int)format scale:(CGFloat)scale;
@end

@interface BBBulletin
-(NSString *)title;
-(NSString *)subtitle;
-(NSString *)message;
-(NSString *)topic;
-(NSString *)sectionID;
@end

@interface Destify : NSObject {
    BOOL fileExists, cipherEnabled, enabled;
    NSMutableDictionary *properties;
    NSString *cipherKey, *_ip, *_port;
}

-(void) parseAndSend:(BBBulletin *) bulletin;
-(NSMutableData *) appendFileForID:(NSString *)_id toData:(NSMutableData *)body;
-(NSMutableData *) appendText:(NSString *)name value:(NSString *)value toData:(NSMutableData *)body;
-(NSData *) encode:(NSString *)stringToEncode;
-(NSString*) crypt:(NSString*)text withKey:(NSString*)code;
-(void)reload;
@end