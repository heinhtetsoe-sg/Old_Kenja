//ＣＳＶ出力
function btn_submit(cmd)
{
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function newwin(SERVLET_URL)
{
    action = document.forms[0].action;
    target = document.forms[0].target;

    document.forms[0].action = SERVLET_URL +"/KNJF";
    document.forms[0].target = "_blank";
    document.forms[0].submit();

    document.forms[0].action = action;
    document.forms[0].target = target;
}

function check_csv_radio(cmd)
{
    if(document.forms[0].OUTPUT_CSV1.checked){
        document.forms[0].OUTPUT_TYPE1.disabled = true;
        document.forms[0].OUTPUT_TYPE2.disabled = true;
    }
    else{
        document.forms[0].OUTPUT_TYPE1.disabled = false;
        document.forms[0].OUTPUT_TYPE2.disabled = false;
    }
}
