<?php

require_once('for_php7.php');
class knjh538aQuery extends Query
{

    /* 学期取得 */
    public function getSemester()
    {
        $query  = " SELECT ";
        $query .= "     SEMESTER AS VALUE, ";
        $query .= "     SEMESTERNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' AND";
        $query .= "     SEMESTER < '9' ";

        return $query;
    }

    //年組一覧
    public function getGrade($model)
    {
        $query  = "SELECT DISTINCT ";
        $query .= "    T1.GRADE AS VALUE, ";
        $query .= "    L1.GRADE_NAME1 AS LABEL ";
        $query .= "FROM ";
        $query .= "    SCHREG_REGD_HDAT T1 ";
        $query .= "    LEFT JOIN SCHREG_REGD_GDAT L1 ON T1.YEAR = L1.YEAR ";
        $query .= "         AND T1.GRADE = L1.GRADE ";
        $query .= "WHERE ";
        $query .= "    T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "    AND T1.SEMESTER = '".$model->field["SEMESTER"]."' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "    AND L1.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind), "','")."') ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "    AND L1.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= "ORDER BY ";
        $query .= "    VALUE ";

        return $query;
    }

    //実力種別
    public function getProfDiv()
    {
        $query  = " SELECT ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND NAMECD1 = 'H508' ";
        $query .= " ORDER BY ";
        $query .= "     NAMECD2 ";

        return $query;
    }

    //実力テスト
    public function getProfMst($model)
    {
        $query  = "SELECT ";
        $query .= "    T1.PROFICIENCYCD AS VALUE, ";
        $query .= "    T1.PROFICIENCYCD || ':' || L1.PROFICIENCYNAME1 AS LABEL ";
        $query .= "FROM ";
        $query .= "    PROFICIENCY_YMST T1 ";
        $query .= "    LEFT JOIN PROFICIENCY_MST L1 ON T1.PROFICIENCYDIV = L1.PROFICIENCYDIV ";
        $query .= "         AND T1.PROFICIENCYCD = L1.PROFICIENCYCD ";
        $query .= "WHERE ";
        $query .= "    T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "    AND T1.SEMESTER = '".$model->field["SEMESTER"]."' ";
        $query .= "    AND T1.PROFICIENCYDIV = '".$model->field["PROFICIENCYDIV"]."' ";
        $query .= "    AND T1.GRADE = '".$model->field["GRADE"]."' ";
        $query .= "ORDER BY ";
        $query .= "    VALUE ";

        return $query;
    }

