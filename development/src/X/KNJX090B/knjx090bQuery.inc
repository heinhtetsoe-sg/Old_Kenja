<?php

require_once('for_php7.php');
class knjx090bquery extends Query
{
    public function getSecurityHigh()
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     MENU_HIGH_SECURITY_MST ";
        $query .= " WHERE ";
        $query .= "     PROGRAMID = 'KNJX090B' ";
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

    //年度一覧
    public function getSelectFieldSQL($model)
    {
        $query  = "select distinct t1.year,t1.semester,t2.semestername ";
        $query .= "from chair_std_dat t1 ";
        $query .= "        inner join semester_mst t2 on t1.year=t2.year and t1.semester=t2.semester ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " INNER JOIN SCHREG_REGD_DAT REGD ON REGD.YEAR = T1.YEAR AND REGD.SEMESTER = T1.SEMESTER AND REGD.SCHREGNO = T1.SCHREGNO ";
                $query .= " INNER JOIN SCHREG_REGD_GDAT GDAT ON GDAT.YEAR = REGD.YEAR AND GDAT.GRADE = REGD.GRADE ";
                $query .= " AND GDAT.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind), "','")."') ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " INNER JOIN SCHREG_REGD_DAT REGD ON REGD.YEAR = T1.YEAR AND REGD.SEMESTER = T1.SEMESTER AND REGD.SCHREGNO = T1.SCHREGNO ";
            $query .= " INNER JOIN SCHREG_REGD_GDAT GDAT ON GDAT.YEAR = REGD.YEAR AND GDAT.GRADE = REGD.GRADE ";
            $query .= " AND GDAT.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= "order by t1.year,t1.semester";

        return $query;
    }

    //講座一覧
    public function getSelectFieldSQL2($model)
    {
        $query  = "SELECT distinct t1.chaircd, t2.chairname ";
        $query .= "FROM   chair_std_dat t1  ";
        $query .= "left join (SELECT DISTINCT chaircd, chairname FROM chair_dat ";
        $query .= "           WHERE YEAR || SEMESTER = '".$model->field["YEAR"]."') t2 on t1.chaircd = t2.chaircd ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " INNER JOIN CHAIR_DAT CHD ON CHD.YEAR = t1.YEAR AND CHD.SEMESTER = T1.SEMESTER AND CHD.CHAIRCD = T1.CHAIRCD ";
                $query .= " AND CHD.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind), "','")."') ";
                $query .= " INNER JOIN SCHREG_REGD_DAT REGD ON REGD.YEAR = CHD.YEAR AND REGD.SEMESTER = CHD.SEMESTER AND REGD.SCHREGNO = t1.SCHREGNO ";
                $query .= " INNER JOIN SCHREG_REGD_GDAT GDAT ON GDAT.YEAR = REGD.YEAR AND GDAT.GRADE = REGD.GRADE ";
                $query .= " AND GDAT.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind), "','")."') ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " INNER JOIN CHAIR_DAT CHD ON CHD.YEAR = t1.YEAR AND CHD.SEMESTER = T1.SEMESTER AND CHD.CHAIRCD = T1.CHAIRCD AND CHD.SCHOOL_KIND = '".SCHOOLKIND."' ";
            $query .= " INNER JOIN SCHREG_REGD_DAT REGD ON REGD.YEAR = CHD.YEAR AND REGD.SEMESTER = CHD.SEMESTER AND REGD.SCHREGNO = t1.SCHREGNO ";
            $query .= " INNER JOIN SCHREG_REGD_GDAT GDAT ON GDAT.YEAR = REGD.YEAR AND GDAT.GRADE = REGD.GRADE ";
            $query .= " AND GDAT.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= "        WHERE  t1.YEAR || t1.SEMESTER = '".$model->field["YEAR"]."' ";
        $query .= "ORDER BY t1.chaircd ";

        return $query;
    }

