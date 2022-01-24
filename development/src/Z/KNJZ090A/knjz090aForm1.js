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
    var sep;
    var len;
    var val;
    var i;
    if (document.forms[0].finschoolyear.length==0 && document.forms[0].finschoolmaster.length==0) {
        alert('データは存在していません。');
        return false;
    }
    attribute3 = document.forms[0].selectdata;
    attribute3.value = "";
    sep = "";
    val = "";
    len = document.forms[0].finschoolyear.length;
    for (i = 0; i < len; i++)
    {
        val += sep + document.forms[0].finschoolyear.options[i].value;
        sep = ",";
    }
    attribute3.value = val;
    document.forms[0].cmd.value = 'update';
    document.forms[0].submit();
    return false;
}
function add()
{
    var temp1 = new Array();
    var tempa = new Array();
    var v = document.forms[0].year.length;
    var w = document.forms[0].year_add.value;
    
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
//    temp_clear();
      
}

function temp_clear()
{
    ClearList(document.forms[0].finschoolyear,document.forms[0].finschoolyear);
    ClearList(document.forms[0].finschoolmaster,document.forms[0].finschoolmaster);
}

function OnAuthError()
{
    alert('{rval MSG300}');
    closeWin();
}

function move1(side, left, right, sort)
{
    var temp1 = [];
    var temp2 = [];
    var tempa = [];
    var tempb = [];
    var a = [];
    var current1 = 0;
    var current2 = 0;
    var y=0;
    var attribute1;
    var attribute2;
    var i;
    var attribute1HTML;
    var attribute2HTML;
    var sep;
    var len;
    var val;

    //assign what select attribute treat as attribute1 and attribute2
    if (side == "right" || side == "sel_del_all")
    {
        attribute1 = document.forms[0][left];
        attribute2 = document.forms[0][right];
    }
    else
    {
        attribute1 = document.forms[0][right];
        attribute2 = document.forms[0][left];
    }
    //fill an array with old values
    len = attribute2.length;
    for (i = 0; i < len; i++)
    {
        y=current1++;
        temp1[y] = attribute2.options[i].value;
        tempa[y] = attribute2.options[i].text;
        a[tempa[y]] = temp1[y];
    }
    //assign new values to arrays
    len = attribute1.length;
    for (i = 0; i < len; i++)
    {
        if (side == "right" || side == "left")
        {
            if ( attribute1.options[i].selected )
            {
                y=current1++;
                temp1[y] = attribute1.options[i].value;
                tempa[y] = attribute1.options[i].text;
                a[tempa[y]] = temp1[y];
            } else {
                y=current2++;
                temp2[y] = attribute1.options[i].value;
                tempb[y] = attribute1.options[i].text;
            }
        } else {
            y=current1++;
            temp1[y] = attribute1.options[i].value;
            tempa[y] = attribute1.options[i].text;
            a[tempa[y]] = temp1[y];
        }
    }
    if (sort){
        //sort
        tempa = tempa.sort();
        //generating new options
        for (i = 0; i < tempa.length; i++)
        {
            temp1[i] = a[tempa[i]];
        }
    }

    //generating new options
    attribute2HTML = "";
    for (i = 0; i < temp1.length; i++)
    {
        attribute2HTML += '<option value="' + temp1[i] + '">' + tempa[i] + '</option>';
    }
    attribute2.innerHTML = attribute2HTML;
    
    //generating new options
    ClearList1(attribute1);
    if (temp2.length>0)
    {
        attribute1HTML = '';
        for (i = 0; i < temp2.length; i++)
        {
            attribute1HTML += '<option value="' + temp2[i] + '">' + tempb[i] + '</option>';
        }
        attribute1.innerHTML = attribute1HTML;
    }
    attribute3 = document.forms[0].selectdata;
    attribute3.value = "";
    sep = "";
    val = "";
    len = document.forms[0][left].length;
    for (i = 0; i < len; i++)
    {
        val += sep + document.forms[0][left].options[i].value;
        sep = ",";
    }
    attribute3.value = val;
}

function ClearList1(OptionList)
{
    OptionList.innerHTML = "";
}
