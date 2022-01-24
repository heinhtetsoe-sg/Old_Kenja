<?php

require_once('for_php7.php');

class knjm390mQuery extends Query {

    //校時取得
    function selectName($cd1) {
        $query  = " SELECT NAME1 AS LABEL, NAMECD2 AS VALUE ";
        $query .= " FROM V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR    = '".CTRL_YEAR."' ";
        $query .= " AND NAMECD1 = '".$cd1."' ";
        $query .= " ORDER BY VALUE ";

        return $query;
    }

    //講座取得（権限チェック）
    function getAuth(&$model) {
        //参照・更新可
        if (AUTHORITY == DEF_UPDATABLE) {
            $model->User = 0;
            $query  = "  SELECT DISTINCT ";
            $query .= "      T1.CHAIRCD AS VALUE, ";
            $query .= "      T1.CHAIRNAME AS LABEL ";
            $query .= "  FROM ";
            $query .= "      CHAIR_DAT T1 ";
            $query .= "  LEFT JOIN SUBCLASS_SCHOOLING_COMBINED_DAT T2 ON T1.CLASSCD = T2.ATTEND_CLASSCD ";
            $query .= "                                          AND T1.SCHOOL_KIND = T2.ATTEND_SCHOOL_KIND ";
            $query .= "                                          AND T1.CURRICULUM_CD = T2.ATTEND_CURRICULUM_CD ";
            $query .= "                                          AND T1.SUBCLASSCD = T2.ATTEND_SUBCLASSCD ";
            $query .= "                                          AND T2.YEAR      = '".$model->Year."' ";
            $query .= "  WHERE ";
            $query .= "      T1.YEAR      = '".$model->Year."' ";
            $query .= "  AND T1.SEMESTER  = '".$model->semester."'  ";
            $query .= "  AND T1.CLASSCD < '91' ";
            $query .= "  AND T2.ATTEND_CLASSCD IS NULL ";
            $query .= "  AND T2.ATTEND_SCHOOL_KIND IS NULL ";
            $query .= "  AND T2.ATTEND_CURRICULUM_CD IS NULL ";
            $query .= "  AND T2.ATTEND_SUBCLASSCD IS NULL ";
            $query .= " ORDER BY VALUE ";
        } else {
        //参照・更新可（制限付き）
            $model->User = 1;
            $query  = "  SELECT DISTINCT ";
            $query .= "      T1.CHAIRCD AS VALUE, ";
            $query .= "      T1.CHAIRNAME AS LABEL ";
            $query .= "  FROM ";
            $query .= "      CHAIR_DAT T1 ";
            $query .= "  LEFT JOIN SUBCLASS_SCHOOLING_COMBINED_DAT T2 ON T1.CLASSCD = T2.ATTEND_CLASSCD ";
            $query .= "                                          AND T1.SCHOOL_KIND = T2.ATTEND_SCHOOL_KIND ";
            $query .= "                                          AND T1.CURRICULUM_CD = T2.ATTEND_CURRICULUM_CD ";
            $query .= "                                          AND T1.SUBCLASSCD = T2.ATTEND_SUBCLASSCD ";
            $query .= "                                          AND T2.YEAR      = '".$model->Year."' ";
            $query .= "  WHERE ";
            $query .= "     T1.YEAR      = '" .$model->Year ."'";
            $query .= " AND T1.SEMESTER  = '" .$model->semester ."'";
            $query .= " AND T1.CLASSCD < '91' ";
            $query .= " AND T2.ATTEND_CLASSCD IS NULL ";
            $query .= " AND T2.ATTEND_SCHOOL_KIND IS NULL ";
            $query .= " AND T2.ATTEND_CURRICULUM_CD IS NULL ";
            $query .= " AND T2.ATTEND_SUBCLASSCD IS NULL ";
            $query .= " AND T1.CHAIRCD in ";
            $query .= "    (SELECT C1.CHAIRCD ";
            $query .= "     FROM CHAIR_STF_DAT C1";
            $query .= "     WHERE ";
            $query .= "         C1.YEAR     ='".$model->Year."'";
            $query .= "     AND C1.SEMESTER ='".$model->semester."'";
            $query .= "     AND C1.STAFFCD  ='".STAFFCD."')";            
            $query .= " ORDER BY VALUE ";
        }

        return $query;
    }

