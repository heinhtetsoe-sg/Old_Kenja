function btn_submit(cmd) {

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

//合計値の計算
function CalculateSum(obj) {

    textbook = obj.value.split("-");
    div     = Number(textbook[5]);
    price   = Number(textbook[6]);

    num1 = parseInt(document.forms[0].DIV1_NUM_CNT.value);
    num2 = parseInt(document.forms[0].DIV2_NUM_CNT.value);
    num3 = parseInt(document.forms[0].DIV3_NUM_CNT.value);
    numT = parseInt(document.forms[0].TOTAL_NUM_CNT.value);

    price1 = parseInt(document.forms[0].DIV1_PRICE_CNT.value.replace(",", ""));
    price2 = parseInt(document.forms[0].DIV2_PRICE_CNT.value.replace(",", ""));
    price3 = parseInt(document.forms[0].DIV3_PRICE_CNT.value.replace(",", ""));
    priceT = parseInt(document.forms[0].TOTAL_PRICE_CNT.value.replace(",", ""));

    number = parseInt("1");

    if (obj.checked) {
        switch (div) {
        case 1:
            num1   += number;
            price1 += price;
            break;
        case 2:
            num2   += number;
            price2 += price;
            break;
        case 3:
            num3   += number;
            price3 += price;
            break;
        }
        numT   += number;
        priceT += price;
    } else {
        switch (div) {
        case 1:
            if (num1 > 0) num1 -= number;
            if (price1 > 0) price1 -= price;
            break;
        case 2:
            if (num2 > 0) num2 -= number;
            if (price2 > 0) price2 -= price;
            break;
        case 3:
            if (num3 > 0) num3 -= number;
            if (price3 > 0) price3 -= price;

            break;
        }
        if (numT > 0)   numT   -= number;
        if (priceT > 0) priceT -= price;
    }

    document.forms[0].DIV1_NUM_CNT.value = parseInt(num1);
    document.forms[0].DIV2_NUM_CNT.value = parseInt(num2);
    document.forms[0].DIV3_NUM_CNT.value = parseInt(num3);
    document.forms[0].TOTAL_NUM_CNT.value = parseInt(numT);
    document.forms[0].DIV1_PRICE_CNT.value = number_format(price1);
    document.forms[0].DIV2_PRICE_CNT.value = number_format(price2);
    document.forms[0].DIV3_PRICE_CNT.value = number_format(price3);
    document.forms[0].TOTAL_PRICE_CNT.value = number_format(priceT);

    document.getElementById('DIV1_NUM_CNT').innerHTML = parseInt(num1);
    document.getElementById('DIV2_NUM_CNT').innerHTML = parseInt(num2);
    document.getElementById('DIV3_NUM_CNT').innerHTML = parseInt(num3);
    document.getElementById('TOTAL_NUM_CNT').innerHTML = parseInt(numT);
    document.getElementById('DIV1_PRICE_CNT').innerHTML = number_format(price1);
    document.getElementById('DIV2_PRICE_CNT').innerHTML = number_format(price2);
    document.getElementById('DIV3_PRICE_CNT').innerHTML = number_format(price3);
    document.getElementById('TOTAL_PRICE_CNT').innerHTML = number_format(priceT);

    //チェック対象と同じ教科書で違う科目は使用不可
    allVal = document.forms[0].ALLVAL;
    subclasscd = textbook[0]+textbook[1]+textbook[2]+textbook[3];
    textbookcd = textbook[4];

    var temp = new Array();
    var y=0;
    search_check_on = false;

    if (obj.checked == false) {
        for (var i = 0; i < allVal.length; i++) {
            meisai1 = allVal[i].value.split("-");
            meisai1_subclasscd = meisai1[0]+meisai1[1]+meisai1[2]+meisai1[3];
            meisai1_textbookcd = meisai1[4];

            if (subclasscd != meisai1_subclasscd && textbookcd == meisai1_textbookcd) {
                no_check_on = true;
                for (var j = 0; j < allVal.length; j++) {
                    meisai2 = allVal[j].value.split("-");
                    meisai2_subclasscd = meisai2[0]+meisai2[1]+meisai2[2]+meisai2[3];
                    meisai2_textbookcd = meisai2[4];

                    if (textbookcd == meisai2_textbookcd) {
                        //異なる科目が2科目以上ある場合
                        if (subclasscd != meisai2_subclasscd && meisai1_subclasscd != meisai2_subclasscd) {
                            if (document.forms[0]["CHECKED\[\]"][j].checked) {
                                search_check_on = true;
                            }
                        //異なる科目が1科目の場合
                        } else if (subclasscd != meisai2_subclasscd) {
                            if (document.forms[0]["CHECKED\[\]"][j].checked) {
                                search_check_on = true;
                            }
                        }

                        //同一教科書の科目にチェッが付いているか
                        if (document.forms[0]["CHECKED\[\]"][j].checked) {
                            no_check_on = false;
                        }
                    }
                }

                //チェックが1つも付いていない場合、値をセット
                if (no_check_on) {
                    temp[y] = document.forms[0]["CHECKED\[\]"][i];
                    y++
                }
            }
        }

        //同じ教科書の科目が全てチェックなしの場合、他の科目のチェックを使用可にする
        if (temp.length > 0) {
            for (var i = 0; i < temp.length; i++) {
                temp[i].disabled = false;
            }
        }

        //同じ教科書の違う科目にチェックありの場合、使用不可にする
        if (search_check_on) {
            obj.disabled = true;
        } else {
            obj.disabled = false;
        }

    } else {
        for (var i = 0; i <= allVal.length; i++) {
            meisai = allVal[i].value.split("-");
            meisai_subclasscd = meisai[0]+meisai[1]+meisai[2]+meisai[3];
            meisai_textbookcd = meisai[4];

            //チェックを付けた科目以外は使用不可（同一教科書）
            if (subclasscd != meisai_subclasscd && textbookcd == meisai_textbookcd) {
                document.forms[0]["CHECKED\[\]"][i].disabled = true;
            }
        }
    }

    return;
}

//印刷
function newwin(SERVLET_URL){

    if (document.forms[0].SCHREGNO.value == '') {
        alert('{rval MSG304}\n　　（生徒）');
        return;
    }

    action = document.forms[0].action;
    target = document.forms[0].target;

//    url = location.hostname;
//    document.forms[0].action = "http://" + url +"/cgi-bin/printenv.pl";
    document.forms[0].action = SERVLET_URL +"/KNJB";
    document.forms[0].target = "_blank";
    document.forms[0].submit();

    document.forms[0].action = action;
    document.forms[0].target = target;
}
