window.onload = function(){

}
function btn_submit(cmd) {
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function closecheck() {
    parent.window.close();
}

function openGamen(prg, para) {

    if(document.forms[0].NENDO.value == '' || document.forms[0].GYOUSYA.value == ''){
        alert('期と業者を選択してください');
        return false;
    }

    var url = document.forms[0].requestroot.value+"/U/"+prg.toUpperCase()+"/"+prg;
    window.open(url+'index.php'+para,'bottom_frame');    
}
