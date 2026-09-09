/**
 * @license
 * SPDX-License-Identifier: Apache-2.0
 */

export default function App() {
  return (
    <div className="min-h-screen bg-neutral-900 text-neutral-100 flex flex-col items-center justify-center p-6 font-sans">
      <div className="max-w-xl w-full bg-neutral-800 rounded-2xl p-8 border border-neutral-700 shadow-2xl">
        <h1 className="text-2xl font-bold text-white mb-2">Vertical 3D Island Explorer</h1>
        <p className="text-neutral-400 mb-8">Android Kotlin Project Generated</p>
        
        <div className="space-y-4 mb-8">
          <div className="p-4 bg-neutral-900/50 rounded-xl border border-neutral-700/50">
            <h2 className="text-sm font-semibold text-emerald-400 uppercase tracking-wider mb-2">Project Structure</h2>
            <p className="text-sm text-neutral-300">
              The complete modular architecture has been scaffolded in Kotlin under the <code className="text-xs bg-neutral-800 px-1.5 py-0.5 rounded text-emerald-300">AndroidProject</code> directory.
            </p>
          </div>
          
          <div className="p-4 bg-neutral-900/50 rounded-xl border border-neutral-700/50">
            <h2 className="text-sm font-semibold text-blue-400 uppercase tracking-wider mb-2">Included Systems</h2>
            <ul className="text-sm text-neutral-300 grid grid-cols-2 gap-2">
              <li>• Player Controller</li>
              <li>• 3D Camera System</li>
              <li>• Touch Input</li>
              <li>• Day/Night Cycle</li>
              <li>• Terrain/LOD Managers</li>
              <li>• Inventory/Tent System</li>
            </ul>
          </div>
        </div>
        
        <div className="bg-blue-900/20 border border-blue-500/30 rounded-xl p-4 flex items-start space-x-3">
          <svg className="w-5 h-5 text-blue-400 mt-0.5 shrink-0" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
          </svg>
          <div className="text-sm text-blue-200">
            <span className="font-semibold text-blue-100 block mb-1">How to use this code:</span>
            Since this web preview cannot compile Android Kotlin code, you must export the project to use it. Click the <strong>Settings (gear) icon</strong> in the top right, then select <strong>Export to GitHub</strong> or <strong>Download ZIP</strong> to open this project in Android Studio.
          </div>
        </div>
      </div>
    </div>
  );
}
