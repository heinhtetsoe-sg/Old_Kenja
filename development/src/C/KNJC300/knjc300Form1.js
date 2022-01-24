//画面表示された時
window.onload = function(){
    
    //選択されたラジオボタンに関連しないコンボボックスを無効＆初期化
    var radio1 = document.getElementById("JOKEN1");
    var radio2 = document.getElementById("JOKEN2");
    var radio3 = document.getElementById("JOKEN3");
    if (radio1.checked){
        document.forms[0].C_HMRM.disabled = false;
    } else{
        document.forms[0].C_HMRM.disabled = true;
        document.forms[0].C_HMRM.value    = '';
    }
    if (radio2.checked){
        document.forms[0].C_KAMK.disabled = false;
        document.forms[0].C_KOZA.disabled = false;
    } else{
        document.forms[0].C_KAMK.disabled = true;
        document.forms[0].C_KOZA.disabled = true;
        document.forms[0].C_KAMK.value    = '';
        document.forms[0].C_KOZA.value    = '';
    }
    if (radio3.checked){
        document.forms[0].C_CLII.disabled = false;
    } else{
        document.forms[0].C_CLII.disabled = true;
        document.forms[0].C_CLII.value    = '';
    }
}

function btn_submit(cmd) {
    
    //確定
    if(cmd == "insert"){
        
        //生徒選択チェック
        if (document.forms[0].CATEGORY_SELECTED1.length == 0) {
            alert('生徒が選択されていません。');
            return false;
        }
        
        //伝言入力チェック
        if (document.forms[0].D_TEXT1.value == '' && document.forms[0].D_TEXT2.value == '') {
            alert('伝言が入力されていません。');
            return false;
        }
        
        //更新確認メッセージ
        if (confirm('{rval MSG102}')) {
        } else {
            return;
        }
        
    }
    if(cmd != "changeRadio" && cmd != "reset"){
        attribute3 = document.forms[0].selectdata;
        attribute3.value = "";
        sep = "";
        //if (document.forms[0].CATEGORY_NAME.length==0 && document.forms[0].CATEGORY_SELECTED1.length==0) {
            //alert("データは存在していません。");
            //return false;
        //}
        for (var i = 0; i < document.forms[0].CATEGORY_SELECTED1.length; i++)
        {
            attribute3.value = attribute3.value + sep + document.forms[0].CATEGORY_SELECTED1.options[i].value;
            sep = ",";
        }
    }
    
    document.forms[0].cmd.value = cmd ;
    document.forms[0].submit();
    return false;
}

//１件移動
function move1(side) {
    var temp1 = new Array();
    var temp2 = new Array();
    var tempa = new Array();
    var tempb = new Array();
    var tempaa = new Array();
    var current1 = 0;
    var current2 = 0;
    var y=0;
    var attribute;
    
    if (side == "right") {
        attribute1 = document.forms[0].CATEGORY_SELECTED1;
        attribute2 = document.forms[0].CATEGORY_NAME;  
    } else {
        attribute1 = document.forms[0].CATEGORY_NAME;
        attribute2 = document.forms[0].CATEGORY_SELECTED1;
    }
    
    for (var i = 0; i < attribute2.length; i++) {
        y=current1++
        temp1[y] = attribute2.options[i].value;
        tempa[y] = attribute2.options[i].text;
        tempaa[y] = attribute2.options[i].value+","+y;
    }
    
    for (var i = 0; i < attribute1.length; i++) {
        if ( attribute1.options[i].selected ) {
            y=current1++
            temp1[y] = attribute1.options[i].value;
            tempa[y] = attribute1.options[i].text; 
            tempaa[y] = attribute1.options[i].value+","+y;
        } else {
            y=current2++
            temp2[y] = attribute1.options[i].value; 
            tempb[y] = attribute1.options[i].text;
        }
    }
    
    tempaa.sort();
    
    for (var i = 0; i < temp1.length; i++) {
        var val = tempaa[i];
        var tmp = val.split(',');
        
        attribute2.options[i] = new Option();
        attribute2.options[i].value = temp1[tmp[1]];
        attribute2.options[i].text =  tempa[tmp[1]];
    }
    
    ClearList(attribute1,attribute1);
    if (temp2.length>0) {
        for (var i = 0; i < temp2.length; i++) {
            attribute1.options[i] = new Option();
            attribute1.options[i].value = temp2[i];
            attribute1.options[i].text =  tempb[i];
        }
    }
}

//全件移動
function moves(sides) {
    var temp5 = new Array();
    var tempc = new Array();
    var tempaa = new Array();
    var current5 = 0;
    var z=0;
    
    if (sides == "right") {
        attribute5 = document.forms[0].CATEGORY_SELECTED1;
        attribute6 = document.forms[0].CATEGORY_NAME;  
    } else {
        attribute5 = document.forms[0].CATEGORY_NAME;
        attribute6 = document.forms[0].CATEGORY_SELECTED1;
    }
    
    for (var i = 0; i < attribute6.length; i++) {
        z=current5++
        temp5[z] = attribute6.options[i].value;
        tempc[z] = attribute6.options[i].text;
        tempaa[z] = attribute6.options[i].value+","+z;
    }
    
    for (var i = 0; i < attribute5.length; i++) {
        z=current5++
        temp5[z] = attribute5.options[i].value;
        tempc[z] = attribute5.options[i].text; 
        tempaa[z] = attribute5.options[i].value+","+z;
    }
    
    tempaa.sort();
    
    for (var i = 0; i < temp5.length; i++) {
        var val = tempaa[i];
        var tmp = val.split(',');
    
        attribute6.options[i] = new Option();
        attribute6.options[i].value = temp5[tmp[1]];
        attribute6.options[i].text =  tempc[tmp[1]];
    }
    
    ClearList(attribute5,attribute5);

}

