let ajaxPost = function (url,result,success) {
    $.ajax({
        headers: {
            'Accept': 'application/json',
            'Content-Type': 'application/json'
        },
        dataType : "json",
        type : 'POST',
        url : url,
        data : JSON.stringify(result),
        success : success
    });
};

let appendTable = function (tableId,tr_em,...$tds) {
    let $tr_em = tr_em.clone();
    $tr_em.find('td').each(function (index,val) {
        $(this).text($tds[index]);
    });
    $('#'+tableId).find('tbody').append($tr_em);
};

let appendTableByTable = function ($table,tr_em,...$tds) {
    let $tr_em = tr_em.clone();
    $tr_em.find('td').each(function (index,val) {
        $(this).text($tds[index]);
    });
    $table.find('tbody').append($tr_em);
};

let appendTableWithColNum = function(tableId,tr_em,max,...$tds){
    let $tr_em = tr_em.clone();
    let $tcs = $tr_em.find('td');
    for(let i=0 ;i<max && i<$tcs.length ;++i){
        $tcs.eq(i).text($tds[i]);
    }
    $('#'+tableId).find('tbody').append($tr_em);
};

let appendTableStartWithIndex = function (tableId,tr_em,min,...$tds) {
    let $tr_em = tr_em.clone();
    let $tcs = $tr_em.find('td');
    for(let i=min ;i<$tcs.length ;++i){
        $tcs.eq(i).text($tds[i]);
    }
    $('#'+tableId).find('tbody').append($tr_em);
};

let appendTableWithBody = function ($tbody,tr_em,...$tds) {
    let $tr_em = tr_em.clone();
    $tr_em.find('td').each(function (index,val) {
        $(this).text($tds[index]);
    });
    $tbody.append($tr_em);
};

let appendTableWithFunctionAndColumnNum = function(tableId,tr_em,max,callback,...$tds){
    let $tr_em = tr_em.clone();
    let $tcs = $tr_em.find('td');
    for(let i=0 ;i<max && i<$tcs.length ;++i){
        $tcs.eq(i).text($tds[i]);
    }
    $tr_em.find('button').click(callback);
    $('#'+tableId).find('tbody').append($tr_em);
};