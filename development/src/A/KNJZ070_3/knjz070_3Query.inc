<?php

require_once('for_php7.php');

class knjz070_3Query extends Query {

    //１レコード取得
    function getSemesterMst($model, $flg)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        if (!$flg) {
            $query .= "     YEAR = '".$model->field["YEAR"]."' ";
        } else {
            $model->leftyear = ($model->leftyear == "") ? CTRL_YEAR : $model->leftyear;
            $query .= "     YEAR = '".$model->leftyear."' ";
        }
        $query .= " AND SEMESTER <> '9' ";

        return $query;
    }
    
    //SUBCLASS_YDAT 年度取得
    function getYear($model)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "      YEAR AS LABEL, ";
        $query .= "      YEAR AS VALUE ";
        $query .= " FROM ";
        $query .= "      SUBCLASS_YDAT ";
        $query .= " ORDER BY ";
        $query .= "      VALUE DESC";
        
        return $query;
    }

    //教科コンボ
    function getClasscd($model)
    {
        $query  = " SELECT  ";
        $query .= "      CLASSCD, ";
        $query .= "      SCHOOL_KIND, ";
        $query .= "      CLASSCD || '-' || SCHOOL_KIND AS VALUE, ";
        $query .= "      CLASSCD || '-' || SCHOOL_KIND || '：' || CLASSNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "      CLASS_MST ";
        $query .= " ORDER BY ";
        $query .= "      SCHOOL_KIND, ";
        $query .= "      CLASSCD ";
        
        return $query;
    }

    //SUBCLASS_MST データ取得
    function getData($model)
    {
        //学期確認
        $nameSemester = array();
        if ($model->semesterCount != "2") {
            $nameSemester[0] = '1学期';
            $nameSemester[1] = '2学期';
            $nameSemester[2] = '3学期';
        } else {
            $nameSemester[0] = '前期';
            $nameSemester[1] = '後期';
        }
        $query  = " SELECT DISTINCT ";
        $query .= "     Y1.YEAR, ";
        $query .= "     T1.*, ";
        $query .= "     CASE WHEN L1.SUBCLASS_SEQ IS NOT NULL THEN '有効' ELSE NULL END AS SEQ_001, ";
        /*$query .= "     CASE WHEN L2.SUBCLASS_SEQ IS NOT NULL THEN '有効' ELSE NULL END AS SEQ_002, ";
        $query .= "     L2.SUBCLASS_REMARK1 AS REMARK1_002, ";
        $query .= "     L2.SUBCLASS_REMARK2 AS REMARK2_002, ";
        $query .= "     CASE WHEN L3.SUBCLASS_SEQ IS NOT NULL THEN '有効' ELSE NULL END AS SEQ_003, ";
        $query .= "     L3.SUBCLASS_REMARK2 AS REMARK1_003, ";*/
        $query .= "     CASE WHEN L4.SUBCLASS_SEQ IS NOT NULL THEN '有効' ELSE NULL END AS SEQ_004, ";
        $query .= "     CASE WHEN L5.SUBCLASS_SEQ IS NOT NULL THEN '有効' ELSE NULL END AS SEQ_005, ";
        $query .= "     CASE WHEN L6.SUBCLASS_SEQ IS NOT NULL THEN '有効' ELSE NULL END AS SEQ_006, ";
        $query .= "     CASE WHEN L7.SUBCLASS_SEQ IS NOT NULL AND  (L7.SUBCLASS_REMARK1 IS NOT NULL OR L7.SUBCLASS_REMARK2 IS NOT NULL OR L7.SUBCLASS_REMARK3 IS NOT NULL OR L7.SUBCLASS_REMARK4 IS NOT NULL) THEN '有効' ELSE NULL END AS SEQ_007, ";
        $query .= "     CASE WHEN L7.SUBCLASS_REMARK1 IS NOT NULL THEN '".$nameSemester[0]."' ELSE NULL END AS REMARK1_007, ";
        $query .= "     CASE WHEN L7.SUBCLASS_REMARK2 IS NOT NULL THEN '".$nameSemester[1]."' ELSE NULL END AS REMARK2_007, ";
        if ($model->semesterCount != "2") {
            $query .= "     CASE WHEN L7.SUBCLASS_REMARK3 IS NOT NULL THEN '".$nameSemester[2]."' ELSE NULL END AS REMARK3_007, ";
        }
        $query .= "     CASE WHEN L7.SUBCLASS_REMARK4 IS NOT NULL THEN '学年末' ELSE NULL END AS REMARK4_007, ";
        $query .= "     CASE WHEN L8.SUBCLASS_SEQ IS NOT NULL AND (L8.SUBCLASS_REMARK1 IS NOT NULL OR L8.SUBCLASS_REMARK2 IS NOT NULL OR L8.SUBCLASS_REMARK3 IS NOT NULL OR L8.SUBCLASS_REMARK4 IS NOT NULL) THEN '有効' ELSE NULL END AS SEQ_008, ";
        $query .= "     CASE WHEN L8.SUBCLASS_REMARK1 IS NOT NULL THEN '".$nameSemester[0]."' ELSE NULL END AS REMARK1_008, ";
        $query .= "     CASE WHEN L8.SUBCLASS_REMARK2 IS NOT NULL THEN '".$nameSemester[1]."' ELSE NULL END AS REMARK2_008, ";
        if ($model->semesterCount != "2") {
            $query .= "     CASE WHEN L8.SUBCLASS_REMARK3 IS NOT NULL THEN '".$nameSemester[2]."' ELSE NULL END AS REMARK3_008, ";
        }
        $query .= "     CASE WHEN L8.SUBCLASS_REMARK4 IS NOT NULL THEN '学年末' ELSE NULL END AS REMARK4_008, ";
        $query .= "     CASE WHEN L9.SUBCLASS_SEQ IS NOT NULL THEN '有効' ELSE NULL END AS SEQ_009, ";
        $query .= "     L9.SUBCLASS_REMARK1 AS REMARK1_009, ";
        $query .= "     CASE WHEN L10.SUBCLASS_SEQ IS NOT NULL THEN '有効' ELSE NULL END AS SEQ_010, ";
        $query .= "     CASE WHEN L11.SUBCLASS_SEQ IS NOT NULL THEN '有効' ELSE NULL END AS SEQ_011, ";
        $query .= "     CASE WHEN L12.SUBCLASS_SEQ IS NOT NULL AND (L12.SUBCLASS_REMARK1 IS NOT NULL OR L12.SUBCLASS_REMARK2 IS NOT NULL OR L12.SUBCLASS_REMARK3 IS NOT NULL) THEN '有効' ELSE NULL END AS SEQ_012, ";
        $query .= "     CASE WHEN L12.SUBCLASS_REMARK1 IS NOT NULL THEN '".$nameSemester[0]."' ELSE NULL END AS REMARK1_012, ";
        $query .= "     CASE WHEN L12.SUBCLASS_REMARK2 IS NOT NULL THEN '".$nameSemester[1]."' ELSE NULL END AS REMARK2_012 ";
        if ($model->semesterCount != "2") {
            $query .= "     ,CASE WHEN L12.SUBCLASS_REMARK3 IS NOT NULL THEN '".$nameSemester[2]."' ELSE NULL END AS REMARK3_012 ";
        }
        $query .= " FROM ";
        $query .= "      SUBCLASS_MST T1 ";
        $query .= " LEFT JOIN SUBCLASS_YDAT Y1 ON Y1.CLASSCD       = T1.CLASSCD ";
        $query .= "                           AND Y1.SCHOOL_KIND   = T1.SCHOOL_KIND ";
        $query .= "                           AND Y1.CURRICULUM_CD = T1.CURRICULUM_CD ";
        $query .= "                           AND Y1.SUBCLASSCD    = T1.SUBCLASSCD ";
        $query .= "                           AND Y1.YEAR = '".$model->field["YEAR"]."' ";
        for ($i = 1; $i <= 12; $i++ ) {
            //頭0埋め3桁統一
            $seq = sprintf("%03d", $i);
            $tablename = "L".$i;
            
            $query .= " LEFT JOIN SUBCLASS_DETAIL_DAT $tablename ON $tablename.YEAR          = '".$model->field["YEAR"]."' ";
            $query .= "                                         AND $tablename.CLASSCD       = T1.CLASSCD ";
            $query .= "                                         AND $tablename.SCHOOL_KIND   = T1.SCHOOL_KIND ";
            $query .= "                                         AND $tablename.CURRICULUM_CD = T1.CURRICULUM_CD ";
            $query .= "                                         AND $tablename.SUBCLASSCD    = T1.SUBCLASSCD ";
            $query .= "                                         AND $tablename.SUBCLASS_SEQ  = '".$seq."' ";
        }
        if ($model->field["CLASSCD_SET"]) {
            $query .= " WHERE ";
            $query .= "    T1.CLASSCD || '-' || T1.SCHOOL_KIND= '".$model->field["CLASSCD_SET"]."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     Y1.YEAR, ";
        $query .= "     T1.SCHOOL_KIND, ";
        $query .= "     T1.CLASSCD, ";
        $query .= "     T1.CURRICULUM_CD, ";
        $query .= "     T1.SUBCLASSCD ";

        return $query;
    }

    //１レコード取得
    function getRow($model)
    {
        $db = Query::dbCheckOut();

        $query  = " SELECT DISTINCT ";
        $query .= "     T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD AS SUBCLASSCD_SET, ";
        $query .= "     T1.CLASSCD, ";
        $query .= "     T1.SCHOOL_KIND, ";
        $query .= "     T1.CURRICULUM_CD, ";
        $query .= "     T1.SUBCLASSCD, ";
        $query .= "     T1.SUBCLASSNAME, ";
        $query .= "     L1.SUBCLASS_SEQ AS SEQ_001, ";
        /*$query .= "     L2.SUBCLASS_SEQ AS SEQ_002, ";
        $query .= "     L2.SUBCLASS_REMARK1 AS REMARK1_002, ";
        $query .= "     L2.SUBCLASS_REMARK2 AS REMARK2_002, ";
        $query .= "     L3.SUBCLASS_SEQ AS SEQ_003, ";
        $query .= "     L3.SUBCLASS_REMARK1 AS REMARK1_003, ";*/
        $query .= "     L4.SUBCLASS_SEQ AS SEQ_004, ";
        $query .= "     L5.SUBCLASS_SEQ AS SEQ_005, ";
        $query .= "     L6.SUBCLASS_SEQ AS SEQ_006, ";
        $query .= "     L7.SUBCLASS_SEQ AS SEQ_007, ";
        $query .= "     L7.SUBCLASS_REMARK1 AS REMARK1_007, ";
        $query .= "     L7.SUBCLASS_REMARK2 AS REMARK2_007, ";
        if ($model->semesterCount != "2") {
            $query .= "     L7.SUBCLASS_REMARK3 AS REMARK3_007, ";
        }
        $query .= "     L7.SUBCLASS_REMARK4 AS REMARK4_007, ";
        $query .= "     L8.SUBCLASS_SEQ AS SEQ_008, ";
        $query .= "     L8.SUBCLASS_REMARK1 AS REMARK1_008, ";
        $query .= "     L8.SUBCLASS_REMARK2 AS REMARK2_008, ";
        if ($model->semesterCount != "2") {
            $query .= "     L8.SUBCLASS_REMARK3 AS REMARK3_008, ";
        }
        $query .= "     L8.SUBCLASS_REMARK4 AS REMARK4_008, ";
        $query .= "     L9.SUBCLASS_SEQ AS SEQ_009, ";
        $query .= "     L9.SUBCLASS_REMARK1 AS REMARK1_009, ";
        $query .= "     L10.SUBCLASS_SEQ AS SEQ_010, ";
        $query .= "     L11.SUBCLASS_SEQ AS SEQ_011, ";
        $query .= "     L12.SUBCLASS_SEQ AS SEQ_012, ";
        $query .= "     L12.SUBCLASS_REMARK1 AS REMARK1_012, ";
        $query .= "     L12.SUBCLASS_REMARK2 AS REMARK2_012 ";
        if ($model->semesterCount != "2") {
            $query .= "     ,L12.SUBCLASS_REMARK3 AS REMARK3_012 ";
        }
        $query .= " FROM ";
        $query .= "     SUBCLASS_MST T1 ";
        for ($i = 1; $i <= 12; $i++ ) {
            //頭0埋め3桁統一
            $seq = sprintf("%03d", $i);
            $tablename = "L".$i;
            
            $query .= " LEFT JOIN SUBCLASS_DETAIL_DAT $tablename ON $tablename.YEAR          = '" .$model->leftyear ."' ";
            $query .= "                                         AND $tablename.CLASSCD       = T1.CLASSCD ";
            $query .= "                                         AND $tablename.SCHOOL_KIND   = T1.SCHOOL_KIND ";
            $query .= "                                         AND $tablename.CURRICULUM_CD = T1.CURRICULUM_CD ";
            $query .= "                                         AND $tablename.SUBCLASSCD    = T1.SUBCLASSCD ";
            $query .= "                                         AND $tablename.SUBCLASS_SEQ  = '".$seq."' ";
        }
        $query .= " WHERE ";
        $query .= "      T1.CLASSCD       = '" .$model->classcd ."'";
        $query .= "  AND T1.SCHOOL_KIND   = '" .$model->school_kind."' ";
        $query .= "  AND T1.CURRICULUM_CD = '" .$model->curriculum_cd."' ";
        $query .= "  AND T1.SUBCLASSCD    = '" .$model->subclasscd."' ";
        
        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);
        return $row;
    }

    //SUBCLASS_DETAIL_DAT データ取得(前年度コピー用)
    function getDetailDat($model, $flg)
    {
        $query  = " SELECT ";
        $query .= "     T1.* ";
        $query .= " FROM ";
        $query .= "     SUBCLASS_DETAIL_DAT T1";
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
        $query .= "      SUBCLASS_DETAIL_DAT ";
        $query .= " WHERE ";
        $query .= "      YEAR           = '".$model->leftyear."' ";
        $query .= "  AND CLASSCD        = '".$model->classcd."' ";
        $query .= "  AND SCHOOL_KIND    = '".$model->school_kind."' ";
        $query .= "  AND CURRICULUM_CD  = '".$model->curriculum_cd."' ";
        $query .= "  AND SUBCLASSCD     = '".$model->subclasscd."' ";
        //宮城、常磐のSEQ=012のみを表示するプロパティ
        if ($model->Properties["kari_useMiyagiTokiwa"] === '1') {
            $query .= "  AND SUBCLASS_SEQ     = '012' ";
        }
        $db->query($query);

        //宮城、常磐のSEQ=012のみを表示するプロパティ
        if ($model->Properties["kari_useMiyagiTokiwa"] === '1') {
            //データ更新
            $seq = '012';
            $data = array();
            $data["YEAR"][TEXT]                 = $model->leftyear;
            $data["CLASSCD"][TEXT]              = $model->classcd;
            $data["SCHOOL_KIND"][TEXT]          = $model->school_kind;
            $data["CURRICULUM_CD"][TEXT]        = $model->curriculum_cd;
            $data["SUBCLASSCD"][TEXT]           = $model->subclasscd;
            $data["SUBCLASS_SEQ"][TEXT]         = $seq;
            $data["SUBCLASS_REMARK1"][TEXT]     = $fields["REMARK1_".$seq];
            $data["SUBCLASS_REMARK2"][TEXT]     = $fields["REMARK2_".$seq];
            if ($model->semesterCount != "2") {
                $data["SUBCLASS_REMARK3"][TEXT]     = $fields["REMARK3_".$seq];
            }
            $data["ORIGINAL_CD"][TEXT]          = 'M015';
            $data["REGISTERCD"][TEXT]           = STAFFCD;
            $data["UPDATED"][NUMBER]            = "sysdate()";
            $query = Query::insertSQL($data, "SUBCLASS_DETAIL_DAT");
            $db->query($query);
        } else {
            for ($i = 1; $i <= 12; $i++ ) {
                //頭0埋め3桁統一
                $seq = sprintf("%03d", $i);
                if ($fields["SEQ_".$seq]) {
                    //データ更新
                    $data = array();
                    $data["YEAR"][TEXT]                 = $model->leftyear;
                    $data["CLASSCD"][TEXT]              = $model->classcd;
                    $data["SCHOOL_KIND"][TEXT]          = $model->school_kind;
                    $data["CURRICULUM_CD"][TEXT]        = $model->curriculum_cd;
                    $data["SUBCLASSCD"][TEXT]           = $model->subclasscd;
                    $data["SUBCLASS_SEQ"][TEXT]         = $seq;
                    if ($seq === '007' || $seq === '008' || $seq === '009' || $seq === '012') {
                        if ($seq === '007' || $seq === '008' || $seq === '012') {
                            $data["SUBCLASS_REMARK1"][TEXT]            = $fields["REMARK1_".$seq];
                            $data["SUBCLASS_REMARK2"][TEXT]            = $fields["REMARK2_".$seq];
                            if ($model->semesterCount != "2") {
                                $data["SUBCLASS_REMARK3"][TEXT]            = $fields["REMARK3_".$seq];
                            }
                            if ($seq === '007' || $seq === '008') {
                                $data["SUBCLASS_REMARK4"][TEXT]            = $fields["REMARK4_".$seq];
                            }
                        } else if ($seq === '009') {
                            $data["SUBCLASS_REMARK1"][TEXT]            = '1';//チェックボックスが有効の時、同時にREMARK1に1を入れる
                        }
                    }
                    //オリジナルコード
                    if ($seq === '001') {
                        $data["ORIGINAL_CD"][TEXT]          = 'D004';
                    } else if ($seq === '004') {
                        $data["ORIGINAL_CD"][TEXT]          = 'D017';
                    } else if ($seq === '005') {
                        $data["ORIGINAL_CD"][TEXT]          = 'D018';
                    } else if ($seq === '006') {
                        $data["ORIGINAL_CD"][TEXT]          = 'D020';
                    } else if ($seq === '007') {
                        $data["ORIGINAL_CD"][TEXT]          = 'D026';
                    } else if ($seq === '008') {
                        $data["ORIGINAL_CD"][TEXT]          = 'D046';
                    } else if ($seq === '009') {
                        $data["ORIGINAL_CD"][TEXT]          = 'D051';
                    } else if ($seq === '010') {
                        $data["ORIGINAL_CD"][TEXT]          = 'D052';
                    } else if ($seq === '011') {
                        $data["ORIGINAL_CD"][TEXT]          = 'E014';
                    } else if ($seq === '012') {
                        $data["ORIGINAL_CD"][TEXT]          = 'M015';
                    }
                    $data["REGISTERCD"][TEXT]           = STAFFCD;
                    $data["UPDATED"][NUMBER]            = "sysdate()";
                    $query = Query::insertSQL($data, "SUBCLASS_DETAIL_DAT");
                    $db->query($query);
                }
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
        $query .= "      SUBCLASS_DETAIL_DAT ";
        $query .= " WHERE ";
        $query .= "      YEAR           = '".$model->leftyear."' ";
        $query .= "  AND CLASSCD        = '".$model->classcd."' ";
        $query .= "  AND SCHOOL_KIND    = '".$model->school_kind."' ";
        $query .= "  AND CURRICULUM_CD  = '".$model->curriculum_cd."' ";
        $query .= "  AND SUBCLASSCD     = '".$model->subclasscd."' ";
        //宮城、常磐のSEQ=012のみを表示するプロパティ
        if ($model->Properties["kari_useMiyagiTokiwa"] === '1') {
            $query .= "  AND SUBCLASS_SEQ     = '012' ";
        }
        
        $db->query($query);
        Query::dbCheckIn($db);

        return $result;
    }
    
    //--- COPY
    function &getCopyQuery($model)
    {
        $db = Query::dbCheckOut();
        //コピーデータを取得
        $result = $db->query(knjz070_3Query::getDetailDat($model, "COPY"));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $data = array();

            $data["YEAR"][TEXT]                = $model->field["YEAR"];
            $data["CLASSCD"][TEXT]             = $row["CLASSCD"];
            $data["SCHOOL_KIND"][TEXT]         = $row["SCHOOL_KIND"];
            $data["CURRICULUM_CD"][TEXT]       = $row["CURRICULUM_CD"];
            $data["SUBCLASSCD"][TEXT]          = $row["SUBCLASSCD"];
            $data["SUBCLASS_SEQ"][TEXT]        = $row["SUBCLASS_SEQ"];
            $data["SUBCLASS_REMARK1"][TEXT]    = $row["SUBCLASS_REMARK1"];
            $data["SUBCLASS_REMARK2"][TEXT]    = $row["SUBCLASS_REMARK2"];
            $data["SUBCLASS_REMARK3"][TEXT]    = $row["SUBCLASS_REMARK3"];
            $data["SUBCLASS_REMARK4"][TEXT]    = $row["SUBCLASS_REMARK4"];
            $data["SUBCLASS_REMARK5"][TEXT]    = $row["SUBCLASS_REMARK5"];
            $data["SUBCLASS_REMARK6"][TEXT]    = $row["SUBCLASS_REMARK6"];
            $data["SUBCLASS_REMARK7"][TEXT]    = $row["SUBCLASS_REMARK7"];
            $data["SUBCLASS_REMARK8"][TEXT]    = $row["SUBCLASS_REMARK8"];
            $data["SUBCLASS_REMARK9"][TEXT]    = $row["SUBCLASS_REMARK9"];
            $data["SUBCLASS_REMARK10"][TEXT]   = $row["SUBCLASS_REMARK10"];
            $data["ORIGINAL_CD"][TEXT]         = $row["ORIGINAL_CD"];
            $data["REGISTERCD"][TEXT]          = STAFFCD;
            $data["UPDATED"][NUMBER]           = "sysdate()";

            $query = Query::insertSQL($data, "SUBCLASS_DETAIL_DAT");
            $db->query($query);
        }
        $result->free();

        Query::dbCheckIn($db);
        return;
    }
    
}
?>
