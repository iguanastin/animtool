/*
 MIT License

 Copyright (c) 2019. Austin Thompson

 Permission is hereby granted, free of charge, to any person obtaining a copy
 of this software and associated documentation files (the "Software"), to deal
 in the Software without restriction, including without limitation the rights
 to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 copies of the Software, and to permit persons to whom the Software is
 furnished to do so, subject to the following conditions:

 The above copyright notice and this permission notice shall be included in all
 copies or substantial portions of the Software.

 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 SOFTWARE.
 */

package settings;

import java.util.Collections;

public class MenagerieSettings extends Settings {

    public GroupSetting importGroup, autoImportGroup, duplicatesGroup, videoGroup, dbGroup, explorerGroup, slideshowGroup, findOnlineGroup;
    public FolderSetting defaultFolder, autoImportFolder, vlcFolder, lastImportFolder;
    public StringSetting userFileTypes, dbUrl, dbUser, dbPass, tagWithOnImport, importItemsIntoGroupName, importOrder;
    public BooleanSetting urlFilename, tagImages, tagVideos, tagTagme, autoImportMove, repeatVideo, muteVideo, dbBackup, helpOnStart, windowMaximized, expandItemInfo, recursivelyImport, tagParentFolderOnImport, doTagWithOnImport, doImportItemsIntoGroup, renameToHashOnImport, duplicatesIncludeGroups, slideshowPreload, duplicatePreload, explorerGroupAscending;
    public DoubleSetting duplicatesConfidence, slideshowInterval;
    public IntSetting gridWidth, licensesAgreed, windowX, windowY, windowWidth, windowHeight, onlineLoadAhead;


    public MenagerieSettings() {
        importGroup = new GroupSetting("import-group").label("Importing");
        defaultFolder = new FolderSetting("default-folder").label("Default Folder").tip("Folder to import files into, where applicable");
        userFileTypes = new StringSetting("user-filetypes").label("Also import filetypes").tip("File extensions, separated by spaces. E.g. \".txt .pdf .rar\"");
        urlFilename = new BooleanSetting("url-filename", true).label("Use filename from URL").tip("When importing from web, skip opening the save file dialog");
        tagImages = new BooleanSetting("tag-images", false).label("Tag images with 'image'");
        tagVideos = new BooleanSetting("tag-videos", false).label("Tag videos with 'video'");
        tagTagme = new BooleanSetting("tag-tagme", true).label("Tag everything with 'tagme'");
        autoImportGroup = new GroupSetting("auto-import-group").label("Automatically import").toggleable().disable();
        autoImportFolder = new FolderSetting("auto-import-folder").label("Auto import from folder").tip("Folder to automatically import files from");
        autoImportMove = new BooleanSetting("auto-import-move", true).label("Move auto-imported files to default folder");
        Collections.addAll(autoImportGroup.getChildren(), autoImportFolder, autoImportMove);
        Collections.addAll(importGroup.getChildren(), defaultFolder, userFileTypes, urlFilename, tagImages, tagVideos, tagTagme, autoImportGroup);
        getSettings().add(importGroup);

        explorerGroup = new GroupSetting("explorer-group").label("Explorer");
        gridWidth = new IntSetting("grid-width", 3).range(1, 8).label("Grid width");
        explorerGroupAscending = new BooleanSetting("group-ascending", true).label("Open groups in ascending order");
        Collections.addAll(explorerGroup.getChildren(), explorerGroupAscending, gridWidth);
        getSettings().add(explorerGroup);

        duplicatesGroup = new GroupSetting("duplicate-group").label("Duplicate Finding");
        duplicatesConfidence = new DoubleSetting("duplicate-confidence", 0.95).range(0.9, 1.0).label("Duplicate Confidence").tip("Value between 0.90 and 1.00");
        duplicatesIncludeGroups = new BooleanSetting("duplicates-groups", true).label("Include items in groups").tip("Include group items in duplicate comparisons");
        duplicatePreload = new BooleanSetting("duplicate-preload", true).label("Preload next/previous duplicates");
        Collections.addAll(duplicatesGroup.getChildren(), duplicatesConfidence, duplicatesIncludeGroups, duplicatePreload);
        getSettings().add(duplicatesGroup);

        findOnlineGroup = new GroupSetting("").label("Find Online");
        onlineLoadAhead = new IntSetting("online-load-ahead", 1).label("Load Next").min(0);
        Collections.addAll(findOnlineGroup.getChildren(), onlineLoadAhead);
        getSettings().add(findOnlineGroup);

        slideshowGroup = new GroupSetting("slideshow-group").label("Slideshow");
        slideshowInterval = new DoubleSetting("slideshow-interval", 10).label("Slideshow interval (seconds)").min(0.1);
        slideshowPreload = new BooleanSetting("slideshow-preload", true).label("Preload previous/next images");
        Collections.addAll(slideshowGroup.getChildren(), slideshowInterval, slideshowPreload);
        getSettings().add(slideshowGroup);

        videoGroup = new GroupSetting("video-group").label("Video Playback");
        repeatVideo = new BooleanSetting("repeat-video", true).label("Repeat video");
        muteVideo = new BooleanSetting("mute-video", false).label("Mute video");
        vlcFolder = new FolderSetting("vlc-folder").label("VLC Folder").tip("Only needs to be specified if VLC cannot be found in the normal places");
        Collections.addAll(videoGroup.getChildren(), repeatVideo, muteVideo, vlcFolder);
        getSettings().add(videoGroup);

        dbGroup = new GroupSetting("db-group").label("Database");
        dbUrl = new StringSetting("db-url", "~/menagerie").label("Database URL").tip("\"~/menagerie\" by default");
        dbUser = new StringSetting("db-user", "sa").label("Database User").tip("\"sa\" by default");
        dbPass = new StringSetting("db-pass", "").label("Database Pass").tip("Empty by default");
        dbBackup = new BooleanSetting("db-backup", true).label("Backup database on launch");
        Collections.addAll(dbGroup.getChildren(), dbUrl, dbUser, dbPass, dbBackup);
        getSettings().add(dbGroup);

        helpOnStart = new BooleanSetting("help-on-start", true).hide();
        windowMaximized = new BooleanSetting("window-maximized", false).hide();
        expandItemInfo = new BooleanSetting("expand-item-info", false).hide();
        licensesAgreed = new IntSetting("licenses-agreed", -1).hide();
        windowX = new IntSetting("window-x").hide();
        windowY = new IntSetting("window-y").hide();
        windowWidth = new IntSetting("window-width", 800).min(0).hide();
        windowHeight = new IntSetting("window-height", 600).min(0).hide();
        Collections.addAll(getSettings(), helpOnStart, windowMaximized, expandItemInfo, licensesAgreed, windowX, windowY, windowWidth, windowHeight);

        importOrder = new StringSetting("import-order").hide();
        lastImportFolder = new FolderSetting("last-import-folder").hide();
        recursivelyImport = new BooleanSetting("recurse-import", true).hide();
        tagParentFolderOnImport = new BooleanSetting("import-tag-parent-folder", false).hide();
        renameToHashOnImport = new BooleanSetting("import-rename-to-hash", false).hide();
        tagWithOnImport = new StringSetting("import-tag-with").hide();
        doTagWithOnImport = new BooleanSetting("import-do-tag-with", false).hide();
        importItemsIntoGroupName = new StringSetting("import-items-into-group").hide();
        doImportItemsIntoGroup = new BooleanSetting("do-import-items-into-group", false).hide();
        Collections.addAll(getSettings(), importOrder, lastImportFolder, recursivelyImport, tagParentFolderOnImport, renameToHashOnImport, tagWithOnImport, doTagWithOnImport, importItemsIntoGroupName, doImportItemsIntoGroup);
    }

