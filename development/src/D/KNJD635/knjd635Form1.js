function btn_submit(cmd) {

    if (document.forms[0].SCHREGNO.value == ""){
        alert('{rval MSG304}');
        return true;
    } else if (cmd == 'clear') {
        if (!confirm('{rval MSG106}')) {
            return false;
        }
    } else if (cmd == 'update'){

        for (var i = 0; i < document.forms[0].elements.length; i++ ) {
            var e = document.forms[0].elements[i];
            if (e.type == 'text' && e.value != '') {
                var str = e.value;
                var nam = e.name;
                //英小文字から大文字へ自動変換
                if (str.match(/a|b|c/)) { 
                    e.value = str.toUpperCase();
                    str = str.toUpperCase();
                }

                if (!str.match(/A|B|C/)) { 
                    alert('{rval MSG901}'+'「AまたはBまたはC」を入力して下さい。');
                    return;
                }
            }
        }
    }
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

//入力チェック
function calc(obj){

    var str = obj.value;
    var nam = obj.name;
    
    //空欄
    if (str == '') { 
        return;
    }

    //英小文字から大文字へ自動変換
    if (str.match(/a|b|c/)) { 
        obj.value = str.toUpperCase();
        str = str.toUpperCase();
    }

    if (!str.match(/A|B|C/)) { 
        alert('{rval MSG901}'+'「AまたはBまたはC」を入力して下さい。');
		obj.focus();
        background_color(obj);
        return;
    }
}
