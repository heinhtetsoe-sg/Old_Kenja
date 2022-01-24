function btn_submit(cmd) {
    //前年度コピー確認
    if (cmd == 'copy') {
        if (!confirm('{rval MSG101}')) {
            return false;
        }
    }
    //取消確認
    if (cmd == 'reset') {
        if (!confirm('{rval MSG106}')) {
            return false;
        }
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function setPattern(){
    var isA = document.forms[0].SEQ0511.checked;
    var isB = document.forms[0].SEQ0512.checked;

    //平均点
    document.forms[0].SEQ0541.disabled = !(isA || isB);
    document.forms[0].SEQ0542.disabled = !(isA || isB);
    document.forms[0].SEQ0543.disabled = !(isA || isB);
    
    //担任項目名
    document.forms[0].SEQ0621.disabled = !(isA || isB);
    document.forms[0].SEQ0622.disabled = !(isA || isB);

    document.forms[0].SEQ057.disabled = !(isA || isB);  //LHR欠課時数
    document.forms[0].SEQ058.disabled = !(isA || isB);  //留学中の授業日数
    document.forms[0].SEQ059.disabled = !(isA || isB);  //行事欠課時数
    document.forms[0].SEQ063.disabled = !(isA);         //返信欄
    document.forms[0].SEQ064.disabled = !(isB);         //度数分布表
}
