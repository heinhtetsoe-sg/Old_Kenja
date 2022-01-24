<?php

require_once('for_php7.php');

class knje031Query extends Query {

    //左側のlistを作るためのデータを取得
    function getList($model, $row_year, $row_semester) {
        $query  = " SELECT DISTINCT ";
        $query .= "     T2.GRADE, ";
        $query .= "     T2.HR_CLASS, ";
        $query .= "     T2.ATTENDNO, ";
        $query .= "     T1.NAME,";
        $query .= "     T1.NAME_KANA AS KANA, ";
        $query .= "     T2.YEAR, ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T1.ENT_DATE, ";
        $query .= "     L1.HR_NAMEABBV ";
        $query .= " FROM ";
        $query .= "     SCHREG_BASE_MST T1, ";
        $query .= "     SCHREG_REGD_DAT T2 ";
        $query .= "     LEFT JOIN SCHREG_REGD_HDAT L1 ";
        $query .= "         ON L1.YEAR      = T2.YEAR AND ";
        $query .= "            L1.SEMESTER  = T2.SEMESTER AND ";
        $query .= "            L1.GRADE     = T2.GRADE AND ";
        $query .= "            L1.HR_CLASS  = T2.HR_CLASS ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "     INNER JOIN SCHREG_REGD_GDAT G1 ";
                $query .= "          ON G1.YEAR         = T2.YEAR ";
                $query .= "         AND G1.GRADE        = T2.GRADE ";
                $query .= "         AND G1.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= "     INNER JOIN SCHREG_REGD_GDAT G1 ";
            $query .= "          ON G1.YEAR         = T2.YEAR ";
            $query .= "         AND G1.GRADE        = T2.GRADE ";
            $query .= "         AND G1.SCHOOL_KIND  = '".SCHOOLKIND."' ";
        }
        $query .= " WHERE ";
        $query .= "     T2.SCHREGNO = T1.SCHREGNO AND ";
        $query .= "     T2.YEAR     = '".$row_year."' AND ";
        $query .= "     T2.SEMESTER = '".$row_semester."' AND ";
        $query .= "     T1.ENT_DIV IN('4', '5') ";
        $query .= " ORDER BY ";
        $query .= "     T2.GRADE, ";
        $query .= "     T2.HR_CLASS, ";
        $query .= "     T2.ATTENDNO ";

