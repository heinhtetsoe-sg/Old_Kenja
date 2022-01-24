<?php

require_once('for_php7.php');

class knjz242Query extends Query {
    //年度取得
    function getYear() {
        $query  = " WITH CTRL_T(VALUE, LABEL) AS ( ";
        $query .= "     VALUES('".CTRL_YEAR."', '".CTRL_YEAR."') ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     YEAR AS VALUE,";
        $query .= "     YEAR AS LABEL ";
        $query .= " FROM ";
        $query .= "     MEDEXAM_DET_NOT_EXAMINED_DAT ";
        $query .= " GROUP BY ";
        $query .= "     YEAR ";
        $query .= " UNION ";
        $query .= " SELECT ";
        $query .= "     VALUE,";
        $query .= "     LABEL ";
        $query .= " FROM ";
        $query .= "     CTRL_T ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //学校名取得
    function getSchoolName() {
        $query  = " SELECT ";
        $query .= "     NAME1 ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'Z010' ";
        $query .= " AND NAMECD2 = '00' ";
        
        return $query;
    }

    //学年取得
    function getGdat($model, $kindFlg="") {
        $query  = "";
        $query .= " SELECT ";
        if ($kindFlg) {
            $query .= "     SCHOOL_KIND ";
        } else {
            $query .= "     GRADE AS VALUE,";
            $query .= "     GRADE_NAME1 AS LABEL ";
        }
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$model->field["YEAR"]."' ";
        if ($kindFlg) {
            $query .= "     AND GRADE = '".$model->field["GRADE"]."' ";
        } else {
            if ($model->Properties["use_prg_schoolkind"] == "1") {
                if ($model->selectSchoolKind) {
                    $query .= " AND SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
                }
            } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
                $query .= " AND SCHOOL_KIND = '".SCHOOLKIND."' ";
            }
            $query .= " ORDER BY ";
            $query .= "     VALUE ";
        }

        return $query;
    }

    //１レコード取得
    function getMedexamDetNotExaminedDat($model) {
        $query  = " SELECT ";
        $query .= "     case when R_BAREVISION      = '1' then '1' else '' end as VISION, ";    // 視力
        $query .= "     case when R_EAR             = '1' then '1' else '' end as EAR, ";       // EAR
        $query .= "     case when ALBUMINURIA1CD    = '1' then '1' else '' end as URI, ";       // 聴力
        $query .= "     case when EYEDISEASECD      = '1' then '1' else '' end as EYE, ";       // 目の疾病及び異常
        $query .= "     case when HEART_MEDEXAM     = '1' then '1' else '' end as HEART, ";     // 心臓
        $query .= "     case when TB_DATE           = '1' then '1' else '' end as TB_, ";       // 結核
        $query .= "     case when DOC_CD            = '1' then '1' else '' end as DOC_, ";      // 学校医
        $query .= "     case when MEDICAL_HISTORY1  = '1' then '1' else '' end as MEDICAL_HISTORY, "; // 既往症
        $query .= "     T1.* ";
        $query .= " FROM ";
        $query .= "     MEDEXAM_DET_NOT_EXAMINED_DAT T1 ";
        $query .= " WHERE ";
        $query .= "         YEAR  = '".$model->field["YEAR"]."' ";
        $query .= "     AND GRADE = '".$model->field["GRADE"]."' ";

        return $query;
    }

    /**************/
    /** 更新処理 **/
    /**************/
    //更新
    function getUpdateQuery(&$model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $query  = " DELETE FROM MEDEXAM_DET_NOT_EXAMINED_DAT ";
        $query .= " WHERE YEAR  = '".$model->field["YEAR"]."' ";
        $query .= "   AND GRADE = '".$model->field["GRADE"]."' ";
        $db->query($query);

        $data = array();
        $data["YEAR"][TEXT]     = $model->field["YEAR"];
        $data["GRADE"][TEXT]    = $model->field["GRADE"];
        foreach($model->fieldArr as $field => $fieldVal) {
            if ($fieldVal == "1") {
                continue;
            }
            $data[$field][TEXT]     = $model->field[$field];
        }
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][FUNC]          = "sysdate()";

        $query = Query::insertSQL($data, "MEDEXAM_DET_NOT_EXAMINED_DAT");
        $db->query($query);

        $db->commit();
        Query::dbCheckIn($db);

        return true;
    }

    /****************/
    /** コピー処理 **/
    /****************/
    //今年度のデータが存在するかチェック
    function CheckData($year) {
        $db = Query::dbCheckOut();

        $query  = " SELECT 'X' FROM MEDEXAM_DET_NOT_EXAMINED_DAT ";
        $query .= " WHERE YEAR = '".$year."'";
        $row = $db->getOne($query);

        Query::dbCheckIn($db);
        return $row;
    }

    function &getCopyQuery($model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $setField = $sep = "";
        foreach ($model->fieldArr as $field => $fieldVal) {
            if ($fieldVal == "1") continue;

            $setField .= $sep.$field;
            $sep = ",";
        }

        $query  = " INSERT INTO MEDEXAM_DET_NOT_EXAMINED_DAT ";
        $query .= "           ( YEAR, ";
        $query .= "             GRADE, ";
        $query .= "             {$setField}, ";
        $query .= "             REGISTERCD, ";
        $query .= "             UPDATED ) ";
        $query .= "   (SELECT ";
        $query .= "         '".$model->field["YEAR"]."', ";
        $query .= "         GRADE, ";
        $query .= "         {$setField}, ";
        $query .= "         '".STAFFCD."', ";
        $query .= "         sysdate() ";
        $query .= "     FROM ";
        $query .= "         MEDEXAM_DET_NOT_EXAMINED_DAT ";
        $query .= "     WHERE ";
        $query .= "         YEAR = '".($model->field["YEAR"] - 1)."'";
        $query .= "     ) ";
        $db->query($query);

        $db->commit();
        Query::dbCheckIn($db);
        return;
    }
}
?>