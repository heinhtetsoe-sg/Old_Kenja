//サブミット
function btn_submit(cmd)
{
    if (cmd == 'cancel') {
        if (!confirm('{rval MSG106}')){
            return false;
        }
    } else if (cmd == 'delete_update') {
        if (document.forms[0].COLLECT_L_M_S_CD.value == "") {
            alert('{rval MSG301}' + '(入金科目)');
            return false;
        }
        if (document.forms[0].INCOME_L_M_CD.value == "") {
            alert('{rval MSG301}' + '(収入項目)');
            return false;
        }
        if (document.forms[0].REQUEST_GK.value == "") {
            alert('{rval MSG301}' + '(収入額)');
            return false;
        }
        if (parseInt(document.forms[0].REQUEST_GK.value) < parseInt(document.forms[0].SUM_TOTAL_PRICE.value)) {
            alert('{rval MSG203}' + '\n摘要総額が収入額を超えています。');
            return false;
        }
        if (!confirm('{rval MSG102}' + '\n入金科目または収入項目が変更されています。\n変更前に登録されていた収入の振分け情報は全て削除されます。')){
            return false;
        }
    } else if (cmd == 'update') {
        if (document.forms[0].COLLECT_L_M_S_CD.value == "") {
            alert('{rval MSG301}' + '(入金科目)');
            return false;
        }
        if (document.forms[0].INCOME_L_M_CD.value == "") {
            alert('{rval MSG301}' + '(収入項目)');
            return false;
        }
        if (document.forms[0].REQUEST_GK.value == "") {
            alert('{rval MSG301}' + '(収入額)');
            return false;
        }
        if (parseInt(document.forms[0].REQUEST_GK.value) < parseInt(document.forms[0].SUM_TOTAL_PRICE.value)) {
            alert('{rval MSG203}' + '\n摘要総額が収入額を超えています。');
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
        alert('入金科目または収入項目が変更されています。\n更新ボタンまたは取消ボタンで入力内容を確定してください。');
    } else if (flg == 'new') {
        alert('{rval MSG303}' + '\n更新ボタンを押して、入力内容を保存してください。');
    } else if (flg == 'kessai') {
        alert('収入額と摘要総額の金額が異なります。');
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
