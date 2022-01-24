function btn_submit(cmd) {
    document.forms[0].GRADE_HR_CLASS.value = document.forms[0].GRADE_HR_CLASS_SET.value;
    if (cmd == 'update') {

        //管理者コントロールチェック
        if (document.forms[0].ADMIN_CONTROL.value == 0) {
            alert("{rval MSG300}")
            return false;
        }
    }
    if (cmd == 'clear') {
        document.forms[0].GRADE_HR_CLASS.value = "";
    }

    //処理中はボタンを使用不可
    ButtonDisable();

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

//事前チェック
function PreCheck() {
    alert('処理日付が不正です。\n日付を学期の範囲内に設定してください。');
    closeWin();
}

//画面移動
function Page_jumper(link) {
    parent.location.href=link;
}

//選択クラスセット
function setHrClass(obj, cd, stf) {
    //権限チェック（制限付き）
    if (document.forms[0].RESTRICT.value == "1" && stf != "1") {
        alert("{rval MSG300}")
    } else {
        obj.style.color = "red";
        document.forms[0].GRADE_HR_CLASS.value = cd;
    }

    //処理中はボタンを使用不可
    ButtonDisable();

    document.forms[0].cmd.value = "knjc033d";
    document.forms[0].submit();
    return false;
}

//処理中はボタンを使用不可
function ButtonDisable() {
    for (i = 0; i < document.forms[0].elements.length; i++) {
        if (document.forms[0].elements[i].type == 'button') {
            document.forms[0].elements[i].disabled = true;
        }
    }
}
