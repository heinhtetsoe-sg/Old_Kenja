<?php

require_once('for_php7.php');
class knjx020query extends Query
{
    public function getSecurityHigh()
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     MENU_HIGH_SECURITY_MST ";
        $query .= " WHERE ";
        $query .= "     PROGRAMID = 'KNJX020' ";
        $query .= "     AND INVALID_FLG = '0' ";

        return $query;
    }

    //校種取得
    public function getSchoolKind($model)
    {
        $query  = " SELECT ";
        $query .= "     ABBV1 AS LABEL, ";
        $query .= "     NAME1 AS VALUE ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR    = '".CTRL_YEAR."' AND ";
        $query .= "     NAMECD1 = 'A023' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "     AND NAME1 IN ('".implode(explode(':', $model->selectSchoolKind), "','")."') ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1") {
            $query .= " AND NAME1 = '".SCHOOLKIND."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";
        
        return $query;
    }

    public function getSchoolCd()
    {
        $query  = " SELECT ";
        $query .= "     NAME2 ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'Z010' ";
        $query .= "     AND NAMECD2 = '00' ";

        return $query;
    }

    //マスタのSQL文作成(存在チェック用)
    public function getMasterSQL($table, $where)
    {
        $query = "SELECT * FROM " . $table . " " . $where;

        return $query;
    }

    //データ出力用ＳＱＬ
    public function getMainSQL($model, $schregno = "")
    {
        $query  = " WITH NAMECD AS ( ";
        $query .= "     SELECT ";
        $query .= "         T1.SCHREGNO, ";
        $query .= "         MAX(NAMECD2) AS NAMECD2 ";
        $query .= "     FROM ";
        $query .= "         SCHREG_ENT_GRD_HIST_DAT T1, ";
        $query .= "         NAME_MST T2 ";
        $query .= "     WHERE ";
        $query .= "         T2.NAMECD1 = 'A023' AND ";
        $query .= "         T1.SCHOOL_KIND = T2.NAME1 ";
        if ($schregno) {
            $query .= "         AND T1.SCHREGNO = '".$schregno."' ";
        }
        $query .= "     GROUP BY ";
        $query .= "         T1.SCHREGNO ";
        $query .= " ), SCHOOL_KIND AS ( ";
        $query .= "     SELECT ";
        $query .= "         T3.* ";
        $query .= "     FROM ";
        $query .= "         NAMECD T1, ";
        $query .= "         NAME_MST T2, ";
        $query .= "         SCHREG_ENT_GRD_HIST_DAT T3 ";
        $query .= "     WHERE ";
        $query .= "         T2.NAMECD1 = 'A023' AND ";
        $query .= "         T1.NAMECD2 = T2.NAMECD2 AND ";
        $query .= "         T2.NAME1 = T3.SCHOOL_KIND AND ";
        $query .= "         T1.SCHREGNO = T3.SCHREGNO ";
        $query .= " ) ";

        $query .= " SELECT ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T1.INOUTCD, ";
        $query .= "     T1.NAME, ";
        $query .= "     T1.NAME_SHOW, ";
        $query .= "     T1.NAME_KANA, ";
        $query .= "     T1.NAME_ENG, ";
        $query .= "     T1.REAL_NAME, ";
        $query .= "     T1.REAL_NAME_KANA, ";
        $query .= "     T1.BIRTHDAY, ";
        $query .= "     T1.SEX, ";
        $query .= "     T1.BLOODTYPE, ";
        $query .= "     T1.BLOOD_RH, ";
        $query .= "     T1.HANDICAP, ";
        $query .= "     T1.NATIONALITY, ";
        $query .= "     T2.SCHOOL_KIND, ";
        //出身
        if ($model->Properties["Origin_hyouji"] == '1') {
            $query .= "     D016.BASE_REMARK1 AS ORIGIN, ";
        }
        $query .= "     T2.FINSCHOOLCD, ";
        $query .= "     T2.FINISH_DATE, ";
        $query .= "     T1.PRISCHOOLCD, ";
        $query .= "     D010.BASE_REMARK1 AS PRISCHOOL_CLASS_CD, ";
        $query .= "     T2.CURRICULUM_YEAR, ";
        $query .= "     T2.ENT_DATE, ";
        $query .= "     T2.ENT_DIV, ";
        $query .= "     T2.ENT_REASON, ";
        $query .= "     T2.ENT_SCHOOL, ";
        $query .= "     T2.ENT_ADDR, ";
        if ($model->Properties["useAddrField2"] == "1") {
            $query .= "     T2.ENT_ADDR2, ";
        }
        $query .= "     T2.GRD_DATE, ";
        $query .= "     T2.GRD_DIV, ";
        $query .= "     T2.GRD_REASON, ";
        $query .= "     T2.GRD_SCHOOL, ";
        $query .= "     T2.GRD_ADDR, ";
        if ($model->Properties["useAddrField2"] == "1") {
            $query .= "     T2.GRD_ADDR2, ";
        }
        $query .= "     T2.GRD_NO, ";
        $query .= "     T2.GRD_TERM, ";
        $query .= "     T1.REMARK1, ";
        $query .= "     T1.REMARK2, ";
        $query .= "     T1.REMARK3, ";
        $query .= "     T1.EMERGENCYCALL, ";
        $query .= "     T1.EMERGENCYNAME, ";
        $query .= "     T1.EMERGENCYRELA_NAME, ";
        $query .= "     T1.EMERGENCYTELNO, ";
        $query .= "     T4.BASE_REMARK3 AS E_TELNO_MEMO, ";
        $query .= "     T4.BASE_REMARK1 AS EMERGENCYTELNO_2, ";
        $query .= "     T4.BASE_REMARK4 AS E_TELNO_MEMO_2, ";
        $query .= "     T1.EMERGENCYCALL2, ";
        $query .= "     T1.EMERGENCYNAME2, ";
        $query .= "     T1.EMERGENCYRELA_NAME2, ";
        $query .= "     T1.EMERGENCYTELNO2, ";
        $query .= "     T4.BASE_REMARK5 AS E_TELNO_MEMO2, ";
        $query .= "     T4.BASE_REMARK2 AS EMERGENCYTELNO2_2, ";
        $query .= "     T4.BASE_REMARK6 AS E_TELNO_MEMO2_2, ";
        $query .= "     T2.TENGAKU_SAKI_ZENJITU, ";
        $query .= "     T2.NYUGAKUMAE_SYUSSIN_JOUHOU, ";
        $query .= "     T3.BASE_REMARK1 AS EXAMNO, ";
        $query .= "     '".$model->lastColumn."' AS ".$model->lastColumn." ";
        $query .= " FROM ";
        $query .= "     SCHREG_BASE_MST T1 ";
        $query .= "     LEFT JOIN SCHOOL_KIND T2 ON T1.SCHREGNO = T2.SCHREGNO ";
        $query .= "     LEFT JOIN SCHREG_BASE_DETAIL_MST T3 ON T3.SCHREGNO = T1.SCHREGNO AND T3.BASE_SEQ = '003' ";
        $query .= "     LEFT JOIN SCHREG_BASE_DETAIL_MST T4 ON T4.SCHREGNO = T1.SCHREGNO AND T4.BASE_SEQ = '012' ";
        $query .= "     LEFT JOIN SCHREG_BASE_DETAIL_MST D010 ON D010.SCHREGNO = T1.SCHREGNO AND D010.BASE_SEQ = '010' ";
        //出身
        if ($model->Properties["Origin_hyouji"] == '1') {
            $query .= "     LEFT JOIN SCHREG_BASE_DETAIL_MST D016 ON D016.SCHREGNO = T1.SCHREGNO AND D016.BASE_SEQ = '016' ";
        }
        if ($schregno) {
            $query .= " WHERE ";
            $query .= "     T1.SCHREGNO = '".$schregno."' ";
            if ($model->Properties["use_prg_schoolkind"] == "1") {
                if ($model->selectSchoolKind) {
                    $query .= " AND T2.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind), "','")."') ";
                }
            } elseif ($model->Properties["useSchool_KindField"] == "1") {
                $query .= " AND T2.SCHOOL_KIND = '".SCHOOLKIND."' ";
            }
        } else {
            $whereFlg = "";
            if ($model->field["SCHREGNO_FROM"] && $model->field["SCHREGNO_TO"]) {
                $query .= " WHERE ";
                $query .= "     T1.SCHREGNO BETWEEN '".$model->field["SCHREGNO_FROM"]."' AND '".$model->field["SCHREGNO_TO"]."' ";
                $whereFlg = 1;
            } elseif ($model->field["SCHREGNO_FROM"] || $model->field["SCHREGNO_TO"]) {
                $schno = ($model->field["SCHREGNO_FROM"]) ? $model->field["SCHREGNO_FROM"] : $model->field["SCHREGNO_TO"];
                $query .= " WHERE ";
                $query .= "     T1.SCHREGNO = '".$schno."' ";
                $whereFlg = 1;
            }
            if ($model->Properties["use_prg_schoolkind"] == "1") {
                if ($model->selectSchoolKind) {
                    $query .= ($whereFlg) ? " AND " : " WHERE ";
                    $query .= "     T2.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind), "','")."') ";
                }
            } elseif ($model->Properties["useSchool_KindField"] == "1") {
                $query .= ($whereFlg) ? " AND " : " WHERE ";
                $query .= "     T2.SCHOOL_KIND = '".SCHOOLKIND."' ";
            }
        }
        $query .= " ORDER BY ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "     T2.SCHOOL_KIND, ";
            }
        }
        $query .= "     T1.SCHREGNO ";

        return $query;
    }

    //マスタデータの削除
    public function deleteQueryCsv(&$db, $data, $model)
    {
        $query  = "DELETE FROM schreg_base_mst where SCHREGNO = '".$data["SCHREGNO"]."'";
        $db->query($query);

        $query  = "DELETE FROM SCHREG_BASE_DETAIL_MST where SCHREGNO = '".$data["SCHREGNO"]."' AND BASE_SEQ = '001'";
        $db->query($query);

        $query  = "DELETE FROM SCHREG_BASE_DETAIL_MST where SCHREGNO = '".$data["SCHREGNO"]."' AND BASE_SEQ = '002'";
        $db->query($query);

        if ($model->Properties["Origin_hyouji"] == '1') {
            $query = "DELETE FROM SCHREG_BASE_DETAIL_MST WHERE SCHREGNO = '".$data["SCHREGNO"]."' AND BASE_SEQ = '016'";
            $db->query($query);
        }
    }

    //入学卒業履歴データの削除
    public function deleteHistQueryCsv(&$db, $data)
    {
        $query  = "DELETE FROM SCHREG_ENT_GRD_HIST_DAT WHERE SCHREGNO = '".$data["SCHREGNO"]."'";

        $result = $db->query($query);
    }

    //マスタの追加（ＣＳＶデータより読込）
    public function insertQueryCsv(&$db, $data, $registercd, $hist_data, $model)
    {
        $data1["SCHREGNO           "][TEXT]         = $data["SCHREGNO"];
        $data1["INOUTCD            "][TEXT]         = $data["INOUTCD"];
        $data1["NAME               "][TEXT]         = $data["NAME"];
        $data1["NAME_SHOW          "][TEXT]         = $data["NAME_SHOW"];
        $data1["NAME_KANA          "][TEXT]         = $data["NAME_KANA"];
        $data1["NAME_ENG           "][TEXT]         = $data["NAME_ENG"];
        $data1["REAL_NAME          "][TEXT]         = $data["REAL_NAME"];
        $data1["REAL_NAME_KANA     "][TEXT]         = $data["REAL_NAME_KANA"];
        $data1["BIRTHDAY           "][TEXT]         = $data["BIRTHDAY"];
        $data1["SEX                "][TEXT]         = $data["SEX"];
        $data1["BLOODTYPE          "][TEXT]         = $data["BLOODTYPE"];
        $data1["BLOOD_RH           "][TEXT]         = $data["BLOOD_RH"];
        $data1["HANDICAP           "][TEXT]         = $data["HANDICAP"];
        $data1["NATIONALITY        "][TEXT]         = $data["NATIONALITY"];
        $data1["FINSCHOOLCD        "][TEXT]         = $hist_data["FINSCHOOLCD"];
        $data1["FINISH_DATE        "][TEXT]         = $hist_data["FINISH_DATE"];
        $data1["PRISCHOOLCD        "][TEXT]         = $data["PRISCHOOLCD"];
        $data1["ENT_DATE           "][TEXT]         = $hist_data["ENT_DATE"];
        $data1["ENT_DIV            "][TEXT]         = $hist_data["ENT_DIV"];
        $data1["ENT_REASON         "][TEXT]         = $hist_data["ENT_REASON"];
        $data1["ENT_SCHOOL         "][TEXT]         = $hist_data["ENT_SCHOOL"];
        $data1["ENT_ADDR           "][TEXT]         = $hist_data["ENT_ADDR"];
        if ($model->Properties["useAddrField2"] == "1") {
            $data1["ENT_ADDR2          "][TEXT]         = $hist_data["ENT_ADDR2"];
        }
        $data1["GRD_DATE           "][TEXT]         = $hist_data["GRD_DATE"];
        $data1["GRD_DIV            "][TEXT]         = $hist_data["GRD_DIV"];
        $data1["GRD_REASON         "][TEXT]         = $hist_data["GRD_REASON"];
        $data1["GRD_SCHOOL         "][TEXT]         = $hist_data["GRD_SCHOOL"];
        $data1["GRD_ADDR           "][TEXT]         = $hist_data["GRD_ADDR"];
        if ($model->Properties["useAddrField2"] == "1") {
            $data1["GRD_ADDR2          "][TEXT]         = $hist_data["GRD_ADDR2"];
        }
        $data1["GRD_NO             "][TEXT]         = $hist_data["GRD_NO"];
        $data1["GRD_TERM           "][TEXT]         = $hist_data["GRD_TERM"];
        $data1["REMARK1            "][TEXT]         = $data["REMARK1"];
        $data1["REMARK2            "][TEXT]         = $data["REMARK2"];
        $data1["REMARK3            "][TEXT]         = $data["REMARK3"];
        $data1["EMERGENCYCALL      "][TEXT]         = $data["EMERGENCYCALL"];
        $data1["EMERGENCYNAME      "][TEXT]         = $data["EMERGENCYNAME"];
        $data1["EMERGENCYRELA_NAME "][TEXT]         = $data["EMERGENCYRELA_NAME"];
        $data1["EMERGENCYTELNO     "][TEXT]         = $data["EMERGENCYTELNO"];
        $data1["EMERGENCYCALL2     "][TEXT]         = $data["EMERGENCYCALL2"];
        $data1["EMERGENCYNAME2     "][TEXT]         = $data["EMERGENCYNAME2"];
        $data1["EMERGENCYRELA_NAME2"][TEXT]         = $data["EMERGENCYRELA_NAME2"];
        $data1["EMERGENCYTELNO2    "][TEXT]         = $data["EMERGENCYTELNO2"];

        $data1["REGISTERCD"][TEXT]          = $registercd;
        $data1["UPDATED"][NUMBER]           = "sysdate()";

        $query = Query::insertSQL($data1, "schreg_base_mst");

        $result = $db->query($query);
    }

    //マスタの更新（ＣＳＶデータより読込）
    public function updateQueryCsv(&$db, $data, $registercd, $hist_data, $model)
    {
        $data1["SCHREGNO           "][TEXT]         = $data["SCHREGNO"];
        $data1["INOUTCD            "][TEXT]         = $data["INOUTCD"];
        $data1["NAME               "][TEXT]         = $data["NAME"];
        $data1["NAME_SHOW          "][TEXT]         = $data["NAME_SHOW"];
        $data1["NAME_KANA          "][TEXT]         = $data["NAME_KANA"];
        $data1["NAME_ENG           "][TEXT]         = $data["NAME_ENG"];
        $data1["REAL_NAME          "][TEXT]         = $data["REAL_NAME"];
        $data1["REAL_NAME_KANA     "][TEXT]         = $data["REAL_NAME_KANA"];
        $data1["BIRTHDAY           "][TEXT]         = $data["BIRTHDAY"];
        $data1["SEX                "][TEXT]         = $data["SEX"];
        $data1["BLOODTYPE          "][TEXT]         = $data["BLOODTYPE"];
        $data1["BLOOD_RH           "][TEXT]         = $data["BLOOD_RH"];
        $data1["HANDICAP           "][TEXT]         = $data["HANDICAP"];
        $data1["NATIONALITY        "][TEXT]         = $data["NATIONALITY"];
        $data1["FINSCHOOLCD        "][TEXT]         = $hist_data["FINSCHOOLCD"];
        $data1["FINISH_DATE        "][TEXT]         = $hist_data["FINISH_DATE"];
        $data1["PRISCHOOLCD        "][TEXT]         = $data["PRISCHOOLCD"];
        $data1["ENT_DATE           "][TEXT]         = $hist_data["ENT_DATE"];
        $data1["ENT_DIV            "][TEXT]         = $hist_data["ENT_DIV"];
        $data1["ENT_REASON         "][TEXT]         = $hist_data["ENT_REASON"];
        $data1["ENT_SCHOOL         "][TEXT]         = $hist_data["ENT_SCHOOL"];
        $data1["ENT_ADDR           "][TEXT]         = $hist_data["ENT_ADDR"];
        if ($model->Properties["useAddrField2"] == "1") {
            $data1["ENT_ADDR2          "][TEXT]         = $hist_data["ENT_ADDR2"];
        }
        $data1["GRD_DATE           "][TEXT]         = $hist_data["GRD_DATE"];
        $data1["GRD_DIV            "][TEXT]         = $hist_data["GRD_DIV"];
        $data1["GRD_REASON         "][TEXT]         = $hist_data["GRD_REASON"];
        $data1["GRD_SCHOOL         "][TEXT]         = $hist_data["GRD_SCHOOL"];
        $data1["GRD_ADDR           "][TEXT]         = $hist_data["GRD_ADDR"];
        if ($model->Properties["useAddrField2"] == "1") {
            $data1["GRD_ADDR2          "][TEXT]         = $hist_data["GRD_ADDR2"];
        }
        $data1["GRD_NO             "][TEXT]         = $hist_data["GRD_NO"];
        $data1["GRD_TERM           "][TEXT]         = $hist_data["GRD_TERM"];
        $data1["REMARK1            "][TEXT]         = $data["REMARK1"];
        $data1["REMARK2            "][TEXT]         = $data["REMARK2"];
        $data1["REMARK3            "][TEXT]         = $data["REMARK3"];
        $data1["EMERGENCYCALL      "][TEXT]         = $data["EMERGENCYCALL"];
        $data1["EMERGENCYNAME      "][TEXT]         = $data["EMERGENCYNAME"];
        $data1["EMERGENCYRELA_NAME "][TEXT]         = $data["EMERGENCYRELA_NAME"];
        $data1["EMERGENCYTELNO     "][TEXT]         = $data["EMERGENCYTELNO"];
        $data1["EMERGENCYCALL2     "][TEXT]         = $data["EMERGENCYCALL2"];
        $data1["EMERGENCYNAME2     "][TEXT]         = $data["EMERGENCYNAME2"];
        $data1["EMERGENCYRELA_NAME2"][TEXT]         = $data["EMERGENCYRELA_NAME2"];
        $data1["EMERGENCYTELNO2    "][TEXT]         = $data["EMERGENCYTELNO2"];

        $data1["REGISTERCD         "][TEXT]         = $registercd;
        $data1["UPDATED            "][NUMBER]       = "sysdate()";

        $where = " WHERE SCHREGNO = '" .$data["SCHREGNO"] ."' ";
        $query = Query::updateSQL($data1, "schreg_base_mst", $where);

        $db->query($query);
    }

    //入学卒業履歴データ更新
    public function updateHistQueryCsv(&$db, $data, $registercd, $flg, $model)
    {
        //課程入学年度がNULLの場合（入学日付が存在するとき）、入学日付より課程入学年度を算出
        if ($data["ENT_DATE"] != "" && $data["CURRICULUM_YEAR"] == "") {
            $entDateArray = array();
            $entDateArray = preg_split("/-/", $data["ENT_DATE"]);
            $entdateyear  = $entDateArray[0];
            $month = $entDateArray[1];
            if ($month == "01" || $month == "02" || $month == "03") {
                $entdateyear = $entdateyear - 1;
            }
        }
        $data_hist["SCHREGNO"][TEXT]                    = $data["SCHREGNO"];
        $data_hist["SCHOOL_KIND"][TEXT]                 = $data["SCHOOL_KIND"];
        $data_hist["FINSCHOOLCD"][TEXT]                 = $data["FINSCHOOLCD"];
        $data_hist["FINISH_DATE"][TEXT]                 = $data["FINISH_DATE"];
        if ($data["ENT_DATE"] != "" && $data["CURRICULUM_YEAR"] == "") {
            $data_hist["CURRICULUM_YEAR"][TEXT]             = $entdateyear;
        } else {
            $data_hist["CURRICULUM_YEAR"][TEXT]             = $data["CURRICULUM_YEAR"];
        }
        $data_hist["ENT_DATE"][TEXT]                    = $data["ENT_DATE"];
        $data_hist["ENT_DIV"][TEXT]                     = $data["ENT_DIV"];
        $data_hist["ENT_REASON"][TEXT]                  = $data["ENT_REASON"];
        $data_hist["ENT_SCHOOL"][TEXT]                  = $data["ENT_SCHOOL"];
        $data_hist["ENT_ADDR"][TEXT]                    = $data["ENT_ADDR"];
        if ($model->Properties["useAddrField2"] == "1") {
            $data_hist["ENT_ADDR2"][TEXT]                   = $data["ENT_ADDR2"];
        }
        $data_hist["GRD_DATE"][TEXT]                    = $data["GRD_DATE"];
        $data_hist["GRD_DIV"][TEXT]                     = $data["GRD_DIV"];
        $data_hist["GRD_REASON"][TEXT]                  = $data["GRD_REASON"];
        $data_hist["GRD_SCHOOL"][TEXT]                  = $data["GRD_SCHOOL"];
        $data_hist["GRD_ADDR"][TEXT]                    = $data["GRD_ADDR"];
        if ($model->Properties["useAddrField2"] == "1") {
            $data_hist["GRD_ADDR2"][TEXT]                   = $data["GRD_ADDR2"];
        }
        $data_hist["GRD_NO"][TEXT]                      = $data["GRD_NO"];
        $data_hist["GRD_TERM"][TEXT]                    = $data["GRD_TERM"];
        $data_hist["TENGAKU_SAKI_ZENJITU"][DATE]        = $data["TENGAKU_SAKI_ZENJITU"];
        $data_hist["NYUGAKUMAE_SYUSSIN_JOUHOU"][TEXT]   = $data["NYUGAKUMAE_SYUSSIN_JOUHOU"];

        $data_hist["REGISTERCD"][TEXT]      = $registercd;
        $data_hist["UPDATED"][NUMBER]       = "sysdate()";

        if (!$flg) {
            $query = Query::insertSQL($data_hist, "SCHREG_ENT_GRD_HIST_DAT");
        } else {
            $where  = " WHERE SCHREGNO    = '" .$data["SCHREGNO"] ."' AND ";
            $where .= "       SCHOOL_KIND = '" .$data["SCHOOL_KIND"] ."' ";
            $query = Query::updateSQL($data_hist, "SCHREG_ENT_GRD_HIST_DAT", $where);
        }

        $db->query($query);

        $query = "DELETE FROM SCHREG_BASE_DETAIL_MST WHERE SCHREGNO = '".$data["SCHREGNO"]."' AND BASE_SEQ = '001'";
        $db->query($query);

        $dataDetail = array();
        $dataDetail["SCHREGNO"][TEXT]       = $data["SCHREGNO"];
        $dataDetail["BASE_SEQ"][TEXT]       = "001";
        $dataDetail["BASE_REMARK1"][DATE]   = $data["TENGAKU_SAKI_ZENJITU"];
        $dataDetail["REGISTERCD"][TEXT]     = $registercd;
        $dataDetail["UPDATED"][NUMBER]      = "sysdate()";
        $query = Query::insertSQL($dataDetail, "SCHREG_BASE_DETAIL_MST");
        $db->query($query);

        $query = "DELETE FROM SCHREG_BASE_DETAIL_MST WHERE SCHREGNO = '".$data["SCHREGNO"]."' AND BASE_SEQ = '002'";
        $db->query($query);

        $dataDetail = array();
        $dataDetail["SCHREGNO"][TEXT]       = $data["SCHREGNO"];
        $dataDetail["BASE_SEQ"][TEXT]       = "002";
        $dataDetail["BASE_REMARK1"][TEXT]   = $data["NYUGAKUMAE_SYUSSIN_JOUHOU"];
        $dataDetail["REGISTERCD"][TEXT]     = $registercd;
        $dataDetail["UPDATED"][NUMBER]      = "sysdate()";
        $query = Query::insertSQL($dataDetail, "SCHREG_BASE_DETAIL_MST");
        $db->query($query);

        $query = "DELETE FROM SCHREG_BASE_DETAIL_MST WHERE SCHREGNO = '".$data["SCHREGNO"]."' AND BASE_SEQ = '003'";
        $db->query($query);

        $dataDetail = array();
        $dataDetail["SCHREGNO"][TEXT]       = $data["SCHREGNO"];
        $dataDetail["BASE_SEQ"][TEXT]       = "003";
        $dataDetail["BASE_REMARK1"][TEXT]   = $data["EXAMNO"];
        $dataDetail["REGISTERCD"][TEXT]     = $registercd;
        $dataDetail["UPDATED"][NUMBER]      = "sysdate()";
        $query = Query::insertSQL($dataDetail, "SCHREG_BASE_DETAIL_MST");
        $db->query($query);

        //SCHREG_BASE_DETAIL_MST -- 教室コード
        $query = "DELETE FROM SCHREG_BASE_DETAIL_MST WHERE SCHREGNO = '".$data["SCHREGNO"]."' AND BASE_SEQ = '010'";
        $db->query($query);

        $dataDetail = array();
        $dataDetail["SCHREGNO"][TEXT]       = $data["SCHREGNO"];
        $dataDetail["BASE_SEQ"][TEXT]       = "010";
        $dataDetail["BASE_REMARK1"][TEXT]   = $data["PRISCHOOL_CLASS_CD"];
        $dataDetail["REGISTERCD"][TEXT]     = $registercd;
        $dataDetail["UPDATED"][NUMBER]      = "sysdate()";
        $query = Query::insertSQL($dataDetail, "SCHREG_BASE_DETAIL_MST");
        $db->query($query);

        //SCHREG_BASE_DETAIL_MST -- BASE_SEQ : 012
        $query = "DELETE FROM SCHREG_BASE_DETAIL_MST WHERE SCHREGNO = '".$data["SCHREGNO"]."' AND BASE_SEQ = '012'";
        $db->query($query);

        $dataDetail = array();
        $dataDetail["SCHREGNO"][TEXT]       = $data["SCHREGNO"];
        $dataDetail["BASE_SEQ"][TEXT]       = "012";
        $dataDetail["BASE_REMARK1"][TEXT]   = $data["EMERGENCYTELNO_2"];
        $dataDetail["BASE_REMARK2"][TEXT]   = $data["EMERGENCYTELNO2_2"];
        $dataDetail["BASE_REMARK3"][TEXT]   = $data["E_TELNO_MEMO"];
        $dataDetail["BASE_REMARK4"][TEXT]   = $data["E_TELNO_MEMO_2"];
        $dataDetail["BASE_REMARK5"][TEXT]   = $data["E_TELNO_MEMO2"];
        $dataDetail["BASE_REMARK6"][TEXT]   = $data["E_TELNO_MEMO2_2"];
        $dataDetail["REGISTERCD"][TEXT]     = $registercd;
        $dataDetail["UPDATED"][NUMBER]      = "sysdate()";
        $query = Query::insertSQL($dataDetail, "SCHREG_BASE_DETAIL_MST");
        $db->query($query);

        if ($model->Properties["Origin_hyouji"] == '1') {
            //SCHREG_BASE_DETAIL_MST -- BASE_SEQ : 016
            $query = "DELETE FROM SCHREG_BASE_DETAIL_MST WHERE SCHREGNO = '".$data["SCHREGNO"]."' AND BASE_SEQ = '016'";
            $db->query($query);

            $dataDetail = array();
            $dataDetail["SCHREGNO"][TEXT]       = $data["SCHREGNO"];
            $dataDetail["BASE_SEQ"][TEXT]       = "016";
            $dataDetail["BASE_REMARK1"][TEXT]   = $data["ORIGIN"];
            $dataDetail["REGISTERCD"][TEXT]     = $registercd;
            $dataDetail["UPDATED"][NUMBER]      = "sysdate()";
            $query = Query::insertSQL($dataDetail, "SCHREG_BASE_DETAIL_MST");
            $db->query($query);
        }
    }

    //エラーＤＢへの追加
    public function insertQueryErr(&$db, $record_no, $check_error)
    {
        $data1["PROGRAMID"][TEXT] = PROGRAMID;
        $data1["MSGROW"][NUMBER]  = $record_no;
        $data1["MSGREMARK"][TEXT] = $check_error;

        $query = Query::insertSQL($data1, "W_CSVMSG_PRG_DAT");

        $result = $db->query($query);
    }
}
