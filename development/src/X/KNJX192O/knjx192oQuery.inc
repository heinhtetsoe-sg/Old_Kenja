<?php

require_once('for_php7.php');
class knjx192oquery extends Query
{
    public function getSecurityHigh()
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     MENU_HIGH_SECURITY_MST ";
        $query .= " WHERE ";
        $query .= "     PROGRAMID = 'KNJX192O' ";
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
        $query  = " SELECT DISTINCT T1.YEAR, T1.SEMESTER, T2.SEMESTERNAME ";
        $query .= " FROM SCHREG_REGD_HDAT T1 ";
        $query .= "         INNER JOIN SEMESTER_MST T2 ON T1.YEAR = T2.YEAR AND T1.SEMESTER = T2.SEMESTER ";
        $query .= " ORDER BY T1.YEAR, T1.SEMESTER";

        return $query;
    }

    //年組一覧
    public function getSelectFieldSQL2($model)
    {
        $query  = " SELECT DISTINCT GRADE, HR_CLASS, HR_NAME ";
        $query .= " FROM SCHREG_REGD_HDAT ";
        $query .= " WHERE YEAR || SEMESTER = '".$model->field["YEAR"]."' ";
        $query .= " ORDER BY GRADE, HR_CLASS";

        return $query;
    }

    //マスタの追加（ＣＳＶデータより読込）
    public function insertQueryCsv($model, &$data_arr)
    {
        $data = array();

        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $cnt = 0; //処理件数
        for ($i = 0; $i < get_count($data_arr); $i++) {
            //データセット
            $data["SCHREGNO"][TEXT]             = $data_arr[$i]["SCHREGNO"];
            $data["JOBHUNT_REC"][TEXT]          = $data_arr[$i]["JOBHUNT_REC"];
            $data["JOBHUNT_ABSENCE"][TEXT]      = $data_arr[$i]["JOBHUNT_ABSENCE"];
            $data["JOBHUNT_HEALTHREMARK"][TEXT] = $data_arr[$i]["JOBHUNT_HEALTHREMARK"];
            $data["JOBHUNT_RECOMMEND"][TEXT]    = $data_arr[$i]["JOBHUNT_RECOMMEND"];
            $data["REGISTERCD"][TEXT]           = STAFFCD;
            $data["UPDATED"][NUMBER]            = "SYSDATE()";

            if (1 > $db->getOne("SELECT COUNT(*) FROM HEXAM_EMPREMARK_DAT WHERE SCHREGNO = '".$data_arr[$i]["SCHREGNO"]."'")) {
                $query = Query::insertSQL($data, "HEXAM_EMPREMARK_DAT");
            } else {
                $where  = "WHERE SCHREGNO = '".$data_arr[$i]["SCHREGNO"]."' ";

                $query = Query::updateSQL($data, "HEXAM_EMPREMARK_DAT", $where);
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

        $cnt = 0; //処理件数
        for ($i = 0; $i < get_count($data_arr); $i++) {
            $query  = " DELETE FROM HEXAM_EMPREMARK_DAT ";
            $query .= "	WHERE SCHREGNO = '".$data_arr[$i]["SCHREGNO"]."' ";
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
        $query  = " SELECT ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.HR_CLASS, ";
        $query .= "     T1.ATTENDNO, ";
        $query .= "     T2.NAME, ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T3.JOBHUNT_REC, ";
        $query .= "     T3.JOBHUNT_ABSENCE, ";
        $query .= "     T3.JOBHUNT_HEALTHREMARK, ";
        $query .= "     T3.JOBHUNT_RECOMMEND, ";
        $query .= "     '".$model->lastColumn."' AS ".$model->lastColumn." ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= "     LEFT JOIN SCHREG_BASE_MST T2 ON T2.SCHREGNO = T1.SCHREGNO ";
        $query .= "     LEFT JOIN HEXAM_EMPREMARK_DAT T3 ON T3.SCHREGNO = T1.SCHREGNO ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR || T1.SEMESTER = '".$model->field["YEAR"]."' ";
        if ($model->field["GRADE_HR_CLASS"] != "") {
            $query .= "     AND T1.GRADE || T1.HR_CLASS = '".$model->field["GRADE_HR_CLASS"]."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.HR_CLASS, ";
        $query .= "     T1.ATTENDNO, ";
        $query .= "     T1.SCHREGNO ";

        return $query;
    }
}