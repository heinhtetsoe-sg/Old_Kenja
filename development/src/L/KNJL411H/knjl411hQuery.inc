<?php

class knjl411hquery extends Query
{

    //入試設定マスタ取得
    public function getEntSettingMst($applicantDiv, $settingCd, $seq = "")
    {
        $query  = " SELECT ";
        $query .= "     SEQ VALUE, ";
        $query .= "     SEQ || ':' || NAME1 LABEL, ";
        $query .= "     NAME1 ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_SETTING_MST ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR         = '".(CTRL_YEAR + 1)."' ";
        $query .= "     AND APPLICANTDIV    = '{$applicantDiv}' ";
        $query .= "     AND SETTING_CD      = '{$settingCd}' ";
        if ($seq != "") {
            $query .= "     AND SEQ             = '{$seq}' ";
        }

        return $query;
    }

    //名称マスタ取得(入試制度)
    public function getApplicantDivMst()
    {
        $query  = "";
        $query .= " SELECT ";
        $query .= "     NAMECD2 VALUE ";
        $query .= "   , NAMECD2 || ':' || NAME1 LABEL ";
        $query .= " FROM V_NAME_MST ";
        $query .= " WHERE YEAR = '".(CTRL_YEAR + 1)."' ";
        $query .= "   AND NAMECD1 = 'L003' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //入試制度名の取得
    public function getApplicantDivName($applicantDiv)
    {
        $query  = "";
        $query .= " SELECT ";
        $query .= "     NAME1 ";
        $query .= " FROM V_NAME_MST ";
        $query .= " WHERE YEAR    = '".(CTRL_YEAR + 1)."' ";
        $query .= "   AND NAMECD1 = 'L003' ";
        $query .= "   AND NAMECD2 = '".$applicantDiv."' ";

        return $query;
    }

    //入試区分マスタ取得
    public function getEntexamTestDivMst($applicantDiv)
    {
        $query  = "";
        $query .= " SELECT ";
        $query .= "     TESTDIV VALUE ";
        $query .= "   , TESTDIV || ':' || TESTDIV_ABBV LABEL ";
        $query .= " FROM ENTEXAM_TESTDIV_MST ";
        $query .= " WHERE ENTEXAMYEAR = '".(CTRL_YEAR + 1)."' ";
        if ($applicantDiv != "") {
            $query .= "   AND APPLICANTDIV = '{$applicantDiv}' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //入試区分マスタ取得
    public function getEntexamTestDivName($applicantDiv, $testDiv)
    {
        $query  = "";
        $query .= " SELECT ";
        $query .= "     TESTDIV_ABBV ";
        $query .= " FROM ENTEXAM_TESTDIV_MST ";
        $query .= " WHERE ENTEXAMYEAR = '".(CTRL_YEAR + 1)."' ";
        if ($applicantDiv != "") {
            $query .= "   AND APPLICANTDIV = '{$applicantDiv}' ";
        }
        if ($testDiv != "") {
            $query .= "   AND TESTDIV = '{$testDiv}' ";
        }
        return $query;
    }

    //名称マスタL007取得
    public function getNameMst($model, $nameCd1)
    {
        $query  = "   SELECT ";
        $query .= "       NAMECD2, ";
        $query .= "       NAME1, ";
        $query .= "       NAME2, ";
        $query .= "       NAMESPARE1, ";
        $query .= "       NAMESPARE2, ";
        $query .= "       NAMESPARE3 ";
        $query .= "   FROM ";
        $query .= "       V_NAME_MST ";
        $query .= "   WHERE ";
        $query .= "       YEAR = '{$model->examyear}' ";
        $query .= "       AND NAMECD1 = '{$nameCd1}' ";

        return $query;
    }

    //受験型取得
    public function getExamTypeMst($model)
    {
        $query .= "   SELECT ";
        $query .= "       EXAM_TYPE, ";
        $query .= "       EXAMTYPE_NAME, ";
        $query .= "       REMARK2 AS MAPPING_NAME ";
        $query .= "   FROM ";
        $query .= "       ENTEXAM_EXAMTYPE_MST ";
        $query .= "   WHERE ";
        $query .= "           ENTEXAMYEAR  = '{$model->examyear}' ";
        $query .= "       AND APPLICANTDIV = '{$model->field["APPLICANTDIV"]}' ";

        return $query;
    }

    //受験コース取得
    public function getExamCourseMst($model)
    {
        $query  = "   SELECT ";
        $query .= "       EXAMCOURSECD, ";
        $query .= "       EXAMCOURSE_NAME, ";
        $query .= "       MAPPING_NAME ";
        $query .= "   FROM ";
        $query .= "       ENTEXAM_COURSE_MST ";
        $query .= "   WHERE ";
        $query .= "           ENTEXAMYEAR  = '{$model->examyear}' ";
        $query .= "       AND APPLICANTDIV = '{$model->field["APPLICANTDIV"]}' ";

        return $query;
    }

    /* データ更新処理 */
    //願書の追加（ＣＳＶデータより読込）
    public function updateQueryCsv($model, &$data_arr)
    {
        $data = array();
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $cnt = 0;
        for ($i = 0; $i < count($data_arr); $i++) {
            //どの試験回にも出願されない志願者はスルー
            if (count($data_arr[$i]["RECEPT_DATA"]) == 0) {
                continue;
            }
            //行単位更新
            knjl411hQuery::updateRow($db, $model, $data_arr[$i]);

            $cnt++;
        }

        $db->commit();
        Query::dbCheckIn($db);

        return $cnt;
    }

    //行単位更新
    public function updateRow($db, $model, &$data_arr_i)
    {
        //更新 ENTEXAM_APPLICANTBASE_DAT
        knjl411hQuery::getUpdateApplicantBaseQuery($db, $model, $data_arr_i);
    
        //更新 ENTEXAM_APPLICANTBASE_DETAIL_DAT
        if ($model->field["APPLICANTDIV"] == "1") {
            knjl411hQuery::getUpdateApplicantBaseDetail008Query($db, $model, $data_arr_i);
        }
        knjl411hQuery::getUpdateApplicantBaseDetail009Query($db, $model, $data_arr_i);
    
        //更新 ENTEXAM_APPLICANTADDR_DAT
        knjl411hQuery::getUpdateApplicantAddrQuery($db, $model, $data_arr_i);
        foreach ($data_arr_i["RECEPT_DATA"] as $testDiv => $receptData) {
            $receptData["TESTDIV"]      = $testDiv;
            //更新 ENTEXAM_RECEPT_DAT
            knjl411hQuery::getUpdateEntexamReceptQuery($db, $model, $receptData);
               
            //各試験毎の志望コースを登録
            $seqArray = array();
            $seqArray[] = "015"; //第一志望 登録SEQ
            $seqArray[] = "016"; //第二志望 登録SEQ
            if ($model->field["APPLICANTDIV"] == "2") {
                $seqArray[] = "017"; //第三志望 登録SEQ (高校のみ)
            }
            for ($i = 1; $i <= count($seqArray); $i++) {
                $seq = $seqArray[$i - 1];
                $receptData["EXAMCOURSECD"] = $receptData["HOPE_COURSE_{$i}"];
                $receptData["SHDIV"]        = $receptData["SHDIV_{$i}"];
                if ($receptData["EXAMCOURSECD"] && $receptData["SHDIV"]) { //志望コースと出願区分がセットされていない場合はレコードを追加しない
                    knjl411hQuery::getUpdateEntexamReceptDetailQuery($db, $model, $seq, $receptData);
                }
            }
        }
    }

    //追加 ENTEXAM_APPLICANTBASE_DAT
    public function getUpdateApplicantBaseQuery($db, $model, $data_arr_i)
    {
        $data = array();
        $data["ENTEXAMYEAR"][TEXT]          = $model->examyear;
        $data["APPLICANTDIV"][TEXT]         = $model->field["APPLICANTDIV"];
        $data["EXAMNO"][TEXT]               = $data_arr_i["EXAMNO"];
        $data["TESTDIV"][TEXT]              = "1";
        $data["SHDIV"][TEXT]                = "1";
        $data["DESIREDIV"][TEXT]            = "1";
        $data["NAME"][TEXT]                 = $data_arr_i["NAME"];
        $data["NAME_KANA"][TEXT]            = $data_arr_i["NAME_KANA"];
        $data["SEX"][TEXT]                  = $data_arr_i["SEX"];
        $data["ERACD"][TEXT]                = $data_arr_i["ERACD"];
        $data["BIRTH_Y"][TEXT]              = $data_arr_i["BIRTH_Y"];
        $data["BIRTH_M"][TEXT]              = $data_arr_i["BIRTH_M"];
        $data["BIRTH_D"][TEXT]              = $data_arr_i["BIRTH_D"];
        $data["BIRTHDAY"][DATE]             = $data_arr_i["BIRTHDAY"];
        $data["FS_CD"][TEXT]                = $data_arr_i["FS_CD"];
        $data["FS_NAME"][TEXT]              = $data_arr_i["FS_NAME"];
        $data["FS_GRDYEAR"][TEXT]           = $data_arr_i["FS_GRDYEAR"];
        $data["FS_ERACD"][TEXT]             = $data_arr_i["FS_ERACD"];
        $data["FS_Y"][TEXT]                 = $data_arr_i["FS_Y"];
        $data["FS_M"][TEXT]                 = $data_arr_i["FS_M"];
        $data["FS_DAY"][TEXT]               = $data_arr_i["FS_DAY"];
        $data["FS_GRDDIV"][TEXT]            = $data_arr_i["FS_GRDDIV"];

        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][FUNC]              = "SYSDATE()";

        $where  = " WHERE ENTEXAMYEAR   = '{$model->examyear}' AND ";
        $where .= "       APPLICANTDIV  = '{$model->field["APPLICANTDIV"]}' AND ";
        $where .= "       EXAMNO        = '{$data_arr_i["EXAMNO"]}' ";

        $base_cnt = $db->getOne(knjl411hQuery::cntEntexamApplicantbaseDat($model, $data_arr_i["EXAMNO"]));
        if (0 == $base_cnt) {
            $query = Query::insertSQL($data, "ENTEXAM_APPLICANTBASE_DAT");
        } else {
            $query = Query::updateSQL($data, "ENTEXAM_APPLICANTBASE_DAT", $where);
        }

        $db->query($query);
    }

    //追加 ENTEXAM_APPLICANTBASE_DETAIL_DAT(SEQ008)
    public function getUpdateApplicantBaseDetail008Query($db, $model, $data_arr_i)
    {
        $seq = "008";
       
        $data = array();
        $data["ENTEXAMYEAR"][TEXT]      = $model->examyear;
        $data["APPLICANTDIV"][TEXT]     = $model->field["APPLICANTDIV"];
        $data["SEQ"][TEXT]              = $seq;
        $data["EXAMNO"][TEXT]           = $data_arr_i["EXAMNO"];
        $data["REMARK1"][TEXT]          = $data_arr_i["PS_CD"];
        $data["REMARK2"][TEXT]          = $data_arr_i["PS_NAME"];
        $data["REMARK3"][TEXT]          = $data_arr_i["PS_NAME_OTHER"];
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][FUNC]          = "SYSDATE()";

        $where  = " WHERE ENTEXAMYEAR   = '{$model->examyear}' AND ";
        $where .= "       APPLICANTDIV  = '{$model->field["APPLICANTDIV"]}' AND ";
        $where .= "       EXAMNO        = '{$data_arr_i["EXAMNO"]}' AND ";
        $where .= "       SEQ           = '{$seq}' ";

        $cnt = $db->getOne(knjl411hQuery::cntEntexamApplicantbaseDetailDat($model, $data_arr_i["EXAMNO"], $seq));
        if (0 == $cnt) {
            $query = Query::insertSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT");
        } else {
            $query = Query::updateSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT", $where);
        }

        $db->query($query);
    }

    //追加 ENTEXAM_APPLICANTBASE_DETAIL_DAT(SEQ009)
    public function getUpdateApplicantBaseDetail009Query($db, $model, $data_arr_i)
    {
        $seq = "009";
       
        $data = array();
        $data["ENTEXAMYEAR"][TEXT]      = $model->examyear;
        $data["APPLICANTDIV"][TEXT]     = $model->field["APPLICANTDIV"];
        $data["SEQ"][TEXT]              = $seq;
        $data["EXAMNO"][TEXT]           = $data_arr_i["EXAMNO"];
        $data["REMARK2"][TEXT]          = $data_arr_i["LOGIN_SEQ"];
        $data["REMARK3"][TEXT]          = $data_arr_i["LOGIN_MAIL"];
        $data["REMARK4"][TEXT]          = $data_arr_i["APP_MAIL"];
        $data["REMARK8"][TEXT]          = $data_arr_i["ROUNIN_FLG"];
        $data["REMARK9"][TEXT]          = $data_arr_i["FS_NAME"];
        $data["REMARK10"][TEXT]         = $data_arr_i["FS_NAME_OTHER"];

        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][FUNC]          = "SYSDATE()";

        $where  = " WHERE ENTEXAMYEAR   = '{$model->examyear}' AND ";
        $where .= "       APPLICANTDIV  = '{$model->field["APPLICANTDIV"]}' AND ";
        $where .= "       EXAMNO        = '{$data_arr_i["EXAMNO"]}' AND ";
        $where .= "       SEQ           = '{$seq}' ";

        $cnt = $db->getOne(knjl411hQuery::cntEntexamApplicantbaseDetailDat($model, $data_arr_i["EXAMNO"], $seq));
        if (0 == $cnt) {
            $query = Query::insertSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT");
        } else {
            $query = Query::updateSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT", $where);
        }

        $db->query($query);
    }

