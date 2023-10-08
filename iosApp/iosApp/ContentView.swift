import UIKit
import SwiftUI
import shared

struct ComposeView: UIViewControllerRepresentable {
    
    let component: OpenOtpAppComponent
    
    func makeUIViewController(context: Context) -> UIViewController {
        Main_iosKt.MainViewController(component: component)
    }
    
    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}

struct ContentView: View {
    
    let component: OpenOtpAppComponent
    
    @Environment(\.colorScheme) var colorScheme
    
    var body: some View {
        let backgroundColor = Color(
            colorScheme == .dark
            ? SwiftHelpersKt.MD_THEME_DARK_BACKGROUND
            : SwiftHelpersKt.MD_THEME_LIGHT_BACKGROUND
        )
        ZStack {
            backgroundColor
                .edgesIgnoringSafeArea(.all)
            
            ComposeView(component: component)
                .ignoresSafeArea(.all)
        }.ignoresSafeArea(.all)
    }
}
