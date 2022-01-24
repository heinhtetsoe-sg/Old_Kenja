<?php

require_once('for_php7.php');
class knjl015cquery extends Query
{
    //入試制度
    public function getApplicantdiv($model)
    {
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

    //入試区分取得
    public function getTestDiv($model)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     NAME1 AS LABEL, ";
        $query .= "     NAMESPARE2 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$model->test_year}' AND ";
        $query .= "     NAMECD1 = 'L004' AND ";
        $query .= "     NAMECD2 IN( SELECT DISTINCT ";
        $query .= "                     TESTDIV ";
        $query .= "                 FROM ";
        $query .= "                     ENTEXAM_APPLICANTBASE_DAT ";
        $query .= "                 WHERE ";
        $query .= "                     ENTEXAMYEAR = '{$model->test_year}' )";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //マスタの追加（ＣＳＶデータより読込）
    public function udpateQueryCsv($model, &$data_arr)
    {
        $data = array();
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $result = $db->query(knjl015cQuery::getCalendarno($model->test_year));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $eracd[]    = $row["NAMECD2"];
            $defyear[]  = $row["NAMESPARE1"];
            $defsdate[] = $row["NAMESPARE2"];
            $defedate[] = $row["NAMESPARE3"];
        }

        $result->free();

        $cnt = 0;   //処理件数
        for ($i = 0; $i < get_count($data_arr); $i++) {
            //分解する日付一覧
            $date_array = array("BIRTHDAY" => "birth", "FS_YM" => "fs");
            //部品名
            $parts = array("_eracd", "_y", "_m", "_d");

            foreach ($date_array as $dkey => $dval) {
                //部品の変数名
                list($e_id, $y_id, $m_id, $d_id) = array($dval."_eracd", $dval."_y", $dval."_m", $dval."_d");
                $$e_id = $$y_id = $$m_id = $$d_id = "";
                if (strlen($data_arr[$i][$dkey])) {
                    //日付を分解
                    list($y, $m, $d) = preg_split('/-/', $data_arr[$i][$dkey]);
                    for ($ere_no=0; $ere_no < get_count($eracd); $ere_no++) {
                        if ($defsdate[$ere_no] <= $data_arr[$i][$dkey] && $data_arr[$i][$dkey] <= $defedate[$ere_no]) {
                            $$e_id = $eracd[$ere_no];
                            $$y_id = ((int)$y + 1) - ((int)$defyear[$ere_no]);
                            $$y_id = sprintf("%02d", $$y_id);
                            $$m_id = $m;
                            $$d_id = $d;
                            break;
                        }
                    }
                }
            }

            $base_cnt = $db->getOne(knjl015cQuery::cntEntexamApplicantbaseDat($model, $data_arr[$i]));
            $addr_cnt = $db->getOne(knjl015cQuery::cntEntexamApplicantaddrDat($model, $data_arr[$i]));

            $finschool = $db->getRow(knjl015cQuery::getFinschoolMst($data_arr[$i]["FS_CD"]), DB_FETCHMODE_ASSOC);

            $data = array();
            $data["ENTEXAMYEAR"][TEXT]      = $model->test_year;
            $data["APPLICANTDIV"][TEXT]     = $data_arr[$i]["APPLICANTDIV"];
            $data["EXAMNO"][TEXT]           = $data_arr[$i]["EXAMNO"];
            $data["TESTDIV"][TEXT]          = $data_arr[$i]["TESTDIV"];
            $data["SHDIV"][TEXT]            = $data_arr[$i]["SHDIV"];
            $data["DESIREDIV"][TEXT]        = '1';
            //第２志望
            //$testdiv1 = "";
            //if ($data_arr[$i]["APPLICANTDIV"] == "2" && $data_arr[$i]["TESTDIV"] == "3" && $data_arr[$i]["TESTDIV1"] == "1") $testdiv1 = "8"; //EMS(一般)
            //$data["TESTDIV1"][TEXT]         = $testdiv1;
            $data["NAME"][TEXT]             = $data_arr[$i]["NAME"];
            $data["NAME_KANA"][TEXT]        = $data_arr[$i]["NAME_KANA"];
            $data["SEX"][TEXT]              = $data_arr[$i]["SEX"];
            $data["ERACD"][TEXT]            = $birth_eracd;
            $data["BIRTH_Y"][TEXT]          = $birth_y;
            $data["BIRTH_M"][TEXT]          = $birth_m;
            $data["BIRTH_D"][TEXT]          = $birth_d;
            $data["BIRTHDAY"][TEXT]         = str_replace("/", "-", $data_arr[$i]["BIRTHDAY"]);
            $data["FS_CD"][TEXT]            = $data_arr[$i]["FS_CD"];
            $data["FS_ERACD"][TEXT]         = $fs_eracd;
            $data["FS_Y"][TEXT]             = $fs_y;
            $data["FS_M"][TEXT]             = $fs_m;
            $data["RECOM_EXAMNO"][TEXT]     = $data_arr[$i]["RECOM_EXAMNO"];
            $data["SELECT_SUBCLASS_DIV"][TEXT] = $data_arr[$i]["SELECT_SUBCLASS_DIV"];
            if ($model->isCollege) {
                $shift_desire_flg = "";
                if (strlen($data_arr[$i]["SHIFT_DESIRE_FLG1"]) && strlen($data_arr[$i]["SHIFT_DESIRE_FLG2"])) {
                    $shift_desire_flg = "3";    //3:両方
                } elseif (strlen($data_arr[$i]["SHIFT_DESIRE_FLG1"])) {
                    $shift_desire_flg = "1";    //1:五條
                } elseif (strlen($data_arr[$i]["SHIFT_DESIRE_FLG2"])) {
                    $shift_desire_flg = "2";    //2:和歌山
                }
                $data["SHIFT_DESIRE_FLG"][TEXT] = $shift_desire_flg;
            } else {
                $data["SHIFT_DESIRE_FLG"][TEXT] = $data_arr[$i]["SHIFT_DESIRE_FLG"];
            }
            $data["PRISCHOOLCD"][TEXT]      = $data_arr[$i]["PRISCHOOLCD"];
            $data["REMARK1"][TEXT]          = $data_arr[$i]["REMARK1"];
            $data["REMARK2"][TEXT]          = $data_arr[$i]["REMARK2"];
            $data["BUS_USE"][TEXT]          = $data_arr[$i]["BUS_USE"];
            $data["STATIONDIV"][TEXT]       = $data_arr[$i]["STATIONDIV"];
            $data["BUS_USER_COUNT"][NUMBER] = $data_arr[$i]["BUS_USER_COUNT"];
            $data["PRE_RECEPTNO"][TEXT]     = $data_arr[$i]["PRE_RECEPTNO"];
            $data["DORMITORY_FLG"][TEXT]    = $data_arr[$i]["DORMITORY_FLG"];
            $data["PERSONAL_FLG"][TEXT]     = $data_arr[$i]["PERSONAL_FLG"];
            if ($model->isCollege) {
                $data["SUB_ORDER"][TEXT]        = $data_arr[$i]["SUB_ORDER"];
            }
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][NUMBER]        = "SYSDATE()";

            $where  = " WHERE ENTEXAMYEAR  = '{$model->test_year}' AND";
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
            $data["PREF_CD"][TEXT]       = $data_arr[$i]["PREF_CD"];
            $data["ADDRESS1"][TEXT]      = $data_arr[$i]["ADDRESS1"];
            $data["ADDRESS2"][TEXT]      = $data_arr[$i]["ADDRESS2"];
            $data["EDBOARDCD"][TEXT]     = $data_arr[$i]["EDBOARDCD"];
            $data["TELNO"][TEXT]         = $data_arr[$i]["TELNO"];
            $data["GKANA"][TEXT]         = $data_arr[$i]["GKANA"];
            $data["GNAME"][TEXT]         = $data_arr[$i]["GNAME"];
            $data["RELATIONSHIP"][TEXT]  = $data_arr[$i]["RELATIONSHIP"];
            $data["GZIPCD"][TEXT]        = $data_arr[$i]["GZIPCD"];
            $data["GPREF_CD"][TEXT]      = $data_arr[$i]["GPREF_CD"];
            $data["GADDRESS1"][TEXT]     = $data_arr[$i]["GADDRESS1"];
            $data["GADDRESS2"][TEXT]     = $data_arr[$i]["GADDRESS2"];
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

            $cnt++;
        }
        $db->commit();
        Query::dbCheckIn($db);

