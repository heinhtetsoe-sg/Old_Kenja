<?php

require_once('for_php7.php');

class knje011oQuery extends Query {

    function getSecurityHigh() {
        $query  = " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     MENU_HIGH_SECURITY_MST ";
        $query .= " WHERE ";
        $query .= "     PROGRAMID = 'KNJE011O' ";
        $query .= "     AND INVALID_FLG = '0' ";

        return $query;
    }

    function getSchoolCd() {
        $query  = " SELECT ";
        $query .= "     NAME2 ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'Z010' ";
        $query .= "     AND NAMECD2 = '00' ";

        return $query;
    }

    //PROGRAMPATH取得
    function getProgrampath() {
        $query  = " WITH MENU AS ( ";
        $query .= "     SELECT ";
        $query .= "         MIN(MENUID) AS MENUID ";
        $query .= "     FROM ";
        $query .= "         MENU_HIGH_SECURITY_MST ";
        $query .= "     WHERE ";
        $query .= "         PROGRAMID ='KNJE011O' ";
        $query .= " ) ";

        $query .= " SELECT ";
        $query .= "     PROGRAMPATH ";
        $query .= " FROM ";
        $query .= "     MENU_HIGH_SECURITY_MST T1, ";
        $query .= "     MENU T2 ";
        $query .= " WHERE ";
        $query .= "     T1.MENUID = T2.MENUID AND ";
        $query .= "     T1.PROGRAMID ='KNJE011O' ";

        return $query;
    }

    function selectQuery($model) {
        $query  =" SELECT ";
        $query .= "   * ";
        $query .= " FROM ";
        if ($model->mode == "grd") {     //卒業
            $query .= "   GRD_HEXAM_ENTREMARK_HDAT ";
        } else {
            $query .= "   HEXAM_ENTREMARK_HDAT ";
        }
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '{$model->schregno}' ";

        return $query;
    }

    //フォーム2の値を取得
    function selectQueryForm2($model) {
        $query = " SELECT ";
        $query .= "   YEAR, ";
        $query .= "   SCHREGNO, ";
        $query .= "   ANNUAL, ";
        $query .= "   ATTENDREC_REMARK, ";
        $query .= "   SPECIALACTREC, ";
        if ($model->useSyojikou3 == "1") {
            $query .= "   TRAIN_REF1 AS TRAIN_REF, ";
            $query .= "   TRAIN_REF2, ";
            $query .= "   TRAIN_REF3 ";
        } else {
            $query .= "   TRAIN_REF ";
        }
        $query .= " FROM ";
        if ($model->mode == "grd") {     //卒業
            $query .= "   GRD_HEXAM_ENTREMARK_DAT ";
        } else {
            $query .= "   HEXAM_ENTREMARK_DAT ";
        }
        $query .= " WHERE ";
        $query .= "       YEAR     = '{$model->annual["YEAR"]}' ";
        $query .= "   AND SCHREGNO = '{$model->schregno}' ";

        return $query;
    }

