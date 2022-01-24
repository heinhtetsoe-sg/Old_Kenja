<?php

require_once('for_php7.php');

class knjz250_2Query extends Query {

    //１レコード取得
    function getRow($cd)
    {
        $db = Query::dbCheckOut();

        $query = "SELECT * FROM certif_kind_mst ".
                 " WHERE certif_kindcd = '" .$cd ."'";

        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);
        return $row;
    }
    //事務発行区分
    function getIssue()
    {
        return $query = "SELECT * FROM name_mst WHERE namecd1 = '" ."G001" ."'";    
    }
    //発行対象区分
    function getStudent()
    {
        return $query = "SELECT * FROM name_mst WHERE namecd1 = '" ."G002" ."'";  
    }

    //証明書種類コード一覧取得
    function getCertifKindcd($certif_kindcd) {
        $query  = " WITH MY_DATA (CERTIF_KINDCD) AS (VALUES('".$certif_kindcd."')) ";
        $query .= " SELECT ";
        $query .= "     CERTIF_KINDCD ";
        $query .= " FROM ";
        $query .= "     CERTIF_KIND_MST ";
        $query .= " UNION  ";
        $query .= " SELECT ";
        $query .= "     CERTIF_KINDCD ";
        $query .= " FROM ";
        $query .= "     MY_DATA ";

        return $query;
    }

    function getCertifKindMst($model) {
        $query  = "SELECT T1.* ";
        if ($model->Properties["use_certif_kind_mst_school_kind"] == "1") {
            $query .= "    , T2.ABBV1 AS CERTIF_SCHOOL_KIND_NAME ";
        }
        $query .= " FROM CERTIF_KIND_MST T1 ";
        if ($model->Properties["use_certif_kind_mst_school_kind"] == "1") {
            $query .= " LEFT JOIN NAME_MST T2 ON T2.NAMECD1 = 'A023' AND T2.NAME1 = T1.CERTIF_SCHOOL_KIND ";
        }
        $query .= " ORDER BY T1.CERTIF_KINDCD";
        return $query;
    }

    //INSERT
    function &getInsertQuery($model, $fields)
    {
        $db = Query::dbCheckOut();

        $data = array();
        $data["CERTIF_KINDCD"][TEXT]     = $fields["CERTIF_KINDCD"];
        $data["KINDNAME"][TEXT]          = $fields["KINDNAME"];
        $data["ISSUECD"][TEXT]           = $fields["ISSUECD"];
        $data["STUDENTCD"][TEXT]         = $fields["STUDENTCD"];
        $data["GRADUATECD"][TEXT]        = $fields["GRADUATECD"];
        $data["DROPOUTCD"][TEXT]         = $fields["DROPOUTCD"];
        $data["ELAPSED_YEARS"][NUMBER]   = $fields["ELAPSED_YEARS"];
        if ($model->Properties["certif_no_8keta"] == "1") {
            $data["CERTIF_DIV"][TEXT]       = $fields["CERTIF_DIV"];
            $data["CERTIF_GRPCD"][TEXT]     = "";
        }
        $data["CURRENT_PRICE"][TEXT]     = $fields["CURRENT_PRICE"];
        $data["GRADUATED_PRICE"][TEXT]   = $fields["GRADUATED_PRICE"];
        $data["ISSUENO_AUTOFLG"][TEXT]   = $fields["ISSUENO_AUTOFLG"];
        if ($model->Properties["use_certif_kind_mst_school_kind"] == "1") {
            $data["CERTIF_SCHOOL_KIND"][TEXT]   = $fields["CERTIF_SCHOOL_KIND"];
        }
        $data["REGISTERCD"][TEXT]        = STAFFCD;
        $data["UPDATED"][NUMBER]         = "sysdate()";

        $query = Query::insertSQL($data, "certif_kind_mst");
        $db->query($query);
        Query::dbCheckIn($db);
        return;
    }
    //UPDATE
    function &getUpdateQuery($model, $fields)
    {
        $db = Query::dbCheckOut();

        $data = array();
        $data["CERTIF_KINDCD"][TEXT]     = $fields["CERTIF_KINDCD"];
        $data["KINDNAME"][TEXT]          = $fields["KINDNAME"];
        $data["ISSUECD"][TEXT]           = $fields["ISSUECD"];
        $data["STUDENTCD"][TEXT]         = $fields["STUDENTCD"];
        $data["GRADUATECD"][TEXT]        = $fields["GRADUATECD"];
        $data["DROPOUTCD"][TEXT]         = $fields["DROPOUTCD"];
        $data["ELAPSED_YEARS"][NUMBER]   = $fields["ELAPSED_YEARS"];
        if ($model->Properties["certif_no_8keta"] == "1") {
            $data["CERTIF_DIV"][TEXT]       = $fields["CERTIF_DIV"];
            $data["CERTIF_GRPCD"][TEXT]     = "";
        }
        $data["CURRENT_PRICE"][TEXT]     = $fields["CURRENT_PRICE"];
        $data["GRADUATED_PRICE"][TEXT]   = $fields["GRADUATED_PRICE"];
        $data["ISSUENO_AUTOFLG"][TEXT]   = $fields["ISSUENO_AUTOFLG"];
        if ($model->Properties["use_certif_kind_mst_school_kind"] == "1") {
            $data["CERTIF_SCHOOL_KIND"][TEXT]   = $fields["CERTIF_SCHOOL_KIND"];
        }
        $data["REGISTERCD"][TEXT]        = STAFFCD;
        $data["UPDATED"][NUMBER]         = "sysdate()";

        $where = " WHERE certif_kindcd    = '" .$fields["CERTIF_KINDCD"]."'";
        $query = Query::updateSQL($data, "certif_kind_mst", $where);
        $db->query($query);
        Query::dbCheckIn($db);
        return;
    }
    //DELETE
    function &getDeleteQuery($fields)
    {
        if (knjz250_2Query::year_data_chk($fields["CERTIF_KINDCD"])==0) {
            $db = Query::dbCheckOut();

            $query = "DELETE FROM certif_kind_mst "
                    ." WHERE certif_kindcd  = '".$fields["CERTIF_KINDCD"] ."'";

            $db->query($query);
            Query::dbCheckIn($db);
            $result = true;
        } else {
            $result = false;
        }   
        return $result;
    }

    function year_data_chk($cd)
    {
        $db = Query::dbCheckOut();
        $query = "SELECT COUNT(*) AS CNT FROM certif_kind_ydat WHERE certif_kindcd = '" .$cd ."'";
        $db->query($query);

        $cnt = $db->getone($query);
        Query::dbCheckIn($db);
        return $cnt;
    }

    function getCertifDivCd()
    {
        $query  = " SELECT * FROM CERTIF_KIND_DIV_MST ORDER BY CERTIF_DIV ";

        return $query;
    }

    //校種
    function getA023()
    {
        return " SELECT NAME1 AS VALUE, NAME1 || ' ' || ABBV1 AS LABEL FROM NAME_MST WHERE NAMECD1 = 'A023' ORDER BY INT(ABBV3), NAMECD2 ";    
    }
}
?>
