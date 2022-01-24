<?php

require_once('for_php7.php');
class knje102query extends Query
{


    //年度一覧
    public function getSelectFieldSQL()
    {
        $query  = "select distinct t1.year,t1.semester,t2.semestername ";
        $query .= "from schreg_regd_hdat t1 ";
        $query .= "		inner join semester_mst t2 on t1.year=t2.year and t1.semester=t2.semester ";
        $query .= "order by t1.year,t1.semester";

        return $query;
    }

    //年組一覧
    public function getSelectFieldSQL2($model)
    {
        $query  = "select distinct grade,hr_class,hr_name ";
        $query .= "from schreg_regd_hdat ";
        $query .= "where YEAR || SEMESTER = '".$model->field["YEAR"]."' ";
        $query .= "order by grade,hr_class";

        return $query;
    }

    //実行
    public function insertQuery($model, &$data_arr)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $cnt = 0; //処理件数
        $count = get_count($data_arr);
        for ($i=0; $i<$count; $i++) {
            $table = "SHAMEXAMINATION_DAT";
            $query  = " SELECT COUNT(*) FROM ".$table ;
            $query .= "	WHERE YEAR = '".$data_arr[$i]["YEAR"]."' ";
            $query .= "	AND SHAMEXAMCD	  = '".$data_arr[$i]["SHAMEXAMCD"]."' ";
            $query .= "	AND SCHREGNO	  = '".$data_arr[$i]["SCHREGNO"]."' ";
            $query .= " AND CLASSCD       = '".$data_arr[$i]["CLASSCD"]."' ";
            $query .= " AND SCHOOL_KIND   = '".$data_arr[$i]["SCHOOL_KIND"]."' ";
            $query .= " AND CURRICULUM_CD = '".$data_arr[$i]["CURRICULUM_CD"]."' ";
            $query .= "	AND SUBCLASSCD	  = '".$data_arr[$i]["SUBCLASSCD"]."' ";

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
                $data["WISHSCHOOLCD1"][TEXT]        = $data_arr[$i]["WISHSCHOOLCD1"];
                $data["WISHSCHOOLCD2"][TEXT]        = $data_arr[$i]["WISHSCHOOLCD2"];
                $data["WISHSCHOOLCD3"][TEXT]        = $data_arr[$i]["WISHSCHOOLCD3"];
                $data["REGISTERCD"][TEXT]           = STAFFCD;
                $data["UPDATED"][FUNC]              = "SYSDATE()";
                $where = "	WHERE YEAR = '".$data_arr[$i]["YEAR"]."' ";
                $where .= "       AND SHAMEXAMCD	= '".$data_arr[$i]["SHAMEXAMCD"]."' ";
                $where .= "       AND SCHREGNO      = '".$data_arr[$i]["SCHREGNO"]."' ";
                $where .= "       AND SUBCLASSCD	= '".$data_arr[$i]["SUBCLASSCD"]."' ";
                $where .= "       AND CLASSCD       = '".$data_arr[$i]["CLASSCD"]."' ";
                $where .= "       AND SCHOOL_KIND   = '".$data_arr[$i]["SCHOOL_KIND"]."' ";
                $where .= "       AND CURRICULUM_CD = '".$data_arr[$i]["CURRICULUM_CD"]."' ";

                $insup = "up";
            } else {
                $data["YEAR"][TEXT]                 = $data_arr[$i]["YEAR"];
                $data["SHAMEXAMCD"][TEXT]           = $data_arr[$i]["SHAMEXAMCD"];
                $data["SCHREGNO"][TEXT]             = $data_arr[$i]["SCHREGNO"];
                $data["CLASSCD"][TEXT]              = $data_arr[$i]["CLASSCD"];
                $data["SCHOOL_KIND"][TEXT]          = $data_arr[$i]["SCHOOL_KIND"];
                $data["CURRICULUM_CD"][TEXT]        = $data_arr[$i]["CURRICULUM_CD"];
                $data["SUBCLASSCD"][TEXT]           = $data_arr[$i]["SUBCLASSCD"];
                $data["SUBCLASSNAME"][TEXT]         = $data_arr[$i]["SUBCLASSNAME"];
                $data["SUBCLASSCD_CNT"][TEXT]       = $data_arr[$i]["SUBCLASSCD_CNT"];
                $data["TRADE"][TEXT]                = $data_arr[$i]["TRADE"];
                $data["SCORE"][NUMBER]              = $data_arr[$i]["SCORE"];
                $data["DEVIATION"][NUMBER]          = $data_arr[$i]["DEVIATION"];
                $data["SCHOOL_DEVIATION"][NUMBER]   = $data_arr[$i]["SCHOOL_DEVIATION"];
                $data["PRECEDENCE"][NUMBER]         = $data_arr[$i]["PRECEDENCE"];
                $data["SCHOOL_PRECEDENCE"][NUMBER]  = $data_arr[$i]["SCHOOL_PRECEDENCE"];
                $data["WISHSCHOOLCD1"][TEXT]        = $data_arr[$i]["WISHSCHOOLCD1"];
                $data["WISHSCHOOLCD2"][TEXT]        = $data_arr[$i]["WISHSCHOOLCD2"];
                $data["WISHSCHOOLCD3"][TEXT]        = $data_arr[$i]["WISHSCHOOLCD3"];
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

    //実行
    public function deleteQuery($model, &$data_arr)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $cnt = 0; //処理件数
        $count = get_count($data_arr);
        for ($i=0; $i<$count; $i++) {
            $table = SHAMEXAMINATION_DAT;
            $query  = " SELECT COUNT(*) FROM ".$table ;
            $query .= "	WHERE YEAR = '".$data_arr[$i]["YEAR"]."' ";
            $query .= "	AND SHAMEXAMCD	= '".$data_arr[$i]["SHAMEXAMCD"]."' ";
            $query .= "	AND SCHREGNO	= '".$data_arr[$i]["SCHREGNO"]."' ";
            $query .= "	AND SUBCLASSCD	= '".$data_arr[$i]["SUBCLASSCD"]."' ";

            $tblcnt = $db->getOne($query);
            if ($tblcnt > 0) {
                $query  = " DELETE FROM ".$table ;
                $query .= "	WHERE YEAR = '".$data_arr[$i]["YEAR"]."' ";
                $query .= "	AND SHAMEXAMCD	= '".$data_arr[$i]["SHAMEXAMCD"]."' ";
                $query .= "	AND SCHREGNO	= '".$data_arr[$i]["SCHREGNO"]."' ";
                $query .= "	AND SUBCLASSCD	= '".$data_arr[$i]["SUBCLASSCD"]."' ";
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
        $data1["PROGRAMID"][TEXT]     = PROGRAMID;
        $data1["MSGROW"][NUMBER]      = $record_no;
        $data1["MSGREMARK"][TEXT]     = $check_error;

        $query = Query::insertSQL($data1, "W_CSVMSG_PRG_DAT");
        $result = $db->query($query);
    }

    //データ出力用CVS作成用
    public function getselectDataSQL($model)
    {
        $query  = "SELECT ";
        $query .= "    T1.YEAR, ";
        $query .= "    T2.GRADE, ";
        $query .= "    T2.HR_CLASS, ";
        $query .= "    T2.ATTENDNO, ";
        $query .= "    T1.SCHREGNO, ";
        $query .= "    T3.NAME, ";
        $query .= "    T1.SHAMEXAMCD, ";
        $query .= "    T1.SUBCLASSCD, ";
        $query .= "    T1.SUBCLASSNAME, ";
        $query .= "    T1.SUBCLASSCD_CNT, ";
        $query .= "    T1.TRADE, ";
        $query .= "    T1.SCORE, ";
        $query .= "    T1.DEVIATION, ";
        $query .= "    T1.PRECEDENCE, ";
        $query .= "    T1.SCHOOL_DEVIATION, ";
        $query .= "    T1.SCHOOL_PRECEDENCE, ";
        $query .= "    T1.WISHSCHOOLCD1, ";
        $query .= "    T1.WISHSCHOOLCD2, ";
        $query .= "    T1.WISHSCHOOLCD3, ";
        $query .= "    '".$model->lastColumn."' AS ".$model->lastColumn." ";
        $query .= "FROM ";
        $query .= "    SCHREG_REGD_DAT T2 ";
        $query .= "    LEFT JOIN SCHREG_BASE_MST T3 ON T3.SCHREGNO = T2.SCHREGNO ";
        $query .= "    LEFT JOIN SHAMEXAMINATION_DAT T1 ON T1.YEAR = T2.YEAR ";
        $query .= "    AND T1.SCHREGNO = T2.SCHREGNO ";
        $query .= "WHERE ";
        $query .= "    T2.YEAR || T2.SEMESTER = '".$model->field["YEAR"]."' ";
        if ($model->field["GRADE_HR_CLASS"]) {
            $query .= "    AND T2.GRADE || T2.HR_CLASS = '".$model->field["GRADE_HR_CLASS"]."' ";
        }

        return $query;
    }
}
