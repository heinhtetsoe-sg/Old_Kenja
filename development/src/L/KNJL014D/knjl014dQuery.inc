<?php

require_once('for_php7.php');
class knjl014dquery extends Query
{
    /******************/
    /**  画面で使用  **/
    /******************/
    //名称マスタより取得
    public function getNameMst($year, $namecd1, $namecd2 = "")
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     NAME1, ";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL, ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     NAMESPARE2 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR    = '".$year."' ";
        $query .= "     AND NAMECD1 = '".$namecd1."' ";
        if ($namecd2) {
            $query .= " AND NAMECD2 = '".$namecd2."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    /************************/
    /**  ＣＳＶ取込で使用  **/
    /************************/
    //願書の追加（ＣＳＶデータより読込）
    public function updateQueryCsv1($model, &$data_arr)
    {
        $data = array();
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $cnt = 0;   //処理件数
        for ($i = 0; $i < get_count($data_arr); $i++) {
            //データ件数取得
            $base_cnt       = $db->getOne(knjl014dQuery::cntEntexamApplicantbaseDat($model, $data_arr[$i]));
            $addr_cnt       = $db->getOne(knjl014dQuery::cntEntexamApplicantaddrDat($model, $data_arr[$i]));
            $baseD008_cnt   = $db->getOne(knjl014dQuery::cntEntexamApplicantbaseDetailDat($model, $data_arr[$i], "008"));
            $baseD015_cnt   = $db->getOne(knjl014dQuery::cntEntexamApplicantbaseDetailDat($model, $data_arr[$i], "015"));

            //ENTEXAM_APPLICANTBASE_DAT
            $data = array();
            $data["ENTEXAMYEAR"][TEXT]          = $model->ObjYear;
            $data["APPLICANTDIV"][TEXT]         = $model->applicantdiv;
            $data["TESTDIV"][TEXT]              = $data_arr[$i]["TESTDIV"];
            $data["SHDIV"][TEXT]                = $data_arr[$i]["SHDIV"];
            $data["DESIREDIV"][TEXT]            = $data_arr[$i]["DESIREDIV"];
            $data["EXAMNO"][TEXT]               = $data_arr[$i]["EXAMNO"];
            $data["NAME"][TEXT]                 = $data_arr[$i]["NAME"];
            $data["NAME_KANA"][TEXT]            = $data_arr[$i]["NAME_KANA"];
            $data["SEX"][TEXT]                  = $data_arr[$i]["SEX"];
            $data["ERACD"][TEXT]                = $data_arr[$i]["ERACD"];
            $data["BIRTH_Y"][TEXT]              = $data_arr[$i]["BIRTH_Y"];
            $data["BIRTH_M"][TEXT]              = $data_arr[$i]["BIRTH_M"];
            $data["BIRTH_D"][TEXT]              = $data_arr[$i]["BIRTH_D"];
            $data["BIRTHDAY"][TEXT]             = "";
            $year = common::getDateHenkan($data_arr[$i]["ERACD"].sprintf("%02s", $data_arr[$i]["BIRTH_Y"]), "wy");
            if (checkdate((int)$data_arr[$i]["BIRTH_M"], (int)$data_arr[$i]["BIRTH_D"], (int)$year)) {
                $data["BIRTHDAY"][TEXT]       = $year."-".sprintf("%02s", $data_arr[$i]["BIRTH_M"])."-".sprintf("%02s", $data_arr[$i]["BIRTH_D"]);
            }
            $data["FS_CD"][TEXT]                = $data_arr[$i]["FS_CD"];
            $data["FS_GRDYEAR"][TEXT]           = $data_arr[$i]["FS_GRDYEAR"];
            $data["REMARK1"][TEXT]              = $data_arr[$i]["REMARK1"];
            $data["REMARK2"][TEXT]              = $data_arr[$i]["REMARK2"];
            $data["REGISTERCD"][TEXT]           = STAFFCD;
            $data["UPDATED"][NUMBER]            = "SYSDATE()";

            $where  = " WHERE ENTEXAMYEAR   = '{$model->ObjYear}' ";
            $where .= "   AND APPLICANTDIV  = '{$model->applicantdiv}' ";
            $where .= "   AND EXAMNO        = '{$data_arr[$i]["EXAMNO"]}' ";

            if (0 == $base_cnt) {
                $query = Query::insertSQL($data, "ENTEXAM_APPLICANTBASE_DAT");
            } else {
                $query = Query::updateSQL($data, "ENTEXAM_APPLICANTBASE_DAT", $where);
            }
            $db->query($query);

            //ENTEXAM_APPLICANTADDR_DAT
            $data = array();
            $data["ENTEXAMYEAR"][TEXT]      = $model->ObjYear;
            $data["APPLICANTDIV"][TEXT]     = $model->applicantdiv;
            $data["EXAMNO"][TEXT]           = $data_arr[$i]["EXAMNO"];
            $data["ZIPCD"][TEXT]            = $data_arr[$i]["ZIPCD"];
            $data["ADDRESS1"][TEXT]         = $data_arr[$i]["ADDRESS1"];
            $data["ADDRESS2"][TEXT]         = $data_arr[$i]["ADDRESS2"];
            $data["TELNO"][TEXT]            = $data_arr[$i]["TELNO"];
            $data["GKANA"][TEXT]            = $data_arr[$i]["GKANA"];
            $data["RELATIONSHIP"][TEXT]     = $data_arr[$i]["RELATIONSHIP"];
            $data["GNAME"][TEXT]            = $data_arr[$i]["GNAME"];
            $data["GZIPCD"][TEXT]           = $data_arr[$i]["GZIPCD"];
            $data["GADDRESS1"][TEXT]        = $data_arr[$i]["GADDRESS1"];
            $data["GADDRESS2"][TEXT]        = $data_arr[$i]["GADDRESS2"];
            $data["GTELNO"][TEXT]           = $data_arr[$i]["GTELNO"];
            $data["RELATIONSHIP"][TEXT]     = $data_arr[$i]["RELATIONSHIP"];
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][NUMBER]        = "SYSDATE()";

            $where  = " WHERE ENTEXAMYEAR   = '{$model->ObjYear}' ";
            $where .= "   AND APPLICANTDIV  = '{$model->applicantdiv}' ";
            $where .= "   AND EXAMNO        = '{$data_arr[$i]["EXAMNO"]}' ";

            if (0 == $addr_cnt) {
                $query = Query::insertSQL($data, "ENTEXAM_APPLICANTADDR_DAT");
            } else {
                $query = Query::updateSQL($data, "ENTEXAM_APPLICANTADDR_DAT", $where);
            }
            $db->query($query);

            //ENTEXAM_APPLICANTBASE_DETAIL_DAT SEQ=008
            $data = array();
            $data["ENTEXAMYEAR"][TEXT]      = $model->ObjYear;
            $data["APPLICANTDIV"][TEXT]     = $model->applicantdiv;
            $data["EXAMNO"][TEXT]           = $data_arr[$i]["EXAMNO"];
            $data["SEQ"][TEXT]              = "008";
            $data["REMARK1"][TEXT]          = $data_arr[$i]["PRISCHOOLCD1"];
            $data["REMARK3"][TEXT]          = $data_arr[$i]["PRISCHOOL_CLASS_CD1"];
            $data["REMARK4"][TEXT]          = $data_arr[$i]["PRISCHOOLCD2"];
            $data["REMARK5"][TEXT]          = $data_arr[$i]["PRISCHOOL_CLASS_CD2"];
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][NUMBER]        = "SYSDATE()";

            $where  = " WHERE ENTEXAMYEAR   = '{$model->ObjYear}' ";
            $where .= "   AND APPLICANTDIV  = '{$model->applicantdiv}' ";
            $where .= "   AND EXAMNO        = '{$data_arr[$i]["EXAMNO"]}' ";
            $where .= "   AND SEQ           = '008' ";

            if (0 == $baseD008_cnt) {
                $query = Query::insertSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT");
            } else {
                $query = Query::updateSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT", $where);
            }
            $db->query($query);

            //ENTEXAM_APPLICANTBASE_DETAIL_DAT SEQ=015
            $data = array();
            $data["ENTEXAMYEAR"][TEXT]      = $model->ObjYear;
            $data["APPLICANTDIV"][TEXT]     = $model->applicantdiv;
            $data["EXAMNO"][TEXT]           = $data_arr[$i]["EXAMNO"];
            $data["SEQ"][TEXT]              = "015";
            $data["REMARK1"][TEXT]          = $data_arr[$i]["CHILD"];
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][NUMBER]        = "SYSDATE()";

