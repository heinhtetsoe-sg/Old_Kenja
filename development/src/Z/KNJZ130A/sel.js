function btn_submit(cmd) {
    if (cmd=="clear") {
        result = confirm('{rval MSG107}');
        if (result == false) {
            return false;
        }
    }
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function doSubmit() {
    attribute3 = document.forms[0].selectdata;
    attribute3.value = "";
    sep = "";
    if (document.forms[0].nameyear.length==0 && document.forms[0].namemaster.length==0) {
        alert("データは存在していません。");
        return false;
    }
    for (var i = 0; i < document.forms[0].nameyear.length; i++)
    {
        attribute3.value = attribute3.value + sep + document.forms[0].nameyear.options[i].value;
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
    
function add()
{
    var temp1 = new Array();
    var tempa = new Array();
    var v = document.forms[0].year.length;
    var w = document.forms[0].year_add.value
    
    if (w == "")
        return false;

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
    //temp_clear();
      
}

function temp_clear()
{
    ClearList(document.forms[0].nameyear,document.forms[0].nameyear);
    ClearList(document.forms[0].namemaster,document.forms[0].namemaster);
}

function OnAuthError()
{
    alert('{rval MSG300}');
    closeWin();
}

