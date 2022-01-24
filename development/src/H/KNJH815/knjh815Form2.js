window.onload = function(){
    document.getElementById('radar0').style.display="none"

    createRadarChart('radar', '0');
}

function btn_submit(cmd) {

    
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
