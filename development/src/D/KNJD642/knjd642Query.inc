<?php

require_once('for_php7.php');

class knjd642Query extends Query {

    //年度一覧取得
    function selectYearQuery()
    {
        $query  = " SELECT DISTINCT YEAR ";
        $query .= " FROM COLLEGE_RECOMMENDATION_DAT ";
        $query .= " ORDER BY YEAR DESC";
        return $query;
    }

    //現在の年度のデータが無くなった時に次年度以降の値を取得
    function DeleteAtExist($model)
    {
        $query  = " SELECT MIN(YEAR) ";
        $query .= " FROM COLLEGE_RECOMMENDATION_DAT ";
        $query .= " WHERE YEAR >= '" .$model->year. "'";
        return $query;
    }

    //学校コード取得
    function getSchoolcdQuery()
    {
        $query  = " SELECT ABBV3 ";
        $query .= " FROM NAME_MST ";
        $query .= " WHERE NAMECD1 = 'Z010' AND ";
        $query .= "       NAMECD2 = '00' ";
        return $query;
    }


    //データ取得
    function selectQuery($year)
    {
        $query  = "";
        $query .= "  SELECT  ";
        $query .= "      * ";
        $query .= "  FROM";
        $query .= "      COLLEGE_RECOMMENDATION_DAT ";
        $query .= "  WHERE";
        $query .= "      YEAR = '".$year."' ";
        $query .= "  ORDER BY";
        $query .= "      BU_CD,";
        $query .= "      KA_CD";
        return $query;
    }

