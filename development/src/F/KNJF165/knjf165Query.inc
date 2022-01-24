<?php

require_once('for_php7.php');

class knjf165Query extends Query {

    //学年取得
    function getSelectGrade($model) {
        $query  = " SELECT ";
        $query .= "    T1.GRADE_NAME1 AS LABEL, ";
        $query .= "    T1.GRADE AS VALUE ";
        $query .= " FROM ";
        $query .= "    SCHREG_REGD_GDAT T1 ";
        $query .= " WHERE ";
        $query .= "         T1.YEAR = '".CTRL_YEAR."' ";
        if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND T1.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    function getHrClassAuth($grade) {
        //参照・更新可
        if (AUTHORITY == DEF_REFERABLE || AUTHORITY == DEF_UPDATABLE){
            $query  = "SELECT GRADE || HR_CLASS AS VALUE,HR_NAME AS LABEL ";
            $query .= "FROM SCHREG_REGD_HDAT ";
            $query .= "WHERE YEAR='" .CTRL_YEAR ."'";
            $query .= "  AND SEMESTER='".CTRL_SEMESTER ."'";
            $query .= "  AND GRADE='{$grade}'";
        }
        //参照・更新可（制限付き）//Modify 副担任コードの条件を追加 naka 05/01/05
        if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT){
            $query  = "SELECT GRADE || HR_CLASS AS VALUE,HR_NAME AS LABEL ";
            $query .= "FROM SCHREG_REGD_HDAT ";
            $query .= "WHERE YEAR='" .CTRL_YEAR ."' ";
            $query .= "  AND SEMESTER='".CTRL_SEMESTER ."' ";
            $query .= "  AND GRADE='{$grade}'";
            $query .= " AND (TR_CD1 = '" .STAFFCD ."' ";
            $query .= "   OR TR_CD2 = '" .STAFFCD ."' ";
            $query .= "   OR TR_CD3 = '" .STAFFCD ."' ";
            $query .= "   OR SUBTR_CD1 = '" .STAFFCD ."' ";
            $query .= "   OR SUBTR_CD2 = '" .STAFFCD ."' ";
            $query .= "   OR SUBTR_CD3 = '" .STAFFCD ."') ";
        }
        return $query;
    }

    //クラスの時、BASE_REMARK1が1のカウント取得
    function getBaseCnt($grade_hr_class) {
        $query  = " SELECT ";
        $query .= "     COUNT(*) AS SCHREG_CNT, ";
        $query .= "     COUNT(L1.BASE_REMARK1) AS BASE_CNT ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= "     INNER JOIN SCHREG_BASE_MST T2 ON T2.SCHREGNO = T1.SCHREGNO ";
        $query .= "     INNER JOIN SCHREG_REGD_HDAT T3 ON T3.YEAR = T1.YEAR ";
        $query .= "                                   AND T3.SEMESTER = T1.SEMESTER ";
        $query .= "                                   AND T3.GRADE = T1.GRADE ";
        $query .= "                                   AND T3.HR_CLASS = T1.HR_CLASS ";
        $query .= "     LEFT JOIN SCHREG_BASE_DETAIL_MST L1 ON L1.SCHREGNO = T1.SCHREGNO AND L1.BASE_SEQ = '008' ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND T1.SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= "     AND T1.GRADE || T1.HR_CLASS = '".$grade_hr_class."' ";

        return $query;
    }

