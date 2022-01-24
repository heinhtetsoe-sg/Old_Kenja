<?php

require_once('for_php7.php');

class knji100aQuery extends Query {

    function getKakiDashiListQuery($model)
    {
        $query  = "  SELECT ";
        $query .= "      * ";
        $query .= "  FROM ";
        $query .= "      KNJI100A_KAKIDASHI_LIST ";
        $query .= "  WHERE ";
        $query .= "      YEAR = '".CTRL_YEAR."' ";
        $query .= "  AND DATA_DIV = '00' ";

        return $query;
    }
/*
        内外区分 .........A001
        性別区分 .........Z002
        出身学校 .........FINSCHOOL_MST.FINSCHOOL_NAME
        保護者との続柄 ...H201
        保護者職種........H202

        使用テーブル
        在校生
        SCHREG_BASE_MST......在校生基礎情報取得用
        SCHREG_REGD_HDAT.....担任コード取得用
        SCHREG_REGD_DAT......在校生学籍情報取得用
        SCHREG_ADDRESS_DAT...在校生住所情報取得用
        GUARDIAN_DAT.........在校生保護者情報取得用
        共通
        STAFF_MST............担任名称取得用
*/

    //在校生のとき
    function selectUngrdQuery($model)
    {
        $query = "";
        $query .= "WITH ISSUEDATE AS ( ";
        $query .= " SELECT ";
        $query .= "   SCHREGNO, ";
        $query .= "   MAX(ISSUEDATE) AS ISSUEDATE ";
        $query .= " FROM ";
        $query .= "   SCHREG_ADDRESS_DAT ";
        $query .= " GROUP BY ";
        $query .= "   SCHREGNO ";
        $query .= " ), REGD_HDAT AS ( ";
        $query .= " SELECT ";
        $query .= "   YEAR, ";
        $query .= "   GRADE, ";
        $query .= "   HR_CLASS, ";
        $query .= "   MAX(SEMESTER) AS SEMESTER ";
        $query .= " FROM ";
        $query .= "   SCHREG_REGD_HDAT ";
        $query .= " GROUP BY ";
        $query .= "   YEAR, ";
        $query .= "   GRADE, ";
        $query .= "   HR_CLASS ";
        $query .= " ), BASE_MST AS ( ";
        $query .= " SELECT ";
        $query .= "   T1.* ";
        $query .= " FROM ";
        $query .= "   SCHREG_BASE_MST T1 ";
        if ($model->field["TAISYOSENTAKU"] == "2") {
            $query .= " WHERE ";
            $query .= "   SCHREGNO IN ('" .str_replace(",", "','", $model->selectdata_l) ."') ";
        } else {
            $query .= "  INNER JOIN SCHREG_REGD_DAT T5 ON  T5.SCHREGNO = T1.SCHREGNO ";
            $query .= "                                AND T5.YEAR     = '".CTRL_YEAR."' ";
            $query .= "                                AND T5.SEMESTER = '".CTRL_SEMESTER."' ";
            $query .= "                                AND T5.GRADE IN ('" .str_replace(",", "','", $model->selectdata_l) ."') ";
        }
        $query .= " ) ";
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
        $query .= "  T1.REAL_NAME, ";
        $query .= "  T1.REAL_NAME_KANA, ";
        $query .= "  T1.BIRTHDAY, ";
        if ($model->output == OUT_CODE_NAME){       //コード＋名称
            $query .= "  VALUE(T1.SEX,'') || ',' || ";
            $query .= "  VALUE(N2.NAME1,'') SEX,";
        }else if ($model->output == OUT_CODE_ONLY){ //コードのみ
            $query .= "  T1.SEX SEX, ";
        }else if ($model->output == OUT_NAME_ONLY){ //名称のみ
            $query .= "  N2.NAME1 SEX,";
        }
        $query .= "  N12.NAME1 AS HANDICAP, ";
        $query .= "  T1.BLOODTYPE, ";
        $query .= "  T1.BLOOD_RH, ";
        if ($model->output == OUT_CODE_NAME){       //コード＋名称
            $query .= "  VALUE(T1.PRISCHOOLCD,'') || ',' || VALUE(P1.PRISCHOOL_NAME,'') PRISCHOOLCD,";
        }else if ($model->output == OUT_CODE_ONLY){ //コードのみ
            $query .= "  T1.PRISCHOOLCD PRISCHOOLCD, ";
        }else if ($model->output == OUT_NAME_ONLY){ //名称のみ
            $query .= "  P1.PRISCHOOL_NAME PRISCHOOLCD,";
        }
        if ($model->output == OUT_CODE_NAME){       //コード＋名称
            $query .= "  VALUE(T1.FINSCHOOLCD,'') || ',' || VALUE(F1.FINSCHOOL_NAME,'') FINSCHOOLCD,";
        }else if ($model->output == OUT_CODE_ONLY){ //コードのみ
            $query .= "  T1.FINSCHOOLCD FINSCHOOLCD, ";
        }else if ($model->output == OUT_NAME_ONLY){ //名称のみ
            $query .= "  F1.FINSCHOOL_NAME FINSCHOOLCD,";
        }
        $query .= "  T1.FINISH_DATE, ";
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
        if ($model->Properties["useAddrField2"] == "1") {
            $query .= "  T1.ENT_ADDR2, ";
        }
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
        if ($model->Properties["useAddrField2"] == "1") {
            $query .= "  T1.GRD_ADDR2, ";
        }
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
        $query .= "  BD009.BASE_REMARK2 AS BIBOUROKU, ";
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
        $query .= "  BASE_MST T1 ";
        $query .= "  LEFT JOIN ISSUEDATE L1 ON  L1.SCHREGNO = T1.SCHREGNO ";
        $query .= "  LEFT JOIN SCHREG_ADDRESS_DAT T2 ON  T2.SCHREGNO = T1.SCHREGNO ";
        $query .= "                                  AND T2.ISSUEDATE = L1.ISSUEDATE ";
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
        $query .= "  LEFT JOIN NAME_MST N11 ON N11.NAMECD2 = T3.GUARANTOR_JOBCD AND N11.NAMECD1='H202' ";
        $query .= "  LEFT JOIN NAME_MST N12 ON N12.NAMECD2 = T1.HANDICAP AND N12.NAMECD1 = 'A025' ";
        $query .= "  LEFT JOIN SCHREG_BASE_DETAIL_MST BD009 ON BD009.SCHREGNO = T1.SCHREGNO AND BD009.BASE_SEQ = '009' ";
        $query .= "  INNER JOIN SCHREG_REGD_DAT T5 ON  T5.SCHREGNO = T1.SCHREGNO ";
        $query .= "                                AND T5.YEAR     = '".CTRL_YEAR."' ";
        $query .= "                                AND T5.SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= "  INNER JOIN REGD_HDAT L2 ON  L2.YEAR     = T5.YEAR ";
        $query .= "                          AND L2.GRADE    = T5.GRADE ";
        $query .= "                          AND L2.HR_CLASS    = T5.HR_CLASS ";
        $query .= "  INNER JOIN SCHREG_REGD_HDAT T6 ON  T6.YEAR     = T5.YEAR ";
        $query .= "                                 AND T6.GRADE    = T5.GRADE ";
        $query .= "                                 AND T6.HR_CLASS = T5.HR_CLASS ";
        $query .= "                                 AND T6.SEMESTER = L2.SEMESTER ";
        $query .= "  INNER JOIN STAFF_MST T7 ON  T7.STAFFCD = T6.TR_CD1 ";
        $query .= "ORDER BY ";
        $query .= "  T5.GRADE, ";
        $query .= "  T5.HR_CLASS, ";
        $query .= "  T5.ATTENDNO ";

        return $query;
    }

