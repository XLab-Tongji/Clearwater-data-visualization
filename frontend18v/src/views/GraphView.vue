<template>
    <div>
        <div id="alchemy" class="alchemy" style="width:100%;height:680px;"></div>
    </div>
</template>

<style scoped>
</style>

<script>
    let axios = require('axios')
    var some_data = {
        "nodes": [{
                "id": 1
            },
            {
                "id": 2
            },
            {
                "id": 3
            }
        ],
        "edges": [{
                "source": 1,
                "target": 2
            },
            {
                "source": 1,
                "target": 3,
            }
        ]
    };

    export default {
        name: 'graph',
        data() {
            return {
                refresh: false,
                alchemy: ''
            }
        },
        mounted() {
            this.refresh = true;
            this.alchemy = alchemy;

            axios.get('/example.json')
                .then((response) => {
                    console.log(response)
                    let nodes = response.data.nodes;
                    let edges = response.data.links;

                    let data = {
                        nodes: nodes,
                        edges: edges
                    }

                    console.log(data)

                    this.alchemy.begin({
                        // "dataSource": require('../assets/example.json')
                        "dataSource": data,
                        "nodeCaption": function (node) {
                            return node.name;
                        },
                        edgeCaption: function (node) {
                            return node.type;
                        },
                        forceLocked: false,
                        "linkDistance": function () {
                            return 40;
                        },

                    });
                })

        }
    }
</script>