<?php

require_once('for_php7.php');

class knja050hQuery extends Query
{
    /******************/
    /**  画面で使用  **/
    /******************/

    //動作条件チェック
    public function checktoStart()
    {
        return "SELECT COUNT(*) FROM V_COURSE_MAJOR_MST";
    }

    //校種
    public function getA023($model)
    {
        $query  = " SELECT ";
        $query .= "     NAMECD2, ";
        $query .= "     NAME1 AS VALUE, ";
        $query .= "     NAME1 || ':' || ABBV1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "   NAMECD1 = 'A023' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " AND NAME1 IN ('".implode(explode(':', $model->selectSchoolKind), "','")."') ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND NAME1 = '".SCHOOLKIND."' ";
        }
        $query .= " ORDER BY ";
        $query .= "   NAMECD2 ";

        return $query;
    }

    //学期
    public function getSemester($model)
    {
        $query  = " SELECT ";
        $query .= "     SEMESTER AS VALUE, ";
        $query .= "     SEMESTERNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR    = '".$model->field["YEAR"]."' ";
        $query .= "     AND SEMESTER <> '9' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //学校マスタ取得
    public function getSchoolMst($model)
    {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     SCHOOL_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR     = '".$model->field["YEAR"]."' ";
        if ($model->Properties["useSchool_KindField"] == "1") {
            $query .= " AND SCHOOLCD    = '".sprintf("%012d", SCHOOLCD)."' ";
            $query .= " AND SCHOOL_KIND = '".$model->schoolKind."' ";
        }

        return $query;
    }

    //A023
    public function getA023GradeRange($schoolKind)
    {
        $query  = " SELECT ";
        $query .= "     INT(NAME3) - INT(NAME2) AS RANGE ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'A023' ";
        $query .= " AND NAME1 = '".$schoolKind."' ";
        $query .= " ORDER BY ";
        $query .= "   NAMECD2 ";

        return $query;
    }

    //前回実行日取得
    public function getMaxUpdate($db)
    {
        $query = "SELECT DATE(MAX(UPDATED)),TIME(MAX(UPDATED)) FROM NYUGAKU_LIST_ANYTIME_DAT";
        $tmp   = $db->getRow($query);
        
        $date1 = ($tmp[0]=="") ? null : strftime("%Y年%m月%d日 %H時%M分", strtotime($tmp[0]." ".$tmp[1]));
        return $date1;
    }

    /************************/
    /**  ＣＳＶ取込で使用  **/
    /************************/
    public function insertQueryNyugaku($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        // 寮の最小コードを取得
        $domiMinCd = $db->getOne(knja050hQuery::getDomitoryMst());

        //INSERT
        for ($i = 0; $i < get_count($model->data_arr); $i++) {
            //データ件数取得
            $nyugaku_cnt= $db->getOne(knja050hQuery::cntNyugakuListAnytimeDat($model->data_arr[$i]["ENTERYEAR"], $model->data_arr[$i]["SCHREGNO"]));

            /** NYUGAKU_LIST_ANYTIME_DAT **/
            $data = array();
            $data["ENTERYEAR"][TEXT]            = $model->data_arr[$i]["ENTERYEAR"];
            $data["SCHREGNO"][TEXT]             = $model->data_arr[$i]["SCHREGNO"];
            $data["SEMESTER"][TEXT]             = $model->data_arr[$i]["SEMESTER"];
            $data["ENT_DATE"][DATE]             = str_replace("/", "-", $model->data_arr[$i]["ENT_DATE"]);
            $data["ENT_DIV"][TEXT]              = $model->data_arr[$i]["ENT_DIV"];
            $data["GRADE"][TEXT]                = $model->data_arr[$i]["GRADE"];
            $data["HR_CLASS"][TEXT]             = $model->data_arr[$i]["HR_CLASS"];
            $data["ATTENDNO"][TEXT]             = $model->data_arr[$i]["ATTENDNO"];
            $data["INOUTCD"][TEXT]              = $model->data_arr[$i]["INOUTCD"];
            $data["COURSECD"][TEXT]             = $model->data_arr[$i]["COURSECD"];
            $data["MAJORCD"][TEXT]              = $model->data_arr[$i]["MAJORCD"];
            $data["COURSECODE"][TEXT]           = $model->data_arr[$i]["COURSECODE"];
            $data["NAME"][TEXT]                 = $model->data_arr[$i]["NAME"];
            $data["NAME_KANA"][TEXT]            = $model->data_arr[$i]["NAME_KANA"];
            $data["BIRTHDAY"][DATE]             = str_replace("/", "-", $model->data_arr[$i]["BIRTHDAY"]);
            $data["SEX"][TEXT]                  = $model->data_arr[$i]["SEX"];
            $data["FS_CD"][TEXT]                = $model->data_arr[$i]["FINSCHOOLCD"];
            $data["FINSCHOOLGRADDATE"][DATE]    = str_replace("/", "-", $model->data_arr[$i]["FINSCHOOLGRADDATE"]);
            $data["PRISCHOOLCD"][TEXT]          = $model->data_arr[$i]["PRISCHOOLCD"];
            $data["DORMITORY_FLG"][TEXT]        = $model->data_arr[$i]["DORMITORY_FLG"];
            $data["ZIPCD"][TEXT]                = $model->data_arr[$i]["ZIPCD"];
            $data["ADDRESS1"][TEXT]             = $model->data_arr[$i]["ADDRESS1"];
            $data["ADDRESS2"][TEXT]             = $model->data_arr[$i]["ADDRESS2"];
            $data["TELNO"][TEXT]                = $model->data_arr[$i]["TELNO"];
            $data["FAXNO"][TEXT]                = $model->data_arr[$i]["FAXNO"];
            $data["EMAIL"][TEXT]                = $model->data_arr[$i]["EMAIL"];
            $data["EMERGENCYCALL"][TEXT]        = $model->data_arr[$i]["EMERGENCYCALL"];
            $data["EMERGENCYTELNO"][TEXT]       = $model->data_arr[$i]["EMERGENCYTELNO"];
            $data["SCALASHIPDIV"][TEXT]         = $model->data_arr[$i]["SCALASHIPDIV"];
            $data["KATEI_ENTEXAMYEAR"][TEXT]    = $model->data_arr[$i]["KATEI_ENTEXAMYEAR"];
            $data["TIKUCD"][TEXT]               = $model->data_arr[$i]["TIKUCD"];
            $data["EXAMNO"][TEXT]               = $model->data_arr[$i]["EXAMNO"];
            $data["TESTDIV"][TEXT]              = $model->data_arr[$i]["TESTDIV"];
            $data["EXAM_TYPE"][TEXT]            = $model->data_arr[$i]["EXAM_TYPE"];

            $data["REGISTERCD"][TEXT]     = STAFFCD;
            $data["UPDATED"][FUNC]        = "SYSDATE()";

            $where  = " WHERE ENTERYEAR = '{$model->data_arr[$i]["ENTERYEAR"]}' ";
            $where .= "   AND SCHREGNO  = '{$model->data_arr[$i]["SCHREGNO"]}' ";

            if (0 == $nyugaku_cnt) {
                $query = Query::insertSQL($data, "NYUGAKU_LIST_ANYTIME_DAT");
            } else {
                $query = Query::updateSQL($data, "NYUGAKU_LIST_ANYTIME_DAT", $where);
            }
            $db->query($query);

            /** SCHREG_BASE_MST **/
            $data2 = array();
            $data2["SCHREGNO"][TEXT]           = $model->data_arr[$i]["SCHREGNO"];
            $data2["INOUTCD"][TEXT]            = $model->data_arr[$i]["INOUTCD"];
            $data2["NAME"][TEXT]               = $model->data_arr[$i]["NAME"];
            $data2["NAME_SHOW"][TEXT]          = $model->data_arr[$i]["NAME"];
            $data2["NAME_KANA"][TEXT]          = $model->data_arr[$i]["NAME_KANA"];
            $data2["BIRTHDAY"][DATE]           = str_replace("/", "-", $model->data_arr[$i]["BIRTHDAY"]);
            $data2["SEX"][TEXT]                = $model->data_arr[$i]["SEX"];
            $data2["FINSCHOOLCD"][TEXT]        = $model->data_arr[$i]["FINSCHOOLCD"];
            $data2["FINISH_DATE"][DATE]        = str_replace("/", "-", $model->data_arr[$i]["FINSCHOOLGRADDATE"]);
            $data2["PRISCHOOLCD"][TEXT]        = $model->data_arr[$i]["PRISCHOOLCD"];
            $data2["ENT_DATE"][DATE]           = str_replace("/", "-", $model->data_arr[$i]["ENT_DATE"]);
            $data2["ENT_DIV"][TEXT]            = $model->data_arr[$i]["ENT_DIV"];
            $data2["GRD_DATE"][DATE]           = str_replace("/", "-", $model->data_arr[$i]["FINSCHOOLGRADDATE"]);
            $data2["EMERGENCYCALL"][TEXT]      = $model->data_arr[$i]["EMERGENCYCALL"];
            $data2["EMERGENCYTELNO"][TEXT]     = $model->data_arr[$i]["EMERGENCYTELNO"];

            $data2["REGISTERCD"][TEXT]     = STAFFCD;
            $data2["UPDATED"][FUNC]        = "SYSDATE()";

            $query = Query::insertSQL($data2, "SCHREG_BASE_MST");
            $db->query($query);

            /** SCHREG_BASE_DETAIL_MST **/
            $data6 = array();
            $data6["SCHREGNO"][TEXT]       = $model->data_arr[$i]["SCHREGNO"];
            $data6["BASE_SEQ"][TEXT]       = "014";
            $data6["BASE_REMARK1"][TEXT]   = $model->data_arr[$i]["TESTDIV"];
            $data6["BASE_REMARK2"][TEXT]   = $model->data_arr[$i]["EXAM_TYPE"];

            $data6["REGISTERCD"][TEXT]     = STAFFCD;
            $data6["UPDATED"][FUNC]        = "SYSDATE()";

            $query = Query::insertSQL($data6, "SCHREG_BASE_DETAIL_MST");
            $db->query($query);

            /** SCHREG_REGD_DAT **/
            $data3 = array();
            $data3["SCHREGNO"][TEXT]        = $model->data_arr[$i]["SCHREGNO"];
            $data3["YEAR"][TEXT]            = $model->data_arr[$i]["ENTERYEAR"];
            $data3["SEMESTER"][TEXT]        = $model->data_arr[$i]["SEMESTER"];
            $data3["GRADE"][TEXT]           = $model->data_arr[$i]["GRADE"];
            $data3["HR_CLASS"][TEXT]        = $model->data_arr[$i]["HR_CLASS"];
            $data3["ATTENDNO"][DATE]        = $model->data_arr[$i]["ATTENDNO"];
            $data3["ANNUAL"][TEXT]          = $model->data_arr[$i]["GRADE"];
            $data3["COURSECD"][TEXT]        = $model->data_arr[$i]["COURSECD"];
            $data3["MAJORCD"][TEXT]         = $model->data_arr[$i]["MAJORCD"];
            $data3["COURSECODE"][TEXT]      = $model->data_arr[$i]["COURSECODE"];

            $data3["REGISTERCD"][TEXT]     = STAFFCD;
            $data3["UPDATED"][FUNC]        = "SYSDATE()";

            $query = Query::insertSQL($data3, "SCHREG_REGD_DAT");
            $db->query($query);

            /** SCHREG_ADDRESS_DAT **/
            $data4 = array();
            $data4["SCHREGNO"][TEXT]    = $model->data_arr[$i]["SCHREGNO"];
            $data4["ISSUEDATE"][TEXT]   = str_replace("/", "-", $model->field["GUARD_ISSUEDATE"]);
            $data4["EXPIREDATE"][TEXT]  = str_replace("/", "-", $model->field["GUARD_EXPIREDATE"]);
            $data4["ZIPCD"][TEXT]       = $model->data_arr[$i]["ZIPCD"];
            $data4["AREACD"][TEXT]      = $model->data_arr[$i]["TIKUCD"];
            $data4["ADDR1"][TEXT]       = $model->data_arr[$i]["ADDRESS1"];
            $data4["ADDR2"][DATE]       = $model->data_arr[$i]["ADDRESS2"];
            $data4["TELNO"][TEXT]       = $model->data_arr[$i]["TELNO"];
            $data4["FAXNO"][TEXT]       = $model->data_arr[$i]["FAXNO"];
            $data4["EMAIL"][TEXT]       = $model->data_arr[$i]["EMAIL"];

            $data4["REGISTERCD"][TEXT]     = STAFFCD;
            $data4["UPDATED"][FUNC]        = "SYSDATE()";

            $query = Query::insertSQL($data4, "SCHREG_ADDRESS_DAT");
            $db->query($query);

            /** SCHREG_DOMITORY_HIST_DAT **/
            if ($domiMinCd != "" && $model->data_arr[$i]["DORMITORY_FLG"] == "1") {
                $data5["SCHREGNO"][TEXT]     = $model->data_arr[$i]["SCHREGNO"];
                $data5["DOMI_CD"][TEXT]      = $domiMinCd;
                $data5["DOMI_ENTDAY"][DATE]  = str_replace("/", "-", $model->data_arr[$i]["ENT_DATE"]);

                $data5["REGISTERCD"][TEXT]   = STAFFCD;
                $data5["UPDATED"][NUMBER]    = "sysdate()";

                $query = Query::insertSQL($data5, "SCHREG_DOMITORY_HIST_DAT");
                $db->query($query);
            }
        }

        $db->commit();
        Query::dbCheckIn($db);
        return;
    }

    //削除（ＣＳＶデータより読込）
    public function deleteQueryNyugaku($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        for ($i = 0; $i < get_count($model->data_arr); $i++) {
            /** NYUGAKU_LIST_ANYTIME_DAT **/
            $query  = " DELETE FROM NYUGAKU_LIST_ANYTIME_DAT ";
            $query .= " WHERE ENTERYEAR = '{$model->data_arr[$i]["ENTERYEAR"]}' ";
            $query .= "   AND SCHREGNO  = '{$model->data_arr[$i]["SCHREGNO"]}' ";
            $db->query($query);

            /** SCHREG_BASE_MST **/
            $query  = " DELETE FROM SCHREG_BASE_MST ";
            $query .= " WHERE SCHREGNO  = '{$model->data_arr[$i]["SCHREGNO"]}' ";
            $db->query($query);

            /** SCHREG_BASE_DETAIL_MST **/
            $query  = " DELETE FROM SCHREG_BASE_DETAIL_MST ";
            $query .= " WHERE SCHREGNO  = '{$model->data_arr[$i]["SCHREGNO"]}' ";
            $db->query($query);

            /** SCHREG_REGD_DAT **/
            $query  = " DELETE FROM SCHREG_REGD_DAT ";
            $query .= " WHERE SCHREGNO  = '{$model->data_arr[$i]["SCHREGNO"]}' ";
            $db->query($query);

            /** SCHREG_ADDRESS_DAT **/
            $query  = " DELETE FROM SCHREG_ADDRESS_DAT ";
            $query .= " WHERE SCHREGNO  = '{$model->data_arr[$i]["SCHREGNO"]}' ";
            $db->query($query);

            /** SCHREG_DOMITORY_HIST_DAT **/
            $query  = " DELETE FROM SCHREG_DOMITORY_HIST_DAT ";
            $query .= " WHERE SCHREGNO  = '{$model->data_arr[$i]["SCHREGNO"]}' ";
            $db->query($query);
        }

        $db->commit();
        Query::dbCheckIn($db);
        return;
    }

    //件数取得 -- NYUGAKU_LIST_ANYTIME_DAT
    public function cntNyugakuListAnytimeDat($year, $schregNo)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     NYUGAKU_LIST_ANYTIME_DAT ";
        $query .= " WHERE ";
        $query .= "         ENTERYEAR = '{$year}' ";
        $query .= "     AND SCHREGNO  = '{$schregNo}' ";

        return $query;
    }

    //寮の最小コード取得 -- DOMITORY_MST
    public function getDomitoryMst()
    {
        $query  = " SELECT ";
        $query .= "     min(DOMI_CD) ";
        $query .= " FROM ";
        $query .= "     DOMITORY_MST ";
        $query .= " GROUP BY ";
        $query .= "     DOMI_CD ";

        return $query;
    }

    //マスタの追加（ＣＳＶデータより読込）保護者
    public function insertQueryGuardian($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $cnt = 0;   //処理件数
        for ($i = 0; $i < get_count($model->data_arr); $i++) {
            //データ件数取得
            $nyugaku_cnt= $db->getOne(knja050hQuery::cntNyugakuListAnytimeDat($model->data_arr[$i]["ENTERYEAR"], $model->data_arr[$i]["SCHREGNO"]));

            /** NYUGAKU_LIST_ANYTIME_DAT **/
            $data = array();
            $data["ENTERYEAR"][TEXT]                = $model->data_arr[$i]["ENTERYEAR"];
            $data["SCHREGNO"][TEXT]                 = $model->data_arr[$i]["SCHREGNO"];

            $data["RELATIONSHIP"][TEXT]             = $model->data_arr[$i]["RELATIONSHIP"];
            $data["GNAME"][TEXT]                    = $model->data_arr[$i]["GUARD_NAME"];
            $data["GKANA"][TEXT]                    = $model->data_arr[$i]["GUARD_KANA"];
            $data["GUARD_SEX"][TEXT]                = $model->data_arr[$i]["GUARD_SEX"];
            $data["GUARD_BIRTHDAY"][TEXT]           = $model->data_arr[$i]["GUARD_BIRTHDAY"];
            $data["GZIPCD"][TEXT]                   = $model->data_arr[$i]["GUARD_ZIPCD"];
            $data["GADDRESS1"][TEXT]                = $model->data_arr[$i]["GUARD_ADDR1"];
            $data["GADDRESS2"][TEXT]                = $model->data_arr[$i]["GUARD_ADDR2"];
            $data["GTELNO"][TEXT]                   = $model->data_arr[$i]["GUARD_TELNO"];
            $data["GFAXNO"][TEXT]                   = $model->data_arr[$i]["GUARD_FAXNO"];
            $data["GEMAIL"][TEXT]                   = $model->data_arr[$i]["GUARD_E_MAIL"];
            $data["GUARD_JOBCD"][TEXT]              = $model->data_arr[$i]["GUARD_JOBCD"];
            $data["GUARD_WORK_NAME"][TEXT]          = $model->data_arr[$i]["GUARD_WORK_NAME"];
            $data["GUARD_WORK_TELNO"][TEXT]         = $model->data_arr[$i]["GUARD_WORK_TELNO"];
            $data["GUARANTOR_RELATIONSHIP"][TEXT]   = $model->data_arr[$i]["GUARANTOR_RELATIONSHIP"];
            $data["GUARANTOR_NAME"][TEXT]           = $model->data_arr[$i]["GUARANTOR_NAME"];
            $data["GUARANTOR_KANA"][TEXT]           = $model->data_arr[$i]["GUARANTOR_KANA"];
            $data["GUARANTOR_SEX"][TEXT]            = $model->data_arr[$i]["GUARANTOR_SEX"];
            $data["GUARANTOR_ZIPCD"][TEXT]          = $model->data_arr[$i]["GUARANTOR_ZIPCD"];
            $data["GUARANTOR_ADDR1"][TEXT]          = $model->data_arr[$i]["GUARANTOR_ADDR1"];
            $data["GUARANTOR_ADDR2"][TEXT]          = $model->data_arr[$i]["GUARANTOR_ADDR2"];
            $data["GUARANTOR_TELNO"][TEXT]          = $model->data_arr[$i]["GUARANTOR_TELNO"];
            $data["GUARANTOR_JOBCD"][TEXT]          = $model->data_arr[$i]["GUARANTOR_JOBCD"];
            $data["PUBLIC_OFFICE"][TEXT]            = $model->data_arr[$i]["PUBLIC_OFFICE"];

            $data["REGISTERCD"][TEXT]     = STAFFCD;
            $data["UPDATED"][FUNC]        = "SYSDATE()";

            $where  = " WHERE ENTERYEAR = '{$model->data_arr[$i]["ENTERYEAR"]}' ";
            $where .= "   AND SCHREGNO  = '{$model->data_arr[$i]["SCHREGNO"]}' ";

            if (0 == $nyugaku_cnt) {
                $query = Query::insertSQL($data, "NYUGAKU_LIST_ANYTIME_DAT");
            } else {
                $query = Query::updateSQL($data, "NYUGAKU_LIST_ANYTIME_DAT", $where);
            }
            $db->query($query);

            /** GUARDIAN_DAT **/
            $data = array();
            $data["SCHREGNO"][TEXT]                = $model->data_arr[$i]["SCHREGNO"];
            $data["RELATIONSHIP"][TEXT]            = $model->data_arr[$i]["RELATIONSHIP"];
            $data["GUARD_NAME"][TEXT]              = $model->data_arr[$i]["GUARD_NAME"];
            $data["GUARD_KANA"][TEXT]              = $model->data_arr[$i]["GUARD_KANA"];
            $data["GUARD_SEX"][TEXT]               = $model->data_arr[$i]["GUARD_SEX"];
            $data["GUARD_BIRTHDAY"][TEXT]          = str_replace("/", "-", $model->data_arr[$i]["GUARD_BIRTHDAY"]);
            $data["GUARD_ZIPCD"][TEXT]             = $model->data_arr[$i]["GUARD_ZIPCD"];
            $data["GUARD_ADDR1"][TEXT]             = $model->data_arr[$i]["GUARD_ADDR1"];
            $data["GUARD_ADDR2"][TEXT]             = $model->data_arr[$i]["GUARD_ADDR2"];
            $data["GUARD_TELNO"][TEXT]             = $model->data_arr[$i]["GUARD_TELNO"];
            $data["GUARD_FAXNO"][TEXT]             = $model->data_arr[$i]["GUARD_FAXNO"];
            $data["GUARD_E_MAIL"][TEXT]            = $model->data_arr[$i]["GUARD_E_MAIL"];
            $data["GUARD_JOBCD"][TEXT]             = $model->data_arr[$i]["GUARD_JOBCD"];
            $data["GUARD_WORK_NAME"][TEXT]         = $model->data_arr[$i]["GUARD_WORK_NAME"];
            $data["GUARD_WORK_TELNO"][TEXT]        = $model->data_arr[$i]["GUARD_WORK_TELNO"];
            $data["GUARANTOR_RELATIONSHIP"][TEXT]  = $model->data_arr[$i]["GUARANTOR_RELATIONSHIP"];
            $data["GUARANTOR_NAME"][TEXT]          = $model->data_arr[$i]["GUARANTOR_NAME"];
            $data["GUARANTOR_KANA"][TEXT]          = $model->data_arr[$i]["GUARANTOR_KANA"];
            $data["GUARANTOR_SEX"][TEXT]           = $model->data_arr[$i]["GUARANTOR_SEX"];
            $data["GUARANTOR_ZIPCD"][TEXT]         = $model->data_arr[$i]["GUARANTOR_ZIPCD"];
            $data["GUARANTOR_ADDR1"][TEXT]         = $model->data_arr[$i]["GUARANTOR_ADDR1"];
            $data["GUARANTOR_ADDR2"][TEXT]         = $model->data_arr[$i]["GUARANTOR_ADDR2"];
            $data["GUARANTOR_TELNO"][TEXT]         = $model->data_arr[$i]["GUARANTOR_TELNO"];
            $data["GUARANTOR_JOBCD"][TEXT]         = $model->data_arr[$i]["GUARANTOR_JOBCD"];
            $data["PUBLIC_OFFICE"][TEXT]           = $model->data_arr[$i]["PUBLIC_OFFICE"];
            $data["REGISTERCD"][TEXT]              = STAFFCD;
            $data["UPDATED"][NUMBER]               = "SYSDATE()";
            if (1 > $db->getOne("SELECT COUNT(*) FROM GUARDIAN_DAT WHERE SCHREGNO = '".$model->data_arr[$i]["SCHREGNO"]."'")) {
                $query = Query::insertSQL($data, "GUARDIAN_DAT");
            } else {
                $where  = "WHERE SCHREGNO = '".$model->data_arr[$i]["SCHREGNO"]."' ";

                $query = Query::updateSQL($data, "GUARDIAN_DAT", $where);
            }
            $db->query($query);

            /** GUARDIAN_ADDRESS_DAT **/
            $query = "DELETE FROM GUARDIAN_ADDRESS_DAT WHERE SCHREGNO = '".$model->data_arr[$i]["SCHREGNO"]."'";
            $db->query($query);

            $data = array();
            $data["SCHREGNO"][TEXT]       = $model->data_arr[$i]["SCHREGNO"];
            $data["ISSUEDATE"][TEXT]      = str_replace("/", "-", $model->field["GUARD_ISSUEDATE"]);
            $data["EXPIREDATE"][TEXT]     = str_replace("/", "-", $model->field["GUARD_EXPIREDATE"]);
            $data["GUARD_ZIPCD"][TEXT]    = $model->data_arr[$i]["GUARD_ZIPCD"];
            $data["GUARD_ADDR1"][TEXT]    = $model->data_arr[$i]["GUARD_ADDR1"];
            $data["GUARD_ADDR2"][TEXT]    = $model->data_arr[$i]["GUARD_ADDR2"];
            $data["GUARD_TELNO"][TEXT]    = $model->data_arr[$i]["GUARD_TELNO"];
            $data["GUARD_FAXNO"][TEXT]    = $model->data_arr[$i]["GUARD_FAXNO"];
            $data["GUARD_E_MAIL"][TEXT]   = $model->data_arr[$i]["GUARD_E_MAIL"];
            $data["REGISTERCD"][TEXT]     = STAFFCD;
            $data["UPDATED"][FUNC]        = "SYSDATE()";

            $query = Query::insertSQL($data, "GUARDIAN_ADDRESS_DAT");
            $db->query($query);

            $cnt++;
        }
        $db->commit();
        Query::dbCheckIn($db);

        return $cnt;
    }

    //削除（ＣＳＶデータより読込）保護者
    public function deleteQueryCsv($model, &$data_arr)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $cnt = 0;   //処理件数
        for ($i = 0; $i < get_count($data_arr); $i++) {
            /** GUARDIAN_DAT **/
            $query  = " DELETE FROM GUARDIAN_DAT ";
            $query .= " WHERE SCHREGNO = '".$data_arr[$i]["SCHREGNO"]."' ";
            $db->query($query);

            /** GUARDIAN_ADDRESS_DAT **/
            $query  = " DELETE FROM GUARDIAN_ADDRESS_DAT ";
            $query .= " WHERE SCHREGNO = '".$data_arr[$i]["SCHREGNO"]."' ";
            $db->query($query);
            $cnt++;
        }
        $db->commit();
        Query::dbCheckIn($db);

        return $cnt;
    }

    /**********************/
    /**  存在チェック用  **/
    /**********************/

    //処理年度以前の在籍データに同じ学籍番号があるかチェックする用
    public function schregnoExist($model, $schregno, $db)
    {
        //SCHREG_BASE_MSTをチェック
        $query  = "SELECT SCHREGNO FROM SCHREG_BASE_MST";
        $query .= " WHERE SCHREGNO = '".$schregno."'";
        $ret_val1 = $db->getOne($query);

        if (isset($ret_val1)) {
            return true;
        }
    }

    //学籍番号と入学日が一致するかチェックする
    public function schregnoExistBase($model, $schregno, $entDate, $db)
    {
        //SCHREG_BASE_MSTをチェック
        $query  = "SELECT SCHREGNO FROM SCHREG_BASE_MST";
        $query .= " WHERE SCHREGNO = '".$schregno."'";
        $query .= "   AND ENT_DATE = '".str_replace("/", "-", $entDate)."'";
        $ret_val1 = $db->getOne($query);

        if (isset($ret_val1)) {
            return true;
        } else {
            return false;
        }
    }

    //名称マスタ取得
    public function getNameMst($db, $year, $cd1, $cd2)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$year."' ";
        $query .= "     AND NAMECD1 = '".$cd1."' ";
        $query .= "     AND NAMECD2 = '".$cd2."' ";

        $rtnOne = $db->getOne($query);
        return $rtnOne;
    }

    //課程学科取得
    public function getCourseMajor($year)
    {
        $query  = " SELECT ";
        $query .= "     COURSECD || '-' || MAJORCD AS VALUE, ";
        $query .= "     COURSECD || MAJORCD || '　' || COURSENAME || '　' || MAJORNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_COURSE_MAJOR_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$year."'";
        $query .= " ORDER BY ";
        $query .= "     COURSECD, MAJORCD";

        return $query;
    }

    //コース取得
    public function getCourseCode($model, $coursecode)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     V_COURSECODE_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR = '".$model->field["YEAR"]."' ";
        $query .= "     AND COURSECODE = '".$coursecode."' ";

        return $query;
    }

    //入試種別取得
    public function getTestDivCode($model, $testDiv)
    {
        $setApplicantDiv = ($model->schoolKind == "J") ? "1": "2";//(1:中学 2:高校)

        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_TESTDIV_MST ";
        $query .= " WHERE ";
        $query .= "         ENTEXAMYEAR  = '".$model->field["YEAR"]."' ";
        $query .= "     AND APPLICANTDIV = '".$setApplicantDiv."' ";
        $query .= "     AND TESTDIV      = '".$testDiv."' ";

        return $query;
    }

    //入試方式取得
    public function getExamTypeCode($model, $examType)
    {
        $setApplicantDiv = ($model->schoolKind == "J") ? "1": "2";//(1:中学 2:高校)

        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_EXAMTYPE_MST ";
        $query .= " WHERE ";
        $query .= "         ENTEXAMYEAR  = '".$model->field["YEAR"]."' ";
        $query .= "     AND APPLICANTDIV = '".$setApplicantDiv."' ";
        $query .= "     AND EXAM_TYPE    = '".$examType."' ";

        return $query;
    }

    /********************/
    /**  エラー処理用  **/
    /********************/

    //エラーＤＢへの追加
    public function insertQueryErr(&$db, $record_no, $check_error)
    {
        $data1["PROGRAMID"][TEXT]       = PROGRAMID;
        $data1["MSGROW"][NUMBER]        = $record_no;
        $data1["MSGREMARK"][TEXT]       = $check_error;

        $query = Query::insertSQL($data1, "W_CSVMSG_PRG_DAT");

        $result = $db->query($query);
    }

    public function getSchregBaseDetail($db, $schregno)
    {
        $query  = "SELECT SCHREGNO FROM SCHREG_BASE_DETAIL_MST";
        $query .= " WHERE SCHREGNO = '".$schregno."' AND";
        $query .= " BASE_SEQ = '014'";
        $ret_val1 = $db->getOne($query);
        if (isset($ret_val1)) {
            return true;
        }
    }

    public function getSchregDat($db, $data, $schregno)
    {
        $query  = "SELECT SCHREGNO FROM SCHREG_REGD_DAT";
        $query .= " WHERE SCHREGNO = '".$schregno."' AND";
        $query .= " YEAR = '".$data["ENTERYEAR"]."' AND";
        $query .= " SEMESTER = '".$data["SEMESTER"]."'";
        $ret_val1 = $db->getOne($query);
        if (isset($ret_val1)) {
            return true;
        }
    }

    public function getSchregAddressDat($db, $model, $schregno)
    {
        $query  = "SELECT SCHREGNO FROM SCHREG_ADDRESS_DAT";
        $query .= " WHERE SCHREGNO = '".$schregno."' AND";
        $query .= " ISSUEDATE = '".str_replace("/", "-", $model->field["GUARD_ISSUEDATE"])."'";
        $ret_val1 = $db->getOne($query);
        if (isset($ret_val1)) {
            return true;
        }
    }

    public function getSchregDomitoryHistDat($db, $schregno, $data)
    {
        $query  = "SELECT SCHREGNO FROM SCHREG_DOMITORY_HIST_DAT";
        $query .= " WHERE SCHREGNO = '".$schregno."' AND";
        $query .= " DOMI_ENTDAY = '".str_replace("/", "-", $data["ENT_DATE"])."'";
        $ret_val1 = $db->getOne($query);
        if (isset($ret_val1)) {
            return true;
        }
    }

    public function getGuardianDat($db, $schregno)
    {
        $query  = "SELECT SCHREGNO FROM GUARDIAN_DAT";
        $query .= " WHERE SCHREGNO = '".$schregno."'";
        $ret_val1 = $db->getOne($query);
        if (isset($ret_val1)) {
            return true;
        }
    }

    public function getGuardianAddressDat($db, $schregno)
    {
        $query  = "SELECT SCHREGNO FROM GUARDIAN_ADDRESS_DAT";
        $query .= " WHERE SCHREGNO = '".$schregno."'";
        $ret_val1 = $db->getOne($query);
        if (isset($ret_val1)) {
            return true;
        }
    }
}
?>
