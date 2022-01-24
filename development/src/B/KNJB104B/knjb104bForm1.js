function btn_submit(cmd) {
    if (cmd == "clear") {
        if (!confirm("{rval MSG106}")) {
            return false;
        }
    }
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
function changeFacility(obj,chaircd){
    chairListVal = document.forms[0].chaircds.value;
    chairList = (chairListVal == '') ? [] : chairListVal.split(',');
    for (var i = 0; i < chairList.length; i++) {
        if (chairList[i] == chaircd) {
            if(document.getElementsByName('FACILITY_'+i)[0] != obj){
                document.getElementsByName('FACILITY_'+i)[0].value = obj.value;
            }
        }
    }
}