<?php

require_once('for_php7.php');

class knjf343Query extends Query
{
    //教育委員会判定
    public function z010Abbv1()
    {
        $query = '';
        $query .= " SELECT ABBV1 ";
        $query .= "   FROM NAME_MST ";
        $query .= "  WHERE NAMECD1 = 'Z010' ";
        $query .= "    AND NAMECD2 = '00' ";

        return $query;
    }

    //FIXED_DAT登録用の学校名称取得
    public function getEdboardSchoolName()
    {
        $query = '';
        $query .= " SELECT SCHOOLNAME1 ";
        $query .= "   FROM SCHOOL_MST ";
        $query .= "  WHERE YEAR        = '".CTRL_YEAR."' ";
        $query .= "    AND SCHOOLCD    = '".sprintf("%012d", SCHOOLCD)."' ";
        $query .= "    AND SCHOOL_KIND = '".SCHOOLKIND."' ";

        $db = Query::dbCheckOut();
        $retName = $db->getOne($query);
        Query::dbCheckIn($db);

        return $retName;
    }

    //教育委員会用の学校コード取得
    public function getSchoolMst()
    {
        $db = Query::dbCheckOut();

        //教育委員会判定
        $query = knjf343Query::z010Abbv1();
        $z010Abbv1 = $db->getOne($query);

        $retSchool = "";
        if ($z010Abbv1 == "1" || $z010Abbv1 == "2") {
            //V_SCHOOL_MSTから学校コードを取得
            $query = '';
            $query .= " SELECT KYOUIKU_IINKAI_SCHOOLCD ";
            $query .= "   FROM V_SCHOOL_MST ";
            $query .= "  WHERE YEAR = '".CTRL_YEAR."' ";

            $retSchool = $db->getOne($query);
        } else {
            $retSchool = "000000000000";
        }

        Query::dbCheckIn($db);

        return $retSchool;
    }

    //報告データコンボ(県へ報告した日付のリストを取得)
    public function getFixed($model)
    {
        $query  = "   SELECT FIXED_DATE AS LABEL ";
        $query .= "        , FIXED_DATE AS VALUE ";
        $query .= "     FROM EDBOARD_MEDEXAM_STAT_ECG_FIXED_DAT ";
        $query .= "    WHERE EDBOARD_SCHOOLCD = '".$model->schoolcd."' ";
        $query .= "      AND YEAR             = '".CTRL_YEAR."' ";
        $query .= " GROUP BY FIXED_DATE ";
        $query .= " ORDER BY FIXED_DATE DESC ";

        return $query;
    }

    //県への報告ボタン右の日付(REPORT_ECG_DATテーブルを参照する)
    public function getMaxFixedDate($model)
    {
        $query = '';
        $query .= "   SELECT MAX(EXECUTE_DATE) AS FIXED_DATE ";
        $query .= "     FROM REPORT_ECG_DAT ";
        $query .= "    WHERE EDBOARD_SCHOOLCD = '".$model->schoolcd."' ";
        $query .= "      AND YEAR             = '".CTRL_YEAR."' ";

        return $query;
    }

    //確定する際にFIXED_DATに登録するデータを取得
    public function getDatData($model)
    {
        $query = '';
        $query .= " SELECT EDBOARD_MEDEXAM_STAT_ECG_DAT.* ";
        $query .= "   FROM EDBOARD_MEDEXAM_STAT_ECG_DAT ";
        $query .= "  WHERE EDBOARD_SCHOOLCD = '".$model->schoolcd."' ";
        $query .= "    AND YEAR             = '".CTRL_YEAR."' ";
        return $query;
    }

    //県への報告の際に、県側DBに登録するFIXED_DATデータを取得するためのSQL
    public function getFixedDatData($model)
    {
        $query = '';
        $query .= " SELECT * ";
        $query .= "   FROM EDBOARD_MEDEXAM_STAT_ECG_FIXED_DAT ";
        $query .= "  WHERE EDBOARD_SCHOOLCD = '".$model->schoolcd."' ";
        $query .= "    AND YEAR             = '".CTRL_YEAR."' ";
        $query .= "    AND FIXED_DATE       = '".str_replace("/", "-", $model->fixedData)."' ";

        return $query;
    }

