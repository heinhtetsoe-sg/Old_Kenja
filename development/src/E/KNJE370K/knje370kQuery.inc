<?php

require_once('for_php7.php');

class knje370kQuery extends Query {

    //年度
    function getYear() {
        $query  = " WITH CTRL_T (LABEL, VALUE) AS ( ";
        $query .= "     VALUES('".CTRL_YEAR."', '".CTRL_YEAR."'), ";
        $query .= "     ('".(CTRL_YEAR + 1)."', '".(CTRL_YEAR + 1)."') ";
        $query .= " ) ";
        $query .= " SELECT DISTINCT ";
        $query .= "     YEAR AS LABEL, ";
        $query .= "     YEAR AS VALUE ";
        $query .= " FROM ";
        $query .= "     COLLECT_GRP_MST ";
        $query .= " UNION ";
        $query .= " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     CTRL_T ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //校種取得
    function getSchkind($model) {
        $query  = " SELECT ";
        $query .= "     NAME1 AS VALUE, ";
        $query .= "     ABBV1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR    = '".CTRL_YEAR."' ";
        $query .= "     AND NAMECD1 = 'A023' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "     AND NAME1 IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        }
        $query .= " ORDER BY ";
        $query .= "     NAMECD2 ";

        return $query;
    }

    //前年度からコピー時のデータ存在チェック
    function selectYearQuery($year)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     AFT_COLLEGE_GROUP_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$year}' ";

        return $query;
    }

    //コピー時のマスタチェック(前年度に登録されている項目が今年度に存在するか)
    function mstMcdCheck($model)
    {
        $query  = " SELECT ";
        $query .= "     T1.COLLECT_LM_CD, ";
        $query .= "     T2.COLLECT_LM_CD AS MST_EXP_MCD ";
        $query .= " FROM ";
        $query .= "     (SELECT DISTINCT COLLECT_L_CD || COLLECT_M_CD AS COLLECT_LM_CD ";
        $query .= "        FROM V_COLLECT_GRP_DAT ";
        $query .= "       WHERE YEAR = '".(CTRL_YEAR - 1)."' ";
        $query .= "             AND SCHOOLCD    = '".(sprintf("%012d", SCHOOLCD))."' ";
        $query .= "             AND SCHOOL_KIND = '".$model->schoolKind."' ";
        $query .= "             AND COLLECT_KOJIN_FLG != '1') T1 ";
        $query .= " LEFT OUTER JOIN ";
        $query .= "     (SELECT COLLECT_L_CD || COLLECT_M_CD AS COLLECT_LM_CD ";
        $query .= "        FROM COLLECT_M_MST ";
        $query .= "       WHERE YEAR = '".CTRL_YEAR."' ";
        $query .= "             AND SCHOOLCD    = '".(sprintf("%012d", SCHOOLCD))."' ";
        $query .= "             AND SCHOOL_KIND = '".$model->schoolKind."') T2 ";
        $query .= " ON ";
        $query .= "         T1.COLLECT_LM_CD = T2.COLLECT_LM_CD ";

        return $query;
    }

    //学校系列
    function getSchoolGroup() {
        $query  = " SELECT ";
        $query .= "     NAMECD2 || ' ' || NAME1 AS LABEL, ";
        $query .= "     NAMECD2 AS VALUE ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'E012' ";
        $query .= " ORDER BY ";
        $query .= "     NAMECD2 ";
        return $query;
    }

    //学校一覧
    function getSchoolList($model){
        //文字列の文頭、文末に含まれる全角or半角スペースを消去
        $trimSchoolname = preg_replace('/^[ 　]+/u', '', $model->field["SCHOOL_NAME"]);
        $trimSchoolname = preg_replace('/[ 　]+$/u', '', $trimSchoolname);

        $query  = " SELECT ";
        $query .= "     COLLEGE_M.SCHOOL_CD, ";
        $query .= "     COLLEGE_M.SCHOOL_NAME, ";
        $query .= "     COLLEGE_M.BUNAME, ";
        $query .= "     COLLEGE_M.KANAME, ";
        $query .= "     COLLEGE_M.SCHOOL_SORT, ";
        $query .= "     COLLEGE_M.BUNYA, ";
        $query .= "     COLLEGE_M.AREA_NAME, ";
        $query .= "     COLLEGE_ADDR.ZIPCD, ";
        $query .= "     COLLEGE_ADDR.ADDR1, ";
        $query .= "     COLLEGE_ADDR.ADDR2, ";
        $query .= "     COLLEGE_ADDR.TELNO, ";
        $query .= "     COLLEGE_M.GREDES, ";
        $query .= "     COLLEGE_M.SCHOOL_NAME_SHOW1, ";
        $query .= "     COLLEGE_M.SCHOOL_NAME_SHOW2, ";
        $query .= "     COLLEGE_M.SCHOOL_GROUP, ";
        $query .= "     E012.NAME1 AS SCHOOL_GROUP_NAME, ";
        $query .= "     GROUP_D.COLLEGE_GRP_CD ";
        $query .= " FROM ";
        $query .= "     COLLEGE_MST COLLEGE_M ";
        $query .= " LEFT JOIN ";
        $query .= "     NAME_MST E012 ON  E012.NAMECD2 = COLLEGE_M.SCHOOL_GROUP ";
        $query .= "                 AND E012.NAMECD1 = 'E012' ";
        $query .= " LEFT JOIN ";
        $query .= "     COLLEGE_CAMPUS_ADDR_DAT COLLEGE_ADDR ON  COLLEGE_ADDR.SCHOOL_CD = COLLEGE_M.SCHOOL_CD ";
        $query .= "                                AND COLLEGE_ADDR.CAMPUS_ADDR_CD = COLLEGE_M.CAMPUS_ADDR_CD ";
        $query .= " LEFT JOIN ";
        $query .= "    AFT_COLLEGE_GROUP_DAT AS GROUP_D ";
        $query .= "    ON ";
        $query .= "    GROUP_D.YEAR = '".CTRL_YEAR."' ";
        $query .= "    AND GROUP_D.COLLEGE_GRP_CD = '{$model->exp_grpcd}' ";
        $query .= "    AND COLLEGE_M.SCHOOL_CD = GROUP_D.SCHOOL_CD ";
        $query .= " WHERE ";
        $query .= "         COLLEGE_M.SCHOOL_CD IS NOT NULL ";
        if ($model->field["SCHOOL_GROUP"] != '') {
            $query .= "     AND SCHOOL_GROUP = '{$model->field["SCHOOL_GROUP"]}' ";
        }
        $query .= "     AND NOT EXISTS (  ";
        $query .= "                         SELECT ";
        $query .= "                             'x' ";
        $query .= "                         FROM ";
        $query .= "                             AFT_COLLEGE_GROUP_DAT AS GROUP_D2 ";
        $query .= "                         WHERE ";
        $query .= "                             GROUP_D2.YEAR = '".CTRL_YEAR."' ";
        $query .= "                             AND COLLEGE_M.SCHOOL_CD = GROUP_D2.SCHOOL_CD ";
        $query .= "                             AND GROUP_D2.COLLEGE_GRP_CD <> '{$model->exp_grpcd}' ";
        $query .= "                    ) ";
        $query .= " ORDER BY ";
        $query .= "     COLLEGE_M.SCHOOL_CD ";
        return $query;
    }


    //学校グループリスト(左画面リスト)
    function SelectQuery($model)
    {
        $query  = "  SELECT ";
        $query .= "    GROUP_M.YEAR ";
        $query .= "    , GROUP_M.COLLEGE_GRP_CD ";
        $query .= "    , GROUP_M.COLLEGE_GRP_NAME ";
        $query .= "    , COUNT(SCHOOL_CD) AS CNT ";
        $query .= "  FROM ";
        $query .= "    AFT_COLLEGE_GROUP_MST AS GROUP_M ";
        $query .= "  LEFT JOIN ";
        $query .= "    AFT_COLLEGE_GROUP_DAT AS GROUP_D ";
        $query .= "    ON ";
        $query .= "    GROUP_M.YEAR = GROUP_D.YEAR AND ";
        $query .= "    GROUP_M.COLLEGE_GRP_CD = GROUP_D.COLLEGE_GRP_CD ";
        $query .= "  WHERE ";
        $query .= "    GROUP_M.YEAR = '".CTRL_YEAR."' ";
        $query .= "  GROUP BY ";
        $query .= "    GROUP_M.YEAR ";
        $query .= "    , GROUP_M.COLLEGE_GRP_CD ";
        $query .= "    , GROUP_M.COLLEGE_GRP_NAME ";
        $query .= "  ORDER BY ";
        $query .= "    GROUP_M.YEAR ";
        $query .= "    , GROUP_M.COLLEGE_GRP_CD ";

        return $query;
    }

    //１レコード取得
    function getSelectData($model, $flg)
    {
        $query  = "  SELECT ";
        $query .= "    GROUP_M.COLLEGE_GRP_CD, ";
        $query .= "    GROUP_M.COLLEGE_GRP_NAME ";
        $query .= "  FROM ";
        $query .= "    AFT_COLLEGE_GROUP_MST AS GROUP_M ";
        $query .= "  WHERE ";
        $query .= "    GROUP_M.YEAR = '".CTRL_YEAR."' ";
        if ($flg == 1) {
            $query .= "   AND GROUP_M.COLLEGE_GRP_CD = '{$model->exp_grpcd}'";
        } else {
            $query .= "   AND GROUP_M.COLLEGE_GRP_CD = '".sprintf("%02d", $model->field["COLLEGE_GRP_CD"])."'";
        }

        return $query;
    }

    //削除
    function &getDeleteQuery($model,&$db)
    {
        $query  = " DELETE FROM AFT_COLLEGE_GROUP_MST ";
        $query .= "     WHERE ";
        $query .= "           YEAR              = '".CTRL_YEAR."' AND ";
        $query .= "           COLLEGE_GRP_CD    = '".(sprintf("%02d", $model->field["COLLEGE_GRP_CD"]))."' ";
        $db->query($query);
        
        $query  = " DELETE FROM AFT_COLLEGE_GROUP_DAT ";
        $query .= "     WHERE ";
        $query .= "           YEAR              = '".CTRL_YEAR."' AND ";
        $query .= "           COLLEGE_GRP_CD    = '".(sprintf("%02d", $model->field["COLLEGE_GRP_CD"]))."' ";
        $db->query($query);

        return;
    }

    function getAftCollegeGroupDatCnt($model){
        $query  = " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     AFT_COLLEGE_GROUP_DAT AS GROUP_D ";
        $query .= " WHERE ";
        $query .= "     GROUP_D.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND GROUP_D.COLLEGE_GRP_CD <> '".sprintf("%02d", $model->field["COLLEGE_GRP_CD"])."' ";
        $query .= "     AND GROUP_D.SCHOOL_CD IN ('".str_replace(",", "','", $model->selectLeft)."') ";
        return $query;
    }

    //追加・更新
    function &getUpdateQuery($model,&$db)
    {
        //学校グループマスタ
        $data = array();
        $data["YEAR"][TEXT]                 = CTRL_YEAR;
        $data["COLLEGE_GRP_CD"][TEXT]       = sprintf("%02d", $model->field["COLLEGE_GRP_CD"]);
        $data["COLLEGE_GRP_NAME"][TEXT]     = $model->field["COLLEGE_GRP_NAME"];
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][FUNC]              = "sysdate()";

        $query = Query::insertSQL($data, "AFT_COLLEGE_GROUP_MST");
        $db->query($query);
        $query = "";

        if ($model->selectLeft) {
            $selectSchArr = explode(",", $model->selectLeft);
            foreach ($selectSchArr as $selectSchCd) {
                //入金グループ入金項目データ
                $data = array();
                $data["YEAR"][TEXT]             = CTRL_YEAR;
                $data["COLLEGE_GRP_CD"][TEXT]   = sprintf("%02d", $model->field["COLLEGE_GRP_CD"]);
                $data["SCHOOL_CD"][TEXT]        = $selectSchCd;
                $data["REGISTERCD"][TEXT]       = STAFFCD;
                $data["UPDATED"][FUNC]          = "sysdate()";

                $query = Query::insertSQL($data, "AFT_COLLEGE_GROUP_DAT");
                $db->query($query);
            }
        }
        return;
    }

    //前年度からデータをコピー
    function &getCopyYearQuery($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $setYear = CTRL_YEAR;

        //学校グループマスタ
        $query  = " DELETE FROM AFT_COLLEGE_GROUP_MST ";
        $query .= "  WHERE YEAR = '{$setYear}'";
        $db->query($query);

        $query  = " INSERT INTO AFT_COLLEGE_GROUP_MST( ";
        $query .= "     YEAR, ";
        $query .= "     COLLEGE_GRP_CD, ";
        $query .= "     COLLEGE_GRP_NAME, ";
        $query .= "     REGISTERCD, ";
        $query .= "     UPDATED) ";
        $query .= " SELECT ";
        $query .= "     '{$setYear}', ";
        $query .= "     COLLEGE_GRP_CD, ";
        $query .= "     COLLEGE_GRP_NAME, ";
        $query .= "     '" .STAFFCD. "', ";
        $query .= "     sysdate() ";
        $query .= " FROM ";
        $query .= "     AFT_COLLEGE_GROUP_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '" . ($setYear - 1) . "' ";
        $db->query($query);

        //学校グループデータ
        $query  = " DELETE FROM AFT_COLLEGE_GROUP_DAT ";
        $query .= "  WHERE YEAR = '{$setYear}'";
        $db->query($query);

        $query  = " INSERT INTO AFT_COLLEGE_GROUP_DAT( ";
        $query .= "     YEAR, ";
        $query .= "     COLLEGE_GRP_CD, ";
        $query .= "     SCHOOL_CD, ";
        $query .= "     REGISTERCD, ";
        $query .= "     UPDATED) ";
        $query .= " SELECT ";
        $query .= "     '{$setYear}', ";
        $query .= "     COLLEGE_GRP_CD, ";
        $query .= "     SCHOOL_CD, ";
        $query .= "     '" .STAFFCD. "', ";
        $query .= "     sysdate() ";
        $query .= " FROM ";
        $query .= "     AFT_COLLEGE_GROUP_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '" . ($setYear - 1) . "' ";
        $db->query($query);

        $db->commit();
        Query::dbCheckIn($db);

        return;
    }

}
?>
