function btn_submit(cmd) {
    if (cmd == 'reset' && !confirm('{rval MSG106}'))  return true;

    if (cmd == 'updatePrint' && document.forms[0].PRINT_DATE.value == '') {
        alert('印刷日を指定して下さい。');
        return false;
    }
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

//印刷
function newwin(SERVLET_URL, applicant, school, examno){

    if (applicant == ""){
        alert("入試制度を指定して下さい");
        return;
    }

    if (school == ""){
        alert("出身学校を指定して下さい");
        return;
    }

    if (examno == ""){
        alert("生徒を指定して下さい");
        return;
    }

    document.forms[0].PRINT_EXAMNO.value = examno;

    action = document.forms[0].action;
    target = document.forms[0].target;

    document.forms[0].action = SERVLET_URL +"/KNJL";
    document.forms[0].target = "_blank";
    document.forms[0].submit();

    document.forms[0].action = action;
    document.forms[0].target = target;
}

//受領チェック変更で背景色を黄色表示
function bgcolorYellow(obj, examno) {

    changeData = false;

    //changeCnt：変更があれば加算、元に戻れば減算。0で初期状態と一緒
    //指導要録デフォルト値とのチェック
    if (document.forms[0]["YOUROKU_DATA-" + examno].checked && document.forms[0]["DEF_YOUROKU-" + examno].value == "" ||
        !document.forms[0]["YOUROKU_DATA-" + examno].checked && document.forms[0]["DEF_YOUROKU-" + examno].value == "1"
    ) {
        changeData = true;
        if (obj.name.split("-")[0] == "YOUROKU_DATA") {
            document.forms[0].changeCnt.value++;
        }
    } else {
        if (obj.name.split("-")[0] == "YOUROKU_DATA") {
            document.forms[0].changeCnt.value--;
        }
    }
    //健康診断デフォルト値とのチェック
    if (document.forms[0]["MEDEXAM_DATA-" + examno].checked && document.forms[0]["DEF_MEDEXAM-" + examno].value == "" ||
        !document.forms[0]["MEDEXAM_DATA-" + examno].checked && document.forms[0]["DEF_MEDEXAM-" + examno].value == "1"
    ) {
        changeData = true;
        if (obj.name.split("-")[0] == "MEDEXAM_DATA") {
            document.forms[0].changeCnt.value++;
        }
    } else {
        if (obj.name.split("-")[0] == "MEDEXAM_DATA") {
            document.forms[0].changeCnt.value--;
        }
    }
    //スポーツ振興デフォルト値とのチェック
    if (document.forms[0]["SPORTS_DATA-" + examno].checked && document.forms[0]["DEF_SPORTS-" + examno].value == "" ||
        !document.forms[0]["SPORTS_DATA-" + examno].checked && document.forms[0]["DEF_SPORTS-" + examno].value == "1"
    ) {
        changeData = true;
        if (obj.name.split("-")[0] == "SPORTS_DATA") {
            document.forms[0].changeCnt.value++;
        }
    } else {
        if (obj.name.split("-")[0] == "SPORTS_DATA") {
            document.forms[0].changeCnt.value--;
        }
    }

    //その行に変更があれば、行を黄色
    if (changeData) {
        document.getElementById('ROWID' + examno).style.background = "yellow";
        document.forms[0]["PRINT_DATA-" + examno].disabled = true;
    } else {
        document.getElementById('ROWID' + examno).style.background = "white";
        if (document.forms[0]["YOUROKU_DATA-" + examno].checked &&
            document.forms[0]["MEDEXAM_DATA-" + examno].checked &&
            document.forms[0]["SPORTS_DATA-" + examno].checked) {
                document.forms[0]["PRINT_DATA-" + examno].disabled = false;
        }
    }

    //全体で１箇所でも変更があれば、更新ボタンを黄色
    if (document.forms[0].changeCnt.value > 0) {
        document.forms[0].btn_update.style.background = "yellow";
        document.forms[0].btn_updatePrint.disabled = true;
        printCheckDisabled();
    } else {
        document.forms[0].btn_update.style.background = "";
        document.forms[0].btn_updatePrint.disabled = false;
        printCheckAbled();
    }
    setCombDisabled();
}

//印刷チェック使用不可
function printCheckDisabled() {
    document.forms[0].PRINT_ALL.disabled = true;
    for (var i=0; i < document.forms[0].elements.length; i++) {
        var e = document.forms[0].elements[i];
        var nam = e.name;
        if (e.type == 'checkbox' && nam.split("-")[0] == "PRINT_DATA") {
            e.disabled = true;
        }
    }
}

//印刷チェック使用可(デフォルト値考慮)
function printCheckAbled() {
    document.forms[0].PRINT_ALL.disabled = false;
    for (var i=0; i < document.forms[0].elements.length; i++) {
        var e = document.forms[0].elements[i];
        var nam = e.name;
        if (e.type == 'checkbox' && nam.split("-")[0] == "PRINT_DATA") {
            if (document.forms[0]["DEF_YOUROKU-" + nam.split("-")[1]].value == "1" &&
                document.forms[0]["DEF_MEDEXAM-" + nam.split("-")[1]].value == "1" &&
                document.forms[0]["DEF_SPORTS-" + nam.split("-")[1]].value == "1"
            ) {
                e.disabled = false;
            } else {
                e.disabled = true;
            }
        }
    }
}

//全チェック選択（チェックボックスon/off）
function chkDataALL(obj) {
    for (var i=0; i < document.forms[0].elements.length; i++) {
        var e = document.forms[0].elements[i];
        var nam = e.name;
        if (e.type == 'checkbox' && nam.split("-")[0] == "PRINT_DATA" && e.disabled == false) {
            document.forms[0].elements[i].checked = obj.checked;
        }
    }
    printBtnColor();
    yourokuKensinCheckDisabled(obj.checked);
}

//印刷ボタンの色変更
function printBtnColor() {

    changeFlg = false;
    for (var i=0; i < document.forms[0].elements.length; i++) {
        var e = document.forms[0].elements[i];
        var nam = e.name;
        if (e.type == 'checkbox' && nam.split("-")[0] == "PRINT_DATA") {
            if (e.disabled == false && document.forms[0].elements[i].checked) {
                changeFlg = true;
                document.getElementById('ROWID' + nam.split("-")[1]).style.background = "#ccffcc";
            } else {
                document.getElementById('ROWID' + nam.split("-")[1]).style.background = "white";
            }
        }
    }
    //チェックオンがあれば、印刷ボタンを緑色
    if (changeFlg) {
        document.forms[0].btn_updatePrint.style.background = "#ADFF2F";
        document.forms[0].btn_update.disabled = true;
        yourokuKensinCheckDisabled(true);
    } else {
        document.forms[0].btn_updatePrint.style.background = "";
        document.forms[0].btn_update.disabled = false;
        yourokuKensinCheckDisabled(false);
    }
    setCombDisabled();
}

//指導要録、健康診断チェック使用可/不可
function yourokuKensinCheckDisabled(setFlg) {
    for (var i=0; i < document.forms[0].elements.length; i++) {
        var e = document.forms[0].elements[i];
        var nam = e.name;
        if (e.type == 'checkbox' &&
                (nam.split("-")[0] == "YOUROKU_DATA" || nam.split("-")[0] == "MEDEXAM_DATA" || nam.split("-")[0] == "SPORTS_DATA")) {
            e.disabled = setFlg;
        }
    }
}

//コンボボックスを使用可/不可
function setCombDisabled() {
    if (document.forms[0].btn_update.style.background == "" && document.forms[0].btn_updatePrint.style.background == "") {
        document.forms[0].APPLICANTDIV.disabled = false;
        document.forms[0].FINSCHOOLCD.disabled = false;
        document.forms[0].PRINT_ZUMI.disabled = false;
    } else {
        //disabledにするので値を退避
        document.forms[0].HID_APPLICANTDIV.value = document.forms[0].APPLICANTDIV.options[document.forms[0].APPLICANTDIV.selectedIndex].value;
        document.forms[0].HID_FINSCHOOLCD.value = document.forms[0].FINSCHOOLCD.options[document.forms[0].FINSCHOOLCD.selectedIndex].value;
        document.forms[0].HID_PRINT_ZUMI.value = document.forms[0].PRINT_ZUMI.value;

        document.forms[0].APPLICANTDIV.disabled = true;
        document.forms[0].FINSCHOOLCD.disabled = true;
        document.forms[0].PRINT_ZUMI.disabled = true;
    }
}
