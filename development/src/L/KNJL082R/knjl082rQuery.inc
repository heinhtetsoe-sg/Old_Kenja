<?php

require_once('for_php7.php');

class knjl082rquery extends Query
{
    public function getNameMst($model, $namecd1, $nameCd2='')
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL, ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     NAMESPARE2, ";
        $query .= "     NAME1 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR    = '{$model->ObjYear}' ";
        $query .= "     AND NAMECD1 = '{$namecd1}' ";
        if ($nameCd2) {
            $query .= "     AND NAMECD2 = '{$nameCd2}' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //願書の追加（ＣＳＶデータより読込）
    public function updateQueryCsv($model, &$data_arr)
    {
        $data = array();
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $query  = " DELETE FROM ";
        $query .= "     ENTEXAM_CKIP_CSV2_DAT ";
        $query .= " WHERE ";
        $query .= "         ENTEXAMYEAR  = '{$model->ObjYear}' ";
        $query .= "     AND APPLICANTDIV = '{$model->field["APPLICANTDIV"]}' ";

        $db->query($query);

        $cnt = 0;   //処理件数
        for ($i = 0; $i < get_count($data_arr); $i++) {
            //ENTEXAM_CKIP_CSV2_DAT
            $data = array();
            $data["LOGIN_ID"][TEXT]           = $data_arr[$i]["LOGIN_ID"];
            $data["ENTEXAMYEAR"][TEXT]        = $data_arr[$i]["ENTEXAMYEAR"];
            $data["APPLICANTDIV"][TEXT]       = $data_arr[$i]["APPLICANTDIV"];
            $data["TESTDIV"][TEXT]            = $data_arr[$i]["TESTDIV"];
            $data["TESTDIV1"][TEXT]           = $data_arr[$i]["TESTDIV1"];
            $data["DESIREDIV"][TEXT]          = $data_arr[$i]["DESIREDIV"];
            $data["SHDIV"][TEXT]              = $data_arr[$i]["SHDIV"];
            $data["EXAMNO"][TEXT]             = $data_arr[$i]["EXAMNO"];
            $data["EXAMHALL_NAME"][TEXT]      = $data_arr[$i]["EXAMHALL_NAME"];
            $data["EXAMHALL_CLASSNAME"][TEXT] = $data_arr[$i]["EXAMHALL_CLASSNAME"];
            $data["NAME"][TEXT]               = $data_arr[$i]["NAME"];
            $data["NAME_KANA"][TEXT]          = $data_arr[$i]["NAME_KANA"];
            $data["SEX"][TEXT]                = $data_arr[$i]["SEX"];
            $data["BIRTHDAY"][DATE]           = $data_arr[$i]["BIRTHDAY"];
            $data["ZIPCD"][TEXT]              = $data_arr[$i]["ZIPCD"];
            $data["ADDRESS1"][TEXT]           = $data_arr[$i]["ADDRESS1"];
            $data["ADDRESS2"][TEXT]           = $data_arr[$i]["ADDRESS2"];
            $data["ADDRESS3"][TEXT]           = $data_arr[$i]["ADDRESS3"];
            $data["TELNO"][TEXT]              = $data_arr[$i]["TELNO"];
            $data["GTELNO"][TEXT]             = $data_arr[$i]["GTELNO"];
            $data["GTELNO2"][TEXT]            = $data_arr[$i]["GTELNO2"];
            $data["FS_CD"][TEXT]              = $data_arr[$i]["FS_CD"];
            $data["FS_NAME"][TEXT]            = $data_arr[$i]["FS_NAME"];
            $data["FS_TELNO"][TEXT]           = $data_arr[$i]["FS_TELNO"];
            $data["FS_ZIPCD"][TEXT]           = $data_arr[$i]["FS_ZIPCD"];
            $data["FS_ADDR1"][TEXT]           = $data_arr[$i]["FS_ADDR1"];
            $data["FS_DAY"][DATE]             = $data_arr[$i]["FS_DAY"];
            $data["FS_GRDNAME"][TEXT]         = $data_arr[$i]["FS_GRDNAME"];
            $data["GNAME"][TEXT]              = $data_arr[$i]["GNAME"];
            $data["GKANA"][TEXT]              = $data_arr[$i]["GKANA"];
            $data["GZIPCD"][TEXT]             = $data_arr[$i]["GZIPCD"];
            $data["GADDRESS1"][TEXT]          = $data_arr[$i]["GADDRESS1"];
            $data["GADDRESS2"][TEXT]          = $data_arr[$i]["GADDRESS2"];
            $data["GADDRESS3"][TEXT]          = $data_arr[$i]["GADDRESS3"];
            $data["RELATIONSHIP_NAME"][TEXT]  = $data_arr[$i]["RELATIONSHIP_NAME"];
            $data["ENT_MONEY_STATUS"][TEXT]   = $data_arr[$i]["ENT_MONEY_STATUS"];
            $data["PRI_NAME"][TEXT]           = $data_arr[$i]["PRI_NAME"];
            $data["PRI_CLASSNAME"][TEXT]      = $data_arr[$i]["PRI_CLASSNAME"];
            $data["REMARK1"][TEXT]            = $data_arr[$i]["REMARK1"];
            $data["REMARK2"][TEXT]            = $data_arr[$i]["REMARK2"];
            $data["REMARK3"][TEXT]            = $data_arr[$i]["REMARK3"];
            $data["REMARK4"][TEXT]            = $data_arr[$i]["REMARK4"];
            $data["REMARK5"][TEXT]            = $data_arr[$i]["REMARK5"];
            $data["REMARK6"][TEXT]            = $data_arr[$i]["REMARK6"];
            $data["REMARK7"][TEXT]            = $data_arr[$i]["REMARK7"];
            $data["REMARK8"][TEXT]            = $data_arr[$i]["REMARK8"];
            $data["REMARK9"][TEXT]            = $data_arr[$i]["REMARK9"];
            $data["REMARK10"][TEXT]           = $data_arr[$i]["REMARK10"];
            $data["REGISTERCD"][TEXT]         = STAFFCD;
            $data["UPDATED"][NUMBER]          = "SYSDATE()";

            $query = Query::insertSQL($data, "ENTEXAM_CKIP_CSV2_DAT");
            $db->query($query);

            $cnt++;
        }

        $db->commit();
        Query::dbCheckIn($db);

        return $cnt;
    }

    public function getCkipCsv($model)
    {
        $query  = " SELECT ";
        $query .= "     T1.* ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_CKIP_CSV2_DAT T1 ";
        $query .= " WHERE ";
        $query .= "         T1.ENTEXAMYEAR  = '{$model->ObjYear}' ";
        $query .= "     AND T1.APPLICANTDIV = '{$model->field["APPLICANTDIV"]}' ";

        return $query;
    }

    //カウント取得
    public function getCntData($table, $year, $applicantDiv, $examNo, $seq = '')
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     {$table} ";
        $query .= " WHERE ";
        $query .= "         ENTEXAMYEAR   = '{$year}' ";
        $query .= "     AND APPLICANTDIV  = '{$applicantDiv}' ";
        $query .= "     AND EXAMNO        = '{$examNo}' ";
        if ($seq != '') {
            $query .= " AND SEQ           = '{$seq}' ";
        }

        return $query;
    }

