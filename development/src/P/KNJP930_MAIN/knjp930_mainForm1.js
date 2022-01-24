//サブミット
function btn_submit(cmd)
{
    var sekou_date_from = document.forms[0].SEKOU_DATE_FROM.value;
    var sekou_date_to = document.forms[0].SEKOU_DATE_TO.value;

    if (cmd == 'cancel') {
        if (!confirm('{rval MSG106}')){
            return false;
        }
    } else if (cmd == 'update') {
        if (sekou_date_from > sekou_date_to) {
            alert('{rval MSG916}' + '(施行期間)');
            return false;
        }
        if (document.forms[0].SEKOU_L_M_CD.value == "") {
            alert('{rval MSG301}' + '(施行項目)');
            return false;
        }
    } else if (cmd == 'delete' && !confirm('{rval MSG103}' + '\nただし、支出伺伝票は作成されていても削除されません。')){
        return false;
    }
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function btn_error(flg)
{
    if (flg == 'huriwake') {
        alert('施行項目が変更されています。\n更新ボタンを押して、入力内容を保存してください。');
    } else if (flg == 'new') {
        alert('{rval MSG303}' + '\n更新ボタンを押して、入力内容を保存してください。');
    }
}

function newwin(SERVLET_URL){

    action = document.forms[0].action;
    target = document.forms[0].target;

    //url = location.hostname;
    //document.forms[0].action = "http://" + url +"/cgi-bin/printenv.pl";
    document.forms[0].action = SERVLET_URL +"/KNJP";
    document.forms[0].target = "_blank";
    document.forms[0].submit();

    document.forms[0].action = action;
    document.forms[0].target = target;
}