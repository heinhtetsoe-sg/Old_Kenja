function btn_submit(cmd, zip, gzip, zadd, gadd) 
{
    if (cmd == 'delete'){
        if (!confirm('{rval MSG103}'))
            return false;
    }
    if (cmd == 'reset' && !confirm('{rval MSG106}')) {
        return true;
    }
    //検索時のチェック
    if (cmd == 'reference' || cmd == 'back1' || cmd == 'next1') {
        if (document.forms[0].EXAMNO.value == '') {
            alert('{rval MSG301}\n( 受験番号 )');
            return true;
        }
        cflg = document.forms[0].cflg.value;
        if (vflg == true || cflg == 'true' || zip != document.forms[0].ZIPCD.value ||
            gzip != document.forms[0].GZIPCD.value ||zadd != document.forms[0].ADDRESS.value || 
            gadd != document.forms[0].GADDRESS.value) {
            if (!confirm('{rval MSG108}')) {
                return true;
            }
        }
    }
    
    if (cmd == 'disp_clear') {
        //outputLAYER('label_birthday', '');
        outputLAYER('label_birthday', '&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;');
        for (i = 0; i < document.forms[0].elements.length; i++) {
            if (document.forms[0].elements[i].type == 'select-one' || document.forms[0].elements[i].type == 'text') {
                if (document.forms[0].elements[i].type == 'select-one') {
                    document.forms[0].elements[i].value = document.forms[0].elements[i].options[0].value;
                } else {
                    document.forms[0].elements[i].value = "";
                }
            }
        }
        document.forms[0].btn_copy.disabled = true;
        document.forms[0].btn_udpate.disabled = true;
        document.forms[0].btn_up_pre.disabled = true;
        document.forms[0].btn_up_next.disabled = true;
        document.forms[0].btn_del.disabled = true;
        return false;
    }
    if (cmd == 'copy') {
        var gtel = document.forms[0].GTELNO.value;   
        document.forms[0].GZIPCD.value = document.forms[0].ZIPCD.value;
        document.forms[0].GADDRESS.value = document.forms[0].ADDRESS.value;
        document.forms[0].GTELNO.value = document.forms[0].TELNO.value;
        
        if (gtel != document.forms[0].GTELNO.value) {
                vflg = true;
        }
        return false;
    }
    if (cmd == 'reload2') {
        document.forms[0].cflg.value = 'true';
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
        alert("入力された値は不正な文字列です。\n電話番号を入力してください。\n入力された文字列は削除されます。");
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
            if (document.forms[0].GADDRESS.value == "") {
                document.forms[0].GADDRESS.value = txtvalue;
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

function setWareki(obj, ymd)
{
    var d = ymd;
    var tmp = d.split('/');
    var ret = Calc_Wareki(tmp[0],tmp[1],tmp[2]);
}

function setWarekiName(obj)
{
    if (obj.value == '') {
        //outputLAYER('label_birthday', '');
        outputLAYER('label_birthday', '&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;');
    }
    return;
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
function check_approval(obj){
    if (obj.value != ""){
        document.forms[0].CHK_APPROVAL.disabled = false;
    }else{
        document.forms[0].CHK_APPROVAL.disabled = true;
        document.forms[0].CHK_APPROVAL.checked = false;
    }
}