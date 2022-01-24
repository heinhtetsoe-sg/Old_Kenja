<?php

require_once('for_php7.php');

class knji100bQuery extends Query {

/*
        内外区分 .........A001
        性別区分 .........Z002
        出身学校 .........FINSCHOOL_MST.FINSCHOOL_NAME
        保護者との続柄 ...H201
        保護者職種........H202

        使用テーブル
        卒業生
        GRD_BASE_MST.........卒業生基礎情報・住所情報取得用
        GRD_REGD_HDAT........担任コード取得用
        GRD_REGD_DAT.........卒業生学籍情報取得用
        GRD_GUARDIAN_DAT.....卒業生保護者情報取得用
        在校生
        SCHREG_BASE_MST......在校生基礎情報取得用
        SCHREG_REGD_HDAT.....担任コード取得用
        SCHREG_REGD_DAT......在校生学籍情報取得用
        SCHREG_ADDRESS_DAT...在校生住所情報取得用
        GUARDIAN_DAT.........在校生保護者情報取得用
        共通
        STAFF_MST............担任名称取得用
*/

    //PROGRAMPATH取得
    function getProgrampath() {
        $query  = " WITH MENU AS ( ";
        $query .= "     SELECT ";
        $query .= "         MIN(MENUID) AS MENUID ";
        $query .= "     FROM ";
        $query .= "         MENU_HIGH_SECURITY_MST ";
        $query .= "     WHERE ";
        $query .= "         PROGRAMID ='KNJI100B' ";
        $query .= " ) ";

        $query .= " SELECT ";
        $query .= "     PROGRAMPATH ";
        $query .= " FROM ";
        $query .= "     MENU_HIGH_SECURITY_MST T1, ";
        $query .= "     MENU T2 ";
        $query .= " WHERE ";
        $query .= "     T1.MENUID = T2.MENUID AND ";
        $query .= "     T1.PROGRAMID ='KNJI100B' ";

        return $query;
    }

    function getSecurityHigh() {
        $query  = " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     MENU_HIGH_SECURITY_MST ";
        $query .= " WHERE ";
        $query .= "     PROGRAMID = 'KNJI100B' ";
        $query .= "     AND INVALID_FLG = '0' ";

        return $query;
    }

    function getSchoolCd() {
        $query  = " SELECT ";
        $query .= "     NAME2 ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'Z010' ";
        $query .= "     AND NAMECD2 = '00' ";

        return $query;
    }

