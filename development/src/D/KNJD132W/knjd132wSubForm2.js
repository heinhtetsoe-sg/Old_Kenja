//サブミット
function btn_submit(cmd){
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
                parentRemarkValue = parent.document.forms[0].COMMITTEE;
                if (parentRemarkValue.value.length > 0) {
                    parentRemarkValue.value = parentRemarkValue.value + "\n" + thisValue;
                } else {
                    parentRemarkValue.value = thisValue;
                }
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
