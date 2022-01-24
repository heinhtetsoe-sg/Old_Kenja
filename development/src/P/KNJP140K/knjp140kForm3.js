function btn_submit(cmd) {
   
    if (cmd == "clear") {
        if (!confirm('{rval MSG106}'))
            return false;
    }        
        
    if (cmd == "delete") {
        result = confirm('{rval MSG103}');
        if (result == false) {
            return false;
        }
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function Btn_reset() {
   result = confirm('{rval MSG106}');
   if (result == false) {
       return false;
   }
}

function ChgEdit(flg) {


    if (flg == '1') {
        window.open('knjp140kindex.php?cmd=edit1','_self');
    }else if (flg=='2') {
        //2005/12/14 
        if (document.forms[0].inst_cd.value=="") {
            alert('MSG308  編集対象データを選択してください。');
            document.forms[0].radiodiv[0].checked = true;
        }else{
            window.open('knjp140kindex.php?cmd=edit2','_self');
        }
    }
}

function doSubmit(cmd)
{      
    attribute3 = document.forms[0].selectdata;
    attribute3.value = "";
    sep = "";

    if (document.forms[0].left_select.length==0&&document.forms[0].right_select.length==0) {
        alert('指定範囲が正しく有りません。');
        return false;
    }

//    if (cmd =='update1' && document.forms[0].paid_money.value=='1') {
//        alert('納入済みの分納コードは更新できません');
//        return false;
//    }
//

    for (var i = 0; i < document.forms[0].left_select.length; i++)
    {
        attribute3.value = attribute3.value + sep + document.forms[0].left_select.options[i].value;
        sep = ",";
    }
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function ClearList(OptionList, TitleName)
{
    OptionList.length = 0;
}

function format2(str2){
    var str  = new String(str2);
    var temp1 = '';
    var temp2 = str.match(/...$/);
    while(temp2 != null){
      temp1 = temp2 + ','+temp1;
      str = str.replace(/...$/,'');
      temp2 = str.match(/...$/);
    }
    temp1 = (str!='') ? str + ',' + temp1 : temp1;
    temp1 = temp1.replace(/.$/,"");
    return temp1;
}
function SumMoney()
{   
    var cd;
    var sum_money = 0;
    for (var i = 0; i < document.forms[0].left_select.length; i++)
    {
        if (!isNaN(eval(money[document.forms[0].left_select.options[i].value]))) {
            sum_money += eval(money[document.forms[0].left_select.options[i].value]);
        }
    }
    outputLAYER("M_SUM", "合計 "+format2(sum_money)+" 円");
}
function move2(arg1,arg2,arg3,arg4)
{
    move(arg1,arg2,arg3,arg4);
    SumMoney();
}
