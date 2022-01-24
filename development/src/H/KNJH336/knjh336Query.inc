<?php

require_once('for_php7.php');
class knjh336query extends Query
{

    //Z010
    public function getZ010name1()
    {
        $query  = " SELECT ";
        $query .= "     NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'Z010' ";
        $query .= "     AND NAMECD2 = '00' ";

        return $query;
    }

    //模試種別
    public function getMockDiv($namecd2 = "")
    {
        $query  = " SELECT ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND NAMECD1 = 'H308' ";
        if ("" != $namecd2) {
            $query .= "     AND NAMECD2 = '".$namecd2."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     NAMECD2 ";

        return $query;
    }

    //模試、目標値マスタ取得
    public function getMockTargetMst($target)
    {
        if ($target < 3) {
            $query  = "SELECT ";
            $query .= "    MOCKCD AS VALUE, ";
            $query .= "    MOCKNAME1 AS LABEL ";
            $query .= "FROM ";
            $query .= "    MOCK_MST ";
            $query .= "WHERE ";
            $query .= "    substr(MOCKCD, 1, 1) = '".$target."' ";
            $query .= "ORDER BY ";
            $query .= "    VALUE ";
        } else {
            $query  = "SELECT ";
            $query .= "    TARGET_DIV || TARGETCD AS VALUE, ";
            $query .= "    CASE WHEN TARGET_DIV = '1' ";
            $query .= "         THEN '利用者　：' || TARGETNAME1 ";
            $query .= "         ELSE 'グループ：' || TARGETNAME1 END AS LABEL ";
            $query .= "FROM ";
            $query .= "    MOCK_TARGET_MST ";
            $query .= "WHERE ";
            $query .= "    (STF_AUTH_CD = '".STAFFCD."' ";
            $query .= "     OR STF_AUTH_CD IN (SELECT ";
            $query .= "                            '0000' || GROUPCD ";
            $query .= "                        FROM ";
            $query .= "                            USERGROUP_DAT ";
            $query .= "                        WHERE ";
            $query .= "                            STAFFCD = '".STAFFCD."' ";
            $query .= "                        ) ";
            $query .= "    ) ";
            $query .= "    AND substr(TARGETCD, 1, 1) = '".$target."' ";
            $query .= "ORDER BY ";
            $query .= "    VALUE ";
        }

        return $query;
    }

    //年度一覧
    public function getYearSem()
    {
        $query  = "SELECT DISTINCT ";
        $query .= "    T1.YEAR || '年度' || VALUE(T2.SEMESTERNAME, '') AS LABEL, ";
        $query .= "    T1.YEAR || T1.SEMESTER AS VALUE ";
        $query .= "FROM ";
        $query .= "    SCHREG_REGD_HDAT T1 ";
        $query .= "    INNER JOIN SEMESTER_MST T2 ON T1.YEAR = T2.YEAR ";
        $query .= "          AND T1.SEMESTER = T2.SEMESTER ";
        $query .= "ORDER BY ";
        $query .= "    VALUE ";

        return $query;
    }

    //年組一覧
    public function getGradeClass($model)
    {
        $query  = "SELECT DISTINCT ";
        $query .= "    GRADE || HR_CLASS AS VALUE, ";
        $query .= "    HR_NAME AS LABEL ";
        $query .= "FROM ";
        $query .= "    SCHREG_REGD_HDAT ";
        $query .= "WHERE ";
        $query .= "    YEAR || SEMESTER = '".$model->field["YEAR"]."' ";
        $query .= "ORDER BY ";
        $query .= "    VALUE ";

        return $query;
    }

    //模試データ更新
    public function insertMockQuery($model, &$data_arr)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $cnt = 0;   //処理件数
        $count = get_count($data_arr);
        for ($i=0; $i<$count; $i++) {
            $data_arr[$i]["YEAR"] = (strlen(trim($data_arr[$i]["YEAR"]))) ? sprintf("%04d", $data_arr[$i]["YEAR"]) : "";
            $data_arr[$i]["MOCKCD"] = (strlen(trim($data_arr[$i]["MOCKCD"]))) ? sprintf("%09d", $data_arr[$i]["MOCKCD"]) : "";
            $data_arr[$i]["SCHREGNO"] = (strlen(trim($data_arr[$i]["SCHREGNO"]))) ? sprintf("%08d", $data_arr[$i]["SCHREGNO"]) : "";
            $data_arr[$i]["MOCK_SUBCLASS_CD"] = (strlen(trim($data_arr[$i]["MOCK_SUBCLASS_CD"]))) ? sprintf("%06d", $data_arr[$i]["MOCK_SUBCLASS_CD"]) : "";

            $tblcnt = $db->getOne(knjh336Query::getMockCnt($data_arr, $i));

            $where = "";
            $data = array();
            if ($tblcnt > 0) {
                $data["GRADE"][TEXT]                = $data_arr[$i]["GRADE"];
                $data["HR_CLASS"][TEXT]             = $data_arr[$i]["HR_CLASS"];
                $data["ATTENDNO"][TEXT]             = $data_arr[$i]["ATTENDNO"];
                $data["NAME_KANA"][TEXT]            = $data_arr[$i]["NAME_KANA"];
                $data["SEX"][TEXT]                  = $data_arr[$i]["SEX"];
                $data["FORMNO"][TEXT]               = $data_arr[$i]["FORMNO"];
                $data["EXAMNO"][TEXT]               = $data_arr[$i]["EXAMNO"];
                $data["SCHOOLCD"][TEXT]             = $data_arr[$i]["SCHOOLCD"];
                $data["COURSEDIV"][TEXT]            = $data_arr[$i]["COURSEDIV"];
                $data["STATE_EXAM"][TEXT]           = $data_arr[$i]["STATE_EXAM"];
                $data["EXECUTION_DAY"][DATE]        = $data_arr[$i]["EXECUTION_DAY"];
                $data["SCHEDULE"][TEXT]             = $data_arr[$i]["SCHEDULE"];
                $data["TOTALWISHRANK"][NUMBER]      = $data_arr[$i]["TOTALWISHRANK"];
                $data["TOTALWISHCNT"][NUMBER]       = $data_arr[$i]["TOTALWISHCNT"];
                $data["JUDGEEVALUATION"][TEXT]      = $data_arr[$i]["JUDGEEVALUATION"];
                $data["JUDGEVALUE"][TEXT]           = $data_arr[$i]["JUDGEVALUE"];
                $data["SUBCLASS_NAMECD"][TEXT]      = $data_arr[$i]["SUBCLASS_NAMECD"];
                $data["SUBCLASS_NAME"][TEXT]        = $data_arr[$i]["SUBCLASS_NAME"];
                $data["POINT_CONVERSION"][NUMBER]   = $data_arr[$i]["POINT_CONVERSION"];
                $data["SCORE"][NUMBER]              = $data_arr[$i]["SCORE"];
                $data["DEVIATION"][FUNC]            = $data_arr[$i]["DEVIATION"];
                $data["EVALUATION_S"][TEXT]         = $data_arr[$i]["EVALUATION_S"];
                $data["EVALUATION_N"][TEXT]         = $data_arr[$i]["EVALUATION_N"];
                $data["EVALUATION_P"][TEXT]         = $data_arr[$i]["EVALUATION_P"];
                $data["EVALUATION_T"][TEXT]         = $data_arr[$i]["EVALUATION_T"];
                $data["RANK"][NUMBER]               = $data_arr[$i]["RANK"];
                $data["REGISTERCD"][TEXT]           = STAFFCD;
                $data["UPDATED"][FUNC]              = "SYSDATE()";

                $where  = "WHERE ";
                $where .= "    YEAR = '".$data_arr[$i]["YEAR"]."' ";
                $where .= "    AND MOCKCD = '".$data_arr[$i]["MOCKCD"]."' ";
                $where .= "    AND SCHREGNO = '".$data_arr[$i]["SCHREGNO"]."' ";
                $where .= "    AND MOCK_SUBCLASS_CD = '".$data_arr[$i]["MOCK_SUBCLASS_CD"]."' ";

                $insup = "up";
            } else {
                $data["YEAR"][TEXT]                 = $data_arr[$i]["YEAR"];
                $data["MOCKCD"][TEXT]               = $data_arr[$i]["MOCKCD"];
                $data["SCHREGNO"][TEXT]             = $data_arr[$i]["SCHREGNO"];
                $data["MOCK_SUBCLASS_CD"][TEXT]     = $data_arr[$i]["MOCK_SUBCLASS_CD"];
                $data["GRADE"][TEXT]                = $data_arr[$i]["GRADE"];
                $data["HR_CLASS"][TEXT]             = $data_arr[$i]["HR_CLASS"];
                $data["ATTENDNO"][TEXT]             = $data_arr[$i]["ATTENDNO"];
                $data["NAME_KANA"][TEXT]            = $data_arr[$i]["NAME_KANA"];
                $data["SEX"][TEXT]                  = $data_arr[$i]["SEX"];
                $data["FORMNO"][TEXT]               = $data_arr[$i]["FORMNO"];
                $data["EXAMNO"][TEXT]               = $data_arr[$i]["EXAMNO"];
                $data["SCHOOLCD"][TEXT]             = $data_arr[$i]["SCHOOLCD"];
                $data["COURSEDIV"][TEXT]            = $data_arr[$i]["COURSEDIV"];
                $data["STATE_EXAM"][TEXT]           = $data_arr[$i]["STATE_EXAM"];
                $data["EXECUTION_DAY"][DATE]        = $data_arr[$i]["EXECUTION_DAY"];
                $data["SCHEDULE"][TEXT]             = $data_arr[$i]["SCHEDULE"];
                $data["TOTALWISHRANK"][NUMBER]      = $data_arr[$i]["TOTALWISHRANK"];
                $data["TOTALWISHCNT"][NUMBER]       = $data_arr[$i]["TOTALWISHCNT"];
                $data["JUDGEEVALUATION"][TEXT]      = $data_arr[$i]["JUDGEEVALUATION"];
                $data["JUDGEVALUE"][TEXT]           = $data_arr[$i]["JUDGEVALUE"];
                $data["SUBCLASS_NAMECD"][TEXT]      = $data_arr[$i]["SUBCLASS_NAMECD"];
                $data["SUBCLASS_NAME"][TEXT]        = $data_arr[$i]["SUBCLASS_NAME"];
                $data["POINT_CONVERSION"][NUMBER]   = $data_arr[$i]["POINT_CONVERSION"];
                $data["SCORE"][NUMBER]              = $data_arr[$i]["SCORE"];
                $data["DEVIATION"][FUNC]            = $data_arr[$i]["DEVIATION"];
                $data["EVALUATION_S"][TEXT]         = $data_arr[$i]["EVALUATION_S"];
                $data["EVALUATION_N"][TEXT]         = $data_arr[$i]["EVALUATION_N"];
                $data["EVALUATION_P"][TEXT]         = $data_arr[$i]["EVALUATION_P"];
                $data["EVALUATION_T"][TEXT]         = $data_arr[$i]["EVALUATION_T"];
                $data["RANK"][NUMBER]               = $data_arr[$i]["RANK"];
                $data["REGISTERCD"][TEXT]           = STAFFCD;
                $data["UPDATED"][FUNC]              = "SYSDATE()";
                $insup = "ins";
            }

            if ($insup == "ins") {
                $query = Query::insertSQL($data, "MOCK_DAT");
            } elseif ($insup == "up") {
                $query = Query::updateSQL($data, "MOCK_DAT", $where);
            }
            $db->query($query);

            $cnt++;
        }
        $db->commit();
        Query::dbCheckIn($db);

        return $cnt;
    }

