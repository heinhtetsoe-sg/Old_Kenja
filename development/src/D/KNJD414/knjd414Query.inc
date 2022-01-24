<?php

require_once('for_php7.php');

class knjd414Query extends Query {
    //校種取得
    function getSchoolKind($school_kind="") {
        $query  = " SELECT ";
        $query .= "     NAME1 || ':' || ABBV1 AS LABEL, ";
        $query .= "     NAME1 AS VALUE ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'A023' ";
        if ($school_kind) {
            $query .= "     AND NAME1 = '".$school_kind."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     NAMECD2 ";

        return $query;
    }

    //教科一覧
    function getClassMst($model) {
        $query  = " SELECT DISTINCT ";
        $query .= "     T1.CLASSCD || '-' || T1.SCHOOL_KIND AS VALUE, ";
        $query .= "     T1.CLASSCD || '-' || T1.SCHOOL_KIND || '：' || T1.CLASSNAME AS LABEL, ";
        $query .= "     T2.NAMECD2, ";
        $query .= "     T1.CLASSCD ";
        $query .= " FROM ";
        $query .= "     CLASS_MST T1 ";
        $query .= "     LEFT JOIN V_NAME_MST T2 ON T1.SCHOOL_KIND = T2.NAME1 AND";
        $query .= "     T2.NAMECD1 = 'A023'";
        if($model->school_kind != 'ALL'){
            $query .= " WHERE ";
            $query .= "     T1.SCHOOL_KIND = '{$model->school_kind}' ";
        }
        $query .= " ORDER BY ";
        $query .= "     T2.NAMECD2, ";
        $query .= "     T1.CLASSCD ";

        return $query;
    }

    //段階一覧
    function getNameMst($model, $screen) {
        $query  = " SELECT ";
        $query .= "     T1.NAMECD2 AS VALUE, ";
        $query .= "     T1.NAMECD2 || '：' || T1.NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     NAME_MST T1 ";
        $query .= " WHERE ";
        $query .= "     T1.NAMECD1 = 'D110' ";
        if($screen == 'left'){
            if($model->classcd_left != 'ALL'){
                $query .= "   AND T1.NAMESPARE1 = '".substr($model->classcd_left, 3, 1)."' ";
            }
        }
        if($screen == 'right'){
            if($model->classcd != ''){
                $query .= "   AND T1.NAMESPARE1 = '".substr($model->classcd, 3, 1)."' ";
            }
        }
        $query .= " ORDER BY ";
        $query .= "     NAMECD2 ";

        return $query;
    }

    //リスト取得（左フレーム）
    function getList($model, $classcd, $step_cd, $learning_content_cd) {

        $query  = " SELECT ";
        if ($classcd && $step_cd && $learning_content_cd) {
            $query .= "     COUNT(*) AS CNT ";
        } else {
            $query .= "     T2.CLASSCD || '-' || T2.SCHOOL_KIND || '：' || T2.CLASSNAME AS CLASSCD_NAME, ";
            $query .= "     T3.NAME1, ";
            $query .= "     T1.LEARNING_CONTENT_CD || '：' || LEARNING_CONTENT AS LEARNING_CONTENT, ";
            $query .= "     T2.CLASSCD || '-' || T2.SCHOOL_KIND AS CLASSCD, ";
            $query .= "     T1.STEP_CD, ";
            $query .= "     T1.LEARNING_CONTENT_CD, ";
            $query .= "     T1.GUIDANCE_CONTENT_CD, ";
            $query .= "     T1.GUIDANCE_CONTENT, ";
            $query .= "     T2.CLASSCD AS SORT ";
        }
        $query .= " FROM ";
        $query .= "      HREPORT_GUIDANCE_CONTENTS_MST T1 ";
        $query .= "      LEFT JOIN CLASS_MST T2 ";
        $query .= "          ON T1.CLASSCD = T2.CLASSCD ";
        $query .= "         AND T1.SCHOOL_KIND = T2.SCHOOL_KIND ";
        $query .= "      LEFT JOIN NAME_MST T3 ";
        $query .= "          ON T1.STEP_CD = T3.NAMECD2 ";
        $query .= "         AND T3.NAMECD1 = 'D110' ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR             = '".$model->year."' ";
        if($model->field["SCHOOL_KIND"] != 'ALL' && $model->field["SCHOOL_KIND"]) {
            $query .= " AND T1.SCHOOL_KIND = '".$model->field["SCHOOL_KIND"]."' ";
        }
        if($model->field["CLASSCD"] != 'ALL' && $model->field["CLASSCD"]) {
            $query .= " AND T1.CLASSCD || '-' || T1.SCHOOL_KIND = '".$model->field["CLASSCD"]."' ";
        }
        if($model->classcd_left != 'ALL' && $model->classcd_left) {
            $query .= " AND T1.CLASSCD || '-' || T1.SCHOOL_KIND = '".$model->classcd_left."' ";
        }
        if($model->field["STEP_CD"] != 'ALL' && $model->field["STEP_CD"]) {
            $query .= " AND T1.STEP_CD = '".$model->field["STEP_CD"]."' ";
        }
        if($model->step_cd_left != 'ALL' && $model->step_cd_left) {
            $query .= " AND T1.STEP_CD = '".$model->step_cd_left."' ";
        }
        if($classcd) {
            $query .= " AND T1.CLASSCD || '-' || T1.SCHOOL_KIND = '".$classcd."' ";
        }
        if($step_cd) {
            $query .= " AND T1.STEP_CD          = '".$step_cd."' ";
        }
        if($learning_content_cd) {
            $query .= " AND T1.LEARNING_CONTENT_CD          = '".$learning_content_cd."' ";
        }
        if (!$classcd && !$step_cd && !$learning_content_cd) {
            if($model->step_cd_left != 'ALL' && $model->step_cd_left) {
                $query .= " ORDER BY SORT ";
            }
        }
        return $query;
    }

    //選択した学習内容の取得
    function GuidanceContentsMst($model) {
        $db = Query::dbCheckOut();
        
        $query  = " SELECT ";
        $query .= "     CLASSCD || '-' || SCHOOL_KIND AS CLASSCD, ";
        $query .= "     STEP_CD, ";
        $query .= "     LEARNING_CONTENT_CD, ";
        $query .= "     GUIDANCE_CONTENT_CD, ";
        $query .= "     LEARNING_CONTENT, ";
        $query .= "     GUIDANCE_CONTENT ";
        $query .= " FROM ";
        $query .= "     HREPORT_GUIDANCE_CONTENTS_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR                = '".$model->year."' ";
        $query .= " AND CLASSCD || '-' || SCHOOL_KIND             = '".$model->classcd."' ";
        $query .= " AND STEP_CD             = '".$model->step_cd."' ";
        $query .= " AND LEARNING_CONTENT_CD = '".$model->learning_content_cd."' ";

        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);
        return $row;
    }

