function btn_submit(cmd) {
    var i;
    if (cmd == "update") {
        if (document.forms[0].STR_DATE.value > document.forms[0].END_DATE.value) {
            alert("日付範囲が不正です。");
            return;
        }

        //日付の年度内チェック
        var str_date = document.forms[0].STR_DATE.value;
        var end_date = document.forms[0].END_DATE.value;
        var chk_sdate = document.forms[0].CHK_SDATE.value;
        var chk_edate = document.forms[0].CHK_EDATE.value;

        if((str_date < chk_sdate) || (end_date > chk_edate)){
            alert("日付が範囲外です。\n（" + chk_sdate + "～" + chk_edate + "） ");
            return;
        }

        var flag = "";
        for (i=0; i < document.forms[0].elements.length; i++) {
            if (document.forms[0].elements[i].name == "PRINT_CHECK[]"
                   && document.forms[0].elements[i].checked) {
                flag = "on";
            }
        }
        if (flag == "") {
            alert("チェックボックスが選択されておりません。");
            return;
        }
    }

    if (cmd == "read2" || cmd == "read") {
        select_data = document.forms[0].SELECT_DATA;
        select_data.value = "";
        sep = "";
        for (i = 0; i < document.forms[0].CATEGORY_NAME.length; i++) {
            document.forms[0].CATEGORY_NAME.options[i].selected = 0;
        }

        for (i = 0; i < document.forms[0].CATEGORY_SELECTED.length; i++) {
            document.forms[0].CATEGORY_SELECTED.options[i].selected = 1;
            select_data.value = select_data.value + sep + document.forms[0].CATEGORY_SELECTED.options[i].value;
            sep = ",";
        }
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function check_all(obj) {
    var i;
    for (i=0; i < document.forms[0].elements.length; i++) {
        if (document.forms[0].elements[i].name == "PRINT_CHECK[]") {
            document.forms[0].elements[i].checked = obj.checked;
        }
    }
}

function ClearList(OptionList) {
    OptionList.length = 0;
}

function AllClearList(OptionList, TitleName) {
        attribute = document.forms[0].CATEGORY_NAME;
        ClearList(attribute);
        attribute = document.forms[0].CATEGORY_SELECTED;
        ClearList(attribute);
}

//印刷
function newwin(SERVLET_URL){

    action = document.forms[0].action;
    target = document.forms[0].target;

    //url = location.hostname;

    //document.forms[0].action = "http://" + url +"/cgi-bin/printenv.pl";
    document.forms[0].action = SERVLET_URL +"/KNJG";
    document.forms[0].target = "_blank";
    document.forms[0].submit();

    document.forms[0].action = action;
    document.forms[0].target = target;
}

function move1(side) {
    var temp1 = [];
    var temp2 = [];
    var i;
    var o;
    
    if (side == "left") {
        attribute1 = document.forms[0].CATEGORY_NAME;
        attribute2 = document.forms[0].CATEGORY_SELECTED;
    } else {
        attribute1 = document.forms[0].CATEGORY_SELECTED;
        attribute2 = document.forms[0].CATEGORY_NAME;  
    }

    for (i = 0; i < attribute2.length; i++) {
        o = attribute2.options[i];
        temp1.push({ value: o.value, text: o.text});
    }

    for (i = 0; i < attribute1.length; i++) {
        o = attribute1.options[i];
        if (o.selected) {
            temp1.push({ value: o.value, text: o.text});
        } else {
            temp2.push({ value: o.value, text: o.text});
        }
    }

    temp1.sort(attendnoCompare);

    for (i = 0; i < temp1.length; i++) {
        attribute2.options[i] = new Option();
        attribute2.options[i].value = temp1[i].value;
        attribute2.options[i].text =  temp1[i].text;
    }

    ClearList(attribute1);
    if (temp2.length > 0) {
        for (i = 0; i < temp2.length; i++) {
            attribute1.options[i] = new Option();
            attribute1.options[i].value = temp2[i].value;
            attribute1.options[i].text =  temp2[i].text;
        }
    }
}

function attendnoCompare(o1, o2) {
    var schregno1 = o1.value;
    var schregno2 = o2.value;
    if (compareInfo) {
        var cmp1 = compareInfo[schregno1];
        var cmp2 = compareInfo[schregno2];
        if (cmp1 < cmp2) {
            return -1;
        } else if (cmp1 > cmp2) {
            return 1;
        }
    }
    if (schregno1 < schregno2) {
        return -1;
    } else if (schregno1 > schregno2) {
        return 1;
    }
    return 0;
}

function moves(sides) {
    var temp = [];
    var o;
    var i;
    
    if (sides == "left") {
        attribute5 = document.forms[0].CATEGORY_NAME;
        attribute6 = document.forms[0].CATEGORY_SELECTED;
    } else {
        attribute5 = document.forms[0].CATEGORY_SELECTED;
        attribute6 = document.forms[0].CATEGORY_NAME;  
    }

    for (i = 0; i < attribute6.length; i++) {
        o = attribute6.options[i];
        temp.push({ value: o.value, text: o.text});
    }

    for (i = 0; i < attribute5.length; i++) {
        o = attribute5.options[i];
        temp.push({ value: o.value, text: o.text});
    }

    temp.sort(attendnoCompare);

    for (i = 0; i < temp.length; i++) {
        attribute6.options[i] = new Option();
        attribute6.options[i].value = temp[i].value;
        attribute6.options[i].text =  temp[i].text;
    }

    ClearList(attribute5);

}

/************************************************* 貼付け関係 ***********************************************/
function showPaste(obj, cnt) {
    if (!confirm('内容を貼付けますか？')) {
        return false;
    }

    //テキストボックスの名前の配列を作る
    var nameArray = new Array("STATION_FROM[]",
                              "STATION_TO[]",
                              "STATION_VIA[]");

    if (document.forms[0].objCntSub.value > 1) {
        insertTsv({"clickedObj"      :obj,
                   "harituke_type"   :"hairetu",
                   "objectNameArray" :nameArray,
                   "hairetuCnt"      :cnt
                   });
    } else {
        insertTsv({"clickedObj"      :obj,
                   "harituke_type"   :"kotei",
                   "objectNameArray" :nameArray
                   });
    }

    //これを実行しないと貼付けそのものが実行されてしまう
    return false;
}

/****************************************/
/* 実際に貼付けを実行する関数           */
/* 貼付け時に必要な処理(自動計算とか)は */
/* ここに書きます。                     */
/****************************************/
function execCopy(targetObject, val, objCnt) {
    targetObject.value = val;
};


/***********************************/
/* クリップボードの中身のチェック  */
/* (だめなデータならばfalseを返す) */
/* (共通関数から呼ばれる)          */
/***********************************/
function checkClip(clipTextArray, harituke_jouhou) {
    var startFlg = false;
    var retuCnt;
    var objectNameArray = harituke_jouhou.objectNameArray;
    var objCnt = harituke_jouhou.hairetuCnt;

    for (var gyouCnt = 0; gyouCnt < clipTextArray.length; gyouCnt++) { //クリップボードの各行をループ
        retuCnt = 0;
        startFlg = false;

        for (k = 0; k < objectNameArray.length; k++) { //テキストボックス名でまわす
            if (objectNameArray[k] == harituke_jouhou.clickedObj.name) { //貼付け開始対象のテキストボックスならばフラグを立てる
                startFlg = true;
            }
            if (startFlg) {
                //クリップボードのデータでタブ区切りの最後を越えるとundefinedになる
                if (clipTextArray[gyouCnt][retuCnt] != undefined) { //対象となるデータがあれば

                    if (document.forms[0].objCntSub.value > 1) {
                        targetObject = eval("document.forms[0][\"" + objectNameArray[k] + "\"][" + objCnt + "]");
                    } else {
                        targetObject = eval("document.forms[0][\"" + objectNameArray[k] + "\"]");
                    }
                    if (targetObject) { //テキストボックスがあれば(テキストボックスはあったりなかったりする)
                        if (clipTextArray[gyouCnt][retuCnt].length > 15){
                            alert('{rval MSG915}');
                            return false;
                        }
                    }
                }
                retuCnt++;
            }
        }
        objCnt++;
    }
    return true;
}

