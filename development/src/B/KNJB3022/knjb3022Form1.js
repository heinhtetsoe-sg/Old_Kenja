var weekArray = ['MON', 'TUE', 'WED', 'THU', 'FRI', 'SAT', 'SUN'];
var weekWaArray = ['月曜日', '火曜日', '水曜日', '木曜日', '金曜日', '土曜日', '日曜日'];
var weekWaAbbvArray = ['月', '火', '水', '木', '金', '土', '日'];
var weekValArray = ['2', '3', '4', '5', '6', '7', '1'];

//画面初期化
function dispInit() {

}

function btn_submit(cmd) {
    //サブミット中、更新ボタン使用不可
    if (document.forms[0].btn_update) {
        document.forms[0].btn_update.disabled = true;
    }

    if (cmd == "edit") {
        // 職員
        var selectStaff = $('select[name=STAFFCD] option:selected');
        if (!selectStaff || selectStaff.length <= 0 || !selectStaff[0].value) {
            alert('職員が選択されていません。');
            return;
        }
    }

    if (cmd == "update") {
        if (document.forms[0].BSCSEQ) {
            showDialog('ptrnUpdateBox','科目別基本時間割更新',ptrnUpdateInitFunc);
        } else {
            $('#lockScreen').css({'width': $(document).width() + "px"});
            $('#lockScreen').css({'height': $(document).height() + "px"});
            $('#lockScreen').css('display','table');

            getContent(cmd);
            document.forms[0].cmd.value = cmd;
            document.forms[0].submit();
            return false;
        }
    } else {
        if(cmd == "reset"){
            if(!confirm('{rval MSG106}')){
                return false;
            }
        }
        $('#lockScreen').css({'width': $(document).width() + "px"});
        $('#lockScreen').css({'height': $(document).height() + "px"});
        $('#lockScreen').css('display','table');

        document.forms[0].cmd.value = cmd;
        document.forms[0].submit();
        return false;
    }
}

// 基本時間割更新初期化
function ptrnUpdateInitFunc() {
    var bscSeqText = $('#BSCSEQ option:selected').text();
    if($('#BSCSEQ').val() != '0'){
        var bscSeqTextParts = bscSeqText.split(' ');
        $('#ptrnUpdate_TITLE').val(bscSeqTextParts[3]);
        $('#PTRN_UPDATE_RADIO1').prop('disabled', false);
        $('#PTRN_UPDATE_RADIO1').prop('checked', true);
        var idx = bscSeqTextParts[0];
    } else {
        $('#PTRN_UPDATE_RADIO1').prop('disabled', true);
        $('#PTRN_UPDATE_RADIO2').prop('checked', true);
        var idx = '0';
    }
    $('label[for=PTRN_UPDATE_RADIO1]')[0].innerHTML = '現在のSEQ(' + idx + ')で上書き保存';
}

