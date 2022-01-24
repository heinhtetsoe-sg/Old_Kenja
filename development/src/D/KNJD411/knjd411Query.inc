<?php

require_once('for_php7.php');

class knjd411Query extends Query {

    //グループ、履修情報取得
    function getViewGradeKindSchreg($model, $flg="")
    {
        $query  = " SELECT ";
        if ($flg == 'set') {
            $query .= " DISTINCT ";
            $query .= "     T1.GAKUBU_SCHOOL_KIND, ";
            $query .= "     T1.CONDITION, ";
            $query .= "     T1.GROUPCD ";
        } else {
            $query .= "     T1.* ";
        }
        $query .= " FROM ";
        $query .= "     V_GRADE_KIND_SCHREG_UNIT_GROUP_DAT T1 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR     = '".$model->exp_year."' ";
        $query .= " AND T1.SEMESTER = '".$model->exp_semester."' ";
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
    function getGroupcd($model, $gakubu_school_kind, $condition, $groupcd) {
        $query  = " SELECT ";
        $query .= "     GROUPNAME ";
        $query .= " FROM ";
        $query .= "     GRADE_KIND_COMP_GROUP_YMST ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".$model->exp_year."' ";
        $query .= " AND SEMESTER    = '".$model->exp_semester."' ";
        $query .= " AND GAKUBU_SCHOOL_KIND = '".$gakubu_school_kind."' ";
        $query .= " AND CONDITION   = '".$condition."' ";
        $query .= " AND GROUPCD     = '".$groupcd."' ";

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

    //科目名取得
    function getSubclassMst($model, $set_subclasscd) {
    
        $query  = " SELECT ";
        $query .= "     T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD || ' ' || T1.SUBCLASSNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_SUBCLASS_MST T1 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".$model->exp_year."' ";
        $query .= " AND T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD = '".$set_subclasscd."' ";

        return $query;
    }

    //単元情報
    function getYmstUnitName($model, $rowlist, $set_subclasscd) {
    
        $query  = " SELECT  ";
        $query .= "     UNITCD || ':' || UNITNAME AS LABEL  ";
        $query .= " FROM  ";
        $query .= "     GRADE_KIND_UNIT_GROUP_YMST ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".$model->exp_year."' ";
        $query .= " AND SEMESTER    = '".$model->exp_semester."' ";
        $query .= " AND GAKUBU_SCHOOL_KIND = '".$rowlist["GAKUBU_SCHOOL_KIND"]."' ";
        $query .= " AND CONDITION   = '".$rowlist["CONDITION"]."' ";
        $query .= " AND GROUPCD     = '".$rowlist["GROUPCD"]."' ";
        $query .= " AND CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD = '".$set_subclasscd."' ";
        $query .= " AND UNITCD      = '".$rowlist["UNITCD"]."' ";

        return $query;
    }
    
    //個別単元名
    function getDatUnitName($model, $rowlist, $set_subclasscd) {
    
        $query  = " SELECT  ";
        $query .= "     UNITNAME  ";
        $query .= " FROM  ";
        $query .= "     GRADE_KIND_SCHREG_UNIT_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".$model->exp_year."' ";
        $query .= " AND SEMESTER    = '".$model->exp_semester."' ";
        $query .= " AND SCHREGNO    = '".$model->schregno."' ";
        $query .= " AND CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD = '".$set_subclasscd."' ";
        $query .= " AND UNITCD      = '".$rowlist["UNITCD"]."' ";

        return $query;
    }

    //グループ、履修情報取得(データ)
    function getDataGradeKindSchreg($model)
    {
        $query  = " SELECT ";
        $query .= "     T1.*, ";
        $query .= "     L1.UNITNAME ";
        $query .= " FROM ";
        $query .= "     V_GRADE_KIND_SCHREG_UNIT_GROUP_DAT T1 ";
        $query .= "     LEFT JOIN GRADE_KIND_SCHREG_UNIT_DAT L1 ON L1.YEAR          = T1.YEAR ";
        $query .= "                                            AND L1.SEMESTER      = T1.SEMESTER ";
        $query .= "                                            AND L1.SCHREGNO      = T1.SCHREGNO ";
        $query .= "                                            AND L1.CLASSCD       = T1.CLASSCD ";
        $query .= "                                            AND L1.SCHOOL_KIND   = T1.SCHOOL_KIND ";
        $query .= "                                            AND L1.CURRICULUM_CD = T1.CURRICULUM_CD ";
        $query .= "                                            AND L1.SUBCLASSCD    = T1.SUBCLASSCD ";
        $query .= "                                            AND L1.UNITCD        = T1.UNITCD ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR     = '".$model->exp_year."' ";
        $query .= " AND T1.SEMESTER = '".$model->exp_semester."' ";
        $query .= " AND T1.SCHREGNO = '".$model->schregno."' ";
        $query .= " AND T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD = '".$model->getSetSubclasscd."' ";
        $query .= " AND T1.UNITCD   = '".$model->getUnitcd."' ";

        return $query;
    }

    //INSERT
    function &getInsertQuery($db, $model)
    {
        
        //GRADE_KIND_SCHREG_UNIT_DATのデータカウント
        $query  = " SELECT   ";
        $query .= "     COUNT(*) AS CNT  ";
        $query .= " FROM ";
        $query .= "     GRADE_KIND_SCHREG_UNIT_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".$model->exp_year."' ";
        $query .= " AND SEMESTER    = '".$model->exp_semester."' ";
        $query .= " AND SCHREGNO    = '".$model->schregno."' ";
        $query .= " AND CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD = '".$model->getSetSubclasscd."' ";
        $query .= " AND UNITCD      = '".$model->getUnitcd."' ";
        $getDataCount = $db->getOne($query);

        //追加
        $data = array();
        $data["UNITNAME"][TEXT]        = $model->field["UNITNAME"];
        $data["REGISTERCD"][TEXT]      = STAFFCD;
        $data["UPDATED"][FUNC]         = "sysdate()";
        
        if ($getDataCount == 0) {
            $setSubclassArray = array();
            $setSubclassArray = explode("-", $model->getSetSubclasscd);
            
            $data["YEAR"][TEXT]             = $model->exp_year;
            $data["SEMESTER"][TEXT]         = $model->exp_semester;
            $data["SCHREGNO"][TEXT]         = $model->schregno;
            $data["CLASSCD"][TEXT]          = $setSubclassArray[0];
            $data["SCHOOL_KIND"][TEXT]      = $setSubclassArray[1];
            $data["CURRICULUM_CD"][TEXT]    = $setSubclassArray[2];
            $data["SUBCLASSCD"][TEXT]       = $setSubclassArray[3];
            $data["UNITCD"][TEXT]           = $model->getUnitcd;

            $db->query(Query::insertSQL($data, "GRADE_KIND_SCHREG_UNIT_DAT"));
        } else {
            $where  = " WHERE ";
            $where .= "     YEAR        = '".$model->exp_year."' ";
            $where .= " AND SEMESTER    = '".$model->exp_semester."' ";
            $where .= " AND SCHREGNO    = '".$model->schregno."' ";
            $where .= " AND CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD = '".$model->getSetSubclasscd."' ";
            $where .= " AND UNITCD      = '".$model->getUnitcd."' ";
        
            $db->query(Query::updateSQL($data, "GRADE_KIND_SCHREG_UNIT_DAT", $where));
        }

        return ;
    }

    //DELETE
    function &getDeleteQuery($db, $model)
    {
        $query  = " DELETE FROM ";
        $query .= "     GRADE_KIND_SCHREG_UNIT_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".$model->exp_year."' ";
        $query .= " AND SEMESTER    = '".$model->exp_semester."' ";
        $query .= " AND SCHREGNO    = '".$model->schregno."' ";
        $query .= " AND CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD = '".$model->getSetSubclasscd."' ";
        $query .= " AND UNITCD      = '".$model->getUnitcd."' ";
        $db->query($query);
        return ;
    }
}
?>
