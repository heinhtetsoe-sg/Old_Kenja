<?php

require_once('for_php7.php');


class knjz235aQuery extends Query {

    //学年取得（権限チェック）
    function getGrade($model)
    {
        //参照・更新可
        $query  = " SELECT DISTINCT ";
        $query .= "     T1.GRADE AS VALUE, ";
        $query .= "     T2.GRADE_NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT T1";
        $query .= " LEFT JOIN SCHREG_REGD_GDAT T2 ON  T2.YEAR = T1.YEAR ";
        $query .= "                              AND T2.GRADE = T1.GRADE ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND T1.SEMESTER = '".$model->field["SEMESTER"]."' ";
        $query .= "     AND T2.SCHOOL_KIND <> 'P' ";
        //参照・更新可（制限付き）
        if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT) {
            $query .= "     AND (   T1.TR_CD1 = '".STAFFCD."' ";
            $query .= "          OR T1.TR_CD2 = '".STAFFCD."' ";
            $query .= "          OR T1.TR_CD3 = '".STAFFCD."') ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //学期取得
    function getSemester() {
        $query  = " SELECT DISTINCT ";
        $query .= "     SEMESTER AS VALUE, ";
        $query .= "     SEMESTERNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     SEMESTER <> '9' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //テスト一覧取得
    function getTestList($model)
    {

        $query .= " WITH MAIN AS ( ";
        //模試の選択
        $query .= " SELECT ";
        $query .= "     '2:' || T1.PROFICIENCYDIV || ':' || T1.SEMESTER || ':' || T1.PROFICIENCYCD AS VALUE, ";
        $query .= "     '模試：' || T3.SEMESTERNAME || '：' || T2.PROFICIENCYNAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     PROFICIENCY_YMST T1 ";
        $query .= "     LEFT JOIN PROFICIENCY_MST T2 ON T2.PROFICIENCYDIV = T1.PROFICIENCYDIV ";
        $query .= "                                 AND T2.PROFICIENCYCD  = T1.PROFICIENCYCD ";
        $query .= "     LEFT JOIN SEMESTER_MST T3 ON T3.YEAR        = T1.YEAR ";
        $query .= "                              AND T3.SEMESTER    = T1.SEMESTER ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR             = '".CTRL_YEAR."' AND ";
        $query .= "     T1.GRADE            = '".$model->field["GRADE"]."' AND ";
        $query .= "     T1.SEMESTER         = '".$model->field["SEMESTER"]."' AND ";
        $query .= "     T1.PROFICIENCYDIV   = '01' ";
        $query .= " UNION  ";
        //実力試験の選択
        $query .= " SELECT ";
        $query .= "     '2:' || T1.PROFICIENCYDIV || ':' || T1.SEMESTER || ':' || T1.PROFICIENCYCD AS VALUE, ";
        $query .= "     '実力：' || T3.SEMESTERNAME || '：' || T2.PROFICIENCYNAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     PROFICIENCY_YMST T1 ";
        $query .= "     LEFT JOIN PROFICIENCY_MST T2 ON T2.PROFICIENCYDIV = T1.PROFICIENCYDIV ";
        $query .= "                                 AND T2.PROFICIENCYCD  = T1.PROFICIENCYCD ";
        $query .= "     LEFT JOIN SEMESTER_MST T3 ON T3.YEAR        = T1.YEAR ";
        $query .= "                              AND T3.SEMESTER    = T1.SEMESTER ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR             = '".CTRL_YEAR."' AND ";
        $query .= "     T1.GRADE            = '".$model->field["GRADE"]."' AND ";
        $query .= "     T1.SEMESTER         = '".$model->field["SEMESTER"]."' AND ";
        $query .= "     T1.PROFICIENCYDIV   = '02' ";
        
        //実データを選択
        $query .= " ), SELECTED AS ( ";
        $query .= " SELECT "; //考査は明治では実データはない
        $query .= "     TEST_DIV || ':' || SEMESTER || ':' || TESTKINDCD || ':' || TESTITEMCD AS VALUE ";
        $query .= " FROM ";
        $query .= "     RECORD_PROFICIENCY_ORDER_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".CTRL_YEAR."' AND ";
        $query .= "     GRADE       = '".$model->field["GRADE"]."' AND ";
        $query .= "     SEMESTER    = '".$model->field["SEMESTER"]."' AND ";
        $query .= "     TEST_DIV    = '1' ";
        $query .= " UNION  ";
        $query .= " SELECT "; //実力テストはSEMESTERとPROFICIENCY_CDをセットにし、ユニークな値とする
        $query .= "     TEST_DIV || ':' || PROFICIENCYDIV || ':' || SEMESTER || ':' || PROFICIENCYCD AS VALUE ";
        $query .= " FROM ";
        $query .= "     RECORD_PROFICIENCY_ORDER_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".CTRL_YEAR."' AND ";
        $query .= "     GRADE       = '".$model->field["GRADE"]."' AND ";
        $query .= "     SEMESTER    = '".$model->field["SEMESTER"]."' AND ";
        $query .= "     TEST_DIV    = '2' ";
        $query .= " ) ";
        //main処理
        $query .= " SELECT VALUE,LABEL";
        $query .= " FROM MAIN";
        $query .= " WHERE VALUE NOT IN (SELECT * FROM SELECTED) ";
        $query .= " ORDER BY VALUE ";

        return $query;
    }

    //選択テスト取得
    function getSelectTest($model)
    {
        //考査（明治では未使用）
        $query .= " SELECT ";
        $query .= "     T1.SEQ, ";
        $query .= "     T1.TEST_DIV || ':' || T1.SEMESTER || ':' || T1.TESTKINDCD || ':' || T1.TESTITEMCD AS VALUE, ";
        $query .= "     '考査：' || T2.SEMESTERNAME || '：' AS LABEL ";
        $query .= " FROM ";
        $query .= "     RECORD_PROFICIENCY_ORDER_DAT T1 ";
        $query .= "     LEFT JOIN SEMESTER_MST T2 ON T2.YEAR        = T1.YEAR ";
        $query .= "                              AND T2.SEMESTER    = T1.SEMESTER ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR     = '".CTRL_YEAR."' AND ";
        $query .= "     T1.GRADE    = '".$model->field["GRADE"]."' AND ";
        $query .= "     T1.TEST_DIV = '1'  ";
        $query .= " UNION  ";
        //模試の選択
        $query .= " SELECT ";
        $query .= "     T1.SEQ, ";
        $query .= "     T1.TEST_DIV || ':' || T1.PROFICIENCYDIV || ':' || T1.SEMESTER || ':' || T1.PROFICIENCYCD AS VALUE, ";
        $query .= "     '模試：' || T4.SEMESTERNAME || '：' || T3.PROFICIENCYNAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     RECORD_PROFICIENCY_ORDER_DAT T1, ";
        $query .= "     PROFICIENCY_YMST T2 ";
        $query .= "     LEFT JOIN PROFICIENCY_MST T3 ON T3.PROFICIENCYDIV = T2.PROFICIENCYDIV ";
        $query .= "                                 AND T3.PROFICIENCYCD  = T2.PROFICIENCYCD ";
        $query .= "     LEFT JOIN SEMESTER_MST T4 ON T4.YEAR        = T2.YEAR ";
        $query .= "                              AND T4.SEMESTER    = T2.SEMESTER ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR             = T2.YEAR AND ";
        $query .= "     T1.YEAR             = '".CTRL_YEAR."' AND ";
        $query .= "     T1.GRADE            = T2.GRADE AND ";
        $query .= "     T1.GRADE            = '".$model->field["GRADE"]."' AND ";
        $query .= "     T1.SEMESTER         = T2.SEMESTER AND ";
        $query .= "     T1.TEST_DIV         = '2' AND ";
        $query .= "     T1.PROFICIENCYCD    = T2.PROFICIENCYCD AND ";
        $query .= "     T1.PROFICIENCYDIV   = T2.PROFICIENCYDIV AND ";
        $query .= "     T1.PROFICIENCYDIV   = '01' ";
        $query .= " UNION  ";
        //実力試験の選択
        $query .= " SELECT ";
        $query .= "     T1.SEQ, ";
        $query .= "     T1.TEST_DIV || ':' || T1.PROFICIENCYDIV || ':' || T1.SEMESTER || ':' || T1.PROFICIENCYCD AS VALUE, ";
        $query .= "     '実力：' || T4.SEMESTERNAME || '：' || T3.PROFICIENCYNAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     RECORD_PROFICIENCY_ORDER_DAT T1, ";
        $query .= "     PROFICIENCY_YMST T2 ";
        $query .= "     LEFT JOIN PROFICIENCY_MST T3 ON T3.PROFICIENCYDIV = T2.PROFICIENCYDIV ";
        $query .= "                                 AND T3.PROFICIENCYCD  = T2.PROFICIENCYCD ";
        $query .= "     LEFT JOIN SEMESTER_MST T4 ON T4.YEAR        = T2.YEAR ";
        $query .= "                              AND T4.SEMESTER    = T2.SEMESTER ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR             = T2.YEAR AND ";
        $query .= "     T1.YEAR             = '".CTRL_YEAR."' AND ";
        $query .= "     T1.GRADE            = T2.GRADE AND ";
        $query .= "     T1.GRADE            = '".$model->field["GRADE"]."' AND ";
        $query .= "     T1.SEMESTER         = T2.SEMESTER AND ";
        $query .= "     T1.TEST_DIV         = '2' AND ";
        $query .= "     T1.PROFICIENCYCD    = T2.PROFICIENCYCD AND ";
        $query .= "     T1.PROFICIENCYDIV   = T2.PROFICIENCYDIV AND ";
        $query .= "     T1.PROFICIENCYDIV   = '02' ";
        $query .= " ORDER BY ";
        $query .= "     SEQ ";

        return $query;
    }

    //更新
    function &getUpdateQuery($model)
    {        
        //配列に格納
        $array = explode (",", $model->selectdata);
        
        $db = Query::dbCheckOut();
        $db->autoCommit(false);
        $updated = date("Y-m-d H:i:s", time());     //更新日付
        
        $query = " DELETE FROM RECORD_PROFICIENCY_ORDER_DAT ";
        $query.= " WHERE YEAR = '" .CTRL_YEAR."' AND ";
        $query.= "       GRADE = '".$model->field["GRADE"]."' ";
        $db->query($query);

        //考査順の登録
        if ($array[0] !="") {
            for ($i = 0; $i < get_count($array); $i++){
                //初期化
                $testdiv = "";
                $semester = "";
                $testkindcd = "";
                $testitemcd = "";
                $proficiencydiv = "";
                $proficiencycd = "";
                
                //考査の場合（明治では未使用）
                if(substr($array[$i],0,1) == 1){
                    list($testdiv, $semester, $testkindcd, $testitemcd) = explode(":", $array[$i]);
                //模試、実力試験の場合 $semesterは識別用
                } else {
                    list($testdiv, $proficiencydiv, $semester, $proficiencycd) = explode(":", $array[$i]);
                }

                $data["YEAR"][TEXT]                 = CTRL_YEAR;
                $data["GRADE"][TEXT]                = $model->field["GRADE"];
                $data["SEQ"][NUMBER]                = $i+1;
                $data["TEST_DIV"][TEXT]             = $testdiv;
                $data["SEMESTER"][TEXT]             = ($semester) ? $semester : "";
                $data["TESTKINDCD"][TEXT]           = ($testkindcd) ? $testkindcd : "";
                $data["TESTITEMCD"][TEXT]           = ($testitemcd) ? $testitemcd : "";
                $data["PROFICIENCYDIV"][TEXT]       = ($proficiencydiv) ? $proficiencydiv : "";
                $data["PROFICIENCYCD"][TEXT]        = ($proficiencycd) ? $proficiencycd : "";
                $data["REGISTERCD"][TEXT]           = STAFFCD;
                $data["UPDATED"][NUMBER]            = "sysdate()";

                $query = Query::insertSQL($data, "RECORD_PROFICIENCY_ORDER_DAT");
                $db->query($query);
            }
        }
        $db->commit();      // トランザクションをコミットする。
        Query::dbCheckIn($db);
        return;
    }

    //コピー前チェック
    function cntSelectTest()
    {
        $db = Query::dbCheckOut();

        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     RECORD_PROFICIENCY_ORDER_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";

        $cntselected = $db->getOne($query);
        Query::dbCheckIn($db);
        return $cntselected;
    }

    //前年度のデータチェック
    function cntLastOrder()
    {
        $db = Query::dbCheckOut();

        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     RECORD_PROFICIENCY_ORDER_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".(CTRL_YEAR-1)."' ";

        $cntlastorder = $db->getOne($query);
        Query::dbCheckIn($db);
        return $cntlastorder;
    }

    //前年度からコピー
    function &getCopyQuery()
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $query  = " INSERT INTO RECORD_PROFICIENCY_ORDER_DAT ";
        $query .= "           ( YEAR, ";
        $query .= "             GRADE, ";
        $query .= "             SEQ, ";
        $query .= "             TEST_DIV, ";
        $query .= "             SEMESTER, ";
        $query .= "             TESTKINDCD, ";
        $query .= "             TESTITEMCD, ";
        $query .= "             PROFICIENCYDIV, ";
        $query .= "             PROFICIENCYCD, ";
        $query .= "             REGISTERCD, ";
        $query .= "             UPDATED ) ";
        $query .= "   (SELECT ";
        $query .= "         '".CTRL_YEAR."', ";
        $query .= "         GRADE, ";
        $query .= "         SEQ, ";
        $query .= "         TEST_DIV, ";
        $query .= "         SEMESTER, ";
        $query .= "         TESTKINDCD, ";
        $query .= "         TESTITEMCD, ";
        $query .= "         PROFICIENCYDIV, ";
        $query .= "         PROFICIENCYCD, ";
        $query .= "         '".STAFFCD."', ";
        $query .= "         sysdate() ";
        $query .= "     FROM ";
        $query .= "         RECORD_PROFICIENCY_ORDER_DAT ";
        $query .= "     WHERE ";
        $query .= "         YEAR = '".(CTRL_YEAR-1)."' ";
        $query .= "     ) ";

        $db->query($query);
        $db->commit();
        Query::dbCheckIn($db);
       return true;
    }

}
?>
