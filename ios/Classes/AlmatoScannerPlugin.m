#import "AlmatoScannerPlugin.h"
#if __has_include(<almato_scanner/almato_scanner-Swift.h>)
#import <almato_scanner/almato_scanner-Swift.h>
#else
// Support project import fallback if the generated compatibility header
// is not copied when this plugin is created as a library.
// https://forums.swift.org/t/swift-static-libraries-dont-copy-generated-objective-c-header/19816
#import "almato_scanner-Swift.h"
#endif

@implementation AlmatoScannerPlugin
+ (void)registerWithRegistrar:(NSObject<FlutterPluginRegistrar>*)registrar {
  [SwiftAlmatoScannerPlugin registerWithRegistrar:registrar];
}
@end
