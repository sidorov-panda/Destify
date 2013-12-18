#import <Foundation/Foundation.h>
#import <UIKit/UIKit.h>
#import "BBBulletin.h"
#import "cutils.h"

#define SETTINGSLOCATION @"/var/mobile/Library/Preferences/org.iLendSoft.Destify.plist"

@interface Destify : NSObject
-(void) parseAndSend:(BBBulletin *) bulletin;
-(NSString *) getImagePathForIdentifier:(NSString *)bundleIdentifier;
-(NSMutableData *) appendFileForID:(NSString *)_id toData:(NSMutableData *)body cipherState:(BOOL)cipherEnabled cipherKey:(NSString *)cipherKey;
-(NSMutableData *) appendText:(NSString *)name value:(NSString *)value toData:(NSMutableData *)body cipherState:(BOOL)cipherEnabled cipherKey:(NSString *)cipherKey;
-(NSData *) encode:(NSString *)s;
-(NSString *) checkFileName:(NSString *)n forFileType:(NSString *)f;
-(NSString*) crypt:(NSString*)text withKey:(NSString*)code;
@end
