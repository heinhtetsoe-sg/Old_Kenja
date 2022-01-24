function btn_submit(cmd, zip, gzip, zadd, gadd) 
{
    if (cmd == 'delete'){
        if (!confirm('{rval MSG103}'))
            return false;
    }
    if (cmd == 'reset' && !confirm('{rval MSG106}')) {
        return true;
    }
    if (cmd == 'reference' || cmd == 'back1' || cmd == 'next1') {
        if (document.forms[0].EXAMNO.value == '') {
            alert('{rval MSG301}\n( 受験番号 )');
            return true;
        }
        if (vflg == true || zip != document.forms[0].ZIPCD.value || gzip != document.forms[0].GZIPCD.value ||
            zadd != document.forms[0].ADDRESS1.value || gadd != document.forms[0].GADDRESS1.value) {
            if (!confirm('{rval MSG108}')) {
                return true;
            }
        }
    }

    if (cmd == 'disp_clear') {
        outputLAYER('DATEID', blank_name);
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


        return false;
    }
    if (cmd == 'copy') {
        var gadd2 = document.forms[0].GADDRESS2.value;

        document.forms[0].GZIPCD.value = document.forms[0].ZIPCD.value;
        document.forms[0].GADDRESS1.value = document.forms[0].ADDRESS1.value;
        document.forms[0].GADDRESS2.value = document.forms[0].ADDRESS2.value;
        document.forms[0].GPREF_CD.value = document.forms[0].PREF_CD.value;
        document.forms[0].GTELNO.value = document.forms[0].TELNO.value;

        if ((gadd2 != document.forms[0].GADDRESS2.value)) {
                vflg = true;
        }
        return false;
    }
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function toTelNo(checkString){
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
function toDatecheck(index, obj, calno, spare, spare2, spare3)
{
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
function setName(obj)
{
    var idx = obj.value;
    if (obj.value == '') {
        if (obj.name == 'ERACD') {
            outputLAYER('DATEID', blank_name);
            document.forms[0].WNAME.value = "";

        } else if (obj.name == 'FS_ERACD') {
            outputLAYER('FS_WNAME_ID', blank_name);
            document.forms[0].FS_WNAME.value = "";

        } else if (obj.name == 'PICTURE_ERACD') {
            outputLAYER('PICTURE_WNAME_ID', blank_name);
            document.forms[0].PICTURE_WNAME.value = "";
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

        } else if (obj.name == 'PICTURE_ERACD') {
            outputLAYER('PICTURE_WNAME_ID', year_name[idx]);
            document.forms[0].PICTURE_WNAME.value = year_name[idx];
        }
        return;
    }
    return;
}

//フォーカスが離れた時にコピー
function toCopytxt(index, txtvalue)
{
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

//学園バス利用する場合、有効。(乗降地、ご利用人数)
function disBusUse(obj) {
    useFlg = true;
    if (obj.checked) {
        useFlg = false;
    }
    document.forms[0].STATIONDIV[0].disabled = useFlg;
    document.forms[0].STATIONDIV[1].disabled = useFlg;
    document.forms[0].STATIONDIV[2].disabled = useFlg;
    document.forms[0].BUS_USER_COUNT.disabled = useFlg;
}

//ボタンを押し不可にする
function btn_disabled() {
    document.forms[0].btn_copy.disabled = true;
    document.forms[0].btn_udpate.disabled = true;
    document.forms[0].btn_up_pre.disabled = true;
    document.forms[0].btn_up_next.disabled = true;
    document.forms[0].btn_del.disabled = true;
}

//フォームの値が変更されたか判断する
function change_flg() {
    vflg = true;
}