    //生徒データ
    function getSchno($model) {
        $query  = " SELECT ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.HR_CLASS, ";
        $query .= "     T1.ATTENDNO, ";
        $query .= "     T2.NAME_SHOW, ";
        $query .= "     T3.HR_NAME, ";
        $query .= "     L1.BASE_REMARK1 ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= "     INNER JOIN SCHREG_BASE_MST T2 ON T2.SCHREGNO = T1.SCHREGNO ";
        $query .= "     INNER JOIN SCHREG_REGD_HDAT T3 ON T3.YEAR = T1.YEAR ";
        $query .= "             AND T3.SEMESTER = T1.SEMESTER ";
        $query .= "             AND T3.GRADE = T1.GRADE ";
        $query .= "             AND T3.HR_CLASS = T1.HR_CLASS ";
        $query .= "     LEFT JOIN SCHREG_BASE_DETAIL_MST L1 ON L1.SCHREGNO = T1.SCHREGNO AND L1.BASE_SEQ = '008' ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND T1.SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= "     AND T1.GRADE || T1.HR_CLASS = '".$model->field["GRADE_HR_CLASS"]."' ";
        $query .= " ORDER BY ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.HR_CLASS, ";
        $query .= "     T1.ATTENDNO ";

        return $query;
    }

    //ANOTHER_SPORTが1のカウント取得
    function getAnotherCnt($schregno) {
        $query  = " SELECT ";
        $query .= "     COUNT(T1.ANOTHER_SPORT) AS ANOTHER_CNT ";
        $query .= " FROM ";
        $query .= "     ANOTHER_SCHOOL_HIST_DAT T1 ";
        $query .= " WHERE ";
        $query .= "     T1.SCHREGNO = '".$schregno."' ";

        return $query;
    }

    //名称マスタ
    function getNameMst($namecd1) {
        $query  = "SELECT NAMECD2 AS VALUE, NAMECD2 || ' ' || NAME1 AS LABEL ";
        $query .= "FROM   V_NAME_MST ";
        $query .= "WHERE  YEAR = '".CTRL_YEAR."' AND ";
        $query .= "       NAMECD1 = '".$namecd1."' ";
        $query .= "ORDER BY NAMECD2 ";

        return $query;
    }

    function getBaseRemark1($schregno) {
        $query  = " SELECT BASE_REMARK1 ";
        $query .= "   FROM SCHREG_BASE_DETAIL_MST ";
        $query .= "  WHERE SCHREGNO = '".$schregno."' ";
        $query .= "    AND BASE_SEQ = '008' ";

        return $query;
    }

    //更新
    function &getUpdateQuery($model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //右)一旦NULLで更新
        if ($model->selectright) {
            $array = explode(",", $model->selectright);
            foreach ($array as $val) {
                //1:クラス指定
                if ($model->field["KUBUN"] == 1) {
                    $hrClass = $val;

                    //BASE_REMARK1が1のカウント取得
                    $query = knjf165Query::getBaseCnt($hrClass);
                    $cntRow = $db->getRow($query, DB_FETCHMODE_ASSOC);
                    $mark = "　";
                    if ($cntRow["BASE_CNT"] == 0) {
                        $mark = "×"; //全員NULL
                    } else if ($cntRow["BASE_CNT"] == $cntRow["SCHREG_CNT"]) {
                        $mark = "○"; //全員１
                    } else if ($cntRow["BASE_CNT"] != $cntRow["SCHREG_CNT"]) {
                        $mark = "△"; //一部１
                    }
                    //○のクラスのみ、NULLで更新
                    if ($mark == "○") {
                        $query = knjf165Query::getClearSql($model, $hrClass);
                        $db->query($query);
                    }
                }
                //2:個人指定
                if ($model->field["KUBUN"] == 2) {
                    $cd = explode("-", $val);
                    $schregno = $cd[0];

                    //BASE_REMARK1を取得
                    $query = knjf165Query::getBaseRemark1($schregno);
                    $baseRow = $db->getRow($query, DB_FETCHMODE_ASSOC);
                    //BASE_REMARK1='1'の生徒のみ、NULLで更新
                    if ($baseRow["BASE_REMARK1"] == "1") {
                        $query = knjf165Query::getClearSql($model, $schregno);
                        $db->query($query);
                    }
                }
            }
        }

        //左)１で更新
        if ($model->selectleft) {
            $array = explode(",", $model->selectleft);
            foreach ($array as $val) {
                //1:クラス指定
                if ($model->field["KUBUN"] == 1) {
                    $hrClass = $val;

                    $query  = " SELECT ";
                    $query .= "     T1.SCHREGNO ";
                    $query .= " FROM ";
                    $query .= "     SCHREG_REGD_DAT T1 ";
                    $query .= "     INNER JOIN SCHREG_BASE_MST T2 ON T2.SCHREGNO = T1.SCHREGNO ";
                    $query .= " WHERE ";
                    $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
                    $query .= "     AND T1.SEMESTER = '".CTRL_SEMESTER."' ";
                    $query .= "     AND T1.GRADE || T1.HR_CLASS = '".$hrClass."' ";
                    $result = $db->query($query);
                    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                        $schregno = $row["SCHREGNO"];

                        $query = knjf165Query::getUpdateSql($db, $schregno);
                        $db->query($query);
                    }
                }
                //2:個人指定
                if ($model->field["KUBUN"] == 2) {
                    $cd = explode("-", $val);
                    $schregno = $cd[0];

                    $query = knjf165Query::getUpdateSql($db, $schregno);
                    $db->query($query);
                }
            }
        }

