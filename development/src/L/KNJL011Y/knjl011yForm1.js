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

    if (cmd == 'disp_clear' || cmd == 'showdiv') {
//        outputLAYER('DATEID', blank_name);
        for (i = 0; i < document.forms[0].elements.length; i++) {
            if (document.forms[0].elements[i].type == 'select-one' || document.forms[0].elements[i].type == 'text' || document.forms[0].elements[i].type == 'checkbox') {
                if (document.forms[0].elements[i].name == 'APPLICANTDIV') {
                    // 入試制度はクリアしない
                } else if (document.forms[0].elements[i].type == 'select-one') {
                    document.forms[0].elements[i].value = document.forms[0].elements[i].options[0].value;
                } else if (document.forms[0].elements[i].type == 'checkbox') {
                    document.forms[0].elements[i].checked = false;
                } else {
                    document.forms[0].elements[i].value = "";
                }
            }
        }

        outputLAYER('RPT01', '');
        outputLAYER('RPT02', '');
        outputLAYER('RPT03', '');
        outputLAYER('RPT04', '');
        outputLAYER('RPT05', '');
        outputLAYER('RPT06', '');
        outputLAYER('RPT07', '');
        outputLAYER('RPT08', '');
        outputLAYER('RPT09', '');
        outputLAYER('RPT10', '');
        outputLAYER('AVERAGE_ALL', '');
        outputLAYER('AVERAGE5', '');
        outputLAYER('ABSENCE_DAYS1', '');
        outputLAYER('ABSENCE_DAYS2', '');
        outputLAYER('ABSENCE_DAYS3', '');

        if (cmd == 'disp_clear') return false;
    }
    if (cmd == 'copy') {
        var gadd2 = document.forms[0].GADDRESS2.value;

        document.forms[0].GZIPCD.value = document.forms[0].ZIPCD.value;
        document.forms[0].GADDRESS1.value = document.forms[0].ADDRESS1.value;
        document.forms[0].GADDRESS2.value = document.forms[0].ADDRESS2.value;
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

//ボタンを押し不可にする
function disabledCommon(div, val) {
    //専併区分コンボ変更時
    if (div == 'shdiv') {
        //併願校検索ボタン
        if (val == '2') {
            document.forms[0].btn_fin_high_kana_reference.disabled = false;
            document.forms[0].SH_SCHOOLCD.disabled = false;
        } else {
            document.forms[0].btn_fin_high_kana_reference.disabled = true;
            document.forms[0].SH_SCHOOLCD.disabled = true;
        }
    }
    //性別コンボ変更時
    if (div == 'sex') {
        //入寮チェックボックス
        if (val == '2') {
            document.forms[0].DORMITORY_FLG.disabled = false;
        } else {
            document.forms[0].DORMITORY_FLG.disabled = true;
        }
    }
    //一般入試希望チェックボックスクリック時
    if (div == 'general_flg') {
        if (document.forms[0].GENERAL_FLG.checked) {
            document.forms[0].GENERAL_DESIREDIV.disabled = false;
            document.forms[0].GENERAL_SHDIV.disabled = false;
            if (!document.forms[0].DIS_SELECT_SUBCLASS_DIV.value) {
                document.forms[0].SELECT_SUBCLASS_DIV.disabled = false;
            }
        } else {
            document.forms[0].GENERAL_DESIREDIV.disabled = true;
            document.forms[0].GENERAL_SHDIV.disabled = true;
            document.forms[0].SELECT_SUBCLASS_DIV.disabled = true;
        }
    }
    //一般入試希望チェックボックスクリック時
    if (div == 'general_flg2') {
        if (document.forms[0].GENERAL_FLG2.checked) {
            document.forms[0].GENERAL_DESIREDIV2.disabled = false;
            document.forms[0].GENERAL_SHDIV2.disabled = false;
        } else {
            document.forms[0].GENERAL_DESIREDIV2.disabled = true;
            document.forms[0].GENERAL_SHDIV2.disabled = true;
        }
    }
    //適性検査型希望チェックボックスクリック時
    if (div == 'general_flg3') {
        if (document.forms[0].GENERAL_FLG3.checked) {
            document.forms[0].GENERAL_DESIREDIV3.disabled = false;
            document.forms[0].GENERAL_SHDIV3.disabled = false;
        } else {
            document.forms[0].GENERAL_DESIREDIV3.disabled = true;
            document.forms[0].GENERAL_SHDIV3.disabled = true;
        }
    }
    //スライド希望チェックボックスクリック時
    if (div == 'slide_flg') {
        if (document.forms[0].SLIDE_FLG.checked) {
            document.forms[0].SLIDE_DESIREDIV.disabled = false;
//            document.forms[0].SLIDE_DESIREDIV.options[1].selected = true;
        } else {
            document.forms[0].SLIDE_DESIREDIV.disabled = true;
//            document.forms[0].SLIDE_DESIREDIV.options[0].selected = true;
        }
    }
}

//ボタンを押し不可にする
function btn_disabled() {
    document.forms[0].btn_udpate.disabled = true;
    document.forms[0].btn_up_pre.disabled = true;
    document.forms[0].btn_up_next.disabled = true;
    document.forms[0].btn_del.disabled = true;
}

//フォームの値が変更されたか判断する
function change_flg() {
    vflg = true;
}

//推薦事項の説明をチップヘルプで表示
function ViewcdMousein(msg_no){
    var msg = "";
    if (msg_no==1) msg = "(1) 学業成績の優秀な者";
    if (msg_no==2) msg = "(2) 英検４級合格者及び同等の資格を有する者";
    if (msg_no==3) msg = "(3) スポーツに優れた実績をもつ者";
    if (msg_no==4) msg = "(4) 芸術その他に優れた才能があり、自己推薦できる者";

    x = event.clientX+document.body.scrollLeft;
    y = event.clientY+document.body.scrollTop;
    document.all("lay").innerHTML = msg;
    document.all["lay"].style.position = "absolute";
    document.all["lay"].style.left = x+5;
    document.all["lay"].style.top = y+10;
    document.all["lay"].style.padding = "4px 3px 3px 8px";
    document.all["lay"].style.border = "1px solid";
    document.all["lay"].style.visibility = "visible";
    document.all["lay"].style.background = "#ccffff";
}

function ViewcdMouseout(){
    document.all["lay"].style.visibility = "hidden";
}
