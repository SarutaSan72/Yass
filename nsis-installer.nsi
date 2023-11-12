Name "Yass"

OutFile ".\target\yass-installer-2023.11.exe"

Unicode true
SetCompressor lzma
XPStyle on
InstallColors /windows
Icon .\src\yass\resources\icons\yass-multi-icon.ico
UninstallIcon .\src\yass\resources\icons\yass-multi-icon.ico
InstallDir "$PROGRAMFILES\Yass Along"
RequestExecutionLevel admin

LoadLanguageFile "${NSISDIR}\Contrib\Language files\English.nlf"
LoadLanguageFile "${NSISDIR}\Contrib\Language files\German.nlf"
LoadLanguageFile "${NSISDIR}\Contrib\Language files\Hungarian.nlf"
LoadLanguageFile "${NSISDIR}\Contrib\Language files\Polish.nlf"
LoadLanguageFile "${NSISDIR}\Contrib\Language files\Spanish.nlf"
LicenseLangString myLicenseData ${LANG_ENGLISH} .\License.txt
LicenseLangString myLicenseData ${LANG_GERMAN} .\License.txt
LicenseLangString myLicenseData ${LANG_HUNGARIAN} .\License.txt
LicenseLangString myLicenseData ${LANG_POLISH} .\License.txt
LicenseLangString myLicenseData ${LANG_SPANISH} .\License.txt
LicenseData $(myLicenseData)

LangString Msg_Prev ${LANG_ENGLISH} "Yass Along is already installed. $\n$\nChoose `OK` to remove the previous version or `Cancel` to cancel this upgrade."
LangString Msg_Prev ${LANG_GERMAN} "Yass Along ist bereits installiert. $\n$\nMit `OK` wird die vorherige Version entfernt, mit `Abbrechen` wird die Aktualisierung abgebrochen."
LangString Msg_Prev ${LANG_HUNGARIAN} "Yass Along is already installed. $\n$\nChoose `OK` to remove the previous version or `Cancel` to cancel this upgrade."
LangString Msg_Prev ${LANG_POLISH} "Yass Along is already installed. $\n$\nChoose `OK` to remove the previous version or `Cancel` to cancel this upgrade."
LangString Msg_Prev ${LANG_SPANISH} "Yass Along ya está instalado. $\n$\Elija `OK` para desinstalar la versión anterior o `Cancelar` para cancelar la actualización."

LangString Sec_ContextMenuText ${LANG_ENGLISH} "Add context menu item to text files"
LangString Sec_ContextMenuText ${LANG_GERMAN} "Kontextmenüeintrag für Textdateien"
LangString Sec_ContextMenuText ${LANG_HUNGARIAN} "Add context menu item to text files"
LangString Sec_ContextMenuText ${LANG_POLISH} "Add context menu item to text files"
LangString Sec_ContextMenuText ${LANG_SPANISH} "Añadir al menú contextual de archivos txt"

LangString Sec_ContextMenuKar ${LANG_ENGLISH} "Add context menu item to MIDI/Kar files"
LangString Sec_ContextMenuKar ${LANG_GERMAN} "Kontextmenüeintrag für MIDI/Kar-Dateien"
LangString Sec_ContextMenuKar ${LANG_HUNGARIAN} "Add context menu item to MIDI/Kar files"
LangString Sec_ContextMenuKar ${LANG_POLISH} "Add context menu item to MIDI/Kar files"
LangString Sec_ContextMenuKar ${LANG_SPANISH} "Añadir al menú contextual de arhivos MIDI/Kar"

LangString Sec_ContextMenuDir ${LANG_ENGLISH} "Add context menu item to directories"
LangString Sec_ContextMenuDir ${LANG_GERMAN} "Kontextmenüeintrag für Verzeichnisse"
LangString Sec_ContextMenuDir ${LANG_HUNGARIAN} "Add context menu item to directories"
LangString Sec_ContextMenuDir ${LANG_POLISH} "Add context menu item to directories"
LangString Sec_ContextMenuDir ${LANG_SPANISH} "Añadir al menú contextual de directorios"