function btn_submitPtn(cmd){

    if($('#ptrnUpdate_TITLE').val() == ''){
        alert('タイトルを入力してください。');
        return;
    }

    $('#lockScreen').css({'width': $(document).width() + "px"});
    $('#lockScreen').css({'height': $(document).height() + "px"});
    $('#lockScreen').css('display','table');

    getContent('updatePtrn');
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function btn_close() {
    getContent('');
    var updateDelList = $.parseJSON($('input[name=updateDelData]').val());
    var updateAddList = $.parseJSON($('input[name=updateDelData]').val());

    if(updateDelList.length > 0 || updateAddList.length > 0) {
        if(!confirm('{rval MSG108}')){
            return false;
        }
    }
    closeWin();
}

/**
 * OPTION項目 移動処理
 * ※「クラス」の移動で使用
 *
 * @param srcSelect     移動元
 * @param targetSelect  移動先
 * @param option        全移動の場合'ALL'を指定
 * @param refClassCd    教科の再取得フラグ
 * @returns
 */
function layoutMove(src, target, option, refClassCd) {
    if (!option) { option = ''; }
    if (!refClassCd) { refClassCd = true; }
    // option に ALL が指定されている場合は全て移動する
    if (option == 'ALL') {
        $('select[name='+ src +'] option').prop('selected', true);
    }
    // 選択されているOPTION値を移動し、自身のOPTION値は削除
    $('select[name='+ src +'] option:selected').each(function() {
        $('select[name='+ target +']').append($(this).clone());
        $(this).remove();
    });

    // 移動先ソート用
    var options = $('select[name='+ target +'] option').clone();
    options.sort(function(a, b){
        // ソートは VALUE の値を見て判定する
        if (a.value == b.value) { return 0; }
        if (a.value > b.value) { return 1; }
        return -1;
    });
    $('select[name='+ target +'] option').remove();
    options.each(function() {
        $('select[name='+ target +']').append($(this).clone());
    });

    if (refClassCd) {
        // 教科の再取得
        refreshClass('getClass');
    }
}

//右側 「学級」選択変更
function selectGrade(cmd) {
    return refreshHrClass(cmd);
}
//右側「HRクラス」の再取得(「学級」選択変更)
function refreshHrClass(cmd) {

    // 学級パラメタ設定
    var gradeCd = $('select[name=GRADE_SELECTED] option:selected').val();
    var ajaxParam = {};
    // HRクラスパラメタ設定
    ajaxParam['GRADE'] = gradeCd;

    $.ajax({
        url:'knjb3022index.php',
        type:'POST',
        data:{
            AJAX_PARAM:JSON.stringify(ajaxParam),
            cmd:'getHrClass',
            YEAR:document.forms[0].YEAR.value
        },
        async:false
    }).done(function(data, textStatus, jqXHR) {
        var paramList = $.parseJSON(data);
        // HRクラスのリスト初期化
        $('select[name=GRAND_HR_CLASSCD] option').remove();
        for (var i = 0; i < paramList.length; i++) {
            // HRクラスの左側で選択されているHRクラスは除外
            var isAny = false;
            for (var j = 0; j < $('select[name=GRAND_HR_CLASSCD_SELECTED] option').length; j++) {
                var element = $('select[name=GRAND_HR_CLASSCD_SELECTED] option')[j];
                if ($(element).val() == paramList[i]['VALUE']) {
                    isAny = true;
                    break;
                }
            }
            // $('select[name=GRAND_HR_CLASSCD_SELECTED] option').some(function() {
            //     var val = $(this).val();
            //     if (val == paramList[i]['VALUE']) {
            //         return true;
            //     }
            //     return false;
            // });
            if (!isAny) {
                var option = $('<option>').val(paramList[i]['VALUE']).text(paramList[i]['LABEL']);
                $('select[name=GRAND_HR_CLASSCD]').append(option);
            }
        }
    });
}
//右側「教科」の再取得(「HRクラス」選択変更)
function refreshClass(cmd) {

    var selClass = $('select[name=CLASSCD] option:selected').val();
    var ajaxParam = {};
    // HRクラスパラメタ設定
    ajaxParam['HR_CLASS'] = [];
    $('select[name=GRAND_HR_CLASSCD_SELECTED] option').each(function () {
        ajaxParam['HR_CLASS'].push($(this).val());
    });

    if (ajaxParam['HR_CLASS'].length > 0) {
        $.ajax({
            url:'knjb3022index.php',
            type:'POST',
            data:{
                AJAX_PARAM:JSON.stringify(ajaxParam),
                cmd:'getClass',
                YEAR:document.forms[0].YEAR.value,
                STAFFCD:$('select[name=STAFFCD] option:selected').val(),
                STAFF_CLASS:$('input[name=staffClass]').val()
            },
            async:false
        }).done(function(data, textStatus, jqXHR) {
            var paramList = $.parseJSON(data);
            // 科目のリスト初期化
            $('select[name=CLASSCD] option').remove();
            for (var i = 0; i < paramList.length; i++) {
                var option = $('<option>').val(paramList[i]['VALUE']).text(paramList[i]['LABEL']);
                if (selClass == paramList[i]['VALUE']) {
                    option.prop('selected', true);
                }
                $('select[name=CLASSCD]').append(option);
            }
        });
    } else {
        // 科目のリスト初期化
        $('select[name=CLASSCD] option').remove();
    }

    // 教科パラメタ設定
    var classCd = $('select[name=CLASSCD] option:selected').val();
    if (classCd) {
        // 科目の再取得
        refreshSubclass('getSubclass');
    } else {
        // 科目の初期化
        $('select[name=SUBCLASSCD] option').remove();
    }
}

//右側 「教科」選択変更
function selectClass(cmd) {
    return refreshSubclass(cmd);
}
// 右側 「科目」の再取得
function refreshSubclass(cmd) {

    var ajaxParam = {};

    ajaxParam['HR_CLASS'] = [];
    // HRクラスパラメタ設定
    $('select[name=GRAND_HR_CLASSCD_SELECTED] option').each(function () {
        ajaxParam['HR_CLASS'].push($(this).val());
    });

    // 教科パラメタ設定
    var classCd = $('select[name=CLASSCD] option:selected').val();
    var param = classCd.split('-');
    ajaxParam['CLASSCD'] = param[0];
    ajaxParam['SCHOOL_KIND'] = param[1];

    $.ajax({
        url:'knjb3022index.php',
        type:'POST',
        data:{
            AJAX_PARAM:JSON.stringify(ajaxParam),
            cmd:'getSubclass',
            YEAR:document.forms[0].YEAR.value
        },
        async:false
    }).done(function(data, textStatus, jqXHR) {
        var paramList = $.parseJSON(data);
        // 科目のリスト初期化
        $('select[name=SUBCLASSCD] option').remove();
        for (var i = 0; i < paramList.length; i++) {
            var option = $('<option>').val(paramList[i]['VALUE']).text(paramList[i]['LABEL']);
            $('select[name=SUBCLASSCD]').append(option);
        }
    });
}

/***** 選択した科目をセット *****/
function setSubClass() {

    // 選択されている曜日校時
    var selectElement = $('#' + $('input[name=selectTD]').val().trim())[0];
    if (!selectElement) {
        alert('曜日・校時のセルが選択されていません。');
        return;
    }
    var idArray = $('input[name=selectTD]').val().trim().split("_");
    var week    = idArray[1];
    var period  = idArray[2];

    // HRクラスの一覧
    var selHrClass = $('select[name=GRAND_HR_CLASSCD_SELECTED] option');
    if (!selHrClass || selHrClass.length <= 0) {
        alert('HRクラスが選択されていません。');
        return;
    }
    // 科目
    var selectSubclass = $('select[name=SUBCLASSCD] option:selected');
    if (!selectSubclass || selectSubclass.length <= 0) {
        alert('科目が選択されていません。');
        return;
    }

    // 科目を選択しているHRクラスを取得
    //getCreditHrClass
    var ajaxParam = {};
    var subclassArray = selectSubclass.val().split("-");
    ajaxParam['CLASSCD'] = subclassArray[0];
    ajaxParam['SCHOOL_KIND'] = subclassArray[1];
    ajaxParam['CURRICULUM_CD'] = subclassArray[2];
    ajaxParam['SUBCLASSCD'] = subclassArray[3];

    var hrClassList = [];
    if (ajaxParam['CLASSCD'] <= '90') {
        $.ajax({
            url:'knjb3022index.php',
            type:'POST',
            data:{
                AJAX_PARAM:JSON.stringify(ajaxParam),
                cmd:'getCreditHrClass',
                YEAR:document.forms[0].YEAR.value
            },
            async:false
        }).done(function(data, textStatus, jqXHR) {
            var paramList = $.parseJSON(data);

            for (var i = 0; i < paramList.length; i++) {
                var element = paramList[i];
                var hrClass = {};
                hrClass['GRADE'] = element['GRADE'];
                hrClass['HR_CLASS'] = element['HR_CLASS'];
                hrClassList.push(hrClass);
            }
        });
    } else {
        // 教科コードが９０より大きい場合は選択されている全HRクラス対象
        selHrClass.each(function() {
            var classArray = $(this).val().split("-");
            var hrClass = {};
            hrClass['GRADE'] = classArray[0];
            hrClass['HR_CLASS'] = classArray[1];
            hrClassList.push(hrClass);
        });
    }

    var records = [];
    selHrClass.each(function() {
        var classArray = $(this).val().split("-");
        // HRクラスがあれば追加
        if (!hrClassList.some(function(value){
            if (value['GRADE'] === classArray[0] && value['HR_CLASS'] === classArray[1]) {
                return true;
            }
            return false;
        })) {
            return;
        }

        var rec = {};
        // 曜日校時
        rec['week'] = week;
        rec['period'] = period;
        // HRクラス情報
        rec['grade'] = classArray[0];
        rec['hrclasscd'] = classArray[1];
        rec['hrclassname'] = $(this).text();
        // 科目情報
        rec['classcd'] = subclassArray[0];
        rec['school_kind'] = subclassArray[1];
        rec['curriculum_cd'] = subclassArray[2];
        rec['subclasscd'] = subclassArray[3];
        var subclassText = selectSubclass.text().split(":");
        rec['subclassname'] = subclassText[1];
        records.push(rec);
    });

    //イベントキャンセル
    $(window).off('setCellObjEmptyTargetEvent');

    var cellObj = new CellObj();
    cellObj.setCellObj(selectElement);
    if (!cellObj.checkSubClass(records)) {
        alert('同じ時間に異なる科目は設定できません。');
        return false;
    }
    if (!cellObj.checkHrClassMaxCount(records)) {
        alert('登録可能なHRクラスの件数が最大件数を超えています。');
        return false;
    }
    cellObj.deleteCellAll();
    cellObj.appendRecord(records);

    return true;
}

//更新ボタンの処理
//設定したデータの取得
function getContent(cmd) {

    var deleteDataList = [];
    // 削除する曜日・校時のリスト作成
    $('td[data-update=1]').each(function () {
        var cellObj = new CellObj();
        cellObj.setCellObj($(this)[0]);
        var delData = {};
        delData["WEEK_CD"] = cellObj.cell.week;
        delData["PERIODCD"] = cellObj.cell.period;
        deleteDataList.push(delData);
    });
    $('input[name=updateDelData]').val(JSON.stringify(deleteDataList));

    // 新規に保存の場合は、編集していなくても追加するため、
    // [data-val]に値が入っているものを全て編集済みにする
    if ($('#PTRN_UPDATE_RADIO2').prop('checked')) {
        $('.targetbox').each(function () {
            if ($(this).attr('data-val') != '') {
                $(this).attr('data-update', '1');
            }
        });
    }

    var updateDataList = [];
    // 追加する曜日・校時のリスト作成
    $('td[data-update=1]').each(function () {
        var cellObj = new CellObj();
        cellObj.setCellObj($(this)[0]);

        for (var i = 0; i < cellObj.cell.dataValList.length; i++) {
            var value = cellObj.cell.dataValList[i];

            var updData = {};
            updData["WEEK_CD"] = cellObj.cell.week;
            updData["PERIODCD"] = cellObj.cell.period;
            updData["GRADE"] = value.grade;
            updData["HR_CLASS"] = value.hrclasscd;
            updData["CLASSCD"] = value.classcd;
            updData["SCHOOL_KIND"] = value.school_kind;
            updData["CURRICULUM_CD"] = value.curriculum_cd;
            updData["SUBCLASSCD"] = value.subclasscd;

            updateDataList.push(updData);
        }
    });
    $('input[name=updateAddData]').val(JSON.stringify(updateDataList));

    return;
};


/**
 * HRクラス一覧、教科選択、科目選択を変更
 *
 * @param target 選択中のセル
 * @returns
 */
function selectTDRefresh(target) {

    if (refreshTimer) {
        clearTimeout(refreshTimer);
    }
    if (!target) {
        return;
    }
    var dataVal = $(target).attr('data-val');
    if (!dataVal) {
        return;
    }

    var dataValList = JSON.parse(dataVal);
    if (dataValList.length > 0) {
        // 学級を選択クラスに変更(学級は最初のHRクラス)
        var value = dataValList[0];
        for (var i = 0; i < $('select[name=GRADE_SELECTED] option').length; i++) {
            var element = $('select[name=GRADE_SELECTED] option')[i];
            if (value['grade'] == $(element).val()) {
                $(element).prop('selected', true);
                break;
            }
        }

        // 左側のクラス一覧を全削除
        $('select[name=GRAND_HR_CLASSCD_SELECTED] option').remove();
        // 選択されたセルのHRクラスを追加
        for (var i = 0; i < dataValList.length; i++) {
            var element = dataValList[i];
            var option = $('<option>').val(element['grade'] + '-' + element['hrclasscd']).text(element['hrclassname']);
            $('select[name=GRAND_HR_CLASSCD_SELECTED]').append(option);
        }
        // HRクラス再取得
        refreshHrClass();

        // $('select[name=GRAND_HR_CLASSCD_SELECTED] option').prop('selected', false);
        // // 左側のクラス一覧からセルのHRクラス以外を選択
        // $('select[name=GRAND_HR_CLASSCD_SELECTED] option').each(function () {
        //     var hrclass = $(this).val().split('-');
        //     var isAny = false;
        //     for (var i = 0; i < dataValList.length; i++) {
        //         var val = dataValList[i];
        //         if (val['grade'] == hrclass[0] && val['hrclasscd'] == hrclass[1]) {
        //             isAny = true;
        //             break;
        //         }
        //     }
        //     if (!isAny) {
        //         $(this).prop('selected', true);
        //     }
        // });
        // layoutMove('GRAND_HR_CLASSCD_SELECTED', 'GRAND_HR_CLASSCD', '', false);

        // $('select[name=GRAND_HR_CLASSCD] option').prop('selected', false);
        // for (var i = 0; i < dataValList.length; i++) {
        //     var val = dataValList[i];
        //     // 右側のクラス一覧からセルのHRクラスを選択
        //     $('select[name=GRAND_HR_CLASSCD] option').each(function () {
        //         var hrclass = $(this).val().split('-');
        //        if (val['grade'] == hrclass[0] && val['hrclasscd'] == hrclass[1]) {
        //             $(this).prop('selected', true);
        //         }
        //     });
        // }
        // // 右側選択中のHRクラスを移動
        // layoutMove('GRAND_HR_CLASSCD', 'GRAND_HR_CLASSCD_SELECTED', '', false);
    }

    // 教科再取得
    refreshClass();
    // 教科を選択中のセルの教科にする
    for (var i = 0; i < dataValList.length; i++) {
        var val = dataValList[i];
        $('select[name=CLASSCD] option').each(function () {
            var cls = $(this).val().split('-');
            if (val['classcd'] == cls[0] && val['school_kind'] == cls[1]) {
                $(this).prop('selected', true);
            }
        });
    }

    // 科目を再取得
    refreshSubclass();
    // 科目を選択中のセルの科目にする
    for (var i = 0; i < dataValList.length; i++) {
        var val = dataValList[i];
        $('select[name=SUBCLASSCD] option').each(function () {
            var subclass = $(this).val().split('-');
            if (val['classcd'] == subclass[0] && val['school_kind'] == subclass[1]
                && val['curriculum_cd'] == subclass[2] && val['subclasscd'] == subclass[3]) {
                $(this).prop('selected', true);
            }
        });
    }

}


/****************************
 * マウス処理
 ****************************/

/***** 前回クリックしたTDの色をクリア *****/
function f_clearSelectTDColor() {
    var element = $('#' + $('input[name=selectTD]').val().trim())[0];
    if (element != null) {
        element.style.backgroundColor = "";
    }
}


var refreshTimer = null;
/**
 * クリックイベント処理
 * 曜日・校時のセルを保持
 *
 * @param event
 * @returns
 */
function f_click(event) {
    obj = this;
    //keyEvent
    var key_event = event || window.event;
    //Shift
    var isShift = (key_event.shiftKey);

    //前回にクリックしたTDがあれば、色をリセット
    f_clearSelectTDColor();
    //TDに色を付ける
    document.forms[0].selectTD.value = obj.id;
    obj.style.backgroundColor = "#F5F599";

    // 画面右側のHRクラス・科目のリフレッシュ
    // 体感速度を上げるため、タイマーのコールバックへ登録
    refreshTimer = setTimeout(function() {
        selectTDRefresh(event.target);
    }, 50);
    // HRクラス一覧ポップアップの表示制御
    doclick(event);
    if(!ActiveFlag){
        dodragleave();
    }
    ActiveFlag=false;
}

/**
 * ダブルクリックイベント処理
 *
 * @param event
 * @returns
 */
function f_dblclick(event){
    // ポップアップ画面はないので処理なし
}

/***** ドラッグ開始時の処理 *****/
function f_dragstart(event){
    //ドラッグするデータのid名をDataTransferオブジェクトにセット
    event.dataTransfer.setData("text", event.target.id);
    document.forms[0].startTD.value = event.target.id;

    //クリックしたTDがあれば、色をリセット
    f_clearSelectTDColor();
    DRAGOVER_CURRENT_ID = null;
}

/***** ドラッグ要素がドロップ要素に重なっている間の処理 *****/
//var DRAGOVER_CURRENT_ID;    //画面ちらつきを抑えるためのキャッシュ
function f_dragover(event, obj){
    if(!obj){
        obj=this;
    }
    //dragoverイベントをキャンセルして、ドロップ先の要素がドロップを受け付けるようにする
    event.preventDefault();
    //通過中の色設定
    if(obj.id=='TRASH_BOX'){
        obj.style.backgroundColor = "#F58899";
    } else {
        var idArray1=document.forms[0].startTD.value.split('_');
        var idArray2=obj.id.split('_');

        $('#'+idArray2[0]+'_'+idArray2[1]+'_'+idArray2[2]).css({'background-color': '#F58899'});
    }
}

/***** ドラッグ要素がドロップ要素から出る時の処理 *****/
function f_dragleave(event, obj){

    if(!obj){
        obj=this;
    }
    event.preventDefault();

    if(obj.id=='TRASH_BOX'){
        obj.style.backgroundColor = '#003366';
    } else {
        var idArray1=document.forms[0].startTD.value.split('_');
        var idArray2=obj.id.split('_');
        $('#'+idArray2[0]+'_'+idArray2[1]+'_'+idArray2[2]).css({'background-color': ''});
    }
}

/***** ドラッグの基本イベント *****/
function f_dragevent(event){
    event.preventDefault();
}

/***** コンテキストメニューイベント *****/
function f_contextmenu(){
    return false;
}

/***** ドロップ時の処理 *****/
function f_drop(event){
    obj = this;

    //ドラッグされたデータのid名をDataTransferオブジェクトから取得
    var id_name = event.dataTransfer.getData("text");
    var dragIdArray = id_name.split('_');
    var cntNum = '';
    //ポップアップの時は３番目がある
    if (dragIdArray.length > 3) {
        if (dragIdArray[3] != 'all') {
            cntNum = dragIdArray[3];
        }
    }

    // 入れ替え時はHRクラスでの移動不可
    if ($('#OPERATION_RADIO3').is(':checked') && cntNum) {
        alert('クラス単体での入れ替えは行えません。');
        dodragleave(event);
        //ドロップ後色を戻す
        obj.style.backgroundColor = "";
        event.preventDefault();
        return;
    }

    //id名からドラッグされた要素を取得
    var dragElement = $('#' + dragIdArray[0]+'_' + dragIdArray[1] + '_' + dragIdArray[2])[0];
    if (!dragElement) {
        event.preventDefault();
        return;
    }
    var dropElement = $(obj)[0];
    if (!dropElement) {
        event.preventDefault();
        return;
    }

    var isCopy = $('#OPERATION_RADIO2').is(':checked');
    if (!isCopy) {
        var key_event = event || window.event;
        // Shiftキー押下時はコピー処理にする
        isCopy = (key_event.shiftKey);
    }

    if (dragElement != dropElement) {
        if ($('#OPERATION_RADIO1').is(':checked') || $('#OPERATION_RADIO2').is(':checked')) {
            var srcCellObj = new CellObj();
            var targetCellObj = new CellObj();
    
            srcCellObj.setCellObj(dragElement);
            targetCellObj.setCellObj(dropElement);
            // セルへの追加可否判定
            if (targetCellObj.checkAppendCell(srcCellObj, cntNum)) {
                targetCellObj.appendCell(srcCellObj, cntNum);
                // 移動の場合はSrcのレコードを削除
                if (!isCopy) {
                    srcCellObj.deleteCell(cntNum);
                }
            }
        } else if ($('#OPERATION_RADIO3').is(':checked')) {
            var swapCellObj = new SwapCellObj();
            swapCellObj.setSwapSrcCellObj(dragElement, cntNum);
            swapCellObj.setSwapTargetCellObj(dropElement);

            if (swapCellObj.checkSwap()) {
                swapCellObj.swap();
            }
        }
    }

    dodragleave(event);
    //エラー回避のため、ドロップ処理の最後にdropイベントをキャンセルしておく
    event.preventDefault();
    //ドロップ後色を戻す
    obj.style.backgroundColor = "";
}

/***** ゴミ箱ドロップ時の処理 *****/
function f_dropTrash(event, obj){
    obj = this;

    //ドラッグされたデータのid名をDataTransferオブジェクトから取得
    var id_name = event.dataTransfer.getData("text");
    var dragIdArray = id_name.split('_');
    var cntNum = '';
    //ポップアップの時は３番目がある
    if (dragIdArray.length > 3) {
        if (dragIdArray[3] != 'all') {
            cntNum = '_' + dragIdArray[3];
        }
    }

    //id名からドラッグされた要素を取得
    var dragElement = $('#' + dragIdArray[0]+'_' + dragIdArray[1] + '_' + dragIdArray[2])[0];
    if (!dragElement) {
        event.preventDefault();
        return;
    }

    var cellObj = new CellObj();
    cellObj.setCellObj(dragElement);
    if (cellObj.cell.dataValList.length > 0) {
        $('input[name=selectTD]').val(cellObj.cell.id + cntNum);
        showDialog('deleteSelectBox','削除',deleteSelectInitFunc);
    }

    //ドロップ後色を戻す
    event.target.style.backgroundColor = "#003366";
}


var CTarget;
var ActiveFlag;

//ポップアップを非表示
function dodragleave(event){
    var obj=$('#box')[0];
    obj.classList.add('non_active_box');
    obj.classList.remove('active_box');
}
function doclick(e){
    //console.log($('#'+e.currentTarget.id)[0].getAttribute('data-test'));

    var obj=$('#box')[0];
    //自分自身でポップアップが出ていれば非表示
    if(CTarget == e.currentTarget && obj.className == 'active_box'){
        obj.classList.add('non_active_box');
        obj.classList.remove('active_box');
    } else {
        //ポップアップが出てなければ表示
        var dataVal = $(e.target).attr('data-val');
        var values = [];
        if (dataVal.length > 1){
            values = JSON.parse(dataVal);
        }
        // セル内に2件以上ある場合はポップアップ表示
        if (values.length > 1) {
            var inHtml = '';
            for(var i=0; i < values.length; i++){
                var val = values[i];
                inHtml+='<div id="'+ e.currentTarget.id + '_' + i +'" class="inner_box" draggable="true">' + val['grade'] + '-' + val['hrclasscd'] + '<br>'+ val['hrclassname'] + '</div>';
            }
            inHtml+='<div id="'+e.currentTarget.id+'_all" class="inner_box" style="background-color:#CCFFCC" draggable="true">全件移動</div>';
            obj.innerHTML=inHtml;
            var elements = $('.inner_box');
            for(var i = 0; i < elements.length; i++ ) {
                elements[i].ondragstart = f_dragstart;
            }
            obj.style.top=e.pageY+10;
            obj.style.left=e.pageX+10;
            //activeにしないと座標がとれない
            obj.classList.add('active_box');
            obj.classList.remove('non_active_box');
            //座標の再計算、一番下の時にセルの上方向、右なら左方向といった感じ
            pos = setposition(e, obj, $('#tbody')[0]);
            obj.style.top=pos.y;
            obj.style.left=pos.x;
            ActiveFlag = true;
        }
    }
    CTarget = e.currentTarget;
}
function setposition(e, box1, box2){
    pos1=getElementPosition(box1);
    pos2=getElementPosition(box2);
    if(pos1.x+box1.clientWidth>pos2.x+box2.clientWidth){
        x=e.pageX-20-box1.clientWidth;
    } else {
        x=e.pageX+10;
    }
    if(pos1.y+box1.clientHeight>pos2.y+box2.clientHeight){
        y=e.pageY-20-box1.clientHeight;
    } else {
        y=e.pageY+10;
    }
    return {x:x,y:y};
}
function getElementPosition ( elm ) {
    var fn = arguments.callee, // 再帰用
        rslt = { x:0, y:0 },
        p;
    if( elm ) {
        rslt = {
            x : parseInt(elm.offsetLeft),
            y : parseInt(elm.offsetTop)
        };
        if(elm.offsetParent) {
            p = fn(elm.offsetParent);
            rslt.x += p.x;
            rslt.y += p.y;
        }
    }
    return rslt;
}


//権限チェック
function OnAuthError()
{
    alert('{rval MSG300}');
    closeWin();
}
//スクロール
function scrollRC(){
    $('#trow')[0].scrollLeft = $('#tbody')[0].scrollLeft;
    $('#tcol')[0].scrollTop = $('#tbody')[0].scrollTop;
}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//ダイアログ表示
//ダイアログ内のIDのプレフィックスにdef_は使わないこと。
function showDialog(contentsId, title, callback) {
    var srcDialogBox = $('#def_'+contentsId)[0];
    if (!srcDialogBox) {
        var srcDialogBoxHTML = $('#'+contentsId)[0].outerHTML;
        //ダイアログ内のid="××" → id="def_××"に変更。要はコピー作成
        srcDialogBoxNameHTML = srcDialogBoxHTML.replace(/(<[^>]*name=")([^"]+)("[^>]*>)/g , '$1def_$2$3');
        $('#'+contentsId)[0].outerHTML = srcDialogBoxNameHTML.replace(/(<[^>]*id=")([^"]+)("[^>]*>)/g , '$1def_$2$3');
    } else {
        var srcDialogBoxHTML = $('#def_'+contentsId)[0].outerHTML.replace(/(<[^>]*name=")def_([^"]+)("[^>]*>)/g , '$1$2$3');
        var srcDialogBoxHTML = srcDialogBoxHTML.replace(/(<[^>]*id=")def_([^"]+)("[^>]*>)/g , '$1$2$3');
    }

    //dialogBox.dialogBoxContentsの中身を書き換えている
    $('#dialogBoxContents')[0].innerHTML = srcDialogBoxHTML;
    $('#dialogBoxTitle')[0].innerHTML = title;
    $('#'+contentsId).show();
    $('#dialogBox').show();

    // ブラウザの横幅を取得(全体の幅からダイアログの幅を引いて半分。要は中央ぞろえ)
    var browserWidth = $(window).width();
    var boxW = $("#dialogBoxTable").width();
    var plusPxW = ((browserWidth - boxW) / 2);
    var browserHeight = $(window).height();
    var boxH = $("#dialogBoxTable").height();
    var plusPxH = ((browserHeight - boxH)/2);
    if (plusPxH < 0) {
        plusPxH = 0;
    }

    //dialogBox見えないDivを全画面に展開（表示したダイアログ以外触れないようにする）
    $('#dialogBox').css({'left':0});
    $('#dialogBox').css({'top': 0});
    $('#dialogBox').css({'width': $(document).width() + "px"});
    $('#dialogBox').css({'height': $(document).height() + "px"});

    //ダイアログ
    $('#dialogBoxTable').css({'position': 'absolute'});
    $('#dialogBoxTable').css({'left': plusPxW + "px"});
    $('#dialogBoxTable').css({'top': plusPxH + "px"});

    //タイトルの部分をドラックして移動できるようにしている。
    $('#dialogTitleBar').mousedown(function(e){
        e.preventDefault();
        //dialogBoxTableの中にdataを使って変数を作成(最初の座標保持)
        $('#dialogBoxTable')
            .data("clickPointX" , e.pageX - $('#dialogTitleBar').offset().left)
            .data("clickPointY" , e.pageY - $('#dialogTitleBar').offset().top);
        //dialogBoxTableのmousedown時の座標と現在の座標を使用してダイアログの位置を動かす
        //激しく動かしてダイアログからマウスが外れても動く動くようにdocument.mousemoveにしてる
        $(document).mousemove(function(e){
            e.preventDefault();
            $('#dialogBoxTable').css({
                top:e.pageY - $('#dialogBoxTable').data("clickPointY")+"px",
                left:e.pageX - $('#dialogBoxTable').data("clickPointX")+"px"
            })
        })
    }).mouseup(function(e){
        e.preventDefault();
        $(document).unbind("mousemove");
    });
    if (callback) {
        callback();
    }
}


//デバッグ用アクション
function testAction(){
    //showDialog('dumyBox','ダミー',function(){/*alert('A');*/});
    //showDialog('deleteSelectBox','削除',deleteSelectInitFunc);
}

//キーダウン処理。現在Deleteのみ対応
function documentKyeDown(event) {
    if (!$('#dialogBox').is(':hidden')) {
        return true;
    }

    if(event.keyCode == 46 && $('input[name=selectTD]').val() !=''){
        if($('#'+$('input[name=selectTD]').val().replace(/TD/,'KOMA'))[0].getAttribute('data-val')!=''){
            showDialog('deleteSelectBox','削除',deleteSelectInitFunc);
        }
    }
};

//削除ダイアログ初期化
function deleteSelectInitFunc(){
    dodragleave();
    var idArray = $('input[name=selectTD]').val().split("_");
    var cntNum = null;
    //複数件(2件とか)ある場合のクリック時のリストで選択した番号
    if (idArray.length > 3) {
        if (idArray[3] != 'all') {
            cntNum = idArray[3];
        }
    }

    var elements = $('#tyousei2 td');
    for (var i = 0; i < elements.length; i++) {
        if (i + 1 == idArray[2]) {
            var titleKouzi = elements[i].innerText;
            break;
        }
    }

    $('#deleteSelect_targetName')[0].innerText = weekWaAbbvArray[idArray[1]] + ' ' + titleKouzi;

    var srcBoxId='KOMA_'+idArray[1]+'_'+idArray[2];
    var srcBox = $('#'+srcBoxId)[0];

    var dataVal = srcBox.getAttribute('data-val');
    var values = [];
    if (dataVal.length > 1){
        if (cntNum) {
            var records = JSON.parse(dataVal);
            values.push(records[cntNum]);
        } else {
            values = JSON.parse(dataVal);
        }
    }

    for (var i = 0; i < values.length; i++) {
        var trData = '<tr>';
        trData += '<td align="center"><input type="checkbox" name="deleteSelectCheckBox" value="'+ values[i].grade + "-" + values[i].hrclasscd + '" checked="checked"></td>';
        trData += '<td>' + values[i].hrclassname + '</td>';
        trData += '<td>' + values[i].subclassname + '</td>';
        trData += '</tr>';
        $('#deleteSelectTable').append(trData);
    }

}

//削除ダイアログ削除処理
function deleteSelect_deleteButton() {

    var idArray = $('input[name=selectTD]').val().split("_");
    //ポップアップの時は３番目がある
    if (idArray.length > 3) {
        if (idArray[3] != 'all') {
            cntNum = idArray[3];
        }
    }

    // 選択されている曜日校時
    var selectElement = $('#' + idArray[0] + "_" + idArray[1] + "_" + idArray[2])[0];
    if (!selectElement) {
        return;
    }

    // 削除用レコード
    var records = [];
    var cellObj = new CellObj();
    cellObj.setCellObj(selectElement);
    var dataValList = cellObj.cell.dataValList;

    // 削除ダイアログでチェックの付いているHRクラスを全て取得
    var records = [];
    $('input[name=deleteSelectCheckBox]:checked').each(function() {
        var checkedVal = $(this).val();
        var hrclassArray = checkedVal.split('-');
        for (var i = 0; i < dataValList.length; i++) {
            var value = dataValList[i];
            if (value['grade'] == hrclassArray[0] && value['hrclasscd'] == hrclassArray[1]) {
                records.push(value);
                break;
            }
        }
    });
    // レコード削除
    cellObj.deleteRecord(records);
    // ダイアログ非表示
    $('#dialogBox').hide();
}

//曜日で移動コピーダイアログ初期化
function copyMoveBoxInitFunc(){

    var id = $('input[name=selectTD]').val();
    if (id) {
        id = "KOMA_0_1";
    }
    var idArray = id.split("_");
    var week = idArray[1];

    for (var i = 0; i < weekWaAbbvArray.length; i++) {
        var element = weekWaAbbvArray[i];
        var option = $('<option>').val(i).text(element).prop('selected', (i == week));
        $('#copyMoveBox_fromWeekSelect').append(option);

        var option = $('<option>').val(i).text(element);
        $('#copyMoveBox_toWeekSelect').append(option);
    }
    $('#copyMoveBox_fromWeekSelect').prop('size',weekWaAbbvArray.length);
    $('#copyMoveBox_toWeekSelect').prop('size',weekWaAbbvArray.length);

}

//曜日で移動コピー、曜日変更時処理
function copyMoveBox_change(){

    var errorMsg = "";
    var checkFlg = true;

    // 移動元曜日の選択チェック
    var fromSelected = $('#copyMoveBox_fromWeekSelect option:selected');
    if (!fromSelected || $(fromSelected).length <= 0) {
        errorMsg += "曜日が選択されていません。" + "<br>";
        checkFlg = false;
    }
    // 移動元曜日の飛び石選択チェック
    var firstSelected = $(fromSelected)[0];
    var lastSelected = $(fromSelected)[$(fromSelected).length - 1];
    var firstWeek = $(firstSelected).val();
    var lastWeek = $(lastSelected).val();
    if ((lastWeek - firstWeek) != ($(fromSelected).length - 1)) {
        errorMsg += "曜日が飛び石選択されています。" + "<br>";
        checkFlg = false;
    }

    // 移動先曜日の選択チェック
    var toSelected = $('#copyMoveBox_toWeekSelect option:selected');
    if (!toSelected || $(toSelected).length <= 0) {
        errorMsg += "移動先の曜日が選択されていません。" + "<br>";
        checkFlg = false;
    }
    // 移動先曜日の範囲チェック
    if (firstWeek == $(toSelected[0]).val()) {
        errorMsg += "当日です。" + "<br>";
        checkFlg = false;
    }
    if ((parseInt($(toSelected[0]).val(), 10) + $(fromSelected).length) > weekWaAbbvArray.length) {
        errorMsg += "範囲エラーです。" + "<br>";
        checkFlg = false;
    }

    var weekList = [];
    for (var i = firstWeek; i <= lastWeek; i++) {
        weekList.push(i);
    }

    var blockCell = new BlockCellObj();
    blockCell.setSrcWeeks(weekList);
    blockCell.setTargetWeek($(toSelected[0]).val());
    blockCell.maxPeriod = $('input[name=PERIODCNT_MAX]').val();
    // 移動チェック
    var moveDisabled = true;
    if (checkFlg) {
        if (blockCell.checkMove()) {
            moveDisabled = false;
        } else {
            if (errorMsg.indexOf(blockCell.errorMessage) < 0) {
                errorMsg += blockCell.errorMessage + "<br>";
            }
        }
    }
    // コピーチェック
    var copyDisabled = true;
    if (checkFlg) {
        if (blockCell.checkCopy()) {
            copyDisabled = false;
        } else {
            if (errorMsg.indexOf(blockCell.errorMessage) < 0) {
                errorMsg += blockCell.errorMessage + "<br>";
            }
        }
    }
    // 入れ替えチェック
    var swapDisabled = true;
    if (checkFlg) {
        if (blockCell.checkSwap()) {
            swapDisabled = false;
        } else {
            if (errorMsg.indexOf(blockCell.errorMessage) < 0) {
                errorMsg += blockCell.errorMessage + "<br>";
            }
        }
    }
    // 削除チェック
    var deleteDisabled = true;
    if (blockCell.checkDelete()) {
        deleteDisabled = false;
    } else {
        errorMsg += blockCell.errorMessage + "<br>";
    }

    // ボタン使用可・不可切り替え
    $("#copyMoveBox_moveButton").prop("disabled", moveDisabled);
    $("#copyMoveBox_copyButton").prop("disabled", copyDisabled);
    $("#copyMoveBox_irekaeButton").prop("disabled", swapDisabled);
    $("#copyMoveBox_deleteButton").prop("disabled", deleteDisabled);

    // エラーメッセージ初期化
    $("#copyMoveBox_errorMsg").text("");
    if (errorMsg) {
        // エラーメッセージ表示
        // $("#copyMoveBox_errorMsg").text(errorMsg);
        $("#copyMoveBox_errorMsg").html(errorMsg);
    }

}

// 「曜日でコピー／移動」実行
function copyMoveBox_submit(cmd) {
    // 移動元曜日の選択チェック
    var fromSelected = $('#copyMoveBox_fromWeekSelect option:selected');
    // 移動元曜日の飛び石選択チェック
    var firstSelected = $(fromSelected)[0];
    var lastSelected = $(fromSelected)[$(fromSelected).length - 1];
    var firstWeek = $(firstSelected).val();
    var lastWeek = $(lastSelected).val();

    // 移動先曜日の選択チェック
    var toSelected = $('#copyMoveBox_toWeekSelect option:selected');

    var weekList = [];
    for (var i = firstWeek; i <= lastWeek; i++) {
        weekList.push(i);
    }
    // 
    var blockCell = new BlockCellObj();
    blockCell.setSrcWeeks(weekList);
    blockCell.setTargetWeek($(toSelected[0]).val());
    blockCell.maxPeriod = $('input[name=PERIODCNT_MAX]').val();

    if (cmd == 'move') {
        blockCell.toMove();
    } else if (cmd == 'copy') {
        blockCell.toCopy();
    } else if (cmd == 'swap') {
        blockCell.toSwap();
    } else if (cmd == 'delete') {
        blockCell.toDelete();
    }
    // 曜日の校時データが変更されたので、再度チェック
    copyMoveBox_change();
}