    //成績参照画面
    function selectQueryForm3($model) {
        $query  = " SELECT ";
        $query .= "     T1.ANNUAL, ";
        $query .= "     CASE WHEN T1.CLASSABBV IS NULL OR T1.CLASSABBV = '' ";
        $query .= "         THEN T3.CLASSABBV ";
        $query .= "         ELSE T1.CLASSABBV ";
        $query .= "     END AS CLASSNAME, ";
        $query .= "     CASE WHEN T1.SUBCLASSNAME IS NULL OR T1.SUBCLASSNAME = '' ";
        $query .= "         THEN T4.SUBCLASSNAME ";
        $query .= "         ELSE T1.SUBCLASSNAME ";
        $query .= "     END AS SUBCLASSNAME, ";
        $query .= "     T1.VALUATION, ";
        $query .= "     T1.GET_CREDIT, ";
        $query .= "     T1.ADD_CREDIT, ";
        $query .= "     T1.CLASSCD, ";
        $query .= "     T1.SUBCLASSCD ";
        $query .= " FROM ";
        if ($model->mode == "grd") {     //卒業
            $query .= "     GRD_STUDYREC_DAT T1 ";
            $query .= " LEFT OUTER JOIN GRD_STUDYRECREMARK_DAT T2 ON  ";
        } else {
            $query .= "     SCHREG_STUDYREC_DAT T1 ";
            $query .= " LEFT OUTER JOIN STUDYRECREMARK_DAT T2 ON  ";
        }
        $query .= "                   T1.YEAR       = T2.YEAR AND  ";
        $query .= "                   T1.SCHREGNO   = T2.SCHREGNO AND  ";
        $query .= "                   T1.CLASSCD    = T2.CLASSCD AND  ";
        $query .= "                   T1.SUBCLASSCD = T2.SUBCLASSCD ";
        $query .= " LEFT OUTER JOIN CLASS_MST    T3 ON  ";
        $query .= "                   T1.CLASSCD    = T3.CLASSCD  ";
        $query .= " LEFT OUTER JOIN SUBCLASS_MST T4 ON  ";
        $query .= "                   T1.SUBCLASSCD = T4.SUBCLASSCD  ";
        $query .= " WHERE ";
        $query .= "   T1.SCHOOLCD = '0' AND ";
        if ($model->annual["YEAR"] != "0" || $model->annual["ANNUAL"] != "0") {
            $query .= "     T1.YEAR   = '{$model->annual["YEAR"]}' AND ";
            $query .= "     T1.ANNUAL = '{$model->annual["ANNUAL"]}' AND";
        }
        $query .= "     T1.SCHREGNO = '{$model->schregno}' ";
        $query .= " ORDER BY ";
        $query .= "     T1.CLASSCD, ";
        $query .= "     T1.SUBCLASSCD, ";
        $query .= "     T1.YEAR, ";
        $query .= "     T1.ANNUAL ";

        return $query;
    }

    //学年（年次）コンボボックス用
    function selectQueryAnnual($model) {
        $tableName = ($model->mode == "grd") ? "GRD_STUDYREC_DAT" : "SCHREG_STUDYREC_DAT";

        $query  = " SELECT DISTINCT ";
        $query .= "     YEAR, ";
        $query .= "     ANNUAL ";
        $query .= " FROM ";
        $query .= "     {$tableName} ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '{$model->schregno}' ";

        return $query;
    }

    /* 更新処理 */
    function update(&$db, $model) {
        //レコードを一旦削除する
        if ($model->mode == "grd") {     //卒業
            $tableName = "GRD_HEXAM_ENTREMARK_HDAT";
        } else {
            $tableName = "HEXAM_ENTREMARK_HDAT";
        }

        $query  = " DELETE FROM ";
        $query .= "     {$tableName} ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '{$model->schregno}' ";

        $db->query($query);

        $data = array();
        $data["SCHREGNO"][TEXT]       = $model->schregno;
        $data["DISEASE"][TEXT]        = $model->field["DISEASE"];
        $data["DOC_REMARK"][TEXT]     = $model->field["DOC_REMARK"];
        $data["TR_REMARK"][TEXT]      = $model->field["TR_REMARK"];
        $data["TOTALSTUDYACT"][TEXT]  = $model->field["TOTALSTUDYACT"];
        $data["TOTALSTUDYVAL"][TEXT]  = $model->field["TOTALSTUDYVAL"];
        $data["REMARK"][TEXT]         = $model->field["REMARK"];
        $data["COMMENTEX_A_CD"][TEXT] = $model->field["COMMENTEX_A_CD"];
        $data["REGISTERCD"][TEXT]     = STAFFCD;

        $query = Query::insertSQL($data, $tableName);
        $db->query($query);
    }

