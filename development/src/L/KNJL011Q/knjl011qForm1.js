function btn_submit(cmd, zip, gzip, zadd, gadd) {
    if (cmd == 'delete'){
        if (!confirm('{rval MSG103}'))
            return false;
    }
    //新規ボタン
    if (cmd == 'addnew'){
        if (document.forms[0].APPLICANTDIV.value == "") {
            alert('{rval MSG301}\n( 入試制度 )');
            return true;
        }
    }
    //取込ボタン（実践模試データ）
    if (cmd == 'j_torikomi'){
        if (document.forms[0].JIZEN_BANGOU.value == "") {
            alert('{rval MSG301}\n( 実践模試受験番号 )');
            return true;
        }
    }
    if (cmd == 'reset' && !confirm('{rval MSG106}')) {
        return true;
    }
    if (cmd == 'reference' || cmd == 'back1' || cmd == 'next1') {
        if (document.forms[0].EXAMNO.value == '') {
            alert('{rval MSG301}\n( 受験番号 )');
            return true;
        }
        if (vflg == true) {
            if (!confirm('{rval MSG108}')) {
                return true;
            }
        }
    }

    if (cmd == 'disp_clear') {
        vflg = false;
        for (i = 0; i < document.forms[0].elements.length; i++) {
            if (document.forms[0].elements[i].type == 'select-one' || document.forms[0].elements[i].type == 'text' || document.forms[0].elements[i].type == 'checkbox') {
                if (document.forms[0].elements[i].type == 'select-one') {
                    document.forms[0].elements[i].value = document.forms[0].elements[i].options[0].value;
                } else if (document.forms[0].elements[i].type == 'checkbox') {
                    document.forms[0].elements[i].checked = false;
                } else {
                    document.forms[0].elements[i].value = "";
                }
            }
        }
        outputLAYER('GENERAL_FLG_ID', '');
        outputLAYER('KUBUN_ID', '');
        outputLAYER('FINSCHOOLNAME_ID', '');
        outputLAYER('SH_SCHOOLNAME1_ID', '');
        outputLAYER('SH_SCHOOLNAME2_ID', '');
        outputLAYER('SH_SCHOOLNAME3_ID', '');
        outputLAYER('SH_SCHOOLNAME4_ID', '');
        return false;
    }
    if (cmd == 'copy') {
        var gadd2 = document.forms[0].GADDRESS2.value;
        var gtel = document.forms[0].GTELNO.value;

        document.forms[0].GZIPCD.value = document.forms[0].ZIPCD.value;
        document.forms[0].GADDRESS1.value = document.forms[0].ADDRESS1.value;
        document.forms[0].GADDRESS2.value = document.forms[0].ADDRESS2.value;
        document.forms[0].GTELNO.value = document.forms[0].TELNO.value;

        if ((gadd2 != document.forms[0].GADDRESS2.value) || (gtel != document.forms[0].GTELNO.value)) {
                vflg = true;
        }
        return false;
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function toTelNo(checkString) {
    var newString = "";
    var count = 0;
    for (i = 0; i < checkString.length; i++) {
        ch = checkString.substring(i, i+1);
        if ((ch >= "0" && ch <= "9") || (ch == "-")) {
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
    switch(index) {
        case 0:
            if (txtvalue != "") {
                var num = calno.split(",");
                for (i = 0; i < num.length; i++) {
                    if (txtvalue == num[i]) {
                        return false;
                    }
                }
                alert('{rval MSG901}\n正しい値を入力して下さい。');
                obj.value = "";
                return false;
            }
            break;
        case 1:
            if (txtvalue != "" && txtvalue < 1 || txtvalue > 99) {
                alert('{rval MSG901}\n年には1～99を入力して下さい。');
                obj.value = "";
                return false;
            }
            break;
        case 2:
            if (txtvalue != "" && txtvalue < 1 || txtvalue > 12) {
                alert('{rval MSG901}\n月には1～12を入力して下さい。');
                obj.value = "";
                return false;
            }
            break;
        case 3:
            if (txtvalue != "" && txtvalue < 1 || txtvalue > 31) {
                alert('{rval MSG901}\n日には1～31を入力して下さい。');
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
    if (obj.value == '') {
        if (obj.name == 'ERACD') {
            outputLAYER('DATEID', blank_name);
            document.forms[0].WNAME.value = "";

        } else if (obj.name == 'FS_ERACD') {
            outputLAYER('FS_WNAME_ID', blank_name);
            document.forms[0].FS_WNAME.value = "";

        }
        return;
    }
    if (year_name[idx] != null) {
        if (obj.name == 'ERACD') {
            outputLAYER('DATEID', year_name[idx]);
            document.forms[0].WNAME.value = year_name[idx];

        } else if (obj.name == 'FS_ERACD') {
            outputLAYER('FS_WNAME_ID', year_name[idx]);
            document.forms[0].FS_WNAME.value = year_name[idx];

        }
        return;
    }
    return;
}

//フォーカスが離れた時にコピー
function toCopytxt(index, txtvalue) {
    switch(index) {
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
        case 3:
            if (document.forms[0].GTELNO.value == "") {
                document.forms[0].GTELNO.value = txtvalue;
                return false;
            }
            break;
    }
}

//ボタンを押し不可にする
function btn_disabled() {
    document.forms[0].btn_ao.disabled = true;
    document.forms[0].btn_chousasho.disabled = true;
    document.forms[0].btn_udpate.disabled = true;
    document.forms[0].btn_up_pre.disabled = true;
    document.forms[0].btn_up_next.disabled = true;
    document.forms[0].btn_del.disabled = true;
}

//フォームの値が変更されたか判断する
function change_flg() {
    vflg = true;
}

//フォームの値が変更されたか判断する＋「1:見込み」を選択したら
function changeAndSet_flg() {
    vflg = true;
    //「1:見込み」を選択したら
    if (document.forms[0].FS_GRDDIV.value == "1" && document.forms[0].FS_Y.value == "" && document.forms[0].FS_M.value == "") {
        document.forms[0].FS_Y.value = document.forms[0].SET_WAREKI.value;
        document.forms[0].FS_M.value = "03";
    }
}

//調査書登録ボタン・欠席登録ボタンの移動確認
function Page_jumper(link) {
    parent.location.href=link;
}

// Enterキーが押されたときに「TABキーが押された」イベントにするメソッド
function keyChangeEntToTab(obj) {
    // Ent13
    var e = window.event;
    if (e.keyCode != 13) {
        return;
    }

    for (var f = 0; f < document.forms[0].length; f++) {
        if (document.forms[0][f].name == obj.name) {
            var targetObject = document.forms[0][(f + 1)];
            for (var b = 1; b < document.forms[0].length - f; b++) {
                if (obj.name == 'SH_SCHOOLCD1') {
                    targetObject = document.forms[0].SH_SCHOOLCD2;
                    break;
                } else if (obj.name == 'SH_SCHOOLCD2') {
                    targetObject = document.forms[0].SH_SCHOOLCD3;
                    break;
                } else if (obj.name == 'SH_SCHOOLCD3') {
                    targetObject = document.forms[0].SH_SCHOOLCD4;
                    break;
                } else if (targetObject.name == 'btn_add') {
                    return;
                } else if (targetObject.type != 'text' || targetObject.name == 'GKANA') {//テキスト以外はフォーカスしない
                    targetObject = document.forms[0][(f + b)];
                } else {
                    break;
                }
            }
            targetObject.focus();
            return;
        }
    }
}
