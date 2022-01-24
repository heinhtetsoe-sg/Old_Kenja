<?php

require_once('for_php7.php');

class knja233Query extends Query {

    function getSecurityHigh() {
        $query  = " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     MENU_HIGH_SECURITY_MST ";
        $query .= " WHERE ";
        $query .= "     PROGRAMID = 'KNJA233' ";
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

    //リストに色をつける項目を選択
    function SQLGet_Main_Color($model) {
        $query  = " SELECT ";
        $query .= "     T5.CHAIRCD, ";
        $query .= "     MAX(T8.APPENDDATE) AS APPENDDATE ";
        $query .= " FROM ";

        $query .= "     (SELECT ";
        $query .= "         TT1.CHAIRCD, ";
        $query .= "         TT1.YEAR ";
        $query .= "     FROM ";
        $query .= "         SCH_CHR_DAT TT1 , ";
        $query .= "         (SELECT ";
        $query .= "             SDATE, ";
        $query .= "             EDATE ";
        $query .= "         FROM ";
        $query .= "             SEMESTER_MST ";
        $query .= "         WHERE ";
        $query .= "             YEAR     = '{$model->year}' AND ";
        $query .= "             SEMESTER = '{$model->semester}' ";
        $query .= "         ) TT2 ";
        $query .= "     WHERE ";
        $query .= "         TT1.YEAR = '{$model->year}' AND ";
        $query .= "         TT1.PERIODCD > '0' AND ";
        $query .= "         TT1.EXECUTEDATE BETWEEN TT2.SDATE AND ";
        $query .= "         TT2.EDATE ";
        $query .= "     GROUP BY ";
        $query .= "         TT1.CHAIRCD, ";
        $query .= "         TT1.YEAR ";
        $query .= "     ) T1 ";

        $query .= " INNER JOIN ";
        $query .= "     CHAIR_DAT T5 ON T5.YEAR = T1.YEAR AND T5.CHAIRCD = T1.CHAIRCD ";
        $query .= "               AND T5.SEMESTER = '" .$model->semester ."', ";     //講座データ

        $query .= "     (SELECT ";
        $query .= "         CHAIRCD, ";
        $query .= "         APPENDDATE ";
        $query .= "     FROM ";
        $query .= "         CHAIR_STD_DAT ";
        $query .= "     WHERE ";
        $query .= "         YEAR     = '{$model->year}' AND ";
        $query .= "         SEMESTER = '{$model->semester}' ";
        $query .= "     GROUP BY ";
        $query .= "         CHAIRCD, ";
        $query .= "         APPENDDATE ";
        $query .= "     ) T8 ";

        $query .= " WHERE ";
        $query .= "     T8.CHAIRCD    = T1.CHAIRCD AND ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "    T5.CLASSCD       = '" .substr($model->subclasscd[0], 0, 2) ."' AND ";
            $query .= "    T5.SCHOOL_KIND   = '" .substr($model->subclasscd[0], 3, 1) ."' AND ";
            $query .= "    T5.CURRICULUM_CD = '" .substr($model->subclasscd[0], 5, 1) ."' AND ";
            $query .= "    T5.SUBCLASSCD    = '" .substr($model->subclasscd[0], 7, 6) ."' ";
        } else {
            $query .= "     T5.SUBCLASSCD = '{$model->subclasscd[0]}' ";
        }
        $query .= " GROUP BY ";
        $query .= "     T5.CHAIRCD ";

        return $query;
    }

