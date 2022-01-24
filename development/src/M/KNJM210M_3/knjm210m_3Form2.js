function btn_submit(cmd) {
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function Name_Clip(name_text){
    var str = name_text.value;
    var Cliping_str;
    
    Cliping_str = str.slice(0,10);
    
    if(document.forms[0].NAME_SHOW.value == '') document.forms[0].NAME_SHOW.value = Cliping_str;

    return true;
}

