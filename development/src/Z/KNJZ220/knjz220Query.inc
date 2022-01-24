<?php

require_once('for_php7.php');

class knjz220Query extends Query {

    //ヘッダー情報取得
    function getHeaderInfo()
    {
        //評定区分は固定値３
        $sep = "3";

        $query  = " SELECT                  ";
        $query .= "   ASSESSCD, ASSESSMEMO, ASSESSLEVELCNT ";
        $query .= " FROM                    ";
        $query .= "   ASSESS_HDAT           ";
        $query .= " WHERE                   ";
        $query .= "   ASSESSCD = '".$sep."' ";

        return $query;
    }

    //デフォルト値を取得
    function getDefultData()
    {
        //評定区分は固定値３
        $sep = "3";

        $query  = " SELECT                     ";
        $query .= "     ASSESSCD,              ";
        $query .= "     ASSESSLEVEL,           ";
        $query .= "     ASSESSMARK,            ";
        $query .= "     ASSESSLOW,             ";
        $query .= "     ASSESSHIGH             ";
        $query .= " FROM                       ";
        $query .= "     ASSESS_MST             ";
        $query .= " WHERE                      ";
        $query .= "     ASSESSCD = '".$sep."'  ";
        $query .= " ORDER BY                   ";
        $query .= "     ASSESSLEVEL,           ";
        $query .= "     ASSESSLEVEL            ";

        return $query;
    }

    //学年コンボボックスの値を取得
    function combo_grdQuery($model)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     GRADE_NAME1 AS SHOWGRADE, ";
        $query .= "     GRADE ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " AND SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     GRADE ";

