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

  # In the monorepo, copy starter kit sources from the shared location.
  # Walk up from __dir__ to find the monorepo root (.ubq-version marker),
  # matching the Android approach in starter-kits-local.gradle.
  ubq_root = nil
  current_dir = File.expand_path(__dir__)
  while current_dir != '/'
    if File.exist?(File.join(current_dir, '.ubq-version'))
      ubq_root = current_dir
      break
    end
    current_dir = File.dirname(current_dir)
  end

  if ubq_root
    copy_script = File.join(ubq_root, 'apps', 'cesdk_swift_examples',
                            'starter-kits', 'copy_starter_kits_source_files.sh')
    if File.executable?(copy_script)
      system('bash', copy_script, File.join(__dir__, 'ios', 'StarterKits'))
    end
  end

  s.source_files = 'ios/**/*.{h,m,mm,swift}'
  s.exclude_files = 'ios/StarterKits/**/*StarterKit.swift'
  s.resources = 'ios/StarterKits/*.{scene,png}'

  s.dependency 'IMGLYUI', s.version.to_s

  install_modules_dependencies(s)
end
