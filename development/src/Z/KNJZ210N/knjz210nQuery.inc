<?php

require_once('for_php7.php');

class knjz210nQuery extends Query {
    //年度一覧取得
    function getYear() {
        $query  = " WITH THIS_YEAR(LABEL, VALUE) AS (VALUES('".CTRL_YEAR."', '".CTRL_YEAR."')) ";
        $query .= " SELECT ";
        $query .= "     LABEL, ";
        $query .= "     VALUE ";
        $query .= " FROM ";
        $query .= "     THIS_YEAR ";
        $query .= " UNION ";
        $query .= " SELECT DISTINCT ";
        $query .= "     YEAR AS LABEL, ";
        $query .= "     YEAR AS VALUE ";
        $query .= " FROM ";
        $query .= "     IBVIEW_CUTTING_DAT ";
        $query .= " WHERE ";
        $query .= "     DATA_DIV = '2' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE DESC ";

        return $query;
    }

    //左フレームのリスト
    function getList($model) {
        $query  = " SELECT ";
        $query .= "     T1.YEAR, ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.IBPRG_COURSE, ";
        $query .= "     T1.CLASSCD, ";
        $query .= "     T1.SCHOOL_KIND, ";
        $query .= "     T1.CURRICULUM_CD, ";
        $query .= "     T1.SUBCLASSCD, ";
        $query .= "     L2.SUBCLASSNAME, ";
        $query .= "     L1.NAMECD2, ";
        $query .= "     L1.ABBV1, ";
        $query .= "     L3.GRADE_NAME1, ";
        $query .= "     COUNT(*) AS SEQ_CNT ";
        $query .= " FROM ";
        $query .= "     IBVIEW_CUTTING_DAT T1 ";
        $query .= "     LEFT JOIN NAME_MST L1 ON L1.NAMECD1 = 'A034' ";
        $query .= "                          AND L1.NAME1   = T1.IBPRG_COURSE ";
        $query .= "     LEFT JOIN SUBCLASS_MST L2 ON L2.CLASSCD         = T1.CLASSCD ";
        $query .= "                              AND L2.SCHOOL_KIND     = T1.SCHOOL_KIND ";
        $query .= "                              AND L2.CURRICULUM_CD   = T1.CURRICULUM_CD ";
        $query .= "                              AND L2.SUBCLASSCD      = T1.SUBCLASSCD ";
        $query .= "     LEFT JOIN SCHREG_REGD_GDAT L3 ON L3.YEAR    = T1.YEAR ";
        $query .= "                                  AND L3.GRADE   = T1.GRADE ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR     = '".$model->year."' AND ";
        $query .= "     T1.DATA_DIV = '2' ";
        $query .= " GROUP BY ";
        $query .= "     T1.YEAR, ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.IBPRG_COURSE, ";
        $query .= "     T1.CLASSCD, ";
        $query .= "     T1.SCHOOL_KIND, ";
        $query .= "     T1.CURRICULUM_CD, ";
        $query .= "     T1.SUBCLASSCD, ";
        $query .= "     L2.SUBCLASSNAME, ";
        $query .= "     L1.NAMECD2, ";
        $query .= "     L1.ABBV1, ";
        $query .= "     L3.GRADE_NAME1 ";
        $query .= " ORDER BY ";
        $query .= "     T1.YEAR, ";
        $query .= "     T1.GRADE, ";
        $query .= "     L1.NAMECD2, ";
        $query .= "     T1.IBPRG_COURSE, ";
        $query .= "     T1.SCHOOL_KIND, ";
        $query .= "     T1.CURRICULUM_CD, ";
        $query .= "     T1.CLASSCD, ";
        $query .= "     T1.SUBCLASSCD ";

        return $query;
    }

    //行数取得
    function getRowDataCnt($row, $flg) {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "    (SELECT DISTINCT ";
        $query .= "         YEAR, ";
        $query .= "         GRADE, ";
        $query .= "         IBPRG_COURSE, ";
        $query .= "         CLASSCD, ";
        $query .= "         SCHOOL_KIND, ";
        $query .= "         CURRICULUM_CD, ";
        $query .= "         SUBCLASSCD ";
        $query .= "     FROM ";
        $query .= "         IBVIEW_CUTTING_DAT ";
        $query .= "     WHERE ";
        $query .= "         DATA_DIV = '2') T1 ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".$row["YEAR"]."' AND ";
        $query .= "     GRADE       = '".$row["GRADE"]."' ";
        if ($flg == "2") {
            $query .= "     AND ";
            $query .= "     IBPRG_COURSE    = '".$row["IBPRG_COURSE"]."' ";
        }

        return $query;
    }

