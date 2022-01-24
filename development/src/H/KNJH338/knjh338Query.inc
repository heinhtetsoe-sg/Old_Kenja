<?php

require_once('for_php7.php');

class knjh338Query extends Query
{

    /* 学期取得 */
    public function getSemester()
    {
        $query  = " SELECT ";
        $query .= "     SEMESTER AS VALUE, ";
        $query .= "     SEMESTERNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' AND";
        $query .= "     SEMESTER < '9' ";

        return $query;
    }

    //年組一覧
    public function getGradeHr($model)
    {
        $query  = "SELECT DISTINCT ";
        $query .= "    GRADE || HR_CLASS AS VALUE, ";
        $query .= "    HR_NAME AS LABEL ";
        $query .= "FROM ";
        $query .= "    SCHREG_REGD_HDAT ";
        $query .= "WHERE ";
        $query .= "    YEAR = '".CTRL_YEAR."' ";
        $query .= "    AND SEMESTER = '".$model->field["SEMESTER"]."' ";
        if (AUTHORITY != DEF_UPDATABLE) {
            $query .= "     AND (TR_CD1 = '".STAFFCD."' ";
            $query .= "          OR TR_CD2 = '".STAFFCD."' ";
            $query .= "          OR TR_CD3 = '".STAFFCD."' ";
            $query .= "          OR SUBTR_CD1 = '".STAFFCD."' ";
            $query .= "          OR SUBTR_CD2 = '".STAFFCD."' ";
            $query .= "          OR SUBTR_CD3 = '".STAFFCD."') ";
        }
        $query .= "ORDER BY ";
        $query .= "    VALUE ";

        return $query;
    }

    //模試種別
    public function getMockDiv()
    {
        $query  = " SELECT ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND NAMECD1 = 'H308' ";
        $query .= "     AND NAMESPARE1 = '1' ";
        $query .= " ORDER BY ";
        $query .= "     NAMECD2 ";

        return $query;
    }

    //模試、実力テスト
    public function getMockMst($target)
    {
        $query  = "SELECT ";
        $query .= "    MOCKCD AS VALUE, ";
        $query .= "    MOCKCD || ':' || MOCKNAME1 AS LABEL ";
        $query .= "FROM ";
        $query .= "    MOCK_MST ";
        $query .= "WHERE ";
        $query .= "    substr(MOCKCD, 1, 1) = '".$target."' ";
        $query .= "ORDER BY ";
        $query .= "    VALUE ";

        return $query;
    }

