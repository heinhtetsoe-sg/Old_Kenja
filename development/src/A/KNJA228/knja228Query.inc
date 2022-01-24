<?php

require_once('for_php7.php');

class knja228Query extends Query {

    //年度一覧
    function getNendo() {
        $query .= " select distinct ";
        $query .= "     YEAR as LABEL, ";
        $query .= "     YEAR as VALUE";
        $query .= " from ";
        $query .= "     SCHOOL_MST ";
        $query .= " ORDER BY ";
        $query .= "     VALUE DESC ";

        return $query;
    }

    //出身中学リスト取得
    function getFinSchoolList($model, $ignorelist, $sellist) {
        $query  = " SELECT ";
        $query .= "   FY.FINSCHOOLCD AS VALUE, ";
        $query .= "   FM.FINSCHOOL_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "   FINSCHOOL_YDAT FY ";
        $query .= "   LEFT JOIN FINSCHOOL_MST FM ";
        $query .= "     ON FM.FINSCHOOLCD = FY.FINSCHOOLCD ";
        $query .= " WHERE ";
        $query .= "   FY.YEAR = '".$model->year."' ";
        $query .= "   AND FM.FINSCHOOL_TYPE IN ('3', '5') "; //帳票は高校が対象なので、選択対象は中学のみ抽出
        if (!is_null($ignorelist) && is_array($ignorelist) && get_count($ignorelist) > 0) {
            $lstr = implode("','", $ignorelist);
            if ($lstr != "") {
                $query .= "     AND FY.FINSCHOOLCD NOT IN ('".$lstr."') ";
            }
        } else if (!is_null($sellist) && is_array($sellist) && get_count($sellist) > 0) {
            $lstr = implode("','", $sellist);
            if ($lstr != "") {
                $query .= "     AND FY.FINSCHOOLCD IN ('".$lstr."') ";
            }
        }
        $query .= " ORDER BY ";
        $query .= "   FY.FINSCHOOLCD ";
        return $query;
    }

    //塾リスト取得
    function getPriSchoolList($model, $ignorelist, $sellist) {
        $query  = " SELECT ";
        $query .= "   PY.PRISCHOOLCD || (CASE WHEN PCM.PRISCHOOL_CLASS_CD IS NOT NULL THEN '-' || PCM.PRISCHOOL_CLASS_CD ELSE '' END) AS VALUE, ";
        $query .= "   PM.PRISCHOOL_NAME || (CASE WHEN PCM.PRISCHOOL_NAME IS NOT NULL THEN '(' || PCM.PRISCHOOL_NAME  || ')' ELSE '' END) AS LABEL ";
        $query .= " FROM ";
        $query .= "   PRISCHOOL_YDAT PY ";
        $query .= "   LEFT JOIN PRISCHOOL_MST PM ";
        $query .= "     ON PM.PRISCHOOLCD = PY.PRISCHOOLCD ";
        $query .= "   LEFT JOIN PRISCHOOL_CLASS_MST PCM ";
        $query .= "     ON PCM.PRISCHOOLCD = PM.PRISCHOOLCD ";
        $query .= " WHERE ";
        $query .= "   PY.YEAR = '".$model->year."' ";
        if (!is_null($ignorelist) && is_array($ignorelist) && get_count($ignorelist) > 0) {
            $lstr = implode("','", $ignorelist);
            if ($lstr != "") {
                $query .= "     AND PY.PRISCHOOLCD NOT IN ('".$lstr."') ";
            }
        } else if (!is_null($sellist) && is_array($sellist) && get_count($sellist) > 0) {
            $lstr = implode("','", $sellist);
            if ($lstr != "") {
                $query .= "     AND PY.PRISCHOOLCD IN ('".$lstr."') ";
            }
        }
        $query .= " ORDER BY ";
        $query .= "   PY.PRISCHOOLCD ";
        return $query;
    }

