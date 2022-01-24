function func(a, b){return(b-a);}//比較関数

function btn_submit(cmd) {


    //評定下限値の大小チェック
    var assesslow = document.forms[0]["ASSESSLOW\[\]"];
    for (var i = 0; i < assesslow.length-1; i++){
        if( parseInt(assesslow[i].value) <= parseInt(assesslow[i+1].value)){
            alert('{rval MSG901}');
            return;
        }
    }


    if (cmd == 'sim'){
        //シュミレーション
        avgval = new Array();
        for (i=0;i<top.main_frame.right_frame.document.getElementsByTagName("TAG_AVGVAL").length;i++){
            avgval[i] = parseInt(top.main_frame.right_frame.document.getElementsByTagName("TAG_AVGVAL")[i].value);
        }

        //sort(降順)
        avgval.sort(func);
        var assesslow = document.forms[0]["ASSESSLOW\[\]"];
        var num = new Array();
        for (var i = 0; i <= assesslow.length; i++){
            num[i] = 0;
        }

        for (var i = 0; i < avgval.length; i++){
            for (var j = 0; j < assesslow.length; j++){
                if (avgval[i] >= assesslow[j].value){
                    num[j]++;
                    break;
                }else if (avgval[i] < assesslow[assesslow.length-1].value){
                    num[assesslow.length]++;
                    break;
                }
            }
        }

        num.reverse();
        for (var i = 0; i < num.length; i++){
            outputLAYER('NUM_'+parseInt(i+1) , num[i]);
            outputLAYER('RATE_'+parseInt(i+1) ,Math.round((num[i]/avgval.length*100)*10,0)/10);
        }

    }else if (cmd == 'exec'){ 

        //相対評価処理実行
        var val = top.main_frame.right_frame.document.forms[0]["VALUATION\[\]"];
        avgval = new Array();
        for (i=0;i<top.main_frame.right_frame.document.getElementsByTagName("TAG_AVGVAL").length;i++){
            avgval[i] = parseInt(top.main_frame.right_frame.document.getElementsByTagName("TAG_AVGVAL")[i].value);
        }
        var assesslow = document.forms[0]["ASSESSLOW\[\]"];

        for (var i = 0; i < val.length; i++){
            for (var j = 0; j < assesslow.length; j++){
                if (parseInt(avgval[i]) >= parseInt(assesslow[j].value)){
                    val[i].value = assesslow.length+1-j;
                    break;
                }else if (parseInt(avgval[i]) > parseInt(assesslow[0].value)){
                    val[i].value = assesslow.length+1;
                    break;
                }else if (parseInt(avgval[i]) < parseInt(assesslow[assesslow.length-1].value)){
                    val[i].value = 1;
                    break;
                }
            }
        }
        document.forms[0].cmd.value = cmd;
        document.forms[0].submit();
        top.main_frame.right_frame.document.forms[0].RELATIVED_FLG.value = 1;
        top.main_frame.right_frame.closeit();
        return false;
    }else{
        document.forms[0].cmd.value = cmd;
        document.forms[0].submit();
        return false;
    }
}

function check(obj,id){
    //数字チェック
    if (isNaN(obj.value) || obj.value == ""){
        alert('{rval MSG907}');
        obj.value = obj.defaultValue;
        return;
    }
   //入力上下限値を超えて設定チェック
    if (parseInt(document.forms[0].MAX_ASSESSHIGH.value) < parseInt(obj.value)){
        alert('{rval MSG901}(入力上限値を超えています。)');
        obj.value = obj.defaultValue;
        return;
    }
    if (parseInt(obj.value) < parseInt(document.forms[0].MIN_ASSESSLOW.value)){
        alert('{rval MSG901}(入力下限値を超えています。)');
        obj.value = obj.defaultValue;
        return;
    }
    //下位の上限値を算出しセット
    val = obj.value -1;
    if (parseInt(val) < parseInt(document.forms[0].MIN_ASSESSLOW.value)){
        document.all['strID'+parseInt(id-1)].innerHTML=(document.forms[0].MIN_ASSESSLOW.value)
    } else {
        document.all['strID'+parseInt(id-1)].innerHTML=(obj.value -1)
    }
}


/*
    if (parseInt(assesslow[0].value) > parseInt(document.forms[0].ASSESSLEVELCNT.value)){
        alert('{rval MSG901}');
        obj.value = obj.defaultValue;
    }
*/
/*
    for (var i = 0; i < assesslow.length; i++){
        if (i < assesslow.length-1 && parseInt(assesslow[i+1].value) >= parseInt(assesslow[i].value)){
            alert('{rval MSG901}');
            obj.value = obj.defaultValue;
        }
    }
}
*/

