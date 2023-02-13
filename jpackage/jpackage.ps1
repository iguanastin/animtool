
$jpackage = 'C:\Program Files\Java\jdk-17\bin\jpackage.exe'
$name = "AnimTool"
$description = "AnimTool"
$input = ".\input\"
$output = ".\output\"
$version = $args[0]
$icon = ".\animtool.ico"
$url = "https://github.com/iguanastin/menageriek"
$modulePath = "C:\Program Files\Java\javafx-jmods-21"
$modules = "javafx.controls,javafx.swing,javafx.fxml,java.logging"
$jvmoptions = "--module-path 'C:\Program Files\Java\javafx-sdk-21\lib' --add-modules javafx.controls,javafx.swing,javafx.fxml"
$jar = ".\AnimTool-${version}-jar-with-dependencies.jar"

rm .\input\AnimTool-*-jar-with-dependencies.jar
cp ..\target\AnimTool-${version}-jar-with-dependencies.jar .\input\

& $jpackage --module-path "$modulePath" --add-modules $modules --input "$input" --dest "$output" -n "$name" --app-version "$version" --description "$description" --icon "$icon" --about-url "$url" --java-options "$jvmoptions" --main-jar "$jar" --verbose --win-menu --win-shortcut-prompt --win-dir-chooser --win-menu-group "$name" --add-launcher animtool-console=consolelauncher.properties
# & $jpackage --type app-image --module-path "$modulePath" --add-modules $modules --input "$input" --dest "$output" -n "$name" --app-version "$version" --description "$description" --icon "$icon" --java-options "$jvmoptions" --main-jar "$jar" --verbose --add-launcher animtool-console=consolelauncher.properties