    //データ表示
    //　・再計算時、集計値を取得
    //　・確定日選択時、FIXEDデータを取得
    //　・確定日未選択時、データを取得
    public function readQuery($model)
    {
        $query = '';

        if ($model->cmd == "recalc") {
            //集計値を取得
            $query .= " WITH T_MAIN AS ( ";
            $query .= "     SELECT '".$model->schoolcd."' AS EDBOARD_SCHOOLCD ";
            $query .= "        , SRD.SCHREGNO ";
            $query .= "        , SRD.YEAR ";
            $query .= "        , MDD.HEART_MEDEXAM ";
            $query .= "        , MDD.MANAGEMENT_DIV ";
            $query .= "     FROM ( ";
            $query .= "             SELECT YEAR ";
            $query .= "                  , SCHREGNO ";
            $query .= "               FROM SCHREG_REGD_DAT ";
            $query .= "              WHERE YEAR = '".CTRL_YEAR."' ";
            $query .= "           GROUP BY YEAR ";
            $query .= "                  , SCHREGNO ";
            $query .= "           ) SRD ";
            $query .= "LEFT JOIN MEDEXAM_DET_DAT MDD ";
            $query .= "       ON MDD.YEAR     = SRD.YEAR ";
            $query .= "      AND MDD.SCHREGNO = SRD.SCHREGNO ";
            $query .= "    WHERE SRD.YEAR     = '".CTRL_YEAR."' ";
            $query .= " ) ";
        } else {
            //登録テーブル
            //→既に登録されている値を読み出す
            $query .= " WITH T_MAIN AS ( ";
            $query .= "     SELECT EDBOARD_SCHOOLCD ";
            $query .= "          , YEAR ";
            $query .= "          , TARGET1 ";
            $query .= "          , EXAMINEE1 ";
            $query .= "          , PERCENT1 ";
            $query .= "          , HAVE_REMARK1 ";
            $query .= "          , REMARK1_CNT1 ";
            $query .= "          , REMARK1_CNT2 ";
            $query .= "          , REMARK1_CNT3 ";
            $query .= "          , REMARK1_CNT4 ";
            $query .= "          , HAVE_REMARK_PERCENT ";
            $query .= "          , TARGET2 ";
            $query .= "          , EXAMINEE2 ";
            $query .= "          , PERCENT2 ";
            $query .= "          , REMARK2_CNT1 ";
            $query .= "          , REMARK2_CNT2 ";
            $query .= "          , REMARK2_CNT3 ";
            $query .= "          , REMARK2_CNT4 ";
            $query .= "          , REMARK2_CNT5 ";
            if ($model->fixedData) {
                $query .= "       FROM EDBOARD_MEDEXAM_STAT_ECG_FIXED_DAT ";
            } else {
                $query .= "       FROM EDBOARD_MEDEXAM_STAT_ECG_DAT ";
            }
            $query .= "      WHERE EDBOARD_SCHOOLCD = '".$model->schoolcd."' ";
            $query .= "        AND YEAR             = '".CTRL_YEAR."' ";
            if ($model->fixedData) {
                $query .= "        AND FIXED_DATE       = '".str_replace("/", "-", $model->fixedData)."' ";
            }
            $query .= "     ) ";
        }

        //メイン
        $query .= "   SELECT *";
        $query .= "     FROM T_MAIN T1";
        $query .= " ORDER BY T1.EDBOARD_SCHOOLCD ";

        return $query;
    }

