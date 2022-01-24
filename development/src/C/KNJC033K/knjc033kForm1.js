function btn_submit(cmd) {
    if (cmd == 'subclasscd' && 0 < document.forms[0].COUNTER.value) {
        if (!confirm('{rval MSG108}')) {
            var cnt = document.forms[0]["LIST_SUBCLASSCD" + document.forms[0].SELECT_SUBCLASSCD.value].value;
            document.forms[0].SUBCLASSCD[cnt].selected = true;
            return false;
        }
    }
    if (cmd == 'chaircd' && 0 < document.forms[0].COUNTER.value) {
        if (!confirm('{rval MSG108}')) {
            var cnt = document.forms[0]["LIST_CHAIRCD" + document.forms[0].SELECT_CHAIRCD.value].value;
            document.forms[0].CHAIRCD[cnt].selected = true;
            return false;
        }
    }
    if (cmd == 'change' && 0 < document.forms[0].COUNTER.value) {
        if (!confirm('{rval MSG108}')) {
            var cnt = document.forms[0]["LIST_MONTH" + document.forms[0].SELECT_MONTH.value].value;
            document.forms[0].MONTHCD[cnt].selected = true;
            return false;
        }
    }
    if (cmd == 'kekka_syubetu' && 0 < document.forms[0].COUNTER.value) {
        if (!confirm('{rval MSG108}')) {
            var cnt = document.forms[0]["LIST_SICK" + document.forms[0].SELECT_SICK.value].value;
            document.forms[0].SICK[cnt].selected = true;
            return false;
        }
    }

    if (cmd == 'reset' && !confirm('{rval MSG106}')){
        return;
    }

    if (cmd == 'update') {
        if (document.forms[0].MONTHCD.value == "") {
            alert('対象月を選択してください。');
            return;
        }

        //データを格納
        if (document.forms[0].use_school_detail_gcm_dat.value == "1") document.forms[0].HIDDEN_COURSE_MAJOR.value = document.forms[0].COURSE_MAJOR.value;
        document.forms[0].HIDDEN_SUBCLASSCD.value   = document.forms[0].SUBCLASSCD.value;
        document.forms[0].HIDDEN_CHAIRCD.value      = document.forms[0].CHAIRCD.value;
        document.forms[0].HIDDEN_MONTHCD.value      = document.forms[0].MONTHCD.value;
        document.forms[0].HIDDEN_SICK.value         = document.forms[0].SICK.value;
        document.forms[0].HIDDEN_LESSON.value       = document.forms[0].LESSON_SET.value;

        //使用不可項目
        if (document.forms[0].use_school_detail_gcm_dat.value == "1") document.forms[0].COURSE_MAJOR.disabled = true;
        document.forms[0].SUBCLASSCD.disabled = true;
        document.forms[0].CHAIRCD.disabled = true;
        document.forms[0].MONTHCD.disabled = true;
        document.forms[0].SICK.disabled = true;
        document.forms[0].LESSON_SET.disabled = true;
        document.forms[0].btn_udpate.disabled = true;
        document.forms[0].btn_reset.disabled = true;
        document.forms[0].btn_end.disabled = true;
        document.forms[0].btn_print.disabled = true;
        document.forms[0].btn_csv.disabled = true;
    }
    //フレームロック機能（プロパティの値が1の時有効）
    if (document.forms[0].useFrameLock.value == "1") {
        if (cmd == 'update') {
            updateFrameLock();
        }
    }
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return;
}

/**
*   tabキー、又はエンターキーを押したとき、
*   下のテキストボックスへフォーカスさせる、
*   ちなみに、return false でtabキーそのものを"無効化できなかった"ので、
*   ちょっとダサい書き方になってます。
*/
function checkkey(obj) {
    if (event.keyCode == 9) {
    //tabキーの時の処理
        var table_obj   = obj.parentNode.parentNode.parentNode;
        var rowIndex_no = obj.parentNode.parentNode.rowIndex + 1;
        if (table_obj.rows.length <= rowIndex_no) {
            rowIndex_no = 1;
        }
        var targetObj = table_obj.rows[rowIndex_no].cells[0].firstChild;
        targetObj.focus();
    } else if (event.keyCode == 13) {
    //エンターキーの時の処理
        var cellIndex_no = obj.parentNode.cellIndex;
        var table_obj   = obj.parentNode.parentNode.parentNode;
        var rowIndex_no = obj.parentNode.parentNode.rowIndex + 1;
        if (table_obj.rows.length <= rowIndex_no) {
            rowIndex_no = 1;
        }
        var targetObj = table_obj.rows[rowIndex_no].cells[cellIndex_no].firstChild;
        targetObj.focus();
    }

    return false;
}

