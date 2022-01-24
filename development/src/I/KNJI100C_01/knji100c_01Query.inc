<?php

require_once('for_php7.php');

class knji100c_01Query extends Query
{

/*
        内外区分 .........A001
        性別区分 .........Z002
        出身学校 .........FINSCHOOL_MST.FINSCHOOL_NAME
        塾 ...............PRISCHOOL_MST.PRISCHOOL_NAME
        入学区分 .........A002
        卒業区分 .........A003
        地区 .............A020
        保護者との続柄 ...H201
        保護者職種........H202

        使用テーブル
        SCHREG_BASE_MST........在校生基礎情報取得用
        SCHREG_REGD_HDAT.......担任コード取得用
        SCHREG_REGD_DAT........在校生学籍情報取得用
        SCHREG_ADDRESS_DAT.....在校生住所情報取得用
        GUARDIAN_DAT...........在校生保護者情報取得用
        GUARDIAN_ADDRESS_DAT...在校生保護者住所情報取得用
        STAFF_MST..............担任名称取得用
*/

    //在校生のとき
    public function selectUngrdQuery($model)
    {
        $query  = " WITH BASE_MST AS ( ";
        $query .= "     SELECT ";
        $query .= "         * ";
        $query .= "     FROM ";
        $query .= "         SCHREG_BASE_MST ";
        $query .= "     WHERE ";
        $query .= "         SCHREGNO IN ('" .str_replace(",", "','", $model->selectdata_l) ."') ";
        $query .= " ), SCHREG_ADDRESS AS ( ";
        $query .= "     SELECT ";
        $query .= "         T2.* ";
        $query .= "     FROM ";
        $query .= "         (SELECT ";
        $query .= "             S1.SCHREGNO, ";
        $query .= "             MAX(S2.ISSUEDATE) AS ISSUEDATE ";
        $query .= "         FROM ";
        $query .= "             BASE_MST S1, ";
        $query .= "             SCHREG_ADDRESS_DAT S2 ";
        $query .= "         WHERE ";
        $query .= "             S1.SCHREGNO = S2.SCHREGNO ";
        $query .= "         GROUP BY ";
        $query .= "             S1.SCHREGNO ";
        $query .= "         ) T1, ";
        $query .= "         SCHREG_ADDRESS_DAT T2 ";
        $query .= "     WHERE ";
        $query .= "         T1.SCHREGNO     = T2.SCHREGNO AND ";
        $query .= "         T1.ISSUEDATE    = T2.ISSUEDATE ";
        $query .= " ), GUARDIAN_ADDRESS AS ( ";
        $query .= "     SELECT ";
        $query .= "         T2.* ";
        $query .= "     FROM ";
        $query .= "         (SELECT ";
        $query .= "             S1.SCHREGNO, ";
        $query .= "             MAX(S2.ISSUEDATE) AS ISSUEDATE ";
        $query .= "         FROM ";
        $query .= "             BASE_MST S1, ";
        $query .= "             GUARDIAN_ADDRESS_DAT S2 ";
        $query .= "         WHERE ";
        $query .= "             S1.SCHREGNO = S2.SCHREGNO ";
        $query .= "         GROUP BY ";
        $query .= "             S1.SCHREGNO ";
        $query .= "         ) T1, ";
        $query .= "         GUARDIAN_ADDRESS_DAT T2 ";
        $query .= "     WHERE ";
        $query .= "         T1.SCHREGNO     = T2.SCHREGNO AND ";
        $query .= "         T1.ISSUEDATE    = T2.ISSUEDATE ";
        if ($model->Properties["useGuardian2"] == "1") {
            $query .= " ), GUARDIAN_ADDRESS2 AS ( ";
            $query .= "     SELECT ";
            $query .= "         T2.* ";
            $query .= "     FROM ";
            $query .= "         (SELECT ";
            $query .= "             S1.SCHREGNO, ";
            $query .= "             MAX(S2.ISSUEDATE) AS ISSUEDATE ";
            $query .= "         FROM ";
            $query .= "             BASE_MST S1, ";
            $query .= "             GUARDIAN2_ADDRESS_DAT S2 ";
            $query .= "         WHERE ";
            $query .= "             S1.SCHREGNO = S2.SCHREGNO ";
            $query .= "         GROUP BY ";
            $query .= "             S1.SCHREGNO ";
            $query .= "         ) T1, ";
            $query .= "         GUARDIAN2_ADDRESS_DAT T2 ";
            $query .= "     WHERE ";
            $query .= "         T1.SCHREGNO     = T2.SCHREGNO AND ";
            $query .= "         T1.ISSUEDATE    = T2.ISSUEDATE ";
        }
        $query .= " ) ";

        $query .= " SELECT ";
        $query .= "     T1.SCHREGNO, ";
        if ($model->output == OUT_CODE_NAME) {          //コード＋名称
            $query .= "     VALUE(T1.INOUTCD,'') || ',' || ";
            $query .= "     VALUE(N1.NAME1,'') INOUTCD, ";
        } elseif ($model->output == OUT_CODE_ONLY) {   //コードのみ
            $query .= "     T1.INOUTCD INOUTCD, ";
        } elseif ($model->output == OUT_NAME_ONLY) {   //名称のみ
            $query .= "     N1.NAME1 INOUTCD, ";
        }
        $query .= "     T1.NAME, ";
        $query .= "     T1.NAME_SHOW, ";
        $query .= "     T1.NAME_KANA, ";
        $query .= "     T1.NAME_ENG, ";
        $query .= "     T1.REAL_NAME, ";
        $query .= "     T1.REAL_NAME_KANA, ";
        $query .= "     T1.BIRTHDAY, ";
        if ($model->output == OUT_CODE_NAME) {          //コード＋名称
            $query .= "     VALUE(T1.SEX,'') || ',' || ";
            $query .= "     VALUE(N2.NAME1,'') SEX, ";
        } elseif ($model->output == OUT_CODE_ONLY) {   //コードのみ
            $query .= "     T1.SEX SEX, ";
        } elseif ($model->output == OUT_NAME_ONLY) {   //名称のみ
            $query .= "     N2.NAME1 SEX, ";
        }
        $query .= "     N15.NAME1 HANDICAP, ";
        $query .= "     T1.BLOODTYPE, ";
        $query .= "     T1.BLOOD_RH, ";
        if ($model->output == OUT_CODE_NAME) {          //コード＋名称
            $query .= "     VALUE(T1.PRISCHOOLCD,'') || ',' || VALUE(P1.PRISCHOOL_NAME,'') PRISCHOOLCD, ";
        } elseif ($model->output == OUT_CODE_ONLY) {   //コードのみ
            $query .= "     T1.PRISCHOOLCD PRISCHOOLCD, ";
        } elseif ($model->output == OUT_NAME_ONLY) {   //名称のみ
            $query .= "     P1.PRISCHOOL_NAME PRISCHOOLCD, ";
        }
        if ($model->output == OUT_CODE_NAME) {          //コード＋名称
            $query .= "     VALUE(PCM.PRISCHOOL_CLASS_CD,'') || ',' || VALUE(PCM.PRISCHOOL_NAME,'') PRISCHOOL_CLASS_CD, ";
        } elseif ($model->output == OUT_CODE_ONLY) {   //コードのみ
            $query .= "     PCM.PRISCHOOL_CLASS_CD PRISCHOOL_CLASS_CD, ";
        } elseif ($model->output == OUT_NAME_ONLY) {   //名称のみ
            $query .= "     PCM.PRISCHOOL_NAME PRISCHOOL_CLASS_CD, ";
        }
        if ($model->output == OUT_CODE_NAME) {          //コード＋名称
            $query .= "     VALUE(T1.FINSCHOOLCD,'') || ',' || VALUE(F1.FINSCHOOL_NAME,'') FINSCHOOLCD, ";
        } elseif ($model->output == OUT_CODE_ONLY) {   //コードのみ
            $query .= "     T1.FINSCHOOLCD FINSCHOOLCD, ";
        } elseif ($model->output == OUT_NAME_ONLY) {   //名称のみ
            $query .= "     F1.FINSCHOOL_NAME FINSCHOOLCD, ";
        }
        $query .= "     T1.FINISH_DATE, ";
        $query .= "     T1.ENT_DATE, ";
        if ($model->output == OUT_CODE_NAME) {          //コード＋名称
            $query .= "     VALUE(T1.ENT_DIV,'') || ',' || ";
            $query .= "     VALUE(N6.NAME1,'') ENT_DIV, ";
        } elseif ($model->output == OUT_CODE_ONLY) {   //コードのみ
            $query .= "     T1.ENT_DIV ENT_DIV, ";
        } elseif ($model->output == OUT_NAME_ONLY) {   //名称のみ
            $query .= "     N6.NAME1 ENT_DIV, ";
        }
        $query .= "     T1.ENT_REASON, ";
        $query .= "     T1.ENT_SCHOOL, ";
        $query .= "     T1.ENT_ADDR, ";
        if ($model->Properties["useAddrField2"] == "1") {
            $query .= "     T1.ENT_ADDR2, ";
        }
        $query .= "  T1.GRD_DATE, ";
        if ($model->output == OUT_CODE_NAME) {          //コード＋名称
            $query .= "     VALUE(T1.GRD_DIV,'') || ',' || ";
            $query .= "     VALUE(N7.NAME1,'') GRD_DIV, ";
        } elseif ($model->output == OUT_CODE_ONLY) {   //コードのみ
            $query .= "     T1.GRD_DIV GRD_DIV, ";
        } elseif ($model->output == OUT_NAME_ONLY) {   //名称のみ
            $query .= "     N7.NAME1 GRD_DIV, ";
        }
        $query .= "     T1.GRD_REASON, ";
        $query .= "     T1.GRD_SCHOOL, ";
        $query .= "     T1.GRD_ADDR, ";
        if ($model->Properties["useAddrField2"] == "1") {
            $query .= "     T1.GRD_ADDR2, ";
        }
        $query .= "     T1.GRD_NO, ";
        $query .= "     T1.GRD_TERM, ";
        $query .= "     T1.REMARK1, ";
        $query .= "     T1.REMARK2, ";
        $query .= "     T1.REMARK3, ";
        $query .= "     T1.EMERGENCYCALL, ";
        $query .= "     T1.EMERGENCYNAME, ";
        $query .= "     T1.EMERGENCYRELA_NAME, ";
        $query .= "     T1.EMERGENCYTELNO, ";
        $query .= "     T1.EMERGENCYCALL2, ";
        $query .= "     T1.EMERGENCYNAME2, ";
        $query .= "     T1.EMERGENCYRELA_NAME2, ";
        $query .= "     T1.EMERGENCYTELNO2, ";
        $query .= "     BD009.BASE_REMARK2 AS BIBOUROKU, ";
        $query .= "     T5.GRADE, ";
        $query .= "     T5.HR_CLASS, ";
        $query .= "     T5.ATTENDNO, ";
        $query .= "     T5.ANNUAL, ";
        if ($model->output == OUT_NAME_ONLY) {   //名称のみ
            $query .= "     T14.COURSENAME AS COURSECD, ";
            $query .= "     T15.MAJORNAME AS MAJORCD, ";
            $query .= "     T16.COURSECODENAME AS COURSECODE, ";
        } else {
            $query .= "     T5.COURSECD, ";
            $query .= "     T5.MAJORCD, ";
            $query .= "     T5.COURSECODE, ";
        }
        $query .= "     T6.HR_NAME, ";
        $query .= "     T7.STAFFNAME, ";
        $query .= "     T2.ISSUEDATE, ";
        $query .= "     T2.EXPIREDATE, ";
        $query .= "     T2.ZIPCD, ";
        if ($model->output == OUT_CODE_NAME) {       //コード＋名称
            $query .= "     VALUE(T2.AREACD,'') || ',' || ";
            $query .= "     VALUE(N8.NAME1,'') AREACD, ";
        } elseif ($model->output == OUT_CODE_ONLY) { //コードのみ
            $query .= "     T2.AREACD AREACD, ";
        } elseif ($model->output == OUT_NAME_ONLY) { //名称のみ
            $query .= "     N8.NAME1 AREACD, ";
        }
        $query .= "     T2.ADDR1, ";
        $query .= "     T2.ADDR2, ";
        $query .= "     T2.ADDR1_ENG, ";
        $query .= "     T2.ADDR2_ENG, ";
        $query .= "     T2.ADDR_FLG, ";
        $query .= "     T2.TELNO, ";
        $query .= "     T2.FAXNO, ";
        $query .= "     T2.EMAIL, ";
        if ($model->output == OUT_CODE_NAME) {          //コード＋名称
            $query .= "     VALUE(T3.RELATIONSHIP,'') || ',' || ";
            $query .= "     VALUE(N3.NAME1,'') RELATIONSHIP, ";
        } elseif ($model->output == OUT_CODE_ONLY) {   //コードのみ
            $query .= "     T3.RELATIONSHIP RELATIONSHIP, ";
        } elseif ($model->output == OUT_NAME_ONLY) {   //名称のみ
            $query .= "     N3.NAME1 RELATIONSHIP, ";
        }
        $query .= "     T3.GUARD_NAME, ";
        $query .= "     T3.GUARD_KANA, ";
        $query .= "     T3.GUARD_REAL_NAME, ";
        $query .= "     T3.GUARD_REAL_KANA, ";
        if ($model->output == OUT_CODE_NAME) {          //コード＋名称
            $query .= "     VALUE(T3.GUARD_SEX,'') || ',' || ";
            $query .= "     VALUE(N4.NAME1,'') GUARD_SEX, ";
        } elseif ($model->output == OUT_CODE_ONLY) {   //コードのみ
            $query .= "     T3.GUARD_SEX GUARD_SEX, ";
        } elseif ($model->output == OUT_NAME_ONLY) {   //名称のみ
            $query .= "     N4.NAME1 GUARD_SEX, ";
        }
        $query .= "     T3.GUARD_BIRTHDAY, ";
        $query .= "     T8.ISSUEDATE AS GUARD_ISSUEDATE, ";
        $query .= "     T8.EXPIREDATE AS GUARD_EXPIREDATE, ";
        $query .= "     T8.GUARD_ZIPCD, ";
        $query .= "     T8.GUARD_ADDR1, ";
        $query .= "     T8.GUARD_ADDR2, ";
        $query .= "     T8.GUARD_ADDR_FLG, ";
        $query .= "     T8.GUARD_TELNO, ";
        $query .= "     T8.GUARD_FAXNO, ";
        $query .= "     T8.GUARD_E_MAIL, ";
        if ($model->output == OUT_CODE_NAME) {          //コード＋名称
            $query .= "     VALUE(T3.GUARD_JOBCD,'') || ',' || ";
            $query .= "     VALUE(N5.NAME1,'') GUARD_JOBCD, ";
        } elseif ($model->output == OUT_CODE_ONLY) {   //コードのみ
            $query .= "     T3.GUARD_JOBCD GUARD_JOBCD, ";
        } elseif ($model->output == OUT_NAME_ONLY) {   //名称のみ
            $query .= "     N5.NAME1 GUARD_JOBCD, ";
        }
        $query .= "     T3.GUARD_WORK_NAME, ";
        $query .= "     T3.GUARD_WORK_TELNO, ";
        if ($model->Properties["useGuardian2"] == "1") {
            if ($model->output == OUT_CODE_NAME) {          //コード＋名称
                $query .= "     VALUE(T9.RELATIONSHIP,'') || ',' || ";
                $query .= "     VALUE(N12.NAME1,'') AS GUARD2_RELATIONSHIP, ";
            } elseif ($model->output == OUT_CODE_ONLY) {   //コードのみ
                $query .= "     T9.RELATIONSHIP AS GUARD2_RELATIONSHIP, ";
            } elseif ($model->output == OUT_NAME_ONLY) {   //名称のみ
                $query .= "     N12.NAME1 AS GUARD2_RELATIONSHIP, ";
            }
            $query .= "     T9.GUARD_NAME AS GUARD2_NAME, ";
            $query .= "     T9.GUARD_KANA AS GUARD2_KANA, ";
            $query .= "     T9.GUARD_REAL_NAME AS GUARD2_REAL_NAME, ";
            $query .= "     T9.GUARD_REAL_KANA AS GUARD2_REAL_KANA, ";
            if ($model->output == OUT_CODE_NAME) {          //コード＋名称
                $query .= "     VALUE(T9.GUARD_SEX,'') || ',' || ";
                $query .= "     VALUE(N13.NAME1,'') AS GUARD2_SEX, ";
            } elseif ($model->output == OUT_CODE_ONLY) {   //コードのみ
                $query .= "     T9.GUARD_SEX AS GUARD2_SEX, ";
            } elseif ($model->output == OUT_NAME_ONLY) {   //名称のみ
                $query .= "     N13.NAME1 AS GUARD2_SEX, ";
            }
            $query .= "     T9.GUARD_BIRTHDAY AS GUARD2_BIRTHDAY, ";
            $query .= "     T10.ISSUEDATE AS GUARD2_ISSUEDATE, ";
            $query .= "     T10.EXPIREDATE AS GUARD2_EXPIREDATE, ";
            $query .= "     T10.GUARD_ZIPCD AS GUARD2_ZIPCD, ";
            $query .= "     T10.GUARD_ADDR1 AS GUARD2_ADDR1, ";
            $query .= "     T10.GUARD_ADDR2 AS GUARD2_ADDR2, ";
            $query .= "     T10.GUARD_ADDR_FLG AS GUARD2_ADDR_FLG, ";
            $query .= "     T10.GUARD_TELNO AS GUARD2_TELNO, ";
            $query .= "     T10.GUARD_FAXNO AS GUARD2_FAXNO, ";
            $query .= "     T10.GUARD_E_MAIL AS GUARD2_E_MAIL, ";
            if ($model->output == OUT_CODE_NAME) {          //コード＋名称
                $query .= "     VALUE(T9.GUARD_JOBCD,'') || ',' || ";
                $query .= "     VALUE(N14.NAME1,'') AS GUARD2_JOBCD, ";
            } elseif ($model->output == OUT_CODE_ONLY) {   //コードのみ
                $query .= "     T9.GUARD_JOBCD AS GUARD2_JOBCD, ";
            } elseif ($model->output == OUT_NAME_ONLY) {   //名称のみ
                $query .= "     N14.NAME1 AS GUARD2_JOBCD, ";
            }
            $query .= "     T9.GUARD_WORK_NAME AS GUARD2_WORK_NAME, ";
            $query .= "     T9.GUARD_WORK_TELNO AS GUARD2_WORK_TELNO, ";
        }
        if ($model->output == OUT_CODE_NAME) {          //コード＋名称
            $query .= "     VALUE(T3.GUARANTOR_RELATIONSHIP,'') || ',' || ";
            $query .= "     VALUE(N9.NAME1,'') GUARANTOR_RELATIONSHIP, ";
        } elseif ($model->output == OUT_CODE_ONLY) {   //コードのみ
            $query .= "     T3.GUARANTOR_RELATIONSHIP GUARANTOR_RELATIONSHIP, ";
        } elseif ($model->output == OUT_NAME_ONLY) {   //名称のみ
            $query .= "     N9.NAME1 GUARANTOR_RELATIONSHIP, ";
        }
        $query .= "     T3.GUARANTOR_NAME, ";
        $query .= "     T3.GUARANTOR_KANA, ";
        $query .= "     T3.GUARANTOR_REAL_NAME, ";
        $query .= "     T3.GUARANTOR_REAL_KANA, ";
        if ($model->output == OUT_CODE_NAME) {          //コード＋名称
            $query .= "     VALUE(T3.GUARANTOR_SEX,'') || ',' || ";
            $query .= "     VALUE(N10.NAME1,'') GUARANTOR_SEX, ";
        } elseif ($model->output == OUT_CODE_ONLY) {   //コードのみ
            $query .= "     T3.GUARANTOR_SEX GUARANTOR_SEX, ";
        } elseif ($model->output == OUT_NAME_ONLY) {   //名称のみ
            $query .= "     N10.NAME1 GUARANTOR_SEX, ";
        }
        $query .= "     T3.GUARANTOR_ZIPCD, ";
        $query .= "     T3.GUARANTOR_ADDR1, ";
        $query .= "     T3.GUARANTOR_ADDR2, ";
        $query .= "     T3.GUARANTOR_TELNO, ";
        if ($model->output == OUT_CODE_NAME) {          //コード＋名称
            $query .= "     VALUE(T3.GUARANTOR_JOBCD,'') || ',' || ";
            $query .= "     VALUE(N11.NAME1,'') GUARANTOR_JOBCD, ";
        } elseif ($model->output == OUT_CODE_ONLY) {   //コードのみ
            $query .= "     T3.GUARANTOR_JOBCD GUARANTOR_JOBCD, ";
        } elseif ($model->output == OUT_NAME_ONLY) {   //名称のみ
            $query .= "     N11.NAME1 GUARANTOR_JOBCD, ";
        }
        //出身
        if ($model->Properties["Origin_hyouji"] == '1') {
            if ($model->output == OUT_CODE_NAME) {          //コード＋名称
                $query .= "     VALUE(SBD_016.BASE_REMARK1,'') || ',' || ";
                $query .= "     VALUE(N16.NAME1,'') ORIGIN, ";
            } elseif ($model->output == OUT_CODE_ONLY) {   //コードのみ
                $query .= "     SBD_016.BASE_REMARK1 AS ORIGIN, ";
            } elseif ($model->output == OUT_NAME_ONLY) {   //名称のみ
                $query .= "     N16.NAME1 ORIGIN, ";
            }
        }
        if ($model->z010Name == "meikei" || $model->Properties["usedormitory_hyouji"] == "1") {
            $query .= "     CASE WHEN T11.DOMI_CD IS NOT NULL THEN T13.DOMI_NAME ELSE '' END AS DOMITORY, ";
        }
        $query .= "     CASE WHEN T12.SCHREGNO IS NOT NULL THEN '非送付者' ELSE '' END AS NOT_SENDER, ";
        $query .= "     T3.PUBLIC_OFFICE, ";
        $query .= "     SBD_003.BASE_REMARK1 AS EXAMNO, ";
        //入試区分
        if ($model->output == OUT_CODE_NAME) {          //コード＋名称
            $query .= "     VALUE(SBD_003.BASE_REMARK2,'') || ',' || ";
            $query .= "     VALUE(N17.NAME1,'') TESTDIV ";
        } elseif ($model->output == OUT_CODE_ONLY) {   //コードのみ
            $query .= "     SBD_003.BASE_REMARK2 AS TESTDIV ";
        } elseif ($model->output == OUT_NAME_ONLY) {   //名称のみ
            $query .= "     N17.NAME1 AS TESTDIV ";
        }
        $query .= " FROM ";
        $query .= "     BASE_MST T1 ";
        $query .= "     LEFT JOIN SCHREG_ADDRESS T2 ON T2.SCHREGNO = T1.SCHREGNO ";
        $query .= "     LEFT JOIN FINSCHOOL_MST F1 ON F1.FINSCHOOLCD = T1.FINSCHOOLCD ";
        $query .= "     LEFT JOIN PRISCHOOL_MST P1 ON P1.PRISCHOOLCD = T1.PRISCHOOLCD ";
        $query .= "     LEFT JOIN NAME_MST N1 ON N1.NAMECD2 = T1.INOUTCD AND N1.NAMECD1 = 'A001' ";
        $query .= "     LEFT JOIN NAME_MST N2 ON N2.NAMECD2 = T1.SEX AND N2.NAMECD1 = 'Z002' ";
        $query .= "     LEFT JOIN NAME_MST N6 ON N6.NAMECD2 = T1.ENT_DIV AND N6.NAMECD1 = 'A002' ";
        $query .= "     LEFT JOIN NAME_MST N7 ON N7.NAMECD2 = T1.GRD_DIV AND N7.NAMECD1 = 'A003' ";
        $query .= "     LEFT JOIN NAME_MST N8 ON N8.NAMECD2 = T2.AREACD AND N8.NAMECD1 = 'A020' ";
        $query .= "     LEFT JOIN GUARDIAN_DAT T3 ON T2.SCHREGNO = T3.SCHREGNO ";
        $query .= "     LEFT JOIN GUARDIAN_ADDRESS T8 ON T8.SCHREGNO = T1.SCHREGNO ";
        $query .= "     LEFT JOIN NAME_MST N3 ON N3.NAMECD2 = T3.RELATIONSHIP AND N3.NAMECD1 = 'H201' ";
        $query .= "     LEFT JOIN NAME_MST N4 ON N4.NAMECD2 = T3.GUARD_SEX AND N4.NAMECD1 = 'Z002' ";
        $query .= "     LEFT JOIN NAME_MST N5 ON N5.NAMECD2 = T3.GUARD_JOBCD AND N5.NAMECD1 = 'H202' ";
        $query .= "     LEFT JOIN NAME_MST N9 ON N9.NAMECD2 = T3.GUARANTOR_RELATIONSHIP AND N9.NAMECD1 = 'H201' ";
        $query .= "     LEFT JOIN NAME_MST N10 ON N10.NAMECD2 = T3.GUARANTOR_SEX AND N10.NAMECD1 = 'Z002' ";
        $query .= "     LEFT JOIN NAME_MST N11 ON N11.NAMECD2 = T3.GUARANTOR_JOBCD AND N11.NAMECD1 = 'H202' ";
        $query .= "     LEFT JOIN SCHREG_BASE_DETAIL_MST BD009 ON BD009.SCHREGNO = T1.SCHREGNO AND BD009.BASE_SEQ = '009' ";
        if ($model->Properties["useGuardian2"] == "1") {
            $query .= "     LEFT JOIN GUARDIAN2_DAT T9 ON T2.SCHREGNO = T9.SCHREGNO ";
            $query .= "     LEFT JOIN GUARDIAN_ADDRESS2 T10 ON T10.SCHREGNO = T1.SCHREGNO ";
            $query .= "     LEFT JOIN NAME_MST N12 ON N12.NAMECD2 = T9.RELATIONSHIP AND N12.NAMECD1 = 'H201' ";
            $query .= "     LEFT JOIN NAME_MST N13 ON N13.NAMECD2 = T9.GUARD_SEX AND N13.NAMECD1 = 'Z002' ";
            $query .= "     LEFT JOIN NAME_MST N14 ON N14.NAMECD2 = T9.GUARD_JOBCD AND N14.NAMECD1 = 'H202' ";
        }
        $query .= "     LEFT JOIN NAME_MST N15 ON N15.NAMECD2 = T1.HANDICAP AND N15.NAMECD1 = 'A025' ";
        $query .= "     LEFT JOIN SCHREG_BASE_DETAIL_MST BASE_D ";
        $query .= "              ON T1.SCHREGNO     = BASE_D.SCHREGNO ";
        $query .= "             AND BASE_D.BASE_SEQ = '010' ";
        $query .= "     LEFT JOIN PRISCHOOL_CLASS_MST PCM ";
        $query .= "              ON P1.PRISCHOOLCD      = PCM.PRISCHOOLCD ";
        $query .= "             AND BASE_D.BASE_REMARK1 = PCM.PRISCHOOL_CLASS_CD ";
        $query .= "     INNER JOIN SCHREG_REGD_DAT T5 ";
        $query .= "              ON T5.SCHREGNO = T1.SCHREGNO ";
        $query .= "             AND T5.YEAR     = '".CTRL_YEAR."' ";
        $query .= "             AND T5.SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= "     INNER JOIN SCHREG_REGD_HDAT T6 ";
        $query .= "              ON T6.YEAR     = T5.YEAR ";
        $query .= "             AND T6.SEMESTER = T5.SEMESTER ";
        $query .= "             AND T6.GRADE    = T5.GRADE ";
        $query .= "             AND T6.HR_CLASS = T5.HR_CLASS ";
        $query .= "     INNER JOIN STAFF_MST T7 ON T7.STAFFCD = T6.TR_CD1 ";
        if ($model->z010Name == "meikei" || $model->Properties["usedormitory_hyouji"] == "1") {
            $query .= "     LEFT JOIN SCHREG_DOMITORY_HIST_DAT T11 ";
            $query .= "       ON T11.SCHREGNO = T1.SCHREGNO ";
            $query .= "       AND T11.DOMI_ENTDAY <= '".CTRL_DATE."' ";
            $query .= "       AND (T11.DOMI_OUTDAY IS NULL OR T11.DOMI_OUTDAY >= '".CTRL_DATE."') ";
            $query .= "     LEFT JOIN DOMITORY_MST T13";
            $query .= "       ON T13.DOMI_CD = T11.DOMI_CD ";
        }
        $query .= "     LEFT JOIN SCHREG_NOT_SENDER_DAT T12 ";
        $query .= "       ON T12.SCHREGNO = T5.SCHREGNO AND T12.YEAR = T5.YEAR";
        $query .= "     LEFT JOIN SCHREG_BASE_DETAIL_MST SBD_003 ";
        $query .= "              ON T1.SCHREGNO      = SBD_003.SCHREGNO ";
        $query .= "             AND SBD_003.BASE_SEQ = '003' ";
        $query .= "     LEFT JOIN V_COURSE_MST T14 ON T14.YEAR = '".CTRL_YEAR."' AND T14.COURSECD = T5.COURSECD ";
        $query .= "     LEFT JOIN V_MAJOR_MST T15 ON T15.YEAR = '".CTRL_YEAR."' AND T15.COURSECD = T5.COURSECD AND T15.MAJORCD = T5.MAJORCD ";
        $query .= "     LEFT JOIN V_COURSECODE_MST T16 ON T16.YEAR = '".CTRL_YEAR."' AND T16.COURSECODE = T5.COURSECODE ";
        //出身
        if ($model->Properties["Origin_hyouji"] == '1') {
            $query .= "     LEFT JOIN SCHREG_BASE_DETAIL_MST SBD_016 ";
            $query .= "              ON T1.SCHREGNO      = SBD_016.SCHREGNO ";
            $query .= "             AND SBD_016.BASE_SEQ = '016' ";
            $query .= "     LEFT JOIN NAME_MST N16 ON N16.NAMECD2 = SBD_016.BASE_REMARK1 AND N16.NAMECD1 = 'A053' ";
        }
        //入試区分
        $query .= "     LEFT JOIN NAME_MST N17 ON N17.NAMECD2 = SBD_003.BASE_REMARK2 AND N17.NAMECD1 = 'L004' ";
        $query .= " ORDER BY ";
        $query .= "     T5.GRADE, ";
        $query .= "     T5.HR_CLASS, ";
        $query .= "     T5.ATTENDNO ";

        return $query;
    }

