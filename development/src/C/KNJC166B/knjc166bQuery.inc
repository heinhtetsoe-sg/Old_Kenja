<?php

require_once('for_php7.php');
class knjc166bQuery extends Query {

    //学年取得
    function getGrade($model) {
        $query  = " SELECT DISTINCT ";
        $query .= "     GRADE AS VALUE, ";
        $query .= "     GRADE_NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "     AND SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        } else if ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "     AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //年組取得
    function getHrClass($model) {
        $query  = " SELECT DISTINCT ";
        $query .= "     T1.GRADE || T1.HR_CLASS AS VALUE, ";
        $query .= "     T1.HR_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT T1 ";
        $query .= "     INNER JOIN SCHREG_REGD_GDAT T2 ";
        $query .= "          ON T2.YEAR         = T1.YEAR ";
        $query .= "         AND T2.GRADE        = T1.GRADE ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR     = '".CTRL_YEAR."' ";
        $query .= "     AND T1.GRADE = '".$model->field["GRADE"]."' ";
        $query .= "     AND T1.SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //皆勤区分取得
    function getKaikinCd($model) {
        $query  = " SELECT ";
        $query .= "     T1.KAIKIN_CD AS VALUE ";
        $query .= "     , RIGHT ('0' || T1.KAIKIN_CD, 2) || ':' || T1.KAIKIN_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     KAIKIN_MST T1 ";
        $query .= " INNER JOIN ";
        $query .= "     KAIKIN_GRADE_MST T2 ";
        $query .= " ON ";
        $query .= "     T1.KAIKIN_CD = T2.KAIKIN_CD ";
        $query .= " WHERE ";
        $query .= "     T2.GRADE = '".$model->field["GRADE"]."' ";
        $query .= "  ORDER BY ";
        $query .= "    T1.KAIKIN_CD, T2.GRADE ";

        return $query;
    }

    //リスト取得
    function selectQuery($model) {
        $kaikinCd =  ($model->field["KAIKIN_CD"]) ? $model->field["KAIKIN_CD"] : "NULL";

        $query  = " SELECT ";
        $query .= "     REG.SCHREGNO ";
        $query .= "     , REG.ATTENDNO ";
        $query .= "     , BASE.NAME ";
        $query .= "     , KAI_D.KAIKIN_FLG ";
        $query .= "     , KAI_D.INVALID_FLG ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT REG ";
        $query .= "     INNER JOIN SCHREG_BASE_MST BASE ";
        $query .= "         ON BASE.SCHREGNO = REG.SCHREGNO ";
        $query .= "     LEFT JOIN KAIKIN_DAT KAI_D ";
        $query .= "         ON KAI_D.YEAR = REG.YEAR ";
        $query .= "         AND KAI_D.SCHREGNO = REG.SCHREGNO ";
        $query .= "         AND KAI_D.KAIKIN_CD = ".$kaikinCd." ";
        $query .= " WHERE ";
        $query .= "     REG.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND REG.GRADE || REG.HR_CLASS = '".$model->field["HR_CLASS"]."' ";
        $query .= "     AND REG.SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= " ORDER BY ";
        $query .= "     REG.ATTENDNO ";

        return $query;
    }
    
    //皆勤データの存在チェック
    function getKaikinDat($model, $schregno) {

        $query  = " SELECT ";
        $query .= "     COUNT(*) AS COUNT ";
        $query .= " FROM ";
        $query .= "     KAIKIN_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND SCHREGNO = '".$schregno."' ";
        $query .= "     AND KAIKIN_CD = '".$model->field["KAIKIN_CD"]."' ";

        return $query;
    }

    /* 皆勤データの更新 */
    function getUpdateQuery($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        foreach ($model->data["SCHREGNO"] as $key => $schregno) {
            
            $getCount = $db->getOne(knjc166bQuery::getKaikinDat($model, $schregno));

            $kaikinFlg = VARS::post("KAIKIN_FLG_".$schregno) ? VARS::post("KAIKIN_FLG_".$schregno) : 0;
            $invalidFlg = VARS::post("INVALID_FLG_".$schregno) ? VARS::post("INVALID_FLG_".$schregno) : 0;

            //項目
            $data = array();
            $data["YEAR"][TEXT]         = CTRL_YEAR;
            $data["SCHREGNO"][TEXT]     = $schregno;
            $data["KAIKIN_CD"][TEXT]    = $model->field["KAIKIN_CD"];
            $data["KAIKIN_FLG"][TEXT]   = $kaikinFlg;
            $data["INVALID_FLG"][TEXT]  = $invalidFlg;
            $data["REGISTERCD"][TEXT]   = STAFFCD ;
            $data["UPDATED"][FUNC]      = "sysdate()";
            
            if ($getCount == 0) {
                if ($kaikinFlg != '0' || $invalidFlg != '0') {
                    //追加
                    $query = Query::insertSQL($data, "KAIKIN_DAT");
                    $db->query($query);
                }
            } else {
                //更新
                $where  = "WHERE ";
                $where .= "      YEAR = '".CTRL_YEAR."' ";
                $where .= "  AND SCHREGNO = '".$schregno."' ";
                $where .= "  AND KAIKIN_CD = '".$model->field["KAIKIN_CD"]."' ";
                $query = Query::updateSQL($data, "KAIKIN_DAT", $where);
                $db->query($query);
            }
        }

        $db->commit();
        Query::dbCheckIn($db);
    }
}
?>
