<?php

require_once('for_php7.php');
class knje063cQuery extends Query {
    //変換対象 年度
    function getHenkanMotoYear($model) {
        $query  = " SELECT ";
        $query .= "     YEAR AS LABEL ";
        $query .= "     , YEAR AS VALUE  ";
        $query .= " FROM ";
        $query .= "     SCHREG_STUDYREC_DAT  ";
        $query .= " GROUP BY ";
        $query .= "     YEAR ";
        $query .= " ORDER BY ";
        $query .= "     VALUE DESC ";

        return $query;
    }

    //科目
    function getSubclass($year, $subclass="") {
        $query  = " SELECT ";
        $query .= "     SRD.CLASSCD || '-' || SRD.SCHOOL_KIND || '-' || SRD.CURRICULUM_CD || '-' || SRD.SUBCLASSCD || ':' || SUB.SUBCLASSNAME AS LABEL ";
        $query .= "     , SRD.CLASSCD || '-' || SRD.SCHOOL_KIND || '-' || SRD.CURRICULUM_CD || '-' || SRD.SUBCLASSCD AS VALUE  ";
        $query .= " FROM ";
        $query .= "     SCHREG_STUDYREC_DAT SRD  ";
        $query .= "     INNER JOIN SUBCLASS_MST SUB  ";
        $query .= "         ON SUB.CLASSCD = SRD.CLASSCD  ";
        $query .= "         AND SUB.SCHOOL_KIND = SRD.SCHOOL_KIND  ";
        $query .= "         AND SUB.CURRICULUM_CD = SRD.CURRICULUM_CD  ";
        $query .= "         AND SUB.SUBCLASSCD = SRD.SUBCLASSCD  ";
        $query .= " WHERE ";
        $query .= "     SRD.YEAR = '".$year."'  ";
        if ($subclass) {
            $query .= "     AND SRD.CLASSCD || '-' || SRD.SCHOOL_KIND || '-' || SRD.CURRICULUM_CD || '-' || SRD.SUBCLASSCD <> '".$subclass."'  ";
        }
        $query .= " GROUP BY ";
        $query .= "     SRD.CLASSCD ";
        $query .= "     , SRD.SCHOOL_KIND ";
        $query .= "     , SRD.CURRICULUM_CD ";
        $query .= "     , SRD.SUBCLASSCD ";
        $query .= "     , SUB.SUBCLASSNAME ";

        return $query;
    }

    //変換条件１ 年度
    function getHenkanJoken1Year($model) {
        $query  = " SELECT ";
        $query .= "     YEAR AS LABEL ";
        $query .= "     , YEAR AS VALUE  ";
        $query .= " FROM ";
        $query .= "     SUBCLASS_YDAT ";
        $query .= " GROUP BY ";
        $query .= "     YEAR ";
        $query .= " ORDER BY ";
        $query .= "     VALUE DESC ";

        return $query;
    }

    //年度、科目でSCHREG_STUDY_DATを取得
    function getStudyrec($year, $subclass) {
        $query  = " SELECT ";
        $query .= "     SCHREGNO ";
        $query .= "     , GET_CREDIT ";
        $query .= "     , ADD_CREDIT ";
        $query .= " FROM ";
        $query .= "     SCHREG_STUDYREC_DAT  ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$year."'  ";
        $query .= "     AND CLASSCD || '-' || SCHOOL_KIND || '-' ||  CURRICULUM_CD || '-' ||  SUBCLASSCD = '".$subclass."' ";

        return $query;
    }

    //対象の生徒
    function getTargetStudent($model) {
        $query  = " SELECT DISTINCT ";
        $query .= "     REGD_H.HR_NAME ";
        $query .= "     , REGD.ATTENDNO ";
        $query .= "     , BASE.NAME ";
        $query .= "     , SRD.SCHREGNO ";
        $query .= " FROM ";
        $query .= "     SCHREG_STUDYREC_DAT SRD ";
        $query .= "     INNER JOIN SCHREG_BASE_MST BASE ";
        $query .= "         ON BASE.SCHREGNO = SRD.SCHREGNO ";
        $query .= "     INNER JOIN SCHREG_REGD_DAT REGD ";
        $query .= "         ON REGD.YEAR = SRD.YEAR ";
        $query .= "         AND REGD.SCHREGNO = SRD.SCHREGNO ";
        $query .= "         AND REGD.ANNUAL = SRD.ANNUAL ";
        $query .= "     INNER JOIN SCHREG_REGD_HDAT REGD_H ";
        $query .= "         ON REGD_H.YEAR = SRD.YEAR ";
        $query .= "         AND REGD_H.SEMESTER = REGD.SEMESTER ";
        $query .= "         AND REGD_H.GRADE = REGD.GRADE ";
        $query .= "         AND REGD_H.HR_CLASS = REGD.HR_CLASS ";
        $query .= "     LEFT JOIN (".knje063cQuery::getStudyrec($model->field["HENKAN_JOKEN_1_YEAR"], $model->field["SYUTOKU_JOKEN_SUBCLASS"]).") SYUTOKU_JOKEN ";
        $query .= "         ON SYUTOKU_JOKEN.SCHREGNO = SRD.SCHREGNO ";
        $query .= " WHERE ";
        $query .= "     SRD.YEAR = '".$model->field["HENKAN_MOTO_YEAR"]."' ";
        $query .= "     AND SRD.CLASSCD || '-' ||  SRD.SCHOOL_KIND || '-' ||  SRD.CURRICULUM_CD || '-' ||  SRD.SUBCLASSCD = '".$model->field["HENKAN_MOTO_SUBCLASS"]."' ";
        if ($model->field["SYUTOKU_JOKEN_SUBCLASS"]) {
            if ($model->field["ATTEND_DIV"] == "1") {
                $query .= "     AND SYUTOKU_JOKEN.SCHREGNO IS NOT NULL ";
                if ($model->field["CREGIT_CNT"]) {
                    $query .= "     AND VALUE(SYUTOKU_JOKEN.GET_CREDIT, 0) + VALUE(SYUTOKU_JOKEN.ADD_CREDIT, 0) >= ".$model->field["CREGIT_CNT"]." ";
                }
            } else {
                $query .= "     AND SYUTOKU_JOKEN.SCHREGNO IS NULL ";
            }
        }
        $query .= " ORDER BY ";
        $query .= "     REGD_H.HR_NAME, ";
        $query .= "     REGD.ATTENDNO ";

        return $query;

    }

