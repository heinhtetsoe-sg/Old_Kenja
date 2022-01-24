<?php

require_once('for_php7.php');

class knjz204Query extends Query {
    //名称取得
    function getVNameMstZ042() {
        $query  = " SELECT ";
        $query .= "     V1.NAME1 ";
        $query .= " FROM ";
        $query .= "     SCHOOL_DETAIL_DAT D1 ";
        $query .= " LEFT JOIN V_NAME_MST V1 ON V1.NAMECD2 = D1.SCHOOL_REMARK1 ";
        $query .= "                        AND V1.NAMECD1 = 'Z042' ";
        $query .= "                        AND V1.YEAR    = '".CTRL_YEAR."' ";
        $query .= " WHERE ";
        $query .= "     D1.SCHOOL_SEQ = '001' ";
        $query .= "     AND D1.YEAR    = '".CTRL_YEAR."' ";
        $query .= "     AND D1.SCHOOLCD = '000000000000' ";
        if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= "     AND D1.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }

        return $query;
    }

    //リスト表示
    function getList() {
        $query  = " SELECT ";
        $query .= "     PAT_D.PATTERNCD, ";
        $query .= "     PAT_D.PATTERNCD || '：' || PAT_M.PATTERNCDNAME AS PATTERNCDNAME, ";
        $query .= "     PAT_D.ASSESSMARK, ";
        $query .= "     PAT_D.RATE, ";
        $query .= "     PAT_M.BASEDATE ";
        $query .= " FROM ";
        $query .= "     ABSENCE_PATTERN_MST PAT_M ";
        $query .= " LEFT JOIN ABSENCE_PATTERN_DAT PAT_D ON PAT_D.YEAR      = PAT_M.YEAR ";
        $query .= "                                    AND PAT_D.PATTERNCD = PAT_M.PATTERNCD ";
        $query .= " WHERE ";
        $query .= "     PAT_D.YEAR = '".CTRL_YEAR."' ";

        return $query;
    }

    //段階値カウント
    function getAssesslevelCnt($patternCd) {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     ABSENCE_PATTERN_MST PAT_M ";
        $query .= " LEFT JOIN ABSENCE_PATTERN_DAT PAT_D ON PAT_D.YEAR      = PAT_M.YEAR ";
        $query .= "                                    AND PAT_D.PATTERNCD = PAT_M.PATTERNCD ";
        $query .= " WHERE ";
        $query .= "         PAT_D.YEAR      = '".CTRL_YEAR."' ";
        $query .= "     AND PAT_D.PATTERNCD = '".$patternCd."' ";

        return $query;
    }


    //１レコード取得
    function getRow1($patternCd) {
        $query  = " SELECT ";
        $query .= "     PATTERNCD, ";
        $query .= "     PATTERNCDNAME, ";
        $query .= "     BASEDATE ";
        $query .= " FROM ";
        $query .= "     ABSENCE_PATTERN_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR      = '".CTRL_YEAR."' ";
        $query .= "     AND PATTERNCD = '".$patternCd."' ";

        return $query;
    }

    //レコード取得取得
    function getRow2($patternCd) {
        $query  = " SELECT ";
        $query .= "     ASSESSLEVEL, ";
        $query .= "     ASSESSMARK, ";
        $query .= "     RATE ";
        $query .= " FROM ";
        $query .= "     ABSENCE_PATTERN_DAT ";
        $query .= " WHERE ";
        $query .= "         YEAR      = '".CTRL_YEAR."' ";
        $query .= "     AND PATTERNCD = '".$patternCd."' ";

        return $query;
    }

    //カウント取得
    function getRowCount($patternCd) {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     ABSENCE_PATTERN_DAT ";
        $query .= " WHERE ";
        $query .= "         YEAR      = '".CTRL_YEAR."' ";
        $query .= "     AND PATTERNCD = '".$patternCd."' ";

        return $query;
    }

    //前年度からのコピーの件数カウント
    function getCopyCountQuery() {
        $db = Query::dbCheckOut();

        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     ABSENCE_PATTERN_MST PAT_M ";
        $query .= " LEFT JOIN ABSENCE_PATTERN_DAT PAT_D ON PAT_D.YEAR      = PAT_M.YEAR ";
        $query .= "                                    AND PAT_D.PATTERNCD = PAT_M.PATTERNCD ";
        $query .= " WHERE ";
        $query .= "     PAT_M.YEAR = '".(CTRL_YEAR-1)."' ";

        $cnt = $db->getOne($query);
        Query::dbCheckIn($db);
        return $cnt;
    }

    //今年度の件数カウント
    function getCopyCount2() {
        $db = Query::dbCheckOut();

        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     ABSENCE_PATTERN_MST PAT_M ";
        $query .= " LEFT JOIN ABSENCE_PATTERN_DAT PAT_D ON PAT_D.YEAR      = PAT_M.YEAR ";
        $query .= "                                    AND PAT_D.PATTERNCD = PAT_M.PATTERNCD ";
        $query .= " WHERE ";
        $query .= "     PAT_M.YEAR = '".CTRL_YEAR."' ";

        $cnt2 = $db->getOne($query);
        Query::dbCheckIn($db);
        return $cnt2;
    }

