<?php

require_once('for_php7.php');

class knjp850Query extends Query
{
    public function getNameMst($model, $namecd1)
    {
        $query  = " SELECT ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$model->year}' ";
        $query .= "     AND NAMECD1 = '{$namecd1}' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //V_STAFF_MST取得
    public function getSchregName($model)
    {
        $query  = " SELECT ";
        $query .= "     NAME ";
        $query .= " FROM ";
        $query .= "     SCHREG_BASE_MST";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '{$model->schregno}' ";

        return $query;
    }

    //職員
    public function getStaffMst($model, $staffcd = "")
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     STAF.STAFFCD AS VALUE, ";
        $query .= "     STAF.STAFFCD || ' ' || STAF.STAFFNAME_SHOW AS LABEL ";
        $query .= " FROM ";
        $query .= "     STAFF_MST STAF ";
        if ($staffcd != "") {
            $query .= " WHERE ";
            $query .= "     STAF.STAFFCD        = '{$staffcd}' ";
        }

        return $query;
    }

    public function selectMainQuery($model)
    {
        $query  = " SELECT ";
        $query .= "     CNTCT.SCHREGNO, ";
        $query .= "     CNTCT.CONTACT_SDATE, ";
        $query .= "     CNTCT.SEQ, ";
        $query .= "     CNTCT.CONTACT_DIV, ";
        $query .= "     CNTCT.STAFFCD, ";
        $query .= "     CNTCT.CONTACT_REMARK, ";
        $query .= "     CNTCT.CONTACT_EDATE, ";
        $query .= "     P010.NAME1 AS CONTACT_DIV_NAME, ";
        $query .= "     STF.STAFFNAME_SHOW ";
        $query .= " FROM ";
        $query .= "     COLLECT_CONTACT_HISTORY_DAT CNTCT";
        $query .= "     LEFT JOIN NAME_MST P010 ";
        $query .= "         ON P010.NAMECD1 = 'P010' ";
        $query .= "        AND P010.NAMECD2 = CNTCT.CONTACT_DIV ";
        $query .= "     LEFT JOIN STAFF_MST STF ";
        $query .= "         ON STF.STAFFCD  = CNTCT.STAFFCD ";
        $query .= " WHERE ";
        $query .= "     CNTCT.SCHREGNO = '{$model->schregno}' ";
        //カテゴリ
        if ($model->optContactDiv != "ALL") {
            $query .= "    AND CNTCT.CONTACT_DIV = '{$model->optContactDiv}' ";
        }
        $query .= " ORDER BY ";
        if ($model->optSort == "2") {
            //古い順
            $query .= "     CNTCT.CONTACT_SDATE, ";
            $query .= "     CNTCT.SEQ ";
        } else {
            //カテゴリ順
            if ($model->optSort == "3") {
                $query .= "     CNTCT.CONTACT_DIV, ";
            } elseif ($model->optSort == "4") {
                //記録者順
                $query .= "     CNTCT.STAFFCD, ";
            }
            //最新順
            $query .= "     CNTCT.CONTACT_SDATE DESC, ";
            $query .= "     CNTCT.SEQ DESC";
        }
        return $query;
    }

