function btn_submit(cmd) {

    if (cmd == 'reset'){
        if (!confirm('{rval MSG106}'))
            return false;
    }

    if (cmd == 'delete'){
        if (!confirm('{rval MSG103}'))
            return false;
    }

    if (cmd == "insert" || cmd == "update" || cmd == "delete") {
        //必須チェック
        if (document.forms[0].TOUROKU_DATE.value == ""){
            alert('{rval MSG304}'+'\n　　（ 登録日付 ）');

            return true;
        }
        if (document.forms[0].EVENT_CLASS_CD.value == ""){
            alert('{rval MSG304}'+'\n　　（ 分類 ）');
            return true;
        }
        if (document.forms[0].EVENT_CD.value == ""){
            alert('{rval MSG304}'+'\n　　（ イベント ）');
            return true;
        }
        if (document.forms[0].MEDIA_CD.value == ""){
            alert('{rval MSG304}'+'\n　　　（ 媒体 ）');
            return true;
        }
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

//戻る
function closeMethod() {
    window.opener.btn_submit('ret411');
    closeWin();
}

//権限チェック
function OnAuthError() {
    alert('{rval MSG300}');
    window.opener.btn_submit('ret411');
    closeWin();
}
