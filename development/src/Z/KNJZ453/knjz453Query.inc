<?php

require_once('for_php7.php');

class knjz453Query extends Query {
/************ 左画面 ***************/
    //年度コンボ
    function getYear($model) {
        $query .= " WITH T_ALL (LABEL, VALUE) AS ( ";
        $query .= "      VALUES('".CTRL_YEAR."', '".CTRL_YEAR."') ";
        $query .= "      ) ";
        $query .= " SELECT ";
        $query .= "      LABEL, ";
        $query .= "      VALUE ";
        $query .= " FROM ";
        $query .= "      T_ALL ";
        $query .= " UNION ";
        $query .= " SELECT DISTINCT ";
        $query .= "     YEAR AS LABEL, ";
        $query .= "     YEAR AS VALUE ";
        $query .= " FROM ";
        $query .= "     SUBCLASS_QUALIFIED_RELATION_MST ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //科目データの取得
    function getSubclassCd($model) {
        $query  = " SELECT ";
        $query .= "     CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD AS VALUE, ";
        $query .= "     CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD || '：' || SUBCLASSNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_SUBCLASS_MST T3 ";
        $query .= " WHERE ";
        $query .= "         YEAR       = '".$model->leftYear."' ";
        $query .= "     AND SUBCLASSCD NOT LIKE '9%' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "     AND SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= "     AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " GROUP BY ";
        $query .= "     CLASSCD, ";
        $query .= "     SCHOOL_KIND, ";
        $query .= "     CURRICULUM_CD, ";
        $query .= "     SUBCLASSCD, ";
        $query .= "     SUBCLASSNAME ";
        $query .= " ORDER BY ";
        $query .= "     CLASSCD, ";
        $query .= "     SCHOOL_KIND, ";
        $query .= "     CURRICULUM_CD, ";
        $query .= "     SUBCLASSCD ";

        return $query;
    }

    //学年コンボ
    function getGrade($model) {
        list($classCd, $schKind, $cirrCd, $subclassCd) = explode('-', $model->subclassCd);

        $query  = " SELECT DISTINCT ";
        $query .= "     GRADE AS VALUE, ";
        $query .= "     GRADE_NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT ";
        $query .= " WHERE ";
        $query .= "         YEAR        = '".$model->leftYear."' ";
        $query .= "     AND SCHOOL_KIND = '".$schKind."' ";
        $query .= " ORDER BY ";
        $query .= "     GRADE ";

        return $query;
    }

    //資格コンボ
    function getQualifiedMst() {
        $query  = " SELECT DISTINCT ";
        $query .= "     QUALIFIED_CD AS VALUE, ";
        $query .= "     QUALIFIED_CD || ':' || QUALIFIED_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     QUALIFIED_MST ";
        $query .= " WHERE ";
        $query .= "     MANAGEMENT_FLG = '1' ";
        $query .= " ORDER BY ";
        $query .= "     QUALIFIED_CD ";

        return $query;
    }

    //リスト
    function getList($model) {
        $query  = " SELECT ";
        $query .= "     RELA.RESULT_CD, ";
        $query .= "     REST.RESULT_NAME, ";
        $query .= "     RELA.SCORE, ";
        $query .= "     RELA.CREDIT ";
        $query .= " FROM ";
        $query .= "     SUBCLASS_QUALIFIED_RELATION_MST RELA ";
        $query .= "     LEFT JOIN QUALIFIED_RESULT_MST REST ";
        $query .= "         ON RELA.YEAR         = REST.YEAR ";
        $query .= "        AND RELA.QUALIFIED_CD = REST.QUALIFIED_CD ";
        $query .= "        AND RELA.RESULT_CD    = REST.RESULT_CD ";
        $query .= " WHERE ";
        $query .= "         RELA.YEAR         = '".$model->leftYear."' ";
        $query .= "     AND RELA.CLASS_CD || '-' || RELA.SCHOOL_KIND || '-' || RELA.CURRICULUM_CD || '-' || RELA.SUBCLASS_CD = '".$model->subclassCd."' ";
        $query .= "     AND RELA.GRADE        = '".$model->grade."' ";
        $query .= "     AND RELA.QUALIFIED_CD = '".$model->sikakuCd."' ";
        $query .= " ORDER BY ";
        $query .= "     RELA.RESULT_CD ";

        return $query;
    }

/************ 右画面 ***************/
    //１レコード取得
    function getRow($model, $Cd) {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     SUBCLASS_QUALIFIED_RELATION_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR         = '".$model->leftYear."' ";
        $query .= "     AND QUALIFIED_CD = '".$model->sikakuCd."' ";
        $query .= "     AND CLASS_CD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASS_CD = '".$model->subclassCd."' ";
        $query .= "     AND GRADE   = '".$model->grade."' ";
        $query .= "     AND RESULT_CD    = '".$Cd."' ";

        return $query;
    }

    //結果CD取得
    function getResultCd($model) {
        $query  = " SELECT DISTINCT ";
        $query .= "     RESULT_CD AS VALUE, ";
        $query .= "     RESULT_CD || '：' || RESULT_NAME AS LABEL  ";
        $query .= " FROM ";
        $query .= "     QUALIFIED_RESULT_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR         = '".$model->leftYear."' ";
        $query .= "     AND QUALIFIED_CD = '".$model->sikakuCd."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

/************ コピー ***************/
    //前年度のSUBCLASS_QUALIFIED_RELATION_MSTデータを消去
    function &getCopyDelRelationMstQuery($model) {
        $db = Query::dbCheckOut();

        $query  = " DELETE FROM ";
        $query .= "     SUBCLASS_QUALIFIED_RELATION_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".($model->leftYear + 1)."' ";

        $db->query($query);
        Query::dbCheckIn($db);
        return;
    }

    //前年度からのコピー(SUBCLASS_QUALIFIED_RELATION_MST)
    function &getCopyRelationMstQuery($model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $query  = " INSERT INTO SUBCLASS_QUALIFIED_RELATION_MST ";
        $query .= " SELECT ";
        $query .= "     '".($model->leftYear + 1)."' AS YEAR, ";
        $query .= "     CLASS_CD, ";
        $query .= "     SCHOOL_KIND, ";
        $query .= "     CURRICULUM_CD, ";
        $query .= "     SUBCLASS_CD, ";
        $query .= "     GRADE, ";
        $query .= "     QUALIFIED_CD, ";
        $query .= "     RESULT_CD, ";
        $query .= "     SCORE, ";
        $query .= "     CREDIT, ";
        $query .= "     '".STAFFCD."', ";
        $query .= "     sysdate() ";
        $query .= " FROM ";
        $query .= "     SUBCLASS_QUALIFIED_RELATION_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$model->leftYear."' ";

        $db->query($query);
        $db->commit();
        Query::dbCheckIn($db);
        return true;
   }

/******** 追加更新処理  ********/
    //--- INSERT
    function &getInsertQuery($model, $fields) {
        list($classCd, $schKind, $cirrCd, $subclassCd) = explode('-', $model->subclassCd);

        $db = Query::dbCheckOut();

        $data = array();
        $data["YEAR"][TEXT]               = $model->leftYear;
        $data["CLASS_CD"][TEXT]           = $classCd;
        $data["SCHOOL_KIND"][TEXT]        = $schKind;
        $data["CURRICULUM_CD"][TEXT]      = $cirrCd;
        $data["SUBCLASS_CD"][TEXT]        = $subclassCd;
        $data["GRADE"][TEXT]              = $model->grade;
        $data["QUALIFIED_CD"][TEXT]       = $model->sikakuCd;
        $data["RESULT_CD"][TEXT]          = $fields["RESULT_CD"];
        $data["SCORE"][NUMBER]            = $fields["SCORE"];
        $data["CREDIT"][NUMBER]           = $fields["CREDIT"];
        $data["REGISTERCD"][TEXT]         = STAFFCD;
        $data["UPDATED"][NUMBER]          = "sysdate()";

        $query = Query::insertSQL($data, "SUBCLASS_QUALIFIED_RELATION_MST");
        $db->query($query);
        Query::dbCheckIn($db);
        return;
    }

    //--- UPDATE 
    function &getUpdateQuery($model, $fields) {
        $db = Query::dbCheckOut();

        $data = array();
        $data["SCORE"][NUMBER]            = $fields["SCORE"];
        $data["CREDIT"][NUMBER]           = $fields["CREDIT"];
        $data["REGISTERCD"][TEXT]         = STAFFCD;
        $data["UPDATED"][NUMBER]          = "sysdate()";

        $where  = " WHERE ";
        $where .= "         YEAR         = '".$model->leftYear."' ";
        $where .= "     AND CLASS_CD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASS_CD = '".$model->subclassCd."' ";
        $where .= "     AND GRADE        = '".$model->grade."' ";
        $where .= "     AND QUALIFIED_CD = '".$model->sikakuCd."' ";
        $where .= "     AND RESULT_CD    = '".$model->resultCd."' ";

        $query = Query::updateSQL($data, "SUBCLASS_QUALIFIED_RELATION_MST", $where);
        $db->query($query);
        Query::dbCheckIn($db);
        return;
    }

    //DELETE
    function &getDeleteQuery($model, $fields) {
        $db = Query::dbCheckOut();

        $query  = " DELETE FROM ";
        $query .= "     SUBCLASS_QUALIFIED_RELATION_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR         = '".$model->leftYear."' ";
        $query .= "     AND CLASS_CD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASS_CD = '".$model->subclassCd."' ";
        $query .= "     AND GRADE        = '".$model->grade."' ";
        $query .= "     AND QUALIFIED_CD = '".$model->sikakuCd."' ";
        $query .= "     AND RESULT_CD    = '".$model->resultCd."' ";

        $db->query($query);
        Query::dbCheckIn($db);
        return;
    }
}
?>
