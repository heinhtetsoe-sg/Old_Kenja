
function btn_submit(cmd) {

    //サブミット中、更新ボタン使用不可
    if ($('input[name=btn_update]').prop('disabled')) {
        $('input[name=btn_update]').prop('disabled', true);
    }

    if (cmd == "update") {
        if ($('select[name=PRESEQ]').val()) {
            showDialog('ptrnUpdateBox', '講座展開表更新', ptrnUpdateInitFunc);
        } else {
            $('#lockScreen').css({'width': $(window).width() + "px"});
            $('#lockScreen').css({'height': $(window).height() + "px"});
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
        } else if (cmd == "preChairSeqDelete") {
            if(!confirm('{rval MSG103}' + '\n注意：このテンプレートの関連データも全て削除されます！')){
                return false;
            }
        }
        $('#lockScreen').css({'width': $(window).width() + "px"});
        $('#lockScreen').css({'height': $(window).height() + "px"});
        $('#lockScreen').css('display','table');

        document.forms[0].cmd.value = cmd;
        document.forms[0].submit();
        return false;
    }
}

function btn_submitPtn(cmd){
    
    if($('#ptrnUpdate_TITLE').val() == ''){
        alert('タイトルを入力してください。');
        return;
    }

    $('#lockScreen').css({'width': $(window).width() + "px"});
    $('#lockScreen').css({'height': $(window).height() + "px"});
    $('#lockScreen').css('display','table');

    getContent('updatePtrn');
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function btn_close(){
    getContent('');
    if($('input[name=updateDataList]').val()) {
        if(!confirm('{rval MSG108}')){
            return false;
        }
    }
    closeWin();
}


function btn_submitPreRead(cmd){

    var targetBoxs = $('.targetbox[data-val!=""]');
    if (targetBoxs.length > 0) {
        if(!confirm('{rval MSG104}' + '\n注意：このテンプレートの関連データも全て削除されます！')){
            return false;
        }
    }
    $("input[name=readPreHdatFlg]").val("1");

    btn_submit('edit');
}

// 重複名簿等の移動画面を開く
function btn_overlapMeibo(URL, param){
    getContent('');
    if(document.forms[0].updateDataList.value != ''){
        if(!confirm('{rval MSG108}')){
            return false;
        }
    }
    var selectCell = $("input[name=selectTD]").val();
    if (selectCell) {
        var chairCdList = [];
        var cellObj = new CellObj();
        var chairList = cellObj.getChairInfoList($("#"+selectCell));
        for (let i = 0; i < chairList.length; i++) {
            const chair = chairList[i];
            chairCdList.push(chair['chairCd']);
        }
        param += "&SELECT_CHAIRCD=" + chairCdList.join(',');
        param += "&SELECT_PREORDER=" + $("#"+selectCell).attr("order");
    }
    wopen(URL+param, 'SUBWIN2',0,0,screen.availWidth,screen.availHeight);
}

/**
 * OPTION項目 移動処理
 * ※「クラス」の移動で使用
 *
 * @param srcSelect     移動元
 * @param targetSelect  移動先
 * @param option        全移動の場合'ALL'を指定
 * @returns
 */
function layoutMove(src, target, option) {
    if (!option) { option = ''; }

    // option に ALL が指定されている場合は全て移動する
    if (option == 'ALL') {
        $('select[name='+ target +']').append($('select[name='+ src +'] option'));
    }

    // 選択されているOPTION値を移動
    var selectedList = $('select[name='+ src +'] option:selected');
    for (let index = 0; index < selectedList.length; index++) {
        const element = selectedList[index];
        $('select[name='+ target +']').append(element);
    }

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

}


var initTimer = null;
/**
 * 画面初期化処理
 */
function dispInit() {

    $('input[name=btn_update]').prop('disabled', false);
    $('input[name=btn_reset]').prop('disabled', false);
    $('input[name=btn_preChairSeqDelete]').prop('disabled', false);
    $('input[name=btn_setChair]').prop('disabled', false);

    // 教育課程(HRクラス)以外の場合は参照のみ
    //   [更新][削除] [反映]使用不可
    if ($('#SCH_DIV2').is(':checked') && $('select[name=LEFT_MENU]').val() != "2") {
        $('input[name=btn_update]').prop('disabled', true);
        $('input[name=btn_reset]').prop('disabled', true);
        $('input[name=btn_setChair]').prop('disabled', true);
    }

    // 画面初期処理
    // ※画面読込後に処理の重たい処理を行う
    initTimer = setTimeout(function() {
        initTimerFunc();
    }, 50);
}

/**
 * 画面初期処理
 * ※画面表示後に処理が行われる
 */
function initTimerFunc() {

    if (initTimer) {
        clearTimeout(initTimer);
    }

    // 右側の科目一覧をリフレッシュ
    selectCourse();

    return;
}



/**
 * [読込]ボタン押下処理
 */
function yomikomiTimeCheck(){

    $('#lockScreen').css({'width': $(window).width() + "px"});
    $('#lockScreen').css({'height': $(window).height() + "px"});
    $('#lockScreen').css('display','table');

    btn_submit('edit');
}

/**
 * [科目展開表読込]ボタン押下処理
 */
function yomikomiPreHdatCheck(){

    // showDialog('ptrnPreReadBox', '講座展開表読込', ptrnPreReadInitFunc);
    showDialog('ptrnPreReadBox', '講座展開表読込');

}

// 更新ボタンの処理
// 設定したデータの取得
function getContent(cmd) {

    var updateDataList = [];

    // 更新
    if (cmd == 'updatePtrn') {
        // 新規登録の場合は科目情報の登録されているデータのフラグを立てる
        if ($('input[name=PTRN_UPDATE_RADIO]:checked').val() == '2') {
            var cellList = $('.targetbox');
            for (let i = 0; i < cellList.length; i++) {
                const cell = cellList[i];
                $(cell).attr('data-update', '1');
            }
        }
    }

    // 更新するリスト作成
    var cellList = $('.targetbox[data-update=1]');
    for (let i = 0; i < cellList.length; i++) {
        const cell = cellList[i];

        var updateData = {};
        // コースID
        updateData['COURSE'] = $(cell).attr('line-key');
        updateData['HR_CLASS'] = $(cell).attr('line-key2');
        updateData['ORDER'] = $(cell).attr('order');
        // 科目一覧
        updateData['CHAIR'] = [];
        var cellObj = new CellObj();
        var chairList = cellObj.getChairInfoList(cell);
        for (let j = 0; j < chairList.length; j++) {
            const chairInfo = chairList[j];
            updateData['CHAIR'].push(chairInfo['chairCd']);
        }
        updateDataList.push(updateData);
    }
    $('input[name=updateDataList]').val(JSON.stringify(updateDataList));

    return;
}

/**
 * 講座情報の追加(反映ボタン押下時処理)
 */
function setChairInfo() {

    if ($('select[name=LEFT_MENU]').val() != "2") {
        alert('「教育課程(年組)」以外は参照のみ可能です。');
        return false;
    }

    // if (!$('select[name=COURSECD] option:selected').val()) {
    //     alert('コースを選択してください。');
    //     return false;
    // }

    var ajaxParam = {};
    var chairList = [];
    var chairInfoList = [];
    var selectChairInfo = $('select[name=CATEGORY_SELECTED] option:selected');
    for (let i = 0; i < selectChairInfo.length; i++) {
        const element = selectChairInfo[i];

        var chairInfo = JSON.parse($(element).attr('data-val'));
        chairList.push(chairInfo['chairCd']);
        chairInfoList.push(chairInfo);
    }
    ajaxParam['CHAIRCD'] = chairList;
    ajaxParam['MENUDIV'] = $('select[name=LEFT_MENU]').val();
    ajaxParam['COURSECD'] = $('select[name=COURSECD] option:selected').val();

    // 選択されている講座がなければ処理しない
    if (chairList.length <= 0) {
        return;
    }

    // 選択列取得
    var selCell = $('#' + $('input[name=selectTD]').val())[0];
    if (!selCell) {
        return;
    }
    var order = $(selCell).attr('order');

    // 画面のロック
    $('#lockScreenPopUp').css({'width': $(document).width() + "px"});
    $('#lockScreenPopUp').css({'height': $(document).height() + "px"});
    $('#lockScreenPopUp').show();

    $.ajax({
        url:'knjb3043index.php',
        type:'POST',
        data:{
              AJAX_PARAM : JSON.stringify(ajaxParam)
            , cmd : 'getCourseChair'
            , YEAR_SEME : $('select[name=YEAR_SEME] option:selected').val()
        },
        async:true
    }).done(function(data, textStatus, jqXHR) {
        var paramList = $.parseJSON(data);

        for (let i = 0; i < chairInfoList.length; i++) {
            const chairInfo = chairInfoList[i];

            courseList = paramList[chairInfo['chairCd']];
            if (!courseList) {
                continue;
            }
            for (let j = 0; j < courseList.length; j++) {
                const courseInfo = courseList[j];

                var key1 = "";
                var key2 = "";

                key1 += courseInfo['COURSECD'];
                key1 += '-' + courseInfo['MAJORCD'];
                key1 += '-' + courseInfo['GRADE'];
                key1 += '-' + courseInfo['COURSECODE'];

                key2 += courseInfo['TRGTGRADE'];
                key2 += '-' + courseInfo['TRGTCLASS'];

                var targetBoxs = $('.targetbox[line-key=' + key1 + '][line-key2=' + key2 + '][order=' + order + ']')[0];
                if (targetBoxs) {
                    var targetCellObj = new CellObj();
                    targetCellObj.setCell(targetBoxs);
                    // 講座の追加判定
                    if (targetCellObj.checkAddChair(chairInfo)) {
                        targetCellObj.addChair(chairInfo);
                        targetCellObj.writeCell();
                    }
                }


            }
        }

        // 重複人数チェック
        getStdDupCnt(order);
        // 未配置人数チェック
        getStdUnPlacedCnt(order);

        // // 選択中セルを解除
        // f_clearSelectTDColor();

        $('#lockScreenPopUp').hide();

    });



    return;
}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////
// AJAX

/**
 * コースの選択を変更
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

    // 展開表で選択したコースを選択する
    var targetLineKeyArray = $(target).attr('line-key').split('-');
    var targetLineKey = targetLineKeyArray[0] + '-' + targetLineKeyArray[1] + '-' + targetLineKeyArray[2] + '-' + targetLineKeyArray[3];
    $('select[name=COURSECD]').val(targetLineKey);

    if ($('select[name=LEFT_MENU]').val() == '2') {
        var hrClass = $(target).attr('line-key2');
        $('select[name=HRCLASSCD]').val(hrClass);
    }
    // 設定講座一覧の講座情報をリフレッシュ
    getChairInfo();

    return;
}

// 右側「コース」選択変更(「科目」の再取得)
function selectCourse() {

    // コースID パラメタ設定
    var courseCd = $('select[name=COURSECD] option:selected').val();
    if (!courseCd) {
        $('select[name=CATEGORY_SELECTED] option').remove();
        return;
    }

    // 設定講座一覧の講座情報をリフレッシュ
    getChairInfo();

    var ajaxParam = {};
    // AJAXパラメタ設定
    ajaxParam['COURSECD'] = courseCd;

    $.ajax({
        url:'knjb3043index.php',
        type:'POST',
        data:{
              AJAX_PARAM : JSON.stringify(ajaxParam)
            , cmd : 'getSubclass'
            , YEAR_SEME : $('select[name=YEAR_SEME] option:selected').val()
        },
        async:true
    }).done(function(data, textStatus, jqXHR) {
        var paramList = $.parseJSON(data);

        // 科目情報リストの初期化
        $('select[name=SUBCLASSCD] option').remove();
        // 空のオプションを追加
        var option = $('<option>').val('').text('');
        $('select[name=SUBCLASSCD]').append(option);

        for (var i = 0; i < paramList.length; i++) {
            // 展開表のコースで選択されている科目は除外する
            var isAny = false;
            const paramElement = paramList[i];

            var dataVal = {};
            dataVal['classcd'] = paramElement['CLASSCD'];
            dataVal['school_kind'] = paramElement['SCHOOL_KIND'];
            dataVal['curriculum_cd'] = paramElement['CURRICULUM_CD'];
            dataVal['subclasscd'] = paramElement['SUBCLASSCD'];
            // dataVal['credits'] = paramElement['CREDITS'];
            dataVal['subclassname'] = paramElement['SUBCLASSABBV'];

            var option = $('<option>').val(paramElement['VALUE']).text(paramElement['VALUE']+ ' ' +paramElement['LABEL']);
            $(option).attr('data-val', JSON.stringify(dataVal));
            $('select[name=SUBCLASSCD]').append(option);
        }

    });
}


/**
 * 講座情報取得
 */
function getChairInfo() {

    var ajaxParam = {};
    // AJAXパラメタ設定
    if ($('select[name=HRCLASSCD] option:selected').val()) {
        ajaxParam['HRCLASSCD'] = $('select[name=HRCLASSCD] option:selected').val();
    }
    if ($('select[name=SUBCLASSCD] option:selected').val()) {
        ajaxParam['SUBCLASSCD'] = $('select[name=SUBCLASSCD] option:selected').val();
    }
    if ($('select[name=GUNCD] option:selected').val()) {
        ajaxParam['GUNCD'] = $('select[name=GUNCD] option:selected').val();
    }
    if ($('select[name=STAFFCD] option:selected').val()) {
        ajaxParam['STAFFCD'] = $('select[name=STAFFCD] option:selected').val();
    }
    if ($('select[name=COURSECD] option:selected').val()) {
        ajaxParam['COURSECD'] = $('select[name=COURSECD] option:selected').val();
    }

    $.ajax({
        url:'knjb3043index.php',
        type:'POST',
        data:{
              AJAX_PARAM : JSON.stringify(ajaxParam)
            , cmd : 'getChair'
            , YEAR_SEME : $('select[name=YEAR_SEME] option:selected').val()
        },
        async:true
    }).done(function(data, textStatus, jqXHR) {
        var paramList = $.parseJSON(data);

        // 講座情報リストの初期化
        $('select[name=CATEGORY_SELECTED] option').remove();

        for (var i = 0; i < paramList.length; i++) {
            // 展開表のコースで選択されている講座は除外する
            var isAny = false;
            const paramElement = paramList[i];

            var dataVal = {};

            dataVal['classCd'] = paramElement['CLASSCD'];
            dataVal['schoolKind'] = paramElement['SCHOOL_KIND'];
            dataVal['curriculumCd'] = paramElement['CURRICULUM_CD'];
            dataVal['subclassCd'] = paramElement['SUBCLASSCD'];

            dataVal['chairCd'] = paramElement['CHAIRCD'];
            dataVal['chairName'] = paramElement['CHAIRABBV'];

            var value = paramElement['CHAIRCD'];
            var label = paramElement['CHAIRCD'] + ' ' + paramElement['CHAIRNAME'];

            var option = $('<option>').val(value).text(label);
            $(option).attr('data-val', JSON.stringify(dataVal));
            $('select[name=CATEGORY_SELECTED]').append(option);
        }

    });
}

/**
 * 重複人数チェック処理
 */
function getStdDupCnt(order) {

    var ajaxParam = {};
    ajaxParam['CHAIRCD'] = [];
    // AJAXパラメタ設定
    var targetBoxs = $('.targetbox[data-val!=""][order=' + order + ']');
    if (targetBoxs.length <= 0) {
        $('.footer_stddup[order=' + order + ']').text('');
        return;
    }

    for (let index = 0; index < targetBoxs.length; index++) {
        const element = targetBoxs[index];
        var dataVal = $(element).attr('data-val');
        var chairList = JSON.parse(dataVal);

        for (let i = 0; i < chairList.length; i++) {
            var chairInfo = chairList[i];
            ajaxParam['CHAIRCD'].push(chairInfo['chairCd']);
        }
    }

    // Ajax通信前に 情報取得中の値に変更
    $('.footer_stddup[order=' + order + ']').text('--');

    $.ajax({
        url:'knjb3043index.php',
        type:'POST',
        data:{
              AJAX_PARAM : JSON.stringify(ajaxParam)
            , cmd : 'getStdDupCnt'
            , YEAR_SEME : $('select[name=YEAR_SEME] option:selected').val()
        },
        async:false
    }).done(function(data, textStatus, jqXHR) {
        var paramList = $.parseJSON(data);
        // 重複人数の変更
        $('.footer_stddup[order=' + order + ']').text(paramList['STDCNT']);

        var dupChair = paramList['DUPCHAIR'];
        // 列の講座分繰り返し
        var targetBoxs = $('.targetbox[data-val!=""][order=' + order + ']');
        for (let index = 0; index < targetBoxs.length; index++) {
            const element = targetBoxs[index];
            var cell = new CellObj();
            cell.setCell(element);
            cell.setDupChair('');
            // 重複講座が無い場合は「重」を削除する
            if (!dupChair) {
                continue;
            }

            var dataVal = $(element).attr('data-val');
            var chairList = JSON.parse(dataVal);
            for (let i = 0; i < chairList.length; i++) {
                var chairInfo = chairList[i];
                // 重複講座の場合、「重」の文字を追加
                if (dupChair[chairInfo['chairCd']]) {
                    cell.setDupChair(true);
                }
            }
        }

    });
}

/**
 * 未配置人数チェック処理
 */
function getStdUnPlacedCnt(order) {

    // 未配置人数チェック
    var vLineObj = VerticalCellObj(order);
    vLineObj.getStdUnPlaced();



    // var targetBoxs = $('.targetbox[data-val!=""][order=' + order + ']');
    // if (targetBoxs.length <= 0) {
    //     $('.footer_unplaced[order=' + order + ']').text('');
    //     return;
    // }

    // // Ajax通信前に 情報取得中の値に変更
    // $('.footer_unplaced[order=' + order + ']').text('--');
    // var ajaxParam = {};
    // $.ajax({
    //     url:'knjb3043index.php',
    //     type:'POST',
    //     data:{
    //           AJAX_PARAM : JSON.stringify(ajaxParam)
    //         , cmd : 'getStdUnPlacedCnt'
    //         , YEAR_SEME : $('select[name=YEAR_SEME] option:selected').val()
    //     },
    //     async:false
    // }).done(function(data, textStatus, jqXHR) {
    //     var paramList = $.parseJSON(data);

    //     // 未配置人数の変更
    //     var stdCnt = 0;
    //     var targetBoxs = $('.targetbox[data-val!=""][order=' + order + ']');
    //     for (let index = 0; index < targetBoxs.length; index++) {
    //         const element = targetBoxs[index];
    //         var courseCd =$(element).attr('line-key');
    //         if (!paramList[courseCd]) {
    //             continue;
    //         }

    //         var dataVal = $(element).attr('data-val');
    //         var chairList = JSON.parse(dataVal);
    //         for (let i = 0; i < chairList.length; i++) {
    //             var chairInfo = chairList[i];
    //             var subclassCd = chairInfo["classCd"] + "-" + chairInfo["schoolKind"] + "-" + chairInfo["curriculumCd"] + "-" + chairInfo["subclassCd"];

    //             if (paramList[courseCd][subclassCd]) {
    //                 stdCnt += parseInt(paramList[courseCd][subclassCd]);
    //             }
    //         }
    //     }
    //     $('.footer_unplaced[order=' + order + ']').text(stdCnt);
    // });

}

/**
 * 施設講座キャパ超チェック処理
 */
function getFacCapOverCnt(order) {

    var ajaxParam = {};
    ajaxParam['CHAIRCD'] = [];

    // AJAXパラメタ設定
    var targetBoxs = $('.targetbox[data-val!=""][order=' + order + ']');
    for (let index = 0; index < targetBoxs.length; index++) {
        const element = targetBoxs[index];
        var dataVal = $(element).attr('data-val');
        var chairList = JSON.parse(dataVal);

        for (let i = 0; i < chairList.length; i++) {
            var chairInfo = chairList[i];
            ajaxParam['CHAIRCD'].push(chairInfo['chairCd']);
        }
    }

    $.ajax({
        url:'knjb3043index.php',
        type:'POST',
        data:{
              AJAX_PARAM : JSON.stringify(ajaxParam)
            , cmd : 'getFacCapOverCnt'
            , YEAR_SEME : $('select[name=YEAR_SEME] option:selected').val()
        },
        async:false
    }).done(function(data, textStatus, jqXHR) {
        var paramList = $.parseJSON(data);
        var facCapOverChair = paramList['FACCAPOVER'];
        // 列の講座分繰り返し
        var targetBoxs = $('.targetbox[data-val!=""][order=' + order + ']');
        for (let index = 0; index < targetBoxs.length; index++) {
            const element = targetBoxs[index];
            var cell = new CellObj();
            cell.setCell(element);
            cell.setFacCapOver('');
            // 施設講座キャパ超が無い場合は「施」を削除する
            if (!facCapOverChair) {
                continue;
            }

            var dataVal = $(element).attr('data-val');
            var chairList = JSON.parse(dataVal);
            for (let i = 0; i < chairList.length; i++) {
                var chairInfo = chairList[i];
                // 施設講座キャパ超の場合、「施」の文字を追加
                if (facCapOverChair[chairInfo['chairCd']]) {
                    cell.setFacCapOver(true);
                }
            }
        }

    });


}

// 講座展開表 更新画面初期化
function ptrnUpdateInitFunc() {
    var preSeqText = $('#PRECHAIRSEQ option:selected').text();
    if($('#PRECHAIRSEQ').val() != '0'){
        var preSeqTextParts = preSeqText.split(' ');
        $('#ptrnUpdate_TITLE').val(preSeqTextParts[3]);
        $('#PTRN_UPDATE_RADIO1').prop('disabled', false);
        $('#PTRN_UPDATE_RADIO1').prop('checked', true);
        var idx = preSeqTextParts[0];
    } else {
        $('#PTRN_UPDATE_RADIO1').prop('disabled', true);
        $('#PTRN_UPDATE_RADIO2').prop('checked', true);
        var idx = '0';
    }
    $('label[for=PTRN_UPDATE_RADIO1]')[0].innerHTML = '現在のSEQ('+idx+')で上書き保存';
}

// 削除ダイアログ初期化
function deleteSelectInitFunc(){

    dodragleave();

    var targetId = $('input[name=selectTD]').val();

    var idArray1 = targetId.split('_');
    // 複数講座の場合の対応(全移動/対象講座のみ移動)
    targetId = idArray1[0] + '_' + idArray1[1] + '_' + idArray1[2];

    var targetCell = $('.targetbox[id=' + targetId + ']');
    var course = $('.redips-mark[line-key=' + $(targetCell).attr('line-key') + '][line-key2=' + $(targetCell).attr('line-key2') + ']');

    // タイトル設定(コース名)
    $('#deleteSelect_targetName').text($(course).text());

    var trData = '';
    var targetChairList = new CellObj().getChairInfoList(targetCell);
    if (idArray1.length > 3) {
        var chair = targetChairList[idArray1[3]];
        targetChairList = [];
        targetChairList.push(chair);
    }
    for (let i = 0; i < targetChairList.length; i++) {
        const chair = targetChairList[i];
        trData += "<tr>";
        trData += "<td align='center'><input type='checkbox' name='deleteSelectCheckBox' value='"+ JSON.stringify(chair) + "' checked='checked'></td>";
        trData += "<td>" + chair['chairCd'] + " " + chair['chairName'] + "</td>";
        trData += "</tr>";
    }
    $('#deleteSelectTable').append(trData);

    dodragleave(event);
    //ドロップ後色を戻す
    $('#TRASH_BOX').css({'background-color': '#003366'});
    //エラー回避のため、ドロップ処理の最後にdropイベントをキャンセルしておく
    event.preventDefault();
}

//削除ダイアログ削除処理
function deleteSelect_deleteButton() {

    var targetId = $('input[name=selectTD]').val();
    var idArray1 = targetId.split('_');
    // 複数講座の場合の対応(全移動/対象講座のみ移動)
    targetId = idArray1[0] + '_' + idArray1[1] + '_' + idArray1[2];

    var targetCell;
    if (targetId) {
        targetCell =  $('#' + targetId);
    }
    if (!targetCell) {
        return;
    }

    var chairInfoList = [];
    // 削除用レコード
    var checkedList = $('input[name=deleteSelectCheckBox]:checked');
    for (let i = 0; i < checkedList.length; i++) {
        const chair = checkedList[i];
        chairInfoList.push(JSON.parse($(chair).val()));
    }

    // ドロップ元セルへの書込み
    var targetCellObj = new CellObj();
    targetCellObj.setCell($(targetCell));
    targetCellObj.removeChairList(chairInfoList);
    targetCellObj.writeCell();

    // 重複人数チェック
    getStdDupCnt($(targetCell).attr('order'));
    // 未配置人数チェック
    getStdUnPlacedCnt($(targetCell).attr('order'));

    // ダイアログ非表示
    $('#dialogBox').hide();
}


//レイアウト変更初期化
function layoutCourseBoxInitFunc() {

    dispLoad('Course');

}

// レイアウト変更（縦）データ取得
function dispLoad(layoutName) {

    //検索クリア
    if ($('input[name=CLASS_COURSE_SEARCH]')[0]) {
        $('input[name=CLASS_COURSE_SEARCH]').val('');
    }
    //リスト初期化
    $('#category_course_selected option').remove();
    $('#category_course_name option').remove();

    var ajaxParam = {};
    // パラメタ設定
    ajaxParam['LEFT_MENU'] = $('select[name=LEFT_MENU] option:selected').val();

    $.ajax({
        url:'knjb3043index.php',
        type:'POST',
        data:{
              AJAX_PARAM:JSON.stringify(ajaxParam)
            , cmd:'getLayoutCourseChair'
            , YEAR_SEME : $('select[name=YEAR_SEME] option:selected').val()
        },
        async:true
    }).done(function(data, textStatus, jqXHR) {
        var paramList = $.parseJSON(data);

        var visibleLines = $('input[name=visibleLine]').val().split(',');

        for (var i = 0; i < paramList.length; i++) {
            var param = paramList[i];

            var value = param["VALUE"];
            var label = param["LABEL"];
            var option = $('<option>').val(value).text(label);

            if ($('select[name=LEFT_MENU]').val() == '1') {
                $(option).attr('schoolKind', param["SCHOOL_KIND"]);

                $(option).attr('line-Key', value);
                $(option).attr('line-Key2', '');
            } else if ($('select[name=LEFT_MENU]').val() == '2') {
                $(option).attr('schoolKind', param["SCHOOL_KIND"]);
                $(option).attr('hrClass', param["GRADE"] + '-' + param["HR_CLASS"]);
                $(option).attr('line-Key', value);
                $(option).attr('line-Key2', param["GRADE"] + '-' + param["HR_CLASS"]);
            }

            var isAny = true;
            if (visibleLines) {
                isAny = false;
                for (let index = 0; index < visibleLines.length; index++) {
                    const element = visibleLines[index];
                    if (element == i) {
                        isAny = true;
                        break;
                    }
                }
            }
            // 検索条件を満たしている場合は左側へ追加
            // 検索条件を満たしていない場合は右側へ追加
            if (isAny) {
                $('select[name=category_course_selected]').append(option);
            } else {
                $('select[name=category_course_name]').append(option);
            }
        }
    });
}

// 反映押下で職員の表示/非表示切替
function dispChange(upperName) {

    var newSelectedList = $('#category_course_selected option');
    // 縦軸(行タイトル)
    var dispList = $('#REC tr');

    var parentIdxArray = {};
    for (let i = 0; i < dispList.length; i++) {
        const element = dispList[i];
        var lineKey = $(element.cells[0]).attr('line-key');
        var lineKey2 = $(element.cells[0]).attr('line-key2');

        $(element).hide();
        for (let j = 0; j < newSelectedList.length; j++) {
            const selectElement = newSelectedList[j];
            var key1 = $(selectElement).attr('line-Key');
            var key2 = $(selectElement).attr('line-Key2');

            if (lineKey == key1 && lineKey2 == key2) {
                $(element).show();
                parentIdxArray[i] = true;
                break;
            }
        }
    }


    // 縦軸(科目情報)
    var dispList = $('#REC2 tr');
    $('input[name=visibleLine]').val('');

    for (let i = 0; i < dispList.length; i++) {
        const element = dispList[i];

        $(element).hide();
        if (parentIdxArray[i]) {
            $(element).show();
            var lines = $('input[name=visibleLine]').val();
            if (lines) {
                lines += ',';
            }
            $('input[name=visibleLine]').val(lines + i);
        }
    }
    // レイアウト編集(縦)ダイアログを非表示
    $('#dialogBox').hide();
}


// レイアウト編集(縦) 校種変更
function layoutSchoolKind() {

    layoutMove('category_course_selected', 'category_course_name', 'ALL');

    var schoolKind = $('select[name=LAYOUT_SCHOOL_KIND] option:selected').val();
    var hrClass = $('select[name=LAYOUT_HR_CLASS] option:selected').val();
    var subclass = $('select[name=LAYOUT_SUBCLASS] option:selected').val();

    var optionList = $('select[name=category_course_name] option');
    for (let index = 0; index < optionList.length; index++) {
        const element = optionList[index];


        var isSchoolKind = false;
        var isHrClass = false;
        var isSubclass = false;

        if (!schoolKind || schoolKind == $(element).attr('schoolkind')) {
            isSchoolKind = true;
        }
        if (!hrClass || hrClass == $(element).attr('hrClass')) {
            isHrClass = true;
        }
        if (!subclass || subclass == $(element).attr('subclass')) {
            isSubclass = true;
        }
        if (isSchoolKind && isHrClass && isSubclass) {
            $('select[name=category_course_selected]').append(element);
        }

    }

    return;
}

// コース名の検索
function searchCourseName(obj) {
    var rightList = $('#category_classstaff_name option');
    for (let index = 0; index < rightList.length; index++) {
        const element = rightList[index];
        if ($(element).text().indexOf(obj.value) === -1) {
            $(element).hide();
        }
    }
}

///////////////////////////////////////////////////////////
//列で移動/コピー/入替/削除
function copyMoveBoxInitFunc() {

    ///////////////////////////////////
    // 操作対象を指定 初期表示
    // タイトル設定
    var leftTitle = $('select[name=LEFT_MENU] option:selected').text();
    $('label[name=leftTitle]').text(leftTitle);
    // 行タイトルを取得
    var lineTitles = $('.redips-mark');
    for (let i = 0; i < lineTitles.length; i++) {
        const lineTitle = lineTitles[i];
        var option = $('<option>').val($(lineTitle).val()).text($(lineTitle).text());
        $(option).attr('line-key', $(lineTitle).attr('line-key')).attr('line-key2', $(lineTitle).attr('line-key2'));
        $('#category_copymovebox_selected').append(option);
    }

    ///////////////////////////////////
    // 列の初期表示
    var selectFrom = $('<select>').attr('id', 'copyMoveBox_selectFromRenban').attr('size', '10').attr('multiple', '');
    $(selectFrom)[0].addEventListener('change', copyMoveBox_change);
    var selectTo = $('<select>').attr('id', 'copyMoveBox_selectToRenban').attr('size', '10');
    $(selectTo)[0].addEventListener('change', copyMoveBox_change);
    // 列のタイトル(連番)を取得
    var titles = $('.header_renban');
    for (let i = 0; i < titles.length; i++) {
        const title = titles[i];
        var option = $('<option>').val($(title).attr('id')).text($(title).text());
        $(selectFrom).append($(option).clone());
        $(selectTo).append($(option).clone());
    }
    $('#copyMoveBox_fromRenban').append(selectFrom);
    $('#copyMoveBox_toRenban').append(selectTo);

    // 各ボタンの使用可/使用不可を設定
    copyMoveBox_change();

}

//列で移動コピー、列変更時処理
function copyMoveBox_change() {

    // 移動・コピー等の各ボタンの使用可不可を設定
    $('#copyMoveBox_moveButton').prop("disabled", true);
    $('#copyMoveBox_copyButton').prop("disabled", true);
    $('#copyMoveBox_irekaeButton').prop("disabled", true);
    $('#copyMoveBox_deleteButton').prop("disabled", true);

    var errorMsg = '';
    var selectFromRenban = $('#copyMoveBox_selectFromRenban').val();
    var selectToRenban = $('#copyMoveBox_selectToRenban').val();
    // 移動元列と移動先列が選択されていない
    if (!selectFromRenban) {
        errorMsg = '移動元列が選択されていません。';
        $("#copyMoveBox_errorMsg").html(errorMsg);
        return;
    }
    // 移動元列が選択されている場合［削除］ボタンは使用可
    $('#copyMoveBox_deleteButton').prop("disabled", false);

    if (!selectToRenban) {
        errorMsg = '移動先列が選択されていません。';
        $("#copyMoveBox_errorMsg").html(errorMsg);
        return;
    }

    // 移動元列の最小値と移動先列が同じ
    if (selectFromRenban[0] == selectToRenban) {
        errorMsg = '移動元列と移動先列が同じです。';
        $("#copyMoveBox_errorMsg").html(errorMsg);
        return;
    }

    // 飛び石選択の判定
    var minRenban = selectFromRenban[0];
    var maxRenban = selectFromRenban[selectFromRenban.length-1];
    if ((maxRenban - minRenban) != (selectFromRenban.length-1)) {
        errorMsg = '飛び石選択は行えません。';
        $("#copyMoveBox_errorMsg").html(errorMsg);
        return;
    }

    // 最大列数の取得
    var idList = $('.header_renban');
    var maxId = $($(idList)[$(idList).length-1]).attr('id');
    var diffRenban = maxRenban - minRenban;
    if ((parseInt(selectToRenban) + parseInt(diffRenban)) > parseInt(maxId)) {
        errorMsg = '範囲選択エラーです。';
        $("#copyMoveBox_errorMsg").html(errorMsg);
        return;
    }

    // 移動する行の対象を取得
    if (!$('#copyMoveBox_showListToList').prop('checked')) {
        // 行のオプションを対象一覧へ移動する
        var selectTarget = $('#category_copymovebox_selected');
        var options = $('#category_copymovebox_name option');
        for (let index = 0; index < options.length; index++) {
            const option = options[index];
            $(selectTarget).append(option);
        }
    }

    var targetLines = [];
    var selectOptions = $('#category_copymovebox_selected option');
    for (let index = 0; index < selectOptions.length; index++) {
        const option = selectOptions[index];
        var targetKey = {};
        targetKey['line-key'] = $(option).attr('line-key');
        targetKey['line-key2'] = $(option).attr('line-key2');
        targetLines.push(targetKey);
    }

    var vLineObj = VerticalCellObj();
    var isMove = true;
    var isCopy = true;
    var isSwap = true;
    // 移動元範囲と移動先が重なっている場合は入替不可
    if (parseInt(selectToRenban) <= parseInt(selectFromRenban[selectFromRenban.length-1])) {
        isSwap = false;
    }
    for (let i = 0; i < selectFromRenban.length; i++) {
        const srcCol = parseInt(selectFromRenban[i]) - 1;
        var targetCol = parseInt(selectToRenban) + i - 1;
        if (isMove) {
            isMove = vLineObj.isVerticalLineMove(targetLines, srcCol, targetCol);
        }
        if (isCopy) {
            isCopy = vLineObj.isVerticalLineCopy(targetLines, srcCol, targetCol);
        }
        if (isSwap) {
            isSwap = vLineObj.isVerticalLineSwap(targetLines, srcCol, targetCol);
        }
        if (!isMove && !isCopy && !isSwap) {
            break;
        }
    }

    if (!isMove || !isCopy) {
        errorMsg = '移動先または、コピー先に同じ講座があります。';
    }
    if (!isSwap) {
        if (errorMsg) errorMsg += '<br>';
        errorMsg += '入れ替えは移動元範囲内では行えません。';
    }

    if (isMove) $('#copyMoveBox_moveButton').prop("disabled", false);
    if (isCopy) $('#copyMoveBox_copyButton').prop("disabled", false);
    if (isSwap) $('#copyMoveBox_irekaeButton').prop("disabled", false);

    $("#copyMoveBox_errorMsg").html(errorMsg);
}

//列でで移動コピー、移動コピー処理
function copyMoveBox_exec(mode){

    // 移動する行の対象を取得
    if (!$('#copyMoveBox_showListToList').prop('checked')) {
        // 行のオプションを対象一覧へ移動する
        var selectTarget = $('#category_copymovebox_selected');
        var options = $('#category_copymovebox_name option');
        for (let index = 0; index < options.length; index++) {
            const option = options[index];
            $(selectTarget).append(option);
        }
    }

    // 移動する行の絞込み
    var targetLines = [];
    var selectOptions = $('#category_copymovebox_selected option');
    for (let index = 0; index < selectOptions.length; index++) {
        const option = selectOptions[index];
        var targetKey = {};
        targetKey['line-key'] = $(option).attr('line-key');
        targetKey['line-key2'] = $(option).attr('line-key2');
        targetLines.push(targetKey);
    }

    // 移動元の対象列
    var fromOrders = [];
    var fromOrderOptions = $('#copyMoveBox_selectFromRenban option:selected');
    for (let i = 0; i < fromOrderOptions.length; i++) {
        const option = fromOrderOptions[i];
        var fromOrder = parseInt($(option).val()) - 1;
        fromOrders.push(fromOrder);
    }
    var toOrder = $('#copyMoveBox_selectToRenban').val();
    toOrder = parseInt(toOrder) - 1;

    var vLineObj = VerticalCellObj();
    if (mode == 'Move') {
        vLineObj.toVerticalLineMove(targetLines, fromOrders, toOrder);
    } else if (mode == 'Copy') {
        vLineObj.toVerticalLineCopy(targetLines, fromOrders, toOrder);
    } else if (mode == 'Swap') {
        vLineObj.toVerticalLineSwap(targetLines, fromOrders, toOrder);
    } else if (mode == 'Del') {
        vLineObj.toVerticalLineDelete(targetLines, fromOrders);
    } 

    return;
}



////////////////////////////////////////////////////////////////////////////////////////////////////////////////
// ウィンドウ表示
function showWindow(URL, param, winName){
    getContent('');
    if(document.forms[0].updateAddData.value != ''
    || document.forms[0].updateDelData.value != ''
    || document.forms[0].lineStaffInfo.value != '') {
        if(!confirm('{rval MSG108}')){
            return false;
        }
    }

    wopen(URL+param, winName,0,0,screen.availWidth,screen.availHeight);
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


//キーダウン処理。現在Deleteのみ対応
function documentKyeDown(event) {
    if (!$('#dialogBox').is(':hidden')) {
        return true;
    }

    if(event.keyCode == 46) {
        if (!$('input[name=selectTD]').val()) {
            return;
        }
        if ($('input[name=AUTHORITY]').val() != "4") {
            return;
        }
        if($('#' + $('input[name=selectTD]').attr('data-val')) != '') {
            showDialog('deleteSelectBox', '削除', deleteSelectInitFunc);
        }
    }
};


/******************************************************
 * マウス処理
 ******************************************************/

/***** 前回クリックしたTDに色を付ける *****/
function f_selectTDColor() {

    var element = $('#' + $('input[name=selectTD]').val())[0];
    if (!element) {
        return;
    }

    if ($(element).attr('data-val') == '') {
        $(element).css({'background-color': '#F5F599'});
    } else {
        // 背景色を変更する
        $(element).css({'background-color': '#F5F599'});
    }
}

/***** 前回クリックしたTDの色をクリア *****/
function f_clearSelectTDColor() {

    var element = $('#' + $('input[name=selectTD]').val())[0];
    if (!element) {
        return;
    }

    if ($(element).attr('data-val') == '') {
        $(element).css({'background-color': ''});
    } else {
        // 背景色を変更する
        $(element).css({'background-color': ''});
    }
    $('input[name=selectTD]').val('');

}

var refreshTimer = null;
/**
 * クリックイベント処理
 * クリックされたセルを保持
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
    $('input[name=selectTD]').val(obj.id);
    f_selectTDColor();


    // 画面右側のコースのリフレッシュ
    // ※体感速度を上げるため、タイマーのコールバックへ登録
    refreshTimer = setTimeout(function() {
        selectTDRefresh(event.target);
    }, 50);

    // ポップアップの表示制御
    doclick(event);
    if(!ActiveFlag){
        dodragleave();
    }
    ActiveFlag = false;
}

/**
 * ダブルクリックイベント処理
 *
 * @param event
 * @returns
 */
function f_dblclick(event) {

    // 講座作成画面(KNJB3030)を開く
    // var element = $('#' + $('input[name=selectTD]').val())[0];
    var element = this;
    if (!element) {
        return;
    }

    var idArray = $(element).attr('id').split('_');
    var clickId = '#' + idArray[0] + '_' + idArray[1] + '_' + idArray[2];
    if (!$(clickId).attr('data-val')) {
        return;
    }

    // var chairInfoList = new CellObj().getChairInfoList(element);
    var chairInfoList = new CellObj().getChairInfoList($(clickId));
    if (chairInfoList.length > 0) {
        var yearSeme = $('select[name=YEAR_SEME]').val();
        var chairCd = chairInfoList[0]["chairCd"];
        if (idArray.length > 3) {
            chairCd = chairInfoList[idArray[3]]["chairCd"];
        }

        var url = "../../B/KNJB3030/knjb3030index.php";
        var param = "?SEND_PRGRID=KNJB3043";
        param += "&cmd=" + "chairSelect";

        param += "&CHAIRCD=" + chairCd;
        param += "&term=" + yearSeme;
        param += "&schoolKind=" + "999";

        wopen(url+param, 'SUBWIN2', 0, 0, screen.availWidth, screen.availHeight);
    }


}

/***** ドラッグ開始時の処理 *****/
function f_dragstart(event){
    // ドラッグするデータのid名をDataTransferオブジェクトにセット
    event.dataTransfer.setData("text", event.target.id);
    $('input[name=startTD]').val(event.target.id);

    // クリックしたTDがあれば、色をリセット
    f_clearSelectTDColor();
    $('input[name=selectTD]').val(event.target.id);
    f_selectTDColor();
}

/***** ドラッグ要素がドロップ要素に重なっている間の処理 *****/
function f_dragover(event, obj){
    if(!obj){
        obj=this;
    }
    // dragoverイベントをキャンセルして、ドロップ先の要素がドロップを受け付けるようにする
    event.preventDefault();
    // 通過中の色設定
    if(obj.id=='TRASH_BOX'){
        $(obj).css('background-color', '#F58899');
    } else {
        // 同一の行のみ 移動/入替 可能
        // ドラッグ元のセル
        var idArray1 = $('input[name=startTD]').val().split('_');
        // ドラッグ先のセル
        var idArray2 = $(obj).attr('id').split('_');
        // 対象セル
        var targetId = '#' + idArray1[0] + '_' + idArray1[1] + '_' + idArray2[2];
        $(targetId).css({'background-color': '#F58899'});
    }
}

/***** ドラッグ要素がドロップ要素から出る時の処理 *****/
function f_dragleave(event, obj){

    if(!obj){
        obj=this;
    }
    event.preventDefault();

    if(obj.id=='TRASH_BOX'){
        $(obj).css({'background-color': ''});
    } else {
        // 同一の行のみ 移動/入替 可能
        // ドラッグ元のセル
        var idArray1 = $('input[name=startTD]').val().split('_');
        // ドラッグ先のセル
        var idArray2 = $(obj).attr('id').split('_');
        // 対象セル
        var targetId = '#' + idArray1[0] + '_' + idArray1[1] + '_' + idArray2[2];

        // 選択中の科目の場合は選択中の色へ変更する
        $(targetId).css({'background-color': ''});
        var selectCellId = "#" + $('input[name=selectTD]').val();
        if (selectCellId == targetId) {
            $(targetId).css({'background-color': '#F5F599'});
        }

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

    if ($('select[name=LEFT_MENU]').val() != "2") {
        $(obj).css({'background-color': ''});

        alert('「教育課程(年組)」以外は参照のみ可能です。');
        return false;
    }

    // ドラッグされたデータのid名をDataTransferオブジェクトから取得
    var srcId = event.dataTransfer.getData("text");
    if (!$('#' + srcId)) {
        event.preventDefault();
        return;
    }

    // ドラッグ元のセル
    var idArray1 = srcId.split('_');
    // ドラッグ先のセル
    var idArray2 = $(obj).attr('id').split('_');
    // 対象セル
    var targetId = '#' + idArray1[0] + '_' + idArray1[1] + '_' + idArray2[2];
    if (!$(targetId)) {
        event.preventDefault();
        return;
    }

    // ドラッグ元とドロップ先が同じ場合は処理しない
    if (('#' + srcId) == targetId) {
        // ドロップ先の色を選択中の色へ変更
        f_selectTDColor();
        return;
    }

    // 複数講座の場合の対応(全移動/対象講座のみ移動)
    var srcId = '#' + idArray1[0] + '_' + idArray1[1] + '_' + idArray1[2];
    var chairInfoList = new CellObj().getChairInfoList($(srcId));
    if (idArray1.length > 3) {
        var chair = chairInfoList[idArray1[3]];
        chairInfoList = [];
        chairInfoList.push(chair);
    }

    // 移動
    if ($('input[name=OPERATION_RADIO]:checked').val() == '1') {
        // ドロップ先セルへの書込み
        var targetCellObj = new CellObj();
        targetCellObj.setCell($(targetId));
        // 講座の追加判定
        if (!targetCellObj.checkAddChairList(chairInfoList)) {
            alert('同じ時間に同じ講座は設定できません。');
            //ドロップ後色を戻す
            $(targetId).css({'background-color': ''});
            return;
        }
        targetCellObj.addChairList(chairInfoList);
        targetCellObj.writeCell();

        // ドロップ元セルへの書込み
        var srcCellObj = new CellObj();
        srcCellObj.setCell($(srcId));
        srcCellObj.removeChairList(chairInfoList);
        srcCellObj.writeCell();
    }
    // コピー
    if ($('input[name=OPERATION_RADIO]:checked').val() == '2') {
        // ドロップ先セルへの書込み
        var targetCellObj = new CellObj();
        targetCellObj.setCell($(targetId));
        // 講座の追加判定
        if (!targetCellObj.checkAddChairList(chairInfoList)) {
            alert('同じ時間に同じ講座は設定できません。');
            //ドロップ後色を戻す
            $(targetId).css({'background-color': ''});
            return;
        }
        targetCellObj.addChairList(chairInfoList);
        targetCellObj.writeCell();
    }
    // 入れ替え
    if ($('input[name=OPERATION_RADIO]:checked').val() == '3') {

        // ドロップ先セルへの書込み
        var targetCellObj = new CellObj();
        targetCellObj.setCell($(targetId));

        var targetChairList = targetCellObj.getChairInfoList($(targetId));

        // 移動元から講座情報を削除
        var srcCellObj = new CellObj();
        srcCellObj.setCell($(srcId));
        srcCellObj.removeChairList(chairInfoList);
        // 移動元のセルへ講座が追加可能か判定
        if (!srcCellObj.checkAddChairList(targetChairList)) {
            alert('同じ時間に同じ講座は設定できません。');
            //ドロップ後色を戻す
            $(targetId).css({'background-color': ''});
            return;
        }
        // 移動先の講座情報を移動元のセルへ追加
        srcCellObj.addChairList(targetChairList);
        srcCellObj.writeCell();

        // 移動先の講座情報を全て削除後、移動元の講座情報を追加
        targetCellObj.removeChairAll();
        targetCellObj.addChairList(chairInfoList);
        targetCellObj.writeCell();
    }

    // 重複人数チェック
    getStdDupCnt($(targetId).attr('order'));
    getStdDupCnt($(srcId).attr('order'));

    // 未配置人数チェック
    getStdUnPlacedCnt($(targetId).attr('order'));
    getStdUnPlacedCnt($(srcId).attr('order'));

    // 施設講座超チェック
    getFacCapOverCnt($(targetId).attr('order'));
    getFacCapOverCnt($(srcId).attr('order'));

    //ドロップ後色を戻す
    $(targetId).css({'background-color': ''});

    // 選択中色をリセット
    f_clearSelectTDColor();
    $('input[name=selectTD]').val($(targetId).attr('id'));
    // ドロップ先の色を選択中の色へ変更
    f_selectTDColor();

    dodragleave(event);
    //エラー回避のため、ドロップ処理の最後にdropイベントをキャンセルしておく
    event.preventDefault();

}

/***** ゴミ箱ドロップ時の処理 *****/
function f_dropTrash(event, obj){

    obj = this;
    // ドラッグされたデータのid名をDataTransferオブジェクトから取得
    var srcId = event.dataTransfer.getData("text");
    if (!$('#' + srcId)) {
        event.preventDefault();
        return;
    }
    if ($('input[name=AUTHORITY]').val() != "4") {
        return;
    }
    if (!$('input[name=selectTD]').val()) {
        return;
    }
    if($('#' + srcId).attr('data-val') != '') {
        showDialog('deleteSelectBox', '削除', deleteSelectInitFunc);
    }

}


var CTarget;
var ActiveFlag;

//ポップアップを非表示
function dodragleave(event){
    var obj=$('#box')[0];
    obj.classList.add('non_active_box');
    obj.classList.remove('active_box');
}

function doclick(event){

    var obj = $('#box')[0];
    // 自分自身でポップアップが出ていれば非表示
    if(CTarget == event.currentTarget && obj.className == 'active_box'){
        obj.classList.add('non_active_box');
        obj.classList.remove('active_box');
    } else {
        // ポップアップが出てなければ表示
        var dataVal = $(event.target).attr('data-val');
        var values = [];
        if (dataVal.length > 1){
            values = JSON.parse(dataVal);
        }
        // ポップアップ表示
        if (values.length > 1) {
            var inHtml = '';
            for(var i=0; i < values.length; i++){
                var val = values[i];
                inHtml += '<div id="'+ event.currentTarget.id + '_' + i +'" class="inner_box" draggable="true">' + val['chairCd'] + '<br>'+ val['chairName'] + '</div>';
            }
            inHtml += '<div id="' + event.currentTarget.id + '" class="inner_box" style="background-color:#CCFFCC" draggable="true">全件移動</div>';
            obj.innerHTML = inHtml;
            // ポップアップする講座へドラッグ開始イベントの追加
            var elements = $('.inner_box');
            for(var i = 0; i < elements.length; i++ ) {
                elements[i].ondragstart = f_dragstart;
                elements[i].ondblclick = f_dblclick;
            }
            // ポップアップの座標位置設定
            obj.style.top = event.pageY+10;
            obj.style.left = event.pageX+10;
            //activeにしないと座標がとれない
            obj.classList.add('active_box');
            obj.classList.remove('non_active_box');
            //座標の再計算、一番下の時にセルの上方向、右なら左方向といった感じ
            pos = setposition(event, obj, $('#tbody')[0]);
            obj.style.top = pos.y;
            obj.style.left = pos.x;
            ActiveFlag = true;
        }
    }
    CTarget = event.currentTarget;
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


///////////////////////////////////////////////////////////

// 背景色ダイアログ初期化処理
function backColorInitFunc(){
    // 選択色リスト
    var colorList = [
        { "BACKCOLOR" : ""       , "FORECOLOR" : "" }
      , { "BACKCOLOR" : "#0000CC", "FORECOLOR" : "#FFFFFF" }
      , { "BACKCOLOR" : "#009933", "FORECOLOR" : "#FFFFFF"  }
      , { "BACKCOLOR" : "#0099FF", "FORECOLOR" : "#FFFFFF"  }
      , { "BACKCOLOR" : "#3399FF", "FORECOLOR" : "#000000"  }
      , { "BACKCOLOR" : "#993333", "FORECOLOR" : "#FFFFFF"  }
      , { "BACKCOLOR" : "#999933", "FORECOLOR" : "#000000"  }
      , { "BACKCOLOR" : "#99CC66", "FORECOLOR" : "#000000"  }
      , { "BACKCOLOR" : "#CC6699", "FORECOLOR" : "#000000"  }
      , { "BACKCOLOR" : "#CCFF00", "FORECOLOR" : "#000000"  }
    ];

    // 背景色のコンボ作成
    for (let index = 0; index < colorList.length; index++) {
        const element = colorList[index];

        var text = element['BACKCOLOR'];
        if (!text) {
            text = '初期値';
        }
        var option = $('<option>').val(element['BACKCOLOR']).text(text);
        $(option).attr('forecolor', element['FORECOLOR']);
        $(option).css({'background-color' : element['BACKCOLOR'] , 'color' : element['FORECOLOR']});

        $('select[name=backColorCapaOverColor]').append($(option).clone());
        $('select[name=backColorClassColor]').append($(option).clone());
        $('select[name=backColorSameMeiboColor]').append($(option).clone());
        $('select[name=backColorStdChairColor]').append($(option).clone());
    }

    // 展開表画面の初期値を取得
    $('select[name=backColorCapaOverColor]').val($('#colorBoxCapaOver').attr('bkcolor-value'));
    $('select[name=backColorClassColor]').val($('#colorBoxSelectClass').attr('bkcolor-value'));
    $('select[name=backColorSameMeiboColor]').val($('#colorBoxSameMeibo').attr('bkcolor-value'));
    $('select[name=backColorStdChairColor]').val($('#colorBoxStdChair').attr('bkcolor-value'));
    backColorSelectChange($('select[name=backColorClassColor]')[0]);
    backColorSelectChange($('select[name=backColorCapaOverColor]')[0]);
    backColorSelectChange($('select[name=backColorSameMeiboColor]')[0]);
    backColorSelectChange($('select[name=backColorStdChairColor]')[0]);

    /////////////////////////////////////////////////////////////////
    // 講座人数オーバーの初期設定
    //   処理なし

    /////////////////////////////////////////////////////////////////
    // 指定科目・講座の初期設定
    // 科目の一覧取得
    // 右側の科目一覧からコピーする
    var subclassOptions = $('select[name=SUBCLASSCD] option');
    for (let index = 0; index < subclassOptions.length; index++) {
        const option = subclassOptions[index];
        $('select[name=backColorSubclassSelect]').append($(option).clone());
    }

    // 時間割画面の初期値を取得
    var selectSubclass = $('#colorBoxSelectClassText').attr('subclassCd');
    var selectChair = $('#colorBoxSelectClassText').attr('chairCd');
    // 科目CDから講座を取得
    if (selectSubclass) {
        $('select[name=backColorSubclassSelect]').val(selectSubclass);
        backColorSelectSubclass();
    }


    /////////////////////////////////////////////////////////////////
    // 同一名簿の初期設定
    // 展開表に設定されている講座の一覧を設定する
    var chairOptionList = [];
    var chairPutList = [];
    var targetBoxs = $('.targetbox[data-val!=""]');
    for (let index = 0; index < targetBoxs.length; index++) {
        const element = targetBoxs[index];
        var dataVal = $(element).attr('data-val');
        var chairList = JSON.parse(dataVal);

        for (let i = 0; i < chairList.length; i++) {
            var chairInfo = chairList[i];
            if (!chairPutList[chairInfo['chairCd']]) {
                chairPutList[chairInfo['chairCd']] = chairInfo;
                chairOptionList.push({
                    "KEY" : chairInfo['chairCd']
                   ,"LABEL" : chairInfo['chairName']
                });
            }
        }
    }
    // ソートする
    chairOptionList.sort(function(a, b){
        if (a['KEY'] == b['KEY']) { return 0; }
        if (a['KEY'] == b['KEY']) { return 1; }
        if (a['KEY'] > b['KEY']) { return 1; }
        return -1;
    });
    // 空のオプション追加
    $('select[name=backColorSubSameMeiboSelect]').append($('<option>').val('').text(''));
    for (let index = 0; index < chairOptionList.length; index++) {
        const element = chairOptionList[index];
        var option = $('<option>').val(element['KEY']).text(element['KEY'] + " " + element['LABEL']);
        $('select[name=backColorSubSameMeiboSelect]').append($(option));
    }
    // 時間割画面の初期値を取得
    var meiboSelectChairCd = $('#colorBoxSameMeiboText').attr('chairCd');
    if (meiboSelectChairCd) {
        $('select[name=backColorSubSameMeiboSelect]').val(meiboSelectChairCd);
    }


    /////////////////////////////////////////////////////////////////
    // 指定生徒の初期設定
    // 年組の一覧取得
    // 右側の年組一覧からコピーする
    hrclassOptions = $('select[name=HRCLASSCD] option');
    for (let index = 0; index < hrclassOptions.length; index++) {
        var option = hrclassOptions[index];
        var optClone = $(option).clone();
        $(optClone).text($(option).val() + ' ' + $(option).text());
        $('select[name=backColorHrClassSelect]').append($(optClone));
    }
    // 時間割画面の初期値を取得
    var selectHrClassCd = $('#colorBoxStdChairText').attr('hrClass');
    if (selectHrClassCd) {
        $('select[name=backColorHrClassSelect]').val(selectHrClassCd);
        backColorSelectHrClass();
    }

}

// 背景色コンボの選択変更時、自身の背景色を変更
function backColorSelectChange(obj) {
    var option = $('select[name=' + $(obj).prop('name') + '] option:selected');
    $(obj).css({'background-color' : $(obj).val(), 'color' : $(option).attr('forecolor') });
}

// 背景色変更-科目変更時
function backColorSelectSubclass() {
    var selectSubclassCd = $('select[name=backColorSubclassSelect]').val();
    // 講座一覧は初期化
    $('select[name=backColorChairSelect] option').remove();
    if (selectSubclassCd) {
        var ajaxParam = {};
        // AJAXパラメタ設定
        ajaxParam['SUBCLASSCD'] = selectSubclassCd;

        $.ajax({
            url:'knjb3043index.php',
            type:'POST',
            data:{
                AJAX_PARAM:JSON.stringify(ajaxParam)
                , cmd:'getBackColorChair'
                , YEAR_SEME:$('select[name=YEAR_SEME] option:selected').val()
            }
        }).done(function(data, textStatus, jqXHR) {
            var paramList = $.parseJSON(data);
            var option = $('<option>').val('').text('');
            $('select[name=backColorChairSelect]').append(option);
            // 科目のリスト初期化
            for (var i = 0; i < paramList.length; i++) {
                var chairInfo = paramList[i];
                var option = $('<option>').val(chairInfo['CHAIRCD']).text(chairInfo['CHAIRCD'] + ' ' + chairInfo['CHAIRNAME']);
                $('select[name=backColorChairSelect]').append(option);
            }

            // 時間割画面の初期値を取得
            var selectChairCd = $('#colorBoxSelectClassText').attr('chairCd');
            if (selectChairCd) {
                $('select[name=backColorChairSelect]').val(selectChairCd);
            }
        });
    }
}

// 背景色変更-年組変更時
function backColorSelectHrClass() {

    var selectHrClassCd = $('select[name=backColorHrClassSelect] option:selected').val();
    // 受講生一覧は初期化
    $('select[name=backColorStdSelect] option').remove();
    if (selectHrClassCd) {
        var hrClass = selectHrClassCd.split('-');
        var ajaxParam = {};
        ajaxParam['GRADE'] = hrClass[0];
        ajaxParam['HR_CLASS'] = hrClass[1];

        $.ajax({
            url:'knjb3043index.php',
            type:'POST',
            data:{
                AJAX_PARAM:JSON.stringify(ajaxParam)
                , cmd:'getBackColorHrClassStdMeibo'
                , YEAR_SEME:$('select[name=YEAR_SEME] option:selected').val()
            }
        }).done(function(data, textStatus, jqXHR) {
            var paramList = $.parseJSON(data);
            var option = $('<option>').val('').text('');
            $('select[name=backColorStdSelect]').append(option);
            // 科目のリスト初期化
            for (var i = 0; i < paramList.length; i++) {
                var stdInfo = paramList[i];
                var option = $('<option>').val(stdInfo['SCHREGNO']);
                $(option).text(stdInfo['SCHREGNO'] + ' ' + stdInfo['NAME']);
                $('select[name=backColorStdSelect]').append(option);
            }

            // 時間割画面の初期値を取得
            var schregNo = $('#colorBoxStdChairText').attr('schregNo');
            if (schregNo) {
                $('select[name=backColorStdSelect]').val(schregNo);
            }
        });
    }
}

var interval;

// 背景色初期化処理
function executeBackColorInit() {

    $('#backColorProcessed').css('display','table');

    interval = setInterval(function () {
        clearInterval(interval);
        // 背景色を変更する講座の一覧
        var changeChairList = [];
        // 時間割に設定されている講座の一覧を設定する
        var chairPutList = [];
        var targetBoxs = $('.targetbox[data-val!=""]');
        for (let index = 0; index < targetBoxs.length; index++) {
            const element = targetBoxs[index];
            var dataVal = $(element).attr('data-val');
            var chairList = JSON.parse(dataVal);

            for (let i = 0; i < chairList.length; i++) {
                var chairInfo = chairList[i];

                if (!chairPutList[chairInfo['chairCd']]) {
                    chairPutList[chairInfo['chairCd']] = chairInfo;
                    changeChairList.push(chairInfo);
                }
            }
        }

        executeBackColorChange(changeChairList);
        // 時間割画面の背景色情報を初期化する
        $('#colorBoxCapaOver').text('初期値');
        $('#colorBoxCapaOver').attr('bkcolor-value', '');
        $('#colorBoxCapaOver').css({'background-color' :  '', 'color': ''});
        $('#colorBoxSelectClassText').text('');
        $('#colorBoxSelectClass').text('初期値');
        $('#colorBoxSelectClass').attr('bkcolor-value', '');
        $('#colorBoxSelectClass').css({'background-color' :  '', 'color': ''});
        $('#colorBoxSameMeiboText').text('');
        $('#colorBoxSameMeibo').text('初期値');
        $('#colorBoxSameMeibo').attr('bkcolor-value', '');
        $('#colorBoxSameMeibo').css({'background-color' :  '', 'color': ''});
        $('#colorBoxStdChairText').text('');
        $('#colorBoxStdChair').text('初期値');
        $('#colorBoxStdChair').attr('bkcolor-value', '');
        $('#colorBoxStdChair').css({'background-color' :  '', 'color': ''});

        $('#backColorProcessed').hide();
    }, 10);

}

// 講座人数オーバー背景色変更
function executeBackColorCapaOver() {

    $('#backColorProcessed').css('display','table');

    // 背景色を変更する講座の一覧
    var changeChairList = [];

    var ajaxParam = {};
    ajaxParam['UPDDATE'] = $('input[name=semesterEndDate]').val();

    $.ajax({
        url:'knjb3043index.php',
        type:'POST',
        data:{
            AJAX_PARAM:JSON.stringify(ajaxParam)
            , cmd:'getBackColorChairCapaOver'
            , YEAR_SEME: $('select[name=YEAR_SEME] option:selected').val()
        }
    }).done(function(data, textStatus, jqXHR) {
        var paramList = JSON.parse(data);
        for (let i = 0; i < paramList.length; i++) {
            const chairInfo = paramList[i];
            changeChairList.push(chairInfo['CHAIRCD']);
        }

        var colorOption = $('select[name=backColorCapaOverColor] option:selected');
        var backColor = $(colorOption).val();
        var foreColor = $(colorOption).attr('forecolor');
        executeBackColorChange(changeChairList, 'backColorCapaOver', backColor, foreColor);

        // 時間割画面の背景色情報を変更する(講座人数オーバー)
        $('#colorBoxCapaOver').text($(colorOption).text());
        $('#colorBoxCapaOver').attr('bkcolor-value', backColor);
        $('#colorBoxCapaOver').css({'background-color' :  backColor, 'color': foreColor});

        $('#backColorProcessed').hide();
    });

}

// 指定科目・講座 背景色変更
function executeBackColorClass() {

    $('#backColorProcessed').css('display','table');

    interval = setInterval(function () {
        clearInterval(interval);
        // 背景色を変更する講座の一覧
        var changeChairList = [];

        var selected = $('select[name=backColorChairSelect] option:selected').val();
        // 講座が選択されている場合、選択されている講座のみ対象
        if (selected) {
            selected  = ":selected";
        }
        var options = $('select[name=backColorChairSelect] option' + selected);
        for (let index = 0; index < options.length; index++) {
            var element = options[index];
            var chairCd = $(element).val();
            if (chairCd) {
                changeChairList.push(chairCd);
            }
        }

        var colorOption = $('select[name=backColorClassColor] option:selected');
        var backColor = $(colorOption).val();
        var foreColor = $(colorOption).attr('foreColor');
        executeBackColorChange(changeChairList, 'backColorClass', backColor, foreColor);

        // 時間割画面の背景色情報を変更する(指定科目・講座)
        var text = $('select[name=backColorSubclassSelect] option:selected').text();
        if (text) {
            if ($('select[name=backColorChairSelect] option:selected').val()) {
                text += '・' + $('select[name=backColorChairSelect] option:selected').text();
            }

            $('#colorBoxSelectClassText').attr('subclassCd', $('select[name=backColorSubclassSelect] option:selected').val());
            $('#colorBoxSelectClassText').attr('chairCd', $('select[name=backColorChairSelect] option:selected').val());
            $('#colorBoxSelectClassText').text('（' + text + '）');
        }
        $('#colorBoxSelectClass').text($(colorOption).text());
        $('#colorBoxSelectClass').attr('bkcolor-value', backColor);
        $('#colorBoxSelectClass').css({'background-color' :  backColor, 'color': foreColor});

        $('#backColorProcessed').hide();
    }, 10);
}
// 同一名簿 背景色変更
function executeBackColorSameMeibo() {

    $('#lockScreen').css({'width': $(window).width() + "px"});
    $('#lockScreen').css({'height': $(window).height() + "px"});
    $('#lockScreen').css('display','table');
    $('#backColorProcessed').css('display','table');

    // 背景色を変更する講座の一覧
    var changeChairList = [];

    var ajaxParam = {};
    ajaxParam['CHAIRCD'] = $('select[name=backColorSubSameMeiboSelect]').val();
    ajaxParam['UPDDATE'] = $('input[name=semesterEndDate]').val();

    $.ajax({
        url:'knjb3043index.php',
        type:'POST',
        data:{
            AJAX_PARAM:JSON.stringify(ajaxParam)
            , cmd:'getBackColorChairSameMeibo'
            , YEAR_SEME:$('select[name=YEAR_SEME] option:selected').val()
        }
    }).done(function(data, textStatus, jqXHR) {
        var paramList = JSON.parse(data);
        for (let i = 0; i < paramList.length; i++) {
            const chairInfo = paramList[i];
            changeChairList.push(chairInfo['CHAIRCD']);
        }

        var colorOption = $('select[name=backColorSameMeiboColor] option:selected');
        var backColor = $(colorOption).val();
        var foreColor = $(colorOption).attr('foreColor');
        executeBackColorChange(changeChairList, 'backColorSameMeibo', backColor, foreColor);

        // 時間割画面の背景色情報を変更する(同一名簿)
        if ($('select[name=backColorSubSameMeiboSelect] option:selected').val()) {
            $('#colorBoxSameMeiboText').attr('chairCd', $('select[name=backColorSubSameMeiboSelect] option:selected').val());
            var text = $('select[name=backColorSubSameMeiboSelect] option:selected').text();
            $('#colorBoxSameMeiboText').text('（' + text + '）');
        }
        $('#colorBoxSameMeibo').text($(colorOption).text());
        $('#colorBoxSameMeibo').attr('bkcolor-value', backColor);
        $('#colorBoxSameMeibo').css({'background-color' :  backColor, 'color': foreColor});

        $('#lockScreen').hide();
        $('#backColorProcessed').hide();
    });
}
// 生徒の受講講座 背景色変更
function executeBackColorStdChair() {

    var selectSchregNo = $('select[name=backColorStdSelect]').val();

    if (!selectSchregNo) {
        alert('生徒が指定されていません。生徒を選択してください。');
        return;
    }

    $('#lockScreen').css({'width': $(window).width() + "px"});
    $('#lockScreen').css({'height': $(window).height() + "px"});
    $('#lockScreen').css('display','table');
    $('#backColorProcessed').css('display','table');

    // 背景色を変更する講座の一覧
    var changeChairList = [];

    var ajaxParam = {};
    ajaxParam['SCHREGNO'] = $('select[name=backColorStdSelect]').val();
    ajaxParam['UPDDATE'] = $('input[name=semesterEndDate]').val();

    $.ajax({
        url:'knjb3043index.php',
        type:'POST',
        data:{
            AJAX_PARAM:JSON.stringify(ajaxParam)
            , cmd:'getBackColorStdChair'
            , YEAR_SEME:$('select[name=YEAR_SEME] option:selected').val()
        }
    }).done(function(data, textStatus, jqXHR) {
        var paramList = JSON.parse(data);
        // 科目のリスト初期化
        for (let i = 0; i < paramList.length; i++) {
            const chairInfo = paramList[i];
            changeChairList.push(chairInfo['CHAIRCD']);
        }

        var colorOption = $('select[name=backColorStdChairColor] option:selected');
        var backColor = $(colorOption).val();
        var foreColor = $(colorOption).attr('foreColor');
        executeBackColorChange(changeChairList, 'backColorStdChair', backColor, foreColor);

        // 時間割画面の背景色情報を変更する(指定生徒の受講講座)
        if ($('select[name=backColorHrClassSelect] option:selected').val()) {
            $('#colorBoxStdChairText').attr('hrClass', $('select[name=backColorHrClassSelect] option:selected').val());
            $('#colorBoxStdChairText').attr('schregNo', $('select[name=backColorStdSelect] option:selected').val());
            var text = $('select[name=backColorHrClassSelect] option:selected').text();
            if ($('select[name=backColorStdSelect] option:selected').val()) {
                text += '・' + $('select[name=backColorStdSelect] option:selected').text();
            }
            $('#colorBoxStdChairText').text('（' + text + '）');
        }
        $('#colorBoxStdChair').text($(colorOption).text());
        $('#colorBoxStdChair').attr('bkcolor-value', backColor);
        $('#colorBoxStdChair').css({'background-color' :  backColor, 'color': foreColor});

        $('#lockScreen').hide();
        $('#backColorProcessed').hide();
    });
}

// 時間割の背景色変更
function executeBackColorChange(changeChairList, className, backColor, foreColor) {

    if (className) {
        $('#backColorStyle_' + className + '').remove();
        if (backColor) {
            var style = '<style id="backColorStyle_' + className + '">';
            style += '  td.' + className + ' { '
            style += '  background-color : ' + backColor +';';
            style += '  color : ' + foreColor +';';
            style += '} ';
            style += '</style>';
            $(document.forms[0]).append(style);
        }
    } else {
        $('#backColorStyle_backColorCapaOver').remove();
        $('#backColorStyle_backColorClass').remove();
        $('#backColorStyle_backColorSameMeibo').remove();
        $('#backColorStyle_backColorStdChair').remove();
    }

    // 時間割に設定されている講座の一覧を設定する
    var targetBoxs = $('.targetbox[data-val!=""]');
    for (let index = 0; index < targetBoxs.length; index++) {
        const element = targetBoxs[index];
        var chairList = JSON.parse($(element).attr('data-val'));

        if (className) {
            // セルに設定されている講座数分
            for (let i = 0; i < chairList.length; i++) {
                const chairInfo = chairList[i];
                // 背景色変更一覧に存在する場合
                // 背景色を変更する
                if (changeChairList.indexOf(chairInfo['chairCd']) >= 0) {
                    $(element).removeClass(className);
                    $(element).addClass(className);
                    break;
                } else {
                    $(element).removeClass(className);
                }
            }
        } else {
            // 背景色のクラスを削除
            $(element).removeClass('backColorCapaOver');
            $(element).removeClass('backColorClass');
            $(element).removeClass('backColorSameMeibo');
            $(element).removeClass('backColorStdChair');
        }

    }
}


///////////////////////////////////////////////////////////
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
//
    $('#trowf')[0].scrollLeft = $('#tbody')[0].scrollLeft;
}







