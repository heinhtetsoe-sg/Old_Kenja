function btn_submit(cmd) {
    //CSV出力
    if (cmd == 'csv') {
        //対象データチェック
        var cnt_row = 0;
        for (var i=0; i < document.forms[0].elements.length; i++) {
            if (document.forms[0].elements[i].name == "CHECKED[]") {
                cnt_row++;
            }
        }
        if (cnt_row == 0) {
            alert('{rval MSG303}');
            return true;
        }
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

//欠点の値チェック
function changeKetten(obj, cmd, div) {
    var flg = false;
    if (div == '1' && event.keyCode == 13) {
        flg = true;
    } else if (div != '1') {
        flg = true;
    }

    if (flg == true) {
        var beforeVal = document.forms[0].KEEP_KETTEN.value;
        if (obj.value == "") {
            alert('{rval MSG304}\n( 欠点 )');
            obj.focus();
            return;
        }

        //数値チェック
        obj.value = toInteger(obj.value);

        if (obj.value == "") {
            alert('{rval MSG304}\n( 欠点 )');
            obj.focus();
            return;
        } else if (beforeVal != obj.value) {
            document.forms[0].cmd.value = cmd;
            document.forms[0].submit();
        }
    }
}

//全チェック操作
function check_all(obj) {
    var check_flg = false;
    for (var i=0; i < document.forms[0].elements.length; i++) {
        if (document.forms[0].elements[i].name == "CHECKED[]") {
            document.forms[0].elements[i].checked = obj.checked;
            if (obj.checked == true) check_flg = true;
        }
    }

	if (check_flg == true) {
        document.forms[0].btn_print1.disabled = false;
	} else {
	    document.forms[0].btn_print1.disabled = true;
	}
}

//ボタンの使用不可
function OptionUse(obj) {
    var check_flg = false;
    for (var i=0; i < document.forms[0].elements.length; i++) {
        if (document.forms[0].elements[i].name == "CHECKED[]" && document.forms[0].elements[i].checked == true) {
            check_flg = true;
        }
    }

	if (check_flg == true) {
        document.forms[0].btn_print1.disabled = false;
	} else {
	    document.forms[0].btn_print1.disabled = true;
	}
}

//印刷
function newwin(SERVLET_URL, kind) {
    //対象データチェック
    var cnt_check = cnt_row = 0;
    for (var i=0; i < document.forms[0].elements.length; i++) {
        if (document.forms[0].elements[i].name == "CHECKED[]") {
            if (document.forms[0].elements[i].checked) cnt_check++;
            cnt_row++;
        }
    }

    if (kind == 'warning') {
        if (cnt_check == 0) {
            alert('{rval MSG304}');
            return true;
        }
    } else {
        if (cnt_row == 0) {
            alert('{rval MSG303}');
            return true;
        }
    }

    if (document.forms[0].DATE.value == "") {
        alert('{rval MSG304}\n( 出力日付 )');
        return true;
    }

    document.forms[0].FORM_KIND.value = kind;

    action = document.forms[0].action;
    target = document.forms[0].target;

//    url = location.hostname;
//    document.forms[0].action = "http://" + url +"/cgi-bin/printenv.pl";
    document.forms[0].action = SERVLET_URL +"/KNJD";
    document.forms[0].target = "_blank";
    document.forms[0].submit();

    document.forms[0].action = action;
    document.forms[0].target = target;
}