    //担当者取得
    function selectStaff($model) {
        $query  = " SELECT w1.STAFFCD, w1.CHARGEDIV, w2.STAFFNAME ";
        $query .= " FROM CHAIR_STF_DAT w1 ";
        $query .= " LEFT JOIN V_STAFF_MST w2 ON w1.STAFFCD = w2.STAFFCD and w2.YEAR = '" .CTRL_YEAR."' ";
        $query .= " WHERE ";
        $query .= "     w1.YEAR     = '".CTRL_YEAR."' ";
        $query .= " AND w1.SEMESTER = '".$model->semester ."' ";
        $query .= " AND w1.CHAIRCD  = '".$model->field["CHAIR"]."' ";
        $query .= " ORDER BY w1.CHARGEDIV DESC, w1.STAFFCD ";

        return $query;
    }

    //最大時間数取得
    function getCreditTime($model) {
        $query  = " SELECT ";
        $query .= "     NAMESPARE2 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR    = '".CTRL_YEAR."' AND ";
        $query .= "     NAMECD1 = 'M001' AND ";
        $query .= "     NAMECD2 = '".$model->field["COURSE"]."' ";

        return $query;
    }

    //登録済み生徒データ
    function getSch($model) {
    
        //開講しない講座と開講する講座コードを配列に格納
        $attend_chaircd = "('";
        $conma = "";
        $set_array = array();
        $set_array = explode(",", $model->Attendchaircd);
        foreach ($set_array as $var) {
            $attend_chaircd .= $conma.$var;
            $conma = "', '";
        }
        $attend_chaircd .= "')";
        
        $query  = " SELECT t1.SCHREGNO, t2.NAME_SHOW, CREDIT_TIME, RECEIPT_TIME, t1.REMARK ";
        $query .= " FROM SCH_ATTEND_DAT t1 ";
        $query .= " LEFT JOIN SCHREG_BASE_MST t2 ON t1.SCHREGNO = t2.SCHREGNO ";
        $query .= " WHERE ";
        $query .= "     t1.YEAR         = '".$model->Year."' ";
        $query .= " AND t1.EXECUTEDATE  = '".str_replace("/","-",$model->Date)."' ";
        if (!$model->Attendchaircd) {
            $query .= " AND t1.CHAIRCD      = '".$model->field["CHAIR"]."' ";
        //開講しない講座を含む場合
        } else {
            $query .= " AND t1.CHAIRCD IN ".$attend_chaircd." ";
        }
        if ($model->field["COURSE"] == 1 || $model->field["COURSE"] == 4) {
            $query .= " AND t1.PERIODCD     = '".$model->field["PERIOD"]."' ";
            $query .= " AND t1.STAFFCD      = '".$model->field["STAFF"]."' ";
        } else if ($model->field["COURSE"] == 3) {
            $query .= " AND t1.PERIODCD     = '".$model->field["PERIOD"]."' ";
        }
        $query .= " AND t1.SCHOOLINGKINDCD = '".$model->field["COURSE"]."' ";
        $query .= " ORDER BY RECEIPT_DATE DESC, RECEIPT_TIME DESC, t1.SCHREGNO ";

        return $query;
    }

    //講座登録確認
    function getCheckout($model, $rowcheck) {
        $query  = " SELECT DISTINCT SCHREGNO ";
        $query .= " FROM CHAIR_STD_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".$model->Year."' ";
        $query .= " AND SEMESTER    = '".$model->semester ."' ";
        if (!isset($rowcheck["CHAIRCD"])) {
            $query .= " AND CHAIRCD     = '".$model->field["CHAIR"]."' ";
        } else {
            $query .= " AND CHAIRCD     = '".$rowcheck["CHAIRCD"]."' ";
        }
        $query .= " AND SCHREGNO    = '".$model->field["SCHREGNO"]."' ";

        return $query;
    }

