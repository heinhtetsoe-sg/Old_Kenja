// サブミット
function btn_submit(cmd) {

    if (cmd === 'update') {
        var updateChairList = getUpdateChairList('');
        $("input[name=UPDATE_CHAIRLIST]").val(JSON.stringify(updateChairList));
    }
    // 処理中画面表示
    $('#lockScreen').css({'width': $(document).width() + "px"});
    $('#lockScreen').css({'height': $(document).height() + "px"});
    $('#lockScreen').css('display','table');

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

// 講座時間割の画面をリロード
function parentReload() {
    if (this.opener) {
        this.opener.btn_submit('edit');
    }
}

// 日付変更時
function dateChange(object){

    var dateValue = object;
    if (!dateValue){
        return false;
    }

    var dateVal = dateValue.replace(/\//g, "-");
    if (dateVal < $("input[name=SEMESTER_START]").val() ||
        dateVal > $("input[name=SEMESTER_END]").val()) {
        alert("適用開始日は学期範囲内で入力してください。");
        $("input[name=START_DATE]").val($("input[name=START_DATE]")[0].defaultValue);
        return false;
    }
    // 処理中画面表示
    $('#lockScreen').css({'width': $(document).width() + "px"});
    $('#lockScreen').css({'height': $(document).height() + "px"});
    $('#lockScreen').css('display','table');

    document.forms[0].cmd.value = 'main';
    document.forms[0].submit();

    return false;
}

function dataMove(chairCd) {

    var selectStdList = $("select[name=STDLIST][data-chaircd!=" + chairCd + "] option:selected");
    // 同じ受講生がいないかチェック
    for (var index = 0; index < selectStdList.length; index++) {
        var stdOption = selectStdList[index];
        var stdCd = $(stdOption).attr('data-prop');
        var checkStd = $("select[name=STDLIST][data-chaircd=" + chairCd + "] option[data-prop=" + stdCd + "]");
        if (checkStd.length > 0) {
            alert("同じ受講生が登録されています。");
            return;
        }
    }
    // 選択している受講生に同じ受講生がいないかチェック
    for (var index = 0; index < selectStdList.length; index++) {
        var stdOption = selectStdList[index];
        var stdCd = $(stdOption).attr('data-prop');
        var checkStd = $("select[name=STDLIST][data-chaircd!=" + chairCd + "] option:selected[data-prop=" + stdCd + "]");
        if (checkStd.length > 1) {
            alert("同じ受講生が複数選択されています。");
            return;
        }
    }

    // 受講生の移動
    for (let index = 0; index < selectStdList.length; index++) {
        var stdOption = selectStdList[index];
        // 移動元の SELECT の UPDATEフラグを立てる
        var srcChairCd = $(stdOption).parent().attr('data-chaircd');
        $("select[name=STDLIST][data-chaircd=" + srcChairCd + "]").attr("data-update", "1");
        // OPTION の移動
        $(stdOption).prop("selected", false);
        $(stdOption).text($(stdOption).text().replace("重 ", "　 "));
        $("select[name=STDLIST][data-chaircd=" + chairCd + "]").append(stdOption);
        // 移動先の SELECT の UPDATEフラグを立てる
        $("select[name=STDLIST][data-chaircd=" + chairCd + "]").attr("data-update", "1");
    }

    // 受講生の重複チェック
    checkStdOverlap(chairCd);

    sortCombo(document.querySelector("select[name=STDLIST][data-chaircd='" + chairCd + "']"), true);

    return;
}

// 受講生の重複チェック
function checkStdOverlap(chairCd) {

    var ajaxParam = getUpdateChairList(chairCd);

    $.ajax({
        url:'knjb3042_chair_std_selectindex.php',
        type:'POST',
        data:{
            AJAX_PARAM : JSON.stringify(ajaxParam),
            cmd :'getStdOverlap',
            YEAR       : $("input[name=YEAR]").val(),
            SEMESTER   : $("input[name=SEMESTER]").val(),
            START_DATE : $("input[name=START_DATE]").val(),
            SCH_PTRN   : $("input[name=SCH_PTRN]").val(),
            BSCSEQ     : $("input[name=BSCSEQ]").val()
        }
    }).done(function(data, textStatus, jqXHR) {
        var paramList = $.parseJSON(data);
        var stdList = paramList["STDLIST"];

        for (var index = 0; index < stdList.length; index++) {
            var stdCd = stdList[index];
            // 重複のあった受講生に重複マークを追加する
            var stdOption = $("select[name=STDLIST][data-chaircd=" + chairCd + "] option[data-prop=" + stdCd + "]");
            if (stdOption.length > 0) {
                $(stdOption).text($(stdOption).text().replace("　 ", "重 "));
            }
        }

    });

    return;
}

// 更新されている講座の受講生リストを作成
function getUpdateChairList(chairCd) {

    var resultChairList = {};

    // 講座コード
    resultChairList["CHAIRCD"] = chairCd;
    // 受講生の取得
    resultChairList["STDLIST"] = [];
    var stdList = $("select[name=STDLIST][data-chaircd=" + chairCd + "] option");
    for (var index = 0; index < stdList.length; index++) {
        var std = stdList[index];
        resultChairList["STDLIST"].push($(std).attr("data-prop"));
    }

    resultChairList["UPDATE_CHAIRLIST"] = [];
    var updateChairList = $("select[name=STDLIST][data-update=1][data-chaircd!=" + chairCd + "]");
    for (let index = 0; index < updateChairList.length; index++) {
        var chair = updateChairList[index];

        var chairInfo = {};
        chairInfo["CHAIRCD"] = $(chair).attr("data-chaircd");
        chairInfo["STDLIST"] = [];
        var stdOption = $("select[name=STDLIST][data-chaircd=" + $(chair).attr("data-chaircd") + "] option");
        // var stdOption = $("select[name=STDLIST][data-chaircd=" + $(chair).attr(data-chaircd) + "] option");
        for (var index2 = 0; index2 < stdOption.length; index2++) {
            var option = stdOption[index2];
            chairInfo["STDLIST"].push($(option).attr("data-prop"));
        }
        resultChairList["UPDATE_CHAIRLIST"].push(chairInfo);
    }

    return resultChairList;
}

//下行の上移動
function hrToChair(){
    var trgtChaircd = document.forms[0].HR_TO_CHAIR_CMB.value;
    var trgtChair = $("select[name=STDLIST][data-chaircd=" + trgtChaircd + "]");

    for(var i = 0; i < document.forms[0].HR_CNT.value; i++){
        var obj = document.forms[0]['HR_STDLIST_'+i];
        for (var j = obj.options.length - 1; 0 <= j; --j) {
            if(obj.options[j].selected) {
                var option_add = document.createElement("option");
                option_add.setAttribute("value", obj.options[j].value);
                option_add.setAttribute("data-prop", obj.options[j].getAttribute("data-prop"));
                option_add.setAttribute("data-prop2", obj.options[j].getAttribute("data-prop2"));
                option_add.setAttribute("data-prop3", obj.getAttribute("data-hr"));
                option_add.setAttribute("data-sortkey", obj.options[j].getAttribute("data-sortkey"));
                option_add.innerHTML = obj.options[j].text;
                trgtChair.append(option_add);
                obj.removeChild(obj.options[j]);
            }
        }
    }

    // 移動先にUPDATEフラグを立てる
    trgtChair.attr("data-update", "1");

    // 受講生の重複チェック
    checkStdOverlap(trgtChaircd);

    sortCombo(document.querySelector("select[name=STDLIST][data-chaircd='" + trgtChaircd + "']"), true);
}

//上行の下移動
function retrunHr(){
    var idxList={};
    for(var i = 0; i < document.forms[0].HR_CNT.value; i++){
        idxList[document.forms[0]["HR_STDLIST_"+i].getAttribute("data-hr")] ="HR_STDLIST_"+i;
    }

    var hrToChairCmbOpt = document.forms[0].HR_TO_CHAIR_CMB.options;
    var chairList = [];
    for (i = 0; i < hrToChairCmbOpt.length; i++) {
        chairList[i] = hrToChairCmbOpt[i].value;
    }

    for(var i = 0; i < document.forms[0].CHAIR_CNT.value; i++){
        var selectedStd = $("select[name=STDLIST][data-chaircd="+ chairList[i] +"] option:selected");
        if (selectedStd.length > 0) {
            selectedStd.each(function () {
                var trgtHr = $(this).data("prop3");
                if (trgtHr != "") {
                    var option_add = document.createElement("option");
                    option_add.setAttribute("value", $(this).val());
                    option_add.setAttribute("data-prop", $(this).data("prop"));
                    option_add.setAttribute("data-prop2", $(this).data("prop2"));
                    option_add.setAttribute("data-sortkey", $(this).data("sortkey"));
                    option_add.innerHTML = $(this).text().replace("重", "　 ");
                    if(idxList[trgtHr]){
                        document.forms[0][idxList[trgtHr]].appendChild(option_add);
                    }
                    $(this).remove();
                }
            });

            // 移動元にUPDATEフラグを立てる
            $("select[name=STDLIST][data-chaircd="+ chairList[i] +"]").attr("data-update", "1");
        }
    }
    for(var i=0;i<document.forms[0].HR_CNT.value;i++){
        sortCombo(document.forms[0]['HR_STDLIST_'+i], false);
    }

}

function sortCombo(obj, isChairList){
    var tmpAry = new Array();
    for (var i=0;i<obj.options.length;i++) {
        tmpAry[i] = new Array();
        tmpAry[i][0] = obj.options[i].text;
        tmpAry[i][1] = obj.options[i].value;
        tmpAry[i][2] = obj.options[i].getAttribute("data-prop");
        tmpAry[i][3] = obj.options[i].getAttribute("data-prop2");
        if (isChairList) {
            tmpAry[i][4] = obj.options[i].getAttribute("data-prop3");
        }
        tmpAry[i][5] = obj.options[i].getAttribute("data-sortkey");
    }
    tmpAry.sort(function(a,b){
        if( a[5] < b[5] ) return -1;
        if( a[5] > b[5] ) return 1;
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
        if (isChairList) {
            option_add.setAttribute("data-prop3", tmpAry[i][4]);
        }
        option_add.setAttribute("data-sortkey", tmpAry[i][5]);
        option_add.innerHTML = tmpAry[i][0];
        obj.appendChild(option_add);
    }
    return;
}