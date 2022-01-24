function btn_submit(cmd) {
    if (cmd == 'reset') {
        if (!confirm('{rval MSG106}')) return false;
    }
    if (cmd == 'update') {
        var cntKoumoku    = 0;
        var schList = document.forms[0].SCHREGNOS.value.split(',');

        for (var i=0; i < schList.length; i++) {
            if (document.forms[0]['CHECK_' + schList[i]]) {
                if (document.forms[0]['CHECK_' + schList[i]].checked == true) {
                    cntKoumoku++;
                }
            }
        }

        if (cntKoumoku == 0) {
            alert('{rval MSG203}' + '\n最低ひとつチェックを入れてください。');
            return false;
        }
    }

    //サブミット
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
//全チェック
function chkAll(obj) {
    var schList = document.forms[0].SCHREGNOS.value.split(',');

    for (var i=0; i < schList.length; i++) {
        if (document.forms[0]['CHECK_' + schList[i]]) {
            document.forms[0]['CHECK_' + schList[i]].checked = obj.checked;
        }
    }
}