    //模試データ削除
    public function deleteMockQuery($model, &$data_arr)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $cnt = 0;    //処理件数
        $count = get_count($data_arr);
        for ($i=0; $i<$count; $i++) {
            $data_arr[$i]["YEAR"] = (strlen(trim($data_arr[$i]["YEAR"]))) ? sprintf("%04d", $data_arr[$i]["YEAR"]) : "";
            $data_arr[$i]["MOCKCD"] = (strlen(trim($data_arr[$i]["MOCKCD"]))) ? sprintf("%09d", $data_arr[$i]["MOCKCD"]) : "";
            $data_arr[$i]["SCHREGNO"] = (strlen(trim($data_arr[$i]["SCHREGNO"]))) ? sprintf("%08d", $data_arr[$i]["SCHREGNO"]) : "";
            $data_arr[$i]["MOCK_SUBCLASS_CD"] = (strlen(trim($data_arr[$i]["MOCK_SUBCLASS_CD"]))) ? sprintf("%06d", $data_arr[$i]["MOCK_SUBCLASS_CD"]) : "";

            $tblcnt = $db->getOne(knjh336Query::getMockCnt($data_arr, $i));

            if ($tblcnt > 0) {
                $query  = "DELETE FROM MOCK_DAT ";
                $query .= "WHERE ";
                $query .= "    YEAR = '".$data_arr[$i]["YEAR"]."' ";
                $query .= "    AND MOCKCD = '".$data_arr[$i]["MOCKCD"]."' ";
                $query .= "    AND SCHREGNO = '".$data_arr[$i]["SCHREGNO"]."' ";
                $query .= "    AND MOCK_SUBCLASS_CD = '".$data_arr[$i]["MOCK_SUBCLASS_CD"]."' ";

                $db->query($query);
                $cnt++;
            }
        }
        $db->commit();
        Query::dbCheckIn($db);

