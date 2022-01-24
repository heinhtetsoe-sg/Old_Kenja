<?php

require_once('for_php7.php');

class knjl210cQuery extends Query 
{

    //最後の受験番号＋１を取得
    function getMaxExamnoQuery($model)
    {
        $db = Query::dbCheckOut();
        $query  = " SELECT  VALUE(MAX(SMALLINT(PRE_RECEPTNO)), 0) + 1 AS PRE_RECEPTNO ";
        $query .= " FROM    ENTEXAM_APPLICANTBASE_PRE_DAT ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR = '{$model->year}' ";
        $query .= "     AND PRE_RECEPTNO LIKE '{$model->L104nmsp1[$model->preTestdiv]}%' ";

        $max_receptno = $db->getOne($query);
        Query::dbCheckIn($db);
        return $max_receptno;
    }

    //入試制度を取得
    function getApplicantdiv($model) {
        $query  = " WITH DATA(VALUE, LABEL) AS ( ";
        $query .= "     VALUES('1','1:中学') ";
        $query .= " ) ";

        $query .= " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     DATA ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //性別を取得
    function getSex($model) {
        $query  = " SELECT ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$model->year}' AND ";
        $query .= "     NAMECD1 = 'Z002' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";
        return $query;
    }

    //出身学校
    function getFinschoolName($cd)
    {
        $query .= " SELECT ";
        $query .= "     FINSCHOOL_NAME ";
        $query .= " FROM ";
        $query .= "     FINSCHOOL_MST ";
        $query .= " WHERE ";
        $query .= "     FINSCHOOLCD = '{$cd}' ";

        return $query;
    }
    //出身学校マスタよりデータを取得
    function getFinschoolcd($year, $finschoolcd)
    {
        $query  = " SELECT  T1.FINSCHOOLCD, T1.FINSCHOOL_NAME ";
        $query .= "   FROM  FINSCHOOL_YDAT T2, ";
        $query .= "         FINSCHOOL_MST T1 ";
        $query .= "  WHERE  T2.YEAR = '{$year}' ";
        $query .= "    AND  T2.FINSCHOOLCD = '{$finschoolcd}' ";
        $query .= "    AND  T2.FINSCHOOLCD = T1.FINSCHOOLCD ";
        $query .= " ORDER BY T1.FINSCHOOLCD ";
        return $query;
    }

    //塾マスタよりデータを取得
    function getPrischoolcd($year, $prischoolcd)
    {
        $query  = " SELECT  T1.PRISCHOOLCD, T1.PRISCHOOL_NAME ";
        $query .= "   FROM  PRISCHOOL_MST T1, ";
        $query .= "         PRISCHOOL_YDAT T2 ";
        $query .= "  WHERE  T2.YEAR = '{$year}' ";
        $query .= "    AND  T2.PRISCHOOLCD = '{$prischoolcd}' ";
        $query .= "    AND  T2.PRISCHOOLCD = T1.PRISCHOOLCD ";
        $query .= " ORDER BY T1.PRISCHOOLCD ";
        return $query;
    }

    //名称マスタよりデータを取得
    function getVnameMst($year, $namecd1)
    {
        $query .= " SELECT ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL, ";
        $query .= "     NAME1, ";
        $query .= "     NAMESPARE1 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$year}' ";
        $query .= "     AND NAMECD1 = '{$namecd1}' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //メインデータ取得
    function get_edit_data($model)
    {
        $db = Query::dbCheckOut();

        $query  = " SELECT ";
        $query .= "     APPLICANTDIV, ";
        $query .= "     PRE_RECEPTNO, ";
        $query .= "     PRE_TESTDIV, ";
        $query .= "     PRE_EXAM_TYPE, ";
        $query .= "     PRE_RECEPTDIV, ";
        $query .= "     PRE_RECEPTDATE, ";
        $query .= "     NAME, ";
        $query .= "     NAME_KANA, ";
        $query .= "     SEX, ";
        $query .= "     GNAME, ";
        $query .= "     GKANA, ";
        $query .= "     ZIPCD, ";
        $query .= "     ADDRESS1, ";
        $query .= "     ADDRESS2, ";
        $query .= "     TELNO, ";
        $query .= "     FS_CD, ";
        $query .= "     PS_CD, ";
        $query .= "     PS_CONTACT, ";
        $query .= "     BUS_USE, ";
        $query .= "     STATIONDIV, ";
        $query .= "     BUS_USER_COUNT, ";
        $query .= "     RECOM_EXAMNO, ";
        $query .= "     REMARK ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_PRE_DAT ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR = '{$model->year}' AND ";

        if ($model->cmd == "back" || $model->cmd == "back1") {
            $query.= " PRE_RECEPTNO < '".$model->receptno."'";
            $query.= " ORDER BY ";
            $query.= "     PRE_RECEPTNO DESC ";
        } elseif ($model->cmd == "next" || $model->cmd == "next1") {
            $query.= " PRE_RECEPTNO > '".$model->receptno."'";
            $query.= " ORDER BY ";
            $query.= "     PRE_RECEPTNO ASC ";
        } else {
            $query.= " PRE_RECEPTNO = '".$model->receptno."'";
        }

        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);

        return $row;
    }

    //存在チェック
    function Existdata($model)
    {
        $db = Query::dbCheckOut();

        $query = "  SELECT
                        PRE_RECEPTNO
                    FROM
                        ENTEXAM_APPLICANTBASE_PRE_DAT
                    WHERE
                        ENTEXAMYEAR = '".$model->year."' AND     
                        PRE_RECEPTNO = '".$model->receptno."'";

        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);

        return $row;
    }