        $db->commit();
        Query::dbCheckIn($db);
        return;
    }

    //一旦NULLで更新
    function getClearSql($model, $cd) {
        $query  = " UPDATE ";
        $query .= "     SCHREG_BASE_DETAIL_MST ";
        $query .= " SET ";
        $query .= "     BASE_REMARK1 = NULL ";
        $query .= " WHERE ";
        $query .= "     BASE_SEQ = '008' ";
        $query .= "     AND SCHREGNO IN ( ";
        $query .= "         SELECT ";
        $query .= "             T1.SCHREGNO ";
        $query .= "         FROM ";
        $query .= "             SCHREG_REGD_DAT T1 ";
        $query .= "             INNER JOIN SCHREG_BASE_MST T2 ON T2.SCHREGNO = T1.SCHREGNO ";
        $query .= "         WHERE ";
        $query .= "             T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "             AND T1.SEMESTER = '".CTRL_SEMESTER."' ";
        //1:クラス指定
        if ($model->field["KUBUN"] == 1) {
            $query .= "             AND T1.GRADE || T1.HR_CLASS = '".$cd."' ";
        }
        //2:個人指定
        if ($model->field["KUBUN"] == 2) {
            $query .= "             AND T1.SCHREGNO = '".$cd."' ";
        }
        $query .= "     ) ";
        //ANOTHER_SPORTが1の人はクリアしない
        $query .= "     AND SCHREGNO NOT IN ( ";
        $query .= "         SELECT ";
        $query .= "             T1.SCHREGNO ";
        $query .= "         FROM ";
        $query .= "             ANOTHER_SCHOOL_HIST_DAT T1 ";
        $query .= "         GROUP BY ";
        $query .= "             T1.SCHREGNO ";
        $query .= "         HAVING ";
        $query .= "             0 < COUNT(T1.ANOTHER_SPORT) ";
        $query .= "     ) ";

        return $query;
    }

    //--- INSERT or UPDATE
    function getUpdateSql($db, $schregno) {
        $base_seq = "008";

        $data = array();
        $data["SCHREGNO"][TEXT]      = $schregno;
        $data["BASE_SEQ"][TEXT]      = $base_seq;
        $data["BASE_REMARK1"][TEXT]  = "1";
        $data["REGISTERCD"][TEXT]    = STAFFCD;
        $data["UPDATED"][FUNC]       = "sysdate()";

        $where  = " WHERE SCHREGNO = '{$schregno}' ";
        $where .= "   AND BASE_SEQ = '{$base_seq}' ";

        $query  = " SELECT COUNT(*) FROM SCHREG_BASE_DETAIL_MST ".$where;
        if (0 < $db->getOne($query)) {
            $query = Query::updateSQL($data, "SCHREG_BASE_DETAIL_MST", $where);
        } else {
            $query = Query::insertSQL($data, "SCHREG_BASE_DETAIL_MST");
        }

        return $query;
    }

}
?>
