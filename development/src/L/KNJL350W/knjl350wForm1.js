function btn_submit(cmd) {
    if (!document.forms[0].SCHOOLCD.value) {
        OnSchoolcdError();
        return false;
    }
    document.forms[0].encoding = "multipart/form-data";
    if (cmd == 'reset') {
        if (!confirm('{rval MSG106}')) {
            return false;
        }
    }

    //CSV
    if (cmd == 'exec') {
        if (document.forms[0].OUTPUT[1].checked && document.forms[0].FILE.value == '') {
            alert('ファイルを指定してください');
            return false;
        }

        if (document.forms[0].OUTPUT[1].checked) {
            cmd = 'uploadCsv';
        } else if (document.forms[0].OUTPUT[2].checked) {
            cmd = 'downloadCsv';
        } else if (document.forms[0].OUTPUT[0].checked) {
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

function OnAuthError() {
    alert('{rval MSG300}');
    closeWin();
}

function OnSchoolcdError() {
    var chkYear = document.forms[0].KYOUIKU_IINKAI_SCHOOLCD_YEAR.value;
    alert(chkYear + '年度の教育委員会統計用学校番号が未設定です。\n学校マスタの他条件設定を確認してください。');
}

function changeColor(objName) {
    document.getElementById('ID_' + objName).style.backgroundColor = "pink";
}

//印刷
function newwin(SERVLET_URL){
    action = document.forms[0].action;
    target = document.forms[0].target;

//    url = location.hostname;
//    document.forms[0].action = "http://" + url +"/cgi-bin/printenv.pl";
    document.forms[0].action = SERVLET_URL +"/KNJL";
    document.forms[0].target = "_blank";
    document.forms[0].submit();

    document.forms[0].action = action;
    document.forms[0].target = target;
}

function scrollRC(){
    if (document.getElementById('trow') != null) {
        document.getElementById('trow').scrollLeft = document.getElementById('tbody').scrollLeft;
    }
    if (document.getElementById('tcol') != null) {
        document.getElementById('tcol').scrollTop = document.getElementById('tbody').scrollTop;
    }
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
