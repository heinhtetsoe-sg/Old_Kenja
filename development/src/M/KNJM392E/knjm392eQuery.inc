<?php

require_once('for_php7.php');

class knjm392eQuery extends Query {

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
            $query .= "          T1.YEAR      = '".$model->Year."' ";
            $query .= "      AND T1.SEMESTER  = '".$model->semester."'  ";
            $query .= "      AND T1.CLASSCD   < '91' ";
            $query .= "      AND T2.ATTEND_CLASSCD       IS NULL ";
            $query .= "      AND T2.ATTEND_SCHOOL_KIND   IS NULL ";
            $query .= "      AND T2.ATTEND_CURRICULUM_CD IS NULL ";
            $query .= "      AND T2.ATTEND_SUBCLASSCD    IS NULL ";
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
            $query .= "                             C1.YEAR     ='".$model->Year."'";
            $query .= "                         AND C1.SEMESTER ='".$model->semester."'";
            $query .= "                         AND C1.STAFFCD  ='".STAFFCD."')";            
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

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

        $sortDiv = $model->sort[$model->getSort] == 1 ? "" : " DESC ";

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

        $query  = " WITH CHAIR_STD AS ( ";
        $query .= "     SELECT DISTINCT ";
        $query .= "         SCHREGNO ";
        $query .= "     FROM ";
        $query .= "         CHAIR_STD_DAT ";
        $query .= "     WHERE ";
        $query .= "         YEAR        = '".$model->Year."' ";
        $query .= "     AND SEMESTER    = '".$model->semester ."' ";
        if (!$model->Attendchaircd) {
            $query .= " AND CHAIRCD     = '".$model->field["CHAIR"]."' ";
        } else {
            $query .= " AND CHAIRCD    IN ".$attend_chaircd." ";
        }
        $query .= " ), ATTEND_DAT_0 AS ( ";
        $query .= "     SELECT ";
        $query .= "         YEAR, ";
        $query .= "         EXECUTEDATE, ";
        $query .= "         CHAIRCD, ";
        $query .= "         SCHOOLINGKINDCD, ";
        $query .= "         SCHREGNO, ";
        $query .= "         min(PERIODCD) as MIN_PERIODCD, ";
        $query .= "         COUNT(*) as CREDIT_CNT ";
        $query .= "     FROM ";
        $query .= "         SCH_ATTEND_DAT ";
        $query .= "     WHERE ";
        $query .= "             YEAR        = '".$model->Year."' ";
        $query .= "         AND EXECUTEDATE = '".str_replace("/","-",$model->Date)."' ";
        if (!$model->Attendchaircd) {
            $query .= " AND CHAIRCD     = '".$model->field["CHAIR"]."' ";
        } else {
            $query .= " AND CHAIRCD    IN ".$attend_chaircd." ";
        }
        $query .= "         AND SCHOOLINGKINDCD = '".$model->field["COURSE"]."' ";
        $query .= "         AND PERIODCD = '".$model->field["PERIOD"]."' ";
        $query .= "     GROUP BY ";
        $query .= "         YEAR, ";
        $query .= "         EXECUTEDATE, ";
        $query .= "         CHAIRCD, ";
        $query .= "         SCHOOLINGKINDCD, ";
        $query .= "         SCHREGNO ";
        $query .= " ), ATTEND_DAT AS ( ";
        $query .= "     SELECT ";
        $query .= "         T1.SCHREGNO, ";
        $query .= "         T2.MIN_PERIODCD, ";
        $query .= "         T2.CREDIT_CNT, ";
        $query .= "         T1.REMARK, ";
        $query .= "         T1.RECEIPT_TIME, ";
        $query .= "         T1.CHAIRCD ";
        $query .= "     FROM ";
        $query .= "         SCH_ATTEND_DAT T1 ";
        $query .= "         INNER JOIN ATTEND_DAT_0 T2 ON T2.YEAR         = T1.YEAR ";
        $query .= "                                   AND T2.EXECUTEDATE  = T1.EXECUTEDATE ";
        $query .= "                                   AND T2.CHAIRCD      = T1.CHAIRCD ";
        $query .= "                                   AND T2.SCHREGNO     = T1.SCHREGNO ";
        $query .= "                                   AND T2.MIN_PERIODCD = T1.PERIODCD ";
        $query .= "                                   AND T2.SCHOOLINGKINDCD = T1.SCHOOLINGKINDCD ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     t0.SCHREGNO, ";
        $query .= "     HDAT.HR_NAME || '-' || value(REGD.ATTENDNO, '') as HR_NAME_ATTENDNO, ";
        $query .= "     t2.NAME_SHOW, ";
        $query .= "     B001.NAME1 as PERIOD_NAME, ";
        $query .= "     ATT.CREDIT_CNT as CREDIT_TIME, ";
        $query .= "     ATT.REMARK, ";
        $query .= "     ATT.RECEIPT_TIME, ";
        $query .= "     ATT.CHAIRCD AS AT_CHAIRCD ";
        $query .= " FROM ";
        $query .= "     CHAIR_STD t0 ";
        $query .= "     LEFT JOIN SCHREG_BASE_MST t2 ON t2.SCHREGNO = t0.SCHREGNO ";
        $query .= "     LEFT JOIN SCHREG_REGD_DAT REGD ON REGD.SCHREGNO = t0.SCHREGNO ";
        $query .= "                                   AND REGD.YEAR     = '{$model->Year}' ";
        $query .= "                                   AND REGD.SEMESTER = '{$model->semester}' ";
        $query .= "     LEFT JOIN SCHREG_REGD_HDAT HDAT ON HDAT.YEAR     = REGD.YEAR ";
        $query .= "                                    AND HDAT.SEMESTER = REGD.SEMESTER ";
        $query .= "                                    AND HDAT.GRADE    = REGD.GRADE ";
        $query .= "                                    AND HDAT.HR_CLASS = REGD.HR_CLASS ";
        $query .= "     LEFT JOIN ATTEND_DAT ATT ON ATT.SCHREGNO = REGD.SCHREGNO ";
        $query .= "     LEFT JOIN V_NAME_MST B001 ON B001.YEAR    = REGD.YEAR ";
        $query .= "                              AND B001.NAMECD1 = 'B001' ";
        $query .= "                              AND B001.NAMECD2 = ATT.MIN_PERIODCD ";
        $query .= " ORDER BY ";
        if ($model->getSort == "SRT_SCHREGNO") {
            $query .= "     t0.SCHREGNO{$sortDiv} ";
        }
        if ($model->getSort == "SRT_HR_NAME") {
            $query .= "     REGD.GRADE || REGD.HR_CLASS || REGD.ATTENDNO {$sortDiv} ";
        }