    ///開講しない科目、講座情報チェック
    function getAttendChairDataCheck($model) {
        $query .= "  WITH CHAIR_DATA AS ( ";
        $query .= "  SELECT DISTINCT ";
        $query .= "      T1.CLASSCD, ";
        $query .= "      T1.SCHOOL_KIND, ";
        $query .= "      T1.CURRICULUM_CD, ";
        $query .= "      T1.SUBCLASSCD ";
        $query .= "  FROM ";
        $query .= "      CHAIR_DAT T1 ";
        $query .= "  WHERE ";
        $query .= "      T1.YEAR      = '".$model->Year."' ";
        $query .= "  AND T1.SEMESTER  = '".$model->semester ."'  ";
        $query .= "  AND T1.CHAIRCD   = '".$model->field["CHAIR"]."' ";
        $query .= "  ) ";
        $query .= "  SELECT ";
        $query .= "      COUNT(*) AS CNT ";
        $query .= "  FROM ";
        $query .= "      CHAIR_DATA T1, ";
        $query .= "      SUBCLASS_SCHOOLING_COMBINED_DAT T2 ";
        $query .= "  WHERE ";
        $query .= "      T1.CLASSCD = T2.COMBINED_CLASSCD ";
        $query .= "  AND T1.SCHOOL_KIND = T2.COMBINED_SCHOOL_KIND ";
        $query .= "  AND T1.CURRICULUM_CD = T2.COMBINED_CURRICULUM_CD ";
        $query .= "  AND T1.SUBCLASSCD = T2.COMBINED_SUBCLASSCD ";
        $query .= "  AND T2.YEAR = '".$model->Year."' ";

        return $query;
    }

    ///開講しない科目、講座、学籍番号情報取得
    function getAttendChairData($model, $flg) {
        $query .= "  WITH CHAIR_DATA AS ( ";
        $query .= "  SELECT DISTINCT ";
        $query .= "      T1.CLASSCD, ";
        $query .= "      T1.SCHOOL_KIND, ";
        $query .= "      T1.CURRICULUM_CD, ";
        $query .= "      T1.SUBCLASSCD ";
        $query .= "  FROM ";
        $query .= "      CHAIR_DAT T1 ";
        $query .= "  WHERE ";
        $query .= "      T1.YEAR      = '".$model->Year."' ";
        $query .= "  AND T1.SEMESTER  = '".$model->semester ."'  ";
        $query .= "  AND T1.CHAIRCD   = '".$model->field["CHAIR"]."' ";
        $query .= "  ), COMBINED_DATA AS ( ";
        $query .= "  SELECT ";
        $query .= "      T2.* ";
        $query .= "  FROM ";
        $query .= "      CHAIR_DATA T1, ";
        $query .= "      SUBCLASS_SCHOOLING_COMBINED_DAT T2 ";
        $query .= "  WHERE ";
        $query .= "      T1.CLASSCD = T2.COMBINED_CLASSCD ";
        $query .= "  AND T1.SCHOOL_KIND = T2.COMBINED_SCHOOL_KIND ";
        $query .= "  AND T1.CURRICULUM_CD = T2.COMBINED_CURRICULUM_CD ";
        $query .= "  AND T1.SUBCLASSCD = T2.COMBINED_SUBCLASSCD ";
        $query .= "  AND T2.YEAR = '".$model->Year."' ";
        if ($flg) {
            $query .= "  ), CHECK_DAT AS ( ";
        } else {
            $query .= "  ) ";
        }
        $query .= "  SELECT ";
        $query .= "      T1.* ";
        $query .= "  FROM ";
        $query .= "      CHAIR_DAT T1 ";
        $query .= "      INNER JOIN COMBINED_DATA L1 ON L1.ATTEND_CLASSCD = T1.CLASSCD ";
        $query .= "                                AND L1.ATTEND_SCHOOL_KIND = T1.SCHOOL_KIND  ";
        $query .= "                                AND L1.ATTEND_CURRICULUM_CD = T1.CURRICULUM_CD  ";
        $query .= "                                AND L1.ATTEND_SUBCLASSCD = T1.SUBCLASSCD ";
        $query .= "  WHERE ";
        $query .= "      T1.YEAR = '".$model->Year."' ";
        $query .= "  AND T1.SEMESTER = '".$model->semester ."' ";
        if ($flg) {
            $query .= "  ) ";
            $query .= "  SELECT DISTINCT ";
            $query .= "      T1.SCHREGNO, T1.CHAIRCD ";
            $query .= "  FROM ";
            $query .= "      CHAIR_STD_DAT T1 ";
            $query .= "      INNER JOIN CHECK_DAT L1 ON L1.YEAR = T1.YEAR ";
            $query .= "                             AND L1.SEMESTER = T1.SEMESTER ";
            $query .= "                             AND L1.CHAIRCD = T1.CHAIRCD ";
            $query .= "  WHERE ";
            $query .= "      T1.SCHREGNO = '".$model->field["SCHREGNO"]."' ";
        }
        
        return $query;
    }

