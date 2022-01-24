<?php

require_once('for_php7.php');

class knjx160bquery extends Query {

    function getSecurityHigh() {
        $query  = " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     MENU_HIGH_SECURITY_MST ";
        $query .= " WHERE ";
        $query .= "     PROGRAMID = 'KNJX160B' ";
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
        $query  = " SELECT DISTINCT ";
        $query .= "     T1.YEAR || '年度 ' || T2.SEMESTERNAME AS LABEL, ";
        $query .= "     T1.YEAR || T1.SEMESTER AS VALUE ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT T1 ";
        $query .= "     INNER JOIN SEMESTER_MST T2 ON T1.YEAR = T2.YEAR AND T1.SEMESTER = T2.SEMESTER ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //年組一覧
    function getSelectFieldSQL2($model)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     HR_NAME AS LABEL, ";
        $query .= "     GRADE || HR_CLASS AS VALUE ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR || SEMESTER = '".$model->field["YEAR"]."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;    
    }

    //名称マスタ存在チェックSQL
    function checkMstData($namecd1, $namecd2)
    {
        $query  = " SELECT COUNT(*)";
        $query .= " FROM NAME_MST";
        $query .= " WHERE NAMECD1 = '".$namecd1."' ";
        $query .= "       AND NAMECD2 = '".$namecd2."' ";

        return $query;
    }

    //マスタの追加（ＣＳＶデータより読込）
    function insertQueryCsv($model, &$data_arr)
    {
        $data = array();

		$db = Query::dbCheckOut();
		$db->autoCommit(false);

		$cnt = 0;	//処理件数
		for ($i = 0; $i < get_count($data_arr); $i++) {
            //データセット
            $data["SCHREGNO"][TEXT]                = $data_arr[$i]["SCHREGNO"];
            $data["RELATIONSHIP"][TEXT]            = $data_arr[$i]["RELATIONSHIP"];
            $data["GUARD_NAME"][TEXT]              = $data_arr[$i]["GUARD_NAME"];
            $data["GUARD_KANA"][TEXT]              = $data_arr[$i]["GUARD_KANA"];
            $data["GUARD_SEX"][TEXT]               = $data_arr[$i]["GUARD_SEX"];
            $data["GUARD_BIRTHDAY"][TEXT]          = str_replace("/", "-", $data_arr[$i]["GUARD_BIRTHDAY"]);
            $data["GUARD_ZIPCD"][TEXT]             = $data_arr[$i]["GUARD_ZIPCD"];
            $data["GUARD_ADDR1"][TEXT]             = $data_arr[$i]["GUARD_ADDR1"];
            $data["GUARD_ADDR2"][TEXT]             = $data_arr[$i]["GUARD_ADDR2"];
            $data["GUARD_TELNO"][TEXT]             = $data_arr[$i]["GUARD_TELNO"];
            $data["GUARD_FAXNO"][TEXT]             = $data_arr[$i]["GUARD_FAXNO"];
            $data["GUARD_E_MAIL"][TEXT]            = $data_arr[$i]["GUARD_E_MAIL"];
            $data["GUARD_JOBCD"][TEXT]             = $data_arr[$i]["GUARD_JOBCD"];
            $data["GUARD_WORK_NAME"][TEXT]         = $data_arr[$i]["GUARD_WORK_NAME"];
            $data["GUARD_WORK_TELNO"][TEXT]        = $data_arr[$i]["GUARD_WORK_TELNO"];
            $data["GUARANTOR_RELATIONSHIP"][TEXT]  = $data_arr[$i]["GUARANTOR_RELATIONSHIP"];
            $data["GUARANTOR_NAME"][TEXT]          = $data_arr[$i]["GUARANTOR_NAME"];
            $data["GUARANTOR_KANA"][TEXT]          = $data_arr[$i]["GUARANTOR_KANA"];
            $data["GUARANTOR_SEX"][TEXT]           = $data_arr[$i]["GUARANTOR_SEX"];
            $data["GUARANTOR_ZIPCD"][TEXT]         = $data_arr[$i]["GUARANTOR_ZIPCD"];
            $data["GUARANTOR_ADDR1"][TEXT]         = $data_arr[$i]["GUARANTOR_ADDR1"];
            $data["GUARANTOR_ADDR2"][TEXT]         = $data_arr[$i]["GUARANTOR_ADDR2"];
            $data["GUARANTOR_TELNO"][TEXT]         = $data_arr[$i]["GUARANTOR_TELNO"];
            $data["GUARANTOR_JOBCD"][TEXT]         = $data_arr[$i]["GUARANTOR_JOBCD"];
            $data["PUBLIC_OFFICE"][TEXT]           = $data_arr[$i]["PUBLIC_OFFICE"];
            $data["REGISTERCD"][TEXT]              = STAFFCD;
            $data["UPDATED"][NUMBER]               = "SYSDATE()";
            if (1 > $db->getOne("SELECT COUNT(*) FROM GUARDIAN_DAT WHERE SCHREGNO = '".$data_arr[$i]["SCHREGNO"]."'")) {
                $query = Query::insertSQL($data, "GUARDIAN_DAT");
            } else {
                $where  = "WHERE SCHREGNO = '".$data_arr[$i]["SCHREGNO"]."' ";

                $query = Query::updateSQL($data, "GUARDIAN_DAT", $where);
            }
			$db->query($query);
			$cnt++;
		}
		$db->commit();
		Query::dbCheckIn($db);

		return $cnt;

    }

