function btn_submit(cmd) {   
    switch(cmd){
        case 'clear':
            if (document.forms[0].UPDATE_FLG.value == "1") {
                if (!confirm('{rval MSG107}')) return true;
            }
            break;
        case 'selectclass':
            if (document.forms[0].UPDATE_FLG.value == "1") {
                if (!confirm('{rval MSG107}')) return true;
            }
            break;
        default:
            break;
    }
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function doSubmit() {
    //左の生徒
    leftStd = document.forms[0].leftData;
    leftStd.value = "";
    sep = "";
    for (var i = 0; i < document.forms[0].LEFT_CLASS_STU.length; i++) {
        leftStd.value = leftStd.value + sep + document.forms[0].LEFT_CLASS_STU.options[i].value;
        sep = ",";
    }

    //右の生徒
    rightStd = document.forms[0].rightData;
    rightStd.value = "";
    sep = "";
    for (var i = 0; i < document.forms[0].RIGHT_CLASS_STU.length; i++) {
        rightStd.value = rightStd.value + sep + document.forms[0].RIGHT_CLASS_STU.options[i].value;
        sep = ",";
    }

    document.forms[0].cmd.value = 'update';
    document.forms[0].submit();
    return false;
}
function hiddenWin(url){
    //document.getElementById("dwindow").style.display="none"
    document.getElementById("cframe").src=url
}
//生徒移動
function moveStudent(side){
    var moveflg = true;
    if (moveflg){
        move(side,'LEFT_CLASS_STU', 'RIGHT_CLASS_STU',1);
        document.forms[0].UPDATE_FLG.value = "1";
    }
    with(document.forms[0]){
        document.getElementById("LEFTNUM").innerHTML = LEFT_CLASS_STU.options.length;
        document.getElementById("RIGHTNUM").innerHTML = RIGHT_CLASS_STU.options.length;
    }
}
function OnAuthError()
{
    alert('{rval MSG300}');
    closeWin();
}
