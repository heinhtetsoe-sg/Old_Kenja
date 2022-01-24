<?php

require_once('for_php7.php');

class knjh661Query extends Query
{

    /************************/
    /**  ＣＳＶ取込で使用  **/
    /************************/
    public function updateQueryCsv($model, &$data_arr)
    {
        $data = array();
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $cnt = 0;   //処理件数
        for ($i = 0; $i < get_count($data_arr); $i++) {

            //MOCK_CENTER_DAT
            //DEL
            $query  = " DELETE FROM MOCK_CENTER_DAT ";
            $query .= " WHERE ";
            $query .= "     YEAR = '".CTRL_YEAR."' ";
            $query .= "     AND CENTERCD = '000000000' ";
            $query .= "     AND SCHREGNO = '{$data_arr[$i]["SCHREGNO"]}' ";
            $query .= "     AND RANK_RANGE = '1' ";
            $query .= "     AND RANK_DIV = '02' ";
            $query .= "     AND CENTERDIV = '1' ";
            $db->query($query);
            //INS
            $data = array();
            $data["YEAR"][TEXT]                 = CTRL_YEAR;
            $data["CENTERCD"][TEXT]             = "000000000";
            $data["SCHREGNO"][TEXT]             = $data_arr[$i]["SCHREGNO"];
            $data["RANK_RANGE"][TEXT]           = "1";
            $data["RANK_DIV"][TEXT]             = "02";
            $data["CENTERDIV"][TEXT]            = "1";
            $data["BUNRIDIV"][TEXT]             = $data_arr[$i]["BUNRIDIV"];
            $data["REGISTERCD"][TEXT]           = STAFFCD;
            $data["UPDATED"][NUMBER]            = "sysdate()";
            $query = Query::insertSQL($data, "MOCK_CENTER_DAT");
            $db->query($query);
            //MOCK_CENTER_RANK_RANGE_DAT
            //DEL
            $query  = " DELETE FROM MOCK_CENTER_RANK_RANGE_DAT ";
            $query .= " WHERE ";
            $query .= "     YEAR = '".CTRL_YEAR."' ";
            $query .= "     AND CENTERCD = '000000000' ";
            $query .= "     AND SCHREGNO = '{$data_arr[$i]["SCHREGNO"]}' ";
            $query .= "     AND RANK_RANGE = '1' ";
            $query .= "     AND RANK_DIV = '02' ";
            $query .= "     AND CENTERDIV = '1' ";
            $db->query($query);
            //INS
            foreach ($model->subclassArray as $key => $val) { //$tmp[5]～
                $subclassCd = $val["CENTER_SUBCLASS_CD"];
                //国語合計
                if ($subclassCd == "000000") {
                    continue;
                }

                if (strlen($data_arr[$i]["SCORE".$subclassCd]) > 0) {
                    $data = array();
                    $data["YEAR"][TEXT]                 = CTRL_YEAR;
                    $data["CENTERCD"][TEXT]             = "000000000";
                    $data["SCHREGNO"][TEXT]             = $data_arr[$i]["SCHREGNO"];
                    $data["CENTER_CLASS_CD"][TEXT]      = $val["CENTER_CLASS_CD"];
                    $data["CENTER_SUBCLASS_CD"][TEXT]   = $subclassCd;
                    $data["RANK_RANGE"][TEXT]           = "1";
                    $data["RANK_DIV"][TEXT]             = "02";
                    $data["CENTERDIV"][TEXT]            = "1";
                    $data["SCORE"][NUMBER]              = $data_arr[$i]["SCORE".$subclassCd]; //得点
                    //第1選択フラグ(社会と理科２のみ)
                    if ($val["CENTER_CLASS_CD"] == "04" || $val["CENTER_CLASS_CD"] == "06") {
                        $data["DAIITI_SENTAKU"][TEXT]       = $data_arr[$i]["CHECK".$subclassCd];
                    }
                    $data["REGISTERCD"][TEXT]           = STAFFCD;
                    $data["UPDATED"][NUMBER]            = "sysdate()";
                    $query = Query::insertSQL($data, "MOCK_CENTER_RANK_RANGE_DAT");
                    $db->query($query);
                }
            }

            $cnt++;
        }
        $db->commit();
        Query::dbCheckIn($db);

        return $cnt;
    }