LangString ContextMenu_Edit ${LANG_ENGLISH} "Edit with Yass"
LangString ContextMenu_Edit ${LANG_GERMAN} "Bearbeiten mit Yass"
LangString ContextMenu_Edit ${LANG_HUNGARIAN} "Edit with Yass"
LangString ContextMenu_Edit ${LANG_POLISH} "Edit with Yass"
LangString ContextMenu_Edit ${LANG_SPANISH} "Editar con Yass"

LangString ContextMenu_Convert ${LANG_ENGLISH} "Convert with Yass"
LangString ContextMenu_Convert ${LANG_GERMAN} "Konvertieren mit Yass"
LangString ContextMenu_Convert ${LANG_HUNGARIAN} "Convert with Yass"
LangString ContextMenu_Convert ${LANG_POLISH} "Convert with Yass"
LangString ContextMenu_Convert ${LANG_SPANISH} "Convertir con Yass"

LangString Sec_Desktop ${LANG_ENGLISH} "Create shortcut on desktop"
LangString Sec_Desktop ${LANG_GERMAN} "Verknüpfung auf dem Desktop"
LangString Sec_Desktop ${LANG_HUNGARIAN} "Create shortcut on desktop"
LangString Sec_Desktop ${LANG_POLISH} "Create shortcut on desktop"
LangString Sec_Desktop ${LANG_SPANISH} "Crear acceso directo en el escritorio"

LangString Msg_Uninstall ${LANG_ENGLISH} "This will uninstall Yass from your system."
LangString Msg_Uninstall ${LANG_GERMAN} "Yass wird nun von Ihrem System deinstalliert."
LangString Msg_Uninstall ${LANG_HUNGARIAN} "This will uninstall Yass from your system."
LangString Msg_Uninstall ${LANG_POLISH} "This will uninstall Yass from your system."
LangString Msg_Uninstall ${LANG_SPANISH} "Esto desinstalará Yass de tu sistema."

LangString Msg_RemoveSettings ${LANG_ENGLISH} "Remove user data and settings? This will delete the .yass folder in your home folder. $\n$\nChoose `YES` to remove it, or 'NO' to keep it for future use."
LangString Msg_RemoveSettings ${LANG_GERMAN} "Persönliche Daten und Einstellungen entfernen? Dies löscht das Verzeichnis .yass in Ihrem Benutzerverzeichnis. $\n$\nMit `Ja` löschen Sie es, mit 'Nein' können Sie es später weiterverwenden."
LangString Msg_RemoveSettings ${LANG_HUNGARIAN} "Remove user data and settings? This will delete the .yass folder in your home folder. $\n$\nChoose `YES` to remove it, or 'NO' to keep it for future use."
LangString Msg_RemoveSettings ${LANG_POLISH} "Remove user data and settings? This will delete the .yass folder in your home folder. $\n$\nChoose `YES` to remove it, or 'NO' to keep it for future use."
LangString Msg_RemoveSettings ${LANG_SPANISH} "¿Deseas eliminar los datos de usuario y configuración? $\n$\nElige `Sí` para eliminarlo, o 'No' para mantenerlos en un futuro."

LangString Msg_RemoveSettingsReally ${LANG_ENGLISH} "Are you sure? This will delete your personal settings and spelling."
LangString Msg_RemoveSettingsReally ${LANG_GERMAN} "Achtung, dies entfernt alle persönliche Einstellungen inklusive der Rechtschreibkorrektur."
LangString Msg_RemoveSettingsReally ${LANG_HUNGARIAN} "Are you sure? This will delete your personal settings and spelling."
LangString Msg_RemoveSettingsReally ${LANG_POLISH} "Are you sure? This will delete your personal settings and spelling."
LangString Msg_RemoveSettingsReally ${LANG_SPANISH} "¿Estás seguro? Se borrará tu configuración personal que tenías guardada."

##################
# uninstall previous version
Function .onInit
    Push ""
	Push ${LANG_ENGLISH}
	Push English
	Push ${LANG_GERMAN}
	Push German
	Push ${LANG_HUNGARIAN}
	Push Hungarian
	Push ${LANG_POLISH}
	Push Polish
	Push ${LANG_SPANISH}
	Push Spanish
	Push A ; auto count languages; the first empty push (Push "") must remain
	LangDLL::LangDialog "Installer Language" "Please select the language of the installer"

	Pop $LANGUAGE
	StrCmp $LANGUAGE "cancel" 0 +2
		Abort

  ReadRegStr $R0 HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\Yass Along" "UninstallString"
  StrCmp $R0 "" done

  MessageBox MB_OKCANCEL|MB_ICONEXCLAMATION \
    $(Msg_Prev) \
    IDOK uninst
  Abort

