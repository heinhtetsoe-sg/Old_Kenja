<?php

require_once('for_php7.php');
class knjz091a_2Query extends Query
{

    //一覧用データ取得
    public function selectQuery($model, $div = "")
    {
        $query  = " ";
        $query .= " SELECT ";
        if ($div == 'CNT') {
            $query .= "     PRI.PRISCHOOLCD, ";
            $query .= "     COUNT(*) AS CNT ";
        } else {
            $query .= "     PRI.PRISCHOOLCD, ";
            $query .= "     PRI.PRISCHOOL_NAME, ";
            $query .= "     PRI_CLASS.PRISCHOOL_CLASS_CD, ";
            $query .= "     PRI_CLASS.PRISCHOOL_NAME AS PRISCHOOL_CLASS_NAME, ";
            $query .= "     PRI.PRISCHOOL_KANA, ";
            $query .= "     PRI.PRINCNAME, ";
            $query .= "     PRI.PRINCNAME_SHOW, ";
            $query .= "     PRI.PRINCKANA, ";
            $query .= "     PRI.PRISCHOOL_STAFFCD || ':' || S1.STAFFNAME AS PRISCHOOL_STAFFCD, ";
            $query .= "     PRI.DISTRICTCD || ':' || MEISYOU_GET(PRI.DISTRICTCD,'Z060',1) AS DISTRICTCD, ";
            $query .= "     PRI.PRISCHOOL_PREF_CD || ':' || P1.PREF_NAME AS PRISCHOOL_PREF_CD, ";
            $query .= "     PRI.PRISCHOOL_CITY_CD || ':' || C1.CITY_NAME AS PRISCHOOL_CITY_CD, ";
            $query .= "     PRI.PRISCHOOL_ZIPCD, ";
            $query .= "     PRI.PRISCHOOL_ADDR1, ";
            $query .= "     PRI.PRISCHOOL_ADDR2, ";
            $query .= "     PRI.PRISCHOOL_TELNO, ";
            $query .= "     PRI.PRISCHOOL_FAXNO, ";
            $query .= "     PRI.GRP_PRISCHOOLCD ";
        }
        $query .= " FROM ";
        $query .= "     PRISCHOOL_MST PRI ";
        $query .= "     LEFT JOIN PRISCHOOL_CLASS_MST PRI_CLASS ON PRI.PRISCHOOLCD = PRI_CLASS.PRISCHOOLCD ";
        $query .= "     LEFT JOIN PREF_MST P1 ON PRI.PRISCHOOL_PREF_CD  = P1.PREF_CD ";
        $query .= "     LEFT JOIN CITY_MST C1 ON PRI.PRISCHOOL_PREF_CD  = C1.PREF_CD AND PRI.PRISCHOOL_CITY_CD  = C1.CITY_CD ";
        $query .= "     LEFT JOIN STAFF_MST S1 ON PRI.PRISCHOOL_STAFFCD  = S1.STAFFCD ";
        if ($div == 'CNT') {
            $query .= " GROUP BY ";
            $query .= "     PRI.PRISCHOOLCD ";
        } else {
            $query .= " ORDER BY ";
            $query .= "     PRI.PRISCHOOLCD ";
        }
        return $query ;
    }

