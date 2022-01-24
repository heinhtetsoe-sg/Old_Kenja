<?php
class knjl690iquery extends Query
{
    /******************/
    /**  画面で使用  **/
    /******************/

    //名称マスタより取得
    public function getNameMst($year, $namecd1, $namecd2="")
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     NAME1, ";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL, ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     NAMESPARE2 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR    = '".$year."' AND ";
        $query .= "     NAMECD1 = '".$namecd1."' ";
        if ($namecd2) {
            $query .= " AND NAMECD2 = '".$namecd2."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //設定マスタチェック
    public function getEntexamSettingMst($model, $setcd)
    {
        $query .= "   SELECT ";
        $query .= "       SEQ AS VALUE, ";
        $query .= "       NAME1 AS NAME";
        $query .= "   FROM ";
        $query .= "       ENTEXAM_SETTING_MST ";
        $query .= "   WHERE ";
        $query .= "       ENTEXAMYEAR      = '{$model->ObjYear}' ";
        $query .= "       AND APPLICANTDIV = '{$model->field["APPLICANTDIV"]}' ";
        $query .= "       AND SETTING_CD   = '{$setcd}' ";
        $query .= "   ORDER BY ";
        $query .= "       SEQ ";
        if ($where) {
            $query .= $where;
        }

        return $query;
    }

    //入試区分マスタより取得
    public function getTestdivMst($model)
    {
        $query  = " SELECT ";
        $query .= "     TESTDIV AS VALUE, ";
        $query .= "     TESTDIV_NAME AS LABEL";
        $query .= " FROM ";
        $query .= "     ENTEXAM_TESTDIV_MST ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR      = '{$model->ObjYear}' ";
        $query .= "     AND APPLICANTDIV = '{$model->field["APPLICANTDIV"]}' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //コースマスタより取得
    public function getCourseMst($model)
    {
        $query  = "   SELECT ";
        $query .= "       GENERAL_CD AS VALUE, ";
        $query .= "       GENERAL_NAME AS LABEL ";
        $query .= "   FROM ";
        $query .= "       ENTEXAM_GENERAL_MST ";
        $query .= "   WHERE ";
        $query .= "       ENTEXAMYEAR      = '{$model->ObjYear}' ";
        $query .= "       AND APPLICANTDIV = '{$model->field["APPLICANTDIV"]}' ";
        $query .= "       AND TESTDIV      = '0' ";
        $query .= "       AND GENERAL_DIV  = '02' ";

        return $query;
    }

    /************************/
    /**  ＣＳＶ取込で使用  **/
    /************************/

    //追加（ＣＳＶデータより読込）
    public function updateQueryCsv($model, &$data_arr)
    {
        $data = array();
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $cnt = 0;   //処理件数
        for ($i = 0; $i < count($data_arr); $i++) {

            //更新　-- ENTEXAM_APPLICANTBASE_DAT
            $query  = " DELETE FROM ENTEXAM_APPLICANTBASE_DAT";
            $query .= " WHERE ENTEXAMYEAR   = '{$model->ObjYear}' AND ";
            $query .= "       APPLICANTDIV  = '{$model->field["APPLICANTDIV"]}' AND ";
            $query .= "       EXAMNO        = '{$data_arr[$i]["EXAMNO"]}' ";
            $db->query($query);

            $data = array();
            $data["ENTEXAMYEAR"][TEXT]          = $model->ObjYear;
            $data["APPLICANTDIV"][TEXT]         = $model->field["APPLICANTDIV"];
            $data["TESTDIV"][TEXT]              = $data_arr[$i]["TESTDIV"];
            $data["EXAMNO"][TEXT]               = $data_arr[$i]["EXAMNO"];
            $data["TESTDIV"][TEXT]              = $data_arr[$i]["TESTDIV"];
            $data["SHDIV"][TEXT]                = $model->field["SHDIV"];
            $data["DESIREDIV"][TEXT]            = $model->field["DESIREDIV"];
            $data["TESTDIV0"][TEXT]             = $data_arr[$i]["MAJORCD"];
            $data["NAME"][TEXT]                 = $data_arr[$i]["NAME"];
            $data["NAME_KANA"][TEXT]            = $data_arr[$i]["NAME_KANA"];
            $data["SEX"][TEXT]                  = $data_arr[$i]["SEX"];
            //日付データであれば登録
            if ($model->date_rep($model->getHihunDate($data_arr[$i]["BIRTHDAY"])) != "") {
                $data["BIRTHDAY"][TEXT]         = $model->getHihunDate($data_arr[$i]["BIRTHDAY"]);
            }
            $data["FS_CD"][TEXT]                = $data_arr[$i]["FS_CD"];
            $data["FS_GRDYEAR"][TEXT]           = $data_arr[$i]["FS_GRDYEAR"];
            $data["DORMITORY_FLG"][TEXT]        = ($data_arr[$i]["ENTER_DORMITORY_HOPE"] == "1" || $data_arr[$i]["ENTER_DORMITORY_HOPE"] == "2") ? "1" : "";
            $data["REGISTERCD"][TEXT]           = STAFFCD;
            $data["UPDATED"][NUMBER]            = "SYSDATE()";

            $query = Query::insertSQL($data, "ENTEXAM_APPLICANTBASE_DAT");
            $db->query($query);

            //更新 -- ENTEXAM_APPLICANTADDR_DAT
            $query  = " DELETE FROM ENTEXAM_APPLICANTADDR_DAT";
            $query .= " WHERE ENTEXAMYEAR   = '{$model->ObjYear}' AND ";
            $query .= "       APPLICANTDIV  = '{$model->field["APPLICANTDIV"]}' AND ";
            $query .= "       EXAMNO        = '{$data_arr[$i]["EXAMNO"]}' ";
            $db->query($query);

            $data = array();
            $data["ENTEXAMYEAR"][TEXT]      = $model->ObjYear;
            $data["APPLICANTDIV"][TEXT]     = $model->field["APPLICANTDIV"];
            $data["EXAMNO"][TEXT]           = $data_arr[$i]["EXAMNO"];
            $data["ZIPCD"][TEXT]            = $data_arr[$i]["ZIPCD"];
            $data["ADDRESS1"][TEXT]         = $data_arr[$i]["ADDRESS1"];
            $data["ADDRESS2"][TEXT]         = $data_arr[$i]["ADDRESS2"];
            $data["TELNO"][TEXT]            = $data_arr[$i]["TELNO"];
            $data["GNAME"][TEXT]            = $data_arr[$i]["GNAME"];
            $data["GKANA"][TEXT]            = $data_arr[$i]["GKANA"];
            $data["GZIPCD"][TEXT]           = $data_arr[$i]["ZIPCD"];
            $data["GADDRESS1"][TEXT]        = $data_arr[$i]["ADDRESS1"];
            $data["GADDRESS2"][TEXT]        = $data_arr[$i]["ADDRESS2"];
            $data["GTELNO"][TEXT]           = $data_arr[$i]["TELNO"];

            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][NUMBER]        = "SYSDATE()";

            $query = Query::insertSQL($data, "ENTEXAM_APPLICANTADDR_DAT");
            $db->query($query);

            //SEQ012
            //中学校コード、氏名、性別、生年月日が一致した各入試区分の受験番号をセット
            $sameExamArray = array();
            $query = knjl690iQuery::getSameApplicantExamno($model, $data_arr[$i]["EXAMNO"], $data_arr[$i]["TESTDIV"]);
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $sameExamArray[$row["TESTDIV"]] = $row["EXAMNO"];
            }

            foreach ($sameExamArray as $testdiv => $testdivExamno) {
                //更新 -- ENTEXAM_APPLICANTBASE_DETAIL_DAT
                $query  = " DELETE FROM ENTEXAM_APPLICANTBASE_DETAIL_DAT";
                $query .= " WHERE ENTEXAMYEAR   = '{$model->ObjYear}' AND ";
                $query .= "       APPLICANTDIV  = '{$model->field["APPLICANTDIV"]}' AND ";
                $query .= "       EXAMNO        = '{$testdivExamno}' AND ";
                $query .= "       SEQ           = '012' ";
                $db->query($query);

                $data = array();
                $data["ENTEXAMYEAR"][TEXT]      = $model->ObjYear;
                $data["APPLICANTDIV"][TEXT]     = $model->field["APPLICANTDIV"];
                $data["EXAMNO"][TEXT]           = $testdivExamno;
                $data["SEQ"][TEXT]              = '012';
                $data["REMARK1"][TEXT]          = $sameExamArray["01"];
                $data["REMARK2"][TEXT]          = $sameExamArray["02"];
                $data["REMARK3"][TEXT]          = $sameExamArray["03"];

                $data["REGISTERCD"][TEXT]       = STAFFCD;
                $data["UPDATED"][NUMBER]        = "SYSDATE()";

                $query = Query::insertSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT");
                $db->query($query);
            }

            //SEQ031
            //更新 -- ENTEXAM_APPLICANTBASE_DETAIL_DAT
            $query  = " DELETE FROM ENTEXAM_APPLICANTBASE_DETAIL_DAT";
            $query .= " WHERE ENTEXAMYEAR   = '{$model->ObjYear}' AND ";
            $query .= "       APPLICANTDIV  = '{$model->field["APPLICANTDIV"]}' AND ";
            $query .= "       EXAMNO        = '{$data_arr[$i]["EXAMNO"]}' AND ";
            $query .= "       SEQ           = '031' ";
            $db->query($query);

            $age = 0;
            if ($model->date_rep($model->getHihunDate($data_arr[$i]["BIRTHDAY"])) != "") {
                $age = ($this->ObjYear."0401" - $data_arr[$i]["BIRTHDAY"]) / 10000; //年齢取得
            }
            
            $data = array();
            $data["ENTEXAMYEAR"][TEXT]      = $model->ObjYear;
            $data["APPLICANTDIV"][TEXT]     = $model->field["APPLICANTDIV"];
            $data["EXAMNO"][TEXT]           = $data_arr[$i]["EXAMNO"];
            $data["SEQ"][TEXT]              = '031';
            $data["REMARK1"][TEXT]          = $data_arr[$i]["HOPE_ORDER1"];
            $data["REMARK2"][TEXT]          = $data_arr[$i]["HOPE_ORDER2"];
            $data["REMARK3"][TEXT]          = $data_arr[$i]["HOPE_ORDER3"];
            $data["REMARK4"][TEXT]          = $data_arr[$i]["HOPE_ORDER4"];
            $data["REMARK5"][TEXT]          = $age > 16 ? "1" : "";
            $data["REMARK6"][TEXT]          = ($data_arr[$i]["SCHOLARSHIP_HOPE"] == "1") ? "1" : "";
            $data["REMARK7"][TEXT]          = $model->courseChkMst[$data_arr[$i]["HOPE_ORDER1"]]["COURSE_TYPE"];
            $data["REMARK8"][TEXT]          = $model->courseChkMst[$data_arr[$i]["HOPE_ORDER2"]]["COURSE_TYPE"];
            $data["REMARK9"][TEXT]          = $model->courseChkMst[$data_arr[$i]["HOPE_ORDER3"]]["COURSE_TYPE"];
            $data["REMARK10"][TEXT]         = $model->courseChkMst[$data_arr[$i]["HOPE_ORDER4"]]["COURSE_TYPE"];

            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][NUMBER]        = "SYSDATE()";

            $where  = " WHERE ENTEXAMYEAR   = '{$model->ObjYear}' AND";
            $where .= "       APPLICANTDIV  = '{$model->field["APPLICANTDIV"]}' AND ";
            $where .= "       EXAMNO        = '{$data_arr[$i]["EXAMNO"]}' AND ";
            $where .= "       SEQ           = '031' ";

            $query = Query::insertSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT");
            $db->query($query);
            
            //SEQ032
            //更新 -- ENTEXAM_APPLICANTBASE_DETAIL_DAT
            $query  = " DELETE FROM ENTEXAM_APPLICANTBASE_DETAIL_DAT";
            $query .= " WHERE ENTEXAMYEAR   = '{$model->ObjYear}' AND ";
            $query .= "       APPLICANTDIV  = '{$model->field["APPLICANTDIV"]}' AND ";
            $query .= "       EXAMNO        = '{$data_arr[$i]["EXAMNO"]}' AND ";
            $query .= "       SEQ           = '032' ";
            $db->query($query);

            $data = array();
            $data["ENTEXAMYEAR"][TEXT]      = $model->ObjYear;
            $data["APPLICANTDIV"][TEXT]     = $model->field["APPLICANTDIV"];
            $data["EXAMNO"][TEXT]           = $data_arr[$i]["EXAMNO"];
            $data["SEQ"][TEXT]              = '032';
            $data["REMARK1"][TEXT]          = $data_arr[$i]["BIRTHDAY"];

            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][NUMBER]        = "SYSDATE()";

