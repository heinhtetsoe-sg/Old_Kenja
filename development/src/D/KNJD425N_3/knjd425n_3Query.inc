<?php

require_once('for_php7.php');

class knjd425n_3Query extends Query {

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

    //グループ、履修情報取得
    function getViewGradeKindSchreg($model, $flg="") {
        $query  = " SELECT ";
        if ($flg == 'set') {
            $query .= " DISTINCT ";
            $query .= "     T1.GAKUBU_SCHOOL_KIND, ";
            $query .= "     T1.GHR_CD, ";
            $query .= "     T1.GRADE, ";
            $query .= "     T1.HR_CLASS, ";
            $query .= "     T1.CONDITION, ";
            $query .= "     T1.GROUPCD ";
        } else {
            $query .= "     T1.* ";
        }
        $query .= " FROM ";
        $query .= "     V_GRADE_KIND_SCHREG_UNIT_GROUP_DAT T1 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR     = '".$model->exp_year."' ";
        $query .= " AND T1.SEMESTER = '9' ";
        $query .= " AND T1.SCHREGNO = '".$model->schregno."' ";
        if ($flg != 'set') {
            $query .= " ORDER BY ";
            $query .= "     T1.GROUPCD,  ";
            $query .= "     T1.CLASSCD,  ";
            $query .= "     T1.SCHOOL_KIND,  ";
            $query .= "     T1.CURRICULUM_CD,  ";
            $query .= "     T1.SUBCLASSCD,  ";
            $query .= "     T1.UNITCD  ";
        }

        return $query;
    }

    //グループコード取得
    function getGroupcd($model, $row) {
        $query  = " SELECT ";
        $query .= "     GROUPNAME ";
        $query .= " FROM ";
        $query .= "     GRADE_KIND_COMP_GROUP_YMST ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".$model->exp_year."' ";
        $query .= " AND SEMESTER    = '9' ";
        $query .= " AND GAKUBU_SCHOOL_KIND = '".$row["GAKUBU_SCHOOL_KIND"]."' ";
        $query .= " AND GHR_CD      = '".$row["GHR_CD"]."' ";
        $query .= " AND GRADE       = '".$row["GRADE"]."' ";
        $query .= " AND HR_CLASS    = '".$row["HR_CLASS"]."' ";
        $query .= " AND CONDITION   = '".$row["CONDITION"]."' ";
        $query .= " AND GROUPCD     = '".$row["GROUPCD"]."' ";

        return $query;
    }