uninst:
  # workaround: cleanup previous start menu entries (all users)
  SetShellVarContext all
  RMDir /r "$SMPROGRAMS\Yass Along 1.9.0"
  RMDir /r "$SMPROGRAMS\Yass Along 1.8.1"
  RMDir /r "$SMPROGRAMS\Yass Along 1.8.0"
  RMDir /r "$SMPROGRAMS\Yass Along 1.7.1"

  ClearErrors
  ExecWait $R0
done:
FunctionEnd
##################

Page license
Page components
Page directory

Section $(Sec_ContextMenuText)
  SetOutPath "$INSTDIR"
  WriteRegStr HKCR "Directory\shell\yass" "" $(ContextMenu_Edit)
  WriteRegStr HKCR "Directory\shell\yass\command" "" '"$INSTDIR\yass.exe" "%1"'
  WriteRegStr HKCR "SystemFileAssociations\.txt\shell\yass" "" $(ContextMenu_Edit)
  WriteRegStr HKCR "SystemFileAssociations\.txt\shell\yass\command" "" '"$INSTDIR\yass.exe" "%1"'
SectionEnd

Section $(Sec_ContextMenuKar)
  SetOutPath "$INSTDIR"

  ReadRegStr $R0 HKCR ".mid" ""
  StrCmp $R0 "" NoDefaultMid DefaultMid
NoDefaultMid:
  WriteRegStr HKCR ".mid\shell\yass" "" $(ContextMenu_Convert)
  WriteRegStr HKCR ".mid\shell\yass\command" "" '"$INSTDIR\yass.exe" "%1"'
  Goto EndMid
DefaultMid:
  WriteRegStr HKCR "$R0\shell\yass" "" $(ContextMenu_Convert)
  WriteRegStr HKCR "$R0\shell\yass\command" "" '"$INSTDIR\yass.exe" "%1"'
EndMid:

  ReadRegStr $R0 HKCR ".midi" ""
  StrCmp $R0 "" NoDefaultMidi DefaultMidi
NoDefaultMidi:
  WriteRegStr HKCR ".midi\shell\yass" "" $(ContextMenu_Convert)
  WriteRegStr HKCR ".midi\shell\yass\command" "" '"$INSTDIR\yass.exe" "%1"'
  Goto EndMidi
DefaultMidi:
  WriteRegStr HKCR "$R0\shell\yass" "" $(ContextMenu_Convert)
  WriteRegStr HKCR "$R0\shell\yass\command" "" '"$INSTDIR\yass.exe" "%1"'
EndMidi:

  ReadRegStr $R0 HKCR ".kar" ""
  StrCmp $R0 "" NoDefaultKar DefaultKar
NoDefaultKar:
  WriteRegStr HKCR ".kar\shell\yass" "" $(ContextMenu_Convert)
  WriteRegStr HKCR ".kar\shell\yass\command" "" '"$INSTDIR\yass.exe" "%1"'
  Goto EndKar
DefaultKar:
  WriteRegStr HKCR "$R0\shell\yass" "" $(ContextMenu_Convert)
  WriteRegStr HKCR "$R0\shell\yass\command" "" '"$INSTDIR\yass.exe" "%1"'
EndKar:

SectionEnd

Section $(Sec_ContextMenuDir)
  SetOutPath "$INSTDIR"
  WriteRegStr HKCR "Directory\shell\yass" "" $(ContextMenu_Edit)
  WriteRegStr HKCR "Directory\shell\yass\command" "" '"$INSTDIR\yass.exe" "%1"'
SectionEnd

Section $(Sec_Desktop)
  SetOutPath $INSTDIR
  CreateShortCut "$DESKTOP\Yass Editor.lnk" "$OUTDIR\yass.exe" "-lib" 
# "$INSTDIR\yass-edit.ico"
#  CreateShortCut "$DESKTOP\Yass Player.lnk" "$OUTDIR\yass.exe" "-play"
SectionEnd

Page instfiles
Section
  SetShellVarContext all
  SetOutPath $INSTDIR
  File ".\target\yass.exe"
