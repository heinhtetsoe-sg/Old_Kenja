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
    }else if (cmd == 'delete2'){
        for (var i=0; i < document.forms[0].elements.length; i++)
        {
            if (document.forms[0].elements[i].name == "CHECKED2[]" && document.forms[0].elements[i].checked){
                break;
            }
        }
        if (i == document.forms[0].elements.length){
            alert("チェックボックスを選択してください");
            return true;
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
    btn_submit('add_year');
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

//背景色の変更
function background_color(obj){
    obj.style.background='#ffffff';
}
//入力チェック
function calc(obj, electdiv){

    var str = obj.value;
    var nam = obj.name;
    
    //空欄
    if (str == '') { 
        return;
    }

    //英小文字から大文字へ自動変換
    if (str.match(/a|b|c/)) { 
        obj.value = str.toUpperCase();
        str = str.toUpperCase();
    }

    //評定（必修）
    if (electdiv == '0' && nam.match(/STATUS9./)) {
        if (!str.match(/1|2|3|4|5/)) {
            alert('{rval MSG901}'+'「1～5」を入力して下さい。');
            obj.value = "";
            obj.focus();
            background_color(obj);
            return;
        }

    //観点1～5 & 評定（選択）
    } else {
        if (!str.match(/A|B|C/)) { 
            alert('{rval MSG901}'+'「AまたはBまたはC」を入力して下さい。');
            obj.value = "";
            obj.focus();
            background_color(obj);
            return;
        }
    }
}

function kirikae2(obj, showName) {
    event.cancelBubble = true
    event.returnValue = false;
    clickList(obj, showName);
}

function clickList(obj, showName) {
    innerName = showName;

    setObj = obj;
    myObj = document.getElementById("myID_Menu").style;
    myObj.left = event.clientX + document.body.scrollLeft + "px";
    myObj.top  = event.clientY + document.body.scrollTop + "px";
    myObj.visibility = "visible";
}

function myHidden() {
    document.all["myID_Menu"].style.visibility = "hidden";
}

function setClickValue(val) {
    if (val != '999') {
        typeShowArray = document.forms[0].SETSHOW.value.split(",");
        if (setObj.value != typeShowArray[val - 1]) {
            setObj.style.background = '#ccffcc';
        }
        setObj.value = typeShowArray[val - 1];
        typeValArray = document.forms[0].SETVAL.value.split(",");
    }
    myHidden();
    setObj.focus();
}
