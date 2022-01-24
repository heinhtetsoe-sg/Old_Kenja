<?php

require_once('for_php7.php');

class knja126pQuery extends Query {

    //１レコード取得
    function getTrainRow($model)
    {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     HTRAINREMARK_P_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".$model->exp_year."' AND ";
        $query .= "     SCHREGNO    = '".$model->schregno."' ";

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

    //年次の取得
    function getAnnual($model)
    {
        $query  = " SELECT ";
        $query .= "     ANNUAL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO    = '".$model->schregno."' AND ";
        $query .= "     YEAR        = '".$model->exp_year."' AND ";
        $query .= "     SEMESTER    = '".$model->exp_semester."' ";

        return $query;
    }

    //学年(GRADE_CD)の取得
    function getGradecd($model)
    {
        $query  = " SELECT ";
        $query .= "     T2.GRADE_CD ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1, ";
        $query .= "     SCHREG_REGD_GDAT T2 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR     = T2.YEAR AND ";
        $query .= "     T1.YEAR     = '$model->exp_year' AND ";
        $query .= "     T1.SEMESTER = '$model->exp_semester' AND ";
        $query .= "     T1.GRADE    = T2.GRADE AND ";
        $query .= "     T1.SCHREGNO = '$model->schregno' AND ";
        $query .= "     T2.SCHOOL_KIND = 'P' ";

        return $query;
    }

    //名称マスタ取得
    function getNameMst($model, $namecd1, $name1="")
    {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$model->exp_year."' ";
        $query .= "     AND NAMECD1 = '".$namecd1."' ";
        if ($name1) {
            $query .= "     AND NAME1 = '".$name1."' ";
        }

        return $query;
    }

    //INSERT
    function &getInsertQuery($model)
    {
        $db = Query::dbCheckOut();

        //年次の取得
        $annual = $db->getOne(knja126pQuery::getAnnual($model));

        //存在チェック
        $check = $db->getCol(knja126pQuery::getTrainRow($model));

        if(get_count($check) == 0) {
            $data["YEAR"][TEXT]                 = $model->exp_year;
            $data["SCHREGNO"][TEXT]             = $model->schregno;
            $data["ANNUAL"][TEXT]               = $annual;
            $data["TOTALSTUDYACT"][TEXT]        = $model->field["TOTALSTUDYACT"];
            $data["TOTALSTUDYVAL"][TEXT]        = $model->field["TOTALSTUDYVAL"];
            $data["TOTALREMARK"][TEXT]          = $model->field["TOTALREMARK"];
            $data["ATTENDREC_REMARK"][TEXT]     = $model->field["ATTENDREC_REMARK"];
            $data["VIEWREMARK"][TEXT]           = $model->field["VIEWREMARK"];
            $data["FOREIGNLANGACT1"][TEXT]      = $model->field["FOREIGNLANGACT1"];
            $data["FOREIGNLANGACT2"][TEXT]      = $model->field["FOREIGNLANGACT2"];
            $data["FOREIGNLANGACT3"][TEXT]      = $model->field["FOREIGNLANGACT3"];
            $data["REGISTERCD"][TEXT]           = STAFFCD;
            $data["UPDATED"][FUNC]              = "sysdate()";

            $db->query(Query::insertSQL($data, "HTRAINREMARK_P_DAT"));
        } else {
            $data["ANNUAL"][TEXT]               = $annual;
            $data["TOTALSTUDYACT"][TEXT]        = $model->field["TOTALSTUDYACT"];
            $data["TOTALSTUDYVAL"][TEXT]        = $model->field["TOTALSTUDYVAL"];
            $data["TOTALREMARK"][TEXT]          = $model->field["TOTALREMARK"];
            $data["ATTENDREC_REMARK"][TEXT]     = $model->field["ATTENDREC_REMARK"];
            $data["VIEWREMARK"][TEXT]           = $model->field["VIEWREMARK"];
            $data["FOREIGNLANGACT1"][TEXT]      = $model->field["FOREIGNLANGACT1"];
            $data["FOREIGNLANGACT2"][TEXT]      = $model->field["FOREIGNLANGACT2"];
            $data["FOREIGNLANGACT3"][TEXT]      = $model->field["FOREIGNLANGACT3"];
            $data["REGISTERCD"][TEXT]           = STAFFCD;
            $data["UPDATED"][FUNC]              = "sysdate()";

            $where  = " WHERE ";
            $where .= "     YEAR     = '".$model->exp_year."' AND ";
            $where .= "     SCHREGNO = '".$model->schregno."' ";

            $db->query(Query::updateSQL($data, "HTRAINREMARK_P_DAT", $where));
        }

        Query::dbCheckIn($db);
        return ;
    }

    //行動記録の取得
    function getBehavior($model)
    {
        $query  = " SELECT ";
        $query .= "     DIV, ";
        $query .= "     CODE, ";
        $query .= "     ANNUAL, ";
        $query .= "     RECORD ";
        $query .= " FROM ";
        $query .= "     BEHAVIOR_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".$model->exp_year."' AND ";
        $query .= "     SCHREGNO    = '".$model->schregno."' AND ";
        $query .= "     DIV IN ('3', '4') ";
        $query .= " ORDER BY ";
        $query .= "     DIV, ";
        $query .= "     CODE ";

        return $query;
    }

    //INSERT
    function &getInsertQuery2($model)
    {
        $db = Query::dbCheckOut();

        //年次の取得
        $annual = $db->getOne(knja126pQuery::getAnnual($model));

        //行動の記録・特別活動の記録（削除）
        $query  = " DELETE FROM ";
        $query .= "     BEHAVIOR_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".$model->exp_year."' AND ";
        $query .= "     SCHREGNO    = '".$model->schregno."' AND ";
        $query .= "     DIV IN ('3', '4') ";

        $db->query($query);

        //行動の記録・特別活動の記録（追加）
        for($j=3; $j<5; $j++)
        {
            $cnt = ($j == "3") ? "11" : "5";

            for($i=1; $i<$cnt; $i++)
            {
                $ival = $j . sprintf("%02d", $i);
                if ($model->record["RECORD"][$ival] != "1") continue;

                $data["YEAR"][TEXT]         = $model->exp_year;
                $data["SCHREGNO"][TEXT]     = $model->schregno;
                $data["DIV"][TEXT]          = $j;
                $data["CODE"][TEXT]         = sprintf("%02d", $i);
                $data["ANNUAL"][TEXT]       = $annual;
                $data["RECORD"][TEXT]       = "1";
                $data["REGISTERCD"][TEXT]   = STAFFCD;
                $data["UPDATED"][FUNC]      = "sysdate()";

                $db->query(Query::insertSQL($data, "BEHAVIOR_DAT"));
            }
        }

        //存在チェック
        $check = $db->getCol(knja126pQuery::getTrainRow($model));

        //特別活動の記録の観点
        if(get_count($check) == 0) {
            $data1["YEAR"][TEXT]                = $model->exp_year;
            $data1["SCHREGNO"][TEXT]            = $model->schregno;
            $data1["ANNUAL"][TEXT]              = $annual;
            $data1["SCHREGNO"][TEXT]            = $model->schregno;
            $data1["SPECIALACTREMARK"][TEXT]    = $model->field["SPECIALACTREMARK"];
            $data1["REGISTERCD"][TEXT]          = STAFFCD;
            $data1["UPDATED"][FUNC]             = "sysdate()";

            $db->query(Query::insertSQL($data1, "HTRAINREMARK_P_DAT"));
        } else {
            $data1["ANNUAL"][TEXT]              = $annual;
            $data1["SCHREGNO"][TEXT]            = $model->schregno;
            $data1["SPECIALACTREMARK"][TEXT]    = $model->field["SPECIALACTREMARK"];
            $data1["REGISTERCD"][TEXT]          = STAFFCD;
            $data1["UPDATED"][FUNC]             = "sysdate()";

            $where  = " WHERE ";
            $where .= "     YEAR     = '".$model->exp_year."' AND ";
            $where .= "     SCHREGNO = '".$model->schregno."' ";

            $db->query(Query::updateSQL($data1, "HTRAINREMARK_P_DAT", $where));
        }

        Query::dbCheckIn($db);
        return ;
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

    //出欠記録データ取得
    function getAttendRec($model) {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     SCHREG_ATTENDREC_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR     = '".$model->exp_year."' AND ";
        $query .= "     SCHREGNO = '".$model->schregno."' ";

        return $query;
    }

    //通知表所見参照
    function getHreportRemarkDat($model)
    {
        $query  = " SELECT  ";
        $query .= "     T1.SEMESTER, ";
        $query .= "     T1.SEMESTERNAME, ";
        $query .= "     T2.TOTALSTUDYTIME, ";
        $query .= "     T2.FOREIGNLANGACT, ";
        $query .= "     T2.COMMUNICATION, ";
        $query .= "     T2.SPECIALACTREMARK AS HREPORTSPECIALACTREMARK";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST T1 ";
        $query .= "     LEFT JOIN HREPORTREMARK_DAT T2 ON T1.YEAR       = T2.YEAR ";
        $query .= "                                   AND T1.SEMESTER   = T2.SEMESTER ";
        $query .= "                                   AND T2.SCHREGNO   = '" .$model->schregno ."' ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR     = '" .$model->exp_year ."' AND ";
        $query .= "     T1.SEMESTER <> '9' ";
        $query .= " ORDER BY ";
        $query .= "     T1.SEMESTER";

        return $query;
    }

    //学校種別取得
    function checkSchregRegdGdat($model)
    {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1, ";
        $query .= "     SCHREG_REGD_GDAT T2 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR         = T2.YEAR AND ";
        $query .= "     T1.YEAR         = '$model->exp_year' AND ";
        $query .= "     T1.SEMESTER     = '$model->exp_semester' AND ";
        $query .= "     T1.GRADE        = T2.GRADE AND ";
        $query .= "     T1.SCHREGNO     = '$model->schregno' AND ";
        $query .= "     T2.SCHOOL_KIND  = 'P' AND ";
        $query .= "     T2.GRADE_CD IN ('05', '06') ";

        return $query;
    }

    //行動の記録取得
    function getBehaviorSemesDat($model, $semester, $cd)
    {
        $query  = " SELECT ";
        $query .= "     CODE, ";
        $query .= "     RECORD ";
        $query .= " FROM ";
        $query .= "     BEHAVIOR_SEMES_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".$model->exp_year."' AND ";
        $query .= "     SCHREGNO    = '".$model->schregno."' AND ";
        $query .= "     SEMESTER    = '".$semester."' AND ";
        $query .= "     CODE        = '".$cd."' ";

        return $query;
    }

    //学校マスタの校種有無チェック
    function checkSchoolMst() {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     SYSIBM.SYSCOLUMNS ";
        $query .= " WHERE ";
        $query .= "     TBNAME  = 'SCHOOL_MST' AND ";
        $query .= "     NAME    = 'SCHOOL_KIND' ";

        return $query;
    }

    //出欠の記録参照
    function getAttendSemesDat($model, $semester, $knjSchoolMst)
    {
        $query  = " WITH ATTEND_SEM AS ( ";
        $query .= "     SELECT ";
        $query .= "         SEMESTER, ";
        $query .= "         SCHREGNO, ";
        if ($knjSchoolMst["SEM_OFFDAYS"] == "1") {
            $query .= "         SUM(VALUE(LESSON,0) - VALUE(ABROAD,0))  AS LESSON, ";
            $query .= "         SUM(VALUE(SICK,0) + VALUE(NOTICE,0) + VALUE(NONOTICE,0) + VALUE(OFFDAYS,0))  AS SICK, ";
        } else {
            $query .= "         SUM(VALUE(LESSON,0) - VALUE(ABROAD,0) - VALUE(OFFDAYS,0))  AS LESSON, ";
            $query .= "         SUM(VALUE(SICK,0) + VALUE(NOTICE,0) + VALUE(NONOTICE,0))  AS SICK, ";
        }
        $query .= "         SUM(VALUE(SUSPEND,0) ";
        if ($model->Properties["useVirus"] == "true") {
            $query .= "             + VALUE(VIRUS,0) ";
        }
        if ($model->Properties["useKoudome"] == "true") {
            $query .= "             + VALUE(KOUDOME,0) ";
        }
        $query .= "             + VALUE(MOURNING,0))  AS SUSPEND ";
        $query .= "     FROM ";
        $query .= "         ATTEND_SEMES_DAT ";
        $query .= "     WHERE ";
        $query .= "         YEAR        = '".$model->exp_year."' AND ";
        $query .= "         SCHREGNO    = '".$model->schregno."' AND ";
        $query .= "         SEMESTER    = '".$semester."' ";
        $query .= "     GROUP BY ";
        $query .= "         SEMESTER, ";
        $query .= "         SCHREGNO ";
        $query .= " ) ";
        $query .= " , ATTEND_REMARK AS ( ";
        $query .= "     SELECT ";
        $query .= "         SEMESTER, ";
        $query .= "         SCHREGNO, ";
        $query .= "         ATTENDREC_REMARK ";
        $query .= "     FROM ";
        $query .= "         HREPORTREMARK_DAT ";
        $query .= "     WHERE ";
        $query .= "         YEAR        = '".$model->exp_year."' AND ";
        $query .= "         SCHREGNO    = '".$model->schregno."' AND ";
        $query .= "         SEMESTER    = '".$semester."' ";
        $query .= " ) ";
        $query .= " , MAIN AS ( ";
        $query .= "     SELECT ";
        $query .= "         T1.SEMESTER, ";
        $query .= "         T1.SCHREGNO, ";
        $query .= "         T1.LESSON, ";
        $query .= "         T1.SUSPEND, ";
        $query .= "         VALUE(T1.LESSON,0) - VALUE(T1.SUSPEND,0) AS ATTEND1, ";
        $query .= "         T1.SICK, ";
        $query .= "         VALUE(T1.LESSON,0) - VALUE(T1.SUSPEND,0) - VALUE(T1.SICK,0) AS ATTEND2, ";
        $query .= "         T2.ATTENDREC_REMARK ";
        $query .= "     FROM ";
        $query .= "         ATTEND_SEM T1, ";
        $query .= "         ATTEND_REMARK T2 ";
        $query .= "     WHERE ";
        $query .= "         T1.SEMESTER = T2.SEMESTER AND ";
        $query .= "         T1.SCHREGNO = T2.SCHREGNO ";
        $query .= " ) ";

        $query .= "     SELECT ";
        $query .= "         T1.SEMESTER, ";
        $query .= "         T2.SCHREGNO, ";
        $query .= "         T2.LESSON, ";
        $query .= "         T2.SUSPEND, ";
        $query .= "         T2.ATTEND1, ";
        $query .= "         T2.SICK, ";
        $query .= "         T2.ATTEND2, ";
        $query .= "         T2.ATTENDREC_REMARK ";
        $query .= "     FROM ";
        $query .= "         SEMESTER_MST T1 ";
        $query .= "         LEFT JOIN MAIN T2 ON T1.SEMESTER = T2.SEMESTER ";
        $query .= "     WHERE ";
        $query .= "         T1.YEAR     = '".$model->exp_year."' AND ";
        $query .= "         T1.SEMESTER = '".$semester."' ";

        return $query;
    }
}
?>
