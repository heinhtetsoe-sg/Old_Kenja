function btn_submit(cmd) {
    if(cmd == 'reset' && !confirm('{rval MSG106}'))  return true;
    if (cmd == "update" && document.forms[0].HID_EXAMNO.value == '')  {
        return false;
    }
    
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function lineSummary(obj) {
        var setArr = document.forms[0].HID_EXAMNO.value.split(',');
        var tmp = obj.id.split('_');

        var index = setArr.indexOf(tmp[0]);

        var total5ka = 0;
        var totalall = 0;
        for (var j = 1;j <= 9;j++) {
            var targetId = tmp[0] + '_' + j;
            if (document.getElementById(targetId).value != "") {
                totalall += parseInt(document.getElementById(targetId).value);
                if (document.forms[0].kyouka5.value.indexOf(j) >= 0) {
                    total5ka += parseInt(document.getElementById(targetId).value);
                }
            }
        }
        var targetId = tmp[0] + '_TOTAL5';
        document.getElementById(targetId).innerHTML = total5ka;
        document.forms[0]["HID_"+targetId].value = total5ka;

        var targetId = tmp[0] + '_TOTAL_ALL';
        document.getElementById(targetId).innerHTML = totalall;
        document.forms[0]["HID_"+targetId].value = totalall;
}

function Setflg(obj) {
    document.forms[0].HID_TESTDIV.value = document.forms[0].TESTDIV.options[document.forms[0].TESTDIV.selectedIndex].value;
    document.forms[0].HID_HOPE_COURSECODE.value = document.forms[0].HOPE_COURSECODE.options[document.forms[0].HOPE_COURSECODE.selectedIndex].value;

    document.forms[0].TESTDIV.disabled = true;
    document.forms[0].HOPE_COURSECODE.disabled = true;

    var tmp = obj.id.split('_');
    document.getElementById('ROWID' + tmp[0]).style.background="yellow";
    obj.style.background="yellow";
}

//Enterキーで移動
function keyChangeEntToTab(obj) {
    if (window.event.keyCode == '13') {
        var setArr = document.forms[0].HID_EXAMNO.value.split(',');
        var tmp = obj.id.split('_');
        var tmpArr = new Array();
        for (var i = 0; i < setArr.length; i++) {
            var rowidx = (i)*9;
            for (var j = 1;j <= 9;j++) {
                tmpArr[rowidx+j] = setArr[i]+'_'+j;
            }
        }
        var index = tmpArr.indexOf(obj.id);

        if (window.event.shiftKey) {
            if (index > 0) {
                index--;
            }
            var targetId = tmpArr[index];
            if (document.getElementById(targetId).disabled == true) {
                for (var i = index; i > 0; i--) {
                    targetId = tmpArr[i];
                    if (document.getElementById(targetId).disabled == false) break;
                }
            }
        } else {
            if (index < (tmpArr.length - 1)) {
                index++;
            }
            var targetId = tmpArr[index];
            if (document.getElementById(targetId).disabled == true) {
                for (var i = index; i < (tmpArr.length - 1); i++) {
                    targetId = tmpArr[i];
                    if (document.getElementById(targetId).disabled == false) break;
                }
            }
        }

        document.getElementById(targetId).focus();
        document.getElementById(targetId).select();
        return false;
    }
}
