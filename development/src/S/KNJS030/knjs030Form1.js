function btn_submit(cmd)
{
    if (cmd == 'reset' && !confirm('{rval MSG106}')){
        return;
    }
    if (cmd != 'update') {
        document.forms[0].changeVal.value = "";
    }
    if (cmd == 'hrDel') {
        document.forms[0].HR_CLASS.value = "";
    }
    if (cmd == 'staffDel') {
        document.forms[0].STAFF.value = "";
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function scrollRC(){
    document.getElementById('trow').scrollLeft = document.getElementById('tbody').scrollLeft;
    document.getElementById('tcol').scrollTop = document.getElementById('tbody').scrollTop;
}

function chengeData(clickData) {
    if (clickData == "HR") {
        document.forms[0].HR_CLASS.disabled = false;
        document.forms[0].STAFF.disabled = true;
    } else {
        document.forms[0].HR_CLASS.disabled = true;
        document.forms[0].STAFF.disabled = false;
    }
}

function dropUnitDate(obj, taisyou) {
    for (var i=0; i < document.forms[0].elements.length; i++) {
        re = new RegExp("^CHAIR_" + taisyou);
        var obj_updElement = document.forms[0].elements[i];
        if (obj_updElement.name.match(re)) {
            var setTaisyou = taisyou + obj_updElement.name.substr(re.lastIndex);
            var setColor = obj.checked ? "#cccccc" : "#ccffcc";
            setChangeColor(setTaisyou, 'dateAll', setColor);
        }
    }
}

function dropUnit(taisyou) {
    var unitName = "UNIT_" + taisyou;
    var bunkatuName = "BUNKATU_" + taisyou;
    var remarkName = "REMARK_" + taisyou;
    document.getElementById(unitName).innerHTML = "&nbsp;";
    document.getElementById(bunkatuName).innerHTML = "&nbsp;";
    document.forms[0][remarkName].value = "";
    setChangeColor(taisyou, 'drop', '#ccffcc');
}

function setChangeColor(taisyou, div, setColor) {
    var idName = "ID_" + taisyou;
    document.getElementById(idName).bgColor = setColor;

    if (div == "text") {
        setChangeText(taisyou, div);
    }
}
function setChangeText(taisyou, div) {
    var sep = "";
    if (document.forms[0].changeVal.value != "") {
        sep = ":";
    }
    document.forms[0].changeVal.value += sep + taisyou;
}
