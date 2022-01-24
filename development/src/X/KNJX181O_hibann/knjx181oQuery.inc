<?php

require_once('for_php7.php');

class knjx181oquery extends Query {

    function getSecurityHigh() {
        $query  = " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     MENU_HIGH_SECURITY_MST ";
        $query .= " WHERE ";
        $query .= "     PROGRAMID = 'KNJX181O' ";
        $query .= "     AND INVALID_FLG = '0' ";

        return $query;
    }

    function getSchoolCd() {
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
    function getSelectFieldSQL()
    {
        $query  = "SELECT DISTINCT T1.YEAR,T1.SEMESTER,T2.SEMESTERNAME ";
        $query .= "FROM SCHREG_REGD_HDAT T1 ";
        $query .= "        INNER JOIN SEMESTER_MST T2 ON T1.YEAR=T2.YEAR AND T1.SEMESTER=T2.SEMESTER ";
        $query .= "ORDER BY T1.YEAR,T1.SEMESTER";

        return $query;
    }

    //年組一覧
    function getSelectFieldSQL2($model)
    {
        $query  = "SELECT DISTINCT GRADE,HR_CLASS,HR_NAME ";
        $query .= "FROM SCHREG_REGD_HDAT ";
        $query .= "WHERE YEAR || SEMESTER = '".$model->field["YEAR"]."' ";
        $query .= "ORDER BY GRADE,HR_CLASS";

        return $query;    
    }

    //マスタの追加（ＣＳＶデータより読込）
    function insertQueryCsv($model,&$data_arr)
    {
        $data = array();

		$db = Query::dbCheckOut();
		$db->autoCommit(false);

		$cnt = 0;	//処理件数
		for ($i = 0; $i < get_count($data_arr); $i++) {
            //データセット
            $data["SCHREGNO"][TEXT]      = $data_arr[$i]["SCHREGNO"];
            $data["TOTALSTUDYACT"][TEXT] = $data_arr[$i]["TOTALSTUDYACT"];
            $data["TOTALSTUDYVAL"][TEXT] = $data_arr[$i]["TOTALSTUDYVAL"];
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
    function DeleteQueryCsv($model,&$data_arr)
    {
		$db = Query::dbCheckOut();
		$db->autoCommit(false);

		$cnt = 0;	//処理件数
		for ($i = 0; $i < get_count($data_arr); $i++) {
            $query	= " DELETE FROM HTRAINREMARK_HDAT ";
            $query .= "	WHERE SCHREGNO = '".$data_arr[$i]["SCHREGNO"]."' ";
			$db->query($query);
			$cnt++;
		}
		$db->commit();
		Query::dbCheckIn($db);

		return $cnt;
    }

    //エラーデータの削除
    function deleteQueryErr()
    {
        $query  = "DELETE FROM W_CSVMSG_DAT ";
        return $query;
    }


    //エラーＤＢへの追加
    function insertQueryErr(&$db, $record_no, $check_error)
    {

		$data1["MSGROW"][NUMBER] 		= $record_no;
		$data1["MSGREMARK"][TEXT] 		= $check_error;

		$query = Query::insertSQL($data1, "w_csvmsg_dat");

        $result = $db->query($query);

    }


    //CVSエラー作成用のQUERY
    function selectCsvErrQuery()
    {
        $query  = "select * from w_csvmsg_dat";

        return $query;
    }

    //CSVデータ出力
    function selectMainQuery($model)
    {
        $query  = " SELECT ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.HR_CLASS, ";
        $query .= "     T1.ATTENDNO, ";
        $query .= "     T2.NAME, ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T3.TOTALSTUDYACT, ";
        $query .= "     T3.TOTALSTUDYVAL, ";
        $query .= "     'DUMMY' AS DUMMY ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= "     LEFT JOIN SCHREG_BASE_MST T2 ON T2.SCHREGNO = T1.SCHREGNO ";
        $query .= "     LEFT JOIN HTRAINREMARK_HDAT T3 ON T3.SCHREGNO = T1.SCHREGNO ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR || T1.SEMESTER = '".$model->field["YEAR"]."' ";
        $query .= "     AND T3.SCHREGNO = T1.SCHREGNO ";
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
?>
