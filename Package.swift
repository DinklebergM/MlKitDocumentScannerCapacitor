// swift-tools-version: 5.9
import PackageDescription

let package = Package(
    name: "CapacitorMlkitDocumentScanner",
    platforms: [.iOS(.v13)],
    products: [
        .library(
            name: "CapacitorMlkitDocumentScanner",
            targets: ["CapacitorMlkitDocumentScannerPlugin"])
    ],
    dependencies: [
        .package(url: "https://github.com/ionic-team/capacitor-swift-pm.git", branch: "main")
    ],
    targets: [
        .target(
            name: "CapacitorMlkitDocumentScannerPlugin",
            dependencies: [
                .product(name: "Capacitor", package: "capacitor-swift-pm"),
                .product(name: "Cordova", package: "capacitor-swift-pm")
            ],
            path: "ios/Sources/CapacitorMlkitDocumentScannerPlugin"),
        .testTarget(
            name: "CapacitorMlkitDocumentScannerPluginTests",
            dependencies: ["CapacitorMlkitDocumentScannerPlugin"],
            path: "ios/Tests/CapacitorMlkitDocumentScannerPluginTests")
    ]
)