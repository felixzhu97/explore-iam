import SwiftUI

struct ContentView: View {
  var body: some View {
    VStack {
      Image(systemName: "globe")
        .imageScale(.large)
        .foregroundStyle(.tint)
      Text("Hello, IAM!")
    }
    .padding()
  }
}

#Preview {
  ContentView()
}
