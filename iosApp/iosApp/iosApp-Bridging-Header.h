#ifndef iosApp_Bridging_Header_h
#define iosApp_Bridging_Header_h

#import <Foundation/Foundation.h>

NS_SWIFT_NAME(FirebaseApp)
@interface FIRApp : NSObject
+ (void)configure;
@end

#endif
