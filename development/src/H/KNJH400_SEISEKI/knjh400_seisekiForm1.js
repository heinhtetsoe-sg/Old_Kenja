window.onload = function(){
    //�O���t�쐬
    document.getElementById("line0").style.display="none"
    createRadarChart("line", 0);

}

function btn_submit(cmd) {   


    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
function closecheck() {
    parent.window.close();
}
