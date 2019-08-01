<template>
    <div>
        <VueCtkDateTimePicker class="time-picker" label="Select date range" v-model="value"  :range="range" format="YYYY-MM-DD" formatted="ll"/>
        <VueCtkDateTimePicker class="time-picker" label="Select start date time" v-model="value1" :only-time="onlytime" format="HH:mm" formatted="HH:mm"/>
        <VueCtkDateTimePicker class="time-picker" label="Select end date time" v-model="value2" :only-time="onlytime" format="HH:mm" formatted="HH:mm"/>
        <el-button @click.prevent="getEventsByTimeRange">
            Comfirm
        </el-button>
    </div>
</template>

<script>
    import VueCtkDateTimePicker from 'vue-ctk-date-time-picker';
    import 'vue-ctk-date-time-picker/dist/vue-ctk-date-time-picker.css';
    import ElButton from "element-ui/packages/button/src/button";
    export default {
        name: "time-period",
        props: {
            allTimeStamps: Array,
            eventList: Array
        },
        components: {
            ElButton,
            VueCtkDateTimePicker,
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
        watch: {

        },
        mounted() {

        },
        methods: {
            getEventsByTimeRange() {
                var timeObj={
                    startDate:this.value.start,
                    endDate:this.value.end,
                    startTime:this.value1,
                    endTime:this.value2,
                    toggle:true
                }
                // console.log(timeObj)
                this.$emit("getEventsByTimeRange", timeObj)
            }
        }
    }
</script>

<style scoped>
.time-picker {
    padding: 5px 5px;
}
</style>