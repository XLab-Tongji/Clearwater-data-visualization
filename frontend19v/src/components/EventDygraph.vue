<template>
    <div id="event-container">
        <div id="div_g" class="graph_div">

        </div>
    </div>
</template>

<script>
    import Dygraph from 'dygraphs';
    export default {
        name: "event-dygraph",
        props: {
            eventObj: Array,
        },
        components: {

        },
        data() {
            return {
                value: null,
                value1: null,
                value2: null,
                range: true,
                onlytime: true
            };
        },
        computed: {

        },
        created(){

        },
        mounted() {
            // var data = [];
            // var t = new Date();
            // for (var i = 10; i >= 0; i--) {
            //     var x = new Date(t.getTime() - i * 1000);
            //     data.push([x, Math.floor(Math.random()*4)] );
            // }

            // console.log(data)
            var data = this.eventObj;
            for(var i=0;i<data.length;i++){
                data[i][0] = new Date(data[i][0]);
            }
            // console.log(data)
            let _this = this;
            var g = new Dygraph(document.getElementById("div_g"), data,
                {
                    legend: 'always',
                    width:1000,
                    height:300,
                    drawPoints: true,
                    showRoller: true,
                    //rollPeriod: 14,
                    valueRange: [0, 3],
                    labels: ['Time', 'Event'],
                    strokeWidth: 0.0,
                    pointSize: 8,
                    // pointClickCallback: function callback(e, point) {
                    //     //emit the event to parents component
                    //     //timestamp is point.xval eg.1564297357987
                    // },

                    pointClickCallback: function callback(e, point) {
                        //emit the event to parents component
                        //timestamp is point.xval eg.1564297357987
                        _this.$emit("pointClickCallback", point.xval)
                    }

                });
        },
        methods: {
            getEventsByTimeRange() {
                console.log(this.value)
                console.log(this.value1)
                console.log(this.value2)
            }
        }
    }
</script>

<style scoped>
    #event-container {
        position: fixed;
        /* bottom: -0px; */
        bottom: 50px;
        width: 60%;
        height: 320px;
        text-align: center;
        white-space: nowrap; /* scroll x effects */
        border: 1px solid lightgray;
        background-color: rgb(255, 255, 255);
        border-radius: 10px;
        padding: 20px 20px;
        transition: bottom 0.3s;
    }

    #graph_div {
        padding:15px 15px
    }
</style>