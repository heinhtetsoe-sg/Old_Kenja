<?php

require_once('for_php7.php');

class knjh020Query extends Query {

    function getRow_himself($model)
    {
           $db = Query::dbCheckOut();

        $query  = " SELECT name_show, birthday FROM schreg_base_mst ";
        $query .= "  WHERE schregno ='".$model->schregno."'";

        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);

        return $row;
    }

    //保護者情報を取得
    function getRow_parents($model)
    {
        if($model->schregno != ""){

            $db = Query::dbCheckOut();

            $query  = " SELECT * ";
            $query .= "   FROM guardian_dat ";
            $query .= "  WHERE schregno = '".$model->schregno."' ";

            $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
            Query::dbCheckIn($db);

        }
        return $row;
    }

    //
    function getGuardian($schregno)
    {
        $query  = " SELECT COUNT(*) ";
        $query .= "   FROM guardian_dat ";
        $query .= "  WHERE schregno = '".$schregno."' ";

        return $query;
    }

    //NAME_MSTから情報を取得
    function getNameMst_data($cd)
    {
        $query  = " SELECT * ";
        $query .= "   FROM v_name_mst ";
        $query .= "  WHERE year ='".CTRL_YEAR."'";
        $query .= "    AND namecd1 ='".$cd."'";

        return $query;
    }

    //
    function getGrdCls()
    {
        $query  = "SELECT DISTINCT ";
        $query .= "       GRADE || ',' || HR_CLASS AS GC, GRADE, HR_CLASS, ";
        $query .= "       HR_NAME AS GC_J ";
#kyan        $query .= "       CHAR(CHAR(INTEGER(GRADE)),2)  || '年' || CHAR(CHAR(INTEGER(HR_CLASS)),3) || '組' AS GC_J ";
#kyan        $query .= " FROM SCHREG_REGD_DAT ";
        $query .= " FROM SCHREG_REGD_HDAT ";
        $query .= " WHERE YEAR = '".$this->year."' ";
        $query .= " ORDER BY GC";

        return $query;
    }

    /**
     * INSERT
     */
    function &getInsertQuery($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $query = "DELETE FROM guardian_dat WHERE schregno = '" .$model->schregno ."'";

        $db->query($query);
        $data["SCHREGNO"][TEXT]                 = $model->schregno;
        $data["RELATIONSHIP"][TEXT]             = $model->field["RELATIONSHIP"];
        $data["GUARD_NAME"][TEXT]               = $model->field["GUARD_NAME"];
        $data["GUARD_KANA"][TEXT]               = $model->field["GUARD_KANA"];
        $data["GUARD_SEX"][TEXT]                = $model->field["GUARD_SEX"];
        if (preg_match("/[0-9]{4}\/[0-9]{2}\/[0-9]{2}/", $model->field["GUARD_BIRTHDAY"])){
            $data["GUARD_BIRTHDAY"][TEXT]           = str_replace("/","-",$model->field["GUARD_BIRTHDAY"]);
        }
        $data["GUARD_ZIPCD"][TEXT]              = $model->field["GUARD_ZIPCD"];
        $data["GUARD_ADDR1"][TEXT]              = $model->field["GUARD_ADDR1"];
        $data["GUARD_ADDR2"][TEXT]              = $model->field["GUARD_ADDR2"];
        $data["GUARD_TELNO"][TEXT]              = $model->field["GUARD_TELNO"];
        $data["GUARD_FAXNO"][TEXT]              = $model->field["GUARD_FAXNO"];
        $data["GUARD_E_MAIL"][TEXT]             = $model->field["GUARD_E_MAIL"];
        $data["GUARD_JOBCD"][TEXT]              = $model->field["GUARD_JOBCD"];
        $data["GUARD_WORK_NAME"][TEXT]          = $model->field["GUARD_WORK_NAME"];
        $data["GUARD_WORK_TELNO"][TEXT]         = $model->field["GUARD_WORK_TELNO"];

        $data["GUARANTOR_RELATIONSHIP"][TEXT]   = $model->field["GUARANTOR_RELATIONSHIP"];
        $data["GUARANTOR_NAME"][TEXT]           = $model->field["GUARANTOR_NAME"];
        $data["GUARANTOR_KANA"][TEXT]           = $model->field["GUARANTOR_KANA"];
        $data["GUARANTOR_SEX"][TEXT]            = $model->field["GUARANTOR_SEX"];
        $data["GUARANTOR_ZIPCD"][TEXT]          = $model->field["GUARANTOR_ZIPCD"];
        $data["GUARANTOR_ADDR1"][TEXT]          = $model->field["GUARANTOR_ADDR1"];
        $data["GUARANTOR_ADDR2"][TEXT]          = $model->field["GUARANTOR_ADDR2"];
        $data["GUARANTOR_TELNO"][TEXT]          = $model->field["GUARANTOR_TELNO"];
        $data["GUARANTOR_JOBCD"][TEXT]          = $model->field["GUARANTOR_JOBCD"];
        $data["PUBLIC_OFFICE"][TEXT]            = $model->field["PUBLIC_OFFICE"];
        $data["REGISTERCD"][TEXT]               = STAFFCD;
        $data["UPDATED"][FUNC]                  = "sysdate()";

        $query = Query::insertSQL($data, "GUARDIAN_DAT");
        $db->query($query);

        $db->commit(); // トランザクションをコミットする。
        Query::dbCheckIn($db);

        return true;
    }

    /**
     * DELETE
     */
    function &getDeleteQuery($cd)
    {
        $db = Query::dbCheckOut();

        $query = "DELETE FROM guardian_dat WHERE schregno = '" .$cd ."'";

        $db->query($query);
        Query::dbCheckIn($db);

        return true;
    }

    //親族データ取得
    function getParentsdata_cnt($schregno)
    {
        $query  = " SELECT COUNT(*) FROM guardian_dat ";
        $query .= " WHERE schregno  = '" .$schregno."' ";

        return $query;
    }
    //親族データ取得
    function get_Parents_data($schregno)
    {
        $query  = " SELECT * FROM schreg_rela_dat ";
        $query .= " WHERE schregno  = '" .$schregno."' ";
        $query .= " ORDER BY INT(relano) ";

        return $query;
    }
}
?>
