function btn_submit(cmd) {
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
        if (document.forms[0].COLLECT_GRP_CD.value == '') {
            alert('{rval MSG304}\n　　　( 入金グループ )');
            return;
        }
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

//全チェック操作
function check_all(obj){
    val = obj.id.split(':');
    for (var i = 1; i <= 12; i++) {
        document.getElementById("COLLECT_MONTH_"+i+":"+val[1]).checked = obj.checked;
    }

    if (obj.checked) {
        document.forms[0]["MONTH_CNT:"+val[1]].value = 12;
        document.getElementById("MONTH_CNT:"+val[1]).innerHTML = 12;
    } else {
        document.forms[0]["MONTH_CNT:"+val[1]].value = "";
        document.getElementById("MONTH_CNT:"+val[1]).innerHTML = "";
    }

}

//チェック件数
function check_cnt(obj){

    val = obj.id.split(':');
    var cnt = 0;
    for (var i = 1; i <= 12; i++) {
        if (document.getElementById("COLLECT_MONTH_"+i+":"+val[1]).checked) cnt++;
    }

    if (cnt > 0) {
        document.forms[0]["MONTH_CNT:"+val[1]].value = cnt;
        document.getElementById("MONTH_CNT:"+val[1]).innerHTML = cnt;
    } else {
        document.forms[0]["MONTH_CNT:"+val[1]].value = "";
        document.getElementById("MONTH_CNT:"+val[1]).innerHTML = "";
    }

}
