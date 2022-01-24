<?php

require_once('for_php7.php');
class knjj141Query extends Query {

        //学年取得
        function getGrade($model) {
            $query  = " SELECT ";
            $query .= "     GRADE AS VALUE, ";
            $query .= "     GRADE_NAME1 AS LABEL ";
            $query .= " FROM ";
            $query .= "     SCHREG_REGD_GDAT ";
            $query .= " WHERE ";
            $query .= "     YEAR = '".CTRL_YEAR."' ";
            if ($model->Properties["use_prg_schoolkind"] == "1") {
                if ($model->selectSchoolKind) {
                    $query .= "     AND SCHOOL_KIND IN " . $model->selectSchoolKindSql . " ";
                }
            } else if ($model->Properties["useSchool_KindField"] == "1") {
                $query .= "     AND SCHOOL_KIND = '".SCHOOLKIND."' ";
            }
            $query .= " ORDER BY ";
            $query .= "     VALUE ";

            return $query;
        }

        function getClubSql($model) {
            $query = " SELECT ";
            if ($model->Properties["use_prg_schoolkind"] == "1" || $model->Properties["useSchool_KindField"] == "1") {
                $query .= "     T1.SCHOOLCD, ";
                $query .= "     T1.SCHOOL_KIND, ";
            }
            $query .= "     T1.CLUBCD, ";
            $query .= "     T2.CLUBNAME ";
            $query .= " FROM ";
            $query .= "     CLUB_YDAT T1, ";
            $query .= "     CLUB_MST T2 ";
            $query .= " WHERE ";
            $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
            $query .= "     AND T1.CLUBCD   = T2.CLUBCD ";
            if ($model->Properties["useClubMultiSchoolKind"] == "1") {
                $query .= "   AND T1.SCHOOLCD = '".SCHOOLCD."' ";
                $query .= "   AND T1.SCHOOL_KIND = '".SCHOOLKIND."' ";
                $query .= "   AND T2.SCHOOLCD = '".SCHOOLCD."' ";
                $query .= "   AND T2.SCHOOL_KIND = '".SCHOOLKIND."' ";
            } else if ($model->Properties["use_prg_schoolkind"] == "1") {
                $query .= "   AND T2.SCHOOLCD = T1.SCHOOLCD ";
                $query .= "   AND T2.SCHOOL_KIND = T1.SCHOOL_KIND ";
                if ($model->selectSchoolKind != "") {
                    $query .= "   AND T1.SCHOOLCD = '".SCHOOLCD."' ";
                    $query .= "   AND T1.SCHOOL_KIND IN ".$model->selectSchoolKindSql." ";
                }
            } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "" && SCHOOLCD != "") {
                $query .= "   AND T1.SCHOOLCD = '".SCHOOLCD."' ";
                $query .= "   AND T1.SCHOOL_KIND = '".SCHOOLKIND."' ";
                $query .= "   AND T2.SCHOOLCD = '".SCHOOLCD."' ";
                $query .= "   AND T2.SCHOOL_KIND = '".SCHOOLKIND."' ";
            }
            $query .= " ORDER BY ";
            if ($model->Properties["use_prg_schoolkind"] == "1" || $model->Properties["useSchool_KindField"] == "1") {
                $query .= "     T1.SCHOOLCD, ";
                $query .= "     T1.SCHOOL_KIND, ";
            }
            $query .= "     T1.CLUBCD ";
            return $query;
        }

        function getRegdHdat($model) {
            $query .= " SELECT ";
            $query .= "     REGDH.* ";
            $query .= "     ,GDAT.SCHOOL_KIND ";
            $query .= " FROM ";
            $query .= "     SCHREG_REGD_HDAT REGDH ";
            $query .= " INNER JOIN SCHREG_REGD_GDAT GDAT ON GDAT.YEAR = REGDH.YEAR AND GDAT.GRADE = REGDH.GRADE ";
            if ($model->Properties["useClubMultiSchoolKind"] == "1") {
                //SCHOOL_KINDで絞らない
            } else if ($model->Properties["use_prg_schoolkind"] == "1") {
                if ($model->selectSchoolKind) {
                    $query .= "   AND GDAT.SCHOOL_KIND IN ".$model->selectSchoolKindSql." ";
                }
            } else if ($model->Properties["useSchool_KindField"] == "1") {
                if (SCHOOLKIND != "") {
                    $query .= "       AND GDAT.SCHOOL_KIND = '".SCHOOLKIND."' ";
                }
            }
            $query .= " WHERE ";
            $query .= "     REGDH.YEAR = '".CTRL_YEAR."' ";
            $query .= "     AND REGDH.SEMESTER = '".CTRL_SEMESTER."' ";
            if ($model->field["GRADE"] != "ALL") {
                $query .= " AND REGDH.GRADE = '".$model->field["GRADE"]."' ";
            }
            $query .= " ORDER BY ";
            $query .= "     GDAT.SCHOOL_KIND, ";
            $query .= "     REGDH.GRADE, ";
            $query .= "     REGDH.HR_CLASS ";

            return $query;
        }

        function getClubDataSql($model) {
            $cnvBDate = str_replace('/', '-', $model->field["BASE_DATE"]);
            $query .= " WITH REGD_H AS ( ";
            $query .= " SELECT ";
            $query .= "     REGDH.*, ";
            $query .= "     REGD_G.SCHOOL_KIND, ";
            $query .= "     N1.NAMECD2 AS SEX, ";
            $query .= "     N1.NAME1 ";
            $query .= " FROM ";
            $query .= "     SCHREG_REGD_HDAT REGDH ";
            $query .= " LEFT JOIN SCHREG_REGD_GDAT REGD_G ";
            $query .= "   ON REGD_G.YEAR = REGDH.YEAR ";
            $query .= "  AND REGD_G.GRADE = REGDH.GRADE ";
            if ($model->Properties["useClubMultiSchoolKind"] == "1") {
                //全生徒が対象
            } else if ($model->Properties["use_prg_schoolkind"] == "1") {
                if ($model->selectSchoolKind != "") {
                    $query .= " INNER JOIN SCHREG_REGD_GDAT REGDG ON REGDG.YEAR = REGDH.YEAR AND REGDG.GRADE = REGDH.GRADE ";
                    $query .= "   AND REGDG.SCHOOL_KIND IN ".$model->selectSchoolKindSql." ";
                }
            } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLCD != "") {
                $query .= "     INNER JOIN SCHREG_REGD_GDAT REGDG ON REGDG.YEAR = REGDH.YEAR AND REGDG.GRADE = REGDH.GRADE ";
                $query .= "       AND REGDG.SCHOOL_KIND = '".SCHOOLKIND."' ";
            }
            $query .= "     LEFT JOIN NAME_MST N1 ON N1.NAMECD1 = 'Z002' ";
            $query .= " WHERE ";
            $query .= "     REGDH.YEAR = '".CTRL_YEAR."' ";
            $query .= "     AND REGDH.SEMESTER = '".CTRL_SEMESTER."' ";
            if ($model->field["GRADE"] != "ALL") {
                $query .= " AND REGDH.GRADE = '".$model->field["GRADE"]."' ";
            }
            $query .= " ), REGD_T AS ( ";
            $query .= " SELECT ";
            $query .= "     REGD.*, ";
            $query .= "     BASE.SEX ";
            $query .= " FROM ";
            $query .= "     SCHREG_REGD_DAT REGD ";
            $query .= "     LEFT JOIN SCHREG_BASE_MST BASE ON REGD.SCHREGNO = BASE.SCHREGNO ";
            $query .= "     , ";
            $query .= "     (SELECT ";
            $query .= "          T2.SCHREGNO, ";
            $query .= "          MAX(T2.SEMESTER) AS SEMESTER ";
            $query .= "      FROM ";
            $query .= "          SCHREG_REGD_DAT T2 ";
            $query .= "      WHERE ";
            $query .= "          T2.YEAR = '".CTRL_YEAR."' ";
            $query .= "      GROUP BY ";
            $query .= "          T2.SCHREGNO ";
            $query .= "     ) MAXSEME ";
            $query .= " WHERE ";
            $query .= "     REGD.YEAR = '".CTRL_YEAR."' ";
            $query .= "     AND REGD.SCHREGNO = MAXSEME.SCHREGNO ";
            $query .= "     AND REGD.SEMESTER = MAXSEME.SEMESTER ";
            if ($model->field["GRADE"] != "ALL") {
                $query .= " AND REGD.GRADE = '".$model->field["GRADE"]."' ";
            }
            $query .= " ) ";
            $query .= " SELECT ";
            $query .= "     REGD_H.GRADE, ";
            $query .= "     REGD_H.HR_CLASS, ";
            $query .= "     REGD_H.HR_NAME, ";
            $query .= "     REGD_H.SEX, ";
            if ($model->Properties["use_prg_schoolkind"] == "1" || $model->Properties["useSchool_KindField"] == "1") {
                $query .= "     CLUB.SCHOOLCD, ";
                $query .= "     REGD_H.SCHOOL_KIND, ";
            }
            $query .= "     CLUB.CLUBCD, ";
            $query .= "     SUM(CASE WHEN CLUB.CLUBCD IS NOT NULL THEN 1 ELSE 0 END) AS CNT ";
            $query .= " FROM ";
            $query .= "     REGD_H ";
            $query .= "     LEFT JOIN REGD_T ON REGD_H.GRADE = REGD_T.GRADE ";
            $query .= "          AND REGD_H.HR_CLASS = REGD_T.HR_CLASS ";
            $query .= "          AND REGD_H.SEX = REGD_T.SEX ";
            $query .= "     INNER JOIN SCHREG_CLUB_HIST_DAT CLUB ON '".$cnvBDate."' BETWEEN CLUB.SDATE AND VALUE(CLUB.EDATE, '9999-12-31') ";
            $query .= "          AND REGD_T.SCHREGNO = CLUB.SCHREGNO ";
            if ($model->Properties["useClubMultiSchoolKind"] == "1") {
                $query .= "   AND CLUB.SCHOOLCD = '".SCHOOLCD."' ";
                $query .= "   AND CLUB.SCHOOL_KIND = '".SCHOOLKIND."' ";
            } else if ($model->Properties["use_prg_schoolkind"] == "1") {
                if ($model->selectSchoolKind != "") {
                    $query .= "   AND CLUB.SCHOOLCD = '".SCHOOLCD."' ";
                    $query .= "   AND CLUB.SCHOOL_KIND IN ".$model->selectSchoolKindSql." ";
                }
            } else if ($model->Properties["useSchool_KindField"] = "1" && SCHOOLKIND != "" && SCHOOLCD != "") {
                $query .= "   AND CLUB.SCHOOLCD = '".SCHOOLCD."' ";
                $query .= "   AND CLUB.SCHOOL_KIND = '".SCHOOLKIND."' ";
            }
            $query .= " GROUP BY ";
            $query .= "     REGD_H.GRADE, ";
            $query .= "     REGD_H.HR_CLASS, ";
            $query .= "     REGD_H.HR_NAME, ";
            $query .= "     REGD_H.SEX, ";
            if ($model->Properties["use_prg_schoolkind"] == "1" || $model->Properties["useSchool_KindField"] == "1") {
                $query .= "     CLUB.SCHOOLCD, ";
                $query .= "     REGD_H.SCHOOL_KIND, ";
            }
            $query .= "     CLUB.CLUBCD ";
            $query .= " UNION ";
            $query .= " SELECT ";
            $query .= "     REGD_H.GRADE, ";
            $query .= "     '999' AS HR_CLASS, ";
            $query .= "     '学年' AS HR_NAME, ";
            $query .= "     REGD_H.SEX, ";
            if ($model->Properties["use_prg_schoolkind"] == "1" || $model->Properties["useSchool_KindField"] == "1") {
                $query .= "     CLUB.SCHOOLCD, ";
                $query .= "     REGD_H.SCHOOL_KIND, ";
            }
            $query .= "     CLUB.CLUBCD, ";
            $query .= "     SUM(CASE WHEN CLUB.CLUBCD IS NOT NULL THEN 1 ELSE 0 END) AS CNT ";
            $query .= " FROM ";
            $query .= "     REGD_H ";
            $query .= "     LEFT JOIN REGD_T ON REGD_H.GRADE = REGD_T.GRADE ";
            $query .= "          AND REGD_H.HR_CLASS = REGD_T.HR_CLASS ";
            $query .= "          AND REGD_H.SEX = REGD_T.SEX ";
            $query .= "     INNER JOIN SCHREG_CLUB_HIST_DAT CLUB ON '".$cnvBDate."' BETWEEN CLUB.SDATE AND VALUE(CLUB.EDATE, '9999-12-31') ";
            $query .= "          AND REGD_T.SCHREGNO = CLUB.SCHREGNO ";
            if ($model->Properties["useClubMultiSchoolKind"] == "1") {
                $query .= "   AND CLUB.SCHOOLCD = '".SCHOOLCD."' ";
                $query .= "   AND CLUB.SCHOOL_KIND = '".SCHOOLKIND."' ";
            } else if ($model->Properties["use_prg_schoolkind"] == "1") {
                if ($model->selectSchoolKind != "") {
                    $query .= "   AND CLUB.SCHOOLCD = '".SCHOOLCD."' ";
                    $query .= "   AND CLUB.SCHOOL_KIND IN ".$model->selectSchoolKindSql." ";
                }
            } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "" && SCHOOLCD != "") {
                $query .= "   AND CLUB.SCHOOLCD = '".SCHOOLCD."' ";
                $query .= "   AND CLUB.SCHOOL_KIND = '".SCHOOLKIND."' ";
            }
            $query .= " GROUP BY ";
            $query .= "     REGD_H.GRADE, ";
            $query .= "     REGD_H.SEX, ";
            if ($model->Properties["use_prg_schoolkind"] == "1" || $model->Properties["useSchool_KindField"] == "1") {
                $query .= "     CLUB.SCHOOLCD, ";
                $query .= "     REGD_H.SCHOOL_KIND, ";
            }
            $query .= "     CLUB.CLUBCD ";

            $query .= " UNION ";  //学年別合計
            $query .= " SELECT ";
            $query .= "     REGD_H.GRADE, ";
            $query .= "     '999' AS HR_CLASS, ";
            $query .= "     '学年' AS HR_NAME, ";
            $query .= "     '3' AS SEX, ";
            if ($model->Properties["use_prg_schoolkind"] == "1" || $model->Properties["useSchool_KindField"] == "1") {
                $query .= "     CLUB.SCHOOLCD, ";
                $query .= "     REGD_H.SCHOOL_KIND, ";
            }
            $query .= "     CLUB.CLUBCD, ";
            $query .= "     SUM(CASE WHEN CLUB.CLUBCD IS NOT NULL THEN 1 ELSE 0 END) AS CNT ";
            $query .= " FROM ";
            $query .= "     REGD_H ";
            $query .= "     LEFT JOIN REGD_T ON REGD_H.GRADE = REGD_T.GRADE ";
            $query .= "          AND REGD_H.HR_CLASS = REGD_T.HR_CLASS ";
            $query .= "          AND REGD_H.SEX = REGD_T.SEX ";
            $query .= "     INNER JOIN SCHREG_CLUB_HIST_DAT CLUB ON '".$cnvBDate."' BETWEEN CLUB.SDATE AND VALUE(CLUB.EDATE, '9999-12-31') ";
            $query .= "          AND REGD_T.SCHREGNO = CLUB.SCHREGNO ";
            if ($model->Properties["useClubMultiSchoolKind"] == "1") {
                $query .= "   AND CLUB.SCHOOLCD = '".SCHOOLCD."' ";
                $query .= "   AND CLUB.SCHOOL_KIND = '".SCHOOLKIND."' ";
            } else if ($model->Properties["use_prg_schoolkind"] == "1") {
                if ($model->selectSchoolKind != "") {
                    $query .= "   AND CLUB.SCHOOLCD = '".SCHOOLCD."' ";
                    $query .= "   AND CLUB.SCHOOL_KIND IN ".$model->selectSchoolKindSql." ";
                }
            } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "" && SCHOOLCD != "") {
                $query .= "   AND CLUB.SCHOOLCD = '".SCHOOLCD."' ";
                $query .= "   AND CLUB.SCHOOL_KIND = '".SCHOOLKIND."' ";
            }
            $query .= " GROUP BY ";
            $query .= "     REGD_H.GRADE, ";
            if ($model->Properties["use_prg_schoolkind"] == "1" || $model->Properties["useSchool_KindField"] == "1") {
                $query .= "     CLUB.SCHOOLCD, ";
                $query .= "     REGD_H.SCHOOL_KIND, ";
            }
            $query .= "     CLUB.CLUBCD ";
            $query .= " UNION ";  //(校種別)全学年男女別合計
            $query .= " SELECT ";
            $query .= "     '99' AS GRADE, ";
            $query .= "     '999' AS HR_CLASS, ";
            $query .= "     '全学年' AS HR_NAME, ";
            $query .= "     REGD_H.SEX, ";
            if ($model->Properties["use_prg_schoolkind"] == "1" || $model->Properties["useSchool_KindField"] == "1") {
                $query .= "     CLUB.SCHOOLCD, ";
                $query .= "     REGD_H.SCHOOL_KIND, ";
            }
            $query .= "     CLUB.CLUBCD, ";
            $query .= "     SUM(CASE WHEN CLUB.CLUBCD IS NOT NULL THEN 1 ELSE 0 END) AS CNT ";
            $query .= " FROM ";
            $query .= "     REGD_H ";
            $query .= "     LEFT JOIN REGD_T ON REGD_H.GRADE = REGD_T.GRADE ";
            $query .= "          AND REGD_H.HR_CLASS = REGD_T.HR_CLASS ";
            $query .= "          AND REGD_H.SEX = REGD_T.SEX ";
            $query .= "     INNER JOIN SCHREG_CLUB_HIST_DAT CLUB ON '".$cnvBDate."' BETWEEN CLUB.SDATE AND VALUE(CLUB.EDATE, '9999-12-31') ";
            $query .= "          AND REGD_T.SCHREGNO = CLUB.SCHREGNO ";
            if ($model->Properties["useClubMultiSchoolKind"] == "1") {
                $query .= "   AND CLUB.SCHOOLCD = '".SCHOOLCD."' ";
                $query .= "   AND CLUB.SCHOOL_KIND = '".SCHOOLKIND."' ";
            } else if ($model->Properties["use_prg_schoolkind"] == "1") {
                if ($model->selectSchoolKind != "") {
                    $query .= "   AND CLUB.SCHOOLCD = '".SCHOOLCD."' ";
                    $query .= "   AND CLUB.SCHOOL_KIND IN ".$model->selectSchoolKindSql." ";
                }
            } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "" && SCHOOLCD != "") {
                $query .= "   AND CLUB.SCHOOLCD = '".SCHOOLCD."' ";
                $query .= "   AND CLUB.SCHOOL_KIND = '".SCHOOLKIND."' ";
            }
            $query .= " GROUP BY ";
            $query .= "     REGD_H.SEX, ";
            if ($model->Properties["use_prg_schoolkind"] == "1" || $model->Properties["useSchool_KindField"] == "1") {
                $query .= "     CLUB.SCHOOLCD, ";
                $query .= "     REGD_H.SCHOOL_KIND, ";
            }
            $query .= "     CLUB.CLUBCD ";
            $query .= " UNION ";  //(校種別)全学年合計
            $query .= " SELECT ";
            $query .= "     '99' AS GRADE, ";
            $query .= "     '999' AS HR_CLASS, ";
            $query .= "     '全学年' AS HR_NAME, ";
            $query .= "     '3' AS SEX, ";
            if ($model->Properties["use_prg_schoolkind"] == "1" || $model->Properties["useSchool_KindField"] == "1") {
                $query .= "     CLUB.SCHOOLCD, ";
                $query .= "     REGD_H.SCHOOL_KIND, ";
            }
            $query .= "     CLUB.CLUBCD, ";
            $query .= "     SUM(CASE WHEN CLUB.CLUBCD IS NOT NULL THEN 1 ELSE 0 END) AS CNT ";
            $query .= " FROM ";
            $query .= "     REGD_H ";
            $query .= "     LEFT JOIN REGD_T ON REGD_H.GRADE = REGD_T.GRADE ";
            $query .= "          AND REGD_H.HR_CLASS = REGD_T.HR_CLASS ";
            $query .= "          AND REGD_H.SEX = REGD_T.SEX ";
            $query .= "     INNER JOIN SCHREG_CLUB_HIST_DAT CLUB ON '".$cnvBDate."' BETWEEN CLUB.SDATE AND VALUE(CLUB.EDATE, '9999-12-31') ";
            $query .= "          AND REGD_T.SCHREGNO = CLUB.SCHREGNO ";
            if ($model->Properties["useClubMultiSchoolKind"] == "1") {
                $query .= "   AND CLUB.SCHOOLCD = '".SCHOOLCD."' ";
                $query .= "   AND CLUB.SCHOOL_KIND = '".SCHOOLKIND."' ";
            } else if ($model->Properties["use_prg_schoolkind"] == "1") {
                if ($model->selectSchoolKind != "") {
                    $query .= "   AND CLUB.SCHOOLCD = '".SCHOOLCD."' ";
                    $query .= "   AND CLUB.SCHOOL_KIND IN ".$model->selectSchoolKindSql." ";
                }
            } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "" && SCHOOLCD != "") {
                $query .= "   AND CLUB.SCHOOLCD = '".SCHOOLCD."' ";
                $query .= "   AND CLUB.SCHOOL_KIND = '".SCHOOLKIND."' ";
            }
            $query .= " GROUP BY ";
            if ($model->Properties["use_prg_schoolkind"] == "1" || $model->Properties["useSchool_KindField"] == "1") {
                $query .= "     CLUB.SCHOOLCD, ";
                $query .= "     REGD_H.SCHOOL_KIND, ";
            }
            $query .= "     CLUB.CLUBCD ";

            $query .= " ORDER BY ";
            $query .= "     GRADE, ";
            $query .= "     HR_CLASS, ";
            $query .= "     SEX, ";
            if ($model->Properties["use_prg_schoolkind"] == "1" || $model->Properties["useSchool_KindField"] == "1") {
                $query .= "     SCHOOLCD, ";
                $query .= "     SCHOOL_KIND, ";
            }
            $query .= "     CLUBCD ";

            return $query;
        }

}
?>
