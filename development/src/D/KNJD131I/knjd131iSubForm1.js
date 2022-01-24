//サブミット
function btn_submit(cmd) {
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function addRemark() {
    var inputs = document.getElementsByTagName("input");

    for (k = 0; k < inputs.length; k++) {
        if (inputs[k].name.match(/^RCHECK/)) {
            if (inputs[k].checked) {
                thisValue = eval("document.forms[0].HIDDEN_" + inputs[k].name + ".value");
                parent.dataPositionSet(thisValue);
                inputs[k].checked = false;
            }
        }
    }
}

function checkAll() {
    var inputs = document.getElementsByTagName("input");

    for (k = 0; k < inputs.length; k++) {
        if (inputs[k].name.match(/^RCHECK/)) {
            inputs[k].checked = document.forms[0].ALL.checked;
        }
    }
}