    //１レコード取得
    public function getRow($prischoolcd, $model)
    {
        $db = Query::dbCheckOut();
        $query  = " SELECT ";
        $query .= "     P1.* ";
        if ($model->Properties["useMiraicompass"] == '1') {
            $query .= "     ,P2.MIRAI_PS_CD ";
        }
        $query .= " FROM ";
        $query .= "     PRISCHOOL_MST P1 ";
        if ($model->Properties["useMiraicompass"] == '1') {
            $query .= "     LEFT JOIN ENTEXAM_MIRAI_PS_REP_DAT P2 ON P1.PRISCHOOLCD = P2.PS_CD ";
        }
        $query .= " WHERE ";
        $query .= "     P1.PRISCHOOLCD = '" .$prischoolcd ."'";
        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);
        return $row;
    }

    //教室
    public function getPriClass($model)
    {
        $query  = " ";
        $query .= " SELECT ";
        $query .= "     PRI_CLASS.* ";
        $query .= " FROM ";
        $query .= "     PRISCHOOL_CLASS_MST PRI_CLASS ";
        $query .= " WHERE ";
        $query .= "     PRI_CLASS.PRISCHOOLCD = '{$model->prischoolcd}' ";
        $query .= " ORDER BY ";
        $query .= "     PRI_CLASS.PRISCHOOL_CLASS_CD ";

        return $query ;
    }

    //地区コード
    public function getDistinct()
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     NAMECD2 || ' ' || NAME1 AS LABEL, ";
        $query .= "     NAMECD2 AS VALUE ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'Z060' ";
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

    //職員データ取得
    public function getStaffMst()
    {
        $query  = " SELECT ";
        $query .= "     STAFFCD || ' ' || STAFFNAME AS LABEL, ";
        $query .= "     STAFFCD AS VALUE ";
        $query .= " FROM ";
        $query .= "     STAFF_MST ";
        $query .= " ORDER  BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //年度データの確認
    public function isExisting($prischoolcd)
    {
        $db    = Query::dbCheckOut();
        $query = "select * from PRISCHOOL_YDAT where PRISCHOOLCD = '".$prischoolcd."'";
        $row   = $db->getRow($query);
        Query::dbCheckIn($db);

        if (is_array($row)) {
            return true;
        } else {
            return false;
        }
    }

    //--- INSERT
    public function &getInsertQuery($fields, $model)
    {
        $db = Query::dbCheckOut();

        $data["PRISCHOOLCD"][TEXT]          = $fields["PRISCHOOLCD"];
        $data["PRISCHOOL_NAME"][TEXT]       = $fields["PRISCHOOL_NAME"];
        $data["PRISCHOOL_KANA"][TEXT]       = $fields["PRISCHOOL_KANA"];
        $data["PRINCNAME"][TEXT]            = $fields["PRINCNAME"];
        $data["PRINCNAME_SHOW"][TEXT]       = $fields["PRINCNAME_SHOW"];
        $data["PRINCKANA"][TEXT]            = $fields["PRINCKANA"];
        $data["DISTRICTCD"][TEXT]           = $fields["DISTRICTCD"];
        $data["PRISCHOOL_ZIPCD"][TEXT]      = $fields["PRISCHOOL_ZIPCD"];
        $data["PRISCHOOL_ADDR1"][TEXT]      = $fields["PRISCHOOL_ADDR1"];
        $data["PRISCHOOL_ADDR2"][TEXT]      = $fields["PRISCHOOL_ADDR2"];
        $data["PRISCHOOL_TELNO"][TEXT]      = $fields["PRISCHOOL_TELNO"];
        $data["PRISCHOOL_FAXNO"][TEXT]      = $fields["PRISCHOOL_FAXNO"];
        $data["GRP_PRISCHOOLCD"][TEXT]      = $fields["GRP_PRISCHOOLCD"];
        $data["PRISCHOOL_STAFFCD"][TEXT]    = $fields["PRISCHOOL_STAFFCD"];
        $data["PRISCHOOL_PREF_CD"][TEXT]    = $fields["PRISCHOOL_PREF_CD"];
        $data["PRISCHOOL_CITY_CD"][TEXT]    = $fields["PRISCHOOL_CITY_CD"];
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][FUNC]              = "sysdate()";

        $query = Query::insertSQL($data, "PRISCHOOL_MST");
        $db->query($query);

        //ミライコンパス塾コード
        if ($model->Properties["useMiraicompass"] == '1') {
            $query  = " DELETE FROM ENTEXAM_MIRAI_PS_REP_DAT ";
            $query .= " WHERE PS_CD = '".$fields["PRISCHOOLCD"]."' ";
            $db->query($query);
            if (strlen($fields["MIRAI_PS_CD"])) {
                $query  = " DELETE FROM ENTEXAM_MIRAI_PS_REP_DAT ";
                $query .= " WHERE MIRAI_PS_CD = '".$fields["MIRAI_PS_CD"]."' ";
                $db->query($query);
                $data = array();
                $data["MIRAI_PS_CD"][TEXT]   = $fields["MIRAI_PS_CD"];
                $data["PS_CD"][TEXT]         = $fields["PRISCHOOLCD"];
                $data["REGISTERCD"][TEXT]    = STAFFCD;
                $data["UPDATED"][FUNC]       = "SYSDATE()";
                $query = Query::insertSQL($data, "ENTEXAM_MIRAI_PS_REP_DAT");
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

        $data["PRISCHOOLCD"][TEXT]          = $fields["PRISCHOOLCD"];
        $data["PRISCHOOL_NAME"][TEXT]       = $fields["PRISCHOOL_NAME"];
        $data["PRISCHOOL_KANA"][TEXT]       = $fields["PRISCHOOL_KANA"];
        $data["PRINCNAME"][TEXT]            = $fields["PRINCNAME"];
        $data["PRINCNAME_SHOW"][TEXT]       = $fields["PRINCNAME_SHOW"];
        $data["PRINCKANA"][TEXT]            = $fields["PRINCKANA"];
        $data["DISTRICTCD"][TEXT]           = $fields["DISTRICTCD"];
        $data["PRISCHOOL_ZIPCD"][TEXT]      = $fields["PRISCHOOL_ZIPCD"];
        $data["PRISCHOOL_ADDR1"][TEXT]      = $fields["PRISCHOOL_ADDR1"];
        $data["PRISCHOOL_ADDR2"][TEXT]      = $fields["PRISCHOOL_ADDR2"];
        $data["PRISCHOOL_TELNO"][TEXT]      = $fields["PRISCHOOL_TELNO"];
        $data["PRISCHOOL_FAXNO"][TEXT]      = $fields["PRISCHOOL_FAXNO"];
        $data["GRP_PRISCHOOLCD"][TEXT]      = $fields["GRP_PRISCHOOLCD"];
        $data["PRISCHOOL_STAFFCD"][TEXT]    = $fields["PRISCHOOL_STAFFCD"];
        $data["PRISCHOOL_PREF_CD"][TEXT]    = $fields["PRISCHOOL_PREF_CD"];
        $data["PRISCHOOL_CITY_CD"][TEXT]    = $fields["PRISCHOOL_CITY_CD"];
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][FUNC]              = "sysdate()";

        $where = "WHERE PRISCHOOLCD         = '" .$fields["PRISCHOOLCD"] ."'";
        $query = Query::updateSQL($data, "PRISCHOOL_MST", $where);

        $db->query($query);

        //ミライコンパス塾コード
        if ($model->Properties["useMiraicompass"] == '1') {
            $query  = " DELETE FROM ENTEXAM_MIRAI_PS_REP_DAT ";
            $query .= " WHERE PS_CD = '".$fields["PRISCHOOLCD"]."' ";
            $db->query($query);
            if (strlen($fields["MIRAI_PS_CD"])) {
                $query  = " DELETE FROM ENTEXAM_MIRAI_PS_REP_DAT ";
                $query .= " WHERE MIRAI_PS_CD = '".$fields["MIRAI_PS_CD"]."' ";
                $db->query($query);
                $data = array();
                $data["MIRAI_PS_CD"][TEXT]   = $fields["MIRAI_PS_CD"];
                $data["PS_CD"][TEXT]         = $fields["PRISCHOOLCD"];
                $data["REGISTERCD"][TEXT]    = STAFFCD;
                $data["UPDATED"][FUNC]       = "SYSDATE()";
                $query = Query::insertSQL($data, "ENTEXAM_MIRAI_PS_REP_DAT");
                $db->query($query);
            }
        }

        Query::dbCheckIn($db);
        return;
    }
    /**
     * DELETE
     */
    public function &getDeleteQuery($fields, $model)
    {
        $db = Query::dbCheckOut();
        $query = "DELETE FROM PRISCHOOL_MST WHERE PRISCHOOLCD = '".$fields["PRISCHOOLCD"]."'";
        $query;
        $db->query($query);

        if ($model->Properties["useMiraicompass"] == '1') {
            $query  = " DELETE FROM ENTEXAM_MIRAI_PS_REP_DAT ";
            $query .= " WHERE PS_CD = '".$fields["PRISCHOOLCD"]."' ";
            $db->query($query);
            if (strlen($fields["MIRAI_PS_CD"])) {
                $query  = " DELETE FROM ENTEXAM_MIRAI_PS_REP_DAT ";
                $query .= " WHERE MIRAI_PS_CD = '".$fields["MIRAI_PS_CD"]."' ";
                $db->query($query);
            }
        }

        Query::dbCheckIn($db);
        return true;
    }

    /**********************/
    /**  存在チェック用  **/
    /**********************/
    //駅ネットマスタ
    public function stationNetMstCntSql($lineCd)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     STATION_NETMST ";
        $query .= " WHERE ";
        $query .= "     LINE_CD = '{$lineCd}' ";

        return $query;
    }

    //名称マスタ登録コードチェック
    public function nameCntSql($namecd, $setcd, $where = "")
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "         NAMECD1 = '{$namecd}' ";
        $query .= "     AND NAMECD2 = '{$setcd}' ";
        if ($where) {
            $query .= $where;
        }

        return $query;
    }

    //職員マスタ
    public function staffMstCntSql($staffcd)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     STAFF_MST ";
        $query .= " WHERE ";
        $query .= "     STAFFCD = '{$staffcd}' ";

        return $query;
    }

    //都道府県マスタ
    public function prefMstCntSql($prefcd)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     PREF_MST ";
        $query .= " WHERE ";
        $query .= "     PREF_CD = '{$prefcd}' ";

        return $query;
    }

    //市区町村マスタマスタ
    public function cityMstCntSql($prefcd, $citycd)
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

    /************************/
    /**  ＣＳＶ取込で使用  **/
    /************************/
    //マスタの追加（ＣＳＶデータより読込）
    public function insertQueryCsv($model, &$data_arr)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $cnt = 0;   //処理件数
        for ($i = 0; $i < get_count($data_arr); $i++) {
            $data = array();
            //データセット「PRISCHOOL_MST（塾マスタ）」
            $data["PRISCHOOLCD"][TEXT]          = $data_arr[$i]["PRISCHOOLCD"];
            $data["PRISCHOOL_NAME"][TEXT]       = $data_arr[$i]["PRISCHOOL_NAME"];
            $data["PRISCHOOL_KANA"][TEXT]       = $data_arr[$i]["PRISCHOOL_KANA"];
            $data["PRINCNAME"][TEXT]            = $data_arr[$i]["PRINCNAME"];
            $data["PRINCNAME_SHOW"][TEXT]       = $data_arr[$i]["PRINCNAME_SHOW"];
            $data["PRINCKANA"][TEXT]            = $data_arr[$i]["PRINCKANA"];
            $data["DISTRICTCD"][TEXT]           = $data_arr[$i]["DISTRICTCD"];
            $data["PRISCHOOL_ZIPCD"][TEXT]      = $data_arr[$i]["PRISCHOOL_ZIPCD"];
            $data["PRISCHOOL_ADDR1"][TEXT]      = $data_arr[$i]["PRISCHOOL_ADDR1"];
            $data["PRISCHOOL_ADDR2"][TEXT]      = $data_arr[$i]["PRISCHOOL_ADDR2"];
            $data["PRISCHOOL_TELNO"][TEXT]      = $data_arr[$i]["PRISCHOOL_TELNO"];
            $data["PRISCHOOL_FAXNO"][TEXT]      = $data_arr[$i]["PRISCHOOL_FAXNO"];
            $data["GRP_PRISCHOOLCD"][TEXT]      = $data_arr[$i]["GRP_PRISCHOOLCD"];
            $data["PRISCHOOL_STAFFCD"][TEXT]    = $data_arr[$i]["PRISCHOOL_STAFFCD"];
            $data["PRISCHOOL_PREF_CD"][TEXT]    = $data_arr[$i]["PRISCHOOL_PREF_CD"];
            $data["PRISCHOOL_CITY_CD"][TEXT]    = $data_arr[$i]["PRISCHOOL_CITY_CD"];
            $data["REGISTERCD"][TEXT]           = STAFFCD;
            $data["UPDATED"][NUMBER]            = "SYSDATE()";

            if (1 > $db->getOne("SELECT COUNT(*) FROM PRISCHOOL_MST WHERE PRISCHOOLCD = '".$data_arr[$i]["PRISCHOOLCD"]."' ")) {
                $query = Query::insertSQL($data, "PRISCHOOL_MST");
            } else {
                $where = " WHERE PRISCHOOLCD = '".$data_arr[$i]["PRISCHOOLCD"]."' ";

                $query = Query::updateSQL($data, "PRISCHOOL_MST", $where);
            }
            $db->query($query);

            //ミライコンパス塾コード
            if ($model->Properties["useMiraicompass"] == '1') {
                $query  = " DELETE FROM ENTEXAM_MIRAI_PS_REP_DAT ";
                $query .= " WHERE PS_CD = '".$data_arr[$i]["PRISCHOOLCD"]."' ";
                $db->query($query);
                if (strlen($data_arr[$i]["MIRAI_PS_CD"])) {
                    $query  = " DELETE FROM ENTEXAM_MIRAI_PS_REP_DAT ";
                    $query .= " WHERE MIRAI_PS_CD = '".$data_arr[$i]["MIRAI_PS_CD"]."' ";
                    $db->query($query);
                    $data = array();
                    $data["MIRAI_PS_CD"][TEXT]   = $data_arr[$i]["MIRAI_PS_CD"];
                    $data["PS_CD"][TEXT]         = $data_arr[$i]["PRISCHOOLCD"];
                    $data["REGISTERCD"][TEXT]    = STAFFCD;
                    $data["UPDATED"][FUNC]       = "SYSDATE()";
                    $query = Query::insertSQL($data, "ENTEXAM_MIRAI_PS_REP_DAT");
                    $db->query($query);
                }
            }

            if ($data_arr[$i]["C_PRISCHOOLCD"] && $data_arr[$i]["C_PRISCHOOL_CLASS_CD"]) {
                //データセット「PRISCHOOL_CLASS_MST（塾教室マスタ）」
                $data = array();
                $data["PRISCHOOLCD"][TEXT]              = $data_arr[$i]["C_PRISCHOOLCD"];
                $data["PRISCHOOL_CLASS_CD"][TEXT]       = $data_arr[$i]["C_PRISCHOOL_CLASS_CD"];
                $data["PRISCHOOL_NAME"][TEXT]           = $data_arr[$i]["C_PRISCHOOL_NAME"];
                $data["PRISCHOOL_KANA"][TEXT]           = $data_arr[$i]["C_PRISCHOOL_KANA"];
                $data["PRINCNAME"][TEXT]                = $data_arr[$i]["C_PRINCNAME"];
                $data["PRINCNAME_SHOW"][TEXT]           = $data_arr[$i]["C_PRINCNAME_SHOW"];
                $data["PRINCKANA"][TEXT]                = $data_arr[$i]["C_PRINCKANA"];
                $data["DISTRICTCD"][TEXT]               = $data_arr[$i]["C_DISTRICTCD"];
                $data["PRISCHOOL_ZIPCD"][TEXT]          = $data_arr[$i]["C_PRISCHOOL_ZIPCD"];
                $data["PRISCHOOL_ADDR1"][TEXT]          = $data_arr[$i]["C_PRISCHOOL_ADDR1"];
                $data["PRISCHOOL_ADDR2"][TEXT]          = $data_arr[$i]["C_PRISCHOOL_ADDR2"];
                $data["PRISCHOOL_TELNO"][TEXT]          = $data_arr[$i]["C_PRISCHOOL_TELNO"];
                $data["PRISCHOOL_FAXNO"][TEXT]          = $data_arr[$i]["C_PRISCHOOL_FAXNO"];
                $data["ROSEN_1"][TEXT]                  = $data_arr[$i]["C_ROSEN_1"];
                $data["ROSEN_2"][TEXT]                  = $data_arr[$i]["C_ROSEN_2"];
                $data["ROSEN_3"][TEXT]                  = $data_arr[$i]["C_ROSEN_3"];
                $data["ROSEN_4"][TEXT]                  = $data_arr[$i]["C_ROSEN_4"];
                $data["ROSEN_5"][TEXT]                  = $data_arr[$i]["C_ROSEN_5"];
                $data["NEAREST_STATION_NAME1"][TEXT]    = $data_arr[$i]["C_NEAREST_STATION_NAME1"];
                $data["NEAREST_STATION_KANA1"][TEXT]    = $data_arr[$i]["C_NEAREST_STATION_KANA1"];
                $data["NEAREST_STATION_NAME2"][TEXT]    = $data_arr[$i]["C_NEAREST_STATION_NAME2"];
                $data["NEAREST_STATION_KANA2"][TEXT]    = $data_arr[$i]["C_NEAREST_STATION_KANA2"];
                $data["DIRECT_MAIL_FLG"][TEXT]          = $data_arr[$i]["C_DIRECT_MAIL_FLG"];
                $data["REGISTERCD"][TEXT]               = STAFFCD;
                $data["UPDATED"][NUMBER]                = "SYSDATE()";

                if (1 > $db->getOne("SELECT COUNT(*) FROM PRISCHOOL_CLASS_MST WHERE PRISCHOOLCD = '".$data_arr[$i]["C_PRISCHOOLCD"]."' AND PRISCHOOL_CLASS_CD = '".$data_arr[$i]["C_PRISCHOOL_CLASS_CD"]."' ")) {
                    $query = Query::insertSQL($data, "PRISCHOOL_CLASS_MST");
                } else {
                    $where  = " WHERE PRISCHOOLCD        = '".$data_arr[$i]["C_PRISCHOOLCD"]."' ";
                    $where .= "   AND PRISCHOOL_CLASS_CD = '".$data_arr[$i]["C_PRISCHOOL_CLASS_CD"]."' ";

                    $query = Query::updateSQL($data, "PRISCHOOL_CLASS_MST", $where);
                }
                $db->query($query);
            }

            $cnt++;
        }
        $db->commit();
        Query::dbCheckIn($db);

        return $cnt;
    }

    //エラーＤＢへの追加
    public function insertQueryErr(&$db, $record_no, $check_error)
    {
        $data1 = array();
        $data1["PROGRAMID"][TEXT] = PROGRAMID;
        $data1["MSGROW"][NUMBER]  = $record_no;
        $data1["MSGREMARK"][TEXT] = $check_error;

        $query = Query::insertSQL($data1, "W_CSVMSG_PRG_DAT");
        $db->query($query);
    }

    /************************/
    /**  ＣＳＶ出力で使用  **/
    /************************/
    //CSVデータ出力
    public function selectMainQuery($model)
    {
        $query  = " SELECT ";
        //PRISCHOOL_MST ";
        $query .= "     PRISCH.PRISCHOOLCD, ";
        $query .= "     PRISCH.PRISCHOOL_NAME, ";
        $query .= "     PRISCH.PRISCHOOL_KANA, ";
        $query .= "     PRISCH.PRISCHOOL_STAFFCD, ";
        $query .= "     S1.STAFFNAME AS PRISCHOOL_STAFF_NAME, ";
        $query .= "     PRISCH.PRINCNAME, ";
        $query .= "     PRISCH.PRINCNAME_SHOW, ";
        $query .= "     PRISCH.PRINCKANA, ";
        $query .= "     PRISCH.DISTRICTCD, ";
        $query .= "     NMZ060.NAME1 AS DISTRICT_NAME, ";
        $query .= "     PRISCH.PRISCHOOL_PREF_CD, ";
        $query .= "     P1.PREF_NAME AS PRISCHOOL_PREF_NAME, ";
        $query .= "     PRISCH.PRISCHOOL_CITY_CD, ";
        $query .= "     C1.CITY_NAME AS PRISCHOOL_CITY_NAME, ";
        $query .= "     PRISCH.PRISCHOOL_ZIPCD, ";
        $query .= "     PRISCH.PRISCHOOL_ADDR1, ";
        $query .= "     PRISCH.PRISCHOOL_ADDR2, ";
        $query .= "     PRISCH.PRISCHOOL_TELNO, ";
        $query .= "     PRISCH.PRISCHOOL_FAXNO, ";
        if ($model->Properties["useMiraicompass"] == '1') {
            $query .= "     M1.MIRAI_PS_CD, ";
        }
        $query .= "     PRISCH.GRP_PRISCHOOLCD, ";
        //PRISCHOOL_CLASS_MST ";
        $query .= "     PCLASS.PRISCHOOLCD           AS C_PRISCHOOLCD, ";
        $query .= "     PCLASS.PRISCHOOL_CLASS_CD    AS C_PRISCHOOL_CLASS_CD, ";
        $query .= "     PCLASS.PRISCHOOL_NAME        AS C_PRISCHOOL_NAME, ";
        $query .= "     PCLASS.PRISCHOOL_KANA        AS C_PRISCHOOL_KANA, ";
        $query .= "     PCLASS.PRINCNAME             AS C_PRINCNAME, ";
        $query .= "     PCLASS.PRINCNAME_SHOW        AS C_PRINCNAME_SHOW, ";
        $query .= "     PCLASS.PRINCKANA             AS C_PRINCKANA, ";
        $query .= "     PCLASS.DISTRICTCD            AS C_DISTRICTCD, ";
        $query .= "     PCLASS.PRISCHOOL_ZIPCD       AS C_PRISCHOOL_ZIPCD, ";
        $query .= "     PCLASS.PRISCHOOL_ADDR1       AS C_PRISCHOOL_ADDR1, ";
        $query .= "     PCLASS.PRISCHOOL_ADDR2       AS C_PRISCHOOL_ADDR2, ";
        $query .= "     PCLASS.PRISCHOOL_TELNO       AS C_PRISCHOOL_TELNO, ";
        $query .= "     PCLASS.PRISCHOOL_FAXNO       AS C_PRISCHOOL_FAXNO, ";
        $query .= "     PCLASS.ROSEN_1               AS C_ROSEN_1, ";
        $query .= "     PCLASS.ROSEN_2               AS C_ROSEN_2, ";
        $query .= "     PCLASS.ROSEN_3               AS C_ROSEN_3, ";
        $query .= "     PCLASS.ROSEN_4               AS C_ROSEN_4, ";
        $query .= "     PCLASS.ROSEN_5               AS C_ROSEN_5, ";
        $query .= "     PCLASS.NEAREST_STATION_NAME1 AS C_NEAREST_STATION_NAME1, ";
        $query .= "     PCLASS.NEAREST_STATION_KANA1 AS C_NEAREST_STATION_KANA1, ";
        $query .= "     PCLASS.NEAREST_STATION_NAME2 AS C_NEAREST_STATION_NAME2, ";
        $query .= "     PCLASS.NEAREST_STATION_KANA2 AS C_NEAREST_STATION_KANA2, ";
        $query .= "     PCLASS.DIRECT_MAIL_FLG       AS C_DIRECT_MAIL_FLG, ";
        $query .= "     '".$model->lastColumn."' AS ".$model->lastColumn." ";
        $query .= " FROM ";
        $query .= "     PRISCHOOL_MST PRISCH ";
        $query .= "     LEFT JOIN PRISCHOOL_CLASS_MST PCLASS ON PRISCH.PRISCHOOLCD = PCLASS.PRISCHOOLCD ";
        $query .= "     LEFT JOIN NAME_MST NMZ060 ON PRISCH.DISTRICTCD = NMZ060.NAMECD2 ";
        $query .= "                              AND NMZ060.NAMECD1 = 'Z060' ";
        $query .= "     LEFT JOIN PREF_MST P1 ON PRISCH.PRISCHOOL_PREF_CD  = P1.PREF_CD ";
        $query .= "     LEFT JOIN CITY_MST C1 ON PRISCH.PRISCHOOL_PREF_CD  = C1.PREF_CD ";
        $query .= "                          AND PRISCH.PRISCHOOL_CITY_CD  = C1.CITY_CD ";
        $query .= "     LEFT JOIN STAFF_MST S1 ON PRISCH.PRISCHOOL_STAFFCD  = S1.STAFFCD ";
        if ($model->Properties["useMiraicompass"] == '1') {
            $query .= "     LEFT JOIN ENTEXAM_MIRAI_PS_REP_DAT M1 ON PRISCH.PRISCHOOLCD = M1.PS_CD ";
        }
        $query .= " ORDER BY ";
        $query .= "     PRISCH.PRISCHOOLCD, ";
        $query .= "     PCLASS.PRISCHOOL_CLASS_CD ";

        return $query;
    }
}
