function btn_submit(cmd) {
    //管理者コントロールチェック
    if (cmd == 'form2_delete' || cmd == 'form2_update') {
        if (document.forms[0].ADMIN_CONTROL.value == 0) {
            alert("{rval MSG300}")
            return false;
        }
    }

    //取消確認
    if (cmd == 'form2_clear') {
        if (!confirm('{rval MSG106}'))
            return false;
    }
    //削除確認
    if (cmd == 'form2_delete') {
        if (!confirm('{rval MSG103}'))
            return false;
    }

    //処理中はボタンを使用不可
    ButtonDisable();

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

//事前チェック
function PreCheck(comment) {
    alert('{rval MSG305}\n'+comment);
    document.forms[0].cmd.value = "main";
    document.forms[0].submit();
    return false;
}

//出欠コードセット
function setDICD(obj, cd) {

    for (var i=0; i < document.forms[0].elements.length; i++) {
        if (document.forms[0].elements[i].name.match(/btn_di_cd/)) {
            document.forms[0].elements[i].style.color = "";
        }
    }

    //処理中はボタンを使用不可
    ButtonDisable();

    obj.style.color = "red";
    document.forms[0].DI_CD.value = cd;

    document.forms[0].cmd.value = "form2_chg";
    document.forms[0].submit();
    return false;
}

//出欠コードセット
function setATTEND(obj, cd) {
    e = document.forms[0].elements;
    d = document.forms[0].DI_CD_SET;
    p = document.forms[0].DI_CD_SET_POSITION;

    //出欠コード情報
    var c001 = new Array();
    for (var i=0; i < e.length; i++) {
        if (e[i].name.match(/^C001_/)) {
            n = e[i].name.split("_");
            c001[n[1]] = e[i].value;
        }
    }

    re = new RegExp(cd + "$");

    //入力値と既存値の比較
    a1 = false;
    a2 = false;
    a3 = false;
    for (var i=0; i < e.length; i++) {
        if (e[i].name.match(re)) {
            //左
            if (p.value == "1") {
                if (e[i].name == 'ATTEND1_'+cd && d.value == e[i].value) {
                    a1 = true;
                }
            }
            //中央
            if (p.value == "2") {
                if (e[i].name == 'ATTEND2_'+cd && d.value == e[i].value) {
                    a2 = true;
                }
            }
            //右
            if (p.value == "3") {
                if (e[i].name == 'ATTEND3_'+cd && d.value == e[i].value) {
                    a3 = true;
                }
            }
        }
    }

    //値をセット
    for (var i=0; i < e.length; i++) {
        if (e[i].name.match(re)) {
            //左
            if (p.value == "1") {
                if (e[i].name == 'MARK1_'+cd) {
                    if (a1 == true) {
                        e[i].value = "";
                    } else {
                        e[i].value = c001[d.value];
                    }
                }
                if (e[i].name == 'ATTEND1_'+cd) {
                    if (a1 == true) {
                        e[i].value = "";
                    } else {
                        e[i].value = d.value;
                    }
                }
                //中央、右の値をクリア
                if (e[i].name == 'MARK2_'+cd || e[i].name == 'MARK3_'+cd || e[i].name == 'ATTEND2_'+cd || e[i].name == 'ATTEND3_'+cd) {
                    e[i].value = "";
                }
            }
            //中央
            if (p.value == "2") {
                if (e[i].name == 'MARK2_'+cd) {
                    if (a2 == true) {
                        e[i].value = "";
                    } else {
                        e[i].value = c001[d.value];
                    }
                }
                if (e[i].name == 'ATTEND2_'+cd) {
                    if (a2 == true) {
                        e[i].value = "";
                    } else {
                        e[i].value = d.value;
                    }
                }
                //左の値をクリア
                if (e[i].name == 'MARK1_'+cd || e[i].name == 'ATTEND1_'+cd) {
                    e[i].value = "";
                }
            }
            //右
            if (p.value == "3") {
                if (e[i].name == 'MARK3_'+cd) {
                    if (a3 == true) {
                        e[i].value = "";
                    } else {
                        e[i].value = c001[d.value];
                    }
                }
                if (e[i].name == 'ATTEND3_'+cd) {
                    if (a3 == true) {
                        e[i].value = "";
                    } else {
                        e[i].value = d.value;
                    }
                }
                //左の値をクリア
                if (e[i].name == 'MARK1_'+cd || e[i].name == 'ATTEND1_'+cd) {
                    e[i].value = "";
                }
            }
        }
    }
}

//処理中はボタンを使用不可
function ButtonDisable() {
    for (i = 0; i < document.forms[0].elements.length; i++) {
        if (document.forms[0].elements[i].type == 'button') {
            document.forms[0].elements[i].disabled = true;
        }
    }
}
