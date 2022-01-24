<?php

require_once('for_php7.php');

class knjl014tquery extends Query {
    //入試制度
    function getApplicantdiv($model) {
        $query  = " SELECT DISTINCT ";
        $query .= "     L1.NAMECD2 || ' ' || L1.NAME1 AS LABEL, ";
        $query .= "     L1.NAMECD2 AS VALUE, ";
        $query .= "     L1.NAMESPARE2 ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DAT T1 ";
        $query .= " LEFT JOIN ";
        $query .= "     NAME_MST L1 ON  L1.NAMECD2 = T1.APPLICANTDIV ";
        $query .= "                 AND L1.NAMECD1 = 'L003' ";
        $query .= " WHERE ";
        $query .= "     T1.ENTEXAMYEAR = '{$model->test_year}' ";
        $query .= " ORDER BY ";
        $query .= "     L1.NAMESPARE2, ";
        $query .= "     L1.NAMECD2 ";

        return $query;
    }

    //マスタの追加（ＣＳＶデータより読込）
    function udpateQueryCsv($model,&$data_arr) {
        $data = array();
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $result = $db->query(knjl014tQuery::get_calendarno($model->test_year));
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $eracd[]    = $row["NAMECD2"];
            $defyear[]  = $row["NAMESPARE1"];
            $defsdate[] = $row["NAMESPARE2"];
            $defedate[] = $row["NAMESPARE3"];
        }

        $result->free();