    //CSV出力
    public function selectCsvMainQuery($model)
    {
        $query .= " WITH CONTACT_DATA AS ( ";
        $query .= "     SELECT ";
        $query .= "         SCHREGNO, ";
        $query .= "         CONTACT_SDATE, ";
        $query .= "         SEQ, ";
        $query .= "         CONTACT_DIV, ";
        $query .= "         STAFFCD, ";
        $query .= "         YEAR, ";
        $query .= "         SEMESTER, ";
        $query .= "         CONTACT_REMARK, ";
        $query .= "         CONTACT_EDATE, ";
        $query .= "         ROW_NUMBER() OVER (PARTITION BY SCHREGNO ORDER BY CONTACT_SDATE DESC, SEQ DESC) AS LASTEST_NO ";
        $query .= "     FROM ";
        $query .= "         COLLECT_CONTACT_HISTORY_DAT ";
        $query .= "     WHERE ";
        $query .= "         SCHREGNO IN ('".implode("','", $model->csvSchregnoList)."') ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     REGD.SCHREGNO, ";
        $query .= "     A023.ABBV1 AS SCHOOL_KIND_NAME, ";
        $query .= "     HDAT.HR_NAME, ";
        $query .= "     REGD.ATTENDNO, ";
        $query .= "     BASE.NAME, ";
        $query .= "     BASE.NAME_KANA, ";
        $query .= "     (CASE WHEN LASTEST_NO = 1 AND BASE.GRD_DIV IN ('2', '3', '6') THEN BASE.GRD_DATE ELSE NULL END) AS GRD_DATE, "; //転退学者の一番最新の履歴レコードのみに転学日を表示
        $query .= "     CNTCT.CONTACT_SDATE, ";
        $query .= "     CNTCT.CONTACT_EDATE, ";
        $query .= "     CNTCT.SEQ, ";
        $query .= "     STF.STAFFCD, ";
        $query .= "     STF.STAFFNAME_SHOW, ";
        $query .= "     CNTCT.CONTACT_DIV, ";
        $query .= "     P010.NAME1 AS CONTACT_DIV_NAME, ";
        $query .= "     CNTCT.CONTACT_REMARK ";
        $query .= " FROM ";
        $query .= "     CONTACT_DATA CNTCT ";
        $query .= "     LEFT JOIN SCHREG_REGD_DAT REGD ";
        $query .= "         ON REGD.SCHREGNO = CNTCT.SCHREGNO ";
        $query .= "         AND REGD.YEAR = CNTCT.YEAR ";
        $query .= "         AND REGD.SEMESTER = CNTCT.SEMESTER ";
        $query .= "     LEFT JOIN SCHREG_BASE_MST BASE ";
        $query .= "         ON BASE.SCHREGNO = REGD.SCHREGNO ";
        $query .= "     LEFT JOIN SCHREG_REGD_GDAT GDAT ";
        $query .= "         ON GDAT.YEAR = REGD.YEAR ";
        $query .= "         AND GDAT.GRADE = REGD.GRADE ";
        $query .= "     LEFT JOIN SCHREG_REGD_HDAT HDAT ";
        $query .= "         ON HDAT.YEAR = REGD.YEAR ";
        $query .= "         AND HDAT.SEMESTER = REGD.SEMESTER ";
        $query .= "         AND HDAT.GRADE = REGD.GRADE ";
        $query .= "         AND HDAT.HR_CLASS = REGD.HR_CLASS ";
        $query .= "     LEFT JOIN NAME_MST A023 ";
        $query .= "         ON A023.NAMECD1 = 'A023' ";
        $query .= "         AND A023.NAME1 = GDAT.SCHOOL_KIND ";
        $query .= "     LEFT JOIN STAFF_MST STF ";
        $query .= "         ON STF.STAFFCD = CNTCT.STAFFCD ";
        $query .= "     LEFT JOIN NAME_MST P010 ";
        $query .= "         ON P010.NAMECD1 = 'P010' ";
        $query .= "         AND P010.NAMECD2 = CNTCT.CONTACT_DIV ";
        $query .= " ORDER BY ";
        $query .= "     REGD.SCHREGNO, ";
        $query .= "     CNTCT.LASTEST_NO ";

        return $query;
    }

    //一行取得
    public function getRowQuery($model)
    {
        $query  = " SELECT ";
        $query .= "     CNTCT.SCHREGNO, ";
        $query .= "     CNTCT.CONTACT_SDATE, ";
        $query .= "     CNTCT.SEQ, ";
        $query .= "     CNTCT.CONTACT_DIV, ";
        $query .= "     CNTCT.STAFFCD, ";
        $query .= "     CNTCT.CONTACT_REMARK, ";
        $query .= "     CNTCT.CONTACT_EDATE ";
        $query .= " FROM ";
        $query .= "     COLLECT_CONTACT_HISTORY_DAT CNTCT";
        $query .= " WHERE ";
        $query .= "         CNTCT.SCHREGNO      = '{$model->schregno}' ";
        $query .= "     AND CNTCT.CONTACT_SDATE = '{$model->sdate}' ";
        $query .= "     AND CNTCT.SEQ           = '{$model->seq}' ";

        return $query;
    }

