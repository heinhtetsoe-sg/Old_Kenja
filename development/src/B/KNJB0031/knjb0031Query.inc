<?php

require_once('for_php7.php');

class knjb0031Query extends Query
{

    /*------------------------左フレーム-----------------------*/

    //学校を判断
    public function getNameMstZ010()
    {
        $query = " SELECT NAME1 FROM NAME_MST WHERE NAMECD1 = 'Z010' AND NAMECD2 = '00' ";
        return $query;
    }

    //学科連番を取得
    public function getMajorMst($year)
    {
        $query = " SELECT * FROM V_COURSE_MAJOR_MST WHERE YEAR = '{$year}' ORDER BY COURSECD, MAJORCD ";
        return $query;
    }

    //対象年度コンボ
    public function getSemesterMst($isLastSemester)
    {
        $query  = "";
        $query .= " SELECT ";
        $query .= "     YEAR || '-' || SEMESTER AS VALUE, ";
        $query .= "     YEAR || '年度　' || SEMESTERNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' AND SEMESTER <> '9' ";
        //ログイン学期が最終学期の場合、対象年度リストに次年度・１学期（前期）を追加
        if ($isLastSemester) {
            $query .= "     OR (YEAR = '".(CTRL_YEAR + 1)."' AND SEMESTER = '1') ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //学籍在籍データの件数取得
    public function checkRegdDat($model, $year, $semester)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$year}' ";
        $query .= "     AND SEMESTER = '{$semester}' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " AND GRADE IN (  SELECT ";
                $query .= "                     G1.GRADE ";
                $query .= "                 FROM ";
                $query .= "                     SCHREG_REGD_GDAT G1 ";
                $query .= "                 WHERE ";
                $query .= "                     G1.YEAR         = T1.YEAR AND ";
                $query .= "                     G1.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind), "','")."') ";
                $query .= "                 ) ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND GRADE IN (  SELECT ";
            $query .= "                     G1.GRADE ";
            $query .= "                 FROM ";
            $query .= "                     SCHREG_REGD_GDAT G1 ";
            $query .= "                 WHERE ";
            $query .= "                     G1.YEAR         = T1.YEAR AND ";
            $query .= "                     G1.SCHOOL_KIND  = '".SCHOOLKIND."' ";
            $query .= "                 ) ";
        }

        return $query;
    }