    //指導内容を取得
    function OneGuidanceContentsMst($model, $guidance_content_cd, $flg) {
        $query  = " SELECT ";
        if($flg == 'one'){
            $query .= "     GUIDANCE_CONTENT ";
        } else {
            $query .= "     COUNT(*) AS CNT ";
        }
        $query .= " FROM ";
        $query .= "     HREPORT_GUIDANCE_CONTENTS_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR                          = '".$model->year."' ";
        $query .= " AND CLASSCD || '-' || SCHOOL_KIND = '".$model->classcd."' ";
        $query .= " AND STEP_CD                       = '".$model->step_cd."' ";
        $query .= " AND LEARNING_CONTENT_CD           = '".$model->learning_content_cd."' ";
        if($flg == 'one'){
            $query .= " AND GUIDANCE_CONTENT_CD           = '".$guidance_content_cd."' ";
        }

        return $query;
    }

    //追加
    function &getInsertQuery($model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);
        $classcd     = substr($model->field["CLASSCD"], 0 ,2);
        $school_kind = substr($model->field["CLASSCD"], 3 ,1);

        for ($i = 1; $i <= $model->field["GUIDANCE_CONTENT_CD"]; $i++) {

            $data = array();
            $data["YEAR"][TEXT]                = $model->year ;
            $data["CLASSCD"][TEXT]             = $classcd;
            $data["SCHOOL_KIND"][TEXT]         = $school_kind;
            $data["STEP_CD"][TEXT]             = $model->field["STEP_CD"];
            $data["LEARNING_CONTENT_CD"][TEXT] = $model->field["LEARNING_CONTENT_CD"];
            $data["GUIDANCE_CONTENT_CD"][TEXT] = $i;
            $data["LEARNING_CONTENT"][TEXT]    = $model->field["LEARNING_CONTENT"];
            $data["GUIDANCE_CONTENT"][TEXT]    = $model->fields["GUIDANCE_CONTENT".$i];
            $data["REGISTERCD"][TEXT]          = STAFFCD;
            $data["UPDATED"][FUNC]             = "sysdate()";

            $query = Query::insertSQL($data, "HREPORT_GUIDANCE_CONTENTS_MST");
            $db->query($query);
        }

