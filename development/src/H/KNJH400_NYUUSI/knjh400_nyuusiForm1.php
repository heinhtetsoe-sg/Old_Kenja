<?php

require_once('for_php7.php');

class knjh400_nyuusiForm1
{
    public function main(&$model)
    {
        $objForm      = new form();
        $arg["start"] = $objForm->get_start("edit", "POST", "knjh400_nyuusiindex.php", "", "edit");

        $db     = Query::dbCheckOut();

        $arg['SCHREGNO'] = $model->schregno;
        $arg['NAME'] = $db->getOne(knjh400_nyuusiQuery::getName($model));

        $result = $db->query(knjh400_nyuusiQuery::selectQuery($model));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $row2 = $db->getRow(knjh400_nyuusiQuery::selectQuery2($model, $row['BASE_REMARK1']), DB_FETCHMODE_ASSOC);
            //志望コース
            $query = knjh400_nyuusiQuery::getGeneralMst($model, "02");
            $extra = "";
            for ($idx = 1; $idx <= 4; $idx++) {
                $row["HOPE_COURSE".$idx] = makeCmb($objForm, $arg, $db, $query, $row2["HOPE_COURSE".$idx], "HOPE_COURSE".$idx, $extra, 1, "BLANK");
            }

            //特待コースコンボ
            $query = knjh400_nyuusiQuery::getHonordivQuery($model);
            $row["HONORDIV"] = makeCmb($objForm, $arg, $db, $query, $row2["HONORDIV"], "HONORDIV".$idx, $extra, 1, "BLANK");

            //特待理由コースコンボ
            $query = knjh400_nyuusiQuery::getHonorReasondivQuery($model);
            $row["HONOR_REASONDIV"] = makeCmb($objForm, $arg, $db, $query, $row2["HONOR_REASONDIV"], "HONOR_REASONDIV".$idx, $extra, 1, "BLANK");

            $query = knjh400_nyuusiQuery::getFinschoolName($row2["FINSCHOOLCD"]);
            $fsArray = $db->getRow($query, DB_FETCHMODE_ASSOC);
            $row["FINSCHOOLNAME"] = $fsArray["FINSCHOOL_DISTCD_NAME"].$fsArray["FINSCHOOL_NAME"];

            $row['GADDRESS1'] = $row2['GADDRESS1'];
            $row['GADDRESS2'] = $row2['GADDRESS2'];
            $row['GTELNO'] = $row2['GTELNO'];
            $row['SEX'] = $row2['SEX'];

            $query = knjh400_nyuusiQuery::getNameMst($model, "Z002");
            $extra = "";
            $row['SEX'] = makeCmb($objForm, $arg, $db, $query, $row2['SEX'], "SEX", $extra, 1, "BLANK");

            $row['BIRTHDAY'] = str_replace('-', '/', $row2['BIRTHDAY']);

            $arg['data'] = $row;
            $examno = $row['BASE_REMARK1'];
        }

        //教科名取得
        $headerKyoka = array();
        $keyKyoka = 0;
        $result = $db->query(knjh400_nyuusiQuery::getSettingMst($model, "L008"));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $headerKyoka[$keyKyoka]["TITLE"] = $row["NAME1"];
            $headerKyoka[$keyKyoka]["SEQ"] = $row["SEQ"];
            $keyKyoka++;
        }
        $arg["headerKyoka"] = $headerKyoka;

        $flg = false;
        $result = $db->query(knjh400_nyuusiQuery::selectQuery3($model, $examno));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            for ($i = 1; $i <= 9; $i++) {
                $num = sprintf("%02d", $i);
                $arg['data']["kyoka1"][] = $row["KYOKA1_{$num}"];
                $arg['data']["kyoka2"][] = $row["KYOKA2_{$num}"];
                $arg['data']["kyoka3"][] = $row["KYOKA3_{$num}"];
            }
            $arg['data']['AVERAGE_ALL1'] = $row["AVERAGE_ALL1"];
            $arg['data']['AVERAGE_ALL2'] = $row["AVERAGE_ALL2"];
            $arg['data']['AVERAGE_ALL3'] = $row["AVERAGE_ALL3"];

            $arg['data']['KESSEKI1'] = $row["KESSEKI1"];
            $arg['data']['KESSEKI2'] = $row["KESSEKI2"];
            $arg['data']['KESSEKI3'] = $row["KESSEKI3"];
            $arg['data']['KESSEKI_ALL'] = intval($row["KESSEKI1"]) + intval($row["KESSEKI2"]) + intval($row["KESSEKI3"]);
            $flg = true;
        }
        if (!$flg) {
            for ($i = 1; $i <= 9; $i++) {
                $num = sprintf("%02d", $i);
                $arg['data']["kyoka1"][] = '';
                $arg['data']["kyoka2"][] = '';
                $arg['data']["kyoka3"][] = '';
            }
            $arg['data']['AVERAGE_ALL1'] = '';
            $arg['data']['AVERAGE_ALL2'] = '';
            $arg['data']['AVERAGE_ALL3'] = '';
            $arg['data']['KESSEKI1'] = '';
            $arg['data']['KESSEKI2'] = '';
            $arg['data']['KESSEKI3'] = '';
            $arg['data']['KESSEKI_ALL'] = '';
        }
        //終了ボタン
        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["btn_back"] = knjCreateBtn($objForm, 'btn_back', '戻 る', $extra);

        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();
        View::toHTML($model, "knjh400_nyuusiForm1.html", $arg);
    }
}
function makeCmb(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $space = "")
{
    $result1 = $db->query($query);
    while ($row1 = $result1->fetchrow(DB_FETCHMODE_ASSOC)) {
        if ($value == $row1["VALUE"]) {
            $label = $row1["LABEL"];
        }
    }
    return $label;
}
