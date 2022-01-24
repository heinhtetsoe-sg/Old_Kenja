function btn_submit(cmd)
{
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function getFrame()
{
    return document.forms[0].frame.value;
}

function apply_jobtypes(obj)
{
    if (obj.selectedIndex >= 0) {
        var val  = obj.options[obj.selectedIndex].text;
        var arr  = val.split('-');

        var jobtype_Lcd   = parent.document.forms[0].JOBTYPE_LCD;
        var jobtype_Mcd   = parent.document.forms[0].JOBTYPE_MCD;
        var jobtype_Scd   = parent.document.forms[0].JOBTYPE_SCD;
        var jobtype_SScd  = parent.document.forms[0].JOBTYPE_SSCD;
        var jobtype_Sname = parent.document.getElementById('label_jobtype_name');

        //職業種別（大）コード
        if (jobtype_Lcd) {
            jobtype_Lcd.value = document.forms[0].JOBTYPE_LCD.value;
        }

        //職業種別（中）コード
        if (jobtype_Mcd) {
            jobtype_Mcd.value = document.forms[0].JOBTYPE_MCD.value
        }

        //職業種別（小）コード
        if (jobtype_Scd) {
            jobtype_Scd.value = arr[0];
        }
        
        //職業種別（細）コード
        if (jobtype_SScd) {
            jobtype_SScd.value = arr[1];
        }

        //産業種別名称innerHTML
        if (jobtype_Sname) {
            jobtype_Sname.innerHTML = arr[2];
        }

        parent.closeit();
    } else {
        alert("データが選択されていません");
    }
}
