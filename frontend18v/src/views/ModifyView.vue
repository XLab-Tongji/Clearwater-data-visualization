<template>
    <el-container style="margin:20px;">
        <el-card class="box-card" v-for="card in cards" :key="card.name" style="margin-right:25px;">
            <div slot="header" class="clearfix">
                <span>{{card.name}}
                </span>
                <el-button style="float: right; padding: 3px 0" type="text" @click="handleUpload(card.type)">{{card.uploadText}}</el-button>
            </div>
            <div class="text item">
                {{card.escri}}
            </div>
        </el-card>

        <el-dialog title="上传文件" :visible.sync="dialogVisible" width="30%" :before-close="handleClose">
            <el-upload class="upload-demo" ref="upload" action="https://jsonplaceholder.typicode.com/posts/"
                :on-preview="handlePreview" :on-remove="handleRemove" :file-list="fileList" :auto-upload="false"
                :accept="fileType" :on-progress="uploadOnProgress">
                <el-button slot="trigger" size="small" plain type="primary">选取文件</el-button>
                <el-button style="margin-left: 15px;" size="small" type="success" plain @click="submitUpload">上传到服务器</el-button>
                <div slot="tip" class="el-upload__tip" style="margin-bottom:15px;">只能上传{{this.fileType}}文件，且不超过500kb</div>
            </el-upload>
            <span slot="footer" class="dialog-footer">
                <el-button @click="dialogVisible = false">取 消</el-button>
                <el-button type="primary" @click="dialogVisible = false">确 定</el-button>
            </span>
        </el-dialog>

        <el-dialog title="输入地址" :visible.sync="dialogURLVisible" width="30%" :before-close="handleClose">
            <el-input placeholder="请输入内容" v-model="inputURL" style="margin: 15px 0px;">
                <template slot="prepend">http://</template>
            </el-input>
            <span slot="footer" class="dialog-footer">
                <el-button @click="dialogURLVisible = false">取 消</el-button>
                <el-button type="primary" @click="dialogURLVisible = false">确 定</el-button>
            </span>
        </el-dialog>
    </el-container>
</template>

<style scoped>
    .text {
        font-size: 14px;
    }

    .item {
        margin-bottom: 15px;
    }

    .input-with-select .el-input-group__prepend {
        background-color: #fff;
    }

    .clearfix:before,
    .clearfix:after {
        display: table;
        content: "";
    }

    .clearfix:after {
        clear: both
    }

    .box-card {
        width: 300px;
    }
</style>

<script>
    let axios = require('axios')
    export default {
        name: 'modify',
        data() {
            return {
                inputURL: '',
                dialogVisible: false,
                dialogURLVisible: false,
                fileList: [
                    //     {
                    //     name: 'food.jpeg',
                    //     url: 'https://fuss10.elemecdn.com/3/63/4e7f3a15429bfda99bce42a18cdd1jpeg.jpeg?imageMogr2/thumbnail/360x360/format/webp/quality/100'
                    // }, {
                    //     name: 'food2.jpeg',
                    //     url: 'https://fuss10.elemecdn.com/3/63/4e7f3a15429bfda99bce42a18cdd1jpeg.jpeg?imageMogr2/thumbnail/360x360/format/webp/quality/100'
                    // }
                ],
                cards: [{
                        name: '上传yaml部署文件',
                        type: '.yaml',
                        escri: '上传部署配置文件，系统将自动提取节点间的关系。',
                        uploadText: '上传文件'
                    },
                    {
                        name: '输入目标URL地址',
                        type: '.pod',
                        escri: '输入目标URL地址，系统将自动提取pods与节点间的关系。',
                        uploadText: '输入地址'
                    },
                    {
                        name: '上传csv数据集文件',
                        type: '.csv',
                        escri: '输入csv数据集，系统将自动提取性能与数据集列间的关系。',
                        uploadText: '上传文件'
                    },
                ],
                fileType: '.yaml'

            };
        },
        methods: {
            uploadOnProgress(e, file, fileList) { //开始上传

                console.log('file', file)
                console.log(fileList)

                var oneFile = file.raw;
                var formdata = new FormData(); // 创建form对象
                formdata.append('file', oneFile);

                let config = {
                    headers: {
                        'Content-Type': 'multipart/form-data'
                    }
                }; //添加请求头

                let uploadURL = 'http://10.60.38.182:9999/bbs/api/yamldeal';

                if (this.fileType == '.yaml') {
                    let uploadURL = 'http://10.60.38.182:9999/bbs/api/yamldeal';
                } else { // .csv
                    let uploadURL = 'http://10.60.38.182:9999/bbs/api/yamldeal';
                }

                axios.post(uploadURL,
                    formdata,
                    config).then((response) => { //这里的/xapi/upimage为接口
                    console.log(response.data);
                })
            },
            handleUpload(fileType) {
                this.fileType = fileType;
                console.log(fileType)
                if (fileType == '.pod') {
                    this.dialogURLVisible = true
                } else {
                    this.dialogVisible = true
                }
            },
            handleClose(done) {
                this.$confirm('确认关闭？')
                    .then(_ => {
                        done();
                    })
                    .catch(_ => {});
            },
            submitUpload() {
                this.$refs.upload.submit();
            },
            handleRemove(file, fileList) {
                console.log(file, fileList);
            },
            handlePreview(file) {
                console.log(file);
            }
        }
    }
</script>