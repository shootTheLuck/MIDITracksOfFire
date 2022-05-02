package page;

import actions.Actions;
import track.TrackController;

class SelectTrackCommand extends Actions.Item {

    private Page page;
    TrackController newSelected;
    TrackController oldSelected;

    public SelectTrackCommand(Page page, TrackController track) {
        this.page = page;
        TrackController oldSelected = null;
        if (page.selectedTrack != null && page.selectedTrack != track) {
            oldSelected = page.selectedTrack;
        }
        this.newSelected = track;
        this.oldSelected = oldSelected;
    }

    public void execute() {
        if (oldSelected != null) {
            oldSelected.setAsNotSelectedTrack();
        }
        page.selectedTrack = newSelected;
        newSelected.setAsSelectedTrack();
    }

    public void redo() {
        execute();
    }

    public void undo() {
        if (oldSelected != null) {
            oldSelected.setAsSelectedTrack();
            page.selectedTrack = oldSelected;
        }
        newSelected.setAsNotSelectedTrack();
    }

}