    //今年度のABSENCE_PATTERN_MSTデータを消去
    function &getCopyDelMstQuery() {
        $db = Query::dbCheckOut();

        $query  = " DELETE FROM ";
        $query .= "     ABSENCE_PATTERN_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";

        $db->query($query);
        Query::dbCheckIn($db);
        return;
    }

    //今年度のABSENCE_PATTERN_DATデータを消去
    function &getCopyDelDatQuery() {
        $db = Query::dbCheckOut();

        $query  = " DELETE FROM ";
        $query .= "     ABSENCE_PATTERN_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";

        $db->query($query);
        Query::dbCheckIn($db);
        return;
    }

    //前年度からのコピー(ABSENCE_PATTERN_MST)
    function &getCopyClassQuery1() {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $query  = " INSERT INTO ABSENCE_PATTERN_MST ";
        $query .= " SELECT ";
        $query .= "     '".CTRL_YEAR."' AS YEAR, ";
        $query .= "     PATTERNCD, ";
        $query .= "     PATTERNCDNAME, ";
        $query .= "     BASEDATE, ";
        $query .= "     '".STAFFCD."', ";
        $query .= "     sysdate() ";
        $query .= " FROM ";
        $query .= "     ABSENCE_PATTERN_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".(CTRL_YEAR-1)."' ";

        $db->query($query);
        $db->commit();
        Query::dbCheckIn($db);
        return true;
   }

    //前年度からのコピー(ABSENCE_PATTERN_DAT)
    function &getCopyClassQuery2() {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $query  = " INSERT INTO ABSENCE_PATTERN_DAT ";
        $query .= " SELECT ";
        $query .= "     '".CTRL_YEAR."' AS YEAR, ";
        $query .= "     PATTERNCD, ";
        $query .= "     ASSESSLEVEL, ";
        $query .= "     ASSESSMARK, ";
        $query .= "     RATE, ";
        $query .= "     '".STAFFCD."', ";
        $query .= "     sysdate() ";
        $query .= " FROM ";
        $query .= "     ABSENCE_PATTERN_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".(CTRL_YEAR-1)."' ";

        $db->query($query);
        $db->commit();
        Query::dbCheckIn($db);
        return true;
   }

    //重複チェック
    function checkPcd($patternCd) {
        $db = Query::dbCheckOut();

        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     ABSENCE_PATTERN_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR      = '".CTRL_YEAR."' ";
        $query .= "     AND PATTERNCD = '".$patternCd."' ";

        $checkPcd = $db->getOne($query);
        Query::dbCheckIn($db);
        return $checkPcd;
    }

    //--- INSERT
    function &getInsertQuery($fields, $model) {
        $db = Query::dbCheckOut();

        $data["YEAR"][TEXT]               = CTRL_YEAR;
        $data["PATTERNCD"][TEXT]          = $fields["PATTERNCD"];
        $data["PATTERNCDNAME"][TEXT]      = $fields["PATTERNCDNAME"];
        $data["BASEDATE"][DATE]           = str_replace("/","-",$fields["BASEDATE"]);
        $data["REGISTERCD"][TEXT]         = STAFFCD;
        $data["UPDATED"][NUMBER]          = "sysdate()";

        $query = Query::insertSQL($data, "ABSENCE_PATTERN_MST");
        $db->query($query);

        for ($i = 0; $i < $model->field["ASSESSLEVELCNT"]; $i++) {
            $data = array();
            $data["YEAR"][TEXT]               = CTRL_YEAR;
            $data["PATTERNCD"][TEXT]          = $fields["PATTERNCD"];
            $data["ASSESSLEVEL"][NUMBER]      = $i + 1;
            $data["ASSESSMARK"][TEXT]         = $model->fields["ASSESSMARK"][$i];
            $data["RATE"][NUMBER]             = $model->fields["RATE"][$i];
            $data["REGISTERCD"][TEXT]         = STAFFCD;
            $data["UPDATED"][NUMBER]          = "sysdate()";
            //追加
            $query = Query::insertSQL($data, "ABSENCE_PATTERN_DAT");
            $db->query($query);
        }

        Query::dbCheckIn($db);
        return;
    }

    //DELETE
    function &getDeleteQuery($fields) {
        $db = Query::dbCheckOut();

        $query  = " DELETE FROM ";
        $query .= "     ABSENCE_PATTERN_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR          = '".CTRL_YEAR."' ";
        $query .= "     AND PATTERNCD     = '" .$fields["PATTERNCD"]."' ";

        $db->query($query);

        $query  = " DELETE FROM ";
        $query .= "     ABSENCE_PATTERN_DAT ";
        $query .= " WHERE ";
        $query .= "         YEAR          = '".CTRL_YEAR."' ";
        $query .= "     AND PATTERNCD     = '" .$fields["PATTERNCD"]."' ";

        $db->query($query);

        Query::dbCheckIn($db);
        return;
    }
}
?>
