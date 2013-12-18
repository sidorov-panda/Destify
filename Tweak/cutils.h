#define LOGPATH @"/var/tmp/tweak.log"

bool logToFile(NSString *s);
int validateIP4Dotted(const char *s);
int validatePortNumber(const char *s);

bool logToFile(NSString *s) {
    @autoreleasepool {
        NSStringEncoding enc = NSUTF8StringEncoding;
        NSFileHandle* fh = [NSFileHandle fileHandleForWritingAtPath:LOGPATH];
        
        /*
        if (!fh) {
            [[NSFileManager defaultManager] createFileAtPath:LOGPATH contents:nil attributes:nil];
            fh = [NSFileHandle fileHandleForWritingAtPath:LOGPATH];
        }
         */
        
        if (fh) {
            if (![s hasSuffix: @"\n"]) {
                s = [s stringByAppendingString: @"\n"];
            }
            
            @try {
                [fh seekToEndOfFile];
                [fh writeData:[s dataUsingEncoding:enc]];
            }
            @catch (NSException *e) {
                NSLog(@"Failed to log to file: %@", LOGPATH);
                return NO;
            }
            
            [fh closeFile];
            return YES;
            
        } else {
            return NO;
        }
    }
}

int validatePortNumber(const char *s) {
    int port = atoi(s);
    
    while(*s) {
        if (!isdigit(*s))
            return 0;
        else
            ++s;
    }
    
    if(port >= 0 && port <= 99999)
        return 1;
    
    return 0;
}

int validateIP4Dotted(const char *s) {
    int len = strlen(s);
    
    if (len < 7 || len > 15)
        return 0;
    
    char tail[16];
    tail[0] = 0;
    
    unsigned int d[4];
    int c = sscanf(s, "%3u.%3u.%3u.%3u%s", &d[0], &d[1], &d[2], &d[3], tail);
    
    if (c != 4 || tail[0])
        return 0;
    
    for (int i = 0; i < 4; i++)
        if (d[i] > 255)
            return 0;
    
    return 1;
}