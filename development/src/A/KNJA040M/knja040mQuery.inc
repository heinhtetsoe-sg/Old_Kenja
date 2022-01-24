<?php

require_once('for_php7.php');

class knja040mQuery extends Query
{
    //年度取得
    public function getYear($model)
    {
        $query  = " WITH YEAR_T(VALUE, LABEL) AS ( ";
        $query .= "     VALUES('".CTRL_YEAR."', '".CTRL_YEAR."') ";
        $query .= "     UNION ";
        $query .= "     VALUES('".(CTRL_YEAR + 1)."', '".(CTRL_YEAR + 1)."') ";
        $query .= " ) ";

        $query .= " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     YEAR_T ";
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
        $query .= "     YEAR    = '".$model->field["EXE_YEAR"]."' ";
        if ($model->Properties["useSchool_KindField"] == "1") {
            $query .= " AND SCHOOLCD    = '".sprintf("%012d", SCHOOLCD)."' ";
            $query .= " AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        }

        return $query;
    }

    //在籍期間取得(A023)
    public function getA023GradeRange()
    {
        $query  = " SELECT ";
        $query .= "     INT(NAMESPARE2) - INT(NAME2) AS RANGE ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'A023' AND ";
        $query .= "     NAME1   = '".SCHOOLKIND."' ";
        $query .= " ORDER BY ";
        $query .= "   NAMECD2 ";

        return $query;
    }

    //前回実行データ取得
    public function getMaxUpdate($db)
    {
        $query = " SELECT DATE(MAX(UPDATED)), TIME(MAX(UPDATED)) FROM FRESHMAN_DAT ";
        $tmp   = $db->getRow($query);

        $date1 = ($tmp[0]=="") ? null : strftime("%Y年%m月%d日 %H時%M分", strtotime($tmp[0]." ".$tmp[1]));
        return $date1;
    }

    public function insertQuery($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //DELETE
        $query  = " DELETE FROM ";
        $query .= "     FRESHMAN_DAT ";
        $query .= " WHERE ";
        $query .= "     ENTERYEAR = '".$model->field["EXE_YEAR"]."' ";
        $db->query($query);

        //INSERT
        for ($i = 0; $i < get_count($model->data_arr); $i++) {
            /******************/
            /*  FRESHMAN_DAT  */
            /******************/
            $data = array();
            $data["ENTERYEAR"][TEXT]        = $model->data_arr[$i]["ENTERYEAR"];
            $data["SCHREGNO"][TEXT]         = $model->data_arr[$i]["SCHREGNO"];
            $data["ENT_DIV"][TEXT]          = (0 != $model->data_arr[$i]["ENT_DIV"]) ? $model->data_arr[$i]["ENT_DIV"] : "2";
            $data["GRADE"][TEXT]            = $model->data_arr[$i]["GRADE"];
            $data["HR_CLASS"][TEXT]         = $model->data_arr[$i]["HR_CLASS"];
            $data["ATTENDNO"][TEXT]         = $model->data_arr[$i]["ATTENDNO"];
            $data["INOUTCD"][TEXT]          = $model->data_arr[$i]["INOUTCD"];
            $data["COURSECD"][TEXT]         = $model->data_arr[$i]["COURSECD"];
            $data["MAJORCD"][TEXT]          = $model->data_arr[$i]["MAJORCD"];
            $data["COURSECODE"][TEXT]       = $model->data_arr[$i]["COURSECODE"];
            $data["NAME"][TEXT]             = $model->data_arr[$i]["NAME"];
            $data["NAME_KANA"][TEXT]        = $model->data_arr[$i]["NAME_KANA"];
            if (strlen($model->data_arr[$i]["BIRTHDAY"]) > 0) {
                $data["BIRTHDAY"][FUNC]         = "date('".str_replace("/", "-", $model->data_arr[$i]["BIRTHDAY"])."')";
            } else {
                $data["BIRTHDAY"][FUNC]         = "";
            }
            $data["SEX"][TEXT]              = $model->data_arr[$i]["SEX"];
            $data["FINSCHOOLCD"][TEXT]      = $model->data_arr[$i]["FINSCHOOLCD"];
            if (strlen($model->data_arr[$i]["FINSCHOOLGRADDATE"]) > 0) {
                $data["FINSCHOOLGRADDATE"][FUNC]    = "date('".str_replace("/", "-", $model->data_arr[$i]["FINSCHOOLGRADDATE"])."')";
            } else {
                $data["FINSCHOOLGRADDATE"][FUNC]    = "CAST(NULL as date)";
            }
            $data["ADDR_FLG"][TEXT]         = $model->field["GUARD_ADDR_FLG"] ? $model->field["GUARD_ADDR_FLG"] : null;
            $data["ISSUEDATE"][TEXT]        = str_replace("/", "-", $model->field["GUARD_ISSUEDATE"]);
            $data["EXPIREDATE"][TEXT]       = str_replace("/", "-", $model->field["GUARD_EXPIREDATE"]);
            $data["ZIPCD"][TEXT]            = $model->data_arr[$i]["ZIPCD"];
            $data["ADDR1"][TEXT]            = $model->data_arr[$i]["ADDRESS1"];
            $data["ADDR2"][TEXT]            = $model->data_arr[$i]["ADDRESS2"];
            $data["TELNO"][TEXT]            = $model->data_arr[$i]["TELNO"];
            $data["FAXNO"][TEXT]            = $model->data_arr[$i]["FAXNO"];
            $data["EMAIL"][TEXT]            = $model->data_arr[$i]["EMAIL"];
            $data["EMERGENCYCALL"][TEXT]    = $model->data_arr[$i]["EMERGENCYCALL"];
            $data["EMERGENCYTELNO"][TEXT]   = $model->data_arr[$i]["EMERGENCYTELNO"];
            $data["SCALASHIPDIV"][TEXT]     = ($model->data_arr[$i]["SCALASHIPDIV"]) ? sprintf("%02d", $model->data_arr[$i]["SCALASHIPDIV"]) : "";
            $data["CURRICULUM_YEAR"][TEXT]  = $model->data_arr[$i]["CURRICULUM_YEAR"];
            $data["EXAMNO"][TEXT]           = $model->data_arr[$i]["EXAMNO"];
            //AREACDがnullの時、00をセット
            if ($model->data_arr[$i]["AREACD"] !== '') {
                $data["AREACD"][TEXT]           = $model->data_arr[$i]["AREACD"];
            } else {
                $data["AREACD"][TEXT]           = '00';
            }
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][FUNC]          = "SYSDATE()";

            $query = Query::insertSQL($data, "FRESHMAN_DAT");
            $db->query($query);

            /*********************/
            /*  SCHREG_BASE_MST  */
            /*********************/
            $data = array();
            $data["SCHREGNO"][TEXT]         = $model->data_arr[$i]["SCHREGNO"];
            $data["INOUTCD"][TEXT]          = $model->data_arr[$i]["INOUTCD"];
            $data["NAME"][TEXT]             = $model->data_arr[$i]["NAME"];
            $data["NAME_SHOW"][TEXT]        = $model->data_arr[$i]["NAME"];
            $data["NAME_KANA"][TEXT]        = $model->data_arr[$i]["NAME_KANA"];
            if (strlen($model->data_arr[$i]["BIRTHDAY"]) > 0) {
                $data["BIRTHDAY"][FUNC]         = "date('".str_replace("/", "-", $model->data_arr[$i]["BIRTHDAY"])."')";
            } else {
                $data["BIRTHDAY"][FUNC]         = "";
            }
            $data["SEX"][TEXT]              = $model->data_arr[$i]["SEX"];
            $data["FINSCHOOLCD"][TEXT]      = $model->data_arr[$i]["FINSCHOOLCD"];
            if (strlen($model->data_arr[$i]["FINSCHOOLGRADDATE"]) > 0) {
                $data["FINISH_DATE"][FUNC]      = "date('".str_replace("/", "-", $model->data_arr[$i]["FINSCHOOLGRADDATE"])."')";
            } else {
                $data["FINISH_DATE"][FUNC]      = "CAST(NULL as date)";
            }
            $data["ENT_DIV"][TEXT]          = (0 != $model->data_arr[$i]["ENT_DIV"]) ? $model->data_arr[$i]["ENT_DIV"] : "2";
            $data["EMERGENCYCALL"][TEXT]    = $model->data_arr[$i]["EMERGENCYCALL"];
            $data["EMERGENCYTELNO"][TEXT]   = $model->data_arr[$i]["EMERGENCYTELNO"];
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][FUNC]          = "SYSDATE()";

            $query = Query::insertSQL($data, "SCHREG_BASE_MST");
            $db->query($query);

            /*****************************/
            /*  SCHREG_ENT_GRD_HIST_DAT  */
            /*****************************/
            $data = array();
            $data["SCHREGNO"][TEXT]         = $model->data_arr[$i]["SCHREGNO"];
            $data["SCHOOL_KIND"][TEXT]      = $model->data_arr[$i]["SCHOOL_KIND"];
            $data["FINSCHOOLCD"][TEXT]      = $model->data_arr[$i]["FINSCHOOLCD"];
            if (strlen($model->data_arr[$i]["FINSCHOOLGRADDATE"]) > 0) {
                $data["FINISH_DATE"][FUNC]      = "date('".str_replace("/", "-", $model->data_arr[$i]["FINSCHOOLGRADDATE"])."')";
            } else {
                $data["FINISH_DATE"][FUNC]      = "CAST(NULL as date)";
            }
            $data["CURRICULUM_YEAR"][TEXT]  = $model->data_arr[$i]["CURRICULUM_YEAR"];
            $data["ENT_DIV"][TEXT]          = (0 != $model->data_arr[$i]["ENT_DIV"]) ? $model->data_arr[$i]["ENT_DIV"] : "2";
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][FUNC]          = "SYSDATE()";

