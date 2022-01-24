<?php

require_once('for_php7.php');
class knjl213cquery extends Query
{
    //入試制度
    public function getApplicantdiv($flg, $val = "")
    {
        $query  = " WITH DATA(LABEL, VALUE) AS ( ";
        $query .= "     VALUES('1:中学', '1') ";
        $query .= " ) ";

        $query .= " SELECT ";
        if ($flg == "cnt") {
            $query .= "     COUNT(*) AS CNT ";
        } else {
            $query .= "     * ";
        }
        $query .= " FROM ";
        $query .= "     DATA ";
        if ($flg == "cnt") {
            $query .= " WHERE ";
            $query .= "     VALUE = '".$val."' ";
        } else {
            $query .= " ORDER BY ";
            $query .= "     VALUE ";
        }

        return $query;
    }

    //性別取得
    public function getSex($flg, $namecd2 = "")
    {
        $query  = " SELECT ";
        if ($flg == "cnt") {
            $query .= "     COUNT(*) AS CNT ";
        } else {
            $query .= "     NAMECD2 || ':' || ABBV1 ";
        }
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'Z002' ";
        if ($flg == "cnt") {
            $query .= " AND NAMECD2 = '".$namecd2."' ";
        } else {
            $query .= " ORDER BY ";
            $query .= "     NAMECD2 ";
        }

        return $query;
    }

    //出身学校コード
    public function chkFinSchool($fs_cd)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     FINSCHOOL_MST ";
        $query .= " WHERE ";
        $query .= "     FINSCHOOLCD = '".$fs_cd."' ";

