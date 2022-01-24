function btn_submit(cmd) {
    //編集中フラグON
    if (cmd == 'calc') {
        setupFlgOn();
    } else {
        //編集中かどうかの確認
        if (cmd != 'update' && !setupFlgCheck()) return;
    }
    //編集中フラグOFF
    if (cmd == 'reset') {
        setupFlgOff();
    }
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

//編集フラグ 1:編集中
function setupFlgOn() {
    document.forms[0].setupFlg.value = "1";
}
function setupFlgOff() {
    document.forms[0].setupFlg.value = "";
    document.forms[0].btn_calc.disabled = false;
    document.forms[0].btn_update.disabled = true;
    document.forms[0].btn_reset.disabled = true;
}
function setupFlgCheck() {
    setupFlg = document.forms[0].setupFlg.value;
    if (setupFlg == "1" && !confirm('{rval MSG108}')) {
        return false;
    }
    setupFlgOff();
    return true;
}

function setupFlgCheck2(obj, defval, d086) {
    setupFlg = document.forms[0].setupFlg.value;
    if (setupFlg == "1" && !confirm('{rval MSG108}')) {
        obj.value = defval;
        return false;
    }
    setupFlgOff();
    if (!inputCheck2(obj, d086)) {
        return false;
    }
    return true;
}

function inputCheck2(obj, d086) {
    var AssesList = document.forms[0].HID_ASSESHIGH.value.split(',');
    var arr = document.forms[0].HID_UNITINFO.value.split(',');
    var hdiOmomi = document.forms[0].HID_OMOMI.value.split(',');

    if (obj.value != null && obj.value != "") {
        if (!isFinite(obj.value)) {
            alert('{rval MSG907}');
            return false;
        }
        var regex = new RegExp(/^[0-9]+$/);
        if (!regex.test(obj.value)) {
            alert('{rval MSG907}');
            return false;
        }
        if (obj.value < 0 || 100 < obj.value ) {
            alert("{rval MSG914}" + "0～100の範囲で設定してください。");
            return false;
        }
        for(var cntval = 0; cntval < arr.length; cntval++) {
            if (obj.id.substring(5) == arr[cntval]) {
                if (d086 == '1') {
                    document.getElementById("CALC"+obj.id.substring(5)).innerHTML = Math.round(AssesList[cntval] * obj.value / 100);
                } else {
                    //最初のテキストボックスの値を数字に変換
                    var num = parseFloat(AssesList[cntval]) * parseFloat(obj.value);
                    //小数点の位置を2桁右に移動する（1234567.89にする）
                    var num1 = num / 100;
                    document.getElementById("CALC"+obj.id.substring(5)).innerHTML = num1;
                }
                break;
            }
        }
    } else {
        for(var cntval = 0; cntval < arr.length; cntval++) {
            if (obj.id.substring(5) == arr[cntval]) {
                document.getElementById("CALC"+obj.id.substring(5)).innerHTML = "<br>" ;
                break;
            }
        }
    }
    
    for(var cntval = 0; cntval < arr.length; cntval++) {
        if (obj.id.substring(5) == arr[cntval]) {
            var hidOmomi = hdiOmomi[cntval];
            if(hidOmomi == ""){
              obj.style.backgroundColor = '#ffffff';
            } else {
              if(hidOmomi != obj.value){
                obj.style.backgroundColor = '#ff99cc';
              } else {
                obj.style.backgroundColor = '#ffffff';
              }
            }
            break;
        }
    }
    
    return true;
}

//終了
function btnEnd() {
    if (!setupFlgCheck()) return;
    closeWin();
}

//スクロール
function scrollRC(){
    document.getElementById('trow').scrollLeft = document.getElementById('tbody').scrollLeft;
    document.getElementById('tcol').scrollTop = document.getElementById('tbody').scrollTop;
    document.getElementById('trowFoot').scrollLeft = document.getElementById('tbody').scrollLeft;
}
