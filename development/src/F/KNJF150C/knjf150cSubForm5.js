function btn_submit(cmd) {

    //チェック
    if ((cmd == 'update') || (cmd == 'insert')){
        if (document.forms[0].SCHREGNO.value == ""){
            alert('{rval MSG304}');
            return true;
        } else if ((document.forms[0].SEQ01_REMARK1.value == "") || (document.forms[0].SEQ01_REMARK2.value == "") || (document.forms[0].SEQ01_REMARK3.value == "")){
            alert('来室日時が入力されていません。\n　　　　（必須入力）');
            return true;
        } else if (document.forms[0].SEQ02_REMARK1.value == "") {
            alert('来室理由が入力されていません。\n　　　　（必須入力）');
            return true;
        }
    } else if (cmd == 'subform5_clear'){
        if (!confirm('{rval MSG106}')) {
            return false;
        }
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

//印刷
function newwin(SERVLET_URL){

    alert('登録された内容が印刷されます。');

    action = document.forms[0].action;
    target = document.forms[0].target;

//    url = location.hostname;
//    document.forms[0].action = "http://" + url +"/cgi-bin/printenv.pl";
    document.forms[0].action = SERVLET_URL +"/KNJF";
    document.forms[0].target = "_blank";
    document.forms[0].submit();

    document.forms[0].action = action;
    document.forms[0].target = target;
}

//disabled
function OptionUse(obj)
{
    if(document.forms[0].CONDITION4[0].checked == true)
    {
        document.forms[0].MEAL.disabled = false;
        document.forms[0].MEAL.style.backgroundColor = "#ffffff";
    } else {
        document.forms[0].MEAL.disabled = true;
        document.forms[0].MEAL.style.backgroundColor = "#D3D3D3";
    }
}

//disabled
function OptionUse2(obj, checkText) {
    var array = document.forms[0][checkText].value.split(',');

    flg = true;
    for (var i = 0; i < array.length; i++) {
        if (array[i] == obj.value) {
            flg = false;
        }
    }

    document.forms[0][obj.name+'_TEXT'].disabled = flg;
}