        return $query;
    }

    //講座登録確認
    function getCheckout($model, $rowcheck, $schregno) {
        $query  = " SELECT DISTINCT ";
        $query .= "     SCHREGNO ";
        $query .= " FROM ";
        $query .= "     CHAIR_STD_DAT ";
        $query .= " WHERE ";
        $query .= "         YEAR        = '".$model->Year."' ";
        $query .= "     AND SEMESTER    = '".$model->semester ."' ";
        if (!isset($rowcheck["CHAIRCD"])) {
            $query .= " AND CHAIRCD     = '".$model->field["CHAIR"]."' ";
        } else {
            $query .= " AND CHAIRCD     = '".$rowcheck["CHAIRCD"]."' ";
        }
        $query .= "     AND SCHREGNO    = '".$schregno."' ";

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
        $query .= "  ) ";
        $query .= " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     CHAIR_DATA T1, ";
        $query .= "     SUBCLASS_SCHOOLING_COMBINED_DAT T2 ";
        $query .= " WHERE ";
        $query .= "         T1.CLASSCD       = T2.COMBINED_CLASSCD ";
        $query .= "     AND T1.SCHOOL_KIND   = T2.COMBINED_SCHOOL_KIND ";
        $query .= "     AND T1.CURRICULUM_CD = T2.COMBINED_CURRICULUM_CD ";
        $query .= "     AND T1.SUBCLASSCD    = T2.COMBINED_SUBCLASSCD ";
        $query .= "     AND T2.YEAR          = '".$model->Year."' ";

        return $query;
    }

    ///開講しない科目、講座、学籍番号情報取得
    function getAttendChairData($model, $flg, $schregno) {
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
        $query .= "     SELECT ";
        $query .= "         T1.* ";
        $query .= "     FROM ";
        $query .= "         CHAIR_DAT T1 ";
        $query .= "         INNER JOIN COMBINED_DATA L1 ON L1.ATTEND_CLASSCD       = T1.CLASSCD ";
        $query .= "                                    AND L1.ATTEND_SCHOOL_KIND   = T1.SCHOOL_KIND  ";
        $query .= "                                    AND L1.ATTEND_CURRICULUM_CD = T1.CURRICULUM_CD  ";
        $query .= "                                    AND L1.ATTEND_SUBCLASSCD    = T1.SUBCLASSCD ";
        $query .= "     WHERE ";
        $query .= "         T1.YEAR = '".$model->Year."' ";
        $query .= "     AND T1.SEMESTER = '".$model->semester ."' ";
        if ($flg) {
            $query .= "  ) ";
            $query .= " SELECT DISTINCT ";
            $query .= "     T1.SCHREGNO, ";
            $query .= "     T1.CHAIRCD ";
            $query .= " FROM ";
            $query .= "     CHAIR_STD_DAT T1 ";
            $query .= "     INNER JOIN CHECK_DAT L1 ON L1.YEAR     = T1.YEAR ";
            $query .= "                            AND L1.SEMESTER = T1.SEMESTER ";
            $query .= "                            AND L1.CHAIRCD  = T1.CHAIRCD ";
            $query .= " WHERE ";
            $query .= "     T1.SCHREGNO = '".$schregno."' ";
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

    //登録済みチェック
    function getCheckDatePeriod($model, $flg, $rowcheck = '', $schregno) {
        $query  = " SELECT ";
        $query .= "     SCHREGNO ";
        $query .= " FROM ";
        $query .= "     SCH_ATTEND_DAT ";
        $query .= " WHERE ";
        $query .= "         YEAR        = '" .$model->Year."' ";
        $query .= "     AND SCHREGNO    = '".$schregno."' ";
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
        $query .= "     AND SCHOOLINGKINDCD <> '2' ";
        return $query;
    }

    //登録済みチェック（登校スクーリングのみ）
    function getCheckIns($model, $rowcheck, $schregno) {
        $query  = " SELECT ";
        $query .= "     SCHREGNO ";
        $query .= " FROM ";
        $query .= "     SCH_ATTEND_DAT ";
        $query .= " WHERE ";
        $query .= "         YEAR        = '" .$model->Year."' ";
        $query .= "     AND SCHREGNO    = '".$schregno."' ";
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
    function &getDeleteSchAttendDat($model, $rowcheck, $schregNo, $periodCd) {
        $query  = " DELETE FROM ";
        $query .= "     SCH_ATTEND_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".$model->Year."' ";
        $query .= " AND SCHREGNO    = '".$schregNo."' ";
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
    function &getInsertQuery($model, $rowcheck, $schregno, $periodCd, $remark) {
        $data = array();
        $data["YEAR"][TEXT]             = $model->Year;
        $data["SCHREGNO"][TEXT]         = $schregno;
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
        if ($remark == '') {
            $data["REMARK"][TEXT]       = '無';
        } else {
            $data["REMARK"][TEXT]       = $remark;
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
        $query .= "     EXECUTEDATE = '".str_replace("/","-",$model->Date)."' ";
        $query .= " AND PERIODCD    = '".$periodCd."' ";
        if (!isset($rowcheck["CHAIRCD"])) {
            $query .= " AND CHAIRCD     = '".$model->field["CHAIR"]."' ";
        } else {
            $query .= " AND CHAIRCD     = '".$rowcheck["CHAIRCD"]."' ";
        }

        return $query;
    }

    //DELETE
    function &getDeleteQuerysitei($model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        for ($i = 0; $i < get_count($model->deldata); $i++) {
            list($schregno, $receipt_time, $remark, $at_chaircd) = preg_split("/,/", $model->deldata[$i]);

            $query  = " DELETE FROM ";
            $query .= "     SCH_ATTEND_DAT ";
            $query .= " WHERE  ";
            $query .= "     YEAR        = '".$model->Year."' ";
            $query .= " AND SCHREGNO    = '".$schregno."' ";
            $query .= " AND EXECUTEDATE = '".str_replace("/","-",$model->Date)."' ";
            $query .= " AND CHAIRCD     = '".$at_chaircd."' ";
            $query .= " AND SCHOOLINGKINDCD = '".$model->field["COURSE"]."' ";

            $db->query($query);
        }

        $db->commit(); // トランザクションをコミットする。
        Query::dbCheckIn($db);
        return ;
    }
}
?>
