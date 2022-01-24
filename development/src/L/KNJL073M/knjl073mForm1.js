function btn_submit(cmd) {
    if (cmd == 'reset') {
        if (!confirm('{rval MSG106}')) return false;
    }
    if (cmd == 'update') {
        var check_array = new Array();
        for (var i = 0; i < document.forms[0].length; i++) {
            if (document.forms[0][i].name.match(/^SUB_ORDER_/)) {
                if (document.forms[0][i].value != "") {
                    if (document.forms[0][i].value < 1) {
                        alert('1以上を入力してください');
                        return false;
                    }
                    for(var j = 0; j < check_array.length; j++) {
                        if(check_array[j] == document.forms[0][i].value) {
                            alert('同じ順位が存在します。\n' + document.forms[0][i].value);
                            return false;
                        }
                    }
                    check_array.push(document.forms[0][i].value);
                }
            }
        }
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function closing_window() {
    alert('{rval MSG300}');
    closeWin();
    return true;
}
