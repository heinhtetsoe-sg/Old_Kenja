
function btn_submit(cmd) {

    if (cmd == 'update') {
        if (!$('input[name=BSCTITLE]').val()) {
            alert('基本時間割のタイトルが設定されていません。');
            return false;
        }
        if (0 < $('input[name=CHAIR_CNT]').val()) {
            if (!confirm('基本時間割に登録されている講座が存在します。\r\n基本時間割の再作成を行います。よろしいでしょうか？')) {
                return false;
            }
        }
        if (!confirm('{rval MSG101}')) {
            return false;
        }
    }
    getContent();
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return true;
}

// 更新用の情報取得
function getContent(cmd) {

    var preSeqData = {};

    var cells = $('.targetbox');
    for (let i = 0; i < cells.length; i++) {
        const cell = cells[i];
        preSeqData[$(cell).attr('id')] = $(cell).text();
    }
    if (cmd != 'update') {
        var cells = $('.noneSeqbox');
        for (let i = 0; i < cells.length; i++) {
            const cell = cells[i];
            preSeqData[$(cell).attr('id')] = $(cell).text();
        }
    }
    $('input[name=PRESEQDATA]').val(JSON.stringify(preSeqData));
}

function displayInit() {
    // 起動時ソート
    selectNoneSeqSort();
}
function selectNoneSeqSort() {

    var noneSeqCell = $('select[name=NONE_SEQ]');
    if (noneSeqCell.length <= 0) {
        return;
    }

    // 入替え後にオプションをソートする
    var selectNoneSeq = $('select[name=NONE_SEQ] option').clone();
    selectNoneSeq.sort(function(a, b) {
        // ソートは TEXT の値を見て判定する
        if (parseInt(a.text) == parseInt(b.text)) { return 0; }
        if (parseInt(a.text) > parseInt(b.text)) { return 1; }
        return -1;
    });
    $('select[name=NONE_SEQ] option').remove();
    selectNoneSeq.each(function() {
        $('select[name=NONE_SEQ]').append($(this));
    });

}
// 選択中の値を入替え
function btn_seqSwap() {

    // 未設定列番号
    var noneSeqCell = $('select[name=NONE_SEQ] option:selected');
    if (noneSeqCell.length <= 0) {
        return;
    }
    // 選択中のセル
    var selectCellId = $('input[name=selectCell]').val();
    if (!selectCellId) {
        return;
    }

    // 値を入替え
    var seq = $(noneSeqCell).text();
    if ($('#' + selectCellId).text()) {
        $(noneSeqCell).text($('#' + selectCellId).text());
        $(noneSeqCell).attr('id', 'NONE_' + $('#' + selectCellId).text());
    } else {
        $(noneSeqCell).remove();
    }
    $('#' + selectCellId).text(seq);

    // 値入替え後、ソートする
    selectNoneSeqSort();
}
// セルの列番号をクリア
function clearCellSeq(cell) {
    if (!$(cell).text()) {
        return;
    }
    var seq = $(cell).text();
    // 列番号をクリア
    $(cell).text('');
    // 背景食を戻す
    $(cell).css('background-color','');
    // 列番号を未設定列番号へ追加
    var noneOption = $('<option>').attr('id', 'NONE_' + seq).text(seq);
    $(noneOption).addClass('noneSeqbox');
    $(noneOption).css({'height':'25px', 'text-align':'center'});
    $('select[name=NONE_SEQ]').append(noneOption);
    // 値入替え後、ソートする
    selectNoneSeqSort();
}
// 選択状態のセル背景色を戻す
function clearSelectCellColor() {
    var cellId = $('input[name=selectCell]').val();
    if (cellId) {
        $('#' + cellId).css('background-color','');
    }
    $('input[name=selectCell]').val('');
}

// チェックされている曜日を初期化
function seqInitChecked() {
    var weekChecks = $('input[name=weekCheck]');
    for (let i = 0; i < weekChecks.length; i++) {
        const check = weekChecks[i];
        if ($(check).prop('checked')) {
            seqInit($(check).val());
        }
    }
}
// 列番号を初期化
function seqInit(week) {

    var cells = $('.targetbox');
    for (let i = 0; i < cells.length; i++) {
        const cell = cells[i];
        // 取得した曜日と一致しない場合は処理しない
        var cellId = $(cell).attr('id');
        if (week != cellId.substr(0, week.length)) {
            continue;
        }
        // 列番号に値がある場合、未設定列番号に値を設定
        if ($(cell).text()) {
            var seq = $(cell).text();
            // 列番号をクリア
            $(cell).text('');
            // 背景食を戻す
            $(cell).css('background-color','');
            // 列番号を未設定列番号へ追加
            var noneOption = $('<option>').attr('id', 'NONE_' + seq).text(seq);
            $(noneOption).addClass('noneSeqbox');
            $(noneOption).css({'height':'25px', 'text-align':'center'});
            $('select[name=NONE_SEQ]').append(noneOption);
        }
    }
    // 値入替え後、ソートする
    selectNoneSeqSort();

}

