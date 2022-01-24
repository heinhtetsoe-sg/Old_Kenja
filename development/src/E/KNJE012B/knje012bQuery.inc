<?php

require_once('for_php7.php');

class knje012bQuery extends Query
{
    //１レコード取得
    public function getTrainRow($model, $flg)
    {
        $query  = " SELECT ";
        $query .= "   T1.*, ";
        $query .= "   L1.TRAIN_REF1, ";
        $query .= "   L1.TRAIN_REF2, ";
        $query .= "   L1.TRAIN_REF3, ";
        $query .= "   L2.REMARK1, ";
        $query .= "   L3.REMARK1 AS DEVIATION, ";
        $query .= "   L4.REMARK1 AS REMARK1_008, ";
        $query .= "   L5.SPECIALACTREC, ";
        $query .= "   L5.CLUBACT ";
        $query .= " FROM ";
        $query .= "     HTRAINREMARK_DAT T1 ";
        $query .= "     LEFT JOIN HTRAINREMARK_DETAIL_DAT L1 ON T1.YEAR = L1.YEAR ";
        $query .= "          AND T1.SCHREGNO = L1.SCHREGNO ";
        $query .= "     LEFT JOIN HTRAINREMARK_DETAIL2_DAT L2 ON T1.YEAR = L2.YEAR ";
        $query .= "          AND T1.SCHREGNO   = L2.SCHREGNO ";
        $query .= "          AND L2.HTRAIN_SEQ = '004' ";
        $query .= "     LEFT JOIN HTRAINREMARK_DETAIL2_DAT L3 ON T1.YEAR = L3.YEAR ";
        $query .= "          AND T1.SCHREGNO   = L3.SCHREGNO ";
        $query .= "          AND L3.HTRAIN_SEQ = '007' ";
        $query .= "     LEFT JOIN HTRAINREMARK_DETAIL2_DAT L4 ON T1.YEAR = L4.YEAR ";
        $query .= "          AND T1.SCHREGNO   = L4.SCHREGNO ";
        $query .= "          AND L4.HTRAIN_SEQ = '008' ";
        $query .= "     LEFT JOIN HEXAM_ENTREMARK_DAT L5 ON T1.YEAR = L5.YEAR ";
        $query .= "          AND T1.SCHREGNO   = L5.SCHREGNO ";
        $query .= " WHERE ";
        if ($flg === 'sanshou') {
            $query .= "     T1.YEAR     < '".$model->exp_year."' AND ";
        } else {
            $query .= "     T1.YEAR     = '".$model->exp_year."' AND ";
        }
        $query .= "     T1.SCHREGNO = '".$model->schregno."' ";
        if ($flg === 'sanshou') {
            $query .= " ORDER BY ";
            $query .= "     T1.YEAR ";
        }

        return $query;
    }
    //調査書所見データ
    public function selectQuery($model, $annual = '')
    {
        $query  = " SELECT ";
        $query .= "   T1.SCHREGNO, ";
        $query .= "   T1.ATTENDREC_REMARK, ";
        $query .= "   T1.SPECIALACTREC, ";
        $query .= "   T1.CLUBACT, ";
        $query .= "   T1.STUDENTACT, ";
        $query .= "   T1.CALSSACT, ";
        $query .= "   T1.SCHOOLEVENT, ";
        $query .= "   T2.REMARK AS SOUGOUSYOKEN, ";
        $query .= "   T2.REMARK2 AS ZITUYOUSIKEN, ";
        $query .= "   T3.TOTALREMARK AS SOUGOUSYOKENSANSYOU ";
        $query .= " FROM ";
        $query .= "   HEXAM_ENTREMARK_DAT T1";
        $query .= " LEFT JOIN ";
        $query .= "   HEXAM_ENTREMARK_HDAT T2 ";
        $query .= "   ON T1.SCHREGNO = T2.SCHREGNO ";
        $query .= " LEFT JOIN ";
        $query .= "   HTRAINREMARK_DAT T3 ";
        $query .= "   ON T1.SCHREGNO = T3.SCHREGNO ";
        $query .= "   AND T1.YEAR = T3.YEAR ";
        $query .= " WHERE ";
        $query .= "       T1.YEAR     = '{$model->annual["YEAR"]}' ";
        $query .= "   AND T1.SCHREGNO = '{$model->schregno}' ";
        if ($annual != '') {
            $query .= "   AND T1.ANNUAL = '{$annual}' ";
        }
        return $query;
    }
    //特別活動の記録
    public function selectQuery2($model)
    {
        $query  = " SELECT ";
        $query .= "   T1.NAME1 AS NAME, ";
        $query .= "   T1.NAMECD2 AS CODE, ";
        $query .= "   T2.SCHREGNO, ";
        $query .= "   T2.RECORD ";
        $query .= " FROM ";
        $query .= "   NAME_MST T1 ";
        $query .= " LEFT JOIN ";
        $query .= "   BEHAVIOR_DAT T2 ";
        $query .= "   ON T1.NAMECD2 = T2.CODE ";
        $query .= "   AND T2.YEAR     = '{$model->annual["YEAR"]}' ";
        $query .= "   AND T2.SCHREGNO = '{$model->schregno}' ";
        $query .= "   AND T2.DIV = '2' ";
        $query .= "   AND T2.ANNUAL = '{$model->grade}' ";
        $query .= " WHERE ";
        $query .= "   T1.NAMECD1 = 'D034' ";
        $query .= " ORDER BY ";
        $query .= "     T1.NAMECD2 ";

        return $query;
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
        $query .= "     YEAR        = '{$model->annual["YEAR"]}' AND ";
        $query .= "     SCHREGNO    = '{$model->schregno}' AND ";
        $query .= "     DIV         = '2' ";
        $query .= " ORDER BY ";
        $query .= "     DIV, ";
        $query .= "     CODE ";

        return $query;
    }

