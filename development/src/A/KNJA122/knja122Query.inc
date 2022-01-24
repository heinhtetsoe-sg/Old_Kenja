<?php

require_once('for_php7.php');

class knja122Query extends Query {
    //近大判定用
    function getKindaiJudgment() {
        $db = Query::dbCheckOut();

        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     SCHOOL_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND SCHOOLNAME1 LIKE '%近畿大学%' ";

        $cnt = $db->getOne($query);
        Query::dbCheckIn($db);

        return $cnt;
    }

    //１レコード取得
    function getTrainRow($model, $db) {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     HTRAINREMARK_DAT ";
        $query .= " WHERE ";
        $query .= "       YEAR     = '{$model->exp_year}' ";
        $query .= "   AND SCHREGNO = '{$model->schregno}' ";

        $row = $db->getRow($query,DB_FETCHMODE_ASSOC);
        return $row;
    }

    //行動の記録
    function getBehavior($year, $schregno) {
        $query  = " SELECT ";
        $query .= "     CODE ";
        $query .= " FROM ";
        $query .= "     BEHAVIOR_DAT ";
        $query .= " WHERE ";
        $query .= "         YEAR     = '{$year}' ";
        $query .= "     AND SCHREGNO = '{$schregno}' ";
        $query .= "     AND DIV      = '1' ";
        $query .= "     AND RECORD   = '1' ";
        $query .= " ORDER BY ";
        $query .= "     INT(CODE) ";

        return $query;
    }

    //通知票より読込
    function getBehaviorSemesDat($code, $model) {
        $query  = " SELECT ";
        $query .= "     COUNT(*) AS COUNT ";
        $query .= " FROM ";
        $query .= "     BEHAVIOR_SEMES_DAT ";
        $query .= " WHERE ";
        $query .= "         YEAR     = '{$model->exp_year}' ";
        $query .= "     AND SEMESTER IN ('1', '2') ";
        $query .= "     AND SCHREGNO = '{$model->schregno}' ";
        $query .= "     AND CODE     = '{$code}' ";
        $query .= "     AND RECORD   = 'A' ";
        $query .= " GROUP BY ";
        $query .= "     YEAR, ";
        $query .= "     SCHREGNO, ";
        $query .= "     CODE, ";
        $query .= "     RECORD ";

        return $query;
    }

    //ANNUAL取得
    function getAnnual($model) {
        $query  = " SELECT ";
        $query .= "     ANNUAL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT ";
        $query .= " WHERE ";
        $query .= "         SCHREGNO = '{$model->schregno}' ";
        $query .= "     AND YEAR     = '{$model->exp_year}' ";
        $query .= "     AND SEMESTER = '{$model->exp_semester}' ";

        return $query;
    }

    /********************************************************************************/
    /********************************* DELETE ***************************************/
    /********************************************************************************/
    //BEHAVIOR_DAT
    function &getDeleteBehavior($year, $schregno, $db) {
        $query  = " DELETE FROM ";
        $query .= "     BEHAVIOR_DAT ";
        $query .= " WHERE ";
        $query .= "         YEAR     = '{$year}' ";
        $query .= "     AND SCHREGNO = '{$schregno}' ";
        $query .= "     AND DIV      = '1' ";

        $db->query($query);
    }

    /******************************************************************************************/
    /********************************* INSERT or UPDATE ***************************************/
    /******************************************************************************************/
    //HTRAINREMARK_DAT の UPDATE
    function &getUpdateHtrainDat($model, $annual, $db) {
        $data["ANNUAL"][TEXT]           = $annual;
        $data["TOTALSTUDYACT"][TEXT]    = $model->field["TOTALSTUDYACT"];
        $data["VIEWREMARK"][TEXT]       = $model->field["VIEWREMARK"];
        $data["TOTALSTUDYVAL"][TEXT]    = $model->field["TOTALSTUDYVAL"];
        $data["TOTALREMARK"][TEXT]      = $model->field["TOTALREMARK"];
        $data["ATTENDREC_REMARK"][TEXT] = $model->field["ATTENDREC_REMARK"];
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][FUNC]          = "sysdate()";