    public void loadFrom(OldSettings old) {
        defaultFolder.setValue(old.getString(OldSettings.Key.DEFAULT_FOLDER));
        userFileTypes.setValue(old.getString(OldSettings.Key.USER_FILETYPES));
        urlFilename.setValue(old.getBoolean(OldSettings.Key.USE_FILENAME_FROM_URL));
        tagImages.setValue(old.getBoolean(OldSettings.Key.TAG_IMAGE));
        tagVideos.setValue(old.getBoolean(OldSettings.Key.TAG_VIDEO));
        tagTagme.setValue(old.getBoolean(OldSettings.Key.TAG_TAGME));
        autoImportGroup.setEnabled(old.getBoolean(OldSettings.Key.DO_AUTO_IMPORT));
        autoImportFolder.setValue(old.getString(OldSettings.Key.AUTO_IMPORT_FOLDER));
        autoImportMove.setValue(old.getBoolean(OldSettings.Key.AUTO_IMPORT_MOVE_TO_DEFAULT));

        duplicatesConfidence.setValue(old.getDouble(OldSettings.Key.CONFIDENCE));

        repeatVideo.setValue(old.getBoolean(OldSettings.Key.REPEAT_VIDEO));
        muteVideo.setValue(old.getBoolean(OldSettings.Key.MUTE_VIDEO));
        vlcFolder.setValue(old.getString(OldSettings.Key.VLCJ_PATH));

        dbUrl.setValue(old.getString(OldSettings.Key.DATABASE_URL));
        dbUser.setValue(old.getString(OldSettings.Key.DATABASE_USER));
        dbPass.setValue(old.getString(OldSettings.Key.DATABASE_PASSWORD));
        dbBackup.setValue(old.getBoolean(OldSettings.Key.BACKUP_DATABASE));

        gridWidth.setValue(old.getInt(OldSettings.Key.GRID_WIDTH));

        helpOnStart.setValue(old.getBoolean(OldSettings.Key.SHOW_HELP_ON_START));
        windowMaximized.setValue(old.getBoolean(OldSettings.Key.WINDOW_MAXIMIZED));
        expandItemInfo.setValue(old.getBoolean(OldSettings.Key.EXPAND_ITEM_INFO));
        licensesAgreed.setValue(old.getInt(OldSettings.Key.LICENSES_AGREED));
        windowX.setValue(old.getInt(OldSettings.Key.WINDOW_X));
        windowY.setValue(old.getInt(OldSettings.Key.WINDOW_Y));
        windowWidth.setValue(old.getInt(OldSettings.Key.WINDOW_WIDTH));
        windowHeight.setValue(old.getInt(OldSettings.Key.WINDOW_HEIGHT));
    }

}