        $db->commit();
        Query::dbCheckIn($db);
        return;
    }

    //更新
    function &UpdateQuery($model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);
        $classcd     = substr($model->field["CLASSCD"], 0 ,2);
        $school_kind = substr($model->field["CLASSCD"], 3 ,1);

        //削除
        $query  = " DELETE FROM  ";
        $query .= "     HREPORT_GUIDANCE_CONTENTS_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR                = '".$model->year."' ";
        $query .= " AND CLASSCD             = '".$classcd."' ";
        $query .= " AND SCHOOL_KIND         = '".$school_kind."' ";
        $query .= " AND STEP_CD             = '".$model->field["STEP_CD"]."' ";
        $query .= " AND LEARNING_CONTENT_CD = '".$model->field["LEARNING_CONTENT_CD"]."' ";

        $db->query($query);

        //追加
        for ($i = 1; $i <= $model->field["GUIDANCE_CONTENT_CD"]; $i++) {

            $data = array();
            $data["YEAR"][TEXT]                = $model->year ;
            $data["CLASSCD"][TEXT]             = $classcd;
            $data["SCHOOL_KIND"][TEXT]         = $school_kind;
            $data["STEP_CD"][TEXT]             = $model->field["STEP_CD"];
            $data["LEARNING_CONTENT_CD"][TEXT] = $model->field["LEARNING_CONTENT_CD"];
            $data["GUIDANCE_CONTENT_CD"][TEXT] = $i;
            $data["LEARNING_CONTENT"][TEXT]    = $model->field["LEARNING_CONTENT"];
            $data["GUIDANCE_CONTENT"][TEXT]    = $model->fields["GUIDANCE_CONTENT".$i];
            $data["REGISTERCD"][TEXT]          = STAFFCD;
            $data["UPDATED"][FUNC]             = "sysdate()";

            $query = Query::insertSQL($data, "HREPORT_GUIDANCE_CONTENTS_MST");
            $db->query($query);
        }

        $db->commit();      // トランザクションをコミットする。
        Query::dbCheckIn($db);
        return;
    }

    //削除
    function &DeleteQuery($model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //削除 HREPORT_GUIDANCE_CONTENTS_MST
        $query  = " DELETE FROM  ";
        $query .= "     HREPORT_GUIDANCE_CONTENTS_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR                          = '".$model->year."'  ";
        $query .= " AND CLASSCD || '-' || SCHOOL_KIND = '".$model->field["CLASSCD"]."' ";
        $query .= " AND STEP_CD                       = '".$model->field["STEP_CD"]."' ";
        $query .= " AND LEARNING_CONTENT_CD           = '".$model->field["LEARNING_CONTENT_CD"]."' ";

        $db->query($query);

        $db->commit();
        Query::dbCheckIn($db);
        return;
    }

    //前年度データ取得
    function getCopyData($year, $flg) {
        $query  =  " SELECT ";
        if ($flg == "cnt") {
            $query .=  "    COUNT(*) ";
        } else {
            $query .=  "    * ";
        }
        $query .=  " FROM ";
        $query .=  "    HREPORT_GUIDANCE_CONTENTS_MST ";
        $query .=  " WHERE ";
        $query .=  "    YEAR = '".$year."' ";

        return $query;
    }

    //前年度コピー処理
    function &getCopyQuery($model, $pre_year) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //削除
        $query  =  " DELETE FROM ";
        $query .=  "    HREPORT_GUIDANCE_CONTENTS_MST ";
        $query .=  " WHERE ";
        $query .=  "    YEAR = '".$model->year."' ";
        $db->query($query);

        //前年度データ取得
        $query = knjd414Query::getCopyData($pre_year, "list");
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //追加

            $data = array();
            $data["YEAR"][TEXT]                = $model->year ;
            $data["CLASSCD"][TEXT]             = $row["CLASSCD"];
            $data["SCHOOL_KIND"][TEXT]         = $row["SCHOOL_KIND"];
            $data["STEP_CD"][TEXT]             = $row["STEP_CD"];
            $data["LEARNING_CONTENT_CD"][TEXT] = $row["LEARNING_CONTENT_CD"];
            $data["GUIDANCE_CONTENT_CD"][TEXT] = $row["GUIDANCE_CONTENT_CD"];
            $data["LEARNING_CONTENT"][TEXT]    = $row["LEARNING_CONTENT"];
            $data["GUIDANCE_CONTENT"][TEXT]    = $row["GUIDANCE_CONTENT"];
            $data["REGISTERCD"][TEXT]          = STAFFCD;
            $data["UPDATED"][FUNC]             = "sysdate()";

            $query = Query::insertSQL($data, "HREPORT_GUIDANCE_CONTENTS_MST");
            $db->query($query);
        }

        $db->commit();
        Query::dbCheckIn($db);
    }
}
?>
