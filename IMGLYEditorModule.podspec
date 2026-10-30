require 'json'

package = JSON.parse(File.read(File.join(__dir__, 'package.json')))

Pod::Spec.new do |s|
  s.name            = 'IMGLYEditorModule'
  s.version         = package['version']
  s.summary         = package['description']
  s.description     = package['description']
  s.homepage        = package['homepage']
  s.license         = package['license']
  s.platforms       = { ios: '16.0' }
  s.author          = package['author']
  s.source          = { path: './ios' }

  s.source_files = 'ios/**/*.{h,m,mm,swift}'
  # ios/Package.swift is the manifest for React Native's experimental Swift
  # Package Manager mode, not a source file.
  s.exclude_files = ['ios/StarterKits/**/*StarterKit.swift', 'ios/Package.swift']
  s.resources = 'ios/StarterKits/*.{scene,png}'

  s.dependency 'IMGLYUI', s.version.to_s

  install_modules_dependencies(s)
end
