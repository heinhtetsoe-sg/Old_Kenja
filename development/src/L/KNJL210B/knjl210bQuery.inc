<?php

require_once('for_php7.php');

class knjl210bquery extends Query {

    //名称マスタより取得
    function getNameMst($year, $namecd1)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL, ";
        $query .= "     NAMECD2 AS VALUE,";
        $query .= "     NAMESPARE2 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$year."' AND ";
        $query .= "     NAMECD1 = '".$namecd1."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //志願者詳細データ(SEQ='004')確認
    function getDetailDat($year, $examno)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DETAIL_DAT ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR  = '".$year."' ";
        $query .= " AND EXAMNO  = '".$examno."' ";
        $query .= " AND SEQ  = '004'";

        return $query;
    }

    //取込(新規)
    function InsertQuery($model,&$data_arr)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $cnt = 0;   //処理件数
        $count = get_count($data_arr);
        //願書データ
        if ($model->field["DATADIV"] === '1') {
            for ($i = 0; $i < $count; $i++) {
                //志願者基礎データ
                $data = array();
                $data["ENTEXAMYEAR"][TEXT]    = $data_arr[$i]["ENTEXAMYEAR"];
                $data["APPLICANTDIV"][TEXT]   = $data_arr[$i]["APPLICANTDIV"];
                $data["TESTDIV"][TEXT]        = $data_arr[$i]["TESTDIV"];
                $data["SHDIV"][TEXT]          = "1";
                $data["DESIREDIV"][TEXT]      = "1";
                $data["TESTDIV1"][TEXT]       = $data_arr[$i]["TESTDIV1"];
                $data["EXAMNO"][TEXT]         = $data_arr[$i]["EXAMNO"];
                $data["NAME"][TEXT]           = $data_arr[$i]["NAME"];
                $data["NAME_KANA"][TEXT]      = $data_arr[$i]["NAME_KANA"];
                $data["BIRTHDAY"][DATE]       = $data_arr[$i]["BIRTHDAY"];
                $data["SEX"][TEXT]            = $data_arr[$i]["SEX"];
                $data["FS_CD"][TEXT]          = $data_arr[$i]["FS_CD"];
                $data["FS_DAY"][DATE]         = $data_arr[$i]["FS_DAY"];
                $data["FS_GRDDIV"][TEXT]      = $data_arr[$i]["FS_GRDDIV"];
                $data["RECEPTDATE"][DATE]     = $data_arr[$i]["RECEPTDATE"];
                //希望コース A(1):推薦Ａ B(2):推薦Ｂ T(3):特進選抜希望
                //半角英字１文字で入力（A、B、T　大文字）。空欄も可。
                //テーブルには、1、2、3　に変換して登録
                if (strlen($data_arr[$i]["SELECT_SUBCLASS_DIV"])) {
                    $data["SELECT_SUBCLASS_DIV"][TEXT]  = $db->getOne(knjl210bQuery::dataConvertNameMstL033Sql($model, $data_arr[$i]["SELECT_SUBCLASS_DIV"]));
                } else {
                    $data["SELECT_SUBCLASS_DIV"][TEXT]  = "";
                }
                $data["REGISTERCD"][TEXT]     = STAFFCD;
                $data["UPDATED"][FUNC]        = "SYSDATE()";

                $query = Query::insertSQL($data, "ENTEXAM_APPLICANTBASE_DAT");
                $db->query($query);
                
                //志願者詳細データ(SEQ='004')
                //データ確認
                $detail_data = "";
                $detail_data = $db->getOne(knjl210bQuery::getDetailDat($data_arr[$i]["ENTEXAMYEAR"], $data_arr[$i]["EXAMNO"]));
                
                $data = array();
                if ($detail_data == 0) {
                    $data["ENTEXAMYEAR"][TEXT]    = $data_arr[$i]["ENTEXAMYEAR"];
                    $data["EXAMNO"][TEXT]         = $data_arr[$i]["EXAMNO"];
                    $data["SEQ"][TEXT]            = '004';
                }
                $data["REMARK3"][TEXT]        = $data_arr[$i]["REMARK3"];
                $data["REMARK4"][TEXT]        = $data_arr[$i]["REMARK4"];
                $data["REMARK8"][TEXT]        = $data_arr[$i]["REMARK8"];
                //$data["REMARK1"][TEXT]        = $data_arr[$i]["REMARK1"];
                $data["REMARK2"][TEXT]        = $data_arr[$i]["REMARK2"];
                $data["REGISTERCD"][TEXT]     = STAFFCD;
                $data["UPDATED"][FUNC]        = "SYSDATE()";
                if ($detail_data > 0) {
                    $where  = " WHERE ";
                    $where .= "     ENTEXAMYEAR  = '".$data_arr[$i]["ENTEXAMYEAR"]."' ";
                    $where .= " AND EXAMNO  = '".$data_arr[$i]["EXAMNO"]."' ";
                    $where .= " AND SEQ  = '004'";
                }
                if ($detail_data == 0) {
                    $query = Query::insertSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT");
                } else {
                    $query = Query::updateSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT", $where);
                }
                $db->query($query);
                
                //志願者住所データ
                $query  = " DELETE FROM ENTEXAM_APPLICANTADDR_DAT ";
                $query .= " WHERE ";
                $query .= "     ENTEXAMYEAR  = '".$data_arr[$i]["ENTEXAMYEAR"]."' ";
                $query .= " AND EXAMNO  = '".$data_arr[$i]["EXAMNO"]."' ";
                $db->query($query);
                
                $data = array();
                $data["ENTEXAMYEAR"][TEXT]    = $data_arr[$i]["ENTEXAMYEAR"];
                $data["EXAMNO"][TEXT]         = $data_arr[$i]["EXAMNO"];
                $data["ZIPCD"][TEXT]          = $data_arr[$i]["ZIPCD"];
                $data["ADDRESS1"][TEXT]       = $data_arr[$i]["ADDRESS1"];
                $data["ADDRESS2"][TEXT]       = $data_arr[$i]["ADDRESS2"];
                $data["TELNO"][TEXT]          = $data_arr[$i]["TELNO"];
                $data["GNAME"][TEXT]          = $data_arr[$i]["GNAME"];
                $data["GKANA"][TEXT]          = $data_arr[$i]["GKANA"];
                //保護者住所もあわせてコピー
                $data["GZIPCD"][TEXT]         = $data_arr[$i]["ZIPCD"];
                $data["GADDRESS1"][TEXT]      = $data_arr[$i]["ADDRESS1"];
                $data["GADDRESS2"][TEXT]      = $data_arr[$i]["ADDRESS2"];
                $data["GTELNO"][TEXT]         = $data_arr[$i]["TELNO"];
                $data["REGISTERCD"][TEXT]     = STAFFCD;
                $data["UPDATED"][FUNC]        = "SYSDATE()";

                $query = Query::insertSQL($data, "ENTEXAM_APPLICANTADDR_DAT");
                $db->query($query);

                $cnt++;
            }
        //調査書データ
        } else {
            for ($i = 0; $i < $count; $i++) {
                $data = array();
                $data["ENTEXAMYEAR"][TEXT]            = $data_arr[$i]["ENTEXAMYEAR"];
                $data["EXAMNO"][TEXT]                 = $data_arr[$i]["EXAMNO"];
                $data["CONFIDENTIAL_RPT01"][NUMBER]   = $data_arr[$i]["CONFIDENTIAL_RPT01"];
                $data["CONFIDENTIAL_RPT02"][NUMBER]   = $data_arr[$i]["CONFIDENTIAL_RPT02"];
                $data["CONFIDENTIAL_RPT03"][NUMBER]   = $data_arr[$i]["CONFIDENTIAL_RPT03"];
                $data["CONFIDENTIAL_RPT04"][NUMBER]   = $data_arr[$i]["CONFIDENTIAL_RPT04"];
                $data["CONFIDENTIAL_RPT05"][NUMBER]   = $data_arr[$i]["CONFIDENTIAL_RPT05"];
                $data["CONFIDENTIAL_RPT06"][NUMBER]   = $data_arr[$i]["CONFIDENTIAL_RPT06"];
                $data["CONFIDENTIAL_RPT07"][NUMBER]   = $data_arr[$i]["CONFIDENTIAL_RPT07"];
                $data["CONFIDENTIAL_RPT08"][NUMBER]   = $data_arr[$i]["CONFIDENTIAL_RPT08"];
                $data["CONFIDENTIAL_RPT09"][NUMBER]   = $data_arr[$i]["CONFIDENTIAL_RPT09"];
                $data["CONFIDENTIAL_RPT10"][NUMBER]   = $data_arr[$i]["CONFIDENTIAL_RPT10"];
                $data["CONFIDENTIAL_RPT11"][NUMBER]   = $data_arr[$i]["CONFIDENTIAL_RPT11"];
                $data["CONFIDENTIAL_RPT12"][NUMBER]   = $data_arr[$i]["CONFIDENTIAL_RPT12"];
                $data["AVERAGE_ALL"][NUMBER]          = $data_arr[$i]["AVERAGE_ALL"];
                $data["TOTAL_ALL"][NUMBER]            = $data_arr[$i]["TOTAL_ALL"];
                $data["ABSENCE_DAYS"][NUMBER]         = $data_arr[$i]["ABSENCE_DAYS"];
                $data["ABSENCE_DAYS2"][NUMBER]        = $data_arr[$i]["ABSENCE_DAYS2"];
                $data["ABSENCE_DAYS3"][NUMBER]        = $data_arr[$i]["ABSENCE_DAYS3"];
                $data["ABSENCE_REMARK"][TEXT]         = $data_arr[$i]["ABSENCE_REMARK"];
                $data["ABSENCE_REMARK2"][TEXT]        = $data_arr[$i]["ABSENCE_REMARK2"];
                $data["ABSENCE_REMARK3"][TEXT]        = $data_arr[$i]["ABSENCE_REMARK3"];
                $data["BASE_FLG"][TEXT]               = $data_arr[$i]["BASE_FLG"];
                $data["HEALTH_FLG"][TEXT]             = $data_arr[$i]["HEALTH_FLG"];
                $data["ACTIVE_FLG"][TEXT]             = $data_arr[$i]["ACTIVE_FLG"];
                $data["RESPONSIBLE_FLG"][TEXT]        = $data_arr[$i]["RESPONSIBLE_FLG"];
                $data["ORIGINAL_FLG"][TEXT]           = $data_arr[$i]["ORIGINAL_FLG"];
                $data["MIND_FLG"][TEXT]               = $data_arr[$i]["MIND_FLG"];
                $data["NATURE_FLG"][TEXT]             = $data_arr[$i]["NATURE_FLG"];
                $data["WORK_FLG"][TEXT]               = $data_arr[$i]["WORK_FLG"];
                $data["JUSTICE_FLG"][TEXT]            = $data_arr[$i]["JUSTICE_FLG"];
                $data["PUBLIC_FLG"][TEXT]             = $data_arr[$i]["PUBLIC_FLG"];
                $data["REMARK1"][TEXT]                = $data_arr[$i]["REMARK1"];
                $data["REGISTERCD"][TEXT]             = STAFFCD;
                $data["UPDATED"][FUNC]                = "SYSDATE()";
                
                $query = Query::insertSQL($data, "ENTEXAM_APPLICANTCONFRPT_DAT");
                $db->query($query);
                
                //クラブ名の更新、追加（志願者詳細データ(SEQ='004')）
                $detail_data = "";
                $detail_data = $db->getOne(knjl210bQuery::getDetailDat($data_arr[$i]["ENTEXAMYEAR"], $data_arr[$i]["EXAMNO"]));
                $data = array();
                if ($detail_data == 0) {
                    $data["ENTEXAMYEAR"][TEXT]    = $data_arr[$i]["ENTEXAMYEAR"];
                    $data["EXAMNO"][TEXT]         = $data_arr[$i]["EXAMNO"];
                    $data["SEQ"][TEXT]            = '004';
                }
                $data["REMARK1"][TEXT]        = $data_arr[$i]["REMARK_CLUB"];
                $data["REGISTERCD"][TEXT]     = STAFFCD;
                $data["UPDATED"][FUNC]        = "SYSDATE()";
                if ($detail_data > 0) {
                    $where  = " WHERE ";
                    $where .= "     ENTEXAMYEAR  = '".$data_arr[$i]["ENTEXAMYEAR"]."' ";
                    $where .= " AND EXAMNO  = '".$data_arr[$i]["EXAMNO"]."' ";
                    $where .= " AND SEQ  = '004'";
                }
                if ($detail_data == 0) {
                    $query = Query::insertSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT");
                } else {
                    $query = Query::updateSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT", $where);
                }
                $db->query($query);
                
                $cnt++;
            }
        }
        
        $db->commit();
        Query::dbCheckIn($db);

        return $cnt;
    }

    //取込(更新)
    function UpdateQuery($model,&$data_arr)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $cnt = 0;   //処理件数
        $count = get_count($data_arr);
        //願書データ
        if ($model->field["DATADIV"] === '1') {
            for ($i = 0; $i < $count; $i++) {
                //志願者基礎データ
                $data = array();
                $data["APPLICANTDIV"][TEXT]   = $data_arr[$i]["APPLICANTDIV"];
                $data["TESTDIV"][TEXT]        = $data_arr[$i]["TESTDIV"];
                $data["SHDIV"][TEXT]          = "1";
                $data["DESIREDIV"][TEXT]      = "1";
                $data["TESTDIV1"][TEXT]       = $data_arr[$i]["TESTDIV1"];
                $data["NAME"][TEXT]           = $data_arr[$i]["NAME"];
                $data["NAME_KANA"][TEXT]      = $data_arr[$i]["NAME_KANA"];
                $data["BIRTHDAY"][DATE]       = $data_arr[$i]["BIRTHDAY"];
                $data["SEX"][TEXT]            = $data_arr[$i]["SEX"];
                $data["FS_CD"][TEXT]          = $data_arr[$i]["FS_CD"];
                $data["FS_DAY"][DATE]         = $data_arr[$i]["FS_DAY"];
                $data["FS_GRDDIV"][TEXT]      = $data_arr[$i]["FS_GRDDIV"];
                $data["RECEPTDATE"][DATE]     = $data_arr[$i]["RECEPTDATE"];
                //希望コース A(1):推薦Ａ B(2):推薦Ｂ T(3):特進選抜希望
                //半角英字１文字で入力（A、B、T　大文字）。空欄も可。
                //テーブルには、1、2、3　に変換して登録
                if (strlen($data_arr[$i]["SELECT_SUBCLASS_DIV"])) {
                    $data["SELECT_SUBCLASS_DIV"][TEXT]  = $db->getOne(knjl210bQuery::dataConvertNameMstL033Sql($model, $data_arr[$i]["SELECT_SUBCLASS_DIV"]));
                } else {
                    $data["SELECT_SUBCLASS_DIV"][TEXT]  = "";
                }
                $data["REGISTERCD"][TEXT]     = STAFFCD;
                $data["UPDATED"][FUNC]        = "SYSDATE()";
                
                $where  = " WHERE ENTEXAMYEAR  = '".$data_arr[$i]["ENTEXAMYEAR"]."' AND";
                $where .= "       EXAMNO = '".$data_arr[$i]["EXAMNO"]."'";
                $query = Query::updateSQL($data, "ENTEXAM_APPLICANTBASE_DAT", $where);
                $db->query($query);
                
                //志願者基礎詳細データ
                $data = array();
                $data["REMARK3"][TEXT]        = $data_arr[$i]["REMARK3"];
                $data["REMARK4"][TEXT]        = $data_arr[$i]["REMARK4"];
                $data["REMARK8"][TEXT]        = $data_arr[$i]["REMARK8"];
                //$data["REMARK1"][TEXT]        = $data_arr[$i]["REMARK1"];
                $data["REMARK2"][TEXT]        = $data_arr[$i]["REMARK2"];
                $data["REGISTERCD"][TEXT]     = STAFFCD;
                $data["UPDATED"][FUNC]        = "SYSDATE()";
                
                $where  = " WHERE ENTEXAMYEAR  = '".$data_arr[$i]["ENTEXAMYEAR"]."' AND";
                $where .= "       EXAMNO = '".$data_arr[$i]["EXAMNO"]."' AND ";
                $where .= "       SEQ = '004' ";
                $query = Query::updateSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT", $where);
                $db->query($query);
                
                //志願者住所データ
                $data = array();
                $data["ZIPCD"][TEXT]          = $data_arr[$i]["ZIPCD"];
                $data["ADDRESS1"][TEXT]       = $data_arr[$i]["ADDRESS1"];
                $data["ADDRESS2"][TEXT]       = $data_arr[$i]["ADDRESS2"];
                $data["TELNO"][TEXT]          = $data_arr[$i]["TELNO"];
                $data["GNAME"][TEXT]          = $data_arr[$i]["GNAME"];
                $data["GKANA"][TEXT]          = $data_arr[$i]["GKANA"];
                //保護者住所もあわせてコピー
                $data["GZIPCD"][TEXT]         = $data_arr[$i]["ZIPCD"];
                $data["GADDRESS1"][TEXT]      = $data_arr[$i]["ADDRESS1"];
                $data["GADDRESS2"][TEXT]      = $data_arr[$i]["ADDRESS2"];
                $data["GTELNO"][TEXT]         = $data_arr[$i]["TELNO"];
                $data["REGISTERCD"][TEXT]     = STAFFCD;
                $data["UPDATED"][FUNC]        = "SYSDATE()";
                
                $where  = " WHERE ENTEXAMYEAR  = '".$data_arr[$i]["ENTEXAMYEAR"]."' AND";
                $where .= "       EXAMNO = '".$data_arr[$i]["EXAMNO"]."'";
                $query = Query::updateSQL($data, "ENTEXAM_APPLICANTADDR_DAT", $where);
                $db->query($query);

                $cnt++;
            }
        //調査書データ
        } else {
            for ($i = 0; $i < $count; $i++) {
                $data = array();
                $data["CONFIDENTIAL_RPT01"][NUMBER]   = $data_arr[$i]["CONFIDENTIAL_RPT01"];
                $data["CONFIDENTIAL_RPT02"][NUMBER]   = $data_arr[$i]["CONFIDENTIAL_RPT02"];
                $data["CONFIDENTIAL_RPT03"][NUMBER]   = $data_arr[$i]["CONFIDENTIAL_RPT03"];
                $data["CONFIDENTIAL_RPT04"][NUMBER]   = $data_arr[$i]["CONFIDENTIAL_RPT04"];
                $data["CONFIDENTIAL_RPT05"][NUMBER]   = $data_arr[$i]["CONFIDENTIAL_RPT05"];
                $data["CONFIDENTIAL_RPT06"][NUMBER]   = $data_arr[$i]["CONFIDENTIAL_RPT06"];
                $data["CONFIDENTIAL_RPT07"][NUMBER]   = $data_arr[$i]["CONFIDENTIAL_RPT07"];
                $data["CONFIDENTIAL_RPT08"][NUMBER]   = $data_arr[$i]["CONFIDENTIAL_RPT08"];
                $data["CONFIDENTIAL_RPT09"][NUMBER]   = $data_arr[$i]["CONFIDENTIAL_RPT09"];
                $data["CONFIDENTIAL_RPT10"][NUMBER]   = $data_arr[$i]["CONFIDENTIAL_RPT10"];
                $data["CONFIDENTIAL_RPT11"][NUMBER]   = $data_arr[$i]["CONFIDENTIAL_RPT11"];
                $data["CONFIDENTIAL_RPT12"][NUMBER]   = $data_arr[$i]["CONFIDENTIAL_RPT12"];
                $data["AVERAGE_ALL"][NUMBER]          = $data_arr[$i]["AVERAGE_ALL"];
                $data["TOTAL_ALL"][NUMBER]            = $data_arr[$i]["TOTAL_ALL"];
                $data["ABSENCE_DAYS"][NUMBER]         = $data_arr[$i]["ABSENCE_DAYS"];
                $data["ABSENCE_DAYS2"][NUMBER]        = $data_arr[$i]["ABSENCE_DAYS2"];
                $data["ABSENCE_DAYS3"][NUMBER]        = $data_arr[$i]["ABSENCE_DAYS3"];
                $data["ABSENCE_REMARK"][TEXT]         = $data_arr[$i]["ABSENCE_REMARK"];
                $data["ABSENCE_REMARK2"][TEXT]        = $data_arr[$i]["ABSENCE_REMARK2"];
                $data["ABSENCE_REMARK3"][TEXT]        = $data_arr[$i]["ABSENCE_REMARK3"];
                $data["BASE_FLG"][TEXT]               = $data_arr[$i]["BASE_FLG"];
                $data["HEALTH_FLG"][TEXT]             = $data_arr[$i]["HEALTH_FLG"];
                $data["ACTIVE_FLG"][TEXT]             = $data_arr[$i]["ACTIVE_FLG"];
                $data["RESPONSIBLE_FLG"][TEXT]        = $data_arr[$i]["RESPONSIBLE_FLG"];
                $data["ORIGINAL_FLG"][TEXT]           = $data_arr[$i]["ORIGINAL_FLG"];
                $data["MIND_FLG"][TEXT]               = $data_arr[$i]["MIND_FLG"];
                $data["NATURE_FLG"][TEXT]             = $data_arr[$i]["NATURE_FLG"];
                $data["WORK_FLG"][TEXT]               = $data_arr[$i]["WORK_FLG"];
                $data["JUSTICE_FLG"][TEXT]            = $data_arr[$i]["JUSTICE_FLG"];
                $data["PUBLIC_FLG"][TEXT]             = $data_arr[$i]["PUBLIC_FLG"];
                $data["REMARK1"][TEXT]                = $data_arr[$i]["REMARK1"];
                $data["REGISTERCD"][TEXT]             = STAFFCD;
                $data["UPDATED"][FUNC]                = "SYSDATE()";
                
                $where  = " WHERE ENTEXAMYEAR  = '".$data_arr[$i]["ENTEXAMYEAR"]."' AND";
                $where .= "       EXAMNO = '".$data_arr[$i]["EXAMNO"]."'";
                $query = Query::updateSQL($data, "ENTEXAM_APPLICANTCONFRPT_DAT", $where);
                $db->query($query);
                
                //志願者基礎詳細データ(クラブ名)
                $data = array();
                $data["REMARK1"][TEXT]        = $data_arr[$i]["REMARK_CLUB"];
                $data["REGISTERCD"][TEXT]     = STAFFCD;
                $data["UPDATED"][FUNC]        = "SYSDATE()";
                
                $where  = " WHERE ENTEXAMYEAR  = '".$data_arr[$i]["ENTEXAMYEAR"]."' AND";
                $where .= "       EXAMNO = '".$data_arr[$i]["EXAMNO"]."' AND ";
                $where .= "       SEQ = '004' ";
                $query = Query::updateSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT", $where);
                $db->query($query);
                
                $cnt++;
            }
        }
        
        $db->commit();
        Query::dbCheckIn($db);

        return $cnt;
    }

    //削除
    function DeleteQuery($model,&$data_arr)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $cnt = 0;   //処理件数
        $count = get_count($data_arr);
        //願書データ
        if ($model->field["DATADIV"] === '1') {
            for ($i = 0; $i < $count; $i++) {
                //志願者基礎データ
                $query  = " DELETE FROM ENTEXAM_APPLICANTBASE_DAT ";
                $query .= " WHERE ";
                $query .= "     ENTEXAMYEAR  = '".$data_arr[$i]["ENTEXAMYEAR"]."' ";
                $query .= " AND EXAMNO  = '".$data_arr[$i]["EXAMNO"]."' ";
                $db->query($query);
                
                //志願者詳細データ
                /*$query  = " DELETE FROM ";
                $query .= "     ENTEXAM_APPLICANTBASE_DETAIL_DAT ";
                $query .= " WHERE ";
                $query .= "     ENTEXAMYEAR  = '".$data_arr[$i]["ENTEXAMYEAR"]."' ";
                $query .= " AND EXAMNO  = '".$data_arr[$i]["EXAMNO"]."' ";
                $query .= " AND SEQ  = '004'";
                $db->query($query);*/
                $data = array();
                $data["REMARK3"][TEXT]        = "";
                $data["REMARK4"][TEXT]        = "";
                $data["REMARK8"][TEXT]        = "";
                //$data["REMARK1"][TEXT]        = $data_arr[$i]["REMARK1"];
                $data["REMARK2"][TEXT]        = "";
                $data["REGISTERCD"][TEXT]     = STAFFCD;
                $data["UPDATED"][FUNC]        = "SYSDATE()";
                
                $where  = " WHERE ENTEXAMYEAR  = '".$data_arr[$i]["ENTEXAMYEAR"]."' AND";
                $where .= "       EXAMNO = '".$data_arr[$i]["EXAMNO"]."' AND ";
                $where .= "       SEQ = '004' ";
                $query = Query::updateSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT", $where);
                $db->query($query);
                
                //志願者住所データ
                $query  = " DELETE FROM ENTEXAM_APPLICANTADDR_DAT ";
                $query .= " WHERE ";
                $query .= "     ENTEXAMYEAR  = '".$data_arr[$i]["ENTEXAMYEAR"]."' ";
                $query .= " AND EXAMNO  = '".$data_arr[$i]["EXAMNO"]."' ";
                $db->query($query);
                
                $cnt++;
            }
        //調査書データ
        } else {
            for ($i = 0; $i < $count; $i++) {
                $query  = " DELETE FROM ENTEXAM_APPLICANTCONFRPT_DAT ";
                $query .= " WHERE ";
                $query .= "     ENTEXAMYEAR  = '".$data_arr[$i]["ENTEXAMYEAR"]."' ";
                $query .= " AND EXAMNO  = '".$data_arr[$i]["EXAMNO"]."' ";
                $db->query($query);
                
                //志願者基礎詳細データ(クラブ名)
                $data = array();
                $data["REMARK1"][TEXT]        = "";
                $data["REGISTERCD"][TEXT]     = STAFFCD;
                $data["UPDATED"][FUNC]        = "SYSDATE()";
                
                $where  = " WHERE ENTEXAMYEAR  = '".$data_arr[$i]["ENTEXAMYEAR"]."' AND";
                $where .= "       EXAMNO = '".$data_arr[$i]["EXAMNO"]."' AND ";
                $where .= "       SEQ = '004' ";
                $query = Query::updateSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT", $where);
                $db->query($query);
                
                $cnt++;
            }
        }
        $db->commit();
        Query::dbCheckIn($db);

        return $cnt;
    }
    
    //受験型存在チェックSQL
    function dataCntTestDiv1Sql($model, $testdiv1)
    {
        $query  = " SELECT  ";
        $query .= "     COUNT(*)  ";
        $query .= " FROM  ";
        $query .= "     V_NAME_MST  ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$model->ObjYear."' AND ";
        $query .= "     NAMECD1 = 'L005' AND ";
        $query .= "     NAMECD2 = '".$testdiv1."' ";
        
        return $query;
    }
    
    //中学校コード存在チェックSQL
    function dataCntSchoolSql($model, $fs_cd)
    {
        $query  = " SELECT  ";
        $query .= "     COUNT(*)  ";
        $query .= " FROM  ";
        $query .= "     FINSCHOOL_MST  ";
        $query .= " WHERE FINSCHOOLCD   = '".$fs_cd."' ";
                        
        return $query;
    }

    //希望コース存在チェックSQL A(1):推薦Ａ B(2):推薦Ｂ T(3):特進選抜希望
    function dataCntNameMstL033Sql($model, $select_subclass_div) {
        $query  = " SELECT  ";
        $query .= "     COUNT(*)  ";
        $query .= " FROM  ";
        $query .= "     V_NAME_MST  ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$model->ObjYear."' AND ";
        $query .= "     NAMECD1 = 'L033' AND ";
        $query .= "     ABBV2 = '".$select_subclass_div."' "; //A、B、T
        return $query;
    }

    //希望コース A(1):推薦Ａ B(2):推薦Ｂ T(3):特進選抜希望
    //半角英字１文字で入力（A、B、T　大文字）。空欄も可。
    //テーブルには、1、2、3　に変換して登録
    function dataConvertNameMstL033Sql($model, $select_subclass_div) {
        $query  = " SELECT  ";
        $query .= "     NAMECD2  "; //1、2、3
        $query .= " FROM  ";
        $query .= "     V_NAME_MST  ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$model->ObjYear."' AND ";
        $query .= "     NAMECD1 = 'L033' AND ";
        $query .= "     ABBV2 = '".$select_subclass_div."' "; //A、B、T
        return $query;
    }

    //重複チェック(願書データ)
    function dataCntGanshoSql($model, $year, $examno, $flg)
    {
        $query  = " SELECT  ";
        $query .= "     COUNT(*)  ";
        $query .= " FROM  ";
        $query .= "     ENTEXAM_APPLICANTBASE_DAT  ";
        $query .= " WHERE ENTEXAMYEAR   = '".$year."' ";
        $query .= "   AND EXAMNO  = '".$examno."' ";
        if ($flg) {
            $query .= "   AND APPLICANTDIV  = '".$model->field["APPLICANTDIV"]."' ";
            $query .= "   AND TESTDIV  = '".$model->field["TESTDIV"]."' ";
        }
                        
        return $query;
    }

    //重複チェック(調査書データ)
    function dataCntChosashoSql($model, $year, $examno)
    {
        $query  = " SELECT  ";
        $query .= "     COUNT(*)  ";
        $query .= " FROM  ";
        $query .= "     ENTEXAM_APPLICANTCONFRPT_DAT  ";
        $query .= " WHERE ENTEXAMYEAR   = '".$year."' ";
        $query .= "   AND EXAMNO        = '".$examno."' ";

        return $query;
    }

    //Err用CVS作成用
    function selectCsvErrQuery()
    {
        $query  = "SELECT MSGROW, MSGREMARK FROM W_CSVMSG_PRG_DAT WHERE PROGRAMID = 'KNJL210B'";

        return $query;
    }

    //エラーデータの削除
    function deleteQueryErr()
    {
        $query  = "DELETE FROM W_CSVMSG_PRG_DAT WHERE PROGRAMID = 'KNJL210B'";
        return $query;
    }

    //エラーＤＢへの追加
    function insertQueryErr(&$db, $record_no, $check_error)
    {
        $data1["PROGRAMID"][TEXT]       = "KNJL210B";
        $data1["MSGROW"][NUMBER]        = $record_no;
        $data1["MSGREMARK"][TEXT]       = $check_error;

        $query = Query::insertSQL($data1, "W_CSVMSG_PRG_DAT");

        $result = $db->query($query);

    }

}
?>
