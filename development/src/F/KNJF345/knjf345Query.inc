<?php

require_once('for_php7.php');

class knjf345Query extends Query
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
        $query = knjf345Query::z010Abbv1();
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
        $query .= "     FROM EDBOARD_MEDEXAM_STAT_ECG2REMARK_FIXED_DAT ";
        $query .= "    WHERE EDBOARD_SCHOOLCD = '".$model->schoolcd."' ";
        $query .= "      AND YEAR             = '".CTRL_YEAR."' ";
        $query .= " GROUP BY FIXED_DATE ";
        $query .= " ORDER BY FIXED_DATE DESC ";

        return $query;
    }

    //県への報告ボタン右の日付(REPORT_DISEASE_KENSIN_DATテーブルを参照する)
    public function getMaxFixedDate($model)
    {
        $query = '';
        $query .= "   SELECT MAX(EXECUTE_DATE) AS FIXED_DATE ";
        $query .= "     FROM REPORT_DISEASE_KENSIN_DAT ";
        $query .= "    WHERE EDBOARD_SCHOOLCD = '".$model->schoolcd."' ";
        $query .= "      AND YEAR             = '".CTRL_YEAR."' ";

        return $query;
    }

    //確定する際にFIXED_DATに登録するデータを取得
    public function getDatData($model)
    {
        $query = '';
        $query .= " SELECT EDBOARD_MEDEXAM_STAT_ECG2REMARK_DAT.* ";
        $query .= "   FROM EDBOARD_MEDEXAM_STAT_ECG2REMARK_DAT ";
        $query .= "  WHERE EDBOARD_SCHOOLCD = '".$model->schoolcd."' ";
        $query .= "    AND YEAR             = '".CTRL_YEAR."' ";
        return $query;
    }

    //県への報告の際に、県側DBに登録するFIXED_DATデータを取得するためのSQL
    public function getFixedDatData($model)
    {
        $query = '';
        $query .= " SELECT * ";
        $query .= "   FROM EDBOARD_MEDEXAM_STAT_ECG2REMARK_FIXED_DAT ";
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

        //出席番号名と学年
        $query .= " WITH GRADE_CLASS_ATTEND AS ( ";
        $query .= "    SELECT SRD.SCHREGNO ";//学籍番号
        $query .= "         , CASE WHEN SRD.GRADE = '01' THEN '1年' ";
        $query .= "                WHEN SRD.GRADE = '02' THEN '2年' ";
        $query .= "                WHEN SRD.GRADE = '03' THEN '3年' ";
        $query .= "                ELSE '' ";
        $query .= "           END ";                             //学年
        $query .= "        || SRH.HR_CLASS_NAME1 ";              //組
        $query .= "        || LTRIM(SRD.ATTENDNO, '0') || '番' ";//番号
        $query .= "           AS ATTEND_NAME ";                  //(学年||組||番号)->出席番号名
        $query .= "         , SRD.GRADE AS GRADECD";//学年コード
        $query .= "         , SRD.HR_CLASS";//クラス
        $query .= "         , SRD.ATTENDNO";//出席番号
        $query .= "      FROM SCHREG_REGD_DAT SRD ";
        $query .= " LEFT JOIN SCHREG_REGD_HDAT SRH ";
        $query .= "        ON (SRH.YEAR     = SRD.YEAR ";
        $query .= "       AND  SRH.SEMESTER = SRD.SEMESTER ";
        $query .= "       AND  SRH.GRADE    = SRD.GRADE ";
        $query .= "       AND  SRH.HR_CLASS = SRD.HR_CLASS) ";
        $query .= "      JOIN SCHREG_REGD_GDAT SRG ";
        $query .= "        ON (SRG.YEAR        = SRD.YEAR ";
        $query .= "       AND  SRG.GRADE       = SRD.GRADE ";
        $query .= "       AND  SRG.SCHOOL_KIND = '".SCHOOLKIND."') ";
        $query .= "     WHERE SRD.YEAR = '".CTRL_YEAR."' ";
        $query .= "       AND NOT EXISTS ( ";//同じ年度で学年が違うデータが存在しうるようなので、最大のもので絞り込む
        $query .= "               SELECT * ";
        $query .= "                 FROM SCHREG_REGD_DAT SRD_TEMP ";
        $query .= "                WHERE SRD_TEMP.YEAR = SRD.YEAR ";
        $query .= "                  AND SRD_TEMP.SCHREGNO = SRD.SCHREGNO ";
        $query .= "                  AND SRD_TEMP.GRADE > SRD.GRADE ";
        $query .= "           ) ";
        $query .= "  GROUP BY SRD.SCHREGNO ";
        $query .= "         , SRD.GRADE ";
        $query .= "         , SRH.HR_CLASS_NAME1 ";
        $query .= "         , SRD.ATTENDNO ";
        $query .= "         , SRD.GRADE ";
        $query .= "         , SRD.HR_CLASS";
        $query .= "         , SRD.ATTENDNO";
        $query .= " ) ";
        if ($model->cmd == "recalc") {
            //生徒ごとの値を取得
            $query .= " ,";
            $query .= " BASE AS ( ";
            $query .= "     SELECT SRD.SCHREGNO ";
            $query .= "          , SRD.YEAR ";
            $query .= "          , SBM.NAME ";
            $query .= "          , NM.NAME1 AS DETAIL_RESULT ";
            $query .= "          , MD.MANAGEMENT_REMARK AS DETAIL_REMARK ";
            $query .= "       FROM ( ";
            $query .= "                 SELECT SCHREGNO ";
            $query .= "                      , YEAR ";
            $query .= "                   FROM SCHREG_REGD_DAT ";
            $query .= "                  WHERE YEAR  = '".CTRL_YEAR."' ";
            $query .= "               GROUP BY SCHREGNO ";
            $query .= "                      , YEAR ";
            $query .= "            ) SRD ";
            $query .= "  LEFT JOIN SCHREG_BASE_MST SBM ";
            $query .= "         ON SBM.SCHREGNO      = SRD.SCHREGNO ";
            $query .= "  LEFT JOIN MEDEXAM_DET_DAT MD ";
            $query .= "         ON MD.YEAR           = SRD.YEAR ";
            $query .= "        AND MD.SCHREGNO       = SRD.SCHREGNO ";
            $query .= "  LEFT JOIN NAME_MST NM ";
            $query .= "         ON NM.NAMECD1        = 'F091' ";
            $query .= "        AND MD.MANAGEMENT_DIV = NM.NAMECD2 ";
            $query .= "      WHERE ( ";
            $query .= "                 MD.MANAGEMENT_DIV IS NOT NULL ";
            $query .= "             AND MD.MANAGEMENT_DIV <> '00' ";
            $query .= "            ) ";
            $query .= " ) ";
            //集計値を取得
            $query .= " ,";
            $query .= " T_MAIN AS ( ";
            $query .= "     SELECT '".$model->schoolcd."' AS EDBOARD_SCHOOLCD ";
            $query .= "          , BASE.YEAR ";
            $query .= "          , BASE.SCHREGNO ";
            $query .= "          , GCA.ATTEND_NAME ";
            $query .= "          , GCA.GRADECD AS GRADE ";
            $query .= "          , BASE.NAME ";
            $query .= "          , BASE.DETAIL_RESULT ";
            $query .= "          , BASE.DETAIL_REMARK ";
            $query .= "          , GCA.HR_CLASS";
            $query .= "          , GCA.ATTENDNO";
            $query .= "       FROM BASE ";
            $query .= "  LEFT JOIN GRADE_CLASS_ATTEND AS GCA ";
            $query .= "         ON GCA.SCHREGNO = BASE.SCHREGNO ";
            $query .= " ) ";
        } else {
            //登録テーブル
            //→既に登録されている値を読み出す
            $query .= " ,";
            $query .= " T_MAIN AS ( ";

            $query .= "      SELECT BASE.EDBOARD_SCHOOLCD ";
            $query .= "           , BASE.YEAR ";
            $query .= "           , BASE.SCHREGNO ";
            $query .= "           , GCA.ATTEND_NAME ";
            $query .= "           , GCA.GRADECD AS GRADE ";
            $query .= "           , BASE.NAME ";
            $query .= "           , BASE.DETAIL_RESULT ";
            $query .= "           , BASE.DETAIL_REMARK ";
            $query .= "           , GCA.HR_CLASS";
            $query .= "           , GCA.ATTENDNO";
            if ($model->fixedData) {
                $query .= "        FROM EDBOARD_MEDEXAM_STAT_ECG2REMARK_FIXED_DAT AS BASE";
            } else {
                $query .= "        FROM EDBOARD_MEDEXAM_STAT_ECG2REMARK_DAT       AS BASE";
            }
            $query .= "  LEFT JOIN GRADE_CLASS_ATTEND AS GCA ";
            $query .= "         ON GCA.SCHREGNO = BASE.SCHREGNO ";
            $query .= "      WHERE BASE.EDBOARD_SCHOOLCD = '".$model->schoolcd."' ";
            $query .= "        AND BASE.YEAR             = '".CTRL_YEAR."' ";
            if ($model->fixedData) {
                $query .= "        AND BASE.FIXED_DATE       = '".str_replace("/", "-", $model->fixedData)."' ";
            }
            $query .= "     ) ";
        }

        //メイン
        $query .= "   SELECT T1.EDBOARD_SCHOOLCD ";
        $query .= "        , T1.YEAR ";
        $query .= "        , T1.SCHREGNO ";
        $query .= "        , T1.ATTEND_NAME ";
        $query .= "        , T1.GRADE ";
        $query .= "        , T1.NAME ";
        $query .= "        , T1.DETAIL_RESULT ";
        $query .= "        , T1.DETAIL_REMARK ";
        $query .= "     FROM T_MAIN T1";
        $query .= " ORDER BY T1.EDBOARD_SCHOOLCD ";
        $query .= "        , T1.GRADE ";
        $query .= "        , T1.HR_CLASS ";
        $query .= "        , T1.ATTENDNO ";

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
        $query .= "   FROM EDBOARD_MEDEXAM_STAT_ECG2REMARK_DAT ";
        $query .= "  WHERE EDBOARD_SCHOOLCD = '".$model->schoolcd."'";
        $query .= "    AND YEAR             = '".CTRL_YEAR."'";
        $db->query($query);

        foreach ($model->updField as $key => $val) {
            //DATテーブルに登録
            $data = array();
            $data['EDBOARD_SCHOOLCD'][TEXT] = $model->schoolcd;
            $data['YEAR'][TEXT]             = CTRL_YEAR;
            $data['SCHREGNO'][TEXT]         = $val['SCHREGNO'];
            $data['GRADE'][TEXT]            = $val['GRADE'];
            $data['NAME'][TEXT]             = $val['NAME'];
            $data['DETAIL_RESULT'][TEXT]    = $val['DETAIL_RESULT'];
            $data['DETAIL_REMARK'][TEXT]    = $val['DETAIL_REMARK'];
            $data['REGISTERCD'][TEXT]       = STAFFCD;
            $data['UPDATED'][NUMBER]        = "SYSDATE()";
            $query = Query::insertSQL($data, 'EDBOARD_MEDEXAM_STAT_ECG2REMARK_DAT');
            $db->query($query);
        }

        $db->commit();
        Query::dbCheckIn($db);
        return;
    }

    //確定処理＞確定ボタン押下時
    public function &getFixedUpdateQuery($model)
    {
        $schoolName = knjf345Query::getEdboardSchoolName();//学校名称

        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //同じ確定日のFIXED_DATデータを削除
        $query = '';
        $query .= " DELETE ";
        $query .= "   FROM EDBOARD_MEDEXAM_STAT_ECG2REMARK_FIXED_DAT ";
        $query .= "  WHERE EDBOARD_SCHOOLCD = '".$model->schoolcd."'";
        $query .= "    AND YEAR             = '".CTRL_YEAR."'";
        $query .= "    AND FIXED_DATE       = '".str_replace("/", "-", $model->fixedDate)."'";
        $db->query($query);

        //FIXED_DATデータを登録
        $query = knjf345Query::getDatData($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $data = array();
            $data['EDBOARD_SCHOOLCD'][TEXT] = $model->schoolcd;
            $data['YEAR'][TEXT]             = CTRL_YEAR;
            $data['FIXED_DATE'][DATE]       = $model->fixedDate;
            $data['SCHOOLNAME'][TEXT]       = $schoolName;
            $data['SCHREGNO'][TEXT]         = $row['SCHREGNO'];
            $data['GRADE'][TEXT]            = $row['GRADE'];
            $data['NAME'][TEXT]             = $row['NAME'];
            $data['DETAIL_RESULT'][TEXT]    = $row['DETAIL_RESULT'];
            $data['DETAIL_REMARK'][TEXT]    = $row['DETAIL_REMARK'];
            $data['REGISTERCD'][TEXT]       = STAFFCD;
            $data['UPDATED'][NUMBER]        = "SYSDATE()";
            $query = Query::insertSQL($data, "EDBOARD_MEDEXAM_STAT_ECG2REMARK_FIXED_DAT");
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

        //県側、学校側から報告日を一旦削除(REPORT_DISEASE_KENSIN_DATテーブル参照)
        $query  = "DELETE FROM REPORT_DISEASE_KENSIN_DAT ";
        $query .= " WHERE EDBOARD_SCHOOLCD = '".$model->schoolcd."'";
        $query .= "   AND YEAR             = '".CTRL_YEAR."'";
        $query .= "   AND EXECUTE_DATE     = '".str_replace("/", "-", $model->execute_date)."' ";
        $db->query($query);
        $db2->query($query);

        //県側、学校側に報告日を再登録(REPORT_DISEASE_KENSIN_DATテーブル参照)
        $data = array();
        $data["EDBOARD_SCHOOLCD"][TEXT]     = $model->schoolcd;
        $data["YEAR"][TEXT]                 = CTRL_YEAR;
        $data["EXECUTE_DATE"][DATE]         = $model->execute_date;
        $data["FIXED_DATE"][DATE]           = $model->fixedData;
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][NUMBER]            = "SYSDATE()";
        $query = Query::insertSQL($data, "REPORT_DISEASE_KENSIN_DAT");
        $db->query($query);
        $db2->query($query);

        //県側のFIXED_DATから削除する
        $query = '';
        $query .= " DELETE";
        $query .= "   FROM EDBOARD_MEDEXAM_STAT_ECG2REMARK_FIXED_DAT ";
        $query .= "  WHERE EDBOARD_SCHOOLCD = '".$model->schoolcd."'";
        $query .= "    AND YEAR             = '".CTRL_YEAR."'";
        $query .= "    AND FIXED_DATE       = '".str_replace("/", "-", $model->fixedData)."' ";
        $db2->query($query);

        //学校側のFIXED_DATの内容を県側のFIXED_DATにコピーする
        $query = knjf345Query::getFixedDatData($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $data = array();
            $data['EDBOARD_SCHOOLCD'][TEXT] = $model->schoolcd;
            $data['YEAR'][TEXT]             = CTRL_YEAR;
            $data['FIXED_DATE'][DATE]       = $row['FIXED_DATE'];
            $data['SCHREGNO'][TEXT]         = $row['SCHREGNO'];
            $data['SCHOOLNAME'][TEXT]       = $row['SCHOOLNAME'];
            $data['GRADE'][TEXT]            = $row['GRADE'];
            $data['NAME'][TEXT]             = $row['NAME'];
            $data['DETAIL_RESULT'][TEXT]    = $row['DETAIL_RESULT'];
            $data['DETAIL_REMARK'][TEXT]    = $row['DETAIL_REMARK'];
            $data['REGISTERCD'][TEXT]       = STAFFCD;
            $data['UPDATED'][NUMBER]        = "SYSDATE()";
            $query = Query::insertSQL($data, "EDBOARD_MEDEXAM_STAT_ECG2REMARK_FIXED_DAT");
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

        //出席番号名と学年
        $query .= " WITH GRADE_CLASS_ATTEND AS ( ";
        $query .= "    SELECT SRD.SCHREGNO ";//学籍番号
        $query .= "         , CASE WHEN SRD.GRADE = '01' THEN '1年' ";
        $query .= "                WHEN SRD.GRADE = '02' THEN '2年' ";
        $query .= "                WHEN SRD.GRADE = '03' THEN '3年' ";
        $query .= "                ELSE '' ";
        $query .= "           END ";                             //学年
        $query .= "        || SRH.HR_CLASS_NAME1 ";              //組
        $query .= "        || LTRIM(SRD.ATTENDNO, '0') || '番' ";//番号
        $query .= "           AS ATTEND_NAME ";                  //(学年||組||番号)->出席番号名
        $query .= "         , SRD.GRADE AS GRADECD";//学年コード
        $query .= "         , SRD.HR_CLASS";//クラス
        $query .= "         , SRD.ATTENDNO";//出席番号
        $query .= "      FROM SCHREG_REGD_DAT SRD ";
        $query .= " LEFT JOIN SCHREG_REGD_HDAT SRH ";
        $query .= "        ON (SRH.YEAR     = SRD.YEAR ";
        $query .= "       AND  SRH.SEMESTER = SRD.SEMESTER ";
        $query .= "       AND  SRH.GRADE    = SRD.GRADE ";
        $query .= "       AND  SRH.HR_CLASS = SRD.HR_CLASS) ";
        $query .= "      JOIN SCHREG_REGD_GDAT SRG ";
        $query .= "        ON (SRG.YEAR        = SRD.YEAR ";
        $query .= "       AND  SRG.GRADE       = SRD.GRADE ";
        $query .= "       AND  SRG.SCHOOL_KIND = '".SCHOOLKIND."') ";
        $query .= "     WHERE SRD.YEAR = '".CTRL_YEAR."' ";
        $query .= "       AND NOT EXISTS ( ";//同じ年度で学年が違うデータが存在しうるようなので、最大のもので絞り込む
        $query .= "               SELECT * ";
        $query .= "                 FROM SCHREG_REGD_DAT SRD_TEMP ";
        $query .= "                WHERE SRD_TEMP.YEAR = SRD.YEAR ";
        $query .= "                  AND SRD_TEMP.SCHREGNO = SRD.SCHREGNO ";
        $query .= "                  AND SRD_TEMP.GRADE > SRD.GRADE ";
        $query .= "           ) ";
        $query .= "  GROUP BY SRD.SCHREGNO ";
        $query .= "         , SRD.GRADE ";
        $query .= "         , SRH.HR_CLASS_NAME1 ";
        $query .= "         , SRD.ATTENDNO ";
        $query .= "         , SRD.GRADE ";
        $query .= "         , SRD.HR_CLASS";
        $query .= "         , SRD.ATTENDNO";
        $query .= " ) ";
        $query .= " , ";
        $query .= " T_MAIN AS ( ";
        if ($model->fixedData) {
            $query .= "     SELECT BASE.SCHOOLNAME ";
        } else {
            $query .= "     SELECT '' AS SCHOOLNAME ";//学校名称は学校マスタから別途取得する
        }
        $query .= "          , BASE.GRADE ";
        $query .= "          , BASE.NAME ";
        $query .= "          , BASE.DETAIL_RESULT ";
        $query .= "          , BASE.DETAIL_REMARK ";
        $query .= "          , BASE.EDBOARD_SCHOOLCD ";
        $query .= "          , BASE.SCHREGNO ";
        $query .= "          , GCA.HR_CLASS ";
        $query .= "          , GCA.ATTENDNO ";
        if ($model->fixedData) {
            $query .= "       FROM EDBOARD_MEDEXAM_STAT_ECG2REMARK_FIXED_DAT AS BASE ";
        } else {
            $query .= "       FROM EDBOARD_MEDEXAM_STAT_ECG2REMARK_DAT       AS BASE ";
        }
        $query .= "  LEFT JOIN GRADE_CLASS_ATTEND AS GCA ";
        $query .= "         ON GCA.SCHREGNO = BASE.SCHREGNO ";
        $query .= "      WHERE BASE.EDBOARD_SCHOOLCD = '".$model->schoolcd."' ";
        $query .= "        AND BASE.YEAR             = '".CTRL_YEAR."' ";
        if ($model->fixedData) {
            $query .= "        AND BASE.FIXED_DATE       = '".str_replace("/", "-", $model->fixedData)."' ";
        }
        $query .= "     ) ";

        //メイン
        $query .= "   SELECT T1.SCHOOLNAME ";
        $query .= "        , T1.GRADE ";
        $query .= "        , T1.NAME ";
        $query .= "        , T1.DETAIL_RESULT ";
        $query .= "        , T1.DETAIL_REMARK ";
        $query .= "        , T1.EDBOARD_SCHOOLCD ";
        $query .= "        , T1.SCHREGNO ";
        $query .= "     FROM T_MAIN T1";
        $query .= " ORDER BY T1.EDBOARD_SCHOOLCD ";
        $query .= "        , T1.GRADE ";
        $query .= "        , T1.HR_CLASS ";
        $query .= "        , T1.ATTENDNO ";

        return $query;
    }
}
