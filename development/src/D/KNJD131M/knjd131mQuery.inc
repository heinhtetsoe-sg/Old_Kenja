<?php

require_once('for_php7.php');

class knjd131mQuery extends Query
{
    //校種取得
    public function getSchoolKind($grade)
    {
        $query  = " SELECT ";
        $query .= "     SCHOOL_KIND ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR    = '".CTRL_YEAR."' AND ";
        $query .= "     GRADE   = '".$grade."' ";

        return $query;
    }

    //学期取得
    public function getSemester()
    {
        $query  = " SELECT ";
        $query .= "     SEMESTER AS VALUE, ";
        $query .= "     SEMESTERNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".CTRL_YEAR."' AND ";
        $query .= "     SEMESTER    < '9' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //データ取得
    public function getHreportremarkDat($model, $schregno, $flg = "")
    {
        $query  = " SELECT ";
        if ($flg == "cnt") {
            $query .= "     COUNT(*) ";
        } else {
            $query .= "     * ";
        }
        $query .= " FROM ";
        $query .= "     HREPORTREMARK_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".CTRL_YEAR."' AND ";
        $query .= "     SEMESTER    = '".$model->semester."' AND ";
        $query .= "     SCHREGNO    = '".$schregno."' ";

        return $query;
    }

    //更新
    public function &getUpdateQuery($model, $fields, $schregno)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);
        //存在チェック
        $dataCnt = $db->getOne(knjd131mQuery::getHreportremarkDat($model, $schregno, "cnt"));

        $data = array();
        if ($model->semester == "1" || $model->semester == "2") {
            //備考
            $data["COMMUNICATION"][TEXT] = $fields["COMMUNICATION"];
        }
        if ($model->semester == "3") {
            //学校行事の記録
            $data["SPECIALACTREMARK"][TEXT] = $fields["SPECIALACTREMARK"];
        }
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][NUMBER]        = "sysdate()";

        if ($dataCnt > 0) {
            $where  = " WHERE ";
            $where .= "     YEAR        = '".CTRL_YEAR."' AND ";
            $where .= "     SEMESTER    = '".$model->semester."' AND ";
            $where .= "     SCHREGNO    = '".$schregno."' ";

            $query = Query::updateSQL($data, "HREPORTREMARK_DAT", $where);
        } else {
            $data["YEAR"][TEXT]                = CTRL_YEAR;
            $data["SEMESTER"][TEXT]            = $model->semester;
            $data["SCHREGNO"][TEXT]            = $schregno;

            $query = Query::insertSQL($data, "HREPORTREMARK_DAT");
        }
        $db->query($query);

        $db->commit();
        Query::dbCheckIn($db);
        return ;
    }
}
