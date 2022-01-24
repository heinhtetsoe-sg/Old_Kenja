function moveLeft(callFrom) {
    var rightList = document.forms[0].STUDENTS_LIST;
    var leftList = document.forms[0].STUDENTS_SELECTED;

    var moveAll = true;
    if(callFrom.name.indexOf("_all") < 0) {
        moveAll = false;
    }

    for(var idx =0; leftList != null && rightList != null && idx < rightList.childNodes.length;) {
        if(moveAll || rightList.childNodes[idx].selected) {
            leftList.appendChild(rightList.childNodes[idx].cloneNode(true));
            rightList.removeChild(rightList.childNodes[idx]);
            continue;
        }
        idx++;
    }
    //学籍番号順に並び替え
    sortList(leftList);
}

function moveRight(callFrom) {
    var rightList = document.forms[0].STUDENTS_LIST;
    var leftList = document.forms[0].STUDENTS_SELECTED;

    var moveAll = true;
    if(callFrom.name.indexOf("_all") < 0) {
        moveAll = false;
    }

    for(var idx =0; rightList != null && leftList != null && idx < leftList.childNodes.length;) {
        if(moveAll || leftList.childNodes[idx].selected) {
            rightList.appendChild(leftList.childNodes[idx].cloneNode(true));
            leftList.removeChild(leftList.childNodes[idx]);
            continue;
        }
        idx++;
    }

    sortList(rightList);
}

function sortList(list) {
    var items = [];
    for(var idx =0; idx < list.childNodes.length; idx++) {
        items.push(list.childNodes[idx]);
    }
    //並び替え
    items.sort(function(dest, src) {
        return dest.value.localeCompare(src.value);
    });
    //項目差し替え
    ////リスト項目を全消去
    while(list.firstChild) {
        list.removeChild(list.firstChild);
    }
    ////並び替え終えた項目を反映
    while(0 < items.length) {
        list.appendChild(items[0]);
        items.splice(0, 1);
    }
}

function btn_submit(cmd) {
    document.forms[0].cmd.value = cmd;
    document.forms[0].target = "_self";
    document.forms[0].action = "knjd187rindex.php";
    document.forms[0].submit();
    return false;
}

//印刷
function newwin(SERVLET_URL) {
    if(document.forms[0].STUDENTS_SELECTED.options.length < 1) {
        alert('{rval MSG310}' + "\n\n( 出力対象一覧 )");
        return false;
    }
    var endDate = document.forms[0].EDATE.value;
    if(endDate == null || endDate.length < 10) {
        alert('{rval MSG310}' + "\n\n( 出欠集計日付To )");
        return false;
    }
    var objEndDate = new Date(endDate);

    var limitDt = document.forms[0].HID_LIMIT_DATE.value;
    if(limitDt != null) {
        var objLimitDt = new Date(limitDt);
        if((objLimitDt - objEndDate) < 0) {
            alert('{rval MSG914}' + "\n\n( " + limitDt + " ＜ 出欠集計日付To )");
            return false;
        }
    }
    limitDt = document.forms[0].SDATE.value;
    if(limitDt != null) {
        var objLimitDt = new Date(limitDt);
        if((objEndDate - objLimitDt) < 0) {
            alert('{rval MSG915}' + "\n\n( 出欠集計日付To ＜ " + limitDt + " )");
            return false;
        }
    }

    for(var idx =0; idx < document.forms[0].STUDENTS_SELECTED.options.length; idx++) {
        document.forms[0].STUDENTS_SELECTED.options[idx].selected = true;
    }

    document.forms[0].target = "_blank";
    //document.forms[0].action = "http://" + location.hostname +"/cgi-bin/printenv.pl";
    document.forms[0].action = SERVLET_URL + "/KNJL";
    document.forms[0].submit();
}