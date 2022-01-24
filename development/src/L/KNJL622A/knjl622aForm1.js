function btn_submit(cmd) {

    //取消確認
    if (cmd == 'clear') {
        if (!confirm('{rval MSG106}')) {
            return false;
        }
    }
    //終了
    if (cmd == 'close') {
        if (confirm('{rval MSG108}')) {
            closeWin();
        }
        return false;
    }
    //更新
    if (cmd == 'update') {
        var hallSeatArray = {};
        var elements = document.getElementsByName("HALLSEATCD[]");
        // 座席番号重複チェック
        for (let i = 0; i < elements.length; i++) {
            const hallSeat = elements[i];
            if (!hallSeat.value) {
                continue;
            }
            if (!hallSeatArray[hallSeat.value]) {
                hallSeatArray[hallSeat.value] = 1;
            } else {
                alert('{rval MSG302}' + '\n座席番号：' + hallSeat.value);
                return false;
            }
        }
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function checkHallSeat(obj) {
    // 未入力の場合はチェックなし
    if (!obj.value) {
        return true;
    }
    if (obj.value.length != 4) {
        alert('{rval MSG901}' + '\n座席番号は４桁で入力してください。');
        return false;
    }

    var seat = obj.value.substring(2);
    var regex = new RegExp('[0-9]{2}', 'g');
    if (!regex.test(seat)) {
        alert('{rval MSG901}' + '\n座席番号は数値で入力してください。');
        return false;
    }

    return true;
}