        $where  = " WHERE ";
        $where .= "     YEAR     = '{$model->exp_year}' ";
        $where .= " AND SCHREGNO = '{$model->schregno}'";

        $query = Query::updateSQL($data, "HTRAINREMARK_DAT", $where);
        $db->query($query);
    }

    //HTRAINREMARK_DAT の INSERT
    function &getInsertHtrainDat($model, $annual, $db) {
        $data["YEAR"][TEXT]             = $model->exp_year;
        $data["SCHREGNO"][TEXT]         = $model->schregno;
        $data["ANNUAL"][TEXT]           = $annual;
        $data["TOTALSTUDYACT"][TEXT]    = $model->field["TOTALSTUDYACT"];
        $data["VIEWREMARK"][TEXT]       = $model->field["VIEWREMARK"];
        $data["TOTALSTUDYVAL"][TEXT]    = $model->field["TOTALSTUDYVAL"];
        $data["TOTALREMARK"][TEXT]      = $model->field["TOTALREMARK"];
        $data["ATTENDREC_REMARK"][TEXT] = $model->field["ATTENDREC_REMARK"];
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][FUNC]          = "sysdate()";

        $query = Query::insertSQL($data, "htrainremark_dat");
        $db->query($query);
    }

/*********************************************** subform1より ***********************************************/
    //BEHAVIOR_DAT
    function getInsertBehavior($model, $annual, $db) {
        $year     = $model->exp_year;
        $schregno = $model->schregno;

        foreach ($model->field1 as $key => $val) {
            if ($val) {
                $data["YEAR"][TEXT]       = $year;
                $data["SCHREGNO"][TEXT]   = $schregno;
                $data["DIV"][TEXT]        = '1';
                $data["CODE"][TEXT]       = sprintf("%02d", $val);
                $data["ANNUAL"][TEXT]     = $annual;
                $data["RECORD"][TEXT]     = '1';
                $data["REGISTERCD"][TEXT] = STAFFCD;
                $data["UPDATED"][FUNC]    = "sysdate()";

                $query = Query::insertSQL($data, "BEHAVIOR_DAT");

                $db->query($query);
            }
        }
    }
/*********************************************** subform2より ***********************************************/
    //HTRAINREMARK_DAT の UPDATE(subform2より)
    function getUpdateHtrainDat2($model, $annual, $db) {
        $data = array();
        $data["ANNUAL"][TEXT]      = $annual;
        $data["CLASSACT"][TEXT]    = $model->field2["CLASSACT"];
        $data["STUDENTACT"][TEXT]  = $model->field2["STUDENTACT"];
        $data["CLUBACT"][TEXT]     = $model->field2["CLUBACT"];
        $data["SCHOOLEVENT"][TEXT] = $model->field2["SCHOOLEVENT"];
        $data["REGISTERCD"][TEXT]  = STAFFCD;
        $data["UPDATED"][FUNC]     = "SYSDATE()";

        $where  = " WHERE ";
        $where .= "     YEAR     = '{$model->exp_year}' ";
        $where .= " AND SCHREGNO = '{$model->schregno}'";

        $query = Query::updateSQL($data, "HTRAINREMARK_DAT", $where);
        $db->query($query);
    }

    //HTRAINREMARK_DAT の INSERT(subform2より)
    function getInsertHtrainDat2($model, $annual, $db) {
        $data = array();
        $data["YEAR"][TEXT]        = $model->exp_year;
        $data["SCHREGNO"][TEXT]    = $model->schregno;
        $data["ANNUAL"][TEXT]      = $annual;
        $data["CLASSACT"][TEXT]    = $model->field2["CLASSACT"];
        $data["STUDENTACT"][TEXT]  = $model->field2["STUDENTACT"];
        $data["CLUBACT"][TEXT]     = $model->field2["CLUBACT"];
        $data["SCHOOLEVENT"][TEXT] = $model->field2["SCHOOLEVENT"];
        $data["REGISTERCD"][TEXT]  = STAFFCD;
        $data["UPDATED"][FUNC]     = "SYSDATE()";

        $query = Query::insertSQL($data, "HTRAINREMARK_DAT", $where);
        $db->query($query);
    }
}
?>
