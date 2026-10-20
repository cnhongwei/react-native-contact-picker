#import "ContactPicker.h"
#import <React/RCTUtils.h>

@implementation ContactPicker {
  RCTPromiseResolveBlock _resolve;
  RCTPromiseRejectBlock _reject;
  CNContactPickerViewController *_picker;
  BOOL _invalidated;
}

- (void)pickContactPhone:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject
{
  dispatch_async(dispatch_get_main_queue(), ^{
    if (self->_invalidated) {
      reject(@"E_INVALIDATED", @"The contact picker is no longer available.", nil);
      return;
    }
    if (self->_resolve != nil) {
      reject(@"E_PICKER_BUSY", @"A contact picker is already open.", nil);
      return;
    }
    UIViewController *presenter = RCTPresentedViewController();
    if (presenter == nil || presenter.view.window == nil || presenter.isBeingDismissed) {
      reject(@"E_NO_VIEW_CONTROLLER", @"There is no active view to present the contact picker.", nil);
      return;
    }
    self->_resolve = [resolve copy];
    self->_reject = [reject copy];
    self->_picker = [CNContactPickerViewController new];
    self->_picker.delegate = self;
    self->_picker.displayedPropertyKeys = @[CNContactPhoneNumbersKey];
    self->_picker.predicateForEnablingContact = [NSPredicate predicateWithFormat:@"phoneNumbers.@count > 0"];
    // Always open the contact's phone properties, including for multiple numbers.
    self->_picker.predicateForSelectionOfContact = [NSPredicate predicateWithValue:NO];
    self->_picker.predicateForSelectionOfProperty =
      [NSPredicate predicateWithFormat:@"key == %@", CNContactPhoneNumbersKey];
    [presenter presentViewController:self->_picker animated:YES completion:nil];
    self->_picker.presentationController.delegate = self;
  });
}

- (void)finish:(id)result
{
  RCTPromiseResolveBlock resolve = _resolve;
  [self clearPending];
  if (resolve) resolve(result);
}

- (void)clearPending
{
  _picker.delegate = nil;
  _picker.presentationController.delegate = nil;
  _picker = nil;
  _resolve = nil;
  _reject = nil;
}

- (void)contactPicker:(CNContactPickerViewController *)picker
    didSelectContactProperty:(CNContactProperty *)property
{
  if (![property.key isEqualToString:CNContactPhoneNumbersKey] ||
      ![property.value isKindOfClass:[CNPhoneNumber class]]) {
    RCTPromiseRejectBlock reject = _reject;
    [self clearPending];
    if (reject) reject(@"E_INVALID_RESULT", @"No phone number was selected.", nil);
    return;
  }
  NSString *number = ((CNPhoneNumber *)property.value).stringValue;
  if (number.length == 0) {
    RCTPromiseRejectBlock reject = _reject;
    [self clearPending];
    if (reject) reject(@"E_INVALID_RESULT", @"The selected phone number is empty.", nil);
    return;
  }
  NSString *name = [CNContactFormatter stringFromContact:property.contact style:CNContactFormatterStyleFullName];
  [self finish:@{@"name": name ?: @"", @"phoneNumber": number}];
}

- (void)contactPickerDidCancel:(CNContactPickerViewController *)picker
{
  [self finish:[NSNull null]];
}

- (void)presentationControllerDidDismiss:(UIPresentationController *)presentationController
{
  [self finish:[NSNull null]];
}

- (void)invalidate
{
  dispatch_async(dispatch_get_main_queue(), ^{
    self->_invalidated = YES;
    [self->_picker dismissViewControllerAnimated:NO completion:nil];
    RCTPromiseRejectBlock reject = self->_reject;
    [self clearPending];
    if (reject) reject(@"E_INVALIDATED", @"The contact picker was invalidated.", nil);
  });
}

- (std::shared_ptr<facebook::react::TurboModule>)getTurboModule:
    (const facebook::react::ObjCTurboModule::InitParams &)params
{
  return std::make_shared<facebook::react::NativeContactPickerSpecJSI>(params);
}

+ (NSString *)moduleName { return @"ContactPicker"; }
@end
