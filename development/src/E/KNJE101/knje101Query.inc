<?php

require_once('for_php7.php');
class knje101Query extends Query
{

    //年度一覧
    public function getYearSem($val = "")
    {
        $query  = "SELECT DISTINCT ";
        $query .= "    T1.YEAR || '年度' || VALUE(T2.SEMESTERNAME, '') AS LABEL, ";
        $query .= "    T1.YEAR || T1.SEMESTER AS VALUE ";
        $query .= "FROM ";
        $query .= "    SCHREG_REGD_HDAT T1 ";
        $query .= "    INNER JOIN SEMESTER_MST T2 ON T1.YEAR = T2.YEAR ";
        $query .= "          AND T1.SEMESTER = T2.SEMESTER ";
        if ($val != "") {
            $query .= "WHERE ";
            $query .= "    T1.YEAR || T1.SEMESTER = '".$val."' ";
        }
        $query .= "ORDER BY ";
        $query .= "    VALUE ";

        return $query;
    }

    //年組一覧
    public function getGradeClass($model, $val = "")
    {
        $query  = "SELECT DISTINCT ";
        $query .= "    GRADE || HR_CLASS AS VALUE, ";
        $query .= "    HR_NAME AS LABEL ";
        $query .= "FROM ";
        $query .= "    SCHREG_REGD_HDAT ";
        $query .= "WHERE ";
        $query .= "    YEAR || SEMESTER = '".$model->field["YEAR"]."' ";
        if ($val != "") {
            $query .= "    AND GRADE || HR_CLASS = '".$val."' ";
        }
        $query .= "ORDER BY ";
        $query .= "    VALUE ";

        return $query;
    }

