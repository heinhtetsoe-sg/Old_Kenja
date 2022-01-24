function moveLeft(callFrom) {
    var rightlist = document.forms[0].CLASS_LIST;
    var leftList = document.forms[0].CLASS_SELECTED;

    var moveAll = true;
    if(callFrom.name.indexOf("_all") < 0) {
        moveAll = false;
    }

    for(var idx =0; leftList != null && rightlist != null && idx < rightlist.childNodes.length;) {
        if(moveAll || rightlist.childNodes[idx].selected) {
            leftList.appendChild(rightlist.childNodes[idx].cloneNode(true));
            rightlist.removeChild(rightlist.childNodes[idx]);
            idx = 0;
            continue;
        }
        idx++;
    }

    sortList(leftList);
}

function moveRight(callFrom) {
    var rightlist = document.forms[0].CLASS_LIST;
    var leftList = document.forms[0].CLASS_SELECTED;

    var moveAll = true;
    if(callFrom.name.indexOf("_all") < 0) {
        moveAll = false;
    }

    for(var idx =0; leftList != null && rightlist != null && idx < leftList.childNodes.length;) {
        if(moveAll || leftList.childNodes[idx].selected) {
            rightlist.appendChild(leftList.childNodes[idx].cloneNode(true));
            leftList.removeChild(leftList.childNodes[idx]);
            idx = 0;
            continue;
        }
        idx++;
    }

    sortList(rightlist);
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
    ////全項目を削除
    while(list.firstChild) {
        list.removeChild(list.firstChild);   
    }
    ////ソート済み項目をマージ
    while(0 < items.length) {
        list.appendChild(items[0]);
        items.splice(0, 1);
    }
}

function btn_submit(cmd, handler) {
    document.forms[0].cmd.value = cmd;
    document.forms[0].HID_EVENT_FROM.value = handler.name;
    document.forms[0].target = "_self";
    document.forms[0].action = "knjd627dindex.php";
    document.forms[0].submit();
    return false;
}

//印刷
function newwin(SERVLET_URL) {
    if(document.forms[0].BORDER_SCORE.value.length < 1) {
        alert('{rval MSG301}' + "\n\n( 再試験基準 )");
        document.forms[0].BORDER_SCORE.focus();
        return false;
    }
    
    if(document.forms[0].CLASS_SELECTED.options.length < 1) {
        alert('{rval MSG310}' + "\n\n( 出力対象一覧 )");
        return false;
    }

    for(var idx =0; idx < document.forms[0].CLASS_SELECTED.options.length; idx++) {
        document.forms[0].CLASS_SELECTED.options[idx].selected = true;
    }

    document.forms[0].target = "_blank";
    
    //document.forms[0].action = "http://" + location.hostname +"/cgi-bin/printenv.pl";
    document.forms[0].action = SERVLET_URL + "/KNJL";
    document.forms[0].submit();
}