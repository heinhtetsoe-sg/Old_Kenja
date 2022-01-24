<?php

require_once('for_php7.php');
class knjl114dquery extends Query
{
    /******************/
    /**  画面で使用  **/
    /******************/
    //名称マスタより取得
    public function getNameMst($year, $namecd1, $namecd2 = "")
    {
        $query  = " SELECT ";
        $query .= "     NAME1, ";
        if ($namecd1 == "L004") {
            $query .= "     NAME1 AS LABEL, ";
        } else {
            $query .= "     NAMECD2 || ':' || NAME1 AS LABEL, ";
        }
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
        if ($namecd1 == "L004") {
            $query .= "     int(VALUE) ";
        } else {
            $query .= "     VALUE ";
        }

        return $query;
    }

    //MAX_TESTDIV取得
    public function getMaxTestDiv($year)
    {
        $query  = " SELECT ";
        $query .= "     max(TESTDIV) ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DAT ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR = '".$year."' ";
        $query .= " GROUP BY ";
        $query .= "     ENTEXAMYEAR ";

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
            $base_cnt       = $db->getOne(knjl114dQuery::cntEntexamApplicantbaseDat($model, $data_arr[$i]));
            $addr_cnt       = $db->getOne(knjl114dQuery::cntEntexamApplicantaddrDat($model, $data_arr[$i]));
            $baseD033_cnt   = $db->getOne(knjl114dQuery::cntEntexamApplicantbaseDetailDat($model, $data_arr[$i], "033"));

            //ENTEXAM_APPLICANTBASE_DAT
            $data = array();
            $data["ENTEXAMYEAR"][TEXT]          = $model->ObjYear;
            $data["APPLICANTDIV"][TEXT]         = $model->applicantdiv;
            $data["EXAMNO"][TEXT]               = $data_arr[$i]["EXAMNO"];
            $data["TESTDIV"][TEXT]              = $data_arr[$i]["TESTDIV"];
            $data["SHDIV"][TEXT]                = $model->shdiv;
            $data["DESIREDIV"][TEXT]            = $data_arr[$i]["DESIREDIV"];
            $data["NAME"][TEXT]                 = $data_arr[$i]["NAME"];
            $data["NAME_KANA"][TEXT]            = $data_arr[$i]["NAME_KANA"];
            $data["SEX"][TEXT]                  = $data_arr[$i]["SEX"];
            $data["ERACD"][TEXT]                = $data_arr[$i]["ERACD"];
            $data["BIRTH_Y"][TEXT]              = $data_arr[$i]["BIRTH_Y"];
            $data["BIRTH_M"][TEXT]              = $data_arr[$i]["BIRTH_M"];
            $data["BIRTH_D"][TEXT]              = $data_arr[$i]["BIRTH_D"];
            $data["FS_CD"][TEXT]                = $data_arr[$i]["FS_CD"];
            $data["FS_ERACD"][TEXT]             = $data_arr[$i]["FS_ERACD"];
            $data["FS_Y"][TEXT]                 = $data_arr[$i]["FS_Y"];
            $data["FS_M"][TEXT]                 = $data_arr[$i]["FS_M"];
            $data["FS_GRDDIV"][TEXT]            = $data_arr[$i]["FS_GRDDIV"];
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
            $data["GNAME"][TEXT]            = $data_arr[$i]["GNAME"];
            $data["GKANA"][TEXT]            = $data_arr[$i]["GKANA"];
            $data["RELATIONSHIP"][TEXT]     = $data_arr[$i]["RELATIONSHIP"];
            $data["GZIPCD"][TEXT]           = $data_arr[$i]["GZIPCD"];
            $data["GADDRESS1"][TEXT]        = $data_arr[$i]["GADDRESS1"];
            $data["GADDRESS2"][TEXT]        = $data_arr[$i]["GADDRESS2"];
            $data["GTELNO"][TEXT]           = $data_arr[$i]["GTELNO"];
            $data["GTELNO2"][TEXT]          = $data_arr[$i]["GTELNO2"];
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

            //ENTEXAM_APPLICANTBASE_DETAIL_DAT SEQ=033
            $data = array();
            $data["ENTEXAMYEAR"][TEXT]      = $model->ObjYear;
            $data["APPLICANTDIV"][TEXT]     = $model->applicantdiv;
            $data["EXAMNO"][TEXT]           = $data_arr[$i]["EXAMNO"];
            $data["SEQ"][TEXT]              = "033";
            $data["REMARK1"][TEXT]          = $data_arr[$i]["REMARK1_033"];
            $data["REMARK2"][TEXT]          = $data_arr[$i]["REMARK2_033"];
            $data["REMARK3"][TEXT]          = $data_arr[$i]["REMARK3_033"];
            $data["REMARK4"][TEXT]          = $data_arr[$i]["REMARK4_033"];
            $data["REMARK5"][TEXT]          = $data_arr[$i]["REMARK5_033"];
            $data["REMARK6"][TEXT]          = $data_arr[$i]["REMARK6_033"];
            $data["REMARK8"][TEXT]          = $data_arr[$i]["REMARK8_033"];
            $data["REMARK9"][TEXT]          = $data_arr[$i]["REMARK9_033"];
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][NUMBER]        = "SYSDATE()";

