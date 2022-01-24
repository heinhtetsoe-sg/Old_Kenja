<?php

require_once('for_php7.php');

class knjd425lQuery extends Query
{

    //名称マスタ取得
    public function getNameMst($nameCd1, $nameCd2)
    {
        $query  = " SELECT ";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL ";
        $query .= "   , NAMECD2 AS VALUE ";
        $query .= "   , NAME1 ";
        $query .= "   , NAMESPARE1 ";
        $query .= "   , NAMESPARE3 ";
        $query .= "   , ABBV3 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND NAMECD1 = '".$nameCd1."' ";
        if ($nameCd2 != "") {
            $query .= "     AND NAMECD2 = '".$nameCd2."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE(NAMECD2, '99') ";
        return $query;
    }

    //支援計画項目名取得
    public function getChallengedSupportplanKindName($model, $kindNo)
    {
        $query .= " SELECT * ";
        $query .= " FROM CHALLENGED_SUPPORTPLAN_KIND_NAME_DAT ";
        $query .= " WHERE ";
        $query .= "   YEAR = '{$model->exp_year}' ";
        $query .= "   AND KIND_NO = '{$kindNo}' ";

        return $query;
    }

    //ボタン名称取得
    public function getGuidanceKindName($model, $schregNo, $grade, $kindNo)
    {
        $query  = " SELECT ";
        $query .= "  T1.KIND_NO, ";
        $query .= "  T2.KIND_NAME AS BTN_SUBFORMCALL, ";
        $query .= "  CASE WHEN T1.KIND_NO = '01' THEN '1' ";
        $query .= "       WHEN T1.KIND_NO = '02' THEN '2' ";
        $query .= "       WHEN T1.KIND_NO IN ('03','05') THEN '3' ";
        $query .= "       WHEN T1.KIND_NO = '04' THEN '4' ";
        // $query .= "       WHEN T1.KIND_NO = '05' THEN '3' ";
        $query .= "       ELSE '' END AS KINDCD ";
        $query .= " FROM ";
        $query .= "  HREPORT_GUIDANCE_KIND_DAT T1 ";
        $query .= "  LEFT JOIN HREPORT_GUIDANCE_KIND_NAME_HDAT T2 ";
        $query .= "    ON T2.YEAR = T1.YEAR ";
        $query .= "   AND T2.KIND_NO = T1.KIND_NO";
        $query .= " WHERE ";
        $query .= "  T1.YEAR = '{$model->exp_year}' ";
        if ($schregNo != "") {
            $query .= "  AND T1.SCHREGNO = '{$schregNo}' ";
        } else {
            $query .= "  AND T1.SCHREGNO = '00000000' ";
        }
        if ($grade != "") {
            $query .= "  AND T1.GRADE || '-' || T1.HR_CLASS = '{$grade}' ";
        } else {
            $query .= "  AND T1.GRADE || '-' || T1.HR_CLASS = '00-000' ";
        }
        if ($kindNo != "") {
            $query .= "  AND T1.KIND_NO = '{$kindNo}' ";
        }
        $query .= " ORDER BY T1.SHOWORDER ";

        return $query;
    }

    //生徒詳細情報(氏名、生年月日等)取得
    public function getSchInfoShousai($model)
    {
        $query  = "";
        $query .= " SELECT ";
        $query .= "   BASE.SCHREGNO ";
        $query .= "   , REGDG.SCHOOL_KIND ";
        $query .= "   , COURSE.COURSENAME ";
        $query .= "   , REGDH.HR_NAME ";
        $query .= "   , REGDG.GRADE_NAME2 GRADE_NAME ";
        $query .= "   , REGDH.HR_CLASS_NAME2 HR_CLASS_NAME ";
        $query .= "   , GHRH.GHR_NAMEABBV GHR_NAME ";
        $query .= "   , BASE.NAME ";
        $query .= "   , BASE.NAME_KANA ";
        $query .= " FROM ";
        $query .= "   V_SCHREG_BASE_MST BASE ";
        $query .= "   LEFT JOIN SCHREG_REGD_DAT REGD ";
        $query .= "     ON BASE.SCHREGNO = REGD.SCHREGNO ";
        $query .= "   LEFT JOIN SCHREG_REGD_GDAT REGDG ";
        $query .= "     ON REGD.YEAR = REGDG.YEAR ";
        $query .= "     AND REGD.GRADE = REGDG.GRADE ";
        $query .= "   LEFT JOIN SCHREG_REGD_HDAT REGDH ";
        $query .= "     ON REGD.YEAR = REGDH.YEAR ";
        $query .= "     AND REGD.SEMESTER = REGDH.SEMESTER ";
        $query .= "     AND REGD.GRADE = REGDH.GRADE ";
        $query .= "     AND REGD.HR_CLASS = REGDH.HR_CLASS ";
        $query .= "   LEFT JOIN SCHREG_REGD_GHR_DAT GHR ";
        $query .= "     ON GHR.SCHREGNO = REGD.SCHREGNO ";
        $query .= "     AND GHR.YEAR = REGD.YEAR ";
        $query .= "     AND GHR.SEMESTER = REGD.SEMESTER ";
        $query .= "   LEFT JOIN SCHREG_REGD_GHR_HDAT GHRH ";
        $query .= "     ON GHRH.YEAR = GHR.YEAR ";
        $query .= "     AND GHRH.SEMESTER = GHR.SEMESTER ";
        $query .= "     AND GHRH.GHR_CD = GHR.GHR_CD ";
        $query .= "   LEFT JOIN V_COURSE_MST COURSE ";
        $query .= "     ON COURSE.YEAR = REGD.YEAR ";
        $query .= "     AND COURSE.COURSECD = REGD.COURSECD ";
        $query .= " WHERE ";
        $query .= "      BASE.SCHREGNO = '".$model->schregno."' ";
        $query .= "  AND REGD.YEAR     = '".$model->exp_year."' ";
        $query .= "  AND REGD.SEMESTER = '".CTRL_SEMESTER."' ";

        return $query;
    }

    //アセスメント情報取得
    public function getAssessmentMain($model)
    {
        $query  = " SELECT ";
        $query .= "     T1.* ";
        $query .= " FROM ";
        $query .= "     SCHREG_CHALLENGED_ASSESSMENT_MAIN_DAT T1 ";
        $query .= " WHERE ";
        $query .= "       T1.YEAR = '".$model->exp_year."'";
        $query .= "   AND T1.SCHREGNO = '".$model->schregno."' ";
        $query .= " ORDER BY ";
        $query .= "      T1.RECORD_DATE DESC ";

        return $query;
    }

    //アセスメント情報取得
    public function getAssessmentCheckRecord($model, $recordDate, $recordSeq)
    {
        $query  = " SELECT ";
        $query .= "     T1.* ";
        $query .= " FROM ";
        $query .= "     SCHREG_CHALLENGED_ASSESSMENT_CHECK_RECORD_DAT T1 ";
        $query .= " WHERE ";
        $query .= "       T1.YEAR = '".$model->exp_year."'";
        $query .= "   AND T1.SCHREGNO = '".$model->schregno."' ";
        $query .= "   AND T1.RECORD_DATE = '".$recordDate."' ";
        if ($recordSeq != "") {
            $query .= "   AND T1.RECORD_SEQ = ".$recordSeq." ";
        }
        $query .= " ORDER BY ";
        $query .= "      T1.RECORD_SEQ ";

        return $query;
    }

    //支援計画情報取得
    public function getSupportplanMain($model)
    {
        $query  = " SELECT ";
        $query .= "     T1.* ";
        $query .= " FROM ";
        $query .= "     SCHREG_CHALLENGED_SUPPORTPLAN_MAIN_DAT T1 ";
        $query .= " WHERE ";
        $query .= "       T1.YEAR = '".$model->exp_year."'";
        $query .= "   AND T1.SCHREGNO = '".$model->schregno."' ";

        $query .= " ORDER BY ";
        $query .= "      T1.RECORD_DATE DESC ";

        return $query;
    }

    //支援計画情報取得
    public function getSupportplanRecord($model, $div)
    {
        $query  = " SELECT ";
        $query .= "     T1.* ";
        $query .= " FROM ";
        $query .= "     SCHREG_CHALLENGED_SUPPORTPLAN_RECORD_DAT T1 ";
        $query .= " WHERE ";
        $query .= "       T1.YEAR = '".$model->exp_year."'";
        $query .= "   AND T1.SCHREGNO = '".$model->schregno."' ";
        if ($div != "") {
            $query .= "   AND T1.DIV = '".$div."' ";
        }
        $query .= "   AND T1.RECORD_DATE = (SELECT MAX(T2.RECORD_DATE) FROM SCHREG_CHALLENGED_SUPPORTPLAN_RECORD_DAT T2 ";
        $query .= "                         WHERE T2.YEAR = T1.YEAR AND T2.SCHREGNO = T1.SCHREGNO AND T2.DIV = T1.DIV ) ";
        $query .= " ORDER BY ";
        $query .= "      T1.DIV ";

        return $query;
    }

    //作成日取得
    public function getGuidanceSchregRemark($model, $seq)
    {
        $query  = " SELECT ";
        $query .= "     T1.* ";
        $query .= " FROM ";
        $query .= "     HREPORT_GUIDANCE_SCHREG_REMARK_DAT T1 ";
        $query .= " WHERE ";
        $query .= "       T1.YEAR = '".$model->exp_year."'";
        $query .= "   AND T1.SEMESTER = '9' ";
        $query .= "   AND T1.DIV = '00' ";
        $query .= "   AND T1.SCHREGNO = '".$model->schregno."' ";
        if ($seq != "") {
            $query .= "   AND T1.SEQ = ".$seq." ";
        }

        $query .= " ORDER BY ";
        $query .= "      T1.RECORD_DATE DESC ";

        return $query;
    }

    //職員
    public function getStaff($model)
    {
        $query .= " SELECT ";
        $query .= "     STF.STAFFCD AS VALUE, ";
        $query .= "     STF.STAFFNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_STAFF_MST STF ";
        $query .= " WHERE ";
        $query .= "     STF.YEAR = '{$model->exp_year}' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "           AND (STF.STAFFCD NOT IN (SELECT STAFFCD FROM USER_MST) ";
                $query .= "             OR STF.STAFFCD IN (SELECT STAFFCD FROM USER_MST WHERE SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind), "','")."')) ";
                $query .= "               ) ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= "           AND (STF.STAFFCD NOT IN (SELECT STAFFCD FROM USER_MST) ";
            $query .= "             OR STF.STAFFCD IN (SELECT STAFFCD FROM USER_MST WHERE SCHOOL_KIND = '".SCHOOLKIND."') ";
            $query .= "               ) ";
        }
        $query .= " ORDER  BY ";
        $query .= "     STF.STAFFCD ";

        return $query;
    }

    //更新日取得
    public function getUpdatedDateList($model)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "    RECORD_DATE AS LABEL ";
        $query .= "  , RECORD_DATE AS VALUE ";
        $query .= " FROM ";
        $query .= "    HREPORT_GUIDANCE_SCHREG_REMARK_DAT ";
        $query .= " WHERE ";
        $query .= "    YEAR = '{$model->exp_year}' ";
        $query .= "    AND SEMESTER = '9' ";
        $query .= "    AND SCHREGNO = '{$model->schregno}' ";
        $query .= " ORDER BY ";
        $query .= "    RECORD_DATE ";

        return $query;
    }

    //更新
    public function &getUpdateQuery($model)
    {
        $db = Query::dbCheckOut();
        // $db->autoCommit(false);

        //削除
        // HREPORT_GUIDANCE_SCHREG_REMARK_DAT
        $query  = " DELETE FROM ";
        $query .= "     HREPORT_GUIDANCE_SCHREG_REMARK_DAT ";
        $query .= " WHERE ";
        $query .= "       YEAR = '".$model->exp_year."'";
        $query .= "   AND SEMESTER = '9' ";
        $query .= "   AND DIV = '00' ";
        $query .= "   AND SCHREGNO = '".$model->schregno."' ";
        $db->query($query);

        //更新(作成日)
        $data = array();
        $data["YEAR"][TEXT]        = $model->exp_year;
        $data["SEMESTER"][TEXT]    = '9';
        $data["RECORD_DATE"][TEXT] = str_replace("/", "-", $model->recordDate);
        $data["SCHREGNO"][TEXT]    = $model->schregno;
        $data["DIV"][TEXT]         = '00';
        $data["SEQ"][NUMBER]       = 1;
        $data["REMARK"][TEXT]      = str_replace("/", "-", $model->field["SUPPORTPLAN_DATE"]);

        $data["REGISTERCD"][TEXT]  = STAFFCD;
        $data["UPDATED"][FUNC]     = "sysdate()";
        $query = Query::insertSQL($data, "HREPORT_GUIDANCE_SCHREG_REMARK_DAT");
        $db->query($query);

        //更新(作成者)
        $data = array();
        $data["YEAR"][TEXT]        = $model->exp_year;
        $data["SEMESTER"][TEXT]    = '9';
        $data["RECORD_DATE"][TEXT] = str_replace("/", "-", $model->recordDate);
        $data["SCHREGNO"][TEXT]    = $model->schregno;
        $data["DIV"][TEXT]         = '00';
        $data["SEQ"][NUMBER]       = 2;
        $data["REMARK"][TEXT]      = $model->field["SUPPORTPLAN_STAFFCD"];

        $data["REGISTERCD"][TEXT]  = STAFFCD;
        $data["UPDATED"][FUNC]     = "sysdate()";
        $query = Query::insertSQL($data, "HREPORT_GUIDANCE_SCHREG_REMARK_DAT");
        $db->query($query);

        $db->commit();
        Query::dbCheckIn($db);
        return;
    }

    //障害名、概要を取得。
    public function getSubQuery1($model, $setSanshouData = "")
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
    public function getTorikomiRecordDate($model)
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
    public function getSubQuery2CheckRecordList($model, $setSanshouData = "")
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
    public function getRemarkDiv01Data($model)
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
}
