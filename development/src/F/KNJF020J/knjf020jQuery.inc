<?php

require_once('for_php7.php');

class knjf020jQuery extends Query
{
    //生徒健康診断ヘッダデータ取得
    public function getMedexamHdat($model)
    {
        $db = Query::dbCheckOut();

        $query  = "SELECT *  ";
        $query .= "  FROM MEDEXAM_HDAT ";
        $query .= " WHERE SCHREGNO = '".$model->schregno."' ";
        $query .= "   AND YEAR     = '".$model->year."'";

        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);

        Query::dbCheckIn($db);
        return $row;
    }

    //MEDEXAM_HDATにレコードがあるかチェック
    public function checkMedexamHdat($year, $schregno)
    {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     MEDEXAM_HDAT ";
        $query .= " WHERE ";
        $query .= "        YEAR     = '{$year}' ";
        $query .= "    AND SCHREGNO = '{$schregno}' ";

        return $query;
    }

    //生徒健康診断歯口腔データ取得
    public function getMedexamToothDat($model)
    {
        $db = Query::dbCheckOut();

        $query  = "SELECT ";
        $query .= "    T1.* ";
        $query .= "FROM ";
        $query .= "    V_MEDEXAM_TOOTH_DAT T1 ";
        $query .= "WHERE  T1.SCHREGNO            = '".$model->schregno."' ";
        $query .= "   AND T1.YEAR                = '".$model->year."' ";

        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);
        return $row;
    }

    //生徒名前取得（学籍基礎マスタ）
    public function getSchregBaseMstData($model)
    {
        $query  = " SELECT ";
        $query .= "     BIRTHDAY, ";
        $query .= "     FISCALYEAR(ENT_DATE) AS ENT_YEAR ";
        $query .= " FROM ";
        $query .= "     SCHREG_BASE_MST ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '" .$model->schregno."' ";
        return $query;
    }

    //生徒学年クラス取得
    public function getSchregRegdDatData($model)
    {
        $query  = " SELECT t1.GRADE, ";
        $query .= "        t1.HR_CLASS, ";
        $query .= "        t1.ATTENDNO, ";
        $query .= "        t2.HR_NAME ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT t1 ";
        $query .= "     LEFT JOIN SCHREG_REGD_HDAT t2 ON t2.YEAR = t1.YEAR ";
        $query .= "     AND t2.SEMESTER = t1.SEMESTER ";
        $query .= "     AND t2.GRADE    = t1.GRADE ";
        $query .= "     AND t2.HR_CLASS = t1.HR_CLASS ";
        $query .= " WHERE   t1.SCHREGNO = '" .$model->schregno."' ";
        $query .= "     AND t1.YEAR     = '".$model->year."' ";
        $query .= "     AND t1.SEMESTER = '".$model->semester."' ";

        return $query;
    }

    //生徒健康診断歯口腔データ取得
    public function getToothData($model)
    {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     MEDEXAM_TOOTH_DAT ";
        $query .= " WHERE ";
        $query .= "        SCHREGNO = '".$model->schregno."' ";
        $query .= "    AND YEAR     = '".$model->year."' ";

        return $query;
    }

    //名称マスタ
    public function getNameMst($model, $namecd1)
    {
        $query  = " SELECT ";
        $query .= "     ABBV1 AS SHOW, ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     NVL(NAMESPARE2, 0) AS FLG, ";
        $query .= "     NAMECD2 || '  ' || NAME1 AS LABEL, ";
        $query .= "     CASE WHEN ABBV1 IS NULL ";
        $query .= "          THEN NAMECD2 || '  ' || NAME1 ";
        $query .= "          ELSE NAMECD2 || '  ' || ABBV1 || '  ' || NAME1 ";
        $query .= "     END AS LABEL2 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "        YEAR    = '{$model->year}' ";
        $query .= "    AND NAMECD1 = '".$namecd1."' ";
        return $query;
    }

    //名称マスタ(所見欄を無効にするNAMECD2を取得)
    public function getNameMstDisableCodes($model, $setInNamecd1)
    {
        $query  = " SELECT ";
        $query .= "     NAMECD2 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "        YEAR       = '{$model->year}' ";
        $query .= "    AND NAMECD1    = '{$setInNamecd1}' ";
        $query .= "    AND NAMESPARE2 = '1' ";
        return $query;
    }

    //更新対象のレコードが存在するのかチェック
    public function checkHdat($model)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     MEDEXAM_HDAT ";
        $query .= " WHERE ";
        $query .= "        YEAR     = '{$model->year}' ";
        $query .= "    AND SCHREGNO = '{$model->schregno}' ";

        return $query;
    }

    //MEDEXAM_HDAT(健康診断ヘッダデータ)を UPDATE
    public function getHdatUpdate($model)
    {
        $db = Query::dbCheckOut();

        $data["TOOTH_DATE"][TEXT] = $model->field["TOOTH_DATE"];
        $data["REGISTERCD"][TEXT] = STAFFCD;
        $data["UPDATED"][FUNC]    = "sysdate()";

        $where = " WHERE YEAR = '{$model->year}' AND SCHREGNO = '{$model->schregno}' ";

        $db->query(Query::updateSQL($data, "MEDEXAM_HDAT", $where));
        Query::dbCheckIn($db);
        return ;
    }

    //MEDEXAM_HDAT(健康診断ヘッダデータ)をINSERT
    public function &getHdatInsert($model)
    {
        $db = Query::dbCheckOut();

        $data["YEAR"][TEXT]       = $model->year;
        $data["SCHREGNO"][TEXT]   = $model->schregno;
        $data["TOOTH_DATE"][TEXT] = $model->field["TOOTH_DATE"];
        $data["REGISTERCD"][TEXT] = STAFFCD;
        $data["UPDATED"][FUNC]    = "sysdate()";

        $db->query(Query::insertSQL($data, "MEDEXAM_HDAT"));
        Query::dbCheckIn($db);
        return ;
    }

    //MEDEXAM_TOOTH_DAT(健康診断歯口腔データ)をUPDATE or INSERT
    public function &getInsertUpdateQuery($model, $flag)
    {
        $db = Query::dbCheckOut();
        //「学校歯科医・日付」が設定されていないとき、「健康診断実施日付」をセットする

        $data["YEAR"][TEXT]                = $model->year;
        $data["SCHREGNO"][TEXT]            = $model->schregno;
        $data["JAWS_JOINTCD"][TEXT]        = $model->field["JAWS_JOINTCD"];
        $data["JAWS_JOINTCD2"][TEXT]       = $model->field["JAWS_JOINTCD2"];
        $data["PLAQUECD"][TEXT]            = $model->field["PLAQUECD"];
        $data["GUMCD"][TEXT]               = $model->field["GUMCD"];
        $data["CALCULUS"][TEXT]            = $model->field["CALCULUS"];
        $data["ORTHODONTICS"][TEXT]        = $model->field["ORTHODONTICS"] ? 1 : 0;
        $data["BABYTOOTH"][NUMBER]         = $model->field["BABYTOOTH"];
        $data["REMAINBABYTOOTH"][NUMBER]   = $model->field["REMAINBABYTOOTH"];
        $data["TREATEDBABYTOOTH"][NUMBER]  = $model->field["TREATEDBABYTOOTH"];
        $data["BRACK_BABYTOOTH"][NUMBER]   = $model->field["BRACK_BABYTOOTH"];
        $data["ADULTTOOTH"][NUMBER]        = $model->field["ADULTTOOTH"];
        $data["REMAINADULTTOOTH"][NUMBER]  = $model->field["REMAINADULTTOOTH"];
        $data["TREATEDADULTTOOTH"][NUMBER] = $model->field["TREATEDADULTTOOTH"];
        $data["LOSTADULTTOOTH"][NUMBER]    = $model->field["LOSTADULTTOOTH"];
        $data["BRACK_ADULTTOOTH"][NUMBER]  = $model->field["BRACK_ADULTTOOTH"];
        $data["OTHERDISEASECD"][TEXT]      = $model->field["OTHERDISEASECD"];
        $data["DENTISTREMARKCD"][TEXT]     = $model->field["DENTISTREMARKCD"];
        $data["DENTISTREMARK"][TEXT]       = $model->field["DENTISTREMARK"];
        $data["DENTISTTREATCD"][TEXT]      = $model->field["DENTISTTREATCD"];
        $data["DENTISTTREAT"][TEXT]        = $model->field["DENTISTTREAT"];
        $data["OTHERDISEASE"][TEXT]        = $model->field["OTHERDISEASE"];
        $data["DENTISTREMARKDATE"][TEXT]   = $model->field["DENTISTREMARKDATE"];

        foreach ($model->subField as $key => $val) {
            $data[$key][TEXT] = $val;
        }

        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][FUNC]              = "sysdate()";

        if ($flag == "insert") {
            $db->query(Query::insertSQL($data, "MEDEXAM_TOOTH_DAT"));
        } elseif ($flag == "update") {
            $where = " WHERE YEAR = '".$model->year."' AND SCHREGNO = '".$model->schregno."' ";
            $db->query(Query::updateSQL($data, "MEDEXAM_TOOTH_DAT", $where));
        }

        $query = "DELETE FROM MEDEXAM_TOOTH_DETAIL_DAT WHERE YEAR = '{$model->year}' AND SCHREGNO = '{$model->schregno}' ";
        $db->query($query);

        $data2 = array();
        $data2["YEAR"][TEXT]          = $model->year;
        $data2["SCHREGNO"][TEXT]      = $model->schregno;
        $data2["TOOTH_SEQ"][TEXT]     = "002";
        $data2["TOOTH_REMARK1"][TEXT] = $model->field["OTHERDISEASECD2"];
        $data2["TOOTH_REMARK2"][TEXT] = $model->field["OTHERDISEASE2"];
        $db->query(Query::insertSQL($data2, "MEDEXAM_TOOTH_DETAIL_DAT"));

        $data2 = array();
        $data2["YEAR"][TEXT]          = $model->year;
        $data2["SCHREGNO"][TEXT]      = $model->schregno;
        $data2["TOOTH_SEQ"][TEXT]     = "005";
        $data2["TOOTH_REMARK1"][TEXT] = $model->field["DENTISTREMARKCD2"];
        $data2["TOOTH_REMARK2"][TEXT] = $model->field["DENTISTREMARK2"];
        $data2["TOOTH_REMARK3"][TEXT] = $model->field["DENTISTREMARKCD3"];
        $data2["TOOTH_REMARK4"][TEXT] = $model->field["DENTISTREMARK3"];
        $data2["TOOTH_REMARK5"][TEXT] = $model->field["DENTISTTREATCD2"];
        $data2["TOOTH_REMARK6"][TEXT] = $model->field["DENTISTTREAT2_1"];
        $db->query(Query::insertSQL($data2, "MEDEXAM_TOOTH_DETAIL_DAT"));

        Query::dbCheckIn($db);
        return ;
    }

    //MEDEXAM_TOOTH_DATを DELETE
    public function &getDeleteQuery($model)
    {
        $db = Query::dbCheckOut();

        $query  = " DELETE FROM MEDEXAM_TOOTH_DAT ";
        $query .= " WHERE  YEAR     = '".$model->year."' ";
        $query .= "    AND SCHREGNO = '".$model->schregno."'";

        $db->query($query);

        $query  = " DELETE FROM MEDEXAM_TOOTH_DETAIL_DAT ";
        $query .= " WHERE  YEAR     = '".$model->year."' ";
        $query .= "    AND SCHREGNO = '".$model->schregno."'";

        $db->query($query);
        Query::dbCheckIn($db);
        return $result;
    }

    //MEDEXAM_DET_DAT(健康診断詳細データを取得)
    public function getMedexamDetDat($model)
    {
        $db = Query::dbCheckOut();

        $query  = "SELECT *  ";
        $query .= "  FROM MEDEXAM_DET_DAT ";
        $query .= "WHERE  SCHREGNO = '".$model->schregno."' ";
        $query .= "   AND YEAR     = '".$model->year."'";

        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);
        return $row;
    }

    //MEDEXAM_DET_DATが存在しない場合はMEDEXAM_HDATを削除
    public function getHdatDeleteQuery($model)
    {
        $query  = " DELETE FROM ";
        $query .= "     MEDEXAM_HDAT ";
        $query .= " WHERE ";
        $query .= "        YEAR     = '{$model->year}' ";
        $query .= "    AND SCHREGNO = '{$model->schregno}' ";

        $db = Query::dbCheckOut();
        $db->query($query);
        Query::dbCheckIn($db);
    }

    //MEDEXAM_DET_DATがあればTOOTH_DATEだけNULLにする
    public function getHdatDateDeleteQuery($model)
    {
        $data["TOOTH_DATE"][TEXT] = "";
        $data["REGISTERCD"][TEXT] = STAFFCD;
        $data["UPDATED"][FUNC]    = "sysdate()";

        $where  = " WHERE ";
        $where .= "        YEAR     = '{$model->year}' ";
        $where .= "    AND SCHREGNO = '{$model->schregno}' ";

        $db = Query::dbCheckOut();
        $query = Query::updateSQL($data, "MEDEXAM_HDAT", $where);
        $db->query($query);
        Query::dbCheckIn($db);
    }

    //一括更新処理画面用の生徒一覧
    public function getStudent($model)
    {
        $arr = explode("-", $model->GradeClass);
        $query  = "SELECT a.NAME_SHOW";
        $query .= "      ,b.SCHREGNO";
        $query .= "      ,b.ATTENDNO";
        $query .= "      ,FISCALYEAR(ENT_DATE) AS ENT_YEAR ";
        $query .= "  FROM SCHREG_BASE_MST a RIGHT OUTER JOIN ";
        $query .= "       SCHREG_REGD_DAT b";
        $query .= "    ON a.SCHREGNO = b.SCHREGNO";
        $query .= " WHERE b.YEAR     = '".$model->year."'";
        $query .= "   AND b.SEMESTER = '".$model->semester."'";
        $query .= "   AND b.GRADE    = '".$arr[0]."'";
        $query .= "   AND b.HR_CLASS = '".$arr[1]."'";
        $query .= " ORDER BY ATTENDNO";
        return $query;
    }

    public function getDentistremarkDate($year, $schregno)
    {
        $query  = " SELECT ";
        $query .= "     DENTISTREMARKDATE ";
        $query .= " FROM ";
        $query .= "     MEDEXAM_TOOTH_DAT ";
        $query .= " WHERE ";
        $query .= "        YEAR     = '{$year}' ";
        $query .= "    AND SCHREGNO = '{$schregno}' ";

        return $query;
    }

    //MEDEXAM_TOOTH_DATに対する一括更新、追加
    public function replaceUpdateTooth($model, $schregno, $flag, $db)
    {
        //口腔の疾病及び異常
        if ($model->replace_data["tooth_check"][17] == "1") {
            $where  = " WHERE ";
            $where .= "        YEAR      = '{$model->year}' ";
            $where .= "    AND SCHREGNO  = '{$schregno}' ";
            $where .= "    AND TOOTH_SEQ = '002' ";
            $query = "SELECT COUNT(*) AS CNT FROM MEDEXAM_TOOTH_DETAIL_DAT ".$where;
            $cnt = $db->getOne($query);

            $data2 = array();
            $data2["TOOTH_REMARK1"][TEXT]  = $model->replace_data["tooth_field"]["OTHERDISEASECD2"];
            $data2["TOOTH_REMARK2"][TEXT]  = $model->replace_data["tooth_field"]["OTHERDISEASE2"];
            $data2["REGISTERCD"][TEXT]     = STAFFCD;
            $data2["UPDATED"][FUNC]        = "sysdate()";

            if ($cnt > 0) {
                $db->query(Query::updateSQL($data2, "MEDEXAM_TOOTH_DETAIL_DAT", $where));
            } else {
                $data2["YEAR"][TEXT]           = $model->year;
                $data2["SCHREGNO"][TEXT]       = $schregno;
                $data2["TOOTH_SEQ"][TEXT]      = "002";
                $db->query(Query::insertSQL($data2, "MEDEXAM_TOOTH_DETAIL_DAT"));
            }
        }
        //所見2、所見3、事後措置2
        if ($model->replace_data["tooth_check"][18] == "1" || $model->replace_data["tooth_check"][19] == "1" || $model->replace_data["tooth_check"][21] == "1") {
            $where  = " WHERE ";
            $where .= "        YEAR      = '{$model->year}' ";
            $where .= "    AND SCHREGNO  = '{$schregno}' ";
            $where .= "    AND TOOTH_SEQ = '005' ";
            $query = "SELECT COUNT(*) AS CNT FROM MEDEXAM_TOOTH_DETAIL_DAT ".$where;
            $cnt = $db->getOne($query);

            $data2 = array();
            $data2["TOOTH_REMARK1"][TEXT]  = $model->replace_data["tooth_field"]["DENTISTREMARKCD2"];
            $data2["TOOTH_REMARK2"][TEXT]  = $model->replace_data["tooth_field"]["DENTISTREMARK2"];
            $data2["TOOTH_REMARK3"][TEXT]  = $model->replace_data["tooth_field"]["DENTISTREMARKCD3"];
            $data2["TOOTH_REMARK4"][TEXT]  = $model->replace_data["tooth_field"]["DENTISTREMARK3"];
            $data2["TOOTH_REMARK5"][TEXT]  = $model->replace_data["tooth_field"]["DENTISTTREATCD2"];
            $data2["TOOTH_REMARK6"][TEXT]  = $model->replace_data["tooth_field"]["DENTISTTREAT2_1"];
            $data2["REGISTERCD"][TEXT]     = STAFFCD;
            $data2["UPDATED"][FUNC]        = "sysdate()";

            if ($cnt > 0) {
                $db->query(Query::updateSQL($data2, "MEDEXAM_TOOTH_DETAIL_DAT", $where));
            } else {
                $data2["YEAR"][TEXT]           = $model->year;
                $data2["SCHREGNO"][TEXT]       = $schregno;
                $data2["TOOTH_SEQ"][TEXT]      = "005";
                $db->query(Query::insertSQL($data2, "MEDEXAM_TOOTH_DETAIL_DAT"));
            }
        }

        foreach ($model->replace_data["tooth_field"] as $key => $val) {
            //口腔の疾病及び異常
            if ($key == "OTHERDISEASECD2") {
                continue;
            } elseif ($key == "OTHERDISEASE2") {
                continue;
            }
            //所見2
            if ($key == "DENTISTREMARKCD2") {
                continue;
            }
            if ($key == "DENTISTREMARK2") {
                continue;
            }
            //所見3
            if ($key == "DENTISTREMARKCD3") {
                continue;
            }
            if ($key == "DENTISTREMARK3") {
                continue;
            }
            //事後措置2
            if ($key == "DENTISTTREATCD2") {
                continue;
            }
            if ($key == "DENTISTTREAT2_1") {
                continue;
            }

            $data[$key][TEXT] = $val;
        }
        $data["REGISTERCD"][TEXT] = STAFFCD;
        $data["UPDATED"][FUNC]    = "sysdate()";

        if ($flag == "insert") {
            $data["YEAR"][TEXT]     = $model->year;
            $data["SCHREGNO"][TEXT] = $schregno;
            $query = Query::insertSQL($data, "MEDEXAM_TOOTH_DAT");
        } else {
            $where  ="WHERE  SCHREGNO = '".$schregno."' ";
            $where .="   AND YEAR     = '".$model->year."'";
            $query = Query::updateSQL($data, "MEDEXAM_TOOTH_DAT", $where);
        }
        return $query;
    }

    //MEDEXAM_HDATに対する一括更新、追加
    public function replaceUpdateHead($year, $tooth_date, $schregno, $flag)
    {
        $data["YEAR"][TEXT]         = $year;
        $data["SCHREGNO"][TEXT]     = $schregno;
        $data["TOOTH_DATE"][TEXT]   = $tooth_date;
        $data["REGISTERCD"][TEXT]   = STAFFCD;
        $data["UPDATED"][FUNC]      = "sysdate()";

        if ($flag == "noRecord") {
            $query = Query::insertSQL($data, "MEDEXAM_HDAT");
        } else {
            $where  = "WHERE  SCHREGNO = '" .$schregno ."' ";
            $where .= "   AND YEAR     = '".$year."'";
            $query = Query::updateSQL($data, "MEDEXAM_HDAT", $where);
        }
        $query;
        return $query;
    }

    //MEDEXAM_HDAT歯式更新
    public function sisikiInsUpd($model, $db)
    {
        $select  = "SELECT COUNT(*) FROM MEDEXAM_TOOTH_DAT ";
        $select .= "WHERE  SCHREGNO = '".$model->schregno."' ";
        $select .= "   AND YEAR     = '".$model->year."'";

        $dataUmu = $db->getOne($select);
        foreach ($model->subField as $key => $val) {
            $data[$key][TEXT] = $val;
        }
        foreach ($model->subTotalField as $key => $val) {
            $data[$key][NUMBER] = $val;
        }

        $data["REGISTERCD"][TEXT]   = STAFFCD;
        $data["UPDATED"][FUNC]      = "sysdate()";

        if ($dataUmu > 0) {
            $where  = "WHERE  SCHREGNO = '" .$model->schregno ."' ";
            $where .= "   AND YEAR     = '".$model->year."'";
            $query = Query::updateSQL($data, "MEDEXAM_TOOTH_DAT", $where);
            $db->query($query);
        } else {
            $data["YEAR"][TEXT]         = $model->year;
            $data["SCHREGNO"][TEXT]     = $model->schregno;
            $query = Query::insertSQL($data, "MEDEXAM_TOOTH_DAT");
            $db->query($query);
        }

        return ;
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

    //校種取得
    public function getSchoolKind($grade)
    {
        $query  = " SELECT ";
        $query .= "    SCHOOL_KIND ";
        $query .= " FROM ";
        $query .= "    SCHREG_REGD_GDAT ";
        $query .= " WHERE ";
        $query .= "        YEAR  = '".CTRL_YEAR."' ";
        $query .= "    AND GRADE = '".$grade."' ";

        return $query;
    }

    //生徒項目名取得
    public function getSchName($model)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     REMARK1, ";
        $query .= "     SCHOOLCD ";
        $query .= " FROM ";
        $query .= "     SETTING_DAT ";
        $query .= " WHERE ";
        $query .= "     SEQ = '001' ";
        $query .= " ORDER BY ";
        $query .= "     SCHOOLCD ";

        return $query;
    }
}
