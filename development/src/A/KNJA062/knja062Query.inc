<?php

require_once('for_php7.php');
class knja062Query extends Query
{

    public function getSecurityHigh()
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     MENU_HIGH_SECURITY_MST ";
        $query .= " WHERE ";
        $query .= "     PROGRAMID = 'KNJA062' ";
        $query .= "     AND INVALID_FLG = '0' ";

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

    //職員リストコンボ用取得
    public function getStaff($year, $staffcd)
    {
        $db  = Query::dbCheckOut();

        $query  = " SELECT DISTINCT ";
        $query .= "     STAFFCD, ";
        $query .= "     STAFFNAME_SHOW ";
        $query .= " FROM ";
        $query .= "     STAFF_MST ";
        $query .= " WHERE ";
        $query .= "     STAFFCD = '{$staffcd}' ";

        $staff_mst = $db->getRow($query, DB_FETCHMODE_ASSOC);

        Query::dbCheckIn($db);

        return "{$staff_mst['STAFFCD']}　{$staff_mst['STAFFNAME_SHOW']}";
    }

    //１レコード取得
    public function getRowHdat($term, $ghr_cd)
    {
        $db  = Query::dbCheckOut();

        $query  = " SELECT  ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GHR_HDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR     = '".SUBSTR($term, 0, 4)."' ";
        $query .= " AND SEMESTER = '".SUBSTR($term, 5)."' ";
        $query .= " AND GHR_CD   = '".$ghr_cd."' ";

        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);
        return $row;
    }

    //複式クラス一覧
    public function selectList($term)
    {
        $query  = " SELECT ";
        $query .= "     T1.GHR_CD, ";
        $query .= "     T1.GHR_NAME, ";
        $query .= "     T1.GHR_NAMEABBV, ";
        $query .= "     T1.TR_CD1, ";
        $query .= "     T1.TR_CD2, ";
        $query .= "     T1.TR_CD3, ";
        $query .= "     T1.SUBTR_CD1, ";
        $query .= "     T1.SUBTR_CD2, ";
        $query .= "     T1.SUBTR_CD3, ";
        $query .= "     L1.STAFFNAME_SHOW AS NAME1, ";
        $query .= "     L2.STAFFNAME_SHOW AS NAME2, ";
        $query .= "     L3.STAFFNAME_SHOW AS NAME3, ";
        $query .= "     L4.STAFFNAME_SHOW AS NAME4, ";
        $query .= "     L5.STAFFNAME_SHOW AS NAME5, ";
        $query .= "     L6.STAFFNAME_SHOW AS NAME6 ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GHR_HDAT T1 ";
        $query .= "     LEFT JOIN V_STAFF_MST L1 ON T1.TR_CD1 = L1.STAFFCD AND T1.YEAR = L1.YEAR ";
        $query .= "     LEFT JOIN V_STAFF_MST L2 ON T1.TR_CD2 = L2.STAFFCD AND T1.YEAR = L2.YEAR ";
        $query .= "     LEFT JOIN V_STAFF_MST L3 ON T1.TR_CD3 = L3.STAFFCD AND T1.YEAR = L3.YEAR ";
        $query .= "     LEFT JOIN V_STAFF_MST L4 ON T1.SUBTR_CD1 = L4.STAFFCD AND T1.YEAR = L4.YEAR ";
        $query .= "     LEFT JOIN V_STAFF_MST L5 ON T1.SUBTR_CD2 = L5.STAFFCD AND T1.YEAR = L5.YEAR ";
        $query .= "     LEFT JOIN V_STAFF_MST L6 ON T1.SUBTR_CD3 = L6.STAFFCD AND T1.YEAR = L6.YEAR ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".SUBSTR($term, 0, 4)."' ";
        $query .= "     AND T1.SEMESTER = '".SUBSTR($term, 5)."' ";
        $query .= " ORDER BY ";
        $query .= "     T1.GHR_CD ";

        return $query;
    }

//--------追加
    //学期マスタ取得
    public function getSemesterMst($year, $semester)
    {
        $query  = " SELECT ";
        $query .= "     SEMESTER, ";
        $query .= "     SEMESTERNAME, ";
        $query .= "     SDATE, ";
        $query .= "     EDATE ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR     = '{$year}' ";
        $query .= "     AND SEMESTER = '{$semester}' ";

        return $query;
    }


