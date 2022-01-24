<?php

require_once('for_php7.php');

class knje012yQuery extends Query {

    //調査書所見ヘッダーデータ
    function getHexamEntremarkHdat($model) {
        $query  = " SELECT ";
        $query .= "   SCHREGNO, ";
        $query .= "   REMARK ";
        $query .= " FROM ";
        if ($model->mode == "grd") {     //卒業
            $query .= "   GRD_HEXAM_ENTREMARK_J_HDAT ";
        } else {
            $query .= "   HEXAM_ENTREMARK_J_HDAT ";
        }
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '{$model->schregno}' ";

        return $query;
    }

    //調査書所見データ
    function getHexamEntremarkDat($model) {
        $query  = " SELECT ";
        $query .= "   SCHREGNO, ";
        $query .= "   ATTENDREC_REMARK, ";
        $query .= "   TOTALSTUDYVAL, ";
        $query .= "   CALSSACT, ";
        $query .= "   STUDENTACT, ";
        $query .= "   SCHOOLEVENT, ";
        $query .= "   CLUBACT ";
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

    //指導要録所見データ
    function getHtrainremarkDat($model) {
        $query  = " SELECT ";
        $query .= "     SCHREGNO, ";
        $query .= "     TOTALSTUDYVAL AS SIDOU_TOTALSTUDYVAL, ";
        $query .= "     TOTALREMARK AS SIDOU_TOTALREMARK ";
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

    //「出欠の記録～」の学年（年次）コンボボックス
    function getYearAnnual($model) {
        $query  = " WITH SUB_MAIN AS ( ";
        $query .= " SELECT DISTINCT ";
        $query .= "     T1.YEAR, ";
        $query .= "     T1.ANNUAL, ";
        $query .= "     T1.SCHREGNO ";
        $query .= " FROM ";
        $query .= "     SCHREG_STUDYREC_DAT T1 ";
        $query .= " UNION ";
        $query .= " SELECT DISTINCT ";
        $query .= "     T2.YEAR, ";
        $query .= "     T2.ANNUAL, ";
        $query .= "     T2.SCHREGNO ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T2 ";
        $query .= " ) ";
        $query .= " SELECT DISTINCT ";
        $query .= "     YEAR, ";
        $query .= "     ANNUAL ";
        $query .= " FROM ";
        $query .= "     SUB_MAIN ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '{$model->schregno}' ";
        $query .= " ORDER BY ";
        $query .= "     YEAR, ";
        $query .= "     ANNUAL ";

        return $query;
    }

    //通知表所見より年度を取得
    function getHreportremarkYear($model)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     YEAR ";
        $query .= " FROM ";
        $query .= "     HREPORTREMARK_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '".$model->schregno ."' ";
        $query .= " ORDER BY ";
        $query .= "     YEAR DESC ";

        return $query;
    }

    //通知表所見データ取得
    function getHreportremarkDat($model)
    {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     HREPORTREMARK_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR     = '".$model->year_cmb."' AND ";
        $query .= "     SCHREGNO = '".$model->schregno."' ";
        $query .= " ORDER BY ";
        $query .= "     SEMESTER ";

        return $query;
    }

    //学期取得
    function getSemesterMst($model)
    {
        $query  = " SELECT ";
        $query .= "     SEMESTER ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR     = '".$model->year_cmb."' AND ";
        $query .= "     SEMESTER <> '9' ";

        return $query;
    }

    //項目取得
    function getNameMst($model)
    {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR    = '".$model->year_cmb."' AND ";
        $query .= "     NAMECD1 = 'D034' ";

        return $query;
    }

    //通知表所見詳細データ取得
    function getHreportremarkDetailDat($model, $flg="")
    {
        $query  = " SELECT ";
        $query .= "     CODE, ";
        $query .= "     REMARK1 ";
        $query .= " FROM ";
        $query .= "     HREPORTREMARK_DETAIL_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".$model->year_cmb."' AND ";
        $query .= "     SEMESTER    = '9' AND ";
        $query .= "     SCHREGNO    = '".$model->schregno."' AND ";
        if ($flg) {
            $query .= "     DIV     = '03' AND ";
            $query .= "     CODE IN ('03', '04') ";
        } else {
            $query .= "     DIV     = '01' ";
        }
        $query .= " ORDER BY ";
        $query .= "     CODE ";

        return $query;
    }

    /* 更新処理 */
    function update(&$db, $model) {
        /********/
        /* HDAT */
        /********/
        //更新テーブル
        $tableName = "HEXAM_ENTREMARK_J_HDAT";
        if ($model->mode == "grd") {     //卒業
            $tableName = "GRD_HEXAM_ENTREMARK_J_HDAT";
        }
        //更新フィールド
        $data = array();
        $data["SCHREGNO"][TEXT]         = $model->schregno;
        $data["REMARK"][TEXT]           = $model->field["REMARK"];
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][FUNC]          = "SYSDATE()";
        //条件
        $where = "WHERE SCHREGNO = '{$model->schregno}' ";
        if (0 < $db->getOne("SELECT COUNT(*) FROM {$tableName} " .$where)) {
            $query = Query::updateSQL($data, $tableName, $where);
        } else {
            $query = Query::insertSQL($data, $tableName);
        }
        $db->query($query);
        /*******/
        /* DAT */
        /*******/
        //更新テーブル
        $tableName = "HEXAM_ENTREMARK_DAT";
        if ($model->mode == "grd") {     //卒業
            $tableName = "GRD_HEXAM_ENTREMARK_DAT";
        }
        //更新フィールド
        $data = array();
        $data["YEAR"][TEXT]             = $model->annual["YEAR"];
        $data["SCHREGNO"][TEXT]         = $model->schregno;
        $data["ANNUAL"][TEXT]           = $model->annual["ANNUAL"];
        $data["ATTENDREC_REMARK"][TEXT] = $model->field["ATTENDREC_REMARK"];
        $data["TOTALSTUDYVAL"][TEXT]    = $model->field["TOTALSTUDYVAL"];
        $data["CALSSACT"][TEXT]         = $model->field["CALSSACT"];
        $data["STUDENTACT"][TEXT]       = $model->field["STUDENTACT"];
        $data["SCHOOLEVENT"][TEXT]      = $model->field["SCHOOLEVENT"];
        $data["CLUBACT"][TEXT]          = $model->field["CLUBACT"];
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][FUNC]          = "SYSDATE()";
        //条件
        $where = "WHERE YEAR = '{$model->annual["YEAR"]}' AND SCHREGNO = '{$model->schregno}' ";
        if (0 < $db->getOne("SELECT COUNT(*) FROM {$tableName} " .$where)) {
            $query = Query::updateSQL($data, $tableName, $where);
        } else {
            $query = Query::insertSQL($data, $tableName);
        }
        $db->query($query);
    }
}
?>
