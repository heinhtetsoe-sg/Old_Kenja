<?php

require_once('for_php7.php');

class knjb0031_2Query extends Query
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

    //科目担任取得
    public function getStaff($term, $chaircd, $chargediv)
    {
        $query  = "SELECT ";
        $query .= "    t1.staffcd, ";
        $query .= "    value(t1.chargediv,0) as chargediv, ";
        $query .= "    t2.staffname_show ";
        $query .= "FROM ";
        $query .= "    chair_stf_dat t1, ";
        $query .= "    v_staff_mst t2 ";
        $query .= "WHERE ";
        $query .= "    t1.year='".SUBSTR($term, 0, 4)."' AND ";
        $query .= "    t1.semester='".SUBSTR($term, 5)."' AND ";
        $query .= "    t1.chaircd='".$chaircd."' AND ";
        $query .= "    value(t1.chargediv,0) = {$chargediv} AND ";
        $query .= "    t1.year=t2.year AND ";
        $query .= "    t1.staffcd=t2.staffcd ";
        $query .= " ORDER BY t1.staffcd ";

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
    //職員マスタの存在チェック
    public function getCntStaff($model, $staffcd)
    {
        $query  = "SELECT COUNT(*) FROM V_STAFF_MST ";
        $query .= " WHERE YEAR     = '".SUBSTR($model->term, 0, 4)."' ";
        $query .= "   AND STAFFCD  = '{$staffcd}' ";

        return $query;
    }
    //--- INSERT（講座職員データ）
    public function insertChairStf($model, $staffcd, $chargediv, $g)
    {
        $data["YEAR"][TEXT]         = substr($model->term, 0, 4);
        $data["SEMESTER"][TEXT]     = substr($model->term, 5);
        $data["CHAIRCD"][TEXT]      = $model->fields["CHAIRCD"][$g];
        $data["STAFFCD"][TEXT]      = $staffcd;
        $data["CHARGEDIV"][NUMBER]  = $chargediv;
        $data["REGISTERCD"][TEXT]   = STAFFCD;
        $data["UPDATED"][NUMBER]    = "SYSDATE()";

        $query = Query::insertSQL($data, "chair_stf_dat");

        return $query;
    }
    //--- DELETE（講座職員データ）
    public function deleteChairStf($model, $g)
    {
        $query  = "DELETE FROM chair_stf_dat ";
        $query .= " WHERE year     = '".SUBSTR($model->term, 0, 4)."'";
        $query .= "   AND semester = '".SUBSTR($model->term, 5)."'";
        $query .= "   AND chaircd  = '".$model->fields["CHAIRCD"][$g]."'";

        return $query;
    }
    //--- DELETE（講座職員データ）
    public function deleteChairStf2($model, $staffcd, $g)
    {
        $query  = "DELETE FROM chair_stf_dat ";
        $query .= " WHERE year     = '".SUBSTR($model->term, 0, 4)."'";
        $query .= "   AND semester = '".SUBSTR($model->term, 5)."'";
        $query .= "   AND chaircd  = '".$model->fields["CHAIRCD"][$g]."'";
        $query .= "   AND staffcd  = '".$staffcd."'";

        return $query;
    }

    //更新
    public function &getUpdateQuery($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        for ($g = 0; $g < $model->data_cnt; $g++) {
            //講座職員データ
            $db->query(knjb0031_2Query::deleteChairStf($model, $g));
            for ($div = 1; $div <= 2; $div++) {
                $soeji = ($div == 1) ? "_S" : "_F";
                $chargediv = ($div == 1) ? "1" : "0";
                $maxNo = 3;
                for ($no = 1; $no <= $maxNo; $no++) {
                    $staffcd = $model->fields["STAFFCD".$soeji.$no][$g];
                    if (strlen($staffcd)) {
                        $db->query(knjb0031_2Query::deleteChairStf2($model, $staffcd, $g));
                        $db->query(knjb0031_2Query::insertChairStf($model, $staffcd, $chargediv, $g));
                    }
                }
            }
        }//for

        $db->commit();
        Query::dbCheckIn($db);

        return $ret_val;
    }
}
