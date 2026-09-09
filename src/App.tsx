/**
 * @license
 * SPDX-License-Identifier: Apache-2.0
 */

export default function App() {
  return (
    <div className="min-h-screen bg-neutral-900 text-neutral-100 flex flex-col items-center justify-center p-6 font-sans">
      <div className="max-w-xl w-full bg-neutral-800 rounded-2xl p-8 border border-neutral-700 shadow-2xl text-center">
        <h1 className="text-2xl font-bold text-white mb-2">Vertical 3D Island Explorer</h1>
        <p className="text-emerald-400 mb-8 font-semibold">GitHub Actions CI/CD Configured ✓</p>
        
        <div className="space-y-4 mb-8 text-left">
          <div className="p-4 bg-neutral-900/50 rounded-xl border border-neutral-700/50">
            <h2 className="text-sm font-semibold text-blue-400 uppercase tracking-wider mb-2">Build Configuration</h2>
            <p className="text-sm text-neutral-300">
              The project is now completely configured for GitHub Actions. A new file has been added at <code className="text-xs bg-neutral-800 px-1.5 py-0.5 rounded text-emerald-300">.github/workflows/android-build.yml</code>.
            </p>
          </div>
          
          <div className="p-4 bg-neutral-900/50 rounded-xl border border-neutral-700/50">
            <h2 className="text-sm font-semibold text-blue-400 uppercase tracking-wider mb-2">Automated Build Process</h2>
            <ul className="text-sm text-neutral-300 space-y-2">
              <li>1. Push or merge code to the <code className="text-xs bg-neutral-800 px-1.5 py-0.5 rounded">main</code> branch.</li>
              <li>2. GitHub Actions will automatically launch an Ubuntu runner.</li>
              <li>3. It sets up JDK 17 and compiles the Kotlin Android project via Gradle.</li>
              <li>4. The compiled <strong>APK file</strong> will be attached to the Action run as an artifact.</li>
            </ul>
          </div>
        </div>
        
        <div className="bg-emerald-900/20 border border-emerald-500/30 rounded-xl p-4 flex items-start space-x-3 text-left">
          <svg className="w-5 h-5 text-emerald-400 mt-0.5 shrink-0" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M5 13l4 4L19 7" />
          </svg>
          <div className="text-sm text-emerald-200">
            <span className="font-semibold text-emerald-100 block mb-1">Ready for Export</span>
            You can now export this project back to GitHub. The CI/CD pipeline will immediately trigger your first APK build!
          </div>
        </div>
      </div>
    </div>
  );
}
