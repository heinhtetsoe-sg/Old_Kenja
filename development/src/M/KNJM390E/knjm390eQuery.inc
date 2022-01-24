<?php

require_once('for_php7.php');

class knjm390eQuery extends Query {

    //校時取得
    function selectName($cd1) {
        $query  = " SELECT ";
        $query .= "     NAME1 AS LABEL, ";
        $query .= "     NAMECD2 AS VALUE ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR    = '".CTRL_YEAR."' ";
        $query .= "     AND NAMECD1 = '".$cd1."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //校時グループ取得
    function selectNameB001($namecd2) {
        $query  = " SELECT ";
        $query .= "     NAME2 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR    = '".CTRL_YEAR."' ";
        $query .= "     AND NAMECD1 = 'B001' ";
        $query .= "     AND NAMECD2 = '{$namecd2}' ";

        return $query;
    }

    //講座取得（権限チェック）
    function getAuth(&$model) {
        //参照・更新可
        if (AUTHORITY == DEF_UPDATABLE) {
            $model->User = 0;
            $query  = " SELECT DISTINCT ";
            $query .= "     T1.CHAIRCD AS VALUE, ";
            $query .= "     T1.CHAIRNAME AS LABEL ";
            $query .= " FROM ";
            $query .= "     CHAIR_DAT T1 ";
            $query .= "     LEFT JOIN SUBCLASS_SCHOOLING_COMBINED_DAT T2 ON T1.CLASSCD       = T2.ATTEND_CLASSCD ";
            $query .= "                                                 AND T1.SCHOOL_KIND   = T2.ATTEND_SCHOOL_KIND ";
            $query .= "                                                 AND T1.CURRICULUM_CD = T2.ATTEND_CURRICULUM_CD ";
            $query .= "                                                 AND T1.SUBCLASSCD    = T2.ATTEND_SUBCLASSCD ";
            $query .= "                                                 AND T2.YEAR          = '".$model->Year."' ";
            $query .= " WHERE ";
            $query .= "         T1.YEAR      = '".$model->Year."' ";
            $query .= "     AND T1.SEMESTER  = '".$model->semester."'  ";
            $query .= "     AND T1.CLASSCD   < '91' ";
            $query .= "     AND T2.ATTEND_CLASSCD       IS NULL ";
            $query .= "     AND T2.ATTEND_SCHOOL_KIND   IS NULL ";
            $query .= "     AND T2.ATTEND_CURRICULUM_CD IS NULL ";
            $query .= "     AND T2.ATTEND_SUBCLASSCD    IS NULL ";
            $query .= " ORDER BY ";
            $query .= "     VALUE ";
        } else {
        //参照・更新可（制限付き）
            $model->User = 1;
            $query  = " SELECT DISTINCT ";
            $query .= "     T1.CHAIRCD AS VALUE, ";
            $query .= "     T1.CHAIRNAME AS LABEL ";
            $query .= " FROM ";
            $query .= "     CHAIR_DAT T1 ";
            $query .= "     LEFT JOIN SUBCLASS_SCHOOLING_COMBINED_DAT T2 ON T1.CLASSCD       = T2.ATTEND_CLASSCD ";
            $query .= "                                                 AND T1.SCHOOL_KIND   = T2.ATTEND_SCHOOL_KIND ";
            $query .= "                                                 AND T1.CURRICULUM_CD = T2.ATTEND_CURRICULUM_CD ";
            $query .= "                                                 AND T1.SUBCLASSCD    = T2.ATTEND_SUBCLASSCD ";
            $query .= "                                                 AND T2.YEAR          = '".$model->Year."' ";
            $query .= " WHERE ";
            $query .= "         T1.YEAR      = '" .$model->Year ."'";
            $query .= "     AND T1.SEMESTER  = '" .$model->semester ."'";
            $query .= "     AND T1.CLASSCD   < '91' ";
            $query .= "     AND T2.ATTEND_CLASSCD       IS NULL ";
            $query .= "     AND T2.ATTEND_SCHOOL_KIND   IS NULL ";
            $query .= "     AND T2.ATTEND_CURRICULUM_CD IS NULL ";
            $query .= "     AND T2.ATTEND_SUBCLASSCD    IS NULL ";
            $query .= "     AND T1.CHAIRCD in ( SELECT ";
            $query .= "                             C1.CHAIRCD ";
            $query .= "                         FROM ";
            $query .= "                             CHAIR_STF_DAT C1 ";
            $query .= "                         WHERE ";
            $query .= "                                 C1.YEAR     ='".$model->Year."'";
            $query .= "                             AND C1.SEMESTER ='".$model->semester."'";
            $query .= "                             AND C1.STAFFCD  ='".STAFFCD."' ";            
            $query .= "                        ) ";
            $query .= " ORDER BY ";
            $query .= "     VALUE ";
        }

        return $query;
    }

    //担当者取得
    function selectStaff($model) {
        $query  = " SELECT ";
        $query .= "     w1.STAFFCD, ";
        $query .= "     w1.CHARGEDIV, ";
        $query .= "     w2.STAFFNAME ";
        $query .= " FROM ";
        $query .= "     CHAIR_STF_DAT w1 ";
        $query .= "     LEFT JOIN V_STAFF_MST w2 ON w1.STAFFCD = w2.STAFFCD ";
        $query .= "                             and w2.YEAR    = '" .CTRL_YEAR."' ";
        $query .= " WHERE ";
        $query .= "         w1.YEAR     = '".CTRL_YEAR."' ";
        $query .= "     AND w1.SEMESTER = '".$model->semester ."' ";
        $query .= "     AND w1.CHAIRCD  = '".$model->field["CHAIR"]."' ";
        $query .= " ORDER BY ";
        $query .= "     w1.CHARGEDIV DESC, ";
        $query .= "     w1.STAFFCD ";

        return $query;
    }

    //最大時間数取得
    function getCreditTime($model) {
        $query  = " SELECT ";
        $query .= "     NAME3 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR    = '".CTRL_YEAR."' ";
        $query .= "     AND NAMECD1 = 'B001' ";
        $query .= "     AND NAMECD2 = '".$model->field["PERIOD"]."' ";

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
        
        $query  = " SELECT ";
        $query .= "     t1.SCHREGNO, ";
        $query .= "     t2.NAME_SHOW, ";
        $query .= "     CREDIT_TIME, ";
        $query .= "     RECEIPT_TIME, ";
        $query .= "     t1.REMARK ";
        $query .= " FROM ";
        $query .= "     SCH_ATTEND_DAT t1  ";
        $query .= "     LEFT JOIN SCHREG_BASE_MST t2 ON t1.SCHREGNO = t2.SCHREGNO ";
        $query .= " WHERE ";
        $query .= "         t1.YEAR         = '".$model->Year."' ";
        $query .= "     AND t1.EXECUTEDATE  = '".str_replace("/","-",$model->Date)."' ";
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
        $query .= " ORDER BY ";
        $query .= "     RECEIPT_DATE DESC, ";
        $query .= "     RECEIPT_TIME DESC, ";
        $query .= "     t1.SCHREGNO ";

        return $query;
    }

    // 単位時間取得
    function getCreditTimeCnt($model, $schregNo) {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     SCH_ATTEND_DAT t1  ";
        $query .= "     LEFT JOIN V_NAME_MST B001 ON B001.YEAR    = t1.YEAR ";
        $query .= "                              AND B001.NAMECD1 = 'B001' ";
        $query .= "                              AND B001.NAMECD2 = t1.PERIODCD ";
        $query .= " WHERE ";
        $query .= "         t1.YEAR        = '".$model->Year."' ";
        $query .= "     AND t1.SCHREGNO    = '".$schregNo."'";
        $query .= "     AND t1.EXECUTEDATE = '".str_replace("/","-",$model->Date)."' ";
        $query .= "     AND t1.CHAIRCD     = '".$model->field["CHAIR"]."' ";
        if ($model->field["COURSE"] == 1 || $model->field["COURSE"] == 4) {
            $query .= " AND t1.STAFFCD      = '".$model->field["STAFF"]."' ";
        }
        $query .= "     AND t1.SCHOOLINGKINDCD = '".$model->field["COURSE"]."' ";
        $query .= "     AND B001.NAME2 = '".$model->reriodGrp."' ";

        return $query;
    }

    //講座登録確認
    function getCheckout($model, $rowcheck) {
        $query  = " SELECT DISTINCT ";
        $query .= "     SCHREGNO ";
        $query .= " FROM ";
        $query .= "     CHAIR_STD_DAT ";
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
        $query .= " WITH CHAIR_DATA AS ( ";
        $query .= "     SELECT DISTINCT ";
        $query .= "         T1.CLASSCD, ";
        $query .= "         T1.SCHOOL_KIND, ";
        $query .= "         T1.CURRICULUM_CD, ";
        $query .= "         T1.SUBCLASSCD ";
        $query .= "     FROM ";
        $query .= "         CHAIR_DAT T1 ";
        $query .= "     WHERE ";
        $query .= "         T1.YEAR      = '".$model->Year."' ";
        $query .= "     AND T1.SEMESTER  = '".$model->semester ."'  ";
        $query .= "     AND T1.CHAIRCD   = '".$model->field["CHAIR"]."' ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     CHAIR_DATA T1, ";
        $query .= "     SUBCLASS_SCHOOLING_COMBINED_DAT T2 ";
        $query .= " WHERE ";
        $query .= "     T1.CLASSCD       = T2.COMBINED_CLASSCD ";
        $query .= " AND T1.SCHOOL_KIND   = T2.COMBINED_SCHOOL_KIND ";
        $query .= " AND T1.CURRICULUM_CD = T2.COMBINED_CURRICULUM_CD ";
        $query .= " AND T1.SUBCLASSCD    = T2.COMBINED_SUBCLASSCD ";
        $query .= " AND T2.YEAR          = '".$model->Year."' ";

        return $query;
    }

    ///開講しない科目、講座、学籍番号情報取得
    function getAttendChairData($model, $flg) {
        $query .= " WITH CHAIR_DATA AS ( ";
        $query .= "     SELECT DISTINCT ";
        $query .= "         T1.CLASSCD, ";
        $query .= "         T1.SCHOOL_KIND, ";
        $query .= "         T1.CURRICULUM_CD, ";
        $query .= "         T1.SUBCLASSCD ";
        $query .= "     FROM ";
        $query .= "         CHAIR_DAT T1 ";
        $query .= "     WHERE ";
        $query .= "         T1.YEAR     = '".$model->Year."' ";
        $query .= "     AND T1.SEMESTER = '".$model->semester ."'  ";
        $query .= "     AND T1.CHAIRCD  = '".$model->field["CHAIR"]."' ";
        $query .= " ), COMBINED_DATA AS ( ";
        $query .= "     SELECT ";
        $query .= "         T2.* ";
        $query .= "     FROM ";
        $query .= "         CHAIR_DATA T1, ";
        $query .= "         SUBCLASS_SCHOOLING_COMBINED_DAT T2 ";
        $query .= "     WHERE ";
        $query .= "         T1.CLASSCD       = T2.COMBINED_CLASSCD ";
        $query .= "     AND T1.SCHOOL_KIND   = T2.COMBINED_SCHOOL_KIND ";
        $query .= "     AND T1.CURRICULUM_CD = T2.COMBINED_CURRICULUM_CD ";
        $query .= "     AND T1.SUBCLASSCD    = T2.COMBINED_SUBCLASSCD ";
        $query .= "     AND T2.YEAR          = '".$model->Year."' ";
        if ($flg) {
            $query .= "  ), CHECK_DAT AS ( ";
        } else {
            $query .= "  ) ";
        }
        $query .= " SELECT ";
        $query .= "     T1.* ";
        $query .= " FROM ";
        $query .= "     CHAIR_DAT T1 ";
        $query .= "     INNER JOIN COMBINED_DATA L1 ON L1.ATTEND_CLASSCD = T1.CLASSCD ";
        $query .= "                               AND L1.ATTEND_SCHOOL_KIND = T1.SCHOOL_KIND  ";
        $query .= "                               AND L1.ATTEND_CURRICULUM_CD = T1.CURRICULUM_CD  ";
        $query .= "                               AND L1.ATTEND_SUBCLASSCD = T1.SUBCLASSCD ";
        $query .= " WHERE ";
        $query .= "         T1.YEAR = '".$model->Year."' ";
        $query .= "     AND T1.SEMESTER = '".$model->semester ."' ";
        if ($flg) {
            $query .= "  ) ";
            $query .= " SELECT DISTINCT ";
            $query .= "     T1.SCHREGNO, T1.CHAIRCD ";
            $query .= " FROM ";
            $query .= "     CHAIR_STD_DAT T1 ";
            $query .= "     INNER JOIN CHECK_DAT L1 ON L1.YEAR     = T1.YEAR ";
            $query .= "                            AND L1.SEMESTER = T1.SEMESTER ";
            $query .= "                            AND L1.CHAIRCD  = T1.CHAIRCD ";
            $query .= " WHERE ";
            $query .= "     T1.SCHREGNO = '".$model->field["SCHREGNO"]."' ";
        }
        
        return $query;
    }

    //講座登録確認
    function getCheckSchChrT($model, $rowcheck, $periodCd) {
        $query  = " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     SCH_CHR_T_DAT ";
        $query .= " WHERE ";
        $query .= "         EXECUTEDATE = '".str_replace("/","-",$model->Date)."' ";
        $query .= "     AND PERIODCD    = '".$periodCd."' ";
        if (!isset($rowcheck["CHAIRCD"])) {
            $query .= " AND CHAIRCD     = '".$model->field["CHAIR"]."' ";
        } else {
            $query .= " AND CHAIRCD     = '".$rowcheck["CHAIRCD"]."' ";
        }

        return $query;
    }

    //サブフォームデータ
    function getsubremark($model) {
        $query  = " SELECT ";
        $query .= "     t1.SCHREGNO, ";
        $query .= "     t2.NAME_SHOW, ";
        $query .= "     t1.REMARK ";
        $query .= " FROM ";
        $query .= "     SCH_ATTEND_DAT t1 ";
        $query .= "     left join SCHREG_BASE_MST t2 on t1.SCHREGNO = t2.SCHREGNO  ";
        $query .= " WHERE ";
        $query .= "         t1.YEAR        = '" .$model->Year."' ";
        $query .= "     AND t1.SCHREGNO    = '".$model->sch."' ";
        $query .= "     AND t1.EXECUTEDATE = '" .str_replace("/","-",$model->date)."' ";
        $query .= "     AND t1.CHAIRCD     = '".$model->chir."' ";
        if ($model->crs == 1 || $model->crs == 4) {
            $query .= " AND t1.PERIODCD    = '".$model->peri."' ";
            $query .= " AND t1.STAFFCD     = '".$model->stf."' ";
        } else if ($model->crs == 3) {
            $query .= " AND t1.PERIODCD    = '".$model->peri."' ";
        }
        $query .= " AND t1.SCHOOLINGKINDCD = '".$model->crs."' ";
        $query .= " AND t1.RECEIPT_TIME    = '".$model->time."' ";

        return $query;
    }

    //登録済みチェック
    function getCheckDatePeriod($model, $flg, $rowcheck = '') {
        $query  = " SELECT ";
        $query .= "     SCHREGNO ";
        $query .= " FROM ";
        $query .= "     SCH_ATTEND_DAT ";
        $query .= " WHERE ";
        $query .= "         YEAR        = '" .$model->Year."' ";
        $query .= "     AND SCHREGNO    = '".$model->field["SCHREGNO"]."' ";
        $query .= "     AND EXECUTEDATE = '" .str_replace("/","-",$model->Date)."' ";
        if ($flg == 1) {
            $query .= " AND PERIODCD    = '" .$model->field["PERIOD"]."' ";
        } else if ($flg == 2) {
            if (!isset($rowcheck["CHAIRCD"])) {
                $query .= " AND CHAIRCD = '".$model->field["CHAIR"]."' ";
            } else {
                $query .= " AND CHAIRCD = '".$rowcheck["CHAIRCD"]."' ";
            }
        }
        $query .= " AND SCHOOLINGKINDCD <> '2' ";

        return $query;
    }

    //登録済みチェック（登校スクーリングのみ）
    function getCheckIns($model, $rowcheck) {
        $query  = " SELECT ";
        $query .= "     SCHREGNO ";
        $query .= " FROM ";
        $query .= "     SCH_ATTEND_DAT ";
        $query .= " WHERE ";
        $query .= "         YEAR        = '" .$model->Year."' ";
        $query .= "     AND SCHREGNO    = '".$model->field["SCHREGNO"]."' ";
        $query .= "     AND EXECUTEDATE = '" .str_replace("/","-",$model->Date)."' ";
        if (!isset($rowcheck["CHAIRCD"])) {
            $query .= " AND CHAIRCD     = '".$model->field["CHAIR"]."' ";
        } else {
            $query .= " AND CHAIRCD     = '".$rowcheck["CHAIRCD"]."' ";
        }
        $query .= "     AND PERIODCD    = '" .$model->field["PERIOD"]."' ";
        $query .= "     AND SCHOOLINGKINDCD = '".$model->field["COURSE"]."' ";

        return $query;
    }

    //DELETE
    function &getDeleteSchAttendDat($model, $rowcheck, $periodCd) {
        $query  = " DELETE FROM ";
        $query .= "     SCH_ATTEND_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".$model->Year."' ";
        $query .= " AND SCHREGNO    = '".$model->field["SCHREGNO"]."' ";
        $query .= " AND EXECUTEDATE = '".str_replace("/","-",$model->Date)."' ";
        if (!isset($rowcheck["CHAIRCD"])) {
            $query .= " AND CHAIRCD = '".$model->field["CHAIR"]."' ";
        } else {
            $query .= " AND CHAIRCD = '".$rowcheck["CHAIRCD"]."' ";
        }
        $query .= " AND PERIODCD    = '".$periodCd."' ";

        return $query;
    }

    //INSERT
    function &getInsertQuery($model, $rowcheck, $periodCd) {
        $data = array();
        $data["YEAR"][TEXT]             = $model->Year;
        $data["SCHREGNO"][TEXT]         = $model->field["SCHREGNO"];
        $data["EXECUTEDATE"][DATE]      = str_replace("/","-",$model->Date);
        if (!isset($rowcheck["CHAIRCD"])) {
            $data["CHAIRCD"][TEXT]      = $model->field["CHAIR"];
        } else {
            $data["CHAIRCD"][TEXT]      = $rowcheck["CHAIRCD"];
        }
        if ($model->field["COURSE"] == 1 || $model->field["COURSE"] == 3 || $model->field["COURSE"] == 4) {
            $data["PERIODCD"][TEXT]     = $periodCd;
        }
        $data["RECEIPT_DATE"][DATE]     = CTRL_DATE;
        $data["RECEIPT_TIME"][TEXT]     = date('H:i:s');
        if ($model->field["COURSE"] == 1 || $model->field["COURSE"] == 4) {
            $data["STAFFCD"][TEXT]      = $model->field["STAFF"];
        }
        $data["SCHOOLINGKINDCD"][TEXT]  = $model->field["COURSE"];
        if ($model->field["REMARK"] == '') {
            $data["REMARK"][TEXT]       = '無';
        } else {
            $data["REMARK"][TEXT]       = $model->field["REMARK"];
        }
        $data["CREDIT_TIME"][TEXT]      = ($model->field["CREDIT_TIME"] != '') ? '1.0': '';

        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][FUNC]          = "sysdate()";

        $query = Query::insertSQL($data, "SCH_ATTEND_DAT");

        return $query;
    }

