# ext-mp-stats-tracker

## What is this?

This is an extension for the [musicplayer](https://github.com/scorbo2/musicplayer) application which allows
tracking of how many times each track is played, so you can view listening statistics in the stats dialog.

### How do I use it?

**NEW** starting in MusicPlayer 3.0, you no longer have to build the jar yourself! You can use the
new and improved extension manager feature that lets you download the jar through the application
and install it automatically!

TODO insert a screenshot here.

### Alternatively, you can build the extension jar and install it yourself:

Clone the repo and build the jar with maven:

```shell
git clone https://github.com/scorbo2/ext-mp-stats-tracker.git
cd ext-mp-stats-tracker
mvn package
```

You can then copy the extension jar file to wherever you keep your musicplayer extensions:

```shell
cp target/ext-mp-stats-tracker-3.0.0.jar ~/.MusicPlayer/extensions
```

Then restart musicplayer and you should find an option to view the statistics dialog:

### Screenshots

TODO add screenshots and additional usage information.

### Requirements

MusicPlayer 3.0 or higher.

### License

Musicplayer and this extension are made available under the MIT license: https://opensource.org/license/mit
