<?php
class knjx442bquery extends Query
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

    public function getSchoolNm()
    {
        $query  = " SELECT ";
        $query .= "     NAME1 ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'Z010' ";
        $query .= "     AND NAMECD2 = '00' ";

        return $query;
    }

    //年度一覧 ログイン年度学期で固定
    public function getSelectFieldSQL()
    {
        $query  = "SELECT DISTINCT T1.YEAR,T1.SEMESTER,T2.SEMESTERNAME ";
        $query .= "FROM SCHREG_REGD_HDAT T1 ";
        $query .= "        INNER JOIN SEMESTER_MST T2 ON T1.YEAR=T2.YEAR AND T1.SEMESTER=T2.SEMESTER ";
        $query .= "WHERE ";
        $query .= "    T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "    AND T1.SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= "ORDER BY T1.YEAR,T1.SEMESTER";

        return $query;
    }

    //年組一覧
    public function getSelectFieldSQL2($model)
    {
        $query  = "SELECT DISTINCT T1.GRADE,T1.HR_CLASS,T1.HR_NAME ";
        $query .= "FROM SCHREG_REGD_HDAT T1 ";
        if ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "      INNER JOIN SCHREG_REGD_GDAT REG_G ON T1.YEAR = REG_G.YEAR ";
            $query .= "           AND T1.GRADE = REG_G.GRADE ";
            $query .= "           AND REG_G.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= "    , STAFF_MST T2 ";
        $query .= "WHERE ";
        $query .= "    T1.YEAR || T1.SEMESTER = '".$model->field["YEAR"]."' ";
        $query .= "    AND T1.TR_CD1 = T2.STAFFCD ";
        if ($model->auth == DEF_UPDATE_RESTRICT || $model->auth == DEF_REFER_RESTRICT) {
            $query .= " AND (T1.TR_CD1 = '". STAFFCD ."' OR";
            $query .= "      T1.TR_CD2 = '". STAFFCD ."' OR";
            $query .= "      T1.TR_CD3 = '". STAFFCD ."' OR";
            $query .= "      T1.SUBTR_CD1 = '". STAFFCD ."' OR";
            $query .= "      T1.SUBTR_CD2 = '". STAFFCD ."' OR";
            $query .= "      T1.SUBTR_CD3 = '". STAFFCD ."')";
        }
        //卒業可能学年
        $query .= " AND EXISTS (SELECT ";
        $query .= "     'X' ";
        $query .= "         FROM ";
        $query .= "             NAME_MST A023 ";
        $query .= "         WHERE ";
        $query .= "             A023.NAMECD1 = 'A023' ";
        $query .= "             AND T1.GRADE BETWEEN A023.NAMESPARE2 AND A023.NAMESPARE3) ";
        $query .= "ORDER BY T1.GRADE,T1.HR_CLASS";

        return $query;
    }

    //学籍番号チェック（ＣＳＶデータより読込）
    public function checkSchregNo($model, $schregNo)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR || SEMESTER = '".$model->field["YEAR"]."' AND ";
        $query .= "     SCHREGNO         = '{$schregNo}' ";

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
            //データセット(MOCK_TRAINREMARK_DAT)
            $data = array();
            $data["GRADE1_REMARK"][TEXT]      = $data_arr[$i]["GRADE1_REMARK"];
            $data["GRADE2_REMARK"][TEXT]      = $data_arr[$i]["GRADE2_REMARK"];
            $data["GRADE3_REMARK"][TEXT]      = $data_arr[$i]["GRADE3_REMARK"];
            $data["CONDITION"][TEXT]          = $data_arr[$i]["CONDITION"];
            $data["HOPE_COLLEGE_NAME1"][TEXT] = $data_arr[$i]["HOPE_COLLEGE_NAME1"];
            $data["HOPE_COURSE_NAME1"][TEXT]  = $data_arr[$i]["HOPE_COURSE_NAME1"];
            $data["HOPE_COLLEGE_NAME2"][TEXT] = $data_arr[$i]["HOPE_COLLEGE_NAME2"];
            $data["HOPE_COURSE_NAME2"][TEXT]  = $data_arr[$i]["HOPE_COURSE_NAME2"];
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][NUMBER]        = "SYSDATE()";
            if (1 > $db->getOne("SELECT COUNT(*) FROM MOCK_TRAINREMARK_DAT WHERE SCHREGNO = '".$data_arr[$i]["SCHREGNO"]."'")) {
                $data["SCHREGNO"][TEXT]           = $data_arr[$i]["SCHREGNO"];

                $query = Query::insertSQL($data, "MOCK_TRAINREMARK_DAT");
            } else {
                $where  = "WHERE SCHREGNO = '".$data_arr[$i]["SCHREGNO"]."' ";

                $query = Query::updateSQL($data, "MOCK_TRAINREMARK_DAT", $where);
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
        for ($i = 0; $i < count($data_arr); $i++) {
            //MOCK_TRAINREMARK_DAT
            $query  = " DELETE FROM MOCK_TRAINREMARK_DAT ";
            $query .= " WHERE SCHREGNO = '".$data_arr[$i]["SCHREGNO"]."' ";

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
        $data1 = array();
        $data1["PROGRAMID"][TEXT] = PROGRAMID;
        $data1["MSGROW"][NUMBER]  = $record_no;
        $data1["MSGREMARK"][TEXT] = $check_error;

        $query = Query::insertSQL($data1, "W_CSVMSG_PRG_DAT");

        $result = $db->query($query);
    }

    //CSVデータ出力
    public function selectMainQuery($model)
    {
        $query  = " SELECT ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.HR_CLASS, ";
        $query .= "     T1.ATTENDNO, ";
        $query .= "     T2.NAME, ";
        $query .= "     T1.YEAR, ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     L1.GRADE1_REMARK, ";
        $query .= "     L1.GRADE2_REMARK, ";
        $query .= "     L1.GRADE3_REMARK, ";
        $query .= "     L1.CONDITION, ";
        $query .= "     L1.HOPE_COLLEGE_NAME1, ";
        $query .= "     L1.HOPE_COURSE_NAME1, ";
        $query .= "     L1.HOPE_COLLEGE_NAME2, ";
        $query .= "     L1.HOPE_COURSE_NAME2, ";
        $query .= "     '".$model->lastColumn."' AS ".$model->lastColumn." ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        if ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "      INNER JOIN SCHREG_REGD_GDAT REG_G ON T1.YEAR = REG_G.YEAR ";
            $query .= "           AND T1.GRADE = REG_G.GRADE ";
            $query .= "           AND REG_G.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= "     INNER JOIN SCHREG_BASE_MST T2 ON T2.SCHREGNO = T1.SCHREGNO ";
        $query .= "     INNER JOIN SCHREG_REGD_HDAT RH ON RH.YEAR = T1.YEAR ";
        $query .= "         AND RH.SEMESTER = T1.SEMESTER ";
        $query .= "         AND RH.GRADE = T1.GRADE ";
        $query .= "         AND RH.HR_CLASS = T1.HR_CLASS ";
        $query .= "     LEFT JOIN MOCK_TRAINREMARK_DAT L1 ";
        $query .= "          ON L1.SCHREGNO = T1.SCHREGNO  ";
        $query .= "    , STAFF_MST STAFF ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR || T1.SEMESTER = '".$model->field["YEAR"]."' ";
        $query .= "     AND RH.TR_CD1 = STAFF.STAFFCD ";
        if ($model->field["GRADE_HR_CLASS"] != "") {
            $query .= "     AND T1.GRADE || T1.HR_CLASS = '".$model->field["GRADE_HR_CLASS"]."' ";
        } else {
            $query .= "     AND L1.SCHREGNO = T1.SCHREGNO ";
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
        $query .= "     T1.GRADE, ";
        $query .= "     T1.HR_CLASS, ";
        $query .= "     T1.ATTENDNO, ";
        $query .= "     T1.SCHREGNO ";

        return $query;
    }
}
