<?php

require_once('for_php7.php');
class knjz090_2aQuery extends Query
{

    //一覧用データ取得
    public function selectQuery($model)
    {
        $query  = " SELECT ";
        $query .= "     T1.FINSCHOOLCD, ";
        $query .= "     T1.FINSCHOOL_TYPE || ':' || L2.NAME1 AS FINSCHOOL_TYPE, ";
        $query .= "     T1.FINSCHOOL_DISTCD || ':' || L3.NAME1 AS FINSCHOOL_DISTCD, ";
        $query .= "     T1.FINSCHOOL_DISTCD2 || ':' || L6.NAME1 AS FINSCHOOL_DISTCD2, ";
        $query .= "     T1.FINSCHOOL_DIV || ':' || L4.NAME1 AS FINSCHOOL_DIV, ";
        $query .= "     T1.FINSCHOOL_NAME, ";
        $query .= "     T1.FINSCHOOL_KANA, ";
        $query .= "     T1.FINSCHOOL_NAME_ABBV, ";
        $query .= "     T1.FINSCHOOL_KANA_ABBV, ";
        $query .= "     T1.PRINCNAME, ";
        $query .= "     T1.PRINCNAME_SHOW, ";
        $query .= "     T1.PRINCKANA, ";
        $query .= "     T1.FINSCHOOL_STAFFCD || ':' || S1.STAFFNAME AS FINSCHOOL_STAFFCD, ";
        $query .= "     T1.DISTRICTCD || ':' || L5.NAME1 AS DISTRICTCD, ";
        $query .= "     T1.FINSCHOOL_PREF_CD || ':' || P1.PREF_NAME AS FINSCHOOL_PREF_CD, ";
        $query .= "     T1.FINSCHOOL_CITY_CD || ':' || C1.CITY_NAME AS FINSCHOOL_CITY_CD, ";
        $query .= "     T1.FINSCHOOL_ZIPCD, ";
        $query .= "     T1.FINSCHOOL_ADDR1, ";
        $query .= "     T1.FINSCHOOL_ADDR2, ";
        $query .= "     T1.FINSCHOOL_TELNO, ";
        $query .= "     T1.FINSCHOOL_FAXNO, ";
        $query .= "     T1.EDBOARDCD || ':' || L1.EDBOARDNAME AS EDBOARDCD, ";
        $query .= "     T1.REGISTERCD, ";
        $query .= "     T1.UPDATED ";
        $query .= " FROM ";
        $query .= "     FINSCHOOL_MST T1 ";
        $query .= " LEFT JOIN EDBOARD_MST L1 ON L1.EDBOARDCD = T1.EDBOARDCD ";
        $query .= " LEFT JOIN NAME_MST L2 ON T1.FINSCHOOL_TYPE     = L2.NAMECD2 AND L2.NAMECD1 = 'L019' ";
        $query .= " LEFT JOIN NAME_MST L3 ON T1.FINSCHOOL_DISTCD   = L3.NAMECD2 AND L3.NAMECD1 = 'L001' ";
        $query .= " LEFT JOIN NAME_MST L4 ON T1.FINSCHOOL_DIV      = L4.NAMECD2 AND L4.NAMECD1 = 'L015' ";
        $query .= " LEFT JOIN NAME_MST L5 ON T1.DISTRICTCD         = L5.NAMECD2 AND L5.NAMECD1 = 'Z003' ";
        $query .= " LEFT JOIN NAME_MST L6 ON T1.FINSCHOOL_DISTCD2  = L6.NAMECD2 AND L6.NAMECD1 = 'Z015' ";
        $query .= " LEFT JOIN PREF_MST P1 ON T1.FINSCHOOL_PREF_CD  = P1.PREF_CD ";
        $query .= " LEFT JOIN CITY_MST C1 ON T1.FINSCHOOL_PREF_CD  = C1.PREF_CD AND T1.FINSCHOOL_CITY_CD  = C1.CITY_CD ";
        $query .= " LEFT JOIN STAFF_MST S1 ON T1.FINSCHOOL_STAFFCD  = S1.STAFFCD ";
        if ($model->selectFinschoolType != "99") {
            $query .= " WHERE ";
            $query .= "     T1.FINSCHOOL_TYPE = '".$model->selectFinschoolType."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     FINSCHOOLCD ";

        return $query ;
    }

    //１レコード取得
    public function getScoolData($cd, $model)
    {
        $query  = " SELECT ";
        $query .= "     F1.*, ";
        $query .= "     F2.REMARK1 AS TOUHAIKOU, ";
        if ($model->Properties["useLocationMst"] == '1') {
            $query .= "     F3.REMARK1 AS DISTRICT_NAME, ";
        }
        $query .= "     F4.REMARK1 AS INJI_FLG ";
        if ($model->Properties["useMiraicompass"] == '1') {
            $query .= "     ,M1.MIRAI_FS_CD ";
        }
        $query .= " FROM ";
        $query .= "     FINSCHOOL_MST F1 ";
        $query .= "     LEFT JOIN FINSCHOOL_DETAIL_MST F2 ON F1.FINSCHOOLCD   = F2.FINSCHOOLCD ";
        $query .= "                                      AND F2.FINSCHOOL_SEQ = '001' ";
        $query .= "     LEFT JOIN FINSCHOOL_DETAIL_MST F3 ON F1.FINSCHOOLCD   = F3.FINSCHOOLCD ";
        $query .= "                                      AND F3.FINSCHOOL_SEQ = '002' ";
        $query .= "     LEFT JOIN FINSCHOOL_DETAIL_MST F4 ON F1.FINSCHOOLCD   = F4.FINSCHOOLCD ";
        $query .= "                                      AND F4.FINSCHOOL_SEQ = '003' ";
        if ($model->Properties["useMiraicompass"] == '1') {
            $query .= "     LEFT JOIN ENTEXAM_MIRAI_FS_REP_DAT M1 ON F1.FINSCHOOLCD = M1.FS_CD ";
        }
        $query .= " WHERE ";
        $query .= "     F1.FINSCHOOLCD = '".$cd ."' ";

        return $query;
    }

    //名称取得
    public function getNameMst($namecd1)
    {
        $query  = " SELECT ";
        $query .= "     NAMECD2 || ' ' || NAME1 AS LABEL, ";
        $query .= "     NAMECD2 AS VALUE ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = '".$namecd1."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //都道府県コード取得
    public function getPrefMst()
    {
        $query  = " SELECT ";
        $query .= "     PREF_CD || ' ' || PREF_NAME AS LABEL, ";
        $query .= "     PREF_CD AS VALUE ";
        $query .= " FROM ";
        $query .= "     PREF_MST ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //市町村コード取得
    public function getCityMst($prefcd)
    {
        $query  = " SELECT ";
        $query .= "     CITY_CD || ' ' || CITY_NAME AS LABEL, ";
        $query .= "     CITY_CD AS VALUE ";
        $query .= " FROM ";
        $query .= "     CITY_MST ";
        $query .= " WHERE ";
        $query .= "     PREF_CD = '".$prefcd."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //教育委員会コード取得
    public function getEdboardMst()
    {
        $query  = " SELECT ";
        $query .= "     EDBOARDCD || ' ' || EDBOARDNAME AS LABEL, ";
        $query .= "     EDBOARDCD AS VALUE ";
        $query .= " FROM ";
        $query .= "     EDBOARD_MST ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //所在地データ取得
    public function getDistrictName()
    {
        $query  = " SELECT ";
        $query .= "     DISTRICTCD || ' ' || DISTRICT_NAME AS LABEL, ";
        $query .= "     DISTRICTCD AS VALUE ";
        $query .= " FROM ";
        $query .= "     FINSCHOOL_LOCATION_MST ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //職員データ取得
    public function getStaffMst()
    {
        $query  = " SELECT ";
        $query .= "     STAFFCD || ' ' || STAFFNAME AS LABEL, ";
        $query .= "     STAFFCD AS VALUE ";
        $query .= " FROM ";
        $query .= "     STAFF_MST ";
        $query .= " ORDER  BY ";
        $query .= "     STAFFCD ";

        return $query;
    }

    //CSV書出し
    public function getCsvData($model)
    {
        $query  = " SELECT ";
        $query .= "     F1.FINSCHOOLCD, ";
        $query .= "     F1.FINSCHOOL_TYPE, ";
        $query .= "     F1.FINSCHOOL_DISTCD, ";
        $query .= "     F1.FINSCHOOL_DISTCD2, ";
        $query .= "     F1.FINSCHOOL_DIV, ";
        $query .= "     F1.FINSCHOOL_NAME, ";
        $query .= "     F1.FINSCHOOL_KANA, ";
        $query .= "     F1.FINSCHOOL_NAME_ABBV, ";
        $query .= "     F1.FINSCHOOL_KANA_ABBV, ";
        $query .= "     F1.FINSCHOOL_STAFFCD, ";
        $query .= "     S1.STAFFNAME AS FINSCHOOL_STAFF_NAME, ";
        $query .= "     F1.PRINCNAME, ";
        $query .= "     F1.PRINCNAME_SHOW, ";
        $query .= "     F1.PRINCKANA, ";
        $query .= "     F1.DISTRICTCD, ";
        $query .= "     NMZ003.NAME1 AS FINSCHOOL_DISTRICT_NAME, ";
        $query .= "     F1.FINSCHOOL_PREF_CD, ";
        $query .= "     P1.PREF_NAME AS FINSCHOOL_PREF_NAME, ";
        $query .= "     F1.FINSCHOOL_CITY_CD, ";
        $query .= "     C1.CITY_NAME AS FINSCHOOL_CITY_NAME, ";
        $query .= "     F1.FINSCHOOL_ZIPCD, ";
        $query .= "     F1.FINSCHOOL_ADDR1, ";
        $query .= "     F1.FINSCHOOL_ADDR2, ";
        $query .= "     F1.FINSCHOOL_TELNO, ";
        $query .= "     F1.FINSCHOOL_FAXNO, ";
        $query .= "     F1.EDBOARDCD, ";
        $query .= "     F2.REMARK1 AS TOUHAIKOU, ";
        if ($model->Properties["useLocationMst"] == '1') {
            $query .= "     F3.REMARK1 AS DISTRICT_NAME, ";
        }
        if ($model->Properties["useMiraicompass"] == '1') {
            $query .= "     M1.MIRAI_FS_CD, ";
        }
        $query .= "     F4.REMARK1 AS INJI_FLG, ";
        $query .= "     '".$model->lastColumn."' AS ".$model->lastColumn." ";
        $query .= " FROM ";
        $query .= "     FINSCHOOL_MST F1 ";
        $query .= "     LEFT JOIN FINSCHOOL_DETAIL_MST F2 ON F1.FINSCHOOLCD   = F2.FINSCHOOLCD ";
        $query .= "                                      AND F2.FINSCHOOL_SEQ = '001' ";
        $query .= "     LEFT JOIN FINSCHOOL_DETAIL_MST F3 ON F1.FINSCHOOLCD   = F3.FINSCHOOLCD ";
        $query .= "                                      AND F3.FINSCHOOL_SEQ = '002' ";
        $query .= "     LEFT JOIN FINSCHOOL_DETAIL_MST F4 ON F1.FINSCHOOLCD   = F4.FINSCHOOLCD ";
        $query .= "                                      AND F4.FINSCHOOL_SEQ = '003' ";
        $query .= "     LEFT JOIN NAME_MST NMZ003 ON F1.DISTRICTCD = NMZ003.NAMECD2 ";
        $query .= "                              AND NMZ003.NAMECD1 = 'Z003' ";
        $query .= "     LEFT JOIN PREF_MST P1 ON F1.FINSCHOOL_PREF_CD  = P1.PREF_CD ";
        $query .= "     LEFT JOIN CITY_MST C1 ON F1.FINSCHOOL_PREF_CD  = C1.PREF_CD ";
        $query .= "                          AND F1.FINSCHOOL_CITY_CD  = C1.CITY_CD ";
        $query .= "     LEFT JOIN STAFF_MST S1 ON F1.FINSCHOOL_STAFFCD  = S1.STAFFCD ";
        if ($model->Properties["useMiraicompass"] == '1') {
            $query .= "     LEFT JOIN ENTEXAM_MIRAI_FS_REP_DAT M1 ON F1.FINSCHOOLCD = M1.FS_CD ";
        }
        if ($model->selectFinschoolType != "" && $model->selectFinschoolType != "99") {
            $query .= " WHERE ";
            $query .= "     F1.FINSCHOOL_TYPE = '{$model->selectFinschoolType}' ";
        }
        $query .= "ORDER BY";
        $query .= "     F1.FINSCHOOLCD";

        return $query;
    }

    //年度データの確認
    public function isExisting($db, $finschoolcd)
    {
        $query = "SELECT * FROM FINSCHOOL_YDAT WHERE FINSCHOOLCD = '".$finschoolcd."'";
        $row   = $db->getRow($query);

        if (is_array($row)) {
            return true;
        } else {
            return false;
        }
    }

    //CSV取り込み時の入力チェック、名称マスターに登録されているコードなのか。
    public function checkNameMst($namecd1, $namecd2)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = '{$namecd1}' AND ";
        $query .= "     NAMECD2 = '{$namecd2}' ";

        return $query;
    }

    //CSV取り込み時の入力チェック、都道府県マスタに登録されているコードなのか。
    public function checkPrefcd($prefcd)
    {
        $query .= " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     PREF_MST ";
        $query .= " WHERE ";
        $query .= "     PREF_CD = '{$prefcd}' ";

        return $query;
    }

    //CSV取り込み時の入力チェック、市区町村マスタに登録されているコードなのか。
    public function checkCitycd($prefcd, $citycd)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     CITY_MST ";
        $query .= " WHERE ";
        $query .= "     PREF_CD = '{$prefcd}' AND ";
        $query .= "     CITY_CD = '{$citycd}' ";

        return $query;
    }

    //CSV取り込み時の入力チェック、職員マスタに登録されているコードなのか。
    public function checkStaffcd($staffcd)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     STAFF_MST ";
        $query .= " WHERE ";
        $query .= "     STAFFCD = '{$staffcd}' ";

        return $query;
    }

    //CSV取り込み時の入力チェック、教育委員会マスターに登録されているコードなのか。
    public function checkEdboardcd($edboardcd)
    {
        $query .= " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     EDBOARD_MST ";
        $query .= " WHERE ";
        $query .= "     EDBOARDCD = '{$edboardcd}' ";

        return $query;
    }

    //CSV取り込み時の入力チェック、出身校所在地マスターに登録されているコードなのか。
    public function checkDistrictcd($districtcd)
    {
        $query .= " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     FINSCHOOL_LOCATION_MST ";
        $query .= " WHERE ";
        $query .= "     DISTRICTCD = '{$districtcd}' ";

        return $query;
    }

    //エラーＤＢへの追加
    public function insertQueryErr(&$db, $record_no, $check_error)
    {
        $data1["PROGRAMID"][TEXT] = PROGRAMID;
        $data1["MSGROW"][NUMBER]  = $record_no;
        $data1["MSGREMARK"][TEXT] = $check_error;

        $query = Query::insertSQL($data1, "W_CSVMSG_PRG_DAT");
        $result = $db->query($query);
    }

    //--- INSERT
    public function &getInsertQuery($fields, $model)
    {
        $db = Query::dbCheckOut();

        $data = array();
        $data["FINSCHOOLCD"][TEXT]         = $fields["FINSCHOOLCD"];
        $data["FINSCHOOL_TYPE"][TEXT]      = $fields["FINSCHOOL_TYPE"];
        $data["FINSCHOOL_DISTCD"][TEXT]    = $fields["FINSCHOOL_DISTCD"];
        $data["FINSCHOOL_DISTCD2"][TEXT]   = $fields["FINSCHOOL_DISTCD2"];
        $data["FINSCHOOL_DIV"][TEXT]       = $fields["FINSCHOOL_DIV"];
        $data["FINSCHOOL_NAME"][TEXT]      = $fields["FINSCHOOL_NAME"];
        $data["FINSCHOOL_KANA"][TEXT]      = $fields["FINSCHOOL_KANA"];
        $data["FINSCHOOL_NAME_ABBV"][TEXT] = $fields["FINSCHOOL_NAME_ABBV"];
        $data["FINSCHOOL_KANA_ABBV"][TEXT] = $fields["FINSCHOOL_KANA_ABBV"];
        $data["PRINCNAME"][TEXT]           = $fields["PRINCNAME"];
        $data["PRINCNAME_SHOW"][TEXT]      = $fields["PRINCNAME_SHOW"];
        $data["PRINCKANA"][TEXT]           = $fields["PRINCKANA"];
        $data["DISTRICTCD"][TEXT]          = $fields["DISTRICTCD"];
        $data["FINSCHOOL_PREF_CD"][TEXT]   = $fields["FINSCHOOL_PREF_CD"];
        $data["FINSCHOOL_ZIPCD"][TEXT]     = $fields["FINSCHOOL_ZIPCD"];
        $data["FINSCHOOL_ADDR1"][TEXT]     = $fields["FINSCHOOL_ADDR1"];
        $data["FINSCHOOL_ADDR2"][TEXT]     = $fields["FINSCHOOL_ADDR2"];
        $data["FINSCHOOL_TELNO"][TEXT]     = $fields["FINSCHOOL_TELNO"];
        $data["FINSCHOOL_FAXNO"][TEXT]     = $fields["FINSCHOOL_FAXNO"];
        $data["EDBOARDCD"][TEXT]           = $fields["EDBOARDCD"];
        $data["FINSCHOOL_STAFFCD"][TEXT]   = $fields["FINSCHOOL_STAFFCD"];
        $data["FINSCHOOL_CITY_CD"][TEXT]   = $fields["FINSCHOOL_CITY_CD"];
        $data["REGISTERCD"][TEXT]          = STAFFCD;
        $data["UPDATED"][FUNC]             = "SYSDATE()";

        $query = Query::insertSQL($data, "FINSCHOOL_MST");
        $db->query($query);

        //統廃校フラグ
        $data = array();
        $data["FINSCHOOLCD"][TEXT]   = $fields["FINSCHOOLCD"];
        $data["FINSCHOOL_SEQ"][TEXT] = "001";
        $data["REMARK1"][TEXT]       = $fields["TOUHAIKOU"];
        $data["REGISTERCD"][TEXT]    = STAFFCD;
        $data["UPDATED"][FUNC]       = "SYSDATE()";
        $query = Query::insertSQL($data, "FINSCHOOL_DETAIL_MST");
        $db->query($query);

        //所在地コード
        if ($model->Properties["useLocationMst"] == '1') {
            $data = array();
            $data["FINSCHOOLCD"][TEXT]   = $fields["FINSCHOOLCD"];
            $data["FINSCHOOL_SEQ"][TEXT] = "002";
            $data["REMARK1"][TEXT]       = $fields["DISTRICT_NAME"];
            $data["REGISTERCD"][TEXT]    = STAFFCD;
            $data["UPDATED"][FUNC]       = "SYSDATE()";
            $query = Query::insertSQL($data, "FINSCHOOL_DETAIL_MST");
            $db->query($query);
        }
        
        //卒業印字フラグ
        $data = array();
        $data["FINSCHOOLCD"][TEXT]   = $fields["FINSCHOOLCD"];
        $data["FINSCHOOL_SEQ"][TEXT] = "003";
        $data["REMARK1"][TEXT]       = $fields["INJI_FLG"];
        $data["REGISTERCD"][TEXT]    = STAFFCD;
        $data["UPDATED"][FUNC]       = "SYSDATE()";
        $query = Query::insertSQL($data, "FINSCHOOL_DETAIL_MST");
        $db->query($query);
        
        //ミライコンパス学校コード
        if ($model->Properties["useMiraicompass"] == '1') {
            $query  = " DELETE FROM ENTEXAM_MIRAI_FS_REP_DAT ";
            $query .= " WHERE FS_CD = '".$fields["FINSCHOOLCD"]."' ";
            $db->query($query);
            if (strlen($fields["MIRAI_FS_CD"])) {
                $query  = " DELETE FROM ENTEXAM_MIRAI_FS_REP_DAT ";
                $query .= " WHERE MIRAI_FS_CD = '".$fields["MIRAI_FS_CD"]."' ";
                $db->query($query);
                $data = array();
                $data["MIRAI_FS_CD"][TEXT]   = $fields["MIRAI_FS_CD"];
                $data["FS_CD"][TEXT]         = $fields["FINSCHOOLCD"];
                $data["REGISTERCD"][TEXT]    = STAFFCD;
                $data["UPDATED"][FUNC]       = "SYSDATE()";
                $query = Query::insertSQL($data, "ENTEXAM_MIRAI_FS_REP_DAT");
                $db->query($query);
            }
        }
        
        Query::dbCheckIn($db);
        return;
    }

    //--- UPDATE
    public function &getUpdateQuery($fields, $model)
    {
        $db = Query::dbCheckOut();

        $data = array();
        $data["FINSCHOOLCD"][TEXT]         = $fields["FINSCHOOLCD"];
        $data["FINSCHOOL_TYPE"][TEXT]      = $fields["FINSCHOOL_TYPE"];
        $data["FINSCHOOL_DISTCD"][TEXT]    = $fields["FINSCHOOL_DISTCD"];
        $data["FINSCHOOL_DISTCD2"][TEXT]   = $fields["FINSCHOOL_DISTCD2"];
        $data["FINSCHOOL_DIV"][TEXT]       = $fields["FINSCHOOL_DIV"];
        $data["FINSCHOOL_NAME"][TEXT]      = $fields["FINSCHOOL_NAME"];
        $data["FINSCHOOL_KANA"][TEXT]      = $fields["FINSCHOOL_KANA"];
        $data["FINSCHOOL_NAME_ABBV"][TEXT] = $fields["FINSCHOOL_NAME_ABBV"];
        $data["FINSCHOOL_KANA_ABBV"][TEXT] = $fields["FINSCHOOL_KANA_ABBV"];
        $data["PRINCNAME"][TEXT]           = $fields["PRINCNAME"];
        $data["PRINCNAME_SHOW"][TEXT]      = $fields["PRINCNAME_SHOW"];
        $data["PRINCKANA"][TEXT]           = $fields["PRINCKANA"];
        $data["DISTRICTCD"][TEXT]          = $fields["DISTRICTCD"];
        $data["FINSCHOOL_PREF_CD"][TEXT]   = $fields["FINSCHOOL_PREF_CD"];
        $data["FINSCHOOL_ZIPCD"][TEXT]     = $fields["FINSCHOOL_ZIPCD"];
        $data["FINSCHOOL_ADDR1"][TEXT]     = $fields["FINSCHOOL_ADDR1"];
        $data["FINSCHOOL_ADDR2"][TEXT]     = $fields["FINSCHOOL_ADDR2"];
        $data["FINSCHOOL_TELNO"][TEXT]     = $fields["FINSCHOOL_TELNO"];
        $data["FINSCHOOL_FAXNO"][TEXT]     = $fields["FINSCHOOL_FAXNO"];
        $data["EDBOARDCD"][TEXT]           = $fields["EDBOARDCD"];
        $data["FINSCHOOL_STAFFCD"][TEXT]   = $fields["FINSCHOOL_STAFFCD"];
        $data["FINSCHOOL_CITY_CD"][TEXT]   = $fields["FINSCHOOL_CITY_CD"];
        $data["REGISTERCD"][TEXT]          = STAFFCD;
        $data["UPDATED"][FUNC]             = "SYSDATE()";

        $where = "WHERE FINSCHOOLCD = '".$fields["FINSCHOOLCD"]."'";
        $query = Query::updateSQL($data, "FINSCHOOL_MST", $where);

        $db->query($query);

        //統廃校フラグ
        $data = array();
        $data["FINSCHOOLCD"][TEXT]   = $fields["FINSCHOOLCD"];
        $data["FINSCHOOL_SEQ"][TEXT] = '001';
        $data["REMARK1"][TEXT]       = $fields["TOUHAIKOU"];
        $data["REGISTERCD"][TEXT]    = STAFFCD;
        $data["UPDATED"][FUNC]       = "SYSDATE()";

        if (1 > $db->getOne("SELECT COUNT(*) FROM FINSCHOOL_DETAIL_MST WHERE FINSCHOOLCD = '{$fields["FINSCHOOLCD"]}' AND FINSCHOOL_SEQ = '001'")) {
            $query = Query::insertSQL($data, "FINSCHOOL_DETAIL_MST");
        } else {
            $where = " WHERE FINSCHOOLCD = '".$fields["FINSCHOOLCD"]."' AND FINSCHOOL_SEQ = '001' ";
            $query = Query::updateSQL($data, "FINSCHOOL_DETAIL_MST", $where);
        }
        $db->query($query);

        //所在地コード
        if ($model->Properties["useLocationMst"] == '1') {
            $data = array();
            $data["FINSCHOOLCD"][TEXT]   = $fields["FINSCHOOLCD"];
            $data["FINSCHOOL_SEQ"][TEXT] = '002';
            $data["REMARK1"][TEXT]       = $fields["DISTRICT_NAME"];
            $data["REGISTERCD"][TEXT]    = STAFFCD;
            $data["UPDATED"][FUNC]       = "SYSDATE()";
            
            if (1 > $db->getOne("SELECT COUNT(*) FROM FINSCHOOL_DETAIL_MST WHERE FINSCHOOLCD = '{$fields["FINSCHOOLCD"]}' AND FINSCHOOL_SEQ = '002'")) {
                $query = Query::insertSQL($data, "FINSCHOOL_DETAIL_MST");
            } else {
                $where = " WHERE FINSCHOOLCD = '".$fields["FINSCHOOLCD"]."' AND FINSCHOOL_SEQ = '002' ";
                $query = Query::updateSQL($data, "FINSCHOOL_DETAIL_MST", $where);
            }
            $db->query($query);
        }



        //卒業印字フラグ
        $data = array();
        $data["FINSCHOOLCD"][TEXT]   = $fields["FINSCHOOLCD"];
        $data["FINSCHOOL_SEQ"][TEXT] = '003';
        $data["REMARK1"][TEXT]       = $fields["INJI_FLG"];
        $data["REGISTERCD"][TEXT]    = STAFFCD;
        $data["UPDATED"][FUNC]       = "SYSDATE()";
        if (1 > $db->getOne("SELECT COUNT(*) FROM FINSCHOOL_DETAIL_MST WHERE FINSCHOOLCD = '{$fields["FINSCHOOLCD"]}' AND FINSCHOOL_SEQ = '003'")) {
            $query = Query::insertSQL($data, "FINSCHOOL_DETAIL_MST");
        } else {
            $where = " WHERE FINSCHOOLCD = '".$fields["FINSCHOOLCD"]."' AND FINSCHOOL_SEQ = '003' ";
            $query = Query::updateSQL($data, "FINSCHOOL_DETAIL_MST", $where);
        }
        $db->query($query);

        //ミライコンパス学校コード
        if ($model->Properties["useMiraicompass"] == '1') {
            $query  = " DELETE FROM ENTEXAM_MIRAI_FS_REP_DAT ";
            $query .= " WHERE FS_CD = '".$fields["FINSCHOOLCD"]."' ";
            $db->query($query);
            if (strlen($fields["MIRAI_FS_CD"])) {
                $query  = " DELETE FROM ENTEXAM_MIRAI_FS_REP_DAT ";
                $query .= " WHERE MIRAI_FS_CD = '".$fields["MIRAI_FS_CD"]."' ";
                $db->query($query);
                $data = array();
                $data["MIRAI_FS_CD"][TEXT]   = $fields["MIRAI_FS_CD"];
                $data["FS_CD"][TEXT]         = $fields["FINSCHOOLCD"];
                $data["REGISTERCD"][TEXT]    = STAFFCD;
                $data["UPDATED"][FUNC]       = "SYSDATE()";
                $query = Query::insertSQL($data, "ENTEXAM_MIRAI_FS_REP_DAT");
                $db->query($query);
            }
        }

        Query::dbCheckIn($db);
        return;
    }

    //--- DELETE
    public function &getDeleteQuery($db, $fields, $model)
    {
        $query  = " DELETE FROM FINSCHOOL_MST ";
        $query .= " WHERE FINSCHOOLCD  = '".$fields["FINSCHOOLCD"]."' ";

        $db->query($query);

        $query  = " DELETE FROM FINSCHOOL_DETAIL_MST ";
        $query .= " WHERE FINSCHOOLCD = '".$fields["FINSCHOOLCD"]."' AND FINSCHOOL_SEQ = '001' ";

        $db->query($query);

        if ($model->Properties["useLocationMst"] == '1') {
            $query  = " DELETE FROM FINSCHOOL_DETAIL_MST ";
            $query .= " WHERE FINSCHOOLCD = '".$fields["FINSCHOOLCD"]."' AND FINSCHOOL_SEQ = '002' ";

            $db->query($query);
        }

        $query  = " DELETE FROM FINSCHOOL_DETAIL_MST ";
        $query .= " WHERE FINSCHOOLCD = '".$fields["FINSCHOOLCD"]."' AND FINSCHOOL_SEQ = '003' ";
        $db->query($query);

        if ($model->Properties["useMiraicompass"] == '1') {
            $query  = " DELETE FROM ENTEXAM_MIRAI_FS_REP_DAT ";
            $query .= " WHERE FS_CD = '".$fields["FINSCHOOLCD"]."' ";
            $db->query($query);
            if (strlen($fields["MIRAI_FS_CD"])) {
                $query  = " DELETE FROM ENTEXAM_MIRAI_FS_REP_DAT ";
                $query .= " WHERE MIRAI_FS_CD = '".$fields["MIRAI_FS_CD"]."' ";
                $db->query($query);
            }
        }

        return true;
    }

    //CSVファイルよりDBへインサート
    public function insertQueryCsv($db, $model, $data_arr)
    {
        $data = array();
        $db->autoCommit(false);

        $cnt = 0;    //処理件数
        for ($i = 0; $i < get_count($data_arr); $i++) {
            if ($model->selectFinschoolType == "99" || $model->selectFinschoolType == $data_arr[$i]["FINSCHOOL_TYPE"]) {
                //データセット
                $data = array();
                $data["FINSCHOOLCD"][TEXT]         = $data_arr[$i]["FINSCHOOLCD"];
                $data["FINSCHOOL_TYPE"][TEXT]      = $data_arr[$i]["FINSCHOOL_TYPE"];
                $data["FINSCHOOL_DISTCD"][TEXT]    = $data_arr[$i]["FINSCHOOL_DISTCD"];
                $data["FINSCHOOL_DISTCD2"][TEXT]   = $data_arr[$i]["FINSCHOOL_DISTCD2"];
                $data["FINSCHOOL_DIV"][TEXT]       = $data_arr[$i]["FINSCHOOL_DIV"];
                $data["FINSCHOOL_NAME"][TEXT]      = $data_arr[$i]["FINSCHOOL_NAME"];
                $data["FINSCHOOL_KANA"][TEXT]      = $data_arr[$i]["FINSCHOOL_KANA"];
                $data["FINSCHOOL_NAME_ABBV"][TEXT] = $data_arr[$i]["FINSCHOOL_NAME_ABBV"];
                $data["FINSCHOOL_KANA_ABBV"][TEXT] = $data_arr[$i]["FINSCHOOL_KANA_ABBV"];
                $data["PRINCNAME"][TEXT]           = $data_arr[$i]["PRINCNAME"];
                $data["PRINCNAME_SHOW"][TEXT]      = $data_arr[$i]["PRINCNAME_SHOW"];
                $data["PRINCKANA"][TEXT]           = $data_arr[$i]["PRINCKANA"];
                $data["DISTRICTCD"][TEXT]          = $data_arr[$i]["DISTRICTCD"];
                $data["FINSCHOOL_PREF_CD"][TEXT]   = $data_arr[$i]["FINSCHOOL_PREF_CD"];
                $data["FINSCHOOL_ZIPCD"][TEXT]     = $data_arr[$i]["FINSCHOOL_ZIPCD"];
                $data["FINSCHOOL_ADDR1"][TEXT]     = $data_arr[$i]["FINSCHOOL_ADDR1"];
                $data["FINSCHOOL_ADDR2"][TEXT]     = $data_arr[$i]["FINSCHOOL_ADDR2"];
                $data["FINSCHOOL_TELNO"][TEXT]     = $data_arr[$i]["FINSCHOOL_TELNO"];
                $data["FINSCHOOL_FAXNO"][TEXT]     = $data_arr[$i]["FINSCHOOL_FAXNO"];
                $data["EDBOARDCD"][TEXT]           = $data_arr[$i]["EDBOARDCD"];
                $data["FINSCHOOL_STAFFCD"][TEXT]   = $data_arr[$i]["FINSCHOOL_STAFFCD"];
                $data["FINSCHOOL_CITY_CD"][TEXT]   = $data_arr[$i]["FINSCHOOL_CITY_CD"];
                $data["REGISTERCD"][TEXT]          = STAFFCD;
                $data["UPDATED"][NUMBER]           = "SYSDATE()";
                if (1 > $db->getOne("SELECT COUNT(*) FROM FINSCHOOL_MST WHERE FINSCHOOLCD = '{$data_arr[$i]["FINSCHOOLCD"]}'")) {
                    $query = Query::insertSQL($data, "FINSCHOOL_MST");
                } else {
                    $where  = "WHERE ";
                    $where .= "     FINSCHOOLCD = '{$data_arr[$i]["FINSCHOOLCD"]}' ";

                    $query = Query::updateSQL($data, "FINSCHOOL_MST", $where);
                }
                $db->query($query);

                //統廃校フラグ
                $data = array();
                $data["FINSCHOOLCD"][TEXT]         = $data_arr[$i]["FINSCHOOLCD"];
                $data["FINSCHOOL_SEQ"][TEXT]       = '001';
                $data["REMARK1"][TEXT]             = $data_arr[$i]["TOUHAIKOU"];
                $data["REGISTERCD"][TEXT]          = STAFFCD;
                $data["UPDATED"][NUMBER]           = "SYSDATE()";
                if (1 > $db->getOne("SELECT COUNT(*) FROM FINSCHOOL_DETAIL_MST WHERE FINSCHOOLCD = '{$data_arr[$i]["FINSCHOOLCD"]}' AND FINSCHOOL_SEQ = '001'")) {
                    $query = Query::insertSQL($data, "FINSCHOOL_DETAIL_MST");
                } else {
                    $where  = "WHERE ";
                    $where .= "     FINSCHOOLCD = '{$data_arr[$i]["FINSCHOOLCD"]}' AND FINSCHOOL_SEQ = '001' ";

                    $query = Query::updateSQL($data, "FINSCHOOL_DETAIL_MST", $where);
                }
                $db->query($query);

                //所在地コード
                if ($model->Properties["useLocationMst"] == '1') {
                    $data = array();
                    $data["FINSCHOOLCD"][TEXT]         = $data_arr[$i]["FINSCHOOLCD"];
                    $data["FINSCHOOL_SEQ"][TEXT]       = '002';
                    $data["REMARK1"][TEXT]             = $data_arr[$i]["DISTRICT_NAME"];
                    $data["REGISTERCD"][TEXT]          = STAFFCD;
                    $data["UPDATED"][NUMBER]           = "SYSDATE()";
                    if (1 > $db->getOne("SELECT COUNT(*) FROM FINSCHOOL_DETAIL_MST WHERE FINSCHOOLCD = '{$data_arr[$i]["FINSCHOOLCD"]}' AND FINSCHOOL_SEQ = '002'")) {
                        $query = Query::insertSQL($data, "FINSCHOOL_DETAIL_MST");
                    } else {
                        $where  = "WHERE ";
                        $where .= "     FINSCHOOLCD = '{$data_arr[$i]["FINSCHOOLCD"]}' AND FINSCHOOL_SEQ = '002' ";

                        $query = Query::updateSQL($data, "FINSCHOOL_DETAIL_MST", $where);
                    }
                    $db->query($query);
                }
                
                //卒業印字フラグ
                $data = array();
                $data["FINSCHOOLCD"][TEXT]         = $data_arr[$i]["FINSCHOOLCD"];
                $data["FINSCHOOL_SEQ"][TEXT]       = '003';
                $data["REMARK1"][TEXT]             = $data_arr[$i]["INJI_FLG"];
                $data["REGISTERCD"][TEXT]          = STAFFCD;
                $data["UPDATED"][NUMBER]           = "SYSDATE()";
                if (1 > $db->getOne("SELECT COUNT(*) FROM FINSCHOOL_DETAIL_MST WHERE FINSCHOOLCD = '{$data_arr[$i]["FINSCHOOLCD"]}' AND FINSCHOOL_SEQ = '003'")) {
                    $query = Query::insertSQL($data, "FINSCHOOL_DETAIL_MST");
                } else {
                    $where  = "WHERE ";
                    $where .= "     FINSCHOOLCD = '{$data_arr[$i]["FINSCHOOLCD"]}' AND FINSCHOOL_SEQ = '003' ";

                    $query = Query::updateSQL($data, "FINSCHOOL_DETAIL_MST", $where);
                }
                $db->query($query);

                //ミライコンパス学校コード
                if ($model->Properties["useMiraicompass"] == '1') {
                    $query  = " DELETE FROM ENTEXAM_MIRAI_FS_REP_DAT ";
                    $query .= " WHERE FS_CD = '".$data_arr[$i]["FINSCHOOLCD"]."' ";
                    $db->query($query);
                    if (strlen($data_arr[$i]["MIRAI_FS_CD"])) {
                        $query  = " DELETE FROM ENTEXAM_MIRAI_FS_REP_DAT ";
                        $query .= " WHERE MIRAI_FS_CD = '".$data_arr[$i]["MIRAI_FS_CD"]."' ";
                        $db->query($query);
                        $data = array();
                        $data["MIRAI_FS_CD"][TEXT]   = $data_arr[$i]["MIRAI_FS_CD"];
                        $data["FS_CD"][TEXT]         = $data_arr[$i]["FINSCHOOLCD"];
                        $data["REGISTERCD"][TEXT]    = STAFFCD;
                        $data["UPDATED"][FUNC]       = "SYSDATE()";
                        $query = Query::insertSQL($data, "ENTEXAM_MIRAI_FS_REP_DAT");
                        $db->query($query);
                    }
                }

                $cnt++;
            }
        }
        $db->commit();

        return $cnt;
    }
}