    //テスト種別一覧
    public function getShamexamcd($model, $val = "")
    {
        $query .= " SELECT DISTINCT ";
        $query .= "     T1.SHAMEXAMCD AS VALUE, ";
        $query .= "     CASE WHEN L1.GRADE = '01' AND T1.SHAMEXAMCD = '06' ";
        $query .= "          THEN '宿題テスト' ";
        $query .= "          ELSE '第' || CAST(CAST(T1.SHAMEXAMCD AS SMALLINT) AS CHAR(1)) || '回' ";
        $query .= "     END AS LABEL ";
        $query .= " FROM ";
        $query .= "     SHAMEXAMINATION_DAT T1 ";
        $query .= "     INNER JOIN SCHREG_REGD_DAT L1 ON L1.SCHREGNO = T1.SCHREGNO ";
        $query .= "           AND L1.YEAR || L1.SEMESTER = '".$model->field["YEAR"]."' ";
        $query .= "           AND L1.GRADE || L1.HR_CLASS = '".$model->field["GRADE_HR_CLASS"]."' ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".substr($model->field["YEAR"], 0, 4)."' ";
        if ($val != "") {
            $query .= "     AND T1.SHAMEXAMCD = '".$model->field["SHAMEXAMCD"]."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //実行（更新）
    public function insertQuery($model, &$data_arr)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $cnt = 0;    //処理件数
        $count = get_count($data_arr);
        for ($i=0; $i<$count; $i++) {
            $table = "SHAMEXAMINATION_DAT";
            $query    = " SELECT COUNT(*) FROM ".$table ;
            $query .= "    WHERE YEAR = '".$data_arr[$i]["YEAR"]."' ";
            $query .= "    AND SHAMEXAMCD  = '".$data_arr[$i]["SHAMEXAMCD"]."' ";
            $query .= "    AND SCHREGNO    = '".$data_arr[$i]["SCHREGNO"]."' ";
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "    AND CLASSCD       = '".$data_arr[$i]["CLASSCD"]."' ";
                $query .= "    AND SCHOOL_KIND   = '".$data_arr[$i]["SCHOOL_KIND"]."' ";
                $query .= "    AND CURRICULUM_CD = '".$data_arr[$i]["CURRICULUM_CD"]."' ";
            }
            $query .= "    AND SUBCLASSCD  = '".$data_arr[$i]["SUBCLASSCD"]."' ";

            $tblcnt = $db->getOne($query);
            $where = "";
            $data = array();
            if ($tblcnt > 0) {
                $data["SUBCLASSNAME"][TEXT]         = $data_arr[$i]["SUBCLASSNAME"];
                $data["SUBCLASSCD_CNT"][TEXT]       = $data_arr[$i]["SUBCLASSCD_CNT"];
                $data["TRADE"][TEXT]                = $data_arr[$i]["TRADE"];
                $data["SCORE"][NUMBER]              = $data_arr[$i]["SCORE"];
                $data["DEVIATION"][NUMBER]          = $data_arr[$i]["DEVIATION"];
                $data["SCHOOL_DEVIATION"][NUMBER]   = $data_arr[$i]["SCHOOL_DEVIATION"];
                $data["PRECEDENCE"][NUMBER]         = $data_arr[$i]["PRECEDENCE"];
                $data["SCHOOL_PRECEDENCE"][NUMBER]  = $data_arr[$i]["SCHOOL_PRECEDENCE"];
                $data["REGISTERCD"][TEXT]           = STAFFCD;
                $data["UPDATED"][FUNC]              = "SYSDATE()";

                $where  = "     WHERE YEAR          = '".$data_arr[$i]["YEAR"]."' ";
                $where .= "       AND SHAMEXAMCD    = '".$data_arr[$i]["SHAMEXAMCD"]."' ";
                $where .= "       AND SCHREGNO      = '".$data_arr[$i]["SCHREGNO"]."' ";
                if ($model->Properties["useCurriculumcd"] == '1') {
                    $where .= "       AND CLASSCD       = '".$data_arr[$i]["CLASSCD"]."' ";
                    $where .= "       AND SCHOOL_KIND   = '".$data_arr[$i]["SCHOOL_KIND"]."' ";
                    $where .= "       AND CURRICULUM_CD = '".$data_arr[$i]["CURRICULUM_CD"]."' ";
                }
                $where .= "       AND SUBCLASSCD    = '".$data_arr[$i]["SUBCLASSCD"]."' ";

                $insup = "up";
            } else {
                $data["YEAR"][TEXT]                 = $data_arr[$i]["YEAR"];
                $data["SHAMEXAMCD"][TEXT]           = $data_arr[$i]["SHAMEXAMCD"];
                $data["SCHREGNO"][TEXT]             = $data_arr[$i]["SCHREGNO"];
                if ($model->Properties["useCurriculumcd"] == '1') {
                    $data["CLASSCD"][TEXT]          = $data_arr[$i]["CLASSCD"];
                    $data["SCHOOL_KIND"][TEXT]      = $data_arr[$i]["SCHOOL_KIND"];
                    $data["CURRICULUM_CD"][TEXT]    = $data_arr[$i]["CURRICULUM_CD"];
                }
                $data["SUBCLASSCD"][TEXT]           = $data_arr[$i]["SUBCLASSCD"];
                $data["SUBCLASSNAME"][TEXT]         = $data_arr[$i]["SUBCLASSNAME"];
                $data["SUBCLASSCD_CNT"][TEXT]       = $data_arr[$i]["SUBCLASSCD_CNT"];
                $data["TRADE"][TEXT]                = $data_arr[$i]["TRADE"];
                $data["SCORE"][NUMBER]              = $data_arr[$i]["SCORE"];
                $data["DEVIATION"][NUMBER]          = $data_arr[$i]["DEVIATION"];
                $data["SCHOOL_DEVIATION"][NUMBER]   = $data_arr[$i]["SCHOOL_DEVIATION"];
                $data["PRECEDENCE"][NUMBER]         = $data_arr[$i]["PRECEDENCE"];
                $data["SCHOOL_PRECEDENCE"][NUMBER]  = $data_arr[$i]["SCHOOL_PRECEDENCE"];
                $data["REGISTERCD"][TEXT]           = STAFFCD;
                $data["UPDATED"][FUNC]              = "SYSDATE()";

                $insup = "ins";
            }

            if ($insup == "ins") {
                $query = Query::insertSQL($data, $table);
                $db->query($query);
            } elseif ($insup == "up") {
                $query = Query::updateSQL($data, $table, $where);
                $db->query($query);
            }

            $cnt++;
        }
        $db->commit();
        Query::dbCheckIn($db);

        return $cnt;
    }

    //実行（削除）
    public function deleteQuery($model, &$data_arr)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $cnt = 0;    //処理件数
        $count = get_count($data_arr);
        for ($i=0; $i<$count; $i++) {
            $query  = " SELECT " ;
            $query .= "     COUNT(*) " ;
            $query .= " FROM " ;
            $query .= "     SHAMEXAMINATION_DAT " ;
            $query .= " WHERE ";
            $query .= "     YEAR            = '".$data_arr[$i]["YEAR"]."' ";
            $query .= " AND SHAMEXAMCD      = '".$data_arr[$i]["SHAMEXAMCD"]."' ";
            $query .= " AND SCHREGNO        = '".$data_arr[$i]["SCHREGNO"]."' ";
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= " AND CLASSCD         = '".$data_arr[$i]["CLASSCD"]."' ";
                $query .= " AND SCHOOL_KIND     = '".$data_arr[$i]["SCHOOL_KIND"]."' ";
                $query .= " AND CURRICULUM_CD   = '".$data_arr[$i]["CURRICULUM_CD"]."' ";
            }
            $query .= " AND SUBCLASSCD      = '".$data_arr[$i]["SUBCLASSCD"]."' ";
            $tblcnt = $db->getOne($query);

