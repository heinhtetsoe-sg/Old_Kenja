function btn_submit(cmd) {
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function apply_school(obj) {
    //学校名
    var job_list    = document.forms[0].JOB_LIST;
    if (job_list.value) {
        var jobtype_lcd      = job_list.value.split('|')[0];
        var jobtype_mcd      = job_list.value.split('|')[1];
        var jobtype_scd      = job_list.value.split('|')[2];
        var jobtype_sname    = job_list.value.split('|')[3];
    } else {
        var jobtype_lcd      = "";
        var jobtype_mcd      = "";
        var jobtype_scd      = "";
        var jobtype_sname    = "";
    }

    var p_jobtype_lcd   = eval("parent.document.forms[0].JOBTYPE_LCD" + document.forms[0].target_number.value);
    var p_jobtype_mcd   = eval("parent.document.forms[0].JOBTYPE_MCD" + document.forms[0].target_number.value);
    var p_jobtype_scd   = eval("parent.document.forms[0].JOBTYPE_SCD" + document.forms[0].target_number.value);
    var p_jobtype_sname = eval("parent.document.getElementById('JOBTYPE_SNAME" + document.forms[0].target_number.value + "')");

    p_jobtype_lcd.value = jobtype_lcd;
    p_jobtype_mcd.value = jobtype_mcd;
    p_jobtype_scd.value = jobtype_scd;
    p_jobtype_sname.innerHTML = jobtype_sname;

    parent.closeit();
}

//Enterをsubmitさせない
function submitStop(e){
	if (!e) var e = window.event;

	if(e.keyCode == 13)
		return false;
}