    //状態区分名称取得
    function getConditionName($model, $condition) {
        $query  = " SELECT ";
        $query .= "     NAME1 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".$model->exp_year."' ";
        $query .= " AND NAMECD1     = 'A033' ";
        $query .= " AND NAMECD2     = '".$condition."' ";

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

    //一覧を取得（各教科等）
    function getSubclassRemark($model) {
        if ($model->sort["SEMESTER"] == 1) {
            $order = "SCH_SUB.SEMESTER";
        } else {
            $order = "SCH_SUB.SEMESTER DESC";
        }

        $query  = " SELECT ";
        $query .= "    SCH_SUB.CLASSCD || '-' || SCH_SUB.SCHOOL_KIND || '-' || SCH_SUB.CURRICULUM_CD || '-' || SCH_SUB.SUBCLASSCD || ':' || V_SUB.SUBCLASSNAME AS SUBCLASSNAME ";
        $query .= "    , SCH_SUB.CLASSCD || '-' || SCH_SUB.SCHOOL_KIND || '-' || SCH_SUB.CURRICULUM_CD || '-' || SCH_SUB.SUBCLASSCD AS SUBCLASSCD ";
        $query .= "    , SCH_SUB.SEMESTER ";
        $query .= "    , SCH_SUB.SEQ ";
        $query .= "    , SCH_SUB.REMARK ";
        $query .= "    , SEM.SEMESTERNAME ";
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
        $query .= "     AND SCH_SUB.UNITCD = '00' ";
        $query .= "     AND SCH_SUB.GUIDANCE_PATTERN = '2' ";
        $query .= " ORDER BY ";
        $query .=       $order ;
        $query .= "     , SCH_SUB.CLASSCD || '-' || SCH_SUB.SCHOOL_KIND || '-' || SCH_SUB.CURRICULUM_CD || '-' || SCH_SUB.SUBCLASSCD ";
        $query .= "     , SCH_SUB.SEQ ";

        return $query;
    }

    //構成元教科を取得
    function getTargetClass($model, $subclasscd) {
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
    function getSubclasscdCombo($model) {
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
    function getRemarkList($model) {
        $dstr = str_replace("/", "-", $model->upddate);
        if ($model->sort["SEMESTER"] == 1) {
            $order = "SCH_RE.SEMESTER";
        } else {
            $order = "SCH_RE.SEMESTER DESC";
        }

        $query  = " SELECT ";
        $query .= "     KIND_NAME.KIND_REMARK ";
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
        $query .= "     AND SCH_RE.RECORD_DATE = '{$dstr}' ";
        $query .= "     AND SCH_RE.SCHREGNO = '{$model->schregno}' ";
        $query .= "     AND SCH_RE.DIV = '{$model->selKindNo}' ";
        $query .= "     AND SCH_RE.SEQ = 4 ";
        $query .= " ORDER BY ";
        $query .= $order;

        return $query;
    }

    //INSERT
    function &getInsertQuery($model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        if ($model->field["INPUT_ITEMS"] == 1) {
            //各教科等 -- HREPORT_GUIDANCE_SCHREG_SUBCLASS_DAT

            $cd = explode("-", $model->field["SUBCLASSCD"]);
            for ($i = 1; $i <= 3; $i++) {
                $data = array();
                $data["REMARK"][TEXT]           = $model->field["REMARK-{$i}"];
                $data["REGISTERCD"][TEXT]       = STAFFCD;
                $data["UPDATED"][NUMBER]        = "sysdate()";
                //存在チェック
                if (0 == $db->getOne(knjd425n_3Query::getHreportGuidanceSchregSubclassDatCnt($model, $model->field["SUBCLASSCD"], $model->field["SEMESTER"], $i))) {
                    //登録
                    $data["YEAR"][TEXT]                = $model->exp_year;
                    $data["SEMESTER"][TEXT]            = $model->field["SEMESTER"];
                    $data["SCHREGNO"][TEXT]            = $model->schregno;
                    $data["CLASSCD"][TEXT]             = $cd[0];
                    $data["SCHOOL_KIND"][TEXT]         = $cd[1];
                    $data["CURRICULUM_CD"][TEXT]       = $cd[2];
                    $data["SUBCLASSCD"][TEXT]          = $cd[3];
                    $data["UNITCD"][TEXT]              = "00";
                    $data["GUIDANCE_PATTERN"][TEXT]    = "2";
                    $data["SEQ"][NUMBER]               = $i;

                    $query = Query::insertSQL($data, "HREPORT_GUIDANCE_SCHREG_SUBCLASS_DAT");
                    $db->query($query);

                } else {
                    //更新
                    $where  = " WHERE ";
                    $where .= "         YEAR             = '{$model->exp_year}' ";
                    $where .= "     AND SEMESTER         = '{$model->field["SEMESTER"]}' ";
                    $where .= "     AND SCHREGNO         = '{$model->schregno}' ";
                    $where .= "     AND CLASSCD          = '{$cd[0]}' ";
                    $where .= "     AND SCHOOL_KIND      = '{$cd[1]}' ";
                    $where .= "     AND CURRICULUM_CD    = '{$cd[2]}' ";
                    $where .= "     AND SUBCLASSCD       = '{$cd[3]}' ";
                    $where .= "     AND UNITCD           = '00' ";
                    $where .= "     AND GUIDANCE_PATTERN = '2' ";
                    $where .= "     AND SEQ              = {$i} ";

                    $query = Query::updateSQL($data, "HREPORT_GUIDANCE_SCHREG_SUBCLASS_DAT", $where);
                    $db->query($query);
                }
            }
        } else {
            //総合所見 -- HREPORT_GUIDANCE_SCHREG_REMARK_DAT

            $dstr = str_replace("/", "-", $model->upddate);

            $data = array();
            $data["REMARK"][TEXT]           = $model->field["REMARK-4"];
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][NUMBER]        = "sysdate()";
            //存在チェック
            if (0 == $db->getOne(knjd425n_3Query::getHreportGuidanceSchregRemarkDatCnt($model, $model->field["SEMESTER"], 4))) {
                //登録
                $data["YEAR"][TEXT]             = $model->exp_year;
                $data["SEMESTER"][TEXT]         = $model->field["SEMESTER"];
                $data["RECORD_DATE"][DATE]      = $dstr;
                $data["SCHREGNO"][TEXT]         = $model->schregno;
                $data["DIV"][TEXT]              = $model->selKindNo;
                $data["SEQ"][NUMBER]            = 4;

                $query = Query::insertSQL($data, "HREPORT_GUIDANCE_SCHREG_REMARK_DAT");
                $db->query($query);

            } else {
                //更新
                $where  = " WHERE ";
                $where .= "         YEAR         = '{$model->exp_year}' ";
                $where .= "     AND SEMESTER     = '{$model->field["SEMESTER"]}' ";
                $where .= "     AND RECORD_DATE  = '{$dstr}' ";
                $where .= "     AND SCHREGNO     = '{$model->schregno}' ";
                $where .= "     AND DIV          = '{$model->selKindNo}' ";
                $where .= "     AND SEQ          = 4 ";

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
       $db->autoCommit(false);

       if ($model->field["INPUT_ITEMS"] == 1) {
           $cd = explode("-", $model->field["SUBCLASSCD"]);

           $query  = " DELETE FROM ";
           $query .= "     HREPORT_GUIDANCE_SCHREG_SUBCLASS_DAT ";
           $query .= " WHERE ";
           $query .= "         YEAR         = '{$model->exp_year}' ";
           $query .= "     AND SEMESTER     = '{$model->field["SEMESTER"]}' ";
           $query .= "     AND SCHREGNO     = '{$model->schregno}' ";
           $query .= "     AND CLASSCD          = '{$cd[0]}' ";
           $query .= "     AND SCHOOL_KIND      = '{$cd[1]}' ";
           $query .= "     AND CURRICULUM_CD    = '{$cd[2]}' ";
           $query .= "     AND SUBCLASSCD       = '{$cd[3]}' ";
           $query .= "     AND UNITCD           = '00' ";
           $query .= "     AND GUIDANCE_PATTERN = '2' ";
           $query .= "     AND SEQ         IN (1, 2, 3) ";

       } else {
           $dstr = str_replace("/", "-", $model->upddate);

           $query  = " DELETE FROM ";
           $query .= "     HREPORT_GUIDANCE_SCHREG_REMARK_DAT ";
           $query .= " WHERE ";
           $query .= "         YEAR         = '{$model->exp_year}' ";
           $query .= "     AND SEMESTER     = '{$model->field["SEMESTER"]}' ";
           $query .= "     AND RECORD_DATE  = '{$dstr}' ";
           $query .= "     AND SCHREGNO     = '{$model->schregno}' ";
           $query .= "     AND DIV          = '{$model->selKindNo}' ";
           $query .= "     AND SEQ          = 4 ";
       }

       $db->query($query);

       $db->commit();
       Query::dbCheckIn($db);
       return;
    }

    //DELETE -- HREPORT_GUIDANCE_SCHREG_REMARK_DAT
    function &getListDeleteQuery($model) {

        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $delList = array();
        if ($model->deleteList) {
            $delList = explode(",", $model->deleteList);
        }

        if ($model->field["INPUT_ITEMS"] == 1) {
            for ($i=0; $i < get_count($delList); $i++) { 
                list($subclass, $semester) = explode("_", $delList[$i]);
                $cd = explode("-", $subclass);

                $query  = " DELETE FROM ";
                $query .= "     HREPORT_GUIDANCE_SCHREG_SUBCLASS_DAT ";
                $query .= " WHERE ";
                $query .= "         YEAR         = '{$model->exp_year}' ";
                $query .= "     AND SEMESTER     = '{$semester}' ";
                $query .= "     AND SCHREGNO     = '{$model->schregno}' ";
                $query .= "     AND CLASSCD          = '{$cd[0]}' ";
                $query .= "     AND SCHOOL_KIND      = '{$cd[1]}' ";
                $query .= "     AND CURRICULUM_CD    = '{$cd[2]}' ";
                $query .= "     AND SUBCLASSCD       = '{$cd[3]}' ";
                $query .= "     AND UNITCD           = '00' ";
                $query .= "     AND GUIDANCE_PATTERN = '2' ";
                $query .= "     AND SEQ         IN (1, 2, 3) ";

                $db->query($query);
            }
 
        } else {
            for ($i=0; $i < get_count($delList); $i++) { 
                $semester = $delList[$i];
                $dstr = str_replace("/", "-", $model->upddate);
 
                $query  = " DELETE FROM ";
                $query .= "     HREPORT_GUIDANCE_SCHREG_REMARK_DAT ";
                $query .= " WHERE ";
                $query .= "         YEAR         = '{$model->exp_year}' ";
                $query .= "     AND SEMESTER     = '{$semester}' ";
                $query .= "     AND RECORD_DATE  = '{$dstr}' ";
                $query .= "     AND SCHREGNO     = '{$model->schregno}' ";
                $query .= "     AND DIV          = '{$model->selKindNo}' ";
                $query .= "     AND SEQ          = 4 ";

                $db->query($query);
            }
        }
  
        $db->commit();
        Query::dbCheckIn($db);
        return;
    }


    //存在チェック -- HREPORT_GUIDANCE_SCHREG_SUBCLASS_DAT
    function getHreportGuidanceSchregSubclassDatCnt($model, $subclasscd, $semester, $seq) {
        $query  = " SELECT ";
        $query .= "     COUNT ";
        $query .= " FROM ";
        $query .= "     HREPORT_GUIDANCE_SCHREG_SUBCLASS_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$model->exp_year}' ";
        $query .= "     AND SEMESTER = '{$semester}' ";
        $query .= "     AND SCHREGNO = '{$model->schregno}' ";
        $query .= "     AND CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD = '{$subclasscd}' ";
        $query .= "     AND SEQ = '{$seq}' ";

        return $query;
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

    /****************************/
    /* 「合わせた指導」教科登録 */
    /****************************/
    //教科マスタを取得
    function getVClass($model) {

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
    function checkTargetClass($model, $subclasscd, $targetSchoolKind, $targetClasscd) {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     HREPORT_GUIDANCE_SCHREG_TARGETCLASS_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$model->exp_year}' ";
        $query .= "     AND SCHREGNO = '{$model->schregno}' ";
        $query .= "     AND CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD = '{$subclasscd}' ";
        $query .= "     AND TARGET_CLASSCD = '{$targetClasscd}' ";
        $query .= "     AND TARGET_SCHOOL_KIND = '{$targetSchoolKind}' ";

        return $query;
    }

    /****************************/
    /* 「合わせた指導」教科登録 */
    /****************************/
    //INSERT -- HREPORT_GUIDANCE_SCHREG_SUBCLASS_DAT
    function &getTargetClassInsertQuery($model) {
        //DB接続
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $query  = " DELETE FROM ";
        $query .= "     HREPORT_GUIDANCE_SCHREG_TARGETCLASS_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$model->exp_year}' ";
        $query .= "     AND SCHREGNO = '{$model->schregno}' ";
        $query .= "     AND CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD = '{$model->field["SUBCLASSCD"]}' ";

        $db->query($query);

        foreach ($model->targetClassCheckValue as $val) {
            $data = array();

            $cd = explode("-", $model->targetClassField["SUBCLASSCD"]);
            $target = explode(":", $val);

            $data["YEAR"][TEXT]               = $model->exp_year;
            $data["SCHREGNO"][TEXT]           = $model->schregno;
            $data["CLASSCD"][TEXT]            = $cd[0];
            $data["SCHOOL_KIND"][TEXT]        = $cd[1];
            $data["CURRICULUM_CD"][TEXT]      = $cd[2];
            $data["SUBCLASSCD"][TEXT]         = $cd[3];
            $data["TARGET_CLASSCD"][TEXT]     = $target[0];
            $data["TARGET_SCHOOL_KIND"][TEXT] = $target[1];
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
    function getReasonableAccommodation($model) {
        $query  = " SELECT ";
        $query .= "     REASONABLE_ACCOMMODATION ";
        $query .= " FROM ";
        $query .= "     SCHREG_CHALLENGED_SUPPORTPLAN_MAIN_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$model->exp_year}' ";
        $query .= "     AND SCHREGNO = '{$model->schregno}' ";
        $query .= "     AND RECORD_DATE = '{$model->exp_year}' ";

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
