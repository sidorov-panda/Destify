#import "Destify.h"
#import <CommonCrypto/CommonCryptor.h>
#import <objc/runtime.h>
#import "SBApplication.h"

@implementation Destify
-(void) parseAndSend:(BBBulletin *) bulletin {
    NSString *_title = bulletin.title;
    NSString *_subtitle = bulletin.subtitle;
    NSString *_message = bulletin.message;
    NSString *_topic = bulletin.topic;
    NSString *_id = bulletin.sectionID;
    NSString *cipherKey;
    
    BOOL fileExists = [[NSFileManager defaultManager] fileExistsAtPath:SETTINGSLOCATION];
    NSMutableDictionary *properties;
    
    if(fileExists) {
        properties = [[[NSMutableDictionary alloc] initWithContentsOfFile:SETTINGSLOCATION] retain];
    } else {
        properties = [[[NSMutableDictionary alloc] init] retain];
    }
    
    BOOL cipherEnabled = [properties objectForKey:@"cipherState"] ? [[properties objectForKey:@"cipherState"] boolValue] : NO;
    
    if(cipherEnabled) {
        cipherKey = [properties objectForKey:@"key"] ? [properties objectForKey:@"key"] : @"";
        NSString *trimmed = [cipherKey stringByTrimmingCharactersInSet:[NSCharacterSet whitespaceCharacterSet]];
        
        if([trimmed length] == 0) { cipherEnabled = NO; }
    }
    
    BOOL enabled = [properties objectForKey:@"enabled"] ? [[properties objectForKey:@"enabled"] boolValue] : NO;
    
    if(enabled) {
        NSString *_ip = [properties objectForKey:@"ip"] ? [properties objectForKey:@"ip"] : @"127.0.0.1";
        _ip = [_ip stringByTrimmingCharactersInSet:[NSCharacterSet whitespaceCharacterSet]];
        
        NSString *_port = [properties objectForKey:@"port"] ? [properties objectForKey:@"port"] : @"3128";
        _port = [_port stringByTrimmingCharactersInSet:[NSCharacterSet whitespaceCharacterSet]];
        
        if(validateIP4Dotted([_ip UTF8String]) == 1 && validatePortNumber([_port UTF8String]) == 1) {
            NSURL *_url = [NSURL URLWithString:[NSString stringWithFormat:@"http://%@:%@", _ip, _port]];
            
            NSDateFormatter *formatter = [[[NSDateFormatter alloc] init] retain];
            [formatter setDateFormat:@"HH:mm"];
            
            NSString *_time = [formatter stringFromDate:[NSDate date]];
            [formatter release];
            
            NSMutableData *body = [[[NSMutableData alloc] init] retain];
            
            body = [self appendText:@"--" value:@"--" toData:body cipherState:cipherEnabled cipherKey:cipherKey];
            body = [self appendText:@"check" value:@"Destify" toData:body cipherState:cipherEnabled cipherKey:cipherKey];
            body = [self appendText:@"title" value:_title toData:body cipherState:cipherEnabled cipherKey:cipherKey];
            body = [self appendText:@"subtitle" value:_subtitle toData:body cipherState:cipherEnabled cipherKey:cipherKey];
            body = [self appendText:@"message" value:_message toData:body cipherState:cipherEnabled cipherKey:cipherKey];
            body = [self appendText:@"topic" value:_topic toData:body cipherState:cipherEnabled cipherKey:cipherKey];
            body = [self appendText:@"time" value:_time toData:body cipherState:cipherEnabled cipherKey:cipherKey];
            body = [self appendFileForID:_id toData:body cipherState:cipherEnabled cipherKey:cipherKey];
            body = [self appendText:@"--" value:@"--" toData:body cipherState:cipherEnabled cipherKey:cipherKey];
            
            NSMutableURLRequest *request = [[[NSMutableURLRequest alloc] init] retain];
            [request setURL:_url];
            [request setHTTPMethod:@"POST"];
            [request setHTTPBody:body];
            
            [NSURLConnection sendAsynchronousRequest:request
                                               queue:[NSOperationQueue currentQueue]
                                   completionHandler:^(NSURLResponse *response, NSData *data, NSError *error) {
                                       [request release];
                                       [body release];
                                       [formatter release];
                                       [properties release];
                                   }];
        }
    }
    
    [properties release];
}

-(NSMutableData *) appendFileForID:(NSString *)_id toData:(NSMutableData *)body cipherState:(BOOL)cipherEnabled cipherKey:(NSString *)cipherKey {
    NSString *imagePath = [self getImagePathForIdentifier:_id];
    NSData *imgData = [NSData dataWithContentsOfFile:imagePath];
    body = [self appendText:@"icon" value:imgData.description toData:body cipherState:cipherEnabled cipherKey:cipherKey];
    return body;
}

-(NSData *) encode:(NSString *)stringToEncode {
    return [stringToEncode dataUsingEncoding:NSUTF8StringEncoding allowLossyConversion:YES];
}

-(NSMutableData *) appendText:(NSString *)name value:(NSString *)value toData:(NSMutableData *)body cipherState:(BOOL)cipherEnabled cipherKey:(NSString *)cipherKey {
    if(![name isEqualToString:@"icon"] && ![name isEqualToString:@"time"]) {
        if(cipherEnabled) {
            value = [self crypt:value withKey:cipherKey];
        }
    }
    
    NSString *ts = [NSString stringWithFormat:@"%@=%@", name, value];
    [body appendData:[self encode:ts]];
    [body appendData:[self encode:@"\r\n"]];
    
    return body;
}

