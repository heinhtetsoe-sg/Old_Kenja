function btn_submit(cmd, zip, gzip, zadd, gadd) {
    if (cmd == "changeApplicantdiv") {
        document.forms[0].EXAMNO.value = "";
        document.forms[0].RECEPTNO.value = "";
    }

    //新規ボタン
    if (cmd == "reset" && !confirm("{rval MSG106}")) {
        return true;
    }
    if (cmd == "reference" || cmd == "back1" || cmd == "next1") {
        if (document.forms[0].RECEPTNO.value == "") {
            alert("{rval MSG301}\n( 志願者SEQ )");
            return true;
        }
        if (vflg == true || zip != document.forms[0].ZIPCD.value || gzip != document.forms[0].GZIPCD.value || zadd != document.forms[0].ADDRESS1.value || gadd != document.forms[0].GADDRESS1.value) {
            if (!confirm("{rval MSG108}")) {
                return true;
            }
        }
    }

    if (cmd == "disp_clear") {
        for (i = 0; i < document.forms[0].elements.length; i++) {
            if (document.forms[0].elements[i].type == "select-one" || document.forms[0].elements[i].type == "text" || document.forms[0].elements[i].type == "checkbox") {
                if (document.forms[0].elements[i].type == "select-one") {
                    document.forms[0].elements[i].value = document.forms[0].elements[i].options[0].value;
                } else if (document.forms[0].elements[i].type == "checkbox") {
                    document.forms[0].elements[i].checked = false;
                } else {
                    document.forms[0].elements[i].value = "";
                }
            }
        }
        outputLAYER("label_name", "");
        outputLAYER("TOKU_TEST_ID", "");
        return false;
    }
    if (cmd == "copy") {
        var gadd2 = document.forms[0].GADDRESS2.value;

        document.forms[0].GZIPCD.value = document.forms[0].ZIPCD.value;
        document.forms[0].GADDRESS1.value = document.forms[0].ADDRESS1.value;
        document.forms[0].GADDRESS2.value = document.forms[0].ADDRESS2.value;

        if (gadd2 != document.forms[0].GADDRESS2.value) {
            vflg = true;
        }
        return false;
    }

    //読込中は、更新・削除ボタンをグレーアウト
    document.forms[0].btn_udpate.disabled = true;
    document.forms[0].btn_up_pre.disabled = true;
    document.forms[0].btn_up_next.disabled = true;

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function toTelNo(checkString) {
    var newString = "";
    var count = 0;
    for (i = 0; i < checkString.length; i++) {
        ch = checkString.substring(i, i + 1);
        if ((ch >= "0" && ch <= "9") || ch == "-") {
            newString += ch;
        }
    }

    if (checkString != newString) {
        alert("入力された値は不正な文字列です。\n電話(FAX)番号を入力してください。\n入力された文字列は削除されます。");
        // 文字列を返す
        return newString;
    }
    return checkString;
}

//日付チェック
function toDatecheck(index, obj, calno, spare, spare2, spare3) {
    var txtvalue = obj.value;
    switch (index) {
        case 0:
            if (txtvalue != "") {
                var num = calno.split(",");
                for (i = 0; i < num.length; i++) {
                    if (txtvalue == num[i]) {
                        setNameAD();
                        return false;
                    }
                }
                alert("{rval MSG901}\n正しい値を入力して下さい。");
                obj.value = "";
                setNameAD();
                return false;
            }
            setNameAD();
            break;
        case 1:
            if (txtvalue != "" && (isNaN(txtvalue) || txtvalue < 1 || txtvalue > 99)) {
                alert("{rval MSG901}\n年には1～99を入力して下さい。");
                obj.value = "";
                setNameAD();
                return false;
            }
            setNameAD();
            break;
        case 2:
            if (txtvalue != "" && (isNaN(txtvalue) || txtvalue < 1 || txtvalue > 12)) {
                alert("{rval MSG901}\n月には1～12を入力して下さい。");
                obj.value = "";
                setNameAD();
                return false;
            }
            setNameAD();
            break;
        case 3:
            if (txtvalue != "" && (isNaN(txtvalue) || txtvalue < 1 || txtvalue > 31)) {
                alert("{rval MSG901}\n日には1～31を入力して下さい。");
                obj.value = "";
                return false;
            }
            break;
    }
    return;
}

//和暦名を置き換える
function setName(obj) {
    var idx = obj.value;
    if (obj.value == "") {
        if (obj.name == "ERACD") {
            outputLAYER("DATEID", blank_name);
            document.forms[0].WNAME.value = "";
        } else if (obj.name == "FS_ERACD") {
            outputLAYER("FS_WNAME_ID", blank_name);
            document.forms[0].FS_WNAME.value = "";
        }
        return;
    }
    if (year_name[idx] != null) {
        if (obj.name == "ERACD") {
            outputLAYER("DATEID", year_name[idx]);
            document.forms[0].WNAME.value = year_name[idx];
        } else if (obj.name == "FS_ERACD") {
            outputLAYER("FS_WNAME_ID", year_name[idx]);
            document.forms[0].FS_WNAME.value = year_name[idx];
        }
        return;
    }
    return;
}

//西暦表示セット
function setNameAD() {
    var fsEraCd = document.forms[0].FS_ERACD.value;
    var fsY = document.forms[0].FS_Y.value;
    var fsM = document.forms[0].FS_M.value;
    var eraY = document.forms[0].ERCD_Y.value;
    var arrayEraCd = new Array();
    var setYear;
    var setFsYM;

    if (fsEraCd == "" || fsY == "" || fsM == "") {
        outputLAYER("FS_Y_M_INNER_ID", blank_name2);
    } else {
        arrayEraCd = eraY.split(":")[0].split(",");
        for (var i = 0; i < arrayEraCd.length; i++) {
            if (arrayEraCd[i] == fsEraCd) {
                setYear = parseInt(eraY.split(":")[1].split(",")[i]) + parseInt(fsY) - 1;
            }
        }
        setFsYM = setYear + "." + fsM;
        if (isNaN(setYear) || isNaN(fsM)) {
            outputLAYER("FS_Y_M_INNER_ID", blank_name2);
        } else {
            outputLAYER("FS_Y_M_INNER_ID", setFsYM);
        }
    }
    return;
}

//フォーカスが離れた時にコピー
function toCopytxt(index, txtvalue) {
    switch (index) {
        case 0:
            if (document.forms[0].GZIPCD.value == "") {
                document.forms[0].GZIPCD.value = txtvalue;
                return false;
            }
            break;
        case 1:
            if (document.forms[0].GADDRESS1.value == "") {
                document.forms[0].GADDRESS1.value = txtvalue;
                return false;
            }
            break;
        case 2:
            if (document.forms[0].GADDRESS2.value == "") {
                document.forms[0].GADDRESS2.value = txtvalue;
                return false;
            }
            break;
    }
}

//ボタンを押し不可にする
function btn_disabled() {
    document.forms[0].btn_udpate.disabled = true;
    document.forms[0].btn_up_pre.disabled = true;
    document.forms[0].btn_up_next.disabled = true;
}

//フォームの値が変更されたか判断する
function change_flg() {
    vflg = true;
}

//学校検索画面のプログラムで呼び出す関数を空定義
function current_cursor_focus() {}
function current_cursor_list() {}
