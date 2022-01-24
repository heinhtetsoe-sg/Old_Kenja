<?php

require_once('for_php7.php');

class knjf020Query extends Query
{

    //最小学年（校種毎）
    public function getMinGrade($model)
    {
        $query  = " SELECT ";
        $query .= "     NAME2 AS MIN_GRADE ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR    = '".CTRL_YEAR."' ";
        $query .= "     AND NAMECD1 = 'A023' ";
        $query .= "     AND NAME1   = '{$model->getSchKind}' ";

        return $query;
    }

    //生徒健康診断ヘッダデータ取得
    public function getMedexamHdat($model)
    {
        $db = Query::dbCheckOut();

        $query  = "SELECT * FROM MEDEXAM_HDAT ";
        $query .= "WHERE SCHREGNO = '".$model->schregno."' AND ";
        $query .= "      YEAR = '".$model->year."'";

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
        $query .= "     YEAR     = '{$year}' AND ";
        $query .= "     SCHREGNO = '{$schregno}' ";

        return $query;
    }
    //生徒健康診断歯口腔データ取得
    public function getMedexamToothDat($model)
    {
        $db = Query::dbCheckOut();

        $query  = "SELECT ";
        $query .= "    T1.* ";
        $query .= "    , DT002.TOOTH_REMARK3 AS OTHERDISEASE_REMARK1 ";
        $query .= "    , DT002.TOOTH_REMARK4 AS OTHERDISEASE_REMARK2 ";
        $query .= "    , DT002.TOOTH_REMARK5 AS OTHERDISEASE_REMARK3 ";
        $query .= "    , DT002.TOOTH_REMARK6 AS OTHERDISEASE_REMARK4 ";
        $query .= "    , DT003.TOOTH_REMARK3 AS OTHERDISEASECD3 ";
        $query .= "    , DT003.TOOTH_REMARK4 AS OTHERDISEASECD4 ";
        $query .= "    , DT003.TOOTH_REMARK5 AS OTHERDISEASE3 ";
        $query .= "    , DT003.TOOTH_REMARK6 AS OTHERDISEASE4 ";
        $query .= "    , DT005.TOOTH_REMARK1 AS DENTISTREMARK_REMARK1 ";
        $query .= "    , DT005.TOOTH_REMARK2 AS DENTISTREMARK_REMARK2 ";
        $query .= "    , DT005.TOOTH_REMARK3 AS DENTISTREMARK_REMARK3 ";
        $query .= "    , DT005.TOOTH_REMARK4 AS DENTISTREMARK_REMARK4 ";
        $query .= "FROM ";
        $query .= "    V_MEDEXAM_TOOTH_DAT T1 ";
        $query .= "    LEFT JOIN MEDEXAM_TOOTH_DETAIL_DAT DT002 ";
        $query .= "           ON DT002.YEAR      = T1.YEAR ";
        $query .= "          AND DT002.SCHREGNO  = T1.SCHREGNO ";
        $query .= "          AND DT002.TOOTH_SEQ = '002' ";
        $query .= "    LEFT JOIN MEDEXAM_TOOTH_DETAIL_DAT DT003 ";
        $query .= "           ON DT003.YEAR      = T1.YEAR ";
        $query .= "          AND DT003.SCHREGNO  = T1.SCHREGNO ";
        $query .= "          AND DT003.TOOTH_SEQ = '003' ";
        $query .= "    LEFT JOIN MEDEXAM_TOOTH_DETAIL_DAT DT005 ";
        $query .= "           ON DT005.YEAR      = T1.YEAR ";
        $query .= "          AND DT005.SCHREGNO  = T1.SCHREGNO ";
        $query .= "          AND DT005.TOOTH_SEQ = '005' ";
        $query .= "WHERE T1.SCHREGNO = '".$model->schregno."' AND ";
        $query .= "      T1.YEAR = '".$model->year."'";

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
        $query .= "     SCHREGNO = '" .$model->schregno."'";
        return $query;
    }
    //生徒学年クラス取得
    public function getSchregRegdDatData($model)
    {
        $query  = " SELECT t1.GRADE,t1.HR_CLASS,t1.ATTENDNO, ";
        $query .= "        t2.HR_NAME ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT t1 ";
        $query .= "     LEFT JOIN SCHREG_REGD_HDAT t2 ON t2.YEAR = t1.YEAR ";
        $query .= "     AND t2.SEMESTER = t1.SEMESTER ";
        $query .= "     AND t2.GRADE = t1.GRADE ";
        $query .= "     AND t2.HR_CLASS = t1.HR_CLASS ";
        $query .= " WHERE t1.SCHREGNO = '" .$model->schregno."' AND ";
        $query .= "       t1.YEAR = '".$model->year."' AND ";
        $query .= "       t1.SEMESTER = '".$model->semester."' ";

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
        $query .= "     SCHREGNO = '".$model->schregno."' ";
        $query .= "     AND YEAR = '".$model->year."'";

        return $query;
    }

