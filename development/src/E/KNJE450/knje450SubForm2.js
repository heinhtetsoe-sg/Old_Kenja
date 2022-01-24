function btn_submit(cmd) {
    //必須チェック
    if (document.forms[0].SCHREGNO.value == "") {
        alert('{rval MSG304}');
        return true;
    }

    //取消
    if (cmd == 'subform2_clear') {
        if (!confirm('{rval MSG106}')) {
            return false;
        }
    }

    //画面切替
    if (cmd == 'subform1') {
        if (!confirm('{rval MSG108}')) {
            return false;
        } else {
            document.forms[0].WRT_DATE.value = "";
        }
    }

    //追加
    if (cmd == 'subform2_insert') {
        //必須チェック
        if (document.forms[0].WRT_DATE.value == "") {
            alert('{rval MSG301}\n（作成日）');
            return true;
        }

        //作成日の範囲チェック
        var date = document.forms[0].WRT_DATE.value.split('/');
        var this_year = document.forms[0].THIS_YEAR.value;
        var next_year = document.forms[0].NEXT_YEAR.value;

        if ((new Date(eval(this_year),eval('04')-1,eval('01')) > new Date(eval(date[0]),eval(date[1])-1,eval(date[2]))) ||
            (new Date(eval(next_year),eval('03')-1,eval('31')) < new Date(eval(date[0]),eval(date[1])-1,eval(date[2]))))
        {
            alert('作成日が入力範囲外です。\n（' + this_year + '/04/01' + '～' + next_year + '/03/31' + '）');
            return true;
        }
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function newwin(SERVLET_URL) {
    if (document.forms[0].WRITING_DATE.value == "") {
        alert('作成年月日を選択して下さい。');
        return false;
    }

    document.forms[0].SCHREG_SELECTED.value = document.forms[0].SCHREGNO.value;

    action = document.forms[0].action;
    target = document.forms[0].target;

//  url = location.hostname;
//  document.forms[0].action = "http://" + url +"/cgi-bin/printenv.pl";
    document.forms[0].action = SERVLET_URL +"/KNJE";
    document.forms[0].target = "_blank";
    document.forms[0].submit();

    document.forms[0].action = action;
    document.forms[0].target = target;
}

//disabled
function OptionUse(obj) {
    var flg;
    if (obj.checked == true) {
        flg = false;
	} else {
        flg = true;
    }

    var n = obj.name.replace("_FLG", "");
    for (var i=0; i < document.forms[0].elements.length; i++) {
        if (document.forms[0].elements[i].name == n) {
            document.forms[0].elements[i].disabled = flg;
        }
    }
}

//文字数チェック
function lenCheck(obj, maxlen) {
    String.prototype.mlength = function() {
        var len=0;
        for (var i=0; i<this.length; i++) {
            var code=this.charCodeAt(i);
            if (code > 255 && !(code > 65381 && code < 65440)) len++;
            len++
        }
        return len;
    }

    var str = new String(obj.value);
    var sm = str.mlength();
    if (sm > maxlen) {
        alert('{rval MSG914}\n全角'+(maxlen/2)+'文字までです。');
    }
}
