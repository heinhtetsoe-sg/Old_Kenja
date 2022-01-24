function btn_submit(cmd) {

    bodyHeight = (window.innerHeight || document.body.clientHeight || 0);
    document.forms[0].windowHeight.value = bodyHeight;

    //取消
    if (cmd == 'reset') {
        if (!confirm('{rval MSG106}')) return false;
    }

    //更新
    if (cmd == 'update') {
        if (document.forms[0].SCHOOL_KIND.value == '') {
            alert('{rval MSG304}\n　　　( 校種 )');
            return;
        }

        var schList = document.forms[0].SCH_LIST;
        var sep = '';

        //最低一つチェック入っているか
        var cnt = 0;
        var schNos = document.forms[0].SCH_LIST_JS.value.split(',');
        for (var i = 0; i < schNos.length; i++) {
            if (document.forms[0]["CHK_SCHREG_" + schNos[i]].checked == true) {
                cnt++;
                schList.value = schList.value + sep + schNos[i];
                sep = ',';
            }
        }
        if (cnt == 0) {
            alert('{rval MSG203}' + '\n最低ひとつチェックを入れてください。');
            return false;
        }
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
//画面リサイズ
function submit_reSize() {
    bodyHeight = (window.innerHeight || document.body.clientHeight || 0);

    document.getElementById("tbody").style.height = bodyHeight - 200;
}
//全チェック操作
function check_all(obj){
    var schNos = document.forms[0].SCH_LIST_JS.value.split(',');
    for (var i = 0; i < schNos.length; i++) {
        document.forms[0]["CHK_SCHREG_" + schNos[i]].checked = obj.checked;
    }
}