        return $cnt;
    }

    //エラーデータの削除
    public function deleteQueryErr()
    {
        $query  = "DELETE FROM W_CSVMSG_DAT ";
        return $query;
    }

    //エラーＤＢへの追加
    public function insertQueryErr(&$db, $record_no, $check_error)
    {
        $data1["MSGROW"][NUMBER]  = $record_no;
        $data1["MSGREMARK"][TEXT] = $check_error;

        $query = Query::insertSQL($data1, "w_csvmsg_dat");

        $result = $db->query($query);
    }

    //CVSエラー作成用のQUERY
    public function selectCsvErrQuery()
    {
        $query  = "SELECT * FROM W_CSVMSG_DAT";
        return $query;
    }

    //CSVデータ出力
    public function selectMainQuery($model)
    {
        $query  = " SELECT ";
        $query .= "     T1.APPLICANTDIV, ";
        $query .= "     T1.TESTDIV, ";
        $query .= "     T1.EXAMNO, ";
        $query .= "     T1.NAME_KANA, ";
        $query .= "     T1.NAME, ";
        $query .= "     T1.SHDIV, ";
        $query .= "     CASE WHEN T1.APPLICANTDIV = '2' AND T1.TESTDIV = '3' AND T1.TESTDIV1 = '8' THEN '1' ";
        $query .= "          ELSE '' END AS TESTDIV1, ";
        $query .= "     T1.SEX, ";
        $query .= "     T1.BIRTHDAY, ";
        $query .= "     T2.ZIPCD, ";
        $query .= "     T2.PREF_CD, ";
        $query .= "     T2.ADDRESS1, ";
        $query .= "     T2.ADDRESS2, ";
        $query .= "     T2.EDBOARDCD, ";
        $query .= "     T2.TELNO, ";
        $query .= "     T1.FS_CD, ";
        $query .= "     T1.FS_ERACD, ";
        $query .= "     T1.FS_Y, ";
        $query .= "     T1.FS_M, ";
        $query .= "     N1.NAMESPARE1 AS S_YEAR, ";
        $query .= "     T2.GKANA, ";
        $query .= "     T2.GNAME, ";
        $query .= "     T2.RELATIONSHIP, ";
        $query .= "     T2.GZIPCD, ";
        $query .= "     T2.GPREF_CD, ";
        $query .= "     T2.GADDRESS1, ";
        $query .= "     T2.GADDRESS2, ";
        $query .= "     T2.GTELNO, ";
        $query .= "     T1.RECOM_EXAMNO, ";
        $query .= "     T1.SELECT_SUBCLASS_DIV, ";
        $query .= "     T1.SHIFT_DESIRE_FLG, ";
        $query .= "     CASE WHEN T1.SHIFT_DESIRE_FLG IN ('1','3') THEN '1' END AS SHIFT_DESIRE_FLG1, ";
        $query .= "     CASE WHEN T1.SHIFT_DESIRE_FLG IN ('2','3') THEN '1' END AS SHIFT_DESIRE_FLG2, ";
        $query .= "     T1.PRISCHOOLCD, ";
        $query .= "     T1.REMARK1, ";
        $query .= "     T1.REMARK2, ";
        $query .= "     T1.BUS_USE, ";
        $query .= "     T1.STATIONDIV, ";
        $query .= "     T1.BUS_USER_COUNT, ";
        $query .= "     T1.PRE_RECEPTNO, ";
        $query .= "     T1.DORMITORY_FLG, ";
        $query .= "     T1.PERSONAL_FLG, ";
        $query .= "     T1.SUB_ORDER, ";
        $query .= "     '".$model->lastColumn."' AS ".$model->lastColumn." ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DAT T1 ";
        $query .= " LEFT JOIN ";
        $query .= "     ENTEXAM_APPLICANTADDR_DAT T2 ON T1.ENTEXAMYEAR = T2.ENTEXAMYEAR AND T1.EXAMNO = T2.EXAMNO ";
        $query .= " LEFT JOIN ";
        $query .= "     NAME_MST N1 ON N1.NAMECD1 = 'L007' AND N1.NAMECD2 = T1.FS_ERACD ";
        $query .= " WHERE ";
        $query .= "     T1.ENTEXAMYEAR = '{$model->test_year}' AND ";
        $query .= "     T1.APPLICANTDIV = '{$model->field["APPLICANTDIV"]}' ";
        if ($model->field["TESTDIV"] != "9") {
            $query .= "     AND T1.TESTDIV = '{$model->field["TESTDIV"]}' ";
        }
        $query .= " ORDER BY ";
        $query .= "     T1.APPLICANTDIV, ";
        $query .= "     T1.TESTDIV, ";
        $query .= "     T1.EXAMNO ";

        return $query;
    }

    /******************************************************************************/
    /*************************** そのデータが存在するか ***************************/
    /******************************************************************************/
    //名称マスタ件数取得
    public function getNameMstCnt($namecd1, $namecd2)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = '{$namecd1}' AND ";
        $query .= "     NAMECD2 = '{$namecd2}' ";

        return $query;
    }

    //委員会コード
    public function edboardcdCntSql($edboardcd)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     EDBOARD_MST ";
        $query .= " WHERE ";
        $query .= "     EDBOARDCD = '{$edboardcd}' ";

        return $query;
    }

    //出身学校コード
    public function fsCdCntSql($fs_cd)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     FINSCHOOL_MST ";
        $query .= " WHERE ";
        $query .= "     FINSCHOOLCD = '{$fs_cd}' ";

        return $query;
    }

    //都道府県コード
    public function prefCdCntSql($pref_cd)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     PREF_MST ";
        $query .= " WHERE ";
        $query .= "     PREF_CD = '{$pref_cd}' ";

        return $query;
    }

    //塾コード
    public function prischoolcdCntSql($prischoolcd)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     PRISCHOOL_MST ";
        $query .= " WHERE ";
        $query .= "     PRISCHOOLCD = '{$prischoolcd}' ";

        return $query;
    }

    /**************************************************************/
    /************************* 更新時に使う ***********************/
    /**************************************************************/
    public function cntEntexamApplicantbaseDat($model, $data_arr)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DAT ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR = '{$model->test_year}' AND ";
        $query .= "     EXAMNO = '{$data_arr["EXAMNO"]}' ";

        return $query;
    }

    public function cntEntexamApplicantaddrDat($model, $data_arr)
    {
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
    public function getFinschoolMst($finschool_cd)
    {
        $query .= " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     FINSCHOOL_MST ";
        $query .= " WHERE ";
        $query .= "     FINSCHOOLCD = '{$finschool_cd}' ";

        return $query;
    }

    //名称マスタより和暦の元号を取得
    public function getCalendarno($year)
    {
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

    //学校コード取得
    public function getSchoolcd()
    {
        $query  = " SELECT ";
        $query .= "     NAME2 ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'Z010' AND ";
        $query .= "     NAMECD2 = '00' ";

        $db = Query::dbCheckOut();
        $schoolcd = $db->getOne($query);
        Query::dbCheckIn($db);

        return $schoolcd;
    }
}
