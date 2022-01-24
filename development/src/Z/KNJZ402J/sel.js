function btn_submit(cmd) {
    if (cmd=="clear") {
        result = confirm('{rval MSG106}');
        if (result == false) {
            return false;
        }
    }
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
function Btn_reset(cmd) {
    result = confirm('{rval MSG106}');
    if (result == false) {
        return false;
    }
}
function doSubmit() {
    attribute3 = document.forms[0].selectdata;
    attribute3.value = "";
    sep = "";
    if (document.forms[0].subclassyear.length==0 && document.forms[0].subclassmaster.length==0) {
        alert("データは存在していません。");
        return false;
    }
    for (var i = 0; i < document.forms[0].subclassyear.length; i++)
    {
        attribute3.value = attribute3.value + sep + document.forms[0].subclassyear.options[i].value;
        sep = ",";
    }
    document.forms[0].cmd.value = 'update';
    document.forms[0].submit();
    return false;
}
function ClearList(OptionList, TitleName) {
    OptionList.length = 0;
}
function add()
{
    var temp1 = new Array();
    var tempa = new Array();
    var v = document.forms[0].year.length;
    var w = document.forms[0].year_add.value
    
    if (w == "")
    {
        alert("{rval MSG901}\n数字を入力してください。");
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
    ClearList(document.forms[0].subclassyear,document.forms[0].subclassyear);
    ClearList(document.forms[0].subclassmaster,document.forms[0].subclassmaster);
}

function OnAuthError()
{
    alert('{rval MSG300}');
    closeWin();
}

//moveする(追加)
function move2(side, left, right, sort)
{
    var temp1 = new Array();
    var temp2 = new Array();
    var tempa = new Array();
    var tempb = new Array();
    var a = new Array();
    var current1 = 0;
    var current2 = 0;
    var y=0;
    var attribute1;
    var attribute2;

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
    for (var i = 0; i < attribute2.length; i++)
    {
        y=current1++
        temp1[y] = attribute2.options[i].value;
        tempa[y] = attribute2.options[i].text;
        a[tempa[y]] = temp1[y];
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
                a[tempa[y]] = temp1[y];
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
            a[tempa[y]] = temp1[y];
        }
    }
    if (sort){
        //sort
        //学校校種(降順)、科目コード(降順),観点コード(昇順)でソートする
        tempa = tempa.sort(function (value1, value2) {
        var school_kind1 = value1.substr(12,1);
        var school_kind2 = value2.substr(12,1);
        var subclass_viewcd1 = value1.substr(0,11);
        var subclass_viewcd2 = value2.substr(0,11);

        if(school_kind1 == school_kind2){
            if(subclass_viewcd1 > subclass_viewcd2){
                return 1;
            } else {
                return -1;
            }
        }
        if ( school_kind1 > school_kind2 ) {
                // 引数１のschool_kindの方が大きい: 引数２→引数１の順に並べる
                return -1;
            } else {
                // 引数１のschool_kindの方が小さい: 引数１→引数２の順に並べる
                return 1;
            }
        });
        
        //tempa = tempa.sort();
        //generating new options
        for (var i = 0; i < tempa.length; i++)
        {
            //alert(a[tempa[i]]);
            temp1[i] = a[tempa[i]];
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
    ClearList(attribute1);
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
    for (var i = 0; i < document.forms[0][left].length; i++)
    {
        attribute3.value = attribute3.value + sep + document.forms[0][left].options[i].value;
        sep = ",";
    }
}

