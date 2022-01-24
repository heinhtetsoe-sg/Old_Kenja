<?php

require_once('for_php7.php');

class knja612Query extends Query
{
    //年組取得
    public function getGradeHrClass($model)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     GRADE || HR_CLASS AS VALUE, ";
        $query .= "     HR_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR     = '".CTRL_YEAR."' AND ";
        $query .= "     SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //得点一覧取得
    public function getScoreList($model)
    {
        $query  = " SELECT ";
        $query .= "     DAT.ATTENDNO AS NO, ";
        $query .= "     BASE.SCHREGNO, ";
        $query .= "     BASE.NAME, ";
        $query .= "     T1.SCORE ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT DAT ";
        $query .= "     LEFT JOIN SCHREG_BASE_MST BASE ON ";
        $query .= "          BASE.SCHREGNO = DAT.SCHREGNO ";
        $query .= "     LEFT JOIN HTRAINREMARK_SCORE_DAT T1 ON ";
        $query .= "          T1.YEAR     = DAT.YEAR AND ";
        $query .= "          T1.SCHREGNO = DAT.SCHREGNO AND ";
        $query .= "          T1.DATA_DIV = '".$model->field["DATA_DIV"]."' ";
        $query .= " WHERE ";
        $query .= "     DAT.YEAR                  = '".CTRL_YEAR."' AND ";
        $query .= "     DAT.SEMESTER              = '".CTRL_SEMESTER."' AND ";
        $query .= "     DAT.GRADE || DAT.HR_CLASS = '".$model->field["GRADE_HR_CLASS"]."' ";
        $query .= " ORDER BY ";
        $query .= "     DAT.ATTENDNO ";

        return $query;
    }

    public function getMaxScore($model)
    {
        $query  = " SELECT ";
        $query .= "     MAX(TO_SCORE) ";
        $query .= " FROM ";
        $query .= "     HTRAINREMARK_TEMP_SCORE_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     GRADE = '".substr($model->field["GRADE_HR_CLASS"], 0, 2)."' AND ";
        $query .= "     DATA_DIV = '".$model->field["DATA_DIV"]."' ";

        return $query;
    }

    public function modifyHtrainremarkScoreDat($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        foreach ($model->scoreList as $schregno => $score) {
            $where  = " WHERE ";
            $where .= "     YEAR     = '".CTRL_YEAR."' AND ";
            $where .= "     DATA_DIV = '".$model->field["DATA_DIV"]."' AND ";
            $where .= "     SCHREGNO = '".$schregno."' ";

            /* 更新対象データが存在するかをチェック */
            $query  = " SELECT ";
            $query .= "     COUNT(*) ";
            $query .= " FROM ";
            $query .= "     HTRAINREMARK_SCORE_DAT ";
            $query .= $where;

            $flag = $db->getOne($query);

            $data = array();

            if ($flag > 0) {
                /* 更新対象データがある時 かつ 点数が空欄でないとき - UPDATE */
                if ($score != "") {
                    $data["SCORE"][TEXT]      = $score;
                    $data["REGISTERCD"][TEXT] = STAFFCD ;
                    $data["UPDATED"][FUNC]    = "sysdate()";

                    $query = Query::updateSQL($data, "HTRAINREMARK_SCORE_DAT", $where);
                } else {
                    $query = "DELETE FROM HTRAINREMARK_SCORE_DAT ".$where;
                }
            } else {
                $data["YEAR"][TEXT]       = CTRL_YEAR;
                $data["DATA_DIV"][TEXT]   = $model->field["DATA_DIV"];
                $data["SCHREGNO"][TEXT]   = $schregno;
                $data["SCORE"][TEXT]      = $score;
                $data["REGISTERCD"][TEXT] = STAFFCD ;
                $data["UPDATED"][FUNC]    = "sysdate()";

                $query = Query::insertSQL($data, "HTRAINREMARK_SCORE_DAT");
            }
            $db->query($query);
        }
        $db->commit();
        Query::dbCheckIn($db);

        return true;
    }
}
