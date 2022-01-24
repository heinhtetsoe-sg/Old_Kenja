function btn_submit(cmd) {
    if (cmd == 'delete'){
        if (!confirm('{rval MSG103}'))
            return false;
    }
    if (cmd == 'reset'){
        if (!confirm('{rval MSG106}'))
            return false;
    }
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function OnAuthError()
{
    alert('{rval MSG300}');
    closeWin();
}

function add(cmd)
{
    var temp1 = new Array();
    var tempa = new Array();
    var v = document.forms[0].term.length;
    var w = document.forms[0].year_add.value
    var x = document.forms[0].seme_add.value
    
    if (w == "" || x == "")
        return false;

    if (x > 3 || x < 1){
		alert("学期が不正です。");
		return false;
	}

    for (var i = 0; i < v; i++)
    {   
        if (w+"-"+x == document.forms[0].term.options[i].value) {
            alert("追加した年度は既に存在しています。");
            return false;
        }
    }
    document.forms[0].term.options[v] = new Option();
    document.forms[0].term.options[v].value = w+"-"+x;
    document.forms[0].term.options[v].text = w+"年度 "+x+"学期";
    
    for (var i = 0; i < document.forms[0].term.length; i++)
    {  
        temp1[i] = document.forms[0].term.options[i].value;
        tempa[i] = document.forms[0].term.options[i].text;
    } 
    //sort
    temp1 = temp1.sort();
    tempa = tempa.sort();
    temp1 = temp1.reverse();
    tempa = tempa.reverse();
    
    if (temp1.length>0)
    {   
        for (var i = 0; i < temp1.length; i++)
        {   
            document.forms[0].term.options[i] = new Option();
            document.forms[0].term.options[i].value = temp1[i];
            document.forms[0].term.options[i].text =  tempa[i];
            if(w+"-"+x==temp1[i]){
                document.forms[0].term.options[i].selected=true;
            }
        }
    }
    document.forms[0].cmd.value  = cmd;
    document.forms[0].cmd2.value = 'nendadd';
    document.forms[0].submit();
    return false;

}
