<?php

require_once('for_php7.php');

class knjp741form1
{
    function main(&$model)
    {
        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("list", "POST", "knjp741index.php", "", "right_list");

        $db = Query::dbCheckOut();

        $query = knjp741Query::getStdInfo($model->schregno);
        $model->getGrdInfo = array();
        $model->getGrdInfo = $db->getRow($query, DB_FETCHMODE_ASSOC);
        if ($model->getGrdInfo["GRD_DATE"]) {
            $arg["GRD_DIV"]  = str_replace("-", "/", $model->getGrdInfo["GRD_DATE"])." ".$model->getGrdInfo["NAME1"];
        }

        $arg["SCHREGNO"] = $model->schregno;
        $arg["NAME"]     = $model->getGrdInfo["NAME"];
        $arg["HR_NAME"]  = $model->getGrdInfo["HR_NAME"];
        $arg["ATTENDNO"] = $model->getGrdInfo["ATTENDNO"];

        $arg["YEAR"] = View::alink("knjp741index.php", "<font color=\"white\">年度</font>", "",
                        array("cmd"=>"sort", "sort"=>"YEAR")) .$order[$model->sort["YEAR"]];

        $arg["SUBCLASSCD"] = View::alink("knjp741index.php", "<font color=\"white\">科目名</font>", "",
                        array("cmd"=>"sort", "sort"=>"SUBCLASSCD")) .$order[$model->sort["SUBCLASSCD"]];

        $zaisekiCol = 4;
        if ($model->Properties["useProvFlg"] == '1') {
            $arg["useProvFlg"] = "1";
            $zaisekiCol = 5;
        }
        $arg["ZAISEKI_COL"] = $zaisekiCol;
        //学籍賞罰データよりデータを取得
        $yearAnu = "";          //前データ年次
        $sub = "";          //前データ科目
        $firstflg = true;   //初回フラグ
        $setval = array();  //出力データ配列
        $totalCredit = 0;   //合計単位
        $repayDivName = array("1" => "口座", "2" => "口座2", "3" => "現金");
        if ($model->schregno) {
            $result = $db->query(knjp741Query::selectQuery($model));
            $soeji = 0;
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {

                $setKey = $row["SCHOOLCD"].":".$row["SCHOOL_KIND"].":".$row["YEAR"].":".$row["REPAY_SLIP_NO"];
                $setval = $row;
                //hidden
                knjCreateHidden($objForm, "CHECKED{$soeji}", $setKey);
                $soeji++;

                $setval["REPAY_DATE"]  = str_replace("-", "/", $setval["REPAY_DATE"]);
                $setval["REPAID_DATE"] = str_replace("-", "/", $setval["REPAID_DATE"]);
                $setval["REPAY_DIV"]   = $repayDivName[$row["REPAY_DIV"]];
                $arg["data"][] = $setval;
            }
        }
        Query::dbCheckIn($db);

        //hiddenを作成する
        //hidden
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "clear", "0");

        $arg["finish"]  = $objForm->get_finish();
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjp741Form1.html", $arg);
    }

}
?>
