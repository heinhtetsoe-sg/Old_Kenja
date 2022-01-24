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
     *                          "week"
     *                          "period"
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
        dataValList:null,           //講座IDの一覧
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
            this.errorMessage = "同じ時間に異なる科目は設定できません。";
            if (this.showErrorMessage) {
                alert(this.errorMessage);
            }
            return false;
        }

        // HRクラスの重複チェック
        if (!this.checkHrClassOverlap(records)) {
            this.errorMessage = "同じ時間に同じHRクラスは設定できません。";
            if (this.showErrorMessage) {
                alert(this.errorMessage);
            }
            return false;
        }

        // HRクラスの最大件数チェック
        if (!this.checkHrClassMaxCount(records)) {
            this.errorMessage = "登録可能なHRクラスの件数が最大件数を超えています。";
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
        // 引き渡されたレコードの曜日・校時を変更し追加する
        for (var i = 0; i < records.length; i++) {
            var record = records[i];
            record["week"] = this.cell.week;
            record["period"] =  this.cell.period;
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

        return true;
    }

    /**
     * 入れ替え処理
     */
    this.swap = function() {
        if (!this.srcCellObj || !this.targetCellObj) {
            return;
        }

        // 入れ替え先を保持
        var targetRecords = this.targetCellObj.cell.dataValList;
        this.targetCellObj.deleteCellAll();
        // データ入れ替え
        this.targetCellObj.appendCell(this.srcCellObj);
        this.srcCellObj.deleteCellAll();
        this.srcCellObj.appendRecord(targetRecords);

    }
}

/**
 * 曜日で移動・コピー用
 */
