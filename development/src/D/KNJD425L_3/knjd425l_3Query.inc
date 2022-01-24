<?php

require_once('for_php7.php');

class knjd425l_3Query extends Query {

    //タイトル取得
    function getHreportGuidanceKindNameHdat($model, $year) {
        $query  = " SELECT ";
        $query .= "     KIND_NAME ";
        $query .= " FROM ";
        $query .= "     HREPORT_GUIDANCE_KIND_NAME_HDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$year}' ";
        $query .= "     AND KIND_NO = '{$model->selKindNo}' ";

        return $query;
    }

    //データ項目取得
    function getDetailSchregSubclassRemark($model, $year, $div, $subclass) {

        $query  = " SELECT ";
        $query .= "   COMP.YEAR ";
        $query .= "   , COMP.SEMESTER ";
        $query .= "   , COMP.CLASSCD ";
        $query .= "   , COMP.SCHOOL_KIND ";
        $query .= "   , COMP.CURRICULUM_CD ";
        $query .= "   , COMP.SUBCLASSCD ";
        $query .= "   , SUB.SUBCLASSNAME ";
        $query .= "   , REMARK.REMARK ";

        $query .= " FROM ";
        $query .= "   GRADE_KIND_SCHREG_GROUP_DAT SCH ";
        $query .= "   INNER JOIN GRADE_KIND_COMP_GROUP_DAT COMP ";
        $query .= "     ON SCH.YEAR = COMP.YEAR ";
        $query .= "     AND SCH.SEMESTER = COMP.SEMESTER ";
        $query .= "     AND SCH.GAKUBU_SCHOOL_KIND = COMP.GAKUBU_SCHOOL_KIND ";
        $query .= "     AND SCH.GHR_CD = COMP.GHR_CD ";
        $query .= "     AND SCH.GRADE = COMP.GRADE ";
        $query .= "     AND SCH.HR_CLASS = COMP.HR_CLASS ";
        $query .= "     AND SCH.CONDITION = COMP.CONDITION ";
        $query .= "     AND SCH.GROUPCD = COMP.GROUPCD ";

        $query .= "   INNER JOIN V_SUBCLASS_MST SUB ";
        $query .= "     ON SCH.YEAR = SUB.YEAR ";
        $query .= "     AND COMP.CLASSCD = SUB.CLASSCD ";
        $query .= "     AND COMP.SCHOOL_KIND = SUB.SCHOOL_KIND ";
        $query .= "     AND COMP.CURRICULUM_CD = SUB.CURRICULUM_CD ";
        $query .= "     AND COMP.SUBCLASSCD = SUB.SUBCLASSCD ";

        $query .= "   LEFT JOIN HREPORT_GUIDANCE_SCHREG_SUBCLASS_REMARK_DAT REMARK ";
        $query .= "     ON SCH.YEAR = REMARK.YEAR ";
        $query .= "     AND SCH.SEMESTER = REMARK.SEMESTER ";
        $query .= "     AND REMARK.RECORD_DATE = '{$model->recordDate}' ";
        $query .= "     AND SCH.SCHREGNO = REMARK.SCHREGNO ";
        $query .= "     AND REMARK.DIV = '{$div}' ";
        $query .= "     AND COMP.CLASSCD = REMARK.CLASSCD ";
        $query .= "     AND COMP.SCHOOL_KIND = REMARK.SCHOOL_KIND ";
        $query .= "     AND COMP.CURRICULUM_CD = REMARK.CURRICULUM_CD ";
        $query .= "     AND COMP.SUBCLASSCD = REMARK.SUBCLASSCD ";

        $query .= " WHERE ";
        $query .= "   SCH.YEAR = '{$year}' ";
        $query .= "   AND SCH.SEMESTER = '9' ";
        $query .= "   AND SCH.SCHREGNO = '{$model->schregno}' ";

        if ($subclass != "") {
            list($classCd, $schoolKind, $curriculumCd, $subclassCd) = explode("-", $subclass);
            $query .= "   AND COMP.CLASSCD = '{$classCd}' ";
            $query .= "   AND COMP.SCHOOL_KIND = '{$schoolKind}' ";
            $query .= "   AND COMP.CURRICULUM_CD = '{$curriculumCd}' ";
            $query .= "   AND COMP.SUBCLASSCD = '{$subclassCd}' ";
        }

        $query .= " ORDER BY ";
        $query .= "   COMP.CLASSCD ";
        $query .= "   , COMP.SCHOOL_KIND ";
        $query .= "   , COMP.CURRICULUM_CD ";
        $query .= "   , COMP.SUBCLASSCD ";
        $query .= "   , SUB.SUBCLASSNAME ";

        return $query;
    }