            $query = Query::insertSQL($data, "SCHREG_ENT_GRD_HIST_DAT");
            $db->query($query);

            /**********************************/
            /*  SCHREG_BASE_DETAIL_MST : 002  */
            /**********************************/
            //出身学校名
            if ($model->data_arr[$i]["SCHOOL_KIND"] == "P" || $model->data_arr[$i]["SCHOOL_KIND"] == "K") {
                //SCHREG_BASE_DETAIL_MSTのデータ確認
                $detail002 = $db->getOne(knja040mQuery::countSchregBaseDetailMst($model->data_arr[$i]["SCHREGNO"], '002'));

                //更新処理
                $data2 = array();
                if ($detail002 == "0") {
                    $data2["SCHREGNO"][TEXT]  = $model->data_arr[$i]["SCHREGNO"];
                    $data2["BASE_SEQ"][TEXT]  = '002';
                }
                $data2["BASE_REMARK1"][TEXT]  = $model->data_arr[$i]["FINSCHOOLNAME"];
                $data2["REGISTERCD"][TEXT]    = STAFFCD;
                $data2["UPDATED"][FUNC]       = "SYSDATE()";

                $where  = " WHERE SCHREGNO  = '".$model->data_arr[$i]["SCHREGNO"]."'";
                $where .= "   AND BASE_SEQ  = '002'";

                if ($detail002 == "0") {
                    $query = Query::insertSQL($data2, "SCHREG_BASE_DETAIL_MST");
                } else {
                    $query = Query::updateSQL($data2, "SCHREG_BASE_DETAIL_MST", $where);
                }
                $db->query($query);
            }