-(NSString *) getImagePathForIdentifier:(NSString *)bundleIdentifier {
    NSBundle *bundle = [NSBundle bundleWithIdentifier:bundleIdentifier];
    NSString *bundlePath;
    
    if(bundle) {
        bundlePath = [bundle bundlePath];
    } else {
        SBApplication *application = [[objc_getClass("SBApplicationController") sharedInstance] applicationWithDisplayIdentifier:bundleIdentifier];
        bundlePath = application.path;
    }
    
    NSDictionary* plist = nil;
    plist = [NSDictionary dictionaryWithContentsOfFile:[bundlePath stringByAppendingPathComponent:@"Info.plist"]];
    
    for(NSString *s in plist) {
        if ([s isEqualToString:@"CFBundleIconFile"]) {
            return [bundlePath stringByAppendingPathComponent:[self checkFileName:[plist objectForKey:s] forFileType:@"png"]];
        }
    }
    
    plist = [NSDictionary dictionaryWithContentsOfFile:@"/var/mobile/Library/Caches/com.apple.mobile.installation.plist"];
    NSDictionary *system = [plist objectForKey:@"System"];
    NSDictionary *user = [plist objectForKey:@"User"];
    
    NSString *imagePath = nil;
    
    if ([system objectForKey:bundleIdentifier]) {
        NSDictionary *icons = [[[[system objectForKey:bundleIdentifier] objectForKey:@"CFBundleIcons"] objectForKey:@"CFBundlePrimaryIcon"] objectForKey:@"CFBundleIconFiles"];
        
        for(NSString *s in icons) {
            if (UI_USER_INTERFACE_IDIOM() == UIUserInterfaceIdiomPad) {
                return [bundlePath stringByAppendingPathComponent:[self checkFileName:s forFileType:@"png"]];
                
            } else if ([s rangeOfString:@"@2x"].location != NSNotFound) {
                return [bundlePath stringByAppendingPathComponent:[self checkFileName:s forFileType:@"png"]];
            }
        }
    } else if ([user objectForKey:bundleIdentifier]) {
        NSDictionary *icons = [[[[user objectForKey:bundleIdentifier] objectForKey:@"CFBundleIcons"] objectForKey:@"CFBundlePrimaryIcon"] objectForKey:@"CFBundleIconFiles"];
        
        for(NSString *s in icons) {
            if (UI_USER_INTERFACE_IDIOM() == UIUserInterfaceIdiomPad) {
                return [bundlePath stringByAppendingPathComponent:[self checkFileName:s forFileType:@"png"]];
                
            } else if ([s rangeOfString:@"@2x"].location != NSNotFound) {
                return [bundlePath stringByAppendingPathComponent:[self checkFileName:s forFileType:@"png"]];
            }
        }
    }
    
    imagePath = [bundlePath stringByAppendingPathComponent:@"Icon@2x.png"];
    if([[NSFileManager defaultManager] fileExistsAtPath:imagePath]) { return imagePath; }
    
    imagePath = [bundlePath stringByAppendingPathComponent:@"icon@2x.png"];
    if([[NSFileManager defaultManager] fileExistsAtPath:imagePath]) { return imagePath; }
    
    imagePath = [bundlePath stringByAppendingPathComponent:@"Icon.png"];
    if([[NSFileManager defaultManager] fileExistsAtPath:imagePath]) { return imagePath; }
    
    imagePath = [bundlePath stringByAppendingPathComponent:@"icon.png"];
    if([[NSFileManager defaultManager] fileExistsAtPath:imagePath]) { return imagePath; }
    
    return nil;
}

-(NSString *) checkFileName:(NSString *)n forFileType:(NSString*)f {
    if([n rangeOfString:[NSString stringWithFormat:@".%@", f]].location != NSNotFound) {
        return n;
    } else {
        return [n stringByAppendingString:[NSString stringWithFormat:@".%@", f]];
    }
}

-(NSString*) crypt:(NSString*)text withKey:(NSString*)code {
    NSMutableString *ctext = [[[NSMutableString alloc] init] autorelease];
    int code_length = code.length;
    int text_length = text.length;
    
    for(int i = 0, a = 0; i < text_length; i++) {
        int aiCh = (int) [text characterAtIndex:i];;
        int kiCh = (int) [[code uppercaseString] characterAtIndex:(a % code_length)];;
        int niCh = (int) ' ';
        
        if(aiCh >= 65 && aiCh <= 90) {
            aiCh = [text characterAtIndex:i];
            niCh = (aiCh +kiCh -65);
            
            if(niCh > 90){ niCh -= 26; }
            
            [ctext appendString:[NSString stringWithFormat:@"%c", niCh]];
            a++;
            
        } else if(aiCh >= 97 && aiCh <= 122) {
            aiCh = [text characterAtIndex:i];
            niCh = (aiCh +kiCh -65);
            
            if(niCh <= 97){ niCh += 26; }
            if(niCh > 122){ niCh -= 26; }
            
            [ctext appendString:[NSString stringWithFormat:@"%c", niCh]];
            a++;
            
        } else {
            niCh = aiCh;
            [ctext appendString:[NSString stringWithFormat:@"%c", niCh]];
        }
    }
    
    return ctext;
}
@end