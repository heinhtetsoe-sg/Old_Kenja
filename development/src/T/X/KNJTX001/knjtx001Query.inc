<?php

require_once('for_php7.php');

class knjtx001Query extends Query {
    function checkControlMst($db) {
        if ($db->getOne("SELECT COUNT(*) FROM CONTROL_MST") > 0) {
            return true;
        } else {
            return false;
        }
    }

    function selectYearQuery() {
        return "SELECT DISTINCT YEAR FROM SCHOOL_MST ORDER BY YEAR DESC";
    }

    function checkAssessMst($db) {
        if ($db->getOne("SELECT COUNT(*) FROM ASSESS_HDAT") > 0) {
            return true;
        } else {
            return false;
        }
    }

    function selectQuery($year) {
        $query  = "SELECT * FROM V_SCHOOL_MST";
        $query .= " WHERE YEAR = '". $year ."'";
        return $query;
    }

    function GetAssesscd1() {
        $query = "SELECT ASSESSCD, ASSESSMEMO,ASSESSLEVELCNT FROM ASSESS_HDAT WHERE ASSESSCD = '2'";
        return $query;
    }

    function GetAssesscd2() {
        $query = "SELECT ASSESSCD || ':' || ASSESSMEMO FROM ASSESS_HDAT WHERE ASSESSCD = '3'";
        return $query;
    }

    function getSchooldiv() {
        return "SELECT NAMECD2, NAME1 FROM NAME_MST WHERE NAMECD1 = 'Z001'";
    }

    //評定計算方法
    function getGvalCalc() {
        return "SELECT NAMECD2, NAME1 FROM NAME_MST WHERE NAMECD1 = 'Z017' ORDER BY NAMECD2";
    }
    /*****************************************
        学校情報マスタ更新(全レコード）
     *****************************************/
    function updateSchoolMst($model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $db->query("DELETE FROM SCHOOL_MST WHERE YEAR = '". $model->year ."'");

        $data["YEAR"][TEXT]                 = $model->year;
        $data["FOUNDEDYEAR"][TEXT]          = $model->field["FOUNDEDYEAR"];
        $data["PRESENT_EST"][TEXT]          = $model->field["PRESENT_EST"];
        $data["CLASSIFICATION"][TEXT]       = $model->field["CLASSIFICATION"];
        $data["SCHOOLNAME1"][TEXT]          = $model->field["SCHOOLNAME1"];
        $data["SCHOOLNAME2"][TEXT]          = $model->field["SCHOOLNAME2"];
        $data["SCHOOLNAME3"][TEXT]          = $model->field["SCHOOLNAME3"];
        $data["SCHOOLNAME_ENG"][TEXT]       = $model->field["SCHOOLNAME_ENG"];
        $data["SCHOOLZIPCD"][TEXT]          = $model->field["SCHOOLZIPCD"];
        $data["SCHOOLADDR1"][TEXT]          = $model->field["SCHOOLADDR1"];
        $data["SCHOOLADDR2"][TEXT]          = $model->field["SCHOOLADDR2"];
        $data["SCHOOLADDR1_ENG"][TEXT]      = $model->field["SCHOOLADDR1_ENG"];
        $data["SCHOOLADDR2_ENG"][TEXT]      = $model->field["SCHOOLADDR2_ENG"];
        $data["SCHOOLTELNO"][TEXT]          = $model->field["SCHOOLTELNO"];
        $data["SCHOOLFAXNO"][TEXT]          = $model->field["SCHOOLFAXNO"];
        $data["SCHOOLMAIL"][TEXT]           = $model->field["SCHOOLMAIL"];
        $data["SCHOOLURL"][TEXT]            = $model->field["SCHOOLURL"];
        $data["SCHOOLDIV"][TEXT]            = $model->field["SCHOOLDIV"];
        $data["SEMESTERDIV"][TEXT]          = $model->field["SEMESTERDIV"];
        $data["GRADE_HVAL"][TEXT]           = $model->field["GRADE_HVAL"];
        $data["ENTRANCE_DATE"][FUNC]        = ($model->field["ENTRANCE_DATE"] != "") ? "DATE('".str_replace("/","-",$model->field["ENTRANCE_DATE"])."')" : "NULL";
        $data["GRADUATE_DATE"][FUNC]        = ($model->field["GRADUATE_DATE"] != "") ? "DATE('".str_replace("/","-",$model->field["GRADUATE_DATE"])."')" : "NULL";
        $data["GRAD_CREDITS"][NUMBER]       = $model->field["GRAD_CREDITS"];
        $data["GRAD_COMP_CREDITS"][NUMBER]  = $model->field["GRAD_COMP_CREDITS"];
        $data["SEMES_ASSESSCD"][TEXT]       = $model->field["SEMES_ASSESSCD"];
        $data["SEMES_FEARVAL"][NUMBER]      = $model->field["SEMES_FEARVAL"];
        $data["GRADE_FEARVAL"][NUMBER]      = $model->field["GRADE_FEARVAL"];
        $data["ABSENT_COV"][TEXT]           = $model->field["ABSENT_COV"];
        $data["ABSENT_COV_LATE"][NUMBER]    = $model->field["ABSENT_COV_LATE"];
        $data["GVAL_CALC"][TEXT]            = $model->field["GVAL_CALC"];
        $data["SEM_OFFDAYS"][TEXT]          = strlen($model->field["SEM_OFFDAYS"]) ? "1" : "";
        $data["SUB_OFFDAYS"][TEXT]          = strlen($model->field["SUB_OFFDAYS"]) ? "1" : "";
        $data["SUB_MOURNING"][TEXT]         = strlen($model->field["SUB_MOURNING"]) ? "1" : "";
        $data["SUB_SUSPEND"][TEXT]          = strlen($model->field["SUB_SUSPEND"]) ? "1" : "";
        if($model->virus == 'true'){
            $data["SUB_VIRUS"][TEXT]        = strlen($model->field["SUB_VIRUS"]) ? "1" : "";
        }
        $data["SUB_ABSENT"][TEXT]           = strlen($model->field["SUB_ABSENT"]) ? "1" : "";
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][FUNC]              = "sysdate()";