    //名称マスタ
    public function getNameMst($model, $namecd1)
    {
        $query  = " SELECT ";
        $query .= "     ABBV1 AS SHOW, ";
        $query .= "     NAMECD2 AS VALUE, ";
        if ($model->z010 == "musashinohigashi" && ($namecd1 == "F511" || $namecd1 == "F510" || $namecd1 == "F520" || $namecd1 == "F513")) {
            $query .= "     NAME2 AS LABEL, ";
        } else {
            $query .= "     NAMECD2 || '  ' || NAME1 AS LABEL, ";
        }
        $query .= "     CASE WHEN ABBV1 IS NULL ";
        $query .= "          THEN NAMECD2 || '  ' || NAME1 ";
        $query .= "          ELSE NAMECD2 || '  ' || ABBV1 || '  ' || NAME1 ";
        $query .= "     END AS LABEL2 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR    = '{$model->year}' AND ";
        $query .= "     NAMECD1 = '".$namecd1."'";
        return $query;
    }

    //名称マスタ(予備２取得)
    public function getNameMstSpare2($model, $setInNamecd1)
    {
        $query  = " SELECT ";
        $query .= "     NAMECD1, ";
        $query .= "     NAMECD2 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR    = '{$model->year}' ";
        $query .= "     AND NAMECD1 IN ({$setInNamecd1}) ";
        $query .= "     AND NAMESPARE2 = '1' ";
        return $query;
    }

    //名称マスタ（Z010）
    public function getZ010($model)
    {
        $query  = " SELECT ";
        $query .= "     NAME1 ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'Z010' AND ";
        $query .= "     NAMECD2 = '00'";
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
        $query .= "         YEAR = '{$model->year}' ";
        $query .= "     AND SCHREGNO = '{$model->schregno}' ";

        return $query;
    }

    //MEDEXAM_HDAT(健康診断ヘッダデータ)を UPDATE
    public function getHdatUpdate($model)
    {
        $db = Query::dbCheckOut();

        $data["TOOTH_DATE"][TEXT]   = $model->field["TOOTH_DATE"];
        $data["REGISTERCD"][TEXT]   = STAFFCD;
        $data["UPDATED"][FUNC]      = "sysdate()";

        $where = " WHERE YEAR = '{$model->year}' AND SCHREGNO = '{$model->schregno}' ";

        $db->query(Query::updateSQL($data, "MEDEXAM_HDAT", $where));
        Query::dbCheckIn($db);
        return ;
    }

    //MEDEXAM_HDAT(健康診断ヘッダデータ)をINSERT
    public function &getHdatInsert($model)
    {
        $db = Query::dbCheckOut();

        $data["YEAR"][TEXT]         = $model->year;
        $data["SCHREGNO"][TEXT]     = $model->schregno;
        $data["TOOTH_DATE"][TEXT]   = $model->field["TOOTH_DATE"];
        $data["REGISTERCD"][TEXT]   = STAFFCD;
        $data["UPDATED"][FUNC]      = "sysdate()";

        $db->query(Query::insertSQL($data, "MEDEXAM_HDAT"));
        Query::dbCheckIn($db);
        return ;
    }

