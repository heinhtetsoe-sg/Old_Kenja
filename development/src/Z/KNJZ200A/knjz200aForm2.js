function btn_submit(cmd) {
    //削除確認
    if (cmd == 'delete'){
        if (!confirm('{rval MSG103}'))
            return false;
    }

    //取消確認
    if (cmd == 'reset'){
        if (!confirm('{rval MSG106}'))
            return false;
    }

    //必須入力チェック
    if (cmd == 'add' || cmd == 'delete') {
        if (document.forms[0].IBSUBCLASS.value == '') {
            alert('{rval MSG301}\n（IB科目）');
            return false;
        }
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function checkDecimal(obj) {
    var decimalValue = obj.value
    var check_result = false;

    if (decimalValue != '') {
        //空じゃなければチェック
        if (decimalValue.match(/^[0-9]+(\.[0-9]+)?$/)) {
            check_result = true;
        }
    } else {
        check_result = true;
    }

    if (!check_result) {
        alert('数字を入力して下さい。');
    }

    //正しい値ならtrueを返す
    return check_result;
}

//移動
function Page_jumper(link) {
    if (!confirm('{rval MSG108}')) {
        return;
    }
    parent.location.href=link;
}