    public function getUpdateApplicantAddrQuery($db, $model, $data_arr_i)
    {
        //ENTEXAM_APPLICANTADDR_DAT
        $data = array();
        $data["ENTEXAMYEAR"][TEXT]      = $model->examyear;
        $data["APPLICANTDIV"][TEXT]     = $model->field["APPLICANTDIV"];
        $data["EXAMNO"][TEXT]           = $data_arr_i["EXAMNO"];
        $data["ZIPCD"][TEXT]            = $data_arr_i["ZIPCD"];
        $data["ADDRESS1"][TEXT]         = $data_arr_i["ADDRESS1"];
        $data["ADDRESS2"][TEXT]         = $data_arr_i["ADDRESS2"];
        $data["TELNO"][TEXT]            = $data_arr_i["TELNO"];
        $data["GNAME"][TEXT]            = $data_arr_i["GNAME"];
        $data["GKANA"][TEXT]            = $data_arr_i["GKANA"];
        $data["GZIPCD"][TEXT]           = $data_arr_i["ZIPCD"];
        $data["GADDRESS1"][TEXT]        = $data_arr_i["ADDRESS1"];
        $data["GADDRESS2"][TEXT]        = $data_arr_i["ADDRESS2"];
        $data["EMERGENCYTELNO"][TEXT]   = $data_arr_i["EMERGENCYTELNO"];
        $data["EMERGENCYCALL"][TEXT] = $data_arr_i["EMERGENCYCALL"];
        $data["GRELATIONSHIP"][TEXT]    = $data_arr_i["GRELATIONSHIP"];
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][FUNC]          = "SYSDATE()";