    //学年・コース取得
    public function getGradeCouse($model, $regdCnt, $year, $semester)
    {
        //在籍データがあればその学年・コースを表示
        //但し、広島国際は、常に在籍データの学年・コースを表示
        if (0 < $regdCnt || $model->z010name1 == "hirogaku") {
            $query  = " SELECT DISTINCT ";
            $query .= "     T1.GRADE, ";
            $query .= "     T4.GRADE_NAME1, ";
            $query .= "     T1.COURSECD, ";
            $query .= "     T1.MAJORCD, ";
            $query .= "     T2.COURSENAME, ";
            $query .= "     T2.MAJORNAME, ";
            $query .= "     T1.COURSECODE, ";
            $query .= "     T3.COURSECODENAME ";
            $query .= " FROM ";
            $query .= "     SCHREG_REGD_DAT T1, ";
            $query .= "     V_COURSE_MAJOR_MST T2, ";
            $query .= "     V_COURSECODE_MST T3, ";
            $query .= "     SCHREG_REGD_GDAT T4 ";
            $query .= " WHERE ";
            $query .= "     T1.YEAR         = T2.YEAR AND ";
            $query .= "     T1.YEAR         = T3.YEAR AND ";
            $query .= "     T1.YEAR         = T4.YEAR AND ";
            $query .= "     T1.YEAR         = '{$year}' AND ";
            $query .= "     T1.SEMESTER     = '{$semester}' AND ";
            $query .= "     T1.COURSECD     = T2.COURSECD AND ";
            $query .= "     T1.MAJORCD      = T2.MAJORCD AND ";
            $query .= "     T1.COURSECODE   = T3.COURSECODE AND ";
            $query .= "     T1.GRADE        = T4.GRADE  ";
            if ($model->Properties["use_prg_schoolkind"] == "1") {
                if ($model->selectSchoolKind) {
                    $query .= " AND T4.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind), "','")."') ";
                }
            } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
                $query .= " AND T4.SCHOOL_KIND = '".SCHOOLKIND."' ";
            }
            $query .= " ORDER BY ";
            $query .= "     T1.GRADE, ";
            $query .= "     T1.COURSECD, ";
            $query .= "     T1.MAJORCD, ";
            $query .= "     T1.COURSECODE ";
        } else {
            $query  = " SELECT DISTINCT ";
            $query .= "     T3.GRADE, ";
            $query .= "     T3.GRADE_NAME1, ";
            $query .= "     T1.COURSECD, ";
            $query .= "     T1.MAJORCD, ";
            $query .= "     T1.COURSENAME, ";
            $query .= "     T1.MAJORNAME, ";
            $query .= "     T2.COURSECODE, ";
            $query .= "     T2.COURSECODENAME ";
            $query .= " FROM ";
            $query .= "     V_COURSE_MAJOR_MST T1, ";
            $query .= "     V_COURSECODE_MST T2, ";
            $query .= "     SCHREG_REGD_GDAT T3 ";
            $query .= " WHERE ";
            $query .= "     T1.YEAR = T2.YEAR AND ";
            $query .= "     T1.YEAR = T3.YEAR AND ";
            $query .= "     T1.YEAR = '{$year}' ";
            if ($model->Properties["use_prg_schoolkind"] == "1") {
                if ($model->selectSchoolKind) {
                    $query .= " AND T3.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind), "','")."') ";
                }
            } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
                $query .= " AND T3.SCHOOL_KIND = '".SCHOOLKIND."' ";
            }
            $query .= " ORDER BY ";
            $query .= "     T3.GRADE, ";
            $query .= "     T1.COURSECD, ";
            $query .= "     T1.MAJORCD, ";
            $query .= "     T2.COURSECODE ";
        }

        return $query;
    }

    //単位マスタの件数取得
    public function checkCreditMst($model, $year)
    {
        $query  = "";
        $query .= " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     CREDIT_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$year}' ";
        $query .= "     AND GRADE || '-' || COURSECD || '-' || MAJORCD || '-' || COURSECODE = '{$model->grade_course}' ";

        return $query;
    }

    //校種取得
    public function getSchoolKind($year, $grade)
    {
        $query  = " SELECT ";
        $query .= "     SCHOOL_KIND ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$year}' ";
        $query .= "     AND GRADE = '{$grade}' ";

        return $query;
    }

    //科目コンボ
    public function getSubclassMst($model, $year, $creditCnt)
    {
        $query  = "";
        $query .= " SELECT ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD AS VALUE, ";
            $query .= "     CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD || '　' || SUBCLASSNAME AS LABEL ";
        } else {
            $query .= "     SUBCLASSCD AS VALUE, ";
            $query .= "     SUBCLASSCD || '　' || SUBCLASSNAME AS LABEL ";
        }
        $query .= " FROM ";
        $query .= "     V_SUBCLASS_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$year}' ";
        if ($model->schoolKind) {
            $query .= "     AND SCHOOL_KIND = '{$model->schoolKind}' ";
        }
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " AND SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind), "','")."') ";
            }
        } elseif ($model->Properties["useCurriculumcd"] == '1' && $model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        //単位マスタがあればその科目を表示
        //但し、広島国際は、常に単位マスタの科目を表示
        if (0 < $creditCnt || $model->z010name1 == "hirogaku") {
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "     AND CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD IN ( ";
                $query .= "         SELECT ";
                $query .= "             CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD ";
                $query .= "         FROM ";
                $query .= "             CREDIT_MST ";
                $query .= "         WHERE ";
                $query .= "             YEAR = '{$year}' ";
                $query .= "             AND GRADE || '-' || COURSECD || '-' || MAJORCD || '-' || COURSECODE = '{$model->grade_course}' ";
                $query .= "         GROUP BY ";
                $query .= "             CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD ";
                $query .= "     ) ";
            } else {
                $query .= "     AND SUBCLASSCD IN ( ";
                $query .= "         SELECT ";
                $query .= "             SUBCLASSCD ";
                $query .= "         FROM ";
                $query .= "             CREDIT_MST ";
                $query .= "         WHERE ";
                $query .= "             YEAR = '{$year}' ";
                $query .= "             AND GRADE || '-' || COURSECD || '-' || MAJORCD || '-' || COURSECODE = '{$model->grade_course}' ";
                $query .= "         GROUP BY ";
                $query .= "             SUBCLASSCD ";
                $query .= "     ) ";
            }
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //科目略称・名称を取得
    public function getSubclassabbv($model)
    {
        $year = substr($model->term, 0, 4);

        $query  = "";
        $query .= " SELECT ";
        $query .= "     SUBCLASSNAME, ";
        $query .= "     SUBCLASSABBV ";
        $query .= " FROM ";
        $query .= "     V_SUBCLASS_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$year}' ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= " AND CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD = '{$model->subclasscd}' ";
        } else {
            $query .= " AND SUBCLASSCD = '{$model->subclasscd}' ";
        }

        return $query;
    }

    //追加件数初期値取得
    public function getKensuuDefault($model)
    {
        $year       = substr($model->term, 0, 4);
        $semester   = substr($model->term, 5);

        $query  = " SELECT ";
        $query .= "     COUNT(T2.CHAIRCD) ";
        $query .= " FROM ";
        $query .= "     CHAIR_DAT T1, ";
        $query .= "     CHAIR_CLS_DAT T2 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR     = T2.YEAR AND ";
        $query .= "     T1.YEAR     = '{$year}' AND ";
        $query .= "     T1.SEMESTER = T2.SEMESTER AND ";
        $query .= "     T1.SEMESTER = '{$semester}' ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     AND T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD = '{$model->subclasscd}' ";
        } else {
            $query .= "     AND T1.SUBCLASSCD = '{$model->subclasscd}' ";
        }
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " AND T2.TRGTGRADE IN (SELECT ";
                $query .= "                         G1.GRADE ";
                $query .= "                     FROM ";
                $query .= "                         SCHREG_REGD_GDAT G1 ";
                $query .= "                     WHERE ";
                $query .= "                         G1.YEAR         = T2.YEAR AND ";
                $query .= "                         G1.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind), "','")."') ";
                $query .= "                     ) ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND T2.TRGTGRADE IN (SELECT ";
            $query .= "                         G1.GRADE ";
            $query .= "                     FROM ";
            $query .= "                         SCHREG_REGD_GDAT G1 ";
            $query .= "                     WHERE ";
            $query .= "                         G1.YEAR         = T2.YEAR AND ";
            $query .= "                         G1.SCHOOL_KIND  = '".SCHOOLKIND."' ";
            $query .= "                     ) ";
        }
        //群ラジオ 1:教科 2:群
        if ($model->group == '2') {
            $query .= "     AND T1.GROUPCD = T2.GROUPCD ";
            $query .= "     AND T1.GROUPCD > '0000' ";
        } else {
            $query .= "     AND T1.CHAIRCD = T2.CHAIRCD ";
            $query .= "     AND T1.GROUPCD = '0000' ";
        }

        return $query;
    }

    //講座一覧
    public function getChairList($model, $year, $semester)
    {
        $query  = "";
        $query .= " SELECT ";
        $query .= "     T1.*, ";
        $query .= "     L1.REMARK1 AS STD_CNT, ";
        $query .= "     L2.REMARK1 AS KEIKOKUTEN_SOTEN, ";
        $query .= "     L2.REMARK2 AS KEIKOKUTEN_HYOUKA, ";
        $query .= "     L2.REMARK3 AS KEIKOKUTEN_HYOUTEI, ";
        $query .= "     L3.REMARK1 AS SEQ004_REMARK1, ";
        $query .= "     L3.REMARK2 AS SEQ004_REMARK2, ";
        $query .= "     L3.REMARK3 AS SEQ004_REMARK3 ";
        $query .= " FROM ";
        $query .= "     CHAIR_DAT T1 ";
        $query .= "     LEFT JOIN CHAIR_DETAIL_DAT L1 ";
        $query .= "          ON T1.YEAR     = L1.YEAR ";
        $query .= "         AND T1.SEMESTER = L1.SEMESTER ";
        $query .= "         AND T1.CHAIRCD  = L1.CHAIRCD ";
        $query .= "         AND L1.SEQ      = '001' ";
        $query .= "     LEFT JOIN CHAIR_DETAIL_DAT L2 ";
        $query .= "          ON T1.YEAR     = L2.YEAR ";
        $query .= "         AND T1.SEMESTER = L2.SEMESTER ";
        $query .= "         AND T1.CHAIRCD  = L2.CHAIRCD ";
        $query .= "         AND L2.SEQ      = '003' ";
        $query .= "     LEFT JOIN CHAIR_DETAIL_DAT L3 ";
        $query .= "          ON T1.YEAR     = L3.YEAR ";
        $query .= "         AND T1.SEMESTER = L3.SEMESTER ";
        $query .= "         AND T1.CHAIRCD  = L3.CHAIRCD ";
        $query .= "         AND L3.SEQ      = '004' ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '{$year}' ";
        $query .= "     AND T1.SEMESTER = '{$semester}' ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     AND T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD = '{$model->subclasscd}' ";
        } else {
            $query .= "     AND T1.SUBCLASSCD = '{$model->subclasscd}' ";
        }
        //群ラジオ 1:教科 2:群
        if ($model->group == '2') {
            $query .= "     AND T1.GROUPCD > '0000' ";
        } else {
            $query .= "     AND T1.GROUPCD = '0000' ";
        }
        $query .= " ORDER BY ";
        $query .= "     T1.CHAIRCD ";

        return $query;
    }

    //MAX講座コード取得
    public function getMaxChaircd($model, $year, $semester, $chaircd5)
    {
        $query  = "";
        $query .= " SELECT ";
//        $query .= "     COUNT(*) AS CNT, ";
        $query .= "     MAX(CHAIRCD) AS CHAIRCD ";
        $query .= " FROM ";
        $query .= "     CHAIR_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$year}' ";
        $query .= "     AND SEMESTER = '{$semester}' ";
        $query .= "     AND CHAIRCD LIKE '{$chaircd5}%' ";

        return $query;
    }

    /*------------------------右フレーム-----------------------*/

    //列名リスト
    public function getRetumei($model)
    {
        $opt = array();

        list($year, $seme) = explode("-", $model->term);
        $query  = " SELECT ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$year}' ";
        $query .= "     AND NAMECD1 = 'B023' ";
        $query .= " ORDER BY ";
        $query .= "     NAMECD2 ";

        return $query;
    }

    //警告点区分
    public function getIsKeikokuten($model)
    {
        list($grade)=explode('-', $model->grade_course);
        list($year)=explode('-', $model->term);

        $query  = " SELECT ";
        $query .= "     SCHOOL_REMARK1 ";
        $query .= " FROM ";
        $query .= "     SCHOOL_DETAIL_DAT T1 ";
        $query .= "     LEFT JOIN ";
        $query .= "         SCHREG_REGD_GDAT T2 ";
        $query .= "     ON ";
        $query .= "         T1.YEAR=T2.YEAR ";
        if($model->Properties["useSchool_KindField"] == "1") {
            $query .= "         AND T1.SCHOOL_KIND=T2.SCHOOL_KIND ";
        }
        $query .= " WHERE ";

        $query .= "         T2.GRADE='{$grade}' ";
        $query .= "         AND T1.YEAR='{$year}' ";
        $query .= "         AND SCHOOL_SEQ='009' ";
        if($model->Properties["useSchool_KindField"] == "1") {
            $query .= "        AND T1.SCHOOLCD    = '".sprintf("%012d", SCHOOLCD)."' ";
        }
        return $query;
    }
    //リストコンボ用取得
    //履修期間区分
    public function getTakesemes($year)
    {
        $query  = " WITH T_COMMON (VALUE,LABEL) AS ( ";
        $query .= "     VALUES('0','0　通年') ";
        $query .= "     ) ";
        $query .= " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     T_COMMON ";
        $query .= " UNION ALL ";
        $query .= " SELECT ";
        $query .= "     SEMESTER AS VALUE, ";
        $query .= "     SEMESTER || '　' || SEMESTERNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$year}' AND SEMESTER <> '9' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }
    //群
    public function getGroup($model, $year)
    {
        $query  = "";
        //群ラジオ 1:教科 2:群
        if ($model->group == '2') {
            $query .= " SELECT ";
            $query .= "     GROUPCD AS VALUE, ";
            $query .= "     GROUPCD || '　' || GROUPNAME AS LABEL ";
            $query .= " FROM ";
            $query .= "     V_ELECTCLASS_MST ";
            $query .= " WHERE ";
            $query .= "     YEAR = '{$year}' ";
            $query .= " ORDER BY ";
            $query .= "     VALUE ";
        } else {
            $query .= " WITH T_COMMON (VALUE,LABEL) AS ( ";
            $query .= "     VALUES('0000','0000　なし（ＨＲ）') ";
            $query .= "     ) ";
            $query .= " SELECT ";
            $query .= "     VALUE, ";
            $query .= "     LABEL ";
            $query .= " FROM ";
            $query .= "     T_COMMON ";
            $query .= " ORDER BY ";
            $query .= "     VALUE ";
        }

        return $query;
    }

    //選択ダイアログ

    //学籍在籍データの件数取得
    public function checkRegdDatSubForm1($term, $grade_course)
    {
        $year = substr($term, 0, 4);
        $semester = substr($term, 5);

        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$year}' ";
        $query .= "     AND SEMESTER = '{$semester}' ";
        $query .= "     AND GRADE || '-' || COURSECD || '-' || MAJORCD || '-' || COURSECODE = '{$grade_course}' ";

        return $query;
    }

    //受講クラス
    public function selectQuerySubForm1($term, $grade_course, $regdCnt)
    {
        $year = substr($term, 0, 4);
        $semester = substr($term, 5);

        $query  = " SELECT ";
        $query .= "     GRADE || HR_CLASS AS GRADE_CLASS, ";
        $query .= "     HR_NAME, ";
        $query .= "     HR_NAMEABBV ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$year}' ";
        $query .= "     AND SEMESTER = '{$semester}' ";
        //コースコンボで指定した学籍在籍データがあればそのクラスを表示
        if (0 < $regdCnt) {
            $query .= "     AND GRADE || HR_CLASS IN ( ";
            $query .= "         SELECT ";
            $query .= "             GRADE || HR_CLASS ";
            $query .= "         FROM ";
            $query .= "             SCHREG_REGD_DAT ";
            $query .= "         WHERE ";
            $query .= "             YEAR = '{$year}' ";
            $query .= "             AND SEMESTER = '{$semester}' ";
            $query .= "             AND GRADE || '-' || COURSECD || '-' || MAJORCD || '-' || COURSECODE = '{$grade_course}' ";
            $query .= "         GROUP BY ";
            $query .= "             GRADE || HR_CLASS ";
            $query .= "     ) ";
        } else {
            $grade = substr($grade_course, 0, 2);
            $query .= "     AND GRADE = '{$grade}' ";
        }
        $query .= " ORDER BY GRADE_CLASS ";

        return $query;
    }
    //科目担任
    public function selectQuerySubForm2($term, $staffcd, $subclass)
    {
        $query  = "SELECT ";
        $query .= "    T1.SECTIONCD, ";
        $query .= "    T2.SECTIONABBV, ";
        $query .= "    T1.STAFFCD, ";
        $query .= "    T1.STAFFNAME_SHOW ";
        $query .= "FROM ";
        $query .= "    V_STAFF_MST T1 ";
        $query .= "    LEFT JOIN V_SECTION_MST T2 ON T2.YEAR =T1.YEAR AND T2.SECTIONCD = T1.SECTIONCD ";
        $query .= "    LEFT JOIN STAFF_CLASS_MST T3 ON T3.STAFFCD = T1.STAFFCD ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            list($classcd, $school_kind, $curriculum_cd, $subclasscd) = explode("-", $subclass);
            $query .= "     AND T3.CLASSCD         = '".$classcd."' ";
            $query .= "     AND T3.SCHOOL_KIND     = '".$school_kind."' ";
        } else {
            $query .= "     AND T3.CLASSCD = SUBSTR('".$subclass."', 1, 2) ";
        }
        $query .= "WHERE ";
        $query .= "    T1.YEAR='".SUBSTR($term, 0, 4)."' ";
        $query .= " ORDER BY ";
        $query .= "    CASE WHEN T3.STAFFCD IS NOT NULL ";
        $query .= "         THEN 0 ";
        $query .= "         ELSE 1 ";
        $query .= "    END, ";
        $query .= "    T1.STAFFCD, ";
        $query .= "    T1.SECTIONCD ";
        return $query;
    }

    //資格科目
    public function getStaffClass($model, $subclass, $flg = "")
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     STAFFCD ";
        $query .= " FROM ";
        $query .= "     STAFF_CLASS_MST ";
        if ($flg) {
            $query .= " WHERE ";
            if ($model->Properties["useCurriculumcd"] == '1') {
                list($classcd, $school_kind, $curriculum_cd, $subclasscd) = explode("-", $subclass);
                $query .= "     CLASSCD         = '".$classcd."' AND ";
                $query .= "     SCHOOL_KIND     = '".$school_kind."' ";
            } else {
                $query .= "     CLASSCD = SUBSTR('".$subclass."',1,2) ";
            }
        }
        $query .= " ORDER BY ";
        $query .= "     STAFFCD ";

        return $query;
    }

    //使用施設
    public function selectQuerySubForm3($term)
    {
        $query  = "SELECT ";
        $query .= "    faccd, ";
        $query .= "    facilityname, ";
        $query .= "    facilityabbv ";
        $query .= "FROM ";
        $query .= "    v_facility_mst ";
        $query .= "WHERE ";
        $query .= "    year='".SUBSTR($term, 0, 4)."' ";
        $query .= " ORDER BY 1 ";

        return $query;
    }
    //教科書
    public function selectQuerySubForm4($term)
    {
        $query  = "SELECT ";
        $query .= "    textbookcd, ";
        $query .= "    textbookname, ";
        $query .= "    textbookabbv ";
        $query .= "FROM ";
        $query .= "    v_textbook_mst ";
        $query .= "WHERE ";
        $query .= "    year='".SUBSTR($term, 0, 4)."' ";
        $query .= " ORDER BY 1 ";

        return $query;
    }

    //受講クラス取得
    public function getGradeClass($model, $term, $chaircd, $groupcd)
    {
        $query  = "SELECT ";
        $query .= "    t1.trgtgrade || t1.trgtclass as grade_class, ";
        $query .= "    t2.hr_nameabbv ";
        $query .= "FROM ";
        $query .= "    chair_cls_dat t1, ";
        $query .= "    schreg_regd_hdat t2 ";
        $query .= "WHERE ";
        $query .= "    t1.year='".SUBSTR($term, 0, 4)."' AND ";
        $query .= "    t1.semester='".SUBSTR($term, 5)."' AND ";
        if ($groupcd > "0000") {
            $query .= "    t1.groupcd='".$groupcd."' AND ";
        } else {
            $query .= "    t1.chaircd='".$chaircd."' AND ";
        }
        $query .= "    t1.year=t2.year AND ";
        $query .= "    t1.semester=t2.semester AND ";
        $query .= "    t1.trgtgrade=t2.grade AND ";
        $query .= "    t1.trgtclass=t2.hr_class ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " AND t2.GRADE IN (SELECT ";
                $query .= "                     G1.GRADE ";
                $query .= "                 FROM ";
                $query .= "                     SCHREG_REGD_GDAT G1 ";
                $query .= "                 WHERE ";
                $query .= "                     G1.YEAR         = t2.YEAR AND ";
                $query .= "                     G1.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind), "','")."') ";
                $query .= "                 ) ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND t2.GRADE IN (SELECT ";
            $query .= "                     G1.GRADE ";
            $query .= "                 FROM ";
            $query .= "                     SCHREG_REGD_GDAT G1 ";
            $query .= "                 WHERE ";
            $query .= "                     G1.YEAR         = t2.YEAR AND ";
            $query .= "                     G1.SCHOOL_KIND  = '".SCHOOLKIND."' ";
            $query .= "                 ) ";
        }
        $query .= " ORDER BY 1 ";

        return $query;
    }
    //科目担任取得
    public function getStaff($term, $chaircd)
    {
        $query  = "SELECT ";
        $query .= "    t1.staffcd, ";
        $query .= "    value(t1.chargediv,0) as chargediv, ";
        $query .= "    t2.staffname_show ";
        $query .= "FROM ";
        $query .= "    chair_stf_dat t1, ";
        $query .= "    v_staff_mst t2 ";
        $query .= "WHERE ";
        $query .= "    t1.year='".SUBSTR($term, 0, 4)."' AND ";
        $query .= "    t1.semester='".SUBSTR($term, 5)."' AND ";
        $query .= "    t1.chaircd='".$chaircd."' AND ";
        $query .= "    t1.year=t2.year AND ";
        $query .= "    t1.staffcd=t2.staffcd ";
        $query .= " ORDER BY t1.staffcd ";

        return $query;
    }
    //使用施設取得
    public function getFac($term, $chaircd)
    {
        $query  = "SELECT ";
        $query .= "    t1.faccd, ";
        $query .= "    t2.facilityabbv ";
        $query .= "FROM ";
        $query .= "    chair_fac_dat t1, ";
        $query .= "    v_facility_mst t2 ";
        $query .= "WHERE ";
        $query .= "    t1.year='".SUBSTR($term, 0, 4)."' AND ";
        $query .= "    t1.semester='".SUBSTR($term, 5)."' AND ";
        $query .= "    t1.chaircd='".$chaircd."' AND ";
        $query .= "    t1.year=t2.year AND ";
        $query .= "    t1.faccd=t2.faccd ";
        $query .= " ORDER BY t1.faccd";

        return $query;
    }
    //教科書取得
    public function getTextbook($term, $chaircd)
    {
        $query  = "SELECT ";
        $query .= "    t1.textbookcd, ";
        $query .= "    t2.textbookabbv ";
        $query .= "FROM ";
        $query .= "    chair_textbook_dat t1, ";
        $query .= "    v_textbook_mst t2 ";
        $query .= "WHERE ";
        $query .= "    t1.year='".SUBSTR($term, 0, 4)."' AND ";
        $query .= "    t1.semester='".SUBSTR($term, 5)."' AND ";
        $query .= "    t1.chaircd='".$chaircd."' AND ";
        $query .= "    t1.year=t2.year AND ";
        $query .= "    t1.textbookcd=t2.textbookcd ";
        $query .= " ORDER BY t1.textbookcd ";

        return $query;
    }
    //--- INSERT（講座データ）
    public function insertChair($model, $g)
    {
        $data["YEAR"][TEXT]         = substr($model->term, 0, 4);
        $data["SEMESTER"][TEXT]     = substr($model->term, 5);
        $data["CHAIRCD"][TEXT]      = $model->fields["CHAIRCD"][$g];
        $data["GROUPCD"][TEXT]      = $model->fields["GROUPCD"][$g];
        if ($model->Properties["useCurriculumcd"] == '1') {
            list($classcd, $school_kind, $curriculum_cd, $subclasscd) = explode("-", $model->subclasscd);
            $data["CLASSCD"][TEXT]      = $classcd;
            $data["SCHOOL_KIND"][TEXT]  = $school_kind;
            $data["CURRICULUM_CD"][TEXT]= $curriculum_cd;
            $data["SUBCLASSCD"][TEXT]   = $subclasscd;
        } else {
            $data["SUBCLASSCD"][TEXT]   = $model->subclasscd;
        }
        $data["CHAIRNAME"][TEXT]    = $model->fields["CHAIRNAME"][$g];
        $data["CHAIRABBV"][TEXT]    = $model->fields["CHAIRABBV"][$g];
        $data["TAKESEMES"][TEXT]    = $model->fields["TAKESEMES"][$g];
        $data["LESSONCNT"][NUMBER]  = $model->fields["LESSONCNT"][$g];
        $data["FRAMECNT"][NUMBER]   = $model->fields["FRAMECNT"][$g];
        //集計フラグ---05/01/19Add 1:集計する 0:集計しない
        $data["COUNTFLG"][TEXT]     = ($model->fields["COUNTFLG"][$g] == "1") ? $model->fields["COUNTFLG"][$g] : "0";
        $data["REGISTERCD"][TEXT]   = STAFFCD;
        $data["UPDATED"][NUMBER]    = "SYSDATE()";

        $query = Query::insertSQL($data, "chair_dat");

        return $query;
    }
    //--- UPDATE（講座データ）
    public function updateChair($model, $g)
    {
        $data["YEAR"][TEXT]         = substr($model->term, 0, 4);
        $data["SEMESTER"][TEXT]     = substr($model->term, 5);
        $data["CHAIRCD"][TEXT]      = $model->fields["CHAIRCD"][$g];
        $data["GROUPCD"][TEXT]      = $model->fields["GROUPCD"][$g];
        if ($model->Properties["useCurriculumcd"] == '1') {
            list($classcd, $school_kind, $curriculum_cd, $subclasscd) = explode("-", $model->subclasscd);
            $data["CLASSCD"][TEXT]      = $classcd;
            $data["SCHOOL_KIND"][TEXT]  = $school_kind;
            $data["CURRICULUM_CD"][TEXT]= $curriculum_cd;
            $data["SUBCLASSCD"][TEXT]   = $subclasscd;
        } else {
            $data["SUBCLASSCD"][TEXT]   = $model->subclasscd;
        }
        $data["CHAIRNAME"][TEXT]    = $model->fields["CHAIRNAME"][$g];
        $data["CHAIRABBV"][TEXT]    = $model->fields["CHAIRABBV"][$g];
        $data["TAKESEMES"][TEXT]    = $model->fields["TAKESEMES"][$g];
        $data["LESSONCNT"][NUMBER]  = $model->fields["LESSONCNT"][$g];
        $data["FRAMECNT"][NUMBER]   = $model->fields["FRAMECNT"][$g];
        //集計フラグ---05/01/19Add 1:集計する 0:集計しない
        $data["COUNTFLG"][TEXT]     = ($model->fields["COUNTFLG"][$g] == "1") ? $model->fields["COUNTFLG"][$g] : "0";
        $data["REGISTERCD"][TEXT]   = STAFFCD;
        $data["UPDATED"][NUMBER]    = "sysdate()";
        $where  = " WHERE year   = '".SUBSTR($model->term, 0, 4)."'";
        $where .= " AND semester = '".SUBSTR($model->term, 5)."'";
        $where .= " AND chaircd  = '".$model->fields["CHAIRCD"][$g]."'";

        $query = Query::updateSQL($data, "chair_dat", $where);

        return $query;
    }
    //--- CHEACK（講座データ）
    public function countChair($model, $g)
    {
        $query  = "SELECT COUNT(*) FROM chair_dat ";
        $query .= "WHERE year   = '".SUBSTR($model->term, 0, 4)."' ";
        $query .= "AND semester = '".SUBSTR($model->term, 5)."' ";
        $query .= "AND chaircd  = '".$model->fields["CHAIRCD"][$g]."' ";

        return $query;
    }
    //--- CHEACK（講座データ：群コード）---2004.04.25
    public function checktoGroupcd($model)
    {
        $query  = "SELECT COUNT(*) FROM chair_dat ";
        $query .= "WHERE year   = '".SUBSTR($model->term, 0, 4)."' ";
        $query .= "AND semester = '".SUBSTR($model->term, 5)."' ";
        $query .= "AND chaircd  = '".$model->fields["CHAIRCD"]."' ";
        $query .= "AND groupcd  = '0000' ";

        return $query;
    }
    //--- INSERT（講座受講クラスデータ）
    public function insertChairCls($model, $grade_class, $g)
    {
        $data["YEAR"][TEXT]         = substr($model->term, 0, 4);
        $data["SEMESTER"][TEXT]     = substr($model->term, 5);
        $data["CHAIRCD"][TEXT]      = $model->fields["CHAIRCD"][$g];
        $data["GROUPCD"][TEXT]      = $model->fields["GROUPCD"][$g];
        $data["TRGTGRADE"][TEXT]    = SUBSTR($grade_class, 0, 2);
        $data["TRGTCLASS"][TEXT]    = SUBSTR($grade_class, 2);
        $data["REGISTERCD"][TEXT]   = STAFFCD;
        $data["UPDATED"][NUMBER]    = "SYSDATE()";

        $query = Query::insertSQL($data, "chair_cls_dat");

        return $query;
    }
    //--- DELETE（講座受講クラスデータ）
    public function deleteChairCls($model, $g)
    {
        $query  = "DELETE FROM chair_cls_dat ";
        $query .= " WHERE year     = '".SUBSTR($model->term, 0, 4)."'";
        $query .= "   AND semester = '".SUBSTR($model->term, 5)."'";
        $query .= "   AND chaircd  = '".$model->fields["CHAIRCD"][$g]."'";

        return $query;
    }
    //--- INSERT（講座担当職員データ）
    public function insertChairStf($model, $staffcd, $chargediv, $g)
    {
        $data["YEAR"][TEXT]         = substr($model->term, 0, 4);
        $data["SEMESTER"][TEXT]     = substr($model->term, 5);
        $data["CHAIRCD"][TEXT]      = $model->fields["CHAIRCD"][$g];
        $data["STAFFCD"][TEXT]      = $staffcd;
        $data["CHARGEDIV"][NUMBER]  = $chargediv;
        $data["REGISTERCD"][TEXT]   = STAFFCD;
        $data["UPDATED"][NUMBER]    = "SYSDATE()";

        $query = Query::insertSQL($data, "chair_stf_dat");

        return $query;
    }
    //--- DELETE（講座担当職員データ）
    public function deleteChairStf($model, $g)
    {
        $query  = "DELETE FROM chair_stf_dat ";
        $query .= " WHERE year     = '".SUBSTR($model->term, 0, 4)."'";
        $query .= "   AND semester = '".SUBSTR($model->term, 5)."'";
        $query .= "   AND chaircd  = '".$model->fields["CHAIRCD"][$g]."'";

        return $query;
    }
    //--- INSERT（講座使用施設データ）
    public function insertChairFac($model, $faccd, $g)
    {
        $data["YEAR"][TEXT]         = substr($model->term, 0, 4);
        $data["SEMESTER"][TEXT]     = substr($model->term, 5);
        $data["CHAIRCD"][TEXT]      = $model->fields["CHAIRCD"][$g];
        $data["FACCD"][TEXT]        = $faccd;
        $data["REGISTERCD"][TEXT]   = STAFFCD;
        $data["UPDATED"][NUMBER]    = "SYSDATE()";

        $query = Query::insertSQL($data, "chair_fac_dat");

        return $query;
    }
    //--- DELETE（講座使用施設データ）
    public function deleteChairFac($model, $g)
    {
        $query  = "DELETE FROM chair_fac_dat ";
        $query .= " WHERE year     = '".SUBSTR($model->term, 0, 4)."'";
        $query .= "   AND semester = '".SUBSTR($model->term, 5)."'";
        $query .= "   AND chaircd  = '".$model->fields["CHAIRCD"][$g]."'";

        return $query;
    }
    //--- INSERT（講座詳細データ）
    public function insertChairDetail($model, $g, $seq)
    {
        $data = array();
        $data["YEAR"][TEXT]         = substr($model->term, 0, 4);
        $data["SEMESTER"][TEXT]     = substr($model->term, 5);
        $data["CHAIRCD"][TEXT]      = $model->fields["CHAIRCD"][$g];
        $data["SEQ"][TEXT]          = $seq;
        $data["REMARK1"][TEXT]      = $model->fields["STD_CNT"][$g];
        $data["REGISTERCD"][TEXT]   = STAFFCD;
        $data["UPDATED"][NUMBER]    = "SYSDATE()";

        $query = Query::insertSQL($data, "chair_detail_dat");

        return $query;
    }

    public function insertChairDetail2($model, $g, $seq)
    {
        $data = array();
        $data["YEAR"][TEXT]         = substr($model->term, 0, 4);
        $data["SEMESTER"][TEXT]     = substr($model->term, 5);
        $data["CHAIRCD"][TEXT]      = $model->fields["CHAIRCD"][$g];
        $data["SEQ"][TEXT]          = $seq;
        $data["REMARK1"][TEXT]      = $model->fields["KEIKOKUTEN_SOTEN"][$g];
        $data["REMARK2"][TEXT]      = $model->fields["KEIKOKUTEN_HYOUKA"][$g];
        $data["REMARK3"][TEXT]      = $model->fields["KEIKOKUTEN_HYOUTEI"][$g];
        $data["REGISTERCD"][TEXT]   = STAFFCD;
        $data["UPDATED"][NUMBER]    = "SYSDATE()";
        $query = Query::insertSQL($data, "chair_detail_dat");

        return $query;
    }
    public function insertChairDetail3($model, $g, $seq)
    {
        $data = array();
        $data["YEAR"][TEXT]         = substr($model->term, 0, 4);
        $data["SEMESTER"][TEXT]     = substr($model->term, 5);
        $data["CHAIRCD"][TEXT]      = $model->fields["CHAIRCD"][$g];
        $data["SEQ"][TEXT]          = $seq;
        $data["REMARK1"][TEXT]      = $model->fields["SEQ004_REMARK1"][$g];
        $data["REMARK2"][TEXT]      = $model->fields["SEQ004_REMARK2"][$g];
        $data["REMARK3"][TEXT]      = $model->fields["SEQ004_REMARK3"][$g];
        $data["REGISTERCD"][TEXT]   = STAFFCD;
        $data["UPDATED"][NUMBER]    = "SYSDATE()";
        $query = Query::insertSQL($data, "chair_detail_dat");

        return $query;
    }
    //--- DELETE（講座詳細データ）
    public function deleteChairDetail($model, $g, $seq = "")
    {
        $query  = " DELETE FROM chair_detail_dat ";
        $query .= " WHERE year      = '".SUBSTR($model->term, 0, 4)."' ";
        $query .= "   AND semester  = '".SUBSTR($model->term, 5)."' ";
        $query .= "   AND chaircd   = '".$model->fields["CHAIRCD"][$g]."' ";
        if ($seq) {
            $query .= "   AND seq       = '".$seq."' ";
        }

        return $query;
    }
    //--- DELETE（講座受講生徒データ）
    public function deleteChairStd($model, $g)
    {
        $query  = "DELETE FROM chair_std_dat ";
        $query .= " WHERE year     = '".SUBSTR($model->term, 0, 4)."'";
        $query .= "   AND semester = '".SUBSTR($model->term, 5)."'";
        $query .= "   AND chaircd  = '".$model->fields["CHAIRCD"][$g]."'";

        return $query;
    }
    //--- INSERT（講座教科書データ）
    public function insertChairTextbook($model, $textbookcd, $g)
    {
        $data["YEAR"][TEXT]         = substr($model->term, 0, 4);
        $data["SEMESTER"][TEXT]     = substr($model->term, 5);
        $data["CHAIRCD"][TEXT]      = $model->fields["CHAIRCD"][$g];
        $data["TEXTBOOKCD"][TEXT]   = $textbookcd;
        $data["REGISTERCD"][TEXT]   = STAFFCD;
        $data["UPDATED"][NUMBER]    = "SYSDATE()";

        $query = Query::insertSQL($data, "chair_textbook_dat");

        return $query;
    }
    //--- DELETE（講座教科書データ）
    public function deleteChairTextbook($model, $g)
    {
        $query  = "DELETE FROM chair_textbook_dat ";
        $query .= " WHERE year     = '".SUBSTR($model->term, 0, 4)."'";
        $query .= "   AND semester = '".SUBSTR($model->term, 5)."'";
        $query .= "   AND chaircd  = '".$model->fields["CHAIRCD"][$g]."'";

        return $query;
    }

    //「更新ボタン」押し時の処理（メイン）・・・データがない場合、追加。ある場合、更新。
    public function &getUpdateQuery($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        for ($g = 0; $g < $model->data_cnt; $g++) {
            //配列データ
            $grade_class = $staffcd = $chargediv = $textbookcd = $faccd = array();
            $grade_class    = explode(",", $model->fields["GRADE_CLASS"][$g]);
            $staffcd        = explode(",", $model->fields["STAFFCD"][$g]);
            $chargediv      = explode(",", $model->fields["CHARGEDIV"][$g]);
            $textbookcd     = explode(",", $model->fields["TEXTBOOKCD"][$g]);
            $faccd          = explode(",", $model->fields["FACCD"][$g]);

            //講座データ
            $query = knjb0031Query::countChair($model, $g);
            if ($db->getOne($query) > 0) {
                $db->query(knjb0031Query::updateChair($model, $g));
                $ret_val = "Update";
            } else {
                $db->query(knjb0031Query::insertChair($model, $g));
                $ret_val = "Insert";
            }
            //講座受講クラスデータ
            $db->query(knjb0031Query::deleteChairCls($model, $g));
            if ($model->fields["GROUPCD"][$g] == "0000") {
                if ($model->fields["GRADE_CLASS"][$g] != "") {
                    for ($i = 0; $i < get_count($grade_class); $i++) {
                        $db->query(knjb0031Query::insertChairCls($model, $grade_class[$i], $g));
                    }
                }
            }
            //講座担当職員データ
            $db->query(knjb0031Query::deleteChairStf($model, $g));
            if ($model->fields["STAFFCD"][$g] != "") {
                for ($i = 0; $i < get_count($staffcd); $i++) {
                    $db->query(knjb0031Query::insertChairStf($model, $staffcd[$i], $chargediv[$i], $g));
                }
            }
            //講座教科書データ
            $db->query(knjb0031Query::deleteChairTextbook($model, $g));
            if ($model->fields["TEXTBOOKCD"][$g] != "") {
                for ($i = 0; $i < get_count($textbookcd); $i++) {
                    $db->query(knjb0031Query::insertChairTextbook($model, $textbookcd[$i], $g));
                }
            }
            //講座使用施設データ
            $db->query(knjb0031Query::deleteChairFac($model, $g));
            if ($model->fields["FACCD"][$g] != "") {
                for ($i = 0; $i < get_count($faccd); $i++) {
                    $db->query(knjb0031Query::insertChairFac($model, $faccd[$i], $g));
                }
            }
            //講座詳細データ
            $db->query(knjb0031Query::deleteChairDetail($model, $g, '001'));
            if ($model->fields["STD_CNT"][$g] != "") {
                $db->query(knjb0031Query::insertChairDetail($model, $g, '001'));
            }
            if ($model->keikokutenFlag) {
                //講座詳細データ
                $db->query(knjb0031Query::deleteChairDetail($model, $g, '003'));
                $db->query(knjb0031Query::insertChairDetail2($model, $g, '003'));
            }
            if ($model->Properties['chairRetsuMeisho_Hyouji']) {
                //講座詳細データ
                $db->query(knjb0031Query::deleteChairDetail($model, $g, '004'));
                $db->query(knjb0031Query::insertChairDetail3($model, $g, '004'));
            }
        }//for

        $db->commit();
        Query::dbCheckIn($db);

        return $ret_val;
    }

    //「削除ボタン」押し時の処理（メイン）
    public function &getDeleteQuery($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        for ($g = 0; $g < $model->data_cnt; $g++) {
            //削除チェックＯＮのみ処理する
            if ($model->fields["DEL_FLG"][$g] != "1") {
                continue;
            }

            $query  = "DELETE FROM chair_dat ";
            $query .= " WHERE year     = '".SUBSTR($model->term, 0, 4)."'";
            $query .= "   AND semester = '".SUBSTR($model->term, 5)."'";
            $query .= "   AND chaircd  = '".$model->fields["CHAIRCD"][$g]."'";
            $db->query($query);

            if ($model->fields["GROUPCD"][$g] == "0000") {
                $db->query(knjb0031Query::deleteChairCls($model, $g));
            }
            $db->query(knjb0031Query::deleteChairStf($model, $g));
            $db->query(knjb0031Query::deleteChairTextbook($model, $g));
            $db->query(knjb0031Query::deleteChairFac($model, $g));
            $db->query(knjb0031Query::deleteChairDetail($model, $g));
            $db->query(knjb0031Query::deleteChairStd($model, $g));
        }//for

        $db->commit();
        Query::dbCheckIn($db);
        return "succeeded";
    }


    // HRクラスの自動名簿生成:select
    public function selectAutoChairStd($model, $year, $semester)
    {
        $query  = "";

        // 対象講座を抽出
        // -- ログインの年度、学期であること
        // -- 群コードが '0000' であること
        // -- 講座受講生徒データがゼロ件であること
        // -- 受講クラスが1つだけ指定されていること
        $query .= " WITH GET_CHAIRCD AS ( ";
        $query .= "      SELECT W1.CHAIRCD ";
        $query .= "       FROM CHAIR_DAT W1 ";
        $query .= "      WHERE W1.YEAR = '{$year}' ";
        $query .= "        AND W1.SEMESTER = '{$semester}' ";
        $query .= "        AND W1.GROUPCD = '0000' ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "    AND W1.CLASSCD || '-' || W1.SCHOOL_KIND || '-' || W1.CURRICULUM_CD || '-' || W1.SUBCLASSCD = '{$model->subclasscd}' ";
        } else {
            $query .= "    AND W1.SUBCLASSCD = '{$model->subclasscd}' ";
        }
        $query .= "        AND NOT EXISTS (SELECT 'X' ";
        $query .= "                          FROM CHAIR_STD_DAT W2 ";
        $query .= "                         WHERE W2.YEAR     = W1.YEAR ";
        $query .= "                           AND W2.SEMESTER = W1.SEMESTER ";
        $query .= "                           AND W2.CHAIRCD  = W1.CHAIRCD ) ";
        $query .= "        AND W1.CHAIRCD IN (SELECT W1.CHAIRCD ";
        $query .= "                           FROM CHAIR_CLS_DAT W1 ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "                       INNER JOIN SCHREG_REGD_GDAT G1 ";
                $query .= "                             ON G1.YEAR  = W1.YEAR ";
                $query .= "                            AND G1.GRADE = W1.TRGTGRADE ";
                $query .= "                            AND G1.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind), "','")."') ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= "                       INNER JOIN SCHREG_REGD_GDAT G1 ";
            $query .= "                             ON G1.YEAR  = W1.YEAR ";
            $query .= "                            AND G1.GRADE = W1.TRGTGRADE ";
            $query .= "                            AND G1.SCHOOL_KIND  = '".SCHOOLKIND."' ";
        }
        $query .= "                           WHERE W1.YEAR     = '{$year}' ";
        $query .= "                              AND W1.SEMESTER = '{$semester}' ";
        $query .= "                              AND W1.GROUPCD  = '0000' ";
        $query .= "                           GROUP BY ";
        $query .= "                               W1.CHAIRCD ";
        $query .= "                           HAVING COUNT(*) = 1 ) ";
        $query .= "     ) ";
        // -- 受講クラスを取り込み、名簿を算出する
        $query .= " SELECT DISTINCT ";
        $query .= "     W1.CHAIRCD, ";
        $query .= "     W3.GRADE, ";
        $query .= "     W3.HR_CLASS, ";
        $query .= "     W3.ATTENDNO, ";
        $query .= "     W5.NAME, ";
        $query .= "     W3.SCHREGNO, ";
        $query .= "     W4.SDATE, ";
        $query .= "     W4.EDATE ";
        $query .= "   FROM CHAIR_CLS_DAT   W1, ";
        $query .= "        GET_CHAIRCD     W2, ";
        $query .= "        SCHREG_REGD_DAT W3, ";
        $query .= "        SEMESTER_MST    W4, ";
        $query .= "        SCHREG_BASE_MST W5 ";
        $query .= "  WHERE W1.YEAR = '{$year}' ";
        $query .= "    AND W1.SEMESTER = '{$semester}' ";
        $query .= "    AND W1.CHAIRCD = W2.CHAIRCD ";
        $query .= "    AND W1.GROUPCD = '0000' ";
        $query .= "    AND W3.YEAR = W1.YEAR ";
        $query .= "    AND W3.SEMESTER = W1.SEMESTER ";
        $query .= "    AND W3.GRADE = W1.TRGTGRADE ";
        $query .= "    AND W3.HR_CLASS = W1.TRGTCLASS ";
        $query .= "    AND W4.YEAR = W1.YEAR ";
        $query .= "    AND W4.SEMESTER = W1.SEMESTER ";
        $query .= "    AND W5.SCHREGNO = W3.SCHREGNO ";
        $query .= " ORDER BY W1.CHAIRCD, W3.SCHREGNO ";

        return $query;
    }


    // HRクラスの自動名簿生成:insert
    // -- 以下に生成するテーブルのデフォルト値について記述する。
    // -- ・適用開始日付～適用終了日付の期間は学期の開始～終了とする
    // -- ・座席行、座席列は null とする
    public function insertAutoChairStd($data, $year, $semester, $registercd)
    {
        $auto_data = array();
        $auto_data["YEAR"][TEXT]        = $year;
        $auto_data["SEMESTER"][TEXT]    = $semester;
        $auto_data["CHAIRCD"][TEXT]     = $data["CHAIRCD"];
        $auto_data["SCHREGNO"][TEXT]    = $data["SCHREGNO"];
        $auto_data["APPDATE"][TEXT]     = $data["SDATE"];
        $auto_data["APPENDDATE"][TEXT]  = $data["EDATE"];
        $auto_data["ROW"][TEXT]         = "";
        $auto_data["COLUMN"][TEXT]      = "";
        $auto_data["REGISTERCD"][TEXT]  = $registercd;
        $auto_data["UPDATED"][NUMBER]   = "sysdate()";

        $query = Query::insertSQL($auto_data, "chair_std_dat");
        return $query;
    }
}