    //実力科目一覧
    public function getProfSubclassMst($model, $div = "")
    {
        $query  = " WITH REGD_T AS ( ";
        $query .= " SELECT DISTINCT ";
        $query .= "     T1.YEAR, ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.COURSECD, ";
        $query .= "     T1.MAJORCD, ";
        $query .= "     T1.COURSECODE, ";
        $query .= "     L1.GROUP_CD ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= "     LEFT JOIN COURSE_GROUP_CD_DAT L1 ON L1.YEAR = T1.YEAR ";
        $query .= "          AND L1.GRADE = T1.GRADE ";
        $query .= "          AND L1.COURSECD || L1.MAJORCD || L1.COURSECODE = T1.COURSECD || T1.MAJORCD || T1.COURSECODE ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND T1.SEMESTER = '".$model->field["SEMESTER"]."' ";
        $query .= "     AND T1.GRADE = '".$model->field["GRADE"]."' ";
        $query .= " ), GROUP_T AS ( ";
        $query .= " SELECT DISTINCT ";
        $query .= "     T1.PROFICIENCY_SUBCLASS_CD ";
        $query .= " FROM ";
        $query .= "     PROFICIENCY_PERFECT_COURSE_DAT T1 ";
        $query .= "     LEFT JOIN REGD_T L1 ON L1.YEAR = T1.YEAR ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND T1.SEMESTER = '{$model->field["SEMESTER"]}' ";
        $query .= "     AND T1.PROFICIENCYDIV = '{$model->field["PROFICIENCYDIV"]}' ";
        $query .= "     AND T1.PROFICIENCYCD = '{$model->field["PROFICIENCY_TARGET"]}' ";
        $query .= "     AND T1.GRADE = CASE WHEN DIV = '01' ";
        $query .= "                         THEN '00' ";
        $query .= "                         ELSE L1.GRADE ";
        $query .= "                    END ";
        $query .= "     AND T1.COURSECD || T1.MAJORCD || T1.COURSECODE = CASE WHEN T1.DIV IN ('01','02') ";
        $query .= "                                                           THEN '00000000' ";
        $query .= "                                                           ELSE CASE WHEN T1.DIV = '04' ";
        $query .= "                                                                     THEN '0' || L1.GROUP_CD || '0000' ";
        $query .= "                                                                     ELSE L1.COURSECD || L1.MAJORCD || L1.COURSECODE ";
        $query .= "                                                                END ";
        $query .= "                                                      END ";
        $query .= " ) ";
        if ($div == "YOMIKAE") {
            $query .= " SELECT ";
            $query .= "     T1.PROFICIENCY_SUBCLASS_CD, ";
            if ($model->Properties["useCurriculumcd"] == "1") {
                $query .= "     T1.CLASSCD, ";
                $query .= "     T1.SCHOOL_KIND, ";
                $query .= "     T1.CURRICULUM_CD, ";
            }
            $query .= "     T1.SUBCLASSCD ";
            $query .= " FROM ";
            $query .= "     PROFICIENCY_SUBCLASS_YDAT T1 ";
            $query .= " WHERE ";
            $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
            $query .= "     AND T1.SEMESTER = '".$model->field["SEMESTER"]."' ";
            $query .= "     AND T1.PROFICIENCYDIV = '".$model->field["PROFICIENCYDIV"]."' ";
            $query .= "     AND T1.PROFICIENCYCD = '".$model->field["PROFICIENCY_TARGET"]."' ";
            $query .= "     AND T1.GRADE = '".$model->field["GRADE"]."' ";
            $query .= "     AND T1.PROFICIENCY_SUBCLASS_CD IN (SELECT I1.PROFICIENCY_SUBCLASS_CD FROM GROUP_T I1) ";
            if ($model->Properties["use_prg_schoolkind"] == "1") {
                if ($model->selectSchoolKind) {
                    $query .= "     AND T1.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind), "','")."') ";
                }
            } elseif ($model->Properties["useSchool_KindField"] == "1") {
                $query .= "     AND T1.SCHOOL_KIND = '".SCHOOLKIND."' ";
            }
        } else {
            $query .= " SELECT DISTINCT ";
            $query .= "     T1.PROFICIENCY_SUBCLASS_CD AS VALUE, ";
            $query .= "     T1.PROFICIENCY_SUBCLASS_CD ||':'|| T1.SUBCLASS_ABBV AS LABEL ";
            $query .= " FROM ";
            $query .= "     PROFICIENCY_SUBCLASS_MST T1 ";
            $query .= " WHERE ";
            $query .= "     T1.PROFICIENCY_SUBCLASS_CD IN (SELECT I1.PROFICIENCY_SUBCLASS_CD FROM GROUP_T I1) ";
            if ($model->Properties["use_prg_schoolkind"] == "1") {
                if ($model->selectSchoolKind) {
                    $query .= "     AND T1.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind), "','")."') ";
                }
            } elseif ($model->Properties["useSchool_KindField"] == "1") {
                $query .= "     AND T1.SCHOOL_KIND = '".SCHOOLKIND."' ";
            }
            $query .= " ORDER BY ";
            $query .= "     VALUE ";
        }

        return $query;
    }

