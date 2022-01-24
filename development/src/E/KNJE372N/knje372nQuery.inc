<?php
class knje372nQuery extends Query {

    //年度
    function getYear($year_name){
        if($year_name == "OYEAR"){
            $query .= "  WITH YEAR_T(VALUE, LABEL) AS ( ";
            $query .= "    VALUES('".CTRL_YEAR."', '".CTRL_YEAR."') ";
            $query .= "  ) ";
            $query .= "  SELECT ";
            $query .= "    * ";
            $query .= "  FROM ";
            $query .= "    YEAR_T ";
            $query .= "  UNION ";
        }
        $query .= "  SELECT DISTINCT ";
        $query .= "    YEAR AS LABEL, ";
        $query .= "    YEAR AS VALUE ";
        $query .= "  FROM ";
        $query .= "    AFT_RECOMMENDATION_ASSESS_AVG_DAT ";
        $query .= "  ORDER BY ";
        $query .= "    VALUE ";

        return $query;
    }

    //推薦枠番号
    function getAftRecommendationLimitMst($year, $recommendation_cd = "") {
        $query  = "  WITH MAIN AS( ";
        $query .= "    SELECT ";
        $query .= "      RECOMMENDATION_CD, ";
        $query .= "      CASE WHEN DEPARTMENT_S IS NOT NULL ";
        $query .= "           THEN '専願'||LPAD(DEPARTMENT_S,2,'0') ";
        $query .= "           ELSE '' ";
        $query .= "      END AS DEPARTMENT_S, ";
        $query .= "      CASE WHEN DEPARTMENT_H IS NOT NULL ";
        $query .= "           THEN '併願'||LPAD(DEPARTMENT_H,2,'0') ";
        $query .= "           ELSE '' ";
        $query .= "      END AS DEPARTMENT_H ";
        $query .= "    FROM ";
        $query .= "          AFT_RECOMMENDATION_LIMIT_MST ";
        $query .= "    WHERE ";
        $query .= "         YEAR = '".$year."' ";
        if($recommendation_cd){
            $query .= "     AND RECOMMENDATION_CD = '".$recommendation_cd."' ";
        }
        $query .= " ) ";
        $query .= "  SELECT ";
        $query .= "    RECOMMENDATION_CD AS VALUE, ";
        $query .= "    CASE WHEN DEPARTMENT_S <> '' AND DEPARTMENT_H <> '' ";
        $query .= "         THEN RECOMMENDATION_CD ||':'|| DEPARTMENT_S ||','|| DEPARTMENT_H ";
        $query .= "         ELSE RECOMMENDATION_CD ||':'|| DEPARTMENT_S || DEPARTMENT_H ";
        $query .= "    END AS LABEL ";
        $query .= "    FROM ";
        $query .= "      MAIN ";
        $query .= "  ORDER BY ";
        $query .= "      VALUE ";

        return $query;
    }

    //教科コンボ
    function getClassMst($year) {
        $query  = "  SELECT ";
        $query .= "    CLASSCD || SCHOOL_KIND AS VALUE, ";
        $query .= "    CLASSCD || '-' || SCHOOL_KIND || '-' || CLASSNAME AS LABEL ";
        $query .= "  FROM ";
        $query .= "    V_CLASS_MST ";
        $query .= "  WHERE ";
        $query .= "        YEAR        = '".$year."' ";
        $query .= "    AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        $query .= "  ORDER BY ";
        $query .= "    VALUE ";

        return $query;
    }

