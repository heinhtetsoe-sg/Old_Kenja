var globalDelKeyList = {};
var globalDelKeyListDef = {};
var globalLayoutRightListFull = '';
var globalLayoutRightList = '';

function btn_submit(cmd) {

    //サブミット中、更新ボタン使用不可
    if ($('input[name=btn_update]').prop('disabled')) {
        $('input[name=btn_update]').prop('disabled', true);
    }

    if (cmd == "update") {
        if ($('select[name=PRESEQ]').val()) {
            showDialog('ptrnUpdateBox', '科目展開表更新', ptrnUpdateInitFunc);
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
        } else if (cmd == "preSeqDelete") {
            if(!confirm('{rval MSG103}' + '\n注意：このテンプレートの関連データも全て削除されます！')){
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

function btn_close(){
    getContent('');
    if($('input[name=updateDataList]').val()) {
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

    $('input[name=btn_preseq_delete]').prop('disabled', false);
    $('input[name=btn_creditMstread]').prop('disabled', false);

    $('input[name=btn_set]').prop('disabled', false);

    // HRクラスの場合は参照のみ
    //   [更新][削除] [反映]使用不可
    if ($('select[name=LEFT_MENU]').val() == "2") {
        $('input[name=btn_update]').prop('disabled', true);
        $('input[name=btn_reset]').prop('disabled', true);
        $('input[name=btn_preseq_delete]').prop('disabled', true);
        $('input[name=btn_creditMstread]').prop('disabled', true);
        $('input[name=btn_set]').prop('disabled', true);
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

    $('#lockScreen').css({'width': $(document).width() + "px"});
    $('#lockScreen').css({'height': $(document).height() + "px"});
    $('#lockScreen').css('display','table');

    btn_submit('edit');
}

/**
 * [単位マスタ読込]ボタン押下処理
 */
function yomikomiCreditsMstCheck(){

    var targetBoxs = $('.targetbox[data-val!=""]');
    if (targetBoxs.length > 0) {
        if(!confirm('{rval MSG104}' + '\n注意：このテンプレートの関連データも全て削除されます！')){
            return false;
        }
    }
    $("input[name=readCreditsMstFlg]").val("1");

    btn_submit('edit');
}

// 更新ボタンの処理
// 設定したデータの取得
function getContent(cmd) {

    var updateDataList = [];

    // 更新
    if (cmd == 'updatePtrn') {
        // 新規登録の場合は科目情報の登録されているデータのフラグを立てる
        if ($('input[name=PTRN_UPDATE_RADIO]:checked').val() == '2') {
            var courseList = $('.redips-mark');
            for (let i = 0; i < courseList.length; i++) {
                const course = courseList[i];
                var subclassList = $('.targetbox[line-key=' + $(course).attr('line-key') + '][data-val!=""]');
                if (subclassList.length > 0) {
                    $(course).attr('data-update', '1')
                }
            }
        }
    }

    // 更新するリスト作成
    var courseList = $('.redips-mark[data-update=1]');
    for (let i = 0; i < courseList.length; i++) {
        const course = courseList[i];
        
        var updateData = {};
        // コースID
        updateData['COURSE'] = $(course).attr('line-key');
        // 科目一覧
        updateData['SUBCLASS'] = [];
        var subclassList = $('.targetbox[line-key=' + $(course).attr('line-key') + ']');
        for (let j = 0; j < subclassList.length; j++) {
            const element = subclassList[j];
            if ($(element).attr('data-val')) {
                var updData = JSON.parse($(element).attr('data-val'));
                var subclass = {};
                subclass['preOrder'] = j;
                subclass['classcd'] = updData['classcd'];
                subclass['school_kind'] = updData['school_kind'];
                subclass['curriculum_cd'] = updData['curriculum_cd'];
                subclass['subclasscd'] = updData['subclasscd'];

                updateData['SUBCLASS'].push(subclass);
            }
        }
        updateDataList.push(updateData);
    }
    $('input[name=updateDataList]').val(JSON.stringify(updateDataList));

    return;
}

/**
 * 科目情報の追加
 */
function setSubclass() {

    if ($('select[name=LEFT_MENU]').val() == "2") {
        alert('クラス表示の場合は参照のみ可能です。');
        return false;
    }

    var subclassList = [];
    var selectSubclass = $('select[name=CATEGORY_SELECTED] option:selected');
    for (let i = 0; i < selectSubclass.length; i++) {
        const element = selectSubclass[i];
        subclassList.push($(element).attr('data-val'));
    }

    var courseObj = new CourseObj();
    courseObj.setCourseObj($('select[name=COURSECD]').val());

    var targetId = $('input[name=selectTD]').val();
    var targetCell;
    if (targetId != '') {
        // 選択中セルのコースと設定科目一覧(右側)の選択中コースが同じ場合
        if ($('select[name=COURSECD]').val() == $('#' + targetId).attr('line-key')) {
            targetCell = $('#' + targetId);
        }
    }
    // セルを選択していない場合
    if (!targetCell) {
        // 設定科目一覧(右側)の選択中コースから行を割り出す
        targetCell = "";

        var targetList = $('.targetbox[line-key=' + $('select[name=COURSECD]').val() + ']');
        for (let i = 0; i < targetList.length; i++) {
            const element = targetList[i];
            if ($(element).attr('data-val') == '') {
                targetCell = $('#' + $(element).attr('id'));
                break;
            }
        }
    }
    // コースへ追加
    courseObj.addSubclass(subclassList, targetCell);
    // コースの変更フラグ設定
    $('.redips-mark[line-key=' + $(targetCell).attr('line-key') + ']').attr('data-update', '1');


    // 科目情報の追加後、右の科目情報のリストから追加した情報を削除
    var selectSubclass = $('select[name=CATEGORY_SELECTED] option:selected');
    for (let i = 0; i < selectSubclass.length; i++) {
        const element = selectSubclass[i];
        $(element).remove();
    }

    // 選択中セルを解除
    f_clearSelectTDColor();


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
    // $('select[name=COURSECD]').val($(target).attr('line-key'));
    $('select[name=COURSECD]').val(targetLineKey);
    // 右側の科目一覧をリフレッシュ
    selectCourse();

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
        $('select[name=CATEGORY_SELECTED] option').remove();
        for (var i = 0; i < paramList.length; i++) {
            // 展開表のコースで選択されている科目は除外する
            var isAny = false;
            const paramElement = paramList[i];

            var lineArray = $('.targetbox[line-key='+ courseCd +']');
            for (var index = 0; index < lineArray.length; index++) {
                const lineElement = lineArray[index];
                if ($(lineElement).attr('data-val') == '') {
                    break;
                }
                var lineSubclass = JSON.parse($(lineElement).attr('data-val'));
                var subclassKey = lineSubclass['classcd'];
                subclassKey += '-' + lineSubclass['school_kind'];
                subclassKey += '-' + lineSubclass['curriculum_cd'];
                subclassKey += '-' + lineSubclass['subclasscd'];
                if (paramElement['VALUE'] == subclassKey) {
                        isAny = true;
                        break;
                }
            }
            // 展開表の科目に登録されていない為、追加する
            if (!isAny) {
                var dataVal = {};
                dataVal['classcd'] = paramElement['CLASSCD'];
                dataVal['school_kind'] = paramElement['SCHOOL_KIND'];
                dataVal['curriculum_cd'] = paramElement['CURRICULUM_CD'];
                dataVal['subclasscd'] = paramElement['SUBCLASSCD'];
                dataVal['credits'] = paramElement['CREDITS'];
                dataVal['subclassname'] = paramElement['SUBCLASSABBV'];

                var option = $('<option>').val(paramElement['VALUE']).text(paramElement['VALUE']+ ' ' +paramElement['LABEL']);
                $(option).attr('data-val', JSON.stringify(dataVal));
                $('select[name=CATEGORY_SELECTED]').append(option);
            }
        }

    });
}


// 科目展開表 更新画面初期化
function ptrnUpdateInitFunc() {
    var preSeqText = $('#PRESEQ option:selected').text();
    if($('#PRESEQ').val() != '0'){
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

    var targetCell = $('.targetbox[id=' + targetId + ']');
    var course = $('.redips-mark[line-key=' + $(targetCell).attr('line-key') + ']');

    // タイトル設定(コース名)
    $('#deleteSelect_targetName').text($(course).text());

    var trData = '<tr>';
    trData += '<td>' + $(targetCell).text() + '</td>';
    trData += '<td align="center"><input type="checkbox" name="deleteSelectCheckBox" value="'+ $(targetCell).attr('data-val') + '" checked="checked"></td>';
    trData += '</tr>';
    $('#deleteSelectTable').append(trData);

}

//削除ダイアログ削除処理
function deleteSelect_deleteButton() {

    var targetId = $('input[name=selectTD]').val();

    var targetCell;
    if (targetId) {
        targetCell =  $('#' + targetId);
    }
    if (!targetCell) {
        return;
    }

    // 削除用レコード
    var isChecked = $('input[name=deleteSelectCheckBox]:checked');
    if (isChecked.length > 0) {
        // 選択中セルを解除
        f_clearSelectTDColor();

        var courseObj = new CourseObj();
        courseObj.setCourseObj($(targetCell).attr('line-key'));
        // 科目情報の削除
        courseObj.deleteSubclass(targetCell);

        $('.redips-mark[line-key=' + $(targetCell).attr('line-key') + ']').attr('data-update', '1');
        // 設定科目一覧の科目リストを再取得
        selectCourse();
    }

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
    ajaxParam['SCHOOL_KIND'] = $('select[name=SCHOOL_KIND] option:selected').val();
    $.ajax({
        url:'knjb3043index.php',
        type:'POST',
        data:{
              AJAX_PARAM:JSON.stringify(ajaxParam)
            , cmd:'getLayoutCourse'
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
            $(option).attr('schoolKind', param["SCHOOL_KIND"])

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
        var courseCd = $(element.cells[0]).attr('line-key');

        $(element).hide();
        for (let j = 0; j < newSelectedList.length; j++) {
            const selectElement = newSelectedList[j];
            var selectCourseCd = $(selectElement).val();
            if (courseCd == selectCourseCd) {
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

    var schoolKind = $('select[name=SCHOOL_KIND] option:selected').val();
    var optionList = $('select[name=category_course_name] option');
    for (let index = 0; index < optionList.length; index++) {
        const element = optionList[index];
        if (schoolKind == $(element).attr('schoolkind')) {
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

    if(event.keyCode == 46 
        && $('input[name=selectTD]').val() !=''
        && $('input[name=AUTHORITY]').val() == "4") {
        if($('#' + $('input[name=selectTD]').attr('data-val')) != '') {

            if ($('select[name=LEFT_MENU]').val() == "1") {
                showDialog('deleteSelectBox', '削除', deleteSelectInitFunc);
            }
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
        // コース内の同じ科目は背景色を変更する
        var lineKey = $(element).attr('line-key');
        var lineKey2 = $(element).attr('line-key2');
        var lineArray = $('.targetbox[line-key='+ lineKey +'][line-key2='+ lineKey2 +']');
        for (let index = 0; index < lineArray.length; index++) {
            const lineElement = lineArray[index];
            if ($(element) == $(lineElement)
            || $(element).attr('data-val') == $(lineElement).attr('data-val')) {
                $(lineElement).css({'background-color': '#F5F599'});
            }
        }
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
        // コース内の同じ科目は背景色を変更する
        var lineKey = $(element).attr('line-key');
        var lineArray = $('.targetbox[line-key='+ lineKey +']');
        for (let index = 0; index < lineArray.length; index++) {
            const lineElement = lineArray[index];
            if ($(element) == $(lineElement)
            || $(element).attr('data-val') == $(lineElement).attr('data-val')) {
                $(lineElement).css({'background-color': ''});
            }
        }
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


    // 画面右側のHRクラス・コースのリフレッシュ
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
function f_dblclick(event){
    // ポップアップ画面はないので処理なし
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
        if ($(selectCellId).attr('data-val') != ''
            && $(selectCellId).attr('data-val') == $(targetId).attr('data-val')) {
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

    if ($('select[name=LEFT_MENU]').val() == "2") {
        $(obj).css({'background-color': ''});

        alert('クラス表示の場合は参照のみ可能です。');
        return false;
    }

    // ドラッグされたデータのid名をDataTransferオブジェクトから取得
    var id_name = event.dataTransfer.getData("text");
    var dragIdArray = id_name.split('_');

    //id名からドラッグされた要素を取得
    var dragElement = $('#' + dragIdArray[0]+'_' + dragIdArray[1] + '_' + dragIdArray[2])[0];
    if (!dragElement) {
        event.preventDefault();
        return;
    }
    var dropElement = null;

    // ドラッグ先のセル
    var dropIdArray = $(obj).attr('id').split('_');
    // 対象セル
    var targetId = '#' + dragIdArray[0] + '_' + dragIdArray[1] + '_' + dropIdArray[2];
    dropElement = $(targetId)[0];

    if (!dropElement) {
        event.preventDefault();
        return;
    }

    var linekey = $(dragElement).attr('line-key');
    var dragSubclass = JSON.stringify(JSON.parse($(dragElement).attr('data-val')));
    // ドラッグ中の色を消す
    $(dropElement).css({'background-color': ''});


    // 前回にクリックしたTDがあれば、色をリセット
    f_clearSelectTDColor();

    // 科目情報入替え
    if (dragElement != dropElement) {
        var courseObj = new CourseObj();
        courseObj.setCourseObj(linekey);
        courseObj.moveSubclass(dragElement, dropElement);
    }

    // TDに色を付ける
    var lineArray = $('.targetbox[line-key='+ linekey +']');
    for (var index = 0; index < lineArray.length; index++) {
        var lineElement = lineArray[index];
        if (dragSubclass == $(lineElement).attr('data-val')) {
            $('input[name=selectTD]').val($(lineElement).attr('id'));
            break;
        }
    }
    // ドラッグ元の科目情報でセルの先頭を取得
    f_selectTDColor();

    dodragleave(event);
    //エラー回避のため、ドロップ処理の最後にdropイベントをキャンセルしておく
    event.preventDefault();

}

/***** ゴミ箱ドロップ時の処理 *****/
function f_dropTrash(event, obj){

    obj = this;

    if ($('select[name=LEFT_MENU]').val() == "2") {
        alert('クラス表示の場合は参照のみ可能です。');
        event.target.style.backgroundColor = "#003366";
        return false;
    }

    //ドラッグされたデータのid名をDataTransferオブジェクトから取得
    var id_name = event.dataTransfer.getData("text");
    var dragIdArray = id_name.split('_');

    //id名からドラッグされた要素を取得
    var dragElement = $('#' + dragIdArray[0]+'_' + dragIdArray[1] + '_' + dragIdArray[2])[0];
    if (!dragElement) {
        event.preventDefault();
        return;
    }

    $('input[name=selectTD]').val($(dragElement).attr('id'));
    showDialog('deleteSelectBox', '削除', deleteSelectInitFunc);

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

    var obj=$('#box')[0];
    // 自分自身でポップアップが出ていれば非表示
    if(CTarget == e.currentTarget && obj.className == 'active_box'){
        obj.classList.add('non_active_box');
        obj.classList.remove('active_box');
    } else {
        // ポップアップが出てなければ表示
        var dataVal = $(e.target).attr('data-val');
        var values = [];
        if (dataVal.length > 1){
            values = JSON.parse(dataVal);
        }
        // ポップアップ表示
        if (values.length > 1) {
            var inHtml = '';
            for(var i=0; i < values.length; i++){
                var val = values[i];
                inHtml+='<div id="'+ e.currentTarget.id + '_' + i +'" class="inner_box" draggable="true">' + val['grade'] + '-' + val['hrclasscd'] + '<br>'+ val['hrclassname'] + '</div>';
            }
            inHtml+='<div id="'+e.currentTarget.id+'_all" class="inner_box" style="background-color:#CCFFCC" draggable="true">全件移動</div>';
            obj.innerHTML = inHtml;
            // ポップアップの座標位置設定
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
}