    //講座登録確認
    function getCheckSchChrT($model, $rowcheck) {
        $query  = " SELECT COUNT(*) AS CNT ";
        $query .= " FROM SCH_CHR_T_DAT ";
        $query .= " WHERE ";
        $query .= "     EXECUTEDATE = '".str_replace("/","-",$model->Date)."' ";
        $query .= " AND PERIODCD    = '".$model->field["PERIOD"]."' ";
        if (!isset($rowcheck["CHAIRCD"])) {
            $query .= " AND CHAIRCD     = '".$model->field["CHAIR"]."' ";
        } else {
            $query .= " AND CHAIRCD     = '".$rowcheck["CHAIRCD"]."' ";
        }

        return $query;
    }

    //サブフォームデータ
    function getsubremark($model) {
        $query  = " SELECT t1.SCHREGNO, t2.NAME_SHOW, t1.REMARK ";
        $query .= " FROM SCH_ATTEND_DAT t1 left join SCHREG_BASE_MST t2 on t1.SCHREGNO = t2.SCHREGNO ";
        $query .= " WHERE ";
        $query .= "     t1.YEAR         = '" .$model->Year."' ";
        $query .= " AND t1.SCHREGNO     = '".$model->sch."' ";
        $query .= " AND t1.EXECUTEDATE  = '" .str_replace("/","-",$model->date)."' ";
        $query .= " AND t1.CHAIRCD      = '".$model->chir."' ";
        if ($model->crs == 1 || $model->crs == 4) {
            $query .= " AND t1.PERIODCD     = '".$model->peri."' ";
            $query .= " AND t1.STAFFCD      = '".$model->stf."' ";
        } else if ($model->crs == 3) {
            $query .= " AND t1.PERIODCD     = '".$model->peri."' ";
        }
        $query .= " AND t1.SCHOOLINGKINDCD  = '".$model->crs."' ";
        $query .= " AND t1.RECEIPT_TIME     = '".$model->time."' ";

        return $query;
    }

    //登録済みチェック
    function getCheckDatePeriod($model, $flg, $rowcheck = '') {
        $query  = " SELECT SCHREGNO ";
        $query .= " FROM SCH_ATTEND_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '" .$model->Year."' ";
        $query .= " AND SCHREGNO    = '".$model->field["SCHREGNO"]."' ";
        $query .= " AND EXECUTEDATE = '" .str_replace("/","-",$model->Date)."' ";
        if ($flg == 1) {
            $query .= " AND PERIODCD    = '" .$model->field["PERIOD"]."' ";
        } else if ($flg == 2) {
            if (!isset($rowcheck["CHAIRCD"])) {
                $query .= " AND CHAIRCD     = '".$model->field["CHAIR"]."' ";
            } else {
                $query .= " AND CHAIRCD     = '".$rowcheck["CHAIRCD"]."' ";
            }
        }
        $query .= " AND SCHOOLINGKINDCD <> '2' ";
        return $query;
    }