    //リストtoリスト
    function getSubclassList($model, $row, $select = ""){
        if($select == "CHECK"){
            $classcd     = $row["CLASSCD"];
            $school_kind = $row["SCHOOL_KIND"];
        } else {
            $classcd     = $model->field["CLASSCD"];
            $school_kind = $model->field["SCHOOL_KIND"];
        }

        $query  = "  SELECT DISTINCT ";
        $query .= "    T2.RECOMMENDATION_CD, ";
        $query .= "    T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD || ':' || T1.SUBCLASSNAME AS LABEL, ";
        $query .= "    T1.CLASSCD || T1.SCHOOL_KIND || T1.CURRICULUM_CD || T1.SUBCLASSCD AS VALUE ";
        $query .= "  FROM ";
        $query .= "    V_SUBCLASS_MST T1 ";
        $query .= "  LEFT JOIN ";
        $query .= "    AFT_RECOMMENDATION_ASSESS_AVG_SUBCLASS_DAT T2 ON T1.YEAR              = T2.YEAR ";
        $query .= "                                                 AND T1.CLASSCD           = T2.CLASSCD ";
        $query .= "                                                 AND T1.SCHOOL_KIND       = T2.SCHOOL_KIND ";
        $query .= "                                                 AND T1.CURRICULUM_CD     = T2.CURRICULUM_CD ";
        $query .= "                                                 AND T1.SUBCLASSCD        = T2.SUBCLASSCD";
        $query .= "                                                 AND T2.RECOMMENDATION_CD = '".$model->field["RECOMMENDATION_CD"]."' ";
        if($model->field["SEQ"]){
            $query .= "                                             AND T2.SEQ               = '".$model->field["SEQ"]."' ";
        }
        $query .= "  WHERE ";
        $query .= "          T1.YEAR        = '".$model->oyear."' ";
        $query .= "      AND T1.CLASSCD     = '".$classcd."' ";
        $query .= "      AND T1.SCHOOL_KIND = '".$school_kind."' ";
        if($select == "CHECK"){
            $query .= "  AND T1.CURRICULUM_CD = '".$row["CURRICULUM_CD"]."' ";
            $query .= "  AND T1.SUBCLASSCD    = '".$row["SUBCLASSCD"]."' ";
        }
        $query .= "  ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //一覧表示(左画面)
    function getSelectQuery($model){
        $query  = "  SELECT DISTINCT ";
        $query .= "    T1.RECOMMENDATION_CD, ";
        $query .= "    T2.DEPARTMENT_S, ";
        $query .= "    T2.DEPARTMENT_H, ";
        $query .= "    T1.SEQ, ";
        $query .= "    CAST(T1.ASSESS_AVG AS DECIMAL(6,1)) AS ASSESS_AVG, ";
        $query .= "    T3.CLASSCD || T3.SCHOOL_KIND AS CLASSCD ";
        $query .= "  FROM ";
        $query .= "    AFT_RECOMMENDATION_ASSESS_AVG_DAT T1 ";
        $query .= "  LEFT JOIN ";
        $query .= "    AFT_RECOMMENDATION_LIMIT_MST T2 ON T1.YEAR              = T2.YEAR ";
        $query .= "                                   AND T1.RECOMMENDATION_CD = T2.RECOMMENDATION_CD ";
        $query .= "  LEFT JOIN ";
        $query .= "    AFT_RECOMMENDATION_ASSESS_AVG_SUBCLASS_DAT T3 ON T1.YEAR              = T3.YEAR ";
        $query .= "                                                 AND T1.RECOMMENDATION_CD = T3.RECOMMENDATION_CD ";
        $query .= "                                                 AND T1.SEQ = T3.SEQ ";
        $query .= "  WHERE ";
        $query .= "    T1.YEAR = '".$model->oyear."' ";
        $query .= "  ORDER BY ";
        $query .= "    T1.RECOMMENDATION_CD, ";
        $query .= "    T1.SEQ ";

        return $query;
    }

    //科目取得(左画面)
    function getSubclassName($year, $row){
        $query  = "  SELECT ";
        $query .= "    T2.SUBCLASSNAME ";
        $query .= "  FROM ";
        $query .= "    AFT_RECOMMENDATION_ASSESS_AVG_SUBCLASS_DAT T1 ";
        $query .= "  LEFT JOIN ";
        $query .= "    V_SUBCLASS_MST T2 ON T1.YEAR          = T2.YEAR ";
        $query .= "                     AND T1.CLASSCD       = T2.CLASSCD ";
        $query .= "                     AND T1.SCHOOL_KIND   = T2.SCHOOL_KIND ";
        $query .= "                     AND T1.CURRICULUM_CD = T2.CURRICULUM_CD ";
        $query .= "                     AND T1.SUBCLASSCD    = T2.SUBCLASSCD";
        $query .= "  WHERE ";
        $query .= "        T1.YEAR                      = '".$year."' ";
        $query .= "    AND T1.RECOMMENDATION_CD         = '".$row["RECOMMENDATION_CD"]."' ";
        $query .= "    AND T1.SEQ                       = '".$row["SEQ"]."' ";
        $query .= "  ORDER BY ";
        $query .= "    T1.SUBCLASSCD ";

        return $query;
    }

    //存在チェック
    function getCountData($model)
    {
        $query  = "  SELECT ";
        $query .= "    COUNT(*) ";
        $query .= "  FROM ";
        $query .= "    AFT_RECOMMENDATION_ASSESS_AVG_DAT T1 ";
        $query .= "  LEFT JOIN ";
        $query .= "    AFT_RECOMMENDATION_ASSESS_AVG_SUBCLASS_DAT T2 ON T1.YEAR  = T2.YEAR ";   
        $query .= "                                                 AND T1.RECOMMENDATION_CD   = T2.RECOMMENDATION_CD ";
        $query .= "                                                 AND T1.SEQ      = T2.SEQ ";
        $query .= "                                                 AND T2.CLASSCD || T2.SCHOOL_KIND = '".$model->field["CLASSCD"]."' ";
        $query .= "  WHERE ";
        $query .= "        T1.YEAR              = '".$model->oyear."' ";
        $query .= "    AND T1.RECOMMENDATION_CD = '".$model->field["RECOMMENDATION_CD"]."' ";
        $query .= "    AND T1.SEQ               = '".$model->field["SEQ"]."' ";

        return $query;
    }

    //最大SEQ取得
    function getMaxSeq($model)
    {
        $query  = "  SELECT ";
        $query .= "    MAX(SEQ) ";
        $query .= "  FROM ";
        $query .= "    AFT_RECOMMENDATION_ASSESS_AVG_DAT T1 ";
        $query .= "  WHERE ";
        $query .= "        T1.YEAR              = '".$model->oyear."' ";
        $query .= "    AND T1.RECOMMENDATION_CD = '".$model->field["RECOMMENDATION_CD"]."' ";

        return $query;
    }

    //削除処理
    function &getDeleteQuery($model, &$db, $table)
    {
        $query  = " DELETE FROM ".$table." ";
        $query .= " WHERE ";
        $query .= "       YEAR              = '".$model->oyear."' ";
        $query .= "   AND RECOMMENDATION_CD = '".$model->field["RECOMMENDATION_CD"]."' ";
        $query .= "   AND SEQ               = '".$model->field["SEQ"]."' ";
        $db->query($query);
        return;
    }

    //追加・更新処理(AFT_RECOMMENDATION_ASSESS_AVG_DAT)
    function &getUpdateQueryAvg($model,&$db){
        $data = array();
        $data["YEAR"][TEXT]              = $model->oyear;
        $data["RECOMMENDATION_CD"][TEXT] = $model->field["RECOMMENDATION_CD"];
        $data["SEQ"][TEXT]               = $model->field["SEQ"];
        $data["ASSESS_AVG"][TEXT]        = $model->field["ASSESS_AVG"];
        $data["REGISTERCD"][TEXT]        = STAFFCD;
        $data["UPDATED"][FUNC]           = "sysdate()";

        $query = Query::insertSQL($data, "AFT_RECOMMENDATION_ASSESS_AVG_DAT");
        $db->query($query);
        return;
    }

    //追加・更新処理(AFT_RECOMMENDATION_ASSESS_AVG_SUBCLASS_DAT)
    function &getUpdateQuerySubclass($model,&$db){
        if ($model->selectLeft) {
            $selectSchArr = explode(",", $model->selectLeft);
            foreach ($selectSchArr as $selectSchCd) {
                $data = array();
                $data["YEAR"][TEXT]              = $model->oyear;
                $data["RECOMMENDATION_CD"][TEXT] = $model->field["RECOMMENDATION_CD"];
                $data["SEQ"][TEXT]               = $model->field["SEQ"];
                $data["CLASSCD"][TEXT]           = substr($selectSchCd, 0, 2);
                $data["SCHOOL_KIND"][TEXT]       = substr($selectSchCd, 2, 1);
                $data["CURRICULUM_CD"][TEXT]     = substr($selectSchCd, 3, 1);
                $data["SUBCLASSCD"][TEXT]        = substr($selectSchCd, 4, 6);
                $data["REGISTERCD"][TEXT]        = STAFFCD;
                $data["UPDATED"][FUNC]           = "sysdate()";

                $query = Query::insertSQL($data, "AFT_RECOMMENDATION_ASSESS_AVG_SUBCLASS_DAT");
                $db->query($query);
            }
        }
        return;
    }

    //年度データ取得(AFT_RECOMMENDATION_ASSESS_AVG_DAT)
    function &getCopyDataAvg($year) {
        $query  =  " SELECT ";
        $query .=  "   * ";
        $query .=  " FROM ";
        $query .=  "   AFT_RECOMMENDATION_ASSESS_AVG_DAT ";
        $query .=  " WHERE ";
        $query .=  "   YEAR = '".$year."' ";

        return $query;
    }

    //年度データ取得(AFT_RECOMMENDATION_ASSESS_AVG_SUBCLASS_DAT)
    function &getCopyDataSubclass($year,$row) {
        $query  =  " SELECT ";
        $query .=  "   * ";
        $query .=  " FROM ";
        $query .=  "   AFT_RECOMMENDATION_ASSESS_AVG_SUBCLASS_DAT ";
        $query .=  " WHERE ";
        $query .=  "       YEAR = '".$year."' ";
        $query .=  "   AND RECOMMENDATION_CD = '".$row["RECOMMENDATION_CD"]."' ";
        $query .=  "   AND SEQ  = '".$row["SEQ"]."' ";

        return $query;
    }

    //コピー処理(AFT_RECOMMENDATION_ASSESS_AVG_DAT)
    function &getCopyQueryAvg($model, $row, &$db){
        $data = array();
        $data["YEAR"][TEXT]              = $model->oyear;
        $data["RECOMMENDATION_CD"][TEXT] = $row["RECOMMENDATION_CD"];
        $data["SEQ"][TEXT]               = $row["SEQ"];
        $data["ASSESS_AVG"][TEXT]        = $row["ASSESS_AVG"];
        $data["REGISTERCD"][TEXT]        = STAFFCD;
        $data["UPDATED"][FUNC]           = "sysdate()";

        $query = Query::insertSQL($data, "AFT_RECOMMENDATION_ASSESS_AVG_DAT");
        $db->query($query);
        return;
    }

    //コピー処理(AFT_RECOMMENDATION_ASSESS_AVG_SUBCLASS_DAT)
    function &getCopyQuerySubclass($model, $row, &$db){
        $data = array();
        $data["YEAR"][TEXT]              = $model->oyear;
        $data["RECOMMENDATION_CD"][TEXT] = $row["RECOMMENDATION_CD"];
        $data["SEQ"][TEXT]               = $row["SEQ"];
        $data["CLASSCD"][TEXT]           = $row["CLASSCD"];
        $data["SCHOOL_KIND"][TEXT]       = $row["SCHOOL_KIND"];
        $data["CURRICULUM_CD"][TEXT]     = $row["CURRICULUM_CD"];
        $data["SUBCLASSCD"][TEXT]        = $row["SUBCLASSCD"];
        $data["REGISTERCD"][TEXT]        = STAFFCD;
        $data["UPDATED"][FUNC]           = "sysdate()";

        $query = Query::insertSQL($data, "AFT_RECOMMENDATION_ASSESS_AVG_SUBCLASS_DAT");
        $db->query($query);
        return;
    }
}
?>
