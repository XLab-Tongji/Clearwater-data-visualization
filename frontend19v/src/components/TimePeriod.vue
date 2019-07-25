<template>
    <div>
        <VueCtkDateTimePicker class="time-picker" label="Select date range" v-model="value"  :range="range" format="YYYY-MM-DD" formatted="ll"/>
        <VueCtkDateTimePicker class="time-picker" label="Select start date time" v-model="value1" :only-time="onlytime" format="HH:mm:ss" formatted="HH:mm:ss"/>
        <VueCtkDateTimePicker class="time-picker" label="Select end date time" v-model="value2" :only-time="onlytime" format="HH:mm" formatted="HH:mm"/>
        <el-button @click="getEventsByTimeRange">
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
                console.log(this.value)
                console.log(this.value1)
                console.log(this.value2)
            }
        }
    }
</script>

<style scoped>
.time-picker {
    padding: 5px 5px;
}
</style>