    //登録済みチェック（登校スクーリングのみ）
    function getCheckIns($model, $rowcheck) {
        $query  = " SELECT SCHREGNO ";
        $query .= " FROM SCH_ATTEND_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '" .$model->Year."' ";
        $query .= " AND SCHREGNO    = '".$model->field["SCHREGNO"]."' ";
        $query .= " AND EXECUTEDATE = '" .str_replace("/","-",$model->Date)."' ";
        if (!isset($rowcheck["CHAIRCD"])) {
            $query .= " AND CHAIRCD     = '".$model->field["CHAIR"]."' ";
        } else {
            $query .= " AND CHAIRCD     = '".$rowcheck["CHAIRCD"]."' ";
        }
        $query .= " AND PERIODCD    = '" .$model->field["PERIOD"]."' ";
        $query .= " AND SCHOOLINGKINDCD = '".$model->field["COURSE"]."' ";

        return $query;
    }

    //INSERT
    function &getInsertQuery($model, $rowcheck) {

        $query  = " INSERT INTO SCH_ATTEND_DAT ";
        $query .= " (YEAR, SCHREGNO, EXECUTEDATE, CHAIRCD, PERIODCD, ";
        $query .= "  RECEIPT_DATE, RECEIPT_TIME, STAFFCD, TERMINAL_CD, SCHOOLINGKINDCD, ";
        $query .= "  REMARK, CREDIT_TIME, REGISTERCD, UPDATED) ";
        $query .= " VALUES( ";
        $query .= "     '".$model->Year."', ";
        $query .= "     '".$model->field["SCHREGNO"]."', ";
        $query .= "     '".str_replace("/","-",$model->Date)."', ";
        if (!isset($rowcheck["CHAIRCD"])) {
            $query .= "     '".$model->field["CHAIR"]."', ";
        } else {
            $query .= "     '".$rowcheck["CHAIRCD"]."', ";
        }
        if ($model->field["COURSE"] == 1 || $model->field["COURSE"] == 3 || $model->field["COURSE"] == 4) {
            $query .= "     '".$model->field["PERIOD"]."', ";
        } else {
            $query .= "     '', ";
        }
        $query .= "     '".CTRL_DATE."', ";
        $query .= "     current time, ";
        if ($model->field["COURSE"] == 1 || $model->field["COURSE"] == 4) {
            $query .= "     '".$model->field["STAFF"]."', ";
        } else {
            $query .= "     '', ";
        }
        $query .= "     '', ";
        $query .= "     '".$model->field["COURSE"]."', ";
        if ($model->field["REMARK"] == '') {
            $query .= "     '無', ";
        } else {
            $query .= "     '".$model->field["REMARK"]."', ";
        }
        if ($model->field["COURSE"] == 1 || $model->field["COURSE"] == 4 || !$model->field["CREDIT_TIME"]) {
            $query .= "    NULL, ";
        } else {
            $query .= "     ".sprintf("%01.1f", $model->field["CREDIT_TIME"]).", ";
        }
        $query .= "     '".STAFFCD."', ";
        $query .= "     current timestamp ";
        $query .= " ) ";

        return $query;
    }

    //UPDATE
    //実施区分設定
    function getUpdateSchchr($model, $rowcheck) {

        $query  = " UPDATE ";
        $query .= "     SCH_CHR_T_DAT ";
        $query .= " SET ";
        $query .= "     EXECUTED = '1' ";
        $query .= " WHERE ";
        $query .= "     EXECUTEDATE = '".str_replace("/","-",$model->Date)."' ";
        $query .= " AND PERIODCD    = '".$model->field["PERIOD"]."' ";
        if (!isset($rowcheck["CHAIRCD"])) {
            $query .= " AND CHAIRCD     = '".$model->field["CHAIR"]."' ";
        } else {
            $query .= " AND CHAIRCD     = '".$rowcheck["CHAIRCD"]."' ";
        }

        return $query;
    }

