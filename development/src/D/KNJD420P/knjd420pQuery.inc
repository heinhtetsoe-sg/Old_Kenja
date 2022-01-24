<?php

require_once('for_php7.php');

class knjd420pQuery extends Query {

    //更新日取得(※最新を最後にするために、上位ではLIFO形式で処理する。そのため、降順で取得している。)
    function getUpdatedDateList($model) {
        $query  = " SELECT DISTINCT ";
        $query .= "  RECORD_DATE AS LABEL, ";
        $query .= "  RECORD_DATE AS VALUE ";
        $query .= " FROM ";
        $query .= "  HREPORT_GUIDANCE_SCHREG_REMARK_DAT ";
        $query .= " WHERE ";
        $query .= "  YEAR = '{$model->exp_year}' ";
        $query .= "  AND SEMESTER = '9' ";
        $query .= "  AND SCHREGNO = '{$model->schregno}' ";
        $query .= " ORDER BY ";
        $query .= "  RECORD_DATE DESC";

        return $query;
    }

    //生徒詳細情報(氏名、生年月日等)取得
    function getSchInfoShousai($model)
    {
        $query  = "  SELECT ";
        $query .= "      T1.SCHREGNO,  ";
        $query .= "      T3.SCHOOL_KIND,  ";
        $query .= "      N1.ABBV1 AS GAKUBU_NAME, ";
        $query .= "      T4.HR_NAME, ";
        $query .= "      G2.GHR_NAME, ";
        $query .= "      T1.NAME_SHOW,  ";
        $query .= "      T1.NAME_KANA,  ";
        $query .= "      T1.BIRTHDAY,  ";
        $query .= "      N2.NAME2 AS SEX_NAME ";
        $query .= "  FROM ";
        $query .= "      V_SCHREG_BASE_MST T1 ";
        $query .= "      LEFT JOIN SCHREG_REGD_DAT T2 ON T1.SCHREGNO = T2.SCHREGNO ";
        $query .= "      LEFT JOIN SCHREG_REGD_GDAT T3 ON T2.YEAR = T3.YEAR ";
        $query .= "                                   AND T2.GRADE = T3.GRADE ";
        $query .= "      LEFT JOIN SCHREG_REGD_HDAT T4 ON T2.YEAR = T4.YEAR ";
        $query .= "                                   AND T2.SEMESTER = T4.SEMESTER  ";
        $query .= "                                   AND T2.GRADE || T2.HR_CLASS = T4.GRADE || T4.HR_CLASS  ";
        $query .= "      LEFT JOIN SCHREG_REGD_GHR_DAT G1 ON G1.SCHREGNO = T2.SCHREGNO ";
        $query .= "                                      AND G1.YEAR =T2.YEAR ";
        $query .= "                                      AND G1.SEMESTER = T2.SEMESTER ";
        $query .= "      LEFT JOIN SCHREG_REGD_GHR_HDAT G2 ON G2.YEAR = G1.YEAR ";
        $query .= "                                       AND G2.SEMESTER = G1.SEMESTER ";
        $query .= "                                       AND G2.GHR_CD = G1.GHR_CD ";
        $query .= "      LEFT JOIN V_NAME_MST N1 ON N1.YEAR = T2.YEAR ";
        $query .= "                             AND N1.NAMECD1 = 'A023' ";
        $query .= "                             AND N1.NAME1 = T3.SCHOOL_KIND ";
        $query .= "      LEFT JOIN V_NAME_MST N2 ON T2.YEAR = N2.YEAR ";
        $query .= "                             AND N2.NAMECD1 = 'Z002' ";
        $query .= "                             AND T1.SEX = N2.NAMECD2 ";
        $query .= "  WHERE ";
        $query .= "      T1.SCHREGNO = '".$model->schregno."' ";
        $query .= "  AND T2.YEAR     = '".$model->exp_year."' ";
        $query .= "  AND T2.SEMESTER = '".CTRL_SEMESTER."' ";

        return $query;
    }

    //KNJE390を元に作成。
    //障害名、概要を取得。
    function getSubQuery1($model, $setSanshouData="")
    {

        $query  = " SELECT ";
        $query .= "     T1.* ";
        $query .= " FROM ";
        $query .= "     SCHREG_CHALLENGED_ASSESSMENT_MAIN_DAT T1 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".$model->exp_year."'";
        $query .= " AND T1.SCHREGNO = '".$model->schregno."' ";
        $query .= " AND T1.RECORD_DATE = '".$setSanshouData."' ";

        return $query;
    }

    //KNJE390を元に作成。
    //レコードの日付取得 (全て表示)
    function getTorikomiRecordDate($model)
    {
        $settableName = "SCHREG_CHALLENGED_ASSESSMENT_MAIN_DAT";

        $query  = " SELECT ";
        $query .= "      RECORD_DATE AS VALUE, ";
        $query .= "      CASE WHEN RECORD_DATE = 'NEW' THEN '最新' ELSE REPLACE(RECORD_DATE,'-','/') END AS LABEL ";
        $query .= "  FROM ";
        $query .= "      $settableName ";
        $query .= "  WHERE ";
        $query .= "      YEAR = '".$model->exp_year."' ";
        $query .= "  AND SCHREGNO = '".$model->schregno."' ";
        $query .= "  ORDER BY ";
        $query .= "      VALUE DESC ";

        return $query;
    }