    //「出欠の記録～」の学年（年次）コンボボックス
    public function getYearAnnual($model)
    {
        $query  = " WITH SUB_MAIN AS ( ";
        $query .= " SELECT DISTINCT ";
        $query .= "     T1.YEAR, ";
        $query .= "     T1.ANNUAL, ";
        $query .= "     T1.SCHREGNO ";
        $query .= " FROM ";
        $query .= "     SCHREG_STUDYREC_DAT T1 ";
        $query .= " UNION ";
        $query .= " SELECT DISTINCT ";
        $query .= "     T2.YEAR, ";
        $query .= "     T2.ANNUAL, ";
        $query .= "     T2.SCHREGNO ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T2 ";
        $query .= " ) ";
        $query .= " SELECT DISTINCT ";
        $query .= "     YEAR, ";
        $query .= "     ANNUAL ";
        $query .= " FROM ";
        $query .= "     SUB_MAIN ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '{$model->schregno}' ";
        $query .= " ORDER BY ";
        $query .= "     YEAR, ";
        $query .= "     ANNUAL ";

        return $query;
    }

    //通知表所見より年度を取得
    public function getHreportremarkYear($model)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     YEAR ";
        $query .= " FROM ";
        $query .= "     HREPORTREMARK_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '".$model->schregno ."' ";
        $query .= " ORDER BY ";
        $query .= "     YEAR DESC ";

