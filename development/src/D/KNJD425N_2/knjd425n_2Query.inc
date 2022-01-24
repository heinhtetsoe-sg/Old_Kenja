<?php

require_once('for_php7.php');
class knjd425n_2Query extends Query {

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

    //テーブルヘッダ取得
    function getDetailRemark($model) {
        $dstr = str_replace("/", "-", $model->upddate);

        $query  = " SELECT ";
        $query .= "     V_SUB.SUBCLASSNAME ";
        $query .= "     , V_SUB.CLASSCD || '-' || V_SUB.SCHOOL_KIND || '-' || V_SUB.CURRICULUM_CD || '-' || V_SUB.SUBCLASSCD AS SUBCLASSCD ";
        $query .= "     , REMARK.REMARK ";
        $query .= " FROM ";
        $query .= "     GRADE_KIND_SCHREG_GROUP_DAT SCH_GRP ";
        $query .= "     LEFT JOIN GRADE_KIND_COMP_GROUP_DAT COMP_GRP ";
        $query .= "         ON COMP_GRP.YEAR = SCH_GRP.YEAR ";
        $query .= "         AND COMP_GRP.SEMESTER = SCH_GRP.SEMESTER ";
        $query .= "         AND COMP_GRP.GAKUBU_SCHOOL_KIND = SCH_GRP.GAKUBU_SCHOOL_KIND ";
        $query .= "         AND COMP_GRP.GHR_CD = SCH_GRP.GHR_CD ";
        $query .= "         AND COMP_GRP.GRADE = SCH_GRP.GRADE ";
        $query .= "         AND COMP_GRP.HR_CLASS = SCH_GRP.HR_CLASS ";
        $query .= "         AND COMP_GRP.CONDITION = SCH_GRP.CONDITION ";
        $query .= "         AND COMP_GRP.GROUPCD = SCH_GRP.GROUPCD ";
        $query .= "     LEFT JOIN V_SUBCLASS_MST V_SUB ";
        $query .= "         ON V_SUB.YEAR = SCH_GRP.YEAR ";
        $query .= "         AND V_SUB.CLASSCD = COMP_GRP.CLASSCD ";
        $query .= "         AND V_SUB.SCHOOL_KIND = COMP_GRP.SCHOOL_KIND ";
        $query .= "         AND V_SUB.CURRICULUM_CD = COMP_GRP.CURRICULUM_CD ";
        $query .= "         AND V_SUB.SUBCLASSCD = COMP_GRP.SUBCLASSCD ";
        $query .= "     LEFT JOIN HREPORT_GUIDANCE_SCHREG_SUBCLASS_REMARK_DAT REMARK ";
        $query .= "         ON REMARK.YEAR = SCH_GRP.YEAR ";
        $query .= "         AND REMARK.SEMESTER = '9' ";
        $query .= "         AND REMARK.RECORD_DATE = '{$dstr}' ";
        $query .= "         AND REMARK.SCHREGNO = SCH_GRP.SCHREGNO ";
        $query .= "         AND REMARK.DIV = '{$model->selKindNo}' ";
        $query .= "         AND REMARK.CLASSCD = COMP_GRP.CLASSCD ";
        $query .= "         AND REMARK.SCHOOL_KIND = COMP_GRP.SCHOOL_KIND ";
        $query .= "         AND REMARK.CURRICULUM_CD = COMP_GRP.CURRICULUM_CD ";
        $query .= "         AND REMARK.SUBCLASSCD = COMP_GRP.SUBCLASSCD ";
        $query .= " WHERE ";
        $query .= "     SCH_GRP.YEAR = '{$model->exp_year}' ";
        $query .= "     AND SCH_GRP.SEMESTER = '9' ";
        $query .= "     AND SCH_GRP.GRADE || '-' || SCH_GRP.HR_CLASS = '{$model->grade}' ";
        $query .= "     AND SCH_GRP.SCHREGNO = '{$model->schregno}' ";
        $query .= " ORDER BY ";
        $query .= "     V_SUB.CLASSCD ";
        $query .= "     , V_SUB.SCHOOL_KIND ";
        $query .= "     , V_SUB.CURRICULUM_CD ";
        $query .= "     , V_SUB.SUBCLASSCD ";

        return $query;
    }

    //UPDATE処理
    function &getUpdateQuery($model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);
        $dstr = str_replace("/", "-", $model->upddate);

        //削除
        $query  = " DELETE FROM HREPORT_GUIDANCE_SCHREG_SUBCLASS_REMARK_DAT ";
        $query .= " WHERE ";
        $query .= "  YEAR = '{$model->exp_year}' ";
        $query .= "  AND SEMESTER = '9' ";
        $query .= "  AND SCHREGNO = '{$model->schregno}' ";
        $query .= "  AND RECORD_DATE = '{$dstr}' ";
        $query .= "  AND DIV = '{$model->selKindNo}' ";
        $db->query($query);

