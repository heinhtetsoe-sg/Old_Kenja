function btn_submit(cmd) {
    if (cmd == "fix" && document.forms[0].TESTDIV.value == "02"){  //小論文ではない試験(testdiv="02")の時は、合格点をチェック
        var ppval = document.forms[0].PASS_POINT.value;
        if (!isNumOnly(ppval)) {
            alert("{rval MSG907}" + " 合格点");
            return false;
        }
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function checkReceptRange() {
    var start = document.forms[0].SIM_START.value;
    var end = document.forms[0].SIM_END.value;
    var h1 = document.forms[0].TOKIO_PROBABILITY.value;
    var h2 = document.forms[0].OTHER_PROBABILITY.value;
    var f1 = document.forms[0].GENERAL_PROBABILITY1.value;
    var f2 = document.forms[0].GENERAL_PROBABILITY2.value;
    var e1 = document.forms[0].POSTPONE_PROBABILITY1.value;
    var e2 = document.forms[0].POSTPONE_PROBABILITY2.value;
    var e3 = document.forms[0].POSTPONE_PROBABILITY3.value;

    if (start != '' && end != '') {
        if (!isNumOnly(start) || !isNumOnly(end)) {
            alert("{rval MSG907}" + " シミュレーション最低点");
            return false;
        }
        if (!isNumOnly(h1)) {
            alert("{rval MSG907}" + " 併願(東京)確率");
            return false;
        }
        if (!isNumOnly(h2)) {
            alert("{rval MSG907}" + " 併願(東京以外)確率");
            return false;
        }
        if (!isNumOnly(f1)) {
            alert("{rval MSG907}" + " 一般確率1");
            return false;
        }
        if (!isNumOnly(f2)) {
            alert("{rval MSG907}" + " 一般確率2");
            return false;
        }
        if (!isNumOnly(e1)) {
            alert("{rval MSG907}" + " 一般延納確率1");
            return false;
        }
        if (!isNumOnly(e2)) {
            alert("{rval MSG907}" + " 一般延納確率2");
            return false;
        }
        if (!isNumOnly(e3)) {
            alert("{rval MSG907}" + " 一般延納確率3");
            return false;
        }
        if (parseInt(start) > parseInt(end)) {
            alert("終了には開始以降の値を入力してください。");
            document.forms[0].RECEPTNO_END.focus();
            return false;
        }
    } else {
        alert("{rval MSG907}" + " シミュレーション最低点");
        return false;
    }

    return true;
}

function isNumOnly(obj) {
    var regex = new RegExp(/^[0-9]+$/);
    return regex.test(obj);
}

//印刷
function newwin(SERVLET_URL) {
    if (!checkReceptRange()) {
        return false;
    }

    action = document.forms[0].action;
    target = document.forms[0].target;

    //    url = location.hostname;
    //    document.forms[0].action = "http://" + url +"/cgi-bin/printenv.pl";
    document.forms[0].action = SERVLET_URL + "/KNJL";
    document.forms[0].target = "_blank";
    document.forms[0].submit();

    document.forms[0].action = action;
    document.forms[0].target = target;
}
