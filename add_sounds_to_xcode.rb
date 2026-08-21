require 'xcodeproj'

project_path = 'iosApp/iosApp.xcodeproj'
project = Xcodeproj::Project.open(project_path)

target = project.targets.find { |t| t.name == 'iosApp' } || project.targets.first

# The physical path is iosApp/iosApp/Sounds, which corresponds to the logical path 'iosApp' -> 'Sounds'
iosAppGroup = project.main_group.children.find { |c| c.display_name == 'iosApp' || c.path == 'iosApp' }
unless iosAppGroup
  iosAppGroup = project.main_group.new_group('iosApp', 'iosApp')
end

group = iosAppGroup.children.find { |c| c.display_name == 'Sounds' || c.path == 'Sounds' }
unless group
  group = iosAppGroup.new_group('Sounds', 'Sounds')
end

sound_files = [
  'urgent_simple_tone_loop.wav',
  'bell_notification.wav',
  'clear_announce_tones.wav',
  'happy_bells_notification.wav',
  'time_for_medication_english.mp3',
  'time_for_medication_twi.mp3'
]

resources_build_phase = target.resources_build_phase

sound_files.each do |file_name|
  file_ref = group.files.find { |f| f.path == file_name }
  if file_ref.nil?
    file_ref = group.new_file(file_name)
  end
  
  unless resources_build_phase.files_references.include?(file_ref)
    resources_build_phase.add_file_reference(file_ref, true)
    puts "Added #{file_name} to Resources build phase"
  end
end

project.save
puts "Xcode project updated successfully!"
