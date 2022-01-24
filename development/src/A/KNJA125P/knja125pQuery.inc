<?php

require_once('for_php7.php');

class knja125pQuery extends Query
{

    //１レコード取得
    public function getTrainRow($model)
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

    //年次の取得
    public function getAnnual($model)
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
    public function getGradecd($model)
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

    //学年(GRADE_CD)の取得
    public function getGradecd2($model)
    {
        $query  = " SELECT ";
        $query .= "     T2.GRADE_CD ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT T2 ";
        $query .= " WHERE ";
        $query .= "     T2.YEAR     = '{$model->exp_year}' AND ";
        $query .= "     T2.GRADE    = '{$model->grade}' ";

        return $query;
    }

    //学校種別取得
    public function getSchoolKind($model)
    {

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

    //名称マスタ取得
    public function getNameMst($model, $namecd1, $name1 = "")
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

    //観点マスタ
    public function getBehaviorSemesMst($model)
    {
        $query  = " SELECT ";
        $query .= "     CODE AS NAMECD2, ";
        $query .= "     VIEWNAME AS NAME1 ";
        $query .= " FROM ";
        $query .= "     BEHAVIOR_SEMES_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR    = '".$model->exp_year."' AND ";
        $query .= "     GRADE   = '".$model->grade_cd."' ";
        $query .= " ORDER BY ";
        $query .= "     CODE ";

        return $query;
    }

    //INSERT
    public function &getInsertQuery($model)
    {
        $db = Query::dbCheckOut();

        //年次の取得
        $annual = $db->getOne(knja125pQuery::getAnnual($model));

        //存在チェック
        $check = $db->getCol(knja125pQuery::getTrainRow($model));

        $data = array();
        $data["ANNUAL"][TEXT]               = $annual;
        $data["TOTALSTUDYACT"][TEXT]        = $model->field["TOTALSTUDYACT"];
        $data["TOTALSTUDYVAL"][TEXT]        = $model->field["TOTALSTUDYVAL"];
        $data["TOTALREMARK"][TEXT]          = $model->field["TOTALREMARK"];
        $data["ATTENDREC_REMARK"][TEXT]     = $model->field["ATTENDREC_REMARK"];
        $data["VIEWREMARK"][TEXT]           = $model->field["VIEWREMARK"];
        if ($model->is_gaikokugo1) {
            $data["FOREIGNLANGACT1"][TEXT]      = $model->field["FOREIGNLANGACT1_2"];
        } else {
            $data["FOREIGNLANGACT1"][TEXT]      = $model->field["FOREIGNLANGACT1"];
            $data["FOREIGNLANGACT2"][TEXT]      = $model->field["FOREIGNLANGACT2"];
            $data["FOREIGNLANGACT3"][TEXT]      = $model->field["FOREIGNLANGACT3"];
        }
        $data["FOREIGNLANGACT4"][TEXT]      = $model->field["FOREIGNLANGACT4"];
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][FUNC]              = "sysdate()";
        if (get_count($check) == 0) {
            $data["YEAR"][TEXT]                 = $model->exp_year;
            $data["SCHREGNO"][TEXT]             = $model->schregno;

            $db->query(Query::insertSQL($data, "HTRAINREMARK_P_DAT"));
        } else {
            $where  = " WHERE ";
            $where .= "     YEAR     = '".$model->exp_year."' AND ";
            $where .= "     SCHREGNO = '".$model->schregno."' ";

            $db->query(Query::updateSQL($data, "HTRAINREMARK_P_DAT", $where));
        }

        Query::dbCheckIn($db);
        return ;
    }

    //行動記録の取得
    public function getBehavior($model)
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
        $query .= "     SCHREGNO    = '".$model->schregno."' ";
        $query .= " ORDER BY ";
        $query .= "     DIV, ";
        $query .= "     CODE ";