    //在校生のとき
    function selectUngrdQuery($model)
    {
        $query = "";
        $query .= "SELECT ";
        $query .= "  T1.SCHREGNO, ";
        if ($model->output == OUT_CODE_NAME){       //コード＋名称
            $query .= "  VALUE(T1.INOUTCD,'') || ',' || ";
            $query .= "  VALUE(N1.NAME1,'') INOUTCD, ";
        }else if ($model->output == OUT_CODE_ONLY){ //コードのみ
            $query .= "  T1.INOUTCD INOUTCD, ";
        }else if ($model->output == OUT_NAME_ONLY){ //名称のみ
            $query .= "  N1.NAME1 INOUTCD, ";
        }
        $query .= "  T1.NAME, ";
        $query .= "  T1.NAME_SHOW, ";
        $query .= "  T1.NAME_KANA, ";
        $query .= "  T1.NAME_ENG, ";
        $query .= "  T1.BIRTHDAY, ";
        if ($model->output == OUT_CODE_NAME){       //コード＋名称
            $query .= "  VALUE(T1.SEX,'') || ',' || ";
            $query .= "  VALUE(N2.NAME1,'') SEX,";
        }else if ($model->output == OUT_CODE_ONLY){ //コードのみ
            $query .= "  T1.SEX SEX, ";
        }else if ($model->output == OUT_NAME_ONLY){ //名称のみ
            $query .= "  N2.NAME1 SEX,";
        }
        $query .= "  T1.BLOODTYPE, ";
        $query .= "  T1.BLOOD_RH, ";
        if ($model->output == OUT_CODE_NAME){       //コード＋名称
            $query .= "  VALUE(T1.FINSCHOOLCD,'') || ',' || VALUE(F1.FINSCHOOL_NAME,'') FINSCHOOLCD,";
        }else if ($model->output == OUT_CODE_ONLY){ //コードのみ
            $query .= "  T1.FINSCHOOLCD FINSCHOOLCD, ";
        }else if ($model->output == OUT_NAME_ONLY){ //名称のみ
            $query .= "  F1.FINSCHOOL_NAME FINSCHOOLCD,";
        }
        $query .= "  T1.FINISH_DATE, ";
        if ($model->output == OUT_CODE_NAME){       //コード＋名称
            $query .= "  VALUE(T1.PRISCHOOLCD,'') || ',' || VALUE(P1.PRISCHOOL_NAME,'') PRISCHOOLCD,";
        }else if ($model->output == OUT_CODE_ONLY){ //コードのみ
            $query .= "  T1.PRISCHOOLCD PRISCHOOLCD, ";
        }else if ($model->output == OUT_NAME_ONLY){ //名称のみ
            $query .= "  P1.PRISCHOOL_NAME PRISCHOOLCD,";
        }
        $query .= "  T1.ENT_DATE, ";
        if ($model->output == OUT_CODE_NAME){       //コード＋名称
            $query .= "  VALUE(T1.ENT_DIV,'') || ',' || ";
            $query .= "  VALUE(N6.NAME1,'') ENT_DIV, ";
        }else if ($model->output == OUT_CODE_ONLY){ //コードのみ
            $query .= "  T1.ENT_DIV ENT_DIV, ";
        }else if ($model->output == OUT_NAME_ONLY){ //名称のみ
            $query .= "  N6.NAME1 ENT_DIV, ";
        }
        $query .= "  T1.ENT_REASON, ";
        $query .= "  T1.ENT_SCHOOL, ";
        $query .= "  T1.ENT_ADDR, ";
        $query .= "  T1.GRD_DATE, ";
        if ($model->output == OUT_CODE_NAME){       //コード＋名称
            $query .= "  VALUE(T1.GRD_DIV,'') || ',' || ";
            $query .= "  VALUE(N7.NAME1,'') GRD_DIV, ";
        }else if ($model->output == OUT_CODE_ONLY){ //コードのみ
            $query .= "  T1.GRD_DIV GRD_DIV, ";
        }else if ($model->output == OUT_NAME_ONLY){ //名称のみ
            $query .= "  N7.NAME1 GRD_DIV, ";
        }
        $query .= "  T1.GRD_REASON, ";
        $query .= "  T1.GRD_SCHOOL, ";
        $query .= "  T1.GRD_ADDR, ";
        $query .= "  T1.GRD_NO, ";
        $query .= "  T1.GRD_TERM, ";
        $query .= "  T1.REMARK1, ";
        $query .= "  T1.REMARK2, ";
        $query .= "  T1.REMARK3, ";
        $query .= "  T5.GRADE, ";
        $query .= "  T5.HR_CLASS, ";
        $query .= "  T5.ATTENDNO, ";
        $query .= "  T5.ANNUAL, ";
        $query .= "  T5.COURSECD, ";
        $query .= "  T5.MAJORCD, ";
        $query .= "  T5.COURSECODE, ";
        $query .= "  T7.STAFFNAME, ";
        $query .= "  T2.ZIPCD, ";
        if ($model->output == OUT_CODE_NAME){       //コード＋名称
            $query .= "  VALUE(T2.AREACD,'') || ',' || ";
            $query .= "  VALUE(N8.NAME1,'') AREACD, ";
        }else if ($model->output == OUT_CODE_ONLY){ //コードのみ
            $query .= "  T2.AREACD AREACD, ";
        }else if ($model->output == OUT_NAME_ONLY){ //名称のみ
            $query .= "  N8.NAME1 AREACD, ";
        }
        $query .= "  T2.ADDR1, ";
        $query .= "  T2.ADDR2, ";
        $query .= "  T2.ADDR1_ENG, ";
        $query .= "  T2.ADDR2_ENG, ";
        $query .= "  T2.TELNO, ";
        $query .= "  T2.FAXNO, ";
        $query .= "  T2.EMAIL, ";
        $query .= "  T1.EMERGENCYCALL, ";
        $query .= "  T1.EMERGENCYNAME, ";
        $query .= "  T1.EMERGENCYRELA_NAME, ";
        $query .= "  T1.EMERGENCYTELNO, ";
        $query .= "  T1.EMERGENCYCALL2, ";
        $query .= "  T1.EMERGENCYNAME2, ";
        $query .= "  T1.EMERGENCYRELA_NAME2, ";
        $query .= "  T1.EMERGENCYTELNO2, ";
        if ($model->output == OUT_CODE_NAME){       //コード＋名称
            $query .= "  VALUE(T3.RELATIONSHIP,'') || ',' || ";
            $query .= "  VALUE(N3.NAME1,'') RELATIONSHIP,";
        }else if ($model->output == OUT_CODE_ONLY){ //コードのみ
            $query .= "  T3.RELATIONSHIP RELATIONSHIP, ";
        }else if ($model->output == OUT_NAME_ONLY){ //名称のみ
            $query .= "  N3.NAME1 RELATIONSHIP,";
        }
        $query .= "  T3.GUARD_NAME, ";
        $query .= "  T3.GUARD_KANA, ";
        if ($model->output == OUT_CODE_NAME){       //コード＋名称
            $query .= "  VALUE(T3.GUARD_SEX,'') || ',' || ";
            $query .= "  VALUE(N4.NAME1,'') GUARD_SEX, ";
        }else if ($model->output == OUT_CODE_ONLY){ //コードのみ
            $query .= "  T3.GUARD_SEX GUARD_SEX, ";
        }else if ($model->output == OUT_NAME_ONLY){ //名称のみ
            $query .= "  N4.NAME1 GUARD_SEX, ";
        }
        $query .= "  T3.GUARD_BIRTHDAY, ";
        $query .= "  T3.GUARD_ZIPCD, ";
        $query .= "  T3.GUARD_ADDR1, ";
        $query .= "  T3.GUARD_ADDR2, ";
        $query .= "  T3.GUARD_TELNO, ";
        $query .= "  T3.GUARD_FAXNO, ";
        $query .= "  T3.GUARD_E_MAIL, ";
        if ($model->output == OUT_CODE_NAME){       //コード＋名称
            $query .= "  VALUE(T3.GUARD_JOBCD,'') || ',' || ";
            $query .= "  VALUE(N5.NAME1,'') GUARD_JOBCD,";
        }else if ($model->output == OUT_CODE_ONLY){ //コードのみ
            $query .= "  T3.GUARD_JOBCD GUARD_JOBCD, ";
        }else if ($model->output == OUT_NAME_ONLY){ //名称のみ
            $query .= "  N5.NAME1 GUARD_JOBCD,";
        }
        $query .= "  T3.GUARD_WORK_NAME, ";
        $query .= "  T3.GUARD_WORK_TELNO, ";
        if ($model->output == OUT_CODE_NAME){       //コード＋名称
            $query .= "  VALUE(T3.GUARANTOR_RELATIONSHIP,'') || ',' || ";
            $query .= "  VALUE(N9.NAME1,'') GUARANTOR_RELATIONSHIP,";
        }else if ($model->output == OUT_CODE_ONLY){ //コードのみ
            $query .= "  T3.GUARANTOR_RELATIONSHIP GUARANTOR_RELATIONSHIP, ";
        }else if ($model->output == OUT_NAME_ONLY){ //名称のみ
            $query .= "  N9.NAME1 GUARANTOR_RELATIONSHIP,";
        }
        $query .= "  T3.GUARANTOR_NAME, ";
        $query .= "  T3.GUARANTOR_KANA, ";
        if ($model->output == OUT_CODE_NAME){       //コード＋名称
            $query .= "  VALUE(T3.GUARANTOR_SEX,'') || ',' || ";
            $query .= "  VALUE(N10.NAME1,'') GUARANTOR_SEX, ";
        }else if ($model->output == OUT_CODE_ONLY){ //コードのみ
            $query .= "  T3.GUARANTOR_SEX GUARANTOR_SEX, ";
        }else if ($model->output == OUT_NAME_ONLY){ //名称のみ
            $query .= "  N10.NAME1 GUARANTOR_SEX, ";
        }
        $query .= "  T3.GUARANTOR_ZIPCD, ";
        $query .= "  T3.GUARANTOR_ADDR1, ";
        $query .= "  T3.GUARANTOR_ADDR2, ";
        $query .= "  T3.GUARANTOR_TELNO, ";
        if ($model->output == OUT_CODE_NAME){       //コード＋名称
            $query .= "  VALUE(T3.GUARANTOR_JOBCD,'') || ',' || ";
            $query .= "  VALUE(N11.NAME1,'') GUARANTOR_JOBCD,";
        }else if ($model->output == OUT_CODE_ONLY){ //コードのみ
            $query .= "  T3.GUARANTOR_JOBCD GUARANTOR_JOBCD, ";
        }else if ($model->output == OUT_NAME_ONLY){ //名称のみ
            $query .= "  N11.NAME1 GUARANTOR_JOBCD,";
        }
        $query .= "  T3.PUBLIC_OFFICE ";
        $query .= "FROM ";
        $query .= "  SCHREG_BASE_MST T1 ";
        $query .= "  LEFT JOIN SCHREG_ADDRESS_DAT T2 ON T1.SCHREGNO = T2.SCHREGNO ";
        $query .= "  LEFT JOIN FINSCHOOL_MST F1 ON F1.FINSCHOOLCD = T1.FINSCHOOLCD ";
        $query .= "  LEFT JOIN PRISCHOOL_MST P1 ON P1.PRISCHOOLCD = T1.PRISCHOOLCD ";
        $query .= "  LEFT JOIN NAME_MST N1 ON N1.NAMECD2 = T1.INOUTCD AND N1.NAMECD1='A001' ";
        $query .= "  LEFT JOIN NAME_MST N2 ON N2.NAMECD2 = T1.SEX AND N2.NAMECD1='Z002' ";
        $query .= "  LEFT JOIN NAME_MST N6 ON N6.NAMECD2 = T1.ENT_DIV AND N6.NAMECD1='A002' ";
        $query .= "  LEFT JOIN NAME_MST N7 ON N7.NAMECD2 = T1.GRD_DIV AND N7.NAMECD1='A003' ";
        $query .= "  LEFT JOIN NAME_MST N8 ON N8.NAMECD2 = T2.AREACD AND N8.NAMECD1='A020' ";
        $query .= "  LEFT JOIN GUARDIAN_DAT T3 ON T2.SCHREGNO = T3.SCHREGNO ";
        $query .= "  LEFT JOIN NAME_MST N3 ON N3.NAMECD2 = T3.RELATIONSHIP AND N3.NAMECD1='H201' ";
        $query .= "  LEFT JOIN NAME_MST N4 ON N4.NAMECD2 = T3.GUARD_SEX AND N4.NAMECD1='Z002' ";
        $query .= "  LEFT JOIN NAME_MST N5 ON N5.NAMECD2 = T3.GUARD_JOBCD AND N5.NAMECD1='H202' ";
        $query .= "  LEFT JOIN NAME_MST N9 ON N9.NAMECD2 = T3.GUARANTOR_RELATIONSHIP AND N9.NAMECD1='H201' ";
        $query .= "  LEFT JOIN NAME_MST N10 ON N10.NAMECD2 = T3.GUARANTOR_SEX AND N10.NAMECD1='Z002' ";
        $query .= "  LEFT JOIN NAME_MST N11 ON N11.NAMECD2 = T3.GUARANTOR_JOBCD AND N11.NAMECD1='H202', ";
        $query .= "  SCHREG_REGD_DAT T5, ";
        $query .= "  SCHREG_REGD_HDAT T6, ";
        $query .= "  STAFF_MST T7 ";
        $query .= "WHERE ";
        $query .= "  T1.SCHREGNO IN ('" .str_replace(",", "','", $model->schregno) ."') AND ";
        $query .= "  T1.SCHREGNO = T5.SCHREGNO AND ";
        $query .= "  T5.YEAR = '".CTRL_YEAR."' AND ";
        $query .= "  T5.SEMESTER = '".CTRL_SEMESTER."' AND ";
        $query .= "  T5.YEAR = T6.YEAR AND   ";
        $query .= "  T5.GRADE = T6.GRADE AND   ";
        $query .= "  T5.HR_CLASS = T6.HR_CLASS AND ";
        $query .= "  T6.TR_CD1 = T7.STAFFCD AND   ";
        $query .= "  T6.SEMESTER = (SELECT ";
        $query .= "                  MAX(ST1.SEMESTER) ";
        $query .= "                FROM ";
        $query .= "                  SCHREG_REGD_HDAT ST1 ";
        $query .= "                WHERE ";
        $query .= "                  ST1.YEAR = T5.YEAR AND ";
        $query .= "                  ST1.GRADE = T5.GRADE AND ";
        $query .= "                  ST1.HR_CLASS = T5.HR_CLASS ";
        $query .= "                ) AND ";
        $query .= "  T2.ISSUEDATE = (SELECT ";
        $query .= "                  MAX(ST2.ISSUEDATE) ";
        $query .= "                FROM ";
        $query .= "                  SCHREG_ADDRESS_DAT ST2 ";
        $query .= "                WHERE ";
        $query .= "                  T2.SCHREGNO = ST2.SCHREGNO ";
        $query .= "                ) ";
        $query .= "ORDER BY ";
        $query .= "  T5.GRADE, ";
        $query .= "  T5.HR_CLASS, ";
        $query .= "  T5.ATTENDNO ";

        return $query;
    }
    //卒業生のとき
    function selectGrdQuery($model)
    {
        $query = "";
        $query .= "SELECT ";
        $query .= "  T1.SCHREGNO, ";
        if ($model->output == OUT_CODE_NAME){       //コード＋名称
            $query .= "  VALUE(T1.INOUTCD,'') || ',' || ";
            $query .= "  VALUE(N1.NAME1,'') INOUTCD, ";
        }else if ($model->output == OUT_CODE_ONLY){ //コードのみ
            $query .= "  T1.INOUTCD INOUTCD, ";
        }else if ($model->output == OUT_NAME_ONLY){ //名称のみ
            $query .= "  N1.NAME1 INOUTCD, ";
        }
        $query .= "  T1.NAME, ";
        $query .= "  T1.NAME_SHOW, ";
        $query .= "  T1.NAME_KANA, ";
        $query .= "  T1.NAME_ENG, ";
        $query .= "  T1.BIRTHDAY, ";
        if ($model->output == OUT_CODE_NAME){       //コード＋名称
            $query .= "  VALUE(T1.SEX,'') || ',' || ";
            $query .= "  VALUE(N2.NAME1,'') SEX,";
        }else if ($model->output == OUT_CODE_ONLY){ //コードのみ
            $query .= "  T1.SEX SEX, ";
        }else if ($model->output == OUT_NAME_ONLY){ //名称のみ
            $query .= "  N2.NAME1 SEX,";
        }
        $query .= "  T1.BLOODTYPE, ";
        $query .= "  T1.BLOOD_RH, ";
        if ($model->output == OUT_CODE_NAME){       //コード＋名称
            $query .= "  VALUE(T1.FINSCHOOLCD,'') || ',' || VALUE(F1.FINSCHOOL_NAME,'') FINSCHOOLCD,";
        }else if ($model->output == OUT_CODE_ONLY){ //コードのみ
            $query .= "  T1.FINSCHOOLCD FINSCHOOLCD, ";
        }else if ($model->output == OUT_NAME_ONLY){ //名称のみ
            $query .= "  F1.FINSCHOOL_NAME FINSCHOOLCD,";
        }
        $query .= "  T1.FINISH_DATE, ";
        if ($model->output == OUT_CODE_NAME){       //コード＋名称
            $query .= "  VALUE(T1.PRISCHOOLCD,'') || ',' || VALUE(P1.PRISCHOOL_NAME,'') PRISCHOOLCD,";
        }else if ($model->output == OUT_CODE_ONLY){ //コードのみ
            $query .= "  T1.PRISCHOOLCD PRISCHOOLCD, ";
        }else if ($model->output == OUT_NAME_ONLY){ //名称のみ
            $query .= "  P1.PRISCHOOL_NAME PRISCHOOLCD,";
        }
        $query .= "  T1.ENT_DATE, ";
        if ($model->output == OUT_CODE_NAME){       //コード＋名称
            $query .= "  VALUE(T1.ENT_DIV,'') || ',' || ";
            $query .= "  VALUE(N6.NAME1,'') ENT_DIV, ";
        }else if ($model->output == OUT_CODE_ONLY){ //コードのみ
            $query .= "  T1.ENT_DIV ENT_DIV, ";
        }else if ($model->output == OUT_NAME_ONLY){ //名称のみ
            $query .= "  N6.NAME1 ENT_DIV, ";
        }
        $query .= "  T1.ENT_REASON, ";
        $query .= "  T1.ENT_SCHOOL, ";
        $query .= "  T1.ENT_ADDR, ";
        $query .= "  T1.GRD_DATE, ";
        if ($model->output == OUT_CODE_NAME){       //コード＋名称
            $query .= "  VALUE(T1.GRD_DIV,'') || ',' || ";
            $query .= "  VALUE(N7.NAME1,'') GRD_DIV, ";
        }else if ($model->output == OUT_CODE_ONLY){ //コードのみ
            $query .= "  T1.GRD_DIV GRD_DIV, ";
        }else if ($model->output == OUT_NAME_ONLY){ //名称のみ
            $query .= "  N7.NAME1 GRD_DIV, ";
        }
        $query .= "  T1.GRD_REASON, ";
        $query .= "  T1.GRD_SCHOOL, ";
        $query .= "  T1.GRD_ADDR, ";
        $query .= "  T1.GRD_NO, ";
        $query .= "  T1.GRD_TERM, ";
        $query .= "  T1.REMARK1, ";
        $query .= "  T1.REMARK2, ";
        $query .= "  T1.REMARK3, ";
        $query .= "  T5.GRADE, ";
        $query .= "  T5.HR_CLASS, ";
        $query .= "  T5.ATTENDNO, ";
        $query .= "  T5.ANNUAL, ";
        $query .= "  T5.COURSECD, ";
        $query .= "  T5.MAJORCD, ";
        $query .= "  T5.COURSECODE, ";
        $query .= "  T7.STAFFNAME, ";
        $query .= "  T1.CUR_ZIPCD ZIPCD, ";
        if ($model->output == OUT_CODE_NAME){       //コード＋名称
            $query .= "  VALUE(T1.CUR_AREACD,'') || ',' || ";
            $query .= "  VALUE(N8.NAME1,'') AREACD, ";
        }else if ($model->output == OUT_CODE_ONLY){ //コードのみ
            $query .= "  T1.CUR_AREACD AREACD, ";
        }else if ($model->output == OUT_NAME_ONLY){ //名称のみ
            $query .= "  N8.NAME1 AREACD, ";
        }
        $query .= "  T1.CUR_ADDR1 ADDR1, ";
        $query .= "  T1.CUR_ADDR2 ADDR2, ";
        $query .= "  T1.CUR_ADDR1_ENG ADDR1_ENG, ";
        $query .= "  T1.CUR_ADDR2_ENG  ADDR2_ENG, ";
        $query .= "  T1.CUR_TELNO TELNO, ";
        $query .= "  T1.CUR_FAXNO FAXNO, ";
        $query .= "  T1.CUR_EMAIL EMAIL, ";
        $query .= "  T1.CUR_EMERGENCYCALL EMERGENCYCALL, ";
        $query .= "  T1.CUR_EMERGENCYNAME EMERGENCYNAME, ";
        $query .= "  T1.CUR_EMERGENCYRELA_NAME EMERGENCYRELA_NAME, ";
        $query .= "  T1.CUR_EMERGENCYTELNO EMERGENCYTELNO, ";
        $query .= "  T1.CUR_EMERGENCYCALL2 EMERGENCYCALL2, ";
        $query .= "  T1.CUR_EMERGENCYNAME2 EMERGENCYNAME2, ";
        $query .= "  T1.CUR_EMERGENCYRELA_NAME2 EMERGENCYRELA_NAME2, ";
        $query .= "  T1.CUR_EMERGENCYTELNO2 EMERGENCYTELNO2, ";
        if ($model->output == OUT_CODE_NAME){       //コード＋名称
            $query .= "  VALUE(T3.RELATIONSHIP,'') || ',' || ";
            $query .= "  VALUE(N3.NAME1,'') RELATIONSHIP,";
        }else if ($model->output == OUT_CODE_ONLY){ //コードのみ
            $query .= "  T3.RELATIONSHIP RELATIONSHIP, ";
        }else if ($model->output == OUT_NAME_ONLY){ //名称のみ
            $query .= "  N3.NAME1 RELATIONSHIP,";
        }
        $query .= "  T3.GUARD_NAME, ";
        $query .= "  T3.GUARD_KANA, ";
        if ($model->output == OUT_CODE_NAME){       //コード＋名称
            $query .= "  VALUE(T3.GUARD_SEX,'') || ',' || ";
            $query .= "  VALUE(N4.NAME1,'') GUARD_SEX, ";
        }else if ($model->output == OUT_CODE_ONLY){ //コードのみ
            $query .= "  T3.GUARD_SEX GUARD_SEX, ";
        }else if ($model->output == OUT_NAME_ONLY){ //名称のみ
            $query .= "  N4.NAME1 GUARD_SEX, ";
        }
        $query .= "  T3.GUARD_BIRTHDAY, ";
        $query .= "  T3.GUARD_ZIPCD, ";
        $query .= "  T3.GUARD_ADDR1, ";
        $query .= "  T3.GUARD_ADDR2, ";
        $query .= "  T3.GUARD_TELNO, ";
        $query .= "  T3.GUARD_FAXNO, ";
        $query .= "  T3.GUARD_E_MAIL, ";
        if ($model->output == OUT_CODE_NAME){       //コード＋名称
            $query .= "  VALUE(T3.GUARD_JOBCD,'') || ',' || ";
            $query .= "  VALUE(N5.NAME1,'') GUARD_JOBCD,";
        }else if ($model->output == OUT_CODE_ONLY){ //コードのみ
            $query .= "  T3.GUARD_JOBCD GUARD_JOBCD, ";
        }else if ($model->output == OUT_NAME_ONLY){ //名称のみ
            $query .= "  N5.NAME1 GUARD_JOBCD,";
        }
        $query .= "  T3.GUARD_WORK_NAME, ";
        $query .= "  T3.GUARD_WORK_TELNO, ";
        if ($model->output == OUT_CODE_NAME){       //コード＋名称
            $query .= "  VALUE(T3.GUARANTOR_RELATIONSHIP,'') || ',' || ";
            $query .= "  VALUE(N9.NAME1,'') GUARANTOR_RELATIONSHIP,";
        }else if ($model->output == OUT_CODE_ONLY){ //コードのみ
            $query .= "  T3.GUARANTOR_RELATIONSHIP GUARANTOR_RELATIONSHIP, ";
        }else if ($model->output == OUT_NAME_ONLY){ //名称のみ
            $query .= "  N9.NAME1 GUARANTOR_RELATIONSHIP,";
        }
        $query .= "  T3.GUARANTOR_NAME, ";
        $query .= "  T3.GUARANTOR_KANA, ";
        if ($model->output == OUT_CODE_NAME){       //コード＋名称
            $query .= "  VALUE(T3.GUARANTOR_SEX,'') || ',' || ";
            $query .= "  VALUE(N10.NAME1,'') GUARANTOR_SEX, ";
        }else if ($model->output == OUT_CODE_ONLY){ //コードのみ
            $query .= "  T3.GUARANTOR_SEX GUARANTOR_SEX, ";
        }else if ($model->output == OUT_NAME_ONLY){ //名称のみ
            $query .= "  N10.NAME1 GUARANTOR_SEX, ";
        }
        $query .= "  T3.GUARANTOR_ZIPCD, ";
        $query .= "  T3.GUARANTOR_ADDR1, ";
        $query .= "  T3.GUARANTOR_ADDR2, ";
        $query .= "  T3.GUARANTOR_TELNO, ";
        if ($model->output == OUT_CODE_NAME){       //コード＋名称
            $query .= "  VALUE(T3.GUARANTOR_JOBCD,'') || ',' || ";
            $query .= "  VALUE(N11.NAME1,'') GUARANTOR_JOBCD,";
        }else if ($model->output == OUT_CODE_ONLY){ //コードのみ
            $query .= "  T3.GUARANTOR_JOBCD GUARANTOR_JOBCD, ";
        }else if ($model->output == OUT_NAME_ONLY){ //名称のみ
            $query .= "  N11.NAME1 GUARANTOR_JOBCD,";
        }
        $query .= "  T3.PUBLIC_OFFICE ";
        $query .= "FROM ";
        $query .= "  GRD_BASE_MST T1 "; 
        $query .= "  LEFT JOIN FINSCHOOL_MST F1 ON F1.FINSCHOOLCD = T1.FINSCHOOLCD ";
        $query .= "  LEFT JOIN PRISCHOOL_MST P1 ON P1.PRISCHOOLCD = T1.PRISCHOOLCD ";
        $query .= "  LEFT JOIN NAME_MST N1 ON N1.NAMECD2 = T1.INOUTCD AND N1.NAMECD1='A001' ";
        $query .= "  LEFT JOIN NAME_MST N2 ON N2.NAMECD2 = T1.SEX AND N2.NAMECD1='Z002' ";
        $query .= "  LEFT JOIN NAME_MST N6 ON N6.NAMECD2 = T1.ENT_DIV AND N6.NAMECD1='A002' ";
        $query .= "  LEFT JOIN NAME_MST N7 ON N7.NAMECD2 = T1.GRD_DIV AND N7.NAMECD1='A003' ";
        $query .= "  LEFT JOIN NAME_MST N8 ON N8.NAMECD2 = T1.CUR_AREACD AND N8.NAMECD1='A020' ";
        $query .= "  LEFT OUTER JOIN GRD_GUARDIAN_DAT T3 ON T1.SCHREGNO = T3.SCHREGNO ";
        $query .= "  LEFT JOIN NAME_MST N3 ON N3.NAMECD2 = T3.RELATIONSHIP AND N3.NAMECD1='H201' ";
        $query .= "  LEFT JOIN NAME_MST N4 ON N4.NAMECD2 = T3.GUARD_SEX AND N4.NAMECD1='Z002' ";
        $query .= "  LEFT JOIN NAME_MST N5 ON N5.NAMECD2 = T3.GUARD_JOBCD AND N5.NAMECD1='H202' ";
        $query .= "  LEFT JOIN NAME_MST N9 ON N9.NAMECD2 = T3.GUARANTOR_RELATIONSHIP AND N9.NAMECD1='H201' ";
        $query .= "  LEFT JOIN NAME_MST N10 ON N10.NAMECD2 = T3.GUARANTOR_SEX AND N10.NAMECD1='Z002' ";
        $query .= "  LEFT JOIN NAME_MST N11 ON N11.NAMECD2 = T3.GUARANTOR_JOBCD AND N11.NAMECD1='H202', ";
        $query .= "  GRD_REGD_DAT T5, ";
        $query .= "  GRD_REGD_HDAT T6, ";
        $query .= "  STAFF_MST T7 ";
        $query .= "WHERE ";
        $query .= "  T1.SCHREGNO IN ('" .str_replace(",", "','", $model->schregno) ."') AND ";
        $query .= "  T1.SCHREGNO = T5.SCHREGNO AND ";
        $query .= "  T5.YEAR = FISCALYEAR(T1.GRD_DATE) AND ";
        $query .= "  T5.SEMESTER = T1.GRD_SEMESTER AND ";
        $query .= "  T5.HR_CLASS = T1.GRD_HR_CLASS AND ";
        $query .= "  T5.GRADE = T1.GRD_GRADE AND ";
        $qeury .= "  T5.ATTENDNO = T1.GRD_ATTENDNO AND ";
        $query .= "  T5.YEAR = T6.YEAR AND   ";
        $query .= "  T5.GRADE = T6.GRADE AND   ";
        $query .= "  T5.HR_CLASS = T6.HR_CLASS AND ";
        $query .= "  T6.TR_CD1 = T7.STAFFCD AND   ";
        $query .= "  T6.SEMESTER = (SELECT ";
        $query .= "                  MAX(ST1.SEMESTER) ";
        $query .= "                FROM ";
        $query .= "                  SCHREG_REGD_HDAT ST1 ";
        $query .= "                WHERE ";
        $query .= "                  ST1.YEAR = T5.YEAR AND ";
        $query .= "                  ST1.GRADE = T5.GRADE AND ";
        $query .= "                  ST1.HR_CLASS = T5.HR_CLASS ";
        $query .= "                ) ";
        $query .= "ORDER BY ";
        $query .= "  T5.YEAR, ";
        $query .= "  T5.GRADE, ";
        $query .= "  T5.HR_CLASS, ";
        $query .= "  T5.ATTENDNO ";

        return $query;
    }
}
?>