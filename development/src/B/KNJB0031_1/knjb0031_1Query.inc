<?php

require_once('for_php7.php');

class knjb0031_1Query extends Query
{

    /*------------------------左フレーム-----------------------*/

    //対象年度コンボ
    public function getSemesterMst($model)
    {
        $year = substr($model->term, 0, 4);
        $semester = substr($model->term, 5);

        $query  = "";
        $query .= " SELECT ";
        $query .= "     YEAR || '年度　' || SEMESTERNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$year}' ";
        $query .= "     AND SEMESTER = '{$semester}' ";

        return $query;
    }

    //学年・コース取得
    public function getGradeCouse($model)
    {
        $year = substr($model->term, 0, 4);

        $query  = " SELECT ";
        $query .= "     T3.GRADE, ";
        $query .= "     T3.GRADE_NAME1, ";
        $query .= "     T1.COURSECD, ";
        $query .= "     T1.MAJORCD, ";
        $query .= "     T1.COURSENAME, ";
        $query .= "     T1.MAJORNAME, ";
        $query .= "     T2.COURSECODE, ";
        $query .= "     T2.COURSECODENAME ";
        $query .= " FROM ";
        $query .= "     V_COURSE_MAJOR_MST T1, ";
        $query .= "     V_COURSECODE_MST T2, ";
        $query .= "     SCHREG_REGD_GDAT T3 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = T2.YEAR AND ";
        $query .= "     T1.YEAR = T3.YEAR AND ";
        $query .= "     T1.YEAR = '{$year}' AND ";
        $query .= "     T3.GRADE || '-' || T1.COURSECD || '-' || T1.MAJORCD || '-' || T2.COURSECODE = '{$model->grade_course}' ";

        return $query;
    }

