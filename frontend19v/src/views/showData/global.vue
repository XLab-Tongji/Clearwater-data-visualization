<script>
import global from "../global";
const kpis = [
  {
    value: "1",
    label: "Network_Output_Packets"
  },
  {
    value: "2",
    label: "Network_Input_Packets"
  },
  {
    value: "3",
    label: "Network_Output_Bytes"
  },
  {
    value: "4",
    label: "Network_Input_Bytes"
  }
];
const url = global.base_url;
const timestampToTime = timestamp => {
  var date = new Date(timestamp * 1000); //时间戳为10位需*1000，时间戳为13位的话不需乘1000
  var Y = date.getFullYear() + "-";
  var M =
    (date.getMonth() + 1 < 10
      ? "0" + (date.getMonth() + 1)
      : date.getMonth() + 1) + "-";
  var D = (date.getDate() < 10 ? "0" + date.getDate() : date.getDate()) + " ";
  var h =
    (date.getHours() < 10 ? "0" + date.getHours() : date.getHours()) + ":";
  var m =
    (date.getMinutes() < 10 ? "0" + date.getMinutes() : date.getMinutes()) +
    ":";
  var s = date.getSeconds() < 10 ? "0" + date.getSeconds() : date.getSeconds();
  return Y + M + D + h + m + s;
};
export default {
  url,
  timestampToTime,
  kpis,
};
</script>