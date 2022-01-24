<?php

require_once('for_php7.php');

class knjd425l_4Query extends Query
{

    //タイトル取得
    public function getHreportGuidanceKindNameHdat($model)
    {
        $query  = " SELECT ";
        $query .= "     KIND_NAME ";
        $query .= " FROM ";
        $query .= "     HREPORT_GUIDANCE_KIND_NAME_HDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$model->exp_year}' ";
        $query .= "     AND KIND_NO = '{$model->selKindNo}' ";

        return $query;
    }

    //項目タイトル取得
    public function getHreportGuidanceKindNamedat($model)
    {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     HREPORT_GUIDANCE_KIND_NAME_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$model->exp_year}' ";
        $query .= "     AND KIND_NO = '{$model->selKindNo}' ";

        return $query;
    }

    //履修教科・科目グループ取得
    public function getGradeKindSchregGroupDat($model)
    {
        $query  = " SELECT ";
        $query .= "     T1.YEAR ";
        $query .= "   , T1.SEMESTER ";
        $query .= "   , T1.GAKUBU_SCHOOL_KIND ";
        $query .= "   , T1.GHR_CD ";
        $query .= "   , T1.GRADE ";
        $query .= "   , T1.HR_CLASS ";
        $query .= "   , T1.CONDITION ";
        $query .= "   , T3.NAME1 CONDITION_NAME ";
        $query .= "   , T1.GROUPCD ";
        $query .= "   , T2.GROUPNAME ";
        $query .= "   , T1.SCHREGNO ";
        $query .= " FROM ";
        $query .= "     GRADE_KIND_SCHREG_GROUP_DAT T1 ";
        $query .= " LEFT JOIN GRADE_KIND_COMP_GROUP_YMST T2 ";
        $query .= "     ON T2.YEAR = T1.YEAR ";
        $query .= "     AND T2.SEMESTER = T1.SEMESTER ";
        $query .= "     AND T2.GAKUBU_SCHOOL_KIND = T1.GAKUBU_SCHOOL_KIND ";
        $query .= "     AND T2.GHR_CD = T1.GHR_CD ";
        $query .= "     AND T2.GRADE = T1.GRADE ";
        $query .= "     AND T2.HR_CLASS = T1.HR_CLASS ";
        $query .= "     AND T2.CONDITION = T1.CONDITION ";
        $query .= "     AND T2.GROUPCD = T1.GROUPCD ";
        $query .= " LEFT JOIN V_NAME_MST T3 ";
        $query .= "     ON T3.YEAR = T1.YEAR ";
        $query .= "     AND T3.NAMECD1 = 'A033' ";
        $query .= "     AND T3.NAMECD2 = T1.CONDITION ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".$model->exp_year."' ";
        $query .= "     AND T1.SEMESTER = '9' ";
        $query .= "     AND T1.SCHREGNO = '".$model->schregno."' ";

        return $query;
    }

    //項目名取得
    public function getGuidanceItemName($model)
    {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     HREPORT_GUIDANCE_ITEM_NAME_DAT ";
        $query .= " WHERE YEAR                = '".$model->exp_year."' ";
        $query .= "   AND SEMESTER            = '9' ";
        $query .= "   AND GAKUBU_SCHOOL_KIND  = '".$model->schregInfo["GAKUBU_SCHOOL_KIND"]."' ";
        $query .= "   AND CONDITION           = '".$model->schregInfo["CONDITION"]."' ";
        $query .= "   AND GUIDANCE_PATTERN    = '3' ";

        return $query;
    }

    //学期取得
    public function getSemester($model)
    {
        $query  = " SELECT ";
        $query .= "     SEMESTER AS VALUE ";
        $query .= "   , SEMESTERNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$model->exp_year}' ";
        $query .= "     AND SEMESTER <> '9' ";
        $query .= " ORDER BY ";
        $query .= "     SEMESTER ";

        return $query;
    }

    //科目取得
    public function getSubclass($model)
    {
        $query  = " SELECT ";
        $query .= "     L3.CLASSCD || '-' || L3.SCHOOL_KIND || '-' || L3.CURRICULUM_CD || '-' || L3.SUBCLASSCD AS VALUE ";
        $query .= "   , L3.CLASSCD || '-' || L3.SCHOOL_KIND || '-' || L3.CURRICULUM_CD || '-' || L3.SUBCLASSCD || ':' || L1.SUBCLASSNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     GRADE_KIND_SCHREG_GROUP_DAT T1 ";
        $query .= "     INNER JOIN GRADE_KIND_COMP_GROUP_DAT L3 ";
        $query .= "         ON T1.YEAR = L3.YEAR ";
        $query .= "        AND T1.SEMESTER    = L3.SEMESTER ";
        $query .= "        AND T1.GAKUBU_SCHOOL_KIND = L3.GAKUBU_SCHOOL_KIND ";
        $query .= "        AND T1.GHR_CD      = L3.GHR_CD ";
        $query .= "        AND T1.GRADE       = L3.GRADE ";
        $query .= "        AND T1.HR_CLASS    = L3.HR_CLASS ";
        $query .= "        AND T1.CONDITION   = L3.CONDITION ";
        $query .= "        AND T1.GROUPCD     = L3.GROUPCD ";
        $query .= "     INNER JOIN SUBCLASS_MST L1 ";
        $query .= "         ON L3.CLASSCD = L1.CLASSCD ";
        $query .= "        AND L3.SCHOOL_KIND = L1.SCHOOL_KIND ";
        $query .= "        AND L3.CURRICULUM_CD = L1.CURRICULUM_CD ";
        $query .= "        AND L3.SUBCLASSCD  = L1.SUBCLASSCD ";
        $query .= " WHERE ";
        $query .= "         T1.YEAR = '".$model->exp_year."' ";
        $query .= "     AND T1.SEMESTER = '9' ";
        $query .= "     AND T1.SCHREGNO = '".$model->schregno."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //単元取得
    public function getUnit($model, $subclassCd, $unitCd)
    {
        $query  = " SELECT ";
        $query .= "     T1.UNITCD AS VALUE ";
        $query .= "   , T1.UNITNAME AS LABEL ";
        $query .= "   , T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD SUBCLASSCD ";
        $query .= " FROM ";
        $query .= "     GRADE_KIND_UNIT_GROUP_YMST T1 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR         = '".$model->exp_year."' ";
        $query .= " AND T1.SEMESTER     = '9' ";
        $query .= " AND T1.GAKUBU_SCHOOL_KIND = '".$model->schregInfo["GAKUBU_SCHOOL_KIND"]."' ";
        $query .= " AND T1.GHR_CD       = '".$model->schregInfo["GHR_CD"]."' ";
        $query .= " AND T1.GRADE        = '".$model->schregInfo["GRADE"]."' ";
        $query .= " AND T1.HR_CLASS     = '".$model->schregInfo["HR_CLASS"]."' ";
        $query .= " AND T1.CONDITION    = '".$model->schregInfo["CONDITION"]."' ";
        $query .= " AND T1.GROUPCD      = '".$model->schregInfo["GROUPCD"]."' ";
        if ($subclassCd != "") {
            $query .= " AND T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD = '{$subclassCd}' ";
        }
        if ($unitCd != "") {
            $query .= " AND T1.UNITCD = '{$unitCd}' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }


    //一覧を取得（各教科等）
    public function getSubclassRemark($model)
    {

        $query  = " SELECT ";
        $query .= "      SCH_SUB.SEMESTER";
        $query .= "    , SEM.SEMESTERNAME ";
        $query .= "    , SCH_SUB.CLASSCD || '-' || SCH_SUB.SCHOOL_KIND || '-' || SCH_SUB.CURRICULUM_CD || '-' || SCH_SUB.SUBCLASSCD AS SUBCLASSCD ";
        $query .= "    , SCH_SUB.CLASSCD || '-' || SCH_SUB.SCHOOL_KIND || '-' || SCH_SUB.CURRICULUM_CD || '-' || SCH_SUB.SUBCLASSCD || ':' || V_SUB.SUBCLASSNAME AS SUBCLASSNAME ";
        $query .= "    , SCH_SUB.UNITCD ";
        $query .= "    , SCH_SUB.SEQ ";
        $query .= "    , SCH_SUB.REMARK ";
        $query .= " FROM ";
        $query .= "     HREPORT_GUIDANCE_SCHREG_SUBCLASS_DAT SCH_SUB ";
        $query .= "     LEFT JOIN V_SUBCLASS_MST V_SUB ";
        $query .= "         ON V_SUB.YEAR = SCH_SUB.YEAR ";
        $query .= "         AND V_SUB.CLASSCD = SCH_SUB.CLASSCD ";
        $query .= "         AND V_SUB.SCHOOL_KIND = SCH_SUB.SCHOOL_KIND ";
        $query .= "         AND V_SUB.CURRICULUM_CD = SCH_SUB.CURRICULUM_CD ";
        $query .= "         AND V_SUB.SUBCLASSCD = SCH_SUB.SUBCLASSCD ";
        $query .= "     LEFT JOIN SEMESTER_MST SEM ";
        $query .= "         ON SEM.YEAR = SCH_SUB.YEAR ";
        $query .= "         AND SEM.SEMESTER = SCH_SUB.SEMESTER ";
        $query .= " WHERE ";
        $query .= "     SCH_SUB.YEAR = '{$model->exp_year}' ";
        $query .= "     AND SCH_SUB.SCHREGNO = '{$model->schregno}' ";
        $query .= "     AND SCH_SUB.GUIDANCE_PATTERN = '3' ";
        $query .= " ORDER BY ";
        $query .= "       SCH_SUB.SEMESTER ";
        $query .= "     , SCH_SUB.CLASSCD || '-' || SCH_SUB.SCHOOL_KIND || '-' || SCH_SUB.CURRICULUM_CD || '-' || SCH_SUB.SUBCLASSCD ";
        $query .= "     , SCH_SUB.UNITCD ";
        $query .= "     , SCH_SUB.SEQ ";

        return $query;
    }

    //構成元教科を取得
    public function getTargetClass($model, $subclasscd)
    {
        $query  = " SELECT ";
        $query .= "     V_CLS.CLASSNAME ";
        $query .= " FROM ";
        $query .= "     HREPORT_GUIDANCE_SCHREG_TARGETCLASS_DAT TGT ";
        $query .= "     LEFT JOIN V_CLASS_MST V_CLS ";
        $query .= "        ON V_CLS.YEAR = TGT.YEAR ";
        $query .= "        AND V_CLS.CLASSCD = TGT.TARGET_CLASSCD ";
        $query .= "        AND V_CLS.SCHOOL_KIND = TGT.TARGET_SCHOOL_KIND ";
        $query .= " WHERE ";
        $query .= "     TGT.YEAR = '{$model->exp_year}' ";
        $query .= "     AND TGT.SCHREGNO = '{$model->schregno}' ";
        $query .= "     AND TGT.CLASSCD || '-' || TGT.SCHOOL_KIND || '-' || TGT.CURRICULUM_CD || '-' || TGT.SUBCLASSCD = '{$subclasscd}' ";

        return $query;
    }

    //教科・科目コンボ
    public function getSubclasscdCombo($model)
    {
        $query  = " SELECT ";
        $query .= "     V_SUB.CLASSCD || '-' || V_SUB.SCHOOL_KIND || '-' || V_SUB.CURRICULUM_CD || '-' || V_SUB.SUBCLASSCD || ':' || V_SUB.SUBCLASSNAME AS LABEL ";
        $query .= "     , V_SUB.CLASSCD || '-' || V_SUB.SCHOOL_KIND || '-' || V_SUB.CURRICULUM_CD || '-' || V_SUB.SUBCLASSCD AS VALUE ";
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
        $query .= " WHERE ";
        $query .= "     SCH_GRP.YEAR = '{$model->exp_year}' ";
        $query .= "     AND SCH_GRP.SEMESTER = '9' ";
        $query .= "     AND SCH_GRP.GRADE || '-' || SCH_GRP.HR_CLASS = '{$model->grade}' ";
        $query .= "     AND SCH_GRP.SCHREGNO = '{$model->schregno}' ";
        $query .= " ORDER BY ";
        $query .= "     V_SUB.CLASSCD ";
        $query .= "     , V_SUB.SCHOOL_KIND ";
        $query .= "     , V_SUB.CURRICULUM_CD ";
        $query .= "     , V_SUB.SUBCLASSCD; ";

        return $query;
    }

    //一覧を取得（総合所見）
    public function getRemarkList($model)
    {
        $query  = " SELECT ";
        $query .= "       KIND_NAME.KIND_REMARK ";
        $query .= "     , SCH_RE.SEMESTER ";
        $query .= "     , SCH_RE.SEQ ";
        $query .= "     , SCH_RE.REMARK ";
        $query .= "     , SEM.SEMESTERNAME ";
        $query .= " FROM ";
        $query .= "     HREPORT_GUIDANCE_SCHREG_REMARK_DAT SCH_RE ";
        $query .= "     LEFT JOIN HREPORT_GUIDANCE_KIND_NAME_DAT KIND_NAME ";
        $query .= "         ON KIND_NAME.YEAR = SCH_RE.YEAR ";
        $query .= "         AND KIND_NAME.KIND_NO = SCH_RE.DIV ";
        $query .= "         AND LTRIM(KIND_NAME.KIND_SEQ, '0') = SCH_RE.SEQ ";
        $query .= "     LEFT JOIN SEMESTER_MST SEM ";
        $query .= "         ON SEM.YEAR = SCH_RE.YEAR ";
        $query .= "         AND SEM.SEMESTER = SCH_RE.SEMESTER ";
        $query .= " WHERE ";
        $query .= "     SCH_RE.YEAR = '{$model->exp_year}' ";
        $query .= "     AND SCH_RE.RECORD_DATE = '{$model->recordDate}' ";
        $query .= "     AND SCH_RE.SCHREGNO = '{$model->schregno}' ";
        $query .= "     AND SCH_RE.DIV = '{$model->selKindNo}' ";
        $query .= "     AND SCH_RE.SEQ = 1 ";
        $query .= " ORDER BY ";
        $query .= "       SCH_RE.SEMESTER ";
        $query .= "     , SCH_RE.SEQ ";

        return $query;
    }

    //INSERT
    public function &getInsertQuery($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        if ($model->field["INPUT_ITEMS"] == 1) {
            //各教科等 -- HREPORT_GUIDANCE_SCHREG_SUBCLASS_DAT
            $cd = explode("-", $model->field["SUBCLASSCD"]);
            list($classCd, $schoolKind, $curriculumCd, $subclassCd) = explode("-", $model->field["SUBCLASSCD"]);

            for ($i = 1; $i <= 3; $i++) {
                $data = array();
                $data["REMARK"][TEXT]           = $model->field["REMARK_{$i}"];
                $data["REGISTERCD"][TEXT]       = STAFFCD;
                $data["UPDATED"][NUMBER]        = "sysdate()";
                //存在チェック
                $query = knjd425l_4Query::getHreportGuidanceSchregSubclassDatCnt($model, $model->field["SUBCLASSCD"], $model->field["SEMESTER"], $model->field["UNITCD"], $i);
                if (0 == $db->getOne($query)) {
                    //登録
                    $data["YEAR"][TEXT]                = $model->exp_year;
                    $data["SEMESTER"][TEXT]            = $model->field["SEMESTER"];
                    $data["SCHREGNO"][TEXT]            = $model->schregno;
                    $data["CLASSCD"][TEXT]             = $classCd;
                    $data["SCHOOL_KIND"][TEXT]         = $schoolKind;
                    $data["CURRICULUM_CD"][TEXT]       = $curriculumCd;
                    $data["SUBCLASSCD"][TEXT]          = $subclassCd;
                    $data["UNITCD"][TEXT]              = $model->field["UNITCD"];
                    $data["GUIDANCE_PATTERN"][TEXT]    = "3";
                    $data["SEQ"][NUMBER]               = $i;

                    $query = Query::insertSQL($data, "HREPORT_GUIDANCE_SCHREG_SUBCLASS_DAT");
                    $db->query($query);
                } else {
                    //更新
                    $where  = " WHERE ";
                    $where .= "         YEAR             = '{$model->exp_year}' ";
                    $where .= "     AND SEMESTER         = '{$model->field["SEMESTER"]}' ";
                    $where .= "     AND SCHREGNO         = '{$model->schregno}' ";
                    $where .= "     AND CLASSCD          = '{$classCd}' ";
                    $where .= "     AND SCHOOL_KIND      = '{$schoolKind}' ";
                    $where .= "     AND CURRICULUM_CD    = '{$curriculumCd}' ";
                    $where .= "     AND SUBCLASSCD       = '{$subclassCd}' ";
                    $where .= "     AND UNITCD           = '{$model->field["UNITCD"]}' ";
                    $where .= "     AND GUIDANCE_PATTERN = '3' ";
                    $where .= "     AND SEQ              = {$i} ";

                    $query = Query::updateSQL($data, "HREPORT_GUIDANCE_SCHREG_SUBCLASS_DAT", $where);
                    $db->query($query);
                }
            }
        } else {
            //総合所見 -- HREPORT_GUIDANCE_SCHREG_REMARK_DAT
            $data = array();
            $data["REMARK"][TEXT]           = $model->field["REMARK"];
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][NUMBER]        = "sysdate()";
            //存在チェック
            $query = knjd425l_4Query::getHreportGuidanceSchregRemarkDatCnt($model, $model->field["SEMESTER"], 1);
            if (0 == $db->getOne($query)) {
                //登録
                $data["YEAR"][TEXT]             = $model->exp_year;
                $data["SEMESTER"][TEXT]         = $model->field["SEMESTER"];
                $data["RECORD_DATE"][DATE]      = $model->recordDate;
                $data["SCHREGNO"][TEXT]         = $model->schregno;
                $data["DIV"][TEXT]              = $model->selKindNo;
                $data["SEQ"][NUMBER]            = 1;

                $query = Query::insertSQL($data, "HREPORT_GUIDANCE_SCHREG_REMARK_DAT");
                $db->query($query);
            } else {
                //更新
                $where  = " WHERE ";
                $where .= "         YEAR         = '{$model->exp_year}' ";
                $where .= "     AND SEMESTER     = '{$model->field["SEMESTER"]}' ";
                $where .= "     AND RECORD_DATE  = '{$model->recordDate}' ";
                $where .= "     AND SCHREGNO     = '{$model->schregno}' ";
                $where .= "     AND DIV          = '{$model->selKindNo}' ";
                $where .= "     AND SEQ          = 1 ";

                $query = Query::updateSQL($data, "HREPORT_GUIDANCE_SCHREG_REMARK_DAT", $where);
                $db->query($query);
            }
        }

        $db->commit();
        Query::dbCheckIn($db);
        return;
    }

    //DELETE -- HREPORT_GUIDANCE_SCHREG_REMARK_DAT
    public function &getDeleteQuery($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        if ($model->field["INPUT_ITEMS"] == 1) {
            list($classCd, $schoolKind, $curriculumCd, $subclassCd) = explode("-", $model->field["SUBCLASSCD"]);

            $query  = " DELETE FROM ";
            $query .= "     HREPORT_GUIDANCE_SCHREG_SUBCLASS_DAT ";
            $query .= " WHERE ";
            $query .= "         YEAR         = '{$model->exp_year}' ";
            $query .= "     AND SEMESTER     = '{$model->field["SEMESTER"]}' ";
            $query .= "     AND SCHREGNO     = '{$model->schregno}' ";
            $query .= "     AND CLASSCD          = '{$classCd}' ";
            $query .= "     AND SCHOOL_KIND      = '{$schoolKind}' ";
            $query .= "     AND CURRICULUM_CD    = '{$curriculumCd}' ";
            $query .= "     AND SUBCLASSCD       = '{$subclassCd}' ";
            $query .= "     AND UNITCD           = '{$model->field["UNITCD"]}' ";
            $query .= "     AND GUIDANCE_PATTERN = '3' ";
            $query .= "     AND SEQ         IN (1, 2, 3) ";
        } else {
            $query  = " DELETE FROM ";
            $query .= "     HREPORT_GUIDANCE_SCHREG_REMARK_DAT ";
            $query .= " WHERE ";
            $query .= "         YEAR         = '{$model->exp_year}' ";
            $query .= "     AND SEMESTER     = '{$model->field["SEMESTER"]}' ";
            $query .= "     AND RECORD_DATE  = '{$model->recordDate}' ";
            $query .= "     AND SCHREGNO     = '{$model->schregno}' ";
            $query .= "     AND DIV          = '{$model->selKindNo}' ";
            $query .= "     AND SEQ          = 1 ";
        }

        $db->query($query);

        $db->commit();
        Query::dbCheckIn($db);
        return;
    }

    //DELETE -- HREPORT_GUIDANCE_SCHREG_REMARK_DAT
    public function &getListDeleteQuery($model)
    {

        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $delList = array();
        if ($model->deleteList) {
            $delList = explode(",", $model->deleteList);
        }
        if ($model->field["INPUT_ITEMS"] == 1) {
            for ($i=0; $i < get_count($delList); $i++) {
                list($semester, $subclass, $unitCd) = explode("_", $delList[$i]);
                list($classCd, $schoolKind, $curriculumCd, $subclassCd) = explode("-", $subclass);

                $query  = " DELETE FROM ";
                $query .= "     HREPORT_GUIDANCE_SCHREG_SUBCLASS_DAT ";
                $query .= " WHERE ";
                $query .= "         YEAR         = '{$model->exp_year}' ";
                $query .= "     AND SEMESTER     = '{$semester}' ";
                $query .= "     AND SCHREGNO     = '{$model->schregno}' ";
                $query .= "     AND CLASSCD          = '{$classCd}' ";
                $query .= "     AND SCHOOL_KIND      = '{$schoolKind}' ";
                $query .= "     AND CURRICULUM_CD    = '{$curriculumCd}' ";
                $query .= "     AND SUBCLASSCD       = '{$subclassCd}' ";
                $query .= "     AND UNITCD           = '{$unitCd}' ";
                $query .= "     AND GUIDANCE_PATTERN = '3' ";
                $query .= "     AND SEQ         IN (1, 2, 3) ";

                $db->query($query);
            }
        } else {
            for ($i=0; $i < get_count($delList); $i++) {
                $semester = $delList[$i];
 
                $query  = " DELETE FROM ";
                $query .= "     HREPORT_GUIDANCE_SCHREG_REMARK_DAT ";
                $query .= " WHERE ";
                $query .= "         YEAR         = '{$model->exp_year}' ";
                $query .= "     AND SEMESTER     = '{$semester}' ";
                $query .= "     AND RECORD_DATE  = '{$model->recordDate}' ";
                $query .= "     AND SCHREGNO     = '{$model->schregno}' ";
                $query .= "     AND DIV          = '{$model->selKindNo}' ";
                $query .= "     AND SEQ          = 1 ";

                $db->query($query);
            }
        }
  
        $db->commit();
        Query::dbCheckIn($db);
        return;
    }


    //存在チェック -- HREPORT_GUIDANCE_SCHREG_SUBCLASS_DAT
    public function getHreportGuidanceSchregSubclassDatCnt($model, $subclasscd, $semester, $unitCd, $seq)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) CNT ";
        $query .= " FROM ";
        $query .= "     HREPORT_GUIDANCE_SCHREG_SUBCLASS_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$model->exp_year}' ";
        $query .= "     AND SEMESTER = '{$semester}' ";
        $query .= "     AND SCHREGNO = '{$model->schregno}' ";
        $query .= "     AND CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD = '{$subclasscd}' ";
        $query .= "     AND UNITCD = '{$unitCd}' ";
        $query .= "     AND GUIDANCE_PATTERN = '3' ";
        $query .= "     AND SEQ = '{$seq}' ";

        return $query;
    }

    //存在チェック -- HREPORT_GUIDANCE_SCHREG_REMARK_DAT
    public function getHreportGuidanceSchregRemarkDatCnt($model, $semester, $seq)
    {
        $query  = " SELECT ";
        $query .= "     COUNT ";
        $query .= " FROM ";
        $query .= "     HREPORT_GUIDANCE_SCHREG_REMARK_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$model->exp_year}' ";
        $query .= "     AND SEMESTER = '{$semester}' ";
        $query .= "     AND RECORD_DATE = '{$model->recordDate}' ";
        $query .= "     AND SCHREGNO = '{$model->schregno}' ";
        $query .= "     AND DIV = '{$model->selKindNo}' ";
        $query .= "     AND SEQ = '{$seq}' ";

        return $query;
    }

    /****************************/
    /* 「合わせた指導」教科登録 */
    /****************************/
    //教科マスタを取得
    public function getVClass($model)
    {

        $query  = " SELECT ";
        $query .= "     CLS.SCHOOL_KIND ";
        $query .= "     , NAME.ABBV1 AS SCHOOL_KIND_NAME ";
        $query .= "     , CLS.CLASSCD ";
        $query .= "     , CLS.CLASSNAME ";
        $query .= " FROM ";
        $query .= "     V_CLASS_MST CLS ";
        $query .= "     LEFT JOIN V_NAME_MST NAME ";
        $query .= "         ON NAME.YEAR = CLS.YEAR ";
        $query .= "         AND NAME.NAME1 = CLS.SCHOOL_KIND ";
        $query .= "         AND NAME.NAMECD1 = 'A023' ";
        $query .= " WHERE ";
        $query .= "     CLS.YEAR = '{$model->exp_year}' ";
        $query .= " ORDER BY ";
        $query .= "     CLS.SCHOOL_KIND ";
        $query .= "     , CLS.CLASSCD ";

        return $query;
    }

    //登録チェック
    public function checkTargetClass($model, $subclasscd, $targetClasscd, $targetSchoolKind)
    {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     HREPORT_GUIDANCE_SCHREG_TARGETCLASS_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$model->exp_year}' ";
        $query .= "     AND SCHREGNO = '{$model->schregno}' ";
        // $query .= "     AND CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD = '{$subclasscd}' ";
        // $query .= "     AND TARGET_CLASSCD = '{$targetClasscd}' ";
        // $query .= "     AND TARGET_SCHOOL_KIND = '{$targetSchoolKind}' ";

        return $query;
    }

    /****************************/
    /* 「合わせた指導」教科登録 */
    /****************************/
    //INSERT -- HREPORT_GUIDANCE_SCHREG_SUBCLASS_DAT
    public function &getTargetClassInsertQuery($model)
    {
        //DB接続
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $query  = " DELETE FROM ";
        $query .= "     HREPORT_GUIDANCE_SCHREG_TARGETCLASS_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$model->exp_year}' ";
        $query .= "     AND SCHREGNO = '{$model->schregno}' ";
        $query .= "     AND CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD = '{$model->targetClassField["SUBCLASSCD"]}' ";

        $db->query($query);

        foreach ($model->targetClassCheck as $val) {
            $data = array();
            list($classCd, $schoolKind, $curriculumCd, $subclassCd) = explode("-", $model->targetClassField["SUBCLASSCD"]);
            list($targetClassCd, $targetSchoolKind) = explode("-", $val);

            $data["YEAR"][TEXT]               = $model->exp_year;
            $data["SCHREGNO"][TEXT]           = $model->schregno;
            $data["CLASSCD"][TEXT]            = $classCd;
            $data["SCHOOL_KIND"][TEXT]        = $schoolKind;
            $data["CURRICULUM_CD"][TEXT]      = $curriculumCd;
            $data["SUBCLASSCD"][TEXT]         = $subclassCd;
            $data["TARGET_CLASSCD"][TEXT]     = $targetClassCd;
            $data["TARGET_SCHOOL_KIND"][TEXT] = $targetSchoolKind;
            $data["REGISTERCD"][TEXT]         = STAFFCD;
            $data["UPDATED"][NUMBER]          = "sysdate()";

            $query = Query::insertSQL($data, "HREPORT_GUIDANCE_SCHREG_TARGETCLASS_DAT");

            $db->query($query);
        }

        $db->commit();
        //DB切断
        Query::dbCheckIn($db);
    }

    /************************/
    /* 合理的配慮参照 */
    /************************/
    //データ取得
    public function getReasonableAccommodation($model)
    {
        $query  = " SELECT ";
        $query .= "     REASONABLE_ACCOMMODATION ";
        $query .= " FROM ";
        $query .= "     SCHREG_CHALLENGED_SUPPORTPLAN_MAIN_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$model->exp_year}' ";
        $query .= "     AND SCHREGNO = '{$model->schregno}' ";
        $query .= " ORDER BY ";
        $query .= "     RECORD_DATE DESC ";

        return $query;
    }

    public function getLastYearSemester($model)
    {
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