    //講座データの取得
    public function selectChairQuery($model)
    {
        $subclassCd = "IN ('";
        $sep = "";
        if (is_array($model->kamokuYomikae[$model->field["PROFICIENCY_SUBCLASS_CD"]])) {
            foreach ($model->kamokuYomikae[$model->field["PROFICIENCY_SUBCLASS_CD"]] as $key => $val) {
                if ($model->Properties["useCurriculumcd"] == "1") {
                    $subclassCd .= $sep.$val["CLASSCD"].$val["SCHOOL_KIND"].$val["CURRICULUM_CD"].$val["SUBCLASSCD"];
                } else {
                    $subclassCd .= $sep.$val["SUBCLASSCD"];
                }
                $sep = "', '";
            }
        }
        $subclassCd .= "')";
        $query  = " WITH REGD_T AS ( ";
        $query .= " SELECT DISTINCT ";
        $query .= "     T1.SCHREGNO ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= "     LEFT JOIN COURSE_GROUP_CD_DAT L1 ON L1.YEAR = T1.YEAR ";
        $query .= "          AND L1.GRADE = T1.GRADE ";
        $query .= "          AND L1.COURSECD || L1.MAJORCD || L1.COURSECODE = T1.COURSECD || T1.MAJORCD || T1.COURSECODE ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND T1.SEMESTER = '".$model->field["SEMESTER"]."' ";
        $query .= "     AND T1.GRADE = '".$model->field["GRADE"]."' ";
        $query .= " ) ";
        $query .= " SELECT DISTINCT ";
        $query .= "     T1.CHAIRCD VALUE, ";
        $query .= "     T1.CHAIRCD || ':' || T1.CHAIRNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     CHAIR_DAT T1, ";
        $query .= "     CHAIR_STD_DAT T2, ";
        $query .= "     CHAIR_STF_DAT T3 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR       = '".CTRL_YEAR."' AND ";
        $query .= "     T1.SEMESTER   = '".$model->field["SEMESTER"]."' AND ";
        $query .= "     T2.YEAR       = T1.YEAR AND ";
        $query .= "     T2.SEMESTER   = T1.SEMESTER AND ";
        $query .= "     T2.CHAIRCD    = T1.CHAIRCD AND ";
        $query .= "     T2.SCHREGNO IN (SELECT REGD_T.SCHREGNO FROM REGD_T) AND ";
        $query .= "     T1.YEAR       = T3.YEAR AND ";
        $query .= "     T1.SEMESTER   = T3.SEMESTER AND ";
        $query .= "     T1.CHAIRCD    = T3.CHAIRCD AND ";
        //制限付の場合
        if ($model->auth != DEF_UPDATABLE && $model->auth != DEF_REFERABLE) {
            $query .= "       t3.STAFFCD = '".STAFFCD."' AND ";
        }
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "     T1.CLASSCD || T1.SCHOOL_KIND || T1.CURRICULUM_CD || T1.SUBCLASSCD ".$subclassCd." ";
        } else {
            $query .= "     T1.SUBCLASSCD ".$subclassCd." ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    public function getCtl($model)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     ADMIN_CONTROL_PROFICIENCY_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND PROFICIENCYDIV = '{$model->field["PROFICIENCYDIV"]}' ";
        $query .= "     AND PROFICIENCYCD = '{$model->field["PROFICIENCY_TARGET"]}' ";

        return $query;
    }

    public function selectMainData($model, $execute_date)
    {
        $query  = " SELECT ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T1.ATTENDNO AS ORG_ATTENDNO, ";
        $query .= "     T1.COURSECD || T1.MAJORCD || T1.COURSECODE as COURSE, ";
        $query .= "     L4.GROUP_CD, ";
        $query .= "     L1.HR_NAME, ";
        $query .= "     L1.HR_NAME || '-' || T1.ATTENDNO AS ATTENDNO, ";
        $query .= "     T1.SCHREGNO || '　' || L2.NAME AS NAME_SHOW, ";
        $query .= "     L2.NAME, ";
        $query .= "     L3.SCORE, ";
        $query .= "     L3.SCORE_DI, ";
        $query .= "     L2.GRD_DATE, ";
        $query .= "     L5.TRANSFER_SDATE, ";
        $query .= "     L5.TRANSFER_EDATE ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= "     LEFT JOIN SCHREG_REGD_HDAT L1 ON L1.YEAR = T1.YEAR ";
        $query .= "          AND L1.SEMESTER = T1.SEMESTER ";
        $query .= "          AND L1.GRADE || L1.HR_CLASS = T1.GRADE || T1.HR_CLASS ";
        $query .= "     LEFT JOIN SCHREG_BASE_MST L2 ON L2.SCHREGNO = T1.SCHREGNO ";
        $query .= "     LEFT JOIN PROFICIENCY_DAT L3 ON L3.YEAR = T1.YEAR ";
        $query .= "          AND L3.SEMESTER = '".$model->field["SEMESTER"]."' ";
        $query .= "          AND L3.PROFICIENCYDIV = '".$model->field["PROFICIENCYDIV"]."' ";
        $query .= "          AND L3.PROFICIENCYCD = '".$model->field["PROFICIENCY_TARGET"]."' ";
        $query .= "          AND L3.SCHREGNO = T1.SCHREGNO ";
        $query .= "          AND L3.PROFICIENCY_SUBCLASS_CD = '".$model->field["PROFICIENCY_SUBCLASS_CD"]."' ";
        $query .= "     LEFT JOIN COURSE_GROUP_CD_DAT L4 ON L4.YEAR = T1.YEAR ";
        $query .= "          AND L4.GRADE = T1.GRADE ";
        $query .= "          AND L4.COURSECD || L4.MAJORCD || L4.COURSECODE = T1.COURSECD || T1.MAJORCD || T1.COURSECODE ";
        $query .= "     LEFT JOIN ( ";
        $query .= "         SELECT ";
        $query .= "             SCHREGNO, ";
        $query .= "             MIN(TRANSFER_SDATE) AS TRANSFER_SDATE, ";
        $query .= "             MAX(TRANSFER_EDATE) AS TRANSFER_EDATE ";
        $query .= "         FROM ";
        $query .= "             SCHREG_TRANSFER_DAT ";
        $query .= "         WHERE ";
        $query .= "             FISCALYEAR(TRANSFER_SDATE) = '".CTRL_YEAR."' ";
        $query .= "         GROUP BY ";
        $query .= "             SCHREGNO ";
        $query .= "     ) L5 ON L5.SCHREGNO = T1.SCHREGNO ";
        $query .= "     INNER JOIN (SELECT ";
        $query .= "                     E1.SCHREGNO, ";
        $query .= "                     E1.CHAIRCD ";
        $query .= "                 FROM ";
        $query .= "                     CHAIR_STD_DAT E1 ";
        $query .= "                 WHERE ";
        $query .= "                     E1.YEAR = '".CTRL_YEAR."' ";
        $query .= "                     AND E1.CHAIRCD = '".$model->field["CHAIRCD"]."'  ";
        $query .= "                     AND '".$execute_date."' BETWEEN APPDATE AND APPENDDATE ";
        $query .= "         ) I1 ON I1.SCHREGNO = T1.SCHREGNO ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND T1.SEMESTER = '".$model->field["SEMESTER"]."' ";
        $query .= "     AND T1.GRADE = '".$model->field["GRADE"]."' ";
        $query .= " ORDER BY ";
        $query .= "     T1.HR_CLASS, ";
        $query .= "     T1.ATTENDNO ";

        return $query;
    }

