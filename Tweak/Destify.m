#import "Destify.h"

@implementation Destify
-(void) parseAndSend:(BBBulletin *) bulletin {
    NSString *_title = bulletin.title;
    NSString *_subtitle = bulletin.subtitle;
    NSString *_message = bulletin.message;
    NSString *_topic = bulletin.topic;
    NSString *_id = bulletin.sectionID;
    
    if(enabled && validateIP4([_ip UTF8String]) && validatePortNumner([_port UTF8String])) {
        NSURL *_url = [NSURL URLWithString:[NSString stringWithFormat:@"http://%@:%@", _ip, _port]];
        
        NSDateFormatter *formatter = [[[NSDateFormatter alloc] init] retain];
        [formatter setDateFormat:@"HH:mm"];
        
        NSString *_time = [formatter stringFromDate:[NSDate date]];
        [formatter release];
        
        NSMutableData *body = [[[NSMutableData alloc] init] retain];
        
        body = [self appendText:@"--" value:@"--" toData:body];
        body = [self appendText:@"check" value:@"Destify" toData:body];
        body = [self appendText:@"title" value:_title toData:body];
        body = [self appendText:@"subtitle" value:_subtitle toData:body];
        body = [self appendText:@"message" value:_message toData:body];
        body = [self appendText:@"topic" value:_topic toData:body];
        body = [self appendText:@"time" value:_time toData:body];
        body = [self appendFileForID:_id toData:body];
        body = [self appendText:@"--" value:@"--" toData:body];
        
        NSMutableURLRequest *request = [[[NSMutableURLRequest alloc] init] retain];
        [request setURL:_url];
        [request setHTTPMethod:@"POST"];
        [request setHTTPBody:body];
        [request setTimeoutInterval:15];
        request.cachePolicy = NSURLRequestReloadIgnoringLocalCacheData;
        
        [NSURLConnection sendAsynchronousRequest:request queue:[NSOperationQueue currentQueue]
                               completionHandler:^(NSURLResponse *response, NSData *data, NSError *error) {
                                   [request release];
                                   [body release];
                                   [formatter release];
                                   [properties release];
                               }];
    }
}

-(NSMutableData *) appendFileForID:(NSString *)_id toData:(NSMutableData *)body {
	UIImage *largeIcon = [UIImage _applicationIconImageForBundleIdentifier:_id format:0 scale:[UIScreen mainScreen].scale];
    NSData *imageData = [NSData dataWithData:UIImagePNGRepresentation(largeIcon)];
    
    return [self appendText:@"icon" value:imageData.description toData:body];
}

-(NSMutableData *) appendText:(NSString *)name value:(NSString *)value toData:(NSMutableData *)body {
    if(![name isEqualToString:@"icon"] && ![name isEqualToString:@"time"] && cipherEnabled)
        value = [self crypt:value withKey:cipherKey];
    
    [body appendData:[self encode:[NSString stringWithFormat:@"%@=%@\r\n", name, value]]];
    
    return body;
}

-(NSData *) encode:(NSString *)stringToEncode {
    return [stringToEncode dataUsingEncoding:NSUTF8StringEncoding allowLossyConversion:YES];
}

-(NSString*) crypt:(NSString*)text withKey:(NSString*)code {
    NSMutableString *ctext = [[[NSMutableString alloc] init] autorelease];
    NSUInteger code_length = code.length;
    NSUInteger text_length = text.length;
    
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

-(void)reload {
    [properties release];
    fileExists = [[NSFileManager defaultManager] fileExistsAtPath:SETTINGSLOCATION];
    
    if(fileExists) {
        properties = [[[NSMutableDictionary alloc] initWithContentsOfFile:SETTINGSLOCATION] retain];
    } else {
        properties = [[[NSMutableDictionary alloc] init] retain];
    }
    
    cipherEnabled = [properties objectForKey:@"cipherState"] ? [[properties objectForKey:@"cipherState"] boolValue] : NO;
    enabled = [properties objectForKey:@"enabled"] ? [[properties objectForKey:@"enabled"] boolValue] : NO;
    
    if(cipherEnabled) {
        cipherKey = [properties objectForKey:@"key"] ? [properties objectForKey:@"key"] : @"";
        NSString *trimmed = [cipherKey stringByTrimmingCharactersInSet:[NSCharacterSet whitespaceCharacterSet]];
        
        if([trimmed length] == 0) { cipherEnabled = NO; }
    }
    
    _ip = [properties objectForKey:@"ip"] ? [properties objectForKey:@"ip"] : @"127.0.0.1";
    _ip = [_ip stringByTrimmingCharactersInSet:[NSCharacterSet whitespaceCharacterSet]];
    
    _port = [properties objectForKey:@"port"] ? [properties objectForKey:@"port"] : @"3128";
    _port = [_port stringByTrimmingCharactersInSet:[NSCharacterSet whitespaceCharacterSet]];
}
@end