#  File ".\lib\fobs4jmf.dll"
#  File ".\lib\jinput-raw.dll"
#  File ".\lib\jinput-dx8.dll"
#  File ".\src\icons\yass-edit.ico"
  File ".\lib\Yass Along 1.0.1.pref"
  WriteRegStr HKLM "SOFTWARE\Yass Along" "installdir" "$INSTDIR"

  WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\Yass Along" "DisplayName" "Yass Along"
  WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\Yass Along" "UninstallString" "$INSTDIR\uninstall.exe"

  WriteUninstaller $INSTDIR\uninstall.exe

  StrCpy $R1 $R0 1
  StrCmp $R1 ">" skip
	CreateDirectory "$SMPROGRAMS\Yass Along"
#	CreateShortCut "$SMPROGRAMS\Yass Along\Yass Player.lnk" "$INSTDIR\yass.exe" "-play"
	CreateShortCut "$SMPROGRAMS\Yass Along\Yass Editor.lnk" "$INSTDIR\yass.exe" "-lib"
# "$INSTDIR\yass-edit.ico"
	CreateShortCut "$SMPROGRAMS\Yass Along\Yass Converter.lnk" "$INSTDIR\yass.exe" "-convert"
	CreateShortCut "$SMPROGRAMS\Yass Along\Uninstall Yass Along.lnk" "$INSTDIR\uninstall.exe"
  skip:
SectionEnd

UninstallText $(Msg_Uninstall)

Section "Uninstall"
  SetShellVarContext all

  DeleteRegKey HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\Yass Along"

  DeleteRegKey HKCR "SystemFileAssociations\.txt\shell\yass\command"
  DeleteRegKey HKCR "SystemFileAssociations\.txt\shell\yass"

  DeleteRegKey HKCR "Directory\shell\yass\command"
  DeleteRegKey HKCR "Directory\shell\yass"
  DeleteRegKey HKCR "txtfile\shell\yass\command"
  DeleteRegKey HKCR "txtfile\shell\yass"

  ReadRegStr $R0 HKCR ".mid" ""
  DeleteRegKey HKCR "$R0\shell\yass\command"
  DeleteRegKey HKCR "$R0\shell\yass"
  DeleteRegKey HKCR ".mid\shell\yass\command"
  DeleteRegKey HKCR ".mid\shell\yass"

  ReadRegStr $R0 HKCR ".midi" ""
  DeleteRegKey HKCR "$R0\shell\yass\command"
  DeleteRegKey HKCR "$R0\shell\yass"
  DeleteRegKey HKCR ".midi\shell\yass\command"
  DeleteRegKey HKCR ".midi\shell\yass"

  ReadRegStr $R0 HKCR ".kar" ""
  DeleteRegKey HKCR "$R0\shell\yass\command"
  DeleteRegKey HKCR "$R0\shell\yass"
  DeleteRegKey HKCR ".kar\shell\yass\command"
  DeleteRegKey HKCR ".kar\shell\yass"


  Delete "$DESKTOP\Yass Editor.lnk"
#  delete "$DESKTOP\Yass Player.lnk"
#  delete "$SMPROGRAMS\Yass Along\Yass Player.lnk"
  Delete "$SMPROGRAMS\Yass Along\Yass Editor.lnk"
  Delete "$SMPROGRAMS\Yass Along\Yass Converter.lnk"
  Delete "$SMPROGRAMS\Yass Along\Uninstall Yass.lnk"
  RMDir /r "$SMPROGRAMS\Yass Along"
  Delete "$INSTDIR\Yass Along 1.0.1.pref"
  Delete "$INSTDIR\fobs4jmf.dll"
  Delete "$INSTDIR\yass.exe"
  Delete "$INSTDIR\uninstall.exe"
  RMDir  "$INSTDIR"

  MessageBox MB_YESNO|MB_ICONEXCLAMATION \
    $(Msg_RemoveSettings) \
    IDNO bye IDYES yes
yes:
  MessageBox MB_OKCANCEL|MB_ICONEXCLAMATION \
    $(Msg_RemoveSettingsReally) \
    IDOK yes2 IDCANCEL bye
yes2:
  RMDir /r "$PROFILE\.yass"
bye:

sectionEnd
