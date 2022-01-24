function btn_submit(cmd) {
    //削除確認
    if (cmd == 'delete'){
        if (!confirm('{rval MSG103}'))
            return false;
    }

    //取消確認
    if (cmd == 'reset'){
        if (!confirm('{rval MSG106}'))
            return false;
    }

    //必須入力チェック
    if (cmd == 'update' || cmd == 'add') {
        if (document.forms[0].CODE.value == '') {
            alert('{rval MSG301}\n　　　　　　　(　コード　)');
            return false;
        }
        if (document.forms[0].NAME1.value == '') {
            alert('{rval MSG301}\n　　　　　　　(　名称　)');
            return false;
        }
    } else if (cmd == 'delete') {
        if (document.forms[0].CODE.value == '') {
            alert('{rval MSG301}\n　　　　　　　(　コード　)');
            return false;
        }
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function paddingZero(val) {
    //数字チェック
    if (isNaN(val)) {
        return val;
    }
    return ('0'+val).substr(val.length-1,2);
}
function chgcmbchk(obj) {
    //選択状態をチェック
    var matubival = obj.id.slice(-2);
    console.log("matubival:" + matubival);
    if (obj.value == "3") {
        //選択数を無効化
        document.getElementById("ANSWER_SELECT_COUNT"+matubival).disabled=true;
    } else {
        //選択数を有効化
        document.getElementById("ANSWER_SELECT_COUNT"+matubival).disabled=false;
    }
}
