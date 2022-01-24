<?php

require_once('for_php7.php');

class knjh090Query extends Query
{
    //名称取得
    public function getName($nm1)
    {
        $query  = " SELECT namecd1,namecd2,name1 FROM v_name_mst ";
        $query .= " WHERE year = '".CTRL_YEAR."' ";
        $query .= " AND namecd1 = '".$nm1."' ";   //,'H013'←資格取得や特技の項目
        $query .= " ORDER BY namecd1,namecd2 ";
        return $query;
    }

    //スタッフ名称取得
    public function getStaffName()
    {
        $query  = " SELECT STAFFCD,STAFFNAME FROM v_staff_mst ";
        $query .= " WHERE year = '".CTRL_YEAR."' ";
        $query .= " ORDER BY STAFFCD ";
        return $query;
    }

    //常盤木チェック情報取得
    public function getTokiwagiChkInfo()
    {
        $query  = " SELECT NAME1 FROM NAME_MST WHERE NAMECD1='Z010' AND NAMECD2='00' ";
        return $query;
    }

    //学籍基礎マスタより学籍番号と名前を取得
    public function getSchregnoName($schregno)
    {
        $query  =" SELECT schregno,name_show FROM schreg_base_mst ";
        $query .=" WHERE schregno = '".$schregno."' ";
        return $query;
    }

    //学籍賞罰データよりデータを取得
    public function getAward($schregno, $tokiwagiflg)
    {
        $query  =" SELECT ";
        $query .="     T1.YEAR, ";
        $query .="     T1.DETAIL_SDATE, ";   //NO001
        $query .="     T1.DETAIL_EDATE, ";   //NO001
        $query .="     T1.DETAIL_DIV, ";     //NO001
        $query .="     case ";
        if (!$tokiwagiflg) {
            $query .="     when T1.DETAIL_DIV = '1' then T1.DETAIL_DIV || '：' || '賞データ' "; //NO001
            $query .="     when T1.DETAIL_DIV = '2' then T1.DETAIL_DIV || '：' || '罰データ' ";      //NO001
        } else {
            $query .="     when T1.DETAIL_DIV = '2' then T1.DETAIL_DIV || '：' || '罰データ' ";
            $query .="     when T1.DETAIL_DIV = '4' then T1.DETAIL_DIV || '：' || '活動の記録' ";
        }
        $query .="     else T1.DETAIL_DIV || '：' || '自転車データ' end as DETAIL_DATA, ";       //NO001
        $query .="     T1.DETAILCD, ";       //NO001
        $query .="     T1.content, ";
        $query .="     T1.remark, ";
        $query .="     T1.BICYCLE_CD, ";
        $query .="     T1.BICYCLE_NO, ";
        $query .="     case ";
        if (!$tokiwagiflg) {
            $query .="     when T1.DETAIL_DIV = '1' then T1.DETAILCD || '：' || meisyou_get(T1.DETAILCD,'H303',1) ";    //NO001
            $query .="     when T1.DETAIL_DIV = '2' then T1.DETAILCD || '：' || meisyou_get(T1.DETAILCD,'H304',1) else '' end as DETAILCD "; //NO001
        } else {
            $query .="     when T1.DETAIL_DIV = '2' then T1.DETAILCD || '：' || meisyou_get(T1.DETAILCD,'H304',1) ";    //NO001
            $query .="     when T1.DETAIL_DIV = '4' then T1.DETAILCD || '：' || meisyou_get(T1.DETAILCD,'H317',1) else '' end as DETAILCD ";
        }
        $query .=" FROM SCHREG_DETAILHIST_DAT T1 "; //NO002
        $query .=" WHERE ";
        if (!$tokiwagiflg) {
            $query .="         T1.DETAIL_DIV < '3' ";    //NO002
        } else {
            $query .="         T1.DETAIL_DIV IN ('2', '4') ";
        }
        $query .="     AND T1.schregno = '".$schregno."' ";
        $query .=" ORDER BY T1.DETAIL_SDATE";    //NO001

        return $query;
    }

