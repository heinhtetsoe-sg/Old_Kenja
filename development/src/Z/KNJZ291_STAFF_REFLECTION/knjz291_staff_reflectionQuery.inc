<?php

require_once('for_php7.php');

class knjz291_staff_reflectionQuery extends Query {

    //教育委員会学校コード
    function getZ010() {
        $query  = " SELECT ";
        $query .= "     NAME1 ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "         NAMECD1 = 'Z010' ";
        $query .= "     AND NAMECD2 = '00' ";

        return $query;
    }

    //教育課程
    function getYear() {
        $query  = " WITH YEAR_T(LABEL, VALUE) AS ( ";
        $query .= "     VALUES('".(CTRL_YEAR - 1)."', '".(CTRL_YEAR - 1)."') ";
        $query .= "     UNION ";
        $query .= "     VALUES('".CTRL_YEAR."', '".CTRL_YEAR."') ";
        $query .= "     UNION ";
        $query .= "     VALUES('".(CTRL_YEAR + 1)."', '".(CTRL_YEAR + 1)."') ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     YEAR_T ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //教育委員会学校コード
    function getEdboardSchool() {
        $query  = " SELECT ";
        $query .= "     KYOUIKU_IINKAI_SCHOOLCD ";
        $query .= " FROM ";
        $query .= "     V_SCHOOL_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";

        return $query;
    }

    //リスト表示
    function getList($model) {

        $query  = " SELECT ";
        $query .= "     T1.STAFFCD, ";
        $query .= "     I1.STAFFNAME, ";
        $query .= "     MAX(T1.FROM_DATE) AS SDATE, ";
        $query .= "     MAX(T1.TO_DATE) AS EDATE ";
        $query .= " FROM ";
        $query .= "     EDBOARD_STAFF_WORK_HIST_DAT T1 ";
        $query .= "     INNER JOIN STAFF_MST I1 ON T1.STAFFCD = I1.STAFFCD ";
        if ($model->field["STAFFCD"] != "" || $model->field["STAFFNAME"] != "" || $model->field["STAFFNAME_KANA"] != "") {
            $query .= " WHERE ";
            $query .= "         I1.STAFFCD IS NOT NULL ";
            if ($model->field["STAFFCD"]) {
                $query .= "     AND I1.STAFFCD LIKE '{$model->field["STAFFCD"]}%' ";
            }
            if ($model->field["STAFFNAME"]) {
                $query .= "     AND I1.STAFFNAME LIKE '{$model->field["STAFFNAME"]}%' ";
            }
            if ($model->field["STAFFNAME_KANA"]) {
                $query .= "     AND I1.STAFFNAME_KANA LIKE '{$model->field["STAFFNAME_KANA"]}%' ";
            }
        }
        $query .= " GROUP BY ";
        $query .= "     T1.STAFFCD, ";
        $query .= "     I1.STAFFNAME ";

        return $query;
    }

    //職員取得
    function getStaff($staffCd, $flg="") {
        $query  = " SELECT ";
        if (!$flg) {
            $query .= "     COUNT(*) AS CNT ";
        } else {
            $query .= "     STAFFCD ";
        }
        $query .= " FROM ";
        $query .= "     STAFF_MST ";
        $query .= " WHERE ";
        $query .= "     EDBOARD_STAFFCD = '".$staffCd."' ";

        return $query;
    }

    //更新用職員取得
    function getSetStaff($staffCd, $flg) {
        $query  = " SELECT ";
        if ($flg) {
            $query .= "     COUNT(*) AS CNT ";
        } else {
            $query .= "     * ";
        }
        $query .= " FROM ";
        $query .= "     STAFF_MST ";
        $query .= " WHERE ";
        $query .= "     STAFFCD = '".$staffCd."' ";

        return $query;
    }

    //更新用職員取得(利用者マスタ)
    function getUserMst($model, $staffCd) {
        $query  = " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     USER_MST ";
        $query .= " WHERE ";
        $query .= "     STAFFCD = '".$staffCd."' ";
        if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= "     AND SCHOOLCD    = '".sprintf("%012d", SCHOOLCD)."' ";
            $query .= "     AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        }

        return $query;
    }

    /* データ更新処理 */
    function update($model, $updateStaff)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $db2 = Query::dbCheckOut2();
        $db2->autoCommit(false);

        $newPass = "pass";
        $setPass = md5($newPass);

        foreach ($updateStaff as $key => $val) {
            //学校側の職員コードを取得
            $getStaffQuery = knjz291_staff_reflectionQuery::getSetStaff($val, "");
            $setStaff = $db2->getRow($getStaffQuery, DB_FETCHMODE_ASSOC);
            if (is_array($setStaff)) {
                $upstaffcdCnt = $db->getOne(knjz291_staff_reflectionQuery::getSetStaff($model->fields["UP_STAFFCD"][$val], "COUNT"));
                if ($upstaffcdCnt == 0 && $model->fields["UP_STAFFCD"][$val] != "") {
                    $data = array();
                    $data["STAFFCD"][TEXT]              = $model->fields["UP_STAFFCD"][$val];
                    $data["STAFFNAME"][TEXT]            = $setStaff["STAFFNAME"];
                    $data["STAFFNAME_SHOW"][TEXT]       = $setStaff["STAFFNAME_SHOW"];
                    $data["STAFFNAME_KANA"][TEXT]       = $setStaff["STAFFNAME_KANA"];
                    $data["STAFFNAME_ENG"][TEXT]        = $setStaff["STAFFNAME_ENG"];
                    $data["STAFFNAME_REAL"][TEXT]       = $setStaff["STAFFNAME_REAL"];
                    $data["STAFFNAME_KANA_REAL"][TEXT]  = $setStaff["STAFFNAME_KANA_REAL"];
                    $data["JOBCD"][TEXT]                = $setStaff["JOBCD"];
                    $data["SECTIONCD"][TEXT]            = $setStaff["SECTIONCD"];
                    $data["DUTYSHARECD"][TEXT]          = $setStaff["DUTYSHARECD"];
                    $data["CHARGECLASSCD"][TEXT]        = $setStaff["CHARGECLASSCD"];
                    $data["STAFFSEX"][TEXT]             = $setStaff["STAFFSEX"];
                    $data["STAFFBIRTHDAY"][DATE]        = $setStaff["STAFFBIRTHDAY"] ? $setStaff["STAFFBIRTHDAY"] : NULL;
                    $data["STAFFZIPCD"][TEXT]           = $setStaff["STAFFZIPCD"];
                    $data["STAFFADDR1"][TEXT]           = $setStaff["STAFFADDR1"];
                    $data["STAFFADDR2"][TEXT]           = $setStaff["STAFFADDR2"];
                    $data["STAFFTELNO"][TEXT]           = $setStaff["STAFFTELNO"];
                    $data["STAFFFAXNO"][TEXT]           = $setStaff["STAFFFAXNO"];
                    $data["STAFFE_MAIL"][TEXT]          = $setStaff["STAFFE_MAIL"];
                    $data["EDBOARD_STAFFCD"][TEXT]      = $setStaff["STAFFCD"];
                    $data["EDBOARD_TORIKOMI_FLG"][TEXT] = "1";
                    $data["REGISTERCD"][TEXT]           = STAFFCD;
                    $data["UPDATED"][FUNC]              = "sysdate()";
                    $query = Query::insertSQL($data, "STAFF_MST");
                    $db->query($query);

                    // 三重県の時、利用者マスタを登録する
                    if ($model->z010Name1 == 'mieken') {
                        $userCnt = $db->getOne(knjz291_staff_reflectionQuery::getUserMst($model, $model->fields["UP_STAFFCD"][$val]));

                        // --USER_MST--
                        $data = array();
                        if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
                            $data["SCHOOLCD"][TEXT]             = sprintf("%012d", SCHOOLCD);
                            $data["SCHOOL_KIND"][TEXT]          = SCHOOLKIND;
                        }
                        $data["STAFFCD"][TEXT]        = $model->fields["UP_STAFFCD"][$val];
                        $data["USERID"][TEXT]         = 'm'.substr($model->fields["UP_STAFFCD"][$val], 2);// "m"+先頭2桁除いた職員番号
                        $data["PASSWD"][TEXT]         = $setPass;
                        $data["INVALID_FLG"][TEXT]    = "0";
                        $data["PWDTERMCHK_FLG"][TEXT] = "0";
                        $data["REGISTERCD"][TEXT]     = STAFFCD;
                        $data["UPDATED"][NUMBER]      = "sysdate()";
                        //登録データチェック
                        if (1 > $userCnt) {
                            $query = Query::insertSQL($data, "USER_MST");
                            $db->query($query);
                        } else {
                            $this->setWarning('MSG203',"入力した職員番号は利用者マスタに登録されています。"."(".$model->fields["UP_STAFFCD"][$val].")");
                            return false;
                        }
                    }

                    //教育委員会外テーブルに登録
                    $query = "DELETE FROM EDBOARD_STAFF_DAT WHERE EDBOARD_SCHOOLCD = '{$model->edboard_schoolcd}' AND STAFFCD = '{$setStaff["STAFFCD"]}' ";
                    $db2->query($query);
                    $data = array();
                    $data["EDBOARD_SCHOOLCD"][TEXT] = $model->edboard_schoolcd;
                    $data["STAFFCD"][TEXT]          = $setStaff["STAFFCD"];
                    $data["STAFF_DIV"][TEXT]        = "1";
                    $data["REGISTERCD"][TEXT]       = STAFFCD;
                    $data["UPDATED"][FUNC]          = "sysdate()";
                    $query = Query::insertSQL($data, "EDBOARD_STAFF_DAT");
                    $db2->query($query);
                } else {
                    if ($model->fields["UP_STAFFCD"][$val] == "") {
                        $this->setWarning('MSG301',"(職員番号)");
                        return false;
                    } else if ($upstaffcdCnt > 0) {
                        $this->setWarning('MSG203',"入力した職員番号は既に登録されています。"."(".$model->fields["UP_STAFFCD"][$val].")");
                        return false;
                    }
                }
            }
        }
        $db->commit();
        $db2->commit();
        Query::dbCheckIn($db);
        Query::dbCheckIn($db2);
    }
}
?>