function BlockCellObj() {

    this.srcWeekList = [];
    this.targetWeek = null;
    this.maxPeriod = null;

    this.setSrcWeeks = function(srcWeeks) {
        this.srcWeekList = srcWeeks;
    }

    this.setTargetWeek = function(targetWeek) {
        this.targetWeek = targetWeek;
    }

    this.errorMessage = "";
    /**
     * 曜日移動チェック
     */
    this.checkMove = function() {
        var firstWeek = this.srcWeekList[0];
        var lastWeek = this.srcWeekList[this.srcWeekList.length - 1];
        // 移動元曜日を移動先の曜日へ
        for (var i = 0; i < this.srcWeekList.length; i++) {
            var week = this.srcWeekList[i];
            var targetWeek = parseInt(this.targetWeek) + i;

            for (var period = 1; period <= this.maxPeriod; period++) {
                // 移動元セル情報取得
                var srcId = "#KOMA_" + week + "_" + period;
                var srcCell = new CellObj();
                srcCell.setCellObj($(srcId)[0]);
                // 移動先が移動元の範囲内に入っていた場合、移動先は空なのでチェックしない
                if (firstWeek <= targetWeek && targetWeek <= lastWeek) {
                    continue;
                }
                // 移動先セル情報取得
                var targetId = "#KOMA_" + targetWeek + "_" + period;
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

    this.toMove = function() {
        var srcCellList = [];
        // 移動元曜日を移動先の曜日へ
        for (var i = 0; i < this.srcWeekList.length; i++) {
        // for (var i = (this.srcWeekList.length - 1); i >= 0; i--) {
            var week = this.srcWeekList[i];
            var targetWeek = parseInt(this.targetWeek) + i;

            for (var period = 1; period <= this.maxPeriod; period++) {
                // 移動元セル情報取得
                var srcId = "#KOMA_" + week + "_" + period;
                var srcCell = new CellObj();
                srcCell.setCellObj($(srcId)[0]);
                if (srcCell.cell.dataValList.length > 0) {
                    var tempCell = new CellObj();
                    tempCell.cell.week = srcCell.cell.week;
                    tempCell.cell.period = srcCell.cell.period;
                    tempCell.cell.dataValList = srcCell.cell.dataValList;
                    srcCellList.push(tempCell);
                }
                srcCell.deleteCellAll();
            }
        }

        // 移動先へ移動
        for (var i = 0; i < srcCellList.length; i++) {
            var srcCell = srcCellList[i];
            var weekIdx = 0;
            for (; weekIdx < this.srcWeekList.length; weekIdx++) {
                if (srcCell.cell.week == this.srcWeekList[weekIdx]) {
                    break;
                }
            }
            // 移動先セル情報取得
            var targetWeek = parseInt(this.targetWeek) + weekIdx;
            var targetId = "#KOMA_" + targetWeek + "_" + srcCell.cell.period;
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
        // 移動元曜日を移動先の曜日へ
        for (var i = 0; i < this.srcWeekList.length; i++) {
            var week = this.srcWeekList[i];
            var targetWeek = parseInt(this.targetWeek) + i;

            for (var period = 1; period <= this.maxPeriod; period++) {
                // 移動元セル情報取得
                var srcId = "#KOMA_" + week + "_" + period;
                var srcCell = new CellObj();
                srcCell.setCellObj($(srcId)[0]);
                // 移動先セル情報取得
                var targetId = "#KOMA_" + targetWeek + "_" + period;
                if (!$(targetId)[0]) {
                    continue;
                }
                var targetCell = new CellObj();
                targetCell.setCellObj($(targetId)[0]);
                targetCell.showErrorMessage = false;

                if (!targetCell.checkAppendCell(srcCell)) {
                    // this.errorMessage = targetCell.errorMessage;
                    this.errorMessage = targetCell.errorMessage.replace("設定", "コピー");
                    return false;
                }
            }
        }
        return true;
    }

    this.toCopy = function() {
        var srcCellList = [];
        // 移動元曜日を移動先の曜日へ
        for (var i = 0; i < this.srcWeekList.length; i++) {
        // for (var i = (this.srcWeekList.length - 1); i >= 0; i--) {
            var week = this.srcWeekList[i];
            var targetWeek = parseInt(this.targetWeek) + i;

            for (var period = 1; period <= this.maxPeriod; period++) {
                // 移動元セル情報取得
                var srcId = "#KOMA_" + week + "_" + period;
                var srcCell = new CellObj();
                srcCell.setCellObj($(srcId)[0]);
                if (srcCell.cell.dataValList.length > 0) {
                    var tempCell = new CellObj();
                    tempCell.cell.week = srcCell.cell.week;
                    tempCell.cell.period = srcCell.cell.period;
                    tempCell.cell.dataValList = srcCell.cell.dataValList;
                    srcCellList.push(tempCell);
                }
            }
        }

        // 移動先へ移動
        for (var i = 0; i < srcCellList.length; i++) {
            var srcCell = srcCellList[i];
            var weekIdx = 0;
            for (; weekIdx < this.srcWeekList.length; weekIdx++) {
                if (srcCell.cell.week == this.srcWeekList[weekIdx]) {
                    break;
                }
            }
            // 移動先セル情報取得
            var targetWeek = parseInt(this.targetWeek) + weekIdx;
            var targetId = "#KOMA_" + targetWeek + "_" + srcCell.cell.period;
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
        for (var i = 0; i < this.srcWeekList.length; i++) {
            var targetWeek = parseInt(this.targetWeek) + i;
            // 曜日の範囲内での入れ替えは不可
            if (this.srcWeekList[0] <= targetWeek 
                && targetWeek <= this.srcWeekList[this.srcWeekList.length - 1]) {
                    this.errorMessage = "曜日範囲内での入れ替えは行えません。";
                return false;
            }
        }
        return true;
    }

    this.toSwap = function() {
        // 移動元曜日を移動先の曜日へ
        for (var i = 0; i < this.srcWeekList.length; i++) {
            var week = this.srcWeekList[i];
            var targetWeek = parseInt(this.targetWeek) + i;

            for (var period = 1; period <= this.maxPeriod; period++) {
                // 移動元セル情報取得
                var srcId = "#KOMA_" + week + "_" + period;
                // 移動先セル情報取得
                var targetId = "#KOMA_" + targetWeek + "_" + period;
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
     * 曜日削除チェック
     */
    this.checkDelete = function() {
        return true;
    }

    this.toDelete = function() {
        // 移動元曜日を移動先の曜日へ
        for (var i = 0; i < this.srcWeekList.length; i++) {
            var week = this.srcWeekList[i];

            for (var period = 1; period <= this.maxPeriod; period++) {
                // 移動元セル情報取得
                var srcId = "#KOMA_" + week + "_" + period;
                var srcCell = new CellObj();
                srcCell.setCellObj($(srcId)[0]);
                srcCell.deleteCellAll();
            }
        }
        return;
    }

}

