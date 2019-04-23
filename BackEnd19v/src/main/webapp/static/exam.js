var exam = (function () {
    var $container;
    var init = function (container) {
        $container = container;
        sidebar_function_init();
        wait_patient_refresh();
        my_patient_refresh();
        exam_result_refresh();
        exam_result_confirm();
        exam_report_upload();
        test_post_rest();
    }
    var test_post_rest=function (){
        $container.find('#test').click(function () {
            var post = {
                Action:"test message"
            };
            ajaxPost("/bbs/test_post_rest",post,function (data) {
                alert(data.result)

            });

        })
    }
    var exam_report_upload=function (){
        $container.find('#SubmitReport').click(function () {
            var post = {
                Action:"setReport"
            };
            ajaxPost("/exam/examReport",post,function (data) {
               alert("发送成功")

            });

        })
    }
    var exam_result_confirm=function (){
        $container.find('#examresultConfirm').click(function () {
            var post = {
                Action:"getReport"
            };
            ajaxPost("/exam/examReport",post,function (data) {
                $container.find('#name').text(data.name)
                $container.find('#diagnose').text(data.diagnose)
                $container.find('#doctor').text(data.doctor)
                $container.find('#examtype').text(data.examtype)
                $container.find('#gender').text(data.gender)
                $container.find('#department').text(data.department)
                $container.find('#examdoctor').text(data.examdoctor)
                $container.find('#printtime').text(data.printtime)
                $container.find('#age').text(data.age)
                $container.find('#number').text(data.number)
                $container.find('#examtime').text(data.examtime)
                $container.find('#tip').text(data.tip)
                if(data.result)
                {
                    var dataarr=data.result.split("|")
                    var referarr=data.refer.split("|")
                    var num=dataarr.length/2
                    for(i=0;i<num;) {
                        var line = "<tr>" + "<td align='center'>" + dataarr[i * 2] + "</td>" + "<td align='center'>" + dataarr[i * 2 + 1] + "</td>"+"<td align='center'>" + referarr[i] + "</td>"
                        i++;
                        if (i < num) {
                            line = line + "<td align='center'>" + dataarr[i * 2] + "</td>" + "<td align='center'>" + dataarr[i * 2 + 1] + "</td>" +"<td align='center'>" + referarr[i] + "</td>"+ "<tr>"
                        }
                        else {
                            line = line + "<tr>"
                        }
                        exam.shell.addtabelrow("examReporttable", line, -1)
                    }

                }

            });

        })
    }
    var exam_result_refresh=function (){
        $container.find('#examresultRefresh').click(function () {
            var id=$container.find('#myPatientTable').find("tr").find("td:eq(0)").text()
            var post = {
                ID:id,
                Action:"getResult"
            };
            ajaxPost("/exam/examResult",post,function (data) {
                if(data.result)
                {
                    var dataarr=data.result.split("|")
                    var num=dataarr.length/2
                    for(i=0;i<num;) {
                        var line = "<tr>" + "<td align='center'>" + dataarr[i * 2] + "</td>" + "<td align='center'>" + dataarr[i * 2 + 1] + "</td>"
                        i++;
                        if (i < num) {
                            line = line + "<td align='center'>" + dataarr[i * 2] + "</td>" + "<td align='center'>" + dataarr[i * 2 + 1] + "</td>" + "<tr>"
                        }
                        else {
                            line = line + "<tr>"
                        }
                        exam.shell.addtabelrow("examresultdata", line, -1)
                    }

                }

            });

        })
    }
    var sidebar_function_init = function () {
            $container.find('#functions').find('a').each(function () {
                $(this).click(function() {
                    changeTemplate($(this).text());
                });
                
            });

    }
    var my_patient_refresh=function () {
        $container.find('#refreshMyPatient').click(function () {
            var first=true;
            $container.find("#myPatientTable tr").each(function () {
                if(!first)
                {
                    $(this).remove();

                }
                else
                {
                    first=false;
                }
            })
            var post={
                email : $.cookie('email')
            }
            ajaxPost("/exam/myPatient",post,function (data) {
                if(data.mypatientRefresh) {
                    data.mypatientRefresh.forEach(function (value) {
                        exam.shell.addtabelrow("myPatientTable","<tr>"+"<td>"+value.ID+"</td>"+"<td>"+value.name+"</td>"+"<td>"+value.gender+"</td>"+"<td>"+value.doctorID+"</td>"+"<td>"+value.examitem+"</td>"+"<td>"+value.department+"</td>"+"<td>"+value.time+"</td>"+"<td>"+'<button type="button" class="btn btn-default">选择</button>'+"</td>"+"</tr>",-1);

                    })
                }
                addMySelectTobuttons()

            });

        })

    }
    var addWaitSelectTobuttons=function (){
        $container.find('#patientList').find('button').each(function () {
            $(this).click(function () {
                var id=$(this).parents("tr").find("td:eq(0)").text()
                var post = {
                    email : $.cookie('email'),
                    ID:id
                };
                ajaxPost("/exam/patientState",post,function (data) {
                    if(data.mypatient) {
                        data.mypatient.forEach(function (value) {
                            exam.shell.addtabelrow("myPatientTable","<tr>"+"<td>"+value.ID+"</td>"+"<td>"+value.name+"</td>"+"<td>"+value.gender+"</td>"+"<td>"+value.doctorID+"</td>"+"<td>"+value.examitem+"</td>"+"<td>"+value.department+"</td>"+"<td>"+value.time+"</td>"+"<td>"+'<button type="button" class="btn btn-default">选择</button>'+"</td>"+"</tr>",-1);

                        })
                    }

                });
                $(this).parents("tr").remove();
                addMySelectTobuttons();

            })

        })

    }
    var wait_patient_refresh = function () {
        $container.find('#refreshWaitPatient').click(function() {
            var first=true;
            $container.find("#patientList tr").each(function () {
                if(!first)
                {
                    $(this).remove();

                }
                else
                {
                    first=false;
                }
            })
            var post = {
            };
            ajaxPost("/exam/patientList",post,function (data) {
                if(data.patientlist) {
                    data.patientlist.forEach(function (value) {
                        exam.shell.addtabelrow("patientList","<tr>"+"<td>"+value.ID+"</td>"+"<td>"+value.name+"</td>"+"<td>"+value.gender+"</td>"+"<td>"+value.doctorID+"</td>"+"<td>"+value.examitem+"</td>"+"<td>"+value.department+"</td>"+"<td>"+value.time+"</td>"+"<td>"+'<button type="button" class="btn btn-default">选择</button>'+"</td>"+"</tr>",-1);

                    })
                }
                addWaitSelectTobuttons()

               /* var patient;
               for( patient in data.patientlist)
               {
                    alert(patient);
                    exam.shell.addtabelrow("patientList","<tr>"+"<td>"+patient.ID+"</td>"+"<td>"+patient.name+"</td>"+"<td>"+patient.gender+"</td>"+"<td>"+patient.doctorID+"</td>"+"<td>"+patient.examitem+"</td>"+"<td>"+patient.department+"</td>"+"<td>"+patient.time+"</td>"+"<td>"+patient.operation+"</td>"+"</tr>",-1)
               }*/


            });
        });
    }
    var addMySelectTobuttons=function (){
        $container.find('#myPatientTable').find('button').each(function () {
            $(this).click(function () {
                //插入检查报告需要的信息
                var id=$(this).parents("tr").find("td:eq(0)").text()
                var type=$(this).parents("tr").find("td:eq(4)").text()
                if(type=="血糖测试")
                {
                    var examresult="白细胞|14.69|单核细胞|0.64|红细胞|5.05|嗜酸性粒细胞|0.03|血红蛋白|159.00|嗜碱性粒细胞|0.04|血小板|298.00|红细胞平均体积|87.90|红细胞压积|44.40|平均血红蛋白量|31.50|中性细胞比率|81.00|平均血红蛋白浓度|358.00|淋巴细胞比率|14.10|红细胞分布宽度|37.70|单核细胞比率|4.40|血小板分布宽度|11.40|嗜酸性粒细胞比率|0.20|平均血小板体积|9.90|嗜碱性粒细胞比率|0.30|血小板压积|0.30|中心细胞数|11.91|C-反应蛋白|1.00|淋巴细胞数|2.07";

                }

                var post = {
                    ID:id,
                    Result:examresult,
                    Action:"setResult"
                };
                ajaxPost("/exam/examResult",post,function (data) {
                    if(data.result=="Success")
                    {
                        alert("检查结果已经发布，请前往获取")
                    }

                });
                $(this).parents("tr").remove();


            })

        })

    }


    var changeTemplate=function(data){
        if(data=="待查病人")
        {
            $container.find('#welcome').hide();
            $container.find('#mypatient').hide();
            $container.find('#examresult').hide();
            $container.find('#examreport').hide();
            $container.find('#waitforexam').show();

        }
        else if(data=="我的病人")
        {
            $container.find('#welcome').hide();
            $container.find('#mypatient').show();
            $container.find('#examresult').hide();
            $container.find('#examreport').hide();
            $container.find('#waitforexam').hide();
            addMySelectTobuttons()
        }
        else if(data=="检查结果")
        {
            $container.find('#welcome').hide();
            $container.find('#mypatient').hide();
            $container.find('#examresult').show();
            $container.find('#examreport').hide();
            $container.find('#waitforexam').hide();
        }
        else if(data=="检查报告")
        {
            $container.find('#welcome').hide();
            $container.find('#mypatient').hide();
            $container.find('#examresult').hide();
            $container.find('#examreport').show();
            $container.find('#waitforexam').hide();
        }

    }

    return{
        init : init
    }
})();