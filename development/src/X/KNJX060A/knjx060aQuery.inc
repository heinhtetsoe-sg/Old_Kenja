<?php

require_once('for_php7.php');

class knjx060aQuery extends Query
{
    //警告点区分
    public function getIsKeikokuten($model)
    {
        $year = substr($model->field['YEAR'], 0, 4);

        $query  = " SELECT ";
        $query .= "     SCHOOL_REMARK1 ";
        $query .= " FROM ";
        $query .= "     SCHOOL_DETAIL_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR='{$year}' ";
        $query .= "     AND SCHOOL_SEQ='009' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            $query .= "     AND SCHOOLCD    = '".sprintf("%012d", SCHOOLCD)."' ";
            if ($model->selectSchoolKind) {
                $query .= " AND SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind), "','")."') ";
            } else {
                $query .= " AND SCHOOL_KIND = '' ";
            }
        } elseif ($model->Properties["useCurriculumcd"] == '1' && $model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND SCHOOLCD    = '".sprintf("%012d", SCHOOLCD)."' ";
            $query .= " AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        }


        return $query;
    }

    public function getSecurityHigh()
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     MENU_HIGH_SECURITY_MST ";
        $query .= " WHERE ";
        $query .= "     PROGRAMID = 'KNJX060A' ";
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