    /* 更新処理 */
    function update2(&$db, $model) {
        if ($model->mode == "grd") {     //卒業
            $tableName = "GRD_HEXAM_ENTREMARK_DAT";
        } else {
            $tableName = "HEXAM_ENTREMARK_DAT";
        }

        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     {$tableName} ";
        $query .= " WHERE ";
        $query .= "       YEAR      = '{$model->annual["YEAR"]}' ";
        $query .= "   AND SCHREGNO  = '{$model->schregno}' ";

        $row_data = $db->getRow($query, DB_FETCHMODE_ASSOC);

        if ($model->useSyojikou3 == "1") {
            $row_data["TRAIN_REF1"]       = $model->field2["TRAIN_REF"];
            $row_data["TRAIN_REF2"]       = $model->field2["TRAIN_REF2"];
            $row_data["TRAIN_REF3"]       = $model->field2["TRAIN_REF3"];
        } else {
            $row_data["TRAIN_REF"]        = $model->field2["TRAIN_REF"];
        }

        //レコードを一旦削除する
        $query  = " DELETE FROM ";
        $query .= "     {$tableName} ";
        $query .= " WHERE ";
        $query .= "       YEAR      = '{$model->annual["YEAR"]}' ";
        $query .= "   AND SCHREGNO  = '{$model->schregno}' ";

        $db->query($query);

        $data = array();
        $data["YEAR"][TEXT]             = $model->annual["YEAR"];
        $data["SCHREGNO"][TEXT]         = $model->schregno;
        $data["ANNUAL"][TEXT]           = $model->annual["ANNUAL"];
        $data["SPECIALACTREC"][TEXT]    = $model->field2["SPECIALACTREC"];
        $data["TRAIN_REF"][TEXT]        = $row_data["TRAIN_REF"];
        $data["TRAIN_REF1"][TEXT]       = $row_data["TRAIN_REF1"];
        $data["TRAIN_REF2"][TEXT]       = $row_data["TRAIN_REF2"];
        $data["TRAIN_REF3"][TEXT]       = $row_data["TRAIN_REF3"];
        $data["ATTENDREC_REMARK"][TEXT] = $model->field2["ATTENDREC_REMARK"];
        $data["REGISTERCD"][TEXT]       = STAFFCD;

        $query = Query::insertSQL($data, $tableName);
        $db->query($query);
    }

    //保健より読み込み
    function selectQueryHealth($model) {
         if ($model->mode == "grd") {     //卒業
            $year = $model->grd_year;
        } else {
            $year = $model->year;
        }
        $query  = " SELECT ";
        $query .= "     MEISYOU_GET(OTHERDISEASECD,'F140',1) AS OTHERDISEASECD, ";
        $query .= "     DOC_REMARK ";
        $query .= " FROM ";
        if ($model->mode == "grd") {     //卒業
            $query .= "   GRD_MEDEXAM_DET_DAT ";
        } else {
            $query .= "   MEDEXAM_DET_DAT ";
        }
        $query .= " WHERE ";
        $query .= "     YEAR     = '{$year}' AND ";
        $query .= "     SCHREGNO = '{$model->schregno}' ";

        return $query;
    }

    //学習指導要録より再読込
    function selectQuery_Htrainremark_Dat($model) {
        $query  = " SELECT ";
        $query .= "     YEAR, ";
        $query .= "     ANNUAL, ";
        $query .= "     SPECIALACTREMARK AS SPECIALACTREC, ";
        $query .= "     TOTALREMARK AS TRAIN_REF, ";
        $query .= "     ATTENDREC_REMARK ";
        $query .= " FROM ";
        if ($model->mode == "grd") {     //卒業
            $query .= "   GRD_HTRAINREMARK_DAT ";
        } else {
            $query .= "   HTRAINREMARK_DAT ";
        }
        $query .= " WHERE ";
        $query .= "         YEAR      = '{$model->annual["YEAR"]}' ";
        $query .= "     AND SCHREGNO  = '{$model->schregno}' ";

        return $query;
    }

    //指導要録参照画面
    function selectQuery_Htrainremark_Hdat($model) {
        $query  = " SELECT ";
        $query .= "     TOTALSTUDYACT, ";
        $query .= "     TOTALSTUDYVAL ";
        $query .= " FROM ";
        if ($model->mode == "grd") {     //卒業
            $query .= "   GRD_HTRAINREMARK_HDAT ";
        } else {
            $query .= "   HTRAINREMARK_HDAT ";
        }
        $query .= " WHERE ";
        $query .= "     SCHREGNO  = '{$model->schregno}' ";

        return $query;
    }
}
?>
