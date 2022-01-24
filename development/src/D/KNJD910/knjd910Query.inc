<?php

require_once('for_php7.php');


class knjd910query extends Query {

    //処理学期
    function GetSemester()
    {
        $query  = " SELECT SEMESTER,SEMESTERNAME,SDATE,EDATE FROM SEMESTER_MST ";
        $query .= " WHERE  YEAR = '".CTRL_YEAR."' ORDER BY SEMESTER ";
        return $query;
    }

    //存在チェック（講座名簿データ）
    function existsChairStd($model)
    {
        $db = Query::dbCheckOut();

        $query  = " SELECT COUNT(*) ";
        $query .= " FROM   CHAIR_STD_DAT ";
        $query .= " WHERE ";
        $query .= "        YEAR = '".CTRL_YEAR."' ";
        if ($model->seme != "9") {
            $query .= "    AND SEMESTER = '".$model->seme."' ";
        }

        $ret_val = $db->getOne($query);

        Query::dbCheckIn($db);
        return $ret_val;
    }

    //存在チェック（削除する成績データ）
    function existsRecordScoreDeleteData($model)
    {
        $db = Query::dbCheckOut();

        $query  = knjd910Query::getDeleteCountQuery($model);

        $ret_val = $db->getOne($query);

        Query::dbCheckIn($db);
        return $ret_val;
    }

    //削除クエリー
    function getDeleteCountQuery($model)
    {
        $query  = "";
        $query .= " SELECT COUNT(*) FROM ";
        $query .= "     RECORD_SCORE_DAT L1 ";
        $query .= " WHERE ";
        $query .= "     L1.YEAR         = '".CTRL_YEAR."' AND ";
        $query .= "     L1.SEMESTER     = '".$model->seme."' AND ";
        $query .= "     NOT EXISTS(SELECT ";
        $query .= "                     'X' ";
        $query .= "                 FROM ";
        $query .= "                     (SELECT ";
        $query .= "                         T2.SCHREGNO, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                         T1.CLASSCD, ";
            $query .= "                         T1.SCHOOL_KIND, ";
            $query .= "                         T1.CURRICULUM_CD, ";
        }
        $query .= "                         T1.SUBCLASSCD ";
        $query .= "                     FROM ";
        $query .= "                         CHAIR_DAT T1, ";
        $query .= "                         CHAIR_STD_DAT T2 ";
        $query .= "                     WHERE ";
        $query .= "                         T1.YEAR     = '".CTRL_YEAR."' AND ";
        if ($model->seme != "9") {
            $query .= "                     T1.SEMESTER = '".$model->seme."' AND ";
        }
        $query .= "                         T2.YEAR     = T1.YEAR AND ";
        $query .= "                         T2.SEMESTER = T1.SEMESTER AND ";
        $query .= "                         T2.CHAIRCD  = T1.CHAIRCD ";
        $query .= "                     GROUP BY ";
        $query .= "                         T2.SCHREGNO, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                         T1.CLASSCD, ";
            $query .= "                         T1.SCHOOL_KIND, ";
            $query .= "                         T1.CURRICULUM_CD, ";
        }
        $query .= "                         T1.SUBCLASSCD ";
        $query .= "                     ) L4 ";
        $query .= "                 WHERE ";
        $query .= "                     L4.SCHREGNO     = L1.SCHREGNO ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                 AND L4.CLASSCD         = L1.CLASSCD ";
            $query .= "                 AND L4.SCHOOL_KIND     = L1.SCHOOL_KIND ";
            $query .= "                 AND L4.CURRICULUM_CD   = L1.CURRICULUM_CD ";
        }
        $query .= "                 AND L4.SUBCLASSCD   = L1.SUBCLASSCD ";
        $query .= "                 ) ";

        return $query;
    }


    /* 実行 */
    function ExecuteQuery(&$model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //削除クエリー
        $query  = knjd910Query::getDeleteQuery($model);
        $db->query($query);
        $db->commit();

        Query::dbCheckIn($db);
        return true;
    }

    //削除クエリー
    function getDeleteQuery($model)
    {
        $query  = "";
        $query .= " DELETE FROM ";
        $query .= "     RECORD_SCORE_DAT L1 ";
        $query .= " WHERE ";
        $query .= "     L1.YEAR         = '".CTRL_YEAR."' AND ";
        $query .= "     L1.SEMESTER     = '".$model->seme."' AND ";
        $query .= "     NOT EXISTS(SELECT ";
        $query .= "                     'X' ";
        $query .= "                 FROM ";
        $query .= "                     (SELECT ";
        $query .= "                         T2.SCHREGNO, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                         T1.CLASSCD, ";
            $query .= "                         T1.SCHOOL_KIND, ";
            $query .= "                         T1.CURRICULUM_CD, ";
        }
        $query .= "                         T1.SUBCLASSCD ";
        $query .= "                     FROM ";
        $query .= "                         CHAIR_DAT T1, ";
        $query .= "                         CHAIR_STD_DAT T2 ";
        $query .= "                     WHERE ";
        $query .= "                         T1.YEAR     = '".CTRL_YEAR."' AND ";
        if ($model->seme != "9") {
            $query .= "                     T1.SEMESTER = '".$model->seme."' AND ";
        }
        $query .= "                         T2.YEAR     = T1.YEAR AND ";
        $query .= "                         T2.SEMESTER = T1.SEMESTER AND ";
        $query .= "                         T2.CHAIRCD  = T1.CHAIRCD ";
        $query .= "                     GROUP BY ";
        $query .= "                         T2.SCHREGNO, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                         T1.CLASSCD, ";
            $query .= "                         T1.SCHOOL_KIND, ";
            $query .= "                         T1.CURRICULUM_CD, ";
        }
        $query .= "                         T1.SUBCLASSCD ";
        $query .= "                     ) L4 ";
        $query .= "                 WHERE ";
        $query .= "                     L4.SCHREGNO     = L1.SCHREGNO ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                 AND L4.CLASSCD         = L1.CLASSCD ";
            $query .= "                 AND L4.SCHOOL_KIND     = L1.SCHOOL_KIND ";
            $query .= "                 AND L4.CURRICULUM_CD   = L1.CURRICULUM_CD ";
        }
        $query .= "                 AND L4.SUBCLASSCD   = L1.SUBCLASSCD ";
        $query .= "                 ) ";

        return $query;
    }

}
?>