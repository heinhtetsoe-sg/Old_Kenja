<?php

require_once('for_php7.php');

class knjm271eQuery extends Query {

    //担当者取得
    function selectStaff(&$model) {
        //参照・更新可
        if (AUTHORITY == DEF_UPDATABLE){
            $model->User = 0;
            $query  = "SELECT DISTINCT w1.STAFFCD,w2.STAFFNAME ";
            $query .= "FROM REP_STF_DAT w1 left join V_STAFF_MST w2 on w1.STAFFCD = w2.STAFFCD and w1.YEAR = w2.YEAR ";
            $query .= "WHERE w1.YEAR ='" .$model->Year ."'";
            $query .= " ORDER BY w1.STAFFCD ";
        }else {
            $model->User = 1;
            $query  = " with atable (CHAIRCD) as ( ";
            $query .= " SELECT ";
            $query .= "     CHAIRCD ";
            $query .= " FROM ";
            $query .= "     REP_STF_DAT ";
            $query .= " WHERE ";
            $query .= "     YEAR = '" .$model->Year."'      AND ";
            $query .= "     STAFFCD ='" .STAFFCD ."' ";
            $query .= " GROUP BY ";
            $query .= "     CHAIRCD), ";
            $query .= " btable (STAFFCD,YEAR) as ( ";
            $query .= " SELECT ";
            $query .= "     w1.STAFFCD,w1.YEAR ";
            $query .= " FROM ";
            $query .= "     REP_STF_DAT w1,atable w2 ";
            $query .= " WHERE ";
            $query .= "     w1.YEAR = '" .$model->Year."'      AND ";
            $query .= "     w1.CHAIRCD = w2.CHAIRCD ";
            $query .= " GROUP BY ";
            $query .= "     w1.STAFFCD,w1.YEAR) ";
            $query .= " SELECT ";
            $query .= "     w1.STAFFCD, ";
            $query .= "     w2.STAFFNAME ";
            $query .= " FROM ";
            $query .= "     btable w1 left join V_STAFF_MST w2 on w1.STAFFCD = w2.STAFFCD and ";
            $query .= "     w1.YEAR = w2.YEAR ";
            $query .= " ORDER BY ";
            $query .= "     w1.STAFFCD ";
        }

        return $query;
    }

    //科目
    function getSubClass($model) {
        $query  = " SELECT ";
        $query .= "     CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD AS VALUE, ";
        $query .= "     SUBCLASSNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_SUBCLASS_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$model->Year}' ";
        $query .= "     AND CLASSCD < '91' ";
        if ($model->useCombinedDat == "1") {
            $query .= "     AND NOT EXISTS( ";
            $query .= "         SELECT ";
            $query .= "             'X' ";
            $query .= "         FROM ";
            $query .= "             REP_SUBCLASS_COMBINED_DAT ";
            $query .= "         WHERE ";
            $query .= "             YEAR = '{$model->Year}' ";
            $query .= "             AND ATTEND_CLASSCD || ATTEND_SCHOOL_KIND || ATTEND_CURRICULUM_CD || ATTEND_SUBCLASSCD = CLASSCD || SCHOOL_KIND || CURRICULUM_CD || SUBCLASSCD ";
            $query .= "     ) ";
        }
        $query .= "     AND CLASSCD || SCHOOL_KIND || CURRICULUM_CD || SUBCLASSCD IN ( ";
        $query .= "         SELECT ";
        $query .= "             CLASSCD || SCHOOL_KIND || CURRICULUM_CD || SUBCLASSCD ";
        $query .= "         FROM ";
        $query .= "             REP_STF_DAT ";
        $query .= "         WHERE ";
        $query .= "             YEAR = '{$model->Year}' ";
        $query .= "             AND STAFFCD = '{$model->field["STAFF"]}' ";
        $query .= "     ) ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

//        echo $query;
        return $query;
    }

    //回数
    function getStandardSeq($model) {
        $query  = " SELECT ";
        $query .= "     REP_SEQ_ALL, ";
        $query .= "     VALUE(REP_START_SEQ, 1) AS REP_START_SEQ ";
        $query .= " FROM ";
        $query .= "     CHAIR_CORRES_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '" .$model->Year."' ";
        if ($model->Properties["useTsushin_Repout_KouzaKonboHyoji"] == "1") {
            $query .= "     AND CHAIRCD = '{$model->field["CHAIRCD"]}' ";
        }else{
            $query .= "     AND CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD = '{$model->field["SUBCLASSCD"]}' ";
        }

        return $query;
    }