        return $query;
    }

    //学籍基礎マスタ取得
    function getSchBaseMst($model){
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     SCHREG_BASE_MST ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '".$model->schregno."' ";

        return $query;
    }

    //課程入学年度取得
    function getCurriculumYear($model){
        $query  = " SELECT ";
        $query .= "     CURRICULUM_YEAR ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1, ";
        $query .= "     SCHREG_REGD_GDAT T2, ";
        $query .= "     SCHREG_ENT_GRD_HIST_DAT T3 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR         = T2.YEAR AND ";
        $query .= "     T1.YEAR         = '".CTRL_YEAR."' AND ";
        $query .= "     T1.SEMESTER     = '".CTRL_SEMESTER."' AND ";
        $query .= "     T1.GRADE        = T2.GRADE AND ";
        $query .= "     T1.SCHREGNO     = T3.SCHREGNO AND ";
        $query .= "     T1.SCHREGNO     = '".$model->schregno."' AND ";
        $query .= "     T2.SCHOOL_KIND  = T3.SCHOOL_KIND ";

        return $query;
    }

    //異動リストの取得
    function getTransferList($model)
    {
        $query  = " SELECT ";
        $query .= "     T1.*, ";
        $query .= "     T2.NAME1 AS TRANSFERNAME ";
        $query .= " FROM ";
        $query .= "     SCHREG_TRANSFER_DAT T1 ";
        $query .= "     LEFT JOIN NAME_MST T2 ON T2.NAMECD1 = 'A004' AND T1.TRANSFERCD = T2.NAMECD2 ";
        $query .= " WHERE ";
        $query .= "     T1.SCHREGNO = '".$model->schregno."' ";
        $query .= " ORDER BY ";
        $query .= "     T1.TRANSFER_SDATE, ";
        $query .= "     T1.TRANSFERCD ";

        return $query;
    }

    
    //異動データ取得
    function getTransferDat($model)
    {
        $query .= " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     SCHREG_TRANSFER_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO        = '".$model->schregno."' AND ";
        $query .= "     TRANSFERCD      = '".$model->transfercd."' AND ";
        $query .= "     TRANSFER_SDATE  = DATE('".$model->transfer_sdate."') ";

        return $query;
    }

    //異動区分の取得
    function getTransfercd()
    {
        $query  = " SELECT ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     NAMECD2 || '：' || NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'A004' AND ";
        $query .= "     YEAR    = '".CTRL_YEAR."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //追加
    function &getInsertQuery($model)
    {
        $db = Query::dbCheckOut();

        $data["SCHREGNO"][TEXT]             = sprintf('%08d', $model->schregno);
        $data["TRANSFERCD"][TEXT]           = $model->field["TRANSFERCD"];
        $data["TRANSFER_SDATE"][TEXT]       = str_replace("/","-",$model->field["TRANSFER_SDATE"]);
        $data["TRANSFER_EDATE"][TEXT]       = str_replace("/","-",$model->field["TRANSFER_EDATE"]);
        $data["TRANSFERREASON"][TEXT]       = $model->field["TRANSFERREASON"];
        $data["TRANSFERPLACE"][TEXT]        = $model->field["TRANSFERPLACE"];
        $data["TRANSFERADDR"][TEXT]         = $model->field["TRANSFERADDR"];
        $data["ABROAD_CLASSDAYS"][NUMBER]   = $model->field["ABROAD_CLASSDAYS"];
        $data["ABROAD_CREDITS"][NUMBER]     = $model->field["ABROAD_CREDITS"];
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][NUMBER]            = "sysdate()";

        $query = Query::insertSQL($data, "SCHREG_TRANSFER_DAT");
        $db->query($query);

        Query::dbCheckIn($db);
        return;
    }

    //更新
    function &getUpdateQuery($model)
    {
        $db = Query::dbCheckOut();

        $data["TRANSFER_SDATE"][TEXT]       = str_replace("/","-",$model->field["TRANSFER_SDATE"]);
        $data["TRANSFER_EDATE"][TEXT]       = str_replace("/","-",$model->field["TRANSFER_EDATE"]);
        $data["TRANSFERREASON"][TEXT]       = $model->field["TRANSFERREASON"];
        $data["TRANSFERPLACE"][TEXT]        = $model->field["TRANSFERPLACE"];
        $data["TRANSFERADDR"][TEXT]         = $model->field["TRANSFERADDR"];
        $data["ABROAD_CLASSDAYS"][NUMBER]   = $model->field["ABROAD_CLASSDAYS"];
        $data["ABROAD_CREDITS"][NUMBER]     = $model->field["ABROAD_CREDITS"];
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][NUMBER]            = "sysdate()";

        $where  = " WHERE SCHREGNO          = '" .$model->schregno."' AND ";
        $where .= "       TRANSFERCD        = '" .$model->field["TRANSFERCD"]."' AND ";
        $where .= "       TRANSFER_SDATE    = '" .str_replace("/","-",$model->transfer_sdate)."'";

        $query = Query::updateSQL($data, "SCHREG_TRANSFER_DAT", $where);
        $db->query($query);

        Query::dbCheckIn($db);
        return;
    }

    //削除
    function &getDeleteQuery($model)
    {
        $db = Query::dbCheckOut();

        $query = " SELECT * FROM SCHREG_TRANSFER_DAT WHERE SCHREGNO = '".$model->schregno."' ";
        $get   = $db->getRow($query);

        if (is_array($get)) {
            $query  = "  DELETE FROM ";
            $query .= "     SCHREG_TRANSFER_DAT ";
            $query .= "  WHERE";
            $query .= "      SCHREGNO       = '".$model->schregno."' AND ";
            $query .= "      TRANSFERCD     = '".$model->field["TRANSFERCD"]."' AND ";
            $query .= "      TRANSFER_SDATE = '".str_replace("/","-",$model->field["TRANSFER_SDATE"])."' ";

            $db->query($query);
            Query::dbCheckIn($db);
            return true;
        } else {
            Query::dbCheckIn($db);
            return false;
        }
    }
}
?>
