<?php

require_once('for_php7.php');

class knje012aQuery extends Query {

    //対象データ取得
    function selectQuery($model) {
        $query  =" SELECT ";
        $query .= "   T0.ANNUAL, ";
        $query .= "   T1.*, ";
        $query .= "   T2.ATTENDREC_REMARK ";
        $query .= " FROM ";
        $query .= "   SCHREG_REGD_DAT T0 ";
        $query .= "   LEFT JOIN HEXAM_ENTREMARK_J_HDAT T1 ON T1.SCHREGNO = T0.SCHREGNO ";
        $query .= "   LEFT JOIN HEXAM_ENTREMARK_DAT T2 ON T2.SCHREGNO = T0.SCHREGNO ";
        $query .= "                                   AND T2.YEAR = T0.YEAR ";
        $query .= " WHERE ";
        $query .= "     T0.SCHREGNO = '{$model->schregno}' ";
        $query .= " AND T0.YEAR     = '{$model->exp_year}' ";
        $query .= " AND T0.SEMESTER = '{$model->exp_semester}' ";

        return $query;
    }
    
    //更新年次取得
    function getAnnual($model) {
        $query  =" SELECT ";
        $query .= "     ANNUAL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR     = '{$model->exp_year}' ";
        $query .= " AND SCHREGNO = '{$model->schregno}' ";
        $query .= " AND SEMESTER = '{$model->exp_semester}' ";
        
        return $query;
    }
    
    //更新対象確認用1
    function getHexamEntremarkHdat($model) {
        $query  =" SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     HEXAM_ENTREMARK_J_HDAT ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '{$model->schregno}' ";

        return $query;
    }
    
    //更新対象確認用2
    function getHexamEntremarkDat($model) {
        $query  =" SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     HEXAM_ENTREMARK_DAT T1";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '{$model->schregno}' ";
        $query .= " AND YEAR = '".CTRL_YEAR."' ";

        return $query;
    }

    //フォーム2の値を取得
    function selectQueryForm2($model) {
        $query  = " SELECT ";
        $query .= "   * ";
        $query .= " FROM ";
        $query .= "   HTRAINREMARK_DAT ";
        $query .= " WHERE ";
        if ($model->annual["YEAR"] !== '0') {
            $query .= "       YEAR     = '{$model->annual["YEAR"]}' ";
        } else {
            $query .= "       YEAR     = '{$model->exp_year}' ";
        }
        $query .= "   AND SCHREGNO = '{$model->schregno}' ";

        return $query;
    }

    //学年（年次）コンボボックス
    function selectQueryAnnual_knje012aForm2($model) {
        $query  = " WITH SUB_MAIN AS ( ";
        $query .= " SELECT DISTINCT ";
        $query .= "     T1.YEAR, ";
        $query .= "     T1.ANNUAL, ";
        $query .= "     T1.SCHREGNO ";
        $query .= " FROM ";
        $query .= "     HTRAINREMARK_DAT T1 ";
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

    /* 更新処理 */
    function update(&$db, $model) {

        $query = knje012aQuery::getHexamEntremarkHdat($model);
        $count1 = $db->getOne($query);

        $data = array();
        $data["SCHREGNO"][TEXT]          = $model->schregno;
        $data["TOTALSTUDYVAL"][TEXT]     = $model->field["TOTALSTUDYVAL"];
        $data["SPECIALACTREC"][TEXT]     = $model->field["SPECIALACTREC"];
        $data["BEHAVEREC_REMARK"][TEXT]  = $model->field["BEHAVEREC_REMARK"];
        $data["TRIN_REF"][TEXT]          = $model->field["TRIN_REF"];
        $data["HEALTHREC"][TEXT]         = $model->field["HEALTHREC"];
        $data["REGISTERCD"][TEXT]        = STAFFCD;
        if ($count1 > 0) {
            $where  = " WHERE ";
            $where .= "     SCHREGNO  = '{$model->schregno}' ";
        }
        if ($count1 > 0) {
            $query = Query::updateSQL($data, "HEXAM_ENTREMARK_J_HDAT", $where);
        } else {
            $query = Query::insertSQL($data, "HEXAM_ENTREMARK_J_HDAT");
        }
        $db->query($query);
    }

    /* 更新処理 */
    function update2(&$db, $model) {
    
        $query = knje012aQuery::getHexamEntremarkDat($model);
        $count2 = $db->getOne($query);
        $query = knje012aQuery::getAnnual($model);
        $annual = $db->getOne($query);
    
        $data = array();
        $data["YEAR"][TEXT]             = CTRL_YEAR;
        $data["SCHREGNO"][TEXT]         = $model->schregno;
        $data["ANNUAL"][TEXT]           = $annual;
        $data["ATTENDREC_REMARK"][TEXT] = $model->field["ATTENDREC_REMARK"];
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        if ($count2 > 0) {
            $where  = " WHERE ";
            $where .= "     YEAR = '{$model->exp_year}' ";
            $where .= " AND SCHREGNO    = '{$model->schregno}' ";
        }
        if ($count2 > 0) {
            $query = Query::updateSQL($data, "HEXAM_ENTREMARK_DAT", $where);
        } else {
            $query = Query::insertSQL($data, "HEXAM_ENTREMARK_DAT");
        }
        $db->query($query);
    }
}
?>
