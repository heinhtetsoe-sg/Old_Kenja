<?php

require_once('for_php7.php');
class knjx_a129query extends Query
{
    public function getSecurityHigh($model)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     MENU_HIGH_SECURITY_MST ";
        $query .= " WHERE ";
        $query .= "     PROGRAMID = '{$model->getPrgId}' ";
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

    //校種
    public function getSchoolKind($model)
    {
        $db = Query::dbCheckOut();

        $query  = " SELECT GDAT.SCHOOL_KIND ";
        $query .= " FROM SCHREG_REGD_GDAT GDAT ";
        $query .= " INNER JOIN (SELECT YEAR, MAX(GRADE) AS GRADE ";
        $query .= "             FROM SCHREG_REGD_GDAT ";
        $query .= "             GROUP BY YEAR ";
        $query .= "            ) MAX ON MAX.YEAR = GDAT.YEAR ";
        $query .= "                 AND MAX.GRADE = GDAT.GRADE ";
        $query .= " WHERE ";
        $query .= "    GDAT.YEAR = '{$model->field["YEAR"]}' ";

        $schoolKind = $db->getOne($query);

        Query::dbCheckIn($db);
        return $schoolKind;
    }

    //年度一覧
    public function getSelectFieldSQL()
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     T1.YEAR, T1.SEMESTER, T2.SEMESTERNAME ";
        $query .= " FROM SCHREG_REGD_HDAT T1 ";
        $query .= " INNER JOIN SEMESTER_MST T2 ON T1.YEAR = T2.YEAR AND T1.SEMESTER = T2.SEMESTER ";
        $query .= " ORDER BY ";
        $query .= "     T1.YEAR, T1.SEMESTER";

