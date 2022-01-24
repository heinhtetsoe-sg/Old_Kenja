function btn_submit(cmd) {
    if (cmd == 'update' || cmd == 'delete') {
        if (document.forms[0].SUBCLASSCD.value=='') {
            alert('{rval MSG301}');
            return false;
        }    
    }
    if (cmd == 'delete' && !confirm('{rval MSG103}')){
        return true;
    }
    if (cmd == 'delete2' && !confirm('{rval MSG103}')){
        return true;
    } else if (cmd == 'delete2'){
        for (var i=0; i < document.forms[0].elements.length; i++)
        {
            if (document.forms[0].elements[i].name == "CHECKED2[]" && document.forms[0].elements[i].checked){
                break;
            }
        }
        if (i == document.forms[0].elements.length){
            alert("削除チェックボックスを選択してください");
            return true;
        }
    }

    if (cmd == "update") {
        if (!document.forms[0].CHECKED10.checked &&
            !document.forms[0].CHECKED11.checked &&
            !document.forms[0].CHECKED12.checked
        ){
            alert("更新チェックボックスを選択してください");
            return false;
        }
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
function setDisabled(obj) {
    if ((obj.value == "0" || obj.value == "00") &&
        document.forms[0].YEAR.value == "0000"
    ) {
        document.forms[0].CHECKED10.disabled = true;
        document.forms[0].CHECKED12.disabled = true;
    } else {
        document.forms[0].CHECKED10.disabled = false;
        document.forms[0].CHECKED12.disabled = false;
    }
}
function Btn_reset(cmd) {
    result = confirm('{rval MSG106}');
    if (result == false) {
        return false;
    }
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
function add()
{
    var temp1 = new Array();
    var tempa = new Array();
    var v = document.forms[0].YEAR.length;
    var w = document.forms[0].year_add.value;

    if (w == "")
    {
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
    btn_submit('add_year');
}
function ClearList(OptionList, TitleName)
{
    OptionList.length = 0;
}

function add2() {

    var attribute;
    
    attribute = document.forms[0].SUBCLASS;
    
    for (var i = 0; i < attribute.length; i++)
    {     
        if ( attribute.options[i].selected )
        {  
            temp       = attribute.options[i].text.split(":");
            temp_value = attribute.options[i].value.split(":");    
        }
    }
   
    document.forms[0].SUBCLASSCD.value = temp[0];
    document.forms[0].SUBCLASSNAME.value = temp[1];
    document.forms[0].SUBCLASSABBV.value = temp_value[1];
    document.forms[0].SUBCLASSNAME_ENG.value = temp_value[2];
    document.forms[0].SUBCLASSABBV_ENG.value = temp_value[3];
      
}

function moji_hantei(that)
{
 kekka=0;
 moji=that.value;
 for(i=0; i<moji.length; i++)
     {
     dore=escape(moji.charAt(i));
     if(navigator.appName.indexOf("Netscape")!=-1)
         {
             if(dore.length>3 && dore.indexOf("%")!=-1){
             }
         }
     else 
         {
             if(dore.indexOf("%uFF")!=-1 && '0x'+dore.substring(2,dore.length) < 0xFF60){
                 kekka++;
             }else if(moji.match(/\W/g) != null && dore.length == 6){
                 kekka++;
             }

         }
     }
 if(kekka>0){
 alert("全角文字が含まれています。");
     srch='';
     that.value=moji.replace(/\W/g, srch);
 }

}

//データ入力項目の説明をチップヘルプで表示
function ViewcdMousein(msg_no){
	var msg = "";
	if (msg_no==0) msg = "0：成績データと資格データの高認試験以外データ";
	if (msg_no==1) msg = "1：０年度データと転入生の過去のデータ";
	if (msg_no==2) msg = "2：資格データの高認試験データ";

	x = event.clientX+document.body.scrollLeft;
	y = event.clientY+document.body.scrollTop;
	document.all("lay").innerHTML = msg;
	document.all["lay"].style.position = "absolute";
	document.all["lay"].style.left = x+5;
	document.all["lay"].style.top = y+10;
	document.all["lay"].style.padding = "4px 3px 3px 8px";
	document.all["lay"].style.border = "1px solid";
	document.all["lay"].style.visibility = "visible";
	document.all["lay"].style.background = "#ccffff";
}

function ViewcdMouseout(){
    document.all["lay"].style.visibility = "hidden";
}
