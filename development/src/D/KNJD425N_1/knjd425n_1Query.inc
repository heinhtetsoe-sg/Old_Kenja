<?php

require_once('for_php7.php');

class knjd425n_1Query extends Query {

    //タイトル取得
    function getHreportGuidanceKindNameHdat($model) {
        $query  = " SELECT ";
        $query .= "     KIND_NAME ";
        $query .= " FROM ";
        $query .= "     HREPORT_GUIDANCE_KIND_NAME_HDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$model->exp_year}' ";
        $query .= "     AND KIND_NO = '{$model->selKindNo}' ";

        return $query;
    }

    //「自立活動」区分のデータを取得
    function getZirituKatudouList($model) {
        $query  = " SELECT ";
        $query .= "     DAT.SELF_TARGET ";
        $query .= "     , DAT.SELF_DIV ";
        $query .= "     , MST.SELF_TITLE ";
        $query .= "     , MST.SELF_ITEM ";
        $query .= "     , MST.SELF_CONTENT ";
        $query .= "     , ST_CNT.CNT AS SELF_TARGET_ROWSPAN ";
        $query .= " FROM ";
        $query .= "     HREPORT_GUIDANCE_SCHREG_SELFRELIANCE_DAT DAT ";
        $query .= "     INNER JOIN HREPORT_GUIDANCE_SELFRELIANCE_MST MST ";
        $query .= "         ON MST.YEAR = DAT.YEAR ";
        $query .= "         AND MST.SELF_DIV = DAT.SELF_DIV ";
        $query .= "         AND MST.SELF_SEQ = DAT.SELF_SEQ ";
        $query .= "     INNER JOIN ( ";
        $query .= "         SELECT ";
        $query .= "             COUNT(SELF_TARGET) AS CNT ";
        $query .= "             , SELF_TARGET ";
        $query .= "         FROM ";
        $query .= "             HREPORT_GUIDANCE_SCHREG_SELFRELIANCE_DAT ";
        $query .= "         WHERE ";
        $query .= "             YEAR = '{$model->exp_year}' ";
        $query .= "             AND SCHREGNO = '{$model->schregno}' ";
        $query .= "         GROUP BY ";
        $query .= "             SELF_TARGET ";
        $query .= "     ) ST_CNT ";
        $query .= "         ON ST_CNT.SELF_TARGET = DAT.SELF_TARGET ";
        $query .= " WHERE ";
        $query .= "     DAT.YEAR = '{$model->exp_year}' ";
        $query .= "     AND DAT.SCHREGNO = '{$model->schregno}' ";
        $query .= " ORDER BY ";
        $query .= "     DAT.SELF_TARGET ";
        $query .= "     , DAT.SELF_DIV ";

        return $query;
    }

    //各テーブルの項目名取得
    function getHreportGuidanceKindNameDat($year, $kindNo) {
        $query  = " SELECT ";
        $query .= "     KIND_SEQ ";
        $query .= "     , KIND_REMARK ";
        $query .= " FROM ";
        $query .= "     HREPORT_GUIDANCE_KIND_NAME_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$year}' ";
        $query .= "     AND KIND_NO = '{$kindNo}' ";

        return $query;
    }

    //データ取得
    function getHreportGuidanceSchregRemarkDat($model) {
        $dstr = str_replace("/", "-", $model->upddate);

        $query  = " SELECT ";
        $query .= "     SEQ ";
        $query .= "     , REMARK ";
        $query .= " FROM ";
        $query .= "     HREPORT_GUIDANCE_SCHREG_REMARK_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$model->exp_year}' ";
        $query .= "     AND SEMESTER = '9' ";
        $query .= "     AND RECORD_DATE = '{$dstr}' ";
        $query .= "     AND SCHREGNO = '{$model->schregno}' ";
        $query .= "     AND DIV = '{$model->selKindNo}' ";
        $query .= " UNION ";
        $query .= " SELECT ";
        $query .= "     SEQ ";
        $query .= "     , REMARK ";
        $query .= " FROM ";
        $query .= "     HREPORT_GUIDANCE_SCHREG_REMARK_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$model->exp_year}' ";
        $query .= "     AND SEMESTER = '{$model->field["SEMESTER"]}' ";
        $query .= "     AND RECORD_DATE = '{$dstr}' ";
        $query .= "     AND SCHREGNO = '{$model->schregno}' ";
        $query .= "     AND DIV = '{$model->selKindNo}' ";

        return $query;
    }

    //学期取得
    function getSemester($model) {
        $query  = " SELECT ";
        $query .= "     SEMESTER AS VALUE, ";
        $query .= "     SEMESTERNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$model->exp_year}' ";
        $query .= "     AND SEMESTER <> '9' ";
        $query .= " ORDER BY ";
        $query .= "     SEMESTER ";

        return $query;
    }