        return $query;
    }

    //塾コード
    public function chkPriSchool($ps_cd)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     PRISCHOOL_MST ";
        $query .= " WHERE ";
        $query .= "     PRISCHOOLCD = '".$ps_cd."' ";

        return $query;
    }

    //名称マスタよりデータを取得
    public function getVnameMst($year, $namecd1, $namecd2 = "")
    {
        $query .= " SELECT ";
        $query .= "     NAMECD2, ";
        $query .= "     NAME1, ";
        $query .= "     NAMESPARE1 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$year}' ";
        $query .= "     AND NAMECD1 = '{$namecd1}' ";
        if ($namecd2) {
            $query .= "     AND NAMECD2 = '{$namecd2}' ";
        }
        $query .= " ORDER BY ";
        $query .= "     NAMECD2 ";

        return $query;
    }

    //存在チェック
    public function existdataNoAndName($model, $data)
    {
        $query .= " SELECT ";
        $query .= "     PRE_RECEPTNO ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_PRE_DAT ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR = '{$model->entexamyear}' ";
        $query .= "     AND PRE_RECEPTNO = '{$data["RECOM_EXAMNO"]}' ";
        $query .= "     AND NAME = '{$data["NAME"]}' ";

        return $query;
    }

    //マスタの追加（ＣＳＶデータより読込）
    public function udpateQueryCsv($model, &$data_arr)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $cnt = 0;   //処理件数
        for ($i = 0; $i < get_count($data_arr); $i++) {
            //データ件数取得
            $query  = " SELECT ";
            $query .= "     COUNT(*) ";
            $query .= " FROM ";
            $query .= "     ENTEXAM_APPLICANTBASE_PRE_DAT ";
            $query .= " WHERE ";
            $query .= "     ENTEXAMYEAR     = '".$model->entexamyear."' AND ";
            $query .= "     PRE_RECEPTNO    = '".$data_arr[$i]["PRE_RECEPTNO"]."' ";
            $data_cnt = $db->getOne($query);

            $data = array();
            $data["ENTEXAMYEAR"][TEXT]      = $model->entexamyear;
            $data["APPLICANTDIV"][TEXT]     = $data_arr[$i]["APPLICANTDIV"];
            $data["PRE_RECEPTNO"][TEXT]     = $data_arr[$i]["PRE_RECEPTNO"];
            $data["PRE_TESTDIV"][TEXT]      = $data_arr[$i]["PRE_TESTDIV"];
            $data["PRE_EXAM_TYPE"][TEXT]    = $data_arr[$i]["PRE_EXAM_TYPE"];
            $data["PRE_RECEPTDIV"][TEXT]    = $data_arr[$i]["PRE_RECEPTDIV"];
            $data["PRE_RECEPTDATE"][TEXT]   = str_replace("/", "-", $data_arr[$i]["PRE_RECEPTDATE"]);
            $data["NAME"][TEXT]             = $data_arr[$i]["NAME"];
            $data["NAME_KANA"][TEXT]        = $data_arr[$i]["NAME_KANA"];
            $data["SEX"][TEXT]              = $data_arr[$i]["SEX"];
            $data["GNAME"][TEXT]            = $data_arr[$i]["GNAME"];
            $data["GKANA"][TEXT]            = $data_arr[$i]["GKANA"];
            $data["ZIPCD"][TEXT]            = $data_arr[$i]["ZIPCD"];
            $data["ADDRESS1"][TEXT]         = $data_arr[$i]["ADDRESS1"];
            $data["ADDRESS2"][TEXT]         = $data_arr[$i]["ADDRESS2"];
            $data["TELNO"][TEXT]            = $data_arr[$i]["TELNO"];
            $data["FS_CD"][TEXT]            = $data_arr[$i]["FS_CD"];
            $data["PS_CD"][TEXT]            = $data_arr[$i]["PS_CD"];
            $data["PS_CONTACT"][TEXT]       = $data_arr[$i]["PS_CONTACT"];
            if ($model->Properties["Pretest_bus_Not_Hyouji"] != "1") {
                $data["BUS_USE"][TEXT]          = $data_arr[$i]["BUS_USE"];
                $data["STATIONDIV"][TEXT]       = ($data_arr[$i]["BUS_USE"]) ? $data_arr[$i]["STATIONDIV"] : "";
                $data["BUS_USER_COUNT"][NUMBER] = ($data_arr[$i]["BUS_USE"]) ? $data_arr[$i]["BUS_USER_COUNT"] : "";
            }
            $data["RECOM_EXAMNO"][TEXT]     = $data_arr[$i]["RECOM_EXAMNO"];
            $data["REMARK"][TEXT]           = $data_arr[$i]["REMARK"];
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][NUMBER]        = "SYSDATE()";

            $where  = " WHERE ";
            $where .= "     ENTEXAMYEAR     = '".$model->entexamyear."' AND";
            $where .= "     PRE_RECEPTNO    = '".$data_arr[$i]["PRE_RECEPTNO"]."' ";

            if (0 == $data_cnt) {
                $query = Query::insertSQL($data, "ENTEXAM_APPLICANTBASE_PRE_DAT");
            } else {
                $query = Query::updateSQL($data, "ENTEXAM_APPLICANTBASE_PRE_DAT", $where);
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
        $query  = " DELETE FROM ";
        $query .= "     W_CSVMSG_PRG_DAT ";
        $query .= " WHERE ";
        $query .= "     PROGRAMID = '".PROGRAMID."' ";

        return $query;
    }

    //エラーＤＢへの追加
    public function insertQueryErr(&$db, $record_no, $check_error)
    {
        $data = array();
        $data["PROGRAMID"][TEXT] = PROGRAMID;
        $data["MSGROW"][NUMBER]  = $record_no;
        $data["MSGREMARK"][TEXT] = $check_error;

        $query = Query::insertSQL($data, "W_CSVMSG_PRG_DAT");
        $result = $db->query($query);
    }

    //CVSエラー作成用のQUERY
    public function selectCsvErrQuery()
    {
        $query  = " SELECT ";
        $query .= "     MSGROW, ";
        $query .= "     MSGREMARK ";
        $query .= " FROM ";
        $query .= "     W_CSVMSG_PRG_DAT ";
        $query .= " WHERE ";
        $query .= "     PROGRAMID = '".PROGRAMID."' ";

        return $query;
    }

    //CSVデータ出力
    public function selectMainQuery($model)
    {
        $query  = " SELECT ";
        $query .= "     APPLICANTDIV, ";
        $query .= "     PRE_RECEPTNO, ";
        $query .= "     PRE_TESTDIV, ";
        $query .= "     PRE_RECEPTDIV, ";
        $query .= "     PRE_RECEPTDATE, ";
        $query .= "     NAME, ";
        $query .= "     NAME_KANA, ";
        $query .= "     SEX, ";
        $query .= "     GNAME, ";
        $query .= "     GKANA, ";
        $query .= "     ZIPCD, ";
        $query .= "     ADDRESS1, ";
        $query .= "     ADDRESS2, ";
        $query .= "     TELNO, ";
        $query .= "     FS_CD, ";
        $query .= "     PS_CD, ";
        $query .= "     PS_CONTACT, ";
        $query .= "     PRE_EXAM_TYPE, ";
        if ($model->Properties["Pretest_bus_Not_Hyouji"] != "1") {
            $query .= "     BUS_USE, ";
            $query .= "     STATIONDIV, ";
            $query .= "     BUS_USER_COUNT, ";
        }
        $query .= "     RECOM_EXAMNO, ";
        $query .= "     REMARK, ";
        $query .= "     '".$model->lastColumn."' AS ".$model->lastColumn." ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_PRE_DAT ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR     = '".$model->entexamyear."' AND ";
        $query .= "     APPLICANTDIV    = '".$model->field["APPLICANTDIV"]."' ";
        $query .= " ORDER BY ";
        $query .= "     PRE_RECEPTNO ";

        return $query;
    }
}
