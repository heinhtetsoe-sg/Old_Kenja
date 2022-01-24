<?php

require_once('for_php7.php');

class knjc053Query extends Query
{

    //V_科目担任データ取得
    public function SQLGet_Main($model)
    {
        $query  = "SELECT DISTINCT ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "    T5.CLASSCD, ";
            $query .= "    T5.SCHOOL_KIND, ";
            $query .= "    T5.CURRICULUM_CD, ";
            $query .= "    T5.SUBCLASSCD, ";
        } else {
            $query .= "    SUBSTR(T5.SUBCLASSCD,1,2) CLASSCD, ";
            $query .= "    T5.SUBCLASSCD, ";
        }
        $query .= "    T5.CHAIRCD ATTENDCLASSCD, ";
        $query .= "    T3.CLASSNAME, ";
        $query .= "    T4.SUBCLASSNAME, ";
        $query .= "    (CASE WHEN T5.GROUPCD='0000' THEN (T7.HR_NAME) ELSE (T7.HR_NAME || '*') END )TARGETCLASS, ";
        $query .= "    T1.STARTDAY, ";
        $query .= "    T1.ENDDAY, ";
        $query .= "    T5.GROUPCD, ";
        $query .= "    T5.CHAIRNAME GROUPNAME, ";
        $query .= "    T6.STAFFCD, ";
        $query .= "    T8.appdate, ";
        $query .= "    T6.STAFFNAME_SHOW ";
        $query .= "FROM ";
        $query .= "    (SELECT ";
        $query .= "        TT1.CHAIRCD, ";
        $query .= "        TT2.STAFFCD, ";
        $query .= "        TT1.YEAR, ";
        $query .= "        MIN(DATE(TT1.EXECUTEDATE)) STARTDAY, ";
        $query .= "        MAX(DATE(TT1.EXECUTEDATE)) ENDDAY ";
        $query .= "    FROM ";
        $query .= "        SCH_CHR_DAT   TT1,  ";    //時間割データ
        $query .= "        CHAIR_STF_DAT TT2,   ";    //講座担当職員データ
        $query .= "         (SELECT";
        $query .= "             SDATE,";
        $query .= "             EDATE";
        $query .= "         FROM";
        $query .= "             SEMESTER_MST";
        $query .= "         WHERE";
        $query .= "             YEAR     = '" .$model->year ."' AND ";
        $query .= "             SEMESTER = '" .$model->semester ."'";
        $query .= "         ) TT3";
        $query .= "    WHERE ";
        $query .= "        TT1.YEAR = '" .$model->year ."' AND ";
        $query .= "        TT1.YEAR = TT2.YEAR AND ";
        $query .= "        TT2.SEMESTER = '" .$model->semester ."' AND ";
        $query .= "        TT1.CHAIRCD = TT2.CHAIRCD AND ";
        $query .= "         TT1.EXECUTEDATE BETWEEN TT3.SDATE AND TT3.EDATE ";
        //制限付き参照・更新可は自分が受け持つクラスのみ
        if (AUTHORITY == DEF_UPDATE_RESTRICT || AUTHORITY == DEF_REFER_RESTRICT) {
            $query .= "  AND TT2.STAFFCD  = '" .STAFFCD ."' ";
        } elseif (AUTHORITY == DEF_NOAUTH) {        //権限なし(表示不可)
            $query .= "  AND TT2.STAFFCD  IS NULL ";
        }
        $query .= "    GROUP BY ";
        $query .= "        TT1.CHAIRCD,TT2.STAFFCD,TT1.YEAR ";
        $query .= "    ) T1 ";
        $query .= "    INNER JOIN CHAIR_DAT T5 ON T5.YEAR = T1.YEAR AND T5.SEMESTER   = '" .$model->semester ."' AND T5.CHAIRCD = T1.CHAIRCD , ";     //講座データ
        $query .= "    (SELECT ";
        $query .= "        WK1.chaircd, ";
        $query .= "        SUBSTR(MIN(WK1.trgtgrade || WK1.trgtclass),1,2) AS trgtgrade, ";
        $query .= "        SUBSTR(MIN(WK1.trgtgrade || WK1.trgtclass),3,abs(length(MIN(WK1.trgtgrade || WK1.trgtclass))-length(MIN(WK1.trgtgrade)))) AS trgtclass ";  //05/06/28 yamashiro(f) Modify
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
        $query .= "        chaircd,appdate ";
        $query .= "    FROM ";
        $query .= "        chair_std_dat ";
        $query .= "    WHERE ";
        $query .= "        year='" .$model->year ."' AND semester='" .$model->semester ."'  ";
        $query .= "    GROUP BY ";
        $query .= "        chaircd,appdate ";
        $query .= "    ) T8, ";
        //---------------------------------
        $query .= "    CLASS_MST T3, ";       //教科マスタ
        $query .= "    SUBCLASS_MST T4, ";    //科目マスタ
        $query .= "    STAFF_MST T6, ";       //職員マスタ
        $query .= "    SCHREG_REGD_HDAT T7 "; //学籍在籍ヘッダデータ
        $query .= "WHERE ";
        $query .= "    T2.CHAIRCD    = T1.CHAIRCD AND ";
        $query .= "    T1.CHAIRCD IN ('" .implode($model->checked_attend, "','") ."') AND ";
        $query .= "    T8.CHAIRCD    = T1.CHAIRCD AND ";
        if ($model->cmd == "main" && isset($model->subclasscd) && $model->disp != "TEST") {
            if ($model->Properties["useCurriculumcd"] == "1") {
                $query .= "    T5.CLASSCD = '" .$model->classcd[0] ."' AND ";
                $query .= "    T5.SCHOOL_KIND = '" .$model->school_kind[0] ."' AND ";
                $query .= "    T5.CURRICULUM_CD = '" .$model->curriculum_cd[0] ."' AND ";
                $query .= "    T5.SUBCLASSCD = '" .$model->subclasscd[0] ."' AND ";
            } else {
                $query .= "    T5.SUBCLASSCD = '" .$model->subclasscd[0] ."' AND ";
            }
        }
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "    T3.CLASSCD = T5.CLASSCD AND ";
            $query .= "    T3.SCHOOL_KIND = T5.SCHOOL_KIND AND ";
            $query .= "    T4.CLASSCD = T5.CLASSCD AND ";
            $query .= "    T4.SCHOOL_KIND = T5.SCHOOL_KIND AND ";
            $query .= "    T4.CURRICULUM_CD = T5.CURRICULUM_CD AND ";
            $query .= "    T4.SUBCLASSCD = T5.SUBCLASSCD AND ";
        } else {
            $query .= "    T3.CLASSCD    = SUBSTR(T5.SUBCLASSCD,1,2) AND ";
            $query .= "    T4.SUBCLASSCD = T5.SUBCLASSCD AND ";
        }
        $query .= "    T1.STAFFCD    = '" .$model->checked_staff ."' AND ";
        $query .= "    T8.APPDATE    = '" .$model->checked_appd ."' AND ";
        $query .= "    T6.STAFFCD    = T1.STAFFCD AND ";
        $query .= "    T7.YEAR       = '" .$model->year ."' AND ";
        $query .= "    T7.SEMESTER   = '" .$model->semester ."' AND ";
        $query .= "    T2.TRGTGRADE  = T7.GRADE AND ";
        $query .= "    T2.TRGTCLASS  = T7.HR_CLASS ";
        $query .= "ORDER BY ";
        $query .= "    1, ";
        $query .= "    2, ";
        $query .= "    6, ";
        $query .= "    7 ";

        return $query;
    }

    //日付取得
    public function SQLGet_Date($model, $chair)
    {
        $query  = " WITH ATABLE as ( ";
        $query .= " SELECT ";
        $query .= "     CHAIRCD ";
        $query .= " FROM ";
        $query .= "     CHAIR_STD_DAT T1 ";
        $query .= " WHERE ";
        $query .= "     YEAR = '" .$model->year ."' AND ";
        $query .= "     SEMESTER = '" .$model->semester ."' AND ";
        $query .= "     EXISTS(SELECT ";
        $query .= "                 SCHREGNO ";
        $query .= "             FROM ";
        $query .= "                 CHAIR_STD_DAT T2 ";
        $query .= "             WHERE ";
        $query .= "                 YEAR = '" .$model->year ."' AND ";
        $query .= "                 SEMESTER = '" .$model->semester ."' AND ";
        $query .= "                 CHAIRCD = '" .$chair ."' AND ";
        $query .= "                 T2.SCHREGNO = T1.SCHREGNO ";
        $query .= "             GROUP BY ";
        $query .= "                 SCHREGNO ";
        $query .= "             ) AND ";
        $query .= "     EXISTS(SELECT ";
        $query .= "                 'X' ";
        $query .= "             FROM ";
        $query .= "                 CHAIR_DAT T2 ";
        $query .= "             WHERE ";
        $query .= "                 YEAR = '" .$model->year ."' AND ";
        $query .= "                 SEMESTER = '" .$model->semester ."' AND ";
        $query .= "                 T2.CHAIRCD = T1.CHAIRCD AND ";
        $query .= "                 EXISTS(SELECT ";
        $query .= "                             'X' ";
        $query .= "                         FROM ";
        $query .= "                             CHAIR_DAT T3 ";
        $query .= "                         WHERE ";
        $query .= "                             YEAR = '" .$model->year ."' AND ";
        $query .= "                             SEMESTER = '" .$model->semester ."' AND ";
        $query .= "                             CHAIRCD = '" .$chair ."' AND ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "                             T3.CLASSCD = T2.CLASSCD AND ";
            $query .= "                             T3.SCHOOL_KIND = T2.SCHOOL_KIND AND ";
            $query .= "                             T3.CURRICULUM_CD = T2.CURRICULUM_CD AND ";
        }
        $query .= "                             T3.SUBCLASSCD = T2.SUBCLASSCD ";
        $query .= "                         ) ";
        $query .= "             ) ";
        $query .= " GROUP BY ";
        $query .= "     CHAIRCD ) ";
        $query .= " SELECT MIN(EXECUTEDATE) as STARTDAY,MAX(EXECUTEDATE) as ENDDAY ";
        $query .= " FROM ";
        $query .= "     SCH_CHR_DAT t1,ATABLE t2 ";
        $query .= " WHERE ";
        $query .= "     t1.CHAIRCD = t2.CHAIRCD AND ";
        $query .= "     t1.YEAR = '" .$model->year ."' ";

        return $query;
    }

    //遅刻早退データ取得
    public function GetSchoolmst()
    {
        $query  = " SELECT ABSENT_COV_LATE ";
        $query .= " FROM ";
        $query .= "     SCHOOL_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";

        return $query;
    }
}
