<?php

require_once('for_php7.php');

class knjx010query extends Query
{
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

    public function getSecurityHigh()
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     MENU_HIGH_SECURITY_MST ";
        $query .= " WHERE ";
        $query .= "     PROGRAMID = 'KNJX010' ";
        $query .= "     AND INVALID_FLG = '0' ";

        return $query;
    }
    
    //マスタのSQL文作成(存在チェック用)
    public function getMasterSQL($table, $where)
    {
        $query = "SELECT * FROM " . $table . " " . $where;
        return $query;
    }
    //マスタのSQL文作成(出力用)
    public function getOutPutMasterSQL($model)
    {
        $query  = " SELECT ";
        $query .= "     T1.STAFFCD, ";
        $query .= "     T1.STAFFNAME, ";
        $query .= "     T1.STAFFNAME_SHOW, ";
        $query .= "     T1.STAFFNAME_KANA, ";
        $query .= "     T1.STAFFNAME_ENG, ";
        $query .= "     T1.STAFFNAME_REAL, ";
        $query .= "     T1.STAFFNAME_KANA_REAL, ";
        $query .= "     L1.FIELD1 AS JOBCD, ";
        $query .= "     L2.FIELD1 AS SECTIONCD, ";
        $query .= "     L3.FIELD1 AS DUTYSHARECD, ";
        $query .= "     L4.FIELD1 AS CHARGECLASSCD, ";
        $query .= "     T1.STAFFSEX, ";
        $query .= "     T1.STAFFBIRTHDAY, ";
        $query .= "     T1.STAFFZIPCD, ";
        $query .= "     T1.STAFFADDR1, ";
        $query .= "     T1.STAFFADDR2, ";
        $query .= "     T1.STAFFTELNO, ";
        $query .= "     T1.STAFFFAXNO, ";
        $query .= "     T1.STAFFE_MAIL, ";
        $query .= "     '".CTRL_YEAR."' AS YEAR, ";
        $query .= "     L5.FIELD1 AS POSITIONCD1, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     L5.FIELD2 AS POSITIONCD1_MANAGER1, ";
            $query .= "     L5.FIELD3 AS POSITIONCD1_MANAGER2, ";
        } else {
            $query .= "     L5.FIELD2 AS POSITIONCD1_MANAGER1, ";
        }
        $query .= "     L6.FIELD1 AS POSITIONCD2, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     L6.FIELD2 AS POSITIONCD2_MANAGER1, ";
            $query .= "     L6.FIELD3 AS POSITIONCD2_MANAGER2, ";
        } else {
            $query .= "     L6.FIELD2 AS POSITIONCD2_MANAGER1, ";
        }
        $query .= "     L7.FIELD1 AS POSITIONCD3, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     L7.FIELD2 AS POSITIONCD3_MANAGER1, ";
            $query .= "     L7.FIELD3 AS POSITIONCD3_MANAGER2, ";
        } else {
            $query .= "     L7.FIELD2 AS POSITIONCD3_MANAGER1, ";
        }
        $query .= "     T1.REGISTERCD ";
        //$query .= "     T1.UPDATED ";
        $query .= " FROM ";
        $query .= "     STAFF_MST T1";
        $query .= "     LEFT JOIN STAFF_DETAIL_MST L1 ON L1.YEAR = '".CTRL_YEAR."' ";
        $query .= "          AND T1.STAFFCD = L1.STAFFCD ";
        $query .= "          AND L1.STAFF_SEQ = '001' ";
        $query .= "     LEFT JOIN STAFF_DETAIL_MST L2 ON L2.YEAR = '".CTRL_YEAR."' ";
        $query .= "          AND T1.STAFFCD = L2.STAFFCD ";
        $query .= "          AND L2.STAFF_SEQ = '002' ";
        $query .= "     LEFT JOIN STAFF_DETAIL_MST L3 ON L3.YEAR = '".CTRL_YEAR."' ";
        $query .= "          AND T1.STAFFCD = L3.STAFFCD ";
        $query .= "          AND L3.STAFF_SEQ = '003' ";
        $query .= "     LEFT JOIN STAFF_DETAIL_MST L4 ON L4.YEAR = '".CTRL_YEAR."' ";
        $query .= "          AND T1.STAFFCD = L4.STAFFCD ";
        $query .= "          AND L4.STAFF_SEQ = '004' ";
        $query .= "     LEFT JOIN STAFF_DETAIL_MST L5 ON L5.YEAR = '".CTRL_YEAR."' ";
        $query .= "          AND T1.STAFFCD = L5.STAFFCD ";
        $query .= "          AND L5.STAFF_SEQ = '005' ";
        $query .= "     LEFT JOIN STAFF_DETAIL_MST L6 ON L6.YEAR = '".CTRL_YEAR."' ";
        $query .= "          AND T1.STAFFCD = L6.STAFFCD ";
        $query .= "          AND L6.STAFF_SEQ = '006' ";
        $query .= "     LEFT JOIN STAFF_DETAIL_MST L7 ON L7.YEAR = '".CTRL_YEAR."' ";
        $query .= "          AND T1.STAFFCD = L7.STAFFCD ";
        $query .= "          AND L7.STAFF_SEQ = '007' ";
        $query .= " ORDER BY  ";
        $query .= "     T1.STAFFCD ";
        return $query;
    }

    //マスタデータの削除
    public function deleteQueryCsv(&$db, $data)
    {
        $query  = "DELETE FROM staff_mst where staffcd = '".$data["STAFFCD"]."'";
        $result = $db->query($query);
        
        //STAFF_DETAIL_MST削除
        $query  = " DELETE FROM STAFF_DETAIL_MST ";
        $query .= " WHERE  ";
        $query .= "     STAFFCD   = '".$data["STAFFCD"]."' ";
        $query .= " AND STAFF_SEQ IN ('001', '002', '003', '004', '005', '006', '007') ";
        $result = $db->query($query);

        //STAFF_YDAT削除
        $query  = "DELETE FROM STAFF_YDAT WHERE STAFFCD = '".$data["STAFFCD"]."'";
        $result = $db->query($query);

        //USER_MST削除
        $query  = "DELETE FROM USER_MST WHERE STAFFCD = '".$data["STAFFCD"]."'";
        $result = $db->query($query);

        //USER_PWD_HIST_MST削除
        $query  = "DELETE FROM USER_PWD_HIST_MST WHERE STAFFCD = '".$data["STAFFCD"]."'";
        $result = $db->query($query);
    }

    //マスタの追加（ＣＳＶデータより読込）
    public function insertQueryCsv(&$db, $data, $registercd)
    {
        $data1 = array();
        $data1["STAFFCD"][TEXT]             = $data["STAFFCD"];
        $data1["STAFFNAME"][TEXT]           = $data["STAFFNAME"];
        $data1["STAFFNAME_SHOW"][TEXT]      = $data["STAFFNAME_SHOW"];
        $data1["STAFFNAME_KANA"][TEXT]      = $data["STAFFNAME_KANA"];
        $data1["STAFFNAME_ENG"][TEXT]       = $data["STAFFNAME_ENG"];
        $data1["STAFFNAME_REAL"][TEXT]      = $data["STAFFNAME_REAL"];
        $data1["STAFFNAME_KANA_REAL"][TEXT] = $data["STAFFNAME_KANA_REAL"];
        $data1["JOBCD"][TEXT]               = $data["JOBCD"];
        $data1["SECTIONCD"][TEXT]           = $data["SECTIONCD"];
        $data1["DUTYSHARECD"][TEXT]         = $data["DUTYSHARECD"];
        $data1["CHARGECLASSCD"][TEXT]       = $data["CHARGECLASSCD"];
        $data1["STAFFSEX"][TEXT]            = $data["STAFFSEX"];
        $data1["STAFFBIRTHDAY"][TEXT]       = $data["STAFFBIRTHDAY"];
        $data1["STAFFZIPCD"][TEXT]          = $data["STAFFZIPCD"];
        $data1["STAFFADDR1"][TEXT]          = $data["STAFFADDR1"];
        $data1["STAFFADDR2"][TEXT]          = $data["STAFFADDR2"];
        $data1["STAFFTELNO"][TEXT]          = $data["STAFFTELNO"];
        $data1["STAFFFAXNO"][TEXT]          = $data["STAFFFAXNO"];
        $data1["STAFFE_MAIL"][TEXT]         = $data["STAFFE_MAIL"];

        $data1["REGISTERCD"][TEXT]          = $registercd;
        $data1["UPDATED"][NUMBER]           = "sysdate()";

        $query = Query::insertSQL($data1, "staff_mst");
        $result = $db->query($query);
    }


    //マスタの更新（ＣＳＶデータより読込）
    public function updateQueryCsv(&$db, $data, $registercd)
    {
        $data1["STAFFCD"][TEXT]             = $data["STAFFCD"];
        $data1["STAFFNAME"][TEXT]           = $data["STAFFNAME"];
        $data1["STAFFNAME_SHOW"][TEXT]      = $data["STAFFNAME_SHOW"];
        $data1["STAFFNAME_KANA"][TEXT]      = $data["STAFFNAME_KANA"];
        $data1["STAFFNAME_ENG"][TEXT]       = $data["STAFFNAME_ENG"];
        $data1["STAFFNAME_REAL"][TEXT]      = $data["STAFFNAME_REAL"];
        $data1["STAFFNAME_KANA_REAL"][TEXT] = $data["STAFFNAME_KANA_REAL"];
        $data1["JOBCD"][TEXT]               = $data["JOBCD"];
        $data1["SECTIONCD"][TEXT]           = $data["SECTIONCD"];
        $data1["DUTYSHARECD"][TEXT]         = $data["DUTYSHARECD"];
        $data1["CHARGECLASSCD"][TEXT]       = $data["CHARGECLASSCD"];
        $data1["STAFFSEX"][TEXT]            = $data["STAFFSEX"];
        $data1["STAFFBIRTHDAY"][TEXT]       = $data["STAFFBIRTHDAY"];
        $data1["STAFFZIPCD"][TEXT]          = $data["STAFFZIPCD"];
        $data1["STAFFADDR1"][TEXT]          = $data["STAFFADDR1"];
        $data1["STAFFADDR2"][TEXT]          = $data["STAFFADDR2"];
        $data1["STAFFTELNO"][TEXT]          = $data["STAFFTELNO"];
        $data1["STAFFFAXNO"][TEXT]          = $data["STAFFFAXNO"];
        $data1["STAFFE_MAIL"][TEXT]         = $data["STAFFE_MAIL"];

        $data1["REGISTERCD"][TEXT]          = $registercd;
        $data1["UPDATED"][NUMBER]           = "sysdate()";

        $where = " WHERE STAFFCD = '" .$data["STAFFCD"] ."' ";
        $query = Query::updateSQL($data1, "staff_mst", $where);
        $result = $db->query($query);
    }

    //STAFF_DETAIL_MSTチェック
    public function &getCountDetailMst($model, $data, $seq)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     STAFF_DETAIL_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '" .$data["YEAR"] ."' ";
        $query .= " AND STAFFCD = '" .$data["STAFFCD"] ."' ";
        $query .= " AND STAFF_SEQ = '".$seq."' ";

        return $query;
    }

    //--- UPDATE, INSERT (STAFF_DETAIL_MST)
    public function &getUpdateDetailQuery(&$db, $data, $registercd, $model)
    {
        //職名
        $getCount1 = $db->getOne(knjx010Query::getCountDetailMst($model, $data, "001"));
        $data1 = array();
        $data1["FIELD1"][TEXT]               = $data["JOBCD"];
        $data1["REGISTERCD"][TEXT]           = $registercd;
        $data1["UPDATED"][FUNC]              = "sysdate()";
        if ($getCount1 == 0) {
            $data1["YEAR"][TEXT]                 = $data["YEAR"];
            $data1["STAFFCD"][TEXT]              = $data["STAFFCD"];
            $data1["STAFF_SEQ"][TEXT]            = "001";
            $query = Query::insertSQL($data1, "STAFF_DETAIL_MST");
            $db->query($query);
        } else {
            $where  = "WHERE YEAR    = '" .$data["YEAR"] ."'";
            $where .= "  AND STAFFCD = '" .$data["STAFFCD"] ."'";
            $where .= "  AND STAFF_SEQ = '001'";
            $query = Query::updateSQL($data1, "STAFF_DETAIL_MST", $where);
            $db->query($query);
        }
        //所属
        $getCount2 = $db->getOne(knjx010Query::getCountDetailMst($model, $data, "002"));
        $data1 = array();
        $data1["FIELD1"][TEXT]               = $data["SECTIONCD"];
        $data1["REGISTERCD"][TEXT]           = STAFFCD;
        $data1["UPDATED"][FUNC]              = "sysdate()";
        if ($getCount2 == 0) {
            $data1["YEAR"][TEXT]                 = $data["YEAR"];
            $data1["STAFFCD"][TEXT]              = $data["STAFFCD"];
            $data1["STAFF_SEQ"][TEXT]            = "002";
            $query = Query::insertSQL($data1, "STAFF_DETAIL_MST");
            $db->query($query);
        } else {
            $where  = "WHERE YEAR    = '" .$data["YEAR"] ."'";
            $where .= "  AND STAFFCD = '" .$data["STAFFCD"] ."'";
            $where .= "  AND STAFF_SEQ = '002'";
            $query = Query::updateSQL($data1, "STAFF_DETAIL_MST", $where);
            $db->query($query);
        }
        //校務分掌部
        $getCount3 = $db->getOne(knjx010Query::getCountDetailMst($model, $data, "003"));
        $data1 = array();
        $data1["FIELD1"][TEXT]               = $data["DUTYSHARECD"];
        $data1["REGISTERCD"][TEXT]           = STAFFCD;
        $data1["UPDATED"][FUNC]              = "sysdate()";
        if ($getCount3 == 0) {
            $data1["YEAR"][TEXT]                 = $data["YEAR"];
            $data1["STAFFCD"][TEXT]              = $data["STAFFCD"];
            $data1["STAFF_SEQ"][TEXT]            = "003";
            $query = Query::insertSQL($data1, "STAFF_DETAIL_MST");
            $db->query($query);
        } else {
            $where  = "WHERE YEAR    = '" .$data["YEAR"] ."'";
            $where .= "  AND STAFFCD = '" .$data["STAFFCD"] ."'";
            $where .= "  AND STAFF_SEQ = '003'";
            $query = Query::updateSQL($data1, "STAFF_DETAIL_MST", $where);
            $db->query($query);
        }
        //授業受け持ち
        $getCount4 = $db->getOne(knjx010Query::getCountDetailMst($model, $data, "004"));
        $data1 = array();
        $data1["FIELD1"][TEXT]               = $data["CHARGECLASSCD"];
        $data1["REGISTERCD"][TEXT]           = STAFFCD;
        $data1["UPDATED"][FUNC]              = "sysdate()";
        if ($getCount4 == 0) {
            $data1["YEAR"][TEXT]                 = $data["YEAR"];
            $data1["STAFFCD"][TEXT]              = $data["STAFFCD"];
            $data1["STAFF_SEQ"][TEXT]            = "004";
            $query = Query::insertSQL($data1, "STAFF_DETAIL_MST");
            $db->query($query);
        } else {
            $where  = "WHERE YEAR    = '" .$data["YEAR"] ."'";
            $where .= "  AND STAFFCD = '" .$data["STAFFCD"] ."'";
            $where .= "  AND STAFF_SEQ = '004'";
            $query = Query::updateSQL($data1, "STAFF_DETAIL_MST", $where);
            $db->query($query);
        }
        //肩書き1
        $getCount5 = $db->getOne(knjx010Query::getCountDetailMst($model, $data, "005"));
        $data1 = array();
        $data1["FIELD1"][TEXT]               = $data["POSITIONCD1"];
        $data1["FIELD2"][TEXT]               = $data["POSITIONCD1_MANAGER1"];
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $data1["FIELD3"][TEXT]               = $data["POSITIONCD1_MANAGER2"];
        }
        $data1["REGISTERCD"][TEXT]           = STAFFCD;
        $data1["UPDATED"][FUNC]              = "sysdate()";
        if ($getCount5 == 0) {
            $data1["YEAR"][TEXT]                 = $data["YEAR"];
            $data1["STAFFCD"][TEXT]              = $data["STAFFCD"];
            $data1["STAFF_SEQ"][TEXT]            = "005";
            $query = Query::insertSQL($data1, "STAFF_DETAIL_MST");
            $db->query($query);
        } else {
            $where  = "WHERE YEAR    = '" .$data["YEAR"] ."'";
            $where .= "  AND STAFFCD = '" .$data["STAFFCD"] ."'";
            $where .= "  AND STAFF_SEQ = '005'";
            $query = Query::updateSQL($data1, "STAFF_DETAIL_MST", $where);
            $db->query($query);
        }
        //肩書き2
        $getCount6 = $db->getOne(knjx010Query::getCountDetailMst($model, $data, "006"));
        $data1 = array();
        $data1["FIELD1"][TEXT]               = $data["POSITIONCD2"];
        $data1["FIELD2"][TEXT]               = $data["POSITIONCD2_MANAGER1"];
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $data1["FIELD3"][TEXT]               = $data["POSITIONCD2_MANAGER2"];
        }
        $data1["REGISTERCD"][TEXT]           = STAFFCD;
        $data1["UPDATED"][FUNC]              = "sysdate()";
        if ($getCount6 == 0) {
            $data1["YEAR"][TEXT]                 = $data["YEAR"];
            $data1["STAFFCD"][TEXT]              = $data["STAFFCD"];
            $data1["STAFF_SEQ"][TEXT]            = "006";
            $query = Query::insertSQL($data1, "STAFF_DETAIL_MST");
            $db->query($query);
        } else {
            $where  = "WHERE YEAR    = '" .$data["YEAR"] ."'";
            $where .= "  AND STAFFCD = '" .$data["STAFFCD"] ."'";
            $where .= "  AND STAFF_SEQ = '006'";
            $query = Query::updateSQL($data1, "STAFF_DETAIL_MST", $where);
            $db->query($query);
        }
        //肩書き3
        $getCount7 = $db->getOne(knjx010Query::getCountDetailMst($model, $data, "007"));
        $data1 = array();
        $data1["FIELD1"][TEXT]               = $data["POSITIONCD3"];
        $data1["FIELD2"][TEXT]               = $data["POSITIONCD3_MANAGER1"];
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $data1["FIELD3"][TEXT]               = $data["POSITIONCD3_MANAGER2"];
        }
        $data1["REGISTERCD"][TEXT]           = STAFFCD;
        $data1["UPDATED"][FUNC]              = "sysdate()";
        if ($getCount6 == 0) {
            $data1["YEAR"][TEXT]                 = $data["YEAR"];
            $data1["STAFFCD"][TEXT]              = $data["STAFFCD"];
            $data1["STAFF_SEQ"][TEXT]            = "007";
            $query = Query::insertSQL($data1, "STAFF_DETAIL_MST");
            $db->query($query);
        } else {
            $where  = "WHERE YEAR    = '" .$data["YEAR"] ."'";
            $where .= "  AND STAFFCD = '" .$data["STAFFCD"] ."'";
            $where .= "  AND STAFF_SEQ = '007'";
            $query = Query::updateSQL($data1, "STAFF_DETAIL_MST", $where);
            $db->query($query);
        }
        return;
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
