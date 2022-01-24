function btn_submit(cmd) {
    if (cmd=="btn_clear") {
        document.forms[0].btn_keep.disabled = true;
        document.forms[0].btn_clear.disabled = true;
        document.forms[0].btn_end.disabled = false;
    }
    if (cmd == "clear") {
        result = confirm('{rval MSG106}');
        if (result == false) {
            return false;
        } else {
            attribute3 = document.forms[0].selectdata;
            attribute3.value = "";
            sep = "";
            for (var i = 0; i < document.forms[0].isGroup.length; i++) {
                attribute3.value = attribute3.value + sep + document.forms[0].isGroup.options[i].value;
                sep = ",";
            }
        }
    }
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
function doSubmit() {

    if (document.forms[0].CLUBCD.value == "ALL") {
        document.forms[0].cmd.value = "copy";
        document.forms[0].submit();
        return false;
    }

    attribute3 = document.forms[0].selectdata;
    attribute3.value = "";
    sep = "";
    if (document.forms[0].isGroup.length==0 && document.forms[0].noGroup.length==0) {
        alert("データは存在していません。");
        return false;
    }
    for (var i = 0; i < document.forms[0].isGroup.length; i++)
    {
        attribute3.value = attribute3.value + sep + document.forms[0].isGroup.options[i].value;
        sep = ",";
    }
    document.forms[0].cmd.value = 'update';
    document.forms[0].submit();
    return false;
}
function add()
{
    var temp1 = new Array();
	var tempa = new Array();
    var v = document.forms[0].YEAR.length;
    var w = document.forms[0].year_add.value
    
    if (w == "") {
        alert('{rval MSG901}\n数字を入力してください。');
        return false;
    }

    for (var i = 0; i < v; i++)
	{	
		if (w == document.forms[0].YEAR.options[i].value) {
            alert("追加した年度は既に存在しています。");
		    return false;
        }
	}
    document.forms[0].YEAR.options[v] = new Option();
    document.forms[0].YEAR.options[v].value = w;
    document.forms[0].YEAR.options[v].text = w;
    
    for (var i = 0; i < document.forms[0].YEAR.length; i++)
	{  
		temp1[i] = document.forms[0].YEAR.options[i].value;
		tempa[i] = document.forms[0].YEAR.options[i].text;
	} 
    //sort
    temp1 = temp1.sort();
    tempa = tempa.sort();
    temp1 = temp1.reverse();
    tempa = tempa.reverse();

    
    //generating new options
	ClearList(document.forms[0].YEAR,document.forms[0].YEAR);
	if (temp1.length>0)
	{	
		for (var i = 0; i < temp1.length; i++)
		{   
			document.forms[0].YEAR.options[i] = new Option();
			document.forms[0].YEAR.options[i].value = temp1[i];
			document.forms[0].YEAR.options[i].text =  tempa[i];
            if(w==temp1[i]){
                document.forms[0].YEAR.options[i].selected=true;
            }
		}
	} 
      
}
function ClearList(OptionList, TitleName) 
{
	OptionList.length = 0;
}
function OnAuthError() {
    alert('{rval MSG300}');
    closeWin();
}
function AllClearList(OptionList, TitleName)  {
        attribute = document.forms[0].isGroup;
        ClearList(attribute,attribute);
        attribute = document.forms[0].noGroup;
        ClearList(attribute,attribute);
}