    //１レコード取得
    public function getRow($cd1, $cd2, $cd3, $cd4, $year)
    {
        $db = Query::dbCheckOut();

        $query  = " SELECT ";   //NO002
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     SCHREG_DETAILHIST_DAT ";
        $query .= " WHERE ";
        $query .= "         YEAR         = '".$year."' ";
        $query .= "     AND SCHREGNO     = '".$cd3. "' ";
        $query .= "     AND DETAIL_SDATE = '".strtr($cd2, "/", "-")."' ";  //NO001
        $query .= "     AND DETAIL_DIV   = '".$cd4."' ";   //NO001

        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);
        return $row;
    }

    // INSERT
    public function &getInsertQuery($fields, $schregno)
    {
        $db = Query::dbCheckOut();

        $data["YEAR"][TEXT]           = CTRL_YEAR;
        $data["SCHREGNO"][TEXT]       = $schregno;
        $data["DETAIL_DIV"][TEXT]     = $fields["DETAIL_DIV"];
        $data["DETAIL_SDATE"][TEXT]   = strtr($fields["DETAIL_SDATE"], "/", "-"); //NO001
        $data["DETAIL_EDATE"][TEXT]   = strtr($fields["DETAIL_EDATE"], "/", "-"); //NO001
        $data["DETAILCD"][TEXT]       = ($fields["DETAILCD"]=="00") ? null : $fields["DETAILCD"];   //NO001
        $data["CONTENT"][TEXT]        = $fields["CONTENT"];
        $data["REMARK"][TEXT]         = $fields["REMARK"];
        $data["BICYCLE_CD"][TEXT]     = (!$fields["BICYCLE_CD"]) ? null : substr("00000000".$fields["BICYCLE_CD"], strlen($fields["BICYCLE_CD"]), 8);
        $data["BICYCLE_NO"][TEXT]     = (!$fields["BICYCLE_NO"]) ? null : $fields["BICYCLE_NO"];
        $data["REGISTERCD"][TEXT]     = STAFFCD;
        $data["UPDATED"][NUMBER]      = "sysdate()";
        $data["OCCURRENCE_DATE"][TEXT]        = strtr($fields["OCCURRENCE_DATE"], "/", "-");
        $data["INVESTIGATION_DATE"][TEXT]     = strtr($fields["INVESTIGATION_DATE"], "/", "-");
        $data["STD_GUID_MTG_DATE"][TEXT]      = strtr($fields["STD_GUID_MTG_DATE"], "/", "-");
        $data["ORIGINAL_PLAN_CD"][TEXT]       = $fields["ORIGINAL_PLAN_CD"];
        $data["STAFF_MTG_DATE"][TEXT]         = strtr($fields["STAFF_MTG_DATE"], "/", "-");
        $data["PUNISH_CD"][TEXT]              = $fields["PUNISH_CD"];
        $data["OCCURRENCE_PLACE"][TEXT]       = $fields["OCCURRENCE_PLACE"];
        $data["DIARY_FLG"][TEXT]              = $fields["DIARY_FLG"];
        $data["WRITTEN_OATH_FLG"][TEXT]       = $fields["WRITTEN_OATH_FLG"];
        $data["REPORT_FLG"][TEXT]             = $fields["REPORT_FLG"];
        $data["WRITTEN_STAFFCD"][TEXT]        = $fields["WRITTEN_STAFFCD"];
        $data["INVESTIGATION_STAFFCD1"][TEXT] = $fields["INVESTIGATION_STAFFCD1"];
        $data["INVESTIGATION_STAFFCD2"][TEXT] = $fields["INVESTIGATION_STAFFCD2"];
        $data["INVESTIGATION_STAFFCD3"][TEXT] = $fields["INVESTIGATION_STAFFCD3"];
        $data["INVESTIGATION_STAFFCD4"][TEXT] = $fields["INVESTIGATION_STAFFCD4"];

        $query = Query::insertSQL($data, "SCHREG_DETAILHIST_DAT");  //NO002
        $db->query($query);
        Query::dbCheckIn($db);
        return;
    }

    //UPDATE
    public function &getUpdateQuery($fields, $schregno, $org_detail_sdate, $year)
    {   //NO001
        $db = Query::dbCheckOut();

        $data["DETAIL_EDATE"][TEXT]   = strtr($fields["DETAIL_EDATE"], "/", "-"); //NO001
        $data["DETAILCD"][TEXT]       = ($fields["DETAILCD"]=="00") ? null : $fields["DETAILCD"];   //NO001
        $data["CONTENT"][TEXT]        = $fields["CONTENT"];
        $data["REMARK"][TEXT]         = $fields["REMARK"];
        $data["BICYCLE_CD"][TEXT]     = (!$fields["BICYCLE_CD"]) ? null : substr("00000000".$fields["BICYCLE_CD"], strlen($fields["BICYCLE_CD"]), 8);
        $data["BICYCLE_NO"][TEXT]     = (!$fields["BICYCLE_NO"]) ? null : $fields["BICYCLE_NO"];
        $data["REGISTERCD"][TEXT]     = STAFFCD;
        $data["UPDATED"][NUMBER]      = "sysdate()";
        $data["OCCURRENCE_DATE"][TEXT]        = strtr($fields["OCCURRENCE_DATE"], "/", "-");
        $data["INVESTIGATION_DATE"][TEXT]     = strtr($fields["INVESTIGATION_DATE"], "/", "-");
        $data["STD_GUID_MTG_DATE"][TEXT]      = strtr($fields["STD_GUID_MTG_DATE"], "/", "-");
        $data["ORIGINAL_PLAN_CD"][TEXT]       = $fields["ORIGINAL_PLAN_CD"];
        $data["STAFF_MTG_DATE"][TEXT]         = strtr($fields["STAFF_MTG_DATE"], "/", "-");
        $data["PUNISH_CD"][TEXT]              = $fields["PUNISH_CD"];
        $data["OCCURRENCE_PLACE"][TEXT]       = $fields["OCCURRENCE_PLACE"];
        $data["DIARY_FLG"][TEXT]              = $fields["DIARY_FLG"];
        $data["WRITTEN_OATH_FLG"][TEXT]       = $fields["WRITTEN_OATH_FLG"];
        $data["REPORT_FLG"][TEXT]             = $fields["REPORT_FLG"];
        $data["WRITTEN_STAFFCD"][TEXT]        = $fields["WRITTEN_STAFFCD"];
        $data["INVESTIGATION_STAFFCD1"][TEXT] = $fields["INVESTIGATION_STAFFCD1"];
        $data["INVESTIGATION_STAFFCD2"][TEXT] = $fields["INVESTIGATION_STAFFCD2"];
        $data["INVESTIGATION_STAFFCD3"][TEXT] = $fields["INVESTIGATION_STAFFCD3"];
        $data["INVESTIGATION_STAFFCD4"][TEXT] = $fields["INVESTIGATION_STAFFCD4"];

        $where  = " WHERE YEAR        = '" .$year. "'";
        $where .= " AND SCHREGNO      = '" .$schregno. "'";
        $where .= " AND DETAIL_DIV    = '" .$fields["DETAIL_DIV"]. "'";
        $where .= " AND DETAIL_SDATE  = '" .strtr($org_detail_sdate, "/", "-")."'";   //NO001

        $query = Query::updateSQL($data, "SCHREG_DETAILHIST_DAT", $where);  //NO002
        $db->query($query);
        Query::dbCheckIn($db);
        return ;
    }

    // DELETE
    public function &getDeleteQuery($fields, $schregno, $year)
    {
        $db = Query::dbCheckOut();

        $query  = " DELETE FROM SCHREG_DETAILHIST_DAT ";    //NO002
        $query .= " WHERE ";
        $query .= "     YEAR         = '" .$year. "'";
        $query .= " AND SCHREGNO     = '" .$schregno. "'";
        $query .= " AND DETAIL_DIV   = '".$fields["DETAIL_DIV"]."'"; //NO001
        $query .= " AND DETAIL_SDATE = '".strtr($fields["DETAIL_SDATE"], "/", "-")."' ";  //NO001

        $db->query($query);
        Query::dbCheckIn($db);
        return $result;
    }
}