        $where  = " WHERE ENTEXAMYEAR   = '{$model->examyear}' AND";
        $where .= "       APPLICANTDIV  = '{$model->field["APPLICANTDIV"]}' AND ";
        $where .= "       EXAMNO        = '{$data_arr_i["EXAMNO"]}' ";

        $addr_cnt = $db->getOne(knjl411hQuery::cntEntexamApplicantAddrDat($model, $data_arr_i["EXAMNO"]));
        if (0 == $addr_cnt) {
            $query = Query::insertSQL($data, "ENTEXAM_APPLICANTADDR_DAT");
        } else {
            $query = Query::updateSQL($data, "ENTEXAM_APPLICANTADDR_DAT", $where);
        }

        $db->query($query);
    }

    public function getUpdateEntexamReceptQuery($db, $model, $receptData)
    {
        $data = array();
        $data["ENTEXAMYEAR"][TEXT]      = $model->examyear;
        $data["APPLICANTDIV"][TEXT]     = $model->field["APPLICANTDIV"];
        $data["TESTDIV"][TEXT]          = $receptData["TESTDIV"];
        $data["EXAM_TYPE"][TEXT]        = $receptData["EXAM_TYPE"];
        $data["RECEPTNO"][TEXT]         = $receptData["RECEPTNO"];
        $data["EXAMNO"][TEXT]           = $receptData["EXAMNO"];
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][FUNC]          = "SYSDATE()";

        $where  = " WHERE ENTEXAMYEAR   = '{$model->examyear}' AND ";
        $where .= "       APPLICANTDIV  = '{$model->field["APPLICANTDIV"]}' AND ";
        $where .= "       TESTDIV       = '{$receptData["TESTDIV"]}' AND ";
        $where .= "       EXAM_TYPE     = '{$receptData["EXAM_TYPE"]}' AND ";
        $where .= "       RECEPTNO      = '{$receptData["RECEPTNO"]}' ";
       
        $recept_cnt = $db->getOne(knjl411hQuery::cntEntexamReceptDat($model, $receptData));
        if (0 == $recept_cnt) {
            $query = Query::insertSQL($data, "ENTEXAM_RECEPT_DAT");
        } else {
            $query = Query::updateSQL($data, "ENTEXAM_RECEPT_DAT", $where);
        }

        $db->query($query);
    }

    public function getUpdateEntexamReceptDetailQuery($db, $model, $seq, $receptData)
    {
        $data = array();
        $data["ENTEXAMYEAR"][TEXT]      = $model->examyear;
        $data["APPLICANTDIV"][TEXT]     = $model->field["APPLICANTDIV"];
        $data["TESTDIV"][TEXT]          = $receptData["TESTDIV"];
        $data["EXAM_TYPE"][TEXT]        = $receptData["EXAM_TYPE"];
        $data["RECEPTNO"][TEXT]         = $receptData["RECEPTNO"];
        $data["SEQ"][TEXT]              = $seq;
        $data["REMARK1"][TEXT]          = $receptData["EXAMCOURSECD"];
        $data["REMARK2"][TEXT]          = $receptData["SHDIV"];
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][FUNC]          = "SYSDATE()";

        $where  = " WHERE ENTEXAMYEAR   = '{$model->examyear}' AND ";
        $where .= "       APPLICANTDIV  = '{$model->field["APPLICANTDIV"]}' AND ";
        $where .= "       TESTDIV       = '{$receptData["TESTDIV"]}' AND ";
        $where .= "       EXAM_TYPE     = '{$receptData["EXAM_TYPE"]}' AND ";
        $where .= "       RECEPTNO      = '{$receptData["RECEPTNO"]}' AND ";
        $where .= "       SEQ           = '{$seq}' ";
       
        $recept_cnt = $db->getOne(knjl411hQuery::cntEntexamReceptDetailDat($model, $receptData, $seq));
        if (0 == $recept_cnt) {
            $query = Query::insertSQL($data, "ENTEXAM_RECEPT_DETAIL_DAT");
        } else {
            $query = Query::updateSQL($data, "ENTEXAM_RECEPT_DETAIL_DAT", $where);
        }

        $db->query($query);
    }

    /* データ削除処理 */
    public function delete($model)
    {
        $db = Query::dbCheckOut();

        foreach ($model->entexamApplicantList as $applicant) {

            /*** ENTEXAM_APPLICANTBASE_DAT ***/
            $data = array();
            $data["ENTEXAMYEAR"][TEXT]  = $applicant["ENTEXAMYEAR"];
            $data["APPLICANTDIV"][TEXT] = $applicant["APPLICANTDIV"];
            $data["TESTDIV"][TEXT]      = $applicant["TESTDIV"];

            $applicant["EXAMNO"] = substr($applicant["EXAMNO"], 6);
            $data["EXAMNO"][TEXT]       = $applicant["EXAMNO"];


            $query  = " DELETE FROM ENTEXAM_APPLICANTBASE_DAT ";
            $query .= " WHERE ENTEXAMYEAR = '{$applicant["ENTEXAMYEAR"]}}'";
            $query .= "   AND APPLICANTDIV = '{$applicant["APPLICANTDIV"]}'";
            $query .= "   AND EXAMNO = '{$applicant["EXAMNO"]}'";
            $db->query($query);

            /*** ENTEXAM_APPLICANTBASE_DETAIL_DAT ***/
            $query  = " DELETE FROM ENTEXAM_APPLICANTBASE_DETAIL_DAT ";
            $query .= " WHERE ENTEXAMYEAR = '{$applicant["ENTEXAMYEAR"]}}'";
            $query .= "   AND APPLICANTDIV = '{$applicant["APPLICANTDIV"]}'";
            $query .= "   AND EXAMNO = '{$applicant["EXAMNO"]}'";
            $db->query($query);

            /*** ENTEXAM_APPLICANTADDR_DAT ***/
            $query  = " DELETE FROM ENTEXAM_APPLICANTADDR_DAT ";
            $query .= " WHERE ENTEXAMYEAR = '{$applicant["ENTEXAMYEAR"]}}'";
            $query .= "   AND APPLICANTDIV = '{$applicant["APPLICANTDIV"]}'";
            $query .= "   AND EXAMNO = '{$applicant["EXAMNO"]}'";
            $db->query($query);
        }

        Query::dbCheckIn($db);
    }

    /*================================== 存在チェック ==================================*/

    //件数取得 -- ENTEXAM_APPLICANTBASE_DAT
    public function cntEntexamApplicantbaseDat($model, $examno)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DAT ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR     = '{$model->examyear}' AND ";
        $query .= "     APPLICANTDIV    = '{$model->field["APPLICANTDIV"]}' AND ";
        $query .= "     EXAMNO          = '{$examno}' ";

        return $query;
    }

    //件数取得 -- ENTEXAM_APPLICANTBASE_DETAIL_DAT
    public function cntEntexamApplicantbaseDetailDat($model, $examno, $seq)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DETAIL_DAT ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR     = '{$model->examyear}' AND ";
        $query .= "     APPLICANTDIV    = '{$model->field["APPLICANTDIV"]}' AND ";
        $query .= "     EXAMNO          = '{$examno}' AND ";
        $query .= "     SEQ             = '{$seq}' ";

        return $query;
    }

    //件数取得 -- ENTEXAM_APPLICANTADDR_DAT
    public function cntEntexamApplicantAddrDat($model, $examno)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTADDR_DAT ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR     = '{$model->examyear}' AND ";
        $query .= "     APPLICANTDIV    = '{$model->field["APPLICANTDIV"]}' AND ";
        $query .= "     EXAMNO          = '{$examno}' ";

        return $query;
    }

    //件数取得 -- ENTEXAM_RECEPT_DAT
    public function cntEntexamReceptDat($model, $receptData)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_RECEPT_DAT ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR     = '{$model->examyear}' AND ";
        $query .= "     APPLICANTDIV    = '{$model->field["APPLICANTDIV"]}' AND ";
        $query .= "     TESTDIV         = '{$receptData["TESTDIV"]}' AND ";
        $query .= "     EXAM_TYPE       = '{$receptData["EXAM_TYPE"]}' AND ";
        $query .= "     RECEPTNO        = '{$receptData["RECEPTNO"]}' ";

        return $query;
    }

    //件数取得 -- ENTEXAM_RECEPT_DAT
    public function cntEntexamReceptDetailDat($model, $receptData, $seq)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_RECEPT_DETAIL_DAT ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR     = '{$model->examyear}' AND ";
        $query .= "     APPLICANTDIV    = '{$model->field["APPLICANTDIV"]}' AND ";
        $query .= "     TESTDIV         = '{$receptData["TESTDIV"]}' AND ";
        $query .= "     EXAM_TYPE       = '{$receptData["EXAM_TYPE"]}' AND ";
        $query .= "     RECEPTNO        = '{$receptData["RECEPTNO"]}' AND ";
        $query .= "     SEQ             = '{$seq}' ";

        return $query;
    }

    /*================================== エラーデータ処理 ==================================*/
    //エラーデータの削除
    public function deleteCsvMsgPrgDat()
    {
        $query  = "DELETE FROM W_CSVMSG_PRG_DAT ";
        $query .= "WHERE PROGRAMID = 'KNJL411H'";
        return $query;
    }

    //エラーＤＢへの追加
    public function insertCsvMsgPrgDat(&$db, $line, $check_error)
    {
        $data1 = array();
        $data1["PROGRAMID"][TEXT] = 'KNJL411H';
        $data1["MSGROW"][NUMBER]  = $line;
        $data1["MSGREMARK"][TEXT] = (strlen($check_error) > 119) ? substr($check_error, 0, 119)." " : $check_error." ";

        $query = Query::insertSQL($data1, "W_CSVMSG_PRG_DAT");
        $db->query($query);

        return;
    }

    //CVS作成用のQUERY
    public function selectCsvMsgPrgDat()
    {
        $query  = "SELECT MSGROW, MSGREMARK ";
        $query .= "FROM W_CSVMSG_PRG_DAT ";
        $query .= "WHERE PROGRAMID = 'KNJL411H' ";
        return $query;
    }
}