        return $query;
    }

    //INSERT
    public function &getInsertQuery2($model)
    {
        $db = Query::dbCheckOut();

        //BEHAVIOR_DAT削除
        $query  = " DELETE FROM ";
        $query .= "     BEHAVIOR_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".$model->exp_year."' AND ";
        $query .= "     SCHREGNO    = '".$model->schregno."' AND ";
        $query .= "     DIV IN ('3', '4') ";

        $db->query($query);

        //年次の取得
        $annual = $db->getOne(knja125pQuery::getAnnual($model));

        //BEHAVIOR_DAT追加
        for ($j=3; $j<5; $j++) {
            $cnt = ($j == "3") ? "11" : "5";

            for ($i=1; $i<$cnt; $i++) {
                $ival = $j . sprintf("%02d", $i);
                if ($model->record["RECORD"][$ival] != "1") {
                    continue;
                }

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
        $check = $db->getCol(knja125pQuery::getTrainRow($model));

        //特別活動の記録の観点
        if (get_count($check) == 0) {
            $data1["YEAR"][TEXT]                = $model->exp_year;
            $data1["SCHREGNO"][TEXT]            = $model->schregno;
            $data1["ANNUAL"][TEXT]              = $annual;
            $data1["SPECIALACTREMARK"][TEXT]    = $model->field["SPECIALACTREMARK"];
            $data1["REGISTERCD"][TEXT]          = STAFFCD;
            $data1["UPDATED"][FUNC]             = "sysdate()";

            $db->query(Query::insertSQL($data1, "HTRAINREMARK_P_DAT"));
        } else {
            $data1["ANNUAL"][TEXT]              = $annual;
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

    /************************/
    /* 通知表所見参照ボタン */
    /************************/
    //通知表所見参照
    public function getHreportRemarkDat($model)
    {
        $query  = " SELECT  ";
        $query .= "     T2.SEMESTER, ";
        $query .= "     L1.SEMESTERNAME, ";
        $query .= "     T2.REMARK3, ";
        $query .= "     T2.TOTALSTUDYTIME, ";
        $query .= "     T2.FOREIGNLANGACT, ";
        $query .= "     T2.COMMUNICATION, ";
        $query .= "     T2.SPECIALACTREMARK AS HREPORTSPECIALACTREMARK ";
        $query .= " FROM ";
        $query .= "     HREPORTREMARK_DAT T2 ";
        $query .= "     LEFT JOIN SEMESTER_MST L1 ON L1.YEAR = T2.YEAR ";
        $query .= "                              AND L1.SEMESTER   = T2.SEMESTER ";
        $query .= " WHERE ";
        $query .= "     T2.YEAR     = '" .$model->exp_year ."' ";
        $query .= " AND T2.SCHREGNO = '" .$model->schregno ."' ";
        $query .= " ORDER BY ";
        $query .= "     T2.SEMESTER";

        return $query;
    }

    //通知表所見参照（特別活動・クラブ活動）
    public function getHreportRemarkDetailDat($model)
    {
        $query  = " SELECT  ";
        $query .= "     REMARK1 as REMARK1_02_01, ";
        $query .= "     REMARK2 as REMARK2_02_01, ";
        $query .= "     REMARK3 as REMARK3_02_01 ";
        $query .= " FROM ";
        $query .= "     HREPORTREMARK_DETAIL_DAT ";
        $query .= " WHERE ";
        $query .= "         YEAR     = '" .$model->exp_year ."' ";
        $query .= "     AND SEMESTER = '9' "; //固定
        $query .= "     AND SCHREGNO = '" .$model->schregno ."' ";
        $query .= "     AND DIV      = '02' ";
        $query .= "     AND CODE     = '01' ";

        return $query;
    }

    //通知表所見参照（特記事項・担任からの通信）
    public function getHreportRemarkDat2($model)
    {
        $query  = " SELECT  ";
        $query .= "     S1.SEMESTER, ";
        $query .= "     S1.SEMESTERNAME, ";
        $query .= "     D2.REMARK3 AS REMARK_TCTTL, ";
        $query .= "     H1.COMMUNICATION ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST S1 ";
        $query .= "     LEFT JOIN HREPORTREMARK_DAT H1 ON H1.YEAR     = S1.YEAR ";
        $query .= "                                   AND H1.SEMESTER = S1.SEMESTER ";
        $query .= "                                   AND H1.SCHREGNO = '{$model->schregno}' ";
        $query .= "     LEFT JOIN HREPORTREMARK_DETAIL_DAT D2 ON D2.YEAR     = S1.YEAR ";
        $query .= "                                          AND D2.SEMESTER = S1.SEMESTER ";
        $query .= "                                          AND D2.SCHREGNO = H1.SCHREGNO";
        $query .= "                                          AND D2.DIV      = '01' ";
        $query .= "                                          AND D2.CODE     = '01' ";
        $query .= " WHERE ";
        $query .= "         S1.YEAR      = '{$model->exp_year}' ";
        $query .= "     AND S1.SEMESTER  = '9' ";
        $query .= " ORDER BY ";
        $query .= "     S1.SEMESTER ";

        return $query;
    }

    //統合的な学習
    public function getTotalStudyText($model)
    {
        $query  = " SELECT ";
        $query .= "     S1.SEMESTER, ";
        $query .= "     S1.SEMESTERNAME, ";
        $query .= "     D1.TOTALSTUDYTIME AS TEXT01, ";
        $query .= "     D2.REMARK1 AS TITLE";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST S1 ";
        $query .= "     LEFT JOIN HREPORTREMARK_DAT D1 ON D1.YEAR     = S1.YEAR ";
        $query .= "                                   AND D1.SEMESTER = S1.SEMESTER ";
        $query .= "                                   AND D1.SCHREGNO = '{$model->schregno}' ";
        $query .= "     LEFT JOIN HREPORTREMARK_DETAIL_DAT D2 ON D2.YEAR     = D1.YEAR ";
        $query .= "                                          AND D2.SEMESTER = D1.SEMESTER ";
        $query .= "                                          AND D2.SCHREGNO = D1.SCHREGNO";
        $query .= "                                          AND D2.DIV      = '01' ";
        $query .= "                                          AND D2.CODE     = '01' ";
        $query .= " WHERE ";
        $query .= "         S1.YEAR     = '{$model->exp_year}' ";
        $query .= "     AND S1.SEMESTER <> '9' ";
        $query .= " ORDER BY ";
        $query .= "     S1.SEMESTER ";

        return $query;
    }

    //道徳
    public function getMoralText($model)
    {
        $query  = " SELECT ";
        $query .= "     S1.SEMESTER, ";
        $query .= "     S1.SEMESTERNAME, ";
        $query .= "     D1.REMARK1 AS TEXT01, ";
        $query .= "     D2.REMARK2 AS TITLE ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST S1 ";
        $query .= "     LEFT JOIN HREPORTREMARK_DAT D1 ON D1.YEAR     = S1.YEAR ";
        $query .= "                                   AND D1.SEMESTER = S1.SEMESTER ";
        $query .= "                                   AND D1.SCHREGNO = '{$model->schregno}' ";
        $query .= "     LEFT JOIN HREPORTREMARK_DETAIL_DAT D2 ON D2.YEAR     = D1.YEAR ";
        $query .= "                                          AND D2.SEMESTER = D1.SEMESTER ";
        $query .= "                                          AND D2.SCHREGNO = D1.SCHREGNO";
        $query .= "                                          AND D2.DIV      = '01' ";
        $query .= "                                          AND D2.CODE     = '01' ";
        $query .= " WHERE ";
        $query .= "         S1.YEAR     = '{$model->exp_year}' ";
        $query .= "     AND S1.SEMESTER <> '9' ";
        $query .= " ORDER BY ";
        $query .= "     S1.SEMESTER ";

        return $query;
    }

    //特別活動・クラブ活動
    public function getActClubText($model)
    {
        $query  = " SELECT ";
        $query .= "     D2.REMARK1 AS TEXT01, ";
        $query .= "     D2.REMARK2 AS TEXT02, ";
        $query .= "     D2.REMARK3 AS TEXT03 ";
        $query .= " FROM ";
        $query .= "     HREPORTREMARK_DETAIL_DAT D2 ";
        $query .= " WHERE ";
        $query .= "         D2.YEAR     = '{$model->exp_year}' ";
        $query .= "     AND D2.SEMESTER = '9' ";
        $query .= "     AND D2.SCHREGNO = '{$model->schregno}' ";
        $query .= "     AND D2.DIV      = '02' ";
        $query .= "     AND D2.CODE     = '01' ";

        return $query;
    }

    //年次取得
    public function getGradeCdSubform1($model)
    {
        $query  = " SELECT ";
        $query .= "     GRADE_CD ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR  = '".$model->exp_year."' ";
        $query .= " AND GRADE = '".$model->grade."' ";

        return $query;
    }
    /*************/

    //学校チェック
    public function getZ010()
    {
        $query  = " SELECT ";
        $query .= "     NAME1 ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'Z010' ";
        $query .= " AND NAMECD2 = '00' ";

        return $query;
    }

    //学校種別取得
    public function checkSchregRegdGdat($model)
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
    public function getBehaviorSemesDat($model, $semester, $cd)
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
    public function checkSchoolMst()
    {
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
    public function getAttendSemesDat($model, $semester, $knjSchoolMst)
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
        $query .= "         SUM(VALUE(LATE,0)) as LATE, ";
        $query .= "         SUM(VALUE(EARLY,0)) as EARLY, ";
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
        $query .= "         T1.LATE, ";
        $query .= "         T1.EARLY, ";
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
        $query .= "         T2.LATE, ";
        $query .= "         T2.EARLY, ";
        $query .= "         T2.ATTENDREC_REMARK ";
        $query .= "     FROM ";
        $query .= "         SEMESTER_MST T1 ";
        $query .= "         LEFT JOIN MAIN T2 ON T1.SEMESTER = T2.SEMESTER ";
        $query .= "     WHERE ";
        $query .= "         T1.YEAR     = '".$model->exp_year."' AND ";
        $query .= "         T1.SEMESTER = '".$semester."' ";

        return $query;
    }

    //学年名取得
    public function getGradeName($model)
    {
        $query  = " SELECT ";
        $query .= "     GRADE_NAME1 ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".$model->exp_year."' AND ";
        $query .= "     GRADE       = '".$model->grade."' ";

        return $query;
    }

    //定型文学期マスター一覧取得
    public function getHtrainRemarkTempSemesDat($model)
    {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     HTRAINREMARK_TEMP_SEMES_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".$model->exp_year."' AND ";
        $query .= "     SEMESTER    = '9' AND ";
        $query .= "     GRADE       = '".$model->grade."' AND ";
        $query .= "     DATA_DIV    = '03' ";
        $query .= " ORDER BY ";
        $query .= "     PATTERN_CD ";

        return $query;
    }
}
