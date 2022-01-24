<?php

require_once('for_php7.php');

class knjz450_2Query extends Query {

    //選択した級・段位
    function selectQuery($model)
    {
        $query .= " SELECT ";
        $query .= "     QUALIFIED_RANK_DAT.RANK AS VALUE, ";
        $query .= "     NAME_MST.NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     QUALIFIED_RANK_DAT ";
        $query .= " INNER JOIN ";
        $query .= "     NAME_MST ";
        $query .= "     ON QUALIFIED_RANK_DAT.RANK = NAME_MST.NAMECD2 ";
        $query .= " WHERE ";
        $query .= "     QUALIFIED_CD='" . $model->QUALIFIED_CD . "' ";
        $query .= "     AND NAME_MST.NAMECD1='H312' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";
        return $query;
    }
    //級・段位一覧
    function selectNoGroupQuery($isGroup_array,$model)
    {
        $query .= " SELECT ";
        $query .= "     NAME_MST.NAMECD2 AS VALUE, ";
        $query .= "     NAME_MST.NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " LEFT JOIN ";
        $query .= "     QUALIFIED_RANK_DAT ";
        $query .= "     ON QUALIFIED_RANK_DAT.RANK = NAME_MST.NAMECD2 ";
        $query .= "     AND QUALIFIED_CD='" . $model->QUALIFIED_CD . "' ";
        $query .= " WHERE ";
        $query .= "     NAMECD1='H312' ";
        $query .= "     AND QUALIFIED_RANK_DAT.RANK IS NULL ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";
        return $query;
    }
    //QUALIFIED_RANK_DAT.NOT_PRINTの有無を確認
    function getSyscatColumns()
    {
        $query  = " SELECT ";
        $query .= "     COUNT(COLNAME) ";
        $query .= " FROM ";
        $query .= "     SYSCAT.COLUMNS ";
        $query .= " WHERE ";
        $query .= "     TABNAME = 'QUALIFIED_RANK_DAT' ";
        $query .= "     AND COLNAME = 'NOT_PRINT' ";

        return $query;
    }
    //得点入力用のリストを取得
    function getList($model, $rankList)
    {
        $query  = " SELECT ";
        $query .= "     Q_RANK.RANK ";
        $query .= "     , Q_RANK.NOT_PRINT ";
        $query .= "     , Q_RANK.SCORE ";
        $query .= "     , NAME.NAME1 ";
        $query .= " FROM ";
        $query .= "     QUALIFIED_RANK_DAT Q_RANK ";
        $query .= " INNER JOIN NAME_MST NAME ";
        $query .= "     ON Q_RANK.RANK = NAME.NAMECD2 ";
        $query .= " WHERE ";
        $query .= "     Q_RANK.QUALIFIED_CD = '".$model->QUALIFIED_CD."' ";
        $query .= "     AND Q_RANK.RANK IN ('".implode("','", $rankList)."') ";
        $query .= "     AND NAME.NAMECD1='H312' ";

        return $query;
    }

    //存在チェック -- QUALIFIED_RANK_DAT
    function getQualifiedRankDat($model, $rank) {
        $query  = " SELECT ";
        $query .= "      COUNT(*) ";
        $query .= " FROM ";
        $query .= "     QUALIFIED_RANK_DAT ";
        $query .= " WHERE ";
        $query .= "     QUALIFIED_CD = '".$model->QUALIFIED_CD."' ";
        $query .= "     AND RANK = '".$rank."' ";

        return $query;
    }

    /**
     * UPDATE
     */
    function &getUpdateQuery($model)
    {        
        //グループコード配列に格納
        $array = explode (",", $model->selectdata);

        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $query = "DELETE FROM QUALIFIED_RANK_DAT WHERE QUALIFIED_CD = '" .$model->QUALIFIED_CD ."' AND RANK NOT IN ('".str_replace(",", "','", $model->selectdata)."') ";
        $db->query($query);

        //利用者グループデータの登録
        if ($array[0] !="") {
            for ($i = 0; $i < get_count($array); $i++){
                //項目
                $data = array();
                $data["QUALIFIED_CD"][TEXT] = $model->QUALIFIED_CD;
                $data["RANK"][TEXT]         = $array[$i];
                $data["REGISTERCD"][TEXT]   = STAFFCD ;
                $data["UPDATED"][FUNC]      = "sysdate()";
                if (0 < $db->getOne(knjz450_2Query::getQualifiedRankDat($model, $array[$i]))) {
                    $where  = " WHERE ";
                    $where .= "     QUALIFIED_CD = '".$model->QUALIFIED_CD."' ";
                    $where .= "     AND RANK = '".$array[$i]."' ";
                    $query = Query::updateSQL($data, "QUALIFIED_RANK_DAT", $where);
                } else {
                    $query = Query::insertSQL($data, "QUALIFIED_RANK_DAT");
                }
                $db->query($query);
            }
        }
        $db->commit(); // トランザクションをコミットする。

        Query::dbCheckIn($db);
        return;
    }
    function &getUpdateQuery2($model)
    {
        if(is_object($model->setKey) or is_array($model->setKey))
        {
            $db = Query::dbCheckOut();
            $db->autoCommit(false);

            foreach ($model->setKey as $rank) {
                //項目
                $data = array();
                $data["NOT_PRINT"][TEXT]    = VARS::post("NOT_PRINT-".$rank);
                $data["SCORE"][NUMBER]      = VARS::post("SCORE-".$rank);
                $data["REGISTERCD"][TEXT]   = STAFFCD ;
                $data["UPDATED"][FUNC]      = "sysdate()";

                $where  = " WHERE ";
                $where .= "     QUALIFIED_CD = '".$model->QUALIFIED_CD."' ";
                $where .= "     AND RANK = '".$rank."' ";
                $query = Query::updateSQL($data, "QUALIFIED_RANK_DAT", $where);

                $db->query($query);
            }
            $db->commit(); // トランザクションをコミットする。

            Query::dbCheckIn($db);
        }
        return;
    }
}
?>