    //年組取得（権限チェック）
    function getAuth($model) {
        //参照・更新可
        if (AUTHORITY == DEF_REFERABLE || AUTHORITY == DEF_UPDATABLE){
            $query  = "SELECT ";
            $query .= "    T1.GRADE || '_' ||  HR_CLASS AS VALUE, ";
            $query .= "    HR_NAME AS LABEL ";
            $query .= "FROM ";
            $query .= "    SCHREG_REGD_HDAT T1 ";
            if ($model->Properties["use_prg_schoolkind"] == "1") {
                $query .= "INNER JOIN SCHREG_REGD_GDAT T2 ON T2.YEAR = T1.YEAR AND T2.GRADE = T1.GRADE ";
                if ($model->selectSchoolKind) {
                    $query .= "                              AND T2.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
                }
            } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
                $query .= "INNER JOIN SCHREG_REGD_GDAT T2 ON T2.YEAR = T1.YEAR AND T2.GRADE = T1.GRADE ";
                $query .= " AND T2.SCHOOL_KIND = '".SCHOOLKIND."' ";
            }
            $query .= "WHERE ";
            $query .= "    T1.YEAR='".CTRL_YEAR."' AND ";
            $query .= "    SEMESTER='". CTRL_SEMESTER ."' ";
            $query .= " ORDER BY ";
            $query .= "     VALUE ";
        }
        //参照・更新可（制限付き）
        if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT){
            $query  = "SELECT ";
            $query .= "    T1.GRADE || '_' ||  HR_CLASS AS VALUE, ";
            $query .= "    HR_NAME AS LABEL ";
            $query .= "FROM ";
            $query .= "    SCHREG_REGD_HDAT T1 ";
            if ($model->Properties["use_prg_schoolkind"] == "1") {
                $query .= "INNER JOIN SCHREG_REGD_GDAT T2 ON T2.YEAR = T1.YEAR AND T2.GRADE = T1.GRADE ";
                if ($model->selectSchoolKind) {
                    $query .= " AND T2.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
                }
            } else             if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
                $query .= "INNER JOIN SCHREG_REGD_GDAT T2 ON T2.YEAR = T1.YEAR AND T2.GRADE = T1.GRADE ";
                $query .= " AND T2.SCHOOL_KIND = '".SCHOOLKIND."' ";
            }
            $query .= "WHERE ";
            $query .= "    T1.YEAR='".CTRL_YEAR."' AND ";
            $query .= "    SEMESTER='". CTRL_SEMESTER ."' AND ";
            $query .= "       (TR_CD1 = '" .STAFFCD ."' ";
            $query .= "     OR TR_CD2 = '" .STAFFCD ."' ";
            $query .= "     OR TR_CD3 = '" .STAFFCD ."') ";
            $query .= " ORDER BY ";
            $query .= "     VALUE ";
        }

        return $query;
    }

    //対象外の生徒取得
    function getSchnoIdou($model)
    {
        $date = str_replace("/","-",$model->date);

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

    //リストtoリスト右（個人）
    function getStudent_right($model) {
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
        $query .= "     T1.SEMESTER = '". CTRL_SEMESTER ."' AND ";
        $query .= "     T1.GRADE || '_' || T1.HR_CLASS = '{$model->grade_hr_class}' ";
        $query .= " ORDER BY ";
        $query .= "     T1.ATTENDNO ";

        return $query;
    }

    //リストtoリスト左（個人）
    function getStudent_left($model) {
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
        $query .= " ORDER BY ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.HR_CLASS, ";
        $query .= "     T1.ATTENDNO ";

        return $query;
    }

    //リストtoリスト右（学年）
    function getGrade_right($model) {
        $query  = " SELECT ";
        $query .= "     GRADE AS VALUE, ";
        $query .= "     GRADE_NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " AND SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        } else         if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= "     AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        }

        return $query;
    }

    //リストtoリスト左（学年）
    function getGrade_left($model) {
        $query  = " SELECT ";
        $query .= "     GRADE AS VALUE, ";
        $query .= "     GRADE_NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND GRADE IN ('" .str_replace(",", "','", $model->selectdata_l) ."') ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " AND SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= "     AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        }

        return $query;
    }

    //学校種別取得
    function getSchoolKind() {
        $query  = " SELECT ";
        $query .= "     NAME1 ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'A023' ";
        $query .= " ORDER BY ";
        $query .= "     NAMECD2 ";

        return $query;
    }

    //入学・卒業履歴情報取得
    function getSchregEntGrdHistDat($name, $schregno, $schoolkind) {
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
    function getFinSchoolMst($model, $finschoolcd) {
        $query  = " SELECT ";
        if ($model->output == OUT_CODE_NAME){           //コード＋名称
            $query .= " VALUE(FINSCHOOLCD,'') || ',' || VALUE(FINSCHOOL_NAME,'') ";
        } else if ($model->output == OUT_CODE_ONLY){    //コードのみ
            $query .= " FINSCHOOLCD ";
        } else if ($model->output == OUT_NAME_ONLY){    //名称のみ
            $query .= " FINSCHOOL_NAME ";
        }
        $query .= " FROM ";
        $query .= "     FINSCHOOL_MST ";
        $query .= " WHERE ";
        $query .= "     FINSCHOOLCD = '".$finschoolcd."' ";

        return $query;
    }

    //名称マスタより取得
    function getNameMst($model, $namecd1, $namecd2) {
        $query  = " SELECT ";
        if ($model->output == OUT_CODE_NAME){           //コード＋名称
            $query .= " VALUE(NAMECD2,'') || ',' || VALUE(NAME1,'') ";
        } else if ($model->output == OUT_CODE_ONLY){    //コードのみ
            $query .= " NAMECD2 ";
        } else if ($model->output == OUT_NAME_ONLY){    //名称のみ
            $query .= " NAME1 ";
        }
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = '".$namecd1."' AND ";
        $query .= "     NAMECD2 = '".$namecd2."' ";

        return $query;
    }

    //書出し項目をDELETEする
    function getDeleteKakidashi($db, $model) {
        //DELETE
        $query  = "  DELETE ";
        $query .= "  FROM ";
        $query .= "      KNJI100A_KAKIDASHI_LIST ";
        $query .= "  WHERE ";
        $query .= "      YEAR = '".CTRL_YEAR."' ";
        $query .= "  AND DATA_DIV = '00' ";
        $db->query($query);

        return;
    }
    
    //書出し項目をINSERTする
    function getUpdateKakidashi($db, $model) {
        $item = explode(",", $model->selectdata_r);
        
        //INSERT
        $data = array();
        foreach ($item as $fieldName) {
            $data[$fieldName][TEXT] = '1';
        }
        $data["YEAR"][TEXT]             = CTRL_YEAR;
        $data["DATA_DIV"][TEXT]         = '00';
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][NUMBER]        = "sysdate()";

        $query = Query::insertSQL($data, "KNJI100A_KAKIDASHI_LIST");
        $db->query($query);

        return;
    }
    
}
?>
