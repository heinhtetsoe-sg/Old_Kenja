<?php

require_once('for_php7.php');

class knjm720Query extends Query {
    //年度一覧取得
    function getYear() {
        $query  = " SELECT ";
        $query .= "     '".CTRL_YEAR."' AS LABEL, ";
        $query .= "     '".CTRL_YEAR."' AS VALUE ";
        $query .= " FROM ";
        $query .= "     SYSIBM.SYSDUMMY1 ";
        $query .= " UNION ";
        $query .= " SELECT ";
        $query .= "     '".(CTRL_YEAR + 1)."' AS LABEL, ";
        $query .= "     '".(CTRL_YEAR + 1)."' AS VALUE ";
        $query .= " FROM ";
        $query .= "     SYSIBM.SYSDUMMY1 ";
        $query .= " UNION ";
        $query .= " SELECT DISTINCT ";
        $query .= "     YEAR AS LABEL, ";
        $query .= "     YEAR AS VALUE ";
        $query .= " FROM ";
        $query .= "     COMP_CREDITS_PATTERN_DAT ";
        $query .= " ORDER BY ";
        $query .= "     VALUE DESC ";

        return $query;
    }

    //履修パターンカウント
    function getCompCreditsPatternCnt($year) {

        $query  = " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     COMP_CREDITS_PATTERN_MST T1, ";
        $query .= "     COMP_CREDITS_PATTERN_DAT T2 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".$year."' ";
        $query .= "     AND T1.YEAR = T2.YEAR ";
        $query .= "     AND T1.PATTERN_CD   = T2.PATTERN_CD ";
        $query .= "     AND T2.SCHOOL_KIND  = 'H' ";

        return $query;
    }

    //履修パターンリスト
    function getCompCreditsPatternList($model) {

        $query  = " SELECT ";
        $query .= "     T1.PATTERN_CD, ";
        $query .= "     T1.PATTERN_NAME, ";
        $query .= "     T2.CLASSCD, ";
        $query .= "     T2.SCHOOL_KIND, ";
        $query .= "     T2.CURRICULUM_CD, ";
        $query .= "     T2.SUBCLASSCD, ";
        $query .= "     T3.SUBCLASSNAME ";
        $query .= " FROM ";
        $query .= "     COMP_CREDITS_PATTERN_MST T1, ";
        $query .= "     COMP_CREDITS_PATTERN_DAT T2 ";
        $query .= "     LEFT JOIN SUBCLASS_MST T3 ON T3.SUBCLASSCD      = T2.SUBCLASSCD ";
        $query .= "                              AND T3.CLASSCD         = T2.CLASSCD ";
        $query .= "                              AND T3.SCHOOL_KIND     = T2.SCHOOL_KIND ";
        $query .= "                              AND T3.CURRICULUM_CD   = T2.CURRICULUM_CD ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".$model->year."' ";
        $query .= "     AND T1.YEAR = T2.YEAR ";
        $query .= "     AND T1.PATTERN_CD   = T2.PATTERN_CD ";
        $query .= "     AND T2.SCHOOL_KIND  = 'H' ";
        $query .= " ORDER BY ";
        $query .= "     T1.PATTERN_CD, ";
        $query .= "     T2.SUBCLASSCD ";

        return $query;
    }