    //満点マスタ
    public function getPerfect($model, $data)
    {
        if ($model->Properties["usePerfectCourse"] == "1") {
            $query  = "";
            $query .= "SELECT ";
            $query .= "    CASE WHEN DIV IS NULL ";
            $query .= "         THEN 100 ";
            $query .= "         ELSE PERFECT ";
            $query .= "    END AS PERFECT ";
            $query .= "FROM ";
            $query .= "    PROFICIENCY_PERFECT_COURSE_DAT ";
            $query .= "WHERE ";
            $query .= "    YEAR = '".CTRL_YEAR."' ";
            $query .= "    AND SEMESTER = '{$model->field["SEMESTER"]}' ";
            $query .= "    AND PROFICIENCYDIV = '{$model->field["PROFICIENCYDIV"]}' ";
            $query .= "    AND PROFICIENCYCD = '{$model->field["PROFICIENCY_TARGET"]}' ";
            $query .= "    AND PROFICIENCY_SUBCLASS_CD = '{$model->field["PROFICIENCY_SUBCLASS_CD"]}' ";
            $query .= "    AND GRADE = CASE WHEN DIV = '01' ";
            $query .= "                     THEN '00' ";
            $query .= "                     ELSE '{$data["GRADE"]}' ";
            $query .= "                END ";
            $query .= "    AND COURSECD || MAJORCD || COURSECODE = CASE WHEN DIV IN ('01','02') ";
            $query .= "                                                 THEN '00000000' ";
            $query .= "                                                 ELSE CASE WHEN DIV = '04' ";
            $query .= "                                                           THEN '0' || '{$data["GROUP_CD"]}' || '0000' ";
            $query .= "                                                           ELSE '{$data["COURSE"]}' ";
            $query .= "                                                      END ";
            $query .= "                                            END ";
        } else {
            $query  = "";
            $query .= "SELECT ";
            $query .= "    CASE WHEN COURSE_DIV IS NULL ";
            $query .= "         THEN 100 ";
            $query .= "         ELSE PERFECT ";
            $query .= "    END AS PERFECT ";
            $query .= "FROM ";
            $query .= "    PROFICIENCY_PERFECT_DAT ";
            $query .= "WHERE ";
            $query .= "    YEAR = '".CTRL_YEAR."' ";
            $query .= "    AND PROFICIENCY_SUBCLASS_CD = '{$model->field["PROFICIENCY_SUBCLASS_CD"]}' ";
            $query .= "    AND GRADE = '{$data["GRADE"]}' ";
        }
        return $query;
    }

