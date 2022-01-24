<?php

require_once('for_php7.php');

class knjz380vQuery extends Query {

    //リスト内情報取得
    function getListdata($model)
    {
        $query = "";
        $query .= "SELECT DISTINCT";
        $query .= "  T1.SEMESTER, ";
        $query .= "  T2.SEMESTERNAME, ";
        $query .= "  T1.TESTKINDCD, ";
        $query .= "  CASE WHEN T1.TESTKINDCD = '99' AND T1.SEMESTER <> '9' THEN '学期末' ";
        $query .= "       WHEN T1.TESTKINDCD = '99' AND T1.SEMESTER = '9' THEN '学年末' ";
        $query .= "       ELSE T4.TESTKINDNAME ";
        $query .= "  END AS TESTKINDNAME, ";
        $query .= "  T1.TESTITEMCD, ";
        $query .= "  T1.TESTITEMNAME, ";
        $query .= "  T1.TESTITEMABBV1, ";
        $query .= "  T1.SCORE_DIV, ";
        $query .= "  T1.COUNTFLG, ";
        $query .= "  CASE WHEN T1.COUNTFLG = '1' THEN '集計する' ";
        $query .= "       WHEN T1.COUNTFLG = '0' THEN '集計しない' ";
        $query .= "       ELSE NULL END AS COUNTFLG_SHOW, ";
        $query .= "  T3.SEMESTER_DETAIL || '：' || T3.SEMESTERNAME AS DETAIL_NAME, ";
        $query .= "  CASE WHEN T1.SIDOU_INPUT = '1' THEN 'あり' ";
        $query .= "       ELSE NULL END AS SIDOU_INPUT_SHOW, ";
        $query .= "  CASE WHEN T1.SIDOU_INPUT_INF = '1' THEN '：記号' ";
        $query .= "       WHEN T1.SIDOU_INPUT_INF = '2' THEN '：得点' ";
        $query .= "       ELSE NULL END AS SIDOU_INPUT_INF_SHOW, ";
        if ($model->Properties["useMikomiFlg"] == "1") {
            $query .= "  CASE WHEN T1.MIKOMI_FLG = '1' THEN 'あり' ";
            $query .= "       ELSE NULL END AS MIKOMI_FLG_SHOW, ";
        }
        if ($model->Properties["useSankouFlg"] == "1") {
            $query .= "  CASE WHEN T1.SANKOU_FLG = '1' THEN 'あり' ";
            $query .= "       ELSE NULL END AS SANKOU_FLG_SHOW, ";
        }
        if ($model->Properties["useRemarkFlg"] == "1") {
            $query .= "  CASE WHEN T1.REMARK_FLG = '1' THEN 'あり' ";
            $query .= "       ELSE NULL END AS REMARK_FLG_SHOW, ";
        }
        $query .= "  CASE WHEN T1.JYORETSU_FLG = '1' THEN 'あり' ";
        $query .= "       ELSE NULL END AS JYORETSU_FLG_SHOW, ";
        $query .= "  CASE WHEN T1.NOT_USE_CSV_FLG = '1' THEN '不可' ";
        $query .= "       ELSE NULL END AS NOT_USE_CSV_FLG_SHOW, ";
        $query .= "  T1.UPDATED ";
        $query .= "FROM ";
        $query .= "  TESTITEM_MST_COUNTFLG_NEW_SDIV T1 ";
        $query .= "LEFT JOIN ";
        $query .= "  TESTKIND_MST T4 ON T4.TESTKINDCD = T1.TESTKINDCD ";
        $query .= "LEFT JOIN ";
        $query .= "  SEMESTER_MST T2 ON  T2.YEAR = T1.YEAR ";
        $query .= "                  AND T2.SEMESTER = T1.SEMESTER ";
        $query .= "LEFT JOIN ";
        $query .= "  SEMESTER_DETAIL_MST T3 ON  T3.YEAR = T1.YEAR ";
        $query .= "                         AND T3.SEMESTER = T1.SEMESTER ";
        $query .= "                         AND T3.SEMESTER_DETAIL = T1.SEMESTER_DETAIL ";
        $query .= "WHERE ";
        //権限による絞込み
        $query .= "  T1.YEAR = '" .CTRL_YEAR ."' AND ";
        $query .= "  T1.YEAR = T2.YEAR AND ";
        $query .= "  T1.SEMESTER = T2.SEMESTER ";
        $query .= "ORDER BY ";
        $query .= "  T1.SEMESTER,T1.TESTKINDCD, T1.TESTITEMCD, T1.SCORE_DIV ";

        return $query;
    }