//----------

    //CSV 書き出し
    public function getCsvData($model)
    {
        $query  = " SELECT ";
        $query .= "     SRH.YEAR, ";
        $query .= "     SRH.SEMESTER, ";
        $query .= "     SRH.GHR_CD, ";
        $query .= "     SRH.GHR_NAME, ";
        $query .= "     SRH.GHR_NAMEABBV, ";
        $query .= "     SRH.TR_CD1, ";
        $query .= "     MAX(T1.FROM_DATE) AS FROM_DATE_T1, ";
        $query .= "     MAX(T1.TO_DATE) AS TO_DATE_T1, ";
        $query .= "     SRH.TR_CD2, ";
        $query .= "     MAX(T2.FROM_DATE) AS FROM_DATE_T2, ";
        $query .= "     MAX(T2.TO_DATE) AS TO_DATE_T2, ";
        $query .= "     SRH.TR_CD3, ";
        $query .= "     MAX(T3.FROM_DATE) AS FROM_DATE_T3, ";
        $query .= "     MAX(T3.TO_DATE) AS TO_DATE_T3, ";
        $query .= "     SRH.SUBTR_CD1, ";
        $query .= "     MAX(S1.FROM_DATE) AS FROM_DATE_S1, ";
        $query .= "     MAX(S1.TO_DATE) AS TO_DATE_S1, ";
        $query .= "     SRH.SUBTR_CD2, ";
        $query .= "     MAX(S2.FROM_DATE) AS FROM_DATE_S2, ";
        $query .= "     MAX(S2.TO_DATE) AS TO_DATE_S2, ";
        $query .= "     SRH.SUBTR_CD3, ";
        $query .= "     MAX(S3.FROM_DATE) AS FROM_DATE_S3, ";
        $query .= "     MAX(S3.TO_DATE) AS TO_DATE_S3, ";
        $query .= "     '".$model->lastColumn."' AS ".$model->lastColumn." ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GHR_HDAT SRH ";
        $query .= " LEFT OUTER JOIN STAFF_CLASS_HIST_GHR_DAT T1 ON SRH.TR_CD1 = T1.STAFFCD AND SRH.YEAR = T1.YEAR AND SRH.SEMESTER = T1.SEMESTER AND SRH.GHR_CD = T1.GHR_CD AND T1.TR_DIV = '1'";
        $query .= " LEFT OUTER JOIN STAFF_CLASS_HIST_GHR_DAT T2 ON SRH.TR_CD2 = T2.STAFFCD AND SRH.YEAR = T2.YEAR AND SRH.SEMESTER = T2.SEMESTER AND SRH.GHR_CD = T2.GHR_CD AND T2.TR_DIV = '2'";
        $query .= " LEFT OUTER JOIN STAFF_CLASS_HIST_GHR_DAT T3 ON SRH.TR_CD3 = T3.STAFFCD AND SRH.YEAR = T3.YEAR AND SRH.SEMESTER = T3.SEMESTER AND SRH.GHR_CD = T3.GHR_CD AND T3.TR_DIV = '3'";
        $query .= " LEFT OUTER JOIN STAFF_CLASS_HIST_GHR_DAT S1 ON SRH.SUBTR_CD1 = S1.STAFFCD AND SRH.YEAR = S1.YEAR AND SRH.SEMESTER = S1.SEMESTER AND SRH.GHR_CD = S1.GHR_CD AND S1.TR_DIV = '4'";
        $query .= " LEFT OUTER JOIN STAFF_CLASS_HIST_GHR_DAT S2 ON SRH.SUBTR_CD2 = S2.STAFFCD AND SRH.YEAR = S2.YEAR AND SRH.SEMESTER = S2.SEMESTER AND SRH.GHR_CD = S2.GHR_CD AND S2.TR_DIV = '5'";
        $query .= " LEFT OUTER JOIN STAFF_CLASS_HIST_GHR_DAT S3 ON SRH.SUBTR_CD3 = S3.STAFFCD AND SRH.YEAR = S3.YEAR AND SRH.SEMESTER = S3.SEMESTER AND SRH.GHR_CD = S3.GHR_CD AND S3.TR_DIV = '6'";
        $query .= " WHERE ";
        $query .= "     SRH.YEAR || '-' || SRH.SEMESTER = '".$model->term."' ";
        $query .= " GROUP BY";
        $query .= "     SRH.YEAR, ";
        $query .= "     SRH.SEMESTER, ";
        $query .= "     SRH.GHR_CD,";
        $query .= "     SRH.GHR_NAME,";
        $query .= "     SRH.GHR_NAMEABBV,";
        $query .= "     SRH.TR_CD1, ";
        $query .= "     SRH.TR_CD2, ";
        $query .= "     SRH.TR_CD3, ";
        $query .= "     SRH.SUBTR_CD1, ";
        $query .= "     SRH.SUBTR_CD2, ";
        $query .= "     SRH.SUBTR_CD3 ";
        $query .= " ORDER BY ";
        $query .= "     SRH.YEAR,SRH.SEMESTER";

        return $query;
    }

