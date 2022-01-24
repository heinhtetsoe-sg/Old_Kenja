function btn_submit(cmd) {
    //取消確認
    if (cmd == 'reset' && !confirm('{rval MSG106}')) {
        return;
    }

    //更新
    if (cmd == 'update') {
        if (document.forms[0].GRADE_HR_CLASS.value == '') {
            alert('{rval MSG916}\n　　　( 年組 )');
            return;
        }
        if (document.forms[0].SUBCLASSCD.value == '') {
            alert('{rval MSG916}\n　　　( 教科 )');
            return;
        }
    }

    //コピー確認
    if (cmd == 'copy' && !confirm('{rval MSG101}')) {
        return false;
    }

    //フレームロック機能（プロパティの値が1の時有効）
    if (document.forms[0].useFrameLock.value == "1") {
        if (cmd == 'update') {
            updateFrameLock();
        }
    }
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

//数字チェック
function NumCheck(num) {
    num = toInteger(num);

    //範囲チェック
    if (num.length > 0 && !(1 <= num && num <= 999)) {
        alert('{rval MSG916}\n( 1 ～ 999 )');
        num = '';
    }
    return num;
}

//変更対象確認
function selectRowList() {
    //チェックボックスが全offなら変更ボタン使用不可
    var check_flg = false;
    for (var i=0; i < document.forms[0].elements.length; i++) {
        if (document.forms[0].elements[i].name == "CHECK[]") {
            if (document.forms[0].elements[i].checked == true) {
                check_flg = true;
            }
        }
    }
	if (check_flg == true) {
        document.forms[0].btn_update.disabled = false;
	} else {
	    document.forms[0].btn_update.disabled = true;
	}
}

//変更対象行自動制御
var selectedRow = 0;
function selectRowList2(idx) {
    var list = document.getElementById('list');
    var chk = document.forms[0]["CHECK\[\]"];
    selectedRow = idx - 1;

    //チェックon/off
    if (chk.length) {
        if (chk[selectedRow].disabled == false) {
            if (!chk[selectedRow].checked) {
                chk[selectedRow].checked = true;
                document.forms[0].btn_update.disabled = false;
            }
        }
    }
}

//ボタンの使用不可
function OptionUse(obj) {
    var check_flg = false;

    for (var i=0; i < document.forms[0].elements.length; i++) {
        if (document.forms[0].elements[i].name == "CHECK[]" && document.forms[0].elements[i].checked == true) {
            check_flg = true;
        }
    }

	if (check_flg == true) {
        document.forms[0].btn_update.disabled = false;
	} else {
	    document.forms[0].btn_update.disabled = true;
	}
}

//計算
function Calc(unitTestCnt, rowCnt, d084) {
    var totalCnt = rowCnt + 1;
    var rowSum1 = new Array(); //列合計 配点(加重値)
    
    for(var idx=1; idx<=totalCnt; idx++){
        rowSum1.push(idx);
        rowSum1[idx] = 0;
    }


    for(var unit=1; unit<=unitTestCnt; unit++){

        var colSum1 = 0; //行合計 配点
        var colSum2 = 0; //行合計 配点(加重値)
        for(var idx=1; idx<=rowCnt; idx++){
            var unitAssesshigh = 0; //配点
            var weighting = 0;      //重み
            var val = 0;            //配点(加重値)
        
            if(document.getElementById("UNIT_ASSESSHIGH_"+unit+"_"+idx)){
                if (document.getElementById("UNIT_ASSESSHIGH_"+unit+"_"+idx).value != ""){
                    unitAssesshigh = eval(document.getElementById("UNIT_ASSESSHIGH_"+unit+"_"+idx).value);
                    colSum1 = colSum1 + unitAssesshigh;
                }
            }
            if(document.getElementById("WEIGHTING_"+unit+"_"+idx)){
                if (document.getElementById("WEIGHTING_"+unit+"_"+idx).value != ""){
                    weighting = eval(document.getElementById("WEIGHTING_"+unit+"_"+idx).value);
                }
            }

            ////配点の入力可否を設定
            //if (weighting != 100){
            //    //入力不可
            //    document.getElementById("UNIT_ASSESSHIGH_"+unit+"_"+idx).readOnly = true;
            //    document.getElementById("UNIT_ASSESSHIGH_"+unit+"_"+idx).style.backgroundColor = "#cccccc";
            //} else {
            //    //入力可
            //    document.getElementById("UNIT_ASSESSHIGH_"+unit+"_"+idx).readOnly = false;
            //    document.getElementById("UNIT_ASSESSHIGH_"+unit+"_"+idx).style.backgroundColor = "#ffffff";
            //}

            //配点(加重値)
            with(document.forms[0]){
                val = unitAssesshigh * (weighting / 100); //配点 * (重み/100)
                if (d084 == '1') {
                    val = Math.round(val);
                } else {
                    //最初のテキストボックスの値を数字に変換
                    var num = parseFloat(unitAssesshigh) * parseFloat(weighting);
                    //小数点の位置を2桁右に移動する（1234567.89にする）
                    val = num / 100;
                    val = Math.round(val * 10) / 10;
                }
                document.getElementById("UNIT_ASSESSHIGH_CALC_"+unit+"_"+idx).innerHTML = val;
                colSum2 = colSum2 + val;
                rowSum1[idx] = rowSum1[idx] + val;
            }

        }
        if(!rowSum1[totalCnt]) rowSum1.push(totalCnt);
        rowSum1[totalCnt] = rowSum1[totalCnt] + colSum2;

        //行合計の設定
        with(document.forms[0]){
            document.getElementById("UNIT_ASSESSHIGH_COL_SUM_"+unit).innerHTML = colSum1;       //行合計 配点
            document.getElementById("UNIT_ASSESSHIGH_CALC_COL_SUM_"+unit).innerHTML = colSum2;  //行合計 配点(加重値)
        }

    }

    for(var idx=1; idx<=totalCnt; idx++){
        //列合計の設定
        with(document.forms[0]){
            document.getElementById("UNIT_ASSESSHIGH_CALC_ROW_SUM_"+idx).innerHTML = rowSum1[idx];   //列合計 配点(加重値)
        }
    }

    return;
}