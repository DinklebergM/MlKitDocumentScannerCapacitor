import Foundation
import Capacitor

/**
 * Please read the Capacitor iOS Plugin Development Guide
 * here: https://capacitorjs.com/docs/plugins/ios
 */
@objc(CapacitorMlkitDocumentScannerPlugin)
public class CapacitorMlkitDocumentScannerPlugin: CAPPlugin, CAPBridgedPlugin {
    public let identifier = "CapacitorMlkitDocumentScannerPlugin"
    public let jsName = "CapacitorMlkitDocumentScanner"
    public let pluginMethods: [CAPPluginMethod] = [
        CAPPluginMethod(name: "echo", returnType: CAPPluginReturnPromise)
    ]
    private let implementation = CapacitorMlkitDocumentScanner()

    @objc func echo(_ call: CAPPluginCall) {
        let value = call.getString("value") ?? ""
        call.resolve([
            "value": implementation.echo(value)
        ])
    }
}
