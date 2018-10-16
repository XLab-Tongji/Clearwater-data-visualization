<template>
  <div id="app">
    <el-container class="whole">
      <el-container style="width:80%;">
        <el-header height="20%">
          <el-row>
            <el-col :span="10">
              <el-input placeholder="Please keywords to search for specific resource" prefix-icon="el-icon-search" v-model="keyword">
              </el-input>
            </el-col>
            <el-col :span="4">
              <el-button plain icon="el-icon-search" v-on:click.native="search()">Search</el-button>
            </el-col>
          </el-row>
          <el-row>
            <el-col :span="12">
              <el-radio-group v-model="whichResourceToShow" @change="display">
                <el-radio-button v-for="item in ['All', 'Resource', 'Failure', 'ADR', 'Test Case']" :label="item" :key="item"></el-radio-button>
              </el-radio-group>
            </el-col>
            <el-col :offset="2" :span="9">
              <label style="color:white; display: block;"><input style="width:240px; margin-top: 20pt;" type="range" min="0" max="0.2" step="any" value="0.1"> Link Strength</label>
            </el-col>
          </el-row>
        </el-header>
        <el-main>
          <el-row style="margin-bottom: 10px">
            <el-tag id="id">id: </el-tag>
            <el-tag id="name">name: </el-tag>
            <el-tag id="type">type: </el-tag>
            <el-tag id="layer">layer: </el-tag>
          </el-row>
          <el-row>
            <img id="loading" src="./assets/loading.gif" />
            <svg id="graph" width="1000" height="500"></svg>
          </el-row>
          <el-row>
            <img src="./assets/Rectangle.png" />
          </el-row>
        </el-main>
      </el-container>
      <el-aside width="25%">
        <el-collapse v-model="activeName" accordion>
          <el-collapse-item title="Information show" name="Information">
            <el-row style="margin-bottom:20px">
              <el-col :offset="2" :span="20">
                <el-form label-position="top" style="text-align:left;height:50%">
                  <el-form-item label="Id">
                      <el-input id="this_id" disabled></el-input>
                  </el-form-item>
                  <el-form-item label="Name">
                    <el-input id="this_name" disabled></el-input>
                  </el-form-item>
                  <el-form-item label="Type">
                    <el-input id="this_type" disabled></el-input>
                  </el-form-item>
                  <el-form-item label="Layer">
                    <el-input id="this_layer" disabled></el-input>
                  </el-form-item>
                  <el-form-item label="Performance">
                    <el-input type="textarea" id="this_performance" disabled></el-input>
                  </el-form-item>
                </el-form>
              </el-col>
            </el-row>
          </el-collapse-item>
          <el-collapse-item title="Resource" name="Resource">
            <el-row>
              <el-col :offset="2" :span="20">
                <el-form label-position="top" style="text-align:left">
                  <el-form-item label="Resourece Name">
                    <el-input v-model="newResource.name"></el-input>
                  </el-form-item>
                  <el-form-item label="Resourece Type">
                    <el-input v-model="newResource.type"></el-input>
                  </el-form-item>
                  <el-form-item label="Resourece Layer">
                    <el-input v-model="newResource.layer"></el-input>
                  </el-form-item>
                  <el-form-item label="Resourece Performance">
                    <el-input v-model="newResource.performance"></el-input>
                  </el-form-item>
                  <el-form-item label="Resourece Relations">
                    <el-input v-model="newResource.relations"></el-input>
                  </el-form-item>
                  <el-button type="primary" size="mini" v-on:click.native="add(1)">提交</el-button>
                </el-form>
              </el-col>
            </el-row>
          </el-collapse-item>
          <el-collapse-item title="Failure" name="Failure">
            <el-row>
              <el-col :offset="2" :span="20">
                <el-form label-position="top" style="text-align:left">
                  <el-form-item label="Failure Name">
                    <el-input v-model="newFailure.name"></el-input>
                  </el-form-item>
                  <el-form-item label="Failure Type">
                    <el-select v-model="newFailure.type" placeholder="Choose failure type" size="medium" style="display:inline">
                      <el-option v-for="item in ['Fail slow', 'Fail partial', 'Fail stop', 'Fail transient', 'Fail as well as corruption']" :key="item" :value="item">
                      </el-option>
                    </el-select>
                  </el-form-item>
                  <el-form-item label="Failure Description">
                    <el-input type="textarea" v-model="newFailure.performance"></el-input>
                  </el-form-item>
                  <el-form-item label="Failure Relations">
                    <el-input v-model="newFailure.relations"></el-input>
                  </el-form-item>
                  <el-button type="primary" size="mini" v-on:click.native="add(2)">提交</el-button>
                </el-form>
              </el-col>
            </el-row>
          </el-collapse-item>
          <el-collapse-item title="ADR" name="ADR">
            <el-row>
              <el-col :offset="2" :span="20">
                <el-form label-position="top" style="text-align:left">
                  <el-form-item label="ADR Name">
                    <el-input v-model="newADR.name"></el-input>
                  </el-form-item>
                  <el-form-item label="ADR Link">
                    <el-input v-model="newADR.performance"></el-input>
                  </el-form-item>
                  <el-form-item label="ADR Relations">
                    <el-input v-model="newADR.relations"></el-input>
                  </el-form-item>
                  <el-button type="primary" size="mini" v-on:click.native="add(3)">提交</el-button>
                </el-form>
              </el-col>
            </el-row>
          </el-collapse-item>
          <el-collapse-item title="Test Case" name="Test Case">
            <el-row>
              <el-col :offset="2" :span="20">
                <el-form label-position="top" style="text-align:left">
                  <el-form-item label="Test Case Name">
                    <el-input v-model="newTestCase.name"></el-input>
                  </el-form-item>
                  <el-form-item label="Test Case Link">
                    <el-input v-model="newTestCase.performance"></el-input>
                  </el-form-item>
                  <el-form-item label="Test Case Relations">
                    <el-input v-model="newTestCase.relations"></el-input>
                  </el-form-item>
                  <el-button type="primary" size="mini" v-on:click.native="add(4)">提交</el-button>
                </el-form>
              </el-col>
            </el-row>
          </el-collapse-item>
        </el-collapse>
      </el-aside>
    </el-container>
  </div>
