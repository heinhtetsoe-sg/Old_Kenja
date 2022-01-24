<?php

require_once('for_php7.php');

class knjf341Query extends Query
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
        $query = knjf341Query::z010Abbv1();
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
        $query .= "     FROM EDBOARD_MEDEXAM_STAT_URINALYSIS1_FIXED_DAT ";
        $query .= "    WHERE EDBOARD_SCHOOLCD = '".$model->schoolcd."' ";
        $query .= "      AND YEAR             = '".CTRL_YEAR."' ";
        $query .= " GROUP BY FIXED_DATE ";
        $query .= " ORDER BY FIXED_DATE DESC ";

        return $query;
    }

    //県への報告ボタン右の日付(REPORT_URINALYSIS1_DATテーブルを参照する)
    public function getMaxFixedDate($model)
    {
        $query = '';
        $query .= "   SELECT MAX(EXECUTE_DATE) AS FIXED_DATE ";
        $query .= "     FROM REPORT_URINALYSIS1_DAT ";
        $query .= "    WHERE EDBOARD_SCHOOLCD = '".$model->schoolcd."' ";
        $query .= "      AND YEAR             = '".CTRL_YEAR."' ";

        return $query;
    }

    //確定する際にFIXED_DATに登録するデータを取得
    public function getDatData($model)
    {
        $query = '';
        $query .= " SELECT EDBOARD_MEDEXAM_STAT_URINALYSIS1_DAT.* ";
        $query .= "   FROM EDBOARD_MEDEXAM_STAT_URINALYSIS1_DAT ";
        $query .= "  WHERE EDBOARD_SCHOOLCD = '".$model->schoolcd."' ";
        $query .= "    AND YEAR             = '".CTRL_YEAR."' ";
        return $query;
    }

    //県への報告の際に、県側DBに登録するFIXED_DATデータを取得するためのSQL
    public function getFixedDatData($model)
    {
        $query = '';
        $query .= " SELECT * ";
        $query .= "   FROM EDBOARD_MEDEXAM_STAT_URINALYSIS1_FIXED_DAT ";
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
        $query  = '';

        if ($model->cmd == "recalc") {
            //集計値を取得
            $query .= " WITH T_MAIN AS ( ";
            $query .= "     SELECT '".$model->schoolcd."' AS EDBOARD_SCHOOLCD ";
            $query .= "          , SRD.SCHREGNO ";
            $query .= "          , SRD.YEAR ";
            $query .= "          , MDD.ALBUMINURIA1CD ";
            $query .= "          , MDD.URICSUGAR1CD ";
            $query .= "          , MDD.URICBLEED1CD ";
            $query .= "          , MDD.ALBUMINURIA2CD ";
            $query .= "          , MDD.URICSUGAR2CD ";
            $query .= "          , MDD.URICBLEED2CD ";
            $query .= "          , M007.DET_REMARK2 ";
            $query .= "       FROM ( ";
            $query .= "              SELECT YEAR ";
            $query .= "                   , SCHREGNO ";
            $query .= "                FROM SCHREG_REGD_DAT ";
            $query .= "               WHERE YEAR = '".CTRL_YEAR."' ";
            $query .= "            GROUP BY YEAR ";
            $query .= "                   , SCHREGNO ";
            $query .= "             ) SRD ";
            $query .= "  LEFT JOIN MEDEXAM_DET_DAT MDD ";
            $query .= "         ON MDD.YEAR      = SRD.YEAR ";
            $query .= "        AND MDD.SCHREGNO  = SRD.SCHREGNO ";
            $query .= "  LEFT JOIN MEDEXAM_DET_DETAIL_DAT M007 ";
            $query .= "         ON M007.YEAR     = SRD.YEAR ";
            $query .= "        AND M007.SCHREGNO = SRD.SCHREGNO ";
            $query .= "        AND M007.DET_SEQ  = '007' ";
            $query .= "      WHERE SRD.YEAR      = '".CTRL_YEAR."' ";
            $query .= " ) ";
        } else {
            //登録テーブル
            //→既に登録されている値を読み出す
            $query .= " WITH T_MAIN AS ( ";
            $query .= "     SELECT EDBOARD_SCHOOLCD ";
            $query .= "          , YEAR ";
            $query .= "          , TARGETS1 ";
            $query .= "          , EXAMINEE1 ";
            $query .= "          , PERCENT1 ";
            $query .= "          , REMARK1 ";
            $query .= "          , URICSUGAR1 ";
            $query .= "          , ALBUMINURIA1 ";
            $query .= "          , URICBLEED1 ";
            $query .= "          , TARGETS2 ";
            $query .= "          , EXAMINEE2 ";
            $query .= "          , PERCENT2 ";
            $query .= "          , TARGETS3 ";
            $query .= "          , EXAMINEE3 ";
            $query .= "          , NORMAL ";
            $query .= "          , CAREFUL ";
            $query .= "          , TREATMENT ";
            $query .= "          , PERCENT3 ";
            if ($model->fixedData) {
                $query .= "       FROM EDBOARD_MEDEXAM_STAT_URINALYSIS1_FIXED_DAT ";
            } else {
                $query .= "       FROM EDBOARD_MEDEXAM_STAT_URINALYSIS1_DAT ";
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
        $query .= "   FROM EDBOARD_MEDEXAM_STAT_URINALYSIS1_DAT ";
        $query .= "  WHERE EDBOARD_SCHOOLCD = '".$model->schoolcd."'";
        $query .= "    AND YEAR             = '".CTRL_YEAR."'";
        $db->query($query);

        foreach ($model->updField as $key => $val) {
            //DATテーブルに登録
            $data = array();
            $data['EDBOARD_SCHOOLCD'][TEXT] = $model->schoolcd;
            $data['YEAR'][TEXT]             = CTRL_YEAR;
            $data['TARGETS1'][NUMBER]       = $val['TARGETS1'];
            $data['EXAMINEE1'][NUMBER]      = $val['EXAMINEE1'];
            $data['REMARK1'][NUMBER]        = $val['REMARK1'];
            $data['URICSUGAR1'][NUMBER]     = $val['URICSUGAR1'];
            $data['ALBUMINURIA1'][NUMBER]   = $val['ALBUMINURIA1'];
            $data['URICBLEED1'][NUMBER]     = $val['URICBLEED1'];
            $data['TARGETS2'][NUMBER]       = $val['TARGETS2'];
            $data['EXAMINEE2'][NUMBER]      = $val['EXAMINEE2'];
            $data['TARGETS3'][NUMBER]       = $val['TARGETS3'];
            $data['EXAMINEE3'][NUMBER]      = $val['EXAMINEE3'];
            $data['NORMAL'][NUMBER]         = $val['NORMAL'];
            $data['CAREFUL'][NUMBER]        = $val['CAREFUL'];
            $data['TREATMENT'][NUMBER]      = $val['TREATMENT'];
            $data['REGISTERCD'][TEXT]       = STAFFCD;
            $data['UPDATED'][NUMBER]        = "SYSDATE()";
            //パーセント値は計算値を登録
            $data['PERCENT1'][NUMBER]       = sprintf('%.1f', ($val['TARGETS1'] > 0)? (round($val['EXAMINEE1'] * 100 / (double)$val['TARGETS1'], 1)): 0);
            $data['PERCENT2'][NUMBER]       = sprintf('%.1f', ($val['TARGETS2'] > 0)? (round($val['EXAMINEE2'] * 100 / (double)$val['TARGETS2'], 1)): 0);
            $data['PERCENT3'][NUMBER]       = sprintf('%.1f', ($val['TARGETS3'] > 0)? (round($val['EXAMINEE3'] * 100 / (double)$val['TARGETS3'], 1)): 0);

            $query = Query::insertSQL($data, 'EDBOARD_MEDEXAM_STAT_URINALYSIS1_DAT');
            $db->query($query);
        }

        $db->commit();
        Query::dbCheckIn($db);
        return;
    }

    //確定処理＞確定ボタン押下時
    public function &getFixedUpdateQuery($model)
    {
        $schoolName = knjf341Query::getEdboardSchoolName();//学校名称

        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //同じ確定日のFIXED_DATデータを削除
        $query = '';
        $query .= " DELETE ";
        $query .= "   FROM EDBOARD_MEDEXAM_STAT_URINALYSIS1_FIXED_DAT ";
        $query .= "  WHERE EDBOARD_SCHOOLCD = '".$model->schoolcd."'";
        $query .= "    AND YEAR             = '".CTRL_YEAR."'";
        $query .= "    AND FIXED_DATE       = '".str_replace("/", "-", $model->fixedDate)."'";
        $db->query($query);

        //FIXED_DATデータを登録
        $query = knjf341Query::getDatData($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $data = array();
            $data['EDBOARD_SCHOOLCD'][TEXT] = $row['EDBOARD_SCHOOLCD'];
            $data['YEAR'][TEXT]             = $row['YEAR'];
            $data['FIXED_DATE'][DATE]       = $model->fixedDate;
            $data['SCHOOLNAME'][TEXT]       = $schoolName;
            $data['TARGETS1'][NUMBER]       = $row['TARGETS1'];
            $data['EXAMINEE1'][NUMBER]      = $row['EXAMINEE1'];
            $data['PERCENT1'][NUMBER]       = $row['PERCENT1'];
            $data['REMARK1'][NUMBER]        = $row['REMARK1'];
            $data['URICSUGAR1'][NUMBER]     = $row['URICSUGAR1'];
            $data['ALBUMINURIA1'][NUMBER]   = $row['ALBUMINURIA1'];
            $data['URICBLEED1'][NUMBER]     = $row['URICBLEED1'];
            $data['TARGETS2'][NUMBER]       = $row['TARGETS2'];
            $data['EXAMINEE2'][NUMBER]      = $row['EXAMINEE2'];
            $data['PERCENT2'][NUMBER]       = $row['PERCENT2'];
            $data['TARGETS3'][NUMBER]       = $row['TARGETS3'];
            $data['EXAMINEE3'][NUMBER]      = $row['EXAMINEE3'];
            $data['NORMAL'][NUMBER]         = $row['NORMAL'];
            $data['CAREFUL'][NUMBER]        = $row['CAREFUL'];
            $data['TREATMENT'][NUMBER]      = $row['TREATMENT'];
            $data['PERCENT3'][NUMBER]       = $row['PERCENT3'];
            $data['REGISTERCD'][TEXT]       = STAFFCD;
            $data['UPDATED'][NUMBER]        = "SYSDATE()";
            $query = Query::insertSQL($data, "EDBOARD_MEDEXAM_STAT_URINALYSIS1_FIXED_DAT");
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

        //県側、学校側から報告日を一旦削除(REPORT_URINALYSIS1_DATテーブル参照)
        $query  = "DELETE FROM REPORT_URINALYSIS1_DAT ";
        $query .= " WHERE EDBOARD_SCHOOLCD = '".$model->schoolcd."'";
        $query .= "   AND YEAR             = '".CTRL_YEAR."'";
        $query .= "   AND EXECUTE_DATE     = '".str_replace("/", "-", $model->execute_date)."' ";
        $db->query($query);
        $db2->query($query);

        //県側、学校側に報告日を再登録(REPORT_URINALYSIS1_DATテーブル参照)
        $data = array();
        $data["EDBOARD_SCHOOLCD"][TEXT] = $model->schoolcd;
        $data["YEAR"][TEXT]             = CTRL_YEAR;
        $data["EXECUTE_DATE"][DATE]     = $model->execute_date;
        $data["FIXED_DATE"][DATE]       = $model->fixedData;
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][NUMBER]        = "SYSDATE()";
        $query = Query::insertSQL($data, "REPORT_URINALYSIS1_DAT");
        $db->query($query);
        $db2->query($query);

        //県側のFIXED_DATから削除する
        $query = '';
        $query .= " DELETE";
        $query .= "   FROM EDBOARD_MEDEXAM_STAT_URINALYSIS1_FIXED_DAT ";
        $query .= "  WHERE EDBOARD_SCHOOLCD = '".$model->schoolcd."'";
        $query .= "    AND YEAR             = '".CTRL_YEAR."'";
        $query .= "    AND FIXED_DATE       = '".str_replace("/", "-", $model->fixedData)."' ";
        $db2->query($query);

        //学校側のFIXED_DATの内容を県側のFIXED_DATにコピーする
        $query = knjf341Query::getFixedDatData($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $data = array();
            $data['EDBOARD_SCHOOLCD'][TEXT] = $row['EDBOARD_SCHOOLCD'];
            $data['YEAR'][TEXT]             = $row['YEAR'];
            $data['FIXED_DATE'][DATE]       = $row['FIXED_DATE'];
            $data['SCHOOLNAME'][TEXT]       = $row['SCHOOLNAME'];
            $data['TARGETS1'][NUMBER]       = $row['TARGETS1'];
            $data['EXAMINEE1'][NUMBER]      = $row['EXAMINEE1'];
            $data['PERCENT1'][NUMBER]       = $row['PERCENT1'];
            $data['REMARK1'][NUMBER]        = $row['REMARK1'];
            $data['URICSUGAR1'][NUMBER]     = $row['URICSUGAR1'];
            $data['ALBUMINURIA1'][NUMBER]   = $row['ALBUMINURIA1'];
            $data['URICBLEED1'][NUMBER]     = $row['URICBLEED1'];
            $data['TARGETS2'][NUMBER]       = $row['TARGETS2'];
            $data['EXAMINEE2'][NUMBER]      = $row['EXAMINEE2'];
            $data['PERCENT2'][NUMBER]       = $row['PERCENT2'];
            $data['TARGETS3'][NUMBER]       = $row['TARGETS3'];
            $data['EXAMINEE3'][NUMBER]      = $row['EXAMINEE3'];
            $data['NORMAL'][NUMBER]         = $row['NORMAL'];
            $data['CAREFUL'][NUMBER]        = $row['CAREFUL'];
            $data['TREATMENT'][NUMBER]      = $row['TREATMENT'];
            $data['PERCENT3'][NUMBER]       = $row['PERCENT3'];
            $data['REGISTERCD'][TEXT]       = STAFFCD;
            $data['UPDATED'][NUMBER]        = "SYSDATE()";
            $query = Query::insertSQL($data, "EDBOARD_MEDEXAM_STAT_URINALYSIS1_FIXED_DAT");
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
        $query .= "          , TARGETS1 ";
        $query .= "          , EXAMINEE1 ";
        $query .= "          , PERCENT1 ";
        $query .= "          , REMARK1 ";
        $query .= "          , URICSUGAR1 ";
        $query .= "          , ALBUMINURIA1 ";
        $query .= "          , URICBLEED1 ";
        $query .= "          , TARGETS2 ";
        $query .= "          , EXAMINEE2 ";
        $query .= "          , PERCENT2 ";
        $query .= "          , TARGETS3 ";
        $query .= "          , EXAMINEE3 ";
        $query .= "          , NORMAL ";
        $query .= "          , CAREFUL ";
        $query .= "          , TREATMENT ";
        $query .= "          , PERCENT3 ";
        $query .= "          , EDBOARD_SCHOOLCD ";
        if ($model->fixedData) {
            $query .= "       FROM EDBOARD_MEDEXAM_STAT_URINALYSIS1_FIXED_DAT ";
        } else {
            $query .= "       FROM EDBOARD_MEDEXAM_STAT_URINALYSIS1_DAT ";
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