            $where  = " WHERE ENTEXAMYEAR   = '{$model->ObjYear}' ";
            $where .= "   AND APPLICANTDIV  = '{$model->applicantdiv}' ";
            $where .= "   AND EXAMNO        = '{$data_arr[$i]["EXAMNO"]}' ";
            $where .= "   AND SEQ           = '015' ";

            if (0 == $baseD015_cnt) {
                $query = Query::insertSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT");
            } else {
                $query = Query::updateSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT", $where);
            }
            $db->query($query);

            $cnt++;
        }
        $db->commit();
        Query::dbCheckIn($db);

        return $cnt;
    }

    //調査書の追加（ＣＳＶデータより読込）
    public function updateQueryCsv2($model, &$data_arr)
    {
        $data = array();
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //教科取得
        $class_array = $total5_class = array();
        $result = $db->query(knjl014dQuery::getNameMstAll($model->ObjYear, "L008"));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $class_array[] = $row["NAMECD2"];
            if ($row["NAMESPARE1"] == "1") {
                $total5_class[] = $row["NAMECD2"];
            }
        }

        $cnt = 0;   //処理件数
        for ($i = 0; $i < get_count($data_arr); $i++) {
            //データ件数取得
            $confRpt_cnt        = $db->getOne(knjl014dQuery::cntEntexamApplicantconfrptDat($model, $data_arr[$i]));
            $confRptD001_cnt    = $db->getOne(knjl014dQuery::cntEntexamApplicantconfrptDetailDat($model, $data_arr[$i], "001"));
            $confRptD002_cnt    = $db->getOne(knjl014dQuery::cntEntexamApplicantconfrptDetailDat($model, $data_arr[$i], "002"));
            $confRptD003_cnt    = $db->getOne(knjl014dQuery::cntEntexamApplicantconfrptDetailDat($model, $data_arr[$i], "003"));

            //ENTEXAM_APPLICANTCONFRPT_DAT
            $sum5 = $sumAll = 0;
            $data = array();
            $data["ENTEXAMYEAR"][TEXT]          = $model->ObjYear;
            $data["APPLICANTDIV"][TEXT]         = $model->applicantdiv;
            $data["EXAMNO"][TEXT]               = $data_arr[$i]["EXAMNO"];
            foreach ($class_array as $class) {
                $confrpt = ($data_arr[$i]["CONFRPT".$class."_3"]) ? $data_arr[$i]["CONFRPT".$class."_3"] : 0;
                $data["CONFIDENTIAL_RPT".$class][NUMBER] = $confrpt;
                if (in_array($class, $total5_class)) {
                    $sum5 += $confrpt;
                }
                $sumAll += $confrpt;
            }
            $data["TOTAL5"][NUMBER]             = $sum5;
            $data["TOTAL_ALL"][NUMBER]          = $sumAll;
            $data["ABSENCE_DAYS"][NUMBER]       = $data_arr[$i]["ABSENCE_DAYS1"];
            $data["ABSENCE_DAYS2"][NUMBER]      = $data_arr[$i]["ABSENCE_DAYS2"];
            $data["ABSENCE_DAYS3"][NUMBER]      = $data_arr[$i]["ABSENCE_DAYS3"];
            $data["REGISTERCD"][TEXT]           = STAFFCD;
            $data["UPDATED"][NUMBER]            = "SYSDATE()";

            $where  = " WHERE ENTEXAMYEAR   = '{$model->ObjYear}' ";
            $where .= "   AND APPLICANTDIV  = '{$model->applicantdiv}' ";
            $where .= "   AND EXAMNO        = '{$data_arr[$i]["EXAMNO"]}' ";

            if (0 == $confRpt_cnt) {
                $query = Query::insertSQL($data, "ENTEXAM_APPLICANTCONFRPT_DAT");
            } else {
                $query = Query::updateSQL($data, "ENTEXAM_APPLICANTCONFRPT_DAT", $where);
            }
            $db->query($query);

            //ENTEXAM_APPLICANTCONFRPT_DETAIL_DAT  SEQ=001,002
            for ($grade = 1; $grade <= 2; $grade++) {
                $seq = sprintf("%03d", $grade);
                $sum5 = $sumAll = 0;
                $data = array();
                $data["ENTEXAMYEAR"][TEXT]          = $model->ObjYear;
                $data["APPLICANTDIV"][TEXT]         = $model->applicantdiv;
                $data["EXAMNO"][TEXT]               = $data_arr[$i]["EXAMNO"];
                $data["SEQ"][TEXT]                  = $seq;
                foreach ($class_array as $class) {
                    $num = (int)$class;
                    if ($class == "12") {
                        continue;
                    }
                    $confrpt = ($data_arr[$i]["CONFRPT".$class."_".$grade]) ? $data_arr[$i]["CONFRPT".$class."_".$grade] : 0;
                    $data["REMARK".$num][TEXT]         = $confrpt;
                    if (in_array($class, $total5_class)) {
                        $sum5 += $confrpt;
                    }
                    $sumAll += $confrpt;
                }
                $data["REMARK10"][TEXT]             = $sum5;
                $data["REMARK11"][TEXT]             = $sumAll;
                $data["REGISTERCD"][TEXT]           = STAFFCD;
                $data["UPDATED"][NUMBER]            = "SYSDATE()";

                $where  = " WHERE ENTEXAMYEAR   = '{$model->ObjYear}' ";
                $where .= "   AND APPLICANTDIV  = '{$model->applicantdiv}' ";
                $where .= "   AND EXAMNO        = '{$data_arr[$i]["EXAMNO"]}' ";
                $where .= "   AND SEQ           = '{$seq}' ";

                $cntdata = "confRptD".$seq."_cnt";
                if (0 == $$cntdata) {
                    $query = Query::insertSQL($data, "ENTEXAM_APPLICANTCONFRPT_DETAIL_DAT");
                } else {
                    $query = Query::updateSQL($data, "ENTEXAM_APPLICANTCONFRPT_DETAIL_DAT", $where);
                }
                $db->query($query);
            }

            //ENTEXAM_APPLICANTCONFRPT_DETAIL_DAT  SEQ=003
            $data = array();
            $data["ENTEXAMYEAR"][TEXT]          = $model->ObjYear;
            $data["APPLICANTDIV"][TEXT]         = $model->applicantdiv;
            $data["EXAMNO"][TEXT]               = $data_arr[$i]["EXAMNO"];
            $data["SEQ"][TEXT]                  = '003';
            $data["REMARK1"][TEXT]              = $data_arr[$i]["REMARK1"];
            $data["REMARK2"][TEXT]              = $data_arr[$i]["REMARK2"];
            $data["REMARK3"][TEXT]              = $data_arr[$i]["REMARK3"];
            $data["REMARK4"][TEXT]              = $data_arr[$i]["REMARK4"];
            $data["REMARK5"][TEXT]              = $data_arr[$i]["REMARK5"];
            $data["REGISTERCD"][TEXT]           = STAFFCD;
            $data["UPDATED"][NUMBER]            = "SYSDATE()";

            $where  = " WHERE ENTEXAMYEAR   = '{$model->ObjYear}' ";
            $where .= "   AND APPLICANTDIV  = '{$model->applicantdiv}' ";
            $where .= "   AND EXAMNO        = '{$data_arr[$i]["EXAMNO"]}' ";
            $where .= "   AND SEQ           = '003' ";

            if (0 == $confRptD003_cnt) {
                $query = Query::insertSQL($data, "ENTEXAM_APPLICANTCONFRPT_DETAIL_DAT");
            } else {
                $query = Query::updateSQL($data, "ENTEXAM_APPLICANTCONFRPT_DETAIL_DAT", $where);
            }
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
    public function nameCntSql($namecd, $setcd, $where = "")
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

    //塾コード
    public function priSchCdCntSql($priSch_cd)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     PRISCHOOL_MST ";
        $query .= " WHERE ";
        $query .= "     PRISCHOOLCD = '{$priSch_cd}' ";

        return $query;
    }

    //塾(教室)コード
    public function priSchClassCdCntSql($priSch_cd, $classCd)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     PRISCHOOL_CLASS_MST ";
        $query .= " WHERE ";
        $query .= "         PRISCHOOLCD        = '{$priSch_cd}' ";
        $query .= "     AND PRISCHOOL_CLASS_CD = '{$classCd}' ";

        return $query;
    }

    /********************/
    /**  更新時に使用  **/
    /********************/

    //件数取得 -- ENTEXAM_APPLICANTBASE_DAT
    public function cntEntexamApplicantbaseDat($model, $data_arr)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DAT ";
        $query .= " WHERE ";
        $query .= "         ENTEXAMYEAR  = '{$model->ObjYear}' ";
        $query .= "     AND APPLICANTDIV = '{$model->applicantdiv}' ";
        $query .= "     AND EXAMNO       = '".sprintf("%0{$model->examLen}d", $data_arr["EXAMNO"])."' ";

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
        $query .= "     APPLICANTDIV    = '{$model->applicantdiv}' AND ";
        $query .= "     EXAMNO          = '{$data_arr["EXAMNO"]}' ";

        return $query;
    }

    //件数取得 -- ENTEXAM_APPLICANTBASE_DETAIL_DAT
    public function cntEntexamApplicantbaseDetailDat($model, $data_arr, $seq)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DETAIL_DAT ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR     = '{$model->ObjYear}' AND ";
        $query .= "     APPLICANTDIV    = '{$model->applicantdiv}' AND ";
        $query .= "     EXAMNO          = '{$data_arr["EXAMNO"]}' AND ";
        $query .= "     SEQ             = '{$seq}' ";

        return $query;
    }

    //件数取得 -- ENTEXAM_APPLICANTCONFRPT_DAT
    public function cntEntexamApplicantconfrptDat($model, $data_arr)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTCONFRPT_DAT ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR     = '{$model->ObjYear}' AND ";
        $query .= "     APPLICANTDIV    = '{$model->applicantdiv}' AND ";
        $query .= "     EXAMNO          = '{$data_arr["EXAMNO"]}' ";

        return $query;
    }

    //件数取得 -- ENTEXAM_APPLICANTCONFRPT_DETAIL_DAT
    public function cntEntexamApplicantconfrptDetailDat($model, $data_arr, $seq)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTCONFRPT_DETAIL_DAT ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR     = '{$model->ObjYear}' AND ";
        $query .= "     APPLICANTDIV    = '{$model->applicantdiv}' AND ";
        $query .= "     EXAMNO          = '{$data_arr["EXAMNO"]}' AND ";
        $query .= "     SEQ             = '{$seq}' ";

        return $query;
    }

    /************************/
    /**  ＣＳＶ出力で使用  **/
    /************************/

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

    //ヘッダ例用データを名称マスタより取得
    public function getHeaderReiNameMst($year, $namecd1, $name, $namecd2 = "", $where = "")
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     NAMECD2 || ':' || {$name} AS LABEL, ";
        $query .= "     NAMECD2 AS VALUE ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR    = '".$year."' AND ";
        $query .= "     NAMECD1 = '".$namecd1."' ";
        if ($namecd2 == "RELA") {
            $query .= " AND NAMECD2 IN ('01', '02') ";
        } elseif ($namecd2) {
            $query .= " AND NAMECD2 = '".$namecd2."' ";
        }
        if ($where) {
            $query .= " AND ".$where;
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //出身学校名取得
    public function getFinschoolDistcdName()
    {
        $query  = " SELECT ";
        $query .= "     T1.FINSCHOOLCD, ";
        $query .= "     VALUE(L1.NAME1,'') || T1.FINSCHOOL_NAME AS FINSCHOOL_DISTCD_NAME ";
        $query .= " FROM ";
        $query .= "     FINSCHOOL_MST T1 ";
        $query .= "     LEFT JOIN NAME_MST L1 ";
        $query .= "          ON L1.NAMECD1 = 'L001' ";
        $query .= "         AND L1.NAMECD2 = T1.FINSCHOOL_DISTCD ";

        return $query;
    }

    //CSVデータ出力（願書）
    public function selectMainQuery1($model)
    {
        $query .= " SELECT ";
        $query .= "     BASE.ENTEXAMYEAR, ";
        $query .= "     BASE.TESTDIV, ";
        $query .= "     '".$model->field["DATADIV"]."' AS DATADIV, ";
        $query .= "     BASE.SHDIV, ";
        $query .= "     BASE.DESIREDIV, ";
        $query .= "     BASE.EXAMNO, ";
        //志願者情報
        $query .= "     BASE.NAME, ";
        $query .= "     BASE.NAME_KANA, ";
        $query .= "     BASE.SEX, ";
        $query .= "     BASE.ERACD, ";
        $query .= "     BASE.BIRTH_Y, ";
        $query .= "     BASE.BIRTH_M, ";
        $query .= "     BASE.BIRTH_D, ";
        $query .= "     SEQ015.REMARK1 AS CHILD, ";
        //住所・電話番号
        $query .= "     ADDR.ZIPCD, ";
        $query .= "     ADDR.ADDRESS1, ";
        $query .= "     ADDR.ADDRESS2, ";
        $query .= "     ADDR.TELNO, ";
        //出身学校
        $query .= "     BASE.FS_CD, ";
        $query .= "     FIN.FINSCHOOL_NAME AS FS_NAME, ";
        $query .= "     BASE.FS_GRDYEAR, ";
        //保護者情報
        $query .= "     ADDR.GNAME, ";
        $query .= "     ADDR.GKANA, ";
        $query .= "     ADDR.RELATIONSHIP, ";
        $query .= "     ADDR.GZIPCD, ";
        $query .= "     ADDR.GADDRESS1, ";
        $query .= "     ADDR.GADDRESS2, ";
        $query .= "     ADDR.GTELNO, ";
        //備考１、２
        $query .= "     BASE.REMARK1, ";
        $query .= "     BASE.REMARK2, ";
        //塾
        $query .= "     SEQ008.REMARK1 AS PRISCHOOLCD1, ";
        $query .= "     P_SCH1.PRISCHOOL_NAME AS PRISCHOOL_NAME1, ";
        $query .= "     SEQ008.REMARK3 AS PRISCHOOL_CLASS_CD1, ";
        $query .= "     P_CLS1.PRISCHOOL_NAME AS PRISCHOOL_CLASS_NAME1, ";
        $query .= "     SEQ008.REMARK4 AS PRISCHOOLCD2, ";
        $query .= "     P_SCH2.PRISCHOOL_NAME AS PRISCHOOL_NAME2, ";
        $query .= "     SEQ008.REMARK5 AS PRISCHOOL_CLASS_CD2, ";
        $query .= "     P_CLS2.PRISCHOOL_NAME AS PRISCHOOL_CLASS_NAME2, ";
        $query .= "     '".$model->lastColumn."' AS ".$model->lastColumn." ";
        $query .= " FROM ";
        $query .= "     V_ENTEXAM_APPLICANTBASE_DAT BASE ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTADDR_DAT ADDR ";
        $query .= "          ON ADDR.ENTEXAMYEAR  = BASE.ENTEXAMYEAR ";
        $query .= "         AND ADDR.APPLICANTDIV = BASE.APPLICANTDIV ";
        $query .= "         AND ADDR.EXAMNO       = BASE.EXAMNO ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT SEQ015 ";
        $query .= "          ON SEQ015.ENTEXAMYEAR  = BASE.ENTEXAMYEAR ";
        $query .= "         AND SEQ015.APPLICANTDIV = BASE.APPLICANTDIV ";
        $query .= "         AND SEQ015.EXAMNO       = BASE.EXAMNO ";
        $query .= "         AND SEQ015.SEQ          = '015' ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT SEQ019 ";
        $query .= "          ON SEQ019.ENTEXAMYEAR  = BASE.ENTEXAMYEAR ";
        $query .= "         AND SEQ019.APPLICANTDIV = BASE.APPLICANTDIV ";
        $query .= "         AND SEQ019.EXAMNO       = BASE.EXAMNO ";
        $query .= "         AND SEQ019.SEQ          = '019' ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT SEQ008 ";
        $query .= "          ON SEQ008.ENTEXAMYEAR  = BASE.ENTEXAMYEAR ";
        $query .= "         AND SEQ008.APPLICANTDIV = BASE.APPLICANTDIV ";
        $query .= "         AND SEQ008.EXAMNO       = BASE.EXAMNO ";
        $query .= "         AND SEQ008.SEQ          = '008' ";
        $query .= "     LEFT JOIN FINSCHOOL_MST FIN ON BASE.FS_CD = FIN.FINSCHOOLCD ";
        $query .= "     LEFT JOIN PRISCHOOL_MST P_SCH1 ON SEQ008.REMARK1 = P_SCH1.PRISCHOOLCD ";
        $query .= "     LEFT JOIN PRISCHOOL_MST P_SCH2 ON SEQ008.REMARK4 = P_SCH2.PRISCHOOLCD ";
        $query .= "     LEFT JOIN PRISCHOOL_CLASS_MST P_CLS1 ON SEQ008.REMARK1 = P_CLS1.PRISCHOOLCD ";
        $query .= "                                         AND SEQ008.REMARK3 = P_CLS1.PRISCHOOL_CLASS_CD ";
        $query .= "     LEFT JOIN PRISCHOOL_CLASS_MST P_CLS2 ON SEQ008.REMARK4 = P_CLS2.PRISCHOOLCD ";
        $query .= "                                         AND SEQ008.REMARK5 = P_CLS2.PRISCHOOL_CLASS_CD ";
        $query .= " WHERE ";
        $query .= "         BASE.ENTEXAMYEAR  = '".$model->ObjYear."' ";
        $query .= "     AND BASE.APPLICANTDIV = '".$model->applicantdiv."' ";
        $query .= "     AND BASE.TESTDIV      = '".$model->field["TESTDIV"]."' ";
        $query .= " ORDER BY ";
        $query .= "     BASE.EXAMNO ";

        return $query;
    }

    //CSVデータ出力（調査書）
    public function selectMainQuery2($model, $class_array)
    {
        $query  = " SELECT ";
        $query .= "     BASE.ENTEXAMYEAR, ";
        $query .= "     BASE.TESTDIV, ";
        $query .= "     '".$model->field["DATADIV"]."' AS DATADIV, ";
        $query .= "     BASE.EXAMNO, ";
        for ($grade = 1; $grade <= 3; $grade++) {
            foreach ($class_array as $key => $label) {
                $num = (int)$key;
                if ($grade == "3") {
                    $query .= "     CONF.CONFIDENTIAL_RPT{$key} AS CONFRPT{$key}_{$grade}, ";
                } else {
                    $query .= "     D{$grade}.REMARK{$num} AS CONFRPT{$key}_{$grade}, ";
                }
            }
            if ($grade == "3") {
                $query .= "     CONF.TOTAL5 AS TOTAL5_{$grade}, ";
                $query .= "     CONF.TOTAL_ALL AS TOTALALL_{$grade}, ";
            } else {
                $query .= "     D{$grade}.REMARK10 AS TOTAL5_{$grade}, ";
                $query .= "     D{$grade}.REMARK11 AS TOTALALL_{$grade}, ";
            }
        }
        $query .= "     D3.REMARK1, ";//行動の記録
        $query .= "     D3.REMARK2, ";//英検
        $query .= "     D3.REMARK3, ";//数検
        $query .= "     D3.REMARK4, ";//漢検
        $query .= "     D3.REMARK5, ";//特別活動の記録
        $query .= "     CONF.ABSENCE_DAYS AS ABSENCE_DAYS1, ";
        $query .= "     CONF.ABSENCE_DAYS2, ";
        $query .= "     CONF.ABSENCE_DAYS3, ";
        $query .= "     '".$model->lastColumn."' AS ".$model->lastColumn." ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DAT BASE ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTCONFRPT_DAT CONF ";
        $query .= "          ON BASE.ENTEXAMYEAR  = CONF.ENTEXAMYEAR ";
        $query .= "         AND BASE.APPLICANTDIV = CONF.APPLICANTDIV ";
        $query .= "         AND BASE.EXAMNO       = CONF.EXAMNO ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTCONFRPT_DETAIL_DAT D1 ";
        $query .= "          ON BASE.ENTEXAMYEAR  = D1.ENTEXAMYEAR ";
        $query .= "         AND BASE.APPLICANTDIV = D1.APPLICANTDIV ";
        $query .= "         AND BASE.EXAMNO       = D1.EXAMNO ";
        $query .= "         AND D1.SEQ          = '001' ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTCONFRPT_DETAIL_DAT D2 ";
        $query .= "          ON BASE.ENTEXAMYEAR  = D2.ENTEXAMYEAR ";
        $query .= "         AND BASE.APPLICANTDIV = D2.APPLICANTDIV ";
        $query .= "         AND BASE.EXAMNO       = D2.EXAMNO ";
        $query .= "         AND D2.SEQ          = '002' ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTCONFRPT_DETAIL_DAT D3 ";
        $query .= "          ON BASE.ENTEXAMYEAR  = D3.ENTEXAMYEAR ";
        $query .= "         AND BASE.APPLICANTDIV = D3.APPLICANTDIV ";
        $query .= "         AND BASE.EXAMNO       = D3.EXAMNO ";
        $query .= "         AND D3.SEQ          = '003' ";
        $query .= " WHERE ";
        $query .= "         BASE.ENTEXAMYEAR  = '".$model->ObjYear."' ";
        $query .= "     AND BASE.APPLICANTDIV = '".$model->applicantdiv."' ";
        $query .= "     AND BASE.TESTDIV      = '".$model->field["TESTDIV"]."' ";
        $query .= " ORDER BY ";
        $query .= "      BASE.EXAMNO ";

        return $query;
    }
}