        return $query;
    }

    //通知表所見データ取得
    public function getHreportremarkDat($model)
    {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     HREPORTREMARK_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR     = '".$model->year_cmb."' AND ";
        $query .= "     SCHREGNO = '".$model->schregno."' ";
        $query .= " ORDER BY ";
        $query .= "     SEMESTER ";

        return $query;
    }

    //学期取得
    public function getSemesterMst($model)
    {
        $query  = " SELECT ";
        $query .= "     SEMESTER ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR     = '".$model->year_cmb."' AND ";
        $query .= "     SEMESTER <> '9' ";

        return $query;
    }

    //項目取得
    public function getNameMst($model)
    {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR    = '".$model->year_cmb."' AND ";
        $query .= "     NAMECD1 = 'D034' ";

        return $query;
    }

    //通知表所見詳細データ取得
    public function getHreportremarkDetailDat($model, $flg = "")
    {
        $query  = " SELECT ";
        $query .= "     CODE, ";
        $query .= "     REMARK1 ";
        $query .= " FROM ";
        $query .= "     HREPORTREMARK_DETAIL_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".$model->year_cmb."' AND ";
        $query .= "     SEMESTER    = '9' AND ";
        $query .= "     SCHREGNO    = '".$model->schregno."' AND ";
        if ($flg) {
            $query .= "     DIV     = '03' AND ";
            $query .= "     CODE IN ('03', '04') ";
        } else {
            $query .= "     DIV     = '01' ";
        }
        $query .= " ORDER BY ";
        $query .= "     CODE ";

        return $query;
    }

    //クラブ情報
    public function getClubData($model)
    {
        $year = $model->annual["YEAR"];
        $next = $year + 1;

        $query  = " SELECT ";
        $query .= "     CLUBNAME, ";
        $query .= "     T3.NAME1 AS EXECUTIVE_SHOW, ";
        $query .= "     VALUE(T2.CLUBNAME,'') || ' ' || VALUE(T3.NAME1,'') AS DATA ";
        $query .= " FROM ";
        $query .= "     SCHREG_CLUB_HIST_DAT T1 ";
        $query .= " LEFT JOIN ";
        $query .= "     CLUB_MST T2 ";
        $query .= "     ON T1.CLUBCD = T2.CLUBCD ";
        $query .= "     AND T1.SCHOOL_KIND = T2.SCHOOL_KIND ";
        $query .= "     AND T1.SCHOOLCD = T2.SCHOOLCD ";
        $query .= " LEFT OUTER JOIN ";
        $query .= "     NAME_MST T3 ON  T3.NAMECD1     = 'J001' ";
        $query .= "                 AND T1.EXECUTIVECD = T3.NAMECD2 ";
        $query .= " WHERE ";
        $query .= "     T1.SCHREGNO = '{$model->schregno}' ";
        $query .= "     AND T1.SCHOOL_KIND = 'J' ";
        $query .= "     AND T1.SDATE >= '{$year}-04-01' ";
        $query .= "     AND T1.SDATE <= '{$next}-03-31' ";
        $query .= "     AND T1.SCHOOLCD <= '".SCHOOLCD."' ";

        return $query;
    }

    //クラブ詳細情
    public function getClubDataDetail($model)
    {
        $year = $model->annual["YEAR"];
        $next = $year + 1;

        $query .= " SELECT ";
        $query .= "     VALUE(T2.CLUBNAME,'') || ' ' || VALUE(T1.MEET_NAME,'') || ' ' || VALUE(T1.RECORDCD,'') AS DATA ";
        $query .= " FROM ";
        $query .= "     SCHREG_CLUB_HDETAIL_DAT T1 ";
        $query .= " LEFT JOIN ";
        $query .= "     CLUB_MST T2 ";
        $query .= "     ON T1.CLUBCD = T2.CLUBCD ";
        $query .= "     AND T1.SCHOOL_KIND = T2.SCHOOL_KIND ";
        $query .= "     AND T1.SCHOOLCD = T2.SCHOOLCD ";
        $query .= " WHERE ";
        $query .= "     T1.SCHREGNO = '{$model->schregno}' ";
        $query .= "     AND T1.SCHOOL_KIND = 'J' ";
        $query .= "     AND T1.DETAIL_DATE >= '{$year}-04-01' ";
        $query .= "     AND T1.DETAIL_DATE  <= '{$next}-03-31' ";
        $query .= "     AND T1.SCHOOLCD <= '".SCHOOLCD."' ";

        return $query;
    }

    //皆勤情報
    public function getKaikinData($model)
    {
        $query .= " SELECT ";
        $query .= "     CASE WHEN KAIKIN_DIV ='1' THEN '皆勤' WHEN KAIKIN_DIV ='2' THEN '精勤' ELSE '' END AS KAIKIN ";
        $query .= " FROM ";
        $query .= "     KAIKIN_DAT T1 ";
        $query .= " LEFT JOIN KAIKIN_MST T2 ";
        $query .= "     ON T1.KAIKIN_CD = T2.KAIKIN_CD ";
        $query .= "     AND T2.REF_YEAR = '1' ";
        $query .= " WHERE ";
        $query .= "     (T1.KAIKIN_FLG='1' ";
        $query .= "     OR T1.KAIKIN_FLG IS NULL) ";
        $query .= "     AND T1.SCHREGNO = '{$model->schregno}' ";

        return $query;
    }

    //委員会活動参照
    public function getCommittee($model)
    {
        $query  = " SELECT ";
        $query .= "     VALUE(L3.NAME1,'') || ' ' || VALUE(L1.COMMITTEENAME,'') || ' ' || VALUE(T1.CHARGENAME ,'') || ' ' || VALUE(L2.NAME1 ,'') AS DATA ";
        $query .= " FROM ";
        $query .= "     SCHREG_COMMITTEE_HIST_DAT T1 ";
        $query .= "     INNER JOIN SCHREG_REGD_GDAT G1 ";
        $query .= "          ON T1.YEAR         = G1.YEAR ";
        $query .= "         AND T1.GRADE        = G1.GRADE ";
        if ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "     AND T1.SCHOOL_KIND  = G1.SCHOOL_KIND ";
        }
        $query .= "     LEFT JOIN COMMITTEE_MST L1 ";
        $query .= "          ON T1.COMMITTEE_FLG    = L1.COMMITTEE_FLG ";
        $query .= "         AND T1.COMMITTEECD      = L1.COMMITTEECD ";
        if ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "     AND T1.SCHOOLCD         = L1.SCHOOLCD ";
            $query .= "     AND T1.SCHOOL_KIND      = L1.SCHOOL_KIND ";
        }
        $query .= "     LEFT JOIN NAME_MST L2 ";
        $query .= "          ON L2.NAMECD1      = 'J002' ";
        $query .= "         AND T1.EXECUTIVECD  = L2.NAMECD2 ";
        $query .= "     LEFT JOIN NAME_MST L3 ";
        $query .= "          ON L3.NAMECD1      = 'J004' ";
        $query .= "         AND T1.SEMESTER     = L3.NAMECD2 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".$model->annual["YEAR"]."' AND ";
        $query .= "     T1.SCHOOL_KIND = 'J' AND ";
        $query .= "     T1.SCHREGNO     = '".$model->schregno."' ";
        if ($model->Properties["useSchool_KindField"] == "1") {
            $query .= " AND T1.SCHOOLCD     = '".sprintf("%012d", SCHOOLCD)."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.SEMESTER, ";
        $query .= "     T1.SEQ ";

        return $query;
    }

    //学籍資格データよりデータを取得
    public function getAward($model, $db)
    {
        $year = $model->annual["YEAR"];

        $query  = "";
        if ($model->Properties["useQualifiedMst"] == '1') {
            $query .= " SELECT ";
            $query .= "     T1.SEQ, ";
            $query .= "     T1.REGDDATE, ";
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "     T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD AS SUBCLASSCD, ";
            } else {
                $query .= "     T1.SUBCLASSCD, ";
            }
            $query .= "     VALUE(L1.QUALIFIED_NAME,'') || ' ' || VALUE(T1.REMARK,'') AS DATA ";
            $query .= " FROM ";
            $query .= "     SCHREG_QUALIFIED_HOBBY_DAT T1 ";
            $query .= " LEFT JOIN ";
            $query .= "     QUALIFIED_MST L1 ON L1.QUALIFIED_CD = T1.QUALIFIED_CD ";
            $query .= " LEFT JOIN ";
            $query .= "     NAME_MST L2 ON  L2.NAMECD2 = T1.RANK ";
            $query .= "                 AND L2.NAMECD1 = 'H312' ";
            if (0 < $db->getOne(knje012bQuery::getSyscatColumns("QUALIFIED_RANK_DAT"))) {
                $query .= " INNER JOIN ";
                $query .= "     QUALIFIED_RANK_DAT L3 ON L3.QUALIFIED_CD = T1.QUALIFIED_CD ";
                $query .= "                          AND L3.RANK = T1.RANK ";
                $query .= "                          AND L3.NOT_PRINT IS NULL ";
            }
            $query .= " WHERE ";
            $query .= "     T1.YEAR     = '{$year}' AND ";
            $query .= "     T1.SCHREGNO = '{$model->schregno}' ";
            if ($model->Properties["useSchregQualified"] == 'SUBCLASS_QUALIFIED_TEST_DAT') {
                $query .= " UNION ";
                $query .= " SELECT ";
                $query .= "     0 AS SEQ, ";
                $query .= "     T1.TEST_DATE AS REGDDATE, ";
                $query .= "     '00-00-00-000000' AS SUBCLASSCD, ";
                $query .= "     VALUE(L1.QUALIFIED_NAME,'') || ' ' || VALUE(N1.NAME1,'') AS DATA ";
                $query .= " FROM ";
                $query .= "     SCHREG_QUALIFIED_TEST_DAT T1 ";
                $query .= "     LEFT JOIN QUALIFIED_MST L1 ON L1.QUALIFIED_CD = T1.QUALIFIED_CD ";
                $query .= "     LEFT JOIN QUALIFIED_TEST_MST L2 ";
                $query .= "          ON L2.YEAR         = T1.YEAR ";
                $query .= "         AND L2.QUALIFIED_CD = T1.QUALIFIED_CD ";
                $query .= "         AND L2.TEST_CD      = T1.TEST_CD ";
                $query .= "     LEFT JOIN NAME_MST N1 ";
                $query .= "          ON N1.NAMECD1      = 'Z050' ";
                $query .= "         AND SUBSTR(T1.RESULT_CD, 4) = N1.NAMECD2 ";
                if (0 < $db->getOne(knje012bQuery::getSyscatColumns("QUALIFIED_RESULT_MST"))) {
                    $query .= " INNER JOIN ";
                    $query .= "     QUALIFIED_RESULT_MST L3 ON L3.YEAR = T1.YEAR ";
                    $query .= "                            AND L3.QUALIFIED_CD = T1.QUALIFIED_CD ";
                    $query .= "                            AND L3.RESULT_CD = T1.RESULT_CD ";
                    $query .= "                            AND L3.NOT_PRINT IS NULL ";
                }
                $query .= " WHERE ";
                $query .= "     T1.YEAR     = '{$year}' AND ";
                $query .= "     T1.SCHREGNO = '{$model->schregno}' AND ";
                $query .= "     T1.RESULT_CD NOT IN ('8888', '9999') "; //欠席・不合格を除く
            }
            $query .= " ORDER BY ";
            $query .= "     REGDDATE, ";
            $query .= "     SUBCLASSCD, ";
            $query .= "     SEQ ";
        } else {
            $query .= " SELECT ";
            $query .= "     T1.REGDDATE, ";
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "     T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD AS SUBCLASSCD, ";
            } else {
                $query .= "     T1.SUBCLASSCD, ";
            }
            $query .= "     T1.SEQ, ";
            $query .= "     VALUE(T1.CONTENTS,'') || ' ' || VALUE(T1.REMARK,'') AS DATA ";
            $query .= " FROM ";
            $query .= "     SCHREG_QUALIFIED_HOBBY_DAT T1 ";
            $query .= "     LEFT JOIN SUBCLASS_MST T2 ON T1.SUBCLASSCD = T2.SUBCLASSCD ";
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "                                  AND T1.CLASSCD         = T2.CLASSCD ";
                $query .= "                                  AND T1.SCHOOL_KIND     = T2.SCHOOL_KIND ";
                $query .= "                                  AND T1.CURRICULUM_CD   = T2.CURRICULUM_CD ";
            }
            $query .= "       LEFT JOIN SCHREG_REGD_DAT T4 ON T4.SCHREGNO = T1.SCHREGNO ";
            $query .= "                                   AND T4.YEAR     = T1.YEAR ";
            $query .= "                                   AND T4.SEMESTER = '".$model->exp_semester."' ";
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
            if (0 < $db->getOne(knje012bQuery::getSyscatColumns("QUALIFIED_RANK_DAT"))) {
                $query .= " INNER JOIN ";
                $query .= "     QUALIFIED_RANK_DAT L3 ON L3.QUALIFIED_CD = T1.QUALIFIED_CD ";
                $query .= "                          AND L3.RANK = T1.RANK ";
                $query .= "                          AND L3.NOT_PRINT IS NULL ";
            }
            $query .= " WHERE ";
            $query .= "     T1.YEAR     = '{$year}' AND ";
            $query .= "     T1.SCHREGNO = '{$model->schregno}' ";
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

    //NOT_PRINTがテーブルにあるかどうか取得
    public function getSyscatColumns($tableName)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(COLNAME) ";
        $query .= " FROM ";
        $query .= "     SYSCAT.COLUMNS ";
        $query .= " WHERE ";
        $query .= "     TABNAME = '".$tableName."' ";
        $query .= "     AND COLNAME = 'NOT_PRINT' ";

        return $query;
    }

    //賞データ取得
    public function getHyosyo($model, $detail_div, $namecd1)
    {
        $query  = "";
        $query .= " SELECT ";
        $query .= "     DETAIL_SDATE, ";
        $query .= "     VALUE(L1.NAME1,'') || ' ' || VALUE(CONTENT,'') || ' ' || VALUE(REMARK,'') AS DATA ";
        $query .= " FROM ";
        $query .= "     SCHREG_DETAILHIST_DAT T1 ";
        $query .= "     LEFT JOIN NAME_MST L1 ON L1.NAMECD1 = '".$namecd1."' AND L1.NAMECD2 = T1.DETAILCD ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO     = '".$model->schregno."' AND ";
        $query .= "     DETAIL_DIV   = '".$detail_div."' AND ";
        $query .= "     FISCALYEAR(DETAIL_SDATE) = '".$model->annual["YEAR"]."' ";
        $query .= " ORDER BY ";
        $query .= "     DETAIL_SDATE ";

        return $query;
    }

    //学校名取得
    public function getSchoolName()
    {
        $query  = " SELECT ";
        $query .= "     NAME1 ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'Z010' AND ";
        $query .= "     NAMECD2 = '00' ";

        return $query;
    }

    //罰データ取得
    public function getBatsu($model, $detail_div, $namecd1)
    {
        $query  = "";
        $query .= " SELECT ";
        $query .= "     DETAIL_SDATE, ";
        $query .= "     VALUE(L1.NAME1,'') || ' ' || VALUE(CONTENT,'') || ' ' || VALUE(REMARK,'') AS DATA ";
        $query .= " FROM ";
        $query .= "     SCHREG_DETAILHIST_DAT T1 ";
        $query .= "     LEFT JOIN NAME_MST L1 ON L1.NAMECD1 = '".$namecd1."' AND L1.NAMECD2 = T1.DETAILCD ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO     = '".$model->schregno."' AND ";
        $query .= "     DETAIL_DIV   = '".$detail_div."' AND ";
        $query .= "     FISCALYEAR(DETAIL_SDATE) = '".$model->annual["YEAR"]."' ";
        $query .= " ORDER BY ";
        $query .= "     DETAIL_SDATE ";

        return $query;
    }

    //記録備考
    public function getSchregClubHdetailDat($model)
    {
        $query  = "";
        $query .= " SELECT DISTINCT ";
        $query .= "     L1.CLUBNAME AS CLUB_SHOW, ";
        $query .= "     T1.HOSTCD, ";
        $query .= "     T1.MEET_NAME, ";
        $query .= "     T1.DETAIL_DATE, ";
        $query .= "     T1.DETAIL_SEQ, ";
        $query .= "     CASE T1.DIV WHEN '1' THEN '個人' WHEN '2' THEN '団体' ELSE '' END AS DIV_NAME, ";
        $query .= "     L3.RECORDNAME, ";
        $query .= "     L4.KINDNAME, ";
        $query .= "     T1.DOCUMENT, ";
        $query .= "     T1.DETAIL_REMARK ";
        $query .= " FROM ";
        $query .= "     SCHREG_CLUB_HDETAIL_DAT T1 ";
        $query .= "     LEFT JOIN CLUB_MST L1 ";
        $query .= "          ON T1.CLUBCD       = L1.CLUBCD ";
        if ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "         AND T1.SCHOOLCD     = L1.SCHOOLCD ";
            $query .= "         AND T1.SCHOOL_KIND  = L1.SCHOOL_KIND ";
        }
        $query .= "     LEFT JOIN CLUB_HOST_MST L2 ";
        $query .= "          ON T1.HOSTCD       = L2.HOSTCD ";
        if ($model->Properties["useClubMultiSchoolKind"] == "1") {
            $query .= "         AND T1.SCHOOLCD             = L2.SCHOOLCD ";
            $query .= "         AND T1.DETAIL_SCHOOL_KIND   = L2.SCHOOL_KIND ";
        } elseif ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "         AND T1.SCHOOLCD     = L2.SCHOOLCD ";
            $query .= "         AND T1.SCHOOL_KIND  = L2.SCHOOL_KIND ";
        }
        $query .= "     LEFT JOIN CLUB_RECORD_MST L3 ";
        $query .= "          ON T1.RECORDCD     = L3.RECORDCD ";
        if ($model->Properties["useClubMultiSchoolKind"] == "1") {
            $query .= "         AND T1.SCHOOLCD             = L3.SCHOOLCD ";
            $query .= "         AND T1.DETAIL_SCHOOL_KIND   = L3.SCHOOL_KIND ";
        } elseif ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "         AND T1.SCHOOLCD     = L3.SCHOOLCD ";
            $query .= "         AND T1.SCHOOL_KIND  = L3.SCHOOL_KIND ";
        }
        $query .= "     LEFT JOIN CLUB_ITEM_KIND_MST L4 ";
        $query .= "          ON T1.ITEMCD       = L4.ITEMCD ";
        $query .= "         AND T1.KINDCD       = L4.KINDCD ";
        if ($model->Properties["useClubMultiSchoolKind"] == "1") {
            $query .= "         AND T1.SCHOOLCD             = L4.SCHOOLCD ";
            $query .= "         AND T1.DETAIL_SCHOOL_KIND   = L4.SCHOOL_KIND ";
        } elseif ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "         AND T1.SCHOOLCD     = L4.SCHOOLCD ";
            $query .= "         AND T1.SCHOOL_KIND  = L4.SCHOOL_KIND ";
        }
        $query .= " WHERE ";
        $query .= "     T1.SCHREGNO = '".$model->schregno."' ";
        $query .= " AND FISCALYEAR(T1.DETAIL_DATE) = '".$model->annual["YEAR"]."' ";
        if ($model->Properties["useClubMultiSchoolKind"] == "1") {
            $query .= " AND T1.SCHOOLCD     = '".sprintf("%012d", SCHOOLCD)."' ";
            $query .= " AND T1.SCHOOL_KIND  = '".SCHOOLKIND."' ";
        } elseif ($model->Properties["useSchool_KindField"] == "1") {
            $query .= " AND T1.SCHOOLCD     = '".sprintf("%012d", SCHOOLCD)."' ";
            $query .= " AND T1.SCHOOL_KIND IN ( SELECT ";
            $query .= "                             S2.SCHOOL_KIND ";
            $query .= "                         FROM ";
            $query .= "                             SCHREG_REGD_DAT S1, ";
            $query .= "                             SCHREG_REGD_GDAT S2 ";
            $query .= "                         WHERE ";
            $query .= "                             S1.YEAR     = S2.YEAR AND ";
            $query .= "                             S1.YEAR     = '".$model->annual["YEAR"]."' AND ";
            $query .= "                             S1.SEMESTER = '".$model->exp_semester."' AND ";
            $query .= "                             S1.GRADE    = S2.GRADE AND ";
            $query .= "                             S1.SCHREGNO = '".$model->schregno."') ";
        }
        $query .= " ORDER BY ";
        $query .= "     T1.HOSTCD, ";
        $query .= "     T1.DETAIL_DATE DESC, ";
        $query .= "     T1.DETAIL_SEQ ";

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

    //校種取得
    public function getSchoolKind2($model)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     T2.SCHOOL_KIND ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1, ";
        $query .= "     SCHREG_REGD_GDAT T2 ";
        $query .= " WHERE ";
        $query .= "         T1.YEAR     = T2.YEAR ";
        $query .= "     AND T1.YEAR     = '".$model->exp_year."' ";
        $query .= "     AND T1.GRADE    = T2.GRADE ";
        $query .= "     AND T1.SCHREGNO = '".$model->schregno."' ";
        $query .= "     AND T1.SEMESTER IN (SELECT ";
        $query .= "                         MAX(SEMESTER) ";
        $query .= "                     FROM ";
        $query .= "                         SCHREG_REGD_DAT S1 ";
        $query .= "                     WHERE ";
        $query .= "                             T1.YEAR     = S1.YEAR ";
        $query .= "                         AND T1.SCHREGNO = S1.SCHREGNO ";
        $query .= "                     ) ";

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
        $query .= "         ATTEND_SEM T1 ";
        $query .= "         LEFT JOIN ATTEND_REMARK T2 ON T1.SEMESTER = T2.SEMESTER AND ";
        $query .= "             T1.SCHREGNO = T2.SCHREGNO ";
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

    //出欠の記録参照
    public function getAttendSemesDat2($model, $knjSchoolMst)
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
        $query .= "             ) AS SUSPEND, ";
        $query .= "         SUM(VALUE(MOURNING,0)) AS MOURNING, ";
        $query .= "         SUM(VALUE(LATE,0)) AS LATE, ";
        $query .= "         SUM(VALUE(EARLY,0)) AS EARLY ";
        $query .= "     FROM ";
        $query .= "         ATTEND_SEMES_DAT ";
        $query .= "     WHERE ";
        $query .= "             YEAR     = '".$model->exp_year."' ";
        $query .= "         AND SCHREGNO = '".$model->schregno."' ";
        $query .= "     GROUP BY ";
        $query .= "         SEMESTER, ";
        $query .= "         SCHREGNO ";
        $query .= " ), MAIN AS ( ";
        $query .= "     SELECT ";
        $query .= "         SEMESTER, ";
        $query .= "         SCHREGNO, ";
        $query .= "         LESSON, ";
        $query .= "         SUSPEND, ";
        $query .= "         MOURNING, ";
        $query .= "         VALUE(LESSON,0) - VALUE(SUSPEND,0) - VALUE(MOURNING,0) AS ATTEND1, ";
        $query .= "         SICK, ";
        $query .= "         VALUE(LESSON,0) - VALUE(SUSPEND,0) - VALUE(MOURNING,0) - VALUE(SICK,0) AS ATTEND2, ";
        $query .= "         LATE, ";
        $query .= "         EARLY ";
        $query .= "     FROM ";
        $query .= "         ATTEND_SEM ";
        $query .= " ) ";

        $query .= "     SELECT ";
        $query .= "         T1.SEMESTER, ";
        $query .= "         T1.SEMESTERNAME, ";
        $query .= "         T2.SCHREGNO, ";
        $query .= "         T2.LESSON, ";
        $query .= "         T2.SUSPEND, ";
        $query .= "         T2.MOURNING, ";
        $query .= "         T2.ATTEND1, ";
        $query .= "         T2.SICK, ";
        $query .= "         T2.ATTEND2, ";
        $query .= "         T2.LATE, ";
        $query .= "         T2.EARLY ";
        $query .= "     FROM ";
        $query .= "         SEMESTER_MST T1 ";
        $query .= "         LEFT JOIN MAIN T2 ON T1.SEMESTER = T2.SEMESTER ";
        $query .= "     WHERE ";
        $query .= "             T1.YEAR      = '".$model->exp_year."' ";
        $query .= "         AND T1.SEMESTER <> '9' ";
        $query .= "     ORDER BY ";
        $query .= "         T1.SEMESTER ";

        return $query;
    }

    /* 更新処理 */
    public function update(&$db, $model)
    {
        /********/
        /* HDAT */
        /********/
        //更新テーブル
        $tableName = "HEXAM_ENTREMARK_HDAT";
        //更新フィールド
        $data = array();
        $data["SCHREGNO"][TEXT]         = $model->schregno;
        $data["REMARK"][TEXT]           = $model->field["SOUGOUSYOKEN"];
        $data["REMARK2"][TEXT]          = $model->field["ZITUYOUSIKEN"];
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][FUNC]          = "SYSDATE()";
        //条件
        $where = "WHERE SCHREGNO = '{$model->schregno}' ";
        if (0 < $db->getOne("SELECT COUNT(*) FROM {$tableName} " .$where)) {
            $query = Query::updateSQL($data, $tableName, $where);
        } else {
            $query = Query::insertSQL($data, $tableName);
        }

        $db->query($query);
        /*******/
        /* DAT */
        /*******/
        //更新テーブル
        $tableName = "HEXAM_ENTREMARK_DAT";
        //更新フィールド
        $data = array();
        $data["YEAR"][TEXT]             = $model->annual["YEAR"];
        $data["SCHREGNO"][TEXT]         = $model->schregno;
        $data["ANNUAL"][TEXT]           = $model->annual["ANNUAL"];
        $data["ATTENDREC_REMARK"][TEXT] = $model->field["ATTENDREC_REMARK"];
        $data["SPECIALACTREC"][TEXT]    = $model->field["SPECIALACTREC"];
        $data["CLUBACT"][TEXT]          = $model->field["CLUBACT"];
        $data["STUDENTACT"][TEXT]       = $model->field["STUDENTACT"];
        $data["CALSSACT"][TEXT]         = $model->field["CALSSACT"];
        $data["SCHOOLEVENT"][TEXT]      = $model->field["SCHOOLEVENT"];
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][FUNC]          = "SYSDATE()";
        //条件
        $where = "WHERE YEAR = '{$model->annual["YEAR"]}' AND SCHREGNO = '{$model->schregno}' ";
        if (0 < $db->getOne("SELECT COUNT(*) FROM {$tableName} " .$where)) {
            $query = Query::updateSQL($data, $tableName, $where);
        } else {
            $query = Query::insertSQL($data, $tableName);
        }

        $db->query($query);
    }
}
