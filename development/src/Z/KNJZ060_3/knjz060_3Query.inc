<?php

require_once('for_php7.php');

class knjz060_3Query extends Query {

    //CLASS_YDAT 年度取得
    function getYear($model)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "      YEAR AS LABEL, ";
        $query .= "      YEAR AS VALUE ";
        $query .= " FROM ";
        $query .= "      CLASS_YDAT ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " WHERE SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " WHERE SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " ORDER BY ";
        $query .= "      VALUE DESC";
        
        return $query;
    }

    //学校校種コンボ
    function getNamecd($model, $cd)
    {
        $query  = " SELECT  ";
        $query .= "      NAME1 AS VALUE, ";
        $query .= "      NAME1 || '：' || ABBV1 AS LABEL ";
        $query .= " FROM ";
        $query .= "      NAME_MST ";
        $query .= " WHERE ";
        $query .= "      NAMECD1 = '".$cd."' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " AND NAME1 IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND NAME1 = '".SCHOOLKIND."' ";
        }
        $query .= " ORDER BY ";
        $query .= "      VALUE ";
        
        return $query;
    }

    //CLASS_MST データ取得
    function getData($model)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "      Y1.YEAR, ";
        $query .= "      T1.*, ";
        $query .= "      CASE WHEN L1.CLASS_SEQ IS NOT NULL THEN '有効' ELSE NULL END AS SEQ_001, ";
        $query .= "      CASE WHEN L2.CLASS_SEQ IS NOT NULL THEN '有効' ELSE NULL END AS SEQ_002, ";
        $query .= "      CASE WHEN L3.CLASS_SEQ IS NOT NULL THEN '有効' ELSE NULL END AS SEQ_003, ";
        $query .= "      CASE WHEN L4.CLASS_SEQ IS NOT NULL THEN '有効' ELSE NULL END AS SEQ_004, ";
        $query .= "      L4.CLASS_REMARK1 AS REMARK1_004, ";
        $query .= "      CASE WHEN L5.CLASS_SEQ IS NOT NULL THEN '有効' ELSE NULL END AS SEQ_005, ";
        $query .= "      L5.CLASS_REMARK1 AS REMARK1_005, ";
        $query .= "      CASE WHEN L6.CLASS_SEQ IS NOT NULL THEN '有効' ELSE NULL END AS SEQ_006, ";
        $query .= "      CASE WHEN L7.CLASS_SEQ IS NOT NULL THEN '有効' ELSE NULL END AS SEQ_007, ";
        $query .= "      L7.CLASS_REMARK1 AS REMARK1_007, ";
        $query .= "      L7.CLASS_REMARK2 AS REMARK2_007 ";
        $query .= " FROM ";
        $query .= "      CLASS_MST T1 ";
        $query .= " LEFT JOIN CLASS_YDAT Y1 ON Y1.CLASSCD = T1.CLASSCD ";
        $query .= "                        AND Y1.SCHOOL_KIND = T1.SCHOOL_KIND ";
        $query .= "                        AND Y1.YEAR = '".$model->field["YEAR"]."' ";
        for ($i = 1; $i <= 7; $i++ ) {
            //頭0埋め3桁統一
            $seq = sprintf("%03d", $i);
            $tablename = "L".$i;
            
            $query .= " LEFT JOIN CLASS_DETAIL_DAT $tablename ON $tablename.YEAR          = '".$model->field["YEAR"]."' ";
            $query .= "                                      AND $tablename.CLASSCD       = T1.CLASSCD ";
            $query .= "                                      AND $tablename.SCHOOL_KIND   = T1.SCHOOL_KIND ";
            $query .= "                                      AND $tablename.CLASS_SEQ  = '".$seq."' ";
        }
        if ($model->field["SCHOOL_KIND"]) {
            $query .= " WHERE ";
            $query .= "     T1.SCHOOL_KIND = '".$model->field["SCHOOL_KIND"]."' ";
        } else if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " WHERE ";
                $query .= " T1.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " WHERE T1.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     Y1.YEAR, ";
        $query .= "     T1.SCHOOL_KIND, ";
        $query .= "     T1.CLASSCD ";

        return $query;
    }

    //１レコード取得
    function getRow($model)
    {
        $db = Query::dbCheckOut();

        $query  = " SELECT DISTINCT ";
        $query .= "     T1.CLASSCD || '-' || T1.SCHOOL_KIND AS CLASSCD_SET, ";
        $query .= "     T1.CLASSCD, ";
        $query .= "     T1.SCHOOL_KIND, ";
        $query .= "     T1.CLASSNAME, ";
        $query .= "     L1.CLASS_SEQ AS SEQ_001, ";
        $query .= "     L2.CLASS_SEQ AS SEQ_002, ";
        $query .= "     L3.CLASS_SEQ AS SEQ_003, ";
        $query .= "     L4.CLASS_SEQ AS SEQ_004, ";
        $query .= "     L4.CLASS_REMARK1 AS REMARK1_004, ";
        $query .= "     L5.CLASS_SEQ AS SEQ_005, ";
        $query .= "     L5.CLASS_REMARK1 AS REMARK1_005, ";
        $query .= "     L6.CLASS_SEQ AS SEQ_006, ";
        $query .= "     L7.CLASS_SEQ AS SEQ_007, ";
        $query .= "     L7.CLASS_REMARK1 AS REMARK1_007, ";
        $query .= "     L7.CLASS_REMARK2 AS REMARK2_007 ";
        $query .= " FROM ";
        $query .= "     CLASS_MST T1 ";
        for ($i = 1; $i <= 7; $i++ ) {
            //頭0埋め3桁統一
            $seq = sprintf("%03d", $i);
            $tablename = "L".$i;
            
            $query .= " LEFT JOIN CLASS_DETAIL_DAT $tablename ON $tablename.YEAR          = '" .$model->leftyear ."' ";
            $query .= "                                      AND $tablename.CLASSCD       = T1.CLASSCD ";
            $query .= "                                      AND $tablename.SCHOOL_KIND   = T1.SCHOOL_KIND ";
            $query .= "                                      AND $tablename.CLASS_SEQ  = '".$seq."' ";
        }
        $query .= " WHERE ";
        $query .= "      T1.CLASSCD     = '" .$model->classcd ."'";
        $query .= "  AND T1.SCHOOL_KIND = '" .$model->school_kind."' ";
        
        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);
        return $row;
    }

    //CLASS_DETAIL_DAT データ取得(前年度コピー用)
    function getDetailDat($model, $flg)
    {
        $query  = " SELECT ";
        $query .= "     T1.* ";
        $query .= " FROM ";
        $query .= "     CLASS_DETAIL_DAT T1";
        $query .= " WHERE ";
        if ($flg) {
            $query .= "     T1.YEAR        = '".($model->field["YEAR"] - 1)."' ";
        } else {
            $query .= "     T1.YEAR        = '".$model->field["YEAR"]."' ";
        }

        return $query;
    }

    //--- UPDATE 
    function &getUpdateQuery($fields, $model)
    {
        $db = Query::dbCheckOut();
        
        $query  = " DELETE ";
        $query .= " FROM ";
        $query .= "      CLASS_DETAIL_DAT ";
        $query .= " WHERE ";
        $query .= "      YEAR           = '".$model->leftyear."' ";
        $query .= "  AND CLASSCD        = '".$model->classcd."' ";
        $query .= "  AND SCHOOL_KIND    = '".$model->school_kind."' ";
        $db->query($query);
        
        for ($i = 1; $i <= 7; $i++ ) {
            //頭0埋め3桁統一
            $seq = sprintf("%03d", $i);
            if ($fields["SEQ_".$seq]) {
                //データ更新
                $data = array();
                $data["YEAR"][TEXT]                 = $model->leftyear;
                $data["CLASSCD"][TEXT]              = $model->classcd;
                $data["SCHOOL_KIND"][TEXT]          = $model->school_kind;
                $data["CLASS_SEQ"][TEXT]            = $seq;
                if ($seq === '004' || $seq === '005' || $seq === '007') {
                    $data["CLASS_REMARK1"][TEXT]            = $fields["REMARK1_".$seq];
                    if ($seq === '007') {
                        $data["CLASS_REMARK2"][TEXT]            = $fields["REMARK2_".$seq];
                    }
                }
                //オリジナルコード
                if ($seq === '001') {
                    $data["ORIGINAL_CD"][TEXT]          = 'D005';
                } else if ($seq === '002') {
                    $data["ORIGINAL_CD"][TEXT]          = 'D006';
                } else if ($seq === '003') {
                    $data["ORIGINAL_CD"][TEXT]          = 'D008';
                } else if ($seq === '004') {
                    $data["ORIGINAL_CD"][TEXT]          = 'D009';
                } else if ($seq === '005') {
                    $data["ORIGINAL_CD"][TEXT]          = 'D031';
                } else if ($seq === '006') {
                    $data["ORIGINAL_CD"][TEXT]          = 'D049';
                } else if ($seq === '007') {
                    $data["ORIGINAL_CD"][TEXT]          = 'D010';
                }
                $data["REGISTERCD"][TEXT]           = STAFFCD;
                $data["UPDATED"][NUMBER]            = "sysdate()";

                $query = Query::insertSQL($data, "CLASS_DETAIL_DAT");
                $db->query($query);
            }
        }

        Query::dbCheckIn($db);
        return;
    }

    //--- DELETE
    function &getDeleteQuery($model)
    {
        $db = Query::dbCheckOut();

        $query  = " DELETE ";
        $query .= " FROM ";
        $query .= "      CLASS_DETAIL_DAT ";
        $query .= " WHERE ";
        $query .= "      YEAR           = '".$model->leftyear."' ";
        $query .= "  AND CLASSCD        = '".$model->classcd."' ";
        $query .= "  AND SCHOOL_KIND    = '".$model->school_kind."' ";

        $db->query($query);
        Query::dbCheckIn($db);

        return $result;
    }
    
    //--- COPY
    function &getCopyQuery($model)
    {
        $db = Query::dbCheckOut();
        //コピーデータを取得
        $result = $db->query(knjz060_3Query::getDetailDat($model, "COPY"));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $data = array();

            $data["YEAR"][TEXT]             = $model->field["YEAR"];
            $data["CLASSCD"][TEXT]          = $row["CLASSCD"];
            $data["SCHOOL_KIND"][TEXT]      = $row["SCHOOL_KIND"];
            $data["CLASS_SEQ"][TEXT]        = $row["CLASS_SEQ"];
            $data["CLASS_REMARK1"][TEXT]    = $row["CLASS_REMARK1"];
            $data["CLASS_REMARK2"][TEXT]    = $row["CLASS_REMARK2"];
            $data["CLASS_REMARK3"][TEXT]    = $row["CLASS_REMARK3"];
            $data["CLASS_REMARK4"][TEXT]    = $row["CLASS_REMARK4"];
            $data["CLASS_REMARK5"][TEXT]    = $row["CLASS_REMARK5"];
            $data["CLASS_REMARK6"][TEXT]    = $row["CLASS_REMARK6"];
            $data["CLASS_REMARK7"][TEXT]    = $row["CLASS_REMARK7"];
            $data["CLASS_REMARK8"][TEXT]    = $row["CLASS_REMARK8"];
            $data["CLASS_REMARK9"][TEXT]    = $row["CLASS_REMARK9"];
            $data["CLASS_REMARK10"][TEXT]   = $row["CLASS_REMARK10"];
            $data["ORIGINAL_CD"][TEXT]      = $row["ORIGINAL_CD"];
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][NUMBER]        = "sysdate()";

            $query = Query::insertSQL($data, "CLASS_DETAIL_DAT");
            $db->query($query);
        }
        $result->free();

        Query::dbCheckIn($db);
        return;
    }
    
}
?>
