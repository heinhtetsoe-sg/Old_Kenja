function btn_submit(cmd) {   
	if (cmd == 'clear'){
		if (!confirm('{rval MSG106}'))
			return false;
		else
			cmd = "";
	}
        document.forms[0].cmd.value = cmd;
        document.forms[0].submit();
        return false;

}

function doSubmit() {
	attribute3 = document.forms[0].selectdata;
	attribute3.value = "";
	sep = "";
    if (document.forms[0].classyear.length==0 && document.forms[0].classmaster.length==0) {
		alert('{rval MSG303}');
		return false;
	}
    for (var i = 0; i < document.forms[0].classyear.length; i++)
	{
		attribute3.value = attribute3.value + sep + document.forms[0].classyear.options[i].value;
        sep = ",";
	}
    document.forms[0].cmd.value = 'update';
    document.forms[0].submit();
    return false;

}

function ClearList(OptionList, TitleName) 
{
	OptionList.length = 0;
}
	
function move(side)
{   
	var temp1 = new Array();
	var temp2 = new Array();
	var tempa = new Array();
	var tempb = new Array();
	var current1 = 0;
	var current2 = 0;
	var y=0;
	var attribute;
	
	//assign what select attribute treat as attribute1 and attribute2
	if (side == "right" || side == "sel_del_all")
	{  
		attribute1 = document.forms[0].classyear;
		attribute2 = document.forms[0].classmaster;
	}
	else
	{  
		attribute1 = document.forms[0].classmaster;
		attribute2 = document.forms[0].classyear;  
	}

	
	//fill an array with old values
	for (var i = 0; i < attribute2.length; i++)
	{  
		y=current1++
		temp1[y] = attribute2.options[i].value;
		tempa[y] = attribute2.options[i].text;
	}

	//assign new values to arrays
	for (var i = 0; i < attribute1.length; i++)
	{ 
        if (side == "right" || side == "left") 
        {
    		if ( attribute1.options[i].selected )
    		{  
    			y=current1++
    			temp1[y] = attribute1.options[i].value;
    			tempa[y] = attribute1.options[i].text; 
    		}
    		else
    		{  
    			y=current2++
    			temp2[y] = attribute1.options[i].value; 
    			tempb[y] = attribute1.options[i].text;
    		}
        } else {

            y=current1++
			temp1[y] = attribute1.options[i].value;
			tempa[y] = attribute1.options[i].text; 
        }        
	}

	//generating new options 
	for (var i = 0; i < temp1.length; i++)
	{  
		attribute2.options[i] = new Option();
		attribute2.options[i].value = temp1[i];
		attribute2.options[i].text =  tempa[i];
	}

	//generating new options
	ClearList(attribute1,attribute1);
	if (temp2.length>0)
	{	
		for (var i = 0; i < temp2.length; i++)
		{   
			attribute1.options[i] = new Option();
			attribute1.options[i].value = temp2[i];
			attribute1.options[i].text =  tempb[i];
		}
	}


	attribute3 = document.forms[0].selectdata;
	attribute3.value = "";
	sep = "";
    for (var i = 0; i < document.forms[0].classyear.length; i++)
	{
		attribute3.value = attribute3.value + sep + document.forms[0].classyear.options[i].value;
        sep = ",";
	}	
}

function add()
{
    var temp1 = new Array();
	var tempa = new Array();
    var v = document.forms[0].year.length;
    var w = document.forms[0].year_add.value
    
    if (w == "") {
        alert('{rval MSG901}\n数字を入力してください。');
        return false;
    }

    for (var i = 0; i < v; i++)
	{	
		if (w == document.forms[0].year.options[i].value) {
            alert("追加した年度は既に存在しています。");
		    return false;
        }
	}
    document.forms[0].year.options[v] = new Option();
    document.forms[0].year.options[v].value = w;
    document.forms[0].year.options[v].text = w;
    
    for (var i = 0; i < document.forms[0].year.length; i++)
	{  
		temp1[i] = document.forms[0].year.options[i].value;
		tempa[i] = document.forms[0].year.options[i].text;
	} 
    //sort
    temp1 = temp1.sort();
    tempa = tempa.sort();
    temp1 = temp1.reverse();
    tempa = tempa.reverse();

    
    //generating new options
	ClearList(document.forms[0].year,document.forms[0].year);
	if (temp1.length>0)
	{	
		for (var i = 0; i < temp1.length; i++)
		{   
			document.forms[0].year.options[i] = new Option();
			document.forms[0].year.options[i].value = temp1[i];
			document.forms[0].year.options[i].text =  tempa[i];
            if(w==temp1[i]){
                document.forms[0].year.options[i].selected=true;
            }
		}
	} 
      
}

function temp_clear()
{
    ClearList(document.forms[0].classyear,document.forms[0].classyear);
    ClearList(document.forms[0].classmaster,document.forms[0].classmaster);
}

function OnAuthError()
{
	alert('{rval MSG300}');
	closeWin();
}
