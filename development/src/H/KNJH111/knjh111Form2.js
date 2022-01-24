function btn_submit(cmd) {

    if (cmd == 'delete' && !confirm('{rval MSG103}')){
        return true;
    }
    if (cmd == "contedit") {
        var index = document.forms[0].CONTENTS1.selectedIndex;
        document.forms[0].CONTENTSTEXT.value = document.forms[0].CONTENTS1.options[index].text;
        return false;
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
    if (val.value == 1){
        document.forms[0].CONTENTS1.disabled = false;
        document.forms[0].CONTENTSTEXT.disabled = false;
        document.forms[0].CONTENTS2.disabled = true;
        document.forms[0].CONTENTS2.value = "";
        document.forms[0].REMARK.value = "";
        document.forms[0].CREDITS.value = "";
        CONTENTS1_ON.style.visibility = "visible";  // 表示/非表示
        CONTENTS2_ON.style.visibility = "hidden";   // 表示/非表示
    } else {
        document.forms[0].CONTENTS1.value = "";
        document.forms[0].CONTENTS1.disabled = true;
        document.forms[0].CONTENTSTEXT.disabled = true;
        document.forms[0].CONTENTS2.disabled = false;
        document.forms[0].CONTENTSTEXT.value = "";
        document.forms[0].REMARK.value = "";
        document.forms[0].CREDITS.value = "";
        CONTENTS1_ON.style.visibility = "hidden";   // 表示/非表示
        CONTENTS2_ON.style.visibility = "visible";  // 表示/非表示
    }
}