    //MEDEXAM_TOOTH_DAT(健康診断派口腔データ)をUPDATE or INSERT
    public function &getInsertUpdateQuery($model, $flag)
    {
        $db = Query::dbCheckOut();
        //「学校歯科医・日付」が設定されていないとき、「健康診断実施日付」をセットする
        //近大は「学校歯科医・日付」が設定されていないとき、nullのまま更新する
        if ($model->z010 !== 'KINDAI' && $model->z010 !== 'KINJUNIOR') {
            if (!$model->field["DENTISTREMARKDATE"]) {
                $model->field["DENTISTREMARKDATE"] = $model->field["TOOTH_DATE"];
            }
        }

        $data["YEAR"][TEXT]                 = $model->year;
        $data["SCHREGNO"][TEXT]             = $model->schregno;
        $data["JAWS_JOINTCD"][TEXT]         = $model->field["JAWS_JOINTCD"];
        if ($model->Properties["printKenkouSindanIppan"] == "2" || $model->Properties["printKenkouSindanIppan"] == "3" || ($model->Properties["printKenkouSindanIppan"] == "1" && ($model->z010 === "mieken" || $model->KNJF030D || $model->Properties["KenkouSindan_Ippan_Pattern"] == "1"))) {
            $data["JAWS_JOINTCD3"][TEXT]        = $model->field["JAWS_JOINTCD3"];
        }
        $data["JAWS_JOINTCD2"][TEXT]        = $model->field["JAWS_JOINTCD2"];
        $data["PLAQUECD"][TEXT]             = $model->field["PLAQUECD"];
        $data["GUMCD"][TEXT]                = $model->field["GUMCD"];
        if ($model->Properties["printKenkouSindanIppan"] != "2") {
            $data["CALCULUS"][TEXT]             = $model->field["CALCULUS"];
        }
        $data["ORTHODONTICS"][TEXT]         = $model->field["ORTHODONTICS"] ? 1 : 0;
        $data["BABYTOOTH"][NUMBER]          = $model->field["BABYTOOTH"];
        $data["REMAINBABYTOOTH"][NUMBER]    = $model->field["REMAINBABYTOOTH"];
        $data["TREATEDBABYTOOTH"][NUMBER]   = $model->field["TREATEDBABYTOOTH"];
        $data["BRACK_BABYTOOTH"][NUMBER]    = $model->field["BRACK_BABYTOOTH"];
        $data["ADULTTOOTH"][NUMBER]         = $model->field["ADULTTOOTH"];
        $data["REMAINADULTTOOTH"][NUMBER]   = $model->field["REMAINADULTTOOTH"];
        $data["TREATEDADULTTOOTH"][NUMBER]  = $model->field["TREATEDADULTTOOTH"];
        $data["LOSTADULTTOOTH"][NUMBER]     = $model->field["LOSTADULTTOOTH"];
        $data["BRACK_ADULTTOOTH"][NUMBER]   = $model->field["BRACK_ADULTTOOTH"];
        if ($model->Properties["printKenkouSindanIppan"] == "2" || $model->Properties["printKenkouSindanIppan"] == "3") {
            if ($model->Properties["printKenkouSindanIppan"] == "2") {
                $data["OTHERDISEASECD"][TEXT]       = $model->field["OTHERDISEASECD"];
                $data["DENTISTTREATCD"][TEXT]       = $model->field["DENTISTTREATCD"];
            }
            $data["DENTISTREMARK_CO"][NUMBER]   = $model->field["DENTISTREMARK_CO"];
            $data["DENTISTREMARK_GO"][TEXT]     = $model->field["DENTISTREMARK_GO"];
            $data["DENTISTREMARK_G"][TEXT]      = $model->field["DENTISTREMARK_G"];
            $data["DENTISTTREAT"][TEXT]         = $model->field["DENTISTTREAT"];
        } else {
            $data["OTHERDISEASECD"][TEXT]       = $model->field["OTHERDISEASECD"];
            $data["DENTISTREMARKCD"][TEXT]      = $model->field["DENTISTREMARKCD"];
            $data["DENTISTREMARK"][TEXT]        = $model->field["DENTISTREMARK"];
            $data["DENTISTTREATCD"][TEXT]       = $model->field["DENTISTTREATCD"];
            $data["DENTISTTREAT"][TEXT]         = $model->field["DENTISTTREAT"];
        }
        $data["OTHERDISEASE"][TEXT]         = $model->field["OTHERDISEASE"];
        $data["DENTISTREMARKDATE"][TEXT]    = $model->field["DENTISTREMARKDATE"];

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

        if ($model->Properties["printKenkouSindanIppan"] == "2") {
            $data2 = array();
            $data2["YEAR"][TEXT]            = $model->year;
            $data2["SCHREGNO"][TEXT]        = $model->schregno;
            $data2["TOOTH_SEQ"][TEXT]       = "001";
            $data2["TOOTH_REMARK2"][TEXT]   = $model->field["DENTISTTREAT2"];
            $data2["TOOTH_REMARK3"][TEXT]   = $model->field["DENTISTTREAT3"];
            $db->query(Query::insertSQL($data2, "MEDEXAM_TOOTH_DETAIL_DAT"));
        }

        if ($model->Properties["printKenkouSindanIppan"] != "2") {
            $data2 = array();
            $data2["YEAR"][TEXT]            = $model->year;
            $data2["SCHREGNO"][TEXT]        = $model->schregno;
            $data2["TOOTH_SEQ"][TEXT]       = "002";
            $data2["TOOTH_REMARK1"][TEXT]   = $model->field["OTHERDISEASECD2"];
            $data2["TOOTH_REMARK2"][TEXT]   = $model->field["OTHERDISEASE2"];
            if ($model->is_f020_otherdisese_hyouji) {
                $data2["TOOTH_REMARK3"][TEXT]   = $model->field["OTHERDISEASE_REMARK1"];
                $data2["TOOTH_REMARK4"][TEXT]   = $model->field["OTHERDISEASE_REMARK2"];
                $data2["TOOTH_REMARK5"][TEXT]   = $model->field["OTHERDISEASE_REMARK3"];
                $data2["TOOTH_REMARK6"][TEXT]   = $model->field["OTHERDISEASE_REMARK4"];
            }
            $db->query(Query::insertSQL($data2, "MEDEXAM_TOOTH_DETAIL_DAT"));
        }

        if ($model->z010 == "miyagiken") {
            $data2 = array();
            $data2["YEAR"][TEXT]            = $model->year;
            $data2["SCHREGNO"][TEXT]        = $model->schregno;
            $data2["TOOTH_SEQ"][TEXT]       = "003";
            $data2["TOOTH_REMARK3"][TEXT]   = $model->field["OTHERDISEASECD3"];
            $data2["TOOTH_REMARK4"][TEXT]   = $model->field["OTHERDISEASECD4"];
            $db->query(Query::insertSQL($data2, "MEDEXAM_TOOTH_DETAIL_DAT"));
        } elseif ($model->is_f020_otherdisese_hyouji2) {
            $data2 = array();
            $data2["YEAR"][TEXT]            = $model->year;
            $data2["SCHREGNO"][TEXT]        = $model->schregno;
            $data2["TOOTH_SEQ"][TEXT]       = "003";
            $data2["TOOTH_REMARK3"][TEXT]   = $model->field["OTHERDISEASECD3"];
            $data2["TOOTH_REMARK4"][TEXT]   = $model->field["OTHERDISEASECD4"];
            $data2["TOOTH_REMARK5"][TEXT]   = $model->field["OTHERDISEASE3"];
            $data2["TOOTH_REMARK6"][TEXT]   = $model->field["OTHERDISEASE4"];
            $db->query(Query::insertSQL($data2, "MEDEXAM_TOOTH_DETAIL_DAT"));
        }

        if ($model->z010 == "mieken") {
            $data2 = array();
            $data2["YEAR"][TEXT]            = $model->year;
            $data2["SCHREGNO"][TEXT]        = $model->schregno;
            $data2["TOOTH_SEQ"][TEXT]       = "004";
            $data2["TOOTH_REMARK1"][TEXT]   = $model->field["DOC_NAME"];
            $data2["REGISTERCD"][TEXT]      = STAFFCD;
            $data2["UPDATED"][FUNC]         = "sysdate()";
            $db->query(Query::insertSQL($data2, "MEDEXAM_TOOTH_DETAIL_DAT"));
        }

        if ($model->is_f020_dentistremark_hyouji) {
            $data2 = array();
            $data2["YEAR"][TEXT]            = $model->year;
            $data2["SCHREGNO"][TEXT]        = $model->schregno;
            $data2["TOOTH_SEQ"][TEXT]       = "005";
            $data2["TOOTH_REMARK1"][TEXT]   = $model->field["DENTISTREMARK_REMARK1"];
            $data2["TOOTH_REMARK2"][TEXT]   = $model->field["DENTISTREMARK_REMARK2"];
            $data2["TOOTH_REMARK3"][TEXT]   = $model->field["DENTISTREMARK_REMARK3"];
            $data2["TOOTH_REMARK4"][TEXT]   = $model->field["DENTISTREMARK_REMARK4"];
            $db->query(Query::insertSQL($data2, "MEDEXAM_TOOTH_DETAIL_DAT"));
        }

        Query::dbCheckIn($db);
        return ;
    }

