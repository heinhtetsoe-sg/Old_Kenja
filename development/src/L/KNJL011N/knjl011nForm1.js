function btn_submit(cmd, zip, gzip, zadd, gadd) 
{
    if (cmd == 'delete'){
        if (!confirm('{rval MSG103}'))
            return false;
    }
    //新規ボタン
    if (cmd == 'addnew'){
        if (document.forms[0].TESTDIV.value == "" ||
            document.forms[0].SHDIV.value == "" ||
            document.forms[0].EXAMCOURSE.value == "") {
            alert('{rval MSG301}\n( 入試区分 )( 専併区分 )( 志望区分 )');
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
        if (vflg == true || zip != document.forms[0].ZIPCD.value || gzip != document.forms[0].GZIPCD.value ||
            zadd != document.forms[0].ADDRESS1.value || gadd != document.forms[0].GADDRESS1.value) {
            if (!confirm('{rval MSG108}')) {
                return true;
            }
        }
    }

    if (cmd == 'disp_clear') {
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
    if (cmd == 'copy_shougaku') {
        var shougaku5 = document.forms[0].SHOUGAKU5.value;
        var remark5 = document.forms[0].REMARK5.value;

        document.forms[0].SHOUGAKU5.value = document.forms[0].SHOUGAKU1.value;
        document.forms[0].REMARK5.value = document.forms[0].REMARK1.value;

        if ((shougaku5 != document.forms[0].SHOUGAKU5.value) || (remark5 != document.forms[0].REMARK5.value)) {
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
function btn_disabled() {
    document.forms[0].btn_kesseki.disabled = true;
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

//調査書登録ボタン・欠席登録ボタンの移動確認
function Page_jumper(link)
{
    if (!confirm('{rval MSG108}')) {
        return;
    }
    parent.location.href=link;
}