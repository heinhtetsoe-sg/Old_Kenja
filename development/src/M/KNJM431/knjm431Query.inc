<?php

require_once('for_php7.php');
class knjm431Query extends Query
{
    public function getSecurityHigh()
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     MENU_HIGH_SECURITY_MST ";
        $query .= " WHERE ";
        $query .= "     PROGRAMID = 'KNJM431' ";
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

    //科目一覧
    public function getSelectFieldSQL($model)
    {
        $query  = "SELECT ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "t1.CLASSCD || '-' || t1.SCHOOL_KIND || '-' || t1.CURRICULUM_CD || '-' || t1.SUBCLASSCD AS SUBCLASSCD, ";
        } else {
            $query .= "t1.SUBCLASSCD, ";
        }
        $query .= "MAX(t2.SUBCLASSNAME) AS SUBCLASSNAME ";
        $query .= "FROM CHAIR_DAT t1,SUBCLASS_MST t2 ";
        $query .= "WHERE t1.YEAR = '".CTRL_YEAR."' ";
        $query .= "      AND t1.SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= "AND t1.SUBCLASSCD = t2.SUBCLASSCD ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "AND t1.CLASSCD = t2.CLASSCD ";
            $query .= "AND t1.SCHOOL_KIND = t2.SCHOOL_KIND ";
            $query .= "AND t1.CURRICULUM_CD = t2.CURRICULUM_CD ";
        }
        $query .= "GROUP BY ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "t1.CLASSCD, ";
            $query .= "t1.SCHOOL_KIND, ";
            $query .= "t1.CURRICULUM_CD, ";
        }
        $query .= "t1.SUBCLASSCD ";
        $query .= "ORDER BY ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "t1.CLASSCD, ";
            $query .= "t1.SCHOOL_KIND, ";
            $query .= "t1.CURRICULUM_CD, ";
        }
        $query .= "t1.SUBCLASSCD";

        return $query;
    }

    //データ出力
    public function getselectDataSQL($model)
    {
        $query  = "SELECT ";
        $query .= "    t1.YEAR,t1.SCHREGNO,t2.NAME,t4.HR_NAME,t3.ATTENDNO, ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "t1.CLASSCD, ";
            $query .= "t1.SCHOOL_KIND, ";
            $query .= "t1.CURRICULUM_CD, ";
        }
        $query .= "    t1.SUBCLASSCD,t5.SUBCLASSNAME, ";
        $query .= "    t1.SEM1_TERM_SCORE,t1.SEM1_VALUE, ";
        $query .= "    t1.SEM2_TERM_SCORE,t1.SEM2_VALUE, ";
        $query .= "    t1.GRAD_VALUE,'".$model->lastColumn."' AS ".$model->lastColumn." ";
        $query .= "FROM ";
        $query .= "    RECORD_DAT t1 ";
        $query .= "    LEFT JOIN SCHREG_BASE_MST t2 ON t1.SCHREGNO = t2.SCHREGNO ";
        $query .= "    LEFT JOIN SCHREG_REGD_DAT t3 ON t1.SCHREGNO = t3.SCHREGNO ";
        $query .= "    AND t1.YEAR = t3.YEAR ";
        $query .= "    AND t3.SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= "    LEFT JOIN SCHREG_REGD_HDAT t4 ON t1.YEAR = t4.YEAR ";
        $query .= "    AND t3.SEMESTER = t4.SEMESTER ";
        $query .= "    AND t3.GRADE || t3.HR_CLASS = t4.GRADE || t4.HR_CLASS ";
        $query .= "    LEFT JOIN SUBCLASS_MST t5 ON t1.SUBCLASSCD = t5.SUBCLASSCD ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "AND t1.CLASSCD = t5.CLASSCD ";
            $query .= "AND t1.SCHOOL_KIND = t5.SCHOOL_KIND ";
            $query .= "AND t1.CURRICULUM_CD = t5.CURRICULUM_CD ";
        }
        $query .= "WHERE ";
        $query .= "    t1.YEAR = '".CTRL_YEAR."' ";
        $query .= "    AND t1.TAKESEMES = '0' ";
        if ($model->field["SUBCLASSCD"] != "") {
            if ($model->Properties["useCurriculumcd"] == "1") {
                $query .= "    AND t1.CLASSCD || '-' || t1.SCHOOL_KIND || '-' || t1.CURRICULUM_CD || '-' || t1.SUBCLASSCD = '".$model->field["SUBCLASSCD"]."' ";
            } else {
                $query .= "    AND t1.SUBCLASSCD = '".$model->field["SUBCLASSCD"]."' ";
            }
        }
        $query .= "ORDER BY ";
        $query .= "    t1.SCHREGNO ";

        return $query;
    }

    //マスタ(存在チェック用)
    public function getMasterCheck($data, $chk_no, $model)
    {
        $flg    = true;
        $query  = "";
        //条件選択
        switch ($chk_no) {
            case "1":
                $where = "SCHREG_BASE_MST WHERE SCHREGNO = '".$data["SCHREGNO"]."' ";
                break;
            case "2":
                if ($model->Properties["useCurriculumcd"] == "1") {
                    $where  = "SUBCLASS_MST WHERE CLASSCD = '".$data["CLASSCD"]."' ";
                    $where .= " AND SCHOOL_KIND = '".$data["SCHOOL_KIND"]."' ";
                    $where .= " AND CURRICULUM_CD = '".$data["CURRICULUM_CD"]."' ";
                    $where .= " AND SUBCLASSCD = '".$data["SUBCLASSCD"]."' ";
                } else {
                    $where = "SUBCLASS_MST WHERE SUBCLASSCD = '".$data["SUBCLASSCD"]."' ";
                }
                break;
            case "3":
                $where = "NAME_MST WHERE NAMECD1 = 'Z018' AND NAMECD2 = '".$data["CURRICULUM_CD"]."' ";
                break;
            case "4":
                $where = "SCHREG_REGD_GDAT WHERE YEAR = '".$data["YEAR"]."' AND SCHOOL_KIND = '".$data["SCHOOL_KIND"]."' ";
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
    public function getExistCheck($data, $model)
    {
        $query  = "SELECT 'X' FROM RECORD_DAT ";
        $query .= "WHERE YEAR = '".$data["YEAR"]."' ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "AND CLASSCD = '".$data["CLASSCD"]."' ";
            $query .= "AND SCHOOL_KIND = '".$data["SCHOOL_KIND"]."' ";
            $query .= "AND CURRICULUM_CD = '".$data["CURRICULUM_CD"]."' ";
            $query .= "AND SUBCLASSCD = '".$data["SUBCLASSCD"]."' ";
        } else {
            $query .= "AND SUBCLASSCD = '".$data["SUBCLASSCD"]."' ";
        }
        $query .= "AND TAKESEMES = '0' ";
        $query .= "AND SCHREGNO = '".$data["SCHREGNO"]."' ";

        return $query;
    }


    //履修単位・修得単位を更新する為単位マスタから単位数を取得する
    public function getCredits($data, $model)
    {
        $query  = " SELECT ";
        $query .= "     w1.SCHREGNO, ";
        $query .= "     w1.YEAR, ";
        $query .= "     w1.SEMESTER, ";
        $query .= "     w1.GRADE, ";
        $query .= "     w1.COURSECD, ";
        $query .= "     w1.MAJORCD, ";
        $query .= "     w1.COURSECODE, ";
        $query .= "     w2.CREDITS AS CREDITS ";
        $query .= " FROM (SELECT ";
        $query .= "           SCHREGNO, ";
        $query .= "           YEAR, ";
        $query .= "           SEMESTER, ";
        $query .= "           GRADE, ";
        $query .= "           COURSECD, ";
        $query .= "           MAJORCD, ";
        $query .= "           COURSECODE ";
        $query .= "       FROM ";
        $query .= "           SCHREG_REGD_DAT ";
        $query .= "       WHERE ";
        $query .= "           SCHREGNO = '".$data["SCHREGNO"]."' ";
        $query .= "           AND YEAR = '".CTRL_YEAR."' ";
        $query .= "           AND SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= "  ) w1 ";
        $query .= " LEFT OUTER JOIN ";
        $query .= " (SELECT ";
        $query .= "     YEAR, ";
        $query .= "     COURSECD, ";
        $query .= "     MAJORCD, ";
        $query .= "     GRADE, ";
        $query .= "     COURSECODE, ";
        $query .= "     CLASSCD, ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "     SCHOOL_KIND, ";
            $query .= "     CURRICULUM_CD, ";
        }
        $query .= "     SUBCLASSCD, ";
        $query .= "     CREDITS ";
        $query .= "  FROM ";
        $query .= "      CREDIT_MST ";
        $query .= "  WHERE ";
        $query .= "      CLASSCD = '".substr($data["SUBCLASSCD"], 0, 2)."' ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "      AND CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD ='".$data["SUBCLASSCD"]."' ";
        } else {
            $query .= "      AND SUBCLASSCD ='".$data["SUBCLASSCD"]."' ";
        }
        $query .= " ) w2 ON ";
        $query.= " w1.year = w2.year AND w1.coursecd = w2.coursecd AND w1.majorcd = w2.majorcd ";
        $query .= " AND w1.coursecode = w2.coursecode AND w1.grade = w2.grade";

        return $query;
    }


    //マスタの追加（ＣＳＶデータより読込）
    //パラメータの追加・$credits（単位数）
    public function insertQueryCsv(&$db, $data, $registercd, $credits, $model)
    {
        $wrkchaircd = $data["SUBCLASSCD"]."0";

        $data1["YEAR"][TEXT]                = $data["YEAR"];
        if ($model->Properties["useCurriculumcd"] == "1") {
            $data1["CLASSCD"][TEXT]          = $data["CLASSCD"];
            $data1["SCHOOL_KIND"][TEXT]      = $data["SCHOOL_KIND"];
            $data1["CURRICULUM_CD"][TEXT]    = $data["CURRICULUM_CD"];
        }
        $data1["SUBCLASSCD"][TEXT]          = $data["SUBCLASSCD"];
        $data1["TAKESEMES"][TEXT]           = '0';
        $data1["SCHREGNO"][TEXT]            = $data["SCHREGNO"];
        $data1["CHAIRCD"][TEXT]             = $wrkchaircd;
        $data1["SEM1_TERM_SCORE"][NUMBER]   = $data["SEM1_TERM_SCORE"];
        $data1["SEM1_VALUE"][NUMBER]        = $data["SEM1_VALUE"];
        $data1["SEM2_TERM_SCORE"][NUMBER]   = $data["SEM2_TERM_SCORE"];
        $data1["SEM2_VALUE"][NUMBER]        = $data["SEM2_VALUE"];
        $data1["GRAD_VALUE"][NUMBER]        = $data["GRAD_VALUE"];
        if ($data["GRAD_VALUE"] > "1") {
            $data1["COMP_CREDIT"][NUMBER]       = $credits;
            $data1["GET_CREDIT"][NUMBER]        = $credits;
        } elseif ($data["GRAD_VALUE"] == "1") {
            $data1["COMP_CREDIT"][NUMBER]       = $credits;
            $data1["GET_CREDIT"][NUMBER]        = 0;
        } elseif ($data["GRAD_VALUE"] == "0") {
            $data1["COMP_CREDIT"][NUMBER]       = 0;
            $data1["GET_CREDIT"][NUMBER]        = 0;
        } else {
            $data1["COMP_CREDIT"][NUMBER]       = null;
            $data1["GET_CREDIT"][NUMBER]        = null;
        }

        $data1["REGISTERCD"][TEXT]          = $registercd;
        $data1["UPDATED"][NUMBER]           = "sysdate()";

        $query = Query::insertSQL($data1, "RECORD_DAT");

        $result = $db->query($query);
    }

    //マスタの更新（ＣＳＶデータより読込）
    //パラメータの追加・$credits（単位数）
    public function updateQueryCsv(&$db, $data, $registercd, $credits, $model)
    {
        $wrkchaircd = $data["SUBCLASSCD"]."0";

        $data1["YEAR"][TEXT]                = $data["YEAR"];
        if ($model->Properties["useCurriculumcd"] == "1") {
            $data1["CLASSCD"][TEXT]          = $data["CLASSCD"];
            $data1["SCHOOL_KIND"][TEXT]      = $data["SCHOOL_KIND"];
            $data1["CURRICULUM_CD"][TEXT]    = $data["CURRICULUM_CD"];
        }
        $data1["SUBCLASSCD"][TEXT]          = $data["SUBCLASSCD"];
        $data1["TAKESEMES"][TEXT]           = '0';
        $data1["SCHREGNO"][TEXT]            = $data["SCHREGNO"];
        $data1["CHAIRCD"][TEXT]             = $wrkchaircd;
        $data1["SEM1_TERM_SCORE"][NUMBER]   = $data["SEM1_TERM_SCORE"];
        $data1["SEM1_VALUE"][NUMBER]        = $data["SEM1_VALUE"];
        $data1["SEM2_TERM_SCORE"][NUMBER]   = $data["SEM2_TERM_SCORE"];
        $data1["SEM2_VALUE"][NUMBER]        = $data["SEM2_VALUE"];
        $data1["GRAD_VALUE"][NUMBER]        = $data["GRAD_VALUE"];
        if ($data["GRAD_VALUE"] > "1") {
            $data1["COMP_CREDIT"][NUMBER]       = $credits;
            $data1["GET_CREDIT"][NUMBER]        = $credits;
        } elseif ($data["GRAD_VALUE"] == "1") {
            $data1["COMP_CREDIT"][NUMBER]       = $credits;
            $data1["GET_CREDIT"][NUMBER]        = 0;
        } elseif ($data["GRAD_VALUE"] == "0") {
            $data1["COMP_CREDIT"][NUMBER]       = 0;
            $data1["GET_CREDIT"][NUMBER]        = 0;
        } else {
            $data1["COMP_CREDIT"][NUMBER]       = null;
            $data1["GET_CREDIT"][NUMBER]        = null;
        }

        $data1["REGISTERCD"][TEXT]          = $registercd;
        $data1["UPDATED"][NUMBER]           = "sysdate()";

        $where  = "WHERE YEAR = '".$data["YEAR"]."' ";
        $where .= "AND TAKESEMES = '0' ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $where .= "AND CLASSCD = '".$data["CLASSCD"]."' ";
            $where .= "AND SCHOOL_KIND = '".$data["SCHOOL_KIND"]."' ";
            $where .= "AND CURRICULUM_CD = '".$data["CURRICULUM_CD"]."' ";
        }
        $where .= "AND SUBCLASSCD = '".$data["SUBCLASSCD"]."' ";
        $where .= "AND SCHREGNO = '".$data["SCHREGNO"]."' ";

        $query = Query::updateSQL($data1, "RECORD_DAT", $where);

        $result = $db->query($query);
    }

    //エラーＤＢへの追加
    public function insertQueryErr(&$db, $record_no, $check_error)
    {
        $data1["PROGRAMID"][TEXT]       = PROGRAMID;
        $data1["MSGROW"][NUMBER]        = $record_no;
        $data1["MSGREMARK"][TEXT]       = $check_error;

        $query = Query::insertSQL($data1, "W_CSVMSG_PRG_DAT");

        $result = $db->query($query);
    }
}