        return $query;
    }

    //年組一覧
    public function getSelectFieldSQL2($model)
    {
        $query  = "SELECT DISTINCT ";
        $query .= "    T1.GRADE, T1.HR_CLASS, T1.HR_NAME ";
        $query .= "FROM SCHREG_REGD_HDAT T1 ";
        if ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "      INNER JOIN SCHREG_REGD_GDAT GDAT ON T1.YEAR = GDAT.YEAR ";
            $query .= "           AND T1.GRADE = GDAT.GRADE ";
            $query .= "           AND GDAT.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= "WHERE T1.YEAR || T1.SEMESTER = '".$model->field["YEAR"]."' ";
        if ($model->auth == DEF_UPDATE_RESTRICT || $model->auth == DEF_REFER_RESTRICT) {
            $query .= " AND (T1.TR_CD1 = '". STAFFCD ."' OR";
            $query .= "      T1.TR_CD2 = '". STAFFCD ."' OR";
            $query .= "      T1.TR_CD3 = '". STAFFCD ."' OR";
            $query .= "      T1.SUBTR_CD1 = '". STAFFCD ."' OR";
            $query .= "      T1.SUBTR_CD2 = '". STAFFCD ."' OR";
            $query .= "      T1.SUBTR_CD3 = '". STAFFCD ."')";
        }
        $query .= "ORDER BY T1.GRADE, T1.HR_CLASS ";

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
            //データセット(HTRAINREMARK_ONLINE_DAT)
            $data = array();
            $data["YEAR"][TEXT]             = $data_arr[$i]["YEAR"];
            $data["SCHREGNO"][TEXT]         = $data_arr[$i]["SCHREGNO"];
            $data["ANNUAL"][TEXT]           = $data_arr[$i]["ANNUAL"];
            foreach ($model->fieldSize as $key => $val) {
                $data[$key][TEXT]    = $data_arr[$i][$key];
            }
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][NUMBER]        = "SYSDATE()";
            $chkSql = "SELECT COUNT(*) FROM HTRAINREMARK_ONLINE_DAT WHERE YEAR = '".$data_arr[$i]["YEAR"]."' AND SCHREGNO = '".$data_arr[$i]["SCHREGNO"]."'";
            if (1 > $db->getOne($chkSql)) {
                $query = Query::insertSQL($data, "HTRAINREMARK_ONLINE_DAT");
            } else {
                $where  = "WHERE YEAR     = '".$data_arr[$i]["YEAR"]."' ";
                $where .= "  AND SCHREGNO = '".$data_arr[$i]["SCHREGNO"]."' ";

                $query = Query::updateSQL($data, "HTRAINREMARK_ONLINE_DAT", $where);
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
            //HTRAINREMARK_ONLINE_DAT
            $query  = " DELETE FROM HTRAINREMARK_ONLINE_DAT ";
            $query .= " WHERE SCHREGNO = '".$data_arr[$i]["SCHREGNO"]."' ";
            $query .= "       AND YEAR = '".$data_arr[$i]["YEAR"]."' ";
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

    public function knjx_a129Query::zenkakuNumToHankakuNum($s)
    {
        $s2 = "";
        foreach (str_split($s) as $c) {
            if ($c == "０") {
                $s2 .= "0";
            } elseif ($c == "１") {
                $s2 .= "1";
            } elseif ($c == "２") {
                $s2 .= "2";
            } elseif ($c == "３") {
                $s2 .= "3";
            } elseif ($c == "４") {
                $s2 .= "4";
            } elseif ($c == "５") {
                $s2 .= "5";
            } elseif ($c == "６") {
                $s2 .= "6";
            } elseif ($c == "７") {
                $s2 .= "7";
            } elseif ($c == "８") {
                $s2 .= "8";
            } elseif ($c == "９") {
                $s2 .= "9";
            } else {
                $s2 .= $c;
            }
        }
        return $s2;
    }

    //CSVデータ出力
    public function selectMainQuery($model)
    {
        $field = "";
        foreach ($model->fieldSize as $key => $val) {
            $field .= "T1.".$key.", ";
        }

        $query  = " SELECT ";
        $query .= "     REGD.GRADE, ";
        $query .= "     REGD.HR_CLASS, ";
        $query .= "     REGD.ATTENDNO, ";
        $query .= "     BASE.NAME, ";
        $query .= "     T1.YEAR, ";
        $query .= "     REGD.SCHREGNO, ";
        $query .= "     T1.ANNUAL, ";
        $query .= "     ".$field." ";
        $query .= "     '".$model->lastColumn."' AS ".$model->lastColumn." ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT REGD ";
        if ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "      INNER JOIN SCHREG_REGD_GDAT REG_G ";
            $query .= "            ON REGD.YEAR = REG_G.YEAR ";
            $query .= "           AND REGD.GRADE = REG_G.GRADE ";
            $query .= "           AND REG_G.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= "     LEFT JOIN SCHREG_REGD_HDAT RH ";
        $query .= "          ON RH.YEAR = REGD.YEAR ";
        $query .= "         AND RH.SEMESTER = REGD.SEMESTER ";
        $query .= "         AND RH.GRADE = REGD.GRADE ";
        $query .= "         AND RH.HR_CLASS = REGD.HR_CLASS ";
        $query .= "     LEFT JOIN SCHREG_BASE_MST BASE ";
        $query .= "          ON BASE.SCHREGNO = REGD.SCHREGNO ";
        $query .= "     LEFT JOIN HTRAINREMARK_ONLINE_DAT T1 ";
        $query .= "          ON T1.YEAR = REGD.YEAR ";
        $query .= "         AND T1.SCHREGNO = REGD.SCHREGNO ";
        $query .= " WHERE ";
        $query .= "     REGD.YEAR || REGD.SEMESTER = '".$model->field["YEAR"]."' ";
        if ($model->field["GRADE_HR_CLASS"] != "") {
            $query .= "     AND REGD.GRADE || REGD.HR_CLASS = '".$model->field["GRADE_HR_CLASS"]."' ";
        } else {
            $query .= "     AND T1.SCHREGNO = REGD.SCHREGNO ";
        }
        if ($model->auth == DEF_UPDATE_RESTRICT || $model->auth == DEF_REFER_RESTRICT) {
            $query .= " AND (RH.TR_CD1 = '". STAFFCD ."' OR";
            $query .= "      RH.TR_CD2 = '". STAFFCD ."' OR";
            $query .= "      RH.TR_CD3 = '". STAFFCD ."' OR";
            $query .= "      RH.SUBTR_CD1 = '". STAFFCD ."' OR";
            $query .= "      RH.SUBTR_CD2 = '". STAFFCD ."' OR";
            $query .= "      RH.SUBTR_CD3 = '". STAFFCD ."')";
        }
        $query .= " ORDER BY ";
        $query .= "     REGD.GRADE, ";
        $query .= "     REGD.HR_CLASS, ";
        $query .= "     REGD.ATTENDNO, ";
        $query .= "     REGD.SCHREGNO ";

        return $query;
    }
}
