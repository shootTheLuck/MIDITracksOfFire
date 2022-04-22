package instruments;


import java.util.ArrayList;
import java.util.List;


public class Instrument {

    public String name;
    public int number;

    public Instrument(String s, int n) {
        name = s;
        number = n;
    }

    @Override
    public String toString() {
        return name;
    }

    public static int NUM_PER_CATEGORY = 8;
    public static List<Instrument> list = new ArrayList<>();
    public static List<Instrument> drumList = new ArrayList<>();
    public static String[] categories = {"Piano", "Chromatic", "Organ", "Guitar", "Bass",
            "Strings", "Ensemble", "Brass", "Reed", "Pipe", "Synth Lead", "Synth Pad",
            "Synth Effects", "Ethnic", "Percussive", "Sound Effects", "Drum Sets"};

    static {

        // Piano
            list.add(new Instrument("Acoustic Grand Piano", 0));
            list.add(new Instrument("Bright Acoustic Piano", 1));
            list.add(new Instrument("Electric Grand Piano", 2));
            list.add(new Instrument("Honky-tonk Piano", 3));
            list.add(new Instrument("Rhodes Piano", 4));
            list.add(new Instrument("Chorused Piano", 5));
            list.add(new Instrument("Harpsichord", 6));
            list.add(new Instrument("Clavinet", 7));

        // Chromatic
            list.add(new Instrument("Celesta", 8));
            list.add(new Instrument("Glockenspiel", 9));
            list.add(new Instrument("Music Box", 10));
            list.add(new Instrument("Vibraphone", 11));
            list.add(new Instrument("Marimba", 12));
            list.add(new Instrument("Xylophone", 13));
            list.add(new Instrument("Tubular Bells", 14));
            list.add(new Instrument("Dulcimer", 15));

        // Organ
            list.add(new Instrument("Drawbar Organ", 16));
            list.add(new Instrument("Percussive Organ", 17));
            list.add(new Instrument("Rock Organ", 18));
            list.add(new Instrument("Church Organ", 19));
            list.add(new Instrument("Reed Organ", 20));
            list.add(new Instrument("Accordian", 21));
            list.add(new Instrument("Harmonica", 22));
            list.add(new Instrument("Tango Accordian", 23));

        // Guitar
            list.add(new Instrument("Acoustic Nylon Guitar", 24));
            list.add(new Instrument("Acoustic Steel Guitar", 25));
            list.add(new Instrument("Electric Jazz Guitar", 26));
            list.add(new Instrument("Electric Clean Guitar", 27));
            list.add(new Instrument("Electric Muted Guitar", 28));
            list.add(new Instrument("Overdriven Guitar", 29));
            list.add(new Instrument("Distortion Guitar", 30));
            list.add(new Instrument("Guitar Harmonics", 31));

        // Bass
            list.add(new Instrument("Acoustic Bass", 32));
            list.add(new Instrument("Fingered Electric Bass", 33));
            list.add(new Instrument("Plucked Electric Bass", 34));
            list.add(new Instrument("Fretless Bass", 35));
            list.add(new Instrument("Slap Bass 1", 36));
            list.add(new Instrument("Slap Bass 2", 37));
            list.add(new Instrument("Synth Bass 1", 38));
            list.add(new Instrument("Synth Bass 2", 39));

        // Strings
            list.add(new Instrument("Violin", 40));
            list.add(new Instrument("Viola", 41));
            list.add(new Instrument("Cello", 42));
            list.add(new Instrument("Contrabass", 43));
            list.add(new Instrument("Tremolo Strings", 44));
            list.add(new Instrument("Pizzicato Strings", 45));
            list.add(new Instrument("Orchestral Harp", 46));
            list.add(new Instrument("Timpani", 47));

        // Ensemble
            list.add(new Instrument("String Ensemble 1", 48));
            list.add(new Instrument("String Ensemble 2", 49));
            list.add(new Instrument("Synth Strings 1", 50));
            list.add(new Instrument("Synth Strings 2", 51));
            list.add(new Instrument("Choir Aahs", 52));
            list.add(new Instrument("Voice Oohs", 53));
            list.add(new Instrument("Synth Choir", 54));
            list.add(new Instrument("Orchestra Hit", 55));

        // Brass
            list.add(new Instrument("Trumpet", 56));
            list.add(new Instrument("Trombone", 57));
            list.add(new Instrument("Tuba", 58));
            list.add(new Instrument("Muted Trumpet", 59));
            list.add(new Instrument("French Horn", 60));
            list.add(new Instrument("Brass Section", 61));
            list.add(new Instrument("Synth Brass 1", 62));
            list.add(new Instrument("Synth Brass 2", 63));

        // Reed
            list.add(new Instrument("Soprano Sax", 64));
            list.add(new Instrument("Alto Sax", 65));
            list.add(new Instrument("Tenor Sax", 66));
            list.add(new Instrument("Baritone Sax", 67));
            list.add(new Instrument("Oboe", 68));
            list.add(new Instrument("English Horn", 69));
            list.add(new Instrument("Bassoon", 70));
            list.add(new Instrument("Clarinet", 71));

        // Pipe"
            list.add(new Instrument("Piccolo", 72));
            list.add(new Instrument("Flute", 73));
            list.add(new Instrument("Recorder", 74));
            list.add(new Instrument("Pan Flute", 75));
            list.add(new Instrument("Blown bottle", 76));
            list.add(new Instrument("Shakuhachi", 77));
            list.add(new Instrument("Whistle", 78));
            list.add(new Instrument("Ocarina", 79));

        // Synth Lead
            list.add(new Instrument("Square Wave", 80));
            list.add(new Instrument("Sawtooth Wave", 81));
            list.add(new Instrument("Calliope", 82));
            list.add(new Instrument("Chiffer", 83));
            list.add(new Instrument("Charang", 84));
            list.add(new Instrument("Voice", 85));
            list.add(new Instrument("Fifths", 86));
            list.add(new Instrument("Bass + Lead", 87));

        // Synth Pad
            list.add(new Instrument("New Age", 88));
            list.add(new Instrument("Warm", 89));
            list.add(new Instrument("Polysynth", 90));
            list.add(new Instrument("Choir", 91));
            list.add(new Instrument("Bowed", 92));
            list.add(new Instrument("Metalic", 93));
            list.add(new Instrument("Halo", 94));
            list.add(new Instrument("Sweep", 95));

        // Synth Effects
            list.add(new Instrument("Rain", 96));
            list.add(new Instrument("Soundtrack", 97));
            list.add(new Instrument("Crystal", 98));
            list.add(new Instrument("Atmosphere", 99));
            list.add(new Instrument("Brightness", 100));
            list.add(new Instrument("Goblins", 101));
            list.add(new Instrument("Echoes", 102));
            list.add(new Instrument("Sci-Fi", 103));

        // Ethnic
            list.add(new Instrument("Sitar", 104));
            list.add(new Instrument("Banjo", 105));
            list.add(new Instrument("Shamisen", 106));
            list.add(new Instrument("Koto", 107));
            list.add(new Instrument("Kalimba", 108));
            list.add(new Instrument("Bagpipe", 109));
            list.add(new Instrument("Fiddle", 110));
            list.add(new Instrument("Shanai", 111));

        // Percussive
            list.add(new Instrument("Tinkle Bell", 112));
            list.add(new Instrument("Agogo", 113));
            list.add(new Instrument("Steel Drums", 114));
            list.add(new Instrument("Woodblock", 115));
            list.add(new Instrument("Taiko Drum", 116));
            list.add(new Instrument("Melodic Tom", 117));
            list.add(new Instrument("Synth Drum", 118));
            list.add(new Instrument("Reverse Cymbal", 119));

        // Sound Effects
            list.add(new Instrument("Guitar Fret Noise", 120));
            list.add(new Instrument("Breath Noise", 121));
            list.add(new Instrument("Seashore", 122));
            list.add(new Instrument("Bird Tweet", 123));
            list.add(new Instrument("Telephone Ring", 124));
            list.add(new Instrument("Helicopter", 125));
            list.add(new Instrument("Applause", 126));
            list.add(new Instrument("Gunshot", 127));

        // Drum Sets
            drumList.add(new Instrument("Standard Drum Kit", 0));
            drumList.add(new Instrument("Room Kit", 8));
            drumList.add(new Instrument("Power Kit", 16));
            drumList.add(new Instrument("Electric Kit", 24));
            drumList.add(new Instrument("Rap TR808", 25));
            drumList.add(new Instrument("Jazz Kit", 32));
            drumList.add(new Instrument("Brush Kit", 40));

        list.addAll(drumList); //TODO get rid of this?
    }

    public static boolean isDrumSet(Instrument inst) {
        if (inst.name == "Standard Drum Kit" ||
            inst.name == "Room Kit"          ||
            inst.name == "Power Kit"         ||
            inst.name == "Electric Kit"      ||
            inst.name == "Rap TR808"         ||
            inst.name == "Jazz Kit"          ||
            inst.name == "Brush Kit") {
            return true;
        }
        return false;
    }
}