            /**********************************/
            /*  SCHREG_BASE_DETAIL_MST : 003  */
            /**********************************/
            //SCHREG_BASE_DETAIL_MSTのデータ確認
            $detail003 = $db->getOne(knja040mQuery::countSchregBaseDetailMst($model->data_arr[$i]["SCHREGNO"], '003'));

            $data = array();
            if ($detail003 == "0") {
                $data["SCHREGNO"][TEXT]         = $model->data_arr[$i]["SCHREGNO"];
                $data["BASE_SEQ"][TEXT]         = '003';
            }
            $data["BASE_REMARK1"][TEXT]     = $model->data_arr[$i]["EXAMNO"];
            $data["BASE_REMARK2"][TEXT]     = $model->data_arr[$i]["TESTDIV"];
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][FUNC]          = "SYSDATE()";

            $where  = " WHERE SCHREGNO  = '".$model->data_arr[$i]["SCHREGNO"]."' ";
            $where .= "   AND BASE_SEQ  = '003'";
            
            if (!$detail003) {
                $query = Query::insertSQL($data, "SCHREG_BASE_DETAIL_MST");
            } else {
                $query = Query::updateSQL($data, "SCHREG_BASE_DETAIL_MST", $where);
            }
            $db->query($query);

            /**********************************/
            /*  SCHREG_BASE_DETAIL_MST : 004  */
            /**********************************/
            //無償回数の更新
            if ($model->Properties["useMusyouKaisu"] == "1") {
                //SCHREG_BASE_DETAIL_MSTのデータ確認
                $detail004 = $db->getOne(knja040mQuery::countSchregBaseDetailMst($model->data_arr[$i]["SCHREGNO"], '004'));

                $data = array();
                if ($detail004 == "0") {
                    $data["SCHREGNO"][TEXT]         = $model->data_arr[$i]["SCHREGNO"];
                    $data["BASE_SEQ"][TEXT]         = '004';
                }
                $data["BASE_REMARK4"][TEXT]     = $model->data_arr[$i]["MUSYOU_KAISU"];
                $data["REGISTERCD"][TEXT]       = STAFFCD;
                $data["UPDATED"][FUNC]          = "SYSDATE()";

                $where  = " WHERE SCHREGNO  = '".$model->data_arr[$i]["SCHREGNO"]."' ";
                $where .= "   AND BASE_SEQ  = '004'";

                if (!$detail004) {
                    $query = Query::insertSQL($data, "SCHREG_BASE_DETAIL_MST");
                } else {
                    $query = Query::updateSQL($data, "SCHREG_BASE_DETAIL_MST", $where);
                }
                $db->query($query);
            }
        }

        $db->commit();
        Query::dbCheckIn($db);
        return;
    }

    //名称マスタ学校取得
    public function checkSchool()
    {
        $query  = "SELECT NAME1, NAMESPARE2 ";
        $query .= " FROM NAME_MST";
        $query .= " WHERE NAMECD1 = 'Z010' ";
        $query .= "       AND NAMECD2 = '00' ";

        return $query;
    }

    //Z053
    public function getCsvFormatType()
    {
        $query  = " SELECT ";
        $query .= "     NAMESPARE1 ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'Z053' ";
        $query .= "     AND NAME1 = 'KNJA040M' ";

        return $query;
    }

    //SCHREG_BASE_DETAIL_MST　データ確認
    public function countSchregBaseDetailMst($schregno, $seq)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     SCHREG_BASE_DETAIL_MST ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '".$schregno."' AND ";
        $query .= "     BASE_SEQ = '".$seq."' ";

        return $query;
    }

    //同じ学籍番号があるかチェックする用
    public function schregnoExist($model, $schregno, $db)
    {
        //SCHREG_BASE_MSTをチェック
        $query  = " SELECT ";
        $query .= "     SCHREGNO ";
        $query .= " FROM ";
        $query .= "     SCHREG_BASE_MST";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '".$schregno."' ";
        $ret_val1 = $db->getOne($query);

        if (isset($ret_val1)) {
            return true;
        }
    }

    //存在チェック -- 名称マスタ
    public function checkNameMst($model, $cd1, $cd2)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR    = '".$model->field["EXE_YEAR"]."' AND ";
        $query .= "     NAMECD1 = '".$cd1."' AND ";
        $query .= "     NAMECD2 = '".$cd2."' ";

        return $query;
    }
    //存在チェック -- 課程マスタ
    public function checkCourseCd($model, $coursecd)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     V_COURSE_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".$model->field["EXE_YEAR"]."' AND ";
        $query .= "     COURSECD    = '".$coursecd."' ";

        return $query;
    }
    //存在チェック -- 学科マスタ
    public function checkMajorCd($model, $coursecd, $majorcd)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     V_MAJOR_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".$model->field["EXE_YEAR"]."' AND ";
        $query .= "     COURSECD    = '".$coursecd."' AND ";
        $query .= "     MAJORCD     = '".$majorcd."' ";

        return $query;
    }
    //存在チェック -- コースコードマスタ
    public function checkCourseCode($model, $coursecode)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     V_COURSECODE_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".$model->field["EXE_YEAR"]."' AND ";
        $query .= "     COURSECODE  = '".$coursecode."' ";

        return $query;
    }

    //校種取得
    public function getSchoolKind($model, $grade)
    {
        $query  = " SELECT ";
        $query .= "     SCHOOL_KIND ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR    = '".$model->field["EXE_YEAR"]."' AND ";
        $query .= "     GRADE   = '".$grade."' ";

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

    //マスタの追加（ＣＳＶデータより読込）
    public function insertQueryCsv($model, &$data_arr)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $cnt = 0;   //処理件数
        for ($i = 0; $i < get_count($data_arr); $i++) {
            //データセット
            $data = array();
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

            $query = "DELETE FROM GUARDIAN_ADDRESS_DAT WHERE SCHREGNO = '".$data_arr[$i]["SCHREGNO"]."'";
            $db->query($query);

            $data = array();
            $data["SCHREGNO"][TEXT]       = $data_arr[$i]["SCHREGNO"];
            $data["ISSUEDATE"][TEXT]      = str_replace("/", "-", $model->field["GUARD_ISSUEDATE"]);
            $data["EXPIREDATE"][TEXT]     = str_replace("/", "-", $model->field["GUARD_EXPIREDATE"]);
            $data["GUARD_ZIPCD"][TEXT]    = $data_arr[$i]["GUARD_ZIPCD"];
            $data["GUARD_ADDR1"][TEXT]    = $data_arr[$i]["GUARD_ADDR1"];
            $data["GUARD_ADDR2"][TEXT]    = $data_arr[$i]["GUARD_ADDR2"];
            $data["GUARD_ADDR_FLG"][TEXT] = $model->field["GUARD_ADDR_FLG"] ? $model->field["GUARD_ADDR_FLG"] : null;
            $data["GUARD_TELNO"][TEXT]    = $data_arr[$i]["GUARD_TELNO"];
            $data["GUARD_FAXNO"][TEXT]    = $data_arr[$i]["GUARD_FAXNO"];
            $data["GUARD_E_MAIL"][TEXT]   = $data_arr[$i]["GUARD_E_MAIL"];
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

    //名称マスタ存在チェック
    public function checkMstData($namecd1, $namecd2)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = '".$namecd1."' AND ";
        $query .= "     NAMECD2 = '".$namecd2."' ";

        return $query;
    }

    //SCHREG_ENT_GRD_HIST_DAT　重複(登録済)チェック
    public function checkSchregEntGrdHistDat($schregno, $schoolkind)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     SCHREG_ENT_GRD_HIST_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '".$schregno."' AND ";
        $query .= "     SCHOOL_KIND = '".$schoolkind."' ";

        return $query;
    }
}
?>
