<?php

require_once('for_php7.php');

class knjd520kQuery extends Query
{

    //処理年度より学年を取得
    function get_grade_data($a_year)
    {
        $query = false;
        if($a_year != "")
        {
            $query = "  SELECT
                            GRADE
                        FROM
                            SCHREG_REGD_HDAT 
                        WHERE
                            YEAR = '".$a_year."'
                        GROUP BY
                            GRADE ";
        }
        return $query;
    }

    //追試験対象科目を取得
    function get_subject_data($a_year, $a_semes, $gkcode, $model)
    {
        $query = false;
        if($a_year != "")
        {
            $query  = " SELECT DISTINCT ";
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "    T1.CLASSCD || '-' || ";
                $query .= "    T1.SCHOOL_KIND || '-' || ";
                $query .= "    T1.CURRICULUM_CD || '-' || ";
            }
            $query .= "     T1.SUBCLASSCD AS SUBCLASSCD, ";
            $query .= "     T2.SUBCLASSABBV, ";
            $query .= "     T2.ELECTDIV, ";
            $query .= "     COALESCE(T2.ELECTDIV,'0') ";
            $query .= " FROM ";
            $query .= "     SUPP_EXA_DAT T1 ";
            $query .= " LEFT OUTER JOIN ";
            $query .= "     V_SUBCLASS_MST T2 ";
            $query .= " ON ";
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "     T1.CLASSCD = T2.CLASSCD AND ";
                $query .= "     T1.SCHOOL_KIND = T2.SCHOOL_KIND AND ";
                $query .= "     T1.CURRICULUM_CD = T2.CURRICULUM_CD AND ";
            }
            $query .= "     T1.SUBCLASSCD = T2.SUBCLASSCD AND ";
            $query .= "     T1.YEAR = T2.YEAR ";
            $query .= " LEFT OUTER JOIN ";
            $query .= "     SCHREG_REGD_DAT T3 ";
            $query .= " ON ";
            $query .= "     T1.SCHREGNO = T3.SCHREGNO AND  ";
            $query .= "     T1.YEAR = T3.YEAR ";
            $query .= " WHERE ";
            $query .= "     T1.YEAR = '{$a_year}' AND ";
            $query .= "     T3.SEMESTER = '{$a_semes}' AND ";
            $query .= "     T3.GRADE = '{$gkcode}' ";
            $query .= " ORDER BY ";
            $query .= "     COALESCE(T2.ELECTDIV,'0'), ";
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "    T1.CLASSCD || '-' || ";
                $query .= "    T1.SCHOOL_KIND || '-' || ";
                $query .= "    T1.CURRICULUM_CD || '-' || ";
            }
            $query .= "     T1.SUBCLASSCD ";
        }

        return $query;

    }


    //追試験対象者を取得
    function get_supp_name($a_year, $a_semes, $gkcd)
    {
        $query = "  SELECT DISTINCT
                        T1.SCHREGNO,
                        T2.NAME_SHOW,
                        T3.ATTENDNO,
                        T4.HR_NAMEABBV
                    FROM
                        SUPP_EXA_DAT T1
                    LEFT OUTER JOIN
                        SCHREG_BASE_MST T2
                    ON
                        T1.SCHREGNO = T2.SCHREGNO 
                    LEFT OUTER JOIN
                        SCHREG_REGD_DAT T3
                    ON
                        T1.SCHREGNO = T3.SCHREGNO AND
                        T1.YEAR = T3.YEAR
                    LEFT OUTER JOIN
                        SCHREG_REGD_HDAT T4
                    ON
                        T1.YEAR = T4.YEAR AND
                        T3.SEMESTER = T4.SEMESTER AND
                        T3.GRADE = T4.GRADE AND 
                        T3.HR_CLASS = T4.HR_CLASS
                    WHERE
                        T1.YEAR = '".$a_year."' AND
                        T3.SEMESTER = '".$a_semes."' AND
                        T3.GRADE = '".$gkcd."'
                    ORDER BY
                        T4.HR_NAMEABBV,
                        T3.ATTENDNO,
                        T1.SCHREGNO ";
        return $query;
    }


    //メインデータ取得
    function get_main_data($a_year, $schregno, $range_cd, $model)
    {
        $query  = " SELECT ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "    T1.CLASSCD || '-' || ";
            $query .= "    T1.SCHOOL_KIND || '-' || ";
            $query .= "    T1.CURRICULUM_CD || '-' || ";
        }
        $query .= "     T1.SUBCLASSCD AS SUBCLASSCD, ";
        $query .= "     T1.SCORE, ";
        $query .= "     T1.DI_MARK, ";
        $query .= "     T1.JUDGE_PATTERN, ";
        $query .= "     T2.TYPE_ASSES_LOW ";
        $query .= " FROM ";
        $query .= "     SUPP_EXA_DAT T1 ";
        $query .= " LEFT OUTER JOIN ";
        $query .= "     TYPE_ASSES_MST T2 ";
        $query .= " ON ";
        $query .= "     T1.YEAR = T2.YEAR AND ";
        $query .= "     T1.JUDGE_PATTERN = T2.TYPE_ASSES_CD  AND ";
        $query .= "     T2.TYPE_ASSES_LEVEL = '2' ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '{$a_year}' AND ";
        $query .= "     T1.SCHREGNO = '{$schregno}' AND ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "    T1.CLASSCD || '-' || ";
            $query .= "    T1.SCHOOL_KIND || '-' || ";
            $query .= "    T1.CURRICULUM_CD || '-' || ";
        }
        $query .= "     T1.SUBCLASSCD IN ( ";

        for($i=0; $i<get_count($range_cd); $i++) {
            $query .= "'".$range_cd[$i]."'";

            if (!isset($range_cd[$i + 1])) {
                break;
            } else {
                $query .= ",";
            }
        }

        $query .= ")";
        $query .= " ORDER BY ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "    T1.CLASSCD || '-' || ";
            $query .= "    T1.SCHOOL_KIND || '-' || ";
            $query .= "    T1.CURRICULUM_CD || '-' || ";
        }
        $query .= "     T1.SUBCLASSCD " ;

        return $query;
    }


    //編集対象の科目を取得
    function get_edit_data($a_year, $subcd, $model)
    {

        $query  = " SELECT DISTINCT ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "    T1.CLASSCD || '-' || ";
            $query .= "    T1.SCHOOL_KIND || '-' || ";
            $query .= "    T1.CURRICULUM_CD || '-' || ";
        }
        $query .= "     T1.SUBCLASSCD AS SUBCLASSCD, ";
        $query .= "     T2.SUBCLASSABBV, ";
        $query .= "     T2.ELECTDIV ";
        $query .= " FROM ";
        $query .= "     SUPP_EXA_DAT T1 ";
        $query .= " LEFT OUTER JOIN ";
        $query .= "     V_SUBCLASS_MST T2 ";
        $query .= " ON ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T1.CLASSCD = T2.CLASSCD AND ";
            $query .= "     T1.SCHOOL_KIND = T2.SCHOOL_KIND AND ";
            $query .= "     T1.CURRICULUM_CD = T2.CURRICULUM_CD AND ";
        }
        $query .= "     T1.SUBCLASSCD = T2.SUBCLASSCD AND ";
        $query .= "     T1.YEAR = T2.YEAR ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '{$a_year}' AND ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "    T1.CLASSCD || '-' || ";
            $query .= "    T1.SCHOOL_KIND || '-' || ";
            $query .= "    T1.CURRICULUM_CD || '-' || ";
        }
        $query .= "     T1.SUBCLASSCD = '{$subcd}' ";

        return $query;
    }


    //編集用メインデータ取得
    function edit_main_data($a_year, $a_semes, $gkcd, $subcd, $model)
    {
        $query  = " SELECT ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T1.SCORE, ";
        $query .= "     T1.DI_MARK, ";
        $query .= "     T1.JUDGE_PATTERN, ";
        $query .= "     T2.NAME_SHOW, ";
        $query .= "     T3.ATTENDNO, ";
        $query .= "     T4.HR_NAMEABBV, ";
        $query .= "     T5.TYPE_ASSES_LOW ";
        $query .= " FROM ";
        $query .= "     SUPP_EXA_DAT T1 ";
        $query .= " LEFT OUTER JOIN ";
        $query .= "     SCHREG_BASE_MST T2 ";
        $query .= " ON ";
        $query .= "     T1.SCHREGNO = T2.SCHREGNO ";
        $query .= " LEFT OUTER JOIN ";
        $query .= "     SCHREG_REGD_DAT T3 ";
        $query .= " ON ";
        $query .= "     T1.SCHREGNO = T3.SCHREGNO AND ";
        $query .= "     T1.YEAR = T3.YEAR ";
        $query .= " LEFT OUTER JOIN ";
        $query .= "     SCHREG_REGD_HDAT T4 ";
        $query .= " ON ";
        $query .= "     T1.YEAR = T4.YEAR AND ";
        $query .= "     T3.SEMESTER = T4.SEMESTER AND ";
        $query .= "     T3.GRADE = T4.GRADE AND  ";
        $query .= "     T3.HR_CLASS = T4.HR_CLASS ";
        $query .= " LEFT OUTER JOIN ";
        $query .= "     TYPE_ASSES_MST T5 ";
        $query .= " ON ";
        $query .= "     T1.YEAR = T5.YEAR AND ";
        $query .= "     T1.JUDGE_PATTERN = T5.TYPE_ASSES_CD  AND ";
        $query .= "     T5.TYPE_ASSES_LEVEL = '2' ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".$a_year."' AND ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "    T1.CLASSCD || '-' || ";
            $query .= "    T1.SCHOOL_KIND || '-' || ";
            $query .= "    T1.CURRICULUM_CD || '-' || ";
        }
        $query .= "     T1.SUBCLASSCD = '".$subcd."' AND ";
        $query .= "     T3.SEMESTER = '".$a_semes."' AND ";
        $query .= "     T3.GRADE = '".$gkcd."' ";
        $query .= " ORDER BY ";
        $query .= "     T4.HR_NAMEABBV, ";
        $query .= "     T3.ATTENDNO, ";
        $query .= "     T1.SCHREGNO ";
        return $query;
    }


    //生徒毎の対象科目数を取得
    function get_sub_get_count($a_year, $schregno, $flg, $model)
    {
        $query  = " SELECT ";
        $query .= "     CASE WHEN get_count(T1.SCHREGNO) = 0 THEN NULL ELSE get_count(T1.SCHREGNO) ";
        $query .= "     END ";
        $query .= " FROM ";
        $query .= "     SUPP_EXA_DAT T1, ";
        $query .= "     V_SUBCLASS_MST T2 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = T2.YEAR AND ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T1.CLASSCD = T2.CLASSCD AND ";
            $query .= "     T1.SCHOOL_KIND = T2.SCHOOL_KIND AND ";
            $query .= "     T1.CURRICULUM_CD = T2.CURRICULUM_CD AND ";
        }
        $query .= "     T1.SUBCLASSCD = T2.SUBCLASSCD AND ";
        $query .= "     T1.YEAR = '".$a_year."' AND ";
        $query .= "     T1.SCHREGNO = '".$schregno."' ";


        if ($flg == 2) {
            $query .= " AND T2.ELECTDIV = '1' ";
        }

