<?php

require_once('for_php7.php');
class knjz438Query extends Query
{
    public function getSecurityHigh()
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     MENU_HIGH_SECURITY_MST ";
        $query .= " WHERE ";
        $query .= "     PROGRAMID = 'KNJZ438' ";
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

    //入学年度
    public function getSelectFieldSQL()
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     T1.ENTYEAR AS VALUE, ";
        $query .= "     T1.ENTYEAR AS LABEL ";
        $query .= " FROM ";
        $query .= "     SUBCLASS_HOLD_DAT T1 ";
        $query .= " ORDER BY ";
        $query .= "     VALUE DESC ";
        return $query;
    }

    //課程・学科
    public function getSelectFieldSQL2($model)
    {
        $query  = " WITH T_SUBCLASS_HOLD AS ( ";
        $query .= "     SELECT  ";
        $query .= "         COURSECD, ";
        $query .= "         MAJORCD ";
        $query .= "     FROM ";
        $query .= "         SUBCLASS_HOLD_DAT ";
        $query .= "     WHERE ";
        $query .= "         ENTYEAR='".$model->field["ENTYEAR"]."' ";
        $query .= "     GROUP BY ";
        $query .= "         COURSECD, ";
        $query .= "         MAJORCD ";
        $query .= "     ) ";
        $query .= " SELECT  ";
        $query .= "     T1.COURSECD || T1.MAJORCD AS VALUE, ";
        $query .= "     T1.COURSECD || T1.MAJORCD || ':' || L1.COURSENAME || L2.MAJORNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     T_SUBCLASS_HOLD T1 ";
        $query .= "     LEFT JOIN COURSE_MST L1 ON L1.COURSECD = T1.COURSECD ";
        $query .= "     LEFT JOIN MAJOR_MST L2 ON L2.COURSECD = T1.COURSECD AND L2.MAJORCD = T1.MAJORCD ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";
        return $query;
    }

    public function countCoursecd($code)
    {
        $query  = "SELECT COUNT(*) FROM COURSE_MST WHERE COURSECD = '{$code}' ";
        return $query;
    }

    public function countMajorcd($code)
    {
        $query  = "SELECT COUNT(*) FROM MAJOR_MST WHERE MAJORCD = '{$code}' ";
        return $query;
    }

    public function countClasscd($code)
    {
        $query  = "SELECT COUNT(*) FROM CLASS_MST WHERE CLASSCD = '{$code}' ";
        return $query;
    }
    
    //教育課程用
    public function countSchoolKind($code)
    {
        $query  = "SELECT COUNT(*) FROM CLASS_MST WHERE SCHOOL_KIND = '{$code}' ";
        return $query;
    }
    //教育課程用
    public function countCurriculumCd($code)
    {
        $query  = "SELECT COUNT(*) FROM SUBCLASS_MST WHERE CURRICULUM_CD = '{$code}' ";
        return $query;
    }

    public function countSubclasscd($code)
    {
        $query  = "SELECT COUNT(*) FROM SUBCLASS_MST WHERE SUBCLASSCD = '{$code}' ";
        return $query;
    }

    public function countSelectkind($code)
    {
        $query  = "SELECT COUNT(*) FROM NAME_MST WHERE NAMECD1 = 'D032' AND NAMECD2 = '{$code}' ";
        return $query;
    }

    //マスタの追加（ＣＳＶデータより読込）
    public function insertQueryCsv($model, &$data_arr)
    {
        $data = array();

        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $cnt = 0;   //処理件数
        for ($i = 0; $i < get_count($data_arr); $i++) {
            //データセット
            $data["ENTYEAR"][TEXT]          = $data_arr[$i]["ENTYEAR"];
            $data["COURSECD"][TEXT]         = $data_arr[$i]["COURSECD"];
            $data["MAJORCD"][TEXT]          = $data_arr[$i]["MAJORCD"];
            $data["CLASSCD"][TEXT]          = $data_arr[$i]["CLASSCD"];
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $data["SCHOOL_KIND"][TEXT]      = $data_arr[$i]["SCHOOL_KIND"];
                $data["CURRICULUM_CD"][TEXT]    = $data_arr[$i]["CURRICULUM_CD"];
            }
            $data["SUBCLASSCD"][TEXT]       = $data_arr[$i]["SUBCLASSCD"];
            $data["SELECTKIND"][TEXT]       = $data_arr[$i]["SELECTKIND"];
            $data["STUDY1"][TEXT]           = $data_arr[$i]["STUDY1"];
            $data["STUDY2"][TEXT]           = $data_arr[$i]["STUDY2"];
            $data["STUDY3"][TEXT]           = $data_arr[$i]["STUDY3"];
            $data["STUDY4"][TEXT]           = $data_arr[$i]["STUDY4"];
            $data["STUDY5"][TEXT]           = $data_arr[$i]["STUDY5"];
            $data["STUDY6"][TEXT]           = $data_arr[$i]["STUDY6"];
            $data["CREDITS"][NUMBER]        = $data_arr[$i]["CREDITS"];
            $data["REMARK"][TEXT]           = $data_arr[$i]["REMARK"];
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][NUMBER]        = "SYSDATE()";

            $where = knjz438Query::getWhere($data_arr, $i, $model);
            if (1 > $db->getOne("SELECT COUNT(*) FROM SUBCLASS_HOLD_DAT " .$where)) {
                $query = Query::insertSQL($data, "SUBCLASS_HOLD_DAT");
            } else {
                $query = Query::updateSQL($data, "SUBCLASS_HOLD_DAT", $where);
            }
            $db->query($query);
            $cnt++;
        }
        $db->commit();
        Query::dbCheckIn($db);

        return $cnt;
    }

    //条件
    public function getWhere(&$data_arr, $i, $model)
    {
        $query  = "  WHERE ENTYEAR      = '".$data_arr[$i]["ENTYEAR"]."' ";
        $query .= "    AND COURSECD     = '".$data_arr[$i]["COURSECD"]."' ";
        $query .= "    AND MAJORCD      = '".$data_arr[$i]["MAJORCD"]."' ";
        $query .= "    AND CLASSCD      = '".$data_arr[$i]["CLASSCD"]."' ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "    AND SCHOOL_KIND   = '".$data_arr[$i]["SCHOOL_KIND"]."' ";
            $query .= "    AND CURRICULUM_CD = '".$data_arr[$i]["CURRICULUM_CD"]."' ";
        }
        $query .= "    AND SUBCLASSCD   = '".$data_arr[$i]["SUBCLASSCD"]."' ";
        return $query;
    }

    //削除（ＣＳＶデータより読込）
    public function deleteQueryCsv($model, &$data_arr)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $cnt = 0;   //処理件数
        for ($i = 0; $i < get_count($data_arr); $i++) {
            $query  = " DELETE FROM SUBCLASS_HOLD_DAT ";
            $query .= knjz438Query::getWhere($data_arr, $i, $model);
            $db->query($query);
            $cnt++;
        }
        $db->commit();
        Query::dbCheckIn($db);

        return $cnt;
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

    //CSVデータ出力
    public function selectMainQuery($model)
    {
        $query  = " SELECT ";
        $query .= "     T1.ENTYEAR, ";
        $query .= "     T1.COURSECD, ";
        $query .= "     L1.COURSENAME, ";
        $query .= "     T1.MAJORCD, ";
        $query .= "     L2.MAJORNAME, ";
        $query .= "     T1.CLASSCD, ";
        $query .= "     L4.CLASSNAME, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T1.SCHOOL_KIND, ";
            $query .= "     T1.CURRICULUM_CD, ";
        }
        $query .= "     T1.SUBCLASSCD, ";
        $query .= "     L5.SUBCLASSNAME, ";
        $query .= "     T1.SELECTKIND, ";
        $query .= "     L6.NAME1 AS SELECTKIND_NAME, ";
        $query .= "     T1.STUDY1, ";
        $query .= "     T1.STUDY2, ";
        $query .= "     T1.STUDY3, ";
        $query .= "     T1.STUDY4, ";
        $query .= "     T1.STUDY5, ";
        $query .= "     T1.STUDY6, ";
        $query .= "     T1.CREDITS, ";
        $query .= "     T1.REMARK, ";
        $query .= "     '".$modle->lastColumn."' AS ".$modle->lastColumn." ";
        $query .= " FROM ";
        $query .= "     SUBCLASS_HOLD_DAT T1 ";
        $query .= "     LEFT JOIN COURSE_MST L1 ON L1.COURSECD = T1.COURSECD ";
        $query .= "     LEFT JOIN MAJOR_MST L2 ON L2.COURSECD = T1.COURSECD AND L2.MAJORCD = T1.MAJORCD ";
        $query .= "     LEFT JOIN CLASS_MST L4 ON L4.CLASSCD = T1.CLASSCD ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                         AND L4.SCHOOL_KIND = T1.SCHOOL_KIND ";
        }
        $query .= "     LEFT JOIN SUBCLASS_MST L5 ON L5.SUBCLASSCD = T1.SUBCLASSCD ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                         AND L5.CLASSCD       = T1.CLASSCD ";
            $query .= "                         AND L5.SCHOOL_KIND   = T1.SCHOOL_KIND ";
            $query .= "                         AND L5.CURRICULUM_CD = T1.CURRICULUM_CD ";
        }
        $query .= "     LEFT JOIN NAME_MST L6 ON L6.NAMECD1 = 'D032' AND L6.NAMECD2 = T1.SELECTKIND ";
        $query .= " WHERE ";
        $query .= "         T1.ENTYEAR = '".$model->field["ENTYEAR"]."' ";
        if ($model->field["COURSE"] != "") {
            $query .= "     AND T1.COURSECD || T1.MAJORCD = '".$model->field["COURSE"]."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     T1.COURSECD, ";
        $query .= "     T1.MAJORCD, ";
        $query .= "     T1.CLASSCD, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T1.SCHOOL_KIND, ";
            $query .= "     T1.CURRICULUM_CD, ";
        }
        $query .= "     T1.SUBCLASSCD ";

        return $query;
    }
}