    //指導内容、支援、評価の一覧用
    function getRemarkList($model) {
        $dstr = str_replace("/", "-", $model->upddate);
        if ($model->sort["SEMESTER"] == 1) {
            $order = "REM.SEMESTER";
        } else {
            $order = "REM.SEMESTER DESC";
        }

        $query  = " SELECT ";
        $query .= "     REM.REMARK ";
        $query .= "     , REM.SEMESTER ";
        $query .= "     , REM.SEQ ";
        $query .= "     , SEM.SEMESTERNAME ";
        $query .= " FROM ";
        $query .= "     HREPORT_GUIDANCE_SCHREG_REMARK_DAT REM ";
        $query .= "     LEFT JOIN SEMESTER_MST SEM ";
        $query .= "         ON SEM.YEAR = REM.YEAR ";
        $query .= "         AND SEM.SEMESTER = REM.SEMESTER ";
        $query .= " WHERE ";
        $query .= "     REM.YEAR = '{$model->exp_year}' ";
        $query .= "     AND REM.RECORD_DATE = '{$dstr}' ";
        $query .= "     AND REM.SCHREGNO = '{$model->schregno}' ";
        $query .= "     AND REM.DIV = '{$model->selKindNo}' ";
        $query .= "     AND REM.SEQ IN (3, 4, 5) ";
        $query .= " ORDER BY ";
        $query .= $order ;
        $query .= "     , REM.SEQ ";

        return $query;
    }

    /************/
    /* 更新処理 */
    /************/
    //INSERT -- HREPORT_GUIDANCE_SCHREG_SUBCLASS_DAT
    function &getInsertQuery($model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $semester = in_array("3", $model->updateSeq) ? $model->field["SEMESTER"] : '9';
        $dstr = str_replace("/", "-", $model->upddate);

        foreach($model->updateSeq as $seq) {
            $data = array();
            $data["REMARK"][TEXT]           = $model->field["REMARK-{$seq}"];
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][NUMBER]        = "sysdate()";
            //存在チェック
            if (0 == $db->getOne(knjd425n_1Query::getHreportGuidanceSchregRemarkDatCnt($model, $semester, $seq))) {
                //登録
                $data["YEAR"][TEXT]             = $model->exp_year;
                $data["SEMESTER"][TEXT]         = $semester;
                $data["RECORD_DATE"][DATE]      = $dstr;
                $data["SCHREGNO"][TEXT]         = $model->schregno;
                $data["DIV"][TEXT]              = $model->selKindNo;
                $data["SEQ"][NUMBER]            = $seq;

                $query = Query::insertSQL($data, "HREPORT_GUIDANCE_SCHREG_REMARK_DAT");
                $db->query($query);

            } else {
                //更新
                $where  = " WHERE ";
                $where .= "         YEAR         = '{$model->exp_year}' ";
                $where .= "     AND SEMESTER     = '{$semester}' ";
                $where .= "     AND RECORD_DATE  = '{$dstr}' ";
                $where .= "     AND SCHREGNO     = '{$model->schregno}' ";
                $where .= "     AND DIV          = '{$model->selKindNo}' ";
                $where .= "     AND SEQ          = {$seq} ";

                $query = Query::updateSQL($data, "HREPORT_GUIDANCE_SCHREG_REMARK_DAT", $where);
                $db->query($query);

            }
        }

