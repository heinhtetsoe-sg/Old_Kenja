function btn_submit(cmd) {
    if (cmd=="clear") {
        result = confirm('{rval MSG106}');
        if (result == false) {
            return false;
        }
    }
    if (cmd == "update") {
        var dataFlg = false;
        for (var i = 0; i < document.forms[0].length; i++) {
            if (document.forms[0][i].name.match(/SCORE/)) {
                dataFlg = true;
                if (document.forms[0][i].value == "") {
                    alert('{rval MSG301}');
                    return false;
                }
            }
        }
        if (!dataFlg) {
            alert('更新データがありません。');
            return false;
        }
    }

    if (cmd=="delete") {
        result = confirm('{rval MSG103}');
        if (result == false) {
            return false;
        }
    }

    if (cmd=="copy") {
        if (document.forms[0].CHECK_YEAR_CNT.value > 0) {
            alert('今年度データが存在します。');
            return false;
        }
        if (document.forms[0].CHECK_LASTYEAR_CNT.value == 0) {
            alert('前年度データが存在しません。');
            return false;
        }
        result = confirm('前年度からコピーします。宜しいでしょうか？');
        if (result == false) {
            return false;
        } 
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function closing_window(){
        alert('{rval MSG300}');
        closeWin();
        return true;
}