    //障害種別、作成日、作成者を取得
    function getSubQuery2CheckRecordList($model, $setSanshouData="")
    {
        $query  = " SELECT ";
        $query .= "     T1.* ";
        $query .= " FROM ";
        $query .= "     SCHREG_CHALLENGED_ASSESSMENT_CHECK_RECORD_DAT T1 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".$model->exp_year."' ";
        $query .= " AND T1.SCHREGNO = '".$model->schregno."' ";
        $query .= " AND T1.RECORD_DATE = '".$setSanshouData."' ";
        $query .= " ORDER BY ";
        $query .= "     T1.RECORD_SEQ ";

        return $query;
    }

    //障害種別、作成日、作成者(DIV='01'の情報)を取得
    function getRemarkDiv01Data($model)
    {
        $dstr = str_replace("/", "-", $model->upddate);
        $query  = " SELECT ";
        $query .= "     T1.DIV, ";
        $query .= "     T1.SEQ, ";
        $query .= "     CASE WHEN T1.SEQ = 3 THEN T2.STAFFNAME_SHOW ";
        $query .= "          WHEN T1.SEQ = 1 THEN ";
        $query .= "               CASE WHEN D091.NAME1 IS NULL THEN T1.REMARK ELSE D091.NAME1 END ";
        $query .= "          ELSE T1.REMARK END AS REMARK ";
        $query .= " FROM ";
        $query .= "     HREPORT_GUIDANCE_SCHREG_REMARK_DAT T1 ";
        $query .= "     LEFT JOIN STAFF_MST T2 ";
        $query .= "       ON T2.STAFFCD = T1.REMARK ";
        $query .= "     LEFT JOIN V_NAME_MST D091 ";
        $query .= "       ON D091.YEAR = '{$model->exp_year}' ";
        $query .= "      AND D091.NAMECD1 = 'D091' ";
        $query .= "      AND D091.NAMECD2 = T1.REMARK ";
        $query .= " WHERE ";
        $query .= "   T1.YEAR = '{$model->exp_year}' ";
        $query .= "   AND T1.SEMESTER = '9' ";
        $query .= "   AND T1.RECORD_DATE = '{$dstr}' ";
        $query .= "   AND T1.SCHREGNO = '{$model->schregno}' ";
        $query .= "   AND T1.DIV = '01' ";

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

    function getHDKindNameDat($model, $kindNo) {

        $query  = " SELECT ";
        $query .= "     KIND.KIND_SEQ, ";
        $query .= "     KIND.KIND_REMARK ";
        $query .= " FROM ";
        $query .= "     HREPORT_GUIDANCE_KIND_NAME_DAT KIND ";
        $query .= " WHERE ";
        $query .= "         KIND.YEAR    = '".CTRL_YEAR."' ";
        $query .= "     AND KIND.KIND_NO = '".$kindNo."' ";
        $query .= " ORDER BY ";
        $query .= "     KIND.KIND_SEQ ";

        return $query;
    }

    //「自立活動中心用」のデータを取得
    function getZirituKatudouList($model, $semester = "") {
        $query  = " SELECT ";
        $query .= "     DAT.YEAR, ";
        $query .= "     DAT.SCHREGNO, ";
        $query .= "     DAT.SEMESTER, ";
        $query .= "     DAT.GOALS, ";
        $query .= "     DAT.ACCOMMODATION, ";
        $query .= "     DAT.KEY_GOALS, ";
        $query .= "     DAT.GOALS_REASON, ";
        $query .= "     DAT.LONG_GOALS1, ";
        $query .= "     DAT.SHORT_GOALS1, ";
        $query .= "     DAT.MEANS1, ";
        $query .= "     DAT.EVALUATION1, ";
        $query .= "     DAT.LONG_GOALS2, ";
        $query .= "     DAT.SHORT_GOALS2, ";
        $query .= "     DAT.MEANS2, ";
        $query .= "     DAT.EVALUATION2, ";
        $query .= "     DAT.LONG_GOALS3, ";
        $query .= "     DAT.SHORT_GOALS3, ";
        $query .= "     DAT.MEANS3, ";
        $query .= "     DAT.EVALUATION3, ";
        $query .= "     DAT.LONG_GOALS4, ";
        $query .= "     DAT.SHORT_GOALS4, ";
        $query .= "     DAT.MEANS4, ";
        $query .= "     DAT.EVALUATION4, ";
        $query .= "     SEMS.SEMESTERNAME ";
        $query .= " FROM ";
        $query .= "     V_HREPORT_SELFRELIANCE_SCHREG_REMARK_DAT DAT ";
        $query .= " INNER JOIN ";
        $query .= "     SEMESTER_MST SEMS";
        $query .= "     ON SEMS.YEAR = DAT.YEAR ";
        $query .= "     AND SEMS.SEMESTER = DAT.SEMESTER ";
        $query .= " WHERE ";
        $query .= "     DAT.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND DAT.SCHREGNO = '{$model->schregno}' ";
        if ($semester != "") {
            $query .= "     AND DAT.SEMESTER = '{$semester}' ";
        }
        $query .= " ORDER BY ";
        $query .= "     DAT.YEAR, ";
        $query .= "     DAT.SCHREGNO, ";
        $query .= "     DAT.SEMESTER ";

        return $query;
    }

    function getZirituKatudouDivList($model, $semester, $div) {

        $query  = " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     HREPORT_SELFRELIANCE_SCHREG_REMARK_DIV_DAT DAT ";
        $query .= " WHERE ";
        $query .= "     DAT.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND DAT.SCHREGNO = '{$model->schregno}' ";
        $query .= "     AND DAT.SEMESTER = '{$semester}' ";
        $query .= "     AND DAT.DIV = '{$div}' ";

        return $query;
    }

    function &getInsertQuery($model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $dstr = str_replace("/", "-", $model->upddate);

        $data = array();
        $dataDiv = array();
        switch (intval($model->updateIndex)) {
            case 1 :
                $semester = "9";
                for ($i = 1; $i <= 2; $i++) {
                    $key = sprintf("%03d", $i);
                    $data[$model->title_key[$key]][TEXT] = $model->field[$model->title_key[$key]];
                }
                break;
            case 2 :
                $semester = "9";
                for ($i = 3; $i <= 4; $i++) {
                    $key = sprintf("%03d", $i);
                    $data[$model->title_key[$key]][TEXT] = $model->field[$model->title_key[$key]];
                }
                break;
            case 3 :
                $semester = $model->field["SEMESTER"];
                for ($i = 1; $i <= 4; $i++) {
                    for ($j = 5; $j <= 8; $j++) {
                        $key = sprintf("%03d", $j);
                        $dataDiv[$i][$model->title_key[$key]][TEXT] = $model->field[$model->title_key[$key].$i];
                    }
                }
                break;
            default :
                //処理なし
        }

        if (intval($model->updateIndex) == 3) {
            foreach ($dataDiv as $key => $val) {
                $div = sprintf("%02d", $key);
                $divCnt = $db->getOne(knjd420pQuery::getZirituKatudouDivList($model, $semester, $div));

                $data = array();
                $data["REGISTERCD"][TEXT] = STAFFCD;
                $data["UPDATED"][NUMBER]  = "sysdate()";
                foreach ($val as $fieldName => $fieldVal) {
                    $data[$fieldName] = $fieldVal;
                }
                if ($divCnt == 0) {
                    //登録
                    $data["YEAR"][TEXT]     = CTRL_YEAR;
                    $data["SEMESTER"][TEXT] = $semester;
                    $data["SCHREGNO"][TEXT] = $model->schregno;
                    $data["DIV"][TEXT]      = $div;
        
                    $query = Query::insertSQL($data, "HREPORT_SELFRELIANCE_SCHREG_REMARK_DIV_DAT");
                    $db->query($query);
                } else {
                    //更新
                    $where  = " WHERE ";
                    $where .= "         YEAR         = '".CTRL_YEAR."' ";
                    $where .= "     AND SEMESTER     = '".$semester."' ";
                    $where .= "     AND SCHREGNO     = '{$model->schregno}' ";
                    $where .= "     AND DIV          = '{$div}' ";
        
                    $query = Query::updateSQL($data, "HREPORT_SELFRELIANCE_SCHREG_REMARK_DIV_DAT", $where);
                    $db->query($query);
                }
            }
        } else {
            $result = $db->getRow(knjd420pQuery::getZirituKatudouList($model, $semester));
        
            $data["REGISTERCD"][TEXT] = STAFFCD;
            $data["UPDATED"][NUMBER]  = "sysdate()";
            if (!is_array($result)) {
                //登録
                $data["YEAR"][TEXT]     = CTRL_YEAR;
                $data["SEMESTER"][TEXT] = $semester;
                $data["SCHREGNO"][TEXT] = $model->schregno;
    
                $query = Query::insertSQL($data, "HREPORT_SELFRELIANCE_SCHREG_REMARK_DAT");
                $db->query($query);
            } else {
                //更新
                $where  = " WHERE ";
                $where .= "         YEAR         = '".CTRL_YEAR."' ";
                $where .= "     AND SEMESTER     = '".$semester."' ";
                $where .= "     AND SCHREGNO     = '{$model->schregno}' ";
    
                $query = Query::updateSQL($data, "HREPORT_SELFRELIANCE_SCHREG_REMARK_DAT", $where);
                $db->query($query);
            }
        }

        $db->commit();
        Query::dbCheckIn($db);
        return;
    }
}
?>
