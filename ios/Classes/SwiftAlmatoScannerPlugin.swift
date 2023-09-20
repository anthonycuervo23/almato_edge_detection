import Flutter
import UIKit

public class SwiftAlmatoScannerPlugin: NSObject, FlutterPlugin {
    
    var rootViewController: UIViewController?
    var result: FlutterResult?
    
    
    public override init() {
        super.init()
        rootViewController =
            (UIApplication.shared.delegate?.window??.rootViewController)!;
    }
    
    public static func register(with registrar: FlutterPluginRegistrar) {
        let channel = FlutterMethodChannel(name: Utils.channelName, binaryMessenger: registrar.messenger())
        let instance = SwiftAlmatoScannerPlugin()
        registrar.addMethodCallDelegate(instance, channel: channel)
    }
    
    public func handle(_ call: FlutterMethodCall, result: @escaping FlutterResult) {
        self.result = result
        typealias channelMethod = () -> ()
        var channelMethods : Dictionary = [String : channelMethod]()
        channelMethods["edge_detect"] = camera
        channelMethods["edge_detect_gallery"] = gallery
        if(!channelMethods.keys.contains(call.method)){
            result(FlutterMethodNotImplemented)
        }
        
        channelMethods[call.method]!();
        
    }
    
    private func camera() {
        DispatchQueue.main.async {
            let scannerViewController: ImageScannerController = ImageScannerController()
            scannerViewController.imageScannerDelegate = self
            scannerViewController.modalPresentationStyle = .fullScreen

            if #available(iOS 13.0, *) {
                scannerViewController.overrideUserInterfaceStyle = .dark
            }

            self.rootViewController?.present(scannerViewController, animated:true, completion:nil)
        }
    }
    
    func gallery() {
        DispatchQueue.main.async {
            let imagePicker = UIImagePickerController()
            imagePicker.delegate = self
            imagePicker.sourceType = .photoLibrary
            imagePicker.modalPresentationStyle = .popover

            self.rootViewController?.present(imagePicker, animated: true)
        }
    }
}

extension SwiftAlmatoScannerPlugin : ImageScannerControllerDelegate{
    
    public func imageScannerController(_ scanner: ImageScannerController, didFinishScanningWithResults results: ImageScannerResults) {
        scanner.dismiss(animated: true)
        let path = Utils.getScannedFile(results: results)
        result?(path)
    }
    
    public func imageScannerControllerDidCancel(_ scanner: ImageScannerController) {
        scanner.dismiss(animated: true)
    }
    
    public func imageScannerController(_ scanner: ImageScannerController, didFailWithError error: Error) {
        scanner.dismiss(animated: true)
    }
}

extension SwiftAlmatoScannerPlugin: UIImagePickerControllerDelegate, UINavigationControllerDelegate {
    public func imagePickerControllerDidCancel(_ picker: UIImagePickerController) {
        picker.dismiss(animated: true)
    }
    
    public func imagePickerController(_ picker: UIImagePickerController, didFinishPickingMediaWithInfo info: [UIImagePickerController.InfoKey: Any]) {
        picker.dismiss(animated: true)
        
        guard let image = info[.originalImage] as? UIImage else { return }
        pikedCamera(image: image)
    }
    
    private func pikedCamera(image: UIImage? = nil){
        let scannerViewController: ImageScannerController = ImageScannerController(image:image)
        scannerViewController.imageScannerDelegate = self
        scannerViewController.modalPresentationStyle = .fullScreen

        if #available(iOS 13.0, *) {
            scannerViewController.overrideUserInterfaceStyle = .dark
        }
        rootViewController?.present(scannerViewController, animated:true, completion:nil)
    }
}
