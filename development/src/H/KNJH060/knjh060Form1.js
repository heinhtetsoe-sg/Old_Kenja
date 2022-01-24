function btn_submit(cmd) {

    if (cmd == 'clear') {
        if (!confirm('{rval MSG106}')) {
            return false;
        }
    }
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}


function OnNotUse(term)
{
    for (var i=0; i<document.forms[0].elements.length;i++){

        var e = document.forms[0].elements[i];

        if (e.type=='checkbox') {
            if (e.name!='caution_check'+term && e.name!='admonition_check'+term) {
                e.disabled = true;
            } 
        }
    }    
}