// チェックされている曜日を初期化
function seqAttachChecked() {
    var weekChecks = $('input[name=weekCheck]');
    for (let i = 0; i < weekChecks.length; i++) {
        const check = weekChecks[i];
        if ($(check).prop('checked')) {
            seqAttach($(check).val());
        }
    }
}
// 未設定一覧にある列番号を空白の列番号へ設定
function seqAttach(week) {

    var cells = $('.targetbox');
    var unAttachList = $('input[name=UNATTACH]').val().split(',');

    for (let i = 0; i < cells.length; i++) {
        const cell = cells[i];
        // 取得した曜日と一致しない場合は処理しない
        var cellId = $(cell).attr('id');
        if (week != cellId.substr(0, week.length)) {
            continue;
        }
        // 読み飛ばしの校時
        if (unAttachList && unAttachList.indexOf(cellId.substr(2)) >= 0) {
            continue;
        }
        // 列番号が空白の場合、未設定列番号の最小値を設定
        if (!$(cell).text()) {

            var noneSeqCell = $('select[name=NONE_SEQ] option');
            if (noneSeqCell.length > 0) {
                var option = noneSeqCell[0];

                $(cell).text($(option).text());
                // 未設定一覧から削除
                $(option).remove();
            }
        }
    }
}

/***** クリックの処理 *****/
function f_click(event) {
    obj = this;

    clearSelectCellColor();
    $(obj).css('background-color','#F5F599');
    $('input[name=selectCell]').val($(obj).attr('id'));

}
/***** ダブルクリックの処理 *****/
function f_dblclick(event){
}
/***** ドラッグ開始時の処理 *****/
function f_dragstart(event){
    obj = this;
    //ドラッグするデータのid名をDataTransferオブジェクトにセット
    event.dataTransfer.setData("text", event.target.id);

    clearSelectCellColor();
    $(obj).css('background-color','#F5F599');
    $('input[name=selectCell]').val($(obj).attr('id'));
}
/***** ドロップイベント *****/
function f_drop(event){
    obj = this;

    var srcCellId = event.dataTransfer.getData("text");
    var srcCell = $('#' + srcCellId);
    var targetCell = this;
    // 値を変更
    var seq = $(srcCell).text();
    $(srcCell).text($(targetCell).text());
    $(targetCell).text(seq);
    // 背景食を戻す
    $(srcCell).css('background-color','');
    $(targetCell).css('background-color','');
}
/***** ドラッグ要素がドロップ要素に重なっている間の処理 *****/
function f_dragover(event, obj){
    if(!obj){
        obj = this;
    }
    //dragoverイベントをキャンセルして、ドロップ先の要素がドロップを受け付けるようにする
    event.preventDefault();
    //通過中の色設定
    $(obj).css('background-color','#F58899');
}

/***** ドラッグ要素がドロップ要素から出る時の処理 *****/
function f_dragleave(event, obj){
    if(!obj){
        obj = this;
    }
    event.preventDefault();
    $(obj).css('background-color','');
}
/***** ドラッグの基本イベント *****/
function f_dragevent(event){
    event.preventDefault();
}
/***** ゴミ箱ドロップイベント *****/
function f_dropTrash(event, obj) {
    if(!obj){
        obj = this;
    }

    // 列番号をクリア
    var srcCellId = event.dataTransfer.getData("text");
    clearCellSeq($('#' + srcCellId));
    // ゴミ箱の背景色を戻す
    $(obj).css('background-color', '');
}
/***** コンテキストメニューイベント *****/
function f_contextmenu(){
    return false;
}
/***** キーダウン処理。現在Deleteのみ対応 *****/
function documentKyeDown(event) {
    if (event.keyCode == 46) {
        // 選択情報のセルがある場合のみ
        var cellId = $('input[name=selectCell]').val();
        if (cellId) {
            // 列番号をクリア
            clearCellSeq($('#' + cellId));
        }
    }
};


//権限チェック
function OnAuthError()
{
    alert('{rval MSG300}');
    closeWin();
}