    //IBVIEW_CUTTING_DATからデータ取得
    function getIBViewCuttingDat($year, $grade, $ibprg_course, $classcd, $school_kind, $curriculum_cd, $subclasscd, $seq, $flg) {
        $query  = " SELECT DISTINCT ";
        if ($flg == "row") {
            $query .= "     GRADE, ";
            $query .= "     IBPRG_COURSE, ";
            $query .= "     CLASSCD, ";
            $query .= "     SCHOOL_KIND, ";
            $query .= "     CURRICULUM_CD, ";
            $query .= "     SUBCLASSCD ";
        } else {
            $query .= "     * ";
        }
        $query .= " FROM ";
        $query .= "     IBVIEW_CUTTING_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR            = '".$year."' AND ";
        $query .= "     GRADE           = '".$grade."' AND ";
        $query .= "     IBPRG_COURSE    = '".$ibprg_course."' AND ";
        $query .= "     CLASSCD         = '".$classcd."' AND ";
        $query .= "     SCHOOL_KIND     = '".$school_kind."' AND ";
        $query .= "     CURRICULUM_CD   = '".$curriculum_cd."' AND ";
        $query .= "     SUBCLASSCD      = '".$subclasscd."' AND ";
        $query .= "     DATA_DIV        = '2' ";
        if ($flg == "list") {
            $query .= "     AND ";
            $query .= "     SEQ           = ".$seq." ";
        }

        return $query;
    }

    //学年一覧取得
    function getGrade($model, $flg) {
        $query  = " SELECT ";
        if ($flg == "check") {
            $query .= "     COUNT(*) ";
        } else {
            $query .= "     GRADE AS VALUE, ";
            $query .= "     GRADE_NAME1 AS LABEL ";
        }
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$model->year."' ";
        if ($flg == "list") {
            $query .= " ORDER BY ";
            $query .= "     VALUE ";
        }

        return $query;
    }

    //IBコース一覧取得
    function getIBPrgCourse($model, $flg) {
        if ($model->cmd === 'edit' && $model->grade != "") {
            $set_grade = $model->grade;
        } else {
            $set_grade = $model->field["GRADE"];
        }
    
        $query  = " SELECT ";
        if ($flg == "check") {
            $query .= "     COUNT(*) ";
        } else {
            $query .= "     NAME1 AS VALUE, ";
            $query .= "     ABBV1 AS LABEL ";
        }
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR    = '".$model->year."' ";
        $query .= " AND NAMECD1 = 'A034' ";
        $query .= " AND '".$set_grade."' BETWEEN NAME2 AND NAME3 ";
        if ($flg == "list") {
            $query .= " ORDER BY ";
            $query .= "     NAMECD2 ";
        }

        return $query;
    }

    //科目一覧取得
    function getSubclasscd($model, $flg) {
        if ($flg == "check") {
            $query  = " SELECT ";
            $query .= "     COUNT(*) ";
        } else {
            $query  = " WITH STANDARD(CLASSCD, SCHOOL_KIND, CURRICULUM_CD, SUBCLASSCD) AS (VALUES('00','00','00','000000')) ";
            $query .= " SELECT ";
            $query .= "     CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD AS VALUE, ";
            $query .= "     CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD || ' 基本設定' AS LABEL, ";
            $query .= "     CLASSCD, ";
            $query .= "     SCHOOL_KIND, ";
            $query .= "     CURRICULUM_CD, ";
            $query .= "     SUBCLASSCD ";
            $query .= " FROM ";
            $query .= "     STANDARD ";
            $query .= " UNION ";
            $query .= " SELECT ";
            $query .= "     CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD AS VALUE, ";
            $query .= "     CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD || ' ' || SUBCLASSNAME AS LABEL, ";
            $query .= "     CLASSCD, ";
            $query .= "     SCHOOL_KIND, ";
            $query .= "     CURRICULUM_CD, ";
            $query .= "     SUBCLASSCD ";
        }
        $query .= " FROM ";
        $query .= "     V_SUBCLASS_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$model->year."' ";
        if ($flg == "list") {
            $query .= " ORDER BY ";
            $query .= "     SCHOOL_KIND, ";
            $query .= "     CURRICULUM_CD, ";
            $query .= "     CLASSCD, ";
            $query .= "     SUBCLASSCD ";
        }

        return $query;
    }

