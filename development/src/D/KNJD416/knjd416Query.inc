<?php

require_once('for_php7.php');

class knjd416Query extends Query {
    //学年取得
    function getGrade($model) {
        $query  = " SELECT ";
        $query .= "     GRADE AS VALUE, ";
        $query .= "     GRADE_NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".CTRL_YEAR."' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " AND SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        } else if ($model->Properties["useSchool_KindField"] == "1") {
            $query .= " AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //校種取得
    function getSchoolKind($grade) {
        $query  = " SELECT ";
        $query .= "     SCHOOL_KIND ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT ";
        $query .= " WHERE ";
        $query .= "         YEAR  = '".CTRL_YEAR."' ";
        $query .= "     AND GRADE = '".$grade."' ";

        return $query;
    }

    //学年取得
    function getNameMst($model, $namecd1) {
        $query  = " SELECT ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     NAMECD2 || ':' || VALUE(NAME1,'') AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR    = '".CTRL_YEAR."' AND ";
        $query .= "     NAMECD1 = '".$namecd1."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //年組取得
    function getHrClass($model) {
        $query  = " SELECT ";
        $query .= "     GRADE || HR_CLASS AS VALUE, ";
        $query .= "     HR_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".CTRL_YEAR."' AND ";
        $query .= "     SEMESTER    = '".CTRL_SEMESTER."' AND ";
        $query .= "     GRADE       = '".$model->field["GRADE"]."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //生徒取得
    function getSchreg($model) {
        $query  = " SELECT ";
        $query .= "     T1.SCHREGNO AS VALUE, ";
        $query .= "     T1.SCHREGNO || ':' || T2.NAME AS LABEL, ";
        $query .= "     T1.SCHREGNO ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= "     INNER JOIN SCHREG_BASE_MST T2 ";
        $query .= "        ON T1.SCHREGNO = T2.SCHREGNO ";
        $query .= "     INNER JOIN SCHREG_REGD_HDAT T3 ";
        $query .= "        ON T1.YEAR     = T3.YEAR ";
        $query .= "       AND T1.SEMESTER = T3.SEMESTER ";
        $query .= "       AND T1.GRADE    = T3.GRADE ";
        $query .= "       AND T1.HR_CLASS = T3.HR_CLASS ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR                     = '".CTRL_YEAR."' ";
        $query .= "     AND T1.SEMESTER             = '".CTRL_SEMESTER."' ";
        $query .= "     AND T1.GRADE                = '".$model->field["GRADE"]."' ";
        $query .= "     AND T1.GRADE || T1.HR_CLASS = '".$model->field["GRADE_HR_CLASS"]."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //生徒一覧取得
    function getSchList($model, $flg) {
        $query  = " SELECT ";
        $query .= "     T1.GRADE || T1.HR_CLASS || T1.ATTENDNO || ':' || T1.SCHREGNO AS VALUE, ";
        $query .= "     T3.HR_NAME || ' ' || T1.ATTENDNO || '番 ' || T2.NAME_SHOW AS LABEL, ";
        $query .= "     T1.SCHREGNO ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= "     INNER JOIN SCHREG_BASE_MST T2 ON T1.SCHREGNO = T2.SCHREGNO ";
        $query .= "     INNER JOIN SCHREG_REGD_HDAT T3 ";
        $query .= "          ON T1.YEAR     = T3.YEAR ";
        $query .= "         AND T1.SEMESTER = T3.SEMESTER ";
        $query .= "         AND T1.GRADE    = T3.GRADE ";
        $query .= "         AND T1.HR_CLASS = T3.HR_CLASS ";
        if ($flg == "left") {
            $query .= "     INNER JOIN SCHREG_BASE_DETAIL_MST T4 ";
            $query .= "          ON T4.SCHREGNO     = T1.SCHREGNO ";
            $query .= "         AND T4.BASE_SEQ     = '003' ";
            $query .= "         AND T4.BASE_REMARK2 = '".$model->field["TESTDIV"]."' ";
        }
        $query .= " WHERE ";
        $query .= "     T1.YEAR         = '".CTRL_YEAR."' AND ";
        $query .= "     T1.SEMESTER     = '".CTRL_SEMESTER."' AND ";
        $query .= "     T1.GRADE        = '".$model->field["GRADE"]."' ";
        if ($flg == "right") {
            $query .= " AND T1.GRADE || T1.HR_CLASS = '".$model->field["GRADE_HR_CLASS"]."' ";
        }
        if ($flg == "select") {
            $query .= " AND T1.SCHREGNO IN ('".implode(explode(',', $model->selectdata),"','")."') ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //指導一覧取得
    function getHreportList($model, $flg) {
        if($model->type == "0" && $flg == "right"){
            //基本かつ右リストの取得時
            $query  = " SELECT ";
            $query .= "     T1.NAMECD2 AS VALUE, ";
            $query .= "     T1.NAME1   AS LABEL  ";
            $query .= " FROM ";
            $query .= "     V_NAME_MST T1 ";
            $query .= " WHERE ";
            $query .= "     T1.YEAR        = '".CTRL_YEAR."' ";
            $query .= "     AND T1.NAMECD1 = 'D090' ";
            $query .= " ORDER BY ";
            $query .= "     VALUE ";
        }else{
            $query  = " SELECT ";
            if($flg == "count"){
                $query .= "     COUNT(T1.KIND_NO) AS VALUE ";
            }else{
                $query .= "     T1.KIND_NO AS VALUE, ";
                $query .= "     T2.NAME1   AS LABEL  ";
            }
            $query .= " FROM ";
            $query .= "     HREPORT_GUIDANCE_KIND_DAT T1 ";
            $query .= "     LEFT JOIN V_NAME_MST T2";
            $query .= "       ON T2.YEAR     = T1.YEAR ";
            $query .= "      AND T2.NAMECD2  = T1.KIND_NO ";
            $query .= "      AND T2.NAMECD1  = 'D090' ";
            $query .= " WHERE ";
            $query .= "     T1.YEAR          = '".CTRL_YEAR."' ";
            if($model->type == "0"){
                //基本(左リスト)
                $query .= "     AND T1.GRADE    = '00' ";
                $query .= "     AND T1.HR_CLASS = '000' ";
                $query .= "     AND T1.SCHREGNO = '00000000' ";
            }else if($model->type == "1"){
                //学年別
                if ($flg == "left") {
                    $query .= "     AND T1.GRADE    = '".$model->field["GRADE"]."' ";
                    $query .= "     AND T1.HR_CLASS = '000' ";
                    $query .= "     AND T1.SCHREGNO = '00000000' ";
                }
                if ($flg == "right") {
                    $query .= "     AND T1.GRADE    = '00' ";
                    $query .= "     AND T1.HR_CLASS = '000' ";
                    $query .= "     AND T1.SCHREGNO = '00000000' ";
                }
            }else if($model->type == "2"){
                //個人別
                if ($flg == "left") {
                    $query .= "     AND T1.GRADE                = '".$model->field["GRADE"]."' ";
                    $query .= "     AND T1.GRADE || T1.HR_CLASS = '".$model->field["GRADE_HR_CLASS"]."' ";
                    $query .= "     AND T1.SCHREGNO             = '".$model->field["SCHREGNO"]."' ";
                }
                if ($flg == "right") {
                    $query .= "     AND T1.GRADE                = '00' ";
                    $query .= "     AND T1.GRADE || T1.HR_CLASS = '00000' ";
                    $query .= "     AND T1.SCHREGNO             = '00000000' ";
                }
            }
            $query .= " ORDER BY ";
            if ($flg == "left") {
                $query .= "     T1.SHOWORDER, ";
            }
            $query .= "     VALUE ";
            
        }
        return $query;
    }

    //UPDATE
    function &getUpdateQuery(&$model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $grade    = '00';
        $hrClass  = '000';
        $schregNo = '00000000';

        if($model->type == "1"){
            //学年別
            $grade    = $model->field["GRADE"];
        }else if($model->type == "2"){
            //個人別
            $grade    = $model->field["GRADE"];
            $hrClass  = substr($model->field["GRADE_HR_CLASS"],2,5);
            $schregNo = $model->field["SCHREGNO"];
        }

        //削除
        $selectdata = explode(",", $model->selectdata);
        $query  = " DELETE FROM ";
        $query .= "     HREPORT_GUIDANCE_KIND_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND GRADE    = '".$grade."' ";
        $query .= "     AND HR_CLASS = '".$hrClass."' ";
        $query .= "     AND SCHREGNO = '".$schregNo."' ";

        $db->query($query);

        if($model->type == "0"){
            //基本の場合、学年別・個人別のレコードも削除する
            $query  = " DELETE FROM ";
            $query .= "     HREPORT_GUIDANCE_KIND_DAT ";
            $query .= " WHERE ";
            $query .= "     YEAR            =  '".CTRL_YEAR."' ";
            $query .= "     AND GRADE       <> '".$grade."' ";
            if ($selectdata[0] != "") {
                $query .= "     AND KIND_NO NOT IN ('".implode(explode(',', $model->selectdata),"','")."') ";
            }

            $db->query($query);
        }
        //登録
        if ($selectdata[0] != "") {
            for ($i = 0; $i < get_count($selectdata); $i++) {
                $kindNo = $selectdata[$i];

                $data = array();
                $data["YEAR"][TEXT]         = CTRL_YEAR;
                $data["GRADE"][TEXT]        = $grade;
                $data["HR_CLASS"][TEXT]     = $hrClass;
                $data["SCHREGNO"][TEXT]     = $schregNo;
                $data["KIND_NO"][TEXT]      = $kindNo;
                $data["SHOWORDER"][NUMBER]  = $i + 1;
                $data["REGISTERCD"][TEXT]   = STAFFCD;
                $data["UPDATED"][NUMBER]    = "sysdate()";
                $db->query(Query::insertSQL($data, "HREPORT_GUIDANCE_KIND_DAT"));
            }
        }

        $db->commit();
        Query::dbCheckIn($db);
        return;
    }

    //前年度データ取得
    function getCopyData($year, $flg) {
        $query  =  " SELECT ";
        if ($flg == "cnt") {
            $query .=  "    COUNT(*) ";
        } else {
            $query .=  "    * ";
        }
        $query .=  " FROM ";
        $query .=  "    HREPORT_GUIDANCE_KIND_DAT ";
        $query .=  " WHERE ";
        $query .=  "    YEAR = '".$year."' ";

        return $query;
    }

    //前年度コピー処理
    function &getCopyQuery($year, $pre_year) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //削除
        $query  =  " DELETE FROM ";
        $query .=  "    HREPORT_GUIDANCE_KIND_DAT ";
        $query .=  " WHERE ";
        $query .=  "    YEAR = '".$year."' ";
        $db->query($query);

        //前年度データ取得
        $query = knjd416Query::getCopyData($pre_year, "list");
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //追加
            $data = array();
            $data["YEAR"][TEXT]         = CTRL_YEAR;
            $data["GRADE"][TEXT]        = $row["GRADE"];
            $data["HR_CLASS"][TEXT]     = $row["HR_CLASS"];
            $data["SCHREGNO"][TEXT]     = $row["SCHREGNO"];
            $data["KIND_NO"][TEXT]      = $row["KIND_NO"];
            $data["SHOWORDER"][NUMBER]  = $row["SHOWORDER"];
            $data["REGISTERCD"][TEXT]   = STAFFCD;
            $data["UPDATED"][NUMBER]    = "sysdate()";

            $query = Query::insertSQL($data, "HREPORT_GUIDANCE_KIND_DAT");
            $db->query($query);
        }

        $db->commit();
        Query::dbCheckIn($db);
    }
}
?>
