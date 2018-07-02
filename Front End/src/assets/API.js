function getAll() {
    var temp;
    $.ajax({
        type: 'get',
        async: false,
        crossDomain: true,
        url: 'http://localhost:8088/bbs/api/getAll',
        success: function (res) {
            temp = res
            console.log(res)
        },
        error: function (res) {
            console.log('Something went wrong!')
        }
    })
    return temp;
}

function addOne(inputData) {
    var temp;
    var a = JSON.stringify({"label":inputData.label, "id":100, "name":inputData.name, "type":inputData.type, "layer":inputData.layer,
        "performance":inputData.performance, "relations":inputData.relations});
    console.log(a);
    $.ajax({
        type: 'post',
        async: false,
        crossDomain: true,
        url: 'http://localhost:8088/bbs/api/addOneNode',
        beforeSend: function (xhr) {
            xhr.setRequestHeader('Content-type', 'application/json')
        },
        dataType: "json",
        data: a,
        success: function (res) {
            temp = res
            console.log(res)
        },
        error: function (res) {
            console.log('A Something wrong!')
        }
    })
    return temp;
}

function getLabel(label) {
    var temp;
    $.ajax({
        type: 'post',
        async: false,
        crossDomain: true,
        url: 'http://localhost:8088/bbs/api/getLabel',
        beforeSend: function (xhr) {
            xhr.setRequestHeader('Content-type', 'application/x-www-form-urlencoded')
        },
        data: {label:label},
        success: function (res) {
            temp = res
            console.log(res)
        },
        error: function (res) {
            console.log('A Something wrong!')
        }
    })
    return temp;
}



export {getAll, addOne, getLabel}