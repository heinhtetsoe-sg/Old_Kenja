function btn_submit(cmd)
{
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
function OnAuthError()
{
    alert('{rval MSG300}');
    closeWin();
}
var tmp = '';
function check_all(obj)
{
    var count = 0;
    var chaircd = '';
    var str = '';
    for (var i=0; i < document.forms[0].elements.length; i++)
    {
        if (document.forms[0].elements[i].name == "CHAIRCHK"){
            count++;
            document.forms[0].elements[i].checked = obj.checked;
            chaircd = chaircd + document.forms[0].elements[i].value + ',';
            if(count == 1){
                tmp = document.forms[0].elements[i];
                str = 'knjz231index.php?cmd=sel&CHAIRCHK=' + document.forms[0].elements[i].value;
            }
        }
    }
    if(obj.checked == true){
        str = str + '&COUNT=' + count;
        parent.right_frame.location.href = str;
        parent.right_frame.getData(chaircd,count);
    }else{
        str = 'knjz231index.php?cmd=sel&CHAIRCHK=FALSE';
        parent.right_frame.location.href = str;
    }
}
function reload(that)
{
    var count = 0;
    var chaircd = '';
    var str = '';
    var flag = 0;
    var topchaircd = '';
    for (var i=0; i < document.forms[0].elements.length; i++)
    {
        if (document.forms[0].elements[i].checked == true && document.forms[0].elements[i].name == "CHAIRCHK"){
            count++;
            chaircd = chaircd + document.forms[0].elements[i].value + ',';
            if(count == 1){
                topchaircd = document.forms[0].elements[i];
            }
            if(tmp.checked == false && count == 1){
                flag = 1;
                tmp = document.forms[0].elements[i];
                str = 'knjz231index.php?cmd=sel&CHAIRCHK=' + document.forms[0].elements[i].value;
            }
        }
    }
    if(that.checked == true && count == 1){
        tmp = that;
        chaircd = that.value;
        parent.right_frame.getData(chaircd,1);
        str = 'knjz231index.php?cmd=sel&CHAIRCHK='+ that.value +'&COUNT=1';
        parent.right_frame.location.href = str;
    }else if(that.checked == false && count == 0){
        str = 'knjz231index.php?cmd=sel&CHAIRCHK=FALSE';
        parent.right_frame.location.href = str;
    }else{
        if(flag == 1){
            str = str + '&COUNT=' + count;
            parent.right_frame.location.href = str;
            flag = 0;
        }else if(tmp.value != topchaircd.value){
            tmp = topchaircd;
            str = 'knjz231index.php?cmd=sel&CHAIRCHK=' + topchaircd.value;
            str = str + '&COUNT=' + count;
            parent.right_frame.location.href = str;
        }
        parent.right_frame.getData(chaircd,count);
    }
}