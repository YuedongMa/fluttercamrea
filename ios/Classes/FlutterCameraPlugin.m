#import "FlutterCameraPlugin.h"
#import "TZImagePickerController.h"

@interface FlutterCameraPlugin ()<UINavigationControllerDelegate,UIImagePickerControllerDelegate,TZImagePickerControllerDelegate>
//@property (nonatomic, strong) UIImagePickerController *imagePickerVc;
@property (nonatomic, strong) FlutterResult resultBlock;
@property (nonatomic ,copy) NSString *imageCachPath;
@end


@implementation FlutterCameraPlugin
@synthesize imageCachPath = _imageCachPath;

+ (void)registerWithRegistrar:(NSObject<FlutterPluginRegistrar>*)registrar {
  FlutterMethodChannel* channel = [FlutterMethodChannel
      methodChannelWithName:@"flutter_camera_plugin"
            binaryMessenger:[registrar messenger]];
  FlutterCameraPlugin* instance = [[FlutterCameraPlugin alloc] init];
  [registrar addMethodCallDelegate:instance channel:channel];
}

- (void)handleMethodCall:(FlutterMethodCall*)call result:(FlutterResult)result {
    self.resultBlock =result;
  if ([@"getPlatformVersion" isEqualToString:call.method]) {
    result([@"iOS " stringByAppendingString:[[UIDevice currentDevice] systemVersion]]);
  }else if ([@"takePhoto" isEqualToString:call.method]){
      NSInteger count = (NSInteger)call.arguments;
      if (count == 0) {
          count = 1;
      }
      TZImagePickerController *imagePickerVc = [[TZImagePickerController alloc] initWithMaxImagesCount:count delegate:self];
      imagePickerVc.allowTakePicture = NO;
      // You can get the photos by block, the same as by delegate.
      // 你可以通过block或者代理，来得到用户选择的照片.
      __block NSMutableArray *imageList = [NSMutableArray array];
      [imagePickerVc setDidFinishPickingPhotosHandle:^(NSArray<UIImage *> *photos, NSArray *assets, BOOL isSelectOriginalPhoto) {
          if (assets.count>0) {
              for (PHAsset *asset in assets) {
                  NSArray *resources = [PHAssetResource assetResourcesForAsset:asset];
                  NSURL *urlPath = [(PHAssetResource*)resources[0] valueForKey:@"privateFileURL"];
                  NSString *path = urlPath.path;
                  [imageList addObject:path];
              }
              result(imageList);
          }else{
//              FlutterError *error = [FlutterError errorWithCode:@"402" message:@"未选择图片" details:@"未选择图片"];
              result(@[]);
          }
      }];
      //用户取消了照片选择
      [imagePickerVc setImagePickerControllerDidCancelHandle:^{
//                FlutterError *error = [FlutterError errorWithCode:@"401" message:@"获取失败" details:@"取消照片选择"];
          result(@[]);
      }];
      
      [[self viewControllerWithWindow:nil] presentViewController:imagePickerVc animated:YES completion:nil];

  } else if ([@"takeCamera" isEqualToString:call.method]) {
      UIImagePickerController * picker = [[UIImagePickerController alloc] init];
      if ([UIImagePickerController availableMediaTypesForSourceType:UIImagePickerControllerSourceTypeCamera]) {
          picker.sourceType = UIImagePickerControllerSourceTypeCamera;
          picker.delegate = self;
          [[self viewControllerWithWindow:nil] presentViewController:picker animated:YES completion:nil];
      }else{
//          FlutterError *error = [FlutterError errorWithCode:@"403" message:@"不允许使用相机" details:@"不允许使用相机"];
          result(@"");
      }
  } else {
    result(FlutterMethodNotImplemented);
  }
}

#pragma mark - UIImagePickerController delegate
- (void)imagePickerController:(UIImagePickerController *)picker didFinishPickingMediaWithInfo:(NSDictionary<NSString *,id> *)info {
    UIImage *selectedImage = info[UIImagePickerControllerEditedImage]?:info[UIImagePickerControllerOriginalImage];
    NSString *imageName = [NSString stringWithFormat:@"%f",[NSDate timeIntervalSinceReferenceDate]];
    imageName = [imageName stringByReplacingOccurrencesOfString:@" " withString:@""];
    imageName = [imageName stringByAppendingString:@".png"];
    [self saveImage:selectedImage withName:imageName];
    [picker dismissViewControllerAnimated:YES completion:nil];
}

//取消拍照
- (void)imagePickerControllerDidCancel:(UIImagePickerController *)picker{
//    FlutterError *error = [FlutterError errorWithCode:@"401" message:@"获取失败" details:@"取消拍照"];
//    if (self.resultBlock) {
//        self.resultBlock(error);
//    }
    if (self.resultBlock) {
        self.resultBlock(@"");
    }
}
//保存图片
- (void)saveImage:(UIImage *)currentImage withName:(NSString *)imageName {
    NSData *imageData =  UIImageJPEGRepresentation(currentImage, 0); // 1为不缩放保存,取值为(0~1)
    NSString *fullPath =  [self urlForImage:imageName].path;
    BOOL isSave = [imageData writeToFile:fullPath atomically:NO];
    NSLog(@" t图片地址。%@   -save=%@ ",fullPath,isSave?@"YES":@"NO");
    NSMutableArray *pathMArr = [NSMutableArray array];
    [pathMArr addObject:fullPath];
    if (self.resultBlock) {
        self.resultBlock(fullPath);
    }
}
//获取图片路径
- (NSURL *) urlForImage:(NSString *)fileName {
    [self createDir];
     NSString *pathForStoreName = [[self imImageCashPath] stringByAppendingPathComponent:fileName];
    return [NSURL fileURLWithPath:pathForStoreName];
}

- (NSString *)imImageCashPath {
     NSString *dataFilePath = [self urlForStoreName:@"IMCashImage"].path;
    return dataFilePath;
}

- (NSString *)applicationStorageDirectory {
    NSString *applicationName = [[[NSBundle mainBundle] infoDictionary] valueForKey:(NSString *)kCFBundleNameKey];
    return [[self directory:NSApplicationSupportDirectory] stringByAppendingPathComponent:applicationName];
}

- (NSURL *) urlForStoreName:(NSString *)storeFileName
{
    NSString *pathForStoreName = [[self applicationStorageDirectory] stringByAppendingPathComponent:storeFileName];
    return [NSURL fileURLWithPath:pathForStoreName];
}

- (NSString *) directory:(NSSearchPathDirectory)type
{
    return [NSSearchPathForDirectoriesInDomains(type, NSUserDomainMask, YES) lastObject];
}



- (void) createDir {
 
    NSFileManager *fileManager = [NSFileManager defaultManager];
    BOOL isDir = NO;
    BOOL existed = [fileManager fileExistsAtPath:_imageCachPath isDirectory:&isDir];
    if ( !(isDir == YES && existed == YES) ) {
        NSLog(@"+++++++++++++++++++%@",_imageCachPath);
        [fileManager createDirectoryAtPath:_imageCachPath withIntermediateDirectories:YES attributes:nil error:nil];
    }
//    [self fielName];
}


//获取当前Controller
- (UIViewController *)viewControllerWithWindow:(UIWindow *)window {
  UIWindow *windowToUse = window;
  if (windowToUse == nil) {
    for (UIWindow *window in [UIApplication sharedApplication].windows) {
      if (window.isKeyWindow) {
        windowToUse = window;
        break;
      }
    }
  }

  UIViewController *topController = windowToUse.rootViewController;
  while (topController.presentedViewController) {
    topController = topController.presentedViewController;
  }
  return topController;
}

@end