    //MEDEXAM_TOOTH_DATを DELETE
    public function &getDeleteQuery($model)
    {
        $db = Query::dbCheckOut();

        $query  = " DELETE FROM MEDEXAM_TOOTH_DAT ";
        $query .= " WHERE YEAR = '".$model->year."' AND ";
        $query .= "       SCHREGNO = '".$model->schregno."'";

        $db->query($query);

        $query  = " DELETE FROM MEDEXAM_TOOTH_DETAIL_DAT ";
        $query .= " WHERE YEAR = '".$model->year."' AND ";
        $query .= "       SCHREGNO = '".$model->schregno."'";

        $db->query($query);
        Query::dbCheckIn($db);
        return $result;
    }
    //MEDEXAM_DET_DAT(健康診断詳細データを取得)
    public function getMedexamDetDat($model)
    {
        $db = Query::dbCheckOut();

        $query  = "SELECT * FROM MEDEXAM_DET_DAT ";
        $query .= "WHERE SCHREGNO = '".$model->schregno."' AND ";
        $query .= "      YEAR = '".$model->year."'";

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
        $query .= "     YEAR     = '{$model->year}' ";
        $query .= " AND SCHREGNO = '{$model->schregno}' ";

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
        $where .= "     YEAR     = '{$model->year}' ";
        $where .= " AND SCHREGNO = '{$model->schregno}' ";

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
        $query .= "     YEAR = '{$year}' AND ";
        $query .= "     SCHREGNO = '{$schregno}' ";

        return $query;
    }