    //生徒データ
    function getSch($model) {
        $query  = " WITH T_CHAIR AS ( ";
        $query .= "     SELECT ";
        $query .= "         SCHREGNO ";
        $query .= "     FROM ";
        $query .= "         CHAIR_STD_DAT ";
        $query .= "     WHERE ";
        $query .= "         YEAR = '{$model->Year}' ";
        if ($model->Properties["useTsushin_Repout_KouzaKonboHyoji"] == "1") {
            $query .= "         AND CHAIRCD = '{$model->field["CHAIRCD"]}' ";
        }else{
            $query .= "         AND CHAIRCD IN ( ";
            $query .= "             SELECT ";
            $query .= "                 CHAIRCD ";
            $query .= "             FROM ";
            $query .= "                 CHAIR_DAT ";
            $query .= "             WHERE ";
            $query .= "                 YEAR = '{$model->Year}' ";
            $query .= "                 AND SEMESTER = '{$model->semester}' ";
            if ($model->useCombinedDat == "1") {
                $query .= "             AND (CLASSCD || SCHOOL_KIND || CURRICULUM_CD || SUBCLASSCD IN ( ";
                $query .= "                 SELECT ";
                $query .= "                     ATTEND_CLASSCD || ATTEND_SCHOOL_KIND || ATTEND_CURRICULUM_CD || ATTEND_SUBCLASSCD ";
                $query .= "                 FROM ";
                $query .= "                     REP_SUBCLASS_COMBINED_DAT ";
                $query .= "                 WHERE ";
                $query .= "                     YEAR = '{$model->Year}' ";
                $query .= "                     AND COMBINED_CLASSCD || '-' || COMBINED_SCHOOL_KIND || '-' || COMBINED_CURRICULUM_CD || '-' || COMBINED_SUBCLASSCD = '{$model->field["SUBCLASSCD"]}' ";
                $query .= "                 ) ";
                $query .= "             OR CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD = '{$model->field["SUBCLASSCD"]}') ";
            } else {
                $query .= "             AND CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD = '{$model->field["SUBCLASSCD"]}' ";
            }
            $query .= "         ) ";
            $query .= "         AND CHAIRCD IN ( ";
            $query .= "             SELECT ";
            $query .= "                 CHAIRCD ";
            $query .= "             FROM ";
            $query .= "                 CHAIR_CORRES_DAT ";
            $query .= "             WHERE ";
            $query .= "                 YEAR = '{$model->Year}' ";
            if ($model->useCombinedDat == "1") {
                $query .= "             AND (CLASSCD || SCHOOL_KIND || CURRICULUM_CD || SUBCLASSCD IN ( ";
                $query .= "                 SELECT ";
                $query .= "                     ATTEND_CLASSCD || ATTEND_SCHOOL_KIND || ATTEND_CURRICULUM_CD || ATTEND_SUBCLASSCD ";
                $query .= "                 FROM ";
                $query .= "                     REP_SUBCLASS_COMBINED_DAT ";
                $query .= "                 WHERE ";
                $query .= "                     YEAR = '{$model->Year}' ";
                $query .= "                     AND COMBINED_CLASSCD || '-' || COMBINED_SCHOOL_KIND || '-' || COMBINED_CURRICULUM_CD || '-' || COMBINED_SUBCLASSCD = '{$model->field["SUBCLASSCD"]}' ";
                $query .= "                 ) ";
                $query .= "                 OR CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD = '{$model->field["SUBCLASSCD"]}') ";
            } else {
                $query .= "                 AND CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD = '{$model->field["SUBCLASSCD"]}' ";
            }
            $query .= "         ) ";
        }
        $query .= "     GROUP BY ";
        $query .= "         SCHREGNO ";
        $query .= " ) ";
        $query .= " , T_SCHREG AS ( ";
        $query .= "     SELECT ";
        $query .= "         D1.SCHREGNO, ";
        $query .= "         H1.HR_NAME, ";
        $query .= "         B1.NAME ";
        $query .= "     FROM ";
        $query .= "         SCHREG_REGD_DAT D1 ";
        $query .= "         INNER JOIN SCHREG_REGD_HDAT H1 ON H1.YEAR = D1.YEAR AND H1.SEMESTER = D1.SEMESTER AND H1.GRADE = D1.GRADE AND H1.HR_CLASS = D1.HR_CLASS ";
        $query .= "         INNER JOIN SCHREG_BASE_MST B1 ON B1.SCHREGNO = D1.SCHREGNO ";
        $query .= "     WHERE ";
        $query .= "         D1.YEAR = '{$model->Year}' ";
        $query .= "         AND D1.SEMESTER = '{$model->semester}' ";
        $query .= "         AND D1.SCHREGNO IN (SELECT SCHREGNO FROM T_CHAIR) ";
        $query .= " ) ";
        $query .= " , T_PRESENT AS ( ";
        $query .= "     SELECT ";
        $query .= "         SCHREGNO, ";
        $query .= "         REPRESENT_SEQ, ";
        $query .= "         RECEIPT_DATE, ";
        $query .= "         STAFFCD, ";
        $query .= "         GRAD_VALUE, ";
        $query .= "         GRAD_INPUT_DATE, ";
        $query .= "         GRAD_TIME ";
        $query .= "     FROM ";
        $query .= "         REP_PRESENT_DAT ";
        $query .= "     WHERE ";
        $query .= "         YEAR = '{$model->Year}' ";
        if ($model->Properties["useTsushin_Repout_KouzaKonboHyoji"] == "1") {
            $query .= "         AND CHAIRCD = '{$model->field["CHAIRCD"]}' ";
        }else{
            $query .= "         AND CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD = '{$model->field["SUBCLASSCD"]}' ";
        }
        $query .= "         AND STANDARD_SEQ = {$model->field["STANDARD_SEQ"]} "; //回数　DBエラー注意
        $query .= "         AND REPRESENT_SEQ <= 9 "; //再提出は9回まで格納可能
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     B1.SCHREGNO, ";
        $query .= "     B1.HR_NAME, ";
        $query .= "     B1.NAME, ";
        $query .= "     P1.REPRESENT_SEQ, ";
        $query .= "     P1.RECEIPT_DATE, ";
        $query .= "     P1.STAFFCD, ";
        $query .= "     P1.GRAD_VALUE, ";
        $query .= "     P1.GRAD_INPUT_DATE, ";
        $query .= "     P1.GRAD_TIME ";
        $query .= " FROM ";
        $query .= "     T_SCHREG B1 ";
        $query .= "     LEFT JOIN T_PRESENT P1 ON P1.SCHREGNO = B1.SCHREGNO ";
        $query .= " ORDER BY ";
        $query .= "     B1.SCHREGNO, ";
        $query .= "     P1.REPRESENT_SEQ, ";
        $query .= "     P1.RECEIPT_DATE ";
//        echo $query;
        return $query;
    }