    //存在チェック
    function ExistdataNoAndName($model)
    {
        $db = Query::dbCheckOut();

        $query .= " SELECT ";
        $query .= "     PRE_RECEPTNO ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_PRE_DAT ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR = '{$model->year}' ";
        $query .= "     AND PRE_RECEPTNO = '{$model->field["RECOM_EXAMNO"]}' ";
        $query .= "     AND NAME = '{$model->field["NAME"]}' ";

        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);

        return $row;
    }


    //--- INSERT
    //--- UPDATE
    function &getInsertUpdateQuery($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $data = array();
        $data["ENTEXAMYEAR"][TEXT]      = $model->year;
        $data["APPLICANTDIV"][TEXT]     = $model->field["APPLICANTDIV"];
        $data["PRE_RECEPTNO"][TEXT]     = $model->receptno;
        $data["PRE_TESTDIV"][TEXT]      = $model->preTestdiv;
        $data["PRE_EXAM_TYPE"][TEXT]    = $model->field["PRE_EXAM_TYPE"];
        $data["PRE_RECEPTDIV"][TEXT]    = $model->field["PRE_RECEPTDIV"];
        $data["PRE_RECEPTDATE"][DATE]   = $model->field["PRE_RECEPTDATE"];
        $data["NAME"][TEXT]             = $model->field["NAME"];
        $data["NAME_KANA"][TEXT]        = $model->field["NAME_KANA"];
        $data["SEX"][TEXT]              = $model->field["SEX"];
        $data["GNAME"][TEXT]            = $model->field["GNAME"];
        $data["GKANA"][TEXT]            = $model->field["GKANA"];
        $data["ZIPCD"][TEXT]            = $model->field["ZIPCD"];
        $data["ADDRESS1"][TEXT]         = $model->field["ADDRESS1"];
        $data["ADDRESS2"][TEXT]         = $model->field["ADDRESS2"];
        $data["TELNO"][TEXT]            = $model->field["TELNO"];
        $data["FS_CD"][TEXT]            = $model->field["FS_CD"];
        $data["PS_CD"][TEXT]            = $model->field["PS_CD"];
        $data["PS_CONTACT"][TEXT]       = $model->field["PS_CONTACT"];
        if ($model->Properties["Pretest_bus_Not_Hyouji"] != "1") {
            $data["BUS_USE"][TEXT]          = $model->field["BUS_USE"];
            $data["STATIONDIV"][TEXT]       = strlen($model->field["BUS_USE"]) ? $model->field["STATIONDIV"] : "";
            $data["BUS_USER_COUNT"][NUMBER] = strlen($model->field["BUS_USE"]) ? $model->field["BUS_USER_COUNT"] : "";
        }
        $data["REMARK"][TEXT]           = $model->field["REMARK"];
        $data["RECOM_EXAMNO"][TEXT]     = $model->field["RECOM_EXAMNO"];
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][FUNC]          = "sysdate()";

        $where  = " WHERE ENTEXAMYEAR   = '".$model->year."' AND";
        $where .= "       PRE_RECEPTNO  = '".$model->receptno."'";

        if ($this->cmd != "add") {
            $query = Query::updateSQL($data, "ENTEXAM_APPLICANTBASE_PRE_DAT", $where);
        } else {
            $query = Query::insertSQL($data, "ENTEXAM_APPLICANTBASE_PRE_DAT");
        }
        $db->query($query);

        $db->commit();
        Query::dbCheckIn($db);
        return;
    }

    //--- DELETE
    function &getDeleteQuery($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $query  = " DELETE FROM ENTEXAM_APPLICANTBASE_PRE_DAT ";
        $query .= " WHERE ENTEXAMYEAR   = '".$model->year."' AND";
        $query .= "       PRE_RECEPTNO  = '".$model->receptno."'";
        $db->query($query);

        $db->commit();
        Query::dbCheckIn($db);
        return;
    }

}
?>