//ここから取り込み時チェック
    //CSV取り込み時の入力チェック、職員マスタに登録されているコードなのか。
    public function checkStaffMst($staffcd)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     STAFF_MST ";
        $query .= " WHERE ";
        $query .= "     STAFFCD = '{$staffcd}' ";

        return $query;
    }

    //CSV取り込み時の入力チェック、前任者が終了後の日付なのか。




    //エラーＤＢへの追加
    public function insertQueryErr(&$db, $record_no, $check_error)
    {
        $data1["PROGRAMID"][TEXT] = 'KNJA062';
        $data1["MSGROW"][NUMBER]  = $record_no;
        $data1["MSGREMARK"][TEXT] = $check_error;

        $query = Query::insertSQL($data1, "W_CSVMSG_PRG_DAT");
        $result = $db->query($query);
    }

//ここまで


    //--- INSERT
    public function &getInsertQuery($model)
    {
        $db = Query::dbCheckOut();

        $data["YEAR"][TEXT]           = substr($model->term, 0, 4);
        $data["SEMESTER"][TEXT]       = substr($model->term, 5);
        $data["GHR_CD"][TEXT]         = $model->fields["GHR_CD"];
        $data["GHR_NAME"][TEXT]       = $model->fields["GHR_NAME"];
        $data["GHR_NAMEABBV"][TEXT]   = $model->fields["GHR_NAMEABBV"];
        $data["TR_CD1"][TEXT]         = $model->fields["TR_CD1"];
        $data["TR_CD2"][TEXT]         = $model->fields["TR_CD2"];
        $data["TR_CD3"][TEXT]         = $model->fields["TR_CD3"];
        $data["SUBTR_CD1"][TEXT]      = $model->fields["SUBTR_CD1"];
        $data["SUBTR_CD2"][TEXT]      = $model->fields["SUBTR_CD2"];
        $data["SUBTR_CD3"][TEXT]      = $model->fields["SUBTR_CD3"];
        $data["REGISTERCD"][TEXT]     = STAFFCD;
        $data["UPDATED"][NUMBER]      = "SYSDATE()";

        $query = Query::insertSQL($data, "SCHREG_REGD_GHR_HDAT");
        $db->query($query);
        Query::dbCheckIn($db);
        return;
    }

    //--- UPDATE
    public function &getUpdateQuery($model)
    {
        $db = Query::dbCheckOut();

        $data["YEAR"][TEXT]           = substr($model->term, 0, 4);
        $data["SEMESTER"][TEXT]       = substr($model->term, 5);
        $data["GHR_CD"][TEXT]         = $model->fields["GHR_CD"];
        $data["GHR_NAME"][TEXT]       = $model->fields["GHR_NAME"];
        $data["GHR_NAMEABBV"][TEXT]   = $model->fields["GHR_NAMEABBV"];
        $data["TR_CD1"][TEXT]         = $model->fields["TR_CD1"];
        $data["TR_CD2"][TEXT]         = $model->fields["TR_CD2"];
        $data["TR_CD3"][TEXT]         = $model->fields["TR_CD3"];
        $data["SUBTR_CD1"][TEXT]      = $model->fields["SUBTR_CD1"];
        $data["SUBTR_CD2"][TEXT]      = $model->fields["SUBTR_CD2"];
        $data["SUBTR_CD3"][TEXT]      = $model->fields["SUBTR_CD3"];
        $data["REGISTERCD"][TEXT]     = STAFFCD;
        $data["UPDATED"][NUMBER]      = "sysdate()";

        $where  = " WHERE YEAR   = '".SUBSTR($model->term, 0, 4)."'";
        $where .= " AND SEMESTER = '".SUBSTR($model->term, 5)."'";
        $where .= " AND GHR_CD   = '".$model->ghr_cd."'";

        $query = Query::updateSQL($data, "SCHREG_REGD_GHR_HDAT", $where);
        $db->query($query);
        Query::dbCheckIn($db);
        return;
    }

    //DELETE
    public function &getDeleteQuery($model)
    {
        $db = Query::dbCheckOut();
        //生徒が在籍しているかチェック
        $query  = "SELECT COUNT(*) FROM SCHREG_REGD_GHR_DAT ";
        $query .= " WHERE YEAR     = '".SUBSTR($model->term, 0, 4)."'";
        $query .= "   AND SEMESTER = '".SUBSTR($model->term, 5)."'";
        $query .= "   AND GHR_CD   = '".$model->ghr_cd."'";
        if ($db->getOne($query) > 0) {
            return "schreg_regd";
        }
        $query  = "DELETE FROM SCHREG_REGD_GHR_HDAT ";
        $query .= " WHERE YEAR     = '".SUBSTR($model->term, 0, 4)."'";
        $query .= "   AND SEMESTER = '".SUBSTR($model->term, 5)."'";
        $query .= "   AND GHR_CD   = '".$model->ghr_cd."'";
        $db->query($query);
        Query::dbCheckIn($db);
        return "succeeded";
    }

    //「コピーボタン」押し時に,学籍在籍ヘッダデータが作られているかチェック
    public function count($term)
    {
        $db = Query::dbCheckOut();

        $query  = " SELECT COUNT(*) FROM SCHREG_REGD_GHR_HDAT ";
        $query .= " WHERE YEAR = '".SUBSTR($term, 0, 4)."' AND SEMESTER = '".SUBSTR($term, 5, 1)."' ";

        $count = $db->getOne($query);
        return $count;
        Query::dbCheckIn($db);
    }

    //「コピーボタン」押し時に,学籍在籍データに生徒が割り振られているかチェック
    public function count2($term)
    {
        $db = Query::dbCheckOut();

        $query  = " SELECT COUNT(*) FROM SCHREG_REGD_GHR_DAT ";
        $query .= " WHERE YEAR = '".SUBSTR($term, 0, 4)."' AND SEMESTER = '".SUBSTR($term, 5, 1)."' ";

        $count = $db->getOne($query);
        return $count;
        Query::dbCheckIn($db);
    }

    //学籍ヘッダーデータの設定学期のデータを削除
    public function deleteHdat($term)
    {
        $query  ="DELETE FROM SCHREG_REGD_GHR_HDAT ";
        $query .=" WHERE YEAR     ='".SUBSTR($term, 0, 4)."'";
        $query .="   AND SEMESTER ='".SUBSTR($term, 5, 1)."'";

        return $query;
    }
    public function deleteStaffClass($term)
    {
        $query  ="DELETE FROM STAFF_CLASS_HIST_GHR_DAT ";
        $query .=" WHERE YEAR     ='".SUBSTR($term, 0, 4)."' ";
        $query .="   AND SEMESTER ='".SUBSTR($term, 5, 1)."' ";

        return $query;
    }

    //学籍在籍ヘッダデータの参照学期のデータを設定学期にインサート
    public function insertHdat($term2, $term)
    {
        $query  =" INSERT INTO SCHREG_REGD_GHR_HDAT ";
        $query .=" SELECT ";
        $query .="      '".SUBSTR($term, 0, 4)."', ";
        $query .="      '".SUBSTR($term, 5, 1)."', ";
        $query .="      GHR_CD, ";
        $query .="      GHR_NAME, ";
        $query .="      GHR_NAMEABBV, ";
        $query .="      TR_CD1, ";
        $query .="      TR_CD2, ";
        $query .="      TR_CD3,";
        $query .="      SUBTR_CD1, ";
        $query .="      SUBTR_CD2, ";
        $query .="      SUBTR_CD3, ";
        $query .="      '".STAFFCD."', ";
        $query .="      SYSDATE() ";
        $query .="   FROM SCHREG_REGD_GHR_HDAT ";
        $query .="  WHERE YEAR = '".SUBSTR($term2, 0, 4)."' AND SEMESTER = '".SUBSTR($term2, 5, 1)."' ";

        return $query;
    }

    public function insertStaffClass($term2, $term)
    {
        $term_year = SUBSTR($term, 0, 4);
        $term_year_add = (int)$term_year + 1;
        $term2_year = SUBSTR($term2, 0, 4);

        $query  = " INSERT INTO STAFF_CLASS_HIST_GHR_DAT ";
        $query .= " SELECT ";
        $query .="      '".SUBSTR($term, 0, 4)."', ";
        $query .="      '".SUBSTR($term, 5, 1)."', ";
        $query .="      T1.GHR_CD, ";
        $query .="      T1.TR_DIV, ";
        if ($term_year == $term2_year) {
            $query .="      T1.FROM_DATE, ";
            $query .="      T1.TO_DATE, ";
        } else {
            $query .="      '".$term_year."-04-01' AS FROM_DATE, ";
            $query .="      '".$term_year_add."-03-31' AS TO_DATE, ";
        }
        $query .="      T1.STAFFCD, ";
        $query .="      '".STAFFCD."', ";
        $query .="      SYSDATE()";
        $query .= " FROM ";
        $query .= "     STAFF_CLASS_HIST_GHR_DAT T1 ";
        $query .= " INNER JOIN ( ";
        $query .= "             SELECT ";
        $query .= "                 YEAR, ";
        $query .= "                 SEMESTER, ";
        $query .= "                 GHR_CD, ";
        $query .= "                 TR_DIV, ";
        $query .= "                 MAX(FROM_DATE) AS FROM_DATE ";
        $query .= "             FROM ";
        $query .= "                 STAFF_CLASS_HIST_GHR_DAT ";
        $query .= "             GROUP BY ";
        $query .= "                 YEAR, ";
        $query .= "                 SEMESTER, ";
        $query .= "                 GHR_CD, ";
        $query .= "                 TR_DIV ";
        $query .= "             ) L1 ON  L1.YEAR = T1.YEAR ";
        $query .= "                  AND L1.SEMESTER = T1.SEMESTER ";
        $query .= "                  AND L1.GHR_CD = T1.GHR_CD ";
        $query .= "                  AND L1.TR_DIV = T1.TR_DIV ";
        $query .= "                  AND L1.FROM_DATE = T1.FROM_DATE ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR     = '".SUBSTR($term2, 0, 4)."' AND ";
        $query .= "     T1.SEMESTER = '".SUBSTR($term2, 5, 1)."' ";

        return $query;
    }

    /*在籍データの設定学期に参照学期のデータを追加。
      (設定学期の出席番号は参照学期の出席番号でインサート)*/
    public function insertCfd($term2, $term, $grd_div)
    {
        $query  = " INSERT INTO SCHREG_REGD_GHR_DAT ";
        $query .= "     (SCHREGNO,YEAR,SEMESTER,GHR_CD,GHR_ATTENDNO,REGISTERCD,UPDATED) ";
        $query .= " SELECT ";
        $query .= "     SCHREGNO,";
        $query .= "     '".substr($term, 0, 4)."', ";
        $query .= "     '".substr($term, 5, 1)."', ";
        $query .= "     GHR_CD, ";
        $query .= "     GHR_ATTENDNO, ";
        $query .= "     '".STAFFCD."', ";
        $query .= "     SYSDATE() ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GHR_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".substr($term2, 0, 4)."' AND ";
        $query .= "     SEMESTER = '".substr($term2, 5, 1)."' ";
        if ($grd_div == "") {
            $query .= "     AND SCHREGNO IN (SELECT SCHREGNO FROM SCHREG_BASE_MST WHERE GRD_DIV IS NULL) ";
        }

        return $query;
    }

    //「コピーボタン」押し時の処理
    public function getInsertCopyQuery($term2, $term, $check, $grd_div)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $db->query(knja062Query::deleteHdat($term));
        $db->query(knja062Query::insertHdat($term2, $term));

        $db->query(knja062Query::deleteStaffClass($term));
        $db->query(knja062Query::insertStaffClass($term2, $term));

        if ($check == 1) {
            $db->query(knja062Query::insertCfd($term2, $term, $grd_div));
        }

        $db->commit();
        Query::dbCheckIn($db);
    }

    //CSVファイルよりDBへインサート
    public function insertQueryCsv($db, $model, $data_arr)
    {
        $data = array();
        $cnt = 0;    //処理件数
        for ($i = 0; $i < get_count($data_arr); $i++) {
            //データセット
            $data["YEAR"][TEXT]           = $data_arr[$i]["YEAR"];
            $data["SEMESTER"][TEXT]       = $data_arr[$i]["SEMESTER"];
            $data["GHR_CD"][TEXT]         = $data_arr[$i]["GHR_CD"];
            $data["GHR_NAME"][TEXT]       = $data_arr[$i]["GHR_NAME"];
            $data["GHR_NAMEABBV"][TEXT]   = $data_arr[$i]["GHR_NAMEABBV"];
            $data["TR_CD1"][TEXT]         = $data_arr[$i]["TR_CD1"];
            $data["TR_CD2"][TEXT]         = $data_arr[$i]["TR_CD2"];
            $data["TR_CD3"][TEXT]         = $data_arr[$i]["TR_CD3"];
            $data["SUBTR_CD1"][TEXT]      = $data_arr[$i]["SUBTR_CD1"];
            $data["SUBTR_CD2"][TEXT]      = $data_arr[$i]["SUBTR_CD2"];
            $data["SUBTR_CD3"][TEXT]      = $data_arr[$i]["SUBTR_CD3"];
            $data["REGISTERCD"][TEXT]     = STAFFCD;
            $data["UPDATED"][NUMBER]      = "SYSDATE()";

            //データが一件もなければ、insertする
            if (1 > $db->getOne("SELECT COUNT(*) FROM SCHREG_REGD_GHR_HDAT WHERE YEAR = '{$data_arr[$i]["YEAR"]}' AND SEMESTER = '{$data_arr[$i]["SEMESTER"]}' AND GHR_CD = '{$data_arr[$i]["GHR_CD"]}'")) {
                $query = Query::insertSQL($data, "SCHREG_REGD_GHR_HDAT");
            } else {
                $where  = "WHERE ";
                $where .= "     YEAR = '{$data_arr[$i]["YEAR"]}' AND SEMESTER = '{$data_arr[$i]["SEMESTER"]}' AND GHR_CD = '{$data_arr[$i]["GHR_CD"]}' ";

                $query = Query::updateSQL($data, "SCHREG_REGD_GHR_HDAT", $where);
            }
            $db->query($query);
            $cnt++;
        }
        return $cnt;
    }

    //CSVファイルよりDBへインサート(STAFF_CLASS_HIST_GHR_DAT)
    public function insert2QueryCsv($db, $model, $data_arr)
    {
        $data = array();
        for ($i = 0; $i < get_count($data_arr); $i++) {
            for ($staffcnt = 1; $staffcnt <= 6; $staffcnt++) {
                $soeji     = $staffcnt < 4 ? $staffcnt : $staffcnt - 3;
                $dateSoeji = $staffcnt < 4 ? "_T" : "_S";
                $trSoeji   = $staffcnt < 4 ? "" : "SUB";
                if (strlen($data_arr[$i]["FROM_DATE".$dateSoeji.$soeji]) > 0) {
                    //データセット
                    $data["YEAR"][TEXT]           = $data_arr[$i]["YEAR"];
                    $data["SEMESTER"][TEXT]       = $data_arr[$i]["SEMESTER"];
                    $data["GHR_CD"][TEXT]         = $data_arr[$i]["GHR_CD"];
                    $data["TR_DIV"][TEXT]         = $data_arr[$i]["TR_DIV".$staffcnt];
                    $data["FROM_DATE"][TEXT]      = $data_arr[$i]["FROM_DATE".$dateSoeji.$soeji];
                    $data["TO_DATE"][TEXT]        = $data_arr[$i]["TO_DATE".$dateSoeji.$soeji];
                    $data["STAFFCD"][TEXT]        = $data_arr[$i][$trSoeji."TR_CD".$soeji];
                    $data["REGISTERCD"][TEXT]     = STAFFCD;
                    $data["UPDATED"][NUMBER]      = "SYSDATE()";

                    //データが一件もなければ、insertする
                    $query  = " SELECT";
                    $query .= "   COUNT(*)";
                    $query .= " FROM";
                    $query .= "   STAFF_CLASS_HIST_GHR_DAT";
                    $query .= " WHERE YEAR      = '".$data_arr[$i]["YEAR"]."'";
                    $query .= "   AND SEMESTER  = '".$data_arr[$i]["SEMESTER"]."'";
                    $query .= "   AND GHR_CD    = '".$data_arr[$i]["GHR_CD"]."'";
                    $query .= "   AND TR_DIV    = '".$data_arr[$i]["TR_DIV".$staffcnt]."'";
                    $query .= "   AND FROM_DATE = '".str_replace("/", "-", $data_arr[$i]["FROM_DATE".$dateSoeji.$soeji])."'";
                    if (1 > $db->getOne($query)) {
                        $query = Query::insertSQL($data, "STAFF_CLASS_HIST_GHR_DAT");
                    } else {
                        $where  = " WHERE YEAR      = '".$data_arr[$i]["YEAR"]."'";
                        $where .= "   AND SEMESTER  = '".$data_arr[$i]["SEMESTER"]."'";
                        $where .= "   AND GHR_CD    = '".$data_arr[$i]["GHR_CD"]."'";
                        $where .= "   AND TR_DIV    = '".$data_arr[$i]["TR_DIV".$staffcnt]."'";
                        $where .= "   AND FROM_DATE = '".str_replace("/", "-", $data_arr[$i]["FROM_DATE".$dateSoeji.$soeji])."'";
                        $query = Query::updateSQL($data, "STAFF_CLASS_HIST_GHR_DAT", $where);
                    }

                    $db->query($query);

                    //TO_DATEを、updateするSQL
                    $query  = " SELECT";
                    $query .= "   *";
                    $query .= " FROM";
                    $query .= "   STAFF_CLASS_HIST_GHR_DAT";
                    $query .= " WHERE YEAR      = '".$data_arr[$i]["YEAR"]."'";
                    $query .= "   AND SEMESTER  = '".$data_arr[$i]["SEMESTER"]."'";
                    $query .= "   AND GHR_CD    = '".$data_arr[$i]["GHR_CD"]."'";
                    $query .= "   AND TR_DIV    = '".$data_arr[$i]["TR_DIV".$staffcnt]."'";
                    $query .= " ORDER BY";
                    $query .= "   FROM_DATE DESC";

                    //TO_DATEを、updateする
                    $setDate =  ((int)$data_arr[$i]["YEAR"] + 1)."-03-31";
                    $result = $db->query($query);
                    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                        if ($row["TO_DATE"] <= $setDate) {
                        } else {
                            //データセット
                            $data = array();
                            $data["TO_DATE"][TEXT]        = $setDate;
                            $data["REGISTERCD"][TEXT]     = STAFFCD;
                            $data["UPDATED"][NUMBER]      = "SYSDATE()";
                            $where  = " WHERE YEAR      = '".$row["YEAR"]."'";
                            $where .= "   AND SEMESTER  = '".$row["SEMESTER"]."'";
                            $where .= "   AND GHR_CD    = '".$row["GHR_CD"]."'";
                            $where .= "   AND TR_DIV    = '".$row["TR_DIV"]."'";
                            $where .= "   AND FROM_DATE = '".$row["FROM_DATE"]."'";
                            $query = Query::updateSQL($data, "STAFF_CLASS_HIST_GHR_DAT", $where);
                            $db->query($query);
                        }

                        $setDate = date("Y-m-d", strtotime("-1 day", strtotime($row["FROM_DATE"])));
                    }
                    $result->free();
                }
            }
        }
    }
}
?>