    //更新ボタン押下時
    public function &getUpdateQuery($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //DATテーブルから削除
        $query = '';
        $query .= " DELETE ";
        $query .= "   FROM EDBOARD_MEDEXAM_STAT_ECG_DAT ";
        $query .= "  WHERE EDBOARD_SCHOOLCD = '".$model->schoolcd."'";
        $query .= "    AND YEAR             = '".CTRL_YEAR."'";
        $db->query($query);

        foreach ($model->updField as $key => $val) {
            //DATテーブルに登録
            $data = array();
            $data['EDBOARD_SCHOOLCD'][TEXT] = $model->schoolcd;
            $data['YEAR'][TEXT]             = CTRL_YEAR;
            $data['TARGET1'][NUMBER]        = $val['TARGET1'];
            $data['EXAMINEE1'][NUMBER]      = $val['EXAMINEE1'];
            $data['HAVE_REMARK1'][NUMBER]   = $val['HAVE_REMARK1'];
            $data['REMARK1_CNT1'][NUMBER]   = $val['REMARK1_CNT1'];
            $data['REMARK1_CNT2'][NUMBER]   = $val['REMARK1_CNT2'];
            $data['REMARK1_CNT3'][NUMBER]   = $val['REMARK1_CNT3'];
            $data['REMARK1_CNT4'][NUMBER]   = $val['REMARK1_CNT4'];
            $data['TARGET2'][NUMBER]        = $val['TARGET2'];
            $data['EXAMINEE2'][NUMBER]      = $val['EXAMINEE2'];
            $data['REMARK2_CNT1'][NUMBER]   = $val['REMARK2_CNT1'];
            $data['REMARK2_CNT2'][NUMBER]   = $val['REMARK2_CNT2'];
            $data['REMARK2_CNT3'][NUMBER]   = $val['REMARK2_CNT3'];
            $data['REMARK2_CNT4'][NUMBER]   = $val['REMARK2_CNT4'];
            $data['REMARK2_CNT5'][NUMBER]   = $val['REMARK2_CNT5'];
            $data['REGISTERCD'][TEXT]       = STAFFCD;
            $data['UPDATED'][NUMBER]        = "SYSDATE()";

            //パーセント値は計算した値を登録
            $data['PERCENT1'][NUMBER]            = sprintf('%.1f', ($val['TARGET1']   > 0)? (round($val['EXAMINEE1']    * 100 / (double)$val['TARGET1'], 1)): 0);
            $data['HAVE_REMARK_PERCENT'][NUMBER] = sprintf('%.1f', ($val['EXAMINEE1'] > 0)? (round($val['HAVE_REMARK1'] * 100 / (double)$val['EXAMINEE1'], 1)): 0);
            $data['PERCENT2'][NUMBER]            = sprintf('%.1f', ($val['TARGET2']   > 0)? (round($val['EXAMINEE2']    * 100 / (double)$val['TARGET2'], 1)): 0);

            $query = Query::insertSQL($data, 'EDBOARD_MEDEXAM_STAT_ECG_DAT');
            $db->query($query);
        }

        $db->commit();
        Query::dbCheckIn($db);
        return;
    }

    //確定処理＞確定ボタン押下時
    public function &getFixedUpdateQuery($model)
    {
        $schoolName = knjf343Query::getEdboardSchoolName();//学校名称

        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //同じ確定日のFIXED_DATデータを削除
        $query = '';
        $query .= " DELETE ";
        $query .= "   FROM EDBOARD_MEDEXAM_STAT_ECG_FIXED_DAT ";
        $query .= "  WHERE EDBOARD_SCHOOLCD = '".$model->schoolcd."'";
        $query .= "    AND YEAR             = '".CTRL_YEAR."'";
        $query .= "    AND FIXED_DATE       = '".str_replace("/", "-", $model->fixedDate)."'";
        $db->query($query);

        //FIXED_DATデータを登録
        $query = knjf343Query::getDatData($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $data = array();
            $data['EDBOARD_SCHOOLCD'][TEXT]      = $row['EDBOARD_SCHOOLCD'];
            $data['YEAR'][TEXT]                  = $row['YEAR'];
            $data['FIXED_DATE'][DATE]            = $model->fixedDate;
            $data['SCHOOLNAME'][TEXT]            = $schoolName;
            $data['TARGET1'][NUMBER]             = $row['TARGET1'];
            $data['EXAMINEE1'][NUMBER]           = $row['EXAMINEE1'];
            $data['PERCENT1'][NUMBER]            = $row['PERCENT1'];
            $data['HAVE_REMARK1'][NUMBER]        = $row['HAVE_REMARK1'];
            $data['REMARK1_CNT1'][NUMBER]        = $row['REMARK1_CNT1'];
            $data['REMARK1_CNT2'][NUMBER]        = $row['REMARK1_CNT2'];
            $data['REMARK1_CNT3'][NUMBER]        = $row['REMARK1_CNT3'];
            $data['REMARK1_CNT4'][NUMBER]        = $row['REMARK1_CNT4'];
            $data['HAVE_REMARK_PERCENT'][NUMBER] = $row['HAVE_REMARK_PERCENT'];
            $data['TARGET2'][NUMBER]             = $row['TARGET2'];
            $data['EXAMINEE2'][NUMBER]           = $row['EXAMINEE2'];
            $data['PERCENT2'][NUMBER]            = $row['PERCENT2'];
            $data['REMARK2_CNT1'][NUMBER]        = $row['REMARK2_CNT1'];
            $data['REMARK2_CNT2'][NUMBER]        = $row['REMARK2_CNT2'];
            $data['REMARK2_CNT3'][NUMBER]        = $row['REMARK2_CNT3'];
            $data['REMARK2_CNT4'][NUMBER]        = $row['REMARK2_CNT4'];
            $data['REMARK2_CNT5'][NUMBER]        = $row['REMARK2_CNT5'];
            $data['REGISTERCD'][TEXT]            = STAFFCD;
            $data['UPDATED'][NUMBER]             = "SYSDATE()";
            $query = Query::insertSQL($data, "EDBOARD_MEDEXAM_STAT_ECG_FIXED_DAT");
            $db->query($query);
        }
        $result->free();

        $db->commit();
        Query::dbCheckIn($db);
        return;
    }

