function baseTdtag(id){
    return '<td id="'+id+'" data-val="" ></td>';
}

function CellObj() {

    // 件数より大きい場合、エラー
    this.MAXLINE = 4;

    this.showErrorMessage = true;
    this.errorMessage = "";

    /**
     * セルオブジェクト
     * 
     * @param box           セル
     * @param id            セルのID='KOMA_XX_XX'
     * @param week          曜日(連番で保持-月:0 ～ 日:6)
     * @param period        校時(連番で保持-1校時:1 ～)
     * @param dataValList   セルのデータリスト
     *                          // HRクラス情報
     *                          "grade"
     *                          "hrclasscd"
     *                          "hrclassname"
     *                          // 科目情報
     *                          "classcd"
     *                          "school_kind"
     *                          "curriculum_cd"
     *                          "subclasscd"
     *                          "subclassname"
     */
    this.cell = {
        box:null,
        id:null,
        week:null,
        period:null,
        line:null,
        staffcd:null,
        dataValList:null,           //HRクラス・科目の一覧
    }

    /**
     * 対象セルを設定
     * 
     * @param cellBox       セル情報(<td class='targetbox'> を設定する)
     */
    this.setCellObj = function(cellBox) {
        $(window).trigger('setCellObjEvent',[this,cellBox]);
        this.cell.box = cellBox;
        this.cell.id = cellBox.id;
        var idArr = cellBox.id.split("_");
        this.cell.week = idArr[1];
        this.cell.period = idArr[2];
        this.cell.line = idArr[3];
        if ($('td[name=staffLine]')[this.cell.line]) {
            var staff = $('td[name=staffLine]')[this.cell.line];
            this.cell.staffcd = $(staff).attr('data-keyname');
        }

        var cellDataVal = $(cellBox).attr('data-val');
        if (cellDataVal) {
            this.cell.dataValList = JSON.parse(cellDataVal);
        } else {
            this.cell.dataValList = [];
        }
    }

    /**
     * セルへレコードの追加が可能か判定
     * 追加元のレコードを追加が可能か判定
     * 
     * @param srcCell       追加元セル情報
     * @param targetRecord  対象レコード(全対象の場合は設定しない)
     */
    this.checkAppendCell = function(srcCell, targetRecord) {
        if (!targetRecord) { targetRecord = ''; }
        // 追加元セルからレコードを取得し、
        // checkAppendRecord で処理する
        var records = [];
        if (targetRecord) {
            records.push(srcCell.cell.dataValList[targetRecord]);
        } else {
            records = srcCell.cell.dataValList;
        }
        return this.checkAppendRecord(records);
    }

    /**
     * セルへレコードの追加が可能か判定
     * 
     * @param srcCell       追加元セル情報
     */
    this.checkAppendRecord = function(records) {

        // 登録科目の不一致チェック
        if (!this.checkSubClass(records)) {
            if (this.showErrorMessage) {
                alert(this.errorMessage);
            }
            return false;
        }

        // HRクラスの重複チェック
        if (!this.checkHrClassOverlap(records)) {
            if (this.showErrorMessage) {
                alert(this.errorMessage);
            }
            return false;
        }

        // HRクラスの最大件数チェック
        if (!this.checkHrClassMaxCount(records)) {
            if (this.showErrorMessage) {
                alert(this.errorMessage);
            }
            return false;
        }

        return true;
    }

    /**
     * 登録科目の不一致チェック
     * ※同一セルへ複数科目の登録不可
     * 
     * @param records       追加レコードリスト
     */
    this.checkSubClass = function(records) {
        if (!this.cell) {
            return true;
        }
        for(var i = 0; i < this.cell.dataValList.length; i++){
            var element = this.cell.dataValList[i];
            for(var j = 0; j < records.length; j++){
                var record = records[j];
                if (element['classcd'] != record['classcd']
                  || element['school_kind'] != record['school_kind']
                  || element['curriculum_cd'] != record['curriculum_cd']
                  || element['subclasscd'] != record['subclasscd']) {
                    this.errorMessage = "同じ時間に異なる科目は設定できません。";
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * 移動元HRクラスと移動先のHRクラスの重複チェック
     * 
     * @param records       追加レコードリスト
     */
    this.checkHrClassOverlap = function(records) {
        if (!this.cell) {
            return true;
        }
        for(var i = 0; i < this.cell.dataValList.length; i++){
            var element = this.cell.dataValList[i];
            for(var j = 0; j < records.length; j++){
                var record = records[j];
                if (element['grade'] == record['grade'] 
                    && element['hrclasscd'] == record['hrclasscd']) {
                        this.errorMessage = "同じ時間に同じHRクラスは設定できません。";
                        return false;
                }
            }
        }
        return true;
    }

    /**
     * HRクラスの最大件数チェック
     * 
     * @param records       追加レコードリスト
     */
    this.checkHrClassMaxCount = function(records) {
        if (!this.cell) {
            return true;
        }
        var hrClassCnt = this.cell.dataValList.length;

        // 同一クラスはカウントしない
        for(var i = 0; i < records.length; i++){
            var record = records[i];
            var isAny = this.cell.dataValList.some(function(value){
                if (value['grade'] == record['grade']
                && value['hrclasscd'] == record['hrclasscd']) {
                    return true;
                }
                return false;
            });
            if (!isAny) {
                hrClassCnt++;
            }
        }
        if (hrClassCnt > this.MAXLINE) {
            this.errorMessage = "登録可能なHRクラスの件数が最大件数を超えています。";
            return false;
        }
        return true;
    }

    /**
     * セルのレコードをソートする
     * ソート順：grade, hrclasscd
     */
    this.cellSort = function() {
        if (!this.cell) {
            return ;
        }
        this.cell.dataValList.sort(function(a, b){
            // ソートは HRクラス の値を見て判定する
            if (a.grade == b.grade && a.hrclasscd == b.hrclasscd) { return 0; }
            if (a.grade == b.grade && a.hrclasscd > b.hrclasscd) { return 1; }
            if (a.grade > b.grade) { return 1; }
            return -1;
        });
        return;
    }

    /**
     * セルのHTMLへの書き込み
     */
    this.writeCell = function() {
        var lineCnt = 0;
        var dataText = "";
        var hrClassName = "";

        for (var i = 0; i < this.cell.dataValList.length; i++) {
            var element = this.cell.dataValList[i];
            lineCnt++;
            // セルの表示文字成形
            if (lineCnt == 1) {
                hrClassName = element.hrclassname;
            } else if (lineCnt == 2) {
                hrClassName +=  ' *';
            }
            dataText = hrClassName + '<br/>';
            dataText += element.subclassname;
        }
        $(this.cell.box).attr('data-val', JSON.stringify(this.cell.dataValList));
        $(this.cell.box).attr('data-update', '1');
        $(this.cell.box).html(dataText);
    }

    /**
     * セルへレコードの追加
     * 追加元のレコードを追加が可能か判定
     * 
     * @param srcCell       追加元セル情報
     * @param targetRecord  対象レコード(全対象の場合は設定しない)
     */
    this.appendCell = function(srcCell, targetRecord) {
        if (!targetRecord) { targetRecord = ''; }

        // 追加元セルからレコードを取得し、
        // appendRecord で処理する
        var records = [];
        if (targetRecord) {
            records.push(srcCell.cell.dataValList[targetRecord]);
        } else {
            records = srcCell.cell.dataValList;
        }
        return this.appendRecord(records);
    }

    /**
     * セルへレコードの追加
     */
    this.appendRecord = function(records) {
        if (!this.cell) {
            return ;
        }
        // 引き渡されたレコードを追加する
        for (var i = 0; i < records.length; i++) {
            var record = records[i];
            this.cell.dataValList.push(record);
        }
        this.cellSort();
        this.writeCell();
        return;
    }

    /**
     * セルのレコードの削除
     * 全件削除する
     */
    this.deleteCellAll = function() {
        if (!this.cell) {
            return;
        }
        this.cell.dataValList = [];
        this.writeCell();
        return;
    }

    /**
     * セルのレコードの削除
     * 
     * @param targetRecord  対象レコード(全対象の場合は設定しない)
     */
    this.deleteCell = function(targetRecord) {
        if (!targetRecord) { targetRecord = ''; }

        // 追加元セルからレコードを取得し、
        // appendRecord で処理する
        var records = [];
        if (targetRecord) {
            records.push(this.cell.dataValList[targetRecord]);
        } else {
            records = this.cell.dataValList;
        }
        return this.deleteRecord(records);
    }

    /**
     * セルのレコードの削除
     * 引き渡されたレコード一覧と同一のレコードを削除する
     */
    this.deleteRecord = function(records) {
        if (!this.cell) {
            return ;
        }

        var newArray = [];
        for (var i = 0; i < this.cell.dataValList.length; i++) {
            var element = this.cell.dataValList[i];
            var isAny = false;
            for (var j = 0; j < records.length; j++) {
                var record = records[j];
                // レコード同一値判定
                if (element['grade'] == record['grade']
                && element['hrclasscd'] == record['hrclasscd']
                && element['classcd'] == record['classcd']
                && element['school_kind'] == record['school_kind']
                && element['curriculum_cd'] == record['curriculum_cd']) {
                  isAny = true;
                  break;
                }
            }
            if (!isAny) {
                newArray.push(element);
            }
        }
        this.cell.dataValList = newArray;

        this.cellSort();
        this.writeCell();
        return;
    }

}

/**
 * 入れ替え用オブジェクト
 */
function SwapCellObj() {
    // SWAP元セル情報
    this.srcCellObj = new CellObj();
    this.srcTargetRecord = null;
    // SWAP先セル情報
    this.targetCellObj = new CellObj();
    this.targetTargetRecord = null;

    /**
     * 対象セルを設定
     * 
     * @param srcCell       SWAP元セル情報(<td class='targetbox'> を設定する)
     * @param targetCall    SWAP先セル情報(<td class='targetbox'> を設定する)
     */
    this.setSwapCellObj = function(srcCell, targetCall) {
        $(window).trigger('setSwapCellObjEvent',[this, srcCell, targetCall]);

        this.srcCellObj.setSwapSrcCellObj(srcCell);
        this.targetCellObj.setSwapTargetCellObj(targetCall);
    }

    /***
     * SWAP元セルを設定
     * 
     * @param srcCell       SWAP元セル情報
     * @param targetRecord  対象レコード(全対象の場合は設定しない)
     */
    this.setSwapSrcCellObj = function(srcCell, targetRecord) {
        $(window).trigger('setSwapSrcCellObjEvent',[this, srcCell, targetRecord]);

        this.srcCellObj.setCellObj(srcCell);
        this.srcTargetRecord = targetRecord;
    }
    /***
     * SWAP先セルを設定
     * 
     * @param srcCell       SWAP元セル情報
     * @param targetRecord  対象レコード(全対象の場合は設定しない)
     */
    this.setSwapTargetCellObj = function(targetCell, targetRecord) {
        $(window).trigger('setSwapTargetCellObjEvent',[this, targetCell, targetRecord]);

        this.targetCellObj.setCellObj(targetCell);
        this.targetTargetRecord = targetRecord;
    }

    /**
     * 入れ替えチェック
     */
    this.checkSwap = function() {
        if (!this.srcCellObj || !this.targetCellObj) {
            return true;
        }

        // 同じ曜日・校時での入れ替えは不可
        if (this.srcCellObj.cell.week == this.targetCellObj.cell.week &&
            this.srcCellObj.cell.period == this.targetCellObj.cell.period) {
            alert("同じ曜日・校時での入れ替えは行えません。");
            return false;
        }

        // 担当者が同じ場合、入れ替え先セル以外に科目が設定されていない場合は列チェックしない
        if (this.srcCellObj.cell.staffcd == this.targetCellObj.cell.staffcd) {
            var staffCheck = true;
            var staffLineCell = $('td[name=staffLine][data-keyname='+ this.srcCellObj.cell.staffcd +']');
            for (let index = 0; index < staffLineCell.length; index++) {
                const element = staffLineCell[index];
                var line = $(element).attr('data-line');
                if (this.targetCellObj.cell.line == line) {
                    continue;
                }
                var targetId = "#KOMA_" + this.targetCellObj.cell.week + "_" + this.targetCellObj.cell.period + "_" + line;
                var targetCell = $(targetId)[0];
                var cellObj = new CellObj();
                cellObj.setCellObj(targetCell);
                if (cellObj.cell.dataValList.length > 0) {
                    staffCheck = false;
                    break;
                }
            }
            if (staffCheck) {
                return true;
            }
        }

        // 列の追加チェック
        var columnCheck = new checkColumnObj();
        columnCheck.targetWeek = this.targetCellObj.cell.week;
        columnCheck.targetPeriod = this.targetCellObj.cell.period;
        columnCheck.targetStaffCd = this.srcCellObj.cell.staffcd;
        if (!columnCheck.checkMove(this.srcCellObj)) {
            alert(columnCheck.errorMessage);
            return false;
        }

        var columnCheck = new checkColumnObj();
        columnCheck.targetWeek = this.srcCellObj.cell.week;
        columnCheck.targetPeriod = this.srcCellObj.cell.period;
        columnCheck.targetStaffCd = this.targetCellObj.cell.staffcd;
        if (!columnCheck.checkMove(this.targetCellObj)) {
            alert(columnCheck.errorMessage);
            return false;
        }

        return true;
    }

    /**
     * 入れ替え処理
     */
    this.swap = function() {
        if (!this.srcCellObj || !this.targetCellObj) {
            return;
        }

        var targetId = "#KOMA_" + this.targetCellObj.cell.week + "_" + this.targetCellObj.cell.period + "_" + this.srcCellObj.cell.line;
        var targetCell = $(targetId)[0];
        var cellObj = new CellObj();
        cellObj.setCellObj(targetCell);
        // 入れ替え先を保持
        var targetRecords = cellObj.cell.dataValList;
        if (this.srcCellObj.cell.line == this.targetCellObj.cell.line) {
            cellObj.deleteCellAll();
        }
        // データ入れ替え
        cellObj.appendCell(this.srcCellObj);
        this.srcCellObj.deleteCellAll();
        if (this.srcCellObj.cell.line == this.targetCellObj.cell.line) {
            this.srcCellObj.appendRecord(targetRecords);
        }

        // 入替元と入替先の行が異なる場合は入替先行もSWAPする
        if (this.srcCellObj.cell.line != this.targetCellObj.cell.line) {
            // 入れ替え先を保持
            var targetId = "#KOMA_" + this.srcCellObj.cell.week + "_" + this.srcCellObj.cell.period + "_" + this.targetCellObj.cell.line;
            var targetCell = $(targetId)[0];
            var cellObj = new CellObj();
            cellObj.setCellObj(targetCell);
            // 入れ替え先を保持
            var targetRecords = cellObj.cell.dataValList;
            // cellObj.deleteCellAll();
            // データ入れ替え
            cellObj.appendCell(this.targetCellObj);
            this.targetCellObj.deleteCellAll();
            // this.targetCellObj.appendRecord(targetRecords);
        }

    }
}

/**
 * セル追加・移動時の縦列チェック用
 */
function checkColumnObj() {

    this.srcCell = null;

    this.targetWeek = null;
    this.targetPeriod = null;
    this.targetStaffCd = null;

    this.errorMessage = "";

    this.setTarget = function(targetWeek, targetPeriod) {
        this.targetWeek = targetWeek;
        this.targetPeriod = targetPeriod;
    }

    this.checkMove = function(srcCell, targetRecord) {
        if (!targetRecord) { targetRecord = ''; }
        var week = srcCell.cell.week;
        var period = srcCell.cell.period;
        var line = srcCell.cell.line;

        var targetWeek = this.targetWeek;
        var targetPeriod = this.targetPeriod;
        var targetStaffCd = this.targetStaffCd;

        // 移動元セル情報取得
        var srcId = "#KOMA_" + week + "_" + period + "_" + line;
        var srcCell = new CellObj();
        srcCell.setCellObj($(srcId)[0]);

        if (srcCell.cell.dataValList.length > 0) {
            var records = [];
            if (targetRecord) {
                records.push(srcCell.cell.dataValList[targetRecord]);
            } else {
                records = srcCell.cell.dataValList;
            }

            // 担当者の行を取得
            var staffLines = $('td[name=staffLine]');
            for (let staffCnt = 0; staffCnt < staffLines.length; staffCnt++) {
                var element = staffLines[staffCnt];
                var targetLine = $(element).attr('data-line');

                // 移動先セル情報取得
                var targetId = "#KOMA_" + targetWeek + "_" + targetPeriod + "_" + targetLine;
                var targetCell = new CellObj();
                targetCell.showErrorMessage = false;
                targetCell.setCellObj($(targetId)[0]);
                // 移動先行が空の場合は読み飛ばし
                if (targetCell.cell.dataValList.length <= 0) {
                    continue;
                }
                // 移動先担当者と移動先セル担当者が同じ場合は入力チェック
                if (this.targetStaffCd == targetCell.cell.staffcd) {
                    // 担当者が同じ場合は全チェック
                    if (!targetCell.checkAppendRecord(records)) {
                        this.errorMessage = targetCell.errorMessage.replace("設定", "移動");
                        return false;
                    }
                } else {
                    // 科目別基本時間割(職員別)で登録が可能なため、チェックは行わない
                    // // 担当者が異なる場合は「HRクラス」の重複チェック
                    // if (!targetCell.checkHrClassOverlap(records)) {
                    //     this.errorMessage = targetCell.errorMessage.replace("設定", "コピー");
                    //     return false;
                    // }
                }
            }
        }
        return true;
    }

    this.checkCopy = function(srcCell, targetRecord) {
        var week = srcCell.cell.week;
        var period = srcCell.cell.period;
        var line = srcCell.cell.line;

        var targetWeek = this.targetWeek;
        var targetPeriod = this.targetPeriod;

        // 移動元セル情報取得
        var srcId = "#KOMA_" + week + "_" + period + "_" + line;
        var srcCell = new CellObj();
        srcCell.setCellObj($(srcId)[0]);

        if (srcCell.cell.dataValList.length > 0) {
            var records = [];
            if (targetRecord) {
                records.push(srcCell.cell.dataValList[targetRecord]);
            } else {
                records = srcCell.cell.dataValList;
            }
            // 担当者の行を取得
            var staffLines = $('td[name=staffLine]');
            for (let staffCnt = 0; staffCnt < staffLines.length; staffCnt++) {
                var element = staffLines[staffCnt];
                var targetLine = $(element).attr('data-line');

                // 移動先セル情報取得
                var targetId = "#KOMA_" + targetWeek + "_" + targetPeriod + "_" + targetLine;
                var targetCell = new CellObj();
                targetCell.showErrorMessage = false;
                targetCell.setCellObj($(targetId)[0]);
                // 移動先行が空の場合は読み飛ばし
                if (targetCell.cell.dataValList.length <= 0) {
                    continue;
                }
                // 移動先担当者と移動先セル担当者が同じ場合は入力チェック
                if (this.targetStaffCd == targetCell.cell.staffcd) {
                    // 担当者が同じ場合は「科目」チェック
                    if (!targetCell.checkSubClass(records)) {
                        this.errorMessage = targetCell.errorMessage.replace("設定", "コピー");
                        return false;
                    }
                    // 担当者が同じ場合は「HRクラス」の重複チェック
                    if (!targetCell.checkHrClassOverlap(records)) {
                        this.errorMessage = targetCell.errorMessage.replace("設定", "移動");
                        return false;
                    }
                } else {
                    // 科目別基本時間割(職員別)で登録が可能なため、チェックは行わない
                    // // 担当者が異なる場合は「HRクラス」の重複チェック
                    // if (!targetCell.checkHrClassOverlap(records)) {
                    //     this.errorMessage = targetCell.errorMessage.replace("設定", "コピー");
                    //     return false;
                    // }
                }
            }
        }
        return true;
    }

    this.checkRecord = function(records) {

        if (!records) {
            return true;
        }

        var targetWeek = this.targetWeek;
        var targetPeriod = this.targetPeriod;

        if (records.length > 0) {
            var staffCd = records[0]['staffcd'];
            // 担当者の行を取得
            var staffLines = $('td[name=staffLine]');
            for (let staffCnt = 0; staffCnt < staffLines.length; staffCnt++) {
                var element = staffLines[staffCnt];
                var targetLine = $(element).attr('data-line');

                // 移動先セル情報取得
                var targetId = "#KOMA_" + targetWeek + "_" + targetPeriod + "_" + targetLine;
                var targetCell = new CellObj();
                targetCell.showErrorMessage = false;
                targetCell.setCellObj($(targetId)[0]);
                // 移動先行が空の場合は読み飛ばし
                if (targetCell.cell.dataValList.length <= 0) {
                    continue;
                }
                if (staffCd == targetCell.cell.staffcd) {
                    // 担当者が同じ場合は「科目」チェック
                    if (!targetCell.checkSubClass(records)) {
                        this.errorMessage = targetCell.errorMessage.replace("設定", "コピー");
                        return false;
                    }
                    // 担当者が同じ場合は「HRクラス」の重複チェック
                    if (!targetCell.checkHrClassOverlap(records)) {
                        this.errorMessage = targetCell.errorMessage.replace("設定", "移動");
                        return false;
                    }
                } else {
                    // 職員別入力で可能な為、重複チェックはしない
                    // // 担当者が異なる場合は「HRクラス」の重複チェック
                    // if (!targetCell.checkHrClassOverlap(records)) {
                    //     this.errorMessage = targetCell.errorMessage.replace("設定", "コピー");
                    //     return false;
                    // }
                }
            }
        }
        return true;
    }
}

/**
 * 曜日で移動・コピー用
 */
function BlockCellObj() {

    this.srcWeek = null;
    this.srcPeriodList = [];
    this.targetWeek = null;
    this.targetPeriod = null;

    this.maxPeriod = null;
    this.maxLine = null;

    this.setSrc = function(srcWeek, srcPeriodList) {
        this.srcWeek = srcWeek;
        this.srcPeriodList = srcPeriodList;
    }

    this.setTarget = function(targetWeek, targetPeriod) {
        this.targetWeek = targetWeek;
        this.targetPeriod = targetPeriod;
    }

    this.errorMessage = "";
    /**
     * 曜日移動チェック
     */
    this.checkMove = function() {
        // 移動先行チェック
        if (!this.checkMoveLine()) {
            return false;
        }
        // 移動先列チェック
        if (!this.checkMoveColumn()) {
            return false;
        }
        return true;
    }

    // 移動先行のチェック
    this.checkMoveLine = function() {
        var firstPeriod = this.srcPeriodList[0];
        var lastPeriod = this.srcPeriodList[this.srcPeriodList.length - 1];
        // 移動元曜日を移動先の曜日へ
        // 移動元校時を移動先校時へ
        for (var i = 0; i < this.srcPeriodList.length; i++) {
            var week = this.srcWeek;
            var period = this.srcPeriodList[i];

            var targetWeek = this.targetWeek;
            var targetPeriod = parseInt(this.targetPeriod) + i;

            //移動元行を移動先行へ
            for (let line = 0; line < this.maxLine; line++) {
                // 移動元セル情報取得
                var srcId = "#KOMA_" + week + "_" + period + "_" + line;
                var srcCell = new CellObj();
                srcCell.setCellObj($(srcId)[0]);
                // 移動先が移動元の範囲内に入っていた場合、移動先は空なのでチェックしない
                if (week == targetWeek) {
                    if (firstPeriod <= targetPeriod && targetPeriod <= lastPeriod) {
                        continue;
                    }
                }
                // 移動先セル情報取得
                var targetId = "#KOMA_" + targetWeek + "_" + targetPeriod + "_" + line;
                if (!$(targetId)[0]) {
                    continue;
                }
                var targetCell = new CellObj();
                targetCell.showErrorMessage = false;
                targetCell.setCellObj($(targetId)[0]);

                if (!targetCell.checkAppendCell(srcCell)) {
                    this.errorMessage = targetCell.errorMessage.replace("設定", "移動");
                    return false;
                }
            }
        }
        return true;
    }
    // 移動先列のチェック
    this.checkMoveColumn = function() {
        // 移動元曜日を移動先の曜日へ
        // 移動元校時を移動先校時へ
        for (var i = 0; i < this.srcPeriodList.length; i++) {
            var week = this.srcWeek;
            var period = this.srcPeriodList[i];

            var targetWeek = this.targetWeek;
            var targetPeriod = parseInt(this.targetPeriod) + i;

            var columnCheck = new checkColumnObj();
            columnCheck.targetWeek = targetWeek;
            columnCheck.targetPeriod = targetPeriod;

            //移動元行を移動先行へ
            for (let line = 0; line < this.maxLine; line++) {
                // 移動元セル情報取得
                var srcId = "#KOMA_" + week + "_" + period + "_" + line;
                var srcCell = new CellObj();
                srcCell.setCellObj($(srcId)[0]);

                if (srcCell.cell.dataValList.length > 0) {
                    columnCheck.targetStaffCd = srcCell.cell.staffcd;
                    if (!columnCheck.checkMove(srcCell)) {
                        this.errorMessage = columnCheck.errorMessage.replace("設定", "移動");
                        return false;
                    }
                }

            }
        }
        return true;
    }


    this.toMove = function() {
        var srcCellList = [];
        // 移動元曜日を移動先の曜日へ
        // 移動元校時を移動先校時へ
        for (var i = 0; i < this.srcPeriodList.length; i++) {
            var week = this.srcWeek;
            var period = this.srcPeriodList[i];

            //移動元行を移動先行へ
            for (let line = 0; line < this.maxLine; line++) {
                // 移動元セル情報取得
                var srcId = "#KOMA_" + week + "_" + period + "_" + line;
                var srcCell = new CellObj();
                srcCell.setCellObj($(srcId)[0]);

                if (srcCell.cell.dataValList.length > 0) {
                    var tempCell = new CellObj();
                    tempCell.cell.week = srcCell.cell.week;
                    tempCell.cell.period = srcCell.cell.period;
                    tempCell.cell.line = srcCell.cell.line;
                    tempCell.cell.dataValList = srcCell.cell.dataValList;
                    srcCellList.push(tempCell);
                }
                srcCell.deleteCellAll();
            }
        }

        // 移動先へ移動
        for (var i = 0; i < srcCellList.length; i++) {
            var srcCell = srcCellList[i];
            var periodIdx = 0;
            for (; periodIdx < this.srcPeriodList.length; periodIdx++) {
                if (srcCell.cell.period == this.srcPeriodList[periodIdx]) {
                    break;
                }
            }

            // 移動先セル情報取得
            var targetWeek = this.targetWeek;
            var targetPeriod = parseInt(this.targetPeriod) + periodIdx;
            var line = srcCell.cell.line;

            var targetId = "#KOMA_" + targetWeek + "_" + targetPeriod + "_" + line;
            if (!$(targetId)[0]) {
                continue;
            }
            var targetCell = new CellObj();
            targetCell.setCellObj($(targetId)[0]);
            targetCell.appendRecord(srcCell.cell.dataValList);
        }
        return;
    }

    /**
     * 曜日コピーチェック
     */
    this.checkCopy = function() {
        // 移動先行チェック
        if (!this.checkCopyLine()) {
            return false;
        }
        // 移動先列チェック
        if (!this.checkCopyColumn()) {
            return false;
        }
        return true;
    }

    // 移動先行のチェック
    this.checkCopyLine = function() {
        // 移動元曜日を移動先の曜日へ
        // 移動元校時を移動先校時へ
        for (var i = 0; i < this.srcPeriodList.length; i++) {
            var week = this.srcWeek;
            var period = this.srcPeriodList[i];

            var targetWeek = this.targetWeek;
            var targetPeriod = parseInt(this.targetPeriod) + i;

            //移動元行を移動先行へ
            for (let line = 0; line < this.maxLine; line++) {
                // 移動元セル情報取得
                var srcId = "#KOMA_" + week + "_" + period + "_" + line;
                var srcCell = new CellObj();
                srcCell.setCellObj($(srcId)[0]);
                // コピー先セル情報取得
                var targetId = "#KOMA_" + targetWeek + "_" + targetPeriod + "_" + line;
                if (!$(targetId)[0]) {
                    continue;
                }
                var targetCell = new CellObj();
                targetCell.showErrorMessage = false;
                targetCell.setCellObj($(targetId)[0]);

                if (!targetCell.checkAppendCell(srcCell)) {
                    this.errorMessage = targetCell.errorMessage.replace("設定", "コピー");
                    return false;
                }
            }
        }
        return true;
    }
    // 移動先列のチェック
    this.checkCopyColumn = function() {
        // 移動元曜日を移動先の曜日へ
        // 移動元校時を移動先校時へ
        for (var i = 0; i < this.srcPeriodList.length; i++) {
            var week = this.srcWeek;
            var period = this.srcPeriodList[i];

            var targetWeek = this.targetWeek;
            var targetPeriod = parseInt(this.targetPeriod) + i;

            var columnCheck = new checkColumnObj();
            columnCheck.targetWeek = targetWeek;
            columnCheck.targetPeriod = targetPeriod;

            //移動元行を移動先行へ
            for (let line = 0; line < this.maxLine; line++) {
                // 移動元セル情報取得
                var srcId = "#KOMA_" + week + "_" + period + "_" + line;
                var srcCell = new CellObj();
                srcCell.setCellObj($(srcId)[0]);

                if (srcCell.cell.dataValList.length > 0) {
                    columnCheck.targetStaffCd = srcCell.cell.staffcd;
                    if (!columnCheck.checkCopy(srcCell)) {
                        this.errorMessage = columnCheck.errorMessage.replace("設定", "移動");
                        return false;
                    }
                }

            }
        }
        return true;
    }

    this.toCopy = function() {
        var srcCellList = [];
        // 移動元曜日を移動先の曜日へ
        // 移動元校時を移動先校時へ
        for (var i = 0; i < this.srcPeriodList.length; i++) {
            var week = this.srcWeek;
            var period = this.srcPeriodList[i];

            //移動元行を移動先行へ
            for (let line = 0; line < this.maxLine; line++) {
                // 移動元セル情報取得
                var srcId = "#KOMA_" + week + "_" + period + "_" + line;
                var srcCell = new CellObj();
                srcCell.setCellObj($(srcId)[0]);

                if (srcCell.cell.dataValList.length > 0) {
                    var tempCell = new CellObj();
                    tempCell.cell.week = srcCell.cell.week;
                    tempCell.cell.period = srcCell.cell.period;
                    tempCell.cell.line = srcCell.cell.line;
                    tempCell.cell.dataValList = srcCell.cell.dataValList;
                    srcCellList.push(tempCell);
                }

            }
        }

        // 移動先へ追加
        for (var i = 0; i < srcCellList.length; i++) {
            var srcCell = srcCellList[i];
            var periodIdx = 0;
            for (; periodIdx < this.srcPeriodList.length; periodIdx++) {
                if (srcCell.cell.period == this.srcPeriodList[periodIdx]) {
                    break;
                }
            }

            // 移動先セル情報取得
            var targetWeek = this.targetWeek;
            var targetPeriod = parseInt(this.targetPeriod) + periodIdx;
            var line = srcCell.cell.line;

            var targetId = "#KOMA_" + targetWeek + "_" + targetPeriod + "_" + line;
            if (!$(targetId)[0]) {
                continue;
            }
            var targetCell = new CellObj();
            targetCell.setCellObj($(targetId)[0]);
            targetCell.appendRecord(srcCell.cell.dataValList);
        }
        return;
    }

    /**
     * 曜日入れ替えチェック
     */
    this.checkSwap = function() {
        for (var i = 0; i < this.srcPeriodList.length; i++) {
            var targetPeriod = parseInt(this.targetPeriod) + i;
            if (this.srcWeek == this.targetWeek) {
                // 曜日の範囲内での入れ替えは不可
                if (this.srcPeriodList[0] <= targetPeriod 
                    && targetPeriod <= this.srcPeriodList[this.srcPeriodList.length - 1]) {
                        this.errorMessage = "曜日・校時範囲内での入れ替えは行えません。";
                    return false;
                }
            }
        }
        return true;
    }

    this.toSwap = function() {
        // 移動元曜日を移動先の曜日へ
        for (var i = 0; i < this.srcPeriodList.length; i++) {
            var week = this.srcWeek;
            var period = this.srcPeriodList[i];

            var targetWeek = this.targetWeek;
            var targetPeriod = parseInt(this.targetPeriod) + i;

            for (var line = 0; line < this.maxLine; line++) {
                // 移動元セル情報取得
                var srcId = "#KOMA_" + week + "_" + period + "_" + line;
                // 移動先セル情報取得
                var targetId = "#KOMA_" + targetWeek + "_" + targetPeriod + "_" + line;
                if (!$(targetId)[0]) {
                    continue;
                }
                var swapObj = new SwapCellObj();
                swapObj.setSwapSrcCellObj($(srcId)[0]);
                swapObj.setSwapTargetCellObj($(targetId)[0]);
                swapObj.swap();
            }
        }
        return;
    }

    /**
     * 曜日・校時削除チェック
     */
    this.checkDelete = function() {
        return true;
    }

    this.toDelete = function() {
        // 移動元曜日・校時を移動先の曜日へ
        for (var i = 0; i < this.srcPeriodList.length; i++) {
            var week = this.srcWeek;
            var period = this.srcPeriodList[i];

            for (var line = 0; line < this.maxLine; line++) {
                // 移動元セル情報取得
                var srcId = "#KOMA_" + week + "_" + period + "_" + line;
                var srcCell = new CellObj();
                srcCell.setCellObj($(srcId)[0]);
                srcCell.deleteCellAll();
            }
        }
        return;
    }

}

