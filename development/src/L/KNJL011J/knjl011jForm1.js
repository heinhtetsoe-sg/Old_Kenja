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
//        outputLAYER('DISTCD_ID', blank_fsdistname);
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

//        outputLAYER('RPT01', '');
//        outputLAYER('RPT02', '');
//        outputLAYER('RPT03', '');
//        outputLAYER('RPT04', '');
//        outputLAYER('RPT05', '');
//        outputLAYER('RPT06', '');
//        outputLAYER('RPT07', '');
//        outputLAYER('RPT08', '');
//        outputLAYER('RPT09', '');
//        outputLAYER('RPT10', '');
//        outputLAYER('RPT11', '');
//        outputLAYER('RPT12', '');
//        outputLAYER('AVERAGE5', '');
//        outputLAYER('AVERAGE_ALL', '');
//        outputLAYER('TOTAL_ALL', '');
//        outputLAYER('KASANTEN_ALL', '');
//        outputLAYER('ABSENCE_DAYS', '');

        return false;
    }
    if (cmd == 'copy') {
        var gadd2 = document.forms[0].GADDRESS2.value;

        document.forms[0].GZIPCD.value = document.forms[0].ZIPCD.value;
        document.forms[0].GADDRESS1.value = document.forms[0].ADDRESS1.value;
        document.forms[0].GADDRESS2.value = document.forms[0].ADDRESS2.value;

        if ((gadd2 != document.forms[0].GADDRESS2.value)) {
                vflg = true;
        }
        return false;
    }
    if (cmd == 'gtelno_copy') {
        document.forms[0].GFAXNO.value = document.forms[0].GTELNO.value;
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
function toDatecheck(index, txtvalue, calno, spare, spare2, spare3)
{
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
                document.forms[0].ERACD.value = "";
                return false;
            }
            break;
        case 1:
            if (txtvalue != "" && txtvalue < 1 || txtvalue > 99) {
                alert('{rval MSG901}\n年には1～99を入力して下さい。');
                document.forms[0].BIRTH_Y.value = "";
                return false;
            }
            break;
        case 2:
            if (txtvalue != "" && txtvalue < 1 || txtvalue > 12) {
                alert('{rval MSG901}\n月には1～12を入力して下さい。');
                document.forms[0].BIRTH_M.value = "";
                return false;
            }
            break;
        case 3:
            if (txtvalue != "" && txtvalue < 1 || txtvalue > 31) {
                alert('{rval MSG901}\n日には1～31を入力して下さい。');
                document.forms[0].BIRTH_D.value = "";
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
        outputLAYER('DATEID', blank_name);
        document.forms[0].WNAME.value = "";
        return;
    }
    if (year_name[idx] != null) {
        outputLAYER('DATEID', year_name[idx]);
        document.forms[0].WNAME.value = year_name[idx];
        return;
    }
    return;
}

//設立名を置き換える
function setFsDistName(obj)
{
    var idx = obj.selectedIndex;
    if (obj.value == '' || fsdistname[idx] == undefined) {
        outputLAYER('DISTCD_ID', blank_fsdistname);
        return;
    }
    if (fsdistname[idx] != null) {
        outputLAYER('DISTCD_ID', fsdistname[idx]);
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
        case 4:
            if (document.forms[0].GFAXNO.value == "") {
                document.forms[0].GFAXNO.value = txtvalue;
                return false;
            }
            break;
    }
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

//受験型を初期表示する
function setExamType(obj, number)
{
        if (number == '1') document.forms[0].EXAM_TYPE1.value = (obj.checked) ? "1" : "";
        if (number == '2') document.forms[0].EXAM_TYPE2.value = (obj.checked) ? "1" : "";
        if (number == '3') document.forms[0].EXAM_TYPE3.value = (obj.checked) ? "1" : "";
        if (number == '4') document.forms[0].EXAM_TYPE4.value = (obj.checked) ? "2" : "";
        if (number == '5') document.forms[0].EXAM_TYPE5.value = (obj.checked) ? "3" : "";
        if (number == '6') document.forms[0].EXAM_TYPE6.value = (obj.checked) ? "2" : "";
        return;
}