    //読替テーブル有無
    function useCombinedDat() {
        $query  = " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     SYSIBM.TABLES ";
        $query .= " WHERE ";
        $query .= "     TABLE_NAME = 'REP_SUBCLASS_COMBINED_DAT' ";

        return $query;
    }

    //評価値チェック
    function getHyouka() {
        $query  = " SELECT ";
        $query .= "     NAMECD2, ";
        $query .= "     ABBV2, ";
        $query .= "     NAME1 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND NAMECD1 = 'M003' ";
        $query .= " ORDER BY ";
        $query .= "     NAMECD2 ";

        return $query;
    }

    //添削者登録確認
    function getCheckstf($model) {
        $query  = " SELECT ";
        $query .= "     STAFFCD ";
        $query .= " FROM ";
        $query .= "     REP_STF_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '" .$model->Year."' ";
        $query .= "     AND STAFFCD = '{$model->field["STAFF"]}' ";
        if ($model->Properties["useTsushin_Repout_KouzaKonboHyoji"] == "1") {
            $query .= "     AND CHAIRCD = '{$model->field["CHAIRCD"]}' ";
        }else{
            $query .= "     AND CHAIRCD IN ( ";
            $query .= "         SELECT ";
            $query .= "             CHAIRCD ";
            $query .= "         FROM ";
            $query .= "             CHAIR_DAT ";
            $query .= "         WHERE ";
            $query .= "             YEAR = '{$model->Year}' ";
            $query .= "             AND SEMESTER = '{$model->semester}' ";
            $query .= "             AND CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD = '{$model->field["SUBCLASSCD"]}' ";
            $query .= "     ) ";
        }
        return $query;
    }

    //UPDATE
    function &getUpdateQuery($model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //入力値変換(ABBV2 → NAMECD2)
        $hyoukaArray = array();
        $result = $db->query(knjm271eQuery::getHyouka());
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $hyoukaArray[$row["ABBV2"]] = $row["NAMECD2"];
        }
        $result->free();

