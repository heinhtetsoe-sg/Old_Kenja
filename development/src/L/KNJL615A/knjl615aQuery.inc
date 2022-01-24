<?php

require_once('for_php7.php');

class knjl615aquery extends Query
{

    //名称マスタよりデータを取得
    public function getNameCd($year, $namecd1)
    {
        $query  = " SELECT ";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL, ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     ABBV1, ";
        $query .= "     NAMESPARE2 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$year}' ";
        $query .= "     AND NAMECD1 = '{$namecd1}' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //名称マスタよりデータを取得(NAME1からNAMECD2を取得)
    public function getNameCd2($year)
    {
        $query  = " SELECT ";
        $query .= "     NAMECD2, ";
        $query .= "     NAME1 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$year}' ";
        $query .= "     AND NAMECD1 = 'L070' ";

        return $query;
    }

    //都道府県
    public function getPrefMst()
    {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     PREF_MST ";

        return $query;
    }

    //志望コース取得
    public function getMiraiCourseGrp($model)
    {
        $query  = " SELECT ";
        $query .= "     TESTDIV, ";
        $query .= "     MIRAI_COURSECD, ";
        $query .= "     MIRAI_COURSE_ABBV, ";
        $query .= "     EXAMCOURSECD1, ";
        $query .= "     EXAMCOURSECD2 ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_MIRAI_COURSE_GRP_MST ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR = '{$model->year}' ";
        $query .= "     AND APPLICANTDIV = '{$model->field["APPLICANTDIV"]}' ";

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
            //志願
            $query  = " SELECT ";
            $query .= "     COUNT(*) AS CNT ";
            $query .= " FROM ";
            $query .= "     ENTEXAM_APPLICANTBASE_DAT ";
            $query .= " WHERE ";
            $query .= "     ENTEXAMYEAR = '{$data_arr[$i]["ENTEXAMYEAR"]}' ";
            $query .= "     AND APPLICANTDIV = '{$model->field["APPLICANTDIV"]}' ";
            $query .= "     AND EXAMNO = '{$data_arr[$i]["EXAMNO"]}' ";

            $baseCnt = $db->getOne($query);

            $data = array();
            $data["TESTDIV"][TEXT]          = "1";
            $data["SHDIV"][TEXT]            = "1";
            $data["DESIREDIV"][TEXT]        = "1";
            $data["NAME"][TEXT]             = $data_arr[$i]["NAME"];
            $data["NAME_KANA"][TEXT]        = $data_arr[$i]["NAME_KANA"];
            $data["SEX"][TEXT]              = $data_arr[$i]["SEX"];
            $data["BIRTHDAY"][TEXT]         = $data_arr[$i]["BIRTHDAY"];
            $data["FS_CD"][TEXT]            = $data_arr[$i]["FS_CD"];
            $data["FS_NAME"][TEXT]          = $data_arr[$i]["FS_NAME"];
            $data["FS_GRDDIV"][TEXT]        = $data_arr[$i]["FS_GRDDIV"];
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][NUMBER]        = "SYSDATE()";

            if ($baseCnt > 0) {
                $where  = " WHERE ";
                $where .= "     ENTEXAMYEAR = '{$data_arr[$i]["ENTEXAMYEAR"]}' ";
                $where .= "     AND APPLICANTDIV = '{$model->field["APPLICANTDIV"]}' ";
                $where .= "     AND EXAMNO = '{$data_arr[$i]["EXAMNO"]}' ";
    
                $query = Query::updateSQL($data, "ENTEXAM_APPLICANTBASE_DAT", $where);
            } else {
                $data["ENTEXAMYEAR"][TEXT]      = $data_arr[$i]["ENTEXAMYEAR"];
                $data["APPLICANTDIV"][TEXT]     = $model->field["APPLICANTDIV"];
                $data["EXAMNO"][TEXT]           = $data_arr[$i]["EXAMNO"];

                $query = Query::insertSQL($data, "ENTEXAM_APPLICANTBASE_DAT");
            }
            $db->query($query);

            //住所
            $query  = " DELETE FROM ENTEXAM_APPLICANTADDR_DAT ";
            $query .= " WHERE ";
            $query .= "     ENTEXAMYEAR = '{$data_arr[$i]["ENTEXAMYEAR"]}' ";
            $query .= "     AND APPLICANTDIV = '{$model->field["APPLICANTDIV"]}' ";
            $query .= "     AND EXAMNO = '{$data_arr[$i]["EXAMNO"]}' ";
            $db->query($query);

            $setZip = strlen($data_arr[$i]["ZIPCD"]) == 7 ? substr($data_arr[$i]["ZIPCD"], 0, 3).'-'.substr($data_arr[$i]["ZIPCD"], 3) : $data_arr[$i]["ZIPCD"];
            $data = array();
            $data["ENTEXAMYEAR"][TEXT]      = $data_arr[$i]["ENTEXAMYEAR"];
            $data["APPLICANTDIV"][TEXT]     = $model->field["APPLICANTDIV"];
            $data["EXAMNO"][TEXT]           = $data_arr[$i]["EXAMNO"];
            $data["ZIPCD"][TEXT]            = $setZip;
            $data["PREF_CD"][TEXT]          = $data_arr[$i]["PREF_CD"];
            $data["ADDRESS1"][TEXT]         = $data_arr[$i]["ADDRESS1"];
            $data["ADDRESS2"][TEXT]         = $data_arr[$i]["ADDRESS2"];
            $data["TELNO"][TEXT]            = $data_arr[$i]["TEL"];
            $data["EMAIL"][TEXT]            = $data_arr[$i]["EMAIL"];
            $data["GNAME"][TEXT]            = $data_arr[$i]["GNAME"];
            $data["GKANA"][TEXT]            = $data_arr[$i]["GKANA"];
            $data["GZIPCD"][TEXT]           = $setZip;
            $data["GPREF_CD"][TEXT]         = $data_arr[$i]["PREF_CD"];
            $data["GADDRESS1"][TEXT]        = $data_arr[$i]["ADDRESS1"];
            $data["GADDRESS2"][TEXT]        = $data_arr[$i]["ADDRESS2"];
            $data["GTELNO"][TEXT]           = $data_arr[$i]["G_TEL"];
            $data["RELATIONSHIP"][TEXT]     = $data_arr[$i]["RELATIONSHIP"];
            $data["EMERGENCYCALL"][TEXT]    = $data_arr[$i]["EMERGENCYCALL"];
            $data["EMERGENCYTELNO"][TEXT]   = $data_arr[$i]["EMERGENCYTELNO"];

            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][NUMBER]        = "SYSDATE()";

