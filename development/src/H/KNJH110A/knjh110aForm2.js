function btn_submit(cmd) {

    if (cmd == 'delete' && !confirm('{rval MSG103}')){
        return true;
    }
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function Btn_reset(cmd) {
    result = confirm('{rval MSG107}');
    if (result == false) {
        return false;
    }
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function changecondition(val) {
    if (val.value != 3){
        document.forms[0].CONTENTS1.disabled = false;
        document.forms[0].CONTENTSTEXT.disabled = false;
        document.forms[0].CONTENTS2.disabled = true;
        CONTENTS1_ON.style.visibility = "visible";  // 表示/非表示
        CONTENTS2_ON.style.visibility = "hidden";   // 表示/非表示
    }else {
        document.forms[0].CONTENTS1.disabled = true;
        document.forms[0].CONTENTSTEXT.disabled = true;
        document.forms[0].CONTENTS2.disabled = false;
        CONTENTS1_ON.style.visibility = "hidden";   // 表示/非表示
        CONTENTS2_ON.style.visibility = "visible";  // 表示/非表示
    }
}
