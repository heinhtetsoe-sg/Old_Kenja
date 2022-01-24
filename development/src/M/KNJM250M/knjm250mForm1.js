function btn_submit(cmd) {
    document.forms[0].encoding = "multipart/form-data";

    var str = new Object();
    if(cmd == 'reset' && !confirm('{rval MSG106}'))  return true;

    //前年度コピー
    if (cmd == 'copy' && !confirm('{rval MSG101}')) {
        return false;
    }

    if (cmd == 'exec') {
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

function setName(obj, rowid, flg) {
    Setflg(obj);    //バックカラーを黄色に変更
    var idx = obj.value;
    if (obj.value == '') {
        if (flg == '0') {
            outputLAYER('STATE_NAME' + rowid, '');
        } else {
            outputLAYER('PERIOD_NAME' + rowid, '');
        }
        return;
    }

    if (flg == '0') {
        if (state_name[idx] != null) {
            outputLAYER('STATE_NAME' + rowid, state_name[idx]);
            if (obj.value == 0){
                document.forms[0].elements[obj.id*2+1].value = '*';
                document.forms[0].elements[obj.id*2+1].readOnly = true ;
                document.forms[0].elements[obj.id*2+2].focus();
            }else {
                if (document.forms[0].elements[obj.id*2+1].value == '*') document.forms[0].elements[obj.id*2+1].value = '';
                document.forms[0].elements[obj.id*2+1].focus();
                document.forms[0].elements[obj.id*2+1].readOnly = false ;
            }
        } else {
            alert('状態区分を指定して下さい。');
            outputLAYER('STATE_NAME' + rowid, '');
            obj.value = '';
            if (obj.id == 0) {
                document.forms[0].elements[obj.id+1].focus();   //OnChangeでは、フォーカスの移動が直接できないので
            } else {
                document.forms[0].elements[obj.id-1].focus();   //OnChangeでは、フォーカスの移動が直接できないので
            }
            obj.focus();                                        //一旦他のテキストへ移動後、再度フォーカスを設定する。
        }
    } else {
        if (pcnt_name[idx] != null) {
            outputLAYER('PERIOD_NAME' + rowid, pcnt_name[idx]);
        } else {
            //document.forms[0].elements[obj.id*2].value：現在行の状態コード
            if(document.forms[0].elements[obj.id*2].value == 0 && obj.value == '*'){
                outputLAYER('PERIOD_NAME' + rowid, '');
            }else if(document.forms[0].elements[obj.id*2].value == 0 && obj.value != '*'){
                alert('校時ＣＤが不正です。\n\n状態【0】は、【*】を指定して下さい。');
                outputLAYER('PERIOD_NAME' + rowid, '');
                obj.value = '';
                if (obj.id == 0) {
                    document.forms[0].elements[0].focus();          //OnChangeでは、フォーカスの移動が直接できないので
                } else {
                    document.forms[0].elements[obj.id-1].focus();   //OnChangeでは、フォーカスの移動が直接できないので
                }
                obj.focus();                                        //一旦他のテキストへ移動後、再度フォーカスを設定する。
            }else {
                alert('校時ＣＤが不正です。');
                outputLAYER('PERIOD_NAME' + rowid, '');
                obj.value = '';
                if (obj.id == 0) {
                    document.forms[0].elements[0].focus();          //OnChangeでは、フォーカスの移動が直接できないので
                } else {
                    document.forms[0].elements[obj.id-1].focus();   //OnChangeでは、フォーカスの移動が直接できないので
                }
                obj.focus();                                        //一旦他のテキストへ移動後、再度フォーカスを設定する。
            }
        }
    }
    return;
}

function Setflg(obj) {
    change_flg = true;
    if (obj.id){
        obj.style.background="yellow";
        document.getElementById('ROWID' + obj.id).style.background="yellow";
    }
}
function check(obj){
    if (getByte(obj.value) > 40){
        alert("全角２０、半角６０文字以内で入力してください。");
        obj.focus();
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