    //１レコード取得
    function getRow($year,$bu_cd,$ka_cd)
    {
        $db = Query::dbCheckOut();

        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     COLLEGE_RECOMMENDATION_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR      = '".$year."' AND";
        $query .= "     BU_CD     = '".$bu_cd."' AND ";
        $query .= "     KA_CD     = '".$ka_cd."'";

        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);
        return $row;
    }

    //INSERT
    function &getInsertQuery($fields)
    {
        $db = Query::dbCheckOut();

        $school_cd = $db->getOne(knjd642Query::getSchoolcdQuery());
        $data["YEAR"][TEXT]             = $fields["YEAR"];
        $data["SCHOOL_CD"][TEXT]        = $school_cd;
        $data["BU_CD"][TEXT]            = $fields["BU_CD"];
        $data["KA_CD"][TEXT]            = $fields["KA_CD"];
        $data["BU_NAME"][TEXT]          = $fields["BU_NAME"];
        $data["BU_ABBV"][TEXT]          = $fields["BU_ABBV"];
        $data["KA_NAME"][TEXT]          = $fields["KA_NAME"];
        $data["KA_ABBV"][TEXT]          = $fields["KA_ABBV"];
        $data["KA_MARK"][TEXT]          = $fields["KA_MARK"];
        $data["DIV"][TEXT]              = $fields["DIV"];
        $data["FRAME"][NUMBER]            = $fields["FRAME"];
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][FUNC]          = "sysdate()";

        $query = Query::insertSQL($data, "COLLEGE_RECOMMENDATION_DAT");

        $db->query($query);
        Query::dbCheckIn($db);
        return;
    }

    //UPDATE
    function &getUpdateQuery($fields)
    {
        $db = Query::dbCheckOut();

        $data["BU_CD"][TEXT]            = $fields["BU_CD"];
        $data["KA_CD"][TEXT]            = $fields["KA_CD"];
        $data["BU_NAME"][TEXT]          = $fields["BU_NAME"];
        $data["BU_ABBV"][TEXT]          = $fields["BU_ABBV"];
        $data["KA_NAME"][TEXT]          = $fields["KA_NAME"];
        $data["KA_ABBV"][TEXT]          = $fields["KA_ABBV"];
        $data["KA_MARK"][TEXT]          = $fields["KA_MARK"];
        $data["DIV"][TEXT]              = $fields["DIV"];
        $data["FRAME"][NUMBER]            = $fields["FRAME"];
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][FUNC]          = "sysdate()";
        
        $where  = " WHERE year          = '".$fields["YEAR"]."'";
        $where .= "   AND bu_cd         = '".$fields["BU_CD"]."'";
        $where .= "   AND ka_cd         = '".$fields["KA_CD"]."'";

        $query = Query::updateSQL($data, "COLLEGE_RECOMMENDATION_DAT", $where);

        $db->query($query);
        Query::dbCheckIn($db);
        return;
    }

    //DELETE
    function &getDeleteQuery($fields)
    {
        $db = Query::dbCheckOut();

        $query  = " DELETE FROM college_recommendation_dat ";
        $query .= "       WHERE year        = '".$fields["YEAR"] ."'";
        $query .= "         AND bu_cd       = '".$fields["BU_CD"]."'";
        $query .= "         AND ka_cd       = '".$fields["KA_CD"]."'";

        $db->query($query);
        Query::dbCheckIn($db);
        
        return $result;
    }


    //次年度のデータを作成
    function &getCopyYearQuery($model)
    {
        $flg    = false;
        $db = Query::dbCheckOut();

        $query  = "";
        $query .= "  SELECT";
        $query .= "      *";
        $query .= "  FROM";
        $query .= "      COLLEGE_RECOMMENDATION_DAT T1";
        $query .= "  WHERE";
        $query .= "      YEAR = '".$model->year."' AND";
        $query .= "      NOT EXISTS (SELECT 'X'";
        $query .= "                    FROM COLLEGE_RECOMMENDATION_DAT W1";
        $query .= "                   WHERE W1.YEAR      = '".$model->NextYear."' AND";
        $query .= "                         W1.BU_CD     = T1.BU_CD AND";
        $query .= "                         W1.KA_CD     = T1.KA_CD";
        $query .= "                 )";

        //次年度作成できるデータが存在すれば追加
        if (is_array($db->getRow($query, DB_FETCHMODE_ASSOC))) {
            $flg = true;

            $db->autoCommit(false);
            $query  = "";
            $query .= " INSERT INTO COLLEGE_RECOMMENDATION_DAT";
            $query .= " (";
            $query .= "     YEAR,";
            $query .= "     SCHOOL_CD,";
            $query .= "     BU_CD,";
            $query .= "     KA_CD,";
            $query .= "     BU_NAME,";
            $query .= "     BU_ABBV,";
            $query .= "     KA_NAME,";
            $query .= "     KA_ABBV,";
            $query .= "     KA_MARK,";
            $query .= "     DIV,";
            $query .= "     FRAME,";
            $query .= "     REGISTERCD,UPDATED";
            $query .= " )";
            $query .= "  SELECT";
            $query .= "      '$model->NextYear',";
            $query .= "      SCHOOL_CD,";
            $query .= "      BU_CD,";
            $query .= "      KA_CD,";
            $query .= "      BU_NAME,";
            $query .= "      BU_ABBV,";
            $query .= "      KA_NAME,";
            $query .= "      KA_ABBV,";
            $query .= "      KA_MARK,";
            $query .= "      DIV,";
            $query .= "      FRAME,";
            $query .= "      '".STAFFCD."', sysdate()";
            $query .= "  FROM";
            $query .= "      COLLEGE_RECOMMENDATION_DAT T1";
            $query .= "  WHERE";
            $query .= "      YEAR = '".$model->year."' AND";
            $query .= "      NOT EXISTS (SELECT 'X'";
            $query .= "                    FROM COLLEGE_RECOMMENDATION_DAT W1";
            $query .= "                   WHERE W1.YEAR      = '".$model->NextYear."' AND";
            $query .= "                         W1.BU_CD     = T1.BU_CD AND";
            $query .= "                         W1.KA_CD     = T1.KA_CD";
            $query .= "                 )";

            $db->query($query);
            $db->commit();
        }

        Query::dbCheckIn($db);

        return $flg;
     }

}
?>
