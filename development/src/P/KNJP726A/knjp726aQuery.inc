<?php

require_once('for_php7.php');

class knjp726aQuery extends Query
{

    //校種取得
    public function getSchoolKind()
    {
        $query  = " SELECT ";
        $query .= "     ABBV1 AS LABEL, ";
        $query .= "     NAME1 AS VALUE ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'A023' ";
        $query .= "  ";

        return $query;
    }

    //年組取得
    public function getGrade()
    {
        $query .= " SELECT DISTINCT ";
        $query .= "     REGD_H.GRADE AS VALUE, ";
        $query .= "     REGD_G.GRADE_NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT REGD_H ";
        $query .= "     INNER JOIN SCHREG_REGD_GDAT REGD_G ON REGD_H.YEAR = REGD_G.YEAR ";
        $query .= "           AND REGD_H.GRADE = REGD_G.GRADE ";
        $query .= " WHERE ";
        $query .= "     REGD_H.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND REGD_H.SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //年組取得
    public function getGradeHr($model)
    {
        $query .= " SELECT ";
        $query .= "     GRADE || ':' || HR_CLASS AS VALUE, ";
        $query .= "     HR_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND SEMESTER = '".CTRL_SEMESTER."' ";
        if ($model->field["GRADE"] != "99") {
            $query .= "     AND GRADE = '{$model->field["GRADE"]}' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //模試データ出力用
    public function getCsvData($model)
    {
        $query  = " SELECT ";
        $query .= "     NMST4.NAMECD2 || ':' || NMST4.ABBV1 || '(' || NMST4.NAME1 || ')' AS SCHOOL_KIND, ";
        $query .= "     REGD.SCHREGNO, ";
        $query .= "     REGD.GRADE, ";
        $query .= "     REGD.HR_CLASS, ";
        $query .= "     REGD.ATTENDNO, ";
        $query .= "     REGD.COURSECD || REGD.MAJORCD AS COURSEMAJOR, ";
        $query .= "     S1.COURSENAME || S1.MAJORNAME AS COURSEMAJORNAME, ";
        $query .= "     REGD.COURSECODE, ";
        $query .= "     CMST.COURSECODENAME, ";
        $query .= "     BASE.SEX, ";
        $query .= "     NMST1.NAME1 AS SEXNAME, ";
        $query .= "     REPLACE(BASE.BIRTHDAY, '-', '/') AS BIRTHDAY, ";
        $query .= "     BASE.NAME, ";
        $query .= "     BASE.NAME_KANA, ";
        $query .= "     BASE.NAME_ENG, ";
        $query .= "     ADDRESS.ZIPCD, ";
        $query .= "     ADDRESS.ADDR1, ";
        $query .= "     ADDRESS.ADDR2, ";
        $query .= "     ADDRESS.TELNO, ";
        $query .= "     ADDRESS.EMAIL, ";
        $query .= "     BASE.REMARK1, ";
        $query .= "     BASE.REMARK2, ";
        $query .= "     BASE.REMARK3, ";
        $query .= "     BASE.NATIONALITY, ";
        $query .= "     NMST3.NAME1 AS NATIONALITYNAME, ";
        $query .= "     BASE2.BASE_REMARK1 AS NATIONALITY2, ";
        $query .= "     BASE2.BASE_REMARK2 AS NATIONALITYNAME2, ";
        $query .= "     GUARDIAN.GUARD_NAME, ";
        $query .= "     GUARDIAN.GUARD_KANA, ";
        $query .= "     GUARDIAN.RELATIONSHIP, ";
        $query .= "     NMST2.NAME1 AS RELATIONSHIPNAME, ";
        $query .= "     SADDR.SEND_ZIPCD, ";
        $query .= "     SADDR.SEND_ADDR1, ";
        $query .= "     SADDR.SEND_ADDR2, ";
        $query .= "     GUARDIAN.GUARD_TELNO, ";
        $query .= "     GUARDIAN.GUARD_TELNO2, ";
        $query .= "     GUARDIAN.GUARD_E_MAIL, ";
        $query .= "     BASE.EMERGENCYCALL, ";
        $query .= "     BASE.EMERGENCYNAME, ";
        $query .= "     BASE.EMERGENCYTELNO, ";
        $query .= "     SBD_012.BASE_REMARK1 AS EMERGENCYTELNO_2, ";
        $query .= "     BASE.EMERGENCYCALL2, ";
        $query .= "     BASE.EMERGENCYNAME2, ";
        $query .= "     BASE.EMERGENCYTELNO2, ";
        $query .= "     SBD_012.BASE_REMARK2 AS EMERGENCYTELNO2_2, ";
        $query .= "     SBD_009.BASE_REMARK2 AS YAKUYSOKU, ";
        $query .= "     SBD_003.BASE_REMARK1 AS EXAMNO, ";
        $query .= "     BASE.ENT_DATE, ";
        $query .= "     BASE.GRD_DATE, ";
        $query .= "     BASE.GRD_NO, ";
        $query .= "     BASE.FINSCHOOLCD, ";
        $query .= "     FMST.FINSCHOOL_NAME, ";
        $query .= "     BASE.FINISH_DATE, ";
        $query .= "     BANK1.BANKNAME AS BANKNAME1, ";
        $query .= "     BANK1.BANKNAME_KANA AS BANKNAME_KANA1, ";
        $query .= "     BANK1.BRANCHNAME_KANA AS BRANCHNAME_KANA1, ";
        $query .= "     REGIST_B1.BRANCHCD AS BRANCHCD1, ";
        $query .= "     REGIST_B1.DEPOSIT_ITEM AS DEPOSIT_ITEM1, ";
        $query .= "     REGIST_B1.ACCOUNTNO AS ACCOUNTNO1, ";
        $query .= "     CASE WHEN REGIST_B1.BANKCD IS NOT NULL THEN TRANSLATE_H_HK(REGIST_B1.ACCOUNTNAME) ELSE TRANSLATE_H_HK(GUARDIAN.GUARD_KANA) END AS ACCOUNTNAME1, ";
        $query .= "     BANK2.BANKNAME AS BANKNAME2, ";
        $query .= "     BANK2.BANKNAME_KANA AS BANKNAME_KANA2, ";
        $query .= "     BANK2.BRANCHNAME_KANA AS BRANCHNAME_KANA2, ";
        $query .= "     REGIST_B2.BRANCHCD AS BRANCHCD2, ";
        $query .= "     REGIST_B2.DEPOSIT_ITEM AS DEPOSIT_ITEM2, ";
        $query .= "     REGIST_B2.ACCOUNTNO AS ACCOUNTNO2, ";
        $query .= "     REGIST_B2.ACCOUNTNAME AS ACCOUNTNAME2, ";
        $query .= "     ENVIR.JOSYA_2, ";
        $query .= "     ENVIR.GESYA_2, ";
        $query .= "     ENVIR.JOSYA_3, ";
        $query .= "     ENVIR.GESYA_3, ";
        $query .= "     ENVIR.JOSYA_4, ";
        $query .= "     ENVIR.GESYA_4, ";
        $query .= "     ENVIR.JOSYA_5, ";
        $query .= "     ENVIR.GESYA_5, ";
        $query .= "     '" . $model->lastColumn . "' AS LASTCOLUMN ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT REGD ";
        $query .= "     LEFT JOIN SCHREG_REGD_GDAT REGDG ON REGD.YEAR = REGDG.YEAR AND REGD.GRADE = REGDG.GRADE ";
        $query .= "     LEFT JOIN V_COURSE_MAJOR_MST S1 ON REGD.YEAR = S1.YEAR AND REGD.COURSECD = S1.COURSECD AND REGD.MAJORCD = S1.MAJORCD ";
        $query .= "     LEFT JOIN COURSECODE_MST CMST ON REGD.COURSECODE = CMST.COURSECODE ";
        $query .= "     LEFT JOIN SCHREG_BASE_MST BASE ON REGD.SCHREGNO = BASE.SCHREGNO ";
        $query .= "     LEFT JOIN GUARDIAN_DAT GUARDIAN ON REGD.SCHREGNO = GUARDIAN.SCHREGNO ";
        $query .= "     LEFT JOIN SCHREG_REGD_HDAT REGD_H ON REGD.YEAR = REGD_H.YEAR ";
        $query .= "          AND REGD.SEMESTER = REGD_H.SEMESTER ";
        $query .= "          AND REGD.GRADE = REGD_H.GRADE ";
        $query .= "          AND REGD.HR_CLASS = REGD_H.HR_CLASS ";
        $query .= "     LEFT JOIN REGISTBANK_DAT REGIST_B1 ON REGIST_B1.SCHOOLCD = '".sprintf("%012d", SCHOOLCD)."' ";
        $query .= "          AND REGD.SCHREGNO = REGIST_B1.SCHREGNO ";
        $query .= "          AND REGIST_B1.SEQ = '1' ";
        $query .= "     LEFT JOIN BANK_MST BANK1 ON REGIST_B1.BANKCD = BANK1.BANKCD ";
        $query .= "          AND REGIST_B1.BRANCHCD = BANK1.BRANCHCD ";
        $query .= "     LEFT JOIN REGISTBANK_DAT REGIST_B2 ON REGIST_B2.SCHOOLCD = '".sprintf("%012d", SCHOOLCD)."' ";
        $query .= "          AND REGD.SCHREGNO = REGIST_B2.SCHREGNO ";
        $query .= "          AND REGIST_B2.SEQ = '2' ";
        $query .= "     LEFT JOIN BANK_MST BANK2 ON REGIST_B2.BANKCD = BANK2.BANKCD ";
        $query .= "          AND REGIST_B2.BRANCHCD = BANK2.BRANCHCD ";
        $query .= "     LEFT JOIN NAME_MST NMST1 ON NMST1.NAMECD1 = 'Z002' AND NMST1.NAMECD2 = BASE.SEX ";
        $query .= "     LEFT JOIN SCHREG_ADDRESS_DAT ADDRESS ON ADDRESS.SCHREGNO=REGD.SCHREGNO ";
        $query .= "         AND ADDRESS.ISSUEDATE = (SELECT MAX(ISSUEDATE) FROM SCHREG_ADDRESS_DAT WHERE SCHREGNO=REGD.SCHREGNO) ";
        $query .= "     LEFT JOIN NAME_MST NMST2 ON NMST2.NAMECD2 = GUARDIAN.RELATIONSHIP AND NMST2.NAMECD1 = 'H201' ";
        $query .= "     LEFT JOIN SCHREG_BASE_DETAIL_MST SBD_003 ON REGD.SCHREGNO = SBD_003.SCHREGNO AND SBD_003.BASE_SEQ = '003' ";
        $query .= "     LEFT JOIN FINSCHOOL_MST FMST ON FMST.FINSCHOOLCD = BASE.FINSCHOOLCD ";
        $query .= "     LEFT JOIN NAME_MST NMST3 ON NMST3.NAMECD2 = BASE.NATIONALITY AND NMST3.NAMECD1 = 'A024' ";
        $query .= "     LEFT JOIN SCHREG_BASE_DETAIL_MST BASE2 ON BASE2.SCHREGNO = REGD.SCHREGNO AND BASE2.BASE_SEQ = '011' ";
        $query .= "     LEFT JOIN SCHREG_ENVIR_DAT ENVIR ON ENVIR.SCHREGNO = REGD.SCHREGNO ";
        $query .= "     LEFT JOIN SCHREG_BASE_DETAIL_MST SBD_012 ON REGD.SCHREGNO = SBD_012.SCHREGNO AND SBD_003.BASE_SEQ = '012' ";
        $query .= "     LEFT JOIN SCHREG_BASE_DETAIL_MST SBD_009 ON REGD.SCHREGNO = SBD_009.SCHREGNO AND SBD_003.BASE_SEQ = '009' ";
        $query .= "     LEFT JOIN SCHREG_SEND_ADDRESS_DAT SADDR ON REGD.SCHREGNO = SADDR.SCHREGNO AND SADDR.DIV = '1' ";
        $query .= "     LEFT JOIN NAME_MST NMST4 ON NMST4.NAME1 = REGDG.SCHOOL_KIND AND NMST4.NAMECD1 = 'A023' ";
        $query .= " WHERE ";
        $query .= "     REGD.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND REGD.SEMESTER = '".CTRL_SEMESTER."' ";
        if ($model->field["GRADE"] != "99") {
            $query .= "     AND REGD.GRADE = '{$model->field["GRADE"]}' ";
        }
        if ($model->field["GRADE_HR"] != "99") {
            $query .= "     AND REGD.GRADE || ':' || REGD.HR_CLASS = '{$model->field["GRADE_HR"]}' ";
        }
        if ($model->field["SCHOOL_KIND"] != "99") {
            $query .= "     AND REGDG.SCHOOL_KIND = '{$model->field["SCHOOL_KIND"]}' ";
        }

        $query .= " ORDER BY ";
        $query .= "     NMST4.NAMECD2, ";
        $query .= "     REGD.SCHREGNO ";

        return $query;
    }
}
