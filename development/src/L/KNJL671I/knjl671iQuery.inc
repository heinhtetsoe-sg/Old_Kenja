<?php

require_once('for_php7.php');

class knjl671iQuery extends Query
{

    //名称マスタ
    public function getNameMst($model, $namecd1, $namecd2 = "")
    {
        $query  = " SELECT ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR    = '".$model->year."' ";
        $query .= " AND NAMECD1 = '".$namecd1."' ";
        if ($namecd2) {
            $query .= " AND NAMECD2 = '".$namecd2."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";
        return $query;
    }

    //一覧取得
    public function getList($model)
    {
        $query  = " SELECT ";
        $query .= "     ADVICE.*, ";
        $query .= "     VALUE(L001.NAME1||' ', '') || FIN.FINSCHOOL_NAME AS FINSCHOOL_NAME ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_RECRUIT_ADVICE_DAT ADVICE ";
        $query .= "     LEFT JOIN FINSCHOOL_MST FIN ";
        $query .= "            ON FIN.FINSCHOOLCD = ADVICE.FS_CD ";
        $query .= "     LEFT JOIN NAME_MST L001 ";
        $query .= "            ON L001.NAMECD1 = 'L001' ";
        $query .= "           AND L001.NAMECD2 = FIN.FINSCHOOL_DISTCD ";
        $query .= " WHERE  ";
        $query .= "     ADVICE.ENTEXAMYEAR = '".$model->year."' ";
        if ($model->cmd == "listSearch") {
            if ($model->finschoolcd) {
                $query .= "     AND ADVICE.FS_CD = '".$model->finschoolcd."' ";
            }
            if ($model->name) {
                $query .= "     AND ADVICE.NAME LIKE '%".$model->name."%' ";
            }
        }
        $query .= " ORDER BY ";
        if ($model->sort == "2") {
            $query .= "     ADVICE.NAME, ";
        } elseif ($model->sort == "3") {
            $query .= "     ADVICE.FS_CD, ";
        }
        $query .= "     ADVICE.EXAMNO ";
        return $query;
    }

    //１レコード取得
    public function getSelectQuery($model)
    {
        $query  = " SELECT ";
        $query .= "     ADVICE.EXAMNO, ";
        $query .= "     ADVICE.STAFFCD, ";
        $query .= "     ADVICE.FS_CD AS FINSCHOOLCD, ";
        $query .= "     ADVICE.FS_AREA_CD AS FINSCHOOL_DISTCD, ";
        $query .= "     ADVICE.NAME, ";
        for ($seq = 1; $seq <= $model->maxSeq; $seq++) {
            $setSeq = sprintf("%03d", $seq);
            $table = "AD".$setSeq;
            $query .= "     ".$table.".REMARK1 AS SEQ".$setSeq."_REMARK1, ";
            $query .= "     ".$table.".REMARK2 AS SEQ".$setSeq."_REMARK2, ";
        }
        $query .= "     ADVICE.EXCLUSION, ";
        $query .= "     sysdate() AS CHANGE_DATE ";
        $query .= " FROM  ";
        $query .= "     ENTEXAM_RECRUIT_ADVICE_DAT ADVICE ";
        for ($seq = 1; $seq <= $model->maxSeq; $seq++) {
            $setSeq = sprintf("%03d", $seq);
            $table = "AD".$setSeq;
            $query .= "     LEFT JOIN ENTEXAM_RECRUIT_ADVICE_DETAIL_DAT ".$table;
            $query .= "            ON ".$table.".ENTEXAMYEAR  = ADVICE.ENTEXAMYEAR ";
            $query .= "           AND ".$table.".EXAMNO       = ADVICE.EXAMNO ";
            $query .= "           AND ".$table.".SEQ          = '".$setSeq."' ";
        }
        $query .= " WHERE  ";
        $query .= "     ADVICE.ENTEXAMYEAR  = '".$model->year."' ";
        $query .= " AND ADVICE.EXAMNO       = '".$model->examno."' ";
        return $query;
    }

    //変更履歴の取得
    public function getHistQuery($model)
    {
        $query  = " SELECT ";
        $query .= "     HIST.* ";
        $query .= "     , VARCHAR_FORMAT(CHANGE_DATE, 'yyyy/MM/dd hh:mm') AS FORMAT_CHANGE_DATE ";
        $query .= "     , CLIENT.STAFFNAME AS CLIENT_STAFFNAME ";
        $query .= "     , EDIT.STAFFNAME AS EDIT_STAFFNAME ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_RECRUIT_ADVICE_DAT ADVICE ";
        $query .= "     LEFT JOIN ENTEXAM_RECRUIT_ADVICE_HIST_DAT HIST ";
        $query .= "            ON HIST.ENTEXAMYEAR  = ADVICE.ENTEXAMYEAR ";
        $query .= "           AND HIST.EXAMNO       = ADVICE.EXAMNO ";
        $query .= "     LEFT JOIN STAFF_MST CLIENT ";
        $query .= "            ON CLIENT.STAFFCD = HIST.CLIENT_STAFFCD ";
        $query .= "     LEFT JOIN STAFF_MST EDIT ";
        $query .= "            ON EDIT.STAFFCD = HIST.EDIT_STAFFCD ";
        $query .= " WHERE ";
        $query .= "     ADVICE.ENTEXAMYEAR  = '".$model->year."' ";
        $query .= " AND ADVICE.EXAMNO       = '".$model->examno."' ";
        $query .= " ORDER BY CHANGE_DATE DESC";
        return $query;
    }

    //ENTEXAM_SETTING_MSTより取得
    public function getEntexamSettingMst($model, $namecd1, $seq = "")
    {
        $query  = " SELECT ";
        $query .= "     SEQ AS VALUE, ";
        $query .= "     SEQ || ':' || NAME1 AS LABEL, ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_SETTING_MST ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR  = '".$model->year."' ";
        $query .= " AND APPLICANTDIV = '".$model->applicantdiv."' ";
        $query .= " AND SETTING_CD   = '".$namecd1."' ";
        if ($seq) {
            $query .= " AND SEQ = '".$seq."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";
        return $query;
    }

    //入試区分マスタの取得
    public function getEntexamTestdivMst($model, $testdiv = "")
    {
        $query  = " SELECT ";
        $query .= "     T1.TESTDIV AS VALUE, ";
        $query .= "     T1.TESTDIV || ':' || T1.TESTDIV_NAME AS LABEL, ";
        $query .= "     T1.* ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_TESTDIV_MST T1 ";
        $query .= " WHERE ";
        $query .= "       T1.ENTEXAMYEAR  = '{$model->year}' ";
        $query .= "   AND T1.APPLICANTDIV = '{$model->applicantdiv}' ";
        if ($testdiv) {
            $query .= "   AND T1.TESTDIV = '{$testdiv}' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";
        return $query;
    }

    //コース・特待理由・特待コードの取得
    public function getEntexamGeneralMst($model, $generalDiv, $generalCd = "")
    {
        $query  = " SELECT ";
        $query .= "     T1.GENERAL_CD AS VALUE, ";
        $query .= "     T1.GENERAL_CD || ':' || T1.GENERAL_NAME AS LABEL, ";
        $query .= "     T1.* ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_GENERAL_MST T1 ";
        $query .= " WHERE ";
        $query .= "       T1.ENTEXAMYEAR  = '{$model->year}' ";
        $query .= "   AND T1.APPLICANTDIV = '{$model->applicantdiv}' ";
        $query .= "   AND T1.TESTDIV      = '0' ";
        $query .= "   AND T1.GENERAL_DIV  = '{$generalDiv}' "; //02:コース, 04:特待コード, 05:特待理由
        if ($generalCd) {
            $query .= "   AND T1.GENERAL_CD = '{$generalCd}' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";
        return $query;
    }

    //部活動マスタの取得
    public function getClubMst($clubcd = "")
    {
        $query  = " SELECT ";
        $query .= "     T1.CLUBCD AS VALUE, ";
        $query .= "     T1.CLUBCD || ':' || T1.CLUBNAME AS LABEL, ";
        $query .= "     T1.* ";
        $query .= " FROM ";
        $query .= "     CLUB_MST T1 ";
        if ($clubcd) {
            $query .= " WHERE ";
            $query .= "       T1.CLUBCD = '{$clubcd}' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";
        return $query;
    }

    //出身学校マスタよりデータを取得
    public function getFinschoolName($cd = "")
    {
        $query  = " SELECT ";
        $query .= "         T1.*, ";
        $query .= "         VALUE(L001.NAME1, '') AS FINSCHOOL_DISTCD_NAME, ";
        $query .= "         VALUE(STAFF.STAFFNAME, '') AS STAFFNAME, ";
        $query .= "         T1.FINSCHOOLCD AS VALUE, ";
        $query .= "         T1.FINSCHOOLCD || ':' || VALUE(L001.NAME1||' ', '') || FINSCHOOL_NAME AS LABEL ";
        $query .= "   FROM  FINSCHOOL_MST T1 ";
        $query .= "         LEFT JOIN NAME_MST L001 ";
        $query .= "                ON L001.NAMECD1 = 'L001' ";
        $query .= "               AND L001.NAMECD2 = T1.FINSCHOOL_DISTCD ";
        $query .= "         LEFT JOIN STAFF_MST STAFF ";
        $query .= "                ON  STAFF.STAFFCD = T1.FINSCHOOL_STAFFCD ";
        if ($cd) {
            $query .= "  WHERE  T1.FINSCHOOLCD = '".$cd."' ";
        }
        $query .= "  ORDER BY T1.FINSCHOOLCD ";
        return $query;
    }

    //塾マスタよりデータを取得
    public function getPriSchoolName($cd)
    {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     PRISCHOOL_MST ";
        $query .= "  WHERE  PRISCHOOLCD = '".$cd."' ";
        return $query;
    }


    //担任マスタよりデータを取得
    public function getStaffMst($staffcd = "")
    {
        $query  = " SELECT ";
        $query .= "         *, ";
        $query .= "         STAFFCD AS VALUE, ";
        $query .= "         STAFFCD || ':' || STAFFNAME AS LABEL ";
        $query .= "   FROM  STAFF_MST ";
        if ($staffcd) {
            $query .= "  WHERE  STAFFCD = '".$staffcd."' ";
        }
        $query .= "  ORDER BY STAFFCD ";
        return $query;
    }

    //UPDATE
    public function &getUpdateQuery($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $model->examno = sprintf("%04d", $model->examno);
        $model->field["FINSCHOOLCD"] = (strlen($model->field["FINSCHOOLCD"])) ? sprintf("%07s", $model->field["FINSCHOOLCD"]) : "";
        $fsArray = $db->getRow(knjl671iQuery::getFinschoolName($model->field["FINSCHOOLCD"]), DB_FETCHMODE_ASSOC);

        if ($model->sendCmd == "insert" || $model->sendCmd == "updateAll") {
            $data = array();
            $data["STAFFCD"][TEXT]          = $fsArray["FINSCHOOL_STAFFCD"];
            $data["FS_CD"][TEXT]            = $model->field["FINSCHOOLCD"];
            $data["FS_AREA_CD"][TEXT]       = $fsArray["FINSCHOOL_DISTCD"];
            $data["NAME"][TEXT]             = $model->field["NAME"];
            $data["EXCLUSION"][TEXT]        = $model->field["EXCLUSION"];
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][NUMBER]        = "sysdate()";

            if ($model->sendCmd == "insert") {
                //新規
                $data["ENTEXAMYEAR"][TEXT]      = $model->year;
                $data["EXAMNO"][TEXT]           = $model->examno;
                $query = Query::insertSQL($data, "ENTEXAM_RECRUIT_ADVICE_DAT");
            } else {
                //更新
                $where  = " WHERE ";
                $where .= "     ENTEXAMYEAR  = '".$model->year."' ";
                $where .= " AND EXAMNO       = '".$model->examno."' ";
                $query = Query::updateSQL($data, "ENTEXAM_RECRUIT_ADVICE_DAT", $where);
            }
            $db->query($query);
        }

        //更新 ENTEXAM_RECRUIT_ADVICE_DETAIL_DAT
        knjl671iQuery::getUpdateEntexamApplicantbaseDetailDat($db, $model);

        //更新 ENTEXAM_RECRUIT_ADVICE_HIST_DAT
        knjl671iQuery::getUpdateEEntexamRecruitAdviceHistDat($db, $model);

        $db->commit();
        Query::dbCheckIn($db);
        return true;
    }

    //UPDATE -- ENTEXAM_RECRUIT_ADVICE_DETAIL_DAT
    public function &getUpdateEntexamApplicantbaseDetailDat($db, $model)
    {
        for ($seq = 1; $seq <= $model->maxSeq; $seq++) {
            //更新対象項目のチェック
            if ($seq == 4 || $seq == 8) {
                //複数項目
                $key1 = "SEQ".sprintf("%03d", $seq)."_REMARK1";
                $key2 = "SEQ".sprintf("%03d", $seq)."_REMARK2";
                if (!array_key_exists($key1, $model->updItemArray) || !array_key_exists($key2, $model->updItemArray)) {
                    continue; //更新対象外の項目が1つ以上存在する場合、処理しない
                }
            } else {
                //単一項目
                $key = "SEQ".sprintf("%03d", $seq)."_REMARK1";
                if (!array_key_exists($key, $model->updItemArray)) {
                    continue; //更新対象外の項目は処理しない
                }
            }

            //更新対象項目の処理
            $setSeq = sprintf("%03d", $seq);
            $where  = " WHERE ";
            $where .= "     ENTEXAMYEAR  = '".$model->year."' ";
            $where .= " AND EXAMNO       = '".$model->examno."' ";
            $where .= " AND SEQ          = '".$setSeq."' ";

            //件数取得
            $cnt = $db->getOne("SELECT COUNT(*) AS COUNT FROM ENTEXAM_RECRUIT_ADVICE_DETAIL_DAT ".$where);

            //更新 -- ENTEXAM_RECRUIT_ADVICE_DETAIL_DAT
            $fieldSeq = "SEQ".$setSeq;
            $data = array();
            $data["REMARK1"][TEXT]  = $model->field[$fieldSeq."_REMARK1"];
            if ($fieldSeq == "SEQ004" || $fieldSeq == "SEQ008") {
                $data["REMARK2"][TEXT]  = $model->field[$fieldSeq."_REMARK2"];
            }
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][NUMBER]        = "sysdate()";

            if ($cnt == 0) {
                //新規
                $data["ENTEXAMYEAR"][TEXT]      = $model->year;
                $data["EXAMNO"][TEXT]           = $model->examno;
                $data["SEQ"][TEXT]              = $setSeq;
                $query = Query::insertSQL($data, "ENTEXAM_RECRUIT_ADVICE_DETAIL_DAT");
            } else {
                //更新
                $query = Query::updateSQL($data, "ENTEXAM_RECRUIT_ADVICE_DETAIL_DAT", $where);
            }
            $db->query($query);
        }
    }

    //UPDATE -- ENTEXAM_RECRUIT_ADVICE_HIST_DAT
    public function &getUpdateEEntexamRecruitAdviceHistDat($db, $model)
    {

        //変更履歴の入力確認
        $histUpdFlg = false;
        foreach ($model->histField as $key => $val) {
            if ($val && $key != "CHANGE_DATE") {
                $histUpdFlg = true; //入力されている
            }
        }

        //変更履歴が1項目以上入力されている場合、履歴データ作成
        if ($histUpdFlg) {
            //新規 -- ENTEXAM_RECRUIT_ADVICE_HIST_DAT
            $data = array();
            $data["ENTEXAMYEAR"][TEXT]          = $model->year;
            $data["EXAMNO"][TEXT]               = $model->examno;
            $data["CHANGE_DATE"][TEXT]          = $model->histField["CHANGE_DATE"];
            $data["NAME_FLG"][TEXT]             = $model->histField["NAME_FLG"];
            $data["TESTDIV_FLG"][TEXT]          = $model->histField["TESTDIV_FLG"];
            $data["COURSE_FLG"][TEXT]           = $model->histField["COURSE_FLG"];
            $data["STANDARD_EXAM_FLG"][TEXT]    = $model->histField["STANDARD_EXAM_FLG"];
            $data["HONOR_FLG"][TEXT]            = $model->histField["HONOR_FLG"];
            $data["OTHER_FLG"][TEXT]            = $model->histField["OTHER_FLG"];
            $data["CHANGE_TEXT"][TEXT]          = $model->histField["CHANGE_TEXT"];
            $data["CLIENT_STAFFCD"][TEXT]       = $model->histField["CLIENT_STAFFCD"];
            $data["EDIT_STAFFCD"][TEXT]         = $model->histField["EDIT_STAFFCD"];
            $data["REGISTERCD"][TEXT]           = STAFFCD;
            $data["UPDATED"][NUMBER]            = "sysdate()";
            $query = Query::insertSQL($data, "ENTEXAM_RECRUIT_ADVICE_HIST_DAT");
            $db->query($query);
        }
    }

    //DELETE
    public function &getDeleteQuery($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //削除 -- ENTEXAM_RECRUIT_ADVICE_DAT
        $query  = " DELETE FROM ";
        $query .= "     ENTEXAM_RECRUIT_ADVICE_DAT ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR  = '".$model->year."' ";
        $query .= " AND EXAMNO       = '".$model->examno."' ";
        $db->query($query);

        //削除 -- ENTEXAM_RECRUIT_ADVICE_DETAIL_DAT
        $query  = " DELETE FROM ";
        $query .= "     ENTEXAM_RECRUIT_ADVICE_DETAIL_DAT ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR  = '".$model->year."' ";
        $query .= " AND EXAMNO       = '".$model->examno."' ";
        $db->query($query);

        //削除 -- ENTEXAM_RECRUIT_ADVICE_HIST_DAT
        $query  = " DELETE FROM ";
        $query .= "     ENTEXAM_RECRUIT_ADVICE_HIST_DAT ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR  = '".$model->year."' ";
        $query .= " AND EXAMNO       = '".$model->examno."' ";
        $db->query($query);

        $db->commit();
        Query::dbCheckIn($db);

        return true;
    }

    //CSVダウンロードデータの抽出
    public static function getCsvData($model)
    {
        $query = "
            SELECT
                 T1.ENTEXAMYEAR  AS ENTEXAMYEAR
                ,T1.EXAMNO       AS EXAMNO
                ,T1.STAFFCD      AS STAFFCD
                ,T1.FS_CD        AS FS_CD
                ,T1.FS_AREA_CD   AS FS_AREA_CD
                ,T1.NAME         AS NAME
                ,T1.EXCLUSION    AS EXCLUSION
                ,SEQ001.REMARK1  AS SEQ001_REMARK1
                ,SEQ002.REMARK1  AS SEQ002_REMARK1
                ,SEQ003.REMARK1  AS SEQ003_REMARK1
                ,SEQ004.REMARK1  AS SEQ004_REMARK1
                ,SEQ004.REMARK2  AS SEQ004_REMARK2
                ,SEQ006.REMARK1  AS SEQ006_REMARK1
            FROM ENTEXAM_RECRUIT_ADVICE_DAT AS T1
                LEFT JOIN ENTEXAM_RECRUIT_ADVICE_DETAIL_DAT AS SEQ001
                       ON SEQ001.ENTEXAMYEAR = T1.ENTEXAMYEAR
                      AND SEQ001.EXAMNO      = T1.EXAMNO
                      AND SEQ001.SEQ         = '001'
                LEFT JOIN ENTEXAM_RECRUIT_ADVICE_DETAIL_DAT AS SEQ002
                       ON SEQ002.ENTEXAMYEAR = T1.ENTEXAMYEAR
                      AND SEQ002.EXAMNO      = T1.EXAMNO
                      AND SEQ002.SEQ         = '002'
                LEFT JOIN ENTEXAM_RECRUIT_ADVICE_DETAIL_DAT AS SEQ003
                       ON SEQ003.ENTEXAMYEAR = T1.ENTEXAMYEAR
                      AND SEQ003.EXAMNO      = T1.EXAMNO
                      AND SEQ003.SEQ         = '003'
                LEFT JOIN ENTEXAM_RECRUIT_ADVICE_DETAIL_DAT AS SEQ004
                       ON SEQ004.ENTEXAMYEAR = T1.ENTEXAMYEAR
                      AND SEQ004.EXAMNO      = T1.EXAMNO
                      AND SEQ004.SEQ         = '004'
                LEFT JOIN ENTEXAM_RECRUIT_ADVICE_DETAIL_DAT AS SEQ006
                       ON SEQ006.ENTEXAMYEAR = T1.ENTEXAMYEAR
                      AND SEQ006.EXAMNO      = T1.EXAMNO
                      AND SEQ006.SEQ         = '006'
            WHERE T1.ENTEXAMYEAR = '{$model->year}'
            ORDER BY T1.EXAMNO 
        ";

        return $query;
    }

    //CSV取込
    public function insertQueryCsv(&$db, $model, &$data_arr)
    {
        $data = array();
        $db->autoCommit(false);

        $cnt = 0; //処理件数
        for ($i = 0; $i < count($data_arr); $i++) {
            $data_arr[$i]["EXAMNO"] = sprintf("%04d", $data_arr[$i]["EXAMNO"]);
            $data_arr[$i]["FS_CD"] = (strlen($data_arr[$i]["FS_CD"])) ? sprintf("%07s", $data_arr[$i]["FS_CD"]) : "";

            $fsArray = [];
            if ($data_arr[$i]["FS_CD"]) {
                $fsArray = $db->getRow(knjl671iQuery::getFinschoolName($data_arr[$i]["FS_CD"]), DB_FETCHMODE_ASSOC);
            }

            //データセット
            $data = [];
            $data["NAME"][TEXT]         = $data_arr[$i]["NAME"];
            $data["FS_CD"][TEXT]        = $data_arr[$i]["FS_CD"];
            $data["STAFFCD"][TEXT]      = ($fsArray["FINSCHOOL_STAFFCD"] ? $fsArray["FINSCHOOL_STAFFCD"] : '');
            $data["FS_AREA_CD"][TEXT]   = ($fsArray["FINSCHOOL_DISTCD"] ? $fsArray["FINSCHOOL_DISTCD"] : '');
            $data["REGISTERCD"][TEXT]   = STAFFCD;
            $data["UPDATED"][NUMBER]    = "sysdate()";

            $where  = " WHERE ";
            $where .= "     ENTEXAMYEAR  = '".$data_arr[$i]["ENTEXAMYEAR"]."' ";
            $where .= " AND EXAMNO       = '".$data_arr[$i]["EXAMNO"]."' ";

            $getCnt = $db->getOne("SELECT COUNT(*) AS COUNT FROM ENTEXAM_RECRUIT_ADVICE_DAT ".$where);
            if ($getCnt == 0) {
                //登録番号が未登録の場合、追加
                $data["ENTEXAMYEAR"][TEXT]      = $data_arr[$i]["ENTEXAMYEAR"];
                $data["EXAMNO"][TEXT]           = $data_arr[$i]["EXAMNO"];
                $query = Query::insertSQL($data, "ENTEXAM_RECRUIT_ADVICE_DAT");
            } else {
                //更新
                $query = Query::updateSQL($data, "ENTEXAM_RECRUIT_ADVICE_DAT", $where);
            }
            $db->query($query);

            //更新 ENTEXAM_RECRUIT_ADVICE_DETAIL_DAT
            self::getCsvUpdateEntexamRecruitAdviceDetailDat($db, $model, $data_arr[$i]);

            $cnt++;
        }
        $db->commit();

        return $cnt;
    }

    //CSV取込み -- ENTEXAM_RECRUIT_ADVICE_DETAIL_DAT
    private static function getCsvUpdateEntexamRecruitAdviceDetailDat(&$db, $model, $field)
    {
        // SEQ 配列
        $seqs = ['001','002','003','004','006'];

        foreach ($seqs as $i => $seq) {
            $where  = " WHERE ";
            $where .= "     ENTEXAMYEAR  = '" . $field['ENTEXAMYEAR'] ."' ";
            $where .= " AND EXAMNO       = '" . $field['EXAMNO'] ."' ";
            $where .= " AND SEQ          = '${seq}' ";

            // まず削除を行う
            $db->query("DELETE FROM ENTEXAM_RECRUIT_ADVICE_DETAIL_DAT ".$where);

            $data = [];
            if ($field['SEQ'. $seq .'_REMARK1']) {
                $data["REMARK1"][TEXT]          = $field['SEQ'. $seq .'_REMARK1'];
            }
            if ($seq == '004') {
                if ($field['SEQ'. $seq .'_REMARK2']) {
                    $data["REMARK2"][TEXT]      = $field['SEQ'. $seq .'_REMARK2'];
                }
            }
            if (empty($data)) {
                // 登録するデータがない場合は次へ
                continue;
            }
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][NUMBER]        = "sysdate()";

            //追加登録する
            $data["ENTEXAMYEAR"][TEXT]      = $field['ENTEXAMYEAR'];
            $data["EXAMNO"][TEXT]           = $field['EXAMNO'];
            $data["SEQ"][TEXT]              = $seq;
            $query = Query::insertSQL($data, "ENTEXAM_RECRUIT_ADVICE_DETAIL_DAT");
            $db->query($query);
        }
    }

    //CVS作成用のQUERY
    public static function selectCsvMsgPrgDat($model)
    {
        $query  = "SELECT MSGROW, MSGREMARK ";
        $query .= "FROM W_CSVMSG_PRG_DAT ";
        $query .= "WHERE PROGRAMID = '".$model->sendPrgid."' ";
        return $query;
    }

    //エラーデータの削除
    public static function deleteQueryErr($model)
    {
        $query  = " DELETE FROM W_CSVMSG_PRG_DAT WHERE PROGRAMID = '".$model->sendPrgid."' ";
        return $query;
    }

    //エラーＤＢへの追加
    public function insertQueryErr(&$db, $record_no, $check_error, $model)
    {
        $data1 = [];
        $data1["PROGRAMID"][TEXT]   = $model->sendPrgid;
        $data1["MSGROW"][NUMBER]    = $record_no;
        $data1["MSGREMARK"][TEXT]   = $check_error;

        $query = Query::insertSQL($data1, "W_CSVMSG_PRG_DAT");
        $result = $db->query($query);
    }
}