        $db->commit();
        Query::dbCheckIn($db);
        return;
    }

   //DELETE -- HREPORT_GUIDANCE_SCHREG_REMARK_DAT
   function &getDeleteQuery($model) {

       $db = Query::dbCheckOut();

       $semester = in_array("3", $model->deleteSeq) ? $model->field["SEMESTER"] : '9';
       $dstr = str_replace("/", "-", $model->upddate);

       $seqStr = "";
       $sep = "";
       foreach ($model->deleteSeq as $seq) {
           $seqStr .= $sep . $seq;
           $sep = "','";
       }

       $query  = " DELETE FROM ";
       $query .= "     HREPORT_GUIDANCE_SCHREG_REMARK_DAT ";
       $query .= " WHERE ";
       $query .= "         YEAR         = '{$model->exp_year}' ";
       $query .= "     AND SEMESTER     = '{$semester}' ";
       $query .= "     AND RECORD_DATE  = '{$dstr}' ";
       $query .= "     AND SCHREGNO     = '{$model->schregno}' ";
       $query .= "     AND DIV          = '{$model->selKindNo}' ";
       $query .= "     AND SEQ         IN ('{$seqStr}') ";

       $db->query($query);

       Query::dbCheckIn($db);
       return;
   }

   //存在チェック -- HREPORT_GUIDANCE_SCHREG_REMARK_DAT
   function getHreportGuidanceSchregRemarkDatCnt($model, $semester, $seq) {
       $dstr = str_replace("/", "-", $model->upddate);

       $query  = " SELECT ";
       $query .= "     COUNT ";
       $query .= " FROM ";
       $query .= "     HREPORT_GUIDANCE_SCHREG_REMARK_DAT ";
       $query .= " WHERE ";
       $query .= "     YEAR = '{$model->exp_year}' ";
       $query .= "     AND SEMESTER = '{$semester}' ";
       $query .= "     AND RECORD_DATE = '{$dstr}' ";
       $query .= "     AND SCHREGNO = '{$model->schregno}' ";
       $query .= "     AND DIV = '{$model->selKindNo}' ";
       $query .= "     AND SEQ = '{$seq}' ";

        return $query;
   }

    /************************/
    /* 「自立活動」区分登録 */
    /************************/
    //指導内容を取得
    function getSidouNaiyou($model) {
        $dstr = str_replace("/", "-", $model->upddate);

        $query  = " SELECT ";
        $query .= "     REMARK ";
        $query .= " FROM ";
        $query .= "     HREPORT_GUIDANCE_SCHREG_REMARK_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$model->exp_year}' ";
        $query .= "     AND RECORD_DATE = '{$dstr}' ";
        $query .= "     AND SCHREGNO = '{$model->schregno}' ";
        $query .= "     AND DIV = '{$model->selKindNo}' ";
        $query .= "     AND SEQ = 3 ";

        return $query;
    }

    //「自立活動」区分登録のリストを取得
    function getHreportGuidanceSelfrelianceMst($model) {
        $query  = " SELECT ";
        $query .= "     SELF_DIV ";
        $query .= "     , SELF_SEQ ";
        $query .= "     , SELF_TITLE ";
        $query .= "     , SELF_ITEM ";
        $query .= "     , SELF_CONTENT ";
        $query .= " FROM ";
        $query .= "     HREPORT_GUIDANCE_SELFRELIANCE_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$model->exp_year}' ";

        return $query;
    }

    //登録チェック
    function getHreportGuidanceSchregSelfrelianceDat($model, $selfDiv, $selfSeq) {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     HREPORT_GUIDANCE_SCHREG_SELFRELIANCE_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$model->exp_year}' ";
        $query .= "     AND SCHREGNO = '{$model->schregno}' ";
        $query .= "     AND SELF_TARGET = '".sprintf("%02d", $model->field["TARGET"])."' ";
        $query .= "     AND SELF_DIV = '{$selfDiv}' ";
        $query .= "     AND SELF_SEQ = '{$selfSeq}' ";

        return $query;
    }

    //INSERT -- HREPORT_GUIDANCE_SCHREG_SUBCLASS_DAT
    function &getZirituInsertQuery($model) {
        //DB接続
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $query  = " DELETE FROM ";
        $query .= "     HREPORT_GUIDANCE_SCHREG_SELFRELIANCE_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$model->exp_year}' ";
        $query .= "     AND SCHREGNO = '{$model->schregno}' ";
        $query .= "     AND SELF_TARGET = '".sprintf("%02d", $model->field["TARGET"])."' ";

        $db->query($query);

        foreach ($model->zirituCheckValue as $val) {
            $data = array();

            $self = explode(":", $val);

            $data["YEAR"][TEXT]             = $model->exp_year;
            $data["SCHREGNO"][TEXT]         = $model->schregno;
            $data["SELF_TARGET"][TEXT]      = sprintf("%02d", $model->field["TARGET"]);
            $data["SELF_DIV"][TEXT]         = $self[0];
            $data["SELF_SEQ"][TEXT]         = $self[1];
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][NUMBER]        = "sysdate()";

            $query = Query::insertSQL($data, "HREPORT_GUIDANCE_SCHREG_SELFRELIANCE_DAT");

            $db->query($query);
        }

        $db->commit();
        //DB切断
        Query::dbCheckIn($db);
    }

    /************************/
    /* 前年度データ参照 */
    /************************/
    function getLastYearData($model, $semester) {
        $dstr = str_replace("/", "-", $model->upddate);

        $query  = " SELECT ";
        $query .= "     KIND_NAME.KIND_SEQ ";
        $query .= "     , KIND_NAME.KIND_REMARK ";
        $query .= "     , SCH_RE.SEMESTER ";
        $query .= "     , SCH_RE.REMARK ";
        $query .= "     , SCH_RE.SEQ ";
        $query .= " FROM ";
        $query .= "     HREPORT_GUIDANCE_KIND_NAME_DAT KIND_NAME ";
        $query .= "     LEFT JOIN HREPORT_GUIDANCE_SCHREG_REMARK_DAT SCH_RE ";
        $query .= "         ON SCH_RE.YEAR = KIND_NAME.YEAR ";
        $query .= "         AND SCH_RE.DIV = KIND_NAME.KIND_NO ";
        $query .= "         AND SCH_RE.SEQ = LTRIM(KIND_NAME.KIND_SEQ, '0') ";
        $query .= "         AND SCH_RE.SEMESTER = '{$semester}' ";
        $query .= "         AND SCH_RE.RECORD_DATE = '{$dstr}' ";
        $query .= "         AND SCH_RE.SCHREGNO = '{$model->schregno}' ";
        $query .= " WHERE ";
        $query .= "     KIND_NAME.YEAR = '".($model->exp_year - 1)."' ";
        $query .= "     AND KIND_NAME.KIND_NO = '{$model->selKindNo}'; ";

        return $query;
    }

    function getLastYearSemester($model) {
        $query  = " SELECT ";
        $query .= "     SEMESTER, ";
        $query .= "     SEMESTERNAME ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".($model->exp_year - 1)."' ";
        $query .= "     AND SEMESTER <> '9' ";
        $query .= " ORDER BY ";
        $query .= "     SEMESTER ";

        return $query;
    }
}
?>
