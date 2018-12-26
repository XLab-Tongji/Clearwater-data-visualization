<template>
    <el-container style="padding:30px;">
        <el-row :gutter="20" style="width:100%">
            <el-col :span="10">
                <!-- <el-row>
                    <el-transfer filterable :filter-method="filterMethod" filter-placeholder="请输入实体拼音" v-model="entity"
                        :data="allEntityIndexed" :titles="['Entities', 'Selected']">
                    </el-transfer>
                </el-row>

                <el-row style="margin-top:30px;">
                    <el-transfer filterable :filter-method="filterMethod" filter-placeholder="请输入属性拼音" v-model="property"
                        :data="allEntityIndexed" :titles="['Properties', 'Selected']">
                        
                    </el-transfer>
                </el-row> -->
                <el-form ref="form" :model="form" label-width="80px">
                    <el-form-item label="实体名称">
                        <el-select v-model="form.entity" filterable remote reserve-keyword placeholder="请输入关键词"
                            :remote-method="remoteMethod" :loading="loading">
                            <el-option v-for="item in options4" :key="item.value" :label="item.label" :value="item.value">
                            </el-option>
                        </el-select>
                    </el-form-item>
                    <el-form-item label="属性名称">
                        <!-- <el-select v-model="form.property" multiple filterable remote reserve-keyword placeholder="请输入关键词"
                            :remote-method="remoteMethod" :loading="loading">
                            <el-option v-for="item in options4" :key="item.value" :label="item.label" :value="item.value">
                            </el-option>
                        </el-select> -->
                        <el-cascader expand-trigger="hover" :options="properties" v-model="form.property" @change="handleChange">
                        </el-cascader>
                    </el-form-item>
                    <el-form-item label="关系">
                        <el-input placeholder="请输入关系" v-model="form.relation" clearable>
                        </el-input>
                    </el-form-item>
                    <el-form-item>
                        <el-button type="primary" @click="onSubmit">添加</el-button>
                        <el-button @click="onCancel">取消</el-button>
                    </el-form-item>
                </el-form>
            </el-col>

            <!-- <el-col :span="2" style="height:100%">
                <el-button type="success" icon="el-icon-check" circle style="margin-top:200px" @click="transferClick"></el-button>
            </el-col> -->

            <el-col :span="12" :offset="2">
                <el-table :data="tableData4" style="width: 100%" max-height="500">
                    <el-table-column prop="entity" label="实体" width="100">
                    </el-table-column>
                    <el-table-column prop="property" label="属性" width="200">
                    </el-table-column>
                    <el-table-column prop="relation" label="关系" width="150">
                    </el-table-column>
                    <el-table-column fixed="right" label="操作" width="120">
                        <template slot-scope="scope">
                            <el-button @click.native.prevent="deleteRow(scope.$index, tableData4)" type="text" size="small">
                                移除
                            </el-button>
                        </template>
                    </el-table-column>
                </el-table>
            </el-col>
        </el-row>

    </el-container>

</template>

<style scoped>

</style>

