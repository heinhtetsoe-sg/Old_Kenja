<?php

require_once('for_php7.php');

class knjb3045Query extends Query {

    // 年度学期取得
    function getYearSemester($model) {

        $query  = " SELECT ";
        $query .= "     SEME.YEAR || '-' || SEME.SEMESTER AS VALUE, ";
        $query .= "     SEME.YEAR || '年度 ' || SEME.SEMESTERNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST SEME ";
        $query .= " WHERE ";
        $query .= "     SEME.YEAR IN ('".CTRL_YEAR."', '".(CTRL_YEAR + 1)."') ";
        $query .= "     AND SEMESTER <> '9' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    // 講座展開表パターン
    function getSchPtrnPreChrHdat($model) {
        $query  = " SELECT ";
        $query .= "     PRESEQ, ";
        $query .= "     TITLE, ";
        $query .= "     UPDATED ";
        $query .= " FROM ";
        $query .= "     SCH_PTRN_PRE_CHR_HDAT ";
        $query .= " WHERE YEAR = '{$model->year}' ";
        $query .= "   AND SEMESTER = '{$model->semester}' ";
        $query .= " ORDER BY ";
        $query .= "     PRESEQ ";

        return $query;
    }

    // 基本時間割パターン
    function getSchPtrnHdat($model) {
        $query  = " SELECT ";
        $query .= "     BSCSEQ, ";
        $query .= "     TITLE, ";
        $query .= "     UPDATED ";
        $query .= " FROM ";
        $query .= "     SCH_PTRN_HDAT ";
        $query .= " WHERE YEAR = '{$model->year}' ";
        $query .= "   AND SEMESTER = '{$model->semester}' ";
        $query .= " ORDER BY ";
        $query .= "     BSCSEQ ";

        return $query;
    }

    // 曜日
    function getDefaultWeek($model) {
        $query  = " SELECT ";
        $query .= "     B004.NAMECD2 AS VALUE, ";
        $query .= "     B004.NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST B004 ";
        $query .= " WHERE ";
        $query .= "     B004.YEAR = '{$model->year}' ";
        $query .= "     AND B004.NAMECD1 = 'B004' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    // 校時
    function getPeriod($model) {
        $query  = " SELECT ";
        $query .= "     B001.NAMECD2 AS VALUE, ";
        $query .= "     B001.NAME1 AS LABEL, ";
        $query .= "     B003.NAMECD2 AS UNATTACH ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST B001 ";
        $query .= " LEFT JOIN V_NAME_MST B003 ";
        $query .= "      ON B003.YEAR = '{$model->year}' ";
        $query .= "     AND B003.NAMECD1 = 'B003' ";
        $query .= "     AND B003.NAMECD2 = B001.NAMECD2 ";

        $query .= " WHERE ";
        $query .= "     B001.YEAR = '{$model->year}' ";
        $query .= "     AND B001.NAMECD1 = 'B001' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    // 単位マスタ コース毎の最大単位数
    function getCreditsMax($model) {
        $query  = "";
        $query .= " WITH CREDITS AS ( ";
        $query .= "   SELECT ";
        $query .= "     T1.COURSECD ";
        $query .= "     , T1.MAJORCD ";
        $query .= "     , T1.GRADE ";
        $query .= "     , T1.COURSECODE ";
        $query .= "     , SUM(T1.CREDITS) CREDITS_SUM ";
        $query .= "   FROM ";
        $query .= "     CREDIT_MST T1 ";
        $query .= "   WHERE ";
        $query .= "     T1.YEAR = '".$model->year."' ";
        $query .= "   GROUP BY ";
        $query .= "     T1.COURSECD ";
        $query .= "     , T1.MAJORCD ";
        $query .= "     , T1.GRADE ";
        $query .= "     , T1.COURSECODE ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "   MAX(CREDITS_SUM) CREDITS_MAX ";
        $query .= " FROM ";
        $query .= "   CREDITS ";

        return $query;
    }

    // 基本時間割の講座カウント
    function getSchPtrnChrCnt($model) {
        $query  = " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     SCH_PTRN_DAT ";
        $query .= " WHERE YEAR = '{$model->year}' ";
        $query .= "   AND SEMESTER = '{$model->semester}' ";
        $query .= "   AND BSCSEQ = '{$model->field['BSCSEQ']}' ";

        return $query;
    }

    // 展開表講座-基本時間割割り当てテーブル一覧読込
    function selectSchPtrnPreChaToBasicDat($model) {
        $query  = "";
        $query .= " SELECT T1.YEAR ";
        $query .= " , T1.SEMESTER ";
        $query .= " , T1.BSCSEQ ";
        $query .= " , MAX(T1.UPDATED) UPDATED ";
        $query .= " , T2.TITLE ";

        $query .= " FROM SCH_PTRN_PRE_CHR_TO_BASIC_DAT T1 ";
        $query .= " INNER JOIN SCH_PTRN_HDAT T2 ";
        $query .= "   ON T1.YEAR = T2.YEAR ";
        $query .= "   AND T1.SEMESTER = T2.SEMESTER ";
        $query .= "   AND  T1.BSCSEQ = T2.BSCSEQ ";
        $query .= " WHERE T1.YEAR = '{$model->year}' ";
        $query .= "   AND T1.SEMESTER = '{$model->semester}' ";
        $query .= " GROUP BY ";
        $query .= "   T1.YEAR ";
        $query .= " , T1.SEMESTER ";
        $query .= " , T1.BSCSEQ ";
        $query .= " , T2.TITLE ";

        return $query;
    }
    // 展開表講座-基本時間割割り当てテーブル 読込
    function getSchPtrnPreChaToBasicDat($model, $bscSeq) {

        $query  = "";
        $query .= " SELECT * ";
        $query .= " FROM SCH_PTRN_PRE_CHR_TO_BASIC_DAT ";
        $query .= " WHERE YEAR = '{$model->year}' ";
        $query .= "   AND SEMESTER = '{$model->semester}' ";
        if ($bscSeq) {
            $query .= "   AND BSCSEQ = {$bscSeq} ";
        }

        return $query;
    }

    // 基本時間割の削除→登録
    function update($db, $model) {

        // 登録用データ
        $preSeqData = array();
        if ($model->field['PRESEQDATA']) {
            $preSeqData = json_decode($model->field['PRESEQDATA'], true);
        }

        // 講座展開表取得
        $preChairList = array();
        $query = knjb3045Query::selectSchPtrnPreChrDat($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $preChairList[$row['PRE_ORDER']][] = $row['CHAIRCD'];
        }

        // SEQ取得(新規作成の場合は 最大SEQ+1)
        $bscSeq = knjb3045Query::getBscSeq($db, $model);

        $model->field['BSCSEQ'] = sprintf('%02d', $bscSeq);
        // 基本時間割ヘッダ 削除
        $db->query(knjb3045Query::deleteSchPtrnHDat($model, $bscSeq));
        // 基本時間割ヘッダ 登録
        $data = array();
        $data["YEAR"][TEXT]           = $model->year;
        $data["SEMESTER"][TEXT]       = $model->semester;
        $data["BSCSEQ"][TEXT]         = $bscSeq;
        $data["TITLE"][TEXT]          = $model->field['BSCTITLE'];
        $data["REGISTERCD"][TEXT]     = STAFFCD;
        $data["UPDATED"][FUNC]        = "sysdate()";
        $query = Query::insertSQL($data, "SCH_PTRN_HDAT");
        $db->query($query);

        // 基本時間割 削除
        $db->query(knjb3045Query::deleteSchPtrnDat($model, $bscSeq));
        // 基本時間割(施設) 削除
        $db->query(knjb3045Query::deleteSchPtrnFacDat($model, $bscSeq));
        // 基本時間割(集計フラグ) 削除
        $db->query(knjb3045Query::deleteSchPtrnCountFlgDat($model, $bscSeq));

        // 展開表講座-基本時間割割り当てテーブル 削除
        $db->query(knjb3045Query::deleteSchPtrnPreChaToBasicDat($model, $bscSeq));

        foreach ($preSeqData as $key => $value) {
            list($week, $period) = explode('_', $key);
            if ($week == 'NONE') {
                continue;
            }
            // 講座展開表の SEQ 0開始なので、 SEQ を -1 する
            if ($value) {
                $value = $value - 1;
            }

            // 展開表講座-基本時間割割り当てテーブル 登録
            $data = array();
            $data["YEAR"][TEXT]           = $model->year;
            $data["SEMESTER"][TEXT]       = $model->semester;
            $data["BSCSEQ"][TEXT]         = $bscSeq;
            $data["DAYCD"][TEXT]          = $week;

            $data["PERIODCD"][TEXT]       = $period;
            $data["PRESEQ"][TEXT]         = $model->field['PRESEQ'];
            $data["PRE_ORDER"][TEXT]      = $value;

            $data["REGISTERCD"][TEXT]     = STAFFCD;
            $data["UPDATED"][FUNC]        = "sysdate()";
            $query = Query::insertSQL($data, "SCH_PTRN_PRE_CHR_TO_BASIC_DAT");
            $db->query($query);

            if (strlen($value) > 0) {

                if (isset($preChairList[$value])) {
                    for ($i=0; $i < get_count($preChairList[$value]); $i++) { 
                        $chairCd = $preChairList[$value][$i];

                        // 基本時間割登録
                        $data = array();
                        $data["YEAR"][TEXT]           = $model->year;
                        $data["SEMESTER"][TEXT]       = $model->semester;

                        $data["BSCSEQ"][TEXT]         = $bscSeq;
                        $data["DAYCD"][TEXT]          = $week;
                        $data["PERIODCD"][TEXT]       = $period;
                        $data["CHAIRCD"][TEXT]        = $chairCd;

                        $data["REGISTERCD"][TEXT]     = STAFFCD;
                        $data["UPDATED"][FUNC]        = "sysdate()";

                        $query = Query::insertSQL($data, "SCH_PTRN_DAT");
                        $db->query($query);
                    }
                }
            }
        }
        return true;
    }

    // 基本時間割 SEQ取得(新規作成の場合は 最大SEQ+1)
    function getBscSeq($db, $model) {
        if ($model->field["BSCSEQ"]) {
            return $model->field["BSCSEQ"];
        } else {
            $query  = " SELECT MAX(BSCSEQ) AS BSCSEQ ";
            $query .= " FROM SCH_PTRN_HDAT ";
            $query .= " WHERE YEAR = '{$model->year}' ";
            $query .= "   AND SEMESTER = '{$model->semester}' ";

            $maxBscSeq = $db->getOne($query);
            $maxBscSeq = $maxBscSeq > 0 ? $maxBscSeq + 1 : 1;
            return $maxBscSeq;
        }
    }

    // 基本時間割ヘッダ 削除
    function deleteSchPtrnHDat($model, $bscSeq) {

        $query  = "";
        $query .= " DELETE FROM SCH_PTRN_HDAT ";
        $query .= " WHERE YEAR = '{$model->year}' ";
        $query .= "   AND SEMESTER = '{$model->semester}' ";
        $query .= "   AND BSCSEQ = {$bscSeq} ";

        return $query;
    }
    // 基本時間割 削除
    function deleteSchPtrnDat($model, $bscSeq) {

        $query  = "";
        $query .= " DELETE FROM SCH_PTRN_DAT ";
        $query .= " WHERE YEAR = '{$model->year}' ";
        $query .= "   AND SEMESTER = '{$model->semester}' ";
        $query .= "   AND BSCSEQ = {$bscSeq} ";

        return $query;
    }
    // 基本時間割(施設) 削除
    function deleteSchPtrnFacDat($model, $bscSeq) {

        $query  = "";
        $query .= " DELETE FROM SCH_PTRN_FAC_DAT ";
        $query .= " WHERE YEAR = '{$model->year}' ";
        $query .= "   AND SEMESTER = '{$model->semester}' ";
        $query .= "   AND BSCSEQ = {$bscSeq} ";

        return $query;
    }
    // 基本時間割(集計フラグ) 削除
    function deleteSchPtrnCountFlgDat($model, $bscSeq) {

        $query  = "";
        $query .= " DELETE FROM SCH_PTRN_COUNTFLG_DAT ";
        $query .= " WHERE YEAR = '{$model->year}' ";
        $query .= "   AND SEMESTER = '{$model->semester}' ";
        $query .= "   AND BSCSEQ = {$bscSeq} ";

        return $query;
    }
    // 展開表講座-基本時間割割り当てテーブル 削除
    function deleteSchPtrnPreChaToBasicDat($model, $bscSeq) {

        $query  = "";
        $query .= " DELETE FROM SCH_PTRN_PRE_CHR_TO_BASIC_DAT ";
        $query .= " WHERE YEAR = '{$model->year}' ";
        $query .= "   AND SEMESTER = '{$model->semester}' ";
        $query .= "   AND BSCSEQ = {$bscSeq} ";

        return $query;
    }

    // 講座展開表 取得
    function selectSchPtrnPreChrDat($model) {

        $query  = "";
        $query .= " SELECT DISTINCT ";
        $query .= "   PRE_ORDER ";
        $query .= " , CHAIRCD ";
        $query .= " FROM SCH_PTRN_PRE_CHR_DAT ";
        $query .= " WHERE YEAR = '{$model->year}' ";
        $query .= "   AND SEMESTER = '{$model->semester}' ";
        $query .= "   AND PRESEQ = {$model->field["PRESEQ"]} ";

        return $query;
    }

}
?>