    //科目コンボ
    public function getSubclassMst($model)
    {
        $year = substr($model->term, 0, 4);

        $query  = "";
        $query .= " SELECT ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD || '　' || SUBCLASSNAME AS LABEL ";
        } else {
            $query .= "     SUBCLASSCD || '　' || SUBCLASSNAME AS LABEL ";
        }
        $query .= " FROM ";
        $query .= "     V_SUBCLASS_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$year}' ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     AND CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD = '{$model->subclasscd}' ";
        } else {
            $query .= "     AND SUBCLASSCD = '{$model->subclasscd}' ";
        }

        return $query;
    }

    //講座一覧
    public function getChairList($model)
    {
        $year = substr($model->term, 0, 4);
        $semester = substr($model->term, 5);

        $query  = "";
        $query .= " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     CHAIR_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$year}' ";
        $query .= "     AND SEMESTER = '{$semester}' ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     AND CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD = '{$model->subclasscd}' ";
        } else {
            $query .= "     AND SUBCLASSCD = '{$model->subclasscd}' ";
        }
        //群ラジオ 1:教科 2:群
        if ($model->group == '2') {
            $query .= "     AND GROUPCD > '0000' ";
        } else {
            $query .= "     AND GROUPCD = '0000' ";
        }
        $query .= " ORDER BY ";
        $query .= "     CHAIRCD ";

        return $query;
    }

    /*------------------------右フレーム-----------------------*/

    //受講クラス取得
    public function getCls($model, $chaircd, $groupcd)
    {
        list($grade, $coursecd, $majorcd, $coursecode) = explode("-", $model->grade_course);

        $query  = "SELECT ";
        $query .= "    t1.trgtclass, ";
        $query .= "    t2.hr_nameabbv ";
        $query .= "FROM ";
        $query .= "    chair_cls_dat t1, ";
        $query .= "    schreg_regd_hdat t2 ";
        $query .= "WHERE ";
        $query .= "    t1.year='".SUBSTR($model->term, 0, 4)."' AND ";
        $query .= "    t1.semester='".SUBSTR($model->term, 5)."' AND ";
        if ($groupcd > "0000") {
            $query .= "    t1.groupcd='".$groupcd."' AND ";
        } else {
            $query .= "    t1.chaircd='".$chaircd."' AND ";
        }
        $query .= "    t1.trgtgrade = '{$grade}' AND ";
        $query .= "    t1.year=t2.year AND ";
        $query .= "    t1.semester=t2.semester AND ";
        $query .= "    t1.trgtgrade=t2.grade AND ";
        $query .= "    t1.trgtclass=t2.hr_class ";
        $query .= " ORDER BY 1 ";

        return $query;
    }
    //--- CHEACK（講座データ）
    public function countChair($model, $g)
    {
        $query  = "SELECT COUNT(*) FROM chair_dat ";
        $query .= "WHERE year   = '".SUBSTR($model->term, 0, 4)."' ";
        $query .= "AND semester = '".SUBSTR($model->term, 5)."' ";
        $query .= "AND chaircd  = '".$model->fields["CHAIRCD"][$g]."' ";

        return $query;
    }

    //学籍在籍データの件数取得
    public function getRegdDatCnt($term, $grade_course)
    {
        $year = substr($term, 0, 4);
        $semester = substr($term, 5);

        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$year}' ";
        $query .= "     AND SEMESTER = '{$semester}' ";
        $query .= "     AND GRADE || '-' || COURSECD || '-' || MAJORCD || '-' || COURSECODE = '{$grade_course}' ";

        return $query;
    }

    //在籍ヘッダの存在チェック
    public function getCntCls($term, $grade_course, $regdCnt, $trgtclass)
    {
        $year = substr($term, 0, 4);
        $semester = substr($term, 5);

        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$year}' ";
        $query .= "     AND SEMESTER = '{$semester}' ";
        //コースコンボで指定した学籍在籍データがあればそのクラス
        if (0 < $regdCnt) {
            $query .= "     AND GRADE || HR_CLASS IN ( ";
            $query .= "         SELECT ";
            $query .= "             GRADE || HR_CLASS ";
            $query .= "         FROM ";
            $query .= "             SCHREG_REGD_DAT ";
            $query .= "         WHERE ";
            $query .= "             YEAR = '{$year}' ";
            $query .= "             AND SEMESTER = '{$semester}' ";
            $query .= "             AND GRADE || '-' || COURSECD || '-' || MAJORCD || '-' || COURSECODE = '{$grade_course}' ";
            $query .= "         GROUP BY ";
            $query .= "             GRADE || HR_CLASS ";
            $query .= "     ) ";
        } else {
            $grade = substr($grade_course, 0, 2);
            $query .= "     AND GRADE = '{$grade}' ";
        }
        $query .= "     AND HR_CLASS = '{$trgtclass}' ";

        return $query;
    }
    //--- INSERT（講座受講クラスデータ）
    public function insertChairCls($model, $grade, $trgtclass, $g)
    {
        $data["YEAR"][TEXT]         = substr($model->term, 0, 4);
        $data["SEMESTER"][TEXT]     = substr($model->term, 5);
        $data["CHAIRCD"][TEXT]      = $model->fields["CHAIRCD"][$g];
        $data["GROUPCD"][TEXT]      = "0000";
        $data["TRGTGRADE"][TEXT]    = $grade;
        $data["TRGTCLASS"][TEXT]    = $trgtclass;
        $data["REGISTERCD"][TEXT]   = STAFFCD;
        $data["UPDATED"][NUMBER]    = "SYSDATE()";

        $query = Query::insertSQL($data, "chair_cls_dat");

        return $query;
    }
    //--- DELETE（講座受講クラスデータ）
    public function deleteChairCls($model, $g)
    {
        $query  = "DELETE FROM chair_cls_dat ";
        $query .= " WHERE year     = '".SUBSTR($model->term, 0, 4)."'";
        $query .= "   AND semester = '".SUBSTR($model->term, 5)."'";
        $query .= "   AND chaircd  = '".$model->fields["CHAIRCD"][$g]."'";

        return $query;
    }
    //--- DELETE（講座受講クラスデータ）
    public function deleteChairCls2($model, $grade, $trgtclass, $g)
    {
        $query  = "DELETE FROM chair_cls_dat ";
        $query .= " WHERE year     = '".SUBSTR($model->term, 0, 4)."'";
        $query .= "   AND semester = '".SUBSTR($model->term, 5)."'";
        $query .= "   AND chaircd  = '".$model->fields["CHAIRCD"][$g]."'";
        $query .= "   AND trgtgrade = '{$grade}'";
        $query .= "   AND trgtclass = '{$trgtclass}'";

        return $query;
    }

    //更新
    public function &getUpdateQuery($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        list($grade, $coursecd, $majorcd, $coursecode) = explode("-", $model->grade_course);

        for ($g = 0; $g < $model->data_cnt; $g++) {
            //講座受講クラスデータ
            $db->query(knjb0031_1Query::deleteChairCls($model, $g));
            $maxNo = 20;
            for ($no = 1; $no <= $maxNo; $no++) {
                $trgtclass = $model->fields["TRGTCLASS".$no][$g];
                if (strlen($trgtclass)) {
                    $db->query(knjb0031_1Query::deleteChairCls2($model, $grade, $trgtclass, $g));
                    $db->query(knjb0031_1Query::insertChairCls($model, $grade, $trgtclass, $g));
                }
            }
        }//for

        $db->commit();
        Query::dbCheckIn($db);

        return $ret_val;
    }
}
