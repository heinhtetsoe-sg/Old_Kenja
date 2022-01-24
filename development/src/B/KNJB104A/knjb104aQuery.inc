<?php

require_once('for_php7.php');

class knjb104aQuery extends Query
{

    //年度
    public function getYear($model)
    {
        $query .= " SELECT DISTINCT ";
        $query .= "     YEAR AS VALUE, ";
        $query .= "     YEAR AS LABEL ";
        $query .= " FROM ";
        $query .= "     CHAIR_TEST_FAC_DAT ";

        return $query;
    }

    //学期
    public function getSemester($model, $year)
    {
        $query  = " SELECT ";
        $query .= "     SEMESTER AS VALUE, ";
        $query .= "     SEMESTERNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$year}' ";
        $query .= "     AND SEMESTER <> '9' ";
        $query .= " ORDER BY ";
        $query .= "     SEMESTER ";

        return $query;
    }

    //登録データ
    public function selectQuery($model)
    {
        $query .= " SELECT ";
        $query .= "     T1.FACCD, ";
        $query .= "     T2.FACILITYNAME, ";
        $query .= "     T3.CHAIRNAME, ";
        $query .= "     T1.CHAIRCD, ";
        $query .= "     CASE WHEN T4.YEAR IS NOT NULL THEN '1' ELSE '0' END AS FLAG ";
        $query .= " FROM ";
        $query .= "     CHAIR_TEST_FAC_DAT T1 ";
        $query .= " LEFT JOIN V_FACILITY_MST T2 ";
        $query .= "     ON T2.YEAR = T1.YEAR ";
        $query .= "     AND T2.FACCD = T1.FACCD ";
        $query .= " LEFT JOIN CHAIR_DAT T3 ";
        $query .= "     ON T3.YEAR = T1.YEAR ";
        $query .= "     AND T3.SEMESTER = T1.SEMESTER ";
        $query .= "     AND T3.CHAIRCD = T1.CHAIRCD ";
        $query .= " LEFT JOIN CHAIR_TEST_FAC_LAYOUT_HDAT T4 ";
        $query .= "     ON T4.YEAR = T1.YEAR ";
        $query .= "     AND T4.SEMESTER = T1.SEMESTER ";
        $query .= "     AND T4.CHAIRCD = T1.CHAIRCD ";
        $query .= "     AND T4.FACCD = T1.FACCD ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '{$model->year}' ";
        $query .= "     AND T1.SEMESTER = '{$model->semester}' ";
        $query .= " ORDER BY ";
        $query .= "     T1.FACCD, ";
        $query .= "     T1.CHAIRCD ";

        return $query;
    }

    //登録データ(教室ヘッダ)
    public function selectQuery2($model)
    {
        $query .= " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     CHAIR_TEST_FAC_LAYOUT_HDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$model->exp_year}' ";
        $query .= "     AND SEMESTER = '{$model->exp_semester}' ";
        $query .= "     AND CHAIRCD = '{$model->exp_chaircd}' ";
        $query .= "     AND FACCD = '{$model->exp_faccd}' ";

        return $query;
    }
    //登録データ(教室データ)
    public function selectQuery3($model)
    {
        $query .= " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     CHAIR_TEST_FAC_LAYOUT_NOTUSE_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$model->exp_year}' ";
        $query .= "     AND SEMESTER = '{$model->exp_semester}' ";
        $query .= "     AND CHAIRCD = '{$model->exp_chaircd}' ";
        $query .= "     AND FACCD = '{$model->exp_faccd}' ";

        return $query;
    }

