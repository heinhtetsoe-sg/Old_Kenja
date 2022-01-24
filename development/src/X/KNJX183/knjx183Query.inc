<?php

require_once('for_php7.php');

class knjx183query extends Query {
    /******************/
    /**  画面で使用  **/
    /******************/
    function getSecurityHigh() {
        $query  = " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     MENU_HIGH_SECURITY_MST ";
        $query .= " WHERE ";
        $query .= "     PROGRAMID = 'KNJX183' ";
        $query .= "     AND INVALID_FLG = '0' ";

        return $query;
    }

    //年度一覧
    function getYearSeme() {
        $query  = " SELECT DISTINCT ";
        $query .= "     T1.YEAR || T1.SEMESTER  AS VALUE, ";
        $query .= "     T1.YEAR || '年度 ' || T2.SEMESTERNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT T1 ";
        $query .= "     INNER JOIN SEMESTER_MST T2 ON T1.YEAR     = T2.YEAR ";
        $query .= "                               AND T1.SEMESTER = T2.SEMESTER ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //年組一覧
    function getGradeHrClass($model) {
        $query  = " SELECT DISTINCT ";
        $query .= "     HDAT.GRADE || HDAT.HR_CLASS AS VALUE, ";
        $query .= "     HDAT.HR_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT HDAT ";
        $query .= "     LEFT JOIN SCHREG_REGD_GDAT GDAT ON GDAT.YEAR  = HDAT.YEAR ";
        $query .= "                                    AND GDAT.GRADE = HDAT.GRADE ";
        $query .= " WHERE ";
        $query .= "         HDAT.YEAR || HDAT.SEMESTER = '".$model->field["YEAR"]."' ";
        $query .= "     AND GDAT.SCHOOL_KIND = 'P' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    /**********************/
    /**  存在チェック用  **/
    /**********************/
    //学籍番号
    function schregNoCntSql($schregno) {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT ";
        $query .= " WHERE ";
        $query .= "         YEAR     = '".CTRL_YEAR."' ";
        $query .= "     AND SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= "     AND SCHREGNO = '{$schregno}' ";

        return $query;
    }

    //名称マスタ登録コードチェック
    function nameCntSql($namecd, $setcd, $where="") {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR    = '".CTRL_YEAR."' ";
        $query .= "     AND NAMECD1 = '{$namecd}' ";
        $query .= "     AND NAMECD2 = '{$setcd}' ";
        if ($where) {
            $query .= $where;
        }

        return $query;
    }

    //教科コード
    function classcdCntSql($classcd) {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     CLASS_MST ";
        $query .= " WHERE ";
        $query .= "         CLASSCD     = '{$classcd}' ";
        $query .= "     AND SCHOOL_KIND = 'P' ";

        return $query;
    }

    //科目コード
    function subclassCntSql($data, $subclassCd) {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     SUBCLASS_MST ";
        $query .= " WHERE ";
        $query .= "         SUBCLASSCD      = '{$subclassCd}' ";
        $query .= "     AND CLASSCD         = '{$data["CLASSCD"]}' ";
        $query .= "     AND SCHOOL_KIND     = 'P' ";
        $query .= "     AND CURRICULUM_CD   = '{$data["CURRICULUM_CD"]}' ";

        return $query;
    }

    /************************/
    /**  ＣＳＶ取込で使用  **/
    /************************/
    //科目コード
    function studyrecDetailDatCntSql($data_arr, $seq) {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     SCHREG_STUDYREC_DETAIL_DAT ";
        $query .= " WHERE ";
        $query .= "         SCHOOLCD      = '".$data_arr["SCHOOLCD"]."' ";
        $query .= "     AND YEAR          = '".$data_arr["YEAR"]."' ";
        $query .= "     AND SCHREGNO      = '".$data_arr["SCHREGNO"]."' ";
        $query .= "     AND CLASSCD       = '".$data_arr["CLASSCD"]."' ";
        $query .= "     AND SCHOOL_KIND   = '".$data_arr["SCHOOL_KIND"]."' ";
        $query .= "     AND CURRICULUM_CD = '".$data_arr["CURRICULUM_CD"]."' ";
        $query .= "     AND SUBCLASSCD    = '".$data_arr["SUBCLASSCD"]."' ";
        $query .= "     AND SEQ           = '{$seq}' ";

        return $query;
    }

    //年次の取得
    function getAnnual($schregno) {
        $query  = " SELECT ";
        $query .= "     ANNUAL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT ";
        $query .= " WHERE ";
        $query .= "         SCHREGNO = '".$schregno."' ";
        $query .= "     AND YEAR     = '".CTRL_YEAR."' ";
        $query .= "     AND SEMESTER = '".CTRL_SEMESTER."' ";

        return $query;
    }

    //データの追加（ＣＳＶデータより読込）
    function insertQueryCsv($model, &$data_arr) {
        $data = array();

        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $cnt = 0;   //処理件数
        for ($i = 0; $i < get_count($data_arr); $i++) {
            //知的障害
            if ($model->field["TAISYOU"] == "1") {
                //学習
                if ($model->field["PAGEDIV"] == "1") {
                    //SCHREG_STUDYREC_DETAIL_DAT
                    $data = array();
                    $data["SCHOOLCD"][TEXT]             = $data_arr[$i]["SCHOOLCD"];
                    $data["YEAR"][TEXT]                 = $data_arr[$i]["YEAR"];
                    $data["SCHREGNO"][TEXT]             = $data_arr[$i]["SCHREGNO"];
                    $data["CLASSCD"][TEXT]              = $data_arr[$i]["CLASSCD"];
                    $data["SCHOOL_KIND"][TEXT]          = $data_arr[$i]["SCHOOL_KIND"];
                    $data["CURRICULUM_CD"][TEXT]        = $data_arr[$i]["CURRICULUM_CD"];
                    $data["SUBCLASSCD"][TEXT]           = $data_arr[$i]["SUBCLASSCD"];
                    $data["SEQ"][TEXT]                  = "001";
                    $data["REMARK1"][TEXT]              = $data_arr[$i]["REMARK1"];
                    $data["REGISTERCD"][TEXT]           = STAFFCD;
                    $data["UPDATED"][NUMBER]            = "SYSDATE()";

                    if (1 > $db->getOne(knjx183Query::studyrecDetailDatCntSql($data_arr[$i], "001"))) {
                        $query = Query::insertSQL($data, "SCHREG_STUDYREC_DETAIL_DAT");
                    } else {
                        $where  = " WHERE SCHOOLCD      = '".$data_arr[$i]["SCHOOLCD"]."' ";
                        $where .= "   AND YEAR          = '".$data_arr[$i]["YEAR"]."' ";
                        $where .= "   AND SCHREGNO      = '".$data_arr[$i]["SCHREGNO"]."' ";
                        $where .= "   AND CLASSCD       = '".$data_arr[$i]["CLASSCD"]."' ";
                        $where .= "   AND SCHOOL_KIND   = '".$data_arr[$i]["SCHOOL_KIND"]."' ";
                        $where .= "   AND CURRICULUM_CD = '".$data_arr[$i]["CURRICULUM_CD"]."' ";
                        $where .= "   AND SUBCLASSCD    = '".$data_arr[$i]["SUBCLASSCD"]."' ";
                        $where .= "   AND SEQ           = '001' ";

                        $query = Query::updateSQL($data, "SCHREG_STUDYREC_DETAIL_DAT", $where);
                    }
                    $db->query($query);
                } else {
                //所見
                    //入学時の障害の状態
                    if ($model->field["DATADIV"] == "1") {
                        //HTRAINREMARK_DETAIL2_HDAT
                        $data = array();
                        $data["SCHREGNO"][TEXT]             = $data_arr[$i]["SCHREGNO"];
                        $data["HTRAIN_SEQ"][TEXT]           = "001";
                        $data["REMARK1"][TEXT]              = $data_arr[$i]["ENT_DISABILITY_REMARK"];
                        $data["REGISTERCD"][TEXT]           = STAFFCD;
                        $data["UPDATED"][NUMBER]            = "SYSDATE()";

                        if (1 > $db->getOne("SELECT COUNT(*) FROM HTRAINREMARK_DETAIL2_HDAT WHERE SCHREGNO = '".$data_arr[$i]["SCHREGNO"]."' AND HTRAIN_SEQ = '001'")) {
                            $query = Query::insertSQL($data, "HTRAINREMARK_DETAIL2_HDAT");
                        } else {
                            $where  = " WHERE SCHREGNO      = '".$data_arr[$i]["SCHREGNO"]."' ";
                            $where .= "   AND HTRAIN_SEQ    = '001' ";

                            $query = Query::updateSQL($data, "HTRAINREMARK_DETAIL2_HDAT", $where);
                        }
                        $db->query($query);
                    } else {
                    //毎年度データ
                        //HTRAINREMARK_DETAIL2_DAT
                        $data = array();
                        $data["YEAR"][TEXT]                 = $data_arr[$i]["YEAR"];
                        $data["SCHREGNO"][TEXT]             = $data_arr[$i]["SCHREGNO"];
                        $data["HTRAIN_SEQ"][TEXT]           = "002";
                        $data["REMARK1"][TEXT]              = $data_arr[$i]["BEHAVEREC_REMARK"];
                        $data["REGISTERCD"][TEXT]           = STAFFCD;
                        $data["UPDATED"][NUMBER]            = "SYSDATE()";

                        if (1 > $db->getOne("SELECT COUNT(*) FROM HTRAINREMARK_DETAIL2_DAT WHERE YEAR = '".$data_arr[$i]["YEAR"]."' AND SCHREGNO = '".$data_arr[$i]["SCHREGNO"]."' AND HTRAIN_SEQ = '002'")) {
                            $query = Query::insertSQL($data, "HTRAINREMARK_DETAIL2_DAT");
                        } else {
                            $where  = " WHERE YEAR          = '".$data_arr[$i]["YEAR"]."' ";
                            $where .= "   AND SCHREGNO      = '".$data_arr[$i]["SCHREGNO"]."' ";
                            $where .= "   AND HTRAIN_SEQ    = '002' ";

                            $query = Query::updateSQL($data, "HTRAINREMARK_DETAIL2_DAT", $where);
                        }
                        $db->query($query);

                        //年次の取得
                        $annual = $db->getOne(knjx183Query::getAnnual($data_arr[$i]["SCHREGNO"]));
                        //HTRAINREMARK_P_DAT
                        $data = array();
                        $data["YEAR"][TEXT]                 = $data_arr[$i]["YEAR"];
                        $data["SCHREGNO"][TEXT]             = $data_arr[$i]["SCHREGNO"];
                        $data["ANNUAL"][TEXT]               = $annual;
                        $data["TOTALREMARK"][TEXT]          = $data_arr[$i]["TOTALREMARK"];
                        $data["ATTENDREC_REMARK"][TEXT]     = $data_arr[$i]["ATTENDREC_REMARK"];
                        $data["REGISTERCD"][TEXT]           = STAFFCD;
                        $data["UPDATED"][NUMBER]            = "SYSDATE()";

                        if (1 > $db->getOne("SELECT COUNT(*) FROM HTRAINREMARK_P_DAT WHERE YEAR = '".$data_arr[$i]["YEAR"]."' AND SCHREGNO = '".$data_arr[$i]["SCHREGNO"]."'")) {
                            $query = Query::insertSQL($data, "HTRAINREMARK_P_DAT");
                        } else {
                            $where  = " WHERE YEAR          = '".$data_arr[$i]["YEAR"]."' ";
                            $where .= "   AND SCHREGNO      = '".$data_arr[$i]["SCHREGNO"]."' ";

                            $query = Query::updateSQL($data, "HTRAINREMARK_P_DAT", $where);
                        }
                        $db->query($query);
                    }
                }
            } else {
            //知的障害以外(所見のみ)
                //入学時の障害の状態
                if ($model->field["DATADIV"] == "1") {
                    //HTRAINREMARK_DETAIL2_HDAT
                    $data = array();
                    $data["SCHREGNO"][TEXT]             = $data_arr[$i]["SCHREGNO"];
                    $data["HTRAIN_SEQ"][TEXT]           = "001";
                    $data["REMARK1"][TEXT]              = $data_arr[$i]["ENT_DISABILITY_REMARK"];
                    $data["REGISTERCD"][TEXT]           = STAFFCD;
                    $data["UPDATED"][NUMBER]            = "SYSDATE()";

                    if (1 > $db->getOne("SELECT COUNT(*) FROM HTRAINREMARK_DETAIL2_HDAT WHERE SCHREGNO = '".$data_arr[$i]["SCHREGNO"]."' AND HTRAIN_SEQ = '001'")) {
                        $query = Query::insertSQL($data, "HTRAINREMARK_DETAIL2_HDAT");
                    } else {
                        $where  = " WHERE SCHREGNO      = '".$data_arr[$i]["SCHREGNO"]."' ";
                        $where .= "   AND HTRAIN_SEQ    = '001' ";

                        $query = Query::updateSQL($data, "HTRAINREMARK_DETAIL2_HDAT", $where);
                    }
                    $db->query($query);
                } else {
                //毎年度データ
                    //年次の取得
                    $annual = $db->getOne(knjx183Query::getAnnual($data_arr[$i]["SCHREGNO"]));
                    //HTRAINREMARK_P_DAT
                    $data = array();
                    $data["YEAR"][TEXT]                 = $data_arr[$i]["YEAR"];
                    $data["SCHREGNO"][TEXT]             = $data_arr[$i]["SCHREGNO"];
                    $data["ANNUAL"][TEXT]               = $annual;
                    $data["FOREIGNLANGACT1"][TEXT]      = $data_arr[$i]["FOREIGNLANGACT1"];
                    $data["FOREIGNLANGACT2"][TEXT]      = $data_arr[$i]["FOREIGNLANGACT2"];
                    $data["FOREIGNLANGACT3"][TEXT]      = $data_arr[$i]["FOREIGNLANGACT3"];
                    $data["TOTALSTUDYACT"][TEXT]        = $data_arr[$i]["TOTALSTUDYACT"];
                    $data["VIEWREMARK"][TEXT]           = $data_arr[$i]["VIEWREMARK"];
                    $data["TOTALSTUDYVAL"][TEXT]        = $data_arr[$i]["TOTALSTUDYVAL"];
                    $data["TOTALREMARK"][TEXT]          = $data_arr[$i]["TOTALREMARK"];
                    $data["ATTENDREC_REMARK"][TEXT]     = $data_arr[$i]["ATTENDREC_REMARK"];
                    $data["SPECIALACTREMARK"][TEXT]     = $data_arr[$i]["SPECIALACTREMARK"];
                    $data["REGISTERCD"][TEXT]           = STAFFCD;
                    $data["UPDATED"][NUMBER]            = "SYSDATE()";

                    if (1 > $db->getOne("SELECT COUNT(*) FROM HTRAINREMARK_P_DAT WHERE YEAR = '".$data_arr[$i]["YEAR"]."' AND SCHREGNO = '".$data_arr[$i]["SCHREGNO"]."'")) {
                        $query = Query::insertSQL($data, "HTRAINREMARK_P_DAT");
                    } else {
                        $where  = " WHERE YEAR          = '".$data_arr[$i]["YEAR"]."' ";
                        $where .= "   AND SCHREGNO      = '".$data_arr[$i]["SCHREGNO"]."' ";

                        $query = Query::updateSQL($data, "HTRAINREMARK_P_DAT", $where);
                    }
                    $db->query($query);

                    //HTRAINREMARK_DETAIL2_DAT
                    $data = array();
                    $data["YEAR"][TEXT]                 = $data_arr[$i]["YEAR"];
                    $data["SCHREGNO"][TEXT]             = $data_arr[$i]["SCHREGNO"];
                    $data["HTRAIN_SEQ"][TEXT]           = "001";
                    $data["REMARK1"][TEXT]              = $data_arr[$i]["INDEPENDENT_REMARK"];
                    $data["REGISTERCD"][TEXT]           = STAFFCD;
                    $data["UPDATED"][NUMBER]            = "SYSDATE()";

                    if (1 > $db->getOne("SELECT COUNT(*) FROM HTRAINREMARK_DETAIL2_DAT WHERE YEAR = '".$data_arr[$i]["YEAR"]."' AND SCHREGNO = '".$data_arr[$i]["SCHREGNO"]."' AND HTRAIN_SEQ = '001'")) {
                        $query = Query::insertSQL($data, "HTRAINREMARK_DETAIL2_DAT");
                    } else {
                        $where  = " WHERE YEAR          = '".$data_arr[$i]["YEAR"]."' ";
                        $where .= "   AND SCHREGNO      = '".$data_arr[$i]["SCHREGNO"]."' ";
                        $where .= "   AND HTRAIN_SEQ    = '001' ";

                        $query = Query::updateSQL($data, "HTRAINREMARK_DETAIL2_DAT", $where);
                    }
                    $db->query($query);

                    //BEHAVIOR_DAT
                    foreach ($model->arrayD035 as $key => $val) {
                        $data = array();
                        $data["YEAR"][TEXT]                 = $data_arr[$i]["YEAR"];
                        $data["SCHREGNO"][TEXT]             = $data_arr[$i]["SCHREGNO"];
                        $data["DIV"][TEXT]                  = "3";
                        $data["CODE"][TEXT]                 = $key;
                        $data["ANNUAL"][TEXT]               = $annual;
                        $data["RECORD"][TEXT]               = $data_arr[$i]["RECORD_3".$key];
                        $data["REGISTERCD"][TEXT]           = STAFFCD;
                        $data["UPDATED"][NUMBER]            = "SYSDATE()";

                        if (1 > $db->getOne("SELECT COUNT(*) FROM BEHAVIOR_DAT WHERE YEAR = '".$data_arr[$i]["YEAR"]."' AND SCHREGNO = '".$data_arr[$i]["SCHREGNO"]."' AND DIV = '3' AND CODE = '{$key}'")) {
                            $query = Query::insertSQL($data, "BEHAVIOR_DAT");
                        } else {
                            $where  = " WHERE YEAR          = '".$data_arr[$i]["YEAR"]."' ";
                            $where .= "   AND SCHREGNO      = '".$data_arr[$i]["SCHREGNO"]."' ";
                            $where .= "   AND DIV           = '3' ";
                            $where .= "   AND CODE          = '{$key}' ";

                            $query = Query::updateSQL($data, "BEHAVIOR_DAT", $where);
                        }
                        $db->query($query);
                    }
                    foreach ($model->arrayD034 as $key => $val) {
                        $data = array();
                        $data["YEAR"][TEXT]                 = $data_arr[$i]["YEAR"];
                        $data["SCHREGNO"][TEXT]             = $data_arr[$i]["SCHREGNO"];
                        $data["DIV"][TEXT]                  = "4";
                        $data["CODE"][TEXT]                 = $key;
                        $data["ANNUAL"][TEXT]               = $annual;
                        $data["RECORD"][TEXT]               = $data_arr[$i]["RECORD_4".$key];
                        $data["REGISTERCD"][TEXT]           = STAFFCD;
                        $data["UPDATED"][NUMBER]            = "SYSDATE()";

                        if (1 > $db->getOne("SELECT COUNT(*) FROM BEHAVIOR_DAT WHERE YEAR = '".$data_arr[$i]["YEAR"]."' AND SCHREGNO = '".$data_arr[$i]["SCHREGNO"]."' AND DIV = '4' AND CODE = '{$key}'")) {
                            $query = Query::insertSQL($data, "BEHAVIOR_DAT");
                        } else {
                            $where  = " WHERE YEAR          = '".$data_arr[$i]["YEAR"]."' ";
                            $where .= "   AND SCHREGNO      = '".$data_arr[$i]["SCHREGNO"]."' ";
                            $where .= "   AND DIV           = '4' ";
                            $where .= "   AND CODE          = '{$key}' ";

                            $query = Query::updateSQL($data, "BEHAVIOR_DAT", $where);
                        }
                        $db->query($query);
                    }
                }
            }
            $cnt++;
        }
        $db->commit();
        Query::dbCheckIn($db);

        return $cnt;
    }

    /************************/
    /**  ＣＳＶ削除で使用  **/
    /************************/
    //削除（ＣＳＶデータより読込）
    function DeleteQueryCsv($model,&$data_arr) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $cnt = 0;   //処理件数
        for ($i = 0; $i < get_count($data_arr); $i++) {
            //知的障害
            if ($model->field["TAISYOU"] == "1") {
                //学習
                if ($model->field["PAGEDIV"] == "1") {
                    $query  = " DELETE FROM SCHREG_STUDYREC_DETAIL_DAT ";
                    $query .= " WHERE SCHOOLCD      = '".$data_arr[$i]["SCHOOLCD"]."' ";
                    $query .= "   AND YEAR          = '".$data_arr[$i]["YEAR"]."' ";
                    $query .= "   AND SCHREGNO      = '".$data_arr[$i]["SCHREGNO"]."' ";
                    $query .= "   AND CLASSCD       = '".$data_arr[$i]["CLASSCD"]."' ";
                    $query .= "   AND SCHOOL_KIND   = '".$data_arr[$i]["SCHOOL_KIND"]."' ";
                    $query .= "   AND CURRICULUM_CD = '".$data_arr[$i]["CURRICULUM_CD"]."' ";
                    $query .= "   AND SUBCLASSCD    = '".$data_arr[$i]["SUBCLASSCD"]."' ";
                    $query .= "   AND SEQ           = '001' ";
                    $db->query($query);
                } else {
                //所見
                    //入学時の障害の状態
                    if ($model->field["DATADIV"] == "1") {
                        $query  = " DELETE FROM HTRAINREMARK_DETAIL2_HDAT ";
                        $query .= " WHERE SCHREGNO      = '".$data_arr[$i]["SCHREGNO"]."' ";
                        $query .= "   AND HTRAIN_SEQ    = '001' ";
                        $db->query($query);
                    } else {
                    //毎年度データ
                        $query  = " DELETE FROM HTRAINREMARK_DETAIL2_DAT ";
                        $query .= " WHERE YEAR          = '".$data_arr[$i]["YEAR"]."' ";
                        $query .= "   AND SCHREGNO      = '".$data_arr[$i]["SCHREGNO"]."' ";
                        $query .= "   AND HTRAIN_SEQ    = '002' ";
                        $db->query($query);

                        $query  = " DELETE FROM HTRAINREMARK_P_DAT ";
                        $query .= " WHERE YEAR          = '".$data_arr[$i]["YEAR"]."' ";
                        $query .= "   AND SCHREGNO      = '".$data_arr[$i]["SCHREGNO"]."' ";
                        $db->query($query);
                    }
                }
            } else {
            //知的障害以外(所見のみ)
                //入学時の障害の状態
                if ($model->field["DATADIV"] == "1") {
                    $query  = " DELETE FROM HTRAINREMARK_DETAIL2_HDAT ";
                    $query .= " WHERE SCHREGNO      = '".$data_arr[$i]["SCHREGNO"]."' ";
                    $query .= "   AND HTRAIN_SEQ    = '001' ";
                    $db->query($query);
                } else {
                //毎年度データ
                    $query  = " DELETE FROM HTRAINREMARK_P_DAT ";
                    $query .= " WHERE YEAR          = '".$data_arr[$i]["YEAR"]."' ";
                    $query .= "   AND SCHREGNO      = '".$data_arr[$i]["SCHREGNO"]."' ";
                    $db->query($query);

                    $query  = " DELETE FROM HTRAINREMARK_DETAIL2_DAT ";
                    $query .= " WHERE YEAR          = '".$data_arr[$i]["YEAR"]."' ";
                    $query .= "   AND SCHREGNO      = '".$data_arr[$i]["SCHREGNO"]."' ";
                    $query .= "   AND HTRAIN_SEQ    = '001' ";
                    $db->query($query);

                    $query  = " DELETE FROM BEHAVIOR_DAT ";
                    $query .= " WHERE YEAR          = '".$data_arr[$i]["YEAR"]."' ";
                    $query .= "   AND SCHREGNO      = '".$data_arr[$i]["SCHREGNO"]."' ";
                    $query .= "   AND DIV          IN ('3', '4') ";
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

    /************************/
    /**  ＣＳＶ出力で使用  **/
    /************************/
    //名称マスタ取得
    function getNameMst($namecd1) {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR    = '".CTRL_YEAR."' ";
        $query .= "     AND NAMECD1 = '".$namecd1."' ";
        $query .= " ORDER BY ";
        $query .= "     NAMECD2 ";

        return $query;
    }

    //CSVデータ出力
    function selectMainQuery($model) {
        //知的障害
        if ($model->field["TAISYOU"] == "1") {
            //学習
            if ($model->field["PAGEDIV"] == "1") {
                $query  = " SELECT ";
                $query .= "     D001.SCHOOLCD, ";
                $query .= "     REGD.YEAR, ";
                $query .= "     REGD.SCHREGNO, ";
                $query .= "     REGD.GRADE, ";
                $query .= "     REGD.HR_CLASS, ";
                $query .= "     REGD.ATTENDNO, ";
                $query .= "     BASE.NAME, ";
                $query .= "     D001.CLASSCD, ";
                $query .= "     D001.SCHOOL_KIND, ";
                $query .= "     D001.CURRICULUM_CD, ";
                $query .= "     D001.SUBCLASSCD, ";
                $query .= "     SUBM.SUBCLASSNAME, ";
                $query .= "     D001.REMARK1 ";
                $query .= " FROM ";
                $query .= "     SCHREG_REGD_DAT REGD ";
                $query .= "     LEFT JOIN SCHREG_BASE_MST BASE ON BASE.SCHREGNO = REGD.SCHREGNO ";
                $query .= "     LEFT JOIN SCHREG_REGD_GDAT GDAT ON GDAT.YEAR  = REGD.YEAR ";
                $query .= "                                    AND GDAT.GRADE = REGD.GRADE ";
                $query .= "     LEFT JOIN SCHREG_STUDYREC_DETAIL_DAT D001 ON REGD.YEAR     = D001.YEAR  ";
                $query .= "                                              AND REGD.SCHREGNO = D001.SCHREGNO ";
                $query .= "                                              AND D001.SEQ      = '001' ";
                $query .= "     LEFT JOIN SUBCLASS_MST SUBM ON D001.CLASSCD       = SUBM.CLASSCD ";
                $query .= "                                AND D001.SCHOOL_KIND   = SUBM.SCHOOL_KIND ";
                $query .= "                                AND D001.CURRICULUM_CD = SUBM.CURRICULUM_CD ";
                $query .= "                                AND D001.SUBCLASSCD    = SUBM.SUBCLASSCD ";
                $query .= " WHERE ";
                $query .= "         REGD.YEAR || REGD.SEMESTER = '".$model->field["YEAR"]."' ";
                $query .= "     AND GDAT.SCHOOL_KIND = 'P' ";
                if ($model->field["GRADE_HR_CLASS"] != "") {
                    $query .= "     AND REGD.GRADE || REGD.HR_CLASS = '".$model->field["GRADE_HR_CLASS"]."' ";
                }
                $query .= " ORDER BY ";
                $query .= "     REGD.GRADE || REGD.HR_CLASS || REGD.ATTENDNO, ";
                $query .= "     D001.CLASSCD, D001.CURRICULUM_CD, D001.SUBCLASSCD ";
            } else {
            //所見
                //入学時の障害の状態
                if ($model->field["DATADIV"] == "1") {
                    $query  = " SELECT ";
                    $query .= "     REGD.SCHREGNO, ";
                    $query .= "     REGD.GRADE, ";
                    $query .= "     REGD.HR_CLASS, ";
                    $query .= "     REGD.ATTENDNO, ";
                    $query .= "     BASE.NAME, ";
                    $query .= "     DET2.REMARK1 AS ENT_DISABILITY_REMARK ";
                    $query .= " FROM ";
                    $query .= "     SCHREG_REGD_DAT REGD ";
                    $query .= "     LEFT JOIN SCHREG_BASE_MST BASE ON BASE.SCHREGNO = REGD.SCHREGNO ";
                    $query .= "     LEFT JOIN SCHREG_REGD_GDAT GDAT ON GDAT.YEAR  = REGD.YEAR ";
                    $query .= "                                    AND GDAT.GRADE = REGD.GRADE ";
                    $query .= "     LEFT JOIN HTRAINREMARK_DETAIL2_HDAT DET2 ON REGD.SCHREGNO   = DET2.SCHREGNO ";
                    $query .= "                                             AND DET2.HTRAIN_SEQ = '001' ";
                    $query .= " WHERE ";
                    $query .= "         REGD.YEAR || REGD.SEMESTER = '".$model->field["YEAR"]."' ";
                    $query .= "     AND GDAT.SCHOOL_KIND = 'P' ";
                    if ($model->field["GRADE_HR_CLASS"] != "") {
                        $query .= "     AND REGD.GRADE || REGD.HR_CLASS = '".$model->field["GRADE_HR_CLASS"]."' ";
                    }
                    $query .= " ORDER BY ";
                    $query .= "     REGD.GRADE || REGD.HR_CLASS || REGD.ATTENDNO ";
                } else {
                //毎年度データ
                    $query  = " SELECT ";
                    $query .= "     REGD.YEAR, ";
                    $query .= "     REGD.SCHREGNO, ";
                    $query .= "     REGD.GRADE, ";
                    $query .= "     REGD.HR_CLASS, ";
                    $query .= "     REGD.ATTENDNO, ";
                    $query .= "     BASE.NAME, ";
                    $query .= "     DE2H.REMARK1 AS BEHAVEREC_REMARK, ";
                    $query .= "     HT_P.TOTALREMARK AS TOTALREMARK, ";
                    $query .= "     HT_P.ATTENDREC_REMARK AS ATTENDREC_REMARK ";
                    $query .= " FROM ";
                    $query .= "     SCHREG_REGD_DAT REGD ";
                    $query .= "     LEFT JOIN SCHREG_BASE_MST BASE ON BASE.SCHREGNO = REGD.SCHREGNO ";
                    $query .= "     LEFT JOIN SCHREG_REGD_GDAT GDAT ON GDAT.YEAR  = REGD.YEAR ";
                    $query .= "                                    AND GDAT.GRADE = REGD.GRADE ";
                    $query .= "     LEFT JOIN HTRAINREMARK_DETAIL2_DAT DE2H ON REGD.YEAR       = DE2H.YEAR ";
                    $query .= "                                            AND REGD.SCHREGNO   = DE2H.SCHREGNO ";
                    $query .= "                                            AND DE2H.HTRAIN_SEQ = '002' ";
                    $query .= "     LEFT JOIN HTRAINREMARK_P_DAT HT_P ON REGD.YEAR     = HT_P.YEAR ";
                    $query .= "                                      AND REGD.SCHREGNO = HT_P.SCHREGNO ";
                    $query .= " WHERE ";
                    $query .= "         REGD.YEAR || REGD.SEMESTER = '".$model->field["YEAR"]."' ";
                    $query .= "     AND GDAT.SCHOOL_KIND = 'P' ";
                    if ($model->field["GRADE_HR_CLASS"] != "") {
                        $query .= "     AND REGD.GRADE || REGD.HR_CLASS = '".$model->field["GRADE_HR_CLASS"]."' ";
                    }
                    $query .= " ORDER BY ";
                    $query .= "     REGD.GRADE || REGD.HR_CLASS || REGD.ATTENDNO ";
                }
            }
        } else {
        //知的障害以外(所見のみ)
            //入学時の障害の状態
            if ($model->field["DATADIV"] == "1") {
                $query  = " SELECT ";
                $query .= "     REGD.SCHREGNO, ";
                $query .= "     REGD.GRADE, ";
                $query .= "     REGD.HR_CLASS, ";
                $query .= "     REGD.ATTENDNO, ";
                $query .= "     BASE.NAME, ";
                $query .= "     DET2.REMARK1 AS ENT_DISABILITY_REMARK ";
                $query .= " FROM ";
                $query .= "     SCHREG_REGD_DAT REGD ";
                $query .= "     LEFT JOIN SCHREG_BASE_MST BASE ON BASE.SCHREGNO = REGD.SCHREGNO ";
                $query .= "     LEFT JOIN SCHREG_REGD_GDAT GDAT ON GDAT.YEAR  = REGD.YEAR ";
                $query .= "                                    AND GDAT.GRADE = REGD.GRADE ";
                $query .= "     LEFT JOIN HTRAINREMARK_DETAIL2_HDAT DET2 ON REGD.SCHREGNO   = DET2.SCHREGNO ";
                $query .= "                                             AND DET2.HTRAIN_SEQ = '001' ";
                $query .= " WHERE ";
                $query .= "         REGD.YEAR || REGD.SEMESTER = '".$model->field["YEAR"]."' ";
                $query .= "     AND GDAT.SCHOOL_KIND = 'P' ";
                if ($model->field["GRADE_HR_CLASS"] != "") {
                    $query .= "     AND REGD.GRADE || REGD.HR_CLASS = '".$model->field["GRADE_HR_CLASS"]."' ";
                }
                $query .= " ORDER BY ";
                $query .= "     REGD.GRADE || REGD.HR_CLASS || REGD.ATTENDNO ";
            } else {
            //毎年度データ
                $query  = " SELECT ";
                $query .= "     REGD.YEAR, ";
                $query .= "     REGD.SCHREGNO, ";
                $query .= "     REGD.GRADE, ";
                $query .= "     REGD.HR_CLASS, ";
                $query .= "     REGD.ATTENDNO, ";
                $query .= "     BASE.NAME, ";
                $query .= "     HT_P.FOREIGNLANGACT1, ";
                $query .= "     HT_P.FOREIGNLANGACT2, ";
                $query .= "     HT_P.FOREIGNLANGACT3, ";
                $query .= "     HT_P.TOTALSTUDYACT, ";
                $query .= "     HT_P.VIEWREMARK, ";
                $query .= "     HT_P.TOTALSTUDYVAL, ";
                $query .= "     DE2H.REMARK1 AS INDEPENDENT_REMARK, ";
                $query .= "     HT_P.TOTALREMARK, ";
                $query .= "     HT_P.ATTENDREC_REMARK, ";
                foreach ($model->arrayD035 as $key => $val) {
                    $query .= "     DIV3_{$key}.RECORD AS RECORD_3{$key}, ";
                }
                foreach ($model->arrayD034 as $key => $val) {
                    $query .= "     DIV4_{$key}.RECORD AS RECORD_4{$key}, ";
                }
                $query .= "     HT_P.SPECIALACTREMARK ";
                $query .= " FROM ";
                $query .= "     SCHREG_REGD_DAT REGD ";
                $query .= "     LEFT JOIN SCHREG_BASE_MST BASE ON BASE.SCHREGNO = REGD.SCHREGNO ";
                $query .= "     LEFT JOIN SCHREG_REGD_GDAT GDAT ON GDAT.YEAR  = REGD.YEAR ";
                $query .= "                                    AND GDAT.GRADE = REGD.GRADE ";
                $query .= "     LEFT JOIN HTRAINREMARK_DETAIL2_DAT DE2H ON REGD.YEAR       = DE2H.YEAR ";
                $query .= "                                            AND REGD.SCHREGNO   = DE2H.SCHREGNO ";
                $query .= "                                            AND DE2H.HTRAIN_SEQ = '001' ";
                $query .= "     LEFT JOIN HTRAINREMARK_P_DAT HT_P ON REGD.YEAR     = HT_P.YEAR ";
                $query .= "                                      AND REGD.SCHREGNO = HT_P.SCHREGNO ";
                foreach ($model->arrayD035 as $key => $val) {
                    $query .= "     LEFT JOIN BEHAVIOR_DAT DIV3_{$key} ON DIV3_{$key}.YEAR     = REGD.YEAR ";
                    $query .= "                                       AND DIV3_{$key}.SCHREGNO = REGD.SCHREGNO ";
                    $query .= "                                       AND DIV3_{$key}.DIV      = '3' ";
                    $query .= "                                       AND DIV3_{$key}.CODE     = '{$key}' ";
                }
                foreach ($model->arrayD034 as $key => $val) {
                    $query .= "     LEFT JOIN BEHAVIOR_DAT DIV4_{$key} ON DIV4_{$key}.YEAR     = REGD.YEAR ";
                    $query .= "                                       AND DIV4_{$key}.SCHREGNO = REGD.SCHREGNO ";
                    $query .= "                                       AND DIV4_{$key}.DIV      = '4' ";
                    $query .= "                                       AND DIV4_{$key}.CODE     = '{$key}' ";
                }
                $query .= " WHERE ";
                $query .= "         REGD.YEAR || REGD.SEMESTER = '".$model->field["YEAR"]."' ";
                $query .= "     AND GDAT.SCHOOL_KIND = 'P' ";
                if ($model->field["GRADE_HR_CLASS"] != "") {
                    $query .= "     AND REGD.GRADE || REGD.HR_CLASS = '".$model->field["GRADE_HR_CLASS"]."' ";
                }
                $query .= " ORDER BY ";
                $query .= "     REGD.GRADE || REGD.HR_CLASS || REGD.ATTENDNO ";
            }
        }

        return $query;
    }
}
?>