    //MEDEXAM_TOOTH_DATに対する一括更新、追加
    public function replaceUpdateTooth($model, $schregno, $flag, $db)
    {
        if ($model->Properties["printKenkouSindanIppan"] == "2") {
            if ($model->replace_data["tooth_check"][15] == "1") {
                $query = "SELECT COUNT(*) AS CNT FROM MEDEXAM_TOOTH_DETAIL_DAT WHERE YEAR = '{$model->year}' AND SCHREGNO = '{$schregno}' AND TOOTH_SEQ = '001'";
                $cnt = $db->getOne($query);

                $data2 = array();
                if ($cnt > 0) {
                    $data2["TOOTH_REMARK2"][TEXT]  = $model->replace_data["tooth_field"]["DENTISTTREAT2"];
                    $data2["TOOTH_REMARK3"][TEXT]  = $model->replace_data["tooth_field"]["DENTISTTREAT3"];
                    $where  = " WHERE ";
                    $where .= "     YEAR = '{$model->year}' ";
                    $where .= "     AND SCHREGNO = '{$schregno}' ";
                    $where .= "     AND TOOTH_SEQ = '001' ";
                    $db->query(Query::updateSQL($data2, "MEDEXAM_TOOTH_DETAIL_DAT", $where));
                } else {
                    $data2["YEAR"][TEXT]         = $model->year;
                    $data2["SCHREGNO"][TEXT]     = $schregno;
                    $data2["TOOTH_SEQ"][TEXT]      = "001";
                    $data2["TOOTH_REMARK2"][TEXT]  = $model->replace_data["tooth_field"]["DENTISTTREAT2"];
                    $data2["TOOTH_REMARK3"][TEXT]  = $model->replace_data["tooth_field"]["DENTISTTREAT3"];
                    $db->query(Query::insertSQL($data2, "MEDEXAM_TOOTH_DETAIL_DAT"));
                }
            }
        } else {
            if ($model->z010 === "mieken") {
                if ($model->replace_data["tooth_check"][16] == "1") {
                    $where  = " WHERE ";
                    $where .= "     YEAR = '{$model->year}' ";
                    $where .= "     AND SCHREGNO = '{$schregno}' ";
                    $where .= "     AND TOOTH_SEQ = '004' ";
                    $query = "SELECT COUNT(*) AS CNT FROM MEDEXAM_TOOTH_DETAIL_DAT ".$where;
                    $cnt = $db->getOne($query);

                    $data2 = array();
                    $data2["TOOTH_REMARK1"][TEXT]  = $model->replace_data["tooth_field"]["DOC_NAME"];
                    $data2["REGISTERCD"][TEXT]   = STAFFCD;
                    $data2["UPDATED"][FUNC]      = "sysdate()";
                    if ($cnt > 0) {
                        $db->query(Query::updateSQL($data2, "MEDEXAM_TOOTH_DETAIL_DAT", $where));
                    } else {
                        $data2["YEAR"][TEXT]           = $model->year;
                        $data2["SCHREGNO"][TEXT]       = $schregno;
                        $data2["TOOTH_SEQ"][TEXT]      = "004";
                        $db->query(Query::insertSQL($data2, "MEDEXAM_TOOTH_DETAIL_DAT"));
                    }
                }
            }
        }
        foreach ($model->replace_data["tooth_field"] as $key => $val) {
            if (
                $model->Properties["printKenkouSindanIppan"] == "2" &&
                ($key == "DENTISTTREAT2" || $key == "DENTISTTREAT3")
            ) {
                continue;
            } else {
                if ($key == "DOC_NAME") {
                    continue;
                }
                if ($key == "DENTISTREMARK_CO") {
                    $data[$key][NUMBER] = $val;
                } else {
                    $data[$key][TEXT] = $val;
                }
            }
        }
        $data["REGISTERCD"][TEXT] = STAFFCD;
        $data["UPDATED"][FUNC]    = "sysdate()";

        if ($flag == "insert") {
            $data["YEAR"][TEXT]     = $model->year;
            $data["SCHREGNO"][TEXT] = $schregno;
            $query = Query::insertSQL($data, "MEDEXAM_TOOTH_DAT");
        } else {
            $where  ="WHERE SCHREGNO = '".$schregno."' AND ";
            $where .="      YEAR = '".$model->year."'";
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
            $where  = "WHERE SCHREGNO = '" .$schregno ."' AND ";
            $where .= "      YEAR = '".$year."'";
            $query = Query::updateSQL($data, "MEDEXAM_HDAT", $where);
        }
        $query;
        return $query;
    }
    //MEDEXAM_HDAT歯式更新
    public function sisikiInsUpd($model, $db)
    {
        $select  = "SELECT COUNT(*) FROM MEDEXAM_TOOTH_DAT ";
        $select .= "WHERE SCHREGNO = '".$model->schregno."' AND ";
        $select .= "      YEAR = '".$model->year."'";

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
            $where  = "WHERE SCHREGNO = '" .$model->schregno ."' AND ";
            $where .= "      YEAR = '".$model->year."'";
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
        $query .= "     REMARK1 ";
        if ($model->Properties["useSchool_KindField"] == "1") {
        $query .= "    , SCHOOLCD ";
        }
        $query .= " FROM ";
        $query .= "     SETTING_DAT ";
        $query .= " WHERE ";
        $query .= "     SEQ = '001' ";
        if ($model->Properties["useSchool_KindField"] == "1") {
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " AND SCHOOLCD    = '".sprintf("%012d", SCHOOLCD)."' ";
                $query .= " AND SCHOOL_KIND = '".$model->getSchKind."' ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND SCHOOLCD    = '".sprintf("%012d", SCHOOLCD)."' ";
            $query .= " AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        } else {
            $query .= " AND SCHOOL_KIND IN (SELECT ";
            $query .= "                         S2.SCHOOL_KIND ";
            $query .= "                     FROM ";
            $query .= "                         SCHREG_REGD_DAT S1, ";
            $query .= "                         SCHREG_REGD_GDAT S2 ";
            $query .= "                     WHERE ";
            $query .= "                         S1.YEAR     = S2.YEAR AND ";
            $query .= "                         S1.YEAR     = '".$model->year."' AND ";
            $query .= "                         S1.SEMESTER = '".$model->semester."' AND ";
            $query .= "                         S1.GRADE    = S2.GRADE AND ";
            $query .= "                         S1.SCHREGNO = '".$model->schregno."' ";
            $query .= "                     ) ";
        }
        $query .= " ORDER BY ";
        $query .= "     SCHOOLCD ";
    }
        return $query;
    }
}
