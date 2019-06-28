// 前后两个时间戳的数据对比
// 打开显示 diff 按钮时：
// 给增加的类新增 class：diff-add
// 给删除的类新增 class: diff-delete

export default {
  diffNodes: function diffNodes(dataBefore, dataAfter) {
    const jsondiffpatch = require('jsondiffpatch').create({
      // used to match objects when diffing arrays, by default only === operator is used
      objectHash: function (obj) {
        // this function is used only to when objects are not equal by ref
        return obj._id || obj.id;
      },
      arrays: {
        // default true, detect items moved inside the array (otherwise they will be registered as remove+add)
        detectMove: true,
        // default false, the value of items moved is not included in deltas
        includeValueOnMove: false
      },
      textDiff: {
        // default 60, minimum string length (left and right sides) to use text diff algorythm: google-diff-match-patch
        minLength: 60
      },
      propertyFilter: function (name, context) {
        /*
         this optional function can be specified to ignore object properties (eg. volatile data)
          name: property name, present in either context.left or context.right objects
          context: the diff context (has context.left and context.right objects)
        */
        let ignoreProps = ['index', 'svgObj', 'svgSym', 'vx', 'vy', 'x', 'y', 'v', 'svgIcon', '_cssClass']
        if (ignoreProps.indexOf(name) !== -1)
          return false
        return true
      },
      cloneDiffValues: false /* default false. if true, values in the obtained delta will be cloned
    (using jsondiffpatch.clone by default), to ensure delta keeps no references to left or right objects. this becomes useful if you're diffing and patching the same objects multiple times without serializing deltas.
    instead of true, a function can be specified here to provide a custom clone(value)
    */
    });
    return jsondiffpatch.diff(dataBefore, dataAfter);
  },


  diffLinks: function diffLinks(dataBefore, dataAfter) {
    const jsondiffpatch = require('jsondiffpatch').create({
      // used to match objects when diffing arrays, by default only === operator is used
      objectHash: function (obj) {
        return obj.sid + obj.tid
      },
      arrays: {
        // default true, detect items moved inside the array (otherwise they will be registered as remove+add)
        detectMove: true,
        // default false, the value of items moved is not included in deltas
        includeValueOnMove: false
      },
      textDiff: {
        // default 60, minimum string length (left and right sides) to use text diff algorythm: google-diff-match-patch
        minLength: 60
      },
      propertyFilter: function (name, context) {
        /*
         this optional function can be specified to ignore object properties (eg. volatile data)
          name: property name, present in either context.left or context.right objects
          context: the diff context (has context.left and context.right objects)
        */
        let ignoreProps = ['id', 'index', 'source', 'target', '_cssClass', '_color', '_linkLabelClass', '_svgAttrs']
        if (ignoreProps.indexOf(name) !== -1)
          return false
        return true
      },
      cloneDiffValues: false /* default false. if true, values in the obtained delta will be cloned
    (using jsondiffpatch.clone by default), to ensure delta keeps no references to left or right objects. this becomes useful if you're diffing and patching the same objects multiple times without serializing deltas.
    instead of true, a function can be specified here to provide a custom clone(value)
    */
    });
    return jsondiffpatch.diff(dataBefore, dataAfter);
  }
}