            $where  = " WHERE ENTEXAMYEAR   = '{$model->ObjYear}' AND";
            $where .= "       APPLICANTDIV  = '{$model->field["APPLICANTDIV"]}' AND ";
            $where .= "       EXAMNO        = '{$data_arr[$i]["EXAMNO"]}' AND ";
            $where .= "       SEQ           = '032' ";

            $query = Query::insertSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT");
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
        $data = array();
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $cnt = 0;   //処理件数
        for ($i = 0; $i < count($data_arr); $i++) {

            //ENTEXAM_APPLICANTBASE_DAT --DELETE
            $query  = " DELETE FROM ENTEXAM_APPLICANTBASE_DAT ";
            $query .= "       WHERE ENTEXAMYEAR     = '".$model->ObjYear."' AND ";
            $query .= "             APPLICANTDIV    = '".$model->field["APPLICANTDIV"]."' AND ";
            $query .= "             EXAMNO          = '".$data_arr[$i]["EXAMNO"]."' ";
            $db->query($query);

            //ENTEXAM_APPLICANTADDR_DAT --DELETE
            $query  = " DELETE FROM ENTEXAM_APPLICANTADDR_DAT ";
            $query .= "       WHERE ENTEXAMYEAR     = '".$model->ObjYear."' AND ";
            $query .= "             APPLICANTDIV    = '".$model->field["APPLICANTDIV"]."' AND ";
            $query .= "             EXAMNO          = '".$data_arr[$i]["EXAMNO"]."' ";
            $db->query($query);

            //ENTEXAM_APPLICANTBASE_DETAIL_DAT(012) --DELETE
            $query  = " DELETE FROM ENTEXAM_APPLICANTBASE_DETAIL_DAT ";
            $query .= "       WHERE ENTEXAMYEAR     = '".$model->ObjYear."' AND ";
            $query .= "             APPLICANTDIV    = '".$model->field["APPLICANTDIV"]."' AND ";
            $query .= "             EXAMNO          = '".$data_arr[$i]["EXAMNO"]."' AND ";
            $query .= "             SEQ             = '012' ";
            $db->query($query);

            //削除に伴い同一志願者の全てのテーブルから受験番号を除外する
            $testdivNo = intval($data_arr[$i]["TESTDIV"]);
            $query  = "   UPDATE ENTEXAM_APPLICANTBASE_DETAIL_DAT ";
            $query .= "   SET ";
            $query .= "       REMARK{$testdivNo} = NULL ";
            $query .= "   WHERE ";
            $query .= "       ENTEXAMYEAR               = '".$model->ObjYear."' ";
            $query .= "       AND APPLICANTDIV          = '".$model->field["APPLICANTDIV"]."' ";
            $query .= "       AND SEQ                   = '012' ";
            $query .= "       AND REMARK{$testdivNo}    = '".$data_arr[$i]["EXAMNO"]."' ";
            $db->query($query);

            //ENTEXAM_APPLICANTBASE_DETAIL_DAT(031) --DELETE
            $query  = " DELETE FROM ENTEXAM_APPLICANTBASE_DETAIL_DAT ";
            $query .= "       WHERE ENTEXAMYEAR     = '".$model->ObjYear."' AND ";
            $query .= "             APPLICANTDIV    = '".$model->field["APPLICANTDIV"]."' AND ";
            $query .= "             EXAMNO          = '".$data_arr[$i]["EXAMNO"]."' AND ";
            $query .= "             SEQ             = '031' ";
            $db->query($query);
            
            //ENTEXAM_APPLICANTBASE_DETAIL_DAT(032) --DELETE
            $query  = " DELETE FROM ENTEXAM_APPLICANTBASE_DETAIL_DAT ";
            $query .= "       WHERE ENTEXAMYEAR     = '".$model->ObjYear."' AND ";
            $query .= "             APPLICANTDIV    = '".$model->field["APPLICANTDIV"]."' AND ";
            $query .= "             EXAMNO          = '".$data_arr[$i]["EXAMNO"]."' AND ";
            $query .= "             SEQ             = '032' ";
            $db->query($query);

            //ENTEXAM_RECEPT_DAT --DELETE
            $query  = " DELETE FROM ENTEXAM_RECEPT_DAT ";
            $query .= "       WHERE ENTEXAMYEAR      = '{$model->ObjYear}' AND ";
            $query .= "             APPLICANTDIV     = '{$model->field["APPLICANTDIV"]}' AND ";
            $query .= "             EXAM_TYPE        = '1' AND ";
            $query .= "             RECEPTNO         = '{$data_arr[$i]["EXAMNO"]}' ";
            $db->query($query);

            $cnt++;
        }
        $db->commit();
        Query::dbCheckIn($db);
        return $cnt;
    }


    /********************/
    /**  エラー処理用  **/
    /********************/

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
        $data1["PROGRAMID"][TEXT]   = PROGRAMID;
        $data1["MSGROW"][NUMBER]    = $record_no;
        $data1["MSGREMARK"][TEXT]   = $check_error;

        $query = Query::insertSQL($data1, "W_CSVMSG_PRG_DAT");
        $db->query($query);
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

    /**********************/
    /**  存在チェック用  **/
    /**********************/

    //名称マスタ登録コードチェック
    public function nameCntSql($namecd, $setcd, $where="")
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = '{$namecd}' AND ";
        $query .= "     NAMECD2 = '{$setcd}' ";
        if ($where) {
            $query .= $where;
        }

        return $query;
    }

    //設定マスタチェック
    public function entexamSettingCntSql($model, $setcd, $seq, $where="")
    {
        $query .= "   SELECT ";
        $query .= "       COUNT(*) ";
        $query .= "   FROM ";
        $query .= "       ENTEXAM_SETTING_MST ";
        $query .= "   WHERE ";
        $query .= "       ENTEXAMYEAR      = '{$model->ObjYear}' ";
        $query .= "       AND APPLICANTDIV = '{$model->field["APPLICANTDIV"]}' ";
        $query .= "       AND SETTING_CD   = '{$setcd}' ";
        $query .= "       AND SEQ          = '{$seq}' ";
        if ($where) {
            $query .= $where;
        }

        return $query;
    }

    //出身学校コードチェック
    public function fs_cdCntSql($fs_cd)
    {
        $query .= "  SELECT ";
        $query .= "    COUNT(*) ";
        $query .= "  FROM ";
        $query .= "    FINSCHOOL_MST ";
        $query .= "  WHERE ";
        $query .= "    FINSCHOOLCD = '".$fs_cd."' ";

        return $query;
    }

    //志望区分チェック
    public function hopeCourseCdCntSql($model, $courseCd)
    {
        $query  = "   SELECT ";
        $query .= "       GENERAL_CD, ";
        $query .= "       GENERAL_NAME ";
        $query .= "   FROM ";
        $query .= "       ENTEXAM_GENERAL_MST ";
        $query .= "   WHERE ";
        $query .= "       ENTEXAMYEAR      = '{$model->ObjYear}' ";
        $query .= "       AND APPLICANTDIV = '{$model->field["APPLICANTDIV"]}' ";
        $query .= "       AND GENERAL_CD   = '{$courseCd}' ";
        $query .= "       AND TESTDIV      = '0' ";
        $query .= "       AND GENERAL_DIV  = '02' ";

        return $query;
    }

    //入試区分チェック
    public function testDivCntSql($model, $testDiv)
    {
        $query .= "   SELECT ";
        $query .= "       COUNT(*) ";
        $query .= "   FROM ";
        $query .= "       ENTEXAM_TESTDIV_MST ";
        $query .= "   WHERE ";
        $query .= "       ENTEXAMYEAR       = '{$model->ObjYear}' ";
        $query .= "       AND APPLICANTDIV  = '{$model->field["APPLICANTDIV"]}' ";
        $query .= "       AND TESTDIV       = '{$testDiv}' ";

        return $query;
    }

    //コースチェック
    public function getCoureChkSql($model)
    {
        $query  = "   SELECT ";
        $query .= "       G1.GENERAL_CD AS COURSE, ";
        $query .= "       G1.GENERAL_NAME AS COURSE_NAME, ";
        $query .= "       G1.REMARK1 AS COURSE_TYPE, ";
        $query .= "       G2.REMARK1 AS MAJORCD ";
        $query .= "   FROM ";
        $query .= "       ENTEXAM_GENERAL_MST G1 ";
        $query .= "       LEFT JOIN  ENTEXAM_GENERAL_MST G2 ";
        $query .= "       ON G2.ENTEXAMYEAR     = G1.ENTEXAMYEAR ";
        $query .= "       AND G2.APPLICANTDIV   = G1.APPLICANTDIV ";
        $query .= "       AND G2.TESTDIV        = G1.TESTDIV ";
        $query .= "       AND G2.GENERAL_DIV    = '01' ";
        $query .= "       AND G2.GENERAL_CD     = G1.REMARK1 ";
        $query .= "   WHERE ";
        $query .= "       G1.ENTEXAMYEAR        = '{$model->ObjYear}' ";
        $query .= "       AND G1.APPLICANTDIV   = '{$model->field["APPLICANTDIV"]}' ";
        $query .= "       AND G1.TESTDIV        = '0' ";
        $query .= "       AND G1.GENERAL_DIV    = '02' ";

        return $query;
    }
    

    /********************/
    /**  更新時に使用  **/
    /********************/

    //名称マスタより取得
    public function getNameMstAll($year, $namecd1)
    {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR    = '".$year."' AND ";
        $query .= "     NAMECD1 = '".$namecd1."' ";
        $query .= " ORDER BY ";
        $query .= "     NAMECD2 ";

        return $query;
    }

    //件数取得 -- ENTEXAM_APPLICANTBASE_DAT
    public function cntEntexamApplicantbaseDat($model, $data_arr)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DAT ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR     = '{$model->ObjYear}' AND ";
        $query .= "     APPLICANTDIV    = '{$model->field["APPLICANTDIV"]}' AND ";
        $query .= "     EXAMNO          = '{$data_arr["EXAMNO"]}' ";

        return $query;
    }

    //件数取得 -- ENTEXAM_APPLICANTADDR_DAT
    public function cntEntexamApplicantaddrDat($model, $data_arr)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTADDR_DAT ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR     = '{$model->ObjYear}' AND ";
        $query .= "     APPLICANTDIV    = '{$model->field["APPLICANTDIV"]}' AND ";
        $query .= "     EXAMNO          = '{$data_arr["EXAMNO"]}' ";

        return $query;
    }

    //件数取得 -- ENTEXAM_APPLICANTBASE_DETAIL_DAT
    public function cntEntexamApplicantbaseDetailDat($model, $data_arr)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DETAIL_DAT ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR     = '{$model->ObjYear}' AND ";
        $query .= "     APPLICANTDIV    = '{$model->field["APPLICANTDIV"]}' AND ";
        $query .= "     EXAMNO          = '{$data_arr["EXAMNO"]}' AND ";
        $query .= "     SEQ             = '031' ";

        return $query;
    }

    //件数取得 -- ENTEXAM_SCORE_DAT
    public function cntEntexamScoreDat($model, $data_arr)
    {
        $query  = "  SELECT ";
        $query .= "    COUNT(*) ";
        $query .= "  FROM ";
        $query .= "    ENTEXAM_SCORE_DAT ";
        $query .= "  WHERE ";
        $query .= "    ENTEXAMYEAR      = '{$model->ObjYear}' AND ";
        $query .= "    APPLICANTDIV     = '{$model->field["APPLICANTDIV"]}' AND ";
        $query .= "    TESTDIV          = '{$data_arr["TESTDIV"]}' AND ";
        $query .= "    EXAM_TYPE        = '1' AND ";
        $query .= "    RECEPTNO         = '{$data_arr["EXAMNO"]}' ";

        return $query;
    }

    //出身学校名取得
    public function getFsName($data_arr)
    {
        $query  = " SELECT ";
        $query .= "     FINSCHOOL_NAME ";
        $query .= " FROM ";
        $query .= "     FINSCHOOL_MST ";
        $query .= " WHERE ";
        $query .= "     FINSCHOOLCD = '{$data_arr["FS_CD"]}' ";

        return $query;
    }

    //各入試区分で同じ中学コード・氏名・性別・生年月日の受験番号を取得
    public function getSameApplicantExamno($model, $examno, $testdiv)
    {
        $query  = "   SELECT DISTINCT ";
        $query .= "       B2.TESTDIV, ";
        $query .= "       B2.EXAMNO ";
        $query .= "   FROM ";
        $query .= "       ENTEXAM_APPLICANTBASE_DAT B1 ";
        $query .= "       INNER JOIN ENTEXAM_APPLICANTBASE_DAT B2 ";
        $query .= "           ON B2.ENTEXAMYEAR   = B1.ENTEXAMYEAR ";
        $query .= "           AND B2.APPLICANTDIV = B1.APPLICANTDIV ";
        $query .= "   WHERE ";
        $query .= "       B1.ENTEXAMYEAR       = '{$model->ObjYear}' ";
        $query .= "       AND B1.APPLICANTDIV  = '{$model->field["APPLICANTDIV"]}' ";
        $query .= "       AND B1.EXAMNO        = '{$examno}' ";
        $query .= "       AND B1.TESTDIV       = '{$testdiv}' ";
        $query .= "       AND B1.FS_CD         = B2.FS_CD ";
        $query .= "       AND B1.NAME          = B2.NAME ";
        $query .= "       AND B1.SEX           = B2.SEX ";
        $query .= "       AND B1.BIRTHDAY      = B2.BIRTHDAY ";

        return $query;
    }
}
