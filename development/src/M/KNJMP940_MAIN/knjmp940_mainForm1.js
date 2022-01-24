//サブミット
function btn_submit(cmd)
{
    if (cmd == 'cancel') {
        if (!confirm('{rval MSG106}')){
            return false;
        }
    } else if (cmd == 'update') {
        if (document.forms[0].SEISAN_L_M_CD.value == "") {
            alert('{rval MSG301}' + '(精算項目)');
            return false;
        }
        if (document.forms[0].JURYOU_GK.value == "") {
            alert('{rval MSG301}' + '(受領額)');
            return false;
        }
        if (document.forms[0].SIHARAI_GK.value == "") {
            alert('{rval MSG301}' + '(支払額)');
            return false;
        }
        if (document.forms[0].ZAN_GK.value == "") {
            alert('{rval MSG301}' + '(残額)');
            return false;
        }
    } else if (cmd == 'delete' && !confirm('{rval MSG103}')){
        return false;
    }
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function btn_error(flg)
{
    if (flg == 'huriwake') {
        alert('精算項目が変更されています。\n更新ボタンを押して、入力内容を保存してください。');
    } else if (flg == 'new') {
        alert('{rval MSG303}' + '\n更新ボタンを押して、入力内容を保存してください。');
    }
}

function newwin(SERVLET_URL){

    action = document.forms[0].action;
    target = document.forms[0].target;

    //url = location.hostname;
    //document.forms[0].action = "http://" + url +"/cgi-bin/printenv.pl";
    document.forms[0].action = SERVLET_URL +"/KNJM";
    document.forms[0].target = "_blank";
    document.forms[0].submit();

    document.forms[0].action = action;
    document.forms[0].target = target;
}


//購入金額計算
function keisanZangk(setvalue)
{
    var juryou_gk = document.forms[0].JURYOU_GK.value;
    var siharai_gk = document.forms[0].SIHARAI_GK.value;
    juryou_gk = (isNaN(juryou_gk) || !juryou_gk) ? 0 : parseInt(juryou_gk, 10);
    siharai_gk = (isNaN(siharai_gk) || !siharai_gk) ? 0 : parseInt(siharai_gk, 10);
    
    document.forms[0].ZAN_GK.value = juryou_gk - siharai_gk;
}
