<?php

require_once("for_php7.php");

class knja125jQuery extends Query
{
    public function getZ010()
    {
        $query  = " SELECT ";
        $query .= "   T1.NAME1 ";
        $query .= " FROM ";
        $query .= "     NAME_MST T1 ";
        $query .= " WHERE ";
        $query .= "     T1.NAMECD1 = 'Z010' AND T1.NAMECD2 = '00' ";

        return $query;
    }

    //１レコード取得
    public function getTrainRow($model, $flg)
    {
        $query  = " SELECT ";
        $query .= "   T1.*, ";
        $query .= "   L1.TRAIN_REF1, ";
        $query .= "   L1.TRAIN_REF2, ";
        $query .= "   L1.TRAIN_REF3, ";
        $query .= "   L2.REMARK1, ";
        $query .= "   L3.REMARK1 AS DEVIATION, ";
        $query .= "   L4.REMARK1 AS REMARK1_002, ";
        $query .= "   L5.REMARK1 AS REMARK1_009 ";
        $query .= " FROM ";
        $query .= "     HTRAINREMARK_DAT T1 ";
        $query .= "     LEFT JOIN HTRAINREMARK_DETAIL_DAT L1 ON T1.YEAR = L1.YEAR ";
        $query .= "          AND T1.SCHREGNO = L1.SCHREGNO ";
        $query .= "     LEFT JOIN HTRAINREMARK_DETAIL2_DAT L2 ON T1.YEAR = L2.YEAR ";
        $query .= "          AND T1.SCHREGNO   = L2.SCHREGNO ";
        $query .= "          AND L2.HTRAIN_SEQ = '004' ";
        $query .= "     LEFT JOIN HTRAINREMARK_DETAIL2_DAT L3 ON T1.YEAR = L3.YEAR ";
        $query .= "          AND T1.SCHREGNO   = L3.SCHREGNO ";
        $query .= "          AND L3.HTRAIN_SEQ = '007' ";
        $query .= "     LEFT JOIN HTRAINREMARK_DETAIL2_HDAT L4 ";
        $query .= "          ON T1.SCHREGNO   = L4.SCHREGNO ";
        $query .= "          AND L4.HTRAIN_SEQ = '002' ";
        $query .= "     LEFT JOIN HTRAINREMARK_DETAIL2_DAT L5 ON T1.YEAR = L5.YEAR ";
        $query .= "          AND T1.SCHREGNO   = L5.SCHREGNO ";
        $query .= "          AND L5.HTRAIN_SEQ = '009' ";
        $query .= " WHERE ";
        if ($flg === 'sanshou') {
            $query .= "     T1.YEAR     < '".$model->exp_year."' AND ";
        } else {
            $query .= "     T1.YEAR     = '".$model->exp_year."' AND ";
        }
        $query .= "     T1.SCHREGNO = '".$model->schregno."' ";
        if ($flg === 'sanshou') {
            $query .= " ORDER BY ";
            $query .= "     T1.YEAR ";
        }

        return $query;
    }

    //１レコード取得(HTRAINREMARK_DETAIL2_DAT)
    public function getTrainDetailRow($model, $seq)
    {
        $query  = " SELECT ";
        $query .= "   * ";
        $query .= " FROM ";
        $query .= "     HTRAINREMARK_DETAIL2_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR       = '".$model->exp_year."' AND ";
        $query .= "     SCHREGNO   = '".$model->schregno."' AND ";
        $query .= "     HTRAIN_SEQ = '".$seq."' ";

        return $query;
    }
    //１レコード取得(HTRAINREMARK_DETAIL2_HDAT)
    public function getTrainDetailHRow($model, $seq)
    {
        $query  = " SELECT ";
        $query .= "   * ";
        $query .= " FROM ";
        $query .= "     HTRAINREMARK_DETAIL2_HDAT ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO   = '".$model->schregno."' AND ";
        $query .= "     HTRAIN_SEQ = '".$seq."' ";

        return $query;
    }

    //年次の取得
    public function getAnnual($model)
    {
        $query  = " SELECT ";
        $query .= "     ANNUAL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO    = '".$model->schregno."' AND ";
        $query .= "     YEAR        = '".$model->exp_year."' AND ";
        $query .= "     SEMESTER    = '".$model->exp_semester."' ";

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
        $query .= "     T1.YEAR     = '$model->exp_year' AND ";
        $query .= "     T1.SEMESTER = '$model->exp_semester' AND ";
        $query .= "     T1.GRADE    = T2.GRADE AND ";
        $query .= "     T1.SCHREGNO = '$model->schregno' ";

        return $query;
    }

    //校種取得
    public function getSchoolKind2($model)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     T2.SCHOOL_KIND ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1, ";
        $query .= "     SCHREG_REGD_GDAT T2 ";
        $query .= " WHERE ";
        $query .= "         T1.YEAR     = T2.YEAR ";
        $query .= "     AND T1.YEAR     = '".$model->exp_year."' ";
        $query .= "     AND T1.GRADE    = T2.GRADE ";
        $query .= "     AND T1.SCHREGNO = '".$model->schregno."' ";
        $query .= "     AND T1.SEMESTER IN (SELECT ";
        $query .= "                         MAX(SEMESTER) ";
        $query .= "                     FROM ";
        $query .= "                         SCHREG_REGD_DAT S1 ";
        $query .= "                     WHERE ";
        $query .= "                             T1.YEAR     = S1.YEAR ";
        $query .= "                         AND T1.SCHREGNO = S1.SCHREGNO ";
        $query .= "                     ) ";

