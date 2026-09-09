import SwiftUI

struct ContentView: View {
  private let loginURL = URL(string: "http://localhost:9100/login")!

  var body: some View {
    VStack(spacing: 16) {
      Image(systemName: "shield.checkered")
        .imageScale(.large)
        .foregroundStyle(.tint)
      Text("Explore IAM")
        .font(.title2.weight(.semibold))
      Text("Sign in with demo / demo-password, then authorize AI or Chat from those apps.")
        .font(.subheadline)
        .foregroundStyle(.secondary)
        .multilineTextAlignment(.center)
        .padding(.horizontal)
      Link("Open IAM Login", destination: loginURL)
        .buttonStyle(.borderedProminent)
    }
    .padding()
  }
}

#Preview {
  ContentView()
}