    //UPDATE
    //実施区分設定
    function getUpdateSchchr($model, $rowcheck, $periodCd) {

        $query  = " UPDATE ";
        $query .= "     SCH_CHR_T_DAT ";
        $query .= " SET ";
        $query .= "     EXECUTED = '1' ";
        $query .= " WHERE ";
        $query .= "         EXECUTEDATE = '".str_replace("/","-",$model->Date)."' ";
        $query .= "     AND PERIODCD    = '".$periodCd."' ";
        if (!isset($rowcheck["CHAIRCD"])) {
            $query .= "     AND CHAIRCD     = '".$model->field["CHAIR"]."' ";
        } else {
            $query .= "     AND CHAIRCD     = '".$rowcheck["CHAIRCD"]."' ";
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

            if ($model->field["CREDIT_TIME"] > 1) {
                $query  = " DELETE FROM ";
                $query .= "     SCH_ATTEND_DAT ";
                $query .= " WHERE  ";
                $query .= "     YEAR        = '".$model->Year."' ";
                $query .= " AND SCHREGNO    = '".$schregno."' ";
                $query .= " AND EXECUTEDATE = '".str_replace("/","-",$model->Date)."' ";
                if (!$model->Attendchaircd) {
                    $query .= " AND CHAIRCD = '".$model->field["CHAIR"]."' ";
                //開講しない講座を含む場合
                } else {
                    $query .= " AND CHAIRCD IN ".$attend_chaircd." ";
                }
                if ($model->field["COURSE"] == 1 || $model->field["COURSE"] == 4) {
                    $query .= " AND PERIODCD in (SELECT ";
                    $query .= "                      NAMECD2 ";
                    $query .= "                  FROM  ";
                    $query .= "                      V_NAME_MST ";
                    $query .= "                  WHERE ";
                    $query .= "                      YEAR    = '{$model->Year}' ";
                    $query .= "                  AND NAMECD1 = 'B001' ";
                    $query .= "                  AND NAME2   = '{$model->reriodGrp}')";
                    $query .= " AND STAFFCD = '".$model->field["STAFF"]."' ";
                } else if ($model->field["COURSE"] == 3) {
                    $query .= " AND PERIODCD in (SELECT ";
                    $query .= "                      NAMECD2 ";
                    $query .= "                  FROM  ";
                    $query .= "                      V_NAME_MST ";
                    $query .= "                  WHERE ";
                    $query .= "                      YEAR    = '{$model->Year}' ";
                    $query .= "                  AND NAMECD1 = 'B001' ";
                    $query .= "                  AND NAME2   = '{$model->reriodGrp}')";
                }
                $query .= " AND SCHOOLINGKINDCD = '".$model->field["COURSE"]."' ";

                $db->query($query);
            } else {
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
        }

        $db->commit(); // トランザクションをコミットする。
        Query::dbCheckIn($db);
        return ;
    }
}
?>
