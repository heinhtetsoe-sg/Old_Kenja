<?php

require_once('for_php7.php');

class knja032Query extends Query {
    //年、組コンボボックス
    function GradeClassQuery()
    {
        $query = " SELECT ";
        $query .= "   T1.GRADE || ',' || T1.HR_CLASS AS GC, ";
        $query .= "   T1.HR_NAME, ";
        $query .= "   T2.STAFFNAME_SHOW ";
        $query .= " FROM ";
        $query .= "   SCHREG_REGD_HDAT T1 LEFT OUTER JOIN  ";
        $query .= "   STAFF_MST T2 ON T1.TR_CD1 = T2.STAFFCD ";
        $query .= " WHERE ";
        $query .= "   T1.YEAR  = '" .CTRL_YEAR ."' AND ";
        $query .= "   T1.SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= " ORDER BY ";
        $query .= "   GC ";

        return $query;
    }
    //学校マスタ
    function selectSchool_Mst()
    {
        $query = " SELECT ";
        $query .= "   * ";
        $query .= " FROM ";
        $query .= "   SCHOOL_MST ";
        $query .= " WHERE ";
        $query .= "   YEAR          = '" .CTRL_YEAR ."'";

        return $query;
    }
    //次年度の学期マスタが存在するか
    function selectSemester_Mst()
    {
        $query = " SELECT ";
        $query .= "   * ";
        $query .= " FROM ";
        $query .= "   SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "   YEAR          = '" .((int)CTRL_YEAR + 1) ."' ";

        return $query;
    }
    //コンボボックスより、生徒を検索
    function ReadQuery($model)
    {
        list($grade, $hr_class) = explode(",",$model->gc_select);

        $query = " SELECT ";
        $query .= "   T1.YEAR, ";
        $query .= "   T1.SEMESTER, ";
        $query .= "   T1.SCHREGNO, ";
        $query .= "   INTEGER(T1.ATTENDNO) AS ATTENDNO, ";
        $query .= "   T2.HR_NAMEABBV, ";
        $query .= "   T3.NAME_SHOW, ";
        $query .= "   T3.NAME_KANA, ";
        $query .= "   T3.GRD_DATE, ";
        $query .= "   T3.GRD_DIV, ";
        $query .= "   T8.REMAINGRADE_FLG, ";
        $query .= "   T3.GRD_NO, ";
        $query .= "   MEISYOU_GET(T6.TRANSFERCD,'A004',1) AS TRANS_STATUS, ";
        $query .= "   COALESCE(T7.ABROAD_CREDITS, 0) AS ABROAD_CREDITS ";
        $query .= " FROM ";
        $query .= "   SCHREG_REGD_DAT T1 ";
        $query .= "   LEFT OUTER JOIN SCHREG_TRANSFER_DAT T6  ";
        $query .= "   ON T1.SCHREGNO = T6.SCHREGNO AND  ";
        $query .= "   DATE('".str_replace("/","-",CTRL_DATE)."') BETWEEN T6.TRANSFER_SDATE AND T6.TRANSFER_EDATE  ";
        $query .= "   LEFT OUTER JOIN ( SELECT ";
        $query .= "   ST1.SCHREGNO, ";
        $query .= "   SUM(COALESCE(ST1.ABROAD_CREDITS, 0)) AS ABROAD_CREDITS ";
        $query .= " FROM ";
        $query .= "   SCHREG_TRANSFER_DAT ST1 ";
        if ($model->school["SCHOOLDIV"] == 0 ){     //学年制
            $query .= " WHERE ";
            $query .= "   FISCALYEAR(ST1.TRANSFER_SDATE) = '" .CTRL_YEAR ."' ";
        }
        $query .= " GROUP BY ";
        $query .= "   ST1.SCHREGNO ";
        $query .= " ) T7 ON T1.SCHREGNO = T7.SCHREGNO ";
        $query .= "   LEFT OUTER JOIN CLASS_FORMATION_DAT T8  ";
        $query .= "   ON T1.SCHREGNO = T8.SCHREGNO AND ";
        $query .= "   INTEGER(T1.YEAR) + 1 = INTEGER(T8.YEAR) AND ";
        $query .= "   T8.SEMESTER = '1',";
        $query .= "   SCHREG_REGD_HDAT T2, ";
        $query .= "   SCHREG_BASE_MST T3 ";
        $query .= " WHERE ";
        $query .= "   T1.SCHREGNO = T3.SCHREGNO AND ";
        $query .= "   T1.YEAR = '" .CTRL_YEAR ."' AND ";
        $query .= "   T1.YEAR = T2.YEAR AND ";
        $query .= "   T1.SEMESTER = '" .CTRL_SEMESTER ."' AND ";
        $query .= "   T1.SEMESTER = T2.SEMESTER AND ";
        $query .= "   T2.GRADE = '" .$grade ."' AND ";
        $query .= "   T1.GRADE = T2.GRADE AND ";
        $query .= "   T2.HR_CLASS = '" .$hr_class ."' AND ";
        $query .= "   T1.HR_CLASS = T2.HR_CLASS AND ";
        $query .= "   ((T3.GRD_DIV IS NOT NULL AND T3.GRD_DIV NOT IN ('2','3')) OR ";
        $query .= "   (T3.GRD_DIV IS NULL OR T3.GRD_DIV = '')) ";
        if (is_array($model->sort)){
            $query .= " ORDER BY ";
            foreach ($model->sort as $field => $order){
                switch($field){
                    case "ATTENDNO":    //番号順
                        $query .= "T2.HR_NAMEABBV,INTEGER(T1.ATTENDNO) ";
                        $query .= ($order > 0)? "ASC " : "DESC ";
                        break 2;
                    case "TARGET":       //席次順
                        if ($order > 0){
                            $query .= "T8.REMAINGRADE_FLG,T3.GRD_DIV DESC,T2.HR_NAMEABBV,INTEGER(T1.ATTENDNO) ";
                        }else{
                            $query .= "T8.REMAINGRADE_FLG,T3.GRD_DIV,T2.HR_NAMEABBV,INTEGER(T1.ATTENDNO) ";
                        }
                        break 2;
                }
            }
        }else{
            $query .= "ORDER BY ";
            $query .= "    T2.HR_NAMEABBV, ";
            $query .= "    INTEGER(T1.ATTENDNO) ASC ";
        }

        return $query;
    }

