/*global require, dat*/

require.config({
  deps : ['vendor/Events', 'vendor/lodash']
});

require(
  [
    'lib/ParticleSystem',
    'lib/Display',
    'lib/Vector'
  ],
  function(ParticleSystem, Display, Vector){
    "use strict";

    var canvas = document.getElementById('canvas');
    var ctx = canvas.getContext('2d');
    window.addEventListener('resize', resize); resize();

    var display = new Display(document.getElementById('canvas'));
    display.init();
    var particleSystem = new ParticleSystem().init(display);
    display.start();

    //var gui = new GUI(particleSystem, display);

    particleSystem.addEmitter(new Vector(360,280),Vector.fromAngle(0,2));
    var field1 = particleSystem.addField(new Vector(700,230), -140);
    var field2 = particleSystem.addField(new Vector(700,430), -140);

    function resize() {
      canvas.width = window.innerWidth;
      canvas.height = window.innerHeight;
    }
    
    var ws = new WebSocket("ws://localhost:9101/websocket"); 

    ws.onopen = function(event) { }

    ws.onmessage = function(event) { 
        var data = JSON.parse(event.data);
        field1.setMass(data.field1);
        field2.setMass(data.field2);
    }

    ws.onclose = function(event) { }
  }
);

