# AnimTool v1.2.1

![Animated image of application window](https://user-images.githubusercontent.com/19540597/66709872-e69cf980-ed29-11e9-98cd-d1430a9edf00.gif) ![File structure example](https://user-images.githubusercontent.com/19540597/66709891-6f1b9a00-ed2a-11e9-8108-145c692796b1.png)

This tool is intended to be a way to continue using your art program of choice (that doesn't have animation tools) and still be able to create animations with relative ease. By saving individual keyframes, you can have a constant preview of the animation as well as change delays on the fly.

Open a folder with AnimTool and it will find all the image (`.png` and `.jpg`) files and show you an animated preview of them. As you add/edit/delete those files, the preview will automatically update the animation.

NOTE: This software is *NOT* a standalone animation suite; it's a way to use your favorite drawing software and still make animations.

## Features
- Real time preview as files are modified
- Export GIFs without any external software
- Use custom per-frame delays or an overall framerate
- Pinning window to top
- Seeking through frames individually

## [Download](https://github.com/iguanastin/animtool/releases)
Only Windows is supported.

The latest release can be found on the [releases](https://github.com/iguanastin/animtool/releases) page. Download the `animtool-#.#-exe`

## Or build it yourself
1. Clone this repo
2. Run: `mvn package`