    //１レコード取得
    function getRow($model)
    {
        $db = Query::dbCheckOut();

        $query = "";
        $query .= "SELECT ";
        $query .= "  * ";
        $query .= "FROM ";
        $query .= "  TESTITEM_MST_COUNTFLG_NEW_SDIV ";
        $query .= "WHERE ";
        $query .= "  YEAR = '" .CTRL_YEAR ."' AND ";
        $query .= "  SEMESTER = '" .$model->semester ."' AND ";
        $query .= "  TESTKINDCD = '" .$model->testkindcd ."' AND ";
        $query .= "  TESTITEMCD = '" .$model->testitemcd ."' AND ";
        $query .= "  SCORE_DIV = '" .$model->score_div ."' ";
        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);
        return $row;
    }

    //考査種別（大分類）取得
    function getTestKindName($model)
    {
        if ($model->field["SEMESTER"] === '9') {
            $query   = " SELECT DISTINCT ";
            $query  .= "    '99' || ' ' || '学年末' AS LABEL, ";
            $query  .= "    '99' AS VALUE ";
        } else {
            $query   = " SELECT ";
            $query  .= "    TESTKINDCD || ' ' || TESTKINDNAME AS LABEL, ";
            $query  .= "    TESTKINDCD AS VALUE ";
            $query  .= " FROM";
            $query  .= "    TESTKIND_MST ";
            $query  .= " UNION ";
            $query  .= " SELECT ";
            $query  .= "    '99' || ' ' || '学期末' AS LABEL, ";
            $query  .= "    '99' AS VALUE ";
        }
        $query  .= " FROM";
        $query  .= "    TESTKIND_MST ";
        $query  .= " ORDER BY ";
        $query  .= "    VALUE";

        return $query;
    }
    
    //考査種別（小分類）取得
    function getScoreDiv($model)
    {
        $query   = " SELECT ";
        if ($model->field["SEMESTER"] !== '9') {
            if ($model->field["TESTKINDCD"] === '99') {
                $query  .= "    NAMECD2 || ' ' || NAME2 AS LABEL, ";
                $query  .= "    NAMECD2 AS VALUE ";
            } else {
                $query  .= "    NAMECD2 || ' ' || NAME1 AS LABEL, ";
                $query  .= "    NAMECD2 AS VALUE ";
            }
        } else {
            $query  .= "    NAMECD2 || ' ' || NAME3 AS LABEL, ";
            $query  .= "    NAMECD2 AS VALUE ";
        }
        $query  .= " FROM ";
        $query  .= "     V_NAME_MST ";
        $query  .= " WHERE ";
        $query  .= "     YEAR    = '" .CTRL_YEAR ."' ";
        $query  .= " AND NAMECD1 = 'D053' ";
        if ($model->field["SEMESTER"] === '9') {
            $query  .= "  AND  NAMECD2 NOT IN ('01', '02') ";
        }
        $query  .= " ORDER BY ";
        $query  .= "    VALUE ";

        return $query;
    }

    //学期名の取得
    function getSemester($model)
    {
        $query   = " SELECT ";
        $query  .= "    SEMESTERNAME AS LABEL, ";
        $query  .= "    SEMESTER AS VALUE ";
        $query  .= " FROM ";
        $query  .= "    SEMESTER_MST ";
        $query  .= " WHERE ";
        $query  .= "    YEAR = '" .CTRL_YEAR ."' ";
        $query  .= " ORDER BY ";
        $query  .= "    VALUE ";

        return $query;
    }

    //学期詳細名の取得
    function getSemesterDetail($Row)
    {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     SEMESTER_DETAIL_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '" .CTRL_YEAR ."' AND ";
        $query .= "     SEMESTER = '{$Row["SEMESTER"]}' ";

        return $query;
    }

    //学期詳細の日付を取得してくる
    function getSemesterDetail_sdate_edate($Row) {
        $query  = " SELECT ";
        $query .= "     SDATE, ";
        $query .= "     EDATE ";
        $query .= " FROM ";
        $query .= "     SEMESTER_DETAIL_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR            = '" .CTRL_YEAR ."' AND ";
        $query .= "     SEMESTER        = '{$Row["SEMESTER"]}' AND ";
        $query .= "     SEMESTER_DETAIL = '{$Row["SEMESTER_DETAIL"]}' ";

        return $query;
    }

    //前年度からのコピー
    function &getCopyQuery($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $query = "";
        $query .= "INSERT INTO TESTITEM_MST_COUNTFLG_NEW_SDIV( ";
        $query .= "YEAR, ";
        $query .= "SEMESTER, ";
        $query .= "TESTKINDCD, ";
        $query .= "TESTITEMCD, ";
        $query .= "SCORE_DIV, ";
        $query .= "TESTITEMNAME, ";
        $query .= "TESTITEMABBV1, ";
        $query .= "COUNTFLG, ";
        $query .= "SEMESTER_DETAIL, ";
        $query .= "SIDOU_INPUT, ";
        $query .= "SIDOU_INPUT_INF, ";
        if ($model->Properties["useMikomiFlg"] == "1") {
            $query .= "MIKOMI_FLG, ";
        }
        if ($model->Properties["useSankouFlg"] == "1") {
            $query .= "SANKOU_FLG, ";
        }
        if ($model->Properties["useRemarkFlg"] == "1") {
            $query .= "REMARK_FLG, ";
        }
        $query .= "JYORETSU_FLG, ";
        $query .= "NOT_USE_CSV_FLG, ";
        $query .= "REGISTERCD, ";
        $query .= "UPDATED ";
        $query .= ") ";
        $query .= " (SELECT ";
        $query .= "      '" . CTRL_YEAR ."', ";
        $query .= "      SEMESTER, ";
        $query .= "      TESTKINDCD, ";
        $query .= "      TESTITEMCD, ";
        $query .= "      SCORE_DIV, ";
        $query .= "      TESTITEMNAME, ";
        $query .= "      TESTITEMABBV1, ";
        $query .= "      COUNTFLG, ";
        $query .= "      SEMESTER_DETAIL, ";
        $query .= "      SIDOU_INPUT, ";
        $query .= "      SIDOU_INPUT_INF, ";
        if ($model->Properties["useMikomiFlg"] == "1") {
            $query .= "      MIKOMI_FLG, ";
        }
        if ($model->Properties["useSankouFlg"] == "1") {
            $query .= "      SANKOU_FLG, ";
        }
        if ($model->Properties["useRemarkFlg"] == "1") {
            $query .= "      REMARK_FLG, ";
        }
        $query .= "      JYORETSU_FLG, ";
        $query .= "      NOT_USE_CSV_FLG, ";
        $query .= "      '".STAFFCD."', ";
        $query .= "      SYSDATE() ";
        $query .= "  FROM ";
        $query .= "      TESTITEM_MST_COUNTFLG_NEW_SDIV T0 ";
        $query .= "  WHERE YEAR = '" .(CTRL_YEAR-1) ."' AND ";
        $query .= "      not exists ( ";
        $query .= "                  SELECT 'x' FROM TESTITEM_MST_COUNTFLG_NEW_SDIV T1 ";
        $query .= "                  WHERE ";
        $query .= "                  T1.YEAR = '" . CTRL_YEAR ."' AND ";
        $query .= "                  T1.SEMESTER = T0.SEMESTER AND ";
        $query .= "                  T1.TESTKINDCD = T0.TESTKINDCD AND ";
        $query .= "                  T1.TESTITEMCD = T0.TESTITEMCD AND ";
        $query .= "                  T1.SCORE_DIV = T0.SCORE_DIV ";
        $query .= "                  ) AND ";
        $query .= "      (TESTKINDCD IN (SELECT TESTKINDCD FROM TESTKIND_MST) OR TESTKINDCD = '99') ";
        $query .= " ) ";

        $db->query($query);
        $db->commit();
        Query::dbCheckIn($db);
        return true;
   }

    //存在チェック
    function getExists($model)
    {
        $db = Query::dbCheckOut();

        $query = "";
        $query .= "SELECT ";
        $query .= "  COUNT(*) ";
        $query .= "FROM ";
        $query .= "  TESTITEM_MST_COUNTFLG_NEW_SDIV ";
        $query .= "WHERE ";
        $query .= "  YEAR = '" .CTRL_YEAR ."' AND ";
        $query .= "  SEMESTER = '" .$model->field["SEMESTER"] ."' AND ";
        $query .= "  TESTKINDCD = '" .$model->field["TESTKINDCD"] ."' AND ";
        $query .= "  TESTITEMCD = '" .$model->field["TESTITEMCD"] ."' AND ";
        $query .= "  SCORE_DIV  = '" .$model->field["SCORE_DIV"] ."' ";
        $ret_val = $db->getOne($query);
        Query::dbCheckIn($db);

        return $ret_val;
    }

    // INSERT&UPDATE 一度DELETEしてINSERTする。
    function &getInsertUpdateQuery($model)
    {
        $db = Query::dbCheckOut();

        $sem_detail = ($model->Properties["Semester_Detail_Hyouji"] == "1") ? $model->field["SEMESTER_DETAIL"] : $db->getOne(knjz380vQuery::getSemDetail($model));

        $db->autoCommit(false);
        $query  = " DELETE FROM TESTITEM_MST_COUNTFLG_NEW_SDIV ";
        $query .= " WHERE YEAR = '" .CTRL_YEAR."' ";
        $query .= " AND SEMESTER = '" .$model->field["SEMESTER"] ."' ";
        $query .= " AND TESTKINDCD = '" .$model->field["TESTKINDCD"] ."' ";
        $query .= " AND TESTITEMCD = '" .$model->field["TESTITEMCD"] ."' ";
        $query .= " AND SCORE_DIV  = '" .$model->field["SCORE_DIV"] ."' ";
        $db->query($query);

        $query = "";

        $data["YEAR"][TEXT]            = CTRL_YEAR;
        $data["SEMESTER"][TEXT]        = $model->field["SEMESTER"];
        $data["TESTKINDCD"][TEXT]      = $model->field["TESTKINDCD"];
        $data["TESTITEMCD"][TEXT]      = $model->field["TESTITEMCD"];
        $data["SCORE_DIV"][TEXT]       = $model->field["SCORE_DIV"];
        $data["TESTITEMNAME"][TEXT]    = $model->field["TESTITEMNAME"];
        $data["TESTITEMABBV1"][TEXT]   = $model->field["TESTITEMABBV1"];
        $data["COUNTFLG"][TEXT]        = ($model->field["COUNTFLG"] == "1") ? "1" : "0" ;
        $data["SEMESTER_DETAIL"][TEXT] = $sem_detail;
        $data["SIDOU_INPUT"][TEXT]     = ($model->field["SIDOU_INPUT"] == "1") ? "1" : "0" ;
        $data["SIDOU_INPUT_INF"][TEXT] = $model->field["SIDOU_INPUT_INF"];
        if ($model->Properties["useMikomiFlg"] == "1") {
            $data["MIKOMI_FLG"][TEXT]      = ($model->field["MIKOMI_FLG"] == "1") ? "1" : "0" ;
        }
        if ($model->Properties["useSankouFlg"] == "1") {
            $data["SANKOU_FLG"][TEXT]      = ($model->field["SANKOU_FLG"] == "1") ? "1" : "0" ;
        }
        if ($model->Properties["useRemarkFlg"] == "1") {
            $data["REMARK_FLG"][TEXT]      = ($model->field["REMARK_FLG"] == "1") ? "1" : "0" ;
        }
        $data["JYORETSU_FLG"][TEXT]    = ($model->field["JYORETSU_FLG"] == "1") ? "1" : "0" ;
        $data["NOT_USE_CSV_FLG"][TEXT] = ($model->field["NOT_USE_CSV_FLG"] == "1") ? "1" : "0" ;
        $data["REGISTERCD"][TEXT]      = STAFFCD;
        $data["UPDATED"][NUMBER]       = "SYSDATE()";

        $query = Query::insertSQL($data, "TESTITEM_MST_COUNTFLG_NEW_SDIV");
        $db->query($query);

        $db->commit(); // トランザクションをコミットする。
        Query::dbCheckIn($db);
        return;
    }

    // DELETE
    function &getDeleteQuery($model)
    {
        $db = Query::dbCheckOut();

        $query  = " DELETE FROM TESTITEM_MST_COUNTFLG_NEW_SDIV ";
        $query .= " WHERE YEAR = '" .CTRL_YEAR."' ";
        $query .= " AND SEMESTER = '" .$model->semester ."' ";
        $query .= " AND TESTKINDCD = '" .$model->testkindcd ."' ";
        $query .= " AND TESTITEMCD = '" .$model->testitemcd ."' ";
        $query .= " AND SCORE_DIV  = '" .$model->score_div ."' ";

        $db->query($query);
        Query::dbCheckIn($db);

        return $result;
    }

    //出欠集計範囲取得
    function getSemDetail($model)
    {
        $query  = " SELECT ";
        $query .= "     SEMESTER_DETAIL ";
        $query .= " FROM ";
        $query .= "     TESTITEM_MST_COUNTFLG_NEW_SDIV ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".CTRL_YEAR."' AND ";
        $query .= "     SEMESTER    = '".$model->field["SEMESTER"]."' AND ";
        $query .= "     TESTKINDCD  = '".$model->field["TESTKINDCD"]."' AND ";
        $query .= "     TESTITEMCD  = '".$model->field["TESTITEMCD"]."' AND ";
        $query .= "     SCORE_DIV   = '".$model->field["SCORE_DIV"]."' ";
        
        return $query;
    }
}
?>