        $cnt = 0;   //処理件数
        for ($i = 0; $i < get_count($data_arr); $i++) {
            //誕生日用
            $birth_eracd = $birth_y = $birth_m = $birth_d = "";
            if (strlen($data_arr[$i]["BIRTHDAY"])) {
                list($s_year, $birth_m, $birth_d) = explode("/", $data_arr[$i]["BIRTHDAY"]);
                for ($ere_no=0; $ere_no<get_count($eracd); $ere_no++) {
                    if ($defsdate[$ere_no] <= $data_arr[$i]["BIRTHDAY"] && $data_arr[$i]["BIRTHDAY"] <= $defedate[$ere_no]) {
                        $birth_eracd = $eracd[$ere_no];
                        $birth_y = ((int)$s_year + 1) - ((int)$defyear[$ere_no]);
                        $birth_y = sprintf("%02d",$birth_y);
                        break;
                    }
                }
            } else {
                $birth_eracd = "";
                $birth_y = "";
                $birth_m = "";
                $birth_d = "";
            }

            //卒業日用
            $fs_eracd = $fs_y = $fs_m = $fs_day = "";
            if (strlen($data_arr[$i]["FS_DAY"])) {
                list($fs_grdyear, $fs_m, $fs_day) = explode("/", $data_arr[$i]["FS_DAY"]);
                for ($ere_no=0; $ere_no<get_count($eracd); $ere_no++) {
                    if ($defsdate[$ere_no] <= $data_arr[$i]["FS_DAY"] && $data_arr[$i]["FS_DAY"] <= $defedate[$ere_no]) {
                        $fs_eracd = $eracd[$ere_no];
                        $fs_y = ((int)$fs_grdyear + 1) - ((int)$defyear[$ere_no]);
                        $fs_y = sprintf("%02d",$fs_y);
                        break;
                    }
                }
            } else {
                $fs_grdyear = "";
                $fs_eracd = "";
                $fs_y = "";
                $fs_m = "";
                $fs_day = "";
            }

            $base_cnt = $db->getOne(knjl014tQuery::cntEntexamApplicantbaseDat($model, $data_arr[$i]));
            $addr_cnt = $db->getOne(knjl014tQuery::cntEntexamApplicantaddrDat($model, $data_arr[$i]));

            $finschool = $db->getRow(knjl014tQuery::getFinschoolMst($data_arr[$i]["FS_CD"]), DB_FETCHMODE_ASSOC);

            $data = array();
            $data["ENTEXAMYEAR"][TEXT]   = $model->test_year;
            $data["APPLICANTDIV"][TEXT]  = $data_arr[$i]["APPLICANTDIV"];
            $data["EXAMNO"][TEXT]        = $data_arr[$i]["EXAMNO"];
            $data["TESTDIV"][TEXT]       = '1';
            $data["SHDIV"][TEXT]         = '1';
            $data["DESIREDIV"][TEXT]     = '1';
            $data["NAME"][TEXT]          = $data_arr[$i]["NAME"];
            $data["NAME_KANA"][TEXT]     = $data_arr[$i]["NAME_KANA"];
            $data["SEX"][TEXT]           = $data_arr[$i]["SEX"];
            $data["ERACD"][TEXT]         = $birth_eracd;
            $data["BIRTH_Y"][TEXT]       = $birth_y;
            $data["BIRTH_M"][TEXT]       = $birth_m;
            $data["BIRTH_D"][TEXT]       = $birth_d;
            $data["BIRTHDAY"][TEXT]      = str_replace("/", "-", $data_arr[$i]["BIRTHDAY"]);
            $data["FS_CD"][TEXT]         = $data_arr[$i]["FS_CD"];
            if (strlen($finschool["FINSCHOOL_NAME"]) > 45) {
                $data["FS_NAME"][TEXT]       = substr($finschool["FINSCHOOL_NAME"],0,45);
            } else {
                $data["FS_NAME"][TEXT]       = $finschool["FINSCHOOL_NAME"];
            }
            $data["FS_GRDYEAR"][TEXT]    = $fs_grdyear;
            $data["FS_ERACD"][TEXT]      = $fs_eracd;
            $data["FS_Y"][TEXT]          = $fs_y;
            $data["FS_M"][TEXT]          = $fs_m;
            $data["FS_DAY"][TEXT]        = str_replace("/", "-", $data_arr[$i]["FS_DAY"]);
            $data["FS_GRDDIV"][TEXT]     = $data_arr[$i]["FS_GRDDIV"];

            $data["REGISTERCD"][TEXT]    = STAFFCD;
            $data["UPDATED"][NUMBER]     = "SYSDATE()";


            $where  = " WHERE ENTEXAMYEAR  = '{$model->test_year}' AND";
            $where .= "       APPLICANTDIV = '{$data_arr[$i]["APPLICANTDIV"]}' AND";
            $where .= "       EXAMNO       = '{$data_arr[$i]["EXAMNO"]}' ";

            if (0 == $base_cnt) {
                $query = Query::insertSQL($data, "ENTEXAM_APPLICANTBASE_DAT");
            } else {
                $query = Query::updateSQL($data, "ENTEXAM_APPLICANTBASE_DAT", $where);
            }

            $db->query($query);

            $data = array();
            $data["ENTEXAMYEAR"][TEXT]   = $model->test_year;
            $data["EXAMNO"][TEXT]        = $data_arr[$i]["EXAMNO"];
            $data["ZIPCD"][TEXT]         = $data_arr[$i]["ZIPCD"];
            $data["ADDRESS1"][TEXT]      = $data_arr[$i]["ADDRESS1"];
            $data["ADDRESS2"][TEXT]      = $data_arr[$i]["ADDRESS2"];
            $data["GNAME"][TEXT]         = $data_arr[$i]["GNAME"];
            $data["GTELNO"][TEXT]        = $data_arr[$i]["GTELNO"];

            $data["REGISTERCD"][TEXT]    = STAFFCD;
            $data["UPDATED"][NUMBER]     = "SYSDATE()";

            $where  = " WHERE ENTEXAMYEAR  = '{$model->test_year}' AND";
            $where .= "       EXAMNO       = '{$data_arr[$i]["EXAMNO"]}' ";

            if (0 == $addr_cnt) {
                $query = Query::insertSQL($data, "ENTEXAM_APPLICANTADDR_DAT");
            } else {
                $query = Query::updateSQL($data, "ENTEXAM_APPLICANTADDR_DAT", $where);
            }
            $db->query($query);


            $query  = " DELETE FROM ";
            $query .= "     ENTEXAM_APPLICANTWISH_DAT ";
            $query .= " WHERE ";
            $query .= "     ENTEXAMYEAR = '{$model->test_year}' AND ";
            $query .= "     EXAMNO = '{$data_arr[$i]["EXAMNO"]}' ";
            $db->query($query);

            for ($wish_no = 1; $wish_no < 7; $wish_no++) {
                if (strlen($data_arr[$i]["WISHNO".$wish_no])) {
                    $data = array();
                    $data["ENTEXAMYEAR"][TEXT]   = $model->test_year;
                    $data["EXAMNO"][TEXT]        = $data_arr[$i]["EXAMNO"];
                    $data["WISHNO"][TEXT]        = $wish_no;
                    $data["MAJORLCD"][TEXT]      = substr($data_arr[$i]["WISHNO".$wish_no], 0, 2);
                    $data["MAJORSCD"][TEXT]      = substr($data_arr[$i]["WISHNO".$wish_no], 2, 1);

                    $data["REGISTERCD"][TEXT]    = STAFFCD;
                    $data["UPDATED"][NUMBER]     = "SYSDATE()";

                    $query = Query::insertSQL($data, "ENTEXAM_APPLICANTWISH_DAT");
                    $db->query($query);
                }
            }

            $cnt++;
        }
        $db->commit();
        Query::dbCheckIn($db);