    //V_科目担任データ取得
    function SQLGet_Main($model)
    {
        $query  = "SELECT DISTINCT ";
        $query .= "    T3.CLASSCD, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "         T5.SCHOOL_KIND, ";
            $query .= "         T5.CURRICULUM_CD, ";
        }
        $query .= "    T5.SUBCLASSCD, ";
        $query .= "    T5.CHAIRCD ATTENDCLASSCD, ";
        $query .= "    T3.CLASSNAME, ";
        $query .= "    T4.SUBCLASSNAME, ";
        $query .= "    (CASE WHEN T5.GROUPCD='0000' THEN (T7.HR_NAME) ELSE (T7.HR_NAME || '*') END )TARGETCLASS, ";
        $query .= "    T1.STARTDAY, ";
        $query .= "    T1.ENDDAY, ";
        $query .= "    T5.GROUPCD, ";
        $query .= "    T5.CHAIRNAME GROUPNAME, ";
        $query .= "    T6.STAFFNAME_SHOW, ";
        $query .= "    T6.STAFFCD, ";
        $query .= "    T7.GRADE || T7.HR_CLASS, ";
        $query .= "    T6.CHARGEDIV, ";
        $query .= "    T8.appdate, ";
        $query .= "    T8.APPENDDATE, ";
        $query .= "    T5.CHAIRNAME AS CLASSALIAS ";
        $query .= "FROM ";
        $query .= "    (SELECT ";
        $query .= "        TT1.CHAIRCD, ";
        $query .= "        TT1.YEAR, ";
        $query .= "        MIN(DATE(TT1.EXECUTEDATE)) STARTDAY, ";
        $query .= "        MAX(DATE(TT1.EXECUTEDATE)) ENDDAY ";
        $query .= "    FROM ";
        $query .= "        SCH_CHR_DAT   TT1  ,";   //時間割データ
        $query .= "         (SELECT";
        $query .= "             SDATE,";
        $query .= "             EDATE";
        $query .= "         FROM";
        $query .= "             SEMESTER_MST";
        $query .= "         WHERE";
        $query .= "             YEAR     = '" .$model->year ."' AND ";
        $query .= "             SEMESTER = '" .$model->semester ."'";
        $query .= "         ) TT2";
        $query .= "    WHERE ";
        $query .= "        TT1.YEAR = '" .$model->year ."' AND ";
        $query .= "        TT1.PERIODCD > '0' AND ";
        $query .= "         TT1.EXECUTEDATE BETWEEN TT2.SDATE AND TT2.EDATE ";
        $query .= "    GROUP BY ";
        $query .= "        TT1.CHAIRCD,TT1.YEAR ";
        $query .= "    ) T1 ";
        $query .= "    INNER JOIN CHAIR_DAT T5 ON T5.YEAR = T1.YEAR AND T5.CHAIRCD = T1.CHAIRCD ";     //講座データ
        $query .= "          AND T5.SEMESTER = '" .$model->semester ."', ";     //講座データ
        $query .= "    (SELECT DISTINCT ";
        $query .= "        TT3.CHAIRCD,TT4.STAFFCD,TT4.STAFFNAME_SHOW,TT3.CHARGEDIV ";  //2004/06/30 nakamoto
        $query .= "     FROM   ";
        $query .= "        CHAIR_STF_DAT TT3,   ";  //講座担当職員データ
        $query .= "        STAFF_MST TT4 ";   //職員マスタ
        $query .= "    WHERE ";
        $query .= "        TT3.YEAR = '" .$model->year ."' AND ";
        $query .= "        TT3.SEMESTER   = '" .$model->semester ."' AND ";
        //制限付き参照・更新可は自分が受け持つクラスのみ
        if ($model->auth == DEF_REFER_RESTRICT || $model->auth == DEF_UPDATE_RESTRICT){
            $query .= "  TT3.STAFFCD  = '" .$model->staffcd ."' AND ";
        }elseif ($model->auth == DEF_NOAUTH){
            $query .= "     TT3.STAFFCD    IS NULL AND";
        }
        $query .= "        TT3.STAFFCD    = TT4.STAFFCD  ";
        $query .= "    ) T6, ";

