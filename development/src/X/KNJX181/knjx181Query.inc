<?php

require_once('for_php7.php');
class knjx181query extends Query
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

    //名称マスタより東京仕様かどうか確認
    public function getNamecd($cd, $value)
    {
        $query  = " SELECT ";
        $query .= "     NAME1 ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "         NAMECD1 = '".$cd."' ";
        $query .= "     AND NAMECD2 = '".$value."' ";

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
        $query  = " SELECT DISTINCT GRADE,HR_CLASS,HR_NAME ";
        $query .= " FROM SCHREG_REGD_HDAT ";
        $query .= " WHERE YEAR || SEMESTER = '".$model->field["YEAR"]."' ";
        if ($model->auth == DEF_UPDATE_RESTRICT || $model->auth == DEF_REFER_RESTRICT) {
            $query .= " AND (TR_CD1 = '". STAFFCD ."' OR";
            $query .= "      TR_CD2 = '". STAFFCD ."' OR";
            $query .= "      TR_CD3 = '". STAFFCD ."' OR";
            $query .= "      SUBTR_CD1 = '". STAFFCD ."' OR";
            $query .= "      SUBTR_CD2 = '". STAFFCD ."' OR";
            $query .= "      SUBTR_CD3 = '". STAFFCD ."')";
        }
        $query .= " ORDER BY GRADE,HR_CLASS";

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
            $data["SCHREGNO"][TEXT]      = $data_arr[$i]["SCHREGNO"];
            $data["TOTALSTUDYACT"][TEXT] = $data_arr[$i]["TOTALSTUDYACT"];
            $data["TOTALSTUDYVAL"][TEXT] = $data_arr[$i]["TOTALSTUDYVAL"];
            if ($model->getname === 'tokyoto') {
                $data["TOTALSTUDYACT2"][TEXT] = $data_arr[$i]["TOTALSTUDYACT2"];
                $data["TOTALSTUDYVAL2"][TEXT] = $data_arr[$i]["TOTALSTUDYVAL2"];
                $data["CREDITREMARK"][TEXT]   = $data_arr[$i]["CREDITREMARK"];
            }
            $data["REGISTERCD"][TEXT]    = STAFFCD;
            $data["UPDATED"][NUMBER]     = "SYSDATE()";
            if (1 > $db->getOne("SELECT COUNT(*) FROM HTRAINREMARK_HDAT WHERE SCHREGNO = '".$data_arr[$i]["SCHREGNO"]."'")) {
                $query = Query::insertSQL($data, "HTRAINREMARK_HDAT");
            } else {
                $where  = "WHERE SCHREGNO = '".$data_arr[$i]["SCHREGNO"]."' ";

                $query = Query::updateSQL($data, "HTRAINREMARK_HDAT", $where);
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
            $query  = " DELETE FROM HTRAINREMARK_HDAT ";
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
        $query .= "     T3.TOTALSTUDYACT, ";
        $query .= "     T3.TOTALSTUDYVAL, ";
        if ($model->getname === 'tokyoto') {
            $query .= "     T3.TOTALSTUDYACT2, ";
            $query .= "     T3.TOTALSTUDYVAL2, ";
            $query .= "     T3.CREDITREMARK, ";
        }
        $query .= "     '".$model->lastColumn."' AS ".$model->lastColumn." ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= "     LEFT JOIN SCHREG_BASE_MST T2 ON T2.SCHREGNO = T1.SCHREGNO ";
        $query .= "     LEFT JOIN HTRAINREMARK_HDAT T3 ON T3.SCHREGNO = T1.SCHREGNO ";
        $query .= "     LEFT JOIN SCHREG_REGD_HDAT T4 ON T4.YEAR        = T1.YEAR ";
        $query .= "                                  AND T4.SEMESTER    = T1.SEMESTER ";
        $query .= "                                  AND T4.GRADE       = T1.GRADE ";
        $query .= "                                  AND T4.HR_CLASS    = T1.HR_CLASS ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR || T1.SEMESTER = '".$model->field["YEAR"]."' ";
        if ($model->field["GRADE_HR_CLASS"] != "") {
            $query .= "     AND T1.GRADE || T1.HR_CLASS = '".$model->field["GRADE_HR_CLASS"]."' ";
        }
        if ($model->auth == DEF_UPDATE_RESTRICT || $model->auth == DEF_REFER_RESTRICT) {
            $query .= "     AND (T4.TR_CD1 = '". STAFFCD ."' OR";
            $query .= "          T4.TR_CD2 = '". STAFFCD ."' OR";
            $query .= "          T4.TR_CD3 = '". STAFFCD ."' OR";
            $query .= "          T4.SUBTR_CD1 = '". STAFFCD ."' OR";
            $query .= "          T4.SUBTR_CD2 = '". STAFFCD ."' OR";
            $query .= "          T4.SUBTR_CD3 = '". STAFFCD ."')";
        }
        $query .= " ORDER BY ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.HR_CLASS, ";
        $query .= "     T1.ATTENDNO, ";
        $query .= "     T1.SCHREGNO ";

        return $query;
    }
}