    //年度一覧
    public function getSelectFieldSQL($model)
    {
        $query  = "select distinct t1.year,t1.semester,t2.semestername ";
        $query .= "from chair_dat t1 ";
        $query .= "     inner join semester_mst t2 on t1.year=t2.year and t1.semester=t2.semester ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " AND t1.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind), "','")."') ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND t1.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= "order by t1.year,t1.semester";

        return $query;
    }

    //講座一覧
    public function getSelectFieldSQL2($model)
    {
        $query  = "select distinct chaircd,chairname ";
        $query .= "from chair_dat ";
        $query .= "where YEAR || SEMESTER = '".$model->field["YEAR"]."' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " AND SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind), "','")."') ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "     AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= "order by chaircd";

        return $query;
    }

    //科目一覧
    public function getSelectFieldSQL3($model)
    {
        $query  = " SELECT DISTINCT ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD AS SUBCLASSCD, ";
        } else {
            $query .= "     T1.SUBCLASSCD, ";
        }
        $query .= "     T2.SUBCLASSNAME ";
        $query .= " FROM ";
        $query .= "     CHAIR_DAT T1, ";
        $query .= "     SUBCLASS_MST T2 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR || T1.SEMESTER = '".$model->field["YEAR"]."' AND ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T1.CLASSCD       = T2.CLASSCD AND ";
            $query .= "     T1.SCHOOL_KIND   = T2.SCHOOL_KIND AND ";
            $query .= "     T1.CURRICULUM_CD = T2.CURRICULUM_CD AND ";
        }
        $query .= "     T1.SUBCLASSCD = T2.SUBCLASSCD ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " AND T2.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind), "','")."') ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "     AND T2.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " ORDER ";
        $query .= "     BY SUBCLASSCD ";

        return $query;
    }

    //データ出力
    public function getselectDataSQL($model)
    {
        $query  = " SELECT ";
        $query .= "     T1.YEAR, ";
        $query .= "     T1.SEMESTER, ";
        $query .= "     T1.CHAIRCD, ";
        $query .= "     T1.GROUPCD, ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T1.CLASSCD, ";
            $query .= "     T1.SCHOOL_KIND, ";
            $query .= "     T1.CURRICULUM_CD, ";
        }
        $query .= "     T1.SUBCLASSCD, ";
        $query .= "     T1.CHAIRNAME, ";
        $query .= "     T1.CHAIRABBV, ";
        if ($model->Properties["chairRetsuMeisho_Hyouji"] == '1') {
            $query .= "     L3.NAME1   AS SEQ004_REMARK1, ";
            $query .= "     L2.REMARK2 AS SEQ004_REMARK2, ";
            $query .= "     L2.REMARK3 AS SEQ004_REMARK3, ";
        }
        $query .= "     T1.TAKESEMES, ";
        $query .= "     T1.LESSONCNT, ";
        $query .= "     T1.FRAMECNT, ";
        $query .= "     T1.COUNTFLG, ";
        if ($model->keikokutenFlag == '1') {
            $query .= "     L1.REMARK1 AS KEIKOKUTEN_SOTEN, ";
            $query .= "     L1.REMARK2 AS KEIKOKUTEN_HYOUKA, ";
            $query .= "     L1.REMARK3 AS KEIKOKUTEN_HYOUTEI, ";
        }
        $query .= "     T1.REGISTERCD ";
        $query .= " FROM ";
        $query .= "     CHAIR_DAT T1";
        $query .= "     LEFT JOIN CHAIR_DETAIL_DAT L1 ";
        $query .= "          ON T1.YEAR     = L1.YEAR ";
        $query .= "         AND T1.SEMESTER = L1.SEMESTER ";
        $query .= "         AND T1.CHAIRCD  = L1.CHAIRCD ";
        $query .= "         AND L1.SEQ      = '003' ";
        $query .= "     LEFT JOIN CHAIR_DETAIL_DAT L2 ";
        $query .= "          ON T1.YEAR     = L2.YEAR ";
        $query .= "         AND T1.SEMESTER = L2.SEMESTER ";
        $query .= "         AND T1.CHAIRCD  = L2.CHAIRCD ";
        $query .= "         AND L2.SEQ      = '004' ";
        $query .= "     LEFT JOIN V_NAME_MST L3 ";
        $query .= "          ON T1.YEAR     = L3.YEAR ";
        $query .= "         AND L3.NAMECD1  = 'B023' ";
        $query .= "         AND L2.REMARK1  = L3.NAMECD2 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR || T1.SEMESTER = '".$model->field["YEAR"]."' ";
        if ($model->field["CHAIRCD"] != "") {
            $query .= " AND T1.CHAIRCD = '".$model->field["CHAIRCD"]."' ";
        }
        if ($model->field["SUBCLASSCD"] != "") {
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= " AND T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD = '".$model->field["SUBCLASSCD"]."' ";
            } else {
                $query .= " AND T1.SUBCLASSCD = '".$model->field["SUBCLASSCD"]."' ";
            }
        }
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " AND T1.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind), "','")."') ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "     AND T1.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     T1.CHAIRCD, ";
        $query .= "     T1.SUBCLASSCD ";

        return $query;
    }

    //学校種別取得
    public function getSchoolKind($model)
    {
        $query  = " SELECT ";
        $query .= "     NAME1, ";
        $query .= "     ABBV1 ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'A023' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " AND NAME1 IN ('".implode(explode(':', $model->selectSchoolKind), "','")."') ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND NAME1 = '".SCHOOLKIND."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     NAME1 ";

        return $query;
    }

    //教育課程コード取得
    public function getCurriculumCd()
    {
        $query  = " SELECT ";
        $query .= "     NAMECD2 ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'Z018' ";

        return $query;
    }

    //マスタ(存在チェック用)
    public function getMasterCheck($model, $data, $chk_no)
    {
        $flg    = true;
        $query  = "";
        if ($model->Properties["useCurriculumcd"] == '1') {
            //条件選択
            switch ($chk_no) {
                case "1":
                    $where  = "     SUBCLASS_MST ";
                    $where .= " WHERE ";
                    $where .= "     CLASSCD         = '".$data["CLASSCD"]."' AND ";
                    $where .= "     SCHOOL_KIND     = '".$data["SCHOOL_KIND"]."' AND ";
                    $where .= "     CURRICULUM_CD   = '".$data["CURRICULUM_CD"]."' AND ";
                    $where .= "     SUBCLASSCD      = '".$data["SUBCLASSCD"]."' ";
                    if ($model->Properties["use_prg_schoolkind"] == "1") {
                        if ($model->selectSchoolKind) {
                            $where .= " AND SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind), "','")."') ";
                        }
                    } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
                        $where .= " AND SCHOOL_KIND = '".SCHOOLKIND."' ";
                    }
                    break;
                case "2":
                    $where  = "     V_ELECTCLASS_MST ";
                    $where .= " WHERE ";
                    $where .= "     YEAR    = '".$data["YEAR"]."' AND ";
                    $where .= "     GROUPCD = '".$data["GROUPCD"]."' ";
                    break;
                case "3":
                    $where  = "     CLASS_MST ";
                    $where .= " WHERE ";
                    $where .= "     CLASSCD         = '".$data["CLASSCD"]."' AND ";
                    $where .= "     SCHOOL_KIND     = '".$data["SCHOOL_KIND"]."' ";
                    if ($model->Properties["use_prg_schoolkind"] == "1") {
                        if ($model->selectSchoolKind) {
                            $where .= " AND SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind), "','")."') ";
                        }
                    } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
                        $where .= " AND SCHOOL_KIND = '".SCHOOLKIND."' ";
                    }
                    break;
                default:
                    $flg = false;
                    break;
            }
        } else {
            //条件選択
            switch ($chk_no) {
                case "1":
                    $where = "subclass_mst where SUBCLASSCD = '".$data["SUBCLASSCD"]."' ";
                    break;
                case "2":
                    $where = "v_electclass_mst where YEAR = '".$data["YEAR"]."' AND GROUPCD = '".$data["GROUPCD"]."' ";
                    break;
                default:
                    $flg = false;
                    break;
            }
        }
        if ($flg) {
            $query = "SELECT 'X' FROM " . $where;
        }

        return $query;
    }

    //マスタ(存在チェック用)
    public function getMasterCheck2($model, $data)
    {
        $query  = " SELECT ";
        $query .= "     NAMECD2 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$data["YEAR"]}' ";
        $query .= "     AND NAMECD1 = 'B023' ";
        $query .= "     AND NAME1 = '{$data["SEQ004_REMARK1"]}' ";

        return $query;
    }

    //マスタ(追加・更新・削除用)
    public function getExistCheck($model, $data)
    {
        $query  = "select GROUPCD from chair_dat ";
        $query .= "where YEAR = '".$data["YEAR"]."' ";
        $query .= "and SEMESTER = '".$data["SEMESTER"]."' ";
        $query .= "and CHAIRCD = '".$data["CHAIRCD"]."' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " AND SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind), "','")."') ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        }

        return $query;
    }


    //マスタデータの削除
    public function deleteQueryCsv(&$db, $data)
    {
        $query  = "DELETE FROM chair_dat ";
        $query .= "where YEAR = '".$data["YEAR"]."' ";
        $query .= "and SEMESTER = '".$data["SEMESTER"]."' ";
        $query .= "and CHAIRCD = '".$data["CHAIRCD"]."' ";

        $result = $db->query($query);
    }


    //マスタの追加（ＣＳＶデータより読込）
    public function insertQueryCsv(&$db, $data, $registercd, $model)
    {
        $data1["YEAR"][TEXT]                = $data["YEAR"];
        $data1["SEMESTER"][TEXT]            = $data["SEMESTER"];
        $data1["CHAIRCD"][TEXT]             = $data["CHAIRCD"];
        $data1["CHAIRNAME"][TEXT]           = $data["CHAIRNAME"];
        $data1["CHAIRABBV"][TEXT]           = $data["CHAIRABBV"];
        if ($model->Properties["useCurriculumcd"] == '1') {
            $data1["CLASSCD"][TEXT]             = $data["CLASSCD"];
            $data1["SCHOOL_KIND"][TEXT]         = $data["SCHOOL_KIND"];
            $data1["CURRICULUM_CD"][TEXT]       = $data["CURRICULUM_CD"];
        }
        $data1["SUBCLASSCD"][TEXT]          = $data["SUBCLASSCD"];
        $data1["GROUPCD"][TEXT]             = $data["GROUPCD"];
        $data1["TAKESEMES"][TEXT]           = $data["TAKESEMES"];
        $data1["LESSONCNT"][NUMBER]         = $data["LESSONCNT"];
        $data1["FRAMECNT"][NUMBER]          = $data["FRAMECNT"];
        $data1["COUNTFLG"][TEXT]            = $data["COUNTFLG"];//NO001

        $data1["REGISTERCD"][TEXT]          = $registercd;
        $data1["UPDATED"][NUMBER]           = "sysdate()";

        $query = Query::insertSQL($data1, "chair_dat");

        $result = $db->query($query);
        $result = $db->query(knjx060aQuery::deleteChairDetail($db, $data, $model, "003"));
        $result = $db->query(knjx060aQuery::insertChairDetail($db, $data, $model, "003"));
        if ($model->Properties["chairRetsuMeisho_Hyouji"] == '1') {
            $result = $db->query(knjx060aQuery::deleteChairDetail($db, $data, $model, "004"));
            $result = $db->query(knjx060aQuery::insertChairDetail($db, $data, $model, "004"));
        }
    }


    //マスタの更新（ＣＳＶデータより読込）
    public function updateQueryCsv(&$db, $data, $registercd, $model)
    {
        $data1["YEAR"][TEXT]                = $data["YEAR"];
        $data1["SEMESTER"][TEXT]            = $data["SEMESTER"];
        $data1["CHAIRCD"][TEXT]             = $data["CHAIRCD"];
        $data1["CHAIRNAME"][TEXT]           = $data["CHAIRNAME"];
        $data1["CHAIRABBV"][TEXT]           = $data["CHAIRABBV"];
        if ($model->Properties["useCurriculumcd"] == '1') {
            $data1["CLASSCD"][TEXT]             = $data["CLASSCD"];
            $data1["SCHOOL_KIND"][TEXT]         = $data["SCHOOL_KIND"];
            $data1["CURRICULUM_CD"][TEXT]       = $data["CURRICULUM_CD"];
        }
        $data1["SUBCLASSCD"][TEXT]          = $data["SUBCLASSCD"];
        $data1["GROUPCD"][TEXT]             = $data["GROUPCD"];
        $data1["TAKESEMES"][TEXT]           = $data["TAKESEMES"];
        $data1["LESSONCNT"][NUMBER]         = $data["LESSONCNT"];
        $data1["FRAMECNT"][NUMBER]          = $data["FRAMECNT"];
        $data1["COUNTFLG"][TEXT]            = $data["COUNTFLG"];//NO001

        $data1["REGISTERCD"][TEXT]          = $registercd;
        $data1["UPDATED"][NUMBER]           = "sysdate()";

        $where  = "where YEAR = '".$data["YEAR"]."' ";
        $where .= "and SEMESTER = '".$data["SEMESTER"]."' ";
        $where .= "and CHAIRCD = '".$data["CHAIRCD"]."' ";

        $query = Query::updateSQL($data1, "chair_dat", $where);

        $result = $db->query($query);
        $result = $db->query(knjx060aQuery::deleteChairDetail($db, $data, $model, "003"));
        $result = $db->query(knjx060aQuery::insertChairDetail($db, $data, $model, "003"));
        if ($model->Properties["chairRetsuMeisho_Hyouji"] == '1') {
            $result = $db->query(knjx060aQuery::deleteChairDetail($db, $data, $model, "004"));
            $result = $db->query(knjx060aQuery::insertChairDetail($db, $data, $model, "004"));
        }
    }
    //--- INSERT（講座詳細データ）
    public function insertChairDetail(&$db, $inData, $model, $seq = "")
    {
        $data = array();
        $data["YEAR"][TEXT]         = $inData["YEAR"];
        $data["SEMESTER"][TEXT]     = $inData["SEMESTER"];
        $data["CHAIRCD"][TEXT]      = $inData["CHAIRCD"];
        $data["SEQ"][TEXT]          = $seq;
        if ($seq == '003') {
            $data["REMARK1"][TEXT]      = $inData["KEIKOKUTEN_SOTEN"];
            $data["REMARK2"][TEXT]      = $inData["KEIKOKUTEN_HYOUKA"];
            $data["REMARK3"][TEXT]      = $inData["KEIKOKUTEN_HYOUTEI"];
        } elseif ($seq == '004') {
            $data["REMARK1"][TEXT]      = $inData["SEQ004_REMARK1"];
            $data["REMARK2"][TEXT]      = $inData["SEQ004_REMARK2"];
            $data["REMARK3"][TEXT]      = $inData["SEQ004_REMARK3"];
        }
        $data["REGISTERCD"][TEXT]   = STAFFCD;
        $data["UPDATED"][NUMBER]    = "SYSDATE()";
        $query = Query::insertSQL($data, "chair_detail_dat");

        return $query;
    }
    //--- DELETE（講座詳細データ）
    public function deleteChairDetail(&$db, $data, $model, $seq = "")
    {
        $query  = " DELETE FROM chair_detail_dat ";
        $query .= " WHERE year      = '".$data["YEAR"]."' ";
        $query .= "   AND semester  = '".$data["SEMESTER"]."' ";
        $query .= "   AND chaircd   = '".$data["CHAIRCD"]."' ";
        if ($seq) {
            $query .= "   AND seq       = '".$seq."' ";
        }

        return $query;
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

    //CHAIR_CLS_DATの削除
    public function deleteChairClsDat(&$db, $data)
    {
        $query  = "DELETE FROM chair_cls_dat ";
        $query .= "where YEAR = '".$data["YEAR"]."' ";
        $query .= "and SEMESTER = '".$data["SEMESTER"]."' ";
        $query .= "and CHAIRCD = '".$data["CHAIRCD"]."' ";

        $result = $db->query($query);
    }
}
