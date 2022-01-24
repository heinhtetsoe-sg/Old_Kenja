function btn_submit(cmd) {
    var str = new Object();
    if(cmd == 'reset' && !confirm('{rval MSG106}'))  return true;
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function dateChange(kai, dateValue) {

    if (isDate2(dateValue)){
        var startDate = new Date(dateValue);
        //期限日の次月末日付
        var nmonth_EndDate = new Date(startDate.getFullYear(), startDate.getMonth() + 2, 0);

        //１か月後の日付を設定
        var endDate = new Date(startDate.getFullYear(), startDate.getMonth() + 1, startDate.getDate());
        if (endDate > nmonth_EndDate){
            endDate = nmonth_EndDate;
        }
        //再提出日の曜日を取得(土日ならずらす)
        var dval = endDate.getDay();
        if (dval == 6) {
            endDate.setDate(endDate.getDate() + 2);
        }
        if (dval == 0) {
            endDate.setDate(endDate.getDate() + 1);
        }
        //再提出期限日を設定
        var mm = endDate.getMonth() + 1;
        if (mm < 10){ mm = "0" + mm; }
        var dd = endDate.getDate();
        if (dd < 10){ dd = "0" + dd; }
        var endDateElement = document.getElementsByName('RETRYDAY'+kai)[0];
        endDateElement.value = endDate.getFullYear() + "/" + mm + "/" + dd;
    }
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
