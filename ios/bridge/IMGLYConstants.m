#import "IMGLYConstants.h"

@implementation IMGLYConstants

// Error codes
NSString* const IMGLYErrorExportFailed = @"E_EXPORT_FAILED";
NSString* const IMGLYErrorParsing = @"E_PARSING";
NSString* const IMGLYErrorImplementationMissing = @"E_IMPLEMENTATION_MISSING";

// Error messages
NSString* const IMGLYErrorExportFailedMessage = @"The export of the asset failed due to: ";
NSString* const IMGLYErrorParsingMessage = @"Unable to parse the argument(s): ";
NSString* const IMGLYErrorImplementationMissingMessage =
  @"The Swift implementation of the IMGLYEditor module was not linked into the app.";

@end
