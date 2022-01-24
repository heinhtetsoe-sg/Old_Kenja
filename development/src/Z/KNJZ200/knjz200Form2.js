function btn_submit(cmd) {
    var jugyou_jisu_flg  = document.forms[0].JUGYOU_JISU_FLG;
    //法定欠課数上限値の入力チェック
    if (jugyou_jisu_flg.value != '2') {
        var absence_high     = document.forms[0].ABSENCE_HIGH;
        var get_absence_high = document.forms[0].GET_ABSENCE_HIGH;
        if (parseInt(absence_high.value) < parseInt(get_absence_high.value)) {
            alert('履修より修得の欠課数上限値が大きくなっています。');
            return false;
        }
        if (!checkDecimal(absence_high)) {
            return false;
        }
        if (!checkDecimal(get_absence_high)) {
            return false;
        }
    }

    if (cmd == "delete") {
        result = confirm('{rval MSG103}');
        if (result == false) {
            return false;
        }
    }
    if (cmd == 'reset'){
        if (!confirm('{rval MSG106}')){
            return false;
        }else{
            document.forms[0].cmd.value = cmd;
            document.forms[0].submit();
            return false;
        }
    }
    if (cmd == 'exec') {
        document.forms[0].encoding = "multipart/form-data";
        if (document.forms[0].OUTPUT[1].checked && document.forms[0].FILE.value == '') {
            alert('ファイルを指定してください');
            return false;
        }

        if (document.forms[0].OUTPUT[0].checked) {
            cmd = 'downloadHead';
        } else if (document.forms[0].OUTPUT[1].checked) {
            cmd = 'uploadCsv';
        } else if (document.forms[0].OUTPUT[2].checked) {
            cmd = 'downloadCsv';
        } else if (document.forms[0].OUTPUT[3].checked) {
            cmd = 'downloadError';
        } else {
            alert('ラジオボタンを選択してください。');
            return false;
        }
    }
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
function OnAuthError()
{
   alert('{rval MSG300}');
   closeWin();
}

function check(that) {
    //全角から半角
    that.value = toHankakuNum(that.value);
    //数値型へ変換
    that.value = toInteger(that.value);
}

//NO001
function Check_a(Name){

    flg = document.forms[0].AUTHORIZE_FLG.checked ;

    if(flg){
        document.all('check_a').innerHTML=document.all('check1_a').innerHTML;
    }else{
        document.all('check_a').innerHTML=document.all('check2_a').innerHTML;
    }

}
//NO001
function Check_c(Name){

    flg = document.forms[0].COMP_UNCONDITION_FLG.checked ;

    if(flg){
        document.all('check_c').innerHTML=document.all('check1_c').innerHTML;
    }else{
        document.all('check_c').innerHTML=document.all('check2_c').innerHTML;
    }

}

function checkDecimal(obj) {
    var decimalValue = obj.value
    var check_result = false;

    if (decimalValue != '') {
        //空じゃなければチェック
        if (decimalValue.match(/^[0-9]+(\.[0-9]+)?$/)) {
            check_result = true;
        }
    } else {
        check_result = true;
    }

    if (!check_result) {
        alert('数字を入力して下さい。');
    }

    //正しい値ならtrueを返す
    return check_result;
}

//移動
function Page_jumper(link) {
    if (!confirm('{rval MSG108}')) {
        return;
    }
    parent.location.href=link;
}

function changeRadio(obj) {
    var type_file;
    if (obj.value == '1') { //1は取り込み
        document.forms[0].FILE.disabled = false;
    } else {
        document.forms[0].FILE.disabled = true;
        type_file = document.getElementById('type_file'); //ファイルアップローダーの値を消す
        var innertString = type_file.innerHTML;
        type_file.innerHTML = innertString;
    }
}