    //県への報告ボタン押下時
    public function &getUpdateReport($model)
    {
        $db = Query::dbCheckOut();
        $db2 = Query::dbCheckOut2();
        $db->autoCommit(false);
        $db2->autoCommit(false);

        //県側、学校側から報告日を一旦削除(REPORT_ECG_DATテーブル参照)
        $query  = "DELETE FROM REPORT_ECG_DAT ";
        $query .= " WHERE EDBOARD_SCHOOLCD = '".$model->schoolcd."'";
        $query .= "   AND YEAR             = '".CTRL_YEAR."'";
        $query .= "   AND EXECUTE_DATE     = '".str_replace("/", "-", $model->execute_date)."' ";
        $db->query($query);
        $db2->query($query);

        //県側、学校側に報告日を再登録(REPORT_ECG_DATテーブル参照)
        $data = array();
        $data["EDBOARD_SCHOOLCD"][TEXT] = $model->schoolcd;
        $data["YEAR"][TEXT]             = CTRL_YEAR;
        $data["EXECUTE_DATE"][DATE]     = $model->execute_date;
        $data["FIXED_DATE"][DATE]       = $model->fixedData;
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][NUMBER]        = "SYSDATE()";
        $query = Query::insertSQL($data, "REPORT_ECG_DAT");
        $db->query($query);
        $db2->query($query);

        //県側のFIXED_DATから削除する
        $query = '';
        $query .= " DELETE";
        $query .= "   FROM EDBOARD_MEDEXAM_STAT_ECG_FIXED_DAT ";
        $query .= "  WHERE EDBOARD_SCHOOLCD = '".$model->schoolcd."'";
        $query .= "    AND YEAR             = '".CTRL_YEAR."'";
        $query .= "    AND FIXED_DATE       = '".str_replace("/", "-", $model->fixedData)."' ";
        $db2->query($query);

