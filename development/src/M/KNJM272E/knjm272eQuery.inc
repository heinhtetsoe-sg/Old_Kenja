<?php

require_once('for_php7.php');

class knjm272eQuery extends Query {

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

    //生徒データ
    function getStudent($model) {
        $query  = " SELECT ";
        $query .= "     B1.SCHREGNO, ";
        $query .= "     B1.NAME ";
        $query .= " FROM ";
        $query .= "     SCHREG_BASE_MST B1 ";
        $query .= " WHERE ";
        $query .= "     B1.SCHREGNO = '{$model->schregno}' ";

//        echo $query;
        return $query;
    }

    //科目
    function getSubClass($model) {
        $query  = " SELECT ";
        $query .= "     CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD AS SUBCD, ";
        $query .= "     SUBCLASSNAME AS SUBNAME ";
        $query .= " FROM ";
        $query .= "     V_SUBCLASS_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$model->Year}' ";
        $query .= "     AND CLASSCD < '91' ";
        if (AUTHORITY != DEF_UPDATABLE) {
            $query .= "     AND CLASSCD || SCHOOL_KIND || CURRICULUM_CD || SUBCLASSCD IN ( ";
            $query .= "         SELECT ";
            $query .= "             CLASSCD || SCHOOL_KIND || CURRICULUM_CD || SUBCLASSCD ";
            $query .= "         FROM ";
            $query .= "             REP_STF_DAT ";
            $query .= "         WHERE ";
            $query .= "             YEAR = '{$model->Year}' ";
            $query .= "             AND STAFFCD = '".STAFFCD."' ";
            $query .= "     ) ";
        }
        //該当生徒の履修科目
        $query .= "     AND CLASSCD || SCHOOL_KIND || CURRICULUM_CD || SUBCLASSCD IN ( ";
        $query .= "         SELECT ";
        $query .= "             CLASSCD || SCHOOL_KIND || CURRICULUM_CD || SUBCLASSCD ";
        $query .= "         FROM ";
        $query .= "             CHAIR_DAT ";
        $query .= "         WHERE ";
        $query .= "             YEAR = '{$model->Year}' ";
        $query .= "             AND SEMESTER = '{$model->semester}' ";
        $query .= "             AND CHAIRCD IN ( ";
        $query .= "                 SELECT ";
        $query .= "                     CHAIRCD ";
        $query .= "                 FROM ";
        $query .= "                     CHAIR_STD_DAT ";
        $query .= "                 WHERE ";
        $query .= "                     YEAR = '{$model->Year}' ";
        $query .= "                     AND SEMESTER = '{$model->semester}' ";
        $query .= "                     AND SCHREGNO = '{$model->schregno}' ";
        $query .= "                 GROUP BY ";
        $query .= "                     CHAIRCD ";
        $query .= "             ) ";
        $query .= "         GROUP BY ";
        $query .= "             CLASSCD || SCHOOL_KIND || CURRICULUM_CD || SUBCLASSCD ";
        $query .= "     ) ";
        $query .= " ORDER BY ";
        $query .= "     SUBCD ";

//        echo $query;
        return $query;
    }

    //回数
    function getStandardSeq($model, $subcd) {
        $query  = " SELECT ";
        $query .= "     REP_SEQ_ALL, ";
        $query .= "     VALUE(REP_START_SEQ, 1) AS REP_START_SEQ ";
        $query .= " FROM ";
        $query .= "     CHAIR_CORRES_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$model->Year}' ";
        $query .= "     AND CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD = '{$subcd}' ";

//        echo $query;
        return $query;
    }

    //生徒データ(レポート)
    function getSch($model, $subcd, $standard_seq) {
        $query  = " SELECT ";
        $query .= "     REPRESENT_SEQ, ";
        $query .= "     RECEIPT_DATE, ";
        $query .= "     GRAD_VALUE ";
        $query .= " FROM ";
        $query .= "     REP_PRESENT_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$model->Year}' ";
        $query .= "     AND CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD = '{$subcd}' ";
        $query .= "     AND STANDARD_SEQ = {$standard_seq} "; //回数　DBエラー注意
        $query .= "     AND REPRESENT_SEQ <= 9 "; //再提出は9回まで格納可能
        $query .= "     AND SCHREGNO = '{$model->schregno}' ";
        $query .= " ORDER BY ";
        $query .= "     REPRESENT_SEQ, ";
        $query .= "     RECEIPT_DATE ";

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

    //UPDATE
    function &getUpdateQuery($model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //入力値変換(ABBV2 → NAMECD2)
        $hyoukaArray = array();
        $result = $db->query(knjm272eQuery::getHyouka());
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $hyoukaArray[$row["ABBV2"]] = $row["NAMECD2"];
        }
        $result->free();

        for ($i = 0; $i < get_count($model->upddata); $i++) {
            list($subclasscd, $standard_seq, $represent_seq, $receipt_date, $grad_value) = preg_split("/,/", $model->upddata[$i]);

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
            $query .= "     AND CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD = '{$subclasscd}' ";
            $query .= "     AND STANDARD_SEQ = {$standard_seq} ";
            $query .= "     AND REPRESENT_SEQ = {$represent_seq} ";
            $query .= "     AND SCHREGNO = '{$model->schregno}' ";
            $query .= "     AND RECEIPT_DATE = '{$receipt_date}' ";

//echo $query."<BR>";
            $db->query($query);
        }

        $db->commit(); // トランザクションをコミットする。
        Query::dbCheckIn($db);

        return ;
    }

}
?>