    //削除（ＣＳＶデータより読込）
    function DeleteQueryCsv($model, &$data_arr)
    {
		$db = Query::dbCheckOut();
		$db->autoCommit(false);

		$cnt = 0;	//処理件数
		for ($i = 0; $i < get_count($data_arr); $i++) {
            $query	= " DELETE FROM GUARDIAN_DAT ";
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
        $query  = " DELETE FROM W_CSVMSG_DAT ";
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
        $query .= "     T3.RELATIONSHIP, ";
        $query .= "     T3.GUARD_NAME, ";
        $query .= "     T3.GUARD_KANA, ";
        $query .= "     T3.GUARD_SEX, ";
        $query .= "     T3.GUARD_BIRTHDAY, ";
        $query .= "     T3.GUARD_ZIPCD, ";
        $query .= "     T3.GUARD_ADDR1, ";
        $query .= "     T3.GUARD_ADDR2, ";
        $query .= "     T3.GUARD_TELNO, ";
        $query .= "     T3.GUARD_FAXNO, ";
        $query .= "     T3.GUARD_E_MAIL, ";
        $query .= "     T3.GUARD_JOBCD, ";
        $query .= "     T3.GUARD_WORK_NAME, ";
        $query .= "     T3.GUARD_WORK_TELNO, ";
        $query .= "     T3.GUARANTOR_RELATIONSHIP, ";
        $query .= "     T3.GUARANTOR_NAME, ";
        $query .= "     T3.GUARANTOR_KANA, ";
        $query .= "     T3.GUARANTOR_SEX, ";
        $query .= "     T3.GUARANTOR_ZIPCD, ";
        $query .= "     T3.GUARANTOR_ADDR1, ";
        $query .= "     T3.GUARANTOR_ADDR2, ";
        $query .= "     T3.GUARANTOR_TELNO, ";
        $query .= "     T3.GUARANTOR_JOBCD, ";
        $query .= "     T3.PUBLIC_OFFICE, ";
        $query .= "     'DUMMY' AS DUMMY ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= "     LEFT JOIN SCHREG_BASE_MST T2 ON T2.SCHREGNO = T1.SCHREGNO ";
        $query .= "     LEFT JOIN GUARDIAN_DAT T3 ON T3.SCHREGNO = T1.SCHREGNO ";
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
