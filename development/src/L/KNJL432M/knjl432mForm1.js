function btn_submit(cmd) {
    //全員合格
    if (cmd == "allpass" && !confirm("{rval MSG102}")) return true;

    //終了
    if (cmd == "end") {
        closeWin();
    }

    //更新
    if (cmd == "update") {
        if (!confirm("{rval MSG102}")) {
            return;
        }
    }

    document.forms[0].btn_allpass.disabled = true;
    document.forms[0].btn_update.disabled = true;
    document.forms[0].btn_csv.disabled = true;
    document.forms[0].btn_end.disabled = true;

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

//一括チェック
function check_all(obj) {
    console.log(obj.name);
    var keyArray = document.forms[0].HID_KEY.value.split(",");
    for (var i = 0; i < keyArray.length; i++) {
        console.log("CHECK_" + keyArray[i]);
        //更新チェック
        if (obj.name == "CHECKALL") {
            tergetObject = document.getElementById("CHECK_" + keyArray[i]);
            console.log(tergetObject);
            if (tergetObject) {
                console.log(obj.checked);
                tergetObject.checked = obj.checked;
            }
        }
        console.log("---");
    }
}