    //課程学科取得
    public function getCourseMajor($model)
    {
        $query  = " SELECT ";
        $query .= "     COURSECD || MAJORCD AS VALUE, ";
        $query .= "     COURSENAME || MAJORNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_COURSE_MAJOR_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //学年取得
    public function getGrade($model)
    {
        $query  = "SELECT ";
        $query .= "    GRADE AS VALUE, ";
        $query .= "    GRADE_NAME1 AS LABEL ";
        $query .= "FROM ";
        $query .= "    SCHREG_REGD_GDAT ";
        $query .= "WHERE ";
        $query .= "    YEAR = '".CTRL_YEAR."' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "     AND GRADE IN (SELECT ";
                $query .= "                      REGD_G.GRADE ";
                $query .= "                   FROM ";
                $query .= "                      SCHREG_REGD_GDAT REGD_G ";
                $query .= "                   WHERE ";
                $query .= "                      REGD_G.YEAR = '".CTRL_YEAR."' ";
                $query .= "                      AND REGD_G.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind), "','")."')) ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "     AND GRADE IN (SELECT ";
            $query .= "                      REGD_G.GRADE ";
            $query .= "                   FROM ";
            $query .= "                      SCHREG_REGD_GDAT REGD_G ";
            $query .= "                   WHERE ";
            $query .= "                      REGD_G.YEAR = '".CTRL_YEAR."' ";
            $query .= "                      AND REGD_G.SCHOOL_KIND = '" .SCHOOLKIND ."') ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //性別取得
    public function getSex($model)
    {
        $query  = " SELECT ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     ABBV1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR    = '".CTRL_YEAR."' AND ";
        $query .= "     NAMECD1 = 'Z002' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //年組取得（権限チェック）
    public function getAuth($model)
    {
        $query  = " SELECT ";
        $query .= "     GRADE || '_' ||  HR_CLASS AS VALUE, ";
        $query .= "     HR_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".CTRL_YEAR."' AND ";
        $query .= "     SEMESTER    = '".CTRL_SEMESTER."' ";
        if ($model->grade) {
            $query .= " AND GRADE       = '".$model->grade."' ";
        }
        //参照・更新可（制限付き）
        if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT) {
            $query .= " AND '".STAFFCD."' IN (TR_CD1, TR_CD2, TR_CD3) ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //対象外の生徒取得
    public function getSchnoIdou($model)
    {
        $date = str_replace("/", "-", $model->date);

        $query  = "";
        $query .= " SELECT T1.SCHREGNO, T1.ATTENDNO ";
        $query .= " FROM   SCHREG_REGD_DAT T1, SEMESTER_MST T2 ";
        $query .= " WHERE  T1.YEAR = '".CTRL_YEAR."' AND ";
        $query .= "        T1.SEMESTER = '".CTRL_SEMESTER."' AND ";
        $query .= "        T1.YEAR = T2.YEAR AND ";
        $query .= "        T1.SEMESTER = T2.SEMESTER AND ";
                           //卒業(1)・転学(2)・退学(3)者 但し異動日が学期終了日または異動基準日より小さい場合
        $query .= "        EXISTS(SELECT 'X' FROM SCHREG_BASE_MST S1 ";
        $query .= "                WHERE S1.SCHREGNO = T1.SCHREGNO AND ";
        $query .= "                    (S1.GRD_DIV IN('1','2','3') AND S1.GRD_DATE < ";
        $query .= "                         CASE WHEN T2.EDATE < '".$date."' ";
        $query .= "                              THEN T2.EDATE ELSE '".$date."' END)) ";
        $query .= " ORDER BY T1.ATTENDNO ";

        return $query;
    }

    //リストtoリスト右
    public function getStudent_right($model)
    {
        $query .= " SELECT ";
        $query .= "     T2.HR_NAME, ";
        $query .= "     T1.ATTENDNO, ";
        $query .= "     L1.NAME_SHOW, ";
        $query .= "     T1.GRADE || T1.HR_CLASS || T1.ATTENDNO || '-' || T1.SCHREGNO AS VALUE ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= " LEFT JOIN SCHREG_REGD_HDAT T2 ";
        $query .= "        ON T2.YEAR = T1.YEAR ";
        $query .= "       AND T2.SEMESTER = T1.SEMESTER ";
        $query .= "       AND T2.GRADE = T1.GRADE ";
        $query .= "       AND T2.HR_CLASS = T1.HR_CLASS ";
        $query .= " LEFT JOIN SCHREG_BASE_MST L1 ON L1.SCHREGNO = T1.SCHREGNO ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     T1.SEMESTER = '". CTRL_SEMESTER ."' ";
        if ($model->coursemajor) {
            $query .= " AND T1.COURSECD || T1.MAJORCD = '{$model->coursemajor}' ";
        }
        if ($model->grade) {
            $query .= " AND T1.GRADE = '{$model->grade}' ";
        }
        if ($model->sex) {
            $query .= " AND L1.SEX = '{$model->sex}' ";
        }
        if ($model->grade_hr_class) {
            $query .= " AND T1.GRADE || '_' || T1.HR_CLASS = '{$model->grade_hr_class}' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }


    //リストtoリスト左
    public function getStudent_left($model)
    {
        $query .= " SELECT ";
        $query .= "     T2.HR_NAME, ";
        $query .= "     T1.ATTENDNO, ";
        $query .= "     L1.NAME_SHOW, ";
        $query .= "     T1.GRADE || T1.HR_CLASS || T1.ATTENDNO || '-' || T1.SCHREGNO AS VALUE  ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= " LEFT JOIN SCHREG_REGD_HDAT T2 ";
        $query .= "        ON T2.YEAR = T1.YEAR ";
        $query .= "       AND T2.SEMESTER = T1.SEMESTER ";
        $query .= "       AND T2.GRADE = T1.GRADE ";
        $query .= "       AND T2.HR_CLASS = T1.HR_CLASS ";
        $query .= " LEFT JOIN SCHREG_BASE_MST L1 ON L1.SCHREGNO = T1.SCHREGNO ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     T1.SEMESTER = '". CTRL_SEMESTER ."' AND ";
        $query .= "     T1.SCHREGNO IN ('" .str_replace(",", "','", $model->selectdata_l) ."') ";
        if ($model->coursemajor) {
            $query .= " AND T1.COURSECD || T1.MAJORCD = '{$model->coursemajor}' ";
        }
        if ($model->grade) {
            $query .= " AND T1.GRADE = '{$model->grade}' ";
        }
        if ($model->sex) {
            $query .= " AND L1.SEX = '{$model->sex}' ";
        }
        if ($model->grade_hr_class) {
            $query .= " AND T1.GRADE || '_' || T1.HR_CLASS = '{$model->grade_hr_class}' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //サブシステム名取得
    public function getSubSystem()
    {
        $query  = " SELECT ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     NAMECD2 || '：' || NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     NAMECD1 = 'I001' ";
        $query .= " ORDER BY ";
        $query .= "     NAMECD2 ";

        return $query;
    }

    //学校種別取得
    public function getSchoolKind()
    {
        $query  = " SELECT ";
        $query .= "     NAME1 ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'A023' ";
        $query .= " ORDER BY ";
        $query .= "     INT(ABBV3), NAMECD2 ";

        return $query;
    }

    //入学・卒業履歴情報取得
    public function getSchregEntGrdHistDat($name, $schregno, $schoolkind)
    {
        $query  = " SELECT ";
        $query .=       $name;
        $query .= " FROM ";
        $query .= "     SCHREG_ENT_GRD_HIST_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '".$schregno."' AND ";
        $query .= "     SCHOOL_KIND = '".$schoolkind."' ";

        return $query;
    }

    //出身学校取得
    public function getFinSchoolMst($model, $finschoolcd)
    {
        $query  = " SELECT ";
        if ($model->output == OUT_CODE_NAME) {           //コード＋名称
            $query .= " VALUE(FINSCHOOLCD,'') || ',' || VALUE(FINSCHOOL_NAME,'') ";
        } elseif ($model->output == OUT_CODE_ONLY) {    //コードのみ
            $query .= " FINSCHOOLCD ";
        } elseif ($model->output == OUT_NAME_ONLY) {    //名称のみ
            $query .= " FINSCHOOL_NAME ";
        }
        $query .= " FROM ";
        $query .= "     FINSCHOOL_MST ";
        $query .= " WHERE ";
        $query .= "     FINSCHOOLCD = '".$finschoolcd."' ";

        return $query;
    }

    //名称マスタより取得
    public function getNameMst($model, $namecd1, $namecd2)
    {
        $query  = " SELECT ";
        if ($model->output == OUT_CODE_NAME) {           //コード＋名称
            $query .= " VALUE(NAMECD2,'') || ',' || VALUE(NAME1,'') ";
        } elseif ($model->output == OUT_CODE_ONLY) {    //コードのみ
            $query .= " NAMECD2 ";
        } elseif ($model->output == OUT_NAME_ONLY) {    //名称のみ
            $query .= " NAME1 ";
        }
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = '".$namecd1."' AND ";
        $query .= "     NAMECD2 = '".$namecd2."' ";

        return $query;
    }

    //郵便番号マスタより取得
    public function getZipAddress($zipcd)
    {
        $query  = " SELECT ";
        $query .= "     PREF || CITY ";
        $query .= " FROM ";
        $query .= "     ZIPCD_MST ";
        $query .= " WHERE ";
        $query .= "     NEW_ZIPCD = '".$zipcd."' ";

        return $query;
    }

    //選択フィールド取得
    public function getFieldSql($model)
    {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     KNJI100C_KAKIDASHI_LIST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND DATA_DIV = '01' ";
        $query .= " ORDER BY ";
        $query .= "     SEQ ";

        return $query;
    }

    //選択フィールド削除
    public function getDelFieldSql($model)
    {
        $query  = " DELETE FROM ";
        $query .= "     KNJI100C_KAKIDASHI_LIST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND DATA_DIV = '01' ";

        return $query;
    }

    //選択フィールド保存
    public function getFieldInsSql($model, $setSeq, $val)
    {

        //入金予定項目データ
        $data = array();
        $data["YEAR"][TEXT]            = CTRL_YEAR;
        $data["DATA_DIV"][TEXT]        = "01";
        $data["SEQ"][NUMBER]           = $setSeq;
        $data["FIELD_NAME"][TEXT]      = $val;
        $data["REGISTERCD"][TEXT]      = STAFFCD;
        $data["UPDATED"][FUNC]         = "sysdate()";

        $query = Query::insertSQL($data, "KNJI100C_KAKIDASHI_LIST");
        return $query;
    }

    public function getZ010Name1()
    {
        $query  = " SELECT ";
        $query .= "     NAME1 ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'Z010' ";
        $query .= "     AND NAMECD2 = '00' ";

        return $query;
    }

    //課程取得
    public function getCourseMst($model, $cd)
    {
        $query  = " SELECT ";
        $query .= "     COURSENAME ";
        $query .= " FROM ";
        $query .= "     V_COURSE_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND COURSECD = '".$cd."' ";

        return $query;
    }

    //学科取得
    public function getMajorMst($model, $coursecd, $majorcd)
    {
        $query  = " SELECT ";
        $query .= "     MAJORNAME ";
        $query .= " FROM ";
        $query .= "     V_MAJOR_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND COURSECD = '".$coursecd."' ";
        $query .= "     AND MAJORCD = '".$majorcd."' ";

        return $query;
    }

    //コース取得
    public function getCoursecodeMst($model, $cd)
    {
        $query  = " SELECT ";
        $query .= "     COURSECODENAME ";
        $query .= " FROM ";
        $query .= "     V_COURSECODE_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND COURSECODE = '".$cd."' ";

        return $query;
    }
}
