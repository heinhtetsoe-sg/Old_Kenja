function btn_submit(cmd) {
    var i; 
    if (cmd == 'search') {
        if (document.forms[0].EXAMNO.value == '') {
            alert('検索受験番号を入力してください');
            return false;
        }
    } else if (cmd == 'ban') { // 発番
        if (document.forms[0].PLACECD.value == '') {
            alert('試験会場が設定されていません');
            return false;
        }
        if (!document.forms[0].FEE1.checked && !document.forms[0].FEE2.checked) {
            alert('受験料徴収有無が設定されていません');
            return false;
        }
        if (!document.forms[0].APPLYDIV1.checked && !document.forms[0].APPLYDIV2.checked && !document.forms[0].APPLYDIV3.checked) {
            alert('申込区分が設定されていません');
            return false;
        }
    } else if (cmd == 'add') { // 追加
        if (document.forms[0].PLACECD.value == '') {
            alert('試験会場が設定されていません');
            return false;
        }
        if (!document.forms[0].FEE1.checked && !document.forms[0].FEE2.checked) {
            alert('受験料徴収有無が設定されていません');
            return false;
        }
        if (!document.forms[0].APPLYDIV1.checked && !document.forms[0].APPLYDIV2.checked && !document.forms[0].APPLYDIV3.checked) {
            alert('申込区分が設定されていません');
            return false;
        }
    } else if (cmd == 'cancel') { // 取消
        if (!confirm('各項目欄をクリアします。\nよろしいですか？')) {
            return false;
        }
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
//権限
function OnAuthError()
{
    alert('{rval MSG300}');
    closeWin();
}

function newwin(SERVLET_URL) {
//    var i;
//    var sel;
//    var cmdbk;
//    var selbk = [];
//    //何年用のフォームを使うのか決める
//    if (document.forms[0].FORM6 && document.forms[0].FORM6.checked) {
//        document.forms[0].NENYOFORM.value = document.forms[0].NENYOFORM_CHECK.value
//    } else {
//        document.forms[0].NENYOFORM.value = document.forms[0].NENYOFORM_SYOKITI.value
//    }
//
//    if (document.forms[0].category_selected.length == 0) {
//        alert('{rval MSG916}');
//        return false;
//    }
//    if (document.forms[0].tyousasyoCheckCertifDate.value == '1' && document.forms[0].DATE.value == '') {
//        alert('記載（証明）日付を指定してください。');
//        return;
//    }
//
//    for (i = 0; i < document.forms[0].category_name.length; i++) {
//        document.forms[0].category_name.options[i].selected = 0;
//    }
//    for (i = 0; i < document.forms[0].category_selected.length; i++) {
//        document.forms[0].category_selected.options[i].selected = 1;
//        selbk[i] = document.forms[0].category_selected.options[i].value;
//        sel = document.forms[0].category_selected.options[i].value.split('-');
//        if (sel.length > 1) {
//            document.forms[0].category_selected.options[i].value = sel[3];
//        }
//    }

    action = document.forms[0].action;
    target = document.forms[0].target;

//    url = location.hostname;
//    document.forms[0].action = "http://" + url +"/cgi-bin/printenv.pl";
    document.forms[0].action = SERVLET_URL +"/KNJL";
    document.forms[0].target = "_blank";
    document.forms[0].submit();

    document.forms[0].action = action;
    document.forms[0].target = target;
}