    //名称マスタより取得
    function getNamecd($model, $cd, $flg="") {
        $query  = " SELECT ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR    = '".$model->year."' AND ";
        $query .= "     NAMECD1 = '".$cd."' ";
        if ($flg) {
            $query .= "     AND '".$model->year."' BETWEEN NAMESPARE1 AND NAMESPARE2 ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //履修パターン情報
    function GetGroup($model, $groupcd) {

        $query .= " SELECT ";
        $query .= "     T1.PATTERN_CD, ";
        $query .= "     T1.CLASSCD, ";
        $query .= "     T1.SCHOOL_KIND, ";
        $query .= "     T1.CURRICULUM_CD, ";
        $query .= "     NM.NAME1 AS CURRI_NAME, ";
        $query .= "     T1.SUBCLASSCD, ";
        $query .= "     SUB_C.SUBCLASSNAME, ";
        $query .= "     T1.COMP_FLG ";
        $query .= " FROM ";
        $query .= "     COMP_CREDITS_PATTERN_DAT T1 ";
        $query .= "     LEFT JOIN SUBCLASS_MST SUB_C ON T1.CLASSCD = SUB_C.CLASSCD ";
        $query .= "          AND T1.SCHOOL_KIND = SUB_C.SCHOOL_KIND ";
        $query .= "          AND T1.CURRICULUM_CD = SUB_C.CURRICULUM_CD ";
        $query .= "          AND T1.SUBCLASSCD = SUB_C.SUBCLASSCD ";
        $query .= "     LEFT JOIN NAME_MST NM ON NM.NAMECD1 = 'Z018' ";
        $query .= "          AND T1.CURRICULUM_CD = NM.NAMECD2 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".$model->year."' ";
        $query .= "     AND T1.PATTERN_CD = '".$model->field["PATTERN_CD"]."' ";
        $query .= " UNION ";
        $query .= " SELECT ";
        $query .= "     '999' AS PATTERN_CD, ";
        $query .= "     SUB_C.CLASSCD, ";
        $query .= "     SUB_C.SCHOOL_KIND, ";
        $query .= "     SUB_C.CURRICULUM_CD, ";
        $query .= "     NM.NAME1 AS CURRI_NAME, ";
        $query .= "     SUB_C.SUBCLASSCD, ";
        $query .= "     SUB_C.SUBCLASSNAME, ";
        $query .= "     '' AS COMP_FLG ";
        $query .= " FROM ";
        $query .= "     V_SUBCLASS_MST SUB_C ";
        $query .= "     LEFT JOIN NAME_MST NM ON NM.NAMECD1 = 'Z018' ";
        $query .= "          AND SUB_C.CURRICULUM_CD = NM.NAMECD2 ";
        $query .= " WHERE ";
        $query .= "     SUB_C.YEAR = '".$model->year."' ";
        $query .= "     AND SUB_C.SCHOOL_KIND = 'H' ";
        $query .= "     AND NOT EXISTS( ";
        $query .= "                 SELECT ";
        $query .= "                     'x' ";
        $query .= "                 FROM ";
        $query .= "                     COMP_CREDITS_PATTERN_DAT T1 ";
        $query .= "                 WHERE ";
        $query .= "                     T1.YEAR = '".$model->year."' ";
        $query .= "                     AND T1.PATTERN_CD = '".$model->field["PATTERN_CD"]."' ";
        $query .= "                     AND T1.CLASSCD = SUB_C.CLASSCD ";
        $query .= "                     AND T1.SCHOOL_KIND = SUB_C.SCHOOL_KIND ";
        $query .= "                     AND T1.CURRICULUM_CD = SUB_C.CURRICULUM_CD ";
        $query .= "                     AND T1.SUBCLASSCD = SUB_C.SUBCLASSCD ";
        $query .= "     ) ";
        $query .= " ORDER BY ";
        $query .= "     PATTERN_CD, ";
        $query .= "     CLASSCD, ";
        $query .= "     SCHOOL_KIND, ";
        $query .= "     CURRICULUM_CD, ";
        $query .= "     SUBCLASSCD, ";
        $query .= "     SUBCLASSNAME, ";
        $query .= "     COMP_FLG ";
        return $query;
    }

    //SUBCLASS_MSTより取得
    function getSubclassRow($classcd, $school_kind, $curriculum_cd, $subclasscd) {
        $query  = " SELECT ";
        $query .= "     SUBCLASSNAME ";
        $query .= " FROM ";
        $query .= "     SUBCLASS_MST ";
        $query .= " WHERE ";
        $query .= "     CLASSCD         = '".$classcd."' AND ";
        $query .= "     SCHOOL_KIND     = '".$school_kind."' AND ";
        $query .= "     CURRICULUM_CD   = '".$curriculum_cd."' AND ";
        $query .= "     SUBCLASSCD      = '".$subclasscd."' ";
        
        return $query;
    }

    //科目マスタより取得
    function getSubClass($curriculum_cd) {

        $query  = " SELECT ";
        $query .= "     CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD || ' ' || SUBCLASSNAME AS LABEL, ";
        $query .= "     CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD AS VALUE ";
        $query .= " FROM ";
        $query .= "     SUBCLASS_MST ";
        $query .= " WHERE ";
        $query .= "     SCHOOL_KIND     = 'H' AND ";
        $query .= "     CURRICULUM_CD   = '".$curriculum_cd."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //COMP_CREDITS_PATTERN_MSTより取得
    function getPatternRow($model, $cd) {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     COMP_CREDITS_PATTERN_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$model->year."' ";
        $query .= "     AND PATTERN_CD = '".$cd."' ";

        return $query;
    }

    //追加
    function &InsertQuery($model, $fields) {
        $db = Query::dbCheckOut();

        //COMP_CREDITS_PATTERN_MSTテーブル更新
        $data["YEAR"][TEXT]         = $model->year;
        $data["PATTERN_CD"][TEXT]   = $fields["PATTERN_CD"];
        $data["PATTERN_NAME"][TEXT] = $fields["PATTERN_NAME"];

        $data["REGISTERCD"][TEXT]   = STAFFCD;
        $data["UPDATED"][FUNC]      = "sysdate()";

        $query = Query::insertSQL($data, "COMP_CREDITS_PATTERN_MST");
        $db->query($query);

        $db->commit();
        Query::dbCheckIn($db);
    }

    //追加
    function InsertDatQuery($model, $fields, $setSubData) {
        $db = Query::dbCheckOut();

        foreach ($setSubData as $key => $valArray) {
            list($classcd, $school_kind, $curriculum_cd, $subclasscd) = preg_split("/-/", $key);

            $data["YEAR"][TEXT]             = $model->year;
            $data["PATTERN_CD"][TEXT]       = $fields["PATTERN_CD"];
            $data["CLASSCD"][TEXT]          = $classcd;
            $data["SCHOOL_KIND"][TEXT]      = $school_kind;
            $data["CURRICULUM_CD"][TEXT]    = $curriculum_cd;
            $data["SUBCLASSCD"][TEXT]       = $subclasscd;
            $data["COMP_FLG"][TEXT]         = $valArray["COMP_FLG"];

            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][FUNC]          = "sysdate()";

            $query = Query::insertSQL($data, "COMP_CREDITS_PATTERN_DAT");
            $db->query($query);
        }

        $db->commit();
        Query::dbCheckIn($db);
    }

    //更新
    function &UpdateQuery($model, $fields) {
        $db = Query::dbCheckOut();

        //COMP_CREDITS_PATTERN_MSTテーブル更新
        $data["YEAR"][TEXT]         = $model->year;
        $data["PATTERN_CD"][TEXT]   = $fields["PATTERN_CD"];
        $data["PATTERN_NAME"][TEXT] = $fields["PATTERN_NAME"];

        $data["REGISTERCD"][TEXT]   = STAFFCD;
        $data["UPDATED"][FUNC]      = "sysdate()";

        $where  = " WHERE PATTERN_CD = '".$fields["PATTERN_CD"]."' ";
        $where .= "   AND YEAR = '".$model->year."' ";

        $query = Query::updateSQL($data, "COMP_CREDITS_PATTERN_MST", $where);
        $db->query($query);

        $db->commit();
        Query::dbCheckIn($db);
    }

    //COMP_CREDITS_PATTERN_MST削除
    function &DeleteQuery($model, $fields) {
        $db = Query::dbCheckOut();

        $query  = " DELETE FROM ";
        $query .= "     COMP_CREDITS_PATTERN_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$model->year."' ";
        $query .= "     AND PATTERN_CD = '".$fields["PATTERN_CD"]."' ";

        $db->query($query);

        Query::dbCheckIn($db);
    }

    //COMP_CREDITS_PATTERN_DAT削除
    function &DeleteDatQuery($model, $cd) {
        $db = Query::dbCheckOut();

        $query  = " DELETE FROM ";
        $query .= "     COMP_CREDITS_PATTERN_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$model->year."' ";
        $query .= "     AND PATTERN_CD = '".$cd."' ";

        $db->query($query);

        Query::dbCheckIn($db);
    }

    //前年度からコピー
    function &CopyQuery($model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $query  = " INSERT INTO ";
        $query .= "     COMP_CREDITS_PATTERN_MST ";
        $query .= " SELECT ";
        $query .= "     '".$model->year."', ";
        $query .= "     PATTERN_CD, ";
        $query .= "     PATTERN_NAME, ";
        $query .= "     '".STAFFCD."', ";
        $query .= "     sysdate() ";
        $query .= " FROM ";
        $query .= "     COMP_CREDITS_PATTERN_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".($model->year - 1)."' ";

        $db->query($query);

        $query  = " INSERT INTO ";
        $query .= "     COMP_CREDITS_PATTERN_DAT ";
        $query .= " SELECT ";
        $query .= "     '".$model->year."', ";
        $query .= "     PATTERN_CD, ";
        $query .= "     CLASSCD, ";
        $query .= "     SCHOOL_KIND, ";
        $query .= "     CURRICULUM_CD, ";
        $query .= "     SUBCLASSCD, ";
        $query .= "     COMP_FLG, ";
        $query .= "     '".STAFFCD."', ";
        $query .= "     sysdate() ";
        $query .= " FROM ";
        $query .= "     COMP_CREDITS_PATTERN_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".($model->year - 1)."' ";

        $db->query($query);

        $db->commit();
        Query::dbCheckIn($db);
    }
}
?>
