<?php

require_once('for_php7.php');

class knjl256cQuery extends Query {

    //学校コードを取得
    function getNameMstZ010() {
        $query  = " SELECT ";
        $query .= "     CASE WHEN NAME1 = 'CHIBEN' THEN NAME2 ELSE NULL END AS SCHOOLCODE ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'Z010' AND ";
        $query .= "     NAMECD2 = '00' ";

        $db = Query::dbCheckOut();
        $rtn = $db->getOne($query);
        Query::dbCheckIn($db);

        return $rtn;
    }

    //プレテスト区分
    function getPreTestdiv($model, $csvOutput="")
    {
        $query  = " SELECT ";
        if ($csvOutput) {
            $query .= "     NAME1, ";
        }
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL, ";
        $query .= "     NAMECD2 AS VALUE ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR    = '{$model->ObjYear}' AND ";
        $query .= "     NAMECD1 = 'L104' ";
        if ($csvOutput) {
            $query .= "     AND NAMECD2 = '{$model->field["PRE_TESTDIV"]}' ";
        }

        return $query;
    }

    //入試制度取得
    function getApctDiv($model)
    {
        $query  = " SELECT ";
        $query .= "     NAME1 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR    = '{$model->ObjYear}' AND ";
        $query .= "     NAMECD1 = 'L103' AND ";
        $query .= "     NAMECD2 = '1' ";

        return $query;
    }

    //科目取得
    function getTestSubclass($model)
    {
        $query .= " SELECT ";
        $query .= "     NAMECD2 AS SUBCLASSCD, ";
        $query .= "     NAME1 AS SUBCLASSNAME ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$model->ObjYear}' ";
        $query .= "     AND NAMECD1 = 'L109' ";
        $query .= "     AND ABBV3 = '{$model->field["PRE_TESTDIV"]}' ";
        $query .= " ORDER BY ";
        $query .= "     NAMECD2 ";

        return $query;
    }