#        if ($flg == 1) {
#            $query .= " AND (T2.ELECTDIV = '0' OR T2.ELECTDIV IS NULL) ";
#        } elseif ($flg == 2) {
#            $query .= " AND T2.ELECTDIV = '1' ";
#        }

        return $query;
    }

    //追試験結果の更新
    function getUpdateQuery(&$model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //対象生徒の回数繰り返し
        for($i=0; $i<get_count($model->schregno_cnt); $i++)
        {
            $data = array();

            //入力された内容が空の場合
            if ($model->field["EDIT_TEXT"][$i] == "") {
                $data["SCORE"][NUMBER] = NULL;
                $data["DI_MARK"][TEXT] = NULL;
            //入力された内容が勤怠記号の場合
            } elseif($model->field["EDIT_TEXT"][$i] == "KK" || $model->field["EDIT_TEXT"][$i] == "KS") {
                $data["SCORE"][NUMBER] = NULL;
                $data["DI_MARK"][TEXT] = $model->field["EDIT_TEXT"][$i];
            } else {
                $data["SCORE"][NUMBER] = $model->field["EDIT_TEXT"][$i];        //素点
                $data["DI_MARK"][TEXT] = NULL;                                  //勤怠記号
            }

            $data["REGISTERCD"][TEXT]  = STAFFCD ;         //登録者コード
            $data["UPDATED"][FUNC]     = "sysdate()";      //登録日付

            $where  = " WHERE YEAR           = '".CTRL_YEAR."' AND ";
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $where .= "    CLASSCD || '-' || ";
                $where .= "    SCHOOL_KIND || '-' || ";
                $where .= "    CURRICULUM_CD || '-' || ";
            }
            $where .= "       SUBCLASSCD     = '".$model->sub_cmb."' AND ";
            $where .= "       SCHREGNO       = '".$model->schregno_cnt[$i]."' ";

            $query = Query::updateSQL($data, "SUPP_EXA_DAT",$where);
            $db->query($query);
        }

        $db->commit();
        Query::dbCheckIn($db);
        return true;
    }

    //成績反映
    function getReflectModel(&$model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //追試験の合格者を抽出
        $query  = " SELECT ";
        $query .= "     T1.SCHREGNO, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "    T1.CLASSCD || '-' || ";
            $query .= "    T1.SCHOOL_KIND || '-' || ";
            $query .= "    T1.CURRICULUM_CD || '-' || ";
        }
        $query .= "     T1.SUBCLASSCD AS SUBCLASSCD, ";
        $query .= "     T1.JUDGE_PATTERN, ";
        $query .= "     T2.TYPE_ASSES_LEVEL, ";
        $query .= "     T2.TYPE_ASSES_LOW ";
        $query .= " FROM ";
        $query .= "     SUPP_EXA_DAT T1 ";
        $query .= " LEFT OUTER JOIN ";
        $query .= "     TYPE_ASSES_MST T2 ";
        $query .= " ON ";
        $query .= "     T1.YEAR = T2.YEAR AND ";
        $query .= "     T1.JUDGE_PATTERN = T2.TYPE_ASSES_CD  AND ";
        $query .= "     T2.TYPE_ASSES_LEVEL = '2' ";
        $query .= " INNER JOIN SCHREG_REGD_DAT T3 ";
        $query .= "     ON  T3.SCHREGNO=T1.SCHREGNO ";
        $query .= "     AND T3.YEAR='".$model->cntl_dt_year."' ";
        $query .= "     AND T3.SEMESTER='".$model->cntl_semester."' ";
        $query .= "     AND T3.GRADE='".$model->gk_cmb."' ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".$model->cntl_dt_year."' AND ";
        $query .= "     T1.SCORE >= T2.TYPE_ASSES_LOW ";
        $query .= " ORDER BY ";
        $query .= "     T1.SCHREGNO, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "    T1.CLASSCD || '-' || ";
            $query .= "    T1.SCHOOL_KIND || '-' || ";
            $query .= "    T1.CURRICULUM_CD || '-' || ";
        }
        $query .= "     T1.SUBCLASSCD ";

        $result = $db->query($query);
        while($Row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $data = array();

            //成績データに反映
            //学年成績に各類型評定の評価２の最低点をセット
            $data["GRADE_RECORD"][NUMBER] = $Row["TYPE_ASSES_LOW"];

            //各類型評定へ２をセット
            if ($Row["JUDGE_PATTERN"] == "A") {
                $data["A_PATTERN_ASSESS"][TEXT] = $Row["TYPE_ASSES_LEVEL"];
            } elseif ($Row["JUDGE_PATTERN"] == "B") {
                $data["B_PATTERN_ASSESS"][TEXT] = $Row["TYPE_ASSES_LEVEL"];
            } elseif ($Row["JUDGE_PATTERN"] == "C") {
                $data["C_PATTERN_ASSESS"][TEXT] = $Row["TYPE_ASSES_LEVEL"];
            }

            $data["REGISTERCD"][TEXT]  = STAFFCD ;         //登録者コード
            $data["UPDATED"][FUNC]     = "sysdate()";      //登録日付

            $where  = " WHERE YEAR           = '".$model->cntl_dt_year."' AND ";
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $where .= "    CLASSCD || '-' || ";
                $where .= "    SCHOOL_KIND || '-' || ";
                $where .= "    CURRICULUM_CD || '-' || ";
            }
            $where .= "       SUBCLASSCD     = '".$Row["SUBCLASSCD"]."' AND ";
            $where .= "       SCHREGNO       = '".$Row["SCHREGNO"]."' ";

            $query2 = Query::updateSQL($data, "KIN_RECORD_DAT",$where);
            $db->query($query2);
        }


        //追試験の不合格者を抽出
        $query  = " SELECT ";
        $query .= "     T1.SCHREGNO, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "    T1.CLASSCD || '-' || ";
            $query .= "    T1.SCHOOL_KIND || '-' || ";
            $query .= "    T1.CURRICULUM_CD || '-' || ";
        }
        $query .= "     T1.SUBCLASSCD AS SUBCLASSCD, ";
        $query .= "     T1.GRADE_RECORD, ";
        $query .= "     T1.A_PATTERN_ASSESS, ";
        $query .= "     T1.B_PATTERN_ASSESS, ";
        $query .= "     T1.C_PATTERN_ASSESS, ";
        $query .= "     T1.JUDGE_PATTERN ";
        $query .= " FROM ";
        $query .= "     SUPP_EXA_DAT T1 ";
        $query .= " LEFT OUTER JOIN ";
        $query .= "     TYPE_ASSES_MST T2 ";
        $query .= " ON ";
        $query .= "     T1.YEAR = T2.YEAR AND ";
        $query .= "     T1.JUDGE_PATTERN = T2.TYPE_ASSES_CD  AND ";
        $query .= "     T2.TYPE_ASSES_LEVEL = '2' ";
        $query .= " INNER JOIN SCHREG_REGD_DAT T3 ";
        $query .= "     ON  T3.SCHREGNO=T1.SCHREGNO ";
        $query .= "     AND T3.YEAR='".$model->cntl_dt_year."' ";
        $query .= "     AND T3.SEMESTER='".$model->cntl_semester."' ";
        $query .= "     AND T3.GRADE='".$model->gk_cmb."' ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".$model->cntl_dt_year."' AND ";
        $query .= "    (T1.SCORE < T2.TYPE_ASSES_LOW OR ";
        $query .= "     T1.SCORE IS NULL) ";
        $query .= " ORDER BY ";
        $query .= "     T1.SCHREGNO, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "    T1.CLASSCD || '-' || ";
            $query .= "    T1.SCHOOL_KIND || '-' || ";
            $query .= "    T1.CURRICULUM_CD || '-' || ";
        }
        $query .= "     T1.SUBCLASSCD ";

        $result = $db->query($query);
        while($Row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $data = array();

            //追試験データに退避しておいたデータを成績データにセットし、値を元に戻す
            $data["GRADE_RECORD"][NUMBER]    = $Row["GRADE_RECORD"];          //学年成績
            $data["A_PATTERN_ASSESS"][TEXT]  = $Row["A_PATTERN_ASSESS"];      //類型Ａ評定
            $data["B_PATTERN_ASSESS"][TEXT]  = $Row["B_PATTERN_ASSESS"];      //類型Ｂ評定
            $data["C_PATTERN_ASSESS"][TEXT]  = $Row["C_PATTERN_ASSESS"];      //類型Ｃ評定
            $data["JUDGE_PATTERN"][TEXT]     = $Row["JUDGE_PATTERN"];         //判定類型

            $data["REGISTERCD"][TEXT]  = STAFFCD ;         //登録者コード
            $data["UPDATED"][FUNC]     = "sysdate()";      //登録日付

            $where  = " WHERE YEAR           = '".$model->cntl_dt_year."' AND ";
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $where .= "    CLASSCD || '-' || ";
                $where .= "    SCHOOL_KIND || '-' || ";
                $where .= "    CURRICULUM_CD || '-' || ";
            }
            $where .= "       SUBCLASSCD     = '".$Row["SUBCLASSCD"]."' AND ";
            $where .= "       SCHREGNO       = '".$Row["SCHREGNO"]."' ";

            $query2 = Query::updateSQL($data, "KIN_RECORD_DAT",$where);
            $db->query($query2);
        }


        $db->commit();
        Query::dbCheckIn($db);
        return true;


    }
}
?>
