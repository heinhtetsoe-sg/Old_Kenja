<?php

require_once('for_php7.php');

class knjb0031_3Query extends Query
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

    //使用施設取得
    public function getFac($term, $chaircd)
    {
        $query  = "SELECT ";
        $query .= "    t1.faccd, ";
        $query .= "    t2.facilityabbv ";
        $query .= "FROM ";
        $query .= "    chair_fac_dat t1, ";
        $query .= "    v_facility_mst t2 ";
        $query .= "WHERE ";
        $query .= "    t1.year='".SUBSTR($term, 0, 4)."' AND ";
        $query .= "    t1.semester='".SUBSTR($term, 5)."' AND ";
        $query .= "    t1.chaircd='".$chaircd."' AND ";
        $query .= "    t1.year=t2.year AND ";
        $query .= "    t1.faccd=t2.faccd ";
        $query .= " ORDER BY t1.faccd";

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
    //施設マスタの存在チェック
    public function getCntFac($model, $faccd)
    {
        $query  = "SELECT COUNT(*) FROM V_FACILITY_MST ";
        $query .= " WHERE YEAR     = '".SUBSTR($model->term, 0, 4)."' ";
        $query .= "   AND FACCD    = '{$faccd}' ";

        return $query;
    }
    //--- INSERT（講座使用施設データ）
    public function insertChairFac($model, $faccd, $g)
    {
        $data["YEAR"][TEXT]         = substr($model->term, 0, 4);
        $data["SEMESTER"][TEXT]     = substr($model->term, 5);
        $data["CHAIRCD"][TEXT]      = $model->fields["CHAIRCD"][$g];
        $data["FACCD"][TEXT]        = $faccd;
        $data["REGISTERCD"][TEXT]   = STAFFCD;
        $data["UPDATED"][NUMBER]    = "SYSDATE()";

        $query = Query::insertSQL($data, "chair_fac_dat");

        return $query;
    }
    //--- DELETE（講座使用施設データ）
    public function deleteChairFac($model, $g)
    {
        $query  = "DELETE FROM chair_fac_dat ";
        $query .= " WHERE year     = '".SUBSTR($model->term, 0, 4)."'";
        $query .= "   AND semester = '".SUBSTR($model->term, 5)."'";
        $query .= "   AND chaircd  = '".$model->fields["CHAIRCD"][$g]."'";

        return $query;
    }
    //--- DELETE（講座使用施設データ）
    public function deleteChairFac2($model, $faccd, $g)
    {
        $query  = "DELETE FROM chair_fac_dat ";
        $query .= " WHERE year     = '".SUBSTR($model->term, 0, 4)."'";
        $query .= "   AND semester = '".SUBSTR($model->term, 5)."'";
        $query .= "   AND chaircd  = '".$model->fields["CHAIRCD"][$g]."'";
        $query .= "   AND faccd    = '".$faccd."'";

        return $query;
    }

    //更新
    public function &getUpdateQuery($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        for ($g = 0; $g < $model->data_cnt; $g++) {
            //講座使用施設データ
            $db->query(knjb0031_3Query::deleteChairFac($model, $g));
            $maxNo = 3;
            for ($no = 1; $no <= $maxNo; $no++) {
                $faccd = $model->fields["FACCD".$no][$g];
                if (strlen($faccd)) {
                    $db->query(knjb0031_3Query::deleteChairFac2($model, $faccd, $g));
                    $db->query(knjb0031_3Query::insertChairFac($model, $faccd, $g));
                }
            }
        }//for

        $db->commit();
        Query::dbCheckIn($db);

        return $ret_val;
    }
}
