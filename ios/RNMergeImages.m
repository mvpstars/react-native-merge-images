
#import "RNMergeImages.h"

@implementation RNMergeImages

- (dispatch_queue_t)methodQueue
{
    return dispatch_get_main_queue();
}
RCT_EXPORT_MODULE()

RCT_EXPORT_METHOD(mergeImages:(NSDictionary *)options
                  failureCallback:(RCTResponseErrorBlock)failureCallback
                  successCallback:(RCTResponseSenderBlock)successCallback)
{
    NSURL *URL = [RCTConvert NSURL:options[@"url"]];
    
}
@end
  