    //UPDATE 
    function &getUpdateQuery($model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $query  = " UPDATE ";
        $query .= "     SCH_ATTEND_DAT ";
        $query .= " SET  ";
        if (VARS::post("REMARKSUB") == '') {
            $query .= "  REMARK = '無', ";
        } else {
            $query .= "  REMARK = '".VARS::post("REMARKSUB")."', ";
        }
        $query .= "     REGISTERCD = '".STAFFCD."', ";
        $query .= "     UPDATED = sysdate() ";
        $query .= " WHERE  ";
        $query .= "     YEAR        = '" .$model->Year."' ";
        $query .= " AND SCHREGNO    = '".$model->sch."' ";
        $query .= " AND EXECUTEDATE = '" .str_replace("/","-",$model->date)."' ";
        $query .= " AND CHAIRCD     = '".$model->chir."' ";
        if ($model->crs == 1 || $model->crs == 4) {
            $query .= " AND PERIODCD    = '".$model->peri."' ";
            $query .= " AND STAFFCD     = '".$model->stf."' ";
        } else if ($model->crs == 3) {
            $query .= " AND PERIODCD    = '".$model->peri."' ";
        }
        $query .= " AND SCHOOLINGKINDCD = '".$model->crs."' ";
        $query .= " AND RECEIPT_TIME    = '".$model->time."' ";

        $db->query($query);
        $db->commit();      // トランザクションをコミットする。
        Query::dbCheckIn($db);
        return $query;
    }

    //DELETE 
    function &getDeleteQuery($model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $query  = " DELETE FROM ";
        $query .= "     SCH_ATTEND_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".$model->Year."' ";
        $query .= " AND EXECUTEDATE = '".str_replace("/","-",$model->Date)."' ";
        $query .= " AND CHAIRCD     = '".$model->field["CHAIR"]."' ";
        if ($model->field["COURSE"] == 1 || $model->field["COURSE"] == 4) {
            $query .= " AND PERIODCD    = '".$model->field["PERIOD"]."' ";
            $query .= " AND STAFFCD     = '".$model->field["STAFF"]."' ";
        } else if ($model->field["COURSE"] == 3) {
            $query .= " AND PERIODCD    = '".$model->field["PERIOD"]."' ";
        }
        $query .= " AND SCHOOLINGKINDCD = '".$model->field["COURSE"]."' ";

        $db->query($query);
        $db->commit();      // トランザクションをコミットする。
        Query::dbCheckIn($db);
        return ;
    }

    //DELETE
    function &getDeleteQuerysitei($model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //開講しない講座と開講する講座コードを配列に格納
        $attend_chaircd = "('";
        $conma = "";
        $set_array = array();
        $set_array = explode(",", $model->Attendchaircd);
        foreach ($set_array as $var) {
            $attend_chaircd .= $conma.$var;
            $conma = "', '";
        }
        $attend_chaircd .= "')";

        for ($i = 0; $i < get_count($model->schnodel); $i++) {
            list($schregno, $receipt_time) = preg_split("/,/",$model->schnodel[$i]);

            $query  = " DELETE FROM ";
            $query .= "     SCH_ATTEND_DAT ";
            $query .= " WHERE  ";
            $query .= "     YEAR        = '".$model->Year."' ";
            $query .= " AND SCHREGNO    = '".$schregno."' ";
            $query .= " AND EXECUTEDATE = '".str_replace("/","-",$model->Date)."' ";
            if (!$model->Attendchaircd) {
                $query .= " AND CHAIRCD     = '".$model->field["CHAIR"]."' ";
            //開講しない講座を含む場合
            } else {
                $query .= " AND CHAIRCD IN ".$attend_chaircd." ";
            }
            if ($model->field["COURSE"] == 1 || $model->field["COURSE"] == 4) {
                $query .= " AND PERIODCD    = '".$model->field["PERIOD"]."' ";
                $query .= " AND STAFFCD     = '".$model->field["STAFF"]."' ";
            } else if ($model->field["COURSE"] == 3) {
                $query .= " AND PERIODCD    = '".$model->field["PERIOD"]."' ";
            }
            $query .= " AND SCHOOLINGKINDCD = '".$model->field["COURSE"]."' ";
            $query .= " AND RECEIPT_TIME    = '".$receipt_time."' ";

            $db->query($query);
        }

        $db->commit(); // トランザクションをコミットする。
        Query::dbCheckIn($db);
        return ;
    }
}
?>