    //習得単位(学年制)
    function getGetCredits0($schregno, $model)
    {
        $query  = "WITH RECORD_CREDIT AS ( ";
        $query .= "SELECT ";
        $query .= "    SUM(VALUE(GET_CREDIT, 0)) AS RECORD_CREDITS ";
        $query .= "FROM ";
        $query .= "    RECORD_DAT ";
        $query .= "WHERE ";
        $query .= "    YEAR = '".CTRL_YEAR ."' ";
        $query .= "    AND SUBCLASSCD NOT IN (SELECT DISTINCT ";
        $query .= "                               ATTEND_SUBCLASSCD ";
        $query .= "                           FROM ";
        $query .= "                               SUBCLASS_REPLACE_COMBINED_DAT ";
        $query .= "                           WHERE ";
        $query .= "                               REPLACECD = '1' ";
        $query .= "                               AND YEAR = '".CTRL_YEAR ."' ";
        $query .= "                          ) ";
        $query .= "    AND SCHREGNO = '".$schregno."' ";
        $query .= "), TRANSFER_CREDIT AS ( ";
        $query .= "SELECT ";
        $query .= "    SUM(VALUE(ABROAD_CREDITS, 0)) AS TRANSFER_CREDITS ";
        $query .= "FROM ";
        $query .= "    SCHREG_TRANSFER_DAT ";
        $query .= "WHERE ";
        $query .= "    SCHREGNO = '".$schregno."' ";
        $query .= "    AND TRANSFER_SDATE BETWEEN '".str_replace("/", "-", $model->control_data["学期開始日付"][9])."' ";
        $query .= "                           AND '".str_replace("/", "-", $model->control_data["学期終了日付"][9])."' ";
        $query .= "), QUALIFIED_CREDIT AS ( ";
        $query .= "SELECT ";
        $query .= "    SUM(VALUE(CREDITS, 0)) AS QUALIFIED_CREDITS ";
        $query .= "FROM ";
        $query .= "    SCHREG_QUALIFIED_DAT ";
        $query .= "WHERE ";
        $query .= "    YEAR = '".CTRL_YEAR ."' ";
        $query .= "    AND SCHREGNO = '".$schregno."' ";
        $query .= ") ";
        $query .= "SELECT ";
        $query .= "    VALUE(RECORD_CREDITS, 0) + ";
        $query .= "    VALUE(TRANSFER_CREDITS, 0) + ";
        $query .= "    VALUE(QUALIFIED_CREDITS, 0) AS GET_CREDITS ";
        $query .= "FROM ";
        $query .= "    RECORD_CREDIT, ";
        $query .= "    TRANSFER_CREDIT, ";
        $query .= "    QUALIFIED_CREDIT ";

        return $query;
    }