    /********************/
    /**  エラー処理用  **/
    /********************/

    //エラーＤＢへの追加
    public function insertQueryErr(&$db, $record_no, $check_error)
    {
        $data1 = array();
        $data1["PROGRAMID"][TEXT] = PROGRAMID;
        $data1["MSGROW"][NUMBER]  = $record_no;
        $data1["MSGREMARK"][TEXT] = $check_error;

        $query = Query::insertSQL($data1, "W_CSVMSG_PRG_DAT");
        $db->query($query);
    }

    /**********************/
    /**  存在チェック用  **/
    /**********************/

    //名称マスタ登録コードチェック
    public function nameCntSql($namecd, $setcd, $where="")
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = '{$namecd}' AND ";
        $query .= "     NAMECD2 = '{$setcd}' ";
        if ($where) {
            $query .= $where;
        }

        return $query;
    }

    //タイトル
    public function getTitle()
    {
        $query  = " WITH KOKUGO_GOUKEI(CENTER_CLASS_CD, CENTER_SUBCLASS_CD, SUBCLASS_ABBV) AS ( ";
        $query .= "     VALUES('02', '000000', '国語') ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     MOCK_SUB.CENTER_CLASS_CD, ";
        $query .= "     MOCK_SUB.CENTER_SUBCLASS_CD, ";
        $query .= "     MOCK_SUB.SUBCLASS_ABBV ";
        $query .= " FROM ";
        $query .= "     MOCK_CENTER_SUBCLASS_MST MOCK_SUB ";
        $query .= " WHERE ";
        $query .= "     MOCK_SUB.YEAR = '".CTRL_YEAR."' ";
        $query .= " UNION ";
        $query .= " SELECT ";
        $query .= "     CENTER_CLASS_CD, ";
        $query .= "     CENTER_SUBCLASS_CD, ";
        $query .= "     SUBCLASS_ABBV ";
        $query .= " FROM ";
        $query .= "     KOKUGO_GOUKEI ";
        $query .= " ORDER BY ";
        $query .= "     CENTER_CLASS_CD, ";
        $query .= "     CENTER_SUBCLASS_CD ";

        return $query;
    }

    //模試データ出力用
    public function getselectCenterData($model)
    {
        $query .= " WITH MAX_SEME AS ( ";
        $query .= " SELECT ";
        $query .= "     REGD.YEAR, ";
        $query .= "     REGD.SCHREGNO, ";
        $query .= "     MAX(REGD.SEMESTER) AS SEMESTER ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT REGD ";
        $query .= " WHERE ";
        $query .= "     REGD.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND EXISTS( ";
        $query .= "         SELECT ";
        $query .= "             'x' ";
        $query .= "         FROM ";
        $query .= "             NAME_MST NM ";
        $query .= "         WHERE ";
        $query .= "             NM.NAMECD1 = 'A023' ";
        $query .= "             AND NM.NAME1 = 'H' ";
        $query .= "             AND REGD.GRADE BETWEEN NM.NAMESPARE2 AND NAMESPARE3 ";
        $query .= "     ) ";
        $query .= " GROUP BY ";
        $query .= "     REGD.YEAR, ";
        $query .= "     REGD.SCHREGNO ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     REGD.SCHREGNO, ";
        $query .= "     HDAT.HR_NAME || '-' || REGD.ATTENDNO, ";
        $query .= "     BASE.NAME, ";
        $query .= "     CENTER_D.BUNRIDIV, ";
        $query .= "     H316.NAME1 AS BUNRINAME, ";
        foreach ($model->subclassArray as $key => $val) {
            $subclassCd = $val["CENTER_SUBCLASS_CD"];
            if ($subclassCd == "000000") {
                $query .= "     KOKUGO_GOUKEI.SCORE AS KOKUGO_GOUKEI, ";
            } else {
                $query .= "     SCORE{$subclassCd}.SCORE AS SCORE{$subclassCd}, ";
            }
            //社会と理科２
            if ($val["CENTER_CLASS_CD"] == "04" || $val["CENTER_CLASS_CD"] == "06") {
                $query .= "     SCORE{$subclassCd}.DAIITI_SENTAKU AS CHECK{$subclassCd}, ";
            }
        }
        $query .= "     EIGO_GOUKEI.SCORE AS EIGO_GOUKEI, ";
        $query .= "     SUUGAKU_GOUKEI.SCORE AS SUUGAKU_GOUKEI, ";
        $query .= "     CASE WHEN SCORE040001.SCORE IS NULL AND SCORE040002.SCORE IS NULL AND SCORE040003.SCORE IS NULL AND SCORE040004.SCORE IS NULL AND SCORE040005.SCORE IS NULL AND SCORE040006.SCORE IS NULL THEN CAST(NULL AS INT) ";
        $query .= "         ELSE VALUE(SCORE040001.SCORE, 0) + VALUE(SCORE040002.SCORE, 0) + VALUE(SCORE040003.SCORE, 0) + VALUE(SCORE040004.SCORE, 0) + VALUE(SCORE040005.SCORE, 0) + VALUE(SCORE040006.SCORE, 0) END AS TIREKI_GOUKEI, ";
        $query .= "     CASE WHEN SCORE040007.SCORE IS NULL AND SCORE040008.SCORE IS NULL AND SCORE040009.SCORE IS NULL AND SCORE040010.SCORE IS NULL THEN CAST(NULL AS INT) ";
        $query .= "         ELSE VALUE(SCORE040007.SCORE, 0) + VALUE(SCORE040008.SCORE, 0) + VALUE(SCORE040009.SCORE, 0) + VALUE(SCORE040010.SCORE, 0) END AS KOUMIN_GOUKEI, ";
        $query .= "     CASE WHEN RIKA_GOUKEI1.SCORE IS NULL AND RIKA_GOUKEI2.SCORE IS NULL THEN CAST(NULL AS INT) ";
        $query .= "         ELSE VALUE(RIKA_GOUKEI1.SCORE, 0) + VALUE(RIKA_GOUKEI2.SCORE, 0) END AS RIKA_GOUKEI, ";
        $query .= "     CASE WHEN EIGO_GOUKEI.SCORE IS NULL AND KOKUGO_GOUKEI.SCORE IS NULL AND SUUGAKU_GOUKEI.SCORE IS NULL AND SYAKAI_GOUKEI.SCORE IS NULL AND RIKA_GOUKEI1.SCORE IS NULL AND RIKA_GOUKEI2.SCORE IS NULL THEN CAST(NULL AS INT) ";
        $query .= "         ELSE VALUE(EIGO_GOUKEI.SCORE, 0) + VALUE(KOKUGO_GOUKEI.SCORE, 0) + VALUE(SUUGAKU_GOUKEI.SCORE, 0) + VALUE(SYAKAI_GOUKEI.SCORE, 0) + VALUE(RIKA_GOUKEI1.SCORE, 0) + VALUE(RIKA_GOUKEI2.SCORE, 0) END AS SOU_GOUKEI, ";
        $sep = "";
        foreach ($model->subclassArray as $key => $val) {
            $subclassCd = $val["CENTER_SUBCLASS_CD"];
            if ($subclassCd == "000000") {
                continue;
            }
            $query .= "     {$sep} CASE WHEN SCORE{$subclassCd}.SCORE IS NOT NULL THEN VALUE(SUB_M{$subclassCd}.ALLOT_POINT, 0) ELSE 0 END ";
            $sep = "+";
        }
        $query .= "     AS TOTAL_PERFECT, ";
        $query .= "     '' AS SCORE800, ";
        $query .= "     '' AS SCORE500, ";
        $query .= "     '' AS RANK800, ";
        $query .= "     '' AS RANK500, ";
        $query .= "     '' AS NYUUSHI_NO ";
        $query .= " FROM ";
        $query .= "     MAX_SEME, ";
        $query .= "     SCHREG_REGD_DAT REGD ";
        $query .= "     LEFT JOIN SCHREG_REGD_HDAT HDAT ON REGD.YEAR = HDAT.YEAR ";
        $query .= "          AND REGD.SEMESTER = HDAT.SEMESTER ";
        $query .= "          AND REGD.GRADE = HDAT.GRADE ";
        $query .= "          AND REGD.HR_CLASS = HDAT.HR_CLASS ";
        $query .= "     LEFT JOIN SCHREG_BASE_MST BASE ON REGD.SCHREGNO = BASE.SCHREGNO ";
        $query .= "     LEFT JOIN MOCK_CENTER_DAT CENTER_D ON REGD.YEAR = CENTER_D.YEAR ";
        $query .= "          AND CENTER_D.CENTERCD = '000000000' ";
        $query .= "          AND REGD.SCHREGNO = CENTER_D.SCHREGNO ";
        $query .= "          AND CENTER_D.RANK_RANGE = '1' ";
        $query .= "          AND CENTER_D.RANK_DIV = '02' ";
        $query .= "          AND CENTER_D.CENTERDIV = '1' ";
        $query .= "     LEFT JOIN NAME_MST H316 ON H316.NAMECD1 = 'H316' ";
        $query .= "          AND CENTER_D.BUNRIDIV = H316.NAMECD2 ";
        foreach ($model->subclassArray as $key => $val) {
            $subclassCd = $val["CENTER_SUBCLASS_CD"];
            if ($subclassCd == "000000") {
                continue;
            }
            $query .= "     LEFT JOIN MOCK_CENTER_RANK_RANGE_DAT SCORE{$subclassCd} ON CENTER_D.YEAR = SCORE{$subclassCd}.YEAR ";
            $query .= "          AND CENTER_D.CENTERCD = SCORE{$subclassCd}.CENTERCD ";
            $query .= "          AND CENTER_D.SCHREGNO = SCORE{$subclassCd}.SCHREGNO ";
            $query .= "          AND SCORE{$subclassCd}.CENTER_SUBCLASS_CD = '{$subclassCd}' ";
            $query .= "          AND CENTER_D.RANK_RANGE = SCORE{$subclassCd}.RANK_RANGE ";
            $query .= "          AND CENTER_D.RANK_DIV = SCORE{$subclassCd}.RANK_DIV ";
            $query .= "          AND CENTER_D.CENTERDIV = SCORE{$subclassCd}.CENTERDIV ";
            $query .= "     LEFT JOIN MOCK_CENTER_SUBCLASS_MST SUB_M{$subclassCd} ON SCORE{$subclassCd}.YEAR = SUB_M{$subclassCd}.YEAR ";
            $query .= "          AND SCORE{$subclassCd}.CENTER_CLASS_CD = SUB_M{$subclassCd}.CENTER_CLASS_CD ";
            $query .= "          AND SCORE{$subclassCd}.CENTER_SUBCLASS_CD = SUB_M{$subclassCd}.CENTER_SUBCLASS_CD ";
        }
        $query .= "     LEFT JOIN MOCK_CENTER_TOTAL_SCORE_DAT EIGO_GOUKEI ON CENTER_D.YEAR = EIGO_GOUKEI.YEAR ";
        $query .= "          AND CENTER_D.CENTERCD = EIGO_GOUKEI.CENTERCD ";
        $query .= "          AND CENTER_D.SCHREGNO = EIGO_GOUKEI.SCHREGNO ";
        $query .= "          AND EIGO_GOUKEI.CENTER_CLASS_CD = '01' ";
        $query .= "          AND EIGO_GOUKEI.CENTER_SUBCLASS_CD = '999999' ";
        $query .= "          AND CENTER_D.RANK_RANGE = EIGO_GOUKEI.RANK_RANGE ";
        $query .= "          AND CENTER_D.RANK_DIV = EIGO_GOUKEI.RANK_DIV ";
        $query .= "          AND CENTER_D.CENTERDIV = EIGO_GOUKEI.CENTERDIV ";
        $query .= "     LEFT JOIN MOCK_CENTER_TOTAL_SCORE_DAT KOKUGO_GOUKEI ON CENTER_D.YEAR = KOKUGO_GOUKEI.YEAR ";
        $query .= "          AND CENTER_D.CENTERCD = KOKUGO_GOUKEI.CENTERCD ";
        $query .= "          AND CENTER_D.SCHREGNO = KOKUGO_GOUKEI.SCHREGNO ";
        $query .= "          AND KOKUGO_GOUKEI.CENTER_CLASS_CD = '02' ";
        $query .= "          AND KOKUGO_GOUKEI.CENTER_SUBCLASS_CD = '999999' ";
        $query .= "          AND CENTER_D.RANK_RANGE = KOKUGO_GOUKEI.RANK_RANGE ";
        $query .= "          AND CENTER_D.RANK_DIV = KOKUGO_GOUKEI.RANK_DIV ";
        $query .= "          AND CENTER_D.CENTERDIV = KOKUGO_GOUKEI.CENTERDIV ";
        $query .= "     LEFT JOIN MOCK_CENTER_TOTAL_SCORE_DAT SUUGAKU_GOUKEI ON CENTER_D.YEAR = SUUGAKU_GOUKEI.YEAR ";
        $query .= "          AND CENTER_D.CENTERCD = SUUGAKU_GOUKEI.CENTERCD ";
        $query .= "          AND CENTER_D.SCHREGNO = SUUGAKU_GOUKEI.SCHREGNO ";
        $query .= "          AND SUUGAKU_GOUKEI.CENTER_CLASS_CD = '03' ";
        $query .= "          AND SUUGAKU_GOUKEI.CENTER_SUBCLASS_CD = '999999' ";
        $query .= "          AND CENTER_D.RANK_RANGE = SUUGAKU_GOUKEI.RANK_RANGE ";
        $query .= "          AND CENTER_D.RANK_DIV = SUUGAKU_GOUKEI.RANK_DIV ";
        $query .= "          AND CENTER_D.CENTERDIV = SUUGAKU_GOUKEI.CENTERDIV ";
        $query .= "     LEFT JOIN MOCK_CENTER_TOTAL_SCORE_DAT SYAKAI_GOUKEI ON CENTER_D.YEAR = SYAKAI_GOUKEI.YEAR ";
        $query .= "          AND CENTER_D.CENTERCD = SYAKAI_GOUKEI.CENTERCD ";
        $query .= "          AND CENTER_D.SCHREGNO = SYAKAI_GOUKEI.SCHREGNO ";
        $query .= "          AND SYAKAI_GOUKEI.CENTER_CLASS_CD = '04' ";
        $query .= "          AND SYAKAI_GOUKEI.CENTER_SUBCLASS_CD = '999999' ";
        $query .= "          AND CENTER_D.RANK_RANGE = SYAKAI_GOUKEI.RANK_RANGE ";
        $query .= "          AND CENTER_D.RANK_DIV = SYAKAI_GOUKEI.RANK_DIV ";
        $query .= "          AND CENTER_D.CENTERDIV = SYAKAI_GOUKEI.CENTERDIV ";
        $query .= "     LEFT JOIN MOCK_CENTER_TOTAL_SCORE_DAT RIKA_GOUKEI1 ON CENTER_D.YEAR = RIKA_GOUKEI1.YEAR ";
        $query .= "          AND CENTER_D.CENTERCD = RIKA_GOUKEI1.CENTERCD ";
        $query .= "          AND CENTER_D.SCHREGNO = RIKA_GOUKEI1.SCHREGNO ";
        $query .= "          AND RIKA_GOUKEI1.CENTER_CLASS_CD = '05' ";
        $query .= "          AND RIKA_GOUKEI1.CENTER_SUBCLASS_CD = '999999' ";
        $query .= "          AND CENTER_D.RANK_RANGE = RIKA_GOUKEI1.RANK_RANGE ";
        $query .= "          AND CENTER_D.RANK_DIV = RIKA_GOUKEI1.RANK_DIV ";
        $query .= "          AND CENTER_D.CENTERDIV = RIKA_GOUKEI1.CENTERDIV ";
        $query .= "     LEFT JOIN MOCK_CENTER_TOTAL_SCORE_DAT RIKA_GOUKEI2 ON CENTER_D.YEAR = RIKA_GOUKEI2.YEAR ";
        $query .= "          AND CENTER_D.CENTERCD = RIKA_GOUKEI2.CENTERCD ";
        $query .= "          AND CENTER_D.SCHREGNO = RIKA_GOUKEI2.SCHREGNO ";
        $query .= "          AND RIKA_GOUKEI2.CENTER_CLASS_CD = '06' ";
        $query .= "          AND RIKA_GOUKEI2.CENTER_SUBCLASS_CD = '999999' ";
        $query .= "          AND CENTER_D.RANK_RANGE = RIKA_GOUKEI2.RANK_RANGE ";
        $query .= "          AND CENTER_D.RANK_DIV = RIKA_GOUKEI2.RANK_DIV ";
        $query .= "          AND CENTER_D.CENTERDIV = RIKA_GOUKEI2.CENTERDIV ";
        $query .= "     LEFT JOIN MOCK_CENTER_KANSAN_RANK_DAT KANSAN800 ON CENTER_D.YEAR = KANSAN800.YEAR ";
        $query .= "          AND CENTER_D.CENTERCD = KANSAN800.CENTERCD ";
        $query .= "          AND CENTER_D.SCHREGNO = KANSAN800.SCHREGNO ";
        $query .= "          AND KANSAN800.KANSAN_DIV = '01' ";
        $query .= "          AND KANSAN800.BUNRIDIV = '0' ";
        $query .= "          AND CENTER_D.RANK_RANGE = KANSAN800.RANK_RANGE ";
        $query .= "          AND CENTER_D.RANK_DIV = KANSAN800.RANK_DIV ";
        $query .= "          AND CENTER_D.CENTERDIV = KANSAN800.CENTERDIV ";
        $query .= "     LEFT JOIN MOCK_CENTER_KANSAN_RANK_DAT KANSAN500 ON CENTER_D.YEAR = KANSAN500.YEAR ";
        $query .= "          AND CENTER_D.CENTERCD = KANSAN500.CENTERCD ";
        $query .= "          AND CENTER_D.SCHREGNO = KANSAN500.SCHREGNO ";
        $query .= "          AND KANSAN500.KANSAN_DIV = '02' ";
        $query .= "          AND CENTER_D.BUNRIDIV = KANSAN500.BUNRIDIV ";
        $query .= "          AND CENTER_D.RANK_RANGE = KANSAN500.RANK_RANGE ";
        $query .= "          AND CENTER_D.RANK_DIV = KANSAN500.RANK_DIV ";
        $query .= "          AND CENTER_D.CENTERDIV = KANSAN500.CENTERDIV ";
        $query .= " WHERE ";
        $query .= "     MAX_SEME.YEAR = REGD.YEAR ";
        $query .= "     AND MAX_SEME.SCHREGNO = REGD.SCHREGNO ";
        $query .= "     AND MAX_SEME.SEMESTER = REGD.SEMESTER ";
        $query .= " ORDER BY ";
        $query .= "     HDAT.GRADE, ";
        $query .= "     HDAT.HR_CLASS, ";
        $query .= "     REGD.ATTENDNO ";

        return $query;
    }
}
