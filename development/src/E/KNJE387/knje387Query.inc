<?php

require_once('for_php7.php');

class knje387Query extends Query {

    //学年コンボリスト取得
    function getGrade($model) {
        $query  = " SELECT ";
        $query .= "   GRADE AS VALUE, ";
        $query .= "   GRADE || ':' || GRADE_NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "   SCHREG_REGD_GDAT ";
        $query .= " WHERE ";
        $query .= "   YEAR = '".CTRL_YEAR."' ";
        return $query;
    }

    //一覧表示
    function SelectQuery($model, $flg)
    {
        $query = " SELECT ";
        if ($flg == "COUNT") {
            $query .= "     COUNT(*) AS CNT ";
        } else {
            $query .= "   T1.SCHREGNO, ";
            $query .= "   T3.HR_NAME, ";
            $query .= "   T1.ATTENDNO, ";
            $query .= "   T2.NAME, ";
            $query .= "   CASE WHEN COALESCE(T4.CLASS_SCORE, T4.ABILITY_SCORE, T4.TOEFL_SCORE, T4.QUALIFIED_SCORE, T4.ADJUSTMENT_SCORE) = NULL THEN NULL ";
            $query .= "        ELSE VALUE(T4.CLASS_SCORE, 0)+VALUE(T4.ABILITY_SCORE, 0)+VALUE(T4.TOEFL_SCORE, 0)+VALUE(T4.QUALIFIED_SCORE, 0)+VALUE(T4.ADJUSTMENT_SCORE, 0) END AS SCORE_TOTAL, ";
            $query .= "   (VALUE(T4.CLASS_SCORE, 0)+VALUE(T4.ABILITY_SCORE, 0)+VALUE(T4.TOEFL_SCORE, 0)+VALUE(T4.QUALIFIED_SCORE, 0)) AS SCORE_SUBTOTAL, ";
            $query .= "   T4.CLASS_SCORE AS SCORE1, ";
            $query .= "   T4.ABILITY_SCORE AS SCORE2, ";
            $query .= "   T4.TOEFL_SCORE AS SCORE3, ";
            $query .= "   T4.QUALIFIED_SCORE AS SCORE4, ";
            $query .= "   T4.ADJUSTMENT_SCORE AS INPUT_ADJUST ";
        }
        $query .= " FROM ";
        $query .= "   SCHREG_REGD_DAT T1 ";
        $query .= "   INNER JOIN SCHREG_BASE_MST T2 ";
        $query .= "     ON T2.SCHREGNO = T1.SCHREGNO ";
        $query .= "   LEFT JOIN SCHREG_REGD_HDAT T3 ";
        $query .= "     ON T3.YEAR = T1.YEAR ";
        $query .= "    AND T3.SEMESTER = T1.SEMESTER ";
        $query .= "    AND T3.GRADE = T1.GRADE ";
        $query .= "    AND T3.HR_CLASS = T1.HR_CLASS ";
        $query .= "   LEFT JOIN AFT_TOTAL_STUDY_BATCH_DAT T4 ";
        $query .= "     ON T4.YEAR = T1.YEAR ";
        $query .= "    AND T4.SCHREGNO = T1.SCHREGNO ";
        $query .= " WHERE ";
        $query .= "   T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "   AND T1.SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= "   AND T1.GRADE = '".$model->grade."' ";
        if ($flg != "COUNT") {
            switch($flg) {
            case "CLASS_ASC":
                $query .= " ORDER BY T1.HR_CLASS ASC, T1.ATTENDNO ASC";
                break;
            case "CLASS_DESC":
                $query .= " ORDER BY T1.HR_CLASS DESC, T1.ATTENDNO ASC";
                break;
            case "SCORE1SORT_ASC":
                $query .= " ORDER BY CASE WHEN SCORE1 IS NULL THEN 0 ELSE 1 END DESC, SCORE1 ASC, T1.HR_CLASS ASC, T1.ATTENDNO ASC";
                break;
            case "SCORE1SORT_DESC":
                $query .= " ORDER BY CASE WHEN SCORE1 IS NULL THEN 0 ELSE 1 END DESC, SCORE1 DESC, T1.HR_CLASS ASC, T1.ATTENDNO ASC";
                break;
            case "SCORE2SORT_ASC":
                $query .= " ORDER BY CASE WHEN SCORE2 IS NULL THEN 0 ELSE 1 END DESC, SCORE2 ASC, T1.HR_CLASS ASC, T1.ATTENDNO ASC";
                break;
            case "SCORE2SORT_DESC":
                $query .= " ORDER BY CASE WHEN SCORE2 IS NULL THEN 0 ELSE 1 END DESC, SCORE2 DESC, T1.HR_CLASS ASC, T1.ATTENDNO ASC";
                break;
            case "SCORE3SORT_ASC":
                $query .= " ORDER BY CASE WHEN SCORE3 IS NULL THEN 0 ELSE 1 END DESC, SCORE3 ASC, T1.HR_CLASS ASC, T1.ATTENDNO ASC";
                break;
            case "SCORE3SORT_DESC":
                $query .= " ORDER BY CASE WHEN SCORE3 IS NULL THEN 0 ELSE 1 END DESC, SCORE3 DESC, T1.HR_CLASS ASC, T1.ATTENDNO ASC";
                break;
            case "SCORE4SORT_ASC":
                $query .= " ORDER BY CASE WHEN SCORE4 IS NULL THEN 0 ELSE 1 END DESC, SCORE4 ASC, T1.HR_CLASS ASC, T1.ATTENDNO ASC";
                break;
            case "SCORE4SORT_DESC":
                $query .= " ORDER BY CASE WHEN SCORE4 IS NULL THEN 0 ELSE 1 END DESC, SCORE4 DESC, T1.HR_CLASS ASC, T1.ATTENDNO ASC";
                break;
            case "SCORE5SORT_ASC":
                $query .= " ORDER BY CASE WHEN INPUT_ADJUST IS NULL THEN 0 ELSE 1 END DESC, INPUT_ADJUST ASC, T1.HR_CLASS ASC, T1.ATTENDNO ASC";
                break;
            case "SCORE5SORT_DESC":
                $query .= " ORDER BY CASE WHEN INPUT_ADJUST IS NULL THEN 0 ELSE 1 END DESC, INPUT_ADJUST DESC, T1.HR_CLASS ASC, T1.ATTENDNO ASC";
                break;
            case "TOTAL_ASC":
                $query .= " ORDER BY CASE WHEN SCORE_TOTAL IS NULL THEN 0 ELSE 1 END DESC, SCORE_TOTAL ASC, T1.HR_CLASS ASC, T1.ATTENDNO ASC";
                break;
            case "TOTAL_DESC":
            default:
                $query .= " ORDER BY CASE WHEN SCORE_TOTAL IS NULL THEN 0 ELSE 1 END DESC, SCORE_TOTAL DESC, T1.HR_CLASS ASC, T1.ATTENDNO ASC";
                break;
            }
        }

        return $query;
    }

