function btn_submit(cmd)
{
    if (cmd == 'search') {
        if (document.forms[0].STAFFCD.value == "" && document.forms[0].STAFFNAME.value == "" && document.forms[0].STAFFNAME_KANA.value == "") {
            alert('{rval MSG301}' + '\nいずれかの検索項目を入力してください。');
            return false;
       }
    }
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function insDefStaffCd(obj) {
    var schoolStaffCd = document.forms[0]["UP_STAFFCD" + obj.value];
    if (obj.checked) {
        if (schoolStaffCd.value == "") {
            schoolStaffCd.value = obj.value;
        }
    } else {
        schoolStaffCd.value = "";
    }
}

function closeMethod() {
    window.opener.btn_submit('');
    closeWin();
}
//権限
function closing_window()
{
    alert('{rval MSG300}');
    closeWin();
    return true;
}
