function btn_submit(cmd) {
    //取消
    if (cmd == 'reset' && !confirm('{rval MSG106}')) return true;

    //更新
    if (cmd == "update" && document.forms[0].HID_EXAMNO.value.length == 0) {
        return false;
    }
/*
    if (cmd == "update") {
        var errFlg = false;
        var examArr = document.forms[0].HID_EXAMNO.value.split(",");
        for (var i = 0; i < examArr.length; i++) {
            var date = document.getElementById("PROCEDUREDATE_"+examArr[i]).value;
            if (date && !isDate2(date)) {
            console.log("date_err");
                return false;
            }
        }

    }
*/
    //終了
    if (cmd == 'end') {
        if (document.forms[0].TESTDIV.disabled) {
            if (confirm('{rval MSG108}')) {
                closeWin();
            }
            return false;
        }
        closeWin();
    }


    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

//事前チェックエラー
function errorPreCheck(field) {
    alert('{rval MSG305}'+'\n'+field+'が設定されていません。');
}

function Setflg(obj){
    change_flg = true;
    document.forms[0].HID_TESTDIV.value         = document.forms[0].TESTDIV.options[document.forms[0].TESTDIV.selectedIndex].value;
    document.forms[0].TESTDIV.disabled          = true;

    document.forms[0].btn_back.disabled         = true;
    document.forms[0].btn_next.disabled         = true;

    var tmp         = obj.name.split('_');

    //入学予定フラグが立っているときのみ入学キャンセルが設定可。
    if (obj.name.indexOf('ENTRYFLG') >= 0) {
        //入学予定を操作→入学キャンセルを制御
        var entcancelctl = document.getElementById('ENTRYCANCEL_'+tmp[1]);
        if (obj.checked) {
            //入学キャンセルを活性化する。
            entcancelctl.disabled = false;
        } else {
            //入学キャンセルを非活性に。
            entcancelctl.disabled = true;
        }
    }
}