    //更新
    function &getUpdateQuery($model) {

        knje063cQuery::getDeleteQuery($model, $model->field["HENKAN_MOTO_YEAR"], $model->field["HENKAN_GO_SUBCLASS"]);

        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //登録するデータ
        $subclass = explode("-", $model->field["HENKAN_GO_SUBCLASS"]);
        $query = knje063cQuery::getUpdateStudyrec($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $data = array();
            $data["SCHOOLCD"][TEXT]          = $row["SCHOOLCD"];
            $data["YEAR"][TEXT]              = $model->field["HENKAN_MOTO_YEAR"];
            $data["SCHREGNO"][TEXT]          = $row["SCHREGNO"];
            $data["ANNUAL"][TEXT]            = $row["ANNUAL"];
            $data["CLASSCD"][TEXT]           = $subclass[0];
            $data["SCHOOL_KIND"][TEXT]       = $subclass[1];
            $data["CURRICULUM_CD"][TEXT]     = $subclass[2];
            $data["SUBCLASSCD"][TEXT]        = $subclass[3];
            $data["VALUATION"][NUMBER]       = $row["VALUATION"];
            $data["GET_CREDIT"][NUMBER]      = $row["GET_CREDIT"];
            $data["ADD_CREDIT"][NUMBER]      = $row["ADD_CREDIT"];
            $data["COMP_CREDIT"][NUMBER]     = $row["COMP_CREDIT"];
            $data["PRINT_FLG"][TEXT]         = $row["PRINT_FLG"];
            $data["REGISTERCD"][TEXT]        = STAFFCD;
            $data["UPDATED"][FUNC]           = "sysdate()";

            $query = Query::insertSQL($data, "SCHREG_STUDYREC_DAT");
            $db->query($query);
        }
        $db->commit();
        Query::dbCheckIn($db);

        knje063cQuery::getDeleteQuery($model, $model->field["HENKAN_MOTO_YEAR"], $model->field["HENKAN_MOTO_SUBCLASS"]);

        //リストを初期化
        $this->selectdata = "";
        $this->selectdataText = "";
        return;
    }

    //削除
    function &getDeleteQuery($model, $year, $subclass) {
        $db = Query::dbCheckOut();

        $query  = " DELETE FROM ";
        $query .= "     SCHREG_STUDYREC_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$year."' ";
        $query .= "     AND CLASSCD || '-' ||  SCHOOL_KIND || '-' ||  CURRICULUM_CD || '-' ||  SUBCLASSCD = '".$subclass."' ";
        $query .= "     AND SCHREGNO IN ('".str_replace(",", "','", $model->selectdata)."') ";
        $db->query($query);

        Query::dbCheckIn($db);
        return;
    }

    //登録データ取得
    function getUpdateStudyrec($model) {
        $query  = " SELECT ";
        $query .= "     SCHOOLCD ";
        $query .= "     , SCHREGNO ";
        $query .= "     , ANNUAL ";
        $query .= "     , VALUATION ";
        $query .= "     , GET_CREDIT ";
        $query .= "     , ADD_CREDIT ";
        $query .= "     , COMP_CREDIT ";
        $query .= "     , PRINT_FLG ";
        $query .= " FROM ";
        $query .= "     SCHREG_STUDYREC_DAT  ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$model->field["HENKAN_MOTO_YEAR"]."' ";
        $query .= "     AND CLASSCD || '-' ||  SCHOOL_KIND || '-' ||  CURRICULUM_CD || '-' ||  SUBCLASSCD = '".$model->field["HENKAN_MOTO_SUBCLASS"]."' ";
        $query .= "     AND SCHREGNO IN ('".str_replace(",", "','", $model->selectdata)."') ";

        return $query;
    }
}
?>
