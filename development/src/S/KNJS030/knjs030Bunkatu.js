function btn_submit(cmd) {
    if (cmd != 'subEnd') {

        if (cmd == 'bunUpd') {
            var checkAllMinute = 0;
            for (var i = 1; i <= parseInt(document.forms[0].bunkatuSu.value); i++) {
                var checkChair = document.getElementById("B_CHAIRCD" + i).value;
                var checkMinute = document.getElementById("B_MINUTE" + i).value;
                if ((checkChair == '' && checkMinute != '') || (checkChair != '' && checkMinute == '')) {
                    alert('講座/時間のみの設定は出来ません。');
                    return false;
                }
                if (parseInt(checkMinute)) {
                    checkAllMinute += parseInt(checkMinute);
                }
            }
            if (checkAllMinute != 45) {
                alert('分割時間はトータル45分\nになるよう設定して下さい。');
                return false;
            }
        }

        document.forms[0].cmd.value = cmd;
        document.forms[0].submit();
        return false;
    }

    top.main_frame.closeit();
    top.main_frame.document.forms[0].cmd.value = cmd;
    top.main_frame.document.forms[0].submit();
    return false;
}

function checkMinute(obj) {
    if (toInteger(obj.value)) {
        if (parseInt(obj.value) % 5 != 0) {
            alert('登録は5分単位で設定して下さい。');
            obj.value = "";
            obj.focus();
        }
        var minute1 = parseInt(document.forms[0].B_MINUTE1.value) ? parseInt(document.forms[0].B_MINUTE1.value) : 0;
        var minute2 = parseInt(document.forms[0].B_MINUTE2.value) ? parseInt(document.forms[0].B_MINUTE2.value) : 0;
        var minute3 = parseInt(document.forms[0].B_MINUTE3.value) ? parseInt(document.forms[0].B_MINUTE3.value) : 0;
        var minute4 = parseInt(document.forms[0].B_MINUTE4.value) ? parseInt(document.forms[0].B_MINUTE4.value) : 0;
        var minute5 = parseInt(document.forms[0].B_MINUTE5.value) ? parseInt(document.forms[0].B_MINUTE5.value) : 0;
        var minute6 = parseInt(document.forms[0].B_MINUTE6.value) ? parseInt(document.forms[0].B_MINUTE6.value) : 0;
        var minute7 = parseInt(document.forms[0].B_MINUTE7.value) ? parseInt(document.forms[0].B_MINUTE7.value) : 0;
        var minute8 = parseInt(document.forms[0].B_MINUTE8.value) ? parseInt(document.forms[0].B_MINUTE8.value) : 0;
        var minute9 = parseInt(document.forms[0].B_MINUTE9.value) ? parseInt(document.forms[0].B_MINUTE9.value) : 0;
        if (minute1 + minute2 + minute3 + minute4 + minute5 + minute6 + minute7 + minute8 + minute9 > 45) {
            alert('45分以内で設定して下さい。');
            obj.value = "";
            obj.focus();
        }
    } else {
        obj.value = "";
    }
    return false;
}
