<?php

require_once('for_php7.php');

class knjz175Query extends Query {

    //選択科目マスタが登録済みか
    function cnt_Electclass($year)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     v_electclass_mst ";
        $query .= " WHERE ";
        $query .= "     year = '".$year."' ";
        return $query;
    }

    //左画面リスト
    function Listdata($model)
    {
        $query  = " SELECT w1.pgroupcd,w1.pgroupname,w1.plessoncnt,w1.pframecnt, ";
        $query .= "        w2.cgroupcdname,w2.clessoncnt,w2.cframecnt ";
        $query .= " FROM ";
        $query .= "    (SELECT ";
        $query .= "         t1.groupcd as pgroupcd, ";
        $query .= "         t1.groupname as pgroupname, ";
        $query .= "         max(t2.lessoncnt) as plessoncnt, ";
        $query .= "         max(t2.framecnt) as pframecnt ";
        $query .= "     FROM ";
        $query .= "         v_electclass_mst t1 ";
        $query .= "     LEFT JOIN chair_dat t2 ";
        $query .= "            ON t1.groupcd = t2.groupcd ";
        $query .= "           AND t1.year = t2.year ";
        $query .= "           AND t2.semester = '".CTRL_SEMESTER."' ";
        $query .= "     WHERE t1.year = '".CTRL_YEAR."' ";
        $query .= "       AND t1.groupcd NOT IN(SELECT child_grpcd ";
        $query .= "                               FROM electclass_rela_dat ";
        $query .= "                              WHERE year = '".CTRL_YEAR."') ";
        $query .= "     GROUP BY ";
        $query .= "         t1.groupcd,t1.groupname ) w1 ";
        $query .= " LEFT JOIN ";
        $query .= "    (SELECT ";
        $query .= "         t3.groupcd ||'  '|| t3.groupname as cgroupcdname, ";
        $query .= "         t5.parents_grpcd, ";
        $query .= "         max(t4.lessoncnt) as clessoncnt, ";
        $query .= "         max(t4.framecnt) as cframecnt ";
        $query .= "     FROM ";
        $query .= "         electclass_rela_dat t5, ";
        $query .= "         v_electclass_mst t3 ";
        $query .= "     LEFT JOIN chair_dat t4 ";
        $query .= "            ON t3.groupcd = t4.groupcd ";
        $query .= "           AND t3.year = t4.year ";
        $query .= "           AND t4.semester = '".CTRL_SEMESTER."' ";
        $query .= "     WHERE t3.year = t5.year ";
        $query .= "       AND t3.groupcd = t5.child_grpcd ";
        $query .= "       AND t3.year = '".CTRL_YEAR."' ";
        $query .= "     GROUP BY ";
        $query .= "         t3.groupcd,t3.groupname,t5.parents_grpcd) w2 ";
        $query .= " ON w1.pgroupcd = w2.parents_grpcd ";
        $query .= " ORDER BY ";
        $query .= "     w1.pgroupcd ";
        return $query;
    }

    //右画面子群の取得
    function getChGroup($pcd,$year)
    {
        $query  = " SELECT ";
        $query .= "     w1.child_grpcd, ";
        $query .= "     w2.groupname ";
        $query .= " FROM ";
        $query .= "     electclass_rela_dat w1, ";
        $query .= "     v_electclass_mst w2 ";
        $query .= " WHERE ";
        $query .= "     w1.child_grpcd = w2.groupcd AND ";
        $query .= "     w1.parents_grpcd = '".$pcd."' AND ";
        $query .= "     w1.year = w2.year AND ";
        $query .= "     w2.year = '".$year."' ";
        return $query;
    }

    //右画面群の取得
    function getGroup($year,$pgroupcd)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     w1.groupcd, ";
        $query .= "     w1.groupname ";
        $query .= " FROM ";
        $query .= "     v_electclass_mst w1 ";
        $query .= " WHERE ";
        $query .= "     w1.year = '".$year."' AND ";
        $query .= "     w1.groupcd NOT IN(SELECT distinct ";
        $query .= "                             child_grpcd ";
        $query .= "                         FROM ";
        $query .= "                             electclass_rela_dat ";
        $query .= "                         WHERE ";
        $query .= "                             year = '".$year."' ";
        $query .= "                         ) AND ";
        $query .= "     w1.groupcd NOT IN(SELECT ";
        $query .= "                             parents_grpcd ";
        $query .= "                         FROM ";
        $query .= "                             electclass_rela_dat ";
        $query .= "                         WHERE ";
        $query .= "                             year = '".$year."' ";
        $query .= "                         )  AND ";
        $query .= "     w1.groupcd NOT IN ('".$pgroupcd."') ";
        $query .= " ORDER BY ";
        $query .= "     w1.groupcd ";
        return $query;
    }

    //コピー前チェック
    function cntElectRela($year)
    {
        $db = Query::dbCheckOut();

        $query = "SELECT COUNT(*) FROM electclass_rela_dat WHERE year = '".$year."' ";

        $cntelectclass = $db->getOne($query);
        Query::dbCheckIn($db);
        return $cntelectclass;
    }

    //前年度からのコピー
    function &getCopyQuery($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $query  = " INSERT INTO electclass_rela_dat ";
        $query .= "           ( year, ";
        $query .= "             parents_grpcd, ";
        $query .= "             child_grpcd, ";
        $query .= "             registercd, ";
        $query .= "             updated ) ";
        $query .= "   (SELECT ";
        $query .= "         '".CTRL_YEAR."', ";
        $query .= "         parents_grpcd, ";
        $query .= "         child_grpcd, ";
        $query .= "         '".STAFFCD."', ";
        $query .= "         sysdate() ";
        $query .= "     FROM ";
        $query .= "         electclass_rela_dat ";
        $query .= "     WHERE ";
        $query .= "         year = '".(CTRL_YEAR-1)."' AND ";
        $query .= "         parents_grpcd in(SELECT ";
        $query .= "                             groupcd ";
        $query .= "                         FROM ";
        $query .= "                             v_electclass_mst ";
        $query .= "                         WHERE ";
        $query .= "                             year = '".CTRL_YEAR."' ";
        $query .= "                         ) AND ";
        $query .= "         child_grpcd in(SELECT ";
        $query .= "                             groupcd ";
        $query .= "                         FROM ";
        $query .= "                             v_electclass_mst ";
        $query .= "                         WHERE ";
        $query .= "                             year = '".CTRL_YEAR."' ";
        $query .= "                         ) ";
        $query .= "     ) ";

        $db->query($query);
        $db->commit();
        Query::dbCheckIn($db);
       return true;
    }

    //更新前チェック
    function cntCgrp($model)
    {
        $selectdata = str_replace(",","','",$model->selectdata);

        $query  = " SELECT max(lessoncnt) as maxlesson, max(framecnt) as maxframe ";
        $query .= "   FROM chair_dat ";
        $query .= "  WHERE year     = '".CTRL_YEAR."' ";
        $query .= "    AND semester = '".CTRL_SEMESTER."' ";
        $query .= "    AND groupcd  IN ('".$selectdata."') ";
        return $query;
    }

    // UPDATE
    function &getUpdateQuery($model)
    {
        $selectdata = str_replace(",","','",$model->selectdata);

        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $query  = " DELETE FROM electclass_rela_dat ";
        $query .= "  WHERE parents_grpcd = '".$model->pgroupcd."' ";
        $query .= "    AND year = '".CTRL_YEAR."' ";

        $db->query($query);

        if($model->selectdata != "")
        {
            $selectdata = explode(",", $model->selectdata);

                $query  = " INSERT INTO electclass_rela_dat ";
                $query .= "            (year, ";
                $query .= "             parents_grpcd, ";
                $query .= "             child_grpcd, ";
                $query .= "             registercd, ";
                $query .= "             updated ) ";
                $query .= " VALUES ";

            for($i=0; $i<get_count($selectdata); $i++)
            {
                $sp = ($i==get_count($selectdata)-1)? "" : "," ;

                $query .= "      ('".CTRL_YEAR."', ";
                $query .= "       '".$model->pgroupcd."', ";
                $query .= "       '".$selectdata[$i]."', ";
                $query .= "       '".STAFFCD."', ";
                $query .= "       sysdate() ) " .$sp;
            }
            $db->query($query);
        }
        $db->commit();
        Query::dbCheckIn($db);
        return;
    }
}
?>
