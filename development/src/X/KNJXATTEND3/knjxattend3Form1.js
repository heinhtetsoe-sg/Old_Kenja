function btn_submit(cmd) {

    if (cmd == "reset") {
        result = confirm('{rval MSG106}');
        if (result == false) {
            return false;
        }
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
var w;
function text_onFocus(){
    clearInterval(w);
}

function checkSchregno() {
    alert('{rval MSG304}');
    parent.closeit();
}

function add()
{
    var temp1 = new Array();
    var tempa = new Array();
    var v = document.forms[0].GRADE_COMBO.length;
    var w = document.forms[0].GRADE_TXT.value

    if (w == ""){
        alert("{rval MSG901}\n数字を入力してください。")
        return false;
    }

    for (var i = 0; i < v; i++)
    {
        if (w == document.forms[0].GRADE_COMBO.options[i].value) {
            alert("追加した年度は既に存在しています。");
            return false;
        }
    }
    document.forms[0].GRADE_COMBO.options[v] = new Option();
    document.forms[0].GRADE_COMBO.options[v].value = w;
    document.forms[0].GRADE_COMBO.options[v].text = w;

    //年度項目追加
    document.forms[0].year_add.value = 'on';

    for (var i = 0; i < document.forms[0].GRADE_COMBO.length; i++)
    {
        temp1[i] = document.forms[0].GRADE_COMBO.options[i].value;
        tempa[i] = document.forms[0].GRADE_COMBO.options[i].text;
    }
    //sort
    temp1 = temp1.sort();
    tempa = tempa.sort();
    temp1 = temp1.reverse();
    tempa = tempa.reverse();

    //generating new options
    ClearList(document.forms[0].GRADE_COMBO,document.forms[0].GRADE_COMBO);
    if (temp1.length>0)
    {
        for (var i = 0; i < temp1.length; i++)
        {
            document.forms[0].GRADE_COMBO.options[i] = new Option();
            document.forms[0].GRADE_COMBO.options[i].value = temp1[i];
            document.forms[0].GRADE_COMBO.options[i].text =  tempa[i];
            if(w==temp1[i]){
                document.forms[0].GRADE_COMBO.options[i].selected=true;
            }
        }
    }
}
function ClearList(OptionList, TitleName)
{
    OptionList.length = 0;
}

function sum(obj,scd)
{
    var returnValue;
    var setValue = 0;
    var number = new Array;

    returnValue = checkValue(obj);

    if(returnValue){
        number[0] = (document.all('CLASSDAYS'+scd).value == '')? 0 : parseInt(document.all('CLASSDAYS'+scd).value);
        number[1] = (document.all('OFFDAYS'+scd).value == '')?   0 : parseInt(document.all('OFFDAYS'+scd).value);
        number[2] = (document.all('SUSPEND'+scd).value == '')?   0 : parseInt(document.all('SUSPEND'+scd).value);
        number[3] = (document.all('MOURNING'+scd).value == '')?  0 : parseInt(document.all('MOURNING'+scd).value);
        number[4] = (document.all('ABROAD'+scd).value == '')?    0 : parseInt(document.all('ABROAD'+scd).value);
        number[5] = (document.all('SICK'+scd).value == '')?      0 : parseInt(document.all('SICK'+scd).value);
        number[6] = (document.all('ACCIDENTNOTICE'+scd).value == '')? 
                     0 : parseInt(document.all('ACCIDENTNOTICE'+scd).value);
        number[7] = (document.all('NOACCIDENTNOTICE'+scd).value == '')? 
                     0 : parseInt(document.all('NOACCIDENTNOTICE'+scd).value);

        setValue = (number[0] - number[1] ) - (number[2] + number[3] + number[4]);
        document.all('REQUIREPRESENT'+scd).innerHTML = setValue;
        number[8] = parseInt(document.all('REQUIREPRESENT'+scd).innerHTML);
        setValue = number[8] - (number[5] + number[6] + number[7]);
        document.all('PRESENT'+scd).innerHTML = parseInt(setValue);
    }
    return;
}

function checkValue(obj)
{
    if(obj.value != ''){
        obj.value = toInteger(obj.value)
    }
    return true;
}

function ShowConfirm() {
    result = confirm('{rval MSG106}');
    if (result == false) {
        return false;
    }
    document.forms[0].cmd.value = 'reset';
    document.forms[0].submit();
    return false;
}

window.onunload=function(){if (newWin) newWin.close();}