        return $query;
    }

    //INSERT
    public function &getInsertQuery($model)
    {
        $db = Query::dbCheckOut();

        //年次の取得
        $annual = $db->getOne(knja125jQuery::getAnnual($model));

        //存在チェック
        $check = $db->getCol(knja125jQuery::getTrainRow($model, ""));

        //存在チェック(道徳)
        $detailCheck004 = $db->getCol(knja125jQuery::getTrainDetailRow($model, "004"));

        $data = array();
        if (get_count($check) == 0) {
            $data["YEAR"][TEXT]                 = $model->exp_year;
            $data["SCHREGNO"][TEXT]             = $model->schregno;
            $data["ANNUAL"][TEXT]               = $annual;
            $data["TOTALSTUDYACT"][TEXT]        = $model->field["TOTALSTUDYACT"];
            $data["TOTALSTUDYVAL"][TEXT]        = $model->field["TOTALSTUDYVAL"];
            if ($model->Properties["train_ref_1_2_3_use_J"] != '1') {
                $data["TOTALREMARK"][TEXT]          = $model->field["TOTALREMARK"];
            }
            $data["ATTENDREC_REMARK"][TEXT]     = $model->field["ATTENDREC_REMARK"];
            $data["VIEWREMARK"][TEXT]           = $model->field["VIEWREMARK"];
            $data["REGISTERCD"][TEXT]           = STAFFCD;
            $data["UPDATED"][FUNC]              = "sysdate()";

            $db->query(Query::insertSQL($data, "HTRAINREMARK_DAT"));
        } else {
            $data["ANNUAL"][TEXT]               = $annual;
            $data["TOTALSTUDYACT"][TEXT]        = $model->field["TOTALSTUDYACT"];
            $data["TOTALSTUDYVAL"][TEXT]        = $model->field["TOTALSTUDYVAL"];
            if ($model->Properties["train_ref_1_2_3_use_J"] != '1') {
                $data["TOTALREMARK"][TEXT]          = $model->field["TOTALREMARK"];
            }
            $data["ATTENDREC_REMARK"][TEXT]     = $model->field["ATTENDREC_REMARK"];
            $data["VIEWREMARK"][TEXT]           = $model->field["VIEWREMARK"];
            $data["REGISTERCD"][TEXT]           = STAFFCD;
            $data["UPDATED"][FUNC]              = "sysdate()";

            $where  = " WHERE ";
            $where .= "     YEAR     = '".$model->exp_year."' AND ";
            $where .= "     SCHREGNO = '".$model->schregno."' ";

            $db->query(Query::updateSQL($data, "HTRAINREMARK_DAT", $where));
        }

        $data = array();
        if (get_count($detailCheck004) == 0) {
            $data["YEAR"][TEXT]                 = $model->exp_year;
            $data["SCHREGNO"][TEXT]             = $model->schregno;
            $data["HTRAIN_SEQ"][TEXT]           = '004';
            $data["REMARK1"][TEXT]              = $model->field["REMARK1"];
            $data["REGISTERCD"][TEXT]           = STAFFCD;
            $data["UPDATED"][FUNC]              = "sysdate()";

            $db->query(Query::insertSQL($data, "HTRAINREMARK_DETAIL2_DAT"));
        } else {
            $data["REMARK1"][TEXT]              = $model->field["REMARK1"];
            $data["REGISTERCD"][TEXT]           = STAFFCD;
            $data["UPDATED"][FUNC]              = "sysdate()";

            $where  = " WHERE ";
            $where .= "     YEAR       = '".$model->exp_year."' AND ";
            $where .= "     SCHREGNO   = '".$model->schregno."' AND ";
            $where .= "     HTRAIN_SEQ = '004' ";

            $db->query(Query::updateSQL($data, "HTRAINREMARK_DETAIL2_DAT", $where));
        }

        if ($model->z010 = "teihachi") {
            //存在チェック(中学で履修済み備考)
            $detailCheck002 = $db->getCol(knja125jQuery::getTrainDetailHRow($model, "002"));

            $data = array();
            $data["SCHREGNO"][TEXT]             = $model->schregno;
            $data["HTRAIN_SEQ"][TEXT]           = '002';
            $data["REMARK1"][TEXT]              = $model->field["REMARK1_002"];
            $data["REGISTERCD"][TEXT]           = STAFFCD;
            $data["UPDATED"][FUNC]              = "sysdate()";

            if (get_count($detailCheck002) == 0) {
                $db->query(Query::insertSQL($data, "HTRAINREMARK_DETAIL2_HDAT"));
            } else {
                $where  = " WHERE ";
                $where .= "     SCHREGNO   = '".$model->schregno."' AND ";
                $where .= "     HTRAIN_SEQ = '002' ";
                $db->query(Query::updateSQL($data, "HTRAINREMARK_DETAIL2_HDAT", $where));
            }
        }

        if ($model->Properties["knje125j_Chinokensa_show"] == '1') {
            //存在チェック(知能検査偏差値)
            $detailCheck007 = $db->getCol(knja125jQuery::getTrainDetailRow($model, "007"));

            $data = array();
            $data["YEAR"][TEXT]                 = $model->exp_year;
            $data["SCHREGNO"][TEXT]             = $model->schregno;
            $data["HTRAIN_SEQ"][TEXT]           = '007';
            $data["REMARK1"][TEXT]              = $model->field["DEVIATION"];
            $data["REGISTERCD"][TEXT]           = STAFFCD;
            $data["UPDATED"][FUNC]              = "sysdate()";

            if (get_count($detailCheck007) == 0) {
                $db->query(Query::insertSQL($data, "HTRAINREMARK_DETAIL2_DAT"));
            } else {
                $where  = " WHERE ";
                $where .= "     YEAR       = '".$model->exp_year."' AND ";
                $where .= "     SCHREGNO   = '".$model->schregno."' AND ";
                $where .= "     HTRAIN_SEQ = '007' ";
                $db->query(Query::updateSQL($data, "HTRAINREMARK_DETAIL2_DAT", $where));
            }
        }
        if ($model->Properties["Totalremark_2disp_J"] == '1') {
            //存在チェック
            $detailCheck009 = $db->getCol(knja125jQuery::getTrainDetailRow($model, "009"));

            $data = array();
            $data["YEAR"][TEXT]                 = $model->exp_year;
            $data["SCHREGNO"][TEXT]             = $model->schregno;
            $data["HTRAIN_SEQ"][TEXT]           = '009';
            $data["REMARK1"][TEXT]              = $model->field["REMARK1_009"];
            $data["REGISTERCD"][TEXT]           = STAFFCD;
            $data["UPDATED"][FUNC]              = "sysdate()";

            if (get_count($detailCheck009) == 0) {
                $db->query(Query::insertSQL($data, "HTRAINREMARK_DETAIL2_DAT"));
            } else {
                $where  = " WHERE ";
                $where .= "     YEAR       = '".$model->exp_year."' AND ";
                $where .= "     SCHREGNO   = '".$model->schregno."' AND ";
                $where .= "     HTRAIN_SEQ = '009' ";
                $db->query(Query::updateSQL($data, "HTRAINREMARK_DETAIL2_DAT", $where));
            }
        }

        //詳細
        //総合所見（３分割）
        if ($model->Properties["train_ref_1_2_3_use_J"] == '1') {
            $query = knja125jQuery::getDetailDataCnt($model->schregno, $model->exp_year);
            $dataCnt = $db->getOne($query);

            $data = array();
            if ($dataCnt == 0) {
                $data["YEAR"][TEXT]                 = $model->exp_year;
                $data["SCHREGNO"][TEXT]             = $model->schregno;
                $data["TRAIN_REF1"][TEXT]           = $model->field["TRAIN_REF1"];
                $data["TRAIN_REF2"][TEXT]           = $model->field["TRAIN_REF2"];
                $data["TRAIN_REF3"][TEXT]           = $model->field["TRAIN_REF3"];
                $data["REGISTERCD"][TEXT]           = STAFFCD;
                $data["UPDATED"][FUNC]              = "sysdate()";

                $db->query(Query::insertSQL($data, "HTRAINREMARK_DETAIL_DAT"));
            } else {
                $data["TRAIN_REF1"][TEXT]           = $model->field["TRAIN_REF1"];
                $data["TRAIN_REF2"][TEXT]           = $model->field["TRAIN_REF2"];
                $data["TRAIN_REF3"][TEXT]           = $model->field["TRAIN_REF3"];
                $data["REGISTERCD"][TEXT]           = STAFFCD;
                $data["UPDATED"][FUNC]              = "sysdate()";

                $where  = " WHERE ";
                $where .= "     YEAR     = '".$model->exp_year."' AND ";
                $where .= "     SCHREGNO = '".$model->schregno."' ";

                $db->query(Query::updateSQL($data, "HTRAINREMARK_DETAIL_DAT", $where));
            }
        }

        Query::dbCheckIn($db);
        return ;
    }

    //DETAILDATA有無
    public function getDetailDataCnt($schregno, $exp_year)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     HTRAINREMARK_DETAIL_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$exp_year."'";
        $query .= "     AND SCHREGNO = '".$schregno."'";

        return $query;
    }

    //行動記録の取得
    public function getBehavior($model)
    {
        $query  = " SELECT ";
        $query .= "     DIV, ";
        $query .= "     CODE, ";
        $query .= "     ANNUAL, ";
        $query .= "     RECORD ";
        $query .= " FROM ";
        $query .= "     BEHAVIOR_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".$model->exp_year."' AND ";
        $query .= "     SCHREGNO    = '".$model->schregno."' ";
        $query .= " ORDER BY ";
        $query .= "     DIV, ";
        $query .= "     CODE ";

        return $query;
    }

    //INSERT
    public function &getInsertQuery2($model)
    {
        $db = Query::dbCheckOut();

        //年次の取得
        $annual = $db->getOne(knja125jQuery::getAnnual($model));

        //行動の記録・特別活動の記録（削除）
        $query  = " DELETE FROM ";
        $query .= "     BEHAVIOR_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".$model->exp_year."' AND ";
        $query .= "     SCHREGNO    = '".$model->schregno."' AND ";
        $query .= "     DIV IN ('1', '2') ";

        $db->query($query);

        //行動の記録・特別活動の記録（追加）
        for ($j=1; $j<3; $j++) {
            $cnt = ($j == "1") ? "11" : "4";

            for ($i=1; $i<$cnt; $i++) {
                $ival = $j . sprintf("%02d", $i);
                if ($model->record["RECORD"][$ival] != "1") {
                    continue;
                }

                $data["YEAR"][TEXT]         = $model->exp_year;
                $data["SCHREGNO"][TEXT]     = $model->schregno;
                $data["DIV"][TEXT]          = $j;
                $data["CODE"][TEXT]         = sprintf("%02d", $i);
                $data["ANNUAL"][TEXT]       = $annual;
                $data["RECORD"][TEXT]       = "1";
                $data["REGISTERCD"][TEXT]   = STAFFCD;
                $data["UPDATED"][FUNC]      = "sysdate()";

                $db->query(Query::insertSQL($data, "BEHAVIOR_DAT"));
            }
        }

        //存在チェック
        $check = $db->getCol(knja125jQuery::getTrainRow($model, ""));

        //特別活動の記録の観点
        $data1 = array();
        if (get_count($check) == 0) {
            $data1["YEAR"][TEXT]                = $model->exp_year;
            $data1["SCHREGNO"][TEXT]            = $model->schregno;
            $data1["ANNUAL"][TEXT]              = $annual;
            if ($model->Properties["Specialactremark_3disp_J"] == '1') {
                $data1["CLASSACT"][TEXT]         = $model->field["CLASSACT"];
                $data1["STUDENTACT"][TEXT]       = $model->field["STUDENTACT"];
                $data1["SCHOOLEVENT"][TEXT]      = $model->field["SCHOOLEVENT"];
            } else {
                $data1["SPECIALACTREMARK"][TEXT]    = $model->field["SPECIALACTREMARK"];
            }
            $data1["REGISTERCD"][TEXT]          = STAFFCD;
            $data1["UPDATED"][FUNC]             = "sysdate()";

            $db->query(Query::insertSQL($data1, "HTRAINREMARK_DAT"));
        } else {
            $data1["ANNUAL"][TEXT]              = $annual;
            if ($model->Properties["Specialactremark_3disp_J"] == '1') {
                $data1["CLASSACT"][TEXT]         = $model->field["CLASSACT"];
                $data1["STUDENTACT"][TEXT]       = $model->field["STUDENTACT"];
                $data1["SCHOOLEVENT"][TEXT]      = $model->field["SCHOOLEVENT"];
            } else {
                $data1["SPECIALACTREMARK"][TEXT]    = $model->field["SPECIALACTREMARK"];
            }
            $data1["REGISTERCD"][TEXT]          = STAFFCD;
            $data1["UPDATED"][FUNC]             = "sysdate()";

            $where  = " WHERE ";
            $where .= "     YEAR     = '".$model->exp_year."' AND ";
            $where .= "     SCHREGNO = '".$model->schregno."' ";

            $db->query(Query::updateSQL($data1, "HTRAINREMARK_DAT", $where));
        }

        Query::dbCheckIn($db);
        return ;
    }

    //セット日々出欠備考
    public function getSemesRemark($db, $model)
    {
        $query  = "  SELECT ";
        $query .= "      REMARK1, ";
        $query .= "      CASE WHEN MONTH = '01' THEN '13' WHEN MONTH = '02' THEN '14' WHEN MONTH = '03' THEN '15' ELSE MONTH END AS SORT_MONTH ";
        $query .= "  FROM ";
        $query .= "      ATTEND_SEMES_REMARK_DAT ";
        $query .= "  WHERE ";
        $query .= "      COPYCD = '0' ";
        $query .= "  AND YEAR = '{$model->exp_year}' ";
        $query .= "  AND SCHREGNO = '{$model->schregno}' ";
        $query .= "  ORDER BY ";
        $query .= "      YEAR, ";
        $query .= "      SEMESTER, ";
        $query .= "      SORT_MONTH ";

        $result = $db->query($query);
        $set_remark = "";
        $count = 0;
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            if ($count == 0) {
                $set_remark .= $row["REMARK1"];
            } else {
                if ($row["REMARK1"] != "") {
                    $set_remark .= "／".$row["REMARK1"];
                }
            }
            $count++;
        }
        return $set_remark;
    }

    //通知表所見参照
    public function getHreportRemarkDat($model)
    {
        $query  = " SELECT  ";
        $query .= "     T1.SEMESTER, ";
        $query .= "     T1.SEMESTERNAME, ";
        $query .= "     T2.TOTALSTUDYTIME, ";
        $query .= "     T2.COMMUNICATION ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST T1 ";
        $query .= "     LEFT JOIN HREPORTREMARK_DAT T2 ON T1.YEAR       = T2.YEAR ";
        $query .= "                                   AND T1.SEMESTER   = T2.SEMESTER ";
        $query .= "                                   AND T2.SCHREGNO   = '" .$model->schregno ."' ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR     = '" .$model->exp_year ."' AND ";
        $query .= "     T1.SEMESTER <> '9' ";
        $query .= " ORDER BY ";
        $query .= "     T1.SEMESTER";

        return $query;
    }

    //学期取得
    public function getSemester($model)
    {
        $query  = " SELECT ";
        $query .= "     S1.SEMESTER, ";
        $query .= "     S1.SEMESTERNAME ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST S1 ";
        $query .= " WHERE ";
        $query .= "         S1.YEAR = '".$model->exp_year."' ";
        $query .= " ORDER BY ";
        $query .= "     S1.SEMESTER ";

        return $query;
    }

    //HREPORTREMARK_DAT取得
    public function getHreportremarkDatKahen($model, $semester, $dispData)
    {
        $query  = " SELECT ";
        $query .= "     HREP_D.{$dispData["FIELD"]} ";
        $query .= " FROM ";
        $query .= "     HREPORTREMARK_DAT HREP_D ";
        $query .= " WHERE ";
        $query .= "     HREP_D.YEAR = '{$model->exp_year}' ";
        $query .= "     AND HREP_D.SEMESTER = '{$semester}' ";
        $query .= "     AND HREP_D.SCHREGNO = '{$model->schregno}' ";

        return $query;
    }

    //HREPORTREMARK_DETAIL_DAT取得
    public function getHreportremarkDetailDatKahen($model, $semester, $dispData)
    {
        list($div, $code) = explode(":", $dispData["WHERE"]);
        $query  = " SELECT ";
        $query .= "     HREP_DD.{$dispData["FIELD"]} ";
        $query .= " FROM ";
        $query .= "     HREPORTREMARK_DETAIL_DAT HREP_DD ";
        $query .= " WHERE ";
        $query .= "     HREP_DD.YEAR = '{$model->exp_year}' ";
        $query .= "     AND HREP_DD.SEMESTER = '{$semester}' ";
        $query .= "     AND HREP_DD.SCHREGNO = '{$model->schregno}' ";
        $query .= "     AND HREP_DD.DIV = '{$div}' ";
        $query .= "     AND HREP_DD.CODE = '{$code}' ";

        return $query;
    }

    //学校マスタの校種有無チェック
    public function checkSchoolMst()
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     SYSIBM.SYSCOLUMNS ";
        $query .= " WHERE ";
        $query .= "     TBNAME  = 'SCHOOL_MST' AND ";
        $query .= "     NAME    = 'SCHOOL_KIND' ";

        return $query;
    }

    //出欠の記録参照
    public function getAttendSemesDat($model, $semester, $knjSchoolMst)
    {
        $query  = " WITH ATTEND_SEM AS ( ";
        $query .= "     SELECT ";
        $query .= "         SEMESTER, ";
        $query .= "         SCHREGNO, ";
        if ($knjSchoolMst["SEM_OFFDAYS"] == "1") {
            $query .= "         SUM(VALUE(LESSON,0) - VALUE(ABROAD,0))  AS LESSON, ";
            $query .= "         SUM(VALUE(SICK,0) + VALUE(NOTICE,0) + VALUE(NONOTICE,0) + VALUE(OFFDAYS,0))  AS SICK, ";
        } else {
            $query .= "         SUM(VALUE(LESSON,0) - VALUE(ABROAD,0) - VALUE(OFFDAYS,0))  AS LESSON, ";
            $query .= "         SUM(VALUE(SICK,0) + VALUE(NOTICE,0) + VALUE(NONOTICE,0))  AS SICK, ";
        }
        $query .= "         SUM(VALUE(SUSPEND,0) ";
        if ($model->Properties["useVirus"] == "true") {
            $query .= "             + VALUE(VIRUS,0) ";
        }
        if ($model->Properties["useKoudome"] == "true") {
            $query .= "             + VALUE(KOUDOME,0) ";
        }
        $query .= "             + VALUE(MOURNING,0))  AS SUSPEND ";
        $query .= "     FROM ";
        $query .= "         ATTEND_SEMES_DAT ";
        $query .= "     WHERE ";
        $query .= "         YEAR        = '".$model->exp_year."' AND ";
        $query .= "         SCHREGNO    = '".$model->schregno."' AND ";
        $query .= "         SEMESTER    = '".$semester."' ";
        $query .= "     GROUP BY ";
        $query .= "         SEMESTER, ";
        $query .= "         SCHREGNO ";
        $query .= " ) ";
        $query .= " , ATTEND_REMARK AS ( ";
        $query .= "     SELECT ";
        $query .= "         SEMESTER, ";
        $query .= "         SCHREGNO, ";
        $query .= "         ATTENDREC_REMARK ";
        $query .= "     FROM ";
        $query .= "         HREPORTREMARK_DAT ";
        $query .= "     WHERE ";
        $query .= "         YEAR        = '".$model->exp_year."' AND ";
        $query .= "         SCHREGNO    = '".$model->schregno."' AND ";
        $query .= "         SEMESTER    = '".$semester."' ";
        $query .= " ) ";
        $query .= " , MAIN AS ( ";
        $query .= "     SELECT ";
        $query .= "         T1.SEMESTER, ";
        $query .= "         T1.SCHREGNO, ";
        $query .= "         T1.LESSON, ";
        $query .= "         T1.SUSPEND, ";
        $query .= "         VALUE(T1.LESSON,0) - VALUE(T1.SUSPEND,0) AS ATTEND1, ";
        $query .= "         T1.SICK, ";
        $query .= "         VALUE(T1.LESSON,0) - VALUE(T1.SUSPEND,0) - VALUE(T1.SICK,0) AS ATTEND2, ";
        $query .= "         T2.ATTENDREC_REMARK ";
        $query .= "     FROM ";
        $query .= "         ATTEND_SEM T1 ";
        $query .= "         LEFT JOIN ATTEND_REMARK T2 ON T1.SEMESTER = T2.SEMESTER AND ";
        $query .= "             T1.SCHREGNO = T2.SCHREGNO ";
        $query .= " ) ";

        $query .= "     SELECT ";
        $query .= "         T1.SEMESTER, ";
        $query .= "         T2.SCHREGNO, ";
        $query .= "         T2.LESSON, ";
        $query .= "         T2.SUSPEND, ";
        $query .= "         T2.ATTEND1, ";
        $query .= "         T2.SICK, ";
        $query .= "         T2.ATTEND2, ";
        $query .= "         T2.ATTENDREC_REMARK ";
        $query .= "     FROM ";
        $query .= "         SEMESTER_MST T1 ";
        $query .= "         LEFT JOIN MAIN T2 ON T1.SEMESTER = T2.SEMESTER ";
        $query .= "     WHERE ";
        $query .= "         T1.YEAR     = '".$model->exp_year."' AND ";
        $query .= "         T1.SEMESTER = '".$semester."' ";

        return $query;
    }

    //出欠の記録参照
    public function getAttendSemesDat2($model, $knjSchoolMst)
    {
        $query  = " WITH ATTEND_SEM AS ( ";
        $query .= "     SELECT ";
        $query .= "         SEMESTER, ";
        $query .= "         SCHREGNO, ";
        if ($knjSchoolMst["SEM_OFFDAYS"] == "1") {
            $query .= "         SUM(VALUE(LESSON,0) - VALUE(ABROAD,0))  AS LESSON, ";
            $query .= "         SUM(VALUE(SICK,0) + VALUE(NOTICE,0) + VALUE(NONOTICE,0) + VALUE(OFFDAYS,0))  AS SICK, ";
        } else {
            $query .= "         SUM(VALUE(LESSON,0) - VALUE(ABROAD,0) - VALUE(OFFDAYS,0))  AS LESSON, ";
            $query .= "         SUM(VALUE(SICK,0) + VALUE(NOTICE,0) + VALUE(NONOTICE,0))  AS SICK, ";
        }
        $query .= "         SUM(VALUE(SUSPEND,0) ";
        if ($model->Properties["useVirus"] == "true") {
            $query .= "             + VALUE(VIRUS,0) ";
        }
        if ($model->Properties["useKoudome"] == "true") {
            $query .= "             + VALUE(KOUDOME,0) ";
        }
        $query .= "             ) AS SUSPEND, ";
        $query .= "         SUM(VALUE(MOURNING,0)) AS MOURNING, ";
        $query .= "         SUM(VALUE(LATE,0)) AS LATE, ";
        $query .= "         SUM(VALUE(EARLY,0)) AS EARLY ";
        $query .= "     FROM ";
        $query .= "         ATTEND_SEMES_DAT ";
        $query .= "     WHERE ";
        $query .= "             YEAR     = '".$model->exp_year."' ";
        $query .= "         AND SCHREGNO = '".$model->schregno."' ";
        $query .= "     GROUP BY ";
        $query .= "         SEMESTER, ";
        $query .= "         SCHREGNO ";
        $query .= " ), MAIN AS ( ";
        $query .= "     SELECT ";
        $query .= "         SEMESTER, ";
        $query .= "         SCHREGNO, ";
        $query .= "         LESSON, ";
        $query .= "         SUSPEND, ";
        $query .= "         MOURNING, ";
        $query .= "         VALUE(LESSON,0) - VALUE(SUSPEND,0) - VALUE(MOURNING,0) AS ATTEND1, ";
        $query .= "         SICK, ";
        $query .= "         VALUE(LESSON,0) - VALUE(SUSPEND,0) - VALUE(MOURNING,0) - VALUE(SICK,0) AS ATTEND2, ";
        $query .= "         LATE, ";
        $query .= "         EARLY ";
        $query .= "     FROM ";
        $query .= "         ATTEND_SEM ";
        $query .= " ) ";

        $query .= "     SELECT ";
        $query .= "         T1.SEMESTER, ";
        $query .= "         T1.SEMESTERNAME, ";
        $query .= "         T2.SCHREGNO, ";
        $query .= "         T2.LESSON, ";
        $query .= "         T2.SUSPEND, ";
        $query .= "         T2.MOURNING, ";
        $query .= "         T2.ATTEND1, ";
        $query .= "         T2.SICK, ";
        $query .= "         T2.ATTEND2, ";
        $query .= "         T2.LATE, ";
        $query .= "         T2.EARLY ";
        $query .= "     FROM ";
        $query .= "         SEMESTER_MST T1 ";
        $query .= "         LEFT JOIN MAIN T2 ON T1.SEMESTER = T2.SEMESTER ";
        $query .= "     WHERE ";
        $query .= "             T1.YEAR      = '".$model->exp_year."' ";
        $query .= "         AND T1.SEMESTER <> '9' ";
        $query .= "     ORDER BY ";
        $query .= "         T1.SEMESTER ";

        return $query;
    }

    //名称マスタ取得
    public function getNameMst($model, $namecd1, $name1 = "")
    {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$model->exp_year."' ";
        $query .= "     AND NAMECD1 = '".$namecd1."' ";
        if ($name1) {
            $query .= "     AND NAME1 = '".$name1."' ";
        }

        return $query;
    }

    //行動の記録取得
    public function getBehaviorSemesDat($model, $semester, $cd)
    {
        $query  = " SELECT ";
        $query .= "     CODE, ";
        $query .= "     RECORD ";
        $query .= " FROM ";
        $query .= "     BEHAVIOR_SEMES_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".$model->exp_year."' AND ";
        $query .= "     SCHREGNO    = '".$model->schregno."' AND ";
        $query .= "     SEMESTER    = '".$semester."' AND ";
        $query .= "     CODE        = '".$cd."' ";

        return $query;
    }

    //特別活動の記録取得
    public function getHreportremarkDetailDat($model, $semester, $div, $cd)
    {
        $query  = " SELECT ";
        $query .= "     DIV, ";
        $query .= "     CODE, ";
        $query .= "     REMARK1 ";
        $query .= " FROM ";
        $query .= "     HREPORTREMARK_DETAIL_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".$model->exp_year."' AND ";
        $query .= "     SCHREGNO    = '".$model->schregno."' AND ";
        $query .= "     SEMESTER    = '".$semester."' AND ";
        $query .= "     DIV         = '".$div."' AND ";
        $query .= "     CODE        = '".$cd."' ";

        return $query;
    }

    //道徳の通知票記録取得
    public function getHreportremarkDetailDatMoral($model, $semester, $div, $code)
    {
        $query  = " SELECT ";
        $query .= "     DIV, ";
        $query .= "     CODE, ";
        $query .= "     REMARK1 ";
        $query .= " FROM ";
        $query .= "     HREPORTREMARK_DETAIL_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$model->exp_year}' ";
        $query .= "     AND SEMESTER = '{$semester}' ";
        $query .= "     AND SCHREGNO = '{$model->schregno}' ";
        $query .= "     AND DIV = '{$div}' ";
        $query .= "     AND CODE = '{$code}' ";

        return $query;
    }
    //札幌開成判定
    public function getSapporoHantei()
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'Z010' AND ";
        $query .= "     NAMECD2 = '00' AND ";
        $query .= "     NAME1   = 'sapporo' ";

        return $query;
    }

    //行動の記録取得
    public function getActionDocumentDat($model)
    {
        $query  = " SELECT ";
        $query .= "     T1.ACTIONDATE, ";
        $query .= "     T1.SEQ, ";
        $query .= "     T1.ACTIONTIME, ";
        $query .= "     L1.NAME1 AS DIVIDECD, ";
        $query .= "     T1.TITLE, ";
        $query .= "     T1.TEXT ";
        $query .= " FROM ";
        $query .= "     ACTION_DOCUMENT_DAT T1 ";
        $query .= "     LEFT JOIN V_NAME_MST L1 ";
        $query .= "          ON L1.YEAR     = '".$model->exp_year."' ";
        $query .= "         AND L1.NAMECD1  = 'H307' ";
        $query .= "         AND L1.NAMECD2  = T1.DIVIDECD ";
        $query .= " WHERE ";
        $query .= "     T1.SCHREGNO = '".$model->schregno."' ";
        $query .= " ORDER BY ";
        $query .= "     T1.ACTIONDATE DESC, ";
        $query .= "     T1.ACTIONTIME DESC, ";
        $query .= "     T1.SEQ ";

        return $query;
    }

    //学年名取得
    public function getGradeName($model)
    {
        $query  = " SELECT ";
        $query .= "     GRADE_NAME1 ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".$model->exp_year."' AND ";
        $query .= "     GRADE       = '".$model->grade."' ";

        return $query;
    }

    //定型分マスター一覧取得
    public function getHtrainRemarkTempDat($model)
    {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     HTRAINREMARK_TEMP_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".$model->exp_year."' AND ";
        $query .= "     GRADE       = '".$model->grade."' AND ";
        if ($model->cmd === 'teikei') {
            $query .= "     DATA_DIV    = '11' ";
        } elseif ($model->cmd === 'teikei_act') {
            $query .= "     DATA_DIV    = '01' ";
        } else {
            $query .= "     DATA_DIV    = '02' ";
        }
        $query .= " ORDER BY ";
        $query .= "     PATTERN_CD ";

        return $query;
    }

    //通知票より読込
    public function getRecordTotalstudytimeDat($model, $classcd = "90")
    {
        $setyear = "";
        $setyear = $model->exp_year == "" ? CTRL_YEAR : $model->exp_year;
        $result = array();
        $targetSemester = "";
        if ($model->Properties["tutisyoSougoushokengakki"] == '1-2') {
            $targetSemester = "('2')";
        } elseif (preg_match("/1-([0-9]+)/", $model->Properties["tutisyoSougoushokengakki"], $result)) {
            $targetSemester = " (";
            $comma = "";
            for ($i = 0; $i < strlen($result[1]); $i++) {
                $targetSemester .= $comma."'".substr($result[1], $i, 1)."'";
                $comma = ", ";
            }
            $targetSemester .= ") ";
        } elseif ($model->Properties["tutisyoSougoushokengakki"] == '1') {
            $targetSemester = "('9')";
        }

        $query  = " SELECT ";
        $query .= "     TOTALSTUDYTIME, ";
        $query .= "     REMARK1, ";
        $query .= "     TOTALSTUDYACT ";
        $query .= " FROM ";
        $query .= "     RECORD_TOTALSTUDYTIME_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '{$model->schregno}' ";
        $query .= " AND YEAR =  '{$setyear}' ";
        if ($model->Properties["tutisyoSougoushokengakki"] == '1-2') {
            $query .= " AND CLASSCD = '".$classcd."' ";
        }
        if ($targetSemester) {
            $query .= " AND SEMESTER IN ". $targetSemester;
        }
        $query .= " ORDER BY ";
        $query .= "     YEAR, ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     CLASSCD, ";
            $query .= "     SCHOOL_KIND, ";
            $query .= "     CURRICULUM_CD, ";
        }
        $query .= "     SUBCLASSCD, ";
        $query .= "     SEMESTER ";

        return $query;
    }

    //近大用通知票より読込
    public function getKindaiSougakuKanten($model)
    {
        $setyear = "";
        $setyear = $model->exp_year == "" ? CTRL_YEAR : $model->exp_year;

        $query  = " WITH MAIN AS( ";
        $query .= "   SELECT ";
        $query .= "     GRADE, ";
        $query .= "     CLASSCD, ";
        $query .= "     SCHOOL_KIND, ";
        $query .= "     MAX(CURRICULUM_CD) AS CURRICULUM_CD, ";
        $query .= "     SUBCLASSCD ";
        $query .= "   FROM JVIEWNAME_GRADE_MST ";
        $query .= "   WHERE GRADE       IN (SELECT MAX(GRADE) FROM SCHREG_REGD_DAT WHERE SCHREGNO = '{$model->schregno}' AND YEAR = '{$setyear}' ) ";
        $query .= "     AND CLASSCD     = '90' ";
        $query .= "     AND SCHOOL_KIND = 'J' ";
        $query .= "     AND SUBCLASSCD  = '900200' "; // 総合的な探究の時間
        $query .= "   GROUP BY GRADE, CLASSCD, SCHOOL_KIND, SUBCLASSCD ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "   T2.VIEWNAME ";
        $query .= " FROM ";
        $query .= "   MAIN T1 ";
        $query .= "   INNER JOIN JVIEWNAME_GRADE_MST T2 ";
        $query .= "           ON T2.GRADE         = T1.GRADE ";
        $query .= "          AND T2.CLASSCD       = T1.CLASSCD ";
        $query .= "          AND T2.SCHOOL_KIND   = T1.SCHOOL_KIND ";
        $query .= "          AND T2.CURRICULUM_CD = T1.CURRICULUM_CD ";
        $query .= "          AND T2.SUBCLASSCD    = T1.SUBCLASSCD ";
        $query .= " ORDER BY ";
        $query .= "   T2.VIEWCD ";
        return $query;
    }

    //近大用通知票より読込
    public function getKindaiTsushisyo($model, $datadiv)
    {
        $setyear = "";
        $setyear = $model->exp_year == "" ? CTRL_YEAR : $model->exp_year;

        $query  = " SELECT  ";
        $query .= "   DT.SCHREGNO, ";
        $query .= "   DT.DIV, ";
        $query .= "   DT.CODE, ";
        $query .= "   DT.REMARK1, ";
        $query .= "   DT.REMARK2, ";
        $query .= "   DT.REMARK3 ";
        $query .= " FROM  ";
        $query .= "   HREPORTREMARK_DETAIL_DAT DT ";
        $query .= " WHERE  ";
        $query .= "       DT.YEAR     = '{$setyear}'  ";
        $query .= "   AND DT.SEMESTER = '3' "; // 3学期固定
        $query .= "   AND DT.SCHREGNO = '{$model->schregno}' ";
        if ($datadiv == "SOUGAKU") {
            $query .= "   AND DT.DIV = '07' "; // 07: 総合探究
            $query .= "   AND DT.CODE = '02' "; // 02: 評価
        } elseif ($datadiv == "DOUTOKU") {
            $query .= "   AND DT.DIV = '08' "; // 08: 道徳
            $query .= "   AND DT.CODE = '01' ";
        }
        $query .= " ORDER BY  ";
        $query .= "   SCHREGNO, ";
        $query .= "   DIV, ";
        $query .= "   CODE ";

        return $query;
    }

    //HREPORTREMARK_DATの取得
    public function getHreportremarkDat2($model)
    {
        $setyear = "";
        $setyear = $model->exp_year == "" ? CTRL_YEAR : $model->exp_year;
        $query  = " SELECT ";
        $query .= "     {$model->hreportremark} ";
        $query .= " FROM ";
        $query .= "     HREPORTREMARK_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR =  '{$setyear}' ";
        $query .= " AND SCHREGNO = '{$model->schregno}' ";
        $query .= " AND SEMESTER = '{$model->exp_semester}' ";
        return $query;
    }

    //HREPORTREMARK_DATの取得(道徳)
    public function getDoutokuHreportremarkDat($model, $field)
    {
        $setyear = "";
        $setyear = $model->exp_year == "" ? CTRL_YEAR : $model->exp_year;

        $query  = " SELECT ";
        $query .= "    T1.{$field} AS VALUE ";
        $query .= " FROM HREPORTREMARK_DAT T1 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR     =  '{$setyear}' ";
        $query .= " AND T1.SEMESTER = '9' ";
        $query .= " AND T1.SCHREGNO = '{$model->schregno}' ";

        return $query;
    }
    // HREPORTREMARK_DETAIL_DATの取得(道徳)
    public function getDoutokuHreportremarkDetailDat($model, $field, $div, $code)
    {
        $setyear = "";
        $setyear = $model->exp_year == "" ? CTRL_YEAR : $model->exp_year;

        $query  = " WITH MAX_SEMES AS ( ";
        $query .= "   SELECT MAX(SEMESTER) SEMESTER ";
        $query .= "   FROM SEMESTER_MST ";
        $query .= "   WHERE YEAR = '{$setyear}' ";
        $query .= "     AND SEMESTER <> '9' ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "    T1.{$field} AS VALUE ";
        $query .= " FROM HREPORTREMARK_DETAIL_DAT T1 ";
        $query .= " INNER JOIN MAX_SEMES SEMES ON T1.SEMESTER = SEMES.SEMESTER ";
        $query .= " WHERE T1.YEAR = '{$setyear}' ";
        $query .= "   AND T1.SCHREGNO = '{$model->schregno}' ";
        $query .= "   AND T1.DIV = '{$div}' ";
        $query .= "   AND T1.CODE = '{$code}' ";

        return $query;
    }
    //皆勤情報
    public function getKaikinData($model)
    {
        $query .= " SELECT ";
        $query .= "     CASE WHEN KAIKIN_DIV ='1' THEN '皆勤' WHEN KAIKIN_DIV ='2' THEN '精勤' ELSE '' END AS KAIKIN ";
        $query .= " FROM ";
        $query .= "     KAIKIN_DAT T1 ";
        $query .= " LEFT JOIN KAIKIN_MST T2 ";
        $query .= "     ON T1.KAIKIN_CD = T2.KAIKIN_CD ";
        $query .= "     AND T2.REF_YEAR = '1' ";
        $query .= " WHERE ";
        $query .= "     (T1.KAIKIN_FLG='1' ";
        $query .= "     OR T1.KAIKIN_FLG IS NULL) ";
        $query .= "     AND T1.SCHREGNO = '{$model->schregno}' ";

        return $query;
    }
    //入試順位
    public function getNyuusiZyunni($model)
    {
        $query .= " SELECT ";
        $query .= "     REMARK1 ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_RECEPT_DETAIL_DAT T1 ";
        $query .= " LEFT JOIN ";
        $query .= "     SCHREG_BASE_DETAIL_MST T2 ";
        $query .= "     ON T1.TESTDIV = T2.BASE_REMARK2 ";
        $query .= "     AND T1.RECEPTNO = T2.BASE_REMARK1 ";
        $query .= "     AND T2.BASE_SEQ ='003' ";
        $query .= " WHERE ";
        $query .= "     T1.SEQ = '015'      ";
        $query .= "     AND T1.ENTEXAMYEAR = '{$model->exp_year}' ";
        $query .= "     AND T1.APPLICANTDIV IN ('1', '2') ";
        $query .= "     AND T1.EXAM_TYPE = '1' ";
        $query .= "     AND T2.SCHREGNO='{$model->schregno}' ";

        return $query;
    }

    //学年順位
    public function getGakunenZyunni($model)
    {
        $query .= " SELECT ";
        $query .= "     GRADE_RANK ";
        $query .= " FROM ";
        $query .= "     RECORD_RANK_SDIV_DAT T1 ";
        $query .= " INNER JOIN ";
        $query .= "     SCHREG_REGD_GDAT T2 ";
        $query .= "     ON T1.YEAR = T2.YEAR ";
        $query .= "     AND T1.SCHOOL_KIND = T2.SCHOOL_KIND ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '{$model->exp_year}' ";
        $query .= "     AND T1.SEMESTER = '{$model->exp_semester}' ";
        $query .= "     AND T1.TESTKINDCD = '99' ";
        $query .= "     AND T1.TESTITEMCD = '00' ";
        $query .= "     AND T1.SCORE_DIV = '08' ";
        $query .= "     AND T1.CLASSCD = '99' ";
        $query .= "     AND T1.CURRICULUM_CD ='99' ";
        $query .= "     AND T1.SUBCLASSCD = '999999' ";
        $query .= "     AND T1.SCHREGNO='{$model->schregno}' ";
        $query .= "     AND T2.GRADE='{$model->grade}' ";

        return $query;
    }
    //IQ
    public function getIQ($model)
    {
        $query .= " SELECT ";
        $query .= "     IQ ";
        $query .= " FROM ";
        $query .= "     SCHREG_IQ_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$model->exp_year}' ";
        $query .= "     AND SCHREGNO = '{$model->schregno}' ";

        return $query;
    }

    //委員会活動参照
    public function getCommittee($model)
    {
        $query  = " SELECT ";
        $query .= "     T1.GRADE, ";
        $query .= "     G1.GRADE_NAME1, ";
        $query .= "     T1.SEMESTER, ";
        $query .= "     L3.NAME1 AS SEMESTER_SHOW, ";
        $query .= "     T1.SEQ, ";
        $query .= "     L1.COMMITTEENAME AS COMMITTEE_SHOW, ";
        $query .= "     T1.CHARGENAME AS CHARGE_SHOW, ";
        $query .= "     L2.NAME1 AS EXECUTIVE_SHOW ";
        $query .= " FROM ";
        $query .= "     SCHREG_COMMITTEE_HIST_DAT T1 ";
        $query .= "     INNER JOIN SCHREG_REGD_GDAT G1 ";
        $query .= "          ON T1.YEAR         = G1.YEAR ";
        $query .= "         AND T1.GRADE        = G1.GRADE ";
        if ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "     AND T1.SCHOOL_KIND  = G1.SCHOOL_KIND ";
        }
        $query .= "     LEFT JOIN COMMITTEE_MST L1 ";
        $query .= "          ON T1.COMMITTEE_FLG    = L1.COMMITTEE_FLG ";
        $query .= "         AND T1.COMMITTEECD      = L1.COMMITTEECD ";
        if ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "     AND T1.SCHOOLCD         = L1.SCHOOLCD ";
            $query .= "     AND T1.SCHOOL_KIND      = L1.SCHOOL_KIND ";
        }
        $query .= "     LEFT JOIN NAME_MST L2 ";
        $query .= "          ON L2.NAMECD1      = 'J002' ";
        $query .= "         AND T1.EXECUTIVECD  = L2.NAMECD2 ";
        $query .= "     LEFT JOIN NAME_MST L3 ";
        $query .= "          ON L3.NAMECD1      = 'J004' ";
        $query .= "         AND T1.SEMESTER     = L3.NAMECD2 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '{$model->exp_year}' AND ";
        $query .= "     T1.SCHREGNO     = '{$model->schregno}' ";
        if ($model->Properties["useSchool_KindField"] == "1") {
            $query .= " AND T1.SCHOOLCD     = '".sprintf("%012d", SCHOOLCD)."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.SEMESTER, ";
        $query .= "     T1.SEQ ";

        return $query;
    }

    //賞データ取得
    public function getHyosyo($model)
    {
        $query  = "";
        $query .= " SELECT ";
        $query .= "     DETAIL_SDATE, ";
        $query .= "     DETAILCD, ";
        $query .= "     L1.NAME1 AS DETAILCDNAME, ";
        $query .= "     CONTENT AS CONTENT, ";
        $query .= "     REMARK ";
        $query .= " FROM ";
        $query .= "     SCHREG_DETAILHIST_DAT T1 ";
        $query .= "     LEFT JOIN NAME_MST L1 ON L1.NAMECD1 = 'H303' AND L1.NAMECD2 = T1.DETAILCD ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO     = '{$model->schregno}' AND ";
        $query .= "     DETAIL_DIV   = '1' AND ";
        $query .= "     FISCALYEAR(DETAIL_SDATE) = '{$model->exp_year}' ";
        $query .= " ORDER BY ";
        $query .= "     DETAIL_SDATE ";

        return $query;
    }

    //罰データ取得
    public function getBatsu($model)
    {
        $query  = "";
        $query .= " SELECT ";
        $query .= "     DETAIL_SDATE, ";
        $query .= "     DETAILCD, ";
        $query .= "     L1.NAME1 AS DETAILCDNAME, ";
        $query .= "     CONTENT AS CONTENT, ";
        $query .= "     REMARK ";
        $query .= " FROM ";
        $query .= "     SCHREG_DETAILHIST_DAT T1 ";
        $query .= "     LEFT JOIN NAME_MST L1 ON L1.NAMECD1 = 'H304' AND L1.NAMECD2 = T1.DETAILCD ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO     = '{$model->schregno}' AND ";
        $query .= "     DETAIL_DIV   = '2' AND ";
        $query .= "     FISCALYEAR(DETAIL_SDATE) = '{$model->exp_year}' ";
        $query .= " ORDER BY ";
        $query .= "     DETAIL_SDATE ";

        return $query;
    }
    //定型文得点取得
    public function getHtrainremarkScoreDat($model, $dataDiv)
    {
        $query .= " SELECT ";
        $query .= "     T2.REMARK ";
        $query .= " FROM ";
        $query .= "     HTRAINREMARK_SCORE_DAT T1 ";
        $query .= " LEFT JOIN ";
        $query .= "     HTRAINREMARK_TEMP_SCORE_MST T2 ";
        $query .= "     ON T1.YEAR = T2.YEAR ";
        $query .= "     AND T1.DATA_DIV = T2.DATA_DIV ";
        $query .= "     AND T2.FROM_SCORE <= T1.SCORE  ";
        $query .= "     AND T2.TO_SCORE >= T1.SCORE ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '{$model->exp_year}' ";
        $query .= "     AND T1.SCHREGNO = '{$model->schregno}' ";
        $query .= "     AND T1.DATA_DIV = '{$dataDiv}' ";
        $query .= "     AND T2.GRADE = '{$model->grade}' ";

        return $query;
    }
    //調査書所見データ
    public function selectSyoken($model)
    {
        $query  = " SELECT ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T1.YEAR, ";
        $query .= "     T1.ANNUAL, ";
        $query .= "     T1.SPECIALACTREC, ";
        $query .= "     T1.CLUBACT ";
        $query .= " FROM ";
        $query .= "     HEXAM_ENTREMARK_DAT T1 ";
        $query .= " INNER JOIN ( ";
        $query .= "     SELECT ";
        $query .= "         SUB1.ANNUAL, ";
        $query .= "         MAX(SUB1.YEAR) AS YEAR ";
        $query .= "     FROM ";
        $query .= "         HEXAM_ENTREMARK_DAT SUB1 ";
        $query .= "     INNER JOIN ( ";
        $query .= "         SELECT DISTINCT ";
        $query .= "             ANNUAL ";
        $query .= "         FROM ";
        $query .= "             HEXAM_ENTREMARK_DAT ";
        $query .= "         WHERE ";
        $query .= "             SCHREGNO = '{$model->schregno}' ";
        $query .= "         ORDER BY ";
        $query .= "             ANNUAL ";
        $query .= "     ) SUB2 ON SUB1.ANNUAL = SUB2.ANNUAL ";
        $query .= "     WHERE ";
        $query .= "         SUB1.SCHREGNO = '{$model->schregno}' ";
        $query .= "     GROUP BY ";
        $query .= "         SUB1.ANNUAL ";
        $query .= " ) T2 ON T1.ANNUAL = T2.ANNUAL AND T1.YEAR = T2.YEAR ";
        $query .= " WHERE ";
        $query .= "     T1.SCHREGNO = '{$model->schregno}' ";

        return $query;
    }
}
