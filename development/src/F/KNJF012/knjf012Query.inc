<?php

require_once('for_php7.php');

class knjf012Query extends Query {

    //SCHREG_BASE_MST取得
    function getSchregBaseMst($model)  {

        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     SCHREG_BASE_MST ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '".$model->schregno."' ";

        return $query;
    }

    //MEDEXAM_DET_NO_DAT取得
    function getMedexamDetNoDat($model) {

        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     MEDEXAM_DET_NO_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".CTRL_YEAR."' AND ";
        $query .= "     SCHREGNO    = '".$model->schregno."' AND ";
        $query .= "     NO          = '".$model->field["NO"]."' ";

        return $query;
    }

    //視力の文字取得
    function getVisionMark() {

        $query .= " SELECT ";
        $query .= "     NAMESPARE1, ";
        $query .= "     NAME1 ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'F011' ";
        $query .= " ORDER BY ";
        $query .= "     NAMECD2 DESC ";

        return $query;
    }

    //MEDEXAM_DET_NO_DAT更新
    function &updateMedexamDetNoDat($model) {
        $db = Query::dbCheckOut();
        $data = array();

        //データの有無チェック
        $check = $db->getOne(knjf012Query::getMedexamDetNoDat($model));

        $data["DATE"][TEXT]                 = $model->field["DATE"];
        $data["HEIGHT"][NUMBER]             = $model->field["HEIGHT"];
        $data["WEIGHT"][NUMBER]             = $model->field["WEIGHT"];
        $data["SITHEIGHT"][NUMBER]          = $model->field["SITHEIGHT"];
        $data["R_BAREVISION"][TEXT]         = $model->field["R_BAREVISION"];
        $data["R_BAREVISION_MARK"][TEXT]    = $model->field["R_BAREVISION_MARK"];
        $data["R_VISION"][TEXT]             = $model->field["R_VISION"];
        $data["R_VISION_MARK"][TEXT]        = $model->field["R_VISION_MARK"];
        $data["L_BAREVISION"][TEXT]         = $model->field["L_BAREVISION"];
        $data["L_BAREVISION_MARK"][TEXT]    = $model->field["L_BAREVISION_MARK"];
        $data["L_VISION"][TEXT]             = $model->field["L_VISION"];
        $data["L_VISION_MARK"][TEXT]        = $model->field["L_VISION_MARK"];

        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][FUNC]              = "sysdate()";

        if ($check) {
            $where  = " WHERE ";
            $where .= "     YEAR        = '".CTRL_YEAR."' AND ";
            $where .= "     SCHREGNO    = '".$model->schregno."' AND ";
            $where .= "     NO          = '".$model->field["NO"]."' ";

            $db->query(Query::updateSQL($data, "MEDEXAM_DET_NO_DAT", $where));
        } else {
            $data["YEAR"][TEXT]                 = CTRL_YEAR;
            $data["SCHREGNO"][TEXT]             = $model->schregno;
            $data["NO"][TEXT]                   = $model->field["NO"];

            $db->query(Query::insertSQL($data, "MEDEXAM_DET_NO_DAT"));
        }

        Query::dbCheckIn($db);
        return;
    }

    //MEDEXAM_HDAT更新
    function &updateMedexamHdat($model) {
        $db = Query::dbCheckOut();
        $data = array();

        //データの有無チェック
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     MEDEXAM_HDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".CTRL_YEAR."' AND ";
        $query .= "     SCHREGNO    = '".$model->schregno."' ";
        $check = $db->getOne($query);

        $data["DATE"][TEXT]                 = $model->field["DATE"];
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][FUNC]              = "sysdate()";

        if ($check) {
            $where  = " WHERE ";
            $where .= "     YEAR        = '".CTRL_YEAR."' AND ";
            $where .= "     SCHREGNO    = '".$model->schregno."' ";

            $db->query(Query::updateSQL($data, "MEDEXAM_HDAT", $where));
        } else {
            $data["YEAR"][TEXT]                 = CTRL_YEAR;
            $data["SCHREGNO"][TEXT]             = $model->schregno;

            $db->query(Query::insertSQL($data, "MEDEXAM_HDAT"));
        }

        Query::dbCheckIn($db);
        return;
    }

    //MEDEXAM_DET_DAT更新
    function &updateMedexamDetDat($model) {
        $db = Query::dbCheckOut();
        $data = array();

        //データの有無チェック
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     MEDEXAM_DET_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".CTRL_YEAR."' AND ";
        $query .= "     SCHREGNO    = '".$model->schregno."' ";
        $check = $db->getOne($query);

        $data["HEIGHT"][NUMBER]             = $model->field["HEIGHT"];
        $data["WEIGHT"][NUMBER]             = $model->field["WEIGHT"];
        $data["SITHEIGHT"][NUMBER]          = $model->field["SITHEIGHT"];
        $data["R_BAREVISION"][TEXT]         = $model->field["R_BAREVISION"];
        $data["R_BAREVISION_MARK"][TEXT]    = $model->field["R_BAREVISION_MARK"];
        $data["R_VISION"][TEXT]             = $model->field["R_VISION"];
        $data["R_VISION_MARK"][TEXT]        = $model->field["R_VISION_MARK"];
        $data["L_BAREVISION"][TEXT]         = $model->field["L_BAREVISION"];
        $data["L_BAREVISION_MARK"][TEXT]    = $model->field["L_BAREVISION_MARK"];
        $data["L_VISION"][TEXT]             = $model->field["L_VISION"];
        $data["L_VISION_MARK"][TEXT]        = $model->field["L_VISION_MARK"];

        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][FUNC]              = "sysdate()";

        if ($check) {
            $where  = " WHERE ";
            $where .= "     YEAR        = '".CTRL_YEAR."' AND ";
            $where .= "     SCHREGNO    = '".$model->schregno."' ";

            $db->query(Query::updateSQL($data, "MEDEXAM_DET_DAT", $where));
        } else {
            $data["YEAR"][TEXT]                 = CTRL_YEAR;
            $data["SCHREGNO"][TEXT]             = $model->schregno;

            $db->query(Query::insertSQL($data, "MEDEXAM_DET_DAT"));
        }

        Query::dbCheckIn($db);
        return;
    }

    //MEDEXAM_DET_NO_DATを削除
    function &deleteMedexamDetNoDat($model) {
        $db = Query::dbCheckOut();

        $query  = " DELETE FROM ";
        $query .= "     MEDEXAM_DET_NO_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".CTRL_YEAR."' AND ";
        $query .= "     SCHREGNO    = '".$model->schregno."' AND ";
        $query .= "     NO          = '".$model->field["NO"]."' ";

        $db->query($query);
        Query::dbCheckIn($db);
        return;
    }
}
?>
