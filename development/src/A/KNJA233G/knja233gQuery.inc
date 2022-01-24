<?php

require_once('for_php7.php');

class knja233gQuery extends Query
{

    //学期日付取得
    public function getSemester($year, $semester)
    {
        $query  = "SELECT ";
        $query .= "    SDATE, ";
        $query .= "    EDATE ";
        $query .= "FROM ";
        $query .= "    SEMESTER_MST ";
        $query .= "WHERE ";
        $query .= "    YEAR = '{$year}' AND ";
        $query .= "    SEMESTER = '{$semester}' ";

        return $query;
    }

    //名称マスタB023取得
    public function getNameMstB023()
    {
        $query  = " SELECT ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND NAMECD1 = 'B023' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //学年取得
    public function getGrade($model, $semester, $grade = "")
    {
        $query  = " SELECT DISTINCT ";
        if (!$grade) {
            $query .= "     GDAT.GRADE AS VALUE, ";
            $query .= "     GDAT.GRADE_NAME1 AS LABEL, ";
        }
        $query .= "     GDAT.SCHOOL_KIND ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT REGDH ";
        $query .= "     INNER JOIN SCHREG_REGD_GDAT GDAT ON REGDH.YEAR = GDAT.YEAR ";
        $query .= "           AND REGDH.GRADE = GDAT.GRADE ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "           AND GDAT.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind), "','")."') ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "           AND GDAT.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " WHERE ";
        $query .= "     REGDH.YEAR        = '".CTRL_YEAR."' AND ";
        $query .= "     REGDH.SEMESTER    = '".$semester."' ";
        //参照・更新可（制限付き）
        if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT) {
            $query .= " AND '".STAFFCD."' IN (REGDH.TR_CD1, REGDH.TR_CD2, REGDH.TR_CD3, REGDH.SUBTR_CD1, REGDH.SUBTR_CD2, REGDH.SUBTR_CD3) ";
        }
        if ($grade) {
            $query .= "     AND REGDH.GRADE = '{$grade}' ";
        }
        if (!$grade) {
            $query .= " ORDER BY ";
            $query .= "     VALUE ";
        }
        return $query;
    }

    //年組取得（権限チェック）
    public function getGradeHrClass($model, $semester, $gradeHr = "")
    {
        $query  = " SELECT ";
        if (!$gradeHr || ($model->field["DISP"] == "1" && $gradeHr)) {
            $query .= "     REGDH.GRADE || REGDH.HR_CLASS AS VALUE, ";
            $query .= "     REGDH.HR_NAME AS LABEL, ";
        }
        $query .= "     GDAT.SCHOOL_KIND ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT REGDH ";
        $query .= "     INNER JOIN SCHREG_REGD_GDAT GDAT ON REGDH.YEAR = GDAT.YEAR ";
        $query .= "           AND REGDH.GRADE = GDAT.GRADE ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "           AND GDAT.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind), "','")."') ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "           AND GDAT.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " WHERE ";
        $query .= "     REGDH.YEAR        = '".CTRL_YEAR."' AND ";
        $query .= "     REGDH.SEMESTER    = '".$semester."' ";
        //参照・更新可（制限付き）
        if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT) {
            $query .= " AND '".STAFFCD."' IN (REGDH.TR_CD1, REGDH.TR_CD2, REGDH.TR_CD3, REGDH.SUBTR_CD1, REGDH.SUBTR_CD2, REGDH.SUBTR_CD3) ";
        }
        if ($gradeHr) {
            if ($model->field["DISP"] == "1") {
                $query .= "     AND REGDH.GRADE = '{$gradeHr}' ";
            } else {
                $query .= "     AND REGDH.GRADE || REGDH.HR_CLASS = '{$gradeHr}' ";
            }
        }
        if (!$gradeHr || ($model->field["DISP"] == "1" && $gradeHr)) {
            $query .= " ORDER BY ";
            $query .= "     VALUE ";
        }
        return $query;
    }

    //対象外の生徒取得
    public function getSchnoIdou($model, $semester)
    {
        $date = str_replace("/", "-", $model->field["DATE"]);

        $query  = " SELECT ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T1.ATTENDNO ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1, ";
        $query .= "     SEMESTER_MST T2 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR     = '".CTRL_YEAR."' AND ";
        $query .= "     T1.SEMESTER = '".$semester."' AND ";
        $query .= "     T1.YEAR     = T2.YEAR AND ";
        $query .= "     T1.SEMESTER = T2.SEMESTER AND ";
        $query .= "     T1.GRADE || T1.HR_CLASS = '".$model->field["GRADE_HR_CLASS"]."' AND ( ";
                        //転学(2)・退学(3)者 ・除籍(6)・転籍(7) 但し異動日が学期終了日または異動基準日より小さい場合
                        //転入(4)・編入(5)者 但し異動日が学期終了日または異動基準日より大きい場合
        $query .= "     EXISTS( SELECT 'X' FROM SCHREG_BASE_MST S1 ";
        $query .= "             WHERE S1.SCHREGNO = T1.SCHREGNO AND ";
        $query .= "                 ((S1.GRD_DIV IN('2','3','6','7') AND S1.GRD_DATE <  ";
        $query .= "                         CASE WHEN T2.EDATE < '".$date."' ";
        $query .= "                              THEN T2.EDATE ELSE '".$date."' END) OR ";
        $query .= "                  (S1.ENT_DIV IN('4','5') AND S1.ENT_DATE >  ";
        $query .= "                         CASE WHEN T2.EDATE < '".$date."' ";
        $query .= "                              THEN T2.EDATE ELSE '".$date."' END)) ) OR ";
                        //留学(1)・休学(2)者
        $query .= "     EXISTS( SELECT 'X' FROM SCHREG_TRANSFER_DAT S1 ";
        $query .= "             WHERE S1.SCHREGNO = T1.SCHREGNO AND ";
        $query .= "                   S1.TRANSFERCD IN ('1','2') AND  ";
        $query .= "                   CASE WHEN T2.EDATE < '".$date."' ";
        $query .= "                        THEN T2.EDATE ELSE '".$date."' END ";
        $query .= "                        BETWEEN S1.TRANSFER_SDATE AND S1.TRANSFER_EDATE)) ";
        $query .= " ORDER BY ";
        $query .= "     T1.ATTENDNO, ";
        $query .= "     T1.SCHREGNO ";

        return $query;
    }

    //対象生徒
    public function getStudent($model, $semester)
    {
        $query  = " SELECT ";
        $query .= "     T2.SCHREGNO, ";
        $query .= "     T2.ATTENDNO, ";
        $query .= "     T1.NAME_SHOW ";
        $query .= " FROM ";
        $query .= "     SCHREG_BASE_MST T1, ";
        $query .= "     SCHREG_REGD_DAT T2 ";
        $query .= " WHERE ";
        $query .= "     T2.YEAR     = '".CTRL_YEAR."' AND ";
        $query .= "     T2.SEMESTER = '".$semester."' AND ";
        $query .= "     T2.GRADE || T2.HR_CLASS = '".$model->field["GRADE_HR_CLASS"]."' AND ";
        $query .= "     T2.SCHREGNO = T1.SCHREGNO ";
        $query .= " ORDER BY ";
        $query .= "     T2.ATTENDNO, ";
        $query .= "     T2.SCHREGNO ";

        return $query;
    }

    //CSVデータ
    public function getCsvData($model, $where, $flg)
    {
        $date = str_replace("/", "-", $model->field["DATE"]);

        //出力対象の生徒
        $query .= " WITH SCHREGNO AS ( ";
        $query .= " SELECT ";
        $query .= "   REGD.SCHREGNO ";
        $query .= " FROM ";
        $query .= "   SCHREG_REGD_DAT REGD ";
        $query .= " WHERE ";
        $query .= "   REGD.YEAR = '".CTRL_YEAR."' ";
        $query .= "   AND REGD.SEMESTER = '".CTRL_SEMESTER."' ";
        if ($model->field["DISP"] == "1") {
            $query .= "   AND REGD.GRADE || REGD.HR_CLASS IN ".$where;
        } else {
            $query .= "   AND REGD.SCHREGNO IN ".$where;
        }
        $query .= " ORDER BY ";
        $query .= "   REGD.HR_CLASS, REGD.ATTENDNO ";
        //1年前の最終学期
        $query .= " ), ZENNEN_LASTSEME AS ( ";
        $query .= " SELECT ";
        $query .= "   REGD.YEAR, ";
        $query .= "   REGD.SCHREGNO, ";
        $query .= "   MAX(REGD.SEMESTER) AS LASTSEME ";
        $query .= " FROM ";
        $query .= "   SCHREG_REGD_DAT REGD ";
        $query .= " INNER JOIN ";
        $query .= "   SCHREGNO T1  ";
        $query .= "   ON REGD.SCHREGNO = T1.SCHREGNO ";
        $query .= " WHERE ";
        $query .= "   REGD.YEAR = '".(CTRL_YEAR - 1)."' ";
        $query .= " GROUP BY ";
        $query .= "   REGD.YEAR, ";
        $query .= "   REGD.SCHREGNO ";
        //1年前の最終学期データ
        $query .= " ), ZENNEN_DATA AS ( ";
        $query .= " SELECT ";
        $query .= "   REGD.YEAR, ";
        $query .= "   REGD.SCHREGNO, ";
        $query .= "   REGD.SEMESTER AS ZENNEN_SEMESTER, ";
        $query .= "   REGD.ATTENDNO AS ZENNEN_ATTENDNO,   ";
        $query .= "   REGD_H.HR_NAME AS ZENNEN_HR_NAME ";
        $query .= " FROM ";
        $query .= "   ZENNEN_LASTSEME T1 ";
        $query .= " INNER JOIN ";
        $query .= "   SCHREG_REGD_DAT REGD ";
        $query .= "    ON REGD.YEAR = T1.YEAR ";
        $query .= "   AND REGD.SCHREGNO = T1.SCHREGNO ";
        $query .= "   AND REGD.SEMESTER = T1.LASTSEME  ";
        $query .= " INNER JOIN ";
        $query .= "   SCHREG_REGD_HDAT REGD_H ";
        $query .= "    ON REGD.YEAR = REGD_H.YEAR ";
        $query .= "   AND REGD.SEMESTER = REGD_H.SEMESTER ";
        $query .= "   AND REGD.GRADE = REGD_H.GRADE ";
        $query .= "   AND REGD.HR_CLASS = REGD_H.HR_CLASS ";
        //出力対象の生徒データ
        $query .= " ), SCHREG_DATA AS ( ";
        $query .= " SELECT ";
        $query .= "   REGD.SCHREGNO, ";
        $query .= "   REGD.YEAR, ";
        $query .= "   REGD.SEMESTER, ";
        $query .= "   REGD.GRADE, ";
        $query .= "   REGD.HR_CLASS, ";
        $query .= "   BASE.NAME, ";
        $query .= "   BASE.NAME_KANA, ";
        $query .= "   CASE WHEN BASE.SEX = '1' THEN '○' END AS SEX, ";
        $query .= "   REGD.ATTENDNO, ";
        $query .= "   REGD.COURSECD, ";
        $query .= "   REGD.MAJORCD, ";
        $query .= "   REGD.COURSECODE, ";
        $query .= "   REGD_H.HR_NAME, ";
        $query .= "   BASE.GRD_DIV, ";
        $query .= "   N3.NAME1 AS A053, ";
        $query .= "   CASE WHEN N1.NAME1 IS NOT NULL AND N2.NAME1 IS NOT NULL THEN N1.NAME1 WHEN N1.NAME1 IS NOT NULL THEN N1.NAME1 WHEN N2.NAME1 IS NOT NULL THEN N2.NAME1 END KUBUN, ";
        $query .= "   BASE003.BASE_REMARK1 AS EXAMNO, ";
        $query .= "   T2.ZENNEN_HR_NAME, ";
        $query .= "   T2.ZENNEN_ATTENDNO ";
        $query .= " FROM ";
        $query .= "   SCHREG_REGD_DAT REGD ";
        $query .= " INNER JOIN ";
        $query .= "   SCHREG_REGD_HDAT REGD_H ";
        $query .= "    ON REGD_H.YEAR = REGD.YEAR ";
        $query .= "   AND REGD_H.SEMESTER = REGD.SEMESTER ";
        $query .= "   AND REGD_H.GRADE = REGD.GRADE ";
        $query .= "   AND REGD_H.HR_CLASS = REGD.HR_CLASS ";
        $query .= " INNER JOIN ";
        $query .= "   SCHREG_BASE_MST BASE ";
        $query .= "    ON BASE.SCHREGNO = REGD.SCHREGNO ";
        $query .= " LEFT JOIN ";
        $query .= "   SCHREG_BASE_DETAIL_MST BASE003 ";
        $query .= "    ON BASE003.SCHREGNO = REGD.SCHREGNO ";
        $query .= "   AND BASE003.BASE_SEQ = '003' ";
        $query .= " LEFT JOIN ";
        $query .= "   SCHREG_BASE_DETAIL_MST BASE016 ";
        $query .= "    ON BASE016.SCHREGNO = REGD.SCHREGNO ";
        $query .= "   AND BASE016.BASE_SEQ = '016' ";
        $query .= " LEFT JOIN ";
        $query .= "   SCHREG_TRANSFER_DAT TRANS ";
        $query .= "    ON TRANS.SCHREGNO = REGD.SCHREGNO ";
        $query .= "   AND '".$date."' BETWEEN TRANS.TRANSFER_SDATE AND TRANS.TRANSFER_EDATE ";
        $query .= " LEFT JOIN ";
        $query .= "   V_NAME_MST N1 ";
        $query .= "    ON N1.YEAR = REGD.YEAR ";
        $query .= "   AND N1.NAMECD1 = 'A003' ";
        $query .= "   AND N1.NAMECD2 = BASE.GRD_DIV ";
        $query .= " LEFT JOIN ";
        $query .= "   V_NAME_MST N2 ";
        $query .= "    ON N2.YEAR = REGD.YEAR ";
        $query .= "   AND N2.NAMECD1 = 'A004' ";
        $query .= "   AND N2.NAMECD2 = TRANS.TRANSFERCD ";
        $query .= " LEFT JOIN ";
        $query .= "   V_NAME_MST N3 ";
        $query .= "    ON N3.YEAR = REGD.YEAR ";
        $query .= "   AND N3.NAMECD1 = 'A053' ";
        $query .= "   AND N3.NAMECD2 = BASE016.BASE_REMARK1 ";
        $query .= " LEFT JOIN ";
        $query .= "   ZENNEN_DATA T2";
        $query .= "    ON T2.SCHREGNO = REGD.SCHREGNO ";
        $query .= " WHERE ";
        $query .= "   REGD.YEAR = '".CTRL_YEAR."' ";
        $query .= "   AND REGD.SEMESTER = '".CTRL_SEMESTER."' ";
        if ($model->field["DISP"] == "1") {
            $query .= "   AND REGD.GRADE || REGD.HR_CLASS IN ".$where;
        } else {
            $query .= "   AND REGD.SCHREGNO IN ".$where;
        }
        $query .= " ORDER BY ";
        $query .= "   REGD.HR_CLASS, REGD.ATTENDNO ";
        //講座データ
        $query .= " ), CHAIR_DATA AS ( ";
        $query .= " SELECT ";
        $query .= "   CHAIR_S.CHAIRCD, ";
        $query .= "   CHAIR_S.SCHREGNO, ";
        $query .= "   SUBCLASS.SUBCLASSABBV, ";
        $query .= "   SUBCLASS.ELECTDIV, ";
        $query .= "   CHAIR_D.REMARK1 AS RETSU_NAME, ";
        $query .= "   CHAIR_D.REMARK2 AS S_CLASS, ";
        $query .= "   CHAIR_D.REMARK3 AS SYUJUKUDO ";
        $query .= " FROM ";
        $query .= "   CHAIR_STD_DAT CHAIR_S ";
        $query .= " INNER JOIN ";
        $query .= "   CHAIR_DETAIL_DAT CHAIR_D ";
        $query .= "    ON CHAIR_D.YEAR = CHAIR_S.YEAR ";
        $query .= "   AND CHAIR_D.SEMESTER = CHAIR_S.SEMESTER ";
        $query .= "   AND CHAIR_D.CHAIRCD = CHAIR_S.CHAIRCD ";
        $query .= "   AND CHAIR_D.SEQ = '004' ";
        $query .= "   AND CHAIR_D.REMARK1 IS NOT NULL ";
        $query .= " INNER JOIN ";
        $query .= "   CHAIR_DAT CHAIR ";
        $query .= "    ON CHAIR.YEAR = CHAIR_S.YEAR ";
        $query .= "   AND CHAIR.SEMESTER = CHAIR_S.SEMESTER ";
        $query .= "   AND CHAIR.CHAIRCD = CHAIR_S.CHAIRCD ";
        $query .= " INNER JOIN ";
        $query .= "   SCHREG_DATA T1 ";
        $query .= "    ON T1.YEAR = CHAIR_S.YEAR ";
        $query .= "   AND T1.SEMESTER = CHAIR_S.SEMESTER ";
        $query .= "   AND T1.SCHREGNO = CHAIR_S.SCHREGNO ";
        $query .= " INNER JOIN ";
        $query .= "   SUBCLASS_MST SUBCLASS ";
        $query .= "    ON SUBCLASS.CLASSCD = CHAIR.CLASSCD ";
        $query .= "   AND SUBCLASS.SCHOOL_KIND = CHAIR.SCHOOL_KIND ";
        $query .= "   AND SUBCLASS.CURRICULUM_CD = CHAIR.CURRICULUM_CD ";
        $query .= "   AND SUBCLASS.SUBCLASSCD = CHAIR.SUBCLASSCD ";
        $query .= " WHERE ";
        $query .= "   CHAIR_S.YEAR = '".CTRL_YEAR."' ";
        $query .= "   AND '".$date."' BETWEEN CHAIR_S.APPDATE AND CHAIR_S.APPENDDATE ";
        //列名称で絞り込み
        if ($model->field["B023"] != "ALL") {
            $query .= "   AND CHAIR_D.REMARK1 = '".$model->field["B023"]."' ";
        }
        //習熟度チェックがONの場合条件に含める
        if ($flg) {
            $query .= "   AND (CHAIR_D.REMARK3 IS NOT NULL OR SUBCLASS.ELECTDIV = '1') ";
        } else {
            $query .= "   AND SUBCLASS.ELECTDIV = '1' ";
        }
        $query .= " ORDER BY ";
        $query .= "   T1.HR_CLASS, T1.ATTENDNO, CHAIR_S.CHAIRCD ";
        $query .= " ) ";
        //メイン表
        $query .= " SELECT ";
        $query .= "   T1.SCHREGNO, ";
        $query .= "   T1.HR_CLASS, ";
        $query .= "   T1.NAME, ";
        $query .= "   T1.NAME_KANA, ";
        $query .= "   T1.SEX, ";
        $query .= "   T1.ATTENDNO, ";
        $query .= "   T1.HR_NAME, ";
        $query .= "   T1.KUBUN, ";
        $query .= "   T1.A053, ";
        $query .= "   T1.EXAMNO, ";
        $query .= "   T1.ZENNEN_HR_NAME, ";
        $query .= "   T1.ZENNEN_ATTENDNO, ";
        $query .= "   T2.CHAIRCD, ";
        $query .= "   T2.SUBCLASSABBV, ";
        $query .= "   T2.ELECTDIV, ";
        $query .= "   T2.RETSU_NAME, ";
        $query .= "   T2.S_CLASS, ";
        $query .= "   T2.SYUJUKUDO ";
        $query .= " FROM SCHREG_DATA T1 ";
        $query .= " LEFT JOIN ";
        $query .= "   CHAIR_DATA T2 ";
        $query .= "  ON T2.SCHREGNO = T1.SCHREGNO ";
        $query .= " ORDER BY ";
        $query .= "   T1.HR_CLASS, T1.ATTENDNO, T2.RETSU_NAME, T2.CHAIRCD ";

        return $query;
    }
}
