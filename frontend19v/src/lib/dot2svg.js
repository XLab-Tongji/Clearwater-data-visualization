// 需要一个小后端读入文件 返回svg
const Viz = require('viz.js');
const { Module, render } = require('viz.js/full.render.js');

let viz = new Viz({ Module, render });

viz.renderString('digraph { a -> b }')
  .then(result => {
    console.log(result);
  })
  .catch(error => {
    // Create a new Viz instance (@see Caveats page for more info)
    viz = new Viz({ Module, render });

    // Possibly display the error
    console.error(error);
  });