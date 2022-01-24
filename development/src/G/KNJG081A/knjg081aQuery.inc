<?php

require_once('for_php7.php');


class knjg081aquery extends Query {

    //年組一覧取得
    function getGradeHrclass()
    {
        $query  = " SELECT ";
        $query .= "     GRADE || HR_CLASS AS VALUE, ";
        $query .= "     HR_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     SEMESTER = '".CTRL_SEMESTER."' ";
        if(AUTHORITY == DEF_UPDATE_RESTRICT || $AUTHORITY == DEF_REFER_RESTRICT) {
            $query .= "     AND (TR_CD1     = '".STAFFCD."' OR ";
            $query .= "          TR_CD2     = '".STAFFCD."' OR ";
            $query .= "          TR_CD3     = '".STAFFCD."' OR ";
            $query .= "          SUBTR_CD1  = '".STAFFCD."' OR ";
            $query .= "          SUBTR_CD1  = '".STAFFCD."' OR ";
            $query .= "          SUBTR_CD1  = '".STAFFCD."') ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //全体のデータの取得
    function selectQuery($model)
    {
        $query  = " WITH SCHINFO AS ( ";
        $query .= "     SELECT ";
        $query .= "         T1.SCHREGNO, ";
        $query .= "         T1.ATTENDNO, ";
        $query .= "         T2.NAME ";
        $query .= "     FROM ";
        $query .= "         SCHREG_REGD_DAT T1, ";
        $query .= "         SCHREG_BASE_MST T2 ";
        $query .= "     WHERE ";
        $query .= "         T1.YEAR = '".CTRL_YEAR."' AND ";
        $query .= "         T1.SEMESTER = '".CTRL_SEMESTER."' AND ";
        $query .= "         T1.GRADE || T1.HR_CLASS = '".$model->field["GRADE_HR_CLASS"]."' AND ";
        $query .= "         T1.SCHREGNO = T2.SCHREGNO ";
        $query .= " ) ";

        $query .= " SELECT ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T1.ATTENDNO, ";
        $query .= "     T1.NAME, ";
        $query .= "     T2.PRINT_CNT ";
        $query .= " FROM ";
        $query .= "     SCHINFO T1 ";
        $query .= "     LEFT JOIN CERTIF_SCH_PRINT_CNT_DAT T2 ";
        $query .= "             ON T1.SCHREGNO = T2.SCHREGNO ";
        $query .= "            AND T2.CERTIF_KINDCD = '008' ";
        $query .= " ORDER BY ";
        $query .= "     T1.ATTENDNO ";

        return $query;
    }

    /* データ更新処理 */
    function update($model)
    {
        $db = Query::dbCheckOut();

        for ($i = 0; $i < get_count($model->data["SCHREGNO"]); $i++) {
            $schregno = $model->data["SCHREGNO"][$i];

            //削除
            $query  = " DELETE FROM CERTIF_SCH_PRINT_CNT_DAT";
            $query .= " WHERE ";
            $query .= "     SCHREGNO        = '".$schregno."' AND ";
            $query .= "     CERTIF_KINDCD   = '008' ";

            $db->query($query);

            if($model->fields["PRINT_CNT"][$i]){
                //追加
                $data = array();
                $data["SCHREGNO"][TEXT]         = $schregno;
                $data["CERTIF_KINDCD"][TEXT]    = "008";
                $data["PRINT_CNT"][NUMBER]      = $model->fields["PRINT_CNT"][$i];
                $data["REGISTERCD"][TEXT]       = STAFFCD ;
                $data["UPDATED"][FUNC]          = "sysdate()";

                $query = Query::insertSQL($data, "CERTIF_SCH_PRINT_CNT_DAT");
                $db->query($query);
            }
        }
        Query::dbCheckIn($db);
    }

    //////////////////////////////// 以下 KNJE070 からコピー /////////////////////////////////////
    //校種を取得　getSchoolDiv()で使用
    function getSchoolKind($grade) {
        $query  = " SELECT ";
        $query .= "     SCHOOL_KIND ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND GRADE = '".$grade."' ";

        return $query;
    }

    //卒業可能学年チェック
    function checkGrdGrade($grade) {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'A023' AND ";
        $query .= "     NAME1   = 'H' AND ";
        $query .= "     '".$grade."' BETWEEN NAMESPARE2 AND NAMESPARE3 ";

        return $query;
    }

    //テーブルのフィールド確認
    function getTableColumn($tabname, $colname) {
        $query  = " SELECT T1.COLUMN_NAME ";
        $query .= " FROM ";
        $query .= "     SYSIBM.COLUMNS T1 ";
        $query .= " WHERE ";
        $query .= "     T1.TABLE_NAME = '".$tabname."' AND T1.COLUMN_NAME = '".$colname."' ";

        return $query;
    }

    //仮評定データ取得
    function getProvData($model) {
        $query  = " SELECT DISTINCT ";
        $query .= "     CASE WHEN T2.PROV_SEMESTER IS NULL THEN '99' ELSE T2.PROV_SEMESTER END AS PROV_SEMESTER, ";
        $query .= "     CASE WHEN T2.PROV_TESTKINDCD IS NULL THEN '999' ELSE T2.PROV_TESTKINDCD END AS PROV_TESTKINDCD, ";
        $query .= "     CASE WHEN T2.PROV_TESTITEMCD IS NULL THEN '999' ELSE T2.PROV_TESTITEMCD END AS PROV_TESTITEMCD, ";
        $query .= "     CASE WHEN T2.PROV_SCORE_DIV IS NULL THEN '999' ELSE T2.PROV_SCORE_DIV END AS PROV_SCORE_DIV, ";
        $query .= "     VALUE(L1.SEMESTERNAME,'　') AS SEMESTERNAME, ";
        $query .= "     VALUE(L2.TESTITEMNAME,'　') AS TESTITEMNAME ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1, ";
        $query .= "     STUDYREC_PROV_FLG_DAT T2 ";
        $query .= "     LEFT JOIN SEMESTER_MST L1 ";
        $query .= "              ON T2.YEAR = L1.YEAR ";
        $query .= "             AND T2.PROV_SEMESTER    = L1.SEMESTER ";
        $query .= "     LEFT JOIN ".$model->Properties["useTestCountflg"]." L2 ";
        $query .= "              ON T2.YEAR = L2.YEAR ";
        if ($model->Properties["useTestCountflg"] != "TESTITEM_MST_COUNTFLG") {
            $query .= "             AND T2.PROV_SEMESTER    = L2.SEMESTER ";
        }
        $query .= "             AND T2.PROV_TESTKINDCD  = L2.TESTKINDCD ";
        $query .= "             AND T2.PROV_TESTITEMCD  = L2.TESTITEMCD ";
        if ($model->Properties["useTestCountflg"] == "TESTITEM_MST_COUNTFLG_NEW_SDIV") {
            $query .= "             AND T2.PROV_SCORE_DIV  = L2.SCORE_DIV ";
        }
        $query .= " WHERE ";
        $query .= "     T1.YEAR = T2.YEAR AND ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     T1.SEMESTER = '".CTRL_SEMESTER."' AND ";
        $query .= "     T1.GRADE || T1.HR_CLASS = '".$model->field["GRADE_HR_CLASS"]."' AND ";
        $query .= "     T1.SCHREGNO = T2.SCHREGNO ";
        $query .= " ORDER BY ";
        $query .= "     PROV_SEMESTER, ";
        $query .= "     PROV_TESTKINDCD, ";
        $query .= "     PROV_TESTITEMCD, ";
        $query .= "     PROV_SCORE_DIV ";

        return $query;
    }

    //評定有の件数チェック
    function getValuationCnt($model) {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1, ";
        $query .= "     SCHREG_STUDYREC_DAT T2 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR     = T2.YEAR AND ";
        $query .= "     T1.YEAR     = '".CTRL_YEAR."' AND ";
        $query .= "     T1.SEMESTER = '".CTRL_SEMESTER."' AND ";
        $query .= "     T1.GRADE || T1.HR_CLASS = '".$model->field["GRADE_HR_CLASS"]."' AND ";
        $query .= "     T1.SCHREGNO = T2.SCHREGNO AND ";
        $query .= "     T2.SCHOOLCD = '0' AND ";
        $query .= "     T2.VALUATION IS NOT NULL ";

        return $query;
    }

    function getSchoolName() {
        $query  = " SELECT ";
        $query .= "     NAME1 ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'Z010' AND ";
        $query .= "     NAMECD2 = '00' ";

        return $query;
    }

    //SCHOOLDIV を取得する(0:学年制、1:単位制)
    function getSchoolDiv($model) {
        $query  = " SELECT ";
        $query .= "     T1.SCHOOLDIV, ";
        $query .= "     VALUE(T2.NAMESPARE2,'0') AS NEN, ";
        $query .= "     VALUE(T3.NAMESPARE3,'0') AS IS_TUSIN ";
        $query .= " FROM ";
        $query .= "     SCHOOL_MST T1 ";
        $query .= "     LEFT JOIN V_NAME_MST T2 ON T2.YEAR = T1.YEAR AND T2.NAMECD1 = 'Z001' AND ";
        $query .= "             T2.NAMECD2 = T1.SCHOOLDIV AND T2.NAMESPARE2 IN ('3','4','6') ";
        $query .= "     LEFT JOIN V_NAME_MST T3 ON T3.YEAR = T1.YEAR AND T3.NAMECD1 = 'Z001' AND ";
        $query .= "             T3.NAMECD2 = T1.SCHOOLDIV ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '". CTRL_YEAR ."' ";
        if ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "           AND T1.SCHOOL_KIND = '".$model->schoolKind."' ";
        }

        return $query;
    }

    //近大チェック
    function checkKindai()
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'Z010' AND ";
        $query .= "     NAMECD2 = '00' AND ";
        $query .= "     UPPER(NAME1) IN ('KINDAI', 'KINJUNIOR') ";

        return $query;
    }

    //職員一覧取得
    function getStaffList() {
        $query  = " SELECT ";
        $query .= "     T1.STAFFCD AS VALUE, ";
        $query .= "     T1.STAFFCD || '　' || T1.STAFFNAME_SHOW AS LABEL ";
        $query .= " FROM ";
        $query .= "     STAFF_MST AS T1, ";
        $query .= "     STAFF_YDAT AS T2 ";
        $query .= " WHERE ";
        $query .= "     T1.STAFFCD = T2.STAFFCD AND ";
        $query .= "     T2.YEAR = '".CTRL_YEAR."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

}
?>