</template>

<script>
  import {getAll, addOne, getLabel} from './assets/API'
  import {showData2} from "./assets/dataV";
  import $ from 'jquery'


  export default {
    data() {
      return {
        nodes:[],
        links:[],
        activeName: 'Information',
        keyword: '',
        newResource: {},
        newADR: {},
        newFailure: {},
        newTestCase: {},
        whichResourceToShow: 'All',
      }
    },
    methods: {
        showAll: function () {
            var self = this;
            $.ajax({
                type: 'get',
                async: true,
                crossDomain: true,
                // url: 'http://localhost:8088/bbs/api/getAll',
                url: '/bbs/api/getAll',
                success: function (res) {
                    self.nodes = res.nodes;
                    self.links = res.links;
                    showData2(self.nodes, self.links);
                    document.getElementById('loading').style.display = 'none';
                },
                error: function (res) {
                    console.log('Something went wrong!')
                }
            })
        },
        display: function () {
            document.getElementById('loading').style.display = 'inline';
            if (this.whichResourceToShow === 'All')
                this.showAll();
            else {
                var labelName;
                switch (this.whichResourceToShow) {
                    case 'Resource':
                        labelName = 'ResourceNode';
                        break;
                    case 'Failure':
                        labelName = 'Failure_Node';
                        break;
                    case 'ADR':
                        labelName = 'ADR_Node';
                        break;
                    case 'Test Case':
                        labelName = 'TestCase_Node';
                        break;
                }
                var self = this;
                $.ajax({
                    type: 'post',
                    async: true,
                    crossDomain: true,
                    // url: 'http://localhost:8088/bbs/api/getLabel',
                    url: '/bbs/api/getLabel',
                    beforeSend: function (xhr) {
                        xhr.setRequestHeader('Content-type', 'application/x-www-form-urlencoded')
                    },
                    data: {label:labelName},
                    success: function (res) {
                        self.nodes = res.nodes;
                        self.links = res.links;
                        showData2(self.nodes, self.links);
                        document.getElementById('loading').style.display = 'none';
                    },
                    error: function (res) {
                        console.log('A Something wrong!')
                    }
                })
            }
        },
        add: function (type) {
            var inputData;
            switch (type) {
                case 1:
                    this.newResource.label = "ResourceNode";
                    var array = this.newResource.relations.split(", ");
                    for(var i=0; i<array.length; i++)
                        array[i] = parseInt(array[i]);
                    this.newResource.relations = array;
                    inputData = this.newResource;
                    break;
                case 2:
                    this.newFailure.label = "Failure_Node";
                    this.newFailure.layer = "0";
                    var array = this.newFailure.relations.split(", ");
                    for(var i=0; i<array.length; i++)
                        array[i] = parseInt(array[i]);
                    this.newFailure.relations = array;
                    inputData = this.newFailure;
                    break;
                case 3:
                    this.newADR.label = "ADR_Node";
                    this.newADR.layer = "0";
                    this.newADR.type = "ADR;"
                    var array = this.newADR.relations.split(", ");
                    for(var i=0; i<array.length; i++)
                        array[i] = parseInt(array[i]);
                    this.newADR.relations = array;
                    inputData = this.newADR;
                    break;
                case 4:
                    this.newTestCase.label = "TestCase_Node";
                    this.newTestCase.layer = "0";
                    this.newTestCase.type = "Test Case;"
                    var array = this.newTestCase.relations.split(", ");
                    for(var i=0; i<array.length; i++)
                        array[i] = parseInt(array[i]);
                    this.newTestCase.relations = array;
                    inputData = this.newTestCase;
                    break;
            }
            console.log(inputData);
            var result = addOne(inputData);
            alert(result.state);
            this.display();
        },
        search: function () {
            var self = this;
            document.getElementById('loading').style.display = 'inline';
            $.ajax({
                type: 'post',
                async: true,
                crossDomain: true,
                url: 'http://localhost:8088/bbs/api/getNeighbors',
                beforeSend: function (xhr) {
                    xhr.setRequestHeader('Content-type', 'application/x-www-form-urlencoded')
                },
                data: {name:self.keyword},
                success: function (res) {
                    self.links = res.links
                    self.nodes = res.nodes
                    showData2(self.nodes, self.links)
                    document.getElementById('loading').style.display = 'none'
                },
                error: function (res) {
                    console.log('A Something wrong!')
                }
            })
        }
    }
  }
</script>

<style>
  #app {
    font-family: Helvetica, sans-serif;
    text-align: center;
  }
  .whole {
    position: absolute;
    width: 100%;
    height: 100%;
  }
  .el-header {
    background-color: #232931;
    color: #616161;
    text-align: center;
    line-height: 60px;
  }
  .el-aside {
    background-color: #393e46;
    color: #616161;
    text-align: center;
    line-height: 200px;
  }
  .el-main {
    background-color: #eeeeee;
    color: #616161;
    text-align: center;
    /*line-height: 160px;*/
    position: relative;
  }
  .el-icon-arrow-right:before {
    content: "\E62B";
  }
  #graph{
    border: 1px solid blue;
    max-width: 100%;
    scroll-behavior: auto;
  }
  #loading{
    position: absolute;
    left: 35%;
    top: 100px;
    display: none;
  }
</style>
