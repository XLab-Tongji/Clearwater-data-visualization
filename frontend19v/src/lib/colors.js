const colors = ["blueviolet", "coral", "lightgreen", "hotpink"]
let nodeIdsSet = new Set()

export function getColor(id) {
  nodeIdsSet.add(id);
  let index = Array.from(nodeIdsSet).indexOf(id);
  return colors[index % colors.length];
}