            $query = Query::insertSQL($data, "ENTEXAM_APPLICANTADDR_DAT");
            $db->query($query);

            //ENTEXAM_APPLICANTBASE_DETAIL_DAT
            $query  = " DELETE FROM ENTEXAM_APPLICANTBASE_DETAIL_DAT ";
            $query .= " WHERE ";
            $query .= "     ENTEXAMYEAR = '{$data_arr[$i]["ENTEXAMYEAR"]}' ";
            $query .= "     AND APPLICANTDIV = '{$model->field["APPLICANTDIV"]}' ";
            $query .= "     AND EXAMNO = '{$data_arr[$i]["EXAMNO"]}' ";
            $query .= "     AND SEQ IN ('005', '008') ";
            $db->query($query);

            //塾
            $data = array();
            $data["ENTEXAMYEAR"][TEXT]      = $data_arr[$i]["ENTEXAMYEAR"];
            $data["APPLICANTDIV"][TEXT]     = $model->field["APPLICANTDIV"];
            $data["EXAMNO"][TEXT]           = $data_arr[$i]["EXAMNO"];
            $data["SEQ"][TEXT]              = "008";
            $data["REMARK1"][TEXT]          = $data_arr[$i]["PRICD"];
            $data["REMARK2"][TEXT]          = $data_arr[$i]["PRINAME"];

            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][NUMBER]        = "SYSDATE()";

            $query = Query::insertSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT");
            $db->query($query);

            //優遇、受験校
            $data = array();
            $data["ENTEXAMYEAR"][TEXT]      = $data_arr[$i]["ENTEXAMYEAR"];
            $data["APPLICANTDIV"][TEXT]     = $model->field["APPLICANTDIV"];
            $data["EXAMNO"][TEXT]           = $data_arr[$i]["EXAMNO"];
            $data["SEQ"][TEXT]              = "005";
            $data["REMARK1"][TEXT]          = $data_arr[$i]["TOKUBETSU"];
            $data["REMARK2"][TEXT]          = $data_arr[$i]["KIKOKU"];
            $data["REMARK3"][TEXT]          = $data_arr[$i]["EIKEN"];
            $data["REMARK4"][TEXT]          = $data_arr[$i]["ENNOU"];
            $data["REMARK5"][TEXT]          = $data_arr[$i]["EXAM_SCHOOL1"];
            $data["REMARK6"][TEXT]          = $data_arr[$i]["EXAM_SCHOOL2"];
            $data["REMARK7"][TEXT]          = $data_arr[$i]["EXAM_SCHOOL3"];

            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][NUMBER]        = "SYSDATE()";

            $query = Query::insertSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT");
            $db->query($query);

            //ENTEXAM_RECEPT_DAT
            $query  = " SELECT ";
            $query .= "     COUNT(*) AS CNT ";
            $query .= " FROM ";
            $query .= "     ENTEXAM_RECEPT_DAT ";
            $query .= " WHERE ";
            $query .= "     ENTEXAMYEAR = '{$data_arr[$i]["ENTEXAMYEAR"]}' ";
            $query .= "     AND APPLICANTDIV = '{$model->field["APPLICANTDIV"]}' ";
            $query .= "     AND TESTDIV = '{$data_arr[$i]["TESTDIV"]}' ";
            $query .= "     AND RECEPTNO = '{$data_arr[$i]["RECEPTNO"]}' ";

            $receptCnt = $db->getOne($query);

            $data = array();
            $data["EXAMNO"][TEXT]           = $data_arr[$i]["EXAMNO"];
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][NUMBER]        = "SYSDATE()";
            if ($receptCnt > 0) {
                $where  = " WHERE ";
                $where .= "     ENTEXAMYEAR = '{$data_arr[$i]["ENTEXAMYEAR"]}' ";
                $where .= "     AND APPLICANTDIV = '{$model->field["APPLICANTDIV"]}' ";
                $where .= "     AND TESTDIV = '{$data_arr[$i]["TESTDIV"]}' ";
                $where .= "     AND RECEPTNO = '{$data_arr[$i]["RECEPTNO"]}' ";
        
                $query = Query::updateSQL($data, "ENTEXAM_RECEPT_DAT", $where);
            } else {
                $data["ENTEXAMYEAR"][TEXT]      = $data_arr[$i]["ENTEXAMYEAR"];
                $data["APPLICANTDIV"][TEXT]     = $model->field["APPLICANTDIV"];
                $data["TESTDIV"][TEXT]          = $data_arr[$i]["TESTDIV"];
                $data["EXAM_TYPE"][TEXT]        = "1";
                $data["RECEPTNO"][TEXT]         = $data_arr[$i]["RECEPTNO"];
    
                $query = Query::insertSQL($data, "ENTEXAM_RECEPT_DAT");
            }
            $db->query($query);

            //ENTEXAM_RECEPT_DETAIL_DAT //SEQ007
            $query  = " SELECT ";
            $query .= "     COUNT(*) AS CNT ";
            $query .= " FROM ";
            $query .= "     ENTEXAM_RECEPT_DETAIL_DAT ";
            $query .= " WHERE ";
            $query .= "     ENTEXAMYEAR         = '{$data_arr[$i]["ENTEXAMYEAR"]}' ";
            $query .= "     AND APPLICANTDIV    = '{$model->field["APPLICANTDIV"]}' ";
            $query .= "     AND TESTDIV         = '{$data_arr[$i]["TESTDIV"]}' ";
            $query .= "     AND RECEPTNO        = '{$data_arr[$i]["RECEPTNO"]}' ";
            $query .= "     AND SEQ IN ('007') ";
            $receptCnt = $db->getOne($query);

            $data = array();
            $data["REMARK1"][TEXT]          = $data_arr[$i]["HOPE1"];
            $data["REMARK2"][TEXT]          = $data_arr[$i]["HOPE2"];
            $data["REMARK5"][TEXT]          = $data_arr[$i]["TSUUGAKU"];
            $data["REMARK6"][TEXT]          = $data_arr[$i]["SENBATSU"];

            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][NUMBER]        = "SYSDATE()";
            if ($receptCnt > 0) {
                $where  = " WHERE ";
                $where .= "     ENTEXAMYEAR = '{$data_arr[$i]["ENTEXAMYEAR"]}' ";
                $where .= "     AND APPLICANTDIV = '{$model->field["APPLICANTDIV"]}' ";
                $where .= "     AND TESTDIV = '{$data_arr[$i]["TESTDIV"]}' ";
                $where .= "     AND RECEPTNO = '{$data_arr[$i]["RECEPTNO"]}' ";
                $where .= "     AND SEQ = '007' ";

                $query = Query::updateSQL($data, "ENTEXAM_RECEPT_DETAIL_DAT", $where);
            } else {
                $data["ENTEXAMYEAR"][TEXT]      = $data_arr[$i]["ENTEXAMYEAR"];
                $data["APPLICANTDIV"][TEXT]     = $model->field["APPLICANTDIV"];
                $data["TESTDIV"][TEXT]          = $data_arr[$i]["TESTDIV"];
                $data["EXAM_TYPE"][TEXT]        = "1";
                $data["RECEPTNO"][TEXT]         = $data_arr[$i]["RECEPTNO"];
                $data["SEQ"][TEXT]              = "007";

                $query = Query::insertSQL($data, "ENTEXAM_RECEPT_DETAIL_DAT");
            }
            $db->query($query);

            //ENTEXAM_RECEPT_DETAIL_DAT //SEQ014 : 志願者毎の試験会場コードを保持
            $query  = " SELECT ";
            $query .= "     COUNT(*) AS CNT ";
            $query .= " FROM ";
            $query .= "     ENTEXAM_RECEPT_DETAIL_DAT ";
            $query .= " WHERE ";
            $query .= "     ENTEXAMYEAR         = '{$data_arr[$i]["ENTEXAMYEAR"]}' ";
            $query .= "     AND APPLICANTDIV    = '{$model->field["APPLICANTDIV"]}' ";
            $query .= "     AND TESTDIV         = '{$data_arr[$i]["TESTDIV"]}' ";
            $query .= "     AND RECEPTNO        = '{$data_arr[$i]["RECEPTNO"]}' ";
            $query .= "     AND SEQ IN ('014') ";
            $receptCnt = $db->getOne($query);

            $data = array();
            $data["REMARK1"][TEXT]          = $data_arr[$i]["HALL_CD"];

            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][NUMBER]        = "SYSDATE()";
            if ($receptCnt > 0) {
                $where  = " WHERE ";
                $where .= "     ENTEXAMYEAR = '{$data_arr[$i]["ENTEXAMYEAR"]}' ";
                $where .= "     AND APPLICANTDIV = '{$model->field["APPLICANTDIV"]}' ";
                $where .= "     AND TESTDIV = '{$data_arr[$i]["TESTDIV"]}' ";
                $where .= "     AND RECEPTNO = '{$data_arr[$i]["RECEPTNO"]}' ";
                $where .= "     AND SEQ = '014' ";

                $query = Query::updateSQL($data, "ENTEXAM_RECEPT_DETAIL_DAT", $where);
            } else {
                $data["ENTEXAMYEAR"][TEXT]      = $data_arr[$i]["ENTEXAMYEAR"];
                $data["APPLICANTDIV"][TEXT]     = $model->field["APPLICANTDIV"];
                $data["TESTDIV"][TEXT]          = $data_arr[$i]["TESTDIV"];
                $data["EXAM_TYPE"][TEXT]        = "1";
                $data["RECEPTNO"][TEXT]         = $data_arr[$i]["RECEPTNO"];
                $data["SEQ"][TEXT]              = "014";

                $query = Query::insertSQL($data, "ENTEXAM_RECEPT_DETAIL_DAT");
            }
            $db->query($query);

            $query  = " SELECT ";
            $query .= "     COUNT(*) AS CNT ";
            $query .= " FROM ";
            $query .= "     ENTEXAM_APPLICANTCONFRPT_DAT ";
            $query .= " WHERE ";
            $query .= "     ENTEXAMYEAR = '{$data_arr[$i]["ENTEXAMYEAR"]}' ";
            $query .= "     AND APPLICANTDIV = '{$model->field["APPLICANTDIV"]}' ";
            $query .= "     AND EXAMNO = '{$data_arr[$i]["EXAMNO"]}' ";

            $baseCnt = $db->getOne($query);
            
            $data = array();
            $data["ENTEXAMYEAR"][TEXT]      = $data_arr[$i]["ENTEXAMYEAR"];
            $data["APPLICANTDIV"][TEXT]     = $model->field["APPLICANTDIV"];
            $data["EXAMNO"][TEXT]           = $data_arr[$i]["EXAMNO"];
            $data["TOTALSTUDYTIME"][TEXT]   = $data_arr[$i]["TOKUBETSU"];

            if ($baseCnt > 0) {
                $where  = " WHERE ";
                $where .= "     ENTEXAMYEAR = '{$data_arr[$i]["ENTEXAMYEAR"]}' ";
                $where .= "     AND APPLICANTDIV = '{$model->field["APPLICANTDIV"]}' ";
                $where .= "     AND EXAMNO = '{$data_arr[$i]["EXAMNO"]}' ";

                $query = Query::updateSQL($data, "ENTEXAM_APPLICANTCONFRPT_DAT", $where);
            } else {
                $data["ENTEXAMYEAR"][TEXT]      = $data_arr[$i]["ENTEXAMYEAR"];
                $data["APPLICANTDIV"][TEXT]     = $model->field["APPLICANTDIV"];
                $data["EXAMNO"][TEXT]           = $data_arr[$i]["EXAMNO"];

                $query = Query::insertSQL($data, "ENTEXAM_APPLICANTCONFRPT_DAT");
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
        $query  = " DELETE FROM W_CSVMSG_PRG_DAT WHERE PROGRAMID = '".PROGRAMID."' ";
        return $query;
    }

    //エラーＤＢへの追加
    public function insertQueryErr(&$db, $record_no, $check_error)
    {
        $data1 = array();
        $data1["PROGRAMID"][TEXT]       = PROGRAMID;
        $data1["MSGROW"][NUMBER]        = $record_no;
        $data1["MSGREMARK"][TEXT]       = $check_error;

        $query = Query::insertSQL($data1, "W_CSVMSG_PRG_DAT");
        $result = $db->query($query);
    }

    //CVSエラー作成用のQUERY
    public function selectCsvErrQuery()
    {
        $query  = " SELECT MSGROW, MSGREMARK FROM W_CSVMSG_PRG_DAT WHERE PROGRAMID = '".PROGRAMID."' ";

        return $query;
    }
}
?>
