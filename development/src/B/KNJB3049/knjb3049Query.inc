<?php

require_once('for_php7.php');

class knjb3049query extends Query {

    /******************/
    /**  画面で使用  **/
    /******************/

    //年度取得
    function getYear($model) {
        $query  = " SELECT DISTINCT ";
        $query .= "     YEAR AS VALUE, ";
        $query .= "     YEAR AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHOOL_MST ";
        $query .= " WHERE ";
        $query .= "     SCHOOLCD = '".SCHOOLCD."' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " AND SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        } else {
            $query .= " AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE DESC ";

        return $query;
    }

    //学期取得
    function getSemester($model, $semester="") {
        $query  = " SELECT ";
        $query .= "     SEMESTERNAME AS LABEL, ";
        $query .= "     SEMESTER AS VALUE ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR      = '".$model->field["YEAR"]."' AND ";
        if ($semester) {
            $query .= "     SEMESTER  = '".$semester."' ";
        } else {
            $query .= "     SEMESTER <> '9' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //基本時間割取得
    function getBscseq($model) {
        $query  = " SELECT ";
        $query .= "     BSCSEQ AS VALUE, ";
        $query .= "     RTRIM(CHAR(BSCSEQ)) || ':' || TITLE AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCH_PTRN_HDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".$model->field["YEAR"]."' AND ";
        $query .= "     SEMESTER    = '".$model->field["SEMESTER"]."' ";

        return $query;
    }


    /**********************/
    /**  ＣＳＶ取込処理  **/
    /**********************/

    function updateQueryCsv($model, &$data_arr) {
        $data = array();
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $cnt = 0;   //処理件数
        $subclass_cnt = array();
        for ($i = 0; $i < get_count($data_arr); $i++) {

            //基本時間割SEQ
            $bscseq = $model->field["BSCSEQ"];
            if ($model->field["BSCSEQ"] == "NEW") {
                $maxBscseq = $db->getOne(knjb3049Query::getMaxBscseq($model));
                $bscseq    = ($maxBscseq) ? $maxBscseq+1 : 1;
            }

            //曜日コード
            $weekArray = array('月' => "2", '火' => "3", '水' => "4", '木' => "5", '金' => "6", '土' => "7", '日' => "1");
            $daycd = "";
            foreach ($weekArray as $str => $cd) {
                if ($daycd) break;
                if (preg_match("/{$str}/", $data_arr[$i]["WEEK"])) {
                    $daycd = $cd;
                }
            }

            //講座コード
            $chaircd = $data_arr[$i]["SUBCLASSCD"].$data_arr[$i]["FACCD"];

            //科目コード
            $subclasscd = $data_arr[$i]["SUBCLASSCD"].'00';
            if (!$subclass_cnt[$subclasscd]) $subclass_cnt[$subclasscd] = 1;

            //データ件数取得
            $chair_cnt  = $db->getOne(knjb3049Query::cntChairDat($model, $chaircd));
            $stf_cnt    = $db->getOne(knjb3049Query::cntChairStfDat($model, $chaircd, $data_arr[$i]["STAFFCD"]));

            //DELETE -- SCH_PTRN_DAT
            $query  = " DELETE FROM ";
            $query .= "     SCH_PTRN_DAT ";
            $query .= " WHERE ";
            $query .= "     YEAR        = '".$model->field["YEAR"]."' AND ";
            $query .= "     SEMESTER    = '".$model->field["SEMESTER"]."' AND ";
            $query .= "     BSCSEQ      =  ".$bscseq." AND ";
            $query .= "     DAYCD       = '".$daycd."' AND ";
            $query .= "     PERIODCD    = '".$data_arr[$i]["PERIODCD"]."' AND ";
            $query .= "     CHAIRCD     = '".$chaircd."' ";
            $db->query($query);

            //INSERT -- SCH_PTRN_DAT
            $data = array();
            $data["YEAR"][TEXT]             = $model->field["YEAR"];
            $data["SEMESTER"][TEXT]         = $model->field["SEMESTER"];
            $data["BSCSEQ"][NUMBER]         = $bscseq;
            $data["DAYCD"][TEXT]            = $daycd;
            $data["PERIODCD"][TEXT]         = $data_arr[$i]["PERIODCD"];
            $data["CHAIRCD"][TEXT]          = $chaircd;
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][NUMBER]        = "SYSDATE()";

            $query = Query::insertSQL($data, "SCH_PTRN_DAT");
            $db->query($query);

            if (!$chair_cnt) {
                $subcls = $db->getRow(knjb3049Query::getSubclassMst($model, $subclasscd), DB_FETCHMODE_ASSOC);

                //INSERT -- CHAIR_DAT
                $data = array();
                $data["YEAR"][TEXT]             = $model->field["YEAR"];
                $data["SEMESTER"][TEXT]         = $model->field["SEMESTER"];
                $data["CHAIRCD"][TEXT]          = $chaircd;
                $data["GROUPCD"][TEXT]          = '0000';
                $data["CLASSCD"][TEXT]          = substr($subclasscd,0,2);
                $data["SCHOOL_KIND"][TEXT]      = 'H';
                $data["CURRICULUM_CD"][TEXT]    = $subcls["CURRICULUM_CD"];
                $data["SUBCLASSCD"][TEXT]       = $subclasscd;
                $data["CHAIRNAME"][TEXT]        = (strlen($subcls["SUBCLASSNAME"]) > 27 ? $subcls["SUBCLASSABBV"] : $subcls["SUBCLASSNAME"]).$subclass_cnt[$subclasscd];
                $data["COUNTFLG"][TEXT]         = '1';
                $data["REGISTERCD"][TEXT]       = STAFFCD;
                $data["UPDATED"][NUMBER]        = "SYSDATE()";

                $query = Query::insertSQL($data, "CHAIR_DAT");
                $db->query($query);
                $subclass_cnt[$subclasscd]++;
            }

            if (!$stf_cnt) {
                //INSERT -- CHAIR_STF_DAT
                $data = array();
                $data["YEAR"][TEXT]             = $model->field["YEAR"];
                $data["SEMESTER"][TEXT]         = $model->field["SEMESTER"];
                $data["CHAIRCD"][TEXT]          = $chaircd;
                $data["STAFFCD"][TEXT]          = $data_arr[$i]["STAFFCD"];
                $data["CHARGEDIV"][NUMBER]      = 1;
                $data["REGISTERCD"][TEXT]       = STAFFCD;
                $data["UPDATED"][NUMBER]        = "SYSDATE()";

                $query = Query::insertSQL($data, "CHAIR_STF_DAT");
                $db->query($query);
            }
            $cnt++;
        }

        //INSERT -- SCH_PTRN_HDAT
        if ($model->field["BSCSEQ"] == "NEW" && $cnt > 0) {
            $data = array();
            $data["YEAR"][TEXT]         = $model->field["YEAR"];
            $data["SEMESTER"][TEXT]     = $model->field["SEMESTER"];
            $data["BSCSEQ"][NUMBER]     = $bscseq;
            $data["TITLE"][TEXT]        = $model->field["TITLE"];
            $data["REGISTERCD"][TEXT]   = STAFFCD;
            $data["UPDATED"][NUMBER]    = "SYSDATE()";

            $query = Query::insertSQL($data, "SCH_PTRN_HDAT");
            $db->query($query);
        }

        $db->commit();
        Query::dbCheckIn($db);

        return array($cnt, $bscseq);
    }


    /**********************/
    /**  存在チェック用  **/
    /**********************/

    //校時コード取得
    function getPeriodcd($model) {
        $query  = " SELECT ";
        $query .= "     NAMECD2 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR    = '".$model->field["YEAR"]."' AND ";
        $query .= "     NAMECD1 = 'B001' ";

        return $query;
    }

    //科目コード取得
    function getSubclasscd($model) {
        $query  = " SELECT ";
        $query .= "     SUBCLASSCD ";
        $query .= " FROM ";
        $query .= "     V_SUBCLASS_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR    = '".$model->field["YEAR"]."' AND ";
        $query .= "     CLASSCD = SUBSTR(SUBCLASSCD,1,2) AND ";
        $query .= "     SCHOOL_KIND = 'H' ";

        return $query;
    }

    //職員コード取得
    function getStaffcd($model) {
        $query  = " SELECT ";
        $query .= "     STAFFCD ";
        $query .= " FROM ";
        $query .= "     V_STAFF_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$model->field["YEAR"]."' ";

        return $query;
    }


    /********************/
    /**  更新時に使用  **/
    /********************/

    //件数取得 -- CHAIR_DAT
    function cntChairDat($model, $chaircd) {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     CHAIR_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".$model->field["YEAR"]."' AND ";
        $query .= "     SEMESTER    = '".$model->field["SEMESTER"]."' AND ";
        $query .= "     CHAIRCD     = '".$chaircd."' ";

        return $query;
    }

    //件数取得 -- CHAIR_STF_DAT
    function cntChairStfDat($model, $chaircd, $staffcd) {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     CHAIR_STF_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".$model->field["YEAR"]."' AND ";
        $query .= "     SEMESTER    = '".$model->field["SEMESTER"]."' AND ";
        $query .= "     CHAIRCD     = '".$chaircd."' AND ";
        $query .= "     STAFFCD     = '".$staffcd."' ";

        return $query;
    }

    //MAX値取得
    function getMaxBscseq($model) {
        $query  = " SELECT ";
        $query .= "     MAX(BSCSEQ) ";
        $query .= " FROM ";
        $query .= "     SCH_PTRN_HDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".$model->field["YEAR"]."' AND ";
        $query .= "     SEMESTER    = '".$model->field["SEMESTER"]."' ";

        return $query;
    }

    //科目マスタから取得
    function getSubclassMst($model, $subclasscd) {
        $query  = " SELECT ";
        $query .= "     CURRICULUM_CD, ";
        $query .= "     SUBCLASSNAME, ";
        $query .= "     SUBCLASSABBV ";
        $query .= " FROM ";
        $query .= "     V_SUBCLASS_MST T1 ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".$model->field["YEAR"]."' AND ";
        $query .= "     CLASSCD     = '".substr($subclasscd,0,2)."' AND ";
        $query .= "     SCHOOL_KIND = 'H' AND ";
        $query .= "     SUBCLASSCD  = '".$subclasscd."' AND ";
        $query .= "     CURRICULUM_CD IN (  SELECT ";
        $query .= "                             MAX(CURRICULUM_CD) ";
        $query .= "                         FROM ";
        $query .= "                             V_SUBCLASS_MST S1 ";
        $query .= "                         WHERE ";
        $query .= "                             T1.YEAR         = S1.YEAR AND ";
        $query .= "                             T1.CLASSCD      = S1.CLASSCD AND ";
        $query .= "                             T1.SCHOOL_KIND  = S1.SCHOOL_KIND AND ";
        $query .= "                             T1.SUBCLASSCD   = S1.SUBCLASSCD ";
        $query .= "                         ) ";

        return $query;
    }


    /********************/
    /**  エラー処理用  **/
    /********************/

    //エラーＤＢへの追加
    function insertQueryErr(&$db, $record_no, $check_error) {
        $data1 = array();
        $data1["PROGRAMID"][TEXT]   = PROGRAMID;
        $data1["MSGROW"][NUMBER]    = $record_no;
        $data1["MSGREMARK"][TEXT]   = $check_error;

        $query = Query::insertSQL($data1, "W_CSVMSG_PRG_DAT");
        $db->query($query);
    }
}
?>