<script>
    const axios = require('axios')
    export default {
        name: 'EnterRel',
        data() {
            const generateData2 = _ => {
                const data = [];
                const cities = ['上海', '北京', '广州', '深圳', '南京', '西安', '成都'];
                const pinyin = ['shanghai', 'beijing', 'guangzhou', 'shenzhen', 'nanjing', 'xian', 'chengdu'];
                cities.forEach((city, index) => {
                    data.push({
                        label: city,
                        key: index,
                        pinyin: pinyin[index]
                    });
                });
                return data;
            };
            const generateEntity = _ => {
                const data = [];
                var cities = ['上海', '北京', '广州', '深圳', '南京', '西安', '成都'];
                const pinyin = ['shanghai', 'beijing', 'guangzhou', 'shenzhen', 'nanjing', 'xian', 'chengdu'];

                axios.get('http://10.60.38.182:9999/bbs/api/getAllLabel')
                    .then((response) => {
                        cities = response.data.Label;
                        this.allEntity = cities;
                        this.allProperty = cities;
                        cities.forEach((city, index) => {
                            data.push({
                                label: city,
                                key: index,
                                pinyin: pinyin[index]
                            });
                        });
                    })

                return data;
            };
            const generateProperty = _ => {
                const data = [];
                const cities = ['上海', '北京', '广州', '深圳', '南京', '西安', '成都'];
                const pinyin = ['shanghai', 'beijing', 'guangzhou', 'shenzhen', 'nanjing', 'xian', 'chengdu'];
                cities.forEach((city, index) => {
                    data.push({
                        label: city,
                        key: index,
                        pinyin: pinyin[index]
                    });
                });
                return data;
            };
            return {
                // data2: generateData2(),
                form: {
                    entity: [],
                    relation: '',
                    property: [],
                },
                properties: [],
                allEntityIndexed: generateEntity(),
                allPropertyIndexed: generateProperty(),
                allEntity: ['上海', '北京', '广州', '深圳', '南京', '西安', '成都'],
                allProperty: ['上海', '北京', '广州', '深圳', '南京', '西安', '成都'],
                entity: [],
                property: [],
                selectedOptions: [],
                filterMethod(query, item) {
                    console.log(item)
                    return item.pinyin.indexOf(query) > -1;
                },
                tableData4: [{
                    date: '2016-05-03',
                    name: '王小虎',
                    province: '上海',
                    city: '普陀区',
                    address: '上海市普陀区金沙江路 1518 弄',
                    zip: 200333
                }, {
                    date: '2016-05-02',
                    name: '王小虎',
                    province: '上海',
                    city: '普陀区',
                    address: '上海市普陀区金沙江路 1518 弄',
                    zip: 200333
                }, {
                    date: '2016-05-04',
                    name: '王小虎',
                    province: '上海',
                    city: '普陀区',
                    address: '上海市普陀区金沙江路 1518 弄',
                    zip: 200333
                }, {
                    date: '2016-05-01',
                    name: '王小虎',
                    province: '上海',
                    city: '普陀区',
                    address: '上海市普陀区金沙江路 1518 弄',
                    zip: 200333
                }, {
                    date: '2016-05-08',
                    name: '王小虎',
                    province: '上海',
                    city: '普陀区',
                    address: '上海市普陀区金沙江路 1518 弄',
                    zip: 200333
                }, {
                    date: '2016-05-06',
                    name: '王小虎',
                    province: '上海',
                    city: '普陀区',
                    address: '上海市普陀区金沙江路 1518 弄',
                    zip: 200333
                }, {
                    date: '2016-05-07',
                    name: '王小虎',
                    province: '上海',
                    city: '普陀区',
                    address: '上海市普陀区金沙江路 1518 弄',
                    zip: 200333
                }],

                options4: [],
                value9: [],
                list: [],
                loading: false,
                states: ["Alabama", "Alaska", "Arizona",
                    "Arkansas", "California", "Colorado",
                    "Connecticut", "Delaware", "Florida",
                    "Georgia", "Hawaii", "Idaho", "Illinois",
                    "Indiana", "Iowa", "Kansas", "Kentucky",
                    "Louisiana", "Maine", "Maryland",
                    "Massachusetts", "Michigan", "Minnesota",
                    "Mississippi", "Missouri", "Montana",
                    "Nebraska", "Nevada", "New Hampshire",
                    "New Jersey", "New Mexico", "New York",
                    "North Carolina", "North Dakota", "Ohio",
                    "Oklahoma", "Oregon", "Pennsylvania",
                    "Rhode Island", "South Carolina",
                    "South Dakota", "Tennessee", "Texas",
                    "Utah", "Vermont", "Virginia",
                    "Washington", "West Virginia", "Wisconsin",
                    "Wyoming"
                ]
            }
        },

        methods: {
            deleteRow(index, rows) {
                rows.splice(index, 1);
            },
            onSubmit() {

                axios.post(
                        'http://10.60.38.182:9999/bbs/api/addMetric?pod=' + this.form.property[2] + '&metric=' + this.form
                        .property[
                            3] + '&container=' + this.form.property[1] + '&dateset=' + this.form.property[0] +
                        '&relation=' + this.form.relation
                    ).then((response) => {
                        let log = response.data.state;
                        this.$message({
                            message: log,
                            type: 'success'
                        });
                        this.tableData4.push({
                            entity: this.form.entity,
                            property: this.form.property[3],
                            relation: this.form.relation
                        });
                    })
                    .catch((error) => {
                        // handle error
                        console.log(error);
                        this.$message.error('出现错误，请稍后再试。');

                    })
            },
            onCancel() {
                this.form = {
                    entity: [],
                    relation: '',
                    property: [],
                }
            },
            transferClick() {
                this.entity.forEach(elementE => {
                    this.property.forEach(elementP => {
                        this.tableData4.push({
                            entity: this.allEntity[elementE],
                            property: this.allProperty[elementP]
                        })
                    });
                });
            },
            remoteMethod(query) {
                if (query !== '') {
                    this.loading = true;
                    setTimeout(() => {
                        this.loading = false;
                        this.options4 = this.list.filter(item => {
                            return item.label.toLowerCase()
                                .indexOf(query.toLowerCase()) > -1;
                        });
                    }, 200);
                } else {
                    this.options4 = [];
                }
            },
            handleChange(value) {
                console.log(value);
            }
        },
        created() {
            axios.get('http://lab:409@10.60.38.182:5525/tool/api/v1.0/get_namespace')
            .then((response)=>{
                console.log(response.data)
            })
            this.tableData4 = [];
            // this.form.property =
            axios.get('http://10.60.38.182:9999/bbs/api/metricInfo?filename=result.csv')
                .then((response) => {
                    var Container = response.data.Container;
                    let entity = [];
                    let firstCasc = {
                        label: 'result.csv',
                        value: 'result.csv'
                    }
                    firstCasc.children = []


                    Container.name.forEach(element => {
                        let secondCasc = {};
                        secondCasc.label = element
                        secondCasc.value = element

                        secondCasc.children = [];

                        for (const key in response.data) {
                            if (response.data.hasOwnProperty(key)) {
                                if (key == element) {
                                    const containerElement = response.data[key];
                                    let service = containerElement.service;
                                    service.forEach(element => {
                                        entity.push(element);
                                    });

                                    delete containerElement.service;


                                    for (const key in containerElement) {
                                        if (containerElement.hasOwnProperty(key)) {
                                            const element = containerElement[key];
                                            let thirdCasc = {}
                                            thirdCasc.label = key;
                                            thirdCasc.value = key;
                                            thirdCasc.children = element;

                                            let tempChildren = []

                                            thirdCasc.children.forEach(element => {
                                                let temp = {}
                                                temp.label = element;
                                                temp.value = element;
                                                tempChildren.push(temp)

                                            });

                                            thirdCasc.children = tempChildren;
                                            console.log(thirdCasc)

                                            // console.log(children)
                                            secondCasc.children.push(thirdCasc);
                                        }
                                    }

                                }
                            }
                        }
                        firstCasc.children.push(secondCasc)
                    });

                    let tempEntity = []
                    entity.forEach(element => {
                        tempEntity.push({
                            label: element,
                            value: element,
                        })
                    });

                    // this.form.entity = tempEntity;
                    this.options4 = tempEntity;
                    this.properties = [firstCasc];
                })
        },
        mounted() {
            this.list = this.states.map(item => {
                return {
                    value: item,
                    label: item
                };
            });
        },

    }
</script>