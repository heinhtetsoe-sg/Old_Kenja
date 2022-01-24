<?php

require_once('for_php7.php');
class knjx230query extends Query
{
    public function getSecurityHigh()
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     MENU_HIGH_SECURITY_MST ";
        $query .= " WHERE ";
        $query .= "     PROGRAMID = 'KNJX230' ";
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
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " AND GDAT.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind), "','")."') ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " INNER JOIN SCHREG_REGD_GDAT GDAT ON GDAT.YEAR = T1.YEAR AND GDAT.GRADE = T1.GRADE ";
            $query .= " AND GDAT.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
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
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "     AND REGD_H.YEAR        = REGD_G.YEAR ";
                $query .= "     AND REGD_H.GRADE       = REGD_G.GRADE ";
                $query .= "     AND REGD_G.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind), "','")."') ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "     AND REGD_H.YEAR        = REGD_G.YEAR ";
            $query .= "     AND REGD_H.GRADE       = REGD_G.GRADE ";
            $query .= "     AND REGD_G.SCHOOL_KIND = '" .SCHOOLKIND ."' ";
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
        $query .= "    AND NAMECD2 IN ('4', '5', '6', '2', '19', '25') ";
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
            $data["APPOINTED_DAY"][TEXT]    = $data_arr[$i]["APPOINTED_DAY"];
            if ($model->Properties["use_Attend_zero_hyoji"] == "1") {
                foreach ($model->fieldSize as $key => $val) {
                    if ($key == "APPOINTED_DAY") {
                        continue;
                    }
                    if ($key == "LESSON") {
                        $data[$key][NUMBER]     = ($data_arr[$i][$key]) ? $data_arr[$i][$key] : 0 ;
                    } else {
                        $data[$key][NUMBER]     = $data_arr[$i][$key];
                    }
                }
            } else {
                foreach ($model->fieldSize as $key => $val) {
                    if ($key == "APPOINTED_DAY") {
                        continue;
                    }
                    $data[$key][NUMBER]         = ($data_arr[$i][$key]) ? $data_arr[$i][$key] : 0 ;
                }
                if ($model->Properties["useKekkaJisu"] == 'true') {
                    $data["KEKKA_JISU"][NUMBER] = 0;
                }
                if ($model->Properties["useKekka"] == 'true') {
                    $data["KEKKA"][NUMBER]      = 0;
                }
                if ($model->Properties["useLatedetail"] == 'true') {
                    $data["LATEDETAIL"][NUMBER] = 0;
                }
            }
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][NUMBER]        = "SYSDATE()";
            if (1 > $db->getOne("SELECT COUNT(*) FROM ATTEND_SEMES_DAT WHERE COPYCD = '".$data_arr[$i]["COPYCD"]."' AND YEAR = '".$data_arr[$i]["YEAR"]."' AND MONTH = '".$data_arr[$i]["MONTH"]."' AND SEMESTER = '".$data_arr[$i]["SEMESTER"]."' AND SCHREGNO = '".$data_arr[$i]["SCHREGNO"]."'")) {
                $query = Query::insertSQL($data, "ATTEND_SEMES_DAT");
            } else {
                $where  = "WHERE COPYCD = '".$data_arr[$i]["COPYCD"]."' ";
                $where .= "      AND YEAR = '".$data_arr[$i]["YEAR"]."' ";
                $where .= "      AND MONTH = '".$data_arr[$i]["MONTH"]."' ";
                $where .= "      AND SEMESTER = '".$data_arr[$i]["SEMESTER"]."' ";
                $where .= "      AND SCHREGNO = '".$data_arr[$i]["SCHREGNO"]."' ";

                $query = Query::updateSQL($data, "ATTEND_SEMES_DAT", $where);
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
            $query  = " DELETE FROM ATTEND_SEMES_DAT ";
            $query .= " WHERE COPYCD = '".$data_arr[$i]["COPYCD"]."' ";
            $query .= "       AND YEAR = '".$data_arr[$i]["YEAR"]."' ";
            $query .= "       AND MONTH = '".$data_arr[$i]["MONTH"]."' ";
            $query .= "       AND SEMESTER = '".$data_arr[$i]["SEMESTER"]."' ";
            $query .= "       AND SCHREGNO = '".$data_arr[$i]["SCHREGNO"]."' ";
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

        $query  = " SELECT ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.HR_CLASS, ";
        $query .= "     T1.ATTENDNO, ";
        $query .= "     T2.NAME, ";
        $query .= "     T3.COPYCD, ";
        $query .= "     T3.YEAR, ";
        $query .= "     T3.MONTH, ";
        $query .= "     T3.SEMESTER, ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     ".$field." ";
        $query .= "     '".$model->lastColumn."' AS ".$model->lastColumn." ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= "     LEFT JOIN SCHREG_BASE_MST T2 ON T2.SCHREGNO = T1.SCHREGNO ";
        $query .= "     LEFT JOIN ATTEND_SEMES_DAT T3 ON T3.YEAR = T1.YEAR ";
        $query .= "     AND T3.SEMESTER = T1.SEMESTER ";
        $query .= "     AND T3.SCHREGNO = T1.SCHREGNO ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " INNER JOIN SCHREG_REGD_GDAT GDAT ON GDAT.YEAR = T1.YEAR AND GDAT.GRADE = T1.GRADE ";
                $query .= " AND GDAT.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind), "','")."') ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " INNER JOIN SCHREG_REGD_GDAT GDAT ON GDAT.YEAR = T1.YEAR AND GDAT.GRADE = T1.GRADE ";
            $query .= " AND GDAT.SCHOOL_KIND = '".SCHOOLKIND."' ";
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
        $query .= "     T3.MONTH ";

        return $query;
    }
}
