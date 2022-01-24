function moveLeft(callFrom) {
    var rightList = document.forms[0].CLSS_OR_STDNTS_LIST;
    var leftList = document.forms[0].CLSS_OR_STDNTS_SELECTED;

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
    //VALUE値に沿って並び替え
    sortList(leftList);
}

function moveRight(callFrom) {
    var rightList = document.forms[0].CLSS_OR_STDNTS_LIST;
    var leftList = document.forms[0].CLSS_OR_STDNTS_SELECTED;

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
    //VALUE値に沿って並び替え
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
    document.forms[0].submit();
    return false;
}

//印刷
//inpChk: 入力チェックフラグ
function newwin(SERVLET_URL, inpChk) {
    if(inpChk) {
        if (document.forms[0].CLSS_OR_STDNTS_SELECTED.options.length < 1) {
            alert('{rval MSG310}' + "\n\n( 出力対象一覧 )");
            return;
        }
    
        for (var i = 0; i < document.forms[0].CLSS_OR_STDNTS_LIST.options.length; i++) {
            document.forms[0].CLSS_OR_STDNTS_LIST.options[i].selected = false;
        }
        for (var i = 0; i < document.forms[0].CLSS_OR_STDNTS_SELECTED.options.length; i++) {
            document.forms[0].CLSS_OR_STDNTS_SELECTED.options[i].selected = true;
        }
    }

    //変更前の値を退避
    action = document.forms[0].action;
    target = document.forms[0].target;

    //document.forms[0].action = "http://" + location.hostname +"/cgi-bin/printenv.pl";
    document.forms[0].action = SERVLET_URL +"/KNJD";
    document.forms[0].target = "_blank";
    document.forms[0].submit();

    //元の値に戻す
    document.forms[0].action = action;
    document.forms[0].target = target;
}
