<?php
class knje372gQuery extends Query
{

    //学科推薦枠数取得
    public function getDepLimitCnt()
    {
        $query  = "         WITH REC_LIMIT AS ( ";
        $query .= "             SELECT ";
        $query .= "               DEPARTMENT_S, ";
        $query .= "     		  LIMIT_COUNT_S, ";
        $query .= "               DEPARTMENT_H, ";
        $query .= "     		  LIMIT_COUNT_H, ";
        $query .= "     		  DEPARTMENT_NAME ";
        $query .= "             FROM ";
        $query .= "               AFT_RECOMMENDATION_LIMIT_MST ";
        $query .= "             WHERE ";
        $query .= "               YEAR = '".CTRL_YEAR."' ";
        $query .= "         ), DEPARTMENT_HS AS ( ";
        $query .= "     	    SELECT '専' AS DEPARTMENT_DIV, DEPARTMENT_S AS DEPARTMENTCD, LIMIT_COUNT_S AS LIMIT_COUNT, DEPARTMENT_NAME FROM REC_LIMIT WHERE DEPARTMENT_S IS NOT NULL ";
        $query .= "     	    UNION ";
        $query .= "     	    SELECT '併' AS DEPARTMENT_DIV, DEPARTMENT_H AS DEPARTMENTCD, LIMIT_COUNT_H AS LIMIT_COUNT, DEPARTMENT_NAME FROM REC_LIMIT WHERE DEPARTMENT_H IS NOT NULL ";
        $query .= "         ) ";
        $query .= "         SELECT ";
        $query .= "     	  DEPARTMENT_DIV, ";
        $query .= "           DEPARTMENTCD, ";
        $query .= "     	  LIMIT_COUNT, ";
        $query .= "     	  DEPARTMENT_NAME ";
        $query .= "         FROM ";
        $query .= "           DEPARTMENT_HS ";

        return $query;
    }

    //専併対応取得
    public function getDepCd()
    {
        $query  = "     SELECT ";
        $query .= "         DEPARTMENT_H,";
        $query .= "         DEPARTMENT_S";
        $query .= "     FROM ";
        $query .= "         AFT_RECOMMENDATION_LIMIT_MST ";
        $query .= "     WHERE ";
        $query .= "         YEAR = '".CTRL_YEAR."' ";

        return $query;
    }

    //メイン取得
    public function getList($model)
    {
        $query  = "     SELECT ";
        $query .= "       HOPE.SCHREGNO, ";
        $query .= "       CONVERT.CONVERT_RANK, ";
        $query .= "       HOPE.HOPE_ORDER, ";
        $query .= "       HOPE.DEPARTMENT_CD ";
        $query .= "     FROM ";
        $query .= "       AFT_SCHREG_HOPE_DEPARTMENT HOPE ";
        $query .= "       INNER JOIN SCHREG_REGD_DAT REGD ";
        $query .= "         ON REGD.YEAR = HOPE.YEAR ";
        $query .= "         AND REGD.SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= "         AND REGD.SCHREGNO = HOPE.SCHREGNO ";
        $query .= "       INNER JOIN AFT_SCHREG_RECOMMENDATION_INFO_DAT INFO ";
        $query .= "         ON INFO.YEAR = HOPE.YEAR ";
        $query .= "         AND INFO.SCHREGNO = HOPE.SCHREGNO ";
        $query .= "       INNER JOIN AFT_SCHREG_CONVERT_SCORE_DAT CONVERT ";
        $query .= "         ON CONVERT.YEAR = HOPE.YEAR ";
        $query .= "         AND CONVERT.SCHREGNO = HOPE.SCHREGNO ";
        $query .= "     WHERE ";
        $query .= "       HOPE.YEAR = '".CTRL_YEAR."' ";
        $query .= "       AND HOPE.DEPARTMENT_CD IS NOT NULL ";
        $query .= "       AND CONVERT.CONVERT_RANK IS NOT NULL ";
        $query .= "       AND VALUE (HOPE.RECOMMENDATION_BASE_DIV, '') <> '9' ";
        $query .= "       AND VALUE (HOPE.DEPARTMENT_BASE_DIV, '') <> '9' ";
        $query .= "       AND VALUE (INFO.DECLINE_FLG, '') <> '1' ";
        $query .= "       AND CONVERT.PROFICIENCY1_AVG IS NOT NULL ";
        $query .= "       AND CONVERT.PROFICIENCY2_AVG IS NOT NULL ";
        $query .= "     ORDER BY ";
        $query .= "       CONVERT.CONVERT_RANK, ";
        $query .= "       HOPE.HOPE_ORDER, ";
        $query .= "       REGD.GRADE, ";
        $query .= "       REGD.HR_CLASS, ";
        $query .= "       REGD.ATTENDNO ";

        return $query;
    }

    public function &getUpdateQuery($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //更新
        $data = array();
        $data["RECOMMENDATION_DEPARTMENT_CD"][TEXT] = null;
        //条件
        $where  = "WHERE   YEAR          = '".CTRL_YEAR."' ";

        $query = Query::updateSQL($data, "AFT_SCHREG_CONVERT_SCORE_DAT", $where);
        $db->query($query);

        foreach ($model->allocatedSchMap as $schregno => $allocatedDepCd) {
            //更新
            $data = array();
            $data["RECOMMENDATION_DEPARTMENT_CD"][TEXT] = $allocatedDepCd;
            //条件
            $where  = "WHERE   YEAR          = '".CTRL_YEAR."' ";
            $where .= "  AND   SCHREGNO      = '{$schregno}' ";

            $query = Query::updateSQL($data, "AFT_SCHREG_CONVERT_SCORE_DAT", $where);
            $db->query($query);
        }

        $db->commit();
        Query::dbCheckIn($db);

        return true;
    }
}
