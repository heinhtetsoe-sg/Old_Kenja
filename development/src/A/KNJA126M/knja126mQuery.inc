<?php

require_once('for_php7.php');

class knja126mQuery extends Query {

    //１レコード取得
    function getTrainRow($schregno, $exp_year)
    {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     HTRAINREMARK_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".$exp_year."' AND ";
        $query .= "     SCHREGNO    = '".$schregno."' ";

        return $query;
    }

    //学習記録データ取得
    function getStudyRec($model) {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     SCHREG_STUDYREC_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR     = '".$model->exp_year."' AND ";
        $query .= "     SCHREGNO = '".$model->schregno."' ";

        return $query;
    }

    //ホームルーム出席履歴データ取得
    function getHrAttend($model) {
        $query  = " SELECT ";
        $query .= "     COUNT(DISTINCT EXECUTEDATE) ";
        $query .= " FROM ";
        $query .= "     HR_ATTEND_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR     = '".$model->exp_year."' AND ";
        $query .= "     SCHREGNO = '".$model->schregno."' ";

        return $query;
    }

    //署名チェック
    function getOpinionsWk($model)
    {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     ATTEST_OPINIONS_WK ";
        $query .= " WHERE ";
        $query .= "     YEAR     = '".$model->exp_year."' AND ";
        $query .= "     SCHREGNO = '".$model->schregno."' ";

        return $query;
    }

    //ハッシュ値取得
    function getOpinionsDat($model)
    {
        $query  = " SELECT ";
        $query .= "     T1.CHAGE_OPI_SEQ, ";
        $query .= "     T2.OPINION ";
        $query .= " FROM ";
        $query .= "     ATTEST_OPINIONS_WK T1, ";
        $query .= "     ATTEST_OPINIONS_DAT T2 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = T2.YEAR AND ";
        $query .= "     T1.YEAR = '".$model->exp_year."' AND ";
        $query .= "     T1.SCHREGNO = '".$model->schregno."' AND ";
        $query .= "     T1.CHAGE_OPI_SEQ = T2.SEQ ";

        return $query;
    }

    //学校種別取得
    function getSchoolKind($model) {

        $query  = " SELECT ";
        $query .= "     T2.SCHOOL_KIND ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1, ";
        $query .= "     SCHREG_REGD_GDAT T2 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR     = T2.YEAR AND ";
        $query .= "     T1.YEAR     = '$model->exp_year' AND ";
        $query .= "     T1.SEMESTER = '$model->exp_semester' AND ";
        $query .= "     T1.GRADE    = T2.GRADE AND ";
        $query .= "     T1.SCHREGNO = '$model->schregno' ";

        return $query;
    }

    //INSERT
    function &getInsertQuery($fields, $schregno, $exp_year, $exp_semester)
    {
        $db = Query::dbCheckOut();

        //年次の取得
        $query  = "SELECT ANNUAL FROM SCHREG_REGD_DAT ";
        $query .= " WHERE SCHREGNO = '".$schregno."'";
        $query .= "   AND YEAR = '".$exp_year."'";
        $query .= "   AND SEMESTER = '".$exp_semester."'";

        $data["YEAR"][TEXT]                 = $exp_year;
        $data["SCHREGNO"][TEXT]             = $schregno;
        $data["ANNUAL"][TEXT]               = $db->getOne($query);
        $data["TOTALSTUDYACT"][TEXT]        = $fields["TOTALSTUDYACT"];
        $data["TOTALSTUDYVAL"][TEXT]        = $fields["TOTALSTUDYVAL"];
        $data["SPECIALACTREMARK"][TEXT]     = $fields["SPECIALACTREMARK"];
        $data["TOTALREMARK"][TEXT]          = $fields["TOTALREMARK"];
        $data["ATTENDREC_REMARK"][TEXT]     = $fields["ATTENDREC_REMARK"];
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][FUNC]              = "sysdate()";

        $db->query(Query::insertSQL($data, "HTRAINREMARK_DAT"));

