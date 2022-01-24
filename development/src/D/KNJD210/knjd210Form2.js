function check(obj){
    obj.value = toInteger(obj.value);
    var err = false;
    switch(obj.name){
    case "AVG_HOSEI_RATE":
        if (obj.value == ""){
            alert("数値を入力してください。");
            err = true;
        }else if (parseInt(obj.value) > 80 || parseInt(obj.value) < 60){
            alert("50以上80以下の値を入力してください。");
            err = true;
        }
        break;
    case "MOD_AVG":
        if (obj.value == ""){
            alert("数値を入力してください。");
            err = true;
        }else if (parseInt(obj.value) > 100){
            alert("100以下の値を入力してください。");
            err = true;
        }
        break;
    }
    if (err){
        obj.value = obj.defaultValue;
    }
}
function calc(){
    with(document.forms[0]){
        if (!getRadioValue('STATUS')){
            alert("現在の状態を選択してください");
            return;
        }
        if (MOD_AVG.value == ""){
            alert("平均点を入力してください");
            return;
        }
        if (AVG_HOSEI_RATE.value == ""){
            alert("平均点基準点を入力してください");
            return;
        }
        top.main_frame.right_frame.calcAvgRevise(getRadioValue('STATUS'), MOD_AVG.value, AVG_HOSEI_RATE.value);
    }
    top.main_frame.right_frame.document.forms[0].AVGMOD_FLG.value = 1;
    top.main_frame.right_frame.closeit();
}
function getRadioValue(radioName) {
    var collection;
    collection = document.all[radioName];
    for (i=0;i<collection.length;i++) {
        if (collection[i].checked)
        return(collection[i].value);
    }
}
function setAvg(obj){
    document.forms[0].MOD_AVG.value = obj.value;
}