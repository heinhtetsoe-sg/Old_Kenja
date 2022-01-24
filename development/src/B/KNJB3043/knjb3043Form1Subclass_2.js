
function baseTdtag(id){
    return '<td id="'+id+'" data-text="" data-val="" data-def="" data-test="" data-exec="" data-linking="" data-zyukou="" data-selectfacility="" data-count-lesson="" data-dirty=""></td>';
}







function CourseObj() {

    this.COLNUM_MAX = 0;
    this.srcCourseKey = "";

    /**
     * セルオブジェクト(科目情報)
     * 
     * @param id            セルのID='KOMA_XX_XX'
     * @param seq           シーケンス番号
     * @param dataValList   セルのデータリスト
     *                          // 科目情報
     *                          "classcd"
     *                          "school_kind"
     *                          "curriculum_cd"
     *                          "subclasscd"
     *                          "subclassname"
     */
    this.srcSubclass = {
        id:null,
        seq:null,
        dataValList:null,           // 科目の一覧
    }

    this.subclassList = [];

    // 初期化。コースの情報を取得
    //   コースのキーを取得し、コースに設定されている科目の一覧まで取得
    this.setCourseObj = function(srcCoursekey){

        this.srcCourseKey = srcCoursekey;

        // コースキーを元に科目情報を取得
        var lineArray = $('.targetbox[line-key='+ this.srcCourseKey +']');
        // 列の最大
        this.COLNUM_MAX = lineArray.length;

        for (var i = 0; i < lineArray.length; i++) {
            var subclass = lineArray[i];
            if ($(subclass).attr('data-val') != '') {
                var cellInfo = {};
                cellInfo['id'] = $(subclass).attr('id');
                cellInfo['seq'] = i;
                cellInfo['dataValList'] = JSON.parse($(subclass).attr('data-val'));

                this.subclassList.push(cellInfo);
            }
        }

        return;
    }


    /**
     * セル(科目情報)の移動
     */
    this.moveSubclass = function (srcCell, targetCell) {

        // 異動元の科目を取得
        var srcId = $(srcCell).attr('id');
        var srcSubclass = JSON.parse($(srcCell).attr('data-val'));
        var srcSubclassList = [];
        for (var i = 0; i < this.subclassList.length; i++) {
            var cellInfo = this.subclassList[i];
            if (srcSubclass['classcd'] == cellInfo['dataValList']['classcd']
            && srcSubclass['school_kind'] == cellInfo['dataValList']['school_kind']
            && srcSubclass['curriculum_cd'] == cellInfo['dataValList']['curriculum_cd']
            && srcSubclass['subclasscd'] == cellInfo['dataValList']['subclasscd']) {
                srcSubclassList.push(cellInfo);
            }
        }

        // 移動先の科目を取得
        var targetSubclass = '';
        if ($(targetCell).attr('data-val') != '') {
            targetSubclass = JSON.parse($(targetCell).attr('data-val'));
        }

        // ID名生成( KOMA_XX_XX )
        var idArray = $(srcCell).attr('id').split('_');
        var lineNo = idArray[1];

        var seqCnt = 0;
        var isSrcInsFlg = false;
        var sortSubclassList = [];
        for (var i = 0; i < this.subclassList.length; i++) {
            var cellInfo = this.subclassList[i];

            var isSrc = (srcSubclass['classcd'] == cellInfo['dataValList']['classcd'] 
                        && srcSubclass['school_kind'] == cellInfo['dataValList']['school_kind']
                        && srcSubclass['curriculum_cd'] == cellInfo['dataValList']['curriculum_cd']
                        && srcSubclass['subclasscd'] == cellInfo['dataValList']['subclasscd']);

            if (isSrc) {
                // 異動元と同じ科目情報の場合はなにもしない
            } else {
                var isTarget = (targetSubclass['classcd'] == cellInfo['dataValList']['classcd'] 
                            && targetSubclass['school_kind'] == cellInfo['dataValList']['school_kind']
                            && targetSubclass['curriculum_cd'] == cellInfo['dataValList']['curriculum_cd']
                            && targetSubclass['subclasscd'] == cellInfo['dataValList']['subclasscd']);

                if (isTarget) {
                    // 異動先と同じ科目情報の場合、異動元の科目情報を追加
                    if (!isSrcInsFlg) {
                        for (let j = 0; j < srcSubclassList.length; j++) {
                            var element = srcSubclassList[j];
                            var cellId = "KOMA_" + lineNo + "_" + seqCnt;
                            element['id'] = cellId;
                            element['seq'] = seqCnt;

                            sortSubclassList.push(element);
                            seqCnt++;
                        }
                    }
                    isSrcInsFlg = true;
                }

                var cellId = "KOMA_" + lineNo + "_" + seqCnt;
                var cell = {};
                cell['id'] = cellId;
                cell['seq'] = seqCnt;
                cell['dataValList'] = cellInfo['dataValList'];

                // 科目情報を追加
                sortSubclassList.push(cell);
                seqCnt++;
            }
        }

        // 科目情報が取得できなかった場合は最後へ追加
        if (targetSubclass == '') {
            for (let j = 0; j < srcSubclassList.length; j++) {
                var cell = srcSubclassList[j];
                var cellId = "KOMA_" + lineNo + "_" + seqCnt;

                cell['id'] = cellId;
                cell['seq'] = seqCnt;

                sortSubclassList.push(cell);
                seqCnt++;
            }
        }
        this.subclassList = sortSubclassList;

        this.writeSubclass();
        return;
    }


    /**
     * 科目情報の追加
     */
    this.addSubclass = function(addSubclassList, targetCell) {


        // 追加する科目情報
        if (!addSubclassList) {
            return;
        }

        // 選択された科目情報を単位数分作成する
        var srcSubclassList = [];
        for (let i = 0; i < addSubclassList.length; i++) {
            const element = addSubclassList[i];
            var subclass = JSON.parse(element);
            for (let j = 0; j < subclass['credits']; j++) {
                srcSubclassList.push(subclass);
            }
        }


        // 移動先の科目を取得
        var targetSubclass = '';
        if ($(targetCell).attr('data-val') != '') {
            targetSubclass = JSON.parse($(targetCell).attr('data-val'));
        }

        // ID名生成( KOMA_XX_XX )
        var idArray = $(targetCell).attr('id').split('_');
        var lineNo = idArray[1];

        var seqCnt = 0;
        var isSrcInsFlg = false;
        var sortSubclassList = [];
        for (var i = 0; i < this.subclassList.length; i++) {
            var cellInfo = this.subclassList[i];

            var isTarget = (targetSubclass['classcd'] == cellInfo['dataValList']['classcd'] 
                        && targetSubclass['school_kind'] == cellInfo['dataValList']['school_kind']
                        && targetSubclass['curriculum_cd'] == cellInfo['dataValList']['curriculum_cd']
                        && targetSubclass['subclasscd'] == cellInfo['dataValList']['subclasscd']);

            if (isTarget) {
                // 異動先と同じ科目情報の場合、異動元の科目情報を追加
                if (!isSrcInsFlg) {
                    for (let j = 0; j < srcSubclassList.length; j++) {
                        var element = srcSubclassList[j];

                        var cell = {};
                        var cellId = "KOMA_" + lineNo + "_" + seqCnt;
                        cell['id'] = cellId;
                        cell['seq'] = seqCnt;
                        cell['dataValList'] = element;

                        sortSubclassList.push(cell);
                        seqCnt++;
                    }
                }
                isSrcInsFlg = true;
            }

            var cellId = "KOMA_" + lineNo + "_" + seqCnt;
            var cell = {};
            cell['id'] = cellId;
            cell['seq'] = seqCnt;
            cell['dataValList'] = cellInfo['dataValList'];

            // 科目情報を追加
            sortSubclassList.push(cell);
            seqCnt++;
        }

        // 科目情報が取得できなかった場合は最後へ追加
        if (targetSubclass == '') {
            for (let j = 0; j < srcSubclassList.length; j++) {
                var element = srcSubclassList[j];

                var cell = {};
                var cellId = "KOMA_" + lineNo + "_" + seqCnt;
                cell['id'] = cellId;
                cell['seq'] = seqCnt;
                cell['dataValList'] = element;

                sortSubclassList.push(cell);
                seqCnt++;
            }
        }
        this.subclassList = sortSubclassList;


        this.writeSubclass();
        return;
    }

    /**
     * 科目情報の削除
     */
    this.deleteSubclass = function(targetCell) {

        var targetSubclass = '';
        if ($(targetCell).attr('data-val') == '') {
            // 削除先の科目がない場合は処理なし
            return;
        }
        targetSubclass = JSON.parse($(targetCell).attr('data-val'));

        // ID名生成( KOMA_XX_XX )
        var idArray = $(targetCell).attr('id').split('_');
        var lineNo = idArray[1];
        var seqCnt = 0;

        // 新科目情報リスト
        var newSubclassList = [];

        for (var i = 0; i < this.subclassList.length; i++) {
            var cellInfo = this.subclassList[i];

            var isTarget = (targetSubclass['classcd'] == cellInfo['dataValList']['classcd'] 
                        && targetSubclass['school_kind'] == cellInfo['dataValList']['school_kind']
                        && targetSubclass['curriculum_cd'] == cellInfo['dataValList']['curriculum_cd']
                        && targetSubclass['subclasscd'] == cellInfo['dataValList']['subclasscd']);

            // ターゲットと一致した場合は科目情報へ追加しない
            if (!isTarget) {
                var cellId = "KOMA_" + lineNo + "_" + seqCnt;
                var cell = {};
                cell['id'] = cellId;
                cell['seq'] = seqCnt;
                cell['dataValList'] = cellInfo['dataValList'];

                // 科目情報を追加
                newSubclassList.push(cell);
                seqCnt++;
            }
        }

        for (var i = 0; i < this.subclassList.length; i++) {
            var cellInfo = this.subclassList[i];

            cellInfo['dataValList'] = '';
        }
        this.writeSubclass();


        this.subclassList = newSubclassList;

        this.writeSubclass();


        return;
    }

    /**
     * セルへのHTML書き込み
     */
    this.writeSubclass = function() {

        for (let index = 0; index < this.subclassList.length; index++) {
            var cellInfo = this.subclassList[index];
            var subclass = cellInfo['dataValList']
            // 画面表示
            dataText = '';
            $('#'+cellInfo['id']).attr('data-val', '');
            if (subclass) {
                dataText += subclass['classcd'] + "-" + subclass['school_kind'] + "-" + subclass['curriculum_cd'] + "-" + subclass['subclasscd'] + "<br/>";
                dataText += subclass['subclassname'];

                $('#'+cellInfo['id']).attr('data-val', JSON.stringify(subclass));
                $('#'+cellInfo['id']).attr('data-update', '1');
            }
            $('#'+cellInfo['id']).html(dataText);
        }
        return;
    }


}












