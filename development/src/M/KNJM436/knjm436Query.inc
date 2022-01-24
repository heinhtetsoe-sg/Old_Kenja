<?php

require_once('for_php7.php');
class knjm436query extends Query
{

    //科目一覧
    public function getSelectFieldSQL($model)
    {
        $query  = " SELECT ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "     t1.CLASSCD || '-' || t1.SCHOOL_KIND || '-' || t1.CURRICULUM_CD || '-' || t1.SUBCLASSCD AS SUBCLASSCD, ";
        } else {
            $query .= "     t1.SUBCLASSCD, ";
        }
        $query .= "     MAX(t2.SUBCLASSNAME) AS SUBCLASSNAME ";
        $query .= " FROM ";
        $query .= "     CHAIR_DAT t1, ";
        $query .= "     SUBCLASS_MST t2 ";
        $query .= " WHERE ";
        $query .= "     t1.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND t1.SEMESTER = '".CTRL_SEMESTER."' ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "     AND t1.CLASSCD = t2.CLASSCD ";
            $query .= "     AND t1.SCHOOL_KIND = t2.SCHOOL_KIND ";
            $query .= "     AND t1.CURRICULUM_CD = t2.CURRICULUM_CD ";
        }
        $query .= "     AND t1.SUBCLASSCD = t2.SUBCLASSCD ";
        $query .= " GROUP BY ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "     t1.CLASSCD || '-' || t1.SCHOOL_KIND || '-' || t1.CURRICULUM_CD || '-' || t1.SUBCLASSCD ";
        } else {
            $query .= "     t1.SUBCLASSCD ";
        }
        $query .= " ORDER BY ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "     t1.CLASSCD || '-' || t1.SCHOOL_KIND || '-' || t1.CURRICULUM_CD || '-' || t1.SUBCLASSCD ";
        } else {
            $query .= "     t1.SUBCLASSCD ";
        }

        return $query;
    }

    //データ出力
    public function getselectDataSQL($model)
    {
        $query  = "SELECT ";
        $query .= "    t1.YEAR,t1.SCHREGNO,t2.NAME,t4.HR_NAME,t3.ATTENDNO, ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "     t1.CLASSCD,";
            $query .= "     t1.SCHOOL_KIND,";
            $query .= "     t1.CURRICULUM_CD,";
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
            $query .= "     AND t1.CLASSCD = t5.CLASSCD ";
            $query .= "     AND t1.SCHOOL_KIND = t5.SCHOOL_KIND ";
            $query .= "     AND t1.CURRICULUM_CD = t5.CURRICULUM_CD ";
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
                $where = "SUBCLASS_MST WHERE ";
                if ($model->Properties["useCurriculumcd"] == "1") {
                    $where .= " CLASSCD = '".$data["CLASSCD"]."' AND ";
                    $where .= " SCHOOL_KIND = '".$data["SCHOOL_KIND"]."' AND ";
                    $where .= " CURRICULUM_CD = '".$data["CURRICULUM_CD"]."' AND ";
                }
                $where .= " SUBCLASSCD = '".$data["SUBCLASSCD"]."' ";
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
            $query .= "     AND CLASSCD = '".$data["CLASSCD"]."' ";
            $query .= "     AND SCHOOL_KIND = '".$data["SCHOOL_KIND"]."' ";
            $query .= "     AND CURRICULUM_CD = '".$data["CURRICULUM_CD"]."' ";
        }
        $query .= "AND SUBCLASSCD = '".$data["SUBCLASSCD"]."' ";
        $query .= "AND TAKESEMES = '0' ";
        $query .= "AND SCHREGNO = '".$data["SCHREGNO"]."' ";

        return $query;
    }


    //履修単位・修得単位を更新する為単位マスタから単位数を取得する
    public function getCredits($data, $model)
    {
        $query = " SELECT w1.schregno,w1.year,w1.semester,w1.grade,w1.coursecd,w1.majorcd,w1.coursecode,w2.credits as CREDITS ";
        $query .= " FROM (select schregno,year,semester,grade,coursecd,majorcd,coursecode from schreg_regd_dat ";
        $query .= " where schregno='".$data["SCHREGNO"]."' and year='".CTRL_YEAR."' and semester='".CTRL_SEMESTER."' ) w1 ";
        $query .= " LEFT OUTER JOIN ";
        $query .= " (SELECT year,coursecd,majorcd,grade,coursecode,credits FROM credit_mst ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= " where ";
            $query .= "     CLASSCD = '".$data["CLASSCD"]."' ";
            $query .= "     AND SCHOOL_KIND = '".$data["SCHOOL_KIND"]."' ";
            $query .= "     AND CURRICULUM_CD = '".$data["CURRICULUM_CD"]."' ";
            $query .= "     AND SUBCLASSCD ='".$data["SUBCLASSCD"]."' ) w2 ON ";
        } else {
            $query .= " where classcd = '".substr($data["SUBCLASSCD"], 0, 2)."' AND subclasscd ='".$data["SUBCLASSCD"]."' ) w2 ON ";
        }
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
            $data1["CLASSCD"][TEXT]             = $data["CLASSCD"];
            $data1["SCHOOL_KIND"][TEXT]         = $data["SCHOOL_KIND"];
            $data1["CURRICULUM_CD"][TEXT]       = $data["CURRICULUM_CD"];
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
        //湧心館通信制では、「評定＝１」の場合は「未履修」とする。
        } elseif ($data["GRAD_VALUE"] == "1") {
            $data1["COMP_CREDIT"][NUMBER]       = 0;
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
            $data1["CLASSCD"][TEXT]             = $data["CLASSCD"];
            $data1["SCHOOL_KIND"][TEXT]         = $data["SCHOOL_KIND"];
            $data1["CURRICULUM_CD"][TEXT]       = $data["CURRICULUM_CD"];
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
        //湧心館通信制では、「評定＝１」の場合は「未履修」とする。
        } elseif ($data["GRAD_VALUE"] == "1") {
            $data1["COMP_CREDIT"][NUMBER]       = 0;
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