    //CSV
    function getCsvQuery($model)
    {
        $query .= " SELECT ";
        $query .= "     T1.PRE_RECEPTNO, ";
        $query .= "     T1.PRE_EXAM_TYPE, ";
        $query .= "     NML105.NAME1 AS PRE_EXAM_TYPE_NAME, ";
        $query .= "     T1.NAME, ";
        $query .= "     T1.SEX, ";
        $query .= "     NMZ002.ABBV1 AS SEX_NAME, ";
        $query .= "     T1.FS_CD, ";
        $query .= "     VALUE(L1.FINSCHOOL_NAME_ABBV, L1.FINSCHOOL_NAME) AS FINSCHOOL_NAME, ";
        $query .= "     T1.ZIPCD, ";
        $query .= "     L3.PREF AS FINSCHOOL_PREF, ";
        $query .= "     T1.PS_CONTACT, ";
        $query .= "     T1.PS_CD, ";
        $query .= "     L2.PRISCHOOL_NAME, ";
        foreach ($model->testSubclass as $key => $val) {
            $query .= "     SCORE_T{$val["SUBCLASSCD"]}.ATTEND_FLG AS ATTEND_FLG{$val["SUBCLASSCD"]}, ";
            $query .= "     SCORE_T{$val["SUBCLASSCD"]}.SCORE AS SCORE{$val["SUBCLASSCD"]}, ";
        }
        $query .= "     T2.TOTAL2, ";
        $query .= "     T2.AVARAGE2, ";
        $query .= "     T2.TOTAL_RANK2, ";
        $query .= "     T2.TOTAL4, ";
        $query .= "     T2.AVARAGE4, ";
        $query .= "     T2.DIV_RANK4, ";
        if ($model->isGojo) {
            $query .= "     T2.TOTAL3, ";
            $query .= "     T2.AVARAGE3, ";
            $query .= "     T2.DIV_RANK3, ";
        }
//        $query .= "     CASE WHEN T2.TOTAL2 IS NULL THEN '*' ELSE RTRIM(CHAR(T2.TOTAL2)) END AS TOTAL2, ";
//        $query .= "     CASE WHEN T2.TOTAL2 IS NULL THEN '*' ELSE RTRIM(CHAR(T2.AVARAGE2)) END AS AVARAGE2, ";
//        $query .= "     CASE WHEN T2.TOTAL2 IS NULL THEN '*' ELSE RTRIM(CHAR(T2.TOTAL_RANK2)) END AS TOTAL_RANK2, ";
//        $query .= "     CASE WHEN T1.PRE_EXAM_TYPE = '1' THEN (CASE WHEN T2.TOTAL4 IS NULL THEN '*' ELSE RTRIM(CHAR(T2.TOTAL4)) END) ELSE '' END AS TOTAL4, ";
//        $query .= "     CASE WHEN T1.PRE_EXAM_TYPE = '1' THEN (CASE WHEN T2.TOTAL4 IS NULL THEN '*' ELSE RTRIM(CHAR(T2.AVARAGE4)) END) ELSE '' END AS AVARAGE4, ";
//        $query .= "     CASE WHEN T1.PRE_EXAM_TYPE = '1' THEN (CASE WHEN T2.TOTAL4 IS NULL THEN '*' ELSE RTRIM(CHAR(T2.DIV_RANK4)) END) ELSE '' END AS DIV_RANK4, ";
        $query .= "     T1.REMARK ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_PRE_DAT T1 ";
        $query .= "     LEFT JOIN ENTEXAM_RECEPT_PRE_DAT T2 ";
        $query .= "         ON  T2.ENTEXAMYEAR  = T1.ENTEXAMYEAR ";
        $query .= "         AND T2.APPLICANTDIV = T1.APPLICANTDIV ";
        $query .= "         AND T2.PRE_RECEPTNO = T1.PRE_RECEPTNO ";
        $query .= "     LEFT JOIN FINSCHOOL_MST L1 ON L1.FINSCHOOLCD = T1.FS_CD ";
        $query .= "     LEFT JOIN PRISCHOOL_MST L2 ON L2.PRISCHOOLCD = T1.PS_CD ";
        $query .= "     LEFT JOIN NAME_MST NMZ002 ON NMZ002.NAMECD1 = 'Z002' AND NMZ002.NAMECD2 = T1.SEX ";
        $query .= "     LEFT JOIN NAME_MST NML105 ON NML105.NAMECD1 = 'L105' AND NML105.NAMECD2 = T1.PRE_EXAM_TYPE ";
        $query .= "     LEFT JOIN ( ";
        $query .= "         SELECT ";
        $query .= "             NEW_ZIPCD, ";
        $query .= "             PREF ";
        $query .= "         FROM ";
        $query .= "             ZIPCD_MST ";
        $query .= "         GROUP BY ";
        $query .= "             NEW_ZIPCD, ";
        $query .= "             PREF ";
        $query .= "         ) L3 ON L3.NEW_ZIPCD = T1.ZIPCD ";
        foreach ($model->testSubclass as $key => $val) {
            $query .= "     LEFT JOIN ENTEXAM_SCORE_PRE_DAT SCORE_T{$val["SUBCLASSCD"]} ON SCORE_T{$val["SUBCLASSCD"]}.ENTEXAMYEAR = T1.ENTEXAMYEAR ";
            $query .= "          AND SCORE_T{$val["SUBCLASSCD"]}.APPLICANTDIV = T1.APPLICANTDIV ";
            $query .= "          AND SCORE_T{$val["SUBCLASSCD"]}.PRE_RECEPTNO = T1.PRE_RECEPTNO ";
            $query .= "          AND SCORE_T{$val["SUBCLASSCD"]}.TESTSUBCLASSCD = '{$val["SUBCLASSCD"]}' ";
        }
        $query .= " WHERE ";
        $query .= "     T1.ENTEXAMYEAR = '{$model->ObjYear}' ";
        $query .= "     AND T1.APPLICANTDIV = '{$model->field["APPLICANTDIV"]}' ";
        $query .= "     AND T1.PRE_TESTDIV = '{$model->field["PRE_TESTDIV"]}' ";
        $query .= " ORDER BY ";
        $query .= "     T1.PRE_RECEPTNO ";

        return $query;
    }
}
?>
