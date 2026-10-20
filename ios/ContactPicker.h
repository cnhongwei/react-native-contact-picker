#import <ContactPickerSpec/ContactPickerSpec.h>
#import <ContactsUI/ContactsUI.h>
#import <React/RCTInvalidating.h>

@interface ContactPicker : NSObject <NativeContactPickerSpec, CNContactPickerDelegate,
                                    UIAdaptivePresentationControllerDelegate, RCTInvalidating>
@end