    //選択する情報(リスト右側)
    function getSelectInfo($model) {
        $sellist = $model->selected;
        if ($model->disppattern == "1") {
            //出身中学
            return knja228Query::getFinSchoolList($model, $sellist, null);
        } else {
            //塾
            return knja228Query::getPriSchoolList($model, $sellist, null);
        }
    }

    //リスト左側の情報
    function getSelect($model) {
        $sellist = $model->selected;
        if ($model->disppattern == "1") {
            //出身中学
            return knja228Query::getFinSchoolList($model, null, $sellist);
        } else {
            //塾
            return knja228Query::getPriSchoolList($model, null, $sellist);
        }
    }

//    //塾名
//    function getPriSchoolName($priSchoolCd) {
//        $query  = " SELECT ";
//        $query .= "     PRISCHOOL_NAME ";
//        $query .= " FROM ";
//        $query .= "     PRISCHOOL_MST ";
//        $query .= " WHERE ";
//        $query .= "     PRISCHOOLCD = '{$priSchoolCd}' ";
//        return $query;
//    }
//    //教室名
//    function getPriSchoolClassName($priSchoolCd, $priSchoolClassCd) {
//        $query  = " SELECT ";
//        $query .= "     PRISCHOOL_NAME ";
//        $query .= " FROM ";
//        $query .= "     PRISCHOOL_CLASS_MST ";
//        $query .= " WHERE ";
//        $query .= "         PRISCHOOLCD        = '{$priSchoolCd}' ";
//        $query .= "     AND PRISCHOOL_CLASS_CD = '{$priSchoolClassCd}' ";
//        
//        return $query;
//    }

//    //登録チェック
//    function chkDataQuery($model, $param) {
//        $db = Query::dbCheckOut();
//
//        $query .= " SELECT ";
//        $query .= "     COUNT(*) AS CNT ";
//        $query .= "  FROM ";
//        $query .= "     ANOTHER_PRISCHOOL_HIST_DAT ";
//        $query .= " WHERE SCHREGNO = '" . $param['SCHREGNO'] . "'";
//        $query .= "   AND SCHOOL_KIND = '" . $param['SCHOOL_KIND'] . "' ";
//        $query .= "   AND PRISCHOOLCD = '" . $param['PRISCHOOLCD'] . "' ";
//        $query .= "   AND PRISCHOOL_CLASS_CD = '" . $param['PRISCHOOL_CLASS_CD'] . "' ";
//        $query .= "   AND REGISTERCD = '" . $param['REGISTERCD'] . "' ";
//
//        $retval =$db->getOne($query);
//        Query::dbCheckIn($db);
//        return $retval;
//    }

//    //新規挿入
//    function insertQuery($model, $schregNo) {
//        $db = Query::dbCheckOut();
//        $db->autoCommit(false);
//        
//        $data = array();
//        
//        $data["SCHREGNO"][TEXT]             = $schregNo;
//        $data["YEAR"][TEXT]                 = $model->year;
//        $data["PRISCHOOLCD"][TEXT]          = $model->preischoolcd;
//        $data["PRISCHOOL_CLASS_CD"][TEXT]   = $model->preischoolClassCd;
//        $data["REGISTERCD"][TEXT]           = STAFFCD;
//        $data["UPDATED"][FUNC]              = "sysdate()";
//
//        $query = Query::insertSQL($data, "ANOTHER_PRISCHOOL_HIST_DAT");
//        $db->query($query);
//
//        $db->commit();
//        Query::dbCheckIn($db);
//        return true;
//    }
//
//    //削除
//    function deleteQuery($model) {
//        $db = Query::dbCheckOut();
//
//        $query  = " DELETE FROM ";
//        $query .= "     ANOTHER_PRISCHOOL_HIST_DAT ";
//        $query .= " WHERE ";
//        $query .= "     YEAR  = '{$model->year}' ";
//        $query .= "     and PRISCHOOLCD  = '{$model->preischoolcd}' ";
//        $query .= "     and PRISCHOOL_CLASS_CD  = '$model->preischoolClassCd' ";
//
//        $db->query($query);
//        Query::dbCheckIn($db);
//        return true;
//    }
}
?>