        Query::dbCheckIn($db);
        return ;
    }

    //DELETE
    function &getDeleteQuery($schregno, $exp_year)
    {
        $db = Query::dbCheckOut();

        $query  = " DELETE FROM HTRAINREMARK_DAT ";
        $query .= " WHERE YEAR = '".$exp_year."'";
        $query .= "   AND SCHREGNO = '".$schregno."'";

        $db->query($query);

        Query::dbCheckIn($db);
        return ;
    }

    //部活動参照
    function getClub($model) {
        $sdate = $model->exp_year . '-04-01';
        $edate = ((int)$model->exp_year + 1) . '-03-31';

        $query  = " SELECT ";
        $query .= "     T2.CLUBNAME, ";
        $query .= "     T1.SDATE, ";
        $query .= "     T1.EDATE, ";
        $query .= "     T3.NAME1 ";
        $query .= " FROM ";
        $query .= "     SCHREG_CLUB_HIST_DAT T1 ";
        $query .= " LEFT OUTER JOIN ";
        $query .= "     CLUB_MST T2 ON  T1.CLUBCD = T2.CLUBCD ";
        $query .= " LEFT OUTER JOIN ";
        $query .= "     NAME_MST T3 ON  T3.NAMECD1     = 'J001' ";
        $query .= "                 AND T1.EXECUTIVECD = T3.NAMECD2 ";
        $query .= " WHERE ";
        $query .= "     T1.SCHREGNO = '" .$model->schregno ."' AND ";
        $query .= "     ( ";
        $query .= "         (T1.SDATE                      < '{$sdate}' AND VALUE(T1.EDATE, '9999-12-31') > '{$edate}') OR ";
        $query .= "         (T1.SDATE                      > '{$sdate}' AND T1.SDATE                      < '{$edate}') OR ";
        $query .= "         (VALUE(T1.EDATE, '9999-12-31') > '{$sdate}' AND VALUE(T1.EDATE, '9999-12-31') < '{$edate}') ";
        $query .= "     ) ";
        $query .= " ORDER BY ";
        $query .= "     T1.SDATE, ";
        $query .= "     T1.CLUBCD ";

        return $query;
    }

    //委員会活動参照
    function getCommittee($model)
    {
        $query .= "SELECT T1.GRADE,T1.SEQ,T2.COMMITTEENAME,T1.CHARGENAME,T3.NAME1 ";
        $query .= "FROM SCHREG_COMMITTEE_HIST_DAT T1 ";
        $query .= "     LEFT OUTER JOIN COMMITTEE_MST T2 ON T1.COMMITTEE_FLG=T2.COMMITTEE_FLG AND T1.COMMITTEECD=T2.COMMITTEECD ";
        $query .= "     LEFT OUTER JOIN NAME_MST T3 ON T3.NAMECD1='J002' AND T1.EXECUTIVECD=T3.NAMECD2 ";
        $query .= "WHERE T1.YEAR='" .$model->exp_year."' AND ";
        $query .= "      T1.SCHREGNO='" .$model->schregno ."' ";
        $query .= "ORDER BY T1.GRADE,T1.SEQ ";

        return $query;
    }

    //学籍資格データよりデータを取得
    function getAward($model) {
        $sdate = $model->exp_year . '-04-01';
        $edate = ((int)$model->exp_year + 1) . '-03-31';

        if ($model->Properties["useQualifiedMst"] == '1') {
            $query  = " SELECT ";
            $query .= "     T1.YEAR, ";
            $query .= "     T1.SCHREGNO, ";
            $query .= "     T1.SEQ, ";
            $query .= "     T1.REGDDATE, ";
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "     T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD AS SUBCLASSCD, ";
            } else {
                $query .= "     T1.SUBCLASSCD, ";
            }
            $query .= "     CASE L1.CONDITION_DIV ";
            $query .= "          WHEN '1' ";
            $query .= "          THEN '国家資格' ";
            $query .= "          WHEN '2' ";
            $query .= "          THEN '公的資格' ";
            $query .= "          WHEN '3' ";
            $query .= "          THEN '民間資格' ";
            $query .= "          ELSE 'その他' ";
            $query .= "     END AS CONDITION_DIV, ";
            $query .= "     T1.QUALIFIED_CD, ";
            $query .= "     L1.QUALIFIED_NAME, ";
            $query .= "     L1.PROMOTER, ";
            $query .= "     T1.CONTENTS, ";
            $query .= "     L2.NAME1 AS RANK, ";
            $query .= "     T1.REMARK, ";
            $query .= "     T1.CREDITS ";
            $query .= " FROM ";
            $query .= "     SCHREG_QUALIFIED_HOBBY_DAT T1 ";
            $query .= " LEFT JOIN ";
            $query .= "     QUALIFIED_MST L1 ON L1.QUALIFIED_CD = T1.QUALIFIED_CD ";
            $query .= " LEFT JOIN ";
            $query .= "     NAME_MST L2 ON  L2.NAMECD2 = T1.RANK ";
            $query .= "                 AND L2.NAMECD1 = 'H312' ";
            $query .= " WHERE ";
            $query .= "     T1.SCHREGNO = '{$model->schregno}' AND ";
            $query .= "     T1.REGDDATE BETWEEN '{$sdate}' AND '{$edate}' ";
            $query .= " ORDER BY ";
            $query .= "     T1.REGDDATE, ";
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "     T1.CLASSCD, ";
                $query .= "     T1.SCHOOL_KIND, ";
                $query .= "     T1.CURRICULUM_CD, ";
            }
            $query .= "     T1.SUBCLASSCD, ";
            $query .= "     T1.SEQ ";
        } else {
            $query  = " SELECT ";
            $query .= "     T1.YEAR, ";
            $query .= "     T1.REGDDATE, ";
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "     T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD AS SUBCLASSCD, ";
            } else {
                $query .= "     T1.SUBCLASSCD, ";
            }
            $query .= "     CASE WHEN T1.CONDITION_DIV = '1' ";
            $query .= "          THEN T1.CONDITION_DIV || ':資格' ";
            $query .= "          ELSE T1.CONDITION_DIV || ':その他' ";
            $query .= "          END AS CONDITION_DIV, ";
            $query .= "     T1.SEQ, ";
            $query .= "     T1.CONTENTS, ";
            $query .= "     T1.REMARK, ";
            $query .= "     T1.CREDITS, ";
            $query .= "     T5.CREDITS AS CREDIT ";
            $query .= " FROM ";
            $query .= "     SCHREG_QUALIFIED_HOBBY_DAT T1 ";
            $query .= "       LEFT OUTER JOIN SUBCLASS_MST T2 ON T1.SUBCLASSCD = T2.SUBCLASSCD ";
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "                                  AND T1.CLASSCD         = T2.CLASSCD ";
                $query .= "                                  AND T1.SCHOOL_KIND     = T2.SCHOOL_KIND ";
                $query .= "                                  AND T1.CURRICULUM_CD   = T2.CURRICULUM_CD ";
            }
            $query .= "       LEFT JOIN SCHREG_REGD_DAT T4 ON T4.SCHREGNO = T1.SCHREGNO ";
            $query .= "                                   AND T4.YEAR     = T1.YEAR ";
            $query .= "                                   AND T4.SEMESTER = '".CTRL_SEMESTER."' ";
            $query .= "       LEFT JOIN CREDIT_MST T5 ON T5.YEAR       = T1.YEAR ";
            $query .= "                              AND T5.COURSECD   = T4.COURSECD ";
            $query .= "                              AND T5.MAJORCD    = T4.MAJORCD ";
            $query .= "                              AND T5.GRADE      = T4.GRADE ";
            $query .= "                              AND T5.COURSECODE = T4.COURSECODE ";
            $query .= "                              AND T5.CLASSCD    = SUBSTR(T1.SUBCLASSCD,1,2) ";
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "                          AND T5.SCHOOL_KIND     = T1.SCHOOL_KIND ";
                $query .= "                          AND T5.CURRICULUM_CD   = T1.CURRICULUM_CD ";
            }
            $query .= "                              AND T5.SUBCLASSCD = T1.SUBCLASSCD ";
            $query .= " WHERE ";
            $query .= "     T1.SCHREGNO = '{$model->schregno}' AND ";
            $query .= "     T1.REGDDATE BETWEEN '{$sdate}' AND '{$edate}' ";
            $query .= " ORDER BY ";
            $query .= "     T1.REGDDATE, ";
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "     T1.CLASSCD, ";
                $query .= "     T1.SCHOOL_KIND, ";
                $query .= "     T1.CURRICULUM_CD, ";
            }
            $query .= "     T1.SUBCLASSCD, ";
            $query .= "     T1.SEQ ";
        }

        return $query;
    }

    //年度コンボ
    function getHTrainRemarkYear($schregno) {
        $query  = " SELECT ";
        $query .= "     YEAR AS LABEL, ";
        $query .= "     YEAR AS VALUE ";
        $query .= " FROM ";
        $query .= "     HTRAINREMARK_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '".$schregno."' ";

        return $query;
    }

    //出欠の記録
    function getHExamEntRemark($schregno, $year) {
        $query .= " SELECT ";
        $query .= "     ATTENDREC_REMARK ";
        $query .= " FROM ";
        $query .= "     HEXAM_ENTREMARK_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$year."' AND ";
        $query .= "     SCHREGNO = '".$schregno."' ";

        return $query;
    }
}
?>
