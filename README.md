# TracksOfFire

Compose, edit, and play MIDI files with a tablature based ui.


### This is not the greatest midi editor in the world. This is just a tribute.

TracksOfFire is totally inspired by Easy Beat, a musical authoring program designed by Günther Blaschek and distributed by Macility/Ergonis Software. Easy Beat's UI was more possibly more intuitive and awesome than any UI for any application for any purpose. Ever.

One of Easy Beat's greatest features was the way in which it handled guitar/bass tablature: sort-of a cross between normal tablature and piano-roll representations. Just start a note on a string with your mouse, drag it to the length you wanted and you had a note Hit a number on the keyboard to assign a it fret and maybe you were started with a track that was on fire!

Sadly, Easy Beat was discontinued some time ago. Emulators don't seem to do it justice, and the old macs that can run it are...old. So here we are: trying to recreate some of Easy Beat's functionality to make some smokin' MIDI tracks!

## To use:
Download or clone this repo. Run the `start.sh` script or open a terminal and execute
`java -jar build/TracksOfFire.jar`.
TracksOfFire is written in Java so you'll need that.

To rebuild and run with your awesome and needed improvements run the `startDev.sh` script. Rebuilding with this script is done with ant so you'll want that.

## Contribute:
**I would love and appreciate any advice, contributions, feedback, issues etc that anyone might have!**

This is a work in progess. So far, it's just me and I'm a total amateur who is trying to improve. Hit the dang issues button!

GOALS/TODO's:
- architecture improvement. This code is pretty atrocious. **Breaking News! Architectural Rework son to be in dev branch!**
- the drum GUI
- documentation
- tests!
- undo/redo (started)
- duplicate measures via edit menu

## Credits:
- Easy Beat(c) Macility/Ergonis Software.
- RepoMan.mid is a midi rendition of Repo Man by Iggy Pop from the "Repo-Man" soundtrack. Midified by me and included here for demonstration purposes only
- Artwork and soundfonts are the work of others and licensed separately by their respective authors
- Tribute is a song by Tenacious D