        foreach ($model->subclasscdarry as $subclasscd) {
            $data = array();
            $cd = explode("-", $subclasscd);

            //登録
            $data["YEAR"][TEXT]          = $model->exp_year;
            $data["SEMESTER"][TEXT]      = "9";
            $data["RECORD_DATE"][TEXT]   = $dstr;
            $data["SCHREGNO"][TEXT]      = $model->schregno;
            $data["DIV"][TEXT]           = $model->selKindNo;
            $data["CLASSCD"][TEXT]       = $cd[0];
            $data["SCHOOL_KIND"][TEXT]   = $cd[1];
            $data["CURRICULUM_CD"][TEXT] = $cd[2];
            $data["SUBCLASSCD"][TEXT]    = $cd[3];
            $data["REMARK"][TEXT]       = $model->remarkarry[$subclasscd];
            $data["REGISTERCD"][TEXT]   = STAFFCD;
            $data["UPDATED"][FUNC]      = "sysdate()";

            $query = Query::insertSQL($data, "HREPORT_GUIDANCE_SCHREG_SUBCLASS_REMARK_DAT");
            $db->query($query);
        }
        $db->commit();
        Query::dbCheckIn($db);
        return;
    }

    //前年度 テーブルタイトル取得
    function getHreportGuidanceKindNameDat($model) {
        $query  = " SELECT ";
        $query .= "     KIND_REMARK ";
        $query .= " FROM ";
        $query .= "     HREPORT_GUIDANCE_KIND_NAME_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".($model->exp_year - 1)."' ";
        $query .= "     AND KIND_NO = '04' ";
        $query .= "     AND KIND_SEQ = '001' ";

        return $query;
    }

    //前年度 テーブルタイトル取得
    function getNenkanDataList($model) {
        $dstr = str_replace("/", "-", $model->upddate);

        $query  = " SELECT ";
        $query .= "     REMARK ";
        $query .= " FROM ";
        $query .= "     HREPORT_GUIDANCE_SCHREG_SUBCLASS_REMARK_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".($model->exp_year - 1)."' ";
        $query .= "     AND SEMESTER = '9' ";
        $query .= "     AND RECORD_DATE = '{$dstr}' ";
        $query .= "     AND SCHREGNO = '{$model->schregno}' ";
        $query .= "     AND DIV = '04' ";
        $query .= "     AND CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD = '{$model->subclasscd}' ";

        return $query;
    }

    //帳票パターン取得(KNJZ525で設定)
    function getWindowPatternDiv($model) {
        $query   = " SELECT ";
        $query  .= "   SHEET_PATTERN, ";
        $query  .= "   STATUS_NAME, ";
        $query  .= "   GROWUP_NAME ";
        $query  .= " FROM ";
        $query  .= "   CHALLENGED_ASSESSMENT_STATUS_GROWUP_DAT ";
        $query  .= "   V_CHALLENGED_ASSESSMENT_STATUS_GROWUP_DAT ";
        $query  .= " WHERE ";
        $query  .= "     YEAR = '{$model->exp_year}' ";
        $query  .= " AND DATA_DIV = '0' ";

        return $query;
    }

    //「実態＆支援」取得
    function getRemarkZittaiAndSien($model) {
        $query   = " WITH ASSESS_MAIN AS ( ";
        $query  .= "   SELECT ";
        $query  .= "     CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD AS SUBCLASSCD, ";
        $query  .= "     RECORD_DATE, ";
        $query  .= "     STATUS, ";
        $query  .= "     FUTURE_CARE, ";
        $query  .= "     ROW_NUMBER() OVER ( ";
        $query  .= "       PARTITION BY ";
        $query  .= "         CLASSCD, ";
        $query  .= "         SCHOOL_KIND, ";
        $query  .= "         CURRICULUM_CD, ";
        $query  .= "         SUBCLASSCD ";
        $query  .= "       ORDER BY ";
        $query  .= "         RECORD_DATE DESC ";
        $query  .= "     ) AS DATE_NO ";
        $query  .= "   FROM ";
        $query  .= "     SCHREG_CHALLENGED_ASSESSMENT_STATUS_SUBCLASS_DAT ";
        $query  .= "   WHERE ";
        $query  .= "     YEAR = '{$model->exp_year}' ";
        $query  .= "     AND SCHREGNO = '{$model->schregno}' ";
        $query  .= "     AND RECORD_DATE <> 'NEW' ";
        $query  .= " ) ";
        $query  .= " SELECT ";
        $query  .= "   SUBCLASSCD, ";
        $query  .= "   RECORD_DATE, ";
        $query  .= "   STATUS, ";
        $query  .= "   FUTURE_CARE ";
        $query  .= " FROM ";
        $query  .= "   ASSESS_MAIN ";
        $query  .= " WHERE ";
        $query  .= "   DATE_NO = '1' ";
        $query  .= " ORDER BY ";
        $query  .= "   SUBCLASSCD ";

        return $query;
    }
    //「目指したい自立の姿」取得
    function getRemarkZiritu($model) {

        $query   = " SELECT ";
        $query  .= "   YEAR, ";
        $query  .= "   SCHREGNO, ";
        $query  .= "   RECORD_DATE, ";
        $query  .= "   WRITING_DATE, ";
        $query  .= "   SELFRELIANCE_GOAL ";
        $query  .= " FROM ";
        $query  .= "   SCHREG_CHALLENGED_SUPPORTPLAN_MAIN_DAT ";
        $query  .= " WHERE ";
        $query  .= "   YEAR = '{$model->exp_year}' ";
        $query  .= "   AND SCHREGNO = '{$model->schregno}' ";
        $query  .= "   AND RECORD_DATE <> 'NEW' ";
        $query  .= " ORDER BY ";
        $query  .= "   RECORD_DATE DESC ";

        return $query;
    }
}
?>
