<?php

require_once('for_php7.php');

class knja125kQuery extends Query
{
    //データ取得
    public function getHtrainremarkKDat($model)
    {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     HTRAINREMARK_K_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".$model->exp_year."' AND ";
        $query .= "     SCHREGNO    = '".$model->schregno."' ";

        return $query;
    }

    //自立活動に重点を置いた指導
    public function getHtrainremarkDetail2Dat($model)
    {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     HTRAINREMARK_DETAIL2_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR    = '".$model->exp_year."' ";
        $query .= "     AND SCHREGNO    = '".$model->schregno."' ";
        $query .= "     AND HTRAIN_SEQ = '001' ";

        return $query;
    }

    //入学時の障害の状態取得
    public function getHtrainremarkDetail2HDat($model)
    {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     HTRAINREMARK_DETAIL2_HDAT ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO    = '".$model->schregno."' ";
        $query .= "     AND HTRAIN_SEQ = '001' ";

        return $query;
    }

    //ねらい一覧取得
    public function getViewPoint($model, $flg, $point_l_cd = "")
    {
        $query  = " SELECT DISTINCT ";
        if ($flg == "cnt") {
            $query .= "     COUNT(*) ";
        } else {
            $query .= "     T1.POINT_L_CD, ";
            $query .= "     T1.REMARK_L, ";
            $query .= "     T2.POINT_M_CD, ";
            $query .= "     T2.REMARK_M ";
        }
        $query .= " FROM ";
        $query .= "     JVIEW_POINT_L_MST T1, ";
        $query .= "     JVIEW_POINT_M_MST T2 ";
        $query .= " WHERE ";
        $query .= "     T1.POINT_DIV    = T2.POINT_DIV AND ";
        $query .= "     T1.POINT_DIV    = '01' AND ";
        $query .= "     T1.POINT_L_CD   = T2.POINT_L_CD ";
        if ($flg == "cnt") {
            $query .= " AND T1.POINT_L_CD = '".$point_l_cd."' ";
        } else {
            $query .= " ORDER BY ";
            $query .= "     T1.POINT_L_CD, ";
            $query .= "     T2.POINT_M_CD ";
        }

        return $query;
    }

    //学校種別取得
    public function getSchoolKind($model)
    {
        $query  = " SELECT ";
        $query .= "     T2.SCHOOL_KIND ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1, ";
        $query .= "     SCHREG_REGD_GDAT T2 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR     = T2.YEAR AND ";
        $query .= "     T1.YEAR     = '".$model->exp_year."' AND ";
        $query .= "     T1.SEMESTER = '".$model->exp_semester."' AND ";
        $query .= "     T1.GRADE    = T2.GRADE AND ";
        $query .= "     T1.SCHREGNO = '".$model->schregno."' ";

        return $query;
    }

    //テーブル存在チェック
    public function checkTableExist()
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     SYSIBM.SYSTABLES ";
        $query .= " WHERE ";
        $query .= "     NAME = 'SETTING_DAT' ";

