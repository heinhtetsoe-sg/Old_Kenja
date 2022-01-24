function btn_submit(cmd) {
    if (cmd=="clear") {
        result = confirm('{rval MSG106}');
        if (result == false) {
            return false;
        }
    }
    if (cmd == "update") {
        for (var i = 0; i < document.forms[0].length; i++) {
            if (document.forms[0][i].name.match(/ASSESS(MARK|LOW|HIGH)/)) {
                if (document.forms[0][i].value == "") {
                    alert('{rval MSG301}');
                    return false;
                }
            }
        }
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function level(cnt) {
    var level;
    level = document.forms[0].ASSESSLEVELCNT.value;
    if(level == cnt){
        return false;
    }
    if (level == '') {
        document.forms[0].ASSESSLEVELCNT.value = 0;
    }

    document.forms[0].cmd.value = 'level';
    document.forms[0].submit();
    return false;
}

/**********************/
/* 下限が変更された時 */
/**********************/
function changeVal_low(obj) {
    if (obj.value == '') {
        return;
    }
    if (isNaN(obj.value)) {
        alert('{rval MSG914}'+"0点～10.0点以内の数値を入力してください。");
        obj.value = '';
        return false;
    }

    var names = new Array();
    names = obj.name.split("_");

    //直前の下限と比較する
    beforNumber = parseInt(names[2]) - 1;
    beforObject = eval("document.forms[0]." + names[0] + "_" + names[1] + "_" + beforNumber);
    if (beforObject) {
        if (parseFloat(beforObject.value) >= parseFloat(obj.value) && beforObject.value != '') {
            alert("直前の下限より大きい数値を入力して下さい");
            obj.value = "";
            return false;
        }
        if (obj.value == 0) {
            alert("0は最小値です。");
            obj.value = "";
            return false;
        }
    }
    nextNumber = parseInt(names[2]) + 1;
    nextObject = eval("document.forms[0]." + names[0] + "_" + names[1] + "_" + nextNumber);
    if (nextObject) {
        if (parseFloat(nextObject.value) <= parseFloat(obj.value) && nextObject.value != '') {
            alert("直後の下限より小さい数値を入力して下さい");
            obj.value = "";
            return false;
        }
    }

    if (0 > obj.value || obj.value > 10) {
        alert('{rval MSG914}'+'0点～10.0点以内で入力してください。');
        obj.value = "";
        return false;
    }

    obj.value = (Math.round(obj.value * 10) / 10).toFixed(1);

    highScore = (parseInt(obj.value * 10) - 1) / 10;
    assessHigh = eval("document.forms[0].ASSESSHIGH_" + names[1] + "_" + beforNumber);

    if (assessHigh) {
        assessHigh.value = highScore.toFixed(1);
    }
}

/**********************/
/* 上限が変更された時 */
/**********************/
function changeVal_high(obj) {
    if (obj.value == '') {
        return;
    }
    if (isNaN(obj.value)) {
        alert('{rval MSG914}'+"0点～10.0点以内の数値を入力してください。");
        obj.value = '';
        return false;
    }

    var names = new Array();
    names = obj.name.split("_");

    //直前の下限と比較する
    beforNumber = parseInt(names[2]) - 1;
    beforObject = eval("document.forms[0]." + names[0] + "_" + names[1] + "_" + beforNumber);
    if (beforObject) {
        if (parseFloat(beforObject.value) >= parseFloat(obj.value) && beforObject.value != '') {
            alert("直前の下限より大きい数値を入力して下さい");
            obj.value = "";
            return false;
        }
    }
    nextNumber = parseInt(names[2]) + 1;
    nextObject = eval("document.forms[0]." + names[0] + "_" + names[1] + "_" + nextNumber);
    if (nextObject) {
        if (parseFloat(nextObject.value) <= parseFloat(obj.value) && nextObject.value != '') {
            alert("直後の下限より小さい数値を入力して下さい");
            obj.value = "";
            return false;
        }
        if (obj.value == 10) {
            alert("10は最大値です。");
            obj.value = "";
            return false;
        }
    }

    if (0 > obj.value || obj.value > 10) {
        alert('{rval MSG914}'+'0点～10.0点以内で入力してください。');
        obj.value = "";
        return false;
    }

    obj.value = (Math.round(obj.value * 10) / 10).toFixed(1);

    lowScore = (parseInt(obj.value * 10) + 1) / 10;
    assessLow = eval("document.forms[0].ASSESSLOW_" + names[1] + "_" + nextNumber);

    if (assessLow) {
        assessLow.value = lowScore.toFixed(1);
    }
}

function closing_window(){
        alert('{rval MSG300}');
        closeWin();
        return true;
}
