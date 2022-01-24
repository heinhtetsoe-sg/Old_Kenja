function btn_submit(cmd) {

    if (cmd == 'delete' || cmd == 'update') {
        if (document.forms[0].USERSCD.value == '') {
            alert('{rval MSG308}');
            return true;
        }
    }

    if (cmd == 'delete' && !confirm('{rval MSG103}')) {
        return true;
    }

    if (cmd == 'update') {
        if (pwdMojiCheck(document.forms[0].PASSWD)) {
            return true;
        }

        var flug = document.forms[0].PASSWD.value;
        var flug2;
        flug2 = flug.indexOf("*",0);
        if (flug2 > -1) {
            if (flug == '**********') {
            } else {
                alert('{rval MSG901} \n\r記号(*)があるため、パスワードは更新されません。');
                return true;
            }
        }
    }

    if (cmd == 'reset') {
        if (document.forms[0].USERSCD.value == '') {
            alert('{rval MSG308}');
            return true;
        }
        if (!confirm('{rval MSG106}')) {
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

function CheckDispVal(obj, layName) {

    flg = obj.checked;
    if (flg) {
        document.getElementById(layName).style.visibility = 'visible'
    } else {
        document.getElementById(layName).style.visibility = 'hidden'
    }

}

function moji_hantei(that) {
    kekka = 0;
    moji = that.value;
    for (i = 0; i < moji.length; i++) {
        dore = escape(moji.charAt(i));
        if (navigator.appName.indexOf("Netscape") != -1) {
            if (dore.length>3 && dore.indexOf("%")!=-1) {
            }
        } else {
            if (dore.indexOf("%uFF") != -1 && '0x' + dore.substring(2,dore.length) < 0xFF60) {
                kekka++;
            } else if (moji.match(/\W/g) != null && dore.length == 6) {
                kekka++;
            }
        }
    }
    if (kekka > 0) {
        alert("全角文字が含まれています。");
        srch='';
        that.value=moji.replace(/\W/g, srch);
    }

}
//パスワードの桁数、文字チェック（8桁以上、英数字&大文字小文字の混在）
function pwdMojiCheck(that) {
    str = that.value;
    if (str == '' || str == '**********') {
        return false;
    }

    if (str.length >= 8 &&
        str.match(/[0-9]/g) != null &&
        str.match(/[a-z]/g) != null &&
        str.match(/[A-Z]/g) != null) {
    } else {
        alert("パスワードの桁数、文字が不正です。\n【8桁以上、英数字&大文字小文字の混在】");
        return true;
    }

    return false;
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
