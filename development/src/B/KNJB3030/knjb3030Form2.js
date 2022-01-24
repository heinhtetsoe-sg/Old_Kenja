function btn_submit(cmd) {
    //群コードを変更した場合、一時的に更新・削除ボタンを押せないようにする（再読込の処理中）
    if (cmd == "group") {
        document.forms[0].btn_udpate.disabled = true;
        document.forms[0].btn_del.disabled = true;
    }
    //削除ボタン押し下げ時
    if (cmd == "delete") {
        if (!confirm("{rval MSG103}")) return false;
    }
    //取消ボタン押し下げ時
    if (cmd == "reset") {
        if (!confirm("{rval MSG106}")) return false;
    }
    //HRクラス選択ボタン押し下げ時
    if (cmd == "hrSearch") {
        param = document.forms[0].GRADE_CLASS.value;
        loadwindow(
            "knjb3030index.php?cmd=hrSearch&param=" + param,
            0,
            0,
            350,
            400
        );
        return true;
    }
    //授業クラス選択ボタン押し下げ時
    if (cmd == "lcSearch") {
        param = document.forms[0].GRADE_LC_CLASS.value;
        loadwindow(
            "knjb3030index.php?cmd=lcSearch&param=" + param,
            0,
            0,
            350,
            400
        );
        return true;
    }
    //科目担任選択ボタン押し下げ時
    if (cmd == "subformStaff") {
        if (document.forms[0].SUBCLASSCD.value == "") {
            alert("科目を選択してください。");
            return false;
        } else {
            param = document.forms[0].STAFFCD.value;
            param2 = document.forms[0].STF_CHARGE.value;
            stfOrder = document.forms[0].STF_ORDER.value;
            subclass = document.forms[0].SUBCLASSCD.value;
            var setWidth = 350;
            if (document.forms[0].useChairStaffOrder.value == "1") {
                setWidth = 400;
            }
            loadwindow(
                "knjb3030index.php?cmd=subformStaff&param=" +
                    param +
                    "&param2=" +
                    param2 +
                    "&stfOrder=" +
                    stfOrder +
                    "&subclass=" +
                    subclass,
                0,
                0,
                setWidth,
                400
            );
            return true;
        }
    }
    //使用施設選択ボタン押し下げ時
    if (cmd == "subformFacility") {
        param = document.forms[0].FACCD.value;
        loadwindow(
            "knjb3030index.php?cmd=subformFacility&param=" + param,
            0,
            0,
            350,
            400
        );
        return true;
    }
    //使用施設選択ボタン押し下げ時
    if (cmd == "subformSikenKaizyou") {
        param = document.forms[0].SIKENKAIZYOUFACCD.value;
        loadwindow(
            "knjb3030index.php?cmd=subformSikenKaizyou&param=" + param,
            0,
            0,
            350,
            400
        );
        return true;
    }
    //教科書選択ボタン押し下げ時
    if (cmd == "subformTextBook") {
        param = document.forms[0].TEXTBOOKCD.value;
        loadwindow(
            "knjb3030index.php?cmd=subformTextBook&param=" + param,
            0,
            0,
            350,
            400
        );
        return true;
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
//権限チェック
function OnAuthError() {
    alert("{rval MSG300}");
    closeWin();
}
//名簿入力画面へ
function Page_jumper(
    URL,
    year,
    semester,
    chaircd,
    groupcd,
    staffcd,
    authority
) {
    if (chaircd == "" || groupcd == "") {
        alert("{rval MSG304}");
        return false;
    }
    wopen(
        URL +
            "?year=" +
            year +
            "&semester=" +
            semester +
            "&chaircd=" +
            chaircd +
            "&groupcd=" +
            groupcd +
            "&staffcd=" +
            staffcd +
            "&authority=" +
            authority,
        "name",
        0,
        0,
        screen.availWidth,
        screen.availHeight
    );
}

//Submitしない
function btn_keypress(){
    if (event.keyCode == 13){
        event.keyCode = 0;
        window.returnValue  = false;
    }
}