        $query .= "    (SELECT ";
        $query .= "        WK1.chaircd, ";
        $query .= "        SUBSTR(MIN(WK1.trgtgrade || WK1.trgtclass),1,2) AS trgtgrade, ";
        $query .= "        SUBSTR(MIN(WK1.trgtgrade || WK1.trgtclass),3,3) AS trgtclass ";
        $query .= "    FROM ";
        $query .= "        (SELECT ";
        $query .= "            k1.year, k1.semester, k2.chaircd, k1.trgtgrade, k1.trgtclass ";
        $query .= "        FROM ";
        $query .= "            chair_cls_dat k1, ";
        $query .= "            chair_dat k2 ";
        $query .= "        WHERE ";
        $query .= "            k1.year       = k2.year AND ";
        $query .= "            k1.semester   = k2.semester AND ";
        $query .= "            k1.chaircd    = '0000000' AND ";
        $query .= "            k1.year       = '" .$model->year ."' AND ";
        $query .= "            k1.semester   = '" .$model->semester ."' AND ";
        $query .= "            k1.groupcd    = k2.groupcd ";
        $query .= "        UNION   ";
        $query .= "        SELECT ";
        $query .= "            t1.year, t1.semester, t2.chaircd, t1.trgtgrade, t1.trgtclass ";
        $query .= "        FROM ";
        $query .= "            chair_cls_dat t1, ";
        $query .= "            chair_dat t2 ";
        $query .= "        WHERE ";
        $query .= "            t1.year       = t2.year AND ";
        $query .= "            t1.semester   = t2.semester AND ";
        $query .= "            t1.groupcd    = t2.groupcd AND ";
        $query .= "            t1.year       = '" .$model->year ."' AND ";
        $query .= "            t1.semester   = '" .$model->semester ."' AND ";
        $query .= "            t1.chaircd    = t2.chaircd ";
        $query .= "        ) WK1 ";
        $query .= "    GROUP BY ";
        $query .= "        WK1.chaircd ";
        $query .= "    ) T2, ";

        $query .= "    (SELECT ";
        $query .= "        chaircd,appdate,max(APPENDDATE) as APPENDDATE ";
        $query .= "    FROM ";
        $query .= "        chair_std_dat ";
        $query .= "    WHERE ";
        $query .= "        year='" .$model->year ."' AND semester='" .$model->semester ."'  ";
        $query .= "    GROUP BY ";
        $query .= "        chaircd,appdate ";
        $query .= "    ) T8, ";

