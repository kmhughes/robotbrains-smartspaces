// Will write to server scsyndef folder
SynthDef.new("InteractiveSpacesExample", { arg freq = 440, volume = 1;
  Out.ar(0, SinOsc.ar(freq, 0, volume));
}).writeDefFile;

// Experiment with Open Sound Control Messages

// Load synth into server
s.sendMsg("/d_load", "/home/keith/InteractiveSpacesExample.scsyndef");

// Create the synth
s.sendMsg("/s_new", "InteractiveSpacesExample", 1000);

// Change the arguments to the synth
s.sendMsg("/n_set", 1000, "freq", 430, "volume", 1.0);

// Delete the synth from the server
s.sendMsg("/n_free", 1000);

