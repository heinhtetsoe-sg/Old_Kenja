function btn_submit(cmd)
{
    if (cmd == 'reset'){
        if(document.forms[0].DATE.value == "" || document.forms[0].GRADE_HR_CLASS.value == "" ){
            confirm('{rval MSG304}');
            return;
        }else{
           if(!confirm('{rval MSG106}')){
            return;
           }
        }
    }

    if (cmd == 'update' || cmd == 'updateHrAte' || cmd == 'cancelHrAte') {
        if (document.forms[0].DATE.value == "") {
            alert('{rval MSG304}' + '\n【日付】');
            return;
        }
        if (document.forms[0].GRADE_HR_CLASS.value == "") {
            alert('{rval MSG304}' + '\n【学級】');
            return;
        }
        //データを格納
        document.forms[0].HIDDEN_DATE.value             = document.forms[0].DATE.value;
        if (document.forms[0].useSpecial_Support_Hrclass.value == "1" || document.forms[0].useFi_Hrclass.value == "1") {
            if (document.forms[0].HR_CLASS_TYPE[0].checked == true) document.forms[0].HIDDEN_HR_CLASS_TYPE.value    = document.forms[0].HR_CLASS_TYPE[0].value;
            if (document.forms[0].HR_CLASS_TYPE[1].checked == true) document.forms[0].HIDDEN_HR_CLASS_TYPE.value    = document.forms[0].HR_CLASS_TYPE[1].value;
        }
        document.forms[0].HIDDEN_GRADE_HR_CLASS.value   = document.forms[0].GRADE_HR_CLASS.value;

        //使用不可項目
        document.forms[0].DATE.disabled = true;
        if (document.forms[0].useSpecial_Support_Hrclass.value == "1" || document.forms[0].useFi_Hrclass.value == "1") {
            document.forms[0].HR_CLASS_TYPE[0].disabled = true;
            document.forms[0].HR_CLASS_TYPE[1].disabled = true;
        }
        document.forms[0].GRADE_HR_CLASS.disabled = true;
        document.forms[0].btn_update.disabled = true;
        document.forms[0].btn_reset.disabled = true;
        document.forms[0].btn_end.disabled = true;

        //リンクを使用不可
        var elem = document.getElementsByTagName("a");
        for(var i = 0; i < elem.length; ++i){
            elem[i].onclick = "return false;";
        }
    }

    //フレームロック機能（プロパティの値が1の時有効）
    if (document.forms[0].useFrameLock.value == "1") {
        if (cmd == 'update' || cmd == 'updateHrAte' || cmd == 'cancelHrAte') {
            updateFrameLock();
        }
    }
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

//終了時のメッセージ
function closeMsgFunc(msg) {
    var setMsg = "処理を終了します。\n\n【欠席の生徒】\n" + msg;
    if (confirm(setMsg)) {
        closeWin();
    }
}

//子画面へ
function openSubWindow(URL) {
    if (document.forms[0].GRADE_HR_CLASS.value == '') {
        alert('{rval MSG916}');
        return false;
    }
    if (document.forms[0].DATE.value == '') {
        alert('{rval MSG916}');
        return false;
    }

    wopen(URL, 'SUBWIN2', 0, 0, screen.availWidth, screen.availHeight);
}

//チェック制御
function check_change(obj, att) {

    var org = obj.name.split("_");
    for (var i=0; i < document.forms[0].elements.length; i++) {
        re = new RegExp("^SELECTDATA_" + org[1]);
        var obj_updElement = document.forms[0].elements[i];
        if (obj_updElement.name.match(re)) {

            if (att) {
                obj_updElement.checked = false;
            } else if(obj.name == obj_updElement.name) {
                obj_updElement.checked = obj.checked;
            } else {
                var elm = obj_updElement.name.split("_");
                if((org[2] == "LATE" || org[2] == "EARLY") && (elm[2] == "LATE" || elm[2] == "EARLY")) {
                    continue;
                } else {
                    obj_updElement.checked = false;
                }
            }
        }
    }
}