        $query .= "    CLASS_MST T3, ";  //教科マスタ
        $query .= "    SUBCLASS_MST T4, ";    //科目マスタ
        $query .= "    SCHREG_REGD_HDAT T7 "; //学籍在籍ヘッダデータ
        $query .= "WHERE ";
        $query .= "    T2.CHAIRCD    = T1.CHAIRCD AND ";
        $query .= "    T6.CHAIRCD    = T1.CHAIRCD AND ";
        $query .= "    T8.CHAIRCD    = T1.CHAIRCD AND ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "    T5.SCHOOL_KIND   = '" .substr($model->subclasscd[0], 3, 1) ."' AND ";
            $query .= "    T5.CURRICULUM_CD = '" .substr($model->subclasscd[0], 5, 1) ."' AND ";
            $query .= "    T5.SUBCLASSCD    = '" .substr($model->subclasscd[0], 7, 6) ."' AND ";
        } else {
            $query .= "    T5.SUBCLASSCD = '" .$model->subclasscd[0] ."' AND ";
        }
        $query .= "    T3.CLASSCD    = SUBSTR(T5.SUBCLASSCD,1,2) AND ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "    T3.CLASSCD    = T4.CLASSCD  AND ";
            $query .= "    T3.SCHOOL_KIND = T4.SCHOOL_KIND  AND ";
            $query .= "    T4.SCHOOL_KIND = T5.SCHOOL_KIND  AND ";
            $query .= "    T4.CURRICULUM_CD = T5.CURRICULUM_CD  AND ";
        }
        $query .= "    T4.SUBCLASSCD = T5.SUBCLASSCD AND ";
        $query .= "    T7.YEAR       = '" .$model->year ."' AND ";
        $query .= "    T7.SEMESTER   = '" .$model->semester ."' AND ";
        $query .= "    T2.TRGTGRADE  = T7.GRADE AND ";
        $query .= "    T2.TRGTCLASS  = T7.HR_CLASS ";
        $query .= "ORDER BY ";
        $query .= "    1, ";
        $query .= "    2, ";
        $query .= "    13, ";
        $query .= "    12, ";
        $query .= "    7 ";
        
        return $query;
    }

    function getCair($chair, $model){
        $query  = "SELECT ";
        $query .= "    CHAIRNAME ";
        $query .= "FROM ";
        $query .= "    CHAIR_DAT ";
        $query .= "WHERE ";
        $query .= "    YEAR = '".$model->year."' ";
        $query .= "    AND SEMESTER = '".$model->semester."' ";
        $query .= "    AND CHAIRCD = '".$chair."' ";

        return $query;
    }

    function getStaff($staff){
        $query  = "SELECT ";
        $query .= "    STAFFNAME ";
        $query .= "FROM ";
        $query .= "    STAFF_MST ";
        $query .= "WHERE ";
        $query .= "    STAFFCD = '".$staff."' ";

        return $query;
    }

    function Csvdata_get($opt, $chair, $app, $model)
    {
        $query  = "SELECT ";
        $query .= "    T1.SCHREGNO, ";
        $query .= "    CASE WHEN T1.SEX = '2' THEN '*' ELSE '' END AS SEX, ";
        $query .= "    value(T1.NAME,'') NAME, ";
        $query .= "    value(T1.NAME_KANA,'') NAME_KANA, ";
        $query .= "    value(T6.HR_NAMEABBV,'') HR_NAMEABBV, ";
        $query .= "    value(T2.GRADE,'') GRADE, ";
        $query .= "    value(T2.HR_CLASS,'') HR_CLASS, ";
        $query .= "    value(T2.ATTENDNO,'') ATTENDNO ";
        $query .= "FROM ";
        $query .= "    CHAIR_STD_DAT T7, ";
        $query .= "    SCHREG_BASE_MST T1, ";
        $query .= "    SCHREG_REGD_DAT T2, ";
        $query .= "    SCHREG_REGD_HDAT T6 ";
        $query .= "WHERE ";
        $query .= "    T7.YEAR = '".$model->year."' AND ";
        $query .= "    T7.SEMESTER = '".$model->semester."' AND ";
        $query .= "    T7.CHAIRCD ='".$chair."' AND ";
        $query .= "    T7.APPDATE ='".$app."' AND ";
        $query .= "    T1.SCHREGNO = T7.SCHREGNO AND ";
        $query .= "    T2.SCHREGNO = T7.SCHREGNO AND ";
        $query .= "    T2.YEAR = T7.YEAR AND ";
        $query .= "    T2.SEMESTER = T7.SEMESTER AND ";
        $query .= "    T6.YEAR = T2.YEAR AND ";
        $query .= "    T6.SEMESTER = T2.SEMESTER AND ";
        $query .= "    T6.GRADE = T2.GRADE AND ";
        $query .= "    T6.HR_CLASS = T2.HR_CLASS ";
        $query .= "ORDER BY ";
        if ($opt == 1 || $opt == 'musashi'){
            $query .= "    T2.GRADE,T2.HR_CLASS,T2.ATTENDNO ";
        }else {
            $query .= "    T1.SCHREGNO ";
        }

        return $query;
    }

    //縦・横サイズのデフォルト値取得
    function getDefaultSize($div)
    {
        $query  = " SELECT ";
        if($div == "HEIGHT"){
            $query .= "     NAME1 ";
        } else {
            $query .= "     NAME2 ";
        }
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     NAMECD1 = 'A026' AND ";
        $query .= "     NAMECD2 = '00' ";

        return $query;
    }
    //学校名取得の取得(
    function getSchoolName() {
        $query  = " SELECT ";
        $query .= "     NAME1, ";
        $query .= "     NAME2 ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'Z010' AND ";
        $query .= "     NAMECD2 = '00' ";

        return $query;
    }
}
?>