    //習得単位(単位制)
    function getGetCredits1($schregno)
    {
        $query  = "SELECT ";
        $query .= "    SUM(VALUE(GET_CREDIT, 0)) + SUM(VALUE(ADD_CREDIT, 0)) AS GET_CREDITS ";
        $query .= "FROM ";
        $query .= "    SCHREG_STUDYREC_DAT ";
        $query .= "WHERE ";
        $query .= "    SCHREGNO = '".$schregno."' ";

        return $query;
    }

    //保留単位(学年制)
    function getRemCredits0($schregno)
    {
        $query  = "SELECT ";
        $query .= "    SUM(CASE WHEN VALUE(COMP_CREDIT, 0) > VALUE(GET_CREDIT, 0) ";
        $query .= "         THEN VALUE(COMP_CREDIT, 0) - VALUE(GET_CREDIT, 0) ";
        $query .= "         ELSE 0 END ";
        $query .= "       ) AS RECORD_CREDITS ";
        $query .= "FROM ";
        $query .= "    RECORD_DAT ";
        $query .= "WHERE ";
        $query .= "    YEAR = '".CTRL_YEAR ."' ";
        $query .= "    AND SUBCLASSCD NOT IN (SELECT DISTINCT ";
        $query .= "                               ATTEND_SUBCLASSCD ";
        $query .= "                           FROM ";
        $query .= "                               SUBCLASS_REPLACE_COMBINED_DAT ";
        $query .= "                           WHERE ";
        $query .= "                               REPLACECD = '1' ";
        $query .= "                               AND YEAR = '".CTRL_YEAR ."' ";
        $query .= "                          ) ";
        $query .= "    AND SCHREGNO = '".$schregno ."' ";

        return $query;
    }