    //存在チェック
    public function getExistsCnt($db, $model)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) AS CNT";
        $query .= " FROM ";
        $query .= "     COLLECT_CONTACT_HISTORY_DAT CNTCT";
        $query .= " WHERE ";
        $query .= "         CNTCT.SCHREGNO      = '{$model->schregno}' ";
        $query .= "     AND CNTCT.CONTACT_SDATE = '{$model->field["CONTACT_SDATE"]}' ";
        $query .= "     AND CNTCT.SEQ           = '{$model->field["SEQ"]}' ";

        $cnt = $db->getOne($query);
        return $cnt;
    }

    //MAXSEQ取得
    public function getMaxSeq($db, $model)
    {
        $query  = " SELECT ";
        $query .= "     MAX(SEQ) AS MAXSEQ";
        $query .= " FROM ";
        $query .= "     COLLECT_CONTACT_HISTORY_DAT CNTCT";
        $query .= " WHERE ";
        $query .= "         CNTCT.SCHREGNO      = '{$model->schregno}' ";
        $query .= "     AND CNTCT.CONTACT_SDATE = '{$model->field["CONTACT_SDATE"]}' ";

        $maxSeq = $db->getOne($query);
        return $maxSeq;
    }

    //追加
    public function getInsertQuery($db, $model)
    {
        //※YEAR, SEMESTERは追加時のみ登録
        $data = array();
        $data["SCHREGNO"][TEXT]           = $model->schregno;
        $data["CONTACT_SDATE"][DATE]      = $model->field["CONTACT_SDATE"];
        $data["SEQ"][TEXT]                = $model->field["SEQ"];
        $data["CONTACT_DIV"][TEXT]        = $model->field["CONTACT_DIV"];
        $data["STAFFCD"][TEXT]            = STAFFCD; //追加の場合は、入力欄の指定にかかわらずログインユーザで登録
        $data["YEAR"][TEXT]               = $model->year;
        $data["SEMESTER"][TEXT]           = $model->semester;
        $data["CONTACT_REMARK"][TEXT]     = $model->field["CONTACT_REMARK"];
        $data["CONTACT_EDATE"][DATE]      = $model->field["CONTACT_EDATE"];
        $data["REGISTERCD"][TEXT]         = STAFFCD;
        $data["UPDATED"][NUMBER]          = "sysdate()";

        $query = Query::insertSQL($data, "COLLECT_CONTACT_HISTORY_DAT");
        $db->query($query);
    }

    //更新
    public function getUpdateQuery($db, $model)
    {
        //※YEAR, SEMESTERは更新時には変更しない
        $data = array();
        $data["CONTACT_DIV"][TEXT]        = $model->field["CONTACT_DIV"];
        $data["STAFFCD"][TEXT]            = $model->field["STAFFCD"];
        $data["CONTACT_REMARK"][TEXT]     = $model->field["CONTACT_REMARK"];
        $data["CONTACT_EDATE"][DATE]      = $model->field["CONTACT_EDATE"];
        $data["REGISTERCD"][TEXT]         = STAFFCD;
        $data["UPDATED"][NUMBER]          = "sysdate()";

        $where .= " WHERE ";
        $where .= "         SCHREGNO      = '{$model->schregno}' ";
        $where .= "     AND CONTACT_SDATE = '{$model->sdate}' ";
        $where .= "     AND SEQ           = '{$model->seq}' ";

        $query = Query::updateSQL($data, "COLLECT_CONTACT_HISTORY_DAT", $where);
        $db->query($query);
    }

    //削除
    public function getDeleteQuery($db, $model)
    {
        $query  = " DELETE FROM ";
        $query .= "     COLLECT_CONTACT_HISTORY_DAT ";
        $query .= " WHERE ";
        $query .= "         SCHREGNO      = '{$model->schregno}' ";
        $query .= "     AND CONTACT_SDATE = '{$model->sdate}' ";
        $query .= "     AND SEQ           = '{$model->seq}' ";

        $db->query($query);
    }
}
