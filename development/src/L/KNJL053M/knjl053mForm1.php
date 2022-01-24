<?php

require_once('for_php7.php');

class knjl053mForm1
{
    function main(&$model)
    {
        $objForm      = new form;
        $arg["start"] = $objForm->get_start("main", "POST", "knjl053mindex.php", "", "main");

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["TOP"]["YEAR"] = $model->year;

        //会場情報
        $query = knjl053mQuery::getHallDat($model);
        $result = $db->query($query);
        $hallDat = array();
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $hallDat[] = array('EXAMHALL_NAME'  => $row["EXAMHALL_NAME"],
                               'EXAMHALLCD'     => $row["EXAMHALLCD"],
                               'CDNAME'         => $row["CDNAME"],
                               'S_RECEPTNO'     => $row["S_RECEPTNO"],
                               'E_RECEPTNO'     => $row["E_RECEPTNO"],
                               'RECEPT_CNT'     => $row["RECEPT_CNT"]);
            $arg["LTOP"][] = $row["CDNAME"];
            $arg["RTOP"][] = $row["CDNAME"];
        }

        //科目情報
        $query = knjl053mQuery::getSubclassDetail($model);
        $result = $db->query($query);
        $subclassDat = array();
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $subclassDat[] = array('TESTNAME'   => $row["LABEL"],
                                   'TESTVAL'    => $row["VALUE"]);
        }

        $kamokuKugiri = get_count($subclassDat) % 2;
        $kamokuHalf = floor(get_count($subclassDat) / 2);
        //左科目数の決定、データ数/2で余りがあれば左科目数を＋１
        //例：13科目 左：7 右：6
        $tableChange = $kamokuKugiri == 0 ? $kamokuHalf : $kamokuHalf + 1;

        $setTableName = array("L", "R");
        $setTableNameCnt = 0;
        $dataCnt = 0;
        $scoreArgCnt = 0;
        foreach ($subclassDat as $subclassKey => $subclassVal) {
            //argにセットする一行分のデータ配列（科目名～会場15）
            // $setDatas["TESTNAME"] = 科目名
            // $setDatas["LDatas"][0]["JOUKYOU"] = 1(入力完:1 OR 未:0)
            // $setDatas["LDatas"][1]["JOUKYOU"] = 0
            // $setDatas["LDatas"][2]["JOUKYOU"] = 1
            // .....
            // $setDatas["LDatas"][14]["JOUKYOU"] = 1
            $setDatas = array();

            //科目名出力
            if ($tableChange <= $dataCnt) {
                $setTableNameCnt++;
                $dataCnt = 0;
                $scoreArgCnt = 0;
            }

            //HTMLのdataLかdataRがセットされる
            $setTest = "data".$setTableName[$setTableNameCnt];
            $setDatas["TESTNAME"] = $subclassVal["TESTNAME"];
            //得点出力
            foreach ($hallDat as $hallKey => $hallVal) {
                $query = knjl053mQuery::getEntexamControlDat($model, $subclassVal["TESTVAL"], $hallVal["EXAMHALLCD"]);
                $entexamControlDat = $db->getRow($query);

                //HTMLのLDatasかRDatasがセットされる
                $setScore = $setTableName[$setTableNameCnt]."Datas";

                list($testsubclasscd, $testpapercd, $perfect) = preg_split("/-/", $subclassVal["TESTVAL"]);
                $examhallcd = $hallVal["EXAMHALLCD"];
                if (is_array($entexamControlDat)) {
                    $extra = "onBlur=\"return check_val(this);\" ";
                    $joukyou = knjCreateTextBox($objForm, '1', "JOUKYOU_{$testsubclasscd}_{$testpapercd}_{$examhallcd}", 1, 1, $extra);
                    $setDatas[$setScore][] = array("JOUKYOU" => $joukyou, "SET_STYLE" => "ffffff");
                } else {
                    $extra = "onBlur=\"return check_val(this);\" ";
                    $joukyou = knjCreateTextBox($objForm, '0', "JOUKYOU_{$testsubclasscd}_{$testpapercd}_{$examhallcd}", 1, 1, $extra);
                    $setDatas[$setScore][] = array("JOUKYOU" => $joukyou, "SET_STYLE" => "ff0000");
                }
            }
            //一行分データをセット
            $arg[$setTest][] = $setDatas;

            $dataCnt++;
            $scoreArgCnt++;
        }

        /**********/
        /* ボタン */
        /**********/
        //更新
        $extra = "style=\"font-size:20;font-weight:bolder;\" onclick=\"btn_submit('update');\"";
        $arg["button"]["btn_reload"] = knjCreateBtn($objForm, "btn_reload", "更 新", $extra);
        //終了
        $extra = "style=\"font-size:20;font-weight:bolder;\" onclick=\"closeWin();\"";
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        /**********/
        /* hidden */
        /**********/
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "year", $model->year);

        //DB切断
        Query::dbCheckIn($db);

        //インラインフレーム用Javascriptタグ生成
        $arg["IFRAME"] = View::setIframeJs();
        
        $arg["finish"]  = $objForm->get_finish();
        View::toHTML($model, "knjl053mForm1.html", $arg);
    }
}
?>