    //学籍番号一覧
    public function getSelectFieldSQL3($model)
    {
        $query  = "select distinct t1.schregno,t2.name ";
        $query .= "from chair_std_dat t1 inner join schreg_base_mst t2 on t2.schregno = t1.schregno ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "      INNER JOIN CHAIR_DAT CHAIR ON t1.YEAR = CHAIR.YEAR ";
                $query .= "           AND t1.SEMESTER = CHAIR.SEMESTER ";
                $query .= "           AND t1.CHAIRCD = CHAIR.CHAIRCD ";
                $query .= "           AND CHAIR.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind), "','")."') ";
                $query .= " INNER JOIN SCHREG_REGD_DAT REGD ON REGD.YEAR = T1.YEAR AND REGD.SEMESTER = T1.SEMESTER AND REGD.SCHREGNO = T1.SCHREGNO ";
                $query .= " INNER JOIN SCHREG_REGD_GDAT GDAT ON GDAT.YEAR = REGD.YEAR AND GDAT.GRADE = REGD.GRADE ";
                $query .= " AND GDAT.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind), "','")."') ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= "      INNER JOIN CHAIR_DAT CHAIR ON t1.YEAR = CHAIR.YEAR ";
            $query .= "           AND t1.SEMESTER = CHAIR.SEMESTER ";
            $query .= "           AND t1.CHAIRCD = CHAIR.CHAIRCD ";
            $query .= "           AND CHAIR.SCHOOL_KIND = '".SCHOOLKIND."' ";
            $query .= " INNER JOIN SCHREG_REGD_DAT REGD ON REGD.YEAR = T1.YEAR AND REGD.SEMESTER = T1.SEMESTER AND REGD.SCHREGNO = T1.SCHREGNO ";
            $query .= " INNER JOIN SCHREG_REGD_GDAT GDAT ON GDAT.YEAR = REGD.YEAR AND GDAT.GRADE = REGD.GRADE ";
            $query .= " AND GDAT.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= "where t1.YEAR || t1.SEMESTER = '".$model->field["YEAR"]."' ";
        $query .= "order by t1.schregno";

        return $query;
    }

    //適用開始日付・終了日付一覧 04/11/25Add
    public function getSelectFieldSQL4($model)
    {
        $query  = "select distinct T1.appdate,T1.appenddate   ";
        $query .= "from chair_std_dat T1  ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "      INNER JOIN CHAIR_DAT CHAIR ON T1.YEAR = CHAIR.YEAR ";
                $query .= "           AND T1.SEMESTER = CHAIR.SEMESTER ";
                $query .= "           AND T1.CHAIRCD = CHAIR.CHAIRCD ";
                $query .= "           AND CHAIR.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind), "','")."') ";
                $query .= " INNER JOIN SCHREG_REGD_DAT REGD ON REGD.YEAR = t1.year AND REGD.SEMESTER = T1.SEMESTER AND REGD.SCHREGNO = T1.SCHREGNO ";
                $query .= " INNER JOIN SCHREG_REGD_GDAT GDAT ON GDAT.YEAR = REGD.YEAR AND GDAT.GRADE = REGD.GRADE ";
                $query .= " AND GDAT.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind), "','")."') ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "      INNER JOIN CHAIR_DAT CHAIR ON T1.YEAR = CHAIR.YEAR ";
            $query .= "           AND T1.SEMESTER = CHAIR.SEMESTER ";
            $query .= "           AND T1.CHAIRCD = CHAIR.CHAIRCD ";
            $query .= "           AND CHAIR.SCHOOL_KIND = '".SCHOOLKIND."' ";
            $query .= " INNER JOIN SCHREG_REGD_DAT REGD ON REGD.YEAR = t1.year AND REGD.SEMESTER = T1.SEMESTER AND REGD.SCHREGNO = T1.SCHREGNO ";
            $query .= " INNER JOIN SCHREG_REGD_GDAT GDAT ON GDAT.YEAR = REGD.YEAR AND GDAT.GRADE = REGD.GRADE ";
            $query .= " AND GDAT.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= "where T1.YEAR || T1.SEMESTER = '".$model->field["YEAR"]."'  ";
        $query .= "order by T1.appdate,T1.appenddate ";

        return $query;
    }

