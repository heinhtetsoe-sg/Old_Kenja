<?php

require_once('for_php7.php');

class knja233aQuery extends Query
{
    public function getSecurityHigh()
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     MENU_HIGH_SECURITY_MST ";
        $query .= " WHERE ";
        $query .= "     PROGRAMID = 'KNJA233A' ";
        $query .= "     AND INVALID_FLG = '0' ";

        return $query;
    }

    public function getSchoolCd()
    {
        $query  = " SELECT ";
        $query .= "     NAME2 ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'Z010' ";
        $query .= "     AND NAMECD2 = '00' ";

        return $query;
    }

    //講座
    public function getChairDat($model)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     T3.CLASSCD, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "         T5.SCHOOL_KIND, ";
            $query .= "         T5.CURRICULUM_CD, ";
        }
        $query .= "     T5.SUBCLASSCD, ";
        $query .= "     T5.CHAIRCD ATTENDCLASSCD, ";
        $query .= "     T3.CLASSNAME, ";
        $query .= "     T4.SUBCLASSNAME, ";
        $query .= "     (CASE WHEN T5.GROUPCD='0000' THEN (T7.HR_NAME) ELSE (T7.HR_NAME || '*') END )TARGETCLASS, ";
        $query .= "     T5.GROUPCD, ";
        $query .= "     T5.CHAIRNAME GROUPNAME, ";
        $query .= "     T6.STAFFNAME_SHOW, ";
        $query .= "     T6.STAFFCD, ";
        $query .= "     T7.GRADE || T7.HR_CLASS AS GRADE_HR_CLASS, ";
        $query .= "     T6.CHARGEDIV, ";
        $query .= "     T1.APPDATE, ";
        $query .= "     T1.APPENDDATE, ";
        $query .= "     T5.CHAIRNAME AS CLASSALIAS ";
        $query .= " FROM ";

        $query .= "     (SELECT ";
        $query .= "         YEAR, ";
        $query .= "         CHAIRCD, ";
        $query .= "         APPDATE, ";
        $query .= "         MAX(APPENDDATE) AS APPENDDATE ";
        $query .= "     FROM ";
        $query .= "         CHAIR_STD_DAT ";
        $query .= "     WHERE ";
        $query .= "         YEAR     = '{$model->year}' AND ";
        $query .= "         SEMESTER = '{$model->field["SEMESTER"]}' AND ";
        $query .= "         '". str_replace("/", "-", $model->field["DATE"]) ."' BETWEEN APPDATE AND APPENDDATE  ";
        $query .= "     GROUP BY ";
        $query .= "         YEAR, ";
        $query .= "         CHAIRCD, ";
        $query .= "         APPDATE ";
        $query .= "     ) T1 ";

        $query .= "     INNER JOIN CHAIR_DAT T5 ON T5.YEAR = T1.YEAR AND T5.CHAIRCD = T1.CHAIRCD ";     //講座データ
        $query .= "           AND T5.SEMESTER = '{$model->field["SEMESTER"]}', ";     //講座データ
        $query .= "     (SELECT DISTINCT ";
        $query .= "         TT3.CHAIRCD,TT4.STAFFCD,TT4.STAFFNAME_SHOW,TT3.CHARGEDIV ";  //2004/06/30 nakamoto
        $query .= "      FROM   ";
        $query .= "         CHAIR_STF_DAT TT3,   ";  //講座担当職員データ
        $query .= "         STAFF_MST TT4 ";   //職員マスタ
        $query .= "     WHERE ";
        $query .= "         TT3.YEAR = '{$model->year}' AND ";
        $query .= "         TT3.SEMESTER   = '{$model->field["SEMESTER"]}' AND ";
        //制限付き参照・更新可は自分が受け持つクラスのみ
        if ($model->auth == DEF_REFER_RESTRICT || $model->auth == DEF_UPDATE_RESTRICT) {
            $query .= "     TT3.STAFFCD  = '". STAFFCD ."' AND ";
        } elseif ($model->auth == DEF_NOAUTH) {
            $query .= "     TT3.STAFFCD    IS NULL AND";
        }
        $query .= "         TT3.STAFFCD    = TT4.STAFFCD  ";
        $query .= "    ) T6, ";

        $query .= "     (SELECT ";
        $query .= "         WK1.chaircd, ";
        $query .= "         SUBSTR(MIN(WK1.trgtgrade || WK1.trgtclass),1,2) AS trgtgrade, ";
        $query .= "         SUBSTR(MIN(WK1.trgtgrade || WK1.trgtclass),3,3) AS trgtclass ";
        $query .= "     FROM ";
        $query .= "         (SELECT ";
        $query .= "             k1.year, k1.semester, k2.chaircd, k1.trgtgrade, k1.trgtclass ";
        $query .= "         FROM ";
        $query .= "             chair_cls_dat k1, ";
        $query .= "             chair_dat k2 ";
        $query .= "         WHERE ";
        $query .= "             k1.year       = k2.year AND ";
        $query .= "             k1.semester   = k2.semester AND ";
        $query .= "             k1.chaircd    = '0000000' AND ";
        $query .= "             k1.year       = '{$model->year}' AND ";
        $query .= "             k1.semester   = '{$model->field["SEMESTER"]}' AND ";
        $query .= "             k1.groupcd    = k2.groupcd ";
        $query .= "         UNION   ";
        $query .= "         SELECT ";
        $query .= "             t1.year, t1.semester, t2.chaircd, t1.trgtgrade, t1.trgtclass ";
        $query .= "         FROM ";
        $query .= "             chair_cls_dat t1, ";
        $query .= "             chair_dat t2 ";
        $query .= "         WHERE ";
        $query .= "             t1.year       = t2.year AND ";
        $query .= "             t1.semester   = t2.semester AND ";
        $query .= "             t1.groupcd    = t2.groupcd AND ";
        $query .= "             t1.year       = '{$model->year}' AND ";
        $query .= "             t1.semester   = '{$model->field["SEMESTER"]}' AND ";
        $query .= "             t1.chaircd    = t2.chaircd ";
        $query .= "         ) WK1 ";
        $query .= "     GROUP BY ";
        $query .= "         WK1.chaircd ";
        $query .= "     ) T2, ";

        $query .= "     CLASS_MST T3, ";  //教科マスタ
        $query .= "     SUBCLASS_MST T4, ";    //科目マスタ
        $query .= "     SCHREG_REGD_HDAT T7 "; //学籍在籍ヘッダデータ
        $query .= " WHERE ";
        $query .= "     T2.CHAIRCD    = T1.CHAIRCD AND ";
        $query .= "     T6.CHAIRCD    = T1.CHAIRCD AND ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "    T5.SCHOOL_KIND   = '".substr($model->field["SUBCLASSCD"], 3, 1)."' AND ";
            $query .= "    T5.CURRICULUM_CD = '".substr($model->field["SUBCLASSCD"], 5, 1)."' AND ";
            $query .= "    T5.SUBCLASSCD    = '".substr($model->field["SUBCLASSCD"], 7, 6)."' AND ";
            $query .= "    T3.CLASSCD    = T4.CLASSCD  AND ";
            $query .= "    T3.SCHOOL_KIND = T4.SCHOOL_KIND  AND ";
            $query .= "    T4.SCHOOL_KIND = T5.SCHOOL_KIND  AND ";
            $query .= "    T4.CURRICULUM_CD = T5.CURRICULUM_CD  AND ";
        } else {
            $query .= "    T5.SUBCLASSCD = '{$model->field["SUBCLASSCD"]}' AND ";
        }
        $query .= "     T3.CLASSCD    = SUBSTR(T5.SUBCLASSCD,1,2) AND ";
        $query .= "     T4.SUBCLASSCD = T5.SUBCLASSCD AND ";
        $query .= "     T7.YEAR       = '{$model->year}' AND ";
        $query .= "     T7.SEMESTER   = '{$model->field["SEMESTER"]}' AND ";
        $query .= "     T2.TRGTGRADE  = T7.GRADE AND ";
        $query .= "     T2.TRGTCLASS  = T7.HR_CLASS ";
        $query .= " ORDER BY ";
        $query .= "     ATTENDCLASSCD, ";
        $query .= "     T6.CHARGEDIV, ";
        $query .= "     T6.STAFFCD ";
