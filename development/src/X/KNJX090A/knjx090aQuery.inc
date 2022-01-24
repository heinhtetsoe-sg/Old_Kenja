<?php

require_once('for_php7.php');

class knjx090aquery extends Query
{
    public function getSecurityHigh()
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     MENU_HIGH_SECURITY_MST ";
        $query .= " WHERE ";
        $query .= "     PROGRAMID = 'KNJX090A' ";
        $query .= "     AND INVALID_FLG = '0' ";

        return $query;
    }

    //学年一覧
    public function getSelectFieldSQL($model)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     GRADE AS VALUE, ";
        $query .= "     GRADE_NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " AND SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind), "','")."') ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
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
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " AND W1.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind), "','")."') ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND W1.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= "        AND W1.GROUPCD = '0000' ";
        $query .= "        AND NOT EXISTS (SELECT 'X' ";
        $query .= "                          FROM CHAIR_STD_DAT W2 ";
        $query .= "                         WHERE W2.YEAR     = W1.YEAR ";
        $query .= "                           AND W2.SEMESTER = W1.SEMESTER ";
        $query .= "                           AND W2.CHAIRCD  = W1.CHAIRCD ) ";
        $query .= "        AND W1.CHAIRCD IN (SELECT W1.CHAIRCD ";
        $query .= "                           FROM CHAIR_CLS_DAT W1 ";
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
        $query .= "        SCHREG_REGD_DAT W3 ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "      INNER JOIN SCHREG_REGD_GDAT REG_G ON W3.YEAR = REG_G.YEAR ";
                $query .= "           AND W3.GRADE = REG_G.GRADE ";
                $query .= "           AND REG_G.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind), "','")."') ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "      INNER JOIN SCHREG_REGD_GDAT REG_G ON W3.YEAR = REG_G.YEAR ";
            $query .= "           AND W3.GRADE = REG_G.GRADE ";
            $query .= "           AND REG_G.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= "      , SEMESTER_MST    W4, ";
        $query .= "        SCHREG_BASE_MST W5 ";
        if ($model->Properties["use_prg_schoolkind"] == "1" || ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "")) {
            $query .= "  , SCHREG_REGD_GDAT GDAT ";
        }
        $query .= "  WHERE W1.YEAR = '{$year}' ";
        $query .= "    AND W1.SEMESTER = '{$semester}' ";
        $query .= "    AND W1.CHAIRCD = W2.CHAIRCD ";
        $query .= "    AND W1.GROUPCD = '0000' ";
        $query .= "    AND W3.YEAR = W1.YEAR ";
        $query .= "    AND W3.SEMESTER = W1.SEMESTER ";
        if (strlen($model->field["GRADE"]) > 0) {
            $query .= "    AND W3.GRADE = '{$model->field["GRADE"]}' ";
        }
        $query .= "    AND W3.GRADE = W1.TRGTGRADE ";
        $query .= "    AND W3.HR_CLASS = W1.TRGTCLASS ";
        $query .= "    AND W4.YEAR = W1.YEAR ";
        $query .= "    AND W4.SEMESTER = W1.SEMESTER ";
        $query .= "    AND W5.SCHREGNO = W3.SCHREGNO ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            $query .= " AND GDAT.YEAR = W3.YEAR AND GDAT.GRADE = W3.GRADE ";
            if ($model->selectSchoolKind) {
                $query .= " AND GDAT.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind), "','")."') ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND GDAT.YEAR = W3.YEAR AND GDAT.GRADE = W3.GRADE AND GDAT.SCHOOL_KIND = '".SCHOOLKIND."'";
        }
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