    //更新
    public function &updateQuery($model)
    {
        $db = Query::dbCheckOut();

        $query  = " DELETE FROM ";
        $query .= "     CHAIR_TEST_FAC_LAYOUT_HDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR < '".(intval($model->exp_year)-5)."' ";
        $db->query($query);

        $where  = " WHERE ";
        $where .= "     YEAR = '{$model->exp_year}' ";
        $where .= "     AND SEMESTER = '{$model->exp_semester}' ";
        $where .= "     AND CHAIRCD = '{$model->exp_chaircd}' ";
        $where .= "     AND FACCD = '{$model->exp_faccd}' ";

        $data = array();
        $data['ROWS'][TEXT]       = $model->gyou;
        $data['COLUMNS'][TEXT]    = $model->retu;
        $data["REGISTERCD"][TEXT] = STAFFCD;
        $data["UPDATED"][FUNC]    = "sysdate()";
        if ($db->getOne("SELECT COUNT(*) FROM CHAIR_TEST_FAC_LAYOUT_HDAT {$where}") > 0) {
            $db->query(Query::updateSQL($data, "CHAIR_TEST_FAC_LAYOUT_HDAT", $where));
        } else {
            $data['YEAR'][TEXT]     = $model->exp_year;
            $data['SEMESTER'][TEXT] = $model->exp_semester;
            $data['CHAIRCD'][TEXT]  = $model->exp_chaircd;
            $data['FACCD'][TEXT]    = $model->exp_faccd;
            $db->query(Query::insertSQL($data, "CHAIR_TEST_FAC_LAYOUT_HDAT"));
        }

        $query  = " DELETE FROM ";
        $query .= "     CHAIR_TEST_FAC_LAYOUT_NOTUSE_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR < '".(intval($model->exp_year)-5)."' ";
        $db->query($query);

        $query  = " DELETE FROM ";
        $query .= "     CHAIR_TEST_FAC_LAYOUT_NOTUSE_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$model->exp_year}' ";
        $query .= "     AND SEMESTER = '{$model->exp_semester}' ";
        $query .= "     AND CHAIRCD = '{$model->exp_chaircd}' ";
        $query .= "     AND FACCD = '{$model->exp_faccd}' ";
        $db->query($query);

        for ($i = 0; $i < get_count($model->notuse); $i++) {
            list($gyou, $retu) = explode('*', $model->notuse[$i]);
            $data = array();
            $data['ROWS'][TEXT]       = $gyou;
            $data['COLUMNS'][TEXT]    = $retu;
            $data['YEAR'][TEXT]     = $model->exp_year;
            $data['SEMESTER'][TEXT] = $model->exp_semester;
            $data['CHAIRCD'][TEXT]  = $model->exp_chaircd;
            $data['FACCD'][TEXT]    = $model->exp_faccd;
            $data["REGISTERCD"][TEXT] = STAFFCD;
            $data["UPDATED"][FUNC]    = "sysdate()";

            $db->query(Query::insertSQL($data, "CHAIR_TEST_FAC_LAYOUT_NOTUSE_DAT"));
        }
    }

    //コピー先のデータの件数
    public function hdatCount($model, $year, $semester)
    {
        $query .= " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     CHAIR_TEST_FAC_LAYOUT_HDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$year}' ";
        $query .= "     AND SEMESTER = '{$semester}' ";

        return $query;
    }
    //コピー
    public function copyQuery($model, $year, $year2, $semester, $semester2)
    {
        $db = Query::dbCheckOut();

        $query  ="INSERT INTO CHAIR_TEST_FAC_LAYOUT_HDAT";
        $query .="       (YEAR,SEMESTER,CHAIRCD,FACCD,ROWS,COLUMNS,REGISTERCD,UPDATED) ";
        $query .= " SELECT ";
        $query .="      '{$year}',";
        $query .="      '{$semester}',";
        $query .= "     CHAIRCD, ";
        $query .= "     FACCD, ";
        $query .= "     ROWS, ";
        $query .= "     COLUMNS, ";
        $query .="      REGISTERCD,";
        $query .="      SYSDATE()";
        $query .= " FROM ";
        $query .= "     CHAIR_TEST_FAC_LAYOUT_HDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$year2}' ";
        $query .= "     AND SEMESTER = '{$semester2}' ";

        $db->query($query);

        $query  ="INSERT INTO CHAIR_TEST_FAC_LAYOUT_NOTUSE_DAT";
        $query .="       (YEAR,SEMESTER,CHAIRCD,FACCD,ROWS,COLUMNS,REGISTERCD,UPDATED) ";
        $query .= " SELECT ";
        $query .="      '{$year}',";
        $query .="      '{$semester}',";
        $query .= "     CHAIRCD, ";
        $query .= "     FACCD, ";
        $query .= "     ROWS, ";
        $query .= "     COLUMNS, ";
        $query .="      REGISTERCD,";
        $query .="      SYSDATE()";
        $query .= " FROM ";
        $query .= "     CHAIR_TEST_FAC_LAYOUT_NOTUSE_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$year2}' ";
        $query .= "     AND SEMESTER = '{$semester2}' ";

        $db->query($query);

        Query::dbCheckIn($db);
    }
}