//        $query .= "     T3.CLASSCD, ";
//        $query .= "     T5.SUBCLASSCD, ";
//        $query .= "     GRADE_HR_CLASS, ";
//        $query .= "     T6.STAFFCD, ";
//        $query .= "     T1.APPDATE, ";
//        $query .= "     ATTENDCLASSCD ";

        return $query;
    }

    //学校名取得の取得(
    public function getSchoolName()
    {
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

    //職員情報の取得(
    public function getStaffInfo()
    {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     STAFF_DETAIL_SEQ_MST ";
        $query .= " WHERE ";
        $query .= "     STAFFCD = '".STAFFCD."' AND ";
        $query .= "     STAFF_SEQ = '001' ";
        return $query;
    }

    //通信制かチェックするフラグを取得する。
    public function getTuusinflg()
    {
        $query  = " SELECT ";
        $query .= "     Z001.NAMESPARE3 ";
        $query .= " FROM ";
        $query .= "     SCHOOL_MST SM ";
        $query .= "     LEFT JOIN NAME_MST Z001 ";
        $query .= "          ON Z001.NAMECD1 = 'Z001' ";
        $query .= "         AND Z001.NAMECD2 = SM.SCHOOLDIV ";
        $query .= " WHERE ";
        $query .= "         SM.YEAR        = '".CTRL_YEAR."' ";
        if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= "     AND SM.SCHOOLCD    = '".sprintf("%012d", SCHOOLCD)."' ";
            $query .= "     AND SM.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }

        return $query;
    }

    //学期取得
    public function getSemester($model)
    {
        $query  = "SELECT ";
        $query .= "    SEMESTERNAME AS LABEL, ";
        $query .= "    SEMESTER AS VALUE ";
        $query .= "FROM ";
        $query .= "    SEMESTER_MST ";
        $query .= "WHERE ";
        $query .= "    YEAR = '{$model->year}' AND ";
        $query .= "    SEMESTER <> '9' ";
        $query .= "ORDER BY ";
        $query .= "    SEMESTER ";

        return $query;
    }

    //教科取得
    public function getClassMst($model)
    {
        $query  = " SELECT DISTINCT ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     L1.CLASSCD || '-' || L1.SCHOOL_KIND || ':' || L1.CLASSNAME AS LABEL, ";
            $query .= "     L1.CLASSCD || '-' || L1.SCHOOL_KIND AS VALUE ";
        } else {
            $query .= "     L1.CLASSCD || ':' || L1.CLASSNAME AS LABEL, ";
            $query .= "     L1.CLASSCD AS VALUE ";
        }
        $query .= " FROM ";
        $query .= "     CHAIR_DAT T1 ";
        $query .= " LEFT JOIN ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     CLASS_MST L1 ON L1.CLASSCD = SUBSTR(T1.SUBCLASSCD, 1, 2) ";
            $query .= "                 AND L1.SCHOOL_KIND  = T1.SCHOOL_KIND ";
        } else {
            $query .= "     CLASS_MST L1 ON L1.CLASSCD = SUBSTR(SUBCLASSCD, 1, 2) ";
        }
        $query .= " INNER JOIN ";
        $query .= "     SUBCLASS_MST L2 ON L2.SUBCLASSCD = T1.SUBCLASSCD ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                 AND L2.CLASSCD      = T1.CLASSCD ";
            $query .= "                 AND L2.SCHOOL_KIND  = T1.SCHOOL_KIND ";
            $query .= "                 AND L2.CURRICULUM_CD = T1.CURRICULUM_CD ";
        }
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '{$model->year}' AND ";
        $query .= "     T1.SEMESTER = '{$model->field["SEMESTER"]}' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " AND T1.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind), "','")."') ";
            }
        } elseif ($model->Properties["useCurriculumcd"] == '1' && $model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND T1.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        //更新可能(制限付)
        if (AUTHORITY != DEF_UPDATABLE) {
            $query .= "    AND T1.CHAIRCD IN (SELECT ";
            $query .= "                          S1.CHAIRCD ";
            $query .= "                      FROM ";
            $query .= "                          CHAIR_STF_DAT S1 ";
            $query .= "                      WHERE ";
            $query .= "                          S1.YEAR = T1.YEAR ";
            $query .= "                      AND S1.SEMESTER = T1.SEMESTER ";
            $query .= "                      AND S1.CHAIRCD  = T1.CHAIRCD ";
            $query .= "                      AND S1.STAFFCD = '".STAFFCD."' ";
            $query .= "                     ) ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //科目取得
    public function getSubclassMst($model)
    {
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $class_array = array();
            $class_array = explode("-", $model->field["CLASSCD"]);
        }
        $query  = " SELECT DISTINCT ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     L1.CLASSCD || '-' || L1.SCHOOL_KIND || '-' || L1.CURRICULUM_CD || '-' || L1.SUBCLASSCD || ':' || L1.SUBCLASSNAME AS LABEL, ";
            $query .= "     L1.CLASSCD || '-' || L1.SCHOOL_KIND || '-' || L1.CURRICULUM_CD || '-' || L1.SUBCLASSCD AS VALUE ";
        } else {
            $query .= "     L1.SUBCLASSCD || ':' || L1.SUBCLASSNAME AS LABEL, ";
            $query .= "     L1.SUBCLASSCD AS VALUE ";
        }
        $query .= " FROM ";
        $query .= "     CHAIR_DAT T1 ";
        $query .= " INNER JOIN ";
        $query .= "     SUBCLASS_MST L1 ON L1.SUBCLASSCD = T1.SUBCLASSCD ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                 AND L1.CLASSCD      = T1.CLASSCD ";
            $query .= "                 AND L1.SCHOOL_KIND  = T1.SCHOOL_KIND ";
            $query .= "                 AND L1.CURRICULUM_CD = T1.CURRICULUM_CD ";
        }
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '{$model->year}' AND ";
        $query .= "     T1.SEMESTER = '{$model->field["SEMESTER"]}' AND ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T1.CLASSCD          = '".$class_array[0]."' AND ";
            $query .= "     T1.SCHOOL_KIND      = '".$class_array[1]."' ";
        } else {
            $query .= "     SUBSTR(T1.SUBCLASSCD, 1, 2) = '{$model->field["CLASSCD"]}' ";
        }
        //更新可能(制限付)
        if (AUTHORITY != DEF_UPDATABLE) {
            $query .= "    AND T1.CHAIRCD IN (SELECT ";
            $query .= "                          S1.CHAIRCD ";
            $query .= "                      FROM ";
            $query .= "                          CHAIR_STF_DAT S1 ";
            $query .= "                      WHERE ";
            $query .= "                          S1.YEAR = T1.YEAR ";
            $query .= "                      AND S1.SEMESTER = T1.SEMESTER ";
            $query .= "                      AND S1.CHAIRCD  = T1.CHAIRCD ";
            $query .= "                      AND S1.STAFFCD = '".STAFFCD."' ";
            $query .= "                     ) ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //縦・横サイズのデフォルト値取得
    public function getDefaultSize()
    {
        $query  = " SELECT ";
        $query .= "     NAME1 AS HEIGHT, ";
        $query .= "     NAME2 AS WIDTH ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     NAMECD1 = 'A026' AND ";
        $query .= "     NAMECD2 = '00' ";

        return $query;
    }

    /************************************/
    /********* 以下CSV関係 **************/
    /************************************/
    public function getCair($chair, $model)
    {
        $query  = "SELECT ";
        $query .= "    CHAIRNAME ";
        $query .= "FROM ";
        $query .= "    CHAIR_DAT ";
        $query .= "WHERE ";
        $query .= "    YEAR = '".$model->year."' ";
        $query .= "    AND SEMESTER = '".$model->field["SEMESTER"]."' ";
        $query .= "    AND CHAIRCD = '".$chair."' ";

        return $query;
    }

    public function getStaff($staff)
    {
        $query  = "SELECT ";
        $query .= "    STAFFNAME ";
        $query .= "FROM ";
        $query .= "    STAFF_MST ";
        $query .= "WHERE ";
        $query .= "    STAFFCD = '".$staff."' ";

        return $query;
    }

    public function csvDataGet($opt, $chair, $app, $model)
    {
        $query  = "SELECT ";
        $query .= "    T1.SCHREGNO, ";
        //文京の場合、性別の＊を表記しない。
        if ($model->schoolName == 'bunkyo') {
            $query .= "    '' AS SEX, ";
        } else {
            if (($model->Properties["useFormNameA233A"] == 'KNJA233A_10') &&
                (($model->field["PATTERN"] == "3") || ($model->field["PATTERN"] == "4"))) {
                //関西学院（プロパティー「useFormNameA233A」の値が「KNJA233A_10」の場合）かつ、
                //講座名簿が 3:Ｃ1 4:Ｃ2 の場合、性別が女でも * は出力しない。
                $query .= "    '' AS SEX, ";
            } else {
                $query .= "    CASE WHEN T1.SEX = '2' THEN '*' ELSE '' END AS SEX, ";
            }
        }
        $query .= "    value(T1.NAME,'') NAME, ";
        $query .= "    value(T1.NAME_KANA,'') NAME_KANA, ";
        $query .= "    value(T1.NAME_ENG,'') NAME_ENG, ";
        $query .= "    value(T6.HR_NAMEABBV,'') HR_NAMEABBV, ";
        $query .= "    value(T6.HR_NAME,'') HR_NAME, ";
        $query .= "    value(T2.GRADE,'') GRADE, ";
        $query .= "    value(T2.HR_CLASS,'') HR_CLASS, ";
        $query .= "    value(T2.ATTENDNO,'') ATTENDNO ";
        if ($model->schoolName == 'hirokoudai' && $model->isTuusin == "1") {
            $query .= "    ,value(T8.STAFFNAME, '') TEACHER_NAME ";
        }
        if ($model->Properties["useFormNameA233A"] == 'KNJA233A_7' && $model->field["PRINT_DIV"] == '1' && $model->field["PATTERN"]  == '3') {
            //GRD_DIVとTRANSFERCDどちらも設定されていればGRD_DIVを出力
            $query .= " ,CASE WHEN N1.NAME1 IS NOT NULL AND N2.NAME1 IS NOT NULL THEN N1.NAME1 WHEN N1.NAME1 IS NOT NULL THEN N1.NAME1 WHEN N2.NAME1 IS NOT NULL THEN N2.NAME1 END KUBUN ";
        }
        $query .= "FROM ";
        $query .= "    CHAIR_STD_DAT T7, ";
        $query .= "    SCHREG_BASE_MST T1, ";
        $query .= "    SCHREG_REGD_DAT T2, ";
        $query .= "    SCHREG_REGD_HDAT T6 ";
        if ($model->schoolName == 'hirokoudai' && $model->isTuusin == "1") {
            $query .= "    LEFT JOIN STAFF_MST T8 ON T8.STAFFCD = T6.TR_CD1 ";
        }
        if ($model->Properties["useFormNameA233A"] == 'KNJA233A_7' && $model->field["PRINT_DIV"] == '1' && $model->field["PATTERN"]  == '3') {
            $date = str_replace("/", "-", $model->field["DATE"]);
            $query .= "    LEFT JOIN SCHREG_TRANSFER_DAT TRANS ON TRANS.SCHREGNO = T1.SCHREGNO AND '".$date."' BETWEEN TRANS.TRANSFER_SDATE AND TRANS.TRANSFER_EDATE ";
            $query .= "    LEFT JOIN V_NAME_MST N2 ON N2.YEAR = T7.YEAR AND N2.NAMECD1 = 'A004' AND N2.NAMECD2 = TRANS.TRANSFERCD ";
            $query .= "    LEFT JOIN V_NAME_MST N1 ON N1.YEAR = T7.YEAR AND N1.NAMECD1 = 'A003' AND N1.NAMECD2 = T1.GRD_DIV ";
        }
        $query .= "WHERE ";
        $query .= "    T7.YEAR = '".$model->year."' AND ";
        $query .= "    T7.SEMESTER = '".$model->field["SEMESTER"]."' AND ";
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
        if ($opt == 2) {
            $query .= "    T1.SCHREGNO ";
        } else {
            if ($model->schoolName == 'hirokoudai' && $model->isTuusin == "1") {
                $query .= "    T1.SCHREGNO ";
            } else {
                $query .= "    T2.GRADE,T2.HR_CLASS,T2.ATTENDNO ";
            }
        }

        return $query;
    }
}