    //データ出力
    public function getselectDataSQL($model)
    {
        $query  = " SELECT ";
        $query .= "     T1.YEAR, ";
        $query .= "     T1.SEMESTER, ";
        $query .= "     T1.CHAIRCD, ";
        $query .= "     L0.CHAIRNAME, ";
        $query .= "     L1.GRADE, ";
        $query .= "     L1.HR_CLASS, ";
        $query .= "     L1.ATTENDNO, ";
        $query .= "     L2.NAME, ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T1.APPDATE, ";
        $query .= "     T1.APPENDDATE, ";
        $query .= "     T1.ROW, ";
        $query .= "     T1.COLUMN, ";
        $query .= "     '".$model->lastColumn."' ";
        $query .= " FROM ";
        $query .= "     CHAIR_STD_DAT T1 ";
        $query .= " LEFT JOIN CHAIR_DAT L0 ON L0.YEAR = T1.YEAR AND L0.SEMESTER = T1.SEMESTER AND L0.CHAIRCD = T1.CHAIRCD ";
        $query .= " LEFT JOIN ";
        $query .= "     SCHREG_REGD_DAT L1 ON  L1.YEAR     = T1.YEAR ";
        $query .= "                        AND L1.SEMESTER = T1.SEMESTER ";
        $query .= "                        AND L1.SCHREGNO = T1.SCHREGNO ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "      INNER JOIN SCHREG_REGD_GDAT REG_G ON L1.YEAR = REG_G.YEAR ";
                $query .= "           AND L1.GRADE = REG_G.GRADE ";
                $query .= "           AND REG_G.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind), "','")."') ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "      INNER JOIN SCHREG_REGD_GDAT REG_G ON L1.YEAR = REG_G.YEAR ";
            $query .= "           AND L1.GRADE = REG_G.GRADE ";
            $query .= "           AND REG_G.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " LEFT JOIN ";
        $query .= "     SCHREG_BASE_MST L2 ON L2.SCHREGNO = T1.SCHREGNO ";

        $query .= " WHERE ";
        $query .= "         T1.YEAR || T1.SEMESTER = '{$model->field["YEAR"]}' ";
        if ($model->field["CHAIRCD"]!="") {
            $query .= " AND T1.CHAIRCD = '{$model->field["CHAIRCD"]}' ";
        }
        if ($model->field["SCHREGNO"]!="") {
            $query .= " AND T1.SCHREGNO = '{$model->field["SCHREGNO"]}' ";
        }
        if ($model->field["APPDATE"]!="") {
            $query .= " AND T1.APPDATE = '".str_replace("/", "-", $model->field["APPDATE"])."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     CHAIRCD, ";
        $query .= "     SCHREGNO, ";
        $query .= "     APPDATE ";

        return $query;
    }

    //マスタ(存在チェック用)
    public function getMasterCheck($model, $data, $chk_no)
    {
        $flg     = true;
        $query     = "";
        //条件選択
        switch ($chk_no) {
            case "1":
                $where  = "chair_dat where YEAR = '".$data["YEAR"]."' ";
                $where .= "and SEMESTER = '".$data["SEMESTER"]."' ";
                $where .= "and CHAIRCD = '".$data["CHAIRCD"]."' ";
                if ($model->Properties["use_prg_schoolkind"] == "1") {
                    if ($model->selectSchoolKind) {
                        $where .= "AND SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind), "','")."') ";
                    }
                } elseif ($model->Properties["useSchool_KindField"] == "1") {
                    $where .= "AND SCHOOL_KIND = '".SCHOOLKIND."' ";
                }
                break;
            case "2":
                $where  = "schreg_regd_dat T1 ";
                if ($model->Properties["use_prg_schoolkind"] == "1") {
                    if ($model->selectSchoolKind) {
                        $where .= " INNER JOIN SCHREG_REGD_GDAT REG_G ON T1.YEAR = REG_G.YEAR ";
                        $where .= "     AND T1.GRADE = REG_G.GRADE ";
                        $where .= "     AND REG_G.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind), "','")."') ";
                    }
                } elseif ($model->Properties["useSchool_KindField"] == "1") {
                    $where .= " INNER JOIN SCHREG_REGD_GDAT REG_G ON T1.YEAR = REG_G.YEAR ";
                    $where .= "     AND T1.GRADE = REG_G.GRADE ";
                    $where .= "     AND REG_G.SCHOOL_KIND = '".SCHOOLKIND."' ";
                }
                $where .= "where T1.YEAR = '".$data["YEAR"]."' ";
                $where .= "and T1.SEMESTER = '".$data["SEMESTER"]."' ";
                $where .= "and T1.SCHREGNO = '".$data["SCHREGNO"]."' ";
                break;
            case "3":
                $where  = "semester_mst where YEAR = '".$data["YEAR"]."' ";
                $where .= "and SEMESTER = '".$data["SEMESTER"]."' ";
                $where .= "and '".$data["APPDATE"]."' BETWEEN SDATE AND EDATE ";
                $where .= "and '".$data["APPENDDATE"]."' BETWEEN SDATE AND EDATE ";
                break;
            default:
                $flg = false;
                break;
        }
        if ($flg) {
            $query = "SELECT 'X' FROM " . $where;
        }

        return $query;
    }


    //マスタ(追加・更新・削除用)
    public function getExistCheck($data)
    {
        $query  = "select 'X' from chair_std_dat ";
        $query .= "where YEAR = '".$data["YEAR"]."' ";
        $query .= "and SEMESTER = '".$data["SEMESTER"]."' ";
        $query .= "and CHAIRCD = '".$data["CHAIRCD"]."' ";
        $query .= "and APPDATE = '".$data["APPDATE"]."' ";
        $query .= "and SCHREGNO = '".$data["SCHREGNO"]."' ";

        return $query;
    }


    //マスタデータの削除
    public function deleteQueryCsv(&$db, $data)
    {
        $query  = "DELETE FROM chair_std_dat ";
        $query .= "where YEAR = '".$data["YEAR"]."' ";
        $query .= "and SEMESTER = '".$data["SEMESTER"]."' ";
        $query .= "and CHAIRCD = '".$data["CHAIRCD"]."' ";
        $query .= "and APPDATE = '".$data["APPDATE"]."' ";
        $query .= "and SCHREGNO = '".$data["SCHREGNO"]."' ";

        $result = $db->query($query);
    }


    //マスタの追加（ＣＳＶデータより読込）
    public function insertQueryCsv(&$db, $data, $registercd)
    {
        $data1["YEAR"][TEXT]                 = $data["YEAR"];
        $data1["SEMESTER"][TEXT]             = $data["SEMESTER"];
        $data1["CHAIRCD"][TEXT]             = $data["CHAIRCD"];
        $data1["SCHREGNO"][TEXT]             = $data["SCHREGNO"];
        $data1["APPDATE"][TEXT]             = $data["APPDATE"];
        $data1["APPENDDATE"][TEXT]             = $data["APPENDDATE"];
        $data1["ROW"][TEXT]                 = $data["ROW"];
        $data1["COLUMN"][TEXT]                 = $data["COLUMN"];

        $data1["REGISTERCD"][TEXT]             = $registercd;
        $data1["UPDATED"][NUMBER]             = "sysdate()";

        $query = Query::insertSQL($data1, "chair_std_dat");

        $result = $db->query($query);
    }


    //マスタの更新（ＣＳＶデータより読込）
    public function updateQueryCsv(&$db, $data, $registercd)
    {
        $data1["YEAR"][TEXT]                 = $data["YEAR"];
        $data1["SEMESTER"][TEXT]             = $data["SEMESTER"];
        $data1["CHAIRCD"][TEXT]             = $data["CHAIRCD"];
        $data1["SCHREGNO"][TEXT]             = $data["SCHREGNO"];
        $data1["APPDATE"][TEXT]             = $data["APPDATE"];
        $data1["APPENDDATE"][TEXT]             = $data["APPENDDATE"];
        $data1["ROW"][TEXT]                 = $data["ROW"];
        $data1["COLUMN"][TEXT]                 = $data["COLUMN"];

        $data1["REGISTERCD"][TEXT]             = $registercd;
        $data1["UPDATED"][NUMBER]             = "sysdate()";

        $where  = "where YEAR = '".$data["YEAR"]."' ";
        $where .= "and SEMESTER = '".$data["SEMESTER"]."' ";
        $where .= "and CHAIRCD = '".$data["CHAIRCD"]."' ";
        $where .= "and APPDATE = '".$data["APPDATE"]."' ";
        $where .= "and SCHREGNO = '".$data["SCHREGNO"]."' ";

        $query = Query::updateSQL($data1, "chair_std_dat", $where);

        $result = $db->query($query);
    }

    //エラーＤＢへの追加
    public function insertQueryErr(&$db, $record_no, $check_error)
    {
        $data1["PROGRAMID"][TEXT]   = "KNJX090B";
        $data1["MSGROW"][NUMBER]    = $record_no;
        $data1["MSGREMARK"][TEXT]   = $check_error;

        $query = Query::insertSQL($data1, "W_CSVMSG_PRG_DAT");

        $result = $db->query($query);
    }

    //学年一覧
    public function getSelectFieldSQL5($model)
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
            $query .= " ,SCHREG_REGD_GDAT GDAT ";
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