    //模試科目一覧
    public function getMockSubclassMst($model)
    {
        $query  = " WITH REGD_T AS ( ";
        $query .= " SELECT DISTINCT ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.COURSECD, ";
        $query .= "     T1.MAJORCD, ";
        $query .= "     T1.COURSECODE ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND T1.SEMESTER = '".$model->field["SEMESTER"]."' ";
        $query .= "     AND T1.GRADE || T1.HR_CLASS = '".$model->field["GRADE_HR_CLASS"]."' ";
        $query .= " ), GROUP_T AS ( ";
        $query .= " SELECT DISTINCT ";
        $query .= "     T1.MOCK_SUBCLASS_CD ";
        $query .= " FROM ";
        $query .= "     MOCK_SUBCLASS_GROUP_DAT T1, ";
        $query .= "     REGD_T T2 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND T1.MOCKCD = '".$model->field["MOCK_TARGET"]."' ";
        $query .= "     AND T1.GRADE = T2.GRADE ";
        $query .= "     AND T1.COURSECD = T2.COURSECD ";
        $query .= "     AND T1.MAJORCD = T2.MAJORCD ";
        $query .= "     AND T1.COURSECODE = T2.COURSECODE ";
        $query .= " ) ";
        $query .= " SELECT DISTINCT ";
        $query .= "     T1.MOCK_SUBCLASS_CD AS VALUE, ";
        $query .= "     T1.SUBCLASS_ABBV AS LABEL ";
        $query .= " FROM ";
        $query .= "     MOCK_SUBCLASS_MST T1 ";
        $query .= " WHERE ";
        $query .= "     T1.MOCK_SUBCLASS_CD IN (SELECT I1.MOCK_SUBCLASS_CD FROM GROUP_T I1) ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    /* 出欠月別累積データ・出欠累積データ・出欠科目別累積データ・学籍異動データ */
    public function selectMainData($model)
    {
        $query .= " SELECT ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.ATTENDNO AS ORG_ATTENDNO, ";
        $query .= "     T1.COURSECD || T1.MAJORCD || T1.COURSECODE as COURSE, ";
        $query .= "     L1.HR_NAME || '-' || T1.ATTENDNO AS ATTENDNO, ";
        $query .= "     T1.SCHREGNO || '　' || L2.NAME AS NAME_SHOW, ";
        $query .= "     L2.NAME, ";
        $query .= "     L3.SCORE, ";
        $query .= "     L3.SCORE_DI, ";
        $query .= "     L3.RANK, ";
        $query .= "     L3.DEVIATION ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= "     LEFT JOIN SCHREG_REGD_HDAT L1 ON L1.YEAR = T1.YEAR ";
        $query .= "          AND L1.SEMESTER = T1.SEMESTER ";
        $query .= "          AND L1.GRADE || L1.HR_CLASS = T1.GRADE || T1.HR_CLASS ";
        $query .= "     LEFT JOIN SCHREG_BASE_MST L2 ON L2.SCHREGNO = T1.SCHREGNO ";
        $query .= "     LEFT JOIN MOCK_DAT L3 ON L3.YEAR = T1.YEAR ";
        $query .= "          AND L3.MOCKCD = '".$model->field["MOCK_TARGET"]."' ";
        $query .= "          AND L3.SCHREGNO = T1.SCHREGNO ";
        $query .= "          AND L3.MOCK_SUBCLASS_CD = '".$model->field["MOCK_SUBCLASS_CD"]."' ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND T1.SEMESTER = '".$model->field["SEMESTER"]."' ";
        $query .= "     AND T1.GRADE || T1.HR_CLASS = '".$model->field["GRADE_HR_CLASS"]."' ";
        $query .= " ORDER BY ";
        $query .= "     T1.ATTENDNO ";

        return $query;
    }

    //満点マスタ
    public function getPerfect($model, $data)
    {
        if ($model->Properties["usePerfectCourse"] == "1") {
            $query  = "";
            $query .= "SELECT ";
            $query .= "    CASE WHEN DIV IS NULL ";
            $query .= "         THEN 100 ";
            $query .= "         ELSE PERFECT ";
            $query .= "    END AS PERFECT ";
            $query .= "FROM ";
            $query .= "    MOCK_PERFECT_COURSE_DAT ";
            $query .= "WHERE ";
            $query .= "    YEAR = '".CTRL_YEAR."' ";
            $query .= "    AND MOCKCD = '{$model->field["MOCK_TARGET"]}' ";
            $query .= "    AND MOCK_SUBCLASS_CD = '{$model->field["MOCK_SUBCLASS_CD"]}' ";
            $query .= "    AND GRADE = CASE WHEN DIV = '01' ";
            $query .= "                     THEN '00' ";
            $query .= "                     ELSE '{$data["GRADE"]}' ";
            $query .= "                END ";
            $query .= "    AND COURSECD || MAJORCD || COURSECODE = CASE WHEN DIV IN ('01','02') ";
            $query .= "                                                 THEN '00000000' ";
            $query .= "                                                 ELSE '{$data["COURSE"]}' ";
            $query .= "                                            END ";
        } else {
            $query  = "";
            $query .= "SELECT ";
            $query .= "    CASE WHEN COURSE_DIV IS NULL ";
            $query .= "         THEN 100 ";
            $query .= "         ELSE PERFECT ";
            $query .= "    END AS PERFECT ";
            $query .= "FROM ";
            $query .= "    MOCK_PERFECT_DAT ";
            $query .= "WHERE ";
            $query .= "    YEAR = '".CTRL_YEAR."' ";
            $query .= "    AND MOCK_SUBCLASS_CD = '{$model->field["MOCK_SUBCLASS_CD"]}' ";
            $query .= "    AND GRADE = '{$data["GRADE"]}' ";
        }
        return $query;
    }

    /* 模試データの更新 */
    public function getUpdateQuery($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        foreach ($model->field["SCHREGNO"] as $key => $schregno) {
            /* 更新対象データが存在するかをチェック */
            $query  = " SELECT ";
            $query .= "     COUNT(*) ";
            $query .= " FROM ";
            $query .= "     MOCK_DAT ";
            $query .= " WHERE ";
            $query .= "     YEAR = '".CTRL_YEAR."' ";
            $query .= "     AND MOCKCD = '".$model->field["MOCK_TARGET"]."' ";
            $query .= "     AND SCHREGNO = '".$schregno."' ";
            $query .= "     AND MOCK_SUBCLASS_CD = '".$model->field["MOCK_SUBCLASS_CD"]."' ";

            $flag = $db->getOne($query);

            $data = array();

            /* 更新対象データがある時 - UPDATE */
            if ($flag > 0) {
                $where  = " WHERE ";
                $where .= "     YEAR = '".CTRL_YEAR."' ";
                $where .= "     AND MOCKCD = '".$model->field["MOCK_TARGET"]."' ";
                $where .= "     AND SCHREGNO = '".$schregno."' ";
                $where .= "     AND MOCK_SUBCLASS_CD = '".$model->field["MOCK_SUBCLASS_CD"]."' ";
            } else {
                $data["YEAR"][TEXT]             = CTRL_YEAR;
                $data["MOCKCD"][TEXT]           = $model->field["MOCK_TARGET"];
                $data["SCHREGNO"][TEXT]         = $schregno;
                $data["MOCK_SUBCLASS_CD"][TEXT] = $model->field["MOCK_SUBCLASS_CD"];
            }

            /* 更新データをセット */
            $setScore = strlen($model->field["SCORE"][$key]) > 0 ? $model->field["SCORE"][$key] : null;
            if ($setScore == "*") {
                $data["SCORE"][NUMBER]      = null;
                $data["SCORE_DI"][TEXT]     = $setScore;
                $data["RANK"][NUMBER]       = null;
                $data["DEVIATION"][NUMBER]  = null;
            } elseif (strlen($setScore) == 0) {
                $data["SCORE"][NUMBER]      = $setScore;
                $data["SCORE_DI"][TEXT]     = null;
                $data["RANK"][NUMBER]       = null;
                $data["DEVIATION"][NUMBER]  = null;
            } else {
                $data["SCORE"][NUMBER]    = $setScore;
                $data["SCORE_DI"][TEXT]    = null;
            }
            $data["REGISTERCD"][TEXT] = STAFFCD ;
            $data["UPDATED"][FUNC]    = "sysdate()";
            /* 更新対象データがある時 - UPDATE */
            if ($flag > 0) {
                $query = Query::updateSQL($data, "MOCK_DAT", $where);
            } else {
                $query = Query::insertSQL($data, "MOCK_DAT");
            }
            $db->query($query);
        }
        $db->commit();
        Query::dbCheckIn($db);
        return true;
    }

    /* 模試データの削除 */
    public function getDeleteQuery($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        foreach ($model->field["SCHREGNO"] as $key => $schregno) {
            /* 更新対象データが存在するかをチェック */
            $query  = " DELETE FROM MOCK_DAT ";
            $query .= " WHERE ";
            $query .= "     YEAR = '".CTRL_YEAR."' ";
            $query .= "     AND MOCKCD = '".$model->field["MOCK_TARGET"]."' ";
            $query .= "     AND SCHREGNO = '".$schregno."' ";
            $query .= "     AND MOCK_SUBCLASS_CD = '".$model->field["MOCK_SUBCLASS_CD"]."' ";

            $db->query($query);
        }

        $db->commit();
        Query::dbCheckIn($db);
        return true;
    }
}
