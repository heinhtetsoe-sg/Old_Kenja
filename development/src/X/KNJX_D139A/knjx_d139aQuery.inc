<?php

require_once('for_php7.php');

class knjx_d139aquery extends Query {

    //名称マスタの取得
    function getNameMst($namecd1)
    {
        $query  = " SELECT ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     NAMECD1 = '".$namecd1."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //学年取得
    function getGrade($model, $schregno){
    
        $query  = " SELECT ";
        $query .= "     GRADE ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT  ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     SEMESTER = '".$model->field["SEMESTER2"]."' AND ";
        $query .= "     SCHREGNO = '".$schregno."' ";

        return $query;
    }

    //教科データの取得
    function selectSubclassQuery($model, $selval="")
    {
        $query  = "SELECT ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "    T3.classcd || '-' || T3.SCHOOL_KIND AS VALUE, ";
            if ($selval !== "") {
                $query .= "    T3.classname AS LABEL ";
            } else {
                $query .= "    T3.classcd || '-' || T3.SCHOOL_KIND || ' ' || T3.classname AS LABEL ";
            }
        } else {
            $query .= "    T3.classcd AS VALUE, ";
            if ($selval !== "") {
                $query .= "    T3.classname AS LABEL ";
            } else {
                $query .= "    T3.classcd || ' ' || T3.classname AS LABEL ";
            }
        }
        $query .= "FROM ";
        $query .= "    chair_dat T1, ";
        $query .= "    chair_std_dat T2, ";
        $query .= "    V_CLASS_MST T3, ";
        $query .= "    chair_stf_dat T4 ";
        $query .= "WHERE ";
        $query .= "    T1.year       = '".CTRL_YEAR."' AND ";
        $query .= "    T1.semester   = '".$model->field["SEMESTER2"]."' AND ";
        $query .= "    T2.year       = T1.year AND ";
        $query .= "    T3.year       = T1.year AND ";
        $query .= "    T1.year       = T4.year AND ";
        $query .= "    T1.semester   = T4.semester AND ";
        $query .= "    T1.chaircd    = T4.chaircd AND ";
        $query .= "    T2.semester   = T1.semester AND ";
        $query .= "    T2.chaircd    = T1.chaircd AND ";
        $query .= "    T3.classcd < '90' AND ";
        $query .= "    T3.classcd IN (SELECT substr(J1.viewcd,1,2) FROM jviewname_grade_ydat J1 WHERE J1.year = '".CTRL_YEAR."' GROUP BY substr(J1.viewcd,1,2)) AND ";
        //参照・更新可（制限付き）
        if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT) {
            $query .= "     t4.STAFFCD = '".STAFFCD."' AND ";
        }
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= " T3.SCHOOL_KIND = T1.SCHOOL_KIND AND ";
        }
        $query .= "    T3.classcd = substr(T1.subclasscd,1,2) ";
        if ($selval !== "") {
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "    AND T3.classcd || '-' || T3.SCHOOL_KIND = '".$selval."' ";
            } else {
                $query .= "    AND T3.classcd  = '".$selval."' ";
            }
        }
        $query .= "GROUP BY ";
        $query .= "    T3.classcd, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= " T3.SCHOOL_KIND, ";
        }
        $query .= "    T3.classname ";
        $query .= "ORDER BY ";
        $query .= "    VALUE ";
        
        return $query;
    }

    //科目取得
    function getSubclassMst($model, $selval="")
    {
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $class_array = array();
            $class_array = explode("-", $model->field["CLASSCD"]);
        }
        $query  = " SELECT DISTINCT ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T2.CLASSCD || '-' || T2.SCHOOL_KIND || '-' || T2.CURRICULUM_CD || '-' || T2.SUBCLASSCD AS VALUE, ";
            if ($selval !== "") {
                $query .= "     T2.SUBCLASSNAME AS LABEL ";
            } else {
                $query .= "     T2.CLASSCD || '-' || T2.SCHOOL_KIND || '-' || T2.CURRICULUM_CD || '-' || T2.SUBCLASSCD || '：' || T2.SUBCLASSNAME AS LABEL ";
            }
        } else {
            $query .= "     T2.SUBCLASSCD AS VALUE, ";
            if ($selval !== "") {
                $query .= "     T2.SUBCLASSNAME AS LABEL ";
            } else {
                $query .= "     T2.SUBCLASSCD || ' ' || T2.SUBCLASSNAME AS LABEL ";
            }
        }
        $query .= " FROM ";
        $query .= "     JVIEWNAME_GRADE_YDAT T1, ";
        $query .= "     V_SUBCLASS_MST T2, ";
        $query .= "     chair_dat T3, ";
        $query .= "     chair_std_dat T4, ";
        $query .= "     chair_stf_dat T5 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = T2.YEAR AND ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' AND ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T1.CLASSCD = T2.CLASSCD AND ";
            $query .= "     T1.SCHOOL_KIND = T2.SCHOOL_KIND AND ";
            $query .= "     T1.CURRICULUM_CD = T2.CURRICULUM_CD AND ";
        }
        $query .= "     T1.SUBCLASSCD = T2.SUBCLASSCD AND ";
        $query .= "     T1.year       = T3.year AND ";
        $query .= "     T4.year       = T3.year AND ";
        $query .= "     T3.year       = T5.year AND ";
        $query .= "     T3.semester   = '".$model->field["SEMESTER2"]."' AND ";
        $query .= "     T3.semester   = T5.semester AND ";
        $query .= "     T3.chaircd    = T5.chaircd AND ";
        $query .= "     T4.semester   = T3.semester AND ";
        $query .= "     T4.chaircd    = T3.chaircd AND ";
        //参照・更新可（制限付き）
        if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT) {
            $query .= "     T5.STAFFCD = '".STAFFCD."' AND ";
        }
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     SUBSTR(T1.SUBCLASSCD,1,2) = '".$class_array[0]."' AND ";
            $query .= "     T1.SCHOOL_KIND = '".$class_array[1]."' ";
        } else {
            $query .= "     SUBSTR(T1.SUBCLASSCD,1,2) = '".$model->field["CLASSCD"]."' ";
        }
        if ($selval !== "") {
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "     AND T2.CLASSCD || '-' || T2.SCHOOL_KIND || '-' || T2.CURRICULUM_CD || '-' || T2.SUBCLASSCD = '".$selval."' ";
            } else {
                $query .= "     AND T2.SUBCLASSCD = '".$selval."' ";
            }
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //科目取得
    function chkSubclassMst($model, $subclscd, $semes)
    {
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $class_array = array();
            $class_array = explode("-", $subclscd);
        }
        $query  = " SELECT DISTINCT ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T2.CLASSCD || '-' || T2.SCHOOL_KIND || '-' || T2.CURRICULUM_CD || '-' || T2.SUBCLASSCD AS VALUE, ";
            $query .= "     T2.CLASSCD || '-' || T2.SCHOOL_KIND || '-' || T2.CURRICULUM_CD || '-' || T2.SUBCLASSCD || '：' || T2.SUBCLASSNAME AS LABEL ";
        } else {
            $query .= "     T2.SUBCLASSCD AS VALUE, ";
            $query .= "     T2.SUBCLASSCD || ' ' || T2.SUBCLASSNAME AS LABEL ";
        }
        $query .= " FROM ";
        $query .= "     JVIEWNAME_GRADE_YDAT T1, ";
        $query .= "     V_SUBCLASS_MST T2, ";
        $query .= "     chair_dat T3, ";
        $query .= "     chair_std_dat T4, ";
        $query .= "     chair_stf_dat T5 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = T2.YEAR AND ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' AND ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T1.CLASSCD = T2.CLASSCD AND ";
            $query .= "     T1.SCHOOL_KIND = T2.SCHOOL_KIND AND ";
            $query .= "     T1.CURRICULUM_CD = T2.CURRICULUM_CD AND ";
        }
        $query .= "     T1.SUBCLASSCD = T2.SUBCLASSCD AND ";
        $query .= "     T1.year       = T3.year AND ";
        $query .= "     T4.year       = T3.year AND ";
        $query .= "     T3.year       = T5.year AND ";
        $query .= "     T3.semester   = '".$semes."' AND ";
        $query .= "     T3.semester   = T5.semester AND ";
        $query .= "     T3.chaircd    = T5.chaircd AND ";
        $query .= "     T4.semester   = T3.semester AND ";
        $query .= "     T4.chaircd    = T3.chaircd AND ";
        //参照・更新可（制限付き）
        if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT) {
            $query .= "     T5.STAFFCD = '".STAFFCD."' AND ";
        }
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T2.CLASSCD || '-' || T2.SCHOOL_KIND || '-' || T2.CURRICULUM_CD || '-' || T2.SUBCLASSCD = '".$subclscd."' ";
        } else {
            $query .= "     T2.SUBCLASSCD = '".$subclscd."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //講座一覧取得
    function getChairList($model, $selval="")
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     T1.CHAIRCD AS VALUE, ";
        if ($selval !== "") {
            $query .= "     VALUE(T1.CHAIRNAME, '') AS LABEL ";
        } else {
            $query .= "     T1.CHAIRCD || ' ' || VALUE(T1.CHAIRNAME, '') AS LABEL ";
        }
        $query .= " FROM ";
        $query .= "     CHAIR_DAT T1, ";
        $query .= "     CHAIR_STF_DAT T2 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = T2.YEAR AND ";
        $query .= "     T1.SEMESTER = T2.SEMESTER AND ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     T1.SEMESTER = '".$model->field["SEMESTER"]."' AND ";
        $query .= "     T1.CHAIRCD = T2.CHAIRCD AND ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD = '".$model->field["SUBCLASSCD"]."' ";
        } else {
            $query .= "     T1.SUBCLASSCD = '".$model->field["SUBCLASSCD"]."' ";
        }
        if ($selval !== "") {
            $query .= "     AND T1.CHAIRCD = '".$selval."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //マスタの追加（ＣＳＶデータより読込）
    function insertQueryCsv($model, &$data_arr)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $cnt = 0;   //処理件数
        for ($i = 0; $i < get_count($data_arr); $i++) {
            $getCnt = $db->getOne(knjx_d139aQuery::chkReportRemarkQuery($model, $data_arr[$i]["YEAR"], $data_arr[$i]["SEMESTER"], $data_arr[$i]["SCHREGNO"], $data_arr[$i]["SUBCLASSCD"]));
            
            $data["REMARK1"][TEXT]    = $data_arr[$i]["COMMENT"];
            $data["REGISTERCD"][TEXT] = STAFFCD;
            $data["UPDATED"][FUNC]    = "sysdate()";
            if ($getCnt == 0) {
                //新規
                $data["YEAR"][TEXT]             = $data_arr[$i]["YEAR"];
                $data["SEMESTER"][TEXT]         = $data_arr[$i]["SEMESTER"];
                $data["SCHREGNO"][TEXT]         = $data_arr[$i]["SCHREGNO"];
                if ($model->Properties["useCurriculumcd"] == '1') {
                    $cutwk = explode("-", $data_arr[$i]["SUBCLASSCD"]);
                    $data["CLASSCD"][TEXT]          = $cutwk[0];
                    $data["SCHOOL_KIND"][TEXT]      = $cutwk[1];
                    $data["CURRICULUM_CD"][TEXT]    = $cutwk[2];
                    $data["SUBCLASSCD"][TEXT]       = $cutwk[3];
                } else {
                    $data["CLASSCD"][TEXT]          = substr($data_arr[$i]["SUBCLASSCD"],0,2);
                    $data["SUBCLASSCD"][TEXT]       = $data_arr[$i]["SUBCLASSCD"];
                }
                $query = Query::insertSQL($data, "JVIEWSTAT_REPORTREMARK_DAT");
                $db->query($query);
            } else {
                //更新
                $where  = " WHERE ";
                $where .= " YEAR = '".$data_arr[$i]["YEAR"]."' ";
                $where .= " AND SEMESTER = '".$data_arr[$i]["SEMESTER"]."' ";
                $where .= " AND SCHREGNO = '".$data_arr[$i]["SCHREGNO"]."' ";
                if ($model->Properties["useCurriculumcd"] == '1') {
                    $cutwk = explode("-", $data_arr[$i]["SUBCLASSCD"]);
                    $where .= " AND CLASSCD = '".$cutwk[0]."' ";
                    $where .= " AND SCHOOL_KIND = '".$cutwk[1]."' ";
                    $where .= " AND CURRICULUM_CD = '".$cutwk[2]."' ";
                    $where .= " AND SUBCLASSCD = '".$cutwk[3]."' ";
                } else {
                    $where .= " AND CLASSCD = '".substr($data_arr[$i]["SUBCLASSCD"],0,2)."' ";
                    $where .= " AND SUBCLASSCD = '".$data_arr[$i]["SUBCLASSCD"]."' ";
                }
                $query = Query::updateSQL($data, "JVIEWSTAT_REPORTREMARK_DAT", $where);
                $db->query($query);
            }
            $cnt++;
        }

        $db->commit();
        Query::dbCheckIn($db);

        return $cnt;
    }

    //削除（ＣＳＶデータより読込）
    function DeleteQueryCsv($model, &$data_arr)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $cnt = 0;   //処理件数
        for ($i = 0; $i < get_count($data_arr); $i++) {
            $query  = " DELETE FROM ";
            $query .= "     JVIEWSTAT_REPORTREMARK_DAT ";
            $query .= " WHERE ";
            $query .= "     YEAR        = '".$data_arr[$i]["YEAR"]."' AND ";
            $query .= "     SEMESTER    = '".$data_arr[$i]["SEMESTER"]."' AND ";
            $query .= "     SCHREGNO    = '".$data_arr[$i]["SCHREGNO"]."' AND ";
            if ($model->Properties["useCurriculumcd"] == '1') {
                $cutwk = explode("-", $data_arr[$i]["SUBCLASSCD"]);
                $query .= "     CLASSCD         = '".$cutwk[0]."' AND ";
                $query .= "     SCHOOL_KIND     = '".$cutwk[1]."' AND ";
                $query .= "     CURRICULUM_CD   = '".$cutwk[2]."' AND ";
                $query .= "     SUBCLASSCD      = '".$cutwk[3]."' ";
            } else {
                $query .= "     SUBCLASSCD  = '".$data_arr[$i]["SUBCLASSCD"]."' ";
            }

            $db->query($query);
            $cnt++;
        }
        $db->commit();
        Query::dbCheckIn($db);

        return $cnt;
    }

    //存在チェック（在籍）
    function checkRegdDat($model, $year, $semester, $schregno)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= " WHERE ";
        $query .= "     YEAR     = '".$year."' AND ";
        $query .= "     SEMESTER = '".$semester."' AND ";
        $query .= "     SCHREGNO = '".$schregno."' ";

        return $query;    
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

    //CSVデータ出力
    function selectQuery($model, $execute_date) {
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
        $query .= "         T1.ATTENDNO, ";
        $query .= "         T1.SCHREGNO, ";
        $query .= "         T2.NAME, ";
        $query .= "         T3.HR_NAME ";
        $query .= "     FROM ";
        $query .= "         SCHREG_REGD_DAT T1, ";
        $query .= "         SCHREG_BASE_MST T2, ";
        $query .= "         SCHREG_REGD_HDAT T3, ";
        $query .= "         CHAIR_STD_DAT T4 ";
        $query .= "     WHERE ";
        $query .= "         T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND T1.SEMESTER = '".$model->field["SEMESTER2"]."' ";
        $query .= "     AND T1.SCHREGNO = T2.SCHREGNO ";
        $query .= "     AND T1.YEAR = T3.YEAR ";
        $query .= "     AND T1.SEMESTER = T3.SEMESTER ";
        $query .= "     AND T1.GRADE = T3.GRADE ";
        $query .= "     AND T1.HR_CLASS = T3.HR_CLASS ";
        $query .= "     AND T4.YEAR      = T1.YEAR ";
        $query .= "     AND T4.CHAIRCD   = '".$model->field["CHAIRCD"]."' ";
        $query .= "     AND T4.SCHREGNO  = T1.SCHREGNO ";
        $query .= "     AND '".$execute_date."' BETWEEN T4.APPDATE AND T4.APPENDDATE ";
        $query .= " ) ";
        
        //メイン処理
        $query .= " SELECT DISTINCT";
        $query .= "     T1.HR_NAME, ";
        $query .= "     T1.ATTENDNO, ";
        $query .= "     T1.NAME, ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T2.REMARK1";
        $query .= " FROM ";
        $query .= "     SCHINFO T1 ";
        $query .= " LEFT JOIN REPORTREMARK T2 ON T1.SCHREGNO = T2.SCHREGNO ";
        $query .= " ORDER BY ";
        $query .= "     T1.ATTENDNO ";

        return $query;
    }

    //CSVデータ出力
    function chkReportRemarkQuery($model, $year, $semes, $schregno, $subclasscd) {
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $subclass_array = array();
            $subclass_array = explode("-", $subclasscd);
        }

        $query  = "     SELECT ";
        $query .= "         YEAR, ";
        $query .= "         SCHREGNO, ";
        $query .= "         REMARK1 ";
        $query .= "     FROM ";
        $query .= "         JVIEWSTAT_REPORTREMARK_DAT ";
        $query .= "     WHERE ";
        $query .= "         YEAR        = '{$year}' ";
        $query .= "     AND SEMESTER    = '{$semes}' ";
        $query .= "     AND SCHREGNO    = '{$schregno}' ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     AND CLASSCD       = '".$subclass_array[0]."' ";
            $query .= "     AND SCHOOL_KIND   = '".$subclass_array[1]."' ";
            $query .= "     AND CURRICULUM_CD = '".$subclass_array[2]."' ";
            $query .= "     AND SUBCLASSCD    = '".$subclass_array[3]."' ";
        } else {
            $query .= "     AND SUBCLASSCD  = '{$subclasscd}' ";
        }

        return $query;
    }

    //観点取得
    function selectViewcdQuery($model, $schregno, $execute_date, $grade){
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $subclass_array = array();
            $subclass_array = explode("-", $model->field["SUBCLASSCD"]);
            $class_array = array();
            $class_array = explode("-", $model->field["CLASSCD"]);
        }
        $query  = " SELECT DISTINCT";
        $query .= "     R1.VIEWCD, ";
        $query .= "     R1.STATUS, ";
        $query .= "     T2.VIEWNAME, ";
        $query .= "     T2.SHOWORDER ";
        $query .= " FROM ";
        $query .= "     JVIEWSTAT_RECORD_DAT R1 ";
        $query .= "     LEFT JOIN JVIEWNAME_GRADE_YDAT T1 ON T1.YEAR        = R1.YEAR";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                                  AND T1.CLASSCD         = R1.CLASSCD ";
            $query .= "                                  AND T1.SCHOOL_KIND     = R1.SCHOOL_KIND ";
            $query .= "                                  AND T1.CURRICULUM_CD   = R1.CURRICULUM_CD ";
        }
        $query .= "                                      AND T1.SUBCLASSCD  = R1.SUBCLASSCD ";
        $query .= "                                      AND T1.VIEWCD      = R1.VIEWCD ";
        $query .= "     LEFT JOIN JVIEWNAME_GRADE_MST T2  ON T2.SUBCLASSCD  = R1.SUBCLASSCD ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                                  AND T2.CLASSCD = R1.CLASSCD ";
            $query .= "                                  AND T2.SCHOOL_KIND = R1.SCHOOL_KIND ";
            $query .= "                                  AND T2.CURRICULUM_CD = R1.CURRICULUM_CD ";
        }
        $query .= "                                      AND T2.VIEWCD      = R1.VIEWCD ";
        $query .= "     LEFT JOIN CHAIR_STD_DAT T3 ON T3.YEAR = R1.YEAR ";
        $query .= "                                      AND T3.SEMESTER = '".$model->field["SEMESTER2"]."' ";
        $query .= "                                      AND T3.CHAIRCD = '".$model->field["CHAIRCD"]."' ";
        $query .= "                                      AND T3.SCHREGNO = R1.SCHREGNO ";
        $query .= "                                      AND '".$execute_date."' BETWEEN T3.APPDATE AND T3.APPENDDATE ";
        $query .= " WHERE ";
        $query .= "    R1.YEAR = '".CTRL_YEAR."' AND ";
        $query .= "    R1.SEMESTER = '".$model->field["SEMESTER"]."' AND ";
        $query .= "    R1.SCHREGNO = '".$schregno."' AND ";
        $query .= "    T1.GRADE = T2.GRADE AND ";
        $query .= "    T1.GRADE = '".$grade."' AND ";
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

}
?>