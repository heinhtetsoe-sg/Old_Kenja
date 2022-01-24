function btn_submit(cmd) {
    if (cmd == 'delete') {
        if (!confirm('{rval MSG103}')) {
            return false;
        }
    }
    if (cmd == 'reset') {
        if (!confirm('{rval MSG106}')) {
            return false;
        }
        else {
            document.forms[0].cmd.value = cmd;
            document.forms[0].submit();
            return false;
        }
    }
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
//function btn_submit(cmd) {
//
//    if (cmd == 'delete' && !confirm('{rval MSG103}')){
//        return true;
//    }
//    
//    document.forms[0].cmd.value = cmd;
//    document.forms[0].submit();
//    return false;
//}

//function ShowConfirm()
//{
//    if (!confirm('{rval MSG106}')){
//        return false;
//    }
//    else
//    {
//        document.forms[0].PRINTFLG.value = "";
//        return true;
//    }
//}

function Cleaning(that)
{
    var str = 'knjg040index.php?cmd=edit&apply_div=' + that.value;
//    document.forms[0].clear.value = '1';
    btn_submit('list');
    window.open(str,'right_frame');
    return false;
}

function Number_check(that,what)
{
    var numbers;
    
    if(that.value.length == 1){
        that.value = 0 + that.value;
    }
    
    switch(what){
        case 'hour':
            numbers = num_toInteger(that)
            time_fix(that,'24')
            break;
            
        case 'minute':
            numbers = num_toInteger(that)
            time_fix(that,'59')
            break;
        default:
            numbers = num_toInteger(that)
            break;
        }
    
    return;
}

function num_toInteger(that)
{
    that.value = toInteger(that.value);
    return that.value
}

function time_fix(that,i)
{
    if(that.value > i){
        alert('{rval MSG901}');
        that.value = i;
        return false;
    }
    
    return true;

}

function Close_Win()
{
//    alert('{rval MA0004}');
//    closeWin();
}

function PrintCheck(xxx)
{
    document.forms[0].PRINTFLG.value = "error";
}


function newwin(SERVLET_URL){
//2004/04/27 function newwin(){

    if (document.forms[0].PRINTAPPLYDAY.value == "")
    {
        alert('更新がまだされていません。');
        return;
    }

    if (document.forms[0].PRINTFLG.value != "")
    {
        alert('更新がまだされていません。');
        return;
    }

    if (document.forms[0].applyday.value != document.forms[0].PRINTAPPLYDAY.value)
    {
        alert('更新がまだされていません。');
        return;
    }

    if (document.forms[0].sdate.value != document.forms[0].PRINTSTARTDAY.value)
    {
        alert('更新がまだされていません。');
        return;
    }

    if (document.forms[0].edate.value != document.forms[0].PRINTENDDAY.value)
    {
        alert('更新がまだされていません。');
        return;
    }

    action = document.forms[0].action;
    target = document.forms[0].target;

//	url = location.hostname;
//	  document.forms[0].action = "http://" + url +"/servlet/KNJG";
//		document.forms[0].action = "http://" + url +"/cgi-bin/printenv.pl";

    document.forms[0].action = SERVLET_URL +"/KNJG"; //2004/04/27 add

    document.forms[0].target = "_blank";
    document.forms[0].submit();

    document.forms[0].action = action;
    document.forms[0].target = target;
}