    //ENTEXAM_APPLICANTBASE_DATカウント取得
    public function getCntBaseData($table, $year, $applicantDiv, $examNo)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     {$table} ";
        $query .= " WHERE ";
        $query .= "         ENTEXAMYEAR   = '{$year}' ";
        $query .= "     AND APPLICANTDIV  = '{$applicantDiv}' ";
        $query .= "     AND EXAMNO        = '{$examNo}' ";
        //手続未確定の人だけを対象とする。
        $query .= "     AND PROCEDUREDIV  IS NULL ";

        return $query;
    }

    //入学コース取得
    public function getEntCourseMst($year, $applicantDiv, $testDiv)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     ENTER_COURSECD || ':' || ENTER_MAJORCD || ':' || ENTER_COURSECODE as CMC_CD ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_COURSE_MST ";
        $query .= " WHERE ";
        $query .= "         ENTEXAMYEAR   = '{$year}' ";
        $query .= "     AND APPLICANTDIV  = '{$applicantDiv}' ";
        $query .= "     AND TESTDIV       = '{$testDiv}' ";

        return $query;
    }

    //更新
    public function updateQuery($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $updateCnt = 0;

        $query = knjl082rQuery::getCkipCsv($model);
        $result = $db->query($query);
        while ($rowCkip = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $rowCkip["LOGIN_ID"] = $rowCkip["LOGIN_ID"];

            //入学コース取得
            $entCMCcd = $db->getOne(knjl082rQuery::getEntCourseMst($rowCkip["ENTEXAMYEAR"], $rowCkip["APPLICANTDIV"], substr($rowCkip["TESTDIV"], 0, 1)));

            //件数取得
            $baseCnt = $db->getOne(knjl082rQuery::getCntBaseData('ENTEXAM_APPLICANTBASE_DAT', $rowCkip["ENTEXAMYEAR"], $rowCkip["APPLICANTDIV"], $rowCkip["LOGIN_ID"]));
            $s005Cnt = $db->getOne(knjl082rQuery::getCntData('ENTEXAM_APPLICANTBASE_DETAIL_DAT', $rowCkip["ENTEXAMYEAR"], $rowCkip["APPLICANTDIV"], $rowCkip["LOGIN_ID"], '005'));
            $s007Cnt = $db->getOne(knjl082rQuery::getCntData('ENTEXAM_APPLICANTBASE_DETAIL_DAT', $rowCkip["ENTEXAMYEAR"], $rowCkip["APPLICANTDIV"], $rowCkip["LOGIN_ID"], '007'));

            //BASEにデータがあるものだけ処理
            if ($baseCnt == 0) {
            } else {
                //ENTEXAM_APPLICANTBASE_DETAIL_DAT(005)
                $data = array();
                $data["ENTEXAMYEAR"][TEXT]      = $rowCkip["ENTEXAMYEAR"];
                $data["APPLICANTDIV"][TEXT]     = $rowCkip["APPLICANTDIV"];
                $data["EXAMNO"][TEXT]           = $rowCkip["LOGIN_ID"];
                $data["SEQ"][TEXT]              = '005';
                $data["REMARK5"][TEXT]          = $rowCkip["ENT_MONEY_STATUS"];

                $data["REGISTERCD"][TEXT]         = STAFFCD;
                $data["UPDATED"][NUMBER]          = "SYSDATE()";

                if ($s005Cnt == 0) {
                    $query = Query::insertSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT");
                } else {
                    $where  = " WHERE ENTEXAMYEAR   = '".$rowCkip["ENTEXAMYEAR"]."' ";
                    $where .= "   AND APPLICANTDIV  = '".$rowCkip["APPLICANTDIV"]."'";
                    $where .= "   AND EXAMNO        = '".$rowCkip["LOGIN_ID"]."'";
                    $where .= "   AND SEQ           = '005'";
                    $query = Query::updateSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT", $where);
                }
                $db->query($query);

                if ($rowCkip["ENT_MONEY_STATUS"] == '20') {
                    //受付日付更新(ENTEXAM_RECEPT_DAT)
                    $maxProcedureDate = knjl082rQuery::updateEntexamReceptDat($db, $model, $rowCkip);

                    //ENTEXAM_APPLICANTBASE_DAT
                    $data = array();
                    $data["PROCEDUREDIV"][TEXT]       = '1'; //手続き区分
                    $data["PROCEDUREDATE"][TEXT]      = ($maxProcedureDate != '') ? $maxProcedureDate: ''; //手続日付
                    $data["ENTDIV"][TEXT]             = '1'; //入学区分

                    $data["REGISTERCD"][TEXT]         = STAFFCD;
                    $data["UPDATED"][NUMBER]          = "SYSDATE()";

                    $where  = " WHERE ENTEXAMYEAR   = '".$rowCkip["ENTEXAMYEAR"]."' ";
                    $where .= "   AND APPLICANTDIV  = '".$rowCkip["APPLICANTDIV"]."' ";
                    $where .= "   AND EXAMNO        = '".$rowCkip["LOGIN_ID"]."' ";
                    $query = Query::updateSQL($data, "ENTEXAM_APPLICANTBASE_DAT", $where);
                    $db->query($query);

                    //ENTEXAM_APPLICANTBASE_DETAIL_DAT(007)
                    list($courseCd, $majorCd, $courseCode) = explode(':', $entCMCcd);
                    $data = array();
                    $data["ENTEXAMYEAR"][TEXT]      = $rowCkip["ENTEXAMYEAR"];
                    $data["APPLICANTDIV"][TEXT]     = $rowCkip["APPLICANTDIV"];
                    $data["EXAMNO"][TEXT]           = $rowCkip["LOGIN_ID"];
                    $data["SEQ"][TEXT]              = '007';
                    $data["REMARK1"][TEXT]          = $courseCd;
                    $data["REMARK2"][TEXT]          = $majorCd;
                    $data["REMARK3"][TEXT]          = $courseCode;

                    $data["REGISTERCD"][TEXT]         = STAFFCD;
                    $data["UPDATED"][NUMBER]          = "SYSDATE()";

                    if ($s007Cnt == 0) {
                        $query = Query::insertSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT");
                    } else {
                        $where  = " WHERE ENTEXAMYEAR   = '".$rowCkip["ENTEXAMYEAR"]."' ";
                        $where .= "   AND APPLICANTDIV  = '".$rowCkip["APPLICANTDIV"]."' ";
                        $where .= "   AND EXAMNO        = '".$rowCkip["LOGIN_ID"]."' ";
                        $where .= "   AND SEQ           = '007'";
                        $query = Query::updateSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT", $where);
                    }
                    $db->query($query);
                }
                $updateCnt++;
            }
        }
        $result->free();

        $db->commit();
        Query::dbCheckIn($db);

        return $updateCnt;
    }

    //更新(ENTEXAM_RECEPT_DAT)
    public function updateEntexamReceptDat($db, $model, $rowCkip)
    {
        $setTestDiv = substr($rowCkip["TESTDIV"], 0, 1);

        //カウント
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_RECEPT_DAT ";
        $query .= " WHERE ";
        $query .= "         ENTEXAMYEAR   = '{$rowCkip["ENTEXAMYEAR"]}' ";
        $query .= "     AND APPLICANTDIV  = '{$rowCkip["APPLICANTDIV"]}' ";
        $query .= "     AND TESTDIV       = '{$setTestDiv}' ";
        $query .= "     AND EXAM_TYPE     = '{$model->exam_type}' ";
        $query .= "     AND EXAMNO        = '{$rowCkip["LOGIN_ID"]}' ";
        $receptCnt = $db->getOne($query);

        if ($receptCnt > 0) {
            //ENTEXAM_RECEPT_DAT
            $data = array();
            $data["PROCEDUREDIV1"][TEXT]      = '1'; //手続区分
            $data["PROCEDUREDATE1"][TEXT]     = str_replace('/', '-', $model->field["PROCEDUREDATE"]); //手続日付

            $data["REGISTERCD"][TEXT]         = STAFFCD;
            $data["UPDATED"][NUMBER]          = "SYSDATE()";

            $where  = " WHERE ENTEXAMYEAR   = '".$rowCkip["ENTEXAMYEAR"]."' ";
            $where .= "   AND APPLICANTDIV  = '".$rowCkip["APPLICANTDIV"]."' ";
            $where .= "   AND TESTDIV       = '".$setTestDiv."' ";
            $where .= "   AND EXAM_TYPE     = '".$model->exam_type."' ";
            $where .= "   AND EXAMNO        = '".$rowCkip["LOGIN_ID"]."' ";
            $query = Query::updateSQL($data, "ENTEXAM_RECEPT_DAT", $where);
            $db->query($query);
        }

        //max PROCEDUREDATE1取得
        $query  = " SELECT ";
        $query .= "     max(PROCEDUREDATE1) ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_RECEPT_DAT ";
        $query .= " WHERE ";
        $query .= "         ENTEXAMYEAR   = '{$rowCkip["ENTEXAMYEAR"]}' ";
        $query .= "     AND APPLICANTDIV  = '{$rowCkip["APPLICANTDIV"]}' ";
        $query .= "     AND EXAM_TYPE     = '{$model->exam_type}' ";
        $query .= "     AND EXAMNO        = '{$rowCkip["LOGIN_ID"]}' ";
        $maxProcedureDate = $db->getOne($query);

        return $maxProcedureDate;
    }
}
