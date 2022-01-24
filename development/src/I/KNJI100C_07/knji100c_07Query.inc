<?php

require_once('for_php7.php');
class knji100c_07Query extends Query {

    //タイトル用科目
    function selectSubclassTitleQuery($model)
    {
        $query  = " SELECT DISTINCT ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     YEAR, ";
            $query .= "     CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' ||SUBCLASSCD AS SUBCLASSCD ";
        } else {
            $query .= "     YEAR, ";
            $query .= "     SUBCLASSCD ";
        }
        $query .= " FROM ";
        $query .= "     SCHREG_STUDYREC_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO IN ('" .str_replace(",", "','", $model->selectdata_l) ."') ";
        $query .= " ORDER BY ";
        $query .= "     YEAR, ";
        $query .= "     SUBCLASSCD ";

        return $query;
    }

    //CSV
    function selectStudyRecQuery($model, $year, $subclassCd, $schregno, $headFlg)
    {
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $subclass_array = array();
            $subclass_array = explode("-", $subclassCd);
        }
        $query  = " SELECT ";
        if ($headFlg) {
            $query .= "     REGD.SCHREGNO, ";
            $query .= "     BASE.NAME, ";
            $query .= "     REGD.GRADE, ";
            $query .= "     REGD.HR_CLASS, ";
            $query .= "     REGD.ATTENDNO, ";
            if ($model->output == OUT_CODE_NAME) {       //コード＋名称
                $query .= "  BASE.SEX, ";
                $query .= "  Z002.NAME1,";
            } else if ($model->output == OUT_CODE_ONLY) { //コードのみ
                $query .= "  BASE.SEX, ";
            } else if ($model->output == OUT_NAME_ONLY) { //名称のみ
                $query .= "  Z002.NAME1,";
            }
        }
        $query .= "     CASE WHEN STUDY.SUBCLASSNAME IS NOT NULL ";
        $query .= "          THEN STUDY.SUBCLASSNAME ";
        $query .= "          ELSE SUB_M.SUBCLASSNAME ";
        $query .= "     END AS SUBCLASSNAME, ";
        $query .= "     VALUATION, ";
        $query .= "     COMP_CREDIT, ";
        $query .= "     CASE WHEN GET_CREDIT IS NOT NULL OR ADD_CREDIT IS NOT NULL ";
        $query .= "          THEN VALUE(GET_CREDIT, 0) + VALUE(ADD_CREDIT, 0) ";
        $query .= "          ELSE NULL ";
        $query .= "     END AS GET_ADD ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT REGD ";
        $query .= "     LEFT JOIN SCHREG_BASE_MST BASE ON REGD.SCHREGNO = BASE.SCHREGNO ";
        $query .= "     LEFT JOIN NAME_MST Z002 ON Z002.NAMECD1 = 'Z002' ";
        $query .= "          AND BASE.SEX = Z002.NAMECD2 ";
        $query .= "     LEFT JOIN SCHREG_STUDYREC_DAT STUDY ON STUDY.YEAR = '".$year."' ";
        $query .= "          AND REGD.SCHREGNO = STUDY.SCHREGNO ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "          AND STUDY.CLASSCD       = '".$subclass_array[0]."' ";
            $query .= "          AND STUDY.SCHOOL_KIND   = '".$subclass_array[1]."' ";
            $query .= "          AND STUDY.CURRICULUM_CD = '".$subclass_array[2]."' ";
            $query .= "          AND STUDY.SUBCLASSCD    = '".$subclass_array[3]."' ";
        } else {
            $query .= "          AND STUDY.SUBCLASSCD = '".$subclassCd."' ";
        }
        $query .= "     LEFT JOIN SUBCLASS_MST SUB_M ON STUDY.SUBCLASSCD = SUB_M.SUBCLASSCD ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                             AND STUDY.CLASSCD = SUB_M.CLASSCD ";
            $query .= "                             AND STUDY.SCHOOL_KIND = SUB_M.SCHOOL_KIND ";
            $query .= "                             AND STUDY.CURRICULUM_CD = SUB_M.CURRICULUM_CD ";
        }
        $query .= " WHERE ";
        $query .= "     REGD.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND REGD.SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= "     AND REGD.SCHREGNO = '".$schregno."' ";
        $query .= "ORDER BY ";
        $query .= "  REGD.GRADE, ";
        $query .= "  REGD.HR_CLASS, ";
        $query .= "  REGD.ATTENDNO ";

        return $query;
    }

    //タイトル用
    function selectAttendTitleQuery($model)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     YEAR ";
        $query .= " FROM ";
        $query .= "     SCHREG_ATTENDREC_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO IN ('" .str_replace(",", "','", $model->selectdata_l) ."') ";
        $query .= " ORDER BY ";
        $query .= "     YEAR ";

        return $query;
    }

    //タイトル用
    function isKindaiSql()
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'Z010' ";
        $query .= "     AND NAMECD2 = '00' ";
        $query .= "     AND NAME1 = 'KINDAI' ";

        return $query;
    }

    //CSV
    function selectAttendQuery($model, $year, $schregno, $isKindai, $headFlg)
    {
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $subclass_array = array();
            $subclass_array = explode("-", $subclassCd);
        }
        $query  = " SELECT ";
        if ($headFlg) {
            $query .= "     REGD.SCHREGNO, ";
            $query .= "     BASE.NAME, ";
            $query .= "     REGD.GRADE, ";
            $query .= "     REGD.HR_CLASS, ";
            $query .= "     REGD.ATTENDNO, ";
            if ($model->output == OUT_CODE_NAME) {       //コード＋名称
                $query .= "  BASE.SEX, ";
                $query .= "  Z002.NAME1,";
            } else if ($model->output == OUT_CODE_ONLY) { //コードのみ
                $query .= "  BASE.SEX, ";
            } else if ($model->output == OUT_NAME_ONLY) { //名称のみ
                $query .= "  Z002.NAME1,";
            }
        }
        $query .= "    CASE WHEN S1.SEM_OFFDAYS = '1' ";
        if ($isKindai) {
            $query .= "              THEN VALUE(CLASSDAYS,0) - VALUE(ABROAD,0) ";
            $query .= "              ELSE VALUE(CLASSDAYS,0) - VALUE(OFFDAYS,0) - VALUE(ABROAD,0) ";
        } else {
            $query .= "              THEN VALUE(CLASSDAYS,0) ";
            $query .= "              ELSE VALUE(CLASSDAYS,0) - VALUE(OFFDAYS,0) ";
        }
        $query .= "    END AS ATTEND_1, "; // 授業日数 - (休学日数) [- 留学日数]
        $query .= "    VALUE(SUSPEND,0) + VALUE(MOURNING,0) AS SUSP_MOUR, "; // 出停・忌引
        $query .= "    VALUE(ABROAD,0) AS ABROAD, "; // 留学:4
        $query .= "    CASE WHEN S1.SEM_OFFDAYS = '1' ";
        $query .= "         THEN VALUE(REQUIREPRESENT,0) + VALUE(OFFDAYS,0) ";
        $query .= "         ELSE VALUE(REQUIREPRESENT,0) ";
        $query .= "    END AS REQUIREPRESENT, "; // 要出席日数:5
        $query .= "    CASE WHEN S1.SEM_OFFDAYS = '1' ";
        $query .= "         THEN VALUE(SICK,0) + VALUE(ACCIDENTNOTICE,0) + VALUE(NOACCIDENTNOTICE,0) + VALUE(OFFDAYS,0) ";
        $query .= "         ELSE VALUE(SICK,0) + VALUE(ACCIDENTNOTICE,0) + VALUE(NOACCIDENTNOTICE,0) ";
        $query .= "    END AS ATTEND_6, "; // 病欠＋事故欠（届・無）:6
        $query .= "    VALUE(PRESENT,0) AS PRESENT, "; // 出席日数:7
        $query .= "    REMARK.ATTENDREC_REMARK ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT REGD ";
        $query .= "     LEFT JOIN SCHREG_BASE_MST BASE ON REGD.SCHREGNO = BASE.SCHREGNO ";
        $query .= "     LEFT JOIN NAME_MST Z002 ON Z002.NAMECD1 = 'Z002' ";
        $query .= "          AND BASE.SEX = Z002.NAMECD2 ";
        $query .= "     LEFT JOIN SCHREG_STUDYREC_DAT STUDY ON STUDY.YEAR = '".$year."' ";
        $query .= "          AND REGD.SCHREGNO = STUDY.SCHREGNO ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "          AND STUDY.CLASSCD       = '".$subclass_array[0]."' ";
            $query .= "          AND STUDY.SCHOOL_KIND   = '".$subclass_array[1]."' ";
            $query .= "          AND STUDY.CURRICULUM_CD = '".$subclass_array[2]."' ";
            $query .= "          AND STUDY.SUBCLASSCD    = '".$subclass_array[3]."' ";
        } else {
            $query .= "          AND STUDY.SUBCLASSCD = '".$subclassCd."' ";
        }
        $query .= "     LEFT JOIN SUBCLASS_MST SUB_M ON STUDY.SUBCLASSCD = SUB_M.SUBCLASSCD ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                             AND STUDY.CLASSCD       = SUB_M.CLASSCD ";
            $query .= "                             AND STUDY.SCHOOL_KIND   = SUB_M.SCHOOL_KIND ";
            $query .= "                             AND STUDY.CURRICULUM_CD = SUB_M.CURRICULUM_CD ";
        }
        $query .= "     LEFT JOIN (SELECT ";
        $query .= "                      YEAR, ";
        $query .= "                      SCHREGNO, ";
        $query .= "                      SUM(CLASSDAYS) AS CLASSDAYS, ";
        $query .= "                      SUM(OFFDAYS) AS OFFDAYS, ";
        $query .= "                      SUM(ABSENT) AS ABSENT, ";
        $query .= "                      SUM(SUSPEND) AS SUSPEND, ";
        $query .= "                      SUM(MOURNING) AS MOURNING, ";
        $query .= "                      SUM(ABROAD) AS ABROAD, ";
        $query .= "                      SUM(REQUIREPRESENT) AS REQUIREPRESENT, ";
        $query .= "                      SUM(SICK) AS SICK, ";
        $query .= "                      SUM(ACCIDENTNOTICE) AS ACCIDENTNOTICE, ";
        $query .= "                      SUM(NOACCIDENTNOTICE) AS NOACCIDENTNOTICE, ";
        $query .= "                      SUM(PRESENT) AS PRESENT ";
        $query .= "                 FROM ";
        $query .= "                      SCHREG_ATTENDREC_DAT ";
        $query .= "                 WHERE ";
        $query .= "                      SCHREGNO = '".$schregno."' ";
        $query .= "                      AND YEAR = '".$year."' ";
        $query .= "                 GROUP BY ";
        $query .= "                      YEAR, ";
        $query .= "                      SCHREGNO ";
        $query .= "               ) T1 ON T1.SCHREGNO = REGD.SCHREGNO ";
        $query .= "     LEFT JOIN SCHOOL_MST S1 ON S1.YEAR = T1.YEAR ";
        $query .= "     LEFT JOIN HEXAM_ENTREMARK_DAT REMARK ON REMARK.YEAR = '".$year."' ";
        $query .= "          AND REMARK.SCHREGNO = REGD.SCHREGNO ";
        $query .= " WHERE ";
        $query .= "     REGD.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND REGD.SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= "     AND REGD.SCHREGNO = '".$schregno."' ";
        $query .= "ORDER BY ";
        $query .= "  REGD.GRADE, ";
        $query .= "  REGD.HR_CLASS, ";
        $query .= "  REGD.ATTENDNO ";

        return $query;
    }

    //備考
    function selectHexamRemarkQuery($schregNo)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     JOBHUNT_ABSENCE ";
        $query .= " FROM ";
        $query .= "     HEXAM_EMPREMARK_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '".$schregNo."' ";

        return $query;
    }

    //タイトル用
    function selectHexamEntTitleQuery($model)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     YEAR ";
        $query .= " FROM ";
        $query .= "     HEXAM_ENTREMARK_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO IN ('" .str_replace(",", "','", $model->selectdata_l) ."') ";

        return $query;
    }

    //CSV
    function selectHexamEntQuery($model, $year, $schregno, $headFlg)
    {
        $query  = " SELECT ";
        if ($headFlg) {
            $query .= "     REGD.SCHREGNO, ";
            $query .= "     BASE.NAME, ";
            $query .= "     REGD.GRADE, ";
            $query .= "     REGD.HR_CLASS, ";
            $query .= "     REGD.ATTENDNO, ";
            if ($model->output == OUT_CODE_NAME) {       //コード＋名称
                $query .= "  BASE.SEX, ";
                $query .= "  Z002.NAME1,";
            } else if ($model->output == OUT_CODE_ONLY) { //コードのみ
                $query .= "  BASE.SEX, ";
            } else if ($model->output == OUT_NAME_ONLY) { //名称のみ
                $query .= "  Z002.NAME1,";
            }
        }
        $query .= "    REMARK.SPECIALACTREC, ";
        $query .= "    REMARK.TRAIN_REF1, ";
        $query .= "    REMARK.TRAIN_REF2, ";
        $query .= "    REMARK.TRAIN_REF3 ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT REGD ";
        $query .= "     LEFT JOIN SCHREG_BASE_MST BASE ON REGD.SCHREGNO = BASE.SCHREGNO ";
        $query .= "     LEFT JOIN NAME_MST Z002 ON Z002.NAMECD1 = 'Z002' ";
        $query .= "          AND BASE.SEX = Z002.NAMECD2 ";
        $query .= "     LEFT JOIN HEXAM_ENTREMARK_DAT REMARK ON REMARK.YEAR = '".$year."' ";
        $query .= "          AND REMARK.SCHREGNO = REGD.SCHREGNO ";
        $query .= " WHERE ";
        $query .= "     REGD.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND REGD.SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= "     AND REGD.SCHREGNO = '".$schregno."' ";
        $query .= "ORDER BY ";
        $query .= "  REGD.GRADE, ";
        $query .= "  REGD.HR_CLASS, ";
        $query .= "  REGD.ATTENDNO ";

        return $query;
    }

    //備考
    function selectHexamEntHdatQuery($schregNo)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     TOTALSTUDYACT, ";
        $query .= "     TOTALSTUDYVAL, ";
        $query .= "     REMARK ";
        $query .= " FROM ";
        $query .= "     HEXAM_ENTREMARK_HDAT ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '".$schregNo."' ";

        return $query;
    }

    //備考
    function selectHexamEmpQuery($model, $schregno, $headFlg)
    {
        $query  = " SELECT ";
        if ($headFlg) {
            $query .= "     REGD.SCHREGNO, ";
            $query .= "     BASE.NAME, ";
            $query .= "     REGD.GRADE, ";
            $query .= "     REGD.HR_CLASS, ";
            $query .= "     REGD.ATTENDNO, ";
            if ($model->output == OUT_CODE_NAME) {       //コード＋名称
                $query .= "  BASE.SEX, ";
                $query .= "  Z002.NAME1,";
            } else if ($model->output == OUT_CODE_ONLY) { //コードのみ
                $query .= "  BASE.SEX, ";
            } else if ($model->output == OUT_NAME_ONLY) { //名称のみ
                $query .= "  Z002.NAME1,";
            }
        }
        $query .= "    REMARK.JOBHUNT_REC, ";
        $query .= "    REMARK.JOBHUNT_HEALTHREMARK, ";
        $query .= "    REMARK.JOBHUNT_RECOMMEND ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT REGD ";
        $query .= "     LEFT JOIN SCHREG_BASE_MST BASE ON REGD.SCHREGNO = BASE.SCHREGNO ";
        $query .= "     LEFT JOIN NAME_MST Z002 ON Z002.NAMECD1 = 'Z002' ";
        $query .= "          AND BASE.SEX = Z002.NAMECD2 ";
        $query .= "     LEFT JOIN HEXAM_EMPREMARK_DAT REMARK ON REMARK.SCHREGNO = REGD.SCHREGNO ";
        $query .= " WHERE ";
        $query .= "     REGD.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND REGD.SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= "     AND REGD.SCHREGNO = '".$schregno."' ";
        $query .= "ORDER BY ";
        $query .= "  REGD.GRADE, ";
        $query .= "  REGD.HR_CLASS, ";
        $query .= "  REGD.ATTENDNO ";

        return $query;
    }

    //名称マスタ
    function getMeisyou($namecd1, $namecd2) {
        $query  = "SELECT ";
        $query .= "    NAME1 ";
        $query .= "FROM ";
        $query .= "    NAME_MST ";
        $query .= "WHERE ";
        $query .= "    NAMECD1 = '".$namecd1."' ";
        $query .= "    AND NAMECD2 = '".$namecd2."' ";

        return $query;
    }

    //課程学科取得
    function getCourseMajor($model) {
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

    //学年
    function getGrade() {
        $query  = "SELECT ";
        $query .= "    GRADE AS VALUE, ";
        $query .= "    GRADE_NAME1 AS LABEL ";
        $query .= "FROM ";
        $query .= "    SCHREG_REGD_GDAT ";
        $query .= "WHERE ";
        $query .= "    YEAR = '".CTRL_YEAR."' ";
        $query .= "    AND SCHOOL_KIND = 'H' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //性別取得
    function getSex($model) {
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
    function getAuth($model) {
        $query  = " SELECT ";
        $query .= "     GRADE || '_' ||  HR_CLASS AS VALUE, ";
        $query .= "     HR_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".CTRL_YEAR."' AND ";
        $query .= "     SEMESTER    = '".CTRL_SEMESTER."' AND ";
        $query .= "     GRADE       = '".$model->grade."' ";
        //参照・更新可（制限付き）
        if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT){
            $query .= " AND '".STAFFCD."' IN (TR_CD1, TR_CD2, TR_CD3) ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

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

    //リストtoリスト右
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
        $query .= "     T1.GRADE    = '". $model->grade ."' AND ";
        $query .= "     T1.SCHREGNO IN ('" .str_replace(",", "','", $model->selectdata_l) ."') ";
        $query .= " ORDER BY ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.HR_CLASS, ";
        $query .= "     T1.ATTENDNO ";

        return $query;
    }

    //サブシステム名取得
    function getSubSystem() {
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

    //選択フィールド取得
    function getFieldSql($model) {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     KNJI100C_KAKIDASHI_LIST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND DATA_DIV = '07' ";
        $query .= " ORDER BY ";
        $query .= "     SEQ ";

        return $query;
    }

    //選択フィールド削除
    function getDelFieldSql($model) {
        $query  = " DELETE FROM ";
        $query .= "     KNJI100C_KAKIDASHI_LIST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND DATA_DIV = '07' ";

        return $query;
    }

    //選択フィールド保存
    function getFieldInsSql($model, $setSeq, $val) {

        //入金予定項目データ
        $data = array();
        $data["YEAR"][TEXT]            = CTRL_YEAR;
        $data["DATA_DIV"][TEXT]        = "07";
        $data["SEQ"][NUMBER]           = $setSeq;
        $data["FIELD_NAME"][TEXT]      = $val;
        $data["REGISTERCD"][TEXT]      = STAFFCD;
        $data["UPDATED"][FUNC]         = "sysdate()";

        $query = Query::insertSQL($data, "KNJI100C_KAKIDASHI_LIST");
        return $query;
    }
}
?>