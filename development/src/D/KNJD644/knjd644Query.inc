<?php

require_once('for_php7.php');

class knjd644Query extends Query {

    //学部一覧取得
    function getBucd()
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     T1.BU_CD, ";
        $query .= "     T3.BU_NAME ";
        $query .= " FROM ";
        $query .= "     SCHREG_RECOMMENDATION_WISH_DAT T1 ";
        $query .= " LEFT OUTER JOIN NAME_MST T2 ";
        $query .= "     ON T1.SCHOOL_CD = T2.ABBV3 AND T2.NAMECD1 = 'Z010' AND T2.NAMECD2 = '00' ";
        $query .= " LEFT OUTER JOIN COLLEGE_RECOMMENDATION_DAT T3 ";
        $query .= "     ON T1.YEAR = T3.YEAR AND T1.SCHOOL_CD = T3.SCHOOL_CD AND T1.BU_CD = T3.BU_CD ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '" .CTRL_YEAR ."'";
        $query .= " ORDER BY T1.BU_CD ";
        return $query;
    }

    //学科一覧取得
    function getKacd($model)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     T1.KA_CD, ";
        $query .= "     T3.KA_NAME ";
        $query .= " FROM ";
        $query .= "     SCHREG_RECOMMENDATION_WISH_DAT T1 ";
        $query .= " LEFT OUTER JOIN NAME_MST T2 ";
        $query .= "     ON T1.SCHOOL_CD = T2.ABBV3 AND T2.NAMECD1 = 'Z010' AND T2.NAMECD2 = '00' ";
        $query .= " LEFT OUTER JOIN COLLEGE_RECOMMENDATION_DAT T3 ";
        $query .= "     ON T1.YEAR = T3.YEAR AND T1.SCHOOL_CD = T3.SCHOOL_CD AND T1.BU_CD = T3.BU_CD AND T1.KA_CD = T3.KA_CD ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '" .CTRL_YEAR ."' AND";
        $query .= "     T1.BU_CD = '" .$model->field["BU_CD"] ."'";
        $query .= " ORDER BY T1.KA_CD ";
        return $query;
    }

    //希望順位一覧取得
    function getWishrank($model)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     INT(T1.WISH_RANK) AS WISH_RANK ";
        $query .= " FROM ";
        $query .= "     SCHREG_RECOMMENDATION_WISH_DAT T1 ";
        $query .= " LEFT OUTER JOIN NAME_MST T2 ";
        $query .= "     ON T1.SCHOOL_CD = T2.ABBV3 AND T2.NAMECD1 = 'Z010' AND T2.NAMECD2 = '00' ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '" .CTRL_YEAR ."' AND";
        $query .= "     T1.BU_CD = '" .$model->field["BU_CD"] ."' AND";
        $query .= "     T1.KA_CD = '" .$model->field["KA_CD"] ."'";
        $query .= " ORDER BY  ";
        $query .= "     WISH_RANK ";

        return $query;
    }


    //候補者一覧
    function selectStdCandidate($model)
    {
        $query  = " SELECT ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     S3.HR_NAME, ";
        $query .= "     T2.ATTENDNO, ";
        $query .= "     S4.NAME, ";
        $query .= "     S5.SCORE3 ";
        $query .= " FROM ";
        $query .= "     SCHREG_RECOMMENDATION_WISH_DAT T1 ";
        $query .= "     LEFT OUTER JOIN NAME_MST S1  ";
        $query .= "         ON T1.SCHOOL_CD = S1.ABBV3 AND S1.NAMECD1 = 'Z010' AND S1.NAMECD2 = '00' ";
        $query .= "     LEFT OUTER JOIN COLLEGE_RECOMMENDATION_DAT S2  ";
        $query .= "         ON T1.YEAR = S2.YEAR AND T1.SCHOOL_CD = S2.SCHOOL_CD AND ";
        $query .= "            T1.BU_CD = S2.BU_CD AND T1.KA_CD = S2.KA_CD ";
        $query .= "     LEFT OUTER JOIN RECORD_MOCK_RANK_DAT S5 ";
        $query .= "             ON S5.YEAR = T1.YEAR  ";
        $query .= "            AND S5.SCHREGNO = T1.SCHREGNO ";
        $query .= "            AND S5.DATA_DIV = '1' ";
        $query .= "            AND S5.COURSE_DIV = S2.DIV ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "         AND S5.CLASSCD = '33' ";
            $query .= "         AND S5.SCHOOL_KIND = 'H' ";
            $query .= "         AND S5.CURRICULUM_CD = '2' ";
        }
        $query .= "            AND S5.SUBCLASSCD = '333333', ";
        $query .= "     SCHREG_REGD_DAT T2   ";
        $query .= "     LEFT OUTER JOIN SCHREG_REGD_HDAT S3  ";
        $query .= "         ON T2.YEAR = S3.YEAR AND T2.SEMESTER = S3.SEMESTER AND  ";
        $query .= "            T2.GRADE = S3.GRADE AND T2.HR_CLASS = S3.HR_CLASS  ";
        $query .= "     LEFT OUTER JOIN SCHREG_BASE_MST S4  ";
        $query .= "         ON T2.SCHREGNO = S4.SCHREGNO ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = T2.YEAR AND ";
        $query .= "     T1.YEAR = '" .CTRL_YEAR ."' AND ";
        $query .= "     T2.SEMESTER = '" .CTRL_SEMESTER ."' AND ";
        $query .= "     T1.SCHREGNO = T2.SCHREGNO AND ";
        $query .= "     T1.WISH_RANK = '" .$model->field["WISH_RANK"] ."' AND ";
        $query .= "     T1.BU_CD = '" .$model->field["BU_CD"] ."' AND ";
        $query .= "     T1.KA_CD = '" .$model->field["KA_CD"] ."' AND ";
        $query .= "     T1.SCHREGNO NOT IN (SELECT W1.SCHREGNO ";
        $query .= "                         FROM SCHREG_RECOMMENDATION_WISH_DAT W1 ";
        $query .= "                         WHERE W1.YEAR = T1.YEAR AND ";
        $query .= "                               W1.RECOMMENDATION_FLG = '1' ";
        $query .= "                        ) ";
        $query .= " ORDER BY ";
        $query .= "     S5.SCORE3 DESC, ";
        $query .= "     T2.GRADE, ";
        $query .= "     T2.HR_CLASS, ";
        $query .= "     T2.ATTENDNO ";
        return $query;
    }


    //決定者一覧
    function selectStdDecision($model)
    {
        $query  = " SELECT";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     S3.HR_NAME, ";
        $query .= "     T2.ATTENDNO, ";
        $query .= "     S4.NAME, ";
        $query .= "     S5.SCORE3 ";
        $query .= " FROM ";
        $query .= "     SCHREG_RECOMMENDATION_WISH_DAT T1   ";
        $query .= "     LEFT OUTER JOIN NAME_MST S1  ";
        $query .= "         ON T1.SCHOOL_CD = S1.ABBV3 AND S1.NAMECD1 = 'Z010' AND S1.NAMECD2 = '00'  ";
        $query .= "     LEFT OUTER JOIN COLLEGE_RECOMMENDATION_DAT S2  ";
        $query .= "         ON T1.YEAR = S2.YEAR AND T1.SCHOOL_CD = S2.SCHOOL_CD AND ";
        $query .= "            T1.BU_CD = S2.BU_CD AND T1.KA_CD = S2.KA_CD ";
        $query .= "     LEFT OUTER JOIN RECORD_MOCK_RANK_DAT S5 ";
        $query .= "             ON S5.YEAR = T1.YEAR  ";
        $query .= "            AND S5.SCHREGNO = T1.SCHREGNO  ";
        $query .= "            AND S5.DATA_DIV = '1' ";
        $query .= "            AND S5.COURSE_DIV = S2.DIV ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "         AND S5.CLASSCD = '33' ";
            $query .= "         AND S5.SCHOOL_KIND = 'H' ";
            $query .= "         AND S5.CURRICULUM_CD = '2' ";
        }
        $query .= "            AND S5.SUBCLASSCD = '333333', ";
        $query .= "     SCHREG_REGD_DAT T2   ";
        $query .= "     LEFT OUTER JOIN SCHREG_REGD_HDAT S3  ";
        $query .= "         ON T2.YEAR = S3.YEAR AND T2.SEMESTER = S3.SEMESTER AND  ";
        $query .= "            T2.GRADE = S3.GRADE AND T2.HR_CLASS = S3.HR_CLASS  ";
        $query .= "     LEFT OUTER JOIN SCHREG_BASE_MST S4  ";
        $query .= "         ON T2.SCHREGNO = S4.SCHREGNO ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = T2.YEAR AND ";
        $query .= "     T1.YEAR = '" .CTRL_YEAR ."' AND ";
        $query .= "     T2.SEMESTER = '" .CTRL_SEMESTER ."' AND ";
        $query .= "     T1.SCHREGNO = T2.SCHREGNO AND ";
        $query .= "     T1.WISH_RANK = '" .$model->field["WISH_RANK"] ."' AND ";
        $query .= "     T1.BU_CD = '" .$model->field["BU_CD"] ."' AND ";
        $query .= "     T1.KA_CD = '" .$model->field["KA_CD"] ."' AND ";
        $query .= "     T1.RECOMMENDATION_FLG = '1' ";
        $query .= " ORDER BY ";
        $query .= "     S5.SCORE3 DESC, ";
        $query .= "     T2.GRADE, ";
        $query .= "     T2.HR_CLASS, ";
        $query .= "     T2.ATTENDNO ";
        return $query;
    }

    //推薦枠取得
    function getRecomFrame($model)
    {
        $query  = " SELECT T1.FRAME ";
        $query .= " FROM COLLEGE_RECOMMENDATION_DAT T1 ";
        $query .= "      LEFT OUTER JOIN NAME_MST T2  ";
        $query .= "          ON T1.SCHOOL_CD = T2.ABBV3 AND T2.NAMECD1 = 'Z010' AND T2.NAMECD2 = '00' ";
        $query .= " WHERE T1.YEAR  = '" .CTRL_YEAR ."' AND ";
        $query .= "       T1.BU_CD = '" .$model->field["BU_CD"] ."' AND ";
        $query .= "       T1.KA_CD = '" .$model->field["KA_CD"] ."' ";
        return $query;
    }

    //決定者数取得
    function getRecomDecis($model)
    {
        $query  = " SELECT COUNT(T1.SCHREGNO) ";
        $query .= " FROM SCHREG_RECOMMENDATION_WISH_DAT T1 ";
        $query .= "      LEFT OUTER JOIN NAME_MST T2  ";
        $query .= "          ON T1.SCHOOL_CD = T2.ABBV3 AND T2.NAMECD1 = 'Z010' AND T2.NAMECD2 = '00' ";
        $query .= " WHERE T1.YEAR  = '" .CTRL_YEAR ."' AND ";
        $query .= "       T1.BU_CD = '" .$model->field["BU_CD"] ."' AND ";
        $query .= "       T1.KA_CD = '" .$model->field["KA_CD"] ."' AND ";
        $query .= "       T1.RECOMMENDATION_FLG = '1' ";
        return $query;
    }

    //候補人数取得
    function getRecomCand($model)
    {
        $query  = " SELECT COUNT(T1.SCHREGNO) ";
        $query .= " FROM SCHREG_RECOMMENDATION_WISH_DAT T1 ";
        $query .= "      LEFT OUTER JOIN NAME_MST T2  ";
        $query .= "          ON T1.SCHOOL_CD = T2.ABBV3 AND T2.NAMECD1 = 'Z010' AND T2.NAMECD2 = '00' ";
        $query .= " WHERE T1.YEAR  = '" .CTRL_YEAR ."' AND ";
        $query .= "       T1.WISH_RANK = '" .$model->field["WISH_RANK"] ."' AND ";
        $query .= "       T1.BU_CD = '" .$model->field["BU_CD"] ."' AND ";
        $query .= "       T1.KA_CD = '" .$model->field["KA_CD"] ."' AND ";
        $query .= "       T1.SCHREGNO NOT IN (SELECT T3.SCHREGNO ";
        $query .= "                           FROM SCHREG_RECOMMENDATION_WISH_DAT T3 ";
        $query .= "                           WHERE T3.YEAR = T1.YEAR AND ";
        $query .= "                                 T3.RECOMMENDATION_FLG = '1' ";
        $query .= "                          ) ";
        return $query;
    }

    //学校コード取得
    function getSchoolcdQuery()
    {
        $query  = " SELECT ABBV3 ";
        $query .= " FROM NAME_MST ";
        $query .= " WHERE NAMECD1 = 'Z010' AND ";
        $query .= "       NAMECD2 = '00' ";
        return $query;
    }

    /**
     * UPDATE
     */
    //推薦フラグをはずす
    function &getUpdateQuery($model)
    {        

        $db = Query::dbCheckOut();

        $school_cd = $db->getOne(knjd644Query::getSchoolcdQuery());
        $data["RECOMMENDATION_FLG"][TEXT]   = '';
        
        $where  = " WHERE YEAR          = '".CTRL_YEAR."'";
        $where .= "   AND WISH_RANK     = '".$model->field["WISH_RANK"]."'";
        $where .= "   AND BU_CD         = '".$model->field["BU_CD"]."'";
        $where .= "   AND KA_CD         = '".$model->field["KA_CD"]."'";
        $where .= "   AND SCHOOL_CD     = '".$school_cd."'";

        $query = Query::updateSQL($data, "SCHREG_RECOMMENDATION_WISH_DAT", $where);

        $db->query($query);
        Query::dbCheckIn($db);
        return;

    }

    function &getUpdateQuery2($model)
    {        

        $array = explode (",", $model->selectdata);
        
        $db = Query::dbCheckOut();
        $db->autoCommit(false);
        $updated = date("Y-m-d H:i:s", time());

        $data = array();
        $selectdata = explode(",", $model->selectdata);
        for ($i = 0; $i < get_count($selectdata); $i++) {
            if (!$selectdata[$i]) {
                continue;
            }
            $school_cd = $db->getOne(knjd644Query::getSchoolcdQuery());
            $data["RECOMMENDATION_FLG"][TEXT]   = 1;
            $data["REGISTERCD"][TEXT]           = STAFFCD;
            $data["UPDATED"][FUNC]              = "sysdate()";

            $where  = " WHERE YEAR          = '".CTRL_YEAR."'";
            $where .= "   AND WISH_RANK     = '".$model->field["WISH_RANK"]."'";
            $where .= "   AND BU_CD         = '".$model->field["BU_CD"]."'";
            $where .= "   AND KA_CD         = '".$model->field["KA_CD"]."'";
            $where .= "   AND SCHREGNO      = '".$selectdata[$i]."'";
            $where .= "   AND SCHOOL_CD     = '".$school_cd."'";

            $query = Query::updateSQL($data, "SCHREG_RECOMMENDATION_WISH_DAT", $where);
            $db->query($query);
        }
        $db->commit();
        Query::dbCheckIn($db);
        return;


    }
}
?>