            $where  = " WHERE ENTEXAMYEAR   = '{$model->ObjYear}' ";
            $where .= "   AND APPLICANTDIV  = '{$model->applicantdiv}' ";
            $where .= "   AND EXAMNO        = '{$data_arr[$i]["EXAMNO"]}' ";
            $where .= "   AND SEQ           = '033' ";

            if (0 == $baseD033_cnt) {
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
        $query .= "     AND EXAMNO       = '".sprintf("%03d", $data_arr["EXAMNO"])."' ";

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
        $query .= "     BASE.DESIREDIV, ";
        $query .= "     BASE.SHDIV, ";
        $query .= "     BASE.TESTDIV, ";
        $query .= "     BASE.EXAMNO, ";
        //志願者情報
        $query .= "     BASE.NAME, ";
        $query .= "     BASE.NAME_KANA, ";
        $query .= "     BASE.SEX, ";
        $query .= "     BASE.ERACD, ";
        $query .= "     BASE.BIRTH_Y, ";
        $query .= "     BASE.BIRTH_M, ";
        $query .= "     BASE.BIRTH_D, ";
        $query .= "     BASE.FS_CD, ";
        $query .= "     L007.NAME1 AS FS_ERACD, ";
        $query .= "     BASE.FS_Y, ";
        $query .= "     BASE.FS_M, ";
        $query .= "     L016.NAME1 AS FS_GRDDIV, ";
        $query .= "     SEQ033.REMARK8 AS REMARK8_033, "; //転編入学照会日
        $query .= "     SEQ033.REMARK1 AS REMARK1_033, "; //転籍フラグ
        $query .= "     SEQ033.REMARK2 AS REMARK2_033, "; //出身校（高校）コード
        $query .= "     case ";
        $query .= "         when SEQ033.REMARK3 = '1' then '全日制' ";
        $query .= "         when SEQ033.REMARK3 = '2' then '定時制' ";
        $query .= "         when SEQ033.REMARK3 = '3' then '通信制' ";
        $query .= "         else '' ";
        $query .= "     end as REMARK3_033, ";            //出身校（高校）課程
        $query .= "     SEQ033.REMARK4 AS REMARK4_033, "; //出身校（高校）学科
        $query .= "     SEQ033.REMARK5 AS REMARK5_033, "; //出身校（高校）学年
        $query .= "     case ";
        $query .= "         when SEQ033.REMARK6 = '1' then '在学中' ";
        $query .= "         when SEQ033.REMARK6 = '2' then '退学' ";
        $query .= "         else '' ";
        $query .= "     end as REMARK6_033, ";            //出身校（高校）状況
        $query .= "     SEQ033.REMARK9 AS REMARK9_033, "; //調査書照会日
        $query .= "     ADDR.ZIPCD, ";
        $query .= "     ADDR.ADDRESS1, ";
        $query .= "     ADDR.ADDRESS2, ";
        $query .= "     ADDR.TELNO, ";
        //保護者情報
        $query .= "     ADDR.GNAME, ";
        $query .= "     ADDR.GKANA, ";
        $query .= "     ADDR.RELATIONSHIP, ";
        $query .= "     ADDR.GZIPCD, ";
        $query .= "     ADDR.GADDRESS1, ";
        $query .= "     ADDR.GADDRESS2, ";
        $query .= "     ADDR.GTELNO, ";
        $query .= "     ADDR.GTELNO2, ";
        $query .= "     '".$model->lastColumn."' AS ".$model->lastColumn." ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DAT BASE ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTADDR_DAT ADDR ";
        $query .= "          ON ADDR.ENTEXAMYEAR  = BASE.ENTEXAMYEAR ";
        $query .= "         AND ADDR.APPLICANTDIV = BASE.APPLICANTDIV ";
        $query .= "         AND ADDR.EXAMNO       = BASE.EXAMNO ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT SEQ033 ";
        $query .= "          ON SEQ033.ENTEXAMYEAR  = BASE.ENTEXAMYEAR ";
        $query .= "         AND SEQ033.APPLICANTDIV = BASE.APPLICANTDIV ";
        $query .= "         AND SEQ033.EXAMNO       = BASE.EXAMNO ";
        $query .= "         AND SEQ033.SEQ          = '033' ";
        $query .= "     LEFT JOIN FINSCHOOL_MST FIN ON BASE.FS_CD = FIN.FINSCHOOLCD ";
        $query .= "     LEFT JOIN NAME_MST L007 ON L007.NAMECD1 = 'L007' ";
        $query .= "                            AND L007.NAMECD2 = BASE.FS_ERACD ";
        $query .= "     LEFT JOIN NAME_MST L016 ON L016.NAMECD1 = 'L016' ";
        $query .= "                            AND L016.NAMECD2 = BASE.FS_GRDDIV ";
        $query .= " WHERE ";
        $query .= "         BASE.ENTEXAMYEAR  = '".$model->ObjYear."' ";
        $query .= "     AND BASE.APPLICANTDIV = '".$model->applicantdiv."' ";
        $query .= "     AND BASE.TESTDIV      = '".$model->field["TESTDIV"]."' ";
        $query .= " ORDER BY ";
        $query .= "     BASE.EXAMNO ";

        return $query;
    }
}