function newwin(SERVLET_URL) {
    if (document.forms[0].SICK.value == '') {
        alert('{rval MSG916}');
        return false;
    }
    if (document.forms[0].SUBCLASSCD.value == '') {
        alert('{rval MSG916}');
        return false;
    }
    if (document.forms[0].CHAIRCD.value == '') {
        alert('{rval MSG916}');
        return false;
    }

    action = document.forms[0].action;
    target = document.forms[0].target;

//    url = location.hostname;
//    document.forms[0].action = "http://" + url +"/cgi-bin/printenv.pl";
    document.forms[0].action = SERVLET_URL +"/KNJC";
    document.forms[0].target = "_blank";
    document.forms[0].submit();

    document.forms[0].action = action;
    document.forms[0].target = target;
}

function Page_jumper(link)
{
    if (document.forms[0].SUBCLASSCD.value == "" || document.forms[0].CHAIRCD.value == "" || document.forms[0].MONTHCD.value == "") {
        alert('一括更新をするには科目/講座/対象月を選択してください。');
        return;
    }
    if (!confirm('{rval MSG108}')) {
        return;
    }
    parent.location.href=link;
}

function openWindow(name, value)
{
    var str_name  = name.split(",");
    var str_value = value.split(",");
    var string = "";
    var sep = "";

    for(var i = 0; i < str_name.length; i++)
    {
        string = string + sep + str_name[i] + "=" + str_value[i];
        sep = "&";
    }

    loadwindow('knjc033kindex.php?' + string + '',0,0,350,350);
}

/************************************************* 貼付け関係 ***********************************************/
function showPaste(obj) {
    if (!confirm('内容を貼付けますか？')) {
        return false;
    }

    //テキストボックスの名前の配列を作る
    var nameArray = new Array("ABSENT",
                              "SUSPEND",
                              "MOURNING",
                              "SICK",
                              "NOTICE",
                              "NONOTICE",
                              "LATE",
                              "EARLY"
                              );

    var hairetuSu = document.forms[0].objCntSub.value;
    var renArray = new Array();
    for (var i = 0; i < hairetuSu; i++) {
        renArray[i] = (i + 1) > 9 ? (i + 1) : "0" + (i + 1);
    }

    insertTsv({"clickedObj"      :obj,
               "harituke_type"   :"renban_hairetu",
               "objectNameArray" :nameArray,
               "hairetuCnt" :hairetuSu,
               "renbanArray" : renArray
               });
    //これを実行しないと貼付けそのものが実行されてしまう
    return false;
}

/****************************************/
/* 実際に貼付けを実行する関数           */
/* 貼付け時に必要な処理(自動計算とか)は */
/* ここに書きます。                     */
/****************************************/
function execCopy(targetObject, val, targetNumber) {
    targetObject.value = val;
}

/***********************************/
/* クリップボードの中身のチェック  */
/* (だめなデータならばfalseを返す) */
/* (共通関数から呼ばれる)          */
/***********************************/
function checkClip(clipTextArray, harituke_jouhou) {
    var startFlg = false;
    var i;
    var targetName   = harituke_jouhou.clickedObj.name.split("-")[0];
    var targetNumber = harituke_jouhou.clickedObj.name.split("-")[1];
    var objectNameArray = harituke_jouhou.objectNameArray;
    for (j = 0; j < clipTextArray.length; j++) { //クリップボードの各行をループ
        i = 0;
        startFlg = false;
        for (k = 0; k < objectNameArray.length; k++) { //テキストボックス名でまわす
            if (objectNameArray[k] == targetName) { //貼付け開始対象のテキストボックスならばフラグを立てる
                startFlg = true;
            }
            if (startFlg) {
                if (clipTextArray[j][i] != undefined) {

                    //数字であるのかチェック
                    if (isNaN(clipTextArray[j][i])){
                        alert('{rval MSG907}');
                        return false;
                    }

                }
                i++;
            }
        }
    }
    return true;
}

