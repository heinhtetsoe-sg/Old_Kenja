function btn_submit(cmd) {
    if (document.forms[0].SCHREGNO.value == "") {
        alert("{rval MSG304}");
        return true;
    }
    if (cmd == "subform3ConcreteSupport_updatemain") {
        //作成年月日チェック
        var getRecordDate = document.forms[0].RECORD_DATE.value;
        // var getSDate = document.forms[0].SDATE.value;
        // var getEDate = document.forms[0].EDATE.value;
        // var ctrlDate = document.forms[0].CTRL_DATE.value;
        // var maxWiringDate = document.forms[0].MAX_DATE.value;

        if (getRecordDate == "") {
            alert("{rval MSG301}" + "\n(作成年月日)");
            return true;
        }
        // if (getSDate > getRecordDate) {
        //    alert('{rval MSG203}' + '\n作成年月日は年度内の日付を指定して下さい。');
        //    return true;
        // }
        // if (getEDate < getRecordDate) {
        //    alert('{rval MSG203}' + '\n作成年月日は年度内の日付を指定して下さい。');
        //    return true;
        // }
        // if (ctrlDate < getRecordDate) {
        //    getSDate = getSDate.replace(/-/g, "/");
        //    ctrlDate = ctrlDate.replace(/-/g, "/");
        //    alert('{rval MSG916}' + '\n作成年月日の範囲は'+getSDate+'～'+ctrlDate+'です。');
        //    return true;
        // }
        // if (maxWiringDate < getRecordDate) {
        //    maxWiringDate = maxWiringDate.replace(/-/g, "/");
        //    alert('{rval MSG916}' + '\n'+maxWiringDate+'以降でログインして下さい。');
        //    return true;
        // }
    }

    if (cmd == "subform3ConcreteSupport_clear") {
        if (!confirm("{rval MSG106}")) {
            return false;
        }
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

//Submitしない
function btn_keypress() {
    if (event.keyCode == 13) {
        event.keyCode = 0;
        window.returnValue = false;
    }
}

function NextStudent(cd) {
    var nextURL;
    nextURL = loadCookie("nextURL");
    if (nextURL) {
        if (cd == "0") {
            //クッキー削除
            deleteCookie("nextURL");
            document.location.replace(nextURL);
            alert("{rval MSG201}");
        } else if (cd == "1") {
            //クッキー削除
            deleteCookie("nextURL");
        }
    }
}