        return $query;
    }

    //教科コンボボックスの値を取得
    function combo_clsQuery($model)
    {
        //return " SELECT CLASSCD,CLASSNAME FROM CLASS_MST where integer(classcd) < 50";

        $query  = " SELECT ";
        $query .= "   CLASSCD, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "   SCHOOL_KIND, ";
        }
        $query .= "   CLASSNAME ";
        $query .= " FROM ";
        $query .= "   V_CLASS_MST ";
        $query .= " WHERE ";
        $query .= "   YEAR = '".CTRL_YEAR."' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            $query .= " AND SCHOOL_KIND = (SELECT ";
            $query .= "                         SCHOOL_KIND ";
            $query .= "                     FROM ";
            $query .= "                         SCHREG_REGD_GDAT ";
            $query .= "                     WHERE ";
            $query .= "                             YEAR  = '".CTRL_YEAR."' ";
            $query .= "                         AND GRADE = '".$model->grade."' ";
            $query .= "                     ) ";
        } else if ($model->Properties["useCurriculumcd"] == '1' && $model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " ORDER BY ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "   CLASSCD, ";
            $query .= "   SCHOOL_KIND ";
        } else {
            $query .= "   CLASSCD ";
        }

        return $query;
    }

    //評定コピーコンボボックスの値を取得
    function Copy_comboQuery($gd, $model)
    {
        //評定区分は固定値３
        $sep = "3";

        $query  = " SELECT DISTINCT ";
        $query .= "   R.GRADE, ";
        $query .= "   R.ASSESSCD, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "   S.CLASSCD || '-' || S.SCHOOL_KIND || '-' || S.CURRICULUM_CD || '-' || S.SUBCLASSCD AS SUBCLASSCD, ";
        } else {
            $query .= "   S.SUBCLASSCD AS SUBCLASSCD, ";
        }
        $query .= "   S.SUBCLASSNAME ";
        $query .= " FROM                       ";
        $query .= "   RELATIVEASSESS_MST R  ";
        $query .= " LEFT JOIN SUBCLASS_MST S ON R.SUBCLASSCD = S.SUBCLASSCD ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                         AND R.CLASSCD       = S.CLASSCD ";
            $query .= "                         AND R.SCHOOL_KIND   = S.SCHOOL_KIND ";
            $query .= "                         AND R.CURRICULUM_CD = S.CURRICULUM_CD ";
        }
        $query .= " WHERE                      ";
        $query .= "   R.ASSESSCD = '".$sep."'  ";
        $query .= " AND                        ";
        $query .= "   R.GRADE    = '".$gd."'   ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            $query .= " AND R.SCHOOL_KIND = (SELECT ";
            $query .= "                          SCHOOL_KIND ";
            $query .= "                      FROM ";
            $query .= "                          SCHREG_REGD_GDAT ";
            $query .= "                      WHERE ";
            $query .= "                              YEAR  = '".CTRL_YEAR."' ";
            $query .= "                          AND GRADE = '".$gd."' ";
            $query .= "                      ) ";
        } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND R.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " ORDER BY                   ";
        $query .= "   SUBCLASSCD             ";

        return $query;
    }

    //リスト内情報取得
    function ReadQuery($cd, $model)
    {
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $class_array = array();
            $class_array = explode("-", $cd);
        }
        $query  = " SELECT ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "   CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD AS VALUE, ";
        } else {
            $query .= "   SUBCLASSCD AS VALUE, ";
        }
        $query .= "   SUBCLASSNAME ";
        $query .= " FROM ";
        $query .= "   V_SUBCLASS_MST ";
        $query .= " WHERE ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "   CLASSCD          = '".$class_array[0]."' AND ";
            $query .= "   SCHOOL_KIND      = '".$class_array[1]."' ";
        } else {
            $query .= "   SUBSTR(SUBCLASSCD,1,2) = '".$cd."' ";
        }
        $query .= " AND ";
        $query .= "   YEAR = '".CTRL_YEAR."' ";
        $query .= " ORDER  BY ";
        $query .= "   VALUE ";

        return $query;
    }

    //評定マスタ情報抽出
    function selectQuery($model)
    {
        //区分は３で固定。
        $sep = "3";

        //コピー情報取得？
        if($model->copy_flg == true) {
            $sbclss = $model->copy_subclasscd;
            $grd    = $model->copy_grade;
        } else if($model->copy_flg == false){
            $sbclss = $model->field1["SUBCLASSCD"];
            $grd    = $model->grade;
        }

        $query  = " SELECT ";
        $query .= "     ASSESSLEVEL, ";
        $query .= "     ASSESSCD, ";
        $query .= "     ASSESSMARK, ";
        $query .= "     ASSESSLOW, ";
        $query .= "     ASSESSHIGH, ";
        $query .= "     UPDATED ";
        $query .= " FROM ";
        $query .= "     RELATIVEASSESS_MST ";
        $query .= " WHERE ";
        $query .= "     ASSESSCD   = '".$sep."' ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= " AND CLASSCD         = '".substr($sbclss, 0, 2)."' ";
            $query .= " AND SCHOOL_KIND     = '".substr($sbclss, 3, 1)."' ";
            $query .= " AND CURRICULUM_CD   = '".substr($sbclss, 5, 1)."' ";
            $query .= " AND SUBCLASSCD      = '".substr($sbclss, 7, 6)."' ";
        } else {
            $query .= " AND SUBCLASSCD = '".$sbclss."' ";
        }
        $query .= " AND GRADE  = '".$grd."' ";
        $query .= " ORDER BY ";
        $query .= "      ASSESSLOW ";

        return $query;
    }

    //delete処理
    function &getDeleteQuery($model)
    {
        //区分は３で固定。
        $sep = "3";

        $db = Query::dbCheckOut();

        $query  = " DELETE FROM ";
        $query .= "     RELATIVEASSESS_MST ";
        $query .= " WHERE ";
        $query .= "     ASSESSCD   = '".$sep."' ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= " AND  CLASSCD        = '".substr($model->field1["SUBCLASSCD"], 0, 2)."' ";
            $query .= " AND  SCHOOL_KIND    = '".substr($model->field1["SUBCLASSCD"], 3, 1)."' ";
            $query .= " AND  CURRICULUM_CD  = '".substr($model->field1["SUBCLASSCD"], 5, 1)."' ";
            $query .= " AND  SUBCLASSCD     = '".substr($model->field1["SUBCLASSCD"], 7, 6)."' ";
        } else {
            $query .= " AND  SUBCLASSCD = '".$model->field1["SUBCLASSCD"]."'  ";
        }
        $query .= " AND  GRADE = '".$model->grade."' ";

        $db->query($query);
        Query::dbCheckIn($db);

        return true;
    }

    //insert処理
    function &getInsertQuery($model)
    {
        //最大値、最小値を取得
        $min = $model->default_val["LOW"];
        $max = $model->default_val["HIGH"];
        //レコード数
        $cnt = $model->TBL_COUNT;
        //db open
        $db = Query::dbCheckOut();

        for($i=1; $i<=$cnt; $i++)
        {
            $ASSESSLOW  = "ASSESSLOW".($i);

            $data["GRADE"][TEXT] = $model->grade;
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $data["CLASSCD"][TEXT]       = substr($model->field1["SUBCLASSCD"], 0, 2);
                $data["SCHOOL_KIND"][TEXT]   = substr($model->field1["SUBCLASSCD"], 3, 1);
                $data["CURRICULUM_CD"][TEXT] = substr($model->field1["SUBCLASSCD"], 5, 1);
                $data["SUBCLASSCD"][TEXT]    = substr($model->field1["SUBCLASSCD"], 7, 6);
            } else {
                $data["SUBCLASSCD"][TEXT]    = $model->field1["SUBCLASSCD"];
            }
            $data["ASSESSMARK"][TEXT]    = $model->field2["ASSESSMARK".$i];
            $data["REGISTERCD"][TEXT]    = STAFFCD;
            $data["ASSESSCD"][TEXT]      = "3";
            $data["ASSESSLEVEL"][NUMBER] = ($i);
            $data["ASSESSLOW"][NUMBER]   = ($i == 1)? $min : $model->field2[$ASSESSLOW];
            $data["ASSESSHIGH"][NUMBER]  = ($i == $cnt)? $max : ((int)($model->field2["ASSESSLOW".($i + 1)])-1);
            $data["UPDATED"][FUNC]       = "sysdate()";
            $query = Query::insertSQL($data, "relativeassess_mst");

            //echo $query;
            $db->query($query);
        }
        Query::dbCheckIn($db);
        return true;
    }
}
?>
