<?php

require_once('for_php7.php');

class knjh155Query extends Query {

        //名称取得
        function getName($nm1)
        {
                $query  = "SELECT namecd1,namecd2,name1 FROM v_name_mst ";
                $query .= " WHERE year = '".CTRL_YEAR."'";
                $query .= " AND namecd1 = '".$nm1."' ";
                $query .= "ORDER BY namecd1,namecd2";
                return $query;
        }

        //学籍基礎マスタより学籍番号と名前を取得
        function getSchregno_name($schregno)
        {
                $query  ="SELECT schregno,name_show FROM schreg_base_mst ";
                $query .=" WHERE schregno = '".$schregno."'";
                return $query;
        }

        //学籍賞罰データよりデータを取得
        function getAward($schregno)
        {

                $query  ="SELECT ";
                $query .="    T1.DETAIL_SDATE, ";
                $query .="    T1.DETAIL_EDATE, ";
                $query .="    T1.DETAIL_DIV, ";
                $query .="    T1.DETAIL_DIV || '：' || '自転車データ' AS DETAIL_DATA, ";
                $query .="    T1.CONTENT, ";
                $query .="    T1.REMARK, ";
                $query .="    T1.BICYCLE_CD, ";
                $query .="    T1.BICYCLE_NO ";
                $query .="FROM  SCHREG_DETAILHIST_DAT T1 ";	//NO001
                $query .="WHERE ";
                $query .="    T1.DETAIL_DIV     = '3'";
                $query .="    AND T1.SCHREGNO = '".$schregno."' ";
                $query .="ORDER BY T1.DETAIL_SDATE";

                return $query;
        }

        //１レコード取得
        function getRow($cd1,$cd2,$cd3,$cd4)
        {
                $db = Query::dbCheckOut();

                $query  = "SELECT * FROM SCHREG_DETAILHIST_DAT ";
                $query .= " WHERE year = Fiscalyear(DETAIL_SDATE) ";
                $query .= " AND  schregno= '".$cd3. "'";
                $query .= " AND DETAIL_SDATE = '".strtr($cd2,"/","-")."'";
                $query .= " AND DETAIL_DIV = '".$cd4."'";
                $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
                Query::dbCheckIn($db);
                return $row;
        }

        // INSERT
        function &getInsertQuery($fields,$schregno)
        {
                $db = Query::dbCheckOut();
                list($setY, $setM, $setD) = preg_split("/\//", $fields["DETAIL_SDATE"]);
                $setYear = $setM > "03" ? $setY : $setY - 1;

                $data["YEAR"][TEXT]           = $setYear;
                $data["SCHREGNO"][TEXT]       = $schregno;
                $data["DETAIL_DIV"][TEXT]     = $fields["DETAIL_DIV"];
                $data["DETAIL_SDATE"][TEXT]   = strtr($fields["DETAIL_SDATE"],"/","-");
                $data["DETAIL_EDATE"][TEXT]   = strtr($fields["DETAIL_EDATE"],"/","-");
                $data["DETAILCD"][TEXT]       = null;
                $data["CONTENT"][TEXT]        = $fields["CONTENT"];
                $data["REMARK"][TEXT]         = $fields["REMARK"];
                $data["BICYCLE_CD"][TEXT]     = (!$fields["BICYCLE_CD"]) ? null : $fields["BICYCLE_CD"];
                $data["BICYCLE_NO"][TEXT]     = (!$fields["BICYCLE_NO"]) ? null : $fields["BICYCLE_NO"];
                $data["REGISTERCD"][TEXT]     = STAFFCD;
                $data["UPDATED"][NUMBER]      = "sysdate()";

                $query = Query::insertSQL($data, "SCHREG_DETAILHIST_DAT");	//NO001
                $db->query($query);
                Query::dbCheckIn($db);
                return;
        }

        //UPDATE
        function &getUpdateQuery($fields,$schregno,$org_detail_sdate)
        {
                $db = Query::dbCheckOut();
                
                $data["DETAIL_EDATE"][TEXT]   = strtr($fields["DETAIL_EDATE"],"/","-");
                $data["DETAILCD"][TEXT]       = null;
                $data["CONTENT"][TEXT]        = $fields["CONTENT"];
                $data["REMARK"][TEXT]         = $fields["REMARK"];
                $data["BICYCLE_CD"][TEXT]     = (!$fields["BICYCLE_CD"]) ? null : $fields["BICYCLE_CD"];
                $data["BICYCLE_NO"][TEXT]     = (!$fields["BICYCLE_NO"]) ? null : $fields["BICYCLE_NO"];
                $data["REGISTERCD"][TEXT]     = STAFFCD;
                $data["UPDATED"][NUMBER]      = "sysdate()";

                $where  = " WHERE SCHREGNO = '" .$schregno. "'";
                $where .= " AND DETAIL_DIV = '" .$fields["DETAIL_DIV"]. "'";
                $where .= " AND DETAIL_SDATE = '" .strtr($org_detail_sdate,"/","-")."'";
                $where .= " AND YEAR = Fiscalyear(DETAIL_SDATE) ";

                $query = Query::updateSQL($data, "SCHREG_DETAILHIST_DAT", $where);	//NO001
                $db->query($query);
                Query::dbCheckIn($db);

                return ;
        }

        // DELETE
        function &getDeleteQuery($fields,$schregno)
        {
                $db = Query::dbCheckOut();

                $query  = " DELETE FROM SCHREG_DETAILHIST_DAT ";	//NO001
                $query .= " WHERE ";
                $query .= " SCHREGNO = '" .$schregno. "'";
                $query .= " AND DETAIL_DIV  = '".$fields["DETAIL_DIV"]."'";
                $query .= " AND DETAIL_SDATE  = '".strtr($fields["DETAIL_SDATE"],"/","-")."'";
                $query .= " AND YEAR = Fiscalyear(DETAIL_SDATE) ";

                $db->query($query);
                Query::dbCheckIn($db);
                return $result;
        }
}
?>