        return $cnt;
    }

    //模試データ更新前チェック
    public function getMockCnt($data_arr, $i)
    {
        $query  = "SELECT ";
        $query .= "    COUNT(*) ";
        $query .= "FROM ";
        $query .= "    MOCK_DAT ";
        $query .= "WHERE ";
        $query .= "    YEAR = '".$data_arr[$i]["YEAR"]."' ";
        $query .= "    AND MOCKCD = '".$data_arr[$i]["MOCKCD"]."' ";
        $query .= "    AND SCHREGNO = '".$data_arr[$i]["SCHREGNO"]."' ";
        $query .= "    AND MOCK_SUBCLASS_CD = '".$data_arr[$i]["MOCK_SUBCLASS_CD"]."' ";

        return $query;
    }

    //目標値データ更新
    public function insertTargetQuery($model, &$data_arr)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $cnt = 0;   //処理件数
        $count = get_count($data_arr);
        for ($i=0; $i<$count; $i++) {
            $data_arr[$i]["TARGET_DIV"] = (strlen(trim($data_arr[$i]["TARGET_DIV"]))) ? sprintf("%01d", $data_arr[$i]["TARGET_DIV"]) : "";
            $data_arr[$i]["STF_AUTH_CD"] = (strlen(trim($data_arr[$i]["STF_AUTH_CD"]))) ? sprintf("%08d", $data_arr[$i]["STF_AUTH_CD"]) : "";
            $data_arr[$i]["TARGETCD"] = (strlen(trim($data_arr[$i]["TARGETCD"]))) ? sprintf("%09d", $data_arr[$i]["TARGETCD"]) : "";
            $data_arr[$i]["MOCK_SUBCLASS_CD"] = (strlen(trim($data_arr[$i]["MOCK_SUBCLASS_CD"]))) ? sprintf("%06d", $data_arr[$i]["MOCK_SUBCLASS_CD"]) : "";

            $tblcnt = $db->getOne(knjh336Query::getTargetCnt($data_arr, $i));

            $where = "";
            $data = array();
            if ($tblcnt > 0) {
                $data["SUBCLASS_NAMECD"][TEXT]      = $data_arr[$i]["SUBCLASS_NAMECD"];
                $data["SUBCLASS_NAME"][TEXT]        = $data_arr[$i]["SUBCLASS_NAME"];
                $data["POINT_CONVERSION"][NUMBER]   = $data_arr[$i]["POINT_CONVERSION"];
                $data["SCORE"][NUMBER]              = $data_arr[$i]["SCORE"];
                $data["DEVIATION"][FUNC]            = $data_arr[$i]["DEVIATION"];
                $data["REGISTERCD"][TEXT]           = STAFFCD;
                $data["UPDATED"][FUNC]              = "SYSDATE()";

                $where  = "WHERE ";
                $where .= "    TARGET_DIV = '".$data_arr[$i]["TARGET_DIV"]."' ";
                $where .= "    AND STF_AUTH_CD = '".$data_arr[$i]["STF_AUTH_CD"]."' ";
                $where .= "    AND TARGETCD = '".$data_arr[$i]["TARGETCD"]."' ";
                $where .= "    AND MOCK_SUBCLASS_CD = '".$data_arr[$i]["MOCK_SUBCLASS_CD"]."' ";

                $insup = "up";
            } else {
                $data["TARGET_DIV"][TEXT]           = $data_arr[$i]["TARGET_DIV"];
                $data["STF_AUTH_CD"][TEXT]          = $data_arr[$i]["STF_AUTH_CD"];
                $data["TARGETCD"][TEXT]             = $data_arr[$i]["TARGETCD"];
                $data["MOCK_SUBCLASS_CD"][TEXT]     = $data_arr[$i]["MOCK_SUBCLASS_CD"];
                $data["SUBCLASS_NAMECD"][TEXT]      = $data_arr[$i]["SUBCLASS_NAMECD"];
                $data["SUBCLASS_NAME"][TEXT]        = $data_arr[$i]["SUBCLASS_NAME"];
                $data["POINT_CONVERSION"][NUMBER]   = $data_arr[$i]["POINT_CONVERSION"];
                $data["SCORE"][NUMBER]              = $data_arr[$i]["SCORE"];
                $data["DEVIATION"][FUNC]            = $data_arr[$i]["DEVIATION"];
                $data["REGISTERCD"][TEXT]           = STAFFCD;
                $data["UPDATED"][FUNC]              = "SYSDATE()";
                $insup = "ins";
            }

            if ($insup == "ins") {
                $query = Query::insertSQL($data, "MOCK_TARGET_DAT");
            } elseif ($insup == "up") {
                $query = Query::updateSQL($data, "MOCK_TARGET_DAT", $where);
            }
            $db->query($query);

            $cnt++;
        }
        $db->commit();
        Query::dbCheckIn($db);

        return $cnt;
    }

    //目標値データ削除
    public function deleteTargetQuery($model, &$data_arr)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $cnt = 0;    //処理件数
        $count = get_count($data_arr);
        for ($i=0; $i<$count; $i++) {
            $data_arr[$i]["TARGET_DIV"] = (strlen(trim($data_arr[$i]["TARGET_DIV"]))) ? sprintf("%01d", $data_arr[$i]["TARGET_DIV"]) : "";
            $data_arr[$i]["STF_AUTH_CD"] = (strlen(trim($data_arr[$i]["STF_AUTH_CD"]))) ? sprintf("%08d", $data_arr[$i]["STF_AUTH_CD"]) : "";
            $data_arr[$i]["TARGETCD"] = (strlen(trim($data_arr[$i]["TARGETCD"]))) ? sprintf("%09d", $data_arr[$i]["TARGETCD"]) : "";
            $data_arr[$i]["MOCK_SUBCLASS_CD"] = (strlen(trim($data_arr[$i]["MOCK_SUBCLASS_CD"]))) ? sprintf("%06d", $data_arr[$i]["MOCK_SUBCLASS_CD"]) : "";

            $tblcnt = $db->getOne(knjh336Query::getTargetCnt($data_arr, $i));

            if ($tblcnt > 0) {
                $query  = "DELETE FROM MOCK_TARGET_DAT ";
                $query .= "WHERE ";
                $query .= "    TARGET_DIV = '".$data_arr[$i]["TARGET_DIV"]."' ";
                $query .= "    AND STF_AUTH_CD = '".$data_arr[$i]["STF_AUTH_CD"]."' ";
                $query .= "    AND TARGETCD = '".$data_arr[$i]["TARGETCD"]."' ";
                $query .= "    AND MOCK_SUBCLASS_CD = '".$data_arr[$i]["MOCK_SUBCLASS_CD"]."' ";

                $db->query($query);
                $cnt++;
            }
        }
        $db->commit();
        Query::dbCheckIn($db);

        return $cnt;
    }

    //目標値データ更新前チェック
    public function getTargetCnt($data_arr, $i)
    {
        $query  = "SELECT ";
        $query .= "    COUNT(*) ";
        $query .= "FROM ";
        $query .= "    MOCK_TARGET_DAT ";
        $query .= "WHERE ";
        $query .= "    TARGET_DIV = '".$data_arr[$i]["TARGET_DIV"]."' ";
        $query .= "    AND STF_AUTH_CD = '".$data_arr[$i]["STF_AUTH_CD"]."' ";
        $query .= "    AND TARGETCD = '".$data_arr[$i]["TARGETCD"]."' ";
        $query .= "    AND MOCK_SUBCLASS_CD = '".$data_arr[$i]["MOCK_SUBCLASS_CD"]."' ";

        return $query;
    }

    //総点データ更新
    public function insertMockSoutenQuery($model, &$data_arr)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $cnt = 0;   //処理件数
        $count = get_count($data_arr);
        for ($i=0; $i<$count; $i++) {
            $data_arr[$i]["YEAR"] = (strlen(trim($data_arr[$i]["YEAR"]))) ? sprintf("%04d", $data_arr[$i]["YEAR"]) : "";
            $data_arr[$i]["MOCKCD"] = (strlen(trim($data_arr[$i]["MOCKCD"]))) ? sprintf("%09d", $data_arr[$i]["MOCKCD"]) : "";
            $data_arr[$i]["SCHREGNO"] = (strlen(trim($data_arr[$i]["SCHREGNO"]))) ? sprintf("%08d", $data_arr[$i]["SCHREGNO"]) : "";

            $tblcnt = $db->getOne(knjh336Query::getMockSoutenCnt($data_arr, $i));

            $where = "";
            $data = array();
            if ($tblcnt > 0) {
                $data["SINT_REMARK1"][NUMBER]   = $data_arr[$i]["SOUTEN"];
                $data["DECI_REMARK1"][FUNC]     = $data_arr[$i]["SOUTEN_PERCENT"];
                $data["SINT_REMARK2"][NUMBER]   = $data_arr[$i]["RISOUTEN"];
                $data["DECI_REMARK2"][FUNC]     = $data_arr[$i]["RISOUTEN_PERCENT"];
                $data["REGISTERCD"][TEXT]       = STAFFCD;
                $data["UPDATED"][FUNC]          = "SYSDATE()";

                $where  = "WHERE ";
                $where .= "    YEAR = '".$data_arr[$i]["YEAR"]."' ";
                $where .= "    AND MOCKCD = '".$data_arr[$i]["MOCKCD"]."' ";
                $where .= "    AND SCHREGNO = '".$data_arr[$i]["SCHREGNO"]."' ";

                $insup = "up";
            } else {
                $data["YEAR"][TEXT]             = $data_arr[$i]["YEAR"];
                $data["MOCKCD"][TEXT]           = $data_arr[$i]["MOCKCD"];
                $data["SCHREGNO"][TEXT]         = $data_arr[$i]["SCHREGNO"];
                $data["SINT_REMARK1"][NUMBER]   = $data_arr[$i]["SOUTEN"];
                $data["DECI_REMARK1"][FUNC]     = $data_arr[$i]["SOUTEN_PERCENT"];
                $data["SINT_REMARK2"][NUMBER]   = $data_arr[$i]["RISOUTEN"];
                $data["DECI_REMARK2"][FUNC]     = $data_arr[$i]["RISOUTEN_PERCENT"];
                $data["REGISTERCD"][TEXT]       = STAFFCD;
                $data["UPDATED"][FUNC]          = "SYSDATE()";
                $insup = "ins";
            }

            if ($insup == "ins") {
                $query = Query::insertSQL($data, "MOCK_SCHREG_DAT");
            } elseif ($insup == "up") {
                $query = Query::updateSQL($data, "MOCK_SCHREG_DAT", $where);
            }
            $db->query($query);

            $cnt++;
        }
        $db->commit();
        Query::dbCheckIn($db);

        return $cnt;
    }

    //総点データ削除
    public function deleteMockSoutenQuery($model, &$data_arr)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $cnt = 0;    //処理件数
        $count = get_count($data_arr);
        for ($i=0; $i<$count; $i++) {
            $data_arr[$i]["YEAR"] = (strlen(trim($data_arr[$i]["YEAR"]))) ? sprintf("%04d", $data_arr[$i]["YEAR"]) : "";
            $data_arr[$i]["MOCKCD"] = (strlen(trim($data_arr[$i]["MOCKCD"]))) ? sprintf("%09d", $data_arr[$i]["MOCKCD"]) : "";
            $data_arr[$i]["SCHREGNO"] = (strlen(trim($data_arr[$i]["SCHREGNO"]))) ? sprintf("%08d", $data_arr[$i]["SCHREGNO"]) : "";

            $tblcnt = $db->getOne(knjh336Query::getMockSoutenCnt($data_arr, $i));

            if ($tblcnt > 0) {
                $query  = "DELETE FROM MOCK_SCHREG_DAT ";
                $query .= "WHERE ";
                $query .= "    YEAR = '".$data_arr[$i]["YEAR"]."' ";
                $query .= "    AND MOCKCD = '".$data_arr[$i]["MOCKCD"]."' ";
                $query .= "    AND SCHREGNO = '".$data_arr[$i]["SCHREGNO"]."' ";

                $db->query($query);
                $cnt++;
            }
        }
        $db->commit();
        Query::dbCheckIn($db);

        return $cnt;
    }

    //総点データ更新前チェック
    public function getMockSoutenCnt($data_arr, $i)
    {
        $query  = "SELECT ";
        $query .= "    COUNT(*) ";
        $query .= "FROM ";
        $query .= "    MOCK_SCHREG_DAT ";
        $query .= "WHERE ";
        $query .= "    YEAR = '".$data_arr[$i]["YEAR"]."' ";
        $query .= "    AND MOCKCD = '".$data_arr[$i]["MOCKCD"]."' ";
        $query .= "    AND SCHREGNO = '".$data_arr[$i]["SCHREGNO"]."' ";

        return $query;
    }



    //エラーＤＢへの追加
    public function insertQueryErr(&$db, $record_no, $check_error)
    {
        $data1["PROGRAMID"][TEXT]        = PROGRAMID;
        $data1["MSGROW"][NUMBER]         = $record_no;
        $data1["MSGREMARK"][TEXT]         = $check_error;

        $query = Query::insertSQL($data1, "W_CSVMSG_PRG_DAT");

        $result = $db->query($query);
    }

    //模試データ出力用
    public function getselectMockDataSQL($model)
    {
        $query  = "SELECT ";
        $query .= "    CASE WHEN VALUE(L1.YEAR, '') = '' ";
        $query .= "         THEN T1.YEAR ";
        $query .= "         ELSE L1.YEAR END AS YEAR, ";
        $query .= "    CASE WHEN VALUE(L1.MOCKCD, '') = '' ";
        $query .= "         THEN '".$model->field["OUT_MOCK_TARGET"]."' ";
        $query .= "         ELSE L1.MOCKCD END AS MOCKCD, ";
        $query .= "    CASE WHEN VALUE(L1.SCHREGNO, '') = '' ";
        $query .= "         THEN T1.SCHREGNO ";
        $query .= "         ELSE L1.SCHREGNO END AS SCHREGNO, ";
        $query .= "    L1.MOCK_SUBCLASS_CD, ";
        $query .= "    CASE WHEN VALUE(L1.GRADE, '00') = '00' ";
        $query .= "         THEN T1.GRADE ";
        $query .= "         ELSE L1.GRADE END AS GRADE, ";
        $query .= "    CASE WHEN VALUE(L1.HR_CLASS, '00') = '00' ";
        $query .= "         THEN T1.HR_CLASS ";
        $query .= "         ELSE L1.HR_CLASS END AS HR_CLASS, ";
        $query .= "    CASE WHEN VALUE(L1.ATTENDNO, '00') = '00' ";
        $query .= "         THEN T1.ATTENDNO ";
        $query .= "         ELSE L1.ATTENDNO END AS ATTENDNO, ";
        $query .= "    CASE WHEN VALUE(L1.NAME_KANA, '00') = '00' ";
        $query .= "         THEN L2.NAME_KANA ";
        $query .= "         ELSE L1.NAME_KANA END AS NAME_KANA, ";
        $query .= "    L1.SEX, ";
        $query .= "    L1.FORMNO, ";
        $query .= "    L1.EXAMNO, ";
        $query .= "    L1.SCHOOLCD, ";
        $query .= "    L1.COURSEDIV, ";
        $query .= "    L1.STATE_EXAM, ";
        $query .= "    L1.EXECUTION_DAY, ";
        $query .= "    L1.SCHEDULE, ";
        $query .= "    L1.TOTALWISHRANK, ";
        $query .= "    L1.TOTALWISHCNT, ";
        $query .= "    L1.JUDGEEVALUATION, ";
        $query .= "    L1.JUDGEVALUE, ";
        $query .= "    L1.SUBCLASS_NAMECD, ";
        $query .= "    L1.SUBCLASS_NAME, ";
        $query .= "    L1.POINT_CONVERSION, ";
        $query .= "    L1.SCORE, ";
        $query .= "    L1.DEVIATION, ";
        $query .= "    L1.EVALUATION_S, ";
        $query .= "    L1.EVALUATION_N, ";
        $query .= "    L1.EVALUATION_P, ";
        $query .= "    L1.EVALUATION_T, ";
        $query .= "    L1.RANK, ";
        $query .= "    '".$model->lastColumn."' AS ".$model->lastColumn." ";
        $query .= "FROM ";
        $query .= "    SCHREG_REGD_DAT T1 ";
        $query .= "    LEFT JOIN MOCK_DAT L1 ON L1.YEAR = T1.YEAR ";
        $query .= "         AND L1.MOCKCD = '".$model->field["OUT_MOCK_TARGET"]."' ";
        $query .= "         AND L1.SCHREGNO = T1.SCHREGNO ";
        $query .= "    LEFT JOIN SCHREG_BASE_MST L2 ON L2.SCHREGNO = T1.SCHREGNO ";
        $query .= "WHERE ";
        $query .= "    T1.YEAR || T1.SEMESTER = '".$model->field["YEAR"]."' ";
        $query .= "    AND T1.GRADE || T1.HR_CLASS = '".$model->field["GRADE_HR_CLASS"]."' ";
        $query .= "ORDER BY ";
        $query .= "    GRADE, ";
        $query .= "    HR_CLASS, ";
        $query .= "    ATTENDNO, ";
        $query .= "    L1.MOCKCD, ";
        $query .= "    L1.MOCK_SUBCLASS_CD ";

        return $query;
    }

    //目標値データ出力用
    public function getselectTargetDataSQL($model)
    {
        $query  = "SELECT ";
        $query .= "    TARGET_DIV, ";
        $query .= "    STF_AUTH_CD, ";
        $query .= "    TARGETCD, ";
        $query .= "    MOCK_SUBCLASS_CD, ";
        $query .= "    SUBCLASS_NAMECD, ";
        $query .= "    SUBCLASS_NAME, ";
        $query .= "    POINT_CONVERSION, ";
        $query .= "    SCORE, ";
        $query .= "    DEVIATION, ";
        $query .= "    '".$model->lastColumn."' AS ".$model->lastColumn." ";
        $query .= "FROM ";
        $query .= "    MOCK_TARGET_DAT T1 ";
        $query .= "WHERE ";
        $query .= "    (STF_AUTH_CD = '".STAFFCD."' ";
        $query .= "     OR STF_AUTH_CD IN (SELECT ";
        $query .= "                            '0000' || GROUPCD ";
        $query .= "                        FROM ";
        $query .= "                            USERGROUP_DAT ";
        $query .= "                        WHERE ";
        $query .= "                            STAFFCD = '".STAFFCD."' ";
        $query .= "                        ) ";
        $query .= "    ) ";
        $query .= "    AND TARGET_DIV || TARGETCD = '".$model->field["OUT_MOCK_TARGET"]."' ";
        $query .= "ORDER BY ";
        $query .= "    TARGET_DIV, ";
        $query .= "    STF_AUTH_CD, ";
        $query .= "    TARGETCD, ";
        $query .= "    MOCK_SUBCLASS_CD ";

        return $query;
    }

    //総点データ出力用
    public function getselectMockSoutenDataSQL($model)
    {
        $query  = "SELECT ";
        $query .= "    CASE WHEN VALUE(L1.YEAR, '') = '' ";
        $query .= "         THEN T1.YEAR ";
        $query .= "         ELSE L1.YEAR END AS YEAR, ";
        $query .= "    CASE WHEN VALUE(L1.MOCKCD, '') = '' ";
        $query .= "         THEN '".$model->field["OUT_MOCK_TARGET"]."' ";
        $query .= "         ELSE L1.MOCKCD END AS MOCKCD, ";
        $query .= "    CASE WHEN VALUE(L1.SCHREGNO, '') = '' ";
        $query .= "         THEN T1.SCHREGNO ";
        $query .= "         ELSE L1.SCHREGNO END AS SCHREGNO, ";
        $query .= "    T1.GRADE, ";
        $query .= "    T1.HR_CLASS, ";
        $query .= "    T1.ATTENDNO, ";
        $query .= "    L2.NAME_KANA, ";
        $query .= "    L1.SINT_REMARK1, ";
        $query .= "    L1.DECI_REMARK1, ";
        $query .= "    L1.SINT_REMARK2, ";
        $query .= "    L1.DECI_REMARK2, ";
        $query .= "    '".$model->lastColumn."' AS ".$model->lastColumn." ";
        $query .= "FROM ";
        $query .= "    SCHREG_REGD_DAT T1 ";
        $query .= "    LEFT JOIN MOCK_SCHREG_DAT L1 ON L1.YEAR = T1.YEAR ";
        $query .= "         AND L1.MOCKCD = '".$model->field["OUT_MOCK_TARGET"]."' ";
        $query .= "         AND L1.SCHREGNO = T1.SCHREGNO ";
        $query .= "    LEFT JOIN SCHREG_BASE_MST L2 ON L2.SCHREGNO = T1.SCHREGNO ";
        $query .= "WHERE ";
        $query .= "    T1.YEAR || T1.SEMESTER = '".$model->field["YEAR"]."' ";
        $query .= "    AND T1.GRADE || T1.HR_CLASS = '".$model->field["GRADE_HR_CLASS"]."' ";
        $query .= "ORDER BY ";
        $query .= "    GRADE, ";
        $query .= "    HR_CLASS, ";
        $query .= "    ATTENDNO, ";
        $query .= "    L1.MOCKCD ";

        return $query;
    }
}
