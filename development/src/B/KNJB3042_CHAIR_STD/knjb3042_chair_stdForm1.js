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
        $(stdOption).text("　 " + $(stdOption).attr('data-prop') + ":" + $(stdOption).attr('data-prop2'));
        $("select[name=STDLIST][data-chaircd=" + chairCd + "]").append(stdOption);
        // 移動先の SELECT の UPDATEフラグを立てる
        $("select[name=STDLIST][data-chaircd=" + chairCd + "]").attr("data-update", "1");
    }

    // 受講生の重複チェック
    checkStdOverlap(chairCd);

    return;
}

// 受講生の重複チェック
function checkStdOverlap(chairCd) {

    var ajaxParam = getUpdateChairList(chairCd);

    $.ajax({
        url:'knjb3042_chair_stdindex.php',
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
                $(stdOption).text("重 " + $(stdOption).attr('data-prop') + ":" + $(stdOption).attr('data-prop2'));
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
