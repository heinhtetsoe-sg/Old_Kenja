function btn_submit(cmd) {
    if (cmd == 'insert' || cmd == 'update' || cmd == 'delete') {
        if (document.forms[0].SEQ.value == ""){
            alert('{rval MSG304}' + '\n(SEQ)');
            return false;
        }
    }
    if (cmd == 'insert' || cmd == 'update') {
        var total = 0;
        for (var i = 0; i < document.forms[0].elements.length; i++) {
            var e = document.forms[0].elements[i];
            if (e.type == "text" && e.name == "WEIGHTING[]"){
                var val = e.value;
                if (val != "") {
                    total += parseFloat(val);
                }
            }
        }
        //重みチェック（文京）
        if (document.forms[0].BUNKYO.value > 0) {
            if (total > 100.0) {
                alert('{rval MSG913}' + '\n( 重みの合計は100.0まで )');
                return false;
            }
        }
    }
    if (cmd == 'delete') {
        if (!confirm('{rval MSG103}'))
            return false;
    }
    if (cmd == 'reset') {
        if (!confirm('{rval MSG106}')){
            return false;
        }
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function calcProperties(obj) {

    //数字チェック
    if (isNaN(obj.value)) {
        alert('{rval MSG907}');
        obj.value = "";
        obj.focus();
        return;
    } else if (obj.value > 100) {
        alert('{rval MSG914}');
        obj.value = "";
        obj.focus();
        return;
    } else {
        s = obj.value.indexOf(".");
        d = obj.value.split(".");
        if (s >= 0 && d[1].length != 1) {
            alert('{rval MSG916}\n（小数点第1位まで）');
            obj.value = "";
            obj.focus();
            return;
        }
    }
}
