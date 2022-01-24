function check(obj){
    switch(obj.name){
        case "DATE_H":
        case "OCCURTIME_H":
        case "BEDTIME_H":
        case "RISINGTIME_H":
            var h = toInteger(obj.value);
            if (parseInt(h) < 0 || parseInt(h) > 23){
                alert("０から２３の数字を入力してください。");
                obj.focus();
            }else{
                obj.value = h;
            }
            break;
        case "DATE_M":
        case "OCCURTIME_M":
        case "BEDTIME_M":
        case "RISINGTIME_M":
            var m = toInteger(obj.value);
            if (parseInt(m) < 0 || parseInt(m) > 59){
                alert("０から５９の数字を入力してください。");
                obj.focus();
            }else{
                obj.value = m;
            }
            break;
        case "TEMPERATURE":
            var f = toFloat(obj.value);
            if (f.match(/^[0-9]+\.[0-9]{1}$/)){
                obj.value = f;
            }else{
                alert("小数点１桁目まで入力して下さい。");
                obj.focus();
            }
            break;
        case "REMARK":
            if (getByte(obj.value) > 80){
                alert("全角４０、半角８０文字以内で入力してください。");
                obj.focus();
            }
            break;
    }
}