    /* 実力データの更新 */
    public function getUpdateQuery($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        foreach ($model->field["SCHREGNO"] as $key => $schregno) {
            if (in_array($schregno, $model->unSetSchreg)) {
                continue;
            }
            /* 更新対象データが存在するかをチェック */
            $query  = " SELECT ";
            $query .= "     COUNT(*) ";
            $query .= " FROM ";
            $query .= "     PROFICIENCY_DAT ";
            $query .= " WHERE ";
            $query .= "     YEAR = '".CTRL_YEAR."' ";
            $query .= "     AND SEMESTER = '".$model->field["SEMESTER"]."' ";
            $query .= "     AND PROFICIENCYDIV = '".$model->field["PROFICIENCYDIV"]."' ";
            $query .= "     AND PROFICIENCYCD = '".$model->field["PROFICIENCY_TARGET"]."' ";
            $query .= "     AND SCHREGNO = '".$schregno."' ";
            $query .= "     AND PROFICIENCY_SUBCLASS_CD = '".$model->field["PROFICIENCY_SUBCLASS_CD"]."' ";

            $flag = $db->getOne($query);

            $data = array();

            /* 更新対象データがある時 - UPDATE */
            if ($flag > 0) {
                $where  = " WHERE ";
                $where .= "     YEAR = '".CTRL_YEAR."' ";
                $where .= "     AND SEMESTER = '".$model->field["SEMESTER"]."' ";
                $where .= "     AND PROFICIENCYDIV = '".$model->field["PROFICIENCYDIV"]."' ";
                $where .= "     AND PROFICIENCYCD = '".$model->field["PROFICIENCY_TARGET"]."' ";
                $where .= "     AND SCHREGNO = '".$schregno."' ";
                $where .= "     AND PROFICIENCY_SUBCLASS_CD = '".$model->field["PROFICIENCY_SUBCLASS_CD"]."' ";
            } else {
                $data["YEAR"][TEXT]                     = CTRL_YEAR;
                $data["SEMESTER"][TEXT]                 = $model->field["SEMESTER"];
                $data["PROFICIENCYDIV"][TEXT]           = $model->field["PROFICIENCYDIV"];
                $data["PROFICIENCYCD"][TEXT]            = $model->field["PROFICIENCY_TARGET"];
                $data["SCHREGNO"][TEXT]                 = $schregno;
                $data["PROFICIENCY_SUBCLASS_CD"][TEXT]  = $model->field["PROFICIENCY_SUBCLASS_CD"];
            }

            /* 更新データをセット */
            $setScore = strlen($model->field["SCORE"][$key]) > 0 ? $model->field["SCORE"][$key] : null;
            if ($setScore == "*") {
                $data["SCORE"][NUMBER]    = null;
                $data["SCORE_DI"][TEXT]    = $setScore;
            } else {
                $data["SCORE"][NUMBER]    = $setScore;
                $data["SCORE_DI"][TEXT]    = null;
            }
            $data["REGISTERCD"][TEXT] = STAFFCD ;
            $data["UPDATED"][FUNC]    = "sysdate()";
            /* 更新対象データがある時 - UPDATE */
            if ($flag > 0) {
                $query = Query::updateSQL($data, "PROFICIENCY_DAT", $where);
            } else {
                $query = Query::insertSQL($data, "PROFICIENCY_DAT");
            }
            $db->query($query);
        }
        $db->commit();
        Query::dbCheckIn($db);
        return true;
    }

    /* 実力データの削除 */
    public function getDeleteQuery($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        foreach ($model->field["SCHREGNO"] as $key => $schregno) {
            /* 更新対象データが存在するかをチェック */
            $query  = " DELETE FROM PROFICIENCY_DAT ";
            $query .= " WHERE ";
            $query .= "     YEAR = '".CTRL_YEAR."' ";
            $query .= "     AND SEMESTER = '".$model->field["SEMESTER"]."' ";
            $query .= "     AND PROFICIENCYDIV = '".$model->field["PROFICIENCYDIV"]."' ";
            $query .= "     AND PROFICIENCYCD = '".$model->field["PROFICIENCY_TARGET"]."' ";
            $query .= "     AND SCHREGNO = '".$schregno."' ";
            $query .= "     AND PROFICIENCY_SUBCLASS_CD = '".$model->field["PROFICIENCY_SUBCLASS_CD"]."' ";

            $db->query($query);
        }

        $db->commit();
        Query::dbCheckIn($db);
        return true;
    }
}