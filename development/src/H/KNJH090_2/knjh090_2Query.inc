<?php

require_once('for_php7.php');

class knjh090_2Query extends Query
{
    //年組取得
    public function getHrClass($model)
    {
        $query  = " SELECT ";
        $query .= "     GRADE || HR_CLASS AS VALUE, ";
        $query .= "     HR_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT T1 ";
        $query .= " WHERE ";
        $query .= "     YEAR     = '".CTRL_YEAR."' AND ";
        $query .= "     SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //生徒取得
    public function getStudent($model, $flg)
    {
        $selectdata = implode("','", explode(',', $model->field["selectdata"]));

        $query  = " SELECT ";
        $query .= "     T2.SCHREGNO AS VALUE, ";
        $query .= "     T2.ATTENDNO || '番 ' || T2.SCHREGNO || ' ' || T1.NAME_SHOW AS LABEL, ";
        $query .= "     T2.ATTENDNO ";
        $query .= " FROM ";
        $query .= "     SCHREG_BASE_MST T1, ";
        $query .= "     SCHREG_REGD_DAT T2 ";
        $query .= " WHERE ";
        $query .= "     T2.YEAR                 = '".CTRL_YEAR."' AND ";
        $query .= "     T2.SEMESTER             = '".CTRL_SEMESTER."' AND ";
        $query .= "     T2.GRADE || T2.HR_CLASS = '".$model->field["GRADE_HR_CLASS"]."' AND ";
        $query .= "     T2.SCHREGNO             = T1.SCHREGNO ";
        if ($flg == "2") {
            $query .= "     AND T2.SCHREGNO IN ('".$selectdata."') ";
        } else {
            $query .= "     AND T2.SCHREGNO NOT IN ('".$selectdata."') ";
        }
        $query .= " ORDER BY ";
        $query .= "     T2.ATTENDNO ";

        return $query;
    }

    //名称マスタ取得
    public function getNameMst($namecd1)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     T1.NAMECD2 AS VALUE, ";
        $query .= "     T1.NAMECD2 || '　' || T1.NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST T1 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     T1.NAMECD1 = '".$namecd1."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //追加処理
    public function &insertQuery($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $cnt = 0;
        $selectdata = explode(',', $model->field["selectdata"]);

        if ($model->field["selectdata"]) {
            foreach ($selectdata as $schregno) {

                //存在チェック
                $query = knjh090_2Query::getSchregDetailhistDatCntSql($model, $schregno);
                $dataCnt = $db->getOne($query);

                //データセット
                $data = array();
                $data["DETAILCD"][TEXT]       = $model->field["DETAILCD"];
                $data["CONTENT"][TEXT]        = $model->field["CONTENT"];
                $data["REMARK"][TEXT]         = $model->field["REMARK"];
                $data["REGISTERCD"][TEXT]       = STAFFCD;
                $data["UPDATED"][FUNC]          = "SYSDATE()";

                if ($dataCnt > 0) {
                    $where  = " WHERE YEAR          = '".CTRL_YEAR."' ";
                    $where .= "   AND SCHREGNO      = '".$schregno."' ";
                    $where .= "   AND DETAIL_DIV    = '".$model->field["DETAIL_DIV"]."' ";
                    $where .= "   AND DETAIL_SDATE  = '".strtr($model->field["DETAIL_SDATE"], "/", "-")."' ";

                    //更新
                    $query = Query::updateSQL($data, "SCHREG_DETAILHIST_DAT", $where);
                } else {
                    $data["YEAR"][TEXT]           = CTRL_YEAR;
                    $data["SCHREGNO"][TEXT]       = $schregno;
                    $data["DETAIL_DIV"][TEXT]     = $model->field["DETAIL_DIV"];
                    $data["DETAIL_SDATE"][TEXT]   = strtr($model->field["DETAIL_SDATE"], "/", "-");

                    //追加
                    $query = Query::insertSQL($data, "SCHREG_DETAILHIST_DAT");
                }

                $db->query($query);
                $cnt++;
            }
        }

        $db->commit();      // トランザクションをコミットする。
        Query::dbCheckIn($db);
        return $cnt;
    }

    public function getSchregDetailhistDatCntSql($model, $schregno)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     SCHREG_DETAILHIST_DAT ";
        $query .= " WHERE YEAR          = '".CTRL_YEAR."' ";
        $query .= "   AND SCHREGNO      = '".$schregno."' ";
        $query .= "   AND DETAIL_DIV    = '".$model->field["DETAIL_DIV"]."' ";
        $query .= "   AND DETAIL_SDATE  = '".strtr($model->field["DETAIL_SDATE"], "/", "-")."' ";

        return $query;
    }
}