    function &getUpdateQuery($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);
        switch($model->trans){
            case 1://進級
            case 3://留年
            case 9://取消
                $query = " DELETE FROM ";
                $query .= "   CLASS_FORMATION_DAT T1 ";
                $query .= " WHERE ";
                $query .= "   T1.SCHREGNO IN ('" .implode($model->checked, "','") ."') AND ";
                $query .= "   (T1.YEAR,T1.SEMESTER) IN (SELECT ";
                $query .= "                         YEAR, ";
                $query .= "                         MIN(SEMESTER) ";
                $query .= "                       FROM ";
                $query .= "                         SEMESTER_MST ";
                $query .= "                       WHERE ";
                $query .= "                         YEAR = '" .((int)CTRL_YEAR+1) ."' ";
                $query .= "                       GROUP BY ";
                $query .= "                         YEAR ";
                $query .= "                       ) AND ";
                $query .= "   NOT EXISTS(SELECT ";
                $query .= "               * ";
                $query .= "             FROM ";
                $query .= "               SCHREG_BASE_MST W ";
                $query .= "             WHERE ";
                $query .= "               W.SCHREGNO = T1.SCHREGNO AND ";
                $query .= "               W.GRD_DIV  = '1' ";
                $query .= "             ) ";

                $result = $db->query($query);
                //取消でチェックされている生徒が「卒業」の場合
                if ($model->trans == 3 || $model->trans == 9){
                    $query = " UPDATE ";
                    $query .= "   SCHREG_BASE_MST ";
                    $query .= " SET ";
                    $query .= "   (GRD_DIV, GRD_NO, GRD_DATE) = (NULL,NULL,NULL) ";
                    $query .= " WHERE ";
                    $query .= "   SCHREGNO IN ('" .implode($model->checked, "','") ."') AND ";
                    $query .= "   GRD_DIV = '1' ";

                    $result = $db->query($query);
                }
                if ($model->trans == 1 || $model->trans == 3){
                    $query = " INSERT INTO CLASS_FORMATION_DAT( ";
                    $query .= " SCHREGNO, ";
                    $query .= " YEAR, ";
                    $query .= " SEMESTER, ";
                    $query .= " GRADE, ";
                    $query .= " OLD_GRADE, ";
                    $query .= " OLD_HR_CLASS, ";
                    $query .= " OLD_ATTENDNO, ";
                    $query .= " COURSECD, ";
                    $query .= " MAJORCD, ";
                    $query .= " COURSECODE, ";
                    $query .= " REMAINGRADE_FLG, ";
                    $query .= " REGISTERCD ";
                    $query .= " ) ";
                    $query .= "   (SELECT ";
                    $query .= "     ST1.SCHREGNO, ";
                    $query .= "     CHAR(INTEGER(ST1.YEAR) + 1) AS YEAR, ";
                    $query .= "     '1', ";
                    if ($model->trans == 1){        //進級
                        $query .= "     SUBSTR(DIGITS(INTEGER(ST1.GRADE)+1),9,2)  AS GRADE, ";
                    }else{
                        $query .= "     ST1.GRADE, ";
                    }
                    $query .= "     ST1.GRADE, ";
                    $query .= "     ST1.HR_CLASS, ";
                    $query .= "     ST1.ATTENDNO, ";
                    $query .= "     ST1.COURSECD, ";
                    $query .= "     ST1.MAJORCD, ";
                    $query .= "     ST1.COURSECODE, ";
                    if ($model->trans == 3){        //留年
                        $query .= "     '1',";
                    }else{
                        $query .= "     '0',";
                    }
                    $query .= "     '".STAFFCD ."' ";
                    $query .= "   FROM ";
                    $query .= "     SCHREG_REGD_DAT ST1 ";
                    $query .= "   WHERE ";
                    $query .= "     ST1.YEAR = '" .CTRL_YEAR ."' AND ";
                    $query .= "     ST1.SEMESTER = '" .CTRL_SEMESTER ."' AND ";
                    $query .= "     ST1.SCHREGNO IN ('" .implode($model->checked, "','") ."') AND ";
                    $query .= "     NOT EXISTS(SELECT ";
                    $query .= "               * ";
                    $query .= "             FROM ";
                    $query .= "               SCHREG_BASE_MST W ";
                    $query .= "             WHERE ";
                    $query .= "               W.SCHREGNO = ST1.SCHREGNO AND ";
                    $query .= "               W.GRD_DIV  = '1' ";
                    $query .= "             ) ";
                    $query .= "   ) ";

                    $result = $db->query($query);
                }
                break;
            case 2://卒業
                $query = " DELETE FROM ";
                $query .= "   CLASS_FORMATION_DAT T1 ";
                $query .= " WHERE ";
                $query .= "   T1.SCHREGNO IN ('" .implode($model->checked, "','") ."') AND ";
                $query .= "   (T1.YEAR,T1.SEMESTER) IN (SELECT ";
                $query .= "                         YEAR, ";
                $query .= "                         MIN(SEMESTER) ";
                $query .= "                       FROM ";
                $query .= "                         SEMESTER_MST ";
                $query .= "                       WHERE ";
                $query .= "                         YEAR = '" .((int)CTRL_YEAR+1) ."' ";
                $query .= "                       GROUP BY ";
                $query .= "                         YEAR ";
                $query .= "                       ) ";

                $result = $db->query($query);

                $query = " UPDATE ";
                $query .= "   SCHREG_BASE_MST ";
                $query .= " SET ";
                $query .= "   (GRD_DATE,GRD_DIV) = (SELECT ";
                $query .= "                           ST1.GRADUATE_DATE, ";
                $query .= "                           '1' ";
                $query .= "                         FROM ";
                $query .= "                           SCHOOL_MST ST1 ";
                $query .= "                         WHERE ";
                $query .= "                           ST1.YEAR = '" .CTRL_YEAR ."' ";
                $query .= "                         ) ";
                $query .= " WHERE ";
                $query .= "   SCHREGNO IN ('" .implode($model->checked, "','") ."') ";

                $result = $db->query($query);

        }

        $db->commit();
        Query::dbCheckIn($db);

        return true;
    }
}
?>
