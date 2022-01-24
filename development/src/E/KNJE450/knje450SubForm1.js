function btn_submit(cmd) {
    //必須チェック
    if (document.forms[0].SCHREGNO.value == "") {
        alert('{rval MSG304}');
        return true;
    }

    //取消
    if (cmd == 'subform1_clear') {
        if (!confirm('{rval MSG106}')) {
            return false;
        }
    }

    //画面切替
    if (cmd == 'subform2') {
        if (!confirm('{rval MSG108}')) {
            return false;
        } else {
            document.forms[0].WRT_DATE.value = "";
        }
    }

    //追加
    if (cmd == 'subform1_insert') {
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
function OptionUse(obj, div) {
    var flg;
    if (obj.checked == true) {
        flg = false;
	} else {
        flg = true;
    }

    var n = obj.name.split('_');
    var remark = n[0]+"_REMARK2";
    var cd = n[1].replace("QUESTION", "");

    var chk = new Array();
    chk[0] = n[0] + "_QUESTION" + String(parseInt(cd)+1);
    chk[1] = n[0] + "_QUESTION" + String(parseInt(cd)+2);

    for (var i=0; i < document.forms[0].elements.length; i++) {
        if (div == "check") {
            for (var j=0; j < chk.length; j++) {

                if (document.forms[0].elements[i].name == chk[j]) {
                    document.forms[0].elements[i].disabled = flg;
                }
            }
        } else {
            if (document.forms[0].elements[i].name == remark) {
                document.forms[0].elements[i].disabled = flg;
            }
        }
    }
}
