function btn_submit(cmd){
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function btnDisabled(disabledFlg) {
    document.forms[0].btn_add.disabled = disabledFlg == "1";
    document.forms[0].btn_update.disabled = disabledFlg == "1";
    document.forms[0].btn_del.disabled = disabledFlg == "1";
}

function doSubmit(cmd) {
    if (document.forms[0].COLLECT_GRP_CD.value == ""){
        alert('{rval MSG304}');
        return true;
    }
    attribute4 = document.forms[0].selectdata2;
    attribute4.value = "";

    sep="";
    for (var i = 0; i < document.forms[0].left_expmcd.length; i++)
    {
        attribute4.value = attribute4.value + sep + document.forms[0].left_expmcd.options[i].value;
        sep = ",";
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function move1(side, left, right, sort, flg)
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
    var attribute3;
    var tmpcmb1;
    var tmpcmb2;
    var tmpcmb3;

    //assign what select attribute treat as attribute1 and attribute2
    if (side == "right" || side == "sel_del_all" || side == "sel_del_all2" || side == "sel_del_all3")
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
        tempa = tempa.sort();
        //generating new options
        for (var i = 0; i < tempa.length; i++)
        {
//            alert(a[tempa[i]]);
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
    
    //入金項目,入金細目のリストが変化したらサブミットする
    if (flg == 2 || flg == 3) {
        tmpcmb2 = document.forms[0].selectdata2;
        tmpcmb2.value = "";
        sep = "";
        for (var i = 0; i < document.forms[0].left_expmcd.length; i++)
        {
            tmpcmb2.value = tmpcmb2.value + sep + document.forms[0].left_expmcd.options[i].value;
            sep = ",";
        }

        document.forms[0].cmd.value = 'reload';
        document.forms[0].submit();
        return false;
    }
}

function ClearList(OptionList)
{
    OptionList.length = 0;
}

function jmsg(msg1,msg2)
{
    alert('{rval MSG300}'+'\n'+msg1+'\n'+msg2);
}
