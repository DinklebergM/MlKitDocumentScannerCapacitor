// swift-tools-version: 5.9
import PackageDescription

let package = Package(
    name: "DkbCapacitorDocumentScanner",
    platforms: [.iOS(.v13)],
    products: [
        .library(
            name: "DkbCapacitorDocumentScanner",
            targets: ["DocumentScannerPlugin"])
    ],
    dependencies: [
        .package(url: "https://github.com/ionic-team/capacitor-swift-pm.git", branch: "main")
    ],
    targets: [
        .target(
            name: "DocumentScannerPlugin",
            dependencies: [
                .product(name: "Capacitor", package: "capacitor-swift-pm"),
                .product(name: "Cordova", package: "capacitor-swift-pm")
            ],
            path: "ios/Sources/DocumentScannerPlugin"),
        .testTarget(
            name: "DocumentScannerPluginTests",
            dependencies: ["DocumentScannerPlugin"],
            path: "ios/Tests/DocumentScannerPluginTests")
    ]
)