    //登録件数チェック
    function cntUpdTblQuery($model, $tblName, $schregno)
    {
        $db = Query::dbCheckOut();

        $query .= " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= "  FROM ";
        $query .= "     ".$tblName." ";
        $query .= " WHERE YEAR     = '".$model->ObjYear."' ";
        $query .= "   AND SCHREGNO = '".$schregno."' ";

        $retval =$db->getOne($query);
        Query::dbCheckIn($db);
        return $retval;
    }

    function UpdQuery_TSBatch($model, $schregno_array, $searchCnt) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $data = array();
        $data["ADJUSTMENT_SCORE"][NUMBER] = $model->inputadjust[$schregno_array[1]];
        $data["REGISTERCD"][TEXT]   = STAFFCD;
        $data["UPDATED"][FUNC]      = "sysdate()";

        if ($searchCnt == 0) {
            $data["YEAR"][TEXT]  = $model->ObjYear;
            $data["SCHREGNO"][TEXT] = $schregno_array[0];
            $query = Query::insertSQL($data, "AFT_TOTAL_STUDY_BATCH_DAT");
        } else {
            $where  = " WHERE ";
            $where .= "   YEAR = '".$model->ObjYear."' ";
            $where .= "   AND SCHREGNO = '".$schregno_array[0]."' ";
            $query = Query::updateSQL($data, "AFT_TOTAL_STUDY_BATCH_DAT", $where);
        }
        $db->query($query);

        $db->commit();
        Query::dbCheckIn($db);
        return true;
    }

    function UpdQuery_TSAdjustment($model, $schregno_array, $searchCnt) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $data = array();
        $data["SCORE"][NUMBER] = $model->inputadjust[$schregno_array[1]];
        $data["REGISTERCD"][TEXT]   = STAFFCD;
        $data["UPDATED"][FUNC]      = "sysdate()";

        if ($searchCnt == 0) {
            $data["YEAR"][TEXT]  = $model->ObjYear;
            $data["SCHREGNO"][TEXT] = $schregno_array[0];

            $query = " SELECT * FROM SCHREG_REGD_DAT WHERE YEAR = '".CTRL_YEAR."' AND SEMESTER = '".CTRL_SEMESTER."' AND SCHREGNO = '".$schregno_array[0]."' ";
            $row = $db->getRow($query, DB_FETCHMODE_ASSOC);

            $data["GRADE"][TEXT] = $row["GRADE"];
            $data["HR_CLASS"][TEXT] = $row["HR_CLASS"];
            $data["ATTENDNO"][TEXT] = $row["ATTENDNO"];

            $query = Query::insertSQL($data, "AFT_TOTAL_STUDY_ADJUSTMENT_DAT");
        } else {
            $where  = " WHERE ";
            $where .= "   YEAR = '".$model->ObjYear."' ";
            $where .= "   AND SCHREGNO = '".$schregno_array[0]."' ";
            $query = Query::updateSQL($data, "AFT_TOTAL_STUDY_ADJUSTMENT_DAT", $where);
        }
        $db->query($query);

        $db->commit();
        Query::dbCheckIn($db);
        return true;
    }

}
?>