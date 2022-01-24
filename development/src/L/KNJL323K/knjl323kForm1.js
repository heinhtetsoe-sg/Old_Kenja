function btn_submit(cmd) {
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
//印刷
function newwin(SERVLET_URL){

    action = document.forms[0].action;
    target = document.forms[0].target;

//  url = location.hostname;
//  document.forms[0].action = "http://" + url +"/cgi-bin/printenv.pl";
    document.forms[0].action = SERVLET_URL +"/KNJL";
    document.forms[0].target = "_blank";
    document.forms[0].submit();

    document.forms[0].action = action;
    document.forms[0].target = target;
}
function dis_test(obj)
{

    if (document.forms[0].SORT[1].checked){
        document.forms[0].OUTTYPE[0].disabled = false;
        document.forms[0].OUTTYPE[1].disabled = false;
        if (document.forms[0].OUTTYPE[0].checked){
            document.forms[0].OUTRIKA[0].disabled = false;
            document.forms[0].OUTRIKA[1].disabled = true;  //NO001
        }else {
            document.forms[0].OUTARAKALT[0].disabled = false;
            document.forms[0].OUTARAKALT[1].disabled = false;
        }
    }

    if (document.forms[0].SORT[0].checked){
        document.forms[0].OUTPUT.disabled = true;
        document.forms[0].OUTPUT3.disabled = true;  //NO002
    }else {
        document.forms[0].OUTPUT.disabled = false;
        document.forms[0].OUTPUT3.disabled = true;  //NO002
    }

    if (document.forms[0].SORT[0].checked){
        document.forms[0].OUTPUT.disabled  = true;
        document.forms[0].OUTPUT2.disabled = false;
    }else {
        document.forms[0].OUTPUT.disabled  = false;
        document.forms[0].OUTPUT2.disabled = true;
    }

}

function dis_sort(obj)
{
    if (obj.value == 2) {
        document.forms[0].OUTTYPE[0].disabled = false;
        document.forms[0].OUTTYPE[1].disabled = false;
        document.forms[0].OUTPUT3.disabled = true;  //NO002
        if (document.forms[0].OUTTYPE[0].checked) {
            document.forms[0].OUTRIKA[0].disabled = false;
            document.forms[0].OUTRIKA[1].disabled = true;  //NO001
        } else {
            document.forms[0].OUTARAKALT[0].disabled = false;
            document.forms[0].OUTARAKALT[1].disabled = false;
        }
        document.forms[0].OUTPUT.disabled = false;
        document.forms[0].OUTPUT2.disabled = true;
    } else {
        document.forms[0].OUTTYPE[0].disabled = true;
        document.forms[0].OUTRIKA[0].disabled = true;
        document.forms[0].OUTRIKA[1].disabled = true;
        document.forms[0].OUTTYPE[1].disabled = true;
        document.forms[0].OUTARAKALT[0].disabled = true;
        document.forms[0].OUTARAKALT[1].disabled = true;
        document.forms[0].OUTPUT.disabled  = true;
        document.forms[0].OUTPUT3.disabled  = true; //NO002
        document.forms[0].OUTPUT2.disabled = false;
    }
}
function dis_ara(obj)
{
    if(obj.value == 1)
    {
//      document.forms[0].OUTPUT.disabled = false;
    }
    else
    {
//      document.forms[0].OUTPUT.disabled = true;
    }
}
function dis_type(obj)
{
    if(obj.value == 2)
    {
        document.forms[0].OUTRIKA[1].disabled = true;
        document.forms[0].OUTRIKA[0].disabled = true;
        document.forms[0].OUTARAKALT[0].disabled = false;
        document.forms[0].OUTARAKALT[1].disabled = false;
        document.forms[0].OUTPUT.disabled = false;
    }
    else
    {
        document.forms[0].OUTRIKA[1].disabled = true;  //NO001
        document.forms[0].OUTRIKA[0].disabled = false;
        document.forms[0].OUTARAKALT[0].disabled = true;
        document.forms[0].OUTARAKALT[1].disabled = true;
        document.forms[0].OUTPUT.disabled = false;
    }
}