        $query = Query::insertSQL($data, "SCHOOL_MST");
        $db->query($query);
        knjtx001Query::updateDetailJougentiSansyutuHou($model, $db);
        knjtx001Query::updateDetailTokubetuKatudoKansan($model, $db);
        knjtx001Query::updateDetailAmariKuriage($model, $db); //一行上の処理で必ず「SCHOOL_DETAIL_DAT」の「SCHOOL_SEQ = '001'」は存在するのでUPDATEの処理だけでいい
        knjtx001Query::updateDetailSyukessekiHanteiHou($model, $db);
        $db->commit();
        Query::dbCheckIn($db);
        return true;
    }

    function updateDetailJougentiSansyutuHou($model, $db) {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     SCHOOL_DETAIL_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$model->year}' AND ";
        $query .= "     SCHOOL_SEQ = '001' ";

        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);

        if (preg_match('/[3-4]/', $model->field["ABSENT_COV"])) {
            $jougenti_sansyutu_hou = 4; //実数
        } else {
            $jougenti_sansyutu_hou = 3; //切捨て
        }

        if (is_array($row)) {
            if (!strlen($row["SCHOOL_REMARK8"])) {
                $data["SCHOOL_REMARK8"][TEXT] = $jougenti_sansyutu_hou;
                $data["REGISTERCD"][TEXT]     = STAFFCD;
                $data["UPDATED"][FUNC]        = "SYSDATE()";

                $where  = " WHERE ";
                $where .= "     YEAR = '{$model->year}' AND ";
                $where .= "     SCHOOL_SEQ = '001' ";

                $query = Query::updateSQL($data, "SCHOOL_DETAIL_DAT", $where);
                $db->query($query);
            }
        } else {
            $data = array();
            $data["YEAR"][TEXT]            = $model->year;
            $data["SCHOOL_SEQ"][TEXT]      = "001";
            $data["SCHOOL_REMARK8"][TEXT]  = $jougenti_sansyutu_hou;
            $data["REGISTERCD"][TEXT]      = STAFFCD;
            $data["UPDATED"][FUNC]         = "sysdate()";

            $query = Query::insertSQL($data, "SCHOOL_DETAIL_DAT");
            $db->query($query);
        }
    }

    function updateDetailTokubetuKatudoKansan($model, $db) {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     SCHOOL_DETAIL_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$model->year}' AND ";
        $query .= "     SCHOOL_SEQ = '003' ";

        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);

        if (is_array($row)) {
            $data["SCHOOL_REMARK6"][TEXT] = $model->field["TOKUBETU_KATUDO_KANSAN"];
            $data["REGISTERCD"][TEXT]     = STAFFCD;
            $data["UPDATED"][FUNC]        = "SYSDATE()";

            $where  = " WHERE ";
            $where .= "     YEAR = '{$model->year}' AND ";
            $where .= "     SCHOOL_SEQ = '003' ";

            $query = Query::updateSQL($data, "SCHOOL_DETAIL_DAT", $where);
            $db->query($query);
        } else {
            $data = array();
            $data["YEAR"][TEXT]            = $model->year;
            $data["SCHOOL_SEQ"][TEXT]      = "003";
            $data["SCHOOL_REMARK6"][TEXT]  = $model->field["TOKUBETU_KATUDO_KANSAN"];
            $data["REGISTERCD"][TEXT]      = STAFFCD;
            $data["UPDATED"][FUNC]         = "sysdate()";

            $query = Query::insertSQL($data, "SCHOOL_DETAIL_DAT");
            $db->query($query);
        }
    }

    function updateDetailAmariKuriage($model, $db) {
        $data["SCHOOL_REMARK9"][TEXT] = $model->field["AMARI_KURIAGE"];
        $data["REGISTERCD"][TEXT]     = STAFFCD;
        $data["UPDATED"][FUNC]        = "SYSDATE()";

        $where  = " WHERE ";
        $where .= "     YEAR = '{$model->year}' AND ";
        $where .= "     SCHOOL_SEQ = '001' ";

        $query = Query::updateSQL($data, "SCHOOL_DETAIL_DAT", $where);
        $db->query($query);
    }

    function updateDetailSyukessekiHanteiHou($model, $db) {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     SCHOOL_DETAIL_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$model->year}' AND ";
        $query .= "     SCHOOL_SEQ = '005' ";

        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);

        if (is_array($row)) {
            $data["SCHOOL_REMARK1"][TEXT] = $model->field["SYUKESSEKI_HANTEI_HOU"];
            $data["REGISTERCD"][TEXT]     = STAFFCD;
            $data["UPDATED"][FUNC]        = "SYSDATE()";

            $where  = " WHERE ";
            $where .= "     YEAR = '{$model->year}' AND ";
            $where .= "     SCHOOL_SEQ = '005' ";

            $query = Query::updateSQL($data, "SCHOOL_DETAIL_DAT", $where);
            $db->query($query);
        } else {
            $data = array();
            $data["YEAR"][TEXT]            = $model->year;
            $data["SCHOOL_SEQ"][TEXT]      = "005";
            $data["SCHOOL_REMARK1"][TEXT]  = $model->field["SYUKESSEKI_HANTEI_HOU"];
            $data["REGISTERCD"][TEXT]      = STAFFCD;
            $data["UPDATED"][FUNC]         = "sysdate()";

            $query = Query::insertSQL($data, "SCHOOL_DETAIL_DAT");
            $db->query($query);
        }
    }

    /***************************************************
      学期マスタ更新(SEMESERDIVの数だけレコードを作成)
     ***************************************************/
    function updateSemesterMst($semesterdiv, $year) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $query  = " SELECT COUNT(*) FROM SEMESTER_MST";
        $query .= " WHERE YEAR = '". $year ."'";
        $query .= "   AND SEMESTER <> '9'";

        $cur_num = $db->getOne($query);

        //テーブルにデータがない場合すべての学期数のレコードを作成
        if ($cur_num == 0) {
            for ($i = 1;$i <= $semesterdiv; $i++) {
                $query  = "INSERT INTO SEMESTER_MST (YEAR, SEMESTER, REGISTERCD, UPDATED) ";
                $query .= " VALUES ('". $year . "','".$i."','".STAFFCD."',sysdate())";
                $db->query($query);
            }
            $query  = "INSERT INTO SEMESTER_MST (YEAR, SEMESTER, REGISTERCD, UPDATED) ";
            $query .= " VALUES ('". $year . "','9','".STAFFCD."',sysdate())";
            $db->query($query);

        //学期数が既存学期数より多い場合はレコードを追加
        } elseif ($cur_num < $semesterdiv) {
            for ($i = $cur_num+1;$i <= $semesterdiv; $i++) {
                $query  = "INSERT INTO SEMESTER_MST (YEAR, SEMESTER, REGISTERCD, UPDATED) ";
                $query .= " VALUES ('". $year . "','".$i."','".STAFFCD."',sysdate())";
                $db->query($query);
            }
        //学期数が既存学期数より少ない場合はレコードを削除
        } elseif ($cur_num > $semesterdiv) {
            for ($i = $semesterdiv+1;$i <= $cur_num; $i++) {
                $query  = "DELETE FROM SEMESTER_MST";
                $query .= " WHERE YEAR = '". $year ."' AND SEMESTER = '" .$i ."'";
                $db->query($query);
            }
        }

        $db->commit();
        Query::dbCheckIn($db);
    }
}
?>
