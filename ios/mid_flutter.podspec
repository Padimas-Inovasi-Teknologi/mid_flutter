#
# To learn more about a Podspec see http://guides.cocoapods.org/syntax/podspec.html
#
Pod::Spec.new do |s|
  s.name             = 'mid_flutter'
  s.version          = '0.0.1'
  s.summary          = 'When Midtrans and Flutter get together'
  s.description      = <<-DESC
When Midtrans and Flutter get together
                       DESC
  s.homepage         = 'http://example.com'
  s.license          = { :file => '../LICENSE' }
  s.author           = { 'Your Company' => 'email@example.com' }
  s.source           = { :path => '.' }
  s.source_files = 'Classes/**/*'
  s.public_header_files = 'Classes/**/*.h'
  s.dependency 'Flutter'
  s.dependency 'MidtransCoreKit', '~> 1.16.3'
  s.static_framework = true

  s.ios.deployment_target = '10.0'
end