        return $query;
    }

    //生徒項目名取得
    public function getSchName($model, $schoolkind)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     REMARK1, ";
        $query .= "     SCHOOLCD ";
        $query .= " FROM ";
        $query .= "     SETTING_DAT ";
        $query .= " WHERE ";
        $query .= "     SEQ = '001' ";
        if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND SCHOOLCD    = '".sprintf("%012d", SCHOOLCD)."' ";
            $query .= " AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        } else {
            $query .= " AND SCHOOL_KIND = '".$schoolkind."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     SCHOOLCD ";

        return $query;
    }

    //更新処理 -- HTRAINREMARK_K_DAT
    public function &getUpdateQuery($model)
    {
        $db = Query::dbCheckOut();

        //存在チェック
        $check = $db->getCol(knja125kQuery::getHtrainremarkKDat($model));

        $data = array();
        $data["TOTALSTUDYACT"][TEXT]        = $model->field["TOTALSTUDYACT"];
        $data["TOTALSTUDYVAL"][TEXT]        = $model->field["TOTALSTUDYVAL"];
        $data["TOTALREMARK"][TEXT]          = $model->field["TOTALREMARK"];
        $data["ATTENDREC_REMARK"][TEXT]     = $model->field["ATTENDREC_REMARK"];
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][FUNC]              = "sysdate()";
        if (get_count($check) == 0) {
            $data["YEAR"][TEXT]                 = $model->exp_year;
            $data["SCHREGNO"][TEXT]             = $model->schregno;

            $db->query(Query::insertSQL($data, "HTRAINREMARK_K_DAT"));
        } else {
            $where  = " WHERE ";
            $where .= "     YEAR     = '".$model->exp_year."' AND ";
            $where .= "     SCHREGNO = '".$model->schregno."' ";

            $db->query(Query::updateSQL($data, "HTRAINREMARK_K_DAT", $where));
        }

        if ($model->Properties["useSpecial_Support_School"] == "1") {
            // 自立活動に重点を置いた指導
            $check = $db->getCol(knja125kQuery::getHtrainremarkDetail2Dat($model));

            $data = array();
            $data["REMARK1"][TEXT]              = $model->field["INDEPENDENCE_REMARK"];
            $data["REGISTERCD"][TEXT]           = STAFFCD;
            $data["UPDATED"][FUNC]              = "sysdate()";
            if (get_count($check) == 0) {
                $data["YEAR"][TEXT]                 = $model->exp_year;
                $data["SCHREGNO"][TEXT]             = $model->schregno;
                $data["HTRAIN_SEQ"][TEXT]           = "001";

                $query = Query::insertSQL($data, "HTRAINREMARK_DETAIL2_DAT");
                $db->query($query);
            } else {
                $where  = " WHERE ";
                $where .= "     YEAR     = '".$model->exp_year."' AND ";
                $where .= "     SCHREGNO = '".$model->schregno."' AND ";
                $where .= "     HTRAIN_SEQ = '001' ";

                $query = Query::updateSQL($data, "HTRAINREMARK_DETAIL2_DAT", $where);
                $db->query($query);
            }

            // 入学時の障害の状態等
            $check = $db->getCol(knja125kQuery::getHtrainremarkDetail2HDat($model));
            $data = array();
            $data["REMARK1"][TEXT]              = $model->field["FIELD1"];
            $data["REGISTERCD"][TEXT]           = STAFFCD;
            $data["UPDATED"][FUNC]              = "sysdate()";
            if (get_count($check) == 0) {
                $data["SCHREGNO"][TEXT]             = $model->schregno;
                $data["HTRAIN_SEQ"][TEXT]           = "001";

                $query = Query::insertSQL($data, "HTRAINREMARK_DETAIL2_HDAT");
                $db->query($query);
            } else {
                $where  = " WHERE ";
                $where .= "     SCHREGNO = '".$model->schregno."' AND ";
                $where .= "     HTRAIN_SEQ = '001' ";

                $query = Query::updateSQL($data, "HTRAINREMARK_DETAIL2_HDAT", $where);
                $db->query($query);
            }
        }

        Query::dbCheckIn($db);
        return ;
    }

    //データ取得
    public function getZ010()
    {
        $query  = " SELECT ";
        $query .= "     NAME1 ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'Z010' ";
        $query .= "     AND NAMECD2 = '00' ";

        return $query;
    }

    //既入力内容の参照
    public function selectKinyuuyroku($model, $grade)
    {
        $query .= " SELECT DISTINCT ";
        $query .= "     T2.* ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= " LEFT JOIN ";
        $query .= "     HTRAINREMARK_K_DAT T2 ";
        $query .= "     ON T1.YEAR = T2.YEAR ";
        $query .= "     AND T1.SCHREGNO = T2.SCHREGNO ";
        $query .= " WHERE ";
        $query .= "     T1.SCHREGNO = '{$model->schregno}' ";
        $query .= "     AND T1.GRADE = '{$grade}' ";

        return $query;
    }
    //既入力内容の参照2
    public function selectKinyuuyroku2($model, $grade)
    {
        $query .= " SELECT DISTINCT ";
        $query .= "     T2.* ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= " LEFT JOIN ";
        $query .= "     HTRAINREMARK_DETAIL2_DAT T2 ";
        $query .= "     ON T1.YEAR = T2.YEAR ";
        $query .= "     AND T1.SCHREGNO = T2.SCHREGNO ";
        $query .= "     AND T2.HTRAIN_SEQ = '001' ";
        $query .= " WHERE ";
        $query .= "     T1.SCHREGNO = '{$model->schregno}' ";
        $query .= "     AND T1.GRADE = '{$grade}' ";

        return $query;
    }
    //年度の取得
    public function selectGrade($model)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     T1.YEAR, ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.GRADE_NAME1 ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT T1 ";
        $query .= " WHERE ";
        $query .= "     T1.SCHOOL_KIND = 'K' ";
        $query .= "     AND YEAR = '{$model->exp_year}' ";
        $query .= " ORDER BY ";
        $query .= "     T1.GRADE ";
        $query .= " FETCH FIRST 2 ROWS ONLY ";

        return $query;
    }
}
