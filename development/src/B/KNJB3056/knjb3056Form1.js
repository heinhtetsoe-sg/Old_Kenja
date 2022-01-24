function btn_submit(cmd) {
    //取消
    if (cmd == "clear" && !confirm('{rval MSG106}')) {
        return true;
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

//更新時のウインドウ表示とその前処理
function doPopup(REQUESTROOT){
    for(var i=0;i<document.forms[0].CHAIRLIST_MAX_CNT.value;i++){
        var obj = document.forms[0]['CHAIRLIST_'+i];
        var ret = new Array();
        for (var j=0;j<obj.options.length;j++) {
            ret.push(obj.options[j].value);
        }
        document.forms[0]['CHAIRLIST_VALUE_'+i].value=ret.join(',');
    }
    for(var i=0;i<document.forms[0].CHAIRLIST2_MAX_CNT.value;i++){
        var obj = document.forms[0]['CHAIRLIST2_'+i];
        var ret = new Array();
        for (var j=0;j<obj.options.length;j++) {
            ret.push(obj.options[j].value);
        }
        document.forms[0]['CHAIRLIST2_VALUE_'+i].value=ret.join(',');
    }
    loadwindow('knjb3056index.php?cmd=subForm2',0,0,400,350);
}

//権限チェック
function OnAuthError() {
    alert('{rval MSG300}');
    closeWin();
}

//上行間の左右移動
function chairToChair1(){
    var chairCd1 = document.forms[0].CHAIRCD1.value;
    var idx='';
    for(var i=0;i<document.forms[0].CHAIRLIST_MAX_CNT.value;i++){
        if(document.forms[0]['CHAIRLIST_'+i].getAttribute('data-chairCd') ==chairCd1){
            idx='CHAIRLIST_'+i;
        }
    }
    if(idx==''){
        return;
    }
    for(var i=0;i<document.forms[0].CHAIRLIST_MAX_CNT.value;i++){
        if ('CHAIRLIST_'+i == idx){
            continue;
        }
        var obj = document.forms[0]['CHAIRLIST_'+i];
        for (var j = obj.options.length - 1; 0 <= j; --j) {
            if(obj.options[j].selected) {
                var option_add = document.createElement("option");
                option_add.setAttribute("value", obj.options[j].value);
                option_add.setAttribute("data-prop", obj.options[j].getAttribute("data-prop"));
                option_add.setAttribute("data-prop2", obj.options[j].getAttribute("data-prop2"));
                option_add.innerHTML = obj.options[j].text;
                document.forms[0][idx].appendChild(option_add);
                obj.removeChild(obj.options[j]);
            }
        }
    }
    sortCombo(document.forms[0][idx]);
}

//下行の上移動
function chairToChair2(){
    var chairCd2 = document.forms[0].CHAIRCD2.value;
    var idx='';
    for(var i=0;i<document.forms[0].CHAIRLIST_MAX_CNT.value;i++){
        if(document.forms[0]['CHAIRLIST_'+i].getAttribute('data-chairCd') ==chairCd2){
            idx='CHAIRLIST_'+i;
        }
    }
    if(idx==''){
        return;
    }
    for(var i=0;i<document.forms[0].CHAIRLIST2_MAX_CNT.value;i++){
        var obj = document.forms[0]['CHAIRLIST2_'+i];
        for (var j = obj.options.length - 1; 0 <= j; --j) {
            if(obj.options[j].selected) {
                var option_add = document.createElement("option");
                option_add.setAttribute("value", obj.options[j].value);
                option_add.setAttribute("data-prop", obj.options[j].getAttribute("data-prop"));
                option_add.setAttribute("data-prop2", obj.options[j].getAttribute("data-prop2"));
                option_add.innerHTML = obj.options[j].text;
                document.forms[0][idx].appendChild(option_add);
                obj.removeChild(obj.options[j]);
            }
        }
    }
    sortCombo(document.forms[0][idx]);
}

//上行の下移動
function retrunHr(){
    var idxList={};
    for(var i=0;i<document.forms[0].CHAIRLIST2_MAX_CNT.value;i++){
        idxList[document.forms[0]['CHAIRLIST2_'+i].getAttribute('data-prop')] ='CHAIRLIST2_'+i; 
    }
    for(var i=0;i<document.forms[0].CHAIRLIST_MAX_CNT.value;i++){
        var obj = document.forms[0]['CHAIRLIST_'+i];
        for (var j = obj.options.length - 1; 0 <= j; --j) {
            if(obj.options[j].selected) {
                prop = obj.options[j].getAttribute("data-prop");
                if(prop!=''){
                    var option_add = document.createElement("option");
                    option_add.setAttribute("value", obj.options[j].value);
                    option_add.setAttribute("data-prop", obj.options[j].getAttribute("data-prop"));
                    option_add.setAttribute("data-prop2", obj.options[j].getAttribute("data-prop2"));
                    option_add.innerHTML = obj.options[j].text;
                    if(idxList[prop]){
                        document.forms[0][idxList[prop]].appendChild(option_add);
                    }
                    obj.removeChild(obj.options[j]);
                }
            }
        }
    }
    for(var i=0;i<document.forms[0].CHAIRLIST2_MAX_CNT.value;i++){
        sortCombo(document.forms[0]['CHAIRLIST2_'+i]);
    }
}

function sortCombo(obj){
    var tmpAry = new Array();
    for (var i=0;i<obj.options.length;i++) {
        tmpAry[i] = new Array();
        tmpAry[i][0] = obj.options[i].text;
        tmpAry[i][1] = obj.options[i].value;
        tmpAry[i][2] = obj.options[i].getAttribute("data-prop");
        tmpAry[i][3] = obj.options[i].getAttribute("data-prop2");
    }
    tmpAry.sort(function(a,b){
        if( a[3] < b[3] ) return -1;
        if( a[3] > b[3] ) return 1;
        return 0;
	});
    while (obj.options.length > 0) {
        obj.options[0] = null;
    }
    for (var i=0;i<tmpAry.length;i++) {
        var option_add = document.createElement("option");
        option_add.setAttribute("value", tmpAry[i][1]);
        option_add.setAttribute("data-prop", tmpAry[i][2]);
        option_add.setAttribute("data-prop2", tmpAry[i][3]);
        option_add.innerHTML = tmpAry[i][0];
        obj.appendChild(option_add);
    }
    return;
}