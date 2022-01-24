<?php

require_once('for_php7.php');
class knjx231query extends Query
{
    public function getSecurityHigh()
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     MENU_HIGH_SECURITY_MST ";
        $query .= " WHERE ";
        $query .= "     PROGRAMID = 'KNJX231' ";
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
    public function getSelectFieldSQL()
    {
        $query  = "SELECT DISTINCT T1.YEAR,T1.SEMESTER,T2.SEMESTERNAME ";
        $query .= "FROM SCHREG_REGD_HDAT T1 ";
        $query .= "        INNER JOIN SEMESTER_MST T2 ON T1.YEAR=T2.YEAR AND T1.SEMESTER=T2.SEMESTER ";
        $query .= "ORDER BY T1.YEAR,T1.SEMESTER";

        return $query;
    }

    //年組一覧
    public function getSelectFieldSQL2($model)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     REGD_H.GRADE,";
        $query .= "     REGD_H.HR_CLASS, ";
        $query .= "     REGD_H.HR_NAME ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT REGD_H, ";
        $query .= "     SCHREG_REGD_GDAT REGD_G ";
        $query .= " WHERE ";
        $query .= "     REGD_H.YEAR || REGD_H.SEMESTER = '".$model->field["YEAR"]."' ";
        if ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "     AND REGD_H.YEAR = REGD_G.YEAR ";
            $query .= "     AND REGD_G.SCHOOL_KIND = '" .SCHOOLKIND ."' ";
            $query .= "     AND REGD_H.GRADE = REGD_G.GRADE ";
        }
        $query .= " ORDER BY ";
        $query .= "     REGD_H.GRADE, ";
        $query .= "     REGD_H.HR_CLASS ";

        return $query;
    }

    //生徒一覧
    public function getSelectFieldSQL3($model)
    {
        $query  = "SELECT DISTINCT T1.SCHREGNO, T1.ATTENDNO, T2.NAME_SHOW ";
        $query .= "FROM SCHREG_REGD_DAT T1 ";
        $query .= "     LEFT JOIN SCHREG_BASE_MST T2 ON T1.SCHREGNO = T2.SCHREGNO ";
        $query .= "WHERE T1.YEAR || T1.SEMESTER = '".$model->field["YEAR"]."' ";
        $query .= "     AND T1.GRADE || T1.HR_CLASS = '".$model->field["GRADE_HR_CLASS"]."' ";
        $query .= "ORDER BY T1.ATTENDNO";

        return $query;
    }

    //科目一覧
    public function getSelectFieldSQL4($model)
    {
        $query  = " SELECT DISTINCT ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= " CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD AS SUBCLASSCD, ";
        } else {
            $query .= " SUBCLASSCD, ";
        }
        $query .= "     SUBCLASSNAME ";
        $query .= " FROM ";
        $query .= "     V_SUBCLASS_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".substr($model->field["YEAR"], 0, 4)."' ";
        if ($model->Properties["useSchool_KindField"] == "1") {
            $query .= " AND SCHOOL_KIND = '" .SCHOOLKIND ."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     SUBCLASSCD ";

        return $query;
    }

    //欠課種別取得
    public function getSickDiv()
    {
        $query  = "SELECT ";
        $query .= "    NAMECD2 AS VALUE, ";
        $query .= "    NAME1 AS LABEL ";
        $query .= "FROM ";
        $query .= "    V_NAME_MST ";
        $query .= "WHERE ";
        $query .= "    YEAR = '".CTRL_YEAR."' ";
        $query .= "    AND NAMECD1 = 'C001' ";
        $query .= "    AND NAMECD2 IN ('4', '5', '6','14', '2', '19', '25') ";
        $query .= "ORDER BY ";
        $query .= "    NAMECD2 ";

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
            $data["COPYCD"][TEXT]           = $data_arr[$i]["COPYCD"];
            $data["YEAR"][TEXT]             = $data_arr[$i]["YEAR"];
            $data["MONTH"][TEXT]            = $data_arr[$i]["MONTH"];
            $data["SEMESTER"][TEXT]         = $data_arr[$i]["SEMESTER"];
            $data["SCHREGNO"][TEXT]         = $data_arr[$i]["SCHREGNO"];
            $data["CLASSCD"][TEXT]          = $data_arr[$i]["CLASSCD"];
            if ($model->Properties["useCurriculumcd"] == '1') {
                $data["SCHOOL_KIND"][TEXT]      = $data_arr[$i]["SCHOOL_KIND"];
                $data["CURRICULUM_CD"][TEXT]    = $data_arr[$i]["CURRICULUM_CD"];
            }
            $data["SUBCLASSCD"][TEXT]       = $data_arr[$i]["SUBCLASSCD"];
            $data["APPOINTED_DAY"][TEXT]    = $data_arr[$i]["APPOINTED_DAY"];
            foreach ($model->fieldSize as $key => $val) {
                $data[$key][NUMBER]         = ($data_arr[$i][$key]) ? $data_arr[$i][$key] : 0 ;
            }
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][NUMBER]        = "SYSDATE()";

            //存在チェックSQL
            $check  = " SELECT ";
            $check .= "     COUNT(*) ";
            $check .= " FROM ";
            $check .= "     ATTEND_SUBCLASS_DAT ";
            $check .= " WHERE ";
            $check .= "     COPYCD      = '".$data_arr[$i]["COPYCD"]."' AND ";
            $check .= "     YEAR        = '".$data_arr[$i]["YEAR"]."' AND ";
            $check .= "     MONTH       = '".$data_arr[$i]["MONTH"]."' AND ";
            $check .= "     SEMESTER    = '".$data_arr[$i]["SEMESTER"]."' AND ";
            $check .= "     SCHREGNO    = '".$data_arr[$i]["SCHREGNO"]."' AND ";
            $check .= "     CLASSCD     = '".$data_arr[$i]["CLASSCD"]."' AND ";
            if ($model->Properties["useCurriculumcd"] == '1') {
                $check .= "     SCHOOL_KIND     = '".$data_arr[$i]["SCHOOL_KIND"]."' AND ";
                $check .= "     CURRICULUM_CD     = '".$data_arr[$i]["CURRICULUM_CD"]."' AND ";
            }
            $check .= "     SUBCLASSCD  = '".$data_arr[$i]["SUBCLASSCD"]."' ";

            if (1 > $db->getOne($check)) {
                $query = Query::insertSQL($data, "ATTEND_SUBCLASS_DAT");
            } else {
                $where  = "WHERE COPYCD = '".$data_arr[$i]["COPYCD"]."' ";
                $where .= "      AND YEAR = '".$data_arr[$i]["YEAR"]."' ";
                $where .= "      AND MONTH = '".$data_arr[$i]["MONTH"]."' ";
                $where .= "      AND SEMESTER = '".$data_arr[$i]["SEMESTER"]."' ";
                $where .= "      AND SCHREGNO = '".$data_arr[$i]["SCHREGNO"]."' ";
                $where .= "      AND CLASSCD = '".$data_arr[$i]["CLASSCD"]."' ";
                if ($model->Properties["useCurriculumcd"] == '1') {
                    $where .= "      AND SCHOOL_KIND = '".$data_arr[$i]["SCHOOL_KIND"]."' ";
                    $where .= "      AND CURRICULUM_CD = '".$data_arr[$i]["CURRICULUM_CD"]."' ";
                }
                $where .= "      AND SUBCLASSCD = '".$data_arr[$i]["SUBCLASSCD"]."' ";

                $query = Query::updateSQL($data, "ATTEND_SUBCLASS_DAT", $where);
            }
            $db->query($query);
            $cnt++;
        }
        $db->commit();
        Query::dbCheckIn($db);

        return $cnt;
    }

    //削除（ＣＳＶデータより読込）
    public function deleteQueryCsv($model, &$data_arr)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $cnt = 0;   //処理件数
        for ($i = 0; $i < get_count($data_arr); $i++) {
            $query  = " DELETE FROM ATTEND_SUBCLASS_DAT ";
            $query .= " WHERE COPYCD = '".$data_arr[$i]["COPYCD"]."' ";
            $query .= "       AND YEAR = '".$data_arr[$i]["YEAR"]."' ";
            $query .= "       AND MONTH = '".$data_arr[$i]["MONTH"]."' ";
            $query .= "       AND SEMESTER = '".$data_arr[$i]["SEMESTER"]."' ";
            $query .= "       AND SCHREGNO = '".$data_arr[$i]["SCHREGNO"]."' ";
            $query .= "       AND CLASSCD = '".$data_arr[$i]["CLASSCD"]."' ";
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "       AND SCHOOL_KIND = '".$data_arr[$i]["SCHOOL_KIND"]."' ";
                $query .= "       AND CURRICULUM_CD = '".$data_arr[$i]["CURRICULUM_CD"]."' ";
            }
            $query .= "       AND SUBCLASSCD = '".$data_arr[$i]["SUBCLASSCD"]."' ";
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
        $data1["PROGRAMID"][TEXT] = PROGRAMID;
        $data1["MSGROW"][NUMBER]  = $record_no;
        $data1["MSGREMARK"][TEXT] = $check_error;

        $query = Query::insertSQL($data1, "W_CSVMSG_PRG_DAT");

        $result = $db->query($query);
    }

    //CSVデータ出力
    public function selectMainQuery($model)
    {
        $field = "";
        foreach ($model->fieldSize as $key => $val) {
            $field .= "T3.".$key.",";
        }

        $query .= " SELECT ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.HR_CLASS, ";
        $query .= "     T1.ATTENDNO, ";
        $query .= "     T2.NAME, ";
        $query .= "     T4.SUBCLASSNAME, ";
        $query .= "     T3.COPYCD, ";
        $query .= "     T3.YEAR, ";
        $query .= "     T3.MONTH, ";
        $query .= "     T3.SEMESTER, ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T3.CLASSCD, ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T3.SCHOOL_KIND, ";
            $query .= "     T3.CURRICULUM_CD, ";
        }
        $query .= "     T3.SUBCLASSCD, ";
        $query .= "     ".$field." ";
        $query .= "     '".$model->lastColumn."' AS ".$model->lastColumn." ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= "     LEFT JOIN SCHREG_BASE_MST T2 ON T2.SCHREGNO = T1.SCHREGNO ";
        $query .= "     LEFT JOIN ATTEND_SUBCLASS_DAT T3 ON T3.YEAR = T1.YEAR ";
        $query .= "     AND T3.SEMESTER = T1.SEMESTER ";
        $query .= "     AND T3.SCHREGNO = T1.SCHREGNO ";
        if ($model->field["SUBCLASS"] != "") {
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "     AND T3.CLASSCD || '-' || T3.SCHOOL_KIND || '-' || T3.CURRICULUM_CD || '-' || T3.SUBCLASSCD = '".$model->field["SUBCLASS"]."' ";
            } else {
                $query .= "     AND T3.SUBCLASSCD = '".$model->field["SUBCLASS"]."' ";
            }
        }
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     LEFT JOIN SUBCLASS_MST T4 ON T4.CLASSCD         = T3.CLASSCD ";
            $query .= "                              AND T4.SCHOOL_KIND     = T3.SCHOOL_KIND ";
            $query .= "                              AND T4.CURRICULUM_CD   = T3.CURRICULUM_CD ";
            $query .= "                              AND T4.SUBCLASSCD      = T3.SUBCLASSCD ";
        } else {
            $query .= "     LEFT JOIN SUBCLASS_MST T4 ON T4.SUBCLASSCD = T3.SUBCLASSCD ";
        }
        $query .= "     INNER JOIN SCHREG_REGD_GDAT T5 ON T1.GRADE = T5.GRADE ";
        $query .= "     AND T1.YEAR = T5.YEAR ";
        if ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "     AND T5.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " WHERE ";
        $query .= "     T1.YEAR || T1.SEMESTER = '".$model->field["YEAR"]."' ";
        $query .= "     AND T3.SCHREGNO = T1.SCHREGNO ";
        if ($model->field["GRADE_HR_CLASS"] != "") {
            $query .= "     AND T1.GRADE || T1.HR_CLASS = '".$model->field["GRADE_HR_CLASS"]."' ";
        }
        if ($model->field["STUDENT"] != "") {
            $query .= "     AND T1.SCHREGNO = '".$model->field["STUDENT"]."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.HR_CLASS, ";
        $query .= "     T1.ATTENDNO, ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T3.CLASSCD, ";
            $query .= "     T3.SCHOOL_KIND, ";
            $query .= "     T3.CURRICULUM_CD, ";
        }
        $query .= "     T3.SUBCLASSCD, ";
        $query .= "     T3.MONTH ";

        return $query;
    }

    //学校種別取得
    public function getSchoolKind()
    {
        $query  = " SELECT ";
        $query .= "     NAME1, ";
        $query .= "     ABBV1 ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'A023' ";
        if ($model->Properties["useSchool_KindField"] == "1") {
            $query .= " AND NAME1 = '".SCHOOLKIND."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     NAME1 ASC ";

        return $query;
    }

    //教育課程取得
    public function getCurriculumCd()
    {
        $query  = " SELECT ";
        $query .= "     NAMECD2 ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'Z018' ";

        return $query;
    }
}