    /**********/
    /* INSERT */
    /**********/
    function &getInsertQuery($model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //削除
        $query  = " DELETE FROM ";
        $query .= "     IBVIEW_CUTTING_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR            = '".$model->year."' AND ";
        $query .= "     GRADE           = '".$model->field["GRADE"]."' AND ";
        $query .= "     IBPRG_COURSE    = '".$model->field["IBPRG_COURSE"]."' AND ";
        $query .= "     CLASSCD         = '".$model->field["CLASSCD"]."' AND ";
        $query .= "     SCHOOL_KIND     = '".$model->field["SCHOOL_KIND"]."' AND ";
        $query .= "     CURRICULUM_CD   = '".$model->field["CURRICULUM_CD"]."' AND ";
        $query .= "     SUBCLASSCD      = '".$model->field["SUBCLASSCD"]."' AND ";
        $query .= "     DATA_DIV        = '2' ";

        $db->query($query);

        //追加
        $pre_high = "";
        for ($i = 1; $i <= 5; $i++) {
            $data = array();
            $data["YEAR"][TEXT]             = $model->year;
            $data["GRADE"][TEXT]            = $model->field["GRADE"];
            $data["IBPRG_COURSE"][TEXT]     = $model->field["IBPRG_COURSE"];
            $data["CLASSCD"][TEXT]          = $model->field["CLASSCD"];
            $data["SCHOOL_KIND"][TEXT]      = $model->field["SCHOOL_KIND"];
            $data["CURRICULUM_CD"][TEXT]    = $model->field["CURRICULUM_CD"];
            $data["SUBCLASSCD"][TEXT]       = $model->field["SUBCLASSCD"];
            $data["DATA_DIV"][TEXT]         = '2';
            $data["SEQ"][NUMBER]            = $i;
            $data["CUTTING_LOW"][NUMBER]    = ($i == 1) ? 0 : (1.0 * $pre_high + 0.1);
            $data["CUTTING_HIGH"][NUMBER]   = $model->field2["CUTTING_HIGH_".$i];
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][NUMBER]        = "sysdate()";

            $query = Query::insertSQL($data, "IBVIEW_CUTTING_DAT");
            $db->query($query);

            $pre_high = $model->field2["CUTTING_HIGH_".$i];
        }
        $db->commit();
        Query::dbCheckIn($db);
    }

    /**********/
    /* DELETE */
    /**********/
    function &getDeleteQuery($model) {
        $db = Query::dbCheckOut();

        $query  = " DELETE FROM ";
        $query .= "     IBVIEW_CUTTING_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR            = '".$model->year."' AND ";
        $query .= "     GRADE           = '".$model->field["GRADE"]."' AND ";
        $query .= "     IBPRG_COURSE    = '".$model->field["IBPRG_COURSE"]."' AND ";
        $query .= "     CLASSCD         = '".$model->field["CLASSCD"]."' AND ";
        $query .= "     SCHOOL_KIND     = '".$model->field["SCHOOL_KIND"]."' AND ";
        $query .= "     CURRICULUM_CD   = '".$model->field["CURRICULUM_CD"]."' AND ";
        $query .= "     SUBCLASSCD      = '".$model->field["SUBCLASSCD"]."' AND ";
        $query .= "     DATA_DIV        = '2' ";

        $db->query($query);
        Query::dbCheckIn($db);
    }

    //IBVIEW_CUTTING_DAT存在チェック
    function checkIBViewCuttingDat($year) {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     IBVIEW_CUTTING_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".$year."' AND ";
        $query .= "     DATA_DIV    = '2' ";

        return $query;
    }

    //前年度データ取得
    function getPreYearData($model) {
        $query  = " SELECT DISTINCT ";
        $query .= "     T1.* ";
        $query .= " FROM ";
        $query .= "     IBVIEW_CUTTING_DAT T1, ";
        $query .= "     SCHREG_REGD_GDAT T2, ";
        $query .= "     V_NAME_MST T3, ";
        $query .= "     V_SUBCLASS_MST T4 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR             = '".($model->year-1)."' AND ";
        $query .= "     T1.DATA_DIV         = '2' AND ";
        $query .= "     T2.YEAR             = '".$model->year."' AND ";
        $query .= "     T1.GRADE            = T2.GRADE AND ";
        $query .= "     T3.NAMECD1          = 'A034' AND ";
        $query .= "     T3.YEAR             = '".$model->year."' AND ";
        $query .= "     T1.IBPRG_COURSE     = T3.NAME1 AND ";
        $query .= "     T4.YEAR             = '".$model->year."' AND ";
        $query .= "     T1.CLASSCD          = T4.CLASSCD AND ";
        $query .= "     T1.SCHOOL_KIND      = T4.SCHOOL_KIND AND ";
        $query .= "     T1.CURRICULUM_CD    = T4.CURRICULUM_CD AND ";
        $query .= "     T1.SUBCLASSCD       = T4.SUBCLASSCD ";
        $query .= " UNION ";
        $query .= " SELECT DISTINCT ";
        $query .= "     T1.* ";
        $query .= " FROM ";
        $query .= "     IBVIEW_CUTTING_DAT T1, ";
        $query .= "     SCHREG_REGD_GDAT T2, ";
        $query .= "     V_NAME_MST T3 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR             = '".($model->year-1)."' AND ";
        $query .= "     T1.DATA_DIV         = '2' AND ";
        $query .= "     T2.YEAR             = '".$model->year."' AND ";
        $query .= "     T1.GRADE            = T2.GRADE AND ";
        $query .= "     T3.NAMECD1          = 'A034' AND ";
        $query .= "     T3.YEAR             = '".$model->year."' AND ";
        $query .= "     T1.IBPRG_COURSE     = T3.NAME1 AND ";
        $query .= "     T1.CLASSCD          = '00' AND ";
        $query .= "     T1.SCHOOL_KIND      = '00' AND ";
        $query .= "     T1.CURRICULUM_CD    = '00' AND ";
        $query .= "     T1.SUBCLASSCD       = '000000' ";

        return $query;
    }

    /********/
    /* COPY */
    /********/
    function &getCopyQuery($model) {
        $db = Query::dbCheckOut();

        //削除
        $query  = " DELETE FROM ";
        $query .= "     IBVIEW_CUTTING_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".$model->year."' AND ";
        $query .= "     DATA_DIV    = '2' ";
        $db->query($query);

        //前年度データ
        $query = knjz210nQuery::getPreYearData($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //追加
            $data = array();
            $data["YEAR"][TEXT]             = $model->year;
            $data["GRADE"][TEXT]            = $row["GRADE"];
            $data["IBPRG_COURSE"][TEXT]     = $row["IBPRG_COURSE"];
            $data["CLASSCD"][TEXT]          = $row["CLASSCD"];
            $data["SCHOOL_KIND"][TEXT]      = $row["SCHOOL_KIND"];
            $data["CURRICULUM_CD"][TEXT]    = $row["CURRICULUM_CD"];
            $data["SUBCLASSCD"][TEXT]       = $row["SUBCLASSCD"];
            $data["DATA_DIV"][TEXT]         = $row["DATA_DIV"];
            $data["SEQ"][NUMBER]            = $row["SEQ"];
            $data["CUTTING_MARK"][TEXT]     = $row["CUTTING_MARK"];
            $data["CUTTING_LOW"][NUMBER]    = $row["CUTTING_LOW"];
            $data["CUTTING_HIGH"][NUMBER]   = $row["CUTTING_HIGH"];
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][NUMBER]        = "sysdate()";

            $query = Query::insertSQL($data, "IBVIEW_CUTTING_DAT");
            $db->query($query);
        }
        Query::dbCheckIn($db);
    }
}
?>