        for ($i = 0; $i < get_count($model->upddata); $i++) {
            list($schregno, $represent_seq, $receipt_date, $grad_value) = preg_split("/,/", $model->upddata[$i]);

            //入力値変換(ABBV2 → NAMECD2)
            $setGradValue = strlen($grad_value) ? $hyoukaArray[$grad_value] : "";

            $query  = " UPDATE ";
            $query .= "     REP_PRESENT_DAT ";
            $query .= " SET ";
            $query .= "     STAFFCD = '{$model->field["STAFF"]}', ";
            $query .= "     GRAD_VALUE = '{$setGradValue}', ";
            if (strlen($grad_value)) {
                if ($model->Properties["KNJM271E_grad_date_upd"] == "1") {
                    $query .= "     GRAD_DATE = '".str_replace("/","-",$model->Date)."', ";
                }
                $query .= "     GRAD_INPUT_DATE = '".str_replace("/","-",$model->Date)."', ";
                $query .= "     GRAD_TIME = current time, ";
            } else {
                $query .= "     GRAD_DATE = NULL, ";
                $query .= "     GRAD_INPUT_DATE = NULL, ";
                $query .= "     GRAD_TIME = NULL, ";
            }
            $query .= "     REGISTERCD = '".STAFFCD."', ";
            $query .= "     UPDATED = sysdate() ";
            $query .= " WHERE ";
            $query .= "     YEAR = '{$model->Year}' ";
            if ($model->Properties["useTsushin_Repout_KouzaKonboHyoji"] == "1") {
                $query .= "     AND CHAIRCD = '{$model->field["CHAIRCD"]}' ";
            }else{
                $query .= "     AND CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD = '{$model->field["SUBCLASSCD"]}' ";
            }
            $query .= "     AND STANDARD_SEQ = {$model->field["STANDARD_SEQ"]} ";
            $query .= "     AND REPRESENT_SEQ = {$represent_seq} ";
            $query .= "     AND SCHREGNO = '{$schregno}' ";
            $query .= "     AND RECEIPT_DATE = '{$receipt_date}' ";

//echo $query."<BR>";
            $db->query($query);
        }

        $db->commit(); // トランザクションをコミットする。
        Query::dbCheckIn($db);

        return ;
    }

    //講座データの取得
    function getChrSubCd($model) {
        $query  = "WITH SUBCLASS AS ( ";
        $query .= "     SELECT ";
        $query .= "         CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD AS VALUE, ";
        $query .= "         SUBCLASSNAME AS LABEL ";        
        $query .= "     FROM ";
        $query .= "         V_SUBCLASS_MST ";
        $query .= "     WHERE ";
        $query .= "         YEAR = '{$model->Year}' ";
        $query .= "         AND CLASSCD < '91' ";
        if ($model->useCombinedDat == "1") {
            $query .= "         AND NOT EXISTS( ";
            $query .= "             SELECT ";
            $query .= "                 'X' ";
            $query .= "             FROM ";
            $query .= "                 REP_SUBCLASS_COMBINED_DAT ";
            $query .= "             WHERE ";
            $query .= "                 YEAR = '{$model->Year}' ";
            $query .= "                 AND ATTEND_CLASSCD || ATTEND_SCHOOL_KIND || ATTEND_CURRICULUM_CD || ATTEND_SUBCLASSCD = CLASSCD || SCHOOL_KIND || CURRICULUM_CD || SUBCLASSCD ";
            $query .= "         ) ";
        }
        $query .= "         AND CLASSCD || SCHOOL_KIND || CURRICULUM_CD || SUBCLASSCD IN ( ";
        $query .= "             SELECT ";
        $query .= "                 CLASSCD || SCHOOL_KIND || CURRICULUM_CD || SUBCLASSCD ";
        $query .= "             FROM ";
        $query .= "                 REP_STF_DAT ";
        $query .= "             WHERE ";
        $query .= "                 YEAR = '{$model->Year}' ";
        $query .= "                 AND STAFFCD = '{$model->field["STAFF"]}' ";
        $query .= "         )";  
        $query .= "     )";
        $query .= " SELECT DISTINCT ";
        $query .= "     T1.CHAIRCD || ' ' || T1.CHAIRNAME AS LABEL, ";
        $query .= "     T1.CHAIRCD AS VALUE ";
        $query .= " FROM ";
        $query .= "     CHAIR_DAT T1";
        $query .= "     INNER JOIN CHAIR_STD_DAT T2 ON T2.YEAR = T1.YEAR AND T2.SEMESTER = T1.SEMESTER AND T2.CHAIRCD  = T1.CHAIRCD";
        $query .= "     INNER JOIN CHAIR_STF_DAT T3 ON T1.YEAR = T3.YEAR AND T1.SEMESTER = T3.SEMESTER AND T1.CHAIRCD  = T3.CHAIRCD";
        $query .= "     INNER JOIN SUBCLASS S1 ON S1.VALUE = T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD";
        if ($model->Properties["printSubclassLastChairStd"] == "1") {
            $query .= "    ,SEMESTER_MST T5 ";
        }
        $query .= " WHERE ";
        $query .= "         T1.YEAR     = '".CTRL_YEAR."' ";
        $query .= "     AND T1.CLASSCD  <= '90' ";
        //更新可能(制限付)
        if(AUTHORITY != DEF_UPDATABLE){
          $query .= "       AND T3.STAFFCD = '".STAFFCD."' ";
        }
        if ($model->Properties["printSubclassLastChairStd"] == "1") {
            $query .= "         AND T5.YEAR  = T1.YEAR ";
            $query .= "         AND T5.SEMESTER = T1.SEMESTER ";
            $query .= "         AND T5.EDATE    = T2.APPENDDATE ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";
        return $query;
    }
}
?>