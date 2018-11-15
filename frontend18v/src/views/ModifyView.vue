<template>
    <el-container style="margin:20px;">
        <el-card class="box-card" v-for="card in cards" :key="card.name" style="margin-right:25px;">
            <div slot="header" class="clearfix">
                <span>上传{{card.name}}
                </span>
                <el-button style="float: right; padding: 3px 0" type="text" @click="handleUpload(card.type)">上传文件</el-button>
            </div>
            <div class="text item">
                {{card.escri}}
            </div>
        </el-card>

        <el-dialog title="上传文件" :visible.sync="dialogVisible" width="30%" :before-close="handleClose">
            <el-upload class="upload-demo" ref="upload" action="https://jsonplaceholder.typicode.com/posts/"
                :on-preview="handlePreview" :on-remove="handleRemove" :file-list="fileList" :auto-upload="false"
                :accept="fileType">
                <el-button slot="trigger" size="small" plain type="primary">选取文件</el-button>
                <el-button style="margin-left: 15px;" size="small" type="success" plain @click="submitUpload">上传到服务器</el-button>
                <div slot="tip" class="el-upload__tip" style="margin-bottom:15px;">只能上传{{this.fileType}}文件，且不超过500kb</div>
            </el-upload>
            <span slot="footer" class="dialog-footer">
                <el-button @click="dialogVisible = false">取 消</el-button>
                <el-button type="primary" @click="dialogVisible = false">确 定</el-button>
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
    export default {
        name: 'modify',
        data() {
            return {
                dialogVisible: false,
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
                        name: 'yaml部署文件',
                        type: '.yaml',
                        escri: '上传部署配置文件，系统将自动提取节点间的关系。'
                    },
                    {
                        name: 'pod配置文件',
                        type: '.pod',
                        escri: '上传pod配置文件，系统将自动提取pods与节点间的关系。（其实我没懂这里要干嘛？？救命。）'
                    },
                    {
                        name: 'csv数据集文件',
                        type: '.csv',
                        escri: '输入csv数据集，系统将自动提取性能与数据集列间的关系。'
                    },
                ],
                fileType: '.yaml'

            };
        },
        methods: {
            handleUpload(fileType) {
                this.fileType = fileType;
                this.dialogVisible = true
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