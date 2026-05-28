@echo off
setlocal
cd /d "%~dp0"
java --module-path lib --add-modules javafx.controls,javafx.fxml --enable-native-access=javafx.graphics -Dprism.order=sw -Djava.library.path=lib -cp bin LoginUI
