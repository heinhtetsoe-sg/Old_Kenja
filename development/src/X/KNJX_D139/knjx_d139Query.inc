<?php

require_once('for_php7.php');

class knjx_d139Query extends Query {

    //校種取得
    function getSchkind($model) {
        $query  = " SELECT ";
        $query .= "     NAME1 AS VALUE, ";
        $query .= "     ABBV1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR    = '".CTRL_YEAR."' ";
        $query .= "     AND NAMECD1 = 'A023' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "     AND NAME1 IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        }
        $query .= " ORDER BY ";
        $query .= "     NAME1 ";

        return $query;
    }

    //名称マスタの取得
    function getNameMst($field,$namecd1,$namecd2)
    {
        $query  = " SELECT ";
        if($field == ""){
            $query .= "     NAMECD2 AS VALUE, ";
            $query .= "     NAME1 AS LABEL ";
        }else{
            $query .= "     ".$field." ";
        }
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     NAMECD1 = '".$namecd1."' ";
        if($namecd2 != "") $query .= " AND NAMECD2 = '".$namecd2."' ";
        if($field == ""){
            $query .= " ORDER BY ";
            $query .= "     VALUE ";
        }

        return $query;
    }

    //年組一覧取得
    function getGradeHrclass($model) {
        $query  = " SELECT ";
        $query .= "     T1.GRADE || T1.HR_CLASS AS VALUE, ";
        $query .= "     T1.HR_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT T1";
        $query .= "     LEFT JOIN  SCHREG_REGD_GDAT G1 ON G1.YEAR  = T1.YEAR ";
        $query .= "                                   AND G1.GRADE = T1.GRADE ";
        $query .= " WHERE ";
        $query .= "         T1.YEAR         = '".CTRL_YEAR."' ";
        $query .= "     AND T1.SEMESTER     = '".$model->field["SEMESTER"]."' ";
        if ($model->Properties["useSchool_KindField"] == "1"){
            $query .= "     AND G1.SCHOOL_KIND  = '".$model->field["SCHOOL_KIND"]."' ";
        }
        if(AUTHORITY == DEF_UPDATE_RESTRICT || $AUTHORITY == DEF_REFER_RESTRICT) {
            $query .= "     AND (T1.TR_CD1     = '".STAFFCD."' OR ";
            $query .= "          T1.TR_CD2     = '".STAFFCD."' OR ";
            $query .= "          T1.TR_CD3     = '".STAFFCD."' OR ";
            $query .= "          T1.SUBTR_CD1  = '".STAFFCD."' OR ";
            $query .= "          T1.SUBTR_CD1  = '".STAFFCD."' OR ";
            $query .= "          T1.SUBTR_CD1  = '".STAFFCD."') ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";
        return $query;
    }


    //教科取得
    function getClassMst($model, $classcd, $grade_hr_class)
    {
        $query  = " SELECT DISTINCT ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     SUBSTR(T1.VIEWCD,1,2) || '-' || T1.SCHOOL_KIND AS VALUE, ";
            $query .= "     SUBSTR(T1.VIEWCD,1,2) || '-' || T1.SCHOOL_KIND || ' ' || T2.CLASSNAME AS LABEL ";
        } else {
            $query .= "     SUBSTR(T1.VIEWCD,1,2) AS VALUE, ";
            $query .= "     SUBSTR(T1.VIEWCD,1,2) || ' ' || T2.CLASSNAME AS LABEL ";
        }
        $query .= " FROM ";
        $query .= "     JVIEWNAME_GRADE_MST T1 ";
        $query .= "     LEFT JOIN CLASS_MST T2 ON T2.CLASSCD = SUBSTR(T1.VIEWCD,1,2)";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                         AND  T2.SCHOOL_KIND = T1.SCHOOL_KIND ";
        }
        $query .= " WHERE ";
        $query .= "     T1.GRADE = '".substr($grade_hr_class,0,2)."' AND ";
        $query .= "     SUBSTR(T1.VIEWCD,1,2) = T2.CLASSCD ";
        $query .= "     AND SUBSTR(T1.VIEWCD,1,2) < '90' ";
        if ($model->Properties["useSchool_KindField"] == "1"){
            $query .= "     AND T1.SCHOOL_KIND  = '".$model->field["SCHOOL_KIND"]."' ";
        }
        if($classcd){
            $query .= "     AND T2.CLASSCD = '".$classcd."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //科目取得
    function getSubclassMst($classcd, $grade_hr_class, $model)
    {
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $class_array = array();
            $class_array = explode("-", $classcd);
        }
        $query  = " SELECT DISTINCT ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T2.CLASSCD || '-' || T2.SCHOOL_KIND || '-' || T2.CURRICULUM_CD || '-' || T2.SUBCLASSCD AS VALUE, ";
            $query .= "     T2.CLASSCD || '-' || T2.SCHOOL_KIND || '-' || T2.CURRICULUM_CD || '-' || T2.SUBCLASSCD || ' ' || T2.SUBCLASSNAME AS LABEL ";
        } else {
            $query .= "     T2.SUBCLASSCD AS VALUE, ";
            $query .= "     T2.SUBCLASSCD || ' ' || T2.SUBCLASSNAME AS LABEL ";
        }
        $query .= " FROM ";
        $query .= "     JVIEWNAME_GRADE_MST T1 ";
        $query .= "     LEFT JOIN SUBCLASS_MST T2 ON T2.SUBCLASSCD = T1.SUBCLASSCD";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                         AND  T2.CLASSCD = T1.CLASSCD ";
            $query .= "                         AND  T2.SCHOOL_KIND = T1.SCHOOL_KIND ";
            $query .= "                         AND  T2.CURRICULUM_CD = T1.CURRICULUM_CD ";
        }
        $query .= " WHERE ";
        $query .= "     T1.GRADE = '".substr($grade_hr_class,0,2)."' AND ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     SUBSTR(T1.SUBCLASSCD,1,2) = '".$class_array[0]."' AND";
            $query .= "     T1.SCHOOL_KIND = '".$class_array[1]."' AND";
            $query .= "     SUBSTR(T1.VIEWCD,1,2) = '".$class_array[0]."' ";
        } else {
            $query .= "     SUBSTR(T1.SUBCLASSCD,1,2) = '".$classcd."' AND";
            $query .= "     SUBSTR(T1.VIEWCD,1,2) = '".$classcd."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //科目名取得
    function getSubclassName($classcd, $grade_hr_class, $model, $subClassCd)
    {
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $class_array = array();
            $class_array = explode("-", $classcd);
        }
        $query  = " SELECT DISTINCT ";
        $query .= "     T2.SUBCLASSNAME ";
        $query .= " FROM ";
        $query .= "     JVIEWNAME_GRADE_MST T1 ";
        $query .= "     LEFT JOIN SUBCLASS_MST T2 ON T2.SUBCLASSCD = T1.SUBCLASSCD";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                         AND  T2.CLASSCD = T1.CLASSCD ";
            $query .= "                         AND  T2.SCHOOL_KIND = T1.SCHOOL_KIND ";
            $query .= "                         AND  T2.CURRICULUM_CD = T1.CURRICULUM_CD ";
        }
        $query .= " WHERE ";
        $query .= "     T1.GRADE = '".substr($grade_hr_class,0,2)."' AND ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     SUBSTR(T1.SUBCLASSCD,1,2) = '".$class_array[0]."' AND";
            $query .= "     T1.SCHOOL_KIND = '".$class_array[1]."' AND";
            $query .= "     SUBSTR(T1.VIEWCD,1,2) = '".$class_array[0]."' ";
            $query .= " AND T2.CLASSCD || '-' || T2.SCHOOL_KIND || '-' || T2.CURRICULUM_CD || '-' || T2.SUBCLASSCD = '".$subClassCd."' ";
        } else {
            $query .= "     SUBSTR(T1.SUBCLASSCD,1,2) = '".$classcd."' AND";
            $query .= "     SUBSTR(T1.VIEWCD,1,2) = '".$classcd."' ";
            $query .= " AND T2.SUBCLASSCD = '".$subClassCd."' ";
        }
        return $query;
    }
    

    //観点取得
    function selectViewcdQuery($model, $schregno){
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $subclass_array = array();
            $subclass_array = explode("-", $model->field["SUBCLASSCD"]);
        }
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $class_array = array();
            $class_array = explode("-", $model->field["CLASSCD"]);
        }
        $query  = " SELECT DISTINCT";
        $query .= "     R1.VIEWCD, ";
        $query .= "     R1.STATUS AS VIEW, ";
        $query .= "     T2.VIEWNAME, ";
        $query .= "     T2.SHOWORDER ";
        $query .= " FROM ";
        $query .= "     JVIEWSTAT_RECORD_DAT R1 ";
        $query .= "     LEFT JOIN JVIEWNAME_GRADE_YDAT T1 ON T1.YEAR        = R1.YEAR";
        $query .= "                                      AND T1.SUBCLASSCD  = R1.SUBCLASSCD ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                                  AND  T1.CLASSCD = R1.CLASSCD ";
            $query .= "                                  AND  T1.SCHOOL_KIND = R1.SCHOOL_KIND ";
            $query .= "                                  AND  T1.CURRICULUM_CD = R1.CURRICULUM_CD ";
        }
        $query .= "                                      AND T1.VIEWCD      = R1.VIEWCD ";
        $query .= "     LEFT JOIN JVIEWNAME_GRADE_MST T2  ON T2.SUBCLASSCD  = R1.SUBCLASSCD ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                                 AND  T1.CLASSCD = R1.CLASSCD ";
            $query .= "                                 AND  T1.SCHOOL_KIND = R1.SCHOOL_KIND ";
            $query .= "                                 AND  T1.CURRICULUM_CD = R1.CURRICULUM_CD ";
        }
        $query .= "                                     AND T2.VIEWCD      = R1.VIEWCD ";
        $query .= " WHERE ";
        $query .= "    R1.YEAR = '".CTRL_YEAR."' AND ";
        $query .= "    R1.SEMESTER = '".$model->field["SEMESTER"]."' AND ";
        $query .= "    R1.SCHREGNO = '".$schregno."' AND ";
        $query .= "    T1.GRADE = '".substr($model->field["GRADE_HR_CLASS"],0,2)."' AND ";
        $query .= "    T1.GRADE = T2.GRADE AND ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "    R1.CLASSCD       = '".$subclass_array[0]."' AND ";
            $query .= "    R1.SCHOOL_KIND   = '".$subclass_array[1]."' AND ";
            $query .= "    R1.CURRICULUM_CD = '".$subclass_array[2]."' AND ";
            $query .= "    R1.SUBCLASSCD    = '".$subclass_array[3]."' AND ";
        } else {
            $query .= "    R1.SUBCLASSCD = '".$model->field["SUBCLASSCD"]."' AND ";
        }
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "  T1.CLASSCD = T2.CLASSCD AND ";
            $query .= "  T1.SCHOOL_KIND = T2.SCHOOL_KIND AND ";
            $query .= "  T1.CURRICULUM_CD = T2.CURRICULUM_CD AND ";
        }
        $query .= "    T1.SUBCLASSCD = T2.SUBCLASSCD AND ";
        $query .= "    T1.VIEWCD = T2.VIEWCD AND ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "    SUBSTR(R1.VIEWCD,1,2) = '".$class_array[0]."' ";
        } else {
            $query .= "    SUBSTR(R1.VIEWCD,1,2) = '".$model->field["CLASSCD"]."' ";
        }
        $query .= " ORDER BY ";
        $query .= "    T2.SHOWORDER, ";
        $query .= "    R1.VIEWCD ";

        return $query;
    }

    //全体のデータの取得
    function selectQuery($model, $subclassName) {
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $subclass_array = array();
            $subclass_array = explode("-", $model->field["SUBCLASSCD"]);
        }
        $query  = " WITH REPORTREMARK AS ( ";
        $query .= "     SELECT ";
        $query .= "         YEAR, ";
        $query .= "         SCHREGNO, ";
        $query .= "         REMARK1 ";
        $query .= "     FROM ";
        $query .= "         JVIEWSTAT_REPORTREMARK_DAT ";
        $query .= "     WHERE ";
        $query .= "         YEAR        = '". CTRL_YEAR ."' ";
        $query .= "     AND SEMESTER    = '{$model->field["SEMESTER"]}' ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     AND CLASSCD       = '".$subclass_array[0]."' ";
            $query .= "     AND SCHOOL_KIND   = '".$subclass_array[1]."' ";
            $query .= "     AND CURRICULUM_CD = '".$subclass_array[2]."' ";
            $query .= "     AND SUBCLASSCD    = '".$subclass_array[3]."' ";
        } else {
            $query .= "     AND SUBCLASSCD  = '{$model->field["SUBCLASSCD"]}' ";
        }

        $query .= " ), SCHINFO AS ( ";
        $query .= "     SELECT ";
        $query .= "         T1.SCHREGNO, ";
        $query .= "         T1.ATTENDNO, ";
        $query .= "         T2.NAME, ";
        $query .= "         T3.HR_NAME ";
        $query .= "     FROM ";
        $query .= "         SCHREG_REGD_DAT T1, ";
        $query .= "         SCHREG_BASE_MST T2, ";
        $query .= "         SCHREG_REGD_HDAT T3 ";
        $query .= "     WHERE ";
        $query .= "         T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND T1.SEMESTER = '".$model->field["SEMESTER"]."' ";
        $query .= "     AND T1.GRADE || T1.HR_CLASS = '".$model->field["GRADE_HR_CLASS"]."' ";
        $query .= "     AND T1.SCHREGNO = T2.SCHREGNO ";
        $query .= "     AND T1.YEAR = T3.YEAR ";
        $query .= "     AND T1.GRADE = T3.GRADE ";
        $query .= "     AND T1.HR_CLASS = T3.HR_CLASS ";
        $query .= "     AND T1.SEMESTER = T3.SEMESTER ";
        $query .= " ) ";
        
        //メイン処理
        $query .= " SELECT DISTINCT";
        $query .= "     T1.HR_NAME AS GRADE_HR_CLASS_NAME, ";
        $query .= "     T1.ATTENDNO, ";
        $query .= "     T1.NAME, ";
        $query .= "     '" .CTRL_YEAR. "'                 AS YEAR,     ";
        $query .= "     '".$model->field["SEMESTER"]."'   AS SEMESTER, ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     '".$model->field["SUBCLASSCD"]."' AS SUBCLASS,      ";
        $query .= "     '".$subclassName."'               AS SUBCLASS_NAME, ";
        $query .= "     T2.REMARK1 ";
        $query .= " FROM ";
        $query .= "     SCHINFO T1 ";
        $query .= " LEFT JOIN REPORTREMARK T2 ON T1.SCHREGNO = T2.SCHREGNO ";
        $query .= " ORDER BY ";
        $query .= "     T1.ATTENDNO ";

        return $query;
    }

    //マスタの追加（ＣＳＶデータより読込）
    function insertQueryCsv($model, &$data_arr)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $cnt = 0;    //処理件数
        for ($i = 0; $i < get_count($data_arr); $i++) {
            knjx_d139Query::deleteQuery($model, $db, $data_arr, $i);
            if (strlen($data_arr[$i]["REMARK1"])) knjx_d139Query::insertQuery($model, $db, $data_arr, $i);
            $cnt++;
        }
        $db->commit();
        Query::dbCheckIn($db);
        return $cnt;
    }

    //削除（ＣＳＶデータより読込）
    function deleteQueryCsv($model,&$data_arr)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $cnt = 0;    //処理件数
        for ($i = 0; $i < get_count($data_arr); $i++) {
            knjx_d139Query::deleteQuery($model, $db, $data_arr, $i);
            $cnt++;
        }
        $db->commit();
        Query::dbCheckIn($db);

        return $cnt;
    }

    //データ削除
    function deleteQuery($model, $db, $data_arr, $i)
    {
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $subclass_array = array();
            $subclass_array = explode("-", $data_arr[$i]["SUBCLASS"]);
        }

        $query  = " DELETE FROM ";
        $query .= "     JVIEWSTAT_REPORTREMARK_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR       = '".$data_arr[$i]["YEAR"]."'     AND ";
        $query .= "     SEMESTER   = '".$data_arr[$i]["SEMESTER"]."' AND ";
        $query .= "     SCHREGNO   = '".$data_arr[$i]["SCHREGNO"]."' AND ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     CLASSCD       = '".$subclass_array[0]."' AND ";
            $query .= "     SCHOOL_KIND   = '".$subclass_array[1]."' AND ";
            $query .= "     CURRICULUM_CD = '".$subclass_array[2]."' AND ";
            $query .= "     SUBCLASSCD    = '".$subclass_array[3]."'     ";
        } else {
            $query .= "     SUBCLASSCD    = '".$data_arr[$i]["SUBCLASS"]."' ";
        }
        $db->query($query);

    }

    //データ登録
    function insertQuery($model, $db, $data_arr, $i)
    {
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $subclass_array = array();
            $subclass_array = explode("-", $data_arr[$i]["SUBCLASS"]);
        }
        $remark = str_replace("↓", "\r\n" , $data_arr[$i]["REMARK1"]);

        //項目
        $data = array();
        $data["YEAR"][TEXT]          = $data_arr[$i]["YEAR"];
        $data["SEMESTER"][TEXT]      = $data_arr[$i]["SEMESTER"];
        $data["SCHREGNO"][TEXT]      = $data_arr[$i]["SCHREGNO"];
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $data["CLASSCD"][TEXT]          = $subclass_array[0];
            $data["SCHOOL_KIND"][TEXT]      = $subclass_array[1];
            $data["CURRICULUM_CD"][TEXT]    = $subclass_array[2];
            $data["SUBCLASSCD"][TEXT]       = $subclass_array[3];
        } else {
            $data["SUBCLASSCD"][TEXT]    = $data_arr[$i]["SUBCLASS"];
        }
        $data["REMARK1"][TEXT]       = $remark;
        $data["REGISTERCD"][TEXT]    = STAFFCD ;
        $data["UPDATED"][FUNC]       = "sysdate()";

        //追加
        $query = Query::insertSQL($data, "JVIEWSTAT_REPORTREMARK_DAT");
        $db->query($query);
    }

    //エラーＤＢへの追加
    function insertQueryErr(&$db, $record_no, $check_error)
    {
        $data1["PROGRAMID"][TEXT]   = PROGRAMID;
        $data1["MSGROW"][NUMBER]    = $record_no;
        $data1["MSGREMARK"][TEXT]   = $check_error;

        $query = Query::insertSQL($data1, "W_CSVMSG_PRG_DAT");
        $result = $db->query($query);
    }
}
?>