            if ($tblcnt > 0) {
                $query  = " DELETE FROM ";
                $query .= "     SHAMEXAMINATION_DAT ";
                $query .= " WHERE ";
                $query .= "     YEAR            = '".$data_arr[$i]["YEAR"]."' ";
                $query .= " AND SHAMEXAMCD      = '".$data_arr[$i]["SHAMEXAMCD"]."' ";
                $query .= " AND SCHREGNO        = '".$data_arr[$i]["SCHREGNO"]."' ";
                if ($model->Properties["useCurriculumcd"] == '1') {
                    $query .= " AND CLASSCD         = '".$data_arr[$i]["CLASSCD"]."' ";
                    $query .= " AND SCHOOL_KIND     = '".$data_arr[$i]["SCHOOL_KIND"]."' ";
                    $query .= " AND CURRICULUM_CD   = '".$data_arr[$i]["CURRICULUM_CD"]."' ";
                }
                $query .= " AND SUBCLASSCD      = '".$data_arr[$i]["SUBCLASSCD"]."' ";

                $db->query($query);
                $cnt++;
            }
        }
        $db->commit();
        Query::dbCheckIn($db);

        return $cnt;
    }

    //エラーＤＢへの追加
    public function insertQueryErr(&$db, $record_no, $check_error)
    {
        $data1 = array();
        $data1["PROGRAMID"][TEXT]         = PROGRAMID;
        $data1["MSGROW"][NUMBER]          = $record_no;
        $data1["MSGREMARK"][TEXT]         = $check_error;

        $query = Query::insertSQL($data1, "W_CSVMSG_PRG_DAT");
        $result = $db->query($query);
    }

    //データ出力CVS作成用
    public function getselectDataSQL($model)
    {
        $query  = "SELECT ";
        $query .= "    T1.YEAR, ";
        $query .= "    L2.HR_NAME || '-' || L1.ATTENDNO || '番', ";
        $query .= "    L3.NAME, ";
        $query .= "    T1.SHAMEXAMCD, ";
        $query .= "    T1.SCHREGNO, ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "    T1.CLASSCD, ";
            $query .= "    T1.SCHOOL_KIND, ";
            $query .= "    T1.CURRICULUM_CD, ";
        }
        $query .= "    T1.SUBCLASSCD, ";
        $query .= "    T1.SUBCLASSNAME, ";
        $query .= "    T1.SUBCLASSCD_CNT, ";
        $query .= "    T1.TRADE, ";
        $query .= "    T1.SCORE, ";
        $query .= "    T1.PRECEDENCE, ";
        $query .= "    T1.DEVIATION, ";
        $query .= "    T1.SCHOOL_PRECEDENCE, ";
        $query .= "    T1.SCHOOL_DEVIATION, ";
        $query .= "    '".$model->lastColumn."' ";
        $query .= "FROM ";
        $query .= "    SHAMEXAMINATION_DAT T1 ";
        $query .= "    INNER JOIN SCHREG_REGD_DAT L1 ON L1.SCHREGNO = T1.SCHREGNO ";
        $query .= "          AND L1.YEAR || L1.SEMESTER = '".$model->field["YEAR"]."' ";
        $query .= "          AND L1.GRADE || L1.HR_CLASS = '".$model->field["GRADE_HR_CLASS"]."' ";
        $query .= "    LEFT JOIN SCHREG_REGD_HDAT L2 ON L2.YEAR || L2.SEMESTER = L1.YEAR || L1.SEMESTER ";
        $query .= "         AND L2.GRADE || L2.HR_CLASS = L1.GRADE || L1.HR_CLASS ";
        $query .= "    LEFT JOIN SCHREG_BASE_MST L3 ON L3.SCHREGNO = T1.SCHREGNO ";
        $query .= "WHERE ";
        $query .= "    T1.YEAR = '".substr($model->field["YEAR"], 0, 4)."' ";
        if ($model->field["SHAMEXAMCD"] != "") {
            $query .= "    AND T1.SHAMEXAMCD = '".$model->field["SHAMEXAMCD"]."' ";
        }
        $query .= "ORDER BY ";
        $query .= "    T1.SHAMEXAMCD, ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "    T1.CLASSCD, ";
            $query .= "    T1.SCHOOL_KIND, ";
            $query .= "    T1.CURRICULUM_CD, ";
        }
        $query .= "    T1.SUBCLASSCD, ";
        $query .= "    L1.ATTENDNO ";

        return $query;
    }

    //名称マスタ取得
    public function getNameMst($namecd1, $target)
    {
        $query  = " SELECT ";
        $query .=       $target;
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = '".$namecd1."' ";
        $query .= " ORDER BY ";
        $query .= "     NAMECD2 ";

        return $query;
    }
}