        return $cnt;
    }

    //エラーデータの削除
    function deleteQueryErr() {
        $query  = "DELETE FROM W_CSVMSG_DAT ";
        return $query;
    }

    //エラーＤＢへの追加
    function insertQueryErr(&$db, $record_no, $check_error) {
        $data1["MSGROW"][NUMBER]        = $record_no;
        $data1["MSGREMARK"][TEXT]       = $check_error;

        $query = Query::insertSQL($data1, "w_csvmsg_dat");

        $result = $db->query($query);
    }

    //CVSエラー作成用のQUERY
    function selectCsvErrQuery() {
        $query  = "SELECT * FROM W_CSVMSG_DAT";
        return $query;
    }

    //CSVデータ出力
    function selectMainQuery($model) {
        $query  = " SELECT ";
        $query .= "     T1.APPLICANTDIV, ";
        $query .= "     T1.EXAMNO, ";
        $query .= "     T1.NAME, ";
        $query .= "     T1.NAME_KANA, ";
        $query .= "     T1.BIRTHDAY, ";
        $query .= "     T1.SEX, ";
        $query .= "     T1.FS_CD, ";
        $query .= "     T1.FS_DAY, ";
        $query .= "     T1.FS_GRDDIV, ";
        $query .= "     L1.ZIPCD, ";
        $query .= "     L1.ADDRESS1, ";
        $query .= "     L1.ADDRESS2, ";
        $query .= "     L1.GTELNO, ";
        $query .= "     L1.GNAME, ";
        $query .= "     M1.MAJORCD AS MAJORCD1, ";
        $query .= "     M2.MAJORCD AS MAJORCD2, ";
        $query .= "     M3.MAJORCD AS MAJORCD3, ";
        $query .= "     M4.MAJORCD AS MAJORCD4, ";
        $query .= "     M5.MAJORCD AS MAJORCD5, ";
        $query .= "     M6.MAJORCD AS MAJORCD6 ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DAT T1 ";
        $query .= " LEFT JOIN ";
        $query .= "     ENTEXAM_APPLICANTADDR_DAT L1 ON  L1.ENTEXAMYEAR = T1.ENTEXAMYEAR ";
        $query .= "                                  AND L1.EXAMNO = T1.EXAMNO ";
        $query .= " LEFT JOIN ";
        $query .= "     ENTEXAM_APPLICANTWISH_DAT W1 ON  W1.ENTEXAMYEAR = T1.ENTEXAMYEAR ";
        $query .= "                                  AND W1.EXAMNO = T1.EXAMNO ";
        $query .= "                                  AND W1.WISHNO = '1' ";
        $query .= " LEFT JOIN ";
        $query .= "     ENTEXAM_APPLICANTWISH_DAT W2 ON  W2.ENTEXAMYEAR = T1.ENTEXAMYEAR ";
        $query .= "                                  AND W2.EXAMNO = T1.EXAMNO ";
        $query .= "                                  AND W2.WISHNO = '2' ";
        $query .= " LEFT JOIN ";
        $query .= "     ENTEXAM_APPLICANTWISH_DAT W3 ON  W3.ENTEXAMYEAR = T1.ENTEXAMYEAR ";
        $query .= "                                  AND W3.EXAMNO = T1.EXAMNO ";
        $query .= "                                  AND W3.WISHNO = '3' ";
        $query .= " LEFT JOIN ";
        $query .= "     ENTEXAM_APPLICANTWISH_DAT W4 ON  W4.ENTEXAMYEAR = T1.ENTEXAMYEAR ";
        $query .= "                                  AND W4.EXAMNO = T1.EXAMNO ";
        $query .= "                                  AND W4.WISHNO = '4' ";
        $query .= " LEFT JOIN ";
        $query .= "     ENTEXAM_APPLICANTWISH_DAT W5 ON  W5.ENTEXAMYEAR = T1.ENTEXAMYEAR ";
        $query .= "                                  AND W5.EXAMNO = T1.EXAMNO ";
        $query .= "                                  AND W5.WISHNO = '5' ";
        $query .= " LEFT JOIN ";
        $query .= "     ENTEXAM_APPLICANTWISH_DAT W6 ON  W6.ENTEXAMYEAR = T1.ENTEXAMYEAR ";
        $query .= "                                  AND W6.EXAMNO = T1.EXAMNO ";
        $query .= "                                  AND W6.WISHNO = '6' ";
        $query .= " LEFT JOIN ";
        $query .= "     V_ENTEXAM_MAJOR_MST M1 ON M1.MAJORCD = W1.MAJORLCD || W1.MAJORSCD ";
        $query .= " LEFT JOIN ";
        $query .= "     V_ENTEXAM_MAJOR_MST M2 ON M2.MAJORCD = W2.MAJORLCD || W2.MAJORSCD ";
        $query .= " LEFT JOIN ";
        $query .= "     V_ENTEXAM_MAJOR_MST M3 ON M3.MAJORCD = W3.MAJORLCD || W3.MAJORSCD ";
        $query .= " LEFT JOIN ";
        $query .= "     V_ENTEXAM_MAJOR_MST M4 ON M4.MAJORCD = W4.MAJORLCD || W4.MAJORSCD ";
        $query .= " LEFT JOIN ";
        $query .= "     V_ENTEXAM_MAJOR_MST M5 ON M5.MAJORCD = W5.MAJORLCD || W5.MAJORSCD ";
        $query .= " LEFT JOIN ";
        $query .= "     V_ENTEXAM_MAJOR_MST M6 ON M6.MAJORCD = W6.MAJORLCD || W6.MAJORSCD ";
        $query .= " WHERE ";
        $query .= "         T1.APPLICANTDIV = '{$model->field["APPLICANTDIV"]}' ";
        $query .= "     AND T1.ENTEXAMYEAR  = '{$model->test_year}' ";
        if ($model->field["SUPPLEMENT"]) {
            $query .= " AND T1.TESTDIV2 = '1' ";
        }
        $query .= " ORDER BY ";
        if ($model->field["SYUTURYOKUJUN"] == '1') {
            $query .= "     T1.EXAMNO ";
        } else {
            $query .= "     T1.FS_CD ";
        }

        return $query;
    }

    /******************************************************************************/
    /*************************** そのデータが存在するか ***************************/
    /******************************************************************************/
    //性別
    function sexCntSql($sex) {
        $query .= " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'Z002' AND ";
        $query .= "     NAMECD2 = '{$sex}' ";

        return $query;
    }

    //出身学校コード
    function fs_cdCntSql($fs_cd) {
        $query .= " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     FINSCHOOL_MST ";
        $query .= " WHERE ";
        $query .= "     FINSCHOOLCD = '{$fs_cd}' ";

        return $query;
    }

    //出身学校区分
    function fs_grddivCntSql($fs_grddiv) {
        $query .= " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'L016' AND ";
        $query .= "     NAMECD2 = '{$fs_grddiv}' ";

        return $query;
    }

    //学科コード
    function wishnoCntSql($majorcd) {
        $query .= " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     V_ENTEXAM_MAJOR_MST ";
        $query .= " WHERE ";
        $query .= "     MAJORCD = '{$majorcd}' ";

        return $query;
    }


    /**************************************************************/
    /************************* 更新時に使う ***********************/
    /**************************************************************/
    function cntEntexamApplicantbaseDat($model, $data_arr) {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DAT ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR = '{$model->test_year}' AND ";
        $query .= "     APPLICANTDIV = '{$data_arr["APPLICANTDIV"]}' AND ";
        $query .= "     EXAMNO = '{$data_arr["EXAMNO"]}' AND ";
        $query .= "     TESTDIV = '1' AND ";
        $query .= "     SHDIV = '1' AND ";
        $query .= "     DESIREDIV = '1' ";

        return $query;
    }

    function cntEntexamApplicantaddrDat($model, $data_arr) {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTADDR_DAT ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR = '{$model->test_year}' AND ";
        $query .= "     EXAMNO = '{$data_arr["EXAMNO"]}' ";

        return $query;
    }

    //出身学校
    function getFinschoolMst($finschool_cd) {
        $query .= " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     FINSCHOOL_MST ";
        $query .= " WHERE ";
        $query .= "     FINSCHOOLCD = '{$finschool_cd}' ";

        return $query;
    }

    //名称マスタより和暦の元号を取得
    function get_calendarno($year) {
        $query = "  SELECT
                        NAMECD2,
                        NAME1,
                        NAMESPARE1,
                        NAMESPARE2,
                        NAMESPARE3
                    FROM
                        V_NAME_MST
                    WHERE
                        YEAR = '".$year."' AND
                        NAMECD1 = 'L007'
                 ";

        return $query;
    }
}
?>
