function btn_submit(cmd)
{
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}


function change_chugaku_koukou(file_name) {
    var href_string = document.getElementById("download").href;
    href_string = href_string.replace(/\/renrakumou_.*$/,"/"+file_name);
    document.getElementById("download").href = href_string;
}
