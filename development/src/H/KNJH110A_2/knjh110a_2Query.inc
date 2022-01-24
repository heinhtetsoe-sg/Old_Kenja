<?php

require_once('for_php7.php');

class knjh110a_2Query extends Query {

    //生徒名
    function getStudentName($model)
    {
        $query  = " SELECT ";
        $query .= "     SCHREGNO AS VALUE, ";
        $query .= "     SCHREGNO || ':' || NAME_SHOW AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_BASE_MST ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '{$model->schregNo}' ";
        return $query;
    }

    //学校校種取得
    function getSchoolKind($model) {
        $query  = "SELECT ";
        $query .= "        SCHOOL_KIND ";
        $query .= "FROM ";
        $query .= "        SCHREG_REGD_GDAT ";
        $query .= "WHERE ";
        $query .= "        GRADE = '".$model->getGrade."' ";
        $query .= "AND      YEAR = '".CTRL_YEAR."' ";
        
        return $query;
    }

    //科目名称取得
    function getName($model, $schoolkind)
    {
        $query  = "SELECT ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD AS VALUE, ";
            $query .= "     CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD || ':' || SUBCLASSNAME AS LABEL ";
        } else {
            $query .= "     SUBCLASSCD AS VALUE, ";
            $query .= "     SUBCLASSCD || ':' || SUBCLASSNAME AS LABEL ";
        }
        $query .= "FROM ";
        $query .= "        SUBCLASS_MST ";
        if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "WHERE";
                $query .= "        SCHOOL_KIND = '".$schoolkind."' ";
        }
        $query .= "ORDER BY ";
        $query .= "    VALUE";
        
        return $query;
    }

    //リスト表示および入力項目
    function getList($model, $input = "") {
        $query  = " SELECT ";
        $query .= "     SEQ, ";
        $query .= "     REGDDATE, ";
        $query .= "     MINUTES, ";
        $query .= "     REMARK ";
        $query .= " FROM ";
        $query .= "     SCHREG_QUALIFIED_BANK_DAT ";
        $query .= " WHERE ";
        $query .= "         SCHREGNO = '{$model->schregNo}' ";
        
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     AND CLASSCD         = '{$model->classCd}' ";
            $query .= "     AND SCHOOL_KIND     = '{$model->schoolkind}' ";
            $query .= "     AND CURRICULUM_CD   = '{$model->curriculum_Cd}' ";
            $query .= "     AND SUBCLASSCD      = '{$model->useCurriculum_subclassCd}' ";
        } else {
            $query .= "     AND SUBCLASSCD      = '{$model->subclassCd}' ";
        }
        
        if (strlen($input)) {
            $query .= "     AND SEQ = {$model->seq} ";
        }
        $query .= " ORDER BY ";
        $query .= "     REGDDATE, ";
        $query .= "     SEQ ";
        return $query;
    }

    //クレジットバンキング登録済みの科目リスト
    function getSubclassList($model) {

        $sDate = (CTRL_YEAR - 1) ."-04-01";

        $query  = " SELECT ";
        $query .= "     T1.SUBCLASSCD, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD || ':' || L1.SUBCLASSNAME AS SUBCLASSNAME, ";
        } else {
            $query .= "     T1.SUBCLASSCD || ':' || L1.SUBCLASSNAME AS SUBCLASSNAME, ";
        }
        $query .= "     SUM(CASE WHEN T1.REGDDATE >= DATE('{$sDate}') THEN T1.MINUTES ELSE 0 END) AS TOTAL ";
        $query .= " FROM ";
        $query .= "     SCHREG_QUALIFIED_BANK_DAT T1 ";
        $query .= "     LEFT JOIN SUBCLASS_MST L1 ON L1.SUBCLASSCD    = T1.SUBCLASSCD ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .="                               AND L1.CLASSCD       = T1.CLASSCD ";
            $query .="                               AND L1.SCHOOL_KIND   = T1.SCHOOL_KIND ";
            $query .="                               AND L1.CURRICULUM_CD = T1.CURRICULUM_CD ";
        }
        $query .= " WHERE ";
        $query .= "     T1.SCHREGNO = '{$model->schregNo}' ";
        $query .= " GROUP BY ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T1.CLASSCD, ";
            $query .= "     T1.SCHOOL_KIND, ";
            $query .= "     T1.CURRICULUM_CD, ";
        }
        $query .= "     T1.SUBCLASSCD, ";
        $query .= "     L1.SUBCLASSNAME ";
        $query .= " ORDER BY ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T1.CLASSCD, ";
            $query .= "     T1.SCHOOL_KIND, ";
            $query .= "     T1.CURRICULUM_CD, ";
        }
        $query .= "     T1.SUBCLASSCD ";

        return $query;
    }

    //MAX_SEQ
    function getMaxSeq($model)
    {
        $query  = " SELECT ";
        $query .= "     MAX(VALUE(SEQ,0)) + 1 AS SEQ ";
        $query .= " FROM ";
        $query .= "     SCHREG_QUALIFIED_BANK_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '{$model->schregNo}' ";
        
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     AND CLASSCD         = '{$model->classCd}' ";
            $query .= "     AND SCHOOL_KIND     = '{$model->schoolkind}' ";
            $query .= "     AND CURRICULUM_CD   = '{$model->curriculum_Cd}' ";
            $query .= "     AND SUBCLASSCD      = '{$model->useCurriculum_subclassCd}' ";
        } else {
            $query .= "     AND SUBCLASSCD      = '{$model->subclassCd}' ";
        }
        
        $query .= " GROUP BY ";
        $query .= "     SCHREGNO, ";
        $query .= "     SUBCLASSCD ";
        return $query;
    }

    //追加
    function insertQuery($model)
    {
        $db = Query::dbCheckOut();

        //MAX_SEQ
        $query = knjh110a_2Query::getMaxSeq($model);
        $seq = $db->getOne($query);
        if (!strlen($seq)) $seq = 1;
        //項目
        $data = array();
        $data["SCHREGNO"][TEXT]         = $model->schregNo;
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $data["SUBCLASSCD"][TEXT]       = $model->useCurriculum_subclassCd;
            $data["CLASSCD"][TEXT]          = SUBSTR($model->subclassCd,0,2) ;
            $data["SCHOOL_KIND"][TEXT]      = $model->schoolkind ;
            $data["CURRICULUM_CD"][TEXT]    = $model->curriculum_Cd ;
        } else {
            $data["SUBCLASSCD"][TEXT]       = $model->subclassCd;
        }
        
        $data["SEQ"][NUMBER]            = $seq;
        $data["REGDDATE"][TEXT]         = str_replace("/", "-", $model->field["REGDDATE"]);
        $data["REGDYEAR"][TEXT]         = common::DateConv1(str_replace("-", "/", $model->field["REGDDATE"]), 12);
        $data["MINUTES"][NUMBER]        = $model->field["MINUTES"];
        $data["REMARK"][TEXT]           = $model->field["REMARK"];
        $data["REGISTERCD"][TEXT]       = STAFFCD ;
        $data["UPDATED"][FUNC]          = "sysdate()";
        //追加
        $query = Query::insertSQL($data, "SCHREG_QUALIFIED_BANK_DAT");
        $db->query($query);
        Query::dbCheckIn($db);

        return $seq; //MAX_SEQ
    }

    //更新
    function updateQuery($model)
    {
        $db = Query::dbCheckOut();

        //項目
        $data = array();
        $data["REGDDATE"][TEXT]         = str_replace("/", "-", $model->field["REGDDATE"]);
        $data["REGDYEAR"][TEXT]         = common::DateConv1(str_replace("-", "/", $model->field["REGDDATE"]), 12);
        $data["MINUTES"][NUMBER]        = $model->field["MINUTES"];
        $data["REMARK"][TEXT]           = $model->field["REMARK"];
        $data["REGISTERCD"][TEXT]       = STAFFCD ;
        $data["UPDATED"][FUNC]          = "sysdate()";
        //条件
        $where  = " WHERE ";
        $where .= "         SCHREGNO        = '{$model->schregNo}' ";
        
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $where .= "     AND CLASSCD         = '{$model->classCd}' ";
            $where .= "     AND CURRICULUM_CD   = '{$model->curriculum_Cd}' ";
            $where .= "     AND SCHOOL_KIND     = '{$model->schoolkind}' ";
            $where .= "     AND SUBCLASSCD      = '{$model->useCurriculum_subclassCd}' ";
        } else {
            $where .= "     AND SUBCLASSCD      = '{$model->subclassCd}' ";
        }
        $where .= "     AND SEQ             = {$model->seq} ";
        
        //更新
        $query = Query::updateSQL($data, "SCHREG_QUALIFIED_BANK_DAT", $where);
        $db->query($query);

        Query::dbCheckIn($db);
    }

    //削除
    function deleteQuery($model)
    {
        $db = Query::dbCheckOut();

        //削除
        $query  = " DELETE FROM SCHREG_QUALIFIED_BANK_DAT ";
        $query .= " WHERE ";
        $query .= "         SCHREGNO    = '{$model->schregNo}' ";
        
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     AND CLASSCD         = '{$model->classCd}' ";
            $query .= "     AND SCHOOL_KIND     = '{$model->schoolkind}' ";
            $query .= "     AND CURRICULUM_CD   = '{$model->curriculum_Cd}' ";
            $query .= "     AND SUBCLASSCD      = '{$model->useCurriculum_subclassCd}' ";
        } else {
            $query .= "     AND SUBCLASSCD      = '{$model->subclassCd}' ";
        }
        
        $query .= "     AND SEQ         = {$model->seq} ";
        $db->query($query);

        Query::dbCheckIn($db);
    }
}
?>