    //データ項目取得
    function getHreportGuidanceSchregSubclassRemark($model, $year, $div, $subclass) {

        $query  = " SELECT ";
        $query .= "   * ";
        $query .= " FROM ";
        $query .= "   HREPORT_GUIDANCE_SCHREG_SUBCLASS_REMARK_DAT ";
        $query .= " WHERE ";
        $query .= "   YEAR = '{$year}' ";
        $query .= "   AND SEMESTER = '9' ";
        $query .= "   AND SCHREGNO = '{$model->schregno}' ";
        $query .= "   AND DIV = '{$div}' ";

        if ($subclass != "") {
            list($classCd, $schoolKind, $curriculumCd, $subclassCd) = explode("-", $subclass);
            $query .= "   AND CLASSCD = '{$classCd}' ";
            $query .= "   AND SCHOOL_KIND = '{$schoolKind}' ";
            $query .= "   AND CURRICULUM_CD = '{$curriculumCd}' ";
            $query .= "   AND SUBCLASSCD = '{$subclassCd}' ";
        }

        $query .= " ORDER BY ";
        $query .= "   DIV ";
        $query .= "   , CLASSCD ";
        $query .= "   , SCHOOL_KIND ";
        $query .= "   , CURRICULUM_CD ";
        $query .= "   , SUBCLASSCD ";

        return $query;
    }

    //(実態)帳票パターン取得
    function getChallengedAssessmentStatusGrowupDat($model, $dataDiv) {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     CHALLENGED_ASSESSMENT_STATUS_GROWUP_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$model->exp_year."' ";
        if ($dataDiv != "") {
            $query .= "   AND DATA_DIV = '{$dataDiv}' ";
        }
        $query .= " ORDER BY ";
        $query .= "     INT(DATA_DIV) ";

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


    /************/
    /* 更新処理 */
    /************/
    //UPDATE -- HREPORT_GUIDANCE_SCHREG_REMARK_DAT
    function &getUpdateQuery($model) {
        $db = Query::dbCheckOut();
        // $db->autoCommit(false);

        $semester = '9';

        foreach ($model->subclassList as $subclass) {

            list($classCd, $schoolKind, $curriculumCd, $subclassCd) = explode("-", $subclass);

            $model->field["REMARK_{$subclass}"];

            $data = array();
            $data["REMARK"][TEXT]           = $model->field["REMARK_{$subclass}"];
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][NUMBER]        = "sysdate()";
            //存在チェック
            $query = knjd425l_3Query::getHreportGuidanceSchregSubclassRemark($model, $model->exp_year, $model->selKindNo, $subclass);
            $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
            if ($row) {
                //更新
                $where  = " WHERE ";
                $where .= "     YEAR          = '{$model->exp_year}' ";
                $where .= " AND SEMESTER      = '{$semester}' ";
                $where .= " AND RECORD_DATE   = '{$model->recordDate}' ";
                $where .= " AND SCHREGNO      = '{$model->schregno}' ";
                $where .= " AND DIV           = '{$model->selKindNo}' ";
                $where .= " AND CLASSCD       = '{$classCd}' ";
                $where .= " AND SCHOOL_KIND   = '{$schoolKind}' ";
                $where .= " AND CURRICULUM_CD = '{$curriculumCd}' ";
                $where .= " AND SUBCLASSCD    = '{$subclassCd}' ";

                $query = Query::updateSQL($data, "HREPORT_GUIDANCE_SCHREG_SUBCLASS_REMARK_DAT", $where);
                $db->query($query);
            } else {
                //登録
                $data["YEAR"][TEXT]          = $model->exp_year;
                $data["SEMESTER"][TEXT]      = $semester;
                $data["RECORD_DATE"][DATE]   = $model->recordDate;
                $data["SCHREGNO"][TEXT]      = $model->schregno;
                $data["DIV"][TEXT]           = $model->selKindNo;
                $data["CLASSCD"][TEXT]       = $classCd;
                $data["SCHOOL_KIND"][TEXT]   = $schoolKind;
                $data["CURRICULUM_CD"][TEXT] = $curriculumCd;
                $data["SUBCLASSCD"][TEXT]    = $subclassCd;

                $query = Query::insertSQL($data, "HREPORT_GUIDANCE_SCHREG_SUBCLASS_REMARK_DAT");
                $db->query($query);
            }
        }

        $db->commit();
        Query::dbCheckIn($db);
        return;
    }

}
?>
