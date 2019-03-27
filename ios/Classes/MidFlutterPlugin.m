#import "MidFlutterPlugin.h"
#import <mid_flutter/mid_flutter-Swift.h>

@implementation MidFlutterPlugin
+ (void)registerWithRegistrar:(NSObject<FlutterPluginRegistrar>*)registrar {
  [SwiftMidFlutterPlugin registerWithRegistrar:registrar];
}
@end