        //学校側のFIXED_DATの内容を県側のFIXED_DATにコピーする
        $query = knjf343Query::getFixedDatData($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $data = array();
            $data['EDBOARD_SCHOOLCD'][TEXT]      = $row['EDBOARD_SCHOOLCD'];
            $data['YEAR'][TEXT]                  = $row['YEAR'];
            $data['FIXED_DATE'][DATE]            = $row['FIXED_DATE'];
            $data['SCHOOLNAME'][TEXT]            = $row['SCHOOLNAME'];
            $data['TARGET1'][NUMBER]             = $row['TARGET1'];
            $data['EXAMINEE1'][NUMBER]           = $row['EXAMINEE1'];
            $data['PERCENT1'][NUMBER]            = $row['PERCENT1'];
            $data['HAVE_REMARK1'][NUMBER]        = $row['HAVE_REMARK1'];
            $data['REMARK1_CNT1'][NUMBER]        = $row['REMARK1_CNT1'];
            $data['REMARK1_CNT2'][NUMBER]        = $row['REMARK1_CNT2'];
            $data['REMARK1_CNT3'][NUMBER]        = $row['REMARK1_CNT3'];
            $data['REMARK1_CNT4'][NUMBER]        = $row['REMARK1_CNT4'];
            $data['HAVE_REMARK_PERCENT'][NUMBER] = $row['HAVE_REMARK_PERCENT'];
            $data['TARGET2'][NUMBER]             = $row['TARGET2'];
            $data['EXAMINEE2'][NUMBER]           = $row['EXAMINEE2'];
            $data['PERCENT2'][NUMBER]            = $row['PERCENT2'];
            $data['REMARK2_CNT1'][NUMBER]        = $row['REMARK2_CNT1'];
            $data['REMARK2_CNT2'][NUMBER]        = $row['REMARK2_CNT2'];
            $data['REMARK2_CNT3'][NUMBER]        = $row['REMARK2_CNT3'];
            $data['REMARK2_CNT4'][NUMBER]        = $row['REMARK2_CNT4'];
            $data['REMARK2_CNT5'][NUMBER]        = $row['REMARK2_CNT5'];
            $data['REGISTERCD'][TEXT]            = STAFFCD;
            $data['UPDATED'][NUMBER]             = "SYSDATE()";
            $query = Query::insertSQL($data, "EDBOARD_MEDEXAM_STAT_ECG_FIXED_DAT");
            $db2->query($query);
        }
        $result->free();

        $db->commit();
        $db2->commit();
        Query::dbCheckIn($db);
        Query::dbCheckIn($db2);
        return;
    }

    //CSVデータ取得SQL
    //確定日が選択されている場合はFIXED_DATテーブル、
    //そうでない場合はDATテーブルから取得
    public function getCsvData($model)
    {
        $query = '';

        $query .= " WITH T_MAIN AS ( ";
        if ($model->fixedData) {
            $query .= "     SELECT SCHOOLNAME ";
        } else {
            $query .= "     SELECT '' AS SCHOOLNAME ";//学校名称は学校マスタから別途取得する
        }
        $query .= "          , TARGET1 ";
        $query .= "          , EXAMINEE1 ";
        $query .= "          , PERCENT1 ";
        $query .= "          , HAVE_REMARK1 ";
        $query .= "          , REMARK1_CNT1 ";
        $query .= "          , REMARK1_CNT2 ";
        $query .= "          , REMARK1_CNT3 ";
        $query .= "          , REMARK1_CNT4 ";
        $query .= "          , HAVE_REMARK_PERCENT ";
        $query .= "          , TARGET2 ";
        $query .= "          , EXAMINEE2 ";
        $query .= "          , PERCENT2 ";
        $query .= "          , REMARK2_CNT1 ";
        $query .= "          , REMARK2_CNT2 ";
        $query .= "          , REMARK2_CNT3 ";
        $query .= "          , REMARK2_CNT4 ";
        $query .= "          , REMARK2_CNT5 ";
        $query .= "          , EDBOARD_SCHOOLCD ";
        if ($model->fixedData) {
            $query .= "       FROM EDBOARD_MEDEXAM_STAT_ECG_FIXED_DAT ";
        } else {
            $query .= "       FROM EDBOARD_MEDEXAM_STAT_ECG_DAT ";
        }
        $query .= "      WHERE EDBOARD_SCHOOLCD = '".$model->schoolcd."' ";
        $query .= "        AND YEAR             = '".CTRL_YEAR."' ";
        if ($model->fixedData) {
            $query .= "        AND FIXED_DATE       = '".str_replace("/", "-", $model->fixedData)."' ";
        }
        $query .= "     ) ";

        //メイン
        $query .= "   SELECT *";
        $query .= "     FROM T_MAIN T1";
        $query .= " ORDER BY T1.EDBOARD_SCHOOLCD ";

        return $query;
    }
}
