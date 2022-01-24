<?php

require_once('for_php7.php');
class knjl014yquery extends Query
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
        $query .= "     YEAR    = '".$year."' AND ";
        $query .= "     NAMECD1 = '".$namecd1."' ";
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

    //追加（ＣＳＶデータより読込）
    public function updateQueryCsv($model, &$data_arr)
    {
        $data = array();
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //元号取得
        $result = $db->query(knjl014yQuery::getNameMstAll($model->ObjYear, "L007"));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $eracd[]    = $row["NAMECD2"];
            $defyear[]  = $row["NAMESPARE1"];
            $defsdate[] = $row["NAMESPARE2"];
            $defedate[] = $row["NAMESPARE3"];
        }
        $result->free();

        $cnt = 0;   //処理件数
        for ($i = 0; $i < get_count($data_arr); $i++) {
            //分解する日付一覧
            $date_array = array("BIRTHDAY" => "birth", "FS_DAY" => "fs");
            //部品名
            $parts = array("_eracd", "_y", "_m", "_d");

            foreach ($date_array as $dkey => $dval) {
                if (strlen($data_arr[$i][$dkey])) {
                    //日付を分解
                    list($y, $m, $d) = preg_split('/-/', $data_arr[$i][$dkey]);
                    //部品の変数名
                    list($e_id, $y_id, $m_id, $d_id) = array($dval."_eracd", $dval."_y", $dval."_m", $dval."_d");

                    $$e_id = $$y_id = $$m_id = $$d_id = "";
                    for ($ere_no=0; $ere_no < get_count($eracd); $ere_no++) {
                        if ($defsdate[$ere_no] <= $data_arr[$i][$dkey] && $data_arr[$i][$dkey] <= $defedate[$ere_no]) {
                            $$e_id = $eracd[$ere_no];
                            $$y_id = ((int)$y + 1) - ((int)$defyear[$ere_no]);
                            $$y_id = sprintf("%02d", $$y_id);
                            $$m_id = $m;
                            $$d_id = $d;
                            break;
                        }
                    }
                }
            }

            //出身学校名取得
            $fs_name = $db->getOne(knjl014yQuery::getFsName($data_arr));

            //データ件数取得
            $base_cnt   = $db->getOne(knjl014yQuery::cntEntexamApplicantbaseDat($model, $data_arr[$i]));
            $addr_cnt   = $db->getOne(knjl014yQuery::cntEntexamApplicantaddrDat($model, $data_arr[$i]));

            //更新 -- ENTEXAM_APPLICANTBASE_DAT
            $data = array();
            $data["ENTEXAMYEAR"][TEXT]          = $model->ObjYear;
            $data["APPLICANTDIV"][TEXT]         = $data_arr[$i]["APPLICANTDIV"];
            $data["EXAMNO"][TEXT]               = $data_arr[$i]["EXAMNO"];
            $data["TESTDIV"][TEXT]              = $data_arr[$i]["TESTDIV"];
            $data["SHDIV"][TEXT]                = $data_arr[$i]["SHDIV"];
            $data["DESIREDIV"][TEXT]            = $data_arr[$i]["DESIREDIV"];
            $data["NAME"][TEXT]                 = $data_arr[$i]["NAME"];
            $data["NAME_KANA"][TEXT]            = $data_arr[$i]["NAME_KANA"];
            $data["SEX"][TEXT]                  = $data_arr[$i]["SEX"];
            $data["ERACD"][TEXT]                = $birth_eracd;
            $data["BIRTH_Y"][TEXT]              = $birth_y;
            $data["BIRTH_M"][TEXT]              = $birth_m;
            $data["BIRTH_D"][TEXT]              = $birth_d;
            $data["BIRTHDAY"][TEXT]             = $data_arr[$i]["BIRTHDAY"];
            $data["FS_CD"][TEXT]                = $data_arr[$i]["FS_CD"];
            $data["FS_NAME"][TEXT]              = $fs_name;
            $data["FS_GRDYEAR"][TEXT]           = $data_arr[$i]["FS_GRDYEAR"];
            $data["FS_ERACD"][TEXT]             = $fs_eracd;
            $data["FS_Y"][TEXT]                 = $fs_y;
            $data["FS_M"][TEXT]                 = $data_arr[$i]["FS_M"];
            $data["FS_DAY"][TEXT]               = $data_arr[$i]["FS_DAY"];
            $data["FS_GRDDIV"][TEXT]            = $data_arr[$i]["FS_GRDDIV"];
            if ($data_arr[$i]["APPLICANTDIV"] == '2') {
                $data["INTERVIEW_ATTEND_FLG"][TEXT] = $data_arr[$i]["INTERVIEW_ATTEND_FLG"];
                $data["SHIFT_DESIRE_FLG"][TEXT]     = $data_arr[$i]["SHIFT_DESIRE_FLG"];
                $data["SH_SCHOOLCD"][TEXT]          = $data_arr[$i]["SH_SCHOOLCD"];
                $data["SLIDE_FLG"][TEXT]            = $data_arr[$i]["SLIDE_FLG"];
                $data["GENERAL_FLG"][TEXT]          = $data_arr[$i]["GENERAL_FLG"];
                $data["SPORTS_FLG"][TEXT]           = $data_arr[$i]["SPORTS_FLG"];
            } else {
                $data["RECOM_ITEM1"][TEXT]          = $data_arr[$i]["RECOM_ITEM1"];
                $data["RECOM_ITEM2"][TEXT]          = $data_arr[$i]["RECOM_ITEM2"];
                $data["RECOM_ITEM3"][TEXT]          = $data_arr[$i]["RECOM_ITEM3"];
                $data["RECOM_ITEM4"][TEXT]          = $data_arr[$i]["RECOM_ITEM4"];
            }
            $data["DORMITORY_FLG"][TEXT]        = $data_arr[$i]["DORMITORY_FLG"];
            $data["REMARK1"][TEXT]              = $data_arr[$i]["REMARK1"];
            $data["REMARK2"][TEXT]              = $data_arr[$i]["REMARK2"];
            $data["REGISTERCD"][TEXT]           = STAFFCD;
            $data["UPDATED"][NUMBER]            = "SYSDATE()";

            $where  = " WHERE ENTEXAMYEAR   = '{$model->ObjYear}' AND ";
            $where .= "       APPLICANTDIV  = '{$data_arr[$i]["APPLICANTDIV"]}' AND ";
            $where .= "       EXAMNO        = '{$data_arr[$i]["EXAMNO"]}' ";

            if (0 == $base_cnt) {
                $query = Query::insertSQL($data, "ENTEXAM_APPLICANTBASE_DAT");
            } else {
                $query = Query::updateSQL($data, "ENTEXAM_APPLICANTBASE_DAT", $where);
            }
            $db->query($query);

            //更新 -- ENTEXAM_APPLICANTADDR_DAT
            $data = array();
            $data["ENTEXAMYEAR"][TEXT]      = $model->ObjYear;
            $data["EXAMNO"][TEXT]           = $data_arr[$i]["EXAMNO"];
            $data["ZIPCD"][TEXT]            = $data_arr[$i]["ZIPCD"];
            $data["ADDRESS1"][TEXT]         = $data_arr[$i]["ADDRESS1"];
            $data["ADDRESS2"][TEXT]         = $data_arr[$i]["ADDRESS2"];
            $data["TELNO"][TEXT]            = $data_arr[$i]["TELNO"];
            $data["GKANA"][TEXT]            = $data_arr[$i]["GKANA"];
            $data["GNAME"][TEXT]            = $data_arr[$i]["GNAME"];
            $data["GZIPCD"][TEXT]           = $data_arr[$i]["GZIPCD"];
            $data["GADDRESS1"][TEXT]        = $data_arr[$i]["GADDRESS1"];
            $data["GADDRESS2"][TEXT]        = $data_arr[$i]["GADDRESS2"];
            $data["GTELNO"][TEXT]           = $data_arr[$i]["GTELNO"];
            $data["RELATIONSHIP"][TEXT]    = $data_arr[$i]["RELATIONSHIP"];
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][NUMBER]        = "SYSDATE()";

            $where  = " WHERE ENTEXAMYEAR   = '{$model->ObjYear}' AND";
            $where .= "       EXAMNO        = '{$data_arr[$i]["EXAMNO"]}' ";

            if (0 == $addr_cnt) {
                $query = Query::insertSQL($data, "ENTEXAM_APPLICANTADDR_DAT");
            } else {
                $query = Query::updateSQL($data, "ENTEXAM_APPLICANTADDR_DAT", $where);
            }
            $db->query($query);

            //削除 -- ENTEXAM_APPLICANTDESIRE_DAT
            $query  = " DELETE FROM ";
            $query .= "     ENTEXAM_APPLICANTDESIRE_DAT ";
            $query .= " WHERE ";
            $query .= "     ENTEXAMYEAR     = '{$model->ObjYear}' AND ";
            $query .= "     APPLICANTDIV    = '{$data_arr[$i]["APPLICANTDIV"]}' AND ";
            $query .= "     EXAMNO          = '{$data_arr[$i]["EXAMNO"]}' ";
            $db->query($query);

            //更新 -- ENTEXAM_APPLICANTDESIRE_DAT
            $data = array();
            $data["ENTEXAMYEAR"][TEXT]      = $model->ObjYear;
            $data["APPLICANTDIV"][TEXT]     = $data_arr[$i]["APPLICANTDIV"];
            $data["TESTDIV"][TEXT]          = $data_arr[$i]["TESTDIV"];
            $data["EXAMNO"][TEXT]           = $data_arr[$i]["EXAMNO"];
            $data["DESIREDIV"][TEXT]        = $data_arr[$i]["DESIREDIV"];
            $data["RECOM_KIND"][TEXT]       = $data_arr[$i]["RECOM_KIND"];
            $data["SHDIV"][TEXT]            = $data_arr[$i]["SHDIV"];
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][NUMBER]        = "SYSDATE()";

            $query = Query::insertSQL($data, "ENTEXAM_APPLICANTDESIRE_DAT");
            $db->query($query);

            if ($data_arr[$i]["TESTDIV"] != '3' && $data_arr[$i]["GENERAL_FLG"] == '1') {
                //更新 -- ENTEXAM_APPLICANTDESIRE_DAT
                $data = array();
                $data["ENTEXAMYEAR"][TEXT]      = $model->ObjYear;
                $data["APPLICANTDIV"][TEXT]     = $data_arr[$i]["APPLICANTDIV"];
                $data["TESTDIV"][TEXT]          = '3';
                $data["EXAMNO"][TEXT]           = $data_arr[$i]["EXAMNO"];
                $data["DESIREDIV"][TEXT]        = $data_arr[$i]["GENERAL_DESIREDIV"];
                $data["SHDIV"][TEXT]            = $data_arr[$i]["GENERAL_SHDIV"];
                $data["REGISTERCD"][TEXT]       = STAFFCD;
                $data["UPDATED"][NUMBER]        = "SYSDATE()";

                $query = Query::insertSQL($data, "ENTEXAM_APPLICANTDESIRE_DAT");
                $db->query($query);
            }

            if ($data_arr[$i]["APPLICANTDIV"] == '2') {
                //更新 -- ENTEXAM_APPLICANTBASE_DETAIL_DAT
                //データ件数取得
                $det_cnt = $db->getOne(knjl014yQuery::getCountDetailDat($model, $data_arr[$i], "005"));
                $data = array();
                $data["REMARK1"][TEXT]     = $data_arr[$i]["SPORTS_FLG2"];
                
                if (0 == $addr_cnt) {
                    $data["ENTEXAMYEAR"][TEXT] = $model->ObjYear;
                    $data["EXAMNO"][TEXT]      = $data_arr[$i]["EXAMNO"];
                    $data["SEQ"][TEXT]         = '005';
                    $query = Query::insertSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT");
                } else {
                    $where  = " WHERE ENTEXAMYEAR   = '{$model->ObjYear}' ";
                    $where .= "       AND EXAMNO    = '{$data_arr[$i]["EXAMNO"]}' ";
                    $where .= "       AND SEQ       = '005' ";
                    $query = Query::updateSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT", $where);
                }

                $db->query($query);
            }
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

    //出身学校コードチェック
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

    //志望区分チェック
    public function desiredivCntSql($data, $desirediv)
    {
        $query  = " SELECT  ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_WISHDIV_MST T1 ";
        $query .= "     INNER JOIN ENTEXAM_COURSE_MST L1 ";
        $query .= "          ON L1.ENTEXAMYEAR  = T1.ENTEXAMYEAR ";
        $query .= "         AND L1.APPLICANTDIV = T1.APPLICANTDIV ";
        $query .= "         AND L1.TESTDIV      = T1.TESTDIV ";
        $query .= "         AND L1.COURSECD     = T1.COURSECD ";
        $query .= "         AND L1.MAJORCD      = T1.MAJORCD ";
        $query .= "         AND L1.EXAMCOURSECD = T1.EXAMCOURSECD ";
        $query .= " WHERE ";
        $query .= "     T1.ENTEXAMYEAR  = '".$data["ENTEXAMYEAR"]."' AND ";
        $query .= "     T1.APPLICANTDIV = '".$data["APPLICANTDIV"]."' AND ";
        $query .= "     T1.TESTDIV      = '".$data["TESTDIV"]."' AND ";
        $query .= "     T1.DESIREDIV    = '".$desirediv."' AND ";
        $query .= "     T1.WISHNO       = '1' ";

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
        $query .= "     APPLICANTDIV    = '{$data_arr["APPLICANTDIV"]}' AND ";
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
        $query .= "     EXAMNO          = '{$data_arr["EXAMNO"]}' ";

        return $query;
    }

    //件数取得 -- ENTEXAM_APPLICANTBASE_DETAIL_DAT
    public function getCountDetailDat($model, $data_arr, $seq)
    {
        $query  = " SELECT COUNT(*) FROM ENTEXAM_APPLICANTBASE_DETAIL_DAT ";
        $query .= " WHERE ENTEXAMYEAR = '{$model->ObjYear}' AND EXAMNO = '{$data_arr["EXAMNO"]}' AND SEQ = '{$seq}' ";
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

    /************************/
    /**  ＣＳＶ出力で使用  **/
    /************************/

    //ヘッダ例用データを名称マスタより取得
    public function getHeaderReiNameMst($year, $namecd1, $name)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     {$name} AS LABEL, ";
        $query .= "     NAMECD2 AS VALUE ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR    = '".$year."' AND ";
        $query .= "     NAMECD1 = '".$namecd1."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //ヘッダ例用データを志望区分より取得
    public function getHeaderReiDesireDiv($model, $testdiv)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     T1.DESIREDIV || ':' || T2.EXAMCOURSE_NAME AS LABEL, ";
        $query .= "     T1.DESIREDIV AS VALUE ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_WISHDIV_MST T1 ";
        $query .= "     INNER JOIN ENTEXAM_COURSE_MST T2 ";
        $query .= "          ON T2.ENTEXAMYEAR  = T1.ENTEXAMYEAR ";
        $query .= "         AND T2.APPLICANTDIV = T1.APPLICANTDIV ";
        $query .= "         AND T2.TESTDIV      = T1.TESTDIV ";
        $query .= "         AND T2.COURSECD     = T1.COURSECD ";
        $query .= "         AND T2.MAJORCD      = T1.MAJORCD ";
        $query .= "         AND T2.EXAMCOURSECD = T1.EXAMCOURSECD ";
        $query .= " WHERE ";
        $query .= "     T1.ENTEXAMYEAR  = '".$model->ObjYear."' AND ";
        $query .= "     T1.APPLICANTDIV = '".$model->field["APPLICANTDIV"]."' AND ";
        $query .= "     T1.TESTDIV      = '".$testdiv."' AND ";
        $query .= "     T1.WISHNO       = '1' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //CSVデータ出力
    public function selectMainQuery($model)
    {
        $query  = " WITH T_DESIREDIV AS ( ";
        $query .= "     SELECT  ";
        $query .= "         T1.*, ";
        $query .= "         L1.EXAMCOURSE_NAME AS DESIREDIVNAME ";
        $query .= "     FROM ";
        $query .= "         ENTEXAM_WISHDIV_MST T1 ";
        $query .= "         INNER JOIN ENTEXAM_COURSE_MST L1 ";
        $query .= "              ON L1.ENTEXAMYEAR  = T1.ENTEXAMYEAR ";
        $query .= "             AND L1.APPLICANTDIV = T1.APPLICANTDIV ";
        $query .= "             AND L1.TESTDIV      = T1.TESTDIV ";
        $query .= "             AND L1.COURSECD     = T1.COURSECD ";
        $query .= "             AND L1.MAJORCD      = T1.MAJORCD ";
        $query .= "             AND L1.EXAMCOURSECD = T1.EXAMCOURSECD ";
        $query .= "     WHERE ";
        $query .= "         T1.WISHNO = '1' ";
        $query .= " ) ";

        $query .= " SELECT DISTINCT ";
        $query .= "     T1.ENTEXAMYEAR, ";
        $query .= "     T1.APPLICANTDIV, ";
        $query .= "     N1.NAME1 AS APPLICANTDIVNAME, ";
        $query .= "     T1.TESTDIV, ";
        $query .= "     N2.NAME1 AS TESTDIVNAME, ";
        //帰国生
        $query .= "     T1.INTERVIEW_ATTEND_FLG, ";
        //志望区分
        $query .= "     T1.DESIREDIV, ";
        $query .= "     D1.DESIREDIVNAME, ";
        //推薦区分
        $query .= "     L2.RECOM_KIND, ";
        $query .= "     N3.NAME1 AS RECOM_KINDNAME, ";
        //専併区分
        $query .= "     T1.SHDIV, ";
        $query .= "     N4.NAME1 AS SHDIVNAME, ";
        //併願校
        $query .= "     T1.SH_SCHOOLCD, ";
        $query .= "     F2.FINSCHOOL_NAME AS SH_SCHOOLNAME, ";
        //推薦事項
        $query .= "     T1.RECOM_ITEM1, ";
        $query .= "     T1.RECOM_ITEM2, ";
        $query .= "     T1.RECOM_ITEM3, ";
        $query .= "     T1.RECOM_ITEM4, ";
        //志願者情報
        $query .= "     T1.EXAMNO, ";
        $query .= "     T1.NAME, ";
        $query .= "     T1.NAME_KANA, ";
        $query .= "     T1.SEX, ";
        $query .= "     N5.NAME1 AS SEXNAME, ";
        $query .= "     T1.BIRTHDAY, ";
        $query .= "     T1.FS_CD, ";
        $query .= "     F1.FINSCHOOL_NAME AS FS_NAME, ";
        $query .= "     T1.FS_GRDYEAR, ";
        $query .= "     T1.FS_M, ";
        $query .= "     T1.FS_GRDDIV, ";
        $query .= "     N6.NAME1 AS FS_GRDDIVNAME, ";
        $query .= "     L1.TELNO, ";
        $query .= "     L1.ZIPCD, ";
        $query .= "     L1.ADDRESS1, ";
        $query .= "     L1.ADDRESS2, ";
        //保護者情報
        $query .= "     L1.GNAME, ";
        $query .= "     L1.GKANA, ";
        $query .= "     L1.RELATIONSHIP, ";
        $query .= "     N7.NAME1 AS RELATIONSHIPNAME, ";
        $query .= "     L1.GTELNO, ";
        $query .= "     L1.GZIPCD, ";
        $query .= "     L1.GADDRESS1, ";
        $query .= "     L1.GADDRESS2, ";
        //希望
        $query .= "     T1.SHIFT_DESIRE_FLG, ";
        $query .= "     T1.SLIDE_FLG, ";
        $query .= "     T1.GENERAL_FLG, ";
        $query .= "     L3.DESIREDIV AS GENERAL_DESIREDIV, ";
        $query .= "     D2.DESIREDIVNAME AS GENERAL_DESIREDIVNAME, ";
        $query .= "     L3.SHDIV AS GENERAL_SHDIV, ";
        $query .= "     N8.NAME1 AS GENERAL_SHDIVNAME, ";
        $query .= "     T1.SPORTS_FLG, ";
        $query .= "     T1.DORMITORY_FLG, ";
        //備考
        $query .= "     T1.REMARK1, ";
        $query .= "     T1.REMARK2, ";
        $query .= "     L4.REMARK1 AS SPORTS_FLG2, ";
        $query .= "     '".$model->lastColumn."' AS ".$model->lastColumn." ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DAT T1 ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTADDR_DAT L1 ";
        $query .= "          ON T1.ENTEXAMYEAR  = L1.ENTEXAMYEAR ";
        $query .= "         AND T1.EXAMNO       = L1.EXAMNO ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTDESIRE_DAT L2 ";
        $query .= "          ON T1.ENTEXAMYEAR  = L2.ENTEXAMYEAR ";
        $query .= "         AND T1.APPLICANTDIV = L2.APPLICANTDIV ";
        $query .= "         AND T1.TESTDIV      = L2.TESTDIV ";
        $query .= "         AND T1.EXAMNO       = L2.EXAMNO ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTDESIRE_DAT L3 ";
        $query .= "          ON T1.ENTEXAMYEAR  = L3.ENTEXAMYEAR ";
        $query .= "         AND T1.APPLICANTDIV = L3.APPLICANTDIV ";
        $query .= "         AND L3.TESTDIV      = '3' ";
        $query .= "         AND T1.EXAMNO       = L3.EXAMNO ";
        $query .= "     LEFT JOIN T_DESIREDIV D1 ";
        $query .= "          ON T1.ENTEXAMYEAR  = D1.ENTEXAMYEAR ";
        $query .= "         AND T1.APPLICANTDIV = D1.APPLICANTDIV ";
        $query .= "         AND T1.TESTDIV      = D1.TESTDIV ";
        $query .= "         AND T1.DESIREDIV    = D1.DESIREDIV ";
        $query .= "     LEFT JOIN T_DESIREDIV D2 ";
        $query .= "          ON T1.ENTEXAMYEAR  = D2.ENTEXAMYEAR ";
        $query .= "         AND T1.APPLICANTDIV = D2.APPLICANTDIV ";
        $query .= "         AND T1.TESTDIV      = D2.TESTDIV ";
        $query .= "         AND L2.DESIREDIV    = D2.DESIREDIV ";
        $query .= "     LEFT JOIN FINSCHOOL_MST F1 ON T1.FS_CD = F1.FINSCHOOLCD ";
        $query .= "     LEFT JOIN FINSCHOOL_MST F2 ON T1.SH_SCHOOLCD = F2.FINSCHOOLCD ";
        $query .= "     LEFT JOIN NAME_MST N1 ON N1.NAMECD1 = 'L003' AND N1.NAMECD2 = T1.APPLICANTDIV ";
        $query .= "     LEFT JOIN NAME_MST N2 ON N2.NAMECD1 = (CASE T1.APPLICANTDIV WHEN '2' THEN 'L004' ELSE 'L024' END) AND N2.NAMECD2 = T1.TESTDIV ";
        $query .= "     LEFT JOIN NAME_MST N3 ON N3.NAMECD1 = 'L023' AND N3.NAMECD2 = L2.RECOM_KIND ";
        $query .= "     LEFT JOIN NAME_MST N4 ON N4.NAMECD1 = 'L006' AND N4.NAMECD2 = T1.SHDIV ";
        $query .= "     LEFT JOIN NAME_MST N5 ON N5.NAMECD1 = 'Z002' AND N5.NAMECD2 = T1.SEX ";
        $query .= "     LEFT JOIN NAME_MST N6 ON N6.NAMECD1 = 'L016' AND N6.NAMECD2 = T1.FS_GRDDIV ";
        $query .= "     LEFT JOIN NAME_MST N7 ON N7.NAMECD1 = 'H201' AND N7.NAMECD2 = L1.RELATIONSHIP ";
        $query .= "     LEFT JOIN NAME_MST N8 ON N8.NAMECD1 = 'L006' AND N8.NAMECD2 = L2.SHDIV ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT L4 ";
        $query .= "          ON L4.ENTEXAMYEAR  = L1.ENTEXAMYEAR ";
        $query .= "         AND L4.EXAMNO       = L1.EXAMNO ";
        $query .= "         AND L4.SEQ          = '005' ";
        $query .= " WHERE ";
        $query .= "     T1.ENTEXAMYEAR  = '".$model->ObjYear."' AND ";
        $query .= "     T1.APPLICANTDIV = '".$model->field["APPLICANTDIV"]."' AND ";
        $query .= "     T1.TESTDIV      = '".$model->field["TESTDIV"]."' ";
        $query .= " ORDER BY ";
        $query .= "     T1.ENTEXAMYEAR, ";
        $query .= "     T1.APPLICANTDIV, ";
        $query .= "     T1.TESTDIV, ";
        $query .= "     T1.EXAMNO ";

        return $query;
    }
}
